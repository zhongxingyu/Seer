 package org.mad.app.hokiehelper;
 
 import android.content.Context;
 import android.content.Intent;
 import android.net.ConnectivityManager;
 import android.net.NetworkInfo;
 import android.os.Bundle;
 import android.os.Handler;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.ImageView;
 import android.widget.LinearLayout;
 import android.widget.TextView;
 import android.widget.Toast;
 
 import com.actionbarsherlock.app.SherlockActivity;
 
 /**
  * Initial dashboard activity that is shown to user when the app
  * is launched.
  * 
  * @author Karthik
  */
 public class HokieHelperActivity extends SherlockActivity {
 
     private final String DEGREE_SYMBOL = "\u00B0";
     private Handler handler = new Handler();
 
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.main);
         View l = findViewById(R.id.lin_layout);
         l.setBackgroundResource(R.drawable.mainpage_background);
         
         // Hide the action bar
         getSupportActionBar().hide();
 
         ImageView topPic = (ImageView)findViewById(R.id.topimage);
         topPic.setVisibility(View.INVISIBLE);
         ImageView dd = (ImageView)findViewById(R.id.img_dining);
         ImageView inf = (ImageView)findViewById(R.id.img_info);
         ImageView twf = (ImageView)findViewById(R.id.img_twitterfeed);
         ImageView maps = (ImageView)findViewById(R.id.img_maps);
         dd.setOnClickListener(new OnClickListener() {
             public void onClick(View v) {
                 launchActivity(0);
             }
         });
 
         maps.setOnClickListener(new OnClickListener() {
             public void onClick(View v) {
                 launchActivity(1);
             }
         });
 
         twf.setOnClickListener(new OnClickListener() {
             public void onClick(View v) {
                 launchActivity(2);
             }
         });
 
         inf.setOnClickListener(new OnClickListener() {
             public void onClick(View v) {
                 launchActivity(3);
             }
         });
 
         if (isNetworkAvailable()) {
             final LinearLayout weatherLayout = (LinearLayout) findViewById(R.id.weatherLayout);
             final Weather_XMLParser parser = new Weather_XMLParser();
             new Thread(new Runnable() {
                 public void run() {
                     parser.doCurrentConditions();
 
                     handler.post(new Runnable() {
                         public void run() {
                             String iconURL = parser.getCurrentWeather().getIcon();
                             ImageView imView = (ImageView) findViewById(R.id.weatherIcon);
                             TextView conditionTv = (TextView) findViewById(R.id.weatherCondition);
                             TextView temperatureTv = (TextView) findViewById(R.id.weatherHighLow);
                             weatherLayout.setOnClickListener(new OnClickListener() {
 
                                 /**
                                  * Listens for clicks on the image, opens the weather bug
                                  * website when pressed (Required by WeatherBug).
                                  */
                                 public void onClick(View v) {
                                     createWeather();
                                 }
                             });
 
                             // Gets the weather condition icon.
                             int icon = parser.getImageForCondition(iconURL);
                             String condition = parser.getCurrentWeather().getDescription();
                             int temperature = parser.getCurrentWeather().getTemperature();
 
                             if (icon != R.drawable.unknown) {
                                 imView.setImageResource(icon);
 
                             } else {
                                 // if you cant find an images, just dont show anything.
                                 imView.setVisibility(View.INVISIBLE);
                             }
                             if (condition != null) {
                                 conditionTv.setText(condition);
                                 temperatureTv.setText(temperature + DEGREE_SYMBOL);
 
                             }
                         }
                     });
                 }
             }).start();
         }
     }
     /**
      * Private method that fires an intent to start the weather display activity
      */
     private void createWeather() {
         if (isNetworkAvailable()) {
             Intent i = new Intent(HokieHelperActivity.this,
                     Weather_MainActivity.class);
             double[] infoArr = new double[2];
             infoArr[0] = Weather_XMLParser.LAT;
             infoArr[1] = Weather_XMLParser.LONG;
             i.putExtra("new_weather_request", infoArr);
             startActivity(i);
         }
         else {
             Toast.makeText(HokieHelperActivity.this, "No network connection available", Toast.LENGTH_SHORT).show();
         }
     }
 
     private void launchActivity(int position) {
         switch (position) {
         case 0:
             startActivity(new Intent(HokieHelperActivity.this, Dining_HallDietChooserActivity.class));
             break;
         case 1:
             startActivity(new Intent(HokieHelperActivity.this, Maps_MainActivity.class));
             break;
         case 2:
            if (isNetworkAvailable()) {
                startActivity(new Intent(HokieHelperActivity.this, Twitter_FeedActivity.class));
            } else {
                Toast.makeText(this, "No network connection available", Toast.LENGTH_LONG).show();
            }
             break;
         case 3:
             startActivity(new Intent(HokieHelperActivity.this, Info_MainActivity.class));
             break;
         }
 
     }
 
     /**
      * Checks if the network is available
      * 
      * @return
      */
     private boolean isNetworkAvailable() {
         ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
         NetworkInfo networkInfo = cm.getActiveNetworkInfo();
         // if no network is available networkInfo will be null, otherwise check
         // if we are connected
         if (networkInfo != null && networkInfo.isConnected()) {
             return true;
         }
         return false;
     }
 }
