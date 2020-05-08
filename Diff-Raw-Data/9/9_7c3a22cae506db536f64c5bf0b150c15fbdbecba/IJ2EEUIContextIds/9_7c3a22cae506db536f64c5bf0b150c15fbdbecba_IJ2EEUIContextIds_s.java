 /*******************************************************************************
  * Copyright (c) 2003, 2006 IBM Corporation and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  * IBM Corporation - initial API and implementation
  *******************************************************************************/
 package org.eclipse.jst.j2ee.internal.actions;
 
 import org.eclipse.jst.j2ee.internal.plugin.J2EEUIPlugin;
 
 
 /**
  * Insert the type's description here. Creation date: (9/6/2001 12:23:02 PM)
  * 
  * @author: Administrator
  */
 public interface IJ2EEUIContextIds {
 	//	 New creation wizards
 	public static final String NEW_EAR_WIZARD_P1 = J2EEUIPlugin.PLUGIN_ID + ".EAR_NEW_EAR_WIZARD_PAGE1"; //$NON-NLS-1$
 	public static final String NEW_EAR_ADD_MODULES_PAGE = J2EEUIPlugin.PLUGIN_ID + ".NEW_EAR_ADD_MODULES_PAGE"; //$NON-NLS-1$
 	public static final String NEW_EAR_COMP_PAGE = J2EEUIPlugin.PLUGIN_ID + ".NEW_EAR_COMP_PAGE"; //$NON-NLS-1$
 	public static final String EAR_NEW_MODULE_PROJECTS_PAGE = J2EEUIPlugin.PLUGIN_ID + ".EAR_NEW_MODULE_PROJECTS_PAGE"; //$NON-NLS-1$
 	public static final String NEW_APPCLIENT_WIZARD_P1 = J2EEUIPlugin.PLUGIN_ID + ".APPCLIENT_NEW_APPCLIENT_WIZARD_PAGE1"; //$NON-NLS-1$
 	public static final String NEW_APPCLIENT_WIZARD_P3 = J2EEUIPlugin.PLUGIN_ID + ".APPCLIENT_NEW_APPCLIENT_WIZARD_PAGE3"; //$NON-NLS-1$
 	public static final String NEW_EJB_WIZARD_P1 = J2EEUIPlugin.PLUGIN_ID + ".EJB_NEW_EJB_WIZARD_PAGE1"; //$NON-NLS-1$
 	public static final String NEW_EJB_WIZARD_P2 = J2EEUIPlugin.PLUGIN_ID + ".EJB_NEW_EJB_WIZARD_PAGE2"; //$NON-NLS-1$
 	public static final String NEW_EJB_WIZARD_P3 = J2EEUIPlugin.PLUGIN_ID + ".EJB_NEW_EJB_WIZARD_PAGE3"; //$NON-NLS-1$
 	public static final String NEW_CONNECTOR_WIZARD_P1 = J2EEUIPlugin.PLUGIN_ID + ".JCA_NEWIZARD_PAGE1"; //$NON-NLS-1$
 	public static final String NEW_CONNECTOR_WIZARD_P3 = J2EEUIPlugin.PLUGIN_ID + ".JCA_NEWIZARD_PAGE3"; //$NON-NLS-1$
 	public static final String NEW_JAVA_COMPONENT_WIZARD_P1 = J2EEUIPlugin.PLUGIN_ID + ".NEW_JAVA_COMPONENT_WIZARD_PAGE1"; //$NON-NLS-1$
 	public static final String NEW_JAVA_CLASS_OPTION_WIZARD_P1 = J2EEUIPlugin.PLUGIN_ID + ".NEW_JAVA_CLASS_OPTION_WIZARD_PAGE1"; //$NON-NLS-1$
 	public static final String NEW_UTILITY_WIZARD_P1 = J2EEUIPlugin.PLUGIN_ID + ".NEW_UTILITY_WIZARD_P1"; //$NON-NLS-1$
 	public static final String NEW_UTILITY_WIZARD_P3 = J2EEUIPlugin.PLUGIN_ID + ".NEW_UTILITY_WIZARD_P3"; //$NON-NLS-1$
 	
 	// Import, export wizards
 	public static final String IMPORT_EAR_WIZARD_P1 = J2EEUIPlugin.PLUGIN_ID + ".EAR_IMPORT_EAR_WIZARD_PAGE1"; //$NON-NLS-1$
 	public static final String IMPORT_EAR_WIZARD_P2 = J2EEUIPlugin.PLUGIN_ID + ".EAR_IMPORT_EAR_WIZARD_PAGE2"; //$NON-NLS-1$
 	public static final String IMPORT_EAR_WIZARD_P3 = J2EEUIPlugin.PLUGIN_ID + ".EAR_IMPORT_EAR_WIZARD_PAGE3"; //$NON-NLS-1$
 	public static final String IMPORT_APPCLIENT_WIZARD_P1 = J2EEUIPlugin.PLUGIN_ID + ".APPCLIENT_IMPORT_APPCLIENT_WIZARD_PAGE1"; //$NON-NLS-1$
 	public static final String IMPORT_EJB_WIZARD_P1 = J2EEUIPlugin.PLUGIN_ID + ".EJB_IMPORT_EJB_WIZARD_PAGE1"; //$NON-NLS-1$
 	public static final String IMPORT_RAR_WIZARD_PAGE = J2EEUIPlugin.PLUGIN_ID + ".IMPORT_RAR_WIZARD_PAGE"; //$NON-NLS-1$
 	public static final String IMPORT_UTILITY_JAR_WIZARD_P1 = J2EEUIPlugin.PLUGIN_ID + ".IMPORT_UTILITY_JAR_WIZARD_PAGE1"; //$NON-NLS-1$
 	public static final String IMPORT_UTILITY_JAR_WIZARD_P2 = J2EEUIPlugin.PLUGIN_ID + ".IMPORT_UTILITY_JAR_WIZARD_PAGE2"; //$NON-NLS-1$
 	public static final String IMPORT_CLASS_WIZARD_P1 = J2EEUIPlugin.PLUGIN_ID + ".IMPORT_CLASS_WIZARD_PAGE1"; //$NON-NLS-1$
 	public static final String IMPORT_CLASS_WIZARD_P2 = J2EEUIPlugin.PLUGIN_ID + ".IMPORT_CLASS_WIZARD_PAGE2"; //$NON-NLS-1$
 
 	public static final String EXPORT_EAR_WIZARD_P1 = J2EEUIPlugin.PLUGIN_ID + ".EAR_EXPORT_PAGE1"; //$NON-NLS-1$ 
 	public static final String EXPORT_APPCLIENT_WIZARD_P1 = J2EEUIPlugin.PLUGIN_ID + ".APPCLIENT_EXPORT_APPCLIENT_WIZARD_PAGE1"; //$NON-NLS-1$
 	public static final String EXPORT_EJB_WIZARD_P1 = J2EEUIPlugin.PLUGIN_ID + ".EJB_EXPORT_PAGE1"; //$NON-NLS-1$
 	public static final String EXPORT_RAR_WIZARD_PAGE = J2EEUIPlugin.PLUGIN_ID + ".EXPORT_RAR_WIZARD_PAGE"; //$NON-NLS-1$
 
 	// dialogs
 	public static final String DELEATE_EAR_DIALOG_1 = J2EEUIPlugin.PLUGIN_ID + ".navm2000"; //$NON-NLS-1$
 	public static final String DELEATE_MODULE_DIALOG_1 = J2EEUIPlugin.PLUGIN_ID + ".navm2010"; //$NON-NLS-1$
 	public static final String RENAME_EAR_DIALOG_1 = J2EEUIPlugin.PLUGIN_ID + ".navm3000"; //$NON-NLS-1$
 	public static final String RENAME_MODULE_DIALOG_1 = J2EEUIPlugin.PLUGIN_ID + ".navm3010"; //$NON-NLS-1$
 
 }
