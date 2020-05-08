 package model.mail;
 
 import java.io.File;
 import java.util.HashMap;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 
 import model.parser.mime.MimeHeader;
 
 import org.joda.time.LocalDate;
 
 public class Mail {
 
 	private Map<String, MimeHeader> headers;
 	private List<String> attachmentsExtension;
 	private File contents;
 	private boolean multipartMail;
 
 	public Mail(File contents) {
 		headers = new HashMap<String, MimeHeader>();
 		attachmentsExtension = new LinkedList<String>();
 		setContents(contents);
 	}
 
 	public long getSizeInBytes() {
 		return contents == null ? 0 : contents.length();
 	}
 	
 	public void setContents(File contents) {
 		this.contents = contents;
 	}
 	
 	public void addAttachmentsExtension(String extension) {
 		attachmentsExtension.add(extension);
 	}
 
 	public List<String> getAttachmentsExtension() {
 		return attachmentsExtension;
 	}
 
 	public boolean hasAttachments() {
 		return attachmentsExtension.size() > 0;
 	}
 
 	public boolean hasAttachmentWithExtension(String extension) {
 		return attachmentsExtension.contains(extension);
 	}
 
 	public void addHeaders(MimeHeader header) {
 		headers.put(header.getKey(), header);
 	}
 
 	public MimeHeader getHeader(String name) {
		return headers.get(name);
 	}
 
 	public String getBoundaryKey() {
 		MimeHeader header = headers.get("Content-Type");
 		if(!header.getValue().startsWith("text/plain")){
 		    return header == null ? null : header.getExtraValue("boundary");
 		}
 		return "text/plain"; //indicates that it is a text-plain mail 
 	}
 
 	public LocalDate getDate() {
 		return new LocalDate(headers.get("Delivery-date").getValue());
 	}
 
 	public String getSender() {
 		return headers.get("Return-path").getValue();
 	}
 
 	public boolean containsHeader(String key, String value) {
 		MimeHeader header = headers.get(key);
 		if (header == null) {
 			return false;
 		}
 		return header.getValue().equals(value);
 	}
 
 	public File getContents() {
 		return contents;
 	}
 
 	public boolean isMultipartMail() {
 	    return multipartMail;
 	}
 
 	public void setMultipartMail(boolean multipartMail) {
 	    this.multipartMail = multipartMail;
 	}

	public Map<String, MimeHeader> getHeaders() {
		return headers;
	}
 	
 	
 
 }
