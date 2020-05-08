 package to.sparks.mtgox.service;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.concurrent.CopyOnWriteArrayList;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import org.jwebsocket.client.java.BaseWebSocket;
 import org.springframework.core.task.TaskExecutor;
 import to.sparks.mtgox.model.Depth;
 import to.sparks.mtgox.model.IEventTime;
 import to.sparks.mtgox.model.Ticker;
 import to.sparks.mtgox.model.Trade;
 import to.sparks.mtgox.net.MtGoxListener;
 import to.sparks.mtgox.net.MtGoxSocketListener;
 
 /**
  *
  * @author SparksG
  */
 public class MtGoxWebSocketClient implements MtGoxListener, Runnable {
 
     private Logger logger;
     private List<Depth> depthHistory = new CopyOnWriteArrayList<>();
     private List<Ticker> tickerHistory = new CopyOnWriteArrayList<>();
     private List<Trade> tradeHistory = new CopyOnWriteArrayList<>();
     final BaseWebSocket websocket = new BaseWebSocket();
     private TaskExecutor taskExecutor;
 
     public MtGoxWebSocketClient(Logger logger, TaskExecutor taskExecutor) {
         this.logger = logger;
         this.taskExecutor = taskExecutor;
     }
 
     public void init() {
         taskExecutor.execute(this);
     }
 
     public void destroy() {
         try {
             websocket.close();
         } catch (Exception ex) {
             logger.log(Level.SEVERE, null, ex);
         }
     }
 
     /*
      * Return ALL the depth items younger than the timestamp
      */
     public List<Depth> getAllDepthSince(long timestamp) {
         int eventIndex = getEventIndex(depthHistory, timestamp);
        return eventIndex <= depthHistory.size() - 1 ? depthHistory.subList(eventIndex, depthHistory.size()) : new ArrayList<Depth>();
     }
 
     public List<Trade> getAllTradesSince(long timestamp) {
         int eventIndex = getEventIndex(tradeHistory, timestamp);
        return eventIndex <= tradeHistory.size() - 1 ? tradeHistory.subList(eventIndex, tradeHistory.size()) : new ArrayList<Trade>();
     }
 
     private int getEventIndex(List list, long timestamp) {
         int length = list.size();
        int result = length;
         for (int i = 0; i < length; i++) {
             IEventTime eventTime = (IEventTime) list.get(i);
             if (eventTime != null && eventTime.getEventTime() > timestamp) {
                 result = i;
                 break;
             }
         }
         return result;
     }
 
     @Override
     public void tradeEvent(Trade trade) {
         getTradeHistory().add(trade);
     }
 
     @Override
     public void tickerEvent(Ticker ticker) {
         tickerHistory.add(ticker);
     }
 
     @Override
     public void depthEvent(Depth depth) {
         depthHistory.add(depth);
     }
 
     public List<Depth> getDepthHistory() {
         return depthHistory;
     }
 
     public List<Ticker> getTickerHistory() {
         return tickerHistory;
     }
 
     public List<Trade> getTradeHistory() {
         return tradeHistory;
     }
 
     @Override
     public void run() {
         try {
 
             websocket.addListener(new MtGoxSocketListener(logger, this));
             websocket.open("ws://websocket.mtgox.com/mtgox");
             logger.info("WS API started.");
 
         } catch (Exception ex) {
             logger.log(Level.SEVERE, null, ex);
         }
 
     }
 }
