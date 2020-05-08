 import java.io.DataOutputStream;
 import java.io.IOException;
 import java.io.PrintWriter;
 import java.net.Socket;
 import java.util.Iterator;
 import java.lang.Math;
 
 /**
  * ATTENTION:  THIS class need to check ID Repeat OUTSIDE BEFORE being called!
  *
  */
 
 /**
  * @author quake0day
  * 
  */
 public class Update extends Thread {
 
     /**
      * @param args
      */
     private ConnectionInfoList clients;
     private Socket forbiddenSocket = null;
     private int TTL=7;
     private int Hops=0;
     private int[] _idNum = new int[14];	//ID number
     private MessageIDList _idList;
     PrintWriter outServer = null;
     DataOutputStream outToServer = null;
 
     public Update(ConnectionInfoList client, MessageIDList idList){
         this.clients = client;
         this.forbiddenSocket = null;
         this._idList = idList;
     }
 
     public Update(ConnectionInfoList client, Socket forbiddenSocket,int TTL
             , int Hops, MessageIDList idList){
         this.clients = client;
         this.forbiddenSocket = forbiddenSocket;
         this.TTL = TTL;
         this.Hops = Hops;
         this._idList = idList;
             }
 
     public void run(){
         double j = 1;
         for(int i = 0; i < _idNum.length; i++)
         {
             _idNum[i] = (int) (Math.random() * 255.0);
         }
         while(_idList.checkID(_idNum) && j < 4.9E33)	// repeated message ID
         {
             j++;
             for(int i: _idNum)
             {
                 _idNum[i] = (int) (Math.random() * 255.0);
             }
         }
         if (j >4.9E33)
         {
             System.out.println("Oops! Message IDs seem to be running out!");
         }
 
         else {
             _idList.addRecord(new IDRecorder(_idNum, forbiddenSocket));
             byte[] ping = null;
             Iterator<ConnectionInfo> iter =clients.iterator();
             //byte[] id, byte type, byte ttl, byte hops, byte[] plength, byte[] payload
             //byte[] mID = new byte[16];
             //byte[] newPacketLength={0x00,0x00,0x00,0x00};
             //byte ttl;
             //byte hops;
             //byte[] payload = null;
             //mID[8] = (byte)0xff;
             //mID[15] = (byte)0x00;
             //byte mtype = (byte)0x00; // ping message
             //ttl = new Integer(TTL).byteValue();
             //hops = new Integer(Hops).byteValue(); Suggest to not use, SEE below
 
 
             MessageContainer pingContainer = new MessageContainer();//use in this way
             pingContainer.setID(_idNum);							//would be more readable
             pingContainer.setPayloadLength(0);						//see comment in 
             pingContainer.setTTL(TTL);								//MessageContainer.java
             pingContainer.setHops(Hops);
             pingContainer.setType(1);
             pingContainer.addPayLoad(null);
 
             ping = pingContainer.convertToByte();						
             while(iter.hasNext()){									
                 Socket clientSocket = iter.next().getSocket();					
                 if(!clientSocket.equals(forbiddenSocket)){
                     try {
                         outToServer = new DataOutputStream(clientSocket.getOutputStream());
                     } catch (IOException e) {
                         // TODO Auto-generated catch block
                         //e.printStackTrace();
                         // This could happen when the other side's socket is closed
                         System.out.println("Error when sending ping in iterator");
                     }
 
                     // send Ping
                     try {
                         outToServer.write(ping);
                         /*
                         System.out.print("sent a PingID: ");	//for test use
                         for (int i: _idNum)
                         {
                             System.out.print(i + "\t");
                         }
                         System.out.println();
                         */
 
 
                         //outToServer.writeChars("\r\n");
                     } catch (IOException e) {
                         // TODO Auto-generated catch block
                        e.printStackTrace();
                     }
                     //outServer.close();
                 }
             }
         }
     }
 }
