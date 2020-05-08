 package guibin.zhang.leecode;
 
 /**
  * Say you have an array for which the ith element is the price of a given stock on day i.
  * Design an algorithm to find the maximum profit. 
  * You may complete as many transactions as you like 
  * (ie, buy one and sell one share of the stock multiple times). 
  * 
  * However, you may not engage in multiple transactions at the same time 
  * (ie, you must sell the stock before you buy again).
  * 
  * http://discuss.leetcode.com/questions/286/best-time-to-buy-and-sell-stock-ii
  * 
  * @author Gubin Zhang <guibin.beijing@gmail.com>
  */
 public class BestTimeToBuyAndSellStockII {
     
     /**
      * 找波谷买入，找波峰卖出，最后一笔特殊处理。这种方式买卖次数最少。
      * @param prices
      * @return 
      */
     public int maxProfit(int[] prices) {
         
         int len = prices.length;
         
         if(len <= 1) {
             return 0;
         } else if(len == 2) {
             return Math.max(prices[1] - prices[0], 0);
         } 
         
         int maxProfit = 0;
         int buy = 0;
         int sell = 0;
         boolean sold = false;
        for(int i = 1; i < len - 1; i++) {
            //Case of peak of wave /\
            if(prices[i - 1] <= prices[i] && prices[i + 1] <= prices[i]) {
                sell = i;
                if(sell >= buy && !sold) {
                    maxProfit = Math.max(maxProfit, maxProfit + prices[sell] - prices[buy]);
                    sold = true;
                }
            //Case of trough of wave \/
            } else if (prices[i - 1] >= prices[i] && prices[i + 1] >= prices[i]) {
                 buy = i;
                 sold = false;
             }
         }
         //last /
         if(sell < len - 1 && prices[len - 1] > prices[buy] && !sold) {
             sell = len - 1;
             maxProfit += prices[sell] - prices[buy];
         } 
        
         return maxProfit;
     }
     
     /**
      * 因为不限制交易次数， 并且同一个价格可以卖再买，因此累加所有差价就OK
      * @param prices
      * @return 
      */
     public int maxProfit_v2(int[] prices) {
         int profit = 0;
         for (int i = 1; i < prices.length; i++) {
             int d = prices[i] - prices[i - 1];
             if (d > 0) {
                 profit += d;
             }
         }
         return profit;
     }
     
     public static void main(String[] args) {
         //                                    output  expected
         //[5,2,3,2,6,6,2,9,1,0,7,4,5,0]         24	20
         //[8,3,6,2,8,8,8,4,2,0,7,2,9,4,9]	40	28
         //[9,9,0,3,0,7,7,7,4,1,5,0,1,7]         35	21
         //[1,9,6,9,1,7,1,1,5,9,9,9]             33	25
         //[3,4,6,0,3,7,5,8,2,9,1,6,6,2]         30	25
         //[5,6,6,5,4,1,9,2,4,5]                 13	12
         BestTimeToBuyAndSellStockII bttbs = new BestTimeToBuyAndSellStockII();
         int[] prices1 = {5,2,3,2,6,6,2,9,1,0,7,4,5,0};
         int[] prices2 = {8,3,6,2,8,8,8,4,2,0,7,2,9,4,9};
         int[] prices3 = {9,9,0,3,0,7,7,7,4,1,5,0,1,7};
         int[] prices4 = {1,9,6,9,1,7,1,1,5,9,9,9};
         int[] prices5 = {3,4,6,0,3,7,5,8,2,9,1,6,6,2};
         int[] prices6 = {5,6,6,5,4,1,9,2,4,5};
         System.out.println(bttbs.maxProfit(prices1));
         System.out.println(bttbs.maxProfit(prices2));
         System.out.println(bttbs.maxProfit(prices3));
         System.out.println(bttbs.maxProfit(prices4));
         System.out.println(bttbs.maxProfit(prices5));
         System.out.println(bttbs.maxProfit(prices6));
     }
 }
