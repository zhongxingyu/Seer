 /*******************************************************************************
  * Copyright (c) 2000, 2004 IBM Corporation and others.
  * All rights reserved. This program and the accompanying materials 
  * are made available under the terms of the Common Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/cpl-v10.html
  * 
  * Contributors:
  *     IBM Corporation - initial API and implementation
  *******************************************************************************/
 package org.rubypeople.rdt.internal.ui.rubyeditor;
 
 import java.text.MessageFormat;
 import java.util.ResourceBundle;
 
 import org.eclipse.osgi.util.NLS;
 
 public class RubyEditorMessages extends NLS {
 
     private static final String BUNDLE_NAME = RubyEditorMessages.class.getName();
 
     public static String RubyOutlinePage_Sort_label;
     public static String RubyOutlinePage_Sort_tooltip;
     public static String RubyOutlinePage_Sort_description;
     public static String RubyOutlinePage_GoIntoTopLevelType_label;
     public static String RubyOutlinePage_GoIntoTopLevelType_tooltip;
     public static String RubyOutlinePage_GoIntoTopLevelType_description;
     public static String RubyOutlinePage_error_NoTopLevelType;
     public static String GotoMatchingBracket_label;
     public static String GotoMatchingBracket_error_bracketOutsideSelectedElement;
     public static String GotoMatchingBracket_error_invalidSelection;
     public static String GotoMatchingBracket_error_noMatchingBracket;
 	public static String Editor_FoldingMenu_name;
 	public static String ToggleComment_error_title;
 	public static String ToggleComment_error_message;
	public static String CompletionProcessor_ContextInfo_value_pattern;
	public static String CompletionProcessor_ContextInfo_display_pattern;
 	public static String EditorUtility_concatModifierStrings;
 	
     private static ResourceBundle fgResourceBundle = ResourceBundle.getBundle(BUNDLE_NAME);
 
     private static final String BUNDLE_FOR_CONSTRUCTED_KEYS= "org.rubypeople.rdt.internal.ui.rubyeditor.ConstructedRubyEditorMessages";//$NON-NLS-1$
     private static ResourceBundle fgBundleForConstructedKeys= ResourceBundle.getBundle(BUNDLE_FOR_CONSTRUCTED_KEYS);
 
	public static String ShowRDoc_label;

     /**
      * Returns the message bundle which contains constructed keys.
      *
      * @since 0.8.0
      * @return the message bundle
      */
     public static ResourceBundle getBundleForConstructedKeys() {
         return fgBundleForConstructedKeys;
     }
 
     
     private RubyEditorMessages() {
     }
 
 
     public static ResourceBundle getResourceBundle() {
         return fgResourceBundle;
     }
 
     /**
      * Gets a string from the resource bundle and formats it with arguments
      */
     public static String getFormattedString(String key, Object[] args) {
         return MessageFormat.format(key, args);
     }
 
     /**
      * Gets a string from the resource bundle and formats it with arguments
      */
     public static String getFormattedString(String key, Object arg) {
         return MessageFormat.format(key, new Object[] { arg});
     }
 
     static {
         NLS.initializeMessages(BUNDLE_NAME, RubyEditorMessages.class);
     }
 
 }
