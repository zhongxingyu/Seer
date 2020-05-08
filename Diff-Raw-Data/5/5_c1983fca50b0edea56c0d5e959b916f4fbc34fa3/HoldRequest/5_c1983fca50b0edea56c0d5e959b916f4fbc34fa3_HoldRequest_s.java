 package tables;
 
 import java.sql.Connection;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.ResultSetMetaData;
 import java.sql.SQLException;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.Collection;
 import java.util.GregorianCalendar;
 import java.util.Iterator;
 
 import users.Conn;
 
 /**
  * This class represents the HoldRequest table in the database.
  * 
  * Changes: 25 Nov
  * - fixed HoldRequest(hid:int) constructor
  *    Moved the initialization of the Connection to before SQL calls are attempted
  *    It was trying to set its attributes without initializing them first.
  *    The column indexes were incorrect.
  * 
  * - fixed HoldRequest(:Borrower,:Book) constructor, but marked is deprecated.
  *   There could be several HoldRequests that match the bid and call number,
  *   but only the first such one will be returned. 
  * 
  * - changed get()
  *   hid will never be null since it's a primitive.  Changed this to 0.
  * 
  * - fixed getAll()
  *   Didn't want to go through all that code, so I just put the code from
  *   HoldRequest(hid:int) into a loop and added them to an array list.
  * 
  * - fixed display()
  *   DateFormat was trying to format a Calendar object.  Added a call to getTime().
  *   case switches didn't make any sense and were producing nulls.  Replaced them.
  * 
  * - fixed update()
  *   changed attribute to callNumber from callNo, which doesn't exist.
  *   changed attribute to issuedDate from issueDate, which doesn't exist.
  *   got rid of the line where you try to set the date to Calendar::toString().
  *   It doesn't work like that.
  * 
  * - fixed delete()
  *   changed the table to delete from to HoldRequest instead of BookCopy
  * 
  * 
  * @author Christiaan Fernando
  * 
  */
 public class HoldRequest implements Table {
 	private final SimpleDateFormat sdf = new SimpleDateFormat("dd-MMM-yyyy");
 
 	// The fields for HoldRequest in the table are hid, bid, callNo, issueDate,
 	// in that order.
 	private Integer hid;
 	private Borrower borr;
 	private Book b;
 	private Calendar issueDate;
 
 	private Connection c;
 	private PreparedStatement ps;
 	private ResultSet rs;
 
 	/**
 	 * Default Constructor.
 	 */
 	public HoldRequest() {
 		c = Conn.getInstance().getConnection();
 	}
 
 	/**
 	 * HoldRequest Constructor.
 	 * 
 	 * This constructor takes in a Calendar, Book, Borrower and creates a
 	 * HoldRequest Object. This object has not been added to the SQL table yet,
 	 * so insert() will need to be called on this object in the future if it
 	 * needs to be added.
 	 * 
 	 * @param issueDate
 	 *            Issue Date for the HoldRequest
 	 * @param b
 	 *            Book for the HoldRequest
 	 * @param borr
 	 *            Borrower for the HoldRequest
 	 */
 	public HoldRequest(Borrower borr, Book b, Calendar issueDate) {
 		this.issueDate = issueDate;
 		this.b = b;
 		this.borr = borr;
 
 		c = Conn.getInstance().getConnection();
 	}
 
 	/**
 	 * HoldRequest Constructor.
 	 * 
 	 * This constructor takes in an Integer and find the HoldRequest entry in
 	 * the SQL table with this hid. This assumes that the entry already exists.
 	 * If it does not, this will return an error value of -1 as the hid.
 	 * 
 	 * @param hid
 	 *            HoldRequest id in the SQL table.
 	 * @throws SQLException
 	 */
 	public HoldRequest(Integer hid) throws SQLException {
 		c = Conn.getInstance().getConnection();
 		ps = c.prepareStatement("SELECT * FROM HoldRequest WHERE hid = ?");
 		ps.setInt(1, hid);
 
 		rs = ps.executeQuery();
 
 		if (rs.next()) {
                   this.hid = hid;
                   b = new Book();
                   borr = new Borrower();
                   borr.setBid(rs.getInt(2));
                   borr = (Borrower) borr.get();
                   b.setCallNumber(rs.getString(4));
                   b = (Book) b.get();
                   issueDate = new GregorianCalendar();
                   this.issueDate.setTime(rs.getDate(3));
 		}
                 else
                 {
                   this.hid = -1;
                 }
 	}
         
         /**
          * Creates a new HoldRequest object based on a result set.
          * Call next() on result set before passing in.
          * @param rs
          * @throws SQLException 
          */
         private HoldRequest(ResultSet rs) throws SQLException
         {
           hid = rs.getInt(1);
           b = new Book();
           borr = new Borrower();
           borr.setBid(rs.getInt(2));
           borr = (Borrower) borr.get();
           b.setCallNumber(rs.getString(4));
           b = (Book) b.get();
           issueDate = new GregorianCalendar();
           this.issueDate.setTime(rs.getDate(3));
         }
 
 	/**
 	 * HoldRequest Constructor.
 	 * 
          * This constructor will only get the first such hold request when there
          * could be several.  Do not use it.
          * 
 	 * This constructor takes in a Borrower and a Book and finds the HoldRequest
 	 * entry in the SQL table that has these two values. This assumes the entry
 	 * already exists. If it does not, this will call the default constructor.
 	 * 
 	 * @param borr
 	 *            Borrower whose bid is shared with the HoldRequest
 	 * @param b
 	 *            Book whose callNo is shared with the HoldRequest
 	 * @throws SQLException
 	 */
         @Deprecated
 	public HoldRequest(Borrower borr, Book b) throws SQLException {
 		c = Conn.getInstance().getConnection();
 		ps = c.prepareStatement("SELECT * FROM HoldRequest WHERE bid = ?, callNo = ?");
 		ps.setInt(1, borr.getBid());
 		ps.setString(2, b.getCallNumber());
 
 		rs = ps.executeQuery();
 
 		if (rs.next()) {
 			this.hid = rs.getInt(1);
 			this.b = b;
 			this.borr = borr;
 			this.issueDate.setTime(rs.getDate(4));
 		}
 
 	}
 
 	/**
 	 * Returns a String representation of the table.
 	 * 
 	 * Returns a 2-D String representation of the HoldRequest table.
 	 */
 	@Override
 	public String[][] display() throws SQLException {
 		String[][] result = null;
 		Collection<Table> hrt = getAll();
 
 		ResultSetMetaData md = getMeta();
 
                 result = new String[hrt.size() + 1][md.getColumnCount()];
                 int i = 0;
                 int numCols = md.getColumnCount();
 
                 for (int colIndex = 1; colIndex <= numCols; colIndex++)
                 {
                   result[i][colIndex-1] = md.getColumnName(colIndex);
                 }
                 i++;
 
                 Iterator<Table> hrItr = hrt.iterator();
                 while (hrItr.hasNext()) {
 
                   HoldRequest hr = (HoldRequest) hrItr.next();
                   int colIndex = 0;
 
                   // hid
                   result[i][colIndex++] = ""+hr.getHid();
 
                   // bid
                   result[i][colIndex++] = ""+hr.getBorr().getBid();
 
                   // issuedDate
                   result[i][colIndex++] = sdf.format(hr.getIssueDate().getTime());
 
                   // callNo
                   result[i][colIndex++] = ""+hr.getB().getCallNumber();
 
                   i++;
                 } // end while
 		return result;
 	}
 
 	/**
 	 * Updates the SQL table.
 	 * 
 	 * This updates this HoldRequest object in the HoldRequest table. This
 	 * assumes the item already exists.
 	 */
 	@Override
 	public void update() throws SQLException {
 		ps = c.prepareStatement("UPDATE holdRequest SET bid = ?, issuedDate = ?, callNumber = ? WHERE hid = ?");
 
 		ps.setInt(4, hid);
 		ps.setInt(1, borr.getBid());
 		ps.setDate(2, new java.sql.Date(issueDate.getTime().getTime()));
 		ps.setString(3, b.getCallNumber());
 
 		int rowCount = ps.executeUpdate();
 		if (rowCount == 0)
                 {
 			// Throw Exception
                 }
 			//c.commit();
 		ps.close();
 	}
 
 	/**
 	 * Deletes from the SQL table.
 	 * 
 	 * This deletes the HoldRequest object from the HoldRequest table.
 	 */
 	@Override
 	public boolean delete() throws SQLException {
 		ps = c.prepareStatement("DELETE FROM HoldRequest WHERE hid = ?");
 		ps.setInt(1, hid);
 
 		int rowCount = ps.executeUpdate();
 		return rowCount == 1;
 	}
 
 	/**
 	 * Inserts into the SQL table.
 	 * 
 	 * This inserts the HoldRequest object into the HoldRequest table. This
 	 * assumes the item doesn't already exist.
 	 */
 	@Override
 	public boolean insert() throws SQLException {
 		ps = c.prepareStatement("INSERT INTO HoldRequest VALUES (hidCounter.nextVal,?,?,?)");
 
 		ps.setInt(1, borr.getBid());
 		ps.setString(3, b.getCallNumber());
 		ps.setDate(2, new java.sql.Date(issueDate.getTime().getTime()));
 
 		int numRowsChanged = ps.executeUpdate();
 		if (numRowsChanged == 1) {
 			ps.close();
 			ps = c.prepareStatement("SELECT hidCounter.currval FROM DUAL");
 			ResultSet rs = ps.executeQuery();
 			if (rs.next()) {
 				hid = rs.getInt(1);
 				return true;
 			}
 		}
 		return false;
 	}
 
 	/**
 	 * Return all HoldRequests.
 	 * 
 	 * This returns all HoldRequest objects in the SQL database.
 	 * 
 	 * @throws SQLException
 	 * 
 	 */
 	@Override
 	public Collection<Table> getAll() throws SQLException {
 		Collection<Table> holdRequests = new ArrayList<Table>();
                 
 		ps = c.prepareStatement("SELECT * FROM HoldRequest");
 
 		rs = ps.executeQuery();
 
 		while (rs.next()) {/*
 			HoldRequest hr = new HoldRequest();
 			Book b = new Book();
 			Borrower borr = new Borrower();
 
 			b.setCallNumber(rs.getString(3));
 			b = (Book) b.get();
 
 			borr.setBid(rs.getInt(2));
 			borr = (Borrower) borr.get();
 
 			hr.setHid(rs.getInt(1));
 			hr.setB(b);
 			hr.setBorr(borr);
 			hr.getIssueDate().setTime(rs.getDate(4));
 			holdRequests.add(hr);*/
                   holdRequests.add(new HoldRequest(rs));
 		}
 
 		return holdRequests;
 	}
 
 	/**
 	 * Returns the ResultSetMetaData object for the HoldRequest table.
 	 * 
 	 * Returns an object that contains the meta data for the HoldRequest table.
 	 * This is an internal helper method to be used by the display method.
 	 * 
 	 * @return
 	 * @throws SQLException
 	 */
 	public ResultSetMetaData getMeta() throws SQLException {
 		ps = c.prepareStatement("SELECT * FROM HoldRequest");
 
 		rs = ps.executeQuery();
 		return rs.getMetaData();
 	}
 
 	/**
 	 * Return the HoldRequest object corresponding with the set id.
 	 * 
 	 * Given a HoldRequest object with an initialized id field, this returns the
 	 * HoldRequest object with that id field that exists in the SQL database.
 	 * This is used if either the default constructor was called and the
 	 * parameters are required, or if some of the parameters are changed and the
 	 * user wants the original database object.
 	 * 
 	 * @throws SQLException
 	 * 
 	 */
 	@Override
 	public Table get() throws SQLException {
 		if (hid != 0)
                 {
                    HoldRequest tempHR = new HoldRequest(hid);
                    if (tempHR.getHid() > 0)
                    {
                       return (new HoldRequest((Integer) hid));
                    }
 		}
 		return null;
 	}
 
 	/**
 	 * Return all HoldRequest objects from a given Borrower.
 	 * 
 	 * Given a borrower, this returns all the HoldRequests made by that
 	 * borrower.
 	 * 
 	 * @param borr
 	 *            Borrower whose bid is shared with the HoldRequest
 	 * @return ArrayList of HoldRequests
 	 * @throws SQLException
 	 */
 	public Collection<Table> getAll(Borrower borr) throws SQLException {
 		Collection<Table> holdRequests = new ArrayList<Table>();
 		ps = c.prepareStatement("SELECT * FROM HoldRequest WHERE bid = ?");
 		ps.setInt(1, borr.getBid());
 
 		rs = ps.executeQuery();
 
 		while (rs.next()) {
                   /*
 			HoldRequest hr = new HoldRequest();
 			Book b = new Book();
 
 			b.setCallNumber(rs.getString(3));
 			b = (Book) b.get();
 
 			hr.setHid(rs.getInt(1));
 			hr.setBorr(borr);
 			hr.setB(b);
 			hr.getIssueDate().setTime(rs.getDate(4));
                    * 
                    */
                   
 			holdRequests.add(new HoldRequest(rs));
 		}
 
 		return holdRequests;
 	}
 
 	/**
 	 * Return all HoldRequest objects for a given Book.
 	 * 
 	 * Given a book, this returns all the HoldRequests for a particular book.
 	 * 
 	 * @param b
 	 *            Book whose callNo is shared with the HoldRequest
 	 * @return ArrayList of HoldRequests
 	 * @throws SQLException
 	 */
 	public Collection<Table> getAll(Book b) throws SQLException {
 		Collection<Table> holdRequests = new ArrayList<Table>();
 		ps = c.prepareStatement("SELECT * FROM HoldRequest WHERE callNumber = ?");
 		ps.setString(1, b.getCallNumber());
 
 		rs = ps.executeQuery();
 
 		while (rs.next()) {
                   /*
 			HoldRequest hr = new HoldRequest();
 			Borrower borr = new Borrower();
 
 			borr.setBid(rs.getInt(2));
 			borr = (Borrower) borr.get();
 
 			hr.setHid(rs.getInt(1));
 			hr.setBorr(borr);
 			hr.setB(b);
 			hr.getIssueDate().setTime(rs.getDate(4));
                    * 
                    */
                   
 			holdRequests.add(new HoldRequest(rs));
 		}
 
 		return holdRequests;
 	}
 
 	/**
          * This doesn't do what you want it to do, and I don't have time to
          * rewrite it.  Don't use it.
          * 
 	 * Return all HoldRequest objects made by a given Borrower for a given Book.
 	 * 
 	 * Given a borrower and a book, this returns all the HoldRequests for that
 	 * borrower and that book. Technically, this is supposed to return only one
 	 * HoldRequest.
 	 * 
 	 * @param borr
 	 *            Borrower whose bid is shared with the HoldRequest
 	 * @param b
 	 *            Book whose callNo is shared with the HoldRequest
 	 * @return ArrayList of HoldRequests
 	 * @throws SQLException
 	 */
         @Deprecated
 	public Collection<Table> getAll(Borrower borr, Book b) throws SQLException {
 		Collection<Table> holdRequests = new ArrayList<Table>();
 		holdRequests.addAll(getAll(borr));
 		holdRequests.retainAll(getAll(b));
 
 		return holdRequests;
 	}
 
 	/**
 	 * @return the hid
 	 */
 	public Integer getHid() {
 		return hid;
 	}
 
 	/**
 	 * @param hid
 	 *            the hid to set
 	 */
 	public void setHid(Integer hid) {
 		this.hid = hid;
 	}
 
 	/**
 	 * @return the issueDate
 	 */
 	public Calendar getIssueDate() {
 		return issueDate;
 	}
 
 	/**
 	 * @param issueDate
 	 *            the issueDate to set
 	 */
 	public void setIssueDate(Calendar issueDate) {
 		this.issueDate = issueDate;
 	}
 
 	/**
 	 * @return the b
 	 */
 	public Book getB() {
 		return b;
 	}
 
 	/**
 	 * @param b
 	 *            the b to set
 	 */
 	public void setB(Book b) {
 		this.b = b;
 	}
 
 	/**
 	 * @return the borr
 	 */
 	public Borrower getBorr() {
 		return borr;
 	}
 
 	/**
 	 * @param borr
 	 *            the borr to set
 	 */
 	public void setBorr(Borrower borr) {
 		this.borr = borr;
 	}
         
         /**
          * Returns the attributes of the class as a string.
          * @return 
          */
         @Override
         public String toString()
         {
           String holdrequest = "";
           holdrequest += "hid = " + hid
                   + "\ncall number = " + ((b == null) ?
                   null : b.getCallNumber())
                   + "\nbid = " + ((borr == null) ? 
                   null : borr.getBid())
                   + "\nissue date = " + ((issueDate == null) ?
                   null : sdf.format(issueDate.getTime()));
           return holdrequest;
         }
         
         public static void main(String[] args) throws Exception{
     
           HoldRequest hr = new HoldRequest();/*
           // test constructor
           hr = new HoldRequest(100);
           System.out.println(hr);
           // test get
           hr = new HoldRequest();
           hr.setHid(100);
           System.out.println(hr.get());
           
           System.out.println("");
           // test getall
           for (Table a : hr.getAll())
           {
             System.out.println(a);
           }
           // test display
           String[][] hrstr = hr.display();
           for (String[] a : hrstr)
           {
             for (String b : a)
             {
               System.out.print(b + '\t');
             }
             System.out.println();
           }
           // test for update
           Book b = new Book();
           b.setCallNumber("ZI372 C30 1984");
           Borrower borr = new Borrower();
           borr.setBid(1);
           hr.setB(b);
           hr.setBorr(borr);
           hr.setIssueDate(new GregorianCalendar());
           hr.update();
           
           
           HoldRequest hr2 = new HoldRequest();
           hr2.setHid(100);
           hr2 =(HoldRequest) hr2.get();
           System.out.println(hr);
           
           // test for insert
           Book book = new Book();
           book.setCallNumber("KH344 L18 2004");
           Borrower borrower = new Borrower();
           borrower.setBid(2);
           hr.setB(book);
           hr.setBorr(borrower);
           hr.setIssueDate(new GregorianCalendar());
           System.out.println(hr.insert());
           int hid = hr.getHid();
           System.out.println(hid);
           
           HoldRequest hr2 = new HoldRequest(hid);
           System.out.println(hr2);
           
           HoldRequest deleteHR = new HoldRequest();
           for (int i = 101; i <= 103; i++)
           {
             deleteHR.setHid(i);
             System.out.println(deleteHR.delete());
           }
           for (int i = 101; i <= 103; i++)
           {
             deleteHR = new HoldRequest();
             deleteHR.setHid(i);
             deleteHR = (HoldRequest)deleteHR.get();
             System.out.println(deleteHR);
           }
           
           // test getall 
           Book getAllBook = new Book();
           getAllBook.setCallNumber("LP353 N145 1983");
           Collection<Table> hrs = hr.getAll(getAllBook);
           for (Table t : hrs)
           {
             System.out.println(t);
           }
           
           Borrower getAllBorrower = new Borrower();
           getAllBorrower.setBid(142);
           Collection<Table> hrs2 = hr.getAll(getAllBorrower);
           for (Table t : hrs2)
           {
             System.out.println(t);
           }
           
           getAllBorrower.setBid(1);
           Collection<Table> hrs3 = hr.getAll(getAllBorrower,getAllBook);
           */
         }
   
 
 }
