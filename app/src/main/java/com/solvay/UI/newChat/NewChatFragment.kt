package com.solvay.UI.newChat

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
import com.solvay.R
import com.solvay.UI.chat.ChatViewModel.ChatViewModels
import com.solvay.UI.newChat.dataModel.Admin
import com.solvay.UI.newChat.dataModel.AdminListRequest
import com.solvay.UI.newChat.dataModel.AdminListResponse
import com.solvay.UI.newChat.dataModel.Pagination
import com.solvay.UI.newChat.viewModel.AdminListViewModel
import com.solvay.Utils.AlertManager
import com.solvay.Utils.EventObserver
import com.solvay.Utils.Prefs
import com.solvay.adapters.NewChatAdapter
import com.solvay.adapters.NewChatClickListener
import com.solvay.databinding.FragmentChatBinding
import com.solvay.databinding.FragmentNewChatBinding


class NewChatFragment : BaseFragment(), NewChatClickListener {
   private lateinit var mBinding :FragmentNewChatBinding
    private val viewModel by viewModels<AdminListViewModel>()
    private lateinit var alertManager: AlertManager
    private var paginationData: Pagination? = null
    private var adminList: ArrayList<Admin>? = null
    private var currentPage = 1
    private var isLoading = false
    private var isLastPage = false
    private var bundle =Bundle()
    private lateinit var newChatAdapter: NewChatAdapter
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val baseContext = LocaleHelper.setLocale(requireContext(), Prefs.init().appLanguageCode ?: "en")
        val themedContext = ContextThemeWrapper(baseContext, R.style.AppTheme)
        val localizedInflater = inflater.cloneInContext(themedContext)
        mBinding = FragmentNewChatBinding.inflate(localizedInflater , container , false)
        showNavBottom()
        return mBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        alertManager = AlertManager(mBinding.root as ViewGroup, requireContext())
        if (::mBinding.isInitialized) setupRecyclerView() else mBinding = FragmentNewChatBinding.inflate(layoutInflater)
        mBinding.llBack.setOnClickListener { findNavController().navigateUp() }
        mBinding.swipeRefresh.setOnRefreshListener {
            refreshList()
        }
        hideNavBottom()
        getList(AdminListRequest(offset = 0, limit = 20))
        observer()
    }

    override fun onClickUser(position: Int, item: Admin, view: View) {


        bundle.putString("name", item.name)
        bundle.putString("_id", item._id)
        bundle.putString("image", item.image)
        item.isOnline?.let { bundle.putBoolean("isOnline", it) }
        findNavController().navigate(R.id.chatFragment , bundle)
    }

    private fun setupRecyclerView() {
        newChatAdapter = NewChatAdapter(requireContext(),adminList?: arrayListOf(), this@NewChatFragment)
        mBinding.rvNewChat.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = newChatAdapter

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
        newChatAdapter.setLoading(true)

        val nextPage = paginationData?.nextPage ?: currentPage + 1
        getList(AdminListRequest(offset = (nextPage - 1) * 20, limit = 20))
    }
    private fun getList(request: AdminListRequest){
        viewModel.adminList(request)
        showProgress(true)
    }

    fun refreshList() {
        resetPagination()
        getList(AdminListRequest(offset = 0, limit = 20))
    }

    fun observer(){
        viewModel.adminListResponse.observe(viewLifecycleOwner, EventObserver{
            adminListObserver(it)
        })
    }

    private fun adminListObserver(response: AdminListResponse){
        mBinding.swipeRefresh.isRefreshing = false
        showProgress(false)
        if (response.success == true){
            response.data?.apply {
                adminList = list
                paginationData = pagination
                if (!adminList.isNullOrEmpty()) {
                    if (currentPage == 1) {
                        newChatAdapter.updateList(adminList ?: arrayListOf())
                    } else {
                        newChatAdapter.addNewListItems(adminList ?: arrayListOf())
                    }

                    pagination?.let {
                        currentPage = it.currentPage ?: currentPage
                        isLastPage = it.nextPage == null
                    }
                }
            }
        }else{
            val baseContext = LocaleHelper.setLocale(requireContext(), Prefs.init().appLanguageCode ?: "en")
            alertManager.showError(response.message?:baseContext.getString(R.string.something_went_wrong))
        }

        isLoading = false
        newChatAdapter.setLoading(false)
    }

    private fun resetPagination() {
        currentPage = 1
        isLastPage = false
        isLoading = false
        adminList?.clear()
        newChatAdapter.updateList(adminList ?: arrayListOf())
    }

    override fun onAttach(context: Context) {
        val newContext = LocaleHelper.setLocale(context, Prefs.init().appLanguageCode ?: "en")
        super.onAttach(newContext)
    }

    private fun showProgress(visibility: Boolean){
        mBinding.includeProgress.clMain.visibility = if(visibility) View.VISIBLE else View.GONE
    }
}