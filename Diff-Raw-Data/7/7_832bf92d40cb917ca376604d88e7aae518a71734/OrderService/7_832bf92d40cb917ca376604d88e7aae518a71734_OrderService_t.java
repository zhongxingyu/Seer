 package com.algo.webshop.server.services;
 
 import java.util.Calendar;
 import java.util.List;
 import java.util.Set;
 
 import org.springframework.stereotype.Service;
 
 import com.algo.webshop.common.domain.Order;
 import com.algo.webshop.common.domainimpl.IOrder;
 import com.algo.webshop.server.jdbc.OrderRowMapper;
 
 @Service("orderService")
 public class OrderService extends AbstractService implements IOrder {
 
 	public Order getOrder(int id) {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	@Override
 	public Order getOrderByNumber(String number) {
 		Order order;
 		order = jdbcTemplate.queryForObject(
 				"select * from orders where (number) = (?)",
 				new Object[] { number }, new OrderRowMapper());
 		return order;
 	}
 
 	@Override
 	public Set<Order> getOrderUser(int users_id) {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	@Override
 	public void addOrder(Order order) {
 		if (order.getUsers_id() == 0) {
 			jdbcTemplate
 					.update("insert into orders (number, phone, email, date_order) values (?,?,?,?)",
 							order.getNumber(), order.getPhone(),
 							order.getEmail(), order.getDate_order());
 		} else {
 			jdbcTemplate
 					.update("insert into orders (number, users_id, date_order) values (?,?,?)",
 							order.getNumber(), order.getUsers_id(),
 							order.getDate_order());
 		}
 	}
 
 	@Override
 	public void updateOrder(Order order) {
 		// TODO Auto-generated method stub
 
 	}
 
 	@Override
 	public String getLastOrderNumber() {
 		String SQL = "select number from orders where id=(SELECT max(id) FROM orders)";
 		return jdbcTemplate.queryForObject(SQL, String.class);
 	}
 
 	@Override
 	public int getOrderIdByNumber(String number) {
 		return jdbcTemplate.queryForObject(
 				"select id from orders where number=?", Integer.class,
 				new Object[] { number });
 	}
 
 	@Override
 	public List<Order> getOrders(int confirmStatus, int canselStatus) {
 		return jdbcTemplate
 				.query("select * from orders where confirm_status_id=? and cansel_status_id=?",
 						new Object[] { confirmStatus, canselStatus },
 						new OrderRowMapper());
 	}
 
 	@Override
 	public Order getOrderById(int id) {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	@Override
 	public List<Order> getOrdersList(int confirmStatus, int canselStatus,
 			Calendar date) {
 		return jdbcTemplate
 				.query("select * from orders where confirm_status_id=? and cansel_status_id=? and date_order>=?",
 						new Object[] { confirmStatus, canselStatus, date },
 						new OrderRowMapper());
 	}
 
	@Override
	public List<Order> getOrdersByUserId(int user_id) {
		return jdbcTemplate
				.query("select * from orders where users_id=?",
						new Object[] { user_id }, new OrderRowMapper());
	}

 }
