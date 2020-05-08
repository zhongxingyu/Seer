 package com.alexrnl.subtitlecorrector.io;
 
 import java.io.BufferedReader;
 import java.io.EOFException;
 import java.io.IOException;
 import java.nio.charset.StandardCharsets;
 import java.nio.file.Files;
 import java.nio.file.Path;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import com.alexrnl.commons.error.ExceptionUtils;
 import com.alexrnl.subtitlecorrector.common.Subtitle;
 import com.alexrnl.subtitlecorrector.common.SubtitleFile;
 
 /**
  * Abstract class for a subtitle reader.<br />
  * @author Alex
  */
 public abstract class SubtitleReader {
 	/** Logger */
 	private static Logger	lg	= Logger.getLogger(SubtitleReader.class.getName());
 	
 	/**
 	 * Constructor #1.<br />
 	 * Default constructor.
 	 */
 	public SubtitleReader () {
 		super();
 	}
 	
 	/**
 	 * Read the specified file and return the loaded {@link SubtitleFile}.
 	 * @param file
 	 *        the file to read.
 	 * @return the subtitle file, loaded.
 	 * @throws IOException
 	 *         if there was a problem while reading the file.
 	 */
 	public SubtitleFile readFile (final Path file) throws IOException {
		if (!Files.exists(file) || !Files.isReadable(file)) {
 			lg.warning("File " + file + " does not exists or cannot be read");
			throw new IllegalArgumentException("The file does not exist or cannot be read");
 		}
 		if (lg.isLoggable(Level.INFO)) {
 			lg.fine("Loading file " + file);
 		}
 		
 		SubtitleFile subtitleFile = null;
 		
 		// TODO check for char set
 		try (final BufferedReader reader = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
 			try {
 				subtitleFile = readHeader(file, reader);
 				for (;;) {
 					subtitleFile.add(readSubtitle(subtitleFile, reader));
 				}
 			} catch (final EOFException e) {
 				if (lg.isLoggable(Level.INFO)) {
 					lg.info("Finished reading file " + file);
 				}
 				readFooter(subtitleFile, reader);
 			}
 		} catch (final IOException e) {
 			lg.warning("Problem while reading subitle file: " + ExceptionUtils.display(e));
 			throw e;
 		}
 		
 		return subtitleFile;
 	}
 	
 	/**
 	 * Read the header of the subtitle file and build the {@link SubtitleFile} to hold the data.<br />
 	 * May be override by specific subtitle implementations.
 	 * @param file
 	 *        the file being read.
 	 * @param reader
 	 *        the reader to use.
 	 * @return the subtitle file to use to store the data.
 	 * @throws IOException
 	 *         if there was a problem while reading the file.
 	 */
 	protected SubtitleFile readHeader (final Path file, final BufferedReader reader) throws IOException {
 		return new SubtitleFile(file);
 	}
 	
 	/**
 	 * Read the footer of the subtitle file.<br />
 	 * May be override by specific implementations.
 	 * @param subtitleFile
 	 *        the subtitle file being read.
 	 * @param reader
 	 *        the reader to use.
 	 * @throws IOException
 	 *         if there was a problem while reading the file.
 	 */
 	protected void readFooter (final SubtitleFile subtitleFile, final BufferedReader reader) throws IOException {
 		// Do nothing
 	}
 	
 	/**
 	 * Read a single subtitle of the file.
 	 * @param subtitleFile
 	 *        the subtitle file being read.
 	 * @param reader
 	 *        the reader to use.
 	 * @return The subtitle read.
 	 * @throws IOException
 	 *         if there was a problem while reading the file.
 	 */
 	protected abstract Subtitle readSubtitle (final SubtitleFile subtitleFile, final BufferedReader reader) throws IOException;
 }
