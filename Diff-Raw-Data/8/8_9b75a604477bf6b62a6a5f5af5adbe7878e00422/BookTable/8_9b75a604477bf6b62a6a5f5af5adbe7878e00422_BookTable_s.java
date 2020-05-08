 import java.io.IOException;
 import java.sql.*;
 import java.util.ArrayList;
 
 	
 
 public class BookTable {
 	
 	private static Connection con;
 		
 	private static final String[] attNames = 
 		{"callNumber", "isbn", "title", "mainAuthor", "publisher", "year"};
 	
 	public ArrayList<ArrayList> searchBook(String titleSearch, String authorSearch, String subjectSearch) throws IllegalArgumentException
 	{
 		try {
 			con = db_helper.connect("ora_i7f7", "a71163091");
 		} catch (SQLException e) {
 			e.printStackTrace();
 		}
 		ArrayList<ArrayList> results = new ArrayList<ArrayList>();
 		ResultSet rs;
 		Statement stmt;
 		try
 		{
 			stmt = con.createStatement();
 			rs = stmt.executeQuery(   "Select * from book b, (SELECT callNumber, count(*) AS qty from ("
 									+ "SELECT * FROM Book b "
 									+ "WHERE b.title LIKE '" + titleSearch
									+ "'UNION ALL" 
 									+ "SELECT * FROM Book b "
 									+ "WHERE b.mainAuthor LIKE '" + authorSearch + "' "
 									+ "OR EXISTS (SELECT * FROM HasAuthor h "
 									+ "WHERE h.callNumber = b.callNumber "
 									+ "AND h.name LIKE '" + authorSearch + "')"
									+ "UNION ALL"
 									+ "SELECT * FROM Book b "
 									+ "WHERE EXISTS (SELECT * FROM HasSubject h WHERE "
 									+ "h.callNumber = b.callNumber "
 									+ "AND h.subject LIKE '" + subjectSearch + "'))"
									+ "GROUP BY callNumber ORDER BY qty desc) c where b.callnumber = c.callnumber");
 			
 			int i = 0;
 			while(rs.next())
 			{
 				String callNumber = rs.getString("callNumber");
 				ResultSet inout;
 				inout = stmt.executeQuery("select count(*) as numin from bookcopy where callnumber = '" + callNumber + "' AND status = 'in'");
 				String numin = inout.getString("numin");
 				inout = stmt.executeQuery("select count(*) as numout from bookcopy where callnumber = '" + callNumber + "' AND status = 'out'");
 				String numout = inout.getString("numout");
 				String title = rs.getString("title");
 				String author = rs.getString("mainAuthor");
 				results.get(i).set(0, callNumber);
 				results.get(i).set(1, title);
 				results.get(i).set(2, author);
 				results.get(i).set(3,numin);
 				results.get(i).set(4,numout);
 				i++;
 			}
 		}catch(SQLException e)
 		{
 			
 			e.printStackTrace();
 			
 		}
 		
 		return results;
 	}
 	
 	public static void insertCopy(String callNumber, boolean firstBook) throws IllegalArgumentException
 	{
 		try {
 			con = db_helper.connect("ora_i7f7", "a71163091");
 		} catch (SQLException e) {
 			e.printStackTrace();
 		}
 		
 		ResultSet rs;
 		Statement stmt;
 		int copyNumber;
 		PreparedStatement  ps;		
 		try
 		{
 			stmt = con.createStatement();
 			rs = stmt.executeQuery("SELECT * FROM ItemCopy WHERE callNumber = " + callNumber);
 			if(rs.getFetchSize() == 0 && !firstBook)
 			{
 				throw new IllegalArgumentException("This call number doesn't exist in the library yet.  If you would like to add it, select 'Add Book'");
 			}
 			copyNumber = rs.getFetchSize() + 1;
 			
 			ps = con.prepareStatement("INSERT INTO BookCopy VALUES (" + callNumber + "," + copyNumber + ",\"in\")");
 			
 			System.out.println(ps);
 			  
 			ps.executeUpdate();
 		
 			// commit work 
 			con.commit();
 		
 			ps.close();
 		}catch(SQLException e)
 		{
 			
 			/* TO BE IMPLEMENTED -- Shouldn't appear, but will allocate a response */
 			
 		}
 	}
 	
 	public static void insertBook(String callNumber, String isbn, String title,
 			String mainAuthor,String publisher,String year, String subject1,
 			String subject2, String subject3, String amount) 
 			throws IllegalArgumentException
 	{
 		ResultSet  rs;
 		PreparedStatement ps;
 		
 		try {
 		
 			con = db_helper.connect("ora_i7f7", "a71163091");
 			
 			ps = con.prepareStatement("INSERT INTO book VALUES (?,?,?,?,?,?)");
 			
 			if ((!((callNumber.matches(".*\\d.*"))&&(!callNumber.matches("^\\d*$"))))||callNumber.equals(""))
 				throw new IllegalArgumentException("Invalid Call Number");
 			else
 				ps.setString(1, callNumber);
 			
 			if ( (isbn.length()!=10) || (!isbn.matches(".*\\d.*")) )
 				throw new IllegalArgumentException("ISBN must be 10 digits");
 			else
 				ps.setString(2, isbn);
 			
 			if (mainAuthor.equals("")||(publisher.equals(""))||(title.equals("")))
 				throw new IllegalArgumentException("Book must have a title, author and publisher");
 			else
 				ps.setString(3, title);
 				ps.setString(4, mainAuthor);
 				ps.setString(5, publisher);
 			
 			if ( (year.length()!=4) || (!year.matches(".*\\d.*")) )
 				throw new IllegalArgumentException("That's not a year, bro");
 			else
 				ps.setString(6, callNumber);
 			
 			ps.executeUpdate();
 			
 			con.commit();
 			
 			
 			try
 			{
 			if (!subject1.equals(""))
 			HasSubjectTable.insertHasSubject(callNumber, subject1);
 			if (!subject2.equals(""))
 			HasSubjectTable.insertHasSubject(callNumber, subject2);
 			if (!subject2.equals(""))
 			HasSubjectTable.insertHasSubject(callNumber, subject3);
 			} catch (SQLException e1)
 				
 	    		{
 				con.rollback();	
 				throw e1;
 	    		} 
 			
 			insertCopy(callNumber, true);
 	    		
 			
 			
 			
 		
 		} catch (SQLException e) {
 			e.printStackTrace();
 			
 			//DO SOMETHING WITH THIS EXCEPTION
 			//unclean
 		}
 		
 	
 
 		
 		
 	}
 
 }
