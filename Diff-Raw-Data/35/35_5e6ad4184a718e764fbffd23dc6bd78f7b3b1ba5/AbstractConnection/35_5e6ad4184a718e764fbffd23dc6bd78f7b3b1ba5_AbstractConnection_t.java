 package client;
 
 import java.io.BufferedReader;
 import java.io.BufferedWriter;
 import java.io.IOException;
 import java.net.Socket;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
 import java.util.List;
 
 import server.ModelEnvelope;
 import client.model.TransferableModel;
 
 /**
  * Abstract connection implements helper methods for reading and 
  * writing generic structures to a stream. Both the ServerConnection
  * and ClientConnection inherits from this.
  * 
  * AbstractConnection is thread safe.
  * 
  * @author Runar B. Olsen <runar.b.olsen@gmail.com>
  */
 public abstract class AbstractConnection {
 

	/**
	 * The default date format used when transmitting models
	 */
	public static final DateFormat defaultDateTimeFormat = new SimpleDateFormat("yyyyMMddHHmmss");
	
 	protected Socket socket;
 	protected BufferedWriter writer;
 	protected BufferedReader reader;
 	
 	/**
 	 * Create a AbstractConnection object
 	 */
 	public AbstractConnection() {
 		this(null, null, null);
 	}
 	
 	/**
 	 * Create a AbstractConnection object
 	 * 
 	 * @param socket
 	 * @param writer
 	 * @param reader
 	 */
 	public AbstractConnection(Socket socket, BufferedWriter writer, BufferedReader reader) {
 		this.socket = socket;
 		this.writer = writer;
 		this.reader = reader;
 	}
 	  	
 	/**
 	 * Send a single line to the user
 	 * 
 	 * @param line
 	 */
 	public void writeLine(String line) throws IOException {
 		synchronized(writer) {
 			writer.write(line + "\r\n");
 			writer.flush();
 		}
 	}
 	
 	/**
 	 * Format and return a command with the given id number and method name
 	 * 
 	 * @param id
 	 * @param method
 	 */
 	protected String formatCommand(int id, String method) {
 		return formatCommand(id, method, "");
 	}
 	
 	/**
 	 * Format and return a command with the given id number, method name and 
 	 * sub method
 	 * 
 	 * @param id
 	 * @param method
 	 * @param smethod
 	 */
 	protected String formatCommand(int id, String method, String smethod) {
 		return String.format("%d %s %s", id, method, smethod).trim();
 	}
 	
 	/**
 	 * Write a error message to stream
 	 * 
 	 * @param errorMessage
 	 * @param id
 	 * @param method
 	 * @param  
 	 */
 	protected void writeError(String errorMessage, int id, String method) 
 			throws IOException {
 		
 		synchronized(writer) {
 			writer.write(formatCommand(id, method, "ERROR")+"\r\n");
 			writer.write(errorMessage+"\r\n");
 			writer.flush();
 		}
 		
 	}
 	
 	/**
 	 * Dumps the given array of models to the stream, the id and method will
 	 * be printed as a header 
 	 * 
 	 * @param models
 	 */	
 	protected void writeModels(List<TransferableModel> models, int id, String method) throws IOException {
 		writeModels(models, id, method, "");
 	}
 	
 	/**
 	 * Dumps the given array of models to the stream
 	 * 
 	 * @param models
 	 * @param id
 	 * @param method
 	 * @param smethod
 	 */
 	protected void writeModels(List<TransferableModel> models, int id, String method, String smethod) 
 			throws IOException {
 
 		synchronized(writer) {	
 			writer.write(formatCommand(id, method, smethod)+"\r\n");
 			ModelEnvelope envelope = new ModelEnvelope(models);
 			envelope.writeToStream(writer);
 			writer.flush();
 		}
 	}
 	
 	/**
 	 * Attempts to read a set of models from the input stream
 	 */
 	protected abstract ModelEnvelope readModels() throws IOException;

 	
 }
