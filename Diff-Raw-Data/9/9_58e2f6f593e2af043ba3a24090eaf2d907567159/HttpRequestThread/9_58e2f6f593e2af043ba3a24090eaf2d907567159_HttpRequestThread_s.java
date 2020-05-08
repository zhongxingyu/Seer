 /*
     Copyright (C) 2010 Brian Dunigan
 
     This program is free software: you can redistribute it and/or modify
     it under the terms of the GNU General Public License as published by
     the Free Software Foundation, either version 3 of the License, or
     (at your option) any later version.
 
     This program is distributed in the hope that it will be useful,
     but WITHOUT ANY WARRANTY; without even the implied warranty of
     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
     GNU General Public License for more details.
 
     You should have received a copy of the GNU General Public License
     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
 
 package org.openstatic.http;
 
 import java.net.Socket;
 import java.net.URLDecoder;
 import java.util.StringTokenizer;
 import java.io.InputStream;
 import java.io.BufferedReader;
 import java.io.InputStreamReader;
 import java.util.Hashtable;
 
 public class HttpRequestThread extends Thread
 {
     private InputStream is;
     private Socket connection;
     private PlaceboHttpServer myServer;
     private String clientHostname;
     
     public HttpRequestThread(Socket connection, PlaceboHttpServer myServer)
     {
         try
         {
             this.is = connection.getInputStream();
         } catch (Exception n) {}
         this.connection = connection;
         this.myServer = myServer;
         this.clientHostname = this.connection.getInetAddress().getCanonicalHostName();
     }
     
     // This thread is to work with the incomming data. And parses http requests very roughly
     // most of the fun stuff happens at the handlehttpRequest part.
     public void run()
     {
         try
         {            
             // here we process input from the Browser
             BufferedReader br = new BufferedReader(new InputStreamReader(this.is));
             
             String cmd_line = null;
             
             String request_type = null;
             String request_path = null;
             String path_token = null;
             Hashtable<String, String> headers = new Hashtable<String, String>();
             Hashtable<String, String> formContent = new Hashtable<String, String>();
             Hashtable<String, String> urlGetParameters = new Hashtable<String, String>();
             Hashtable<String, String> cookies = new Hashtable<String, String>();
             String raw_form_data = "";
             String get_params = "";
             
             try
             {
                 // Recieve GET/POST line and process
                 cmd_line = br.readLine();
                 this.myServer.logln(this.clientHostname, "-> " + cmd_line);
                 StringTokenizer request = new StringTokenizer(cmd_line);
 
                 while (request.hasMoreTokens())
                 {
                     String currentToken = request.nextToken();
                     if ("GET".equals(currentToken) && request.hasMoreTokens())
                     {
                         path_token = URLDecoder.decode(request.nextToken(),"UTF-8");
                         request_type = "GET";
                     }
                     if ("POST".equals(currentToken) && request.hasMoreTokens())
                     {
                         path_token = URLDecoder.decode(request.nextToken(),"UTF-8");
                         request_type = "POST";
                     }
                 }
 
                 // handle get parameters
                 int qidx = path_token.indexOf("?");
                 if (qidx > -1)
                 {
                     request_path = path_token.substring(0,qidx);
                     get_params = path_token.substring(qidx+1);
                     StringTokenizer get_data = new StringTokenizer(get_params, "&");
                     while (get_data.hasMoreTokens())
                     {
                         String currentToken = get_data.nextToken();
                         if (currentToken.indexOf("=") > -1 && !currentToken.endsWith("="))
                         {
                             StringTokenizer get_entry = new StringTokenizer(currentToken, "=");
                             String g_key = get_entry.nextToken();
                             String g_value = get_entry.nextToken();
                             urlGetParameters.put(g_key, g_value);
                             
                             this.myServer.logln(this.clientHostname, "-> (GETPARAMETER) {" + g_key + "} " + g_value);
                         }
                     }
                 } else {
                     request_path = path_token;
                 }
                 
                 while (!"".equals(cmd_line))
                 {
                     cmd_line = br.readLine();
                     //this.myServer.logln(this.clientHostname, "-> " + cmd_line);
                     if (!"".equals(cmd_line))
                     {
                         StringTokenizer header_parts = new StringTokenizer(cmd_line, ":");
                         // lets store the request headers just incase                    
                         String h_key = header_parts.nextToken();
                         String h_value = header_parts.nextToken().trim();;
                         headers.put(h_key, h_value);
                         this.myServer.logln(this.clientHostname, "-> (HEADER) {" + h_key + "} " + h_value);
                     }
                 }
                 
                 // Is there a cookie?
                 String cookie = headers.get("Cookie");
                 if (cookie != null)
                 {
                     StringTokenizer cookie_data = new StringTokenizer(cookie, ";");
                     while (cookie_data.hasMoreTokens())
                     {
                         StringTokenizer this_cookie = new StringTokenizer(cookie_data.nextToken().trim(), "=");
                         String c_key = this_cookie.nextToken();
                         String c_value = this_cookie.nextToken();
                         if (!cookies.containsKey(c_key))
                         {
                             cookies.put(c_key, c_value);
                             this.myServer.logln(this.clientHostname, "-> (COOKIE) {" + c_key + "} " + c_value);
                         } else {
                             cookies.put(c_key, c_value);
                             this.myServer.logln(this.clientHostname, "<> (COOKIE) {" + c_key + "} *OVERWRITE* " + c_value);
                         }
                     }
                     this.myServer.logln(this.clientHostname, "-> (END OF COOKIES)");
                 }
                 
                 // what do we if there was a post!
                 if (request_type.equals("POST"))
                 {
                     int content_length = Integer.valueOf(headers.get("Content-Length")).intValue();
                     this.myServer.logln(this.clientHostname, "***  POST DATA");
                     int bytein = -2;
                     int byteCount = 0;
                     StringBuffer form_raw = new StringBuffer("");
                     while (bytein != -1 && byteCount < content_length)
                     {
                         bytein = br.read();
                         byteCount++;
                         if (bytein > -1) form_raw.append((char) bytein);
                     }
                     raw_form_data = form_raw.toString();
                     if (this.myServer.isShowData())
                     {
                         this.myServer.getDebugStream().println("--------------Inbound Data-------------");
                         this.myServer.getDebugStream().println(raw_form_data);                        
                         this.myServer.getDebugStream().println("---------------------------------------");
                     }
 
                 }
                 
                 // what do we do if it was a form post?
                 if ("application/x-www-form-urlencoded".equals(headers.get("Content-Type")))
                 {
                     this.myServer.logln(this.clientHostname, "***  application/x-www-form-urlencoded");
                     StringTokenizer form_data = new StringTokenizer(raw_form_data, "&");
                     while (form_data.hasMoreTokens())
                     {
                         String currentToken = form_data.nextToken();
                         if (currentToken.indexOf("=") > -1)
                         {
                             StringTokenizer form_entry = new StringTokenizer(currentToken, "=");
                             String f_key = form_entry.nextToken();
                            String f_value = URLDecoder.decode(form_entry.nextToken(),"UTF-8");
                            formContent.put(f_key, f_value);
                            this.myServer.logln(this.clientHostname, "-> (FORMDATA) {" + f_key + "} " + f_value);
                         }
                     }
                 }
             } catch (Exception rex) {
                 this.myServer.logln("Placebo", "Exception: " + rex.toString() + " / " + rex.getMessage());
             }
             
             if (request_type != null)
             {
                 HttpRequest req_obj = new HttpRequest(request_path, request_type, headers, cookies, formContent, raw_form_data, urlGetParameters, get_params, this.connection, this.myServer);
                 this.myServer.routeRequest(req_obj);
             }
         } catch (Exception x) {}
     }
 }
