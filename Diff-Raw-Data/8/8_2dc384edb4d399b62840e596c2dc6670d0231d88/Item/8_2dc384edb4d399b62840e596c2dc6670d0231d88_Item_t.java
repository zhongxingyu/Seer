 package com.android.iliConnect.models;
 
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Date;
 
 import org.simpleframework.xml.Element;
 import org.simpleframework.xml.ElementList;
 
 import com.android.iliConnect.dataproviders.PersistableObject;
 
 @Element
 public class Item extends PersistableObject{
 	
 	@Element (required = false)	
 	public String title = "";
 	@Element (required = false)
 	public String description = "";
 	@Element (required = false)
 	public String type = "";
 	@Element (required = false)
 	public String ref_id = "";
 	@Element (required = false)
 	public String owner = "";
 	@Element (required = false)
 	public boolean changed;
 	@ElementList (required = false, name="Items")
 	public ArrayList<Item> Item;
 	@Element (required = false)
 	public Long last_update;
 	@Override
 	public void load() {
 		
 	}
 
 	public String getTitle() {
 		return title;
 	}
 
 	public String getDescription() {
 		return description;
 	}
 
 	public String getType() {
 		
 	 return type;
 		
 	}
 
 	public String getRef_id() {
 		return ref_id;
 	}
 	
 	public String getOwner() {
 		return owner;
 	}
 
 
 	public ArrayList<Item> getItems() {
 		return Item;
 	}
 	
 	@Override
 	public boolean equals(Object o) {
 		return (o != null) && 
 			   (o instanceof Item) &&
 			   (this.ref_id.equals(((Item)o).ref_id));
 	}
 	
 	
 
 	
 
 
 	
 }
