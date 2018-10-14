package com.winsonchiu.aria.framework.fragment

import android.os.Bundle
import android.os.Parcelable
import androidx.fragment.app.Fragment
import com.winsonchiu.aria.framework.util.putArgument

@Suppress("UNCHECKED_CAST")
inline fun <FragmentType : Fragment, T : FragmentInitializer<FragmentType>> T.build(block: T.() -> Unit): FragmentType {
    block(this)
    return buildFragment()
}

@Suppress("UNCHECKED_CAST")
fun <FragmentType : Fragment, T : FragmentInitializer<FragmentType>> T.build(): FragmentType {
    return buildFragment()
}

@Suppress("UNCHECKED_CAST")
fun <Input, Type, Output> Fragment.arg(arg: FragmentInitializer<*>.Arg<Input, Type, Output>): Lazy<Output> {
    return lazy { arg.retrieve(arguments) }
}

abstract class FragmentInitializer<out FragmentType : Fragment>(private val fragment: () -> FragmentType) {

    private val argumentsMap = mutableMapOf<String, Any?>()

    fun buildFragment() = fragment().apply {
        arguments = (arguments ?: Bundle()).putExtras()
    }

    @Suppress("unused")
    open inner class Arg<Input, Type, Output>(
            internal val transformInput: (Input) -> Type,
            val key: String,
            private val transformOutput: (Type) -> Output
    ) {

        @Suppress("UNCHECKED_CAST")
        fun retrieve(bundle: Bundle?): Output {
            val get = bundle?.get(key)
            val type = get as Type
            return transformOutput(type)
        }
    }

    inner class BasicArg<Type>(key: String) : Arg<Type, Type, Type>({ it }, key, { it })

    inner class OutputArg<Type, Output>(key: String, transformOutput: (Type) -> Output) : Arg<Type, Type, Output>({ it }, key,  transformOutput)
    inner class InputArg<Input, Type>(key: String, transformInput: (Input) -> Type) : Arg<Input, Type, Type>(transformInput, key, { it })

    protected fun string(key: String) = BasicArg<String?>(key)
    protected fun <Output> string(key: String, toOutput: (String) -> Output) = OutputArg(key, toOutput)
    protected fun <Input> string(fromInput: (Input) -> String, key: String) = InputArg(key, fromInput)
    protected fun <Input, Output> string(fromInput: (Input) -> String, key: String, toOutput: (String) -> Output) = Arg(fromInput, key, toOutput)

    protected fun boolean(key: String) = BasicArg<Boolean>(key)
    protected fun <Output> boolean(key: String, transform: (Boolean) -> Output) = OutputArg(key, transform)

    protected fun long(key: String) = BasicArg<Long>(key)
    protected fun <Output> long(key: String, transform: (Long) -> Output) = OutputArg(key, transform)

    protected fun <ParcelableType : Parcelable?> parcelable(key: String) = BasicArg<ParcelableType>(key)

    infix fun <Input, Type> Arg<Input, Type, *>.put(value: Input) = argumentsMap.set(key, transformInput(value))

    private fun Bundle?.putExtras() = this?.apply {
        for ((key, value) in argumentsMap) {
            putArgument(key, value)
        }
    }
}