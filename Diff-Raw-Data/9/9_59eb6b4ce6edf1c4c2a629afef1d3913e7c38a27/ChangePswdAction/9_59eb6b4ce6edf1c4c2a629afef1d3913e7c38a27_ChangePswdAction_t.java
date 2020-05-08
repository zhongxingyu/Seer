 package com.nclodger.control.action.access;
 
 import com.nclodger.control.action.Action;
 import com.nclodger.dao.UserDao;
 import com.nclodger.domain.Users;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 /**
  * Created with IntelliJ IDEA.
  * User: Miredean
  * Date: 23.11.13
  * Time: 23:27
  * To change this template use File | Settings | File Templates.
  */
 public class ChangePswdAction extends Action {
 
     @Override
     public String execute(HttpServletRequest request, HttpServletResponse response) throws Exception {
         UserDao userDao = new UserDao();
         //validation of old pass
        String oldpass = request.getParameter("oldpswd_hidden");
         String email = request.getSession().getAttribute("email").toString();
         if(!oldpass.equals(userDao.getPasswordbyEmail(email))){
             request.setAttribute("notify_wrongpswd",true);
             return "ussettings";
         }
 
         int idUser = userDao.getUserId(request.getSession().getAttribute("email").toString());
         Users u = new Users(request.getParameter("password").toString(),idUser);
         userDao.updatePswd(u);
         request.setAttribute("notify_changepswd","Your password has been successfully changed!");
         return "ussettings";
     }
 }
