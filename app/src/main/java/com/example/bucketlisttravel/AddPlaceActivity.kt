package com.example.bucketlisttravel

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.app.DatePickerDialog
import android.content.ActivityNotFoundException
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.provider.Settings
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import kotlinx.android.synthetic.main.activity_add_place.*
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

class AddPlaceActivity : AppCompatActivity(), View.OnClickListener {
    private var cal = Calendar.getInstance()
    private lateinit var dateListener: DatePickerDialog.OnDateSetListener
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_place)
        setSupportActionBar(toolbar_add_place)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar_add_place.setNavigationOnClickListener {
            this.onBackPressed()
        }
        dateListener = DatePickerDialog.OnDateSetListener { view, year, month, dayOfMonth ->
            cal.set(Calendar.YEAR, year)
            cal.set(Calendar.MONTH, month)
            cal.set(Calendar.DAY_OF_MONTH, dayOfMonth)
            updateDateInVIew()
        }
        et_date.setOnClickListener(this)
        tv_add_image.setOnClickListener(this)
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.et_date -> {
                DatePickerDialog(
                    this@AddPlaceActivity,
                    dateListener,
                    cal.get(Calendar.YEAR),
                    cal.get(Calendar.MONTH),
                    cal.get(Calendar.DAY_OF_MONTH)
                ).show()
            }
            R.id.tv_add_image -> {
                val dialog = AlertDialog.Builder(this)
                val items = arrayOf(
                    getString(R.string.add_image_dialog_option_1),
                    getString(R.string.add_image_dialog_option_2)
                )
                dialog.run {
                    setTitle(R.string.add_image_dialog_title)
                    setItems(items) { _, which ->
                        when (which) {
                            0 -> choosePhotoFromGallery()
                            1 -> takePhotoFromCamera()
                        }
                    }
                    show()
                }
            }
        }
    }

    private fun takePhotoFromCamera() {
        Dexter.withContext(this).withPermissions(
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.CAMERA
        ).withListener(
            object : MultiplePermissionsListener {
                override fun onPermissionsChecked(p0: MultiplePermissionsReport?) {
                    if (p0 != null && p0.areAllPermissionsGranted()) {
                        val cameraIntent =
                            Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                        cameraActivity.launch(cameraIntent)
                    }
                }

                override fun onPermissionRationaleShouldBeShown(
                    p0: MutableList<PermissionRequest>?,
                    p1: PermissionToken?
                ) {
                    showRationalDialogForPermission()
                }
            }
        ).onSameThread().check()
    }

    private fun choosePhotoFromGallery() {
        Dexter.withContext(this).withPermissions(
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        ).withListener(
            object : MultiplePermissionsListener {
                override fun onPermissionsChecked(p0: MultiplePermissionsReport?) {
                    if (p0 != null && p0.areAllPermissionsGranted()) {
                        val galleryIntent =
                            Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                        galleryActivity.launch(galleryIntent)
                    }
                }

                override fun onPermissionRationaleShouldBeShown(
                    p0: MutableList<PermissionRequest>?,
                    p1: PermissionToken?
                ) {
                    showRationalDialogForPermission()
                }
            }
        ).onSameThread().check()
    }

    private fun showRationalDialogForPermission() {
        AlertDialog.Builder(this)
            .setMessage(R.string.add_image_dialog_no_permission)
            .setPositiveButton(R.string.add_image_dialog_positive_btn) { _, _ ->
                try {
                    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                    val uri = Uri.fromParts("package", packageName, null)
                    intent.data = uri
                    startActivity(intent)
                } catch (e: ActivityNotFoundException) {
                    e.printStackTrace()
                }
            }
            .setNegativeButton(R.string.add_image_dialog_negative_btn) { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    private fun updateDateInVIew() {
        val myFormat = "dd.MM.yyyy"
        val sdf = SimpleDateFormat(myFormat, Locale.getDefault())
        et_date.setText(sdf.format(cal.time).toString())
    }

    private val galleryActivity =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            when (result.resultCode) {
                Activity.RESULT_OK -> {
                    if (result.data != null) {
                        val contentURI = result.data?.data
                        try {
                            contentURI?.let {
                                if (Build.VERSION.SDK_INT < 28) {
                                    val selectedImageBitmap =
                                        MediaStore.Images.Media.getBitmap(
                                            contentResolver,
                                            it
                                        )
                                    iv_place_image.setImageBitmap(
                                        selectedImageBitmap
                                    )
                                } else {
                                    val source = ImageDecoder.createSource(
                                        contentResolver,
                                        it
                                    )
                                    val selectedImageBitmap =
                                        ImageDecoder.decodeBitmap(source)
                                    iv_place_image.setImageBitmap(
                                        selectedImageBitmap
                                    )
                                }
                            }
                        } catch (e: IOException) {
                            e.printStackTrace()
                            Toast.makeText(
                                this@AddPlaceActivity,
                                "Failed to load the Image from Gallery",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                }
                Activity.RESULT_CANCELED -> {
                }
            }
        }

    private val cameraActivity =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            when (result.resultCode) {
                Activity.RESULT_OK -> {
                    val thumbnail = result.data?.extras?.get("data") as? Bitmap
                    iv_place_image.setImageBitmap(thumbnail)
                }
                Activity.RESULT_CANCELED -> {
                }
            }
        }
}