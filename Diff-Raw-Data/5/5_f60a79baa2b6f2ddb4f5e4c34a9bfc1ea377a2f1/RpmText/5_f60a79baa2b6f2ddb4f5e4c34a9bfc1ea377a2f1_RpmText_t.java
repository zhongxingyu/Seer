 /*******************************************************************************
  * Copyright (c) 2010-2011 Red Hat Inc. and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     Red Hat Inc. - initial API and implementation
  *******************************************************************************/
 package org.fedoraproject.eclipse.packager.rpm;
 
 import org.eclipse.osgi.util.NLS;
 
 /**
  * 
  * Utility class for String externalization.
  *
  */
 public class RpmText extends NLS {
 	
 	/**
 	 * Do not in-line this into the static initializer as the
 	 * "Find Broken Externalized Strings" tool will not be
 	 * able to find the corresponding bundle file.
 	 * 
 	 * This is the path to the file containing externalized strings.
 	 */
 	private static final String BUNDLE_NAME = "org.fedoraproject.eclipse.packager.rpm.rpmtext"; //$NON-NLS-1$
 
 
 	// LocalBuildHandler
 	/****/ public static String LocalBuildHandler_buildForLocalArch;
 	/****/ public static String LocalBuildHandler_downloadSourcesForLocalBuild;
 	/****/ public static String LocalBuildHandler_buildCanceled;
 	/****/ public static String LocalBuildHandler_buildCancelationResponse;
 	// MockBuildHandler
 	/****/ public static String MockBuildHandler_testLocalBuildWithMock;
 	/****/ public static String MockBuildHandler_creatingSrpm;
 	/****/ public static String MockBuildHandler_srpmBuildFailed;
 	/****/ public static String MockBuildHandler_downloadSourcesForMockBuild;
 	/****/ public static String MockBuildHandler_creatingSRPMForMockBuild;
 	/****/ public static String MockBuildHandler_mockFailedMsg;
 	/****/ public static String MockBuildHandler_mockSucceededMsg;
 	/****/ public static String MockBuildHandler_mockCancelledMsg;
 	/****/ public static String MockBuildHandler_FileSystemDialogTitle;
 	/****/ public static String MockBuildHandler_RootListMessage;
 	// PrepHandler
 	/****/ public static String PrepHandler_prepareSourcesForBuildMsg;
 	/****/ public static String PrepHandler_downloadSourcesForPrep;
 	// RpmBuildCommand
 	/****/ public static String RpmBuildCommand_distDefinesNullError;
 	/****/ public static String RpmBuildCommand_flagsNullError;
 	/****/ public static String RpmBuildCommand_commandStringMsg;
 	/****/ public static String RpmBuildCommand_buildTypeRequired;
 	/****/ public static String RpmBuildCommand_callRpmBuildMsg;
 	/****/ public static String RpmBuildCommand_BuildDidNotStart;
 	/****/ public static String RpmBuildCommand_BuildFailure;
 	// RpmEvalCommand
 	/****/ public static String RpmEvalCommand_variableMustBeSet;
 	// MockBuildCommand
 	/****/ public static String MockBuildCommand_srpmNullError;
 	/****/ public static String MockBuildCommand_invalidMockConfigError;
 	/****/ public static String MockBuildCommand_userNotInMockGroupMsg;
 	/****/ public static String MockBuildCommand_srpmPathDoesNotExist;
 	/****/ public static String MockBuildCommand_mockCommandLog;
 	/****/ public static String MockBuildCommand_callMockBuildMsg;
 	/****/ public static String MockBuildCommand_usingDefaultMockConfig;
 	// SCMMockBuildCommand
 	/****/ public static String SCMMockBuildCommand_invalidRepoType;
 	// MockNotInstalledException
 	/****/ public static String MockNotInstalledException_msg;
 	// RpmEvalCommandException
 	/****/ public static String RpmEvalCommandException_msg;
 	// SRPMBuildHandler
 	/****/ public static String SRPMBuildHandler_downloadSourcesForSRPMBuild;
 	/****/ public static String SRPMBuildHandler_buildingSRPM;
 	// SRPMImportCommand
 	/****/ public static String SRPMImportCommand_IOError;
 	/****/ public static String SRPMImportCommand_NonZeroExit;
 	/****/ public static String SRPMImportCommand_NonZeroQueryExit;
 	/****/ public static String SRPMImportCommand_PathNotSet;
 	/****/ public static String SRPMImportCommand_ProjectNotSet;
 	/****/ public static String SRPMImportCommand_SRPMNotFound;
	/****/ public static String SRPMImportHandler_FileDialogTitle;
 
 
 	//SRPMImportHandler
 	/****/ public static String SRPMImportHandler_ImportingFromSRPM;
 	// SRPMImportJob
 	/****/ public static String SRPMImportJob_CallingCommand;
 	/****/ public static String SRPMImportJob_ExtractFailed;
 
 
	/****/ public static String SRPMImportJob_ExtractingSRPM;
 	/****/ public static String SRPMImportJob_InitialSetup;
 	/****/ public static String SRPMImportJob_MalformedLookasideURL;
 	/****/ public static String SRPMImportJob_StagingChanges;
 	/****/ public static String SRPMImportJob_UploadingSources;
 
 	static {
 		// initialize resource bundle
 		NLS.initializeMessages(BUNDLE_NAME, RpmText.class);
 	}
 }
