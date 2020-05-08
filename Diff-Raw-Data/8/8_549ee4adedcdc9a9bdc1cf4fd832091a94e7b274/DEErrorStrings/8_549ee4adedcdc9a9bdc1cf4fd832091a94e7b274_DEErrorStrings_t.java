 package org.iplantc.de.client;
 
 import org.iplantc.core.uicommons.client.CommonUIErrorStrings;
 
 /**
  * Constants used by the client to communicate system errors.
  * 
  * All values present here are subject to translation for internationalization.
  */
 public interface DEErrorStrings extends CommonUIErrorStrings {
 
     /**
      * Error message displayed when the application fails to display the current view.
      * 
      * @return localized error string.
      */
     String unableToBuildWorkspace();
 
     /**
      * Error message displayed when folder rename fails.
      * 
      * @return localized error string.
      */
     String renameFolderFailed();
 
     /**
      * Error message displayed when file rename fails.
      * 
      * @return localized error string.
      */
     String renameFileFailed();
 
     /**
      * Error message displayed when folder deletion fails.
      * 
      * @return localized error string.
      */
     String deleteFolderFailed();
 
     /**
      * Error message displayed when file deletion fails.
      * 
      * @return localized error string.
      */
     String deleteFileFailed();
 
     /**
      * Error message displayed when the application fails to create a folder.
      * 
      * @return localized error string.
      */
     String createFolderFailed();
 
     /**
      * Error message displayed when the application fails to retrieve folder contents.
      * 
      * @return localized error string.
      */
     String retrieveFolderInfoFailed();
 
     /**
      * Error message displayed when a file fails to upload.
      * 
      * @return localized error string.
      */
     String fileUploadFailed(String filename);
 
     /**
      * Error message displayed when a file import fails.
      * 
      * @param source the file or URL that was imported
      * @return localized error string.
      */
     String importFailed(String source);
 
     /**
      * Error message displayed when a job fails to delete.
      * 
      * @return localized error string.
      */
     String deleteJobError();
 
     /**
      * Error message displayed when a job fails to launch.
      * 
      * @param jobName the name of the failed job.
      * @return localized error string.
      */
     String jobFailedToLaunch(String jobName);
 
     /**
      * Error message displayed when the discovery environment properties fail to load.
      * 
      * @return localized error string.
      */
     String systemInitializationError();
 
     /**
      * Error message displayed when we fail to retrieve a workflow guide.
      * 
      * @return localized error string.
      */
     String unableToRetrieveWorkflowGuide();
 
     /**
      * Error message displayed when we fail to retrieve a file manifest.
      * 
      * @return localized error string.
      */
     String unableToRetrieveFileManifest(String filename);
 
     /**
      * Error message displayed when we fail to retrieve file contents.
      * 
      * @return localized error string.
      */
     String unableToRetrieveFileData(String filename);
 
     /**
      * Error displayed when a user enters an invalid filename.
      * 
      * @return localized error string.
      */
     String invalidFilenameEntered();
 
     /**
      * Error displayed when the UI is unable to retrieve notifications from the server.
      * 
      * @return localized error string.
      */
     String notificationRetrievalFail();
 
     /**
      * Error displayed when the service to delete notifications has failed.
      * 
      * @return localized error string.
      */
     String notificationDeletFail();
 
     /**
      * Error message displayed when we fail to retrieve tree URLs.
      * 
      * @return localized error string.
      */
     String unableToRetrieveTreeUrls(String filename);
 
     /**
      * Error message displayed when a folder is not found.
      * 
      * @return localized error string.
      */
     String folderNotFound(String filename);
 
     /**
      * Error message displayed when an delete app service fails
      * 
      * @return localized error string.
      */
     String appRemoveFailure();
 
     /**
      * Error message displayed when disk resource move fails.
      * 
      * @return localized error string.
      */
     String moveFailed();
 
     /**
      * Error message for display in the error dialog details when a disk resource service call fails.
      * 
      * @param status
      * @param code
      * @param reason
      * @return localized error string.
      */
     String dataServiceErrorReport(String status, String code, String reason);
 
     /**
      * Error message displayed when a service call fails because disk resource(s) do not exist.
      * 
      * @param nameList
      * @return localized error string.
      */
     String diskResourceDoesNotExist(String nameList);
 
     /**
      * Error message displayed when a service call fails because disk resource(s) already exists.
      * 
      * @param nameList
      * @return localized error string.
      */
     String diskResourceExists(String nameList);
 
     /**
      * Error message displayed when a data service call fails because disk resource(s) are not writable.
      * 
      * @param nameList
      * @return localized error string.
      */
     String diskResourceNotWriteable(String nameList);
 
     /**
      * Error message displayed when a data service call fails because disk resource(s) are not readable.
      * 
      * @param nameList
      * @return localized error string.
      */
     String diskResourceNotReadable(String nameList);
 
     /**
      * Error message displayed when a data service call fails because disk resource(s) are writable.
      * 
      * @param nameList
      * @return localized error string.
      */
     String diskResourceWriteable(String nameList);
 
     /**
      * Error message displayed when a data service call fails because disk resource(s) are readable.
      * 
      * @param nameList
      * @return localized error string.
      */
     String diskResourceReadable(String nameList);
 
     /**
      * Error message displayed when a data service call fails because the given username is invalid.
      * 
      * @return localized error string.
      */
     String dataErrorNotAUser();
 
     /**
      * Error message displayed when a service call fails because disk resource(s) are not files.
      * 
      * @param nameList
      * @return localized error string.
      */
     String diskResourceNotAFile(String nameList);
 
     /**
      * Error message displayed when a service call fails because disk resource(s) are not folders.
      * 
      * @param nameList
      * @return localized error string.
      */
     String diskResourceNotAFolder(String nameList);
 
     /**
      * Error message displayed when a data service call fails because disk resource(s) are files.
      * 
      * @param nameList
      * @return localized error string.
      */
     String diskResourceIsAFile(String nameList);
 
     /**
      * Error message displayed when a data service call fails because disk resource(s) are folders.
      * 
      * @param nameList
      * @return localized error string.
      */
     String diskResourceIsAFolder(String nameList);
 
     /**
      * Error message displayed when a data service call fails because the request is invalid JSON.
      * 
      * @return localized error string.
      */
     String dataErrorInvalidJson();
 
     /**
      * Error message displayed when a data service call fails because the request JSON is missing or has
      * an incorrect field.
      * 
      * @return localized error string.
      */
     String dataErrorBadOrMissingField();
 
     /**
      * Error message displayed when a data service call fails because the requesting user is not
      * authorized.
      * 
      * @return localized error string.
      */
     String dataErrorNotAuthorized();
 
     /**
      * Error message displayed when a data service call fails because the request is missing a parameter.
      * 
      * @return localized error string.
      */
     String dataErrorMissingQueryParameter();
 
     /**
      * Error message displayed when a data service call fails because the delete request was incomplete.
      * 
      * @return localized error string.
      */
     String diskResourceIncompleteDeletion();
 
     /**
      * Error message displayed when a data service call fails because the move request was incomplete.
      * 
      * @return localized error string.
      */
     String diskResourceIncompleteMove();
 
     /**
      * Error message displayed when a data service call fails because the rename request was incomplete.
      * 
      * @return localized error string.
      */
     String diskResourceIncompleteRename();
 
     /**
      * Error message displayed when a service call fails because folder(s) do not exist.
      * 
      * @param nameList
      * @return localized error string.
      */
     String folderDoesNotExist(String nameList);
 
     /**
      * Error message displayed when a service call fails because folder(s) already exist.
      * 
      * @param nameList
      * @return localized error string.
      */
     String folderExists(String nameList);
 
     /**
      * Error message displayed when a data service call fails because folder(s) are not writable.
      * 
      * @param nameList
      * @return localized error string.
      */
     String folderNotWriteable(String nameList);
 
     /**
      * Error message displayed when a data service call fails because folder(s) are not readable.
      * 
      * @param nameList
      * @return localized error string.
      */
     String folderNotReadable(String nameList);
 
     /**
      * Error message displayed when a data service call fails because folder(s) are writable.
      * 
      * @param nameList
      * @return localized error string.
      */
     String folderWriteable(String nameList);
 
     /**
      * Error message displayed when a data service call fails because folder(s) are readable.
      * 
      * @param nameList
      * @return localized error string.
      */
     String folderReadable(String nameList);
 
     /**
      * Error message displayed when a service call fails because file(s) do not exist.
      * 
      * @param nameList
      * @return localized error string.
      */
     String fileDoesNotExist(String nameList);
 
     /**
      * Error message displayed when a service call fails because file(s) already exist.
      * 
      * @param nameList
      * @return localized error string.
      */
     String fileExists(String nameList);
 
     /**
      * Error message displayed when a data service call fails because file(s) are not writable.
      * 
      * @param nameList
      * @return localized error string.
      */
     String fileNotWriteable(String nameList);
 
     /**
      * Error message displayed when a data service call fails because file(s) are not readable.
      * 
      * @param nameList
      * @return localized error string.
      */
     String fileNotReadable(String nameList);
 
     /**
      * Error message displayed when a data service call fails because file(s) are writable.
      * 
      * @param nameList
      * @return localized error string.
      */
     String fileWriteable(String nameList);
 
     /**
      * Error message displayed when a data service call fails because file(s) are readable.
      * 
      * @param nameList
      * @return localized error string.
      */
     String fileReadable(String nameList);
 
     /**
      * Error message displayed when metadata update service call fails
      * 
      * @return localized error string.
      */
     String metadataUpdateFailed();
 
     /**
      * Error message displayed when a wiki page for a tool cannot be created.
      * 
      * @param toolName name of the tool to make a page for
      * @return localized error string.
      */
     String cantCreateConfluencePage(String toolName);
 
     /**
      * Error message displayed when a call to save parameters as file failed
      * 
      * @return localized error string.
      */
     String saveParamFailed();
 }
