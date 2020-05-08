 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package my.HMM_Model;
 
 /**
  *
  * @author anu
  */
 import java.sql.*;
 import javax.swing.*;
 import java.util.*;
 
 public class DBConnect {
  
        private Connection con;
        private Statement queryStatement;
        private ResultSet resultSet;
        
        public DBConnect(String ip, String port, String password, String username, String db ,JLabel jLabel_ConnectToDBStatus,String connectionName,JComboBox jComboBox_RecentDBList){
            //Boolean connected= false;
            //String connectorStr= "jdbc:mysql://"+ip+":"+port+"/"+db;
            //System.out.println("CONSTR"+connectorStr);
            //System.out.println("IP: "+ip);
            try{
               
                Class.forName("com.mysql.jdbc.Driver");
                String connectorStr= "jdbc:mysql://"+ip+":"+port+"/"+db;
                con = DriverManager.getConnection(connectorStr,username,password);
                //st = con.createStatement();
                //connected=true;
                jComboBox_RecentDBList.addItem(connectionName);
                jLabel_ConnectToDBStatus.setText("Connected");
            }catch(ClassNotFoundException | SQLException ex){
                System.out.println("Error"+ex);
                 jLabel_ConnectToDBStatus.setText("Not Connected");
            }   
            //return connected;
        }
        
        public ResultSet getData(String query){
                
                try{
                //String query = "SELECT gender FROM CF.patient_information";
                
                resultSet = queryStatement.executeQuery(query);
                
                }catch(Exception ex){
                System.out.println("Error"+ex);
            }
          return resultSet;
       }
    
  
        public LinkedHashMap<String, ArrayList<String>> buildTaxonomyTree(DBConnect connect, String db) {
 
         //create the hashmap to store results
         LinkedHashMap<String, ArrayList<String>> taxomonyTree = new LinkedHashMap<>();
         try {
             String query = "SELECT taxonomy_id, kingdom_name, phylum_name, class_name, order_name, family_name, genus_name, species_name "
                     + "FROM " + db + ".Taxonomy";
 
             //setting up a query result set
             ResultSet taxonomyTreeResultset;
 
             //run the query and store into resultset
             taxonomyTreeResultset = connect.getData(query);
 
             //iterate through the resultset and populate the taxomonyTree variable 
             while (taxonomyTreeResultset.next()) {
 
                 //create an arraylist to hold the query output
                 ArrayList taxonomyList = new ArrayList();
 
                 //getting the values from the resultset
                 String taxid = taxonomyTreeResultset.getString("taxonomy_id");
                 String kingdom = taxonomyTreeResultset.getString("kingdom_name");
                 String phylum = taxonomyTreeResultset.getString("phylum_name");
                 String classname = taxonomyTreeResultset.getString("class_name");
                 String order = taxonomyTreeResultset.getString("order_name");
                 String family = taxonomyTreeResultset.getString("family_name");
                 String genus = taxonomyTreeResultset.getString("genus_name");
                 String species = taxonomyTreeResultset.getString("species_name");
                 //System.out.println("Column Name: "+columnName);               
 
                 //adding the data to arraylist
                 taxonomyList.add(kingdom);
                 taxonomyList.add(phylum);
                 taxonomyList.add(classname);
                 taxonomyList.add(order);
                 taxonomyList.add(family);
                 taxonomyList.add(genus);
                 taxonomyList.add(species);
                 //System.out.println("taxid"+taxid);
                 //System.out.println("genus"+genus);
 
                 //adding the arraylist to the linkedhashmap
                 taxomonyTree.put(taxid, taxonomyList);
             }
         } catch (Exception ex) {
             System.out.println("Error" + ex);
         }
         return taxomonyTree;
     }
 
        public ArrayList createSpeciesList(DBConnect connect, String db, String spiecesname) {
         String query = "SELECT organism_name FROM " + db + ".OrganismInfo "
                 + "WHERE species_name='" + spiecesname + "'";
         //setting up query resultset
         ResultSet speciesResultSet;
         //creating the arraylist to store the results
         ArrayList speciesList = new ArrayList();
         try {
             //run the query and store into resultset
             speciesResultSet = connect.getData(query);
             //iterate through the resultset and populate the speciesList variable 
             while (speciesResultSet.next()) {
                 //getting the values from the resultset
                 String organismName = speciesResultSet.getString("organism_name");
                 speciesList.add(organismName);
                 System.out.println("spiecesname" + spiecesname);
                 System.out.println("organismName" + organismName);
             }
         } catch (Exception ex) {
             System.out.println("Error" + ex);
         }
         return speciesList;
     }
     
     
      public void createQueryGroupTable(String db) {
         
         String query = "CREATE TABLE IF NOT EXISTS `"+db+"`.`querygrpinfo` (" +
                        "`querygrp_pk` int(11) NOT NULL AUTO_INCREMENT," +
                        "`querygrpname` varchar(255) NOT NULL," +
                        "`querygrpvalue` varchar(255) NOT NULL," +
                        "  PRIMARY KEY (`querygrp_pk`))";
         //setting up query resultset
              try {
                   //st.executeUpdate(query);
                    
         } catch (Exception ex) {
             JOptionPane.showMessageDialog(null,ex);
         }
     }
 }
