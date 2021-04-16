package com.danapps.letstalk.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.danapps.letstalk.R
import com.danapps.letstalk.adapters.ChatsAdapter
import com.danapps.letstalk.viewmodel.LetsTalkViewModel
import com.google.firebase.auth.FirebaseAuth
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
            adapter.submitList(it)
            Log.d("LetsTalkApplication", "onCreateView: ${it.size}")
        })

//        adapter.setNewChatClickListener(object : NewChatAdapter.NewChatClickListener {
//            override fun onClick(contact: Contact) {
//                val intent = Intent(requireActivity(), ChatActivity::class.java)
//                intent.putExtra(
//                    "contact",
//                    Gson().toJson(contact)
//                )
//                startActivity(intent)
//            }
//
//        })

        return view
    }
}