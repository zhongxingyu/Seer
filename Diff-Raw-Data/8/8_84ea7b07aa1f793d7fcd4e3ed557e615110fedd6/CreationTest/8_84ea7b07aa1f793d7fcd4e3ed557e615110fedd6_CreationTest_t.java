 /**
  *
  * Copyright (c) 2012, PetalsLink
  * 
  * This library is free software; you can redistribute it and/or
  * modify it under the terms of the GNU Lesser General Public
  * License as published by the Free Software Foundation; either
  * version 2.1 of the License, or (at your option) any later version.
  *
  * This library is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
  * Lesser General Public License for more details.
  *
  * You should have received a copy of the GNU Lesser General Public
  * License along with this library; if not, write to the Free Software
  * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA 
  *
  */
 package eu.playproject.platform.service.bootstrap;
 
 import java.io.InputStream;
 import java.util.ArrayList;
 import java.util.List;
 
 import javax.jws.WebMethod;
 import javax.xml.namespace.QName;
 import javax.xml.ws.wsaddressing.W3CEndpointReference;
 
 import junit.framework.TestCase;
 
 import org.junit.Assert;
 import org.junit.Test;
 import org.ow2.play.metadata.api.Data;
 import org.ow2.play.metadata.api.MetaResource;
 import org.ow2.play.metadata.api.MetadataException;
 import org.ow2.play.metadata.api.Resource;
 import org.ow2.play.metadata.api.service.MetadataService;
 
 import eu.playproject.governance.api.GovernanceExeption;
 import eu.playproject.governance.api.bean.Metadata;
 import eu.playproject.governance.api.bean.Topic;
 import eu.playproject.platform.service.bootstrap.api.GovernanceClient;
 import eu.playproject.platform.service.bootstrap.api.Subscription;
 
 public class CreationTest extends TestCase {
 
 	@Test
 	public void testCreate() throws Exception {
 
 		Topic t1 = new Topic();
 		
 		// just subscribe for T1
 		t1.setName("T1");
 		t1.setNs("http://foo");
 		t1.setPrefix("pre");
 
 		Topic t2 = new Topic();
 		t2.setName("T2");
 		t2.setNs("http://foo");
 		t2.setPrefix("pre");
 
 		final List<Topic> topics = new ArrayList<Topic>();
 		topics.add(t1);
 		topics.add(t2);
 
 		String ecEndpoint = "http://localhost:4568/EventCloudService";
 		String subscriberEndpoint = "http://localhost:4569/SubscriberService";
 
 		DSBSubscribesToECBootstrapServiceImpl service = new DSBSubscribesToECBootstrapServiceImpl();
 		service.setEventCloudClientFactory(new EventCloudClientFactoryMock(
 				ecEndpoint));
 		service.setTopicManager(new TopicManagerMock());
 		service.setSubscriptionRegistry(new SubscriptionRegistryServiceImpl());
 		service.setGovernanceClient(new GovernanceClient() {
 
 			@Override
 			public void loadResources(InputStream arg0)
 					throws GovernanceExeption {
 			}
 
 			@Override
 			public List<Topic> getTopics() {
 				return topics;
 			}
 
 			@Override
 			public List<QName> findTopicsByElement(QName arg0)
 					throws GovernanceExeption {
 				// TODO Auto-generated method stub
 				return null;
 			}
 
 			@Override
 			public List<W3CEndpointReference> findEventProducersByTopics(
 					List<QName> arg0) throws GovernanceExeption {
 				// TODO Auto-generated method stub
 				return null;
 			}
 
 			@Override
 			public List<W3CEndpointReference> findEventProducersByElements(
 					List<QName> arg0) throws GovernanceExeption {
 				// TODO Auto-generated method stub
 				return null;
 			}
 
 			@Override
 			public void createTopic(Topic arg0) throws GovernanceExeption {
 				// TODO Auto-generated method stub
 
 			}
 
 			@Override
 			public void removeMetadata(Topic arg0, Metadata arg1)
 					throws GovernanceExeption {
 				// TODO Auto-generated method stub
 
 			}
 
 			@Override
 			public List<Topic> getTopicsWithMeta(List<Metadata> arg0)
 					throws GovernanceExeption {
 				return null;
 			}
 
 			@Override
 			public Metadata getMetadataValue(Topic arg0, String arg1)
 					throws GovernanceExeption {
 				// TODO Auto-generated method stub
 				return null;
 			}
 
 			@Override
 			public List<Metadata> getMetaData(Topic arg0)
 					throws GovernanceExeption {
 				// TODO Auto-generated method stub
 				return null;
 			}
 
 			@Override
 			public boolean deleteMetaData(Topic arg0) throws GovernanceExeption {
 				// TODO Auto-generated method stub
 				return false;
 			}
 
 			@Override
 			public void addMetadata(Topic arg0, Metadata arg1)
 					throws GovernanceExeption {
 				// TODO Auto-generated method stub
 
 			}
 		});
 
 		service.setMetadataServiceClient(new MetadataService() {
 
 			@Override
 			@WebMethod
 			public void removeMetadata(Resource arg0,
 					org.ow2.play.metadata.api.Metadata arg1)
 					throws MetadataException {
 				// TODO Auto-generated method stub
 
 			}
 
 			@Override
 			@WebMethod
 			public List<MetaResource> list() {
 				// TODO Auto-generated method stub
 				return null;
 			}
 
 			@Override
 			@WebMethod
 			public List<MetaResource> getResoucesWithMeta(
 					List<org.ow2.play.metadata.api.Metadata> arg0)
 					throws MetadataException {
 				// TODO Auto-generated method stub
 				return null;
 			}
 
 			@Override
 			@WebMethod
 			public org.ow2.play.metadata.api.Metadata getMetadataValue(
 					Resource resource, String arg1) throws MetadataException {
 				
 				System.out.println("Get meta for resource " + resource.getUrl() + "  :  " + resource.getName());
 				
 				if (resource.toString().contains("T1")) {
 				return new org.ow2.play.metadata.api.Metadata(
 						"http://www.play-project.eu/xml/ns/dsbneedstosubscribe",
 						new Data("literal", "true"));
 				} else {
 					return null;
 				}
 			}
 
 			@Override
 			@WebMethod
 			public List<org.ow2.play.metadata.api.Metadata> getMetaData(
 					Resource arg0) throws MetadataException {
 				System.out.println("GET META");
 				List<org.ow2.play.metadata.api.Metadata> result = new ArrayList<org.ow2.play.metadata.api.Metadata>();
 				result.add(new org.ow2.play.metadata.api.Metadata(
 						"http://www.play-project.eu/xml/ns/dsbneedstosubscribe",
 						new Data("literal", "false")));
 				return result;
 			}
 
 			@Override
 			@WebMethod
 			public boolean deleteMetaData(Resource arg0)
 					throws MetadataException {
 				return false;
 			}
 
 			@Override
 			@WebMethod
 			public void addMetadata(Resource arg0,
 					org.ow2.play.metadata.api.Metadata arg1)
 					throws MetadataException {
 
 			}
 		});
 
 		List<Subscription> result = service.bootstrap(ecEndpoint,
 				subscriberEndpoint);
 
 		System.out.println(result);
 		
 		Assert.assertTrue(result.size() == 1);
 	}
 }
