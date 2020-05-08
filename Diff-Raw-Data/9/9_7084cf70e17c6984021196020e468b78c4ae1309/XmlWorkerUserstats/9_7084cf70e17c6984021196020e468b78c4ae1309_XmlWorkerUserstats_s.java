 /*
  * Copyright (C) [2011]  [Pascal Knig]
 *
 * This program is free software; you can redistribute it and/or modify it under the terms of
 * the GNU General Public License as published by the Free Software Foundation; either version
 * 3 of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with this program; 
 * if not, see <http://www.gnu.org/licenses/>. 
 */
 package de.sockenklaus.XmlStats.XmlWorkers;
 
 import java.io.File;
 import java.io.StringWriter;
 import java.util.Arrays;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.logging.Level;
 import java.util.List;
 
 import javax.xml.parsers.DocumentBuilder;
 import javax.xml.parsers.DocumentBuilderFactory;
 import javax.xml.transform.Transformer;
 import javax.xml.transform.TransformerFactory;
 import javax.xml.transform.dom.DOMSource;
 import javax.xml.transform.stream.StreamResult;
 
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 
 import com.nidefawl.Stats.Stats;
 import com.nidefawl.Stats.ItemResolver.hModItemResolver;
 import com.nidefawl.Stats.datasource.Category;
 import com.nidefawl.Stats.datasource.PlayerStat;
 
 import de.sockenklaus.XmlStats.Datasource.UserstatsDS;
 
 // TODO: Auto-generated Javadoc
 /**
  * The Class XmlWorkerUserstats.
  */
 public class XmlWorkerUserstats extends XmlWorker {
 	
 	/** The stats ds. */
 	private UserstatsDS statsDS;
 	private hModItemResolver itemResolver;
 	private String[] resolveCats;
 	
 	/**
 	 * Instantiates a new xml worker userstats.
 	 */
 	public XmlWorkerUserstats(){
 		this.statsDS = new UserstatsDS();
 		itemResolver = new hModItemResolver(new File(statsDS.getDataFolder(),"items.txt"));
 		resolveCats = new String[]{"blockdestroy", "blockcreate", "itemdrop", "itempickup"};
 	}
 	
 	/* (non-Javadoc)
 	 * @see de.sockenklaus.XmlStats.XmlWorkers.XmlWorker#getXML(java.util.Map)
 	 */
 	public String getXML(Map<String, List<String>> parameters) {
 		try {
 			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
 			DocumentBuilder builder = factory.newDocumentBuilder();
 			Document doc = builder.newDocument();
 			DOMSource source = new DOMSource(doc);
 			StringWriter writer = new StringWriter();
 			StreamResult result = new StreamResult(writer);
 			TransformerFactory tf = TransformerFactory.newInstance();
 			Transformer transformer = tf.newTransformer();
 			
 			Element root = doc.createElement("stats");
 			doc.appendChild(root);
 			
 			/*
 			 * Hier wird das XML aufgebaut
 			 */
 			if (!parameters.containsKey("player")){
 				// Generate a summarized XML
 				
 				root.appendChild(getAddedUpStatsElement(doc));
 				
 			}
 			else {
 				// Generate the XML for the given user(s)
 				for(String playerName : statsDS.fetchAllPlayers()){
					if (parameters.containsKey("player") && parameters.get("player").contains(playerName)){
 						root.appendChild(getPlayerElement(playerName, doc));
 					}
 				}
 			}
 			
 			/*
 			 * Hier endet der XML-Aufbau
 			 */
 			
 			transformer.transform(source, result);
 			return writer.toString();
 		} 
 		
 		catch (Exception e){
 			Stats.log.log(Level.SEVERE, "Something went terribly wrong!");
 			Stats.log.log(Level.SEVERE, e.getMessage());
 		}
 		
 		return "";
 	}
 	
 	/**
 	 * Build a XML subtree for the given player.
 	 *
 	 * @param playerName the player name
 	 * @param doc the doc
 	 * @return 				Returns a XML subtree for the given playerName.
 	 */
 	private Element getPlayerElement(String playerName, Document doc){
 		PlayerStat player_stats = statsDS.getPlayerStat(playerName);
 		
 		Element elem_player = doc.createElement("player");
 		elem_player.setAttribute("name", playerName);
 		
 		for(String catName : player_stats.getCats()){
 			Category cat = player_stats.get(catName);
 			Element elem_cat = doc.createElement("category");
 			elem_cat.setAttribute("name", catName);
 				
 			for(String valName : cat.stats.keySet()){
 				int value = cat.get(valName);
 				Element elem_value = doc.createElement("stat");
 				
 				elem_value.setAttribute("name", valName);
 				
 				if (Arrays.asList(resolveCats).contains(catName)){
 					elem_value.setAttribute("id", String.valueOf(itemResolver.getItem(valName)));
 				}
 				elem_value.setAttribute("value", String.valueOf(value));
 				
 				elem_cat.appendChild(elem_value);
 			}
 			
 			
 			elem_player.appendChild(elem_cat);
 		}
 		return elem_player;
 	}
 	
 	private Element getAddedUpStatsElement(Document doc){
 		HashMap<String, HashMap<String, Integer>> addedStats = statsDS.getAddedStats();
 		
 		Element elem_player = doc.createElement("player");
 		elem_player.setAttribute("name", "*");
 		
 		for (String catName : addedStats.keySet()){
 			Element elem_cat = doc.createElement("category");
 			elem_cat.setAttribute("name", catName);
 				
 			for(String entryName : addedStats.get(catName).keySet()){
 				Element elem_stat = doc.createElement("stat");
 				elem_stat.setAttribute("name", entryName);
 					
 				if(Arrays.asList(resolveCats).contains(catName)){
 					elem_stat.setAttribute("id", String.valueOf(itemResolver.getItem(entryName)));
 				}
 				elem_stat.setAttribute("value", String.valueOf(addedStats.get(catName).get(entryName)));
 				
 				elem_cat.appendChild(elem_stat);
 			}
 			elem_player.appendChild(elem_cat);
 		}
 		
 		return elem_player;
 	}
 }
