 package suncertify.parser;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import suncertify.db.RecordNotFoundException;
 
 /**
  * The Class DBPresenter provides communication between the database and application.
  */
 public class DBPresenter {
   private static DBPresenter instance;
 
   private String dbPath;
   private String dbHost;
   private String dbPort;
   private int magicCookie;
   private byte[] fileHeader;
   private int fieldsNumber;
   private List<DBRecord> records;
   private long newRecordNumber;
 
   private DBPresenter(String dbPath, String dbHost, String dbPort) {
     this.dbPath = dbPath;
     this.dbHost = dbHost;
     this.dbPort = dbPort;
   }
 
   /**
    * Gets the single instance of DBPresenter.
    * 
    * @return single instance of DBPresenter
    */
   public static synchronized DBPresenter getInstance() {
     if (instance == null || !instance.getDbPath().equals(PropertiesLoader.getInstance().getDbLocation())
         || !instance.getDbHost().equals(PropertiesLoader.getInstance().getDbHost())
         || !instance.getDbPort().equals(PropertiesLoader.getInstance().getDbPort())) {
       instance = new DBPresenter(PropertiesLoader.getInstance().getDbLocation(), PropertiesLoader.getInstance()
                                                                                                  .getDbHost(),
                                  PropertiesLoader.getInstance().getDbPort());
       DBReaderWriter.createDatabasePresenter();
     }
     return instance;
   }
 
   protected long getMagicCookie() {
     return magicCookie;
   }
 
   protected void setMagicCookie(int magicCookie) {
     this.magicCookie = magicCookie;
   }
 
   protected void setFileHeader(byte[] fileHeader) {
     this.fileHeader = fileHeader;
   }
 
   protected byte[] getFileHeader() {
     return fileHeader;
   }
 
   protected long getFieldsNumber() {
     return fieldsNumber;
   }
 
   protected void setFieldsNumber(int fieldsNumber) {
     this.fieldsNumber = fieldsNumber;
   }
 
   /**
    * Gets the records.
    * 
    * @return the records
    */
   public List<DBRecord> getRecords() {
     if (records == null) {
       records = new ArrayList<DBRecord>();
     }
     return records;
   }
 
   protected void setRecords(List<DBRecord> records) {
     this.records = records;
   }
 
   /**
    * Gets the database path.
    * 
    * @return the database path
    */
   public String getDbPath() {
     return dbPath;
   }
 
   /**
    * Gets the database host.
    * 
    * @return the database host
    */
   public String getDbHost() {
     return dbHost;
   }
 
   /**
    * Gets the database port.
    * 
    * @return the database port
    */
   public String getDbPort() {
     return dbPort;
   }
 
   protected void setNewRecordNumber(long newRecordNumber) {
     this.newRecordNumber = newRecordNumber;
   }
 
   protected long getNewRecordNumber() {
     return newRecordNumber;
   }
 
   protected void addRecord(DBRecord record) {
     record.setPosition(newRecordNumber++);
     getRecords().add(record);
   }
 
   /**
    * Gets the record.
    * 
    * @param recNo the rec no
    * @return the record
    * @throws RecordNotFoundException the record not found exception
    */
   public DBRecord getRecord(long recNo) throws RecordNotFoundException {
     return getRecord(recNo, true);
   }
 
   /**
    * Gets the record.
    * 
    * @param recNo the rec no
    * @param checkValid the check valid
    * @return the record
    * @throws RecordNotFoundException the record not found exception
    */
   public DBRecord getRecord(long recNo, boolean checkValid) throws RecordNotFoundException {
    for (DBRecord record : getRecords()) {
       if (record.getPosition() == recNo && (!checkValid || record.isValid())) {
         return record;
       }
     }
     throw new RecordNotFoundException("No record found with number: " + recNo);
   }
 
   /*
    * (non-Javadoc)
    * @see java.lang.Object#toString()
    */
   @Override
   public String toString() {
     StringBuffer sb = new StringBuffer();
     sb.append("DBPresenter[");
     sb.append("dbPath=").append(dbPath);
     sb.append(", dbHost=").append(dbHost);
     sb.append(", dbPort=").append(dbPort);
     sb.append(", magicCookie=").append(magicCookie);
     sb.append(", fieldsNumber=").append(fieldsNumber).append("\n");
     for (DBRecord record : getRecords()) {
       sb.append(", ").append(record).append("\n");
     }
     return sb.toString();
   }
 }
