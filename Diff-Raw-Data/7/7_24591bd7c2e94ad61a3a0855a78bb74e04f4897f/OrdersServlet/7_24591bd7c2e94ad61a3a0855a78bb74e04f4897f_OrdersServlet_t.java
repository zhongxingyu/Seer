 package com.ibm.opensocial.landos;
 
 import org.apache.wink.json4j.JSONException;
 import org.apache.wink.json4j.JSONWriter;
 
 import java.io.IOException;
 import java.sql.Connection;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 public class OrdersServlet extends BaseServlet {
   private static final String CLAZZ = OrdersServlet.class.getName();
   private static final Logger LOGGER = Logger.getLogger(CLAZZ);
   
   /**
    * GET /orders/<runid>[?user=<user>[&item=<bar>]]
    * @throws IOException 
    */
   @Override
   public void doGet(HttpServletRequest req, HttpServletResponse res) throws IOException {
     // Set headers
     setCacheAndTypeHeaders(res);
     
     // Get the run id
     int rid = Integer.parseInt(getPathSegment(req, 0));
     
     // Get the user and item parameters (if any)
     String user = req.getParameter("user");
     String item = req.getParameter("item");
     
     // Prepare database variables
     Connection conn = null;
     PreparedStatement stmt = null;
     ResultSet results = null;
     
     // Writer
     JSONWriter writer = getJSONWriter(res);
     
     // Query
     String query = "SELECT * FROM orders WHERE rid = ?";
     
     try {
       conn = getDataSource(req).getConnection();
       // Parameter control forks
       if (user == null) {
         // Neither user nor item are set
         stmt = conn.prepareStatement(query);
         stmt.setInt(1, rid);
       } else {
         // User is set
         query += " AND user = ?";
         if (item == null) {
           // Only user is set
           stmt = conn.prepareStatement(query);
           stmt.setInt(1, rid);
           stmt.setString(2, user);
         } else {
           // Item is set
           query += " AND item = ?";
           stmt = conn.prepareStatement(query);
           stmt.setInt(1, rid);
           stmt.setString(2, user);
           stmt.setString(3, item);
         }
       }
       // Prepared statement is now ready for execution
       results = stmt.executeQuery();
       writer.array();
       while (results.next()) {
         writeJSONObjectOrder(writer, results.getInt(1), results.getString(2), results.getString(3), results.getString(4), results.getInt(5), results.getInt(6), results.getString(7));
       }
       writer.endArray();
     } catch (Exception e) {
       LOGGER.logp(Level.SEVERE, CLAZZ, "doGet", e.getMessage());
     } finally {
       close(writer, conn);
     }
   }
   
   /**
    * DELETE /orders/<runid>?user=<user>&item=<bar>
    * @throws IOException 
    */
   @Override
   public void doDelete(HttpServletRequest req, HttpServletResponse res) throws IOException {
     // Set headers
     setCacheAndTypeHeaders(res);
     
     // Get the user and item parameters
     String user = req.getParameter("user");
     String item = req.getParameter("item");
     
     // Writer
     JSONWriter writer = getJSONWriter(res);
     
     // Check for required parameters
     if (numSegments(req) < 1 || user == null || item == null) {
       try {
         writer.object().key("error").value("Deleting requires a run id, user id, and an item.");
       } catch (Exception e) {
         LOGGER.logp(Level.SEVERE, CLAZZ, "doDelete", e.getMessage());
       } finally {
         close(writer);
       }
       return;
     }
     
     // Get the run id
     int rid = Integer.parseInt(getPathSegment(req, 0));
     
     // Prepare database variables
     Connection conn = null;
     PreparedStatement stmt = null;
     
     // Query
    String query = "DELETE FROM orders WHERE rid = ? AND user = ? AND item = ?";
     
     try {
       conn = getDataSource(req).getConnection();
       stmt = conn.prepareStatement(query);
       stmt.setInt(1, rid);
       stmt.setString(2, user);
       stmt.setString(3, item);
       writer.object();
      writer.key("delete").value(stmt.executeUpdate());
       writer.endObject();
     } catch (Exception e) {
       LOGGER.logp(Level.SEVERE, CLAZZ, "doDelete", e.getMessage());
     } finally {
       close(writer, conn);
     }
   }
   
   /**
    * PUT /orders/<runid>?user=<user>&item=<item>&price=<price>[&size=<size>][&qty=<qty>][&comments=<comments>]
    * @throws IOException 
    */
   @Override
   public void doPut(HttpServletRequest req, HttpServletResponse res) throws IOException {
     // Set headers
     setCacheAndTypeHeaders(res);
     
     // Get required parameters
     String run = getPathSegment(req, 0);
     String user = req.getParameter("user");
     String item = req.getParameter("item");
     String price = req.getParameter("price");
     
     // Get optional parameters
     String size = req.getParameter("size");
     String comments = req.getParameter("comments");
     int qty;
     try {
       qty = Integer.parseInt(req.getParameter("qty"));
     } catch (NumberFormatException e) {
       qty = 1;
     }
     
     // Writer
     JSONWriter writer = getJSONWriter(res);
     
     // Check for required parameters
     if (req == null || user == null || item == null || price == null) {
       try {
         writer.object().key("error").value("Putting requires a run id, an user id, an item, and a price.");
       } catch (Exception e) {
         LOGGER.logp(Level.SEVERE, CLAZZ, "doDelete", e.getMessage());
       } finally {
         close(writer);
       }
       return;
     }
     
     // Prepare database variables
     Connection conn = null;
     PreparedStatement stmt = null;
     
     // Query
     String query = "INSERT INTO orders VALUES (?, ?, ?, ?, ?, ?, ?)";
     
     try {
       // Prepare variables
       int rid = Integer.parseInt(run);
       int cents = Integer.parseInt(price);
       
       // Insert into database
       conn = getDataSource(req).getConnection();
       stmt = conn.prepareStatement(query);
       stmt.setInt(1, rid);
       stmt.setString(2, user);
       stmt.setString(3, item);
       stmt.setString(4, size);
       stmt.setInt(5, qty);
       stmt.setInt(6, cents);
       stmt.setString(7, comments);
       
       // Write back
       if (stmt.executeUpdate() > 0) {
         writeJSONObjectOrder(writer, rid, user, item, size, qty, cents, comments);
       } else {
         writer.object().key("error").value("Did not insert order.").endObject();
       }
     } catch (Exception e) {
       LOGGER.logp(Level.SEVERE, CLAZZ, "doPut", e.getMessage());
     } finally {
       close(writer, conn);
     }
   }
   
   /**
    * Writes the values of an order to a JSON object
    * @param writer
    * @param rid
    * @param user
    * @param item
    * @param size
    * @param qty
    * @param price
    * @param comments
    * @throws JSONException 
    * @throws IOException 
    * @throws NullPointerException 
    * @throws IllegalStateException 
    */
   private void writeJSONObjectOrder(JSONWriter writer, int rid, String user, String item, String size, int qty, int price, String comments) throws IllegalStateException, NullPointerException, IOException, JSONException {
     writer.object();
     writer.key("rid").value(rid);
     writer.key("user").value(user);
     writer.key("item").value(item);
     writer.key("size").value(size);
     writer.key("qty").value(qty);
     writer.key("price").value(price);
     writer.key("comments").value(comments);
     writer.endObject();
   }
 }
