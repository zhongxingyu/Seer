 package org.vamdc.portal.registry;
 
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.util.Collection;
 import java.util.Collections;
 
 import net.ivoa.xml.voresource.v1.Capability;
 import net.ivoa.xml.voresource.v1.Interface;
 import net.ivoa.xml.voresource.v1.Resource;
 
 import org.jboss.seam.ScopeType;
 import org.jboss.seam.annotations.In;
 import org.jboss.seam.annotations.Logger;
 import org.jboss.seam.annotations.Name;
 import org.jboss.seam.annotations.Scope;
 import org.jboss.seam.international.StatusMessages;
 import org.jboss.seam.log.Log;
 import org.vamdc.dictionary.Restrictable;
 import org.vamdc.registry.client.Registry;
 import org.vamdc.registry.client.RegistryCommunicationException;
 import org.vamdc.registry.client.Registry.Service;
 
 
 @Name("registryFacade")
 @Scope(ScopeType.STATELESS)
 /**
  * Registry facade, wrapping and logging error messages from the registry client calls
  * @author doronin
  *
  */
 public class RegistryFacade {
 
 	@In private StatusMessages statusMessages;
 	@Logger private Log log;
 
 	private Registry registry = Client.INSTANCE.get();
 
 	public Collection<String> getTapIvoaIDs(){
 		try {
 			return Collections.unmodifiableCollection(registry.getIVOAIDs(Service.VAMDC_TAP));
 		}catch (RegistryCommunicationException e) {
 			logError(e);
 		}
 		return Collections.emptyList();
 	}
 
 	public Collection<Restrictable> getRestrictables(String ivoaID){
 		try {
 			return Collections.unmodifiableSet(registry.getRestrictables(ivoaID));
 		}catch (RegistryCommunicationException e) {
 			logError(e);
 		}
 		return Collections.emptyList();
 	}
 
 	
 	public String getResourceTitle(String ivoaID){
 		try {
 			Resource res = registry.getResourceMetadata(ivoaID);
 			if (res!=null)
 				return res.getTitle();
 		} catch (RegistryCommunicationException e) {
 			logError(e);
 		} 
 		return "";
 	}
 	
 	public String getResourceDescription(String ivoaID){
 		try {
 			Resource res = registry.getResourceMetadata(ivoaID);
 			if (res!=null && res.getContent()!=null)
 				return res.getContent().getDescription();
 		} catch (RegistryCommunicationException e) {
 			logError(e);
 		} 
 		return "";
 	}
 	
 	private void logError(RegistryCommunicationException e) {
 		statusMessages.add("Error communicating with the registry! "+e.getMessage());
 		log.error("Error communicating with the registry! "+e.getMessage());
 	}
 
 	public URL getVamdcTapURL(String ivoaID) {
 		try {
 			return registry.getVamdcTapURL(ivoaID);
 		}catch (RegistryCommunicationException e) {
 			logError(e);
 		} 
 		try {
 			return new URL("http://vamdc.org/");
 		} catch (MalformedURLException e) {
 			return null;
 		}
 	}
 
 	public Collection<String> getConsumerIvoaIDs() {
 		try {
 			return Collections.unmodifiableCollection(registry.getIVOAIDs(Service.CONSUMER));
 		}catch (RegistryCommunicationException e) {
 			logError(e);
 		}
 		return Collections.emptyList();
 	}
 
 	public Resource getResource(String ivoaID) {
 		try {
 			return registry.getResourceMetadata(ivoaID);
 		} catch (RegistryCommunicationException e) {
 			logError(e);
 		}
 		return null;
 	}
 	
 	public URL getConsumerService(String ivoaID){
 		URL result = null;
 		net.ivoa.xml.voresource.v1.Service consumer = (net.ivoa.xml.voresource.v1.Service) getResource(ivoaID);
		if (consumer==null)
 			return null;
 		for (Capability cap:consumer.getCapability()){
			if (cap.getStandardID().equalsIgnoreCase(
 					Registry.Service.CONSUMER.getStandardID())){
 				for (Interface interf:cap.getInterface()){
 					if (interf instanceof net.ivoa.xml.vodataservice.v1.ParamHTTP){
 						try {
 							result= new URL(interf.getAccessURL().get(0).getValue());
 						} catch (MalformedURLException e) {
 						}
 					}
 				}
 			}
 		}
 		return result;
 		
 	}
 }
