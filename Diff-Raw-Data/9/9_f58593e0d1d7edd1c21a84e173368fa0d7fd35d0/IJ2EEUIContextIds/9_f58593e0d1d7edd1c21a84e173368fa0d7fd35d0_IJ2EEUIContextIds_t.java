 /*******************************************************************************
  * Copyright (c) 2003, 2004 IBM Corporation and others.
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
 
 	// common stuff
 	public static final String ICON_DIALOG = J2EEUIPlugin.PLUGIN_ID + ""; //$NON-NLS-1$
 	public static final String ADD_SECURITY_ROLE_WIZARD_1 = J2EEUIPlugin.PLUGIN_ID + ""; //$NON-NLS-1$
 
 	//J2EE project view
	public static final String J2EE_PROEJCT_VIEW_P1 = "org.eclipse.wst.navigator.ui.WTPWorkingSetCommonNavigator"; //$NON-NLS-1$
 
 	//jar dependency editor, page - 3 Pages
 	public static final String JAR_DEPENDENCIES_EDITOR_P1 = J2EEUIPlugin.PLUGIN_ID + ".EJB_JAR_DEPENDENCIES_EDITOR_PAGE1"; //$NON-NLS-1$
 	public static final String JAR_DEPENDENCIES_EDITOR_P2 = J2EEUIPlugin.PLUGIN_ID + ".EJB_JAR_DEPENDENCIES_EDITOR_PAGE2"; //$NON-NLS-1$
 	public static final String JAR_DEPENDENCIES_EDITOR_P3 = J2EEUIPlugin.PLUGIN_ID + ".EJB_JAR_DEPENDENCIES_EDITOR_PAGE3"; //$NON-NLS-1$
 	public static final String JAR_DEPENDENCIES_EDITOR_P4 = J2EEUIPlugin.PLUGIN_ID + ".EJB_JAR_DEPENDENCIES_EDITOR_PAGE4"; //$NON-NLS-1$
 	public static final String JAR_DEPENDENCIES_EDITOR_P6 = J2EEUIPlugin.PLUGIN_ID + ".JAR_DEPENDENCIES_EDITOR_PAGE6"; //$NON-NLS-1$
 
 	// New EAR project wizard, page 1
 	public static final String NEW_EAR_WIZARD_P1 = J2EEUIPlugin.PLUGIN_ID + ".EAR_NEW_EAR_WIZARD_PAGE1"; //$NON-NLS-1$
 	public static final String NEW_EAR_ADD_MODULES_PAGE = J2EEUIPlugin.PLUGIN_ID + ".NEW_EAR_ADD_MODULES_PAGE"; //$NON-NLS-1$
 	public static final String EAR_NEW_MODULE_PROJECTS_PAGE = J2EEUIPlugin.PLUGIN_ID + ".EAR_NEW_MODULE_PROJECTS_PAGE"; //$NON-NLS-1$
 
 	// New EJB project wizard, page - 3 Pages
 	public static final String NEW_EJB_WIZARD_P1 = J2EEUIPlugin.PLUGIN_ID + ".EJB_NEW_EJB_WIZARD_PAGE1"; //$NON-NLS-1$
 	public static final String NEW_EJB_WIZARD_P2 = J2EEUIPlugin.PLUGIN_ID + ".EJB_NEW_EJB_WIZARD_PAGE2"; //$NON-NLS-1$
 	public static final String NEW_EJB_WIZARD_P3 = J2EEUIPlugin.PLUGIN_ID + ".EJB_NEW_EJB_WIZARD_PAGE3"; //$NON-NLS-1$
 
 	//New App Client project Wizard - 3 Pages
 	public static final String NEW_APPCLIENT_WIZARD_P1 = J2EEUIPlugin.PLUGIN_ID + ".APPCLIENT_NEW_APPCLIENT_WIZARD_PAGE1"; //$NON-NLS-1$
 
 	//Import App Client wizard - 3 Pages
 	public static final String IMPORT_APPCLIENT_WIZARD_P1 = J2EEUIPlugin.PLUGIN_ID + ".APPCLIENT_IMPORT_APPCLIENT_WIZARD_PAGE1"; //$NON-NLS-1$
 
 	public static final String IMPORT_RAR_WIZARD_PAGE = J2EEUIPlugin.PLUGIN_ID + ".IMPORT_RAR_WIZARD_PAGE"; //$NON-NLS-1$
 	public static final String EXPORT_RAR_WIZARD_PAGE = J2EEUIPlugin.PLUGIN_ID + ".EXPORT_RAR_WIZARD_PAGE"; //$NON-NLS-1$
 
 	//Import EJB wizard - 3 Pages
 	public static final String IMPORT_EJB_WIZARD_P1 = J2EEUIPlugin.PLUGIN_ID + ".EJB_IMPORT_EJB_WIZARD_PAGE1"; //$NON-NLS-1$
 
 	//Import EAR Wizard - 3 Pages
 	public static final String IMPORT_EAR_WIZARD_P1 = J2EEUIPlugin.PLUGIN_ID + ".EAR_IMPORT_EAR_WIZARD_PAGE1"; //$NON-NLS-1$
 	public static final String IMPORT_EAR_WIZARD_P2 = J2EEUIPlugin.PLUGIN_ID + ".EAR_IMPORT_EAR_WIZARD_PAGE2"; //$NON-NLS-1$
 	public static final String IMPORT_EAR_WIZARD_P3 = J2EEUIPlugin.PLUGIN_ID + ".EAR_IMPORT_EAR_WIZARD_PAGE3"; //$NON-NLS-1$
 	public static final String IMPORT_EAR_WIZARD_P4 = J2EEUIPlugin.PLUGIN_ID + ".EAR_IMPORT_EAR_WIZARD_PAGE4"; //$NON-NLS-1$
 	//Export App Client Wizard - 1 Page
 	public static final String EXPORT_APPCLIENT_WIZARD_P1 = J2EEUIPlugin.PLUGIN_ID + ".APPCLIENT_EXPORT_APPCLIENT_WIZARD_PAGE1"; //$NON-NLS-1$
 
 	//Delete Enterprise Bean Dialog
 	public static final String DELETE_ENTERPRISE_BEAN_DIALOG = "org.eclipse.wst.common.generic.Delete"; //$NON-NLS-1$
 
 	//Add Jar Wizard
 	public static final String ADD_JAR_WIZARD_P1 = J2EEUIPlugin.PLUGIN_ID + ".EJB_ADDJAR_PAGE1"; //$NON-NLS-1$
 
 	//Export EJB Wizard - 1 Page
 	public static final String EXPORT_EJB_WIZARD_P1 = J2EEUIPlugin.PLUGIN_ID + ".EJB_EXPORT_PAGE1"; //$NON-NLS-1$
 	//Export EAR Wizard - 1 Page
 	public static final String EXPORT_EAR_WIZARD_P1 = J2EEUIPlugin.PLUGIN_ID + ".EAR_EXPORT_PAGE1"; //$NON-NLS-1$ 
 	// New bean wizard, pages
 	public static final String NEW_BEAN_WIZARD_P1 = J2EEUIPlugin.PLUGIN_ID + ".EJB_NEWBEAN_WIZARD_PAGE1"; //$NON-NLS-1$   
 	public static final String NEW_BEAN_WIZARD_P2 = J2EEUIPlugin.PLUGIN_ID + ".NEW_BEAN_WIZARD_TYPES_SELECTION"; //$NON-NLS-1$		
 	public static final String NEW_BEAN_WIZARD_P3 = J2EEUIPlugin.PLUGIN_ID + ".NEW_BEAN_WIZARD_BEAN_CLASS_SETTINGS"; //$NON-NLS-1$		
 	public static final String NEW_BEAN_WIZARD_JAVA_CLASS_SETTINGS = J2EEUIPlugin.PLUGIN_ID + ".NEW_BEAN_WIZARD_JAVA_CLASS_SETTINGS"; //$NON-NLS-1 //$NON-NLS-1$
 
 	public static final String ADD_CMP_ATTRIBUTE = J2EEUIPlugin.PLUGIN_ID + ".ADD_CMP_ATTRIBUTE"; //$NON-NLS-1$
 
 	//Relationship Wizard
 	public static final String RELATIONSHIP_WIZARD_P1 = J2EEUIPlugin.PLUGIN_ID + ".EJB_RELATIONSHIP_PAGE1"; //$NON-NLS-1$
 	public static final String RELATIONSHIP_WIZARD_P2 = J2EEUIPlugin.PLUGIN_ID + ".EJB_RELATIONSHIP_PAGE2"; //$NON-NLS-1$
 	//Add Environment Variable Wizard
 	public static final String ENVIRONMENT_WIZARD = J2EEUIPlugin.PLUGIN_ID + ".EJBAPPCLIENT_ENVIRONMENT_WIZARD"; //$NON-NLS-1$
 	//Add Client View
 	public static final String CLIENT_WIZARD_P1 = J2EEUIPlugin.PLUGIN_ID + ".EJB_CREATECLIENTVIEW_PAGE1"; //$NON-NLS-1$
 	public static final String CLIENT_WIZARD_P2 = J2EEUIPlugin.PLUGIN_ID + ".EJB_CREATECLIENTVIEW_PAGE2"; //$NON-NLS-1$
 	//EJBQL Query for EJB 2.0
 	public static final String QUERY_WIZARD_P1 = J2EEUIPlugin.PLUGIN_ID + ".EJB_QUERY_PAGE1"; //$NON-NLS-1$
 	public static final String QUERY_WIZARD_P2 = J2EEUIPlugin.PLUGIN_ID + ".EJB_QUERY_PAGE2"; //$NON-NLS-1$
 	//Add Security Role
 	public static final String SECURITY_ROLE_WIZARD_P1 = J2EEUIPlugin.PLUGIN_ID + ".SECURITY_ROLE_PAGE1"; //$NON-NLS-1$
 	//Add Method Permission
 	public static final String METHODS_WIZARD_P1 = J2EEUIPlugin.PLUGIN_ID + ".EJB_METHODSPERMISSION_PAGE1"; //$NON-NLS-1$
 	public static final String METHODS_WIZARD_P2 = J2EEUIPlugin.PLUGIN_ID + ".EJB_METHODSPERMISSION_PAGE2"; //$NON-NLS-1$  
 	public static final String METHODS_WIZARD_P3 = J2EEUIPlugin.PLUGIN_ID + ".EJB_METHODSPERMISSION_PAGE3"; //$NON-NLS-1$  
 	//Add Container Transaction
 	public static final String TRANSACTION_WIZARD_P1 = J2EEUIPlugin.PLUGIN_ID + ".EJB_METHODTRANSACTION_PAGE1"; //$NON-NLS-1$ //NF
 	public static final String TRANSACTION_WIZARD_P2 = J2EEUIPlugin.PLUGIN_ID + ".EJB_METHODTRANSACTION_PAGE2"; //$NON-NLS-1$
 	//Add Exclude List
 	public static final String EXCLUDE_WIZARD_P1 = J2EEUIPlugin.PLUGIN_ID + ".EJB_EXCLUDE_WIZARD_PAGE1"; //$NON-NLS-1$  
 	public static final String EXCLUDE_WIZARD_P2 = J2EEUIPlugin.PLUGIN_ID + ".EJB_EXCLUDE_WIZARD_PAGE2"; //$NON-NLS-1$	
 	//Add Reference
 	public static final String REFERENCE_WIZARD_P1 = J2EEUIPlugin.PLUGIN_ID + ".REFERENCE_WIZARD_PAGE1"; //$NON-NLS-1$
 	public static final String REFERENCE_WIZARD_P2 = J2EEUIPlugin.PLUGIN_ID + ".REFERENCE_WIZARD_PAGE2"; //$NON-NLS-1$
 	public static final String REFERENCE_WIZARD_P3 = J2EEUIPlugin.PLUGIN_ID + ".REFERENCE_WIZARD_PAGE3"; //$NON-NLS-1$ //NF
 	public static final String REFERENCE_WIZARD_P4 = J2EEUIPlugin.PLUGIN_ID + ".REFERENCE_WIZARD_PAGE4"; //$NON-NLS-1$
 	public static final String REFERENCE_WIZARD_P5 = J2EEUIPlugin.PLUGIN_ID + ".REFERENCE_WIZARD_PAGE5"; //$NON-NLS-1$
 	public static final String REFERENCE_WIZARD_P6 = J2EEUIPlugin.PLUGIN_ID + ".REFERENCE_WIZARD_PAGE6"; //$NON-NLS-1$
 	public static final String REFERENCE_WIZARD_P7 = J2EEUIPlugin.PLUGIN_ID + ".REFERENCE_WIZARD_PAGE7"; //$NON-NLS-1$
 
 	//Add Security Identity (Bean Level)
 	public static final String BEAN_SECURITY_WIZARD_P1 = J2EEUIPlugin.PLUGIN_ID + ".EJB_BEANSECURITYWIZARD_PAGE1"; //$NON-NLS-1$
 	public static final String BEAN_SECURITY_WIZARD_P2 = J2EEUIPlugin.PLUGIN_ID + ".EJB_BEANSECURITYWIZARD_PAGE2"; //$NON-NLS-1$
 
 	//Add Module
 	public static final String ADD_MODULE_WIZARD = J2EEUIPlugin.PLUGIN_ID + ".EAR_ADDMODULEWIZARD_PAGE1"; //$NON-NLS-1$
 	//Combine Roles
 	public static final String COMBINE_ROLES_WIZARD_PAGE1 = J2EEUIPlugin.PLUGIN_ID + ".EAR_COMBINEROLESWIZARD_PAGE1"; //$NON-NLS-1$
 	public static final String COMBINE_ROLES_WIZARD_PAGE2 = J2EEUIPlugin.PLUGIN_ID + ".EAR_COMBINEROLESWIZARD_PAGE2"; //$NON-NLS-1$
 	//New Connector Project
 	public static final String NEW_CONNECTOR_WIZARD_P1 = J2EEUIPlugin.PLUGIN_ID + ".JCA_NEWIZARD_PAGE1"; //$NON-NLS-1$
 
 	// V5 application editor
 	public static final String APP_EDITOR_OVERVIEW = J2EEUIPlugin.PLUGIN_ID + ".APP_EDITOR_OVERVIEW"; //$NON-NLS-1$
 	public static final String APP_EDITOR_OVERVIEW_ALERT = J2EEUIPlugin.PLUGIN_ID + ".APP_EDITOR_OVERVIEW_ALERT"; //$NON-NLS-1$
 	public static final String APP_EDITOR_OVERVIEW_GENERAL = J2EEUIPlugin.PLUGIN_ID + ".APP_EDITOR_OVERVIEW_GENERAL"; //$NON-NLS-1$
 	public static final String APP_EDITOR_OVERVIEW_MODULE = J2EEUIPlugin.PLUGIN_ID + ".APP_EDITOR_OVERVIEW_MODULE"; //$NON-NLS-1$
 	public static final String APP_EDITOR_OVERVIEW_SECURITY = J2EEUIPlugin.PLUGIN_ID + ".APP_EDITOR_OVERVIEW_SECURITY"; //$NON-NLS-1$
 	public static final String APP_EDITOR_OVERVIEW_ICON = J2EEUIPlugin.PLUGIN_ID + ".APP_EDITOR_OVERVIEW_ICON"; //$NON-NLS-1$
 	public static final String APP_EDITOR_MODULE = J2EEUIPlugin.PLUGIN_ID + ".APP_EDITOR_MODULE"; //$NON-NLS-1$
 	public static final String APP_EDITOR_MODULE_MODULE = J2EEUIPlugin.PLUGIN_ID + ".APP_EDITOR_MODULE_MODULE"; //$NON-NLS-1$
 	public static final String APP_EDITOR_MODULE_UTILITY = J2EEUIPlugin.PLUGIN_ID + ".APP_EDITOR_MODULE_UTILITY"; //$NON-NLS-1$
 	public static final String APP_EDITOR_SECURITY = J2EEUIPlugin.PLUGIN_ID + ".APP_EDITOR_SECURITY"; //$NON-NLS-1$
 
 	// V5 application client editor
 	public static final String APPCLIENT_EDITOR_OVERVIEW = J2EEUIPlugin.PLUGIN_ID + ".APPCLIENT_EDITOR_OVERVIEW"; //$NON-NLS-1$
 	public static final String APPCLIENT_EDITOR_OVERVIEW_ALERT = J2EEUIPlugin.PLUGIN_ID + ".APPCLIENT_EDITOR_OVERVIEW_ALERT"; //$NON-NLS-1$
 	public static final String APPCLIENT_EDITOR_OVERVIEW_GENERAL = J2EEUIPlugin.PLUGIN_ID + ".APPCLIENT_EDITOR_OVERVIEW_GENERAL"; //$NON-NLS-1$
 	public static final String APPCLIENT_EDITOR_OVERVIEW_ICON = J2EEUIPlugin.PLUGIN_ID + ".APPCLIENT_EDITOR_OVERVIEW_ICON"; //$NON-NLS-1$
 	public static final String APPCLIENT_EDITOR_OVERVIEW_REFERENCES = J2EEUIPlugin.PLUGIN_ID + ".APPCLIENT_EDITOR_OVERVIEW_REFERENCES"; //$NON-NLS-1$
 	public static final String APPCLIENT_EDITOR_OVERVIEW_ENV_VAR = J2EEUIPlugin.PLUGIN_ID + ".APPCLIENT_EDITOR_OVERVIEW_ENV_VAR"; //$NON-NLS-1$
 	public static final String APPCLIENT_EDITOR_OVERVIEW_CALLBACK = J2EEUIPlugin.PLUGIN_ID + ".APPCLIENT_EDITOR_OVERVIEW_CALLBACK"; //$NON-NLS-1$
 	public static final String APPCLIENT_EDITOR_REFERENCES = J2EEUIPlugin.PLUGIN_ID + ".APPCLIENT_EDITOR_REFERENCES"; //$NON-NLS-1$
 	public static final String APPCLIENT_EDITOR_OVERVIEW_MAINCLASS = J2EEUIPlugin.PLUGIN_ID + ".APPCLIENT_EDITOR_OVERVIEW_MAINCLASS"; //$NON-NLS-1$
 
 	// V5 EJB editor
 	public static final String EJB_EDITOR_OVERVIEW = J2EEUIPlugin.PLUGIN_ID + ".EJB_EDITOR_OVERVIEW"; //$NON-NLS-1$
 	public static final String EJB_EDITOR_OVERVIEW_ALERT = J2EEUIPlugin.PLUGIN_ID + ".EJB_EDITOR_OVERVIEW_ALERT"; //$NON-NLS-1$
 	public static final String EJB_EDITOR_OVERVIEW_GENERAL = J2EEUIPlugin.PLUGIN_ID + ".EJB_EDITOR_OVERVIEW_GENERAL"; //$NON-NLS-1$
 	public static final String EJB_EDITOR_OVERVIEW_USAGE = J2EEUIPlugin.PLUGIN_ID + ".EJB_EDITOR_OVERVIEW_USAGE"; //$NON-NLS-1$
 	public static final String EJB_EDITOR_OVERVIEW_BEAN = J2EEUIPlugin.PLUGIN_ID + ".EJB_EDITOR_OVERVIEW_BEAN"; //$NON-NLS-1$
 	public static final String EJB_EDITOR_OVERVIEW_ASSEMBLY = J2EEUIPlugin.PLUGIN_ID + ".EJB_EDITOR_OVERVIEW_ASSEMBLY"; //$NON-NLS-1$
 	public static final String EJB_EDITOR_OVERVIEW_EJBCLIENTJAR = J2EEUIPlugin.PLUGIN_ID + ".EJB_EDITOR_OVERVIEW_EJBCLIENTJAR"; //$NON-NLS-1$
 	public static final String EJB_EDITOR_OVERVIEW_REFERENCES = J2EEUIPlugin.PLUGIN_ID + ".EJB_EDITOR_OVERVIEW_REFERENCES"; //$NON-NLS-1$
 	public static final String EJB_EDITOR_OVERVIEW_ICON = J2EEUIPlugin.PLUGIN_ID + ".EJB_EDITOR_OVERVIEW_ICON"; //$NON-NLS-1$
 	public static final String EJB_EDITOR_OVERVIEW_RELATIONSHIP20 = J2EEUIPlugin.PLUGIN_ID + ".EJB_EDITOR_OVERVIEW_RELATIONSHIP20"; //$NON-NLS-1$
 	public static final String EJB_EDITOR_OVERVIEW_CMPFACTORY = J2EEUIPlugin.PLUGIN_ID + ".EJB_EDITOR_OVERVIEW_CMPFACTORY"; //$NON-NLS-1$
 
 	public static final String EJB_EDITOR_BEAN = J2EEUIPlugin.PLUGIN_ID + ".EJB_EDITOR_BEAN"; //$NON-NLS-1$
 	public static final String EJB_EDITOR_BEAN_BEAN = J2EEUIPlugin.PLUGIN_ID + ".EJB_EDITOR_BEAN_BEAN"; //$NON-NLS-1$
 	public static final String EJB_EDITOR_BEAN_CLASS = J2EEUIPlugin.PLUGIN_ID + ".EJB_EDITOR_BEAN_CLASS"; //$NON-NLS-1$
 	public static final String EJB_EDITOR_BEAN_ENVVAR = J2EEUIPlugin.PLUGIN_ID + ".EJB_EDITOR_BEAN_ENVVAR"; //$NON-NLS-1$
 	public static final String EJB_EDITOR_BEAN_ICON = J2EEUIPlugin.PLUGIN_ID + ".EJB_EDITOR_BEAN_ICON"; //$NON-NLS-1$
 	public static final String EJB_EDITOR_BEAN_QUERY = J2EEUIPlugin.PLUGIN_ID + ".EJB_EDITOR_BEAN_QUERY"; //$NON-NLS-1$
 
 	public static final String EJB_EDITOR_ASSEMBLY = J2EEUIPlugin.PLUGIN_ID + ".EJB_EDITOR_ASSEMBLY"; //$NON-NLS-1$
 	public static final String EJB_EDITOR_ASSEMBLY_SECURITY = J2EEUIPlugin.PLUGIN_ID + ".EJB_EDITOR_ASSEMBLY_SECURITY"; //$NON-NLS-1$
 	public static final String EJB_EDITOR_ASSEMBLY_PERMISSION = J2EEUIPlugin.PLUGIN_ID + ".EJB_EDITOR_ASSEMBLY_PERMISSION"; //$NON-NLS-1$
 	public static final String EJB_EDITOR_ASSEMBLY_TRANSACTION = J2EEUIPlugin.PLUGIN_ID + ".EJB_EDITOR_ASSEMBLY_TRANSACTION"; //$NON-NLS-1$
 	public static final String EJB_EDITOR_ASSEMBLY_EXCLUDE = J2EEUIPlugin.PLUGIN_ID + ".EJB_EDITOR_ASSEMBLY_EXCLUDE"; //$NON-NLS-1$
 	public static final String EJB_EDITOR_REFERENCES = J2EEUIPlugin.PLUGIN_ID + ".EJB_EDITOR_REFERENCES"; //$NON-NLS-1$
 	public static final String EJB_EDITOR_REFERENCES_EJB_REFERENCE = J2EEUIPlugin.PLUGIN_ID + ".EJB_EDITOR_REFERENCES_EJB_REFERENCE"; //$NON-NLS-1$
 	public static final String EJB_EDITOR_REFERENCES_EJB_LOCAL_REFERENCE = J2EEUIPlugin.PLUGIN_ID + ".EJB_EDITOR_REFERENCES_EJB_LOCAL_REFERENCE"; //$NON-NLS-1$
 	public static final String EJB_EDITOR_REFERENCES_RES_REFERENCE = J2EEUIPlugin.PLUGIN_ID + ".EJB_EDITOR_REFERENCES_RES_REFERENCE"; //$NON-NLS-1$
 	public static final String EJB_EDITOR_REFERENCES_SEC_ROLE_REFERENCE = J2EEUIPlugin.PLUGIN_ID + ".EJB_EDITOR_REFERENCES_SEC_ROLE_REFERENCE"; //$NON-NLS-1$
 	public static final String EJB_EDITOR_REFERENCES_RES_ENV_REFERENCE = J2EEUIPlugin.PLUGIN_ID + ".EJB_EDITOR_REFERENCES_RES_ENV_REFERENCE"; //$NON-NLS-1$
 	public static final String EJB_EDITOR_ACCESS = J2EEUIPlugin.PLUGIN_ID + ".EJB_EDITOR_ACCESS"; //$NON-NLS-1$
 
 	// V5 J2EE preference
 	public static final String J2EE_PREFERENCE = J2EEUIPlugin.PLUGIN_ID + ".J2EE_PREFERENCE_PAGE"; //$NON-NLS-1$
 
 	//1.1 Finder Descriptor
 	public static final String FINDER_WIZARD_P1 = J2EEUIPlugin.PLUGIN_ID + ".FINDER_WIZARD_P1"; //$NON-NLS-1$
 	public static final String FINDER_WIZARD_P2 = J2EEUIPlugin.PLUGIN_ID + ".FINDER_WIZARD_P2"; //$NON-NLS-1$
 
 	public static final String EJBQL_METHOD_PARM_PAGE = J2EEUIPlugin.PLUGIN_ID + ".EJBQL_METHOD_PARM_PAGE"; //$NON-NLS-1$
 	public static final String MDB_SETTINGS_PAGE = J2EEUIPlugin.PLUGIN_ID + ".MDB_SETTINGS_PAGE"; //$NON-NLS-1$
 	public static final String MDB_TYPES_PAGE = J2EEUIPlugin.PLUGIN_ID + ".MDB_TYPES_PAGE"; //$NON-NLS-1$
 
 	//	TODO remove these constants
 	public static final String APPCLIENT_EDITOR_SOURCE = J2EEUIPlugin.PLUGIN_ID + ".APPCLIENT_EDITOR_SOURCE"; //$NON-NLS-1$
 	public static final String EJB_EDITOR_OVERVIEW_JARBINDING = J2EEUIPlugin.PLUGIN_ID + ".EJB_EDITOR_OVERVIEW_JARBINDING"; //$NON-NLS-1$
 	public static final String EJB_EDITOR_BEAN_EXTENSIONS = J2EEUIPlugin.PLUGIN_ID + ".EJB_EDITOR_BEAN_EXTENSIONS"; //$NON-NLS-1$
 	public static final String EJB_EDITOR_REFERENCES_EXTENSION = J2EEUIPlugin.PLUGIN_ID + ".EJB_EDITOR_REFERENCES_EXTENSION"; //$NON-NLS-1$
 	public static final String EJB_EDITOR_SOURCE = J2EEUIPlugin.PLUGIN_ID + ".EJB_EDITOR_SOURCE"; //$NON-NLS-1$
 	public static final String ACCESS_INTENT_1_WIZARD_P3 = J2EEUIPlugin.PLUGIN_ID + ".ACCESS_INTENT_1_WIZARD_P3"; //$NON-NLS-1$
 	public static final String ACCESS_INTENT_1_WIZARD_P2 = J2EEUIPlugin.PLUGIN_ID + ".ACCESS_INTENT_1_WIZARD_P2"; //$NON-NLS-1$	
 	public static final String EJB_EDITOR_ACCESS_IDENTITY = J2EEUIPlugin.PLUGIN_ID + ".EJB_EDITOR_ACCESS_IDENTITY"; //$NON-NLS-1$
 	public static final String JAR_DEPENDENCIES_EDITOR_P5 = J2EEUIPlugin.PLUGIN_ID + ".jard3000"; //$NON-NLS-1$
 	public static final String NEW_CONNECTOR_WIZARD_P2 = J2EEUIPlugin.PLUGIN_ID + ".conp2000"; //$NON-NLS-1$
 	public static final String EXTENSION_EDITOR_P1 = J2EEUIPlugin.PLUGIN_ID + ".exte1000"; //$NON-NLS-1$
 	public static final String EXTENSION_EDITOR_P2 = J2EEUIPlugin.PLUGIN_ID + ".exte2000"; //$NON-NLS-1$
 	public static final String EXTENSION_EDITOR_P3 = J2EEUIPlugin.PLUGIN_ID + ".exte3000"; //$NON-NLS-1$
 	public static final String EXTENSION_EDITOR_P4 = J2EEUIPlugin.PLUGIN_ID + ".exte4000"; //$NON-NLS-1$
 	public static final String EXTENSION_EDITOR_P5 = J2EEUIPlugin.PLUGIN_ID + ".exte5000"; //$NON-NLS-1$
 	public static final String EXTENSION_EDITOR_P6 = J2EEUIPlugin.PLUGIN_ID + ".exte6000"; //$NON-NLS-1$
 	public static final String METHOD_SECURITY_WIZARD_P1 = J2EEUIPlugin.PLUGIN_ID + ".secm1000"; //$NON-NLS-1$
 	public static final String METHOD_SECURITY_WIZARD_P2 = J2EEUIPlugin.PLUGIN_ID + ".secm1100"; //$NON-NLS-1$	
 	public static final String NEW_APPCLIENT_WIZARD_P2 = J2EEUIPlugin.PLUGIN_ID + ".appc2000"; //$NON-NLS-1$
 	public static final String NEW_APPCLIENT_WIZARD_P3 = J2EEUIPlugin.PLUGIN_ID + ".appc3000"; //$NON-NLS-1$
 	public static final String IMPORT_APPCLIENT_WIZARD_P2 = J2EEUIPlugin.PLUGIN_ID + ".iapp2000"; //$NON-NLS-1$
 	public static final String IMPORT_APPCLIENT_WIZARD_P3 = J2EEUIPlugin.PLUGIN_ID + ".iapp3000"; //$NON-NLS-1$
 	public static final String IMPORT_EJB_WIZARD_P2 = J2EEUIPlugin.PLUGIN_ID + ".iejb1500"; //$NON-NLS-1$
 	public static final String IMPORT_EJB_WIZARD_P3 = J2EEUIPlugin.PLUGIN_ID + ".iejb2000"; //$NON-NLS-1$
 	public static final String APP_EDITOR_SECURITY_RUN_AS = J2EEUIPlugin.PLUGIN_ID + ".appe3200"; //$NON-NLS-1$
 	public static final String APP_EDITOR_SOURCE = J2EEUIPlugin.PLUGIN_ID + ".appe4000"; //$NON-NLS-1$
 	public static final String EJB_EDITOR_P1 = J2EEUIPlugin.PLUGIN_ID + ".ejbe1000"; //$NON-NLS-1$
 	public static final String EJB_EDITOR_P2 = J2EEUIPlugin.PLUGIN_ID + ".ejbe2000"; //$NON-NLS-1$
 	public static final String EJB_EDITOR_P3 = J2EEUIPlugin.PLUGIN_ID + ".ejbe3000"; //$NON-NLS-1$
 	public static final String EJB_EDITOR_P4 = J2EEUIPlugin.PLUGIN_ID + ".ejbe4000"; //$NON-NLS-1$
 	public static final String EJB_EDITOR_P5 = J2EEUIPlugin.PLUGIN_ID + ".ejbe5000"; //$NON-NLS-1$
 	public static final String EJB_EDITOR_P6 = J2EEUIPlugin.PLUGIN_ID + ".ejbe6000"; //$NON-NLS-1$
 	public static final String EJB_EDITOR_P7 = J2EEUIPlugin.PLUGIN_ID + ".ejbe7000"; //$NON-NLS-1$
 	public static final String J2EE_HIERARCHY_VIEW_P1 = J2EEUIPlugin.PLUGIN_ID + ".J2EE_HIERARCHY_VIEW_P1"; //$NON-NLS-1$
 }
