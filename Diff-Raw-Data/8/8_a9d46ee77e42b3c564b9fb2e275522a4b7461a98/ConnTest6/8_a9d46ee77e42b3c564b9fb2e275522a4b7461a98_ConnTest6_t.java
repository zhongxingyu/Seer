 //Test DML statement with rollback
 import java.sql.*;
 public class ConnTest6
 {
     public static void main(String[] args) 
     {
        try
        {
            Class.forName("csql.jdbc.JdbcSqlDriver");
            Connection con = DriverManager.getConnection("jdbc:csql", "root", "manager");
 	   Statement cStmt = con.createStatement();
           con.setAutoCommit(false);
 	   int ret =0;
            cStmt.execute("CREATE TABLE T1 (f1 integer, f2 char (20));");
 		   
 	   PreparedStatement stmt = null;
            stmt = con.prepareStatement("INSERT INTO T1 VALUES (?, ?);");
            for (int i =0 ; i< 10 ; i++) {
              stmt.setInt(1, i);
              stmt.setString(2, String.valueOf(i+100));
              ret = stmt.executeUpdate();
              if (ret !=1) break;
            }
            stmt.close();
            con.commit();
 
            cStmt.execute("UPDATE T1 set f2='2' where f1 = 2;");
 	   ret = cStmt.executeUpdate("INSERT INTO T1 values (100, '100');");
            if (ret != 1) System.out.println("Insert error");
            ret = cStmt.executeUpdate("DELETE FROM T1 WHERE f1 = 4;");
            if (ret != 1) System.out.println("Delete error");
            con.rollback();
 
            System.out.println("After rollback, listing tuples:");
 	   ResultSet rs = cStmt.executeQuery("SELECT * from T1;");
 	   while (rs.next())
 	   {
 	       System.out.println("Tuple value is " + rs.getInt(1)+ " "+ rs.getString(2));
 	   }
 	   rs.close();
 	   con.commit();
           cStmt.executeUpdate("DROP TABLE T1;");
 
            con.close();
         }catch(Exception e) {
             System.out.println("Exception in Test: "+e);
             e.printStackTrace();
         }
     }
 }
