 package com.stfalcon.mtpclient;
 
 
 import android.app.Service;
 import android.content.Context;
 import android.content.Intent;
 import android.os.AsyncTask;
 import android.os.IBinder;
 import android.util.Log;
 
 import java.io.DataInputStream;
 import java.io.DataOutputStream;
 import java.io.IOException;
 import java.net.InetAddress;
 import java.net.Socket;
 import java.util.HashMap;
 
 /**
  * Created by user on 7/19/13.
  */
 public class TCPLink extends Service {
     private static final String HOST = "95.142.192.65";
     public static TCPLink self;
 
     public static TCPLink getInstance() {
         if (self == null) {
             self = new TCPLink();
         }
         return self;
     }
 
     @Override
     public void onCreate() {
         super.onCreate();
 
         startService();
     }
 
     @Override
     public IBinder onBind(Intent intent) {
         return null;
     }
 
     @Override
     public void onDestroy() {
     }
 
     // Здесь выполняем инициализацию нужных нам значений
 // и открываем наше сокет-соединение
     private void startService() {
         /*Log.i("Loger", "Start Service");
         try {
             openConnection();
 
         } catch (InterruptedException e) {
             e.printStackTrace();
         }*/
     }
 
     public void sendReqPqRequest() {
         try {
 
             // PutData - это класс, с помощью которого мы передадим параметры в
             // создаваемый поток
             PutData data = new PutData();
             data.request = RequestBuilder.createReq_PqRequest();
             data.context = this;
             // создаем новый поток для сокет-соединения
             new ToSocket().execute(data);
         } catch (Exception e) {
             e.printStackTrace();
         }
     }
 
     public void sendReq_DH_params(HashMap<String, Object> hashMap) {
         try {
             Log.v("LOGER", "START");
             // PutData - это класс, с помощью которого мы передадим параметры в
             // создаваемый поток
             PutData data = new PutData();
             data.request = RequestBuilder.createReq_DHRequest(hashMap);
             data.context = this;
             // создаем новый поток для сокет-соединения
             new ToSocket().execute(data);
             Log.v("LOGER", "START EXE");
         } catch (Exception e) {
             e.printStackTrace();
         }
     }
 
     public void sendSet_DH_params(HashMap<String, Object> hashMap) {
         try {
             Log.v("LOGER", "START");
             // PutData - это класс, с помощью которого мы передадим параметры в
             // создаваемый поток
             PutData data = new PutData();
             data.request = RequestBuilder.create_Set_client_DHRequest(hashMap);
             data.context = this;
             // создаем новый поток для сокет-соединения
             new ToSocket().execute(data);
             Log.v("LOGER", "START EXE");
         } catch (Exception e) {
             e.printStackTrace();
         }
     }
 
     class PutData {
         byte[] request;
         Context context;
     }
 
     class ToSocket extends AsyncTask<PutData, Integer, Integer> {
         Context mCtx;
         Socket mySock;
 
         protected void onProgressUpdate(Integer... progress) {
         }
 
         protected void onPostExecute(Integer result) {
             Log.v("SEND_DATA", "done");
             // Это выполнится после завершения работы потока
         }
 
         protected void onCancelled(Integer result) {
             Log.v("SEND_DATA", "canceled");
             // Это выполнится после завершения работы потока
         }
 
         protected Integer doInBackground(PutData... param) {
             Log.v("SEND_DATA", "start");
             InetAddress serverAddr;
 
             mCtx = param[0].context;
             byte[] toServer = param[0].request;
 
             try {
                 // while (true) {
                 serverAddr = InetAddress.getByName(HOST);
                 if (mySock == null || mySock.isClosed()) {
                     mySock = new Socket(serverAddr, 80);
                 }
 
                 // открываем сокет-соединение
                 SocketData data = new SocketData();
                 data.context = mCtx;
                 data.socket = mySock;
 
                 // Еще один поток, именно он будет принимать входящие сообщения
                 GetPacket pack = new GetPacket();
                 AsyncTask<SocketData, Integer, Integer> running = pack.execute(data);
 
 
                 // Посылаем request на сервер
                 try {
                     Log.v("SEND_DATA", "send " + Utils.byteArrayToHex(toServer));
                     DataOutputStream dos = new DataOutputStream(mySock.getOutputStream());
                     dos.write(toServer);
                     dos.flush();
                     //out.println(message);
                 } catch (Exception e) {
                     e.printStackTrace();
                     while (running.getStatus().equals(AsyncTask.Status.RUNNING)) ;
                     Log.v("SEND_DATA", "fail thread");
                 }  // Следим за потоком, принимающим сообщения
                 // Если поток закончил принимать сообщения - это означает,
                 // что соединение разорвано (других причин нет).
                 // Это означает, что нужно закрыть сокет
                 // и открыть его опять в бесконечном цикле (см. while(true) выше)
                 try {
                     //mySock.close();
                 } catch (Exception e) {
                 }
                 return 1;
                 // }
             } catch (Exception e) {
                 e.printStackTrace();
                 return -1;
             }
         }
     }
 
     class SocketData {
         Socket socket;
         Context context;
     }
 
     class GetPacket extends AsyncTask<SocketData, Integer, Integer> {
         Context mCtx;
         byte[] mData;
         Socket mySock;
         int z = 0;
 
         protected void onProgressUpdate(Integer... progress) {
 
         }
 
         protected void onPostExecute(Integer result) {
             // Это выполнится после завершения работы потока
             Log.v("GET_DATA", "done");
             try {
                 // Получаем принятое от сервера сообщение
                 //String prop = String.valueOf(mData);
                 Log.v("GET_DATA", "data: " + Utils.byteArrayToHex(mData));
                HashMap<String, Object> responseMap = Parser.parseReqPqResponse(mData);
                if ((Integer) responseMap.get(Parser.TYPE) == Parser.TYPE_RES_PQ) {
                     sendReq_DH_params(responseMap);
                } else if ((Integer) responseMap.get(Parser.TYPE) == Parser.TYPE_RES_DH) {
                     //send(responseMap);
                 }
 
             } catch (Exception e) {
                 MTPapp.showToastMessage("Socket error: " + e.getMessage());
                 e.printStackTrace();
             }
         }
 
         protected void onCancelled(Integer result) {
             // Это выполнится после завершения работы потока
             Log.v("GET_DATA", "canceled");
         }
 
         protected Integer doInBackground(SocketData... param) {
             Log.v("GET_DATA", "start get thread");
             mySock = param[0].socket;
             mCtx = param[0].context;
             mData = new byte[4096];
 
             try {
                 DataInputStream reader = new DataInputStream(mySock.getInputStream());
                 int read = 0;
                 // Принимаем сообщение от сервера
                 // Данный цикл будет работать, пока соединение не оборвется
                 // или внешний поток не скажет данному cancel()
                 while (((read = reader.read(mData)) >= 0 && !isCancelled() && z == 0)) {
                     // "Вызываем" onProgressUpdate каждый раз, когда принято сообщение
                     Log.v("GET_DATA", "read data");
                     //read = reader.read(mData);
                     if (read > 0) return read;
                 }
                 reader.close();
             } catch (IOException e) {
                 e.printStackTrace();
                 return -1;
             } catch (Exception e) {
                 e.printStackTrace();
                 return -1;
             }
             return 0;
         }
     }
 }
 
 	 
 
 	
