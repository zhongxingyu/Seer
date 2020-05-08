 import java.io.BufferedOutputStream;
 import java.net.Socket;
 
 /*
  * Created on Aug 4, 2006 11:54:28 AM
  * Updated on Sep 26, 2012 4:55:00 PM
  *
  * Copyright (c) 2012 Voxeo Labs. All rights reserved.
  *
  * Blasts a set of syslog (TCP) messages
  * to load test a log server.
  *
  * Supports passing in a number of threads, messages and the hostname.
  *
  * Example: java LogSmasher <threads> <count> <host> <port>
  *          java LogSmasher 2 1000 127.0.0.1 1234
  *
  * Note: For each message loop we actually write 3 messages for different accounts.
  *
  */
 
 public class LogSmasher  extends Thread
 {
 
     private final int m_message_count;
     private final int m_account_count;
     private final String m_host;
     private final int m_port;
     public LogSmasher(int message_count,int account_count,String host,int port)
     {
         m_message_count = message_count;
         m_account_count = account_count;
         m_host  = host;
         m_port  = port;
     }
 
     @Override
     public void run()
     {
         long startTime = System.currentTimeMillis();
         System.out.println("Starting thread");
         String message1 = "<13>Aug  4 16:04:32 b-vmt136.orl.voxeo.net MOT ";
         String message2 = "/7faa8f9bef839e289a2fb33b612ede19/94e41416950eb9010bec91921f5401e3/1/x/16:04:32.481: PROMPTLIST:http:\\s\\sapp.silverlink.com\\sshared\\srev2_1\\ssysaudio\\schime.wav\r\n";
         byte[] b1 = message1.getBytes();
         byte[] b2 = message2.getBytes();
         try
         {
             Socket s = new Socket(m_host,m_port);
             BufferedOutputStream bos = new BufferedOutputStream(s.getOutputStream());
 
             int i=0;
             while (i < m_message_count)
             {
                 i++;
                 bos.write(b1);
                 bos.write(new Long(1+Math.round(m_account_count*Math.random())).toString().getBytes());
                 bos.write(b2);
             }
             bos.close();
             s.close();
         }
         catch (Exception e)
         {
             e.printStackTrace();
         }
         long endTime = System.currentTimeMillis();
         System.out.println("Ending thread - " + (endTime-startTime));
     }
 
     public static void main(String[] args)
     {
 
         int threads       =  1;
         int message_count =  100000;
         int account_count =  10000;
         int port          =  1234;
         String host       =  "127.0.0.1";
 
         try
         {
             threads = Integer.parseInt(args[0]);
             message_count = Integer.parseInt(args[1]);
            account_count = Integer.parseInt(args[2]);
            host = args[3];
             port = Integer.parseInt(args[3]);
         }
         catch (Exception e)
         {
         }
 
         System.out.println("Starting LogSmasher");
         System.out.println("--------------------");
         System.out.println(" - Host: " + host);
         System.out.println(" - Port: " + port);
         System.out.println(" - Threads: " + threads);
         System.out.println(" - Msg Count: " + message_count);
         System.out.println("--------------------");
         int i=0;
 
         while (i< threads)
         {
             i++;
             LogSmasher m = new LogSmasher(message_count,account_count,host,port);
             m.start();
         }
     }
 }
