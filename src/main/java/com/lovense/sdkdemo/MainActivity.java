package com.lovense.sdkdemo;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.lovense.sdklibrary.Lovense;
import com.lovense.sdklibrary.LovenseToy;
import com.lovense.sdklibrary.callBack.LovenseError;
import com.lovense.sdklibrary.callBack.OnErrorListener;
import com.lovense.sdklibrary.callBack.OnSearchToyListener;
import com.tbruyelle.rxpermissions2.RxPermissions;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.logging.Handler;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

/**
 *  Created by Lovense on 2019/5/14
 *
 *  Copyright © 2019 Hytto. All rights reserved.
 */
public class MainActivity extends AppCompatActivity {

    private View start;
    static TextView stop,title,textView2,name;

    static String gender,pref,id,toyId;

    PrintWriter output;
    BufferedReader input;
    Socket socket;

    static EditText phone;

    //RadioGroup radioGroup = (RadioGroup)findViewById(R.id.groupradio);

    private RxPermissions rxPermissions;

    List<LovenseToy> lovenseToys = new ArrayList<>();
    private ToyAdapter toyAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        EventBus.getDefault().register(this);

        Lovense.getInstance(getApplication()).setDeveloperToken("MKT1+Nypf0wxUvgKr9TtXCVcwbTRnTttsYEVBcYIVZ0el+ZROmtm73zhn1LXm/s4");

        textView2= findViewById(R.id.textView2);
        textView2.setText("Whatsapp");
        name= findViewById(R.id.editTextTextPersonName2);
        phone= findViewById(R.id.editTextPhone);
        phone.setText("+");
        start= findViewById(R.id.start_scan);
        stop= findViewById(R.id.stop_scan);
        title= findViewById(R.id.title);
        RecyclerView recyclerView = ((RecyclerView) findViewById(R.id.recyler_view));

        LinearLayoutManager manager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        toyAdapter = new ToyAdapter(this, lovenseToys);
        recyclerView.setLayoutManager(manager);
        recyclerView.setAdapter(toyAdapter);

//        Lovense.getInstance(getApplication()).setSearchToyListener();

        rxPermissions = new RxPermissions(this);

        start.setOnClickListener(new View.OnClickListener() {
           @Override
           public void onClick(View v) {
               rx();
           }
       });
        stop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Lovense.getInstance(getApplication()).stopSearching();
                title.setText("search toy(stop scan)");
            }
        });

        title.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                List<LovenseToy> lovenseToys = Lovense.getInstance(getApplication()).listToys(new OnErrorListener() {
                    @Override
                    public void onError(LovenseError error) {

                    }
                });
            }
        });


        RadioGroup rg = findViewById(R.id.groupradiogender);
        RadioGroup rg2 = findViewById(R.id.groupradiopref);

        rg.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
        //    @Override
            public void onCheckedChanged(RadioGroup radioGroup, int i) {
                RadioButton rb = findViewById(rg.getCheckedRadioButtonId());
                gender = rb.getText().toString();
            }
        });

        rg2.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            //    @Override
            public void onCheckedChanged(RadioGroup radioGroup, int i) {
                RadioButton rb2 = findViewById(rg.getCheckedRadioButtonId());
                pref = rb2.getText().toString();
            }
        });
    }

    private void scanDev() {
        lovenseToys.clear();
        toyAdapter.notifyDataSetChanged();
//        Toast.makeText(MainActivity.this, "start scan！", Toast.LENGTH_SHORT).show();
        title.setText("search toy(scaning)");
        Lovense.getInstance(getApplication()).searchToys(new OnSearchToyListener() {
            @Override
            public void onSearchToy(LovenseToy lovenseToy)
            {
                Toast.makeText(MainActivity.this, "connect！", Toast.LENGTH_SHORT).show();
                //connectToServer();
                Toast.makeText(MainActivity.this, "connected！", Toast.LENGTH_SHORT).show();
                addDevice(lovenseToy);

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                    Handler handle = new Handler() {
                        @Override
                        public void publish(LogRecord logRecord) {

                        }

                        @Override
                        public void flush() {

                        }

                        @Override
                        public void close() throws SecurityException {

                        }
                    };
                    Logger.getGlobal().addHandler(handle);
                }
            }



            @Override
            public void finishSearch() {
                Lovense.getInstance(getApplication()).saveToys(lovenseToys, new OnErrorListener() {
                    @Override
                    public void onError(LovenseError error) {

                    }
                });
                title.setText("search toy(stop scan)");
            }

            @Override
            public void onError(LovenseError msg) {
                Toast.makeText(MainActivity.this, msg.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }


    private void rx(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // Android M Permission check
            if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED
                    || ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.BLUETOOTH) != PackageManager.PERMISSION_GRANTED) {
                rxPermissions.request(Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.BLUETOOTH)
                        .subscribe(new io.reactivex.functions.Consumer<Boolean>() {
                            @Override
                            public void accept(Boolean b) throws Exception {
                                if (b) {
                                    scanDev();
                                } else {
                                    Toast.makeText(MainActivity.this, "If you\\'re using Android 6.0+, your GPS must be enabled to connect to Bluetooth devices.", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
            } else {
                scanDev();
            }
        } else {
            scanDev();
        }
    }

    private void addDevice(LovenseToy lovenseToy) {
        if (lovenseToy != null) {
            if (!isAdded(lovenseToy)) {
                lovenseToys.add(lovenseToy);
                toyAdapter.notifyDataSetChanged();
                //lovense.requestConnect(device.getAddress());
            }
        }
    }

    protected boolean isAdded(LovenseToy lovenseToy) {
        for (LovenseToy t: lovenseToys) {
            id = t.getToyId();
            toyId = lovenseToy.getToyId();
            if (!TextUtils.isEmpty(id) && id.equals(toyId)) {
                return true;
            }
        }
        return false;
    }



    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(ToyConnectEvent mToyConnectEvent) {
        String id = mToyConnectEvent.getId();
        int connect = mToyConnectEvent.getConnect();
        for (int i = 0; i < lovenseToys.size(); i++) {
            LovenseToy lovenseToy = lovenseToys.get(i);
            if (lovenseToy.getToyId().equals(id)){
                lovenseToy.setStatus(connect);
                toyAdapter.notifyItemChanged(i);
                break;
            }
        }
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }

    public void onRadioButtonClicked(View view) {
        // Is the button now checked?
        boolean checked = ((RadioButton) view).isChecked();
        boolean checked2 = ((RadioButton) view).isChecked();

        // Check which radio button was clicked
        switch(view.getId()) {
            case R.id.radioButton:
                if (checked)
                    gender = "Male";
                    break;
            case R.id.radioButton2:
                if (!checked)
                    gender = "Female";
                    break;
            case R.id.radioButton3:
                if (checked2)
                    pref = "Male";
                    break;
            case R.id.radioButton4:
                if (!checked2)
                    pref = "Female";
                    break;
        }
    }


}

