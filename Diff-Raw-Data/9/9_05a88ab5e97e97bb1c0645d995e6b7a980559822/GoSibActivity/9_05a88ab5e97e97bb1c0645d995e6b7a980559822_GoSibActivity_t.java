 package com.stillinbeta.go;
 
 import android.app.Activity;
 import android.app.ProgressDialog;
 import android.content.Context;
 import android.content.Intent;
 import android.content.pm.PackageInfo;
 import android.content.res.Resources;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.inputmethod.InputMethodManager;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.TextView;
 import android.widget.Toast;
 import java.io.IOException;
 import java.io.UnsupportedEncodingException;
 import org.apache.http.client.methods.HttpPost;
 import org.apache.http.entity.StringEntity;
 import org.apache.http.HttpResponse;
 import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.CoreProtocolPNames;
 import org.apache.http.StatusLine;
 import org.apache.http.util.EntityUtils;
 
 public class GoSibActivity extends Activity
 {
     private EditText url;
     /** Called when the activity is first created. */
     @Override
     public void onCreate(Bundle savedInstanceState)
     {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.main);
         this.url = (EditText)findViewById(R.id.url);
         
         Intent intent = getIntent();
         Bundle extras = intent.getExtras();
 
         if (intent.getAction().equals(Intent.ACTION_SEND)) {
             String text = intent.getStringExtra(Intent.EXTRA_TEXT);
             if (text != null) {
                 url.setText(text);
             }
             new GetShortenedUrl().execute(); 
         } else {
             Button shorten = (Button)findViewById(R.id.shorten);
             shorten.setOnClickListener(sendUrl);
         }
 
     }
 
     private OnClickListener sendUrl = new OnClickListener() {
         public void onClick(View v) {
            new GetShortenedUrl().execute();
         }
     };
 
     private class GetShortenedUrl extends AsyncTask<Void, Void, String> {
         private Context context;
         private ProgressDialog dialog;
 
         protected void onPreExecute() {
             this.context = getApplicationContext();
             this.dialog = ProgressDialog.show(GoSibActivity.this, 
                 "", "Shortenting...", true);
 
             // Hide the keyboard
             InputMethodManager imm = (InputMethodManager)
                 getSystemService(Context.INPUT_METHOD_SERVICE);
             imm.hideSoftInputFromWindow(url.getWindowToken(), 0);
 
         }
 
         protected String doInBackground(Void... nothing) {
             
             Resources res = getResources();
 
             HttpPost post = new HttpPost(res.getString(R.string.api_url));
             try {
                 StringEntity ent = new StringEntity("url=" + url.getText());
                 post.setEntity(ent);
             } catch (UnsupportedEncodingException e) {
                 Toast.makeText(context, e.toString(), Toast.LENGTH_SHORT).show(); 
             }
            // Lighttpd doesn't support this for some reason
            post.getParams().setBooleanParameter(
                CoreProtocolPNames.USE_EXPECT_CONTINUE, false);
 
             DefaultHttpClient client = new DefaultHttpClient();
             try {
                 HttpResponse response = client.execute(post);
                 StatusLine status = response.getStatusLine();
                 String code = Integer.toString(status.getStatusCode());
                 String word = EntityUtils.toString(response.getEntity());
                if (Integer.parseInt(code) != 200) {
                    Log.e("gosib", word);
                }
 
                 return word;
 
                         } catch (IOException  e) {
                 Toast.makeText(context, e.toString(), 
                                Toast.LENGTH_SHORT).show();
             }
 
             return "";
         }
 
         protected void onPostExecute(String word) {
             setContentView(R.layout.showurl);
             TextView shorten = (TextView)findViewById(R.id.noun);
             shorten.setText(word); 
 
             this.dialog.dismiss();
         }
     }
 }
