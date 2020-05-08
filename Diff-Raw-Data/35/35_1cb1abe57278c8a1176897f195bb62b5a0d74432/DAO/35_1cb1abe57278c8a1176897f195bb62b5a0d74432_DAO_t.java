 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package productcatalogue.dbagent;
 
 import javax.naming.Context;
 import java.sql.ResultSet;
 import java.sql.PreparedStatement;
 import java.sql.SQLException;
 import java.sql.Connection;
 import java.sql.Statement;
 import java.util.ArrayList;
 import java.util.Hashtable;
 import java.util.List;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import javax.sql.DataSource;
 import javax.naming.InitialContext;
 import javax.naming.NamingException;
 import productcatalogue.model.CD;
 import productcatalogue.model.Category;
 
 /**
  *
  * @author PRISCILLA
  */
 public class DAO {
     
     private Connection connection;
     private PreparedStatement pstatement;
     private Hashtable<Integer,String> table;
     
     
     public DAO() throws NamingException, SQLException
     {
         InitialContext initialContext = new InitialContext();
         Context context = (Context) initialContext.lookup("java:comp/env");
         DataSource ds = (DataSource) context.lookup("cdstore_db");
         connection = ds.getConnection();
         
         
     }
     
     /**
      *creates an ArrayList to get the list of CD categories
      * @return CategoryList
      */
     public ArrayList<Category> getCategoryList()
     {
         ArrayList<Category> CategoryList = new ArrayList<Category>();
         try {
             // reads the xml file that contains the sql queries i.e. ProductCatalogueQueries using ProductCatalogueDOMReader by calling the function readXML defined below and prepares the statement
             readXML(1);
             pstatement = setSQL(1);
             ResultSet set = getQueryResult(pstatement);
             Category temp;
             while(set.next())
             {
                 //gets the list from category and adds it to the arraylist i.e CategoryList
                 temp = new Category();
                 temp.setId(set.getInt("idCa"));
                 temp.setTitle(set.getString("title"));
                 temp.setDescription(set.getString("description"));
                 CategoryList.add(temp);
                 
             }
             
         } catch (SQLException ex) {
             Logger.getLogger(DAO.class.getName()).log(Level.SEVERE, null, ex);
         } catch (Exception ex) {
             Logger.getLogger(DAO.class.getName()).log(Level.SEVERE, null, ex);
         }
         
         return CategoryList;
         
     }
     
    /**
     * Method to get a ArratList of CD based off of a common category ID
     * @param categoryId
     * @return 
     */
     public ArrayList<CD> getProductList(int categoryId)
     {
         ArrayList<CD> ProductList = new ArrayList<CD> ();
         Category Category = null;
         try {
             // reads the xml file that contains the sql queries i.e. ProductCatalogueQueries using ProductCatalogueDOMReader by calling the function readXML defined below and prepares the statement
             readXML(2);
             
             CD CDProductList;
             Category cat = new Category();
             ResultSet set = null;
             
             pstatement = setSQL(2);
             pstatement.setInt(1, categoryId);
             set = getQueryResult(pstatement);
             
             cat.setId(categoryId);
             if(set.next()){
                 cat.setTitle(set.getString("title"));
                 cat.setDescription(set.getString("description"));
             }
             
             pstatement = setSQL(1);
             pstatement.setInt(1, categoryId);
             set = getQueryResult(pstatement);
             
             while(set.next())
             {
                 //gets the list from of CD products from CD and adds it to the arraylist i.e CDProductList
                 CDProductList = new CD();
                 CDProductList.setTitle(set.getString("title"));
                 CDProductList.setAuthor(set.getString("Artist"));
                 CDProductList.setDescription(set.getString("description"));
                 CDProductList.setId(set.getInt("idCD"));
                 CDProductList.setPrice(set.getFloat("price"));
                 
                 CDProductList.setCategory(cat);
                 ProductList.add(CDProductList);
                 
             }
             
         } catch (SQLException ex) {
             Logger.getLogger(DAO.class.getName()).log(Level.SEVERE, null, ex);
         } catch (Exception ex) {
             Logger.getLogger(DAO.class.getName()).log(Level.SEVERE, null, ex);
         }
         
         return ProductList;
         
     }
     
     
     /**
      * Retreives all of the CD's and stores the category within it
      * @return 
      */
     public ArrayList<CD> getAllProductList()
     {
         ArrayList<CD> AllProductList = new ArrayList<CD> ();
         try {
             //reads the xml file that contains the sql queries i.e. ProductCatalogueQueries using ProductCatalogueDOMReader by calling the function readXML defined below and prepares the statement
             readXML(4);
             pstatement = setSQL(1);
             ResultSet set = getQueryResult(pstatement);
             CD temp = null;
             
             while(set.next())
             {
                 temp = new CD();
                 //gets the list from of CD products from CD and adds it to the arraylist i.e CDProductList
                 temp.setTitle(set.getString("title"));
                 temp.setAuthor(set.getString("Artist"));
                 temp.setId(set.getInt("idCD"));
                 temp.setDescription(set.getString("description"));
                 temp.setPrice(set.getFloat("price"));
                 temp.setCategory(getCat(set.getInt("idCa")));
                         
                 AllProductList.add(temp);
                
             }
             
         } catch (SQLException ex) {
             Logger.getLogger(DAO.class.getName()).log(Level.SEVERE, null, ex);
         } catch (Exception ex) {
             Logger.getLogger(DAO.class.getName()).log(Level.SEVERE, null, ex);
         }
         
         return AllProductList;
         
     }
     
     /**
      * Get a specific CD based off of an ID of a CD
      * @param idCD
      * @return 
      */
     public CD getProductInfo(int idCD)
     {
         CD ProductInfo = null;
         Category Category = null;
         try {
             // reads the xml file that contains the sql queries i.e. ProductCatalogueQueries using ProductCatalogueDOMReader by calling the function readXML defined below and prepares the statement
             readXML(3);
             pstatement = setSQL(1);
             pstatement.setInt(1, idCD);
             ResultSet set = getQueryResult(pstatement);
             if(set.next())
             {
                 //gets the list of CD products from CD and adds it to the arraylist i.e CDProductList
                 ProductInfo= new CD();
                 ProductInfo.setId(set.getInt("idCD"));
                 ProductInfo.setTitle(set.getString("title"));
                 ProductInfo.setPrice(set.getFloat("price"));
                 ProductInfo.setAuthor(set.getString("Artist"));
                 ProductInfo.setDescription(set.getString("description"));
                 
                 //prepare the statement and get the information of idCa which is a foreign key in CD table
                 pstatement = setSQL(2);
                 pstatement.setInt(1 , Integer.parseInt(set.getString("idCa")));
                 set = getQueryResult(pstatement);
                 
                 if(set.next())
                 {
                     Category= new Category();
                     Category.setId(set.getInt("idCa"));
                     Category.setTitle(set.getString("title"));
                     Category.setDescription(set.getString("description"));
                 }
                 if(Category != null)
                     ProductInfo.setCategory(Category);
                 
             }
             
         } catch (SQLException ex) {
             Logger.getLogger(DAO.class.getName()).log(Level.SEVERE, null, ex);
         } catch (Exception ex) {
             Logger.getLogger(DAO.class.getName()).log(Level.SEVERE, null, ex);
         }
         
         return ProductInfo;
         
         
     }
     
     /**
      * Retrieves a category information based off of a category ID
      * @param id
      * @return 
      */
     private Category getCat(int id)
     {
         Category cat = null;
         try {
             pstatement = setSQL(2);
             pstatement.setInt(1, id);
             ResultSet set = getQueryResult(pstatement);
             if(set.next())
             {
                 cat = new Category();
                 cat.setId(set.getInt("idCa"));
                 cat.setTitle(set.getString("title"));
                 cat.setDescription(set.getString("description"));
             }
            
         } catch (SQLException ex) {
             Logger.getLogger(DAO.class.getName()).log(Level.SEVERE, null, ex);
         }
         return cat;
     }
     
     //method to close the connection
     public void close()
     {
         try {
             connection.close();
         } catch (SQLException ex) {
             Logger.getLogger(DAO.class.getName()).log(Level.SEVERE, null, ex);
         }
     }
     
     /**
      * Reads the XML file and gets the appropriate queries for the task
      * @param i 
      */
     private void readXML(int i)
     {
         DOMReader dom = new DOMReader();
         switch(i)
         {
             case 1 :
                 table = dom.Read("gCategory");
                 break;
             case 2 :
                 table = dom.Read("gProductList");
                 break;
             case 3:
                 table = dom.Read("gProductInfo");
                 break;
             case 4:
                 table = dom.Read("gAllProductList");
                 break;
             default :
                 break;
         }
         
         
     }
     
     /**
      * Returns a preparedStatement with the appropriate ID setup
      * @param id
      * @return 
      */
     private PreparedStatement setSQL(Integer id)
     {
         PreparedStatement s = null;
         try {
             s = connection.prepareStatement(table.get(id));
         } catch (SQLException ex) {
             Logger.getLogger(DAO.class.getName()).log(Level.SEVERE, null, ex);
         }
         return s;
     }
     
     /**
      * Returns the number of rows affected by the preparedStatement
      * @param pstatement
      * @return 
      */
     private int executeSQL(PreparedStatement pstatement)
     {
         int num = 0;
         try {
             connection.setAutoCommit(false);
             num = pstatement.executeUpdate();
             connection.commit();
         } catch (SQLException ex) {
             try {
                 connection.rollback();
             } catch (SQLException ex1) {
                 Logger.getLogger(DAO.class.getName()).log(Level.SEVERE, null, ex1);
             }
             Logger.getLogger(DAO.class.getName()).log(Level.SEVERE, null, ex);
         }
         
         return num;
     }
     
     /**
      * Returns a ResultSet to which contains the necessary results to continue the computation
      * @param pstatement
      * @return 
      */
     private ResultSet getQueryResult(PreparedStatement pstatement)
     {
         ResultSet set = null;
         try {
             connection.setAutoCommit(false);
             set = pstatement.executeQuery();
             connection.commit();
         } catch (SQLException ex) {
             Logger.getLogger(DAO.class.getName()).log(Level.SEVERE, null, ex);
             try {
                 connection.rollback();
             } catch (SQLException ex1) {
                 Logger.getLogger(DAO.class.getName()).log(Level.SEVERE, null, ex1);
             }
         }
         return set;
     }
     public int testSQL1(String statement)
     {
         int result = 0;
         try {
             PreparedStatement stat = connection.prepareStatement(statement);
             stat.setString(1, "Alternative Rock");
            stat.setString(2, "This is good music");
             
             result = executeSQL(stat);
             
         } catch (SQLException ex) {
             Logger.getLogger(DAO.class.getName()).log(Level.SEVERE, null, ex);
         }
         return result;
     }
    
    public void testSQL2(String statement)
    {
        try {
            PreparedStatement stat = connection.prepareStatement(statement);
            ResultSet set = getQueryResult(stat);
            while(set.next())
            {
                System.out.println("The category ID is " + set.getInt("idCa") + " The category title is " + set.getString("title") + " The category description is " + set.getString("description"));
            }
        } catch (SQLException ex) {
            Logger.getLogger(DAO.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
 }
