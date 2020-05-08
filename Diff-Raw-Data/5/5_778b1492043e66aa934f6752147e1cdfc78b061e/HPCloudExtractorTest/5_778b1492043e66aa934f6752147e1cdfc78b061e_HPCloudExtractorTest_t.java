 package n3phele.factory.test.units;
 
 import static org.mockito.Mockito.*;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Map;
 
 import n3phele.factory.rest.impl.HPCloudExtractor;
 import n3phele.service.model.core.NameValue;
 
 import org.jclouds.openstack.nova.v2_0.domain.Address;
 import org.jclouds.openstack.nova.v2_0.domain.Server;
 import org.jclouds.openstack.nova.v2_0.domain.Server.Status;
 import org.jclouds.openstack.nova.v2_0.domain.ServerExtendedAttributes;
 import org.jclouds.openstack.nova.v2_0.domain.ServerExtendedStatus;
 import org.junit.After;
 import org.junit.AfterClass;
 import org.junit.Assert;
 import org.junit.Before;
 import org.junit.BeforeClass;
 import org.junit.Test;
 import org.mockito.Mockito;
 
 import com.google.common.base.Optional;
 import com.google.common.collect.ArrayListMultimap;
 import com.google.common.collect.Multimap;
 
 public class HPCloudExtractorTest {
 	
 
 	@BeforeClass
 	public static void setUpBeforeClass() throws Exception {
 	}
 
 	@AfterClass
 	public static void tearDownAfterClass() throws Exception {
 	}
 
 	@Before
 	public void setUp() throws Exception {
 	}
 
 	@After
 	public void tearDown() throws Exception {
 	}
 	
 	@Test
 	public void testExtractParameter(){
 		
 		Server server = Mockito.mock(Server.class);
 		
 		when(server.getAccessIPv4()).thenReturn("192.186.100.102");	
 		
 		ArrayList<NameValue> testReturn = HPCloudExtractor.extract(server);
 		
 		NameValue expected = new NameValue("accessIPv4","192.186.100.102");
 		NameValue returned = new NameValue();
 		for(int i = 0; i < testReturn.size(); i++){
 			if(testReturn.get(i).getKey().compareTo("accessIPv4")==0){
 				returned = testReturn.get(i);
 				break;
 			}
 		}		
 		Assert.assertEquals(expected,returned);				
 	}
 	
 	@Test
 	public void testPrivateIP(){
 		 Multimap<String, Address> myMultimap = ArrayListMultimap.create();
 		 Server server = Mockito.mock(Server.class);
 		 
 		 myMultimap.put("private", Address.createV4("192.168.100.1"));
		 myMultimap.put("public", Address.createV4("192.168.100.2"));
 		 
 		 when(server.getAddresses()).thenReturn(myMultimap);	
 		 
 		 ArrayList<NameValue> testReturn = HPCloudExtractor.extract(server);
 		 
 		 NameValue expected = new NameValue("privateIpAddress","192.168.100.1");
 		 NameValue returned = new NameValue();
 		 
 		 for(int i = 0; i < testReturn.size(); i++){
 				if(testReturn.get(i).getKey().compareTo("privateIpAddress")==0){
 					returned = testReturn.get(i);
 					break;
 				}
 		}
 		 
 		Assert.assertEquals(expected,returned);	
 	}
 	
 	@Test
 	public void testPublicIP(){
 		 Multimap<String, Address> myMultimap = ArrayListMultimap.create();
 		 Server server = Mockito.mock(Server.class);
 		 
		 myMultimap.put("private", Address.createV4("192.168.100.1"));
 		 myMultimap.put("public", Address.createV4("192.168.100.2"));
 		 
 		 when(server.getAddresses()).thenReturn(myMultimap);	
 		 
 		 ArrayList<NameValue> testReturn = HPCloudExtractor.extract(server);
 		 
 		 NameValue expected = new NameValue("publicIpAddress","192.168.100.1");
 		 NameValue returned = new NameValue();
 		 
 		 for(int i = 0; i < testReturn.size(); i++){
 				if(testReturn.get(i).getKey().compareTo("publicIpAddress")==0){
 					returned = testReturn.get(i);
 					break;
 				}
 		}
 		 
 		Assert.assertEquals(expected,returned);	
 	}
 	
 	@Test
 	public void testIgnoreMetadata(){
 		
 		Server server = Mockito.mock(Server.class);
 		
 		 Map<String,String> map=new HashMap<String, String>();
 		 map.put("test", "test");
 		 
 		 when(server.getMetadata()).thenReturn(map);
 		 
 		 ArrayList<NameValue> testReturn = HPCloudExtractor.extract(server);
 		 
 		 NameValue expected = new NameValue();
 		 NameValue returned = new NameValue();
 		 for(int i = 0; i < testReturn.size(); i++){
 				if(testReturn.get(i).getKey().compareTo("metadata")==0){
 					returned = testReturn.get(i);
 					break;
 				}
 			}	
 		 
 		Assert.assertEquals(expected,returned);	
 	}
 	
 	@Test
 	public void testIgnoreStatus(){
 		
 		Server server = Mockito.mock(Server.class);
 				
 		when(server.getStatus()).thenReturn(Status.valueOf("ACTIVE"));
 		 
 		 ArrayList<NameValue> testReturn = HPCloudExtractor.extract(server);
 		 
 		 NameValue expected = new NameValue();
 		 NameValue returned = new NameValue();
 		 for(int i = 0; i < testReturn.size(); i++){
 				if(testReturn.get(i).getKey().compareTo("status")==0){
 					returned = testReturn.get(i);
 					break;
 				}
 			}	
 		 
 		Assert.assertEquals(expected,returned);	
 	}	
 	
 	
 	@SuppressWarnings("unchecked")
 	@Test
 	public void testIgnoreExtendedStatus(){
 		
 		Server server = Mockito.mock(Server.class);
 		
 		Optional<ServerExtendedStatus> opt = Mockito.mock(Optional.class);
 		
 		ServerExtendedStatus extStatus = Mockito.mock(ServerExtendedStatus.class);		
 		
 		when(opt.get()).thenReturn(extStatus);			
 		
 		when(server.getExtendedStatus()).thenReturn(opt);
 		
 		ArrayList<NameValue> testReturn = HPCloudExtractor.extract(server);
 		
 		NameValue expected = new NameValue();
 		NameValue returned = new NameValue();
 		 
 		 for(int i = 0; i < testReturn.size(); i++){
 				if(testReturn.get(i).getKey().compareTo("extendedStatus")==0){
 					returned = testReturn.get(i);
 					break;
 				}
 		}
 		 
 		Assert.assertEquals(expected,returned);	
 		
 	}
 	
 	
 	
 	
 }
