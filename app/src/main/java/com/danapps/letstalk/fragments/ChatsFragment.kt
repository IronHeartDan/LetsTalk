package com.danapps.letstalk.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.danapps.letstalk.R
import com.danapps.letstalk.activities.MessageActivity
import com.danapps.letstalk.adapters.ChatsAdapter
import com.danapps.letstalk.models.Contact
import com.danapps.letstalk.viewmodel.LetsTalkViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.gson.Gson
import kotlinx.android.synthetic.main.fragment_chats.view.*

class ChatsFragment : Fragment() {

    private lateinit var letsTalkViewModel: LetsTalkViewModel
    private lateinit var number: String

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_chats, container, false)

        number = FirebaseAuth.getInstance().currentUser!!.phoneNumber!!.substring(3)

        letsTalkViewModel =
            ViewModelProvider(
                requireActivity(),
                ViewModelProvider.AndroidViewModelFactory(requireActivity().application)
            ).get(
                LetsTalkViewModel::class.java
            )

        val adapter = ChatsAdapter()
        view.allChatsList.adapter = adapter
        view.allChatsList.layoutManager = LinearLayoutManager(context)

        letsTalkViewModel.getChats(
            number
        ).observe(requireActivity(), {
            if (it.isNotEmpty()) {
                adapter.submitList(it)
                view.noChatsFound.visibility = View.GONE
                view.allChatsList.visibility = View.VISIBLE

            } else {
                view.noChatsFound.visibility = View.VISIBLE
                view.allChatsList.visibility = View.GONE
            }
        })

        adapter.setOnChatClickListener(object : ChatsAdapter.ChatclickListener {
            override fun onClick(contact: Contact) {
                val intent = Intent(requireActivity(), MessageActivity::class.java)
                intent.putExtra(
                    "contact",
                    Gson().toJson(contact)
                )
                startActivity(intent)
            }

        })

        return view
    }
}