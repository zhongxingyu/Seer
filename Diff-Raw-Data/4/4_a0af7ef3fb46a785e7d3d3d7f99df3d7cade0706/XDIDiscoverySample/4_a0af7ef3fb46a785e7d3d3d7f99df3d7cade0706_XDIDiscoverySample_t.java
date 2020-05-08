 package xdi2.example.client;
 
 import xdi2.client.http.XDIHttpClient;
 import xdi2.core.xri3.XDI3Segment;
 import xdi2.discovery.XDIDiscoveryClient;
 import xdi2.discovery.XDIDiscoveryResult;
 
 public class XDIDiscoverySample {
 
 	public static void main(String[] args) throws Exception {
 
 		XDIDiscoveryClient discovery = new XDIDiscoveryClient();
		discovery.setRegistryXdiClient(new XDIHttpClient("http://mycloud.neustar.biz:12220/"));    // this is the default
 
 		XDIDiscoveryResult result = discovery.discoverFromRegistry(XDI3Segment.create("=markus"));
		result = discovery.discoverFromAuthority(result.getXdiEndpointUri(), result.getCloudNumber());
 
 		System.out.println("Cloud Number: " + result.getCloudNumber());    // [=]!:uuid:91f28153-f600-ae24-91f2-8153f600ae24
 		System.out.println("URI: " + result.getXdiEndpointUri());          // http://mycloud.neustar.biz/%5B%3D%5D!%3Auuid%3A91f28153-f600-ae24-91f2-8153f600ae24/
 	}
 }
