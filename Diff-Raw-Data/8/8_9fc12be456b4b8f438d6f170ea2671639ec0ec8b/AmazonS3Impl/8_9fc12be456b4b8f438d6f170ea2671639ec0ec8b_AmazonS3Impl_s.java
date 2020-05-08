 package org.lastbamboo.common.amazon.s3;
 
 import java.io.File;
 import java.io.FileFilter;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.io.InputStream;
 import java.net.UnknownHostException;
 import java.security.Security;
 import java.text.DecimalFormat;
 import java.util.Collection;
 import java.util.LinkedList;
 
 import javax.xml.xpath.XPathExpressionException;
 
 import org.apache.commons.httpclient.Header;
 import org.apache.commons.httpclient.HttpMethod;
 import org.apache.commons.httpclient.HttpMethodRetryHandler;
 import org.apache.commons.httpclient.StatusLine;
 import org.apache.commons.httpclient.methods.DeleteMethod;
 import org.apache.commons.httpclient.methods.FileRequestEntity;
 import org.apache.commons.httpclient.methods.GetMethod;
 import org.apache.commons.httpclient.methods.PutMethod;
 import org.apache.commons.httpclient.methods.RequestEntity;
 import org.apache.commons.httpclient.params.HttpMethodParams;
 import org.apache.commons.io.IOUtils;
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.lastbamboo.common.amazon.stack.AwsUtils;
import org.lastbamboo.common.http.client.HttpClientManager;
import org.lastbamboo.common.http.client.HttpClientManagerImpl;
 import org.lastbamboo.common.util.DateUtils;
 import org.lastbamboo.common.util.FileInputStreamHandler;
 import org.lastbamboo.common.util.InputStreamHandler;
 import org.lastbamboo.common.util.NoOpInputStreamHandler;
 import org.lastbamboo.common.util.SecurityUtils;
 import org.lastbamboo.common.util.xml.XPathUtils;
 import org.w3c.dom.Node;
 import org.w3c.dom.NodeList;
 import org.xml.sax.SAXException;
 
 
 /**
  * Class implementing REST calls to Amazon's S3 service.
  */
 public class AmazonS3Impl implements AmazonS3
     {
 
     private static final Log LOG = LogFactory.getLog(AmazonS3Impl.class);
     
     private String m_accessKeyId;
     private String m_secretAccessKey;
 
     /**
      * Creates a new S3 instance.
      * 
      * @throws IOException If the props file can't be found or keys can't be read. 
      */
     public AmazonS3Impl() throws IOException
         {
         if (!AwsUtils.hasPropsFile())
             {
             System.out.println("No properties file found");
             throw new IOException("No props file");
             }
         try
             {
             this.m_accessKeyId = AwsUtils.getAccessKeyId();
             }
         catch (final IOException e)
             {
             System.out.println("Found the properties file, but there's no " +
                 "access key ID in the form: accessKeyId=");
             throw e;
             }
         try
             {
             this.m_secretAccessKey = AwsUtils.getAccessKey();
             }
         catch (final IOException e)
             {
             System.out.println("Found the properties file, but there's no " +
                 "secret access key in the form: accessKey=");
             throw e;
             }
         configureDns();
         }
     
     private static void configureDns()
         {
         // We modify the permanent DNS caching for successful lookups because
         // Amazon periodically changes the machines buckets point to.  See:
         // http://java.sun.com/j2se/1.5.0/docs/api/java/net/InetAddress.html
         // and "DNS Considerations" under "Using Amazon S3" at:
         // http://docs.amazonwebservices.com/AmazonS3/2006-03-01/
         final int cacheSeconds = 1000 * 60 * 60;
         Security.setProperty("networkaddress.cache.ttl", 
             Integer.toString(cacheSeconds));
         }
     
     public void getPrivateFile(final String bucketName, final String fileName, 
         final File target) throws IOException
         {
         //final String fullPath = 
             //this.m_accessKeyId + "-" + bucketName + "/" + fileName;
         final String fullPath = bucketName + "/" + fileName;
         final String url = "https://s3.amazonaws.com:443/" + fullPath;
         LOG.debug("Getting file from URL: "+url);
         final GetMethod method = new GetMethod(url);
         normalizeRequest(method, "GET", fullPath, false, true);
         
         final InputStreamHandler handler = 
             new FileInputStreamHandler(target);
         sendRequest(method, handler);
         }
     
     public void getPublicFile(final String bucketName, final String fileName, 
         final File target) throws IOException
         {
         //final String fullPath = 
             //this.m_accessKeyId + "-" + bucketName + "/" + fileName;
         final String fullPath = bucketName + "/" + fileName;
         final String url = "http://s3.amazonaws.com/" + fullPath;
         LOG.debug("Getting file from URL: "+url);
         final GetMethod method = new GetMethod(url);
         
         // This is a public file, so we don't send the authentication token.
         normalizeRequest(method, "GET", fullPath, false, false);
         final InputStreamHandler handler = 
             new FileInputStreamHandler(target);
         sendRequest(method, handler);
         }
     
     public void putPrivateFile(final String bucketName, final File file) 
         throws IOException
         {
         putFile(bucketName, file, false);
         }
     
     public void putPrivateDir(final String bucketName, final File dir) throws IOException
         {
         
         final FileFilter filter = new FileFilter()
             {
             public boolean accept(final File file)
                 {
                 return file.isFile();
                 }
             };
         final File[] files = dir.listFiles(filter);
         for (final File file : files)
             {
             putPrivateFile(bucketName, file);
             }
         }
     
     public void putPublicFile(final String bucketName, final File file) 
         throws IOException
         {
         putFile(bucketName, file, true);
         }
 
     public void putPublicDir(final String bucketName, final File dir) throws IOException
         {
         final FileFilter filter = new FileFilter()
             {
             public boolean accept(final File file)
                 {
                 return file.isFile();
                 }
             };
         final File[] files = dir.listFiles(filter);
         for (final File file : files)
             {
             putPublicFile(bucketName, file);
             }
         }
         
     private void putFile(final String bucketName, final File file, final boolean makePublic) 
         throws IOException
         {
         try
             {
             final RequestEntity re = new FileRequestEntity(file, "");
             put(bucketName+"/"+file.getName(), re, makePublic);
             }
         catch (final FileNotFoundException e)
             {
             LOG.error("File Not Found: "+file, e);
             }
         }
         
     public void createBucket(final String bucketName) throws IOException
         {
         put(bucketName, null, false);
         }
    
     public void listBuckets() throws IOException
         {
         final String url = "https://s3.amazonaws.com:443";
         LOG.debug("Sending to URL: "+url);
         final GetMethod method = new GetMethod(url);
         final InputStreamHandler handler = new InputStreamHandler()
             {
             public void handleInputStream(final InputStream is) throws IOException
                 {
                 // Just convert it to a string for easier debugging.
                 final String xmlBody = IOUtils.toString(is);
                 try
                     {
                     final XPathUtils xPath = XPathUtils.newXPath(xmlBody);
                     final String namePath = "/ListAllMyBucketsResult/Buckets/Bucket/Name"; 
                     final String creationDatePath = 
                         "/ListAllMyBucketsResult/Buckets/Bucket/CreationDate"; 
                     final NodeList nameNodes = xPath.getNodes(namePath);
                     final NodeList lmNodes = xPath.getNodes(creationDatePath);
                     final int charsSep = 46;
                     System.out.println("------------------------------------------------------------------------------");
                     final StringBuilder desc = new StringBuilder();
                     desc.append("Name");
                     appendSpace(desc, charsSep-4);
                     desc.append("Creation Date");
                     System.out.println(desc.toString());
                     System.out.println("------------------------------------------------------------------------------");
                     System.out.println();
                     for (int i = 0; i < nameNodes.getLength(); i++)
                         {
                         final Node nameNode = nameNodes.item(i);
                         final String name = nameNode.getTextContent();
                         final Node lmNode = lmNodes.item(i);
                         final String lm = lmNode.getTextContent();
                         
                         final String dateString = DateUtils.prettyS3Date(lm);
                         final StringBuilder sb = new StringBuilder();
                         sb.append(name);
                         
                         appendSpace (sb, charsSep - name.length());
                         sb.append(dateString);
                         System.out.println(sb.toString());
                         }
                     }
                 catch (final SAXException e)
                     {
                     LOG.warn("Exception with XML" ,e );
                     }
                 catch (final XPathExpressionException e)
                     {
                     LOG.warn("Exception with XPath" ,e );
                     }
                 }
 
             private void appendSpace(final StringBuilder sb, final int extraSpace)
                 {
                 for (int j = 0; j < extraSpace; j++)
                     {
                     sb.append(" ");
                     }
                 }
             };
         normalizeRequest(method, "GET", "", false, true);
         sendRequest(method, handler);
         }
     
     public void listBucket(final String bucketName) throws IOException
         {
         final String fullPath = bucketName;
         final String url = "https://s3.amazonaws.com:443/" + fullPath;
         LOG.debug("Sending to URL: "+url);
         final GetMethod method = new GetMethod(url);
         final InputStreamHandler handler = new InputStreamHandler()
             {
             public void handleInputStream(final InputStream is) throws IOException
                 {
                 // Just convert it to a string for easier debugging.
                 final String xmlBody = IOUtils.toString(is);
                 //XmlUtils.printDoc(xmlBody);
                 try
                     {
                     final XPathUtils xPath = XPathUtils.newXPath(xmlBody);
                     final String namePath = "/ListBucketResult/Contents/Key"; 
                     final String lastModifiedPath = "/ListBucketResult/Contents/LastModified";
                     final String sizePath = "/ListBucketResult/Contents/Size";
                     final NodeList nameNodes = xPath.getNodes(namePath);
                     final NodeList lmNodes = xPath.getNodes(lastModifiedPath);
                     final NodeList sizeNodes = xPath.getNodes(sizePath);
                     
                     System.out.println("------------------------------------------------------------------------------");
                     final int sep1 = 30;
                     final int sep2 = 26;
                     final StringBuilder desc = new StringBuilder();
                     final String desc1 = "Name";
                     final String desc2 = "Last Modified";
                     final String desc3 = "Size";
                     desc.append(desc1);
                     appendSpace(desc, sep1-desc1.length());
                     desc.append(desc2);
                     appendSpace(desc, sep2-desc2.length());
                     desc.append(desc3);
                     System.out.println(desc.toString());
                     System.out.println("------------------------------------------------------------------------------");
                     System.out.println();
                     for (int i = 0; i < nameNodes.getLength(); i++)
                         {
                         final Node nameNode = nameNodes.item(i);
                         final String name = nameNode.getTextContent();
                         final Node lmNode = lmNodes.item(i);
                         final String lm = lmNode.getTextContent();
                         final Node sizeNode = sizeNodes.item(i);
                         final String sizeString = sizeNode.getTextContent();
                         
                         final int sizeInt = Integer.parseInt(sizeString);
                         final double sizeK = sizeInt/1024;
                         final double sizeMb = sizeK/1024;
                         
                         
                         final String dateString = DateUtils.prettyS3Date(lm);
                         final StringBuilder sb = new StringBuilder();
                         sb.append(name);
                         
                         appendSpace (sb, sep1 - name.length());
                         sb.append(dateString);
                         
                         final DecimalFormat df = new DecimalFormat("###0.##");
                         final String formattedSize = df.format(sizeMb);
                         appendSpace (sb, sep2 - dateString.length());
 
                         sb.append(formattedSize);
                         sb.append(" MB");
                         System.out.println(sb.toString());
                         }
                     }
                 catch (final SAXException e)
                     {
                     LOG.warn("Exception with XML" ,e );
                     }
                 catch (final XPathExpressionException e)
                     {
                     LOG.warn("Exception with XPath" ,e );
                     }
                 }
 
             private void appendSpace(final StringBuilder sb, final int extraSpace)
                 {
                 for (int j = 0; j < extraSpace; j++)
                     {
                     sb.append(" ");
                     }
                 }
             };
         normalizeRequest(method, "GET", fullPath, false, true);
         sendRequest(method, handler);
         }
         
     public void deleteBucket(final String bucketName) throws IOException
         {
         delete(bucketName);
         }
     
     public void delete(final String bucketName, final String fileName) throws IOException
         {
         System.out.println("Deleting "+fileName);
         delete(bucketName+"/"+fileName);
         }
     
     public void deleteStar(final String bucketName, final String fileName) 
         throws IOException
         {
         final String fullPath = bucketName;
         final String url = "https://s3.amazonaws.com:443/" + fullPath;
         LOG.debug("Sending to URL: "+url);
         
         final Collection<String> filesToDelete = new LinkedList<String> ();
         final GetMethod method = new GetMethod(url);
         boolean matchStartTemp = false;
         String toMatchTemp = fileName;
         boolean matchEndTemp = false;
         
         if (!fileName.endsWith("*") && !fileName.startsWith("*"))
             {
             delete(bucketName, fileName);
             return;
             }
         if (fileName.endsWith("*"))
             {
             matchStartTemp = true;
             toMatchTemp = toMatchTemp.substring(0, fileName.length()-1);
             LOG.debug("Checking for files that start with: "+toMatchTemp);
             }
         if (fileName.startsWith("*"))
             {
             matchEndTemp = true;
             toMatchTemp = toMatchTemp.substring(1);
             LOG.debug("Checking for files that end with: "+toMatchTemp);
             }
         LOG.debug("Checking for files matching: "+toMatchTemp);
         final boolean matchStart = matchStartTemp;
         final boolean matchEnd = matchEndTemp;
         final String toMatch = toMatchTemp;
         final InputStreamHandler handler = new InputStreamHandler()
             {
             public void handleInputStream(final InputStream is) throws IOException
                 {
                 // Just convert it to a string for easier debugging.
                 final String xmlBody = IOUtils.toString(is);
                 try
                     {
                     final XPathUtils xPath = XPathUtils.newXPath(xmlBody);
                     final String namePath = "/ListBucketResult/Contents/Key"; 
                     final NodeList nameNodes = xPath.getNodes(namePath);
                     
                     for (int i = 0; i < nameNodes.getLength(); i++)
                         {
                         final Node nameNode = nameNodes.item(i);
                         final String name = nameNode.getTextContent();
                         if (matchStart && name.startsWith(toMatch))
                             {
                             LOG.debug("Matched: "+name);
                             filesToDelete.add(name);
                             }
                         else if (matchEnd && name.endsWith(toMatch))
                             {
                             LOG.debug("Matched: "+name);
                             filesToDelete.add(name);
                             }
                         }
                     }
                 catch (final SAXException e)
                     {
                     LOG.warn("Exception with XML" ,e );
                     }
                 catch (final XPathExpressionException e)
                     {
                     LOG.warn("Exception with XPath" ,e );
                     }
                 }
             };
         normalizeRequest(method, "GET", fullPath, false, true);
         sendRequest(method, handler);
         
         for (final String fileKey : filesToDelete)
             {
             delete(bucketName, fileKey);
             }
         }
 
     private void delete(final String relativePath) throws IOException
         {
         final String fullPath = relativePath;
         final String url = "https://s3.amazonaws.com:443/" + fullPath;
         LOG.debug("Sending to URL: "+url);
         final DeleteMethod method = new DeleteMethod(url);
         
         final InputStreamHandler handler = new NoOpInputStreamHandler();
         normalizeRequest(method, "DELETE", fullPath, false, true);
         sendRequest(method, handler);
         }
     
     private void put(final String relativePath, final RequestEntity re, 
         final boolean isPublic) throws IOException
         {
         //final String fullPath = this.m_accessKeyId + "-"+relativePath;
         final String fullPath = relativePath;
         final String url = "https://s3.amazonaws.com:443/" + fullPath;
         LOG.debug("Sending to URL: "+url);
         final PutMethod method = new PutMethod(url);
 
         if (re != null)
             {
             method.setRequestEntity(re);
             }
         else
             {
             method.setRequestHeader("Content-Type", "");
             }
         
         final InputStreamHandler handler = new NoOpInputStreamHandler();
         normalizeRequest(method, "PUT", fullPath, isPublic, true);
         sendRequest(method, handler);
         }
 
     public void normalizeRequest(final HttpMethod method, 
         final String methodString, final String fullPath, 
         final boolean addPublicHeader, final boolean useAuth)
         {
         final Header dateHeader = new Header("Date", DateUtils.createHttpDate());
         method.setRequestHeader(dateHeader);
         if (addPublicHeader)
             {
             final Header publicHeader = new Header("x-amz-acl", "public-read");
             method.setRequestHeader(publicHeader);
             }
         
         if (useAuth)
             {
             final Header auth = createAuthHeader(method, methodString, fullPath);
             method.setRequestHeader(auth);
             }
         }
 
     private void sendRequest(final HttpMethod method, 
         final InputStreamHandler handler) throws IOException
         {
        final HttpClientManager client = new HttpClientManagerImpl();
         
         // We customize the retry handler because AWS apparently disconnects a lot.
         final HttpMethodRetryHandler retryHandler = 
             new HttpMethodRetryHandler()
             {
 
             public boolean retryMethod(final HttpMethod method, 
                 final IOException ioe, final int retries)
                 {
                 if (ioe instanceof UnknownHostException)
                     {
                     return false;
                     }
                 if (retries < 40)
                     {
                     System.out.println("Did not connect.  Received: ");
                     ioe.printStackTrace();
                     try
                         {
                         Thread.sleep(retries * 200);
                         }
                     catch (InterruptedException e)
                         {
                         }
                     return true;
                     }
                 return false;
                 }
             
             };
             
         method.getParams().setParameter(
             HttpMethodParams.RETRY_HANDLER, retryHandler);
         if (LOG.isDebugEnabled())
             {
             printHeaders(method.getRequestHeaders());
             }
         try
             {
             client.executeMethod(method);
             
             final StatusLine statusLine = method.getStatusLine();
             
             final int code = statusLine.getStatusCode();
             if (code < 200 || code > 299)
                 {
                 LOG.debug("Did not receive 200 level response: "+statusLine);
                 final Header[] responseHeaders = method.getResponseHeaders();
                 printHeaders(responseHeaders);
                 
                 // S3 likely returned some sort of error in the response
                 // body, so print it out.
                 final InputStream is = method.getResponseBodyAsStream();
                 final String response = IOUtils.toString(is);
                 final String msg = statusLine + "\nwith body:\n" + response;
                 
                 LOG.debug("Got response: "+msg);
                 
                 IOUtils.closeQuietly(is);
                 // TODO: Send this to the stream handler.
                 throw new IOException("Error accessing S3: "+msg);
                 }
             else
                 {
                 final InputStream body = method.getResponseBodyAsStream();
                 handler.handleInputStream(body);
                 }
             }
         finally
             {
             // Don't forget to release it!
             method.releaseConnection();
             }
         }
 
     /**
      * Create the S3 authorization header.
      * 
      * @param method The HttpClient {@link HttpMethod}.
      * @param methodString The method identifier string, such as "GET" or "PUT.
      * Amazon's auth token algorithm uses this string.
      * @param fullPath The full path to the resource, such as the bucket or the
      * file.  For example:<p>
      * https://s3.amazonaws.com:443/YOUR_BUCKET_ID-YOUR_BUCKET_NAME/yourFile.txt
      * @return The HTTP header.
      */
     private Header createAuthHeader(final HttpMethod method, 
         final String methodString, final String fullPath)
         {
         final String canonicalString =
             AmazonS3Utils.makeCanonicalString(methodString, fullPath, 
                 method.getRequestHeaders());
         final String encodedCanonical = 
             SecurityUtils.signAndEncode(this.m_secretAccessKey, canonicalString);
         
         final String authValue = 
             "AWS " + this.m_accessKeyId + ":" + encodedCanonical;
         final Header auth = new Header("Authorization", authValue);
         return auth;
         }
 
     private void printHeaders(final Header[] headers)
         {
         for (int i = 0; i < headers.length; i++)
             {
             final Header rh = headers[i];
             LOG.debug("Using header: "+rh);
             }
         }
     }
