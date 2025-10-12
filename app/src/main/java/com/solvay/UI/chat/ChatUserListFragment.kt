package com.solvay.UI.chat

import android.content.Context
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.ContextThemeWrapper
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import androidx.annotation.RequiresApi
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.solvay.Base.BaseFragment
import com.solvay.Localization.LocaleHelper
import com.solvay.MainApplication
import com.solvay.Network.SocketIOImpl
import com.solvay.R
import com.solvay.UI.Notification.adapter.NotificationAdapter
import com.solvay.Utils.EventObserver
import com.solvay.Utils.Prefs
import com.solvay.UI.chat.Adapter.ChatListAdapter
import com.solvay.UI.chat.ChatViewModel.ChatViewModels
import com.solvay.UI.chat.model.ChatListRequest
import com.solvay.UI.chat.model.UserData
import com.solvay.databinding.FragmentChatUserListBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


class ChatUserListFragment : BaseFragment(), ChatListAdapter.OnClickListener {
    private lateinit var mBinding: FragmentChatUserListBinding
    private lateinit var socket: SocketIOImpl
    private val mViewModel by viewModels<ChatViewModels>()
    private lateinit var adapterChatList: ChatListAdapter
    private var list: ArrayList<UserData> = ArrayList()
    private var searchJob: Job? = null
    private var currentPage = 1
    private var nextPage = 1
    private var isLoading = false
    private var isLastPage = false
    private var uniqueList: ArrayList<UserData> = ArrayList()
    var Offset = 0
    val oldestOrder = 1
    val latestOrder = -1
    var search = ""
    var Sort = "updatedAt"
    var Order = latestOrder
    var bundle = Bundle()
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val baseContext = LocaleHelper.setLocale(requireContext(), Prefs.init().appLanguageCode ?: "en")
        val themedContext = ContextThemeWrapper(baseContext, R.style.AppTheme)
        val localizedInflater = inflater.cloneInContext(themedContext)
        mBinding = FragmentChatUserListBinding.inflate(localizedInflater, container, false)
        if (Prefs.init().sort.isNotEmpty()){
            mBinding.tvOlder.text= if(Prefs.init().sort == oldestOrder.toString()){getString(R.string.oldest)}else{getString(R.string.newest)}
         if(Prefs.init().sort=="Newest"){
             Order=-1
         }else{
             Order=1
         }
        }


   /**  Only navigate if not handled yet*/
        if (arguments?.getBoolean("pushNot") == true) {
            val bundle = Bundle().apply {
                putString("name", arguments?.getString("name"))
                putString("_id", arguments?.getString("_id"))
                putString("image", arguments?.getString("image"))
                putBoolean("isOnline", arguments?.getBoolean("isOnline") ?: false)
            }

            // Remove the flag to prevent re-trigger
            arguments?.putBoolean("pushNot", false)

            findNavController().navigate(R.id.chatFragment, bundle)
        }

        return mBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        socket = MainApplication.get().socket
        setupTouchListenerToHideKeyboard(view)
        showNavBottom()
        setupRecyclerView()
        mBinding.apply {
            llSort.setOnClickListener {
                clSentMessageDate.visibility =View.VISIBLE
                searchEt.isClickable=false
            }
            tvNewest.setOnClickListener {
                Offset = 0
                Order = latestOrder
                Prefs.init().sort= latestOrder.toString()
                tvOlder.text= getString(R.string.newest)
                clSentMessageDate.visibility =View.GONE
                searchEt.isClickable=true
                chatListApi(Offset, limit = 20, search, Sort, Order)
            }
            tvOldest.setOnClickListener {
                Offset = 0
                Order = oldestOrder
                Prefs.init().sort=oldestOrder.toString()
                tvOlder.text=getString(R.string.oldest)
                clSentMessageDate.visibility =View.GONE
                searchEt.isClickable=true
                chatListApi(Offset, limit = 20, search, Sort, Order)
            }
            ivChatIcon.setOnClickListener {
                findNavController().navigate(R.id.newChatFragment)
            }
            refresh.setOnRefreshListener {
                refreshList()
            }
            searchEt.addTextChangedListener(object : TextWatcher{
                override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

                }

                override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                    if (searchEt.hasFocus()){
                        searchJob?.cancel()
//                        adapterChatList.list.clear()
//                        adapterChatList.notifyDataSetChanged()
                        searchJob = CoroutineScope(Dispatchers.Main).launch {
                            delay(300)
                            search = p0.toString()
                            chatListApi(Offset = 0, limit = 20, search = search, Sort = Sort, Order = Order)
                        }
                    }
                }

                override fun afterTextChanged(p0: Editable?) {
                }

            })
            observer()
            chatListApi(Offset, 20, search, Sort, Order)
        }


        if (!socket.isConnected()) {
            socket.connect()
            Log.e("SocketConnection", getString(R.string.socket_not_connected))
        } else {
            Log.e("SocketConnection", getString(R.string.socket_connected))
        }
    }

    fun refreshList() {
        resetPagination()
        chatListApi(Offset, limit = 20, search = search, Sort, Order)
    }

    private fun resetPagination() {
        Offset = 0
        search = ""
        mBinding.searchEt.setText("")
        Prefs.init().sort = latestOrder.toString()
        Order = latestOrder
        mBinding.tvOlder.text = getString(R.string.newest)
        currentPage = 1
        isLastPage = false
        isLoading = false
        list.clear()
        adapterChatList.updateList(list ?: arrayListOf())
    }

    private fun chatListApi(Offset: Int, limit: Int, search: String, Sort: String, Order: Int) {
        mViewModel.chatList(
            ChatListRequest(
                offset = Offset,
                limit = limit,
                search = search,
                sort = Sort,
                order = Order

            )
        )
        showProgress(true)
    }

    private fun RecyclerView.reStoreState() {
        val recyclerViewState = this.layoutManager?.onSaveInstanceState()
        this.layoutManager?.onRestoreInstanceState(recyclerViewState)
    }

    private fun observer() {
        mViewModel.chatListResponse.observe(viewLifecycleOwner, EventObserver { response ->
            showProgress(false)
            mBinding.refresh.isRefreshing = false
            if (response.success && response.data != null) {
                val newList = response.data?.list
                currentPage = response.data?.pagination?.currentPage?:1
                nextPage = response.data?.pagination?.nextPage?:2
                isLastPage = nextPage == currentPage || nextPage == 0
                isLoading = false

                if (newList?.isNotEmpty() == true){
                    if (currentPage == 1) {
                        adapterChatList.updateList(newList ?: arrayListOf())
                        val unseenCount = adapterChatList.list?.sumOf { it.unseenMessages } ?: 0
                        mBinding.tvMessage.text = when(unseenCount){
                            0 -> {"0 ${getString(R.string.message)}"}
                            1 -> {"1 ${getString(R.string.message)}"}
                            else -> {"$unseenCount ${getString(R.string.messages)}"}
                        }
                    } else {
                        adapterChatList.addNewListItems(newList ?: arrayListOf())
                        val unseenCount = adapterChatList.list?.sumOf { it.unseenMessages } ?: 0
                        mBinding.tvMessage.text = when(unseenCount){
                            0 -> {"0 ${getString(R.string.message)}"}
                            1 -> {"1 ${getString(R.string.message)}"}
                            else -> {"$unseenCount ${getString(R.string.messages)}"}
                        }
                    }
                }else{
                    adapterChatList.updateList(arrayListOf())
                    val unseenCount = adapterChatList.list?.sumOf { it.unseenMessages } ?: 0
                    mBinding.tvMessage.text = when(unseenCount){
                        0 -> {"0 ${getString(R.string.message)}"}
                        1 -> {"1 ${getString(R.string.message)}"}
                        else -> {"$unseenCount ${getString(R.string.messages)}"}
                    }
                }
            }
        })
    }

    private fun setupRecyclerView() {
        adapterChatList = ChatListAdapter(list, onClickListener =  this@ChatUserListFragment)
        mBinding.rvMessageList.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = adapterChatList

            addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                    super.onScrolled(recyclerView, dx, dy)

                    val layoutManager = recyclerView.layoutManager as LinearLayoutManager
                    val visibleItemCount = layoutManager.childCount
                    val totalItemCount = layoutManager.itemCount
                    val firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition()

                    if (!isLoading && !isLastPage) {
                        if ((visibleItemCount + firstVisibleItemPosition) >= totalItemCount
                            && firstVisibleItemPosition >= 0
                            && totalItemCount > 20) {
                            loadMoreItems()
                        }
                    }
                }
            })
        }
    }

    private fun loadMoreItems() {
        if (isLoading || isLastPage) return

        isLoading = true

        val nextPage = nextPage ?: currentPage + 1
        chatListApi(Offset = (nextPage - 1) * 20, limit = 20, Sort= Sort, Order = Order, search = search)
    }
    override fun onClickChat(position: Int, data: UserData) {
        bundle.putString("name", data.user.name)
        bundle.putString("_id", data.user._id)
        bundle.putString("image", data.user.image)
        bundle.putBoolean("isOnline", data.user.isOnline)
        findNavController().navigate(R.id.chatFragment , bundle)
    }

    override fun onCLickBlock(position: Int, data: UserData) {

    }

    override fun onClickDelete(position: Int, data: UserData) {

    }

    override fun onClickClear(position: Int, data: UserData) {

    }

    override fun onClickPin(position: Int, data: UserData) {

    }

    private fun setupTouchListenerToHideKeyboard(view: View) {
        if (view !is EditText) {
            view.setOnTouchListener { _, _ ->
                hideKeyboard()
                view.clearFocus()
                false
            }
        }
        if (view is ViewGroup) {
            for (i in 0 until view.childCount) {
                setupTouchListenerToHideKeyboard(view.getChildAt(i))
            }
        }
    }

    private fun hideKeyboard() {
        val imm = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        val view = requireActivity().currentFocus ?: View(requireContext())
        imm.hideSoftInputFromWindow(view.windowToken, 0)
    }

    private fun showProgress(visibility: Boolean){
        mBinding.includeProgress.clMain.visibility = if(visibility) View.VISIBLE else View.GONE
    }
}