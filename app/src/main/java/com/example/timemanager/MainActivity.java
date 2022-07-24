package com.example.timemanager;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.view.menu.MenuBuilder;
import androidx.lifecycle.ViewModelProvider;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.work.Data;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.provider.Settings;
import android.provider.SyncStateContract;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity {

    RecyclerView rvTasks;
    myAdapter adapter,adapterCompleted,adapterUncompleted;
    public static myViewModel myviewModel;
    Switch viewCompletedSwitch ;
    boolean soundAlarm,airplaneMode;
    Button addTaskBTN , completeTaskBTN, editTaskBTN, renewTaskBTN;
    IntentFilter intentFilter ;
    BroadcastReceiver receiver;
    SharedPreferences.OnSharedPreferenceChangeListener prefChangeListener;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //initializations
        rvTasks = (RecyclerView) findViewById(R.id.rvTasks);
        myviewModel = new ViewModelProvider(this).get(myViewModel.class);
        adapterUncompleted = new myAdapter(this,myviewModel,false);
        adapterCompleted = new myAdapter(this, myviewModel,true);
        adapter = adapterUncompleted;
        rvTasks.setAdapter(adapter);
        rvTasks.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
        addTaskBTN = findViewById(R.id.addTaskBTN);
        editTaskBTN = findViewById(R.id.editTaskBTN);
        renewTaskBTN = findViewById(R.id.renewTaskBTN);
        completeTaskBTN = findViewById(R.id.completeTaskBTN);
        viewCompletedSwitch = findViewById(R.id.viewCompletedSwitch);
        //completed tasks switch listener
        viewCompletedSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                viewCompleted(buttonView,isChecked);
            }
        });
        //getting shared preferences and setting listener -- NOT DETECTING CHANGE YET
        SharedPreferences app_preferences = PreferenceManager.getDefaultSharedPreferences(this);
        soundAlarm = app_preferences.getBoolean("soundAlarmCheckBox",false);
        prefChangeListener = new SharedPreferences.OnSharedPreferenceChangeListener() {
            public void onSharedPreferenceChanged(SharedPreferences prefs, String key) {
                if (key.equals("soundAlarmCheckBox")) {
                    soundAlarm = prefs.getBoolean("soundAlarmCheckBox",true);
                    manageAlarmService();
                }
            }};
        app_preferences.registerOnSharedPreferenceChangeListener(prefChangeListener);
        //initialize and register airplaneMode and broadcast reciever for airplane
        airplaneMode = isAirplaneModeOn(this);
        intentFilter =  new IntentFilter("android.intent.action.AIRPLANE_MODE");
        receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                boolean newMode = isAirplaneModeOn(MainActivity.this);
                if(airplaneMode != newMode)
                {
                    airplaneMode = newMode;
                    manageAlarmService();
                }
            }
        };
        this.registerReceiver(receiver,intentFilter);
        manageAlarmService();
    }

    private void manageAlarmService() {
        if(!checkServiceRunning(AlarmForeGroundService.class)) {
            if (soundAlarm && !airplaneMode) {
                Intent serviceIntent = new Intent(this, AlarmForeGroundService.class);
                startForegroundService(serviceIntent);
            }
        }
        else {
                if (!soundAlarm || airplaneMode)
                    AlarmForeGroundService.stop();
            }
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt("item_pos",adapter.getSelectedPos());
    }

    @Override
    public void onRestoreInstanceState(@Nullable Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        int pos = savedInstanceState.getInt("item_pos");
        adapter.setSelectedPos(pos);
    }



    public static myViewModel getViewModel()
    {
        return myviewModel;
    }
    public void addTaskClicked(View view)
    {
        Intent intent = new Intent(this, AddEditTaskActivity.class);
        intent.putExtra("requestCode",10);
        startActivityForResult(intent,10);
    }

    public void completeTask(View view)
    {
        if (adapter.isSelctedNull())
            Toast.makeText(this, "you must select a task first to complete it!", Toast.LENGTH_SHORT).show();
        else
        {
            adapter.completeTask();
        }
    }

    public void editTaskClicked(View view)
    {
        if (adapter.isSelctedNull())
            Toast.makeText(this, "you must select a task first to edit it!", Toast.LENGTH_SHORT).show();
        else {
            Task task = adapter.getTask();
            Intent intent = new Intent(this, AddEditTaskActivity.class);
            intent.putExtra("requestCode", 20);
            intent.putExtra("task",task);
            startActivityForResult(intent, 20);
        }
    }

    public void renewTaskClicked(View view)
    {
        if (adapter.isSelctedNull())
            Toast.makeText(this, "you must select a task first to renew it!", Toast.LENGTH_SHORT).show();
        else {
            Task task = adapter.getTask();
            task.setCompleted(false);
            Intent intent = new Intent(this, AddEditTaskActivity.class);
            intent.putExtra("requestCode", 20);
            intent.putExtra("task",task);
            startActivityForResult(intent, 20);
        }
    }

    @Override
    protected void onActivityResult (int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && requestCode == 10) {
            adapter.addTask((Task) data.getExtras().get("result"));
        }
        else if (resultCode == RESULT_OK && requestCode == 20)
        {
            adapter.editTask((Task) data.getExtras().get("result"));
        }

    }

    public void viewCompleted(CompoundButton buttonView, boolean isChecked)
    {

        if(isChecked) {
            adapter = adapterCompleted;
            addTaskBTN.setVisibility(View.INVISIBLE);
            editTaskBTN.setVisibility(View.INVISIBLE);
            completeTaskBTN.setVisibility(View.INVISIBLE);
            renewTaskBTN.setVisibility(View.VISIBLE);
        }
        else {
            addTaskBTN.setVisibility(View.VISIBLE);
            editTaskBTN.setVisibility(View.VISIBLE);
            completeTaskBTN.setVisibility(View.VISIBLE);
            renewTaskBTN.setVisibility(View.INVISIBLE);
            adapter = adapterUncompleted;
        }
        rvTasks.setAdapter(adapter);
        rvTasks.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
    }
    public void removeTaskClicked(View view)
    {

        if (adapter.isSelctedNull())
            Toast.makeText(this, "you must select a task first to remove it!", Toast.LENGTH_SHORT).show();
        else
        {
            new AlertDialog.Builder(MainActivity.this)
                    .setTitle("Delete Task")
                    .setMessage("Are you sure you want to delete this task?")

                    .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            adapter.removeTask();
                        }
                    })
                    .setNegativeButton("No", null)
                    .show();
        }
    }

    @SuppressLint("RestrictedApi")
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflator = getMenuInflater();
        inflator.inflate(R.menu.mymenu, menu);
        if(menu instanceof MenuBuilder){
            MenuBuilder m = (MenuBuilder) menu;
            m.setOptionalIconsVisible(true);
        }
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId())
        {
            case R.id.settingBottom:
                getSupportFragmentManager()
                        .beginTransaction()
                        .replace(android.R.id.content ,new MainActivity.MyPreferences()).addToBackStack(null)
                        .commit();
                break;
            case R.id.ExitButton:
            {
                new AlertDialog.Builder(MainActivity.this)
                        .setTitle("Exit App")
                        .setMessage("Are you sure you want exit?")
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {finishAndRemoveTask(); }})
                        .setNegativeButton("No",new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) { }
                        })
                        .show();
            }
                ;
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private boolean checkServiceRunning(Class<?> serviceClass)
    {
        ActivityManager manager = (ActivityManager)getSystemService(Context.ACTIVITY_SERVICE);
        for(ActivityManager.RunningServiceInfo service: manager.getRunningServices(Integer.MAX_VALUE)){
            if(serviceClass.getName().equals(service.service.getClassName()))
                return true;
        }
        return false;
    }

    public static boolean isAirplaneModeOn(Context context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR1) {
            return Settings.System.getInt(context.getContentResolver(),
                    Settings.System.AIRPLANE_MODE_ON, 0) != 0;
        } else {
            return Settings.Global.getInt(context.getContentResolver(),
                    Settings.Global.AIRPLANE_MODE_ON, 0) != 0;
        }
    }

    public static class MyPreferences extends PreferenceFragmentCompat {
        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            setPreferencesFromResource(R.xml.mypreferencescreen, rootKey);
        }

        @Override
        public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
            view.setBackgroundColor(Color.WHITE);
            super.onViewCreated(view, savedInstanceState);
        }
    }

}