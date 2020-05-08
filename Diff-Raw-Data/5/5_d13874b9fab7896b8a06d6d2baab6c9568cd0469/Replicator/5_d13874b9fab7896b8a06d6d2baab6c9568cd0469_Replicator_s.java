 package org.nebulostore.replicator;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.util.HashSet;
 import java.util.Hashtable;
 import java.util.Set;
 import java.util.concurrent.BlockingQueue;
 import java.util.concurrent.Semaphore;
 import java.util.concurrent.TimeUnit;
 
 import com.google.inject.Inject;
 
 import org.apache.commons.configuration.XMLConfiguration;
 import org.apache.log4j.Logger;
 import org.nebulostore.api.GetEncryptedObjectModule;
 import org.nebulostore.appcore.JobModule;
 import org.nebulostore.appcore.Message;
 import org.nebulostore.appcore.MessageVisitor;
 import org.nebulostore.appcore.addressing.AppKey;
 import org.nebulostore.appcore.addressing.ObjectId;
 import org.nebulostore.appcore.exceptions.NebuloException;
 import org.nebulostore.appcore.model.EncryptedObject;
 import org.nebulostore.communication.address.CommAddress;
 import org.nebulostore.crypto.CryptoUtils;
 import org.nebulostore.replicator.messages.ConfirmationMessage;
 import org.nebulostore.replicator.messages.DeleteObjectMessage;
 import org.nebulostore.replicator.messages.GetObjectMessage;
 import org.nebulostore.replicator.messages.ObjectOutdatedMessage;
 import org.nebulostore.replicator.messages.QueryToStoreObjectMessage;
 import org.nebulostore.replicator.messages.ReplicatorErrorMessage;
 import org.nebulostore.replicator.messages.SendObjectMessage;
 import org.nebulostore.replicator.messages.TransactionResultMessage;
 import org.nebulostore.replicator.messages.UpdateRejectMessage;
 import org.nebulostore.replicator.messages.UpdateWithholdMessage;
 import org.nebulostore.replicator.messages.UpdateWithholdMessage.Reason;
 import org.nebulostore.utils.Pair;
 
 import static com.google.common.base.Preconditions.checkNotNull;
 
 // TODO(szm): cloning object before send. java cloning library?
 // TODO(bolek, szm): Refactor static methods into non-static?
 
 /**
  * Replicator - disk interface.
  * @author szymonmatejczyk
  */
 public class Replicator extends JobModule {
   private static Logger logger_ = Logger.getLogger(Replicator.class);
   private static final String CONFIG_PREFIX = "replicator.";
 
   private static final int UPDATE_TIMEOUT_SEC = 10;
   private static final int LOCK_TIMEOUT_SEC = 10;
   private static final int GET_OBJECT_TIMEOUT_SEC = 10;
 
   private static XMLConfiguration config_;
   private static String pathPrefix_;
 
   // Hashtable is synchronized.
   //TODO(szm): filesLocations and previousVersions should be stored on disk!!
   private static Hashtable<ObjectId, String> filesLocations_ = new Hashtable<ObjectId, String>(256);
   // NOTE: Including current version.
   private static Hashtable<ObjectId, Set<String>> previousVersions_ = new Hashtable<ObjectId,
       Set<String>>();
   private static Hashtable<ObjectId, Boolean> freshnessMap_ = new Hashtable<ObjectId, Boolean>(256);
   private static Hashtable<ObjectId, Semaphore> locksMap_ = new Hashtable<ObjectId, Semaphore>();
   private static AppKey appKey_;
 
   private final MessageVisitor<Void> visitor_;
 
   @Inject
   public static void setConfig(XMLConfiguration config) {
     config_ = config;
     pathPrefix_ = config_.getString(CONFIG_PREFIX + "storage-path");
   }
 
   @Inject
   public static void setAppKey(AppKey appKey) {
     appKey_ = appKey;
   }
 
   public Replicator(String jobId, BlockingQueue<Message> inQueue, BlockingQueue<Message> outQueue) {
     super(jobId);
     checkNotNull(config_);
     checkNotNull(pathPrefix_);
     logger_.debug("Replicator ctor");
     setInQueue(inQueue);
     setOutQueue(outQueue);
     visitor_ = new ReplicatorVisitor();
   }
 
   /**
    * Result of queryToStore.
    */
   private enum QueryToStoreResult { OK, OBJECT_OUT_OF_DATE, INVALID_VERSION, SAVE_FAILED, TIMEOUT }
 
   /**
    * Visitor to handle different message types. It calls static methods and returns
    * results via queues.
    * @author szymonmatejczyk
    */
   private class ReplicatorVisitor extends MessageVisitor<Void> {
     private QueryToStoreObjectMessage storeWaitingForCommit_;
 
     @Override
     public Void visit(QueryToStoreObjectMessage message) throws NebuloException {
       logger_.debug("StoreObjectMessage received");
       jobId_ = message.getId();
 
       QueryToStoreResult result = queryToUpdateObject(message.getObjectId(),
           message.getEncryptedEntity(), message.getPreviousVersionSHAs());
       switch (result) {
         case OK:
           networkQueue_.add(new ConfirmationMessage(message.getSourceJobId(), message
               .getDestinationAddress(), message.getSourceAddress()));
           storeWaitingForCommit_ = message;
           try {
             TransactionResultMessage m = (TransactionResultMessage) inQueue_.poll(LOCK_TIMEOUT_SEC,
                 TimeUnit.SECONDS);
             if (m == null) {
               abortUpdateObject(message.getObjectId());
               logger_.warn("Transaction aborted - timeout.");
             } else {
               processMessage(m);
             }
           } catch (InterruptedException exception) {
             abortUpdateObject(message.getObjectId());
             throw new NebuloException("Timeout while handling QueryToStoreObjectMessage",
                 exception);
           } catch (ClassCastException exception) {
             abortUpdateObject(message.getObjectId());
             throw new NebuloException("Wrong message type received.", exception);
           }
           endJobModule();
           break;
         case OBJECT_OUT_OF_DATE:
           networkQueue_.add(new UpdateWithholdMessage(message.getSourceJobId(),
               message.getDestinationAddress(), message.getSourceAddress(),
               Reason.OBJECT_OUT_OF_DATE));
           endJobModule();
           break;
         case INVALID_VERSION:
           networkQueue_.add(new UpdateRejectMessage(message.getSourceJobId(),
               message.getDestinationAddress(), message.getSourceAddress()));
           endJobModule();
           break;
         case SAVE_FAILED:
           networkQueue_.add(new UpdateWithholdMessage(message.getSourceJobId(),
               message.getDestinationAddress(), message.getSourceAddress(), Reason.SAVE_FAILURE));
           break;
         case TIMEOUT:
           networkQueue_.add(new UpdateWithholdMessage(message.getSourceJobId(),
               message.getDestinationAddress(), message.getSourceAddress(), Reason.TIMEOUT));
           endJobModule();
           break;
         default:
           break;
       }
       return null;
     }
 
     @Override
     public Void visit(TransactionResultMessage message) {
       logger_.debug("TransactionResultMessage received: " + message.getResult());
       if (storeWaitingForCommit_ == null) {
         //TODO(szm): ignore late abort transaction messages send by timer.
         logger_.warn("Unexpected commit message received.");
         endJobModule();
         return null;
       }
       if (message.getResult() == TransactionAnswer.COMMIT) {
         commitUpdateObject(storeWaitingForCommit_.getObjectId(),
                            storeWaitingForCommit_.getPreviousVersionSHAs(),
                            CryptoUtils.sha(storeWaitingForCommit_.getEncryptedEntity()));
       } else {
         abortUpdateObject(storeWaitingForCommit_.getObjectId());
       }
       endJobModule();
       return null;
     }
 
     @Override
     public Void visit(GetObjectMessage message) {
       logger_.debug("GetObjectMessage with objectID = " + message.getObjectId());
       jobId_ = message.getId();
       EncryptedObject enc;
       Set<String> versions;
       try {
         enc = getObject(message.getObjectId());
         versions = previousVersions_.get(message.getObjectId());
       } catch (OutOfDateFileException exception) {
         networkQueue_.add(new ReplicatorErrorMessage(message.getSourceJobId(),
             message.getDestinationAddress(), message.getDestinationAddress(),
             "object out of date"));
         return null;
       }
 
       if (enc == null) {
         dieWithError(message.getSourceJobId(), message.getDestinationAddress(),
             message.getSourceAddress(), "Unable to retrieve object.");
       } else {
         networkQueue_.add(new SendObjectMessage(message.getSourceJobId(),
             message.getDestinationAddress(), message.getSourceAddress(), enc,
             versions));
       }
       endJobModule();
       return null;
     }
 
     @Override
     public Void visit(DeleteObjectMessage message) {
       jobId_ = message.getId();
       try {
         deleteObject(message.getObjectId());
         networkQueue_.add(new ConfirmationMessage(message.getSourceJobId(), message
             .getDestinationAddress(), message.getSourceAddress()));
       } catch (DeleteObjectException exception) {
         logger_.warn(exception.toString());
         dieWithError(message.getSourceJobId(), message.getDestinationAddress(),
             message.getSourceAddress(), exception.getMessage());
       }
       endJobModule();
       return null;
     }
 
     @Override
     public Void visit(ObjectOutdatedMessage message) {
       jobId_ = message.getId();
       freshnessMap_.put(message.getAddress().getObjectId(), false);
       try {
         GetEncryptedObjectModule getModule = new GetEncryptedObjectModule(message.getAddress(),
             outQueue_);
         Pair<EncryptedObject, Set<String>> res = getModule.getResult(GET_OBJECT_TIMEOUT_SEC);
         EncryptedObject encryptedObject = res.getFirst();
         try {
           deleteObject(message.getAddress().getObjectId());
         } catch (DeleteObjectException exception) {
           logger_.warn("Error deleting file.");
         }
 
         QueryToStoreResult query = queryToUpdateObject(message.getAddress().getObjectId(),
             encryptedObject, res.getSecond());
         if (query == QueryToStoreResult.OK || query == QueryToStoreResult.OBJECT_OUT_OF_DATE) {
           commitUpdateObject(message.getAddress().getObjectId(), res.getSecond(),
               CryptoUtils.sha(encryptedObject));
           freshnessMap_.put(message.getAddress().getObjectId(), true);
         } else
           throw new NebuloException("Unable to fetch new version of file.");
       } catch (NebuloException exception) {
         logger_.warn(exception);
       }
       return null;
     }
 
     private void dieWithError(String jobId, CommAddress sourceAddress,
         CommAddress destinationAddress, String errorMessage) {
       networkQueue_.add(new ReplicatorErrorMessage(jobId, sourceAddress,
           destinationAddress, errorMessage));
       endJobModule();
     }
   }
 
   @Override
   protected void processMessage(Message message) throws NebuloException {
     message.accept(visitor_);
   }
 
   /*
    * Static methods.
    */
 
 
   /**
    * Begins transaction: tries to store object to temporal location.
    */
   public static QueryToStoreResult queryToUpdateObject(ObjectId objectId,
       EncryptedObject encryptedObject, Set<String> previousVersions) {
     logger_.debug("Checking store consistency");
 
     String currentObjectVersion = null;
 
     String location = filesLocations_.get(objectId);
     if (location != null) {
       /* checking whether local file is up to date */
       try {
         currentObjectVersion = CryptoUtils.sha(getObject(objectId));
       } catch (OutOfDateFileException exception) {
         return QueryToStoreResult.OBJECT_OUT_OF_DATE;
       }
 
       /* checking remote file's previous versions */
       if (!previousVersionsMatch(objectId, currentObjectVersion, previousVersions)) {
         return QueryToStoreResult.INVALID_VERSION;
       }
     } else {
       logger_.debug("storing new file");
       location = getLocationPrefix() + objectId.toString();
       locksMap_.put(objectId, new Semaphore(1));
     }
 
     try {
       if (!locksMap_.get(objectId).tryAcquire(UPDATE_TIMEOUT_SEC, TimeUnit.SECONDS)) {
         logger_.warn("Object " + objectId.toString() + " lock timeout in queryToUpdateObject().");
         return QueryToStoreResult.TIMEOUT;
       }
     } catch (InterruptedException exception) {
       logger_.warn("Interrupted while waiting for object lock in queryToUpdateObject()");
       return QueryToStoreResult.TIMEOUT;
     }
 
     String tmpLocation = location + ".tmp";
     File f = new File(tmpLocation);
     f.getParentFile().mkdirs();
     FileOutputStream fos;
     try {
       fos = new FileOutputStream(f);
     } catch (FileNotFoundException e1) {
       logger_.error("Could not open stream in queryToUpdateObject().");
       return QueryToStoreResult.SAVE_FAILED;
     }
 
     try {
       fos.write(encryptedObject.getEncryptedData());
       logger_.debug("File written to tmp location");
     } catch (IOException exception) {
       logger_.error(exception.getMessage());
       return QueryToStoreResult.SAVE_FAILED;
     } finally {
       try {
         fos.close();
       } catch (IOException e) {
         logger_.error("Could not close stream in queryToUpdateObject().");
         return QueryToStoreResult.SAVE_FAILED;
       }
     }
 
     return QueryToStoreResult.OK;
   }
 
   public static void commitUpdateObject(ObjectId objectId, Set<String> previousVersions,
       String currentVersion) {
     logger_.debug("Commit storing object " + objectId.toString());
 
     String location = filesLocations_.get(objectId);
 
     if (location == null) {
       logger_.debug("commiting new file");
       location = getLocationPrefix() + objectId.toString();
     }
 
     File previous = new File(location);
     previous.delete();
 
     File tmp = new File(location + ".tmp");
 
     tmp.renameTo(previous);
 
     if (filesLocations_.get(objectId) == null) {
       previousVersions_.put(objectId, new HashSet<String>(previousVersions));
       previousVersions_.get(objectId).addAll(previousVersions);
       previousVersions_.get(objectId).add(currentVersion);
       logger_.debug("putting into freshness map : " + objectId);
       freshnessMap_.put(objectId, true);
       filesLocations_.put(objectId, location);
     } else {
       previousVersions_.get(objectId).addAll(previousVersions);
       previousVersions_.get(objectId).add(currentVersion);
     }
 
     locksMap_.get(objectId).release();
     logger_.debug("Commit successful");
   }
 
   public static void abortUpdateObject(ObjectId objectId) {
     logger_.debug("Aborting transaction " + objectId.toString());
     String location = filesLocations_.get(objectId);
     boolean newObjectTransaction = false;
     if (location == null) {
       newObjectTransaction = true;
       location = getLocationPrefix() + objectId.toString();
     }
 
     File file = new File(location + ".tmp");
     file.delete();
     locksMap_.get(objectId).release();
     if (newObjectTransaction) {
       // New local object wasn't created.
       locksMap_.remove(objectId);
     }
   }
 
   /**
   * Returns true only if the latest version of the file stored in this replicator belongs to the
    * set of versions known by the peer requesting update.
    */
   private static boolean previousVersionsMatch(ObjectId objectId, String current,
       Set<String> previousVersions) {
    return previousVersions.contains(current);
   }
 
   /**
    * Retrieves object from disk.
    * @return Encrypted object or null if and only if object can't be read from disk(either because
    * it wasn't stored or there was a problem reading file).
    *
    * @throws OutOfDateFileException if object is stored but out of date.
    */
   public static EncryptedObject getObject(ObjectId objectId) throws OutOfDateFileException {
     logger_.debug("getObject with objectID = " + objectId);
     String location = filesLocations_.get(objectId);
     if (location == null) {
       return null;
     }
 
     if (!freshnessMap_.get(objectId))
       throw new OutOfDateFileException();
 
     File file = new File(location);
     FileInputStream fis = null;
     try {
       fis = new FileInputStream(file);
     } catch (FileNotFoundException exception) {
       logger_.warn("Object file not found.");
       return null;
     }
 
     try {
       byte[] content = new byte[(int) (file.length())];
       fis.read(content);
       return new EncryptedObject(content);
     } catch (IOException exception) {
       logger_.warn(exception.toString());
       return null;
     } finally {
       try {
         fis.close();
       } catch (IOException e) {
         logger_.warn("Could not close stream in getObject().");
         return null;
       }
     }
   }
 
   public static void deleteObject(ObjectId objectId) throws DeleteObjectException {
     String location = filesLocations_.get(objectId);
     if (location == null)
       return;
     Semaphore mutex = locksMap_.get(objectId);
     try {
       if (!mutex.tryAcquire(UPDATE_TIMEOUT_SEC, TimeUnit.SECONDS)) {
         logger_.warn("Object " + objectId.toString() + " lock timeout in deleteObject().");
         throw new DeleteObjectException("Timeout while waiting for object lock.");
       }
     } catch (InterruptedException e) {
       logger_.warn("Interrupted while waiting for object lock in deleteObject()");
       throw new DeleteObjectException("Interrupted while waiting for object lock.", e);
     }
 
     filesLocations_.remove(objectId);
     freshnessMap_.remove(objectId);
     previousVersions_.remove(objectId);
     locksMap_.remove(objectId);
     mutex.release();
 
     File f = new File(location);
     if (!f.exists())
       throw new DeleteObjectException("File does not exist.");
     boolean success = f.delete();
     if (!success)
       throw new DeleteObjectException("Unable to delete file.");
   }
 
   private static String getLocationPrefix() {
     checkNotNull(pathPrefix_);
     return pathPrefix_ + "/" + appKey_.getKey().intValue() + "/";
   }
 }
