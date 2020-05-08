 package org.iucn.sis.shared.api.structures;
 
 import java.util.ArrayList;
 
 public class DisplayableDataHelper {
 	public static String toDisplayableBoolean(String booleanData) {
 		return booleanData = booleanData.length() > 0 ? Character.toUpperCase(booleanData.charAt(0))
 				+ booleanData.substring(1) : booleanData;
 	}
 
 	@SuppressWarnings("unchecked")
 	public static String toDisplayableSingleSelect(String index, Object[] options) {
 		if( options[0] instanceof ArrayList ) {
 			ArrayList<String> listItemsToAdd = (ArrayList<String>)options[0];
			int prettyIndex = index.matches("\\d+") ? Integer.parseInt(index) : 0;
 			String ret;
 			if (prettyIndex > 0)
 				ret = (String) listItemsToAdd.get(prettyIndex-1);
 			else
 				ret = "(Not Specified)";
 			return ret;
 		}
 		
		int prettyIndex = index.matches("\\d+") ? Integer.parseInt(index) : 0;
 		String ret;
 		if (prettyIndex > 0)
 			ret = (String) options[prettyIndex - 1];
 		else
 			ret = "(Not Specified)";
 		return ret;
 	}
 
 }
