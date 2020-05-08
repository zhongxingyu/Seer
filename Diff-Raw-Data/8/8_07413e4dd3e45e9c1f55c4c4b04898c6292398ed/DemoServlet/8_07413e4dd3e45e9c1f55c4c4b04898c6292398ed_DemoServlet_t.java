 /*
  * Copyright (c) 2004 UNINETT FAS
  *
  * This program is free software; you can redistribute it and/or modify it
  * under the terms of the GNU General Public License as published by the Free
  * Software Foundation; either version 2 of the License, or (at your option)
  * any later version.
  *
  * This program is distributed in the hope that it will be useful, but WITHOUT
  * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
  * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for
  * more details.
  *
  * You should have received a copy of the GNU General Public License along with
  * this program; if not, write to the Free Software Foundation, Inc., 59 Temple
  * Place - Suite 330, Boston, MA 02111-1307, USA.
  *
  * $Id$
  */
 
 package no.feide.mellon.demo;
 
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import no.feide.moria.webservices.v2_1.Attribute;
 import no.feide.moria.webservices.v2_1.AuthenticationSoapBindingStub;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.IOException;
 import java.io.PrintWriter;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.rmi.RemoteException;
 import java.util.ArrayList;
 import java.util.Properties;
 import java.util.StringTokenizer;
 
 /**
  * This is a simple demonstration servlet, primarily intended as a code example
  * of how to access the Moria SOAP interface. <br>
  * <br>
  * This implementation uses the Mellon2 API. <br>
  * <br>
  * The servlet works as follows: <br>
  * <ol>
  * <li>If the HTTP request does not contain a service ticket (found by checking
  * URL parameter <code>PARAM_TICKET</code>) the user is redirected to a Moria
  * instance (given by <code>SERVICE_ENDPOINT</code>) for authentication. <br>
  * This is done using the <code>initiateAuthentication(...)</code> method to
  * receive a correct redirect URL to the Moria instance.
  * <li>Once the user has gone through a successful authentication, he or she is
  * redirected back to this servlet, now with the previously missing service
  * ticket.
  * <li>The servlet then attempts to read the attributes requested by the
  * earlier use of <code>initiateAuthentication(...)</code>, by using
  * <code>getUserAttributes(...)</code> with the given service ticket.
  * <li>If successful, the attributes and their values are then displayed.
  * </ol>
  * A few other points to note:
  * <ul>
  * <li>The attributes requested are given by <code>ATTRIBUTE_REQUEST</code>.
  * <li>This servlet, as a Moria client service, authenticates itself to Moria
  * using the username/password combination given by <code>CLIENT_USERNAME</code>
  * and <code>CLIENT_PASSWORD</code>.
  * <li>The Moria service instance used is given by
  * <code>SERVICE_ENDPOINT</code>.
  * </ul>
 * @see no.feide.moria.webservices.v2_1.AuthenticationSoapBindingStub#initiateAuthentication(String[],
 *      String, String, boolean)
 * @see no.feide.moria.webservices.v2_1.AuthenticationSoapBindingStub#getUserAttributes(String)
  */
 public class DemoServlet
 extends HttpServlet {
 
     /**
      * The system property giving the configuration file name for the Demo
      * servlet. <br>
      * <br>
      * Current value is <code>"no.feide.mellon.demo.config"</code>.
      */
     private static final String CONFIG_FILENAME = "no.feide.mellon.demo.config";
 
     /**
      * The service endpoint. <br>
      * <br>
      * Current value is <code>"no.feide.mellon.demo.serviceEndpoint"</code>.
      */
     private static final String CONFIG_SERVICE_ENDPOINT = "no.feide.mellon.demo.serviceEndpoint";
 
     /**
      * A comma-separated list of attributes requested by the main service. <br>
      * <br>
      * Current value is
      * <code>"no.feide.mellon.demo.master.attributeRequest"</code>.
      */
     private static final String CONFIG_MASTER_ATTRIBUTE_REQUEST = "no.feide.mellon.demo.master.attributeRequest";
 
     /**
      * The username used by DemoServlet to access Moria2 as a main service. <br>
      * <br>
      * Current value is <code>"no.feide.mellon.demo.master.username"</code>.
      */
     private static final String CONFIG_MASTER_USERNAME = "no.feide.mellon.demo.master.username";
 
     /**
      * The password used by DemoServlet to access Moria2 as a main service. <br>
      * <br>
      * Current value is <code>"no.feide.mellon.demo.master.password"</code>.
      */
     private static final String CONFIG_MASTER_PASSWORD = "no.feide.mellon.demo.master.password";
 
     /**
      * A comma-separated list of attributes requested by the subservice. <br>
      * <br>
      * Current value is
      * <code>"no.feide.mellon.demo.slave.attributeRequest"</code>.
      */
     private static final String CONFIG_SLAVE_ATTRIBUTE_REQUEST = "no.feide.mellon.demo.slave.attributeRequest";
 
     /**
      * The username used to access Moria2 as a subservice. <br>
      * <br>
      * Current value is <code>"no.feide.mellon.demo.slave.username"</code>.
      */
     private static final String CONFIG_SLAVE_USERNAME = "no.feide.mellon.demo.slave.username";
 
     /**
      * The password used to access Moria2 as a subservice. <br>
      * <br>
      * Current value is <code>"no.feide.mellon.demo.slave.password"</code>.
      */
     private static final String CONFIG_SLAVE_PASSWORD = "no.feide.mellon.demo.slave.password";
 
     /**
      * The URL that the user should be redirected to in order to complete
      * logout. <br>
      * <br>
      * Current value is <code>"no.feide.mellon.demo.logout.url"</code>.
      */
     private static final String CONFIG_LOGOUT_URL = "no.feide.mellon.demo.logout.url";
 
     /**
      * The truststore filename used when accepting Moria's certificate. If not
      * set, no custom truststore will be used. <br>
      * <br>
      * Current value is <code>"no.feide.mellon.demo.trustStore"</code>.
      */
     private static final String CONFIG_TRUSTSTORE = "no.feide.mellon.demo.trustStore";
 
     /**
      * The truststore password used in conjunction with
      * <code>CONFIG_TRUSTSTORE</code>.<br>
      * <br>
      * Current value is <code>"no.feide.mellon.demo.trustStorePassword"</code>.
      */
     private static final String CONFIG_TRUSTSTORE_PASSWORD = "no.feide.mellon.demo.trustStorePassword";
 
     /**
      * Required parameters.
      */
     private static final String[] REQUIRED_PARAMETERS = {CONFIG_SERVICE_ENDPOINT, CONFIG_MASTER_USERNAME, CONFIG_MASTER_PASSWORD, CONFIG_MASTER_ATTRIBUTE_REQUEST, CONFIG_SLAVE_USERNAME, CONFIG_SLAVE_PASSWORD, CONFIG_SLAVE_ATTRIBUTE_REQUEST, CONFIG_LOGOUT_URL};
 
     /**
      * Name of the URL parameter used to retrieve the Moria service ticket. <br>
      * <br>
      * Current value is <code>"ticket"</code>.
      */
     private static final String PARAM_TICKET = "ticket";
 
 
     /**
      * Initialization. Will set the truststore used by the servlet when trusting
      * the Moria instance's certificate, if it is not covered by the default
      * truststore.
      * @throws ServletException
      *             Never.
      */
     public void init() throws ServletException {
 
         // Set the truststore.
         final Properties config = getConfig();
         System.setProperty("javax.net.ssl.trustStore", config.getProperty(CONFIG_TRUSTSTORE));
         System.setProperty("javax.net.ssl.trustStorePassword", config.getProperty(CONFIG_TRUSTSTORE_PASSWORD));
 
     }
 
 
     /**
      * Handles the GET requests.
      * @param request
      *            The HTTP request object. If it contains a request parameter
      *            <i>moriaID </i>, the request's attribute <i>attributes </i>
      *            will be filled with the attributes contained in the session
      *            given by <i>moriaID </i>.
      * @param response
      *            The HTTP response object.
      * @throws java.io.IOException
      *             If an input or output error is detected when the servlet
      *             handles the GET request.
      * @throws javax.servlet.ServletException
      *             If the request for the GET could not be handled.
      * @see javax.servlet.http.HttpServlet#doGet(javax.servlet.http.HttpServletRequest,
      *      javax.servlet.http.HttpServletResponse)
      */
     public final void doGet(final HttpServletRequest request, final HttpServletResponse response)
     throws IOException, ServletException {
 
         // Be sure to dump all exceptions.
         try {
 
             // Get configuration.
             final Properties config = getConfig();
 
             // Handle logout request.
             if (request.getParameter("logout") != null) {
 
                 // Redirect to the configured logout URL.
                 response.sendRedirect(config.getProperty(CONFIG_LOGOUT_URL));
             }
 
             // Prepare API.
             AuthenticationSoapBindingStub service = new AuthenticationSoapBindingStub(new URL(config.getProperty(CONFIG_SERVICE_ENDPOINT)), null);
             service.setUsername(config.getProperty(CONFIG_MASTER_USERNAME));
             service.setPassword(config.getProperty(CONFIG_MASTER_PASSWORD));
 
             // Do we have a ticket?
             final String ticket = request.getParameter(PARAM_TICKET);
             if (ticket == null) {
 
                 // No ticket; redirect for authentication.
                 String redirectURL = service.initiateAuthentication(convert(config.getProperty(CONFIG_MASTER_ATTRIBUTE_REQUEST)), request.getRequestURL().toString() + "?" + PARAM_TICKET + "=", "", true);
                 response.sendRedirect(redirectURL);
 
             } else {
 
                 // We have a ticket.
                 response.setContentType("text/html");
                 PrintWriter out = response.getWriter();
                 out.println("<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.01" + "Transitional//EN\">\n");
                 out.println("<html><head><title>Moria Demo Service</title></head><body>");
                 out.println("<h1 align=\"center\">Authentication successful</h1>");
                 out.println("<p align=\"center\"><a href=\"" + config.getProperty(CONFIG_LOGOUT_URL) + "\">Logout</a></p>");
                 out.println("<i>System '" + config.getProperty(CONFIG_MASTER_USERNAME) + "':</i>");
                 String ticketGrantingTicket = null; // For later use.
 
                 // Get and display attributes.
                 // TODO: Catch exceptions here.
                 Attribute[] attributes = service.getUserAttributes(ticket);
                 out.println("<table align=\"center\"><tr><td><b>Attribute Name</b></td><td><b>Attribute Value(s)</b></td></tr>");
                 for (int i = 0; i < attributes.length; i++) {
 
                     // Is this the ticket granting ticket?
                     String name = attributes[i].getName();
                     if (name.equals("tgt")) {
 
                         // Just remember the ticket for later use.
                         ticketGrantingTicket = attributes[i].getValues()[0];
 
                     } else {
 
                         // Show the actual attribute and its values.
                         out.println("<tr><td>" + name + "</td>");
                         String[] values = attributes[i].getValues();
                         for (int j = 0; j < values.length; j++) {
                             if (j > 0)
                                 out.println("<tr><td></td>");
                             out.println("<td>" + values[j] + "</td></tr>");
                         }
 
                     }
 
                 }
                 out.println("</table>");
                 out.println("<p><b>Ticket granting ticket:</b> <tt>" + ticketGrantingTicket + "</tt></p>");
 
                 // Should we try to fake a subsystem using SSO?
                 out.println("<hr><i>Subsystem '" + config.getProperty(CONFIG_SLAVE_USERNAME) + "':</i>");
                 if (ticketGrantingTicket == null) {
 
                     out.println("<p align=\"center\">No ticket granting ticket was retrieved.<br>SSO denied.</p>");
 
                 } else {
 
                     // Now get a proxy ticket for your subsystem (we're actually
                     // our own subsystem, to keep the code simple).
                     final String proxyTicket = service.getProxyTicket(ticketGrantingTicket, config.getProperty(CONFIG_SLAVE_USERNAME));
 
                     // We now have a proxy ticket; now let's fake our own
                     // subsystem. Retrieve and display some attributes.
                     AuthenticationSoapBindingStub subservice = new AuthenticationSoapBindingStub(new URL(config.getProperty(CONFIG_SERVICE_ENDPOINT)), null);
                     subservice.setUsername(config.getProperty(CONFIG_SLAVE_USERNAME));
                     subservice.setPassword(config.getProperty(CONFIG_SLAVE_PASSWORD));
                     attributes = subservice.proxyAuthentication(convert(config.getProperty(CONFIG_SLAVE_ATTRIBUTE_REQUEST)), proxyTicket);
                     out.println("<table align=\"center\"><tr><td><b>Attribute Name</b></td><td><b>Attribute Value(s)</b></td></tr>");
                     for (int i = 0; i < attributes.length; i++) {
 
                         // Show the actual attribute and its values.
                         String name = attributes[i].getName();
                         out.println("<tr><td>" + name + "</td>");
                         String[] values = attributes[i].getValues();
                         for (int j = 0; j < values.length; j++) {
                             if (j > 0)
                                 out.println("<tr><td></td>");
                             out.println("<td>" + values[j] + "</td></tr>");
                         }
 
                     }
                     out.println("</table>");
                     out.println("<p><b>Proxy ticket:</b> <tt>" + proxyTicket + "</tt></p>");
 
                 }
 
                 // We're done!
                 out.println("</html></body>");
 
             }
 
         } catch (RemoteException e) {
             System.err.println("RemoteException caugth: " + e);
             throw new ServletException(e);
         } catch (MalformedURLException e) {
             System.err.println("MalformedURLException caught: " + e);
             throw new ServletException(e);
         }
 
     }
 
 
     /**
      * Read configuration from the file given by <code>CONFIG_FILENAME</code>
      * and check that all required properties are given.
      * @throws IllegalStateException
      *             If the required property <code>CONFIG_FILENAME</code> is
      *             not set, or if the file given by this property could not be
      *             read.
     * @return Configuration properties from file.
      * @see #REQUIRED_PARAMETERS
      */
     private Properties getConfig() throws IllegalStateException {
 
         // Read properties from file.
         final String configFile = System.getProperty(CONFIG_FILENAME);
         if (configFile == null) { throw new IllegalStateException("Required base property '" + CONFIG_FILENAME + "' not set"); }
         Properties config = new Properties();
         try {
             config.load(new FileInputStream(new File(configFile)));
         } catch (IOException e) {
             throw new IllegalStateException("Unable to read configuration from " + configFile);
         }
 
         // Are we missing some required properties?
         for (int i = 0; i < REQUIRED_PARAMETERS.length; i++) {
             String parvalue = config.getProperty(REQUIRED_PARAMETERS[i]);
             if ((parvalue == null) || (parvalue.equals(""))) { throw new IllegalStateException("Required parameter '" + REQUIRED_PARAMETERS[i] + "' not set in '" + configFile + "'"); }
         }
         return config;
 
     }
 
 
     /**
      * Convert a comma-separated list into an array.
      * @param commaSeparatedList
      *            A comma-separated list of elements. May be <code>null</code>
      *            or an empty string.
      * @return <code>commaSeparatedList</code> as a string array. Will always
      *         return an empty array if <code>commaSeparatedList</code> is
      *         <code>null</code> or an empty string.
      */
     private String[] convert(final String commaSeparatedList) {
 
         // Sanity checks.
         if ((commaSeparatedList == null) || (commaSeparatedList.length() == 0))
             return new String[] {};
 
         // Convert and return.
         ArrayList buffer = new ArrayList();
         StringTokenizer tokenizer = new StringTokenizer(commaSeparatedList, ",");
         while (tokenizer.hasMoreTokens())
             buffer.add(tokenizer.nextToken());
         return (String[]) buffer.toArray(new String[] {});
 
     }
 
 }
