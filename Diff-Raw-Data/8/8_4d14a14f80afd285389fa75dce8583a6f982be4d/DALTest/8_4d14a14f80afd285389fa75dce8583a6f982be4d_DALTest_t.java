 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package my.triviagame.dal;
 
 import java.io.File;
 import java.sql.Connection;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.Statement;
 import java.util.*;
 import my.triviagame.xmcd.*;
 import org.apache.commons.io.FileUtils;
 import org.junit.Assert;
 import org.junit.BeforeClass;
 import org.junit.Test;
 
 public class DALTest {
 
     static final String SCHEMA_NAME = "cd_db_test";
     static final String USER_NAME = "root";
     
    static final String HOST_NAME = "telaviv.rzn.co.il";
    static final String PASSWORD = "d@+a8a$3";    
//    static final String HOST_NAME = "localhost";
//    static final String PASSWORD = "1234";   
     
     /**
      * @post : 
      * 1. All db tables exist. 
      * 2. There are 10 discs in disc table.
      * 
      * @throws Throwable 
      */
     
     @BeforeClass
     public static void createCdDbTest() throws Throwable {
         DAL myDal = new DAL();
         myDal.openConnection(HOST_NAME, 3306, SCHEMA_NAME, USER_NAME, PASSWORD);
         Connection conn = myDal.getConnection();
         Statement stmt = conn.createStatement();
         
         String fileContent = FileUtils.readFileToString(FileUtils.toFile(
                 DALTest.class.getResource("resources/create_cd_db2_test.sql")));
         conn.setAutoCommit(false);
         String[] strStmts = fileContent.split(";");
         for (String strStmt: strStmts) {
             strStmt = strStmt.replace("\r\n","");
             if (strStmt.startsWith("--") || strStmt.equals("")) {
                 continue;
             }
             stmt.addBatch(strStmt);
         }
         stmt.executeBatch();
         stmt.close();
         conn.commit();
         conn.setAutoCommit(true);
         
         File freedb_update_20120401_20120501 = FileUtils.toFile(
                 DALTest.class.getResource("resources/freedb-update-20120401-20120501.tar.bz2"));
         XmcdDiscArchive allDiscs = new XmcdDiscArchive(freedb_update_20120401_20120501);
         
         Iterator<XmcdDisc> toImport = new XmcdFilters.Factory(allDiscs)
                 .stripTrackVariant()
                 .notVarious()
                 .hasYear()
                 .firstN(10)
                 .chain();
         
         XmcdImporter importer = new XmcdImporter(myDal);
         importer.importFreedb(toImport);
         allDiscs.close();
         
         myDal.closeConnection();
     }
     
     @Test
     public void testConnection() throws Throwable {
         IDAL myDal = new DAL();
         myDal.openConnection(HOST_NAME, 3306, SCHEMA_NAME, USER_NAME, PASSWORD);
         myDal.closeConnection();
     }
     
     @Test
     public void testTableStatistics() throws Throwable {
         IDAL myDal = new DAL();
         myDal.openConnection(HOST_NAME, 3306, SCHEMA_NAME, USER_NAME, PASSWORD);
         
         ITableStatistics stsTrack = myDal.getTrackTableStatistics();
         ITableStatistics stsAlbum = myDal.getAlbumTableStatistics();
         ITableStatistics stsArtist = myDal.getArtistTableStatistics();
         
         System.out.println(stsTrack.toString());
         System.out.println(stsArtist.toString());
         
         //since import filters may chage, can trust only album statistics
         Assert.assertEquals(10, stsAlbum.getMaxId());
         Assert.assertEquals(1, stsAlbum.getMinId());
         Assert.assertEquals(10, stsAlbum.getExtimatedRowCount());
                         
         myDal.closeConnection();
     }
 
     @Test
     public void testImportMultipleRevisions() throws Throwable {
         DAL myDal = new DAL();
         myDal.openConnection(HOST_NAME, 3306, SCHEMA_NAME, USER_NAME, PASSWORD);
         
         String fileNames[] = {
             "resources/000cab12_rev8",
             "resources/000cab12_rev6",
             "resources/000eb313_rev1",
             "resources/000eb313_rev3"};
         
         for (String fileName: fileNames) {
             String fileContents = FileUtils.readFileToString(FileUtils.toFile(
                 getClass().getResource(fileName)));
             XmcdDisc xmcdDisc = XmcdDisc.fromXmcdFile(fileContents, FreedbGenre.NEWAGE);
             myDal.importXmcdBatch(Collections.singletonList(xmcdDisc));
         }
         
         String[] highRevisionFileNames = {"resources/000cab12_rev8","resources/000eb313_rev3"};
         
         Connection conn = myDal.getConnection();
         PreparedStatement pstmtDisc = conn.prepareStatement("SELECT * FROM disc WHERE freedb_id = ?");
         PreparedStatement pstmtTrack = conn.prepareStatement("SELECT COUNT(*) FROM track WHERE disc_id = ?");
         
         for (String fileName: highRevisionFileNames) {
             String fileContents = FileUtils.readFileToString(FileUtils.toFile(
                 getClass().getResource(fileName)));
             XmcdDisc xmcdDisc = XmcdDisc.fromXmcdFile(fileContents, FreedbGenre.NEWAGE);
             
             pstmtDisc.setInt(1, xmcdDisc.albumRow.freedbId);
             ResultSet rsDisc = pstmtDisc.executeQuery();
             rsDisc.next();
             Assert.assertEquals(xmcdDisc.albumRow.revision, rsDisc.getInt("revision"));
             
             pstmtTrack.setInt(1, rsDisc.getInt("id"));
             ResultSet rsTrack = pstmtTrack.executeQuery();
             rsTrack.next();
             Assert.assertEquals(xmcdDisc.trackRows.size(), rsTrack.getInt("COUNT(*)"));
             
         }
         
         pstmtDisc.close();
         pstmtTrack.close();
         
         myDal.closeConnection();
 
     }
     
     @Test
     public void testTrackDescriptor() throws Throwable {
         IDAL myDal = new DAL();
         myDal.openConnection(HOST_NAME, 3306, SCHEMA_NAME, USER_NAME, PASSWORD);
         System.out.println(myDal.getTrackDescriptors(Arrays.asList(new Integer[]{1})));
         myDal.closeConnection();
     }
     
     @Test
     public void testSearchTracks() throws Throwable {
         IDAL myDal = new DAL();
         myDal.openConnection(HOST_NAME, 3306, SCHEMA_NAME, USER_NAME, PASSWORD);
         System.out.println(myDal.getTrackDescriptors("rock"));
         myDal.closeConnection();
     }
 
     @Test
     public void testAlbumDescriptor() throws Throwable {
         IDAL myDal = new DAL();
         myDal.openConnection(HOST_NAME, 3306, SCHEMA_NAME, USER_NAME, PASSWORD);
         List<IAlbumDescriptor> albumList = myDal.getAlbumDescriptors(Arrays.asList(new Integer[]{1,2}));
         Assert.assertTrue(albumList.size() == 2);
         List<ITrackDescriptor> trackList = myDal.getAlbumTrackDescriptors(albumList.get(0));
         System.out.println(trackList.get(0));
         myDal.closeConnection();
     }
 
     @Test
     public void testArtistDescriptor() throws Throwable {
         IDAL myDal = new DAL();
         myDal.openConnection(HOST_NAME, 3306, SCHEMA_NAME, USER_NAME, PASSWORD);
         List<IArtistDescriptor> artistList = myDal.getArtistDescriptors(Arrays.asList(new Integer[]{1,2,3}));
         Assert.assertTrue(artistList.size() == 3);
         myDal.closeConnection();
     }
     
 }
