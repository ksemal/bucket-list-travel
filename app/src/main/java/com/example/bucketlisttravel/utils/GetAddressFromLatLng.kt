package com.example.bucketlisttravel.utils

import android.content.Context
import android.location.Address
import android.location.Geocoder
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.invoke
import java.io.IOException
import java.util.*

class GetAddressFromLatLng(context: Context, private val lat: Double, private val lng: Double) {

    private val geoCoder: Geocoder = Geocoder(context, Locale.getDefault())

    suspend fun getAddress() = Dispatchers.Default {
        try {
            val addressList: List<Address>? = geoCoder.getFromLocation(lat, lng, 1)

            if (addressList != null && addressList.isNotEmpty()) {
                val address: Address = addressList[0]
                val sb = StringBuilder()
                for (i in 0..address.maxAddressLineIndex) {
                    sb.append(address.getAddressLine(i)).append(",")
                }
                sb.deleteCharAt(sb.length - 1)
                return@Default sb.toString()
            }
        } catch (e: IOException) {
            Log.e("Bucket List Travel", "Unable connect to GeoCoder")
        }

        return@Default ""
    }
}