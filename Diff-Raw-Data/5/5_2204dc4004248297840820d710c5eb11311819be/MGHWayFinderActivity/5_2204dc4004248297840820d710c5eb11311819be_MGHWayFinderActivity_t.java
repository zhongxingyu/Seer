 package com.MGHWayFinder;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Hashtable;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.database.SQLException;
 import android.os.Bundle;
 import android.widget.ArrayAdapter;
 import android.widget.Button;
 import android.widget.ImageView;
 import android.widget.Spinner;
 import android.widget.TextView;
 import android.widget.Toast;
 import android.util.Log;
 import android.view.View;
 import android.view.View.OnClickListener;
 
 public class MGHWayFinderActivity extends Activity {
     /** Called when the activity is first created. */
 	//hello git
 	Spinner start, end;
 	TextView tvPath;
 	ImageView ivPath;
 	Button go;
 	Button startQR;
 	Button endSet;
 	String startnID;
 	
 	Dijkstra dPath;
 	String sPath;
 	ArrayAdapter<Node> aAdapter;
 	DBHelper db;
     ArrayList<Node> aFloor = new ArrayList<Node>();
     ArrayList<Node> aPath;
     Hashtable<String, Node> hash;
     //ArrayList<Node> bFloor = new ArrayList<Node>();
 	
 	
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.test);
         
         db = new DBHelper(this.getApplicationContext());
         initializeDB();
         hash = db.buildFloorNodes(1);
         for(Node n:hash.values())
         	aFloor.add(n);
         
 ///////////////////UI ELEMENTS////////////////////////
         start = (Spinner)findViewById(R.id.startSpin);
         end = (Spinner)findViewById(R.id.endSpin);
         tvPath = (TextView)findViewById(R.id.tvNP);
         ivPath = (ImageView)findViewById(R.id.imageView);
         go = (Button)findViewById(R.id.goButton);
         
         aAdapter = new ArrayAdapter<Node>(this, android.R.layout.simple_spinner_item, aFloor);
         start.setAdapter(aAdapter);
         end.setAdapter(aAdapter);
         
         go.setOnClickListener(new OnClickListener(){
         	public void onClick(View v){
         		calculatePath();
         	}}); 
         
         //scan buttons
     	startQR = (Button)findViewById(R.id.scanStart);
     	startQR.setOnClickListener(new OnClickListener(){
     	    public void onClick(View v) {
     	    	Intent scanStart = new Intent("com.google.zxing.client.android.SCAN");   
     	        scanStart.putExtra("com.google.zxing.client.android.SCAN.SCAN_MODE", "QR_CODE_MODE");
     	        startActivityForResult(scanStart, 0);
     	    }});
 
     	//do we need an end button??
     	//no end scan, end context menu
     	//COMING SOON
    	endSet = (Button)findViewById(R.id.setEnd);
         endSet.setOnClickListener(new OnClickListener(){
         	public void onClick(View v){
         		contextDestination();
         	}});
     	
 
     }//end of oncreate
     
     
     //receive scan result back from scanner intent
     @Override
     public void onActivityResult(int requestCode, int resultCode, Intent scanStart) {
         if (requestCode == 0) {
             if (resultCode == Activity.RESULT_OK) {
                 String startnID = scanStart.getStringExtra("SCAN_RESULT");	//get the result from extra
                 //test code
                Log.v("QR", startnID);
                 
                 //set spinner
             	for(int i=0; i < aFloor.size(); i++){
             		if(startnID.equals(aFloor.get(i).getNodeID())){
             			start.setSelection(i);
             		}
             	} 
             } else if (resultCode == Activity.RESULT_CANCELED) {
             	Toast.makeText(this, "Scan Cancelled", Toast.LENGTH_LONG).show();
             }
         }
     }
     
     	
     private void calculatePath() {
 		dPath = new Dijkstra((Node)start.getSelectedItem());
 		aPath = dPath.getPath((Node)end.getSelectedItem());
 
 		sPath = aPath.get(0).getNodeID();
 		
 		for(int i = 1; i < aPath.size(); i++){
 			sPath += " -> " + aPath.get(i).getNodeID();
 		}
 		
 		tvPath.setText(sPath);
 	}
     
     private synchronized void initializeDB(){
     	try { 
         	db.createDataBase();
         } 
         catch (IOException ioe) {
         	throw new Error("Unable to create database");
         }
  
         try {
         	db.openDataBase();
         } 
         catch(SQLException sqle){
         	throw sqle;
         }
     }
     
     public void contextDestination(){
     	//use this and onclick leading to it to create and open context menu with
     	//end destinations
     	Toast.makeText(this, "Context Menu", Toast.LENGTH_LONG).show();
     }
     
     
 }//end of class
