 package com.osdiab.patient_organizer;
 
 import org.apache.poi.hwpf.HWPFDocument;
 import org.apache.poi.hwpf.extractor.WordExtractor;
 import org.apache.poi.hwpf.usermodel.Paragraph;
 import org.apache.poi.hwpf.usermodel.Range;
 import org.apache.poi.hwpf.usermodel.Section;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.util.Date;
 import java.util.regex.Matcher;
 
 /**
  * Created by osdiab on 7/29/14.
  * Wrapper on HWPFDocument that replaces appointment dates for .doc files.
  */
 public class WordDocPatientDocument extends PatientDocument {
     private final HWPFDocument doc;
 
     public WordDocPatientDocument(HWPFDocument doc) {
         super();
         this.doc = doc;
     }
 
     @Override
     public void replaceAppointmentDate(Date date) throws DateNotFoundException {
         Range docRange = doc.getRange();
         for (int i = 0; i < docRange.numSections(); ++i ) {
             Section s = docRange.getSection(i);
             for (int j = 0; j < s.numParagraphs(); j++) {
                 Paragraph p = s.getParagraph(j);
                 Matcher m = dateRegex.matcher(p.text());
 
                 if (m.find()) {
                     String formattedDate = DiabPatientOrganizer.DATE_FORMAT.format(date);
                    String toReplace = m.group(1);
                    if (formattedDate.equals(toReplace)) {
                        return;
                    }
                    p.replaceText(toReplace, formattedDate);
                    Debugger.log("\t\t\t\t\t\t\t\tREPLACED!");
 
                     return; // first match is appointment date
                 }
             }
         }
 
         throw new DateNotFoundException();
     }
 
     public static WordDocPatientDocument create(File file) throws IOException {
         return new WordDocPatientDocument(new HWPFDocument(new FileInputStream(file)));
     }
 
     @Override
     public void save(File dest) throws IOException {
         doc.write(new FileOutputStream(dest));
     }
 
     public String toString() {
         return new WordExtractor(doc).getText();
     }
 }
