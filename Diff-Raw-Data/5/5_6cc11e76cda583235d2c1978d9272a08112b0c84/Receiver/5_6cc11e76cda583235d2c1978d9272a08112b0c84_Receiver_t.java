 package com.kokakiwi.fun.pulsar.net;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.net.URL;
 import java.net.URLConnection;
 import java.util.List;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import com.google.common.collect.Lists;
 import com.kokakiwi.fun.pulsar.logger.PulsarLogger;
 
 public class Receiver implements Runnable
 {
     public final static String        URL                   = "http://psrx0392-15.0x10c.com/";
     public final static Pattern       SCANNER_INFOS_PATTERN = Pattern
                                                                     .compile("SCANNING\\s+(.+)\\s+AT\\s+(\\d.\\d+)");
     public final static Pattern       POWER_PATTERN         = Pattern
                                                                     .compile("POWER\\s(\\d+.\\d+)");
     public final static Pattern       DATA_PATTERN          = Pattern
                                                                     .compile("([A-F0-9]{4}\\s[A-F0-9]{4}\\s[A-F0-9]{4}\\s[A-F0-9]{4}\\s[A-F0-9]{4}\\s[A-F0-9]{4}\\s[A-F0-9]{4}\\s[A-F0-9]{4}\\s[A-F0-9]{4}\\s[A-F0-9]{4}\\s[A-F0-9]{4}\\s[A-F0-9]{4}\\s[A-F0-9]{4}\\s[A-F0-9]{4}\\s[A-F0-9]{4}\\s[A-F0-9]{4})");
     
     private boolean                   listening             = false;
     private URLConnection             connection;
     private InputStream               in;
    private BufferedReader            reader;
     
     private final List<IDataListener> listeners             = Lists.newLinkedList();
     private final IDataListener       listener;
     
     public Receiver()
     {
         listener = new ListDataListener(this);
         
         try
         {
             openConnection();
         }
         catch (IOException e)
         {
             e.printStackTrace();
         }
     }
     
     public void run()
     {
         listening = true;
         
         while (listening)
         {
             try
             {
                 String line = reader.readLine();
                 if (line != null)
                 {
                     boolean data = false;
                     Matcher matcher;
                     if ((matcher = SCANNER_INFOS_PATTERN.matcher(line)).find())
                     {
                         String name = matcher.group(1);
                         double value = Double.parseDouble(matcher.group(2));
                         
                         listener.onInfos(name, value);
                     }
                     else if ((matcher = POWER_PATTERN.matcher(line)).find())
                     {
                         double power = Double.parseDouble(matcher.group(1));
                         
                         listener.onPower(power);
                     }
                     else if ((matcher = DATA_PATTERN.matcher(line)).find())
                     {
                         data = true;
                         String[] datas = line.split(" ");
                         
                         listener.onData(datas);
                     }
                     listener.onLine(line);
                     PulsarLogger.info(line, data);
                 }
                 else
                 {
                     openConnection();
                 }
             }
             catch (IOException e)
             {
                 e.printStackTrace();
             }
         }
     }
     
     public void openConnection() throws IOException
     {
         URL url = new URL(URL);
         connection = url.openConnection();
         
         in = connection.getInputStream();
        reader = new BufferedReader(new InputStreamReader(in));
     }
     
     public void start()
     {
         Thread thread = new Thread(this);
         thread.start();
     }
     
     public boolean isListening()
     {
         return listening;
     }
     
     public void setListening(boolean listening)
     {
         this.listening = listening;
     }
     
     public URLConnection getConnection()
     {
         return connection;
     }
     
     public InputStream getInputStream()
     {
         return in;
     }
     
     public List<IDataListener> getListeners()
     {
         return listeners;
     }
     
     public void addListener(IDataListener listener)
     {
         listeners.add(listener);
     }
     
     public static class ListDataListener implements IDataListener
     {
         private final Receiver receiver;
         
         public ListDataListener(Receiver receiver)
         {
             this.receiver = receiver;
         }
         
         public void onInfos(String name, double value)
         {
             for (IDataListener l : receiver.getListeners())
             {
                 l.onInfos(name, value);
             }
         }
         
         public void onPower(double power)
         {
             for (IDataListener l : receiver.getListeners())
             {
                 l.onPower(power);
             }
         }
         
         public void onData(String[] datas)
         {
             for (IDataListener l : receiver.getListeners())
             {
                 l.onData(datas);
             }
         }
         
         public void onLine(String line)
         {
             for (IDataListener l : receiver.getListeners())
             {
                 l.onLine(line);
             }
         }
         
     }
 }
