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
             auction.setDescription("Action Figure");
             auction.setQuantity(1);
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
         auction.setCondition(condition);
     }
 
     @Test
     public void conditionSetOnNotOpenAuction() throws AuctionInProgressException {
         String bidder = "seller";
         String condition = "Awesome!";
         Auction auction = new Auction(bidder);
         auction.setCondition(condition);
         Assert.assertEquals(condition, auction.getCondition());
     }
 
     @Test(expected=AuctionInProgressException.class)
     public void minBidSetOnOpenAuction() throws AuctionInProgressException {
         String bidder = "seller";
         Auction auction = createAuctionFor(bidder);
         auction.setMinBid(100);
     }
     
     @Test
     public void bidNotMeetMinBid() throws AuctionInProgressException, AuctionNotReadyException {
     	String auctionOwner = "owner";
     	String bidder = "bidder";
     	Auction auction = createAuctionFor(auctionOwner, false);
     	auction.setMinBid(2);
     	auction.open();
     	Assert.assertFalse(auction.makeBid(bidder, 1));
     }    
     
     @Test
     public void bidMeetsMinBid() throws AuctionInProgressException, AuctionNotReadyException {
     	String auctionOwner = "owner";
     	String bidder = "bidder";
     	Auction auction = createAuctionFor(auctionOwner, false);
     	auction.setMinBid(2);
     	auction.open();
     	Assert.assertTrue(auction.makeBid(bidder, 2));
     }
 
     @Test
     public void minBidSetOnNotOpenAuction() throws AuctionInProgressException {
         String bidder = "seller";
         int bid = 100;
         Auction auction = new Auction(bidder);
         auction.setMinBid(bid);
 
         Assert.assertEquals(bid, auction.getMinBid());
     }
 
     @Test(expected=AuctionInProgressException.class)
     public void descriptionSetOnOpenAuction() throws AuctionInProgressException {
         String bidder = "seller";
         Auction auction = createAuctionFor(bidder);
         auction.setDescription("My description");
     }
 
     @Test
     public void descriptionSetOnNotOpenAuction() throws AuctionInProgressException {
         String bidder = "seller";
         String descr = "My description";
         Auction auction = createAuctionFor(bidder, false);
         auction.setDescription(descr);
 
         Assert.assertEquals(descr, auction.getDescription());
     }
 
     @Test(expected=AuctionInProgressException.class)
     public void quantitySetOnOpenAuction() throws AuctionInProgressException {
         String bidder = "seller";
         Auction auction = createAuctionFor(bidder);
         auction.setQuantity(100);
     }
 
     @Test
     public void quantitySetOnNotOpenAuction() throws AuctionInProgressException {
         String bidder = "seller";
         Auction auction = createAuctionFor(bidder, false);
         auction.setQuantity(100);
         Assert.assertEquals(100, auction.getQuantity());
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
     	String winner;
     	Auction auction = createAuctionFor(seller);
     	
     	auction.makeBid(bidder, auction.getCurrentBidAmount()+1);
     	auction.close();
     	
     	Assert.assertEquals(bidder, auction.getWinner());
     	
     }
 
     @Test
     public void sellerCanSetBuyItNow() throws AuctionNotReadyException, AuctionInProgressException {
         Auction auction = new Auction("seller");
         int buyItNowAmount = 20;
         auction.setBuyItNowAmount(buyItNowAmount);
         Assert.assertEquals(buyItNowAmount, auction.getBuyItNowAmount());
     }
 
     @Test(expected=AuctionInProgressException.class)
     public void sellerCannotSetBuyItNowOnOpenAuction() throws AuctionNotReadyException, AuctionInProgressException {
        Auction auction = openAuctionFor("seller");
         int buyItNowAmount = 20;
         auction.setBuyItNowAmount(buyItNowAmount);
     }
     
     @Test
     public void auctionHasNotMetReservePriceOnClose()
     {
     	String seller = "seller";
     	String bidder = "bidder";
    	Auction auction = openAuctionFor(seller);
     	auction.setReservePrice(50);
     	auction.makeBid(bidder, 1);
     	auction.close();
     	Assert.assertNull("The auction should not have a winner, as the reserve price was not met.", auction.getWinner());
     }
     
     @Test
     public void auctionHasMetReservePriceOnClose()
     {
     	String seller = "seller";
     	String bidder = "bidder";
    	Auction auction = openAuctionFor(seller);
     	auction.setReservePrice(50);
     	auction.makeBid(bidder, 51);
     	auction.close();
     	Assert.assertNotNull("The auction should have a winner, as the reserve price was met.", auction.getWinner());
     }
 }
