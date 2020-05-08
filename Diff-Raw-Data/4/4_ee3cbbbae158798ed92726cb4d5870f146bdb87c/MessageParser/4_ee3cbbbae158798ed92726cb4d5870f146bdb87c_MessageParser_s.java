 package ar.edu.itba.pdc.duta.http;
 
 import java.nio.ByteBuffer;
import org.apache.commons.lang3.text.WordUtils;
 
 public abstract class MessageParser {
 
 	private enum States {
 		START_LINE, BEGINNING_OF_LINE, FIELD_NAME, FIELD_VALUE, END_OF_HEADER 
 	}
 
 	private ByteBuffer buffer;
 	private States state;
 	private StringBuilder currString;
 	private String fieldName;
 	protected MessageHeader header;
 	
 	public MessageParser(ByteBuffer buffer) {
 
 		this.buffer = buffer;
 		this.state = States.START_LINE;
 		this.currString = new StringBuilder();
 	}
 
 	public boolean parse() throws Exception {
 		
 		char oldc, c = '\0';
 
 		while (state != States.END_OF_HEADER && buffer.hasRemaining()) {
 
 			oldc = c;
 			c = (char)buffer.get();
 			
 			switch (state) {
 				
 				case START_LINE:
 					
 					if (c == '\n') {
 					
 						String s = currString.toString().trim();
 						currString.setLength(0);
 						
 						if (s.length() > 0) {
 							header.setStartLine(s);
 							state = States.BEGINNING_OF_LINE;
 						}
 						break;
 					}
 					currString.append(c);
 					break;
 	
 				case BEGINNING_OF_LINE:
 					
 					switch(c) {
 					
 						case '\r':
 							break;
 							
 						case '\n':
 							state = States.END_OF_HEADER;
 							break;
 							
 						case ' ':
 						case '\t':
 							state = States.FIELD_VALUE;
 							break;
 							
 						default:
 							state = States.FIELD_NAME;
 							break;
 					}
 					break;
 				
 				case FIELD_NAME:
 
 					if (!Grammar.isTokenCharacter(oldc)) {
 						throw new Exception();
 					}
 
 					currString.append(oldc);
 
 					if (c == ':') {
 
						fieldName = WordUtils.capitalizeFully(currString.toString(), '-');
 						currString.setLength(0);
 						state = States.FIELD_VALUE;
 
 						if (fieldName.length() == 0) {
 							throw new Exception();
 						}
 						break;
 					}
 					break;
 					
 				case FIELD_VALUE:
 					
 					if (c == '\r') {
 						c = ' ';
 					}
 					
 					if (c == '\n') {
 						
 						if (fieldName == null) {
 							throw new Exception();
 						}
 
 						header.setField(fieldName, currString.toString());
 						currString.setLength(0);
 						state = States.BEGINNING_OF_LINE;
 						break;
 					}
 					
 					if (Grammar.isControlCharacter(c)) {
 						throw new Exception();
 					}
 					
 					currString.append(c);
 					break;
 			}
 		}
 
 		return state == States.END_OF_HEADER;
 	}
 	
 	public MessageHeader getHeader() {
 		
 		header.trimValues();
 		return header;
 	}
 }
 
