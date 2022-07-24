package com.example.timemanager;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TimePicker;
import android.widget.Toast;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;

public class AddEditTaskActivity extends AppCompatActivity {

    private String dateFormat = "HH:mm dd/MM/yyyy";
    private String name,desc,date;
    private EditText nameET,descET;
    private int ADD_TASK=10,EDIT_TASK=20;
    private Task selectedTask;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_edit_task);
        nameET = (EditText)findViewById(R.id.nameET);
        descET = (EditText)findViewById(R.id.descriptionET);
        int request = getIntent().getExtras().getInt("requestCode");
        if (request == ADD_TASK)
        {
            findViewById(R.id.addTaskButton).setVisibility(View.VISIBLE);
            findViewById(R.id.editTaskButton).setVisibility(View.INVISIBLE);
        }
        else if (request == EDIT_TASK)
        {
            findViewById(R.id.addTaskButton).setVisibility(View.INVISIBLE);
            findViewById(R.id.editTaskButton).setVisibility(View.VISIBLE);
            selectedTask = (Task) getIntent().getExtras().get("task");
            nameET.setText(selectedTask.getName());
            descET.setText(selectedTask.getDescription());
        }
    }


    public void chooseDate(View view)
    {
        final View dialogView = View.inflate(this, R.layout.date_time_picker, null);
        final AlertDialog alertDialog = new AlertDialog.Builder(this).create();

        dialogView.findViewById(R.id.date_time_set).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                DatePicker datePicker = (DatePicker) dialogView.findViewById(R.id.date_picker);
                TimePicker timePicker = (TimePicker) dialogView.findViewById(R.id.time_picker);

                datePicker.setMinDate(System.currentTimeMillis());

                Calendar calendar = new GregorianCalendar(datePicker.getYear(),
                        datePicker.getMonth(),
                        datePicker.getDayOfMonth(),
                        timePicker.getCurrentHour(),
                        timePicker.getCurrentMinute());

                SimpleDateFormat fmt = new SimpleDateFormat(dateFormat);
                fmt.setCalendar(calendar);
                date = fmt.format(calendar.getTime());
                alertDialog.dismiss();
            }});
        alertDialog.setView(dialogView);
        alertDialog.show();
    }

    public void addEditCommonTask()
    {
        name = nameET.getText().toString();
        if(name.length() == 0 ) {
            Toast.makeText(AddEditTaskActivity.this, "You must enter a name", Toast.LENGTH_SHORT).show();
        }
        else if(date != null){
            Calendar cal = new GregorianCalendar();
            SimpleDateFormat fmt = new SimpleDateFormat(dateFormat);
            try { cal.setTime(fmt.parse(date)); } catch (ParseException e) { e.printStackTrace(); }
            if((cal.getTimeInMillis() - System.currentTimeMillis()) > 0) {
                desc = descET.getText().toString();
                Intent returnIntent = new Intent();
                returnIntent.putExtra("result", new Task(name, desc, date));
                setResult(RESULT_OK, returnIntent);
                finish();
            }
            else
                Toast.makeText(AddEditTaskActivity.this, "You must enter a date that hasn't passed", Toast.LENGTH_SHORT).show();
        }
        else
        {
            Toast.makeText(AddEditTaskActivity.this, "You must enter a date", Toast.LENGTH_SHORT).show();
        }
    }

    public void addTask(View view)
    {
        new AlertDialog.Builder(AddEditTaskActivity.this)
                .setTitle("Add Task")
                .setMessage("Are you sure you want to add this task?")
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {addEditCommonTask(); }})
                .setNegativeButton("No",new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) { }
                })
                .show();
    }

    public void editTask(View view)
    {
        new AlertDialog.Builder(AddEditTaskActivity.this)
                .setTitle("Edit Task")
                .setMessage("Are you sure you want to edit this task?")
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) { addEditCommonTask();}})
                .setNegativeButton("No",new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) { }
                })
                .show();
    }
    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString("datetext",date);
    }

    @Override
    public void onRestoreInstanceState(@Nullable Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        String datetxt = savedInstanceState.getString("datetext");
        date=datetxt;
    }

}