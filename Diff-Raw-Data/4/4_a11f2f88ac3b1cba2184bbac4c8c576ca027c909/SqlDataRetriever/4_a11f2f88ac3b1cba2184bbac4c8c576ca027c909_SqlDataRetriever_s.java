 package org.cnio.appform.util.dump;
 
 
 import java.sql.*;
 
 /**
  * This class retrieve the resultset for the interview, patient, section and, 
  * if so, group, by using the java.sql API classes
  * @author bioinfo
  *
  */
 public class SqlDataRetriever {
 	
 	
 	private Connection conn;
 	private Statement stmt;
 	
 /**
  * Retrieves the resultset for the query	
  * @param prjCode, the project code
  * @param intrvId, the database interview id
  * @param grpId, the database groupId
  * @param secOrder, the section order
  * @return a java.sql.ResultSet object with ALL queried rows
  */
 	public ResultSet getResultSet (String prjCode, Integer intrvId, Integer grpId,
 			Integer secOrder) {
 		
 		try {
 			this.conn = this.getConnection ();
 			this.stmt = conn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
 			
 			String secParam = (secOrder == null)?"s.section_order ": secOrder.toString();
 	  	String grpParam = (grpId == null? "1=1 ": "g.idgroup = "+grpId);
 	  	
 	     String sqlqry = "select p.codpatient, g.name as grpname, "+
 	        "i.name as intrvname, s.name as secname, "+
 	      "q.codquestion as codq, a.thevalue, s.section_order, "+
 	      "it.item_order, pga.answer_order, pga.answer_number, it.\"repeatable\" as itrep "+
 	      "from patient p, pat_gives_answer2ques pga, appgroup g,	performance pf, "+
 	        "question q, answer a, interview i, item it, section s, project pj "+
 	      "where "+ grpParam +
 	      " and i.idinterview = "+intrvId +
 	      " and pj.project_code = '"+prjCode+"' " +
 	      "and pj.idprj = i.codprj "+
 	      "and pf.codinterview = i.idinterview "+
 	      "and pf.codgroup = g.idgroup "+
 	      "and s.codinterview = i.idinterview "+
 	      "and pf.codpat = p.idpat "+
 	      "and pga.codpat = p.idpat "+
 	      "and pga.codquestion = q.idquestion "+
 	      "and pga.codanswer = a.idanswer "+
 	      "and q.idquestion = it.iditem "+
 	      "and it.idsection = s.idsection " +
 	      "and s.section_order = " + secParam +
 	      " order by 1, 7, 10, 8, 5, 9";
       
 <<<<<<< HEAD
 =======
 System.out.println ("\nSqlDataRetriever => ResultSet query:\n"+sqlqry);      
 >>>>>>> develop
       ResultSet rs = stmt.executeQuery(sqlqry);
       
       return rs;
 		}
 		catch (ClassNotFoundException cnfe) {
 	    System.out.println("Couldn't find the driver!");
 	    System.out.println("Let's print a stack trace, and exit.");
 	    cnfe.printStackTrace();
 	    
 	    return null;
 	  }
 		catch (SQLException sqlEx) {
 			System.out.println ("Err getting conncection or querying...");
 			sqlEx.printStackTrace();
 			
 			return null;
 		}
 	}
 
 	
 	
 	
 	public void closeConn () throws SQLException {
 		this.stmt.close();
 		this.conn.close();
 	}
 	
 	
 	
 	
 	public void printResultsetOut (ResultSet rs) {
 		String msgRow;
 		int counter = 120000;
 		
 		try {
 			if (counter > 0)
 				rs.absolute(counter);
 			
 			while (rs.next() && counter < 120500) {
 				msgRow = rs.getString(1)+" => "+rs.getString(5)+"-"+rs.getInt(10);
 				msgRow += "-"+rs.getInt(9)+": "+rs.getString(6);
 				
 	      if (rs.wasNull()) 
 	        msgRow += "...theValue is null";
 	      
 	      System.out.println (msgRow);
 	      System.out.println("---------------");
 	      counter++;
 		  }
 //		  rs.close();
 		}
 		catch (SQLException sqlEx) {
 			sqlEx.printStackTrace();
 		}
 		
 	}
 	
 	
 	
 	
 	private Connection getConnection() throws ClassNotFoundException, SQLException {
     Class.forName("org.postgresql.Driver");
     String url = "jdbc:postgresql://padme:5432/appform";
 
     return DriverManager.getConnection(url, "gcomesana", "appform");
   }
 
 }
