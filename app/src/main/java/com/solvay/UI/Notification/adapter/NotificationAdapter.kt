package com.solvay.UI.Notification.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.runtime.produceState
import com.solvay.R
import androidx.recyclerview.widget.RecyclerView
import com.solvay.UI.chat.model.Message
import com.solvay.UI.chat.model.UserData
import com.solvay.Utils.Utils.getDateTime
import com.solvay.Utils.Utils.getTimeFromDateTime
import com.solvay.databinding.LayoutLoadingItemBinding
import com.solvay.databinding.LayoutNotificationItemBinding


class NotificationAdapter(
    val dataList: ArrayList<UserData>,
    private val loadMoreListener: (() -> Unit)? = null,
    private val clickListener: NotificationClickListener
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    companion object {
        private const val VIEW_TYPE_ITEM = 0
        private const val VIEW_TYPE_LOADING = 1
    }

    private var isLoading = false
    var context: Context? = null

    inner class MyViewHolder(private val binding: LayoutNotificationItemBinding, private val context: Context) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(item: UserData, position: Int) {
            binding.apply {
                clRoot.setBackgroundResource(if (item.unseenMessages == 1) R.drawable.message_background else R.drawable.notification_seen_background)
                imgIcon.setImageResource(R.drawable.img_msg)
                tvTitle.text = item.user.name
                tvDescription.text = getMessage(item.message)
                txtTime.text = getDateTime(item.updatedAt)
                root.setOnClickListener {
                    clickListener.onClickNotification(item, position, it)
                }
            }
        }

        private fun getMessage(message: Message): String {
            return when(message.type){
                "image" -> {
                    context.getString(R.string.image)
                }

                "video" -> {
                    context.getString(R.string.video)
                }

                "audio" -> {
                    context.getString(R.string.audio)
                }

                "document" -> {
                    context.getString(R.string.document)
                }

                else -> {
                    message.text?:""
                }
            }
        }

    }

    inner class LoadingViewHolder(private val binding: LayoutLoadingItemBinding) :
        RecyclerView.ViewHolder(binding.root) {

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {

        context = parent.context
        return when (viewType) {
            VIEW_TYPE_ITEM -> {
                val binding = LayoutNotificationItemBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
                MyViewHolder(binding, parent.context)
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
        return dataList.size
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is MyViewHolder -> holder.bind(dataList[position], position)
        }

        if (position == dataList.size - 5 && !isLoading && loadMoreListener != null) {
            loadMoreListener.invoke()
        }
    }

    override fun getItemViewType(position: Int): Int {
        return if (position == dataList.size && isLoading) VIEW_TYPE_LOADING else VIEW_TYPE_ITEM
    }

    fun updateList(newList: ArrayList<UserData>) {
        if (dataList != newList) {
            dataList.clear()
            dataList.addAll(newList)
            notifyDataSetChanged()
        }
    }


    fun addNewListItems(newList: ArrayList<UserData>) {
        val startPosition = dataList.size
        dataList.addAll(newList)
        notifyItemRangeInserted(startPosition, newList.size)
    }

    fun setLoading(loading: Boolean) {
        isLoading = loading
        if (loading) {
            notifyItemInserted(dataList.size)
        } else {
            notifyItemRemoved(dataList.size)
        }
    }
}

interface NotificationClickListener {
    fun onClickNotification(item: UserData, position: Int, view: View)
}
