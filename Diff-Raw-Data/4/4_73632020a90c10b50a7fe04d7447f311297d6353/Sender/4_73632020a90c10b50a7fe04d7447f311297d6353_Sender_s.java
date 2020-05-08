 package bck;
 
 
 import bck.Backup;
 import java.io.IOException;
 import java.net.DatagramPacket;
 import java.net.InetAddress;
 import java.net.MulticastSocket;
 import java.util.HashMap;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 /* thread que vai enviar chunks para fazer backup no receiver*/
 public class Sender extends Thread {
 
     MulticastSocket socket = null;
     InetAddress address = null;
     int MC;
     int MD;
     String sha = "";
     int replication_degree;
     
     public Sender(InetAddress ad, int m_c, int m_d, String sh, int rd) throws IOException {
 
         address = ad;
         MC = m_c;
         MD = m_d;
         sha = sh;
         replication_degree = rd;
         socket = new MulticastSocket(MD);
         socket.joinGroup(address);
     }
 
     public void run() {
         System.out.println("Sending: " + Backup.getMapShaFiles().get(this.sha).getName());
         int n = 0;
         HashMap<Integer, byte[]> file_to_send_chunks = Backup.getMapChunkFiles().get(sha);
         
         while (file_to_send_chunks.get(n) != null) {
             //PUTCHUNK <Version> <FileId> <ChunkNo> <ReplicationDeg><CRLF><CRLF><Body>
             System.out.println("tamanho dos chunks em falta "+Backup.getMissingChunks());
             String msg = "PUTCHUNK " + Backup.getVersion() + " " + this.sha + " " + n + 
                     " " + replication_degree + "\n\n" + file_to_send_chunks.get(n);
            n++;
             DatagramPacket chunk = new DatagramPacket(msg.getBytes(), msg.length(), this.address, this.MD);
 
             try {
                 Thread.sleep(10);
                 socket.send(chunk);
                 Backup.getMissingChunks(sha).put(n, replication_degree);
             } catch (Exception ex) {
                 Logger.getLogger(Sender.class.getName()).log(Level.SEVERE, null, ex);
             }
         }
         
         System.out.print("Tentanto enviar chunks em falta... ");
         System.out.println(Backup.getMissingChunks(sha).size());
         Utils.flag_sending = 0;
     }
 }
