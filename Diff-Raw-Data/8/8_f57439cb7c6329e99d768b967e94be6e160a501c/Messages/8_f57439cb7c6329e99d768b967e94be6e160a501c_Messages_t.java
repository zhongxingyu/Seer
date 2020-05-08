 /*******************************************************************************
  * Copyright (c) 2013 BREDEX GmbH.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     BREDEX GmbH - initial API and implementation and/or initial documentation
  *******************************************************************************/
 package org.eclipse.jubula.client.alm.mylyn.core.i18n;
 
 import org.eclipse.osgi.util.NLS;
 
 /**
  * i18n string internationalization
  */
 public class Messages extends NLS {
     private static final String BUNDLE_NAME = "org.eclipse.jubula.client.alm.mylyn.core.i18n.messages"; //$NON-NLS-1$
     
     public static String TaskAttributeNotFound;
     public static String UnsupportedTaskAttribute;
     public static String TaskRepositoryNotFound;
     public static String TaskRepositoryOffline;
     public static String TaskRepositoryNoCredentialsStored;
     public static String TaskRepositoryNoConnectorFound;
     public static String NodeComment;
     public static String StatusPassed;
     public static String StatusFailed;
     public static String NothingToReport;
    public static String ReportToALMJobName;
     public static String ReportToALMJob;
     public static String ReportToALMJobDone;
     public static String ReportingResult;
     public static String ReportingResults;
     public static String ReportingTaskFailed;
     public static String NotAvailable;
     public static String TaskRepositoryConnectionTest;
     public static String TaskRepositoryConnectionTestFailed;
     public static String TaskRepositoryConnectionTestSucceeded;
     
     static {
         NLS.initializeMessages(BUNDLE_NAME, Messages.class);
     }
     /** Constructor */
     private Messages() {
     }
 }
