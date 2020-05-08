 package net.cyklotron.ngo.it.specialTests;
 
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
          * Test Republish top priority level document as ordinary document
          */
         test1();
 
         /**
          * Test Move terminated document to waiting room.
          */
         test2();
 
     }
 
     /**
      * Republish top priority level document as ordinary document
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
 
         // Wait till document state update (every 1' on tilia).
         Thread.sleep(1000 * 60 * 3);
         
         // republish
         admin.login("root", "12345");        
        editorialTasks.republishTerminatedDocument(wiadomosci.getDocuments().get(0));
         admin.logout();
     }
 
     /**
      * Test Move terminated document to waiting room.
      * 
      * @throws Exception
      */
     private void test2()
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
         editorialTasks.publishDocumentWithoutAttachment(documentId);
         admin.logout();
 
         // request document remove proposal
         wiadomosci.login("selenium", "12345");
         wiadomosci.requestDocumentRemoveProposal(wiadomosci.getDocuments().get(0));
         wiadomosci.logout();
 
         admin.login("root", "12345");
         editorialTasks.acceptPublishedDocumentRemoveProposal(wiadomosci.getDocuments().get(0));
         editorialTasks.documentMoveToWaitingRoom(wiadomosci.getDocuments().get(0));
         admin.logout();
 
     }
 
 }
