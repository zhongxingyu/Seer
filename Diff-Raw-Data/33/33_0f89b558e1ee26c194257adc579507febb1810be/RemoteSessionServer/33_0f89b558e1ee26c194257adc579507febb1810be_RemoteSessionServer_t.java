 /**
  * RmiSessionServer.java
  *
  * Created on January 10, 2000, 4:47 PM
  */
  
 package org.jbundle.base.remote.rmiserver;
 
 import java.net.InetAddress;
 import java.rmi.RMISecurityManager;
 import java.rmi.RemoteException;
 import java.rmi.server.UnicastRemoteObject;
 import java.util.Hashtable;
 import java.util.Map;
 
 import javax.naming.Context;
 import javax.naming.InitialContext;
 import javax.naming.NamingException;
 
 import org.jbundle.base.remote.db.TaskSession;
 import org.jbundle.base.util.BaseApplication;
 import org.jbundle.base.util.DBConstants;
 import org.jbundle.base.util.DBParams;
 import org.jbundle.base.util.Environment;
 import org.jbundle.base.util.MainApplication;
 import org.jbundle.base.util.Utility;
 import org.jbundle.thin.base.remote.ApplicationServer;
 import org.jbundle.thin.base.remote.RemoteTask;
 import org.jbundle.thin.base.util.Application;
 import org.jbundle.thin.base.util.Util;
 
 
 /** 
  * RmiSessionServer - The remote server.
  * You will need to start this Server with a command such as this:
  * <pre>
  * java -Djava.rmi.server.codebase=http://209.85.16.220/classes/
  * -Djava.security.policy=/data/java/tour/bin/policy.all org.jbundle.base.remote.rmiserver.RmiServerSession "local=Jdbc remote=Jdbc"
  * </pre>
  * @author  Administrator
  * @version 1.0.0
  */
 public class RemoteSessionServer extends UnicastRemoteObject
     implements ApplicationServer
 {
     private static final long serialVersionUID = 1L;
 
     /**
      * The main application associated with this server.
      */
     protected BaseApplication m_app = null;
 
     /**
      * Creates new RmiSessionServer.
      */
     public RemoteSessionServer() throws RemoteException
     {
         super();
     }
     /**
      * Creates new RmiSessionServer.
      */
     public RemoteSessionServer(BaseApplication app) throws RemoteException
     {
         this();
         this.init(app);
     }
     /**
      * Creates new RmiSessionServer.
      */
     public void init(BaseApplication app) throws RemoteException
     {
         m_app = app;
     }
     /**
      * Start up the remote server.
      * Params:
      * <br/>local=[(Client)|Jdbc|Net|Memory]  // Type of db to use for local tables
      * <br/>remote=[(Client)|Jdbc|Net|Memory] // Type of db to use for remote tables
      * <br/>table=[Client|Jdbc|(Net)|Memory]  // Type of db to use for table tables
      * <br/>freeifdone=[(true)|false]         // When there are no apps, shut down?
      * <br/>messagefilter=[(sequential)|tree] // Type of message filter
      * <br/>provider=[serverurl][:serverport] // remote RMI Server to hook to (defaults to localhost:1099)
      * <br/>providerapp=[appname]             // remote RMI app server name (defaults to appserver) (msgapp for the messageapp)
      * <br/>lockprovider=[appname]            // remote RMI lock server name (defaults to lockserver)
      * <br/>remoteapp=[appname]               // My RMI app name (clients contact me with this app name)
      * <br/>jmsserver=[true|(false)]          // Am I the XML web server?
      */
     public static void main(String[] args)
     {
     	RemoteSessionServer.startup(args);
     }
     /**
      * Start up the remote server.
      * @param properties The properties to use on setup.
      */
     public static RemoteSessionServer startupServer(Map<String,Object> properties)
     {
     	RemoteSessionServer remoteServer = null;
         Utility.getLogger().info("Starting rmi server");
 
         // create a registry if one is not running already. (GET RID OF THIS!)
         try {
             String strServer = (String)properties.get(DBParams.PROVIDER);
             int iRmiPort = java.rmi.registry.Registry.REGISTRY_PORT;  //(1099);
             try {
                 if (strServer != null)
                     if (strServer.indexOf(':') != -1)
                         iRmiPort = Integer.parseInt(strServer.substring(strServer.indexOf(':') + 1));
             } catch (NumberFormatException ex)   {
             }
             java.rmi.registry.LocateRegistry.createRegistry(iRmiPort);
             Utility.getLogger().info("Starting rmi registry server");
         } catch (java.rmi.server.ExportException ee) {
             // registry already exists, we'll just use it.
         } catch (RemoteException re) {
             System.err.println(re.getMessage());
             re.printStackTrace();
         }
 
  // Create and install a security manager
         if (System.getSecurityManager() == null)
         {
             System.setSecurityManager(new RMISecurityManager());
         }
 
         try {
             remoteServer = new RemoteSessionServer(null);
 
             String strAppName = (String)properties.get(DBParams.PROVIDER_APP);
             if ((strAppName == null) || (strAppName.length() == 0))
                 strAppName = DBParams.DEFAULT_REMOTE_APP;
             String strServer = (String)properties.get(DBParams.PROVIDER);
             if ((strServer == null) || (strServer.length() == 0))
             {
                 try   {
                     strServer = InetAddress.getLocalHost().getHostName();
                 } catch (Exception ex)  {
                     strServer = "localhost";
                 }
             }
 
             Hashtable<String,String> contextEnv = new Hashtable<String,String>();
             contextEnv.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.rmi.registry.RegistryContextFactory");
             contextEnv.put(Context.PROVIDER_URL, "rmi://" + strServer);   // + ":1099");    // The RMI server port
             Context initial = new InitialContext(contextEnv);
             // Bind this object instance to the server app name.
             initial.rebind(strAppName, remoteServer);
 
             Utility.getLogger().info(strAppName + " bound in registry on " + strServer);
         } catch (RemoteException ex)    {
             ex.printStackTrace();
         } catch (NamingException ex)    {
            //x ex.printStackTrace();
            System.out.println("RemoteSessionServer could not start as an rmi server, starting as a standalone service");
            Utility.getLogger().info("RemoteSessionServer could not start as an rmi server, starting as a standalone service");
         }
         return remoteServer;
     }
 
     /**
      * Start up the remote server.
      * Params:
      * <br/>local=[(Client)|Jdbc|Net|Memory]  // Type of db to use for local tables
      * <br/>remote=[(Client)|Jdbc|Net|Memory] // Type of db to use for remote tables
      * <br/>table=[Client|Jdbc|(Net)|Memory]  // Type of db to use for table tables
      * <br/>freeifdone=[(true)|false]         // When there are no apps, shut down?
      * <br/>messagefilter=[(sequential)|tree] // Type of message filter
      * <br/>provider=[serverurl][:serverport] // remote RMI Server to hook to (defaults to localhost:1099)
      * <br/>providerapp=[appname]             // remote RMI app server name (defaults to appserver) (msgapp for the messageapp)
      * <br/>lockprovider=[appname]            // remote RMI lock server name (defaults to lockserver)
      * <br/>remoteapp=[appname]               // My RMI app name (clients contact me with this app name)
      * <br/>jmsserver=[true|(false)]          // Am I the XML web server?
      */
     public static RemoteSessionServer startup(String[] args)
     {
         Map<String,Object> propertiesTemp = new Hashtable<String,Object>();
         Util.parseArgs(propertiesTemp, args);
         if (propertiesTemp.get(DBParams.FREEIFDONE) == null)
             propertiesTemp.put(DBParams.FREEIFDONE, DBConstants.FALSE);   // Don't free when only the last app is running.
         if (propertiesTemp.get(DBParams.LOCAL) == null)
             propertiesTemp.put(DBParams.LOCAL, DBParams.JDBC); // Remote Server ALWAYS uses Jdbc
         if (propertiesTemp.get(DBParams.REMOTE) == null)
             propertiesTemp.put(DBParams.REMOTE, DBParams.JDBC);
         if (propertiesTemp.get(DBParams.TABLE) == null)
             propertiesTemp.put(DBParams.TABLE, DBParams.JDBC);
         if (propertiesTemp.get(DBParams.MESSAGE_FILTER) == null)
             propertiesTemp.put(DBParams.MESSAGE_FILTER, DBParams.TREE_FILTER);  // Default for a server
 
         RemoteSessionServer remoteServer = RemoteSessionServer.startupServer(propertiesTemp);
 
         Environment env = new Environment(propertiesTemp);
         // Note the order that I do this... this is because MainApplication may need access to the remoteapp during initialization
         BaseApplication app = new MainApplication();
         remoteServer.setApp(app);
         Map<String,Object> properties = null;
         if (args != null)
         {
             properties = new Hashtable<String,Object>();
             Util.parseArgs(properties, args);
         }
         app.init(env, properties, null); // Default application (with params).
 
         return remoteServer;
     }
     /**
      * 
      */
     public void setApp(BaseApplication app)
     {
         m_app = app;
     }
     /**
      * Build a new remote session and initialize it.
      * @return The remote Task.
      */
     public RemoteTask createRemoteTask(Map<String, Object> properties) throws RemoteException
     {
         Utility.getLogger().info("createRemoteTask()");
         RemoteTask remoteTask = this.getNewRemoteTask(properties);
         return remoteTask;
     }
     /**
      * Get a remote session from the pool and initialize it.
      * If the pool is empty, create a new remote session.
      * @param strUserID The user name/ID of the user, or null if unknown.
      * @return The remote Task.
      */
     public RemoteTask getNewRemoteTask(Map<String,Object> properties)
     {
         try   {
             Application app = new MainApplication(m_app.getEnvironment(), properties, null);
             RemoteTask remoteServer = new TaskSession(app);
             ((TaskSession)remoteServer).setProperties(properties);
             return remoteServer;
         } catch (Exception ex) {
             ex.printStackTrace();
             return null;
         }
     }
     /**
      * Shutdown the server.
      */
     public void shutdown()
     {
         Environment env = m_app.getEnvironment();
         if (env.getApplicationCount() > 1)
         {
             Utility.getLogger().warning("Active sessions being freed.");
         }
         env.free();
 //x        System.exit(0);
     }
 }
