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
 package org.fedoraproject.eclipse.packager;
 
 import org.eclipse.osgi.util.NLS;
 
 
 /**
  * Translation bundle for FedoraPackager core.
  */
 public class FedoraPackagerText extends NLS {
 
 	/**
 	 * Do not in-line this into the static initializer as the
 	 * "Find Broken Externalized Strings" tool will not be
 	 * able to find the corresponding bundle file.
 	 * 
 	 * This is the path to the file containing externalized strings.
 	 */
 	private static final String BUNDLE_NAME = "org.fedoraproject.eclipse.packager.fedorapackagertext"; //$NON-NLS-1$
 
 	// ConsoleWriterThread
 	/****/ public static String ConsoleWriterThread_ioFail;
 	// DownloadHandler Strings
 	/****/ public static String DownloadHandler_downloadSourceTask;
 	// UploadHandler Strings
 	/****/ public static String UploadHandler_taskName;
 	/****/ public static String UploadHandler_versionOfFileExistsAndUpToDate;
 	/****/ public static String UploadHandler_invalidUrlError;
 	// UploadSourceCommand
 	/****/ public static String UploadSourceCommand_uploadFileUnspecified;
 	/****/ public static String UploadSourceCommand_uploadFileInvalid;
 	/****/ public static String UploadSourceCommand_uploadingFileSubTaskName;
 	/****/ public static String UploadSourceCommand_usingUploadURLMsg;
 	// FileAvailableInLookasideCacheException
 	/****/ public static String FileAvailableInLookasideCacheException_message;
 	// DownloadSourceCommand
 	/****/ public static String DownloadSourceCommand_nothingToDownload;
 	/****/ public static String DownloadSourceCommand_downloadFile;
 	/****/ public static String DownloadSourceCommand_downloadFileError;
 	/****/ public static String DownloadSourceCommand_invalidURL;
 	/****/ public static String DownloadSourceCommand_downloadingFileXofY;
 	/****/ public static String DownloadSourceCommand_usingDownloadURLMsg;
 	// SourcesFile
 	/****/ public static String SourcesFile_saveFailedMsg;
 	/****/ public static String SourcesFile_saveJob;
 	// FedoraSSL
 	/****/ public static String FedoraSSL_certificatesMissingError;
 	// FedoraPackagerUtils
 	/****/ public static String FedoraPackagerUtils_invalidProjectRootError;
 	/****/ public static String FedoraPackagerUtils_invalidContainerOrProjectType;
 	/****/ public static String FedoraPackagerUtils_projectRootClassNameMsg;
 	// UnpushedChangesListener
 	/****/ public static String UnpushedChangesListener_checkUnpushedChangesMsg;
 	/****/ public static String UnpushedChangesListener_unpushedChangesError;
 	// TagSourcesListener
 	/****/ public static String TagSourcesListener_tagBeforeSendingBuild;
 	/****/ public static String TagSourcesListener_tagSourcesMsg;
 	// ChecksumValidListener
 	/****/ public static String ChecksumValidListener_badChecksum;
 	// VCSIgnoreFileUpdater
 	/****/ public static String VCSIgnoreFileUpdater_couldNotCreateFile;
 	/****/ public static String VCSIgnoreFileUpdater_errorWritingFile;
 	// SourcesFileUpdater
 	/****/ public static String SourcesFileUpdater_errorSavingFile;
 	// FedoraPackagerPreferencesPage
 	/****/ public static String FedoraPackagerPreferencePage_lookasideUploadURLLabel;
 	/****/ public static String FedoraPackagerPreferencePage_lookasideDownloadURLLabel;
 	/****/ public static String FedoraPackagerPreferencePage_description;
 	/****/ public static String FedoraPackagerPreferencePage_invalidDownloadURLMsg;
 	/****/ public static String FedoraPackagerPreferencePage_invalidUploadURLMsg;
 	/****/ public static String FedoraPackagerPreferencePage_kojiWebURLLabel;
 	/****/ public static String FedoraPackagerPreferencePage_kojiHubURLLabel;
 	/****/ public static String FedoraPackagerPreferencePage_kojiWebURLInvalidMsg;
 	/****/ public static String FedoraPackagerPreferencePage_kojiHubURLInvalidMsg;
 	/****/ public static String FedoraPackagerPreferencePage_buildSystemGroupName;
 	/****/ public static String FedoraPackagerPreferencePage_lookasideGroupName;
 	/****/ public static String FedoraPackagerPreferencePage_generalGroupName;
 	/****/ public static String FedoraPackagerPreferencePage_debugSwitchLabel;
 	// FedoraPackagerCommand
 	/****/ public static String FedoraPackagerCommand_projectRootSetTwiceError;
 	// FedoraPackager
 	/****/ public static String FedoraPackager_commandNotFoundError;
 	// Generic strings
 	/****/ public static String somethingUnexpectedHappenedError;
 	/****/ public static String commandWasCalledInTheWrongState;
 	/****/ public static String invalidFedoraProjectRootError;
 	/****/ public static String callingCommand;
 	/****/ public static String extensionNotFoundError;
 
 	static {
 		initializeMessages(BUNDLE_NAME,	FedoraPackagerText.class);
 	}
 }
