 /*
  * The MtGox-Java API is free software: you can redistribute it and/or modify
  * it under the terms of the Lesser GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * The MtGox-Java API is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * Lesser GNU General Public License for more details.
  *
  * You should have received a copy of the Lesser GNU General Public License
  * along with the MtGox-Java API .  If not, see <http://www.gnu.org/licenses/>.
  */
 package to.sparks.mtgox.example;
 
 import java.math.BigDecimal;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.List;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import org.apache.commons.collections.comparators.ReverseComparator;
 import org.apache.commons.lang.ArrayUtils;
 import org.springframework.context.ApplicationContext;
 import org.springframework.context.ApplicationListener;
 import org.springframework.context.support.ClassPathXmlApplicationContext;
 import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
 import to.sparks.mtgox.MtGoxHTTPClient;
 import to.sparks.mtgox.event.StreamEvent;
 import to.sparks.mtgox.event.TickerEvent;
 import to.sparks.mtgox.event.TradeEvent;
 import to.sparks.mtgox.model.*;
 
 /**
  * A trading bot that will maintain a sequence of bid and ask orders.
  *
  * It should maintain a staggered set of orders that are cancelled and reordered
  * if they move too far from their calculated price or volume.
  * The bids are for varying amounts of bitcoins, according to a configured array
  * of percentages.
  * The orders are staggered above the lowest ask price and below the highest bid
  * price.
  * The total balance of the account is used in placing the orders.
  *
  * @author SparksG
  */
 public class TradingBot implements ApplicationListener<StreamEvent> {
 
     /* The logger */
     static final Logger logger = Logger.getLogger(TradingBot.class.getName());
 
     /* The percentage of total balance bought or sold in each staggered order */
     static final double[] percentagesOrderPriceSpread = new double[]{0.08D, 0.16D, 0.45D, 0.14D, 0.05D};
 
     /* The percentage above ot below last price that each order should be staggered */
     static final double[] percentagesAboveOrBelowPrice = new double[]{0.003D, 0.005D, 0.008D, 0.0120D, 0.016D};
 
     /* The percentage of price that an order is allowed to deviate before re-ordering
      * at the newly calculated prices */
     static final BigDecimal percentAllowedPriceDeviation = BigDecimal.valueOf(0.0015D);
     private ThreadPoolTaskExecutor taskExecutor;
     private MtGoxHTTPClient mtgoxAPI;
     private CurrencyInfo baseCurrency;
     private Ticker lastTicker;
 
     public TradingBot(ThreadPoolTaskExecutor taskExecutor, MtGoxHTTPClient mtgoxAPI) throws Exception {
         this.mtgoxAPI = mtgoxAPI;
         this.taskExecutor = taskExecutor;
 
        AccountInfo info = mtgoxAPI.getAccountInfo();
         logger.log(Level.INFO, "Logged into account: {0}", info.getLogin());
 
         baseCurrency = mtgoxAPI.getCurrencyInfo(mtgoxAPI.getBaseCurrency());
         String currencyCode = baseCurrency.getCurrency().getCurrencyCode();
         logger.log(Level.INFO, "Configured base currency: {0}", currencyCode);
         lastTicker = mtgoxAPI.getTicker();
         taskExecutor.execute(new Logic());
 
         logger.info("Waiting for trade events to trigger bot activity...");
     }
 
     public static void main(String[] args) throws Exception {
 
         ApplicationContext context = new ClassPathXmlApplicationContext("to/sparks/mtgox/example/TradingBot.xml");
         TradingBot me = context.getBean("tradingBot", TradingBot.class);
     }
 
     @Override
     public void onApplicationEvent(StreamEvent event) {
         try {
 
             if (event instanceof TradeEvent) {
                 Trade trade = (Trade) event.getPayload();
                 if (trade.getPrice().getCurrencyInfo().equals(baseCurrency)) {
 
                     if (trade.getAmount().compareTo(new MtGoxBitcoin(0.9D)) > 0) {
                         logger.log(Level.INFO, "Market-making trade event: {0}${1} volume: {2}", new Object[]{trade.getPrice_currency(), trade.getPrice().toPlainString(), trade.getAmount().toPlainString()});
                         if (taskExecutor.getActiveCount() < 1) {
                             taskExecutor.execute(new Logic());
                         } else {
                             logger.info("TaskExecuter is busy! Skipping a turn...");
                         }
                     } else {
                         logger.log(Level.FINE, "Insufficient sized trade event: {0}${1} volume: {2}", new Object[]{trade.getPrice_currency(), trade.getPrice().toPlainString(), trade.getAmount().toPlainString()});
                     }
                 }
 
             } else if (event instanceof TickerEvent) {
                 if (((Ticker) event.getPayload()).getCurrencyCode().equalsIgnoreCase(baseCurrency.getCurrency().getCurrencyCode())) {
                     lastTicker = (Ticker) event.getPayload();
                     logger.log(Level.FINE, "Ticker Last: {0}{1}{2} Volume: {3} Buy: {0}{4} Sell: {0}{5}", new Object[]{
                                 lastTicker.getVwap().getCurrencyInfo().getCurrency().getCurrencyCode(),
                                 lastTicker.getVwap().getCurrencyInfo().getSymbol(),
                                 lastTicker.getLast().toPlainString(),
                                 lastTicker.getVol().toPlainString(),
                                 lastTicker.getBuy().getDisplay(),
                                 lastTicker.getSell().getDisplay()
                             });
                 }
             }
         } catch (Exception ex) {
             logger.log(Level.SEVERE, null, ex);
         }
     }
 
     private static boolean isOrdersValid(MtGoxFiatCurrency[] optimumBidPrices, MtGoxFiatCurrency[] optimumAskPrices, Order[] orders) {
         boolean bRet = false;
         if (ArrayUtils.isNotEmpty(orders)
                 && orders.length == percentagesOrderPriceSpread.length * 2
                 && orders.length == percentagesAboveOrBelowPrice.length * 2) {
             return isWithinAllowedDeviation(percentAllowedPriceDeviation, orders, optimumBidPrices, optimumAskPrices);
         }
         return bRet;
     }
 
     /**
      * Return the calculated price for the order at the given index in the
      * orders arrays.
      *
      * @param index The index of the order price/volume in the orders arrays.
      * @return The calulated price of the order at the given index
      */
     private static MtGoxFiatCurrency getPriceAtOrderIndex(MtGoxHTTPClient.OrderType orderType, MtGoxFiatCurrency lastPrice, int index) {
         MtGoxFiatCurrency price;
 
         if (orderType == MtGoxHTTPClient.OrderType.Bid) {
             price = new MtGoxFiatCurrency(lastPrice.subtract(lastPrice.multiply(BigDecimal.valueOf(percentagesAboveOrBelowPrice[index]))), lastPrice.getCurrencyInfo());
         } else {
             price = new MtGoxFiatCurrency(lastPrice.add(lastPrice.multiply(BigDecimal.valueOf(percentagesAboveOrBelowPrice[index]))), lastPrice.getCurrencyInfo());
         }
 
         return price;
     }
 
     /**
      * Have the orders deviated from the calculated prices?
      * The list must be sorted by descending price or this comparison will not
      * work.
      */
     private static boolean isWithinAllowedDeviation(BigDecimal percentAllowedPriceDeviation, Order[] allOrders, MtGoxFiatCurrency[] optimumBidPrices, MtGoxFiatCurrency[] optimumAskPrices) {
         boolean bRet = true;
 
         List<Order> bidOrders = new ArrayList<>();
         List<Order> askOrders = new ArrayList<>();
 
         for (Order order : allOrders) {
             if (order.getType() == MtGoxHTTPClient.OrderType.Bid) {
                 bidOrders.add(order);
             } else {
                 askOrders.add(order);
             }
         }
 
         // Sort the bid orders by price descending, so that they can be compared to the optimum prices array which is also in that order.
         Collections.sort(bidOrders, new ReverseComparator(new Comparator<Order>() {
 
             @Override
             public int compare(Order o1, Order o2) {
                 return o1.getPrice().getPriceValue().compareTo(o2.getPrice().getPriceValue().getNumUnits());
             }
         }));
 
         // Sort the ask orders by price ascending, so that they can be compared to the optimum prices array which is also in that order.
         Collections.sort(askOrders, new Comparator<Order>() {
 
             @Override
             public int compare(Order o1, Order o2) {
                 return o1.getPrice().getPriceValue().compareTo(o2.getPrice().getPriceValue().getNumUnits());
             }
         });
 
         for (int i = 0; i < bidOrders.size(); i++) {
             BigDecimal actualPrice = bidOrders.get(i).getPrice().getPriceValue().getNumUnits();
             BigDecimal optimumPrice = optimumBidPrices[i].getNumUnits();
             if (isDiffTooLarge(actualPrice, optimumPrice)) {
                 bRet = false;
                 break;
             }
         }
 
         for (int i = 0; i < askOrders.size(); i++) {
             BigDecimal actualPrice = askOrders.get(i).getPrice().getPriceValue().getNumUnits();
             BigDecimal optimumPrice = optimumAskPrices[i].getNumUnits();
             if (isDiffTooLarge(actualPrice, optimumPrice)) {
                 bRet = false;
                 break;
             }
         }
         return bRet;
     }
 
     private static boolean isDiffTooLarge(BigDecimal actualPrice, BigDecimal optimumPrice) {
         BigDecimal diff;
         if (actualPrice.compareTo(optimumPrice) < 0) {
             diff = optimumPrice.subtract(actualPrice);
         } else {
             diff = actualPrice.subtract(optimumPrice);
         }
         return diff.compareTo(optimumPrice.multiply(percentAllowedPriceDeviation)) > 0;
     }
 
     class Logic implements Runnable {
 
         @Override
         public void run() {
             try {
                 MtGoxFiatCurrency buyPrice = lastTicker.getBuy().getPriceValue();
                 MtGoxFiatCurrency sellPrice = lastTicker.getSell().getPriceValue();
                 Order[] openOrders = mtgoxAPI.getOpenOrders();
 
                 MtGoxFiatCurrency[] optimumBidPrices = new MtGoxFiatCurrency[percentagesAboveOrBelowPrice.length];
                 for (int i = 0; i < percentagesAboveOrBelowPrice.length; i++) {
                     optimumBidPrices[i] = getPriceAtOrderIndex(MtGoxHTTPClient.OrderType.Bid, buyPrice, i);
                 }
 
                 MtGoxFiatCurrency[] optimumAskPrices = new MtGoxFiatCurrency[percentagesAboveOrBelowPrice.length];
                 for (int i = 0; i < percentagesAboveOrBelowPrice.length; i++) {
                     optimumAskPrices[i] = getPriceAtOrderIndex(MtGoxHTTPClient.OrderType.Ask, sellPrice, i);
                 }
 
                 if (isOrdersValid(optimumBidPrices, optimumAskPrices, openOrders)) {
                     logger.info("The current orders remain valid.");
                 } else {
                     logger.info("There are invalid bid or ask orders, or none exist.");
                     cancelOrders(mtgoxAPI, openOrders);
 
                    AccountInfo info = mtgoxAPI.getAccountInfo();

                     Wallet fiatWallet = info.getWallets().get(baseCurrency.getCurrency().getCurrencyCode());
                     try {
                         MtGoxBitcoin numBTCtoBuy = new MtGoxBitcoin(fiatWallet.getBalance().divide(buyPrice));
                         logger.log(Level.INFO, "Trying to buy a total of {0} bitcoins.", numBTCtoBuy.toPlainString());
                         for (int i = 0; i < optimumBidPrices.length; i++) {
                             MtGoxBitcoin vol = new MtGoxBitcoin(numBTCtoBuy.multiply(BigDecimal.valueOf(percentagesOrderPriceSpread[i])));
                             String ref = mtgoxAPI.placeOrder(MtGoxHTTPClient.OrderType.Bid, optimumBidPrices[i], vol);
                             logger.log(Level.FINE, "Bid order placed at price: {0}{1} amount: {2} ref: {3}", new Object[]{optimumBidPrices[i].getCurrencyInfo().getCurrency().getCurrencyCode(), optimumBidPrices[i].getNumUnits(), vol.toPlainString(), ref});
                         }
                     } catch (Exception ex) {
                         Logger.getLogger(TradingBot.class.getName()).log(Level.SEVERE, null, ex);
                     }
 
                     Wallet btcWallet = info.getWallets().get("BTC");
                     try {
                         MtGoxBitcoin numBTCtoSell = (MtGoxBitcoin) btcWallet.getBalance();
                         logger.log(Level.INFO, "Trying to sell a total of {0} bitcoins.", numBTCtoSell.toPlainString());
                         for (int i = 0; i < optimumAskPrices.length; i++) {
                             MtGoxBitcoin vol = new MtGoxBitcoin(numBTCtoSell.multiply(BigDecimal.valueOf(percentagesOrderPriceSpread[i])));
                             String ref = mtgoxAPI.placeOrder(MtGoxHTTPClient.OrderType.Ask, optimumAskPrices[i], vol);
                             logger.log(Level.FINE, "Ask order placed at price: {0}{1} amount: {2} ref: {3}", new Object[]{optimumAskPrices[i].getCurrencyInfo().getCurrency().getCurrencyCode(), optimumAskPrices[i].getNumUnits(), vol.toPlainString(), ref});
                         }
                     } catch (Exception ex) {
                         Logger.getLogger(TradingBot.class.getName()).log(Level.SEVERE, null, ex);
                     }
                     logger.log(Level.INFO, "Account balance: {0} BTC + {2}{3}{1} = Total current value: {2}{3}{4}",
                             new Object[]{btcWallet.getBalance().toPlainString(),
                                 fiatWallet.getBalance().toPlainString(),
                                 lastTicker.getLast().getCurrencyInfo().getCurrency().getCurrencyCode(),
                                 lastTicker.getLast().getCurrencyInfo().getSymbol(),
                                 fiatWallet.getBalance().add(btcWallet.getBalance().multiply(lastTicker.getLast().getNumUnits()))});
                 }
             } catch (Exception ex) {
                 Logger.getLogger(TradingBot.class.getName()).log(Level.SEVERE, null, ex);
             }
         }
 
         private void cancelOrders(MtGoxHTTPClient mtGoxAPI, Order[] orders) throws Exception {
             if (ArrayUtils.isNotEmpty(orders)) {
                 for (Order order : orders) {
                     logger.log(Level.FINE, "Cancelling order: {0}", order.getOid());
                     mtGoxAPI.cancelOrder(order);
                 }
             } else {
                 logger.fine("There are no orders to cancel.");
             }
         }
     }
 }
