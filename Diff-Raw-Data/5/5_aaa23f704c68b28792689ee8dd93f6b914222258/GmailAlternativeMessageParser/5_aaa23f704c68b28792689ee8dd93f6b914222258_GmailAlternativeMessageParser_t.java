 package com.brif.nix.parser;
 
 import java.io.IOException;
 import java.nio.charset.Charset;
 import java.nio.charset.IllegalCharsetNameException;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import javax.mail.BodyPart;
 import javax.mail.MessagingException;
 import javax.mail.internet.MimeMultipart;
 
 import org.jsoup.Jsoup;
 import org.jsoup.nodes.Document;
 
 public class GmailAlternativeMessageParser {
 
 	private static final String DEFAULT_CHARSET = "UTF-8";
 	private MimeMultipart body;
 	private String charset;
 
 	public GmailAlternativeMessageParser(MimeMultipart multipart) {
 		this.body = multipart;
 	}
 
 	public String getContent() {
 		try {
 			final int count = this.body.getCount();
 			final BodyPart bodyPart = this.body.getBodyPart(count == 1 ? 0 : 1);
 			charset = getMessageCharset(bodyPart);
 			Document doc = Jsoup.parse(bodyPart.getInputStream(), charset, "");
 			doc.select(".gmail_quote").remove();
 			if (doc.text().trim().length() !=0) {
 				return doc.outerHtml();	
 			} else {
 				return "";
 			}
 		} catch (MessagingException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		return "";
 	}
 	
 	public String getCharset() {
 		return charset;
 	}
 
 	/**
 	 * @param bodyPart
 	 * @return message CharSet 
 	 * @throws MessagingException
 	 */
 	private String getMessageCharset(final BodyPart bodyPart)
 			throws MessagingException {
 		final String[] header = bodyPart.getHeader("Content-Type");
 
 		if (header.length == 0) {
 			return DEFAULT_CHARSET;
 		}
 
 		final Pattern p = Pattern.compile("(\\w+)=(.+)");
 		final Matcher matcher = p.matcher(header[0]);
		if (!matcher.find()) {
 			System.out.println("couldn't parse content type " + header[0]);
 			return DEFAULT_CHARSET;
 		}
 		String charset = matcher.group(2);
 		try {
 			charset = Charset.isSupported(charset) ? charset : DEFAULT_CHARSET;
 		} catch (IllegalCharsetNameException e) {
 			charset = DEFAULT_CHARSET;
 		}
 		return charset;
 	}
 }
