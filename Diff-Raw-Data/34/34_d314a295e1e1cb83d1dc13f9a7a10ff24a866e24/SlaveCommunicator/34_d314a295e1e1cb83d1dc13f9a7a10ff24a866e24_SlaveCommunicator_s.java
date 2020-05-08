 package no.uio.master.autoscale.slave;
 
 import java.io.IOException;
 import java.io.ObjectInputStream;
 import java.io.ObjectOutputStream;
 import java.net.Socket;
 
 
import no.uio.master.model.SlaveMessage;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 public class SlaveCommunicator {
 	private static Logger LOG = LoggerFactory.getLogger(SlaveCommunicator.class);
 	private static ObjectOutputStream outputStream;
 	private static ObjectInputStream inputStream;
 	
 	private static Integer DEFAULT_PORT = 7799;
 	public SlaveCommunicator() {
 		
 	}
 	
 	
 	public static SlaveMessage readMessage(Socket socket) {
 		SlaveMessage msg = null;
 		try {
 			
 			LOG.debug("Reading message");
 			inputStream = new ObjectInputStream(socket.getInputStream());
 			
 			msg = (SlaveMessage)inputStream.readObject();
 			LOG.debug("Read message: " + msg);
 		} catch (Exception e) {
 			LOG.error("Failed to read message - ",e);
 		}
 		return msg;
 	}
 	
 	public static void sendMessage(Object obj, Socket socket) {
 		try {
 			LOG.debug("Sending message");
 			outputStream = new ObjectOutputStream(socket.getOutputStream());
 			outputStream.writeObject(obj);
 			outputStream.flush();
 			LOG.debug("Message sent");
 		} catch (Exception e) {
 			LOG.error("Failed to send message - ",e);
 		} finally {
 			try {
 				outputStream.close();
 				socket.close();
 			} catch (IOException e) {
 				LOG.error("Failed to close output-stream");
 			}
 		}
 	}
 }
