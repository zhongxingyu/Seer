 package org.amanzi.neo.loader;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileReader;
 import java.io.IOException;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.List;
 
 import net.refractions.udig.catalog.CatalogPlugin;
 import net.refractions.udig.catalog.ICatalog;
 import net.refractions.udig.catalog.IGeoResource;
 import net.refractions.udig.catalog.IService;
 import net.refractions.udig.project.ILayer;
 import net.refractions.udig.project.IMap;
 import net.refractions.udig.project.ui.ApplicationGIS;
 
 import org.amanzi.awe.views.network.view.NetworkTreeView;
 import org.amanzi.neo.core.INeoConstants;
 import org.amanzi.neo.core.NeoCorePlugin;
 import org.amanzi.neo.core.database.services.UpdateDatabaseEvent;
 import org.amanzi.neo.core.database.services.UpdateDatabaseEventType;
 import org.amanzi.neo.core.enums.GeoNeoRelationshipTypes;
 import org.amanzi.neo.core.enums.GisTypes;
 import org.amanzi.neo.core.enums.NetworkElementTypes;
 import org.amanzi.neo.core.enums.NetworkRelationshipTypes;
 import org.amanzi.neo.core.service.NeoServiceProvider;
 import org.amanzi.neo.core.service.listener.NeoServiceProviderEventAdapter;
 import org.amanzi.neo.core.utils.ActionUtil;
 import org.amanzi.neo.core.utils.ActionUtil.RunnableWithResult;
 import org.amanzi.neo.loader.internal.NeoLoaderPlugin;
 import org.amanzi.neo.loader.internal.NeoLoaderPluginMessages;
 import org.amanzi.neo.preferences.DataLoadPreferences;
 import org.eclipse.core.runtime.NullProgressMonitor;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.widgets.MessageBox;
 import org.eclipse.ui.PartInitException;
 import org.eclipse.ui.PlatformUI;
 import org.neo4j.api.core.Direction;
 import org.neo4j.api.core.EmbeddedNeo;
 import org.neo4j.api.core.NeoService;
 import org.neo4j.api.core.Node;
 import org.neo4j.api.core.Relationship;
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
 public class NetworkLoader extends NeoServiceProviderEventAdapter {
     /** String LOAD_NETWORK_TITLE field */
     private static final String LOAD_NETWORK_TITLE = "Load Network";
     private static final String LOAD_NETWORK_MSG = "This network is already loaded into the database.\nDo you wish to overwrite the data?";
 
     /**
 	 * This class handles the CRS specification.
 	 * Currently it is hard coded to return WGS84 (EPSG:4326) for data that looks like lat/long
      * and RT90 2.5 gon V (EPSG:3021) for data that looks like it is in meters and no hints are given.
      * If the user passes a hint, the following are considered:
      * 
 	 * @author craig
 	 */
 	public static class CRS {
         private String type = null;
         private String epsg = null;
         private CRS() {}
         public String getType() {return type;}
         public String toString() {return epsg;}
         public static CRS fromLocation(float lat, float lon, String hint) {
             CRS crs = new CRS();
             crs.type = "geographic";
             crs.epsg = "EPSG:4326";
             if ((lat > 90 || lat < -90) && (lon > 180 || lon < -180)) {
                 crs.type = "projected";
                 if (hint != null && hint.toLowerCase().startsWith("germany")) {
                     crs.epsg = "EPSG:31467";
                 } else {
                     crs.epsg = "EPSG:3021";
                 }
             }
             return crs;
         }
     }
 	private NeoService neo;
 	private NeoServiceProvider neoProvider;
 	private String siteName = null;
     private String bscName = null;
     private String cityName = null;
 	private Node site = null;
     private HashMap<String,Node> bsc_s = new HashMap<String,Node>();
     private HashMap<String,Node> city_s = new HashMap<String,Node>();
     private Node bsc = null;
     private Node city = null;
     private Node network = null;
 	private Node gis = null;
 	private CRS crs = null;
 	private String[] headers = null;
 	private HashMap<String,Integer> headerIndex = null;
 	private int[] mainIndexes = null;
 	private boolean haveTypedIndexes = false;
     private ArrayList<Integer> stringIndexes = new ArrayList<Integer>();
     private ArrayList<Integer> intIndexes = new ArrayList<Integer>();
     private ArrayList<Integer> floatIndexes = new ArrayList<Integer>();
     private ArrayList<String> shortLines = new ArrayList<String>();
     private ArrayList<String> emptyFields = new ArrayList<String>();
     private ArrayList<String> badFields = new ArrayList<String>();
     private ArrayList<String> lineErrors = new ArrayList<String>();
 	private String filename;
 	private String basename;
 	private double[] bbox;
     private String crsHint;
     private long lineNumber = 0;
     private long siteNumber = 0;
     private long sectorNumber = 0;
     private boolean trimSectorName = true;
 
 	public NetworkLoader(String filename) {
 		this(null, filename);
 	}
 
 	public NetworkLoader(NeoService neo, String filename) {
 		this.neo = neo;
 		if(this.neo == null) {
 		    //Lagutko 21.07.2009, using of neo.core plugin
 		    neoProvider = NeoServiceProvider.getProvider();
             this.neo = neoProvider.getService();  // Call this first as it initializes everything
             neoProvider.addServiceProviderListener(this);
 		}
 		this.filename = filename;
 		this.basename = (new File(filename)).getName();
 		//TODO: Enabled user preferences
 		//this.trimSectorName = get from preferences
 	}
 
 	//Lagutko 21.07.2009, using of neo.core plugin
     public void onNeoStop(Object source) {        
         unregisterNeoManager();        
     }
     
     //Lagutko 21.07.2009, using of neo.core plugin
     private void unregisterNeoManager(){        
         neoProvider.commit();
         neoProvider.removeServiceProviderListener(this);        
     }
 
 	public void run() throws IOException {
         trimSectorName = NeoLoaderPlugin.getDefault().getPreferenceStore().getBoolean(DataLoadPreferences.REMOVE_SITE_NAME);
         BufferedReader reader = new BufferedReader(new FileReader(filename));
         try {
             long startTime = System.currentTimeMillis();
             String line;
             while ((line = reader.readLine()) != null) {
                 lineNumber++;
                 if (!parseLine(line)) {
                     break;
                 }
             }
             info("Loaded "+filename+" in "+(System.currentTimeMillis()-startTime)/1000.0+"s");
             printWarnings(emptyFields, "empty fields", 0, lineNumber);
             printWarnings(badFields, "field parsing warnings", 10, lineNumber);
             printWarnings(shortLines, "missing fields", 10, lineNumber);
             printWarnings(lineErrors, "uncaught errors", 10, lineNumber);
         } finally {
             // Close the file reader
             reader.close();
             // Save the bounding box
             if(gis!=null && bbox!=null){
                 Transaction transaction = neo.beginTx();
                 try {
                     gis.setProperty(INeoConstants.PROPERTY_BBOX_NAME, bbox);
                     gis.setProperty("count", siteNumber);
                     transaction.success();
                 }finally{
                     transaction.finish();
                 }
             }
             if(network!=null){
                 Transaction transaction = neo.beginTx();
                 try {
                     network.setProperty("site_count", siteNumber);
                     network.setProperty("sector_count", sectorNumber);
                     network.setProperty("bsc_count", bsc_s.size());
                     network.setProperty("city_count", city_s.size());
                     transaction.success();
                 }finally{
                     transaction.finish();
                 }
             }
             //If we are not running the command-line test then attach the data to the AWE project
             if (neoProvider!=null) {
                 attachDataToProject();
             }
             NeoServiceProvider.getProvider().commit();
         }
     }
 
     private static void printWarnings(ArrayList<String> warnings, String warning_type, int limit, long lineNumber) {
         if(warnings.size()>0){
             info("Had " + warnings.size() + " "+warning_type+" warnings in " + lineNumber + " lines parsed");
             if (limit > 0) {
                 int i = 0;
                 for (String warning : warnings) {
                     info("\t" + warning);
                     if (i++ > limit) {
                         info("\t... and " + (warnings.size() - 10) + " more ...");
                         break;
                     }
                 }
             }
         }
     }
 
     private void attachDataToProject() throws MalformedURLException {
         if (network != null) {
             NeoCorePlugin.getDefault().getProjectService().addNetworkToProject(TEMSLoader.getAweProjectName(), network);
         }
         NeoServiceProvider.getProvider().commit();
         //Lagutko 21.07.2009, using of neo.core plugin
         unregisterNeoManager();
         // Register the database in the uDIG catalog            
         String databaseLocation = neoProvider.getDefaultDatabaseLocation();
         ICatalog catalog = CatalogPlugin.getDefault().getLocalCatalog();
         List<IService> services = CatalogPlugin.getDefault().getServiceFactory().createService(new URL("file://"+databaseLocation));
         IService curService = null;
         for (IService service : services) {
             System.out.println("Found catalog service: " + service);
             curService = service;
             if (catalog.getById(IService.class, service.getIdentifier(), new NullProgressMonitor()) != null) {
                 catalog.replace(service.getIdentifier(), service);
             } else {
                 catalog.add(service);
             }
         }
         NeoCorePlugin.getDefault().getUpdateDatabaseManager()
                 .fireUpdateDatabase(new UpdateDatabaseEvent(UpdateDatabaseEventType.GIS));
         // if(services.size()>0) catalog.add(services.get(0));
 
         // Lagutko, 21.07.2009, show NeworkTree
         ActionUtil.getInstance().runTask(new Runnable() {
             @Override
             public void run() {
                 try {
                     PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().showView(
                             NetworkTreeView.NETWORK_TREE_VIEW_ID);
                 }
 
                 catch (PartInitException e) {
                     NeoCorePlugin.error(null, e);
                 }
             }
         }, false);
 
         try {
             IMap map = ApplicationGIS.getActiveMap();
             if (curService != null && gis != null && findLayerByNode(map, gis) == null && confirmLoadNetworkOnMap(map, basename)) {
                 List<IGeoResource> listGeoRes = new ArrayList<IGeoResource>();
                 for (IGeoResource iGeoResource : curService.resources(null)) {
                     if (iGeoResource.canResolve(Node.class)) {
                         if (iGeoResource.resolve(Node.class, null).equals(gis)) {
                             listGeoRes.add(iGeoResource);
                             ApplicationGIS.addLayersToMap(map, listGeoRes, 0);
                             break;
                         }
                     }
                 };
             }
         } catch (IOException e) {
             // TODO Handle IOException
             throw (RuntimeException)new RuntimeException().initCause(e);
         }
     }
 
     /**
      * Confirm load network on map
      * 
      * @param map map
      * @param fileName name of loaded file
      * @return true or false
      */
     public static boolean confirmLoadNetworkOnMap(final IMap map, final String fileName) {
         return (Integer)ActionUtil.getInstance().runTaskWithResult(new RunnableWithResult() {
             int result;
 
             @Override
             public void run() {
                 String message = String.format(NeoLoaderPluginMessages.ADD_LAYER_MESSAGE, fileName, map.getName());
                 if (map == ApplicationGIS.NO_MAP) {
                     message = String.format(NeoLoaderPluginMessages.ADD_NEW_MAP_MESSAGE, fileName);
                 }
                 MessageBox msg = new MessageBox(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(), SWT.YES | SWT.NO);
                 msg.setText(NeoLoaderPluginMessages.ADD_LAYER_TITLE);
                 msg.setMessage(message);
                 result = msg.open();
             }
 
             @Override
             public Object getValue() {
                 return result;
             }
         }) == SWT.YES;
     }
 
     /**
      *Returns layer, that contains necessary gis node
      * 
      * @param map map
      * @param gisNode gis node
      * @return layer or null
      */
     public static ILayer findLayerByNode(IMap map, Node gisNode) {
         try {
             for (ILayer layer : map.getMapLayers()) {
                 IGeoResource resource = layer.findGeoResource(Node.class);
                 if (resource != null && resource.resolve(Node.class, null).equals(gisNode)) {
                     return layer;
                 }
             }
             return null;
         } catch (IOException e) {
             // TODO Handle IOException
             e.printStackTrace();
             return null;
         }
     }
 
     private static void debug(String line) {
 		NeoLoaderPlugin.debug(line);
 	}
 	
 	private static void info(String line){
 		NeoLoaderPlugin.info(line);
 	}
 	
 	private static void error(String line){
 		System.err.println(line);
 	}
 	
 	private boolean parseLine(String line){
 		debug(line);
         String fields[] = line.split("\\t");
 		if(fields.length>2){
 			if(headers==null){	// first line
 				headers = fields;
 				headerIndex = new HashMap<String,Integer>();
 				mainIndexes = new int[]{-1,-1,-1,-1,-1,-1};
 				int index=0;
 				for(String header:headers){
 					debug("Added header["+index+"] = "+header);
                     if(NetworkElementTypes.BSC.matches(header)) mainIndexes[0]=index;
                     else if(NetworkElementTypes.CITY.matches(header)) mainIndexes[1]=index;
 					else if(NetworkElementTypes.SITE.matches(header)) mainIndexes[2]=index;
 					else if(NetworkElementTypes.SECTOR.matches(header)) mainIndexes[3]=index;
                     else if(header.toLowerCase().startsWith("lat")) mainIndexes[4]=index;
                     else if(header.toLowerCase().startsWith("long")) mainIndexes[5]=index;
                     else if(header.toLowerCase().startsWith("y_wert")) {mainIndexes[4]=index; crsHint="germany";}
                     else if(header.toLowerCase().startsWith("x_wert")) {mainIndexes[5]=index; crsHint="germany";}
                    else if(header.toLowerCase().startsWith("easting")) mainIndexes[4]=index;
                    else if(header.toLowerCase().startsWith("northing")) mainIndexes[5]=index;
                     else if(header.toLowerCase().startsWith("trx")) intIndexes.add(index);
                     else stringIndexes.add(index);
 					headerIndex.put(header,index++);
 				}
 			}else{
 				Transaction transaction = neo.beginTx();
 				try {
                     if (gis == null) {
                         gis = getGISNode(neo, INeoConstants.GIS_PREFIX + basename);
                     }
                     String bscField = null;
                     String cityField = null;
                     try {
                         bscField = fields[mainIndexes[0]];
                     } catch (RuntimeException e1) {
                     }
                     try {
                         cityField = fields[mainIndexes[1]];
                     } catch (RuntimeException e1) {
                     }
 					String siteField = fields[mainIndexes[2]];
 					String sectorField = fields[mainIndexes[3]];
 					if (trimSectorName) {
                         sectorField = sectorField.replaceAll(siteField + "[\\:\\-]?", "");
                     }
 					if(siteField.contains("306460123A")) {
 					    System.out.println("debug");
 					}
                     if (network==null){
                         network = getNetwork(neo, gis, basename);
                         if (network==null){
                             return false;
                         }
                         network.setProperty(INeoConstants.PROPERTY_FILENAME_NAME, filename);
                     }
                     if (cityField!=null && !cityField.equals(cityName)) {
                         cityName = cityField;
                         city = city_s.get(cityField);
                         if (city == null) {
                             debug("New City: " + cityName);
                             city = addChild(network, NetworkElementTypes.CITY.toString(), cityName);
                             city_s.put(cityField, city);
                         }
                     }
                     if (bscField!=null && !bscField.equals(bscName)) {
                         bscName = bscField;
                         bsc = bsc_s.get(bscField);
                         if (bsc == null) {
                             debug("New BSC: " + bscName);
                             bsc = addChild(city == null ? network : city, NetworkElementTypes.BSC.toString(), bscName);
                             bsc_s.put(bscField, bsc);
                         }
                     }
 					if (!siteField.equals(siteName)) {
 						siteName = siteField;
 						debug("New site: " + siteName);
 						Node siteRoot = bsc==null ? (city==null ? network : city) : bsc;
                         Node newSite = addChild(siteRoot, NetworkElementTypes.SITE.toString(), siteName);
 				        (site==null ? network : site).createRelationshipTo(newSite, GeoNeoRelationshipTypes.NEXT);
 				        site = newSite;
 				        siteNumber++;
 						float lat = Float.parseFloat(fields[mainIndexes[4]]);
 						float lon = Float.parseFloat(fields[mainIndexes[5]]);
 						if(crs==null){
 							crs = CRS.fromLocation(lat, lon, crsHint);
                             //network.setProperty(INeoConstants.PROPERTY_CRS_TYPE_NAME, crs.getType());
                             //network.setProperty(INeoConstants.PROPERTY_CRS_NAME, crs.toString());
                             gis.setProperty(INeoConstants.PROPERTY_CRS_TYPE_NAME, crs.getType());
                             gis.setProperty(INeoConstants.PROPERTY_CRS_NAME, crs.toString());
 						}
 						site.setProperty(INeoConstants.PROPERTY_LAT_NAME, lat);
 						site.setProperty(INeoConstants.PROPERTY_LON_NAME, lon);
 						if(bbox==null) {
 						    bbox = new double[]{lon,lon,lat,lat};
 						}else{
                             if(bbox[0]>lon) bbox[0]=lon;
                             if(bbox[1]<lon) bbox[1]=lon;
                             if(bbox[2]>lat) bbox[2]=lat;
                             if(bbox[3]<lat) bbox[3]=lat;
 						}
 					}
 					debug("New Sector: " + sectorField);
 					Node sector = addChild(site, NetworkElementTypes.SECTOR.toString(), sectorField);
                     sectorNumber++;
 					if(!haveTypedIndexes){
                         determineFieldTypes(fields);
 					}
 					try {
                         for (int i : stringIndexes) {
                             if (fields[i].length() > 0) {
                                 sector.setProperty(headers[i], fields[i]);
                             } else {
                                 emptyFields.add("Empty string at " + lineNumber + ":" + i);
                             }
                         }
                         for (int i : intIndexes) {
                             if (fields[i].length() > 0) {
                                 try {
                                     sector.setProperty(headers[i], Integer.parseInt(fields[i]));
                                 } catch (NumberFormatException e) {
                                     badFields.add("Invalid integer '" + fields[i] + "' at " + lineNumber + ":" + i + " ("
                                             + headers[i] + ")");
                                 }
                             } else {
                                 emptyFields.add("Empty string at " + lineNumber + ":" + i);
                             }
                         }
                         for (int i : floatIndexes) {
                             if (fields[i].length() > 0) {
                                 try {
                                     sector.setProperty(headers[i], Float.parseFloat(fields[i]));
                                 } catch (NumberFormatException e) {
                                     badFields.add("Invalid float '" + fields[i] + "' at " + lineNumber + ":" + i + " ("
                                             + headers[i] + ")");
                                 }
                             } else {
                                 emptyFields.add("Empty string at " + lineNumber + ":" + i);
                             }
                         }
                     } catch (ArrayIndexOutOfBoundsException e) {
                         shortLines.add("Empty fields at end of line " + lineNumber + ": "+e.getMessage());
                     }
 					transaction.success();
 					return true;
 				} catch(Exception e) {
 				    lineErrors.add("Error parsing line " + lineNumber + ": " + e);
                     error(lineErrors.get(lineErrors.size() - 1));
                     if (lineErrors.size() == 1) {
                         e.printStackTrace(System.err);
                     } else if (lineErrors.size() > 10) {
                         e.printStackTrace(System.err);
                         return false;
                     }
 				} finally {
 					transaction.finish();
 				}
 			}
 		}
         return true;
 	}
 
     private void determineFieldTypes(String[] fields) {
         for(int i : stringIndexes){
             try {
                 Float.parseFloat(fields[i]);
                 floatIndexes.add(i);
                 Integer.parseInt(fields[i]);
                 floatIndexes.remove(floatIndexes.size()-1);
                 intIndexes.add(i);
             }catch(Exception e){
             }
         }
         for(int i:intIndexes) {
             stringIndexes.remove((Integer)i);
         }
         for(int i:floatIndexes) {
             stringIndexes.remove((Integer)i);
         }
         Collections.sort(stringIndexes);
         Collections.sort(intIndexes);
         Collections.sort(floatIndexes);
         haveTypedIndexes = true;
     }
 
 	private static void deleteTree(Node root) {
         if (root != null) {
             for (Relationship relationship : root.getRelationships(NetworkRelationshipTypes.CHILD, Direction.OUTGOING)) {
                 Node node = relationship.getEndNode();
                 deleteTree(node);
                 debug("Deleting node " + node + ": " + (node.hasProperty("name") ? node.getProperty("name") : ""));
                 deleteNode(node);
             }
         }
     }
 	
 	private static void deleteNode(Node node) {
         if (node != null) {
             for (Relationship relationship : node.getRelationships()) {
                 relationship.delete();
             }
             node.delete();
         }
     }
 
     /**
      * This code finds the specified network node in the database, creating its own transaction for
      * that.
      * 
      * @param gis gis node
      */
 	public static Node getNetwork(NeoService neo, Node gis, String basename) {
 		Node network = null;
 		Transaction transaction = neo.beginTx();
 		try {
             for (Relationship relationship : gis.getRelationships(GeoNeoRelationshipTypes.NEXT, Direction.OUTGOING)) {
 				Node node = relationship.getEndNode();
                 debug("Testing possible Network node "+node+": "+(node.hasProperty("name") ? node.getProperty("name") : ""));
 				if (node.hasProperty(INeoConstants.PROPERTY_TYPE_NAME) && node.getProperty(INeoConstants.PROPERTY_TYPE_NAME).equals(NetworkElementTypes.NETWORK.toString()) && node.hasProperty(INeoConstants.PROPERTY_NAME_NAME)
 						&& node.getProperty(INeoConstants.PROPERTY_NAME_NAME).equals(basename)){
 	                debug("Found matching Network node "+node+": "+(node.hasProperty("name") ? node.getProperty("name") : ""));
 			        try {
 			            PlatformUI.getWorkbench();
 	                    if(!askIfOverwrite()) return null;
 	                    // delete network - begin - gis node.
                         // remove all incoming relationships
                         for (Relationship relationshipIn : node.getRelationships(Direction.INCOMING)) {
                             relationshipIn.delete();
                         }
                         NeoServiceProvider.getProvider().commit();
                         NeoCorePlugin.getDefault().getProjectService().deleteNode(node);
                         NeoServiceProvider.getProvider().commit();
                         // deleteNodeInNewThread(node);
 			        } catch(IllegalStateException e) {
 			            // we are in test mode, automatically agree to overwrite network
 			            deleteTree(node);
 			            deleteNode(node);
 			        }
                     break;
                 }
 			}
 			network = neo.createNode();
 			network.setProperty(INeoConstants.PROPERTY_TYPE_NAME, NetworkElementTypes.NETWORK.toString());
 			network.setProperty(INeoConstants.PROPERTY_NAME_NAME, basename);
             gis.createRelationshipTo(network, GeoNeoRelationshipTypes.NEXT);
 			transaction.success();
 		}catch (Exception e){
 		    e.printStackTrace();
 		} finally {
 			transaction.finish();
 		}
 		return network;
 	}
 
     private static boolean askIfOverwrite() {
         int resultMsg = (Integer)ActionUtil.getInstance().runTaskWithResult(new RunnableWithResult() {
             int result;
             @Override
             public void run() {
                 MessageBox msg = new MessageBox(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(), SWT.YES
                         | SWT.NO);
                 msg.setText(LOAD_NETWORK_TITLE);
                 msg.setMessage(LOAD_NETWORK_MSG);
                 result = msg.open();
             }
 
             @Override
             public Object getValue() {
                 return new Integer(result);
             }
         });
         return resultMsg == SWT.YES;
     }
 
     private static Node getGISNode(NeoService neo, String gisName) {
         Node gis = null;
         Transaction transaction = neo.beginTx();
         try {
             Node reference = neo.getReferenceNode();
             for (Relationship relationship : reference.getRelationships(Direction.OUTGOING)) {
                 Node node = relationship.getEndNode();
                 debug("Testing possible GIS node "+node+": "+(node.hasProperty("name") ? node.getProperty("name") : ""));
                 if (node.hasProperty(INeoConstants.PROPERTY_TYPE_NAME)
                         && node.getProperty(INeoConstants.PROPERTY_TYPE_NAME).equals(INeoConstants.GIS_TYPE_NAME)
                         && node.hasProperty(INeoConstants.PROPERTY_NAME_NAME)
                         && node.getProperty(INeoConstants.PROPERTY_NAME_NAME).toString().equals(gisName))
                     return node;
             }
             gis = neo.createNode();
             gis.setProperty(INeoConstants.PROPERTY_TYPE_NAME, INeoConstants.GIS_TYPE_NAME);
             gis.setProperty(INeoConstants.PROPERTY_NAME_NAME, gisName);
             gis.setProperty(INeoConstants.PROPERTY_GIS_TYPE_NAME, GisTypes.Network.getHeader());
             reference.createRelationshipTo(gis, NetworkRelationshipTypes.CHILD);
             // gis.createRelationshipTo(network, GeoNeoRelationshipTypes.NEXT);
             transaction.success();
         } finally {
             transaction.finish();
         }
         return gis;
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
 		child.setProperty(INeoConstants.PROPERTY_TYPE_NAME, type);
 		child.setProperty(INeoConstants.PROPERTY_NAME_NAME, name);
 		if (parent != null) {
 			parent.createRelationshipTo(child, NetworkRelationshipTypes.CHILD);
 			debug("Added '" + name + "' as child of '" + parent.getProperty(INeoConstants.PROPERTY_NAME_NAME));
 		}
 		return child;
 	}
 	
 	public void printStats(boolean verbose) {
         if (network != null) {
             if (verbose) {
                 Transaction tx = neo.beginTx();
                 try {
                     printChildren(network, 0);
                     tx.success();
                 } finally {
                     tx.finish();
                 }
             }
             info("Finished loading "+siteNumber+" sites and "+sectorNumber+" sectors from "+lineNumber+" lines");
         } else {
             error("No network node found");
         }
     }
 
 	private static void printChildren(Node node, int depth) {
 		if(node==null || depth > 4 || !node.hasProperty(INeoConstants.PROPERTY_NAME_NAME)) return;
 		StringBuffer tab = new StringBuffer();
 		for(int i=0;i<depth;i++) tab.append("    ");
 		StringBuffer properties = new StringBuffer();
 		for(String property:node.getPropertyKeys()) {
 			if(!property.equals(INeoConstants.PROPERTY_NAME_NAME)) properties.append(" - ").append(property).append(" => ").append(node.getProperty(property));
 		}
 		info(tab.toString()+node.getProperty(INeoConstants.PROPERTY_NAME_NAME)+properties);
 		for(Relationship relationship:node.getRelationships(NetworkRelationshipTypes.CHILD,Direction.OUTGOING)){
 			//debug(tab.toString()+"("+relationship.toString()+") - "+relationship.getStartNode().getProperty("name")+" -("+relationship.getType()+")-> "+relationship.getEndNode().getProperty("name"));
 			printChildren(relationship.getEndNode(),depth+1);
 		}
 	}
 
 	/**
 	 * A main method for useful quick-turn-around testing and debugging of data parsing on various sample files.
 	 * @param args
 	 */
 	public static void main(String[] args) {
 	    //NeoLoaderPlugin.debug = true;
 		EmbeddedNeo neo = new EmbeddedNeo("../../var/neo");
 		try{
 		    long startTime = System.currentTimeMillis();
 			NetworkLoader networkLoader = new NetworkLoader(neo,args.length>0 ? args[0] : "amanzi/network.txt");
 			networkLoader.run();
 			networkLoader.printStats(true);
             info("Ran test in "+(System.currentTimeMillis()-startTime)/1000.0+"s");
 		} catch (IOException e) {
 			System.err.println("Failed to load network: "+e);
 			e.printStackTrace(System.err);
 		}finally{
 			neo.shutdown();
 		}
 	}
 }
