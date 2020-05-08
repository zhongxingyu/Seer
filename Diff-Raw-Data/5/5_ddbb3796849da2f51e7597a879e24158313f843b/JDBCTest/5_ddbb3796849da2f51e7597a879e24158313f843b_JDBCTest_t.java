 import java.sql.*;
 public class JDBCTest 
 {
    public static void main(String[] args) 
    {
        try
        {
           Class.forName("csql.jdbc.JdbcSqlDriver");
           Connection con = DriverManager.getConnection("jdbc:csql", "root", "manager");
           Statement cStmt = con.createStatement();
          cStmt.execute("CREATE TABLE T1 (f1 integer, f2 char (194), primary key(f1));");
           System.out.println("Table t1 created");
           cStmt.close();
           con.commit();
 
           PreparedStatement stmt = null, selStmt= null;
           stmt = con.prepareStatement("INSERT INTO T1 (f1, f2) VALUES (?, ?);");
           int count =0;
           int ret =0;
           stmt.setString(2, "DummyValue");
           long start =0, end =0, curr =0;
           long min =100000, max =0, tot =0;
           for (int i =0 ; i<= 100 ; i++) {
              start = System.nanoTime();
              stmt.setInt(1, i);
              stmt.setString(2, "DummyValue");
              ret = stmt.executeUpdate();
              if (ret != 1) break; //error
              end = System.nanoTime();
              if (i ==0) continue;
              curr = end-start;
              tot = tot + curr;
              if (min > curr) min = curr;
              if (max < curr) max = curr;
              count++;
 
            }
            stmt.close();
            con.commit();
            System.out.print("Insert : " + count );
            System.out.println(" Min:" + min+ " Max: "+max+" Avg: "+ tot/100 );
 
            count=0;
            start=0; end=0; curr=0;
            min=100000; max=0;tot =0;
            selStmt = con.prepareStatement("SELECT * from T1 where f1 = ?;");
            ResultSet rs = null;
            int intVal =0;
            String strVal = null;
            for (int i =0 ; i<= 100 ; i++) {
              start = System.nanoTime();
              selStmt.setInt(1, i);
              rs = selStmt.executeQuery();
              if (rs.next())
              {
                 intVal = rs.getInt(1);
                 strVal = rs.getString(2);
                 ;
              }
              rs.close();
              end = System.nanoTime();
              if (i ==0) continue;
              //System.out.println("Tuple "+ intVal+ " "+ strVal);
              curr = end-start;
              tot = tot + curr;
              if (min > curr) min = curr;
              if (max < curr) max = curr;
              count++;
            }
            selStmt.close();
            con.commit();
            System.out.print("Select : " + count);
            System.out.println(" Min:" + min+ " Max: "+max+" Avg: "+ tot/100 );
 
            count =0;
            start=0; end=0; curr=0;
            min=100000; max=0;tot =0;
            stmt = con.prepareStatement("UPDATE T1 SET f2 = ? WHERE f1 = ?;");
            for (int i =0 ; i<= 100 ; i++) {
              start = System.nanoTime();
              stmt.setInt(2, i);
              stmt.setString(1, "UpdatedValue");
              ret = stmt.executeUpdate();
              if (ret != 1) break; //error
              end = System.nanoTime();
              if (i ==0) continue;
              curr = end-start;
              tot = tot + curr;
              if (min > curr) min = curr;
              if (max < curr) max = curr;
              count++;
            }
            stmt.close();
            con.commit();
            System.out.print("Update : " + count);
            System.out.println(" Min:" + min+ " Max: "+max+" Avg: "+ tot/100 );
        
            count =0;
            start=0; end=0; curr=0;
            min=100000; max=0;tot =0;
            stmt = con.prepareStatement("DELETE FROM T1 WHERE f1 = ?;");
            for (int i =0 ; i<= 100 ; i++) {
              start = System.nanoTime();
              stmt.setInt(1, i);
              ret = stmt.executeUpdate();
              if (ret != 1) break; //error
              end = System.nanoTime();
              if (i ==0) continue;
              curr = end-start;
              tot = tot + curr;
              if (min > curr) min = curr;
              if (max < curr) max = curr;
              count++;
            }
            stmt.close();
            con.commit();
            System.out.print("Delete : " + count);
            System.out.println(" Min:" + min+ " Max: "+max+" Avg: "+ tot/100 );
 
            //cStmt.execute("DROP TABLE T1;");
            System.out.println("Dropped table T1");
            cStmt.close();
 
            con.close();
            }
         catch(Exception e) {
            System.out.println("Exception in Test: "+e);
             e.printStackTrace();
         }
        
     }
 }
 
