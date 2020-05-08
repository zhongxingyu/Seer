 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package cz.muni.fi.pv168;
 
import fi.muni.pv168.Ingredient;
import fi.muni.pv168.IngredientManager;
import fi.muni.pv168.IngredientManagerImpl;
 import fi.muni.pv168.exceptions.ServiceFailureException;
 import fi.muni.pv168.utils.DBUtils;
 import java.io.IOException;
 import java.sql.Connection;
 import java.sql.PreparedStatement;
 import java.sql.SQLException;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import javax.servlet.ServletException;
 import javax.servlet.annotation.WebServlet;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import org.apache.tomcat.dbcp.dbcp.BasicDataSource;
 /**
  *
  * @author mulan
  */
 @WebServlet(RecipebookServlet.URL_MAPPING + "/*")
 public class RecipebookServlet extends HttpServlet {
     private static final String LIST_JSP = "/list.jsp";
     public static final String URL_MAPPING = "/ingredients";
     
     private static final Logger logger = Logger.getLogger(IngredientManagerImpl.class.getName());
     
     private IngredientManager ingredientManager;
 
     public RecipebookServlet() {
         BasicDataSource ds = new BasicDataSource();
         ds.setDriverClassName("org.apache.derby.jdbc.ClientDriver");
         ds.setUrl("jdbc:derby://localhost:1527/Recipebook");
         ds.setUsername("recipebook");
         ds.setPassword("asdfghjk");
         ingredientManager = new IngredientManagerImpl(ds);
        
        String createTableSQL = "CREATE TABLE INGREDIENTS("
                 + "ID BIGINT NOT NULL PRIMARY KEY GENERATED ALWAYS AS IDENTITY, "
                 + "NAME VARCHAR(255), "
                 + "AMOUNT DOUBLE, "
                 + "UNIT VARCHAR(255), "
                 + "RECIPEID INTEGER NOT NULL "
                 + ")";
         Connection con=null;
         PreparedStatement query = null;
         
         try {
             con = ds.getConnection();
             con.setAutoCommit(false);
             query = con.prepareStatement(createTableSQL);
             query.executeUpdate();
             con.commit();
         } catch (SQLException ex) {
             Logger.getLogger(RecipebookServlet.class.getName()).log(Level.INFO, "Already created", ex);
         } finally {
             DBUtils.doRollbackQuietly(con);
             DBUtils.closeQuietly(con, query);
         }
     }
     
     @Override
     protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
         request.setCharacterEncoding("utf-8");
         
         String idedit = request.getParameter("idedit");
         
         //we comunicate with jsp script through idedi so that it knows which row to prepare for editing
         if(idedit!=null){
             try{
                 long eid = Long.parseLong(idedit);
                 request.setAttribute("idedit", eid);
             }catch(NumberFormatException e)
             {
                 request.setAttribute("chyba", "identifikator idedit nie je cislo");
             }
         }
         
         showIngredientList(request, response);
     }
     
     @Override
     protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
         //aby fungovala čestina z formuláře
         request.setCharacterEncoding("utf-8");
         //akce podle přípony v URL
         String action = request.getPathInfo();
         if("/add".equals(action))
         {
             
             String amount = request.getParameter("amount");
             String name = request.getParameter("name");
             String unit = request.getParameter("unit");
             double amnt = 0.0;
        
             if (name == null || name.length() == 0 || amount == null || amount.length() == 0 || unit == null || unit.length() == 0) {
                 request.setAttribute("chyba", "Je nutné vyplniť všetky hodnoty!");
                 showIngredientList(request, response);
                 return;
             }
             
             try{
                 amnt = Double.parseDouble(amount);
             }catch(NumberFormatException e){
                 request.setAttribute("chyba", "Množstvo musí byť čislo !");
                 showIngredientList(request, response);
                 return;
             }
 
             try {
                 Ingredient ingredient = new Ingredient(name, amnt, unit);
                 getIngredientManager().createIngredient(ingredient, 0);
                 logger.log(Level.INFO, "created {}",ingredient);
                 //redirect-after-POST je ochrana před vícenásobným odesláním formuláře
                 response.sendRedirect(request.getContextPath()+URL_MAPPING);
             } catch (ServiceFailureException e) {
                 logger.log(Level.SEVERE, "Cannot add book", e);
                 response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
             }
         } else if("/delete".equals(action))
         {
             Long id;
             
              try {
                 id = Long.valueOf(request.getParameter("id"));
             }catch(NumberFormatException e)
             {
                 request.setAttribute("chyba", "Chybný identifikátor riadku !");
                 showIngredientList(request, response);
                 return;
             }
             
             try {
                     Ingredient toDelete = getIngredientManager().getIngredient(id);
                     getIngredientManager().deleteIngredient(toDelete, 0);
                     
                     logger.log(Level.INFO, "deleted ingredient {}",id);
                     
                     response.sendRedirect(request.getContextPath()+URL_MAPPING);
                     
                 } catch (ServiceFailureException e) {
                     logger.log(Level.SEVERE, "Cannot delete ingredient", e);
                     response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
                 }
         }else if("/edit".equals(action))
         {
             long id=0;
             double amnt=0;
             
             String amount = request.getParameter("amount");
             String name = request.getParameter("name");
             String unit = request.getParameter("unit");
             
             try {
                 id = Long.valueOf(request.getParameter("id"));
             }catch(NumberFormatException e)
             {
                 request.setAttribute("chyba", "Chybný identifikátor riadku !");
                 showIngredientList(request, response);
                 return;
             }
             
             try{
                 amnt = Double.parseDouble(amount);
             }catch(NumberFormatException e){
                 request.setAttribute("chyba", "Množstvo musí byť čislo !");
                 showIngredientList(request, response);
                 return;
             }
             
             try{
                 Ingredient toEdit = getIngredientManager().getIngredient(id);
                 getIngredientManager().updateIngredient(toEdit);
                 
                 toEdit.setName(name);
                 toEdit.setAmount(amnt);
                 toEdit.setUnit(unit);
                 
                 ingredientManager.updateIngredient(toEdit);
 
                 logger.log(Level.INFO, "updated ingredient {0}",id);
 
                 response.sendRedirect(request.getContextPath()+URL_MAPPING);
 
             } catch (ServiceFailureException e) {
                 logger.log(Level.SEVERE, "Cannot delete ingredient", e);
                 response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
             }
         }else{
             logger.log(Level.SEVERE, "Unknown action {0}", action);
                 response.sendError(HttpServletResponse.SC_NOT_FOUND, "Unknown action " + action);
         }
     }
     
     private IngredientManager getIngredientManager() {
         return ingredientManager;
     }
 
     /**
      * Stores the list of books to request attribute "books" and forwards to the JSP to display it.
      */
     private void showIngredientList(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
         try {
             request.setAttribute("ingredients", getIngredientManager().getAllIngredients());
             request.getRequestDispatcher(LIST_JSP).forward(request, response);
         } catch (ServiceFailureException e) {
             logger.log(Level.SEVERE, "Cannot show ingredients", e);
             response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
         }
     }
 }
