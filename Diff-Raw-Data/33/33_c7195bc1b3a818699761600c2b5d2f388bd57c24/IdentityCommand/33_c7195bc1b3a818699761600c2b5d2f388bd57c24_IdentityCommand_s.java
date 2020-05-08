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
 * $Id: IdentityCommand.java,v 1.2 2007-02-02 18:05:34 veiming Exp $
  *
  * Copyright 2006 Sun Microsystems Inc. All Rights Reserved
  */
 
 package com.sun.identity.cli.idrepo;
 
 
import com.iplanet.sso.SSOException;
 import com.sun.identity.cli.AuthenticatedCommand;
 import com.sun.identity.cli.CLIException;
 import com.sun.identity.cli.ExitCodes;
 import com.sun.identity.cli.RequestContext;
 import com.sun.identity.delegation.DelegationPrivilege;
import com.sun.identity.idm.AMIdentity;
import com.sun.identity.idm.AMIdentityRepository;
 import com.sun.identity.idm.IdRepoException;
import com.sun.identity.idm.IdSearchControl;
import com.sun.identity.idm.IdSearchResults;
 import com.sun.identity.idm.IdType;
import java.text.MessageFormat;
 import java.util.Iterator;
 import java.util.Set;
 
 /**
  * Base class for Identity command.
  */
 public abstract class IdentityCommand extends AuthenticatedCommand {
     /**
      * Identity name argument name.
      */
     public static final String ARGUMENT_ID_NAME = "idname";
     
     /**
      * Identity names argument name.
      */
     public static final String ARGUMENT_ID_NAMES = "idnames";
     
     /**
      * Identity type argument name.
      */
     public static final String ARGUMENT_ID_TYPE = "idtype";
     /**
      * Member identity name argument name.
      */
     public static final String ARGUMENT_MEMBER_IDNAME = "memberidname";
 
     /**
      * Member identity type argument name.
      */
     public static final String ARGUMENT_MEMBER_IDTYPE = "memberidtype";
 
     /**
      * Services a Commandline Request.
      *
      * @param rc Request Context.
      * @throw CLIException if the request cannot serviced.
      */
     public void handleRequest(RequestContext rc) 
         throws CLIException {
         super.handleRequest(rc);
         ldapLogin();
     }
     
     protected IdType convert2IdType(String idType)
         throws CLIException
     {
        if (IdType.USER.getName().equalsIgnoreCase(idType)) {
            return IdType.USER;
        } else if (IdType.AGENT.getName().equalsIgnoreCase(idType)) {
            return IdType.AGENT;
        } else if (IdType.FILTEREDROLE.getName().equalsIgnoreCase(idType)) {
            return IdType.FILTEREDROLE;
        } else if (IdType.GROUP.getName().equalsIgnoreCase(idType)) {
            return(IdType.GROUP);
        } else if (IdType.ROLE.getName().equalsIgnoreCase(idType)) {
            return(IdType.ROLE);
        } else {
            String[] arg = {idType};
            throw new CLIException(MessageFormat.format(getResourceString(
                "invalid-identity-type"), (Object[])arg), 
                 ExitCodes.REQUEST_CANNOT_BE_PROCESSED);
         }
     }
 
     protected DelegationPrivilege getDelegationPrivilege(
         String name,
         Set privilegeObjects
     ) {
         DelegationPrivilege dp = null;
         for (Iterator i= privilegeObjects.iterator();
             i.hasNext() && (dp == null);
         ) {
             DelegationPrivilege p = (DelegationPrivilege)i.next();
             if (p.getName().equals(name)) {
                 dp = p;
             }
         }
         return dp;
     }
 }
