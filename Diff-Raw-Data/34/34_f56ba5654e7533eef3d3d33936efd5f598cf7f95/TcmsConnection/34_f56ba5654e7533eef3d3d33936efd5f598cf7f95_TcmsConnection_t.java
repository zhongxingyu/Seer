 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package com.redhat.nitrate;
 
 import com.redhat.nitrate.command.Auth;
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.lang.reflect.Field;
 import java.net.HttpURLConnection;
 import java.net.MalformedURLException;
 import java.net.ProtocolException;
 import java.net.URL;
 import java.util.*;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import redstone.xmlrpc.XmlRpcClient;
 import redstone.xmlrpc.XmlRpcException;
 import redstone.xmlrpc.XmlRpcFault;
 import redstone.xmlrpc.XmlRpcStruct;
 import sun.misc.BASE64Encoder;
 
 /**
  *
  * @author asaleh
  */
 public class TcmsConnection {
 
     private transient XmlRpcClient client;
     private String session;
     private URL url;
     private TcmsAccessCredentials credentials;
 
     // TODO: [refactor] maybe change from boolean to void and just throw exception
    public boolean testTcmsConnection() throws IOException, TcmsException {
         HttpURLConnection connection = null;
         BufferedReader rd = null;
         StringBuilder sb = null;
         String line = null;
         boolean result = false;
 
         //Set up the initial connection
         connection = (HttpURLConnection) url.openConnection();
         connection.setRequestMethod("GET");
         connection.setDoOutput(true);
         /**
          * FIXME: decide what timeout is most reasonable
          */
         connection.setReadTimeout(50000);
 
         ///Set basic auth
         if (!credentials.isEmpty()) {
             connection.setRequestProperty("Authorization", basicAuthString(credentials.getUsername(), credentials.getPassword()));
         }
 
         connection.connect();
 
         if (connection.getResponseCode() == 401) {
            throw new TcmsException("Server returned HTTP 401 Unauthorized. Please check username and password.");
         }
 
         //read the result from the server
         rd = new BufferedReader(new InputStreamReader(connection.getInputStream()));
         sb = new StringBuilder();
         while ((line = rd.readLine()) != null) {
             sb.append(line + '\n');
         }
         if (sb.lastIndexOf("XML-RPC Service") > 0) {
             result = true;
         }
         //close the connection, set all objects to null
         connection.disconnect();
 
         rd = null;
         sb = null;
         connection = null;
         return result;
 
     }
 
     public TcmsConnection(String server_url) throws MalformedURLException {
         this(new URL(server_url));
     }
 
     public TcmsConnection(URL server_url) {
         client = new XmlRpcClient(server_url, false);
         url = server_url;
         credentials = new TcmsAccessCredentials();
 
     }
 
     public void setSession(String session) {
         client.setRequestProperty("Cookie", "sessionid=".concat(session));
         this.session = session;
     }
 
     private String basicAuthString(String username, String password) {
         BASE64Encoder enc = new sun.misc.BASE64Encoder();
         String userpassword = username + ":" + password;
         String encodedAuthorization = enc.encode(userpassword.getBytes());
         return "Basic " + encodedAuthorization;
     }
 
     public void setUsernameAndPassword(String username, String password) {
         credentials.setUsername(username);
         credentials.setPassword(password);
         client.setRequestProperty("Authorization", basicAuthString(username, password));
     }
 
     public static <T extends Object> T rpcStructToFields(XmlRpcStruct data, Class<T> c) {
         Object object = null;
         try {
             object = c.newInstance();
 
 
             Field[] fields = c.getFields();
             for (Field field : fields) {
                 String name = getName(field);
                 if (data.containsKey(name)) {
                     Object value = data.get(name);
                     // fix string/integer abiguity
                     if ((value instanceof String) && (field.getType() == Integer.class)) {
                         String s = (String) value;
                         if (s.trim().length() == 0) {
                             value = null;
                         } else {
                             value = Integer.getInteger((String) value);
                         }
                     }
                     field.set(object, value); //object.field=value;
                 }
             }
         } catch (InstantiationException ex) {
             Logger.getLogger(TcmsConnection.class.getName()).log(Level.SEVERE, null, ex);
         } catch (IllegalAccessException ex) {
             Logger.getLogger(TcmsConnection.class.getName()).log(Level.SEVERE, null, ex);
         }
         return (T) object;
     }
 
     public static Hashtable<String, Object> fieldsToHashtable(Object object) {
         Hashtable<String, Object> data = new Hashtable<String, Object>();
 
         Field[] fields = object.getClass().getFields();
         for (Field field : fields) {
             String name = getName(field);
             Object value = null;
             try {
                 value = field.get(object);
             } catch (IllegalArgumentException ex) {
                 Logger.getLogger(TcmsConnection.class.getName()).log(Level.SEVERE, null, ex);
             } catch (IllegalAccessException ex) {
                 Logger.getLogger(TcmsConnection.class.getName()).log(Level.SEVERE, null, ex);
             }
             if (value != null) {
                 data.put(name, value);
             }
         }
         return data;
     }
 
     static String commandToString(TcmsCommand aThis) throws IllegalAccessException {
         String out = "";
         Collection c = (Collection) commandToParams(aThis);
         for (Object o : c) {
             if (o instanceof Hashtable) {
                 Hashtable<String, Object> ht = (Hashtable<String, Object>) o;
                 for (String key : ht.keySet()) {
                     out = out + "," + key + ":" + ht.get(key).toString();
                 }
             } else {
                 out = out + "," + o.toString();
             }
         }
         return out;
     }
 
     static String getName(Field field) {
         String s = field.getName();
         if (s.contains("caseVar")) {
             s = "case";
         }
         return s;
     }
 
     static List<Object> fieldsToCollection(Object object) {
         ArrayList data = new ArrayList();
 
         Field[] fields = object.getClass().getFields();
         for (Field field : fields) {
             String name = getName(field);
             Object value = null;
             try {
                 value = field.get(object);
             } catch (IllegalArgumentException ex) {
                 Logger.getLogger(TcmsConnection.class.getName()).log(Level.SEVERE, null, ex);
             } catch (IllegalAccessException ex) {
                 Logger.getLogger(TcmsConnection.class.getName()).log(Level.SEVERE, null, ex);
             }
 
             if (TcmsArrayCommand.class.isInstance(value)) {
                 value = fieldsToCollection(value);
             } else if (TcmsHashCommand.class.isInstance(value)) {
                 value = fieldsToHashtable(value);
             }
 
             if (value != null) {
                 data.add(value);
             }
         }
         return data;
     }
 
     public static List commandToParams(TcmsCommand cmd) {
         Field[] fields = cmd.getClass().getFields();
 
         if (TcmsArrayCommand.class.isInstance(cmd)) {
             return fieldsToCollection(cmd);
         } else if (TcmsHashCommand.class.isInstance(cmd)) {
             Hashtable<String, Object> data = fieldsToHashtable(cmd);
             Vector params = new Vector();
             params.add(data);
             return params;
         }
         return null;
     }
 
     /**
      * Invokes given command.
      *
      * As a side effect, also does some exception handling: instead of default
      * XmlRpcFault messages, more useful messages are provided and are thrown as
      * TcmsException. This simplifies exception handling across other classes.
      * Author: jrusnack
      *
      * @param cmd
      * @return
      * @throws TcmsException
      */
     public Object invoke(TcmsCommand cmd) throws TcmsException {
         try {
             List params = commandToParams(cmd);
             Object o = client.invoke(cmd.name(), params);
             return o;
             
         } catch (XmlRpcException ex) {
             Logger.getLogger(TcmsConnection.class.getName()).log(Level.SEVERE, null, ex);
 
             /*
              * This exception usually means Unauthorized. Subsitute with more
              * useful message
              */
             if (ex.getMessage().equals("The response could not be parsed.")) {
                 throw new TcmsException("Possibly wrong username/password");
             }
 
             /*
              * Subsitute with more useful message
              */
             if (ex.getMessage().equals("A network error occurred.")) {
                 throw new TcmsException("Cannot connect to server. Check URL or try reloading this page");
             }
             throw ex;
         } catch (XmlRpcFault ex){
             Logger.getLogger(TcmsConnection.class.getName()).log(Level.SEVERE, null, ex);
             throw new TcmsException(ex.getMessage());
         }
     }
 
    public static TcmsConnection connect(String serverUrl, TcmsAccessCredentials credentials) throws TcmsException  {
         TcmsConnection connection = null;
        try{
            connection = new TcmsConnection(serverUrl);
        } catch (MalformedURLException ex){
            throw new TcmsException("URL is malformed");
        }
         connection.setUsernameAndPassword(credentials.getUsername(), credentials.getPassword());
 
         Auth.login_krbv auth = new Auth.login_krbv();
         String session;
         session = auth.invoke(connection);
         if (session != null && session.length() > 0) {
             connection.setSession(session);
         } else {
            throw new TcmsException("Couln't connect to tcms server");
         }
         return connection;
     }
 }
