 /**
  * 
  */
 package com.berbatik.rudi.jaspersandservlet;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.StringBufferInputStream;
 import java.sql.Connection;
 import java.sql.DriverManager;
 import java.util.HashMap;
 
 import javax.servlet.ServletException;
 import javax.servlet.annotation.WebServlet;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import net.sf.jasperreports.engine.JasperCompileManager;
 import net.sf.jasperreports.engine.JasperExportManager;
 import net.sf.jasperreports.engine.JasperFillManager;
 import net.sf.jasperreports.engine.JasperPrint;
 import net.sf.jasperreports.engine.JasperReport;
 import net.sf.jasperreports.engine.JasperRunManager;
 
 import org.apache.commons.io.IOUtils;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 /**
  * @author rudi
  *
  */
 @SuppressWarnings("serial")
 @WebServlet(urlPatterns="/jasper/*")
 public class JasperServlet extends HttpServlet {
 
 	private transient Logger log = LoggerFactory.getLogger(JasperServlet.class);
 	private Connection jdbc;
 
 	@Override
 	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
 			throws ServletException, IOException {
 //		resp.getWriter().write("Hello world");
 		
 		try {
 			InputStream reportStream3 = getClass().getResourceAsStream("/sales_order.jrxml");
 			JasperReport report = JasperCompileManager.compileReport(reportStream3);
 			
 			InputStream reportStream2 = getClass().getResourceAsStream("/sales_order.jrxml");
 			final String reportStr = IOUtils.toString(reportStream2);
 			log.info("JRXML {}", reportStr);
 //			JasperRunManager.
 			
 	//		InputStream reportStream = getClass().getResourceAsStream("/sales_order.jrxml");
 			StringBufferInputStream reportStream = new StringBufferInputStream(reportStr);
 			
 			Class.forName("com.mysql.jdbc.Driver");
 			jdbc = DriverManager.getConnection(
 				"jdbc:mysql://localhost:3306/berbatik_magento?" +
 				"user=root&password=bippo");
 			JasperPrint print = JasperFillManager.fillReport(report, new HashMap<String, Object>(), jdbc);
 			
 //		facesContext.responseComplete();
 			resp.setContentType("application/pdf");
 //		resp.setContentType("text/html");
 //		JasperRunManager.runReportToHtmlFile(sourceFileName, params, conn)
//			JasperRunManager.runReportToPdfStream(reportStream3,
//					resp.getOutputStream(), new HashMap<String, Object>(), jdbc);
 //			JasperRunManager.runReportToPdfStream(reportStream,
 //					resp.getOutputStream(), new HashMap<String, Object>(), jdbc);
 			JasperExportManager.exportReportToPdfStream(print, resp.getOutputStream());
 			
 			// yang di bawah ini juga bisa, asalkan inputnya adalah object JasperReport
 			// bukan Stream (baik file .jasper maupun .jrxml gagal)
 //			byte[] output = JasperRunManager.runReportToPdf(report,
 //					new HashMap<String, Object>(), jdbc);
 //			resp.getOutputStream().write(output);
 			jdbc.close();
 			resp.setStatus(200);
 		} catch (Exception e) {
 			throw new RuntimeException("Can't connect to berbatik_magento", e);
 		}
 	}
 
 }
