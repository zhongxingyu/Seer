 package com.ibm.opensocial.landos;
 
 import java.io.IOException;
 import java.sql.Connection;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import org.apache.wink.json4j.JSONException;
 import org.apache.wink.json4j.JSONWriter;
 
 import com.google.common.base.Strings;
 
 public class OrdersServlet extends BaseServlet {
   private static final long serialVersionUID = 4509166979070691855L;
   private static final String CLAZZ = OrdersServlet.class.getName();
   private static final Logger LOGGER = Logger.getLogger(CLAZZ);
   
   /**
   * GET /orders/<runid>[/orderid][?user=<user>]
    * @throws IOException 
    */
   @Override
   public void doGet(HttpServletRequest req, HttpServletResponse res) throws IOException {
     // Set headers
     setCacheAndTypeHeaders(res);
     
     // Get the run id
     int rid = Integer.parseInt(getPathSegment(req, 0));
     // Get the order id
     String order = getPathSegment(req, 1);
     
     // Get the user and item parameters (if any)
     String user = req.getParameter("user");
     
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
       if (!Strings.isNullOrEmpty(order)) {
         stmt = conn.prepareStatement("SELECT * FROM orders WHERE id = ?");
         stmt.setInt(1, Integer.parseInt(order, 10));
       } else if (user == null) {
         // Neither user nor item are set
         stmt = conn.prepareStatement(query);
         stmt.setInt(1, rid);
       } else {
         // User is set
         query += " AND user = ?";
         // Only user is set
         stmt = conn.prepareStatement(query);
         stmt.setInt(1, rid);
         stmt.setString(2, user);
       }
       // Prepared statement is now ready for execution
       results = stmt.executeQuery();
       writer.array();
       while (results.next()) {
         writeJSONObjectOrder(writer, results.getInt(1), results.getInt(2), results.getString(3), results.getString(4), results.getString(5), results.getInt(6), results.getString(7));
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
   * PUT /orders/<runid>?user=<user>&item=<item>&price=<price>[&size=<size>][&comments=<comments>]
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
     ResultSet result = null;
     
     // Query
     String query = "INSERT INTO `orders` SET `rid`=?, `user`=?, `item`=?, `size`=?, `price`=?, `comments`=?";
     
     try {
       // Prepare variables
       int rid = Integer.parseInt(run);
       int cents = Integer.parseInt(price);
       
       // Insert into database
       conn = getDataSource(req).getConnection();
       stmt = conn.prepareStatement(query, PreparedStatement.RETURN_GENERATED_KEYS);
       stmt.setInt(1, rid);
       stmt.setString(2, user);
       stmt.setString(3, item);
       stmt.setString(4, size);
       stmt.setInt(5, cents);
       stmt.setString(6, comments);
       stmt.executeUpdate();
       result = stmt.getGeneratedKeys();
       
       // Write back
       if (result.first()) {
         writeJSONObjectOrder(writer, result.getInt(1), rid, user, item, size, cents, comments);
       } else {
         writer.object()
           .key("error").value("Did not insert order.")
         .endObject();
       }
     } catch (Exception e) {
       LOGGER.logp(Level.SEVERE, CLAZZ, "doPut", e.getMessage());
     } finally {
       close(result, stmt, conn, writer);
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
   private void writeJSONObjectOrder(JSONWriter writer, int id, int rid, String user, String item, String size, int price, String comments) throws IllegalStateException, NullPointerException, IOException, JSONException {
     writer.object()
       .key("id").value(id)
       .key("rid").value(rid)
       .key("user").value(user)
       .key("item").value(item)
       .key("size").value(size)
       .key("price").value(price)
       .key("comments").value(comments)
     .endObject();
   }
 }
