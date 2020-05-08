 /**
  * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
  *
  * Copyright (c) 2008 Sun Microsystems Inc. All Rights Reserved
  *
  * The contents of this file are subject to the terms
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
 * $Id: PolicyPriviligeManager.java,v 1.2 2009-03-03 03:54:10 dillidorai Exp $
  */
 package com.sun.identity.policy;
 
import com.iplanet.sso.SSOException;
 import com.sun.identity.entitlement.EntitlementException;
 import com.sun.identity.entitlement.Privilige;
 import com.sun.identity.entitlement.PriviligeManager;
 import javax.security.auth.Subject;
 
 /**
  * Implementaton of <code>PriviligeManager</code> that saves priviliges
  * as <code>com.sun.identity.policy</code> objects
  */
 public class PolicyPriviligeManager extends PriviligeManager {
 
     /**
      * Creates instance of <code>PolicyPriviligeManager</code>
      */
     public PolicyPriviligeManager() {
     }
 
     /**
      * Initializes the object
      * @param subject subject that would be used for privilige management
      * operations
      */
     public void initialize(Subject subject) {
     }
 
     /**
      * Adds a privilige
      * @param privilige privilige to be added
      * @throws com.sun.identity.entitlement.EntitlementException if the
      * privilige could not be added
      */
     public void addPrivilige(Privilige privilige)
             throws EntitlementException {
         try {
             Policy policy = PriviligeUtils.priviligeToPolicy(privilige);
         } catch (PolicyException pe) {
        } catch(SSOException ssoe) {
            
         }
     }
 
     /**
      * Removes a privilige
      * @param priviligeName name of the privilige to be removed
      * @throws com.sun.identity.entitlement.EntitlementException
      */
     public void removePrivilige(String priviligeName)
             throws EntitlementException {
     }
 
     /**
      * Modifies a privilige
      * @param privilige the privilige to be modified
      * @throws com.sun.identity.entitlement.EntitlementException
      */
     public void modifyPrivilige(Privilige privilige)
             throws EntitlementException {
     }
 }
