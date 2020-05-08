 /*
  * Copyright (C) 2011 kevin
  *
  * This program is free software; you can redistribute it and/or
  * modify it under the terms of the GNU General Public License
  * as published by the Free Software Foundation; either version 2
  * of the License, or (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program; if not, write to the Free Software
  * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
  */
 
 package models;
 
 import db.factories.UsersFactory;
 import java.sql.SQLException;
 import javax.servlet.http.Cookie;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import javax.servlet.http.HttpSession;
 import metier.User;
 
 
 public class SessionModel {
     private User currentUser = null;
     
     
     public boolean authenticate(String login, String pass, HttpServletRequest request) throws SQLException {
         User u = UsersFactory.get(login, User.hashPass(pass));
         
         if(u == null)
             return false;
         
         connect(u, request);
         
         return true;
     }
     
     public boolean authenticate(String login, String pass, HttpServletRequest request, HttpServletResponse response) throws SQLException {
         if(!authenticate(login, pass, request))
             return false;
        
         Cookie cLogin = new Cookie("login", login);
         cLogin.setMaxAge(3600 * 24 * 30); // 30 jours
         
         Cookie cPass = new Cookie("pass", User.hashPass(pass));
         cPass.setMaxAge(3600 * 24 * 30); // 30 jours
         
         response.addCookie(cLogin);
         response.addCookie(cPass);
         
         return true;
     }
     
     public void logout(HttpServletRequest request, HttpServletResponse response) {
         currentUser = null;
         
         HttpSession session = request.getSession();
         session.invalidate();
         
         Cookie cLogin = new Cookie("login", "");
         cLogin.setMaxAge(-1);
         
         Cookie cPass = new Cookie("pass", "");
         cPass.setMaxAge(-1);
         
         response.addCookie(cLogin);
         response.addCookie(cPass);
     }
     
     public boolean isLoggedIn() {
         return currentUser != null;
     }
 
     public void tryConnect(HttpServletRequest request) throws SQLException {
         HttpSession session = request.getSession();
         Cookie loginCookie = null;
         Cookie passCookie = null;
         
        // pas de cookies disponibles
        if(request.getCookies() == null)
            return;

         for(Cookie c : request.getCookies()) {
             if(c.getName().equals("login"))
                 loginCookie = c;
             else if(c.getName().equals("pass"))
                 passCookie = c;
         }
         
         
         if(session.getAttribute("currentUserId") != null)
             connect(((Integer) session.getAttribute("currentUserId")).intValue(), request);
         else if(loginCookie != null && passCookie != null) {
             User u = UsersFactory.get(loginCookie.getValue(), passCookie.getValue());
             
             if(u != null)
                 connect(u, request);
         }
     }
     
     private void connect(User u, HttpServletRequest request) {
         if(u == null)
             throw new IllegalArgumentException("Impossible de connecter un utilisateur valant null");
         
         currentUser = u;
             
         HttpSession session = request.getSession();
         session.setAttribute("currentUserId", new Integer(u.getId()));
     }
 
     private void connect(int id, HttpServletRequest request) throws SQLException {
         User u = UsersFactory.get(id);
         
         if(u != null)
             connect(u, request);
     }
 }
