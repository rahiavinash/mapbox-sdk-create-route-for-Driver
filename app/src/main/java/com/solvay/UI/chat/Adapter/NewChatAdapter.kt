package com.solvay.UI.chat.Adapter
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.solvay.databinding.LayoutNewChatItemBinding
import com.solvay.R

class NewChatAdapter(private val list: ArrayList<DummyUser>, private val clickListener: NewChatClickListener) : RecyclerView.Adapter<NewChatAdapter.NewChatViewHolder>() {

    inner class NewChatViewHolder(private val binding: LayoutNewChatItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(item: DummyUser, position: Int) {
            binding.apply {
                tvName.text = item.name?:""
                ivUserImage.setImageResource(item.image?:R.drawable.ic_greyuser)
                root.setOnClickListener {
                    clickListener.onClickUser(position , item , it)
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NewChatViewHolder {
        val binding = LayoutNewChatItemBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return NewChatViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return list.size ?: 0
    }

    override fun onBindViewHolder(holder: NewChatViewHolder, position: Int) {
        holder.bind(list[position], position)
    }

}

interface NewChatClickListener {
    fun onClickUser(position: Int, item: DummyUser , view: View)
}
data class DummyUser(
    var name: String? = null,
    var image: Int? = null
)

