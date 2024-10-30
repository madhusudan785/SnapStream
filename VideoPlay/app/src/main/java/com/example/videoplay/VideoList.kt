package com.example.videoplay

import android.content.Intent
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.videoplay.Adapter.MyAdapter
import com.example.videoplay.ViewModel.VideoListViewModel
import com.example.videoplay.databinding.ActivityVideoListBinding


class VideoList : AppCompatActivity() {
    private lateinit var binding: ActivityVideoListBinding
    private lateinit var myAdapter: MyAdapter
    private val videoListViewModel: VideoListViewModel by viewModels()

    override fun onResume() {
        super.onResume()
        videoListViewModel.fetchAllVideos() // Refresh data each time the activity resumes
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        binding = ActivityVideoListBinding.inflate(layoutInflater)
        setContentView(binding.root)
        // Set the status bar to be transparent
        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )
        setupRecyclerView()
        binding.floatingActionButton.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }

        // Observe the LiveData from the ViewModel
        videoListViewModel.videos.observe(this) { videos ->
            if (::myAdapter.isInitialized) {
                myAdapter.updateData(videos) // Create an updateData method in MyAdapter
            } else {
                myAdapter = MyAdapter(videos)
                binding.recyclerView.adapter = myAdapter
            }
        }

    }

    private fun setupRecyclerView() {
        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        binding.recyclerView.setHasFixedSize(true)
    }

}