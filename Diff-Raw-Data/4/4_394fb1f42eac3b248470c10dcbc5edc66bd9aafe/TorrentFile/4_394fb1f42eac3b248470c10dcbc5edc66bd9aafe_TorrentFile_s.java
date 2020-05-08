 /*
  * $Id$
  *
  * Copyright © 2008,2009 Bjørn Øivind Bjørnsen
  *
  * This file is part of Quash.
  *
  * Quash is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * Quash is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with Quash. If not, see <http://www.gnu.org/licenses/>.
  */
 
 package com.tracker.backend.webinterface.entity;
 
 import com.tracker.backend.entity.Torrent;
 import java.io.OutputStream;
 import java.io.Serializable;
 import javax.persistence.Entity;
 import javax.persistence.GeneratedValue;
 import javax.persistence.GenerationType;
 import javax.persistence.Id;
 import javax.persistence.Lob;
 import javax.persistence.OneToOne;
 
 /**
  * This entity class contains the torrentfile to be downloaded.
  * @author bo
  */
 @Entity
 public class TorrentFile implements Serializable {
     private static final long serialVersionUID = 1L;
     @Id
     @GeneratedValue(strategy = GenerationType.AUTO)
     private Long id;
 
     public Long getId() {
         return id;
     }
 
     public void setId(Long id) {
         this.id = id;
     }
 
     /**
      * The torrentfile itself stored in the database.
      */
     @Lob
     byte[] torrentFile;
 
     /**
      * The length of the torrentfile in bytes.
      */
     int fileLength;
 
     /**
      * the torrent this torrentfile belongs to
      */
     @OneToOne(mappedBy = "torrentFile")
     private Torrent torrent;
 
     /**
      * Writes this torrent to a given output stream. Convenience method for
      * writing the torrent to a ServletOutputStream easily.
      * @param out the output stream to write the torrentfile data to.
      * @return the same OutputStream as provided by the user.
      * @throws java.lang.Exception if an I/O error occurs when writing to the
      * output stream or if the given output stream was null.
      */
     public OutputStream writeTorrentFile(OutputStream out) throws Exception {
         if(out == null) {
             throw new Exception("OutputStream given in writeTorrentFile was null?");
         }
         out.write(torrentFile);
 
         return out;
     }
 
     /**
      * Convenience method for raw access to the torrent file data.
      * @return a byte array containing the torrentfile.
      */
     public byte[] getTorrentFileRaw() {
         return torrentFile.clone();
     }
 
     /**
      * Stores the torrentfile given in the bencoded string.
      * @param bencodedTorrent the Bencoded string containing the torrentfile.
      */
     public void setTorrentFile(String bencodedTorrent) {
         torrentFile = new byte[bencodedTorrent.length()];
         for (int i = 0; i < torrentFile.length; i++) {
             torrentFile[i] = (byte) bencodedTorrent.charAt(i);
         }
         fileLength = torrentFile.length;
     }
 
     /**
      * Gets the length of the torrentfile in bytes
      * @return an int containing the length of the torrentfile in bytes.
      */
     public int getFileLength() {
         return fileLength;
     }
 
     /**
      * Sets the Torrent object connected with this torrentfile.
      * @param t the Torrent connected with this torrentfile.
      */
     public void setTorrent(Torrent t) {
         torrent = t;
     }
 
     /**
      * Gets the Torrent object connected with this torrentfile.
      * @return a Torrent object this torrentfile is connected to.
      */
     public Torrent getTorrent() {
         return torrent;
     }
 
     @Override
     public int hashCode() {
         int hash = 0;
         hash += (id != null ? id.hashCode() : 0);
         return hash;
     }
 
     @Override
     public boolean equals(Object object) {
         // TODO: Warning - this method won't work in the case the id fields are not set
         if (!(object instanceof TorrentFile)) {
             return false;
         }
         TorrentFile other = (TorrentFile) object;
         if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
             return false;
         }
         return true;
     }
 
     @Override
     public String toString() {
         return "com.tracker.backend.webinterface.entity.TorrentFile[id=" + id + "]";
     }
 
 }
