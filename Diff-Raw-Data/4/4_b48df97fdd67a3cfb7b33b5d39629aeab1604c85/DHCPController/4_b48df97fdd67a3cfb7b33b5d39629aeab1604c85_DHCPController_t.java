 
 package dhcpserver;
 
 import java.io.BufferedReader;
 import java.io.DataInputStream;
 import java.io.FileInputStream;
 import java.io.InputStreamReader;
 import java.net.InetAddress;
 import java.net.UnknownHostException;
 import java.util.Date;
 
 public class DHCPController {
 
     enum DHCPMessage { INVALID, DHCPDISCOVER, DHCPOFFER, DHCPREQUEST, DHCPDECLINE, DHCPACK, DHCPNAK, DHCPRELEASE, DHCPINFORM };
 
     static DHCPOptions dhcpOptions = new DHCPOptions();
 
     public static class DHCPOptions {
         
         byte[] ipRangeFirst, ipRangeLast;
         byte[] subnetMask, defaultGateway, dnsServer;
 
         int leaseTime;
 
         public DHCPOptions() {
             // init
             ipRangeFirst = new byte[] {(byte)192, (byte)168, (byte)13, (byte)0};
             ipRangeLast = new byte[] {(byte)192, (byte)168, (byte)255, (byte)255};
 
             subnetMask = new byte[] {(byte)255, (byte)255, (byte)255, (byte)0};
             defaultGateway = new byte[] {(byte)192, (byte)168, (byte)134, (byte)1};
             dnsServer = new byte[] {(byte)4, (byte)2, (byte)2, (byte)4};
 
             leaseTime = 24 *  3600;
 
             // read config from file
             try {
                 FileInputStream fstream = new FileInputStream("config");
                 DataInputStream in = new DataInputStream(fstream);
                 BufferedReader br = new BufferedReader(new InputStreamReader(in));
 
                 ipRangeFirst = InetAddress.getByName(br.readLine()).getAddress();
                 ipRangeLast = InetAddress.getByName(br.readLine()).getAddress();
                 subnetMask = InetAddress.getByName(br.readLine()).getAddress();
                 defaultGateway = InetAddress.getByName(br.readLine()).getAddress();
                 dnsServer = InetAddress.getByName(br.readLine()).getAddress();
                 leaseTime = Integer.parseInt(br.readLine());
 
 //                String strLine;
 //                while ((strLine = br.readLine()) != null)   {
 //                  System.out.println (strLine);
 //                }
                 in.close();
 
                 System.out.println("config load");
             } catch (Exception e){System.err.println(e.getMessage());}
         }
     }
 
     void incIp(byte[] ip)
     {
         ip[3]++;
         if (ip[3] == 0)
         {
             ip[2]++;
             if (ip[2] == 0)
             {
                 ip[1]++;
                 if (ip[1] == 0)
                 {
                     ip[0]++;
                     if (ip[0] == 0)
                     {}
                 }
             }
         }
     }
 
     private byte[] lastIp;
     byte[] getNewIp()
     {
         if (lastIp == null)
         {
             lastIp = new byte[4];
             System.arraycopy(dhcpOptions.ipRangeFirst, 0, lastIp, 0, 4);
         }
 
         byte[] tmp = new byte[4];
         System.arraycopy(lastIp, 0, tmp, 0, 4);
         while (true)
         {
             incIp(lastIp);
 
             // infinite loop avoidance
             if (compareIPs(lastIp, tmp) == 0)
                 return null;
 
             // if lastIp expired, free it.
             if (! DHCPDatabase.freeIp(lastIp))
                 continue;
 
             if (compareIPs(lastIp, dhcpOptions.ipRangeFirst) < 0 || compareIPs(lastIp, dhcpOptions.ipRangeLast) > 0)
                 System.arraycopy(dhcpOptions.ipRangeFirst, 0, lastIp, 0, 4);
 
             return lastIp;
         }
     }
 
 
 
     public static int byteToInt(byte b)
     {
         return (int) b & 0xFF;
     }
 
     public static byte[] extractBytes(byte[] buffer, int from, int length)
     {
         byte[] result = new byte[length];
         System.arraycopy(buffer, from, result, 0, length);
         return result;
     }
 
     public boolean readMessage (byte[] buffer, int length) throws UnknownHostException
     {
         byte[] xid = extractBytes(buffer, 4, 4);
         byte[] chaddr = extractBytes(buffer, 28, 6);
         byte[] rquestedIp = new byte[4], serverIp = new byte[4], subnet = new byte[4], clientName = new byte[1];
 
         DHCPMessage message = DHCPMessage.INVALID; // invalid message
         byte[] options = extractBytes(buffer, 240, length - 240);
         for (int i = 0; i < options.length - 1; i++)
         {
             byte[] value = extractBytes(options, i+2, options[i+1]);
             
             switch (options[i])
             {
                 // Message Type
                 case 53:
                     message = DHCPMessage.values()[value[0]];
                 break;
 
                  // Requested IP Address
                 case 50:
                     rquestedIp = value;
                 break;
 
                 // Server Identifier
                 case 54:
                     serverIp = value;
                 break;
 
                 // Subnet Mask
                 case 1:
                     subnet = value;
                 break;
 
                 // Client Identifier
                 case 12:
                     clientName = value;
                 break;
             }
 
             i += options[i+1] + 1;
         }
 
         // controll part -------------------------------------------------------
         System.out.println(message.toString());
 
         boolean refreshTable = false;
 
         // extract or create record for client
         DHCPRecord record = DHCPDatabase.getRecord(chaddr);
 
         if (record == null && message == DHCPMessage.DHCPDISCOVER)
         {
             record = new DHCPRecord();
             record.ip = getNewIp();
             record.chaddr = chaddr;
             record.hostName = new String(clientName);
             DHCPDatabase.data.add(record);
 
             refreshTable = true;
         }
        
        rquestedIp = record.ip;
 
         // decide on client request
         DHCPMessage msgResponse = DHCPMessage.INVALID;
 
         switch (message)
         {
             case DHCPDISCOVER:
                msgResponse = DHCPMessage.DHCPOFFER;
             break;
 
             case DHCPREQUEST:
                 if (record != null && compareIPs(rquestedIp, record.ip) == 0 /*&& compareIPs(serverIp, InetAddress.getLocalHost().getAddress()) == 0*/)
                 {
                     record.ackTime = new Date(); // now
                     refreshTable = true;
                     msgResponse = DHCPMessage.DHCPACK;
                 }
 
                 if (record == null && compareIPs(subnet, dhcpOptions.subnetMask) == 0 && (compareIPs(rquestedIp, dhcpOptions.ipRangeFirst) < 0 || compareIPs(rquestedIp, dhcpOptions.ipRangeLast) > 0))
                     msgResponse = DHCPMessage.DHCPNAK;
             break;
 
             case DHCPDECLINE:
                 // block declined ip permanently except for decline attack
             break;
 
             case DHCPRELEASE:
                 // remove assigned ip without response
                 if (record != null)
                     DHCPDatabase.data.remove(record);
             break;
 
             case DHCPINFORM:
                 msgResponse = DHCPMessage.DHCPACK;
             break;
         }
 
         if (refreshTable)
             DHCPDatabase.model.fireTableDataChanged();
 
         if (msgResponse != DHCPMessage.INVALID)
         {
             DHCPDatabase.logModel.addRow(new Object[] {new String(clientName), message.toString(), msgResponse.toString(), DHCPDatabase.formatMAC(chaddr), DHCPDatabase.formatIp(rquestedIp)});
             return writeResponse(msgResponse, xid, rquestedIp, chaddr);
         }
 
         return false;
     }
 
     // ip1 > ip2 : 1, ip1 == ip2 : 0, ip1 < ip2 : -1
     public static int compareIPs(byte[] ip1, byte[] ip2)
     {
         for (int i = 0; i < 4; i++)
             if (byteToInt(ip1[i]) > byteToInt(ip2[i]))
                 return 1;
             else if (byteToInt(ip1[i]) < byteToInt(ip2[i]))
                 return -1;
         return 0;
     }
 
     public static byte[] getByteArray(int[] bytes)
     {
         byte[] result = new byte[bytes.length];
         for (int i = 0; i < bytes.length; i++)
             result[i] = (byte) bytes[i];
         return result;
     }
 
     public void addResponseBytes(byte[] bytes)
     {
         System.arraycopy(bytes, 0, response, index, bytes.length);
         index += bytes.length;
     }
 
     public static final byte[] intToByteArray(int value) {
         return new byte[]{ (byte)(value >>> 24), (byte)(value >> 16 & 0xff), (byte)(value >> 8 & 0xff), (byte)(value & 0xff) };
     }
 
     public int index;
     public byte[] response;
     public boolean writeResponse (DHCPMessage msgResponse, byte[] xid, byte[] ip, byte[] chaddr) throws UnknownHostException
     {
         response = new byte[1000];
         for (int i = 0; i < response.length; i++) response[i] = 0;
 
         InetAddress myIP = InetAddress.getLocalHost();
 
         index = 0;
         addResponseBytes(new byte[] {2}); // op
         addResponseBytes(new byte[] {1}); // htype
         addResponseBytes(new byte[] {6}); // hlen
         addResponseBytes(new byte[] {0}); // hops
         addResponseBytes(xid);
         addResponseBytes(new byte[] {0, 0}); // secs
         addResponseBytes(new byte[] {(byte)128, 0}); // flags
         addResponseBytes(new byte[] {0, 0, 0, 0}); // ciaddr
         addResponseBytes(ip);
         addResponseBytes(myIP.getAddress());
         addResponseBytes(new byte[] {0, 0, 0, 0}); // giaddr
         addResponseBytes(chaddr);
         index += 10;
         index += 64; // sname
         index += 128; // file
 
         addResponseBytes(intToByteArray(0x63825363)); // magic cookie
 
         // options
 
         // Message type
         addResponseBytes(new byte[] {53, 1, (byte) msgResponse.ordinal()});
         
         // DHCP Server Identifier
         addResponseBytes(new byte[] {54, 4}); addResponseBytes(myIP.getAddress());
 
         // Subnet Mask = 255.255.255.0
         addResponseBytes(new byte[] {1, 4}); addResponseBytes(dhcpOptions.subnetMask);
 
         // Default Gateway
         addResponseBytes(new byte[] {3, 4}); addResponseBytes(dhcpOptions.defaultGateway);
 
         // DNS Server
         addResponseBytes(new byte[] {6, 4}); addResponseBytes(dhcpOptions.defaultGateway);
 
         // IP Address Lease Time = 1 day
         addResponseBytes(new byte[] {51, 4}); addResponseBytes(intToByteArray(dhcpOptions.leaseTime));
         // Rebinding Time = 0.75 day
         addResponseBytes(new byte[] {59, 4}); addResponseBytes(intToByteArray((int) 0.75 * dhcpOptions.leaseTime));
         // Renewal Time = 0.5 day
         addResponseBytes(new byte[] {58, 4}); addResponseBytes(intToByteArray((int) 0.5 * dhcpOptions.leaseTime));
 
         return true;
     }
 }
