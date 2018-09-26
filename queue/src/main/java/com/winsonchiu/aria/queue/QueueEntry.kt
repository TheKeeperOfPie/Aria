package com.winsonchiu.aria.queue

import android.net.Uri
import android.os.Parcelable
import com.squareup.moshi.JsonClass
import kotlinx.android.parcel.Parcelize

@Parcelize
@JsonClass(generateAdapter = true)
data class QueueEntry(
        val content: Uri,
        val image: Uri?,
        val metadata: Metadata,
        val timeAddedToQueue: Long = System.currentTimeMillis()
) : Parcelable {

    @Parcelize
    @JsonClass(generateAdapter = true)
    data class Metadata(
            val title: CharSequence?,
            val description: CharSequence?,
            val album: CharSequence?,
            val artist: CharSequence?,
            val genre: CharSequence?,
            val duration: Long
    ) : Parcelable
}