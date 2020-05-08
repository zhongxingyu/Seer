 package FeeshTank;
 
 import java.io.InputStream;
 import java.io.ObjectInputStream;
 import java.io.ObjectOutputStream;
 import java.io.OutputStream;
 import java.net.ServerSocket;
 import java.net.Socket;
 import java.util.ArrayList;
 
 /**
  * Created with IntelliJ IDEA.
  * User: Christopher
  * Date: 4/30/12
  * Time: 3:32 PM
  * To change this template use File | Settings | File Templates.
  */
 abstract class FeeshContainer {
     ArrayList<Feesh> myFeeshList=new ArrayList<Feesh>();
     ArrayList<Feesh> myOutgoingTransferList=new ArrayList<Feesh>();
     ArrayList<Feesh> myIncomingTransferList=new ArrayList<Feesh>();
     public int port = 2002;
    public String targetIP = "192.168.2.4";
     public boolean debug=true;
 
     abstract public ArrayList<Feesh> getFeeshList();
 
     abstract public ArrayList<Feesh> getFeeshListExcluding(Feesh a);
 
     abstract  public boolean removeFeesh(Feesh toRemove);
 
 
     public void sendList() {
         try {
 
             Socket s = new Socket(targetIP, port);
 
             if(debug)  System.out.println("outgoing: contacting " + targetIP + ":"+ port);
 
             OutputStream os = s.getOutputStream();
             ObjectOutputStream oos = new ObjectOutputStream(os);
             if(debug) System.out.println("outgoing: "+myOutgoingTransferList.size()+ "feesh to transfer");
             oos.writeObject(myOutgoingTransferList);
             myOutgoingTransferList.clear();
             oos.close();
             os.close();
             s.close();
             if(debug) System.out.println("outgoing: Feesh successfully transfered");
         } catch (Exception e) {
             System.out.println(e);
         }
 
     }
 
     public void receiveList() {
         try {
             if(debug) System.out.println("server: spawned");
             ServerSocket ss = new ServerSocket(port);
             Socket s = ss.accept();
             InputStream is = s.getInputStream();
             ObjectInputStream ois = new ObjectInputStream(is);
             ArrayList<Feesh> receivedList = (ArrayList<Feesh>) ois.readObject();
             if(debug) System.out.println("server:" + receivedList.size() +"feesh recieved: " +receivedList);
            for(Feesh curFeesh :receivedList)
             {
                 curFeesh.startDisplaying();
             }
             myIncomingTransferList.addAll(receivedList);
             is.close();
             s.close();
             ss.close();
         } catch (Exception e) {
             e.printStackTrace();
         }
     }
 }
