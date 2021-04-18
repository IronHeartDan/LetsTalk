package com.danapps.letstalk.sheets

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.danapps.letstalk.R
import com.danapps.letstalk.`interface`.ContactsSyncInterface
import com.danapps.letstalk.activities.MainActivity
import com.danapps.letstalk.activities.MessageActivity
import com.danapps.letstalk.adapters.ContactsAdapter
import com.danapps.letstalk.models.Contact
import com.danapps.letstalk.viewmodel.LetsTalkViewModel
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.gson.Gson
import kotlinx.android.synthetic.main.sheet_new_chat.view.*

class ContactsSheet : BottomSheetDialogFragment() {

    private lateinit var letsTalkViewModel: LetsTalkViewModel
    private lateinit var contactsAdapter: ContactsAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.sheet_new_chat, container, false)

        contactsAdapter = ContactsAdapter()
        contactsAdapter.setNewChatClickListener(object : ContactsAdapter.NewChatClickListener {
            override fun onClick(contact: Contact) {
                val intent = Intent(requireActivity(), MessageActivity::class.java)
                intent.putExtra(
                    "contact",
                    Gson().toJson(contact)
                )
                startActivity(intent)
                (activity as MainActivity).findNavController(R.id.main_nav_host_fragment)
                    .navigateUp()
            }

        })

        view.newChatList.layoutManager = LinearLayoutManager(context)
        view.newChatList.adapter = contactsAdapter

        letsTalkViewModel = ViewModelProvider(
            this,
            ViewModelProvider.AndroidViewModelFactory(requireActivity().application)
        ).get(LetsTalkViewModel::class.java)

        when {
            ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.READ_CONTACTS
            ) == PackageManager.PERMISSION_GRANTED -> {
                letsTalkViewModel.syncedContactsLive.observe(this, {
                    contactsAdapter.submitList(it)
                    view.contactsSheetToolBar.subtitle = "${it.size} Found"
                })
            }
            ActivityCompat.shouldShowRequestPermissionRationale(
                requireActivity(),
                Manifest.permission.READ_CONTACTS
            ) -> {
                AlertDialog.Builder(requireContext())
                    .setTitle("Provide Contacts Permission...")
                    .setMessage("Contacts Permission Is Required By LetsTalk To Show Your Contacts On The App")
                    .setPositiveButton("GRANT") { dialog, _ ->
                        dialog.dismiss()
                        requestPermissions(
                            arrayOf(Manifest.permission.READ_CONTACTS),
                            121
                        )
                    }
                    .setNegativeButton("DENY") { dialog, _ ->
                        dialog.dismiss()
                    }
                    .create()
                    .show()
            }
            else -> {
                requestPermissions(
                    arrayOf(Manifest.permission.READ_CONTACTS),
                    121
                )
            }
        }

        view.contactsSheetToolBar.setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.refreshContacts -> refreshContacts()
            }
            true
        }

        return view
    }

    private fun refreshContacts() {
        Log.d("LetsTalkApplication", "refreshContacts: ")
        view?.newChatList?.visibility = View.GONE
        view?.shimmerLoad?.visibility = View.VISIBLE
        view?.shimmerLoad?.startShimmerAnimation()
        when {
            ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.READ_CONTACTS
            ) == PackageManager.PERMISSION_GRANTED -> {
                Log.d("LetsTalkApplication", "Granted: ")
                letsTalkViewModel.syncContacts(object : ContactsSyncInterface {
                    override fun finished() {
                        Toast.makeText(requireActivity(), "Contacts Refreshed", Toast.LENGTH_SHORT)
                            .show()
                        view?.newChatList?.visibility = View.VISIBLE
                        view?.shimmerLoad?.visibility = View.GONE
                        view?.shimmerLoad?.stopShimmerAnimation()
                    }

                    override fun error(error: String?) {
                        Toast.makeText(requireActivity(), error, Toast.LENGTH_SHORT).show()
                    }

                })
            }
            shouldShowRequestPermissionRationale(
                Manifest.permission.READ_CONTACTS
            ) -> {
                AlertDialog.Builder(requireContext())
                    .setTitle("Provide Contacts Permission...")
                    .setMessage("Contacts Permission Is Required By LetsTalk To Show Your Contacts On The App")
                    .setPositiveButton("GRANT") { dialog, _ ->
                        dialog.dismiss()
                        requestPermissions(
                            arrayOf(Manifest.permission.READ_CONTACTS),
                            121
                        )
                    }
                    .setNegativeButton("DENY") { dialog, _ ->
                        dialog.dismiss()
                    }
                    .create()
                    .show()
            }
            else -> {
                requestPermissions(
                    arrayOf(Manifest.permission.READ_CONTACTS),
                    121
                )
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {

        if (requestCode == 121 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            refreshContacts()
        }
    }
}