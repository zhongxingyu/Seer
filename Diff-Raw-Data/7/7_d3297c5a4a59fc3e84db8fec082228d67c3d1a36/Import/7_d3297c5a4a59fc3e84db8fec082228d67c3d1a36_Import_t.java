 package eionet.meta;
 
 import javax.servlet.http.*;
 import javax.servlet.*;
 import javax.xml.parsers.*;
 
 import org.xml.sax.*;
 import java.io.*;
 import java.util.*;
 import java.net.*;
 
 import java.sql.Connection;
 import java.sql.SQLException;
 
 import eionet.meta.imp.*;
 import eionet.util.Props;
 import eionet.util.PropsIF;
 import eionet.util.SecurityUtil;
 import eionet.util.Util;
 
 import com.tee.xmlserver.AppUserIF;
 import com.tee.uit.security.*;
 
 public class Import extends HttpServlet {
 
     private static final int BUF_SIZE = 1024;
     private static final String START_XML_STRING = "<?xml";
     
     private static final String TMP_FILE_PREFIX = "import_";
 
     private static PrintStream s = System.out;
 
     private static boolean validation = false;
     private static String  drv = "";
     private static String  url = "";
     private static String  usr = "";
     private static String  psw = "";
     private static String  xmlDir = "";
 
     private static File[]  xmlFiles;
 
     private static Hashtable conf;
     
     /**
     * Override the servlet's doGet()
     */
     protected void doGet(HttpServletRequest req, HttpServletResponse res)
                     throws ServletException, java.io.IOException {
 
 		req.setCharacterEncoding("UTF-8");
 		
        req.getRequestDispatcher("import_results.jsp").forward(req, res);
     }
 
     /**
     * Override the servlet's doPost()
     */
     protected void doPost(HttpServletRequest req, HttpServletResponse res)
                     throws ServletException, java.io.IOException {
 
 		req.setCharacterEncoding("UTF-8");
 						
         ServletContext ctx = getServletContext();
         
         // init response text and exception indicator
 
         StringBuffer responseText = new StringBuffer();
         boolean bException = false;
 
         // authenticate user
         AppUserIF user = SecurityUtil.getUser(req);
         try{
 			AccessControlListIF acl = AccessController.getAcl("/import");
			if (user==null || !SecurityUtil.hasPerm(user.getUserName(), "/import", "x")){
 				responseText.append("<h1>Not allowed!</h1><br/>");
 				bException = true;
 			}
         }
        catch (Exception e){
			responseText.append("<h1>" + e.toString() + "</h1><br/>");
 			bException = true;
         }
 
         // get content type, check that it's valid
         String contentType = req.getContentType();
         if (contentType == null ||
             !(contentType.toLowerCase().startsWith("multipart/form-data") ||
             contentType.toLowerCase().startsWith("text/xml"))){
             responseText.append("<h1>Posted content type is unknown!</h1>");
             bException = true;
         }
 
         // set the response content type
         res.setContentType("text/html");
 
 
         // start processing the request
 
         String boundary = null;
         String sUrl = null;
         String type = null;
         String delem_id = null;
 
         if (contentType.toLowerCase().startsWith("multipart/form-data")){
 
             // file upload, multipart request
 
             String fileORurl = req.getParameter("fileORurl");
             type = req.getParameter("type");
             delem_id = req.getParameter("delem_id");
 
             if (fileORurl != null && fileORurl.equalsIgnoreCase("url"))
                 sUrl = req.getParameter("url_input");
 
             // extract the multipart request's boundary string
             boundary = extractBoundary(contentType);
         }
 
         // check that the element type is valid
         if (type == null){
             responseText.append("<h1>Failed to get the import type!</h1><br/>");
             bException = true;
         }
         else{
            // check that the element id is valid, when type is FXV
             if (type.equals("FXV") && delem_id == null){
                 responseText.append("<h1>Failed to get data element id!</h1><br/>");
                 bException = true;
             }
         }
 
         // since the data is going to be saved into a file
         // (regardless of whether we have a file upload or
         // URL stream, we initialize the file
         String tmpFilePath = Props.getProperty(PropsIF.TEMP_FILE_PATH);
         if (tmpFilePath == null)
             tmpFilePath = System.getProperty("user.dir");
 
         if (!tmpFilePath.endsWith(File.separator))
             tmpFilePath = tmpFilePath + File.separator;
 
         StringBuffer tmpFileName = new StringBuffer(tmpFilePath + TMP_FILE_PREFIX);
         tmpFileName.append("_");
         tmpFileName.append(req.getRequestedSessionId().replace('-', '_'));
         tmpFileName.append("_");
         tmpFileName.append(System.currentTimeMillis());
         tmpFileName.append(".xml");
 
         File file = new File(tmpFileName.toString());
         RandomAccessFile raFile = new RandomAccessFile(file, "rw");
 
         // set up handler
         BaseHandler handler = new DatasetImportHandler();
 
         // if no exceptions, get the data and save to file, parse
         if (!bException){
         	
         	Connection userConn = user.getConnection();
 
             try{
                 // get the data and save to file
                 if (sUrl == null){
                     writeToFile(raFile, req.getInputStream(), boundary);
                 }
                 else{
                     URL url = new URL(sUrl);
                     HttpURLConnection httpConn = (HttpURLConnection)url.openConnection();
 
                     writeToFile(raFile, url.openStream());
                 }
 
                 // parse the file
 
                 SAXParserFactory factory = SAXParserFactory.newInstance();
                 SAXParser parser = factory.newSAXParser();
                 XMLReader reader = parser.getXMLReader();
                 reader.setContentHandler(handler); // pass our handler to SAX
 
                 reader.parse(tmpFileName.toString());
 
                 // SAX was OK, but maybe handler problems of its own
                 if (!handler.hasError()){
 
                     DatasetImport dbImport =
                         new DatasetImport((DatasetImportHandler)handler, userConn, ctx, type);
 					dbImport.setUser(user);
 					dbImport.setDate(String.valueOf(System.currentTimeMillis()));
                     dbImport.setImportType(type);
                     if (type.equals("FXV")) {
                         dbImport.setParentID(delem_id);
                     }
                     dbImport.execute();
 
                     responseText.append(dbImport.getResponseText());
                 }
                 else{
                     throw new Exception(handler.getErrorBuff().toString());
                 }
             }
             catch (Exception e){
 
                 StringBuffer msg = new StringBuffer();
                 int lineNumber = handler.getLine();
                 if (lineNumber>0)
                     msg.append(" Line ").append(String.valueOf(lineNumber)).
                     append("\n");
 
                 msg.append(Util.getStack(e));
                 responseText.append("<h1>Data Dictionary importer encountered").
                 	append(" an exception:</h1><br/>").append(msg.toString()).
                 	append("<br/><br/>");
             }
             catch (OutOfMemoryError oome){
 
                 StringBuffer msg = new StringBuffer(oome.toString());
                 int lineNumber = handler.getLine();
                 if (lineNumber>0)
                     msg.append(" Line " + String.valueOf(lineNumber));
 
                 responseText.append("<h1>Data Dictionary importer encountered an exception:" +
                                     "</h1><br/>" + msg.toString() + "<br/><br/>");
             }
             finally{
             	try{
             		if (userConn!=null) userConn.close();
             	}
             	catch (SQLException e){}
             }
             
             // if was fixed values import explicitly, add a link back to the element
             if (type.equals("FXV")){
 				responseText.append("<br><br><a href='data_element.jsp?mode=view&delem_id=" + delem_id + "'>Back to data element</a>");
             }
         }
 
 
         file.delete();
 
         
 
         req.setAttribute("TEXT", responseText.toString());
         doGet(req, res);
     }
 
     /**
     * Write to file, if import goes through URL
     */
     private void writeToFile(RandomAccessFile raFile, InputStream in) throws Exception{
         
         byte[] buf = new byte[BUF_SIZE];
         int i;
         while ((i=in.read(buf, 0, buf.length)) != -1){
             raFile.write(buf, 0, i);
         }
             
         raFile.close();
         in.close();
     }
     
     /**
     * Write to file, if import goes through a file upload
     */
     private void writeToFile(RandomAccessFile raFile, ServletInputStream in, String boundary) throws Exception{
         
         byte[] buf = new byte[BUF_SIZE];
         int i;
         
         ByteArrayOutputStream bout = new ByteArrayOutputStream();
         boolean fileStart = false;
         boolean pastContentType = false;
         do{
             int b = in.read();
             if (b == -1) break; // if end of stream, break
             
             bout.write(b);
             
             if (!pastContentType){ // if Content-Type not passed, no check of LNF
                 String s = bout.toString();
                 if (s.indexOf("Content-Type") != -1)
                     pastContentType = true;
             }
             else{
                 // Content-Type is passed, after next double LNF is file start
                 byte[] bs = bout.toByteArray();
                 if (bs != null && bs.length >= 4){
                     if (bs[bs.length-1]==10 &&
                         bs[bs.length-2]==13 &&
                         bs[bs.length-3]==10 &&
                         bs[bs.length-4]==13){
                         
                         fileStart = true;
                     }
                 }
             }
         }
         while(!fileStart);
         
         while ((i=in.readLine(buf, 0, buf.length)) != -1){
             String line = new String(buf, 0, i);
             if (boundary != null && line.startsWith(boundary))
                 break;
             raFile.write(buf, 0, i);
         }
             
         raFile.close();
         in.close();
     }
     
     /**
     * Extract the boundary string in multipart request
     */
     private String extractBoundary(String contentType){
         int i = contentType.indexOf("boundary=");
         if (i == -1) return null;
         String boundary = contentType.substring(i + 9); // 9 for "boundary="
         return "--" + boundary; // the real boundary is always preceded by an extra "--"
     }
 }
