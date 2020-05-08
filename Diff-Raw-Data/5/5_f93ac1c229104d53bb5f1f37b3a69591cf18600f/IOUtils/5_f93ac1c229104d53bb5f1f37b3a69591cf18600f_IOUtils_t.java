 package com.alexrnl.commons.io;
 
 import java.io.BufferedReader;
 import java.io.EOFException;
 import java.io.IOException;
 import java.util.Objects;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 /**
  * Utility methods for IO stuff.<br />
  * @author Alex
  */
 public final class IOUtils {
 	/** Logger */
	private static Logger			lg						= Logger.getLogger(IOUtils.class.getName());
 	
 	/** The byte order mark used at the beginning of unicode files */
	public static final Character	UNICODE_BYTE_ORDER_MARK	= '\ufeff';
 	
 	/**
 	 * Constructor #1.<br />
 	 * Default private constructor.
 	 */
 	private IOUtils () {
 		super();
 	}
 	
 	/**
 	 * Read the next line on the buffered reader provided.<br />
 	 * @param reader
 	 *        the stream to read.
 	 * @return the next line in the stream.
 	 * @throws IOException
 	 *         if there was an issue when reading the stream.
 	 * @throws EOFException
 	 *         if the line returned is <code>null</code>.
 	 */
 	public static String readLine (final BufferedReader reader) throws IOException, EOFException {
 		Objects.requireNonNull(reader);
 		final String line = reader.readLine();
 		if (lg.isLoggable(Level.FINE)) {
 			lg.fine("Read line: " + line);
 		}
 		if (line == null) {
 			throw new EOFException("End of stream reached");
 		}
 		return line;
 	}
 }
