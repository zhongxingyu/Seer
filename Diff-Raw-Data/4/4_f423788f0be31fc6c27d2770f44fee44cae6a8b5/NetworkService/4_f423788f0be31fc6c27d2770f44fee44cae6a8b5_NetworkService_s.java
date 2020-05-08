 package pro.kornev.kcar.cop.services;
 
 import android.app.Service;
 import android.content.Intent;
 import android.os.IBinder;
 import android.widget.Toast;
 
 import java.io.DataInputStream;
 import java.io.DataOutputStream;
 import java.net.Socket;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Queue;
 
 import pro.kornev.kcar.cop.State;
 import pro.kornev.kcar.cop.Utils;
 import pro.kornev.kcar.cop.providers.LogsDB;
 import pro.kornev.kcar.protocol.Data;
 import pro.kornev.kcar.protocol.Protocol;
 
 /**
  * @author vkornev
  * @since 17.10.13
  */
 public class NetworkService extends Service {
     private static final int PROXY_PORT = 6780;
     private static final int PROXY_RECONNECT_TIMEOUT = 10000;
     private LogsDB db;
     private static List<NetworkListener> listeners = new ArrayList<NetworkListener>();
     private Thread controllerThread;
     private boolean writerRunning = false;
 
     @Override
     public IBinder onBind(Intent intent) {
         return null;
     }
 
     @Override
     public void onCreate() {
         super.onCreate();
         db = new LogsDB(getApplicationContext());
     }
 
     @Override
     public int onStartCommand(Intent intent, int flags, int startId) {
         super.onStartCommand(intent, flags, startId);
         try {
             if (controllerThread != null && controllerThread.isAlive()) {
                 db.putLog("NS Start error: controller already run");
                 return START_NOT_STICKY;
             }
             controllerThread = new Thread(new Controller());
             controllerThread.start();
         } catch (Exception e) {
             db.putLog("NS start error: " + e.getMessage());
             e.printStackTrace();
         }
         Toast.makeText(this, "Network service starting", Toast.LENGTH_SHORT).show();
         db.putLog("NS Is running");
         return START_STICKY;
     }
 
     class Controller implements Runnable {
         private volatile Socket socket;
 
         @Override
         public void run() {
             Cleaner cleaner = null;
             while (State.isServiceRunning()) {
                 db.putLog("NS Starting cleaner");
                 if (cleaner == null) {
                     cleaner = new Cleaner();
                     new Thread(cleaner).start();
                 }
                 db.putLog("NS Connect to: " + State.getProxyServer() + ":" + PROXY_PORT);
                 try {
                     setSocket(new Socket(State.getProxyServer(), PROXY_PORT));
                 } catch (Exception e) {
                     db.putLog("NS Failed connect to server: " + e.getMessage());
                     /** wait {@link NetworkService#PROXY_RECONNECT_TIMEOUT} seconds and if isServiceRunning then try reconnect */
                     Utils.sleep(PROXY_RECONNECT_TIMEOUT);
                     continue;
                 }
 
                 try {
                     db.putLog("NS Stopping cleaner");
                     cleaner.stop();
                     cleaner = null;
 
                     setWriterRunning(true);
                     Writer writer = new Writer(socket);
                     Thread writerThread = new Thread(writer);
                     writerThread.start();
                     Reader reader = new Reader(socket);
                     Thread readerThread = new Thread(reader);
                     readerThread.start();
 
                     readerThread.join(); // Work wile reader is working
                    closeSocket(socket); // Close socket and waite while writer is closed
                     writerThread.join(); // Wait while writer was stopped
                     db.putLog("NS reader and writer was closed");
                 } catch (Exception e) {
                     db.putLog("NS run reader and writer was filed: " + e.getMessage());
                 }
             }
         }
 
         public synchronized void setSocket(Socket socket) {
             this.socket = socket;
         }
     }
 
     class Reader implements Runnable {
         private volatile Socket client;
 
         Reader(Socket s) {
             client = s;
         }
 
         @Override
         public void run() {
             db.putLog("NR Start network reader");
             try {
                 DataInputStream input = new DataInputStream(client.getInputStream());
                 while (State.isServiceRunning()) {
                     Data data = Protocol.fromInputStream(input);
 
                     db.putLog(String.format("NR got data id: %d; cmd: %d", data.id, data.cmd));
                     if (data.cmd == Protocol.Cmd.ping() && data.bData == 0) {
                         Data response = new Data();
                         response.id = data.id;
                         response.cmd = data.cmd;
                         response.type = data.type;
                         response.bData = 2;
                         State.getToControlQueue().add(response);
                     }
                     for (NetworkListener l: listeners) {
                         l.onDataReceived(data);
                     }
                 }
             } catch (Exception e) {
                 db.putLog("NR error: " + e.getMessage());
                 e.printStackTrace();
             }
             setWriterRunning(false);
             db.putLog("NR Stop network reader");
             closeSocket(client);
         }
     }
 
     class Writer implements Runnable {
         private volatile Socket client;
         private Queue<Data> queue;
         private int id = 0;
 
         Writer(Socket s) {
             client = s;
             queue = State.getToControlQueue();
         }
 
         @Override
         public void run() {
             db.putLog("NW Start network writer");
             try {
                 DataOutputStream output = new DataOutputStream(client.getOutputStream());
                 while (isWriterRunning()) {
                     if (queue.isEmpty()) {
                         Utils.sleep(1);
                         continue;
                     }
                     Data data = queue.poll();
                     data.id = id++;
                     db.putLog(String.format("NW wrote date id: %d; cmd: %d", data.id, data.cmd));
 
                     Protocol.toOutputStream(data, output);
                 }
                 if (!client.isClosed()) {
                     client.close();
                 }
             } catch (Exception e) {
                 db.putLog("NW error: " + e.getMessage());
             }
             db.putLog("NW Stop network writer");
             closeSocket(client);
         }
     }
 
     class Cleaner implements Runnable {
         private boolean isWork = true;
 
         @Override
         public void run() {
             Queue<Data> queue = State.getToControlQueue();
             while (State.isServiceRunning() && isWork()) {
                 db.putLog("NC Clear queue");
                 queue.clear();
                 Utils.sleep(1000);
             }
             db.putLog("NC Was closed");
         }
 
         private synchronized boolean isWork() {
             return isWork;
         }
 
         public synchronized void stop() {
             isWork = false;
         }
     }
 
     @Override
     public void onDestroy() {
         Toast.makeText(this, "Network service done", Toast.LENGTH_SHORT).show();
         State.setServiceRunning(false);
     }
 
     public static void addListener(NetworkListener listener) {
         listeners.add(listener);
     }
 
     public static void removeAllListeners() {
         listeners.clear();
     }
 
     private synchronized boolean isWriterRunning() {
         return writerRunning;
     }
 
     public void setWriterRunning(boolean writerRunning) {
         this.writerRunning = writerRunning;
     }
 
     private synchronized void closeSocket(Socket socket) {
         if (!socket.isClosed()) {
             try {
                 socket.close();
             } catch (Exception ignored) {
             }
         }
     }
 }
