 
 /**
  * Write a description of class Main here.
  * 
  * @author (your name) 
  * @version (a version number or a date)
  */
 
 import java.io.*;
 import java.net.*;
 import java.util.Scanner;
 
 public class Main
 {
     public static final int COLUMN_SIZE = 16;
     public static final String LOCAL_BROADCAST_ADDRESS = "172.18.114.255";
     
     private static int PORT = 9875;
     private UDPListener udpListener;
     private UserInputThread uiThread;
     public static void main(String args[])
     {
     if(args.length >= 1)
        try
        {
            PORT = Integer.parseInt(args[0]);
            if(PORT >= 65536 || PORT <= 0)
                throw new Exception("Port Error: "+ args[0]);
        }
        catch(Exception e)
        {
             System.err.println(e.getMessage());
             System.err.println("Using default port number: "+PORT);
        }
         Main m = new Main(PORT);
         m.startThreads();
     }
     public Main(int port)
     {
         udpListener = new UDPListener(port);
         uiThread = new UserInputThread(udpListener);
     }
     public void startThreads()
     {
         udpListener.start();
         uiThread.start();
         while(udpListener.getStatus() != 4);
         UDPSender udpSender = new UDPSender(PORT, "Hello World!");
         udpSender.start();
     }
     private class UDPListener extends Thread
     {
         int port;
         int status = 0;
         boolean run = true;
         public UDPListener(int port)
         {
             this.port = port;
             setStatus(1);
         }
         public synchronized int getStatus()
         {
             return status;
         }
         public synchronized void setStatus(int status)
         {
             this.status = status;
         }
         public synchronized boolean isRun()
         {
             return run;
         }
         public synchronized void setRun(boolean runn)
         {
             run = runn;
         }
         public void run()
         {
             setStatus(2);
             System.out.println("Starting "+this.getClass().toString());
             try
             {  
                 DatagramSocket serverSocket = new DatagramSocket(port);
                 setStatus(3);
                 byte[] receiveData = new byte[1024];
                 byte[] sendData = new byte[1024];
                 while(isRun())
                 {
                     setStatus(4);
                     try
                     {
                         DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
                         serverSocket.receive(receivePacket);
                         String sentence = new String( receivePacket.getData());
                         System.out.println("RECEIVED: " + receivePacket.getLength() + " bytes");
                         printAsciiHex(receivePacket.getData(), receivePacket.getLength());
                         InetAddress IPAddress = receivePacket.getAddress();
                         String capitalizedSentence = sentence.toUpperCase();
                         //UDPSender replier = new UDPSender(PORT, capitalizedSentence);
                         //replier.start();
                     }
                     catch(Exception e)
                     {
                         e.printStackTrace();
                     }
                 }
                 System.out.println("Exiting "+this.getClass().toString());
             }
             catch(Exception e)
             {
                 e.printStackTrace();
             }
         }
     }
     private class UDPSender extends Thread
     {
         int port;
         String message;
         public UDPSender(int port, String message)
         {
             this.port = port;
             this.message = message;
         }
         public void run()
         {
             System.out.println("Starting "+this.getClass().toString());
             try
             {
                 DatagramSocket clientSocket = new DatagramSocket();
                 InetAddress IPAddress = InetAddress.getByName(LOCAL_BROADCAST_ADDRESS);
                byte[] sendData = new byte[1024];
                byte[] receiveData = new byte[1024];
                String sentence = new String(message);
                sendData = sentence.getBytes();
                DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, IPAddress, port);
                 clientSocket.send(sendPacket);
                 String modifiedSentence = new String(sendPacket.getData());
                System.out.println("FROM SERVER:" + modifiedSentence);
                 clientSocket.close();
             }
             catch(Exception e)
             {
                 e.printStackTrace();
             }
         }
     }
     private class UserInputThread extends Thread
     {
         UDPListener server;
         boolean myRun = true;
         public UserInputThread(UDPListener server)
         {
             this.server = server;
         }
         public void run()
         {
             System.out.println("waiting input (exit)");
             Scanner scan = new Scanner(System.in);
             while(myRun)
             {
                 System.out.println("waiting input (exit)");
                 String line = scan.nextLine();
                 if(line.equalsIgnoreCase("exit"))
                 {
                     server.setRun(false);
                     myRun = false;
                 }
                 else
                 {
                     UDPSender sender = new UDPSender(PORT, line);
                     sender.start();
                 }
             }
         }
     }
     private static void printAsciiHex(byte[] buffer, int nread)
     {
         int i,j;
         for(i = 0; i<COLUMN_SIZE; i++)
         {
             System.out.printf("%02x", i & (0xFF));
             if(i%2==1 && i!=0)
                 System.out.printf(" ");
         }
         System.out.println();
         for (i = 0; i<COLUMN_SIZE; i++)
         {
             System.out.printf("--", i & (0xFF));
             if(i%2==1 && i!=0)
                 System.out.printf("-");
         }
         System.out.println();
         for(i = 1; i <= nread; i++)
         {
             System.out.printf("%02x", buffer[i - 1] & (0xFF));
             if(i%2==0 && i!=0)
                 System.out.printf(" ");
             if(i%COLUMN_SIZE == 0)
             {
                 System.out.print('\t');
                 for(j=i-COLUMN_SIZE; j<i; j++)
                     {
                         if(buffer[j] >= 32 && buffer[j] <= 126)
                             System.out.print((char)buffer[j]);
                         else
                             System.out.print('.');
                     }
                 System.out.print('\n');
             }
             else if(i==nread)
             {
 
                 for(j=0; j<(COLUMN_SIZE-(nread%COLUMN_SIZE))*3; j++)
                     System.out.print(' ');
                 System.out.print('\t');
                 for(j=(nread/COLUMN_SIZE)*COLUMN_SIZE; j<nread; j++)
                 {
                     if(buffer[j] >= 32 && buffer[j] <= 126)
                         System.out.print((char)buffer[j]);
                     else
                         System.out.print(".");
                 }
                 System.out.print('\n');
             }
         }
         System.out.print('\n');
     }
 }
