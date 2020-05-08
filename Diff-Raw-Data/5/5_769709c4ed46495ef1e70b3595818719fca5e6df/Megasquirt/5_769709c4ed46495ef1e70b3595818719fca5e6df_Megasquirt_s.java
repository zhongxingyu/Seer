 package uk.org.smithfamily.mslogger.ecuDef;
 
 import java.io.BufferedOutputStream;
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.lang.reflect.Constructor;
 import java.lang.reflect.Field;
 import java.text.DecimalFormat;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.List;
 
 import uk.org.smithfamily.mslogger.ApplicationSettings;
 import uk.org.smithfamily.mslogger.comms.CRC32Exception;
 import uk.org.smithfamily.mslogger.comms.Connection;
 import uk.org.smithfamily.mslogger.comms.ConnectionFactory;
 import uk.org.smithfamily.mslogger.comms.ECUConnectionManager;
 import uk.org.smithfamily.mslogger.ecuDef.gen.ECURegistry;
 import uk.org.smithfamily.mslogger.log.DatalogManager;
 import uk.org.smithfamily.mslogger.log.DebugLogManager;
 import uk.org.smithfamily.mslogger.log.FRDLogManager;
 import uk.org.smithfamily.mslogger.widgets.GaugeRegister;
 import uk.org.smithfamily.mslogger.widgets.GaugeRegisterInterface;
 import android.app.Service;
 import android.content.BroadcastReceiver;
 import android.content.Context;
 import android.content.Intent;
 import android.content.IntentFilter;
 import android.os.Binder;
 import android.os.Environment;
 import android.os.IBinder;
 import android.util.Log;
 import android.widget.Toast;
 
 /**
  * Abstract base class for all ECU implementations
  * 
  * @author dgs
  * 
  */
 public class Megasquirt extends Service implements MSControllerInterface
 {
     private MSECUInterface ecuImplementation;
 
     private boolean simulated = false;
     public static final String CONNECTED = "uk.org.smithfamily.mslogger.ecuDef.Megasquirt.CONNECTED";
     public static final String DISCONNECTED = "uk.org.smithfamily.mslogger.ecuDef.Megasquirt.DISCONNECTED";
     public static final String NEW_DATA = "uk.org.smithfamily.mslogger.ecuDef.Megasquirt.NEW_DATA";
     public static final String UNKNOWN_ECU = "uk.org.smithfamily.mslogger.ecuDef.Megasquirt.UNKNOWN_ECU";
     public static final String UNKNOWN_ECU_BT = "uk.org.smithfamily.mslogger.ecuDef.Megasquirt.UNKNOWN_ECU_BT";
     public static final String PROBE_ECU = "uk.org.smithfamily.mslogger.ecuDef.Megasquirt.ECU_PROBED";
 
     private static final String UNKNOWN = "UNKNOWN";
     private static final String LAST_SIG = "LAST_SIG";
     private static final String LAST_PROBE = "LAST_PROBE";
 
     // protected Context context;
 
     private BroadcastReceiver yourReceiver;
 
     private boolean logging;
     private boolean constantsLoaded;
     private String trueSignature = "Unknown";
     private volatile ECUThread ecuThread;
     private volatile boolean running;
     private static volatile ECUThread watch;
 
     public class LocalBinder extends Binder
     {
         public Megasquirt getService()
         {
             return Megasquirt.this;
         }
     }
 
     @Override
     public int onStartCommand(Intent intent, int flags, int startId)
     {
         DebugLogManager.INSTANCE.log("Megasquirt Received start id " + startId + ": " + intent,Log.VERBOSE);
         // We want this service to continue running until it is explicitly
         // stopped, so return sticky.
         return START_STICKY;
     }
 
     @Override
     public IBinder onBind(Intent intent)
     {
         return mBinder;
     }
 
     // This is the object that receives interactions from clients. See
     // RemoteService for a more complete example.
     private final IBinder mBinder = new LocalBinder();
 
     @Override
     public void onCreate()
     {
         super.onCreate();
 
         final IntentFilter theFilter = new IntentFilter();
         theFilter.addAction(ApplicationSettings.BT_CHANGED);
 
         this.yourReceiver = new BroadcastReceiver()
         {
 
             @Override
             public void onReceive(Context context, Intent intent)
             {
                 DebugLogManager.INSTANCE.log("BT_CHANGED received", Log.VERBOSE);
                 stop();
                 start();
             }
         };
 
         // Registers the receiver so that your service will listen for broadcasts
         this.registerReceiver(this.yourReceiver, theFilter);
         ApplicationSettings.INSTANCE.setEcu(this);
         start();
     }
 
     @Override
     public void onDestroy()
     {
         super.onDestroy();
 
         // Do not forget to unregister the receiver!!!
         this.unregisterReceiver(this.yourReceiver);
     }
 
     /**
      * Shortcut function to access data tables. Makes the INI->Java translation a little simpler
      * 
      * @param i1 index into table
      * @param name table name
      * @return value from table
      */
     protected int table(int i1, String name)
     {
         return TableManager.INSTANCE.table(i1, name);
     }
 
     public int table(double d1, String name)
     {
         return table((int) d1, name);
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
      * 
      * @param t temp in F
      * @return temp in C if CELSIUS is set, in F otherwise
      */
     public double tempCvt(double t)
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
 
         if (ApplicationSettings.INSTANCE.getECUBluetoothMac().equals(ApplicationSettings.MISSING_VALUE))
         {
             broadcast(UNKNOWN_ECU_BT);
         }
         else
         {
             if(ecuThread != null)
             {
                 ecuThread.halt();
             }
             ecuThread = new ECUThread();
                 
             ecuThread.start();
         }
     }
 
     /**
      * Shut down the ECU thread
      */
     public synchronized void stop()
     {
         DebugLogManager.INSTANCE.log("Megasquirt.stop()", Log.INFO);
 
         if (ecuThread != null)
         {
             ecuThread.halt();
             ecuThread = null;
         }
 
         running = false;
 
         
         broadcast(DISCONNECTED);
     }
 
     /**
      * Revert to initial state
      */
     public void reset()
     {
         ecuImplementation.refreshFlags();
         constantsLoaded = false;
         running = false;
     }
 
     /**
      * Output the current values to be logged
      */
     private void logValues(byte[] buffer)
     {
 
         if (!isLogging())
         {
             return;
         }
         try
         {
             FRDLogManager.INSTANCE.write(buffer);
             DatalogManager.INSTANCE.write(ecuImplementation.getLogRow());
 
         }
         catch (IOException e)
         {
             DebugLogManager.INSTANCE.logException(e);
         }
     }
 
     /**
      * Shutdown the data connection to the MS
      */
     private void disconnect()
     {
         if (simulated)
             return;
         DebugLogManager.INSTANCE.log("Disconnect", Log.INFO);
 
         ECUConnectionManager.getInstance().disconnect();
         DatalogManager.INSTANCE.mark("Disconnected");
         FRDLogManager.INSTANCE.close();
         DatalogManager.INSTANCE.close();
         broadcast(DISCONNECTED);
     }
 
     /**
      * Send a message to the user
      * 
      * @param msg Message to be sent
      */
     protected void sendMessage(String msg)
     {
         broadcast(ApplicationSettings.GENERAL_MESSAGE, msg);
 
     }
 
     /**
      * Send a toast message to the user
      * 
      * @param message to be sent
      */
     protected void sendToastMessage(String msg)
     {
         Intent broadcast = new Intent();
         broadcast.setAction(ApplicationSettings.TOAST);
         broadcast.putExtra(ApplicationSettings.TOAST_MESSAGE, msg);
         sendBroadcast(broadcast);
     }
 
     /**
      * Send the reads per second to be displayed on the screen
      * 
      * @param RPS the current reads per second value
      */
     private void sendRPS(double RPS)
     {
         DecimalFormat decimalFormat = new DecimalFormat("#.0");
 
         Intent broadcast = new Intent();
         broadcast.setAction(ApplicationSettings.RPS_MESSAGE);
         broadcast.putExtra(ApplicationSettings.RPS, decimalFormat.format(RPS));
         sendBroadcast(broadcast);
     }
 
     /**
      * Send a status update to the rest of the application
      * 
      * @param action
      */
     private void broadcast(String action)
     {
         Intent broadcast = new Intent();
         broadcast.setAction(action);
         sendBroadcast(broadcast);
     }
 
     private void broadcast(String action, String data)
     {
         DebugLogManager.INSTANCE.log("Megasquirt.broadcast("+action+","+data+")",Log.VERBOSE);
         Intent broadcast = new Intent();
         broadcast.setAction(action);
         broadcast.putExtra(ApplicationSettings.MESSAGE, data);
         sendBroadcast(broadcast);
     }
 
     private void broadcast()
     {
         Intent broadcast = new Intent();
         broadcast.setAction(NEW_DATA);
 
         sendBroadcast(broadcast);
 
     }
 
     /**
      * How long have we been running?
      * 
      * @return
      */
     public double timeNow()
     {
         DecimalFormat decimalFormat = new DecimalFormat("#.000");
 
         return Double.parseDouble(decimalFormat.format(((System.currentTimeMillis() - DatalogManager.INSTANCE.getLogStart()) / 1000.0)));
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
      * 
      * @param v
      * @return
      */
     public double round(double v)
     {
         return Math.floor(v * 100 + .5) / 100;
     }
 
     /**
      * Returns if a flag has been set in the application
      * 
      * @param name
      * @return
      */
     public boolean isSet(String name)
     {
         return ApplicationSettings.INSTANCE.isSet(name);
     }
 
     /**
      * The thread that handles all communications with the ECU. This must be done in it's own thread as Android gets very picky about unresponsive UI
      * threads
      */
     private class ECUThread extends Thread
     {
         private class CalculationThread extends Thread
         {
             private volatile boolean running = true;
             public void halt()
             {
                 DebugLogManager.INSTANCE.log("CalculationThread.halt()", Log.INFO);
 
                 running = false;
             }
             
             public void run()
             {
                 this.setName("CalculationThread");
                 try
                 {
                     while (running)
                     {
                         byte[] buffer = handshake.get();
                         if (ecuImplementation != null)
                         {
                             ecuImplementation.calculate(buffer);
                             logValues(buffer);
                             broadcast();
                         }
                     }
                 }
                 catch (InterruptedException e)
                 {
                     // Swallow, we're on our way out.
                 }
             }
             
         }
         
         class Handshake
         {
             private byte[] buffer;
 
             public void put(byte[] buf)
             {
                 buffer = buf;
                 synchronized (this)
                 {
                     notify();
                 }
             }
 
             public byte[] get() throws InterruptedException
             {
                 synchronized (this)
                 {
                     wait();
                 }
                 return buffer;
             }
         }
 
         Handshake handshake = new Handshake();
         CalculationThread calculationThread = new CalculationThread();
 
         /**
          * 
          */
         public ECUThread()
         {
             if (watch != null)
             {
                 DebugLogManager.INSTANCE.log("Attempting to create second connection!", Log.ASSERT);
             }
             watch = this;
             String name = "ECUThread:" + System.currentTimeMillis();
             setName(name);
             DebugLogManager.INSTANCE.log("Creating ECUThread named "+name, Log.VERBOSE);
             calculationThread.start();
         
         }
 
         
 
         /**
          * Kick the connection off
          */
         public void initialiseConnection()
         {
             // sendMessage("Launching connection");
 
             //Connection conn = ConnectionFactory.INSTANCE.getConnection();
             String btAddress = ApplicationSettings.INSTANCE.getECUBluetoothMac();
             ECUConnectionManager.getInstance().init(null, btAddress);
         }
 
         /**
          * The main loop of the connection to the ECU
          */
         public void run()
         {
             try
             {
                 sendMessage("Starting connection");
                 DebugLogManager.INSTANCE.log("BEGIN connectedThread", Log.INFO);
                 initialiseConnection();
 
                 try
                 {
                     Thread.sleep(500);
                 }
                 catch (InterruptedException e)
                 {
                     DebugLogManager.INSTANCE.logException(e);
                 }
 
                 running = true;
 
                 try
                 {
                     ECUConnectionManager.getInstance().flushAll();
                     initialiseImplementation();
                     /*
                      * Make sure we have calculated runtime vars at least once before refreshing flags. The reason is that the refreshFlags() function
                      * also trigger the creation of menus/dialogs/tables/curves/etc that use variables such as {clthighlim} in curves that need to
                      * have their value assigned before being used.
                      */
                     try
                     {
                         byte[] bufferRV = getRuntimeVars();
                         ecuImplementation.calculate(bufferRV);
                     }
                     catch (CRC32Exception e)
                     {
                         DebugLogManager.INSTANCE.logException(e);
                     }
 
                     // Make sure everyone agrees on what flags are set
                     ApplicationSettings.INSTANCE.refreshFlags();
                     ecuImplementation.refreshFlags();
               
                     if (!constantsLoaded)
                     {
                         // Only do this once so reconnects are quicker
                         ecuImplementation.loadConstants(simulated);
                         constantsLoaded = true;
 
                         
                     }
                     sendMessage("Connected to " + getTrueSignature());
                     long lastRpsTime = System.currentTimeMillis();
                     double readCounter = 0;
 
                     // This is the actual work. Outside influences will toggle 'running' when we want this to stop
                     while (running)
                     {
                         try
                         {
                             final byte[] buffer = getRuntimeVars();
                             handshake.put(buffer);
                         }
                         catch (CRC32Exception e)
                         {
                             DatalogManager.INSTANCE.mark(e.getLocalizedMessage());
                             DebugLogManager.INSTANCE.logException(e);
                         }
                         readCounter++;
 
                         long delay = System.currentTimeMillis() - lastRpsTime;
                         if (delay > 1000)
                         {
                             double RPS = readCounter / delay * 1000;
                             readCounter = 0;
                             lastRpsTime = System.currentTimeMillis();
 
                             if (RPS > 0)
                             {
                                 sendRPS(RPS);
                             }
                         }
 
                     }
                 }
                 catch (IOException e)
                 {
                     DebugLogManager.INSTANCE.logException(e);
                 }
                 catch (CRC32Exception e)
                 {
                     DebugLogManager.INSTANCE.logException(e);
                 }
                 catch (ArithmeticException e)
                 {
                     // If we get a maths error, we probably have loaded duff constants and hit a divide by zero
                     // force the constants to reload in case it was just a bad data read
                     DebugLogManager.INSTANCE.logException(e);
                     constantsLoaded = false;
                 }
                 catch (RuntimeException t)
                 {
                     DebugLogManager.INSTANCE.logException(t);
                     throw (t);
                 }
                 // We're on our way out, so drop the connection
                 disconnect();
             }
             finally
             {
                 calculationThread.halt();
                 calculationThread.interrupt();
                 watch = null;
             }
         }
 
         private void initialiseImplementation() throws IOException, CRC32Exception
         {
             sendMessage("Checking your ECU");
             String signature = getSignature();
 
             Class<? extends MSECUInterface> ecuClass = ECURegistry.INSTANCE.findEcu(signature);
 
             if (ecuImplementation != null && ecuImplementation.getClass().equals(ecuClass))
             {
                 return;
             }
 
             Constructor<? extends MSECUInterface> constructor;
             try
             {
                 constructor = ecuClass.getConstructor(MSControllerInterface.class, MSUtilsInterface.class, GaugeRegisterInterface.class);
 
                 ecuImplementation = constructor.newInstance(Megasquirt.this, MSUtils.INSTANCE, GaugeRegister.INSTANCE);
  
                 if(!signature.equals(ecuImplementation.getSignature()))
                 {
                     trueSignature = ecuImplementation.getSignature();
                     
                     String msg = "Got unsupported signature from Megasquirt \"" + trueSignature + "\" but found a similar supported signature \"" + signature + "\"";
                     
                     sendToastMessage(msg);
                     DebugLogManager.INSTANCE.log(msg, Log.INFO);
                 }
                 sendMessage("Found "+trueSignature);
 
             }
             catch (Exception e)
             {
                 DebugLogManager.INSTANCE.logException(e);
                 broadcast(UNKNOWN_ECU);
             }
             broadcast(PROBE_ECU);
         }
 
         private String getSignature() throws IOException, CRC32Exception
         {
             byte[] bootCommand = { 'X' };
             String lastSuccessfulProbeCommand = ApplicationSettings.INSTANCE.getPref(LAST_PROBE);
             String lastSig = ApplicationSettings.INSTANCE.getPref(LAST_SIG);
 
             if (lastSuccessfulProbeCommand != null && lastSig != null)
             {
                 byte[] probe = lastSuccessfulProbeCommand.getBytes();
                 // We need to loop as a BT adapter can pump crap into the MS at the start which confuses the poor thing.
                 for (int i = 0; i < 3; i++)
                 {
                     byte[] response = ECUConnectionManager.getInstance().writeAndRead(probe, 50, false);
                     try
                     {
                         String sig = processResponse(response);
                         if (lastSig.equals(sig))
                         {
                             return sig;
                         }
                     }
                     catch (BootException e)
                     {
                         response = ECUConnectionManager.getInstance().writeAndRead(bootCommand, 500, false);
                     }
                 }
             }
             String probeCommand1 = "Q";
             String probeCommand2 = "S";
             String probeUsed;
             int i = 0;
             String sig = UNKNOWN;
 
             // IF we don't get it in 20 goes, we're not talking to a Megasquirt
             while (i++ < 20)
             {
                 probeUsed = probeCommand1;
                 byte[] response = ECUConnectionManager.getInstance().writeAndRead(probeUsed.getBytes(), 500, false);
 
                 try
                 {
                     if (response != null && response.length > 1)
                     {
                         sig = processResponse(response);
                     }
                     else
                     {
                         probeUsed = probeCommand2;
                         response = ECUConnectionManager.getInstance().writeAndRead(probeUsed.getBytes(), 500, false);
                         if (response != null && response.length > 1)
                         {
                             sig = processResponse(response);
                         }
                     }
                     if (!UNKNOWN.equals(sig))
                     {
                         ApplicationSettings.INSTANCE.setPref(LAST_PROBE, probeUsed);
                         ApplicationSettings.INSTANCE.setPref(LAST_SIG, sig);
                         ECUConnectionManager.getInstance().flushAll();
                         break;
                     }
                 }
                 catch (BootException e)
                 {
                     /*
                      * My ECU also occasionally goes to a Boot> prompt on start up (dodgy electrics) so if we see that, force the ECU to start.
                      */
                     response = ECUConnectionManager.getInstance().writeAndRead(bootCommand, 500, false);
                 }
             }
 
             return sig;
         }
 
         /**
          * Attempt to figure out the data we got back from the device
          * 
          * @param response
          * @return
          * @throws BootException
          */
         private String processResponse(byte[] response) throws BootException
         {
             String result = new String(response);
             trueSignature = result;
             if (result.contains("Boot>"))
             {
                 throw new BootException();
             }
 
             if (response == null)
                 return UNKNOWN;
 
             // Early ECUs only respond with one byte
             if (response.length == 1 && response[0] != 20)
                 return UNKNOWN;
 
             if (response.length <= 1)
                 return UNKNOWN;
 
             // Examine the first few bytes and see if it smells of one of the things an MS may say to us.
             if ((response[0] != 'M' && response[0] != 'J') || (response[1] != 'S' && response[1] != 'o' && response[1] != 'i'))
                 return UNKNOWN;
 
             // Looks like we have a Megasquirt
             return result;
         }
 
         /**
          * Called by other threads to stop the comms
          */
         public void halt()
         {
             DebugLogManager.INSTANCE.log("ECUThread.halt()", Log.INFO);
 
             running = false;
         }
 
         /**
          * Get the current variables from the ECU
          * 
          * @throws IOException
          * @throws CRC32Exception
          */
         private byte[] getRuntimeVars() throws IOException, CRC32Exception
         {
             byte[] buffer = new byte[ecuImplementation.getBlockSize()];
             if (simulated)
             {
                 MSSimulator.INSTANCE.getNextRTV(buffer);
                 return buffer;
             }
             int d = ecuImplementation.getInterWriteDelay();
             ECUConnectionManager.getInstance().writeAndRead(ecuImplementation.getOchCommand(), buffer, d, ecuImplementation.isCRC32Protocol());
             return buffer;
         }
 
         /**
          * Read a page of constants from the ECU into a byte buffer. MS1 uses a select/read combo, MS2 just does a read
          * 
          * @param pageBuffer
          * @param pageSelectCommand
          * @param pageReadCommand
          * @throws IOException
          */
         protected void getPage(byte[] pageBuffer, byte[] pageSelectCommand, byte[] pageReadCommand) throws IOException, CRC32Exception
         {
             ECUConnectionManager.getInstance().flushAll();
             int delay = ecuImplementation.getPageActivationDelay();
             if (pageSelectCommand != null)
             {
                 ECUConnectionManager.getInstance().writeCommand(pageSelectCommand, delay, ecuImplementation.isCRC32Protocol());
             }
             if (pageReadCommand != null)
             {
                 ECUConnectionManager.getInstance().writeCommand(pageReadCommand, delay, ecuImplementation.isCRC32Protocol());
             }
             ECUConnectionManager.getInstance().readBytes(pageBuffer, ecuImplementation.isCRC32Protocol());
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
      * 
      * @param pageNo
      * @param pageOffset
      * @param pageSize
      * @param select
      * @param read
      * @return
      */
     public byte[] loadPage(int pageNo, int pageOffset, int pageSize, byte[] select, byte[] read)
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
         catch (CRC32Exception e)
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
     private void getPage(byte[] buffer, byte[] select, byte[] read) throws IOException, CRC32Exception
     {
         ecuThread.getPage(buffer, select, read);
     }
 
     /**
      * Dumps a loaded page to SD card for analysis
      * 
      * @param pageNo
      * @param buffer
      */
     private void savePage(int pageNo, byte[] buffer)
     {
 
         try
         {
             File dir = new File(Environment.getExternalStorageDirectory(), "MSLogger");
 
             if (!dir.exists())
             {
                 boolean mkDirs = dir.mkdirs();
                 if (!mkDirs)
                 {
                     DebugLogManager.INSTANCE.log("Unable to create directory MSLogger at " + Environment.getExternalStorageDirectory(), Log.ERROR);
                 }
             }
 
             String fileName = ecuImplementation.getClass().getName() + ".firmware";
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
 
     /**
      * Write a constant back to the ECU
      * 
      * @param constant The constant to write
      */
     public void writeConstant(Constant constant)
     {
         List<String> pageIdentifiers = ecuImplementation.getPageIdentifiers();
         List<String> pageValueWrites = ecuImplementation.getPageValueWrites();
 
         // Ex: U08, S16
         String type = constant.getType();
 
         // 8 bits = 1 byte by default
         int size = 1;
         if (type.contains("16"))
         {
             size = 2; // 16 bits = 2 bytes
         }
 
         int pageNo = constant.getPage();
         int offset = constant.getOffset();
 
        double userValue = getField(constant.getName());

         double scale = constant.getScale();
         double translate = constant.getTranslate();
 
         int[] msValue = null;
 
         // Constant to write is of type scalar or bits
         if (constant.getClassType().equals("scalar") || constant.getClassType().equals("bits"))
         {
            userValue = getField(constant.getName());
             
             msValue = new int[1];
             msValue[0] = (int) (userValue / scale - translate);
         }
         // Constant to write to ECU is of type array
         else if (constant.getClassType().equals("array"))
         {
             int shape[] = MSUtilsShared.getArraySize(constant.getShape());
 
             int width = shape[0];
             int height = shape[1];
 
             // Vector
             if (height == -1)
             {
                 size *= width;
 
                 double[] vector = getVector(constant.getName());
                 msValue = new int[vector.length];
 
                 for (int x = 0; x < width; x++)
                 {
                     // Switch from user value to MS value
                     msValue[x] = (int) (vector[x] / scale - translate);
                 }
             }
             // Array
             else
             {
                 double[][] array = getArray(constant.getName());
                 int i = 0;
 
                 size *= width * height;
                 msValue = new int[width * height];
 
                 for (int y = 0; y < height; y++)
                 {
                     for (int x = 0; x < width; x++)
                     {
                         // Switch from user value to MS value
                         msValue[i++] = (int) (array[x][y] / scale - translate);
                     }
                 }
 
             }
         }
         
         // Make sure we have something to send to the MS
         if (msValue != null && msValue.length > 0)
         {
             String command = MSUtilsShared.HexStringToBytes(pageIdentifiers, pageValueWrites.get(pageNo - 1), offset, size, msValue, pageNo);
             byte[] byteCommand = MSUtils.INSTANCE.commandStringtoByteArray(command);
             
             DebugLogManager.INSTANCE.log("Writing to MS: command: " + command + " constant " + constant.getName() + " msValue: " + Arrays.toString(msValue) + " pageValueWrite: " + pageValueWrites.get(pageNo - 1) + " offset: " + offset + " count: " + size + " pageNo: " + pageNo, Log.DEBUG);
             
             List<byte[]> pageActivates = ecuImplementation.getPageActivates();
             
             try
             {
                 int delay = ecuImplementation.getPageActivationDelay();
                 
                 // MS1 use page select command
                 if (pageActivates.size() >= pageNo)
                 {
                     byte[] pageSelectCommand = pageActivates.get(pageNo - 1);
                     ECUConnectionManager.getInstance().writeCommand(pageSelectCommand, delay, ecuImplementation.isCRC32Protocol());
                 }
                 
                 ECUConnectionManager.getInstance().writeCommand(byteCommand, delay, ecuImplementation.isCRC32Protocol());
                 
                 Toast.makeText(this, "Writing constant " + constant.getName() + " to MegaSquirt", Toast.LENGTH_SHORT).show();
             }
             catch (IOException e)
             {
                 DebugLogManager.INSTANCE.logException(e);
             }
             
             burnPage(pageNo);
         }
         // Nothing to send to the MS, maybe unsupported constant type ?
         else
         {
             DebugLogManager.INSTANCE.log("Couldn't find any value to write, maybe unsupported constant type " + constant.getType(), Log.DEBUG);
         }
     }
 
     /**
      * Burn a page from MS RAM to Flash
      * 
      * @param pageNo The page number to burn
      */
     private void burnPage(int pageNo)
     {
         try
         {
             // Convert from page to table index that the ECU understand
             List<String> pageIdentifiers = ecuImplementation.getPageIdentifiers();
             
             String pageIdentifier = pageIdentifiers.get(pageNo - 1).replace("$tsCanId", "");
             
             byte tblIdx = (byte) MSUtilsShared.HexByteToDec(pageIdentifier);
             
             DebugLogManager.INSTANCE.log("Burning page " + pageNo + "(table index: " + tblIdx + ")", Log.DEBUG);
             
             // Send "b" command for the tblIdx
             ECUConnectionManager.getInstance().writeCommand(new byte[]{98, 0, tblIdx}, 0, ecuImplementation.isCRC32Protocol());
             
             Toast.makeText(this, "Burning page " + pageNo + " to MegaSquirt", Toast.LENGTH_SHORT).show();
         }
         catch (IOException e)
         {
             DebugLogManager.INSTANCE.logException(e);
         }
     }
     
     /**
      * Get an array from the ECU
      * 
      * @param channelName
      * @return
      */
     public double[][] getArray(String channelName)
     {
         double[][] value = { { 0 }, { 0 } };
         Class<?> c = ecuImplementation.getClass();
         try
         {
             Field f = c.getDeclaredField(channelName);
             value = (double[][]) f.get(ecuImplementation);
         }
         catch (Exception e)
         {
             DebugLogManager.INSTANCE.log("Failed to get array value for " + channelName, Log.ERROR);
         }
         return value;
     }
 
     /**
      * Get a vector from the ECU
      * 
      * @param channelName
      * @return
      */
     public double[] getVector(String channelName)
     {
         double[] value = { 0 };
         Class<?> c = ecuImplementation.getClass();
         try
         {
             Field f = c.getDeclaredField(channelName);
             value = (double[]) f.get(ecuImplementation);
         }
         catch (Exception e)
         {
             DebugLogManager.INSTANCE.log("Failed to get vector value for " + channelName, Log.ERROR);
         }
         return value;
     }
 
     /**
      * 
      * @param channelName
      * @return
      */
     public double getField(String channelName)
     {
         double value = 0;
         Class<?> c = ecuImplementation.getClass();
         try
         {
             Field f = c.getDeclaredField(channelName);
             value = f.getDouble(ecuImplementation);
         }
         catch (Exception e)
         {
             DebugLogManager.INSTANCE.log("Failed to get value for " + channelName, Log.ERROR);
         }
         return value;
     }
 
     /**
      * 
      * @param channelName
      * @param value
      */
     public void setField(String channelName, double value)
     {
         Class<?> c = ecuImplementation.getClass();
 
         try
         {
             Field f = c.getDeclaredField(channelName);
 
             if (f.getType().toString().equals("int"))
             {
                 f.setInt(ecuImplementation, (int) value);
             }
             else
             {
                 f.setDouble(ecuImplementation, value);
             }
         }
         catch (Exception e)
         {
             DebugLogManager.INSTANCE.log("Failed to set value to " + value + " for " + channelName, Log.ERROR);
         }
     }
 
     /**
      * 
      * @param number
      * @param decimals
      * @return
      */
     public double roundDouble(double number, int decimals)
     {
         double p = (double) Math.pow(10, decimals);
         number = number * p;
         double tmp = Math.round(number);
         return tmp / p;
     }
 
     /**
      * 
      * @param pageBuffer
      * @param offset
      * @param width
      * @param height
      * @param scale
      * @param translate
      * @param digits
      * @param signed
      * @return
      */
     public double[][] loadByteArray(byte[] pageBuffer, int offset, int width, int height, double scale, double translate, int digits, boolean signed)
     {
         double[][] destination = new double[width][height];
         int index = offset;
         for (int y = 0; y < height; y++)
         {
             for (int x = 0; x < width; x++)
             {
                 double value = signed ? MSUtils.INSTANCE.getSignedByte(pageBuffer, index) : MSUtils.INSTANCE.getByte(pageBuffer, index);
                 value = (value + translate) * scale;
                 destination[x][y] = this.roundDouble(value, digits);
                 index = index + 1;
             }
         }
         return destination;
     }
 
     /**
      * 
      * @param pageBuffer
      * @param offset
      * @param width
      * @param scale
      * @param translate
      * @param digits
      * @param signed
      * @return
      */
     public double[] loadByteVector(byte[] pageBuffer, int offset, int width, double scale, double translate, int digits, boolean signed)
     {
         double[] destination = new double[width];
         int index = offset;
         for (int x = 0; x < width; x++)
         {
             double value = signed ? MSUtils.INSTANCE.getSignedByte(pageBuffer, index) : MSUtils.INSTANCE.getByte(pageBuffer, index);
             value = (value + translate) * scale;
             destination[x] = this.roundDouble(value, digits);
             index = index + 1;
         }
 
         return destination;
     }
 
     /**
      * 
      * @param pageBuffer
      * @param offset
      * @param width
      * @param height
      * @param scale
      * @param translate
      * @param digits
      * @param signed
      * @return
      */
     public double[][] loadWordArray(byte[] pageBuffer, int offset, int width, int height, double scale, double translate, int digits, boolean signed)
     {
         double[][] destination = new double[width][height];
         int index = offset;
         for (int y = 0; y < height; y++)
         {
             for (int x = 0; x < width; x++)
             {
                 double value = signed ? MSUtils.INSTANCE.getSignedWord(pageBuffer, index) : MSUtils.INSTANCE.getWord(pageBuffer, index);
                 value = (value + translate) * scale;
                 destination[x][y] = this.roundDouble(value, digits);
                 index = index + 2;
             }
         }
 
         return destination;
     }
 
     /**
      * 
      * @param pageBuffer
      * @param offset
      * @param width
      * @param scale
      * @param translate
      * @param digits
      * @param signed
      * @return
      */
     public double[] loadWordVector(byte[] pageBuffer, int offset, int width, double scale, double translate, int digits, boolean signed)
     {
         double[] destination = new double[width];
         int index = offset;
         for (int x = 0; x < width; x++)
         {
             double value = signed ? MSUtils.INSTANCE.getSignedWord(pageBuffer, index) : MSUtils.INSTANCE.getWord(pageBuffer, index);
             value = (value + translate) * scale;
             destination[x] = this.roundDouble(value, digits);
             index = index + 2;
         }
 
         return destination;
 
     }
 
     /**
      * 
      * @param name
      * @return
      */
     public boolean isConstantExists(String name)
     {
         return MSECUInterface.constants.containsKey(name);
     }
 
     /**
      * 
      * @param name
      * @return
      */
     public Constant getConstantByName(String name)
     {
         return MSECUInterface.constants.get(name);
     }
 
     /**
      * 
      * @param name
      * @return
      */
     public OutputChannel getOutputChannelByName(String name)
     {
         return MSECUInterface.outputChannels.get(name);
     }
 
     /**
      * 
      * @param name
      * @return
      */
     public TableEditor getTableEditorByName(String name)
     {
         return MSECUInterface.tableEditors.get(name);
     }
 
     /**
      * 
      * @param name
      * @return
      */
     public CurveEditor getCurveEditorByName(String name)
     {
         return MSECUInterface.curveEditors.get(name);
     }
 
     /**
      * 
      * @param name
      * @return
      */
     public List<Menu> getMenusForDialog(String name)
     {
         return MSECUInterface.menus.get(name);
     }
 
     /**
      * 
      * @param name
      * @return
      */
     public MSDialog getDialogByName(String name)
     {
         return MSECUInterface.dialogs.get(name);
     }
 
     /**
      * 
      * @param name
      * @return
      */
     public boolean getUserDefinedVisibilityFlagsByName(String name)
     {
         if (MSECUInterface.userDefinedVisibilityFlags.containsKey(name))
         {
             return MSECUInterface.userDefinedVisibilityFlags.get(name);
         }
 
         return true;
     }
 
     /**
      * 
      * @param name
      * @return
      */
     public boolean getMenuVisibilityFlagsByName(String name)
     {
         return MSECUInterface.menuVisibilityFlags.get(name);
     }
 
     /**
      * 
      * @param dialog
      */
     public void addDialog(MSDialog dialog)
     {
         MSECUInterface.dialogs.put(dialog.getName(), dialog);
     }
 
     /**
      * 
      * @param curve
      */
     public void addCurve(CurveEditor curve)
     {
         MSECUInterface.curveEditors.put(curve.getName(), curve);
     }
 
     /**
      * 
      * @param constant
      */
     public void addConstant(Constant constant)
     {
         MSECUInterface.constants.put(constant.getName(), constant);
     }
 
     /**
      * Used to get a list of all constants name used in a specific dialog
      * 
      * @param dialog The dialog to get the list of constants name
      * @return A list of constants name
      */
     public List<String> getAllConstantsNamesForDialog(MSDialog dialog)
     {
         List<String> constants = new ArrayList<String>();
         return buildListOfConstants(constants, dialog);
     }
 
     /**
      * Helper function for getAllConstantsNamesForDialog() which builds the array of constants name
      * 
      * @param constants
      * @param dialog
      */
     private List<String> buildListOfConstants(List<String> constants, MSDialog dialog)
     {
         for (DialogField df : dialog.getFieldsList())
         {
             if (!df.getName().equals("null"))
             {
                 constants.add(df.getName());
             }
         }
 
         for (DialogPanel dp : dialog.getPanelsList())
         {
             MSDialog dialogPanel = this.getDialogByName(dp.getName());
 
             if (dialogPanel != null)
             {
                 buildListOfConstants(constants, dialogPanel);
             }
         }
 
         return constants;
     }
 
     /**
      * 
      * @return
      */
     public String[] defaultGauges()
     {
         return ecuImplementation.defaultGauges();
     }
 
     /**
      * 
      * @return
      */
     public int getBlockSize()
     {
         return ecuImplementation.getBlockSize();
     }
 
     /**
      * 
      * @return
      */
     public int getCurrentTPS()
     {
         return ecuImplementation.getCurrentTPS();
     }
 
     /**
      * 
      * @return
      */
     public String getLogHeader()
     {
         return ecuImplementation.getLogHeader();
     }
 
     /**
      * 
      */
     public void initGauges()
     {
         ecuImplementation.initGauges();
     }
 
     /**
      * 
      */
     public void refreshFlags()
     {
         ecuImplementation.refreshFlags();
     }
 
     /**
      * 
      */
     public void setMenuVisibilityFlags()
     {
         ecuImplementation.setMenuVisibilityFlags();
     }
 
     /**
      * 
      */
     public void setUserDefinedVisibilityFlags()
     {
         ecuImplementation.setUserDefinedVisibilityFlags();
 
     }
 
     /**
      * 
      * @return
      */
     public String[] getControlFlags()
     {
         return ecuImplementation.getControlFlags();
     }
 
     /**
      * 
      * @return
      */
     public List<String> getRequiresPowerCycle()
     {
         return ecuImplementation.getRequiresPowerCycle();
     }
 
     public List<SettingGroup> getSettingGroups()
     {
         ecuImplementation.createSettingGroups();
         return ecuImplementation.getSettingGroups();
     }
 
     /**
      * Helper functions to get specific value out of ECU Different MS version have different name for the same thing so get the right one depending on
      * the MS version we're connected to
      */
 
     /**
      * @return Return the current ECU cylinders count
      */
     public int getCylindersCount()
     {
         return (int) (isConstantExists("nCylinders") ? getField("nCylinders") : getField("nCylinders1"));
     }
 
     /**
      * @return Return the current ECU injectors count
      */
     public int getInjectorsCount()
     {
         return (int) (isConstantExists("nInjectors") ? getField("nInjectors") : getField("nInjectors1"));
     }
 
     /**
      * @return Return the current ECU divider
      */
     public int getDivider()
     {
         return (int) (isConstantExists("divider") ? getField("divider") : getField("divider1"));
     }
 
     /**
      * Return the current ECU injector staging
      * 
      * @return 0 = Simultaneous, 1 = Alternating
      */
     public int getInjectorStating()
     {
         return (int) (isConstantExists("alternate") ? getField("alternate") : getField("alternate1"));
     }
 
 }
