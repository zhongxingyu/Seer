 package org.vamdc.validator.source.http;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Collections;
 
 import net.ivoa.xml.voresource.v1.Capability;
 import net.ivoa.xml.voresource.v1.Interface;
 import net.ivoa.xml.vosicapabilities.v1.Capabilities;
 
 import org.vamdc.validator.source.XSAMSSourceException;
 import org.vamdc.xml.vamdc_tap.v1.VamdcTap;
 
 /**
  * Simple client for capabilities endpoint, 
  * giving out tap sync, capabilities and availability endpoint addresses,
  * restrictables and sample queries
  * 
  */
 public class CapabilitiesClient {
 	
 	public final static String STDIDCapabilities = "ivo://ivoa.net/std/VOSI#capabilities";
 	public final static String STDIDAvailability = "ivo://ivoa.net/std/VOSI#availability";
 	
 	private String endpointAvail;
 	private String endpointCapab;
 	private String vTapEndpoint;
 	private Collection<String> restrictables;
 	private Collection<String> sampleQueries;
 	
 	public CapabilitiesClient(String endpointURL) throws XSAMSSourceException{
		Capabilities caps;
		try{
			caps=Tool.getJerseyClient().resource(endpointURL).get(Capabilities.class);
		}catch (Exception e){
			throw new XSAMSSourceException(e.getMessage());
		}
 		restrictables=new ArrayList<String>();
 		sampleQueries=new ArrayList<String>();
 		
 		for (Capability cap:caps.getCapability()){
 			if (STDIDAvailability.equals(cap.getStandardID())){
 				endpointAvail = extractAccessUrl(cap);
 			}else if (STDIDCapabilities.equals(cap.getStandardID())){
 				endpointCapab = extractAccessUrl(cap);
 			}else if (cap instanceof VamdcTap){
 				vTapEndpoint = extractAccessUrl(cap);
 				VamdcTap vts = (VamdcTap)cap;
 				restrictables.addAll(vts.getRestrictable());
 				sampleQueries.addAll(vts.getSampleQuery());
 			}
 		}
 		validateVariables(endpointURL);
 	}
 	
 	private CapabilitiesClient(){
 		restrictables = Collections.emptyList();
 		sampleQueries = Collections.emptyList();
 		
 	}
 
 	private String extractAccessUrl(Capability cap) {
 		Interface intf = cap.getInterface().get(0);
 		return intf.getAccessURL().get(0).getValue();
 	}
 	
 	private void validateVariables(String endpointURL) throws XSAMSSourceException{
 		Tool.getURL(endpointAvail);
 		Tool.getURL(endpointCapab);
 		Tool.getURL(vTapEndpoint);
 		if (restrictables.size()==0)
 			throw new XSAMSSourceException("No restrictables defined at "+endpointURL);
 		if (sampleQueries.size()==0)
 			throw new XSAMSSourceException("No sample queries defined at "+endpointURL);
 	}
 	
 	public String getTapEndpoint(){	return vTapEndpoint; }
 	public String getAvailabilityEndpoint(){ return endpointAvail; }
 	public String getCapabilitiesEndpoint(){ return endpointCapab; }
 	public Collection<String> getRestrictables(){ return restrictables; }
 	public Collection<String> getSampleQueries(){ return sampleQueries; }
 	
 	public static CapabilitiesClient empty(){
 		return new CapabilitiesClient();
 	}
 	
 }
