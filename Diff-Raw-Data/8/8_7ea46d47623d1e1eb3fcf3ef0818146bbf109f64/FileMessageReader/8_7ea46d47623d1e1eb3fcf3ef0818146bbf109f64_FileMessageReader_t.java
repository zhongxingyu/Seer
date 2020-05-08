 package ru.spbau.martynov.task1;
 
 import java.io.BufferedReader;
 import java.io.FileNotFoundException;
 import java.io.FileReader;
 import java.io.IOException;
 
 /**
  * @author Semen A Martynov, 10 Feb 2013 22:40
  * 
  *         The class allows to read out messages from the prepared file.
  */
 public class FileMessageReader {
 
 	/**
 	 * Constructor from the file name containing messages.
 	 * 
 	 * @param filename
 	 *            file with messages.
 	 * @throws FileNotFoundException
 	 *             there is no access to the specified file (or the file doesn't
 	 *             exist).
 	 */
 	public FileMessageReader(String filename) throws FileNotFoundException {
 		reader = new BufferedReader(new FileReader(filename));
 	}
 
 	/**
 	 * Release resources.
 	 * 
 	 * @throws IOException
 	 *             the stream is closed or another IOException occurs.
 	 */
 	public void close() throws IOException {
 		reader.close();
 	}
 
 	/**
 	 * Function reads the message from the file, and saves it in special
 	 * structure Message.
 	 * 
 	 * @return the message from the file.
 	 * @throws IllegalMessageFormatException
 	 *             Unexpected end of the file or syntax error.
 	 * @throws IOException
 	 *             the stream is closed or another IOException occurs.
 	 */
 	public Message getMessage() throws IllegalMessageFormatException,
 			IOException {
 		String counterSting = null;
 		// Let's try to define number of lines in the message.
 		if ((counterSting = reader.readLine()) != null) {
 
 			// If the counterSting doesn't contain number - we will throw an
 			// exception.
 			int counter = 0;
 			try {
 				counter = Integer.parseInt(counterSting);
 			} catch (NumberFormatException e) {
 				throw new IllegalMessageFormatException("Syntax error, "
 						+ counterSting + "is not a number.", e);
 			}
 
 			// At this point we know quantity of lines in the message
			Message resultMessage = new Message();
 			String messageLine = null;
 			while (counter-- > 0) {
 				if ((messageLine = reader.readLine()) == null) {
 					throw new IllegalMessageFormatException(
 							"Syntax error, unexpected end of file.");
 				} else {
 					if (resultMessage == null) {
 						resultMessage = new Message();
 					}
 					resultMessage.addLine(messageLine);
 				}
 			}
			return resultMessage;
 		}
 		// If the file was empty, the result remains null.
		return null;
 	}
 
 	/**
 	 * The buffer for reading from the file.
 	 */
 	private BufferedReader reader = null;
 
 }
