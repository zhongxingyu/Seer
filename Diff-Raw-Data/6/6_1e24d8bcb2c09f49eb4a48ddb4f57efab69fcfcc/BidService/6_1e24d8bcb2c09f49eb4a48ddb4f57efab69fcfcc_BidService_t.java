 package edu.unsw.triangle.service;
 
 import java.sql.SQLException;
 import java.util.logging.Logger;
 
 import edu.unsw.triangle.data.ConnectionManager;
 import edu.unsw.triangle.data.DataSourceException;
 import edu.unsw.triangle.data.DerbyDaoManager;
 import edu.unsw.triangle.model.Bid;
 import edu.unsw.triangle.model.Item;
 import edu.unsw.triangle.model.Profile;
 
 public class BidService 
 {
 	private final static Logger logger = Logger.getLogger(BidService.class.getName());
 	
 	public static void updateItemBid(Bid bid) throws SQLException, DataSourceException
 	{
 		DerbyDaoManager daoManager = null;
 		try
 		{
 			daoManager = new DerbyDaoManager(ConnectionManager.getInstance());
 			daoManager.getItemDao().updateItemBid(bid);
 		}
 		finally
 		{
 			if(daoManager != null)
 				daoManager.close();
 		}
 	}
 	
 	public static void updateItemBidAndNotify(Bid bid, Item item) throws SQLException, DataSourceException
 	{
 		DerbyDaoManager daoManager = null;
 		try
 		{
 			daoManager = new DerbyDaoManager(ConnectionManager.getInstance());
 			daoManager.getItemDao().updateItemBid(bid);
 			// Notify bidder success
 			Profile bidder = daoManager.getProfileDao().findByUsername(bid.getBidder());
 			NotificationService.notifyItemBidderSuccess(bidder.getEmail(), item, bid);
 			logger.info("notify bidder of bid: " + bid.getBidder());
			
			// Notify previous bidder of new current bid if not the same bidder
			if (item.getBidder() != null && !bid.getBidder().equals(item.getBidder()))
 			{
 				Profile previousBidder = daoManager.getProfileDao().findByUsername(item.getBidder());
 				NotificationService.notifyItemBidderLoss(previousBidder.getEmail(), item, bid);
 				logger.info("notify previous bidder: " + previousBidder.getUsername());
 			}
 		}
 		finally
 		{
 			if(daoManager != null)
 				daoManager.close();
 		}
 	}
 
 }
