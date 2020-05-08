 package com.alexrnl.subtitlecorrector.io;
 
 import java.nio.file.Path;
 import java.util.Collections;
 import java.util.HashSet;
 import java.util.Objects;
 import java.util.Set;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import com.alexrnl.commons.io.IOUtils;
 import com.alexrnl.commons.utils.StringUtils;
 import com.alexrnl.subtitlecorrector.common.SubtitleFile;
 
 /**
  * Manager to ease the use of the {@link SubtitleFile} reading / writing.<br />
  * @author Alex
  */
 public class SubtitleFormatManager {
 	/** Logger */
 	private static Logger				lg	= Logger.getLogger(SubtitleFormatManager.class.getName());
 	
 	/** The registered format of subtitles */
 	private final Set<SubtitleFormat>	formats;
 	
 	/**
 	 * Constructor #1.<br />
 	 * Default constructor.
 	 */
 	public SubtitleFormatManager () {
 		super();
 		formats = new HashSet<>();
 	}
 	
 	/**
 	 * Register a new {@link SubtitleFormat}.
 	 * @param format
 	 *        the format to add.
 	 * @return <code>true</code> if a previous same format existed (and is thus override).
 	 */
 	public boolean registerFormat (final SubtitleFormat format) {
		return formats.add(Objects.requireNonNull(format));
 	}
 	
 	/**
 	 * Return the available {@link SubtitleFormat}.<br />
 	 * The set returned is not modifiable to avoid side effects.
 	 * @return the subtitle format available.
 	 */
 	public Set<SubtitleFormat> getAvailableFormats () {
 		return Collections.unmodifiableSet(formats);
 	}
 	
 	/**
 	 * Find a subtitle format by its name.
 	 * @param name
 	 *        the name of the format to find.
 	 * @return the matching format or <code>null</code> if no matching format were found.
 	 */
 	public SubtitleFormat getFormatByName (final String name) {
 		if (StringUtils.nullOrEmpty(name)) {
 			throw new IllegalArgumentException("Cannot search for null or empty name");
 		}
 		
 		for (final SubtitleFormat format : formats) {
 			if (format.getName().equals(name)) {
 				return format;
 			}
 		}
 		
 		if (lg.isLoggable(Level.INFO)) {
 			lg.info("No matching format found for name " + name);
 		}
 		return null;
 	}
 	
 	/**
 	 * Find the {@link SubtitleFormat} which support the specified extension.
 	 * @param extension
 	 *        the extension of the file.
 	 * @return the format which support the following extension.
 	 */
 	public Set<SubtitleFormat> getFormatByExtension (final String extension) {
 		if (StringUtils.nullOrEmpty(extension)) {
 			throw new IllegalArgumentException("Cannot search for null or empty extension");
 		}
 		
 		final Set<SubtitleFormat> candidates = new HashSet<>();
 		for (final SubtitleFormat format : formats) {
 			if (format.getExtensions().contains(extension)) {
 				candidates.add(format);
 			}
 		}
 		
 		if (lg.isLoggable(Level.INFO)) {
 			lg.info("Subtitle format matching extension " + extension + " are: " + candidates);
 		}
 		return candidates;
 	}
 	
 	/**
 	 * Find the {@link SubtitleFormat} which support the following {@link Path} according to its
 	 * extension.
 	 * @param path
 	 *        the path of the file.
 	 * @return the format(s) which support the following path.
 	 */
 	public Set<SubtitleFormat> getFormatByPath (final Path path) {
 		return getFormatByExtension(IOUtils.getFileExtension(path));
 	}
 }
