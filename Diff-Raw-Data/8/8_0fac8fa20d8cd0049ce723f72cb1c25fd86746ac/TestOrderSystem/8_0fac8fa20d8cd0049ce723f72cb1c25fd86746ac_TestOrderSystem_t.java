 package com.paragon;
 import com.paragon.orders.Order;
 import com.paragon.stock.Quote;
 import com.paragon.orders.OrderLedger;
 import com.paragon.stock.Offer;
 import org.jmock.Expectations;
 import org.jmock.Mockery;
 import org.jmock.integration.junit4.JUnit4Mockery;
 import org.junit.Assert;
 import org.junit.Test;
 import java.lang.String;
 
 import java.math.BigDecimal;
 import java.util.*;
 
 public class TestOrderSystem {
 
     private String searchString = "C";
     private String userAuthToken = "ted@test.com";
 
     Mockery mockingContext = new JUnit4Mockery();
     FulfillmentService fulfillmentService = mockingContext.mock(FulfillmentService.class);
     OrderSystem orderSystem = new OrderSystem();
 
 
     @Test
     public void testSearchForProduct() throws Exception {
         List<Offer> offers = orderSystem.searchForProduct(searchString);
         Assert.assertTrue(!offers.isEmpty());
     }
 
     @Test
     public void testSearchForProductNotThere() throws Exception {
         List<Offer> offers = orderSystem.searchForProduct("Orange Fanta");
         Assert.assertTrue(offers.isEmpty());
     }
 
     @Test
     public void testConfirmOrder() throws Exception {
         List<Offer> offers = orderSystem.searchForProduct(searchString);
         Thread.sleep(19 * OrderSystem.RELATIVE_ONE_MINUTE);
         orderSystem.add(OrderLedger.getInstance());
         Assert.assertNotNull(orderSystem.confirmOrder(orderSystem.validQuote(offers.get(0).id), userAuthToken));
     }
 
     @Test
     public void testConfirmOrderExpired() throws Exception {
         List<Offer> offers = orderSystem.searchForProduct(searchString);
         Thread.sleep(21 * OrderSystem.RELATIVE_ONE_MINUTE);
         orderSystem.add(OrderLedger.getInstance());
         Assert.assertNull(orderSystem.confirmOrder(orderSystem.validQuote(offers.get(0).id), userAuthToken));
     }
 
     @Test
     public void testConfirmOrderCheckPrice() throws Exception {
         List<Offer> offers = orderSystem.searchForProduct(searchString);
         Thread.sleep(1 * OrderSystem.RELATIVE_ONE_MINUTE);
         orderSystem.add(OrderLedger.getInstance());
         Quote quote = orderSystem.validQuote(offers.get(0).id);
         Assert.assertEquals(true, orderSystem.confirmOrder(quote, userAuthToken).totalPrice.equals(orderSystem.totalPrice(quote)));
     }
 
     @Test
     public void testConfirmOrderMock() throws Exception {
 
         List<Offer> offers = new ArrayList<Offer>(orderSystem.searchForProduct(searchString));
         final Order completeOrder = orderSystem.confirmOrder(orderSystem.validQuote(offers.get(0).id), userAuthToken);
         orderSystem.add(fulfillmentService);
 
         mockingContext.checking(new Expectations() {{
             exactly(1).of(fulfillmentService).placeOrder(completeOrder);
         }});
 
         orderSystem.updateOrderLedger(completeOrder);
     }
 
     @Test
     public void testUnder2MinutePrice() throws Exception {
         List<Offer> offers = orderSystem.searchForProduct(searchString);
        Thread.sleep(1 * 3 * OrderSystem.RELATIVE_ONE_MINUTE);
         Quote quote = orderSystem.validQuote(offers.get(0).id);
 
         Assert.assertEquals(true, orderSystem.totalPrice(quote).equals(quote.offer.price.multiply(OrderSystem.CASE_SIZE)));
     }
 
     @Test
     public void testUnder10MinutePrice() throws Exception {
         List<Offer> offers = orderSystem.searchForProduct(searchString);
        Thread.sleep(5 * 3 * OrderSystem.RELATIVE_ONE_MINUTE);
         Quote quote = orderSystem.validQuote(offers.get(0).id);
         BigDecimal casePrice = quote.offer.price.multiply(OrderSystem.CASE_SIZE);
         Assert.assertEquals(true, orderSystem.totalPrice(quote).equals(casePrice.add(casePrice.divide(new BigDecimal(20)).min(new BigDecimal(10)))));
     }
 
     @Test
     public void testUnder20MinutePrice() throws Exception {
         List<Offer> offers = orderSystem.searchForProduct(searchString);
        Thread.sleep(15 * 3 * OrderSystem.RELATIVE_ONE_MINUTE);
         Quote quote = orderSystem.validQuote(offers.get(0).id);
 
         Assert.assertEquals(true, orderSystem.totalPrice(quote).equals(quote.offer.price.multiply(OrderSystem.CASE_SIZE).add(new BigDecimal(20))));
     }
 }
