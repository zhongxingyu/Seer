 import java.sql.*;
 import java.util.Scanner;
  
 public class JDBCtask{
   public static void main(String[] args) {
  System.out.println("Table Creation Example!");
  Connection con = null;
  String url = "jdbc:mysql://localhost:3306/";
  String dbName = "jdbctutorial";
  String driverName = "com.mysql.jdbc.Driver";
  String userName = "root";
  String password = "root";
  PreparedStatement preparedstatement=null;
  ResultSet rs;
  try{
   Class.forName(driverName).newInstance();
   con = DriverManager.getConnection(url, userName, password);
  //con = DriverManager.getConnection(url+dbName, userName, password);
  try{
   Statement st = con.createStatement();
 
   String database =
 		  "CREATE DATABASE "+dbName;
 		  st.executeUpdate(database);
 		  System.out.println("Table creation process successfully!");
 
   String table =
  "CREATE TABLE Film(year integer,film String,Director String,Country String)";
   st.executeUpdate(table);
   System.out.println("Table creation process successfully!");
   }
   catch(SQLException s){
   System.out.println("Table allready exists!");
   }
  preparedstatement = con
          .prepareStatement("insert into  Film values (?,?,?,?)");
  preparedstatement.setLong(1, 2010);
  preparedstatement.setString(2,"Barney's Version" );
  preparedstatement.setString(3,"Richard J. Lewis");
  preparedstatement.setString(4,"Canada");
 int x=0;
 for (;x!=10;){
 Scanner Sc=new Scanner(System.in);
 x=Sc.nextInt();
 if (x==1){
 	Statement stmt = con.createStatement();
 	        
rs = stmt.executeQuery("SELECT film FROM Film WHERE Year = 2010");		//      - No database selected
																								//       SELECT
 while ( rs.next() ) {
             System.out.println(rs.getString("film"));
 }
 }
 /*if(x==2){
 	Scanner Sc1=new Scanner(System.in);
 	int y=Sc1.nextInt();
 	Statement stmt1 = con.createStatement();
	rs = stmt1.executeQuery("Select FROM Film WHERE Director =",y);      // error! -    , -
 }*/
 
 
   con.close();
   }
  }
   catch (Exception e){
   e.printStackTrace();
   }
   
   }
 }
