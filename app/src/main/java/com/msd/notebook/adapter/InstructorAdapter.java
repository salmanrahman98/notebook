package com.msd.notebook.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.msd.notebook.R;
import com.msd.notebook.databinding.InstructorItemBinding;
import com.msd.notebook.models.Instructor;

import java.util.ArrayList;
import java.util.List;

public class InstructorAdapter extends RecyclerView.Adapter<InstructorAdapter.ViewHolder> {

    Context context;
    List<Instructor> instructorList = new ArrayList<>();
    InstructorItemClick instructorItemClick;

    public InstructorAdapter(Context context, InstructorItemClick instructorItemClick) {
        this.context = context;
        this.instructorItemClick = instructorItemClick;
    }

    public void setInstructorList(List<Instructor> instructorList) {
        this.instructorList = instructorList;
        notifyDataSetChanged();
    }


    @NonNull
    @Override
    public InstructorAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.instructor_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull InstructorAdapter.ViewHolder holder, int position) {
        holder.binding.instructorNameTxt.setText(instructorList.get(position).getInstructorName());
        holder.binding.instructorId.setText(instructorList.get(position).getId());

        holder.binding.itemRoot.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                instructorItemClick.itemClick(instructorList.get(holder.getAdapterPosition()));
            }
        });

        holder.binding.deleteCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                instructorItemClick.itemCardClick(instructorList.get(holder.getAdapterPosition()));
            }
        });
    }

    @Override
    public int getItemCount() {
        return instructorList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        InstructorItemBinding binding;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            binding = InstructorItemBinding.bind(itemView);
        }
    }

    public interface InstructorItemClick {
        void itemClick(Instructor instructor);
        void itemCardClick(Instructor instructor);
    }
}
