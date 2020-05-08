 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package com.pos.controller;
 
 import com.pos.action.LoginAction;
 import com.pos.action.SaveMenuItemAction;
 import com.pos.dao.LoginDAO;
 import com.pos.form.MenuItemForm;
 import com.pos.model.Menu;
 import com.pos.model.MenuItem;
 import java.io.IOException;
 import java.util.List;
 import javax.servlet.RequestDispatcher;
 import javax.servlet.ServletException;
 import javax.servlet.annotation.WebServlet;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 /**
  *
  * @author Joshua Miller
  */
 //@todo probably shouldn't have hard coded strings here?  Check best practice...Low Priority
 @WebServlet(name = "ControllerServlet", 
     urlPatterns = {
         "/place_order",
         "/logout",
         "/item_input", 
         "/item_save",
         "/view_items", 
         "/", 
         "", 
         "/login",
         "/verify_login",
        "/POSProject"
         })
 public class ControllerServlet extends HttpServlet{
     private static final long serialVersionUID = 1L;
     
     @Override
     public void doGet(HttpServletRequest request, HttpServletResponse response)
             throws IOException, ServletException{
         process(request, response);
     }
     
     @Override
     public void doPost(HttpServletRequest request, HttpServletResponse response)
             throws IOException, ServletException{
         process(request, response);
     }
     
     private void process(HttpServletRequest request, HttpServletResponse response)
             throws IOException, ServletException{
         
         String dispatchUrl = null;
         //Get the last part of the URI
         
         //@todo when no trailing slash this block fails to work as intended
         String uri = request.getRequestURI();
         int lastSlashIndex = uri.lastIndexOf("/");
         String action = uri.substring(lastSlashIndex + 1);
         
         if(action.equals("POSProject")){
             //@todo grab hostname from properties file or somewhere else
             //shouldn't be hardcoded.
             response.sendRedirect("http://localhost:8080/POSProject/");
             
         }
         
         else if (action.equals("item_input")){
             //ACTION:
             //DISPATCH:
             dispatchUrl = "jsp/MenuItemForm.jsp";
         }
         
         else if (action.equals("item_save")){
             //ACTION:
             MenuItemForm menuItemForm = new MenuItemForm();
             menuItemForm.setName(request.getParameter("name"));
             menuItemForm.setPrice(request.getParameter("price"));
             MenuItem menuItem = new MenuItem();
             menuItem.setName(request.getParameter("name"));
             menuItem.setPrice(Float.parseFloat(request.getParameter("price")));
             SaveMenuItemAction saveMenuItemAction = new SaveMenuItemAction();
             saveMenuItemAction.save(menuItem);
             request.setAttribute("menuItem", menuItem);
             //DISPATCH:
             dispatchUrl = "jsp/ItemDetails.jsp";
         }
         
         else if (action.equals("view_items")){
             //ACTION:
             Menu menu = new Menu();
             menu.refreshMenu();
             List<MenuItem> menuList = menu.getItems();
             request.setAttribute("menuList", menuList);
             //DISPATCH:
             dispatchUrl = "jsp/ViewItems.jsp";
         }
         
         else if (action.equals("verify_login")){
             //ACTION:
             LoginAction loginAction = new LoginAction(request.getParameter("username"),
                     request.getParameter("password"));
             int authenticationResult = loginAction.authenticate();
             //DISPATCH:
             if(authenticationResult == LoginDAO.Result.PASS){
                 //@todo get and pass organization
                 //@todo create ManagerLanding.jsp & EmployeeLanding.jsp
                 if(loginAction.getRole().equals("manager")){
                     dispatchUrl = "jsp/ManagerLanding.jsp";
                 }
                 else if(loginAction.getRole().equals("employee")){
                     dispatchUrl = "jsp/EmployeeLanding.jsp";
                 }
             }
             else if (authenticationResult == LoginDAO.Result.WRONG_PASSWORD){
                 /*@todo all of these setAttribute()'s should conform to package naming standards
                 ...should be com.pos.controller.errorMessage?...look up best practice*/
                 request.setAttribute("errorMessage", "wrong_password");
                 dispatchUrl = "jsp/Login.jsp";
             }
             else if (authenticationResult == LoginDAO.Result.NO_USER){
                 request.setAttribute("errorMessage", "no_user");
                 dispatchUrl = "jsp/Login.jsp";
             }
         }
         
         else if (action.equals("login") || action.equals("") || action.equals("/")){
             //ACTION:
                 //@todo handle those with valid sessions (send right to menu)
             //DISPATCH:
             dispatchUrl = "jsp/Login.jsp";
             
         }
         
         else if (action.equals("place_order")){
             //ACTION:
             //DISPATCH:
                 //@todo implement jsp/PlaceOrder.jsp
             dispatchUrl = "jsp/PlaceOrder.jsp";
         }
         else if (action.equals("logout")){
             //ACTION:
                 //@todo kill session 
             //DISPATCH:
                 //@todo Decide if you want some type of logout specific page
             dispatchUrl = "jsp/Login.jsp";
         }
         if(dispatchUrl != null){
             RequestDispatcher rd = request.getRequestDispatcher(dispatchUrl);
             rd.forward(request, response);
         }
     }
 }
