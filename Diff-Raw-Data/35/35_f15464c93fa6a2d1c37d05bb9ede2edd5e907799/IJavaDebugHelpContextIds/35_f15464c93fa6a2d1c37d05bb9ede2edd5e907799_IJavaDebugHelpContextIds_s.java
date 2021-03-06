 package org.eclipse.jdt.internal.debug.ui;
 
/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
 
 import org.eclipse.jdt.debug.ui.IJavaDebugUIConstants;
 
 /**
  * Help context ids for the Java Debug UI.
  * <p>
  * This interface contains constants only; it is not intended to be implemented
  * or extended.
  * </p>
  * 
  */
 public interface IJavaDebugHelpContextIds {
 	
 	public static final String PREFIX= IJavaDebugUIConstants.PLUGIN_ID + '.';
 
 	// view parts
 	public static final String DISPLAY_VIEW= PREFIX + "display_view_context"; //$NON-NLS-1$
 
 	//dialogs
 	public static final String EDIT_JRE_DIALOG= PREFIX + "edit_jre_dialog_context"; //$NON-NLS-1$
 
 	// Preference/Property pages
 	public static final String JRE_PREFERENCE_PAGE= PREFIX + "jre_preference_page_context"; //$NON-NLS-1$
 	public static final String LAUNCH_JRE_PROPERTY_PAGE= PREFIX + "launch_jre_property_page_context"; //$NON-NLS-1$
 	public static final String JAVA_DEBUG_PREFERENCE_PAGE= PREFIX + "java_debug_preference_page_context"; //$NON-NLS-1$
 	public static final String JAVA_DEBUG_APPEARANCE_PREFERENCE_PAGE= PREFIX + "java_debug_appearance_preference_page_context"; //$NON-NLS-1$	
 	public static final String JAVA_STEP_FILTER_PREFERENCE_PAGE= PREFIX + "java_step_filter_preference_page_context"; //$NON-NLS-1$
 	
 	// reused ui-blocks
 	public static final String SOURCE_ATTACHMENT_BLOCK= PREFIX + "source_attachment_context"; //$NON-NLS-1$
 	public static final String WORKING_DIRECTORY_BLOCK= PREFIX + "working_directory_context"; //$NON-NLS-1$
 
 	// launch configuration dialog tabs
 	public static final String LAUNCH_CONFIGURATION_DIALOG_ARGUMENTS_TAB= PREFIX + "launch_configuration_dialog_arguments_tab"; //$NON-NLS-1$
 	public static final String LAUNCH_CONFIGURATION_DIALOG_CLASSPATH_TAB= PREFIX + "launch_configuration_dialog_classpath_tab"; //$NON-NLS-1$
 	public static final String LAUNCH_CONFIGURATION_DIALOG_CONNECT_TAB= PREFIX + "launch_configuration_dialog_connect_tab"; //$NON-NLS-1$
 	public static final String LAUNCH_CONFIGURATION_DIALOG_JRE_TAB= PREFIX + "launch_configuration_dialog_jre_tab"; //$NON-NLS-1$
 	public static final String LAUNCH_CONFIGURATION_DIALOG_MAIN_TAB= PREFIX + "launch_configuration_dialog_main_tab"; //$NON-NLS-1$
 	public static final String LAUNCH_CONFIGURATION_DIALOG_SOURCE_TAB= PREFIX + "launch_configuration_dialog_source_tab"; //$NON-NLS-1$
 	
 }
