 package com.ee.excellentpdf.email;
 
 import junit.framework.Assert;
 import org.junit.Test;
 
 import java.io.File;
 import java.net.URI;
 import java.net.URISyntaxException;
 import java.net.URL;
 
import static junit.framework.Assert.assertEquals;

 public class GmailEmailServiceTest {
 
    /* File getSalarySlip() throws URISyntaxException {
         URL url = getClass().getClassLoader().getResource("salarySlip.pdf");
         URI uri = url.toURI();
         return new File(uri);
     }*/
 
     @Test
     public void itShouldSendEmailWithAttachment() throws URISyntaxException {
         EmailService service = new GmailEmailService("excellentpdf@gmail.com", "pdfuser@123");
         File salarySlip = new File("src/test/resources/salarySlip.pdf");
        boolean sent = service.sendMail(salarySlip, "rraut@equalexperts.com", "Salary Slip", "\n Hello renuka!!\n");

        assertEquals(sent,true);
 
 
     }
 }
