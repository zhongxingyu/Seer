 package gov.usgs.cida.communication;
 
 import java.io.BufferedOutputStream;
 import java.io.ByteArrayInputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.net.URLDecoder;
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import org.apache.commons.lang.StringUtils;
 import sun.misc.BASE64Decoder;
 
 /**
  *
  * @author isuftin
  */
 public class Export extends HttpServlet {
 
     protected void processRequest(HttpServletRequest request, HttpServletResponse response)
             throws ServletException, IOException {
 
         String filename = request.getParameter("filename");
        String data =  URLDecoder.decode(request.getParameter("data"), "UTF-8");
         String type = request.getParameter("type");
         
         if (StringUtils.isBlank(filename) || StringUtils.isBlank(data)) {
             response.sendError(500, "Either 'filename' or 'data' elements were empty.");
             return;
         }
         
        BufferedOutputStream out = null;
        
         StringBuilder sb = new StringBuilder(data);
        byte[] csvData;
         if ("image/png;base64".equalsIgnoreCase(type)) {
            csvData = new BASE64Decoder().decodeBuffer(sb.toString());
         } else {
            csvData = sb.toString().getBytes("UTF-8");
         }
        int length = csvData.length;
         
         InputStream in = null;
         try {
             response.setContentType("application/octet-stream");
             response.setContentLength(length);
             response.setHeader("Content-Disposition", "attachment;filename=" + filename);
             response.setHeader("Pragma", "no-cache");
             response.setHeader("Expires", "0");
             
            in = new ByteArrayInputStream(csvData);
             out = new BufferedOutputStream(response.getOutputStream());
             
             byte[] outputByte = new byte[4096];
             while (in.read(outputByte, 0, 4096) != -1) {
                 out.write(outputByte, 0, 4096);
             }
             out.flush();
             out.close();
         } finally {
             if (out != null) out.close();
             if (in != null) in.close();
         }
     }
 
     // <editor-fold defaultstate="collapsed" desc="HttpServlet methods. Click on the + sign on the left to edit the code.">
     @Override
     protected void doGet(HttpServletRequest request, HttpServletResponse response)
             throws ServletException, IOException {
         processRequest(request, response);
     }
 
     @Override
     protected void doPost(HttpServletRequest request, HttpServletResponse response)
             throws ServletException, IOException {
         processRequest(request, response);
     }
 
     /** 
      * Returns a short description of the servlet.
      * @return a String containing servlet description
      */
     @Override
     public String getServletInfo() {
         return "Exports posted data via a file back to the client";
     }// </editor-fold>
 }
