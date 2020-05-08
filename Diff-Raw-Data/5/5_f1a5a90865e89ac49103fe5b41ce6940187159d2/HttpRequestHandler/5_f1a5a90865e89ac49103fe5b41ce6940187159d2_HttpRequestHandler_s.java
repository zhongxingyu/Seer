 package br.unb.tr2.harmonic.httpServer;
 
 import br.unb.tr2.harmonic.entity.CalculationInterval;
 import br.unb.tr2.harmonic.server.CalculationManager;
 import br.unb.tr2.harmonic.server.HarmonicServer;
 
 import java.io.*;
 import java.net.Socket;
 import java.util.*;
 import java.util.logging.Logger;
 
 /**
  * Copyright (C) 2013 Loop EC - All Rights Reserved
  * Created by sandoval for harmonic-server
  */
 public class HttpRequestHandler implements Runnable {
 
     private HttpServer httpServer;
 
     private BufferedWriter writer = null;
 
     private BufferedReader reader = null;
 
     private String request;
 
     private Map<String,String> urlParameters = null;
 
     private User loggedUser;
 
     private Socket socket;
 
     private Logger logger = Logger.getLogger("HttpRequestHandler");
 
     public HttpRequestHandler(Socket socket, HttpServer httpServer) throws IOException {
         this.socket = socket;
         reader = new BufferedReader(new InputStreamReader(this.socket.getInputStream()));
         writer = new BufferedWriter(new OutputStreamWriter(this.socket.getOutputStream()));
         this.httpServer = httpServer;
     }
 
     @Override
     public void run() {
         try {
             request = reader.readLine();
             parseUrlParams();
             if (request.startsWith("GET / ") || request.startsWith("GET /index")) {
                 serveView("index");
             } else if (request.startsWith("GET /login")) {
                 User user = retrieveUser(urlParameters.get("user"), urlParameters.get("password"));
                 if (user == null) {
                     logger.info("Failed login attempt: " + urlParameters.get("user") + ":" + urlParameters.get("password"));
                     serve401();
                 } else {
                     if (user.getRole() == Role.ADMIN)
                         redirect("/admin");
                     else if (user.getRole() == Role.USER)
                         redirect("/user");
                 }
             } else if (request.startsWith("GET /admin/intervals/recalculate")) {
                 if (retrieveUser(urlParameters.get("user"), urlParameters.get("password")) == null) {
                     logger.info("Failed login attempt: " + urlParameters.get("user") + ":" + urlParameters.get("password"));
                     serve401();
                 } else if (loggedUser.getRole() != Role.ADMIN) {
                     logger.info("User tried to access admin page: " + loggedUser.getUsername());
                     serve401();
                 } else {
                     recalculateInterval();
                     redirect("/admin/intervals");
                 }
             } else if (request.startsWith("GET /admin/intervals")) {
                 if (retrieveUser(urlParameters.get("user"), urlParameters.get("password")) == null) {
                     logger.info("Failed login attempt: " + urlParameters.get("user") + ":" + urlParameters.get("password"));
                     serve401();
                 } else if (loggedUser.getRole() != Role.ADMIN) {
                     logger.info("User tried to access admin page: " + loggedUser.getUsername());
                     serve401();
                 } else {
                     serveIntervalsView();
                 }
             } else if (request.startsWith("GET /admin")) {
                 if (retrieveUser(urlParameters.get("user"), urlParameters.get("password")) == null) {
                     logger.info("Failed login attempt: " + urlParameters.get("user") + ":" + urlParameters.get("password"));
                     serve401();
                 } else if (loggedUser.getRole() != Role.ADMIN) {
                     logger.info("User tried to access admin page: " + loggedUser.getUsername());
                     serve401();
                 } else {
                     serveAdminView();
                 }
             } else if (request.startsWith("GET /user")) {
                 if (retrieveUser(urlParameters.get("user"), urlParameters.get("password")) == null) {
                     logger.info("Failed login attempt: " + urlParameters.get("user") + ":" + urlParameters.get("password"));
                     serve401();
                 } else {
                     serveUserView();
                 }
             } else if (request.startsWith("GET /removeUser")) {
                 if (retrieveUser(urlParameters.get("user"), urlParameters.get("password")) == null) {
                     logger.info("Failed login attempt: " + urlParameters.get("user") + ":" + urlParameters.get("password"));
                     serve401();
                 } else if (loggedUser.getRole() != Role.ADMIN) {
                     logger.info("User tried to remove user without rights: " + loggedUser.getUsername());
                     serve401();
                 } else {
                     removeUser();
                     redirect("/admin");
                 }
             } else if (request.startsWith("GET /addUser")) {
                 if (retrieveUser(urlParameters.get("user"), urlParameters.get("password")) == null) {
                     logger.info("Failed login attempt: " + urlParameters.get("user") + ":" + urlParameters.get("password"));
                     serve401();
                 } else if (loggedUser.getRole() != Role.ADMIN) {
                     logger.info("User tried to add user without rights: " + loggedUser.getUsername());
                     serve401();
                 } else {
                     addUser();
                     redirect("/admin");
                 }
             } else {
                 serve404();
             }
             writer.close();
         } catch (IOException e) {
             e.printStackTrace();
         }
     }
 
     private void recalculateInterval() {
         CalculationManager.getInstance().recalculate(
                 new CalculationInterval(new Long(urlParameters.get("recalculationStart")), new Long(urlParameters.get("recalculationEnd"))));
     }
 
     private void serveIntervalsView() throws IOException {
         writer.write("HTTP/1.1 200 OK\n" +
                 "status: 200 OK\n" +
                 "version: HTTP/1.1\n" +
                 "content-type: text/html; charset=UTF-8\n\n");
         serveSnippet("admin/intervals/1");
         for (CalculationInterval interval : CalculationManager.getInstance().calculatedIntervalsCollection()) {
             writer.write("<tr><td>" + interval.getStart() + " - " + interval.getEnd() + "</td>\n" +
                     "<td>" + interval.getResult() + "</td>\n" +
                     "<td>" + interval.getExecutionTime() + "</td>\n" +
                     "<td><a href=\"/admin/intervals/recalculate?" + loginUrlParameters() +
                     "&recalculationStart=" + interval.getStart() + "&recalculationEnd=" + interval.getEnd() + "\">X</a></td></tr>");
         }
         serveSnippet("admin/intervals/2");
         writer.flush();
     }
 
     private String loginUrlParameters() {
         return "user=" + loggedUser.getUsername() + "&password=" + loggedUser.getPassword();
     }
 
     private void removeUser() {
         httpServer.getUsers().remove(urlParameters.get("removeUser"));
     }
 
     private void addUser() {
         User user = new User(urlParameters.get("newusername"), urlParameters.get("newpassword"), Role.valueOf(urlParameters.get("newrole")));
         httpServer.getUsers().put(urlParameters.get("newusername"), user);
     }
 
     private void serveAdminView() throws IOException {
         writer.write("HTTP/1.1 200 OK\n" +
                 "status: 200 OK\n" +
                 "version: HTTP/1.1\n" +
                 "content-type: text/html; charset=UTF-8\n\n");
         serveSnippet("admin/1");
         writer.write(CalculationManager.getInstance().getCalculation().toString());
        writer.write(" <span> (<a href=\"/admin/intervals?" + urlParams() + "\">ver intervalos</a>)</span>");
         serveSnippet("admin/2");
         writer.write("<tr><td>" + HarmonicServer.getInstance().getServerInstance().getAddress().getHostAddress() +
                 ":" + HarmonicServer.getInstance().getServerInstance().getPort() + "</td></tr>");
         serveSnippet("admin/2.5");
         writer.write("<tr><td>" + CalculationManager.getInstance().calculatedIntervals() + "</td></tr>");
         serveSnippet("admin/3");
         for(CalculationInterval interval : CalculationManager.getInstance().pendingCalculationIntervals()) {
             writer.write("<tr><td>" + interval.getStart() + " - " + interval.getEnd() + "</tr></td>");
         }
         serveSnippet("admin/3.3");
         writer.write("<input type=\"hidden\" name=\"user\" value=\"" + loggedUser.getUsername() + "\" />");
         writer.write("<input type=\"hidden\" name=\"password\" value=\"" + loggedUser.getPassword() + "\" />");
         serveSnippet("admin/3.5");
         Iterator<User> i = httpServer.getUsers().values().iterator();
         while (i.hasNext()) {
             User user = i.next();
             writer.write("<tr>\n" +
                     "    <td>" + user.getUsername() + "</td>\n" +
                     "    <td>" + user.getRole().getLabel() + "</td>\n" +
                     "    <td style=\"text-align: center\"><a href=\"/removeUser?user=" + loggedUser.getUsername() +
                     "&password=" + loggedUser.getPassword() + "&removeUser=" + user.getUsername() + "\">X</a></td>\n" +
                     "</tr>");
         }
         serveSnippet("admin/4");
         writer.flush();
     }
 
     private void serve404() throws IOException {
         writer.write("HTTP/1.1 404 Not Found\n" +
                 "Content-Type: text/html; charset=UTF-8\n\n");
         BufferedReader fileReader = new BufferedReader(new InputStreamReader(InputStream.class.getResourceAsStream("/html/404.html")));
         String line = fileReader.readLine();
         do {
             writer.write(line);
             line = fileReader.readLine();
         } while (line != null);
         writer.flush();
     }
 
     private void serve401() throws IOException {
         writer.write("HTTP/1.1 401 Unauthorized\n" +
                 "Content-Type: text/html; charset=UTF-8\n\n");
         BufferedReader fileReader = new BufferedReader(new InputStreamReader(InputStream.class.getResourceAsStream("/html/401.html")));
         String line = fileReader.readLine();
         do {
             writer.write(line);
             line = fileReader.readLine();
         } while (line != null);
         writer.flush();
     }
 
     private void serveView(String view) throws IOException {
         writer.write("HTTP/1.1 200 OK\n" +
                 "status: 200 OK\n" +
                 "version: HTTP/1.1\n" +
                 "content-type: text/html; charset=UTF-8\n\n");
         BufferedReader fileReader = new BufferedReader(new InputStreamReader(InputStream.class.getResourceAsStream("/html/" + view + ".html")));
         String line = fileReader.readLine();
         do {
             writer.write(line);
             line = fileReader.readLine();
         } while (line != null);
         writer.flush();
     }
 
     private void serveUserView() throws IOException {
         writer.write("HTTP/1.1 200 OK\n" +
                 "status: 200 OK\n" +
                 "version: HTTP/1.1\n" +
                 "content-type: text/html; charset=UTF-8\n\n");
         serveSnippet("user/1");
         writer.write(CalculationManager.getInstance().getCalculation().toString());
         serveSnippet("user/2");
         writer.write("<tr><td>" + HarmonicServer.getInstance().getServerInstance().getAddress().getHostAddress() +
                 ":" + HarmonicServer.getInstance().getServerInstance().getPort() + "</td></tr>");
         serveSnippet("user/2.5");
         writer.write("<tr><td>" + CalculationManager.getInstance().calculatedIntervals() + "</td></tr>");
         serveSnippet("user/3");
         for(CalculationInterval interval : CalculationManager.getInstance().pendingCalculationIntervals()) {
             writer.write("<tr><td>" + interval.getStart() + " - " + interval.getEnd() + "</tr></td>");
         }
         serveSnippet("user/4");
         writer.flush();
     }
 
     private void serveSnippet(String view) throws IOException {
         BufferedReader fileReader = new BufferedReader(new InputStreamReader(InputStream.class.getResourceAsStream("/html/" + view + ".html")));
         String line = fileReader.readLine();
         do {
             writer.write(line);
             line = fileReader.readLine();
         } while (line != null);
     }
 
     private void redirect(String uri) throws IOException {
        String url = uri + "?" +  urlParams();
         writer.write("HTTP/1.1 200 ok\n" +
                 "Refresh: 0; url=" + url + "\n" +
                 "Content-type: text/html\n\n");
         writer.write("Please follow <a href=\"" + url + "\">this link</a>.");
         writer.flush();
     }
 
     private User retrieveUser(String user, String password) {
         if (user == null || password == null)
             return null;
         User u = httpServer.getUsers().get(user);
         if (u != null && password.equals(u.getPassword())) {
             loggedUser = u;
             return u;
         }
         return null;
     }
 
     private String urlParams() {
         if (request.indexOf('?') == -1)
             return null;
         return request.substring(request.indexOf('?')+1, request.indexOf(' ', request.indexOf('?')));
     }
 
     private void parseUrlParams() {
         String parameters = urlParams();
         if (parameters != null) {
             urlParameters = new HashMap<String, String>();
             for(String param : parameters.split("&")) {
                 String split[] = param.split("=");
                 urlParameters.put(split[0], split[1]);
             }
         }
     }
 }
