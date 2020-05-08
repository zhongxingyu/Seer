/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
 package BLL;
 
 import BE.MembershipType;
 import DAL.MembershipTypeDBManager;
 import java.sql.SQLException;
 import java.util.ArrayList;
 
/**
 *
 * @author Rasmus
 */
 public class MembershipTypeManager
 {
 
     private MembershipTypeDBManager db = null;
     private static MembershipTypeManager instance = null;
     private int memberID;
 
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
 
     public static MembershipTypeManager getInstance()
     {
         if (instance == null)
         {
             instance = new MembershipTypeManager();
         }
         return instance;
     }
 
     public void addPrice(MembershipType mt) throws Exception
     {
         db.addPrice(mt);
     }
 
     public ArrayList getMTName() throws SQLException
     {
         return db.getMTName();
     }
 
     public MembershipType getMemberByID(int id) throws Exception
     {
         return db.getMemberByID(id);
     }
 
     public int getIdByName(String name) throws SQLException
     {
         return db.getIdByName(name);
     }
 }
