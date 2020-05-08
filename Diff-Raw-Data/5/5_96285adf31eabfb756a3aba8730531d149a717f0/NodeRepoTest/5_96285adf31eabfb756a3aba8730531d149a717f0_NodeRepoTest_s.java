 package org.wyona.yarep.tests;
 
 import org.wyona.yarep.core.Node;
 import org.wyona.yarep.core.NodeStateException;
 import org.wyona.yarep.core.NodeType;
 import org.wyona.yarep.core.Path;
 import org.wyona.yarep.core.Repository;
 import org.wyona.yarep.core.RepositoryFactory;
 import org.wyona.yarep.core.Revision;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.InputStreamReader;
 import java.io.OutputStreamWriter;
 import java.io.Reader;
 import java.io.Writer;
 import java.util.Date;
 
 import junit.framework.TestCase;
 
 import org.apache.log4j.Logger;
 
 /**
  * Test for the node based repository implementation.
  */
 public class NodeRepoTest extends TestCase {
 
     protected Repository repo;
 
     private static Logger log = Logger.getLogger(NodeRepoTest.class);
     
     public void setUp() throws Exception {
         RepositoryFactory repoFactory = new RepositoryFactory();
         repo = repoFactory.newRepository("node-fs-example", new File("node-fs-example/repository.xml"));
     }
     
     protected String getCollectionTestPath() {
         return "/hello";
     }
     
     protected String getResourceTestName() {
         return "world.txt";
     }
     
     /**
      * Get mime type of test node
      */
     protected String getMimeTypeOfTestNode() {
         return "text/plain";
     }
     
     protected String getResourceTestPath() {
         return getCollectionTestPath() + "/" + getResourceTestName();
     }
     
     protected String getRevisionTestPath() {
         return getCollectionTestPath() + "/revisiontest.txt";
     }
     
     /**
      * Test write and read re node content
      */
     public void testWriteRead() throws Exception {
         String path = getResourceTestPath();
 
         // Asserts that a condition is true. If it isn't it throws an AssertionFailedError with the given message.
         assertTrue("Path does not exist: " + path, repo.existsNode(path));
         
         Node node = repo.getNode(path);
         node.setMimeType(getMimeTypeOfTestNode());
 
         // Write content to repository
         log.info("Writing content to repository \"" + repo.getName() + "\"");
         String testContent = "Hello World! " + System.currentTimeMillis();
         writeToNode(node, testContent);
         
         // Read content from repository
         log.info("Reading content from repository \"" + repo.getName() + "\"");
         String line = readFromNode(node);
         
         assertEquals(line, testContent);
     }
     
     public void testProperties() throws Exception {
         String path = getResourceTestPath();
         assertTrue("Path does not exist: " + path, repo.existsNode(path));
         
         Node node = repo.getNode(path);
         long size = node.getSize();
         assertTrue(size > 0);
 
         // set a string property:
         String string1 = "test " + System.currentTimeMillis();
         node.setProperty("test", string1);
         String string2 = node.getProperty("test").getString();
         assertEquals(string1, string2);
         
         // set a date property:
         Date date1 = new Date();
         node.setProperty("test-date", date1);
         Date date2 = node.getProperty("test-date").getDate();
         assertEquals(date1, date2);
         
         // delete a property:
         node.removeProperty("test-date");
         assertNull(node.getProperty("test-date"));
     }
 
     public void testAddNode() throws Exception {
         String path = getCollectionTestPath();
         assertTrue("Path does not exist: " + path, repo.existsNode(path));
         
         Node node1 = repo.getNode(path);
         // add file node:
         Node node2 = node1.addNode("sub-uuu", NodeType.RESOURCE);
         
         // write content to node2:
         String testContent = "Hello Sub World! " + System.currentTimeMillis();
         writeToNode(node2, testContent);
         
         // read content from node2:
         String line = readFromNode(node2);
         
         assertEquals(line, testContent);
     }
 
     public void testAddNodes() throws Exception {
         String path = getCollectionTestPath();
         assertTrue("Path does not exist: " + path, repo.existsNode(path));
         
         Node node1 = repo.getNode(path);
         // add directory node:
         Node node2 = node1.addNode("sub-x", NodeType.COLLECTION);
         // add file node:
         Node node3 = node2.addNode("sub-y", NodeType.RESOURCE);
         
         assertTrue(repo.existsNode(path + "/sub-x/sub-y"));
         
         // add string property to node2:
         String string1 = "test " + System.currentTimeMillis();
         node2.setProperty("test", string1);
         String string2 = node2.getProperty("test").getString();
         assertEquals(string1, string2);
         
         // add date property to node3:
         Date date1 = new Date();
         node3.setProperty("test-date", date1);
         Date date2 = node3.getProperty("test-date").getDate();
         assertEquals(date1, date2);
         
         // write content to node3:
         String testContent = "Hello Sub World! " + System.currentTimeMillis();
         writeToNode(node3, testContent);
         
         // read content from node3:
         String line = readFromNode(node3);
         
         assertEquals(line, testContent);
     }
 
     public void testLastModified() throws Exception {
         String path = getResourceTestPath();
         assertTrue("Path does not exist: " + path, repo.existsNode(path));
         
         Node node = repo.getNode(path);
         
         long lastModified1 = node.getLastModified();
 
         Thread.sleep(1000);
         
         // Write content to repository
         String testContent = "bla";
         writeToNode(node, testContent);
         
         long lastModified2 = node.getLastModified();
         
         assertTrue(lastModified2 > lastModified1);
     }
 
     public void testSize() throws Exception {
         String path = getResourceTestPath();
         assertTrue("Path does not exist: " + path, repo.existsNode(path));
         
         Node node = repo.getNode(path);
         
         // Write content to repository
         String testContent = "bla bla bla";
         writeToNode(node, testContent);
         
         long size = node.getSize();
         
         assertEquals(testContent.length(), size);
     }
 
     public void testRootNode() throws Exception {
         assertTrue("Root node does not exist.", repo.existsNode("/"));
         Node node = repo.getRootNode();
         assertEquals(node.getType(), NodeType.COLLECTION);
         assertEquals(node.getName(), "");
         assertNull(node.getParent());
     }
 
     public void testParents() throws Exception {
         String path = getResourceTestPath();
         Node node = repo.getNode(path);
         Node parent = node.getParent();
         String[] pathTokens = path.split("/");
         assertEquals(parent.getName(), pathTokens[pathTokens.length - 2]);
         Node grandParent = parent.getParent();
         assertEquals(grandParent.getName(), "");
         assertNull(grandParent.getParent());
     }
 
     public void testChildren() throws Exception {
         Node parent = repo.getNode(getCollectionTestPath());
         Node[] children = parent.getNodes();
         assertTrue("No children found", children.length > 0);
         boolean foundChild = false;
         for (int i = 0; i < children.length; i++) {
             if (children[i].getName().equals(getResourceTestName())) {
                 assertEquals(getResourceTestPath(), children[i].getPath());
                 foundChild = true;
             }
         }
         assertTrue("Child " +  getResourceTestName() + " not found", foundChild);
         Node child = parent.getNode(getResourceTestName());
         assertEquals(getResourceTestName(), child.getName());
         assertEquals(getResourceTestPath(), child.getPath());
     }
 
     public void testResource() throws Exception {
         Node node = repo.getNode(getResourceTestPath());
         assertTrue(node.isResource());
         assertFalse(node.isCollection());
     }
 
     public void testCollection() throws Exception {
         Node node = repo.getNode(getCollectionTestPath());
         assertTrue(node.isCollection());
         assertFalse(node.isResource());
         
         node = repo.getNode(getCollectionTestPath() + "/");
         assertTrue(node.isCollection());
         assertFalse(node.isResource());
     }
 
     public void testCheckinCheckout() throws Exception {
         Node node = repo.getNode(getResourceTestPath());
         assertFalse("Node is not supposed to be checked out", node.isCheckedOut());
         String testUser = "test-user";
         node.checkout(testUser);
         assertTrue("Node is supposed to be checked out", node.isCheckedOut());
         assertEquals(testUser, node.getCheckoutUserID());
         String testContent = "checkin checkout test";
         writeToNode(node, testContent);
         node.checkin();
         assertFalse("Node is not supposed to be checked out", node.isCheckedOut());
     }
     
     /**
      * Test revision
      */
     public void testRevision() throws Exception {
         Node node = repo.getNode(getRevisionTestPath());
         log.info("Revision Test Path: " + getRevisionTestPath());
         
         node.checkout("test-user");
         String testContent = "editing...";
         writeToNode(node, testContent);
         Revision newRevision = node.checkin();
         
         node.checkout("test-user");
         String testContent2 = "editing2...";
         writeToNode(node, testContent2);
         Revision newRevision2 = node.checkin();
         
         Revision[] revisions = node.getRevisions();
         log.info("Number of revisions: " + revisions.length);
 
         String contentRev0 = readFromNode(revisions[0]);
         log.info("Content of revision 0: " + contentRev0);
         log.info("Test content: " + testContent);
         assertTrue(testContent.equals(contentRev0));
 
         String contentRev1 = readFromNode(revisions[1]);
         assertFalse(testContent.equals(contentRev1));
 
/*
        assertEquals(newRevision, revisions[revisions.length-1]);
         assertTrue(revisions[0].getRevisionName().compareTo(revisions[revisions.length-1].getRevisionName()) < 0);
         
         String contentNewRev = readFromNode(newRevision);
         assertEquals(testContent, contentNewRev);
*/
     }
 
     public void testRevisionTag() throws Exception {
         Node node = repo.getNode(getRevisionTestPath());
         
         node.checkout("test-user");
         String testContent = "revision label test";
         writeToNode(node, testContent);
         Revision newRevision = node.checkin();
         newRevision.setTag("testTag");
         
         Revision revision = node.getRevisionByTag("testTag");
         
         String content = readFromNode(revision);
         assertEquals(testContent, content);
     }
 
     public void testRestore() throws Exception {
         Node node = repo.getNode(getRevisionTestPath());
         
         node.checkout("test-user");
         String testContent1 = "editing... step1";
         writeToNode(node, testContent1);
         Revision newRevision = node.checkin();
         
         String revisionName = newRevision.getRevisionName();
         
         node.checkout("test-user");
         String testContent2 = "editing... step2";
         writeToNode(node, testContent2);
         node.checkin();
         
         String content = readFromNode(node);
         assertEquals(testContent2, content);
         
         node.restore(revisionName);
         
         content = readFromNode(node);
         assertEquals(testContent1, content);
     }
 
     public void testCancelCheckout() throws Exception {
         Node node = repo.getNode(getRevisionTestPath());
         
         node.checkout("test-user");
         try {
             node.checkout("another-user");
             assertTrue("checkout is supposed to fail", false);
         } catch (NodeStateException e) {
             // this exception is expected, checkout is supposed to fail
         }
         node.cancelCheckout();
         
         node.checkout("another-user");
         writeToNode(node, "bla bla");
         Revision newRevision = node.checkin();
     }
 
     // auxiliary methods:
     
     protected void writeToNode(Node node, String content) throws Exception {
         Writer writer = new OutputStreamWriter(node.getOutputStream());
         writer.write(content);
         writer.close();
     }
 
     protected String readFromNode(Node node) throws Exception {
         Reader reader = new InputStreamReader(node.getInputStream());
         BufferedReader br = new BufferedReader(reader);
         String line = br.readLine();
         br.close();
         reader.close();
         return line;
     }
 
 }
