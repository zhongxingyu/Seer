 package controllers;
 
 import model.Bid;
 import model.Load;
 import model.User;
 import play.libs.Json;
 import play.mvc.Controller;
 import play.mvc.Result;
 
 import java.util.ArrayList;
 import java.util.List;
 
 /**
  * Created with IntelliJ IDEA.
  * User: ericwood
  * Date: 8/24/13
  * Time: 7:10 AM
  * To change this template use File | Settings | File Templates.
  */
 public class BidController extends Controller {
 
     public static Result getBids(String userId) {
         List<Bid> bids = new ArrayList<Bid>();
         Load load = new Load();
         load.setLoadDescription("a really big load");
         load.setCustomerId("customer@customer.com");
 
         Bid bid = new Bid();
         User biddingUser = new User();
         biddingUser.setUserId("bidder1@bid.com");
         biddingUser.setHaulerDisplayName("Bidder 1");
        bid.setBiddingUser(biddingUser);
         bid.setPrice(25.00);
         bid.setLoad(load);
         bids.add(bid);
 
         bid = new Bid();
         biddingUser = new User();
         biddingUser.setUserId("bidder2@bid.com");
         biddingUser.setHaulerDisplayName("Bidder 2");
        bid.setBiddingUser(biddingUser);
         bid.setPrice(15.00);
         bid.setLoad(load);
         bids.add(bid);
 
         bid = new Bid();
         biddingUser = new User();
         biddingUser.setUserId("bidder3@bid.com");
         biddingUser.setHaulerDisplayName("Bidder 3");
        bid.setBiddingUser(biddingUser);
         bid.setPrice(18.00);
         bid.setLoad(load);
         bids.add(bid);
 
         return ok(Json.toJson(bids));
     }
 
     public static Result createBid() {
         Bid newBid = Json.fromJson(request().body().asJson(),Bid.class);
         newBid.save();
         return ok();
     }
 
 }
