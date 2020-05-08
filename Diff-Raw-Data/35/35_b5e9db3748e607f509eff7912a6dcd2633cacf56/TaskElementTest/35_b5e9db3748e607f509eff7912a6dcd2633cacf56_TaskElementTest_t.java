 package com.delcyon.capo.controller.elements;
 
 import static org.junit.Assert.fail;
 
 import java.util.List;
 import java.util.concurrent.CopyOnWriteArrayList;
 
 import org.junit.After;
 import org.junit.AfterClass;
 import org.junit.Assert;
 import org.junit.Before;
 import org.junit.BeforeClass;
 import org.junit.Test;
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 import org.w3c.dom.NodeList;
 
 import com.delcyon.capo.CapoApplication;
 import com.delcyon.capo.controller.Group;
 import com.delcyon.capo.controller.client.ServerControllerResponse;
 import com.delcyon.capo.resourcemanager.ResourceDescriptor;
 import com.delcyon.capo.tasks.TaskManagerThread;
 import com.delcyon.capo.tasks.TaskManagerThread.Preferences;
 import com.delcyon.capo.tests.util.ExternalTestClient;
 import com.delcyon.capo.tests.util.ExternalTestServer;
 import com.delcyon.capo.tests.util.TestCapoApplication;
 import com.delcyon.capo.tests.util.TestClient;
 import com.delcyon.capo.tests.util.TestServer;
 import com.delcyon.capo.tests.util.external.Util;
 import com.delcyon.capo.xml.XPath;
 
 public class TaskElementTest
 {
 
     private ExternalTestClient externalTestClient;
     private ExternalTestServer externalTestServer;
 
     @BeforeClass
     public static void setUpBeforeClass() throws Exception
     {
     }
 
     @AfterClass
     public static void tearDownAfterClass() throws Exception
     {
         TestCapoApplication.cleanup();
     }
 
    
     @Before
     public void setUp() throws Exception
     {
         Util.deleteTree("capo");
         Util.copyTree("test-data/capo", "capo");
//        Util.copyTree("lib", "capo/server/lib");
//        Util.copyTree("lib", "capo/client/lib");
            
     }
 
     @After
     public void tearDown() throws Exception
     {
         if (TaskManagerThread.getTaskManagerThread() != null && TaskManagerThread.getTaskManagerThread().getLock().isLocked() && TaskManagerThread.getTaskManagerThread().getLock().isHeldByCurrentThread())
         {
             TaskManagerThread.getTaskManagerThread().getLock().unlock();
         }
         TestServer.getServerInstance().getConfiguration().setValue(TaskManagerThread.Preferences.TASK_INTERVAL, TaskManagerThread.Preferences.TASK_INTERVAL.getDefaultValue());
         TestServer.getServerInstance().getConfiguration().setValue(TaskManagerThread.Preferences.TASK_DEFAULT_LIFESPAN, TaskManagerThread.Preferences.TASK_DEFAULT_LIFESPAN.getDefaultValue());
         if (externalTestClient != null)
         {
             externalTestClient.shutdown();
             CopyOnWriteArrayList<Exception> exceptionList = externalTestClient.getExceptionList();
             if (exceptionList.isEmpty() == false)
             {
                 throw exceptionList.get(0);
             }
         }
         
         if (TestClient.getClientInstance() != null)
         {
             TestClient.shutdown();
             CopyOnWriteArrayList<Exception> exceptionList = TestClient.getExceptionList();
             if (exceptionList.isEmpty() == false)
             {
                 throw exceptionList.get(0);
             }            
         }
         
         if (externalTestServer != null)
         {
             externalTestServer.shutdown();
             CopyOnWriteArrayList<Exception> exceptionList = externalTestServer.getExceptionList();
             if (exceptionList.isEmpty() == false)
             {
                 throw exceptionList.get(0);
             }
         }
 
         if (TestServer.getServerInstance() != null)
         {
             TestServer.shutdown();
             CopyOnWriteArrayList<Exception> exceptionList = TestServer.getExceptionList();
             if (exceptionList.isEmpty() == false)
             {
                 throw exceptionList.get(0);
             }
             TestServer.cleanup();
         }
         
         
     }
 
     @Test
     public void testProcessServerSideElement() throws Exception
     {
         TestServer.start();        
         TaskManagerThread.getTaskManagerThread().getLock().lock();
         externalTestClient = new ExternalTestClient();
         externalTestClient.startClient();    
         
         TaskElement taskElementControl = new TaskElement();
         
         Document document = CapoApplication.getDocumentBuilder().newDocument();
         Element taskElement = document.createElementNS(CapoApplication.SERVER_NAMESPACE_URI,"server:task");
         taskElement.setAttribute(TaskElement.Attributes.name.toString(), "testTask");
         taskElement.setAttribute(TaskElement.Attributes.local.toString(), "true");
         Group group = new Group("test", null, null, null);
         taskElementControl.init(taskElement, null, group, new ServerControllerResponse());
         taskElementControl.processServerSideElement();
         
         long lastRunTime = TaskManagerThread.getTaskManagerThread().getLastRunTime();
         int loopCount = 0;
         System.out.println("waiting for TaskManager to run: ");
         TaskManagerThread.getTaskManagerThread().getLock().unlock();
         while(lastRunTime == TaskManagerThread.getTaskManagerThread().getLastRunTime() || loopCount < 1)
         {
             if (lastRunTime != TaskManagerThread.getTaskManagerThread().getLastRunTime())
             {
                 loopCount++;                
                 lastRunTime = TaskManagerThread.getTaskManagerThread().getLastRunTime();
                 System.out.print(loopCount);
             }
             System.out.print(".");
             Thread.sleep(100);
         }
         TaskManagerThread.getTaskManagerThread().getLock().lock();
         System.out.println();
         List<ResourceDescriptor> taskResourceDescriptorList = CapoApplication.getDataManager().findDocuments(CapoApplication.getDataManager().getResourceDirectory(Preferences.TASK_DIR.toString()));
         Assert.assertEquals(2,taskResourceDescriptorList.size());
         for (ResourceDescriptor resourceDescriptor : taskResourceDescriptorList)
         {
             
             Document testDocument = CapoApplication.getDocumentBuilder().parse(resourceDescriptor.getInputStream(null));
             if (resourceDescriptor.getLocalName().equals("task-status.xml"))
             {
                 XPath.dumpNode(testDocument, System.out);
                 NodeList nodeList = XPath.selectNodes(testDocument, "//server:task");
                 Assert.assertEquals(1,nodeList.getLength());
                 Assert.assertEquals("testTask",((Element)nodeList.item(0)).getAttribute("name"));
                 Assert.assertEquals("testTask.xml",((Element)nodeList.item(0)).getAttribute("taskURI"));
             }
             else if (resourceDescriptor.getLocalName().equals("testTask.xml"))
             {
                 XPath.dumpNode(testDocument, System.out);
                 NodeList nodeList = XPath.selectNodes(testDocument, "//server:task");
                 Assert.assertEquals(1,nodeList.getLength());
                 Assert.assertEquals("testTask",((Element)nodeList.item(0)).getAttribute("name"));
                 Assert.assertTrue(((Element)nodeList.item(0)).getAttribute("lastAccessTime").matches("\\d+"));
             }
             else
             {
                 fail("Unknown task file:"+resourceDescriptor.getLocalName());
             }
         }
         TestServer.getServerInstance().getConfiguration().setValue(TaskManagerThread.Preferences.TASK_INTERVAL, "2000");
         TestServer.getServerInstance().getConfiguration().setValue(TaskManagerThread.Preferences.TASK_DEFAULT_LIFESPAN, "1000");        
         
         
         lastRunTime = TaskManagerThread.getTaskManagerThread().getLastRunTime();
         loopCount = 0;
         TaskManagerThread.getTaskManagerThread().getLock().unlock();
         System.out.println("waiting for TaskManager to run: ");
         while(lastRunTime == TaskManagerThread.getTaskManagerThread().getLastRunTime() || loopCount < 5)
         {
             if (lastRunTime != TaskManagerThread.getTaskManagerThread().getLastRunTime())
             {
                 loopCount++;
                 lastRunTime = TaskManagerThread.getTaskManagerThread().getLastRunTime();
                 System.out.print(loopCount);
             }
             System.out.print(".");    
             Thread.sleep(100);
         }
         TaskManagerThread.getTaskManagerThread().getLock().lock();
         System.out.println();
         taskResourceDescriptorList = CapoApplication.getDataManager().findDocuments(CapoApplication.getDataManager().getResourceDirectory(Preferences.TASK_DIR.toString()));
         Assert.assertEquals(1,taskResourceDescriptorList.size());
         ResourceDescriptor resourceDescriptor = taskResourceDescriptorList.get(0);
         Document testDocument = CapoApplication.getDocumentBuilder().parse(resourceDescriptor.getInputStream(null));
         if (resourceDescriptor.getLocalName().equals("task-status.xml"))
         {
             XPath.dumpNode(testDocument, System.out);
             NodeList nodeList = XPath.selectNodes(testDocument, "//server:task");
             Assert.assertEquals(0,nodeList.getLength());                
         }            
         else
         {
             fail("Unknown task file:"+resourceDescriptor.getLocalName());
         }
         TaskManagerThread.getTaskManagerThread().getLock().unlock();
     }
 
     /**
      * Start an external server.
      * Start a Test client
      * 
      * @throws Exception
      */
     @Test
     public void testProcessClientSideElement() throws Exception
     {
	System.out.println("===================================================================");
         externalTestServer = new ExternalTestServer();
        System.out.println("===================================================================");
         externalTestServer.startServer();
        System.out.println("===================================================================");
         TestClient.start("-CLIENT_AS_SERVICE","true");
        System.out.println();
     }
 
 }
