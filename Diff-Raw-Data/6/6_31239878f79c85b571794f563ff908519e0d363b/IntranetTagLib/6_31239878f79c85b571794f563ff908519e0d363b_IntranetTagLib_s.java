 package org.esup.ecm.dashboard.web.taglib;
 
 import java.text.SimpleDateFormat;
 import java.util.Date;
 
 import org.nuxeo.ecm.automation.client.model.PropertyMap;
 import org.nuxeo.ecm.core.schema.utils.DateParser;
 
 public class IntranetTagLib {
 	private static SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy hh:mm:ss a");
 	private static final String dateColumns = "modified:created:issued:valid:expired";
 	
 	public static String getValue(PropertyMap map, String key){
 		return map.getString(key);
 	}
 	
 	public static String getLastModifiedDate(PropertyMap map){
 		Date d = DateParser.parseDate(map.getString("dc:modified"));
 		return sdf.format(d);
 	}
 	
 	public static String getImgFileName(PropertyMap map){
 		return map.getString("common:icon");
 	}
 	
 	public static String getDublinCoreProperty(PropertyMap map, String name){
 		
 		if(dateColumns.contains(name)){
 			Date d = DateParser.parseDate(map.getString("dc:"+name));
 			return sdf.format(d);
 		}
 		
		return map.getString("dc:"+name);
 	}
 }
