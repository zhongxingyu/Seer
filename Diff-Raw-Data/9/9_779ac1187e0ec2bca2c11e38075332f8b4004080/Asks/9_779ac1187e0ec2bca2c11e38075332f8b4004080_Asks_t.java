 package com.maksym.orderbook.domain.ask;
 
 import com.maksym.orderbook.cache.OrdersFetcher;
 import com.maksym.orderbook.domain.OrdersOperations;
 import com.maksym.orderbook.domain.message.OrderMessage;
 import com.maksym.orderbook.utils.OrderBookHelper;
 
 import java.util.Collection;
 import java.util.Comparator;
 import java.util.Set;
 import java.util.concurrent.ConcurrentSkipListSet;
 
 /**
  * @author mfedoryshyn
  */
 public class Asks  implements OrdersOperations {
     private Set<Ask> asksByPrice;
     private OrdersFetcher<Ask> ordersFetcher = OrdersFetcher.createFetcher();
     private int count;
     private Comparator<Ask> sortByPriceComparator = new Comparator<Ask>() {
         @Override
         public int compare(Ask o1, Ask o2) {
             int result = o1.getPrice().compareTo(o2.getPrice());
            return result == 0 ? (o1.getOrderId().compareTo(o2.getOrderId())) : result;
         }
     };
 
     public Asks() {
         asksByPrice = new ConcurrentSkipListSet<Ask>(sortByPriceComparator);
     }
 
     @Override
     public void add(OrderMessage message){
         if(message.getMessageType() != 'A'){
             throw new IllegalArgumentException("Wrong message type!");
         }
         Ask ask = OrderBookHelper.convertToAsk(message);
         asksByPrice.add(ask);
         ordersFetcher.addOrder(ask);
         count += message.getSize();
     }
 
     @Override
     public boolean reduce(OrderMessage message){
         if(message.getMessageType() != 'R'){
             throw new IllegalArgumentException("Wrong message type!");
         }
         Ask ask = OrderBookHelper.convertToAsk(message);
         Ask existentAsk = ordersFetcher.getOrder(ask);
         if(existentAsk != null){
             if(ask.getSize() >= existentAsk.getSize()){
                 asksByPrice.remove(existentAsk);
                 ordersFetcher.invalidateOrder(existentAsk);
                 count -= existentAsk.getSize();
             } else {
                 existentAsk.setSize(existentAsk.getSize() - ask.getSize());
                 count -= message.getSize();
             }
             return true;
         }
         return false;
     }
 
     @Override
     public int getTotalCount() {
         return count;
     }
 
     public Collection<Ask> getAsks(){
         return asksByPrice;
     }
 }
