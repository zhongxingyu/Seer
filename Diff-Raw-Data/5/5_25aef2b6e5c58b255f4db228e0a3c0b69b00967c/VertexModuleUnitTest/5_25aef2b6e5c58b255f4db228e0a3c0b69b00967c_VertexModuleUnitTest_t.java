 /**
  * Mule Vertex Connector
  *
  * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
  *
  * The software in this package is published under the terms of the CPAL v1.0
  * license, a copy of which has been included with this distribution in the
  * LICENSE.txt file.
  */
 
 package org.mule.modules;
 
 import static org.junit.Assert.*;
 import static org.mockito.Matchers.*;
 import static org.mockito.Mockito.*;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import org.junit.Before;
 import org.junit.Test;
 import org.mule.modules.vertex.TaxTransactionType;
 import org.mule.modules.vertex.VertexModule;
 import org.mule.modules.vertex.api.VertexClient;
 
 import vertexinc.o_series.tps._6._0.AccrualResponseType;
 import vertexinc.o_series.tps._6._0.BuyerInputTaxResponseType;
 import vertexinc.o_series.tps._6._0.InvoiceVerificationResponseType;
 import vertexinc.o_series.tps._6._0.TaxTransactionResponseType;
 
 import com.zauberlabs.commons.mom.MapObjectMapper;
 
 /**
  * @author Pablo Diez * @since 21/03/2012
  */
 public class VertexModuleUnitTest
 {
     private VertexModule module;
     private VertexClient client;
 
     @Before
     public void init()
     {
         client = mock(VertexClient.class);
 
         module = new VertexModule();
         module.setClient(client);
         module.init();
     }
 
     @Test
     public void testPing()
     {
         final String echo = "hello world";
         when(client.pingService(eq(echo))).thenReturn(echo);
         assertEquals(echo, module.ping(echo));
     }
 
     @SuppressWarnings("unchecked")
     @Test
     public void testCalculateAccrual()
     {
         List<Map<String, Object>> testLineItem = new ArrayList<Map<String, Object>>()
         {
             {
                 add(new HashMap<String, Object>()
                 {
                     {
                         put("lineItemNumber", "1");
                     }
                 });
             }
         };
         when(
             client.calculateTax(eq(TaxTransactionType.ACCRUAL_TYPE), anyMap(), (MapObjectMapper) anyObject())).thenReturn(
             new AccrualResponseType());
 
         assertEquals(
             AccrualResponseType.class,
             module.calculateAccrualTax(null, null, null, null, null, null, null, null, null, testLineItem,
                 null, null, null, null, null, null, null, null, null, null, null, false).getClass());
     }
 
     @Test
     public void testCalculateInvoiceVerification()
     {
         setClientResponse(TaxTransactionType.INVOICE_VERIFICATION_TYPE, new InvoiceVerificationResponseType());
 
         assertEquals(InvoiceVerificationResponseType.class, module.calculateInvoiceVerification(null, null,
             null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null,
            null, null, null, null).getClass());
     }
 
     @Test
     public void testCalculateBuyerInputTax()
     {
         setClientResponse(TaxTransactionType.BUYER_INPUT_TAX_TYPE, new BuyerInputTaxResponseType());
 
         assertEquals(BuyerInputTaxResponseType.class, module.calculateBuyerInputTax(null, null, null, null,
            null, null, null, null, null, null, null, null, null, null, null).getClass());
     }
 
     /**
      * Mocks the client response
      * 
      * @param type The type of request
      * @param responseInstance The response instance
      */
     private void setClientResponse(TaxTransactionType type, TaxTransactionResponseType responseInstance)
     {
         when(client.calculateTax(eq(type), anyMap(), (MapObjectMapper) anyObject())).thenReturn(
             responseInstance);
     }
 }
