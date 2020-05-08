 package org.bm3k.abboe.common;
 
 import org.bm3k.abboe.objects.*;
 import util.collections.Pair;
 
 import com.google.common.net.MediaType;
 
 public class BusinessObjectFactory implements IBusinessObjectFactory {
 
 	private static final String IMPL_SANE = "sane";
 	private static final String IMPL_LEGACY = "legacy";
 	private static final String DEFAULT_IMPL = IMPL_LEGACY;
 	
 	private static IBusinessObjectFactory impl;
 	
 	@SuppressWarnings("deprecation")
 	private static IBusinessObjectFactory getImpl() {
 		if (impl == null) {
 			String implName = System.getenv("BUSINESS_OBJECT_IMPL");
 			if (implName == null) {
 				implName = DEFAULT_IMPL;
 			}
			System.err.println("Using business object implName: "+implName);
 			
 			switch(implName) {
 				case IMPL_SANE: 
 					impl = new BusinessObjectFactory();
 					break;
 				case IMPL_LEGACY: 					
 					impl = new LegacyBusinessObject.Factory();
 					break;
 				default:
 				    throw new RuntimeException("No such business objects impl: "+implName);
 			}						
 		}
 		
 		return impl;
 	}
 	
 	public BusinessObject makeEvent(BusinessObjectEventType eventType) {
 		return getImpl().makeEvent(eventType);
 	}
 	
     public BusinessObject makeObject(MediaType type, byte[] payload) {
     	return getImpl().makeObject(type, payload);
     }
     
     public BusinessObject makeObject(Pair<BusinessObjectMetadata, byte[]> data) {
     	return getImpl().makeObject(data);
     }
 	
     public BusinessObject makePlainTextObject(String text) {
     	return getImpl().makePlainTextObject(text);
     }
     public BusinessObject makePlainTextObject(String text, BusinessObjectEventType eventType) {
     	return getImpl().makePlainTextObject(text, eventType);
     }
 	
 }
