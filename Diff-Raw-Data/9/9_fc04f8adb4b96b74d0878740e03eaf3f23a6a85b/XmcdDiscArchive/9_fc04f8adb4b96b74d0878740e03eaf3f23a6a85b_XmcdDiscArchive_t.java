 package my.triviagame.xmcd;
 
 import com.google.common.base.CharMatcher;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.IOException;
 import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
 import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
 import org.apache.commons.compress.compressors.bzip2.BZip2CompressorInputStream;
 
 /**
  * Decompresses an xmcd archive and streams records as {@link XmcdDisc}s.
  * 
 * Remember to call {@link #close()} to release the archive file.
  */
 public class XmcdDiscArchive implements CloseableIterator<XmcdDisc> {
 
     /**
      * Extracts an xmcd archive and streams the records within.
      * Filters out discs with non-ASCII characters.
      */
     public XmcdDiscArchive(File archive) throws XmcdExtractionException {
         this(archive, true);
     }
     
     /**
      * Extracts an xmcd archive and streams the records within.
      * 
      * @param archive the archive file to extract & parse
      * @param onlyAscii whether to allow only ASCII text, skipping non-ASCII records
      */
     public XmcdDiscArchive(File archive, boolean onlyAscii) throws XmcdExtractionException {
         try {
             this.onlyAscii = onlyAscii;
             FileInputStream fin = new FileInputStream(archive);
             BZip2CompressorInputStream bzip2 = new BZip2CompressorInputStream(fin);
             tar = new TarArchiveInputStream(bzip2);
             findNextEntry();
         } catch (IOException e) {
             throw new XmcdExtractionException(String.format("Could not open file %s", archive.getName()), e);
         }
     }
 
     @Override
     public boolean hasNext() {
         return nextEntry != null;
     }
 
     @Override
     public XmcdDisc next() throws XmcdExtractionException, XmcdMissingInformationException, XmcdFormatException {
         XmcdDisc xmcdDisc;
         String dirName;
         try {
             currentEntry = nextEntry;
             entryText = nextEntryText;
             // Advance to the next file for the next call.
             // This allows continuing to the next file if parsing the current file fails.
             findNextEntry();
             // Parse the file
             dirName = new File(currentEntry.getName()).getParentFile().getName();
             FreedbGenre freedbGenre = FreedbGenre.fromDirectoryName(dirName);
             xmcdDisc = XmcdDisc.fromXmcdFile(entryText, freedbGenre);
         } catch (IOException e) {
             throw new XmcdExtractionException("Error while unarchiving", e);
         } catch (XmcdMissingInformationException e) {
             throw new XmcdMissingInformationException("Error while parsing " + currentEntry.getName(), e);
         } catch (XmcdFormatException e) {
             throw new XmcdFormatException("Error while parsing " + currentEntry.getName(), e);
         } catch (Exception e) {
             throw new XmcdException("Error while parsing " + currentEntry.getName(), e);
         }
         return xmcdDisc;
     }
 
     @Override
     public void remove() {
         throw new UnsupportedOperationException();
     }
 
     @Override
     public void close() {
         try {
             tar.close();
         } catch (IOException ex) {
             // Suppress
         }
     }
     
     private void findNextEntry() throws IOException {
         while ((nextEntry = tar.getNextTarEntry()) != null) {
             if (nextEntry.isDirectory()) {
                 // Skip directories
                 continue;
             }
             if (nextEntry.getSize() == 0) {
                 // Skip empty files
                 continue;
             }
             int bytesLeft = (int)nextEntry.getSize();
             byte[] nextEntryBytes = new byte[bytesLeft];
             while (bytesLeft > 0) {
                 bytesLeft -= tar.read(nextEntryBytes, nextEntryBytes.length - bytesLeft, bytesLeft);
             }
             nextEntryText = new String(nextEntryBytes);
             if (onlyAscii && !CharMatcher.ASCII.matchesAllOf(nextEntryText)) {
                 // Skip non-ASCII files if requested
                 continue;
             }
             tar.getRecordSize();
             // Found the next entry
             break;
         }
     }
 
     private TarArchiveInputStream tar;
     private TarArchiveEntry currentEntry;
     private TarArchiveEntry nextEntry;
     private boolean onlyAscii = false;
     private String entryText = null;
     private String nextEntryText;
 }
