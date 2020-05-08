 package com.lexin.extractor.qiushibaike;
 
 import java.io.IOException;
 import java.sql.SQLException;
 import java.util.ArrayList;
 import java.util.Hashtable;
 import java.util.List;
 import java.util.Properties;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import spider.extrator.regex.RegexUtil;
 import spider.util.SpiderUtil;
 import util.SpiderConfiguration;
 import util.Util;
 
 import com.lexin.bean.Feed;
 import com.lexin.bean.Regular;
 import com.lexin.bean.Seed;
 import com.lexin.bean.SiteProperties;
 import com.lexin.db.DBUtil;
 import com.lexin.server.HttpServer;
 
 public class CommonExtractorByDB {
   private Properties dbProp;
 
   private Hashtable<String, List<Pattern>> nextPatterns;
   private Hashtable<String, List<Pattern>> contentPatterns;
   private Hashtable<String, String> encodings;
 
   private static final int REGULER_TYPE_CONTENT = 1;
   private static final int REGULER_TYPE_NEXTPAGE = 0;
   private static final int VISIT_SITE_INTERVAL = 1000;
   private static final int MAX_VISIT_LINKS = 500;
   private static final int SETUP_EXTRACTOR_PATTERN_TIME_INTERVAL = 60000;
   private Hashtable<String, Integer> counters = null;
 
   private PatternOperator patternOperator;
   private DBUtil dbUtil;
 
   public CommonExtractorByDB() throws IOException, SQLException {
     counters = new Hashtable<String, Integer>();
     patternOperator = new PatternOperator();
     dbProp = Util.getProperties(SpiderConfiguration.getDBConfigFilePath());
     dbUtil = new DBUtil(dbProp);
 
     com.lexin.server.HttpServer hs = new HttpServer();
     hs.start();
   }
 
   public void counter(String host) {
     Integer counter = counters.get(host);
     if (null == counter || counter == 0) {
       counters.put(host, 1);
     } else {
       counters.put(host, counter + 1);
     }
   }
 
   public void setup() throws IOException, SQLException {
     nextPatterns = new Hashtable<String, List<Pattern>>();
     contentPatterns = new Hashtable<String, List<Pattern>>();
     encodings = new Hashtable<String, String>();
 
     List<Regular> cRegulars = dbUtil.getCrawlerRegulars(REGULER_TYPE_CONTENT);
     List<Regular> nRegulars = dbUtil.getCrawlerRegulars(REGULER_TYPE_NEXTPAGE);
     List<SiteProperties> siteProps = dbUtil.getSiteProperties();
     if (null != cRegulars) {
       for (Regular regular : cRegulars) {
         String key = regular.getHost();
         String value = regular.getRegular();
         if (null != contentPatterns.get(key)) {
           contentPatterns.get(key).add(Pattern.compile(value));
         } else {
           List<Pattern> patterns = new ArrayList<Pattern>();
           patterns.add(Pattern.compile(value));
           contentPatterns.put(key, patterns);
         }
       }
     }
     if (null != nRegulars) {
       for (Regular regular : nRegulars) {
         String key = regular.getHost();
         String value = regular.getRegular();
         if (null != nextPatterns.get(key)) {
           nextPatterns.get(key).add(Pattern.compile(value));
         } else {
           List<Pattern> patterns = new ArrayList<Pattern>();
           patterns.add(Pattern.compile(value));
           nextPatterns.put(key, patterns);
         }
       }
     }
     if (null != siteProps) {
       for (SiteProperties sp : siteProps) {
         encodings.put(sp.getHost(), sp.getEncoding());
       }
     }
 
   }
 
   public void startExtract() throws Exception {
     List<Seed> crawlingSeeds = null;
     long preLoadTimeSpam = System.currentTimeMillis() - SETUP_EXTRACTOR_PATTERN_TIME_INTERVAL;
     while (true) {
       long curLoadTimeSpam = System.currentTimeMillis();
       if (curLoadTimeSpam - preLoadTimeSpam >= SETUP_EXTRACTOR_PATTERN_TIME_INTERVAL) {
         System.out.println("[info] reload site regex expression comfigurations.");
         preLoadTimeSpam = curLoadTimeSpam;
         setup();
       }
       crawlingSeeds = dbUtil.getCrawlingSeeds(VISIT_SITE_INTERVAL);
       if (null == crawlingSeeds || crawlingSeeds.isEmpty()) {
         Thread.sleep(10000);
        System.out.println("[info] waiting 10 seconds for seeds....");
         continue;
       }
 
       List<Feed> results = new ArrayList<Feed>();
       List<Seed> nextpages = new ArrayList<Seed>();
       for (Seed seed : crawlingSeeds) {
         String url = seed.getUrl();
         System.out.println("[INFO] crawling " + url);
         final String host = SpiderUtil.getHost(url);
         if (null != counters.get(host) && counters.get(host) > MAX_VISIT_LINKS) {
           System.out.println("[INFO] uptomax passed " + url);
           continue;
         }
         String encoding = (null == encodings.get(host) || "".equals(encodings.get(host))) ? "utf-8" : encodings
             .get(host);
         String text = Util.downLoadHTML(url, encoding);
         if (null == text || "".equals(text))
           continue;
         List<Pattern> cps = contentPatterns.get(host);
         if (cps == null) {
          System.out.println("[INFO] Error Configuration: " + host);
           break;
         }
         Matcher matcher = null;
         String patternStr = null;
         for (Pattern cp : cps) {
           matcher = cp.matcher(text);
           if (matcher != null && matcher.find()) {
             matcher.reset();
             patternStr = cp.pattern();
             break;
           }
         }
         // Operate Content
         List<Feed> result = null;
         if (null != matcher && matcher.find()) {
           matcher.reset();
           List<String> namedGroups = RegexUtil.regexPatternNamedGroups(patternStr);
           result = patternOperator.exeObjectExtractor(url, matcher, namedGroups);
         }
         if (null != result) {
           results.addAll(result);
           SpiderUtil.printResult(result);
         } else {
           System.out.println("[INFO] " + url + " is not matched!");
         }
         // Operate Next Page
         matcher = null;
         List<Pattern> nps = nextPatterns.get(host);
         for (Pattern np : nps) {
           matcher = np.matcher(text);
           if (matcher != null && matcher.find()) {
             matcher.reset();
             final Matcher m = matcher;
             final String crawlingUrl = url;
             List<Seed> nextpage = patternOperator.exeExtractor(crawlingUrl, m, new GroupHandler<Seed>() {
               @Override
               public List<Seed> handle() {
                 List<Seed> results = new ArrayList<Seed>();
                 while (m.find()) {
                   String url = SpiderUtil.getAbsoluteUrl(crawlingUrl, m.group(1));
                   if (url.contains(host)) {
                     Seed seed = new Seed();
                     seed.setCrawledAt(Util.getCurrentDate());
                     seed.setCreatedAt(Util.getCurrentDateTime());
                     seed.setUrl(url);
                     results.add(seed);
                   }
                 }
                 return results;
               }
             });
             if (null != nextpage) {
               nextpages.addAll(nextpage);
             }
           }
         }
         Thread.sleep(VISIT_SITE_INTERVAL);
         counter(host);
       }
       try {
         dbUtil.saveFeeds(results);// save results
         dbUtil.saveSeeds(nextpages);// save nextpages
         dbUtil.updateCrawledTimes(crawlingSeeds);// update crawling status
       } catch (SQLException e) {
         e.printStackTrace();
       }
     }
   }
 
   public static void main(String[] args) {
     try {
       CommonExtractorByDB nce = new CommonExtractorByDB();
       nce.startExtract();
     } catch (Exception e) {
       e.printStackTrace();
     }
   }
 }
