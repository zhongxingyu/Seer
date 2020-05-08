 package Specification;
 
import FileOps.XML.XMLParser;
 import Galaxy.Tree.GalaxyNode;
import FileOps.JSON.JSONParser;
 
 public class GalaxySpecification {
 	public static XMLParser<GalaxyNode> xmlParser;
 	public static JSONParser<GalaxyNode> jsonParser;
 	
 	private static void initXMLParser(){
 		
 	}
 	private static void initJSONParser(){
 		
 	}
 	
 	public static XMLParser getXMLParser(){
 		if(xmlParser == null)
 			initXMLParser();
 		return xmlParser;
 	}
 	public static JSONParser getJSONParser(){
 		if(jsonParser == null)
 			initJSONParser();
 		return jsonParser;
 	}
 	
 
 }
