 /**
  * Copyright (c) 2011, 2012 Ericsson AB and others.
  * 
  * All rights reserved. This program and the accompanying materials are
  * made available under the terms of the Eclipse Public License v1.0 which
  * accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * Contributors:
  *    Ericsson AB - Provides resources from resource bundle.
  * 
  */
 package org.eclipse.mylyn.reviews.r4e.mail.smtp.mailVersion.internal;
 
 import java.text.MessageFormat;
 import java.util.MissingResourceException;
 import java.util.ResourceBundle;
 
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.core.runtime.Status;
 import org.eclipse.mylyn.commons.core.StatusHandler;
 import org.eclipse.mylyn.reviews.r4e.mail.smtp.SmtpPlugin;
 
 /**
  * 
  * 
  * @author Jacques Bouthillier
 * @version $Revision: 1.0 $
  */
 public class SMTPHostString {
 	/**
 	 * Field FBUNDLE_NAME.
 	 * (value is ""org.eclipse.mylyn.reviews.r4e.mail.smtp.R4EString"")
 	 */
	private static final String FBUNDLE_NAME = "org.eclipse.mylyn.reviews.r4e.mail.smtp.SMTPHostString";
 
 	/**
 	 * Field RESOURCE_BUNDLE.
 	 */
	private static final ResourceBundle RESOURCE_BUNDLE = ResourceBundle.getBundle(FBUNDLE_NAME);
 
 	/**
 	 * Private Constructor
 	 */
 	private SMTPHostString() {
 		//Prevents clients from instantiating this class.
 	}
 
 	/**
 	 * Gets the key from the value, or 'value' if not found.
 	 * 
 	 * @param key the value to search value.
 	 * @return the key from the value, or 'value' if not found. */
 	public static String getString(String key) {
 		try {
 			return RESOURCE_BUNDLE.getString(key);
 		} catch (MissingResourceException e) {
 			StatusHandler.log(new Status(IStatus.ERROR, SmtpPlugin.FPLUGIN_ID, IStatus.OK, e.toString(), e));
 			return '!' + key + '!';
 		}
 	}
 
 	/**
 	 * Format the String according to the object pass
 	 * 
 	 * @param key
 	 *            String
 	 * @param arg
 	 *            Object
	 * @return the key from the value, or 'value' if not found.
	 */
 	public static String getFormattedString(String key, Object arg) {
 		String format = null;
 		try {
 			format = RESOURCE_BUNDLE.getString(key);
 		} catch (MissingResourceException e) {
 			StatusHandler.log(new Status(IStatus.ERROR, SmtpPlugin.FPLUGIN_ID, IStatus.OK, e.toString(), e));
 			return "!" + key + "!";//$NON-NLS-2$ //$NON-NLS-1$ 
 		}
 		if (null == arg) {
 			arg = ""; //$NON-NLS-1$ 
 		}
 		return MessageFormat.format(format, new Object[] { arg });
 	}
 
 	/**
 	 * Format the String according to the object pass
 	 * 
 	 * @param key
 	 *            String
 	 * @param args String[]
	 * @return the key from the value, or 'value' if not found.
	 */
 	public static String getFormattedString(String key, String[] args) {
		return MessageFormat.format(RESOURCE_BUNDLE.getString(key), args);
 	}
 }
