package my.buysell;
 
 import java.io.IOException;
 import java.net.*;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.util.ArrayList;
 import java.util.List;
 
 /**
  * ClientSocket.java
  *
  * This class will handle communication between the different clients. It will send the update messages
  * when necessary and call an update method when a message is received.
  */
 public class ClientSocket extends Thread{
 
     private DatagramSocket socket;
     private BuySellUI ui;
 
     /**
      * Constructor method that creates the Datagram socket to send and receive messages.
      */
     public ClientSocket(BuySellUI ui) {
         try {
             int port = 25001;
             socket = new DatagramSocket(port);
             
         } catch (SocketException e) {
             e.printStackTrace();
         }
         
         this.ui = ui;
 
     }
 
     public void sendData(String sendMessage, InetAddress client, int port) {
         try {
             byte[] data = sendMessage.getBytes();
 
             DatagramPacket sendPacket = new DatagramPacket(data, data.length, client, port);
             socket.send(sendPacket);
 
 
         } catch(IOException ioException) {
             ioException.printStackTrace();
         }
 
     }
 
     public void waitForPackets() {
         try{
             while(true) {
                     byte[] data = new byte[1024];
                     DatagramPacket received = new DatagramPacket(data, data.length);
 
 
                     socket.receive(received);
                     String output = new String(received.getData());
 
                     interpret(output);
 
 
 
             }
         } catch(IOException e) {
             e.printStackTrace();
         }
 
     }
 
     public void sendUpdateBuy(List<Client> clients) {
         for(Client client : clients) {
             InetAddress address = client.getAddress();
             int port = client.getPort();
 
             String output = "Update_want_to_buy";
 
             sendData(output, address, port);
         }
     }
 
     public void sendUpdateSell(List<Client> clients) {
         for(Client client : clients) {
             InetAddress address = client.getAddress();
             int port = client.getPort();
 
             String output = "Update_want_to_sell";
 
             sendData(output, address, port);
         }
     }
 
     public void interpret(String message) {
         if (message.equals("Update_want_to_sell")) {
             ui.refreshWTS();
 
         } else if (message.equals("Update_want_to_buy")) {
             ui.refreshWTB();
 
         }
     }
 
     public void updateBuy(ResultSet rs) {
         try {
             List<Client> clients = parseClients(rs);
             sendUpdateBuy(clients);
         } catch (SQLException e) {
             e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
         } catch (UnknownHostException e) {
             e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
         }
 
     }
 
     public void updateSell(ResultSet rs) {
 
         try {
             List<Client> clients = parseClients(rs);
             sendUpdateSell(clients);
         } catch (SQLException e) {
             e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
         } catch (UnknownHostException e) {
             e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
         }
 
 
     }
 
     public List<Client> parseClients(ResultSet rs) throws SQLException, UnknownHostException {
         List<Client> clients = new ArrayList<Client>();
         while(rs.next()) {
             String address = rs.getString(1);
             String port = rs.getString(2);
 
             int outPort = Integer.parseInt(port);
             InetAddress outAddress = InetAddress.getByName(address);
 
             Client client = new Client(outAddress, outPort);
             clients.add(client);
         }
 
         return clients;
     }
 
 
     @Override
     public void run() {
         this.waitForPackets();
     }
 }
