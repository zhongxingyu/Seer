 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 
 package com.tracker.backend.webinterface;
 
 import com.tracker.backend.Bencode;
 import com.tracker.backend.entity.Torrent;
 import java.io.ByteArrayInputStream;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.InputStream;
 import java.io.PrintWriter;
 import java.io.StringWriter;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.TreeMap;
 import javax.persistence.EntityManager;
 import javax.persistence.EntityManagerFactory;
 import javax.persistence.Persistence;
 import javax.persistence.Query;
 import org.junit.After;
 import org.junit.AfterClass;
 import org.junit.Before;
 import org.junit.BeforeClass;
 import org.junit.Test;
 import static org.junit.Assert.*;
 
 /**
  *
  * @author bo
  */
 public class TorrentUploadTest {
     static EntityManagerFactory emf = Persistence.createEntityManagerFactory("TorrentTrackerPU");
 
     public TorrentUploadTest() {
     }
 
     @BeforeClass
     public static void setUpClass() throws Exception {
     }
 
     @AfterClass
     public static void tearDownClass() throws Exception {
     }
 
     @Before
     public void setUp() {
     }
 
     @After
     public void tearDown() {
         // removes all the torrents after each test
         EntityManager em = emf.createEntityManager();
 
         System.out.println("Tearing down database");
 
         em.getTransaction().begin();
         em.clear();
 
         Query q = em.createQuery("SELECT t FROM Torrent t");
         List l = q.getResultList();
         Iterator itr = l.iterator();
         while(itr.hasNext()) {
             Torrent t = (Torrent) itr.next();
 
             em.remove(t);
         }
 
         em.getTransaction().commit();
         em.close();
     }
 
     /*
      * We don't test the getDataFromRequest method simply because it's too much
      * of a hassle to fix up a multipart HttpServletRequest mock-object compared
      * to the work the method does. We instead hope that the FileUpload guys
      * have tested their stuff thoroughly (famous last words).
      */
 
     /**
      * Test of addTorrent method, of class TorrentUpload.
      */
     @Test
     public void testAddTorrent() throws Exception {
         System.out.println("addTorrent");
 
         try {
             // a valid pregenerated torrentfile
             File torrentFile = new File("./test/com/tracker/backend/webinterface/SingleFileValid.torrent");
 
             InputStream torrent = new FileInputStream(torrentFile);
             String torrentName = "A Perfectly valid single file torrent!";
             String torrentDescription = "Yep, that's right, all valid.";
             String contextPath = "http://testing";
 
             EntityManager em = emf.createEntityManager();
             Query q;
 
             TreeMap<String, String> expResult = new TreeMap<String, String>();
             expResult.put("warning reason", "");
             expResult.put("error reason", "");
             expResult.put("redownload", "false");
 
             TreeMap<String, String> result = TorrentUpload.addTorrent(torrent, torrentName, torrentDescription, contextPath);
             assertEquals(expResult, result);
 
             // now check the contents of the database.
             q = em.createQuery("SELECT t FROM Torrent t WHERE t.torrentData.name = '" + torrentName + "'");
             // this throws an exception if there is no result
             Torrent t = (Torrent) q.getSingleResult();
 
             assertEquals("mysqltuner.pl",
                     t.getTorrentData().getTorrentContent().get(0).getFileName());
             assertTrue(t.getTorrentFile() != null);
 
             // now try a multifile torrent
             torrentFile = new File("./test/com/tracker/backend/webinterface/MultipleFileValid.torrent");
             torrent = new FileInputStream(torrentFile);
             torrentName = "A perfectly fine torrent with multiple files!";
             torrentDescription = "just because you can transfer more than" +
                     "one file at the time with the lovelyness that is bittorrent.";
             // context path is the same
 
             // expResult is the same
 
             result = TorrentUpload.addTorrent(torrent, torrentName, torrentDescription, contextPath);
             assertEquals(expResult, result);
 
             // check the contents of the database
             q = em.createQuery("SELECT t FROM Torrent t WHERE t.torrentData.name = '" + torrentName + "'");
             t = (Torrent) q.getSingleResult();
 
             // this should contain two files
             assertEquals(2, t.getTorrentData().getTorrentContent().size());
             assertTrue(t.getTorrentFile() != null);
 
             // now try something we have to rewrite. This contains the wrong announce
             torrentFile = new File("./test/com/tracker/backend/webinterface/SingleFileWrongAnnounce.torrent");
             torrent = new FileInputStream(torrentFile);
             torrentName = "I find that the keys are small and tricky to hit.";
             torrentDescription = "So I may very well have mistyped something" +
                     "when creating this.";
             // context path is the same
 
            expResult.put("warning reason", "The torrentfile did " +
                            "not contain the correct announce URL, this " +
                             "has been changed.\n");
             expResult.put("redownload", "true");
 
             result = TorrentUpload.addTorrent(torrent, torrentName, torrentDescription, contextPath);
             assertEquals(expResult, result);
 
             // check the contents of the database.
             q = em.createQuery("SELECT t FROM Torrent t WHERE t.torrentData.name = '" + torrentName + "'");
             t = (Torrent) q.getSingleResult();
 
             // try to decode the torrentfile and check for the announce key, it
             // should have been rewritten.
             ByteArrayInputStream is = new ByteArrayInputStream(t.getTorrentFile().getTorrentFileRaw());
             Map decoded = (Map) Bencode.decode(is).get(0);
             assertEquals("http://testing/Announce", decoded.get("announce"));
 
             // now try some announce list torrents
             torrentFile = new File("./test/com/tracker/backend/webinterface/AnnounceList.torrent");
             torrent = new FileInputStream(torrentFile);
             torrentName = "We like announce lists!";
             torrentDescription = "they make things more distributed and resistant to" +
                     "takedown.";
             // context path is the same
 
             expResult.put("warning reason", "");
             expResult.put("redownload", "false");
 
             result = TorrentUpload.addTorrent(torrent, torrentName, torrentDescription, contextPath);
             assertEquals(expResult, result);
 
             // try some announce lists lacking our announce
             torrentFile = new File("./test/com/tracker/backend/webinterface/AnnounceListWrong.torrent");
             torrent = new FileInputStream(torrentFile);
             torrentName = "however, announce lists does not make typos any less likely.";
             torrentDescription = "on the contrary - having to type up all announce URLs" +
                     "may cause trouble!";
             // context is the same
 
            expResult.put("warning reason", "The torrentfile " +
                                "did not contain the correct announce URL " +
                                 "in it's announce list. This has been changed.\n");
             expResult.put("redownload", "true");
 
             result = TorrentUpload.addTorrent(torrent, torrentName, torrentDescription, contextPath);
             assertEquals(expResult, result);
         }
         catch(Exception ex) {
             StringWriter s = new StringWriter();
             s.append("Exception Caught: ");
             s.append(ex.toString());
             s.append(" ");
             s.append(ex.getMessage());
             PrintWriter w = new PrintWriter(s);
             ex.printStackTrace(w);
             fail(s.toString());
         }
     }
 }
