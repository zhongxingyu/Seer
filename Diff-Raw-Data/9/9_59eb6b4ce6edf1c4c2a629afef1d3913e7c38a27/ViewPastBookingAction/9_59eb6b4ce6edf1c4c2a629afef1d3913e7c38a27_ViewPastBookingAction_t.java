 package com.nclodger.control.action.access;
 
 import com.nclodger.additional.BookingViewing;
 import com.nclodger.control.action.Action;
 import com.nclodger.dao.UserDao;
 import com.nclodger.myexception.MyException;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import java.util.ArrayList;
 
 /**
  * Created with IntelliJ IDEA.
  * User: pasha
  * Date: 12/3/13
  * Time: 9:48 PM
  * To change this template use File | Settings | File Templates.
  */
 public class ViewPastBookingAction extends Action {
 
     @Override
     public String execute(HttpServletRequest request, HttpServletResponse response) throws MyException {
         String email = request.getSession().getAttribute("email").toString();
         UserDao udao = new UserDao();
         int id = udao.getUserId(email);
 
         ArrayList<BookingViewing> bookinglist = new ArrayList<BookingViewing>();
         bookinglist = udao.getPastOrder(id);
         request.setAttribute("bookinglist",bookinglist);
 
        return "myorders";
     }
 }
