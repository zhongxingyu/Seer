 package lanplayer;
 
 import java.io.File;
 import java.net.MalformedURLException;
 import java.util.Date;
 
 import javax.sound.sampled.UnsupportedAudioFileException;
 
 import utilities.SimpleDate;
 
 public class MusicData implements Comparable<MusicData> {
 	
 	private MusicInfo musicInfo;
 	private File musicFile;
 
 	private String ip = "";
 	private String title = null;
 	private String artist = null;
 	private String album = null;
 	private TrackNumber trackno = null;
 	private String duration = null;
 	private Date date;
 	private int rating = 0;
 	private int skip = 0;
 	private int played = 0;
 	private int position = -1;
 	
 	public int getPosition() {
 		return position;
 	}
 	
 	public File getMusicFile() {
 		return musicFile;
 	}
 
 	public String getIp() {
 		return ip;
 	}
 
 	public int getRating() {
 		return rating;
 	}
 
 	public int getSkip() {
 		return skip;
 	}
 
 	public int getPlayed() {
 		return played;
 	}
 
 	public String getTitle() {
 		if(title != null && !title.isEmpty()) return title;
 		title = musicInfo == null ? "" : musicInfo.getTitle();
 		return title;
 	}
 
 	public String getAlbum() {
 		if(album != null && !album.isEmpty()) return album;
 		album = musicInfo == null ? "" : musicInfo.getAlbum();
 		return album;
 	}
 
 	public String getArtist() {
 		if(artist != null && !artist.isEmpty()) return artist;
 		artist = musicInfo == null ? "" : musicInfo.getArtist();
 		return artist;
 	}
 	
 	public String getDuraction() {
 		if(duration != null && !duration.isEmpty()) return duration;
 		duration = musicInfo == null ? "" : musicInfo.getDuration();
 		return duration;
 		
 	}
 	
 	public TrackNumber getTrackNumber() {
 		if(trackno != null && album != null) return trackno;
 		trackno = musicInfo == null ? new TrackNumber(0,null) : musicInfo.getTrackNumber();
 		return trackno;
 	}
 	
 	public SimpleDate getSimpleDate() {
 		return new SimpleDate(this.date);
 	}
 	
 	public MusicData() {
 	}
 	
 	/**
 	 * MUSICDATA
 	 * @param position
 	 * @param musicFile
 	 * @param title
 	 * @param artist
 	 * @param album
 	 * @param trackno
 	 * @param duration
 	 * @param played
 	 * @param rating
 	 * @param skip
 	 * @param date
 	 * @param ip
 	 * @throws MalformedURLException
 	 * @throws UnsupportedAudioFileException
 	 */
 	public MusicData(int position, File musicFile, String title, String artist, String album, String trackno, String duration, int played, int rating, int skip, Date date, String ip) throws MalformedURLException, UnsupportedAudioFileException {
 		this.position = position;
 		this.musicFile = musicFile;
 		this.ip = ip;
 		this.rating = rating;
 		this.skip = skip;
 		this.played = played;
 		this.date = date;
 		
 		this.title = title;
 		this.artist = artist;
 		this.album = album;
 		this.duration = duration;
 		
 		try {
 			int tryTrackNo = Integer.parseInt(trackno);
 			this.trackno = new TrackNumber(tryTrackNo, album);
 		}
 		catch(NumberFormatException nfe) {
 		}
 				
		if(title == null || artist == null || album == null || trackno == null || duration == null
				|| title.isEmpty() || artist.isEmpty() || album.isEmpty() || trackno.isEmpty() || duration.isEmpty()) {
 			String extension = musicFile.getName().substring(musicFile.getName().lastIndexOf("."), musicFile.getName().length());
 			if(extension.equals(".mp3")) {
 				musicInfo = new MP3Info(musicFile);
 			}
 			else if(extension.equals(".xm")) {
 				musicInfo = new ModInfo(musicFile);
 			}
 		}
 
 	}
 	
 	public String toString() {
 		return position + "";
 	}
 
 	@Override
 	public int compareTo(MusicData other) {
 		if(other == null) return 1;
 		return new Integer(this.getPosition()).compareTo(new Integer(other.getPosition()));
 	}
 
 	
 }
