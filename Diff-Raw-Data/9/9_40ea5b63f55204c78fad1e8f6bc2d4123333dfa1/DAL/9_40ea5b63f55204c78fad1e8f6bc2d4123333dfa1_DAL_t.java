 package my.triviagame.dal;
 
 import com.google.common.base.Joiner;
 import com.google.common.base.Preconditions;
 import com.google.common.collect.Sets;
 import java.io.IOException;
 import java.sql.*;
 import java.util.*;
 import my.triviagame.xmcd.XmcdDisc;
 import org.apache.commons.io.FileUtils;
 
 public class DAL implements IDAL {
 
     //DB connection
     private Connection conn = null;
     //Prepared Statements for insetions
     private PreparedStatement pstmtInsertGenre = null;
     private PreparedStatement pstmtInsertArtist = null;
     private PreparedStatement pstmtInsertAlbum = null;
     private PreparedStatement pstmtInsertTrack = null;
     //Prepared Statemnets for table counters
     private PreparedStatement pstmtGetTrackTableStatistics = null;
     private PreparedStatement pstmtGetAlbumTableStatistics = null;
     private PreparedStatement pstmtGetArtistTableStatistics = null;
     private String strGetBasicTracks = null;
     private String strGetBasicAlbums = null;
     private String strGetBasicArtists = null;
     private ITableStatistics trackTableStatistics = null;
     private ITableStatistics albumTableStatistics = null;
     private ITableStatistics artistTableStatistics = null;
 
     //TODO: remove
     public Connection getConnection() {
         return conn;
     }
 
     /**
      * 1. Opens connection to DB according to specifies parameters 2. Prepare all relevant prepared statements
      *
      * @param hostName
      * @param port
      * @param dbName
      * @param userName
      * @param password
      * @throws my.triviagame.dal.IDAL.DALException
      */
     @Override
     public void openConnection(
             String hostName,
             int port,
             String dbName,
             String userName,
             String password) throws IDAL.DALException {
         try {
             Class.forName("com.mysql.jdbc.Driver");
         } catch (ClassNotFoundException ex) {
             throw new IDAL.DALException(ex);
         }
         String formatString = "jdbc:mysql://%s:%d/%s";
         try {
             conn = DriverManager.getConnection(
                     String.format(formatString, hostName, port, dbName), userName, password);
             readResourceSqlFiles();
             prepareQueryStatements();
             prepareImportStatements();
         } catch (IOException ex) {
             throw new DALException(ex);
         } catch (SQLException ex) {
             throw new IDAL.DALException(ex);
         }
     }
 
     @Override
     public void closeConnection() throws IDAL.DALException {
         try {
             destoryQueryStatements();
             destroyImportStatements();
             conn.close();
         } catch (SQLException ex) {
             throw new IDAL.DALException(ex);
         }
         conn = null;
     }
 
     //////////////////////////////////////////
     // Table Statistics
     //////////////////////////////////////////
     private ITableStatistics getTableStatistics(PreparedStatement pstmt)
             throws DALException {
         Preconditions.checkNotNull(conn, "Connection closed");
         ResultSet rs = null;
         try {
 
             rs = pstmt.executeQuery();
             rs.next();
             TableStatistics newStatistics = new TableStatistics(rs);
             rs.close();
 
             return newStatistics;
         } catch (SQLException ex) {
             throw new IDAL.DALException(ex);
         } finally {
             try {
                 if (rs != null) {
                     rs.close();
                 }
             } catch (SQLException ex) {
                 throw new DALException(ex);
             }
         }
     }
 
     @Override
     public int getTrackCount() throws IDAL.DALException {
         return getTrackTableStatistics().getRowCount();
     }
 
     @Override
     public int getAlbumCount() throws IDAL.DALException {
         return getAlbumTableStatistics().getRowCount();
     }
 
     @Override
     public int getArtistCount() throws IDAL.DALException {
         return getArtistTableStatistics().getRowCount();
     }
 
     @Override
     public ITableStatistics getTrackTableStatistics() throws DALException {
         if (trackTableStatistics == null) {
             trackTableStatistics = getTableStatistics(pstmtGetTrackTableStatistics);
         }
         return trackTableStatistics;
     }
 
     @Override
     public ITableStatistics getAlbumTableStatistics() throws DALException {
         if (albumTableStatistics == null) {
             albumTableStatistics = getTableStatistics(pstmtGetAlbumTableStatistics);
         }
         return albumTableStatistics;
     }
 
     @Override
     public ITableStatistics getArtistTableStatistics() throws DALException {
         if (artistTableStatistics == null) {
             artistTableStatistics = getTableStatistics(pstmtGetArtistTableStatistics);
         }
         return artistTableStatistics;
     }
 
     @Override
     public List<ITrackDescriptor> getTrackDescriptors(List<Integer> ids) throws IDAL.DALException {
         String tailTerm = String.format("AND track.id IN (%s)", Joiner.on(",").join(ids));
         return getTrackDescriptorsByTailTerm(tailTerm);
     }
 
     @Override
     public List<ITrackDescriptor> getAlbumTrackDescriptors(IAlbumDescriptor album) throws DALException {
         String tailTerm = "AND disc.id = " + album.getId();
         return getTrackDescriptorsByTailTerm(tailTerm);
     }
 
     @Override
     public List<ITrackDescriptor> getArtistTrackDescriptors(IArtistDescriptor artist)
             throws IDAL.DALException {
         String tailTerm = "AND track_artist.id = " + artist.getId();
         return getTrackDescriptorsByTailTerm(tailTerm);
     }
 
     @Override
     public List<IAlbumDescriptor> getAlbumDescriptors(List<Integer> ids) throws DALException {
         String tailTerm = String.format("AND disc.id IN (%s)", Joiner.on(",").join(ids));
         return getAlbumDescriptorsByTailTerm(tailTerm);
     }
 
     @Override
     public List<IAlbumDescriptor> getArtistAlbumDescriptors(IArtistDescriptor artist) throws DALException {
         String tailTerm = "AND artist.id = " + artist.getId();
         return getAlbumDescriptorsByTailTerm(tailTerm);
     }
 
     @Override
     public List<IArtistDescriptor> getArtistDescriptors(List<Integer> ids) throws DALException {
         String tailTerm = String.format("AND artist.id IN (%s)", Joiner.on(",").join(ids));
         return getArtistDescriptorsByTailTerm(tailTerm);
     }
 
     @Override
     public Collection<ITrackDescriptor> getTrackDescriptors(String... keywords) throws DALException {
         List<String> keywordTerms = new ArrayList<String>(keywords.length);
         for (String keyword : keywords) {
             String tempKeyword = keyword.replace("'", "\"");
             keywordTerms.add(String.format("all_text like '%%%s%%'", tempKeyword.toLowerCase()));
         }
         String tailTerm = Joiner.on(" and ").join(keywordTerms);
         tailTerm = String.format("HAVING %s", tailTerm);
         return getTrackDescriptorsByTailTerm(tailTerm);
     }
 
     //returns all tracks that matches tail term, spelled in SQL
     private List<ITrackDescriptor> getTrackDescriptorsByTailTerm(String tailTerm) throws IDAL.DALException {
         Preconditions.checkNotNull(conn, "Connection closed");
         ResultSet rs = null;
         Statement stmt = null;
         List<ITrackDescriptor> descList = new ArrayList<ITrackDescriptor>();
 
         try {
             String strQuery = strGetBasicTracks + " " + tailTerm;
             stmt = conn.createStatement();
             rs = stmt.executeQuery(strQuery);
             while (rs.next()) {
                 descList.add(new TrackDescriptor(rs));
             }
 
         } catch (SQLException ex) {
             throw new IDAL.DALException(ex);
         } finally {
             try {
                 stmt.close();
                 rs.close();
             } catch (SQLException ex) {
                 throw new DALException(ex);
             }
         }
         return descList;
     }
 
     private List<IAlbumDescriptor> getAlbumDescriptorsByTailTerm(String tailTerm) throws IDAL.DALException {
         Preconditions.checkNotNull(conn, "Connection closed");
         ResultSet rs = null;
         Statement stmt = null;
         List<IAlbumDescriptor> descList = new ArrayList<IAlbumDescriptor>();
 
         try {
             String strQuery = strGetBasicAlbums + " " + tailTerm;
             stmt = conn.createStatement();
             rs = stmt.executeQuery(strQuery);
             while (rs.next()) {
                 descList.add(new AlbumDescriptor(rs));
             }
             stmt.close();
             rs.close();
         } catch (SQLException ex) {
             throw new IDAL.DALException(ex);
         } finally {
             try {
                 stmt.close();
                 rs.close();
             } catch (SQLException ex) {
                 throw new DALException(ex);
             }
         }
         return descList;
     }
 
     private List<IArtistDescriptor> getArtistDescriptorsByTailTerm(String tailTerm) throws IDAL.DALException {
         Preconditions.checkNotNull(conn, "Connection closed");
         ResultSet rs = null;
         Statement stmt = null;
         List<IArtistDescriptor> descList = new ArrayList<IArtistDescriptor>();
 
         try {
             String strQuery = strGetBasicArtists + " " + tailTerm;
             stmt = conn.createStatement();
             rs = stmt.executeQuery(strQuery);
             while (rs.next()) {
                 descList.add(new ArtistDescriptor(rs));
             }
 
         } catch (SQLException ex) {
             throw new IDAL.DALException(ex);
         } finally {
             try {
                 stmt.close();
                 rs.close();
             } catch (SQLException ex) {
                 throw new DALException((ex));
             }
         }
         return descList;
     }
 
     @Override
     public void CreateAlbum(IAlbumDescriptor album) {
         //TODO: update cached counts
         throw new UnsupportedOperationException("Not supported yet.");
     }
 
     @Override
     public void DeleteAlbum(IAlbumDescriptor album) {
         //TODO: update cached counts
         throw new UnsupportedOperationException("Not supported yet.");
     }
 
     @Override
     public void UpdateAlbum(IAlbumDescriptor album) {
         //TODO: update cached counts
         throw new UnsupportedOperationException("Not supported yet.");
         
     }
 
     @Override
     public void CreateAlbum(IAlbumDescriptor album, List<ITrackDescriptor> tracks) throws DALException {
         //TODO: wait for shay converter from descriptors to Xmcd
     }
     
     
     @Override
     public void prepareConstraintsForImport() throws DALException {
         Statement stmt;
         try {
             stmt = conn.createStatement();
         } catch (SQLException ex) {
             throw new DALException(ex);
         }
         try {
             stmt.executeUpdate("ALTER TABLE disc DROP FOREIGN KEY fk_disc_genre_id");
         } catch (SQLException ex) {
         }
         try {
             stmt.executeUpdate("ALTER TABLE disc DROP FOREIGN KEY fk_disc_artist_id");
         } catch (SQLException ex) {
         }
         try {
             stmt.executeUpdate("ALTER TABLE track DROP FOREIGN KEY fk_track_artist_id");
         } catch (SQLException ex) {
         }
         try {
             stmt.executeUpdate("ALTER TABLE track DROP FOREIGN KEY fk_track_disc_id");
         } catch (SQLException ex) {
         }
         try {
             stmt.close();
         } catch (SQLException ex) {
             throw new DALException(ex);
         }
     }
 
     @Override
     public void createConstraintsAfterImport() throws DALException {
         Statement stmt = null;
         try {
             stmt = conn.createStatement();
         } catch (SQLException ex) {
             throw new DALException(ex);
         }
         try {
             stmt.executeUpdate(
                     "ALTER TABLE disc "
                     + "ADD CONSTRAINT `fk_disc_genre_id` "
                     + "FOREIGN KEY (`genre_id` ) REFERENCES `genre` (`id` ) "
                     + "ON DELETE NO ACTION ON UPDATE CASCADE,"
                     + "ADD CONSTRAINT `fk_disc_artist_id` "
                     + "FOREIGN KEY (`artist_id` ) REFERENCES `artist` (`id` ) "
                     + "ON DELETE NO ACTION ON UPDATE CASCADE");
         } catch (SQLException ex) {
             throw new DALException(ex);
         }
         try {
             //TODO: check what is the correct behaviour for delete
             stmt.executeUpdate(
                     "ALTER TABLE track "
                     + "ADD CONSTRAINT `fk_track_disc_id` "
                     + "FOREIGN KEY (`disc_id` ) REFERENCES `disc` (`id` ) "
                     + "ON DELETE CASCADE ON UPDATE CASCADE, "
                     + "ADD CONSTRAINT `fk_track_artist_id` "
                     + "FOREIGN KEY (`artist_id` ) REFERENCES `artist` (`id` ) "
                     + "ON DELETE NO ACTION ON UPDATE CASCADE");
         } catch (SQLException ex) {
             throw new DALException(ex);
         }
         try {
             stmt.close();
         } catch (SQLException ex) {
             throw new DALException(ex);
         }
     }
 
     @Override
     public void importXmcdBatch(List<XmcdDisc> xmcdDiscList) throws DALException {
 
         try {
             conn.setAutoCommit(false);
             Set<String> uniqueGenre = Sets.newHashSet();
             Set<String> uniqueArtist = Sets.newHashSet();
 
             Statement stmtPreImport = conn.createStatement();
             Map<Integer, XmcdDisc> freedbToXmcd = new HashMap<Integer, XmcdDisc>();
             Map <Integer,Integer> deleteFreedbToId = new HashMap<Integer,Integer>();
 
             for (XmcdDisc xmcdDisc : xmcdDiscList) {
                 freedbToXmcd.put(xmcdDisc.albumRow.freedbId, xmcdDisc);
             }
 
             String inTerm = Joiner.on(",").join(freedbToXmcd.keySet());
             String findQuery = String.format(
                     "SELECT id, freedb_id, revision FROM disc WHERE freedb_id in (%s)", inTerm);
 
             ResultSet rsMatchingDiscs = stmtPreImport.executeQuery(findQuery);
 
             //Find which discs to delete and which discs are new
             while (rsMatchingDiscs.next()) {
                 int curFreedb = rsMatchingDiscs.getInt("freedb_id");
                 XmcdDisc newDisc = freedbToXmcd.get(curFreedb);
                 if (newDisc != null) {
                     int curRevision = rsMatchingDiscs.getInt("revision");
                     if (curRevision >= newDisc.albumRow.revision) {
                         //update disc is of stale or same revision
                         freedbToXmcd.remove(curFreedb);
                     } else {
                         //update disc is of newer revision - add exisiting reviosin to delete list
                         deleteFreedbToId.put(curFreedb,rsMatchingDiscs.getInt("id"));
                     }
                 }
             }
             
             //Delete all old discs
             if (!deleteFreedbToId.isEmpty()) {
                 String deleteDiscTerm = Joiner.on(",").join(deleteFreedbToId.keySet());
                 String deleteDiscQuery = String.format(
                         "DELETE FROM disc WHERE freedb_id in (%s)", deleteDiscTerm);
                 stmtPreImport.executeUpdate(deleteDiscQuery);
 
                 //Delete old tracks
                 String deleteTrackTerm = Joiner.on(",").join(deleteFreedbToId.values());
                 String deleteTrackQuery = String.format(
                         "DELETE FROM track WHERE disc_id in (%s)", deleteTrackTerm);
                 stmtPreImport.executeUpdate(deleteTrackQuery);
 
             }
             
             for (XmcdDisc xmcdDisc : freedbToXmcd.values()) {
 
                 AlbumRow albumRow = xmcdDisc.albumRow;
                 uniqueGenre.add(albumRow.freeTextGenre);
                 uniqueArtist.add(albumRow.artistName);
 
                 pstmtInsertAlbum.setInt(1, albumRow.freedbId);
                 pstmtInsertAlbum.setString(2, albumRow.title);
                 pstmtInsertAlbum.setInt(3, albumRow.year);
                 pstmtInsertAlbum.setString(4, albumRow.freeTextGenre);
                 pstmtInsertAlbum.setInt(5, albumRow.freedbGenre);
                 pstmtInsertAlbum.setString(6, albumRow.artistName);
                 pstmtInsertAlbum.setInt(7, albumRow.revision);
                 pstmtInsertAlbum.addBatch();
 
                 for (TrackRow trackRow : xmcdDisc.trackRows) {
 
                     uniqueArtist.add(trackRow.artistName);
 
                     pstmtInsertTrack.setInt(1, albumRow.freedbId);
                     pstmtInsertTrack.setInt(2, trackRow.trackNum);
                     pstmtInsertTrack.setString(3, trackRow.artistName);
                     pstmtInsertTrack.setInt(4, trackRow.lenInSec);
                     pstmtInsertTrack.setString(5, trackRow.title);
                     pstmtInsertTrack.addBatch();
                 }
             }
 
             for (String genre : uniqueGenre) {
                 pstmtInsertGenre.setString(1, genre);
                 pstmtInsertGenre.addBatch();
             }
 
             for (String artist : uniqueArtist) {
                 pstmtInsertArtist.setString(1, artist);
                 pstmtInsertArtist.addBatch();
             }
 
             pstmtInsertGenre.executeBatch();
             pstmtInsertArtist.executeBatch();
             pstmtInsertAlbum.executeBatch();
             pstmtInsertTrack.executeBatch();
 
             conn.commit();
             conn.setAutoCommit(true);
             stmtPreImport.close();
 
         } catch (SQLException ex) {
             throw new DALException(ex);
         }
     }
 
     /////////////////////////////////////////////
     // HELPERS
     /////////////////////////////////////////////
     private String getResourceFileContents(String fileName) throws IOException {
         String fileContent = FileUtils.readFileToString(FileUtils.toFile(
                 getClass().getResource(fileName)));
         return fileContent;
     }
 
     private void readResourceSqlFiles() throws IOException {
         strGetBasicTracks = getResourceFileContents("resources/get_tracks_basic.sql");
         strGetBasicAlbums = getResourceFileContents("resources/get_albums_basic.sql");
         strGetBasicArtists = getResourceFileContents("resources/get_artists_basic.sql");
     }
 
     private void prepareQueryStatements() throws DALException {
         try {
 
             pstmtGetTrackTableStatistics = conn.prepareStatement(
                     "SELECT COUNT(*) as row_count, MIN(id) as min_id, max(id) as max_id FROM track");
 
             pstmtGetAlbumTableStatistics = conn.prepareStatement(
                     "SELECT COUNT(*) as row_count, MIN(id) as min_id, max(id) as max_id FROM disc");
 
             pstmtGetArtistTableStatistics = conn.prepareStatement(
                     "SELECT COUNT(*) as row_count, MIN(id) as min_id, max(id) as max_id FROM artist");
 
         } catch (SQLException ex) {
             throw new DALException(ex);
         }
     }
 
     private void destroyPreparedStatement(PreparedStatement pstmt) throws SQLException {
         if (pstmt != null) {
             pstmt.close();
             pstmt = null;
         }
     }
 
     private void destoryQueryStatements() throws DALException {
         try {
             destroyPreparedStatement(pstmtGetTrackTableStatistics);
             destroyPreparedStatement(pstmtGetAlbumTableStatistics);
             destroyPreparedStatement(pstmtGetArtistTableStatistics);
 
         } catch (SQLException ex) {
             throw new DALException(ex);
         }
     }
 
     private void prepareImportStatements() throws DALException {
         try {
 
             pstmtInsertGenre = conn.prepareStatement(
                     "INSERT IGNORE INTO genre(name) VALUES(?)", Statement.RETURN_GENERATED_KEYS);
             pstmtInsertArtist = conn.prepareStatement(
                     "INSERT IGNORE INTO artist(name) VALUES(?)", Statement.RETURN_GENERATED_KEYS);
 
             pstmtInsertAlbum = conn.prepareStatement(
                     "INSERT IGNORE INTO disc "
                     + "(freedb_id, title, year, genre_id, freedb_genre_index, artist_id, revision) "
                     + "VALUES ("
                     + "?, " //freedb_id
                     + "?, " //title
                     + "?, " //year
                     + "(SELECT id FROM genre WHERE genre.name = ? LIMIT 1), " //genre_name
                     + "?, " //freedb_genre_index
                     + "(SELECT id FROM artist WHERE artist.name = ? LIMIT 1), " //aritst_name
                     + "?)"); //revision
 
             pstmtInsertTrack = conn.prepareStatement(
                     "INSERT IGNORE INTO track "
                     + "(disc_id, track_num, artist_id, len_in_sec, title)"
                     + "VALUES ("
                     + "(SELECT id FROM disc WHERE disc.freedb_id = ? LIMIT 1) , " //freedb_db_id
                     + "?, " //track_num
                     + "(SELECT id FROM artist WHERE artist.name = ? LIMIT 1), " //artist_name
                     + "?, " //len_in_sec
                     + "?)");    //title
         } catch (SQLException ex) {
             throw new DALException(ex);
         }
     }
 
     private void destroyImportStatements() throws DALException {
         try {
             destroyPreparedStatement(pstmtInsertGenre);
             destroyPreparedStatement(pstmtInsertArtist);
             destroyPreparedStatement(pstmtInsertAlbum);
             destroyPreparedStatement(pstmtInsertGenre);
         } catch (SQLException ex) {
             throw new DALException(ex);
         }
     }
 
     
 }
 //            pstmtInsertTrack = conn.prepareStatement(
 //                    "INSERT IGNORE INTO track "
 //                    + "(disc_id, track_num, title, artist_id, len_in_sec) "
 //                    + "VALUES (?, ?, ?, ?, ?, ?)");
 //TODO: If find any solution for getting all keys from transaction, including non-generated,
 //change to the following code
 //
 //            Multimap<String, HasArtist> mmArtistDataRow = ArrayListMultimap.create();
 //            Multimap<String, HasArtist> mmGenreAlbumRow = ArrayListMultimap.create();
 //
 //
 //            for(XmcdDisc xmcdDisc : xmcdDiscList) {
 //                mmArtistDataRow.put(xmcdDisc.albumRow.artistName, xmcdDisc.albumRow);
 //                mmArtistDataRow.putAll(xmcdDisc.albumRow.artistName, xmcdDisc.trackRows);
 //                mmGenreAlbumRow.put(xmcdDisc.albumRow.freeTextGenre, xmcdDisc.albumRow);
 //            }
 //
 //            for (String artist : mmArtistDataRow.keySet()) {
 //                pstmtInsertArtist.setString(1, artist);
 //                pstmtInsertArtist.addBatch();
 //            }
 //
 //            for (String genre : mmGenreAlbumRow.keySet()) {
 //                pstmtInsertGenre.setString(1, genre);
 //                pstmtInsertGenre.addBatch();
 //            }
 //
 //            pstmtInsertGenre.executeBatch();
 //            pstmtInsertArtist.executeBatch();
 //
 //            ResultSet rsGenreKeys = pstmtInsertGenre.getGeneratedKeys();
 //            ResultSet rsArtistKeys = pstmtInsertGenre.getGeneratedKeys();
 //
 //            for (Entry<String, Collection<HasArtist>> artistItems : mmArtistDataRow.asMap().entrySet()) {
 //
 //                for (HasArtist dataRow : artistItems.getValue()) {
 //                    dataRow.artistId = rsArtistKeys.getInt("id");
 //                }
 //            }
