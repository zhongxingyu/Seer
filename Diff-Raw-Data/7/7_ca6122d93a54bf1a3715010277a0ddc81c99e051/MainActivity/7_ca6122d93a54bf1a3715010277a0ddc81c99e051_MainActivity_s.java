 /*  
  *  Codebits
  *  Copyright (C) 2012 Henrique Rocha <hmrocha@gmail.com>
  *
  *  This program is free software: you can redistribute it and/or modify
  *  it under the terms of the GNU General Public License as published by
  *  the Free Software Foundation, either version 3 of the License, or
  *  (at your option) any later version.
  *
  *  This program is distributed in the hope that it will be useful,
  *  but WITHOUT ANY WARRANTY; without even the implied warranty of
  *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *  GNU General Public License for more details.
  *
  *  You should have received a copy of the GNU General Public License
  *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 package net.henriquerocha.android.codebits;
 
 import java.io.IOException;
 import java.util.ArrayList;
 
 import net.henriquerocha.android.codebits.api.Methods;
 import net.henriquerocha.android.codebits.api.Talk;
 
 import org.json.JSONArray;
 import org.json.JSONException;
 
 import android.app.ListActivity;
 import android.content.Context;
 import android.content.Intent;
 import android.net.ConnectivityManager;
 import android.net.NetworkInfo;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.View;
 import android.view.Window;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemClickListener;
 
 import com.actionbarsherlock.app.SherlockListActivity;
 
 public class MainActivity extends SherlockListActivity {
     private static final String DEBUG_TAG = DownloadJsonTalksTask.class.getSimpleName();
 
     // private TextView textView;
     private ArrayList<Talk> talks;
     private ListActivity context;
     private String token;
 
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
         setContentView(R.layout.activity_main);
         setProgressBarIndeterminateVisibility(false);
         this.context = this;
         this.talks = new ArrayList<Talk>();
         this.token = getIntent().getStringExtra(LoginActivity.AUTH_TOKEN);
 
         // textView = (TextView) findViewById(R.id.talks);
         ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
         NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
         if (networkInfo != null && networkInfo.isConnected()) {
             new DownloadJsonTalksTask().execute(Methods.CALL_FOR_TALKS);
         } else {
             // textView.setText("No network connection available.");
         }
         getListView().setOnItemClickListener(new OnItemClickListener() {
             public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                 Intent intent = new Intent(context, DisplayTalkActivity.class);
                 intent.putExtra("talk", talks.get(position));
                 intent.putExtra(LoginActivity.AUTH_TOKEN, token);
                 startActivity(intent);
             }
         });
     }
 
     private class DownloadJsonTalksTask extends AsyncTask<String, Void, String> {
 
         @Override
         protected void onPreExecute() {
             super.onPreExecute();
             setProgressBarIndeterminateVisibility(true);
         }
 
         @Override
         protected String doInBackground(String... urls) {
             String result = null;
             try {
                 String url = urls[0];
                 if (token != null) {
                     url += "?token=" + token;
                 }
                 result = NetworkUtils.downloadUrl(url);
             } catch (IOException e) {
                 result = "Unable to retrieve web page. URL may be invalid.";
             }
             return result;
         }
 
         @Override
         protected void onPostExecute(String result) {
             try {
                 JSONArray jsonTalks = new JSONArray(result);
                 for (int i = 0; i < jsonTalks.length(); i++) {
                     talks.add(new Talk(jsonTalks.getJSONObject(i)));
                 }
             } catch (JSONException e) {
                 Log.d(DEBUG_TAG, e.getMessage());
             }
             context.setListAdapter(new TalksArrayAdapter(context, talks, token != null));
             setProgressBarIndeterminateVisibility(false);
         }
     }
 
 }
