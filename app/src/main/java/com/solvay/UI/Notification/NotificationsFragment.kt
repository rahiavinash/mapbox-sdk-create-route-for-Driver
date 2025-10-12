package com.solvay.UI.Notification

import android.content.Context
import android.os.Bundle
import android.view.ContextThemeWrapper
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.solvay.Base.BaseFragment
import com.solvay.Localization.LocaleHelper
import com.solvay.UI.Notification.adapter.NotificationAdapter
import com.solvay.UI.Notification.adapter.NotificationClickListener
import com.solvay.UI.Notification.viewModel.NotificationViewModel
import com.solvay.UI.chat.model.ChatListRequest
import com.solvay.UI.chat.model.ChatListResponse
import com.solvay.UI.chat.model.Pagination
import com.solvay.UI.chat.model.UserData
import com.solvay.R
import com.solvay.Utils.AlertManager
import com.solvay.Utils.EventObserver
import com.solvay.Utils.Prefs
import com.solvay.Utils.Utils.disableMultiTap
import com.solvay.databinding.FragmentNotificationsBinding


class NotificationsFragment : BaseFragment(), NotificationClickListener {

    private var _binding: FragmentNotificationsBinding? = null
    private val binding get() = _binding!!
    private var notificationList: ArrayList<UserData> = arrayListOf()
    private lateinit var notificationAdapter: NotificationAdapter
    private val viewModel by viewModels<NotificationViewModel>()
    private lateinit var alertManager: AlertManager
    private var paginationData: Pagination? = null
    private var currentPage = 1
    private var isLoading = false
    private var isLastPage = false
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val baseContext = LocaleHelper.setLocale(requireContext(), Prefs.init().appLanguageCode ?: "de")
        val themedContext = ContextThemeWrapper(baseContext, R.style.AppTheme)
        val localizedInflater = inflater.cloneInContext(themedContext)
        _binding = FragmentNotificationsBinding.inflate(localizedInflater, container, false)
        return binding.root
    }

        override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
            super.onViewCreated(view, savedInstanceState)
            alertManager = AlertManager(binding.root as ViewGroup, requireContext())
            setupRecyclerView()
            binding.llBack.setOnClickListener { findNavController().navigateUp() }
            binding.swipeRefresh.setOnRefreshListener {
                refreshList()
            }
            showNavBottom()
            getNotificationList(ChatListRequest(offset = 0, limit = 20))
            observer()
    }


    private fun setupRecyclerView() {
        notificationAdapter = NotificationAdapter(arrayListOf(), clickListener =  this@NotificationsFragment)
        binding.rvNotification.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = notificationAdapter

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
        notificationAdapter.setLoading(true)

        val nextPage = paginationData?.nextPage ?: currentPage + 1
        getNotificationList(ChatListRequest(offset = (nextPage - 1) * 20, limit = 20))
    }

    private fun getNotificationList(request: ChatListRequest){
        viewModel.notificationList(request)
        showProgress(true)
    }

    private fun observer(){
        viewModel.notificationListResponse.observe(viewLifecycleOwner, EventObserver{
            showProgress(false)
            notificationListObserver(it)
        })
    }

    private fun notificationListObserver(response: ChatListResponse){
        binding.swipeRefresh.isRefreshing = false
        if (response.success == true){
            response.data?.apply {
                notificationList = ArrayList(list.filter { it.self != true })
                paginationData = pagination
                if (!notificationList.isNullOrEmpty()) {
                    if (currentPage == 1) {
                        notificationAdapter.updateList(notificationList ?: arrayListOf())
                    } else {
                        notificationAdapter.addNewListItems(notificationList ?: arrayListOf())
                    }

                    pagination?.let {
                        currentPage = it.currentPage ?: currentPage
                        isLastPage = it.nextPage == null
                    }
                }
            }
        }else{
            alertManager.showError(response.message?:"")
        }
    }
    fun refreshList() {
        resetPagination()
        getNotificationList(ChatListRequest(offset = 0, limit = 20))
    }

    private fun resetPagination() {
        currentPage = 1
        isLastPage = false
        isLoading = false
        notificationList?.clear()
        notificationAdapter.updateList(notificationList ?: arrayListOf())
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onClickNotification(item: UserData, position: Int, view: View) {
        view.disableMultiTap()
        if (item.user != null){
            val bundle = Bundle()
            bundle.putString("name", item.user.name)
            bundle.putString("_id", item.user._id)
            bundle.putString("image", item.user.image)
            bundle.putBoolean("isOnline", item.user.isOnline)
            findNavController().navigate(R.id.chatFragment , bundle)
        }
    }

    override fun onAttach(context: Context) {
        val newContext = LocaleHelper.setLocale(context, Prefs.init().appLanguageCode ?: "en")
        super.onAttach(newContext)
    }

    private fun showProgress(visibility: Boolean){
        binding.includeProgress.clMain.visibility = if(visibility) View.VISIBLE else View.GONE
    }
}