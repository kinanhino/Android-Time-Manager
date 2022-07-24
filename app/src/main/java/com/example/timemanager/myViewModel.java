package com.example.timemanager;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.RecyclerView;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.GregorianCalendar;

public class myViewModel extends AndroidViewModel  {

        private MutableLiveData<ArrayList<Task>> TasksLiveData;
        private MutableLiveData<Integer> itemSelected;
        private Integer selectedTask;
        private String tasksFileName = "tasks";
        private ArrayList<Task> tasksArrayList;
        public boolean remember_completed_tasks ;
        public myViewModel(@NonNull Application application) {
            super(application);

            if (!fileExists(tasksFileName))
            {
                try (FileOutputStream fos = getApplication().getApplicationContext().openFileOutput(tasksFileName, getApplication().getApplicationContext().MODE_PRIVATE)) {
                    fos.write("".getBytes());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            SharedPreferences app_preferences = PreferenceManager.getDefaultSharedPreferences(application);
            remember_completed_tasks = app_preferences.getBoolean("rememberCompletedCheckBox",false);
            TasksLiveData = new MutableLiveData<>();
            itemSelected = new MutableLiveData<>();
            tasksArrayList = getTasksArrayList();
            if (!remember_completed_tasks)
                removeAllCompletedTasks();
            TasksLiveData.setValue(tasksArrayList);
            selectedTask = RecyclerView.NO_POSITION;
            itemSelected.setValue(selectedTask);
        }


    private void removeAllCompletedTasks() {
        StringBuilder sb = new StringBuilder();
        ArrayList<Task> res = new ArrayList<>();
        for (Task task : tasksArrayList) {
            if(!task.isCompleted()) {
                sb.append("<task>" + "\n");
                sb.append("<name>" + task.getName() + "\n");
                sb.append("<desc>" + task.getDescription() + "\n");
                sb.append("<date>" + task.getDate() + "\n");
                res.add(task);
            }
        }
        try (FileOutputStream fos = getApplication().getApplicationContext().openFileOutput(tasksFileName, getApplication().getApplicationContext().MODE_PRIVATE)) {
            fos.write(sb.toString().getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }
        tasksArrayList = res;
    }

    public MutableLiveData<ArrayList<Task>> getTasksLiveData(){
            return TasksLiveData;
        }

    public ArrayList<Task> getAllTasks()
    {
        return tasksArrayList;
    }
        public void select(int index){
            selectedTask = index;
            itemSelected.setValue(selectedTask);
        }

        public void removeTask(int index)
        {
            tasksArrayList.remove(index);
            TasksLiveData.setValue(tasksArrayList);
            select(RecyclerView.NO_POSITION);
            overWriteRawFile();
        }

        public void completeTask(int index)
        {
            Task t = tasksArrayList.get(index);
            t.setCompleted(true);
            editTask(t, index);
        }

        public void addTask(Task task)
        {
            tasksArrayList.add(task);
            TasksLiveData.setValue(tasksArrayList);
            addTaskToRawFile(task);
        }

        public void editTask(Task task,int pos)
        {
            tasksArrayList.set(pos,task);
            TasksLiveData.setValue(tasksArrayList);
            overWriteRawFile();
        }


        private void overWriteRawFile() {
            StringBuilder sb = new StringBuilder();
            for (Task task : tasksArrayList) {
                sb.append("<task>" + (task.isCompleted() ? " - completed" : "") + "\n");
                sb.append("<name>" + task.getName() + "\n");
                sb.append("<desc>" + task.getDescription() + "\n");
                sb.append("<date>" + task.getDate() + "\n");
            }
            try (FileOutputStream fos = getApplication().getApplicationContext().openFileOutput(tasksFileName, getApplication().getApplicationContext().MODE_PRIVATE)) {
                fos.write(sb.toString().getBytes());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }


    private void addTaskToRawFile(Task task)
        {
            StringBuilder sb = new StringBuilder();
            sb.append("<task>"+"\n");
            sb.append("<name>"+task.getName()+"\n");
            sb.append("<desc>"+task.getDescription()+"\n");
            sb.append("<date>"+task.getDate()+ "\n");
            try (FileOutputStream fos = getApplication().getApplicationContext().openFileOutput(tasksFileName, getApplication().getApplicationContext().MODE_APPEND)) {
                fos.write(sb.toString().getBytes());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        public ArrayList<Task> getTasksArrayList()
        {
            ArrayList<Task> result = new ArrayList<>();
            FileInputStream fis = null;
            boolean readingTask = false, completed =false;
            String name="",desc="",date="";
            try {
                fis = getApplication().getApplicationContext().openFileInput(tasksFileName);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
            InputStreamReader inputStreamReader =
                    new InputStreamReader(fis, StandardCharsets.UTF_8);
            try (BufferedReader reader = new BufferedReader(inputStreamReader)) {
                String line = reader.readLine();
                while (line != null) {
                    if(line.startsWith("<task>")) {
                        if(line.length() > 6)
                            completed = true;
                        else
                            completed = false;
                        readingTask = true;
                    }
                    if(readingTask)
                    {
                        name = reader.readLine().split("<name>")[1];
                        line = reader.readLine();
                        while(!line.startsWith("<desc>"))
                        {
                            name += "\n" + line;
                            line = reader.readLine();
                        }
                        if (line.length() > 6) {
                            desc = line.split("<desc>")[1];
                            line = reader.readLine();
                            while (!line.startsWith("<date>")) {
                                desc += "\n" + line;
                                line = reader.readLine();
                            }
                        }
                        else
                        {
                            line = reader.readLine();
                            desc = "";
                        }
                        date = line.split("<date>")[1];
                        result.add(new Task(name,desc,date,completed));
                        readingTask = false;
                    }
                    line = reader.readLine();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            return result;
        }

    public boolean fileExists(String filename) {
        File file = getApplication().getApplicationContext().getFileStreamPath(filename);
        if(file == null || !file.exists())
            return false;
        return true;
    }



}

