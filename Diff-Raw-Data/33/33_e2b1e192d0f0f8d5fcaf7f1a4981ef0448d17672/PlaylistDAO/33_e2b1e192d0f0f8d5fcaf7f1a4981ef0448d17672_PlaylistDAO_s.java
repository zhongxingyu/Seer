 package playlist.model;
 
 import com.datastax.driver.core.BoundStatement;
 import com.datastax.driver.core.PreparedStatement;
 import com.datastax.driver.core.ResultSet;
 import com.datastax.driver.core.Row;
 
 import javax.servlet.ServletContext;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
 
 /**
  * Created with IntelliJ IDEA.
  * User: stevelowenthal
  * Date: 9/20/13
  * Time: 8:28 AM
  *
  */
 
 public class PlaylistDAO extends CassandraData {
 
   private UUID user_id;
   private String playlist_name;
   private String email;
   private int playlist_length_in_seconds;
   private List<Track> trackList;
 
   public PlaylistDAO(UserDAO user, String playlist_name) {
 
     // Simple constructor to create an empty playlist
     this.user_id = user.getUserid();
     this.email = user.getEmail();
     this.playlist_name = playlist_name;
     playlist_length_in_seconds = 0;
     trackList = new ArrayList<Track>();
 
   }
 
   public static class Track {
 
     private String track_name;
     private String artist;
     private int track_length_in_seconds;
     private Integer sequence_no;
 
     public Track(String track_name, String artist, int track_length_in_seconds) {
       this.track_name = track_name;
       this.artist = artist;
       this.track_length_in_seconds = track_length_in_seconds;
 
       this.sequence_no = null;  // A new track created this way has no order - it gets this when we persist it. There is no getter or setter.
     }
 
     public Track (Row row) {
       this.track_name = row.getString("track_name");
       this.artist = row.getString("artist");
       this.track_length_in_seconds = row.getInt("track_length_in_seconds");
       this.sequence_no = row.getInt("sequence_no");
     }
 
     public String getTrack_name() {
       return track_name;
     }
 
     public String getArtist() {
       return artist;
     }
 
     public int getTrack_length_in_seconds() {
       return track_length_in_seconds;
     }
 
     public Integer getSequence_no() {
       return sequence_no;
     }
   }
 
   // Static finder method
 
   public static PlaylistDAO getPlaylistForUser(UserDAO user, String playlist_name, ServletContext context) {
 
 
     // Create a new empty playlist object
     PlaylistDAO newPlaylist = new PlaylistDAO(user, playlist_name);
 
 
     // Read the tracks from the database
     PreparedStatement statement = getSession(context).prepare("SELECT user_id, playlist_name, sequence_no, artist, track_name, track_length_in_seconds " +
             "FROM playlist_tracks WHERE user_id = ? and playlist_name = ?");
 
     BoundStatement boundStatement = statement.bind(user.getUserid(), playlist_name);
     ResultSet resultSet = getSession(context).execute(boundStatement);
 
     for (Row row : resultSet)  {
       newPlaylist.trackList.add(new Track(row));
 
       // Pre-aggregate the playlist length in seconds;
       newPlaylist.playlist_length_in_seconds += row.getInt("track_length_in_seconds");
     }
 
     // Return it
     return newPlaylist;
 
   }
 
   public void deleteTrackFromPlaylist(int ordinalToDelete, ServletContext context) {
 
     // Find the track to delete
     Track trackToDelete = null;
     for (int i = 0; i < this.trackList.size(); i++) {
       if (this.trackList.get(i).sequence_no == ordinalToDelete) {
         trackToDelete = this.trackList.get(i);
         this.trackList.remove(i);
         break;
       }
     }
 
     // first adjust the playlist length
     playlist_length_in_seconds -= trackToDelete != null ? trackToDelete.getTrack_length_in_seconds() : 0;
 
     // remove it from the database
     PreparedStatement ps = getSession(context).prepare("DELETE from playlist_tracks where user_id = ? and playlist_name = ? and sequence_no = ?");
     BoundStatement bs = ps.bind(this.user_id, this.playlist_name, ordinalToDelete);
     getSession(context).execute(bs);
 
    }
 
 
   public void deletePlayList(ServletContext context) {
 
     // Change single quotes to a pair of single quotes for escaping into the database
     String fixed_playlist_name = this.playlist_name.replace("'","''");
 
     PreparedStatement preparedStatement = getSession(context).prepare("BEGIN BATCH " +
             "UPDATE users set playlist_names = playlist_names - {'" + fixed_playlist_name + "'} WHERE email = ? " +
             "DELETE FROM playlist_tracks WHERE user_id = ? and playlist_name = ? " +
             "APPLY BATCH;");
 
     BoundStatement bs = preparedStatement.bind(this.email, this.user_id, this.playlist_name);
 
     getSession(context).execute(bs);
 
   }
 
   public static PlaylistDAO createPlayList(UserDAO user, String playlist_name, ServletContext context) {
 
 
     // Change single quotes to a pair of single quotes for escaping into the database
     String fixed_playlist_name = playlist_name.replace("'","''");
 
     PreparedStatement preparedStatement = getSession(context).prepare(
             "UPDATE users set playlist_names = playlist_names + {'" + fixed_playlist_name +"'} WHERE email = ?");
     BoundStatement bs = preparedStatement.bind(user.getEmail());
     getSession(context).execute(bs);
 
     // Update the user object too
     user.getPlaylist_names().add(playlist_name);
 
     return new PlaylistDAO(user,playlist_name);
 
   }
 
 
   private void deletePlaylistTracks(ServletContext context) {
 
     // Delete a whole playlist
 
     PreparedStatement ps = getSession(context).prepare("DELETE from playlist_tracks where user_id = ? and playlist_name = ?");
     BoundStatement bs = ps.bind(this.user_id, this.playlist_name);
     getSession(context).execute(bs);
 
   }
 
   public void rewritePlaylist(ServletContext context) throws Exception {
 
     // First delete the whole playlist
     deletePlaylistTracks(context);
 
     // Now insert all of the track, but first, reset all of the ordinals
     List<Track> tracklist = this.trackList;
 
     // remove the tracks from the playlist, so we can add them back and renumber them
     this.trackList = new ArrayList<Track>();
 
     // Add them back
     addTracksToPlaylist(tracklist, context);
 
   }
 
   public void addTracksToPlaylist(List<Track> newTracks, ServletContext context) throws Exception {
 
     // Prepare an insert statement
     PreparedStatement statement = getSession(context).prepare(
             "INSERT into playlist_tracks" +
                     " (user_id, playlist_name, sequence_no, artist, track_name, track_length_in_seconds) " +
                     "VALUES (?, ?, ?, ?, ?, ?)"
     );
     BoundStatement boundStatement = statement.bind();
 
     int newNumber = 1;
 
     if (this.trackList.size() > 0) {
       newNumber = this.trackList.get(this.trackList.size() - 1).sequence_no + 1;
     }
 
     for (Track track : newTracks) {
       track.sequence_no = newNumber ++;
 
       // Let's use named parameters this time
       boundStatement.setUUID("user_id", getUser_id());
       boundStatement.setString("playlist_name", getPlaylist_name());
       boundStatement.setInt("sequence_no", track.sequence_no);
       boundStatement.setString("track_name", track.getTrack_name());
       boundStatement.setString("artist", track.getArtist());
       boundStatement.setInt("track_length_in_seconds", track.getTrack_length_in_seconds());
 
       getSession(context).execute(boundStatement);
     }
 
     this.trackList.addAll(newTracks);
   }
 
   public UUID getUser_id() {
     return user_id;
   }
 
   public String getPlaylist_name() {
     return playlist_name;
   }
 
   public List<Track> getTrackList() {
     return trackList;
   }
 
   public int getPlaylist_length_in_seconds() {
     return playlist_length_in_seconds;
   }
 
   public String getEmail() {
     return email;
   }
 }
