package com.solvay.UI.chat

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.animation.core.animateFloatAsState
import androidx.navigation.fragment.findNavController
import com.solvay.R
import android.widget.MediaController
import android.widget.VideoView
import androidx.core.net.toUri
import com.solvay.BuildConfig
import com.solvay.Utils.Utils.loadImageAsync
import com.solvay.databinding.FragmentChatUserListBinding
import com.solvay.databinding.FragmentShowimgAndVideoBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ShowingAndVideoFragment : Fragment() {

    private var mBinding: FragmentShowimgAndVideoBinding? = null
    private lateinit var videoView: VideoView
    private lateinit var mediaController: MediaController
    var type =""

    var imageUri =""
    private val binding get() = mBinding!!
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment

        mBinding = FragmentShowimgAndVideoBinding.inflate(inflater, container, false)

        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mBinding?.apply {
        if (arguments?.getString("type")?.isNotEmpty() == true){
            type = arguments?.getString("type").toString()
            Log.e("imageUri", type)
            if (type=="image"){
                imageView.visibility = View.VISIBLE
                videoView.visibility = View.GONE
                imageUri  = arguments?.getString("image").toString()
                Log.e("imageUri", imageUri)
                CoroutineScope(Dispatchers.IO).launch {
                   imageView.loadImageAsync(
                        BuildConfig.IMAGE_URL + imageUri
                    )
                }

            }else{
                imageView.visibility = View.GONE
                videoView.visibility = View.VISIBLE
              val videoUri  = arguments?.getString("video").toString()

                videoView.setOnPreparedListener {
                    videoView.start()
                }

            }

        }


        }

        // Set up the close button
        binding.closeButton.setOnClickListener {
            // Handle the close action, e.g., navigate back
            findNavController().popBackStack()
        }


    }

    override fun onDestroyView() {
        super.onDestroyView()
        mBinding = null
    }
}