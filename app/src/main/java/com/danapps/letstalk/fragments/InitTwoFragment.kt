package com.danapps.letstalk.fragments

import android.Manifest
import android.app.AlertDialog
import android.content.pm.PackageManager
import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import com.bumptech.glide.Glide
import com.danapps.letstalk.InitActivity
import com.danapps.letstalk.R
import com.danapps.letstalk.adapters.MediaAdapter
import com.danapps.letstalk.models.Media
import com.danapps.letstalk.viewmodel.LetsTalkViewModel
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.fragment_init_one.view.*
import kotlinx.android.synthetic.main.fragment_init_two.view.*


class InitTwoFragment : Fragment() {
    private lateinit var db: FirebaseFirestore
    private lateinit var letsTalkViewModel: LetsTalkViewModel
    private lateinit var bottomSheetBehavior: BottomSheetBehavior<View>
    private lateinit var mediaAdapter: MediaAdapter
    private var mediaSet = false
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_init_two, container, false)
        letsTalkViewModel =
            ViewModelProvider(
                requireActivity(),
                ViewModelProvider.AndroidViewModelFactory(requireActivity().application)
            ).get(
                LetsTalkViewModel::class.java
            )
        db = FirebaseFirestore.getInstance()

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
            val initName = view.initName.text?.trim().toString()
            val initProfile = null
            if (!TextUtils.isEmpty(initName)) {
                view.initProgress.visibility = View.VISIBLE
                (requireActivity() as InitActivity).initName = initName
                (requireActivity() as InitActivity).initProfile = initProfile
                (requireActivity() as InitActivity).initUser()
            } else {
                Toast.makeText(requireContext(), "Please Enter Name", Toast.LENGTH_SHORT).show()
            }
        }

        return view
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