 package ch.ubx.startlist.server;
 
 import java.io.ByteArrayOutputStream;
 import java.io.IOException;
 import java.io.UnsupportedEncodingException;
 import java.net.URLEncoder;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Calendar;
 import java.util.List;
 import java.util.Map;
 import java.util.Properties;
 import java.util.Set;
 import java.util.TreeSet;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import javax.activation.DataHandler;
 import javax.activation.DataSource;
 import javax.mail.Message;
 import javax.mail.MessagingException;
 import javax.mail.Multipart;
 import javax.mail.Part;
 import javax.mail.Session;
 import javax.mail.Transport;
 import javax.mail.internet.AddressException;
 import javax.mail.internet.InternetAddress;
 import javax.mail.internet.MimeBodyPart;
 import javax.mail.internet.MimeMessage;
 import javax.mail.internet.MimeMultipart;
 import javax.mail.util.ByteArrayDataSource;
 
 import ch.ubx.startlist.shared.FlightEntry;
 import ch.ubx.startlist.shared.SendExcel;
 import ch.ubx.startlist.shared.SentFlightEntry;
 import ch.ubx.startlist.shared.TextConstants;
 
 import com.google.appengine.api.utils.SystemProperty;
 
 public class ExcelSender implements TextConstants {
 
 	private static SendExcelDAO sendExcelDAO = new SendExcelDAOobjectify();
 	private static FlightEntryDAO flightEntryDAO = new FlightEntryDAOobjectify();
 	private static SentFlightEntryDAO sentFlightEntryDAO = new SentFlightEntryDAOobjectify();
 
 	private static final Logger log = Logger.getLogger(ExcelSender.class.getName());
 
 	/**
 	 * Send e-mail with an excel sheet for all SendExcel.
 	 * 
 	 * @param currentTime
 	 * 
 	 * @throws IOException
 	 * @throws UnsupportedEncodingException
 	 */
 	public static int doSend(List<String> names, Calendar currentTime) throws IOException {
 		int filteredFlightEntriesCnt = 0;
 		Calendar now = Calendar.getInstance();
 		now.setTimeInMillis(currentTime.getTimeInMillis());
 		Calendar startDate = Calendar.getInstance();
 		String applicationId = SystemProperty.applicationId.get();
 		Map<String, SendExcel> sendExcelMap = sendExcelDAO.listSendExcel(names);
 
 		for (SendExcel sendExcel : sendExcelMap.values()) {
 			// TODO - use EntrySet to get job name
 			String sheetname = String.format("%1$tY%1$tm%1$te", now) + "-" + sendExcel.getPlace().replace(" ", "_");
 			List<FlightEntry> flightEntries;
 			List<SentFlightEntry> sentFlightEntries = null;
 
 			// Handle already sent FlightEntries TODO - better comment!
 			if (sendExcel.getDaysBehind() > 0) {
 				startDate.setTimeInMillis(now.getTimeInMillis());
 				startDate.add(Calendar.DAY_OF_YEAR, sendExcel.getDaysBehind() * -1);
 				flightEntries = flightEntryDAO.listflightEntry(startDate, now, sendExcel.getPlace());
 				sentFlightEntries = sentFlightEntryDAO.listFlightEntry(sendExcel.getName());
 
 				for (SentFlightEntry sentFlightEntry : sentFlightEntries) {
 					for (FlightEntry flightEntry : flightEntries) {
 						// Compare Id values, not objects reference!
 						if (sentFlightEntry.getFlightEntry().compareTo(flightEntry.getId()) == 0) {
 							if (sentFlightEntry.getLastModified() == flightEntry.getModified() || flightEntry.getModified() < startDate.getTimeInMillis()) {
 								flightEntries.remove(flightEntry);
 								break;
 							}
 						}
 					}
 				}
 			} else {
 				flightEntries = flightEntryDAO.listflightEntry(now, sendExcel.getPlace());
 			}
 
 			List<FlightEntry> filteredFlightEntries = new ArrayList<FlightEntry>();
 			if (!flightEntries.isEmpty()) {
 				// Filter Gliders and Towplanes
 				Set<String> filterGliders = new TreeSet<String>();
 				if (sendExcel.getFilterGliders() != null) {
 					filterGliders.addAll(Arrays.asList(sendExcel.getFilterGliders().split(";")));
 				}
 				Set<String> filterTowplanes = new TreeSet<String>();
 				if (sendExcel.getFilterTowplanes() != null) {
 					filterTowplanes.addAll(Arrays.asList(sendExcel.getFilterTowplanes().split(";")));
 				}
 				for (FlightEntry flightEntry : flightEntries) {
 					if (filterGliders.isEmpty() & filterTowplanes.isEmpty()) {
 						filteredFlightEntries.add(flightEntry);
 					} else if (flightEntry.getRegistrationGlider() != null && filterGliders.contains(flightEntry.getRegistrationGlider())
 							|| (flightEntry.getRegistrationTowplane() != null && filterTowplanes.contains(flightEntry.getRegistrationTowplane()))) {
 						filteredFlightEntries.add(flightEntry);
 					}
 				}
 			}
 
 			Properties props = new Properties();
 			Session session = Session.getDefaultInstance(props, null);
 			try {
 				Message msg = new MimeMessage(session);
 				msg.setFrom(new InternetAddress("admin@" + applicationId + ".appspotmail.com"));
 				for (String recipient : sendExcel.getRecipientsList()) {
 					msg.addRecipient(Message.RecipientType.TO, new InternetAddress(recipient));
				}
				msg.setSubject(sendExcel.getSubject());

 				Multipart mp = new MimeMultipart();
 				log.log(Level.INFO, "Create excel for FlighEnties: " + filteredFlightEntries.size());
 				// Excel attachment
 				if (!filteredFlightEntries.isEmpty()) {
 					ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
 					ExcelSheet.createExcel(filteredFlightEntries, outputStream, sheetname);
 					MimeBodyPart attachmentBP = new MimeBodyPart();
 					attachmentBP.setFileName(URLEncoder.encode(sheetname + ".xls", "UTF-8"));
 					attachmentBP.setDisposition(Part.ATTACHMENT);
 					DataSource src = new ByteArrayDataSource(outputStream.toByteArray(), "application/x-ms-excel");
 					DataHandler handler = new DataHandler(src);
 					attachmentBP.setDataHandler(handler);
 					mp.addBodyPart(attachmentBP);
 				}
 
 				// Message body
 				MimeBodyPart plainBody = new MimeBodyPart();
 				plainBody.setContent(filteredFlightEntries.isEmpty() ? TXT_NO_FLIGHT_TODAY : TXT_FLIGHT_FOR_TODY_IN_EXCEL_FILE, "text/plain");
 				plainBody.setFileName("body.txt");
 				mp.addBodyPart(plainBody);
 				msg.setText(sendExcel.getSubject());
 				msg.setContent(mp);
 				msg.saveChanges();
 				Transport.send(msg);
 
 				// Update SentFlightEntry
 				if (sendExcel.getDaysBehind() > 0) {
 					for (FlightEntry flightEntry : filteredFlightEntries) {
 						SentFlightEntry sentFlightEntry = sentFlightEntryDAO.getSentFlightEntry(sendExcel.getName(), flightEntry.getId());
 						if (sentFlightEntry == null) {
 							sentFlightEntry = new SentFlightEntry(flightEntry.getId(), sendExcel.getName(), flightEntry.getModified());
 						} else {
 							sentFlightEntry.setLastModified(flightEntry.getModified());
 						}
 						sentFlightEntryDAO.createOrUpdateSentFlightEntry(sentFlightEntry);
 					}
 					sentFlightEntryDAO.purgeSentFlightEntry(sendExcel.getName(), startDate);
 				}
 			} catch (AddressException e) {
 				// ...
 			} catch (MessagingException e) {
 				// ...
 			}
 			filteredFlightEntriesCnt = filteredFlightEntriesCnt + filteredFlightEntries.size();
 		}
 		return filteredFlightEntriesCnt; // for unit test only!
 	}
 
 }
