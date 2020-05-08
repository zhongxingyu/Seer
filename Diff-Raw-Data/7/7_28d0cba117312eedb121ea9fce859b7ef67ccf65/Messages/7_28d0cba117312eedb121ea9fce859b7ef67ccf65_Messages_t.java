 /**
  * Copyright (c) 2008, 2009, Oracle and/or its affiliates. All rights reserved.
  */
 
 
 package org.eclipse.jst.jsf.common.facet;
 
 
 import org.eclipse.osgi.util.NLS;
 
 
 /**
  * Resource bundle
  * 
  * @author Debajit Adhikary
  */
 public class Messages extends NLS
 {
     private static final String BUNDLE_NAME = "org.eclipse.jst.jsf.common.facet.messages"; //$NON-NLS-1$
 
    /**
     * see messages.properties
     */
     public static String UserLibraryVersionValidator_cannotReadLibraryVersion;
 
    /**
     * see messages.properties
     */
     public static String UserLibraryVersionValidator_versionMismatch;
 
     static
     {
         // initialize resource bundle
         NLS.initializeMessages(BUNDLE_NAME, Messages.class);
     }
 
 
     private Messages ()
     {
         //
     }
 }
