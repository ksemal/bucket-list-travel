package com.example.bucketlisttravel.activities

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.app.DatePickerDialog
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.location.LocationManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Looper
import android.provider.MediaStore
import android.provider.Settings
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.example.bucketlisttravel.R
import com.example.bucketlisttravel.database.DatabaseHandler
import com.example.bucketlisttravel.models.PlaceModel
import com.example.bucketlisttravel.utils.GetAddressFromLatLng
import com.google.android.gms.location.*
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.widget.Autocomplete
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import kotlinx.android.synthetic.main.activity_add_place.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.OutputStream
import java.text.SimpleDateFormat
import java.util.*

class AddPlaceActivity : AppCompatActivity(), View.OnClickListener {
    private var cal = Calendar.getInstance()
    private lateinit var dateListener: DatePickerDialog.OnDateSetListener
    private var saveImageToInternalStorage: Uri? = null
    private var mLatitude: Double = 0.0
    private var mLongitude: Double = 0.0

    private var mPlaceDetails: PlaceModel? = null

    private lateinit var mFusedLocationClient: FusedLocationProviderClient
    private val uiScope = CoroutineScope(Dispatchers.Main)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_place)
        setSupportActionBar(toolbar_add_place)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar_add_place.setNavigationOnClickListener {
            this.onBackPressed()
        }

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        if (!Places.isInitialized()) {
            Places.initialize(this, resources.getString(R.string.google_maps_key))
        }

        if (intent.hasExtra(MainActivity.EXTRA_PLACE_DETAILS)) {
            mPlaceDetails = intent.getParcelableExtra(MainActivity.EXTRA_PLACE_DETAILS)
        }

        dateListener = DatePickerDialog.OnDateSetListener { view, year, month, dayOfMonth ->
            cal.set(Calendar.YEAR, year)
            cal.set(Calendar.MONTH, month)
            cal.set(Calendar.DAY_OF_MONTH, dayOfMonth)
            updateDateInVIew()
        }
        updateDateInVIew()

        mPlaceDetails?.let {
            supportActionBar?.title = resources.getString(R.string.edit_place_title)
            et_title.setText(it.title)
            et_description.setText(it.description)
            et_date.setText(it.date)
            et_location.setText(it.location)
            mLatitude = it.latitude
            mLongitude = it.longitude

            saveImageToInternalStorage = Uri.parse(it.image)
            iv_place_image.setImageURI(saveImageToInternalStorage)
            btn_save.text = resources.getString(R.string.update_button)
        }

        et_date.setOnClickListener(this)
        tv_add_image.setOnClickListener(this)
        btn_save.setOnClickListener(this)
        et_location.setOnClickListener(this)
        btn_select_current_location.setOnClickListener(this)
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
            R.id.btn_save -> {
                when {
                    et_title.text.isNullOrEmpty() -> Toast.makeText(
                        this,
                        "Please enter a title",
                        Toast.LENGTH_SHORT
                    ).show()
                    et_description.text.isNullOrEmpty() -> Toast.makeText(
                        this,
                        "Please enter a description",
                        Toast.LENGTH_SHORT
                    ).show()
                    et_location.text.isNullOrEmpty() -> Toast.makeText(
                        this,
                        "Please enter a location",
                        Toast.LENGTH_SHORT
                    ).show()
                    saveImageToInternalStorage == null -> Toast.makeText(
                        this,
                        "Please select an image",
                        Toast.LENGTH_SHORT
                    ).show()
                    else -> {
                        val placeModel = PlaceModel(
                            if (mPlaceDetails == null) 0 else (mPlaceDetails as PlaceModel).id,
                            et_title.text.toString(),
                            saveImageToInternalStorage.toString(),
                            et_description.text.toString(),
                            et_date.text.toString(),
                            et_location.text.toString(),
                            mLatitude,
                            mLongitude
                        )
                        val dataBaseHandler =
                            DatabaseHandler(
                                this
                            )
                        val placeDetailResult: Int? =
                            if (mPlaceDetails == null) {
                                dataBaseHandler.addPlace(placeModel)
                                    ?.toInt()
                            } else {
                                dataBaseHandler.updatePlace(placeModel)
                            }
                        placeDetailResult?.let {
                            if (it > 0) {
                                Toast.makeText(
                                    this,
                                    "Place details are saved successfully",
                                    Toast.LENGTH_SHORT
                                ).show()
                                setResult(Activity.RESULT_OK)
                                finish()
                            }
                        }
                    }
                }
            }
            R.id.et_location -> {
                try {
                    val fields = listOf(
                        Place.Field.ID,
                        Place.Field.NAME,
                        Place.Field.LAT_LNG,
                        Place.Field.ADDRESS
                    )
                    val intent =
                        Autocomplete.IntentBuilder(AutocompleteActivityMode.FULLSCREEN, fields)
                            .build(this)
                    mapActivity.launch(intent)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
            R.id.btn_select_current_location -> {
                if (!isLocationEnabled()) {
                    Toast.makeText(
                        this@AddPlaceActivity,
                        "Your location provider is turned off. Please turn it on",
                        Toast.LENGTH_SHORT
                    ).show()

                    val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                    startActivity(intent)
                } else {
                    Dexter.withContext(this).withPermissions(
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    ).withListener(
                        object : MultiplePermissionsListener {
                            override fun onPermissionsChecked(p0: MultiplePermissionsReport?) {
                                if (p0 != null && p0.areAllPermissionsGranted()) {
                                    requestNewLocationData()
                                    btn_select_current_location.visibility = View.INVISIBLE
                                    current_location_loading.visibility = View.VISIBLE
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
            }
        }
    }

    private suspend fun setAddress() {
        val address = GetAddressFromLatLng(this, mLatitude, mLongitude)
        et_location.setText(address.getAddress())
        btn_select_current_location.visibility = View.VISIBLE
        current_location_loading.visibility = View.GONE
    }

    private fun isLocationEnabled(): Boolean {
        val locationManager: LocationManager =
            getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(
            LocationManager.NETWORK_PROVIDER
        )
    }

    @SuppressLint("MissingPermission")
    private fun requestNewLocationData() {
        val mLocationRequest = LocationRequest.create()
        mLocationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        mLocationRequest.interval = 1000
        mLocationRequest.numUpdates = 1

        mFusedLocationClient.requestLocationUpdates(
            mLocationRequest,
            mLocationCallback,
            Looper.myLooper()
        )
    }

    private val mLocationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {
            val mLastLocation = locationResult.lastLocation
            mLatitude = mLastLocation.latitude
            mLongitude = mLastLocation.longitude
            uiScope.launch {
                setAddress()
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
        val myFormat = "MM.dd.yyyy"
        val sdf = SimpleDateFormat(myFormat, Locale.getDefault())
        et_date.setText(sdf.format(cal.time).toString())
    }

    private fun saveImageToInternalStorage(bitmap: Bitmap?): Uri {
        val wrapper = ContextWrapper(applicationContext)
        var file = wrapper.getDir(IMAGE_DIRECTORY, Context.MODE_PRIVATE)
        file = File(file, "${UUID.randomUUID()}.jpg")
        try {
            val stream: OutputStream = FileOutputStream(file)
            bitmap?.compress(Bitmap.CompressFormat.JPEG, 100, stream)
            stream.flush()
            stream.close()
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return Uri.parse(file.absolutePath)
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
                                    saveImageToInternalStorage =
                                        saveImageToInternalStorage(selectedImageBitmap)
                                    Log.e("Saved image", "Path: $saveImageToInternalStorage")
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
                                    saveImageToInternalStorage =
                                        saveImageToInternalStorage(selectedImageBitmap)
                                    Log.e("Saved image", "Path: $saveImageToInternalStorage")
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
                    saveImageToInternalStorage = saveImageToInternalStorage(thumbnail)
                    Log.e("Saved image", "Path: $saveImageToInternalStorage")
                    iv_place_image.setImageBitmap(thumbnail)
                }
                Activity.RESULT_CANCELED -> {
                }
            }
        }

    private val mapActivity =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            when (result.resultCode) {
                Activity.RESULT_OK -> {
                    result.data?.let {
                        val place = Autocomplete.getPlaceFromIntent(it)
                        et_location.setText(place.address)
                        mLatitude = place.latLng?.latitude ?: 0.toDouble()
                        mLongitude = place.latLng?.longitude ?: 0.toDouble()
                    }
                }
                Activity.RESULT_CANCELED -> {
                }
            }
        }

    companion object {
        private const val IMAGE_DIRECTORY = "BucketListPlacesImages"
    }
}