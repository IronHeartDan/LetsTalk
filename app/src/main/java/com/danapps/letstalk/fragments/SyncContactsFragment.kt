package com.danapps.letstalk.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.danapps.letstalk.R
import com.danapps.letstalk.`interface`.ContactsSyncInterface
import com.danapps.letstalk.activities.InitActivity
import kotlinx.android.synthetic.main.fragment_sync_contacts.view.*

class SyncContactsFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_sync_contacts, container, false)

        view.initSyncContacts.setOnClickListener {
            it.isEnabled = false
            view.initContactsLater.isEnabled = false
            view.syncContactsProgressBar.visibility = View.VISIBLE
            (activity as InitActivity).syncContacts(object : ContactsSyncInterface {
                override fun finished() {
                    view.syncContactsProgressBar.visibility = View.GONE
                    (activity as InitActivity).initializedUser()
                }

                override fun error(error: String?) {
                    view.syncContactsProgressBar.visibility = View.GONE
                    it.isEnabled = true
                    view.initContactsLater.isEnabled = true
                    Toast.makeText(context, error, Toast.LENGTH_SHORT).show()
                }

            })
        }

        view.initContactsLater.setOnClickListener {
            (activity as InitActivity).initializedUser()
        }
        return view
    }
}