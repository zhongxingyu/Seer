 package com.brif.nix.parser;
 
 import java.io.IOException;
 import java.util.List;
 
 import javax.mail.MessagingException;
 import javax.mail.Part;
 import javax.mail.internet.MimeMultipart;
 
 import com.brif.nix.parser.MessageParser.MessageAttachment;
 
 public class MimeParserFactory {
 
 	public static IMimePraser theEmptyParser;
 	static {
 		theEmptyParser = new IMimePraser() {
 			@Override
 			public String getContent() {
 				return "";
 			}
 			@Override
 			public void collectAttachments(List<MessageAttachment> atts) {
 			}
 		};
 	}
 
 	public static IMimePraser getParser(Part message) {
 		IMimePraser result = null;
 		try {
			Object content = null;
			try {
				content = message.getContent();
			} catch (IOException e) {
				content = "<unknown encoding>";
			}
			
 			if (message.isMimeType("text/*")) {
 				result = new TextMessageParser(content, message);
 			} else if (message.isMimeType("multipart/alternative")) {
 				result = new AlternativeContentParser((MimeMultipart) content);
 			} else if (message.isMimeType("multipart/mixed")) {
 				result = new MixedContentParser((MimeMultipart) content);
 			} else if (message.isMimeType("multipart/related")) {
 				result = new RelatedContentParser((MimeMultipart) content);
 			} else {
 				result = theEmptyParser;
 			}
 			
 		} catch (MessagingException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		return result;
 	}
 
 }
