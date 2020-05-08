 package gov.usgs.gdp.servlet;
 
 import gov.usgs.gdp.bean.AckBean;
 import gov.usgs.gdp.bean.ErrorBean;
 import gov.usgs.gdp.bean.XmlReplyBean;
 
 import java.io.IOException;
 import java.io.Writer;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.Map;
 
 import javax.servlet.RequestDispatcher;
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import org.apache.commons.fileupload.servlet.ServletFileUpload;
 import org.apache.log4j.Logger;
 
 import com.sun.xml.fastinfoset.DecoderStateTables;
 
 /**
  * Servlet implementation class RouterServlet
  */
 public class RouterServlet extends HttpServlet {
 
     private static final long serialVersionUID = 1L;
     private static org.apache.log4j.Logger log = Logger.getLogger(RouterServlet.class);
     
     private static final Map<String, String> servletCommandMappings = new HashMap<String, String>();
     static {
     	servletCommandMappings.put("getoutputstats", "/FileProcessServlet");
     	servletCommandMappings.put("listfiles", "/FileSelectionServlet");
     	servletCommandMappings.put("listattributes", "/FileAttributeServlet");
     	servletCommandMappings.put("listfeatures", "/FileFeatureServlet");
     	servletCommandMappings.put("listservers", "/THREDDSCheckServlet");
     	servletCommandMappings.put("checkserver", "/THREDDSCheckServlet");
     	servletCommandMappings.put("createdatastore", "/GeoServerServlet");
     	servletCommandMappings.put("createuserdirectory", "/UploadFilesServlet");
     	servletCommandMappings.put("getdatafileselectables", "/GeoServerServlet");
     	servletCommandMappings.put("createcoloredmap", "/GeoServerServlet");
     	servletCommandMappings.put("getdatasetlist", "/THREDDSServlet");
     	servletCommandMappings.put("getgridlist", "/THREDDSServlet");
     	servletCommandMappings.put("getcatalog", "/THREDDSServlet");
     	servletCommandMappings.put("calculatewcscoverageinfo", "/WCSServlet");
     	servletCommandMappings.put("gettimerange", "/THREDDSServlet");
     	servletCommandMappings.put("submitforprocessing", "/FileProcessServlet");
     	servletCommandMappings.put("outputtypelist", "/FileAttributeServlet");
     	servletCommandMappings.put("getfile", "/FileProcessServlet");
     	servletCommandMappings.put("checkuploadfile", "/FileProcessServlet");
     	servletCommandMappings.put("commandlist", "/SummaryServlet");
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
         @SuppressWarnings("unchecked")
         Map<String, String> requestParameters = request.getParameterMap();
 
         // If the user is attempting to upload files, send them directly to the correct servlet
         if (ServletFileUpload.isMultipartContent(request)) {
             log.info("User is attempting to upload files.");
             RequestDispatcher rd = request.getRequestDispatcher("/UploadFilesServlet?command=upload");
             rd.forward(request, response);
             return;
         }
 
         // Must go after above isMultiPartContent block since that block doesn't contain a command parameter
         if (!requestParameters.containsKey("command")) {
             log.info("User did not send command");
             ErrorBean errorBean = new ErrorBean(ErrorBean.ERR_NO_COMMAND);
             XmlReplyBean xmlReply = new XmlReplyBean(AckBean.ACK_FAIL, errorBean);
             RouterServlet.sendXml(xmlReply, start, response);
             return;
         }
 
         String command = request.getParameter("command");
         
         String forwardToServlet = servletCommandMappings.get(command);
         if (forwardToServlet != null) {
         	log.info(command);
         	RequestDispatcher rd = request.getRequestDispatcher(forwardToServlet);
         	rd.forward(request, response);
         } else {
         	log.info("No such command");
         	ErrorBean errorBean = new ErrorBean(ErrorBean.ERR_NO_COMMAND);
             XmlReplyBean xmlReply = new XmlReplyBean(AckBean.ACK_FAIL, errorBean);
             RouterServlet.sendXml(xmlReply, start, response);
         	return;
         }
     }
 
     public static void sendXml(String xml, Long startTime, HttpServletResponse response) throws IOException {
         Writer writer = response.getWriter();
         try {
             response.setContentType("text/xml");
             response.setCharacterEncoding("utf-8");
             char[] characters = xml.toCharArray();
             for (int index = 0; index < characters.length; ++index) {
                 char current = characters[index];
                 if (DecoderStateTables.UTF8(current) == DecoderStateTables.STATE_ILLEGAL) current = '\u00BF';
                 writer.write(current);
             }
             writer.flush();
         } finally {
             if (writer != null) writer.close();
         }
         log.debug(xml);
         log.info("Process completed in " + (new Date().getTime() - startTime.longValue()) + " milliseconds.");
     }
 
     public static void sendXml(XmlReplyBean xmlReply, Long startTime, HttpServletResponse response) throws IOException {
         sendXml(xmlReply.toXml(), startTime, response);
     }
 }
