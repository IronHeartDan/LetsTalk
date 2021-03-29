package com.danapps.letstalk.contentproviders

import android.content.ContentResolver
import android.content.Context
import android.database.Cursor
import android.os.Build
import android.os.Bundle
import android.provider.ContactsContract
import com.danapps.letstalk.models.Contacts

class ContactsLiveData(
    private val context: Context
) : ContactsProviderLiveData<List<Contacts>>(context, uri) {

    override fun getContentProviderValue(): List<Contacts> {
        return getContacts()
    }

    companion object {
        private val uri = ContactsContract.Contacts.CONTENT_URI
    }


    private fun getContacts(): List<Contacts> {
        val contacts = mutableListOf<Contacts>()
        val cursor: Cursor?
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            cursor = context.contentResolver.query(
                ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                arrayOf(
                    ContactsContract.Contacts.DISPLAY_NAME,
                    ContactsContract.CommonDataKinds.Phone.NUMBER
                ),
                Bundle().apply
                {
                    // Sort function
                    putStringArray(
                        ContentResolver.QUERY_ARG_SORT_COLUMNS,
                        arrayOf(ContactsContract.Contacts.DISPLAY_NAME)
                    )
                }, null
            )
        } else {
            cursor = context.contentResolver.query(
                ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                arrayOf(
                    ContactsContract.Contacts.DISPLAY_NAME,
                    ContactsContract.CommonDataKinds.Phone.NUMBER
                ),
                null,
                null,
                ContactsContract.Contacts.DISPLAY_NAME + "ASC"
            )
        }
        while (cursor!!.moveToNext()) {
            val name =
                cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME))
            val number =
                cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER))
            contacts.add(Contacts(name, number))
        }
        cursor.close()
        return contacts
    }
}