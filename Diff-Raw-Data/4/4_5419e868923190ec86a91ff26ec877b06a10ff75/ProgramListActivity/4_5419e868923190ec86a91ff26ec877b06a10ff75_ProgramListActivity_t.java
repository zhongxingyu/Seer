 package org.ftang;
 
 import android.app.AlertDialog;
 import android.app.Dialog;
 import android.app.ListActivity;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.net.ConnectivityManager;
 import android.net.NetworkInfo;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.View;
 import android.widget.ListView;
 import android.widget.Toast;
 import org.ftang.adapter.ProgramAdapter;
 import org.ftang.cache.SimpleExternalCache;
 import org.ftang.cache.SimpleExternalCacheImpl;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 public class ProgramListActivity extends ListActivity {
 
     private static final String DEBUG_TAG = "ProgramList-NetworkStatus";
 
     private SimpleExternalCache externalCache;
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         externalCache = new SimpleExternalCacheImpl(this);
 
         setListAdapter(new ProgramAdapter(this, createList()));
 
     }
 
     @Override
     protected void onListItemClick(ListView l, View v, int position, long id) {
         String selectedValue = (String) getListAdapter().getItem(position);
 
         if (isOnline()) {
             new DownloadProgramTask(this, externalCache).execute(selectedValue);
         } else {
             //Toast.makeText(this, "Network error!", Toast.LENGTH_SHORT).show();
             showAlert("Error!", "No network connection").show();
         }
     }
     
     private Map<String, String> createList() {
         Map<String, String> programs = new HashMap<String, String>();
         List<String> rawLines = readRawTextFile(getBaseContext(), R.raw.programs);
         for (String line : rawLines) {
             String[] tokens = line.split(",");
             if (tokens.length == 4)
                 programs.put(padding(tokens[2], 2) + "_" + tokens[1], tokens[3]); // FIXME TODO
             else
                 programs.put(padding(tokens[2], 2) + "_" + tokens[1], "placeholder"); // FIXME TODO
         }
         
         return programs;
     }
 
     private String padding(String token, int position) {
         return String.format("%0" + position + "d", Integer.parseInt(token));
     }
 
     public static List<String> readRawTextFile(Context ctx, int resId) {
         InputStream inputStream = ctx.getResources().openRawResource(resId);
 
         InputStreamReader inputreader = new InputStreamReader(inputStream);
         BufferedReader buffreader = new BufferedReader(inputreader);
         String line;
         List<String> l = new ArrayList<String>();
         
         try {
             while (( line = buffreader.readLine()) != null) {
                 l.add(line);
             }
         } catch (IOException e) {
             return null;
         }
         return l;
     }
 
     public boolean isOnline() {
         ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
         NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
         return (networkInfo != null && networkInfo.isConnected());
     }
 
     public boolean isWifiConnected() {
         ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
         NetworkInfo networkInfo = connMgr.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
         boolean isWifiConn = networkInfo.isConnected();
         return isWifiConn;
     }
 
     public boolean isMobileConnected() {
         ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
         NetworkInfo networkInfo = connMgr.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
         boolean isMobileConn = networkInfo.isConnected();
         return isMobileConn;
     }
     
     private Dialog showAlert(String title, String msg) {
         AlertDialog alertDialog = new AlertDialog.Builder(this).create();
        alertDialog.setTitle(title);
        alertDialog.setMessage(msg);
         alertDialog.setButton("OK", new DialogInterface.OnClickListener() {
             public void onClick(DialogInterface dialog, int which) {
                 dialog.dismiss();
             }
         });
         return alertDialog;
     }
 }
