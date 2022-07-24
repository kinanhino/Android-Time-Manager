package com.example.timemanager;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.Observer;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

public class myAdapter extends RecyclerView.Adapter<myAdapter.myViewHolder> {

    ArrayList<Task> allTasks,tasks;
    HashMap<Integer,Integer> task2AllTasksMap ;
    AppCompatActivity mainActivity;
    int selectedPos ;
    View selectedItemView;
    myViewModel model;
    boolean completed;


    public myAdapter(AppCompatActivity mainActivity,myViewModel model,boolean completed){
        this.mainActivity = mainActivity;
        selectedPos = RecyclerView.NO_POSITION;
        this.model = model;
        this.completed = completed;
        task2AllTasksMap = new HashMap<>();
        this.model.getTasksLiveData().observe((LifecycleOwner) mainActivity,getAllTasks);
        tasks = new ArrayList<>();
    }


    Observer<ArrayList<Task>> getAllTasks = new Observer<ArrayList<Task>>() {
        @Override
        public void onChanged(ArrayList<Task> userArrayList) {
            allTasks = userArrayList;
            tasks.clear();
            task2AllTasksMap.clear();
            int localIndex = 0;
            for (int globalIndex=0 ; globalIndex< allTasks.size() ;globalIndex++)
            {
                Task t = allTasks.get(globalIndex);
                if (t.isCompleted() == completed)
                {
                    tasks.add(t);
                    task2AllTasksMap.put(localIndex,globalIndex);
                    localIndex++;
                }
            }
            notifyDataSetChanged();
        }
    };

    @NonNull
    @Override
    public myViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View mView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_layout, parent, false);
            myViewHolder vh = new myViewHolder(mView);
            return vh;
    }

    @Override
    public void onBindViewHolder(@NonNull myViewHolder holder, int position) {
        holder.bindData(position);
        if (selectedPos != RecyclerView.NO_POSITION && selectedPos == position) {
            holder.itemView.setBackgroundColor(Color.CYAN);
            selectedItemView = holder.itemView;
        }
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selectedPos = holder.getAdapterPosition();
                if (selectedItemView != null)
                    selectedItemView.setBackgroundColor(Color.WHITE);
                holder.view.setBackgroundColor(Color.CYAN);
                selectedItemView = view;
                model.select(task2AllTasksMap.get(selectedPos));
            }
        });
    }

    public boolean isSelctedNull()
    {
        return (selectedPos == RecyclerView.NO_POSITION);
    }

    public int getSelectedPos(){return selectedPos;}
    public void setSelectedPos(int pos){ selectedPos = pos; }

    public Task getTask()
    {
        return tasks.get(selectedPos);
    }
    
    public void addTask(Task result) {
        model.addTask(result);
        AlarmForeGroundService.interruptThread();
    }

    public void editTask(Task result) {
        model.editTask(result,task2AllTasksMap.get(selectedPos));
        updateRV();
        AlarmForeGroundService.interruptThread();
    }

    public void removeTask() {
        model.removeTask(task2AllTasksMap.get(selectedPos));
        updateRV();
    }

    public void completeTask() {
        model.completeTask(task2AllTasksMap.get(selectedPos));
        updateRV();
    }

    private void updateRV() {
        selectedPos = RecyclerView.NO_POSITION;
        selectedItemView.setBackgroundColor(Color.WHITE);
        selectedItemView = null;
    }


    @Override
    public int getItemCount() {
        return tasks.size();
    }

    class myViewHolder extends RecyclerView.ViewHolder{

        View view;
        TextView tvName,tvDescription,tvDate;

        public myViewHolder(@NonNull View itemView) {
            super(itemView);
            view = itemView;
            tvName = view.findViewById(R.id.tvName);
            tvDescription = view.findViewById(R.id.tvDescription);
            tvDate = view.findViewById(R.id.tvDate);
        }

        public void bindData(int position)
        {
            Task task = tasks.get(position);
            tvName.setText(task.getName());
            tvDescription.setText(task.getDescription());
            tvDate.setText(task.getDate().toString());
        }

    }
}
