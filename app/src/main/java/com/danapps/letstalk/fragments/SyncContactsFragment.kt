package com.danapps.letstalk.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.danapps.letstalk.InitActivity
import com.danapps.letstalk.R
import kotlinx.android.synthetic.main.fragment_sync_contacts.view.*

class SyncContactsFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_sync_contacts, container, false)

        view.initSyncContacts.setOnClickListener {
            view.syncContactsProgressBar.visibility = View.VISIBLE
            (activity as InitActivity).syncContacts()
        }

        view.initContactsLater.setOnClickListener {
            (activity as InitActivity).initalizedUser()
        }
        return view
    }
}