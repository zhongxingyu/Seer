 /* The contents of this file are subject to the terms
  * of the Common Development and Distribution License
  * (the License). You may not use this file except in
  * compliance with the License.
  *
  * You can obtain a copy of the License at
  * https://opensso.dev.java.net/public/CDDLv1.0.html or
  * opensso/legal/CDDLv1.0.txt
  * See the License for the specific language governing
  * permission and limitations under the License.
  *
  * When distributing Covered Code, include this CDDL
  * Header Notice in each file and include the License file
  * at opensso/legal/CDDLv1.0.txt.
  * If applicable, add the following below the CDDL Header,
  * with the fields enclosed by brackets [] replaced by
  * your own identifying information:
  * "Portions Copyrighted [year] [name of copyright owner]"
  *
 * $Id: DeleteIdentities.java,v 1.5 2007-07-02 21:04:49 veiming Exp $
  *
  * Copyright 2006 Sun Microsystems Inc. All Rights Reserved
  */
 
 package com.sun.identity.cli.idrepo;
 
 
 import com.iplanet.sso.SSOToken;
 import com.iplanet.sso.SSOException;
 import com.sun.identity.cli.CLIException;
 import com.sun.identity.cli.ExitCodes;
 import com.sun.identity.cli.IArgument;
 import com.sun.identity.cli.IOutput;
 import com.sun.identity.cli.LogWriter;
 import com.sun.identity.cli.RequestContext;
 import com.sun.identity.idm.AMIdentity;
 import com.sun.identity.idm.AMIdentityRepository;
 import com.sun.identity.idm.IdRepoException;
 import com.sun.identity.idm.IdType;
import com.sun.identity.sm.OrganizationConfigManager;
import com.sun.identity.sm.SMSException;
 import java.text.MessageFormat;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Set;
 import java.util.logging.Level;
 
 /**
  * This command deletes Identities.
  */
 public class DeleteIdentities extends IdentityCommand {
     /**
      * Services a Commandline Request.
      *
      * @param rc Request Context.
      * @throw CLIException if the request cannot serviced.
      */
     public void handleRequest(RequestContext rc) 
         throws CLIException {
         super.handleRequest(rc);
 
         SSOToken adminSSOToken = getAdminSSOToken();
         String realm = getStringOptionValue(IArgument.REALM_NAME);
         List idNames = (List)rc.getOption(ARGUMENT_ID_NAMES);
         String type = getStringOptionValue(ARGUMENT_ID_TYPE);
 
         String displayableIdNames = tokenize(idNames);
         String[] params = {realm, type, displayableIdNames};
        writeLog(LogWriter.LOG_ACCESS, Level.INFO,
            "ATTEMPT_DELETE_IDENTITY", params);

        // test if realm exists
        try {
            new OrganizationConfigManager(adminSSOToken, realm);
        } catch (SMSException e) {
            String[] args = {realm, type, displayableIdNames, e.getMessage()};
            debugError("DeleteIdentities.handleRequest", e);
            writeLog(LogWriter.LOG_ERROR, Level.INFO, "FAILED_DELETE_IDENTITY",
                args);
            Object[] msgArg = {realm};
            throw new CLIException(MessageFormat.format(getResourceString(
                "delete-identity-realm-does-not-exist"), msgArg),
                ExitCodes.REQUEST_CANNOT_BE_PROCESSED);
        }
 
         try {
             AMIdentityRepository amir = new AMIdentityRepository(
                 adminSSOToken, realm);
             IdType idType = convert2IdType(type);
             Set setDelete = new HashSet();
 
             for (Iterator i = idNames.iterator(); i.hasNext(); ) {
                 String idName = (String)i.next();
                 AMIdentity amid = new AMIdentity(
                     adminSSOToken, idName, idType, realm, null); 
                 setDelete.add(amid);
             }
 
             amir.deleteIdentities(setDelete);
             IOutput outputWriter = getOutputWriter();
 
             Object[] objects = {realm, type};
             if (idNames.size() > 1) {
                 outputWriter.printlnMessage(MessageFormat.format(
                     getResourceString("delete-identities-succeed"), objects));
             } else {
                 outputWriter.printlnMessage(MessageFormat.format(
                     getResourceString("delete-identity-succeed"), objects));
             }
 
             for (Iterator i = idNames.iterator(); i.hasNext(); ) {
                 outputWriter.printlnMessage("    " + (String)i.next());
             }
 
             writeLog(LogWriter.LOG_ACCESS, Level.INFO, 
                 "SUCCEED_DELETE_IDENTITY", params);
         } catch (IdRepoException e) {
             String[] args = {realm, type, displayableIdNames, e.getMessage()};
             debugError("DeleteIdentities.handleRequest", e);
             writeLog(LogWriter.LOG_ERROR, Level.INFO, "FAILED_DELETE_IDENTITY",
                 args);
             throw new CLIException(e, ExitCodes.REQUEST_CANNOT_BE_PROCESSED);
         } catch (SSOException e) {
             String[] args = {realm, type, displayableIdNames, e.getMessage()};
             debugError("DeleteIdentities.handleRequest", e);
             writeLog(LogWriter.LOG_ERROR, Level.INFO, "FAILED_DELETE_IDENTITY",
                 args);
             throw new CLIException(e, ExitCodes.REQUEST_CANNOT_BE_PROCESSED);
         }
     }
 }
