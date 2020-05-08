 package com.epita.mti.plic.opensource.controlibserversample;
 
 import com.epita.mti.plic.opensource.controlibserver.connection.ConnectionManager;
 import com.epita.mti.plic.opensource.controlibserver.jarloader.JarClassLoader;
 import com.epita.mti.plic.opensource.controlibserver.server.CLServer;
 import com.epita.mti.plic.opensource.controlibserversample.observer.JarFileObserver;
 import com.epita.mti.plic.opensource.controlibserversample.view.ServerView;
 import com.epita.mti.plic.opensource.controlibutility.plugins.CLObserver;
 import com.epita.mti.plic.opensource.controlibutility.plugins.CLObserverSend;
 import com.epita.mti.plic.opensource.controlibutility.serialization.CLSerializable;
 import com.epita.mti.plic.opensource.controlibutility.serialization.ObjectReceiver;
 import com.epita.mti.plic.opensource.controlibutility.serialization.ObjectSender;
 import java.awt.AWTException;
 import java.awt.Frame;
 import java.awt.SystemTray;
 import java.io.*;
 import java.lang.reflect.Constructor;
 import java.lang.reflect.InvocationTargetException;
 import java.net.Socket;
 import java.util.ArrayList;
 import java.util.Observer;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 /**
  *
  * @author Julien "Roulyo" Fraisse This the server used for the demonstration.
  */
 public class Server implements CLServer
 {
 
   private static ConnectionManager connectionManager = new ConnectionManager();
   private static ServerView serverView = new ServerView();
   private static Frame qrcodeView = null;
   private JarClassLoader classLoader = new JarClassLoader(this);
   private static ObjectReceiver receiver;
   private static ObjectSender sender;
   private static ServerConfiguration conf;
 
   public static ServerConfiguration getServerConfiguration()
   {
     return conf;
   }
 
   /**
    * This method update the plugins available for the server
    */
   @Override
   public void updatePlugins()
   {
     ArrayList<Class<?>> plugins = classLoader.getPlugins();
 
     receiver.clearPlugins();
     for (Class<?> plugin : plugins)
     {
       boolean isSerializable = false;
       Class<?> superClass = plugin.getSuperclass();
       
       while (superClass != null)
       {
         if (superClass == CLSerializable.class)
         {
           isSerializable = true;
           break;
         }
         else
         {
           superClass = superClass.getSuperclass();
         }
       }
       if (isSerializable)
       {
         Constructor<?> constructor;
         try
         {
           constructor = plugin.getConstructor();
           CLSerializable cls = (CLSerializable) constructor.newInstance();
          if (!ObjectReceiver.beansMap.containsKey(cls.getType()))
            ObjectReceiver.beansMap.put(cls.getType(), plugin);
          
         }
         catch (Exception ex)
         {
           Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
         }      
       }
       try
       {
         Class[] interfaces = plugin.getInterfaces();
         Observer observer = null;
         for (Class c : interfaces)
         {
           if (c == Observer.class)
           {
             Constructor<?> constructor = plugin.getConstructor();
             observer = (Observer) constructor.newInstance();
             break;
           }
           if (c == CLObserver.class)
           {
             Constructor<?> constructor = plugin.getConstructor();
             observer = (CLObserver) constructor.newInstance();
             break;
           }
           else if (c == CLObserverSend.class)
           {
             Constructor<?> constructor = plugin.getConstructor();
             observer = (CLObserverSend) constructor.newInstance();
             ((CLObserverSend) observer).setObjectSender(sender);
             break;
           }
         }
         if (observer != null)
         {
           receiver.addObserver(observer);
         }
       }
       catch (Exception ex)
       {
         Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
       }
     }
     classLoader.getPlugins().clear();
   }
 
   public void loadConf()
   {
     try
     {
       FileInputStream file = new FileInputStream("server.conf");
       ObjectInputStream ois = new ObjectInputStream(file);
 
       conf = (ServerConfiguration) ois.readObject();
     }
     catch (java.io.IOException e)
     {
       conf = new ServerConfiguration();
     }
     catch (ClassNotFoundException e)
     {
       e.printStackTrace();
     }
   }
 
   /**
    * This methods launches the server, shows the Qr code and opens the socket.
    */
   public void start()
   {
     try
     {
       final SystemTray tray = SystemTray.getSystemTray();
       JarFileObserver jarFileObserver = new JarFileObserver();
 
       loadConf();
       connectionManager.openPluginConnection(conf.getInputPort(), conf.getOutputPort());
       jarFileObserver.setClassLoader(classLoader);
 
       tray.add(serverView.getTrayIcon());
 
       while (true)
       {
         Socket inputSocket = connectionManager.getInputSocket().accept();
         Socket outputSocket = connectionManager.getOutputSocket().accept();
         System.out.println("Connected");
 
         receiver = new ObjectReceiver(inputSocket, jarFileObserver);
         sender = new ObjectSender(outputSocket.getOutputStream());
         new Thread(receiver).start();
       }
     }
     catch (IOException ex)
     {
       Logger.getLogger(ServerSample.class.getName()).log(Level.SEVERE, null, ex);
     }
     catch (AWTException ex)
     {
       Logger.getLogger(ServerSample.class.getName()).log(Level.SEVERE, null, ex);
     }
   }
 
   public static ConnectionManager getConnectionManager()
   {
     return connectionManager;
   }
 
   public static void setQrcodeView(Frame f)
   {
     qrcodeView = f;
   }
 
   public static void closeQrcodeView()
   {
     if (qrcodeView != null)
     {
       qrcodeView.dispose();
       qrcodeView = null;
     }
   }
 }
