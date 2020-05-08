 package org.yogocodes.bikewars.services.impl;
 
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Service;
 import org.yogocodes.bikewars.dao.UserStockDao;
 import org.yogocodes.bikewars.model.UserStockModel;
 import org.yogocodes.bikewars.services.UserStockService;
 
 @Service("userStockService")
 public class UserStockServiceImpl implements UserStockService {
 
 	private final Logger logger = LoggerFactory.getLogger(UserStockServiceImpl.class);
 
 	@Autowired
 	private UserStockDao userStockDao;
 
 	@Override
 	public List<UserStockModel> getUserStocks(final Long userId) {
 		return userStockDao.getUserStocks(userId);
 	}
 
 	@Override
 	public Map<Long, UserStockModel> getGroupedUserStocks(final Long userId) {
 
 		final Map<Long, UserStockModel> groupedUserStocks = new HashMap<>();
 
 		final List<UserStockModel> userStocks = getUserStocks(userId);
 
 		if (userStocks != null) {
 			for (final UserStockModel userStockModel : userStocks) {
 				final Long stockId = userStockModel.getStockId();
 				groupedUserStocks.put(stockId, userStockModel);
 			}
 		}
 		return groupedUserStocks;
 	}
 
 	@Override
 	public UserStockModel getStockByUser(final Long userId, final Long stockId) {
 		return userStockDao.getUserStock(userId, stockId);
 	}
 
 	@Override
 	public void save(final UserStockModel stockByUser) {
		// TODO Auto-generated method stub
 
 	}
 
 }
