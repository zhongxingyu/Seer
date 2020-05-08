 package edu.uw.cs.cse461.Net.TCPMessageHandler;
 
 import java.io.EOFException;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.net.InetAddress;
 import java.net.Socket;
 import java.nio.ByteBuffer;
 import java.nio.ByteOrder;
 
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 
import edu.uw.cs.cse461.Net.Base.NetBase;
import edu.uw.cs.cse461.util.ConfigManager;
 import edu.uw.cs.cse461.util.Log;
 
 
 /**
  * Sends/receives a message over an established TCP connection.
  * To be a message means the unit of write/read is demarcated in some way.
  * In this implementation, that's done by prefixing the data with a 4-byte
  * length field.
  * <p>
  * Design note: TCPMessageHandler cannot usefully subclass Socket, but rather must
  * wrap an existing Socket, because servers must use ServerSocket.accept(), which
  * returns a Socket that must then be turned into a TCPMessageHandler.
  *  
  * @author zahorjan
  *
  */
 public class TCPMessageHandler implements TCPMessageHandlerInterface {
 	private static final String TAG="TCPMessageHandler";
 	private static final int MESSAGE_HEADER_SIZE = 4;
 
	private Socket socket;
 	private final int readMessageTimeout;
 	private final int readMessageInterval;
 
 	private int maxReadLength;
 	
 	//--------------------------------------------------------------------------------------
 	// helper routines
 	//--------------------------------------------------------------------------------------
 
 	/**
 	 * We need an "on the wire" format for a binary integer.
 	 * This method encodes into that format, which is little endian
 	 * (low order bits of int are in element [0] of byte array, etc.).
 	 * @param i
 	 * @return A byte[4] encoding the integer argument.
 	 */
 	protected static byte[] intToByte(int i) {
 		ByteBuffer b = ByteBuffer.allocate(4);
 		b.order(ByteOrder.LITTLE_ENDIAN);
 		b.putInt(i);
 		byte buf[] = b.array();
 		return buf;
 	}
 	
 	/**
 	 * We need an "on the wire" format for a binary integer.
 	 * This method decodes from that format, which is little endian
 	 * (low order bits of int are in element [0] of byte array, etc.).
 	 * @param buf
 	 * @return An int decoded from the specified byte buffer.
 	 */
 	protected static int byteToInt(byte buf[]) {
 		ByteBuffer b = ByteBuffer.allocate(4);
 		b.order(ByteOrder.LITTLE_ENDIAN);
 		b.put(buf);
 		return b.getInt();
 	}
 
 	/**
 	 * Constructor, associating this TCPMessageHandler with a connected socket.  If the socket is not already connected, then there will be hell to pay.
 	 * @param sock
 	 * @throws IOException
 	 */
 	public TCPMessageHandler(Socket sock) throws IOException {
 		socket = sock;
 
 		ConfigManager config = NetBase.theNetBase().config();
 		readMessageTimeout = config.getAsInt("tcpmessagehandler.readmessagetimeout", -1, TAG);
 		readMessageInterval = config.getAsInt("tcpmessagehandler.readmessageinterval", -1, TAG);
 	}
 	
 	/**
 	 * Closes resources allocated by this TCPMessageHandler; doesn't close the socket it's attached to.  You're a grownup now, you can close it yourself.
 	 * The TCPMessageHandler object is unusable after execution of this method.
 	 */
 	public void discard() {
 		// Do not close the socket, just release it.
 		socket = null;
 	}
 
 	/**
 	 * Sets the maximum allowed size for which decoding of a message will be attempted.  Used to guard against possibly preposterous payload length values indicated in a received message (length + payload).
 	 * @return The previous setting of the maximum allowed message length.
 	 */
 	@Override
 	public int setMaxReadLength(int maxLen) {
 		int oldLength = maxReadLength;
 		maxReadLength = maxLen;
 		return oldLength;
 	}
 
 	//--------------------------------------------------------------------------------------
 	// send routines
 	//--------------------------------------------------------------------------------------
 	
 	public void sendMessage(byte[] buf) throws IOException {
 		OutputStream outputStream = socket.getOutputStream();
 		// write out the header:  the length of the payload
 		outputStream.write(intToByte(buf.length));
 		// write out the payload
 		outputStream.write(buf);
 	}
 	
 	public void sendMessage(String str) throws IOException {
 		sendMessage(str.getBytes());
 	}
 	
 	public void sendMesssage(JSONArray jsArray) throws IOException {
		sendMessage(jsArray.toString());
 	}
 	
 	public void sendMessage(JSONObject jsObject) throws IOException {
 		sendMessage(jsObject.toString());
 	}
 	
 	//--------------------------------------------------------------------------------------
 	// read routines
 	//--------------------------------------------------------------------------------------
 	
 	/**
 	 * @Return the message as a byte array, or null if message reading failed.
 	 */
 	private byte[] readFromStream(InputStream inputStream, int readLength) throws IOException {
 	 	// Sanitizes the specified read length to be non-preposterous.
 		if (readLength < 0 || readLength > maxReadLength) {
 			Log.w(TAG, "Message read length "+readLength+" is of invalid size.  Message read cancelled.");
 			return null;
 		}
 
 		byte[] buf = new byte[readLength];
 
 		int totalBytesRead = 0;
 		int readTimeInMillis = 0; // Keep track of the total time reading
 		while(totalBytesRead < buf.length && readTimeInMillis < readMessageTimeout) {
 			// sleep the thread for a little bit
 			try {
 				Thread.sleep(readMessageInterval);
 			} catch (InterruptedException e) {
 				Log.i(TAG, "Socket read sleep timer interrupted");
 				e.printStackTrace();
 			}
 			readTimeInMillis += readMessageInterval;
 
 			// read from the stream
 			int bytesRead = inputStream.read(buf, totalBytesRead, buf.length - totalBytesRead);
 
 			// check if the stream was finished
 			if (bytesRead != -1) {
 				// if the stream wasn't finished, then update byte counts
 				totalBytesRead += bytesRead;
 			} else {
 				break;
 			}
 		}
 		Log.d(TAG, totalBytesRead+" total bytes read from the socket");
 
 		return buf;
 	}
 
 	/**
 	 * Reads incoming messages on this Message Handler's socket.
 	 * @Return the message as a byte array, or null if message reading failed.
 	 */
 	public byte[] readMessageAsBytes() throws IOException {
 		byte[] payload = null;
 		InputStream inputStream = socket.getInputStream();
 
 		byte[] header = readFromStream(inputStream, MESSAGE_HEADER_SIZE);
 		int payloadLength = byteToInt(header);
 		
 		payload = readFromStream(inputStream, payloadLength);
 		
 		return payload;
 	}
 	
 	public String readMessageAsString() throws IOException {
 		return new String(readMessageAsBytes());
 	}
 	
 	public JSONArray readMessageAsJSONArray() throws IOException, JSONException {
 		return new JSONArray(readMessageAsString());
 	}
 	
 	public JSONObject readMessageAsJSONObject() throws IOException, JSONException {
 		return new JSONObject(readMessageAsString());
 	}
 
 }
