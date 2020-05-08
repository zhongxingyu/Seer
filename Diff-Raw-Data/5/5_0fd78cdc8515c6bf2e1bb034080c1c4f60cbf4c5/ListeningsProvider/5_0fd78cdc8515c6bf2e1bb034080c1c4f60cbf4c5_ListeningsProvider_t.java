 package msdchallenge.input.provider;
 
 import msdchallenge.simple.Constants;
 import msdchallenge.simple.IoUtil;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 public class ListeningsProvider {
 
 	private static final Logger LOG = LoggerFactory.getLogger(ListeningsProvider.class);
 
 	private final int[][] tracks;
 	private final short[][] counts;
 
 	public ListeningsProvider() {
 		tracks = IoUtil.deserialize(Constants.TRACKS_BY_USER_FILE);
		LOG.info(tracks.length + " users' listenings have been read from " + Constants.TRACKS_BY_USER_FILE);
 		counts = IoUtil.deserialize(Constants.COUNTS_BY_USER_FILE);
		LOG.info(tracks.length + " users' listening counts have been read from " + Constants.COUNTS_BY_USER_FILE);
 	}
 
 	public int[][] getTracks() {
 		return tracks;
 	}
 
 	public short[][] getCounts() {
 		return counts;
 	}
 
 }
