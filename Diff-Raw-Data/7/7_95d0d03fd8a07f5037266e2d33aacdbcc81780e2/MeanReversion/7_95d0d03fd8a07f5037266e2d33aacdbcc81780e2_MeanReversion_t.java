 package simulator.Strategy;
 
 
 import simulator.Order;
 import simulator.OrderBooks;
 import simulator.Reader;
 
 public class MeanReversion extends AbstractStrategy implements Strategy {
 
 	private Double mean; 
	// if there's no my bid order, assigns false. otherwise, assigns true
 	private boolean myBidOrder = false;
 	
 	public MeanReversion(OrderBooks books) {
 		super(books);
 	}
 
 	public void calculateMean() {
 		// pass the file path as a parameter to makeMeanReversion() in Factoroy
 		// and modify the filePath for previous days 
 		//this.mean = (highest + lowest) / 2;
 		
		this.mean = 67.46331; // random mean value from sample1.csv 
 	}	
 	
 	@Override
 	public Order strategise() {
 
 		Order order = null;
 		calculateMean();
 		// if the price is greater than mean, sell order
 		if (books.bidListSize() != 0 && myBidOrder == true) {
 			if (books.bestBidPrice() > this.mean ) { 
 				// && if (you have stock to sell)
 				Order bestBid = books.bestBidOrder();
 				if (books.askListSize() != 0) {
 					if (bestBid.price() < books.bestAskPrice()) {
 						// volume should depend on my volume, so need to change later
 						order = createOrder("ENTER", bestBid.price(), bestBid.volume(), null, "A");
 						myBidOrder = false;
 					} else {
 						if (books.bestAskPrice() - 0.01 > this.mean) {
 							// volume should depend on my volume, so need to change later
 							order = createOrder("ENTER", books.bestAskPrice() - 0.01, bestBid.volume(), null, "A");
 							myBidOrder = false;
 						}
 						// if bestAskPrice - 0.01 is lower than mean, don't sell anything
 						// because you'll sell the stock with lower price than mean
 					}
 				} else {
 					order = createOrder("ENTER", bestBid.price(), bestBid.volume(), null, "A");
 					myBidOrder = false;
 				}
 			}
 		}
 		
 		// if the price is lower than mean, buy order
		if (books.askListSize() != 0 && myBidOrder == false && order == null) {			
 			Order bestAsk = books.bestAskOrder();
 			if (bestAsk.price() < this.mean) { // check whether the price is lower than mean
 				if (books.bidListSize() != 0) {
 					if (books.bestBidPrice() < bestAsk.price()) {
 						order = createOrder ("ENTER", bestAsk.price(), bestAsk.volume(), null, "B");
 						myBidOrder = true;
 					} else {
 						if (books.bestBidPrice() + 0.01 <= this.mean) {
 							order = createOrder ("ENTER", books.bestBidPrice() + 0.01, bestAsk.volume(), null, "B");
 							myBidOrder = true;
 						}
 						// if bestBidPrice + 0.01 is greater than mean, don't buy anything
 						// because you'll buy the stock with higher price than mean
 					}
 				} else {
 					order = createOrder ("ENTER", bestAsk.price(), bestAsk.volume(), null, "B");
 					myBidOrder = true;
 				}
 			}
 		}
 		
 		return order;
 	}
 
 	@Override
 	public String getStrategyName() {
 		return "MeanReversion";
 	}
 
 }
