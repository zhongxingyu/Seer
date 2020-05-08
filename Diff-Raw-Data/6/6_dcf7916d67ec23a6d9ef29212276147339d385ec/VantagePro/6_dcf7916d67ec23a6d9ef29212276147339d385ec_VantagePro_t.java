 /*
  *
  */
 package uk.me.jstott.jweatherstation;
 
 import java.io.IOException;
 import java.io.OutputStream;
 import java.rmi.RemoteException;
 import java.sql.Timestamp;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.Date;
 import java.util.Iterator;
 import java.util.List;
 
 import javax.comm.CommPortIdentifier;
 import javax.comm.NoSuchPortException;
 import javax.comm.PortInUseException;
 import javax.comm.SerialPort;
 
 import org.tom.weather.ArchiveEntry;
 import org.tom.weather.upload.DataUploader;
 import org.tom.weather.ws.client.WxWsClient;
 
 import uk.me.jstott.jweatherstation.util.Process;
 import uk.me.jstott.jweatherstation.util.UnsignedByte;
 
 /**
  * 
  * 
  * @author Jonathan Stott
  * @version 1.0
  * @since 1.0
  */
 public class VantagePro extends Station {
   
   private String location;
   
   public VantagePro(String portName, int baudRate) throws PortInUseException,
       NoSuchPortException, IOException {
     super(portName, baudRate);
     setBaudRate(baudRate);
   }
 
   static CommPortIdentifier portId;
   static SerialPort serialPort;
   static OutputStream outputStream;
   private void processDmpAftPacket(byte[] page, int pageOffset) {
     for (int i = pageOffset; i < 5; i++) {
       byte[] rawData = new byte[RECORD_SIZE];
       int byteOffset = RECORD_SIZE * i;
       for (int k = 0, j = byteOffset; j < byteOffset + RECORD_SIZE; j++, k++) {
         // unsignedBytes[k] = new UnsignedByte(page[j]);
         rawData[k] = page[j];
       }
       DmpRecord dmpRecord = new DmpRecord(rawData);
       if (dmpRecord.getDate().after(getLastDate().getTime())) {
       }
       // LOGGER.info("new DmpRecord = " + dmpRecord.toString());
       if (LOGGER.isDebugEnabled()) {
         LOGGER.debug("DMP Record: " + dmpRecord);
       }
       if (dmpRecord.getDate().after(getLastDate().getTime())) {
         dmpRecords.add(dmpRecord);
         setLastDate(getLastDate());
       } else {
         if (LOGGER.isDebugEnabled()) {
           LOGGER.debug(dmpRecord.getDate() + " older than "
               + getLastDate().getTime());
         }
       }
     }
   }
   private void clearDmpRecords() {
     if (LOGGER.isDebugEnabled()) {
       LOGGER.debug("clearing out dmprecords");
     }
     dmpRecords = new ArrayList();
   }
   
   private void wakeup() throws IOException {
     clearInputBuffer();
     for (int i = 1; i <= 3; i++) {
       sendByte((byte) 0x0d);
       delay(1500);
       int bytes = getInputStream().available();
       if (bytes == 2) {
         byte[] crlf = new byte[2];
         int bytesRead = getInputStream().read(crlf);
         if (bytesRead == 2 && crlf[0] == LF && crlf[1] == CR) {
           // LOGGER.debug("Wakeup attempt " + i + " was successful");
           return;
         }
       } else {
         // LOGGER.warn(bytes + " bytes available after wakeup, should be
         // 2");
       }
     }
     LOGGER.error("Station failed to wake up");
   }
   public boolean test() throws IOException {
     sendString("TEST\n");
     delay(500);
     boolean ok = false;
     byte tmp[] = new byte[10];
     int bytesRead = this.getInputStream().read(tmp);
     StringBuffer sb = new StringBuffer();
     for (int i = 0; i < bytesRead; i++) {
       if (tmp[i] != 10 && tmp[i] != 13) {
         sb.append((char) tmp[i]);
       }
     }
     if (LOGGER.isDebugEnabled()) {
       LOGGER.debug("station responded: " + sb.toString());
     }
     int index = sb.indexOf("TEST");
     if (index != -1) {
       ok = true;
     }
     return ok;
   }
   protected void sendLoopCommand(int i) throws IOException {
     sendString("LOOP " + i + "\n");
     getAck();
   }
   protected LoopPacket readLoopData() throws IOException {
     LoopPacket packet = null;
     int bytes = 0;
     byte[] localBuffer = new byte[LOOP_SIZE];
     try {
       sendLoopCommand(1);
       bytes = getInputStream().available();
       if (bytes == LOOP_SIZE) {
         bytes = getInputStream().read(localBuffer);
         // setData(localBuffer);
         packet = new LoopPacket(localBuffer);
         if (LOGGER.isInfoEnabled()) {
           LOGGER.info(packet.shortToString());
         }
         // NamedCache weatherCache = CacheFactory.getCache("ArchiveCache");
         // weatherCache.put(Constants.CURRENT_SNAPSHOT, packet);
       } else {
         LOGGER.warn("unexpected buffer size of: " + bytes+ " - throwing IOException");
         throw new IOException();
       }
     } catch (IOException ex) {
       LOGGER.warn(getPort().getName() + ": Cannot read input stream");
       throw ex;
     }
     return packet;
   }
   /**
    * 
    * 
    * @return
    * @throws IOException
    * @since 1.0
    */
   public boolean dmpaft() throws IOException {
     UnsignedByte[] datetime = null;
     if (Main.DO_SQL) {
       datetime = getSqlLastRecord();
     } else {
       if (getLastDate() == null) {
         datetime = Process.dmpTimeStamp(29, 8, 2005, 1, 20);
         // datetime = Process.dmpTimeStamp(18, 7, 2005, 9, 35);
       } else {
         datetime = Process.dmpTimeStamp(getLastDate()
             .get(Calendar.DAY_OF_MONTH), getLastDate().get(Calendar.MONTH) + 1,
             getLastDate().get(Calendar.YEAR), getLastDate().get(
                 Calendar.HOUR_OF_DAY), getLastDate().get(Calendar.MINUTE));
       }
     }
     // t lastDate
     sendString("DMPAFT\n");
     getAck();
     // Send date/time
     crc.reset();
     LOGGER.debug("Sending date/time "
         + Process.printUnsignedByteArray(datetime));
     sendUnsignedBytes(datetime);
     // Send CRC and get number of pages to be received
     UnsignedByte[] check = crc.getUnsignedBytes();
     LOGGER.debug("sending CRC " + Process.printUnsignedByteArray(check));
     sendUnsignedBytes(check);
     // sendBytes(zeros);
     if (!getAck()) {
       LOGGER.error("Aborting dmpaft");
       return false;
     }
     int bytes = getInputStream().available();
     if (LOGGER.isDebugEnabled()) {
       LOGGER.debug(bytes + " bytes available on stream");
     }
   
     byte[] header = new byte[bytes];
     int bytesRead = getInputStream().read(header);
    if (bytesRead == 0) { // header must be null - give up and try again
      LOGGER.warn("got zero bytes from serial stream when more were expected");
      return false;
    }
 
     if (LOGGER.isDebugEnabled()) {
       LOGGER.debug(bytesRead + " bytes actually read from stream");
     }
   
     UnsignedByte[] data = UnsignedByte.getUnsignedBytes(header);
     if (LOGGER.isDebugEnabled()) {
       LOGGER.debug(data.length + ": size of data array");
       LOGGER.debug("Data: " + Process.printUnsignedByteArray(data));
     }
   
     int pages = (data[1].getByte() << 8) | data[0].getByte();
     int startRecord = (data[3].getByte() << 8) | data[2].getByte();
   
     if (LOGGER.isDebugEnabled()) {
       LOGGER.debug("Expecting " + pages + " pages; first record: " + startRecord);
     }
     
     if (pages > 512) {
       LOGGER.warn("unexpected number of data pages: " + pages);
       return false;
     }
   
     for (int i = 0; i < pages; i++) {
       if (LOGGER.isDebugEnabled()) {
         LOGGER.debug("sending ack");
       }
       sendBytes(new byte[]{(byte)0x06 });
       delay(500);
   
       if (LOGGER.isDebugEnabled()) {
         LOGGER.debug("sent ack");
       }
       
       byte[] myByte = new byte[1];
       getInputStream().read(myByte);
       int sequenceNumber = (int)myByte[0];
       if (LOGGER.isDebugEnabled()) {
         LOGGER.debug("got sequence number: " + sequenceNumber);
         LOGGER.debug("index: " + i + " and sequence number: " + sequenceNumber);
       }
        if (i == 0) {
         readDmpData(startRecord);
       } else {
         readDmpData();
       }
     }
   
     uploadDmpRecords(dmpRecords);
     
     boolean success = true; // postToCache();
     if (success) {
       clearDmpRecords();
     }
     return success;
   }
   private void uploadDmpRecords(List dmpRecords2) {
     
     if (getUploaderList().size() > 0) {
       ArchiveEntry[] entries = new ArchiveEntry[dmpRecords2.size()];
       Iterator iterator = dmpRecords2.iterator();
       int i = 0;
       while (iterator.hasNext()) {
         entries[i++] = (ArchiveEntry)iterator.next();
       }
       for (Iterator iter = getUploaderList().iterator(); iter.hasNext();) {
         DataUploader myUploader = (DataUploader)iter.next();
         myUploader.upload(entries);
       }
     }
   }
   private void readDmpData() {
     readDmpData(0);
   }
   private void readDmpData(int offset) {
   
     if (LOGGER.isDebugEnabled()) {
       LOGGER.debug("reading dmp data at offset: " + offset);
     }
   
     byte[] localBuffer = new byte[BUFFER_SIZE];
     try {
       int available = getInputStream().available();
       if (LOGGER.isDebugEnabled()) {
         LOGGER.debug(available + " bytes available");
       }
       if (available > 0) {
         int bytes = getInputStream().read(localBuffer);
         if (LOGGER.isDebugEnabled()) {
           LOGGER.debug("looking for: " + localBuffer.length + " retrieved: "
               + bytes + " bytes");
         }
         processDmpAftPacket(localBuffer, offset);
       }
       delay(500);
     } catch (IOException ex) {
       LOGGER.error(getPort().getName() + ": Cannot read input stream", ex);
     }
   }
   private UnsignedByte[] getSqlLastRecord() {
     UnsignedByte[] datetime;
     Timestamp dbLastDate = null;
     try {
       dbLastDate = new Timestamp(WxWsClient.getLatestArchiveRecordDate(getLocation()));
     } catch (RemoteException e) {
       LOGGER.warn(e);
       dbLastDate = new Timestamp(new Date().getTime() - 172800); // use two days ago
     }
     getLastDate().setTime(dbLastDate);
     datetime = Process.dmpTimeStamp(// 26, 1, 2004, 15, 0);
         dbLastDate.getDate(), 
         dbLastDate.getMonth() + 1, 
         dbLastDate.getYear() + 1900, 
         dbLastDate.getHours(), 
         dbLastDate.getMinutes());
     return datetime;
   }
   private void setLastDate(Calendar lastDate) {
     this.lastDate = lastDate;
   }
   private Calendar getLastDate() {
     return lastDate;
   }
   protected void sendString(String str) throws IOException {
     wakeup();
     sendBytes(str.getBytes());
   }
   public void setLocation(String location) {
     this.location = location;
   }
   public String getLocation() {
     return location;
   }
 }
