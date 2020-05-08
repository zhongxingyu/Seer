 package suncertify.parser;
 
 import static java.lang.System.out;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import java.io.BufferedInputStream;
 import java.io.DataInputStream;
 import java.io.EOFException;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.util.Date;
 
 import suncertify.constants.StringPool;
 
 import suncertify.constants.FileUtils;
 
 import suncertify.mock.TestData;
 
 import suncertify.constants.Variables;
 
 import suncertify.constants.StringUtils;
 
 public class DbReaderWriter {
   private static final Logger log = LoggerFactory.getLogger(DbReaderWriter.class);
 
   public static void main(String... args) throws IOException {
     // main method is only for test!!
     log.info("START");
     FileUtils.copyFile(Variables.getOriginalFilePath(), Variables.getWorkedFilePath());
     DBPresenter presenter = createDatabasePresenter();
 
    // log.info("{}", presenter);
   }
 
  private void writeTestData() {
     DbReaderWriter db = new DbReaderWriter();
     try {
       log.info("try to write");
       for (int i = 0; i < 10; i++) {
         db.writeRecord(TestData.getRecord());
       }
     } catch (Exception e) {
       log.info(e.getMessage(), e);
     }
   }
 
   public static DBPresenter createDatabasePresenter() throws IOException {
     DBPresenter presenter = DBPresenter.getInstance();
     File file = new File(presenter.getDbPath());
     FileInputStream fis = new FileInputStream(file);
     BufferedInputStream bis = new BufferedInputStream(fis);
     DataInputStream dis = new DataInputStream(bis);
     // log.info("---Header---");
 
     int magicCookie = dis.readInt();
     presenter.setMagicCookie(magicCookie);
     // log.info("Magic cookie: %d", magicCookie);
 
     int fieldsNumber = dis.readUnsignedShort();
     // log.info("Number of fields in each record: {}", fieldsNumber);
     presenter.setFieldsNumber(fieldsNumber);
     // log.info("---Schema---");
     StringBuilder sb = new StringBuilder();
     for (int i = 0; i < fieldsNumber; i++) {
       int recordNameLength = dis.readUnsignedByte();
       for (int j = 0; j < recordNameLength; j++) {
         sb.append((char) dis.readUnsignedByte());
       }
       int fieldLength = dis.readUnsignedByte();
       log.info("Field name: '{}'\tField length: {}", sb.toString(), fieldLength);
       sb.setLength(0);
     }
     // log.info("data");
     try {
       log.info("{}\t{}\t{}\t{}\t{}\t{}\t{}", new Object[] { "r.getValid()", "r.getName()", "r.getLocation()", "r.getSpecialities()", "r.getNumberOfWorkers()", "r.getRate()",
                                                            "r.getOwner()" });
       int count = 0;
       while (true) {
         count++;
         Record r = readNextRecord(dis);
         DBRecord record = new DBRecord();
         log.info("{}\t{}\t{}\t{}\t{}\t{}\t{}",
                  new Object[] { r.getValid(), r.getName(), r.getLocation(), r.getSpecialities(), r.getNumberOfWorkers(), r.getRate(), r.getOwner() });
         record.setPosition(count);
         record.setValid(r.getValid());
         record.setName(r.getName());
         record.setLocation(r.getLocation());
         record.setSpecialties(r.getSpecialities());
         record.setNumberOfWorkers(r.getNumberOfWorkers());
         record.setRate(r.getRate());
         record.setOwner(r.getOwner());
         presenter.getRecords().add(record);
       }
     } catch (EOFException e) {
       // log.error(e.getMessage(), e);
     }
 
     dis.close();
     bis.close();
     fis.close();
 
     // log.info("{}", presenter);
     out.printf("Successful: " + new Date().toString());
     return presenter;
   }
 
   public long writeRecord(String[] data) throws Exception {
     long recordNumber = 0;
     File file = new File(DBPresenter.getInstance().getDbPath());
     // log.info("write to dbPath: {}", DBPresenter.getInstance().getDbPath());
     FileWriter fw = new FileWriter(file, true);
     fw.write(Variables.TERMINATOR);
     for (int i = 0; i < data.length; i++) {
       // writeNext(fw, "", 8);// valid
       String d = data[i];
       switch (i) {
       case 0:// name
         writeNext(fw, d, 32);
         break;
       case 1:// location
         writeNext(fw, d, 64);
         break;
       case 2:// specialties
         writeNext(fw, d, 64);
         break;
       case 3:// number of workers
         writeNext(fw, d, 6);
         break;
       case 4:// rate
         writeNext(fw, d, 8);
         break;
       case 5:// owner
         writeNext(fw, d, 8);
         break;
       }
     }
     fw.flush();
     fw.close();
     DBRecordHelper.addRecord(data);
     // log.info("Returned number of record: {}", DBPresenter.getInstance().getRecords().size());
     return recordNumber;
   }
 
   private static void writeNext(FileWriter fw, String data, int maxLength) throws Exception {
     if (fw == null) {
       throw new RuntimeException("FileWriter can't be null!");
     }
     if (data == null) {
       data = StringPool.BLANK;
     }
     if (data.length() <= maxLength) {
       fw.write(data);
       if (data.length() < maxLength) {
         fw.write(Variables.TERMINATOR);
       }
 
     } else {
       fw.write(data.substring(0, maxLength - 1));
     }
   }
 
   private static Record readNextRecord(DataInputStream dis) throws IOException {
     Record record = new Record();
     record.setValid(readStringField(dis, 8));
     record.setName(readStringField(dis, 32));
     record.setLocation(readStringField(dis, 64));
     record.setSpecialities(readStringField(dis, 64));
     record.setNumberOfWorkers(readStringField(dis, 6));
     record.setRate(readStringField(dis, 8));
     record.setOwner(readStringField(dis, 8));
     return record;
   }
 
   private static String readStringField(DataInputStream dis, int length) throws IOException {
     if (0 == length) {
       return null;
     }
     StringBuilder sb = new StringBuilder(length);
     for (int i = 0; i < length; i++) {
       int b = dis.readUnsignedByte();
       if (0x0 == b) {
         break;
       }
       sb.append((char) b);
     }
     return sb.toString();
   }
 
   private static class Record {
 
     private String valid;
     private String name;
     private String location;
     private String specialities;
     private String numberOfWorkers;
     private String rate;
     private String owner;
 
     public void setValid(String valid) {
       this.valid = valid;
     }
 
     public String getValid() {
       return valid;
     }
 
     public void setName(String name) {
       this.name = name;
     }
 
     public String getName() {
       return name;
     }
 
     public String getLocation() {
       return location;
     }
 
     public void setLocation(String location) {
       this.location = location;
     }
 
     public String getSpecialities() {
       return specialities;
     }
 
     public void setSpecialities(String specialities) {
       this.specialities = specialities;
     }
 
     public String getNumberOfWorkers() {
       return numberOfWorkers;
     }
 
     public void setNumberOfWorkers(String numberOfWorkers) {
       this.numberOfWorkers = numberOfWorkers;
     }
 
     public String getRate() {
       return rate;
     }
 
     public void setRate(String rate) {
       this.rate = rate;
     }
 
     public String getOwner() {
       return owner;
     }
 
     public void setOwner(String owner) {
       this.owner = owner;
     }
   }
 }
