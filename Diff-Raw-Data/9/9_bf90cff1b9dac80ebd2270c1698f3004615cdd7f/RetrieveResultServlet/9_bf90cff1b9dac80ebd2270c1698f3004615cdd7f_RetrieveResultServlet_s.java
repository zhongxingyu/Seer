 package org.n52.wps.server;
 
 import gov.usgs.cida.gdp.wps.util.MIMEUtil;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.io.PrintWriter;
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import org.apache.commons.io.IOUtils;
 import org.n52.wps.server.database.DatabaseFactory;
 import org.n52.wps.server.database.IDatabase;
 
 public class RetrieveResultServlet extends HttpServlet {
 
 	private static final long serialVersionUID = -268198171054599696L;
 
 	public static String SERVLET_PATH = "RetrieveResultServlet";
 
     @Override
 	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
 
 		String id = request.getParameter("id");

 		if(id == null || id.length() == 0) {
 			errorResponse("id parameter missing", response);
 		} else {
             IDatabase db = DatabaseFactory.getDatabase();
             String mimeType = db.getMimeTypeForStoreResponse(id);
             InputStream is = db.lookupResponse(id);
             try {
                 if (mimeType == null || is == null) {
                     errorResponse("id parameter invalid", response);
                 } else {
                     String suffix = MIMEUtil.getSuffixFromMIMEType(mimeType);
                    if (!"xml".equals(suffix)) {
                         response.addHeader("Content-Disposition", "attachment; filename=\"wps-result." + suffix + "\"");
                     }
                     response.setContentType(mimeType);
                     OutputStream os = response.getOutputStream();
                     IOUtils.copy(is, os);
                     os.flush();
                 }
             } finally {
                 IOUtils.closeQuietly(is);
             }
         }
         response.flushBuffer();
 	}
 
     protected void errorResponse(String error, HttpServletResponse response) throws IOException {
 			response.setContentType("text/html");
 			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
 			PrintWriter writer = response.getWriter();
 			writer.write("<html><title>Error</title><body>" + error + "</body></html>");
             writer.flush();
     }
 }
