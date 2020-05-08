 package org.cvltvre.vo;
 
 import org.cvltvre.utils.CustomLocationListener;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import android.content.Context;
 import android.graphics.Bitmap;
 import android.location.Location;
 import android.location.LocationManager;
 import android.util.Log;
 
 /**
  * @author Pencerval
  *
  */
 public class MuseumVO{
 	
 	private String id;
 	private String phone;
 	private String title;
 	private String fax;
 	private String website;
 	private String address;
 	private String description;
 	private String subtype;
 	private String image;
 	private String contact;
 	private String url;
 	private String map_options;
 	private String emailid;
 	private String distance;
 	private Bitmap bitmap;
 	
 	/**
 	 * 
 	 * Constructor with JSONObject
 	 * 
 	 */
 	public MuseumVO(JSONObject museum,Context context){
 		try {
 			this.setId(museum.getString("guid"));
 			this.setPhone(museum.getString("phone"));
 			this.setTitle(museum.getString("title"));
 			this.setFax(museum.getString("fax"));
 			this.setWebsite(museum.getString("website"));
			this.setAddress(museum.getString("address").replace("<br>", ", ").replace("<br/>", ", ").replace("<BR>", ", ").replace(", , ", ", ").replace(", , ", ", "));
 			this.setDescription(museum.getString("description"));
 			this.setSubtype(museum.getString("subtype"));
 			this.setContact(museum.getString("contact"));
 			this.setUrl(museum.getString("url"));
 			this.setMap_options(museum.getString("map_options"));
 			this.setEmailid(museum.getString("emailid"));
 			if(museum.has("image")){
 				//this.bitmap=RestConnector.getThumb(museum.getString("image"), context);
 				this.setImage(museum.getString("image"));
 			}
 		} catch (JSONException e) {
 			try {
 				Log.e("MuseumVO", "Unable to build museum with id: "+museum.getString("guid"));
 			} catch (JSONException e1) {
 				Log.e("MuseumVO", "Fatal error at object "+museum.toString());
 				e1.printStackTrace();
 			}
 			e.printStackTrace();
 		}
 	}
 
 	/**
 	 * @return the id
 	 */
 	public String getId() {
 		return id;
 	}
 
 	/**
 	 * @param id the id to set
 	 */
 	public void setId(String id) {
 		this.id = id;
 	}
 
 	/**
 	 * @return the phone
 	 */
 	public String getPhone() {
 		return phone;
 	}
 
 	/**
 	 * @param phone the phone to set
 	 */
 	public void setPhone(String phone) {
 		this.phone = phone;
 	}
 
 	/**
 	 * @return the title
 	 */
 	public String getTitle() {
 		return title;
 	}
 
 	/**
 	 * @param title the title to set
 	 */
 	public void setTitle(String title) {
 		this.title = title;
 	}
 
 	/**
 	 * @return the fax
 	 */
 	public String getFax() {
 		return fax;
 	}
 
 	/**
 	 * @param fax the fax to set
 	 */
 	public void setFax(String fax) {
 		this.fax = fax;
 	}
 
 	/**
 	 * @return the website
 	 */
 	public String getWebsite() {
 		return website;
 	}
 
 	/**
 	 * @param website the website to set
 	 */
 	public void setWebsite(String website) {
 		this.website = website;
 	}
 
 	/**
 	 * @return the address
 	 */
 	public String getAddress() {
 		return address;
 	}
 
 	/**
 	 * @param address the address to set
 	 */
 	public void setAddress(String address) {
 		this.address = address;
 	}
 
 	/**
 	 * @return the description
 	 */
 	public String getDescription() {
 		return description;
 	}
 
 	/**
 	 * @param description the description to set
 	 */
 	public void setDescription(String description) {
 		this.description = description;
 	}
 
 	/**
 	 * @return the subtype
 	 */
 	public String getSubtype() {
 		return subtype;
 	}
 
 	/**
 	 * @param subtype the subtype to set
 	 */
 	public void setSubtype(String subtype) {
 		this.subtype = subtype;
 	}
 
 	/**
 	 * @return the image
 	 */
 	public String getImage() {
 		return image;
 	}
 
 	/**
 	 * @param image the image to set
 	 */
 	public void setImage(String image) {
 		this.image = image;
 	}
 
 	/**
 	 * @return the contact
 	 */
 	public String getContact() {
 		return contact;
 	}
 
 	/**
 	 * @param contact the contact to set
 	 */
 	public void setContact(String contact) {
 		this.contact = contact;
 	}
 
 	/**
 	 * @return the url
 	 */
 	public String getUrl() {
 		return url;
 	}
 
 	/**
 	 * @param url the url to set
 	 */
 	public void setUrl(String url) {
 		this.url = url;
 	}
 
 	/**
 	 * @return the map_options
 	 */
 	public String getMap_options() {
 		return map_options;
 	}
 
 	/**
 	 * @param map_options the map_options to set
 	 */
 	public void setMap_options(String map_options) {
 		this.map_options = map_options;
 	}
 
 	/**
 	 * @return the emailid
 	 */
 	public String getEmailid() {
 		return emailid;
 	}
 
 	/**
 	 * @param emailid the emailid to set
 	 */
 	public void setEmailid(String emailid) {
 		this.emailid = emailid;
 	}
 
 	/**
 	 * @return the bitmap
 	 */
 	public Bitmap getBitmap() {
 		return bitmap;
 	}
 
 	/**
 	 * @param bitmap the bitmap to set
 	 */
 	public void setBitmap(Bitmap bitmap) {
 		this.bitmap = bitmap;
 	}
 
 	public String getDistance() {
 		if(this.getMap_options()!=null && this.getMap_options()!=""){
 			String[] locationSplited=this.getMap_options().split(",");
 			Location museumLocation=new Location(LocationManager.GPS_PROVIDER);
 			museumLocation.setLatitude(Double.parseDouble(locationSplited[1]));
 			museumLocation.setLongitude(Double.parseDouble(locationSplited[0]));
 			return Float.toString(CustomLocationListener.location.distanceTo(museumLocation)/1000);
 		}else{
 			return "";
 		}
 	}
 
 	public int compareTo(Object arg0) {
 		MuseumVO museumVO=(MuseumVO) arg0;
 		if(new Double(this.getDistance()).compareTo(new Double(museumVO.getDistance()))==0){
 			return this.getTitle().compareTo(museumVO.getTitle());
 		}else{
 			return new Double(this.getDistance()).compareTo(new Double(museumVO.getDistance()));
 		}
 	}
 	
 	
 
 	
 
 
 }
