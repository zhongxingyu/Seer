 package csheets.ext.database.core;
 
 import java.sql.*;
 import java.util.logging.*;
 
 import javax.swing.JOptionPane;
 
 import csheets.core.Cell;
 import java.util.ArrayList;
 
 public class DerbyConnection implements DBConnectionStrategy {
 
 	private final String driverPath = "org.apache.derby.jdbc.EmbeddedDriver";
 	private Connection connection;
 	private String url, user, pwd;
 
 	@Override
 	public void createConnection(String url, String user, String pass)
 			throws ClassNotFoundException, SQLException {
 		try {
 			Class.forName(driverPath);
 			connection = DriverManager.getConnection(url + ";create=true",
 					user, pass);
 		} catch (ClassNotFoundException e) {
 			JOptionPane.showMessageDialog(null, "Error: class not found!");
 		} catch (SQLException e) {
 			JOptionPane.showMessageDialog(null,
 					"Error: connection to database!");
 			e.printStackTrace();
 		}
 	}
 
 	@Override
 	public void createTable(String tableName, Cell[][] cells) {
 		/* defining row and column number */
 		int numberOfColumns = cells[0].length;
 		int numberOfRows = cells.length;
 
 		/* beginning the construction of the sql statement */
 		String stat = "CREATE TABLE "
 				+ tableName
 				+ "(id INTEGER generated always as identity constraint pkId primary key, ";
 
 		/* the first line of the exported cells is the name of each column */
 		String[] columnsName = new String[cells[0].length];
 		String[] columnsNameCopy = new String[cells[0].length];
 
 		/* cycle to build the columns name string */
 		for (int i = 0; i < cells[0].length; i++) {
 			/* if it's not the last column */
 			if (i != ((cells[0].length) - 1)) {
 				columnsName[i] = cells[0][i].getContent() + " VARCHAR(200), ";
 				columnsNameCopy[i] = cells[0][i].getContent();
 			}
 			/* if it's the last column must close with bracket */
 			else {
 				columnsName[i] = cells[0][i].getContent() + " VARCHAR(200))";
 				columnsNameCopy[i] = cells[0][i].getContent();
 			}
 		}
 
 		/*
 		 * in the end of this cycle we should have the final sql statement for
 		 * table creation
 		 */
 		for (int i = 0; i < columnsName.length; i++) {
 			stat += columnsName[i];
 		}
 
 		/* sql statement */
 		Statement st = null;
 
 		/* creates the table based on the sql statement */
 		try {
 			st = connection.createStatement();
 			int i = st.executeUpdate(stat);
 			/* keep this until observer is implemented */
 			JOptionPane.showMessageDialog(null, "Data succesfully exported!");
 		} catch (SQLException ex) {
 			Logger.getLogger(HsqlDBConnection.class.getName()).log(
 					Level.SEVERE, null, ex);
 			/* keep this until observer is implemented */
 			JOptionPane.showMessageDialog(null, "Error: table already exists");
 		}
 
 		/* now beggins the "insert into" sql statement */
 		String insertStat = "insert into " + tableName + "(";
 		for (int i = 0; i < columnsNameCopy.length; i++) {
 			if (i != (columnsNameCopy.length - 1)) {
 				insertStat += columnsNameCopy[i] + ",";
 			} else {
 				insertStat += columnsNameCopy[i] + ")";
 			}
 		}
 		/* continuing concatenation */
 		insertStat += " VALUES(";
 
 		/* creating a number of insert statements equal to number of rows -1 */
 		String[] insertVector = new String[numberOfRows - 1];
 		for (int i = 0; i < insertVector.length; i++) {
 			insertVector[i] = insertStat;
 		}
 
 		/* concatenating the respecting insert statements */
 		for (int i = 1; i < numberOfRows; i++) {
 			for (int j = 0; j < numberOfColumns; j++) {
 				if (j != (numberOfColumns - 1)) {
 					insertVector[i - 1] += "'" + cells[i][j].getContent()
 							+ "',";
 				} else {
 					insertVector[i - 1] += "'" + cells[i][j].getContent()
 							+ "')";
 					// break;
 				}
 			}
 		}
 
 		/* inserting values into the table */
 		for (int i = 0; i < insertVector.length; i++) {
 			Statement insertSt = null;
 			try {
 				insertSt = connection.createStatement();
 				int j = st.executeUpdate(insertVector[i].toString());
 			} catch (SQLException ex) {
 				Logger.getLogger(HsqlDBConnection.class.getName()).log(
 						Level.SEVERE, null, ex);
 				/* keep this until observer is implemented */
 				JOptionPane.showMessageDialog(null, "Error: data not inserted");
 			}
 		}
 	}
 
 	@Override
 	public void disconnet() {
 		try {
 			connection.close();
 		} catch (SQLException ex) {
 			Logger.getLogger(HsqlDBConnection.class.getName()).log(
 					Level.SEVERE, null, ex);
 		}
 	}
         
         @Override
        public ArrayList queryToArray() 
         {
            return null;
         }
 
 	@Override
 	public String[] getTableList(ArrayList list) {
 		// TODO Auto-generated method stub
 		throw new UnsupportedOperationException("Not supported yet.");
 	}
 
 	@Override
 	public void getTableContent() {
 		// TODO Auto-generated method stub
 		throw new UnsupportedOperationException("Not supported yet.");
 	}
 
 	@Override
 	public void updateTable() {
 		// TODO Auto-generated method stub
 		throw new UnsupportedOperationException("Not supported yet.");
 	}
 
 }
