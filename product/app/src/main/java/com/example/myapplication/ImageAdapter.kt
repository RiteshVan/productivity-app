package com.example.myapplication

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide


class ImageAdapter(private var images: List<ImageItem>,private val context:Context):RecyclerView.Adapter<ImageAdapter.ImageViewHolder>() {

    class ImageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imageView : ImageView= itemView.findViewById(R.id.image)
        val caption:TextView = itemView.findViewById(R.id.task_caption)

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.image_diary_entry,parent,false)
        return ImageViewHolder(view)

    }

    override fun getItemCount(): Int {
        return images.size

    }

    override fun onBindViewHolder(holder: ImageViewHolder, position: Int) {
        val imageItem= images[position]

        Glide.with(context).load(imageItem.imageUrl).centerCrop().into(holder.imageView)

        holder.caption.text = imageItem.caption

    }


    fun updateData(newImages : List<ImageItem>){
        images =newImages
        notifyDataSetChanged()
    }

}