 package com.res.dao.hibernate.impl;
 
import org.apache.log4j.Logger;
 import org.springframework.stereotype.Repository;
 
 import com.res.dao.hibernate.CustomerOrderDao;
 
 @Repository("customerOrderDao")
 public class CustomerOrderDaoImpl extends BaseDaoImpl implements CustomerOrderDao {
 
	public static Logger logger = Logger.getLogger(CustomerOrderDaoImpl.class);
 }
