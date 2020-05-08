 package com.brif.nix.parser;
 
 import java.io.IOException;
 import java.nio.charset.Charset;
 import java.nio.charset.IllegalCharsetNameException;
 import java.util.List;
 import java.util.Scanner;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import javax.mail.MessagingException;
 import javax.mail.Part;
 
 import org.jsoup.Jsoup;
 import org.jsoup.nodes.Document;
 import org.jsoup.nodes.Element;
 import org.jsoup.nodes.Node;
 import org.jsoup.select.Elements;
 
 import com.brif.nix.parser.MessageParser.MessageAttachment;
 
 public class TextMessageParser implements IMimePraser {
 
 	private Object content;
 	private Part message;
 
 	private static final String DEFAULT_CHARSET = "UTF-8";
 	private String charset;
 	private boolean isPromotional = false;
 
 	public TextMessageParser(Object content2, Part message) {
 		this.content = content2;
 		this.message = message;
 	}
 
 	public String getContent() {
 		if (this.content instanceof String) {
 			final String text = (String) this.content;
 			try {
 				if (message.isMimeType("text/html")) {
 					String charset = getMessageCharset(message);
 					Document doc = Jsoup.parse(message.getInputStream(),
 							charset, "");
 					doc.select("head").remove();
 					doc.select("style").remove();
 
 					isPromotional = doc.select("img").size() > 1;
 					
 					// remove tail
 					removeGmail(doc);
 					removeMsOutlook(doc);
 					removeIOS(doc);
 
 					if (doc.text().trim().length() != 0) {
 						return doc.outerHtml();
 					} else {
 						return "";
 					}
 				} else {
 					return removeTail(text);
 				}
 			} catch (MessagingException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			} catch (IOException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 		}
 		return "";
 	}
 
 	private String removeTail(String text) {
 		final Scanner scanner = new Scanner(text);
 		StringBuilder sb = new StringBuilder();
 
 		String nextLine = scanner.nextLine();
 		do {
 			if (!nextLine.startsWith(">")) {
 				sb.append(nextLine);
 				sb.append("<br/>");
 			}
 			nextLine = scanner.nextLine();
 		} while (scanner.hasNextLine());
 
 		return sb.toString();
 	}
 
 	@Override
 	public void collectAttachments(List<MessageAttachment> atts) {
 		if (!(this.content instanceof String)) {
 			// TODO add attachment of text/* attachments
 		}
 		return;
 	}
 
 	protected static boolean removeGmail(Document doc) {
 		final Elements select = doc.select(".gmail_quote");
 		if (select.size() > 0) {
 			select.remove();
 			return true;
 		}
 		return false;
 	}
 
 	protected static boolean removeIOS(Document doc) {
 		final Elements select = doc.select("blockquote");
 		if (select.size() > 0) {
			final Node previousSibling = select.get(0).previousSibling();
			if (previousSibling != null) {
				previousSibling.remove();	
			}
 			select.get(0).remove();
 			return true;
 		}
 		return false;
 	}
 
 	protected static boolean removeMsOutlook(Document doc) {
 		final Elements select = doc.select(".MsoNormal");
 		if (select.size() > 0) {
 			final Element appendElement = doc.prependElement("style");
 			appendElement
 					.html("p.MsoNormal, li.MsoNormal, div.MsoNormal {margin:0cm; margin-bottom:.0001pt; font-size:12.0pt; font-family:\"Times New Roman\",\"serif\";}");
 			Element e = select.get(0).nextElementSibling();
 			while (e != null && e.hasClass("MsoNormal")) {
 				e = e.nextElementSibling();
 			}
 			while (e != null) {
 				Node previous = e;
 				e = e.nextElementSibling();
 				previous.remove();
 			}
 			return true;
 		}
 		return false;
 	}
 
 	public String getCharset() {
 		return charset;
 	}
 
 	/**
 	 * @param bodyPart
 	 * @return message CharSet
 	 * @throws MessagingException
 	 */
 	private String getMessageCharset(final Part bodyPart)
 			throws MessagingException {
 		final String header = bodyPart.getContentType();
 		if (header == null) {
 			System.out.println("couldn't parse content type");
 			return DEFAULT_CHARSET;
 		}
 
 		final Pattern p = Pattern.compile("(\\w+)\\s*=\\s*\\\"?([^\\s;\\\"]*)");
 		final Matcher matcher = p.matcher(header);
 		if (!matcher.find()) {
 			System.out.println("couldn't parse content type " + header);
 			return DEFAULT_CHARSET;
 		}
 		String charset = matcher.group(2);
 		try {
 			charset = Charset.isSupported(charset) ? charset.toUpperCase()
 					: DEFAULT_CHARSET;
 		} catch (IllegalCharsetNameException e) {
 			charset = DEFAULT_CHARSET;
 		}
 		return charset;
 	}
 	
 	public boolean isPromotional() {
 		return isPromotional;
 	}
 }
