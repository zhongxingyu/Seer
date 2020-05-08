 package org.rackspace.stingray.client;
 
 
 import com.sun.jersey.api.client.Client;
 import com.sun.jersey.api.client.ClientRequest;
 import com.sun.jersey.api.client.ClientResponse;
 import com.sun.jersey.api.client.WebResource;
 import org.junit.Assert;
 import org.junit.Before;
 import org.junit.Ignore;
 import org.junit.Test;
 import org.junit.experimental.runners.Enclosed;
 import org.junit.runner.RunWith;
 import org.mockito.InjectMocks;
 import org.mockito.Matchers;
 import org.mockito.Mock;
 import org.mockito.runners.MockitoJUnitRunner;
 import org.rackspace.stingray.client.bandwidth.Bandwidth;
 import org.rackspace.stingray.client.bandwidth.BandwidthBasic;
 import org.rackspace.stingray.client.bandwidth.BandwidthProperties;
 import org.rackspace.stingray.client.config.Configuration;
 import org.rackspace.stingray.client.exception.StingrayRestClientException;
 import org.rackspace.stingray.client.exception.StingrayRestClientPathException;
 import org.rackspace.stingray.client.list.Children;
 import org.rackspace.stingray.client.manager.RequestManager;
 import org.rackspace.stingray.client.mock.MockClientHandler;
 import org.rackspace.stingray.client.pool.Pool;
 import org.rackspace.stingray.client.pool.PoolHttp;
 import org.rackspace.stingray.client.pool.PoolProperties;
 import org.rackspace.stingray.client.util.ClientConstants;
 
 import javax.ws.rs.core.MediaType;
 import javax.ws.rs.core.Response;
 import java.net.URI;
 import java.net.URISyntaxException;
 import java.util.ArrayList;
 
 import static org.mockito.Matchers.anyString;
 import static org.mockito.Mockito.mock;
 import static org.mockito.Mockito.when;
 
 @RunWith(Enclosed.class)
 public class StingrayRestClientTest {
 
     @RunWith(MockitoJUnitRunner.class)
     public static class WhenGettingAListOfItem {
         @Mock
         private RequestManager requestManager;
         @InjectMocks
         private StingrayRestClient stingrayRestClient;
 
         @Before
         public void standUp() throws StingrayRestClientException {
             stingrayRestClient.setRequestManager(requestManager);
             when(requestManager.getList(Matchers.<URI>any(), Matchers.<Client>any(), Matchers.<String>any())).thenReturn(new Children());
         }
 
         @Test
         public void shouldReturnWhenPoolPathValid() throws Exception {
             Children pools = stingrayRestClient.getPools();
 
             Assert.assertNotNull(pools);
         }
     }
 
     @Ignore
     @RunWith(MockitoJUnitRunner.class)
     public static class whenGettingAListOfItems {
         private Client client;
         private MockClientHandler mockClientHandler;
         private ClientResponse mockedResponse;
         private RequestManager mockedManager;
         private Children children;
         private WebResource webResource;
         private WebResource.Builder builder;
 
         @Before
         public void standUp() throws URISyntaxException {
             mockClientHandler = new MockClientHandler();
             mockedManager = mock(RequestManager.class);
             children = new Children();
             ArrayList list = new ArrayList<Pool>();
             children.setChildren(list);
         }
 
         private void setupMocks() throws URISyntaxException {
             ClientRequest clientRequest = new ClientRequest.Builder().accept(MediaType.APPLICATION_JSON).build(getItemsPath(), "GET");
             mockedResponse = mockClientHandler.handle(clientRequest);
 
             client = mock(Client.class);
             webResource = mock(WebResource.class);
             builder = mock(WebResource.Builder.class);
 
             when(client.resource(anyString())).thenReturn(webResource);
             when(webResource.accept(MediaType.APPLICATION_JSON)).thenReturn(builder);
             when(builder.get(ClientResponse.class)).thenReturn(mockedResponse);
             try {
                 when(mockedManager.getList(getItemsPath(), client, anyString())).thenReturn(children);
             } catch (Exception e) {
             }
         }
 
 
         private URI getItemsPath() throws URISyntaxException {
             return new URI(MockClientHandler.ROOT + "pools/");
         }
 
         @Test
         public void genericGetItemsReturnsValidList() throws Exception {
             mockClientHandler.when("pools/", "GET").thenReturn(Response.Status.ACCEPTED, children);
             ArrayList<Pool> actualList = new ArrayList<Pool>();
             setupMocks();
 
 
             StingrayRestClient myClient = new StingrayRestClient(new URI(MockClientHandler.ROOT));
             Assert.assertNotNull(myClient.getPools());
             Assert.assertNotNull(myClient.getPools().getChildren().get(0));
             Assert.assertEquals(actualList.getClass(), myClient.getPools().getChildren().getClass());
         }
 
 
     }
 
     @Ignore
     @RunWith(MockitoJUnitRunner.class)
     public static class whenGettingAnItem {
 
         private Client client;
         private MockClientHandler mockClientHandler;
         private ClientResponse mockedResponse;
         private RequestManager mockedManager;
         private WebResource webResource;
         private WebResource.Builder builder;
         private Pool pool;
         private Bandwidth bandwidth;
         private String vsName;
 
         @Before
         public void standUp() throws URISyntaxException {
             mockClientHandler = new MockClientHandler();
             mockedManager = mock(RequestManager.class);
 
 
             vsName = "1234_123";
 
 
             pool = createPool();
             bandwidth = createBandwidth();
 
         }
 
 
         private Bandwidth createBandwidth() {
             BandwidthProperties bandwidthProperties = new BandwidthProperties();
             bandwidth = new Bandwidth();
             BandwidthBasic bandwidthBasic = new BandwidthBasic();
             bandwidthBasic.setMaximum(5000);
             bandwidthProperties.setBasic(bandwidthBasic);
             bandwidth.setProperties(bandwidthProperties);
             return bandwidth;
         }
 
         private Pool createPool() {
             PoolProperties poolProperties = new PoolProperties();
             PoolHttp poolHttp = new PoolHttp();
             poolHttp.setKeepalive(true);
             poolProperties.setHttp(poolHttp);
 
             Pool pool = new Pool();
             pool.setProperties(poolProperties);
             return pool;
         }
 
         private void setupMocks() throws URISyntaxException {
             ClientRequest clientRequest = new ClientRequest.Builder().accept(MediaType.APPLICATION_JSON).build(getItemPath(), "GET");
             mockedResponse = mockClientHandler.handle(clientRequest);
 
             client = mock(Client.class);
             webResource = mock(WebResource.class);
             builder = mock(WebResource.Builder.class);
             mockedManager = mock(RequestManager.class);
 
             when(client.resource(anyString())).thenReturn(webResource);
             when(webResource.accept(MediaType.APPLICATION_JSON)).thenReturn(builder);
             when(builder.get(ClientResponse.class)).thenReturn(mockedResponse);
             try {
                 when(mockedManager.getItem(getItemPath(), client, anyString())).thenReturn(mockedResponse);
             } catch (Exception e) {
             }
             when(mockedResponse.getEntity(Bandwidth.class)).thenReturn(bandwidth);
         }
 
 
         private URI getItemPath() throws URISyntaxException {
 
             return new URI(MockClientHandler.ROOT + ClientConstants.BANDWIDTH_PATH + vsName);
         }
 
 
         @Test
         public void genericGetItemReturnsValidObject() throws Exception {
             mockClientHandler.when(ClientConstants.BANDWIDTH_PATH + vsName, "GET").thenReturn(Response.Status.ACCEPTED, pool);
             setupMocks();
             StingrayRestClient myClient = new StingrayRestClient(new URI(MockClientHandler.ROOT));
             bandwidth = myClient.getBandwidth(vsName);
             Assert.assertNotNull(myClient.getBandwidth(vsName));
         }
     }
 
 
     @Test
     public void genericGetItemReturnsValidObject() throws Exception {
 
     }
 
     @Test
     public void testValidPath() {
 
     }
 
     @Test
     public void testInvalidPath() {
 
     }
 }
