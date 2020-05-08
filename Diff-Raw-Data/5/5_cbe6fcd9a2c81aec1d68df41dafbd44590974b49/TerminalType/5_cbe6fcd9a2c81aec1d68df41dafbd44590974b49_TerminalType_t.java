 package com.sf.jintn3270.telnet;
 
 import java.io.ByteArrayOutputStream;
 
 /**
  * Implementation of RFC 884
  * 
  * Our implementation does not initiate TerminalType. We only try to negotiate
  * terminal type when we're asked for it by the remote host we're connecting to.
  * 
  * Once the option is enabled (we've received a DO and responded with WILL -- 
  * which is handled in TelnetClient) and we receive an incoming SB if we're 
  * asked to SEND our terminal type, we obtain it from the TerminalModel of the
  * current TelnetClient.
  */
 public class TerminalType extends Option {
 	static short SEND = 1;
 	static short IS = 0;
 	
 	private int requests;
 	
 	public TerminalType() {
 		super();
 		requests = 0;
 	}
 	
 	public String getName() {
 		return "TerminalType";
 	}
 	
 	public short getCode() {
 		return 24;
 	}
 	
 	/** Do nothing!  We never initiate. */
 	public void initiate(TelnetClient client) {
 	}
 	
 	public int consumeIncomingSubcommand(short[] incoming, TelnetClient client) {
 		return consumeIncoming(incoming, client);
 	}
 	
 	public int consumeIncoming(short[] incoming, TelnetClient client) {
 		// Consume an IAC SB <code> SEND IAC SE
 		if (incoming[0] == client.IAC && 
 			incoming[1] == client.SB && 
 			incoming[2] == getCode() &&
 			incoming[3] == SEND &&
 			incoming[4] == client.IAC &&
 			incoming[5] == client.SE)
 		{
 			// Write our termtype message to our output buffer.
 			try {
				System.out.println("Writing term type");
				out.write(new short[] {IAC, SB, getCode(), IS});
 				
 				String[] names = client.getTerminalModel().getModelName();
 				if (requests >= names.length) {
 					out.write(names[names.length - 1].getBytes("ASCII"));
 				} else {
 					out.write(names[requests].getBytes("ASCII"));
 				}
 				out.write(new short[] {IAC, SE});
 				return 6;
 			} catch (Exception ex) {}
 		} else if (incoming[0] == client.IAC &&
 			incoming[1] == client.SB &&
 			incoming[2] == getCode() &&
 			incoming[3] == IS)
 		{
 			// Search for the IAC SE...
 			boolean subEnds = false;
 			int end = 0;
 			for (end = 4; end < incoming.length - 1 && !subEnds; end++) {
 				subEnds = (incoming[end] == client.IAC && incoming[end + 1] == client.SE);
 			}
 			/* if subEnds == true then,
 			 *   end is the zero-based offset into the array where the second
 			 * IAC is located. We need to report that we've read up through 
 			 * the SE when we're done reading the remote term type.
 			 */
 			
 			// Everything between (IAC SB <code> IS) and (IAC SE) is the terminal type name.
 			if (subEnds) {
 				StringBuffer termName = new StringBuffer();
 				for (int i = 4; i < end; i++) {
 					termName.append((char)incoming[i]);
 				}
 				System.out.println("RECEIVED TERMINAL IS: " + termName);
 				return end + 2; 
 			}
 		}
 		return 0;
 	}
 }
