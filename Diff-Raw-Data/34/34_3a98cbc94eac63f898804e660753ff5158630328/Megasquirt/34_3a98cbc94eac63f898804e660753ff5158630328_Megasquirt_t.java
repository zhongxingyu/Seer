 package uk.org.smithfamily.mslogger.ecuDef;
 
 import java.io.*;
 import java.lang.reflect.Field;
 import java.util.*;
 
 import uk.org.smithfamily.mslogger.ApplicationSettings;
 import uk.org.smithfamily.mslogger.MSLoggerApplication;
 import uk.org.smithfamily.mslogger.comms.Connection;
 import uk.org.smithfamily.mslogger.log.DatalogManager;
 import uk.org.smithfamily.mslogger.log.DebugLogManager;
 import uk.org.smithfamily.mslogger.log.FRDLogManager;
 import android.bluetooth.BluetoothAdapter;
 import android.bluetooth.BluetoothSocket;
 import android.content.Context;
 import android.content.Intent;
 import android.os.*;
 import android.util.Log;
 
 /**
  * Abstract base class for all ECU implementations
  * @author dgs
  *
  */
 public abstract class Megasquirt
 {
     static Timer               connectionWatcher = new Timer("ConnectionWatcher", true);
 
     private boolean            simulated         = false;
 
     protected BluetoothSocket  sock;
 
     private BluetoothAdapter   mAdapter;
 
     public static final String NEW_DATA          = "uk.org.smithfamily.mslogger.ecuDef.Megasquirt.NEW_DATA";
 
     public static final String CONNECTED         = "uk.org.smithfamily.mslogger.ecuDef.Megasquirt.CONNECTED";
     public static final String DISCONNECTED      = "uk.org.smithfamily.mslogger.ecuDef.Megasquirt.DISCONNECTED";
     public static final String TAG               = ApplicationSettings.TAG;
 
     protected Context          context;
 
     public abstract Set<String> getSignature();
 
     public abstract byte[] getOchCommand();
 
     public abstract byte[] getSigCommand();
 
     public abstract void loadConstants(boolean simulated) throws IOException;
 
     public abstract void calculate(byte[] ochBuffer) throws IOException;
 
     public abstract String getLogHeader();
 
     public abstract String getLogRow();
 
     public abstract int getBlockSize();
 
     public abstract int getSigSize();
 
     public abstract int getPageActivationDelay();
 
     public abstract int getInterWriteDelay();
 
     public abstract int getCurrentTPS();
 
     public abstract void initGauges();
 
     public abstract String[] defaultGauges();
 
     public abstract void refreshFlags();
 
     private boolean            logging;
     private boolean            constantsLoaded;
     private String             trueSignature = "Unknown";
     private ECUThread          ecuThread;
     private volatile boolean   running;
 
     private byte[]             ochBuffer;
 
     private RebroadcastHandler handler;
 
     /**
      * Shortcut function to access data tables.  Makes the INI->Java translation a little simpler
      * @param i1 index into table
      * @param name table name
      * @return value from table
      */
     protected int table(int i1, String name)
     {
         return TableManager.INSTANCE.table(i1, name);
     }
 
     /**
      * 
      */
     public synchronized void toggleConnection()
     {
         if (!running)
         {
             ApplicationSettings.INSTANCE.setAutoConnectOverride(true);
             start();
         }
         else
         {
             ApplicationSettings.INSTANCE.setAutoConnectOverride(false);
             stop();
         }
     }
 
     /**
      * 
      * @return
      */
     public boolean isLogging()
     {
         return logging;
     }
 
     /**
      * Temperature unit conversion function
      * @param t temp in F
      * @return temp in C if CELSIUS is set, in F otherwise
      */
     protected double tempCvt(int t)
     {
         if (isSet("CELSIUS"))
         {
             return (t - 32.0) * 5.0 / 9.0;
         }
         else
         {
             return t;
         }
     }
 
     /**
      * Launch the ECU thread
      */
     public synchronized void start()
     {
         DebugLogManager.INSTANCE.log("Megasquirt.start()", Log.INFO);
 
         if (ecuThread == null)
         {
             ecuThread = new ECUThread();
             ecuThread.start();
         }
     }
 
     /**
      * Shut down the ECU thread
      */
     public synchronized void stop()
     {
         if (ecuThread != null)
         {
             ecuThread.halt();
             ecuThread = null;
         }
         running = false;
         DebugLogManager.INSTANCE.log("Megasquirt.stop()", Log.INFO);
         // disconnect();
         sendMessage("");
 
     }
 
     /**
      * Revert to initial state
      */
     public void reset()
     {
         refreshFlags();
         constantsLoaded = false;
         running = false;
     }
 
     /**
      * Output the current values to be logged
      */
     private void logValues()
     {
 
         if (!isLogging())
         {
             return;
         }
         try
         {
             FRDLogManager.INSTANCE.write(ochBuffer);
             DatalogManager.INSTANCE.write(getLogRow());
 
         }
         catch (IOException e)
         {
             // ErrorReporter.getInstance().handleException(e);
             DebugLogManager.INSTANCE.logException(e);
 
             Log.e(ApplicationSettings.TAG, "Megasquirt.logValues()", e);
         }
     }
 
     /**
      * Shutdown the data connection to the MS
      */
     private void disconnect()
     {
         if (simulated)
             return;
         DebugLogManager.INSTANCE.log("Disconnect",Log.INFO);
         
         Connection.INSTANCE.disconnect();
         DatalogManager.INSTANCE.mark("Disconnected");
         FRDLogManager.INSTANCE.close();
         DatalogManager.INSTANCE.close();
         broadcast(DISCONNECTED);
     }
 
     /**
      * Send a message to the user
      * @param msg
      */
     protected void sendMessage(String msg)
     {
         Intent broadcast = new Intent();
         broadcast.setAction(ApplicationSettings.GENERAL_MESSAGE);
         broadcast.putExtra(ApplicationSettings.MESSAGE, msg);
         context.sendBroadcast(broadcast);
     }
 
     /**
      * Send a status update to the rest of the application
      * @param action
      */
     private void broadcast(String action)
     {
         Intent broadcast = new Intent();
         broadcast.setAction(action);
         // broadcast.putExtra(LOCATION, location);
         context.sendBroadcast(broadcast);
 
     }
 
     /**
      * 
      * @param c
      */
     public Megasquirt(Context c)
     {
         this.context = c;
         handler = new RebroadcastHandler(this);
     }
 
     /**
      * How long have we been running?
      * @return
      */
     protected double timeNow()
     {
         return (System.currentTimeMillis() - DatalogManager.INSTANCE.getLogStart()) / 1000.0;
     }
 
     /**
      * Use reflection to extract a named value out of the subclass
      * @param channel
      * @return
      */
     public double getValue(String channel)
     {
 
         double value = 0;
         Class<?> c = this.getClass();
         try
         {
             Field f = c.getDeclaredField(channel);
             value = f.getDouble(this);
         }
         catch (Exception e)
         {
             DebugLogManager.INSTANCE.log("Failed to get value for " + channel, Log.ERROR);
             Log.e(ApplicationSettings.TAG, "Megasquirt.getValue()", e);
         }
         return value;
     }
 
     /**
      * Flag the logging process to happen
      */
     public void startLogging()
     {
         logging = true;
         DebugLogManager.INSTANCE.log("startLogging()", Log.INFO);
 
     }
 
     /**
      * Stop the logging process
      */
     public void stopLogging()
     {
         DebugLogManager.INSTANCE.log("stopLogging()", Log.INFO);
         logging = false;
         FRDLogManager.INSTANCE.close();
         DatalogManager.INSTANCE.close();
     }
 
     /**
      * Take a wild stab at what this does.
      * @param v
      * @return
      */
     protected double round(double v)
     {
         return Math.floor(v * 100 + .5) / 100;
     }
 
     /**
      * Returns if a flag has been set in the application
      * @param name
      * @return
      */
     protected boolean isSet(String name)
     {
         return ApplicationSettings.INSTANCE.isSet(name);
     }
 
     /**
      *
      */
     private class RebroadcastHandler extends Handler
     {
         private Megasquirt ecu;
 
         /**
          * 
          * @param ecu
          */
         public RebroadcastHandler(Megasquirt ecu)
         {
             this.ecu = ecu;
         }
 
         /**
          * 
          * @param m
          */
         @Override
         public void handleMessage(Message m)
         {
             super.handleMessage(m);
             Bundle b = m.getData();
             String msg = b.getString(MSLoggerApplication.MSG_ID);
             ecu.sendMessage(msg);
         }
     }
 
     /**
      * The thread that handles all communications with the ECU.  This must be done in it's own thread
      * as Android gets very picky about unresponsive UI threads
      */
     private class ECUThread extends Thread
     {
 
         /**
          * 
          */
         public ECUThread()
         {
             Set<String> sigs = Megasquirt.this.getSignature();
             setName("ECUThread:" + sigs.iterator().next());
         }
 
         /**
          * Kick the connection off
          */
         public void initialiseConnection()
         {
             ochBuffer = new byte[Megasquirt.this.getBlockSize()];
 
             if (!ApplicationSettings.INSTANCE.btDeviceSelected())
             {
                 sendMessage("Bluetooth device not selected");
                 return;
             }
             mAdapter = ApplicationSettings.INSTANCE.getDefaultAdapter();
             if (mAdapter == null)
             {
                 sendMessage("Could not find the default Bluetooth adapter!");
                 return;
             }
             String btAddr = ApplicationSettings.INSTANCE.getBluetoothMac();
 
             sendMessage("Launching connection");
             Connection.INSTANCE.init(btAddr, mAdapter, handler);
 
         }
 
         /**
          * The main loop of the connection to the ECU
          */
         public void run()
         {
             sendMessage("");
             DebugLogManager.INSTANCE.log("BEGIN connectedThread", Log.INFO);
             initialiseConnection();
             
             try
             {
                 Thread.sleep(500);
             }
             catch (InterruptedException e1)
             {
 
                 e1.printStackTrace();
             }
             try
             {
                 Connection.INSTANCE.connect();
                 Connection.INSTANCE.flushAll();
 
                 if (!verifySignature())
                 {
                     DebugLogManager.INSTANCE.log("!verifySignature()", Log.DEBUG);
 
                     Connection.INSTANCE.disconnect();
                     return;
                 }
                
                // Make sure everyone agrees on what flags are set
                 ApplicationSettings.INSTANCE.refreshFlags();
                 refreshFlags();
                 if (!constantsLoaded)
                 {
                    // Only do this once so reconnects are quicker
                     loadConstants(simulated);
                     constantsLoaded = true;
                    
                    sendMessage("Connected to " + getTrueSignature());
                 }
                 running = true;
                 
                // This is the actual work.  Outside influences will toggle 'running' when we want this to stop
                 while (running)
                 {
                     getRuntimeVars();
                     calculateValues();
                     logValues();
                     broadcast(NEW_DATA);
                 }
             }
             catch (IOException e)
             {
                 DebugLogManager.INSTANCE.logException(e);
             }
             catch (ArithmeticException e)
             {
                 //If we get a maths error, we probably have loaded duff constants and hit a divide by zero
                 //force the constants to reload in case it was just a bad data read
                 DebugLogManager.INSTANCE.logException(e);
                 constantsLoaded = false;
             }
             catch (RuntimeException t)
             {
                 DebugLogManager.INSTANCE.logException(t);
                 throw (t);
             }
             //We're on our way out, so drop the connection
             disconnect();
 
         }
 
         /**
          * Called by other threads to stop the comms
          */
         public void halt()
         {
             running = false;
         }
 
         /**
          * Checks that the signature returned by the ECU is what we are expecting
          * @return
          * @throws IOException
          */
         private boolean verifySignature() throws IOException
         {
             boolean verified = false;
             String msSig = null;
             if (simulated)
             {
                 verified = true;
             }
             else
             {
                 byte[] sigCommand = getSigCommand();
                 sendMessage("Verifying MS");
                 Set<String> signatures = Megasquirt.this.getSignature();
 
                 msSig = getSignature(sigCommand);
                 verified = signatures.contains(msSig);
                 if (verified)
                 {
                     trueSignature = msSig;
                 }
             }
             if (verified)
             {
                 sendMessage("Connected to " + trueSignature);
                 broadcast(CONNECTED);
             }
             else
             {
                 sendMessage("Signature error! " + msSig);
             }
             return verified;
         }
 
         /**
          * Get the current variables from the ECU
          * @throws IOException
          */
         private void getRuntimeVars() throws IOException
         {
             // Debug.startMethodTracing("getRuntimeVars");
             if (simulated)
             {
                 MSSimulator.INSTANCE.getNextRTV(ochBuffer);
                 return;
             }
             int d = getInterWriteDelay();
             Connection.INSTANCE.writeAndRead(getOchCommand(), ochBuffer, d);
             // Debug.stopMethodTracing();
         }
 
         /**
          * Call the subclass to calculate any expressions
          * @throws IOException
          */
         private void calculateValues() throws IOException
         {
             calculate(ochBuffer);
         }
 
         /**
          * Read a page of constants from the ECU into a byte buffer.  MS1 uses a select/read combo, MS2 just does a read
          * @param pageBuffer
          * @param pageSelectCommand
          * @param pageReadCommand
          * @throws IOException
          */
         protected void getPage(byte[] pageBuffer, byte[] pageSelectCommand, byte[] pageReadCommand) throws IOException
         {
 
             Connection.INSTANCE.flushAll();
             int delay = getPageActivationDelay();
             if (pageSelectCommand != null)
             {
                 Connection.INSTANCE.writeCommand(pageSelectCommand, delay);
             }
             if (pageReadCommand != null)
             {
                 Connection.INSTANCE.writeCommand(pageReadCommand, delay);
             }
             Connection.INSTANCE.readBytes(pageBuffer);
         }
 
         /**
          * Gets the signature from the ECU
          * @param sigCommand
          * @return
          * @throws IOException
          */
         private String getSignature(byte[] sigCommand) throws IOException
         {
             String sig1 = "NoSigReadYet";
             String sig2 = "Not here";
             DebugLogManager.INSTANCE.log("getSignature()", Log.DEBUG);
             int d = getInterWriteDelay();
             
             /*
              * We need to loop around until we get a constant result.  When a BT module connects, it can feed
              * an initial 'CONNECT xyz' string into the ECU which confuses the hell out of it, and the first few
              * interactions return garbage
              */
             do
             {
                 byte[] buf = Connection.INSTANCE.writeAndRead(sigCommand, d);
                 if (!sig2.equals(ECUFingerprint.UNKNOWN))
                 {
                     sig1 = sig2;
                 }
                 try
                 {
                     sig2 = new String(ECUFingerprint.processResponse(buf));
                 }
                 catch (BootException e)
                 {
                     return "ECU needs a reboot!";
                 }
                 DebugLogManager.INSTANCE.log("Got a signature of " + sig2, Log.INFO);
 
                 Connection.INSTANCE.flushAll();
             }
             while (!sig1.equals(sig2));
             return sig1;
         }
 
     }
 
     /**
      * 
      * @return
      */
     public String getTrueSignature()
     {
         return trueSignature;
     }
 
     /**
      * 
      * @return
      */
     public boolean isRunning()
     {
         return running;
     }
 
     /**
      * helper method for subclasses
      * @param pageNo
      * @param pageOffset
      * @param pageSize
      * @param select
      * @param read
      * @return
      */
     protected byte[] loadPage(int pageNo, int pageOffset, int pageSize, byte[] select, byte[] read)
     {
 
         byte[] buffer = new byte[pageSize];
         try
         {
             sendMessage("Loading constants from page " + pageNo);
             getPage(buffer, select, read);
             savePage(pageNo, buffer);
             sendMessage("Constants loaded from page " + pageNo);
         }
         catch (IOException e)
         {
             e.printStackTrace();
             DebugLogManager.INSTANCE.logException(e);
             sendMessage("Error loading constants from page " + pageNo);
         }
         return buffer;
     }
 
     /**
      * 
      * @param buffer
      * @param select
      * @param read
      * @throws IOException
      */
     private void getPage(byte[] buffer, byte[] select, byte[] read) throws IOException
     {
         ecuThread.getPage(buffer, select, read);
     }
 
     /**
      * Dumps a loaded page to SD card for analysis
      * @param pageNo
      * @param buffer
      */
     private void savePage(int pageNo, byte[] buffer)
     {
 
         try
         {
             File dir = new File(Environment.getExternalStorageDirectory(), "MSLogger");
             dir.mkdirs();
 
             String fileName = this.getClass().getName() + ".firmware";
             File outputFile = new File(dir, fileName);
             BufferedOutputStream out = null;
             try
             {
                 boolean append = !(pageNo == 1);
                 out = new BufferedOutputStream(new FileOutputStream(outputFile, append));
                 DebugLogManager.INSTANCE.log("Saving page " + pageNo + " append=" + append, Log.INFO);
                 out.write(buffer);
             }
             finally
             {
                 if (out != null)
                 {
                     out.flush();
                     out.close();
                 }
             }
         }
         catch (IOException e)
         {
             DebugLogManager.INSTANCE.logException(e);
         }
     }
 
 }
