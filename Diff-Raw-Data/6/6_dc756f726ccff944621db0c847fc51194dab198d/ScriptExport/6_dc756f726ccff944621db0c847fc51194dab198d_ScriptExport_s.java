 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.io.OutputStream;
 import java.util.List;
 import java.util.zip.ZipEntry;
 import java.util.zip.ZipOutputStream;
 import java.util.Date;
 
 import org.jdom.Document;
 import org.jdom.Element;
 import org.jdom.output.XMLOutputter;
 
 // RabbitMQ libraries - the actual jar file needs to be placed in C:\Ephesoft\Application\WEB-INF\lib
 import com.rabbitmq.client.*;
 
 
 import com.ephesoft.dcma.script.IJDomScript;
 
 public class ScriptExport implements IJDomScript {
 
 	private static final String BATCH_LOCAL_PATH = "BatchLocalPath";
 	private static final String BATCH_INSTANCE_ID = "BatchInstanceIdentifier";
 	private static final String EXT_BATCH_XML_FILE = "_batch.xml";
 	private static String ZIP_FILE_EXT = ".zip";
 	private static String AMQP_URI = "amqp://guest:guest@127.0.0.1:5672/";
 	private static String AMQP_EXCHANGE = "TestExchange";
 	private static String AMQP_QUEUE = "TestQueue";
 	private static String AMQP_ROUTING_KEY = "#";
 	
 	public Object execute(Document document, String methodName, String docIdentifier) {
 		Exception exception = null;
 		try {
 			System.out.println("*************  Inside ExportScript scripts.");
 
 			System.out.println("*************  Start execution of the ExportScript scripts.");
 
 			if (null == document) {
 				System.out.println("Input document is null.");
 				return null;
 			}
 			
 			System.out.println("*************  We have a valid document.");
 			
 			// Publish the AMQP message to the RabbitMQ server
 			publishAmqpMessage(document);
 			
 			System.out.println("*************  End execution of the ScriptExport scripts.");
 		} catch (Exception e) {
 			System.out.println("*************  Error occurred in scripts." + e.getMessage());
 			exception = e;
 		}
 		return null;
 	}
 
 	/**
 	 * The <code>publishAmqpMessage</code> method will publish a message to a configured RabbitMQ/AMQP server.
 	 * 
 	 * @param document {@link Document}.
 	 */
 	private void publishAmqpMessage(Document document) {
 		Exception exception = null;
 		try {
 			String batchInstanceID = null;
 			List<?> batchInstanceIDList = document.getRootElement().getChildren(BATCH_INSTANCE_ID);
 			if (null != batchInstanceIDList) {
 				batchInstanceID = ((Element) batchInstanceIDList.get(0)).getText();
 			}
 
 			if (null == batchInstanceID) {
 				System.err.println("Unable to find the batch instance ID in batch xml file.");
 				return;
 			}
 			
 			// Create a message to send via AMQP
 			Date dateNow = new Date();
			String theDate = String.format("Date = %1", dateNow);
 			System.out.println(theDate);
 			System.out.println(batchInstanceID);
 			String message = String.format("{\"DocumentIdentifier\": \"%1\",\"DateReceived\": \"%2\"}", batchInstanceID, dateNow);
 			//String message = String.format("{ \"DocumentIdentifier\": \"%1\" }", docIdentifier);
 			
 			System.out.println("Message to send: ");
 			System.out.println(message);
 			
 			byte[] messageBodyBytes = message.getBytes();
 			
 			// We have a document, so open a connection to the RabbitMQ server
 			ConnectionFactory factory = new ConnectionFactory();
 			factory.setUri(AMQP_URI);
 			// factory.setUsername("guest");
 			// factory.setPassword("guest");
 			// factory.setVirtualHost("/");
 			// factory.setHost("12.0.0.1");
 			// factory.setPort(5672);
 			
 			Connection conn = factory.newConnection();
 			// Open a channel
 			Channel channel = conn.createChannel();
 			
 			System.out.println("Channel open, sending message...");
 			
 			// Send a message that we have a new document
 			channel.basicPublish(AMQP_EXCHANGE, AMQP_ROUTING_KEY, null, messageBodyBytes);
 			
 			// Send with builder class
 			// channel.basicPublish(AMQP_EXCHANGE, AMQP_ROUTING_KEY,
 								// new AMQP.BasicProperties.Builder()
 								// .contentType("text/plain").deliveryMode(2)
 								// .priority(1)
 								// .build(),
 								// messageBodyBytes);
 			
 			// Close the connection
 			channel.close();
 			conn.close();
 			
 		} catch (Exception e) {
 			System.out.println("*************  Error occurred in sending AMQP Message." + e.getMessage());
 			exception = e;
 		}
 	}
 	
 	
 	/**
 	 * The <code>writeToXML</code> method will write the state document to the XML file.
 	 * 
 	 * @param document {@link Document}.
 	 */
 	private void writeToXML(Document document) {
 		String batchLocalPath = null;
 		List<?> batchLocalPathList = document.getRootElement().getChildren(BATCH_LOCAL_PATH);
 		if (null != batchLocalPathList) {
 			batchLocalPath = ((Element) batchLocalPathList.get(0)).getText();
 		}
 
 		if (null == batchLocalPath) {
 			System.err.println("Unable to find the local folder path in batch xml file.");
 			return;
 		}
 
 		String batchInstanceID = null;
 		List<?> batchInstanceIDList = document.getRootElement().getChildren(BATCH_INSTANCE_ID);
 		if (null != batchInstanceIDList) {
 			batchInstanceID = ((Element) batchInstanceIDList.get(0)).getText();
 
 		}
 
 		if (null == batchInstanceID) {
 			System.err.println("Unable to find the batch instance ID in batch xml file.");
 			return;
 		}
 
 		String batchXMLPath = batchLocalPath.trim() + File.separator + batchInstanceID + File.separator + batchInstanceID
 				+ EXT_BATCH_XML_FILE;
 
 		String batchXMLZipPath = batchXMLPath + ZIP_FILE_EXT;
 
 		System.out.println("batchXMLZipPath************" + batchXMLZipPath);
 
 		OutputStream outputStream = null;
 		File zipFile = new File(batchXMLZipPath);
 		FileWriter writer = null;
 		XMLOutputter out = new XMLOutputter();
 		try {
 			if (zipFile.exists()) {
 				System.out.println("Found the batch xml zip file.");
 				outputStream = getOutputStreamFromZip(batchXMLPath, batchInstanceID + EXT_BATCH_XML_FILE);
 				out.output(document, outputStream);
 			} else {
 				writer = new java.io.FileWriter(batchXMLPath);
 				out.output(document, writer);
 				writer.flush();
 				writer.close();
 			}
 		} catch (Exception e) {
 			System.err.println(e.getMessage());
 		} finally {
 			if (outputStream != null) {
 				try {
 					outputStream.close();
 				} catch (IOException e) {
 				}
 			}
 		}
 	}
 
 	public static OutputStream getOutputStreamFromZip(final String zipName, final String fileName) throws FileNotFoundException,
 			IOException {
 		ZipOutputStream stream = null;
 		stream = new ZipOutputStream(new FileOutputStream(new File(zipName + ZIP_FILE_EXT)));
 		ZipEntry zipEntry = new ZipEntry(fileName);
 		stream.putNextEntry(zipEntry);
 		return stream;
 	}
 }
