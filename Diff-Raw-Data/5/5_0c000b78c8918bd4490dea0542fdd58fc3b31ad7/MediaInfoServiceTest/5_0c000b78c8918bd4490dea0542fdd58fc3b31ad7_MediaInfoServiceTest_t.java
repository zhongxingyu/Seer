 package dk.statsbiblioteket.mediaplatform.bes.mediafilelog.batch;
 
 import static org.junit.Assert.*;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.IOException;
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.Collections;
 import java.util.Date;
 import java.util.List;
 import java.util.Properties;
 
 import org.apache.log4j.Logger;
 import org.apache.log4j.xml.DOMConfigurator;
 import org.hibernate.SessionFactory;
 import org.junit.After;
 import org.junit.Before;
 import org.junit.Test;
 
 import dk.statsbiblioteket.mediaplatform.bes.mediafilelog.batch.db.Metadata;
 import dk.statsbiblioteket.mediaplatform.bes.mediafilelog.batch.db.PreviewMediaInfo;
 import dk.statsbiblioteket.mediaplatform.bes.mediafilelog.batch.db.ProgramMediaInfo;
 import dk.statsbiblioteket.mediaplatform.bes.mediafilelog.batch.db.SnapshotMediaInfo;
 import dk.statsbiblioteket.mediaplatform.bes.mediafilelog.batch.extraction.DOMSMetadataExtractor;
 import dk.statsbiblioteket.mediaplatform.bes.mediafilelog.batch.extraction.exception.DOMSMetadataExtractionConnectToDOMSException;
 import dk.statsbiblioteket.mediaplatform.bes.mediafilelog.batch.extraction.model.BESClippingConfiguration;
 import dk.statsbiblioteket.mediaplatform.bes.mediafilelog.batch.extraction.model.MediaTypeEnum;
 
 public class MediaInfoServiceTest {
 
     private static final String DATE_PATTERN = "yyyy-MM-dd HH:mm:ss";
     private static final SimpleDateFormat sdf = new SimpleDateFormat(DATE_PATTERN);
 
     private final Logger log = Logger.getLogger(MediaInfoServiceTest.class);
     private final Properties properties;
 
     private final String shardUuid = "uuid:d93054ed-858d-4b2a-870e-b929f5352ad6";//"uuid:abcd786a-73bb-412b-a4c7-433d5fe62d94";
     private final String programMediaFileRelativePath = "src/test/resources/testfiles/programDirectory/d/9/3/0/d93054ed-858d-4b2a-870e-b929f5352ad6.flv";
     private final String previewMediaFileRelativePath = "src/test/resources/testfiles/previewDirectory/d/9/3/0/d93054ed-858d-4b2a-870e-b929f5352ad6.preview.flv";
     private final String[] snapshotMediaFileRelativePath = {
             "src/test/resources/testfiles/snapshotDirectory/d/9/3/0/d93054ed-858d-4b2a-870e-b929f5352ad6.snapshot.0.jpeg",
             "src/test/resources/testfiles/snapshotDirectory/d/9/3/0/d93054ed-858d-4b2a-870e-b929f5352ad6.snapshot.1.jpeg",
             "src/test/resources/testfiles/snapshotDirectory/d/9/3/0/d93054ed-858d-4b2a-870e-b929f5352ad6.snapshot.2.jpeg",
             "src/test/resources/testfiles/snapshotDirectory/d/9/3/0/d93054ed-858d-4b2a-870e-b929f5352ad6.snapshot.3.jpeg",
             "src/test/resources/testfiles/snapshotDirectory/d/9/3/0/d93054ed-858d-4b2a-870e-b929f5352ad6.snapshot.4.jpeg",
             "src/test/resources/testfiles/snapshotDirectory/d/9/3/0/d93054ed-858d-4b2a-870e-b929f5352ad6.snapshot.preview.0.jpeg"
             };
     private final int[] snapshotMediaFileSize = {16192, 13864, 19414, 12396, 9097, 14203};
     
     private SessionFactory hibernateSessionFactory;
     public MediaInfoServiceTest() throws IOException {
         super();
         File propertyFile = new File("src/test/config/bes_media_file_log_batch_update_unittest.properties");
         FileInputStream in = new FileInputStream(propertyFile);
         properties = new Properties();
         properties.load(in);
         in.close();
         System.getProperties().put("log4j.defaultInitOverride", "true");
         DOMConfigurator.configure(properties.getProperty("log4j.config.file.path"));
     }
 
     @Before
     public void setUp() throws Exception {
         String hibernateConfigFilePath = properties.getProperty("hibernate.config.file.path");
         this.hibernateSessionFactory = HibernateSessionFactoryFactory.create(hibernateConfigFilePath);
     }
 
     @After
     public void tearDown() throws Exception {
     }
 
     @Test
     public void retrieveMetadata() throws DOMSMetadataExtractionConnectToDOMSException, ParseException {
         Date testStartedDate = new Date();
         MediaInfoService mediaInfoService = new MediaInfoService(
                 new DOMSMetadataExtractor(properties), 
                 new BESClippingConfiguration(properties), 
                 new MediaInfoDAO(hibernateSessionFactory));
         //String uuid = "uuid:d93054ed-858d-4b2a-870e-b929f5352ad6";
         Metadata metadata = mediaInfoService.retrieveMetadata(shardUuid);
         log.info("Retrieved metadata: " + metadata);
         
         Date now = new Date();
         log.debug("Date started: " + testStartedDate.getTime());
         log.debug("Date changed: " + metadata.getLastChangedDate().getTime());
         log.debug("Date now    : " + now.getTime());
         
         assertTrue("Expecting changed date to be recent.", testStartedDate.getTime() <= metadata.getLastChangedDate().getTime() && metadata.getLastChangedDate().getTime() <= now.getTime());
         assertEquals(shardUuid,metadata.getShardUuid());
         //mediaType=null, Note=null]
         String expectedProgramUuid = "uuid:ce6ee9ca-c077-4627-ac74-151b31da981c";
         assertEquals(expectedProgramUuid,metadata.getProgramUuid());
         String expectedSbChannelId = "";
         assertEquals(expectedSbChannelId,metadata.getSbChannelID());
         String expectedChannelId = "drhd";
         assertEquals(expectedChannelId,metadata.getChannelID());
         String expectedTitle = "Flykatastrofer";
         assertEquals(expectedTitle,metadata.getProgramTitle());
         Date expectedRitzauStartTime = sdf.parse("2010-10-18 18:25:00");
         assertEquals(expectedRitzauStartTime, metadata.getRitzauStartTime());
         Date expectedRitzauEndTime = sdf.parse("2010-10-18 19:15:00");
         assertEquals(expectedRitzauEndTime, metadata.getRitzauEndTime());
         String expectedNote = "First batch extraction.";
         assertEquals(expectedNote, metadata.getNote());
     }
     
     @Test
     public void inferFilePathForPrograms() {
         MediaInfoService mediaInfoService = new MediaInfoService(
                 new DOMSMetadataExtractor(properties), 
                 new BESClippingConfiguration(properties), 
                 new MediaInfoDAO(hibernateSessionFactory));
         //String shardUuid = "uuid:abcd786a-73bb-412b-a4c7-433d5fe62d94";
         String filePath = mediaInfoService.inferFilePathForProgram(shardUuid);
         String expectedFilePath = new File (programMediaFileRelativePath).getAbsolutePath();
         log.debug("Actual filepath: " + filePath);
         log.debug("Expected filepath: " + expectedFilePath);
         assertEquals(expectedFilePath, filePath);
     }
 
     @Test
     public void retrieveProgramMediaInfo() throws DOMSMetadataExtractionConnectToDOMSException, ParseException {
         Date testStartedDate = new Date();
         MediaInfoService mediaInfoService = new MediaInfoService(
                 new DOMSMetadataExtractor(properties), 
                 new BESClippingConfiguration(properties), 
                 new MediaInfoDAO(hibernateSessionFactory));
         ProgramMediaInfo programMediaInfo = mediaInfoService.retrieveProgramMediaInfo(shardUuid);
         log.info("Retrieved programMediaInfo: " + programMediaInfo);
         assertTrue("Expecting changed date to be recent.", testStartedDate.getTime() <= programMediaInfo.getLastTouched().getTime() && programMediaInfo.getLastTouched().getTime() <= new Date().getTime());
         assertEquals(shardUuid,programMediaInfo.getShardUuid());
         
         boolean fileExist = programMediaInfo.isFileExists();
         boolean expectedFileExist = true;
         assertEquals(expectedFileExist, fileExist);
         
         MediaTypeEnum mediaTypeEnum = programMediaInfo.getMediaType();
         MediaTypeEnum expectedMediaTypeEnum = MediaTypeEnum.FLV;
         assertEquals(expectedMediaTypeEnum, mediaTypeEnum);
         
         long fileSize = programMediaInfo.getFileSizeByte();
         long expectedFileSize = 196189401;
         assertEquals(expectedFileSize, fileSize);
         
         Date lastChanged = programMediaInfo.getFileTimestamp();
         Date expectedLastChanged = new Date(new File(programMediaFileRelativePath).lastModified());
         assertEquals(expectedLastChanged, lastChanged);
         
         int startOffset = programMediaInfo.getStartOffset();
         int expectedStartOffset = 0;// Unknown when looking isolated at a cached file 
         assertEquals(expectedStartOffset, startOffset);
 
         int endOffset = programMediaInfo.getEndOffset();
         int expectedEndOffset = 0;// Unknown when looking isolated at a cached file
         assertEquals(expectedEndOffset, endOffset);
 
         int lengthInSeconds = programMediaInfo.getLengthInSeconds();
        int expectedLengthInSeconds = 0;
         assertEquals(expectedLengthInSeconds, lengthInSeconds);
 
         long fileSizeInBytes = programMediaInfo.getExpectedFileSizeByte();
         long expectedFileSizeInBytes = 0; // Unknown when looking isolated at a cached file
         assertEquals(expectedFileSizeInBytes, fileSizeInBytes);
 
         String tcl = programMediaInfo.getTranscodeCommandLine();
         String expectedTCL = "N/A";
         assertEquals(expectedTCL, tcl);
         
         String note = programMediaInfo.getNote();
         String expectedNote = "First batch extraction.";
         assertEquals(expectedNote, note);
 
     }
 
     @Test
     public void inferFilePathForPreviews() {
         MediaInfoService mediaInfoService = new MediaInfoService(
                 new DOMSMetadataExtractor(properties), 
                 new BESClippingConfiguration(properties), 
                 new MediaInfoDAO(hibernateSessionFactory));
         String filePath = mediaInfoService.inferFilePathForPreview(shardUuid);
         String expectedFilePath = new File (previewMediaFileRelativePath).getAbsolutePath();
         log.debug("Actual filepath: " + filePath);
         log.debug("Expected filepath: " + expectedFilePath);
         assertEquals(expectedFilePath, filePath);
     }
 
     @Test
     public void retrievePreviewMediaInfo() throws DOMSMetadataExtractionConnectToDOMSException, ParseException {
         Date testStartedDate = new Date();
         MediaInfoService mediaInfoService = new MediaInfoService(
                 new DOMSMetadataExtractor(properties), 
                 new BESClippingConfiguration(properties), 
                 new MediaInfoDAO(hibernateSessionFactory));
         PreviewMediaInfo previewMediaInfo = mediaInfoService.retrievePreviewMediaInfo(shardUuid);
         log.info("Retrieved programMediaInfo: " + previewMediaInfo);
         assertTrue("Expecting changed date to be recent.", testStartedDate.getTime() <= previewMediaInfo.getLastTouched().getTime() && previewMediaInfo.getLastTouched().getTime() <= new Date().getTime());
         assertEquals(shardUuid,previewMediaInfo.getShardUuid());
         
         boolean fileExist = previewMediaInfo.isFileExists();
         boolean expectedFileExist = true;
         assertEquals(expectedFileExist, fileExist);
         
         MediaTypeEnum mediaTypeEnum = previewMediaInfo.getMediaType();
         MediaTypeEnum expectedMediaTypeEnum = MediaTypeEnum.FLV;
         assertEquals(expectedMediaTypeEnum, mediaTypeEnum);
         
         long fileSize = previewMediaInfo.getFileSizeByte();
         long expectedFileSize = 1832417;
         assertEquals(expectedFileSize, fileSize);
         
         Date lastChanged = previewMediaInfo.getFileTimestamp();
         Date expectedLastChanged = new Date(new File(previewMediaFileRelativePath).lastModified());
         assertEquals(expectedLastChanged, lastChanged);
         
         int startOffset = previewMediaInfo.getStartOffset();
         int expectedStartOffset = 0;// Unknown when looking isolated at a cached file 
         assertEquals(expectedStartOffset, startOffset);
     
         int endOffset = previewMediaInfo.getEndOffset();
         int expectedEndOffset = 0;// Unknown when looking isolated at a cached file
         assertEquals(expectedEndOffset, endOffset);
     
         int lengthInSeconds = previewMediaInfo.getLengthInSeconds();
        int expectedLengthInSeconds = 0; //
         assertEquals(expectedLengthInSeconds, lengthInSeconds);
     
         long fileSizeInBytes = previewMediaInfo.getExpectedFileSizeByte();
         long expectedFileSizeInBytes = 0; // Unknown when looking isolated at a cached file
         assertEquals(expectedFileSizeInBytes, fileSizeInBytes);
     
         String tcl = previewMediaInfo.getTranscodeCommandLine();
         String expectedTCL = "N/A";
         assertEquals(expectedTCL, tcl);
         
         String note = previewMediaInfo.getNote();
         String expectedNote = "First batch extraction.";
         assertEquals(expectedNote, note);
     }
     
     @Test
     public void inferFilePathForSnapshots() {
         MediaInfoService mediaInfoService = new MediaInfoService(
                 new DOMSMetadataExtractor(properties), 
                 new BESClippingConfiguration(properties), 
                 new MediaInfoDAO(hibernateSessionFactory));
         List<String> filePaths = mediaInfoService.inferFilePathForSnapshots(shardUuid);
         Collections.sort(filePaths);
         for (int i=0; i<filePaths.size();i++) {
             String filePath = filePaths.get(i);
             String expectedFilePath = new File (snapshotMediaFileRelativePath[i]).getAbsolutePath();
             log.debug("Actual filepath: " + filePath);
             log.debug("Expected filepath: " + expectedFilePath);
             assertEquals(expectedFilePath, filePath);
         }
     }
 
     @Test
     public void retrieveSnapshotMediaInfoSnapshotsExist() throws DOMSMetadataExtractionConnectToDOMSException, ParseException {
         Date testStartedDate = new Date();
         MediaInfoService mediaInfoService = new MediaInfoService(
                 new DOMSMetadataExtractor(properties), 
                 new BESClippingConfiguration(properties), 
                 new MediaInfoDAO(hibernateSessionFactory));
         List<SnapshotMediaInfo> snapshotsMediaInfo = mediaInfoService.retrieveSnapshotMediaInfo(shardUuid);
         log.debug("Found files: " + snapshotsMediaInfo);
         Collections.sort(snapshotsMediaInfo, new SnapshotMediaInfoFilenameComparator());
         assertTrue("Expecting snapshots", !snapshotsMediaInfo.isEmpty());
         for (int i=0;i<snapshotsMediaInfo.size();i++) {
             SnapshotMediaInfo snapshotMediaInfo = snapshotsMediaInfo.get(i);
             log.info("Retrieved programMediaInfo: " + snapshotMediaInfo);
             assertTrue("Expecting changed date to be recent.", testStartedDate.getTime() <= snapshotMediaInfo.getLastTouched().getTime() && snapshotMediaInfo.getLastTouched().getTime() <= new Date().getTime());
             assertEquals(shardUuid,snapshotMediaInfo.getShardUuid());
             
             boolean fileExist = snapshotMediaInfo.isFileExists();
             boolean expectedFileExist = true;
             assertEquals(expectedFileExist, fileExist);
             
             String filename = snapshotMediaInfo.getFilename();
             String expectedFilename = new File(snapshotMediaFileRelativePath[i]).getName();
             assertEquals(expectedFilename, filename);
             
             long fileSize = snapshotMediaInfo.getFileSizeByte();
             long expectedFileSize = snapshotMediaFileSize[i];
             assertEquals(expectedFileSize, fileSize);
             
             Date lastChanged = snapshotMediaInfo.getFileTimestamp();
             Date expectedLastChanged = new Date(new File(snapshotMediaFileRelativePath[i]).lastModified());
             assertEquals(expectedLastChanged, lastChanged);
                         
             String tcl = snapshotMediaInfo.getTranscodeCommandLine();
             String expectedTCL = "N/A";
             assertEquals(expectedTCL, tcl);
             
             String note = snapshotMediaInfo.getNote();
             String expectedNote = "First batch extraction.";
             assertEquals(expectedNote, note);
         }
     }
 
     @Test
     public void testPersistence() throws DOMSMetadataExtractionConnectToDOMSException {
         MediaInfoDAO mediaInfoDAO = new MediaInfoDAO(hibernateSessionFactory);
         MediaInfoService mediaInfoService = new MediaInfoService(
                 new DOMSMetadataExtractor(properties), 
                 new BESClippingConfiguration(properties), 
                 mediaInfoDAO);
         Metadata metadata = mediaInfoService.retrieveMetadata(shardUuid);
         ProgramMediaInfo programMediaInfo = mediaInfoService.retrieveProgramMediaInfo(shardUuid);
         PreviewMediaInfo previewMediaInfo = mediaInfoService.retrievePreviewMediaInfo(shardUuid);
         List<SnapshotMediaInfo> snapshotsMediaInfo = mediaInfoService.retrieveSnapshotMediaInfo(shardUuid);
         mediaInfoService.save(metadata, programMediaInfo, previewMediaInfo, snapshotsMediaInfo);
         List<Metadata> persistedMetadata = mediaInfoDAO.readMetadata(shardUuid);
         assertTrue("Expecting nonempty result.", !persistedMetadata.isEmpty());
         for (Metadata metadata2 : persistedMetadata) {
             log.debug("Persisted data: " + metadata2);
         }
         List<ProgramMediaInfo> persistedProgramMediaInfo = mediaInfoDAO.readProgramMediaInfo(shardUuid);
         assertTrue("Expecting nonempty result.", !persistedProgramMediaInfo.isEmpty());
         for (ProgramMediaInfo programMediaInfo2 : persistedProgramMediaInfo) {
             log.debug("Persisted data: " + programMediaInfo2);
         }
         List<PreviewMediaInfo> persistedPreviewMediaInfo = mediaInfoDAO.readPreviewMediaInfo(shardUuid);
         assertTrue("Expecting nonempty result.", !persistedPreviewMediaInfo.isEmpty());
         for (PreviewMediaInfo previewMediaInfo2 : persistedPreviewMediaInfo) {
             log.debug("Persisted data: " + previewMediaInfo2);
         }
         List<SnapshotMediaInfo> persistedSnapshotMediaInfo = mediaInfoDAO.readSnapshotMediaInfo(shardUuid);
         assertTrue("Expecting nonempty result.", !persistedSnapshotMediaInfo.isEmpty());
         for (SnapshotMediaInfo snapshotMediaInfo2 : persistedSnapshotMediaInfo) {
             log.debug("Persisted data: " + snapshotMediaInfo2);
         }
     }
 }
