 package Media;
 
 import java.io.File;
 import java.nio.charset.Charset;
 import java.util.List;
 import java.util.Vector;
 import org.jaudiotagger.audio.AudioHeader;
 import org.jaudiotagger.tag.FieldKey;
 import org.jaudiotagger.tag.Tag;
 
 import service.Errorist;
 import service.IOOperations;
 import service.Utils;
 
 import entagged.audioformats.AudioFileIO;
 import entagged.audioformats.exceptions.CannotReadException;
 
 public class MediaInfo {
 	static public String SitesFilter(String title)				{ return title.replaceAll("([\\(\\[](http:\\/\\/)*(www\\.)*([a-z0-9-])+\\.[a-z]+[\\]\\)])", ""); }
 	static public String ForwardNumberPreFilter(String title)	{ return title.replaceAll("\\A\\d{1,}.|\\(\\w*\\d{1,}\\w*\\)", ""); }
 	static public String SpacesFilter(String title) 			{ return title.replaceAll("[^0-9A-Za-z----]", ""); }
 	static public String ForwardNumberFilter(String title)		{ return title.replaceAll("\\A\\d{1,}", ""); }
 	
 	static Vector<String> InitExtsList() {
 		Vector<String> ret = new Vector<String>();
 		
 		// entagged // 		"flac", "ape", "mp3", "ogg", "wma", "wav", "mpc", "mp+"
 		// jaudiotagger //	
 //		"wsz,snd,aifc,aif,wav,au,mp1,mp2,mp3,ogg,spx,flac,ape,mac"
 		for(String ext : new String [] {"flac", "ape", "mp3", "mp4", "m4a", "m4p", "ogg", "wma", "wav", "asf", "mpc", "mp+", "rmf"}) //ape
 			ret.add(ext);
 	
 		return ret;
 	}
 	
 	public Vector<String> exts = InitExtsList();
 
 	List<String> Artists = new Vector<String>();
 	public List<String> Titles = new Vector<String>();
 	public List<String> Genres = new Vector<String>();
 	
 	public String Bitrate = "Unknow";
 	public String Channels = "Unknow";
 	public String Type = "Unknow";
 	public String SampleRate = "Unknow";
 	public Integer TimeLength = -1;
 	public boolean VariableBitrate = false;
 	
 	public boolean CheckFormat(String title, String ext) { return exts.contains(ext); }
 	
 	void AddTitleHelper(String gipoTitle) {
 		String temp = SitesFilter(gipoTitle);  
 		temp = SpacesFilter(ForwardNumberPreFilter(temp));
 		if (!Titles.contains(temp)) Titles.add(temp);
 		temp = ForwardNumberFilter(temp);
 		if (!Titles.contains(temp)) Titles.add(temp);
 	}
 	
 	public MediaInfo(File f) {
 		Genres.add("default");
 		String title = f.getName().toLowerCase();
 		String ext = IOOperations.extension(title);
 		
 		if (ext.length() == 0) AddTitleHelper(title);
 		else AddTitleHelper(IOOperations.name_without_extension(title, ext));
 		
 		if (CheckFormat(f.getName(), ext))
			if (ext.equals("m4a"))
				TryGetInfo(f);	
			else InitInfo(f);
 	}
 	
 	public MediaInfo(	String bitrate, String channels, String type, String samplerate,
 						String length, Boolean variable_bitrate, List<String> genres	) {
 		Bitrate = bitrate;	Channels = channels;	Type = type;	SampleRate = samplerate;
 		TimeLength = Integer.parseInt(length);	VariableBitrate = variable_bitrate;	Genres = genres;
 	}
 	
 	@Override
 	public String toString() {
 		return 	"Type \t\t: " + Type + "\n" +
 				"Bitrate \t: " + (VariableBitrate ? "~" : "") + Bitrate + "\n" +
 				"Channels \t: " + Channels + "\n" +
 				"SampleRate \t: " + SampleRate + "\n" +
 				"Time \t\t: " + Utils.TimeFormatter(TimeLength) + "\n";
 	}
 	
 	@SuppressWarnings("unchecked")
 	void InitInfo(File file) {
 		try {
 			entagged.audioformats.AudioFile f = AudioFileIO.read(file);
 			entagged.audioformats.Tag tag = f.getTag();
 			
 			String t1;
 			Artists = tag.getArtist();
 			List<Object> tlist = tag.getTitle();
 			for(Object t_title : tlist) {
 				t1 = t_title.toString().toLowerCase();
 				for(Object t_art : Artists)
 					AddTitleHelper(t_art.toString().toLowerCase() + "" + t1);
 			}
 				
 			Genres = tag.getGenre();
 			
 			Bitrate = "" + f.getBitrate();
 			Channels = "" + f.getChannelNumber();
 			
 			SampleRate = "" + f.getSamplingRate();
 			TimeLength = f.getLength();
 			
 			Type = f.getEncodingType();
 			VariableBitrate = f.isVbr();
 		} 
 		catch (CannotReadException e) {
 			TryGetInfo(file);
 			Errorist.printLog(e); 
 		}
 	}
 	
 	void TryGetInfo(File file) {
 		org.jaudiotagger.audio.AudioFile f;
 		try {
 			f = org.jaudiotagger.audio.AudioFileIO.read(file);
 			Tag tag = f.getTag();
 
 			String temp;
 			for(int n = 0;; n++) {
 				temp = tag.getValue(FieldKey.ARTIST, n);
 				temp = new String(temp.getBytes(),Charset.forName("windows-1251")).toLowerCase();
 				if (temp.isEmpty()) break;
 				if (Artists.contains(temp)) break;
 				Artists.add(temp);
 			}
 			
 			for(int n = 0;; n++) {
 				temp = tag.getValue(FieldKey.GENRE, n).toLowerCase();
 				if (temp.isEmpty()) break;
 				if (Genres.contains(temp)) break;
 				Genres.add(temp);
 			}
 
 			for(int n = 0;; n++) {
 				temp = tag.getValue(FieldKey.TITLE, n).toLowerCase();
 				if (temp.isEmpty()) break;
 				
 				if (Artists.size() == 0)
 					AddTitleHelper(temp);
 				else
 					for(String s: Artists)
 						AddTitleHelper(s + temp);
 			}
 			
 			AudioHeader head = f.getAudioHeader();
 			
 			Bitrate = head.getBitRate();
 			Channels = head.getChannels();
 			Type = head.getEncodingType();
 			SampleRate = head.getSampleRate();
 			TimeLength = head.getTrackLength();
 			VariableBitrate = head.isVariableBitRate();			
 		}
 		catch (org.jaudiotagger.audio.exceptions.CannotReadException e) {
 			Errorist.printLog(e);
 		}		
 		catch (Exception e) { Errorist.printLog(e); }
 	}
 }
