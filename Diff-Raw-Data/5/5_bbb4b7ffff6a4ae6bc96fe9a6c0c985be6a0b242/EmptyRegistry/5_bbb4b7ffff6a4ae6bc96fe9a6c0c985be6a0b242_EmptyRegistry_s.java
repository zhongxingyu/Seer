 package org.vamdc.portal.registry;
 
 import java.net.URL;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.List;
 import java.util.Set;
 
 import net.ivoa.xml.voresource.v1.Resource;
 
 import org.vamdc.dictionary.Restrictable;
 import org.vamdc.registry.client.Registry;
 import org.vamdc.registry.client.VamdcTapService;
 
 /**
  * Dummy registry client that is returned when the registry response is not yet ready
  * @author doronin
  *
  */
 public class EmptyRegistry implements Registry{
 
 	@Override
 	public URL getAvailabilityURL(String arg0) {
 		return null;
 	}
 
 	@Override
 	public URL getCapabilitiesURL(String arg0){
 		return null;
 	}
 
 	@Override
 	public Collection<String> getIVOAIDs(Service arg0){
 		return Collections.emptyList();
 	}
 
 	@Override
 	public Resource getResourceMetadata(String arg0){
 		return null;
 	}
 
 	@Override
 	public Set<Restrictable> getRestrictables(String arg0){
 		return Collections.emptySet();
 	}
 
 	@Override
 	public URL getVamdcTapURL(String arg0){
 		return null;
 	}
 
 	@Override
 	public List<VamdcTapService> getMirrors(String ivoaid) {
 		return Collections.emptyList();
 	}
 
 }
