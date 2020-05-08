 package gov.usgs.cida.gdp.filemanagement.servlet;
 
 import gov.usgs.cida.gdp.filemanagement.bean.List;
 import gov.usgs.cida.gdp.filemanagement.interfaces.geoserver.ManageWorkSpace;
 import gov.usgs.cida.gdp.utilities.FileHelper;
 import gov.usgs.cida.gdp.utilities.XmlUtils;
 import gov.usgs.cida.gdp.utilities.bean.Acknowledgement;
 import gov.usgs.cida.gdp.utilities.bean.Message;
 import gov.usgs.cida.gdp.utilities.bean.XmlReply;
 
 import java.io.File;
 import java.io.IOException;
 import java.net.URLDecoder;
 import java.text.ParseException;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 /**
  * Servlet implementation class FileSelectionServlet
  */
 public class GeoServerServlet extends HttpServlet {
 
     private static final long serialVersionUID = 1L;
     private static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(GeoServerServlet.class);
     /**
      * @see HttpServlet#HttpServlet()
      */
     public GeoServerServlet() {
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
         ManageWorkSpace mws = new ManageWorkSpace();
         String command = request.getParameter("command");
 
         String workspace = request.getParameter("workspace");
 
         String dataFileName = request.getParameter("datafile");
         String appTempDir = System.getProperty("applicationTempDir");
         String dataFileLoc = appTempDir + "upload-repository/" + dataFileName;
 
         String delimChar = request.getParameter("delim");
 
         String delim;
         if ("c".equals(delimChar)) {
             delim = ",";
         } else if ("s".equals(delimChar)) {
             delim = " ";
         } else if ("t".equals(delimChar)) {
             delim = "\t";
         } else if (delimChar == null) {
             delim = "";  // if command doesn't require a delim
         } else {
             sendReply(response, Acknowledgement.ACK_FAIL, "Invalid delimiter.");
             System.err.println("ERROR: invalid delimiter: " + delimChar);
             return;
         }
 
         if ("createdatastore".equals(command)) {
             String shapefilePath = URLDecoder.decode(request.getParameter("shapefilepath"), "UTF-8");
			String srsCode = URLDecoder.decode(request.getParameter("srs-code"), "UTF-8");
             String shapefileName = shapefilePath.substring(
                     shapefilePath.lastIndexOf(File.separator) + 1,
                     shapefilePath.lastIndexOf("."));
 
            if (!mws.createDataStore(shapefilePath, shapefileName, workspace, srsCode)) {
                 sendReply(response, Acknowledgement.ACK_FAIL, "Could not create data store.");
             } else {
                 // send back ack with workspace and layer names
                 sendReply(response, Acknowledgement.ACK_OK, workspace, shapefileName);
             }
 
         } else if ("getdatafileselectables".equals(command)) {
             // return list of dates in data file
             ArrayList<String> dates = mws.parseDates(new File(dataFileLoc), delim);
 
             XmlReply xmlReply = new XmlReply(Acknowledgement.ACK_OK, new List(dates));
             XmlUtils.sendXml(xmlReply, Long.valueOf(new Date().getTime()), response);
 
         } else if ("createcoloredmap".equals(command)) {
             // create style to color polygons given a date, stat, and data file
             String shapefileName = request.getParameter("shapefilename");
             String fromDate = request.getParameter("fromdate");
             String toDate = request.getParameter("todate");
             String attribute = request.getParameter("attribute");
             String stat = request.getParameter("stat");
             try {
                 mws.createColoredMap(dataFileLoc, workspace, shapefileName, fromDate, toDate, stat, attribute, delim);
             } catch (ParseException ex) {
                 Logger.getLogger(GeoServerServlet.class.getName()).log(Level.SEVERE, null, ex);
             }
 
             sendReply(response, Acknowledgement.ACK_OK);
         } else if ("clearcache".equals(command)) {
             //clearCache();
         }
     }
 
     void sendReply(HttpServletResponse response, int status, String... messages) throws IOException {
         XmlReply xmlReply = new XmlReply(status, new Message(messages));
         XmlUtils.sendXml(xmlReply, Long.valueOf(new Date().getTime()), response);
     }
 }
