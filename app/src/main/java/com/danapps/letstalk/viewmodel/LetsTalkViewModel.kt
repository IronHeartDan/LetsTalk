package com.danapps.letstalk.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.danapps.letstalk.contentproviders.ContactsLiveData
import com.danapps.letstalk.contentproviders.MediaLiveData

class LetsTalkViewModel(application: Application) : AndroidViewModel(application) {
    val contactsLive = ContactsLiveData(application.applicationContext)
    val mediaLive = MediaLiveData(application.applicationContext)
}