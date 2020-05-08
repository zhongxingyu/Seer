 package fedora.server.storage;
 
 import java.io.ByteArrayOutputStream;
 import java.io.InputStream;
 import java.net.HttpURLConnection;
 import java.net.URL;
 
 import fedora.server.storage.types.MIMETypedStream;
 import fedora.server.errors.HttpServiceNotFoundException;
 
 /**
  * <p>Title: DefaultExternalContentManager.java</p>
  * <p>Description: Provides a service that obtains HTTP-accessible content.</p>
  *
  * <p>Copyright: Copyright (c) 2002</p>
  * <p>Company: </p>
  * @author Ross Wayland
  * @version 1.0
  */
 public class DefaultExternalContentManager implements ExternalContentManager
 {
   /**
    * A method that reads the contents of the specified URL and returns the
    * result as a MIMETypedStream
    *
    * @param urlString The URL of the content.
    * @return A MIME-typed stream.
    * @throws HttpServiceNotFoundException If the URL connection could not
    *         be established.
    */
   public MIMETypedStream getExternalContent(String urlString)
       throws HttpServiceNotFoundException
   {
     try
     {
       MIMETypedStream httpContent = null;
       ByteArrayOutputStream baos = new ByteArrayOutputStream();
       URL url = new URL(urlString);
       HttpURLConnection connection = (HttpURLConnection)url.openConnection();
       String contentType = connection.getContentType();
       InputStream is = connection.getInputStream();
       int byteStream = 0;
       while((byteStream = is.read()) >=0 )
       {
         baos.write(byteStream);
       }
       httpContent = new MIMETypedStream(contentType, baos.toByteArray());
       return(httpContent);
 
     } catch (Throwable th)
     {
       throw new HttpServiceNotFoundException("HTTPService ERROR: "
           + th.getClass().getName() + th.getMessage());
     }
   }
 
   public static void main(String[] args)
   {
    HttpService hs = new HttpService();
     String url = "http://icarus.lib.virginia.edu/test/dummy.html";
     try
     {
      MIMETypedStream content = hs.getHttpContent(url);
       System.out.println("MIME: "+content.MIMEType);
       System.out.write(content.stream);
     } catch (Exception e)
     {
       System.err.println(e.getMessage());
     }
   }
 }
