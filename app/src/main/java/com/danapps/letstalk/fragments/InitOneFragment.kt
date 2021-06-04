package com.danapps.letstalk.fragments

import android.app.AlertDialog
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
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

        view.ccp.registerCarrierNumberEditText(view.regNumber)

        mAuth = FirebaseAuth.getInstance()
        view.enterNumber.setOnClickListener {
            val number = view.ccp.fullNumberWithPlus
            if (!TextUtils.isEmpty(number.substring(3))) {
                view.showProgress.visibility = View.VISIBLE
                view.enterNumber.isEnabled = false
                if (view.enterNumber.tag == "0") {
                    AlertDialog.Builder(requireContext())
                        .setTitle("Confirm Number")
                        .setMessage("Are You Sure You Want To Proceed With $number")
                        .setPositiveButton("Okay") { alertDialog, _ ->
                            alertDialog.dismiss()


                            val options = PhoneAuthOptions.newBuilder(mAuth)
                                .setPhoneNumber(number)       // Phone number to verify
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
                                        view.ccp.visibility = View.GONE
                                        view.regInfo.text = "OTP Sent To $number"
                                        view.outlinedTextField.hint = "Enter OTP"
                                        view.regNumber.text!!.clear()
                                        view.regNumber.hint = "OTP"
                                        view.enterNumber.isEnabled = true
                                        view.enterNumber.text = "Verify"
                                        view.enterNumber.tag = "1"
                                    }

                                    override fun onVerificationCompleted(p0: PhoneAuthCredential) {
                                        Toast.makeText(
                                            requireContext(),
                                            "Auto Verified",
                                            Toast.LENGTH_SHORT
                                        )
                                            .show()
                                        signIn(p0)
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
                        .setCancelable(false)
                        .create()
                        .show()
                } else {
                    val code = view.regNumber.text.toString().trim()
                    Log.d("LetsTalkApplication", "onCreateView: ${code.replace(" ","")}")
                    val credential = PhoneAuthProvider.getCredential(verificationId, code.replace(" ",""))
                    signIn(credential)
                }
            } else {
                Toast.makeText(requireContext(), "Please Enter In The Field", Toast.LENGTH_SHORT)
                    .show()
            }
        }

        return view
    }

    fun signIn(credential: PhoneAuthCredential) {
        mAuth.signInWithCredential(credential)
            .addOnCompleteListener {
                if (it.isSuccessful) {
                    showProgress.visibility = View.GONE
                    view?.findNavController()
                        ?.navigate(R.id.action_initOneFragment_to_initTwoFragment)
                } else {
                    showProgress.visibility = View.GONE
                    Toast.makeText(requireContext(), "Failed: ${it.exception?.message}", Toast.LENGTH_LONG)
                        .show()
                }
            }
    }
}