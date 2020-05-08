 package de.fiz.sak.abschlussprojekt.servlet;
 
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.io.PrintWriter;
 import java.net.HttpURLConnection;
 import java.net.URL;
 import java.nio.charset.Charset;
 import java.util.zip.Adler32;
 import java.util.zip.Checksum;
 
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import de.fiz.sak.abschlussprojekt.sessions.FitsInterface;
 import de.fiz.sak.abschlussprojekt.sessions.impl.FitsInterfaceImpl;
 import edu.harvard.hul.ois.fits.exceptions.FitsException;
 
 public class FitsServlet extends HttpServlet {
 
     private static final long serialVersionUID = -2124267350610278867L;
 
     private FitsInterface fi = new FitsInterfaceImpl();
 
     private PrintWriter w;
 
     private File f;
 
     /**
      * Implements the HTTP get-request
      * 
      * @param req
      *            HttpServletRequest
      * @param resp
      *            HttpServletResponse
      */
     protected void doGet(HttpServletRequest req, HttpServletResponse resp)
         throws ServletException, IOException {
 
         String path = req.getParameter("path");
 
         if (path == null) {
             throw new ServletException("Parameter 'path' not set. ");
         }
 
         if (path.startsWith("http")) {
             URL url = new URL(path);
             HttpURLConnection urlConn =
                 (HttpURLConnection) url.openConnection();
             urlConn.setRequestMethod("GET");
             String fileName = url.getFile();
             int slashIndex = fileName.lastIndexOf('/');
             fileName = fileName.substring(slashIndex + 1);
 
             String dirPath = path.substring(0, path.indexOf(fileName));
             byte[] dirPathBytes = dirPath.getBytes(Charset.forName("UTF-8"));
             Checksum cs = new Adler32();
             cs.update(dirPathBytes, 0, dirPathBytes.length);
 
             InputStream is = url.openStream();
 
            // String tmpFileName = System.getenv("TEMP") +
            // System.getProperty("file.separator") + cs.getValue() + fileName;
             // File tmpFile = new File(tmpFileName);
             File tmpFile =
                 File.createTempFile(String.valueOf(cs.getValue()), fileName);
             // TODO check if delete on exit of VM is sufficient; may be better
             // to delete on servlet destruction
             tmpFile.deleteOnExit();
             OutputStream out = new FileOutputStream(tmpFile);
 
             // download
             byte[] buffer = new byte[2048];
             int bytesRead = is.read(buffer);
             while (bytesRead >= 0) {
                 out.write(buffer, 0, bytesRead);
                 bytesRead = is.read(buffer);
             }
             out.close();
             is.close();
 
            path = tmpFile.getAbsolutePath();// Name;
         }
         else if (path.startsWith("file:///")) {
             path = path.replaceFirst("file:///", "");
         }
 
         this.testParameter(path);
 
         String xml = null;
 
         try {
             xml = fi.extractMetadata(f);
         }
         catch (FitsException e) {
             throw new ServletException(e);
         }
 
         if (xml != null) {
             w = resp.getWriter();
             w.println(xml);
         }
     }
 
     /**
      * Tests the path of the file about the rightness.
      * 
      * @param Pfad
      *            The path of the file.
      * @throws ServletException
      * @throws FileNotFoundException
      */
     private void testParameter(String Pfad) throws ServletException,
         FileNotFoundException {
         f = new File(Pfad);
         if (f == null) {
             throw new FileNotFoundException("Cannot open file: " + Pfad);
         }
         if (f.canRead() != true) {
            throw new FileNotFoundException("Cannot read file: " + Pfad);
         }
     }
 
 }
