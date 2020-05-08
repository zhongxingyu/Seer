 package ru.yetanothercoder.tradingbook;
 
 import javax.annotation.concurrent.GuardedBy;
 import javax.annotation.concurrent.ThreadSafe;
 import java.math.BigDecimal;
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.List;
 import java.util.concurrent.ConcurrentNavigableMap;
 import java.util.concurrent.ConcurrentSkipListMap;
 
 /**
  * @author Mikhail Baturov | www.yetanothercoder.ru
  */
 @ThreadSafe
 public class SkipListsOrderBook<S> implements OrderBook<S> {
 
     private final ConcurrentNavigableMap<BigDecimal, Stock<S>> book =
             new ConcurrentSkipListMap<BigDecimal, Stock<S>>();
     private final Side side;
 
     public SkipListsOrderBook(Side side) {
         this.side = side;
     }
 
     @Override
     public StockTransaction<S> executeOrEnqueue(Stock<S> order) {
         if (order.side == side) throw new IllegalArgumentException("wrong side");
 
         List<Stock<S>> matchingOrders;
         synchronized (this) {
             matchingOrders = removeMostProfitableFirst(order);
             if (matchingOrders.size() == 0) {
                 enqueue(order);
                 return null;
             }
         }
 
         if (matchingOrders.size() > 0) {
             return transact(order, matchingOrders);
         }
 
         throw new IllegalStateException();
     }
 
     @GuardedBy("this")
     private void enqueue(Stock<S> order) {
         if (book.containsKey(order.price)) {
             int newSize = book.get(order.price).size + order.size;
             book.put(order.price, order.cloneWithNewSize(newSize));
 
         } else {
             book.put(order.price, order);
         }
     }
 
     private StockTransaction<S> transact(Stock<S> traded, List<Stock<S>> booked) {
         List<Stock<S>> transactionOrders = new ArrayList<Stock<S>>(booked.size());
 
         int balance = traded.size;
         BigDecimal profit = new BigDecimal(0);
 
         for (Iterator<Stock<S>> iterator = booked.iterator(); iterator.hasNext(); ) {
             Stock<S> order = iterator.next();
 
             if (balance < order.size) {
                 transactionOrders.add(traded);
 
                 Stock<S> residualOrder = order.cloneWithNewSize(order.size - traded.size);
                 enqueue(residualOrder);
 
                 assert !iterator.hasNext() : "wrong booked orders!";
             } else {
                 balance = balance - order.size;
 
                 BigDecimal margin = order.price.subtract(traded.price).abs();
                 profit = profit.add(margin);
 
                 transactionOrders.add(order);
             }
         }
         return new StockTransaction<S>(transactionOrders, profit);
     }
 
     @Override
    public List<Stock<S>> executeOrdersOnQuote(Quote newQuote) {
         
         return null;
     }
 
     @Override
     public void expire(Stock<S>... expired) {
         throw new UnsupportedOperationException();
     }
 
     @Override
     @GuardedBy("this")
     public List<Stock<S>> removeMostProfitableFirst(Stock<S> traded) {
         List<Stock<S>> result = new ArrayList<Stock<S>>(0);
 
         ConcurrentNavigableMap<BigDecimal, Stock<S>> matched = book.tailMap(traded.price).descendingMap();
 
         if (matched.size() > 0) {
 
             // first, find the result
             result = new ArrayList<Stock<S>>(matched.size());
             int totalSize = 0;
             for (Stock<S> bookOrder : matched.values()) {
                 if (totalSize >= traded.size) {
                     break;
                 }
 
                 totalSize += bookOrder.size;
                 result.add(bookOrder);
             }
 
             // second, removes them
             for (Stock<S> matchedStock : result) {
                 book.remove(matchedStock.price);
             }
         }
         return result;
     }
 
     @Override
     public Side getSide() {
         return side;
     }
 }
