 package org.pharmgkb;
 
 import org.apache.commons.io.IOUtils;
 import org.apache.log4j.Logger;
 import org.apache.poi.ss.usermodel.Row;
 import org.apache.poi.ss.usermodel.Sheet;
 import org.apache.poi.ss.usermodel.Workbook;
 import org.apache.poi.ss.usermodel.WorkbookFactory;
 import org.hibernate.Session;
 import org.pharmgkb.util.HibernateUtils;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.io.InputStream;
 
 /**
  * Created by IntelliJ IDEA.
  * User: whaleyr
  * Date: 8/28/12
  */
 public class ExcelParser {
   private static final Logger sf_logger = Logger.getLogger(ExcelParser.class);
   private static final String sf_dataSheetName = "Subject_level_Data";
   private File m_file = null;
   private Workbook m_workbook = null;
   private Sheet m_dataSheet = null;
 
   public ExcelParser(File file) throws Exception {
     if (file == null || !file.exists()) {
       throw new Exception("No file specified");
     }
     setFile(file);
 
     InputStream inputStream = null;
     try {
       inputStream = new FileInputStream(file);
       setWorkbook(WorkbookFactory.create(inputStream));
     }
     finally {
       IOUtils.closeQuietly(inputStream);
     }
 
     setDataSheet(getWorkbook().getSheet(sf_dataSheetName));
     if (getDataSheet() == null) {
       throw new Exception("Required worksheet "+sf_dataSheetName+" not found");
     }
 
     Row headerRow = getDataSheet().getRow(1);
   }
 
   public void parse(File outputFile) throws Exception {
     sf_logger.info("Parsing excel workbook "+getFile());
     sf_logger.info("writing output to "+outputFile);
 
     SubjectIterator subjectIterator = new SubjectIterator(getDataSheet());
     Session session = null;
     FileOutputStream fos = null;
 
     try {
       session = HibernateUtils.getSession();
       subjectIterator.parseHeading(session);
 
       while (subjectIterator.hasNext()) {
 
         Subject subject = subjectIterator.next();
 
         if (sf_logger.isDebugEnabled()) {
           sf_logger.debug("Loaded subject: "+subject.getSubjectId());
         }
 
         Subject existingSubject = (Subject)session.get(Subject.class, subject.getSubjectId());
         if (existingSubject != null) {
           session.delete(existingSubject);
         }
 
         session.save(subject);
       }
       HibernateUtils.commit(session);
 
       fos = new FileOutputStream(outputFile);
       getWorkbook().write(fos);
     }
     catch (Exception ex) {
      sf_logger.error("Error saving subjects", ex);
     }
     finally {
       IOUtils.closeQuietly(fos);
       HibernateUtils.close(session);
     }
   }
 
   public File getFile() {
     return m_file;
   }
 
   public void setFile(File file) {
     m_file = file;
   }
 
   public Workbook getWorkbook() {
     return m_workbook;
   }
 
   public void setWorkbook(Workbook workbook) {
     m_workbook = workbook;
   }
 
   public Sheet getDataSheet() {
     return m_dataSheet;
   }
 
   public void setDataSheet(Sheet dataSheet) {
     m_dataSheet = dataSheet;
   }
 }
