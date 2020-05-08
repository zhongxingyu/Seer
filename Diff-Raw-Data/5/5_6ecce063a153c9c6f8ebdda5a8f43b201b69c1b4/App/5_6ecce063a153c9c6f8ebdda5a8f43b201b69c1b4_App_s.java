 package StockAnalysis;
 
 import hibernate.HibernateUtil;
 import yahooscraper.LoadFromYahoo;
 import org.apache.log4j.ConsoleAppender;
 import org.apache.log4j.FileAppender;
 import org.apache.log4j.Level;
 import org.apache.log4j.Logger;
 import org.apache.log4j.PatternLayout;
 import org.hibernate.Session;
 
 /**
  *
  * @author gtri
  */
 public class App 
 {
 
     /**
      * @param args the command line arguments
      */
     public static void main(String[] args) 
     {
         Logger rootLogger = Logger.getRootLogger();
        if (!rootLogger.getAllAppenders().hasMoreElements()) 
         {
                 rootLogger.setLevel(Level.INFO);
                 try
                 {
                         rootLogger.addAppender(new FileAppender(
                                  new PatternLayout("%-5p [%t]: %m%n"), "stocks.log"));
                 }
                 catch(Exception e){}
                 // The TTCC_CONVERSION_PATTERN contains more info than
                 // the pattern we used for the root logger
                 Logger pkgLogger = rootLogger.getLoggerRepository().getLogger("robertmaldon.moneymachine");
                 pkgLogger.setLevel(Level.DEBUG);
                 pkgLogger.addAppender(new ConsoleAppender(
                          new PatternLayout(PatternLayout.TTCC_CONVERSION_PATTERN)));
        }
 
         LoadFromYahoo.loadInAllStocksFromNASDAQFile();
         LoadFromYahoo.updateAllStocksFromYahoo();
     }
 }
