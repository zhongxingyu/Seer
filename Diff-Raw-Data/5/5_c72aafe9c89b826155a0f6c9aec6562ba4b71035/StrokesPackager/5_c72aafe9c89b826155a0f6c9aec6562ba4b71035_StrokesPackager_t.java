 package net.solajpafistoj.tag.client;
 
 import java.util.ArrayList;
 
 import org.json.JSONArray;
 import org.json.JSONException;
 
 import android.os.Bundle;
 
 //convenient (un)packer for sending strokes thru Intents
 
 public class StrokesPackager extends Object{
 	
 	protected ArrayList<ArrayList<Integer>> strokes = null;
 
 	public static final String STROKE_COUNT = "StrokesPackager.STROKE_COUNT";
 	public static final String STROKE_STORAGE = "StrokesPackager.STROKE_STORAGE";
 	
 
 	public StrokesPackager(Bundle bundle){
 		assert(bundle != null);
 		int count = bundle.getInt(STROKE_COUNT, 0);
 		 
 		strokes = new ArrayList<ArrayList<Integer>>();
 		
 		for (int i = 0; i < count; i++){
 			ArrayList<Integer> stroke = new ArrayList<Integer>();
 			
 			int[] points = bundle.getIntArray(STROKE_STORAGE + i);
 			
			//points are stored in flat array, each coordinates as two subsequent items
 			
			int pointCount = points.length;
 			
 			for(int g = 0; g < pointCount ; g++){
 				stroke.add( points[g] );
 			}
 			
 			strokes.add(stroke);
 			
 		}
 	}
 	
 	public StrokesPackager(String data){
 		try {
 			JSONArray content = new JSONArray( data );	
 			
 			int count = content.length();
 			
 			strokes = new ArrayList<ArrayList<Integer>>();
 			
 			for (int i = 0; i < count; i++){
 					ArrayList<Integer> stroke = new ArrayList<Integer>();
 					
 					JSONArray dataStroke = content.getJSONArray(i);
 					
 					int pointCount = dataStroke.length();
 					
 					for(int g = 0; g < pointCount ; g++){
 							stroke.add( dataStroke.getInt(g) );
 					}
 					
 					strokes.add(stroke);
 			}
 		
 		} catch (JSONException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}			
 	}
     
 	
 	public  StrokesPackager(ArrayList<ArrayList<Integer>> lines){
 		strokes = lines;
 	}
 	
 	
 	
 	
 	public ArrayList<ArrayList<Integer>> getStrokes(){
 		assert(strokes != null);
 		return strokes;
 	}
 	
 	
 	public Bundle getBundle(){
 		assert(strokes != null);
 		
 		Bundle bundle = new Bundle();
 		
         int count = strokes.size();
 
         bundle.putInt(STROKE_COUNT, count);
 
         for (int i = 0; i < count; i++){
         	ArrayList<Integer> stroke = strokes.get(i);
         	int pointCount = stroke.size();
         	 
         	int[] flattened = new int[ pointCount ];
         	
         	int g=0;
         	for (Integer f:  strokes.get(i) ){
         		flattened[g++] = f;
 			}	
         	
         	bundle.putIntArray(STROKE_STORAGE + i ,  flattened );
         }
 
 		return bundle;
 	}
 
 	
 
 }
