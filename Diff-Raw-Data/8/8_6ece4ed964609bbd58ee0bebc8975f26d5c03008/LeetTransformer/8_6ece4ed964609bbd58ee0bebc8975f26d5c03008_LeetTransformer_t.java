 package model.mail.transformerimpl;
 
 import java.io.File;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.util.Map;
 import java.util.Scanner;
 
 import model.parser.mime.MimeHeader;
 import model.util.Base64Util;
 
 import org.apache.commons.codec.DecoderException;
 import org.apache.commons.codec.net.QuotedPrintableCodec;
 
 public class LeetTransformer implements Transformer {
 
 	@Override
 	public StringBuilder transform(StringBuilder text,
 			Map<String, MimeHeader> partheaders) throws IOException {
 		MimeHeader contentType = partheaders.get("Content-Type");
 		MimeHeader contentTransferEncoding = partheaders.get("Content-Transfer-Encoding");
 		if (contentType == null) {
 			return text;
 		}
 		if (contentTransferEncoding == null && contentType.getValue().startsWith("text/plain")) {
 			return new StringBuilder(toLeet(text.toString()));
		} else if (contentTransferEncoding != null && "base64".equals(contentTransferEncoding.getValue())
				&& contentType.getValue().startsWith("text/plain")) {
 			File encodedFile = File.createTempFile("encode_", ".tmp");
 			FileWriter encodedContentsWriter = new FileWriter(encodedFile);
 			try {
 				File decodedFile = Base64Util.decodeUsingOS(text.toString());
 				Scanner decodedContents = new Scanner(decodedFile);
 				while (decodedContents.hasNextLine()) {
 					encodedContentsWriter.append(toLeet(decodedContents.nextLine() + "\r\n"));
 				}
 				decodedContents.close();
 				encodedContentsWriter.close();
 				File encodedText = Base64Util.encodeToFile(encodedFile);
 				Scanner encodedTextScaner = new Scanner(encodedText);
 				StringBuilder builder = new StringBuilder();
 				while (encodedTextScaner.hasNextLine()) {
 					String line = encodedTextScaner.nextLine();
 					builder.append(line);
 				}
 				encodedTextScaner.close();
 				return builder;
 			} catch (InterruptedException e) {
 				throw new IllegalStateException(e);
 			}
		} else if(contentTransferEncoding != null && contentType.getValue().startsWith("text/plain")) { // quoted-printable
 			String decodeString = decode(text.toString());
 			String transformed = toLeet(decodeString);
 			return new StringBuilder(transformed);
 		}
		return text;
 	}
 
 	private String toLeet(String text) {
 		String textString = text;
 		textString = textString.replace("a", "4");
 		textString = textString.replace("e", "3");
 		textString = textString.replace("i", "1");
 		textString = textString.replace("a", "4");
 		return textString;
 	}
 
 	private String decode(String quotedPrintable) {
 		QuotedPrintableCodec codec = new QuotedPrintableCodec("ISO-8859-1");
 		try {
 			return codec.decode(quotedPrintable);
 		} catch (DecoderException e) {
 			throw new IllegalStateException("Could not decode: " + quotedPrintable);
 		}
 	}
 
 }
