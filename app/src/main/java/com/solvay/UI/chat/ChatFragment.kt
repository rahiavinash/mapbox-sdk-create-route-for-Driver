package com.solvay.UI.chat

import android.Manifest
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.ActivityNotFoundException
import android.content.ClipData
import android.content.ClipboardManager
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.media.MediaPlayer
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.provider.MediaStore
import android.provider.OpenableColumns
import android.provider.Settings
import android.text.Html
import android.text.InputType
import android.util.DisplayMetrics
import android.util.Log
import android.view.ContextThemeWrapper
import android.view.Gravity
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.AbsListView
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.MediaController
import android.widget.ProgressBar
import android.widget.Toast
import android.widget.VideoView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.databinding.DataBindingUtil.setContentView
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DecodeFormat
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.google.common.eventbus.Subscribe
import com.google.common.reflect.TypeToken
import com.google.gson.Gson
import com.solvay.Base.BaseChatRecyclerAdapter
import com.solvay.Base.BaseFragment
import com.solvay.BuildConfig
import com.solvay.Localization.LocaleHelper
import com.solvay.MainApplication
import com.solvay.Network.ConnectivityLiveData
import com.solvay.Network.NetworkMonitor
import com.solvay.Network.SocketIOImpl
import com.solvay.R
import com.solvay.Utils.EventObserver
import com.solvay.Utils.Utils
import com.solvay.Utils.Utils.convertDateAndTime
import com.solvay.Utils.Utils.disableMultiTap
import com.solvay.Utils.isSocketConnected
import com.solvay.UI.chat.Adapter.MessageAdapter
import com.solvay.UI.chat.ChatViewModel.ChatViewModels
import com.solvay.UI.chat.model.ChatHistory
import com.solvay.UI.chat.model.ChatHistoryRequest
import com.solvay.Utils.AlertManager
import com.solvay.Utils.Prefs
import com.solvay.Utils.Utils.TAG
import com.solvay.Utils.Utils.loadImageAsync
import com.solvay.Utils.Utils.loadImageMessage
import com.solvay.databinding.FragmentChatBinding
import com.solvay.enumClasses.ChatType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.TimeZone
import java.util.concurrent.TimeUnit
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import org.json.JSONException
import java.io.IOException
import kotlin.coroutines.resume

class ChatFragment : BaseFragment(), MessageAdapter.OnClickListener {

    private lateinit var mBinding: FragmentChatBinding
    private val mViewModel by viewModels<ChatViewModels>()
    private var userChatId = ""
    private var list: ArrayList<ChatHistory> = ArrayList()
    private var reverseList: ArrayList<ChatHistory> = ArrayList()
    private var yearMonthDate = ""
    private var finalDateFormats = ""
    private var amPms = ""
    private var replyID: String? = null
    private var messageType = ""
    var bundle = Bundle()
    private var finalDateFormatsForMessage = ""
    private var messageDateMonth = ""
    private var messageDays: Long = 0L
    private var indexOfUnread = -1
    private var deletePosition = 0
    private var galleryPermissionClick = 0
    private var lastPosition = -1
    private var imageCompressed: Uri? = Uri.EMPTY
    private var isApiSuccess = false
    private var lastMessage: ChatHistory? = null
    private var newMessageList: ArrayList<ChatHistory> = ArrayList()
    private lateinit var socket: SocketIOImpl
    private val childAdapter = MessageAdapter(getLayoutsMap(), this@ChatFragment)
    private var activeAudioPlayer: AudioPlayerView? = null
    var firstVisibleItemPosition = 0
    var isFirstEmission = true
    private var isCreated = false
    private var offset = 0
    private var isPagination = false
    private lateinit var alertManager: AlertManager
    var connectivityLiveData: ConnectivityLiveData? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val baseContext = LocaleHelper.setLocale(requireContext(), Prefs.init().appLanguageCode ?: "en")
        val themedContext = ContextThemeWrapper(baseContext, R.style.AppTheme)
        val localizedInflater = inflater.cloneInContext(themedContext)
        mBinding = FragmentChatBinding.inflate(localizedInflater, container, false)
        socket = MainApplication.get().socket
        mBinding.tvOtherUserName.text = arguments?.getString("name").toString()
        alertManager = AlertManager(mBinding.root as ViewGroup, requireContext())

        hideNavBottom()
        mBinding.tvOtherUserName.text = arguments?.getString("name").toString()
        if (arguments?.getString("_id")?.isNotEmpty() == true) {
            userChatId = arguments?.getString("_id").toString()
            Prefs.init().chatOtherUserID = userChatId
            Prefs.init().chatOtherUserIDNotification = userChatId
        }

        if (arguments?.getString("image").isNullOrEmpty()) {
            CoroutineScope(Dispatchers.Main).launch {
                mBinding.ivUserProfileImage.setImageResource(R.drawable.ic_greyuser)
            }

        } else {
            CoroutineScope(Dispatchers.IO).launch {
                mBinding.ivUserProfileImage.loadImageAsync(
                    BuildConfig.IMAGE_URL + arguments?.getString("image").toString()
                )
            }
            childAdapter.userImage = arguments?.getString("image").toString()
            childAdapter.userName = mBinding.tvOtherUserName.text.toString()
        }
        chatHistoryObserver()
        getChatHistory()
        imageUploadObserver()
        return mBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mBinding.IvBack.setOnClickListener {
            findNavController().navigateUp()
        }

        mBinding.replyClose.setOnClickListener {
            mBinding.clReplyMessage.visibility = View.GONE
            mBinding.tvReplyMsg.text = ""
        }
        mBinding.ivScrollDown.setOnClickListener {
            mBinding.rvMessageChat.smoothScrollToPosition(childAdapter.list.size-1)
        }


        mBinding.ivSendButton.setOnClickListener {
            mBinding.ivImageButton.disableMultiTap()
            mBinding.ivSendButton.disableSendBtn()
            if (context != null) {
                if (isInternetAvailable(requireContext())) {
                    if (mBinding.edMessage.text.trim().toString() != "") {
                        messageType = "text"
                        if (replyID.isNullOrEmpty()) {
                            sendMessage(
                                userChatId,
                                mBinding.edMessage.text.trim().toString(),
                                messageType,
                                "",
                                null
                            )
                            mBinding.ivSendButton.visibility = View.VISIBLE
                        } else {
                            sendMessage(
                                userChatId,
                                mBinding.edMessage.text.trim().toString(),
                                messageType,
                                "",
                                replyID.toString()
                            )
                            mBinding.clReplyMessage.visibility = View.GONE
                            Log.e("replyIdverify", "12" + replyID.toString())
                        }
                        mBinding.edMessage.text.clear()
                        mBinding.close.visibility = View.GONE
                        replyID = null
                    }
                } else {
                    val baseContext = LocaleHelper.setLocale(requireContext(), Prefs.init().appLanguageCode ?: "en")
alertManager.showError(
    baseContext.getString(R.string.check_internet_connection)
)

                }
            }
        }

        mBinding.ivImageButton.setOnClickListener {
            if (context != null) {
                if (hasGalleryPermissions()) {
//                    openGalleryForImageAndVideo()
                    isFirstEmission = true
                    openFilePicker()
                } else {
                    if (Prefs.init().permissionClickGallery < 2) {
                        requestGalleryPermissions()
                        Prefs.init().permissionClickGallery++
                    } else {
                        callCustomFunction()
                    }
                }
            }
        }


        val linearLayoutManager = LinearLayoutManager(context)
        mBinding.rvMessageChat.layoutManager = linearLayoutManager
        mBinding.rvMessageChat.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                firstVisibleItemPosition += 1

                if (childAdapter.list.size - 2 > linearLayoutManager.findLastVisibleItemPosition()) {
                    mBinding.ivScrollDown.visibility = View.VISIBLE
                } else {
                    mBinding.ivScrollDown.visibility = View.GONE
                }

                if (!recyclerView.canScrollVertically(1)) {
                    mBinding.ivScrollDown.visibility = View.GONE
                } else if (!recyclerView.canScrollVertically(-1) && dy < 0) {
                    isCreated = false
                    offset += 70
                    isPagination = true
                    if (indexOfUnread != -1) {
                        reverseList.removeIf { it.type.lowercase() == "unread" }
                    }

                    if (context != null) {
                        getChatHistory()
                    }

                    mBinding.progressBar.clMain.visibility = View.VISIBLE
                }
            }

            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)

                when (newState) {
                    AbsListView.OnScrollListener.SCROLL_STATE_FLING -> {
                    }

                    AbsListView.OnScrollListener.SCROLL_STATE_TOUCH_SCROLL -> {

                    }

                    AbsListView.OnScrollListener.SCROLL_STATE_IDLE -> {
                        /* if (linearLayoutManager.findLastVisibleItemPosition()<0){
                             mBinding.ivScrollDown.visibility = View.GONE
                         }*/
                    }

                    else -> {
                    }
                }

            }
        })


//        mBinding.ivImageButton.setOnClickListener {
//            if (context != null) {
//                val readImagePermission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
//                    Manifest.permission.READ_MEDIA_IMAGES
//                else
//                    Manifest.permission.READ_EXTERNAL_STORAGE
//
//                if (Prefs.init().permissionClickGallery < 2) {
//                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
//                        if (ContextCompat.checkSelfPermission(
//                                requireContext(),
//                                readImagePermission
//                            ) == PackageManager.PERMISSION_DENIED
//                        ) {
//                            ActivityCompat.requestPermissions(
//                                requireActivity(),
//                                arrayOf(
//                                    Manifest.permission.READ_MEDIA_IMAGES,
//                                    Manifest.permission.READ_MEDIA_VIDEO, // 👈 for videos
//                                    Manifest.permission.READ_EXTERNAL_STORAGE
//                                ),
//                                1
//                            )
//                            galleryPermissionClick++
//                            Prefs.init().permissionClickGallery = galleryPermissionClick
//                        } else {
//                            openGalleryForImageAndVideo()
//                        }
//                    } else {
//                        if (ActivityCompat.checkSelfPermission(
//                                requireActivity(),
//                                Manifest.permission.WRITE_EXTERNAL_STORAGE
//                            ) != PackageManager.PERMISSION_GRANTED
//                        ) {
//                            ActivityCompat.requestPermissions(
//                                requireActivity(),
//                                arrayOf(
//                                    Manifest.permission.READ_EXTERNAL_STORAGE,
//                                    Manifest.permission.WRITE_EXTERNAL_STORAGE
//                                ), 1
//                            )
//                            galleryPermissionClick++
//                            Prefs.init().permissionClickGallery = galleryPermissionClick
//                        } else {
//                            openGalleryForImageAndVideo()
//                        }
//                    }
//                } else {
//                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
//                        if (ActivityCompat.checkSelfPermission(
//                                requireContext(),
//                                readImagePermission
//                            ) != PackageManager.PERMISSION_GRANTED
//                        ) {
//                            callCustomFunction()
//                        } else {
//                            openGalleryForImageAndVideo()
//                        }
//                    } else {
//                        if (ActivityCompat.checkSelfPermission(
//                                requireContext(),
//                                Manifest.permission.READ_EXTERNAL_STORAGE
//                            ) != PackageManager.PERMISSION_GRANTED ||
//                            ActivityCompat.checkSelfPermission(
//                                requireActivity(),
//                                Manifest.permission.WRITE_EXTERNAL_STORAGE
//                            ) != PackageManager.PERMISSION_GRANTED
//                        ) {
//                            callCustomFunction()
//                        } else {
//                            openGalleryForImageAndVideo()
//                        }
//                    }
//                }
//            }
//        }
    }

    // 👉 Create a function to open both image + video picker:
    private fun openGalleryForImageAndVideo() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "*/*"
        intent.putExtra(Intent.EXTRA_MIME_TYPES, arrayOf("image/*", "video/*")) // 👈 allow both
        startActivityForResult(intent, 1)
    }

    private fun openFilePicker() {
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.type = "*/*"
        intent.putExtra(Intent.EXTRA_MIME_TYPES, arrayOf(
            "image/*",
            "video/*",
            "audio/*",
            "application/pdf",
            "application/msword",
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
            "application/vnd.ms-excel",
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
            "text/plain"
        ))
        intent.addCategory(Intent.CATEGORY_OPENABLE)

        val chooser = Intent.createChooser(intent, "Select File")
        startActivityForResult(chooser, 1)
    }
    private fun hasGalleryPermissions(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.READ_MEDIA_IMAGES
            ) == PackageManager.PERMISSION_GRANTED &&
                    ContextCompat.checkSelfPermission(
                        requireContext(),
                        Manifest.permission.READ_MEDIA_VIDEO
                    ) == PackageManager.PERMISSION_GRANTED
        } else {
            ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.READ_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED
        }
    }

    private fun requestGalleryPermissions() {
        val permissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            arrayOf(
                Manifest.permission.READ_MEDIA_IMAGES,
                Manifest.permission.READ_MEDIA_VIDEO
            )
        } else {
            arrayOf(
                Manifest.permission.READ_EXTERNAL_STORAGE
            )
        }
        ActivityCompat.requestPermissions(requireActivity(), permissions, 1)
    }

    private fun callCustomFunction() {
        val builder = AlertDialog.Builder(requireContext())
        val boldText = "<b>WeDo 80</b>"
        val message = getString(R.string.gallery_permission)

        builder.setTitle(Html.fromHtml(boldText, Html.FROM_HTML_MODE_LEGACY))
        builder.setMessage(message)
        builder.setPositiveButton(getString(R.string.ok)) { _, _ ->
            // Open settings after user clicks OK
            openAppSettings()
        }
        builder.setNegativeButton(getString(R.string.cancel)) { dialog, _ ->
            dialog.dismiss()
        }
        val dialog = builder.create()
        dialog.show()
    }

    private fun openAppSettings() {
        showProgress()
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
        with(intent) {
            data = Uri.fromParts("package", requireContext().packageName, null)
            addCategory(Intent.CATEGORY_DEFAULT)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY)
            addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS)
        }
        startActivity(intent)
    }

    private fun getChatHistory() {
        if (context != null) {
            if (isInternetAvailable(requireContext())) {
                showProgress()
                mViewModel.chatHistory(
                    ChatHistoryRequest(
                        offset = offset,
                        limit = 70,
                        partner = userChatId
                    )
                )
            } else {
                hideProgressBar()
                val baseContext = LocaleHelper.setLocale(requireContext(), Prefs.init().appLanguageCode ?: "en")
                alertManager.showError(baseContext.getString(R.string.no_internet_message))


            }
        }
    }


    @SuppressLint("NotifyDataSetChanged", "SetTextI18n")
    private fun chatHistoryObserver() {
        mViewModel.chatHistoryResponseData.observe(viewLifecycleOwner, EventObserver { it ->
            mBinding.progressBar.clMain.visibility = View.GONE
//            mBinding.animationView.visibility = View.GONE
//            mBinding.ivScrollDown.visibility = View.GONE
//            mBinding.whiteLotteAnimation.visibility = View.GONE
            if (it.success) {

                it.data?.let {

                    if (!it.data.list.isNullOrEmpty()) {
                        list = it.data.list

                        if (it.user.isOnline) {
                            mBinding.lastSeen.text = getString(R.string.active_now)
                            mBinding.ivBlueTick.visibility = View.VISIBLE
                        } else {
                            mBinding.lastSeen.text = getString(R.string.active_now)
                            mBinding.ivBlueTick.visibility = View.GONE
                        }
                        list.reverse()
                        val item = ChatHistory(
                            0,
                            "",
                            "",
                            null,
                            "",
                            "",
                            "",
                            false,
                            "",
                            "",
                            "",
                            "",
                            "",
                            false,
                            "",
                            false
                        )


                        var previousDate = ""
                        val iterator = list.listIterator()
                        var isFirstItem = true

                        while (iterator.hasNext()) {
                            val currentItem = iterator.next()
                            convertDateAndTime(currentItem.createdAt)
                            convertDateAndTime2(previousDate)

                            if (Utils.yearMonthDate != yearMonthDate && yearMonthDate != "") {
                                item.type = "date"

                                if (Utils.hours <= 24L) {
                                    item.createdAt = "Today"
                                } else {
                                    item.createdAt = finalDateFormats
                                }

                                iterator.previous()
                                iterator.add(item.copy()) // Adding a copy to avoid ConcurrentModificationException
                                iterator.next()
                            } else if (isFirstItem) {
                                /** Handle the first item explicitly for "Today"*/
                                /** Handle the first item explicitly for "Today"*/
                                item.type = "date"
                                if (Utils.hours <= 24L) {
                                    item.createdAt = "Today"
                                } else {
                                    convertDateForFirstMessage(currentItem.createdAt)
                                    item.createdAt = finalDateFormatsForMessage
                                }
                                iterator.previous()
                                iterator.add(item.copy())
                                iterator.next()
                                isFirstItem = false
                            }
                            previousDate = currentItem.createdAt
                        }
                        val originalMessages = list.map { it.text ?: "" }
// Ensure the reverseList is unique and add the new items
                        reverseList =
                            reverseList.distinctBy { it1 -> it1._id } as ArrayList<ChatHistory>
                        reverseList.addAll(0, list)

                        childAdapter.layoutId = getLayoutsMap()
                        mBinding.rvMessageChat.reStoreState()
                        childAdapter.list = reverseList
//                            childAdapter.list.findLast { it1 -> it1.seenStatus == "seen" }?.showSeen =
//                                true
                        mBinding.rvMessageChat.adapter = childAdapter
                        //    childAdapter.unReadCountValue = totalUnreadMessages
                        childAdapter.otherUserId = userChatId
                        childAdapter.notifyDataSetChanged()
                        isApiSuccess = true
                        if (indexOfUnread == -1) {
                            mBinding.rvMessageChat.scrollToPosition(list.size - 1)
                        } else {
                            mBinding.rvMessageChat.scrollToPosition(indexOfUnread - 2)
                        }


                    } else {
                        isApiSuccess = true
                    }
                }

            }
        })
        mViewModel.baseResponse.observe(viewLifecycleOwner, EventObserver { it ->
            Log.e("hfsgdfsf", it.toString())
            if (it.success) {
                childAdapter.list[deletePosition].isMenuOpen = false
                childAdapter.list[deletePosition].unsend = true
                childAdapter.notifyItemRemoved(deletePosition)
            }
        })
        mViewModel.showLoading.observe(viewLifecycleOwner) {

        }

        mViewModel.showMessage.observe(viewLifecycleOwner) {
            if (!it.isNullOrEmpty() || it.contains("body.").not()) {
                if (it.contains(getString(R.string.user_not_found))) {
                    mBinding.messageSend.visibility = View.VISIBLE
                    mBinding.ivImageButton.visibility = View.VISIBLE
                }

            }
        }

    }

    override fun onResume() {
        super.onResume()
        isFirstEmission = true
        connectivityLiveData = ConnectivityLiveData(requireContext())
//        connectivityLiveData?.observe(this) { isConnected ->
//            if (isFirstEmission) {
//                isFirstEmission = false
//                return@observe
//            }
//
//            if (isConnected) {
//                alertManager.showSuccess(getString(R.string.back_online))
//                offset = 0
//                childAdapter.list.clear()
//                getChatHistory()
//            }else{
//                alertManager.showError(getString(R.string.no_internet_message))
//            }
//        }
        if (!socket.isConnected()) {
            socket.connect()
        } else {
            setSocketListenON()
        }
    }

    private fun RecyclerView.reStoreState() {
        val recyclerViewState = this.layoutManager?.onSaveInstanceState()
        this.layoutManager?.onRestoreInstanceState(recyclerViewState)
    }

    private fun convertDateForFirstMessage(dateTimeString: String) {
        try {
            val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.ENGLISH)
            val outputFormat = SimpleDateFormat("yyyy-MM-dd hh:mm:ss a", Locale.ENGLISH)

            /** Parse the input timestamp to Date object*/
            val data = inputFormat.parse(dateTimeString)
            val formattedDate = data?.let { outputFormat.format(it) }

            /** Convert UTC formatted date to local time*/
            val df = SimpleDateFormat("yyyy-MM-dd hh:mm:ss a", Locale.ENGLISH)
            df.timeZone = TimeZone.getTimeZone("UTC")
            val utcDate = formattedDate?.let { df.parse(it) }
            df.timeZone = TimeZone.getDefault()
            val localFormattedDate = utcDate?.let { df.format(it) }

            /** Get year-month-days (01Jan2001) information here*/
            val yearMonths = SimpleDateFormat("EEEE, dd MMM yyyy", Locale.ENGLISH)
            finalDateFormatsForMessage = localFormattedDate.let {
                df.parse(it)
                    ?.let { it1 -> yearMonths.format(it1) }
            }?:""

        } catch (e: Exception) {
            e.printStackTrace()
            // Handle the parsing exception
        }
    }


    private fun convertDateAndTime2(dateTimeString: String) {
        try {
            val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.ENGLISH)
            val outputFormat = SimpleDateFormat("yyyy-MM-dd hh:mm:ss a", Locale.ENGLISH)

            /** Parse the input timestamp to Date object*/
            val data = inputFormat.parse(dateTimeString)
            val formattedDate = data?.let { outputFormat.format(it) }

            /** Convert UTC formatted date to local time*/
            val df = SimpleDateFormat("yyyy-MM-dd hh:mm:ss a", Locale.ENGLISH)
            df.timeZone = TimeZone.getTimeZone("UTC")
            val utcDate = formattedDate?.let { df.parse(it) }
            df.timeZone = TimeZone.getDefault()
            val localFormattedDate = utcDate?.let { df.format(it) }

            /** Get hours/time information*/
            val timeFormat = SimpleDateFormat("hh:mm a", Locale.ENGLISH)
            Utils.time = localFormattedDate?.let {
                df.parse(it)
                    ?.let { it1 -> timeFormat.format(it1) }
            }!!
            /** Get year information*/
            val yearMonth = SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH)
            yearMonthDate = localFormattedDate.let {
                df.parse(it)
                    ?.let { it1 -> yearMonth.format(it1) }
            }!!
            /** Get AM/PM information*/
            val amPmFormat = SimpleDateFormat("a", Locale.ENGLISH)
            amPms = localFormattedDate.let {
                df.parse(it)
                    ?.let { it1 -> amPmFormat.format(it1) }
            }!!
            /** Get year-month-days (01Jan2001) information here*/
            val yearMonths = SimpleDateFormat("EEEE, dd MMM yyyy", Locale.ENGLISH)
            finalDateFormats = localFormattedDate.let {
                df.parse(it)
                    ?.let { it1 -> yearMonths.format(it1) }
            }!!
            /** Get year-month-days (01Jan2001) information here*/
            val yearsMonths = SimpleDateFormat("MMM dd , yyyy", Locale.ENGLISH)
            messageDateMonth = localFormattedDate.let {
                df.parse(it)
                    ?.let { it1 -> yearsMonths.format(it1) }
            }!!
            /**Calculate time differences*/
            val cal1: Calendar = Calendar.getInstance()
            val cal2: Calendar = Calendar.getInstance()
            cal2.time = df.parse(localFormattedDate)!!
            val msDiff: Long = cal1.timeInMillis - cal2.timeInMillis
            messageDays = TimeUnit.MILLISECONDS.toDays(msDiff)
            /*    Utils.monthsDiff = (Utils.daysDiff / 30.44).toLong()
                Utils.seconds = TimeUnit.MILLISECONDS.toSeconds(msDiff)
                Utils.minutes = TimeUnit.MILLISECONDS.toMinutes(msDiff)
                Utils.hours = TimeUnit.MILLISECONDS.toHours(msDiff)*/
        } catch (e: Exception) {
            e.printStackTrace()
            // Handle the parsing exception
        }
    }

    private fun getLayoutsMap(): HashMap<ChatType, Int> {
        val layoutMap = HashMap<ChatType, Int>()
        layoutMap[ChatType.MESSAGE_SENDER] = R.layout.send_message_layout
        layoutMap[ChatType.MESSAGE_RECEIVER] = R.layout.receive_message_layout
        layoutMap[ChatType.IMAGE_SENDER] = R.layout.image_sender_layout
        layoutMap[ChatType.IMAGE_RECEIVER] = R.layout.image_receiver_layout
        layoutMap[ChatType.DOCUMENT_SENDER] = R.layout.document_layout
        layoutMap[ChatType.DOCUMENT_RECEIVER] = R.layout.recivce_document
        layoutMap[ChatType.DATE] = R.layout.date_layout
        // layoutMap[ChatType.UNREAD] = R.layout.unread_message
        return layoutMap
    }

    private fun sendMessage(
        userID: String,
        message: String,
        type: String,
        file: String,
        replyMessage: String? = null
    ) {
        val obj = Utils.toJsonObject(
            "text" to message,
            "replyId" to replyMessage,
            "user" to userID,
            "type" to type,
            "file" to file
        )
        socket.emitMessage("message", obj) { _, _ ->

        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        Log.e("imageUploadSuccesfullly", data?.data.toString())

        if (requestCode == 1) {
            if (data?.data != null) {
                showProgress()
                lifecycleScope.launch(Dispatchers.IO) {
                    val fileUri = data.data!!
                    val fileSizeInMB = getFileSizeInMB(requireContext(), fileUri)
                    Log.e("FILE_SIZE_MB", "$fileSizeInMB MB")

                    if (fileSizeInMB > 25) {
                        withContext(Dispatchers.Main) {
                            Toast.makeText(
                                requireContext(),
                                getString(R.string.file_size_exceeds),
                                Toast.LENGTH_LONG
                            ).show()
                        }
                        return@launch
                    }
                    Log.e("imageUploadSuccesfullly", data?.data.toString())
                    val mimeType = requireContext().contentResolver.getType(fileUri)
                    val parts = mimeType?.split("/") // Split by "/"

                    val type = parts?.getOrNull(0)   // "image"
                    val extension = parts?.getOrNull(1)  // "jpeg"


                    when {
                        mimeType?.startsWith("image/") == true -> {
                            Log.e("File Type", getString(R.string.its_an_image))
                        }

                        mimeType?.startsWith("video/") == true -> {
                            Log.e("File Type", getString(R.string.its_a_video))
                        }

                        mimeType == "application/pdf" -> {
                            Log.e("File Type", getString(R.string.its_a_pdf))
                        }

                        else -> {
                            Log.e("File Type", getString(R.string.other_type))
                        }
                    }
                    Log.e("imageUploadSuccesfullly", type ?: "Unknown")
                    Log.e("imageUploadSuccesfullly", extension ?: "Unknown")
                    Log.e("imageUploadSuccesfullly", mimeType.toString())
                    if (type != null) {
                        messageType = type
                        val extensionWithDot = ".$extension" // like ".jpg" or ".mp4"

                        //    val inputStream = requireContext().contentResolver.openInputStream(data!!.data!!)
                        //  val bytes = data?.data?.let { readInputStreamSafely(it) } ?: byteArrayOf()
                        //    inputStream?.close()
                        val compressedUri =
                            handleFileCompression(requireContext(), data?.data!!, mimeType)

                        // Now you can use compressedUri
                        val inputStream =
                            requireContext().contentResolver.openInputStream(compressedUri)
                        val bytes = inputStream?.readBytes() ?: byteArrayOf()
                        inputStream?.close()
                        val filename = "${data.data}" + extensionWithDot

                        Log.e("imageUploadSuccesfullly", filename.toString())

                        if (messageType == "image") {
                            val requestBody = bytes.toRequestBody("image/*".toMediaTypeOrNull())
                            val image = MultipartBody.Part.createFormData(
                                "media",
                                filename,
                                requestBody
                            )
                            mViewModel.uploadChatMedia(image)

                        } else if (messageType == "video") {
                            val requestBody = bytes.toRequestBody("video/*".toMediaTypeOrNull())
                            val video = MultipartBody.Part.createFormData(
                                "media",
                                filename,
                                requestBody
                            )
                            mViewModel.uploadChatMedia(video)
                        } else if (messageType == "application") {
                            messageType = "document"
                            val requestBody = bytes.toRequestBody("pdf/*".toMediaTypeOrNull())
                            val video = MultipartBody.Part.createFormData(
                                "media",
                                filename,
                                requestBody
                            )
                            mViewModel.uploadChatMedia(video)
                        } else if (messageType == "audio") {
                            val requestBody = bytes.toRequestBody("audio/*".toMediaTypeOrNull())
                            val video = MultipartBody.Part.createFormData(
                                "media",
                                filename,
                                requestBody
                            )
                            mViewModel.uploadChatMedia(video)
                        }
                    }


//                    imageCompressed = async {
//                        data?.data?.let { compressImagesWithGlide(requireContext(), it) }
//                    }.await()

                }
            } else {
                hideProgressBar()
            }


        }

    }

    private fun getFileSizeInMB(context: Context, uri: Uri): Double {
        val returnCursor = context.contentResolver.query(uri, null, null, null, null)
        val sizeIndex = returnCursor?.getColumnIndex(OpenableColumns.SIZE) ?: -1
        returnCursor?.moveToFirst()
        val sizeInBytes = if (sizeIndex != -1) returnCursor?.getLong(sizeIndex) ?: 0L else 0L
        returnCursor?.close()
        return sizeInBytes / (7024.0 * 7024.0) // Convert to MB
    }

    private fun copyUriToTempFile(uri: Uri): File? {
        return try {
            val inputStream = requireContext().contentResolver.openInputStream(uri)
            val tempFile = File.createTempFile("temp_upload", null, requireContext().cacheDir)
            val outputStream = FileOutputStream(tempFile)

            inputStream?.use { input ->
                outputStream.use { output ->
                    val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
                    var bytesRead: Int

                    while (input.read(buffer).also { bytesRead = it } != -1) {
                        output.write(buffer, 0, bytesRead)
                    }
                }
            }

            tempFile
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun readInputStreamSafely(uri: Uri): ByteArray? {
        return try {
            val buffer = ByteArrayOutputStream()
            val inputStream = requireContext().contentResolver.openInputStream(uri)
            val data = ByteArray(DEFAULT_BUFFER_SIZE)
            var nRead: Int

            inputStream?.use { stream ->
                while (stream.read(data, 0, data.size).also { nRead = it } != -1) {
                    buffer.write(data, 0, nRead)
                }
            }

            buffer.toByteArray()
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }


    private fun imageUploadObserver() {
        mViewModel.uploadMediaResponseData.observe(viewLifecycleOwner, EventObserver { response ->
            if (response != null) {
                if (response.success) {
                    response.data?.file?.let {
                        sendMessage(
                            userChatId,
                            mBinding.edMessage.text.trim().toString(),
                            messageType,
                            it,
                            replyID
                        )
                    }
                    mBinding.clReplyMessage.visibility = View.GONE
                }
                replyID = null
            }else{
                replyID = null
            }
        })
        mViewModel.showLoading.observe(requireActivity()) {
        }
    }

    private fun compressImagesWithGlide(context: Context, originalImageUri: Uri): Uri {
        var compressedImageUris: Uri = Uri.EMPTY
        val displayMetrics = DisplayMetrics()
        requireActivity().windowManager.defaultDisplay.getMetrics(displayMetrics)
        val screenWidth = displayMetrics.widthPixels
        try {
            // Compress the image using Glide and obtain the compressed bitmap
            val compressedBitmap = Glide.with(context)
                .asBitmap()
                .load(originalImageUri)
                .apply(
                    RequestOptions()
                        .format(DecodeFormat.PREFER_RGB_565)
                        .override(screenWidth, 7024)
                        .fitCenter()
                        .diskCacheStrategy(DiskCacheStrategy.NONE) // Disable disk caching
                )
                .submit()
                .get()


            val compressedImageFile =
                File.createTempFile("compressed_image", ".jpg", context.cacheDir)

            FileOutputStream(compressedImageFile).use { outputStream ->
                compressedBitmap.compress(Bitmap.CompressFormat.JPEG, 80, outputStream)
            }
            val compressedImageUri = Uri.fromFile(compressedImageFile)
            compressedImageUris = compressedImageUri
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return compressedImageUris
    }

    private fun handleFileCompression(context: Context, originalUri: Uri, mimeType: String): Uri {
        return when {
            mimeType.startsWith("image/") -> compressImage(context, originalUri)
            mimeType.startsWith("video/") -> compressVideo(context, originalUri)
            mimeType == "application/pdf" || mimeType.startsWith("application/") -> copyDocument(
                context,
                originalUri
            )

            else -> originalUri // fallback if unknown type
        }
    }

    private fun compressImage(context: Context, originalUri: Uri): Uri {
        var compressedUri: Uri = Uri.EMPTY
        val displayMetrics = context.resources.displayMetrics
        val screenWidth = displayMetrics.widthPixels

        try {
            val compressedBitmap = Glide.with(context)
                .asBitmap()
                .load(originalUri)
                .apply(
                    RequestOptions()
                        .format(DecodeFormat.PREFER_RGB_565)
                        .override(screenWidth, 7024)
                        .fitCenter()
                        .diskCacheStrategy(DiskCacheStrategy.NONE)
                )
                .submit()
                .get()

            val compressedImageFile =
                File.createTempFile("compressed_image", ".jpg", context.cacheDir)
            FileOutputStream(compressedImageFile).use { outputStream ->
                compressedBitmap.compress(Bitmap.CompressFormat.JPEG, 80, outputStream)
            }
            compressedUri = Uri.fromFile(compressedImageFile)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return compressedUri
    }

    private fun compressVideo(context: Context, originalUri: Uri): Uri {
        // For real compression, use libraries like LightCompressor or FFmpeg.
        // For now, just copying to cache folder (without real compression).
        val inputStream = context.contentResolver.openInputStream(originalUri)
        val tempFile = File.createTempFile("compressed_video", ".mp4", context.cacheDir)
        inputStream?.use { input ->
            tempFile.outputStream().use { output ->
                input.copyTo(output)
            }
        }
        return Uri.fromFile(tempFile)
    }

    private fun copyDocument(context: Context, originalUri: Uri): Uri {
        val inputStream = context.contentResolver.openInputStream(originalUri)
        val tempFile = File.createTempFile("copied_document", ".pdf", context.cacheDir)
        inputStream?.use { input ->
            tempFile.outputStream().use { output ->
                input.copyTo(output)
            }
        }
        return Uri.fromFile(tempFile)
    }


    private fun setSocketListenON() {
        //   socket.listenOff("chatSuccess")
        socket.listenOff("seenChatSuccess")
        socket.listenOff("messageSuccess")
        socket.listenOff("reactMessageSuccess")
        socket.listenOff("unsendMessageSuccess")
        socket.listenOff("onlineStatus")
        socket.listenOff("typingSuccess")
        socket.listenOff("onlineStatus")
        socket.listenOff("error")
        socket.listenOn("error") { args ->
            CoroutineScope(Dispatchers.Main).launch {
                val obj = JSONObject(args[0].toString())
                Log.e("jsakhdjdhsad", obj.toString())
                // Utils.showToast(message)
            }
        }
        socket.listenOn("onlineStatus") { args ->
            CoroutineScope(Dispatchers.Main).launch {

                val obj = JSONObject(args[0].toString())
                Log.e("payloadValue", "OnlineStatus" + obj.toString())
                val userId = obj.getString("user")
                val isOnline = obj.getBoolean("isOnline")
                Log.e("UserID", userId)
                if (userId == userChatId) {
                    if (isOnline) {
                        mBinding.lastSeen.visibility = View.VISIBLE
                        mBinding.ivBlueTick.visibility = View.VISIBLE

                            mBinding.lastSeen.text = requireContext().getString(R.string.active_now)


                    } else {
                        mBinding.lastSeen.visibility = View.GONE
                        mBinding.ivBlueTick.visibility = View.GONE
                    }
                }

            }
        }
        socket.listenOn("messageSuccess") { args ->
            CoroutineScope(Dispatchers.Main).launch {
                hideProgressBar()
                val obj = JSONObject(args[0].toString())
                Log.e("payloadValue", obj.toString())
                childAdapter.otherUserId = userChatId
                val gson = Gson()
                val listType = object : TypeToken<ChatHistory>() {}.type
                val newList = gson.fromJson<ChatHistory>(obj.toString(), listType)
                Log.e("payloadValue", newList.toString())

                lastMessage = newList
                val item =
                    ChatHistory(
                        0,
                        "",
                        "",
                        null,
                        "",
                        "",
                        "",
                        false,
                        "",
                        "",
                        "",
                        "",
                        "",
                        false,
                        "",
                        false
                    )
                if (reverseList.isEmpty()) {
                    if (newMessageList.isNotEmpty()) {
                        newMessageList.forEach {
                            reverseList.add(it)
                        }
                        newMessageList.clear()
                    }
                    item.type = "date"
                    item.createdAt = "Today"
                    reverseList.add(item)
                }
                CoroutineScope(Dispatchers.Main).launch {
                    if (!newList.self){
                        if (newList.type=="text"){
                            val message =   translateMessage(newList.text)
                            newList.text = message
                        }
                    }

                    reverseList.add(newList)
                    Log.e("payloadValue", reverseList.toString())
                    childAdapter.layoutId = getLayoutsMap()
                    mBinding.rvMessageChat.adapter = childAdapter
                    childAdapter.list = reverseList
                    childAdapter.notifyDataSetChanged()
                    mBinding.rvMessageChat.scrollToPosition(
                        childAdapter
                            .list.size - 1
                    )

                    if (lastPosition != -1) {
                        childAdapter.list[lastPosition].isMenuOpen = false
                        childAdapter.notifyItemChanged(lastPosition)
                    }
                    lastPosition = -1
                }






            }


        }


    }

    override fun onLongPressOnSenderMessage(
        view: View,
        position: Int,
        data: ChatHistory,
        time: String
    ) {

    }

    override fun onClickReply(position: Int, data: ChatHistory) {
        mBinding.clReplyMessage.visibility = View.VISIBLE
        Log.e("typeIsGet", data.type.toString())
        mBinding.apply {
            if (data.type == "text") {
                mediaReply.visibility = View.GONE
                tvReplyMsg.text = data.text
            } else {
                mBinding.mediaReply.visibility = View.VISIBLE
                /**Here we handle the small image for reply message */
                if (data.type == "image") {
                    replyClose.visibility = View.VISIBLE
                    CoroutineScope(Dispatchers.IO).launch {
                        mediaReply.loadImageMessage(BuildConfig.IMAGE_URL + data.file)
                    }
                } else if (data.type == "video") {
                    replyClose.visibility = View.VISIBLE
                    CoroutineScope(Dispatchers.IO).launch {
                        mediaReply.loadImageMessage(BuildConfig.IMAGE_URL + data.thumbnail)
                    }
                } else if (data.type == "audio") {
                    mediaReply.visibility = View.VISIBLE
                    mediaReply.setImageResource(R.drawable.audiotrack)
                } else if (data.type == "document") {
                    mediaReply.visibility = View.VISIBLE
                    mediaReply.setImageResource(R.drawable.google_docs)
                } else {
                    mediaReply.visibility = View.GONE
                }


                tvReplyMsg.text = data.type.capitalize()
            }
        }

        replyID = data._id
        Log.e("replyIdverify", "11" + replyID.toString())
    }

    override fun onClickCopy(position: Int, data: ChatHistory) {
        copyToClipboard(requireContext(), data.text)
    }

    override fun onClickDelete(position: Int, data: ChatHistory) {
        deletePosition = position
        mViewModel.deleteMessage(data._id)
    }

    override fun hideMenuOnClickRoot(position: Int, data: ChatHistory, type: String) {
        if (lastPosition != -1) {
            childAdapter.list[lastPosition].isMenuOpen = false
            childAdapter.notifyItemChanged(lastPosition)
        }
        lastPosition = -1
        Log.e("bdfghskdf", data.isMenuOpen.toString())
        Log.e("bdfghskdf", lastPosition.toString())
    }

    override fun onClickItem(position: Int, data: ChatHistory) {
        showVideoAndImageDialog(data)

    }

    override fun onClickHideKeyboard(position: Int) {
        TODO("Not yet implemented")
    }

    override fun onClickDotLine(position: Int, data: ChatHistory) {

    }

    override fun onDownloadDecryptFile(position: Int, data: ChatHistory) {

    }

    override fun onClickDocument(position: Int, data: ChatHistory) {
        openPdfInChrome(BuildConfig.IMAGE_URL + data.file)
    }

    override fun onOpenReplyMenu(position: Int, data: ChatHistory, view: View) {
        val wasOpen = data.isMenuOpen == true

        // Close the previous one if needed
        if (lastPosition != -1 && lastPosition != position) {
            childAdapter.list[lastPosition].isMenuOpen = false
            childAdapter.notifyItemChanged(lastPosition)
        }

        // Toggle current
        data.isMenuOpen = !wasOpen
        lastPosition = position
        childAdapter.notifyItemChanged(position)

        Log.e("MenuStatus", "Position:$view $position, isOpen: ${data.isMenuOpen}")
    }


    @Subscribe()
    fun isSocketConnected(event: isSocketConnected) {
        if (event.isConnected) {
            setSocketListenON()
            //    getNotificationCount()
        }
    }

    private fun openPdfInChrome(url: String) {
        val newUrl = url.replace("\\", "/") // Replace backslashes

        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(newUrl)).apply {
            setPackage("com.android.chrome") // Open specifically in Chrome
        }

        try {
            startActivity(intent)
        } catch (e: ActivityNotFoundException) {
            // Chrome not installed, open with default browser
            val fallbackIntent = Intent(Intent.ACTION_VIEW, Uri.parse(newUrl))
            startActivity(fallbackIntent)
        }
    }

    private fun showVideoAndImageDialog(data: ChatHistory) {
        val dialogView = layoutInflater.inflate(R.layout.fragment_showimg_and_video, null)
        val imageView = dialogView.findViewById<ImageView>(R.id.imageView)
        val playerView = dialogView.findViewById<PlayerView>(R.id.playerView)
        val progressBar = dialogView.findViewById<ProgressBar>(R.id.progressBars)
        val closeButton = dialogView.findViewById<ImageButton>(R.id.closeButton)

        var exoPlayer: ExoPlayer? = null

        progressBar.visibility = View.VISIBLE

        val dialog = androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .setCancelable(false)
            .create()

        closeButton.setOnClickListener {
            exoPlayer?.release()
            dialog.dismiss()
        }

        if (data.type == "image") {
            progressBar.visibility = View.GONE
            imageView.visibility = View.VISIBLE
            playerView.visibility = View.GONE
            val imageUri = data.file
            CoroutineScope(Dispatchers.IO).launch {
                imageView.loadImageAsync(BuildConfig.IMAGE_URL + imageUri)
            }
        } else {
            imageView.visibility = View.GONE
            playerView.visibility = View.VISIBLE

            val videoUri = Uri.parse(BuildConfig.IMAGE_URL + data.file)
            exoPlayer = ExoPlayer.Builder(requireContext()).build().apply {
                setMediaItem(MediaItem.fromUri(videoUri))
                prepare()
                playWhenReady = true

                addListener(object : Player.Listener {
                    override fun onPlaybackStateChanged(state: Int) {
                        when (state) {
                            Player.STATE_BUFFERING -> progressBar.visibility = View.VISIBLE
                            Player.STATE_READY, Player.STATE_ENDED -> progressBar.visibility =
                                View.GONE
                        }
                    }
                })
            }

            playerView.player = exoPlayer
        }

        dialog.setOnDismissListener {
            exoPlayer?.release()
        }

        dialog.show()
    }

    fun showProgress() {
        mBinding.progressBar.clMain.visibility = View.VISIBLE
    }

    fun hideProgressBar() {
        mBinding.progressBar.clMain.visibility = View.GONE
    }

    private fun copyToClipboard(context: Context, text: String) {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("Copied Text", text)
        clipboard.setPrimaryClip(clip)
        val baseContext = LocaleHelper.setLocale(requireContext(), Prefs.init().appLanguageCode ?: "en")
        alertManager.showSuccess(baseContext.getString(R.string.copied))
//        Toast.makeText(context, "Copied to clipboard", Toast.LENGTH_SHORT).show()
    }

    override fun onDestroy() {
        super.onDestroy()
        Prefs.init().chatOtherUserIDNotification=""
    }
    override fun onAttach(context: Context) {
        val newContext = LocaleHelper.setLocale(context, Prefs.init().appLanguageCode ?: "en")
        super.onAttach(newContext)
    }



//    fun translateText(message: String, targetLanguage: String, callback: (String) -> Unit) {
//        if (message.isEmpty()) {
//            callback("")
//            return
//        }
//
//        val langCode = languageMap[targetLanguage.lowercase()] ?: "de"
//        val apiKey = BuildConfig.TRANSLATION_KEY
//        val url = "https://translation.googleapis.com/language/translate/v2?key=$apiKey"
//
//        val json = JSONObject()
//        json.put("q", message)
//        json.put("target", langCode)
//
//        Log.e("sdfsdfsdvvx","Message: ${message}")
//
//        val body = RequestBody.create("application/json; charset=utf-8".toMediaType(), json.toString())
//        val request = Request.Builder().url(url).post(body).build()
//
//        val client = OkHttpClient()
//        client.newCall(request).enqueue(object : Callback {
//            override fun onFailure(call: Call, e: IOException) {
//                callback(message)
//            }
//
//            override fun onResponse(call: Call, response: Response) {
//                val responseBody = response.body?.string()
//                Log.e("sdfsdfsdvvx", "Message: $responseBody")
//
//                if (responseBody != null) {
//                    try {
//                        val translatedText = JSONObject(responseBody)
//                            .getJSONObject("data")
//                            .getJSONArray("translations")
//                            .getJSONObject(0)
//                            .getString("translatedText")
//                        callback(translatedText)
//                    } catch (e: JSONException) {
//                        e.printStackTrace()
//                        callback(message) // Return original message on JSON parsing failure
//                    }
//                } else {
//                    callback(message) // Return original message if responseBody is null
//                }
//            }
//
//        })
//    }

    private suspend fun translateMessage(message: String): String {
        if (message.isEmpty()) {
            return message
        }
        return try {
            translateText(message, Prefs.init().appLanguageCode)
        } catch (e: Exception) {
            Log.e(TAG, "Translation failed: ${e.message}")
            message // Fallback to original message
        }
    }

    private suspend fun translateText(message: String, targetLanguage: String): String = suspendCancellableCoroutine { continuation ->
        if (message.isEmpty()) {
            continuation.resume("")
            return@suspendCancellableCoroutine
        }
        val apiKey = BuildConfig.TRANSLATION_KEY
        val url = "https://translation.googleapis.com/language/translate/v2?key=$apiKey"

        val json = JSONObject()
        json.put("q", message)
        json.put("target", targetLanguage)

        val body = RequestBody.create("application/json; charset=utf-8".toMediaType(), json.toString())
        val request = Request.Builder().url(url).post(body).build()

        val client = OkHttpClient()
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e(TAG, "Translation request failed: ${e.message}")
                continuation.resume(message) // Fallback to original message
            }

            override fun onResponse(call: Call, response: Response) {
                response.use {
                    if (!response.isSuccessful) {
                        Log.e(TAG, "Translation request failed with code: ${response.code}")
                        continuation.resume(message)
                        return
                    }

                    val responseBody = response.body?.string()
                    responseBody?.let {
                        try {
                            val translatedText = JSONObject(it)
                                .getJSONObject("data")
                                .getJSONArray("translations")
                                .getJSONObject(0)
                                .getString("translatedText")
                            continuation.resume(translatedText)
                        } catch (e: JSONException) {
                            Log.e(TAG, "JSON parsing error: ${e.message}")
                            continuation.resume(message)
                        }
                    } ?: run {
                        Log.e(TAG, "Response body is null")
                        continuation.resume(message)
                    }
                }
            }
        })
    }
}