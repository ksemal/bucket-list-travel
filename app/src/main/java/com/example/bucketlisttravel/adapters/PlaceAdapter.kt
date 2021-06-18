package com.example.bucketlisttravel.adapters

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.ActivityResultLauncher
import androidx.recyclerview.widget.RecyclerView
import com.example.bucketlisttravel.R
import com.example.bucketlisttravel.activities.AddPlaceActivity
import com.example.bucketlisttravel.activities.MainActivity
import com.example.bucketlisttravel.database.DatabaseHandler
import com.example.bucketlisttravel.models.PlaceModel
import kotlinx.android.synthetic.main.item_place.view.*

class PlaceAdapter(
    private val context: Context,
    private var list: ArrayList<PlaceModel>
) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var clickListener: OnClickListener? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return PlaceViewHolder(
            LayoutInflater.from(context).inflate(
                R.layout.item_place,
                parent,
                false
            )
        )
    }

    override fun getItemCount(): Int {
        return list.size
    }

    fun setOnClickListener(clickListener: OnClickListener) {
        this.clickListener = clickListener
    }

    fun removeAt(position: Int) {
        val dbHandler = DatabaseHandler(context)
        val deleted = dbHandler.deletePlace(list[position])
        deleted?.let {
            if (it > 0) {
                list.removeAt(position)
                notifyItemRemoved(position)
            }
        }
    }

    fun notifyEditItem(activityResultLauncher: ActivityResultLauncher<Intent>, position: Int) {
        val intent = Intent(context, AddPlaceActivity::class.java)
        intent.putExtra(MainActivity.EXTRA_PLACE_DETAILS, list[position])
        activityResultLauncher.launch(intent)
        notifyItemChanged(position)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val model = list[position]
        if (holder is PlaceViewHolder) {
            holder.itemView.iv_place_image.setImageURI(Uri.parse(model.image))
            holder.itemView.tvTitle.text = model.title
            holder.itemView.tvDescription.text = model.description
            holder.itemView.setOnClickListener {
                clickListener?.onItemClick(position, model)
            }
        }
    }

    private class PlaceViewHolder(view: View) : RecyclerView.ViewHolder(view)

    interface OnClickListener {
        fun onItemClick(position: Int, model: PlaceModel)
    }
}