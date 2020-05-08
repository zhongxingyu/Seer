 package gov.usgs.gdp.servlet;
 
 import gov.usgs.gdp.bean.*;
 import gov.usgs.gdp.helper.FileHelper;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.*;
 
 import javax.servlet.RequestDispatcher;
 import javax.servlet.ServletException;
 import javax.servlet.http.*;
 
 import org.apache.commons.fileupload.DiskFileUpload;
 import org.apache.commons.fileupload.FileItem;
 import org.apache.commons.fileupload.FileItemFactory;
 import org.apache.commons.fileupload.MultipartStream;
 import org.apache.commons.fileupload.disk.DiskFileItemFactory;
 import org.apache.commons.fileupload.servlet.ServletFileUpload;
 import org.apache.log4j.Logger;
 
 /**
  * Servlet implementation class UploadFilesServlet
  * @author isuftin
  *
  */
 public class UploadFilesServlet extends HttpServlet {
 
     private static org.apache.log4j.Logger log = Logger.getLogger(UploadFilesServlet.class);
     private static final long serialVersionUID = 1L;
 
     /**
      * @see HttpServlet#HttpServlet()
      */
     public UploadFilesServlet() {
         super();
     }
 
     /**
      * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
      */
     @Override
     protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
         doPost(request, response);
     }
 
     /**
      * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
      */
     @Override
     protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
         XmlReplyBean xmlOutput = null;
         String command = request.getParameter("command");
 
         if ("upload".equals(command)) {
         	String applicationTempDir = System.getProperty("applicationTempDir") + FileHelper.getSeparator();
             String userDirectory = "";
             
             try {
                 userDirectory = uploadFiles(request, applicationTempDir);
             } catch (Exception e) {
                 xmlOutput = new XmlReplyBean(AckBean.ACK_FAIL, new ErrorBean(ErrorBean.ERR_FILE_UPLOAD, e));
                 RouterServlet.sendXml(xmlOutput, response);
                 return;
             }
             
             if ("".equals(userDirectory)) { // User directory could not be created
                 xmlOutput = new XmlReplyBean(AckBean.ACK_FAIL, new ErrorBean(ErrorBean.ERR_USER_DIR_CREATE));
                 RouterServlet.sendXml(xmlOutput, response);
                 return;
             }
 
             log.debug("Files successfully uploaded.");
             RequestDispatcher rd = request.getRequestDispatcher("/FileSelectionServlet?command=listfiles&userdirectory=" + userDirectory);
             rd.forward(request, response);
         }
     }
 
     /**
      * Scans the upload directory to build a List of type FilesBean
      * @param userDirectory
      * @return
      */
     @SuppressWarnings("unused")
     private List<FilesBean> populateUploadedFilesBean(String userDirectory) {
         List<FilesBean> result = new ArrayList<FilesBean>();
         Collection<File> uploadedFiles = FileHelper.getFileCollection(userDirectory, true);
         result = FilesBean.getFilesBeanSetList(uploadedFiles);
         return result;
     }
 
     /**
      * Save the uploaded files to a specified directory
      * @param request
      * @param directory
      * @return
      * @throws Exception
      */
     @SuppressWarnings("unchecked")
     private String uploadFiles(HttpServletRequest request, String applicationTempDir) throws Exception {
         log.debug("User uploading file(s).");
         
         // Utility method that determines whether the request contains multipart content (files)
         boolean isMultiPart = ServletFileUpload.isMultipartContent(request);
         
         
         if (!isMultiPart) {
             log.debug("ServletFileUpload.isMultipartContent(request) was false. Could not upload files.");
             return "";
         }
 
         String userDirectory = "";
         
         // Create a factory for disk-based file items
         FileItemFactory factory = new DiskFileItemFactory();
 
         // Constructs an instance of this class which
         // uses the supplied factory to create FileItem instances.
         ServletFileUpload upload = new ServletFileUpload(factory);
         
         List<FileItem> items = null;
 
         Object interimItems = upload.parseRequest(request);
 
         if (interimItems instanceof List<?>) {
             items = (List<FileItem>) interimItems;
             for (FileItem fileItem : items) {
             	if(fileItem.getFieldName().equals("userdirectory")) {
             		userDirectory = fileItem.getString();
             	}
             }
         }
 
         // Check for user subdirectory - Create the user directory.
        if (userDirectory == null || "".equals(userDirectory)) {
         	userDirectory = createUserDirectory();
         } else if (!FileHelper.doesDirectoryOrFileExist(applicationTempDir + userDirectory)) {
         	userDirectory = createUserDirectory(userDirectory);
         }
         
         if (FileHelper.saveFileItems(applicationTempDir + userDirectory, items)) return userDirectory;
         return "";
     }
 
     private String createUserDirectory(String path) {
     	String applicationTempDir = System.getProperty("applicationTempDir");
         String seperator = FileHelper.getSeparator();        
         String userTempDir = applicationTempDir + seperator + path;
     	 boolean wasCreated = FileHelper.createDir(userTempDir);
          if (wasCreated) {
              log.debug("User subdirectory created at: " + userTempDir);
              return path;
          }
 
          log.debug("User subdirectory could not be created at: " + path);
          log.debug("User will be unable to upload files for this session.");
          return "";
     }
     
     private String createUserDirectory() {
     	String userSubDir = Long.toString(new Date().getTime());
         return createUserDirectory(userSubDir);       
     }
 }
