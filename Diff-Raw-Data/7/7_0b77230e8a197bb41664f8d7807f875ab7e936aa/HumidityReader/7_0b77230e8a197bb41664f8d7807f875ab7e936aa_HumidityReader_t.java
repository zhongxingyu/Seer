 package net.morrdusk.collector.onewire.readers;
 
 import com.google.common.io.Files;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import java.io.File;
 import java.io.IOException;
 import java.math.BigDecimal;
 
 public class HumidityReader extends Reader {
     private static final Logger LOG = LoggerFactory.getLogger(HumidityReader.class);
     private static final int RETRIES = 3;
     private static final int MAX_VALID_VALUE = 100;
 
     public HumidityReader() {
     }
     
     public BigDecimal readValue(File deviceDirectory) throws ParseException {
         try {
             return readFile(new File(deviceDirectory, "humidity"));
         } catch (IOException e) {
             throw new ParseException("Failed to read humidity value", e);
         }
     }
 
     private BigDecimal readFile(File file) throws IOException, ParseException {
         int retries = RETRIES;
 
         while (retries > 0) {
             final BigDecimal value = parseValueString(Files.toString(file, charset));
             if (value.compareTo(new BigDecimal(MAX_VALID_VALUE)) > 0) {
                 LOG.info("Humidity sensor {} reported error, re-reading, tries left={}",
                         file.getParent(), retries-1);
                 retries--;
             }
             else {
                 return value;
             }
         }
 
         throw new ParseException("Humidity sensor " + file.getParent() + " only return error values");
     }
 
     protected BigDecimal parseValueString(String str) {
         return new BigDecimal(str.trim());
     }
 }
