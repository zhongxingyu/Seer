 import java.sql.Date;
 import java.sql.ResultSet;
 import java.sql.ResultSetMetaData;
 import java.sql.SQLException;
 import java.sql.Time;
 import java.sql.Types;
 import java.text.DateFormat;
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.Calendar;
 
 
 
 public class ReturnOperations extends AbstractTableOperations {
 	/*
 	 * Insert a new tuple in Return table. Return date will uses sysdate 
 	 * specifying the current date.
 	 */
 	boolean insert(String receiptId){
 		
 		Integer r = Integer.parseInt(receiptId);
 		String test1 = r.toString();
 		
 		System.out.println(test1);
 		
 		
 		try {
 			ps = con.prepareStatement("INSERT INTO return VALUES (return_retid.nextval, ? , sysdate)");
 			
 			ps.setInt(1, r);
 //			if(rdate!=null){
 //				ps.setDate(2,rdate);
 //			}
 //			else{
 //				ps.setDate(2, null);
 //			}
 			System.out.println("Executing Query to insert into return");
 			ps.execute();
 			System.out.println("Query to insert Executed");
 			
 			con.commit();
 			
 			return true;
 		} 
 		catch (SQLException ex) {
 			ExceptionEvent event = new ExceptionEvent(this, ex.getMessage());
 			fireExceptionGenerated(event);
 			System.out.println(ex.getMessage());
 			try
 			{
 				con.rollback();
 				return false; 
 			}
 			catch (SQLException ex2)
 			{
 				event = new ExceptionEvent(this, ex2.getMessage());
 				fireExceptionGenerated(event);
 				System.out.println(ex2.getMessage());
 				return false; 
 			}
 		}
 
 
 	}
 	/*
 	 * Deletes a tuple from return table by specifying the retid
 	 */
 	Boolean delete(String retid){
 		try {
 			ps = con.prepareStatement("DELETE FROM return WHERE retid = ?");
 			ps.setString(1, retid);
 			ps.executeUpdate();
 			con.commit();
 			return true;
 		}
 		catch (SQLException ex) {
 			ExceptionEvent event = new ExceptionEvent(this, ex.getMessage());
 			fireExceptionGenerated(event);
 
 			try {
 				con.rollback();
 				return false;
 			}
 			catch (SQLException ex2) {
 				event = new ExceptionEvent(this, ex2.getMessage());
 				fireExceptionGenerated(event);
 				return false;
 			}
 		}
 	}
 	
 	/*
 	 * Process a return for an item.
 	 */
 	public boolean returnItem(String receiptId, String upc) {
 		ResultSet rs;
 		Integer r = Integer.parseInt(receiptId);
 		Integer u = Integer.parseInt(upc);
 
 
 		try {
 			// Checks if purchase has been made within 15 days 
 			// and if the item has already been returned using (sysdate - 15) 
 			ps = con.prepareStatement("SELECT DISTINCT(UPC), RECEIPTID , QUANTITY FROM (SELECT p.pdate, pi.receiptid, pi.upc, pi.quantity FROM purchase p, purchaseitem pi WHERE p.receiptid = pi.receiptid AND p.receiptId = ? AND pi.upc = ? AND pdate >= (sysdate - 15) MINUS select p.pdate, pi.receiptid, pi.upc, pi.quantity from purchase p, purchaseitem pi, return r, returnitem ri where ri.retid = r.retid and r.receiptid = p.receiptid and p.receiptid = pi.receiptid and r.rdate >= (sysdate - 15))");
 			ps.setInt(1, r);
 			ps.setInt(2, u);
 			System.out.println("Executing Query to select");
 			
 			rs = ps.executeQuery();
 			System.out.println("Query to select Executed");
 			
 			// If return already exists or greater than 15 days, do nothing
 			if (!rs.next()) {
 				System.out.println("Return already made or not within 15 days of purchase");
 				ps.close();
 				return true;
 			}
 			// If return made within 15 days, create tuples in Return and ReturnItem
 			// and update stock in Item
 			else {				
 					System.out.println("Processing Return...");
 					// Insert new tuple into Return table
 					
 					insert(receiptId);
 					System.out.println("Inserted into Return");
 					
 					// Insert new tuple into ReturnItem table
 					
 					ReturnItemOperations rio = new ReturnItemOperations();
 					rio.insert(upc, 1);
 					System.out.println("Inserted into ReturnItem");
 					
 					// Increment stock with given upc in Item table
 					ItemOperations io = new ItemOperations();
 					io.updateItem(upc, 1, null);
 					System.out.println("Updated stock on item");
 					ps.close();
 					return true;
 			}
 			
 		}
 		catch (SQLException ex) {
 			ExceptionEvent event = new ExceptionEvent(this, ex.getMessage());
 			fireExceptionGenerated(event);
 			System.out.println(ex.getMessage());
 			try {
 				con.rollback();
 				return false;
 			}
 			catch (SQLException ex2) {
 				event = new ExceptionEvent(this, ex2.getMessage());
 				fireExceptionGenerated(event);
 				return false;
 			}
 		}  
 	}
 	
 	/*
 	 * Checks if return date is within 15 days
 	 * Not being used
 	 */
 	boolean checkValidDate (Integer receiptId){
 
 		Date pdate;
 		String cid;
 		String cardno;
 		String expDate;
 		String eDate;
 		String dDate;
 
 
 
 		try{
 
 			ps = con.prepareStatement("SELECT * FROM purchase where receiptId = ?");
 
 			if (receiptId != null)
 			{
 				ps.setInt(1, receiptId);
 			}
 			else ps.setNull(1,Types.INTEGER);
 
 
 
 			ResultSet rs = ps.executeQuery();
 			// get info on ResultSet
 			ResultSetMetaData rsmd = rs.getMetaData();
 
 			// get number of columns
 			int numCols = rsmd.getColumnCount();
 
 			System.out.println(" ");
 
 			// display column names;
 			for (int i = 0; i < numCols; i++)
 			{
 				// get column name and print it
 
 				System.out.printf("%-15s", rsmd.getColumnName(i+1));    
 			}
 
 			System.out.println(" ");
 
 			while(rs.next())
 			{
 				// for display purposes get everything from Oracle 
 				// as a string
 
 				// simplified output formatting; truncation may occur
 
 				pdate = rs.getDate("pdate");
 				System.out.printf("%-15.15s", pdate);
 
 				cid = rs.getString("cid");
 				System.out.printf("%-15.15s", cid);
 
 				cardno = rs.getString("cardno");
 				if (rs.wasNull())
 				{
 					System.out.printf("%-15.15s", " ");
 				}
 				else
 				{
 					System.out.printf("%-15.15s", cardno);
 				}
 
 				expDate = rs.getString("EXPIRYDATE");
 				System.out.printf("%-15.15s", expDate);
 
 				eDate = rs.getString("EXPECTEDDATE");
 				if (rs.wasNull())
 				{
 					System.out.printf("%-15.15s\n", " ");
 				}
 				else
 				{
 					System.out.printf("%-15.15s\n", eDate);
 				} 
 				dDate = rs.getString("DELIVEREDDATE");
 				if (rs.wasNull())
 				{
 					System.out.printf("%-15.15s\n", " ");
 				}
 				else
 				{
 					System.out.printf("%-15.15s\n", dDate);
 				} 
 
 			}
 
 
 			//		 
 			//			  // close the statement; 
 			//			  // the ResultSet will also be closed
 			//			  stmt.close();
 			//			}
 			//			
 
 			//			Date utilDate = new Date(sqlDate.getTime());
 
 			//			DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd");
 			//get current date time with Date()
 			//			   Date date = new Date();
 			//			   System.out.println(dateFormat.format(date));
 
 			//get current date time with Calendar()
 			//			   Calendar cal = Calendar.getInstance();
 
 			//			   System.out.println(dateFormat.format(cal.getTime()));
 
 			//			   System.out.println(dateFormat.format(rs.getDate("pdate")));
 			//			   java.sql.Date sqlDate = dateFormat.format(rs.getTime(2));
 
 			//			if (utilDate.before(cal.getTime()));
 			//				System.out.println("before");
 
 			//			SimpleDateFormat fm = new SimpleDateFormat("dd/MM/yy");
 			//			java.util.Date utilDate = fm.parse(stringdate);
 			//			java.sql.Date sqldate = new java.sql.Date(utilDate.getTime());
 			//			ps.setDate(1,sqldate);
 
 
 			return true;
 		}
 		catch(SQLException ex){
 			ExceptionEvent event = new ExceptionEvent(this, ex.getMessage());
 			fireExceptionGenerated(event);
 
 			try {
 				con.rollback();
 				return false; 
 			}
 			catch (SQLException ex2) {
 				event = new ExceptionEvent(this, ex2.getMessage());
 				fireExceptionGenerated(event);
 				return false; 
 			}
 		}
 
 	}
 
 
 	//	public ResultSet display(){
 	//		try {
 	//			ps = con.prepareStatement("SELECT * FROM return", 
 	//					ResultSet.TYPE_SCROLL_INSENSITIVE,
 	//					ResultSet.CONCUR_READ_ONLY);
 	//
 	//			ResultSet rs = ps.executeQuery();
 	//
 	//			return rs; 
 	//		}
 	//		catch (SQLException ex) {
 	//			ExceptionEvent event = new ExceptionEvent(this, ex.getMessage());
 	//			fireExceptionGenerated(event);
 	//			// no need to commit or rollback since it is only a query
 	//
 	//			return null; 
 	//		}
 	//	}
 	
 	public static void main(String args[])
 	{
 
 		System.out.println("test");
 
 		AMSOracleConnection oCon = AMSOracleConnection.getInstance();
 		oCon.connect("ora_o0g6", "a40493058");
 		//		oCon.connect("ora_h5n8", "a44140028");
 
 		ReturnOperations ro = new ReturnOperations();
 
 		// both tests are for my account
 
 		// inside of 15 days and should be inserted
		ro.returnItem("1016", "111116");

 		// out side of 15 days from today and shouldn't be inserted into tables
 		//ro.returnItem("1015", "111114");
 
 
 
 
 
 
 	}
 
 }
 
