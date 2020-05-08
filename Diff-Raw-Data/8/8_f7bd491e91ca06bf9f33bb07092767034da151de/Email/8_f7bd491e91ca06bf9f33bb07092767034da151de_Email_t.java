 package general;
 
import java.io.ByteArrayInputStream;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.util.Date;
 import java.util.Enumeration;
 import java.util.Properties;
 
 import javax.activation.DataHandler;
 import javax.mail.Address;
 import javax.mail.Flags;
 import javax.mail.Message;
 import javax.mail.MessagingException;
 import javax.mail.Multipart;
 import javax.mail.Session;
 import javax.mail.internet.MimeMessage;
 
 public class Email extends Message implements Comparable<Email>{
 
 	private Message message;
 	private long uid;
 	
 	/**
 	 * Email constructor.
 	 * @param message Email message
 	 */
 	public Email(Message message) {
 		this.message = message;
 	}
 	
 	/**
 	 * Constructs the email given the email content as a string.
 	 * @param emailContent Email content in string format.
 	 */
 	public Email(String emailContent) {
 		try {
 			Session session = Session.getDefaultInstance(new Properties());
			InputStream inputStream = new ByteArrayInputStream(emailContent.getBytes());
 			Message message = new MimeMessage(session, inputStream);
 			this.message = message;
 		} catch (Exception ex) {
			ex.printStackTrace();
 			System.err.println("Cannot load email from string.");
			System.out.println(emailContent);
 		}
 	}
 	
 	public long getUid() {
 		return uid;
 	}
 
 	public void setUid(long uid) {
 		this.uid = uid;
 	}
 	
 	@Override
 	public void addHeader(String arg0, String arg1) throws MessagingException {
 		message.addHeader(arg0, arg1);
 	}
 
 	@Override
 	public Enumeration getAllHeaders() throws MessagingException {
 		return message.getAllHeaders();
 	}
 
 	@Override
 	public Object getContent() throws IOException, MessagingException {
 		return message.getContent();
 	}
 
 	@Override
 	public String getContentType() throws MessagingException {
 		return message.getContentType();
 	}
 
 	@Override
 	public DataHandler getDataHandler() throws MessagingException {
 		return message.getDataHandler();
 	}
 
 	@Override
 	public String getDescription() throws MessagingException {
 		return message.getDescription();
 	}
 
 	@Override
 	public String getDisposition() throws MessagingException {
 		return message.getDisposition();
 	}
 
 	@Override
 	public String getFileName() throws MessagingException {
 		return message.getFileName();
 	}
 
 	@Override
 	public String[] getHeader(String arg0) throws MessagingException {
 		return message.getHeader(arg0);
 	}
 
 	@Override
 	public InputStream getInputStream() throws IOException, MessagingException {
 		return message.getInputStream();
 	}
 
 	@Override
 	public int getLineCount() throws MessagingException {
 		return message.getLineCount();
 	}
 
 	@Override
 	public Enumeration getMatchingHeaders(String[] arg0)
 			throws MessagingException {
 		return message.getMatchingHeaders(arg0);
 	}
 
 	@Override
 	public Enumeration getNonMatchingHeaders(String[] arg0)
 			throws MessagingException {
 		return message.getNonMatchingHeaders(arg0);
 	}
 
 	@Override
 	public int getSize() throws MessagingException {
 		return message.getSize();
 	}
 
 	@Override
 	public boolean isMimeType(String arg0) throws MessagingException {
 		return message.isMimeType(arg0);
 	}
 
 	@Override
 	public void removeHeader(String arg0) throws MessagingException {
 		message.removeHeader(arg0);
 	}
 
 	@Override
 	public void setContent(Multipart arg0) throws MessagingException {
 		message.setContent(arg0);
 	}
 
 	@Override
 	public void setContent(Object arg0, String arg1) throws MessagingException {
 		message.setContent(arg0, arg1);
 	}
 
 	@Override
 	public void setDataHandler(DataHandler arg0) throws MessagingException {
 		message.setDataHandler(arg0);		
 	}
 
 	@Override
 	public void setDescription(String arg0) throws MessagingException {
 		message.setDescription(arg0);
 	}
 
 	@Override
 	public void setDisposition(String arg0) throws MessagingException {
 		message.setDisposition(arg0);		
 	}
 
 	@Override
 	public void setFileName(String arg0) throws MessagingException {
 		message.setFileName(arg0);
 	}
 
 	@Override
 	public void setHeader(String arg0, String arg1) throws MessagingException {
 		message.setHeader(arg0, arg1);
 	}
 
 	@Override
 	public void setText(String arg0) throws MessagingException {
 		message.setText(arg0);
 	}
 
 	@Override
 	public void writeTo(OutputStream arg0) throws IOException,
 			MessagingException {
 		message.writeTo(arg0);
 	}
 
 	@Override
 	public void addFrom(Address[] arg0) throws MessagingException {
 		message.addFrom(arg0);
 	}
 
 	@Override
 	public void addRecipients(RecipientType arg0, Address[] arg1)
 			throws MessagingException {
 		message.addRecipients(arg0, arg1);		
 	}
 
 	@Override
 	public Flags getFlags() throws MessagingException {
 		return message.getFlags();
 	}
 
 	@Override
 	public Address[] getFrom() throws MessagingException {
 		return message.getFrom();
 	}
 
 	@Override
 	public Date getReceivedDate() throws MessagingException {
 		return message.getReceivedDate();
 	}
 
 	@Override
 	public Address[] getRecipients(RecipientType arg0)
 			throws MessagingException {
 		return message.getRecipients(arg0);
 	}
 
 	@Override
 	public Date getSentDate() throws MessagingException {
 		return message.getSentDate();
 	}
 
 	@Override
 	public String getSubject() throws MessagingException {
 		return message.getSubject();
 	}
 
 	@Override
 	public Message reply(boolean arg0) throws MessagingException {
 		return message.reply(arg0);
 	}
 
 	@Override
 	public void saveChanges() throws MessagingException {
 		message.saveChanges();
 	}
 
 	@Override
 	public void setFlags(Flags arg0, boolean arg1) throws MessagingException {
 		message.setFlags(arg0, arg1);		
 	}
 
 	@Override
 	public void setFrom() throws MessagingException {
 		message.setFrom();
 	}
 
 	@Override
 	public void setFrom(Address arg0) throws MessagingException {
 		message.setFrom(arg0);
 	}
 
 	@Override
 	public void setRecipients(RecipientType arg0, Address[] arg1)
 			throws MessagingException {
 		message.setRecipients(arg0, arg1);
 	}
 
 	@Override
 	public void setSentDate(Date arg0) throws MessagingException {
 		message.setSentDate(arg0);
 	}
 
 	@Override
 	public void setSubject(String arg0) throws MessagingException {
 		message.setSubject(arg0);
 	}
 
 	@Override
 	public int compareTo(Email e) {
 		try {
 			return this.getSentDate().compareTo(e.getSentDate());
 		} catch (MessagingException e1) {
 			e1.printStackTrace();
 			return 0;
 		}
 	}
 
 }
