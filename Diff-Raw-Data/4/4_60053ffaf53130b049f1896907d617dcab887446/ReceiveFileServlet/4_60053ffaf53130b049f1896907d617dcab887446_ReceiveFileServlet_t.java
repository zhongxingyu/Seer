 package gov.usgs.cida.gdp.filemanagement.servlet;
 
 import gov.usgs.cida.gdp.utilities.FileHelper;
 import gov.usgs.cida.gdp.utilities.XmlUtils;
 import gov.usgs.cida.gdp.utilities.bean.AckBean;
 import gov.usgs.cida.gdp.utilities.bean.ErrorBean;
 import gov.usgs.cida.gdp.utilities.bean.UserDirectoryBean;
 import gov.usgs.cida.gdp.utilities.bean.XmlReplyBean;
 
 import java.io.IOException;
 import java.util.*;
 
 import javax.servlet.RequestDispatcher;
 import javax.servlet.ServletException;
 import javax.servlet.http.*;
 
 import org.apache.commons.fileupload.FileItem;
 import org.apache.commons.fileupload.FileItemFactory;
 import org.apache.commons.fileupload.disk.DiskFileItemFactory;
 import org.apache.commons.fileupload.servlet.ServletFileUpload;
 import org.apache.log4j.Logger;
 
 /**
  * Servlet implementation class ReceiveFileServlet
  * @author isuftin
  *
  */
 public class ReceiveFileServlet extends HttpServlet {
 
 	private static final long serialVersionUID = 6766229674722132238L;
 	private static org.apache.log4j.Logger log = Logger.getLogger(ReceiveFileServlet.class);
 
     /**
      * @see HttpServlet#HttpServlet()
      */
     public ReceiveFileServlet() {
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
         Long start = Long.valueOf(new Date().getTime());
 
         XmlReplyBean xmlOutput = null;
         String command = request.getParameter("command");
 
        if (ServletFileUpload.isMultipartContent(request)) {
             String applicationUserspaceDir = System.getProperty("applicationUserSpaceDir");
             String userDirectory = "";
 
             try {
                 userDirectory = uploadFiles(request, applicationUserspaceDir);
             } catch (Exception e) {
                 xmlOutput = new XmlReplyBean(AckBean.ACK_FAIL, new ErrorBean(ErrorBean.ERR_FILE_UPLOAD, e));
                 XmlUtils.sendXml(xmlOutput, start, response);
                 return;
             }
 
             if ("".equals(userDirectory)) { // User directory could not be created
                 xmlOutput = new XmlReplyBean(AckBean.ACK_FAIL, new ErrorBean(ErrorBean.ERR_USER_DIR_CREATE));
                 XmlUtils.sendXml(xmlOutput, start, response);
                 return;
             }
 
             // Work directly into another webservice to list the files available
             // including the file the user just uploaded
             log.debug("Files successfully uploaded.");
             RequestDispatcher rd = request.getRequestDispatcher("/FileSelectionServlet?command=listfiles&userdirectory=" + userDirectory);
             rd.forward(request, response);
            return;
         } else if ("createuserdirectory".equals(command)) {
             String dir = FileHelper.createUserDirectory(System.getProperty("applicationUserSpaceDir"));
 
             XmlReplyBean xmlReply;
             if ("".equals(dir)) {
                 xmlReply = new XmlReplyBean(AckBean.ACK_FAIL);
             } else {
                 Cookie c = new Cookie("gdp-user-directory", dir);
                 c.setMaxAge(-1); // set cookie to be deleted when web browser exits
                 c.setPath("/");  // set cookie's visibility to the whole app
                 response.addCookie(c); // add cookie to the response for the client browser to consume
                 UserDirectoryBean udb = new UserDirectoryBean();
                 udb.setDirectory(dir);
                 xmlReply = new XmlReplyBean(AckBean.ACK_OK, udb);
             }
 
             XmlUtils.sendXml(xmlReply, start, response);
         }
     }
 
     /**
      * Save the uploaded files to a specified directory
      * @param request
      * @param directory
      * @return
      * @throws Exception 
      */
     private String uploadFiles(HttpServletRequest request, String applicationTempDir) throws Exception {
         log.debug("User uploading file(s).");
 
         // Create a factory for disk-based file items
         FileItemFactory factory = new DiskFileItemFactory();
 
         // Constructs an instance of this class which
         // uses the supplied factory to create FileItem instances.
         ServletFileUpload upload = new ServletFileUpload(factory);
 
         Cookie[] cookies = request.getCookies();
         String userDirectory = "";
         for (int cookieIndex = 0;cookieIndex < cookies.length;cookieIndex++) {
         	if ("gdp-user-directory".equals(cookies[cookieIndex].getName().toLowerCase())) {
         		userDirectory = cookies[cookieIndex].getValue();
         	}
         }
 
         Object interimItems = upload.parseRequest(request);
         @SuppressWarnings("unchecked")
         List<FileItem> items = (List<FileItem>) interimItems;
 
         // Save the file(s) to the user directory
         if (FileHelper.saveFileItems(applicationTempDir + userDirectory, items)) {
             return userDirectory;
         }
         return "";
     }
 }
