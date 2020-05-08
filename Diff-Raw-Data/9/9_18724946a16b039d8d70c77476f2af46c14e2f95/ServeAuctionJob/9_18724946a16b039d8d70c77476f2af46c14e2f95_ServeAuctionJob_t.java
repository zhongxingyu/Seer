 package com.epam.lab.buyit.controller.auction.job;
 
 import org.apache.log4j.Logger;
 import org.quartz.Job;
 import org.quartz.JobExecutionContext;
 import org.quartz.JobExecutionException;
 
 import com.epam.lab.buyit.controller.email.EmailMessageBuilder;
 import com.epam.lab.buyit.controller.service.auction.AuctionServiceImp;
 import com.epam.lab.buyit.controller.service.bid.BidServiceImp;
 import com.epam.lab.buyit.controller.service.product.ProductServiceImpl;
 import com.epam.lab.buyit.controller.service.user.UserServiceImpl;
 import com.epam.lab.buyit.model.Auction;
 import com.epam.lab.buyit.model.Product;
 import com.epam.lab.buyit.model.User;
 
 public class ServeAuctionJob implements Job {
 
 	private static final Logger LOGGER = Logger
 			.getLogger(ServeAuctionJob.class);
 
 	@Override
 	public void execute(JobExecutionContext arg0) throws JobExecutionException {
 
 		AuctionServiceImp auctionService = new AuctionServiceImp();
 		ProductServiceImpl productService = new ProductServiceImpl();
 		UserServiceImpl userService = new UserServiceImpl();
 		BidServiceImp bidService = new BidServiceImp();
 		EmailMessageBuilder emailMessageBuilder = new EmailMessageBuilder();
 
 		int auctionId = (int) arg0.getTrigger().getJobDataMap()
 				.get("auctionId");
 
 		LOGGER.info("Serving auction with id = " + auctionId);
 
 		Auction auction = auctionService.getItemById(auctionId);
		LOGGER.info("Get " + auction);
		if (auction.getStatus().equalsIgnoreCase("inProgress")) {
			LOGGER.info("Closing " + auction);
 			auctionService.closeAuction(auctionId);
			LOGGER.info("Closed " + auction);
 			Product product = productService
 					.getItemById(auction.getProductId());
			LOGGER.info("GET product " + product);
 			User seller = userService.getItemById(product.getUserId());
 			User buyer = userService.getItemById(bidService
 					.getWinUserIdByAuctionId(auctionId));
 
 			if (buyer != null) {
 				emailMessageBuilder.sendWinLotForm(buyer, product, seller);
 				emailMessageBuilder.sendProductSoldOnAuctionForm(seller,
 						product, buyer);
 			} else {
 				emailMessageBuilder.sendNoBodyBuyYourProductForm(seller,
 						product);
 			}
 			LOGGER.info("Serve auction with id = " + auctionId);
 		}
 	}
 }
