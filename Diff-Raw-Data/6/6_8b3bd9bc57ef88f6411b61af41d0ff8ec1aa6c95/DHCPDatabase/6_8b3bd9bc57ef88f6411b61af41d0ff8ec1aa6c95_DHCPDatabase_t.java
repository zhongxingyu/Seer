 package dhcpserver;
 
 import java.io.BufferedWriter;
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.io.FileWriter;
 import java.io.ObjectInputStream;
 import java.io.ObjectOutputStream;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Date;
 import java.util.Iterator;
 import javax.swing.table.AbstractTableModel;
 import javax.swing.table.DefaultTableModel;
 
 public class DHCPDatabase extends AbstractTableModel {
 
     public static DefaultTableModel logModel = new DefaultTableModel();
 
 
     public static DHCPDatabase model = new DHCPDatabase();
 
     private String[] columnNames = {"Host Name", "Client Add.", "IP Address", "Acked Time"};
     static ArrayList<DHCPRecord> data = new ArrayList<DHCPRecord>();
 
     // TODO indexed search of mac address
     public static DHCPRecord getRecord(byte[] mac)
     {
         Iterator<DHCPRecord> itr = data.iterator();
         while (itr.hasNext())
         {
             DHCPRecord record = itr.next();
             if (Arrays.equals(record.chaddr, mac))
                 return record;
         }
         return null;
     }
 
     // TODO indexed search of Ip address
     public static boolean freeIp(byte[] ip)
     {
         Iterator<DHCPRecord> itr = data.iterator();
         while (itr.hasNext())
         {
             DHCPRecord record = itr.next();
 
             if (Arrays.equals(record.ip, ip))
             {
                 boolean remove = false;

                // reserve time expire
                 if (record.ackTime == null && secondDiff(record.reserveTime, new Date()) < 120)
                     remove = true;
 
                 // lease time expire
                 if (record.ackTime != null && secondDiff(record.ackTime, new Date()) < DHCPController.dhcpOptions.leaseTime)
                     remove = true;
 
                 if (remove)
                 {
                     data.remove(record);
                     return true;
                 }
 
                 return false;
             }
         }
         return true;
     }
 
     public static int secondDiff(Date t1, Date t2)
     {
        return Math.abs((int)((t1.getTime() - t2.getTime())/1000));
     }
 
     // store and retrieve data
 
     static void writeToFile()
     {
        try{
             System.out.println("write database");
 
             FileOutputStream fstream = new FileOutputStream("db");
             
             ObjectOutputStream out = new ObjectOutputStream(fstream);
             out.writeObject(data);
 
             out.close();
 
         } catch (Exception e){ System.err.println(e.getMessage()); }
     }
 
     static void readFromFile()
     {
         try {
             FileInputStream fis = new FileInputStream("db");
             ObjectInputStream in = new ObjectInputStream(fis);
 
             DHCPDatabase.data = (ArrayList<DHCPRecord>)in.readObject();
             
             in.close();
         } catch (Exception e){ System.err.println(e.getMessage()); }
     }
 
     // global functions
 
     static String formatMAC(byte[] mac)
     {
         return String.format("%2s:%2s:%2s:%2s:%2s:%2s", Integer.toHexString(DHCPController.byteToInt(mac[0])), Integer.toHexString(DHCPController.byteToInt(mac[1])), Integer.toHexString(DHCPController.byteToInt(mac[2])), Integer.toHexString(DHCPController.byteToInt(mac[3])), Integer.toHexString(DHCPController.byteToInt(mac[4])), Integer.toHexString(DHCPController.byteToInt(mac[5]))).replace(" ", "0");
     }
 
     static String formatIp(byte[] ip)
     {
         return String.format("%d.%d.%d.%d", DHCPController.byteToInt(ip[0]), DHCPController.byteToInt(ip[1]), DHCPController.byteToInt(ip[2]), DHCPController.byteToInt(ip[3]));
     }
 
     // model functions
     public int getColumnCount() {
         return columnNames.length;
     }
 
     public int getRowCount() {
         return data.size();
     }
 
     public String getColumnName(int col) {
         return columnNames[col];
     }
 
     public Object getValueAt(int row, int col) {
 
         DHCPRecord record = data.get(row);
         switch (col)
         {
             case 0:
                 return record.hostName;
             case 1:
                 return formatMAC(record.chaddr);
             case 2:
                 return formatIp(record.ip);
             case 3:
                 if (record.ackTime != null)
                     return record.ackTime.toString();
         }
 
         return null;
     }
 
     public Class getColumnClass(int c) {
         return ("").getClass();
     }
 }
