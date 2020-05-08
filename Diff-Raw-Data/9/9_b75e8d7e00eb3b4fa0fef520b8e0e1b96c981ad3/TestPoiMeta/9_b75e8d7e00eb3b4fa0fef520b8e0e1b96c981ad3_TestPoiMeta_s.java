 package de.jpaw.bonaparte.poi.tests;
 
 import java.io.IOException;
 
 import org.joda.time.LocalDateTime;
 
 import de.jpaw.bonaparte.poi.ExcelComposer;
 import de.jpaw.bonaparte.pojos.meta.ClassDefinition;
 
 public class TestPoiMeta {
 
     public static void main(String[] args) throws IOException {
         ClassDefinition cd = new ClassDefinition(true, false, "myName", "Rev", null, null,  87264821612983L, 42,
                null, null, true, LocalDateTime.now());
 
         ExcelComposer ec = new ExcelComposer();
         ec.newSheet("Tabelle Nummer 1");
         ec.writeRecord(cd);
         cd.setBundle("I'm a bundle now");
         ec.writeRecord(cd);
         ec.closeSheet();
 
         ec.startTransmission();
         ec.writeRecord(cd.get$MetaData());
         ec.closeSheet();
 
         ec.writeToFile("test.xls");
     }
 
 }
