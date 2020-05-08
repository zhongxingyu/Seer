 /*
 * Copyright 2012, CMM, University of Queensland.
 *
* This file is part of AclsLib.
 *
* AclsLib is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * AclsLib is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
* along with AclsLib. If not, see <http://www.gnu.org/licenses/>.
 */
 
 package au.edu.uq.cmm.paul.grabber;
 
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertFalse;
 import static org.junit.Assert.assertTrue;
 
 import java.io.File;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.io.Writer;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Date;
 import java.util.List;
 
 import javax.persistence.EntityManager;
 import javax.persistence.EntityManagerFactory;
 import javax.persistence.Persistence;
 
 import org.codehaus.jackson.JsonGenerationException;
 import org.easymock.Capture;
 import org.easymock.EasyMock;
 import org.junit.AfterClass;
 import org.junit.BeforeClass;
 import org.junit.Test;
 
 import au.edu.uq.cmm.eccles.FacilitySession;
 import au.edu.uq.cmm.paul.Paul;
 import au.edu.uq.cmm.paul.PaulConfiguration;
 import au.edu.uq.cmm.paul.PaulException;
 import au.edu.uq.cmm.paul.queue.CopyingQueueFileManager;
 import au.edu.uq.cmm.paul.queue.QueueFileException;
 import au.edu.uq.cmm.paul.queue.QueueManager;
 import au.edu.uq.cmm.paul.status.DatafileTemplate;
 import au.edu.uq.cmm.paul.status.Facility;
 import au.edu.uq.cmm.paul.status.FacilityStatus;
 import au.edu.uq.cmm.paul.status.FacilityStatusManager;
 import au.edu.uq.cmm.paul.watcher.FileWatcherEvent;
 
 public class GrabberEventTest {
     private static EntityManagerFactory EMF;
     private static FacilityStatusManager FSM;
     private static Facility FACILITY;
     private static PaulConfiguration CONFIG;
 
     @BeforeClass
     public static void setup() {
         EMF = Persistence.createEntityManagerFactory("au.edu.uq.cmm.paul");
         EntityManager em = EMF.createEntityManager();
         try {
             em.getTransaction().begin();
             em.getTransaction().commit();
         } finally {
             em.close();
         }
         FACILITY = buildFacility();
         CONFIG = new PaulConfiguration();
         CONFIG.setCaptureDirectory(prepareDirectory("/tmp/testSafe").toString());
         CONFIG.setArchiveDirectory(prepareDirectory("/tmp/testArchive").toString());
         FSM = buildMockFacilityStatusManager();
     }
     
     private static Facility buildFacility() {
         Facility facility = new Facility();
         facility.setFacilityName("test");
         DatafileTemplate template = new DatafileTemplate();
         template.setSuffix("txt");
         template.setMimeType("text/plain");
         template.setOptional(true);
         template.setMinimumSize(20);
         template.setFilePattern("(.*)\\.txt");
         DatafileTemplate template2 = new DatafileTemplate();
         template2.setSuffix("tox");
         template2.setMimeType("text/plain");
         template2.setOptional(true);
         template2.setMinimumSize(20);
         template2.setFilePattern("(.*)\\.tox");
         facility.setDatafileTemplates(Arrays.asList(new DatafileTemplate[] {
                 template, template2
         }));
         return facility;
     }
 
     @AfterClass
     public static void teardown() {
         removeCaptureDirectory();
     }
 
     private static void removeCaptureDirectory() {
         // TODO Auto-generated method stub
     }
 
     private static File prepareDirectory(String pathname) {
         File res = new File(pathname);
         if (!res.mkdir()) {
             if (!res.isDirectory()) {
                 throw new RuntimeException("Can't create the test directory " + pathname + "!");
             }
             // clean it
         }
         return res;
     }
 
     private static FacilityStatusManager buildMockFacilityStatusManager() {
         FacilityStatusManager fsm = EasyMock.createMock(FacilityStatusManager.class);
         FacilityStatus status = new FacilityStatus();
         FacilitySession session = new FacilitySession();
         session.setFacilityName("test");
         session.setUserName("fred");
         session.setAccount("count");
         status.setLocalDirectory(new File("/tmp"));
         EasyMock.expect(fsm.getStatus(FACILITY)).andReturn(status).anyTimes();
         EasyMock.expect(fsm.getSession(EasyMock.eq("test"), EasyMock.anyLong())).andReturn(session).anyTimes();
         fsm.updateHWMTimestamp(EasyMock.eq(FACILITY), EasyMock.anyObject(Date.class));
         EasyMock.expectLastCall().anyTimes();
         EasyMock.replay(fsm);
         return fsm;
     }
 
     @Test
     public void testConstructor() {
         new FileGrabber(buildMockServices(CONFIG), FACILITY);
     }
 
     @Test(expected=PaulException.class)
     public void testConstructorNoCaptureDir() {
         PaulConfiguration badConfig = new PaulConfiguration();
         badConfig.setCaptureDirectory("/nonexistent/directory");
         new FileGrabber(buildMockServices(badConfig), FACILITY);
     }
     
     @Test
     public void testStartupShutdown() throws InterruptedException {
         FileGrabber fg = new FileGrabber(buildMockServices(CONFIG), FACILITY, true);
         try {
             fg.startup();
             assertFalse(fg.isShutDown());
         } finally {
             fg.shutdown();
             assertTrue(fg.isShutDown());
         }
     }
     
     @Test
     public void testFeedEventMissingFile() throws InterruptedException, JsonGenerationException, IOException {
         QueueManager qm = EasyMock.createMock(QueueManager.class);
         FileGrabber fg = new FileGrabber(buildMockServices(CONFIG, qm), FACILITY, true);
         try {
             fg.startup();
             fg.eventOccurred(new FileWatcherEvent(
                     FACILITY, new File("/tmp/weezle.txt"), true, new Date().getTime(), false));
         } finally {
             fg.shutdown();
         }
     }
     
     @Test
     public void testSingleFeedEventTextFile() 
             throws InterruptedException, JsonGenerationException, IOException, QueueFileException {
         Capture<DatasetMetadata> capture = new Capture<DatasetMetadata>(); 
         QueueManager qm = EasyMock.createMock(QueueManager.class);
         qm.addEntry(EasyMock.capture(capture), EasyMock.anyBoolean());
         EasyMock.expectLastCall().times(1);
         EasyMock.expect(qm.getFileManager()).andReturn(new CopyingQueueFileManager(CONFIG)).times(1);
         EasyMock.replay(qm);
         FileGrabber fg = new FileGrabber(buildMockServices(CONFIG, qm), FACILITY, true);
         long now = System.currentTimeMillis();
         try {
             File one = new File("/tmp/one.txt");
             stringToFile("123456789012345678901234567890", one);
             fg.startup();
             fg.eventOccurred(new FileWatcherEvent(FACILITY, one, true, now, false));
         } finally {
             fg.shutdown();
         }
         EasyMock.verify(qm);
         DatasetMetadata dataset = capture.getValue();
         assertEquals("fred", dataset.getUserName());
         assertEquals("count", dataset.getAccountName());
         assertEquals(1, dataset.getDatafiles().size());
         DatafileMetadata datafile = dataset.getDatafiles().get(0);
         File file = new File(datafile.getCapturedFilePathname());
         assertTrue(file.exists());
         assertEquals(30, file.length());
         assertEquals("eba392e2f2094d7ffe55a23dffc29c412abd47057a0823c6c149c9c759423afd" +
         		     "e56f0eef73ade8f79bc1d16a99cbc5e4995afd8c14adb49410ecd957aecc8d02", 
         		     datafile.getDatafileHash().toLowerCase());
         assertEquals(new Date(now), dataset.getCaptureTimestamp());
     }
     
     @Test
     public void testMultipleFeedEventTextFile() 
             throws InterruptedException, JsonGenerationException, IOException, QueueFileException {
         Capture<DatasetMetadata> capture = new Capture<DatasetMetadata>();
         QueueManager qm = EasyMock.createMock(QueueManager.class);
         qm.addEntry(EasyMock.capture(capture), EasyMock.anyBoolean());
         EasyMock.expectLastCall().times(1);
         EasyMock.expect(qm.getFileManager()).andReturn(new CopyingQueueFileManager(CONFIG)).times(1);
         EasyMock.replay(qm);
         FACILITY.setFileSettlingTime(1000);
         FileGrabber fg = new FileGrabber(buildMockServices(CONFIG, qm), FACILITY, true);
         long now = System.currentTimeMillis();
         try {
             File one = new File("/tmp/one.txt");
             stringToFile("123456789012345678901234567890", one);
             fg.startup();
             long tmp = now;
             for (int i = 0; i < 10; i++) {
                 fg.eventOccurred(new FileWatcherEvent(FACILITY, one, true, tmp, false));
                 tmp = System.currentTimeMillis();
                 Thread.sleep(100);
             }
         } finally {
             fg.shutdown();
         }
         EasyMock.verify(qm);
         DatasetMetadata dataset = capture.getValue();
         assertEquals("fred", dataset.getUserName());
         assertEquals("count", dataset.getAccountName());
         assertEquals(1, dataset.getDatafiles().size());
         DatafileMetadata datafile = dataset.getDatafiles().get(0);
         File file = new File(datafile.getCapturedFilePathname());
         assertTrue(file.exists());
         assertEquals(30, file.length());
         assertEquals("eba392e2f2094d7ffe55a23dffc29c412abd47057a0823c6c149c9c759423afd" +
                      "e56f0eef73ade8f79bc1d16a99cbc5e4995afd8c14adb49410ecd957aecc8d02", 
                      datafile.getDatafileHash().toLowerCase());
         assertEquals(new Date(now), dataset.getCaptureTimestamp());
     }
     
     @Test
     public void testSlowGrabbing() 
             throws InterruptedException, JsonGenerationException, IOException, QueueFileException {
         Capture<DatasetMetadata> capture = new Capture<DatasetMetadata>();
         SlowCopyingQueueFileManager fm = new SlowCopyingQueueFileManager(CONFIG);
         QueueManager qm = EasyMock.createMock(QueueManager.class);
         qm.addEntry(EasyMock.capture(capture), EasyMock.anyBoolean());
         EasyMock.expectLastCall().times(1);
         EasyMock.expect(qm.getFileManager()).andReturn(fm).times(1);
         EasyMock.replay(qm);
         FACILITY.setFileSettlingTime(50);
         FileGrabber fg = new FileGrabber(buildMockServices(CONFIG, qm), FACILITY, true);
         long now = System.currentTimeMillis();
         try {
             File one = new File("/tmp/one.txt");
             File onet = new File("/tmp/one.tox");
             stringToFile("123456789012345678901234567890", one);
             stringToFile("123456789012345678901234567890", onet);
             fg.startup();
             long tmp = now;
             for (int i = 0; i < 10; i++) {
                 fg.eventOccurred(new FileWatcherEvent(FACILITY, one, true, tmp, false));
                 fg.eventOccurred(new FileWatcherEvent(FACILITY, onet, true, tmp, false));
                 tmp = System.currentTimeMillis();
                 Thread.sleep(100);
             }
         } finally {
             fg.shutdown();
         }
         EasyMock.verify(qm);
         DatasetMetadata dataset = capture.getValue();
         assertEquals("fred", dataset.getUserName());
         assertEquals("count", dataset.getAccountName());
         assertEquals(2, dataset.getDatafiles().size());
         DatafileMetadata datafile = dataset.getDatafiles().get(0);
         File file = new File(datafile.getCapturedFilePathname());
         assertTrue(file.exists());
         assertEquals(30, file.length());
         assertEquals("eba392e2f2094d7ffe55a23dffc29c412abd47057a0823c6c149c9c759423afd" +
                      "e56f0eef73ade8f79bc1d16a99cbc5e4995afd8c14adb49410ecd957aecc8d02", 
                      datafile.getDatafileHash().toLowerCase());
         assertEquals(new Date(now), dataset.getCaptureTimestamp());
         assertEquals(9, fm.getFilesRemoved().size());
     }
 
     private void stringToFile(String str, File file) throws IOException {
         try (Writer w = new FileWriter(file)) {
             w.write(str);
         }
     }
 
     private Paul buildMockServices(PaulConfiguration config) {
         return buildMockServices(config, EasyMock.createMock(QueueManager.class));
     }
     
     private Paul buildMockServices(PaulConfiguration config, QueueManager qm) {
         Paul services = EasyMock.createMock(Paul.class);
         EasyMock.expect(services.getEntityManagerFactory()).andReturn(EMF).anyTimes();
         EasyMock.expect(services.getFacilityStatusManager()).andReturn(FSM).anyTimes();
         EasyMock.expect(services.getQueueManager()).andReturn(qm).anyTimes();
         EasyMock.expect(services.getConfiguration()).andReturn(config).anyTimes();
         EasyMock.replay(services);
         return services;
     }
     
     public static class SlowCopyingQueueFileManager extends CopyingQueueFileManager {
         private List<File> filesRemoved = new ArrayList<File>();
         
         public SlowCopyingQueueFileManager(PaulConfiguration config) {
             super(config);
         }
 
         @Override
         public File enqueueFile(File source, String suffix, boolean regrabbing)
                 throws QueueFileException, InterruptedException {
             if (suffix.equals("tox")) {
                 Thread.sleep(1000);
             }
             return super.enqueueFile(source, suffix, regrabbing);
         }
 
         @Override
         public void removeFile(File file) throws QueueFileException {
             filesRemoved.add(file);
             super.removeFile(file);
         }
 
         public final List<File> getFilesRemoved() {
             return filesRemoved;
         }
     }
 }
