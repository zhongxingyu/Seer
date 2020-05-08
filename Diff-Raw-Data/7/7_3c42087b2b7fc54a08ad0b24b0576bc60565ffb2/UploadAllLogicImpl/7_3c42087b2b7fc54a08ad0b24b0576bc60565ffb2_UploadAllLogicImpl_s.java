 package org.sakaiproject.assignment2.logic.impl;
 
 import java.io.File;
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 import java.util.regex.Pattern;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.apache.commons.vfs.FileContent;
 import org.apache.commons.vfs.FileDepthSelector;
 import org.apache.commons.vfs.FileObject;
 import org.apache.commons.vfs.FileSystemException;
 import org.apache.commons.vfs.FileSystemManager;
 import org.apache.commons.vfs.FileType;
 import org.apache.commons.vfs.VFS;
 import org.sakaiproject.assignment2.exception.AssignmentNotFoundException;
 import org.sakaiproject.assignment2.exception.UploadException;
 import org.sakaiproject.assignment2.logic.AssignmentLogic;
 import org.sakaiproject.assignment2.logic.AssignmentPermissionLogic;
 import org.sakaiproject.assignment2.logic.AssignmentSubmissionLogic;
 import org.sakaiproject.assignment2.logic.ExternalGradebookLogic;
 import org.sakaiproject.assignment2.logic.ExternalLogic;
 import org.sakaiproject.assignment2.logic.UploadAllLogic;
 import org.sakaiproject.assignment2.logic.UploadGradesLogic;
 import org.sakaiproject.assignment2.logic.ZipExportLogic;
 import org.sakaiproject.assignment2.model.Assignment2;
 import org.sakaiproject.assignment2.model.AssignmentSubmissionVersion;
 import org.sakaiproject.assignment2.model.FeedbackAttachment;
 import org.sakaiproject.assignment2.model.UploadAllOptions;
 import org.sakaiproject.content.api.ContentHostingService;
 import org.sakaiproject.content.api.ContentResource;
 import org.sakaiproject.content.api.ContentTypeImageService;
 import org.sakaiproject.entity.api.ResourceProperties;
 import org.sakaiproject.exception.IdInvalidException;
 import org.sakaiproject.exception.IdUsedException;
 import org.sakaiproject.exception.InconsistentException;
 import org.sakaiproject.exception.OverQuotaException;
 import org.sakaiproject.exception.PermissionException;
 import org.sakaiproject.exception.ServerOverloadException;
 import org.sakaiproject.util.FormattedText;
 
 /**
  * Functionality for uploading and downloading an archive bundle
  * 
  * @author <a href="mailto:carl.hall@et.gatech.edu">Carl Hall</a>
  */
 public class UploadAllLogicImpl implements UploadAllLogic
 {
     private static final Log log = LogFactory.getLog(UploadAllLogicImpl.class);
     
     /**
      * a ;-delimited list of file names of files that should not be
      * added as feedback attachments
      */
     private static final String ATTACH_FILTER = ".DS_Store";
 
     private AssignmentLogic assignmentLogic;
     public void setAssignmentLogic(AssignmentLogic assignmentLogic)
     {
         this.assignmentLogic = assignmentLogic;
     }
 
     private ContentHostingService contentHosting;
     public void setContentHostingService(ContentHostingService contentHosting)
     {
         this.contentHosting = contentHosting;
     }
     private ContentTypeImageService imageService;
     public void setContentTypeImageService(ContentTypeImageService imageService)
     {
         this.imageService = imageService;
     }
 
     private AssignmentSubmissionLogic submissionLogic;
     public void setAssignmentSubmissionLogic(AssignmentSubmissionLogic submissionLogic)
     {
         this.submissionLogic = submissionLogic;
     }
 
     private ExternalLogic externalLogic;
     public void setExternalLogic(ExternalLogic externalLogic)
     {
         this.externalLogic = externalLogic;
     }
 
     public ZipExportLogic zipExportLogic;
     public void setZipExportLogic(ZipExportLogic zipExportLogic) 
     {
         this.zipExportLogic = zipExportLogic;
     }
 
     public UploadGradesLogic uploadGrades;
     public void setUploadGradesLogic(UploadGradesLogic uploadGrades) {
         this.uploadGrades = uploadGrades;
     }
 
     public AssignmentPermissionLogic permissionLogic;
     public void setAssignmentPermissionLogic(AssignmentPermissionLogic permissionLogic) {
         this.permissionLogic = permissionLogic;
     }
     
     public ExternalGradebookLogic gradebookLogic;
     public void setExternalGradebookLogic(ExternalGradebookLogic gradebookLogic) {
         this.gradebookLogic = gradebookLogic;
     }
 
     public List<Map<String, String>> uploadAll(UploadAllOptions options, File file) throws UploadException
     {
         if (options == null) {
             throw new IllegalArgumentException("Null UploadAllOptions options passed to uploadAll");
         }
 
         if (options.assignmentId == null) {
             throw new IllegalArgumentException("Null options.assignmentId passed to uploadAll");
         }
 
         if (file == null) {
             throw new IllegalArgumentException("Null File file passed to uploadAll");
         }
 
         String currUserId = externalLogic.getCurrentUserId();
 
         Assignment2 assign = assignmentLogic.getAssignmentByIdWithAssociatedData(options.assignmentId);
         if (assign == null) {
             throw new AssignmentNotFoundException("No assignment exists with id " + options.assignmentId);
         }
 
         List<Map<String, String>> uploadInfo = new ArrayList<Map<String, String>>();
 
         // construct the pattern for extracting the identifying info from the
         // folder names here so we don't have to do it repeatedly
         Pattern pattern = Pattern.compile(ZipExportLogic.FILE_NAME_REGEX);
         
         Map<String, String> displayIdUserIdMap = externalLogic.getUserDisplayIdUserIdMapForStudentsInSite(assign.getContextId());
 
         List<StudentFeedbackWrapper> feedbackUploadList = new ArrayList<StudentFeedbackWrapper>();
 
         try
         {
             FileSystemManager fsManager = VFS.getManager();
             FileObject zipFile = fsManager.toFileObject(file);
             zipFile = fsManager.createFileSystem("zip", zipFile);
             
             FileObject zipContents = null;
 
             // the first file can be ignored. it should be named based upon the
             // assignment name and site title
             for (FileObject fileObj : zipFile.findFiles(new FileDepthSelector(1,1)))
             {
                 String topLevelFolder = zipExportLogic.getTopLevelFolderName(assign);
                 String baseFolderName = fileObj.getName().getBaseName();
                 if (baseFolderName.startsWith(topLevelFolder)) {
                     zipContents = fileObj;
                     break;
                 }
             }
 
             
             if (zipContents == null || !zipContents.getType().equals(FileType.FOLDER)) {
                 throw new UploadException("Could not extract top level folder for upload.");
             }
             
             // iterate through the student folders (and the grades.csv file, if graded and included)
             for (FileObject fileObj : zipContents.findFiles(new FileDepthSelector(1,1)))
             {
                 if (fileObj.getType().equals(FileType.FOLDER))
                 {
                     List<StudentFeedbackWrapper> feedbackForStudent = processSubmissionFolder(fileObj, 
                             pattern, uploadInfo, assign.getContextId());
                     if (feedbackForStudent != null) {
                         feedbackUploadList.addAll(feedbackForStudent);
                     }
                 } else if (assign.isGraded() && 
                         assign.getGradebookItemId() != null &&
                         fileObj.getType().equals(FileType.FILE) && 
                         fileObj.getName().getExtension().equals("csv")) {
                     // upload the grades csv file
                     processGrades(assign.getContextId(), options.assignmentId, fileObj, uploadInfo, displayIdUserIdMap);
                    // release the grades
                    if (options.releaseGrades) {
                        gradebookLogic.releaseOrRetractGrades(assign.getContextId(), assign.getGradebookItemId(), true, null);
                    }
                 }
 
             }
             
             if (log.isDebugEnabled()) log.debug("Num feedback versions to update:" + feedbackUploadList.size());
 
         } catch (FileSystemException e) {
             throw new UploadException(e.getMessage(), e);
         }
 
         // now let's iterate through the feedback wrappers and create the map
         List<String> gradableStudents = permissionLogic.getGradableStudentsForUserForItem(currUserId, assign);
         Map<String, Collection<AssignmentSubmissionVersion>> studentUidVersionsMap = new HashMap<String, Collection<AssignmentSubmissionVersion>>();
         Date now = new Date();
         if (feedbackUploadList != null) {
             for (StudentFeedbackWrapper fbWrapper : feedbackUploadList) {
                 if (fbWrapper.studentEID != null) {
                     String studentUid = displayIdUserIdMap.get(fbWrapper.studentEID);
                     if (studentUid != null) {
                         // check to see if the current user is auth to update
                         // feedback for this student
                         if (!gradableStudents.contains(studentUid)) {
                             // we will add this error and skip this student
                             addToUploadInfoMap(uploadInfo, studentUid, UploadInfo.NO_GRADING_PERM_FOR_STUDENT);
                         } else {
                             AssignmentSubmissionVersion updatedVersion = new AssignmentSubmissionVersion();
                             updatedVersion.setFeedbackNotes(fbWrapper.feedbackNotes);
                             updatedVersion.setAnnotatedText(fbWrapper.annotatedText);
                             updatedVersion.setFeedbackAttachSet(fbWrapper.feedbackAttachments);
                             updatedVersion.setId(fbWrapper.submissionVersionId);
                             
                             if (options.releaseFeedback) {
                                 updatedVersion.setFeedbackReleasedDate(now);
                             }
 
                             Collection<AssignmentSubmissionVersion> existingVersionList = studentUidVersionsMap.get(studentUid);
                             if (existingVersionList == null) {
                                 existingVersionList = new ArrayList<AssignmentSubmissionVersion>();
                                 existingVersionList.add(updatedVersion);
                             } else {
                                 existingVersionList.add(updatedVersion);
                             }
 
                             studentUidVersionsMap.put(studentUid, existingVersionList);
                         }
                     }
                 }
             }
         }
         
         addToUploadInfoMap(uploadInfo, studentUidVersionsMap.size() + "", UploadInfo.NUM_STUDENTS_IDENTIFIED_FOR_UPDATE);
 
         // now let's do the actual update
         if (!studentUidVersionsMap.isEmpty()) {
             Set<String> studentsUpdated = submissionLogic.saveAllInstructorFeedback(assign, studentUidVersionsMap);
             if (studentsUpdated != null) {
             	addToUploadInfoMap(uploadInfo, studentsUpdated.size() + "", UploadInfo.NUM_STUDENTS_UPDATED);
             }
         }
 
         return uploadInfo;
     }
     
     private void processGrades(String contextId, Long assignmentId, FileObject gradesCsvFile, 
             List<Map<String, String>> uploadInfo, Map<String, String> displayIdUserIdMap) {
         if (gradesCsvFile != null) {
             // first, parse out the content
             List<List<String>> parsedContent;
             try
             {
                 parsedContent = uploadGrades.getCSVContent(gradesCsvFile.getContent().getInputStream());
             }
             catch (FileSystemException e)
             {
                 throw new UploadException("Unable to parse content of grades csv file");
             }
 
             List<String> studentsToRemove = new ArrayList<String>();
 
             // we need to do some validation on this file first
             List<String> invalidStudents = uploadGrades.getInvalidDisplayIdsInContent(displayIdUserIdMap, parsedContent);
             if (invalidStudents != null && !invalidStudents.isEmpty()) {
                 for (String invalidStudent : invalidStudents) {
                     addToUploadInfoMap(uploadInfo, invalidStudent, UploadInfo.INVALID_STUDENT_IN_CSV);
                 }
 
                 // remove these students from the file
                 studentsToRemove.addAll(invalidStudents);
             }
 
             List<String> studentsWithInvalidGrade = uploadGrades.getStudentsWithInvalidGradesInContent(parsedContent, contextId);
             if (studentsWithInvalidGrade != null && !studentsWithInvalidGrade.isEmpty()) {
                 for (String invalidStudent : studentsWithInvalidGrade) {
                     addToUploadInfoMap(uploadInfo, invalidStudent, UploadInfo.STUDENT_WITH_INVALID_GRADE_IN_CSV);
                 }
 
                 // remove these students from the file
                 studentsToRemove.addAll(studentsWithInvalidGrade);
             }
             
             if (!studentsToRemove.isEmpty()) {
                 parsedContent = uploadGrades.removeStudentsFromContent(parsedContent, studentsToRemove);
             }
             
             // now let's proceed with the grade upload!
             List<String> ungradableStudents = uploadGrades.uploadGrades(displayIdUserIdMap, assignmentId, parsedContent);
             if (ungradableStudents != null) {
                 for (String ungradableStudent : ungradableStudents) {
                     addToUploadInfoMap(uploadInfo, ungradableStudent, UploadInfo.NO_GRADING_PERM_FOR_STUDENT);
                 }
             }
         }
     }
 
     private List<StudentFeedbackWrapper> processSubmissionFolder(FileObject submissionFolder, 
             Pattern pattern, List<Map<String, String>> uploadInfo, String contextId) {
         List<StudentFeedbackWrapper> studentFeedbackList = new ArrayList<StudentFeedbackWrapper>();
         // first, extract the student's username from the folder name
         // we need to "decode" the folder name in case it has encoded chars
         String submissionFolderName = FormattedText.decodeNumericCharacterReferences(submissionFolder.getName().getBaseName());
         String studentEID = zipExportLogic.extractIdFromFolderName(submissionFolderName, pattern);
         if (studentEID == null) {
             addToUploadInfoMap(uploadInfo, getAffectedTargetWithParents(submissionFolder, 1), UploadInfo.UNABLE_TO_EXTRACT_USERNAME);
         } else {
 
             // iterate through all of the children folders.  there may be a "Feedback"
             // folder that represents fb without submission. otherwise, there should
             // be a folder for each submitted version
             String feedbackFolderName = zipExportLogic.getFeedbackFolderName();
             String feedbackFileName = zipExportLogic.getFeedbackFileName();
             String annotatedTextFileName = zipExportLogic.getAnnotatedTextFileName();
 
             try
             {
                 for (FileObject fileObj : submissionFolder.findFiles(new FileDepthSelector(1,1)))
                 {
                     if (fileObj.getType().equals(FileType.FOLDER))
                     {
                         String baseFolderName = fileObj.getName().getBaseName();
                         if (feedbackFolderName.equals(baseFolderName)) {
                             StudentFeedbackWrapper feedback = getFeedbackFolderContent(fileObj, 
                                     studentEID, null, feedbackFileName, annotatedTextFileName, 
                                     uploadInfo, 2, contextId);
                             studentFeedbackList.add(feedback);
                         } else {
                             // this must be feedback for a specific version
                             // try to extract the version id from the folder name
                             String versionAsString = zipExportLogic.extractIdFromFolderName(baseFolderName, pattern);
                             if (versionAsString == null) {
                                 addToUploadInfoMap(uploadInfo, getAffectedTargetWithParents(fileObj, 2), UploadInfo.UNABLE_TO_EXTRACT_VERSION);
                             } else {
                                 try {
                                     Long versionAsLong = new Long(versionAsString);
                                     FileObject feedbackFolder = fileObj.resolveFile(feedbackFolderName);
                                     if (feedbackFolder != null) {
                                         StudentFeedbackWrapper feedback = getFeedbackFolderContent(feedbackFolder, 
                                                 studentEID, versionAsLong, feedbackFileName, 
                                                 annotatedTextFileName, uploadInfo, 3, contextId);
                                         studentFeedbackList.add(feedback);
                                     }
                                 } catch (NumberFormatException nfe) {
                                     addToUploadInfoMap(uploadInfo, baseFolderName, UploadInfo.UNABLE_TO_EXTRACT_VERSION);
                                 }
 
                             }
                         }
                     } 
                 }
             }
             catch (FileSystemException e)
             {
                 throw new UploadException("Unable to process folders associated with username: " + studentEID);
             }
         }
 
         return studentFeedbackList;
     }
 
     private StudentFeedbackWrapper getFeedbackFolderContent(FileObject feedbackFolder, String studentEID, 
             Long versionId, String feedbackFileName, String annotatedTextFileName, 
             List<Map<String, String>> uploadErrors, int parentDepth, String contextId) {
         StudentFeedbackWrapper feedback = new StudentFeedbackWrapper(studentEID, versionId);
         try
         {
             for (FileObject fileObj : feedbackFolder.findFiles(new FileDepthSelector(1,1))) {
                 if (fileObj.getType().equals(FileType.FILE)) {
                     String baseFileName = fileObj.getName().getBaseName();
                     if (baseFileName.equals(zipExportLogic.getFeedbackFileName())) {
                         try {
                             String feedbackNotes = readIntoString(fileObj.getContent());
                             feedback.feedbackNotes = feedbackNotes;
                         } catch (IOException ioe) {
                             addToUploadInfoMap(uploadErrors, getAffectedTargetWithParents(fileObj, parentDepth), UploadInfo.FEEDBACK_FILE_UPLOAD_ERROR);
                             log.warn("An IOException occurred while extracting content " +
                                     "from feedback file for " + studentEID);
                         }
                     } else if (baseFileName.equals(annotatedTextFileName)) {
                         try {
                             String annotatedSubmittedText = readIntoString(fileObj.getContent());
                             feedback.annotatedText = annotatedSubmittedText;
                         } catch (IOException ioe) {
                             addToUploadInfoMap(uploadErrors, getAffectedTargetWithParents(fileObj, parentDepth), UploadInfo.ANNOTATED_TEXT_UPLOAD_ERROR);
                             log.warn("An IOException occurred while extracting content " +
                                     "from annotatedSubmittedText file for " + studentEID);
                         }
                     } else {
                         try {
                             if (isFileAttachmentValid(fileObj)) {
                                 FeedbackAttachment feedbackAttachment = constructFeedbackAttachment(fileObj, studentEID, contextId);
                                 if (feedbackAttachment != null) {
                                     feedback.feedbackAttachments.add(feedbackAttachment);
                                 }
                             }
                         } catch (UploadException ue) {
                             // there was an error creating the feedback attachment
                             addToUploadInfoMap(uploadErrors, getAffectedTargetWithParents(fileObj, parentDepth), UploadInfo.FEEDBACK_ATTACHMENT_ERROR);
                         }
                     }
                 }
             }
         }
         catch (FileSystemException e)
         {
             log.warn("A FileSystemException occurred while extracting feedback for " + studentEID);
         }
 
         return feedback;
     }
 
 
     private String readIntoString(FileContent fc) throws IOException
     {
         InputStream in = fc.getInputStream();
         StringBuilder buffer = new StringBuilder();
         int size = 2048;
         byte[] data = new byte[size];
 
         while ((size = in.read(data, 0, data.length)) > 0)
             buffer.append(new String(data, 0, size));
 
         return buffer.toString();
     }
 
     /**
      * 
      * @param feedbackFileObj
      * @param studentEID
      * @return FeedbackAttachment object with the attachmentReference populated with
      * a newly created content resource reference. returns null if attachment could
      * not be created from the given feedbackFileObj or if feedbackFileObj is not a file. 
      * Throws UploadException if an exception was encountered while creating the content resource
      * @throws UploadException
      */
     private FeedbackAttachment constructFeedbackAttachment(FileObject feedbackFileObj, String studentEID, String contextId) throws UploadException
     {
         FeedbackAttachment newAttachment = null;
         try
         {
             if (feedbackFileObj != null && feedbackFileObj.getType().equals(FileType.FILE)) {
                 String toolTitle = externalLogic.getToolTitle();
                 String fileName = feedbackFileObj.getName().getBaseName();
                 ResourceProperties properties = contentHosting.newResourceProperties();
                 properties.addProperty(ResourceProperties.PROP_DISPLAY_NAME, fileName);
                 String extension = feedbackFileObj.getName().getExtension();
                 String contentType = imageService.getContentType(extension);
                 // get the content in bytes
                 FileContent fileContent = feedbackFileObj.getContent();
                 byte contentAsBytes[] = new byte[(int)fileContent.getSize()];
                 fileContent.getInputStream().read(contentAsBytes);
 
                 ContentResource newResource = contentHosting.addAttachmentResource(fileName, 
                         contextId, toolTitle, contentType, contentAsBytes, properties);
 
                 newAttachment = new FeedbackAttachment();
                 newAttachment.setAttachmentReference(newResource.getId());
             }
         } catch (FileSystemException e) {
             log.warn("FileSystemException encountered while creating feedback attachment " +
                     "for " + studentEID + " during upload:" + e.getMessage());
             throw new UploadException(e.getMessage(), e);
         } catch (PermissionException e) {
             log.warn("PermissionException encountered while creating feedback attachment " +
                     "for " + studentEID + " during upload:" + e.getMessage());
             throw new UploadException(e.getMessage(), e);
         } catch (InconsistentException e) {
             log.warn("InconsistentException encountered while creating feedback attachment " +
                     "for " + studentEID + " during upload:" + e.getMessage());
             throw new UploadException(e.getMessage(), e);
         } catch (ServerOverloadException e) {
             log.warn("ServerOverloadException encountered while creating feedback attachment " +
                     "for " + studentEID + " during upload:" + e.getMessage());
             throw new UploadException(e.getMessage(), e);
         } catch (IdInvalidException e) {
             log.warn("IdInvalidException encountered while creating feedback attachment " +
                     "for " + studentEID + " during upload:" + e.getMessage());
             throw new UploadException(e.getMessage(), e);
         } catch (IdUsedException e) {
             log.warn("IdUsedException encountered while creating feedback attachment " +
                     "for " + studentEID + " during upload:" + e.getMessage());
             throw new UploadException(e.getMessage(), e);
         } catch (OverQuotaException e) {
             log.warn("OverQuotaException encountered while creating feedback attachment " +
                     "for " + studentEID + " during upload:" + e.getMessage());
             throw new UploadException(e.getMessage(), e);
         } catch (IOException e) {
             log.warn("IOException encountered while creating feedback attachment " +
                     "for " + studentEID + " during upload:" + e.getMessage());
             throw new UploadException(e.getMessage(), e);
         }
 
         return newAttachment;
     }
 
     /**
      * A wrapper for feedback extracted from the uploaded file for a
      * single student and submission version
      */
     public static class StudentFeedbackWrapper
     {
         private Long submissionVersionId = null;
         private String studentEID = null;
         private String feedbackNotes = "";
         private String annotatedText = "";
         private Set<FeedbackAttachment> feedbackAttachments = new HashSet<FeedbackAttachment>();
 
         public StudentFeedbackWrapper()
         {
         }
 
         public StudentFeedbackWrapper(String studentEID) {
             this.studentEID = studentEID;
         }
 
         public StudentFeedbackWrapper(String studentEID, Long submissionVersionId) {
             this.studentEID = studentEID;
             this.submissionVersionId = submissionVersionId;
         }
 
         public StudentFeedbackWrapper(Long submissionVersionId, String studentEID,
                 String feedbackComments, String annotatedSubmissionText,
                 Set<FeedbackAttachment> feedbackAttachments)
         {
             this.submissionVersionId = submissionVersionId;
             this.studentEID = studentEID;
             this.feedbackNotes = feedbackComments;
             this.annotatedText = annotatedSubmissionText;
             this.feedbackAttachments = feedbackAttachments;
         }
 
     }
 
     /**
      * add the affectedTarget (such as a studentUid) and the associated {@link UploadInfo}
      * value to the list of informational maps returned from the upload process. 
      * @param uploadInfo
      * @param affectedTarget
      * @param info
      */
     private void addToUploadInfoMap(List<Map<String, String>> uploadInfo, String affectedTarget, UploadInfo info) {
         Map<String, String> infoMap = new HashMap<String, String>();
         infoMap.put(UPLOAD_INFO, info.toString());
         infoMap.put(UPLOAD_PARAM, affectedTarget);
 
         uploadInfo.add(infoMap);
     }
 
     /**
      * 
      * @param fileObj
      * @param parentDepth
      * @return will trace back through the parent FileObjects until the parentDepth
      * is reached to construct the directory for the given fileObj. this is useful
      * since the FileObject.getName() structure includes the temp file info. this allows
      * us to extract to the level that the user will understand
      */
     private String getAffectedTargetWithParents(FileObject fileObj, int parentDepth) {
         String fullFileName = "";
         if (fileObj != null) {
             try {
                 int currDepth = 0;
                 FileObject currFileObj = fileObj;
                 while (currDepth < parentDepth) {
                     fullFileName = currFileObj.getName().getBaseName() + "/" + fullFileName;
                     currFileObj = currFileObj.getParent();
                     currDepth++;
                 }
             } catch (FileSystemException fse) {
                 fullFileName = fileObj.getName().toString();
             }
         }
 
         return fullFileName;
     }
     
     /**
      * 
      * @param file
      * @return true if we should proceed with adding this file as a feedback
      * attachment. false if the file is included in ATTACH_FILTER
      */
     private boolean isFileAttachmentValid(FileObject file) {
         boolean fileValid = false;
         List<String> fileNamesToAvoid = new ArrayList<String>();
         String[] filters = ATTACH_FILTER.split(";");
         for (String f : filters) {
             f = f.trim();
             fileNamesToAvoid.add(f);
         }
         // we need to weed out files that we don't want to save
         // such as the .DS_Store files added by Mac OS X
         String fileName = file.getName().getBaseName();
         if (!fileNamesToAvoid.contains(fileName)) {
             fileValid = true;
         }
         
         return fileValid;
     }
 }
