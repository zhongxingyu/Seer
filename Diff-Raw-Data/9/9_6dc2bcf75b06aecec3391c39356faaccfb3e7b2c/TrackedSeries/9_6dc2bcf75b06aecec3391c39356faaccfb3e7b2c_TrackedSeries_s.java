 package stv6.series;
 
 import java.io.File;
 import java.util.ArrayList;
 import java.util.List;
 
 import stv6.Profile;
 import stv6.episodes.BasicEpisode;
 import stv6.episodes.Episode;
 import stv6.episodes.SeriesEpisode;
 import stv6.http.request.Request;
 
 public class TrackedSeries implements Series {
 	
 	private static final SeriesEpisode resetEpisode = new SeriesEpisode("Now unwatched", null, 0);
 	
 	private final BasicSeries base;
 	private int lastEpisode;
 	private long lastView;
 
 	private String letter = null;
 
 	public TrackedSeries(BasicSeries base, int lastEpisode, long lastView) {
 		this.base = base;
 		update(lastEpisode, lastView);
 	}
 	
 	@Override
 	public int compareTo(Series o) {
 		return base.compareTo(o);
 	}
 
 	@Override
 	public String getClassName() {
 		return base.getClassName();
 	}
 
 	/**
 	 * Load the episode as requested in the Request via
 	 * 	GET var "ep"
 	 * 
 	 * @param r
 	 * @return The requested Episode, or null if invalid
 	 */
 	public SeriesEpisode getEpisodeFromRequest(Request r) {
 		if (!r.getGetVars().isSet("ep")) 
 			return null;
 		
 		try {
 			int episodeId = Integer.parseInt(r.getGetVars().getValue("ep"));
 			if (episodeId == -1)
 				return resetEpisode;
 			else if (episodeId > -1  && episodeId < base.episodes.size()) 
 				return new SeriesEpisode(base.episodes.get(episodeId), getId());
 			else return null;
 		} catch (NumberFormatException e) {
 			return null;
 		}
 	}
 	
 	public List<BasicEpisode> getEpisodes() {
 		return base.episodes;
 	}
 
 	@Override
 	public int getId() {
 		return base.getId();
 	}
 	
 	/**
 	 * @return The first letter of the name of this series,
 	 * 	if this is the first series with that letter
 	 */
 	public String getLetter() {
 		return letter;
 	}
 	
 	public long getLastView() {
 		return lastView;
 	}
 	
 	/**
 	 * @return The ID of the last-viewed episode
 	 */
 	public int getLastId() {
 		return lastEpisode;
 	}
 	
 	/**
 	 * @return The title of the last-viewed episode, if available
 	 */
 	public String getLastTitle() {
 		if (isManaged() && base.episodes.size() > lastEpisode)
 			return base.episodes.get(lastEpisode).getTitle();
 		
 		return null;
 	}
 
 	@Override
 	public String getLink() {
 		return base.getLink();
 	}
 	
 	public String getLocalPath() {
 		return base.localPath;
 	}
 	
 	public String getLocalPathFor(Episode e) {
 		// FIXME: Does this always work?
 		return base.localPath + File.separator + e.getTitle();
 	}
 
 	@Override
 	public String getName() {
 		return base.getName();
 	}
 	
 	public String getNextLink() {
 		int next = lastEpisode+1;
 		if (isManaged() && base.episodes.size() > next)
 			//return base.episodes.get(next).getSaveLink();
 			return "view?id=" + getId() + "&amp;ep=" + next + "&amp;save=1";
 		
 		return null;
 	}
 	
 	public String getNextTitle() {
 		int next = lastEpisode+1;
 		if (isManaged() && base.episodes.size() > next)
 			return base.episodes.get(next).getTitle();
 		
 		return null;
 	}
 	
 	public String getPrevLink() {
 		int prev = lastEpisode-1;
 		if (isManaged() && prev >= -1)
 			//return base.episodes.get(next).getSaveLink();
 			return "view?id=" + getId() + "&amp;ep=" + prev + "&amp;save=1";
 		
 		return null;
 	}
 	
 	public String getPrevTitle() {
 		int prev = lastEpisode-1;
 		if (isManaged()) {
 			if (prev > -1)
 				return base.episodes.get(prev).getTitle();
 			else if (prev == -1)
 				return "(Set series as unwatched)";
 		}
 		
 		return null;
 	}
 	
 	public boolean isDone() {
 		return isManaged() && (base.episodes.size() == lastEpisode+1);
 	}
 
 	@Override
 	public boolean isManaged() {
 		return base.isManaged();
 	}
 	
 	public boolean isRecent() {
 		return Profile.getInstance().seriesIsRecent(this);
 	}
 	
 	public boolean isNew() {
 	    return lastEpisode == -1;
 	}
 
 
 	/**
 	 * @return the id of the last-viewed episode
 	 */	
 	public int getLastEpisode() {
 		return lastEpisode;
 	}
 	
 	@Override
 	public void manageify(String localPath, ArrayList<BasicEpisode> eps) {
 		// FIXME What?
 	}
 	
 	@Override
 	public void setId(int newId) {
 		base.setId(newId);
 	}
 
 	/**
 	 * Let us know that we are, in fact, the first series in order
 	 * 	that has our first letter as the first letter in the title
 	 */
 	public void setLetter() {
 		this.letter = String.valueOf(getName().charAt(0));
 	}
 	
 	/** 
 	 * @return number of episodes
 	 */
 	public int size() {
 		return (base.episodes != null) ? base.episodes.size() : 0;
 	}
 	
 	public void update(int lastEpisode, long lastView) {
 		this.lastEpisode = lastEpisode;
 		this.lastView = lastView;
 	}
 
	public boolean hashCover() {
 		return base.hasCover();
 	}
 
 }
