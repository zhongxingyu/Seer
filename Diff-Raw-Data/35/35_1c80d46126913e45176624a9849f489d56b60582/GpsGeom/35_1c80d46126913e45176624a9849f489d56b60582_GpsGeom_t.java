 package com.ecn.urbapp.db;
 
 import java.util.ArrayList;
 
 import android.content.ContentValues;
 import android.database.Cursor;
 
 import com.ecn.urbapp.activities.MainActivity;
 
 public class GpsGeom extends DataObject{
 	
 	
 	//Attributes
 	//TODO Adddescription for javadoc
 	private long gpsGeom_id;
 	//TODO Adddescription for javadoc
 	private String gpsGeom_the_geom;
 
 	
 	
 	
 	//Getters
 	//TODO Adddescription for javadoc
 	public long getGpsGeomsId(){
 		return gpsGeom_id;
 	}
 
 	//TODO Adddescription for javadoc
 	public String getGpsGeomCord() {
 		return gpsGeom_the_geom;
 	}
 
 	
 	
 	
 	
 	//Setters
 	//TODO Adddescription for javadoc
 	public void setGpsGeomCoord(String str) {
 		this.gpsGeom_the_geom = str;
 	}
 
 	//TODO Adddescription for javadoc
 	public void setGpsGeomId(long id) {
 		this.gpsGeom_id = id;
 	}
 
 	
 	
 	
 	
 	
 	//Ovveride methods
 	//TODO Adddescription for javadoc
 	//will be used by the ArayAdapter in the ListView
 	@Override
 	public String toString() {
 		return "gpsGeom_id =" + this.gpsGeom_id + "&" + "\n gpsGeom_the_geom =" + this.gpsGeom_the_geom  + "&" + "\n coord =" + this.gpsGeom_id;
 		
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
 		
 		values.put(MySQLiteHelper.COLUMN_GPSGEOMCOORD, this.gpsGeom_the_geom);
 		
 		if(this.registredInLocal){
 			String[] s=new String[1];
 			s[0]= ""+this.gpsGeom_id;
 			datasource.getDatabase().update(MySQLiteHelper.TABLE_GPSGEOM, values, MySQLiteHelper.COLUMN_GPSGEOMID,s );
 		}
 		else{
 			
 			Cursor cursor = datasource.getDatabase().rawQuery(GETMAXGPSGEOMID, null);
 			cursor.moveToFirst();
 			if(!cursor.isAfterLast()){
 				long old_id = this.getGpsGeomsId();
				long new_id = 1+cursor.getLong(0);
 				this.setGpsGeomId(new_id);
 				this.trigger(old_id, new_id, MainActivity.photo, MainActivity.project, MainActivity.element);
 			}
 			values.put(MySQLiteHelper.COLUMN_GPSGEOMID, this.gpsGeom_id);
 			datasource.getDatabase().insert(MySQLiteHelper.TABLE_GPSGEOM, null, values);	
 		}
 	}		
 	/**
 	 * query to get the biggest GpsGeom_id from local db
 	 * 
 	 */
 	private static final String
 		GETMAXGPSGEOMID = 
 			"SELECT "+MySQLiteHelper.TABLE_GPSGEOM+"."+MySQLiteHelper.COLUMN_GPSGEOMID+" FROM "
 			+ MySQLiteHelper.TABLE_GPSGEOM 
 			+" ORDER BY "+ MySQLiteHelper.TABLE_GPSGEOM+"."+MySQLiteHelper.COLUMN_GPSGEOMID
 			+" DESC LIMIT 1 ;"
 		;
 	
 	
 	public void trigger(long old_id, long new_id, Photo photo, ArrayList<Project> list_projet, ArrayList<Element> list_element ){
 		if (photo.getGpsGeom_id() == old_id){
 			photo.setGpsGeom_id(new_id);
 		}
 		if (list_projet!=null){
 			for (Project p : list_projet){
 				if(p.getGpsGeom_id()==old_id){
 					p.setGpsGeom_id(new_id);
 				}
 			}
 			
 		}
 		if (list_element!=null){
 			for (Element e : list_element){
 				if(e.getGpsGeom_id()==old_id){
 					e.setGpsGeom_id(new_id);
 				}
 			}
 			
 		}
 		
 	}
 }
