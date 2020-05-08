 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 
 package com.tracker.entity;
 
 import com.tracker.backend.StringUtils;
 import java.io.Serializable;
import java.util.Collection;
 import java.util.Date;
 import java.util.Vector;
 import javax.persistence.Column;
 import javax.persistence.Entity;
 import javax.persistence.GeneratedValue;
 import javax.persistence.GenerationType;
 import javax.persistence.Id;
 import javax.persistence.OneToMany;
 import javax.persistence.CascadeType;
 import javax.persistence.Temporal;
 
 
 
 /**
  *
  * @author bo
  */
 @Entity
 public class Torrent implements Serializable {
     private static final long serialVersionUID = 1L;
     /**
      * Primary key
      */
     @Id
     @GeneratedValue(strategy = GenerationType.AUTO)
     private Long id;
     /**
      * Primary key, 40-byte hex-representation of the info hash
      * encoded as hex to make it easier with all the binary data
      */
     @Id
     @Column(length=40)
     private String infoHash;
 
     /**
      * Name of the torrent
      */
     private String name;
     /**
      * Description of the torrent contents
      */
     private String description;
     
     /**
      * Date and time the torrent was added
      */
     @Temporal(javax.persistence.TemporalType.TIMESTAMP)
     private Date added;
     
     /**
      * Number of seeders seeding this torrent
      */
     private Long numSeeders;
     /**
      * Number of leechers downloading this torrent
      */
     private Long numLeechers;
     /**
      * Total number of peers on this torrent (seeders + leechers)
      */
     private Long numPeers;
     /**
      * How many have completed this torrent over its lifetime
      */
     private Long numCompleted;
     
     /**
      * Size of the torrent in bytes
      */
     private Long torrentSize;
 
     /**
      * All peers connected with this torrent
      * bi-directional relationship with the Peer Entity class
      */
     @OneToMany(cascade = CascadeType.REMOVE, mappedBy = "torrent")
    private Collection<Peer> peersData;
     /**
      * The peers that are seeding this torrent
      * one-directional relationship with the Peer Entity class
      */
     @OneToMany
    private Collection<Peer> seedersData;
     /**
      * The peers that are leeching this torrent
      * one-directional relationship with the Peer Entity class
      */
     @OneToMany
    private Collection<Peer> leechersData;
 
     /**
      * default constructor
      */
     public Torrent() {
         numCompleted = numLeechers = numPeers = numSeeders = (long)0;
         peersData = new Vector<Peer>();
         seedersData = new Vector<Peer>();
         leechersData = new Vector<Peer>();
     }
 
     /**
      * marks a leecher as completed (turns into a seed).
      * This is necessary since removePeer() + addSeed() confuses JPA and it's
      * mapping-table.
      * @param p the leecher that has completed this torrent
      * @return true if the leecher is on this torrent and has been changed to a
      * seed, false if the peer is not a leecher of this torrent.
      */
     public boolean leecherCompleted(Peer p) {
         if(leechersData.contains(p)) {
             leechersData.remove(p);
             numLeechers--;
 
             seedersData.add(p);
             numSeeders++;
 
             p.setSeed(true);
 
             return true;
         }
 
         return false;
     }
     
     /**
      * Gets the leechers data
      * @return a Collection of Peers that are leeching this torrent
      */
    public Collection<Peer> getLeechersData() {
         return leechersData;
     }
 
     /**
      * Adds a leecher to this torrent
      * @param p a Peer that is leeching this torrent
      * @return true if the peers is added as a leecher, false if the peer is
      * already in the list of peers.
      */
     public boolean addLeecher(Peer p) {
         if(this.peersData.contains(p))
             return(false);
 
         p.setSeed(false);
         this.leechersData.add(p);
         numLeechers++;
         this.addPeer(p);
         
         return(true);
     }
     
     /**
      * Removes a leecher from this torrent
      * @param p the Peer to remove from the list of leechers
      * @return true if the Peer was leeching this torrent and was successfully
      * removed, false if the Peer was not leeching this torrent.
      */
     private boolean removeLeecher(Peer p) {
         if(this.leechersData.contains(p)) {
             this.leechersData.remove(p);
             numLeechers--;
             return(true);
         }
         else
             return(false);
     }
 
     /**
      * Gets the size of the torrent in bytes
      * @return the size of the torrent in bytes
      */
     public Long getTorrentSize() {
         return torrentSize;
     }
 
     /**
      * sets the size of the torrent in bytes
      * @param torrentSize the new size of the torrent
      */
     public void setTorrentSize(Long torrentSize) {
         this.torrentSize = torrentSize;
     }
     
     /**
      * Gives the number of leechers
      * @return the number of leechers this torrent has
      */
     public Long getNumLeechers() {
         return numLeechers;
     }
 
     /**
      * sets the number of leechers
      * @param numLeechers the number of leechers this torrent supposedly has
      */
     public void setNumLeechers(Long numLeechers) {
         this.numLeechers = numLeechers;
     }
 
     /**
      * gets the data of the seeding peers
      * @return a Collection of Peers that is currently seeding this torrent
      */
    public Collection<Peer> getSeedersData() {
         return seedersData;
     }
 
     /**
      * adds a seed to this torrent
      * @param p the Peer to add as a seed to this torrent
      * @return true if the Peer is successfully added as a seed, false if the
      * Peer is already seeding this torrent
      */
     public boolean addSeeder(Peer p) {
         if(this.seedersData.contains(p))
             return(false);
 
         p.setSeed(true);
         this.seedersData.add(p);
         numSeeders++;
         this.addPeer(p);
         
         return(true);
     }
     
     /**
      * removes a seed from this torrent
      * @param p the Peer to remove as a seed from this torrent
      * @return true if the Peer is successfully removed, false if the Peer is
      * not seeding this torrent or is not a peer of this torrent
      */
     private boolean removeSeed(Peer p) {
         if(this.seedersData.contains(p)) {
             this.seedersData.remove(p);
             numSeeders--;
             return(true);
         }
         else
             return(false);
     }
     
     /**
      * gets the peer data for the given torrent
      * @return a Collection of Peers that is all the peers connected with the
      * torrent
      */
    public Collection<Peer> getPeersData() {
         return peersData;
     }
     
     /**
      * adds a peer to the list of peers.
      * private method only, use addSeeder() or addLeecher() for everyone else
      * @param p the peer to add the the list
      */
     private void addPeer(Peer p) {
         this.peersData.add(p);
         p.setTorrent(this);
         numPeers++;
     }
     
     /**
      * Removes a peer from the list of peers
      * private method only, use removeSeed() or removeLeecher() for everyone
      * else
      * @param p the peer to remove from the list
      * @return true if the peer has been removed, false if the peer is not
      * on this torrent
      */
     public boolean removePeer(Peer p) {
         if(this.peersData.contains(p)) {
             this.peersData.remove(p);
             numPeers--;
 
             if(p.isSeed())
                 return this.removeSeed(p);
             else
                 return this.removeLeecher(p);
         }
         else
             return(false);
     }
 
     public Long getNumPeers() {
         return numPeers;
     }
 
     public void setNumPeers(Long numPeers) {
         this.numPeers = numPeers;
     }
 
     public Long getNumSeeders() {
         return numSeeders;
     }
 
     public void setNumSeeders(Long numSeeders) {
         this.numSeeders = numSeeders;
     }
         
     public Long getNumCompleted() {
         return numCompleted;
     }
 
     public void setNumCompleted(Long completed) {
         this.numCompleted = completed;
     }
 
     public Date getAdded() {
         return added;
     }
 
     public void setAdded(Date added) {
         this.added = added;
     }
 
     public String getDescription() {
         return description;
     }
 
     public void setDescription(String description) {
         this.description = description;
     }
 
     /**
      * returns the hex representation of the info hash
      * @return the hex string representing the info hash
      */
     public String getInfoHash() {
         return infoHash;
     }
 
     /**
      * Takes the raw info hash supplied and turns it into a hex-string and sets
      * the hex string.
      * @param infoHash the raw info hash recevied.
      * @return true if the info hash has been set, false if there is some error
      */
     public boolean setInfoHash(String infoHash) {
         // is the info hash raw?
         if(infoHash.length() == 20) {
             try {
                 byte[] b = new byte[20];
                 // charAt is used instead of getBytes, because getBytes insists on
                 // going through the whole encoding thing
                 for(int i = 0; i < b.length; i++) {
                     b[i] = (byte) infoHash.charAt(i);
                 }
                 this.infoHash = StringUtils.getHexString(b);
             } catch (Exception ex) {
                 // something went wrong
                 return false;
             }
             // all good
             return true;
         } // if(length != 20)
         // is the info hash already encoded?
         if(infoHash.length() == 40) {
             this.infoHash = infoHash;
             return true;
         }
         return false;
     }
 
     public String getName() {
         return name;
     }
 
     public void setName(String name) {
         this.name = name;
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
             hash += infoHash.getBytes()[i] ^ 0x4F;
         }
         return hash;
     }
 
     @Override
     public boolean equals(Object object) {
         if (!(object instanceof Torrent)) {
             return false;
         }
         Torrent other = (Torrent) object;
         if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))
                 || (this.infoHash == null && other.infoHash != null)
                 || (this.infoHash != null && !this.infoHash.equals(other.infoHash))) {
             return false;
         }
         return true;
     }
 
     @Override
     public String toString() {
         return "entity.Torrent[id=" + id + ",info_hash=" + infoHash + ",name=" + name + "]";
     }
 
 }
