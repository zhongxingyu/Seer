 package nl.vu.cs.cn.app;
 
 import android.app.ProgressDialog;
 import android.content.Context;
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import android.graphics.Canvas;
 import android.os.AsyncTask;
 import android.util.Log;
 import android.widget.Toast;
 
 import java.io.ByteArrayOutputStream;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.nio.ByteBuffer;
 
 import nl.vu.cs.cn.IP;
 import nl.vu.cs.cn.R;
 import nl.vu.cs.cn.TCP;
 
 public class TCPManager {
 
     private static final String TAG = "TCPManager";
 
     private static final int CLIENT_ADDR_LAST_OCTET  = 20;
     private static final int SERVER_ADDR_LAST_OCTET  = 21;
     private static final int SERVER_PORT             = 221;
     private static final IP.IpAddress SERVER_IP_ADDR = IP.IpAddress.getAddress("192.168.0." + SERVER_ADDR_LAST_OCTET);
 
 
     private TCP tcp;
     private TCP.Socket socket;
     private final TCPListener listener;
     private final boolean isServer;
 
     private final Context context;
     private ReceiveTask receiveTask;
 
     public TCPManager(Context context, boolean isServer, TCPListener listener){
         this.context = context;
         this.isServer = isServer;
         this.listener = listener;
 
         try {
             tcp = new TCP(isServer ? SERVER_ADDR_LAST_OCTET : CLIENT_ADDR_LAST_OCTET);
             if(isServer){
                 socket = tcp.socket(SERVER_PORT);
             } else {
                 socket = tcp.socket();
             }
         } catch (IOException e) {
             Log.e(TAG, "Error initializing TCP stacks", e);
             Toast.makeText(context, context.getString(R.string.failed_initializing_msg), Toast.LENGTH_SHORT).show();
         }
     }
 
     public void open(){
         new OpenTask().execute();
     }
 
     public void send(String msg){
         new SendTask().execute(msg);
     }
 
     public void sendImage(String filePath){
         new SendImageTask().execute(filePath);
     }
 
     public void close(){
         if(receiveTask != null){
             receiveTask.cancel(true);
         }
 
         new CloseTask().execute();
     }
 
     private class OpenTask extends AsyncTask<Void, Void, Boolean> {
 
         private ProgressDialog dialog;
 
         @Override
         protected void onPreExecute() {
             if(!isServer){
                 // make sure we only show one dialog
                 dialog = ProgressDialog.show(context,
                         context.getString(R.string.connecting_dialog_title),
                         context.getString(R.string.connecting_dialog_msg));
             }
         }
 
         @Override
         protected Boolean doInBackground(Void... voids) {
             if(isServer){
                 socket.accept();
             } else {
                 return socket.connect(SERVER_IP_ADDR, SERVER_PORT);
             }
             return true;
         }
 
         @Override
         protected void onPostExecute(Boolean connected) {
             if(dialog != null && dialog.isShowing()){
                 dialog.dismiss();
                 dialog = null;
             }
 
             if(connected){
                 receiveTask = new ReceiveTask();
                 receiveTask.execute();
                 listener.onConnected(isServer);
             } else {
                 listener.onConnectionFailed(isServer);
             }
         }
     }
 
     private class SendTask extends AsyncTask<String, Void, Boolean> {
 
         @Override
         protected Boolean doInBackground(String... messages) {
             byte[] buf = messages[0].getBytes();
             return (socket.write(buf, 0, buf.length) == buf.length);
         }
 
         @Override
         protected void onPostExecute(Boolean received) {
             if(!received){
                 listener.onSendFailed(isServer);
             }
         }
     }
 
     private class SendImageTask extends AsyncTask<String, Void, Boolean> {
 
         @Override
         protected Boolean doInBackground(String... filePath) {
             // "image protocol: start with message <image>, send image data, send </image>"
             byte[] open = "<image>".getBytes();
             if(socket.write(open, 0, open.length) != open.length){
                 // todo handle errors
                 return false;
             }
 
             Bitmap bitmap = decodeFile(filePath[0]);
             // TODO: bitmap could be null
 
             // compress image
             ByteArrayOutputStream stream = new ByteArrayOutputStream();
             bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
             byte[] buf = stream.toByteArray();
 
             // send image size
             socket.write(ByteBuffer.allocate(4).putInt(buf.length).array(), 0, 4);
 
             // send image
             if(socket.write(buf, 0, buf.length) != buf.length){
                 // todo handle errors
                 return false;
             }
 
             return true;
         }
 
         @Override
         protected void onPostExecute(Boolean received) {
             if(!received){
                 listener.onSendFailed(isServer);
             }
         }
     }
 
     private class ReceiveTask extends AsyncTask<Void, Object, Void> {
 
         @Override
         protected Void doInBackground(Void... voids) {
             byte[] buf = new byte[1024];
             int len;
             while((len = socket.read(buf, 0, buf.length)) > 0){
                 String msg = new String(buf, 0, len);
 
                 if(msg.equals("<image>")){
                     Log.v(TAG, "GETTING IMAGE");
 
                     // read image length
                     byte[] imgLenBuf = new byte[4];
                     socket.read(imgLenBuf, 0, 4);
                     int imageSize = ByteBuffer.wrap(imgLenBuf).getInt();
 
                     Log.v(TAG, "IMAGE SIZE in bytes: " + imageSize);
 
                     // we are getting an image, so completely receive it
                     byte[] imgBuf = new byte[imageSize];
                    int receivedData = 0;
                    while((receivedData += socket.read(imgBuf, receivedData, imageSize-receivedData)) < imageSize){
                        // read until we have the complete image
                    }
 
                     Bitmap bitmap;
                    bitmap = BitmapFactory.decodeByteArray(imgBuf, 0, imageSize);
 
                     publishProgress(bitmap);
                 } else {
                     publishProgress(msg);
                 }
             }
             return null;
         }
 
         @Override
         protected void onProgressUpdate(Object... values) {
             if(values[0] instanceof String){
                 listener.onMessage(isServer, (String)values[0]);
             } else if(values[0] instanceof Bitmap){
                 listener.onImage(isServer, (Bitmap) values[0]);
             }
         }
     }
 
     private class CloseTask extends AsyncTask<Void, Void, Boolean> {
 
         @Override
         protected Boolean doInBackground(Void... voids) {
             return socket.close();
         }
 
         @Override
         protected void onPostExecute(Boolean closed) {
             if(closed){
                 listener.onClosed(isServer);
             } else {
                 Log.w(TAG, "Failed to close connection");
             }
         }
     }
 
     private Bitmap decodeFile(String filePath){
 
         try {
             File f = new File(filePath);
 
             //Decode image size
             BitmapFactory.Options o = new BitmapFactory.Options();
             o.inJustDecodeBounds = true;
             BitmapFactory.decodeStream(new FileInputStream(f),null,o);
 
             //The new size we want to scale to
             final int REQUIRED_SIZE=70;
 
             //Find the correct scale value. It should be the power of 2.
             int scale=1;
             while(o.outWidth/scale/2>=REQUIRED_SIZE && o.outHeight/scale/2>=REQUIRED_SIZE)
                 scale*=2;
 
             //Decode with inSampleSize
             BitmapFactory.Options o2 = new BitmapFactory.Options();
             o2.inSampleSize=scale;
             return BitmapFactory.decodeStream(new FileInputStream(f), null, o2);
         } catch (FileNotFoundException e) {}
         return null;
     }
 }
