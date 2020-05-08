 package com.mss.infrastructure.ormlite;
 
 import com.j256.ormlite.stmt.QueryBuilder;
 import com.mss.domain.models.OrderItem;
 
 public class OrmliteOrderItemRepository extends OrmliteGenericRepository<OrderItem> {
 
 	public OrmliteOrderItemRepository(DatabaseHelper databaseHelper) throws Throwable{
 		super(databaseHelper.getOrderItemDao());
 	}
 	
 	public Iterable<OrderItem> findByOrderId(long id) throws Throwable {
 		
 		QueryBuilder<OrderItem, Integer> queryBuilder = dao.queryBuilder();
 		
		queryBuilder
			.orderBy(com.mss.domain.models.Constants.Tables.OrderItem.PRODUCT_NAME_FIELD, true)
			.where()
			.eq(com.mss.domain.models.Constants.Tables.OrderItem.ORDER_FIELD , id);
 		return dao.query(queryBuilder.prepare());
 	}
 }
