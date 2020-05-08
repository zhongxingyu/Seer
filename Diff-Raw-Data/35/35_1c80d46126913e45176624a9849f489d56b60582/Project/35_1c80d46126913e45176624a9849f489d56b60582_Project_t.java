 package com.ecn.urbapp.db;
 
 import java.util.ArrayList;
 
 import android.content.ContentValues;
 import android.database.Cursor;
 
 import com.ecn.urbapp.activities.MainActivity;
 
 
 public class Project extends DataObject {
 
 	
 	//Attributes
 	//TODO Adddescription for javadoc
 	private long project_id;
 	private String project_name;
 	private long gpsGeom_id;
 	private String Ext_GpsGeomCoord;
 
 	
 	
 	
 	
 	//Getters
 	//TODO Adddescription for javadoc
 	public String getExt_GpsGeomCoord() {
 		return Ext_GpsGeomCoord;
 	}
 	
 	//TODO Adddescription for javadoc
 	public long getGpsGeom_id() {
 		return gpsGeom_id;
 	}
 	
 	//TODO Adddescription for javadoc
 	public long getProjectId(){
 		return project_id;
 	}
 	
 	//TODO Adddescription for javadoc
 	public String getProjectName() {
 		return project_name;
 	}
 	
 	
 	
 	
 	
 	//Setters
 	//TODO Adddescription for javadoc
 	public void setExt_GpsGeomCoord(String ext_GpsGeomCoord) {
 		Ext_GpsGeomCoord = ext_GpsGeomCoord;
 	}
 
 	//TODO Adddescription for javadoc
 	public void setGpsGeom_id(long gpsGeom_id) {
 		this.gpsGeom_id = gpsGeom_id;
 	}
 
 	//TODO Adddescription for javadoc
 	public void setProjectName(String str) {
 		this.project_name = str;
 	}
 
 	//TODO Adddescription for javadoc
 	public void setProjectId(long id) {
 		this.project_id = id;
 	}
 
 	
 	
 	//Override methods
 	//TODO Adddescription for javadoc
 	//will be used by the ArayAdapter in the ListView
 	@Override
 	public String toString() {
 		return "project_id =" + this.project_id + "&" + " name =" + this.project_name  + "&" + "  position =" + this.gpsGeom_id + "&" + "  position =" + this.Ext_GpsGeomCoord;
 		//print the uuid, need a query to get a position
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
 		
 		values.put(MySQLiteHelper.COLUMN_PROJECTNAME, this.project_name);
 		
 			
 		if(this.registredInLocal){
 			String[] s=new String[1];
 			s[0]= ""+this.project_id;
 			datasource.getDatabase().update(MySQLiteHelper.TABLE_PROJECT, values, MySQLiteHelper.COLUMN_PROJECTID,s );
 		}
 		else{
 			//TODO trigger
 			Cursor cursor = datasource.getDatabase().rawQuery(GETMAXPROJECTID, null);
 			cursor.moveToFirst();
 			if(!cursor.isAfterLast()){
 				long old_id = this.getProjectId();
				long new_id = 1+cursor.getLong(0);
 				this.setProjectId(new_id);
 				this.trigger(old_id, new_id, MainActivity.composed);
 			}
 			values.put(MySQLiteHelper.COLUMN_PROJECTID, this.project_id);
 			values.put(MySQLiteHelper.COLUMN_GPSGEOMID, this.gpsGeom_id);
 			datasource.getDatabase().insert(MySQLiteHelper.TABLE_PROJECT, null, values);
 		}
 	}	
 	/**
 	 * query to get the biggest photo_id from local db
 	 * 
 	 */
 	private static final String
 		GETMAXPROJECTID = 
 			"SELECT "+MySQLiteHelper.TABLE_PHOTO+"."+MySQLiteHelper.COLUMN_PHOTOID+" FROM "
 			+ MySQLiteHelper.TABLE_PHOTO 
 			+" ORDER BY "+MySQLiteHelper.TABLE_PHOTO+"."+MySQLiteHelper.COLUMN_PHOTOID
 			+" DESC LIMIT 1 ;"
 		;
 	
 	public void trigger(long old_id, long new_id, ArrayList<Composed> list_composed ){
 
 		if (list_composed!=null){
 			for (Composed c : list_composed){
				if(c.getProject_id()==old_id){
					c.setProject_id(new_id);
 				}
 			}
 			
 		}
 		
 	}
 	
 }
