 package playlist.model;
 
 import com.datastax.driver.core.BoundStatement;
 import com.datastax.driver.core.PreparedStatement;
 import com.datastax.driver.core.ResultSet;
 import com.datastax.driver.core.Row;
 
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 import java.util.UUID;
 
 /**
  * DataStax Academy Sample Application
  *
  * Copyright 2013 DataStax
  *
  */
 
 
 public class PlaylistDAO extends CassandraData {
 
   private String playlist_name;
   private String username;
   private int playlist_length_in_seconds;
   private List<PlaylistTrack> playlistTrackList;
 
  public PlaylistDAO(String username, String playlist_name) {
 
     // Simple constructor to create an empty playlist
    this.username = username;
     this.playlist_name = playlist_name;
     playlist_length_in_seconds = 0;
     playlistTrackList = new ArrayList<>();
   }
 
   public static class PlaylistTrack {
 
     private String track_name;
     private String artist;
     private int track_length_in_seconds;
     private String genre;
     private String track_id;
     private Date sequence_no;
 
     public PlaylistTrack(TracksDAO track) {
       this.track_name = track.getTrack();
       this.artist = track.getArtist();
       this.track_length_in_seconds = track.getTrack_length_in_seconds();
       this.genre = track.getGenre();
       this.track_id = track.getTrack_id();
       this.sequence_no = null;  // A new track created this way has no order - it gets this when we persist it. There is no getter or setter.
     }
 
     public PlaylistTrack(Row row) {
       this.track_name = row.getString("track_name");
       this.artist = row.getString("artist");
       this.track_length_in_seconds = row.getInt("track_length_in_seconds");
       this.sequence_no = row.getDate("sequence_no");
       this.track_id = row.getString("track_id");
       this.genre = row.getString("genre");
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
 
     public long getSequence_no() {
       return sequence_no.getTime();
     }
 
     public String getGenre() {
       return genre;
     }
 
     public String getTrack_id() {
       return track_id;
     }
 
   }
 
   /**
    *
    * Create a new playlist
    *
    * @param user A userDAO object for the user that gets the new playlist
    * @param playlist_name The name of the new Playlist
    * @return  A PlaylistDAO object for the new playlist
    */
   public static PlaylistDAO createPlayList(UserDAO user, String playlist_name) {
 
 
     // Change single quotes to a pair of single quotes for escaping into the database
     String fixed_playlist_name = playlist_name.replace("'","''");
 
     PreparedStatement preparedStatement = getSession().prepare(
             "UPDATE users set playlist_names = playlist_names + {'" + fixed_playlist_name +"'} WHERE username = ?");
     BoundStatement bs = preparedStatement.bind(user.getUsername());
     getSession().execute(bs);
 
     // Update the user object too
 
     user.getPlaylist_names().add(playlist_name);
 
     return new PlaylistDAO(user.getUsername(),playlist_name);
 
   }
 
   /**
    * Delete this playlist
    */
   public void deletePlayList() {
 
     // Change single quotes to a pair of single quotes for escaping into the database
     String fixed_playlist_name = this.playlist_name.replace("'","''");
 
     PreparedStatement preparedStatement = getSession().prepare("BEGIN BATCH " +
             "UPDATE users set playlist_names = playlist_names - {'" + fixed_playlist_name + "'} WHERE username = ? " +
             "DELETE FROM playlist_tracks WHERE username = ? and playlist_name = ? " +
             "APPLY BATCH;");
 
     BoundStatement bs = preparedStatement.bind(this.username, this.username, this.playlist_name);
 
     getSession().execute(bs);
 
   }
 
   // Static finder method
 
   public static PlaylistDAO getPlaylistForUser(String username, String playlist_name) {
 
 
     // Create a new empty playlist object
     PlaylistDAO newPlaylist = new PlaylistDAO(username, playlist_name);
 
 
     // Read the tracks from the database
     PreparedStatement statement = getSession().prepare("SELECT username, playlist_name, sequence_no, artist, track_name, track_id, genre, track_length_in_seconds " +
             "FROM playlist_tracks WHERE username = ? and playlist_name = ?");
 
     BoundStatement boundStatement = statement.bind(username, playlist_name);
     ResultSet resultSet = getSession().execute(boundStatement);
 
     for (Row row : resultSet)  {
       newPlaylist.playlistTrackList.add(new PlaylistTrack(row));
 
       // Pre-aggregate the playlist length in seconds;
       newPlaylist.playlist_length_in_seconds += row.getInt("track_length_in_seconds");
     }
 
     // Return it
     return newPlaylist;
 
   }
 
   /**
    *
    * @param sequenceNumberToDelete - This is a time, express as an offset from Epoch
    */
   public void deleteTrackFromPlaylist(long sequenceNumberToDelete) {
 
     // Find the track to delete, and delete it from the list
 
     // Create a variable that for the playlist object we find
     PlaylistTrack playlistTrackToDelete = null;
 
     // Loop through all of the tracks in the playlistTrackList
     // We are using a simple iterator i in this loop
     for (int i = 0; i < this.playlistTrackList.size(); i++) {
 
       // extract the time of from the current playlist track's sequence number, and compare it to the given time
       if (this.playlistTrackList.get(i).sequence_no.getTime() == sequenceNumberToDelete) {
 
         // If it's correct, set playlistTrackToDelete, remove it from the playlist, and stop looping
         playlistTrackToDelete = this.playlistTrackList.get(i);
         this.playlistTrackList.remove(i);
         break;
       }
     }
 
     // first adjust the playlist length
     playlist_length_in_seconds -= playlistTrackToDelete != null ? playlistTrackToDelete.getTrack_length_in_seconds() : 0;
 
     // remove it from the database
     PreparedStatement ps = getSession().prepare("DELETE from playlist_tracks where username = ? and playlist_name = ? and sequence_no = ?");
     BoundStatement bs = ps.bind(this.username, this.playlist_name, new Date(ordinalToDelete));
     getSession().execute(bs);
 
   }
 
   public void addTrackToPlaylist(PlaylistTrack playlistTrack) {
 
     // Prepare an insert statement
     PreparedStatement statement = getSession().prepare(
             "INSERT into playlist_tracks" +
                     " (username, playlist_name, sequence_no, artist, track_name, genre, track_id, track_length_in_seconds) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?)"
     );
     BoundStatement boundStatement = statement.bind();
 
     // Since the playlistTrack sequence is like a time-series, set it's sequence to the current time
     // Also update the total time for the playlist locally.
 
     playlistTrack.sequence_no = new Date();
     this.playlist_length_in_seconds += playlistTrack.track_length_in_seconds;
 
     // Let's use named parameters this time
     boundStatement.setString("username", getUsername());
     boundStatement.setString("playlist_name", getPlaylist_name());
     boundStatement.setDate("sequence_no", playlistTrack.sequence_no);
     boundStatement.setString("track_name", playlistTrack.getTrack_name());
     boundStatement.setString("artist", playlistTrack.getArtist());
     boundStatement.setString("track_id", playlistTrack.getTrack_id());
     boundStatement.setInt("track_length_in_seconds", playlistTrack.getTrack_length_in_seconds());
     boundStatement.setString("genre", playlistTrack.getGenre());
 
     getSession().execute(boundStatement);
 
     this.playlistTrackList.add(playlistTrack);
   }
 
   public String getPlaylist_name() {
     return playlist_name;
   }
 
   public List<PlaylistTrack> getPlaylistTrackList() {
     return playlistTrackList;
   }
 
   public int getPlaylist_length_in_seconds() {
     return playlist_length_in_seconds;
   }
 
   public String getUsername() {
     return username;
   }
 
 }
