 package gov.usgs.gdp.servlet;
 
 import gov.usgs.gdp.bean.AckBean;
 import gov.usgs.gdp.bean.ErrorBean;
 import gov.usgs.gdp.bean.TimeBean;
 import gov.usgs.gdp.bean.XmlBean;
 import gov.usgs.gdp.bean.XmlReplyBean;
 import gov.usgs.gdp.helper.THREDDSServerHelper;
 
 import java.io.IOException;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.net.URLDecoder;
 import java.text.ParseException;
 import java.util.Date;
 import java.util.List;
 
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import org.apache.commons.httpclient.HttpClient;
 import org.apache.commons.httpclient.HttpException;
 import org.apache.commons.httpclient.HttpMethod;
 import org.apache.commons.httpclient.HttpStatus;
 import org.apache.commons.httpclient.methods.GetMethod;
 import org.apache.log4j.Logger;
 
 /**
  * Servlet implementation class THREDDSServlet
  */
 public class THREDDSServlet extends HttpServlet {
 
     private static final long serialVersionUID = 1L;
     private static org.apache.log4j.Logger log = Logger.getLogger(THREDDSServlet.class);
 
     /**
      * @see HttpServlet#HttpServlet()
      */
     public THREDDSServlet() {
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
         
         String command = request.getParameter("command");
         XmlReplyBean xmlReply = null;
 
         if ("getcatalog".equals(command)) {
         	URL urlObject = null;
         	try {
         		urlObject = new URL(URLDecoder.decode(request.getParameter("url"), "UTF-8"));
         	}catch (MalformedURLException e) {
                 xmlReply = new XmlReplyBean(AckBean.ACK_FAIL, new ErrorBean(ErrorBean.ERR_INVALID_URL));
                 RouterServlet.sendXml(xmlReply, start, response);
                 return;
             }
 
             HttpClient client = new HttpClient();
             HttpMethod method = new GetMethod(urlObject.toString());
             
             try {
                 int statusCode = client.executeMethod(method);
 
                 if (statusCode != HttpStatus.SC_OK) {
                     System.err.println("Method failed: " + method.getStatusLine());
                 }
                 // Read the response body.
                 byte[] responseBody = method.getResponseBody();
 
                 // Deal with the response.
                 // Use caution: ensure correct character encoding and is not binary data
                 String xmlResponse = new String(responseBody);
                 method.releaseConnection();
                 RouterServlet.sendXml(xmlResponse, start, response);
                 return;
             } catch (HttpException e) {
                 xmlReply = new XmlReplyBean(AckBean.ACK_FAIL, new ErrorBean(ErrorBean.ERR_PROTOCOL_VIOLATION));
                 RouterServlet.sendXml(xmlReply, start, response);
                 return;
             } catch (IOException e) {
                 xmlReply = new XmlReplyBean(AckBean.ACK_FAIL, new ErrorBean(ErrorBean.ERR_TRANSPORT_ERROR));
                 RouterServlet.sendXml(xmlReply, start, response);
                 return;
             } finally {
                 // Release the connection.
                 method.releaseConnection();
             }
         }
 
         if ("getgridlist".equals(command)) {
             log.debug("User has chosen to list shapefile attributes");
 
             // Grab what we need to work with for this request
             String datasetUrl = request.getParameter("dataseturl");
             if (datasetUrl == null || "".equals(datasetUrl)) {
                 xmlReply = new XmlReplyBean(AckBean.ACK_FAIL, new ErrorBean(ErrorBean.ERR_MISSING_PARAM));
                 RouterServlet.sendXml(xmlReply, start, response);
                 return;
             }
 
             List<XmlBean> gridBeanList = null;
             try {
                 gridBeanList = THREDDSServerHelper.getGridBeanListFromServer(datasetUrl);
             } catch (IOException e) {
                 xmlReply = new XmlReplyBean(AckBean.ACK_FAIL, new ErrorBean(ErrorBean.ERR_INVALID_URL));
                 RouterServlet.sendXml(xmlReply, start, response);
                 return;
             }
             XmlReplyBean xrb = new XmlReplyBean(AckBean.ACK_OK, gridBeanList);
             RouterServlet.sendXml(xrb, start, response);
             return;
         }
 
         if ("gettimerange".equals(command)) {
             String datasetUrl = request.getParameter("dataseturl");
            String gridSelection = request.getParameter("grid");
             TimeBean timeBean = null;
             try {
                 timeBean = THREDDSServerHelper.getTimeBean(datasetUrl, gridSelection);
             } catch (ParseException e) {
                 xmlReply = new XmlReplyBean(AckBean.ACK_FAIL, new ErrorBean(ErrorBean.ERR_MISSING_TIMERANGE));
                 RouterServlet.sendXml(xmlReply, start, response);
                 return;
             }
 
             if (timeBean == null) {
                 xmlReply = new XmlReplyBean(AckBean.ACK_FAIL, new ErrorBean(ErrorBean.ERR_MISSING_TIMERANGE));
                 RouterServlet.sendXml(xmlReply, start, response);
                 return;
             }
 
 
 
             XmlReplyBean xrb = new XmlReplyBean(AckBean.ACK_OK, timeBean);
             RouterServlet.sendXml(xrb, start, response);
             return;
         }
 
     }
 }
