package com.msd.notebook.view.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.msd.notebook.R
import com.msd.notebook.databinding.InstructorItemBinding
import com.msd.notebook.models.Instructor

class InstructorAdapter(var context: Context, var instructorItemClick: InstructorItemClick) :
    RecyclerView.Adapter<InstructorAdapter.ViewHolder>() {

    var instructorList : ArrayList<Instructor>? = ArrayList()

    fun updateInstructorList(instructorList: ArrayList<Instructor>) {
        this.instructorList = instructorList
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.instructor_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.binding.headerTitle.text = instructorList?.get(position)!!.instructor_name
//        holder.binding.instructorId.text = instructorList!![position].instructor_id
        holder.binding.itemRoot.setOnClickListener {
            instructorItemClick.itemClick(
                instructorList!![holder.getAdapterPosition()]
            )
        }
        /*holder.binding.deleteCard.setOnClickListener {
            instructorItemClick.itemCardClick(
                instructorList!![holder.getAdapterPosition()]
            )
        }*/
    }

    override fun getItemCount(): Int {
        return instructorList!!.size
    }

    interface InstructorItemClick {
        fun itemClick(instructor: Instructor?)
        fun itemCardClick(instructor: Instructor?)
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var binding: InstructorItemBinding

        init {
            binding = InstructorItemBinding.bind(itemView)
        }
    }
}
