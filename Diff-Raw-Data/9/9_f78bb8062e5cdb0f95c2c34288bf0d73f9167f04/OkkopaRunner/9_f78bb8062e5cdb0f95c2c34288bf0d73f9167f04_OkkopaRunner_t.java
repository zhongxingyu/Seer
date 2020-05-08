 package fi.helsinki.cs.okkopa;
 
 import com.unboundid.ldap.sdk.LDAPException;
 import fi.helsinki.cs.okkopa.database.OkkopaDatabase;
 import fi.helsinki.cs.okkopa.mail.read.EmailRead;
 import fi.helsinki.cs.okkopa.mail.send.ExamPaperSender;
 import fi.helsinki.cs.okkopa.exception.DocumentException;
 import fi.helsinki.cs.okkopa.exception.NotFoundException;
 import fi.helsinki.cs.okkopa.model.ExamPaper;
 import fi.helsinki.cs.okkopa.ldap.LdapConnector;
 import fi.helsinki.cs.okkopa.mail.writeToDisk.Saver;
 import fi.helsinki.cs.okkopa.model.Student;
 import fi.helsinki.cs.okkopa.pdfprocessor.PDFProcessor;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.io.InputStream;
 import java.security.GeneralSecurityException;
 import java.nio.file.FileAlreadyExistsException;
 import java.sql.SQLException;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.logging.Level;
 import org.apache.log4j.Logger;
 import javax.mail.MessagingException;
 import org.apache.commons.io.IOUtils;
 import org.jpedal.exception.PdfException;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Component;
 
 @Component
 public class OkkopaRunner implements Runnable {
 
     private PDFProcessor pDFProcessor;
     private EmailRead server;
     private ExamPaperSender sender;
     private static Logger LOGGER = Logger.getLogger(OkkopaRunner.class.getName());
     private OkkopaDatabase okkopaDatabase;
     private LdapConnector ldapConnector;
     private boolean saveToTikli;
     private boolean saveOnExamPaperPDFError;
     private boolean logCompleteExceptionStack;
     private final String saveErrorFolder;
     private Saver saver;
     private final String saveRetryFolder;
     // Used with email retry
     private boolean retrying;
     private boolean sent;
     private int retryExpirationMinutes;
 
     @Autowired
     public OkkopaRunner(EmailRead server, ExamPaperSender sender,
             PDFProcessor pDFProcessor, Settings settings,
             OkkopaDatabase okkopaDatabase, LdapConnector ldapConnector, Saver saver) {
         this.server = server;
         this.sender = sender;
         this.pDFProcessor = pDFProcessor;
         this.okkopaDatabase = okkopaDatabase;
         this.ldapConnector = ldapConnector;
         this.saver = saver;
         saveErrorFolder = settings.getSettings().getProperty("exampaper.savefolder");
         saveRetryFolder = settings.getSettings().getProperty("mail.send.retrysavefolder");
         saveToTikli = Boolean.parseBoolean(settings.getSettings().getProperty("tikli.enable"));
         saveOnExamPaperPDFError = Boolean.parseBoolean(settings.getSettings().getProperty("exampaper.saveunreadablefolder"));
         logCompleteExceptionStack = Boolean.parseBoolean(settings.getSettings().getProperty("logger.logcompletestack"));
         retryExpirationMinutes = Integer.parseInt(settings.getSettings().getProperty("mail.send.retryexpirationminutes"));
         retrying = false;
         sent = true;
     }
 
     @Override
     public void run() {
         retrying = true;
         retryFailedEmails();
         retrying = false;
         try {
             server.connect();
             LOGGER.debug("Kirjauduttu sisään.");
             while (true) {
                 ArrayList<InputStream> attachments = server.getNextMessagesAttachments();
                 LOGGER.debug("Vanhimman viestin liitteet haettu");
                 if (attachments == null) {
                     LOGGER.debug("Ei uusia viestejä.");
                     break;
                 }
                 for (InputStream attachment : attachments) {
                     LOGGER.debug("Käsitellään liitettä.");
                     processAttachment(attachment);
                     IOUtils.closeQuietly(attachment);
                 }
             }
         } catch (MessagingException | IOException ex) {
             logException(ex);
         } finally {
             server.close();
         }
     }
 
     private void processAttachment(InputStream inputStream) {
         List<ExamPaper> examPapers;
         // Split PDF to ExamPapers (2 pages per each).
         try {
             examPapers = pDFProcessor.splitPDF(inputStream);
             IOUtils.closeQuietly(inputStream);
         } catch (DocumentException ex) {
             // Errors: bad PDF-file, odd number of pages.
             logException(ex);
             // nothing to process, return
             return;
         }
         ExamPaper courseInfoPage = examPapers.get(0);
         String courseInfo = null;
         try {
             courseInfo = getCourseInfo(courseInfoPage);
             // Remove if succesful so that the page won't be processed as
             // a normal exam paper.
             examPapers.remove(courseInfoPage);
         } catch (NotFoundException ex) {
             logException(ex);
         }
 
         // Process all examPapers
         while (!examPapers.isEmpty()) {
             ExamPaper examPaper = examPapers.remove(0);
             processExamPaper(examPaper, courseInfo);
         }
     }
 
     private void processExamPaper(ExamPaper examPaper, String courseInfo) {
         try {
             examPaper.setPageImages(pDFProcessor.getPageImages(examPaper));
             examPaper.setQRCodeString(pDFProcessor.readQRCode(examPaper));
         } catch (PdfException | NotFoundException ex) {
             logException(ex);
             if (saveOnExamPaperPDFError) {
                 try {
                     saver.saveInputStream(examPaper.getPdf(), saveErrorFolder, "" + System.currentTimeMillis() + ".pdf");
                 } catch (FileAlreadyExistsException ex1) {
                     java.util.logging.Logger.getLogger(OkkopaRunner.class.getName()).log(Level.SEVERE, "File already exists", ex1);
                 }
             }
             return;
         }
 
         String currentUserId;
         try {
             currentUserId = fetchUserId(examPaper.getQRCodeString());
         } catch (SQLException | NotFoundException ex) {
             logException(ex);
             // QR code isn't an user id and doesn't match any database entries.
             return;
         }
 
         // Get email and student number from LDAP:
         try {
             examPaper.setStudent(ldapConnector.fetchStudent(currentUserId));
         } catch (NotFoundException | LDAPException | GeneralSecurityException ex) {
             logException(ex);
         }
 
         // TODO remove when ldap has been implemented.
         examPaper.setStudent(new Student(currentUserId, "okkopa.2013@gmail.com", "dummystudentnumber"));
 
         sendEmail(examPaper);
         LOGGER.debug("Koepaperi lähetetty sähköpostilla.");
         if (courseInfo != null && saveToTikli) {
             saveToTikli(examPaper);
             LOGGER.debug("Koepaperi tallennettu Tikliin.");
         }
 
     }
 
     public String getCourseInfo(ExamPaper examPaper) throws NotFoundException {
         // TODO unimplemented
         throw new NotFoundException("Course ID not found.");
     }
 
     private void saveToTikli(ExamPaper examPaper) {
         LOGGER.info("Tässä vaiheessa tallennettaisiin paperit Tikliin");
     }
 
     private void sendEmail(ExamPaper examPaper) {
         try {
             sender.send(examPaper);
             sent = true;
         } catch (MessagingException ex) {
             if (!retrying) {
                LOGGER.debug("Sähköpostin lähetys epäonnistui. Tallennetaan PDF-liite levylle.");
                 try {
                     saver.saveInputStream(examPaper.getPdf(), saveRetryFolder, "" + System.currentTimeMillis() + ".pdf");
                 } catch (FileAlreadyExistsException ex1) {
                     logException(ex1);
                 }
             }
             logException(ex);
             sent = false;
         }
     }
 
     private void logException(Exception ex) {
         //Currently just logging exceptions. Should exception handling be in its own class?
         if (logCompleteExceptionStack) {
             LOGGER.error(ex.toString(), ex);
         } else {
             LOGGER.error(ex.toString());
         }
     }
 
     private String fetchUserId(String qrcode) throws SQLException, NotFoundException {
         if (Character.isDigit(qrcode.charAt(0))) {
             return okkopaDatabase.getUserID(qrcode);
         }
         return qrcode;
     }
 
     private void retryFailedEmails() {
         // Get failed email send attachments (PDF-files)
        LOGGER.debug("Yritetään lähettää sähköposteja uudelleen.");
         ArrayList<File> fileList = saver.list(saveRetryFolder);
        if (fileList == null) {
            LOGGER.debug("Ei uudelleenlähetettävää.");
            return;
        };
        LOGGER.debug(fileList.size() + " uudelleenlähetettävää säilössä.");
         for (File pdf : fileList) {
             FileInputStream fis;
             try {
                 fis = new FileInputStream(pdf);
             } catch (FileNotFoundException ex) {
                 logException(ex);
                 continue;
             }
             // Send each single PDF through the whole process.
             processAttachment(fis);
             IOUtils.closeQuietly(fis);
 
             long fileAge = (System.currentTimeMillis() - pdf.lastModified()) / 60000;
             if (sent || fileAge > retryExpirationMinutes) {
                 pdf.delete();
             }
         }
     }
 }
