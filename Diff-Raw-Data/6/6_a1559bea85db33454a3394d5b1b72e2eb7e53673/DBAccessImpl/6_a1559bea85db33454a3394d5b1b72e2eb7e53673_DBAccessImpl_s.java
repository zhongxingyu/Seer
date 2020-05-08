 package suncertify.db;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import suncertify.parser.DBPresenter;
 import suncertify.parser.DBReaderWriter;
 import suncertify.parser.DBRecord;
 import suncertify.parser.DBRecordHelper;
 
 public class DBAccessImpl {
   private static final Logger log = LoggerFactory.getLogger(DBAccessImpl.class);
 
   private static DBAccessImpl dbAccessImpl;
 
   private long cookie;
 
   private DBAccessImpl() {
   }
 
   public static synchronized DBAccessImpl getInstance() {
     if (dbAccessImpl == null) {
       dbAccessImpl = new DBAccessImpl();
     }
     return dbAccessImpl;
   }
 
   public String[] readRecord(long recNo) throws RecordNotFoundException {
     validateRecordNumber(recNo);
     DBPresenter presenter = DBPresenter.getInstance();
     DBRecord record = presenter.getRecord(recNo);
     return DBRecordHelper.getDBRecordAsStringArray(record);
   }
 
   public void updateRecord(long recNo, String[] data, long lockCookie) throws RecordNotFoundException,
                                                                       SecurityException {
     validateRecordNumber(recNo);
     DBPresenter presenter = DBPresenter.getInstance();
     DBRecord record = presenter.getRecord(recNo);
     if (record.getCookie() != lockCookie) {
       throw new SecurityException("The record " + recNo + " with cookie " + record.getCookie()
                                   + " can't be updated with cookie " + lockCookie);
     }
     try {
       DBReaderWriter.updateRecord(record);
     } catch (Exception e) {
       log.info(e.getMessage(), e);
     }
   }
 
   public void deleteRecord(long recNo, long lockCookie) throws RecordNotFoundException, SecurityException {
     validateRecordNumber(recNo);
     DBPresenter presenter = DBPresenter.getInstance();
     DBRecord record = presenter.getRecord(recNo);
     if (record.getCookie() != lockCookie) {
       throw new SecurityException("The record " + recNo + " with cookie " + record.getCookie()
                                   + " can't be deleted with cookie " + lockCookie);
     }
     try {
       DBReaderWriter.deleteRecord(record);
     } catch (Exception e) {
       log.info(e.getMessage(), e);
     }
   }
 
   public long[] findByCriteria(String[] criteria) {
     if (criteria == null || criteria.length != 6) {
       return null;
     }
     DBPresenter presenter = DBPresenter.getInstance();
     List<DBRecord> findedRecords = new ArrayList<DBRecord>();
     for (DBRecord record : presenter.getRecords()) {
       if (!record.isValid()) {
         continue;
       }
       String[] recordArray = DBRecordHelper.getDBRecordAsStringArray2(record);
       boolean isMached = true;
       for (int i = 0; i < recordArray.length; i++) {
         if (criteria[i] != null && !recordArray[i].startsWith(criteria[i])) {
           isMached = false;
           break;
         }
       }
       if (isMached) {
         findedRecords.add(record);
       }
     }
     long[] findedNumbers = new long[findedRecords.size()];
     int count = 0;
     for (DBRecord record : findedRecords) {
       findedNumbers[count++] = record.getPosition();
     }
     return findedNumbers;
   }
 
   public long createRecord(String[] data) throws DuplicateKeyException {
     DBPresenter presenter = DBPresenter.getInstance();
     DBRecord record = DBRecordHelper.createDBRecord(data);
     if (presenter.getRecords().contains(record)) {
       throw new DuplicateKeyException("Database contains: " + record);
     }
     try {
       DBReaderWriter.addRecord(data);
     } catch (Exception e) {
       log.info(e.getMessage(), e);
     }
     return record.getPosition();
   }
 
   public long lockRecord(long recNo) throws RecordNotFoundException {
     // System.out.println("try to lockRecord: " + recNo);
     validateRecordNumber(recNo);
     DBPresenter presenter = DBPresenter.getInstance();
     DBRecord record = presenter.getRecord(recNo);
     record.lockRecord();
     record.setCookie(++cookie);
     // System.out.println("lockRecord: " + recNo + " with cookie: " + cookie);
     return cookie;
   }
 
   public void unlock(long recNo, long cookie) throws SecurityException {
     // System.out.println("try to unlock: " + recNo + " with cookie: " + cookie);
     DBPresenter presenter = DBPresenter.getInstance();
     DBRecord record = null;
     try {
       record = presenter.getRecord(recNo);
     } catch (RecordNotFoundException e) {
       log.error(e.getMessage(), e);
       return;
     }
     if (record.getCookie() != cookie) {
       throw new SecurityException("The record " + recNo + " is locked with cookie: " + record.getCookie()
                                   + ". You cant' unlock this record with cookie: " + cookie);
     }
     record.unlockRecord();
     // System.out.println("unlocked");
   }
 
   private void validateRecordNumber(long recNo) throws RecordNotFoundException {
     if (recNo > Integer.MAX_VALUE) {
       throw new RecordNotFoundException("The record number is over than Integer.MAX_VALUE");
     }
   }
 }
