 /**
  * Copyright (C) 2012-2013  Olexandr Tyshkovets
  *
  * This library is free software; you can redistribute it and/or
  * modify it under the terms of the GNU Lesser General Public
  * License as published by the Free Software Foundation; either
  * version 2.1 of the License, or (at your option) any later version.
  *
  * This library is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
  * Lesser General Public License for more details.
  *
  * You should have received a copy of the GNU Lesser General Public
  * License along with this library; if not, write to the Free Software
  * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
  */
 package com.github.aint.jblog.web.controller;
 
 import java.io.IOException;
 
 import javax.servlet.ServletException;
 import javax.servlet.annotation.WebServlet;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import com.github.aint.jblog.model.dao.hibernate.UserHibernateDao;
 import com.github.aint.jblog.model.entity.User;
 import com.github.aint.jblog.service.data.UserService;
 import com.github.aint.jblog.service.data.impl.UserServiceImpl;
 import com.github.aint.jblog.service.exception.data.UserNotFoundException;
 import com.github.aint.jblog.service.util.HibernateUtil;
 import com.github.aint.jblog.web.constant.ConstantHolder;
 
 /**
  * This servlet displays the user profile.
  * 
  * @author Olexandr Tyshkovets
  */
 @WebServlet("/user")
 public class UserProfile extends HttpServlet {
     private static final long serialVersionUID = 3647042695269023075L;
     private static Logger logger = LoggerFactory.getLogger(UserProfile.class);
     private static final String USER_PROFILE_JSP_PATH = ConstantHolder.PATH.getProperty("path.jsp.userProfile");
 
     /**
      * {@inheritDoc}
      */
     @Override
     protected void doGet(HttpServletRequest request, HttpServletResponse response)
             throws ServletException, IOException {
 
         UserService userService = new UserServiceImpl(new UserHibernateDao(HibernateUtil.getSessionFactory()));
         User user = null;
         try {
             user = userService.getByUserName(request.getParameter("username"));
         } catch (UserNotFoundException e) {
             logger.info("The user not found. {} {}", e.getClass(), e.getMessage());
             response.sendError(HttpServletResponse.SC_NOT_FOUND);
             return;
         }
 
         request.setAttribute("USER", user);
 
         request.getRequestDispatcher(USER_PROFILE_JSP_PATH).forward(request, response);
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     protected void doPost(HttpServletRequest request, HttpServletResponse response)
             throws ServletException, IOException {
         doGet(request, response);
     }
 
 }
