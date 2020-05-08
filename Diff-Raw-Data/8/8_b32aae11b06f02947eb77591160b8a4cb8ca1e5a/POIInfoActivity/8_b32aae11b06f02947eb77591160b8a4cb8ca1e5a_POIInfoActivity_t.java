 package com.example.climbxpert;
 
 import com.example.climbxpert.POI.POI;
 import com.parse.Parse;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.View;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.ImageView;
 import android.widget.Spinner;
 import android.widget.TextView;
 
 public class POIInfoActivity extends Activity {
  
 	private Button poi_btn_foot_nav, poi_btn_navigate;
 	private TextView name;
 	private TextView info;
 	private TextView routes;
 	private ImageView imgId;
 	private int pid;
 	
 	public void onCreate(Bundle unused) { 
 		super.onCreate(unused); 
 		
 		Intent intent = getIntent();
 		
 		pid = intent.getIntExtra("pid",-1);
 		POI poi = ClimbXpertData.getPOI(pid);
 		
 		setContentView(R.layout.poi_info_layout);
 		poi_btn_navigate = (Button)findViewById(R.id.nav_btn);
 		poi_btn_foot_nav = (Button)findViewById(R.id.foot_btn);
 		imgId = (ImageView)findViewById(R.id.poi_img);
 		name = (TextView)findViewById(R.id.poi_name);
 		info = (TextView)findViewById(R.id.poi_info);
 		routes = (TextView)findViewById(R.id.poi_routes);
		setTitle(poi.name);
 		
 		name.setText(poi.name);
 		info.setText(poi.info);
 		routes.setText("Number Of Routes: "+String.valueOf(poi.routes.size()));
 		
 		poi_btn_navigate.setOnClickListener( new View.OnClickListener() {
 			
 			@Override
 			public void onClick(View arg0) {
 				
 				
 			}
 		});
 				
 		poi_btn_foot_nav.setOnClickListener(new View.OnClickListener() {
 			@Override
 			public void onClick(View arg0) {
 				onclickNavigate(arg0);
 				finish();
 			}
 		});	
 			
 	} 	
 	
 	public void onclickNavigate(View arg0) {
 		Intent intent = new Intent(this,NavigateActivity.class); 
 		intent.putExtra("pid",pid);
 		startActivityForResult(intent, 0); 
 		finish();
 	}
 }
