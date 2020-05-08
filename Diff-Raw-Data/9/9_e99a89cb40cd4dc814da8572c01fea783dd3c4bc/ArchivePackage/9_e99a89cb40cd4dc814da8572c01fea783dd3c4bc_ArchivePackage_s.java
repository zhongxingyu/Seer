 package org.runetekk;
 
 import org.apache.commons.compress.compressors.bzip2.BZip2CompressorInputStream;
 
 import java.io.ByteArrayInputStream;
 import java.io.DataInputStream;
 import java.io.IOException;
 
 /**
  * ArchivePackage.java
  * @version 1.0.0
  * @author RuneTekk Development (SiniSoul)
  */
 public final class ArchivePackage {
     
     /**
      * Option on if each archive entry in this package is compressed or the
      * entire archive is compressed. If true then the entire archive
      * is compressed else then each archive entry is compressed.
      */
     private boolean isCompressed;
 
     /**
      * The byte array that contains the unpacked data for each entry.
      */
     private byte[][] entryData;
     
     /**
      * The byte array source for the data of this {@link ArchivePackage}.
      */
     private byte[] archiveData;
     
     /**
      * The amount of entries in this {@link ArchivePackage}.
      */
     private int amountEntries;
     
     /**
      * The name hashes for the entries.
      */
     private int[] nameHashes;
     
     /**
      * The uncompressed sizes for the entries.
      */
     private int[] uSizes;
     
     /**
      * The compressed sizes for the entries.
      */
     private int[] cSizes;
     
     /**
      * The archive data byte array offsets for each archive. 
      */
     private int[] archiveOffsets;
     
     /**
      * Initializes this {@link ArchivePackage}.
      * @param src The byte array source.
      */
     public void initialize(byte[] src) throws IOException {
         int uSize = ((src[0] & 0xFF) << 16) | 
                     ((src[1] & 0xFF) << 8)  | 
                      (src[2] & 0xFF);
         int cSize = ((src[3] & 0xFF) << 16) | 
                     ((src[4] & 0xFF) << 8)  | 
                      (src[5] & 0xFF);
         if(cSize != uSize) {
             archiveData = new byte[uSize];
             DataInputStream is = new DataInputStream(new BZip2CompressorInputStream(new ByteArrayInputStream(src, 6, src.length)));
             is.readFully(archiveData);
             is.close();
             isCompressed = true;
         } else {
             archiveData = src;
             isCompressed = false;
         }
         amountEntries = ((archiveData[!isCompressed ? 6 : 0] & 0xFF) << 8) |
                          (archiveData[!isCompressed ? 7 : 1] & 0xFF);
         nameHashes = new int[amountEntries];
         uSizes = new int[amountEntries];
         cSizes = new int[amountEntries];
         archiveOffsets = new int[amountEntries];
         int dataOffset = isCompressed ? 2 : 8;
         int offset = dataOffset + amountEntries * 10;
         for(int i = 0; i < amountEntries; i++) {
             nameHashes[i] = ((archiveData[dataOffset++] & 0xFF) << 24) |
                             ((archiveData[dataOffset++] & 0xFF) << 16) | 
                             ((archiveData[dataOffset++] & 0xFF) << 8)  | 
                              (archiveData[dataOffset++] & 0xFF);
             uSizes[i] = ((archiveData[dataOffset++] & 0xFF) << 16) | 
                         ((archiveData[dataOffset++] & 0xFF) << 8)  | 
                          (archiveData[dataOffset++] & 0xFF);
             cSizes[i] = ((archiveData[dataOffset++] & 0xFF) << 16) | 
                         ((archiveData[dataOffset++] & 0xFF) << 8)  | 
                          (archiveData[dataOffset++] & 0xFF);
             archiveOffsets[i] = offset;
             offset += cSizes[i];
         }
     }
     
     /**
      * Gets an archive from this {@link ArchivePackage}.
      * @param name The name of the archive.
      * @return The archive source array.
      */
     public byte[] getArchive(String name) {
         try {
             int nameHash = 0;
             name = name.toUpperCase();
             for(int j = 0; j < name.length(); j++)
                 nameHash = (nameHash * 61 + name.charAt(j)) - 32;
             for(int i = 0; i < amountEntries; i++) {
                 if(nameHashes[i] == nameHash) {
                     byte[] src = null;
                     if(entryData != null) {
                         src = new byte[entryData[i].length];
                         System.arraycopy(entryData[i], 0, src, 0, entryData[i].length);
                     } else {
                         src = new byte[uSizes[i]];
                        DataInputStream is = new DataInputStream(new BZip2CompressorInputStream(new ByteArrayInputStream(archiveData, archiveOffsets[i], cSizes[i])));
                        is.readFully(src);
                        is.close();             
                     }
                     return src;
                 }
             }
         } catch(IOException ioex) {}
         return null;
     }
     
     /**
      * Unpacks all the entries and caches them.
      */
     public void unpack() throws IOException {
         if(entryData == null) {
             entryData = new byte[amountEntries][];
             for(int i = 0; i < amountEntries; i++) {
                 entryData[i] = new byte[uSizes[i]];
                 if(isCompressed) {
                     System.arraycopy(archiveData, archiveOffsets[i], entryData[i], 0, uSizes[i]);
                 } else {
                     DataInputStream is = new DataInputStream(new BZip2CompressorInputStream(new ByteArrayInputStream(archiveData, archiveOffsets[i], cSizes[i])));
                     is.readFully(entryData[i]);
                     is.close();
                 }
             }
             archiveData = null;
             archiveOffsets = null;
             uSizes = null;
             cSizes = null;
         }
     }
     
     /**
      * Destroys this {@link ArchivePackage}.
      * This {@link ArchivePackage} will be usable after it is re-initialized.
      */
     public void destroy() {
         entryData = null;
         archiveData = null;
         archiveOffsets = null;
         nameHashes = null;
         cSizes = null;
         uSizes = null;
     }
     
     /**
      * Constructs a new {@link ArchivePackage};
      * @param src The byte array source to initialize
      *            this {@link ArchivePackage} with.
      */
     public ArchivePackage(byte[] src) throws IOException {
         initialize(src);
     }    
 }
