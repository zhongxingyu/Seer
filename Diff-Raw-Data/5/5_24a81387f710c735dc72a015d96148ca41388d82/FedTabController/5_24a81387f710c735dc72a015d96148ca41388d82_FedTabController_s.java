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
 * $Id: FedTabController.java,v 1.2 2007-02-07 21:48:27 jonnelson Exp $
  *
  * Copyright 2006 Sun Microsystems Inc. All Rights Reserved
  */
 
 package com.sun.identity.console.controller;
 
 /**
  * This class determines whether to show/hide the federation tab.
  */
 public class FedTabController
     extends TabControllerBase
 {
     private static FedTabController privateInstance = new FedTabController();
 
    private static FEDERATION_ENABLED = 
         "iplanet-am-admin-console-liberty-enabled";
 
     static {
 	privateInstance.addListener();
 	privateInstance.updateStatus();
     }
 
     /**
      * Returns true if tab is visible.
      *
      * @return true if tab is visible.
      */
     public boolean isVisible() {
 	return privateInstance.visible;
     }
 
     protected String getConfigAttribute() {
 	return FEDERATION_ENABLED;
     }
 }
