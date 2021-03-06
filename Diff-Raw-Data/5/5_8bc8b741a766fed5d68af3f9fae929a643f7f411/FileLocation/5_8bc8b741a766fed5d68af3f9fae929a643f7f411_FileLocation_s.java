 package org.xbmc.api.object;
 
 import java.net.URLDecoder;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import org.xbmc.api.type.MediaType;
 import org.xbmc.httpapi.Connection;
 
 public class FileLocation implements INamedResource {
 	
 	public String name, path, displayPath;
 	public boolean isDirectory;
 	public boolean isArchive = false;
 	public boolean isMultipath = false;
 	public int mediaType = 0;
 	
 	/**
 	 * Class constructor with already parsed data
 	 * @param name Display name
 	 * @param path Path incl filename
 	 */
 	public FileLocation(String name, String path) {
 		this.name = name;
 		this.path = path;
 		isDirectory = path.endsWith("/") || path.endsWith("\\");
 		isArchive = isDirectory && path.startsWith("rar://") || path.startsWith("zip://");
 		isMultipath = path.startsWith("multipath://");
 		
 		if (path.contains("://")) {
 			displayPath = name + "/";
 //			displayPath = URLDecoder.decode(path).replaceAll("\\\\", "/");
 		} else {
 			displayPath = path.replaceAll("\\\\", "/");
 		}
 		
 		setMediaType();
 	}
 	
 	public String getShortName(){
 		return this.name;
 	}
 	
 	/**
 	 * Parses name and path from raw line.
 	 * @param line raw line, either path only or name and path, separated by Connection.VALUE_SEP.
 	 */
 	public FileLocation(String line) {
 		if (line.endsWith(".m3u\\") || line.endsWith(".m3u/")) {
 			line = line.substring(0, line.length() - 1);
 		}
 		if (line.contains(Connection.VALUE_SEP)) {
 			final String[] s = line.split(Connection.VALUE_SEP);
 			name = s[0];
 			path = s[1];
 			isDirectory = path.endsWith("/") || path.endsWith("\\");
 		} else {
 			String trimmed = line.replaceAll("\\\\", "/"); // path without trailing "/"
 			isDirectory = trimmed.endsWith("/");
 			path = line;
 			if (isDirectory) {
 				trimmed = trimmed.substring(0, trimmed.lastIndexOf("/")); 
 			}
 			name = trimmed.substring(trimmed.lastIndexOf("/") + 1);
 		}
 		// treat archives specially
 		if (path.startsWith("rar://") || path.startsWith("zip://")) {
 			final String decoded;
 			if (isDirectory) {
 				decoded = URLDecoder.decode(path.substring(0, path.length() - 1)).replaceAll("\\\\", "/");
 				isArchive = true;
 			} else {
 				decoded = URLDecoder.decode(path).replaceAll("\\\\", "/");
 				isArchive = false;
 			}
 			name = decoded.substring(decoded.lastIndexOf("/") + 1);
 			displayPath = URLDecoder.decode(path).replaceAll("\\\\", "/");
 
 		} else if (path.startsWith("shout://")) {
 			Pattern pattern = Pattern.compile(".*shoutcast\\.com[^\\?]+\\?genre=([^\\\\]+).*", Pattern.CASE_INSENSITIVE);
 			Matcher matcher = pattern.matcher(path);
 			if (matcher.matches()) {
 				name = matcher.group(1);
 				path = path.substring(0, path.length() - 1);
 			} else {
 				pattern = Pattern.compile(".*shoutcast\\.com.*tunein-station\\.pls\\?id=([0-9]+)", Pattern.CASE_INSENSITIVE);
 				matcher = pattern.matcher(path);
 				if (matcher.matches()) {
 					name = "Station #" + matcher.group(1);
 					isDirectory = false;
 					mediaType = MediaType.MUSIC;
 				}
 			}
 			
 		} else if (path.contains("://")) {
 			displayPath = name + "/";
 //			displayPath = URLDecoder.decode(path).replaceAll("\\\\", "/");
 			isMultipath = true;
 			
 		} else {
 			displayPath = path.replaceAll("\\\\", "/");
 		}
 		setMediaType();
 	}
 	
 	private void setMediaType() {
 		final String ext = name.substring(name.lastIndexOf(".") + 1).toLowerCase();
 		if (ext.equals("mp3") || ext.equals("ogg") || ext.equals("flac") || ext.equals("m4a")) {
 			this.mediaType = MediaType.MUSIC;
 		} else if (ext.equals("avi") || ext.equals("mov") || ext.equals("flv") || ext.equals("mkv") || ext.equals("wmv") || ext.equals("mp4")) {
 			this.mediaType = MediaType.VIDEO;
 		} else if (ext.equals("jpg") || ext.equals("jpeg") || ext.equals("bmp") || ext.equals("gif") || ext.equals("png") || ext.equals("tbn")) {
 			this.mediaType = MediaType.PICTURES;
 		} 
 	}
 }
