 package test.java;
 
 import main.java.*;
 
 import org.junit.Assert;
 import org.junit.Test;
 
 /**
  * Tests the functionality of the Auction class.
  *
  */
 public class AuctionTest {
 
 	private Auction createAuctionFor(String seller, boolean isOpen) {
         Auction auction = new Auction(seller);
         try {
             auction.setDescription("Action Figure", seller);
             auction.setQuantity(1, seller);
             if (isOpen) {
             	auction.open();
             }
         }
         catch (Exception e) {
             Assert.fail("openAuction failed when opening auction!");
         }
 
         return auction;
     }
 
 	private Auction createAuctionFor(String seller) {
 		return createAuctionFor(seller, true);
     }
 
     /**
      * Tests that the user (seller) that created the auction
      * cannot bid on that auction.
      */
     @Test
     public void canSellerBidOnOwnAuction() {
         String seller = "sellerx";
         Auction auction = new Auction(seller);
         boolean result = auction.isValidBidder(seller);
 
         Assert.assertFalse(result);
     }
 
     /**
      * Tests that a user can bid on an auction.
      */
     @Test
     public void canUserBidOnAuction() {
         String seller = "sellerx";
         String bidder = "bidderx";
         Auction auction = new Auction(seller);
         boolean result = auction.isValidBidder(bidder);
 
         Assert.assertTrue(result);
     }
 
     @Test
     public void checkAuctionUpdateRestriction() {
         Auction auction = new Auction("sellerDude");
 
         Assert.assertFalse(auction.canModify("otherDude"));
     }
 
     @Test
     public void checkAuctionUpdatePermission() {
         Auction auction = new Auction("sellerDude");
 
         Assert.assertTrue(auction.canModify("sellerDude"));
     }
 
     @Test
     public void noBidUntilAuctionOpens() {
         Auction auction = new Auction("sellerx");
         Assert.assertFalse(auction.canBid());
     }
 
     @Test
     public void canBidWhenOpen() throws AuctionNotReadyException {
         Auction auction = createAuctionFor("seller");
 
         Assert.assertTrue(auction.canBid());
     }
 
     @Test
     public void noBidOnClosedAuction() {
         Auction auction = createAuctionFor("sellerx");
         auction.close();
 
         Assert.assertFalse(auction.canBid());
     }
 
     @Test
     public void AllowBidder() throws AuctionNotReadyException {
         //Given
         Auction auction = createAuctionFor("seller");
         String bidder = "Moneybags";
         int amount = 10;
         auction.open();
 
         //When
         boolean result = auction.makeBid(bidder, amount);
 
         Assert.assertTrue(result);
     }
 
     @Test
     public void PreventBidder() {
         //Given
         String bidder = "seller";
         Auction auction = createAuctionFor(bidder);
         int amount = 10;
 
         //When
         boolean result = auction.makeBid(bidder, amount);
 
         Assert.assertFalse("Bidding on my own Auction", result);
     }
 
     @Test
     public void disallowInsufficientBidder() {
         //Given
         Auction auction = createAuctionFor("seller");
         int amount = 10;
         auction.setCurrentBidAmount(amount);
         String new_bidder = "new bidder";
 
         //When
         boolean result = auction.makeBid(new_bidder, amount);
 
         Assert.assertFalse("Bid was not greater than current", result);
     }
 
     @Test
     public void allowLargerBidder() throws AuctionNotReadyException {
         //Given
         Auction auction = createAuctionFor("seller");
         int amount = 10;
         auction.setCurrentBidAmount(amount);
         String new_bidder = "new bidder";
         auction.open();
 
         //When
         boolean result = auction.makeBid(new_bidder, amount + 1);
 
         Assert.assertTrue("Bid should have been acceptable",
                           result);
     }
 
     @Test(expected=AuctionInProgressException.class)
     public void conditionSetOnOpenAuction() throws AuctionInProgressException {
         String bidder = "seller";
         String condition = "Awesome!";
         Auction auction = createAuctionFor(bidder);
         auction.setCondition(condition, bidder);
     }
 
     @Test
     public void conditionSetOnNotOpenAuction() throws AuctionInProgressException {
         String bidder = "seller";
         String condition = "Awesome!";
         Auction auction = new Auction(bidder);
         auction.setCondition(condition, bidder);
         Assert.assertEquals(condition, auction.getCondition());
     }
 
     @Test(expected=AuctionInProgressException.class)
     public void minBidSetOnOpenAuction() throws AuctionInProgressException {
         String bidder = "seller";
         Auction auction = createAuctionFor(bidder);
         auction.setMinBid(100, bidder);
     }
 
     @Test
     public void bidNotMeetMinBid() throws AuctionInProgressException, AuctionNotReadyException {
     	String auctionOwner = "owner";
     	String bidder = "bidder";
     	Auction auction = createAuctionFor(auctionOwner, false);
     	auction.setMinBid(2, auctionOwner);
     	auction.open();
     	Assert.assertFalse(auction.makeBid(bidder, 1));
     }
 
     @Test
     public void bidMeetsMinBid() throws AuctionInProgressException, AuctionNotReadyException {
     	String auctionOwner = "owner";
     	String bidder = "bidder";
     	Auction auction = createAuctionFor(auctionOwner, false);
     	auction.setMinBid(2, auctionOwner);
     	auction.open();
     	Assert.assertTrue(auction.makeBid(bidder, 2));
     }
 
     @Test
     public void minBidSetOnNotOpenAuction() throws AuctionInProgressException {
         String bidder = "seller";
         int bid = 100;
         Auction auction = new Auction(bidder);
         auction.setMinBid(bid, bidder);
 
         Assert.assertEquals(bid, auction.getMinBid());
     }
 
     @Test(expected=AuctionInProgressException.class)
     public void descriptionSetOnOpenAuction() throws AuctionInProgressException {
         String seller = "joeuser";
         Auction auction = createAuctionFor(seller);
         auction.setDescription("My description", seller);
     }
 
     @Test
     public void descriptionSetOnNotOpenAuction() throws AuctionInProgressException {
         String bidder = "joeuser";
         String descr = "My description";
         Auction auction = createAuctionFor(bidder, false);
         auction.setDescription(descr, bidder);
 
         Assert.assertEquals(descr, auction.getDescription());
     }
 
     @Test
     public void descriptionSetOnNotOpenAuctionWrongUser() throws AuctionInProgressException {
         String bidder = "joeuser";
         String descr = "My description";
         Auction auction = createAuctionFor(bidder, false);
         auction.setDescription(descr, "otheruser");
 
        Assert.assertNotEquals(descr, auction.getDescription());
     }
 
     @Test(expected=AuctionInProgressException.class)
     public void quantitySetOnOpenAuction() throws AuctionInProgressException {
         String bidder = "joeuser";
         Auction auction = createAuctionFor(bidder);
         auction.setQuantity(100, bidder);
     }
 
     @Test
     public void quantitySetOnNotOpenAuction() throws AuctionInProgressException {
         String bidder = "joeuser";
         Auction auction = createAuctionFor(bidder, false);
         auction.setQuantity(100, bidder);
         Assert.assertEquals(100, auction.getQuantity());
     }
 
     @Test
     public void quantitySetOnNotOpenAuctionWrongUser() throws AuctionInProgressException {
         String bidder = "joeuser";
         Auction auction = createAuctionFor(bidder, false);
         auction.setQuantity(100, "otheruser");
        Assert.assertNotEquals(100, auction.getQuantity());
     }
 
     @Test
     public void cantOpenUnlessMinimumFields() {
         Auction auction = new Auction("seller");
 
         boolean exceptionCaught = false;
         try {
             auction.open();
         }
         catch (AuctionNotReadyException e) {
             exceptionCaught = true;
         }
 
         Assert.assertTrue(exceptionCaught);
     }
 
     @Test
     public void bidderNotifiedOfWin() throws AuctionNotReadyException, AuctionInProgressException {
 
     	String seller = "seller";
     	String bidder = "bidder";
     	Auction auction = createAuctionFor(seller);
 
     	auction.makeBid(bidder, auction.getCurrentBidAmount()+1);
     	auction.close();
 
     	Assert.assertEquals(bidder, auction.getWinner());
 
     }
 
     @Test
     public void sellerCanSetBuyItNow() throws AuctionNotReadyException, AuctionInProgressException {
         Auction auction = new Auction("seller");
         int buyItNowAmount = 20;
         auction.setBuyItNowAmount(buyItNowAmount, "seller");
         Assert.assertEquals(buyItNowAmount, auction.getBuyItNowAmount());
     }
 
     @Test(expected=AuctionInProgressException.class)
     public void sellerCannotSetBuyItNowOnOpenAuction() throws AuctionNotReadyException, AuctionInProgressException {
         Auction auction = createAuctionFor("seller");
         int buyItNowAmount = 20;
         auction.setBuyItNowAmount(buyItNowAmount, "seller");
     }
 
     @Test(expected=AuctionInProgressException.class)
     public void preventSetReservePriceOnOpenedAuction() throws AuctionNotReadyException, AuctionInProgressException
     {
     	String seller = "seller";
     	String bidder = "bidder";
     	Auction auction = createAuctionFor(seller, true);
         auction.setReservePrice(50, seller);
     	auction.open();
     	auction.makeBid(bidder, 1);
     	auction.close();
     	Assert.assertNull("The auction should not have a winner, as the reserve price was not met.", auction.getWinner());
     }
     
     @Test
     public void auctionHasNotMetReservePriceOnClose() throws AuctionNotReadyException, AuctionInProgressException
     {
     	String seller = "seller";
     	String bidder = "bidder";
     	Auction auction = createAuctionFor(seller, false);
         auction.setReservePrice(50, seller);
     	auction.open();
     	auction.makeBid(bidder, 1);
     	auction.close();
     	Assert.assertNull("The auction should not have a winner, as the reserve price was not met.", auction.getWinner());
     }
 
     @Test
     public void auctionHasMetReservePriceOnClose() throws AuctionNotReadyException, AuctionInProgressException
     {
     	String seller = "seller";
     	String bidder = "bidder";
     	Auction auction = createAuctionFor(seller, false);
         auction.setReservePrice(50, seller);
     	auction.open();
     	auction.makeBid(bidder, 50);
     	auction.close();
     	Assert.assertNotNull("The auction should have a winner, as the reserve price was met.", auction.getWinner());
     }
     
     @Test
     public void bidderCanBuyItNowOnOpenAuction() throws AuctionNotReadyException, AuctionInProgressException {
         Auction auction = new Auction("seller");
         int buyItNowAmount = 20;
         auction.setDescription("Action Figure", "seller");
         auction.setQuantity(1, "seller");
         auction.setBuyItNowAmount(buyItNowAmount, "seller");
         auction.open();
 
         String buyer = "buyer";
         auction.buyItNow(buyer);
 
         Assert.assertFalse(auction.isOpen());
         Assert.assertEquals(buyer, auction.getWinner());
     }
 
     @Test
     public void auctionHasExceededReservePriceOnClose() throws AuctionNotReadyException, AuctionInProgressException
     {
     	String seller = "seller";
     	String bidder = "bidder";
     	Auction auction = createAuctionFor(seller, false);
     	auction.setReservePrice(50, seller);
     	auction.open();
     	auction.makeBid(bidder, 51);
     	auction.close();
     	Assert.assertNotNull("The auction should have a winner, as the reserve price was met.", auction.getWinner());
     }
     
 //    @Test
 //    public void testAutoBid() {
 //    	
 //    	String seller = "seller";
 //    	String bidder = "bidder";	
 //    	String bidder2 = "bidder";	
 //    	Auction auction = createAuctionFor(seller);
 //    	int bidAmount;
 //    	int newBidAmount;
 //    	boolean autoBid = true;
 //    	int maxBid;
 //    	
 //    	bidAmount = auction.getCurrentBidAmount() + 1;
 //    	newBidAmount = bidAmount + 1;
 //    	maxBid = bidAmount * 10;
 //    	
 //    	Bid autoBid = new Bid(bidder, bidAmount, autoBid, maxBid);
 //    	Bid newBid = new Bid(bidder2, bidAmount + 1, false);
 //    	Bid currentBid;
 //    	
 //    	auction.makeBid(autoBid);
 //    	auction.makeBid(newBid);
 //    	currentBid = auction.getCurrentBid();
 //    	
 //    }
 }
