 package com.android.iliConnect.models;
 
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.HashMap;
 
 import org.simpleframework.xml.Element;
 import org.simpleframework.xml.Root;
 
 import android.widget.Toast;
 
 import com.android.iliConnect.MainActivity;
 import com.android.iliConnect.dataproviders.PersistableObject;
 
 @Root(name = "Iliconnect")
 public class RemoteData extends PersistableObject {
 	@Element
 	public Current Current;

	private HashMap<String, Long> fileDates;
 	
 	@Override
 	public void load() throws Exception {
 
 		super.deserialize(MainActivity.instance.localDataProvider.remoteDataFileName);
 		
 		this.getChangedFiles(this.Current.Desktop.DesktopItem);
 	}
 
 	public String getSyncUrl() {
 		return MainActivity.instance.localDataProvider.auth.url_src + MainActivity.instance.localDataProvider.auth.api_src;
 	}
 
 	public void save() {
 
 		try {
 			super.serialize(MainActivity.instance.localDataProvider.remoteDataFileName);
 
 		} catch (Exception e) {
 			Toast t = Toast.makeText(MainActivity.instance, "Fehler beim speichern der Einstellungen, nutze Standardkonfiguration.", Toast.LENGTH_LONG);
 			t.show();
 		}
 	}
 
 	public void delete() {
 		super.delete(MainActivity.instance.localDataProvider.remoteDataFileName);
 
 	}
 	public void getChangedFiles(ArrayList<Item> items){
 		for(Item item : items){
 			if(item.type.equalsIgnoreCase("FILE") && item.changed)
 					MainActivity.instance.localDataProvider.remoteData.Current.Desktop.ChangedFiles.add(item);
 			if(item.Item!=null)
 				getChangedFiles(item.Item);
 		}
 	}
 	
 	
 
 }
