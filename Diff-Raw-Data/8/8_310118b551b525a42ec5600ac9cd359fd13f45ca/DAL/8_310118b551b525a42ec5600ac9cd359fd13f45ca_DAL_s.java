 package my.triviagame.dal;
 
 import com.google.common.base.Joiner;
 import com.google.common.base.Preconditions;
 import com.google.common.collect.Sets;
 import java.io.IOException;
 import java.sql.*;
 import java.util.*;
 import my.triviagame.xmcd.XmcdDisc;
 import org.apache.commons.io.IOUtils;
 
 public class DAL implements IDAL {
 
     //DB connection
     private Connection conn = null;
     //Prepared Statements for insetions
     private PreparedStatement pstmtInsertGenre = null;
     private PreparedStatement pstmtInsertArtist = null;
     private PreparedStatement pstmtInsertAlbum = null;
     private PreparedStatement pstmtInsertTrackXmcd = null;
     private PreparedStatement pstmtInsertTrackUser = null;
     
     //Statemnets for table counters
     private PreparedStatement pstmtGetTrackTableStatistics = null;
     private PreparedStatement pstmtGetAlbumTableStatistics = null;
     private PreparedStatement pstmtGetArtistTableStatistics = null;
     
     private String strGetBasicAlbums = null;
     private String strGetBasicArtists = null;
     private String strGetBasicTracks = null;
     
     private ITableStatistics trackTableStatistics = null;
     private ITableStatistics albumTableStatistics = null;
     private ITableStatistics artistTableStatistics = null;
        
     private StringBuilder albumKeywords;
     private StringBuilder trackKeywords;        
     
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
      * @throws my.triviagame.dal.DALException
      */
     @Override
     public void openConnection(
             String hostName,
             int port,
             String dbName,
             String userName,
             String password) throws DALException {
         try {
             Class.forName("com.mysql.jdbc.Driver");
         } catch (ClassNotFoundException ex) {
             throw new DALException(ex);
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
             throw new DALException(ex);
         }
     }
 
     @Override
     public void closeConnection() throws DALException {
         try {
             destoryQueryStatements();
             destroyImportStatements();
             conn.close();
         } catch (SQLException ex) {
             throw new DALException(ex);
         }
         conn = null;
     }
 
     @Override
     public int getEstimatedTrackCount() throws DALException {
         return getTrackTableStatistics().getExtimatedRowCount();
     }
 
     @Override
     public int getEstimatedAlbumCount() throws DALException {
         return getAlbumTableStatistics().getExtimatedRowCount();
     }
 
     @Override
     public int getEstimatedArtistCount() throws DALException {
         return getArtistTableStatistics().getExtimatedRowCount();
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
     public List<ITrackDescriptor> getTrackDescriptors(List<Integer> ids) throws DALException {
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
             throws DALException {
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
 
     //TODO: add to documentation when search cannot be done: less than 4 characters, single qoute
     @Override
     public List<ITrackDescriptor> getTrackDescriptors(String... keywords) throws DALException {
         
         StringBuilder keywordsString = new StringBuilder();
         for (String keyword : keywords) {
             keywordsString.append("+").append(keyword.toLowerCase()).append(" ");
         }
         String tailTerm = String.format(
                 "and MATCH (track.title, track.keywords) "
                 + "AGAINST ('%s' IN BOOLEAN MODE) "
                 + "LIMIT 0,500", keywordsString.toString());
         return getTrackDescriptorsByTailTerm(tailTerm);
     }
 
     
     @Override
     public void prepareConstraintsForImport() throws DALException {
         //TODO: remove///////////////////////////////////
         long milis = Calendar.getInstance().getTimeInMillis();
         /////////////////////////////////////////////////
         Statement stmt;
         try {
             stmt = conn.createStatement();
         } catch (SQLException ex) {
             throw new DALException(ex);
         }
         
         //TODO: add check whether index exists
         try {
             stmt.executeUpdate("ALTER TABLE track DROP INDEX in_track_keywords");
         } catch (SQLException ex) {
         }
         
         try {
             stmt.close();
         } catch (SQLException ex) {
             throw new DALException(ex);
         } finally {
             //TODO: remove///////////////////////////////////
             System.out.println(String.format("Finished deleting fulltext index, DURATION: %d",
                 (Calendar.getInstance().getTimeInMillis() - milis)/1000));
             /////////////////////////////////////////////////
         }
     }
 
     @Override
     public void createConstraintsAfterImport() throws DALException {
         //TODO: remove///////////////////////////////////
         long milis = Calendar.getInstance().getTimeInMillis();
         /////////////////////////////////////////////////
         Statement stmt = null;
         try {
             stmt = conn.createStatement();
         } catch (SQLException ex) {
             throw new DALException(ex);
         }
         
         //TODO: add check whether index exists!!
          try {
             stmt.executeUpdate("ALTER TABLE track add FULLTEXT INDEX `in_track_keywords` (`title` ASC, `keywords` ASC)");
         } catch (SQLException ex) {
         }
         try {
             stmt.close();
         } catch (SQLException ex) {
             throw new DALException(ex);
         } finally {
             //TODO: remove///////////////////////////////////
             System.out.println(String.format("Finished Creating fulltext index, DURATION: %d",
                 (Calendar.getInstance().getTimeInMillis() - milis)/1000));
             /////////////////////////////////////////////////
         }
     }
 
     @Override
     public void deleteAlbum(IAlbumDescriptor album) throws DALException{
         try {
             deleteAlbumsByIds(Collections.singleton(album.getId()));
         } catch (SQLException ex) {
             throw new DALException(ex);
         }
     }
 
     @Override
     public void updateAlbum(IAlbumDescriptor album, List<ITrackDescriptor> tracks) throws DALException{
         try {
             //TODO: check whther some inconsistency can happened since inserting tracks without albums
             conn.setAutoCommit(false);
             deleteAlbum(album);       
             createAlbum(album, tracks);
             conn.setAutoCommit(false);
         } catch (SQLException ex) {
             throw new DALException(ex);
         }
         
     }
 
     @Override
     public void createAlbum(IAlbumDescriptor album, List<ITrackDescriptor> tracks) throws DALException {
         try {
             conn.setAutoCommit(false);
             insertUserAlbumData(album, tracks);
             conn.commit();
             conn.setAutoCommit(true);
         } catch (SQLException ex) {
             throw new DALException(ex);
         }
     }
     
     @Override
     public void importXmcdBatch(List<XmcdDisc> xmcdDiscList) throws DALException {
         
         //TODO: remove///////////////////////////////////
         long milis = Calendar.getInstance().getTimeInMillis();
         /////////////////////////////////////////////////
 
         Map<Integer, XmcdDisc> freedbToXmcd = new HashMap<Integer, XmcdDisc>();
         Set<String> uniqueGenre = Sets.newHashSet();
         Set<String> uniqueArtist = Sets.newHashSet();
         
         albumKeywords = new StringBuilder();
         trackKeywords = new StringBuilder();        
         
         try {
             conn.setAutoCommit(false);
                         
             for (XmcdDisc xmcdDisc : xmcdDiscList) {
                 freedbToXmcd.put(xmcdDisc.albumRow.freedbId, xmcdDisc);
             }
 
             deleteOldDiscs(freedbToXmcd);
             
             for (XmcdDisc xmcdDisc : freedbToXmcd.values()) {
                 addAlbumData(xmcdDisc.albumRow, xmcdDisc.trackRows, uniqueGenre, uniqueArtist);
             }
             for (String genre : uniqueGenre) {
                 pstmtInsertGenre.setString(1, genre);
                 pstmtInsertGenre.addBatch();
             }
             for (String artist : uniqueArtist) {
                 pstmtInsertArtist.setString(1, artist);
                 pstmtInsertArtist.addBatch();
             }
             
             executeInsertionBatches();
             
             conn.commit();
             conn.setAutoCommit(true);
 
         } catch (SQLException ex) {
             throw new DALException(ex);
         }
         //TODO: remove///////////////////////////////////
         System.out.println(String.format("Finished Batch, DURATION: %d, Albums: %d, Artists: %d, Genres: %d",
                 (Calendar.getInstance().getTimeInMillis() - milis)/1000, 
                 freedbToXmcd.keySet().size(),
                 uniqueArtist.size(),
                 uniqueGenre.size()
                 ));
         /////////////////////////////////////////////////
         
     }
     
     ///////////////////////////////////////////////////////////////////////////////////////////////
     
     /////////////////////////////////////////////
     // GENERAL HELPERS
     /////////////////////////////////////////////
     private String getResourceFileContents(String fileName) throws IOException {
         return IOUtils.toString(getClass().getResourceAsStream(fileName));
     }
     
     private void destroyPreparedStatement(PreparedStatement pstmt) throws SQLException {
         if (pstmt != null) {
             pstmt.close();
             pstmt = null;
         }
     }
     
     
     /////////////////////////////////////////////
     // IMPORT HELPERS
     /////////////////////////////////////////////
 
     /**
      * Deletes all albums from DB whom id's matches albumIds, and their matchins track
      * @param albumIds ids of albums to delete
      * @throws SQLException 
      */
     private void deleteAlbumsByIds(Collection<Integer> albumIds) throws SQLException {
         Statement stmtDeleteOldDiscs = null;
         try {
             String discTerm = Joiner.on(",").join(albumIds);
             
             //Delete old discs
             String deleteDiscQuery = String.format(
                 "DELETE FROM disc WHERE id in (%s)", discTerm);
             stmtDeleteOldDiscs = conn.createStatement();
             stmtDeleteOldDiscs.executeUpdate(deleteDiscQuery);
 
             //Delete old tracks
             String deleteTrackQuery = String.format(
                 "DELETE FROM track WHERE disc_id in (%s)", discTerm);
             stmtDeleteOldDiscs.executeUpdate(deleteTrackQuery);
         } catch (SQLException ex) {
             throw ex;
         } finally {
             try {
                 stmtDeleteOldDiscs.close();
             } catch (SQLException ex1) {
                 throw ex1;
             }
         }
         
     }
     
     private void deleteOldDiscs(Map<Integer, XmcdDisc> freedbToXmcd) throws SQLException{
         
         Set<Integer> albumIdsToDelete = Sets.newHashSet();
         
         ResultSet rsMatchingDiscs = null;
         Statement stmtDeleteOldDiscs = null;
         try {
             stmtDeleteOldDiscs = conn.createStatement();
             
             String inTerm = Joiner.on(",").join(freedbToXmcd.keySet());
             String findQuery = String.format(
                     "SELECT id, freedb_id, revision FROM disc WHERE freedb_id in (%s)", inTerm);
             rsMatchingDiscs = stmtDeleteOldDiscs.executeQuery(findQuery);
 
             //Find which discs to delete and which discs are new
             while (rsMatchingDiscs.next()) {
                 int curFreedb = rsMatchingDiscs.getInt("freedb_id");
                 XmcdDisc newDisc = freedbToXmcd.get(curFreedb);
                 if (newDisc != null) {
                     int curRevision = rsMatchingDiscs.getInt("revision");
                     if (curRevision >= newDisc.albumRow.revision) {
                         //disc is of stale or same revision, ignore it
                         freedbToXmcd.remove(curFreedb);
                     } else {
                         //update disc is of newer revision - add exisiting revision to delete list
                         albumIdsToDelete.add(rsMatchingDiscs.getInt("id"));
                     }
                 }
             }
             if (!albumIdsToDelete.isEmpty()) {
                 //Delete all old discs
                 deleteAlbumsByIds(albumIdsToDelete);
             }
             
         } catch (SQLException ex) {
             throw ex;
         } finally {
             try {
                 rsMatchingDiscs.close();
                 stmtDeleteOldDiscs.close();
             } catch (SQLException ex) {
                 throw ex;
             }
         }
     }
     
     /**
      * Adds album data to pstmtAlbumBatch
      * @param album the album to add
      * @throws SQLException 
      */
     
     private void addAlbumToBatch(IAlbumDescriptor album) throws SQLException{
         if (album.hasFreedbId()) {
             pstmtInsertAlbum.setInt(1, album.getFreedbId());
             pstmtInsertAlbum.setInt(5, album.getFreedbGenreIndex());
         } else {
             pstmtInsertAlbum.setNull(1, java.sql.Types.INTEGER);
             pstmtInsertAlbum.setNull(5, java.sql.Types.INTEGER);
         }
         pstmtInsertAlbum.setString(2, album.getTitle());
         pstmtInsertAlbum.setInt(3, album.getYear());
         pstmtInsertAlbum.setString(4, album.getGenre());
         pstmtInsertAlbum.setString(6, album.getArtistName());
         pstmtInsertAlbum.setInt(7, album.getRevision());
         pstmtInsertAlbum.addBatch();
     }
     
     /**
      * Adds ITrackDescriptor to pstmt batch.
      * @param pstmt the insertion statement to add. Must be one of: pstmtInsertTrackXmcd, pstmtInsertTrackUser
      * @param track the track to add
      * @param discIdParam the id to insert to the preparedStatement
      * @throws SQLException
      */
     private void addTrackToBatch(
             PreparedStatement pstmt, 
             ITrackDescriptor track, 
             int discIdParam, String trackKeywords) throws SQLException {
         
         pstmt.setInt(1, discIdParam);
         pstmt.setInt(2, track.getTrackNum());
         pstmt.setString(3, track.getArtistName());
         pstmt.setInt(4, track.getLengthInSeconds());                   
         pstmt.setString(5, track.getTitle());
         pstmt.setString(6, trackKeywords);
         pstmt.addBatch();
     }
     
     /**
      * Inserts user disc to DB. Flow here is different that flow in import xmcdDisc, since freedbId might not exist.
      * Therefore, first Needs to insert the disc, then find out the generated key, and only later insert the track
      * @param album the album to add
      * @param tracks
      * @param uniqueGenre
      * @param uniqueArtist
      * @throws DALException
      * @throws SQLException 
      */
     
     private void insertUserAlbumData(
             IAlbumDescriptor album, 
             List<ITrackDescriptor> tracks) throws DALException, SQLException{
 
         albumKeywords = new StringBuilder();
         trackKeywords = new StringBuilder();
         albumKeywords.append(album.getArtistName().toLowerCase()).append(" ");
         albumKeywords.append(album.getTitle().toLowerCase()).append(" ");                
         
         Set<String> uniqueArtist = Sets.newHashSet();
         uniqueArtist.add(album.getArtistName());
 
         addAlbumToBatch(album);
 
         for (ITrackDescriptor track : tracks) {
             uniqueArtist.add(track.getArtistName());
         }
 
         for (String artist : uniqueArtist) {
             pstmtInsertArtist.setString(1, artist);
             pstmtInsertArtist.addBatch();
         }
 
         pstmtInsertGenre.setString(1, album.getGenre());
         pstmtInsertGenre.addBatch();
 
         //can insert all data except for track, as disc id is not known until disc is inserted
         pstmtInsertArtist.executeBatch();
         pstmtInsertGenre.executeBatch();
         pstmtInsertAlbum.executeBatch();
         ResultSet rsDiscId = pstmtInsertAlbum.getGeneratedKeys();
         rsDiscId.next();
         int discId = rsDiscId.getInt(1);
 
         for (ITrackDescriptor track : tracks) {
             trackKeywords.setLength(0);
             trackKeywords.append(albumKeywords);
             if (!track.getArtistName().toLowerCase().equals(album.getArtistName())) {
                 trackKeywords.append(track.getArtistName().toLowerCase());
             }
             addTrackToBatch(pstmtInsertTrackUser, track, discId, trackKeywords.toString());
         }
 
         pstmtInsertTrackUser.executeBatch();
     }
     
     
     /**
      * Adds album data to the Insertion prepared statements.
      * Artist and genre are kept in unique sets in memory and added outside the method
      * for efficient duplicates handling
      * in memory and not in DB.
      * @param album
      * @param tracks
      * @throws DALException
      * @throws SQLException 
      */
     
     private void addAlbumData(
             IAlbumDescriptor album, 
             List<? extends ITrackDescriptor> tracks, 
             Set<String> uniqueGenre, 
             Set<String> uniqueArtist) throws DALException, SQLException{
         
         try {
 
             albumKeywords.setLength(0);
             albumKeywords.append(album.getArtistName().toLowerCase()).append(" ");
             albumKeywords.append(album.getTitle().toLowerCase()).append(" ");                
 
             uniqueGenre.add(album.getGenre());
             uniqueArtist.add(album.getArtistName());
 
             addAlbumToBatch(album);
 
             for (ITrackDescriptor track : tracks) {
 
                 trackKeywords.setLength(0);
                 trackKeywords.append(albumKeywords);
                 if (!track.getArtistName().toLowerCase().equals(album.getArtistName())) {
                     trackKeywords.append(track.getArtistName().toLowerCase());
                 }
 
                 uniqueArtist.add(track.getArtistName());
                 addTrackToBatch(pstmtInsertTrackXmcd, track, album.getFreedbId(), trackKeywords.toString());
 
             }
         } catch (SQLException ex) {
             throw ex;
         }
     }
     
     private void executeInsertionBatches() throws SQLException {
         pstmtInsertGenre.executeBatch();
         pstmtInsertArtist.executeBatch();
         pstmtInsertAlbum.executeBatch();
         pstmtInsertTrackXmcd.executeBatch();
     }
     
     private void prepareImportStatements() throws DALException {
         try {
 
             //usage of "ON DUPLICATE KEY UPDATE" instead of "INSERT IGNORE".
             //When using "INSERT IGNORE", some insertion errors would have been silenced
             //TODO: remove "Statement.RETURN_GENERATED_KEYS"
             pstmtInsertGenre = conn.prepareStatement(
                     "INSERT INTO genre(name) VALUES(?)"
                     + "ON DUPLICATE KEY UPDATE id=id");
             pstmtInsertArtist = conn.prepareStatement(
                     "INSERT INTO artist(name) VALUES(?)"
                     + "ON DUPLICATE KEY UPDATE id=id");
 
             pstmtInsertAlbum = conn.prepareStatement(
                     "INSERT INTO disc "
                     + "(freedb_id, title, year, genre_id, freedb_genre_index, artist_id, revision) "
                     + "VALUES ("
                     + "?, " //freedb_id
                     + "?, " //title
                     + "?, " //year
                     + "(SELECT id FROM genre WHERE genre.name = ? LIMIT 1), " //genre_name
                     + "?, " //freedb_genre_index
                     + "(SELECT id FROM artist WHERE artist.name = ? LIMIT 1), " //aritst_name
                     + "?)" //revision
                     , Statement.RETURN_GENERATED_KEYS);
             
             pstmtInsertTrackXmcd = conn.prepareStatement(
                     "INSERT INTO track "
                     //TODO: decide if necessary
                     //"INSERT DELAYED INTO track "
                     + "(disc_id, track_num, artist_id, len_in_sec, title, keywords) "
                     + "VALUES ("
                     + "(SELECT id FROM disc WHERE disc.freedb_id = ? LIMIT 1) , " //freedb_db_id
                     + "?, " //track_num
                     + "(SELECT id FROM artist WHERE artist.name = ? LIMIT 1), " //artist_name
                     + "?, " //len_in_sec
                     + "?, " //title
                     + "?)" //keywords
                     );
             
             pstmtInsertTrackUser = conn.prepareStatement(
                     "INSERT INTO track "
                     + "(disc_id, track_num, artist_id, len_in_sec, title, keywords) "
                     + "VALUES ("
                     + "?, " //disc_id, 
                     + "?, " //track_num
                     + "(SELECT id FROM artist WHERE artist.name = ? LIMIT 1), " //artist_name
                     + "?, " //len_in_sec
                     + "?, " //title
                     + "?)", //keywords
                     Statement.RETURN_GENERATED_KEYS);
                         
         } catch (SQLException ex) {
             throw new DALException(ex);
         }
     }
 
     private void destroyImportStatements() throws DALException {
         try {
             destroyPreparedStatement(pstmtInsertGenre);
             destroyPreparedStatement(pstmtInsertArtist);
             destroyPreparedStatement(pstmtInsertAlbum);
             destroyPreparedStatement(pstmtInsertTrackUser);
             destroyPreparedStatement(pstmtInsertTrackXmcd);
         } catch (SQLException ex) {
             throw new DALException(ex);
         }
     }
 
     /////////////////////////////////////////////
     // STATISTICS HELPERS
     /////////////////////////////////////////////
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
             throw new DALException(ex);
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
 
     
     /////////////////////////////////////////////
     // QUERY HELPERS
     /////////////////////////////////////////////
     
     private void readResourceSqlFiles() throws IOException {
         strGetBasicTracks = getResourceFileContents("resources/get_tracks_by_text_basic.sql");
         strGetBasicAlbums = getResourceFileContents("resources/get_albums_basic.sql");
         strGetBasicArtists = getResourceFileContents("resources/get_artists_basic.sql");
         strGetBasicTracks = getResourceFileContents("resources/get_tracks_by_text_basic.sql");
     }
 
     private void prepareQueryStatements() throws DALException {
                 
         try {
 
             String statsQueryFormat = 
                 "SELECT min(id) as min_id, max(id) as max_id FROM %s "
                 + "UNION "
                 + "SELECT TABLE_ROWS, TABLE_ROWS "
                 + "from information_schema.tables "
                 + "WHERE table_schema = '" + conn.getCatalog() + "' and table_name = '%s'";
             
             pstmtGetTrackTableStatistics = conn.prepareStatement(
                     String.format(statsQueryFormat, "track", "track"));
 
             pstmtGetAlbumTableStatistics = conn.prepareStatement(
                     String.format(statsQueryFormat, "disc", "disc"));
 
             pstmtGetArtistTableStatistics = conn.prepareStatement(
                     String.format(statsQueryFormat, "artist", "artist"));
 
         } catch (SQLException ex) {
             throw new DALException(ex);
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
     
     /**
      * 
      * @param tailTerm an SQL term 
      * @return List of tracks that satisfies the tail term
      * @throws DALException 
      */
     private List<ITrackDescriptor> getTrackDescriptorsByTailTerm(String tailTerm) throws DALException {
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
             throw new DALException(ex);
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
 
     private List<IAlbumDescriptor> getAlbumDescriptorsByTailTerm(String tailTerm) throws DALException {
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
             throw new DALException(ex);
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
 
     private List<IArtistDescriptor> getArtistDescriptorsByTailTerm(String tailTerm) throws DALException {
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
             throw new DALException(ex);
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
 
 }
