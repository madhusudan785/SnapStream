package com.example.videoplay.Adapter

import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.videoplay.R
import com.example.videoplay.VideoPlayer.VideoPlayer
import com.example.videoplay.data.VideoData
import com.example.videoplay.databinding.ItemListBinding
import com.example.videoplay.utils.AppConstants
import com.squareup.picasso.Callback
import com.squareup.picasso.Picasso

class MyAdapter(private var videoList: List<VideoData>) :
    RecyclerView.Adapter<MyAdapter.VideoViewHolder>() {

    inner class VideoViewHolder(val binding: ItemListBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VideoViewHolder {
        val binding = ItemListBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return VideoViewHolder(binding)
    }

    override fun onBindViewHolder(holder: VideoViewHolder, position: Int) {
        val video = videoList[position]
        with(holder.binding) {
            videoTitle.text = video.title
            videoDescription.text = video.description

            val thumbnailUrl = "${AppConstants.BASE_URL}api/v1/thumbnails/${video.thumbnail}"
            Log.d("ThumbnailURL", "Loading thumbnail from: $thumbnailUrl")

            // Load thumbnail using Glide or Picasso
            Picasso.get()
                .load(thumbnailUrl)
                .fit()
                .centerCrop()
                .into(videoThumbnail, object : Callback {
                    override fun onSuccess() {
                        Log.d(
                            "Thumbnail",
                            "Successfully loaded thumbnail for video ${video.videoId}"
                        )
                    }

                    override fun onError(e: Exception) {
                        Log.e(
                            "Thumbnail",
                            "Error loading thumbnail for video ${video.videoId}: ${e.message}"
                        )
                        // You might want to set a placeholder image here
                        videoThumbnail.setImageResource(R.drawable.ic_launcher_foreground)
                    }
                })
            root.setOnClickListener {
                val context = holder.itemView.context
                val intent = Intent(context, VideoPlayer::class.java)
                intent.putExtra("VIDEO_ID", video.videoId)
                context.startActivity(intent)
            }
        }

    }
    override fun getItemCount(): Int = videoList.size

    fun updateData(newVideoList: List<VideoData>) {
        videoList = newVideoList
        notifyDataSetChanged()
    }

}