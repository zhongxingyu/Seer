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
 package com.flexive.core.security;
 
 import com.flexive.shared.CacheAdmin;
 import com.flexive.shared.EJBLookup;
 import com.flexive.shared.exceptions.FxAccountExpiredException;
 import com.flexive.shared.exceptions.FxAccountInUseException;
 import com.flexive.shared.exceptions.FxApplicationException;
 import com.flexive.shared.exceptions.FxLoginFailedException;
 import com.flexive.shared.interfaces.ScriptingEngine;
 import com.flexive.shared.scripting.FxScriptBinding;
 import com.flexive.shared.scripting.FxScriptEvent;
 import com.flexive.shared.scripting.FxScriptResult;
 import com.flexive.shared.security.UserTicket;
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 
 import javax.security.auth.login.LoginException;
 import java.util.List;
 
 /**
  * Authentication handler,calling relevant scripts or performs authentication against the database if no scripts are available.
  *
  * @author Markus Plesser (markus.plesser@flexive.com), UCS - unique computing solutions gmbh (http://www.ucs.at)
  * @version $Rev
  * @see com.flexive.shared.scripting.FxScriptEvent#AccountLogin
  * @see com.flexive.shared.scripting.FxScriptEvent#AccountLogout
  * @see com.flexive.core.security.FxDBAuthentication
  */
 public class FxAuthenticationHandler {
 
     private static final Log LOG = LogFactory.getLog(FxAuthenticationHandler.class);
 
     /**
      * Login a user using flexive's database
      *
      * @param loginname name of the user
      * @param password plaintext password
      * @param callback callback providing datasource, ejb context and "take over"
      * @return Authenticated UserTicket
      * @throws FxAccountInUseException   on errors
      * @throws FxLoginFailedException    on errors
      * @throws FxAccountExpiredException on errors
      */
     public static UserTicket login(String loginname, String password, FxCallback callback) throws FxAccountInUseException, FxLoginFailedException, FxAccountExpiredException {
         ScriptingEngine scripting = EJBLookup.getScriptingEngine();
         List<Long> events = scripting.getByScriptEvent(FxScriptEvent.AccountLogin);
 
         FxScriptBinding binding = new FxScriptBinding();
         binding.setVariable("loginname", loginname);
         binding.setVariable("password", password);
         binding.setVariable("callback", callback);
         for (Long eventId : events) {
             FxScriptResult res = null;
             try {
                 res = scripting.runScript(eventId, binding);
             } catch (FxApplicationException e) {
                 LOG.error("Error executing script '" + CacheAdmin.getEnvironment().getScript(eventId).getName() + "': " + e.getMessage(), e);
             }
             if (res != null && res.getResult() instanceof UserTicket) {
                 return (UserTicket) res.getResult();
             }
         }
         //fallback
         LOG.info("Authenticating user [" + loginname + "] against the database.");
         return FxDBAuthentication.login(loginname, password, callback);
     }
 
     /**
      * Mark a user as no longer active in the database.
      *
      * @param ticket the ticket of the user
      * @throws LoginException if the function failed
      */
     public static void logout(UserTicket ticket) throws LoginException {
         ScriptingEngine scripting = EJBLookup.getScriptingEngine();
         List<Long> events = scripting.getByScriptEvent(FxScriptEvent.AccountLogout);
 
         FxScriptBinding binding = new FxScriptBinding();
         binding.setVariable("ticket", ticket);
         boolean scriptCalled = false;
         for (Long eventId : events) {
             FxScriptResult res = null;
             try {
                 scripting.runScript(eventId, binding);
                 scriptCalled = true;
             } catch (FxApplicationException e) {
                 LOG.error("Error executing script '" + CacheAdmin.getEnvironment().getScript(eventId).getName() + "': " + e.getMessage(), e);
             }
         }
         if (!scriptCalled) {
             //fallback
             LOG.info("Logging out user [" + ticket.getUserName() + "] from the database.");
             FxDBAuthentication.logout(ticket);
         }
     }
 
 
 }
