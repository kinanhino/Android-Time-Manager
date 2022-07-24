package com.example.timemanager;

import java.io.Serializable;

public class Task implements Serializable {
    String name,description,date;
    boolean completed;

    public Task(String name, String description, String date) {
        this.name = name;
        this.description = description;
        this.date = date;
        this.completed = false;
    }

    public Task(String name, String description, String date, boolean completed) {
        this.name = name;
        this.description = description;
        this.date = date;
        this.completed = completed;
    }



    public boolean isCompleted() {
        return completed;
    }

    public void setCompleted(boolean completed) {
        this.completed = completed;
    }
    
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return "Task{" +
                "name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", date=" + date +
                '}';
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }
}
