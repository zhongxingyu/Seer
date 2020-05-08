 package sql;
 
 import sql.InfoPackage;
 import etc.Constants;
 
 public class XMLParser {
 	private static String xml;
 	
 	public XMLParser() {
 		
 	}
 	
 	public XMLParser(InfoPackage pack) {
		xml = "";
 		xml = xml.concat(String.valueOf(pack.getDate(1)));
 		xml = xml.concat("|");
 		xml = xml.concat(String.valueOf(pack.getDate(2)));
 		xml = xml.concat("|");
 		int [] actions = pack.getActions();
 		int index = 0;
 		while(actions[index] != 0 && index < 500) {
 			switch(actions[index]) {
 			case Constants.DIR_LEFT:
 				xml = xml.concat("l");
 				break;
 			case Constants.DIR_RIGHT:
 				xml = xml.concat("r");
 				break;
 			case Constants.DIR_UP:
 				xml = xml.concat("u");
 				break;
 			case Constants.DIR_DOWN:
 				xml = xml.concat("d");
 				break;
 			}
 			index++;
 		}
 		
 		xml = xml.concat("|");
 	}
 	
 	public String getXML() {
 		return xml;
 	}
 }
