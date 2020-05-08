 package edu.cs408.vormund;
 
 import java.io.BufferedReader;
 import java.io.ByteArrayInputStream;
 import java.io.File;
 import java.io.InputStreamReader;
 import java.io.IOException;
 import java.sql.*;
 
 public class Database {
   private static final String SCHEMA_FILE = "/edu/cs408/vormund/SCHEMA.sql";
   //private static final String DATABASE_FILE = ":resource:/edu/cs408/vormund/lib-common2.3.2.jar";
   private static final String DATABASE_FILE = "test.db";
 
   private Connection conn = null;
   private Statement stmnt = null;
   private PreparedStatement prpstmnt = null;
 
   /**
    * Class constructor.
    */
   public Database() {
     this.makeConnection();
     this.createStatement();
     if( !this.hasConnection() ) {
       System.err.println("Failed to create connection.");
       System.err.println("  Connection: " + this.conn);
       try{System.err.println("  IsClosed: " + this.conn.isClosed());}catch(Exception e){}
       System.err.println("  HasConn: " + this.hasConnection());
       try{System.err.println("  HasConn: " + (this.conn!=null && !this.conn.isClosed()));}catch(Exception e){}
       return;
     }
     if( !this.hasStatement() ) {
       System.err.println("Failed to create statement.");
       return;
     }
     try {
       ResultSet rslt = this.stmnt.executeQuery("select count(name) as count from sqlite_master where type='table'");
       if( rslt.next() ) {
         if( rslt.getInt("count") == 0 ) {
           this.setupNewDatabaseInstance();
         }
       }
     } catch(Exception e) {
       e.printStackTrace();
       this.conn = null;
     }
   }
 
   /**
    * Drops, then creates tables and inserts default data for Vormund.
    */
   private void setupNewDatabaseInstance() {
     BufferedReader br = new BufferedReader(new InputStreamReader(
         this.getClass().getResourceAsStream(SCHEMA_FILE)));
     String schema = " ";
     int c;
     boolean comment=false;
     try {
       while( (c=br.read()) != -1 ) {
         if( (char)c == '\n' ) continue;
         schema += (char)c;
         if( (char)c == ';' ) schema += '\n';
       }
       br.close();
     } catch(IOException e) {
       return;
     }
     for(String query : schema.split("\n")) {
       if( this.updateQuery(query) < 0 ) {
         System.out.println("Error in query: " + query);
       }
     }
   }
 
   /**
    * Returns if a connection exists and is open.
    * @return <code>true</code> if connection exists and is open,
    *         <code>false</code> otherwise.
    */
   public boolean hasConnection() {
     boolean ret = false;
     try { ret = this.conn!=null && !this.conn.isClosed(); }
     catch(SQLException e) {}
     return ret;
   }
 
   /**
    * Returns if a statement exists and is open.
    * @return <code>true</code> if statement exists and is open,
    *         <code>false</code> otherwise.
    */
   public boolean hasStatement() {
     boolean ret = false;
     ret = this.stmnt!=null;
     return ret;
   }
 
   public void makeConnection() {
     if( this.hasConnection() ) return;
     try {
       Class.forName("org.sqlite.JDBC");
       this.conn = DriverManager.getConnection("jdbc:sqlite:" + DATABASE_FILE);
    } catch(ClassNotFoundException e) {
      System.err.println("Error Making DB Connection: " + e.getMessage());
      this.conn = null;
    } catch(SQLException e) {
       System.err.println("Error Making DB Connection: " + e.getMessage());
       this.conn = null;
     }
   }
 
   /**
    * Creates an internal Statement object if one does not exist or is not open.
    */
   public void createStatement() {
     if( !this.hasConnection() || this.hasStatement() ) return;
     try {
       this.stmnt = this.conn.createStatement();
     } catch(SQLException e) {
       this.stmnt = null;
     }
   }
 
   /**
    * Runs a query that will update the database through the internal Statement
    * object. These stateents are normally <code>CREATE</code>,
    * <code>UPDATE</code>, <code>DELETE</code>, and <code>DROP</code>.
    *
    * @param query The query to alter the database.
    * @return      <code>-1</code> if a failure occurs.
    *              <code>\>=0</code> for the # of affected rows.
    */
   public int updateQuery(String query) {
     int ret=-1;
     if( !this.hasConnection() ) this.makeConnection();
     if( !this.hasStatement() ) this.createStatement();
     try {
       ret = this.stmnt.executeUpdate(query);
     } catch(SQLException e) {
       System.out.println("ERROR: " + e.getMessage());
       ret=-1;
     }
     return ret;
   }
 
   /**
    * Runs a query that will <code>INSERT</code> a row into the database.
    *
    * @param query The query to <code>INSERT</code> a row
    * @return      The id of the primary key from the row created
    */
   public int insertQuery(String query) {
     int ret=-1;
     if( !this.hasConnection() ) this.makeConnection();
     if( !this.hasStatement() ) this.createStatement();
     try {
       ret = this.stmnt.executeUpdate(query);
       ResultSet autoGen = this.stmnt.getGeneratedKeys();
       if(autoGen.next()==true) {
         ret = autoGen.getInt(1);
       } else {
         ret = -1;
       }
       autoGen.close();
     } catch(SQLFeatureNotSupportedException e) {
       System.err.println("Error: Get Generated Keys feature is not supported.");
     } catch(SQLException e) {
       System.out.println("MySQL Insert Error: " + e.getMessage());
       ret=-1;
     }
     return ret;
   }
 
   /**
    * Runs a query to pull information from the database.
    *
    * @param query The query to select information from the database.
    * @return      The {@link ResultSet} of results from the query.
    * @see ResultSet
    */
   public ResultSet query(String query) {
     ResultSet ret = null;
     if( !this.hasConnection() ) this.makeConnection();
     if( !this.hasStatement() ) this.createStatement();
     try {
       ret = this.stmnt.executeQuery(query);
     } catch(SQLException e) {
       System.err.println("Error Executing Query: " + e.getMessage());
       ret = null;
     }
     return ret;
   }
 
   /**
    * Closes the internal connection and statement objects.
    * @see Connection#close
    * @see Statement#close
    */
   public void close() {
     if( this.conn != null ) {
       try {
         this.conn.close();
       } catch(SQLException e) {
       } finally {
         this.conn = null;
       }
     }
     if( this.stmnt != null ) {
       try {
         this.stmnt.close();
       } catch(SQLException e) {
       } finally {
         this.stmnt = null;
       }
     }
   }
 
   /**
    * Inserts rows into the encrypted_data table.
    *
    * @param user_id ID of the user the data belongs to
    * @param category Category of the data being stored (ie. Website, Bank, etc.)
    * @param name Name that the data belongs to (ie. Bank of America, Facebook, etc.)
    * @param data Unencrypted data that will be stored in the database
    * @param encryption_key Key that will be used to encrypt the data
    * @return Number of rows affected. <code>-1</code> if the query was unsuccessful
    */
   public int insertBLOB(int user_id, String category, String name, String data, String encryption_key) {
     int ret = -1;
     byte enc_data[] = data.getBytes(); // Needs to run through encryption process
     //ByteArrayInputStream bais = new ByteArrayInputStream(enc_data);
     try {
       this.prpstmnt = this.conn.prepareStatement("INSERT INTO " +
           "encrypted_data(user_id, category, name, encrypted_data) " +
           "VALUES(?, ?, ?, ?)");
       this.prpstmnt.setInt(1, user_id);
       this.prpstmnt.setString(2, category);
       this.prpstmnt.setString(3, name);
       this.prpstmnt.setBytes(4, enc_data);
       //this.prpstmnt.setBinaryStream(5, bais, enc_data.length);
       ret = this.prpstmnt.executeUpdate();
       this.prpstmnt.close();
     } catch(SQLFeatureNotSupportedException e) {
       System.err.println("Error Function Not Supported: " + e.getMessage());
       ret = -1;
     } catch(SQLException e) {
       System.err.println("Error inserting BLOB to encrypted_data table:\n\t" + e.getMessage());
       ret = -1;
     } finally {
       this.prpstmnt = null;
       return ret;
     }
   }
 
   /**
    * Updatess rows into the encrypted_data table.
    *
    * @param data_id ID of the encrypted data being updated
    * @param new_name Name that the data belongs to (ie. Bank of America, Facebook, etc.)
    *             <code>NULL</code> if not being updated
    * @param new_data Unencrypted data that will be replace the old data in the database
    * @param encryption_key Key that will be used to encrypt the data
    * @return Number of rows affected. <code>-1</code> if the query was unsuccessful
    */
   public int updateBLOB(int data_id, String new_name, String new_data, String encryption_key) {
     int ret = -1;
     byte enc_data[] = new_data.getBytes(); // Needs to run through encryption process
     //ByteArrayInputStream bais = new ByteArrayInputStream(enc_data);
     try {
       if( new_name!=null ) {
         this.prpstmnt = this.conn.prepareStatement("UPDATE encrypted_data SET " +
             "encrypted_data=?, name=? WHERE data_id=?");
         this.prpstmnt.setString(2, new_name);
         this.prpstmnt.setInt(3, data_id);
       } else {
         this.prpstmnt = this.conn.prepareStatement("UPDATE encrypted_data SET " +
             "encrypted_data=? WHERE data_id=?");
         this.prpstmnt.setInt(2, data_id);
       }
       this.prpstmnt.setBytes(1, enc_data);
       //this.prpstmnt.setBinaryStream(5, bais, enc_data.length);
       ret = this.prpstmnt.executeUpdate();
       this.prpstmnt.close();
     } catch(SQLFeatureNotSupportedException e) {
       System.err.println("Error Function Not Supported: " + e.getMessage());
       ret = -1;
     } catch(SQLException e) {
       System.err.println("Error inserting BLOB to encrypted_data table:\n\t" + e.getMessage());
       ret = -1;
     } finally {
       this.prpstmnt = null;
       return ret;
     }
   }
 
   /**
    * Reads data from a BLOB in a {@link ResultSet} to a byte array.
    *
    * @param queryResult The {@link ResultSet} from a query
    * @param column number of the column with the BLOB
    * @return string of the data stored in the BLOB.
    * @see ResultSet
    */
   public String readFromBLOB(ResultSet queryResult, int column) throws SQLException {
     byte[] blob = queryResult.getBytes(column);
     // Decryption stuff
     return new String(blob);
   }
 
   /**
    * Reads data from a BLOB in a {@link ResultSet} to a byte array.
    *
    * @param queryResult The {@link ResultSet} from a query
    * @param column Name of the column of the BLOB
    * @return string of the data stored in the BLOB.
    * @see ResultSet
    */
   public String readFromBLOB(ResultSet queryResult, String column) throws SQLException {
     return readFromBLOB(queryResult, queryResult.findColumn(column));
   }
 
   public static void main(String[] args) throws SQLException {
     Database db = new Database();
     assert db.hasConnection();
     assert db.hasStatement();
     db.close();
     assert !db.hasConnection();
     assert !db.hasStatement();
 
     db.makeConnection();
     assert db.hasConnection();
 
     db.createStatement();
     assert db.hasStatement();
 
     //assert db.insertQuery("INSERT INTO data_type(type_name, type_value) VALUES('SSN', 'text')")==6;
     ResultSet result = null;//db.query("SELECT * FROM data_type WHERE type_name LIKE 'SSN'");
     /*assert result.next();
     assert result.getInt("type_id")==6;
     assert result.getString("type_name").compareTo("SSN")==0;
     assert result.getString("type_value").compareTo("text")==0;
     assert !result.next();
     result.close();*/
 
     //assert db.updateQuery("DELETE FROM data_type WHERE type_id=6")==1;
     result = db.query("SELECT * FROM data_type WHERE type_name LIKE 'SSN'");
     assert !result.next();
     result.close();
 
     assert db.insertQuery("INSERT INTO user_data(user_name, password, name) VALUES('test_user', 'test', 'Test McTester')") == 1;
     assert db.insertBLOB(1, "Facebook", "Facebook Username", "Hello World", "test_pass") == 1;
     result = db.query("SELECT * FROM encrypted_data");
     assert result.next();
     assert result.getInt("data_id")==1;
     assert result.getInt("user_id")==1;
     assert result.getString("category").compareTo("Facebook") == 0;
     //assert result.getInt("type_id")==1;
     assert result.getString("note").compareTo("Facebook Username") == 0;
     String test_blob = db.readFromBLOB(result, "encrypted_data");
     assert (test_blob).compareTo("Hello World") == 0;
     assert !result.next();
     result.close();
 
     /*result = db.query("SELECT user_data.user_id AS user_id, user_data.user_name AS user_name, " +
         "encrypted_data.data_id AS data_id, encrypted_data.category AS category, " +
         "encrypted_data.type_id AS type_id, data_type.type_name AS type_name, " +
         "encrypted_data.encrypted_data AS encrypted_data FROM user_data INNER JOIN encrypted_data ON " +
         "user_data.user_id=encrypted_data.user_id INNER JOIN data_type ON " +
         "encrypted_data.type_id=data_type.type_id");
     assert result.next();
     assert result.getInt("user_id") == 1;
     assert result.getString("user_name").compareTo("test_user") == 0;
     assert result.getInt("data_id") == 1;
     assert result.getString("category").compareTo("Facebook") == 0;
     assert result.getInt("type_id") == 1;
     assert result.getString("type_name").compareTo("Username") == 0;
     test_blob = db.readFromBLOB(result, "encrypted_data");
     assert (new String(test_blob)).compareTo("Hello World") == 0;
     result.close();*/
     File f = new File(DATABASE_FILE);
     assert f.delete();
   }
 }
