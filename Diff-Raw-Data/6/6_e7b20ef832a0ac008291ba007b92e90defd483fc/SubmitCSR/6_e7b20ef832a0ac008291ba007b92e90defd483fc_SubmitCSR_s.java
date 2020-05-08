 /*
  * OpenRemote, the Home of the Digital Home.
  * Copyright 2008-2011, OpenRemote Inc.
  *
  * See the contributors.txt file in the distribution for a
  * full listing of individual contributors.
  *
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU Affero General Public License as
  * published by the Free Software Foundation, either version 3 of the
  * License, or (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
  * GNU Affero General Public License for more details.
  *
  * You should have received a copy of the GNU Affero General Public License
  * along with this program. If not, see <http://www.gnu.org/licenses/>.
  */
 package org.openremote.controller.rest;
 
 import java.security.MessageDigest;
 import java.security.NoSuchAlgorithmException;
 
 import java.lang.ProcessBuilder;
 import java.lang.InterruptedException;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.FileReader;
 import java.io.FileWriter;
 import java.io.BufferedReader;
 import java.io.InputStream;
 import java.io.BufferedWriter;
 import java.io.InputStreamReader;
 
 import java.nio.charset.Charset;
 
 import java.net.URLDecoder;
 
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 import java.util.List;
 import java.util.ArrayList;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import org.apache.commons.codec.binary.Base64;
 import org.apache.commons.codec.binary.Hex;
 
 import org.apache.log4j.Logger;
 import org.openremote.controller.Constants;
 import org.openremote.controller.exception.ControlCommandException;
 import org.openremote.controller.service.ClientService;
 import org.openremote.controller.spring.SpringContext;
 
 /**
  * This servlet implements the REST API '/rest/certificate/create' functionality which creates
  * a certificate when a call has been done.  <p>
  *
  * See <a href = "http://www.openremote.org/display/docs/Controller+2.0+HTTP-REST-XML">
  * Controller 2.0 REST XML API<a> and
  * <a href = "http://openremote.org/display/docs/Controller+2.0+HTTP-REST-JSONP">Controller 2.0
  * REST JSONP API</a> for more details.
  *
  * @author <a href="mailto:vincent.kriek@tass.nl">Vincent Kriek</a>
  */
 public class SubmitCSR extends RESTAPI
 {
 
   /*
    *  IMPLEMENTATION NOTES:
    *
    *    - This adheres to the current 2.0 version of the HTTP/REST/XML and HTTP/REST/JSON APIs.
    *      There's currently no packaging or REST URL distinction for supported API versions.
    *      Later versions of the Controller may support multiple revisions of the API depending
    *      on client request. Appropriate implementation changes should be made then.
    *                                                                                      [JPL]
    */
 
 
   // Class Members --------------------------------------------------------------------------------
 
   /**
    * Common log category for HTTP REST API.
    */
   private final static Logger logger = Logger.getLogger(Constants.REST_ALL_PANELS_LOG_CATEGORY);
 
   private final static String CA_LOCATION = "/usr/share/tomcat6/cert/ca/";
   private final static String CSR_HEADER = "-----BEGIN NEW CERTIFICATE REQUEST-----";
   private final static String CSR_FOOTER = "\n-----END NEW CERTIFICATE REQUEST-----\n";
   private final static String openssl = "/usr/bin/openssl";
 
   private static final ClientService clientService = (ClientService) SpringContext.getInstance().getBean("clientService");
 
   /**
    * Execute a openssl command
    * @param filename the file to examine
    * @return OpenSSL's output
    */
   private String executeOpenSSL(String filename)
   {
     List<String> command = new ArrayList<String>();
     command.add(openssl);
     command.add("req");
     command.add("-subject");
     command.add("-pubkey");
     command.add("-noout");
     command.add("-in");
     command.add("csr/" + filename);
 
     ProcessBuilder pb = new ProcessBuilder(command);
     pb.directory(new File(CA_LOCATION));
     
     Process p = null;
     StringBuffer buffer = new StringBuffer();
 
     try {
         p = pb.start();
         p.waitFor();
 
         BufferedReader br = new BufferedReader(
                 new InputStreamReader(p.getInputStream()));
 
         String line = null;
         while((line = br.readLine()) != null) {
             buffer.append(line).append("\n");
         }
     } catch (IOException e) {
         logger.error(e.getMessage());
     } catch (InterruptedException e) {
         logger.error(e.getMessage());
     }
     return buffer.toString();
   }
 
    /**
     * Gerate an md5sum from a base64 encoded message
     * @param message the Base64 encoded message
     * @return MD5Sum as a hex encoded string
     */
    private String generateMD5Sum(String message) throws NoSuchAlgorithmException
    {
       final MessageDigest messageDigest = MessageDigest.getInstance("MD5");
       messageDigest.reset();
       messageDigest.update(message.getBytes(Charset.forName("UTF8")));
       final byte[] resultByte = messageDigest.digest();
       return new String(Hex.encodeHex(resultByte));
    }
 
   /**
    * Retrieve the client information from an CSR file
    * @param filename the file name of the CSR
    */
   private void getClientInformation(String filename)
   {
       String message = executeOpenSSL(filename);
       String username = null;
       String pinCode = null;
       String email = "";
       
       try
       {
          username = message.substring(message.indexOf("CN=") + 3);
          username = username.substring(0, username.indexOf("/"));
       }
       catch(IndexOutOfBoundsException e)
       {
          logger.error(e.getMessage());
       }
 
       try
       {
          String publicKey = message.substring(message.indexOf("KEY-----") + 9, message.lastIndexOf("-----END") - 1);
          if(!publicKey.isEmpty())
          {
             pinCode = generateMD5Sum(publicKey);
             pinCode = pinCode.substring(pinCode.length() - 4, pinCode.length());
          }
          else
          {
             pinCode = "<i>No public key</i>";
          }
       }
       catch(IndexOutOfBoundsException e)
       {
          logger.error(e.getMessage());
       }
       catch (NoSuchAlgorithmException e)
       {
          logger.error(e.getMessage());
       }
       if(!clientService.addClient(pinCode, username, email, filename))
       {
           logger.error("Database insertion went WRONG");
       }
   }
 
   /**
    * Write CSR to file
    */
   protected String putCsr(String username, String cert) throws IOException
   {
     String certificate = URLDecoder.decode(cert);
    BufferedWriter out = new BufferedWriter(new FileWriter(CA_LOCATION + "csr/" + username + ".csr"));
 
     out.write(CSR_HEADER);
     int j = 0;
     for(int i = 0; i < certificate.length(); ++i)
     {
         if((j++ % 65) == 0) {
             out.write("\n");
         } 
         out.write(certificate.charAt(i));
     }
     out.write(CSR_FOOTER);
     out.close();
 
    getClientInformation(username + ".csr");
 
     return certificate;
   }
 
   // Implement REST API ---------------------------------------------------------------------------
   @Override protected void handleRequest(HttpServletRequest request, HttpServletResponse response)
   {
     try
     {
         String url = request.getRequestURL().toString().trim();
         String regexp = "rest\\/cert\\/put\\/(.*)";
         Pattern pattern = Pattern.compile(regexp);
         Matcher matcher = pattern.matcher(url);
         if(matcher.find()) 
             sendResponse(response, putCsr(matcher.group(1), request.getParameter("csr")));
     }
 
     catch (ControlCommandException e)
     {
       logger.error("failed to get all the panels", e);
 
       // TODO :
       //   this might well break the JSON client code -- but can't know for sure cause chinese
       //   are too effin dumb to write proper tests
       //
        response.setStatus(e.getErrorCode());
 
       //sendResponse(response, e.getErrorCode(), e.getMessage());
     }
     catch (Exception e) 
     {
         logger.error("Failed to create certificate");
         sendResponse(response, e.getMessage());
     }
   }
 }
