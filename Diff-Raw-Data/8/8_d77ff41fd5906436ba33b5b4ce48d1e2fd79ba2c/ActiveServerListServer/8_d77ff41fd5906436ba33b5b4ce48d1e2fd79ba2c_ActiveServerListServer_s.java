 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package dblike.server.service;
 
 import dblike.server.ActiveServer;
 import dblike.server.ServerStart;
 import dblike.service.InternetUtil;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.Vector;
 import javax.xml.parsers.DocumentBuilder;
 import javax.xml.parsers.DocumentBuilderFactory;
 import javax.xml.parsers.ParserConfigurationException;
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 import org.w3c.dom.Node;
 import org.w3c.dom.NodeList;
 import org.xml.sax.SAXException;
 
 /**
  * This is to maintain the list of active servers, which is a static list. It is
  * used to keep track of all servers Also will be used by other class.
  *
  * @author wenhanwu
  */
 public class ActiveServerListServer {
 
     private static Vector<ActiveServer> ActiveServerList = new Vector<ActiveServer>();
 
     /**
      * @return the ActiveServerList
      */
     public static Vector<ActiveServer> getActiveServerList() {
         return ActiveServerList;
     }
 
     /**
      * @param aActiveServerList the ActiveServerList to set
      */
     public static void setActiveServerList(Vector<ActiveServer> aActiveServerList) {
         ActiveServerList = aActiveServerList;
     }
 
     /**
      * This is to search the server by the given IP and port, return the object.
      *
      * @param serverIP
      * @param port
      * @return
      */
     public static ActiveServer searchServerByIP_Port(String serverIP, int port) {
         for (int i = 0; i < ActiveServerList.size(); ++i) {
             if (ActiveServerList.get(i).getServerIP().equals(serverIP) && ActiveServerList.get(i).getPort() == port) {
                 return ActiveServerList.get(i);
             }
         }
         return null;
     }
 
     /**
      * This is to check if there is a server has the given IP and port, return the index.
      *
      * @param serverIP
      * @param port
      * @return
      */
     public static int checkServerByIP_Port(String serverIP, int port) {
         for (int i = 0; i < ActiveServerList.size(); ++i) {
             if (ActiveServerList.get(i).getServerIP().equals(serverIP) && ActiveServerList.get(i).getPort() == port) {
                 return i;
             }
         }
         return -1;
     }
 
     /**
      * This is to remove the server object from the list.
      *
      * @param serverIP
      * @param port
      * @return
      */
     public static boolean removeServer(String serverIP, int port) {
         if (searchServerByIP_Port(serverIP, port) == null) {
             return false;
         } else {
             ActiveServerList.remove(searchServerByIP_Port(serverIP, port));
             return true;
         }
     }
 
     /**
      * This is to add a new server object to the list.
      *
      * @param serverIP
      * @param port
      * @return
      */
     public static boolean addServer(String serverIP, int port) {
         if (searchServerByIP_Port(serverIP, port) == null) {
             String serverID = serverIP;
             ActiveServerList.add(new ActiveServer(serverID, serverIP, port));
             System.out.println("Server [" + " " + serverIP + ":" + port + "] Added~!!!");
             return true;
         } else {
             return false;
         }
     }
 
     /**
      * Do the heartbeat for the given server. Will be called by other server.
      *
      * @param serverIP
      * @param port
      * @return
      */
     public static boolean beatTheServer(String serverIP, int port) {
         int position = checkServerByIP_Port(serverIP, port);
         if (position == -1) {
             return false;
         } else {
             ActiveServerList.get(position).setStatus(InternetUtil.getOK());
             return true;
         }
     }
 
     /**
      * Load all the servers from the xml file.
      *
      * @return
      */
     public static boolean loadServerList() {
         DocumentBuilderFactory domfac = DocumentBuilderFactory.newInstance();
         try {
             DocumentBuilder dombuilder = domfac.newDocumentBuilder();
             InputStream fis = new FileInputStream("ServerCfg/serverList.xml");
             Document doc = dombuilder.parse(fis);
             Element root = doc.getDocumentElement();
             NodeList users = root.getChildNodes();
             if (users != null) {
                 for (int i = 0; i < users.getLength(); i++) {
                     Node user = users.item(i);
                     if (user.getNodeType() == Node.ELEMENT_NODE) {
                         int checker = 0;
                         String ip = "";
                         String port = "";
                         for (Node node = user.getFirstChild(); node != null; node = node.getNextSibling()) {
                             if (node.getNodeType() == Node.ELEMENT_NODE) {
                                 if (node.getNodeName().equals("IP")) {
                                     ip = node.getFirstChild().getNodeValue();
                                     ++checker;
                                 }
                                 if (node.getNodeName().equals("port")) {
                                     port = node.getFirstChild().getNodeValue();
                                     ++checker;
                                 }
                             }
                             if (checker == 2 && ip != ServerStart.getServerIP() && Integer.parseInt(port) != ServerStart.getPORT()) {
                                 String serverID = ip;
                                 addServer(ip, Integer.parseInt(port));
                                 checker = 0;
                             }
                         }
                     }
                 }
                 System.out.println("Loaded the server:");
                 for (int i = 0; i < ActiveServerList.size(); ++i) {
                     System.out.println(ActiveServerList.get(i).getServerIP() + ":" + ActiveServerList.get(i).getPort());
                 }
                 return true;
             } else {
                 return false;
             }
         } catch (ParserConfigurationException e) {
             e.printStackTrace();
             return false;
         } catch (FileNotFoundException e) {
             e.printStackTrace();
             return false;
         } catch (SAXException e) {
             e.printStackTrace();
             return false;
         } catch (IOException e) {
             e.printStackTrace();
             return false;
         }
     }
 }
