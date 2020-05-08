 import java.io.BufferedWriter;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.io.PrintWriter;
 import java.sql.Connection;
 import java.sql.DriverManager;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.Statement;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Scanner;
 
 final class ResultExtractor {
   final static class Category {
     public final int id;
     public final String name;
 
     public Category(final int id, final String name) {
       this.id = id;
       this.name = name;
     }
   }
 
   public static void main(String[] args) {
     final String conStr = "jdbc:sqlite:" + args[0];
     final List<Category> categories = new ArrayList<Category>();
     final List<String> agegroups = new ArrayList<String>();
     Scanner sc = new Scanner(System.in);
     final String[] fields = sc.nextLine().substring(2).split(",");
     try {Class.forName("org.sqlite.JDBC");}
     catch (final ClassNotFoundException e) {
       throw new RuntimeException(e);
     }
     try {
       final Connection con = DriverManager.getConnection(conStr);
       final Statement st = con.createStatement();
       ResultSet rs = st.executeQuery(sc.nextLine());
       while (rs.next())
         categories.add(new Category(rs.getInt(1), rs.getString(2)));
       rs = st.executeQuery(sc.nextLine());
       while (rs.next()) agegroups.add(rs.getString(1));
       String stmt = sc.nextLine();
       final PreparedStatement pst = con.prepareStatement(stmt);
       stmt = sc.nextLine();
       final PreparedStatement pstAge = con.prepareStatement(stmt);
       for (Category c : categories) {
         String filename = c.name + ".txt";
         try {
           PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(filename)));
           for (int i=0; i<fields.length; ++i) {
             out.print(fields[i]);
             if (i + 1 < fields.length) out.print(',');
           }
           out.println();
           pst.setInt(1, c.id);
           pstAge.setInt(1, c.id);
           rs = pst.executeQuery();
           while (rs.next()) {
             out.print(rs.getString(3));
             for (int i=2; i<=fields.length; ++i) {
               out.print(',');
               out.print(rs.getInt(i+2));
             }
             out.println();
           }
           out.close();
           for (int i=0; i<agegroups.size(); ++i) {
             filename = c.name + agegroups.get(i) + ".txt";
             out = new PrintWriter(new BufferedWriter(new FileWriter(filename)));
             for (int j=0; j<fields.length; ++j) {
               out.print(fields[j]);
               if (j + 1 < fields.length) out.print(',');
             }
             out.println();
             pstAge.setInt(2, i);
             rs = pstAge.executeQuery();
             while (rs.next()) {
              out.print(rs.getString(4));
               for (int j=2; j<=fields.length; ++j) {
                 out.print(',');
                out.print(rs.getInt(j + 3));
               }
               out.println();
             }
             out.close();
           }
         } catch (final IOException e) {
           throw new RuntimeException(e);
         }
       }
     } catch (final SQLException e) {throw new RuntimeException(e);}
   }
 }
