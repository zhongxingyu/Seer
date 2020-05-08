 package BetMatchProcessing;
 
 import HandicapProcessing.HandicapProcessing;
 import Util.MongoDBUtil;
 import Util.Props;
 import com.mongodb.BasicDBObject;
 import com.mongodb.DBObject;
 
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 import java.util.concurrent.ExecutionException;
 import java.util.concurrent.ExecutorService;
 import java.util.concurrent.Executors;
 import java.util.concurrent.Future;
 
 /**
  * Created with IntelliJ IDEA.
  * User: snowhyzhang
  * Date: 13-1-16
  * Time: 上午12:35
  * To change this template use File | Settings | File Templates.
  */
 public class BetMatchBatchProcessor {
 
     private int processingMatch;
 
     public static void main(String args[]) {
         BetMatchBatchProcessor betMatchBatchProcessor = new BetMatchBatchProcessor();
         double minExpectation = 0.03;
         double minProbability = 0.58;
         betMatchBatchProcessor.betBatchMatchHandicapGuarantee(minExpectation, minProbability);
     }
 
     public void betBatchMatchHandicapGuarantee(final double minExpectation, final double minProbability) {
         long t1 = System.currentTimeMillis();
         int cpuNum = Runtime.getRuntime().availableProcessors();
        ExecutorService executorService = Executors.newFixedThreadPool(Integer.parseInt(Props.getProperty("thread")));
 
         final List<DBObject> matchList = getAllBettingMatch();
         processingMatch = 0;
         final int[] BetOnMatch = {0};
         List<Future> futures = new ArrayList<Future>();
         for (final DBObject match : matchList) {
             Future future = executorService.submit(new Runnable() {
                 public void run() {
                     iBetMatchProcessing bmp = new BetHandicapMatchGuarantee();
                     HandicapProcessing hp = new HandicapProcessing();
 
                     bmp.setCollection(Props.getProperty("MatchBatchBet"));
                     System.out.println("\n*_*_*_*_*_*_*_*_*_*");
                     System.out.println("Processing match: " + processingMatch);
                     ++processingMatch;
                     double win = ((Number) match.get("w1")).doubleValue();
                     double push = ((Number) match.get("p1")).doubleValue();
                     double lose = ((Number) match.get("l1")).doubleValue();
                     double winRate = ((Number) match.get("h1")).doubleValue();
                     double loseRate = ((Number) match.get("h2")).doubleValue();
 
                     String matchId = (String) match.get("matchId");
                     double ch = ((Number) match.get("ch")).doubleValue();
                     String cid = (String) match.get("cid");
                     int abFlag = ((Number) match.get("abFlag")).intValue();
                     Date matchTime = ((Date)match.get("time"));
 
                     double handicap = getHandicap(ch, abFlag);
                     if (handicap == -999) {
                         System.out.println("ERROR handicap!");
                         return;
                     }
                     if (handicap >= 3 || handicap <= -3) {
                         System.out.println("The handicap is out of range: " + handicap);
                         return;
                     }
                     hp.setMatch(win, push, lose, handicap, winRate, loseRate, matchId, "snow", cid, matchTime);
                     int isBet = hp.getResult(10000, 10, false);
                     if (isBet != 0) {
                         return;
                     }
                     isBet = bmp.betMatch(minExpectation, minProbability, 10, hp);
                     if (isBet == 0) {
                         ++BetOnMatch[0];
                     }
                 }
             });
             futures.add(future);
         }
         int index = 0;
         for (Future f : futures) {
             try {
                 f.get();
                 System.out.println("done with" + index++);
             } catch (InterruptedException e) {
                 e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
             } catch (ExecutionException e) {
                 e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
             }
         }
         long t2 = System.currentTimeMillis();
 
         System.out.println("\n****\nTotal Match: " + matchList.size() + "\nBet on match: " + BetOnMatch[0] + "\ntotal time:" + (t2 - t1));
     }
 
     private double getHandicap(double type, int abFlag) {
         double handicap = type / 4.0;
         if (abFlag == 1) {              //让球
             return handicap;
         } else if (abFlag == 2) {       //受让让球
             return handicap * -1;
         } else {
             return -999;
         }
     }
 
     private List<DBObject> getAllBettingMatch() {
         String cid = Props.getProperty("betCId");
 
         DBObject query = new BasicDBObject();
         query.put("ch", new BasicDBObject("$ne", null));
         query.put("abFlag", new BasicDBObject("$ne", null));
         query.put("cid", cid);
 
         DBObject field = new BasicDBObject();
         field.put("h1", 1);
         field.put("h2", 1);
         field.put("abFlag", 1);
         field.put("ch", 1);
         field.put("matchId", 1);
         field.put("cid", 1);
         field.put("w1", 1);
         field.put("p1", 1);
         field.put("l1", 1);
         field.put("time", 1);
 
         MongoDBUtil dbUtil = MongoDBUtil.getInstance(Props.getProperty("MongoDBRemoteHost"),
                 Props.getProperty("MongoDBRemotePort"),
                 Props.getProperty("MongoDBRemoteName"));
 
         List<DBObject> matchList = dbUtil.findAll(query, field, Props.getProperty("BettingMatch"));
 
         return matchList;
     }
 }
