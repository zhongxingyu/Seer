 package org.fao.fi.gis.metadata.utils;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.net.URL;
 import java.util.Arrays;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Random;
 
 import javax.xml.parsers.DocumentBuilder;
 import javax.xml.parsers.DocumentBuilderFactory;
 
 import org.fao.fi.gis.metadata.entity.EntityAddin;
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 import org.w3c.dom.Node;
 import org.w3c.dom.NodeList;
 
 import com.google.gson.JsonArray;
 import com.google.gson.JsonObject;
 import com.google.gson.JsonParser;
 import com.google.gson.stream.JsonReader;
 
 public final class CollectionUtils {
 
 	/**
 	 * Method to parse the specieslist.xml and return a set of alpha3code
 	 * 
 	 * @param file
 	 * @return
 	 */
 	public static Map<String, Map<EntityAddin,String>> parseSpeciesList(String specieslist) {
 		
 		Random styleRandomizer = new Random();
 		String[] styleColors = { "852D36", "A60314", "FF031C", "FA485B",
 				"F58E98", "CC8914", "FAA616", "FAC002", "F0E92E", "FAFA5F",
 				"B5B7CF", "9B9EBF", "C3CBDF", "8B92DF", "D4D1EF", "B8B5CF",
 				"B7AFFF", "BFBFBF", "9FB9FF", "8F98FF" };
 		List<String> styleColorList = Arrays.asList(styleColors);
 		
 		Map<String, Map<EntityAddin,String>> speciesList = new HashMap<String, Map<EntityAddin,String>>();
 		try {
 
 			URL url = new URL(specieslist);
 			DocumentBuilderFactory dbFactory = DocumentBuilderFactory
 					.newInstance();
 			DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
 			Document doc = dBuilder.parse(url.openStream());
 			doc.getDocumentElement().normalize();
 
 			NodeList nList = doc.getElementsByTagName("item");
 
 			for (int temp = 0; temp < nList.getLength(); temp++) {
 				// for (int temp = 0; temp < 20; temp++) { //test with the first
 				// 10 species
 				Node nNode = nList.item(temp);
 				if (nNode.getNodeType() == Node.ELEMENT_NODE) {
 					
 					Map<EntityAddin, String> addins = new HashMap<EntityAddin,String>();
 					
 					Element eElement = (Element) nNode;
 					String alphacode = eElement.getAttribute("a3c");
 
 					String mar = eElement.getAttribute("mar");
 					String inl = eElement.getAttribute("inl");
 					String hab = null;
 					if (mar.equals("1")) {
 						if (inl.equals("1")) {
 							hab = "mi";
 						} else {
 							hab = "m";
 						}
 					} else if (mar.equals("0")) {
 						if (inl.equals("1")) {
 							hab = "i";
 						}
 
 					}
 					
 					String randomStyle = "species_style_"+ styleColorList.get(styleRandomizer.nextInt(styleColorList.size()));
 					
 					addins.put(EntityAddin.Habitat, hab);
 					addins.put(EntityAddin.Style, randomStyle);
 					speciesList.put(alphacode, addins);
 
 				}
 			}
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 
 		return speciesList;
 	}
 
 	/**
 	 * Method to parse the unique source of RFB settings and return a set of rfb
 	 * + style (for now)
 	 * 
 	 * @param file
 	 * @return
 	 * @throws IOException
 	 */
 	public static Map<String, Map<EntityAddin,String>> parseRfbList(String file)
 			throws IOException {
 
 		Map<String, Map<EntityAddin,String>> rfbList = new HashMap<String, Map<EntityAddin,String>>();
 		InputStream is = null;
 		try {
 
 			URL url = new URL(file);
 			DocumentBuilderFactory dbFactory = DocumentBuilderFactory
 					.newInstance();
 			DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
 			is = url.openStream();
 			Document doc = dBuilder.parse(is);
 			doc.getDocumentElement().normalize();
 
 			NodeList nList = doc.getElementsByTagName("rfb");
 
 			for (int temp = 0; temp < nList.getLength(); temp++) {
 				Map<EntityAddin,String> addins = new HashMap<EntityAddin,String>();
 				
 				Node nNode = nList.item(temp);
 				if (nNode.getNodeType() == Node.ELEMENT_NODE) {
 
 					Element eElement = (Element) nNode;
 					String rfb = eElement.getAttribute("name");
 					
 					String style = eElement.getAttribute("style");
 					String fid = eElement.getAttribute("fid");
 					addins.put(EntityAddin.Style, style);
 					
 					rfbList.put(rfb, addins);
 
 				}
 			}
 		} catch (Exception e) {
 			e.printStackTrace();
 		} finally {
 			is.close();
 		}
 
 		return rfbList;
 	}
 	
 	/**
 	 * Method to parse the unique source of EEZs
 	 * 
 	 * @param file
 	 * @return
 	 * @throws IOException
 	 */
 	public static Map<String, Map<EntityAddin,String>> parseEezList(String file)
 			throws IOException {
 
 		Map<String, Map<EntityAddin,String>> eezList = new HashMap<String, Map<EntityAddin,String>>();
 		
 		JsonReader reader = null;
 		try {
 			// read Json data
 			URL dataURL = new URL(file);
 			reader = new JsonReader(new InputStreamReader(dataURL.openStream()));
 			JsonParser parser = new JsonParser();
 			JsonObject flodJsonObject = parser.parse(reader).getAsJsonObject();
 
 			JsonArray bindings = flodJsonObject.get("results")
 					.getAsJsonObject().get("bindings").getAsJsonArray();
 
 			if (bindings.size() > 0) {
 				for(int i = 0;i<bindings.size();i++){
 					JsonObject obj = bindings.get(i).getAsJsonObject();
 					String mrgid = obj.get("code").getAsJsonObject().get("value").getAsString().split("http://www.fao.org/figis/flod/entities/eezcode/")[1];
 					
 					Map<EntityAddin,String> addins = new HashMap<EntityAddin,String>();
 					eezList.put(mrgid, addins);
 				}
 			}
 
 			reader.close();
 
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 
 		return eezList;
 	}
 
 
 }
