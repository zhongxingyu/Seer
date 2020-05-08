 package my.triviagame.xmcd;
 
 import com.google.common.base.Function;
 import com.google.common.collect.Iterators;
 import java.io.Closeable;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.IOException;
 import java.util.Iterator;
 import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
 import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
 import org.apache.commons.compress.compressors.bzip2.BZip2CompressorInputStream;
 import org.apache.commons.io.IOUtils;
 
 /**
  * Decompressed an xmcd archive and streams records as {@link XmcdDisc}s.
  */
 public class XmcdDiscStream implements Iterator<XmcdDisc>, Closeable {
 
     /**
      * Extracts an xmcd archive and streams the records within.
      */
     public XmcdDiscStream(File archive) throws XmcdExtractionException {
         try {
             FileInputStream fin = new FileInputStream(archive);
             BZip2CompressorInputStream bzip2 = new BZip2CompressorInputStream(fin);
             tar = new TarArchiveInputStream(bzip2);
             nextEntry = tar.getNextTarEntry();
         } catch (IOException e) {
             throw new XmcdExtractionException(String.format("Could not open file %s", archive.getName()), e);
         }
     }
 
     /**
      * Extracts several xmcd archives and streams the records within.
      */
     public static Iterator<XmcdDisc> extractAll(Iterable<File> archives) throws IOException {
         Iterator<XmcdDiscStream> xmcdStreams = Iterators.transform(archives.iterator(),
                 new Function<File, XmcdDiscStream>() {
                     @Override
                     public XmcdDiscStream apply(File file) throws XmcdException {
                         return new XmcdDiscStream(file);
                     }
                 });
         return Iterators.concat(xmcdStreams);
     }
 
     @Override
     public boolean hasNext() {
         return nextEntry != null;
     }
 
     @Override
     public XmcdDisc next() throws XmcdExtractionException, XmcdMissingInformationException {
         XmcdDisc xmcdDisc;
         try {
             // Get to the next file, skipping any directory entries along the way
             while ((nextEntry != null) && (nextEntry.isDirectory())) {
                 nextEntry = tar.getNextTarEntry();
             }
             currentEntry = nextEntry;
             // Read the next file
             String currentFileContents = IOUtils.toString(tar);
             // Advance to the next file for the next call.
             // This allows continuing to the next file if parsing the current file fails.
            nextEntry = tar.getNextTarEntry();
             // Parse the next file
            String dirName = new File(currentEntry.getName()).getParentFile().getName();
            FreedbGenre freedbGenre = FreedbGenre.fromDirectoryName(dirName);
             xmcdDisc = XmcdDisc.fromXmcdFile(currentFileContents, freedbGenre);
         } catch (IOException e) {
             throw new XmcdExtractionException("Error while unarchiving", e);
         } catch (IllegalArgumentException e) {
             throw new XmcdFormatException(
                     String.format("Invalid directory name %s", currentEntry.getFile().getParentFile().getName()));
         } catch (XmcdMissingInformationException e) {
             throw new XmcdMissingInformationException(
                     String.format("Error while parsing %s", currentEntry.getName()), e);
         } catch (XmcdFormatException e) {
             throw new XmcdFormatException(
                     String.format("Error while parsing %s", currentEntry.getName()), e);
         }
         return xmcdDisc;
     }
 
     @Override
     public void remove() {
         throw new UnsupportedOperationException();
     }
 
     @Override
     public void close() throws IOException {
         tar.close();
     }
 
     private TarArchiveInputStream tar;
     private TarArchiveEntry currentEntry;
     private TarArchiveEntry nextEntry;
 }
