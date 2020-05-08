 /*
  *
  */
 
 package simpleWebServer;
  
 import java.io.*;
 import java.net.*;
 import java.util.*;
 
 import static simpleWebServer.HttpConstants.*;
 import simpleWebServer.FileExtensionToContentTypeMapper;
 import simpleWebServer.WorkerPool;
 
 
 class ReverseProxyServer {
 
     static final int BUF_SIZE = 2048;
     static final byte[] EOL = {(byte)'\r', (byte)'\n' };
 
     Logger logger = null;
     Socket client = null;
     FileExtensionToContentTypeMapper mapper = new FileExtensionToContentTypeMapper();
 
     public ReverseProxyServer(Logger logger) {
         this.logger = logger;
     }
 
     public void deliverContent(Socket client, int httpMethod, File targ) {
         String hostAddress = client.getInetAddress().getHostAddress();
        int httpCode = HTTP_OK;
 
         try {
         PrintStream ps = new PrintStream(client.getOutputStream());
 
         if (targ == null) {
             httpMethod = HTTP_HEAD;
             httpCode = HTTP_OK;
         }
 
         if (httpMethod == HTTP_HEAD) {
             printHeader(httpCode, ps);
         } else if (httpMethod == HTTP_GET) {
             if (!targ.exists()) {
                 httpCode = HTTP_NOT_FOUND;
                 printHeader(httpCode, ps);
                 send404(ps);
             }  else {
                 httpCode = HTTP_OK;
                 printHeader(httpCode, ps);
                 printContentType(targ, ps);
                 sendFile(targ, ps);
             }
             logger.log("From " + hostAddress + ": GET " +
                        targ.getAbsolutePath() + "-->" + httpCode);
         } else if (httpMethod == HTTP_BAD_METHOD) {
             send405(ps);
         }
         } catch (IOException e) {}
     }
 
     protected void printHeader(int httpCode, PrintStream ps)
         throws IOException
     {
         if (httpCode == HTTP_NOT_FOUND) {
             ps.print("HTTP/1.0 " + HTTP_NOT_FOUND + " not found");
             ps.write(EOL);
         } else if (httpCode == HTTP_OK) {
             ps.print("HTTP/1.0 " + HTTP_OK+" OK");
             ps.write(EOL);
         }
 
         ps.print("Server: Simple java");
         ps.write(EOL);
         ps.print("Date: " + (new Date()));
         ps.write(EOL);
     }
 
    protected void printContentType(File targ, PrintStream ps)
        throws IOException
    {
         if (!targ.isDirectory()) {
             ps.print("Content-length: "+targ.length());
             ps.write(EOL);
             ps.print("Last Modified: " + (new
                           Date(targ.lastModified())));
             ps.write(EOL);
             String name = targ.getName();
             int ind = name.lastIndexOf('.');
             String ct = null;
             if (ind > 0) {
                 ct = mapper.extensionsToContent.get(name.substring(ind));
             }
             if (ct == null) {
                 ct = "unknown/unknown";
             }
             ps.print("Content-type: " + ct);
             ps.write(EOL);
         } else {
             ps.print("Content-type: text/html");
             ps.write(EOL);
         }
     }
 
     protected void send404(PrintStream ps) throws IOException {
         ps.write(EOL);
         ps.println("Not Found\n\n" +
                    "The requested resource was not found.\n");
         ps.flush();
         client.close();
     }
 
     protected void send405(PrintStream ps) throws IOException {
         ps.write(EOL);
         ps.println("HTTP/1.0 " + HTTP_BAD_METHOD +
                    " unsupported method type: ");
         ps.flush();
         client.close();
     }
 
     protected void sendFile(File targ, PrintStream ps) throws IOException {
         InputStream is = null;
         ps.write(EOL);
         if (targ.isDirectory()) {
             listDirectory(targ, ps);
             return;
         } else {
             is = new FileInputStream(targ.getAbsolutePath());
         }
 
         byte[] buf = new byte[BUF_SIZE];
         try {
             int n;
             while ((n = is.read(buf)) > 0) {
                 ps.write(buf, 0, n);
             }
         } finally {
             is.close();
         }
     }
 
     protected void listDirectory(File dir, PrintStream ps) throws IOException {
         ps.println("<TITLE>Directory listing</TITLE><P>\n");
         ps.println("<A HREF=\"..\">Parent Directory</A><BR>\n");
         String[] list = dir.list();
         for (int i = 0; list != null && i < list.length; i++) {
             File f = new File(dir, list[i]);
             if (f.isDirectory()) {
                 ps.println("<A HREF=\""+list[i]+"/\">"+list[i]+"/</A><BR>");
             } else {
                 ps.println("<A HREF=\""+list[i]+"\">"+list[i]+"</A><BR");
             }
         }
         ps.println("<P><HR><BR><I>" + (new Date()) + "</I>");
     }
 }
 
