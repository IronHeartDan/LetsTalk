package com.danapps.letstalk.fragments

import android.Manifest
import android.app.AlertDialog
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.text.TextUtils
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.bumptech.glide.Glide
import com.danapps.letstalk.R
import com.danapps.letstalk.adapters.MediaAdapter
import com.danapps.letstalk.data.RetroFitBuilder
import com.danapps.letstalk.models.Media
import com.danapps.letstalk.models.User
import com.danapps.letstalk.viewmodel.LetsTalkViewModel
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.storage.FirebaseStorage
import kotlinx.android.synthetic.main.fragment_init_two.*
import kotlinx.android.synthetic.main.fragment_init_two.view.*
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.ByteArrayOutputStream


class InitTwoFragment : Fragment() {

    lateinit var number: String
    private var initName: String? = null

    private var currentUser: User? = null

    private lateinit var letsTalkViewModel: LetsTalkViewModel
    private lateinit var bottomSheetBehavior: BottomSheetBehavior<View>
    private lateinit var mediaAdapter: MediaAdapter
    private var mediaSet = false
    private var imageUrl: Uri? = null
    private lateinit var mAuth: FirebaseAuth


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_init_two, container, false)

        mAuth = FirebaseAuth.getInstance()
        number = mAuth.currentUser!!.phoneNumber!!.substring(3)

        letsTalkViewModel =
            ViewModelProvider(
                requireActivity(),
                ViewModelProvider.AndroidViewModelFactory(requireActivity().application)
            ).get(
                LetsTalkViewModel::class.java
            )

        letsTalkViewModel.liveUser(number).observe(requireActivity(), {
            currentUser = it
            if (currentUser != null) {
                if (currentUser!!.profile_pic != null) {
                    Glide.with(requireContext()).load(currentUser!!.profile_pic)
                        .into(view.showInitPicture)
                }
                view.initName.setText(currentUser!!.name)
                imageUrl = Uri.parse(currentUser!!.profile_pic)
            }
        })

        bottomSheetBehavior = BottomSheetBehavior.from(view.initBottomSheet)
        bottomSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN

        bottomSheetBehavior.addBottomSheetCallback(object :
            BottomSheetBehavior.BottomSheetCallback() {
            override fun onStateChanged(bottomSheet: View, newState: Int) {
                when (newState) {
                    BottomSheetBehavior.STATE_COLLAPSED -> view.closeInitBottomSheet.show()
                    BottomSheetBehavior.STATE_HIDDEN -> view.closeInitBottomSheet.hide()
                    BottomSheetBehavior.STATE_DRAGGING -> {
                    }
                    BottomSheetBehavior.STATE_EXPANDED -> {
                    }
                    BottomSheetBehavior.STATE_HALF_EXPANDED -> {
                    }
                    BottomSheetBehavior.STATE_SETTLING -> {
                    }
                }
            }

            override fun onSlide(bottomSheet: View, slideOffset: Float) {
            }

        })

        mediaAdapter = MediaAdapter(requireContext())
        view.initGalleryList.adapter = mediaAdapter
        view.initGalleryList.layoutManager = GridLayoutManager(requireContext(), 3)

        mediaAdapter.setOnItemClickListener(object : MediaAdapter.OnItemClickListener {
            override fun onItemClick(media: Media) {
                imageUrl = media.uri
                Glide.with(requireContext()).load(media.uri).into(view.showInitPicture)
                bottomSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN
            }

        })

        view.initPicture.setOnClickListener {
            if (ContextCompat.checkSelfPermission(
                    requireContext(),
                    Manifest.permission.READ_EXTERNAL_STORAGE
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                if (!mediaSet) {
                    letsTalkViewModel.mediaLive.observe(requireActivity(), {
                        mediaAdapter.submitList(it)
                    })
                    bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
                    mediaSet = true
                } else
                    bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED

            } else if (shouldShowRequestPermissionRationale(Manifest.permission.READ_EXTERNAL_STORAGE)) {
                AlertDialog.Builder(requireContext())
                    .setTitle("Storage Permission Required")
                    .setMessage("Storage Permission Is Required To Fetch Your Pictures")
                    .setPositiveButton("GRANT") { alertDialog, _ ->
                        alertDialog.dismiss()
                        requestPermissions(arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), 122)
                    }
                    .setNegativeButton("DENY") { alertDialog, _ ->
                        alertDialog.dismiss()
                    }
                    .create().show()
            } else
                requestPermissions(arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), 122)
        }

        view.closeInitBottomSheet.setOnClickListener {
            bottomSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN
        }

        view.initLetsGo.setOnClickListener {
            initName = view.initName.text?.trim().toString()
            if (!TextUtils.isEmpty(initName)) {
                initProgress.visibility = View.VISIBLE
                it.isEnabled = false
                if (currentUser == null) {
                    if (imageUrl == null) {
                        initUser(true)
                    } else {
                        uploadPicture(number, true)
                    }
                } else {

                    if (initName != currentUser!!.name && imageUrl.toString() != currentUser!!.profile_pic) {
                        view.initProgress.visibility = View.VISIBLE
                        uploadPicture(number, false)
                    } else if (imageUrl.toString() != currentUser!!.profile_pic) {
                        uploadPicture(number, false)
                    } else if (initName != currentUser!!.name) {
                        initUser(false)
                    } else {
                        activity?.onBackPressed()
                    }
                }


            } else {
                Toast.makeText(requireContext(), "Please Enter Name", Toast.LENGTH_SHORT).show()
            }
        }

        return view
    }

    private fun uploadPicture(name: String, generateToken: Boolean) {
        if (imageUrl != null) {
            GlobalScope.launch(IO) {
                val bitmap = when {
                    Build.VERSION.SDK_INT >= 28 -> {
                        val source =
                            ImageDecoder.createSource(context?.contentResolver!!, imageUrl!!)
                        ImageDecoder.decodeBitmap(source)
                    }
                    else -> {
                        MediaStore.Images.Media.getBitmap(
                            context?.contentResolver,
                            imageUrl
                        )
                    }
                }

                val baos = ByteArrayOutputStream()
                bitmap.compress(Bitmap.CompressFormat.JPEG, 50, baos)
                val data = baos.toByteArray()
                val uploadRef =
                    FirebaseStorage.getInstance().reference.child("profile_pic").child(name)
                uploadRef.putBytes(data).addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        uploadRef.downloadUrl.addOnCompleteListener {
                            if (it.isSuccessful) {
                                imageUrl = it.result
                                initUser(generateToken)
                            }
                        }
                    }
                }
                    .addOnFailureListener {
                        view?.initLetsGo?.isEnabled = true
                        Toast.makeText(context, it.message, Toast.LENGTH_SHORT).show()
                    }
            }
        }
    }


    private fun initUser(generateToken: Boolean) {
        if (generateToken) {
            //Get Token
            FirebaseMessaging.getInstance().token.addOnCompleteListener {
                if (it.isSuccessful) {
                    val user = User(initName!!, number, imageUrl.toString(), it.result)
                    Log.d("TEST", "Generated with $user: ")
                    createOrUpdate(user, false)
                }
            }
        } else {
            val user = User(initName!!, number, imageUrl.toString(), currentUser!!.pushToken)
            Log.d("TEST", "Not Generated with $user: ")
            createOrUpdate(user, true)
        }
    }

    private fun createOrUpdate(user: User, exists: Boolean) {
        RetroFitBuilder.apiService.userExists(user.number).enqueue(object : Callback<Boolean> {
            override fun onResponse(call: Call<Boolean>, response: Response<Boolean>) {
                if (response.code() == 200) {
                    if (response.body() == true) {
                        RetroFitBuilder.apiService.updateUser(user)
                            .enqueue(object : Callback<String> {
                                override fun onResponse(
                                    call: Call<String>,
                                    response: Response<String>
                                ) {
                                    if (response.code() == 200) {
                                        if (exists) {
                                            letsTalkViewModel.updateUser(user)
                                            Log.d("TEST", "Updated And Updated: ")
                                            activity?.onBackPressed()
                                        } else {
                                            letsTalkViewModel.createUser(user)

                                            try {
                                                view?.findNavController()
                                                    ?.navigate(R.id.action_initTwoFragment_to_syncContactsFragment)
                                            } catch (e: Exception) {
                                                Log.d("TEST", "onResponse: ${e.message}")
                                                view?.findNavController()
                                                    ?.navigate(R.id.action_initTwoFragment2_to_syncContactsFragment2)
                                            }

                                            Log.d("TEST", "Updated And Created: ")
                                        }
                                    }
                                }

                                override fun onFailure(call: Call<String>, t: Throwable) {
                                    view?.initLetsGo?.isEnabled = true
                                    Toast.makeText(context, t.message, Toast.LENGTH_SHORT).show()
                                    Log.d("TEST", "onResponse: ${t.message}")
                                }

                            })
                    } else {
                        RetroFitBuilder.apiService.createUser(user)
                            .enqueue(object : Callback<String> {
                                override fun onResponse(
                                    call: Call<String>,
                                    response: Response<String>
                                ) {
                                    if (response.code() == 200) {
                                        if (response.body()!!.isNotEmpty()) {
                                            letsTalkViewModel.createUser(user)
                                        }

                                        try {
                                            view?.findNavController()
                                                ?.navigate(R.id.action_initTwoFragment_to_syncContactsFragment)
                                        } catch (e: Exception) {
                                            Log.d("TEST", "onResponse: ${e.message}")
                                            view?.findNavController()
                                                ?.navigate(R.id.action_initTwoFragment2_to_syncContactsFragment2)
                                        }

                                        Log.d("TEST", "Created And Created: ")
                                    }
                                }

                                override fun onFailure(call: Call<String>, t: Throwable) {
                                    view?.initLetsGo?.isEnabled = true
                                    Toast.makeText(context, t.message, Toast.LENGTH_SHORT).show()
                                    Log.d("TEST", "onResponse: ${t.message}")
                                }

                            })
                    }
                } else {
                    Toast.makeText(activity, response.errorBody().toString(), Toast.LENGTH_SHORT)
                        .show()
                }
            }

            override fun onFailure(call: Call<Boolean>, t: Throwable) {
                view?.initLetsGo?.isEnabled = true
                Toast.makeText(context, t.message, Toast.LENGTH_SHORT).show()
                Log.d("TEST", "onFailure: ${t.message}")
            }

        })
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if (requestCode == 122 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            letsTalkViewModel.mediaLive.observe(requireActivity(), {
                mediaAdapter.submitList(it)
            })
            bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
            mediaSet = true
        }
    }
}