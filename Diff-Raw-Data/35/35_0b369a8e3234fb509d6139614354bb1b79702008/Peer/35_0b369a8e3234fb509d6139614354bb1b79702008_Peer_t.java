 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 
 package com.tracker.backend.entity;
 
 import com.tracker.backend.StringUtils;
 import java.io.Serializable;
 import java.io.UnsupportedEncodingException;
 import java.net.InetAddress;
 import java.nio.ByteBuffer;
 import java.util.Date;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import javax.persistence.Column;
 import javax.persistence.Entity;
 import javax.persistence.GeneratedValue;
 import javax.persistence.GenerationType;
 import javax.persistence.Id;
 import javax.persistence.ManyToOne;
 import javax.persistence.Temporal;
 
 /**
  *
  * @author bo
  */
 @Entity
 public class Peer implements Serializable {
     private static final long serialVersionUID = 1L;
     /**
      * Priamry key.
      */
     @Id
     @GeneratedValue(strategy = GenerationType.AUTO)
     // fixes some errors about multiple write-able IDs
     @Column(nullable=false,insertable=false,updatable=false)
     private Long id;
 
     /**
      * Primary key, 40-byte hex representation of the peer-id
      * stored as hex to make it easier with all the binary data
      */
     @Id
     @Column(length=40)
     private String peerId;
 
     @ManyToOne(optional=false)
     private Torrent torrent;
     private InetAddress ip;
     //private Byte[] port = new Byte[2];
     private Long port;
     
     private Long downloaded;
     private Long uploaded;
     private Long bytesLeft;
     
     private Boolean seed;
 
     @Temporal(javax.persistence.TemporalType.TIMESTAMP)
     private Date lastAction;
 
     public Peer() {
         downloaded = uploaded = bytesLeft = (long)0;
     }
 
     /**
      * gives a compact representation of this peer in a String format
      * @return a String containing 6-bytes; the first 4 being the ip-address,
     * and the last 2 being the port number.
      */
     public String getCompactAddressPort()
     {
        StringBuilder result = new StringBuilder();
         byte address[] = ip.getAddress();

         if(address.length > 4) {
             return null;
         }
 
         for(int i = 0; i < address.length; i++) {
            char charByte = (char) address[i];
            result.append(charByte);
         }
 
        // do port
        ByteBuffer bb = ByteBuffer.allocate(8);
         bb.putLong(port);
        result.append((char)bb.get(6));
        result.append((char)bb.get(7));
 
        return result.toString();
     }
 
     public Date getLastActionTime() {
         return lastAction;
     }
 
     public void setLastActionTime(Date lastAction) {
         this.lastAction = lastAction;
     }
 
     public Boolean isSeed() {
         return seed;
     }
 
     public void setSeed(Boolean seed) {
         this.seed = seed;
     }
 
     public Long getBytesLeft() {
         return bytesLeft;
     }
 
     public void setBytesLeft(Long bytesLeft) {
         this.bytesLeft = bytesLeft;
         
         if(this.bytesLeft == 0) {
             this.seed = true;
         }
     }
 
     public Long getDownloaded() {
         return downloaded;
     }
 
     public void setDownloaded(Long downloaded) {
         this.downloaded = downloaded;
     }
 
     public InetAddress getIp() {
         return ip;
     }
 
     public void setIp(InetAddress ip) {
         this.ip = ip;
     }
 
     /**
      * returns the hex representation of the peer id
      * @return the hex string representing the peer id
      */
     public String getPeerId() {
         return peerId;
     }
 
     /**
      * Takes either a raw peerId of length = 20 or an encoded peerId of length = 40
      * and sets the hex-representation of the raw or the supplied encoded peerId
      * to this objects peerId.
      * @param peerId the peerId to set. Either a raw one (length = 20) or an
      * already encoded one (length = 40).
      * @return true if the peer id has been set, false if something went wrong
      */
     public boolean setPeerId(String peerId) {
         // is the peer id raw?
         if(peerId.length() == 20) {
             try {
                 byte[] b = new byte[20];
                 // charAt is used instead of getBytes, because getBytes insists on
                 // going through the whole encoding thing
                 for(int i = 0; i < b.length; i++) {
                     b[i] = (byte) peerId.charAt(i);
                 }
                 this.peerId = StringUtils.getHexString(b);
             } catch (Exception ex) {
                 // something went wrong
                 return false;
             }
             // all good
             return true;
         } // if(length != 20)
         // or already encoded?
         else if(peerId.length() == 40) {
             this.peerId = peerId;
             return true;
         }
         return false;
     }
 
     public Long getPort() {
         return port;
     }
 
     public void setPort(Long port) {
         this.port = port;
     }
 
     public Torrent getTorrent() {
         return torrent;
     }
 
     public void setTorrent(Torrent torrentId) {
         this.torrent = torrentId;
     }
 
     public Long getUploaded() {
         return uploaded;
     }
 
     public void setUploaded(Long uploaded) {
         this.uploaded = uploaded;
     }
 
     public Long getId() {
         return id;
     }
 
     public void setId(Long id) {
         this.id = id;
     }
 
     @Override
     public int hashCode() {
         int hash = 0;
         for(int i = 0; i < 20; i++) {
             hash += peerId.getBytes()[i] ^ 0x8B;
         }
         return hash;
     }
 
     @Override
     public boolean equals(Object object) {
         if (!(object instanceof Peer)) {
             return false;
         }
         Peer other = (Peer) object;
         if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))
                 || (this.peerId == null && other.peerId != null)
                 || (this.peerId != null && !this.peerId.equals(other.peerId))) {
             return false;
         }
         return true;
     }
 
     @Override
     public String toString() {
         return "entity.Peers[id=" + id + "]";
     }
 
 }
