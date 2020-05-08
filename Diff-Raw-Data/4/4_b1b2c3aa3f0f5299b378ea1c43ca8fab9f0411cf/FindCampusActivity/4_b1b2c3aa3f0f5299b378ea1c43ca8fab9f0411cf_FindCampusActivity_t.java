 package net.danopia.mobile.laundryview;
 
 import android.app.ListActivity;
 import android.content.Context;
 import android.content.Intent;
 import android.location.Location;
 import android.location.LocationManager;
 import android.net.Uri;
 import android.os.Bundle;
 import android.provider.Settings;
 import android.view.KeyEvent;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.view.inputmethod.EditorInfo;
 import android.widget.ArrayAdapter;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.ImageButton;
 import android.widget.ListView;
 import android.widget.TextView;
 
 import net.danopia.mobile.laundryview.data.AssistClient;
 import net.danopia.mobile.laundryview.structs.Assist.Campus;
 import net.danopia.mobile.laundryview.util.LoadingAdapter;
 import net.danopia.mobile.laundryview.util.MessageAdapter;
 
 import java.util.Collections;
 import java.util.List;
 
 /**
  * Created by daniel on 8/29/13.
  */
 public class FindCampusActivity extends ListActivity {
     private List<Campus> nearCampuses;
 
     @Override
     protected void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
 
         final LocationManager locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
         final Location loc = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
         Cache.location = loc;
 
         getListView().addHeaderView(View.inflate(this, R.layout.activity_find_campus_top, null));
         getListView().addFooterView(View.inflate(this, R.layout.activity_find_campus_bottom, null));
         getListView().setFooterDividersEnabled(false);
 
         ((ImageButton) findViewById(R.id.wifiButton)).setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View view) {
                 startActivity(new Intent(Settings.ACTION_WIFI_SETTINGS));
             }
         });
 
         final Button pathButton = (Button) findViewById(R.id.pathButton);
         final EditText pathText = (EditText) findViewById(R.id.pathText);
         pathButton.setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View view) {
                 final String entry = pathText.getText().toString();
 
                 if (entry.length() == 0) {
                     pathText.setError("Please enter the link which you were given");
                     pathText.requestFocus();
                 } else if (entry.replaceAll("[a-zA-Z0-9\\-]", "").length() > 0) {
                     pathText.setError("That link doesn't look right. It should be all letters/numbers, like 'upenn'");
                     pathText.requestFocus();
                 } else {
                     pathText.setError(null);
 
                     Uri uri = Uri.parse("http://laundryview.com/" + entry);
                     Intent intent = new Intent(Intent.ACTION_VIEW, uri, FindCampusActivity.this, LaunchActivity.class);
                     startActivity(intent);
                 }
             }
         });
 
         pathText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
             @Override
             public boolean onEditorAction(TextView textView, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_GO) {
                     pathButton.performClick();
                     return true;
                 }
                 return false;
             }
         });
 
         if (Cache.location == null) {
             setListAdapter(new MessageAdapter(this, "Can't find your current location"));
 
             new Thread(new Runnable() {
                 @Override
                 public void run() {
                     AssistClient.getCampuses(loc); // ping
                 }
             }).start();
 
         } else {
             setListAdapter(new LoadingAdapter(this));
 
             new Thread(new Runnable() {
                 @Override
                 public void run() {
                     final List<Campus> campuses = AssistClient.getCampuses(loc);
                     runOnUiThread(new Runnable() {
                         @Override
                         public void run() {
                             updateCampuses(campuses);
                         }
                     });
                 }
             }).start();
         }
     }
 
     void updateCampuses(List<Campus> campuses) {
         for (Campus campus : campuses) {
             campus.location = new Location("server");
             campus.location.setLatitude(campus.coords[0]);
             campus.location.setLongitude(campus.coords[1]);
 
             campus.distance = (int) (Cache.location.distanceTo(campus.location) / 1000);
         }
         Collections.sort(campuses);
 
         nearCampuses = campuses.subList(0, 5);
         getListView().setAdapter(new ArrayAdapter<Campus>(this, 0, nearCampuses) {
             @Override
             public View getView(int position, View convertView, ViewGroup parent) {
                 View view = convertView;
                 Campus campus = getItem(position);
 
                 if (view == null) {
                     LayoutInflater vi = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                     view = vi.inflate(R.layout.campus_list_item, parent, false);
                 }
 
                 ((TextView) view.findViewById(R.id.textName)).setText(campus.name);
                 ((TextView) view.findViewById(R.id.textDist)).setText(campus.distance + "km");
                 return view;
             }
         });
     }
 
     @Override
     protected void onListItemClick(ListView l, View v, int position, long id) {
         final Campus campus = nearCampuses.get(position - 1);
 
         Uri uri = Uri.parse("http://laundryview.com/" + campus.path);
         Intent intent = new Intent(Intent.ACTION_VIEW, uri, this, LaunchActivity.class);
         startActivity(intent);
     }
 
     static private class CampusAdapter extends ArrayAdapter<Campus> {
         public CampusAdapter(Context context, List<Campus> objects) {
             super(context, 0, objects);
         }
 
         @Override
         public View getView(int position, View convertView, ViewGroup parent) {
             View view = convertView;
             Campus campus = getItem(position);
             if (view == null) {
                 LayoutInflater vi = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                 view = vi.inflate(R.layout.campus_list_item, parent, false);
             }
 
             ((TextView) view.findViewById(R.id.textName)).setText(campus.name);
             ((TextView) view.findViewById(R.id.textDist)).setText(campus.distance + "km");
             return view;
         }
     }
 }
