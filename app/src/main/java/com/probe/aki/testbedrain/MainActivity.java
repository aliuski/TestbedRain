package com.probe.aki.testbedrain;

import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.res.Configuration;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class MainActivity extends AppCompatActivity {
    private ShowDisplay showDisplay;
    private TextView tv;
    private SeekBar simpleSeekBar;
    private SimpleDateFormat dateFormat;
    private ArrayList<Date> date_array;
    private Date lastupdate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        dateFormat = new SimpleDateFormat("HH:mm");
        setContentView(R.layout.activity_main);

        showDisplay = (ShowDisplay) findViewById(R.id.showDisplay);
        tv = (TextView) findViewById(R.id.timeView);
        simpleSeekBar = (SeekBar)findViewById(R.id.simpleSeekBar);
        simpleSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            int progressChangedValue = 0;

            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                progressChangedValue = progress;
            }

            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            public void onStopTrackingTouch(SeekBar seekBar) {
                showDisplay.setTime(progressChangedValue);
                if(date_array != null && date_array.size() == ValuesFromHtml.IMAGE_COUNT)
                    tv.setText(dateFormat.format(date_array.get(progressChangedValue)));
            }
        });

        if(savedInstanceState != null) {
            try {
                byte sd[] = savedInstanceState.getByteArray("savedata");
                if(sd == null)
                    return;
                ByteArrayInputStream bis = new ByteArrayInputStream(sd);
                ObjectInputStream ois = new ObjectInputStream(bis);
                showDisplay.setBitmapArray((ArrayList<byte[]>) ois.readObject());
                date_array = (ArrayList<Date>) ois.readObject();
                lastupdate = (Date) ois.readObject();
                bis.close();
                ois.close();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    @Override
    protected void onStart(){
        super.onStart();
        checkUpdate();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
            View decorView = getWindow().getDecorView();
            decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
        }
        int index = simpleSeekBar.getProgress();
        showDisplay.setTime(index);
        if(date_array != null)
            tv.setText(dateFormat.format(date_array.get(index)));
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);
        if (showDisplay.getBitmapArray() != null) {
            try {
                ByteArrayOutputStream bos = new ByteArrayOutputStream();
                ObjectOutputStream oout = new ObjectOutputStream(bos);
                oout.writeObject(showDisplay.getBitmapArray());
                oout.writeObject(date_array);
                oout.writeObject(lastupdate);
                byte[] yourBytes = bos.toByteArray();
                savedInstanceState.putByteArray("savedata", yourBytes);
                oout.close();
                bos.close();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    public void checkUpdate(){
        if (lastupdate == null || ((new Date()).getTime() > lastupdate.getTime() + 300000)) { // 5 min
            lastupdate = new Date();
            new ReadObservationTask().execute("");
        }
    }

    private class ReadObservationTask extends AsyncTask
            <String, Void, String> {
        ProgressDialog progressDialog;
        ValuesFromHtml valuesFromHtml;
        private ArrayList<byte[]> bitmap_array;
        @Override
        protected void onPreExecute()
        {
            valuesFromHtml = new ValuesFromHtml();
            progressDialog = new ProgressDialog(MainActivity.this);
            progressDialog.setMax(100);
            progressDialog.setMessage(getResources().getString(R.string.download_text));
            progressDialog.setTitle(getResources().getString(R.string.app_name));
            progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
            progressDialog.show();
        }

        @Override
        protected String doInBackground(String... urls) {
            try {
                String res[] = valuesFromHtml.readMainHtmlFile();
                if (res != null) {
                    bitmap_array = new ArrayList<byte[]>();
                    for (int i = 0; i < ValuesFromHtml.IMAGE_COUNT; i++) {
                        bitmap_array.add(valuesFromHtml.getObservationMap(res[i]).toByteArray());
                        progressDialog.incrementProgressBy((int) ((double) i * 0.667));
                    }
                    if(bitmap_array.size() != ValuesFromHtml.IMAGE_COUNT)
                        return "FALSE";
                }
            } catch(Exception e){
                return "FALSE";
            }
            return "OK";
        }

        @Override
        protected void onPostExecute(String result) {
            if (progressDialog != null)
                progressDialog.dismiss();
            if (result.equals("OK")) {
                showDisplay.setBitmapArray(bitmap_array);
                showDisplay.setTime(0);
                date_array = valuesFromHtml.getDateArray();
                tv.setText(dateFormat.format(date_array.get(date_array.size()-1)));
                simpleSeekBar.setProgress(ValuesFromHtml.IMAGE_COUNT);
            } else
                Toast.makeText(MainActivity.this, R.string.error_download_text, Toast.LENGTH_SHORT).show();
        }
    }
}
