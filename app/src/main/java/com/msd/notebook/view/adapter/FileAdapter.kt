package com.msd.notebook.view.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.msd.notebook.R
import com.msd.notebook.databinding.FileItemBinding
import com.msd.notebook.models.InstructorFiles

class FileAdapter(var context: Context, signedInAsInstructor: Boolean, fileBtnClick: FileBtnClick) :
    RecyclerView.Adapter<FileAdapter.ViewHolder>() {
    var files: ArrayList<InstructorFiles>? = (java.util.ArrayList());
    var signedInAsInstructor = true
    var fileBtnClick: FileBtnClick

    init {
        this.signedInAsInstructor = signedInAsInstructor
        this.fileBtnClick = fileBtnClick
    }

    fun updateFiles(files: ArrayList<InstructorFiles>) {
        this.files = files
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.file_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.binding.fileNameTxt.text = files!![position].fileName
        holder.binding.fileUrlTxt.text = files!![position].fileUrl
        if (!signedInAsInstructor) {
            holder.binding.cardText.text = "Download"
        }
        holder.binding.deleteCard.setOnClickListener {
            fileBtnClick.btnClick(
                files!![holder.getAdapterPosition()]
            )
        }
    }

    override fun getItemCount(): Int {
        return files!!.size
    }

    interface FileBtnClick {
        fun btnClick(file: InstructorFiles?)
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var binding: FileItemBinding

        init {
            binding = FileItemBinding.bind(itemView)
        }
    }
}
