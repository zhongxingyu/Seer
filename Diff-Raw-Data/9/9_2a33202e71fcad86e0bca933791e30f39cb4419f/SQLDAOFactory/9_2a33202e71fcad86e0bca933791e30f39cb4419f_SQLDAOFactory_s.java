 /**
  * 
  */
 package model.storage;
 
 import model.storage.SQLDAOs.SQLItemDAO;
 import model.storage.SQLDAOs.SQLMiscStorageDAO;
 import model.storage.SQLDAOs.SQLProductDAO;
 import model.storage.SQLDAOs.SQLProductGroupDAO;
 import model.storage.SQLDAOs.SQLStorageUnitDAO;
 
 import common.Result;
 import java.sql.*;
 import java.util.ArrayList;
 
 /**
  * Concrete factory for creating DAOs that use SQL for storage of models.
  */
 public class SQLDAOFactory implements IDAOFactory {
 	
 	private static Connection _connection;
 	private static Connection _autoConnection;
 	
 	@Override
 	public IStorageDAO getItemDAO() {
 		return new SQLItemDAO();
 	}
 
 	@Override
 	public IStorageDAO getProductDAO() {
 		return new SQLProductDAO();
 	}
 
 	@Override
 	public IStorageDAO getProductGroupDAO() {
 		return new SQLProductGroupDAO();
 	}
 
 	@Override
 	public IStorageDAO getStorageUnitDAO() {
 		return new SQLStorageUnitDAO();
 	}
 
 	public IStorageDAO getMiscStorageDAO() {
 		return new SQLMiscStorageDAO();
 	}
 	
 	
 	@Override
 	public Result startTransaction() {
 		_connection = openNewConnection();
 		if (_connection == null) return new Result(false, "There was a problem opening the connection.");
 		try {
 			_connection.setAutoCommit(false);
 		} catch (SQLException e) {
 			return new Result(false, e.getMessage());
 		}
 		return new Result(true);
 	}
 
 	private Connection openNewConnection() {
 		String connectionURL = "jdbc:sqlite:grp5db.sqlite";
 		try {
 			return DriverManager.getConnection(connectionURL);
 		} catch (SQLException e) {
             assert e.getMessage() == null;
 			return null;
 		}
 	}
 
 	@Override
 	public Result endTransaction(boolean commit) {
 		Result result = new Result();
 		try {
 			if(commit){
 				_connection.commit();
 			}else{
 				_connection.rollback();
 			}
 		} catch (SQLException e) {
 			result.setMessage(e.getMessage());
 			result.setStatus(false);
 		} finally {
 			result = closeConnection();
 		}
 		return result;
 	}
 
 	private Result closeConnection(){
 		if(_connection != null){
 			try {
 				_connection.close();
 				_connection = null;
 			} catch (SQLException e) {
 				return new Result(false, e.getMessage());
 			}
 		}
 		return new Result(true);
 	}
 
 	@Override
 	public Connection getConnection() {
 		if (_connection == null) {
 			return _autoConnection;
 		}else {
 			return _connection;
 		}
 	}
 
 	@Override
 	public Result initializeConnection() {
 		Result r = new Result();
 		try {
 			String driver = "org.sqlite.JDBC";
 			Class.forName(driver);
 			r.setStatus(true);
 		} catch (ClassNotFoundException e) {
 			r = new Result(false, e.getMessage());
 		}
 		_autoConnection = openNewConnection();
         this.generateTables();
 		return r;
 	}
 
     private void generateTables() {
         Connection c = this.getConnection();
         ArrayList<String> cmds = new ArrayList<String>();
         cmds.add("CREATE TABLE IF NOT EXISTS \"item\" (\"id\" INTEGER, \"productId\" INTEGER, " +
                 "\"barcode\" TEXT,\"entryTime\" LONG,\"exitTime\" LONG, \"deleted\" BOOL);");
         cmds.add("CREATE TABLE IF NOT EXISTS \"productGroup\" (\"id\" INTEGER, \"name\" TEXT, " +
                 "\"rootParentId\" INTEGER,  \"parentId\" INTEGER, \"MonthSupplyAmount\" FLOAT, " +
                "\"MonthSupplyUnit\" TEXT);");
         cmds.add("CREATE TABLE IF NOT EXISTS\"storageUnit\" (\"id\" INTEGER, \"name\" TEXT, " +
                 "\"rootParentId\" INTEGER, \"deleted\" BOOL);");
         cmds.add("CREATE TABLE IF NOT EXISTS\"product\" (\"id\" INTEGER , " +
                 "\"storageUnitId\" INTEGER, \"parentId\" INTEGER,  \"barcode\" TEXT, " +
                 "\"MonthSupply\" INTEGER, \"sizeAmount\" FLOAT, \"sizeUnit\" TEXT, " +
                 "\"deleted\" BOOL, \"description\" TEXT, \"shelfLife\" INTEGER, " +
                 "\"creationDate\" LONG);");
 		cmds.add("CREATE TABLE IF NOT EXISTS \"miscStorage\" (\"id\" INTEGER,  \"datetime\" LONG);");
         try {
             for (String cmd : cmds){
                 PreparedStatement p = c.prepareStatement(cmd);
                 p.executeUpdate();
             }
         } catch (SQLException e) {
             e.printStackTrace();
         }
     }
 }
