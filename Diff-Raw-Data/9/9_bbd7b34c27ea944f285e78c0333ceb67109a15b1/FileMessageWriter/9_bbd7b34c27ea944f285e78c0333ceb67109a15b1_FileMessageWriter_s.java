 package ru.spbau.martynov.task1;
 
 import java.io.BufferedWriter;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.util.List;
 
 /**
  * @author Semen A Martynov, 10 Feb 2013 23:28
  * 
  *         Class for a message output in the file. Each message is numbered.
  */
 public class FileMessageWriter implements MessageWriter {
 
 	/**
 	 * Constructor with the file name for messages.
 	 * 
 	 * @param filename
 	 *            file for record messages.
 	 * @throws IOException
 	 *             the stream is closed or another IOException occurs.
 	 */
 	public FileMessageWriter(String filename) throws IOException {
 		writer = new BufferedWriter(new FileWriter(filename));
 	}
 
 	/**
 	 * Function numbers messages, and writes them in the file.
 	 * 
 	 * @param message for outputting.
 	 * @throws IOException
 	 *             the stream is closed or another IOException occurs.
 	 */
 	@Override
 	public void writeMessage(Message message) throws IOException {
 		List<String> messageLines = message.getLines();
 
 		// Strings counter.
 		writer.write(messageLines.size() + "\n");
 		// Strings value.
 		for (String messageLine : messageLines) {
 			writer.write(messageLine + "\n");
 		}
 
 	}
 
 	/**
 	 * Function for release of resources.
 	 * 
 	 * @throws IOException
 	 *             the stream is closed or another IOException occurs.
 	 */
 	@Override
 	public void close() throws IOException {
 		writer.close();
 
 	}
 
 	/**
 	 * The buffer for record of messages in the file.
 	 */
	private BufferedWriter writer = null;
 }
