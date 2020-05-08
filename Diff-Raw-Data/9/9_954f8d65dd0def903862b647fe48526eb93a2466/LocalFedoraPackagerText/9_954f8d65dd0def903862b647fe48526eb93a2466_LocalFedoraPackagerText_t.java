 /*******************************************************************************
  * Copyright (c) 2011 Red Hat Inc. and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     Red Hat Inc. - initial API and implementation
  *******************************************************************************/
 package org.fedoraproject.eclipse.packager.local;
 
 import org.eclipse.osgi.util.NLS;
 
 public class LocalFedoraPackagerText extends NLS {
 	private static final String BUNDLE_NAME = "org.fedoraproject.eclipse.packager.local.localfedorapackagertext"; //$NON-NLS-1$
 
 	//Local Fedora Packager Project-wizards-main
 	/****/ public static String LocalFedoraPackagerWizardPage_title;
 	/****/ public static String LocalFedoraPackagerWizardPage_description;
 	/****/ public static String LocalFedoraPackagerWizardPage_image;
 	//Local Fedora Packager Project-wizards-page one
 	/****/ public static String LocalFedoraPackagerPageOne_lblNoteGit;
 	//Local Fedora Packager Project-wizards-page Two
 	/****/ public static String LocalFedoraPackagerPageTwo_linkFAS;
 	/****/ public static String LocalFedoraPackagerPageTwo_urlFAS;
 	/****/ public static String LocalFedoraPackagerPageTwo_lblTextFAS;
 	/****/ public static String LocalFedoraPackagerPageTwo_linkBugzilla;
 	/****/ public static String LocalFedoraPackagerPageTwo_urlBugzilla;
 	/****/ public static String LocalFedoraPackagerPageTwo_linkInitial;
 	/****/ public static String LocalFedoraPackagerPageTwo_urlInitial;
 	/****/ public static String LocalFedoraPackagerPageTwo_linkIntroduce;
 	/****/ public static String LocalFedoraPackagerPageTwo_urlIntroduce;
 	/****/ public static String LocalFedoraPackagerPageTwo_btnRadioNewMaintainer;
 	/****/ public static String LocalFedoraPackagerPageTwo_btnRadioExistMaintainer;
 	/****/ public static String LocalFedoraPackagerPageTwo_grpAccountSetup;
 	//Local Fedora Packager Project-wizards-page Three
 	/****/ public static String LocalFedoraPackagerPageThree_grpSpec;
 	/****/ public static String LocalFedoraPackagerPageThree_btnCheckStubby;
 	/****/ public static String LocalFedoraPackagerPageThree_btnBrowse;
 	/****/ public static String LocalFedoraPackagerPageThree_btnCheckSrpm;
 	/****/ public static String LocalFedoraPackagerPageThree_lblSrpm;
 	/****/ public static String LocalFedoraPackagerPageThree_lblSpecPlain;
 	/****/ public static String LocalFedoraPackagerPageThree_btnCheckPlain;
 	/****/ public static String LocalFedoraPackagerPageThree_btnTemplateSpec;
 	/****/ public static String LocalFedoraPackagerPageThree_fileDialog;
 
 	//Local Fedora Packager Project-api
 	/****/ public static String LocalFedoraPackagerProjectCreator_FirstCommit;
 
 	//Local Fedora Packager Project-api-errors
 	/****/ public static String invalidLocalFedoraProjectRootError;
 
 	//Local Fedora Packager Project-perspective message
 	/****/ public static String LocalFedoraPackager_switchPerspectiveQuestionTitle;
 	/****/ public static String LocalFedoraPackager_switchPerspectiveQuestionMsg;
 	
 	// LocalHandlerDispatcher
 	/****/ public static String LocalHandlerDispatcher_dispatchToHandlerMsg;
 
 	static {
 		// initialize resource bundle
 		NLS.initializeMessages(BUNDLE_NAME, LocalFedoraPackagerText.class);
 	}
 
 }
