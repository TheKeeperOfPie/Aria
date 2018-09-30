package com.winsonchiu.aria.queue

import android.net.Uri
import androidx.core.net.toUri
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.JsonDataException
import com.squareup.moshi.JsonReader
import com.squareup.moshi.JsonWriter
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import io.reactivex.annotations.CheckReturnValue
import java.io.IOException
import java.lang.reflect.Type

internal object QueueMoshi {

    val moshi = Moshi.Builder()
            .add(
                    RuntimeJsonAdapterFactory.of(QueueOp::class.java, "type")
                            .registerSubtype(QueueOp.AddToEnd::class.java, QueueOp.AddToEnd::class.java.simpleName)
                            .registerSubtype(QueueOp.AddNext::class.java, QueueOp.AddNext::class.java.simpleName)
                            .registerSubtype(QueueOp.Shuffle::class.java, QueueOp.Shuffle::class.java.simpleName)
                            .registerSubtype(QueueOp.ReplaceAll::class.java, QueueOp.ReplaceAll::class.java.simpleName)
                            .registerSubtype(QueueOp.Move::class.java, QueueOp.Move::class.java.simpleName)
                            .registerSubtype(QueueOp.Remove::class.java, QueueOp.Remove::class.java.simpleName)
            )
            .add(Uri::class.java, UriAdapter().nullSafe())
            .add(CharSequence::class.java, CharSequenceAdapter().nullSafe())
            .build()
}

class CharSequenceAdapter : JsonAdapter<CharSequence>() {

    override fun fromJson(reader: JsonReader) = reader.nextString()

    override fun toJson(
            writer: JsonWriter,
            value: CharSequence?
    ) {
        writer.value(value?.toString())
    }
}

class UriAdapter : JsonAdapter<Uri>() {

    override fun fromJson(reader: JsonReader) = reader.nextString().toUri()

    override fun toJson(
            writer: JsonWriter,
            value: Uri?
    ) {
        writer.value(value?.toString())
    }

}

/**
 * A JsonAdapter factory for polymorphic types. This is useful when the type is not known before
 * decoding the JSON. This factory's adapters expect JSON in the format of a JSON object with a
 * key whose value is a label that determines the type to which to map the JSON object.
 */
// TODO: Replace with Moshi 1.8 public version
internal class RuntimeJsonAdapterFactory<T>(
        val baseType: Class<T>,
        val labelKey: String
) : JsonAdapter.Factory {
    val labelToType: MutableMap<String, Type> = LinkedHashMap()

    /**
     * Register the subtype that can be created based on the label. When an unknown type is found
     * during encoding an [IllegalArgumentException] will be thrown. When an unknown label
     * is found during decoding a [JsonDataException] will be thrown.
     */
    fun registerSubtype(
            subtype: Class<out T>?,
            label: String?
    ): RuntimeJsonAdapterFactory<T> {
        if (subtype == null) throw NullPointerException("subtype == null")
        if (label == null) throw NullPointerException("label == null")
        if (labelToType.containsKey(label) || labelToType.containsValue(subtype)) {
            throw IllegalArgumentException("Subtypes and labels must be unique.")
        }
        labelToType[label] = subtype
        return this
    }

    override fun create(
            type: Type,
            annotations: Set<Annotation>,
            moshi: Moshi
    ): JsonAdapter<*>? {
        if (Types.getRawType(type) !== baseType || !annotations.isEmpty()) {
            return null
        }
        val size = labelToType.size
        val labelToAdapter = LinkedHashMap<String, JsonAdapter<Any>>(size)
        val typeToLabel = LinkedHashMap<Type, String>(size)
        for (entry in labelToType.entries) {
            val label = entry.key
            val typeValue = entry.value
            typeToLabel[typeValue] = label
            labelToAdapter[label] = moshi.adapter<Any>(typeValue)
        }
        val objectJsonAdapter = moshi.adapter(Any::class.java)
        return RuntimeJsonAdapter(
                labelKey, labelToAdapter, typeToLabel,
                objectJsonAdapter
        ).nullSafe()
    }

    internal class RuntimeJsonAdapter(
            val labelKey: String,
            val labelToAdapter: Map<String, JsonAdapter<Any>>,
            val typeToLabel: Map<Type, String>,
            val objectJsonAdapter: JsonAdapter<Any>
    ) : JsonAdapter<Any>() {

        @Throws(IOException::class)
        override fun fromJson(reader: JsonReader): Any? {
            val peekedToken = reader.peek()
            if (peekedToken !== JsonReader.Token.BEGIN_OBJECT) {
                throw JsonDataException(
                        "Expected BEGIN_OBJECT but was " + peekedToken
                                + " at path " + reader.getPath()
                )
            }
            val jsonValue = reader.readJsonValue()
            val jsonObject = jsonValue as Map<*, *>
            val label = jsonObject[labelKey] ?: throw JsonDataException("Missing label for $labelKey")
            if (label !is String) {
                throw JsonDataException(
                        ("Label for '"
                                + labelKey
                                + "' must be a string but was "
                                + label
                                + ", a "
                                + label.javaClass)
                )
            }
            val adapter = labelToAdapter[label] ?: throw JsonDataException(
                    ("Expected one of "
                            + labelToAdapter.keys
                            + " for key '"
                            + labelKey
                            + "' but found '"
                            + label
                            + "'. Register a subtype for this label.")
            )
            return adapter.fromJsonValue(jsonValue)
        }

        @Throws(IOException::class)
        override fun toJson(
                writer: JsonWriter,
                value: Any?
        ) {
            val type = value!!.javaClass
            val label = typeToLabel[type] ?: throw IllegalArgumentException(
                    ("Expected one of "
                            + typeToLabel.keys
                            + " but found "
                            + value
                            + ", a "
                            + value.javaClass
                            + ". Register this subtype.")
            )
            val adapter = labelToAdapter[label]
            @Suppress("UNCHECKED_CAST") val jsonValue = adapter!!.toJsonValue(value) as Map<String, Any>

            val valueWithLabel = LinkedHashMap<String, Any>(1 + jsonValue.size)
            valueWithLabel[labelKey] = label
            valueWithLabel.putAll(jsonValue)
            objectJsonAdapter.toJson(writer, valueWithLabel)
        }

        override fun toString(): String {
            return "RuntimeJsonAdapter($labelKey)"
        }
    }

    companion object {

        /**
         * @param baseType The base type for which this factory will create adapters. Cannot be Object.
         * @param labelKey The key in the JSON object whose value determines the type to which to map the
         * JSON object.
         */
        @CheckReturnValue
        fun <T> of(
                baseType: Class<T>?,
                labelKey: String?
        ): RuntimeJsonAdapterFactory<T> {
            if (baseType == null) throw NullPointerException("baseType == null")
            if (labelKey == null) throw NullPointerException("labelKey == null")
            if (baseType == Any::class.java) {
                throw IllegalArgumentException(
                        "The base type must not be Object. Consider using a marker interface."
                )
            }
            return RuntimeJsonAdapterFactory(baseType, labelKey)
        }
    }
}