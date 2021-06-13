package com.example.bucketlisttravel.adapters

import android.content.Context
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.bucketlisttravel.R
import com.example.bucketlisttravel.models.PlaceModel
import kotlinx.android.synthetic.main.item_place.view.*

class PlaceAdapter(
    private val context: Context,
    private var list: ArrayList<PlaceModel>
) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {
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

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val model = list[position]
        if (holder is PlaceViewHolder) {
            holder.itemView.iv_place_image.setImageURI(Uri.parse(model.image))
            holder.itemView.tvTitle.text = model.title
            holder.itemView.tvDescription.text = model.description
        }
    }

    private class PlaceViewHolder(view: View) : RecyclerView.ViewHolder(view)
}