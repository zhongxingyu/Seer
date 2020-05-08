 package it.polito.atlas.alea2;
 
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.List;
 //import static java.lang.String.format;
 
 public class Annotation {
 	/// 
 	/// Constructor
 	/// 
 	public Annotation (String name) {	
 		this.name=name;
 		state = States.Pause;
 		position = 0;
 		length = 0;
 	}
 
 	/// 
 	/// Name of annotation
 	/// 
 	private String name;
 
 	/**
 	 * @return the name
 	 */
 	public String getName() {
 		return name;
 	}
 
 	/**
 	 * @param name the name to set
 	 */
 	public void setName(String name) {
 		this.name = name;
 	}
 
 	/// 
 	/// States of annotation
 	/// 
 	public enum States {
 		Play, Pause
 	}		
 	private States state;
 	/**
 	 * @return the state
 	 */
 	public States getState() {
 		return state;
 	}
 	
 	/// 
 	/// Length of annotation
 	/// 
 	private long length;
 	/**
 	 * @return the length
 	 */
 	public long getLenght() {
 		if (length < 0)
 			length = 0;
 		return length;
 	}
 	/**
 	 * @param lenght the length to set
 	 */
 	public void setLenght(long length) {
 		this.length = length;
 	}
 
 
 	/// 
 	/// Lunghezza massima dei video
 	/// 
 	private long maxLength = -1;
 	
 	/// 
	/// indice del video pi lungo
 	/// 
 	private int maxLengthIndex = -1;
 	
 	///
 	/// LIS Tracks
 	///
 	private List<TrackLIS> tracksLIS = new ArrayList<TrackLIS>();
 
 	/**
 	 * @return the LIS tracks
 	 */
 	public List<TrackLIS> getTracksLIS() {
 		return tracksLIS;
 	}
 
 	///
 	/// Video Tracks
 	///
 	private List <TrackVideo> tracksVideo = new ArrayList <TrackVideo> ();
 
 	/**
 	 * @return the Video tracks
 	 */
 	public List<TrackVideo> getTracksVideo() {
 		return tracksVideo;
 	}
 
 	///
 	/// Text Tracks
 	///
 	private List <TrackText> tracksText = new ArrayList <TrackText> ();
 	
 	/**
 	 * @return the Text tracks
 	 */
 	public List<TrackText> getTracksText() {
 		return tracksText;
 	}
 
 	public Collection<Track> getTracks() {
 		Collection<Track> c = new ArrayList<Track>();
 		c.addAll(getTracksLIS());
 		c.addAll(getTracksText());
 		c.addAll(getTracksVideo());
 		return c;
 	}
 
 	/// 
 	/// Ricalcola la durata in millisecondi
 	/// 
 	public long calcLenght () {
 		long end = 0;
 		for (Track t : getTracks()) {
 			long tmp=t.getEndTime();
 			if (tmp > end)
 				end = tmp;
 		}
 		length = end;
 		return end;
 	}
 	
 	///
 	/// Apre le finestre dei video
 	///
 	public void open () {
 		int i = 0;
 		
 		for (TrackVideo tv : tracksVideo) {
 			tv.open();
 
 			/** Cerca il video con la massima durata
			 * funziona finch non esiste uno spiazzamento
 			 * iniziale nei video
 			 */
 			long tmp = tv.getEndTime();
 			if (tmp > maxLength) {
 				maxLength = tmp;
 				maxLengthIndex = i;
 			}
 			++i;
 		}
 	}
 	
 	///
 	/// Chiude le finestre dei video
 	///
 	public void close () {
 		for (TrackVideo t : tracksVideo) {
 			t.close();
 		}
 	}
 	
 	///
 	/// Play the annotation
 	///
 	public void play () {
 		state = States.Play;
 		for (TrackVideo t : tracksVideo) {
 			t.play();
 		}
 	}
 	
 	///
 	/// Pause the annotation
 	///
 	public void pause () {
 		state = States.Pause;
 		for (TrackVideo t : tracksVideo) {
 			t.pause();
 		}
 		getPosition();
 	}
 	
 	///
 	/// Posiziona l'annotazione (i video) in un punto preciso (in millisecondi)
 	///
 	public void seek(long time) {
 		if (time > length)
 			time = length;
 		for (TrackVideo t : tracksVideo) {
 			t.seek(time);
 		}
 		position = time;
 	}
 	
 	///
 	/// Ritorna la posizione attuale in millisecondi
 	///
 	private long position;
 	/**
 	 * @param position the position to set
 	 */
 	public void setPosition(long time) {
 		seek(time);
 	}
 
 	public long getPosition() {
 		if (tracksVideo.size() > 0) {
 			position = tracksVideo.get(maxLengthIndex).getPosition();
 		} else {
 			position = 0;
 		}
 		return position;
 	}
 	
     /**
      * Returns a time String
      * @param time
      * The time in milliseconds
      * @return
      * The String representing the time
      */
     static public String timeString(long time)
     {
 		if (time == -1)
 			time=0;
 		long millisecs = time;
         long secs = millisecs / 1000;
         int mins = (int)(secs / 60);
 
         millisecs = millisecs - (secs * 1000);
         secs = secs - (mins * 60);
         if (mins >= 60)
         {
         	
             int hours = (int)(mins / 60);
             mins = mins - (hours * 60);
             return String.format("%05d:%02d:%02d:%03d", hours, mins, secs, millisecs);
         }
         return String.format("%02d:%02d:%03d", mins, secs, millisecs);
     }
 	
     public void addTracksVideo(List<Track> list)
     {
         for(Track t : list)
         	tracksVideo.add((TrackVideo)t);
 
     }
 
     public void addTracksLIS(List<Track> list)
     {
         for(Track t : list)
         	tracksLIS.add((TrackLIS)t);
     }
 
     public void addTracksText(List<Track> list)
     {
         for(Track t : list)
         	tracksText.add((TrackText)t);
     }
 	/**
 	 * Link to a Object representing the Annotation
 	 */
 	public Object link;
 }
 
