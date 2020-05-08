 package ru.ifmo.ctddev.larionov.bach.checker;
 
 import org.apache.log4j.Logger;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Service;
 import ru.ifmo.ctddev.larionov.bach.checker.linkstrategy.ILinkStrategy;
 import ru.ifmo.ctddev.larionov.bach.checker.text.checker.ITextChecker;
 import ru.ifmo.ctddev.larionov.bach.checker.text.downloader.IDownloader;
 import ru.ifmo.ctddev.larionov.bach.common.Pair;
 import ru.ifmo.ctddev.larionov.bach.common.site.WeightedPair;
 
 import java.net.URL;
 import java.util.List;
 
 /**
  * User: Oleg Larionov
  * Date: 12.05.13
  * Time: 16:43
  */
 @Service("pageChecker")
 public class PageChecker implements IPageChecker {
 
     private static final Logger logger = Logger.getLogger(PageChecker.class);
     private static final int DEFAULT_LINKS_COUNT = 40;
     private ILinkStrategy linkStrategy;
     private ITextChecker textChecker;
     private IDownloader textDownloader;
 
     @Autowired
     public void setLinkStrategy(ILinkStrategy linkStrategy) {
         this.linkStrategy = linkStrategy;
     }
 
     @Autowired
     public void setTextChecker(ITextChecker textChecker) {
         this.textChecker = textChecker;
     }
 
     @Autowired
     public void setTextDownloader(IDownloader textDownloader) {
         this.textDownloader = textDownloader;
     }
 
     @Override
     public double checkPair(WeightedPair pair) {
         List<Pair<URL, URL>> linksList = linkStrategy.createLinks(pair, DEFAULT_LINKS_COUNT);
 
         double result = 0;
         int validPairs = 0, badPairs = 0;
         for (Pair<URL, URL> links : linksList) {
             String text1 = textDownloader.download(links.getFirst());
             String text2 = textDownloader.download(links.getSecond());
             if (text1 != null && text2 != null) {
                 logger.debug("Valid pair: " + links);
                 result += textChecker.checkText(text1, text2);
                 validPairs++;
             } else if (text1 != null || text2 != null) {
                 logger.debug("Bad pair: " + links);
             }
         }
 
         // TODO may be should think about more complex strategy
         if (validPairs == 0 || (validPairs > 0 && badPairs > linksList.size() * 3 / 4)) {
             result = 0;
         } else {
             result /= validPairs;
         }
 
        logger.debug("Resemblance value for " + pair + " = " + result);
         return result;
     }
 }
