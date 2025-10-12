package com.solvay.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.solvay.authentication.models.LanguageDataModel
import com.solvay.databinding.LayoutLanguageSelectionBinding

class LanguageRecyclerAdapter (private val list: ArrayList<LanguageDataModel>, private val clickListener: ClickListener) :
    RecyclerView.Adapter<LanguageRecyclerAdapter.LanguageViewHolder>(){
        inner class LanguageViewHolder(private val binding: LayoutLanguageSelectionBinding): RecyclerView.ViewHolder(binding.root){
            fun bind(item: LanguageDataModel, position: Int) {
                binding.tvLanguage.text = item.name
                binding.viewWhiteLine.visibility = if (position == itemCount - 1) View.GONE else View.VISIBLE
                binding.root.setOnClickListener{
                    clickListener.selectLanguage(it, item)
                }
            }
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LanguageViewHolder {
        val binding = LayoutLanguageSelectionBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return LanguageViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return list.size ?: 0
    }

    override fun onBindViewHolder(holder: LanguageViewHolder, position: Int) {
        holder.bind(list[position], position)
    }
}

interface ClickListener {
    fun selectLanguage(view: View, language: LanguageDataModel)
}