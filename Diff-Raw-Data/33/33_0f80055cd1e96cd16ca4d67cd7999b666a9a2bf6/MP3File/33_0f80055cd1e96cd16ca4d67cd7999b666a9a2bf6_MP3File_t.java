 /*
  * Copyright 2008 Charles Perry
  *
  * This file is part of Harmonium, the TiVo music player.
  *
  * Harmonium is free software: you can redistribute it and/or modify
  * it under the terms of the GNU Affero General Public License as 
  * published by the Free Software Foundation, either version 3 of the
  * License, or (at your option) any later version.
  *
  * Harmonium is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU Affero General Public License for more details.
  *
  * You should have received a copy of the GNU Affero General Public
  * License along with Harmonium.  If not, see <http://www.gnu.org/licenses/>.
  *
  */
  
 package org.dazeend.harmonium.music;
 
 import java.awt.Image;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import javax.swing.ImageIcon;
 
 import org.blinkenlights.jid3.ID3Exception;
 import org.blinkenlights.jid3.v1.ID3V1Tag;
 import org.blinkenlights.jid3.v1.ID3V1_1Tag;
 import org.blinkenlights.jid3.v2.APICID3V2Frame;
 import org.blinkenlights.jid3.v2.ID3V2_3_0Tag;
 import org.blinkenlights.jid3.v2.POPMID3V2Frame;
 import org.dazeend.harmonium.Harmonium;
 import org.dazeend.harmonium.screens.NowPlayingScreen;
 
 import com.tivo.hme.sdk.util.Mp3Helper;
 
 
 /**
  * This class represents and manages playback of MP3 files.
  * 
  * @author Charles Perry (harmonium@DazeEnd.org)
  *
  */
 public class MP3File extends HMusic implements Playable {
 
 	private String 			albumArtistName = "";	
 	private String			albumName = "";			
 	private String			artistName = "";
 	private String			trackName = "";
 	private File			trackFile;
 	private String			trackPath = "";
 	private String			trackNameTitleSortForm = "";
 	private int				trackNumber;
 	private int				releaseYear;
 	private int				discNumber;			
 	private RatingLevel		rating = RatingLevel.UNRATED;
 	private long			duration = -1;
 	
 	private Boolean			albumImageFetched = false;
 	private Boolean			hasAlbumArt = false;
 
 	/**
 	 * Constructor scans MP3 file for ID3 tags to get track information.
 	 * 
 	 * @param path		a String that represents the path to the track relative to the music root.
 	 * @param file		a <code>File</code> that represents the track on disk.
 	 */
 	public MP3File(String path, File file) throws ID3Exception {
 
 		this.trackFile = file;
 		this.trackPath = path;
 		
 		// Create an org.blinkenlights.jid3.MP3File to read tag data from
 		org.blinkenlights.jid3.MP3File mp3File = new org.blinkenlights.jid3.MP3File(file);
 		
 		// Get any ID3v2.3 tag that exists in the mp3 file and load its data
 		ID3V2_3_0Tag v23Tag = (ID3V2_3_0Tag) mp3File.getID3V2Tag();
 		if(v23Tag != null) {
 
 			// An ID3v2.3 tag exists, so pull text fields from tags
 			if( (v23Tag.getTALBTextInformationFrame() != null) && (v23Tag.getTALBTextInformationFrame().getAlbum() != null ) ){
 				this.albumName = removeDiscLabel(v23Tag.getTALBTextInformationFrame().getAlbum().trim());
 			}
 			
 			if( (v23Tag.getTIT2TextInformationFrame() != null) && (v23Tag.getTIT2TextInformationFrame().getTitle() != null ) ) {
 				this.trackName = v23Tag.getTIT2TextInformationFrame().getTitle().trim();
 			}
 			
 			if( v23Tag.getTRCKTextInformationFrame() != null ) {
 				this.trackNumber = v23Tag.getTRCKTextInformationFrame().getTrackNumber();
 			}
 			
 			if( (v23Tag.getTPOSTextInformationFrame() != null) ) {
 				
 				// Do not set the tag if there is only 1 totalPart
 				if(v23Tag.getTPOSTextInformationFrame().getTotalParts() != 1) {
 					this.discNumber = v23Tag.getTPOSTextInformationFrame().getPartNumber();
 				}
 			}
 			
 			if(v23Tag.getTYERTextInformationFrame() != null) {
 				this.releaseYear = v23Tag.getTYERTextInformationFrame().getYear();
 			}
 			
 			// Create title formatted string for track. 
 			if(! this.trackName.equals("")) {
 				// The track has a name, so re-format it
 				// Compile pattern for matching leading articles
 				Pattern titlePattern = Pattern.compile("(?i)^(the|a|an)\\s");
 				Matcher stringMatcher = titlePattern.matcher(this.trackName);
 				if(stringMatcher.lookingAt()) {
 					// Found a leading article. Move it to the end of the string.
 					this.trackNameTitleSortForm = stringMatcher.replaceFirst("") + ", " + stringMatcher.group(1);
 				}
 				else {
 					this.trackNameTitleSortForm = this.trackName;
 				}
 			}
 			
 			// Artists are returned as an array of artists. Concatenate them into one string, if the frame exists.
 			if(v23Tag.getTPE1TextInformationFrame() != null) {
 				String[] tempArtistNameArray = v23Tag.getTPE1TextInformationFrame().getLeadPerformers();
 				if(tempArtistNameArray.length > 0) {
 					StringBuilder tempArtistNameBuilder = new StringBuilder(tempArtistNameArray[0].trim());
 					for(int i = 1; i < tempArtistNameArray.length; ++i) {
 						tempArtistNameBuilder.append('/').append(tempArtistNameArray[i].trim());
 					}
 					this.artistName = tempArtistNameBuilder.toString();
 				}
 			}
 			
 			// AlbumArtist is a non-standard ID3 tag, and TPE2 isn't really intended for it. So if AlbumArtist is
 			// blank, copy the track artist into the album artist.
 			if( (v23Tag.getTPE2TextInformationFrame() != null) && ( ! v23Tag.getTPE2TextInformationFrame().getBandOrchestraAccompaniment().trim().equals("") ) ) {
 				this.albumArtistName = v23Tag.getTPE2TextInformationFrame().getBandOrchestraAccompaniment().trim();
 			}
 			if(this.albumArtistName.equals("")) {
 				this.albumArtistName = this.artistName;
 			}
 			
 			// Get any popularimeter (POPM) frames in the tag
 			POPMID3V2Frame[] POPMFrames = v23Tag.getPOPMFrames();
 			
 			// Iterate through the POPM frams to see if any of them belong to us.
 			for(POPMID3V2Frame frame : POPMFrames) {
 				
 				// Get email address of user
 				// TODO DEFERRED: implement for ratings: get email address from Preferences object
 				String myEmail = "harmonium@DazeEnd.org";
 				
 				// Check if the frame belongs to us.
 				if( myEmail.equals( frame.getEmailToUser() ) ) {
 						
 					// This is our tag, so get the rating and save it
 					switch(frame.getPopularity()) {
 					
 					case 1:
 						this.rating = RatingLevel.LEVEL_1;
 						break;
 						
 					case 2:
 						this.rating = RatingLevel.LEVEL_2;
 						break;
 						
 					case 3:
 						this.rating = RatingLevel.LEVEL_3;
 						break;
 						
 					case 4:
 						this.rating = RatingLevel.LEVEL_4;
 						break;
 						
 					case 5:
 						this.rating = RatingLevel.LEVEL_5;
 						break;
 						
 					default:
 						this.rating = RatingLevel.UNRATED;
 					}
 					
 					// We've already found our rating, so stop looking by dropping out of the for-loop
 					break;
 				}
 			}
 		}
 	
 		// We've grabbed all the ID3v2.3 data. Now fill in any blanks with data from ID3v1.x tags.
 		// Note that ID3v1.x tags are much more limited than v2.3, so we won't be able to get all the data.
 		ID3V1Tag v1Tag = mp3File.getID3V1Tag();
 		
 		if( (this.artistName.equals("")) && (v1Tag != null) && (v1Tag.getArtist() != null ) ) {
 			this.artistName = v1Tag.getArtist().trim();
 		}
 		
 		if(this.albumArtistName.equals("")) {
 			// There's no way to get album artist from v1.x, so fake it.
 			this.albumArtistName = this.artistName;  
 		}
 		
 		if( (this.albumName.equals("")) && (v1Tag != null) && (v1Tag.getAlbum() != null ) ) {
 			this.albumName = removeDiscLabel(v1Tag.getAlbum().trim());
 		}
 		
 		if( (this.trackName.equals("")) && (v1Tag != null) && (v1Tag.getTitle() != null ) ) {
 			this.trackName = v1Tag.getTitle();
 		}
 		
 		if(this.releaseYear == 0 && (v1Tag != null)) {
 			try{
 				this.releaseYear = Integer.parseInt(v1Tag.getYear());
 			}
 			catch(NumberFormatException e) {
 				// If we get a bad number from the tag, just skip it.
 			}
 		}
 		
 		if((this.trackNumber == 0) && (v1Tag != null) && (v1Tag instanceof ID3V1_1Tag)) {
 			try {
 				this.trackNumber = ((ID3V1_1Tag) v1Tag).getAlbumTrack();
 			}
 			catch(ClassCastException e) {
 				// If there's a problem with the cast, just skip this field.
 			}
 		}
 		
 		// Find the length of this track in milliseconds
 		try {
 			InputStream in = new FileInputStream(file);
 	        try {
 	            Mp3Helper mp3Helper = new Mp3Helper(in, file.length());
 	            this.duration = mp3Helper.getMp3Duration();
 	        } finally {
 	        	try {
 	        		in.close();
 	        	}
 	        	catch(IOException e) {
 	        	}
 	        }
 		}
 		catch(FileNotFoundException e) {
 		}
 	}
 	
 	private static final Pattern discPattern = Pattern.compile("(?i)(.*)(\\s+[\\(\\[\\{]?Disc\\s[0-9]+[\\)\\]\\}]?)\\z");
 	private static final Pattern cdPattern = Pattern.compile("(?i)(.*)(\\s+[\\(\\[\\{]?CD\\s[0-9]+[\\)\\]\\}]?)\\z");
 	private static String removeDiscLabel(String originalName)
 	{
 		Matcher m = discPattern.matcher(originalName);
 		if (m.lookingAt())
 			return m.group(1);
 		else {
 			m = cdPattern.matcher(originalName);
 			if (m.lookingAt())
 				return m.group(1);
 			else
 				return originalName;
 		}
 	}
 	
 	/**
 	 * Constructor takes track information as parameters and does not scan file for ID3 tags.
 	 * 
 	 * @param path		a String that represents the path to the track relative to the music root.
 	 * @param file		a <code>File</code> that represents the track on disk.
 	 */
 	public MP3File(	String	path, 			// path to file relative to music root
 					File	file, 			// file object representing track on disk
 					String	albumArtist,
 					String	albumName, 
 					int		yearNum, 		// Release year
 					int		discNum, 		// disc number for multi-part albums. (Use zero for no disc number.)
 					int		trackNum, 
 					String	trackName, 
 					String	artistName,
 					long	duration
 	) {
 		this.trackFile = file;
 		this.trackPath = path;
 		
 		this.albumArtistName = albumArtist;
 		this.albumName = albumName;
 		this.releaseYear = yearNum;
 		this.discNumber = discNum;
 		this.trackNumber = trackNum;
 		this.trackName = trackName;
 		this.artistName = artistName;
 		this.duration = duration;
 		
 		// Create title formatted string for track. 
 		if(! this.trackName.equals("")) {
 			// The track has a name, so re-format it
 			// Compile pattern for matching leading articles
 			Matcher stringMatcher = titlePattern.matcher(this.trackName);
 			if(stringMatcher.lookingAt()) {
 				// Found a leading article. Move it to the end of the string.
 				this.trackNameTitleSortForm = stringMatcher.replaceFirst("") + ", " + stringMatcher.group(1);
 			}
 			else {
 				this.trackNameTitleSortForm = this.trackName;
 			}
 		}
 		
 	}
 
 	/* (non-Javadoc)
 	 * @see org.dazeend.harmonium.music.Playable#getDuration()
 	 */
 	public long getDuration() {
 		return this.duration;
 	}
 
 	/**
 	 * Checks existance of album art for this object.
 	 */
 	//@Override
 	public boolean hasAlbumArt() {
 		
 		if ( albumImageFetched )
 			return hasAlbumArt;
 		
 		return (this.getAlbumArt() != null);
 	}
 	
 	/* (non-Javadoc)
 	 * @see org.dazeend.harmonium.Playable#getAlbumArt()
 	 */
 	//@Override
 	public synchronized Image getAlbumArt() {
 		
 		Image img = null;
 		
 		try {
 			// Create an org.blinkenlights.jid3.MP3File to read tag data from
 			org.blinkenlights.jid3.MP3File mp3File = new org.blinkenlights.jid3.MP3File(this.trackFile);
 
 			// Get any ID3v2.3 tag that exists in the mp3 file and load its data
 			ID3V2_3_0Tag v23Tag;
 			try {
 				v23Tag = (ID3V2_3_0Tag) mp3File.getID3V2Tag();
 			}
 			catch(ID3Exception e) {
 				// There was an error reading the ID3 info, so just return null
 				return null;
 			}
 			
 			if(v23Tag != null) {
 				
 				// Get any attached picture (APIC) frames in this tag.
 				APICID3V2Frame[] APICFrames = v23Tag.getAPICFrames();
 
 				if(APICFrames.length > 0) {
 					// APIC frames categorize each attached picture with a "picture type". If a picture with the type
 					// of "front cover" exists, use that picture -- it should be the album art. If there's no picture
 					// with a type of "front cover" just use the first picture in the array.
 					for(APICID3V2Frame frame : APICFrames) {
 						if( frame.getPictureType().equals(APICID3V2Frame.PictureType.FrontCover) ) {
 							// Make sure that the MIME type for this cover art can be understood by TiVo
 							String tempMimeType = frame.getMimeType();
 							for(TivoImageFormat format : TivoImageFormat.values()) {
 								if(format.getMimeType().equals(tempMimeType)) {
 									img = new ImageIcon(frame.getPictureData()).getImage();
 									break;
 								}
 							}
 							// We found the cover art, so stop looking for it.
 							break;
 						}
 					}
 					
 					// If we didn't find an attached picture that claims to be of the front cover, just grab
 					// the first valid image.
 					if(img == null) {
 						for(APICID3V2Frame frame : APICFrames) {
 							// Make sure that the MIME type for this album art can be understood by TiVo
 							String tempMimeType = frame.getMimeType();
 							for(TivoImageFormat format : TivoImageFormat.values()) {
 								if(format.getMimeType().equals(tempMimeType)) {
 									img = new ImageIcon(frame.getPictureData()).getImage();
									if (img.getWidth(null) < 1 || img.getHeight(null) < 1)
										img = null;
									else
										break;
 								}
 							}
							if (img != null)
								break;
 						}
 					}
 				}
 			}
 			hasAlbumArt = (img != null);
 		} catch (Exception e) {
 			e.printStackTrace();
 		} finally {
 			albumImageFetched = true;
 		}
 		
 		return img;
 	}
 
 	/* (non-Javadoc)
 	 * @see org.dazeend.harmonium.Playable#getAlbumArtistName()
 	 */
 	//@Override
 	public String getAlbumArtistName() {
 		return this.albumArtistName;
 	}
 
 	/* (non-Javadoc)
 	 * @see org.dazeend.harmonium.Playable#getAlbumName()
 	 */
 	//@Override
 	public String getAlbumName() {
 		return this.albumName;
 	}
 
 	/* (non-Javadoc)
 	 * @see org.dazeend.harmonium.Playable#getArtistName()
 	 */
 	//@Override
 	public String getArtistName() {
 		return this.artistName;
 	}
 
 	/* (non-Javadoc)
 	 * @see org.dazeend.harmonium.Playable#getDiscNumber()
 	 */
 	//@Override
 	public int getDiscNumber() {
 		return this.discNumber;
 	}
 
 	/* (non-Javadoc)
 	 * @see org.dazeend.harmonium.Playable#getRating()
 	 */
 	//@Override
 	public RatingLevel getRating() {
 		return this.rating;
 	}
 
 	/* (non-Javadoc)
 	 * @see org.dazeend.harmonium.Playable#getReleaseYear()
 	 */
 	//@Override
 	public int getReleaseYear() {
 		return this.releaseYear;
 	}
 	
 	/* (non-Javadoc
 	 * 
 	 * Gets an Image containing scaled album art for this track.
 	 *
 	 */
 	//@Override
 	public Image getScaledAlbumArt(int width, int height) {
 		if(this.hasAlbumArt()) {
 	        Image img = this.getAlbumArt();
 	        ImageIcon icon = new ImageIcon(img);
 	        int imgW = icon.getIconWidth();
 	        int imgH = icon.getIconHeight();
 	        
 	        // figure out the scale factor and the new size
 	        float scale = Math.min((float) width / imgW, (float) height / imgH);
 	        if (scale > 1.0f) {
 	            scale = 1.0f;
 	        }
 	        int scaleW = (int)(imgW * scale);
 	        int scaleH = (int)(imgH * scale);
 	        
 	        // Perform scaling if the image must be shrunk. We will send smaller
 	        // images over and let the receiver scale them up.
 	        
 	        if (scale < 1.0f) {
 	            img = img.getScaledInstance(scaleW, scaleH, Image.SCALE_FAST);
 //	            try {
 //	            	// the media tracker reqires a component, so we use this button
 //	                Button mediaTrackerComp = new Button();
 //	                
 //	                // use MediaTracker to wait for image to be completely scaled. (I think? TiVo code.)
 //	                MediaTracker mt = new MediaTracker(mediaTrackerComp);
 //	                mt.addImage(img, 0);
 //	                mt.waitForAll();
 //	            } catch(InterruptedException e) {
 //	            }
 	        }
 	        	        
 	        return img;
 		}
 		return null;
 	}
 
 	/**
 	 * @return the trackPath
 	 */
 	//@Override
 	public String getPath() {
 		return this.trackPath;
 	}
 	
 	/**
 	 * @return the filename
 	 */
 	//@Override
 	public String getFilename() {
 		return this.trackFile.getName();
 	}
 	
 	/* (non-Javadoc)
 	 * @see org.dazeend.harmonium.Playable#getTrackName()
 	 */
 	//@Override
 	public String getTrackName() {
 		return this.trackName;
 	}
 
 	
 	/* (non-Javadoc)
 	 * @see org.dazeend.harmonium.Playable#getTrackNameTitleSortForm()
 	 */
 	//@Override
 	public String getTrackNameTitleSortForm() {
 		return this.trackNameTitleSortForm;
 	}
 
 	/* (non-Javadoc)
 	 * @see org.dazeend.harmonium.Playable#getTrackNumber()
 	 */
 	//@Override
 	public int getTrackNumber() {
 		return this.trackNumber;
 	}
 
 	/* (non-Javadoc)
 	 * @see org.dazeend.harmonium.Playable#play()
 	 */
 	//@Override
 	public boolean play(NowPlayingScreen nowPlayingScreen) {
 		if( nowPlayingScreen.playMP3(this) ) {
 			return true;
 		}
 		else {
 			return false;
 		}
 	}
 	
 	
 	/* (non-Javadoc)
 	 * @see org.dazeend.harmonium.music.Playable#stop()
 	 */
 	//@Override
 	public boolean stop(NowPlayingScreen nowPlayingScreen) {
 		nowPlayingScreen.stopMP3();
 		return true;
 	}
 
 	/* (non-Javadoc)
 	 * @see org.dazeend.harmonium.Playable#pause()
 	 */
 	//@Override
 	public boolean pause(NowPlayingScreen nowPlayingScreen) {
 		if( nowPlayingScreen.getMusicStream() != null ) {
 			if(! nowPlayingScreen.getMusicStream().isPaused() ) {
 				nowPlayingScreen.getMusicStream().pause();
 			}
 			return true;
 		}
 		else {
 			return false;
 		}
 	}
 	
 	/* (non-Javadoc)
 	 * @see org.dazeend.harmonium.Playable#unpause()
 	 */
 	//@Override
 	public boolean unpause(NowPlayingScreen nowPlayingScreen) {
 		if( nowPlayingScreen.getMusicStream() != null ) {
 			if(nowPlayingScreen.getMusicStream().isPaused() ) {
 				nowPlayingScreen.getMusicStream().play();
 			}
 			return true;
 		}
 		else {
 			return false;
 		}
 	}
 	
 	/* (non-Javadoc)
 	 * @see org.dazeend.harmonium.Playable#setPlayRate()
 	 */
 	//@Override
 	public boolean setPlayRate(NowPlayingScreen nowPlayingScreen, float speed) {
 		
 		if(nowPlayingScreen.getMusicStream() == null) {
 			
 		}
 		if(nowPlayingScreen.getMusicStream() != null) {
 			nowPlayingScreen.getMusicStream().setSpeed(speed);
 			return true;
 		}
 		return false;
 	}
 
 	/* (non-Javadoc)
 	 * @see org.dazeend.harmonium.Playable#increaseRating()
 	 */
 	//@Override
 	public synchronized void increaseRating() {
 		// TODO DEFERRED: implement for ratings
 	}
 
 	/* (non-Javadoc)
 	 * @see org.dazeend.harmonium.Playable#decreaseRating()
 	 */
 	//@Override
 	public synchronized void decreaseRating() {
 		// TODO DEFERRED: implement for ratings
 	}
 
 	/* (non-Javadoc)
 	 * @see org.dazeend.harmonium.PlaylistEligible#listMemberTracks()
 	 */
 	//@Override
 	public List<Playable> listMemberTracks(Harmonium app) {
 		// Playable objects are the leaf of the tree and there can be no other	
 		// tracks below it. So return a list with only this object in it.
 		List<Playable> list = new ArrayList<Playable>();
 		list.add(this);
 		return list;
 	}
 
 	/* (non-Javadoc)
 	 * @see org.dazeend.harmonium.Playable#toTitle()
 	 */
 	//@Override
 	public String toStringTitleSortForm() {
 		if((! this.trackNameTitleSortForm.equals("")) && (this.trackNameTitleSortForm != null)) {
 			return this.trackNameTitleSortForm;
 		}
 		else {
 			// The track doesn't have a name, so just return the track's filename
 			return this.trackFile.getName();
 		}
 	}
 	
 	/* (non-Javadoc)
 	 * @see java.lang.Object#toString()
 	 * @see org.dazeend.harmonium.Playable#toString()
 	 */
 	@Override
 	public String toString() {
 		// Prints track name, or failing that the filename.
 		
 		if((! trackName.equals("")) && (this.trackName != null)) {
 			return this.trackName;
 		}
 		else {
 			// The track doesn't have a name, so just return the track's filename
 			return this.trackFile.getName();
 		}
 	}
 
 
 
 	/* (non-Javadoc)
 	 * @see java.lang.Object#hashCode()
 	 */
 	@Override
 	public int hashCode() {
 		final int prime = 31;
 		int result = 1;
 		result = prime * result
 				+ ((trackFile == null) ? 0 : trackFile.hashCode());
 		return result;
 	}
 
 
 
 	/* (non-Javadoc)
 	 * @see java.lang.Object#equals(java.lang.Object)
 	 */
 	@Override
 	public boolean equals(Object obj) {
 		if (this == obj)
 			return true;
 		if (obj == null)
 			return false;
 		if (getClass() != obj.getClass())
 			return false;
 		final MP3File other = (MP3File) obj;
 		if (trackFile == null) {
 			if (other.trackFile != null)
 				return false;
 		} else if (!trackFile.equals(other.trackFile))
 			return false;
 		return true;
 	}
 
 
 
 	/* (non-Javadoc)
 	 * @see org.dazeend.harmonium.music.Playable#getTrackFile()
 	 */
 	public File getTrackFile() {
 		return this.trackFile;
 	}
 
 	public String getDisplayArtistName()
 	{
 		return getArtistName();
 	}
 
 	
 
 	
 }
