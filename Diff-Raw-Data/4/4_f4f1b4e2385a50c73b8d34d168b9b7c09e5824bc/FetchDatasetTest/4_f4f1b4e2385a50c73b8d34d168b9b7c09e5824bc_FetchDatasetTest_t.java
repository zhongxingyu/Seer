 package yao.gamelib.test;
 
 import static org.junit.Assert.assertTrue;
 
 import javax.mail.Folder;
 import javax.mail.MessagingException;
 
 import org.junit.Assert;
 import org.junit.Before;
 import org.junit.Test;
 
 import yao.gamelib.FetchDataset;
 
 public class FetchDatasetTest {
     FetchDataset fetcher;
     
     @Before
     public void before() {
         String user = "";
         String pass = "";
         String server = "";
         int port = 993;
        int max = 10;
         String basedir = "C:\\Users\\Casey\\Documents\\workspace\\tmp";
 
        fetcher = new FetchDataset(user, pass, server, port, basedir, null, max);
     }
 
     @Test
     public void testDownloadMail() {
         assertTrue( fetcher.DownloadMail() );
     }
     
     @Test
     public void testListFolders() throws MessagingException {
         fetcher.CreateConnection();
         Folder sent = fetcher.GetSentFolder();
         Assert.assertNotNull(sent);
         Assert.assertTrue( sent.getName().toLowerCase().contains("sent")  );
     }
 
 }
