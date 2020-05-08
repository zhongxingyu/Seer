 /*
  * Copyright (c) 2000-2004 Netspective Communications LLC. All rights reserved.
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
  *    used to endorse or appear in products derived from The Software without written consent of Netspective.
  *
  * THE SOFTWARE IS PROVIDED "AS IS" WITHOUT A WARRANTY OF ANY KIND. ALL EXPRESS OR IMPLIED REPRESENTATIONS AND
  * WARRANTIES, INCLUDING ANY IMPLIED WARRANTY OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE OR NON-INFRINGEMENT,
  * ARE HEREBY DISCLAIMED.
  *
  * NETSPECTIVE AND ITS LICENSORS SHALL NOT BE LIABLE FOR ANY DAMAGES SUFFERED BY LICENSEE OR ANY THIRD PARTY AS A
  * RESULT OF USING OR DISTRIBUTING THE SOFTWARE. IN NO EVENT WILL NETSPECTIVE OR ITS LICENSORS BE LIABLE FOR ANY LOST
  * REVENUE, PROFIT OR DATA, OR FOR DIRECT, INDIRECT, SPECIAL, CONSEQUENTIAL, INCIDENTAL OR PUNITIVE DAMAGES, HOWEVER
  * CAUSED AND REGARDLESS OF THE THEORY OF LIABILITY, ARISING OUT OF THE USE OF OR INABILITY TO USE THE SOFTWARE, EVEN
  * IF IT HAS BEEN ADVISED OF THE POSSIBILITY OF SUCH DAMAGES.
  */
 package com.netspective.sparx.security.authenticator;
 
 import java.util.Map;
 import java.sql.SQLException;
 
 import javax.naming.NamingException;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 
 import com.netspective.axiom.sql.Query;
 import com.netspective.axiom.sql.QueryResultSet;
 import com.netspective.axiom.sql.ResultSetUtils;
 import com.netspective.commons.security.AuthenticatedUser;
 import com.netspective.commons.security.AuthenticatedUserInitializationException;
 import com.netspective.commons.security.MutableAuthenticatedOrganization;
 import com.netspective.commons.security.MutableAuthenticatedOrganizations;
 import com.netspective.commons.security.MutableAuthenticatedUser;
 import com.netspective.commons.xdm.XmlDataModelSchema;
 import com.netspective.sparx.security.HttpLoginManager;
 import com.netspective.sparx.security.LoginDialogContext;
 
 public class DatabaseLoginAuthenticator extends AbstractLoginAuthenticator
 {
     public static final XmlDataModelSchema.Options XML_DATA_MODEL_SCHEMA_OPTIONS = new XmlDataModelSchema.Options().setIgnorePcData(true);
     private static final Log log = LogFactory.getLog(DatabaseLoginAuthenticator.class);
 
     private String passwordQueryPasswordColumnLabel = "password";
     private Query passwordQuery;        // the query to get the user's password
     private Query roleQuery;            // the query to get the user's roles
     private Query primaryOrgQuery;      // the query to get the user's primary organization
     private Query orgsQuery;            // the query to get the user's supplementary organizations
     private boolean passwordEncrypted;  // true if the password is encrypted
     private boolean showErrorsToUser;   // true if exceptions should be shown to the user
 
     private static final String ATTRNAME_PASSWORD_QUERY_RESULTS = "PASSWORD_QUERY_RESULTS";
     private static final String ATTRNAME_PRIMARY_ORG_QUERY_RESULTS = "PRIMARY_ORG_QUERY_RESULTS";
     private static final String ATTRNAME_ORGS_QUERY_RESULTS = "ORGS_QUERY_RESULTS";
 
     public String getPasswordQueryPasswordColumnLabel()
     {
         return passwordQueryPasswordColumnLabel;
     }
 
     public void setPasswordQueryPasswordColumnLabel(String passwordQueryPasswordColumnLabel)
     {
         this.passwordQueryPasswordColumnLabel = passwordQueryPasswordColumnLabel;
     }
 
     public boolean isUserValid(HttpLoginManager loginManager, LoginDialogContext ldc)
     {
         if(passwordQuery == null)
         {
             ldc.getValidationContext().addError("No password query defined in DatabaseLoginAuthenticator");
             return false;
         }
 
         QueryResultSet qrs = null;
         try
         {
             qrs = passwordQuery.execute(ldc, new Object[]{ldc.getUserIdInput()}, false);
             if(qrs == null)
             {
                 log.error("Unable to execute the password query, the query returned a NULL QueryResultSet.");
                 if(showErrorsToUser) ldc.getValidationContext().addError("Unable to execute the password query.");
                 return false;
             }
         }
         catch(Exception e)
         {
             log.error(e);
             if(showErrorsToUser)
                 ldc.getValidationContext().addError("An error occurred while attempting to authenticate: " + e.getMessage());
             return false;
         }
 
         Map passwordQueryResultRow;
         Object loginPasswordObj;
         try
         {
             passwordQueryResultRow = ResultSetUtils.getInstance().getResultSetSingleRowAsMap(qrs.getResultSet(), true);
            if(passwordQueryResultRow != null)
            {
                loginPasswordObj = passwordQueryResultRow.get(passwordQueryPasswordColumnLabel);
                ldc.setAttribute(ATTRNAME_PASSWORD_QUERY_RESULTS, passwordQueryResultRow);
            }
            else
                return false;
         }
         catch(Exception e)
         {
             log.error(e);
             if(showErrorsToUser)
                 ldc.getValidationContext().addError("An error occurred while attempting to authenticate: " + e.getMessage());
             return false;
         }
         finally
         {
             try
             {
                 qrs.close(true);
             }
             catch(SQLException e)
             {
                 log.error(e);
                 if(showErrorsToUser)
                     ldc.getValidationContext().addError("A SQL occurred while closing the connection: " + e.getMessage());
                 return false;
             }
         }
 
         // make sure the password doesn't stay in the map as it's passed around the system
         passwordQueryResultRow.remove(passwordQueryPasswordColumnLabel);
 
         if(loginPasswordObj == null)
             return false;
 
         String loginPasswordText = loginPasswordObj.toString();
         // if the password is not encrypted in the database, then encrypt it now because we deal with encrypted passwords internally
         if(!passwordEncrypted)
             loginPasswordText = ldc.encryptPlainTextPassword(loginPasswordText);
 
         // now we check if this is a valid user
         if(!loginPasswordText.equals(ldc.getPasswordInput(!ldc.hasEncryptedPassword())))
             return false;
 
         return true;
     }
 
     public void initAuthenticatedUser(HttpLoginManager loginManager, LoginDialogContext ldc, MutableAuthenticatedUser user) throws AuthenticatedUserInitializationException
     {
         assignUserInfo(ldc, user);
         assignOrganizations(ldc, user);
         assignUserRoles(ldc, user);
 
         // the super will call user.init so we want to give the authenticated user class a chance to initalize itself
         // now that the user information and roles have been assigned
         super.initAuthenticatedUser(loginManager, ldc, user);
     }
 
     /**
      * Take all of the columns that were selected by the passwordQuery and assign them using the setXXX() methods
      * of the AuthenticatedUser object. Using reflection, the assignUserInfo() method will take the labels assigned
      * in the query and find the appropriate setXXXX() method (where XXXX is the column label in the SQL query).
      */
     protected void assignUserInfo(LoginDialogContext ldc, AuthenticatedUser user)
     {
         XmlDataModelSchema schema = XmlDataModelSchema.getSchema(user.getClass());
         try
         {
             schema.assignMapValues(user, (Map) ldc.getAttribute(ATTRNAME_PASSWORD_QUERY_RESULTS), "*");
         }
         catch(Exception e)
         {
             log.error(e);
         }
     }
 
     protected void assignUserRoles(LoginDialogContext ldc, MutableAuthenticatedUser user)
     {
         if(roleQuery != null)
         {
             try
             {
                 QueryResultSet qrs = roleQuery.execute(ldc, new Object[]{ldc.getUserIdInput()}, false);
                 if(qrs != null)
                 {
                     String[] roleNames = ResultSetUtils.getInstance().getResultSetRowsAsStrings(qrs.getResultSet());
                     if(roleNames != null && roleNames.length > 0)
                         user.setRoles(ldc.getAccessControlListsManager(), roleNames);
                     qrs.close(true);
                 }
             }
             catch(Exception e)
             {
                 log.error("Error assigning roles to user", e);
             }
         }
     }
 
     protected void assignOrganizations(LoginDialogContext ldc, AuthenticatedUser user)
     {
         MutableAuthenticatedOrganizations mutableOrgs = (MutableAuthenticatedOrganizations) user.getOrganizations();
 
         if(orgsQuery != null)
         {
             try
             {
                 QueryResultSet qrs = orgsQuery.execute(ldc, new Object[]{ldc.getUserIdInput()}, false);
                 if(qrs != null)
                 {
                     Map[] orgsResult = ResultSetUtils.getInstance().getResultSetRowsAsMapArray(qrs.getResultSet(), true);
                     ldc.setAttribute(ATTRNAME_ORGS_QUERY_RESULTS, orgsResult);
 
                     for(int rowIndex = 0; rowIndex < orgsResult.length; rowIndex++)
                     {
                         MutableAuthenticatedOrganization org = mutableOrgs.createOrganization();
                         XmlDataModelSchema schema = XmlDataModelSchema.getSchema(org.getClass());
                         Map row = orgsResult[rowIndex];
                         schema.assignMapValues(org, row, "*");
                         mutableOrgs.addOrganization(org);
                     }
 
                     qrs.close(true);
                 }
             }
             catch(Exception e)
             {
                 log.error("Error assigning orgs to user", e);
             }
         }
 
         if(primaryOrgQuery != null)
         {
             try
             {
                 QueryResultSet qrs = primaryOrgQuery.execute(ldc, new Object[]{ldc.getUserIdInput()}, false);
                 if(qrs != null)
                 {
                     Map orgResult = ResultSetUtils.getInstance().getResultSetSingleRowAsMap(qrs.getResultSet(), true);
                     ldc.setAttribute(ATTRNAME_PRIMARY_ORG_QUERY_RESULTS, orgResult);
 
                     MutableAuthenticatedOrganization org = mutableOrgs.createOrganization();
                     XmlDataModelSchema schema = XmlDataModelSchema.getSchema(org.getClass());
 
                     schema.assignMapValues(org, orgResult, "*");
 
                     org.setPrimary(true);
                     mutableOrgs.addOrganization(org);
 
                     qrs.close(true);
                 }
             }
             catch(Exception e)
             {
                 log.error("Error assigning primary org to user", e);
             }
         }
     }
 
     public boolean isPasswordEncrypted()
     {
         return passwordEncrypted;
     }
 
     public void setPasswordEncrypted(boolean passwordEncrypted)
     {
         this.passwordEncrypted = passwordEncrypted;
     }
 
     public boolean isShowErrorsToUser()
     {
         return showErrorsToUser;
     }
 
     public void setShowErrorsToUser(boolean showErrorsToUser)
     {
         this.showErrorsToUser = showErrorsToUser;
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
 
     public Query getOrgsQuery()
     {
         return orgsQuery;
     }
 
     public Query createOrgsQuery()
     {
         return new Query();
     }
 
     public void addOrgsQuery(Query query)
     {
         orgsQuery = query;
     }
 
     public Query getPrimaryOrgQuery()
     {
         return primaryOrgQuery;
     }
 
     public Query createPrimaryOrgQuery()
     {
         return new Query();
     }
 
     public void addPrimaryOrgQuery(Query query)
     {
         primaryOrgQuery = query;
     }
 }
