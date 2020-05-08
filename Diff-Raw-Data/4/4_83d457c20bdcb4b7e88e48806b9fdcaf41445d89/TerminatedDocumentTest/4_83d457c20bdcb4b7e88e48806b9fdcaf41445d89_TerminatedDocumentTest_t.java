 package net.cyklotron.ngo.it.tests;
 
 import java.sql.Time;
 import java.util.Calendar;
 
 import net.cyklotron.ngo.it.SeleniumTest;
 import net.cyklotron.ngo.it.common.Admin;
 import net.cyklotron.ngo.it.common.EditorialTasks;
 import net.cyklotron.ngo.it.common.Wiadomosci;
 
 import org.junit.Test;
 
 public class TerminatedDocumentTest
     extends SeleniumTest
 {
 
     protected String startPage()
     {
 
         return "http://wiadomosci.ngo.pl/";
 
     }
 
     @Test
     public void test()
         throws Exception
     {
 
         /**
          * Test republish terminated top priority level document as ordinary document
          */
         test1();
 
         /**
          * Test delete terminated document
          */
         // test2();
 
     }
 
     /**
      * Test republish terminated top priority level document as ordinary document
      * 
      * @throws Exception
      */
     private void test1()
         throws Exception
     {
 
         // add document
         Wiadomosci wiadomosci = new Wiadomosci(selenium);
         wiadomosci.login("selenium", "12345");
         wiadomosci.addDocument(true, false);
         wiadomosci.logout();
 
         // publish
         Admin admin = new Admin(selenium);
         admin.login("root", "12345");
         EditorialTasks editorialTasks = new EditorialTasks(admin.getPage());
         String documentId = editorialTasks.getDocumentId(wiadomosci.getDocuments().get(0));
         editorialTasks.assignToMe(documentId);
         editorialTasks.publishDocumentAsTerminated(documentId);
         admin.logout();
  
        // Wait till document state update (59'/25' each hour on ngo.pl/ every 1' on tilia).
        Thread.sleep(1000*60*2);
                       
         // republish
         admin.login("root", "12345");
         editorialTasks.RepublishTerminatedDocument(wiadomosci.getDocuments().get(0));
         admin.logout();
     }
 
     /**
      * 
      * @throws Exception
      */
     private void test2()
         throws Exception
     {
 
 
     }
 
 
 }
