 /***************************************************************
  *  This file is part of the [fleXive](R) framework.
  *
  *  Copyright (c) 1999-2008
  *  UCS - unique computing solutions gmbh (http://www.ucs.at)
  *  All rights reserved
  *
  *  The [fleXive](R) project is free software; you can redistribute
  *  it and/or modify it under the terms of the GNU Lesser General Public
  *  License version 2.1 or higher as published by the Free Software Foundation.
  *
  *  The GNU Lesser General Public License can be found at
  *  http://www.gnu.org/licenses/lgpl.html.
  *  A copy is found in the textfile LGPL.txt and important notices to the
  *  license from the author are found in LICENSE.txt distributed with
  *  these libraries.
  *
  *  This library is distributed in the hope that it will be useful,
  *  but WITHOUT ANY WARRANTY; without even the implied warranty of
  *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *  GNU General Public License for more details.
  *
  *  For further information about UCS - unique computing solutions gmbh,
  *  please see the company website: http://www.ucs.at
  *
  *  For further information about [fleXive](R), please see the
  *  project website: http://www.flexive.org
  *
  *
  *  This copyright notice MUST APPEAR in all copies of the file!
  ***************************************************************/
 package com.flexive.faces.beans;
 
 import com.flexive.faces.FxJsfUtils;
 import com.flexive.faces.messages.FxFacesMsgErr;
 import com.flexive.faces.messages.FxFacesMsgWarn;
 import com.flexive.shared.FxContext;
 import com.flexive.shared.exceptions.FxLoginFailedException;
 import com.flexive.shared.security.UserTicket;
 import com.flexive.war.FxRequest;
 
 import java.io.Serializable;
 
 /**
  * Provides basic authentication for [fleXive] applications. Simply map your username/password inputs
  * to {@code #{fxAuthenticationBean.username}} and {@code #{fxAuthenticationBean.password}} and invoke
  * {@code #{fxAuthenticationBean.login}}.
  * <p>
  * For logout, bind your command element to {@code #{fxAuthenticationBean.logout}}. Note that since the current
  * session is invalidated by this method, you have to perform a redirect after calling this action. Otherwise
  * JSF will not be able to restore the view state and throw an exception.
  * </p>
  *
  * @author Daniel Lichtenberger (daniel.lichtenberger@flexive.com), UCS - unique computing solutions gmbh (http://www.ucs.at)
  * @version $Rev$
  */
 public class AuthenticationBean implements Serializable {
     private static final long serialVersionUID = -8245131162897398694L;
 
     private String username;
     private String password;
     private boolean takeover;
 
 
     public UserTicket getTicket() {
         return FxContext.getUserTicket();
     }
 
     public boolean getIsLoggedIn() {
         return !FxContext.getUserTicket().isGuest();
     }
 
     /**
      * Login using the credentials stored in {@code username} and {@code password}.
      *
      * @return "loginSuccess" after a successful login, null otherwise
      */
     public String login() {
         try {
             FxRequest request = FxJsfUtils.getRequest();
             FxContext.get().login(username, password, takeover);
             request.getUserTicket();
             return "loginSuccess";
         } catch (FxLoginFailedException e) {
             new FxFacesMsgErr(
                     e,
                     // wrong password isn't really an error that should end up in the logfiles
                     e.getType() != FxLoginFailedException.TYPE_USER_OR_PASSWORD_NOT_DEFINED
             ).addToContext();
         } catch (Exception exc) {
            new FxFacesMsgErr(exc).addToContext();
         }
         return null;
     }
 
     /**
      * Logout the current user.
      *
      * @return  "login"
      */
     public String logout() {
         try {
             FxContext.get().logout();
             FxJsfUtils.getSession().invalidate();
         } catch (Exception exc) {
             new FxFacesMsgErr(exc).addToContext();
         }
         return "login";
     }
 
     public String getUsername() {
         return username;
     }
 
     public void setUsername(String username) {
         this.username = username;
     }
 
     public String getPassword() {
         return this.password;
     }
 
     public void setPassword(String password) {
         this.password = password;
     }
 
     public boolean isTakeover() {
         return takeover;
     }
 
     public void setTakeover(boolean takeover) {
         this.takeover = takeover;
     }
 
 }
