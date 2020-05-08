 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package it.holiday69.tinyhttp.task;
 
 import it.holiday69.tinyhttp.AbstractHttpCall;
 import it.holiday69.tinyhttp.utils.Base64;
 import it.holiday69.tinyhttp.utils.DebugHelper;
 import it.holiday69.tinyhttp.utils.IOHelper;
 import it.holiday69.tinyhttp.utils.RandomHelper;
 import it.holiday69.tinyhttp.utils.StringUtils;
 import it.holiday69.tinyhttp.vo.HttpMethod;
 import it.holiday69.tinyhttp.vo.HttpRequest;
 import it.holiday69.tinyhttp.vo.HttpParam;
 import it.holiday69.tinyhttp.vo.HttpResponse;
 import it.holiday69.tinyhttp.vo.ProxyObject;
 
 import java.io.DataOutputStream;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.net.FileNameMap;
 import java.net.HttpURLConnection;
 import java.net.InetSocketAddress;
 import java.net.MalformedURLException;
 import java.net.Proxy;
 import java.net.URL;
 import java.net.URLConnection;
 import java.net.URLEncoder;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 import java.util.concurrent.atomic.AtomicBoolean;
 
 /**
  *
  * @author fratuz610
  */
 public class HttpCall extends AbstractHttpCall {
     
     private final int DEFAULT_BUFFER_SIZE = 1024 * 4;
     private final String CR_LF = "\r\n";
     
     public HttpCall(HttpRequest httpReq) throws Exception {
       super(httpReq, null);
     }
     
     public HttpCall(HttpRequest httpReq, ProxyObject proxyObj) throws Exception {
       super(httpReq, proxyObj);
     }
     
     private final DebugHelper debug = new DebugHelper();
   
     @Override
     public Void call() throws Exception {
       
       // clears the log
       debug.clear();
       
       httpResponse = new HttpResponse();
       httpResponse.responseCode = 500;
       
       Thread timeoutThread = null;
     
       try {
 
         // creates a connection with the final URL
         URL url = new URL(getFinalURL(httpRequest));
         
         debug.log("Attempting a " + httpRequest.method + " call to " + url.toString());
         
         HttpURLConnection urlConn;
 
         if(_proxyObj == null) {
           urlConn = (HttpURLConnection) url.openConnection();
           debug.log("No proxy is used");
         } else {
           urlConn = (HttpURLConnection) url.openConnection(new Proxy(Proxy.Type.HTTP, new InetSocketAddress(_proxyObj.host, _proxyObj.port)));
           String encodedAuth = new String(Base64.encodeBytes((_proxyObj.username + ":" + _proxyObj.password).getBytes()));
           urlConn.setRequestProperty("Proxy-Authorization", "Basic " + encodedAuth);
           debug.log("Using proxy: " + _proxyObj.host + ":" + _proxyObj.port);
         }
 
         // setting GET/POST/PUT/DELETE method
         urlConn.setRequestMethod(httpRequest.method.toString());
 
         // Sets the connection timeout
         urlConn.setConnectTimeout(httpRequest.timeout);
         debug.log("Using timeout: " + httpRequest.timeout + " millisec");
 
         // sets the follow redirects policy
         urlConn.setInstanceFollowRedirects(httpRequest.followRedirects);
         debug.log("Using followRedirects: " + httpRequest.followRedirects);
 
         // no caching by default
         urlConn.setUseCaches (false);
 
         // setting custom request headers
         if(httpRequest.requestHeaderList != null && !httpRequest.requestHeaderList.isEmpty()) {
           for(HttpParam requestHeaderObj : httpRequest.requestHeaderList) {
             urlConn.setRequestProperty(requestHeaderObj.key, requestHeaderObj.value);
             debug.log("Sending request header: " + requestHeaderObj.key + " => " + requestHeaderObj.value);
           }
         }
 
         // starts the timeout thread
         AtomicBoolean interruptFlag = new AtomicBoolean(false);
         timeoutThread = new Thread(new HttpTimeoutTask(urlConn, httpRequest.timeout, interruptFlag));
         timeoutThread.start();
         
         debug.log("Timeout thread started");
         
         // for sure we always need to get some data back
         urlConn.setReadTimeout(httpRequest.timeout);
         urlConn.setDoInput(true);
         
         // we send the data out if necessary (upload and/or parameters)
         sendDataOut(urlConn, httpRequest);
         
         // we read the response code
         httpResponse.responseCode = urlConn.getResponseCode();
         debug.log("Retrieving response code: " + httpResponse.responseCode);
         
         // we read the response headers
         readResponseHeaders(urlConn, httpResponse);
         
         if(httpResponse.responseCode < 299) {
         
           // read the data into a file or into a variable
           readResponseBody(urlConn, httpResponse);
 
           // get the response code
           if(interruptFlag.get())
             httpResponse.responseCode = 408;
         }
         
         // checks if it's a redirect
         if(httpResponse.responseCode == 302 && httpRequest.followRedirects) {
           
           // retrieve the location response header
           String locationValue = null;
           for(HttpParam responseHeader : httpResponse.responseHeaderList) {
             System.out.println(responseHeader.key + " => " + responseHeader.value);
             if("location".equalsIgnoreCase(responseHeader.key))
               locationValue = responseHeader.value; 
           }
           
           if(locationValue != null) {
             HttpRequest redirectHttpReq = new HttpRequest(HttpMethod.GET, locationValue);
             HttpCall redirectHttpCall = new HttpCall(redirectHttpReq, _proxyObj);
             redirectHttpCall.call();
             httpResponse = redirectHttpCall.httpResponse;
           }
           
         }
         
       } catch (MalformedURLException e) {
         throw new Exception("MalformedURLException: " + e.getMessage());
       } catch (IOException e) {
         throw new Exception("IOException: " + e.getMessage());
       } catch (Throwable e) {
         throw new Exception("Exception: " + e.getMessage());
       } finally {
         
         // stops the timeout thread
         try { timeoutThread.interrupt(); } catch(Throwable th) { }
         
         // sets the call as completed
         callCompleted = true;
         
         return null;
       }
       
     }
   
     private String getFinalURL(HttpRequest httpRequest) throws Exception {
       
       String finalURL = httpRequest.url;
       
       if(httpRequest.method == HttpMethod.GET || httpRequest.method == HttpMethod.DELETE) {
 
         if(httpRequest.requestParamList != null && !httpRequest.requestParamList.isEmpty()) {
           // compose the URL for the get method
           List<String> getParamList = new LinkedList<String>();
           int cnt = 0;
           for(HttpParam getParam: httpRequest.requestParamList)
             getParamList.add(URLEncoder.encode(getParam.key, "UTF-8")+"="+URLEncoder.encode(getParam.value, "UTF-8"));
 
           finalURL += "?" + StringUtils.join("&", getParamList.toArray(new String[0]));
         }
       }
       
       return finalURL;
     }
     
     private void sendDataOut(HttpURLConnection urlConn, HttpRequest httpRequest) throws Exception {
       if(httpRequest.method == HttpMethod.POST || httpRequest.method == HttpMethod.PUT) {
         
         // we write the post data
         urlConn.setDoOutput(true);
         writePostData(urlConn, httpRequest);
       } else {
 
         // we send get data only
         urlConn.setDoOutput(false);
         debug.log("No output data to send");
       }
     }
     
     private void writePostData(HttpURLConnection urlConn, HttpRequest httpRequest) throws Exception {
       
       // we send the POST data here
       urlConn.setDoOutput(true);
 
       if(httpRequest.uploadFileList.isEmpty()) {
         // normal post call
         sendParameters(urlConn, httpRequest);
       } else {
         // file upload call
         uploadFiles(urlConn, httpRequest);
       }
       
     }
     
     private void sendParameters(HttpURLConnection urlConn, HttpRequest httpRequest) throws Exception {
       
       String postData = "";
 
       if(httpRequest.requestParamList != null && !httpRequest.requestParamList.isEmpty()) {
         // compose the URL for the get method
         List<String> getParamList = new LinkedList<String>();
         for(HttpParam getParam: httpRequest.requestParamList) {
           if(getParam.value.length() > 100)
             debug.log("Sending body parameter: " + getParam.key + " => " + getParam.value.substring(0, 100));
           else
             debug.log("Sending body parameter: " + getParam.key + " => " + getParam.value);
           getParamList.add(URLEncoder.encode(getParam.key, "UTF-8")+"="+URLEncoder.encode(getParam.value, "UTF-8"));
         }
 
         postData = StringUtils.join("&", getParamList.toArray(new String[0]));
       }
 
       // updates for monitoring
       bytesUploadTotal.set(postData.length());
 
       DataOutputStream output = null;
       try {
         // Send POST data
         output = new DataOutputStream ( urlConn.getOutputStream ());
         output.writeBytes (postData);
         output.flush ();
       
       } finally {
         if(output != null) output.close();
       }
       
       debug.log("Body parameters sent (if any)");
       
       // updates for monitoring
       bytesUploadCount.set(postData.length());
     }
     
     private void uploadFiles(HttpURLConnection urlConn, HttpRequest httpRequest) throws Exception {
             
       String randomDelimiter = new RandomHelper().getRandomDelimiter();
       
       urlConn.setRequestProperty("Content-Type", "multipart/form-data; boundary=---------------------------" + randomDelimiter); 
 
       List<HttpUploadObject> httpUploadList = new LinkedList<HttpUploadObject>();
       
       long aggregateContentLength = 0;
       
       for(File fileToUpload : httpRequest.uploadFileList) {
         
         HttpUploadObject obj = new HttpUploadObject();
         
         FileNameMap fileNameMap = URLConnection.getFileNameMap();
         String mimeType = fileNameMap.getContentTypeFor(fileToUpload.getAbsolutePath());
 
         if(mimeType == null) mimeType = "application/octet-stream";
 
         // creates the message header
         obj.headerMessage = ""; 
         obj.headerMessage += "-----------------------------" + randomDelimiter + CR_LF; 
         obj.headerMessage += "Content-Disposition: form-data; name=\"uploadedfile\"; filename=\""+fileToUpload.getName()+"\"" + CR_LF;
         obj.headerMessage += "Content-Type: " + mimeType + CR_LF; 
         obj.headerMessage += CR_LF; 
         
         // creates the footer header
         obj.footerMessage = CR_LF + "-----------------------------"+randomDelimiter+"--" + CR_LF;
         
         obj.fileToUpload = fileToUpload;
         
         aggregateContentLength += obj.headerMessage.length() + obj.footerMessage.length() + fileToUpload.length();
         
         httpUploadList.add(obj);
       }
       
       // might not need to specify the content-length when sending chunked data. 
       urlConn.setRequestProperty("Content-Length", "" + aggregateContentLength); 
       
       urlConn.setFixedLengthStreamingMode((int)aggregateContentLength);
       
       debug.log("Sending upload contentLength " + aggregateContentLength + " bytes");
       
       // updates for speed monitoring
       bytesUploadTotal.set(aggregateContentLength);
             
       OutputStream output = null;
       
       try {
       
         for(HttpUploadObject uploadObject : httpUploadList) {
           
           debug.log("Uploading file " + uploadObject.fileToUpload.getAbsolutePath());
           
           // writes it all out
           output = urlConn.getOutputStream(); 
 
           // let's write the header
           output.write(uploadObject.headerMessage.getBytes()); 
 
           bytesUploadCount.addAndGet(uploadObject.headerMessage.length());
 
           InputStream input = new FileInputStream(uploadObject.fileToUpload);
           
           new IOHelper().copy(input, output, bytesUploadCount, bytesUploadSpeed);
           
           // let's write the footer
           output.write(uploadObject.footerMessage.getBytes()); 
 
           // updates for monitoring
           bytesUploadCount.addAndGet(uploadObject.footerMessage.length());
 
           output.flush(); 
           
           debug.log(uploadObject.fileToUpload.getAbsolutePath() + " uploaded");
 
         }
       } finally {
         
         if(output != null) output.close();
       }
     }
     
     private static class HttpUploadObject {
       private String headerMessage;
       private String footerMessage;
       private File fileToUpload;
     }
     
     private void readResponseHeaders(HttpURLConnection urlConn, HttpResponse httpResponse) {
       
       Map<String, List<String>> headerMap = urlConn.getHeaderFields();
 
       if(headerMap.size() > 0) {
         for(String key : headerMap.keySet()) {
           HttpParam responseHeaderItem = new HttpParam();
           responseHeaderItem.key = key;
           responseHeaderItem.value = headerMap.get(key).get(0);
           httpResponse.responseHeaderList.add(responseHeaderItem);
 
           debug.log("Response header: " + responseHeaderItem.key + " => " + responseHeaderItem.value);
           
           if(CONTENT_TYPE_HEADER_KEY.equalsIgnoreCase(key)) 
             httpResponse.responseType = responseHeaderItem.value;
 
           if(CONTENT_LENGTH_HEADER_KEY.equalsIgnoreCase(key)) 
             httpResponse.responseLength = Long.parseLong(responseHeaderItem.value);
         }
       }
       
     }
     
     private void readResponseBody(HttpURLConnection urlConn, HttpResponse httpResponse) throws Exception {
       
       // sets for monitoring
       bytesDownloadTotal.set(httpResponse.responseLength);

       switch(responseType) {
         case AUTO:
           if(isTextMimeType(httpResponse.responseType))
             readResponseAsText(urlConn);
           else
             readResponseAsFile(urlConn);
           break;
         case FILE:
           readResponseAsFile(urlConn);
         case TEXT:
           readResponseAsText(urlConn);
       }
     }
     
     private void readResponseAsFile(HttpURLConnection urlConn) throws Exception {
       
       debug.log("Downloading file...");
 
       FileOutputStream out = null;
       
       try {
         // file response
         File retFile = File.createTempFile("ywc", "");
         out = new FileOutputStream(retFile);
 
         new IOHelper().copy(urlConn.getInputStream(), out, bytesDownloadCount, bytesDownloadSpeed);
 
         out.flush();
 
         httpResponse.fileResponse = retFile;
 
         debug.log("File downloaded to " + retFile.getAbsolutePath());
       } finally {
         if(out != null) out.close();
       }
     }
     
     private void readResponseAsText(HttpURLConnection urlConn) throws Exception {
       
       debug.log("Reading text response...");
 
       // text response
       httpResponse.textResponse = new IOHelper().readAsString( urlConn.getInputStream(), bytesDownloadCount, bytesDownloadSpeed);
 
       debug.log("Text response read");
     }
     
     public String getCallLog() {
       return StringUtils.join("\n", debug.getLogList().toArray(new String[0]));
     }
     
     private boolean isTextMimeType(String mimeType) {
       return (mimeType.toLowerCase().indexOf("text") != -1 || 
               mimeType.toLowerCase().indexOf("xml") != -1 || 
               mimeType.toLowerCase().indexOf("json") != -1);
     }
   }
