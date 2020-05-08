 package edu.ualr.bittorrent.impl.core;
 
 import java.security.MessageDigest;
 import java.security.NoSuchAlgorithmException;
 import java.util.UUID;
 
 import org.joda.time.Instant;
 
 import com.google.common.base.Objects;
 import com.google.common.base.Preconditions;
 import com.google.common.collect.ImmutableList;
 import com.google.inject.Inject;
 
 import edu.ualr.bittorrent.impl.core.ExperimentModule.PieceLength;
 import edu.ualr.bittorrent.interfaces.Metainfo;
 import edu.ualr.bittorrent.interfaces.Tracker;
 
 /**
  * Default implementation of the {@link Metainfo} interface.
  */
 public class MetainfoImpl implements Metainfo {
   private final ImmutableList<Tracker> trackers;
   private final ImmutableList<String> pieces;
   private final ImmutableList<File> files;
   private final Integer pieceLength;
   private final byte[] infoHash;
   private final Integer lastPieceIndex;
   private Integer lastPieceSize;
   private Integer totalDownloadSize;
 
   /**
    * Create a new metainfo object, generating a new unique info hash.
    *
    * @param trackers
    *          list of trackers that manage the swarm
    * @param pieces
    *          signatures of data chunks in the torrent
    * @param pieceLength
    *          length of data that constitutes a piece
    * @param files
    *          list of files in the torrent
    * @throws NoSuchAlgorithmException
    */
   @Inject
   public MetainfoImpl(ImmutableList<Tracker> trackers,
       ImmutableList<String> pieces, @PieceLength Integer pieceLength,
       ImmutableList<File> files) throws NoSuchAlgorithmException {
     this.trackers = Preconditions.checkNotNull(trackers);
     this.pieces = Preconditions.checkNotNull(pieces);
     this.pieceLength = Preconditions.checkNotNull(pieceLength);
     this.files = Preconditions.checkNotNull(files);
 
     Preconditions.checkArgument(trackers.size() > 0,
         "At least one tracker is required");
     Preconditions.checkArgument(pieces.size() > 0,
         "At least one piece is required");
     Preconditions.checkArgument(files.size() > 0,
         "At least one file is required");
 
     this.infoHash = MessageDigest.getInstance("SHA").digest(
         UUID.randomUUID().toString().getBytes());
 
     lastPieceIndex = pieces.size() - 1;
 
     totalDownloadSize = 0;
     for (File file : files) {
      totalDownloadSize += file.getLength();
     }
 
     lastPieceSize = (totalDownloadSize % pieceLength);
     if (lastPieceSize == 0) {
       lastPieceSize = pieceLength;
     }
   }
 
   @Override
   public boolean equals(Object object) {
     if (!(object instanceof MetainfoImpl)) {
       return false;
     }
     MetainfoImpl metainfo = (MetainfoImpl) object;
     return Objects.equal(trackers, metainfo.trackers)
         && Objects.equal(infoHash, metainfo.infoHash)
         && Objects.equal(pieces, metainfo.pieces)
         && Objects.equal(pieceLength, metainfo.pieceLength)
         && Objects.equal(files, metainfo.files);
   }
 
   @Override
   public int hashCode() {
     return Objects.hashCode(infoHash, trackers, pieceLength, pieces, files);
   }
 
   /**
    * {@inheritDoc}
    */
   public String getComment() {
     return null; /* optional field that we are opting out of providing */
   }
 
   /**
    * {@inheritDoc}
    */
   public String getCreatedBy() {
     return null; /* optional field that we are opting out of providing */
   }
 
   /**
    * {@inheritDoc}
    */
   public Instant getCreationDate() {
     return null; /* optional field that we are opting out of providing */
   }
 
   /**
    * {@inheritDoc}
    */
   public String getEncoding() {
     return null; /* optional field that we are opting out of providing */
   }
 
   /**
    * {@inheritDoc}
    */
   public byte[] getInfoHash() {
     return infoHash;
   }
 
   /**
    * {@inheritDoc}
    */
   public ImmutableList<File> getFiles() {
     return files;
   }
 
   /**
    * {@inheritDoc}
    */
   public Integer getLength() {
     if (files.size() > 1) {
       return null;
     }
     return files.get(0).getLength();
   }
 
   /**
    * {@inheritDoc}
    */
   public String getMd5Sum() {
     return null; /* optional field that we are opting out of providing */
   }
 
   /**
    * {@inheritDoc}
    */
   public ImmutableList<String> getName() {
     if (files.size() > 1) {
       return null;
     }
     return files.get(0).getName();
   }
 
   /**
    * {@inheritDoc}
    */
   public Integer getPieceLength() {
     return pieceLength;
   }
 
   /**
    * {@inheritDoc}
    */
   public ImmutableList<String> getPieces() {
     return pieces;
   }
 
   /**
    * {@inheritDoc}
    */
   public Integer getPrivate() {
     return null; /* optional field that we are opting out of providing */
   }
 
   /**
    * {@inheritDoc}
    */
   public ImmutableList<Tracker> getTrackers() {
     return trackers;
   }
 
   /**
    * Default implementation of the {@link Metainfo.File} interface.
    */
   public static class FileImpl implements Metainfo.File {
     final Integer length;
     final ImmutableList<String> name;
 
     /**
      * Creates a new file object.
      *
      * @param length
      *          the size of the file in bytes
      * @param name
      *          the name of the file, including path, as a list of strings
      */
     public FileImpl(Integer length, ImmutableList<String> name) {
       this.length = Preconditions.checkNotNull(length);
       this.name = Preconditions.checkNotNull(name);
       Preconditions.checkArgument(name.size() > 0,
           "At least one name component is required");
     }
 
     /**
      * {@inheritDoc}
      */
     public Integer getLength() {
       return length;
     }
 
     /**
      * {@inheritDoc}
      */
     public String getMd5Sum() {
       return null; /* optional field that we are opting out of providing */
     }
 
     /**
      * {@inheritDoc}
      */
     public ImmutableList<String> getName() {
       return name;
     }
   }
 
   public Integer getLastPieceIndex() {
     return lastPieceIndex;
   }
 
   public Integer getLastPieceSize() {
     return lastPieceSize;
   }
 
   public Integer getTotalDownloadSize() {
     return totalDownloadSize;
   }
 }
