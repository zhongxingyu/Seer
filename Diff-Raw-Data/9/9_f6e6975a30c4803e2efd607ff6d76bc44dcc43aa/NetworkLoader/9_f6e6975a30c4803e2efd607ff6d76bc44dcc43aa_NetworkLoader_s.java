 package org.amanzi.neo.loader;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileReader;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.HashMap;
 
import org.amanzi.net.loader.internal.NeoLoaderPlugin;
 import org.neo4j.api.core.Direction;
 import org.neo4j.api.core.EmbeddedNeo;
 import org.neo4j.api.core.NeoService;
 import org.neo4j.api.core.Node;
 import org.neo4j.api.core.Relationship;
 import org.neo4j.api.core.RelationshipType;
 import org.neo4j.api.core.Transaction;
 
 /**
  * This class was written to handle CSV (tab delimited) network data from ice.net in Sweden.
  * It has been written in a partially generic way so as to be possible to change to various
  * other data sources, but some things are hard coded, like the names of key columns and the 
  * assumption of RT90 projection for non-angular coordinates.
  * 
  * It also assumes the data is structured as BSC->Site->Sector in a tree layout.
  * 
  * @author craig
  */
 public class NetworkLoader {
 	public static enum NetworkElementTypes {
 		NETWORK("Network"),
 		BSC("BSC"),
 		SITE("Name"),
 		SECTOR("Cell");
 		private String header = null;
 		private NetworkElementTypes(String header){
 			this.header = header;
 		}
 		public String getHeader(){return header;}
 		public String toString(){return super.toString().toLowerCase();}
 	}
 	public static enum NetworkRelationshipTypes implements RelationshipType {
 		CHILD,
 		SIBLING,
 		INTERFERS
 	}
 	/**
 	 * This class handles the CRS specification.
 	 * Currently it is hard coded to return WGS84 (EPSG:4326) for data that looks like lat/long
      * and RT90 2.5 gon V (EPSG:3021) for data that looks like it is in meters.
 	 * @author craig
 	 */
 	public static class CRS {
 		private String type = null;
 		private String epsg = null;
 		private CRS(){}
 		public String getType(){return type;}
 		public String toString(){return epsg;}
 		public static CRS fromLocation(float lat, float lon){
 			CRS crs = new CRS();
 			crs.type = "geographic";
 			crs.epsg = "EPSG:4326";
 			if((lat>90 || lat<-90) && (lon>180 || lon<-180)) {
 				crs.type="projected";
 				crs.epsg = "EPSG:3021";
 			}
 			return crs;
 		}
 	}
 	private NeoService neo;
 	private String siteName = null;
 	private String bscName = null;
 	private Node site = null;
 	private Node bsc = null;
 	private Node network = null;
 	private CRS crs = null;
 	private String[] headers = null;
 	private HashMap<String,Integer> headerIndex = null;
 	private int[] mainIndexes = null;
 	private int[] stringIndexes = null;
 	private int[] intIndexes = null;
 	private String filename;
 	private String basename;
 
 	public NetworkLoader(String filename) {
 		this(null, filename);
 	}
 
 	public NetworkLoader(NeoService neo, String filename) {
 		this.neo = neo;
 		if(this.neo == null) this.neo = org.neo4j.neoclipse.Activator.getDefault().getNeoServiceSafely();
 		this.filename = filename;
 		this.basename = (new File(filename)).getName();
 	}
 	
 	public void run() throws IOException {
 		BufferedReader reader = new BufferedReader(new FileReader(filename));
 		try{
 			String line;
 			while((line = reader.readLine())!=null) {
 				parseLine(line);
 			}
 		}finally{
 			reader.close();
 		}
 	}
 
 	private static void debug(String line){
 		NeoLoaderPlugin.debug(line);
 	}
 	
 	private static void info(String line){
 		NeoLoaderPlugin.info(line);
 	}
 	
 	private static void error(String line){
 		System.err.println(line);
 	}
 	
 	private void parseLine(String line){
 		debug(line);
 		String fields[] = line.split("\\t");
 		if(fields.length>2){
 			if(headers==null){	// first line
 				headers = fields;
 				headerIndex = new HashMap<String,Integer>();
 				mainIndexes = new int[5];
 				ArrayList<Integer> strings = new ArrayList<Integer>();
 				ArrayList<Integer> ints = new ArrayList<Integer>();
 				int index=0;
 				for(String header:headers){
 					debug("Added header["+index+"] = "+header);
 					if(header.equals(NetworkElementTypes.BSC.getHeader())) mainIndexes[0]=index;
 					else if(header.equals(NetworkElementTypes.SITE.getHeader())) mainIndexes[1]=index;
 					else if(header.equals(NetworkElementTypes.SECTOR.getHeader())) mainIndexes[2]=index;
 					else if(header.toLowerCase().startsWith("lat")) mainIndexes[3]=index;
 					else if(header.toLowerCase().startsWith("long")) mainIndexes[4]=index;
 					else if(header.toLowerCase().contains("type")) strings.add(index);
 					else if(header.toLowerCase().contains("status")) strings.add(index);
 					else ints.add(index);
 					headerIndex.put(header,index++);
 				}
 				stringIndexes = new int[strings.size()];
 				for(int i=0;i<strings.size();i++) stringIndexes[i] = strings.get(i);
 				intIndexes = new int[ints.size()];
 				for(int i=0;i<ints.size();i++) intIndexes[i] = ints.get(i);
 			}else{
 				Transaction tx = neo.beginTx();
 				try {
 					String bscField = fields[mainIndexes[0]];
 					String siteField = fields[mainIndexes[1]];
 					String sectorField = fields[mainIndexes[2]];
 					if (!bscField.equals(bscName)) {
 						bscName = bscField;
 						debug("New BSC: " + bscName);
 						network = getNetwork(neo, basename);
 						network.setProperty("filename", filename);
 						deleteTree(network);
 						bsc = addChild(network, NetworkElementTypes.BSC.toString(), bscName);
 					}
 					if (!siteField.equals(siteName)) {
 						siteName = siteField;
 						debug("New site: " + siteName);
 						site = addChild(bsc, NetworkElementTypes.SITE.toString(), siteName);
 						float lat = Float.parseFloat(fields[mainIndexes[3]]);
 						float lon = Float.parseFloat(fields[mainIndexes[4]]);
 						if(crs==null){
 							crs = CRS.fromLocation(lat, lon);
 							network.setProperty("crs_type", crs.getType());
 							network.setProperty("crs", crs.toString());
 						}
 						site.setProperty("lat", lat);
 						site.setProperty("lon", lon);
 					}
 					debug("New Sector: " + sectorField);
 					Node sector = addChild(site, NetworkElementTypes.SECTOR.toString(), sectorField);
 					for (int i : stringIndexes) sector.setProperty(headers[i], fields[i]);
 					for (int i : intIndexes) sector.setProperty(headers[i], Integer.parseInt(fields[i]));
 					tx.success();
 				} finally {
 					tx.finish();
 				}
 			}
 		}
 	}
 
 	private void deleteTree(Node root){
 		if(root!=null){
 			for (Relationship relationship : root.getRelationships(NetworkRelationshipTypes.CHILD, Direction.OUTGOING)) {
 				Node node = relationship.getEndNode();
 				deleteTree(node);
 				node.delete();
 				relationship.delete();
 			}
 		}
 	}
 	
 	/**
 	 * This code finds the specified network node in the database, creating its own transaction for that.
 	 */
 	public static Node getNetwork(NeoService neo, String basename) {
 		Node network = null;
 		Transaction tx = neo.beginTx();
 		try {
 			Node reference = neo.getReferenceNode();
 			for (Relationship relationship : reference.getRelationships(NetworkRelationshipTypes.CHILD, Direction.OUTGOING)) {
 				Node node = relationship.getEndNode();
 				if (node.hasProperty("type") && node.getProperty("type").equals(NetworkElementTypes.NETWORK.toString()) && node.hasProperty("name")
 						&& node.getProperty("name").equals(basename))
 					return node;
 			}
 			network = neo.createNode();
 			network.setProperty("type", NetworkElementTypes.NETWORK.toString());
 			network.setProperty("name", basename);
 			reference.createRelationshipTo(network, NetworkRelationshipTypes.CHILD);
 			tx.success();
 		} finally {
 			tx.finish();
 		}
 		return network;
 	}
 	
 	/**
 	 * This code expects you to create a transaction around it, so don't forget to do that.
 	 * @param parent
 	 * @param type
 	 * @param name
 	 * @return
 	 */
 	private Node addChild(Node parent, String type, String name) {
 		Node child = null;
 		child = neo.createNode();
 		child.setProperty("type", type);
 		child.setProperty("name", name);
 		if (parent != null) {
 			parent.createRelationshipTo(child, NetworkRelationshipTypes.CHILD);
 			debug("Added '" + name + "' as child of '" + parent.getProperty("name"));
 		}
 		return child;
 	}
 	
 	public void printStats(){
 		if(bsc!=null){
 			printChildren(bsc,0);
 		}else{
 			error("No BSC node found");
 		}
 	}
 
 	public static void printChildren(Node node, int depth){
 		if(node==null || depth > 4 || !node.hasProperty("name")) return;
 		StringBuffer tab = new StringBuffer();
 		for(int i=0;i<depth;i++) tab.append("    ");
 		StringBuffer properties = new StringBuffer();
 		for(String property:node.getPropertyKeys()) {
 			if(!property.equals("name")) properties.append(" - ").append(property).append(" => ").append(node.getProperty(property));
 		}
 		info(tab.toString()+node.getProperty("name")+properties);
 		for(Relationship relationship:node.getRelationships(NetworkRelationshipTypes.CHILD,Direction.OUTGOING)){
 			//debug(tab.toString()+"("+relationship.toString()+") - "+relationship.getStartNode().getProperty("name")+" -("+relationship.getType()+")-> "+relationship.getEndNode().getProperty("name"));
 			printChildren(relationship.getEndNode(),depth+1);
 		}
 	}
 	
 	/**
 	 * @param args
 	 */
 	public static void main(String[] args) {
 		EmbeddedNeo neo = new EmbeddedNeo("var/neo");
 		try{
 			NetworkLoader networkLoader = new NetworkLoader(neo,args.length>0 ? args[0] : "amanzi/network.csv");
 			networkLoader.run();
 			networkLoader.printStats();
 		} catch (IOException e) {
 			System.err.println("Failed to load network: "+e);
 			e.printStackTrace(System.err);
 		}finally{
 			neo.shutdown();
 		}
 	}
 }
