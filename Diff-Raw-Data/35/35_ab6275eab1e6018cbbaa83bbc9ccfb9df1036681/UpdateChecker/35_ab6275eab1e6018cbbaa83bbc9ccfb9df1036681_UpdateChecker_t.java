 package de.yanniks.cm_updatechecker;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.io.OutputStreamWriter;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.content.Intent;
import android.net.Uri;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.View;
 import android.webkit.WebView;
 import android.webkit.WebViewClient;
 import android.widget.TextView;
 import android.widget.Toast;
 
 public class UpdateChecker extends Activity {
     /** Called when the activity is first created. */
 	private WebView mWebView;
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.updatecheck);
         TextView tv = (TextView)findViewById(R.id.installedversion);
     	mWebView = (WebView) findViewById(R.id.updatecheck);
     	mWebView.loadUrl("http://yanniks.de/roms/current-cm101ace.html");
     	mWebView.setWebViewClient(new LoginClient());
         String input = "getprop |awk -F :  '/ro.cm.version/ { print $2 }'";
         execCommandLine(input, tv);}
 
         void execCommandLine(String command, TextView tv)
             {
             Runtime runtime = Runtime.getRuntime();
             Process proc = null;
             OutputStreamWriter osw = null;
             // Running the Script
             try
             {
             proc = runtime.exec("su");
             osw = new OutputStreamWriter(proc.getOutputStream());
             osw.write(command);
             osw.flush();
             osw.close();
             }
             // If return error
             catch (IOException ex)
             {
             // Log error
             Log.e("execCommandLine()", "Command resulted in an IO Exception: " + command);
             return;
             }
             // Try to close the process
             finally
             {
             if (osw != null)
             {
             try
             {
             osw.close();
             }
             catch (IOException e){}
             }
             }
             try 
             {
             proc.waitFor();
             }
             catch (InterruptedException e){}
             // Display on screen if error
             if (proc.exitValue() != 0)
             {
             Log.e("execCommandLine()", "Command returned error: " + command + "\n Exit code: " + proc.exitValue());
             AlertDialog.Builder builder = new AlertDialog.Builder(UpdateChecker.this);
             builder.setMessage(command + "\nCould not get root rights!");
             builder.setNeutralButton("OK", null);
             AlertDialog dialog = builder.create();
             dialog.setTitle("Error!");
             dialog.show(); 
             }
             BufferedReader reader = new BufferedReader(
             new InputStreamReader(proc.getInputStream())); 
             int read;
             char[] buffer = new char[4096];
             StringBuffer output = new StringBuffer();
             try {
             while ((read = reader.read(buffer)) > 0) {
             output.append(buffer, 0, read);
             }
             } catch (IOException e) {
             // TODO Auto-generated catch block
             e.printStackTrace();
             }
             String exit = output.toString();
             if(exit != null && exit.length() == 0) {
             exit = "Cannot read current build";
             } 
             tv.setText(exit); 
             } 
     	public void buildbotdb (final View view) {
    		 Intent facebookIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://yanniks.de/roms/cm-buildbot-ace")); 
    		 startActivity(facebookIntent);
     	}
     	public void downloadnew (final View view) {
         	startActivity (new Intent (this,DownloadNew.class));
     	}
     	public void cmchanges (final View view) {
         	startActivity (new Intent (this,cmchanges.class));
     	}
         private class LoginClient extends WebViewClient {
             @Override
             public boolean shouldOverrideUrlLoading(WebView view, String url) {
                 view.loadUrl(url);
                 return true;
             }
         }
         @Override
         public boolean onCreateOptionsMenu (Menu menu) {
             MenuInflater inflater = getMenuInflater();
             inflater.inflate(R.menu.menu, menu);
             return super.onCreateOptionsMenu(menu);
         }  
 
         @Override
         public boolean onOptionsItemSelected(MenuItem item) {
             if(item.getItemId() == R.id.item3){
             	startActivity (new Intent (this,RegisterActivity.class));
             	return true;
             }
             if(item.getItemId() == R.id.item1){
                 mWebView.reload();
                 Toast.makeText(UpdateChecker.this, getString(R.string.updated), Toast.LENGTH_LONG).show();
                 return true;
                 }
             if(item.getItemId() == R.id.item2){
             	startActivity (new Intent (this,ROMs.class));
             	return true;
             }
             return super.onOptionsItemSelected(item);
         }
 }
