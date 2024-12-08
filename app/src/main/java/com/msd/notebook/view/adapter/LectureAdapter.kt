package com.msd.notebook.view.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.msd.notebook.R
import com.msd.notebook.common.Config
import com.msd.notebook.databinding.AnnouncementItemBinding
import com.msd.notebook.models.Lecture

class LectureAdapter(context: Context, onItemClickListener: OnItemClickListener) :
    RecyclerView.Adapter<LectureAdapter.ViewHolder>() {

    var lectures: ArrayList<Lecture>? = ArrayList()
    var onItemClickListener: OnItemClickListener? = onItemClickListener
    var context: Context = context

    fun updateLectures(lectures: ArrayList<Lecture>) {
        this.lectures = lectures
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): LectureAdapter.ViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.announcement_item, parent, false)
        return ViewHolder(view)
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    override fun onBindViewHolder(holder: LectureAdapter.ViewHolder, position: Int) {

        holder.binding.headerTitle.text = lectures!![position].header
        holder.binding.dateTitle.text =
            Config.getFormattedDateFromTimestamp(lectures!![position].date)
        holder.binding.descrtiptionTxtView.text = lectures!![position].description

        holder.binding.imgView.setImageDrawable(context.getDrawable(R.drawable.lecture_svg))


        holder.binding.root.setOnClickListener(View.OnClickListener {
            onItemClickListener?.onItemClick(position)
        })
    }

    override fun getItemCount(): Int {
        return lectures!!.size
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