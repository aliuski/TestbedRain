package com.probe.aki.testbedrain;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.TimeZone;

public class ValuesFromHtml {

    public static int IMAGE_COUNT = 15;

    private ArrayList<Date> date_array;

    public ArrayList<Date> getDateArray(){
        return date_array;
    }

    public String[] readMainHtmlFile() throws Exception {

        SimpleDateFormat sim = new SimpleDateFormat("yyyyMMddHHmm");
        sim.setTimeZone(TimeZone.getTimeZone("GMT"));
        date_array = new ArrayList<Date>();

        String result[] = null;
        URL url = new URL("https://testbed.fmi.fi/");
        HttpURLConnection conn=(HttpURLConnection) url.openConnection();
        conn.setConnectTimeout(60000);
        BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
        String line;
        while ((line = in.readLine()) != null) {
            if(line.indexOf("anim_timestamps") != -1){
                int start = line.indexOf("(\"")+2;
                int end = line.indexOf("\")");
                String[] str = line.substring(start, end).split("\",\"");
                for(int i=0 ; i<str.length ; i++){
                    date_array.add(sim.parse(str[i]));
                }
            }

            if(line.indexOf("anim_images_anim_anim") != -1){
                int start = line.indexOf("(\"")+2;
                int end = line.indexOf("\")");
                result = line.substring(start, end).split("\",\"");
                break;
            }
        }
        in.close();
        return result;
    }

    public ByteArrayOutputStream getObservationMap(String picture) throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        URL url = new URL(picture);
        InputStream is = url.openStream ();
        byte[] byteChunk = new byte[4096];
        int n;
        while ( (n = is.read(byteChunk)) > 0 ) {
            baos.write(byteChunk, 0, n);
        }
        is.close();
        return baos;
    }
}
