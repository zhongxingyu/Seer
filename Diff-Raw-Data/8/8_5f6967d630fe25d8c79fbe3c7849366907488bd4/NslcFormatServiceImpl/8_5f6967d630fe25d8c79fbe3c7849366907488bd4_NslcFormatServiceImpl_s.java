 package com.ritchey.server;
 
 import java.io.File;
 import java.io.IOException;
 import java.io.OutputStream;
 import java.util.HashMap;
 import java.util.Map;
 
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import javax.xml.parsers.SAXParserFactory;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 
 import com.browsexml.core.XmlObject;
 import com.browsexml.core.XmlParser;
 
 import edu.bxml.format.Property;
 import edu.bxml.format.Select;
 
 public class NslcFormatServiceImpl  extends HttpServlet {
   public void doGet(HttpServletRequest request,
                     HttpServletResponse response)
       throws ServletException, IOException {
 	  
 	  Log log = LogFactory.getLog(NslcFormatServiceImpl.class);
 	  
 	  response.setContentType("application/octet-stream");
 
 
     OutputStream out = response.getOutputStream();
     System.err.println("out = " + out);
     
     
 //    ErrorHandler runner = new MyErrorHandler();
     
 	SAXParserFactory factory = SAXParserFactory.newInstance();
 	factory.setNamespaceAware(true);
 	XmlParser f = null;
 	
 	String grad = request.getParameter("grad");
 	String summer = request.getParameter("summer");
 	Map myMap = new HashMap(request.getParameterMap());
 	
 	String standard = (grad.equalsIgnoreCase("Y")||summer.equalsIgnoreCase("Y"))?"N":"Y";
 	myMap.put("standard", standard);
 	
 	
	if (grad == null || grad.equalsIgnoreCase("N") ) {
 		myMap.put("gradReportClause", "");
 	}
 	System.err.println("Path = " + new File(".").getAbsolutePath());
 	try {
 		// Insert page variables into batch program
 		f = new XmlParser("NSLC.xml", factory, myMap);
 	}
 	catch (java.net.ConnectException e) {
 		e.printStackTrace();
 	}
 	catch (Exception p) {
 			p.printStackTrace();
 	}
 
 	XmlObject root = f.getRoot();
 	System.err.println("root = " + root);
 	
 	HashMap st = root.getSymbolTable();
 	System.err.println("st= " + st);
 	Select select = (Select) st.get("selectNSC");
 	System.err.println("select = " + select);
 	select.setOutputStream(out);
 	select.setLock(true);
 	
 	try {
 		if (f != null) {
 			f.execute();
 		}
 	} catch (Exception e) {
 		e.printStackTrace();
 	} 
 	
 
     out.flush();
 
   }
 }
