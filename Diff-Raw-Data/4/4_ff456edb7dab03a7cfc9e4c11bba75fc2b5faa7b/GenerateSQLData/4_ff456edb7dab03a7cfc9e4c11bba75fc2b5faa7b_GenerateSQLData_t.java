 import java.sql.Connection;
 import java.sql.DriverManager;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.PreparedStatement;
 
 import java.util.Date;
 import java.util.Random;
 
 import java.text.SimpleDateFormat;
 
 
 /**
  * Generate some random SQL data.
  */
 public class GenerateSQLData {
    private static final String DB_URL = "jdbc:mysql://localhost:3306/test?autoReconnect=true";
    private static final String DB_USER = "";
    private static final String DB_PASS = "";
 
    private static int numGeneNames = 100;
    private static int numContigs = 100;
    private static int numCollabExons = 2500;
    private static int numCollabAnnotations = 500;
    private static int numGroups = 100;
    private static int numUsers = 100000;
    private static int numExons = 5000000;
    private static int numAnnotations = 1000000;
    private static double groupJoinChance = 0.1;
    private static double taskAssignChance = 0.05;
    private static Random rand;
 
    public static void main(String[] args) throws Exception {
       Connection conn = null;
 
       rand = new Random();
 
       try {
          // Instantiate the DB Driver
          Class.forName("com.mysql.jdbc.Driver");
 
          conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
       } catch (Exception ex) {         
          System.err.println("Cannot connect to DB:" + ex);
       }
       
       System.out.println("Starting to generate stuff...\n");
       createGeneNames(numGeneNames, conn);
       System.out.println(" Finished generating " + numGeneNames + " Gene names");
       createContigs(numContigs, conn);
       System.out.println(" Finished generating " + numContigs + " Contigs");
       createCollabExons(numCollabExons, conn);
       System.out.println(" Finished generating " + numCollabExons + " Collaborative Exons");
       createCollabAnnotations(numCollabAnnotations, conn);
       System.out.println(" Finished generating " + numCollabAnnotations + " Collaborative Annotations");
       createGroups(numGroups, conn);
       System.out.println(" Finished generating " + numGroups + " Groups");
       createUsers(numUsers, conn);
       System.out.println(" Finished generating " + numUsers + " Users");
       createExons(numExons, conn);
       System.out.println(" Finished generating " + numExons + " Exons");
       createAnnotations(numAnnotations, conn);
       System.out.println(" Finished generating " + numAnnotations + " Annotations");
       createGroupMembership(groupJoinChance, conn);
       System.out.println(" Finished generating group memberships with a join chance of " 
                          + groupJoinChance);
       createTasks(taskAssignChance, conn);
       System.out.println(" Finished generating tasks with a chance of " 
                          + taskAssignChance);
       System.out.println("\nDone!");
    }
 
    private static void createGeneNames(int count, Connection conn) throws Exception {
       String insertQuery = "INSERT INTO GeneNames (Name) VALUES (?)";
       PreparedStatement pstmt = conn.prepareStatement(insertQuery);
 
       for (int i = 0; i < count; i++) {
          pstmt.setString(1, "gene"+(i+1));
          pstmt.executeUpdate();
       }
       pstmt.close();
    }
 
    private static void createContigs(int count, Connection conn) throws Exception {
       String insertQuery = "INSERT INTO Contigs (Name, Difficulty, Sequence, UploaderId, Source, Species, Status, CreateDate) VALUES (?,?,?,?,?,?,?,?)";
       PreparedStatement pstmt = conn.prepareStatement(insertQuery);
 
       for (int i = 0; i < count; i++) {
          pstmt.setString(1, "contig" + (i+1));
          pstmt.setString(2, randomDouble());
          pstmt.setString(3, randomSequence(50000));//SEQUENCE
          pstmt.setString(4, String.valueOf(rand.nextInt(numUsers) + 1));
          pstmt.setString(5, randomString(15));
          pstmt.setString(6, randomString(40));
          pstmt.setString(7, randomString(20));
          pstmt.setString(8, randomDate());
          pstmt.executeUpdate();
       }
 
       pstmt.close();
    }
 
    private static void createCollabExons(int count, Connection conn) throws Exception {
       String insertQuery = "INSERT INTO CollabExons (StartPos, EndPos, CollabAnnotationId) VALUES (?,?,?)";
       PreparedStatement pstmt = conn.prepareStatement(insertQuery);
 
       for (int x = 0; x < count / 5; x++) {
          for (int i = 0; i < 5; i++) {
             pstmt.setString(1, randomInt(5000));//StartPos
             pstmt.setString(2, randomInt(5000));//EndPos
             pstmt.setString(3, String.valueOf(x));
             pstmt.executeUpdate();
          }
       }
       pstmt.close();
    }
 
    private static void createCollabAnnotations(int count, Connection conn) throws Exception {
       String insertQuery = "INSERT INTO CollabAnnotations (GeneId, StartPos, EndPos, ReverseComplement, ContigId, CreateDate, LastModifiedDate) VALUES (?,?,?,?,?,?,?)";
       PreparedStatement pstmt = conn.prepareStatement(insertQuery);
 
       for (int i = 0; i < count / 5; i++) {
          for(int x = 0; x < 5; x++) {
             pstmt.setString(1, String.valueOf(i+x));
             pstmt.setString(2, randomInt(5000));
             pstmt.setString(3, randomInt(5000));
             pstmt.setString(4, randomBool());
             pstmt.setString(5, String.valueOf(i));
             pstmt.setString(6, randomDate());
             pstmt.setString(7, randomDate());
             pstmt.executeUpdate();
          }
       }
 
       pstmt.close();
    }
 
    private static void createGroups(int count, Connection conn) throws Exception {
       String insertQuery = "INSERT INTO Groups (Name, GroupDescription, CreateDate) VALUES (?,?,?)";
       PreparedStatement pstmt = conn.prepareStatement(insertQuery);
 
       for (int i = 0; i < count; i++) {
          pstmt.setString(1, "group" + (i+1));
          pstmt.setString(2, randomString(150));
          pstmt.setString(3, randomDate());
          pstmt.executeUpdate();
       }
 
       pstmt.close();
    }
 
    private static void createUsers(int count, Connection conn) throws Exception {
       String insertQuery = "INSERT INTO Users (FirstName, LastName, UserName, Email, Pass, Salt, LastLoginDate, RegistrationDate, Level, Role, Exp) VALUES (?,?,?,?,?,?,?,?,?,?,?)";
       PreparedStatement pstmt = conn.prepareStatement(insertQuery);
 
       for (int i = 0; i < count; i++) {
          pstmt.setString(1, randomString(20));
          pstmt.setString(2, randomString(25));
          pstmt.setString(3, randomString(30));
          pstmt.setString(4, randomString(40));
          pstmt.setString(5, randomString(64));
          pstmt.setString(6, randomString(32));
          pstmt.setString(7, randomDate());
          pstmt.setString(8, randomDate());
          pstmt.setString(9, randomInt(5));
          pstmt.setString(10, randomString(5));
          pstmt.setString(11, randomInt(7000));
          pstmt.executeUpdate();
       }
 
       pstmt.close();
    }
   
    private static void createExons(int count, Connection conn) throws Exception {
       String insertQuery = "INSERT INTO Exons (StartPos, EndPos, AnnotationId) VALUES (?,?,?)";
       PreparedStatement pstmt = conn.prepareStatement(insertQuery);
 
       for (int x = 0; x < count / 5; x++) {
          for (int i = 0; i < 5; i++) {
             pstmt.setString(1, randomInt(5000));//StartPos
             pstmt.setString(2, randomInt(5000));//EndPos
             pstmt.setString(3, String.valueOf(x));
             pstmt.executeUpdate();
          }
       }
       pstmt.close();
    }
 
    private static void createAnnotations(int count, Connection conn) throws Exception {
       String insertQuery = "INSERT INTO Annotations (GeneId, StartPos, EndPos, ReverseComplement, PartialSubmission, ExpertSubmission, ContigId, UserId, CreateDate, LastModifiedDate, FinishedDate, Incorrect, ExpertIncorrect, ExpGained) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
       PreparedStatement pstmt = conn.prepareStatement(insertQuery);
 
       for (int i = 0; i < count; i++) {
          pstmt.setString(1, String.valueOf(randomInt(numGeneNames)+1));
          pstmt.setString(2, randomInt(7000));
          pstmt.setString(3, randomInt(7000));
          pstmt.setString(4, randomBool());
          pstmt.setString(5, randomBool());
          pstmt.setString(6, randomBool());
         pstmt.setString(7, String.valueOf(rand.nextInt(numContigs)+1));
         pstmt.setString(8, String.valueOf(rand.nextInt(numUsers)+1));
          pstmt.setString(9, randomDate());
          pstmt.setString(10, randomDate());
          pstmt.setString(11, randomDate());
          pstmt.setString(12, randomBool());
          pstmt.setString(13, randomBool());
          pstmt.setString(14, randomInt(100));
          pstmt.executeUpdate();
       }
 
       pstmt.close();
    }
 
    private static void createGroupMembership(double chance, Connection conn) throws Exception {
       String insertQuery = "INSERT INTO GroupMembership (GroupId, UserId) VALUES (?,?)";
       PreparedStatement pstmt = conn.prepareStatement(insertQuery);
 
       for (int x = 0; x < numUsers; x++) {
          for (int i = 0; i < numGroups; i++) {
             double test = rand.nextDouble();
             if(test < chance) {
                pstmt.setString(1, String.valueOf(i+1));
                pstmt.setString(2, String.valueOf(x+1));
                pstmt.executeUpdate();
             }
          }
       }
       pstmt.close();
    
    }
 
    private static void createTasks(double chance, Connection conn) throws Exception {
       String insertQuery = "INSERT INTO Tasks (UserId, ContigId, Description, EndDate)" +
                            "VALUES (?,?,?,?)";
       PreparedStatement pstmt = conn.prepareStatement(insertQuery);
 
       for (int x = 0; x < numUsers; x++) {
          for (int i = 0; i < numContigs; i++) {
             double test = rand.nextDouble();
             if(test < chance) {
                pstmt.setString(1, String.valueOf(x+1));
                pstmt.setString(2, String.valueOf(i+1));
                pstmt.setString(3, randomString(60));
                pstmt.setString(4, randomDate());
                pstmt.executeUpdate();
             }
          }
       }
 
       pstmt.close();
    }
 
    private static String randomDate() {
       SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
       return formatter.format(new Date());
    }
 
    private static String randomDouble() {
       return String.valueOf(rand.nextDouble());
    }
 
    private static String randomInt(int end) {
       return String.valueOf(rand.nextInt(end));
    }
 
    private static String randomSequence(int length) {
       String choices = "CGAT";
       StringBuilder stringb = new StringBuilder(length);
       for (int i = 0; i < length; i++) {
          stringb.append(choices.charAt(rand.nextInt(choices.length())));
       }
       return stringb.toString();
    }
 
    private static String randomString(int length) {
       String choices = "0123456789ABCDEFGHIJKLMOPQRSTUVXYZ";
       StringBuilder stringb = new StringBuilder(length);
       for (int i = 0; i < length; i++) {
          stringb.append(choices.charAt(rand.nextInt(choices.length())));
       }
       return stringb.toString();
    }
 
    private static String randomBool() {
       int testerguything = rand.nextInt();
       return String.valueOf(Math.abs(testerguything % 2));
    }
     
 }
