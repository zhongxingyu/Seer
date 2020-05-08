 package com.thoughtworks.healthgraphexplorer;
 
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.Menu;
 import android.view.View;
 import android.widget.Button;
 import android.widget.Toast;
 
 import com.github.kevinsawicki.http.HttpRequest;
 
 import roboguice.activity.RoboActivity;
 import roboguice.inject.InjectView;
 
 import static com.thoughtworks.healthgraphexplorer.Constants.BASE_URL;
 import static com.thoughtworks.healthgraphexplorer.Constants.CLIENT_ID_QUERY;
 import static com.thoughtworks.healthgraphexplorer.Constants.CLIENT_SECRET_QUERY;
 import static com.thoughtworks.healthgraphexplorer.Constants.REDIRECT_URI_QUERY;
 import static com.thoughtworks.healthgraphexplorer.Constants.SHARED_PREFS_AUTH_KEY;
 import static com.thoughtworks.healthgraphexplorer.Constants.SHARED_PREFS_NAME_AUTH;
 
 public class HelloAndroidActivity extends RoboActivity {
 
 
     @InjectView(R.id.deauthButton)
     private Button deauthButton;
 
 
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
     }
 
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         // Inflate the menu; this adds items to the action bar if it is present.
         getMenuInflater().inflate(R.menu.main, menu);
         return true;
     }
 
     @Override
     protected void onResume() {
         super.onResume();
         Toast toast = Toast.makeText(getApplicationContext(), "Let's start!", Toast.LENGTH_SHORT);
         toast.show();
 
 
         final SharedPreferences preferences = getSharedPreferences(SHARED_PREFS_NAME_AUTH, MODE_PRIVATE);
         final String authCode = preferences.getString(SHARED_PREFS_AUTH_KEY, "");
         Log.i("token", authCode);
 
         if (authCode.isEmpty()) {
             startAuthActivity();
         } else {
             setContentView(R.layout.activity_main);
             deauthButton.setOnClickListener(new View.OnClickListener() {
                 @Override
                 public void onClick(View view) {
                     preferences.edit().remove(SHARED_PREFS_AUTH_KEY).apply();
                     startAuthActivity();
                 }
             });
         }
 
        retireveTokenTask(authCode).execute();
 
     }
 
    private AsyncTask<Void, Void, String> retireveTokenTask(final String authCode) {
         return new AsyncTask<Void, Void, String>() {
             @Override
             protected String doInBackground(Void... voids) {
                 HttpRequest response = HttpRequest.post(BASE_URL + "/token")
                         .send("grant_type=authorization_code"
                                 + "&code=" + authCode
                                 + CLIENT_ID_QUERY
                                 + CLIENT_SECRET_QUERY
                                 + REDIRECT_URI_QUERY);
 
                 String body = response.body();
                 Log.i("XX", body);
 
 
                 return body;
             }
         };
     }
 
     private void startAuthActivity() {
         Intent authIntent = new Intent(this, AuthActivity.class);
         startActivity(authIntent);
     }
 }
 
