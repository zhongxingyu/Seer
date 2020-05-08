 import java.sql.*;
 import javax.sql.*;
 import java.util.*;
 
 class Insert implements SQLFunction {
     public static String query = "INSERT INTO albums(title,year,rank) values(?,?,?);";
 
     private Map<String,String> emptyValues() {
         Map<String,String> values = new HashMap<String,String>();
         values.put("title","");
         values.put("year","");
         values.put("rank","");
         return values;
     }
 
     private Map<String, String> promptValues() {
         Map<String,String> values = emptyValues();
         Scanner in = new Scanner(System.in);
         System.out.println();
         System.out.println("New Album");
         for( String key : values.keySet() ){
             System.out.print(key + ": ");
             values.put(key, in.nextLine());
         }
 
         System.out.println();
 
         return values;
 
     }
 
     public void doSQL( Connection db ) throws SQLException {
        System.out.println("Insert Start");
        PreparedStatement pstmt = db.prepareStatement( query );
        Map<String,String> values = promptValues();
        pstmt.setString(1,values.get("title"));
        pstmt.setInt(2,Integer.parseInt(values.get("year")));
        pstmt.setInt(3,Integer.parseInt(values.get("rank")));
        pstmt.executeUpdate();
        //pstmt.clearParameters();
        System.out.println("Insert End");

        db.commit();
     }
 
 
 }
 
