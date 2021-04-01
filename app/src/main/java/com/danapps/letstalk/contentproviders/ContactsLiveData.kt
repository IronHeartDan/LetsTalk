package com.danapps.letstalk.contentproviders

import android.content.ContentResolver
import android.content.Context
import android.database.Cursor
import android.os.Build
import android.os.Bundle
import android.provider.ContactsContract
import android.util.Log
import com.danapps.letstalk.models.Contact

class ContactsLiveData(
    private val context: Context
) : ContactsProviderLiveData<List<Contact>>(context, uri) {

    override fun getContentProviderValue(): List<Contact> {
        return getContacts()
    }

    companion object {
        private val uri = ContactsContract.Contacts.CONTENT_URI
    }


    private fun getContacts(): List<Contact> {
        val contacts = mutableListOf<Contact>()
        val cursor: Cursor?
        val normalizedNumbers: HashSet<String> = HashSet()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            cursor = context.contentResolver.query(
                ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                arrayOf(
                    ContactsContract.Contacts.DISPLAY_NAME,
                    ContactsContract.CommonDataKinds.Phone.NUMBER,
                    ContactsContract.CommonDataKinds.Phone.NORMALIZED_NUMBER
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
                    ContactsContract.CommonDataKinds.Phone.NUMBER,
                    ContactsContract.CommonDataKinds.Phone.NORMALIZED_NUMBER
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
            val normalizedNum =
                cursor.getString(cursor.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.NORMALIZED_NUMBER))
            if (normalizedNumbers.add(normalizedNum)) {
                contacts.add(Contact(name, null, number))
            } else {
                Log.d("TEST", "getContacts: Duplicate $normalizedNum")
            }
        }
        cursor.close()
        return contacts
    }
}