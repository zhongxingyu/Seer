 package spider.util;
 
 import java.net.URI;
 import java.util.List;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import spider.extrator.regex.RegexUtil;
 
 import com.lexin.bean.Feed;
 import com.lexin.extractor.qiushibaike.PatternOperator;
 
 public class SpiderUtil {
   public final static void printResult(List<Feed> feeds) {
     for (Feed feed : feeds) {
       System.out.println(feed.toString());
     }
   }
 
   public final static String getAbsoluteUrl(String crawlingUrl, String seed) {
     URI uri;
     try {
       uri = new URI(crawlingUrl);
       return uri.resolve(seed).toString();
     } catch (Exception e) {
       e.printStackTrace();
     }
     return null;
   }
 
   public final static String getHost(String urlStr) {
     URI uri;
     try {
       uri = new URI(urlStr);
       return uri.getHost();
     } catch (Exception e) {
       e.printStackTrace();
     }
     return null;
   }
 
   public final static String getURLPathPattern(String url) {
     if (null == url)
       return null;
     return url.replaceAll("\\d{3}", "{id}");
   }
 
   public final static List<Feed> testRegular(String seed, String text, String contentPattern) {
     Pattern pattern = Pattern.compile(contentPattern);
     Matcher matcher = pattern.matcher(text);
     PatternOperator patternOperator = new PatternOperator();
     List<String> namedGroups = RegexUtil.regexPatternNamedGroups(contentPattern);
     List<Feed> result = patternOperator.exeObjectExtractor(seed, matcher, namedGroups);
     return result;
   }
 
   public final static List<String> testNextPage(String seed, String text, String nextPattern) {
     Pattern pattern = Pattern.compile(nextPattern);
     Matcher matcher = pattern.matcher(text);
     PatternOperator patternOperator = new PatternOperator();
     List<String> result = patternOperator.exeExtractor(seed, matcher);
     return result;
   }
  public static void main(String[] args) {
    String url="http://www.haha365.com/bxww/index_1.html ";
    System.out.println(getHost(url));
  }
 }
