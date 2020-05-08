 /*
 * Copyright (C) 2011 Rodrigo Pinheiro Marques de Araujo
 *
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc., 51
 * Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 */
 package midgard.web;
 
 import com.sun.spot.io.j2me.radiogram.RadiogramConnection;
 import java.io.IOException;
 import java.util.Vector;
 import javax.microedition.io.Connector;
 import javax.microedition.io.Datagram;
 import midgard.app.IAppRepositoryManager;
 import midgard.components.IComponentManager;
 import midgard.events.IEvent;
import midgard.kernel.Debug;
 import midgard.services.Service;
 
 /**
  *
  * @author fenrrir
  */
 public class DefaultWebServer extends Service implements IWebServer {
 
     private RadiogramConnection conn;
     private IHTTPServer server;
     private Datagram input, output;
     private IComponentManager componentManager;
     private IAppRepositoryManager appRepositoryManager;
     private Vector webApplications;
     private boolean isRunning = false;
     private Thread thread;
 
     public String[] getRequiredInterfaces() {
         return new String[]{
                     IHTTPServer.class.getName(),
                     IComponentManager.class.getName(),
                     IAppRepositoryManager.class.getName()
                 };
     }
 
     public void initialize() {
         super.initialize();
         server = (IHTTPServer) getConnectedComponents().get(IHTTPServer.class.getName());
 
         componentManager = (IComponentManager) getConnectedComponents().get(IComponentManager.class.getName());
 
         appRepositoryManager = (IAppRepositoryManager) getConnectedComponents().get(IAppRepositoryManager.class.getName());
     }
 
     public void startService() {
         super.startService();
 
         String webAppName;
         IWebApplication webApp;
 
         server.registerEventListener(this);
 
         //System.err.println("WebServer Starting...");
         Vector userWebApplications = appRepositoryManager.listWebApplications();
         for (int i = 0; i < userWebApplications.size(); i++) {
             webAppName = (String) userWebApplications.elementAt(i);
             webApp = (IWebApplication) componentManager.resolveComponent(webAppName);
             if (webApp != null) {
                 addWebApplication(webApp);
             }
         }
 
 
 
         isRunning = true;
         thread = new Thread(this);
         thread.start();
     }
 
     public void stopService() {
         super.stopService();
         isRunning = false;
         server.removeEventListener(this);
 
         String uri;
         IWebApplication webApp;
         Vector URIs;
 
         for (int i = 0; i < webApplications.size(); i++) {
             webApp = (IWebApplication) webApplications.elementAt(i);
             URIs = webApp.getURIs();
             for (int j = 0; j < URIs.size(); j++) {
                 uri = (String) URIs.elementAt(j);
                 server.removeViewWithURI(uri);
             }
 
         }
 
 
         componentManager.destroyComponent(server);
 
         thread.interrupt();
 
         isRunning = false;
         server = null;
     }
 
     public void newEventArrived(IEvent event) {
         //System.err.println("@@@@@@@@@@@@@@@@@@Event class " + event.getClass().getName());
         super.newEventArrived(event);
         fireEvent(event);
     }
 
     public void run() {
         try {
             handleConnections();
         } catch (IOException ex) {
             ex.printStackTrace();
         }
     }
 
     public void addWebApplication(IWebApplication application) {
         String uri;
         Vector appURIs = application.getURIs();
 
 
         //System.err.println("Webserver add application "
         //        + application.getClass().getName());
 
         for (int i = 0; i < appURIs.size(); i++) {
             uri = (String) appURIs.elementAt(i);
             server.addView(new URLView(uri, application));
         }
     }
 
     public Vector listWebApplications() {
         return webApplications;
     }
 
     public void removeWebApplication(IWebApplication application) {
         String uri;
         Vector appURIs = application.getURIs();
         for (int i = 0; i < appURIs.size(); i++) {
             uri = (String) appURIs.elementAt(i);
             server.removeViewWithURI(uri);
         }
     }
 
     private void handleConnections() throws IOException {
 
 
         while (isRunning) {
             try {
 
                 conn = (RadiogramConnection) Connector.open("radiogram://:80");
                 input = conn.newDatagram(conn.getMaximumLength());
                 output = conn.newDatagram(conn.getMaximumLength());
 
                 //System.err.println("Webserver listening");
                 conn.receive(input);
                 output.reset();
                 output.setAddress(input);
                 //System.err.println("Webserver received request " + input.getAddress());
 
                 try{
                     server.handleRequest(input, output);
                     //System.err.println("Webserver process request " + output.readUTF());
                     conn.send(output);
                     //System.err.println("Webserver send request");
                 }catch (NotRequestException e){}
 
 
 
 
             } catch (IOException e) {
                 e.printStackTrace();
             } finally {
                 conn.close();
             }
         }
     }
 }
