 package net.toddsarratt.GaussTrader;
 
 /*	
  * bollingerBand[0] = moving average (period determined by command line args, default 20DMA)
  * SD multiple determined by command line, defaults below
  * bollingerBand[1] = upperBoll1 = MA + 2 standard deviation
  * bollingerBand[2] = upperBoll2 = MA + 2.5 standard deviations
  * bollingerBand[3] = lowerBoll1 = MA - 2 standard deviation
  * bollingerBand[4] = lowerBoll2 = MA - 2.5 standard deviations
  * bollingerBand[5] = lowerBoll3 = MA - 3 standard deviations
  */
 
 import org.joda.time.DateTime;
 import org.joda.time.DateTimeZone;
 import org.joda.time.MutableDateTime;
 import org.joda.time.format.DateTimeFormat;
 import org.joda.time.format.DateTimeFormatter;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.net.URL;
 import java.util.*;
 
 public class Stock extends Security {
 
    /*	private class Dividend {
   DateTime dividendDate;
   double dividendPayment;
   }
    */
    private String ticker = "AAPL";
    private double price = 0.00;
    private long lastPriceUpdateEpoch = 0l;
    private double fiftyDma = 0.00;
    private double twoHundredDma = 0.00;
    private LinkedHashMap<Long, Double> historicalPriceMap = new LinkedHashMap<>(PRICE_TRACKING_MAP);
    Boolean tickerValid = null;
    //	public LinkedList<Dividend> dividendsPaid = null;
    private double[] bollingerBand = new double[6];
    private static final int PRICES_NEEDED = GaussTrader.bollBandPeriod;
    private static final DateTimeFormatter LAST_TICK_FORMATTER = DateTimeFormat.forPattern("MM/dd/yyyy hh:mmaa");
    private static final Logger LOGGER = LoggerFactory.getLogger(Stock.class);
    private static final Map<Long, Double> PRICE_TRACKING_MAP = new HashMap<>();
    private static final DateTime EARLIEST_PRICE_DATE = earliestHistoricalPriceNeeded();
 
    Stock() {
    }
 
    Stock(String ticker) throws IOException {
       LOGGER.debug("Entering constructor Stock(String {})", ticker);
       this.ticker = ticker;
       populateStockInfo();
       populateHistoricalPricesDB();
       addPriceMapToDB(populateHistoricalPricesYahoo());
       calculateBollingerBands();
    }
 
    void populateStockInfo() {
       LOGGER.debug("Entering Stock.populateStockInfo()");
       String[] quoteString;
      for(int yahooAttempt = 1; yahooAttempt <= GaussTrader.YAHOO_RETRIES; yahooAttempt++) {
         try {
            quoteString = askYahoo(ticker, "l1d1t1m3m4");
            price = Double.parseDouble(quoteString[0]);
            lastPriceUpdateEpoch = jodaLastTickToEpoch(quoteString[1] + " " + quoteString[2]);
            fiftyDma = Double.parseDouble(quoteString[3]);
            twoHundredDma = Double.parseDouble(quoteString[4]);
            return;
         } catch (IOException ioe) {
            LOGGER.info("Caught IOException ({}) connecting to Yahoo! to populate stock info for {}", yahooAttempt, ticker);
            LOGGER.debug("Caught (IOException ioe) ", ioe);
         }
       }
    }
 
    void populateHistoricalPricesDB() {
    /* Populate stock price history from DB */
       LOGGER.debug("Entering Stock.populateHistoricalPricesDB()");
       LinkedHashMap<Long, Double> priceMapFromDB = DBHistoricalPrices.getDBPrices(ticker, EARLIEST_PRICE_DATE);
       if (!priceMapFromDB.isEmpty()) {
          for (Long epochInDb : priceMapFromDB.keySet()) {
             if (historicalPriceMap.containsKey(epochInDb)) {
                LOGGER.debug("Required price from epoch {} returned from db, updating price in historicalPriceMap", epochInDb);
                historicalPriceMap.put(epochInDb, priceMapFromDB.get(epochInDb));
             } else {
                LOGGER.warn("Price from epoch {} returned from db but not required per PRICE_TRACKING_MAP", epochInDb);
             }
          }
       }
    }
 
    HashMap<Long, Double> populateHistoricalPricesYahoo() {
 	/* historicalPriceMap was built from PRICE_TRACKING_MAP which contains all necessary epochs as keys and -1.0 for every value, 
 	 * which should be replaced first from the local database first and then supplemented by Yahoo!
 	 * This method returns a map of blahblahblah
 	 */
       LOGGER.debug("Entering Stock.populateHistoricalPricesYahoo()");
       MissingPriceDateRange priceRangeToDownload;
       LinkedHashMap<Long, Double> retrievedYahooPriceMap;
       HashMap<Long, Double> pricesMissingFromDB = new HashMap<>();
 
       if (historicalPriceMap.containsValue(-1.0)) {
          LOGGER.debug("Calculating date range for missing stock prices.");
          priceRangeToDownload = getMissingPriceDateRange();
          if (!priceRangeToDownload.earliest.isAfter(priceRangeToDownload.latest.toInstant())) {
             try {
                retrievedYahooPriceMap = retrieveYahooHistoricalPrices(priceRangeToDownload);
                for (long epochToCheck : historicalPriceMap.keySet()) {
                   if (historicalPriceMap.get(epochToCheck) < 0.00) {
                      if (retrievedYahooPriceMap.containsKey(epochToCheck)) {
                         historicalPriceMap.put(epochToCheck, retrievedYahooPriceMap.get(epochToCheck));
                         pricesMissingFromDB.put(epochToCheck, retrievedYahooPriceMap.get(epochToCheck));
                      } else {
                         LOGGER.warn("historicalPriceMap requires price for epoch {} but data missing from both DB and yahoo!", epochToCheck);
                      }
                   }
                }
             } catch (IOException ioe) {
                LOGGER.info("Could not connect to Yahoo! to get historical prices");
                LOGGER.debug("Caught (IOException ioe) {}", ioe);
             }
          } else {
             LOGGER.warn("historicalPriceMap.containsValue(-1.0) but " +
                "priceRangeToDownload.earliest.isAfter(priceRangeToDownload.latest.toInstant() ({} after {})",
                priceRangeToDownload.earliest.toString(), priceRangeToDownload.latest.toString());
          }
       } else {
          LOGGER.debug("historicalPriceMap.containsValue(-1.0) is false, all needed prices have been retrieved from the database. Not calling Yahoo!");
       }
       return pricesMissingFromDB;
    }
 
    void addPriceMapToDB(Map<Long, Double> pricesMissingFromDB) {
       LOGGER.debug("Entering Stock.addPriceMapToDB(Map<Long, Double> {})", pricesMissingFromDB.toString());
       for (Long priceEpoch : pricesMissingFromDB.keySet()) {
          DBHistoricalPrices.addStockPrice(ticker, priceEpoch, pricesMissingFromDB.get(priceEpoch));
       }
    }
 
    private static DateTime earliestHistoricalPriceNeeded() {
 	/* 
 	 * Count backwards starting from yesterday the number of days the market was open
 	 * Stop when the number of days needed to calculate bollinger bands (usually 20 days) is reached
 	 * Set time to 420pm (Usual 4pm market close + 20min yahoo! price delay)
 	 * Using long(epoch) in database to avoid MutableDateTime to sql.Date conversion hackery
 	 * 420pm in milliseconds = 16th hour * 60min/hour + 20min * 60sec/min * 1000ms/sec
 	 */
       LOGGER.debug("Entering Stock.earliestHistoricalPriceNeeded()");
       MutableDateTime earliestDatePriceNeeded = new MutableDateTime(DateTimeZone.forID("America/New_York"));
       earliestDatePriceNeeded.setMillisOfDay((16 * 60 + 20) * 60 * 1000);
       LOGGER.debug("Looking for valid open market dates backwards from {} ({})", earliestDatePriceNeeded.getMillis(), earliestDatePriceNeeded.toString());
       for (int checkedDates = 0; checkedDates < PRICES_NEEDED; checkedDates++) {
          earliestDatePriceNeeded.addDays(-1);
          while (!DBHistoricalPrices.marketWasOpen(earliestDatePriceNeeded)) {
             earliestDatePriceNeeded.addDays(-1);
          }
          PRICE_TRACKING_MAP.put(earliestDatePriceNeeded.getMillis(), -1.0);
          LOGGER.debug("PRICE_TRACKING_MAP.put({}, {}) == {}", earliestDatePriceNeeded.getMillis(), -1.0, earliestDatePriceNeeded.toString());
       }
       LOGGER.debug("Returning earliest date required for adjusted close {} ({})", earliestDatePriceNeeded.getMillis(), earliestDatePriceNeeded.toString());
       LOGGER.debug("PRICE_TRACKING_MAP == {}", PRICE_TRACKING_MAP.toString());
       return new DateTime(earliestDatePriceNeeded, DateTimeZone.forID("America/New_York"));
    }
 
    private MissingPriceDateRange getMissingPriceDateRange() {
       LOGGER.debug("Entering Stock.getMissingPriceDateRange()");
       MissingPriceDateRange mpdr = new MissingPriceDateRange();
       MutableDateTime histMutDateTime = new MutableDateTime(DateTimeZone.forID("America/New_York"));
       DateTime missingDateTimeToCheck;
 
         /* Adjust for 4pm close + 20min Yahoo! delay and subtract one day  */
       histMutDateTime.setMillisOfDay((16 * 60 + 20) * 60 * 1000);
       histMutDateTime.addDays(-1);
 
       LOGGER.debug("Setting mpdr.latest to {} ({})", EARLIEST_PRICE_DATE.getMillis(), EARLIEST_PRICE_DATE.toString());
       mpdr.latest = new DateTime(EARLIEST_PRICE_DATE, DateTimeZone.forID("America/New_York"));
       LOGGER.debug("mpdr.latest = {} ({})", mpdr.latest.getMillis(), mpdr.latest.toString());
       LOGGER.debug("mpdr.earliest = {} ({})", mpdr.earliest.getMillis(), mpdr.earliest.toString());
       for (long epochPriceRequired : historicalPriceMap.keySet()) {
 	    /* Map defaults all prices == -1.00 which is updated to a real price returned from the DB */
 	    /* Any remaining price of -1.00 needs to be retrieved from Yahoo! */
          if (historicalPriceMap.get(epochPriceRequired) < 0.00) {
             missingDateTimeToCheck = new DateTime(epochPriceRequired, DateTimeZone.forID("America/New_York"));
             LOGGER.debug("historicalPriceMap.get({}) < 0.00 : {}", epochPriceRequired, missingDateTimeToCheck.toString());
             if (mpdr.earliest.isAfter(missingDateTimeToCheck.toInstant())) {
                LOGGER.debug("mpdr.earliest.isAfter(missingDateTimeToCheck.toInstant()) : ({}).isAfter({})",
                   mpdr.earliest.toString(), missingDateTimeToCheck.toString());
                mpdr.earliest = new DateTime(missingDateTimeToCheck, DateTimeZone.forID("America/New_York")).withTime(16, (GaussTrader.delayedQuotes ? 20 : 0), 0, 0);
                LOGGER.debug("Updating mpdr.earliest = {} ({})", mpdr.earliest.getMillis(), mpdr.earliest.toString());
             }
             if (mpdr.latest.isBefore(missingDateTimeToCheck.toInstant())) {
                LOGGER.debug("mpdr.latest.isBefore(missingDateTimeToCheck.toInstant()) : ({}).isBefore({})", mpdr.latest.toString(), missingDateTimeToCheck.toString());
                mpdr.latest = missingDateTimeToCheck;
                LOGGER.debug("Updating mpdr.latest = {} ({})", mpdr.latest.getMillis(), mpdr.latest.toString());
             }
          }
       }
 	/*
 	for(int checkedPrices = 0; checkedPrices < PRICES_NEEDED; checkedPrices++) {
 	    LOGGER.debug("Looking for valid open market date starting with {} ({})", histMutDateTime.getMillis(), histMutDateTime.toString());
 	    while(!DBHistoricalPrices.marketWasOpen(histMutDateTime)) {
 		histMutDateTime.addDays(-1);
 	    }
 	    LOGGER.debug("historicalPriceMap.isEmpty() {}", historicalPriceMap.isEmpty());
 	    LOGGER.debug("historicalPriceMap.containsKey {} = {}", histMutDateTime.getMillis(),
 	    		historicalPriceMap.containsKey(histMutDateTime.getMillis()) );
 	    if(!historicalPriceMap.containsKey(histMutDateTime.getMillis())) {
 		if(mpdr.latest.isBefore(mpdr.earliest)) {
 		    LOGGER.debug("Setting latest date to {} ({})", histMutDateTime.getMillis(), histMutDateTime.toString());
 		    mpdr.latest.setDate(histMutDateTime);
 		}
 		mpdr.earliest.setDate(histMutDateTime);
 		LOGGER.debug("Setting earliest date to {} ({})", histMutDateTime.getMillis(), histMutDateTime.toString());
 	    }
 	    histMutDateTime.addDays(-1);
 	}
 	*/
       if (historicalPriceMap.isEmpty()) {
          LOGGER.info("No current prices stored so need to get everything from Yahoo!");
          LOGGER.info("Setting earliest date to {} ({})", EARLIEST_PRICE_DATE.getMillis(), EARLIEST_PRICE_DATE.toString());
          mpdr.latest = new DateTime(DateTimeZone.forID("America/New_York"));
          mpdr.earliest = new DateTime(EARLIEST_PRICE_DATE, DateTimeZone.forID("America/New_York"));
       }
       LOGGER.debug("Returning latest date {} ({})", mpdr.latest.getMillis(), mpdr.latest.toString());
       LOGGER.debug("Returning earliest date {} ({})", mpdr.earliest.getMillis(), mpdr.earliest.toString());
       return mpdr;
    }
 
    void calculateBollingerBands() {
       LOGGER.debug("Entering Stock.calculateBollingerBands()");
       int period = GaussTrader.bollBandPeriod;
       Collection<Double> historicalPriceArray;
 
       if (historicalPriceMap.containsValue(-1.0)) {
          LOGGER.warn("Not enough historical data to calculate Bollinger Bands for {}", ticker);
          bollingerBand[0] = -1.0;
          bollingerBand[1] = -1.0;
          bollingerBand[2] = -1.0;
          bollingerBand[3] = -1.0;
          bollingerBand[4] = -1.0;
          bollingerBand[5] = -1.0;
       } else {
          double currentSMASum = 0;
          double currentSMA;
          double currentSDSum = 0;
          double currentSD;
          historicalPriceArray = historicalPriceMap.values();
          for (double adjClose : historicalPriceArray) {
             currentSMASum += adjClose;
          }
          LOGGER.debug("currentSMASum = {}", currentSMASum);
          currentSMA = currentSMASum / period;
          LOGGER.debug("currentSMA = {}", currentSMA);
          for (double adjClose : historicalPriceArray) {
             currentSDSum += Math.pow((adjClose - currentSMA), 2);
          }
          LOGGER.debug("currentSDSum = {}", currentSDSum);
          currentSD = Math.sqrt(currentSDSum / period);
          LOGGER.debug("currentSD = {}", currentSD);
          bollingerBand[0] = currentSMA;
          bollingerBand[1] = currentSMA + currentSD * GaussTrader.bollingerSD1;
          bollingerBand[2] = currentSMA + currentSD * GaussTrader.bollingerSD2;
          bollingerBand[3] = currentSMA - currentSD * GaussTrader.bollingerSD1;
          bollingerBand[4] = currentSMA - currentSD * GaussTrader.bollingerSD2;
          bollingerBand[5] = currentSMA - currentSD * GaussTrader.bollingerSD3;
       }
    }
 
    private long jodaLastTickToEpoch(String yahooDateTime) {
       LOGGER.debug("Entering Stock.jodaLastTickToEpoch(String {})", yahooDateTime);
       DateTime dateTimeEasternTZ = LAST_TICK_FORMATTER.withZone(DateTimeZone.forID("America/New_York")).parseDateTime(yahooDateTime);
       LOGGER.debug("Returning {}", dateTimeEasternTZ.getMillis());
       return dateTimeEasternTZ.getMillis();
    }
 
    /*
   private long jodaYahooHistPriceDateTimeToEpoch(String yahooDate) throws ParseException {
   DateTime dateTimeEasternTZ = YAHOO_HIST_FORMATTER.withZone(DateTimeZone.forID("America/New_York")).parseDateTime(yahooDate + " 4:00pm");
   return dateTimeEasternTZ.getMillis();
   }
    */
    private String createYahooHistUrl(MissingPriceDateRange dateRange) {
 	/* http://ichart.finance.yahoo.com/table.csv?s=INTC&a=11&b=1&c=2012&d=00&e=21&f=2013&g=d&ignore=.csv 
 	 * where month January = 00 
 	 */
       LOGGER.debug("Entering Stock.createYahooHistUrl(MissingPriceDateRange dateRange)");
       StringBuilder yahooPriceArgs = new StringBuilder("http://ichart.finance.yahoo.com/table.csv?s=");
       yahooPriceArgs.append(ticker).append("&a=").append(dateRange.earliest.getMonthOfYear() - 1).append("&b=").append(dateRange.earliest.getDayOfMonth());
       yahooPriceArgs.append("&c=").append(dateRange.earliest.getYear()).append("&d=").append(dateRange.latest.getMonthOfYear() - 1);
       yahooPriceArgs.append("&e=").append(dateRange.latest.getDayOfMonth()).append("&f=").append(dateRange.latest.getYear());
       yahooPriceArgs.append("&g=d&ignore=.csv");
       LOGGER.debug("yahooPriceArgs = {}", yahooPriceArgs);
       return yahooPriceArgs.toString();
    }
 
    private LinkedHashMap<Long, Double> retrieveYahooHistoricalPrices(MissingPriceDateRange dateRange) throws IOException {
       LOGGER.debug("Entering Stock.retrieveYahooHistoricalPrices(MissingPriceDateRange dateRange)");
       LinkedHashMap<Long, Double> yahooPriceReturns = new LinkedHashMap<>();
       String inputLine;
       final URL YAHOO_URL = new URL(createYahooHistUrl(dateRange));
       BufferedReader yahooBufferedReader = new BufferedReader(new InputStreamReader(YAHOO_URL.openStream()));
 	/* First line is not added to array : "	Date,Open,High,Low,Close,Volume,Adj Close" */
       LOGGER.debug(yahooBufferedReader.readLine().replace("Date,", "Date         ").replaceAll(",", "    "));
       while ((inputLine = yahooBufferedReader.readLine()) != null) {
          String[] yahooLine = inputLine.replaceAll("[\"+%]", "").split("[,]");
          LOGGER.debug(Arrays.toString(yahooLine));
          HistoricalPrice yahooHistPrice = new HistoricalPrice(yahooLine[0], yahooLine[6]);
          yahooPriceReturns.put(yahooHistPrice.getDateEpoch(), yahooHistPrice.getAdjClose());
       }
       return yahooPriceReturns;
    }
 
 
    //	public void addDividend(Dividend dividendPayment) {
     /* http://ichart.finance.yahoo.com/table.csv?s=INTC&a=11&b=1&c=2011&d=00&e=21&f=2013&g=v&ignore=.csv */
 
    //		dividendsPaid.add(dividendPayment);
    //	}
 
    public static String[] askYahoo(String ticker, String arguments) throws IOException {
       LOGGER.debug("Entering Stock.askYahoo(String {}, String {})", ticker, arguments);
      try {
          final URL YAHOO_URL = new URL("http://finance.yahoo.com/d/quotes.csv?s=" + ticker + "&f=" + arguments);
         for(int yahooAttempt = 1; yahooAttempt <= GaussTrader.YAHOO_RETRIES; yahooAttempt++) {
             BufferedReader br = new BufferedReader(new InputStreamReader(YAHOO_URL.openStream()));
             String[] yahooResults = br.readLine().replaceAll("[\"+%]", "").split("[,]");
             LOGGER.debug("Retrieved from Yahoo! for ticker {} with arguments {} : {}", ticker, arguments, Arrays.toString(yahooResults));
             return yahooResults;
          }
      } catch(IOException ioe) {
         LOGGER.info("Caught IOException attempting Yahoo ticker validity check {}", ticker);
         LOGGER.debug("Caught (IOException ioe)", ioe);
       }
       throw new IOException();
    }
 
    double lastTick() {
       LOGGER.debug("Entering Stock.lastTick()");
       try {
          String[] tickString = askYahoo(ticker, "sl1d1t1");
          if (ticker.equals(tickString[0])) {
             price = Double.parseDouble(tickString[1]);
             lastPriceUpdateEpoch = jodaLastTickToEpoch(tickString[2] + " " + tickString[3]);
             return price;
          }
       } catch (IOException ioe) {
          LOGGER.warn("IOException generated trying to get lastTick for {}", ticker);
          LOGGER.debug("Caught (IOException ioe)", ioe);
       }
       return -1;
    }
 
    public static double lastTick(String ticker) throws IOException {
       LOGGER.debug("Entering Stock.lastTick(String {})", ticker);
       String[] tickString = askYahoo(ticker, "sl1d1t1");
       if (ticker.equals(tickString[0])) {
          return Double.parseDouble(tickString[1]);
       }
       return -1;
    }
 
    double lastBid() throws IOException {
       String[] tickString = askYahoo(ticker, "sb2d1t1");
       if (ticker.equals(tickString[0])) {
          price = Double.parseDouble(tickString[1]);
          lastPriceUpdateEpoch = jodaLastTickToEpoch(tickString[2] + " " + tickString[3]);
          return price;
       }
       return -1;
    }
 
    double lastAsk() throws IOException {
       String[] tickString = askYahoo(ticker, "sb3d1t1");
       if (ticker.equals(tickString[0])) {
          price = Double.parseDouble(tickString[1]);
          lastPriceUpdateEpoch = jodaLastTickToEpoch(tickString[2] + " " + tickString[3]);
          return price;
       }
       return -1;
    }
 
    public String getTicker() {
       return ticker;
    }
 
    void setTicker(String ticker) {
       this.ticker = ticker;
    }
 
    public double getPrice() {
       return price;
    }
 
    public double getBollingerBand(int index) {
       return bollingerBand[index];
    }
 
    public double getFiftyDma() {
       return fiftyDma;
    }
 
    public double getTwoHundredDma() {
       return twoHundredDma;
    }
 
    public String getSecType() {
       return "STOCK";
    }
 
    public long getLastPriceUpdateEpoch() {
       return lastPriceUpdateEpoch;
    }
    public boolean tickerValidDb() {
       LOGGER.debug("Entering Stock.tickerValidDb()");
       return DBHistoricalPrices.tickerPriceInDb(ticker);
    }
    public boolean tickerValidYahoo() {
       LOGGER.debug("Entering Stock.tickerValidYahoo()");
       try {
          return askYahoo(ticker, "e1")[0].equals("N/A");
       } catch(IOException ioe) {
          LOGGER.info("Could not connect to Yahoo! to verify ticker {} validity", ticker);
          LOGGER.debug("Caught exception ", ioe);
       }
       return false;
    }
    public boolean tickerValid() {
       LOGGER.debug("Entering Stock.tickerValid()");
       if (tickerValid == null) {
          return(tickerValid = (tickerValidDb() || tickerValidYahoo()));
       }
       return tickerValid;
    }
 
    String describeBollingerBands() {
       return "SMA " + bollingerBand[0] +
          " Upper 1st " + bollingerBand[1] +
          " 2nd " + bollingerBand[2] +
          " Lower 1st " + bollingerBand[3] +
          " 2nd " + bollingerBand[4] +
          " 3rd " + bollingerBand[5];
    }
 
    @Override
    public String toString() {
       return ticker;
    }
 }
