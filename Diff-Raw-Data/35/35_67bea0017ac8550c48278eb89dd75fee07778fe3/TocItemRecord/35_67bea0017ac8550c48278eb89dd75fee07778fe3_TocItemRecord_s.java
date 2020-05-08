 /**
  * 
  */
 package org.opf_labs.arc_cd.cdrdao.toc;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileReader;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.List;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import org.apache.commons.io.IOUtils;
 import org.opf_labs.arc_cd.cdrdao.toc.AudioTrack.Builder;
 
 /**
  * @author carl
  * 
  */
 public final class TocItemRecord {
 	private static final Pattern TRACK_START_COMMENT = Pattern
 			.compile("^// Track ([0-9]+)");
 	private static final Pattern ISRC_PATTERN = Pattern
 			.compile("^ISRC \"(.*)\"$");
 	private static final Pattern FILE_PATTERN = Pattern
 			.compile("^FILE \"(.*)\" ([0-9:]+) ([0-9:]+)$");
 	private static final TocItemRecord DEFAULT = new TocItemRecord();
 	private final List<AudioTrack> tracks = new ArrayList<>();
 
 	private TocItemRecord() {
 	}
 
 	private TocItemRecord(List<AudioTrack> tracks) {
 		this.tracks.addAll(tracks);
 	}
 
 	/**
 	 * @return the list of track timings
 	 */
 	public List<AudioTrack> getTracks() {
 		return Collections.unmodifiableList(this.tracks);
 	}
 
 	/**
 	 * @param track
 	 *            the track to add
 	 */
 	public void addTrack(AudioTrack track) {
 		this.tracks.add(track);
 	}
 
 	@Override
 	public String toString() {
 		StringBuilder value = new StringBuilder();
 		for (AudioTrack track : this.tracks) {
 			value.append(track.toString());
 		}
 		return value.toString();
 	}
 
 	/**
 	 * @return the default instance
 	 */
 	public static TocItemRecord defaultInstance() {
 		return DEFAULT;
 	}
 
 	/**
 	 * @param tocFile
 	 *            the table of contents file
 	 * @return a TocItemRecord instance created from the file
 	 * @throws IOException
 	 *             when there's a problem reading the file
 	 */
 	public static TocItemRecord fromTocFile(File tocFile) throws IOException {
 		try (BufferedReader reader = new BufferedReader(new FileReader(tocFile))) {
 			TocItemRecord record = fromBufferedReader(reader);
 			IOUtils.closeQuietly(reader);
 			return record;
 		}
 	}
 
 	/**
 	 * @param stream
 	 *            java.io.InputStream with TOC content
 	 * @return the TocItemRecord instance parsed from the stream contents
 	 * @throws IOException
 	 *             if the stream can't be read
 	 */
 	public static TocItemRecord fromInputStream(InputStream stream)
 			throws IOException {
 		try (BufferedReader reader = new BufferedReader(new InputStreamReader(
 				stream))) {
 			TocItemRecord record = fromBufferedReader(reader);
 			IOUtils.closeQuietly(reader);
 			return record;
 		}
 	}
 
 	private static TocItemRecord fromBufferedReader(BufferedReader reader)
 			throws IOException {
 		List<AudioTrack> tracks = new ArrayList<>();
 		String line;
 		while ((line = reader.readLine()) != null) {
 			System.err.println(line);
 			Matcher trackStartMatcher = TRACK_START_COMMENT.matcher(line);
 			if (trackStartMatcher.matches())
 				tracks.add(parseTrack(trackStartMatcher.group(1), reader));
 		}
 		return new TocItemRecord(tracks);
 	}
 
 	private static AudioTrack parseTrack(String numberMatch,
 			BufferedReader reader) throws IOException {
 		int number = Integer.parseInt(numberMatch);
 		Builder builder = new Builder(number);
 		String line;
 		while (((line = reader.readLine()) != null) && !(line.isEmpty())) {
 			Matcher isrcMatcher = ISRC_PATTERN.matcher(line);
 			if (isrcMatcher.matches()) {
 				builder.isrc(isrcMatcher.group(1));
 			}
 			Matcher fileMatcher = FILE_PATTERN.matcher(line);
 			if (fileMatcher.matches()) {
 				builder.file(fileMatcher.group(1));
 				builder.start(fileMatcher.group(2));
 				builder.length(fileMatcher.group(3));
 			}
 		}
 		return builder.build();
 	}
 }
