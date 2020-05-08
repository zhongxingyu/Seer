 package com.bun.notificationshistory;
 
 
 import java.lang.reflect.Field;
 import java.text.DateFormat;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.Collections;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.LinkedHashMap;
 import java.util.LinkedHashSet;
 import java.util.List;
 
 import android.app.Notification;
 import android.content.Context;
 import android.content.Intent;
 import android.graphics.drawable.Drawable;
 import android.util.Log;
 import android.widget.RemoteViews;
 import android.widget.RemoteViews.RemoteView;
 
 
 
 public class Utils {
 	
 	public static HashMap<String, com.bun.notificationshistory.Notification> notMap = new HashMap<String, com.bun.notificationshistory.Notification>();
 
 	public static LinkedHashMap<String,Integer> sortHashMapByValuesD(LinkedHashMap<String,Integer> passedMap) {
 		   List mapKeys = new ArrayList(passedMap.keySet());
 		   List mapValues = new ArrayList(passedMap.values());
 		   Collections.sort(mapValues, Collections.reverseOrder());
 		   Collections.sort(mapKeys,Collections.reverseOrder());
 
 		   LinkedHashMap<String,Integer> sortedMap = new LinkedHashMap<String,Integer>();
 
 		   Iterator valueIt = mapValues.iterator();
 		   while (valueIt.hasNext()) {
 		       Integer val = Integer.valueOf(valueIt.next().toString());
 		       Iterator keyIt = mapKeys.iterator();
 
 			    while (keyIt.hasNext()) {
 			        Object key = keyIt.next();
 			        Integer comp1 = passedMap.get(key);
 			        Integer comp2 = val;
 	
 			        if (comp1.equals(comp2)){
 			            passedMap.remove(key);
 			            mapKeys.remove(key);
 			            sortedMap.put((String)key, Integer.valueOf(val));
 			            break;
 			        }
 	
 			    }
 
 		}
 		//passedMap.putAll(sortedMap);
 		return sortedMap; 
 	}
 	
 	public static Boolean isInteger(String s){
 		try{
 			Integer.parseInt(s);
 			
 		}catch(Exception e){
 			return false;
 		}
 		
 		return true;
 	}
 	
 	public static String getAppSpecificMessage(String packageName, Notification notification){
 		
 		LinkedHashSet<String> valueSet = new LinkedHashSet<String>();
 		ArrayList<String> raw = new ArrayList<String>();		
 		
 		if(notification == null){
 			return "";
 		}
 		
 		RemoteViews views = notification.bigContentView;
 		if(views == null){
 			views = notification.contentView;
 		}
 		
 		if(views == null){
 			return "" ;
 		}
 		
 		Class<?> secretClass = views.getClass();
 
 		
 
 		Field outerFields[] = secretClass.getDeclaredFields();
 		for (int i = 0; i < outerFields.length; i++) {
 			if (outerFields[i].getName().equals("mActions")) {
 				outerFields[i].setAccessible(true);
 
 				ArrayList<Object> actions = null;
 				try {
 					actions = (ArrayList<Object>) outerFields[i].get(views);
 
 					for (Object action : actions) {
 						Field innerFields[] = action.getClass()
 								.getDeclaredFields();
 
 						Object value = null;
 						Integer type = null;
 						for (Field field : innerFields) {
 							//Log.d("Field Name =======" , field.getName() );
 							try {
 								field.setAccessible(true);
 								if (field.getName().equals("type")) {
 									type = field.getInt(action);
 								} else if (field.getName().equals("value")) {
 									value = field.get(action);
 								}
 								
 								//Log.d("Type =======" , type != null ? type.toString() : "null");
 								//Log.d("Value =======" , value != null ? value.toString()  : "null " );
 							} catch (IllegalArgumentException e) {
 							} catch (IllegalAccessException e) {
 							}
 						}
 
 						if (type != null && type == 10 && value != null) {
 							
 							Log.d("Value =======" , value.toString() );
 							raw.add(value.toString());
 						}
 					}
 				} catch (IllegalArgumentException e1) {
 				} catch (IllegalAccessException e1) {
 				}
 			}
 		}
 		
 		String message = "";
 		
 		if(packageName.equals("com.whatsapp")){
 			if(raw.get(1).contains("message") && raw.get(2).contains("@")){
 				message = raw.get(2).split("@")[1].split(":")[0] + ":\n\n" +  raw.get(2).split("@")[0];
 			}
 			else if(raw.get(2).contains(":")){
 				String groupName = "";
 				if(!raw.get(0).toLowerCase().equals("whatsapp")){
 					groupName = raw.get(0) + " @ ";
 				}
 				message = groupName + raw.get(2).split(":")[0] + ":\n\n" +  raw.get(2).split(":")[1];
 			}else{
 				message = raw.get(0) + ":\n\n" + raw.get(2);
 			}
         	 
         }else if(packageName.equals("com.google.android.gm")){
        	message = raw.get(3) + ":\n\n" +  raw.get(4); 
         }else if(packageName.equals("com.android.email")){
        	message = raw.get(3) + ":\n\n" +  raw.get(4);
         }
 		
 		
 		return message;
 		
 	}
 	
 	public static com.bun.notificationshistory.Notification getNotificationData(HashMap<String,String> m, Context ctx, Boolean getDateHeader){
 		
 		com.bun.notificationshistory.Notification n = new com.bun.notificationshistory.Notification();
 		
 		try{		
 			
 			String message = "";
 			
 			message = getMessageBody(m, getDateHeader);
 			
 			n.setMessage(message);
 			
 			String sender = getSender(m, 0);
 			
 			if(sender == null || sender.trim().length() == 0){
 				sender = getSender(m, 1);
 			}
 			
 			if(sender == null || sender.trim().length() == 0){
 				sender = m.get("appName");
 			}
 			
 			n.setSender(sender);
 			n.setNotDate(m.get("notTime") + "  " + m.get("notDate"));
 			Drawable icon;
 			if(m.get("appName").equals("Google Talk")){
 				icon = ctx.getResources().getDrawable( R.drawable.googletalk );
 			}else{
 				icon = ctx.getPackageManager().getApplicationIcon(m.get("packageName"));
 			}
 			n.setAppIcon(icon);
 			
 			
 		}catch(Exception e){
 			Log.e("Notification_Details", "Exception in Handlingthe Event : " + e);
 		}
 		
 		return n;
 		
 	}
 
 
 	private static String getSender(HashMap<String,String> hm, Integer bol){
 		
 		StringBuilder retMessage = new StringBuilder("");
 		
 		if(bol == 0){
 		
 			String[] strArr = hm.get("message").split(":");
 			
 			for(Integer i = 0; i< strArr.length ;i++){
 				if(i == 0){
 					retMessage.append(strArr[i]).append(" ");
 				}			
 			}
 		}else{
 			
 			String[] strArr = hm.get("additionalInfo").split("\n");
 			
 			for(Integer i = 0; i< strArr.length ;i++){
 				if(i == 0){
 					retMessage.append(strArr[i]).append(" ");
 				}			
 			}
 			
 		}
 		
 		String retString = retMessage.toString().replaceAll("\\[", "").replaceAll("\\]", "");
 				
 		return retString;
 		
 		
 	}
 
 	private static String getMessageBody(HashMap<String,String> hm, Boolean getDateHeader){
 		
 		StringBuilder retMessage = new StringBuilder("");
 		String[] strArr = hm.get("additionalInfo").split("\n");
 		
 		for(Integer i = 0; i< strArr.length ;i++){
 			if(i == 2){
 				retMessage.append(strArr[i]).append(" ");
 			}
 		}
 			
 		
 		String message = retMessage.toString();		
 	
 		if(message.length() > 100){
 			message = message.substring(0, 100);
 		}
 		
 		if(getDateHeader)		
 				message += "\n\n" + hm.get("notTime") + "  " + hm.get("notDate");
 	
 		return message;
 	}
 
 }
