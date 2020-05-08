 package com.foodtogo.services;
 
 import com.foodtogo.entities.PendingOrder;
 import com.foodtogo.repositories.PendingOrderRepository;
 import com.foodtogo.values.Address;
 import org.junit.Before;
 import org.junit.Test;
 
 import java.util.Date;
 
import static junit.framework.Assert.assertSame;
import static junit.framework.Assert.assertTrue;

 import static org.mockito.Mockito.mock;
 import static org.mockito.Mockito.when;
 
 public class PlaceOrderServiceTest {
 
     private PlaceOrderService service;
     private Address goodDeliveryAddress;
     private Date goodDeliveryTime;
     private String pendingOrderId;
     private PendingOrder pendingOrder;
 
     private PendingOrderRepository mockPendingOrderRepository;
     private PendingOrder mockPendingOrder;
 
 
     @Before
     public void setUp() throws Exception {
         mockPendingOrderRepository = mock(PendingOrderRepository.class);
         mockPendingOrder = mock(PendingOrder.class);
 
         service = new PlaceOrderServiceImpl(mockPendingOrderRepository);
         goodDeliveryAddress = new Address();
         goodDeliveryTime = new Date();
        pendingOrder = mockPendingOrder;
 
         pendingOrderId = "pendingOrderId";
     }
 
     @Test
     public void testUpdateDeliveryInfo_Valid() throws Exception {
         when(mockPendingOrderRepository.findOrCreatePendingOrder(pendingOrderId)).thenReturn(pendingOrder);
         when(mockPendingOrder.updateDeliveryInfo(goodDeliveryAddress, goodDeliveryTime)).thenReturn(true);
 
         PlaceOrderServiceResult result = service.updateDeliveryInfo(pendingOrderId, goodDeliveryAddress, goodDeliveryTime);
 
         assertTrue(result.isSuccess());
 
         PendingOrder returnedPendingOrder = result.getPendingOrder();
 
         assertSame(pendingOrder, returnedPendingOrder);
     }
 }
