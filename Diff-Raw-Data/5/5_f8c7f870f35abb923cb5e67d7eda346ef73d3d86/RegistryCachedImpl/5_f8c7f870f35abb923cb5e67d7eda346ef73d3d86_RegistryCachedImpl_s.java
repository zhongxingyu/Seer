 package org.vamdc.registry.client.impl;
 
 import java.net.URL;
 import java.util.Collections;
 import java.util.Set;
 
 import net.ivoa.wsdl.registrysearch.RegistrySearchPortType;
 import net.ivoa.xml.voresource.v1.Resource;
 
 import org.vamdc.dictionary.Restrictable;
 import org.vamdc.registry.client.Registry;
 import org.vamdc.registry.client.RegistryCommunicationException;
 import org.vamdc.registry.search.RegistryClientFactory;
 
 /**
  * Provides the cached implementation of the registry client, 
  * all data is retrieved during the initialization and then cached responses are given.
  * @author doronin
  *
  */
 
 public class RegistryCachedImpl implements Registry{
 
 	private RegistrySearch search;
 	
 	public RegistryCachedImpl(String registryEndpoint) throws RegistryCommunicationException{
 		RegistrySearchPortType searchPort = RegistryClientFactory.getSearchPort(registryEndpoint);
 		
 		this.search = new RegistrySearch(searchPort);
 		
 	}
 	
 	@Override
 	public Set<String> getIVOAIDs(Service standard){
 		switch(standard){
 		case VAMDC_TAP:
 			return Collections.unmodifiableSet(search.getTapIvoaIDs());
 		case CONSUMER:
 			return Collections.unmodifiableSet(search.getConsumerIvoaIDs());
 		default:
 			return Collections.emptySet();
 		}
 	}
 	
 	@Override
 	public URL getCapabilitiesURL(String ivoaid){
 		return search.capabilityURLs.get(ivoaid);
 	}
 	
 	@Override
 	public URL getAvailabilityURL(String ivoaid){
 		return search.availabilityURLs.get(ivoaid);
 	}
 	
 	@Override
 	public Resource getResourceMetadata(String ivoaid) {
		return (Resource)search.resultResources.get(ivoaid).clone();
 	}
 	
 	@Override
 	public Set<Restrictable> getRestrictables(String ivoaid){
 		return Collections.unmodifiableSet(search.vamdcTapRestrictables.get(ivoaid));
 	}
 	
 	@Override
 	public URL getVamdcTapURL(String ivoaid){
 		return search.vamdcTapURLs.get(ivoaid);
 	}
 	
 }
