 package org.yogocodes.bikewars.dao.impl;
 
 import java.util.List;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.jdbc.core.JdbcTemplate;
 import org.springframework.stereotype.Repository;
 import org.yogocodes.bikewars.dao.UserStockDao;
 import org.yogocodes.bikewars.model.UserStockModel;
 
 @Repository("userStockDao")
 public class UserStockJdbcDaoImpl implements UserStockDao {
 
 	private final Logger logger = LoggerFactory.getLogger(UserStockJdbcDaoImpl.class);
 
 	@Autowired
 	private JdbcTemplate jdbcTemplate;
 
 	@Override
 	public List<UserStockModel> getUserStocks(final Long userId) {
 
 		final String sql = "select * from user_stocks where user_id = ?";
 		logger.trace("getting user stocks: {}", userId);
 		final List<UserStockModel> userStocks = jdbcTemplate.query(sql, new UserStockRowMapper(), userId);
 		logger.trace("got user stocks for user {}", userId);
 		return userStocks;
 	}
 
 	public int updateUserStock(final Long userId, final Long stockId, final Long stockPrice) {
 
 		return 0;
 	}
 
 	@Override
 	public UserStockModel getUserStock(final Long userId, final Long stockId) {
 
 		final String sql = "select * from user_stocks where user_id = ? and stock_id = ?";
 		logger.trace("getting user stocks: {} - {}", userId, stockId);
 		final List<UserStockModel> userStocks = jdbcTemplate.query(sql, new UserStockRowMapper(), userId, stockId);
 		logger.trace("got user stocks for user {} {}", userId, stockId);
 
 		UserStockModel stock = null;
 		if (userStocks != null && !userStocks.isEmpty()) {
 			stock = userStocks.get(0);
 		}
 
 		return stock;
 	}
 
 	@Override
 	public void save(final UserStockModel stockByUser) {
 		if (stockByUser.getId() == null) {
 			insert(stockByUser);
 		} else {
 			update(stockByUser);
 		}
 	}
 
 	private void update(final UserStockModel stockByUser) {
		// TODO Auto-generated method stub

 	}
 
 	private void insert(final UserStockModel stockByUser) {
		/*
		 * final long id = rs.getLong("id"); final Long userId =
		 * rs.getLong("user_id"); final long stockId = rs.getLong("stock_id");
		 * final Long count = rs.getLong("count"); final Date purchaseDate =
		 * rs.getDate("purchase_date"); final long purchasePrice =
		 * rs.getLong("purchase_price");
		 */
 		final String sql = "insert into user_stocks (user_id, stock_id, count, purchase_date, purchase_price) values (?, ?, ?, ? ,?)";
 		final Object params[] = { stockByUser.getUserId(), stockByUser.getStockId(), stockByUser.getCount(), stockByUser.getPurchaseDate(), stockByUser.getPurchasePrice() };
 		jdbcTemplate.update(sql, params);
 
 	}
 
 }
