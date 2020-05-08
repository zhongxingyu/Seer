 package org.esup.portlet.intranet.web.taglib;
 
 import java.text.SimpleDateFormat;
 import java.util.Date;
 
 import org.nuxeo.ecm.automation.client.model.PropertyMap;
 import org.nuxeo.ecm.core.schema.utils.DateParser;
 
 public class IntranetTagLib {
 	private static SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy hh:mm:ss a");
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
 }
