 package controller;
 
 import manager.DebtTypeManager;
 import manager.HousingManager;
 import manager.RegionManager;
 import model.Housing;
 
 import javax.ejb.EJB;
 import javax.servlet.RequestDispatcher;
 import javax.servlet.ServletException;
 import javax.servlet.annotation.WebServlet;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import javax.servlet.http.HttpSession;
 import java.io.IOException;
 
 /**
  * Created with IntelliJ IDEA.
  * User: acottrill
  * Date: 3/11/13
  * Time: 3:26 PM
  * To change this template use File | Settings | File Templates.
  */
 @WebServlet(name = "aspirations", urlPatterns = {"/aspirations"})
 public class AspirationsServlet extends HttpServlet {
 
     @EJB
     private DebtTypeManager dtm;
 
     @EJB
     private RegionManager rm;
 
     @EJB
     private HousingManager hm;
 
     protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
 
         HttpSession session = request.getSession();
         for(String s : new String[]{
                 "input_bills",
                 "go_out_to_lunch",
                 "go_out_to_dinner",
                 "spend_on_entertainment"
         }){
             session.setAttribute(s, request.getParameter(s));
         }
 
         String h = request.getParameter("housing_situation");
         if(h.equals("Apartment")){
             session.setAttribute("rent", request.getParameter("input_rent"));
             session.setAttribute("utilities", request.getParameter("input_bills"));
         }else{
             Housing housing = hm.findHousing(h);
             session.setAttribute("rent", housing.getRent()+"");
             session.setAttribute("utilities", housing.getUtilities()+"");
         }
         request.setAttribute("all_regions", rm.getRegions());
         RequestDispatcher dispatcher = request.getRequestDispatcher("WEB-INF/aspirations.jsp");
         dispatcher.forward(request, response);
     }
 }
