 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.io.UnsupportedEncodingException;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.Properties;
 
 import javax.mail.Message;
 import javax.mail.Message.RecipientType;
 import javax.mail.MessagingException;
 import javax.mail.NoSuchProviderException;
 import javax.mail.internet.AddressException;
 import javax.mail.internet.InternetAddress;
 import javax.mail.internet.MimeBodyPart;
 import javax.mail.internet.MimeMessage;
 import javax.mail.internet.MimeMultipart;
 
 import org.apache.poi.ss.usermodel.Row;
 import org.apache.poi.xssf.usermodel.XSSFSheet;
 import org.apache.poi.xssf.usermodel.XSSFWorkbook;
 
 import com.sun.mail.smtp.SMTPTransport;
 
 public class ArchivePO {
 	public static int toArchiveCount;
 	public static int archivedCount;
 	
 	public static void selectAttachment(File outputToArchive){
 		toArchiveCount = 0;
 		archivedCount = 0;
 		String outputToArchiveL = null;
 		FileInputStream readStr = null; //POI does not support buffered stream
 		XSSFWorkbook wb = null;
 		XSSFSheet BRSheet = null;
 		String subjectLine = null;
 		String PoNumber;
 		String[] PoFiles = new String[100];
 		String toAttach = null;
 		boolean PoIsInfolder;
 		ArrayList<String> noPoFile = new ArrayList<String>();
 		
 		outputToArchiveL = outputToArchive.getAbsolutePath();
 		PoFiles = OEMultiT.inputPath.list();
 		
 		try {
 			readStr = new FileInputStream (outputToArchiveL);
 			wb = new XSSFWorkbook(readStr);
 			BRSheet = wb.getSheetAt(0);
 			
 			for (Row row : BRSheet) {
 				PoIsInfolder = false;
 				PoNumber = null;
 				try {
 						subjectLine = "RES: Order, " + row.getCell(7).getStringCellValue();
 						PoNumber = row.getCell(2).getStringCellValue();
 						toArchiveCount++;
 						for (String fileName : PoFiles) {
 							if (fileName.contains(PoNumber)) {
 								toAttach = fileName;
 								PoIsInfolder = true;
 							}
 						}
 					
 				} catch (NullPointerException e) {
 					//do nothing
 				}
 				if (PoIsInfolder) {
 					sendMail(subjectLine, toAttach);
 				} else {
 					if (!(PoNumber == null)) {
 						noPoFile.add(PoNumber);
 					}
 				}	
 			}
 			OEMultiT.archivedMessage("Total to Archive: " + toArchiveCount + ", Total Archived: " + archivedCount);
 			if (!noPoFile.isEmpty()) {
 				OEMultiT.archivedMessage("The following PO's were not in the moinho folder and were no archived: " + noPoFile);
 			}
 
 		} catch (FileNotFoundException e) {
 			e.printStackTrace();
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 		
 	}
 	
 	public static void sendMail(String subjectLine, String toAttach) {
 		File attch;
 		Properties prop;
 		String mailhost = "mail.apple.com";
 		String mailer = "smtpsend";
 		
 		attch = new File (OEMultiT.inputPath.getAbsolutePath() + "/" + toAttach);
 		
 		//set properties
 		prop = System.getProperties();
 		prop.put("mail.smtps.host", mailhost);
 		prop.put("mail.smtps.ssl.enable", "true");
 		prop.put("mail.smtps.auth", "true");
 		
 		//instaciate a session
 		javax.mail.Session session = javax.mail.Session.getInstance(prop, null);
 		
 		//create the email message
 		Message email = new MimeMessage(session);
 		try {
 			email.setFrom(new InternetAddress("gpinheiro@apple.com", "Gustavo Pinheiro"));
 			email.setRecipient(RecipientType.TO, new InternetAddress("ARCHIVE@kofax.corp.apple.com", "KOFAX"));
 			email.setRecipient(RecipientType.CC, new InternetAddress("gpinheiro@apple.com", "Gustavo Pinheiro"));
 			email.setSubject(subjectLine);
 		} catch (AddressException e) {
 			e.printStackTrace();
 		} catch (MessagingException e) {
 			e.printStackTrace();
 		} catch (UnsupportedEncodingException e) {
 			e.printStackTrace();
 		} catch (NoClassDefFoundError e) {
 			e.printStackTrace();
 			OEMultiT.archivedMessage( e.getMessage());
 		}
 		MimeBodyPart mbp1 = new MimeBodyPart();
 		try {
 			mbp1.setText("__");
 			MimeBodyPart mbp2 = new MimeBodyPart();
 			mbp2.attachFile(attch);
 			MimeMultipart mp = new MimeMultipart();
 			mp.addBodyPart(mbp1);
 			mp.addBodyPart(mbp2);
 			email.setContent(mp);
 			email.setHeader("X-Mailer", mailer);
 		    email.setSentDate(new Date());
 		} catch (MessagingException e) {
 			e.printStackTrace();
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 		
 		SMTPTransport t = null;
 		try {
 			t = (SMTPTransport)session.getTransport("smtps");
 		} catch (NoSuchProviderException e) {
 			e.printStackTrace();
 		}
 		try {
 			t.connect(mailhost, "gpinheiro", "donner25");
 			t.sendMessage(email, email.getAllRecipients());
 		} catch (MessagingException e) {
 			e.printStackTrace();
			OEMultiT.archivedMessage("Error archiving: " + e.getMessage());
		} 
		finally {
		
 				try {
 					System.out.println("Response for " + subjectLine + ": " + t.getLastServerResponse());
 					OEMultiT.archivedMessage("Response: " + subjectLine + ": " + t.getLastServerResponse());
 					if (t.getLastServerResponse().contains("Ok")) {
 						archivedCount++ ;
 					}
 					t.close();
 				} catch (MessagingException e) {
 					e.printStackTrace();
				} catch (NullPointerException e) {
					e.printStackTrace();
					OEMultiT.archivedMessage("Error archiving: no response from server");
 				}
 	    }
 	}
 }
