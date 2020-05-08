 package com.brif.nix.parser;
 
 import java.io.BufferedOutputStream;
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.UnsupportedEncodingException;
 import java.util.Arrays;
 import java.util.Comparator;
 import java.util.Date;
 import java.util.HashSet;
 import java.util.Set;
 
 import javax.mail.Address;
 import javax.mail.BodyPart;
 import javax.mail.Flags;
 import javax.mail.Message;
 import javax.mail.MessagingException;
 import javax.mail.Part;
 import javax.mail.internet.InternetAddress;
 import javax.mail.internet.MimeBodyPart;
 import javax.mail.internet.MimeMultipart;
 import javax.mail.internet.MimeUtility;
 import javax.mail.search.MessageIDTerm;
 
 import org.apache.commons.codec.digest.DigestUtils;
 
 import com.sun.mail.gimap.GmailFolder;
 import com.sun.mail.gimap.GmailMessage;
 
 public class MessageParser {
 
 	private GmailMessage message;
 	private String allRecipients;
 	private String originalRecipients;
 	private String charset;
 
 	public MessageParser(Message message) {
 		if (message == null || !(message instanceof GmailMessage)) {
 			throw new IllegalArgumentException("empty message in "
 					+ getClass().getCanonicalName());
 		}
 		this.message = (GmailMessage) message;
 	}
 
 	public GmailFolder getFolder() {
 		return (GmailFolder) message.getFolder();
 	}
 
 	public String[] getLabels() throws MessagingException {
 		return this.message.getLabels();
 	}
 
 	public boolean isSeen() throws MessagingException {
 		return message.isSet(Flags.Flag.SEEN);
 	}
 
 	public boolean isDraft() throws MessagingException {
 		final String[] labels = message.getLabels();
 		return labels.length > 0 && "\\Draft".equals(labels[0]);
 	}
 
 	public String getSubject() throws MessagingException {
 		final String header = this.message.getHeader("Subject", null);
 		setCharsetByValue(header);
 
 		final String subject = this.message.getSubject();
 		return subject == null ? "" : subject;
 	}
 
 	private void setCharsetByValue(final String rawvalue) {
 		if (rawvalue == null || !rawvalue.startsWith("=?"))
 			return;
 
 		int start = 2;
 		int pos;
 		if ((pos = rawvalue.indexOf('?', start)) != -1) {
 			String c = rawvalue.substring(start, pos);
 			int lpos = c.indexOf('*'); // RFC 2231 language specified?
 			if (lpos >= 0) // yes, throw it away
 				c = c.substring(0, lpos);
 			this.setCharset(javaCharset(c));
 		}
 	}
 
 	public long getMessageId() throws MessagingException {
 		final GmailFolder folder = (GmailFolder) message.getFolder();
 		return folder.getUID(message);
 	}
 
	public long getGoogleThreadId() throws MessagingException {
		return message.getThrId();
 	}
 
 	public String getGoogleMessageId() throws MessagingException {
 		return Long.toString(message.getMsgId());
 	}
 
 	public String getRecipients() throws MessagingException {
 		if (this.allRecipients == null) {
 			this.allRecipients = this.resolveRecipientsString(
 					message.getAllRecipients(), message.getFrom());
 		}
 		return allRecipients;
 	}
 
 	public String getOriginalRecipientsId() throws MessagingException {
 		String or = this.originalRecipients;
 		if (or == null) {
 			or = getOriginalRecipients();
 		}
 
 		if (or == null || or.length() == 0) {
 			return "";
 		}
 		return DigestUtils.md5Hex(or);
 	}
 
 	public String getOriginalRecipients() throws MessagingException {
 		if (this.originalRecipients == null) {
 			final String[] ref = message.getHeader("References");
 			if (ref == null) {
 				return "";
 			}
 
 			final GmailFolder folder = (GmailFolder) message.getFolder();
 			final Message[] search = folder.search(new MessageIDTerm(ref[0]
 					.substring(1, ref[0].indexOf(">"))));
 			originalRecipients = search.length > 0 ? this
 					.resolveRecipientsString(search[0].getAllRecipients(),
 							search[0].getFrom()) : "";
 		}
 		return originalRecipients;
 
 	}
 
 	public Date getSentDate() throws MessagingException {
 		return message.getSentDate();
 	}
 
 	public String[] getSender() throws MessagingException {
 		final Address[] from = message.getFrom();
 		if (from.length > 0) {
 			InternetAddress ia = (InternetAddress) from[0];
 			return new String[] { ia.getAddress(), ia.getPersonal() };
 		}
 		return null;
 	}
 
 	public static <T> T[] concat(T[] first, T[] second) {
 		if (first == null) {
 			return second;
 		}
 		if (second == null) {
 			return first;
 		}
 		T[] result = Arrays.copyOf(first, first.length + second.length);
 		System.arraycopy(second, 0, result, first.length, second.length);
 		return result;
 	}
 
 	public String getRecipientsId() throws MessagingException {
 		return DigestUtils.md5Hex(this.getRecipients());
 	}
 
 	public String getContent() throws IOException, MessagingException {
 		final Object content = message.getContent();
 		String result = "";
 		if (content instanceof String) {
 			final HTMLMessageParser htmlMessageParser = new HTMLMessageParser(
 					(String) content);
 			result = htmlMessageParser.getContent();
 		}
 
 		if (content instanceof MimeMultipart) {
 			final MimeMultipart multipart = (MimeMultipart) content;
 			if (multipart.getContentType().startsWith("multipart/MIXED")) {
 				GmailMixedMessageParser mixed = new GmailMixedMessageParser(
 						multipart);
 				result = mixed.getContent();
 				setCharset(mixed.getCharset());
 			} else if (multipart.getContentType().startsWith(
 					"multipart/ALTERNATIVE")) {
 				GmailAlternativeMessageParser p = new GmailAlternativeMessageParser(
 						multipart);
 				result = p.getContent();
 				setCharset(p.getCharset());
 			}
 		}
 
 		return result.length() != 0 ? result : getSubject() == null ? ""
 				: getSubject();
 	}
 
 	private String resolveRecipientsString(Address[] allRecipients,
 			Address[] from) {
 		Address[] concat = concat(allRecipients, from);
 		Arrays.sort(concat, new Comparator<Address>() {
 			@Override
 			public int compare(Address o1, Address o2) {
 				InternetAddress a1 = (InternetAddress) o1;
 				InternetAddress a2 = (InternetAddress) o2;
 				return a1.getAddress().toLowerCase()
 						.compareTo(a2.getAddress().toLowerCase());
 			}
 		});
 		StringBuffer result = new StringBuffer();
 		Set<String> s = new HashSet<String>();
 		for (Address address : concat) {
 			InternetAddress a = (InternetAddress) address;
 			String lcAddress = a.getAddress().toLowerCase();
 			if (s.add(lcAddress)) {
 				result.append(lcAddress);
 				result.append(",");
 			}
 		}
 		if (result.length() > 1) {
 			result.deleteCharAt(result.length() - 1);
 		}
 		return result.toString();
 	}
 
 	@Override
 	public String toString() {
 		try {
 
 			StringBuffer sb = new StringBuffer();
 			sb.append("{ msgId: '");
 			sb.append(getMessageId());
 			sb.append("', group: '");
 			sb.append(getRecipients());
 			sb.append("', subject: '");
 			sb.append(getSubject());
 			sb.append("', content: '");
 			sb.append(getContent());
 			sb.append("'}");
 
 			return sb.toString();
 		} catch (MessagingException e) {
 			e.printStackTrace();
 			return "error";
 		} catch (IOException e) {
 			e.printStackTrace();
 			return "error";
 		}
 	}
 
 	public String getCharset() {
 		return charset;
 	}
 
 	/**
 	 * Convert a MIME charset name into a valid Java charset name.
 	 * <p>
 	 * 
 	 * @param charset
 	 *            the MIME charset name
 	 * @return the Java charset equivalent. If a suitable mapping is not
 	 *         available, the passed in charset is itself returned.
 	 */
 	public static String javaCharset(String charset) {
 		return charset;
 	}
 
 	/**
 	 * Temporary method to get all of a message
 	 * 
 	 * @throws MessagingException
 	 * @throws IOException
 	 */
 	public void saveAttachments() throws MessagingException, IOException {
 		final MimeMultipart content = (MimeMultipart) message.getContent();
 		final int count = content.getCount();
 		for (int i = 0; i < count; i++) {
 			final BodyPart part = content.getBodyPart(i);
 			final String disposition2 = part.getDisposition();
 			final String disposition = disposition2 != null ? disposition2
 					.toLowerCase() : null;
 			if ((disposition != null)
 					&& (disposition.equals(Part.ATTACHMENT) || disposition
 							.equals(Part.INLINE))) {
 				// Check if plain
 				MimeBodyPart mbp = (MimeBodyPart) part;
 				if (!mbp.isMimeType("text/plain")) {
 					String name = decodeName(part.getFileName());
 					System.out.println(name);
 					File savedir = new File("/Users/roy/from_/");
 					savedir.mkdirs();
 					File savefile = new File("/Users/roy/from_/" + name);
 					saveFile(savefile, part);
 				}
 			}
 
 		}
 	}
 
 	protected int saveFile(File saveFile, Part part) throws IOException,
 			MessagingException {
 
 		BufferedOutputStream bos = new BufferedOutputStream(
 				new FileOutputStream(saveFile));
 
 		byte[] buff = new byte[2048];
 		InputStream is = part.getInputStream();
 		int ret = 0, count = 0;
 		while ((ret = is.read(buff)) > 0) {
 			bos.write(buff, 0, ret);
 			count += ret;
 		}
 		bos.close();
 		is.close();
 		return count;
 	}
 
 	protected static String decodeName(String name)
 			throws UnsupportedEncodingException {
 		if (name == null || name.length() == 0) {
 			return "unknown";
 		}
 
 		return MimeUtility.decodeText(name);
 	}
 
 	/**
 	 * @param charset
 	 *            the charset to set
 	 */
 	public void setCharset(String charset) {
		if (charset == null || !"utf-8".equalsIgnoreCase(this.charset)) {
 			this.charset = charset;
 		}
 	}
 
 }
