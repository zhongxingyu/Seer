 package com.rixon.virtualmarket.exchange.order.controller;
 
 import com.alibaba.fastjson.JSON;
 import com.rixon.virtualmarket.exchange.order.domain.Order;
 import com.rixon.virtualmarket.exchange.order.domain.OrderResponse;
 import com.rixon.virtualmarket.util.TestUtil;
 import org.junit.Ignore;
 import org.junit.Test;
 import org.springframework.web.client.RestTemplate;
 
 import static junit.framework.TestCase.assertNull;
 import static org.hamcrest.CoreMatchers.is;
 import static org.hamcrest.MatcherAssert.assertThat;
 
 /**
  * This class is used for testing the RESTFul Services exposed by exchange. These are online tests and The tests should
  * be commented when being run via maven. This can be used after deploying the tests to Tomcat to see if the order
  * is correct or not
  *
  * User: rixon
  * Date: 14/8/13
  * Time: 2:37 PM
  */
 public class OrderRESTInterfacesTests {
 
     @Test
    @Ignore
     public void testPOSTOperationForValidOrder() {
         String orderURL = "http://localhost:8080/exchange/order";
         Order mockOrder = JSON.parseObject(TestUtil.fileContentAsString("newSingleOrder.json"),Order.class);
         OrderResponse expectedOrderResponse = JSON.parseObject(TestUtil.fileContentAsString("newSingleOrder-response.json"),OrderResponse.class);
         OrderResponse actualOrderResponse = new RestTemplate().postForObject(orderURL,mockOrder,OrderResponse.class);
         assertThat("OrderResponse is not matching",expectedOrderResponse,is(actualOrderResponse));
     }
 
     @Test
    @Ignore
     public void testGetOperationForOrder() {
         String orderURL = "http://localhost:8080/exchange/order/{id}";
         Order order = new RestTemplate().getForObject(orderURL,Order.class,"000083656");
         System.out.println("actualOrderResponse = " + order);
     }
 
     @Test
    @Ignore
     public void testDeleteOperationForOrder() {
         String orderURL = "http://localhost:8080/exchange/order/{id}";
         new RestTemplate().delete(orderURL,"10101");
         Order order = new RestTemplate().getForObject(orderURL,Order.class,"10101");
         assertNull(order);
     }
 }
