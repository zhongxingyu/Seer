 package fedora.server.storage;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.InputStream;
 import java.io.IOException;
 import java.util.HashMap;
 import java.util.Iterator;
 
 import fedora.server.Context;
 import fedora.server.Logging;
 import fedora.server.StdoutLogging;
 import fedora.server.errors.ObjectNotFoundException;
 import fedora.server.errors.ObjectIntegrityException;
 import fedora.server.errors.ServerException;
 import fedora.server.errors.StorageDeviceException;
 import fedora.server.errors.StreamIOException;
 import fedora.server.errors.UnsupportedTranslationException;
 import fedora.server.storage.translation.DOTranslator;
 
 /**
  * A RepositoryReader that uses a directory of serialized objects
  * as its working repository.
  * <p></p>
  * All files in the directory must be digital object serializations,
  * and none may have the same PID.  This is verified upon construction.
  * <p></p>
  * Note: This implementation does not recognize when files are added
  * to the directory.  What is in the directory at construction-time
  * is what is assumed to be the extent of the repository for the life
  * of the object.
  *
  * @author cwilper@cs.cornell.edu
  */
 public class DirectoryBasedRepositoryReader 
         extends StdoutLogging
         implements RepositoryReader {
         
     private File m_directory;
     private DOTranslator m_translator;
     private String m_shortExportFormat;
     private String m_longExportFormat;
     private String m_storageFormat;
     private String m_encoding;
     private HashMap m_files=new HashMap();
  
     /**
      * Initializes the RepositoryReader by looking at all files in the
      * provided directory and ensuring that they're all serialized
      * digital objects and that there are no PID conflicts.
      *
      * @param directory the directory where this repository is based.
      * @param translator the serialization/deserialization engine for objects.
      * @param shortExportFormat the format to use for getObjectXML requests.
      * @param longExportFormat the format to use for exportObject requests.
      * @param storageFormat the format of the objects on disk.
      * @param encoding The character encoding used across all formats.
      */
     public DirectoryBasedRepositoryReader(File directory, DOTranslator translator,
             String shortExportFormat, String longExportFormat, String storageFormat,
             String encoding, Logging logTarget) 
             throws StorageDeviceException, ObjectIntegrityException, 
             StreamIOException, UnsupportedTranslationException,
             ServerException {
         super(logTarget);
         m_directory=directory;
         m_translator=translator;
         m_shortExportFormat=shortExportFormat;
         m_longExportFormat=longExportFormat;
         m_storageFormat=storageFormat;
         m_encoding=encoding;
         File[] files=directory.listFiles();
         try {
             for (int i=0; i<files.length; i++) {
                 FileInputStream in=new FileInputStream(files[i]);
                 SimpleDOReader reader=new SimpleDOReader(null, this, m_translator,
                         m_shortExportFormat, m_longExportFormat, m_storageFormat,
                         m_encoding, in, this);
                 String pid=reader.GetObjectPID();
                if (reader.GetObjectPID().length()==0) {
                    logWarning("File " + files[i] + " has no pid...skipping");
                } else {
                    m_files.put(pid, files[i]);
                }
             }
         } catch (FileNotFoundException fnfe) {
             // won't happen
         }
     }
     
     private InputStream getStoredObjectInputStream(String pid) 
             throws ObjectNotFoundException {
         try {
            return new FileInputStream((File) m_files.get(pid));
         } catch (Throwable th) {
             throw new ObjectNotFoundException("The object, " + pid + " was "
                     + "not found in the repository.");
         }
     }
         
     public DOReader getReader(Context context, String pid) 
             throws ObjectIntegrityException, ObjectNotFoundException, 
             StreamIOException, UnsupportedTranslationException, ServerException {
         return new SimpleDOReader(null, this, m_translator,
                 m_shortExportFormat, m_longExportFormat, m_storageFormat,
                 m_encoding, getStoredObjectInputStream(pid), this);
     }
             
     public BMechReader getBMechReader(Context context, String pid) 
             throws ObjectIntegrityException, ObjectNotFoundException, 
             StreamIOException, UnsupportedTranslationException, ServerException {
         return new SimpleBMechReader(null, this, m_translator,
                 m_shortExportFormat, m_longExportFormat, m_storageFormat,
                 m_encoding, getStoredObjectInputStream(pid), this);
     }
 
     public BDefReader getBDefReader(Context context, String pid)
             throws ObjectIntegrityException, ObjectNotFoundException, 
             StreamIOException, UnsupportedTranslationException, ServerException {
         return new SimpleBDefReader(null, this, m_translator,
                 m_shortExportFormat, m_longExportFormat, m_storageFormat,
                 m_encoding, getStoredObjectInputStream(pid), this);
     }
 
     public String[] listObjectPIDs(Context context) {
         String[] out=new String[m_files.keySet().size()];
         Iterator iter=m_files.keySet().iterator();
         int i=0;
         while (iter.hasNext()) {
             out[i++]=(String) iter.next();
         }
         return out;
     }
 
 }
