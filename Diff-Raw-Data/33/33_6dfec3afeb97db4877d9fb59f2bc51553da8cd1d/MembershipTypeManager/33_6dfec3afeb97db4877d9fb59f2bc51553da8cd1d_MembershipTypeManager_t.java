 package BLL;
 
 import BE.MembershipType;
 import DAL.MembershipTypeDBManager;
 import java.sql.SQLException;
 import java.util.ArrayList;
 
 public class MembershipTypeManager
 {
 
     private MembershipTypeDBManager db = null;
     private static MembershipTypeManager instance = null;
     private int memberID;
 
    /*
     * Constructor for the membership type manager class
     */
     private MembershipTypeManager()
     {
         try
         {
             db = new MembershipTypeDBManager();
         }
         catch (Exception e)
         {
             System.out.println("ERROR - " + e.getLocalizedMessage());
         }
     }
 
    /*
     * Conversion of MembershipType manager using a singleton pattern
     */
     public static MembershipTypeManager getInstance()
     {
         if (instance == null)
         {
             instance = new MembershipTypeManager();
         }
         return instance;
     }
 
    /*
     * Connects the database to the gui allowing the gui to add a price to a membership type
     */
     public void addPrice(MembershipType mt) throws Exception
     {
         db.addPrice(mt);
     }
 
    /*
     * Connects the database to the gui allowing the gui to retrieve a list of the names of all the membership types
     */
     public ArrayList getMTName() throws SQLException
     {
         return db.getMTName();
     }
 
    /*
     * Connects the database to the gui allowing the gui to retrieve a membership type specified by the ID
     */
     public MembershipType getMemberByID(int id) throws Exception
     {
         return db.getMemberByID(id);
     }
 
    /*
     * Connects the database to the gui allowing the gui to retrieve the ID of a membership type by checking the name of the membership type
     */
     public int getIdByName(String name) throws SQLException
     {
         return db.getIdByName(name);
     }
 }
