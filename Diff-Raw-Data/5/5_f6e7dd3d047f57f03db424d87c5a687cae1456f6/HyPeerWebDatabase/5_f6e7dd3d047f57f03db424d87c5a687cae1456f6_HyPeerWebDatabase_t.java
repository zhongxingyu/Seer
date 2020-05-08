 package hypeerweb;
 
 import java.sql.*;
 import java.util.*;
 
 /**
  * The helper class that allows saving and loading the HyPeerWeb from the database.
  * <br>
  * <br>
  * <pre>
  * <b>Domain:</b>
  *      DATABASE_DIRECTORY     : String
  *      DEFAULT_DATABASE_NAME : String
  *      singleton : HyPeerWebDatabase
  *      connection : Connection
  * 
  * </pre>
  * 
  * @author Konrad Rywelski
  */
 
 public class HyPeerWebDatabase
 {
 
     /**
      * The name of the directory that holds databases associated with HyPeerWebs. Currently it is "db". Of course the directory location is relative to where the HyPeerWeb program starts. 
      */    
     public static String DATABASE_DIRECTORY;
     
     /**
      * The default name of the file that holds the sqlite3 database. Currently it is "HyPeerWeb.db".
      */
     public static String DEFAULT_DATABASE_NAME = "HyPeerWeb.db";
     
     
     
     /**
      * the single HyPeerWebDatabase
      */
     private static HyPeerWebDatabase singleton = null;
     
     /**
      * the connection necessary to interact with the databases 
      */
     private static Connection connection;
 
 
     /**
      * The standard constructor.  It is private and should only be used by getSingleton() method
      * when the singleton is null;
      * 
      * @param dbName  The name of the database.
      * @pre <i>singleton = null</i>
      *      <i>dbName = null OR |dbName| = 0 OR There must exist a database with the given dbName.</i>
      * @post initHyPeerWebDatabase(dbName).postCondition
      */
     private HyPeerWebDatabase(final String dbName)
     {
         initHyPeerWebDatabase(dbName);
     }
 
     /**
      * Gets the single HyPeerWebDatabase.
      * 
      * @pre singleton=null OR initHyPeerWebDatabase() have been called previously
      * @post return=singleton
      * @return the single HyPeerWebDatabase
      */
     public static HyPeerWebDatabase getSingleton()
     {
         if (singleton == null) singleton = new HyPeerWebDatabase(DEFAULT_DATABASE_NAME);
         return singleton;
     }
 
     /**
      * Creates and loads a HyPeerWebDatabase. Should be one of the first things called when creating a HyPeerWeb. 
      * 
      * @pre There must exist a database with the name given by DEFAULT_DATABASE_NAME.
      * @post <i>The connection is set</i>
      *       <i>there are 3 tables in DEFAULT_DATABASE_NAME database</i>
      */
     public static void initHyPeerWebDatabase()
     {
         initHyPeerWebDatabase(DEFAULT_DATABASE_NAME);
     }
     
     /**
      * Creates and loads a HyPeerWebDatabase. Should be one of the first things called when creating a HyPeerWeb. 
      * 
      * @param dbName The name of the database
      * @pre dbName = null OR |dbName| = 0 OR There must exist a database with the given dbName.
      * @post <i>The connection is set</i>
      *       <i>there are 3 tables in dbName database</i>
      */
     public static void initHyPeerWebDatabase(java.lang.String dbName)
     {
         if (connection != null)
         {    
             try 
             {
                 connection.close();
             } 
             catch (final SQLException e)
             {
                 e.printStackTrace();
             }
         }    
         
         if (dbName==null) dbName=DEFAULT_DATABASE_NAME;
         
         try 
         {
             Class.forName("org.sqlite.JDBC");
             connection = DriverManager.getConnection("jdbc:sqlite:"+dbName);
         } 
         catch (final SQLException e) 
         {
             e.printStackTrace();
         }
         catch (final ClassNotFoundException e)
         {
             System.out.println("JDBC cannot be located.");
             e.printStackTrace();
         }
         
         createTables();
 
     }
     
     
     /**
      * Saves the HyPeerWeeb to the current database
      * 
      * @param nodes collection of nodes that belong to the HyPeerWeb that is to be saved
      * @pre the tables of the current database are already prefilled OR they are empty
      * @post <i>there is an entry in the Nodes table for every Node in the nodes collection</i>
      *      <i>for every node pairs of neighbors and surrogate neighbors are saved in the database </i>
      */
     public void save(final Collection<Node> nodes)
     {
             
             dropTables();
             createTables();
         
             for (Node node : nodes)
             {
                     saveNode(node);
                     saveNeighbors(node);
                     saveSurNeighbors(node);
             }
             
     }
     /**
      * @pre tables already exist in the current database OR they don't exist
      * @post there are 3 tables (Nodes, Neighbors, SurNeighbors) in the current database
      */
     private static void createTables()
     {
         try
         {
             final Statement createTables = connection.createStatement();
             createTables.addBatch(
                             "CREATE TABLE IF NOT EXISTS Nodes (" +
                             "WebId int primary key not null unique," +
                             "Height int not null," +
                             "Fold int," +
                             "SurFold int," +
                             "InvSurFold int)"
             );
             createTables.addBatch(
                     "CREATE TABLE IF NOT EXISTS Neighbors (" +
                     "Id int primary key asc," +
                     "Node int not null," +
                     "Neighbor int not null)"
             );
             createTables.addBatch(
                             "CREATE TABLE IF NOT EXISTS SurNeighbors (" +
                             "Id int primary key asc," +
                             "InvSurNeighbor int not null," +
                             "SurNeighbor int not null)"
             );
             
             createTables.executeBatch();
             createTables.close();
         }
         catch(final SQLException e)
         {
             System.out.println("There was an error creating the database.");
             e.printStackTrace();
         }
     }
 
     /**
      * Clears the current database.
      * 
      * @pre tables already exist in the current database OR they don't exist
      * @post there are no tables in the current database
      */
     public void clear()
     {
         dropTables();
     }
     
     /**
      * Drops all 3 tables. Used to clear the current database
      * 
      * @pre tables already exist in the current database OR they don't exist
      * @post there are no tables in the current database
      */
     private void dropTables()
     {
         try
         {
             final Statement dropTables = connection.createStatement();
             try
             {
                 
                 dropTables.executeUpdate("DROP TABLE IF EXISTS Nodes");
                 dropTables.executeUpdate("DROP TABLE IF EXISTS SurNeighbors");
                 dropTables.executeUpdate("DROP TABLE IF EXISTS Neighbors");
                 dropTables.close();
             }
             catch(final SQLException e)
             {
                 System.out.println("There was an error clearing the database.");
                 e.printStackTrace();
             }
         }
         catch(final SQLException e)
         {
             System.out.println("Could not connect to the database.");
             e.printStackTrace();
         }
     }
     
     /**
      * Loads the HyPeerWeb from the current database.
      * 
      * @pre singleton != NULL
      * @post the return HashMap contains all the information of the HyPeerWeb that is to be loaded
      * @return HashMap<Integer,Node> set of nodes that belong to the loaded database
      */
     public HashMap<Integer,Node> loadNodeSet()
     {
         try
         {
             final HashMap<Integer,Node> nodes = new HashMap<Integer,Node>();
             
             //load the nodes
             final ResultSet rs = connection.createStatement().executeQuery("select WebId from Nodes;");
             while (rs.next())
             {
                 final int webId = rs.getInt("WebId");
                 final Node newNode = new Node(webId);
                 nodes.put(webId, newNode);                               
             }
                     
             rs.close();
             final Set<Integer> keySet = nodes.keySet();
             Node currNode;
             for(int id: keySet)
             {    
                 currNode = nodes.get(id);
                 final PreparedStatement nodeStat = connection.prepareStatement("SELECT * FROM Nodes WHERE WebId = ?");
                 nodeStat.setInt(1, id);
                 final ResultSet nodeSet = nodeStat.executeQuery();
                //nodeStat.close();
                 
                 if(nodeSet.next())
                 {
                     if(nodeSet.getInt("Fold")>=0) currNode.setFold(nodes.get(nodeSet.getInt("Fold")));
                     if(nodeSet.getInt("InvSurFold")>=0) currNode.setInverseSurrogateFold(nodes.get(nodeSet.getInt("InvSurFold")));
                     if(nodeSet.getInt("SurFold")>=0) currNode.setSurrogateFold(nodes.get(nodeSet.getInt("SurFold")));
                 }
                nodeStat.close();
                 final HashSet<Integer> neighbors = loadNeighbors(id);
                 for(int neighborId: neighbors) currNode.addNeighbor(nodes.get(neighborId));
                 
                 final HashSet<Integer> surNeighbors = loadSurNeighbors(id);
                 for(int neighborId: surNeighbors) currNode.addDownPointer(nodes.get(neighborId));
                 
                 final HashSet<Integer> invSurNeighbors = loadInvSurNeighbors(id);
                 for(int neighborId: invSurNeighbors) currNode.addUpPointer(nodes.get(neighborId));
             }
             return nodes;
         }
         catch(final SQLException e)
         {
             System.out.println("There was an error loading the nodes from the database.");
             e.printStackTrace();
         }
         return new HashMap<Integer,Node>();
     }
 
     /**
      * Creates a SimplifiedNodeDomain representing the node with indicated webId. The information is retrieved from the database. 
      * 
      * @param webId - The webId of the node whose information we are going to retrieve.
      * @pre There exists a node in the database with the given webId.
      * @post result contains the webId, neighbors, upPointers, downPointers, fold, surrogateFold, and inverse surrogate fold of the indicated node.
      * @return
      */
     public SimplifiedNodeDomain getNode(int webId) throws SQLException
     {    
         final PreparedStatement getNode = connection.prepareStatement("SELECT * FROM Nodes WHERE WebId = ?");
         getNode.setInt(1, webId);
         final ResultSet nodeSet = getNode.executeQuery();
         SimplifiedNodeDomain nodeToReturn = null;
         
         if (nodeSet.next())
         {
             webId = nodeSet.getInt("WebId");
             final int height = nodeSet.getInt("Height");           
             final int fold = nodeSet.getInt("Fold");
             final int surFold = nodeSet.getInt("SurFold");
             final int invSurFold = nodeSet.getInt("InvSurFold");
 
             final HashSet<Integer> neighbors = loadNeighbors(webId);
             final HashSet<Integer> surNeighbors = loadSurNeighbors(webId);
             final HashSet<Integer> invSurNeighbors = loadInvSurNeighbors(webId);
             
             nodeToReturn = new SimplifiedNodeDomain(webId, height, neighbors, 
                     invSurNeighbors, surNeighbors, fold, surFold, invSurFold);
 
         }
         getNode.close(); // also closes the ResultSet
         return nodeToReturn;
 
     }
     
     /**
      * Loads neighbors of the given node.
      * 
      * @param webId - The webId of the node whose information we are going to retrieve.
      * @pre NONE
      * @post HashSet of neighbors contains the webIds (loaded from the database) of the given node's neighbors
      * @return HashSet of neighbors
      */
     private HashSet<Integer> loadNeighbors(final int webId)
     {
         try
         {
             final PreparedStatement loadNeighbors = connection.prepareStatement("SELECT Neighbor FROM Neighbors WHERE Node = ?");
             loadNeighbors.setInt(1, webId);
             final ResultSet neighborsSet = loadNeighbors.executeQuery();
             final HashSet<Integer> neighbors = new HashSet<Integer>();
            
             while (neighborsSet.next())
                     neighbors.add(neighborsSet.getInt("Neighbor"));
            
             loadNeighbors.close();
             return neighbors;
         }
         catch(final SQLException e)
         {
             System.out.println("There was an error loading the surrogate neighbors of node "
                                             + webId + " to the database.");
             e.printStackTrace();
         }
         return new HashSet<Integer>();
     }
     
     /**
      * Loads surrogate neighbors of the given node.
      * 
      * @param webId - The webId of the node whose information we are going to retrieve.
      * @pre NONE
      * @post HashSet of neighbors contains the webIds (loaded from the database) of the given node's surrogate neighbors
      * @return HashSet of surrogate neighbors
      */
     private HashSet<Integer> loadSurNeighbors(final int webId)
     {
         try
         {
             final PreparedStatement loadSurNeighbors = connection.prepareStatement("SELECT SurNeighbor FROM SurNeighbors WHERE InvSurNeighbor = ?");
             loadSurNeighbors.setInt(1, webId);
             final ResultSet surNeighborsSet = loadSurNeighbors.executeQuery();
             final HashSet<Integer> surNeighbors = new HashSet<Integer>();
            
             while (surNeighborsSet.next())
                     surNeighbors.add(surNeighborsSet.getInt("SurNeighbor"));
            
             loadSurNeighbors.close();
             return surNeighbors;
         }
         catch(final SQLException e)
         {
             System.out.println("There was an error loading the surrogate neighbors of node "
                                             + webId + " to the database.");
         e.printStackTrace();
         }
         return new HashSet<Integer>();
     }
     
     /**
      * Loads inverse surrogate neighbors of the given node.
      * 
      * @param webId - The webId of the node whose information we are going to retrieve.
      * @pre NONE
      * @post HashSet of neighbors contains the webIds (loaded from the database) of the given node's inverse surrogate neighbors
      * @return HashSet of inverse surrogate neighbors
      */
     private HashSet<Integer> loadInvSurNeighbors(final int webId)
     {
         try
         {
             final PreparedStatement loadInvSurNeighbors = connection.prepareStatement("SELECT InvSurNeighbor FROM SurNeighbors WHERE SurNeighbor = ?");
             loadInvSurNeighbors.setInt(1, webId);
             final ResultSet invSurNeighborsSet = loadInvSurNeighbors.executeQuery();
             final HashSet<Integer> invSurNeighbors = new HashSet<Integer>();
            
             while(invSurNeighborsSet.next()) invSurNeighbors.add(invSurNeighborsSet.getInt("InvSurNeighbor"));
            
             loadInvSurNeighbors.close();
             return invSurNeighbors;
         }
         catch(final SQLException e)
         {
             System.out.println("There was an error loading the inverse surrogate neighbors of node "
                                         + webId + " to the database.");
             e.printStackTrace();
         }
         return new HashSet<Integer>();
     }
 
     /**
      * Saves node's information to the Nodes table
      * 
      * @param node - node whose information we are going to save to the current database
      * @pre the node is not in the database yet
      * @post <i>node's information is saved in the database</i>
      *      <i>all the node entries that wre in the database before are still there</i>
      * @throws SQLException
      * @param node
      */
     private void saveNode(final Node node)
     {
         try
         {
             final PreparedStatement saveNode = connection.prepareStatement("INSERT INTO Nodes VALUES (?, ?, ?, ?, ?)");
            
             
             saveNode.setInt(1, node.getWebId()); //change when node class is ready
             saveNode.setInt(2, node.getHeight());
             saveNode.setInt(3, node.getFoldId());
             saveNode.setInt(4, node.getSurrogateFoldId());
             saveNode.setInt(5, node.getInverseSurrogateFoldId());
             saveNode.addBatch();
             
             connection.setAutoCommit(false);
             while(true)
             {
                 try
                 { 
                     saveNode.executeBatch();
                     break;
                 }
                 catch(final BatchUpdateException e)
                 {
                     continue;
                 }
             }
             connection.setAutoCommit(true);
             saveNode.close();
         }
         catch(final SQLException e)
         {
             System.out.println("There was an error saving the node "
                                 + node.getWebId() + " to the database.");
             e.printStackTrace();
         }
     }
     
     /**
      * Saves node's neighbors' ids to the Neighbors table
      * 
      * @param node - node whose information we are going to save to the current database
      * @pre the node's neighbors ids are not stored in the table yet
      * @post <i>node's neighbors'ids are saved in the database</i>
      *      <i>all the entries that were in the database before are still there</i>
      * @throws SQLException
      * @param node
      */
     private void saveNeighbors(final Node node)
     {
         try
         {
             final PreparedStatement saveNeighbors = connection.prepareStatement("INSERT INTO Neighbors(Node, Neighbor) VALUES (?, ?)");
             final int webId = node.getWebId();
             
             for (int neighborId : node.getNeighborsIds())
             {
                 saveNeighbors.setInt(1, webId);
                 saveNeighbors.setInt(2, neighborId);
                 saveNeighbors.addBatch();
             }
             connection.setAutoCommit(false);
             while(true)
             {
                 try
                 { 
                     saveNeighbors.executeBatch();
                     break;
                 }
                 catch(final BatchUpdateException e)
                 {
                     continue;
                 }
             }
             connection.setAutoCommit(true);
             saveNeighbors.close();
         }
         catch(final SQLException e)
         {
             System.out.println("There was an error saving a neighbor relationship of node "
                                         + node.getWebId() + " to the database.");
             e.printStackTrace();
         }
     }
     /**
      * Saves node's surrogate neighbors' ids to the Neighbors table
      * 
      * @param node - node whose information we are going to save to the current database
      * @pre the node's surrogate neighbors ids are not stored in the table yet
      * @post <i>node's surrogate neighbors'ids are saved in the database</i>
      *      <i>all the entries that were in the database before are still there</i>
      * @throws SQLException
      * @param node
      */
     private void saveSurNeighbors(final Node node)
     {
         try
         {
             final PreparedStatement saveSurNeighbors = connection.prepareStatement(
                     "INSERT INTO SurNeighbors(InvSurNeighbor, SurNeighbor) VALUES (?, ?)");
           //change when node class is ready
             final int webId = node.getWebId();
             
             for (int surNeighborId : node.getSurNeighborsIds())
             {
                 saveSurNeighbors.setInt(1, webId);
                 saveSurNeighbors.setInt(2, surNeighborId);
                 saveSurNeighbors.addBatch();
             }
             connection.setAutoCommit(false);
             while(true)
             {
                 try
                 { 
                     saveSurNeighbors.executeBatch();
                     break;
                 }
                 catch(final BatchUpdateException e)
                 {
                     continue;
                 }
             }
             connection.setAutoCommit(true);
             saveSurNeighbors.close(); 
         }
         catch(final SQLException e)
         {
             System.out.println("There was an error saving a surrogate neighbor relationship of node " 
                                                 + node.getWebId() + " to the database.");
             e.printStackTrace();
         }  
     }
 }
