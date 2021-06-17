package com.example.bucketlisttravel.activities

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.bucketlisttravel.R
import com.example.bucketlisttravel.adapters.PlaceAdapter
import com.example.bucketlisttravel.database.DatabaseHandler
import com.example.bucketlisttravel.models.PlaceModel
import com.example.bucketlisttravel.utils.SwipeToEditCallback
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity(), PlaceAdapter.OnClickListener {
    override fun onItemClick(position: Int, model: PlaceModel) {
        val intent = Intent(this, PlaceDetailActivity::class.java)
        intent.putExtra(EXTRA_PLACE_DETAILS, model)
        startActivity(intent)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        fabAddPlace.setOnClickListener {
            val intent = Intent(this, AddPlaceActivity::class.java)
            addPlaceActivity.launch(intent)
        }
        getPlacesListFromLocalDB()
    }

    private fun setPlacesRecyclerView(list: ArrayList<PlaceModel>) {
        rv_places_list.run {
            layoutManager = LinearLayoutManager(this@MainActivity)
            adapter = PlaceAdapter(context, list).also { it.setOnClickListener(this@MainActivity) }
            setHasFixedSize(true)
        }

        val editSwipeHandler = object : SwipeToEditCallback(this) {
            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                (rv_places_list.adapter as PlaceAdapter).notifyEditItem(
                    editPlaceActivity,
                    viewHolder.adapterPosition
                )
            }
        }

        val editItemTouchHelper = ItemTouchHelper(editSwipeHandler)
        editItemTouchHelper.attachToRecyclerView(rv_places_list)
    }

    private fun getPlacesListFromLocalDB() {
        val dbHandler =
            DatabaseHandler(this)
        val getPlaceList: ArrayList<PlaceModel> = dbHandler.getPlacesList()
        if (getPlaceList.size > 0) {
            rv_places_list.visibility = View.VISIBLE
            tv_no_records_available.visibility = View.GONE
            setPlacesRecyclerView(getPlaceList)
        } else {
            rv_places_list.visibility = View.GONE
            tv_no_records_available.visibility = View.VISIBLE
        }
    }

    private val addPlaceActivity =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            when (result.resultCode) {
                Activity.RESULT_OK -> {
                    getPlacesListFromLocalDB()
                }
                Activity.RESULT_CANCELED -> {
                    Log.e("Activity", "Canceled or Back pressed")
                }
            }
        }

    private val editPlaceActivity: ActivityResultLauncher<Intent> =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            when (result.resultCode) {
                Activity.RESULT_OK -> {
                    getPlacesListFromLocalDB()
                }
                Activity.RESULT_CANCELED -> {
                    Log.e("Activity", "Canceled or Back pressed")
                }
            }
        }

    companion object {
        const val EXTRA_PLACE_DETAILS = "extra_place_details"
    }

}