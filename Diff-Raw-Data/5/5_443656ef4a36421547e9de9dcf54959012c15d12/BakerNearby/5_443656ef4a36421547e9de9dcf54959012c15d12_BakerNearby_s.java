 package cmuHCI.WalkyScotty;
 
 import java.io.IOException;
 import java.util.Set;
 
 import android.content.Context;
 import android.content.Intent;
 import android.os.Bundle;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.BaseAdapter;
 import android.widget.GridView;
 import android.widget.ImageView;
 import android.widget.TextView;
 import cmuHCI.WalkyScotty.entities.Location;
 import cmuHCI.WalkyScotty.util.MyDirectedGraph;
 import cmuHCI.WalkyScotty.util.WeightedEdge;
 
 public class BakerNearby extends WSActivity{
 	private MyDirectedGraph<Location, WeightedEdge<Location>> graph;
 	private String[] names;
 	private Location[] locations;
 	
     /** Called when the activity is first created. */
     @Override
 	public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.bakernearby);
         
         loadNearby(getIntent().getIntExtra("lID", 1));
         
         // Set title of the page
         TextView title = (TextView) findViewById(R.id.NearbyText);
         title.setText(R.string.WhatNearby);
         
         // Set up GridView
         GridView gridview = (GridView) findViewById(R.id.gridview);
         gridview.setAdapter(new ImageAdapter(this));
 
         gridview.setOnItemClickListener(new OnItemClickListener() {
             public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
                 //Toast.makeText(BakerNearby.this, "" + position, Toast.LENGTH_SHORT).show();
             	navigateDetailsPage(locations[position].getId());
             }
         });
     }
     
     private void navigateDetailsPage(int locID){
 		if(locID <= 0)
 			throw new RuntimeException("Bad Location ID");
 		
 		Intent i = new Intent(this, BakerInfo.class);
 		i.putExtra("lID", locID);
 		this.startActivity(i);
 	}
     
     public void loadNearby(int locID){
     	Set<Location> locs;
     	
     	DBAdapter adp = new DBAdapter(this);
 		try {
 			adp.createDataBase();
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		adp.openDataBase();
 		
 		graph = adp.getGraph();
 		Location thisL = adp.getLocation(locID);
 		
 		TextView bc = (TextView) findViewById(R.id.bakernearby_breadcrumb_building);
 		bc.setText(thisL.getName());
 			
 		locs = graph.outgoingNeighbors(new Location(locID, null, null, null));
 		
 		if(locs == null){
 			names = new String[0];
 			locations = new Location[0];
 		}
 		else{
 		
 			names = new String[locs.size()];
 			locations = new Location[locs.size()];
 			
 			int i = 0;
 			for(Location l : locs){
 				Location h = adp.getLocation(l.getId());
 				names[i] = h.getName();
 				locations[i] = h;
 				i++;
 			}
 		}
 		
 		adp.close();
     	
     }
 
     public class ImageAdapter extends BaseAdapter {
         private Context mContext;
 
         public ImageAdapter(Context c) {
             mContext = c;
         }
 
         public int getCount() {
             return names.length;
         }
         
         // To do: return actual object
         public Object getItem(int position) {
             return null;
         }
 
         // To do: return row id of the item
         public long getItemId(int position) {
             return 0;
         }
 
         // create a new Layout which contains ImageView and it Text for each item referenced by the Adapter
         public View getView(int position, View convertView, ViewGroup parent) {
         	View v;
         	if(convertView==null){
         		LayoutInflater li = getLayoutInflater();
         		v = li.inflate(R.layout.gridbox, null);
         		TextView text = (TextView) v.findViewById(R.id.icon_text);
                 text.setText(names[position]);
                 
                 ImageView image = (ImageView)v.findViewById(R.id.icon_image);
                 image.setImageResource(mThumbIds[position % 2]);
                 //image.setLayoutParams(new GridView.LayoutParams(85, 85));
                 image.setScaleType(ImageView.ScaleType.FIT_CENTER);
                 image.setPadding(8, 8, 8, 8);
             } 
         	else {
                 v = convertView;
             }
         	return v;
         }
 
         private String[] mThumbText = {
         		"Baker", "Hunt"
         };
         
         // references to our images
         private Integer[] mThumbIds = {
         		R.drawable.baker_hall_1, R.drawable.hunt_lib_1
         };
     }
     
 }
