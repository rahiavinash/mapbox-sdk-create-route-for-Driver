package com.solvay.adapters

import android.content.Context
import android.graphics.PorterDuff
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import com.solvay.R
import androidx.recyclerview.widget.RecyclerView
import com.solvay.authentication.models.Location
import com.solvay.databinding.LayoutStopItemBinding

class StopsAdapter(private val ctx: Context, private val list: ArrayList<Location>) :
    RecyclerView.Adapter<StopsAdapter.StopsViewHolder>() {
    private var isExpanded: Boolean = false

    inner class StopsViewHolder(private val binding: LayoutStopItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(item: Location, position: Int) {
            updateViewVisibility(position)
            updateViewColor(item, position)
            Log.e("sdjkfsdlf", item.name.toString())
            Log.e("sdjkfsdlf", item.toString())
            binding.tvStart.text = item.name?:""
        }

        fun updateViewVisibility(position: Int){
            when(position){
                0 -> {
                    binding.trackLineUpper.visibility = View.INVISIBLE
                    binding.trackLineLower.visibility = View.VISIBLE
                    Log.d("LocationTag","Position: $position")
                }
                (itemCount - 1) -> {
                    binding.trackLineUpper.visibility = View.VISIBLE
                    binding.trackLineLower.visibility = View.INVISIBLE
                    Log.d("LocationTag","Position: $position")
                }
                else -> {
                    binding.trackLineUpper.visibility = View.VISIBLE
                    binding.trackLineLower.visibility = View.VISIBLE
                }
            }
        }

        fun updateViewColor(item: Location, position: Int) {
            val currentLocationIndex = getCurrentLocationPosition()
            val lastIndex = itemCount - 1
            if (isExpanded){
                when {
                    position <= currentLocationIndex -> {
                        binding.apply {
                            ivLocationPointer.setImageResource(R.drawable.location_pointer_blue)
                            trackLineUpper.setImageResource(R.drawable.track_line)
                            trackLineUpper.setColorFilter(ContextCompat.getColor(ctx, R.color.black), PorterDuff.Mode.SRC_IN)

                            trackLineLower.setImageResource(R.drawable.track_line)
                            trackLineLower.setColorFilter(ContextCompat.getColor(ctx, R.color.black), PorterDuff.Mode.SRC_IN)

                            tvStart.setBackgroundResource(R.drawable.active_location_background)
                        }
                    }
                    position == currentLocationIndex + 1 && position != lastIndex -> {
                        binding.apply {
                            ivLocationPointer.setImageResource(R.drawable.location_pointer_yellow)
                            trackLineUpper.setImageResource(R.drawable.track_line)
                            trackLineUpper.setColorFilter(ContextCompat.getColor(ctx, R.color.black), PorterDuff.Mode.SRC_IN)

                            trackLineLower.setImageResource(R.drawable.continuous_track_line)
                            tvStart.setBackgroundResource(R.drawable.inactive_location_background)
                        }
                    }
                    position > currentLocationIndex + 1 && position != lastIndex -> {
                        binding.apply {
                            ivLocationPointer.setImageResource(R.drawable.location_pointer)
                            trackLineUpper.setImageResource(R.drawable.continuous_track_line)
                            trackLineLower.setImageResource(R.drawable.continuous_track_line)
                            tvStart.setBackgroundResource(R.drawable.inactive_location_background)
                        }
                    }
                    position == lastIndex -> {
                        binding.apply {
                            ivLocationPointer.setImageResource(R.drawable.location_pointer_red)
                            trackLineUpper.setImageResource(R.drawable.continuous_track_line)
                            tvStart.setBackgroundResource(R.drawable.inactive_location_background)
                        }
                    }
                }
            }else{
                when(position){
                    0 -> {
                        binding.apply {
                            ivLocationPointer.setImageResource(R.drawable.location_pointer_blue)
                            trackLineLower.setImageResource(R.drawable.track_line)
                            tvStart.setBackgroundResource(R.drawable.active_location_background)
                        }
                    }
                    1 -> {
                        binding.apply {
                            ivLocationPointer.setImageResource(if (item.isLastLocation == true) R.drawable.location_pointer_red else R.drawable.location_pointer_yellow)
                            trackLineUpper.setImageResource(R.drawable.track_line)
                            tvStart.setBackgroundResource(R.drawable.inactive_location_background)
                        }
                    }
                }
            }

        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StopsViewHolder {
        val binding = LayoutStopItemBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return StopsViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return list.size ?: 0
    }

    override fun onBindViewHolder(holder: StopsViewHolder, position: Int) {
        holder.bind(list[position], position)
    }

    fun updateList(newList: ArrayList<Location>) {
        if (list != newList) {
            list.clear()
            list.addAll(newList)
            notifyDataSetChanged()
        }
    }

    fun nextStop(position: Int): Boolean {
        if (position in 0 until itemCount - 1) {
            list.forEach { it.isCurrentLocation = false }
            list[position + 1].isCurrentLocation = true
            notifyDataSetChanged()
            return true
        }
        return false
    }

    fun moveBack(position: Int): Boolean {
        if (position in 1 until itemCount) {
            list.forEach { it.isCurrentLocation = false }
            list[position - 1].isCurrentLocation = true
            notifyDataSetChanged()
            return true
        }
        return false
    }

    private fun getCurrentLocationPosition(): Int {
        return list.indexOfFirst { it.isCurrentLocation == true }
    }

    fun setExpandedState(value: Boolean){
        isExpanded = value
    }

    fun getExpandedState(): Boolean{
        return isExpanded
    }
}

