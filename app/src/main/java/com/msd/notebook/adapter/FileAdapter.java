package com.msd.notebook.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.msd.notebook.R;
import com.msd.notebook.databinding.FileItemBinding;
import com.msd.notebook.models.InstructorFiles;

import java.util.ArrayList;
import java.util.List;

public class FileAdapter extends RecyclerView.Adapter<FileAdapter.ViewHolder> {

    Context context;
    List<InstructorFiles> files = new ArrayList<>();
    boolean signedInAsInstructor = true;
    FileBtnClick fileBtnClick;

    public FileAdapter(Context context, boolean signedInAsInstructor, FileBtnClick fileBtnClick) {
        this.context = context;
        this.signedInAsInstructor = signedInAsInstructor;
        this.fileBtnClick = fileBtnClick;
    }

    public void setFiles(List<InstructorFiles> files) {
        this.files = files;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public FileAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.file_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FileAdapter.ViewHolder holder, int position) {

        holder.binding.fileNameTxt.setText(files.get(position).getFileName());
        holder.binding.fileUrlTxt.setText(files.get(position).getFileUrl());

        if (!signedInAsInstructor) {
            holder.binding.cardText.setText("Download");
        }

        holder.binding.deleteCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fileBtnClick.btnClick(files.get(holder.getAdapterPosition()));
            }
        });

    }

    @Override
    public int getItemCount() {
        return files.size();
    }

    public interface FileBtnClick {
        void btnClick(InstructorFiles file);
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        FileItemBinding binding;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            binding = FileItemBinding.bind(itemView);
        }
    }
}
