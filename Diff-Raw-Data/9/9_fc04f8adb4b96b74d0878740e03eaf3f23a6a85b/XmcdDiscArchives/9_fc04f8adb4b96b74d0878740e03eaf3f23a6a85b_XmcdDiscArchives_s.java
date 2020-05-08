 package my.triviagame.xmcd;
 
 import com.google.common.base.Preconditions;
 import java.io.File;
 import java.io.IOException;
 import java.util.Iterator;
 
 /**
  * Chains several FreeDB archives and acts as a single archive.
  */
 public class XmcdDiscArchives implements CloseableIterator<XmcdDisc> {
 
     /**
      * Extracts several xmcd archives and streams the records within.
      * 
      * @param archives the archive files to extract & parse
      * @param onlyAscii whether to allow only ASCII text, skipping non-ASCII records
      */
     public XmcdDiscArchives(Iterable<File> files, boolean onlyAscii) {
         Preconditions.checkArgument(files.iterator().hasNext(), "Need at least one file to stream");
         nextFiles = files.iterator();
         currentStream = new XmcdDiscArchive(nextFiles.next(), onlyAscii);
         this.onlyAscii = onlyAscii;
     }
     
     @Override
     public boolean hasNext() {
         while (!currentStream.hasNext()) {
             currentStream.close();
             if (nextFiles.hasNext()) {
                 currentStream = new XmcdDiscArchive(nextFiles.next(), onlyAscii);
             } else {
                 return false;
             }
         }
         return true;
     }
 
     @Override
     public XmcdDisc next() {
         return currentStream.next();
     }
 
     @Override
     public void remove() {
         throw new UnsupportedOperationException("Not supported yet.");
     }
 
     @Override
     public void close() throws IOException {
         currentStream.close();
     }
 
     private XmcdDiscArchive currentStream;
     private Iterator<File> nextFiles;
     boolean onlyAscii;
 }
