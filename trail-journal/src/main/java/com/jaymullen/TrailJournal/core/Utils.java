package com.jaymullen.TrailJournal.core;

import android.content.Context;
import android.net.ConnectivityManager;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;

import java.io.*;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created with IntelliJ IDEA.
 * User: jaymullen
 * Date: 2/21/13
 * Time: 8:34 PM
 * To change this template use File | Settings | File Templates.
 */
public class Utils {

    public static HttpPost getPost(String url){
        HttpPost httppost = new HttpPost(url);

        httppost.setHeader("Host", "www.trailjournals.com");
        httppost.setHeader("Cache-Control", "max-age=0");
        httppost.setHeader("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");
        httppost.setHeader("Origin", "http://www.trailjournals.com");
        httppost.setHeader("User-Agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_7_5) AppleWebKit/537.17 (KHTML, like Gecko) Chrome/24.0.1312.57 Safari/537.17");
        httppost.setHeader("Accept-Encoding", "gzip,deflate,sdch");
        httppost.setHeader("Accept-Language", "en-US,en;q=0.8");
        httppost.setHeader("Content-Type", "application/x-www-form-urlencoded");
        httppost.setHeader("Accept-Charset", "ISO-8859-1,utf-8;q=0.7,*;q=0.3");

        return httppost;
    }

    public static HttpGet getGet(String url){
        HttpGet httpget = new HttpGet(url);

        httpget.setHeader("Host", "www.trailjournals.com");
        httpget.setHeader("Cache-Control", "max-age=0");
        //httpget.setHeader("Origin", "http://www.trailjournals.com");
        httpget.setHeader("User-Agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_7_5) AppleWebKit/537.17 (KHTML, like Gecko) Chrome/24.0.1312.57 Safari/537.17");
        httpget.setHeader("Accept-Charset", "ISO-8859-1,utf-8;q=0.7,*;q=0.3");

        return httpget;
    }

    /*
    * To convert the InputStream to String we use the BufferedReader.readLine()
    * method. We iterate until the BufferedReader return null which means
    * there's no more data to read. Each line will appended to a StringBuilder
    * and returned as String.
    */
    public static String convertStreamToString(InputStream is) {
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        StringBuilder sb = new StringBuilder();

        String line = null;
        try {
            while ((line = reader.readLine()) != null) {
                sb.append(line + "\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                is.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return sb.toString();
    }

    private static SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    public static String getNowString(boolean encode){
        String date = "{ts '" + sdf.format(new Date()) + "'}";
        if(encode){
            try{
                return URLEncoder.encode(date, "UTF-8");
            } catch(UnsupportedEncodingException e){

            }
        }
        return date;
    }

    public static boolean isOnline(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);

        return (cm.getActiveNetworkInfo() != null)
                && cm.getActiveNetworkInfo().isConnectedOrConnecting();
    }

    /**
     * Loads raw resource and returns as string
     */
    public static String loadResourceAsString(Context context, int resource) throws IOException {
        InputStream is = context.getResources().openRawResource(resource);
        Writer writer = new StringWriter();
        char[] buffer = new char[1024];
        try {
            Reader reader = new BufferedReader(new InputStreamReader(is, "UTF-8"));
            int n;
            while ((n = reader.read(buffer)) != -1) {
                writer.write(buffer, 0, n);
            }
        } finally {
            is.close();
        }

        return writer.toString();
    }
}
