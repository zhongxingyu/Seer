 package com.meltmedia.cadmium.deployer.jboss7;
 
 import junit.framework.Assert;
 import org.apache.commons.io.FileUtils;
 import org.apache.http.HttpResponse;
 import org.apache.http.ProtocolVersion;
 import org.apache.http.client.HttpClient;
 import org.apache.http.client.methods.HttpPost;
 import org.apache.http.entity.StringEntity;
 import org.apache.http.message.BasicStatusLine;
 import org.apache.http.util.EntityUtils;
 import org.junit.After;
 import org.junit.Test;
 import org.mockito.invocation.InvocationOnMock;
 import org.mockito.stubbing.Answer;
 import org.slf4j.Logger;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.Arrays;
 import java.util.List;
 import java.util.Properties;
 
 import static junit.framework.Assert.assertNotNull;
 import static org.jgroups.util.Util.assertTrue;
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
 import static org.mockito.Mockito.mock;
 import static org.mockito.Mockito.when;
 
 /**
  * com.meltmedia.cadmium.deployer.jboss7.JBossAdminApiTest
  *
  * @author jmcentire
  */
 public class JBossAdminApiTest {
   private final String byteSValue = "SbejgggTNOuHdke5k6EeKdB8Zfo=";
 
   @Test
   public void testDecodeByteSValue() throws Exception {
     System.setProperty(JBossAdminApi.DATA_KEY, "target/test-classes/decode-test");
 
     File resultFile = JBossAdminApi.decodeByteSValue(byteSValue);
     assertNotNull(resultFile);
     assertTrue(resultFile.exists());
   }
 
   @Test
   public void testListDeployedCadmiumWarsApiDeployed() throws Exception {
     System.setProperty(JBossAdminApi.DATA_KEY, "target/test-classes/decode-test");
     HttpClient client = getDefaultHttpClient(FileUtils.readFileToString(new File("target/test-classes/api-deployment.json")));
     JBossAdminApi api = new JBossAdminApi("", "");
     api.client = client;
     List<String> response = api.listDeployedCadmiumWars();
     assertRequestEquals(new File("target/test-classes/list-deployments-request.json"));
     assertNotNull(response);
     assertEquals(response.size(), 1);
     assertEquals(response.get(0), "test.cadmium.localhost.war");
   }
 
   @Test
   public void testListDeployedCadmiumWarsManualDeployed() throws Exception {
     System.setProperty("jboss.server.base.dir", "target/test-classes/manual-deployment");
     HttpClient client = getDefaultHttpClient(FileUtils.readFileToString(new File("target/test-classes/manual-deployment.json")));
     JBossAdminApi api = new JBossAdminApi("", "");
     api.client = client;
     List<String> response = api.listDeployedCadmiumWars();
     assertRequestEquals(new File("target/test-classes/list-deployments-request.json"));
     assertNotNull(response);
     assertEquals(response.size(), 1);
     assertEquals(response.get(0), "test.cadmium.localhost.war");
   }
 
   @Test
   public void testGetDeploymentLocation() throws Exception {
     System.setProperty("jboss.server.base.dir", "target/test-classes/manual-deployment");
     HttpClient client = getDefaultHttpClient(FileUtils.readFileToString(new File("target/test-classes/single-deployment.json")));
     JBossAdminApi api = new JBossAdminApi("", "");
     api.client = client;
     File warFile = api.getDeploymentLocation("test.cadmium.localhost");
     assertRequestEquals(new File("target/test-classes/get-deployment-request.json"));
     assertNotNull(warFile);
     assertTrue(warFile.exists());
   }
 
   @Test
   public void testIsWarDeployed() throws Exception {
     System.setProperty("jboss.server.base.dir", "target/test-classes/manual-deployment");
     HttpClient client = getDefaultHttpClient(FileUtils.readFileToString(new File("target/test-classes/single-deployment.json")));
     JBossAdminApi api = new JBossAdminApi("", "");
     api.client = client;
     Boolean enabled = api.isWarDeployed("test.cadmium.localhost");
     assertRequestEquals(new File("target/test-classes/get-deployment-request.json"));
     assertNotNull(enabled);
     assertTrue(enabled);
   }
 
   @Test
   public void testListVHosts() throws Exception {
     HttpClient client = getDefaultHttpClient(FileUtils.readFileToString(new File("target/test-classes/vhost-list.json")));
     JBossAdminApi api = new JBossAdminApi("", "");
     api.client = client;
     List<String> vhosts = api.listVHosts();
     assertRequestEquals(new File("target/test-classes/list-vhost-request.json"));
     assertNotNull(vhosts);
     assertEquals(vhosts.size(), 2);
     assertEquals(vhosts, Arrays.asList("default-host", "test.cadmium.localhost"));
   }
 
   @Test(expected = Exception.class)
   public void testAddVHostFailed() throws Exception {
     HttpClient client = getDefaultHttpClient(FileUtils.readFileToString(new File("target/test-classes/failed-response.json")));
     JBossAdminApi api = new JBossAdminApi("", "");
     api.client = client;
     api.logger = mock(Logger.class);
     api.addVHost("test-vhost");
     fail();
   }
 
   @Test
   public void testAddVHost() throws Exception {
     HttpClient client = getDefaultHttpClient(FileUtils.readFileToString(new File("target/test-classes/empty-success.json")));
     JBossAdminApi api = new JBossAdminApi("", "");
     api.client = client;
     api.logger = mock(Logger.class);
     api.addVHost("test-vhost");
     assertRequestEquals(new File("target/test-classes/add-vhost-request.json"));
   }
 
   @Test(expected = Exception.class)
   public void testRemoveVHostFailed() throws Exception {
     HttpClient client = getDefaultHttpClient(FileUtils.readFileToString(new File("target/test-classes/failed-response.json")));
     JBossAdminApi api = new JBossAdminApi("", "");
     api.client = client;
     api.logger = mock(Logger.class);
     api.removeVHost("test-vhost");
     fail();
   }
 
   @Test
   public void testRemoveVHost() throws Exception {
     HttpClient client = getDefaultHttpClient(FileUtils.readFileToString(new File("target/test-classes/empty-success.json")));
     JBossAdminApi api = new JBossAdminApi("", "");
     api.client = client;
     api.logger = mock(Logger.class);
     api.removeVHost("test-vhost");
     assertRequestEquals(new File("target/test-classes/remove-vhost-request.json"));
   }
 
   @Test(expected = Exception.class)
   public void testUndeployFailed() throws Exception {
     HttpClient client = getDefaultHttpClient(FileUtils.readFileToString(new File("target/test-classes/failed-response.json")));
     JBossAdminApi api = new JBossAdminApi("", "");
     api.client = client;
     api.logger = mock(Logger.class);
     api.undeploy("test-vhost");
     fail();
   }
 
   @Test
   public void testUndeploy() throws Exception {
     HttpClient client = getDefaultHttpClient(FileUtils.readFileToString(new File("target/test-classes/empty-success.json")));
     JBossAdminApi api = new JBossAdminApi("", "");
     api.client = client;
     api.logger = mock(Logger.class);
     api.undeploy("test-vhost");
     assertRequestEquals(new File("target/test-classes/undeploy-request.json"));
   }
 
   @Test(expected = Exception.class)
   public void testDeployFailed() throws Exception {
     HttpClient client = getDefaultHttpClient(FileUtils.readFileToString(new File("target/test-classes/failed-response.json")));
     JBossAdminApi api = new JBossAdminApi("", "");
     api.client = client;
     api.logger = mock(Logger.class);
     api.deploy("test.cadmium.localhost.war", byteSValue);
     fail();
   }
 
   @Test
   public void testDeploy() throws Exception {
     HttpClient client = getDefaultHttpClient(FileUtils.readFileToString(new File("target/test-classes/empty-success.json")));
     JBossAdminApi api = new JBossAdminApi("", "");
     api.client = client;
     api.logger = mock(Logger.class);
     api.deploy("test.cadmium.localhost.war", byteSValue);
     assertRequestEquals(new File("target/test-classes/deploy-request.json"));
   }
 
   @Test(expected = Exception.class)
   public void testUploadFailed() throws Exception {
     HttpClient client = getDefaultHttpClient(FileUtils.readFileToString(new File("target/test-classes/failed-response.json")));
     JBossAdminApi api = new JBossAdminApi("", "");
     api.client = client;
     api.logger = mock(Logger.class);
     String response = api.uploadWar("test.cadmium.localhost.war", new File("target/test-classes/manual-deployment/deployments/test.cadmium.localhost.war"));
     fail();
   }
 
   @Test
   public void testUpload() throws Exception {
     HttpClient client = getDefaultHttpClient(FileUtils.readFileToString(new File("target/test-classes/upload-response.json")));
     JBossAdminApi api = new JBossAdminApi("", "");
     api.client = client;
     api.logger = mock(Logger.class);
     String response = api.uploadWar("test.cadmium.localhost.war", new File("target/test-classes/manual-deployment/deployments/test.cadmium.localhost.war"));
     Assert.assertEquals(byteSValue, response);
   }
 
   @Test
   public void testGetServerGroup() throws Exception {
     System.setProperty("[Host", "true");
     System.setProperty(JBossAdminApi.NODE_NAME_KEY, "masterCadmium:server-one");
     System.setProperty(JBossAdminApi.SERVER_NAME_KEY, "server-one");
     HttpClient client = getDefaultHttpClient(FileUtils.readFileToString(new File("target/test-classes/server-group-response.json")));
     JBossAdminApi api = new JBossAdminApi("", "");
     api.client = client;
     api.logger = mock(Logger.class);
     String group = api.getServerGroup();
     Assert.assertEquals("main-server-group", group);
     assertRequestEquals(new File("target/test-classes/server-group-request.json"));
   }
 
   @Test
   public void testGetProfile() throws Exception {
     System.setProperty("[Host", "true");
     HttpClient client = getDefaultHttpClient(FileUtils.readFileToString(new File("target/test-classes/profile-response.json")));
     JBossAdminApi api = new JBossAdminApi("", "");
     api.client = client;
     api.logger = mock(Logger.class);
     String profile = api.getProfile("main-server-group");
     Assert.assertEquals("default", profile);
     assertRequestEquals(new File("target/test-classes/profile-request.json"));
   }
 
   @After
   public void reset() {
     Properties props = System.getProperties();
     props.remove("[Host");
     props.remove(JBossAdminApi.NODE_NAME_KEY);
     props.remove(JBossAdminApi.SERVER_NAME_KEY);
     props.remove("jboss.server.base.dir");
     props.remove(JBossAdminApi.DATA_KEY);
   }
 
   private String jsonRequest = null;
 
   private HttpClient getDefaultHttpClient(String responseString) throws IOException {
     jsonRequest = null;
     HttpClient client = mock(HttpClient.class);
     final HttpResponse response = mock(HttpResponse.class);
     when(response.getStatusLine()).thenReturn(new BasicStatusLine(new ProtocolVersion("http", 1, 1), 200, "OK"));
     when(response.getEntity()).thenReturn(new StringEntity(responseString));
     when(client.execute(any(HttpPost.class))).thenAnswer(new Answer<Object>() {
       @Override
       public Object answer(InvocationOnMock invocation) throws Throwable {
         Object parameter = invocation.getArguments()[0];
         if(parameter instanceof HttpPost) {
           HttpPost postedObj = (HttpPost)parameter;
           if(postedObj.getEntity() instanceof StringEntity) {
             jsonRequest = EntityUtils.toString(postedObj.getEntity());
           }
         }
         return response;
       }
     });
     return client;
   }
 
   private void assertRequestEquals(File requestExample) throws Exception {
     String example = FileUtils.readFileToString(requestExample);
     assertNotNull(jsonRequest);
     assertEquals(jsonRequest, example);
   }
 }
