 package com.in6k.mypal.service;
 
 import com.in6k.mypal.dao.UserDao;
 import com.in6k.mypal.domain.User;
 import com.in6k.mypal.util.SecurityUtil;
 import org.springframework.ui.ModelMap;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpSession;
 
 public class UserInfo {
     boolean  loginFlag = false;
 
     public UserInfo() {
     }
 
     public void login() {
         loginFlag = true;
     }
 
     public String isLogin() {
         if (!loginFlag) {
             return "false";
         }
         return "true";
     }
 
     public void Logout() {
         loginFlag = false;
     }
 
     public boolean isLogged(String email, String password, ModelMap model, HttpServletRequest request) {
         HttpSession session = request.getSession();
 
         User user = UserDao.getByEmail(email);
         boolean isPasswordEquals = user.getPassword().equals(SecurityUtil.passwordEncoder(password));
 
         if (user != null && isPasswordEquals) {
             session.setAttribute("LoggedUser", user);
         }
         else {
             model.addAttribute("error", "Wrong password for this user");
 
         }
         return false;
     }
 
     public static void logOut(HttpServletRequest request) {
          HttpSession session = request.getSession();
         if (session != null) {
            session.setAttribute("LoggedUser", null);
         }
     }

     public static boolean isLogged(HttpServletRequest request) {
         HttpSession session = request.getSession();
 
         User userSession = (User) session.getAttribute("LoggedUser");
 
         if (userSession == null) {
             return false;
         }
         return true;
     }
 
     /*public String is
         User userSession = (User) session.getAttribute("LoggedUser");
         if (UserInfo.isLogged()) {
             return "redirect:/login";
         }
         model.addAttribute("sess", userSession);
     }*/
 }
