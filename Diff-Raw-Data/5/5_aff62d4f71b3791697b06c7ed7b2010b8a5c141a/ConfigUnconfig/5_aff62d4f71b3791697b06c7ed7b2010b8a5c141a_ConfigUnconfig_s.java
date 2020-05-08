 /* The contents of this file are subject to the terms
  * of the Common Development and Distribution License
  * (the License). You may not use this file except in
  * compliance with the License.
  *
  * You can obtain a copy of the License at
  * https://opensso.dev.java.net/public/CDDLv1.0.html or
  * opensso/legal/CDDLv1.0.txt
  * See the License for the specific language governing
  * permission and limitations under the License.
  *
  * When distributing Covered Code, include this CDDL
  * Header Notice in each file and include the License file
  * at opensso/legal/CDDLv1.0.txt.
  * If applicable, add the following below the CDDL Header,
  * with the fields enclosed by brackets [] replaced by
  * your own identifying information:
  * "Portions Copyrighted [year] [name of copyright owner]"
  *
 * $Id: ConfigUnconfig.java,v 1.1 2007-12-18 23:49:44 rmisra Exp $
  *
  * Copyright 2007 Sun Microsystems Inc. All Rights Reserved
  */
 
 package com.sun.identity.qatest.clientsamples;
 
 import com.gargoylesoftware.htmlunit.WebClient;
 import com.gargoylesoftware.htmlunit.html.HtmlPage;
 import com.sun.identity.qatest.common.TestConstants;
 import com.sun.identity.qatest.common.TestCommon;
 import com.sun.identity.qatest.common.webtest.DefaultTaskHandler;
 import org.testng.annotations.AfterSuite;
 import org.testng.annotations.BeforeSuite;
 import java.io.BufferedWriter;
 import java.io.FileWriter;
 import java.net.InetAddress;
 import java.util.ResourceBundle;
 import java.util.logging.Level;
 import org.mortbay.jetty.Connector;
 import org.mortbay.jetty.Server;
 import org.mortbay.jetty.nio.SelectChannelConnector;
 import org.mortbay.jetty.servlet.Context;
 import org.mortbay.jetty.servlet.ServletHolder;
 import org.mortbay.jetty.webapp.WebAppContext;
 
 /**
  * This class deploys and undeploys the client sampls war. This is
  * achieved using the embedded jetty web server.
  */
 public class ConfigUnconfig extends TestCommon {
     
     private ResourceBundle rb_client;
     private ResourceBundle rb_amconfig;
     private Server server;
     private String clientURL;
     private String baseDir;
 
     /**
      * Creates a new instance of ConfigUnconfig
      */
     public ConfigUnconfig()
     throws Exception {
         super("ConfigUnconfig");
         rb_client = ResourceBundle.getBundle("clientsamplesGlobal");
         rb_amconfig =
                 ResourceBundle.getBundle(TestConstants.TEST_PROPERTY_AMCONFIG);
         baseDir = getBaseDir() + System.getProperty("file.separator")
             + rb_amconfig.getString(TestConstants.KEY_ATT_SERVER_NAME)
             + System.getProperty("file.separator") + "built"
             + System.getProperty("file.separator") + "classes"
             + System.getProperty("file.separator");
     }
     
     /**
      * Deploy the client sampels war on jetty server and start the jetty
      * server.
      */
     @BeforeSuite(groups={"ds_ds", "ds_ds_sec", "ff_ds", "ff_ds_sec"})
     public void startServer()
     throws Exception {
         entering("startServer", null);
 
         server = new Server();
         Connector connector = new SelectChannelConnector();
 
         String deployPort = rb_client.getString("deploy_port");
         log(Level.FINE, "startServer", "Deploy port: " + deployPort);
         connector.setPort(new Integer(deployPort).intValue());
 
         InetAddress addr = InetAddress.getLocalHost();
         String hostname = addr.getCanonicalHostName();
 
         log(Level.FINE, "startServer", "Deploy host: " + hostname);
         connector.setHost(hostname);
         server.addConnector(connector);
 
         WebAppContext wac = new WebAppContext();
 
         String deployURI = rb_client.getString("deploy_uri");
         log(Level.FINE, "startServer", "Deploy URI: " + deployURI);
         wac.setContextPath(deployURI);
 
         clientURL = protocol + "://" + hostname +  ":" + deployPort + deployURI;
         log(Level.FINE, "startServer", "Client URL: " + clientURL);
 
         String warFile = rb_client.getString("war_file");
         log(Level.FINE, "startServer", "WAR File: " + warFile);
         wac.setWar(warFile);
 
         server.setHandler(wac);
         server.setStopAtShutdown(true);
 
         log(Level.FINE, "startServer",
                 "Deploying war and starting jetty server");
         server.start();
         log(Level.FINE, "startServer", "Deployed war and started jetty server");
 
         configureWAR();
         exiting("startServer");
     }
 
     /**
      * Stop the jetty server. This basically undeploys the war.
      */
     @AfterSuite(groups={"ds_ds", "ds_ds_sec", "ff_ds", "ff_ds_sec"})
     public void stopServer()
     throws Exception {
         entering("stopServer", null);
 
         log(Level.FINE, "stopServer", "Stopping jetty server");
         server.stop();
         log(Level.FINE, "stopServer", "Stopped jetty server");
 
         // Time delay required by the jetty server process to die
         Thread.sleep(30000);
 
         exiting("stopServer");
     }
 
     /**
      * This method configures the war using the client samples configurator page
      */
     private void configureWAR()
     throws Exception {
         WebClient webClient = new WebClient();
         HtmlPage page = (HtmlPage)webClient.getPage(clientURL);
         if (getHtmlPageStringIndex(page, rb_client.getString("client_txt"))
                 == -1) {
             log(Level.FINE, "configureWAR", "WAR file is not configured." +
                     " Configuring the deployed war.");
             generateConfigXML();
             DefaultTaskHandler task = new DefaultTaskHandler(baseDir +
                     "sampleconfigurator.xml");
             page = task.execute(webClient);
         } else
             log(Level.FINE, "configureWAR", "WAR file is already configured.");
     }
 
     /**
      * Generates the XML page used by webtest to configure the deployed war.
      */
     private void generateConfigXML()
     throws Exception {
         FileWriter fstream = new FileWriter(baseDir + "sampleconfigurator.xml");
         BufferedWriter out = new BufferedWriter(fstream);
 
         String debugDir = rb_client.getString("debug_dir");
         String appUser = rb_amconfig.getString(
                 TestConstants.KEY_AMC_AGENTS_APP_USERNAME);
         String appPassword = rb_amconfig.getString(
                 TestConstants.KEY_AMC_SERVICE_PASSWORD);
         String configResult = rb_client.getString("config_result");
 
         log(Level.FINEST, "generateConfigXML", "Debug dir: " + debugDir);
         log(Level.FINEST, "generateConfigXML", "App username: " + appUser);
         log(Level.FINEST, "generateConfigXML", "App password: " + appPassword);
         log(Level.FINEST, "generateConfigXML", "Config result: " +
                 configResult);
         log(Level.FINEST, "generateConfigXML", "Server protocol: " + protocol);
         log(Level.FINEST, "generateConfigXML", "Server host: " + host);
         log(Level.FINEST, "generateConfigXML", "Server port: " + port);
         log(Level.FINEST, "generateConfigXML", "Server URI: " + uri);
 
        out.write("<url href=\"" + clientURL + "/sampleconfigurator.jsp");
         out.write("\">");
         out.write(newline);
         out.write("<form name=\"clientsampleconfigurator\"");
         out.write(" buttonName=\"submit\">");
         out.write(newline);
         out.write("<input name=\"famProt\" value=\"" + protocol + "\"/>");
         out.write(newline);
         out.write("<input name=\"famHost\" value=\"" + host + "\"/>");
         out.write(newline);
         out.write("<input name=\"famPort\" value=\"" + port + "\"/>");
         out.write(newline);
         out.write("<input name=\"famDeploymenturi\" value=\"" + uri + "\"/>");
         out.write(newline);
         out.write("<input name=\"debugDir\" value=\"" + debugDir + "\"/>");
         out.write(newline);
         out.write("<input name=\"appUser\" value=\"" + appUser + "\"/>");
         out.write(newline);
         out.write("<input name=\"appPassword\" value=\"" + appPassword);
         out.write("\"/>");
         out.write(newline);
         out.write("<result text=\"" + configResult + "\"/>");
         out.write(newline);
         out.write("</form>");
         out.write(newline);
         out.write("</url>");
         out.write(newline);
         out.close();
     }
 }
