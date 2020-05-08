 import java.io.ByteArrayInputStream;
 import java.io.ByteArrayOutputStream;
 import java.io.IOException;
 import java.io.ObjectInputStream;
 import java.io.ObjectOutputStream;
 import java.util.ArrayList;
 import java.util.List;
 
 /**
  * Transaction abstraction used whenever transactions must be encapsulated
  * and passed around.
  */
 public class Transaction{
   TransactionId id;
   List<ChangedFile> files;
 
   /**
    * Representation of a single file change, as specified by a transaction.
    */
   public class ChangedFile {
     public static final int kNoCheckoutTime = -1;
 
     DFSFilename name;
     FileVersion version;
     String contents;
     boolean exists;
     int checkoutTime;
 
     public ChangedFile(DFSFilename n, FileVersion v, String c, boolean e, int t) {
       this.name = n;
       this.version = v;
       this.contents = c;
       this.exists = e;
       this.checkoutTime = t;
     }
   }
 
 
   public Transaction(TransactionId id) {
     this(id, null);
   }
 
   public Transaction(TransactionId id,
                      List<PersistentStorageCache.CacheEntry> files) {
     this.id = id;
     this.files = new ArrayList<ChangedFile>();
    if (files != null) {
      for (PersistentStorageCache.CacheEntry e : files) {
        addFile(e);
      }
     }
   }
 
 
   /**
    * Adds a file to the list of files changed within this transaction.
    *
    * @param cachedFile
    *            The cache entry associated with the changed file.
    */
   public void addFile(PersistentStorageCache.CacheEntry f) {
     files.add(new ChangedFile(f.key, 
                               f.version, 
                               f.data, 
                               f.exists, 
                               ChangedFile.kNoCheckoutTime));
   }
 
 
   /**
    * Instantiate a Transaction object containing the data encoded in the
    * argument byte array.
    *
    * @param blob
    *            A serialized Transaction object, encoded as a byte array.
    */
 
   public static Transaction unpack(byte[] blob, int offset) {
     ByteArrayInputStream baIn = null;
     ObjectInputStream objIn = null;
     try {
       baIn = new ByteArrayInputStream(blob, offset, blob.length - offset);
       objIn = new ObjectInputStream(baIn);
       Transaction t = (Transaction) objIn.readObject();
       return t;
     } catch (IOException e) {
       return null;
     } catch (ClassNotFoundException e) {
       return null;
     } finally {
       try {
         if (baIn != null)
           baIn.close();
         if (objIn != null)
           objIn.close();
       } catch (IOException e) { }
     }
   }
 
 
   /**
    * Converts the Transaction object and all enclosed data into a
    * byte array.
    *
    * @return a byte array containing the data encoded in this transaction.
    */
   public byte[] pack() {
     ByteArrayOutputStream baOut = null;
     ObjectOutputStream objOut = null;
     try {
       baOut = new ByteArrayOutputStream();
       objOut = new ObjectOutputStream(baOut);
       objOut.writeObject(this);
       return baOut.toByteArray();
     } catch (IOException e) {
       return null;
     } finally {
       try {
         if (baOut != null)
           baOut.close();
         if (objOut != null)
           objOut.close();
       } catch (IOException e) { }
     }
   }
 
   public String terseDescriptor() {
     return "TX " + id + "; " + files.size() + " changed.";
   }
 }
