 package ca.ubc.cpsc304.r3.db;
 
 //general sql imports
 import java.sql.Connection;
 import java.sql.Date;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.Statement;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.GregorianCalendar;
 import java.util.List;
 
 import ca.ubc.cpsc304.r3.DaoUtility;
 import ca.ubc.cpsc304.r3.dto.BookCheckoutReportDto;
 import ca.ubc.cpsc304.r3.dto.BorrowingDto;
 import ca.ubc.cpsc304.r3.dto.CheckedOutBookDto;
 
 public class BorrowingDao {
 	
 	private ConnectionService connService;
 	
 	public BorrowingDao(ConnectionService connService){
 		this.connService = connService;
 	}
 	
 	public List<BorrowingDto> getBorrowedByID(int id) throws SQLException{
 		List<BorrowingDto> queryResult = new ArrayList<BorrowingDto>();
 		Connection conn = null; 
 		try {
 			conn = connService.getConnection();	
 			PreparedStatement ps = conn.prepareStatement(
 					"SELECT * "+
 					"FROM borrowing "  +
 					"WHERE inDate IS NULL AND " +
 					"bid=?");
 			ps.setInt(1, id);
 			ResultSet rs = ps.executeQuery();
 			
 			
 			while(rs.next()){
 				// for each row, put the data in the dto
 				// and add it to list of results
 				BorrowingDto dto = new BorrowingDto();
 				dto.setBorid(rs.getInt("borid"));
 				dto.setBid(rs.getInt("bid"));
 				dto.setCallNumber(rs.getInt("callNumber"));
 				dto.setCopyNo(rs.getInt("copyNo"));
 				dto.setOutDate(rs.getDate("outDate"));
 				dto.setInDate(rs.getDate("inDate"));
 				queryResult.add(dto);
 			}
 		} catch (SQLException e) {
 			// two options here. either don't catch this exception and 
 			// make the caller handle it, or wrap it in a more 
 			// descriptive exception depending on the situation.
 			// I'll just throw it
 			throw e;
 			
 		} finally {
 			// don't forget to close the connection
 			// when you're done with it
 			if(conn != null){
 				conn.close();
 			}
 		}
 		return queryResult;
 	}
 
 	public List<CheckedOutBookDto> generateCheckedOutBooksReport(String subject) throws SQLException {
 		
 		Connection conn = null;
 		try {
 			
 			// is subject valid?
 			boolean hasSubject = (subject != null && !subject.isEmpty());
 			
 			String query =  "SELECT DISTINCT BC.callNumber, BC.copyNo, BK.title, BORRWRING.outDate, BT.bookTimeLimit " +
 							"FROM bookcopy BC, borrowing BORRWRING, borrower B, book BK, hassubject HS, borrowertype BT " + 
 							"WHERE BC.status='out' AND BC.callNumber=BORRWRING.callNumber AND BC.copyNo=BORRWRING.copyNo AND BORRWRING.bid=B.bid AND B.btype=BT.btype AND BK.callNumber=BC.callNumber AND BORRWRING.inDate IS NULL ";
 			if(hasSubject){
 				query = query +
 							"AND BC.callNumber=HS.callNumber AND HS.subject=? ";
 			}
 			query = query + "ORDER BY callNumber;";
 			
 			conn = this.connService.getConnection();
 			PreparedStatement ps = conn.prepareStatement(query);
 			
 			// set the subject parameter if subject is valid
 			if(hasSubject){
 				ps.setString(1, subject);
 			}
 			
 			ResultSet rs = ps.executeQuery();
 			
 			List<CheckedOutBookDto> results = new ArrayList<CheckedOutBookDto>();
 			
 			while(rs.next()){
 				
 				CheckedOutBookDto dto = new CheckedOutBookDto();
 				dto.setCallNumber(rs.getInt("BC.callNumber"));
 				dto.setCopyNo(rs.getInt("BC.copyNo"));
 				dto.setTitle(rs.getString("BK.title"));
 				
 				Date outDate = rs.getDate("BORRWRING.outDate");
 				dto.setOutDate(outDate);
 				
 				int timeLimitWeeks = Integer.parseInt(rs.getString("BT.bookTimeLimit"));
 				
 				// dueDate = checkoutDate + (num of weeks allowed * length of week)
 				dto.setDueDate(new Date(outDate.getTime() + (timeLimitWeeks * DaoUtility.WEEK)));
 				
 				// add it to list
 				results.add(dto);
 			}
 			
 			return results;
 			
 		} finally {
 			
 			if (conn != null){
 				
 				conn.close();
 				
 			}
 		}
 	}
 	
 	/**
 	 * Check-out items borrowed by a borrower. To borrow items, borrowers
 	 * provide their card number and a list with the call numbers of the items
 	 * they want to check out. The system determines if the borrower's account
 	 * is valid and if the library items are available for borrowing. Then it
 	 * registers the items as "out", adding them to the list of library
 	 * materials that are on-loan by that borrower and prints a note with the
 	 * item's details and the due day.
 	 */
 
 	public Date borrowItem(int bid, int callNo) throws Exception {
 
 		Connection conn = null;
 		try {
 			conn = connService.getConnection();
 			Statement st = conn.createStatement();
 			ResultSet rs = null;
 			PreparedStatement ps = null;
 
 			// Check if has overdue book
			rs = st.executeQuery("SELECT amount FROM Fine F, Borrowing B "
					+ "WHERE F.amount > 0 AND B.borid=F.borid AND B.bid="
					+ DaoUtility.convertToSQLvalue(bid));
 			if (rs.next()) {
 				int fine = rs.getInt("amount");
 				System.out.println("You currently have a fine of "
 						+ fine);
 				throw new SQLException("You currently have a fine of $" +fine+" and have been blocked from borrowing.");
 			}
 
 			// get an available copy
 			int copy = findAvailableCopy(callNo);
 			if (copy == -1) {
 				// prompt user to request hold
 				System.out.println("Sorry, there are no available copies ");
 				throw new SQLException("No available copies for call number " + callNo + ".<br>");
 			}
 
 			// Create new borrowing record
 			ps = conn
 					.prepareStatement("INSERT into Borrowing(bid, callNumber, copyNo, outDate) "
 							+ "values (?,?,?,?)");
 			ps.setInt(1, bid);
 			ps.setInt(2, callNo);
 			ps.setInt(3, copy);
 			ps.setDate(4, DaoUtility.makeDate(0));
 
 			// Update book copy to "out"
 			ps.executeUpdate();
 			ps = conn.prepareStatement("UPDATE BookCopy "
 					+ "SET status='out' WHERE callNumber="
 					+ DaoUtility.convertToSQLvalue(callNo) + " AND copyNo="
 					+ DaoUtility.convertToSQLvalue(copy));
 			ps.executeUpdate();
 
 			rs = st.executeQuery("SELECT bookTimeLimit FROM Borrower, BorrowerType WHERE bid="
 					+ bid);
 			if (!rs.next())
 				throw new SQLException("BID "+bid+" not found.");
 
 			return DaoUtility.makeDate(DaoUtility.calcBorrowLimit(rs
 					.getString("bookTimeLimit")));
 		} finally {
 			if (conn != null)
 				conn.close();
 		}
 	}
 
 	// Helper to find first available copy of a book.
 	// returns -1 if no copies are available.
 	public int findAvailableCopy(int callNum) throws SQLException {
 		Connection conn = null;
 		try {
 			conn = connService.getConnection();
 
 			Statement st = conn.createStatement();
 			ResultSet rs = null;
 			rs = st.executeQuery("SELECT copyNo FROM bookCopy "
 					+ "WHERE status='in' AND callNumber="
 					+ DaoUtility.convertToSQLvalue(callNum));
 			if (rs.next()) {
 				int i = rs.getInt(1);
 				if (conn != null)
 					conn.close();
 				return i;
 			}
 			return -1;
 		} finally {
 			if (conn != null)
 				conn.close();
 		}
 	}
 
 	/**
 	 * Generates a collection of the most popular books that have been 
 	 * checked out during the given year.
 	 * 
 	 * The query:
 	 *  - filters by year (starting Jan 1 and ending just prior to Jan 1 of the following year)
 	 *  - orders by number of times borrowed
 	 *  
 	 * @param year the year in which to filter by
 	 * @param limit the maximum number of results to return
 	 * @return a list of BookCheckOutReportDto's, sorted by the number
 	 * 		of times the book has been checked out during the year (descending).
 	 * 		The size of the collection will always be <= limit.
 	 * @throws SQLException
 	 */
 	public List<BookCheckoutReportDto> generateMostPopularBooksReport(int year, int limit) throws SQLException {
 
 		Connection conn = null;
 		
 		try {
 			
 			conn = connService.getConnection();
 			
 			// gets the first day of the year specified
 			Date yearStart = new Date(new GregorianCalendar(year, Calendar.JANUARY, 1).getTime().getTime());
 			
 			// gets the very first day of the next year.
 			// the query will be:   yearStart <= validResult < yearEnd
 			Date yearEnd = new Date(new GregorianCalendar(year + 1, Calendar.JANUARY, 1).getTime().getTime());
 			
 			
 			PreparedStatement ps = conn.prepareStatement(
 					"SELECT book.callNumber, book.title, book.mainAuthor, COUNT(*) AS borrCount " +
 					"FROM borrowing, book " +
 					"WHERE borrowing.outdate >= ? AND borrowing.outdate < ? AND borrowing.callNumber=book.callNumber " +
 					"GROUP BY book.callNumber " + 
 					"ORDER BY borrCount DESC " +
 					"LIMIT ?");
 			
 			ps.setDate(1, yearStart);
 			ps.setDate(2, yearEnd);
 			ps.setInt(3, limit);
 			
 			ResultSet rs = ps.executeQuery();
 			
 			List<BookCheckoutReportDto> results = new ArrayList<BookCheckoutReportDto>();
 			while(rs.next()){
 				
 				BookCheckoutReportDto dto = new BookCheckoutReportDto();
 				dto.setCallNumber(rs.getInt("book.callNumber"));
 				dto.setTitle(rs.getString("book.title"));
 				dto.setMainAuthor(rs.getString("book.mainAuthor"));
 				dto.setBorrowedCount(rs.getInt("borrCount"));
 				
 				results.add(dto);
 			}
 			
 			return results;
 
 		} finally {
 			if(conn != null){
 				conn.close();
 			}
 		}
 	}
 
 }
 
