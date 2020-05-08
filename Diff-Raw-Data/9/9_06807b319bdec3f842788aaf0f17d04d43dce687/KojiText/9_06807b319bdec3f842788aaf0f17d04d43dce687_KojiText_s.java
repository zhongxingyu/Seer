 /*******************************************************************************
  * Copyright (c) 2010 Red Hat Inc. and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     Red Hat Inc. - initial API and implementation
  *******************************************************************************/
 package org.fedoraproject.eclipse.packager.koji;
 
 import org.eclipse.osgi.util.NLS;
 
 /**
  * Text for the koji plug-in.
  *
  */
 public class KojiText extends NLS {
 	
 	/**
 	 * Do not in-line this into the static initializer as the
 	 * "Find Broken Externalized Strings" tool will not be
 	 * able to find the corresponding bundle file.
 	 * 
 	 * This is the path to the file containing externalized strings.
 	 */
 	private static final String BUNDLE_NAME = "org.fedoraproject.eclipse.packager.koji.kojitext"; //$NON-NLS-1$
 
 
 
 	
 	// KojiBuildHandler Strings
 	/****/ public static String KojiBuildHandler_tagBeforeSendingBuild;
 	/****/ public static String KojiBuildHandler_pushBuildToKoji;
 	/****/ public static String KojiBuildHandler_kojiBuild;
 	/****/ public static String KojiBuildHandler_unknownBuildError;
 	/****/ public static String KojiBuildHandler_invalidHubUrl;
 	/****/ public static String KojiBuildHandler_errorGettingNVR;
 	/****/ public static String KojiBuildHandler_invalidKojiWebUrl;
 	/****/ public static String KojiBuildHandler_missingCertificatesMsg;
 	/****/ public static String KojiBuildHandler_certificateExpriredMsg;
 	// KojiMessageDialog Strings
 	/****/ public static String KojiMessageDialog_buildNumberMsg;
 	/****/ public static String KojiMessageDialog_buildResponseMsg;
 	// UnpushedChangesListener
 	/****/ public static String UnpushedChangesListener_checkUnpushedChangesMsg;
 	/****/ public static String UnpushedChangesListener_unpushedChangesError;
 	// TagSourcesListener
 	/****/ public static String TagSourcesListener_tagSourcesMsg;
 	// KojiBuildCommand
 	/****/ public static String KojiBuildCommand_sendBuildCmd;
 	/****/ public static String KojiBuildCommand_kojiLogoutTask;
 	/****/ public static String KojiBuildCommand_configErrorNoClient;
 	/****/ public static String KojiBuildCommand_configErrorNoScmURL;
	/****/ public static String KojiBuildCommand_configErrorNoDistribution;
 	/****/ public static String KojiBuildCommand_configErrorNoNVR;
 	/****/ public static String KojiBuildCommand_kojiLogInTask;
 	/****/ public static String KojiBuildCommand_scratchBuildLogMsg;
 	/****/ public static String KojiBuildCommand_buildLogMsg;
 	// BuildAlreadyExistsException
 	/****/ public static String BuildAlreadyExistsException_msg;
 	// KojiHubClientLoginException
 	/****/ public static String KojiHubClientLoginException_loginFailedMsg;
 	// Generic Strings
 	/****/ public static String xmlRPCconfigNotInitialized;
 	
 	static {
 		initializeMessages(BUNDLE_NAME,	KojiText.class);
 	}
 }
