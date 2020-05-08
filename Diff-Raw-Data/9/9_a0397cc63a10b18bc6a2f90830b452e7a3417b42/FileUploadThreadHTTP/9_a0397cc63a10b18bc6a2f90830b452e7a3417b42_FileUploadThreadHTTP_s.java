 //
 // $Id$
 // 
 // jupload - A file upload applet.
 // Copyright 2007 The JUpload Team
 // 
 // Created: 2007-03-07
 // Creator: Etienne Gauthier
 // Last modified: $Date$
 //
 // This program is free software; you can redistribute it and/or modify it under
 // the terms of the GNU General Public License as published by the Free Software
 // Foundation; either version 2 of the License, or (at your option) any later
 // version. This program is distributed in the hope that it will be useful, but
 // WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 // FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 // details. You should have received a copy of the GNU General Public License
 // along with this program; if not, write to the Free Software Foundation, Inc.,
 // 675 Mass Ave, Cambridge, MA 02139, USA.
 
 package wjhk.jupload2.upload;
 
 import java.io.BufferedOutputStream;
 import java.io.DataOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.io.UnsupportedEncodingException;
 import java.net.Proxy;
 import java.net.ProxySelector;
 import java.net.Socket;
 import java.net.URISyntaxException;
 import java.net.URL;
 import java.net.URLEncoder;
 import java.security.KeyManagementException;
 import java.security.KeyStoreException;
 import java.security.NoSuchAlgorithmException;
 import java.security.UnrecoverableKeyException;
 import java.security.cert.CertificateException;
 import java.text.SimpleDateFormat;
 import java.util.HashMap;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import javax.net.ssl.SSLSocket;
 import javax.swing.JProgressBar;
 
 import netscape.javascript.JSException;
 import netscape.javascript.JSObject;
 import wjhk.jupload2.exception.JUploadException;
 import wjhk.jupload2.exception.JUploadIOException;
 import wjhk.jupload2.filedata.FileData;
 import wjhk.jupload2.policies.UploadPolicy;
 
 /**
  * This class implements the file upload via HTTP POST request.
  * 
  * @author Etienne Gauthier
  * @version $Revision$
  */
 public class FileUploadThreadHTTP extends DefaultFileUploadThread {
 
     private final static int CHUNKBUF_SIZE = 4096;
 
     private final static Pattern pChunked = Pattern.compile(
             "^Transfer-Encoding:\\s+chunked", Pattern.CASE_INSENSITIVE);
 
     private final static Pattern pClose = Pattern.compile(
             "^Connection:\\s+close", Pattern.CASE_INSENSITIVE);
 
     private final static Pattern pProxyClose = Pattern.compile(
             "^Proxy-Connection:\\s+close", Pattern.CASE_INSENSITIVE);
 
     private final static Pattern pHttpStatus = Pattern
             .compile("^HTTP/\\d\\.\\d\\s+((\\d+)\\s+.*)$");
 
     private final static Pattern pContentLen = Pattern.compile(
             "^Content-Length:\\s+(\\d+)$", Pattern.CASE_INSENSITIVE);
 
     private final static Pattern pContentTypeCs = Pattern.compile(
             "^Content-Type:\\s+.*;\\s*charset=([^;\\s]+).*$",
             Pattern.CASE_INSENSITIVE);
 
     private final static Pattern pSetCookie = Pattern.compile(
             "^Set-Cookie:\\s+(.*)$", Pattern.CASE_INSENSITIVE);
 
     private final byte chunkbuf[] = new byte[CHUNKBUF_SIZE];
 
     private CookieJar cookies = new CookieJar();
 
     /**
      * http boundary, for the posting multipart post.
      */
     private String boundary = "-----------------------------"
             + getRandomString();
 
     /**
      * local head within the multipart post, for each file. This is
      * precalculated for all files, in case the upload is not chunked. The heads
      * length are counted in the total upload size, to check that it is less
      * than the maxChunkSize. tails are calculated once, as they depend not of
      * the file position in the upload.
      */
     private ByteArrayEncoder heads[] = null;
 
     /**
      * same as heads, for the ... tail in the multipart post, for each file. But
      * tails depend on the file position (the boundary is added to the last
      * tail). So it's to be calculated fror each upload.
      */
     private ByteArrayEncoder tails[] = null;
 
     /**
      * This stream is open by {@link #startRequest(long, boolean, int, boolean)}.
      * It is closed by the {@link #cleanRequest()} method.
      * 
      * @see #startRequest(long, boolean, int, boolean)
      * @see #cleanRequest()
      * @see #getOutputStream()
      */
     private DataOutputStream httpDataOut = null;
 
     /**
      * The network socket where the bytes should be written.
      */
     private Socket sock = null;
 
     /**
      * This stream allows the applet to get the server response. It is opened
      * and closed as the {@link #httpDataOut}.
      */
     private InputStream httpDataIn = null;
 
     /**
      * This StringBuffer contains the body for the server response. That is: the
      * server response without the http header. This the real functionnal
      * response from the server application, that would be outputed, for
      * instance, by any 'echo' PHP command.
      */
     private StringBuffer sbHttpResponseBody = null;
 
     /**
      * Creates a new instance.
      * 
      * @param filesDataParam The files to upload.
      * @param uploadPolicy The policy to be applied.
      * @param progress The progress bar to be updated.
      */
     public FileUploadThreadHTTP(FileData[] filesDataParam,
             UploadPolicy uploadPolicy, JProgressBar progress) {
         super(filesDataParam, uploadPolicy, progress);
         uploadPolicy.displayDebug("Upload done by using the "
                 + getClass().getName() + " class", 40);
         // Name the thread (useful for debugging)
         setName("FileUploadThreadHTTP");
         this.heads = new ByteArrayEncoder[filesDataParam.length];
         this.tails = new ByteArrayEncoder[filesDataParam.length];
     }
 
     /** @see DefaultFileUploadThread#beforeRequest(int, int) */
     @Override
     void beforeRequest(int firstFileToUploadParam, int nbFilesToUploadParam)
             throws JUploadException {
         setAllHead(firstFileToUploadParam, nbFilesToUploadParam, this.boundary);
         setAllTail(firstFileToUploadParam, nbFilesToUploadParam, this.boundary);
     }
 
     /** @see DefaultFileUploadThread#getAdditionnalBytesForUpload(int) */
     @Override
     long getAdditionnalBytesForUpload(int index) throws JUploadIOException {
         return this.heads[index].getEncodedLength()
                 + this.tails[index].getEncodedLength();
     }
 
     /** @see DefaultFileUploadThread#afterFile(int) */
     @Override
     void afterFile(int index) throws JUploadIOException {
         try {
             this.httpDataOut.write(tails[index].getEncodedByteArray());
             this.uploadPolicy.displayDebug("--- filetail start (len="
                     + tails[index].getEncodedLength() + "):", 80);
             this.uploadPolicy.displayDebug(quoteCRLF(tails[index].getString()),
                     80);
             this.uploadPolicy.displayDebug("--- filetail end", 80);
         } catch (IOException e) {
             throw new JUploadIOException(e);
         }
     }
 
     /** @see DefaultFileUploadThread#beforeFile(int) */
     @Override
     void beforeFile(int index) throws JUploadException {
         // heads[i] contains the header specific for the file, in the multipart
         // content.
         // It is initialized at the beginning of the run() method. It can be
         // override at the beginning
         // of this loop, if in chunk mode.
         try {
             this.httpDataOut.write(this.heads[index].getEncodedByteArray());
 
             // Debug output: always called, so that the debug file is correctly
             // filled.
             this.uploadPolicy.displayDebug("--- fileheader start (len="
                     + this.heads[index].getEncodedLength() + "):", 80);
             this.uploadPolicy.displayDebug(quoteCRLF(this.heads[index]
                     .getString()), 80);
             this.uploadPolicy.displayDebug("--- fileheader end", 80);
         } catch (Exception e) {
             throw new JUploadException(e);
         }
     }
 
     /** @see DefaultFileUploadThread#cleanAll() */
     @SuppressWarnings("unused")
     @Override
     void cleanAll() throws JUploadException {
         // Nothing to do in HTTP mode.
     }
 
     /** @see DefaultFileUploadThread#cleanRequest() */
     @Override
     void cleanRequest() throws JUploadException {
         JUploadException localException = null;
 
         try {
             // Throws java.io.IOException
             this.httpDataOut.close();
         } catch (NullPointerException e) {
             // httpDataOut is already null ...
         } catch (IOException e) {
             localException = new JUploadException(e);
             this.uploadPolicy.displayErr(this.uploadPolicy
                     .getString("errDuringUpload"), e);
         } finally {
             this.httpDataOut = null;
         }
 
         try {
             // Throws java.io.IOException
             this.httpDataIn.close();
         } catch (NullPointerException e) {
             // httpDataIn is already null ...
         } catch (IOException e) {
             if (localException != null) {
                 localException = new JUploadException(e);
                 this.uploadPolicy.displayErr(this.uploadPolicy
                         .getString("errDuringUpload"), localException);
             }
         } finally {
             this.httpDataIn = null;
         }
 
         try {
             // Throws java.io.IOException
             this.sock.close();
         } catch (NullPointerException e) {
             // sock is already null ...
         } catch (IOException e) {
             if (localException != null) {
                 localException = new JUploadException(e);
                 this.uploadPolicy.displayErr(this.uploadPolicy
                         .getString("errDuringUpload"), e);
             }
         } finally {
             this.sock = null;
         }
 
         if (localException != null) {
             throw localException;
         }
     }
 
     @Override
     int finishRequest() throws JUploadException {
         boolean readingHttpBody = false;
         boolean gotClose = false;
         boolean gotChunked = false;
         boolean gotContentLength = false;
         int status = 0;
         int clen = 0;
         String line = "";
         byte[] body = new byte[0];
         String charset = "ISO-8859-1";
 
         this.sbHttpResponseBody = new StringBuffer();
         try {
             // If the user requested abort, we are not going to send
             // anymore, so shutdown the outgoing half of the socket.
             // This helps the server to speed up with it's response.
             if (this.stop && !(this.sock instanceof SSLSocket))
                 this.sock.shutdownOutput();
 
             // && is evaluated from left to right so !stop must come first!
             while (!this.stop && ((!gotContentLength) || (clen > 0))) {
                 if (readingHttpBody) {
                     // Read the http body
                     if (gotChunked) {
                         // Read the chunk header.
                         // This is US-ASCII! (See RFC 2616, Section 2.2)
                         line = readLine(httpDataIn, "US-ASCII", false);
                         if (null == line)
                             throw new JUploadException("unexpected EOF");
                         // Handle a single chunk of the response
                         // We cut off possible chunk extensions and ignore them.
                         // The length is hex-encoded (RFC 2616, Section 3.6.1)
                         int len = Integer.parseInt(line.replaceFirst(";.*", "")
                                 .trim(), 16);
                         this.uploadPolicy.displayDebug("Chunk: " + line
                                 + " dec: " + len, 80);
                         if (len == 0) {
                             // RFC 2616, Section 3.6.1: A length of 0 denotes
                             // the last chunk of the body.
 
                             // This code wrong if the server sends chunks
                             // with trailers! (trailers are HTTP Headers that
                             // are send *after* the body. These are announced
                             // in the regular HTTP header "Trailer".
                             // Fritz: Never seen them so far ...
                             // TODO: Implement trailer-handling.
                             break;
                         }
 
                         // Loop over the chunk (len == length of the chunk)
                         while (len > 0) {
                             int rlen = (len > CHUNKBUF_SIZE) ? CHUNKBUF_SIZE
                                     : len;
                             int ofs = 0;
                             if (rlen > 0) {
                                 while (ofs < rlen) {
                                     int res = this.httpDataIn.read(
                                             this.chunkbuf, ofs, rlen - ofs);
                                     if (res < 0)
                                         throw new JUploadException(
                                                 "unexpected EOF");
                                     len -= res;
                                     ofs += res;
                                 }
                                 if (ofs < rlen)
                                     throw new JUploadException("short read");
                                 if (rlen < CHUNKBUF_SIZE)
                                     body = byteAppend(body, this.chunkbuf, rlen);
                                 else
                                     body = byteAppend(body, this.chunkbuf);
                             }
                         }
                         // Got the whole chunk, read the trailing CRLF.
                         readLine(httpDataIn, false);
                     } else {
                         // Not chunked. Use either content-length (if available)
                         // or read until EOF.
                         if (gotContentLength) {
                             // Got a Content-Length. Read exactly that amount of
                             // bytes.
                             while (clen > 0) {
                                 int rlen = (clen > CHUNKBUF_SIZE) ? CHUNKBUF_SIZE
                                         : clen;
                                 int ofs = 0;
                                 if (rlen > 0) {
                                     while (ofs < rlen) {
                                         int res = this.httpDataIn.read(
                                                 this.chunkbuf, ofs, rlen - ofs);
                                         if (res < 0)
                                             throw new JUploadException(
                                                     "unexpected EOF");
                                         clen -= res;
                                         ofs += res;
                                     }
                                     if (ofs < rlen)
                                         throw new JUploadException("short read");
                                     if (rlen < CHUNKBUF_SIZE)
                                         body = byteAppend(body, this.chunkbuf,
                                                 rlen);
                                     else
                                         body = byteAppend(body, this.chunkbuf);
                                 }
                             }
                         } else {
                             // No Content-length available, read until EOF
                             // 
                             while (true) {
                                 byte[] lbuf = readLine(httpDataIn, true);
                                 if (null == lbuf)
                                     break;
                                 body = byteAppend(body, lbuf);
                             }
                             break;
                         }
                     }
                 } else {
                     // readingHttpBody is false, so we are still in headers.
                     // Headers are US-ASCII (See RFC 2616, Section 2.2)
                     String tmp = readLine(httpDataIn, "US-ASCII", false);
                     if (null == tmp)
                         throw new JUploadException("unexpected EOF");
                     if (status == 0) {
                         this.uploadPolicy.displayDebug(
                                 "-------- Response Headers Start --------", 80);
                         Matcher m = pHttpStatus.matcher(tmp);
                         if (m.matches()) {
                             status = Integer.parseInt(m.group(2));
                             setResponseMsg(m.group(1));
                         } else {
                             // The status line must be the first line of the
                             // response. (See RFC 2616, Section 6.1) so this
                             // is an error.
 
                             // We first display the wrong line.
                             this.uploadPolicy
                                     .displayDebug("First line of response: '"
                                             + tmp + "'", 80);
                             // Then, we throw the exception.
                             throw new JUploadException(
                                     "HTTP response did not begin with status line.");
                         }
                     }
                     // Handle folded headers (RFC 2616, Section 2.2). This is
                     // handled after the status line, because that line may
                     // not be folded (RFC 2616, Section 6.1).
                     if (tmp.startsWith(" ") || tmp.startsWith("\t"))
                         line += " " + tmp.trim();
                     else
                         line = tmp;
                     this.uploadPolicy.displayDebug(line, 80);
                     if (pClose.matcher(line).matches())
                         gotClose = true;
                     if (pProxyClose.matcher(line).matches())
                         gotClose = true;
                     if (pChunked.matcher(line).matches())
                         gotChunked = true;
                     Matcher m = pContentLen.matcher(line);
                     if (m.matches()) {
                         gotContentLength = true;
                         clen = Integer.parseInt(m.group(1));
                     }
                     m = pContentTypeCs.matcher(line);
                     if (m.matches())
                         charset = m.group(1);
                     m = pSetCookie.matcher(line);
                     if (m.matches())
                         this.cookies.parseCookieHeader(m.group(1));
                     if (line.length() == 0) {
                         // RFC 2616, Section 6. Body is separated by the
                         // header with an empty line.
                         readingHttpBody = true;
                         this.uploadPolicy.displayDebug(
                                 "--------- Response Headers End ---------", 80);
                     }
                 }
             } // while
 
             if (gotClose) {
                 // RFC 2868, section 8.1.2.1
                 cleanRequest();
             }
             // Convert the whole body according to the charset.
             // The default for charset ISO-8859-1, but overridden by
             // the charset attribute of the Content-Type header (if any).
             // See RFC 2616, Sections 3.4.1 and 3.7.1.
             this.sbHttpResponseBody.append(new String(body, charset));
         } catch (JUploadException e) {
             throw e;
         } catch (Exception e) {
             e.printStackTrace();
             throw new JUploadException(e);
         }
         return status;
     }
 
     /** @see DefaultFileUploadThread#getResponseBody() */
     @Override
     String getResponseBody() {
         return this.sbHttpResponseBody.toString();
     }
 
     /** @see DefaultFileUploadThread#getOutputStream() */
     @SuppressWarnings("unused")
     @Override
     OutputStream getOutputStream() throws JUploadException {
         return this.httpDataOut;
     }
 
     /** @see DefaultFileUploadThread#startRequest(long, boolean, int, boolean) */
     @Override
     void startRequest(long contentLength, boolean bChunkEnabled, int chunkPart,
             boolean bLastChunk) throws JUploadException {
         ByteArrayEncoder header = new ByteArrayEncoder();
 
         try {
             String chunkHttpParam = "jupart=" + chunkPart + "&jufinal="
                     + (bLastChunk ? "1" : "0");
             this.uploadPolicy.displayDebug("chunkHttpParam: " + chunkHttpParam,
                     40);
 
             URL url = new URL(this.uploadPolicy.getPostURL());
 
             // Add the chunking query params to the URL if there are any
             if (bChunkEnabled) {
                 if (null != url.getQuery() && !"".equals(url.getQuery())) {
                     url = new URL(url.toExternalForm() + "&" + chunkHttpParam);
                 } else {
                     url = new URL(url.toExternalForm() + "?" + chunkHttpParam);
                 }
             }
 
             Proxy proxy = null;
             proxy = ProxySelector.getDefault().select(url.toURI()).get(0);
             boolean useProxy = ((proxy != null) && (proxy.type() != Proxy.Type.DIRECT));
             boolean useSSL = url.getProtocol().equals("https");
 
             // Header: Request line
             // Let's clear it. Useful only for chunked uploads.
             header.append("POST ");
             if (useProxy && (!useSSL)) {
                 // with a proxy we need the absolute URL, but only if not
                 // using SSL. (with SSL, we first use the proxy CONNECT method,
                 // and then a plain request.)
                 header.append(url.getProtocol()).append("://").append(
                         url.getHost());
             }
             header.append(url.getPath());
 
             // Append the query params.
             // TODO: This probably can be removed as we now
             // have everything in POST data. However in order to be
             // backwards-compatible, it stays here for now. So we now provide
             // *both* GET and POST params.
             if (null != url.getQuery() && !"".equals(url.getQuery()))
                 header.append("?").append(url.getQuery());
 
             header.append(" ").append(this.uploadPolicy.getServerProtocol())
                     .append("\r\n");
 
             // Header: General
             header.append("Host: ").append(url.getHost()).append(
                     "\r\nAccept: */*\r\n");
             // We do not want gzipped or compressed responses, so we must
             // specify that here (RFC 2616, Section 14.3)
             header.append("Accept-Encoding: identity\r\n");
 
             // Seems like the Keep-alive doesn't work properly, at least on my
             // local dev (Etienne).
             // TODO: check, how the new code works
             if (!this.uploadPolicy.getAllowHttpPersistent()) {
                 header.append("Connection: close\r\n");
             } else {
                 if (!bChunkEnabled
                         || bLastChunk
                         || useProxy
                         || !this.uploadPolicy.getServerProtocol().equals(
                                 "HTTP/1.1")) { // RFC 2086, section 19.7.1
                     header.append("Connection: close\r\n");
                 } else {
                     header.append("Keep-Alive: 300\r\n");
                     if (useProxy)
                         header.append("Proxy-Connection: keep-alive\r\n");
                     else
                         header.append("Connection: keep-alive\r\n");
                 }
             }
 
             // Get the GET parameters from the URL and convert them to
             // post form params
             ByteArrayEncoder formParams = getFormParamsForPostRequest(url);
             contentLength += formParams.getEncodedLength();
 
             header.append("Content-Type: multipart/form-data; boundary=")
                     .append(this.boundary.substring(2)).append("\r\n");
             header.append("Content-Length: ").append(
                     String.valueOf(contentLength)).append("\r\n");
            header.append("Content-Type: ").append(header.getEncoding())
                    .append("\r\n");
 
             // Get specific headers for this upload.
             this.uploadPolicy.onAppendHeader(header);
 
             // Blank line (end of header)
             header.append("\r\n");
 
             // formParams are not really part of the main header, but we add
             // them here anyway. We write directly into the
             // ByteArrayOutputStream, as we already encoded them, to get the
             // encoded length. We need to flush the writer first, before
             // directly writting to the ByteArrayOutputStream.
             header.append(formParams);
 
             // Only connect, if sock is null!!
             // ... or if we don't persist HTTP connections (patch for IIS, based
             // on Marc Reidy's patch)
             if (this.sock == null || !uploadPolicy.getAllowHttpPersistent()) {
                 this.sock = new HttpConnect(this.uploadPolicy).Connect(url,
                         proxy);
                 this.httpDataOut = new DataOutputStream(
                         new BufferedOutputStream(this.sock.getOutputStream()));
                 this.httpDataIn = this.sock.getInputStream();
             }
 
             // The header is now constructed.
             header.close();
 
             // Send http request to server
             this.httpDataOut.write(header.getEncodedByteArray());
 
             // Debug output: always called, so that the debug file is correctly
             // filled.
             this.uploadPolicy.displayDebug("=== main header (len="
                     + header.getEncodedLength() + "):\n"
                     + quoteCRLF(header.getString()), 80);
             this.uploadPolicy.displayDebug("=== main header end", 80);
         } catch (IOException e) {
             throw new JUploadIOException(e);
         } catch (KeyManagementException e) {
             throw new JUploadException(e);
         } catch (UnrecoverableKeyException e) {
             throw new JUploadException(e);
         } catch (NoSuchAlgorithmException e) {
             throw new JUploadException(e);
         } catch (KeyStoreException e) {
             throw new JUploadException(e);
         } catch (CertificateException e) {
             throw new JUploadException(e);
         } catch (IllegalArgumentException e) {
             throw new JUploadException(e);
         } catch (URISyntaxException e) {
             throw new JUploadException(e);
         }
     }
 
     // ////////////////////////////////////////////////////////////////////////////////////
     // /////////////////////// PRIVATE METHODS
     // ///////////////////////////////////////
     // ////////////////////////////////////////////////////////////////////////////////////
 
     /**
      * Construction of a random string, to separate the uploaded files, in the
      * HTTP upload request.
      */
     private final String getRandomString() {
         StringBuffer sbRan = new StringBuffer(11);
         String alphaNum = "1234567890abcdefghijklmnopqrstuvwxyz";
         int num;
         for (int i = 0; i < 11; i++) {
             num = (int) (Math.random() * (alphaNum.length() - 1));
             sbRan.append(alphaNum.charAt(num));
         }
         return sbRan.toString();
     }
 
     /**
      * Creates a mime multipart string snippet, representing a POST variable.
      * 
      * @param bae The ByteArrayEncoder, in which the variable is to be added.
      * @param bound The multipart boundary to use.
      * @param name The name of the POST variable
      * @param value The value of the POST variable
      * @throws JUploadIOException An exception that can be thrown while trying
      *             to encode the given parameter to the current charset.
      */
     private final ByteArrayEncoder addPostVariable(ByteArrayEncoder bae,
             String bound, String name, String value) throws JUploadIOException {
         bae.append(bound).append("\r\n");
         bae.append("Content-Disposition: form-data; name=\"").append(name)
                 .append("\"\r\n");
         bae.append("Content-Transfer-Encoding: 8bit\r\n");
         bae.append("Content-Type: text/plain; UTF-8\r\n");
         // An empty line before the actual value.
         bae.append("\r\n");
         // And then, the value!
         bae.append(value).append("\r\n");
 
         return bae;
     }
 
     /**
      * Creates a mime multipart string snippet, representing a FORM. Extracts
      * all form elements of a given HTML form and assembles a StringBuffer which
      * contains a sequence of mime multipart messages which represent the
      * elements of that form.
      * 
      * @param bae The ByteArrayEncoder, in which the variable is to be added.
      * @param bound The multipart boundary to use.
      * @param formname The name of the form to evaluate.
      * @return the bae parameter, suitable for appending to the multipart
      *         content.
      * @throws JUploadIOException An exception that can be thrown while trying
      *             to encode the given parameter to the current charset.
      */
     private final ByteArrayEncoder addFormVariables(ByteArrayEncoder bae,
             String bound, String formname) throws JUploadIOException {
         try {
             JSObject win = JSObject.getWindow(this.uploadPolicy.getApplet());
             Object o = win.eval("document." + formname + ".elements.length");
             if (o instanceof Number) {
                 int len = ((Number) o).intValue();
                 if (len <= 0) {
                     this.uploadPolicy.displayWarn("The specified form \""
                             + formname + "\" does not contain any elements.");
                 }
                 int i;
                 for (i = 0; i < len; i++) {
                     try {
                         Object name = win.eval("document." + formname + "[" + i
                                 + "].name");
                         Object value = win.eval("document." + formname + "["
                                 + i + "].value");
                         Object etype = win.eval("document." + formname + "["
                                 + i + "].type");
                         if (etype instanceof String) {
                             String t = (String) etype;
                             if (t.equals("checkbox") || t.equals("radio")) {
                                 Object on = win.eval("document." + formname
                                         + "[" + i + "].checked");
                                 if (on instanceof Boolean) {
                                     // Skip unchecked checkboxes and
                                     // radiobuttons
                                     if (!((Boolean) on).booleanValue())
                                         continue;
                                 }
 
                             }
                         }
                         if (name instanceof String) {
                             if (value instanceof String) {
                                 addPostVariable(bae, bound, (String) name,
                                         (String) value);
                             }
                         }
                     } catch (JSException e1) {
                         this.uploadPolicy.displayDebug(e1.getStackTrace()[1]
                                 + ": got JSException, bailing out", 80);
                         i = len;
                     }
                 }
             } else {
                 this.uploadPolicy.displayWarn("The specified form \""
                         + formname + "\" could not be found.");
             }
         } catch (JSException e) {
             this.uploadPolicy.displayDebug(e.getStackTrace()[1]
                     + ": No JavaScript availabe", 80);
         }
         return bae;
     }
 
     /**
      * Returns the header for this file, within the http multipart body.
      * 
      * @param index Index of the file in the array that contains all files to
      *            upload.
      * @param bound The boundary that separate files in the http multipart post
      *            body.
      * @param chunkPart The numero of the current chunk (from 1 to n)
      * @return The encoded header for this file. The {@link ByteArrayEncoder} is
      *         closed within this method.
      * @throws JUploadException
      */
     private final ByteArrayEncoder getFileHeader(int index, String bound,
             @SuppressWarnings("unused")
             int chunkPart) throws JUploadException {
         String filenameEncoding = this.uploadPolicy.getFilenameEncoding();
         String mimetype = this.filesToUpload[index].getMimeType();
         String uploadFilename = this.filesToUpload[index]
                 .getUploadFilename(index);
         ByteArrayEncoder bae = new ByteArrayEncoder();
 
         // We'll encode the output stream into UTF-8.
         String form = this.uploadPolicy.getFormdata();
         if (null != form) {
             addFormVariables(bae, bound, form);
         }
         addPostVariable(bae, bound, "mimetype[]", mimetype);
         addPostVariable(bae, bound, "pathinfo[]", this.filesToUpload[index]
                 .getDirectory());
         addPostVariable(bae, bound, "relpathinfo[]", this.filesToUpload[index]
                 .getRelativeDir());
         // To add the file datetime, we first have to format this date.
         SimpleDateFormat dateformat = new SimpleDateFormat(uploadPolicy
                 .getDateFormat());
         String uploadFileModificationDate = dateformat
                 .format(this.filesToUpload[index].getLastModified());
         addPostVariable(bae, bound, "filemodificationdate[]",
                 uploadFileModificationDate);
 
         // boundary.
         bae.append(bound).append("\r\n");
 
         // Content-Disposition.
         bae.append("Content-Disposition: form-data; name=\"");
         bae.append(this.filesToUpload[index].getUploadName(index)).append(
                 "\"; filename=\"");
         if (filenameEncoding == null) {
             bae.append(uploadFilename);
         } else {
             try {
                 this.uploadPolicy.displayDebug("Encoded filename: "
                         + URLEncoder.encode(uploadFilename, filenameEncoding),
                         99);
                 bae.append(URLEncoder.encode(uploadFilename, filenameEncoding));
             } catch (UnsupportedEncodingException e) {
                 this.uploadPolicy
                         .displayWarn(e.getClass().getName() + ": "
                                 + e.getMessage()
                                 + " (in UploadFileData.getFileHeader)");
                 bae.append(uploadFilename);
             }
         }
         bae.append("\"\r\n");
 
         // Line 3: Content-Type.
         bae.append("Content-Type: ").append(mimetype).append("\r\n");
 
         // An empty line to finish the header.
         bae.append("\r\n");
 
         // The ByteArrayEncoder is now filled.
         bae.close();
         return bae;
     }// getFileHeader
 
     /**
      * Construction of the head for each file.
      * 
      * @param firstFileToUpload The index of the first file to upload, in the
      *            {@link #filesToUpload} area.
      * @param nbFilesToUpload Number of file to upload, in the next HTTP upload
      *            request. These files are taken from the {@link #filesToUpload}
      *            area
      * @param bound The String boundary between the post data in the HTTP
      *            request.
      * @throws JUploadException
      */
     private final void setAllHead(int firstFileToUpload, int nbFilesToUpload,
             String bound) throws JUploadException {
         for (int i = 0; i < nbFilesToUpload; i++) {
             this.heads[i] = getFileHeader(firstFileToUpload + i, bound, -1);
         }
     }
 
     /**
      * Construction of the tail for each file.
      * 
      * @param firstFileToUpload The index of the first file to upload, in the
      *            {@link #filesToUpload} area.
      * @param nbFilesToUpload Number of file to upload, in the next HTTP upload
      *            request. These files are taken from the {@link #filesToUpload}
      *            area
      * @param bound Current boundary, to apply for these tails.
      */
     private final void setAllTail(int firstFileToUpload, int nbFilesToUpload,
             String bound) throws JUploadException {
 
         for (int i = 0; i < nbFilesToUpload; i++) {
             // We'll encode the output stream into UTF-8.
             ByteArrayEncoder bae = new ByteArrayEncoder();
 
             bae.append("\r\n");
             addPostVariable(bae, bound, "md5sum[]", this.filesToUpload[i]
                     .getMD5());
 
             // The last tail gets an additional "--" in order to tell the
             // Server
             // we
             // have finished.
             if (nbFilesToUpload == i + 1) {
                 bae.append(bound).append("--\r\n");
             }
 
             // Let's store this tail.
             bae.close();
 
             this.tails[firstFileToUpload + i] = bae;
         }
 
     }
 
     /**
      * Converts the parameters in GET form to post form
      * 
      * @param url the <code>URL</code> containing the query parameters
      * @return the parameters in a string in the correct form for a POST request
      * @throws JUploadIOException
      */
     private final ByteArrayEncoder getFormParamsForPostRequest(final URL url)
             throws JUploadIOException {
 
         // Use a string buffer
         // We'll encode the output stream into UTF-8.
         ByteArrayEncoder bae = new ByteArrayEncoder();
 
         // Get the query string
         String query = url.getQuery();
 
         if (null != query) {
             // Split this into parameters
             HashMap<String, String> requestParameters = new HashMap<String, String>();
             String[] paramPairs = query.split("&");
             String[] oneParamArray;
 
             // Put the parameters correctly to the Hashmap
             for (String param : paramPairs) {
                 if (param.contains("=")) {
                     oneParamArray = param.split("=");
                     if (oneParamArray.length > 1) {
                         // There is a value for this parameter
                         requestParameters.put(oneParamArray[0],
                                 oneParamArray[1]);
                     } else {
                         // There is no value for this parameter
                         requestParameters.put(oneParamArray[0], "");
                     }
                 }
             }
 
             // Now add one multipart segment for each
             for (String key : requestParameters.keySet())
                 addPostVariable(bae, this.boundary, key, requestParameters
                         .get(key));
         }
         // Return the body content
         bae.close();
 
         return bae;
     }// getFormParamsForPostRequest
 
     // //////////////////////////////////////////////////////////////////////////////////////
     // //////////////////// Various utilities
     // //////////////////////////////////////////////////////////////////////////////////////
 
     /**
      * Similar like BufferedInputStream#readLine() but operates on raw bytes.
      * Line-Ending is <b>always</b> "\r\n".
      * 
      * @param charset The input charset of the stream.
      * @param includeCR Set to true, if the terminating CR/LF should be included
      *            in the returned byte array.
      */
     static String readLine(InputStream inputStream, String charset,
             boolean includeCR) throws IOException {
         byte[] line = readLine(inputStream, includeCR);
         return (null == line) ? null : new String(line, charset);
     }
 
     /**
      * Similar like BufferedInputStream#readLine() but operates on raw bytes.
      * Line-Ending is <b>always</b> "\r\n".
      * 
      * @param includeCR Set to true, if the terminating CR/LF should be included
      *            in the returned byte array.
      */
     static byte[] readLine(InputStream inputStream, boolean includeCR)
             throws IOException {
         int len = 0;
         int buflen = 128; // average line length
         byte[] buf = new byte[buflen];
         byte[] ret = null;
         int b;
         while (true) {
             b = inputStream.read();
             switch (b) {
                 case -1:
                     if (len > 0) {
                         ret = new byte[len];
                         System.arraycopy(buf, 0, ret, 0, len);
                         return ret;
                     }
                     return null;
                 case 10:
                     if ((len > 0) && (buf[len - 1] == 13)) {
                         if (includeCR) {
                             ret = new byte[len + 1];
                             if (len > 0)
                                 System.arraycopy(buf, 0, ret, 0, len);
                             ret[len] = 10;
                         } else {
                             len--;
                             ret = new byte[len];
                             if (len > 0)
                                 System.arraycopy(buf, 0, ret, 0, len);
                         }
                         return ret;
                     }
                 default:
                     buf[len++] = (byte) b;
                     if (len >= buflen) {
                         buflen *= 2;
                         byte[] tmp = new byte[buflen];
                         System.arraycopy(buf, 0, tmp, 0, len);
                         buf = tmp;
                     }
             }
         }
     }
 
     /**
      * Concatenates two byte arrays.
      * 
      * @param buf1 The first array
      * @param buf2 The second array
      * @return A byte array, containing buf2 appended to buf2
      */
     static byte[] byteAppend(byte[] buf1, byte[] buf2) {
         byte[] ret = new byte[buf1.length + buf2.length];
         System.arraycopy(buf1, 0, ret, 0, buf1.length);
         System.arraycopy(buf2, 0, ret, buf1.length, buf2.length);
         return ret;
     }
 
     /**
      * Concatenates two byte arrays.
      * 
      * @param buf1 The first array
      * @param buf2 The second array
      * @param len Number of bytes to copy from buf2
      * @return A byte array, containing buf2 appended to buf2
      */
     static byte[] byteAppend(byte[] buf1, byte[] buf2, int len) {
         if (len > buf2.length)
             len = buf2.length;
         byte[] ret = new byte[buf1.length + len];
         System.arraycopy(buf1, 0, ret, 0, buf1.length);
         System.arraycopy(buf2, 0, ret, buf1.length, len);
         return ret;
     }
 
 }
