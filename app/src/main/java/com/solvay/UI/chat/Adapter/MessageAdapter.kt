package com.solvay.UI.chat.Adapter

import android.util.Log
import android.view.View
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.solvay.Base.BaseChatRecyclerAdapter
import com.solvay.BuildConfig
import com.solvay.R
import com.solvay.UI.chat.AudioPlayerView
import com.solvay.Utils.Prefs
import com.solvay.Utils.Utils.loadImageMessage
import com.solvay.UI.chat.model.ChatHistory
import com.solvay.Utils.Utils
import com.solvay.Utils.Utils.toJsonString
import com.solvay.databinding.DateLayoutBinding
import com.solvay.databinding.DocumentLayoutBinding
import com.solvay.databinding.ImageReceiverLayoutBinding
import com.solvay.databinding.ImageSenderLayoutBinding
import com.solvay.databinding.ReceiveMessageLayoutBinding
import com.solvay.databinding.RecivceDocumentBinding
import com.solvay.databinding.SendMessageLayoutBinding
import com.solvay.enumClasses.ChatType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


class MessageAdapter(
    override var layoutId: HashMap<ChatType, Int>, onClickListener: OnClickListener
) : BaseChatRecyclerAdapter<ChatHistory>() {
    var onClickListener: OnClickListener = onClickListener
    var otherUserId = ""
    var userImage = ""
    var userName = ""
    var unReadCountValue = 0
    var textView: TextView? = null
    private var seenTxtView: TextView? = null
    private var activeAudioPlayer: AudioPlayerView? = null
    private var activeAudioHolder: ViewHolder? = null
    override fun bind(holder: ViewHolder, item: ChatHistory, position: Int) {
        var audioPlayer: AudioPlayerView? = null


        val darkLightBlue = ContextCompat.getColor(holder.itemView.context, R.color.button_color)
        val darkBlack =
            ContextCompat.getColor(holder.itemView.context, R.color.ediText_background_color)
        when (holder.itemViewType) {

            /**Send message to other user*/
            ChatType.MESSAGE_SENDER.ordinal -> {
                (holder.binding as SendMessageLayoutBinding).apply {
                    when (item.type) {
                        "text" -> {
                            if (item.unsend) {
                                clSendReply.visibility = View.GONE
                                clUserSendMessage.visibility = View.GONE
                                dotedLine.visibility = View.GONE
                                clDeleteMessage.visibility = View.VISIBLE
                                tvDeleted.text =
                                    holder.itemView.context.getString(R.string.you_msg_deleted)
                            } else {
                                if (item.replyMessage != null) {
                                    clSendReply.visibility = View.VISIBLE
                                    if (item.replyMessage.text.isNullOrEmpty()) {
                                        tvReplyMsg.text = item.replyMessage.type.capitalize()
                                    } else {
                                        tvReplyMsg.text = item.replyMessage.text
                                    }

                                    /**To check that user self message */
                                    if (!item.self) {
                                        tvYou.text = userName
                                    } else {
                                        tvYou.text = holder.itemView.context.getString(R.string.you)
                                    }
                                    tvReplySendMsg.text = item.text


                                    /**Here we handle the small image for reply message */
                                    if (item.replyMessage.type == "image") {
                                        replyClose.visibility = View.VISIBLE
                                        CoroutineScope(Dispatchers.IO).launch {
                                            replyClose.loadImageMessage(BuildConfig.IMAGE_URL + item.replyMessage.file)
                                        }
                                    } else if (item.replyMessage.type == "video") {
                                        replyClose.visibility = View.VISIBLE
                                        CoroutineScope(Dispatchers.IO).launch {
                                            replyClose.loadImageMessage(BuildConfig.IMAGE_URL + item.replyMessage.thumbnail)
                                        }
                                    } else if (item.replyMessage.type == "audio") {
                                        replyClose.visibility = View.VISIBLE
                                        replyClose.setImageResource(R.drawable.audiotrack)
                                    } else if (item.replyMessage.type == "document") {
                                        replyClose.visibility = View.VISIBLE
                                        replyClose.setImageResource(R.drawable.google_docs)
                                    } else {
                                        replyClose.visibility = View.GONE
                                    }

                                } else {
                                    clSendReply.visibility = View.GONE
                                    tvUserSendMessage.text = item.text
                                }
                                clDeleteMessage.visibility = View.GONE
                            }

                            tvSendTime.text =
                                Utils.getTimeFromDateTime(item.createdAt)
                        }
                    }


                    /** Here we are handling the Menu option Functionality.*/
                    if (item.isMenuOpen == true) {
                        clSentMessageDate.visibility = View.VISIBLE
                    } else {
                        clSentMessageDate.visibility = View.GONE
                    }
                    dotedLine.setOnClickListener {
                        onClickListener.onOpenReplyMenu(position, item, it)
                        Log.e("fhsjsdfh", item.isMenuOpen.toString())

//                        if (clSentMessageDate.visibility == View.VISIBLE) {
//                            clSentMessageDate.visibility = View.GONE
//                        } else {
//                            clSentMessageDate.visibility = View.VISIBLE
//                        }
                    }


                    llDelete.setOnClickListener {
                        onClickListener.onClickDelete(position, item)
                        clSentMessageDate.visibility = View.GONE
                    }
                    llReply.setOnClickListener {
                        onClickListener.onClickReply(position, item)
                        clSentMessageDate.visibility = View.GONE
                    }
                    llCopy.setOnClickListener {
                        onClickListener.onClickCopy(position, item)
                        clSentMessageDate.visibility = View.GONE
                    }

                    root.setOnClickListener {
                        onClickListener.hideMenuOnClickRoot(position, item, "")
                    }
                }
            }


            /**Receive message from the other user*/
            ChatType.MESSAGE_RECEIVER.ordinal -> {
                (holder.binding as ReceiveMessageLayoutBinding).apply {
                    when (item.type) {
                        "text" -> {
                            if (item.unsend) {
                                tvUserMessage.visibility = View.GONE
                                clSendReply.visibility = View.GONE
                                clUserSendMessage.visibility = View.GONE
                                dotedLine.visibility = View.GONE
                                clDeleteMessage.visibility = View.VISIBLE
                                tvDeleted.text =
                                    holder.itemView.context.getString(R.string.this_msg_deleted)
                            } else {
                                if (item.replyMessage != null) {
                                    clSendReply.visibility = View.VISIBLE
                                    if (!item.self) {
                                        tvYou.text = userName
                                    } else {
                                        tvYou.text = holder.itemView.context.getString(R.string.you)
                                    }

                                    if (item.replyMessage.text.isNullOrEmpty()) {
                                        tvReplyMsg.text = item.replyMessage.type.capitalize()
                                    } else {
                                        tvReplyMsg.text = item.replyMessage.text
                                    }
                                    tvReplySendMsg.text = item.text


                                    /**Here we handle the small image for reply message */
                                    if (item.replyMessage.type == "image") {
                                        replyClose.visibility = View.VISIBLE
                                        CoroutineScope(Dispatchers.IO).launch {
                                            replyClose.loadImageMessage(BuildConfig.IMAGE_URL + item.replyMessage.file)
                                        }
                                    } else if (item.replyMessage.type == "video") {
                                        replyClose.visibility = View.VISIBLE
                                        CoroutineScope(Dispatchers.IO).launch {
                                            replyClose.loadImageMessage(BuildConfig.IMAGE_URL + item.replyMessage.thumbnail)
                                        }
                                    } else if (item.replyMessage.type == "audio") {
                                        replyClose.visibility = View.VISIBLE
                                        replyClose.setImageResource(R.drawable.audiotrack)
                                    } else if (item.replyMessage.type == "document") {
                                        replyClose.visibility = View.VISIBLE
                                        replyClose.setImageResource(R.drawable.google_docs)
                                    } else {
                                        replyClose.visibility = View.GONE
                                    }
                                } else {
                                    clSendReply.visibility = View.GONE
                                    tvUserMessage.text = item.text
                                }
                            }

                            tvTime.text =
                                Utils.getTimeFromDateTime(item.createdAt)


                            tvUserMessage.text = item.text

                            CoroutineScope(Dispatchers.IO).launch {
                                ivUserProfileImage.loadImageMessage(BuildConfig.IMAGE_URL + userImage)
                            }
                        }
                    }


                    /** Here we are handling the Menu option Functionality.*/
                    if (item.isMenuOpen == true) {
                        clSentMessageDate.visibility = View.VISIBLE
                    } else {
                        clSentMessageDate.visibility = View.GONE
                    }
                    dotedLine.setOnClickListener {
                        onClickListener.onOpenReplyMenu(position, item, it)
                    }

                    llDelete.setOnClickListener {
                        onClickListener.onClickDelete(position, item)
                        clSentMessageDate.visibility = View.GONE
                    }
                    llReply.setOnClickListener {
                        onClickListener.onClickReply(position, item)
                        clSentMessageDate.visibility = View.GONE
                    }
                    llCopy.setOnClickListener {
                        onClickListener.onClickCopy(position, item)
                        clSentMessageDate.visibility = View.GONE
                    }

                    root.setOnClickListener {
                        onClickListener.hideMenuOnClickRoot(position, item, "")
                    }
                }
            }


            /**Send Document and Audio to other user*/
            ChatType.DOCUMENT_SENDER.ordinal -> {
                (holder.binding as DocumentLayoutBinding).apply {
                    Log.e("fshlkhdf", item.type.toString())

                    /** Here we are handling the Menu option Functionality.*/
                    if (item.isMenuOpen == true) {
                        clSentMessageDate.visibility = View.VISIBLE
                    } else {
                        clSentMessageDate.visibility = View.GONE
                    }
                    dotedLine.setOnClickListener {
                        onClickListener.onOpenReplyMenu(position, item, it)
                    }


                    when (item.type) {
                        "document" -> {
                            if (item.unsend) {
                                dotedLine.visibility = View.GONE
                                clSendReply.visibility = View.GONE
                                clAudio.visibility = View.GONE
                                constraintLayout.visibility = View.VISIBLE
                                documentIcon.setBackgroundResource(R.drawable.baseline_block_flipped_24)
                                tvUserSendMessage.text =
                                    holder.itemView.context.getString(R.string.you_msg_deleted)
                            } else {
                                if (item.replyMessage != null) {
                                    clAudio.visibility = View.GONE
                                    dotedLine.visibility = View.VISIBLE
                                    clSendReply.visibility = View.VISIBLE
                                    if (!item.self) {
                                        tvYou.text = userName
                                    } else {
                                        tvYou.text = holder.itemView.context.getString(R.string.you)
                                    }
                                    if (item.replyMessage.text.isNullOrEmpty()) {
                                        tvReplyMsg.text = item.replyMessage.type.capitalize()
                                    } else {
                                        tvReplyMsg.text = item.replyMessage.text
                                    }

                                    if (item.replyMessage.type == "image") {
                                        replyClose.visibility = View.VISIBLE
                                        CoroutineScope(Dispatchers.IO).launch {
                                            replyClose.loadImageMessage(BuildConfig.IMAGE_URL + item.replyMessage.file)
                                        }
                                    } else if (item.replyMessage.type == "video") {
                                        replyClose.visibility = View.VISIBLE
                                        CoroutineScope(Dispatchers.IO).launch {
                                            replyClose.loadImageMessage(BuildConfig.IMAGE_URL + item.replyMessage.thumbnail)
                                        }
                                    } else if (item.replyMessage.type == "audio") {
                                        replyClose.visibility = View.VISIBLE
                                        replyClose.setImageResource(R.drawable.audiotrack)
                                    } else if (item.replyMessage.type == "document") {
                                        replyClose.visibility = View.VISIBLE
                                        replyClose.setImageResource(R.drawable.google_docs)
                                    } else {
                                        replyClose.visibility = View.GONE
                                    }
                                    val pdfFilename = extractPdfFilename(item.file)
                                    tvReplySendMsg.text = pdfFilename
                                } else {
                                    clAudio.visibility = View.GONE
                                    dotedLine.visibility = View.VISIBLE
                                    constraintLayout.visibility = View.VISIBLE
                                    val pdfFilename = extractPdfFilename(item.file)
                                    tvUserSendMessage.text = pdfFilename
                                    tvSendTime.text =
                                        Utils.getTimeFromDateTime(item.createdAt)
                                }
                            }

                            /** Here we handle the  document Link for download  */
                            clUserSendMessage.setOnClickListener {
                                if (!item.unsend) onClickListener.onClickDocument(position, item)
                            }
                        }

                        "audio" -> {
                            if (item.unsend) {
                                dotedLine.visibility = View.GONE
                                clAudio.visibility = View.GONE
                                constraintLayout.visibility = View.VISIBLE
                                documentIcon.setBackgroundResource(R.drawable.baseline_block_flipped_24)
                                tvUserSendMessage.text =
                                    holder.itemView.context.getString(R.string.you_msg_deleted)
                            } else {
                                if (item.replyMessage != null) {
                                    clAudio.visibility = View.VISIBLE
                                    dotedLine.visibility = View.VISIBLE
                                    constraintLayout.visibility = View.GONE
                                    replyAudioClose.visibility = View.VISIBLE
                                    tvAudioYou.visibility = View.VISIBLE
                                    tvAudioReply.visibility = View.VISIBLE
                                    if (!item.self) {
                                        tvAudioYou.text = userName
                                    } else {
                                        tvAudioYou.text =
                                            holder.itemView.context.getString(R.string.you)
                                    }
                                    if (item.replyMessage.text.isNullOrEmpty()) {
                                        tvAudioReply.text = item.replyMessage.type.capitalize()
                                    } else {
                                        tvAudioReply.text = item.replyMessage.text
                                    }
                                    if (item.replyMessage.type == "image") {
                                        replyClose.visibility = View.VISIBLE
                                        CoroutineScope(Dispatchers.IO).launch {
                                            replyClose.loadImageMessage(BuildConfig.IMAGE_URL + item.replyMessage.file)
                                        }
                                    } else if (item.replyMessage.type == "video") {
                                        replyClose.visibility = View.VISIBLE
                                        CoroutineScope(Dispatchers.IO).launch {
                                            replyClose.loadImageMessage(BuildConfig.IMAGE_URL + item.replyMessage.thumbnail)
                                        }
                                    } else if (item.replyMessage.type == "audio") {
                                        replyClose.visibility = View.VISIBLE
                                        replyClose.setImageResource(R.drawable.audiotrack)
                                    } else if (item.replyMessage.type == "document") {
                                        replyClose.visibility = View.VISIBLE
                                        replyClose.setImageResource(R.drawable.google_docs)
                                    } else {
                                        replyClose.visibility = View.GONE
                                    }
                                    replyAudioClose.setImageResource(R.drawable.audiotrack)

                                    stopActiveAudioPlayer()

                                    // Create a new AudioPlayerView
                                    audioPlayer = AudioPlayerView.create(
                                        context = holder.itemView.context,
                                        playPauseButton = holder.binding.playPauseButton,
                                        seekBar = holder.binding.audioSeekBar,
                                        durationView = holder.binding.audioDuration,
                                        audioUrl = BuildConfig.IMAGE_URL + item.file
                                    )

                                    // Set this as the active player
                                    activeAudioPlayer = audioPlayer
                                    activeAudioHolder = holder
                                } else {
                                    clAudio.visibility = View.VISIBLE
                                    dotedLine.visibility = View.VISIBLE
                                    constraintLayout.visibility = View.GONE
                                    replyAudioClose.visibility = View.GONE
                                    tvAudioYou.visibility = View.GONE
                                    tvAudioReply.visibility = View.GONE
                                    // Stop and release the previous player if it exists
                                    stopActiveAudioPlayer()

                                    // Create a new AudioPlayerView
                                    audioPlayer = AudioPlayerView.create(
                                        context = holder.itemView.context,
                                        playPauseButton = holder.binding.playPauseButton,
                                        seekBar = holder.binding.audioSeekBar,
                                        durationView = holder.binding.audioDuration,
                                        audioUrl = BuildConfig.IMAGE_URL + item.file
                                    )

                                    // Set this as the active player
                                    activeAudioPlayer = audioPlayer
                                    activeAudioHolder = holder

                                }


                            }


                        }


                    }



                    llDelete.setOnClickListener {
                        onClickListener.onClickDelete(position, item)
                        clSentMessageDate.visibility = View.GONE
                    }
                    llReply.setOnClickListener {
                        onClickListener.onClickReply(position, item)
                        clSentMessageDate.visibility = View.GONE
                    }

                    root.setOnClickListener {
                        onClickListener.hideMenuOnClickRoot(position, item, "")
                    }
                }
            }


            /**Receive Document and Audio to other user*/
            ChatType.DOCUMENT_RECEIVER.ordinal -> {
                (holder.binding as RecivceDocumentBinding).apply {
                    when (item.type) {
                        "document" -> {
                            if (item.unsend) {
                                dotedLine.visibility = View.GONE
                                optionButton.visibility = View.GONE
                                clDeleteMessage.visibility = View.VISIBLE
                                tvDeleted.text =
                                    holder.itemView.context.getString(R.string.this_msg_deleted)
                            } else {
                                if (item.replyMessage != null) {
                                    clAudio.visibility = View.GONE
                                    dotedLine.visibility = View.VISIBLE
                                    clReceiveDoc.visibility = View.VISIBLE
                                    clSendReply.visibility = View.VISIBLE
                                    if (!item.self) {
                                        tvYou.text = userName
                                    } else {
                                        tvYou.text = holder.itemView.context.getString(R.string.you)
                                    }

                                    /** here we set the reply text */
                                    if (item.replyMessage.text.isNullOrEmpty()) {
                                        tvReplyMsg.text = item.replyMessage.type.capitalize()
                                    } else {
                                        tvReplyMsg.text = item.replyMessage.text
                                    }

                                    if (item.replyMessage.type == "image") {
                                        replyClose.visibility = View.VISIBLE
                                        CoroutineScope(Dispatchers.IO).launch {
                                            replyClose.loadImageMessage(BuildConfig.IMAGE_URL + item.replyMessage.file)
                                        }
                                    } else if (item.replyMessage.type == "video") {
                                        replyClose.visibility = View.VISIBLE
                                        CoroutineScope(Dispatchers.IO).launch {
                                            replyClose.loadImageMessage(BuildConfig.IMAGE_URL + item.replyMessage.thumbnail)
                                        }
                                    } else if (item.replyMessage.type == "audio") {
                                        replyClose.visibility = View.VISIBLE
                                        replyClose.setImageResource(R.drawable.audiotrack)
                                    } else if (item.replyMessage.type == "document") {
                                        replyClose.visibility = View.VISIBLE
                                        replyClose.setImageResource(R.drawable.google_docs)
                                    } else {
                                        replyClose.visibility = View.GONE
                                    }

                                    val pdfFilename = extractPdfFilename(item.file)
                                    tvReplySendMsg.text = pdfFilename
                                } else {
                                    clAudio.visibility = View.GONE
                                    dotedLine.visibility = View.VISIBLE
                                    optionButton.visibility = View.VISIBLE
                                    clDeleteMessage.visibility = View.GONE

                                    val pdfFilename = extractPdfFilename(item.file)
                                    tvDocMessage.text = pdfFilename
                                }

                                tvSendTime.text =
                                    Utils.getTimeFromDateTime(item.createdAt)
                            }

                            /** Here we handle the  document Link for download  */
                            clUserSendMessage.setOnClickListener {
                                if (!item.unsend) onClickListener.onClickDocument(position, item)
                            }
                        }


                        "audio" -> {
                            if (item.unsend) {
                                dotedLine.visibility = View.GONE
                                clAudio.visibility = View.GONE
                                clReceiveDoc.visibility = View.GONE
                            } else {

                                if (item.replyMessage != null) {
                                    clAudio.visibility = View.VISIBLE
                                    dotedLine.visibility = View.VISIBLE
                                    clReceiveDoc.visibility = View.GONE
                                    replyAudioClose.visibility = View.VISIBLE
                                    tvAudioYou.visibility = View.VISIBLE
                                    tvAudioReply.visibility = View.VISIBLE
                                    if (!item.self) {
                                        tvAudioYou.text = userName
                                    } else {
                                        tvAudioYou.text =
                                            holder.itemView.context.getString(R.string.you)
                                    }
                                    if (item.replyMessage.text.isNullOrEmpty()) {
                                        tvAudioReply.text = item.replyMessage.type.capitalize()
                                    } else {
                                        tvAudioReply.text = item.replyMessage.text
                                    }
                                    replyAudioClose.setImageResource(R.drawable.audiotrack)

                                    if (item.replyMessage.type == "image") {
                                        replyClose.visibility = View.VISIBLE
                                        CoroutineScope(Dispatchers.IO).launch {
                                            replyClose.loadImageMessage(BuildConfig.IMAGE_URL + item.replyMessage.file)
                                        }
                                    } else if (item.replyMessage.type == "video") {
                                        replyClose.visibility = View.VISIBLE
                                        CoroutineScope(Dispatchers.IO).launch {
                                            replyClose.loadImageMessage(BuildConfig.IMAGE_URL + item.replyMessage.thumbnail)
                                        }
                                    } else if (item.replyMessage.type == "audio") {
                                        replyClose.visibility = View.VISIBLE
                                        replyClose.setImageResource(R.drawable.audiotrack)
                                    } else if (item.replyMessage.type == "document") {
                                        replyClose.visibility = View.VISIBLE
                                        replyClose.setImageResource(R.drawable.google_docs)
                                    } else {
                                        replyClose.visibility = View.GONE
                                    }
                                    stopActiveAudioPlayer()

                                    // Create a new AudioPlayerView
                                    audioPlayer = AudioPlayerView.create(
                                        context = holder.itemView.context,
                                        playPauseButton = holder.binding.playPauseButton,
                                        seekBar = holder.binding.audioSeekBar,
                                        durationView = holder.binding.audioDuration,
                                        audioUrl = BuildConfig.IMAGE_URL + item.file
                                    )

                                    // Set this as the active player
                                    activeAudioPlayer = audioPlayer
                                    activeAudioHolder = holder
                                } else {
                                    clAudio.visibility = View.VISIBLE
                                    clReceiveDoc.visibility = View.GONE
                                    dotedLine.visibility = View.VISIBLE
                                    stopActiveAudioPlayer()

                                    audioPlayer = AudioPlayerView.create(
                                        context = holder.itemView.context,
                                        playPauseButton = holder.binding.playPauseButton,
                                        seekBar = holder.binding.audioSeekBar,
                                        durationView = holder.binding.audioDuration,
                                        audioUrl = BuildConfig.IMAGE_URL + item.file
                                    )

                                    activeAudioPlayer = audioPlayer
                                    activeAudioHolder = holder
                                }

                            }


                        }


                    }
                    /** Here we are handling the Menu option Functionality.*/
                    if (item.isMenuOpen == true) {
                        clSentMessageDate.visibility = View.VISIBLE
                    } else {
                        clSentMessageDate.visibility = View.GONE
                    }
                    dotedLine.setOnClickListener {
                        onClickListener.onOpenReplyMenu(position, item, it)
                    }



                    llDelete.setOnClickListener {
                        onClickListener.onClickDelete(position, item)
                        clSentMessageDate.visibility = View.GONE
                    }
                    llReply.setOnClickListener {
                        onClickListener.onClickReply(position, item)
                        clSentMessageDate.visibility = View.GONE
                    }
                    llCopy.setOnClickListener {
                        onClickListener.onClickCopy(position, item)
                        clSentMessageDate.visibility = View.GONE
                    }


                    /** Here we set the user profile*/
                    CoroutineScope(Dispatchers.IO).launch {
                        ivUserProfileImage.loadImageMessage(BuildConfig.IMAGE_URL + userImage)
                    }

                    tvSendTime.text = Utils.getTimeFromDateTime(item.createdAt)


                    root.setOnClickListener {
                        onClickListener.hideMenuOnClickRoot(position, item, "")
                    }

                }


            }


            /**Send Image and Video to other user*/
            ChatType.IMAGE_SENDER.ordinal -> {
                (holder.binding as ImageSenderLayoutBinding).apply {
                    when (item.type) {
                        "image" -> {
                            if (item.unsend) {
                                clSendReply.visibility = View.VISIBLE
                                clDeleteMessage.visibility = View.VISIBLE
                                clSenderImage.visibility = View.GONE
                                tvDeleted.text =
                                    holder.itemView.context.getString(R.string.you_msg_deleted)
                            } else {
                                if (item.replyMessage == null) {
                                    clSendReply.visibility = View.GONE
                                    CoroutineScope(Dispatchers.IO).launch {
                                        senderImage.loadImageMessage(BuildConfig.IMAGE_URL + item.file)
                                    }

                                } else {
                                    clSendReply.visibility = View.VISIBLE
                                    replyClose.visibility = View.VISIBLE
                                    tvReplyMsg.text = "Photo"
                                    CoroutineScope(Dispatchers.IO).launch {
                                        replyClose.loadImageMessage(BuildConfig.IMAGE_URL + item.replyMessage.file)
                                    }

                                    /** here we set the reply text */
                                    if (item.replyMessage.text.isNullOrEmpty()) {
                                        tvReplyMsg.text = item.replyMessage.type.capitalize()
                                    } else {
                                        tvReplyMsg.text = item.replyMessage.text
                                    }
                                    CoroutineScope(Dispatchers.IO).launch {
                                        senderImageReply.loadImageMessage(BuildConfig.IMAGE_URL + item.file)
                                    }
                                    if (item.replyMessage.type == "image") {
                                        replyClose.visibility = View.VISIBLE
                                        CoroutineScope(Dispatchers.IO).launch {
                                            replyClose.loadImageMessage(BuildConfig.IMAGE_URL + item.replyMessage.file)
                                        }
                                    } else if (item.replyMessage.type == "video") {
                                        replyClose.visibility = View.VISIBLE
                                        CoroutineScope(Dispatchers.IO).launch {
                                            replyClose.loadImageMessage(BuildConfig.IMAGE_URL + item.replyMessage.thumbnail)
                                        }
                                    } else if (item.replyMessage.type == "audio") {
                                        replyClose.visibility = View.VISIBLE
                                        replyClose.setImageResource(R.drawable.audiotrack)
                                    } else if (item.replyMessage.type == "document") {
                                        replyClose.visibility = View.VISIBLE
                                        replyClose.setImageResource(R.drawable.google_docs)
                                    } else {
                                        replyClose.visibility = View.GONE
                                    }
                                }
                            }


                            tvSendTime.text = Utils.getTimeFromDateTime(item.createdAt)
                            ivPlay.visibility = View.GONE
                            ivPlayReply.visibility = View.GONE
                        }

                        "video" -> {
                            if (item.unsend) {
                                clSendReply.visibility = View.VISIBLE
                                clDeleteMessage.visibility = View.VISIBLE
                                clSenderImage.visibility = View.GONE
                                tvDeleted.text =
                                    holder.itemView.context.getString(R.string.you_msg_deleted)

                            } else {
                                ivPlay.visibility = View.VISIBLE
                                ivPlayReply.visibility = View.VISIBLE
                                if (item.replyMessage == null) {
                                    clSendReply.visibility = View.GONE
                                    CoroutineScope(Dispatchers.IO).launch {
                                        senderImage.loadImageMessage(BuildConfig.IMAGE_URL + item.thumbnail)
                                    }
                                } else {
                                    clSendReply.visibility = View.VISIBLE
                                    replyClose.visibility = View.VISIBLE
                                    tvReplyMsg.text =
                                        holder.itemView.context.getString(R.string.video)
                                    CoroutineScope(Dispatchers.IO).launch {
                                        replyClose.loadImageMessage(BuildConfig.IMAGE_URL + item.replyMessage.thumbnail)
                                    }


                                    /** here we set the reply text */
                                    if (item.replyMessage.text.isNullOrEmpty()) {
                                        tvReplyMsg.text = item.replyMessage.type.capitalize()
                                    } else {
                                        tvReplyMsg.text = item.replyMessage.text
                                    }

                                    if (item.replyMessage.type == "image") {
                                        replyClose.visibility = View.VISIBLE
                                        CoroutineScope(Dispatchers.IO).launch {
                                            replyClose.loadImageMessage(BuildConfig.IMAGE_URL + item.replyMessage.file)
                                        }
                                    } else if (item.replyMessage.type == "video") {
                                        replyClose.visibility = View.VISIBLE
                                        CoroutineScope(Dispatchers.IO).launch {
                                            replyClose.loadImageMessage(BuildConfig.IMAGE_URL + item.replyMessage.thumbnail)
                                        }
                                    } else if (item.replyMessage.type == "audio") {
                                        replyClose.visibility = View.VISIBLE
                                        replyClose.setImageResource(R.drawable.audiotrack)
                                    } else if (item.replyMessage.type == "document") {
                                        replyClose.visibility = View.VISIBLE
                                        replyClose.setImageResource(R.drawable.google_docs)
                                    } else {
                                        replyClose.visibility = View.GONE
                                    }
                                    CoroutineScope(Dispatchers.IO).launch {
                                        senderImageReply.loadImageMessage(BuildConfig.IMAGE_URL + item.thumbnail)
                                    }
                                }
                            }
                            tvSendTime.text = Utils.getTimeFromDateTime(item.createdAt)

                        }

                    }

                    /** Here we are handling the Menu option Functionality.*/
                    if (item.isMenuOpen == true) {
                        clSentMessageDate.visibility = View.VISIBLE
                    } else {
                        clSentMessageDate.visibility = View.GONE
                    }
                    dotedLine.setOnClickListener {
                        onClickListener.onOpenReplyMenu(position, item, it)
                    }



                    llDelete.setOnClickListener {
                        onClickListener.onClickDelete(position, item)
                        clSentMessageDate.visibility = View.GONE
                    }
                    llReply.setOnClickListener {
                        onClickListener.onClickReply(position, item)
                        clSentMessageDate.visibility = View.GONE
                    }
                    llCopy.setOnClickListener {
                        onClickListener.onClickCopy(position, item)
                        clSentMessageDate.visibility = View.GONE
                    }


                    clUserSendMessage.setOnClickListener {
                        onClickListener.onClickItem(position, item)
                    }

                    root.setOnClickListener {
                        onClickListener.hideMenuOnClickRoot(position, item, "")
                    }
                }
            }


            /**Receive Image and Video to other user*/
            ChatType.IMAGE_RECEIVER.ordinal -> {
                (holder.binding as ImageReceiverLayoutBinding).apply {
                    when (item.type) {

                        "image" -> {
                            if (item.unsend) {
                                clSendReply.visibility = View.VISIBLE
                                clDeleteMessage.visibility = View.VISIBLE
                                senderImage.visibility = View.GONE
                                dotedLine.visibility = View.GONE
                                messageLayout.visibility = View.GONE
                                tvDeleted.text =
                                    holder.itemView.context.getString(R.string.this_msg_deleted)

                            } else {
                                if (item.replyMessage != null) {
                                    clSendReply.visibility = View.VISIBLE
                                    if (!item.self) {
                                        tvYou.text = userName
                                    } else {
                                        tvYou.text = holder.itemView.context.getString(R.string.you)
                                    }

                                    /** here we set the reply text */
                                    if (item.replyMessage.text.isNullOrEmpty()) {
                                        tvReplyMsg.text = item.replyMessage.type.capitalize()
                                    } else {
                                        tvReplyMsg.text = item.replyMessage.text
                                    }

                                    if (item.replyMessage.type == "image") {
                                        replyClose.visibility = View.VISIBLE
                                        CoroutineScope(Dispatchers.IO).launch {
                                            replyClose.loadImageMessage(BuildConfig.IMAGE_URL + item.replyMessage.file)
                                        }
                                    } else if (item.replyMessage.type == "video") {
                                        replyClose.visibility = View.VISIBLE
                                        CoroutineScope(Dispatchers.IO).launch {
                                            replyClose.loadImageMessage(BuildConfig.IMAGE_URL + item.replyMessage.thumbnail)
                                        }
                                    } else if (item.replyMessage.type == "audio") {
                                        replyClose.visibility = View.VISIBLE
                                        replyClose.setImageResource(R.drawable.audiotrack)
                                    } else if (item.replyMessage.type == "document") {
                                        replyClose.visibility = View.VISIBLE
                                        replyClose.setImageResource(R.drawable.google_docs)
                                    } else {
                                        replyClose.visibility = View.GONE
                                    }
                                    CoroutineScope(Dispatchers.IO).launch {
                                        senderImage.loadImageMessage(BuildConfig.IMAGE_URL + item.replyMessage.file)
                                    }

                                } else {
                                    CoroutineScope(Dispatchers.IO).launch {
                                        senderImage.loadImageMessage(BuildConfig.IMAGE_URL + item.file)
                                    }

                                }

                            }

                            CoroutineScope(Dispatchers.IO).launch {
                                ivUserProfileImage.loadImageMessage(BuildConfig.IMAGE_URL + userImage)
                            }
                            tvTime.text = Utils.getTimeFromDateTime(item.createdAt)
                            ivPlay.visibility = View.GONE
                            ivPlayReply.visibility = View.GONE
                        }

                        "video" -> {
                            if (item.unsend) {
                                clSendReply.visibility = View.VISIBLE
                                clDeleteMessage.visibility = View.VISIBLE
                                senderImage.visibility = View.GONE
                                dotedLine.visibility = View.GONE
                                messageLayout.visibility = View.GONE
                                tvDeleted.text =
                                    holder.itemView.context.getString(R.string.this_msg_deleted)
                            } else {

                                ivPlay.visibility = View.VISIBLE
                                ivPlayReply.visibility = View.VISIBLE
                                ivPlayReply.visibility = View.VISIBLE
                                if (item.replyMessage != null) {
                                    clSendReply.visibility = View.VISIBLE
                                    if (!item.self) {
                                        tvYou.text = userName
                                    } else {
                                        tvYou.text = holder.itemView.context.getString(R.string.you)
                                    }

                                    /** here we set the reply text */
                                    if (item.replyMessage.text.isNullOrEmpty()) {
                                        tvReplyMsg.text = item.replyMessage.type.capitalize()
                                    } else {
                                        tvReplyMsg.text = item.replyMessage.text
                                    }

                                    if (item.replyMessage.type == "image") {
                                        replyClose.visibility = View.VISIBLE
                                        CoroutineScope(Dispatchers.IO).launch {
                                            replyClose.loadImageMessage(BuildConfig.IMAGE_URL + item.replyMessage.file)
                                        }
                                    } else if (item.replyMessage.type == "video") {
                                        replyClose.visibility = View.VISIBLE
                                        CoroutineScope(Dispatchers.IO).launch {
                                            replyClose.loadImageMessage(BuildConfig.IMAGE_URL + item.replyMessage.thumbnail)
                                        }
                                    } else if (item.replyMessage.type == "audio") {
                                        replyClose.visibility = View.VISIBLE
                                        replyClose.setImageResource(R.drawable.audiotrack)
                                    } else if (item.replyMessage.type == "document") {
                                        replyClose.visibility = View.VISIBLE
                                        replyClose.setImageResource(R.drawable.google_docs)
                                    } else {
                                        replyClose.visibility = View.GONE
                                    }
                                    CoroutineScope(Dispatchers.IO).launch {
                                        senderImage.loadImageMessage(BuildConfig.IMAGE_URL + item.replyMessage.thumbnail)
                                    }

                                } else {
                                    CoroutineScope(Dispatchers.IO).launch {
                                        senderImage.loadImageMessage(BuildConfig.IMAGE_URL + item.thumbnail)
                                    }
                                }
                            }
                            CoroutineScope(Dispatchers.IO).launch {
                                ivUserProfileImage.loadImageMessage(BuildConfig.IMAGE_URL + userImage)
                            }
                            tvTime.text = Utils.getTimeFromDateTime(item.createdAt)
                        }

                        /** Here we set the user profile*/


                    }

                    /** Here we are handling the Menu option Functionality.*/
                    if (item.isMenuOpen == true) {
                        clSentMessageDate.visibility = View.VISIBLE
                    } else {
                        clSentMessageDate.visibility = View.GONE
                    }
                    dotedLine.setOnClickListener {
                        onClickListener.onOpenReplyMenu(position, item, it)

                    }

                    llDelete.setOnClickListener {
                        onClickListener.onClickDelete(position, item)
                        clSentMessageDate.visibility = View.GONE
                    }

                    llReply.setOnClickListener {
                        onClickListener.onClickReply(position, item)
                        clSentMessageDate.visibility = View.GONE
                    }

                    clUserSendMessage.setOnClickListener {
                        onClickListener.onClickItem(position, item)
                    }


                    root.setOnClickListener {
                        onClickListener.hideMenuOnClickRoot(position, item, "")
                    }
                }


            }


            ChatType.DATE.ordinal -> {
                (holder.binding as DateLayoutBinding).apply {
                    tvMessageDate.text = item.createdAt
                    root.setOnClickListener {
                        onClickListener.hideMenuOnClickRoot(position, item, "")
                    }
                }


            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        Log.e("gafjsdfd", Prefs.init().userId)
        var type = ChatType.MESSAGE_SENDER
        type = if (list[position].type == "text") {
            if (list[position].self) {
                ChatType.MESSAGE_SENDER
            } else {
                ChatType.MESSAGE_RECEIVER
            }
        } else if (list[position].type == "date") {
            ChatType.DATE
        } else if (list[position].type == "document" || list[position].type == "audio") {
            if (list[position].self) {
                ChatType.DOCUMENT_SENDER
            } else {
                ChatType.DOCUMENT_RECEIVER
            }
        } else {
            if (list[position].self) {
                ChatType.IMAGE_SENDER
            } else {
                ChatType.IMAGE_RECEIVER
            }
        }
        return type.ordinal
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun getItemId(position: Int): Long {
        return list[position]._id.toLong()
    }

    private fun getEmojiByUnicode(unicode: Int): String {
        return String(Character.toChars(unicode))
    }

    private fun getEmoji(unicode: String): String {
        val unicodeInt = unicode.codePointAt(0)
        return getEmojiByUnicode(unicodeInt)
    }

    interface OnClickListener {
        fun onLongPressOnSenderMessage(
            view: View,
            position: Int,
            data: ChatHistory,
            time: String
        )

        fun onClickReply(position: Int, data: ChatHistory)
        fun onClickCopy(position: Int, data: ChatHistory)
        fun onClickDelete(position: Int, data: ChatHistory)
        fun hideMenuOnClickRoot(position: Int, data: ChatHistory, type: String)
        fun onClickItem(position: Int, data: ChatHistory)
        fun onClickHideKeyboard(position: Int)
        fun onClickDotLine(position: Int, data: ChatHistory)
        fun onDownloadDecryptFile(position: Int, data: ChatHistory)
        fun onClickDocument(position: Int, data: ChatHistory)
        fun onOpenReplyMenu(position: Int, data: ChatHistory, view: View)
    }

    // Stop and release the active audio player
    private fun stopActiveAudioPlayer() {
        activeAudioPlayer?.let { player ->
            player.release() // Release MediaPlayer resources
            activeAudioHolder?.let { holder ->
                resetAudioUI(holder) // Reset UI elements (e.g., play/pause button, seek bar)
            }
        }
        activeAudioPlayer = null
        activeAudioHolder = null
    }

    // Reset UI elements of the audio player
    private fun resetAudioUI(holder: ViewHolder) {
        when (holder.binding) {
            is DocumentLayoutBinding -> {
                (holder.binding as DocumentLayoutBinding).apply {
                    playPauseButton.setImageResource(R.drawable.play) // Reset to play icon
                    audioSeekBar.progress = 0 // Reset seek bar
                    audioDuration.text = "0:00" // Reset duration text
                }
            }

            is RecivceDocumentBinding -> {
                (holder.binding as RecivceDocumentBinding).apply {
                    playPauseButton.setImageResource(R.drawable.play) // Reset to play icon
                    audioSeekBar.progress = 0 // Reset seek bar
                    audioDuration.text = "0:00" // Reset duration text
                }
            }
        }
    }

    override fun onViewRecycled(holder: ViewHolder) {
        super.onViewRecycled(holder)
        // If the recycled ViewHolder is the active audio holder, stop and release the player
        if (holder == activeAudioHolder) {
            stopActiveAudioPlayer()
        }
    }

    private fun extractPdfFilename(path: String): String? {
        val parts = path.split("/") // Split by forward slash
        return parts.lastOrNull()   // Get the last part (the file name)
    }


}