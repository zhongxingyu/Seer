/*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package UserInterface;
 
 import com.google.gson.Gson;
 import java.io.IOException;
 import java.io.PrintWriter;
 import java.sql.Connection;
 import java.sql.DriverManager;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.util.ArrayList;
 import java.util.List;
import javax.servlet.RequestDispatcher;
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 /**
  *
  * @author Joseph
  */
 public class DBInfo extends HttpServlet
 {
     protected void processRequest(HttpServletRequest request, HttpServletResponse response)
             throws ServletException, IOException
     {
         response.setContentType("application/json;charset=UTF-8");
         PrintWriter out = response.getWriter();
         try
         {
             String dbName = (String) request.getSession().getAttribute("dbname");
             String dbAddr = (String) request.getSession().getAttribute("addr");
             String dbUser = (String) request.getSession().getAttribute("user");
             String dbPW = (String) request.getSession().getAttribute("pw");
 
             Connection conn = null;
             try
             {
                 String userConnect = "jdbc:mysql://"
                         + dbAddr + "/" + dbName + "?user=" + dbUser
                         + "&password=" + dbPW;
 
                 Class.forName("com.mysql.jdbc.Driver").newInstance();
                 conn = DriverManager.getConnection(userConnect);
 
                 java.sql.PreparedStatement statement = conn.prepareStatement(
                         "select table_name, table_type from information_schema.tables "
                         + "where table_schema = ?");
                 statement.setString(1, "world");
 
                 ResultSet rs = statement.executeQuery();
 
                 int count = 0;
                 List<TreeData> tree = new ArrayList<TreeData>();
                 TreeData tablesRoot = new TreeData("Tables", "");
                 TreeData viewsRoot = new TreeData("Views", "");
                 while(rs.next())
                 {
                     String tableName = rs.getString(1);
                     String type = rs.getString(2);
 
                     TreeData child = new TreeData(tableName, "");
                     if(DBTable.getTableType(type) == DBTable.TableType.TABLE)
                         tablesRoot.addChild(child);
                     else if(DBTable.getTableType(type) == DBTable.TableType.VIEW)
                         viewsRoot.addChild(child);
                 }
 
                 loadData(tablesRoot, conn);
                 loadData(viewsRoot, conn);
 
                 TreeData dbRoot = new TreeData("Data Source - " + dbName, "");
                 dbRoot.addChild(tablesRoot);
                 dbRoot.addChild(viewsRoot);
                 tree.add(dbRoot);
                 String json = new Gson().toJson(tree);
                 out.write(json);
             }
             catch(SQLException ex)
             {
                 System.out.println(ex.getMessage());
                 ex.printStackTrace();
             }
             catch(ClassNotFoundException ex)
             {
                 System.out.println(ex.getMessage());
                 ex.printStackTrace();;
             }
             catch(Exception ex)
             {
                 System.out.println(ex.getMessage());
                 ex.printStackTrace();
             }
             finally
             {
                 if(conn != null)
                 {
                     try
                     {
                         conn.close();
                     }
                     catch(Exception ex)
                     {
                         System.out.println(ex.getMessage());
                         ex.printStackTrace();
                     }
                 }
             }
         }
         finally
         {
             out.close();
         }
     }
 
     // <editor-fold defaultstate="collapsed" desc="HttpServlet methods. Click on the + sign on the left to edit the code.">
     /** 
      * Handles the HTTP <code>GET</code> method.
      * @param request servlet request
      * @param response servlet response
      * @throws ServletException if a servlet-specific error occurs
      * @throws IOException if an I/O error occurs
      */
     @Override
     protected void doGet(HttpServletRequest request, HttpServletResponse response)
             throws ServletException, IOException
     {
         processRequest(request, response);
     }
 
     /** 
      * Handles the HTTP <code>POST</code> method.
      * @param request servlet request
      * @param response servlet response
      * @throws ServletException if a servlet-specific error occurs
      * @throws IOException if an I/O error occurs
      */
     @Override
     protected void doPost(HttpServletRequest request, HttpServletResponse response)
             throws ServletException, IOException
     {
         processRequest(request, response);
     }
 
     /** 
      * Returns a short description of the servlet.
      * @return a String containing servlet description
      */
     @Override
     public String getServletInfo()
     {
         return "Short description";
     }// </editor-fold>
 
     private void loadData(TreeData data, java.sql.Connection conn) throws SQLException
     {
         if(data.getChildren() == null)
             return;
         for(TreeData td : data.getChildren())
         {
             java.sql.PreparedStatement getCols = conn.prepareStatement(
                     "select column_name, data_type from information_schema.columns "
                     + "where table_name=?");
             getCols.setString(1, td.getLabel());
             ResultSet cols = getCols.executeQuery();
 
             while(cols.next())
             {
                 String colName = cols.getString(1);
                 String dType = cols.getString(2);
 
                 TreeData col = new TreeData(colName, dType);
                 td.addChild(col);
             }
         }
     }
 }
