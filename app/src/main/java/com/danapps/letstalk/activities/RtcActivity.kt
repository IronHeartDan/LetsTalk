package com.danapps.letstalk.activities

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.View
import android.webkit.JavascriptInterface
import android.webkit.PermissionRequest
import android.webkit.WebChromeClient
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.danapps.letstalk.LetsTalkApplication
import com.danapps.letstalk.R
import com.danapps.letstalk.models.RtcCall
import com.google.gson.Gson
import io.socket.client.Socket
import kotlinx.android.synthetic.main.activity_rtc.*

class RtcActivity : AppCompatActivity(), View.OnClickListener {

    private lateinit var rtcCall: RtcCall
    private lateinit var mSocket: Socket

    private inner class RtcJavaScriptInterface {

        @JavascriptInterface
        fun localOfferSet(offer: String) {
            if (intent.getIntExtra("type", 0) == 0) {
                rtcCall.offer = offer

                //Calling
                //Send Call Req
                val rtcCall = Gson().toJson(rtcCall)
                mSocket.emit("rtcCall", rtcCall)
                Log.d("LetsTalkApplication", "localOfferSet: $rtcCall")

                //Listening


                //Listen offline
                mSocket.on("rtcOffline") {
                    runOnUiThread {
                        Toast.makeText(this@RtcActivity, "Person is Offline", Toast.LENGTH_SHORT)
                            .show()
                        finish()
                    }
                }

                //Listen Rejected
                mSocket.on("callRejected") {
                    runOnUiThread {
                        Toast.makeText(this@RtcActivity, "Called Declined", Toast.LENGTH_SHORT)
                            .show()
                        finish()
                    }
                }


                //Listen Answer
                mSocket.on("rtcAnswer") {
                    Log.d("LetsTalkApplication", "RtcAnswer: ")
                    runOnUiThread {
                        showCallingAction(false)
                        showInCallActions()

                        val ans = Gson().fromJson(it[0].toString(), RtcCall::class.java)
                        rtcWebView.loadUrl("javascript:setAnswer(${ans.offer})")
                    }
                }


            }
        }

        @JavascriptInterface
        fun answerSet(ans: String) {
            Log.d("LetsTalkApplication", "answerSet: $ans")

            showInCallActions()
            showReceivingActions(false)

            val rtcAns =
                Gson().toJson(RtcCall(ans, rtcCall.to, rtcCall.from))
            mSocket.emit("rtcAnswer",rtcAns)
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_rtc)
        mSocket = (application as LetsTalkApplication).mSocket
        rtcCall = if (intent.getStringExtra("offer") != null) {
            RtcCall(
                intent.getStringExtra("offer"),
                intent.getStringExtra("from")!!,
                intent.getStringExtra("to")!!,
                intent.getIntExtra("callType", 0),
                1
            )
        } else {
            RtcCall(
                null,
                intent.getStringExtra("from")!!,
                intent.getStringExtra("to")!!,
                intent.getIntExtra("callType", 0),
                0
            )
        }
        Log.d("LetsTalkApplication", "onCreate: ${rtcCall.offer}")
        checkPermissions()
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun initiate() {
        rtcWebView.settings.javaScriptEnabled = true
        rtcWebView.settings.mediaPlaybackRequiresUserGesture = false



        rtcWebView.webChromeClient = object : WebChromeClient() {
            override fun onPermissionRequest(request: PermissionRequest) {
                Log.d("LetsTalkApplication", "onPermissionRequest")
                Log.d("LetsTalkApplication", "GRANTED")
                request.grant(request.resources)
            }
        }


        rtcWebView.loadUrl("file:///android_asset/index.html")
        rtcWebView.addJavascriptInterface(RtcJavaScriptInterface(), "Android")

        when (intent.getIntExtra("type", 0)) {
            0 -> showCallingAction(true)
            1 -> showReceivingActions(true)
            else -> {
                showCallingAction(false)
                showReceivingActions(false)
                showInCallActions()
            }
        }

    }


    private fun checkPermissions() {
        when {
            ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) +
                    ActivityCompat.checkSelfPermission(
                        this,
                        Manifest.permission.RECORD_AUDIO
                    ) == PackageManager.PERMISSION_GRANTED -> {
                Log.d("LetsTalkApplication", "checkPermissions: ")
                initiate()
            }

            ActivityCompat.shouldShowRequestPermissionRationale(
                this,
                Manifest.permission.CAMERA
            ) or ActivityCompat.shouldShowRequestPermissionRationale(
                this,
                Manifest.permission.RECORD_AUDIO
            ) -> {
                AlertDialog.Builder(this).setTitle("Permissions Needed")
                    .setMessage("Audio And Video Permissions Are Required To Use This App")
                    .setPositiveButton("GRANT") { d, _ ->
                        d.dismiss()
                        requestPermissions(
                            arrayOf(
                                Manifest.permission.CAMERA,
                                Manifest.permission.RECORD_AUDIO
                            ), 100
                        )
                    }
                    .setNegativeButton("Close The App") { d, _ ->
                        d.dismiss()
                        finish()
                    }
                    .create()
                    .show()
            }
            else -> {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(
                        Manifest.permission.CAMERA,
                        Manifest.permission.RECORD_AUDIO
                    ), 100
                )
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 100 && grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
            initiate()
        }

    }

    private fun showReceivingActions(boolean: Boolean) {
        if (boolean) {
            answer_call.visibility = View.VISIBLE
            reject_call.visibility = View.VISIBLE
        } else {
            answer_call.visibility = View.GONE
            reject_call.visibility = View.GONE
        }
    }

    private fun showInCallActions() {
        action_mic.visibility = View.VISIBLE
        action_video.visibility = View.VISIBLE
        action_end.visibility = View.VISIBLE
    }

    private fun showCallingAction(boolean: Boolean) {
        if (boolean) {
            action_cancle.visibility = View.VISIBLE
        } else {
            action_cancle.visibility = View.GONE
        }
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.action_cancle -> {
                mSocket.emit("callCanceled", rtcCall)
                finish()
            }
            R.id.answer_call -> {
                rtcWebView.loadUrl("javascript:setRemoteOffer(${rtcCall.offer})")
            }
            R.id.reject_call -> {
                mSocket.emit("callRejected", rtcCall.from)
                finish()
            }
            R.id.action_mic -> {
            }
            R.id.action_video -> {
            }
            R.id.action_end -> {
            }
        }
    }

}