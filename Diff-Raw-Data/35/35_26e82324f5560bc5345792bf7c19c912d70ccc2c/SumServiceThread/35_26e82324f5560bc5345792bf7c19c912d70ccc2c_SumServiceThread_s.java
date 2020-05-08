 package hoj_harjoitus_osa1;
 
 import java.io.InputStream;
 import java.io.ObjectInputStream;
 import java.net.ServerSocket;
 import java.net.Socket;
 
 
 public class SumServiceThread extends Thread {
     
     private Socket sumSocket;
     private ServerSocket sumServer;
     private int port;
     private int orderN;
     private boolean stopread = false;
     /* How many numbers we have summed in total */
     private int numbers_summed = 0;
 
     public SumServiceThread(int n, int port_num) {
 	port = port_num;
         orderN = n;
         try {
             sumServer = new ServerSocket(port);
         }
         catch(Exception e) {
             System.out.println("Error:" + e);
         }
     }
 
     public void run() {
         
         try {
             sumSocket = sumServer.accept();	
             InputStream inS = sumSocket.getInputStream();
             ObjectInputStream oIn = new ObjectInputStream(inS);
 
             while(!stopread) {
                int i = oIn.readInt();
                 if(i == 0) {
                     stopread = true;
                 }
                 else {
                     numbers_summed++;
                     SumServiceManager.sumArray[orderN] =+ i;
                     SumServiceManager.sumCounterArray[orderN]++;
                 }
             }
 
             oIn.close();
             sumSocket.close();
             sumServer.close();
         } catch(Exception e) {
             System.out.println("Error:" + e);
         }
     }
     
     public int getPort() {
         return port;
     }
     
     public int getNumbersSummed() {
         return numbers_summed;
     }
 }
