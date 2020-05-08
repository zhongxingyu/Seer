 package it.polito.atlas.alea2.db;
 
 import it.polito.atlas.alea2.Slice;
 import it.polito.atlas.alea2.Track;
 import it.polito.atlas.alea2.TrackLIS;
 import it.polito.atlas.alea2.TrackText;
 import it.polito.atlas.alea2.TrackVideo;
 
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.Statement;
 import java.util.ArrayList;
 import java.util.List;
 
 public class DBTrack {
 	protected static boolean write(Track t, long id_annotation, DBInstance db) throws SQLException, DBRuntimeException { 
        if(t == null)
             return true;
         
 		boolean allOK = true;
 		String name = t.getName();
 		String sql = "insert into track (name, type, id_annotation) values ('" +
 				name + "', '" +
 				t.getTypeString() + "'," +
 				id_annotation + ")";
 
 		ResultSet rs = null;
 		long id_track = -1;
 		Statement stmt = db.getStatement();
 		if (stmt.executeUpdate(sql, Statement.RETURN_GENERATED_KEYS) != 1)
 			throw new DBRuntimeException("Insert track failed (" + name + ")");
 	
 		rs = stmt.getGeneratedKeys();
 		if (rs.next()) {
			id_track = rs.getLong(1);
 		} else {
 			throw new DBRuntimeException("Can't get id_track of (" + name + ")");
 		}
 
 		try {
 			rs.close();
 		} catch (SQLException e) {
 			// ignore
 			e.printStackTrace();
 		}
 		
 		for (Slice s : t.getSlices()) {
 			try {
 				allOK &= DBSlice.write(s, id_track, db);
 			} catch (SQLException e) {
 				e.printStackTrace();
 				allOK = false;
 			}
 		}
 		return allOK;
 	}
 
 	/**
 	 * Returns a list of Annotation Tracks of a specified Track Type
 	 * @param id_annotation The Annotation id stored in database
 	 * @param tt The Track Type
 	 * @param db The active DBInstance
 	 * @return The list of Tracks
 	 * @throws SQLException 
 	 */
 	protected static List<Track> readAll(long id_annotation, Track.Types tt, DBInstance db) throws SQLException {
 		List <Track> tracks = new ArrayList<Track>();
 		String sql = "select id_track, name from track " +
 				"where id_annotation = " + id_annotation +
 				" and type = '" + Track.getTypeString(tt) + "'";
 	
 		ResultSet rs;
 		rs = db.getStatement().executeQuery(sql);
 		
 		Track t = null;
 		long id_track;
 		while (rs.next()) {
 			
 			try {
 				id_track = rs.getLong(1);
 				String trackName = rs.getString(2);
 				switch (tt) {
 					case Video:
 						t = new TrackVideo(trackName);
 						break;
 					case Text:
 						t = new TrackText(trackName);
 						break;
 					case LIS:
 						t = new TrackLIS(trackName);
 						break;
 				}
 			} catch (SQLException e) {
 				e.printStackTrace();
 				continue;
 			}
 			try {
 				t.addSlices(DBSlice.readAll(id_track, db));
 			} catch (SQLException e) {
 				e.printStackTrace();
 			}
 			tracks.add(t);
 		}
 		try {
 			rs.close();
 		} catch (SQLException e) {
 			// ignore
 			e.printStackTrace();
 		}
 		return tracks;
 	}
 }
