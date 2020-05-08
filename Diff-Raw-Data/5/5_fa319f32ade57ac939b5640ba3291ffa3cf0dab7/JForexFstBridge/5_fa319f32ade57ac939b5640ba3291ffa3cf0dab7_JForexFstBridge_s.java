 package org.ttorhcs;
 
 import com.dukascopy.api.*;
 import com.dukascopy.api.IEngine.OrderCommand;
 import com.dukascopy.api.system.TesterFactory;
 import java.io.*;
 import java.text.DecimalFormat;
 import java.util.*;
 import java.util.concurrent.Callable;
 import java.util.logging.Level;
 import org.ttorhcs.logging.LogLevel;
 import org.ttorhcs.logging.Logger;
 import org.ttorhcs.logging.XlsTradeLogger;
 
 @RequiresFullAccess
 public class JForexFstBridge implements IStrategy {
 
     public IHistory history;
     public IEngine engine;
     public IAccount account;
     public IConsole console;
     public IContext context;
     public IDataService dataService;
     public int maxTicket = 1000000000, breakEven = 0, trailingStop = 0;
     public Server server;
     public RandomAccessFile clientPipe = null;
     private IBar presentBar;
     private long last10BarTime;
     private String lastTickStr = "";
     @Configurable("Magic")
     public int magic = 10002000;
     public @Configurable("Period")
     Period period = Period.ONE_HOUR;
     public @Configurable("Conn ID")
     int connId = 111;
     public @Configurable("Logging")
     LogLevel loglevel = LogLevel.INFO;
     public @Configurable("Instrument")
     Instrument instrument = Instrument.EURUSD;
     public @Configurable("TradeLog")
     boolean tradeLog = true;
     public @Configurable("trade/day")
     int maxTrade = 3;
     public boolean started = false;
     private IOrder position = null;
     private IOrder newOrder = null;
     private List<IOrder> orderList = new ArrayList<IOrder>();
     private List<IOrder> maxTradeOrderList = new ArrayList<IOrder>();
     private String periodString;
     private boolean firstTick = false;
     public int waitForFst = 10;
     private XlsTradeLogger tradeLogger;
     private Logger log;
     public int maxLeverage = 30;
     private double allPositionAmount;
     public int consecutiveLosses = 0;
     public double activatedSL = 0, activatedTP = 0, closedSLTPLots = 0;
     private long now;
     private float accountProfit;
     private DecimalFormat df = new DecimalFormat("#.00000");
 
     @Override
     public void onStart(IContext context) throws JFException {
         started = true;
         this.history = context.getHistory();
         this.engine = context.getEngine();
         this.account = context.getAccount();
         this.dataService = context.getDataService();
         this.console = context.getConsole();
         this.context = context;
         periodString = ((int) (period.getInterval()) / 60000) + "";
 
         //initialize logger
         String logFileDir = context.getFilesDir() + "\\logs";
         log = new Logger(context, loglevel, logFileDir, connId + "");
         
         //subscribe to instrument
         Set<Instrument> instruments = new HashSet<Instrument>();
         instruments.add(instrument);
         context.setSubscribedInstruments(instruments, true);
 
         getHistory();
 
         //start srvPipe
         server = new Server();
 
 
         //initalize tradelogger
         tradeLogger = new XlsTradeLogger(magic, connId, context.getFilesDir() + "\\tradeLogs", log, tradeLog);
 
         log.info("Started");
     }
 
     @Override
     public void onTick(Instrument instrument, ITick tick) throws JFException {
         now = tick.getTime();
         if (!dataService.isOfflineTime(tick.getTime()) && instrument == this.instrument) {
             tickToFST(tick, instrument);
             if (breakEven > 0) {
                 checkBreakEven(breakEven, tick);
             }
             if (trailingStop > 0) {
                 setTrailingStops(trailingStop, tick);
             }
             if (!tradeLogger.headerSetted) {
                 tradeLogger.numberOfTicks++;
                 if (tradeLogger.startTime == 0) {
                     tradeLogger.startTime = tick.getTime();
                 }
             }
         }
     }
 
     /**
      * writes tick message to FST pipe
      *
      * @param tick
      * @param instrument
      */
     public void tickToFST(ITick tick, Instrument instrument) {
         String rtnString = "TI " + createTick(tick, instrument);
         byte[] lpInBuffer = new byte[4];
         try {
             if (createClientPipe()) {
                 log.debug("CLwrite: " + rtnString);
                 clientPipe.write(rtnString.getBytes("UTF-8"));
                 clientPipe.read(lpInBuffer, 0, 4);
                 String respond = new String(lpInBuffer, "UTF-8");
                 clientPipe.close();
                 clientPipe = null;
                 log.debug("CLread: " + respond);
                 if (firstTick) {
                     firstTick = false;
                 }
                 if (respond.toUpperCase().startsWith("OK")) {
                 } else {
                     log.error("error in Tick send: \n" + rtnString + "\n respond: " + respond);
                 }
             }
         } catch (Exception e) {
             log.debug("cannot communicate with FST pipe");
             clientPipe = null;
         }
     }
 
     /**
      * creates return tick message
      *
      * @param tick
      * @param instrument
      * @return
      */
     public String createTick(ITick tick, Instrument instrument) { // sends allow
         // tick
         // to FST
         String rtnString = "";
         if (tick.getTime() > (presentBar.getTime()+period.getInterval())){
             firstTick = true;
         }
         
         try {
             rtnString = instrument.name() + " " + periodString + " " + (tick.getTime() / 1000) + " " + tick.getBid() + " " + tick.getAsk() + " "
                     + (Math.round((tick.getAsk() - tick.getBid()) * 100000)) + " " + df.format(instrument.getPipValue() * 10000).replace(',', '.') + " "
                     + barToStrForTick() + " " + last10BarTime + " "
                     + account.getBalance() + " " + account.getEquity() + " " + accountProfit + " "
                     + (float) Math.round((account.getEquity() - (account.getEquity() * account.getUseOfLeverage())) * 100) / 100 + " " // accountfreemargin
                     + getPositionDetails();
         } catch (Exception e) {
             rtnString = "ER";
             log.error(e);
         }
         lastTickStr = rtnString;
         return rtnString;
     }
 
     /**
      *
      * @param bar
      * @return bar data in string format and if it is allow first tick in
      * period/bar the volume = 0
      */
     public String barToStrForTick() {
         String rs = "";
         if (firstTick) {
             getCurrentBar();
             rs += (int) (presentBar.getTime() / 1000) + " ";
             rs = rs + presentBar.getOpen() + " ";
             rs = rs + presentBar.getHigh() + " ";
             rs = rs + presentBar.getLow() + " ";
             rs += presentBar.getClose() + " ";
             rs += "1";
         } else {
             rs += (int) (presentBar.getTime() / 1000) + " ";
             rs += presentBar.getOpen() + " ";
             rs += presentBar.getHigh() + " ";
             rs += presentBar.getLow() + " ";
             rs += presentBar.getClose() + " ";
             rs += Math.round(presentBar.getVolume() * 10) / 10;
         }
         return rs;
     }
 
     /**
      * creates client pipe if pipe not exists then wait for it
      *
      * @return
      */
     public boolean createClientPipe() {
 
         int tryes = 0;
         try {
             while (tryes < 10) {
                 try {
                     clientPipe = new RandomAccessFile("\\\\.\\pipe\\MT4-FST_" + System.getProperty("user.name") + "-" + connId, "rw");
                     break;
                 } catch (SecurityException ex) {
                     log.error("there is a security issue when connecting FST server...");
                     log.error(ex);
                 } catch (FileNotFoundException e) {
                     log.debug("waiting for Fst pipe...");
                     Thread.sleep(waitForFst);
                     clientPipe = null;
                     tryes++;
                 }
             }
             Thread.sleep(waitForFst);
         } catch (InterruptedException e) {
         }
 
         if (tryes < 10) {
             return true;
         }
         return false;
     }
 
     @Override
     public void onMessage(IMessage message) throws JFException {
 
         if (message.getType() == IMessage.Type.ORDER_FILL_OK) {
             IOrder order = message.getOrder();
             if (checkLabel(order)) {
                 getPositions();
                 log.info(message.getContent() + ": " + order.getLabel() + " " + (order.isLong() ? "Long " : "Short ") + "amount: " + (order.getAmount() * 10));
                 newOrder = position;
                 addToMaxTradeOL(order);
             }
         }
         if (message.getType() == IMessage.Type.ORDER_CLOSE_OK) {
             IOrder closedOrder = message.getOrder();
             if (checkLabel(closedOrder)) {
                 getPositions();
                 tradeLogger.logTrade(closedOrder);
                 log.info("closed order: " + closedOrder.getLabel());
                 if (message.getReasons().contains(IMessage.Reason.ORDER_CLOSED_BY_SL)) {
                     consecutiveLosses++;
                     closedSLTPLots = closedOrder.getAmount() * 10;
                     activatedSL = closedOrder.getClosePrice();
                 } else if (message.getReasons().contains(IMessage.Reason.ORDER_CLOSED_BY_TP)) {
                     activatedTP++;
                     consecutiveLosses = 0;
                     closedSLTPLots = closedOrder.getAmount() * 10;
                     activatedTP = closedOrder.getClosePrice();
                 } else {
                     if (closedOrder.getProfitLossInUSD() > 0) {
                         consecutiveLosses = 0;
                     } else {
                         consecutiveLosses++;
                     }
                 }
             }
         }
     }
 
     @Override
     public void onAccount(IAccount account) throws JFException {
     }
 
     @Override
     public void onStop() throws JFException {
         try {
             started = false;
             server.srvPipe.disconnect();
 
             Thread.sleep(50);
             if (server.isAlive()) {
                 log.error("server cannot be dismissed: destroyed.");
                 server.stop();
             }
             log.info("bridge stopped");
             tradeLogger.close();
             log.close();
             log = null;
         } catch (Exception ex) {
             log.error(ex);
         }
     }
 
     @Override
     public void onBar(Instrument instrument, Period period, IBar askBar, IBar bidBar) throws JFException {
         if (instrument == this.instrument) {
             if (period == this.period) {
                 firstTick = true;
                 setAccountProfit();
             }
             if (!tradeLogger.headerSetted) {
                 if (tradeLogger.numberOfTicks >= 500) {
                     tradeLogger.endTime = history.getLastTick(instrument).getTime();
                     tradeLogger.writeHeader(instrument.name());
                     log.info("tradeLogger header created");
                 }
             }
         }
     }
 
     /**
      * sets trailing stops if given
      *
      * @param trailingStop
      * @param tick
      */
     public void setTrailingStops(int trailingStop, ITick tick) {
         try {
             double stoplossPrice;
             for (IOrder order : orderList) {
                 if (order.isLong()) {
                     if (order.getStopLossPrice() == 0) {
                         stoplossPrice = order.getOpenPrice() - (order.getInstrument().getPipValue() * (trailingStop / 10));
                         order.setStopLossPrice(stoplossPrice, OfferSide.BID);
                     } else {
                         stoplossPrice = order.getStopLossPrice();
                     }
                     if ((tick.getBid() - stoplossPrice) > (order.getInstrument().getPipValue() * (trailingStop / 10))) {
                         double price = tick.getBid() - (order.getInstrument().getPipValue() * (trailingStop / 10));
                         order.setStopLossPrice(price, OfferSide.BID);
                     }
                 } else {
                     if (order.getStopLossPrice() == 0) {
                         stoplossPrice = order.getOpenPrice() + (order.getInstrument().getPipValue() * (trailingStop / 10));
                         order.setStopLossPrice(stoplossPrice, OfferSide.ASK);
                     } else {
                         stoplossPrice = order.getStopLossPrice();
                     }
                     if ((stoplossPrice - tick.getAsk()) > (order.getInstrument().getPipValue() * (trailingStop / 10))) {
                         double price = tick.getAsk() + (order.getInstrument().getPipValue() * (trailingStop / 10));
                         order.setStopLossPrice(price, OfferSide.ASK);
                     }
                 }
             }
         } catch (JFException ex) {
             log.error(ex);
         }
     }
 
     /**
      * if break even is set this method checks price and set if it nesessary
      *
      * @param breakeven
      * @param tick
      */
     public void checkBreakEven(int breakeven, ITick tick) {
         try {
             double breakevenprice;
             int newBreakeven = 0;
             for (IOrder order : orderList) {
                 if (order.isLong()) {
                     if (order.getOpenPrice() > order.getStopLossPrice()) {
                         breakevenprice = order.getOpenPrice() - (order.getInstrument().getPipValue() * ((breakeven / 10) + 2));
                         if ((tick.getBid() - breakevenprice) > (order.getInstrument().getPipValue() * (breakeven / 10))) {
                             double price = tick.getBid() - (order.getInstrument().getPipValue() * ((breakeven / 10) + 2));
                             order.setStopLossPrice(price, OfferSide.BID);
                         }
                     } else {
                         newBreakeven = breakEven;
                     }
                 } else {
                     if (order.getStopLossPrice() > order.getOpenPrice()) {
                         breakevenprice = order.getOpenPrice() + (order.getInstrument().getPipValue() * ((breakeven / 10) - 2));
                         if ((breakevenprice - tick.getAsk()) > (order.getInstrument().getPipValue() * (breakeven / 10))) {
                             double price = tick.getAsk() + (order.getInstrument().getPipValue() * ((breakeven / 10) - 2));
                             order.setStopLossPrice(price, OfferSide.ASK);
                         } else {
                             newBreakeven = breakEven;
                         }
                     }
                 }
             }
             breakEven = newBreakeven;
         } catch (JFException ex) {
             log.error(ex);
         }
     }
 
     /*
      * 
      * sor given orderlis by fill time ascending
      */
     private void sortOrderListByFillTime(List<IOrder> ol) {
         //sorting by fill time
         Collections.sort(ol, new Comparator<IOrder>() {
             @Override
             public int compare(IOrder o1, IOrder o2) {
                 return Long.signum(o1.getFillTime() - o2.getFillTime());
             }
         });
     }
 
     private void addToMaxTradeOL(IOrder o) {
 
         if (null != o) {
             for (IOrder mo : maxTradeOrderList) {
                 if (o.getId().equals(mo.getId())) {
                     return;
                 }
             }
             maxTradeOrderList.add(o);
         }
     }
 
     private float setAccountProfit() {
         accountProfit = 0;
         try {
             for (IOrder o : engine.getOrders()) {
                 accountProfit += o.getProfitLossInAccountCurrency();
             }
         } catch (JFException ex) {
             java.util.logging.Logger.getLogger(JForexFstBridge.class.getName()).log(Level.SEVERE, null, ex);
         }
 
         return accountProfit;
     }
 
     public class Server extends Thread {
 
         String msg;
         pipeServer srvPipe;
 
         Server() {
             this.start();
         }
 
         @Override
         public void run() {
             String reply = "";
             srvPipe = new pipeServer(connId, log);
             while (started) {
                 try {
                     srvPipe.connect();
                     msg = srvPipe.read().trim();
                     log.debug("SRVread: " + msg.trim());
                     reply = processMessage(msg);
                     srvPipe.write(reply);
                     srvPipe.disconnect();
                     log.debug("SRVwrite: " + reply.substring(0, (reply.length() > 400 ? 400 : reply.length())));
                     Thread.sleep(5);
                 } catch (Exception e) {
                     if (srvPipe.connected) {
                         log.error(e);
                     }
                 }
             }
             if (!srvPipe.connected) {
                 srvPipe.closePipe();
             }
             srvPipe = null;
             log.info("Server Stopped");
         }
 
         /**
          * extract command from FST command
          *
          * @param message
          * @return
          */
         private String extractOrder(String message) {
             if (null == message || message.equals("") || message.length() < 2) {
                 log.debug("malformed message: " + message);
                 return "ER";
             }
             return message.substring(0, 2).toUpperCase();
         }
 
         /**
          * process command and call related method
          *
          * @param msg2
          * @return
          * @throws JFException
          */
         private String processMessage(String msg2) throws JFException {
             String cmd = extractOrder(msg2);
 
             if (cmd.equals("PI")) {
                 // System.out.println("ping respond");
                 return fstPing();
             } else if (cmd.equals("MA")) {
                 // System.out.println("Market Info All Respond");
                 return fstMarketInfoAll();
             } else if (cmd.equals("AI")) {
                 return fstAccountInfo();
             } else if (cmd.equals("TE")) {
                 return fstTerminalInfo();
             } else if (cmd.equals("BR")) {
                 // System.out.println(msg.split("\\s+")[4]);
                 int barCount = Integer.parseInt(msg.split("\\s+")[4].trim());
                 int offset = Integer.parseInt(msg.split("\\s+")[3].trim());
                 return fstBars(offset, barCount);
             } else if (cmd.equals("OS")) {
                 return fstOrderSend();
             } else if (cmd.equals("OC")) {
                 String[] msgArray = msg.split(" ");
                 double lotsToClose = Double.parseDouble(msgArray[2]);
                 return fstOrderClose(lotsToClose);
             } else if (cmd.equals("OM")) {
                 return fstOrderModify();
             } else if (cmd.equals("ST")) {
                 return "Stopping server....";
             }
             return "ER";
         }
 
         /**
          * creates ping iformation if time is non trading time, and it returns
          * last tick information
          *
          * @return ping in string format
          */
         private String fstPing() {
             try {
                 String response = "";
                 if (!lastTickStr.equals("")) {
                     return "OK " + lastTickStr;
                 } else {
                     ITick tick = history.getLastTick(instrument);
                     DecimalFormat df = new DecimalFormat("#.00000");
                     response += instrument.name()
                             + " "
                             + // symbol
                             periodString
                             + " "
                             + // period
                             (tick.getTime() / 1000)
                             + " "
                             + // time
                             tick.getBid()
                             + " "
                             + // bid
                             tick.getAsk()
                             + " "
                             + // ask
                             Math.round(((tick.getAsk() - tick.getBid()) * 100000))
                             + " "
                             + // spread
                             df.format(instrument.getPipValue() * 10000).replace(',', '.')
                             + " "
                             + // tickValue
                             barToStr(presentBar) + " " + // openTime, open, high, low, close, volume
                             last10BarTime + " " + // bartime10
                             account.getBalance() + " " + // accountbalance
                             account.getEquity() + " " + // accountequity
                             accountProfit + " " + // accountprofit
                             (float) Math.round((account.getEquity() - (account.getEquity() * account.getUseOfLeverage())) * 100) / 100 + " " + // accountfreemargin
                             getPositionDetails(); // position details
                 }
                 return "OK " + response;
             } catch (Exception e) {
                 log.error(e);
                 return "ER cannot get Ping details";
             }
         }
 
         /**
          * gets market information
          *
          * @return
          */
         private String fstMarketInfoAll() {
             String response = "ER";
             try {
                 DecimalFormat df = new DecimalFormat("0.00000");
                 ITick tick = history.getLastTick(instrument);
                 response = "OK " + df.format(instrument.getPipValue() / 10) + " " + // point
                         (instrument.getPipValue() > 0.0001 ? "3.00000" : "5.00000 ") + // digits
                         Math.round(((tick.getAsk() - tick.getBid()) * 100000)) + " " + // spread
                         df.format(account.getStopLossLevel()).replace(',', '.') + " " + // stopLevel
                         "100000.00000 " + // lotSize
                         df.format(instrument.getPipValue() * 10000).replace(',', '.') + " " + // tickValue
                         "0.00001 " + // tickSize
                         df.format(getLongOvernight(instrument)).replace(',', '.') + " " + // swapLong
                         df.format(getShortOvernight(instrument)).replace(',', '.') + " " + // swapShort
                         "0.00000 " + // starting
                         "0.00000 " + // expiration
                         // df.format((account.getAccountState()
                         // ==
                         // IAccount.AccountState.OK ) ?
                         // 1:0).replace(',',
                         // '.')+" "+
                         // //tradeAllowed
                         df.format(dataService.isOfflineTime(System.currentTimeMillis() / 1000) ? 1 : 0).replace(',', '.') + " " + // tradeAllowed
                         "0.01000 " + // minLot
                         "0.01000 " + // lotStep
                         "99999.00000 " + // maxLot
                         "0.00000 " + // swapType
                         "0.00000 " + // profitCalcMode
                         "0.00000 " + // marginCalcMode
                         "0.00000 " + // marginInit
                         "0.00000 " + // marginMaintenance
                         "50000.00000 " + // marginHedged
                         df.format(tick.getAsk() * 1000).replace(',', '.') + " " + // marginRequired
                         "0.00000"; // freezeLevel
             } catch (Exception e) {
                 log.error(e);
                 return "ER";
             }
             return response;
         }
 
         /**
          * gets jForex account information
          *
          * @return
          */
         private String fstAccountInfo() {
             String response = "";
             response += "OK NOT_APLLICABLE " + (account.getAccountId().length() < 1 ? 132456789 : account.getAccountId()) + " " + "Dukascopy_Bank " + "jForex-terminal "
                     + account.getCurrency().getCurrencyCode() + " " + (int) account.getLeverage() + " " + account.getBalance() + " " + account.getEquity() + " "
                     + (account.getEquity() - account.getBalance()) + " " + "0.00 " + (account.getBalance() * account.getUseOfLeverage()) + " " + "1 "
                     + (account.getBalance() - (account.getBalance() * account.getUseOfLeverage())) + " " + "0 " + (account.getMarginCutLevel() / 10) + " "
                     + ((engine.getType() == IEngine.Type.LIVE) ? 0 : 1);
             return response;
         }
 
         private String fstTerminalInfo() {
             // OK MetaTrader_-_Alpari_UK Alpari_(UK)_Ltd.
             // C:|Program_Files|MetaTrader_-_Alpari_UK 1.10 1.4
             String respond = "OK jForex_-_Dukascopy_Bank_SA " + "not_relevant " + "1.10 1.4";
 
             return respond;
         }
 
         /**
          *
          * @param offsetFrom
          * @param offsetTo
          * @return bars in String format from allow given offset
          */
         private String fstBars(int offsetFrom, int offsetTo) {
             java.util.List<IBar> bars = null;
             String result = "OK " + instrument.name() + " " + periodString + " 2000 " + offsetFrom + " ";
             try {
                 long to = history.getBarStart(period, history.getTimeOfLastTick(instrument));
                 long from = to - (period.getInterval() * (offsetTo*2));
                 boolean offline = false;
                 for (int i = 0; i<500 ; i++) {
                     offline = dataService.isOfflineTime(from);
                     if (offline) {
                         from -= period.getInterval()*10; 
                     }
                     else{
                         break;
                     }
                 }
                 log.debug("time from: " + new Date(from));
                 log.debug("time to :" + new Date(to));
                 for (int i = 0; i < 20 && null == bars; i++) {
                     if (offsetFrom == 1) {
                         //bars = history.getBars(instrument, period, OfferSide.ASK, Filter.ALL_FLATS, offsetTo + 10, to, 0);
                         bars = history.getBars(instrument, period, OfferSide.BID, Filter.ALL_FLATS, from, to);
                     }
                     if (null != bars && (bars.size() >= (offsetTo - offsetFrom))) {
                         log.debug(bars + "");
                         break;
                     }
                     log.info("waiting for bars loading...");
                     log.debug(bars + "");
                     Thread.sleep(1000);
                 }
                 IBar ibar;
                 int totalBars = (bars.size() - 1) - offsetFrom;
                 int returnBars = 0;
                 String barsString = "";
                 for (int i = totalBars; i >= ((bars.size() - 1) - (offsetTo)); i--) {
                     ibar = bars.get(i);
                     if (barsString.length() > 50000) {
                         break;
                     }
                     barsString += " " + barToStr(ibar);
                     returnBars++;
                 }
                 result += (returnBars) + "" + barsString;
             } catch (Exception e) {
                 log.error("cannot load bars");
                 log.error(e);
             }
             return result;
         }
 
         /**
          *
          * @return next unique label for new orders
          */
         private String getNextLabel() {
             return "S" + magic + "_" + (maxTicket + 1);
         }
 
         /**
          * sends an order to jForex from FST
          *
          * @return
          */
         private String fstOrderSend() {
 
             if (isOfflineTime()) {
                 return "OK 0";
             }
 
             try {
                 double stoplossprice = 0, takeprofitPrice = 0, realAmount = 0;
                 OrderCommand direction;
                 // OS EURUSD 1 1 1.27206 21 0 0 0 0 TS1=0;BRE=0
                 String[] order = msg.split(" ");
                 parseOrderParameters(order[10]);
                 double amount = (Double.parseDouble(order[3])) / 10;
                 double price = Double.parseDouble(order[4]);
                 int stoploss = Integer.parseInt(order[6]);
                 if (stoploss == 0 && trailingStop > 0) {
                     stoploss = trailingStop;
                 }
                 int takeprofit = Integer.parseInt(order[7]);
                 String label = getNextLabel();
                 String comment = consecutiveLosses + ";" + activatedSL + ";" + activatedTP + ";" + closedSLTPLots;
                 if (Integer.parseInt(order[2]) == 0) {
                     direction = OrderCommand.BUY;
                     realAmount = amount;
                     if (price == 0) {
                         price = history.getLastTick(instrument).getAsk();
                     }
                     if (stoploss > 0) {
                         stoplossprice = price - ((stoploss / 10) * instrument.getPipValue());
                     }
                     if (takeprofit > 0) {
                         takeprofitPrice = price + ((takeprofit / 10) * instrument.getPipValue());
                     }
                 } else {
                     direction = OrderCommand.SELL;
                     realAmount = -amount;
                     if (price == 0) {
                         price = history.getLastTick(instrument).getBid();
                     }
                     if (stoploss > 0) {
                         stoplossprice = price + ((stoploss / 10) * instrument.getPipValue());
                     }
                     if (takeprofit > 0) {
                         takeprofitPrice = price - ((takeprofit / 10) * instrument.getPipValue());
                     }
                 }
 
                 if (checkUsedLeverage(realAmount) && checkMaxTrade()) {
                     log.info("trading allowed");
 
                     setTrailingStop(Integer.parseInt(order[10].split(";")[0].split("=")[1]));
                     setBreakEven(Integer.parseInt(order[10].split(";")[1].split("=")[1]));
                     if (null != position) { // there is allow position
                         if (direction.isLong() == position.isLong()) {
                             OrderTask task = new OrderTask(label, instrument, direction, amount, stoplossprice, takeprofitPrice, comment);
                             context.executeTask(task);
                         } else {
                             return fstOrderClose(amount * 10);
                         }
                     } else {
                         OrderTask task = new OrderTask(label, instrument, direction, amount, stoplossprice, takeprofitPrice, comment);
                         context.executeTask(task);
                     }
                     int waitFor = 1;
                     while (newOrder == null && waitFor < 100) {
                         try {
                             if (newOrder == null) {
                                 for (IOrder o : engine.getOrders()) {
                                     if (o.getLabel().equals(label)) {
                                         o = newOrder;
                                         break;
                                     }
                                 }
                                 wait(50);
                                 waitFor++;
                             }
                         } catch (Exception e) {
                             // newOrder = position;
                         }
                     }
                     if (newOrder != null) {
                         if (newOrder.getState() == com.dukascopy.api.IOrder.State.FILLED) {
                             newOrder = null;
                             return "OK " + maxTicket;
                         }
                     }
                 } else {
                     return "OK 00000000";
                 }
             } catch (Exception e) {
                 // console.getOut().print(e.getMessage());
                 log.error(e);
             }
             return "ER";
 
         }
 
         private void setBreakEven(int brEvPips) {
             if (brEvPips >= 100) {
                 breakEven = brEvPips;
             } else {
                 breakEven = 0;
             }
         }
 
         private void setTrailingStop(int trStPips) {
             if (trStPips >= 100) {
                 trailingStop = trStPips;
             } else {
                 trailingStop = 0;
             }
         }
 
         /**
          * closes orders opened by this strategies lasts as long as the amount
          * of
          *
          * @param lotsToClose desired closing amount
          * @return
          */
         private String fstOrderClose(double lotsToClose) {
 
             if (isOfflineTime()) {
                 return "OK 0";
             }
 
             log.info("close: " + lotsToClose);
             try {
                 int tryes = 0;
                 for (IOrder o : orderList) {
                     if ((o.getAmount() * 10) <= lotsToClose) {
                         log.debug("trying to close: " + o.getLabel());
                         lotsToClose -= o.getAmount() * 10;
                         closeTask closetask = new closeTask(o, o.getAmount());
                         context.executeTask(closetask);
                         log.info("closed: " + tryes + " remaining lots: " + lotsToClose);
                     } else {
                         log.debug("trying to close: " + o.getLabel());
                         closeTask closetask = new closeTask(o, lotsToClose / 10);
                         context.executeTask(closetask);
                         lotsToClose = 0;
                         log.info("closed: " + tryes);
                     }
                     if (lotsToClose == 0) {
                         break;
                     }
                     Thread.sleep(10);
                     tryes++;
                 }
             } catch (Exception e) {
                 log.error(e);
                 return "ER";
             }
             return "OK 0";
         }
 
         private void parseOrderParameters(String parameters) {
             int brEven = Integer.parseInt(parameters.split(";")[1].substring(4));
             int trStop = Integer.parseInt(parameters.split(";")[0].substring(4));
             setTrailingStop(trStop);
 
             if (brEven < trStop) {
                 setBreakEven(brEven);
             } else {
                 setBreakEven(0);
             }
         }
 
         /**
          * Modify given order SL ad TP levels
          *
          * @return order
          */
         private String fstOrderModify() {
 
             if (isOfflineTime()) {
                 return "OK 0";
             }
 
             String[] msgArray = msg.split(" ");
             parseOrderParameters(msgArray[6]);
             double price = Double.parseDouble(msgArray[2]);
             int stoploss = Integer.parseInt(msgArray[3]);
             if (stoploss == 0 && trailingStop > 0) {
                 stoploss = trailingStop;
             }
             int takeprofit = Integer.parseInt(msgArray[4]);
             double stoplossprice = 0, takeprofitPrice = 0;
             OfferSide offSide;
 
             if (position.isLong()) {
                 if (stoploss > 0) {
                     stoplossprice = price - ((double) (stoploss / 10) * instrument.getPipValue());
                 }
                 if (takeprofit > 0) {
                     takeprofitPrice = price + ((double) (takeprofit / 10) * instrument.getPipValue());
                 }
                 offSide = OfferSide.ASK;
             } else {
                 if (stoploss > 0) {
                     stoplossprice = price + ((double) (stoploss / 10) * instrument.getPipValue());
                 }
                 if (takeprofit > 0) {
                     takeprofitPrice = price - ((double) (takeprofit / 10) * instrument.getPipValue());
                 }
                 offSide = OfferSide.BID;
             }
             for (IOrder iOrder : orderList) {
                 if (position.isLong() != iOrder.isLong()) {
                     closeTask closetask = new closeTask(iOrder, iOrder.getAmount());
                     context.executeTask(closetask);
                     log.info("positions in different directions!: " + magic);
                     continue;
                 }
                 if (iOrder.getStopLossPrice() != stoplossprice) {
                     modifyStoploss mSL = new modifyStoploss(iOrder, stoplossprice, offSide);
                     context.executeTask(mSL);
                 }
                 if (iOrder.getTakeProfitPrice() != takeprofitPrice) {
                     modifyTakeProfit mTP = new modifyTakeProfit(iOrder, takeprofitPrice);
                     context.executeTask(mTP);
                 }
             }
             return "OK";
         }
 
         /**
          *
          * @param instrument
          * @return specifyed instrument long swap
          */
         private double getLongOvernight(Instrument instrument) {
             try {
                 Map<Instrument, Double> map = TesterFactory.getDefaultInstance().getOvernights().longValues;
                 for (Instrument key : map.keySet()) {
                     if (key == instrument) {
                         return map.get(key);
                     }
                 }
             } catch (Exception e) {
                 log.error(e);
             }
             return 0;
         }
 
         /**
          *
          * @param instrument
          * @return specifyed instrument short swap
          */
         private double getShortOvernight(Instrument instrument) {
             try {
                 Map<Instrument, Double> map = TesterFactory.getDefaultInstance().getOvernights().shortValues;
                 for (Instrument key : map.keySet()) {
                     if (key == instrument) {
                         return map.get(key);
                     }
                 }
             } catch (Exception e) {
                 log.error(e);
             }
             return 0;
         }
 
         /*
          * private void print(Object o){ debugLog(o); }
          */
         private boolean checkUsedLeverage(double amount) {
             int existingDirection = 1;
             if (null != position) {
                 existingDirection = position.isLong() ? 1 : -1;
             }
 
             double currentLeverageUsed = account.getUseOfLeverage() * existingDirection;
             log.debug("current leverage: " + currentLeverageUsed + "");
             double bal = account.getBalance();
             double nl = (currentLeverageUsed + ((Math.abs(amount) * 1000000) / bal));
             log.debug("new leverage: " + nl);
             boolean allow = Math.abs((int) nl) < maxLeverage;
             log.debug("leverageLimit opening allowed: " + allow);
             if (!allow) {
                 log.info("position opening denied by Leverage Limiter!");
             }
             return allow;
         }
 
         private boolean checkMaxTrade() {
             boolean allow = false;
             for (int i = 0; i < maxTradeOrderList.size(); i++) {
                 IOrder o = maxTradeOrderList.get(i);
                 if (now - (Period.DAILY.getInterval()) > o.getFillTime()) {
                     maxTradeOrderList.remove(i);
                 }
                 log.debug("maxTradeOrderList : " + o);
             }
             sortOrderListByFillTime(maxTradeOrderList);
             if (maxTradeOrderList.size() < maxTrade) {
                 allow = true;
             }
             log.debug("orders count by last day: " + maxTradeOrderList.size() + " trading allowed: " + allow);
             if (!allow) {
                 log.info("position opening denied by MaxTradePerDay Limiter!");
             }
             return allow;
         }
 
         private boolean isOfflineTime() {
             boolean offLineTime = true;
             try {
                 offLineTime = dataService.isOfflineTime(history.getTimeOfLastTick(instrument));
                 log.debug("offlineTime: " + offLineTime);
             } catch (JFException ex) {
                 log.error(ex);
             }
             if (offLineTime) {
                 log.info("at this time trading is not allowed...");
             }
             return offLineTime;
         }
     }
 
     /**
      * sends an order to jForex terminal from strategy outer thread
      */
     public class OrderTask implements Callable<IOrder> {
 
         private Instrument instrument;
         private double amount, stoplossprice, takeProfitPrice;
         private OrderCommand command;
         private String label, comment;
 
         public OrderTask(String label, Instrument instrument, OrderCommand command, double amount, double stoplossprice, double takeProfitPrice, String comment) {
             this.instrument = instrument;
             this.stoplossprice = stoplossprice;
             this.takeProfitPrice = takeProfitPrice;
             this.amount = amount;
             this.command = command;
             this.label = label;
             this.comment = comment;
 
         }
 
         @Override
         public IOrder call() {
             try {
                 System.out.println(label + " " + instrument + " " + command + " " + amount + " " + stoplossprice + " " + takeProfitPrice + " " + comment);
                 return engine.submitOrder(this.label, this.instrument, this.command, this.amount, 0, 5, this.stoplossprice, this.takeProfitPrice, 0, this.comment);
                 // return engine.submitOrder("test123", Instrument.EURUSD ,
                 // OrderCommand.BUY, 0.1 , 0, 5, 0,0);
             } catch (Exception e) {
                 log.error(e);
                 return null;
             }
         }
     }
 
     /**
      * closes an order in jForex terminal from strategy outer thread
      */
     public class closeTask implements Callable<IOrder> {
 
         IOrder order;
         double amount;
 
         public closeTask(IOrder order, double amount) {
             this.order = order;
             this.amount = amount;
         }
 
         @Override
         public IOrder call() throws Exception {
             if (amount == order.getAmount()) {
                 order.close();
             } else {
                 order.close(amount);
             }
             return order;
         }
     }
 
     /**
      * modify an order stoploss to jForex terminal from strategy outer thread
      */
     public class modifyStoploss implements Callable<IOrder> {
 
         IOrder order;
         double stopLossPrice;
         OfferSide offSide;
 
         public modifyStoploss(IOrder order, double stopLossPrice, OfferSide offSide) {
             this.order = order;
             this.stopLossPrice = stopLossPrice;
             this.offSide = offSide;
         }
 
         @Override
         public IOrder call() throws Exception {
             order.setStopLossPrice(stopLossPrice, offSide);
             return order;
         }
     }
 
     /**
      * modify an order take profit to jForex terminal from strategy outer thread
      */
     public class modifyTakeProfit implements Callable<IOrder> {
 
         IOrder order;
         double takeprofitPrice;
 
         public modifyTakeProfit(IOrder order, double takeprofitPrice) {
             this.order = order;
             this.takeprofitPrice = takeprofitPrice;
         }
 
         @Override
         public IOrder call() throws Exception {
             order.setTakeProfitPrice(takeprofitPrice);
             return order;
         }
     }
 
     /**
      *
      * @param bar
      * @return bar data in string format
      */
     public String barToStr(IBar bar) {
         String rs = "";
         rs += (int) (bar.getTime() / 1000) + " ";
         rs = rs + bar.getOpen() + " ";
         rs = rs + bar.getHigh() + " ";
         rs = rs + bar.getLow() + " ";
         rs += bar.getClose() + " ";
         rs += Math.round(bar.getVolume() * 10) / 10;
         return rs;
     }
 
     /**
      * searches all filled orders opened by this strategy
      */
     private void getHistory() {
         getPositions();
         getCurrentBar();
         IOrder lastOrder = position;
         try {
             now = history.getTimeOfLastTick(instrument);
             log.debug("getting order history...");
             List<IOrder> allHistory = history.getOrdersHistory(instrument, now - (Period.WEEKLY.getInterval()), now);
             for (IOrder o : allHistory) {
                 if (checkLabel(o) && o.getState() == IOrder.State.CLOSED) {
                     if (o.getFillTime() > (now - (Period.DAILY.getInterval()))) {
                         addToMaxTradeOL(o);
                     }
                     lastOrder = o;
                     if (maxTicket < getOrderTicket(o)) {
                         maxTicket = getOrderTicket(o);
                     }
                 }
             }
             for (IOrder o : orderList) {
                 if (o.getFillTime() > (now - (Period.DAILY.getInterval()))) {
                     addToMaxTradeOL(o);
                 }
             }
         } catch (JFException ex) {
             log.error(ex);
         }
         setMartingaleDetails(lastOrder);
     }
 
     public void setMartingaleDetails(IOrder o) {
         if (null != o) {
             String comment = o.getComment();
             try {
                 consecutiveLosses = Integer.parseInt(comment.split(";")[0]);
                 activatedSL = Double.parseDouble(comment.split(";")[1]);
                 activatedTP = Double.parseDouble(comment.split(";")[2]);
                 closedSLTPLots = Double.parseDouble(comment.split(";")[3]);
             } catch (Exception e) {
                 log.error(e);
                 consecutiveLosses = 0;
                 activatedSL = 0;
                 activatedTP = 0;
                 closedSLTPLots = 0;
             }
 
             if (o.getProfitLossInUSD() > 0) {
                 consecutiveLosses = 0;
             } else {
                 consecutiveLosses++;
             }
         } else {
             consecutiveLosses = 0;
             activatedSL = 0;
             activatedTP = 0;
             closedSLTPLots = 0;
         }
         log.debug("maxTicket: " + maxTicket);
         log.debug("consecutiveLosses :" + consecutiveLosses);
         log.debug("activatedSL :" + activatedSL);
         log.debug("activatedTP :" + activatedTP);
         log.debug("closedSLTPLots :" + closedSLTPLots);
     }
 
     /**
      * set allPositionAmount, maxTicket and orderList var of this bridge live
      * orders from Dukas srvPipe, ordered by fill time
      */
     private void getPositions() {
         allPositionAmount = 0;
         List<IOrder> ol = new ArrayList<IOrder>();
         try {
             for (IOrder order : engine.getOrders()) {
                 if (checkLabel(order) && order.getState() == IOrder.State.FILLED) {
                     ol.add(order);
                     log.debug("order by this strategy: " + order);
                     allPositionAmount += (order.getAmount() * 10);
                     if (maxTicket < getOrderTicket(order)) {
                         maxTicket = getOrderTicket(order);
                     }
                 }
             }
             sortOrderListByFillTime(ol);
 
         } catch (JFException e) {
             log.error(e);
         }
         if (!ol.isEmpty()) {
             position = ol.get(ol.size() - 1);
         } else {
             position = null;
         }
         this.orderList = ol;
     }
 
     /**
      *
      * @param o - IOrder
      * @return emulated orderTicket
      */
     private int getOrderTicket(IOrder o) {
         return Integer.parseInt(o.getLabel().split("_")[1]);
     }
 
     /**
      *
      * @param order - IOrder
      * @return true if checked order opened with this strategy (compares MAGIC)
      */
     private boolean checkLabel(IOrder order) {
         return order.getLabel().replace("S", "").split("_")[0].equals(magic + "");
     }
 
     /**
      *
      * @return all opened position profit
      */
     private double getPositionProfit() {
         double posProfit = 0;
         for (IOrder order : orderList) {
             posProfit += order.getProfitLossInAccountCurrency();
         }
         return posProfit;
     }
 
     /**
      *
      * @return position details
      */
     private String getPositionDetails() {
         String rtnString = "";
         if (!orderList.isEmpty()) {
             rtnString += "" + maxTicket; // apositionTicket
             rtnString += " " + (position.isLong() ? 0 : 1); // string
             // apositionType
             // 0BUY 1SELL
             rtnString += " " + (allPositionAmount);
             rtnString += " " + position.getOpenPrice();
             rtnString += " " + (int) (position.getFillTime() / 1000);
             rtnString += " " + position.getStopLossPrice();
             rtnString += " " + position.getTakeProfitPrice();
             rtnString += " " + ((double) Math.round(getPositionProfit() * 100) / 100);
             rtnString += " ID=" + connId + ",_MAGIC=" + magic + getMartingaleDetails();
         } else {
             rtnString = "0 -1 0.00 0.00000 1577836800 0.00000 0.00000 0.00 " + getMartingaleDetails();
         }
         return rtnString;
     }
 
     /**
      * return proper format of martingale details to FST
      *
      * @return
      */
     public String getMartingaleDetails() {
         DecimalFormat df = new DecimalFormat("0.00000");
         return " cl=" + consecutiveLosses + ";aSL=" + df.format(activatedSL).replace(',', '.') + ";aTP=" + df.format(activatedTP).replace(',', '.') + ";al="
                 + df.format(closedSLTPLots).replace(',', '.');
     }
 
     private void getCurrentBar() {
         try {
             List<IBar> bars = null;
             long to = history.getBarStart(period, history.getTimeOfLastTick(instrument));
             long from = to - (period.getInterval() * 13);
             log.debug("time from: " + new Date(from));
             log.debug("time to :" + new Date(to));
             for (int i = 0; i < 20 && null == bars; i++) {
                 bars = history.getBars(instrument, period, OfferSide.BID, Filter.ALL_FLATS, from, to);
                 if (null != bars && bars.size() > 12) {
                     log.debug("lastbar + last10Bar: "+bars);
                     continue;
                 }
                 from -= period.getInterval() * 10;
                 if (null == bars) {
                     log.info("waiting for bars loading...");
                     log.debug(bars + "");
                     Thread.sleep(1000);
                 }
                 bars = null;
             }
             presentBar = bars.get(bars.size() - 1);
             last10BarTime = bars.get(bars.size() - 11).getTime() / 1000;
             log.debug("presentBar: "+presentBar);
             log.debug("last10thBatrTime: "+last10BarTime);
         } catch (Exception ex) {
             log.error(ex);
         }
     }
 }
