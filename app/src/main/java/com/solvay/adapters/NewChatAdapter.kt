package com.solvay.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.solvay.BuildConfig
import com.solvay.databinding.LayoutNewChatItemBinding
import com.solvay.R
import com.solvay.UI.newChat.dataModel.Admin
import com.solvay.authentication.models.Location
import com.solvay.databinding.LayoutLoadingItemBinding

class NewChatAdapter(private val context: Context,private val list: ArrayList<Admin>, private val clickListener: NewChatClickListener, private val loadMoreListener: (() -> Unit)? = null) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        private const val VIEW_TYPE_ITEM = 0
        private const val VIEW_TYPE_LOADING = 1
    }
    private var isLoading = false
    inner class NewChatViewHolder(private val binding: LayoutNewChatItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(item: Admin, position: Int) {
            binding.apply {
                tvName.text = item.name?:""
                Glide.with(context)
                    .load(BuildConfig.IMAGE_URL + (item.image?:""))
                    .error(R.drawable.ic_greyuser)
                    .into(ivUserImage)
                root.setOnClickListener {
                    clickListener.onClickUser(position, item, it)
                }
            }
        }
    }

    inner class LoadingViewHolder(private val binding: LayoutLoadingItemBinding) : RecyclerView.ViewHolder(binding.root) {

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            VIEW_TYPE_ITEM -> {
                val binding = LayoutNewChatItemBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
                NewChatViewHolder(binding)
            }
            else -> {
                val binding = LayoutLoadingItemBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
                LoadingViewHolder(binding)
            }
        }
    }

    override fun getItemCount(): Int {
        return list.size ?: 0
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is NewChatViewHolder -> holder.bind(list[position], position)
        }

        if (position == list.size - 5 && !isLoading && loadMoreListener != null) {
            loadMoreListener.invoke()
        }
    }

    override fun getItemViewType(position: Int): Int {
        return if (position == list.size && isLoading) VIEW_TYPE_LOADING else VIEW_TYPE_ITEM
    }
    fun updateList(newList: ArrayList<Admin>) {
        if (list != newList) {
            list.clear()
            list.addAll(newList)
            notifyDataSetChanged()
        }
    }


    fun addNewListItems(newList: ArrayList<Admin>) {
        val startPosition = list.size
        list.addAll(newList)
        notifyItemRangeInserted(startPosition, newList.size)
    }

    fun setLoading(loading: Boolean) {
        isLoading = loading
        if (loading) {
            notifyItemInserted(list.size)
        } else {
            notifyItemRemoved(list.size)
        }
    }
}

interface NewChatClickListener {
    fun onClickUser(position: Int, item: Admin, view: View)
}