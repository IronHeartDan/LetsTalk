package com.danapps.letstalk.fragments

import android.Manifest
import android.app.AlertDialog
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.camera.core.CameraSelector
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import com.danapps.letstalk.R
import com.danapps.letstalk.adapters.MediaAdapter
import com.danapps.letstalk.models.Media
import com.danapps.letstalk.viewmodel.LetsTalkViewModel
import com.google.android.material.bottomsheet.BottomSheetBehavior
import kotlinx.android.synthetic.main.fragment_camera.*
import kotlinx.android.synthetic.main.fragment_camera.view.*

class CameraFragment : Fragment() {

    private lateinit var letsTalkViewModel: LetsTalkViewModel
    private lateinit var bottomSheetBehavior: BottomSheetBehavior<View>
    private lateinit var mediaAdapter: MediaAdapter
    private var mediaSet = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_camera, container, false)

        letsTalkViewModel =
            ViewModelProvider(
                requireActivity(),
                ViewModelProvider.AndroidViewModelFactory(requireActivity().application)
            ).get(
                LetsTalkViewModel::class.java
            )

        bottomSheetBehavior = BottomSheetBehavior.from(view.cameraBottomSheet)
        bottomSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN

        mediaAdapter = MediaAdapter(requireContext())
        view.cameraGalleryList.adapter = mediaAdapter
        view.cameraGalleryList.layoutManager = GridLayoutManager(requireContext(), 3)

        mediaAdapter.setOnItemClickListener(object : MediaAdapter.OnItemClickListener {
            override fun onItemClick(media: Media) {
                bottomSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN
            }

        })


        view.gallery.setOnClickListener {
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

        return view
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        when {
            ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED -> {
                startCamera()
            }
            shouldShowRequestPermissionRationale(
                Manifest.permission.CAMERA
            ) -> {
                AlertDialog.Builder(requireContext())
                    .setTitle("Camera Permission Required")
                    .setMessage("Camera Permission Is Required By LetsTalk ")
                    .setPositiveButton("GRANT") { alertDialog, _ ->
                        alertDialog.dismiss()
                        requestPermissions(arrayOf(Manifest.permission.CAMERA), 212)
                    }
                    .setNegativeButton("DENY") { alertDialog, _ ->
                        alertDialog.dismiss()
                    }
                    .create()
                    .show()
            }
            else -> requestPermissions(arrayOf(Manifest.permission.CAMERA), 212)
        }
    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(requireContext())

        cameraProviderFuture.addListener({
            // Used to bind the lifecycle of cameras to the lifecycle owner
            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()

            // Preview
            val preview = Preview.Builder()
                .build()
                .also {
                    it.setSurfaceProvider(previewView.surfaceProvider)
                }

            // Select back camera as a default
            val cameraSelector = CameraSelector.DEFAULT_FRONT_CAMERA

            try {
                // Unbind use cases before rebinding
                cameraProvider.unbindAll()

                // Bind use cases to camera
                cameraProvider.bindToLifecycle(
                    this, cameraSelector, preview
                )

            } catch (exc: Exception) {
                Log.e("TEST", "Use case binding failed", exc)
            }

        }, ContextCompat.getMainExecutor(requireContext()))
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if (requestCode == 212 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            startCamera()
        }

        if (requestCode == 122 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            letsTalkViewModel.mediaLive.observe(requireActivity(), {
                mediaAdapter.submitList(it)
            })
            bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
            mediaSet = true
        }
    }


    override fun onResume() {
        super.onResume()
        startCamera()
    }
}