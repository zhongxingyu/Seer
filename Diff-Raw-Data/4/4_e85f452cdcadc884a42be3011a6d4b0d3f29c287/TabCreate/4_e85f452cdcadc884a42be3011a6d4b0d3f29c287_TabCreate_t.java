 package com.datakom;
 
 import com.datakom.R;
 import com.datakom.POIObjects.HaggleConnector;
 import com.datakom.POIObjects.ObjectTypes;
 import com.datakom.POIObjects.POIObject;
 import com.google.android.maps.GeoPoint;
 
 import android.os.Bundle;
 import android.util.Log;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.ArrayAdapter;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.Spinner;
 import android.widget.Toast;
 import android.app.Activity;
 import android.content.Intent;
 
 public class TabCreate extends Activity {
 	private String filepath;
 	
 	EditText e_title;
 	Spinner s_type;
 	EditText e_description;
 	Spinner s_rating;
 	
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.createtabview);
 		
 		e_title = (EditText) findViewById(R.id.txt_title);
 		s_type = (Spinner) findViewById(R.id.spinner_type);
 		e_description = (EditText) findViewById(R.id.txt_description);
 		s_rating = (Spinner) findViewById(R.id.spinner_rating); 
 		ArrayAdapter adapter1 = ArrayAdapter.createFromResource(
 				this, R.array.types, android.R.layout.simple_spinner_item);
 		adapter1.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
 		s_type.setAdapter(adapter1);
 
 	  ArrayAdapter adapter = ArrayAdapter.createFromResource(
 	            this, R.array.rating, android.R.layout.simple_spinner_item);
 	    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
 	    s_rating.setAdapter(adapter);
 	  
 	  Button create = (Button) findViewById(R.id.save);
 	  Button add_picture = (Button) findViewById(R.id.add_picture);
 	  
 	  add_picture.setOnClickListener(new OnClickListener() {
 		  public void onClick(View v) {
 			  Intent mycamera = new Intent(v.getContext(), CameraMain.class);
 			  filepath = null; //reset last taken picture each time camera is started
 			  startActivityForResult(mycamera, 1000);
 		  }
 	  });
 	    
 	    create.setOnClickListener(new OnClickListener() {
 	    	public void onClick(View v) {
 	    		String title = e_title.getText().toString();
 	    		String description = e_description.getText().toString();
 	    		String type = (String) s_type.getSelectedItem();
 	    		String rating = (String) s_rating.getSelectedItem();
 	    		GeoPoint p = GpsLocation.getInstance(getApplicationContext()).getCurrentPoint();
 
 	    		if (filepath == null) {
 	    			Toast.makeText(TabCreate.this, "Take a picture before trying to create", Toast.LENGTH_SHORT).show();
 	    			return;
 	    		}
 	    		if (p == null) {
 	    			Toast.makeText(TabCreate.this, "Couldn't fetch GPS coordinates, try again later", Toast.LENGTH_SHORT).show();
 	    			return;
 	    		}
 	    		if (type == null || type.length() <= 0) {
 	    			Log.d("BAJS", "IS IT BAJS:" + s_type.getSelectedItem());
 	    			Toast.makeText(TabCreate.this, "You forgot to choose type", Toast.LENGTH_SHORT).show();
 	    			return;
 	    		}
 	    		if (rating == null || rating.length() <= 0) {
 	    			Toast.makeText(TabCreate.this, "You forgot to choose rating", Toast.LENGTH_SHORT).show();
 	    			return;
 	    		}
 	    		if (title == null || title.length() <= 0) {
 	    			Toast.makeText(TabCreate.this, "You forgot to fill in title", Toast.LENGTH_SHORT).show();
 	    			return;
 	    		}
 	    		if (description == null || description.length() <= 0) {
 	    			Toast.makeText(TabCreate.this, "You forgot to fill in description", Toast.LENGTH_SHORT).show();
 	    			return;
 	    		}
 	    		
 	    		int int_type = ObjectTypes.OTHER;
 	    		if (type.compareTo("Resturant") == 0) {
 	    			int_type = ObjectTypes.RESTURANT;
 	    		} else if(type.compareTo("Pub") == 0) {
 	    			int_type = ObjectTypes.PUB;
 	    		} 
 	    		
 	    		Log.d(getClass().getSimpleName(), "Latitude: " + p.getLatitudeE6() + ", Longitude: " + p.getLongitudeE6());
 	    		Log.d(getClass().getSimpleName(), filepath);
 	    		Log.d(getClass().getSimpleName(), title);
 	    		Log.d(getClass().getSimpleName(), description);
 	    		Log.d(getClass().getSimpleName(), "" + int_type);
 	    		Log.d(getClass().getSimpleName(), rating);
 	    		
 	    		POIObject o = new POIObject(int_type, filepath, Double.parseDouble(rating), title, description, p);
         		int status = HaggleConnector.getInstance().pushPOIObject(o);
 	    		
         		if (status < 0) {
         			Toast.makeText(TabCreate.this, "Couldn't push down object", Toast.LENGTH_SHORT).show();
         			return;
         		}
         		
         		Toast.makeText(TabCreate.this, "POIObject created and pushed down", Toast.LENGTH_SHORT).show();
 	    		
 	    		//reset all fields
 	    		filepath = null;
 	    		e_title.setText("");
 	    		e_description.setText("");
 	    	}
          });
 	}
 	
 	protected void onActivityResult(int reqCode, int resCode, Intent data) {
		if (data == null) {
			return;
		}
		
 		filepath = (String) data.getExtras().get("filename");
 	}
 }
