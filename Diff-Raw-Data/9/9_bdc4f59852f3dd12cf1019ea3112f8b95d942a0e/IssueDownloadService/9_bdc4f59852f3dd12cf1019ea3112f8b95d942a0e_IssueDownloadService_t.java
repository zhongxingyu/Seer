 /*
  * Der Bund ePaper Downloader - App to download ePaper issues of the Der Bund newspaper
  * Copyright (C) 2013 Adrian Gygax
  *
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program. If not, see {http://www.gnu.org/licenses/}.
  */
 
 package com.github.notizklotz.derbunddownloader;
 
 import android.app.IntentService;
 import android.content.Context;
 import android.content.Intent;
 import android.net.ConnectivityManager;
 import android.net.NetworkInfo;
 import android.util.Log;
 
 import org.apache.commons.io.IOUtils;
 
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.net.URL;
 import java.net.URLConnection;
 
 public class IssueDownloadService extends IntentService {
 
     public IssueDownloadService() {
         super(IssueDownloadService.class.getName());
     }
 
     public static Intent createDownloadIntent(Context context, int day, int month, int year) {
         Intent intent = new Intent(context, IssueDownloadService.class);
         intent.putExtra("day", day);
         intent.putExtra("month", month);
         intent.putExtra("year", year);
 
         return intent;
     }
 
     @Override
     protected void onHandleIntent(Intent intent) {
 
         if (!(intent.hasExtra("day") && intent.hasExtra("month") && intent.hasExtra("year"))) {
             Log.e(getClass().toString(), "extras missing");
         }
 
         int day = intent.getIntExtra("day", 0);
         String dayString = String.format("%02d", day);
         int month = intent.getIntExtra("month", 0);
         String monthString = String.format("%02d", month);
         int year = intent.getIntExtra("year", 0);
         String yearString = Integer.toString(year);
 
         String url = "http://epaper.derbund.ch/getAll.asp?d=" + dayString + monthString + yearString;
         try {
 
             ConnectivityManager connMgr = (ConnectivityManager)
                     getSystemService(Context.CONNECTIVITY_SERVICE);
             NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
             if (networkInfo != null && networkInfo.isConnected()) {
                 File downloadedFile = download(url, yearString + monthString + dayString + ".pdf");
                 getContentResolver().insert(IssueContentProvider.ISSUES_URI, IssueContentProvider.createContentValues(day, month, year, downloadedFile.getPath()));
             } else {
                 Log.d(getClass().toString(), "No network connection");
             }
         } catch (IOException e) {
             Log.e(getClass().toString(), "Download failed", e);
         }
     }
 
     private File download(String urlString, String filename) throws IOException {
         InputStream is = null;
         FileOutputStream fileOutputStream = null;
         URL url = new URL(urlString);
         URLConnection conn = url.openConnection();
         try {
             conn.connect();
             is = conn.getInputStream();
             File issuesDirectory = IssueContentProvider.getIssuesDirectory(this);
             if (!issuesDirectory.exists()) {
                //noinspection ResultOfMethodCallIgnored
                 issuesDirectory.mkdir();
             }
             File outputFile = new File(issuesDirectory, filename);
             fileOutputStream = new FileOutputStream(outputFile);
             IOUtils.copy(is, fileOutputStream);
             return outputFile;
         } finally {
             IOUtils.closeQuietly(fileOutputStream);
             IOUtils.closeQuietly(is);
             IOUtils.close(conn);
         }
     }
 }
