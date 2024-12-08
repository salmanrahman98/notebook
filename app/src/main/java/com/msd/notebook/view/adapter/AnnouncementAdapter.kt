package com.msd.notebook.view.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.msd.notebook.R
import com.msd.notebook.common.Config
import com.msd.notebook.databinding.AnnouncementItemBinding
import com.msd.notebook.models.Announcement

class AnnouncementAdapter(onItemClickListener: OnItemClickListener) :
    RecyclerView.Adapter<AnnouncementAdapter.ViewHolder>() {

    var announcements: ArrayList<Announcement>? = ArrayList()
    var onItemClickListener: OnItemClickListener? = onItemClickListener

    fun updateAnnouncements(announcements: ArrayList<Announcement>) {
        this.announcements = announcements
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): AnnouncementAdapter.ViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.announcement_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: AnnouncementAdapter.ViewHolder, position: Int) {

        holder.binding.headerTitle.text = announcements!![position].header
        holder.binding.dateTitle.text =
            Config.getFormattedDateFromTimestamp(announcements!![position].date)
        holder.binding.descrtiptionTxtView.text = announcements!![position].description

        holder.binding.root.setOnClickListener(View.OnClickListener {
            onItemClickListener?.onItemClick(position)
        })
    }

    override fun getItemCount(): Int {
        return announcements!!.size
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var binding: AnnouncementItemBinding

        init {
            binding = AnnouncementItemBinding.bind(itemView)
        }
    }

    public interface OnItemClickListener {
        fun onItemClick(position: Int)
    }
}