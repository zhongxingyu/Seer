 package com.selesse.android.winedb.winescraper.impl;
 
 import android.util.Log;
 import com.google.common.collect.Lists;
 import com.google.common.io.CharStreams;
 import com.google.gson.Gson;
 import com.selesse.android.winedb.database.Wine;
 import com.selesse.android.winedb.priv.UPCDatabaseOrgKey;
 import com.selesse.android.winedb.winescraper.WineScraper;
 
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.net.URL;
 import java.net.URLConnection;
 import java.util.List;
 
 public class UPCDatabaseOrgWineScraper implements WineScraper {
     private static final String TAG = UPCDatabaseOrgWineScraper.class.getSimpleName();
     private String url;
     private List<Exception> errors;
 
     public UPCDatabaseOrgWineScraper(String barcode) {
         url = "http://upcdatabase.org/api/json/" + UPCDatabaseOrgKey.getKey() + "/" + barcode;
        errors = Lists.newArrayList();
     }
 
     @Override
     public List<Wine> scrape() {
         List<Wine> scrapedWines = Lists.newArrayList();
         try {
             URL url = new URL(getQueryUrl());
             URLConnection connection = url.openConnection();
             String jsonResponse = CharStreams.toString(new InputStreamReader(connection.getInputStream()));
 
             Gson gson = new Gson();
             Log.d(TAG, "UPCDatabase.org JSON response was: \n" + jsonResponse);
             UPCDatabaseOrgResponse results = gson.fromJson(jsonResponse, UPCDatabaseOrgResponse.class);
             if (results.getResultsSize() > 0) {
                 scrapedWines = results.convertResponsesToWineList();
             }
         } catch (IOException e) {
             Log.e(TAG, "UPCDatabase.org threw exception : \n" + e);
             errors.add(e);
         }
 
         return scrapedWines;
     }
 
     @Override
     public String getQueryUrl() {
         return url;
     }
 
     @Override
     public List<Exception> getErrors() {
         return errors;
     }
 
     @Override
     public String getSourceName() { return "UPCDatabase.org"; }
 }
