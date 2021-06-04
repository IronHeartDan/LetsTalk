package com.danapps.letstalk.contentproviders

import android.content.ContentResolver
import android.content.ContentUris
import android.content.Context
import android.database.Cursor
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import com.danapps.letstalk.models.Media


class MediaLiveData(val context: Context) : MediaProviderLiveData<List<Media>>(context, uri) {
    override fun getContentProviderValue(): List<Media> {
        return getMedia()
    }

    companion object {
        private val uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
    }

    private fun getMedia(): List<Media> {
        val galleryMedia = mutableListOf<Media>()
        val cursor: Cursor?
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            cursor = context.contentResolver.query(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                arrayOf(
                    MediaStore.Images.ImageColumns._ID,
                    MediaStore.Images.ImageColumns.DATE_MODIFIED
                ),
                Bundle().apply
                {
                    // Sort function
                    putStringArray(
                        ContentResolver.QUERY_ARG_SORT_COLUMNS,
                        arrayOf(MediaStore.Images.ImageColumns.DATE_MODIFIED)
                    )
                    putInt(
                        ContentResolver.QUERY_ARG_SORT_DIRECTION,
                        ContentResolver.QUERY_SORT_DIRECTION_DESCENDING
                    )
                }, null
            )
        } else {
            cursor = context.contentResolver.query(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                arrayOf(
                    MediaStore.Images.Media._ID,
                    MediaStore.Images.ImageColumns.DATE_MODIFIED
                ),
                null,
                null,
                MediaStore.Images.ImageColumns.DATE_MODIFIED + " DESC"
            )
        }
        if (cursor != null) {
            while (cursor.moveToNext()) {
                val l = cursor.getLong(cursor.getColumnIndex(MediaStore.Images.Media._ID))
                var uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                uri = ContentUris.withAppendedId(uri, l)
                galleryMedia.add(Media(l, uri))
            }
            cursor.close()
        }
        return galleryMedia
    }
}