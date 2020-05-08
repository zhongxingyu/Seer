 /*
  * $Id$
  *
  * Copyright 2003-2006 Orange Nederland Breedband B.V.
  * See the COPYRIGHT file for redistribution and use restrictions.
  */
 package org.xins.tests.server;
 
 import java.io.*;
 import java.net.*;
 import java.util.*;
 
 import org.xins.common.text.ParseException;
 
 /**
  * Utility class for making HTTP requests.
  *
  * @version $Revision$ $Date$
  * @author <a href="mailto:ernst.dehaan@orange-ft.com">Ernst de Haan</a>
  */
 public class HTTPCaller extends Object {
 
    //-------------------------------------------------------------------------
    // Class functions
    //-------------------------------------------------------------------------
 
   static Result call(String host, int port, String method, String queryString, Properties inputHeaders)
    throws IOException, ParseException {
 
       // TODO: Send input headers
 
       String eol = "\r\n";
 
       // Prepare a connection
       Socket socket = new Socket(host, port);
 
       byte[] buffer = new byte[16384];
       try {
 
          // Get the input and output streams
          OutputStream out = socket.getOutputStream();
          InputStream  in  = socket.getInputStream();
 
          // Construct the output string
         String toWrite = method + ' ' + queryString + " HTTP/1.1" + eol
                        + "Host: " + host + eol;
          if (inputHeaders != null) {
             Enumeration names = inputHeaders.propertyNames();
             while (names.hasMoreElements()) {
                String key   = (String) names.nextElement();
                String value = inputHeaders.getProperty(key);
 
                toWrite += key + ": " + value + eol;
             }
          }
          toWrite += eol;
 System.err.println("Sending: \"" + toWrite + "\".");
 
          // Write the output
          out.write(toWrite.getBytes());
 
          // Read the input
         in.read(buffer);
       } finally {
          try {
             socket.close();
          } catch (Throwable exception) {
             // ignore
          }
       }
 
       // Convert the response to a character string
      String response = new String(buffer, "ISO-8859-1");
 
       // Prepare the result
       Result result = new Result();;
 
       // Get the first line
       int index = response.indexOf(' ');
       String intro = response.substring(0, index);
       int index2 = response.indexOf(eol);
       result._status = response.substring(index + 1, index2);
 System.err.println("Status is: \"" + result._status + "\".");
 
       // Remove the part we processed
       response = response.substring(index2 + 2);
 
       // Get the headers
       result._headers = new HashMap();
       boolean done = false;
       while (! done) {
          int nextEOL = response.indexOf(eol);
          if (nextEOL < 0) {
             return result;
          } else if (nextEOL == 0) {
             done = true;
          } else {
             parseHeader(result._headers, response.substring(0, nextEOL));
             response = response.substring(nextEOL + 2);
          }
       }
 
       // Get the body
      int index3 = response.indexOf(eol + eol);
      result._body = (index3 < 0)
                   ? ""
                   : response.substring(index3 + 2);
 System.err.println("Body is: \"" + result._body + "\".");
 
       return result;
    }
 
    private static void parseHeader(HashMap headers, String header)
    throws ParseException{
       int index = header.indexOf(':');
       if (index < 1) {
          throw new ParseException();
       }
 
       // Get key and value
       String key   = header.substring(0, index);
       String value = header.substring(index + 1);
 
       // Always convert the key to upper case
       key = key.toUpperCase();
 
       // Always trim the value
       value = value.trim();
 System.err.println("Found header with key \"" + key + "\" and value \"" + value + "\".");
 
       // Store the value in the list associated by key
       List list = (List) headers.get(key);
       if (list == null) {
          list = new ArrayList();
          headers.put(key, list);
       }
       list.add(value);
    }
 
 
    //-------------------------------------------------------------------------
    // Class fields
    //-------------------------------------------------------------------------
 
    //-------------------------------------------------------------------------
    // Constructor
    //-------------------------------------------------------------------------
 
    private HTTPCaller() {
    }
 
 
    //-------------------------------------------------------------------------
    // Inner classes
    //-------------------------------------------------------------------------
 
    static class Result extends Object {
 
       private Result() {
       }
 
       private String _status;
       private String _body;
       private HashMap _headers;
 
       String getStatus() {
          return _status;
       }
 
       String getBody() {
         return _body;
       }
 
       List getHeaderValues(String key) {
          Object value = _headers.get(key.toUpperCase());
         if (value == null) {
            return new ArrayList();
         } else {
            return (List) value;
         }
       }
    }
 }
 
