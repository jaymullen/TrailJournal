package com.jaymullen.TrailJournal.core;

import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;

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
        httppost.setHeader("Origin", "http://www.trailjournals.com");
        httppost.setHeader("User-Agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_7_5) AppleWebKit/537.17 (KHTML, like Gecko) Chrome/24.0.1312.57 Safari/537.17");
        httppost.setHeader("Content-Type", "application/x-www-form-urlencoded");
        httppost.setHeader("Referer", "http://www.trailjournals.com/login.cfm");
        httppost.setHeader("Accept-Charset", "ISO-8859-1,utf-8;q=0.7,*;q=0.3");

        return httppost;
    }

    public static HttpGet getGet(String url){
        HttpGet httpget = new HttpGet(url);

        httpget.setHeader("Host", "www.trailjournals.com");
        httpget.setHeader("Cache-Control", "max-age=0");
        httpget.setHeader("Origin", "http://www.trailjournals.com");
        httpget.setHeader("User-Agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_7_5) AppleWebKit/537.17 (KHTML, like Gecko) Chrome/24.0.1312.57 Safari/537.17");
        httpget.setHeader("Accept-Charset", "ISO-8859-1,utf-8;q=0.7,*;q=0.3");

        return httpget;
    }
}
