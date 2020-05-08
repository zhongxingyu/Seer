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
 
     public static final String ORDER_ID_COLUMN_NAME = "Order ID";
 
     private enum OrdersTableModelMode
     {
         PENDING, PAST, CANCEL_REQUESTED_USER_ONLY, CANCEL_REQUESTED_ALL_USERS, NOT_CANCEL_REQUESTED;
     }
 
     private static final SqlScriptReader SQL_READER = SqlScriptReader.getInstance();
 
     public static User getUserFromDB(String userID)
     {
         User user = new User(userID,
                              getUserIsAdmin(userID),
                              getUserAttributeFromDB(userID, "firstName"),
                              getUserAttributeFromDB(userID, "lastName"),
                              getUserAttributeFromDB(userID, "cardNumber"));
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
         ArrayList<LinkedList<String>> attributesList = ResultSetParser.parseResultSetIntoArray(SQL_READER.query("SQL_Scripts/checkAdmin.sql"), "userID");
 
         try
         {
             return attributesList.get(0).get(0).equals(userID);
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
         final String QUERY_STRING = "SELECT passwordHash FROM Users WHERE userID = '" + userID + "'";
 
         ArrayList<LinkedList<String>> attributesList = ResultSetParser.parseResultSetIntoArray(SQL_READER.query(QUERY_STRING), ATTRIBUTES_STRING);
 
         String hashOfGivenPassword = hashPassword(givenPassword);
         try
         {
             String realPasswordHash = attributesList.get(0).get(0);
            return realPasswordHash.equals(hashOfGivenPassword); //hashOfGivenPassword can be null, but equals can deal with that
         }
         catch(IndexOutOfBoundsException ex)
         {
             return false;
         }
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
      * Returns a TableModel containing the Pending or Past Orders for the given users
      * Assumes userIDs[0] is defined
      * Our Division Query
      * @param userIDs An array containing the user IDs
      * @param mode The mode that corresponds to the desired data in the table model.
      *             If mode is CANCEL_REQUESTED_ALL_USERS, userIDs is ignored, and the data for all users is returned
      * @return A TableModel for use by the GUI Frames dealing with PizzaOrders
      */
     private static TableModel getOrdersTableModel(String[] userIDs, OrdersTableModelMode mode)
     {
         //Keep in sync with TABLE_TITLES
         final String ATTRIBUTES_STRING = (mode != OrdersTableModelMode.CANCEL_REQUESTED_ALL_USERS && userIDs.length == 1) ?
                                                 "oid;deliveryMethod;pizzaType;address" :
                                          "userID;oid;deliveryMethod;pizzaType;address";
         //Keep in sync with ATTRIBUTES_STRING
         final String[] TABLE_TITLES = (mode != OrdersTableModelMode.CANCEL_REQUESTED_ALL_USERS && userIDs.length == 1) ?
                                                  new String[] {ORDER_ID_COLUMN_NAME, "Delivery Method", "Pizza Type", "Address"} :
                                       new String[] {"User ID", ORDER_ID_COLUMN_NAME, "Delivery Method", "Pizza Type", "Address"};
 
         DefaultTableModelNoEdit tableModel = new DefaultTableModelNoEdit();
         String queryString;
 
         if(mode != OrdersTableModelMode.CANCEL_REQUESTED_ALL_USERS)
         {
             queryString = "SELECT * FROM PizzaOrder po WHERE po.userID IN " +
                           "(SELECT u.userID FROM Users u WHERE u.userID = '" + userIDs[0] + "'"; //Add the first user ID
             for(int x = 1; x < userIDs.length; x++)
             {
                 queryString += " OR u.userID = '" + userIDs[x] + "'"; //Add any remaining user IDs
             }
 
             switch(mode)
             {
                 case PENDING:
                 {
                     queryString += ") AND po.isDelivered = 0";
                     break;
                 }
                 case PAST:
                 {
                     queryString += ") AND po.isDelivered = 1";
                     break;
                 }
                 case CANCEL_REQUESTED_USER_ONLY:
                 {
                     queryString += ") AND po.isDelivered = 0 AND po.isCancellationRequested = 1";
                     break;
                 }
                 case NOT_CANCEL_REQUESTED:
                 {
                     queryString += ") AND po.isDelivered = 0 AND po.isCancellationRequested = 0";
                     break;
                 }
                 default:
                 {
                     queryString += ")";
                     break;
                 }
             }
         }
         else
         {
             queryString = "SELECT * FROM PizzaOrder po WHERE po.isCancellationRequested = 1";
         }
 
         ArrayList<LinkedList<String>> attributesList = ResultSetParser.parseResultSetIntoArray(SQL_READER.query(queryString), ATTRIBUTES_STRING);
         for(int x = 0; x < attributesList.size(); x++)
         {
             LinkedList<String> attributesDataList = attributesList.get(x);
             tableModel.addColumn(TABLE_TITLES[x], attributesDataList.toArray());
         }
 
         return tableModel;
     }
 
     /**
      * Returns a TableModel containing the Not Cancel Requested Orders for the given users
      * @param userIDs An array containing the user IDs
      * @return A TableModel for use by the RequestCancellationFrame etc JTables
      */
     public static TableModel getNotCancelRequestedOrdersTableModel(String[] userIDs)
     {
         return getOrdersTableModel(userIDs, OrdersTableModelMode.NOT_CANCEL_REQUESTED);
     }
 
     /**
      * Returns a TableModel containing the Cancel Requested Orders for all the users
      * @return A TableModel for use by ManageCancellationReqsFrame
      */
     public static TableModel getCancelRequestedOrdersAllUsersTableModel()
     {
         return getOrdersTableModel(null, OrdersTableModelMode.CANCEL_REQUESTED_ALL_USERS);
     }
 
     /**
      * Returns a TableModel containing the Cancel Requested Orders for the given users
      * @param userIDs An array containing the user IDs
      * @return A TableModel for use by the RequestCancellationFrame etc JTables
      */
     public static TableModel getCancelRequestedOrdersTableModel(String[] userIDs)
     {
         return getOrdersTableModel(userIDs, OrdersTableModelMode.CANCEL_REQUESTED_USER_ONLY);
     }
 
     /**
      * Returns a TableModel containing the Pending Orders for the given users
      * @param userIDs An array containing the user IDs
      * @return A TableModel for use by the PendingOrdersFrame etc JTables
      */
     public static TableModel getPendingOrdersTableModel(String[] userIDs)
     {
         return getOrdersTableModel(userIDs, OrdersTableModelMode.PENDING);
     }
 
     /**
      * Returns a TableModel containing the Past Orders for the given users
      * @param userIDs An array containing the user IDs
      * @return A TableModel for use by the PastOrdersFrame etc JTables
      */
     public static TableModel getPastOrdersTableModel(String[] userIDs)
     {
         return getOrdersTableModel(userIDs, OrdersTableModelMode.PAST);
     }
 
     /**
      * Deletes a PizzaOrder
      * Our delete query
      * @param oid The order ID
      */
     public static void deleteOrders(Object oid)
     {
         String del_query = "DELETE FROM PizzaOrder WHERE oid = '" + oid + "'";
         SQL_READER.insertUpdateCreateDelete(del_query);
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
             String sum_query = "create view  user_total as " +
             				   "select sum(p.price) as user_sum, u.userID " +
             				   "from Users u, PizzaOrder po, Pizza p " +
             				   "where u.userID = po.userID and po.pizzaType = p.PizzaType " +
             				   "group by u.userID";
             String max_query = "SELECT us.userId, us.user_sum from user_total us where us.user_sum = (select max(us2.user_sum) from user_total us2)";
             SQL_READER.insertUpdateCreateDelete(sum_query);
             ArrayList<LinkedList<String>> total_user_sum = ResultSetParser.parseResultSetIntoArray(SQL_READER.query(max_query), "userID;user_sum");
             return total_user_sum.get(0).get(0) + ";" + total_user_sum.get(1).get(0);
         }
         return null;
     }
 
     /**
      * our aggregate query
      * @param uid
      * @return
      */
     public static String getTotalSum(String uid)
     {
         String sum_query = "SELECT SUM(p.price) " +
                            "FROM Users u, PizzaOrder po, Pizza p " +
                            "WHERE u.userID = po.userID AND po.pizzaType = p.PizzaType AND u.userID = '" + uid + "' " +
                            "GROUP BY u.userID";
         ArrayList<LinkedList<String>> total_user_sum = ResultSetParser.parseResultSetIntoArray(SQL_READER.query(sum_query), "sum(p.price)");
         try
         {
             return total_user_sum.get(0).get(0);
         }
         catch(IndexOutOfBoundsException ex)
         {
             //The query didn't return any results, just return 0
             return "0";
         }
     }
 
     /**
      * change location of logged in User in User_isin table
      * @param uid
      * @param address
      */
     public static void updateLocation(String uid, String address)
     {
         SQL_READER.insertUpdateCreateDelete("UPDATE User_IsIn SET address = '" + address + "' WHERE userID = '" + uid + "'");
     }
 
     /**
      * Sets isCancellationRequested for cancellation request tracking purposes
      * @param oid The orderID
      * @param isCancellationRequested Whether the order should be marked as having a cancellation request
      */
     public static void updateCancellationOrder(Object oid, boolean isCancellationRequested)
     {
     	SQL_READER.insertUpdateCreateDelete("update pizzaorder set ISCANCELLATIONREQUESTED = " +
                                             (isCancellationRequested ? 1 : 0) +
                                             " where oid = '" + oid + "'");
     }
 
     /**
      * Sets isDelivered of order to 1 to notify order has been delivered
      * @param oid The orderID
      */
     public static void updateDelivered(Object oid)
     {
     	SQL_READER.insertUpdateCreateDelete("update pizzaorder set ISDELIVERED = 1 where oid = '" + oid + "'");
     }
 
     /**
      * Updates an attribute of a User tuple in the DB
      * @param userID The user ID
      * @param attribute
      * @param value
      */
     public static void updateUserAttribute(String userID, String attribute, String value)
     {
        String setStatement = "UPDATE Users SET " + attribute + " = '" + value + "' WHERE userID = '" + userID + "'";
         SQL_READER.insertUpdateCreateDelete(setStatement);
     }
 
     /**
      * Adds a new User_IsIn tuple to the DB
      * @param address The address of the user
      * @param userID The user ID
      */
     private static void addUserIsIn(String address, String userID)
     {
         String insertStatement = "INSERT INTO User_IsIn VALUES ( '" +
                                  address + "', '" +
                                  userID + "', 1)";
         SQL_READER.insertUpdateCreateDelete(insertStatement);
     }
 
     /**
      * Adds a new User tuple to the DB
      * @param user The representation of the user to add to the DB
      * @param password The password of the user
      * @param address The address of the user
      * @return Whether or not the add succeeded
      */
     public static boolean addUser(User user, String password, String address)
     {
         String passwordHash = hashPassword(password);
         if(passwordHash != null)
         {
             addUserIsIn(address, user.getUserID()); //Add User_IsIn first to satisfy integrity constraint
 
             String insertStatement = "INSERT INTO Users VALUES ( '" +
                                      user.getUserID() + "', '" +
                                      user.getFirstName() + "', '" +
                                      user.getLastName() + "', '" +
                                      user.getCreditCardNumber() + "', '" +
                                      passwordHash +
                                      "')";
             SQL_READER.insertUpdateCreateDelete(insertStatement);
             return true;
         }
 
         return false;
     }
 
     /**
      * Delete user should delete all entries with same userID in PizzaOrder and User_isin tables
      * our cascade delete
      * @param uid
      * @param admin
      */
     public static void removeUser(String uid, boolean admin)
     {
     	if(admin)
     	{
     		SQL_READER.insertUpdateCreateDelete("delete from user_isin where userID = '" + uid + "'");
     	}
     }
 
     private UserUtils() {}
 }
