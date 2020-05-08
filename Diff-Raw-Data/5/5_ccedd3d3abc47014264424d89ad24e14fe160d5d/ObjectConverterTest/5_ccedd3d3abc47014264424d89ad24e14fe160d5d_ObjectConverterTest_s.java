 package com.orangeleap.tangerine.test.ws.util;
 
 import junit.framework.Assert;
 
 import org.springframework.webflow.execution.RequestContext;
 import org.springframework.webflow.test.MockRequestContext;
 import org.testng.annotations.BeforeMethod;
 import org.testng.annotations.Test;
 
 import com.orangeleap.tangerine.domain.PostBatch;
 import com.orangeleap.tangerine.test.BaseTest;
import com.orangeleap.tangerine.web.flow.BatchSelectionAction;
 import com.orangeleap.tangerine.ws.schema.v2.ActivationType;
 import com.orangeleap.tangerine.ws.schema.v2.PaymentType;
 import com.orangeleap.tangerine.ws.util.v2.ObjectConverter;
 
 
 public class ObjectConverterTest extends BaseTest {
     private RequestContext mockRequestContext;
    private BatchSelectionAction action;
     private PostBatch batch;
 
     @BeforeMethod
     public void setupMocks() {
         mockRequestContext = new MockRequestContext();
        action = new BatchSelectionAction();
         batch = new PostBatch();
     }
 
 	@Test
 	public void testV2ObjectConverterFromEnum() {
 		com.orangeleap.tangerine.ws.schema.v2.Gift v2Gift = new com.orangeleap.tangerine.ws.schema.v2.Gift();
 		com.orangeleap.tangerine.domain.paymentInfo.Gift oleapGift = new com.orangeleap.tangerine.domain.paymentInfo.Gift();
 		ObjectConverter converter = new ObjectConverter();
 		
 		v2Gift.setPaymentType(PaymentType.CREDIT_CARD);
 		converter.ConvertFromJAXB(v2Gift, oleapGift);
 		Assert.assertEquals("Credit Card", oleapGift.getPaymentType());
 		
 		com.orangeleap.tangerine.ws.schema.v2.Email v2Email = new com.orangeleap.tangerine.ws.schema.v2.Email();
 		com.orangeleap.tangerine.domain.communication.Email oleapEmail = new com.orangeleap.tangerine.domain.communication.Email();
 		
 		v2Email.setActivationStatus(ActivationType.PERMANENT);
 		converter.ConvertFromJAXB(v2Email, oleapEmail);
 		Assert.assertEquals(com.orangeleap.tangerine.type.ActivationType.permanent, oleapEmail.getActivationStatus());
 	}
 
 	@Test
 	public void testV1ObjectConverterFromEnum() {
 		com.orangeleap.tangerine.ws.schema.Gift v2Gift = new com.orangeleap.tangerine.ws.schema.Gift();
 		com.orangeleap.tangerine.domain.paymentInfo.Gift oleapGift = new com.orangeleap.tangerine.domain.paymentInfo.Gift();
 		com.orangeleap.tangerine.ws.util.ObjectConverter converter = new com.orangeleap.tangerine.ws.util.ObjectConverter();
 		
 		v2Gift.setPaymentType("Credit Card");
 		converter.ConvertFromJAXB(v2Gift, oleapGift);
 		Assert.assertEquals("Credit Card", oleapGift.getPaymentType());
 		
 		com.orangeleap.tangerine.ws.schema.Email v2Email = new com.orangeleap.tangerine.ws.schema.Email();
 		com.orangeleap.tangerine.domain.communication.Email oleapEmail = new com.orangeleap.tangerine.domain.communication.Email();
 		
 		v2Email.setActivationStatus(com.orangeleap.tangerine.ws.schema.ActivationType.PERMANENT);
 		converter.ConvertFromJAXB(v2Email, oleapEmail);
 		Assert.assertEquals(com.orangeleap.tangerine.type.ActivationType.permanent, oleapEmail.getActivationStatus());
 	}
 	
 	@Test
 	public void testV2ObjectConverterToEnum() {
 		com.orangeleap.tangerine.ws.schema.v2.Gift v2Gift = new com.orangeleap.tangerine.ws.schema.v2.Gift();
 		com.orangeleap.tangerine.domain.paymentInfo.Gift oleapGift = new com.orangeleap.tangerine.domain.paymentInfo.Gift();
 		ObjectConverter converter = new ObjectConverter();
 		
 		oleapGift.setPaymentType("Credit Card");
 		converter.ConvertToJAXB(oleapGift,v2Gift);
 		Assert.assertEquals(PaymentType.CREDIT_CARD,v2Gift.getPaymentType());
 		com.orangeleap.tangerine.ws.schema.v2.Email v2Email = new com.orangeleap.tangerine.ws.schema.v2.Email();
 		com.orangeleap.tangerine.domain.communication.Email oleapEmail = new com.orangeleap.tangerine.domain.communication.Email();
 		
 		oleapEmail.setActivationStatus(com.orangeleap.tangerine.type.ActivationType.permanent);
 		converter.ConvertToJAXB(oleapEmail,v2Email);
 		Assert.assertEquals(ActivationType.PERMANENT, v2Email.getActivationStatus());		
 	}
 	
 	@Test
 	public void testV1ObjectConverterToEnum() {
 		com.orangeleap.tangerine.ws.schema.Gift v2Gift = new com.orangeleap.tangerine.ws.schema.Gift();
 		com.orangeleap.tangerine.domain.paymentInfo.Gift oleapGift = new com.orangeleap.tangerine.domain.paymentInfo.Gift();
 		com.orangeleap.tangerine.ws.util.ObjectConverter converter = new com.orangeleap.tangerine.ws.util.ObjectConverter();
 		
 		oleapGift.setPaymentType("Credit Card");
 		converter.ConvertToJAXB(oleapGift,v2Gift);
 
 		Assert.assertEquals("Credit Card",v2Gift.getPaymentType());
 		com.orangeleap.tangerine.ws.schema.Email v2Email = new com.orangeleap.tangerine.ws.schema.Email();
 		com.orangeleap.tangerine.domain.communication.Email oleapEmail = new com.orangeleap.tangerine.domain.communication.Email();
 		
 		oleapEmail.setActivationStatus(com.orangeleap.tangerine.type.ActivationType.permanent);
 		converter.ConvertToJAXB(oleapEmail,v2Email);
 		Assert.assertEquals(com.orangeleap.tangerine.ws.schema.ActivationType.PERMANENT, v2Email.getActivationStatus());		
 	}	
 }
