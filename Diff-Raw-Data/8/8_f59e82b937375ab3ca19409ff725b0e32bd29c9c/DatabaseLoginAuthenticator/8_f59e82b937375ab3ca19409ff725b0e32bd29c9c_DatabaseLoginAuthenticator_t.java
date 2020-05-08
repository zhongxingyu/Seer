 /*
  * Copyright (c) 2000-2003 Netspective Communications LLC. All rights reserved.
  *
  * Netspective Communications LLC ("Netspective") permits redistribution, modification and use of this file in source
  * and binary form ("The Software") under the Netspective Source License ("NSL" or "The License"). The following
  * conditions are provided as a summary of the NSL but the NSL remains the canonical license and must be accepted
  * before using The Software. Any use of The Software indicates agreement with the NSL.
  *
  * 1. Each copy or derived work of The Software must preserve the copyright notice and this notice unmodified.
  *
  * 2. Redistribution of The Software is allowed in object code form only (as Java .class files or a .jar file
  *    containing the .class files) and only as part of an application that uses The Software as part of its primary
  *    functionality. No distribution of the package is allowed as part of a software development kit, other library,
  *    or development tool without written consent of Netspective. Any modified form of The Software is bound by these
  *    same restrictions.
  *
  * 3. Redistributions of The Software in any form must include an unmodified copy of The License, normally in a plain
  *    ASCII text file unless otherwise agreed to, in writing, by Netspective.
  *
  * 4. The names "Netspective", "Axiom", "Commons", "Junxion", and "Sparx" are trademarks of Netspective and may not be
  *    used to endorse products derived from The Software without without written consent of Netspective. "Netspective",
  *    "Axiom", "Commons", "Junxion", and "Sparx" may not appear in the names of products derived from The Software
  *    without written consent of Netspective.
  *
  * 5. Please attribute functionality where possible. We suggest using the "powered by Netspective" button or creating
  *    a "powered by Netspective(tm)" link to http://www.netspective.com for each application using The Software.
  *
  * THE SOFTWARE IS PROVIDED "AS IS" WITHOUT A WARRANTY OF ANY KIND. ALL EXPRESS OR IMPLIED REPRESENTATIONS AND
  * WARRANTIES, INCLUDING ANY IMPLIED WARRANTY OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE OR NON-INFRINGEMENT,
  * ARE HEREBY DISCLAIMED.
  *
  * NETSPECTIVE AND ITS LICENSORS SHALL NOT BE LIABLE FOR ANY DAMAGES SUFFERED BY LICENSEE OR ANY THIRD PARTY AS A
  * RESULT OF USING OR DISTRIBUTING THE SOFTWARE. IN NO EVENT WILL NETSPECTIVE OR ITS LICENSORS BE LIABLE FOR ANY LOST
  * REVENUE, PROFIT OR DATA, OR FOR DIRECT, INDIRECT, SPECIAL, CONSEQUENTIAL, INCIDENTAL OR PUNITIVE DAMAGES, HOWEVER
  * CAUSED AND REGARDLESS OF THE THEORY OF LIABILITY, ARISING OUT OF THE USE OF OR INABILITY TO USE THE SOFTWARE, EVEN
  * IF HE HAS BEEN ADVISED OF THE POSSIBILITY OF SUCH DAMAGES.
  *
  * @author Shahid N. Shah
  */
 
 /**
 * $Id: DatabaseLoginAuthenticator.java,v 1.2 2003-08-30 00:24:32 shahid.shah Exp $
  */
 
 package com.netspective.sparx.security.authenticator;
 
 import com.netspective.sparx.security.LoginAuthenticator;
 import com.netspective.sparx.security.LoginDialogContext;
 import com.netspective.sparx.security.HttpLoginManager;
 import com.netspective.axiom.sql.Query;
 import com.netspective.axiom.sql.QueryResultSet;
 import com.netspective.axiom.sql.ResultSetUtils;
 
 import org.apache.commons.logging.LogFactory;
 import org.apache.commons.logging.Log;
 
 public class DatabaseLoginAuthenticator implements LoginAuthenticator
 {
     private static final Log log = LogFactory.getLog(DatabaseLoginAuthenticator.class);
 
     private Query passwordQuery;        // the query to get the user's password
     private Query roleQuery;            // the query to get the user's roles
     private boolean passwordEncrypted;  // true if the password is encrypted
 
     public boolean isUserValid(HttpLoginManager loginManager, LoginDialogContext loginDialogContext)
     {
         if(passwordQuery == null)
         {
             loginDialogContext.getValidationContext().addError("No password query defined in DatabaseLoginAuthenticator");
             return false;
         }
 
         try
         {
             QueryResultSet qrs = passwordQuery.execute(loginDialogContext, new Object[] { loginDialogContext.getUserIdInput() }, false);
             if(qrs == null)
                 return false;
             String loginPasswordInDB = ResultSetUtils.getInstance().getResultSetSingleColumn(qrs.getResultSet()).toString();
            qrs.close(true);
 
             // if the password is not encrypted in the database, then encrypt it now because we deal with encrypted passwords internally
             if(! passwordEncrypted)
                 loginPasswordInDB = loginDialogContext.encryptPlainTextPassword(loginPasswordInDB);
 
             // now we check if this is a valid user
             if(! loginPasswordInDB.equals(loginDialogContext.getPasswordInput(! loginDialogContext.hasEncryptedPassword())))
                 return false;
 
             if(roleQuery != null)
             {
                 qrs = roleQuery.execute(loginDialogContext, new Object[] { loginDialogContext.getUserIdInput() }, false);
                 if(qrs != null)
                 {
                     String[] roleNames = ResultSetUtils.getInstance().getResultSetRowsAsStrings(qrs.getResultSet());
                     if(roleNames != null && roleNames.length > 0)
                         loginDialogContext.setUserRoleNames(roleNames);
                    qrs.close(true);
                 }
             }
         }
         catch(Exception e)
         {
             log.error("Error validating login", e);
             return false;
         }
 
         return true;
     }
 
     public boolean isPasswordEncrypted()
     {
         return passwordEncrypted;
     }
 
     public void setPasswordEncrypted(boolean passwordEncrypted)
     {
         this.passwordEncrypted = passwordEncrypted;
     }
 
     public Query getPasswordQuery()
     {
         return passwordQuery;
     }
 
     public Query createPasswordQuery()
     {
         return new Query();
     }
 
     public void addPasswordQuery(Query query)
     {
         passwordQuery = query;
     }
 
     public Query getRoleQuery()
     {
         return roleQuery;
     }
 
     public Query createRoleQuery()
     {
         return new Query();
     }
 
     public void addRoleQuery(Query query)
     {
         roleQuery = query;
     }
 
 }
