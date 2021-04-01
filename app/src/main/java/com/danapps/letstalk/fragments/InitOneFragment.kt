package com.danapps.letstalk.fragments

import android.app.AlertDialog
import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import com.danapps.letstalk.InitActivity
import com.danapps.letstalk.R
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import kotlinx.android.synthetic.main.fragment_init_one.*
import kotlinx.android.synthetic.main.fragment_init_one.view.*
import java.util.concurrent.TimeUnit

class InitOneFragment : Fragment() {
    private lateinit var mAuth: FirebaseAuth
    var verificationId = ""
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_init_one, container, false)
        mAuth = FirebaseAuth.getInstance()
        view.enterNumber.setOnClickListener {
            val number = view.regNumber.text!!.trim().toString()
            if (!TextUtils.isEmpty(number)) {
                view.showProgress.visibility = View.VISIBLE
                view.enterNumber.isEnabled = false
                if (view.enterNumber.tag == "0") {
                    AlertDialog.Builder(requireContext())
                        .setTitle("Confirm Number")
                        .setMessage("Are You Sure You Want To Proceed With $number")
                        .setPositiveButton("Okay") { alertDialog, _ ->
                            alertDialog.dismiss()


                            val options = PhoneAuthOptions.newBuilder(mAuth)
                                .setPhoneNumber("+91$number")       // Phone number to verify
                                .setTimeout(60L, TimeUnit.SECONDS) // Timeout and unit
                                .setActivity(requireActivity())                 // Activity (for callback binding)
                                .setCallbacks(object :
                                    PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

                                    override fun onCodeSent(
                                        p0: String,
                                        p1: PhoneAuthProvider.ForceResendingToken
                                    ) {
                                        super.onCodeSent(p0, p1)
                                        verificationId = p0
                                        view.showProgress.visibility = View.GONE
                                        view.regInfo.text = "Enter OTP Sent To $number"
                                        view.regNumber.text!!.clear()
                                        view.regNumber.hint = "OTP"
                                        view.enterNumber.isEnabled = true
                                        view.enterNumber.text = "Submit"
                                        view.enterNumber.tag = "1"
                                        (requireActivity() as InitActivity).initNumber = number
                                    }

                                    override fun onVerificationCompleted(p0: PhoneAuthCredential) {
                                        Toast.makeText(
                                            requireContext(),
                                            "Auto Verified",
                                            Toast.LENGTH_SHORT
                                        )
                                            .show()
                                        view.findNavController()
                                            .navigate(R.id.action_initOneFragment_to_initTwoFragment)
                                    }

                                    override fun onVerificationFailed(p0: FirebaseException) {
                                        Toast.makeText(
                                            requireContext(),
                                            "Failed",
                                            Toast.LENGTH_SHORT
                                        )
                                            .show()
                                    }

                                })          // OnVerificationStateChangedCallbacks
                                .build()
                            PhoneAuthProvider.verifyPhoneNumber(options)
                        }
                        .setNegativeButton("Cancel") { alertDialog, _ ->
                            view.showProgress.visibility = View.GONE
                            view.enterNumber.isEnabled = true
                            alertDialog.dismiss()
                        }
                        .create()
                        .show()
                } else {
                    val credential = PhoneAuthProvider.getCredential(verificationId, number)
                    mAuth.signInWithCredential(credential)
                        .addOnCompleteListener {
                            if (it.isSuccessful) {
                                showProgress.visibility = View.GONE
                                view.findNavController()
                                    .navigate(R.id.action_initOneFragment_to_initTwoFragment)
                            } else {
                                showProgress.visibility = View.GONE
                                Toast.makeText(requireContext(), "Failed", Toast.LENGTH_SHORT)
                                    .show()
                            }
                        }
                }
            } else {
                Toast.makeText(requireContext(), "Please Enter In The Field", Toast.LENGTH_SHORT)
                    .show()
            }
        }

        return view
    }
}