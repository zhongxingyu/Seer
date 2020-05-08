 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package productcatalogue.business;
 
 import java.sql.SQLException;
 import productcatalogue.model.CD;
 import productcatalogue.model.Category;
 import java.util.ArrayList;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import javax.jws.WebMethod;
 import javax.jws.WebParam;
 import javax.jws.WebService;
 import javax.naming.NamingException;
 import javax.xml.ws.RequestWrapper;
 import javax.xml.ws.ResponseWrapper;
 import productcatalogue.dbagent.DAO;
 
 /**
  *
  * @author valeh
  */
 
 @WebService(serviceName = "CatalogService")
 public class ProductCatalogueService {
     
      /**
      * Web service operation
      */
     @WebMethod(operationName = "getAllProductList")
     public ArrayList<CD> getAllProductList() {
         
         ArrayList<CD> AllProductList= null;
         
         try{
             DAO dao = new DAO();   
             AllProductList = dao.getAllProductList();
             dao.close();
 
         } catch (NamingException ex) {
             Logger.getLogger(ProductCatalogueService.class.getName()).log(Level.SEVERE, null, ex);
         } catch (SQLException ex) {
             Logger.getLogger(ProductCatalogueService.class.getName()).log(Level.SEVERE, null, ex);
         }
 //        return AllProductList;
       
         return AllProductList;
     }
     
     /**
      * Web service operation
      */
     @WebMethod(operationName = "getProductList")
     public ArrayList<CD> getProductList(@WebParam(name = "categoryId") int categoryId) {
         
         ArrayList<CD> ProductList= null;
         try{
             DAO dao = new DAO();   
             ProductList = dao.getProductList(categoryId);
             dao.close();
 
         } catch (NamingException ex) {
             Logger.getLogger(ProductCatalogueService.class.getName()).log(Level.SEVERE, null, ex);
         } catch (SQLException ex) {
             Logger.getLogger(ProductCatalogueService.class.getName()).log(Level.SEVERE, null, ex);
         }
         return ProductList;
     }
 
     /**
      * Web service operation
      */
     @WebMethod(operationName = "getProductInfo")
     public CD getProductInfo(@WebParam(name = "productId") int productId) {
         
         CD ProductInfo= null;
         
         try{
             DAO dao = new DAO();   
             ProductInfo = dao.getProductInfo(productId);
             dao.close();
 
         } catch (NamingException ex) {
             Logger.getLogger(ProductCatalogueService.class.getName()).log(Level.SEVERE, null, ex);
         } catch (SQLException ex) {
             Logger.getLogger(ProductCatalogueService.class.getName()).log(Level.SEVERE, null, ex);
         }
         return ProductInfo;
     }
 
     /**
      * Web service operation
      */
     @WebMethod(operationName = "getCategoryList")
     public ArrayList<Category> getCategoryList() {
         ArrayList<Category> Category = null;
         
         try{
             DAO dao = new DAO();   
             Category = dao.getCategoryList();
             dao.close();
 
         } catch (NamingException ex) {
               Logger.getLogger(ProductCatalogueService.class.getName()).log(Level.SEVERE, null, ex);
         } catch (SQLException ex) {
               Logger.getLogger(ProductCatalogueService.class.getName()).log(Level.SEVERE, null, ex);
         }
         return Category;
     }
     
     /**
      * Test the web service for the executeSQL and getQueryResult this is not used in the 
      * actual project as a testing mechanism
      */
     
     @WebMethod(operationName = "testexecuteSQL")
     public int testexecuteSQL(String statement) {
         int result = 0;
         try {
             DAO dao = new DAO();
             result = dao.testSQL1(statement);
         } catch (NamingException ex) {
             Logger.getLogger(ProductCatalogueService.class.getName()).log(Level.SEVERE, null, ex);
         } catch (SQLException ex) {
             Logger.getLogger(ProductCatalogueService.class.getName()).log(Level.SEVERE, null, ex);
         }
         return result;
     }
 }
