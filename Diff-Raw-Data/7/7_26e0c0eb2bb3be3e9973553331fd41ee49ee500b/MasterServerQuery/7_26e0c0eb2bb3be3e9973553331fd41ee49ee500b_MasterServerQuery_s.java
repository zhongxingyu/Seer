 package com.slugsource.steam.servers.query;
 
 import com.slugsource.steam.serverbrowser.NotAServerException;
 import com.slugsource.steam.servers.ServerAddress;
 import com.slugsource.steam.servers.readers.MasterServerReader;
 import com.slugsource.steam.string.StringUtils;
 import java.io.IOException;
 import java.net.*;
 import java.util.Collections;
 import java.util.List;
 import org.apache.commons.lang3.ArrayUtils;
 
 /**
  *
  * @author Nathan Fearnley
  */
 public class MasterServerQuery extends ServerQuery
 {
 
     private MasterServerReader reader = new MasterServerReader();
     private List<ServerAddress> serverList;
     private String filter;
    private ServerAddress lastAddress;
 
     public MasterServerQuery(InetAddress address, int port, String filter, List<ServerAddress> serverList)
     {
         super(address, port);
         this.filter = filter;
         this.serverList = Collections.synchronizedList(serverList);
     }
 
     @Override
     public void queryServer() throws NotAServerException, SocketTimeoutException, SocketException, IOException
     {
         try (DatagramSocket socket = new DatagramSocket())
         {
             this.socket = socket;
             do
             {
                 sendQueryRequest();
                 readQueryResponse();
             } while (!lastAddress.equals(ServerAddress.getZeroAddress()));
         }
     }
 
     @Override
     protected void sendQueryRequest() throws SocketException, IOException
     {
         byte[] header =
         {
             (byte) 0x31, (byte) 0xFF
         };
 
         byte[] lastAddressBytes = StringUtils.getNullTerminatedString(lastAddress.toString());
 
         byte[] filterBytes = StringUtils.getNullTerminatedString(filter);
 
         byte[] buffer = ArrayUtils.addAll(header, lastAddressBytes);
         buffer = ArrayUtils.addAll(buffer, filterBytes);
 
         DatagramPacket request = new DatagramPacket(
                 buffer, buffer.length, address, port);
 
         socket.send(request);
     }
 
     @Override
     protected void readQueryResponse() throws NotAServerException, SocketTimeoutException, SocketException, IOException
     {
         socket.setSoTimeout(1000);
 
         byte[] receiveBuffer = new byte[1400];
         DatagramPacket response = new DatagramPacket(receiveBuffer, 1400);
 
         socket.receive(response);
 
         reader.readServer(response, serverList);
     }
 }
