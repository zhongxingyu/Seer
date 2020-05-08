 package com.ecn.urbapp.db;
 
 import android.content.ContentValues;
 import android.database.Cursor;
 
 import com.ecn.urbapp.activities.MainActivity;
 
 public class Element extends DataObject {
 
 	
 	//Atributes
 	/**
 	 * long id of the object element
 	 */
 	private long element_id;
 	//TODO Adddescription for javadoc
 	private long photo_id;
 	//TODO Adddescription for javadoc
 	private long material_id;
 	//TODO Adddescription for javadoc
 	private long elementType_id;
 	//TODO Adddescription for javadoc
 	private long pixelGeom_id;
 	//TODO Adddescription for javadoc
 	private long gpsGeom_id;
 	//TODO Adddescription for javadoc
 	private String element_color;
 	
 
 	//Getters
 	//TODO Adddescription for javadoc
 	public long getElement_id() {
 		return element_id;
 	}
 	
 	//TODO Adddescription for javadoc
 	public long getPhoto_id() {
 		return photo_id;
 	}
 	
 	//TODO Adddescription for javadoc
 	public long getMaterial_id() {
 		return material_id;
 	}
 	
 	//TODO Adddescription for javadoc
 	public long getElementType_id() {
 		return elementType_id;
 	}
 	
 	//TODO Adddescription for javadoc
 	public long getPixelGeom_id() {
 		return pixelGeom_id;
 	}
 	
 	//TODO Adddescription for javadoc
 	public long getGpsGeom_id() {
 		return gpsGeom_id;
 	}
 	
 	//TODO Adddescription for javadoc
 	public String getElement_color() {
 		return element_color;
 	}
 	
 
 	//Setters
 	//TODO Adddescription for javadoc
 	public void setElement_id(long element_id) {
 		this.element_id = element_id;
 	}
 
 	//TODO Adddescription for javadoc
 	public void setPhoto_id(long photo_id) {
 		this.photo_id = photo_id;
 	}
 
 	//TODO Adddescription for javadoc
 	public void setMaterial_id(long material_id) {
 		this.material_id = material_id;
 	}
 
 	//TODO Adddescription for javadoc
 	public void setElementType_id(long elementType_id) {
 		this.elementType_id = elementType_id;
 	}
 
 	//TODO Adddescription for javadoc
 	public void setPixelGeom_id(long pixelGeom_id) {
 		this.pixelGeom_id = pixelGeom_id;
 	}
 	
 	//TODO Generate javadoc
 	public void setGpsGeom_id(long gpsGeom_id) {
 		this.gpsGeom_id = gpsGeom_id;
 	}
 
 	//TODO Adddescription for javadoc
 	public void setElement_color(String element_color) {
 		this.element_color = element_color;
 	}
 
 	
 	
 	
 	//Abstract methods
 	//TODO Adddescription for javadoc
 	@Override
 	public String toString() {
 		return "Element [element_id=" + element_id + ", photo_id=" + photo_id
 				+ ", material_id=" + material_id + ", elementType_id="
 				+ elementType_id + ", pixelGeom_id=" + pixelGeom_id
 				+ ", gpsGeom_id=" + gpsGeom_id + ", element_color="
 				+ element_color + "]";
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
 
 		values.put(MySQLiteHelper.COLUMN_ELEMENTCOLOR, this.element_color);
 		
 		if(this.registredInLocal){
 			String[] s=new String[1];
 			s[0]= ""+this.element_id;
 			datasource.getDatabase().update(MySQLiteHelper.TABLE_ELEMENT, values, MySQLiteHelper.COLUMN_ELEMENTID,s );
 		}
 		else{
 			//TODO trigger
 			Cursor cursor = datasource.getDatabase().rawQuery(GETMAXELEMENTID, null);
 			cursor.moveToFirst();
 			if(!cursor.isAfterLast()){
				this.setElement_id(1+cursor.getLong(0));
 			}
 			values.put(MySQLiteHelper.COLUMN_ELEMENTID, this.element_id);
 			values.put(MySQLiteHelper.COLUMN_PHOTOID, this.photo_id);
 			values.put(MySQLiteHelper.COLUMN_MATERIALID, this.material_id);
 			values.put(MySQLiteHelper.COLUMN_ELEMENTTYPEID, this.elementType_id);
 			values.put(MySQLiteHelper.COLUMN_PIXELGEOMID, this.pixelGeom_id);
 			values.put(MySQLiteHelper.COLUMN_GPSGEOMID, this.gpsGeom_id);
 			datasource.getDatabase().insert(MySQLiteHelper.TABLE_ELEMENT, null, values);
 		}
 	}
 	/**
 	 * query to get the biggest Element_id from local db
 	 * 
 	 */
 	private static final String
 		GETMAXELEMENTID = 
 			"SELECT "+MySQLiteHelper.TABLE_ELEMENT+"."+MySQLiteHelper.COLUMN_ELEMENTID+" FROM "
 			+ MySQLiteHelper.TABLE_ELEMENT 
 			+" ORDER BY "+MySQLiteHelper.TABLE_ELEMENT+"."+MySQLiteHelper.COLUMN_ELEMENTID
 			+" DESC LIMIT 1 ;"
 		;
 	
 
 }
