 /*******************************************************************************
  * Copyright (c) 2004, 2010 BREDEX GmbH.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     BREDEX GmbH - initial API and implementation and/or initial documentation
  *******************************************************************************/
 package org.eclipse.jubula.tools.i18n;
 
 import java.text.MessageFormat;
 import java.util.MissingResourceException;
 import java.util.ResourceBundle;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.eclipse.jubula.tools.constants.StringConstants;
 
 /**
  * @author BREDEX GmbH
  * @created 08.09.2004
  */
 public class I18n {
     /** the logger */
     private static Log log = LogFactory.getLog(I18n.class);
 
     /**
      * Name of the Bundle.
      */
     private static final String BUNDLE_NAME = "org.eclipse.jubula.tools.i18n.guidancerStrings"; //$NON-NLS-1$
 
     /**
      * Resource bundle, contains locale-specific objects.
      */
     private static ResourceBundle resourceBundle = null;
     static {
         try {
             resourceBundle = ResourceBundle.getBundle(BUNDLE_NAME);
         } catch (MissingResourceException mre) {
             log.error("Cannot find I18N-resource bundle!"); //$NON-NLS-1$
         }
     }
 
     /**
      * Constructor
      */
     private I18n() {
         // private constructor to prevent instantiation of class utitlity
     }
 
     /**
      * Gets the internationalized String by a given key.
      * 
      * @param key
      *            the key for the internationalized String.
      * @return a internationalized <code>String</code>.
      */
     public static String getString(String key) {
         return getString(key, true);
     }
 
     /**
      * Gets the internationalized String by a given key.
      * 
      * @param key
      *            the key for the internationalized String.
      * @param fallBack
      *            returns the key if no value found
      * @return a internationalized <code>String</code>.
      */
     public static String getString(String key, boolean fallBack) {
         if (key == null) {
             return StringConstants.EMPTY;
         }
         if (StringConstants.EMPTY.equals(key)) {
             return key;
         }
         String str = StringConstants.EMPTY;
         try {
             str = resourceBundle.getString(key);
         } catch (MissingResourceException mre) {
             if (fallBack) {
                 return key;
             }
         }
         return str;
     }
 
     /**
      * returns an internationalized string for the given key
      * 
      * @param key
      *            the key
      * @param args
      *            the arguments needed to generate the string
      * @return the internationalized string
      */
     public static String getString(String key, Object[] args) {
         if (StringConstants.EMPTY.equals(key)) {
             return key;
         }
         try {
             MessageFormat formatter = new MessageFormat(
                     resourceBundle.getString(key));
             return formatter.format(args);
         } catch (MissingResourceException e) {
             log.error(e.toString());
             StringBuffer buf = new StringBuffer(key);
            for (int i = 0; args != null && i < args.length; i++) {
                 if (args[i] != null) {
                    buf.append(StringConstants.SPACE);
                     buf.append(args[i]);
                 }
             }
             return buf.toString();
         }
     }
 }
