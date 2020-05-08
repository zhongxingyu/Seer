 package tables;
 
 import java.sql.Connection;
 import java.sql.Date;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.ResultSetMetaData;
 import java.sql.SQLException;
 import java.sql.Statement;
 import java.text.DateFormat;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.Collection;
 import java.util.GregorianCalendar;
 import java.util.List;
 
 import tables.Book;
 import tables.Table;
 import users.Conn;
 
 public class Borrower implements Table {
 
 	private Integer bid;
 	private String password;
 	private String name;
 	private String address;
 	private String phone;
 	private String emailAddress;
 	private Integer sinOrStNum;
 	private Calendar expiryDate;
 	private Integer bookTimeLimit;
 	private String type;
 
 	private Connection con;
 
 	/**
 	 * Creates an empty Borrower
 	 */
 	public Borrower() {
 		con = Conn.getInstance().getConnection();
 	}
 
 	/**
 	 * Builds a Borrower object from an open result set only for use within this
 	 * object! Call next() before you pass the ResultSet
 	 * 
 	 * @param rs
 	 *            TODO there are several calls to get() that are stubbed out
 	 * @throws SQLException
 	 */
 	public Borrower(ResultSet rs) throws SQLException {
 		con = Conn.getInstance().getConnection();
 		int fieldIndex = 1;
 		bid = rs.getInt(fieldIndex++);
 		String pw = rs.getString(fieldIndex++);
 		password = (rs.wasNull()) ? null : pw;
 		String n = rs.getString(fieldIndex++);
 		name = (rs.wasNull()) ? null : n;
 		String addr = rs.getString(fieldIndex++);
 		address = (rs.wasNull()) ? null : addr;
 		String p = rs.getString(fieldIndex++);
 		phone = (rs.wasNull()) ? null : p;
 		String email = rs.getString(fieldIndex++);
 		emailAddress = (rs.wasNull()) ? null : email;
 		int sos = rs.getInt(fieldIndex++);
 		sinOrStNum = (rs.wasNull()) ? null : sos;
 		Date sqlExpiryDate = rs.getDate(fieldIndex++);
 		expiryDate = (rs.wasNull()) ? null : new GregorianCalendar();
 		if (expiryDate != null) {
 			expiryDate.setTime(sqlExpiryDate);
 		}
 		String t = rs.getString(fieldIndex++);
 		type = (rs.wasNull()) ? null : t;
 	}
 
 	@Override
 	public String[][] display() throws SQLException {
 
 		ArrayList<String[]> borrowerGrowable = new ArrayList<String[]>();
 
 		PreparedStatement ps = con.prepareStatement("SELECT * FROM Borrower");
 		ResultSet rs = ps.executeQuery();
 		ResultSetMetaData md = rs.getMetaData();
 
 		int numFields = md.getColumnCount();
 		String[] columnNames = new String[numFields];
 		for (int i = 0; i < numFields; i++) {
 			columnNames[i] = md.getColumnName(i + 1);
 		}
 		borrowerGrowable.add(columnNames);
 
 		DateFormat df = new SimpleDateFormat("dd-MM-yyyy");
 
 		while (rs.next()) {
 			String[] tuple = new String[numFields];
 			int fieldIndex = 0;
 
 			// the bid field is marked not null in database
 			tuple[fieldIndex] = "" + rs.getInt(fieldIndex + 1);
 			fieldIndex++;
 
 			// these fields might be null
 			String password = rs.getString(fieldIndex + 1);
 			tuple[fieldIndex] = (!rs.wasNull()) ? password : "null";
 			fieldIndex++;
 
 			String name = rs.getString(fieldIndex + 1);
 			tuple[fieldIndex] = (!rs.wasNull()) ? name : "null";
 			fieldIndex++;
 
 			String address = rs.getString(fieldIndex + 1);
 			tuple[fieldIndex] = (!rs.wasNull()) ? address : "null";
 			fieldIndex++;
 
 			String phone = rs.getString(fieldIndex + 1);
 			tuple[fieldIndex] = (!rs.wasNull()) ? phone : "null";
 			fieldIndex++;
 
 			String emailAddress = rs.getString(fieldIndex + 1);
 			tuple[fieldIndex] = (!rs.wasNull()) ? emailAddress : "null";
 			fieldIndex++;
 
 			String sinOrStNo = rs.getString(fieldIndex + 1);
 			tuple[fieldIndex] = (!rs.wasNull()) ? sinOrStNo : "null";
 			fieldIndex++;
 
 			String expiryDate = rs.getString(fieldIndex + 1);
 			tuple[fieldIndex] = (!rs.wasNull()) ? df.format(expiryDate)
 					: "null";
 			fieldIndex++;
 
 			String type = rs.getString(fieldIndex + 1);
 			tuple[fieldIndex] = (!rs.wasNull()) ? type : "null";
 
 			borrowerGrowable.add(tuple);
 		} // end while
 
 		rs.close();
 
 		int numRows = borrowerGrowable.size();
 		String[][] borrower = new String[numRows][];
 		for (int i = 0; i < numRows; i++) {
 			borrower[i] = borrowerGrowable.get(i);
 		}
 		return borrower;
 	}
 
 	@Override
 	public void update() throws SQLException {
 		Statement stmt = con.createStatement();
 
 		// updating the corresponding tuple in Borrower table
 		stmt.executeUpdate("UPDATE Borrwer SET bid = " + bid + ", password = "
 				+ password + ", name = " + name + ", address = " + address
 				+ ", phone = " + phone + ", emailAddress = " + emailAddress
 				+ ", sinOrStNo = " + sinOrStNum + ", expiryDate = "
 				+ expiryDate + ", type = " + type);
 	}
 
 	/**
 	 * Deletes the tuple in the Borrower table whose primary key corresponds to
 	 * this Borrower's bid. All other attributes are ignored.
 	 * 
 	 * @return true if the tuple was successfully deleted, otherwise false
 	 */
 	@Override
 	public boolean delete() throws SQLException {
 
 		String sql = "DELETE FROM Borrower WHERE bid = " + bid;
 		Statement stmt = con.createStatement();
 		stmt.executeQuery(sql);
 		stmt.close();
 		return true;
 
 	}
 
 	/**
 	 * Gets every Borrower from database and create each Borrower object. If
 	 * there are no Borrowers in the database the collection will be empty.
 	 * 
 	 * @return a collection containing all the Borrower objects in the database
 	 * @throws SQLException
 	 */
 	@Override
 	public Collection<Table> getAll() throws SQLException {
 		ArrayList<Table> borrowers = new ArrayList<Table>();
 
 		Statement stmt = con.createStatement();
 		ResultSet rs = stmt.executeQuery("SELECT * FROM Borrwer");
 		while (rs.next()) {
 			borrowers.add(new Borrower(rs));
 		}
 		stmt.close();
 
 		return null;
 	}
 
 	@Override
 	public Table get() throws SQLException {
 		String sql = "SELECT * FROM Borrower WHERE bid = " + bid;
 		Statement stmt = con.createStatement();
 		ResultSet rs = stmt.executeQuery(sql);
 
 		if (rs.next()) {
 			return new Borrower(rs);
 		} else
 			return null;
 	}
 
 	public boolean insert() throws SQLException {
 
 		Date sqlDate = new Date(expiryDate.getTimeInMillis());
 
 		PreparedStatement ps = con
 				.prepareStatement("INSERT INTO Borrower "
 						+ "(bid,password,name,address,phone,emailAddress,sinOrStNo,expiryDate,type) VALUES (bidCounter.nextval,?,?,?,?,?,?,?,?)");
 
 		ps.setString(2, password);
 		ps.setString(3, name);
 		ps.setString(4, address);
 		ps.setString(5, phone);
 		ps.setString(6, emailAddress);
 		ps.setInt(7, sinOrStNum);
 		ps.setDate(8, sqlDate);
 		ps.setString(9, type);
 		int numRowsChanged = ps.executeUpdate();
 		if (numRowsChanged == 1) {
 			ps.close();
 			ps = con.prepareStatement("SELECT bidCounter.currval FROM DUAL");
 			ResultSet rs = ps.executeQuery();
 			if (rs.next()) {
 				bid = rs.getInt(1);
 				return true;
 			}
 		}
 		return false;
 	}
 
 	/**
 	 * @return the bid
 	 */
 	public Integer getBid() {
 		return bid;
 	}
 
 	/**
 	 * @param bid
 	 *            the bid to set
 	 */
 	public void setBid(Integer bid) {
 		this.bid = bid;
 	}
 
 	/**
 	 * @return the password
 	 */
 	public String getPassword() {
 		return password;
 	}
 
 	/**
 	 * @param password
 	 *            the password to set
 	 */
 	public void setPassword(String password) {
 		this.password = password;
 	}
 
 	/**
 	 * @return the name
 	 */
 	public String getName() {
 		return name;
 	}
 
 	/**
 	 * @param name
 	 *            the name to set
 	 */
 	public void setName(String name) {
 		this.name = name;
 	}
 
 	/**
 	 * @return the address
 	 */
 	public String getAddress() {
 		return address;
 	}
 
 	/**
 	 * @param address
 	 *            the address to set
 	 */
 	public void setAddress(String address) {
 		this.address = address;
 	}
 
 	/**
 	 * @return the phone
 	 */
 	public String getPhone() {
 		return phone;
 	}
 
 	/**
 	 * @param phone
 	 *            the phone to set
 	 */
 	public void setPhone(String phone) {
 		this.phone = phone;
 	}
 
 	/**
 	 * @return the emailAddress
 	 */
 	public String getEmailAddress() {
 		return emailAddress;
 	}
 
 	/**
 	 * @param emailAddress
 	 *            the emailAddress to set
 	 */
 	public void setEmailAddress(String emailAddress) {
 		this.emailAddress = emailAddress;
 	}
 
 	/**
 	 * @return the sinOrStNum
 	 */
 	public Integer getSinOrStNum() {
 		return sinOrStNum;
 	}
 
 	/**
 	 * @param sinOrStNum
 	 *            the sinOrStNum to set
 	 */
 	public void setSinOrStNum(Integer sinOrStNum) {
 		this.sinOrStNum = sinOrStNum;
 	}
 
 	/**
 	 * @return the expiryDate
 	 */
 	public Calendar getExpiryDate() {
 		return expiryDate;
 	}
 
 	/**
 	 * @param expiryDate
 	 *            the expiryDate to set
 	 */
 	public void setExpiryDate(Calendar expiryDate) {
 		this.expiryDate = expiryDate;
 	}
 
 	/**
 	 * @return the bookTimeLimit
 	 */
 	public Integer getBookTimeLimit() {
 		return bookTimeLimit;
 	}
 
 	/**
 	 * @param bookTimeLimit
 	 *            the bookTimeLimit to set
 	 */
 	public void setBookTimeLimit(Integer bookTimeLimit) {
 		this.bookTimeLimit = bookTimeLimit;
 	}
 
 	public String[][] searchBookByTitle(String title) throws SQLException {
 		Statement stmt = con.createStatement();
 		ResultSet rs = stmt
 				.executeQuery("SELECT Book.*, COUNT(status) FROM Book, BookCopy "
 						+ "WHERE Book.title="
 						+ title
 						+ " AND Book.callNumber=BookCopy.callNumber");
 		ArrayList<Book> lob = new ArrayList<Book>();
 
 		while (rs.next()) {
 			String callNo = rs.getString(1);
 			// String isbn = rs.getString(2);
 			// String title1 = rs.getString(3);
 			// String mainAuthor = rs.getString(4);
 			// String publisher = rs.getString(5);
 			// int year = rs.getInt(6);
 			// int copiesIn = rs.getInt(7);
 
 			Book b = new Book();
 			b.setCallNumber(callNo);
 			b = b.get();
 			lob.add(b);
 		}
 		String[][] bookArray = listOfBooksTo2DArray(lob);
 		return bookArray;
 	}
 
 	public String[][] searchBookByAuthor(String author) throws SQLException {
 		Statement stmt = con.createStatement();
 		String sql = "SELECT Book.* FROM Book, HasAuthor "
 				+ "WHERE Book.callNumber=HasAuthor.callNumber AND HasAuthor.name='"
 				+ author + "'";
 		ResultSet rs = stmt.executeQuery(sql);
 		ArrayList<Book> lob = new ArrayList<Book>();
 		while (rs.next()) {
 			String callNo = rs.getString(1);
 			Book b = new Book();
 			b.setCallNumber(callNo);
 			b = b.get();
 			lob.add(b);
 		}
 		String[][] bookArray = listOfBooksTo2DArray(lob);
 		return bookArray;
 	}
 
 	public String[][] searchBookBySubject(String subject) throws SQLException {
 		Statement stmt = Conn.getInstance().getConnection().createStatement();
 		String sql = "Select Book.CallNumber FROM Book, HasSubject "
 				+ "WHERE Book.CallNumber = HasSubject.CallNumber AND HasSubject.subject = '"
 				+ subject + "'";
 		ResultSet rs = stmt.executeQuery(sql);
 		ArrayList<Book> lob = new ArrayList<Book>();
 		while (rs.next()) {
 			String callNo = rs.getString(1);
 			Book b = new Book();
 			b.setCallNumber(callNo);
 			b = b.get();
 			lob.add(b);
 		}
 		String[][] bookArray = listOfBooksTo2DArray(lob);
 		return bookArray;
 	}
 
 	public ArrayList<ArrayList<Table>> checkAccount() throws SQLException {
 		Statement stmt1 = con.createStatement();
 		String sql1 = "SELECT Borrowing.borid FROM Borrower, Book, Borrowing, BookCopy "
 				+ "WHERE Book.callNumber=Borrowing.callNumber AND Borrower.bid=Borrowing.bid "
 				+ "AND BookCopy.callNumber=Book.callNumber And BookCopy.status='out'";
 		ResultSet rsCheckedOut = stmt1.executeQuery(sql1);
 		ArrayList<Table> lob = new ArrayList<Table>();
 		while (rsCheckedOut.next()) {
 			int borid = rsCheckedOut.getInt(1);
 			Borrowing b = new Borrowing();
 			b.setBorid(borid);
 			b = (Borrowing) b.get();
 			lob.add(b);
 		}
 
 		Statement stmt2 = con.createStatement();
 		String sql2 = "SELECT Fine.fid FROM Borrower, Fine, Borrowing "
 				+ "WHERE Borrower.bid=Borrowing.bid AND Borrowing.borid=Fine.borid";
 		ResultSet rsFines = stmt2.executeQuery(sql2);
 		ArrayList<Table> lof = new ArrayList<Table>();
 		while (rsFines.next()) {
 			int fid = rsFines.getInt(1);
 			Fine f = new Fine();
 			f.setFid(fid);
 			f = (Fine) f.get();
 			lof.add(f);
 		}
 
 		Statement stmt3 = con.createStatement();
 		String sql3 = "SELECT HoldRequest.hid FROM Borrower, HoldRequest "
 				+ "WHERE Borrower.bid=HoldRequest.bid";
 		ResultSet rsHolds = stmt3.executeQuery(sql3);
 		ArrayList<Table> loh = new ArrayList<Table>();
 		while (rsHolds.next()) {
 			int hid = rsHolds.getInt(1);
 			HoldRequest h = new HoldRequest();
 			h.setHid(hid);
 			h = (HoldRequest) h.get();
 			loh.add(h);
 		}
 
 		ArrayList<ArrayList<Table>> loT = new ArrayList<ArrayList<Table>>();
 		loT.add(lob);
 		loT.add(lof);
 		loT.add(loh);
 
 		return loT;
 	}
 
 	public void placeHoldRequest(String isbn) throws SQLException {
 		Book b = new Book();
 		b.setIsbn(isbn);
 		b = b.get();
 		String call = b.getCallNumber();
 
 		Statement stmt = con.createStatement();
 		HoldRequest h;
 		String sql = "INSERT INTO HoldRequest (hid, bid, callNumber, issueDate) "
 				+ "VALUES ('RANDHID',"
 				+ this.bid
 				+ "'"
 				+ call
 				+ "', GETDATE())";
 		ResultSet rs = stmt.executeQuery(sql);
 	}
 
 	public void payFine(Integer borid, Integer amountInCents)
 			throws SQLException {
 		Fine f = new Fine();
 		Borrowing bor = new Borrowing();
 		bor.setBorid(borid);
 		f.setBorrowing(bor);
 		f = (Fine) f.get();
 		if (amountInCents == f.getAmount()) {
 			Statement stmt = con.createStatement();
 			String sql = "DELETE FROM Fine WHERE EXISTS "
 					+ "(SELECT Borrower.bid FROM Borrower, Fine, Borrowing "
 					+ "WHERE Borrower.bid=Borrowing.bid AND Fine.borid="
 					+ borid + ")";
 			ResultSet rs = stmt.executeQuery(sql);
 			System.out.println("Amount paid in full.");
 		} else {
 			Statement stmt = con.createStatement();
 			String sql = "UPDATE fine SET amount='"
 					+ (f.getAmount() - amountInCents)
 					+ "' "
 					+ "FROM Fine f INNER JOIN Borrowing bor ON f.borid=bor.borid "
 					+ "INNER JOIN Borrower b ON bor.bid=b.bid";
 			ResultSet rs = stmt.executeQuery(sql);
 			System.out.println("You have paid '" + amountInCents
 					+ "' cents, still owing '" + f.getAmount() + "' cents.");
 		}
 	}
 
 	public boolean isValid() throws SQLException {
 	  // if Borrower has unpaid fines
 	  Statement stmt = con.createStatement();
 	  String sql = "SELECT B.bid FROM Borrower B WHERE EXISTS " +
 	  		"(SELECT F.borid FROM Borrowing W, Fine F " +
 	  		"WHERE B.bid=W.bid AND W.borid=F.borid)";
 	  ResultSet rs = stmt.executeQuery(sql);
 	  if(rs.next()) {
 			int id = rs.getInt(1);
 			if(id == this.bid)
 				return false;
 	  }
 	  
 	  // if Borrower has overdue books
 	  Statement stmt2 = con.createStatement();
 	  String sql2 = "SELECT B.bid FROM Borrower B WHERE EXISTS " +
 	  		"(SELECT W.borid FROM Borrowing W, BorrowerType T " +
 	  		"WHERE B.bid=W.bid AND B.type=T.type AND " +
 	  		"DATEADD(W.outDate,T.bookTimeLimit,outDate) < Convert(datetime, Convert(int, GetDate())))";
 	  ResultSet rs2 = stmt2.executeQuery(sql2);
 	  if(rs2.next()) {
 		  int id = rs2.getInt(1);
 		  if(id == this.bid)
 			  return false;
 	  }
 	  return true;
   }
 	
 	public String[][] listOfBooksTo2DArray(ArrayList<Book> lob) throws SQLException {
 
 		int copiesIn,copiesOut;
 		int numColumns = 8;
		int numRows = lob.size() + 1;
 		String[][] twoDArray = new String[numRows][numColumns];
 		String[] columnNames = {"callNumber","isbn","title","mainAuthor","publisher","year","copiesIn","copiesOut"};
 		for(int i=0;i<numColumns;i++) {
 			twoDArray[0][i]= columnNames[i];
 		}
 
		for(int i=1;i<=numRows;i++) {
			// populate row[i] with the (i-1)th Book from lob
			Book b = lob.get(i-1);
 			twoDArray[i][0] = b.getCallNumber().toString();
 			twoDArray[i][1] = b.getIsbn();
 			twoDArray[i][2] = b.getTitle();
 			twoDArray[i][3] = b.getMainAuthor();
 			twoDArray[i][4] = b.getPublisher();
 			twoDArray[i][5] = b.getYear().toString();
 			
 			// Counts number of copies of specific book that are in
 			Statement stmt = con.createStatement();
 			String sql = "SELECT COUNT(BookCopy) AS copiesIn FROM BookCopy " +
 					"WHERE BookCopy.callNumber='" + b.getCallNumber() + "' AND BookCopy.status='in'";
 			ResultSet rs = stmt.executeQuery(sql);
 			
 			if(rs.next()) {
 				copiesIn = rs.getInt(1);
 				twoDArray[i][6] = Integer.toString(copiesIn);
 			}
 			else
 				twoDArray[i][6] = Integer.toString(0);
 			
 			// Counts number of copies of specific book that are out
 			Statement stmt2 = con.createStatement();
 			String sql2 = "SELECT COUNT(BookCopy) AS totalCopies FROM Book, BookCopy " +
 					"WHERE BookCopy.callNumber='" + b.getCallNumber() + "' AND BookCopy.status<>'in'";
 			ResultSet rs2 = stmt2.executeQuery(sql2);
 			
 			if(rs2.next()) {
 				copiesOut = rs2.getInt(1);
 				twoDArray[i][7] = Integer.toString(copiesOut);
 			}
 			else
 				twoDArray[i][7] = Integer.toString(0);			
 		}
 		return twoDArray;
 	}
 
 	
 }
