package com.example.videoplay

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.example.videoplay.Api.MyAPI
import com.example.videoplay.Api.UploadRequestBody
import com.example.videoplay.Api.UploadResponse
import com.example.videoplay.databinding.ActivityMainBinding
import com.example.videoplay.utils.getFileName
import com.example.videoplay.utils.snackbar
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream

class MainActivity : AppCompatActivity(), UploadRequestBody.UploadCallback {

    private lateinit var binding: ActivityMainBinding
    private var selectedFileUri: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.chooseFile.setOnClickListener {
            openMediaChooser()
        }
        binding.btnUpload.setOnClickListener {
            uploadMedia()
        }
        binding.listVideo.setOnClickListener {
            val intent = Intent(this, VideoList::class.java)
            startActivity(intent)
        }
//        binding.image.setImageResource()
    }

    private fun openMediaChooser() {
        Intent(Intent.ACTION_PICK).also {
            it.type = "video/*"
            val mimeTypes = arrayOf("video/mp4", "video/x-matroska", "video/webm", "video/avi", "video/3gpp")
            it.putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes)
            startActivityForResult(it, REQUEST_CODE_PICK_VIDEO)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE_PICK_VIDEO && resultCode == Activity.RESULT_OK) {
            selectedFileUri = data?.data
            selectedFileUri?.let { uri ->
                // Display video file name
                val fileName = getFileName(uri)
                binding.textView.text = fileName
            }
        }
    }

    private fun uploadMedia() {
        if (selectedFileUri == null) {
            binding.layoutRoot.snackbar("Select a Video First")
            return
        }

        val parcelFileDescriptor =
            contentResolver.openFileDescriptor(selectedFileUri!!, "r", null) ?: return
        val inputStream = FileInputStream(parcelFileDescriptor.fileDescriptor)
        val file = File(cacheDir, contentResolver.getFileName(selectedFileUri!!))
        val outputStream = FileOutputStream(file)
        inputStream.copyTo(outputStream)

        // Reset progress
        binding.progressFill.layoutParams.width = 0
        binding.progressPercentage.text = "0%"

        // Creating RequestBody for the file
        val requestBody = UploadRequestBody(file, "video", this)
        val videoPart = MultipartBody.Part.createFormData("file", file.name, requestBody)

        // Creating RequestBody for title , description and thumbnail
        val title = binding.etCustomBox.text.toString().toRequestBody("text/plain".toMediaTypeOrNull())
        val description = binding.etDescriptionBox.text.toString().toRequestBody("text/plain".toMediaTypeOrNull())



        // Upload the video
        MyAPI().uploadVideo(videoPart, title, description)
            .enqueue(object : Callback<UploadResponse> {
            override fun onResponse(call: Call<UploadResponse>, response: Response<UploadResponse>) {
                if (response.isSuccessful) {

                    binding.layoutRoot.snackbar("Video uploaded successfully")
                    Log.d("Upload", "Success: ${response.body()}")
                    binding.etCustomBox.text.clear()
                    binding.etDescriptionBox.text.clear()
                    // Clear the TextView showing the filename
                    binding.textView.text = ""

                    binding.progressFill.layoutParams.width = 0
                    binding.progressPercentage.text = "0%"
                } else {
                    binding.layoutRoot.snackbar("Failed to upload video")
                    Log.e("Upload", "Server error: ${response.errorBody()?.string()}")
                }
            }

            override fun onFailure(call: Call<UploadResponse>, t: Throwable) {
                binding.layoutRoot.snackbar("Failed to upload video: ${t.message}")
                Log.e("UploadVideoError", "Failed to upload video:${t.message}", t)
            }
        })
    }

    private fun getThumbnailByteArray(bitmap: Bitmap): ByteArray {
        val byteArrayOutputStream = ByteArrayOutputStream()
        bitmap.compress(
            Bitmap.CompressFormat.JPEG,
            100,
            byteArrayOutputStream
        )
        return byteArrayOutputStream.toByteArray()
    }

    private fun getThumbnailBitmap(uri: Uri): Bitmap? {
        val retriever = MediaMetadataRetriever()
        retriever.setDataSource(contentResolver.openFileDescriptor(uri, "r")?.fileDescriptor)
        val bitmap = retriever.getFrameAtTime(1_000_000) // Get a frame at 1 second
        retriever.release()

        // Resize the bitmap
        val maxWidth = 200 // Max width in pixels
        val maxHeight = 200 // Max height in pixels
        val width = bitmap?.width ?: 0
        val height = bitmap?.height ?: 0
        val ratio = Math.min(maxWidth.toFloat() / width, maxHeight.toFloat() / height)
        val newWidth = (width * ratio).toInt()
        val newHeight = (height * ratio).toInt()
        return bitmap?.let { Bitmap.createScaledBitmap(it, newWidth, newHeight, true) }
    }



    private fun getFileName(uri: Uri): String {
        var fileName = ""
        val cursor = contentResolver.query(uri, null, null, null, null)
        cursor?.use {
            if (it.moveToFirst()) {
                val nameIndex = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if (nameIndex != -1) {
                    fileName = it.getString(nameIndex)
                }
            }
        }
        return fileName
    }

    companion object {
        const val REQUEST_CODE_PICK_VIDEO = 101
    }

    override fun onProgressUpdate(percentage: Int) {
        // Update the width of the progress fill box
        val params = binding.progressFill.layoutParams
        params.width = (binding.progressContainer.width * (percentage / 100f)).toInt()
        binding.progressFill.layoutParams = params

        // Update the percentage text
        binding.progressPercentage.text = "$percentage%"
    }
}
