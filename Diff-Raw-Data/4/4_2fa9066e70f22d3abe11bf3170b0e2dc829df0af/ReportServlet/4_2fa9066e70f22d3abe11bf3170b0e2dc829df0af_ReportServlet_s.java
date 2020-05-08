 package be.example.jasper.servlet;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.util.HashMap;
import java.util.Locale;
 import java.util.Map;
 
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import net.sf.jasperreports.engine.JRException;
 import net.sf.jasperreports.engine.JRParameter;
 import net.sf.jasperreports.engine.JasperReport;
 import net.sf.jasperreports.engine.query.JRXPathQueryExecuterFactory;
 import net.sf.jasperreports.engine.util.JRXmlUtils;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.w3c.dom.Document;
 
 import be.example.jasper.ReportGenerator;
 
 /**
  * This is a simple servlet that creates a pdf report using JasperReports and offers it as a download by writing it to the
  * {@link HttpServletResponse}'s {@link OutputStream}.
  * 
  * @author aperjor
  * 
  */
 public class ReportServlet extends HttpServlet {
 
     private static final long serialVersionUID = 1L;
     private static final Logger LOGGER = LoggerFactory.getLogger(ReportServlet.class);
 
     @Override
     protected void doGet(final HttpServletRequest request, final HttpServletResponse response) throws ServletException, IOException {
         LOGGER.debug("generate the report");
         ReportGenerator reportGenerator = new ReportGenerator();
         Map<String, Object> params;
         params = new HashMap<String, Object>();
         params.put(ReportGenerator.SUBREPORT_DIR_PARAM, ReportGenerator.SUBREPORT_DIR);
         params.put(JRParameter.REPORT_LOCALE, request.getLocale());
         try {
             InputStream xmlData = getClass().getResourceAsStream("/data/northwind.xml");
             Document document = JRXmlUtils.parse(xmlData);
             params.put(JRXPathQueryExecuterFactory.PARAMETER_XML_DATA_DOCUMENT, document);
             JasperReport jasperReport = ReportGenerator.getCompiledReport("jasper/CustomersReport.jasper");
             prepareDownload(response, "report.pdf");
             reportGenerator.exportReport(params, jasperReport, response.getOutputStream());
             response.getOutputStream().close();
         } catch (JRException e) {
             LOGGER.error("error while generating the report", e);
            response.sendError(404, "unable to generate report");
         }
     }
 
     private void prepareDownload(final HttpServletResponse response, final String filename) {
         response.setContentType("application/pdf");
         response.setHeader("Content-Disposition", "attachment;filename=" + filename);
         response.setHeader("Expires", "0");
         response.setHeader("Pragma", "cache");
         response.setHeader("Cache-Control", "private");
     }
 
 }
