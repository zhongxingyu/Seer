 package matchrecommend;
 
 import HandicapProcessing.HandicapProcessing;
 import Util.Props;
 import com.mongodb.DBObject;
 
 import java.util.List;
 
 /**
  * Created with IntelliJ IDEA.
  * User: snowhyzhang
  * Date: 13-1-29
  * Time: 下午5:17
  * To change this template use File | Settings | File Templates.
  */
 public class MatchRecommendRTGuaranteeProcessor extends MatchRecommendProcessor{
 
     public static void main(String args[]){
         MatchRecommendRTGuaranteeProcessor rtRecommendProcessor = new MatchRecommendRTGuaranteeProcessor();
 
         double minExpectation = Double.parseDouble(Props.getProperty("minExpectation"));
         double minProbability = Double.parseDouble(Props.getProperty("minProbability"));
 
         rtRecommendProcessor.recommendMatches(minExpectation, minProbability);
     }
 
     @Override
     public void recommendMatches(double minExpectation, double minProbability) {
         MatchRecommendGuarantee mrg = new MatchRecommendGuarantee();
         HandicapProcessing hp = new HandicapProcessing();
         List<DBObject> allMatches = MatchRecommendProcessor.getRecommendMatches();
 
         int matchCount = allMatches.size();
         int currentMatch = 0;
         int recommendMatch = 0;
         for (DBObject match: allMatches){
             System.out.println("Processing: " + matchCount + "/" + currentMatch + "\tRecommend: " + recommendMatch);
             ++currentMatch;
             double win = ((Number) match.get("w1")).doubleValue();
             double push = ((Number) match.get("p1")).doubleValue();
             double lose = ((Number) match.get("l1")).doubleValue();

            double h1 = ((Number) match.get("h1")).doubleValue();
            double h2 = ((Number) match.get("h2")).doubleValue();
            int abFlag = ((Number) match.get("abFlag")).intValue();
            double winRate = (abFlag == 1) ? h1 : h2;
            double loseRate = (abFlag == 1) ? h2 : h1;
 
             String matchId = (String) match.get("matchId");
             int ch = ((Number) match.get("ch")).intValue();
             String cid = (String) match.get("cid");
 
             String teamA = match.get("tNameA").toString();
             String teamB = match.get("tNameB").toString();
 
             double handicap = ch / 4.0;
 
             if (handicap >= 3 || handicap <= -3) {
                 System.out.println("The handicap is out of range: " + handicap);
                 continue;
             }
             hp.setMatch(win, push, lose, handicap, winRate, loseRate, matchId, "snow", cid, teamA, teamB, ch);
 
             int isBet = hp.getResult(false);
             if (isBet != 0) {
                 continue;
             }
             isBet = mrg.getRecommendMatch(minProbability, minExpectation, hp);
             if (isBet == 0)
                 ++recommendMatch;
         }
     }
 }
