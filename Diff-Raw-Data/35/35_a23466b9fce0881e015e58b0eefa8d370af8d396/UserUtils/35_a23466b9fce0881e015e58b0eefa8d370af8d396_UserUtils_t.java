 package pizzawatch.utils;
 
 import java.io.UnsupportedEncodingException;
 import java.security.MessageDigest;
 import java.security.NoSuchAlgorithmException;
 import java.util.ArrayList;
 import java.util.LinkedList;
 import javax.swing.table.TableModel;
 import pizzawatch.datamodels.User;
 import pizzawatch.sql.sqlreader.ResultSetParser;
 import pizzawatch.sql.sqlreader.SqlScriptReader;
 
 public class UserUtils
 {
     //leonardo1         watern4tur3  5CA888F39D61ADFE533B0C08BD9F884EE6FF83D69C1221491ECAD366DC56B646
     //raphael2          red          B1F51A511F1DA0CD348B8F8598DB32E61CB963E5FC69E2B41485BF99590ED75A
     //michaelangelo3    kawabanga    C207B1B9E510364443DB9423B36BC5F16DF95DE58A544A64B9D80B0FEBA78065
     //donatello4        purple       8E0A1B0ADA42172886FD1297E25ABF99F14396A9400ACBD5F20DA20289CFF02F
     //mastersplinter10  p3aceinm1nd  6B649D9C83A8E2E01B9B34F442AF5A25797EFE2187F9528DA0C481CDF4A4E1E0
 
     private static final SqlScriptReader SQL_READER = SqlScriptReader.getInstance();
 
     public static User getUserFromDB(String userID)
     {
         User user = new User(userID, getUserIsAdmin(userID), getUserAttributeFromDB(userID, "name"), getUserAttributeFromDB(userID, "cardNumber"));
         return user;
     }
 
     private static String getUserAttributeFromDB(String userID, String attribute)
     {
         final String QUERY_STRING = "SELECT * FROM Users WHERE userID = '" + userID + "'";
         ArrayList<LinkedList<String>> attributesList = ResultSetParser.parseResultSetIntoArray(SQL_READER.query(QUERY_STRING), attribute);
 
         return attributesList.get(0).get(0);
     }
 
     private static boolean getUserIsAdmin(String userID)
     {
         final String QUERY_STRING = "SELECT * FROM PrivilegedUser WHERE userID = '" + userID + "'";
         ArrayList<LinkedList<String>> attributesList = ResultSetParser.parseResultSetIntoArray(SQL_READER.query(QUERY_STRING), "userID");
 
         try
         {
             return attributesList.get(0).get(0) != null;
         }
         catch(IndexOutOfBoundsException ex)
         {
             return false;
         }
     }
 
     /**
      * Returns the user's ID given their name
      * @param name The user's name
      * @return The user's ID, or null if there is no corresponding ID
      */
     public static String getUserIDFromName(String name)
     {
         ArrayList<LinkedList<String>> users =
             ResultSetParser.parseResultSetIntoArray(SQL_READER.query("SELECT userID FROM Users WHERE name = '" + name + "'"), "userID");
         try
         {
             return users.get(0).get(0);
         }
         catch(IndexOutOfBoundsException ex)
         {
             return null;
         }
     }
 
     /**
      * Compares a given password hash and the real password hash of the given user
      * @param userID
      * @param givenPassword
      * @return If the given password matches the password on the DB
      */
     public static boolean isPasswordCorrect(String userID, String givenPassword)
     {
         final String ATTRIBUTES_STRING = "passwordHash";
         final String QUERY_STRING = "SELECT * FROM Users WHERE userID = '" + userID + "'";
 
         ArrayList<LinkedList<String>> attributesList = ResultSetParser.parseResultSetIntoArray(SQL_READER.query(QUERY_STRING), ATTRIBUTES_STRING);
 
         String hashOfGivenPassword = hashPassword(givenPassword);
 
         String realPasswordHash = attributesList.get(0).get(0);
         if(realPasswordHash == null)
         {
             return false;
         }
 
         return realPasswordHash.equals(hashOfGivenPassword); //hashOfGivenPassword can be null, but equals can deal with that
     }
 
     /**
      * check if user admin
      * join query
      * @param userID
      * @return true when an admin user
      */
     public static boolean checkAdmin(String userID)
     {
         ArrayList<LinkedList<String>> users = ResultSetParser.parseResultSetIntoArray(SQL_READER.query("SQL_Scripts/checkAdmin.sql"), "name");
 
         if(users == null) {return false;}
         else
         {
             for(int i = 0; i < users.get(0).size(); i++)
             {
                 String values = users.get(0).get(i);
                 if(values == null)
                 {
                     return false;
                 }
                 else if(values.equals(userID))
                 {
                     return true;
                 }
             }
         }
         return false;
     }
 
     /**
      * Convert byte array to a (caps) hex string
      * Taken from http://stackoverflow.com/questions/9655181/convert-from-byte-array-to-hex-string-in-java
      * @param userID
      * @param userPass
      */
     private static String convertToHex(byte[] bytes)
     {
         final char[] hexArray = "0123456789ABCDEF".toCharArray();
         char[] hexChars = new char[bytes.length * 2];
         int v;
         for (int j = 0; j < bytes.length; j++)
         {
             v = bytes[j] & 0xFF;
             hexChars[j * 2] = hexArray[v >>> 4];
             hexChars[j * 2 + 1] = hexArray[v & 0x0F];
         }
         return new String(hexChars);
     }
 
     /**
      * Hash secure pass
      * @param userPass
      * @return The hash, or null if there is an error
      */
     public static String hashPassword(String userPass)
     {
         try
         {
             MessageDigest md = MessageDigest.getInstance("SHA-256");
             md.update(userPass.getBytes("UTF-8"), 0, userPass.length());
             return convertToHex(md.digest());
         }
         catch(NoSuchAlgorithmException | UnsupportedEncodingException ex)
         {
             return null;
         }
     }
 
     /**
      * Returns a TableModel containing the Past Orders for the given users
      * Assumes userIDs[0] is defined
      * Our Division Query
      * @param userIDs An array containing the user IDs
      * @return A TableModel for use by the PastOrdersFrame JList
      */
     public static TableModel getPastOrdersTableModel(String[] userIDs)
     {
         //TODO protect against non-admin multiuser queries
         final String ATTRIBUTES_STRING = "userID;oid;deliveryMethod;pizzaType;address"; //Keep in sync with TABLE_TITLES
         final String[] TABLE_TITLES = {"User ID", "Order ID", "Delivery Method", "Pizza Type", "Address"}; //Keep in sync with ATTRIBUTES_STRING
         DefaultTableModelNoEdit tableModel = new DefaultTableModelNoEdit();
 
         String queryString = "SELECT * FROM PizzaOrder po WHERE po.userID IN " +
                              "(SELECT u.userID FROM Users u WHERE u.userID = '" + userIDs[0] + "'"; //Add the first user ID
         for(int x = 1; x < userIDs.length; x++)
         {
             queryString += " OR u.userID = '" + userIDs[x] + "'"; //Add any remaining user IDs
         }
         queryString += ")";
 
         ArrayList<LinkedList<String>> attributesList = ResultSetParser.parseResultSetIntoArray(SQL_READER.query(queryString), ATTRIBUTES_STRING);
         for(int x = 0; x < attributesList.size(); x++)
         {
             LinkedList<String> attributesDataList = attributesList.get(x);
             tableModel.addColumn(TABLE_TITLES[x], attributesDataList.toArray());
         }
 
         return tableModel;
     }
 
     /**
      * Deletes an order if the current user is an admin
      * Our delete query.
      * @param uid The user ID
      * @param oid The order ID
      * @param admin
      */
    public static void deleteOrders(String uid, int oid, boolean admin)
     {
         if(admin)
         {
             String del_query = "DELETE FROM PizzaOrder WHERE userID = '" + uid + "' AND oid = '" + oid + "'";
             SQL_READER.insertUpdateCreateDelete(del_query);
         }
     }
 
     /**
      * our nested aggregate query
      * @param admin
      * @return
      */
     public static String punish(boolean admin)
     {
         if(admin)
         {
             String sum_query = "CREATE VIEW [User Total Sum] AS" +
                        "SELECT SUM(p.price) AS user_sum, u.userID" +
                        "FROM Users u, PizzaOrder po, Pizza p" +
                        "WHERE u.userID = po.userID AND po.pizzaType = p.PizzaType" +
                        "GROUP BY u.userID";
             String max_query = "SELECT MAX(user_sum), userID FROM [User Total Sum] GROUP BY userID";
             SQL_READER.query(sum_query);
             ArrayList<LinkedList<String>> total_user_sum = ResultSetParser.parseResultSetIntoArray(SQL_READER.query(max_query), "userID");
             return total_user_sum.get(0).get(0);
         }
         return null;
     }
 
     /**
      * our aggregate query
      * @param name
      * @return
      */
     public static String getTotalSum(String name)
     {
         String sum_query = "SELECT SUM(p.price)" +
                            "FROM Users u, PizzaOrder po, Pizza p" +
                            "WHERE u.userID = po.userID AND po.pizzaType = p.PizzaType AND u.userID = '" + getUserIDFromName(name) + "' " +
                            "GROUP BY u.userID";
         ArrayList<LinkedList<String>> total_user_sum = ResultSetParser.parseResultSetIntoArray(SQL_READER.query(sum_query), "price");
         return total_user_sum.get(0).get(0);
     }
 
     private UserUtils() {}
 }
