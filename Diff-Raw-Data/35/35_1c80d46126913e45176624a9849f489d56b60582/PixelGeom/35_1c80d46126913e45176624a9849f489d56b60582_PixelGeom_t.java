 package com.ecn.urbapp.db;
 
 import java.util.ArrayList;
 
 import android.content.ContentValues;
 import android.database.Cursor;
 
 import com.ecn.urbapp.activities.MainActivity;
 
 public class PixelGeom extends DataObject  {
 
 	
 	//Attributes
 	//TODO Adddescription for javadoc
 	private long pixelGeom_id;
 	private String pixelGeom_the_geom;
 
 	
 	//TODO rendre private et mettre accesseur
 	public boolean selected;
 
 	
 	
 
 
 
 
 	//Getters
 	//TODO Adddescription for javadoc
 	public long getPixelGeomId(){
 		return pixelGeom_id;
 	}
 	
 	public boolean isSelected() {
 		return selected;
 	}
 	
 	//TODO Adddescription for javadoc
 	public String getPixelGeom_the_geom() {
 		return pixelGeom_the_geom;
 	}
 	
 	
 	
 	
 	
 	
 	//Setters
 	//TODO Adddescription for javadoc
 	public void setPixelGeomId(long id) {
 		this.pixelGeom_id = id;
 	}
 	
 	public void setSelected(boolean selected) {
 		this.selected = selected;
 	}
 
 	//TODO Adddescription for javadoc
 	public void setPixelGeom_the_geom(String pixelGeom_the_geom) {
 		this.pixelGeom_the_geom = pixelGeom_the_geom;
 	}
 
 	
 	
 	//Override methods
 	//TODO Adddescription for javadoc
 	//will be used by the ArayAdapter in the ListView
 	@Override
 	public String toString() {
 		return "pixelGeom_id =" + this.pixelGeom_id + "&" + "\ncoord =" + this.pixelGeom_the_geom ;
 		
 	}
 
 	@Override
 	public long getId() {
 		// TODO Auto-generated method stub
 		return 0;
 	}
 
 	@Override
 	public long setId() {
 		// TODO Auto-generated method stub
 		return 0;
 	}
 
 	@Override
 	public void saveToLocal(LocalDataSource datasource) {
 		ContentValues values = new ContentValues(); 
 		
 		values.put(MySQLiteHelper.COLUMN_PIXELGEOMCOORD, this.pixelGeom_the_geom);
 		
 		if(this.registredInLocal){
 			String[] s=new String[1];
 			s[0]= ""+this.pixelGeom_id;
 			datasource.getDatabase().update(MySQLiteHelper.TABLE_PIXELGEOM, values, MySQLiteHelper.COLUMN_PIXELGEOMID,s );
 		}
 		else{
 			//TODO trigger
 			Cursor cursor = datasource.getDatabase().rawQuery(GETMAXPIXELGEOMID, null);
 			cursor.moveToFirst();
 			if(!cursor.isAfterLast()){
 				long old_id = this.getPixelGeomId();
				long new_id = 1+cursor.getLong(0);
 				this.setPixelGeomId(new_id);
 				this.trigger(old_id, new_id, MainActivity.element);
 				
 			}
 			values.put(MySQLiteHelper.COLUMN_PIXELGEOMID, this.pixelGeom_id);
 			datasource.getDatabase().insert(MySQLiteHelper.TABLE_PIXELGEOM, null, values);
 		}
 	}
 	/**
 	 * query to get the biggest photo_id from local db
 	 * 
 	 */
 	private static final String
 		GETMAXPIXELGEOMID = 
 			"SELECT "+MySQLiteHelper.TABLE_PIXELGEOM+"."+MySQLiteHelper.COLUMN_PIXELGEOMID+" FROM "
 			+ MySQLiteHelper.TABLE_PIXELGEOM 
 			+" ORDER BY "+MySQLiteHelper.TABLE_PIXELGEOM+"."+MySQLiteHelper.COLUMN_PIXELGEOMID
 			+" DESC LIMIT 1 ;"
 		;
 
 	public void trigger(long old_id, long new_id,  ArrayList<Element> list_element){
 		if (list_element!=null){
 			for (Element e : list_element){
 				if(e.getPixelGeom_id()==old_id){
 					e.setPixelGeom_id(new_id);
 				}
 			}
 			
 		}
 		
 	}
 }
