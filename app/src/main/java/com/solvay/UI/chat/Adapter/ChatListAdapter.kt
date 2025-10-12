package com.solvay.UI.chat.Adapter

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.solvay.BuildConfig
import com.solvay.R
import com.solvay.UI.chat.model.Message
import com.solvay.Utils.Utils.loadImageAsync
import com.solvay.UI.chat.model.User
import com.solvay.UI.chat.model.UserData
import com.solvay.Utils.Utils.getTimeFromDateTime
import com.solvay.databinding.MessagelistRecycleviewBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ChatListAdapter(
    var list: ArrayList<UserData>,
    private val onClickListener: OnClickListener? = null
) : RecyclerView.Adapter<ChatListAdapter.ViewHolder>() {

    inner class ViewHolder(val view: MessagelistRecycleviewBinding, private val context: Context) : RecyclerView.ViewHolder(view.root) {
         fun bind(item: UserData, position: Int, size: Int){
             view.tvName.text = item.user.name
             view.tvTime.text =   getTimeFromDateTime( item.message.updatedAt)
             view.tvMessage.text = getMessage(item.message)
             view.clOpenChats.setBackgroundResource(if (item.unseenMessages > 0) R.drawable.notification_unseen_background else R.drawable.notification_seen_background)
             view.clOpenChats.setOnClickListener {
                 onClickListener?.onClickChat( position, item)
             }
             if(item.user.isOnline){
                 view.likeImg.visibility= View.VISIBLE
             }else{
                 view.likeImg.visibility= View.GONE
             }

             if (item.user.image.isNotEmpty()) {
                 CoroutineScope(Dispatchers.IO).launch {
                     if (size > 0) {
                         item.user.image.let {
                             view.ivUserProfileImage.loadImageAsync(
                                 BuildConfig.IMAGE_URL + it
                             )
                         }
                     }
                 }
             } else {
                 CoroutineScope(Dispatchers.Main).launch {
                     view.ivUserProfileImage.setImageResource(R.drawable.ic_greyuser)
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

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val mBinding = MessagelistRecycleviewBinding.inflate(LayoutInflater.from(parent.context))
        return ViewHolder(mBinding, parent.context)
    }

    override fun getItemCount(): Int {
        return list.size?:0
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getItemViewType(position: Int): Int {
        return position
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(list[position], position, itemCount)
    }

    fun updateList(newList: ArrayList<UserData>) {
        if (list != newList) {
            list.clear()
            list.addAll(newList)
            notifyDataSetChanged()
        }
    }


    fun addNewListItems(newList: ArrayList<UserData>) {
        val startPosition = list.size
        list.addAll(newList)
        notifyItemRangeInserted(startPosition, newList.size)
    }

    interface OnClickListener {
        fun onClickChat(position: Int, data: UserData)
        fun onCLickBlock(position: Int, data: UserData)
        fun onClickDelete(position: Int, data: UserData)
        fun onClickClear(position: Int, data: UserData)
        fun onClickPin(position: Int, data: UserData)
    }
}
