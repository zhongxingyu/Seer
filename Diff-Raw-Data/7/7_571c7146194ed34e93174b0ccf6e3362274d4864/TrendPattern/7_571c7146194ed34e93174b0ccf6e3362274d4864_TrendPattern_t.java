 import java.util.HashMap;
 import java.util.List;
 
 /**
  * User: mark.mcdonald
  * Date: 4/18/13
  */
 public class TrendPattern extends TradingPattern {
 
     private Market market;
     private int numberRoundsToTrack;
 
 
     public TrendPattern(Market market, int numberRoundsToTrack) {
         this.market = market;
         this.numberRoundsToTrack = numberRoundsToTrack;
     }
 
     @Override
     /**
      * Traders with this pattern will look at the trend of market prices and buy or sell at the last market price
      * accordingly. Will not place bids until as many rounds as he is tracking have occurred.
      */
     public void placeBids() {
         // We'll calculate the expected price for each commodity
         HashMap<Commodity, Double> trends = new HashMap<Commodity, Double>();
 
         double totalTrend = 0;
 
         List<RoundData> marketRoundData = market.getRoundDataList();
         int numberRoundsPassed = marketRoundData.size();
         if (numberRoundsToTrack < numberRoundsPassed) {
             for (Commodity commodity : market.commodities) {
                 double lastMarketPrice = commodity.marketprice;
                 double trend = 0;
 
                 for (int i = numberRoundsToTrack; i > 0; i--){
                     double marketPriceForIthRound = marketRoundData.get(numberRoundsPassed - i).getMarketPrices().get(commodity);
                     double marketPriceForRoundBeforeIthRound = marketRoundData.get(numberRoundsPassed - (i + 1)).getMarketPrices().get(commodity);
                     trend += marketPriceForIthRound - marketPriceForRoundBeforeIthRound; // Change = final - initial
                 }
                 trends.put(commodity, trend / numberRoundsToTrack);
                 if (trend > 0 ) {
                     totalTrend += trend;
                 } else {
                     Bid sellBid = new Bid(agent, BidType.SELL, commodity, agent.inventory.get(commodity), lastMarketPrice);
                     market.acceptBid(sellBid);
     				System.out.println(agent.name + " wants to sell " + sellBid.quantity + " units of " + commodity.name + " for " + lastMarketPrice +  " galactic intracredits each.");
 
                 }
 
             }
             for (Commodity commodity : market.commodities) {
                 double trend = trends.get(commodity);
                if (trend > 0) {

                double lastMarketPrice = commodity.marketprice;
                 double trendPercent = trend / totalTrend;
                 double spendingCap = Math.floor(agent.budget * trendPercent);
                 int quantityToBUy = (int) Math.floor(spendingCap / lastMarketPrice);
                 Bid bid = new Bid(agent, BidType.BUY, commodity, quantityToBUy, lastMarketPrice);
                 market.acceptBid(bid);
 				System.out.println(bid.agent.name + " wants to buy " + bid.quantity + " units of " + commodity.name + ", and is willing to spend " + lastMarketPrice + " galactic intracredits.");
                }
             }
         }
     }
 }
