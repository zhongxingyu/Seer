 
 package edu.vanderbilt.vm.noderegister;
 
 import java.io.File;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.util.Random;
 
 import com.google.android.gms.maps.CameraUpdateFactory;
 import com.google.android.gms.maps.GoogleMap;
 import com.google.android.gms.maps.SupportMapFragment;
 import com.google.android.gms.maps.model.LatLng;
 
 import android.location.Location;
 import android.os.Bundle;
 import android.os.Environment;
 import android.support.v4.app.FragmentActivity;
 import android.support.v4.app.FragmentManager;
 import android.support.v4.app.FragmentTransaction;
 import android.util.JsonWriter;
 import android.view.Menu;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.Button;
 import android.widget.Toast;
 
 /**
  * <p>
  * A utility app to register Nodes into nodes.json.
  * </p>
  * <p>
  * Preparation for usage: turn on GPS and 3G; Make sure you're outdoor with
  * clear view of the sky; Make sure the phone is not plugged in to your laptop,
  * because it needs access to the SD card.
  * </p>
  * <p>
  * Usage: Tap the left button at the bottom of the screen; You'll get a message
  * showing the filename being used, where the node information will be stored;
  * This filename is randomly generated; Walk to a Point of physical
 * significance; tap on the right button with the label "Register"; You'll get
 * a message showing the Id of the Node you've just registered; Continue until
  * you're done; Tap on the left button again to end the session;
  * </p>
  * <p>
  * Final note: Each registering session will generate a .json file with a
  * randomly generated file name. Use the Python script that I wrote to merge
  * multiple .json files together to make a master list.
  * </p>
  * @author athran
  */
 public class Register extends FragmentActivity implements OnClickListener {
 
     private Button mBtn1;
 
     private Button mBtn2;
 
     private static final String MAP = "map";
 
     private static final String NODES = "/nodes";
 
     private static final String LAT = "latitude";
 
     private static final String LNG = "longitude";
 
     private static final String ID = "id";
 
     private static final String NEIGH = "neighbours";
 
     private static final int FIRST_ID = 10000;
 
     private boolean isRegistering;
 
     private int mLastId;
 
     private JsonWriter mJw;
 
     @Override
     protected void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.activity_register);
 
         // Setup UI
         mBtn1 = (Button)findViewById(R.id.btn1);
         mBtn1.setText("Toggle");
         mBtn1.setOnClickListener(this);
 
         mBtn2 = (Button)findViewById(R.id.btn2);
         mBtn2.setText("Register");
         mBtn2.setOnClickListener(this);
 
         // Setup MapFragment
         FragmentManager fm = getSupportFragmentManager();
         FragmentTransaction ft = fm.beginTransaction();
 
         ft.add(R.id.map_container, new SupportMapFragment(), MAP);
         ft.commit();
 
         // Initialization
         isRegistering = false;
         mLastId = FIRST_ID;
     }
 
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         // Inflate the menu; this adds items to the action bar if it is present.
         getMenuInflater().inflate(R.menu.activity_register, menu);
         return true;
     }
 
     @Override
     public void onResume() {
         super.onResume();
 
         SupportMapFragment frag = (SupportMapFragment)getSupportFragmentManager()
                 .findFragmentByTag(MAP);
 
         GoogleMap map = frag.getMap();
         map.setMyLocationEnabled(true);
         map.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(36.144782, -86.803231), 16));
 
         isRegistering = true;
         toggleRegistering();
     }
 
     @Override
     public void onClick(View v) {
         switch (v.getId()) {
 
             case R.id.btn1:
                 toggleRegistering();
                 return;
 
             case R.id.btn2:
                 registerNode();
                 return;
 
             default:
                 return;
 
         }
 
     }
 
     @Override
     public void onPause() {
         super.onPause();
         ((SupportMapFragment)getSupportFragmentManager().findFragmentByTag(MAP)).getMap()
                 .setMyLocationEnabled(false);
         isRegistering = true;
         toggleRegistering();
     }
 
     private void toggleRegistering() {
 
         if (isRegistering) {
             // close everything
             try {
            	mJw.endArray();
                 mJw.close();
             } catch (IOException e) {
                 e.printStackTrace();
             } catch (NullPointerException e) {
                 e.printStackTrace();
             }
 
             mBtn1.setText("OFF");
             Toast.makeText(this, "Registering ended" + mLastId, Toast.LENGTH_SHORT).show();
             isRegistering = !isRegistering;
 
         } else {
             // Generate a random file name
             String path = Environment.getExternalStorageDirectory().getAbsolutePath() + NODES;
 
             Random rand = new Random();
             path += rand.nextInt(1000000);
             path += ".json";
 
             Toast.makeText(this, path, Toast.LENGTH_SHORT).show();
 
             // Setup Json writer
             FileWriter fw = null;
             try {
                 fw = new FileWriter(new File(path));
 
             } catch (IOException e) {
                 e.printStackTrace();
             }
 
             mJw = new JsonWriter(fw);
             mJw.setIndent("   ");
             try {
                 mJw.beginArray();
 
             } catch (IOException e) {
                 e.printStackTrace();
             }
             mBtn1.setText("ON");
             isRegistering = !isRegistering;
         }
     }
 
     private void registerNode() {
 
         if (isRegistering) {
 
             Location loc = ((SupportMapFragment)getSupportFragmentManager().findFragmentByTag(MAP))
                     .getMap().getMyLocation();
             try {
                 mJw.beginObject();
 
                 mJw.name(ID).value(mLastId);
                 mJw.name(LAT).value(loc.getLatitude());
                 mJw.name(LNG).value(loc.getLongitude());
                 mJw.name(NEIGH);
                 mJw.beginArray();
                 mJw.endArray();
 
                 mJw.endObject();
                 Toast.makeText(this, "Registered: " + mLastId, Toast.LENGTH_SHORT).show();
             } catch (IOException e) {
                 e.printStackTrace();
             } catch (NullPointerException e) {
                 e.printStackTrace();
                 Toast.makeText(this, "I think the MapFragment doesn't have a Location yet",
                         Toast.LENGTH_SHORT).show();
             }
 
             mLastId++;
         } else {
             Toast.makeText(this, "Please initiate registering first", Toast.LENGTH_SHORT).show();
         }
     }
 
 }
