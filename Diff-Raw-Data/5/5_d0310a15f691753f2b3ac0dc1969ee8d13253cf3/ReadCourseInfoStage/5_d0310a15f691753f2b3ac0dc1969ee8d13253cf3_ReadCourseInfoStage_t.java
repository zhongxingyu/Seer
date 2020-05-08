 package fi.helsinki.cs.okkopa.main.stage;
 
 import fi.helsinki.cs.okkopa.database.BatchDetailDAO;
 import fi.helsinki.cs.okkopa.shared.exception.NotFoundException;
 import fi.helsinki.cs.okkopa.mail.send.EmailSender;
 import fi.helsinki.cs.okkopa.main.BatchDetails;
 import fi.helsinki.cs.okkopa.main.ExceptionLogger;
 import fi.helsinki.cs.okkopa.shared.Settings;
 import fi.helsinki.cs.okkopa.model.BatchDbModel;
 import fi.helsinki.cs.okkopa.model.ExamPaper;
 import fi.helsinki.cs.okkopa.pdfprocessor.PDFProcessor;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.sql.SQLException;
 import java.util.List;
 import javax.mail.MessagingException;
 import org.apache.log4j.Logger;
 import org.jpedal.exception.PdfException;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Component;
 
 /**
  *
  * @author hannahir
  */
 @Component
 public class ReadCourseInfoStage extends Stage<List<ExamPaper>, ExamPaper> {
 
     private static final Logger LOGGER = Logger.getLogger(ReadCourseInfoStage.class.getName());
     private ExceptionLogger exceptionLogger;
     private PDFProcessor pdfProcessor;
     private BatchDetails batch;
     private EmailSender emailSender;
     private Settings settings;
     private BatchDetailDAO batchDao;
     private final static String NOBATCHSTRING = "NA";
 
     /**
      *
      * @param pDFProcessor
      * @param exceptionLogger
      * @param batch
      * @param settings
      * @param emailSender
      * @param batchDao
      */
     @Autowired
     public ReadCourseInfoStage(PDFProcessor pDFProcessor, ExceptionLogger exceptionLogger, BatchDetails batch, Settings settings, EmailSender emailSender, BatchDetailDAO batchDao) {
         this.pdfProcessor = pDFProcessor;
         this.exceptionLogger = exceptionLogger;
         this.batch = batch;
         this.settings = settings;
         this.emailSender = emailSender;
         this.batchDao = batchDao;
     }
 
     @Override
     public void process(List<ExamPaper> examPapers) {
         ExamPaper courseInfoPage = examPapers.get(0);
         batch.reset();
 
         parseFrontPage(courseInfoPage, examPapers);
 
         processAllExamPapers(examPapers);
 
         if (batch.getReportEmailAddress() != null && !batch.getReportEmailAddress().equals("")) {
             sendEmail();
         }
     }
 
     /**
      *
      * @param examPaper
      * @throws SQLException
      * @throws FileNotFoundException
      * @throws IOException
      * @throws NotFoundException
      */
     public void setBatchDetails(ExamPaper examPaper) throws SQLException, FileNotFoundException, IOException, NotFoundException {
         String[] fields = examPaper.getQRCodeString().split(":");
 
         if (fields.length > 6 || fields.length < 5) {
             throw new NotFoundException("Wrong number of frontpage parameters. Expected 5-6, but was " + fields.length + ".");
         }
         LOGGER.debug("Kurssi-info luettu: " + examPaper.getQRCodeString());
 
         setCourceFields(fields);
 
         if (fields.length == 6 && !fields[5].toUpperCase().equals(NOBATCHSTRING)) {
             getAndSetInfoAndEmail(fields);
         }
     }
 
     private void sendEmail() {
         try {
             LOGGER.debug("Lähetetään raporttisähköposti.");
             sendReportEmail();
             
         } catch (MessagingException ex) {
             LOGGER.debug("Raporttisähköpostin lähetys epäonnistui.");
             exceptionLogger.logException(ex);
         }
     }
 
     private void setCourceFields(String[] fields) throws NotFoundException {
         try {
             batch.setCourseCode(fields[0]);
             batch.setPeriod(fields[1]);
             batch.setYear(Integer.parseInt(fields[2]));
             batch.setType(fields[3]);
             batch.setCourseNumber(Integer.parseInt(fields[4]));
         } catch (Exception e) {
             throw new NotFoundException("Invalid parameter found in frontpage.");
         }
     }
 
     private void getAndSetInfoAndEmail(String[] fields) throws NotFoundException, SQLException {
         BatchDbModel bdm = batchDao.getBatchDetails(fields[5]);
        if (bdm.getEmailContent() != null && !bdm.getEmailContent().equals("")) {
             batch.setEmailContent(bdm.getEmailContent());
         }
        if (bdm.getReportEmailAddress() != null && !bdm.getReportEmailAddress().equals("")) {
             batch.setReportEmailAddress(bdm.getReportEmailAddress());
         }
     }
 
     private void parseFrontPage(ExamPaper courseInfoPage, List<ExamPaper> examPapers) {
         try {
             courseInfoPage.setPageImages(pdfProcessor.getPageImages(courseInfoPage));
             courseInfoPage.setQRCodeString(pdfProcessor.readQRCode(courseInfoPage));
             setBatchDetails(courseInfoPage);
 
             // Remove if succesful so that the page won't be processed as
             // a normal exam paper.
             examPapers.remove(0);
             LOGGER.debug("Kurssi-info luettu onnistuneesti.");
         } catch (PdfException | NotFoundException | SQLException | IOException ex) {
             exceptionLogger.logException(ex);
         }
     }
 
     private void processAllExamPapers(List<ExamPaper> examPapers) {
         // Process all examPapers
         while (!examPapers.isEmpty()) {
             ExamPaper examPaper = examPapers.remove(0);
             // Add course info (doesn't matter if null)
             processNextStages(examPaper);
         }
     }
 
     private void sendReportEmail() throws MessagingException {
         emailSender.send(batch.getReportEmailAddress(),
                 settings.getProperty("mail.message.defaulttopic.report"),
                 "katenoi sisältö kasaan\nSivuja ytheensä: "
                 + batch.getTotalPages() + "\nQR-koodin luku epäonnistui: "
                 + batch.getFailedScans(), null, null);
     }
 }
