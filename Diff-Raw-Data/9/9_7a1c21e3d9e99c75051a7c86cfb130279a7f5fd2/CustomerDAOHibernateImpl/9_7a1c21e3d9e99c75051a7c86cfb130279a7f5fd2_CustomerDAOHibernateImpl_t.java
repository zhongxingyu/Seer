 package org.young.sh.dao;
 
 import java.util.List;
 
 import org.young.hibernate.dao.ICustomerDAO;
 import org.young.hibernate.entity.Customer;
 
 public class CustomerDAOHibernateImpl extends DefaultDAO implements
 		ICustomerDAO {
 
 	public void add(Customer customer) {
 		
 		super.getHibernateTemplate().save(customer);
 
 	}
 
 	@SuppressWarnings("unchecked")
 	public List<Customer> load() {
 		
 		return getHibernateTemplate().loadAll(Customer.class);
 		
 	}
 
 }
