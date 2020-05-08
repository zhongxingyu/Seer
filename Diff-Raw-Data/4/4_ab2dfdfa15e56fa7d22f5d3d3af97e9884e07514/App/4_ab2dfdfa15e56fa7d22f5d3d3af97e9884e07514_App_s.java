 package no.steria.sbang.chinookextractor;
 
 import java.sql.Connection;
 import java.sql.DriverManager;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.Statement;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
import org.elasticsearch.action.index.IndexResponse;
 import org.elasticsearch.client.Client;
 import org.elasticsearch.node.Node;
 import static org.elasticsearch.node.NodeBuilder.*;
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 /**
  * Hello world!
  *
  */
 public class App 
 {
     public static void main( String[] args )
     {
         Connection connection = null;
         Statement statement = null;
         ResultSet musicPurchaseResultSet = null;
         ResultSet albumResultSet = null;
         ResultSet customerResultSet = null;
 
     
         String url = "jdbc:postgresql://localhost/Chinook";
         String user = "chinook";
         String password = "klrp320";
 
         try {
             connection = DriverManager.getConnection(url, user, password);
             statement = connection.createStatement();
             
             // Get the purchased tracks with the IDs of the album and the customer. 
             musicPurchaseResultSet = statement.executeQuery("SELECT \"Name\",\"Composer\",\"InvoiceLine\".\"UnitPrice\" as \"Price\",\"AlbumId\" as \"Album\", \"CustomerId\" as \"Customer\" from \"InvoiceLine\", \"Invoice\", \"Track\" where \"InvoiceLine\".\"TrackId\" = \"Track\".\"TrackId\" and \"InvoiceLine\".\"InvoiceId\" = \"Invoice\".\"InvoiceId\"");
             JSONArray musicPurchaseJson = ResultSetConverter.convert(musicPurchaseResultSet);
             
             // Get the list of albums as a JSON array.
             albumResultSet = statement.executeQuery("SELECT \"AlbumId\", \"Title\", \"Name\" as \"Artist\" from \"Album\", \"Artist\" where \"Album\".\"ArtistId\" = \"Artist\".\"ArtistId\"");
             JSONArray albumJson = ResultSetConverter.convert(albumResultSet);
             
             // Get the list of customers as a JSON array.
             customerResultSet = statement.executeQuery("SELECT * from \"Customer\"");
             JSONArray customerJson = ResultSetConverter.convert(customerResultSet);
             
             createNestedAlbums(musicPurchaseJson, albumJson);
             createNestedCustomer(musicPurchaseJson, customerJson);
 
             pushJsonRecordsToElasticsearch(musicPurchaseJson);
             printJsonLines(musicPurchaseJson);
 
         } catch (SQLException ex) {
             Logger logger = Logger.getLogger(App.class.getName());
             logger.log(Level.SEVERE, ex.getMessage(), ex);
 
         } catch (JSONException e) {
 			e.printStackTrace();
 		} finally {
             try {
                 if (musicPurchaseResultSet != null) {
                     musicPurchaseResultSet.close();
                 }
                 if (albumResultSet != null) {
                 	albumResultSet.close();
                 }
                 if (customerResultSet != null) {
                 	customerResultSet.close();
                 }
                 if (statement != null) {
                     statement.close();
                 }
                 if (connection != null) {
                     connection.close();
                 }
 
             } catch (SQLException ex) {
                 Logger logger = Logger.getLogger(App.class.getName());
                 logger.log(Level.WARNING, ex.getMessage(), ex);
             }
         }    }
 
 	private static void createNestedAlbums(JSONArray musicPurchaseJson, JSONArray albumJson) {
 		for(int i=0; i<musicPurchaseJson.length(); ++i) {
 			JSONObject purchase;
 			try {
 				purchase = musicPurchaseJson.getJSONObject(i);
 				int albumId = purchase.getInt("Album");
 				JSONObject album = findAlbumMatchingAlbumId(albumId, albumJson);
 				if (null != album) {
 					purchase.put("Album", album);
 				}
 			} catch (JSONException e) {
 				e.printStackTrace();
 			}
 		}
 	}
 
 	private static JSONObject findAlbumMatchingAlbumId(int albumId, JSONArray albumJson) throws JSONException {
 		for (int j=0; j<albumJson.length(); ++j) {
 			JSONObject album = albumJson.getJSONObject(j);
 			if (albumId == album.getInt("AlbumId")) {
 				return album;
 			}
 		}
 		
 		return null;
 	}
 
 	private static void createNestedCustomer(JSONArray musicPurchaseJson, JSONArray customerJson) {
 		for(int i=0; i<musicPurchaseJson.length(); ++i) {
 			JSONObject purchase;
 			try {
 				purchase = musicPurchaseJson.getJSONObject(i);
 				int customerId = purchase.getInt("Customer");
 				JSONObject customer = findCustomerMatchingCustomerId(customerId, customerJson);
 				if (null != customer) {
 					purchase.put("Customer", customer);
 				}
 			} catch (JSONException e) {
 				e.printStackTrace();
 			}
 		}
 	}
 
 	private static JSONObject findCustomerMatchingCustomerId(int customerId, JSONArray customerJson) throws JSONException {
 		for (int j=0; j<customerJson.length(); ++j) {
 			JSONObject customer = customerJson.getJSONObject(j);
 			if (customerId == customer.getInt("CustomerId")) {
 				return customer;
 			}
 		}
 		
 		return null;
 	}
 
 	private static void printJsonLines(JSONArray jsonArray)
 			throws JSONException {
 		for(int i=0; i<jsonArray.length(); ++i) {
 			JSONObject rowAsJson = jsonArray.getJSONObject(i);
 			System.out.println(rowAsJson);
 		}
 	}
 
 	private static void pushJsonRecordsToElasticsearch(JSONArray jsonArray) throws JSONException {
 		Node node = null;
 		try{
 			node = nodeBuilder().node();
 			Client client = node.client();
 			for(int i=0; i<jsonArray.length(); ++i) {
 				JSONObject rowAsJson = jsonArray.getJSONObject(i);
				IndexResponse response = client.prepareIndex("chinook", "purchase").setSource(rowAsJson.toString()).execute().actionGet();
 			}
 		} finally {
 			if (null != node) {
 				node.close();
 			}
 		}
 	}
 }
