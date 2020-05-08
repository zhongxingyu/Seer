 package net.java.dev.cejug.classifieds.server.ejb3.entity.facade;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import javax.ejb.EJB;
 import javax.ejb.Stateless;
 import javax.persistence.EntityManager;
 import javax.persistence.NoResultException;
 import javax.persistence.PersistenceContext;
 import javax.persistence.Query;
 
 import net.java.dev.cejug.classifieds.server.ejb3.entity.CustomerEntity;
 import net.java.dev.cejug.classifieds.server.ejb3.entity.DomainEntity;
 import net.java.dev.cejug.classifieds.server.ejb3.entity.QuotaEntity;
 
 @Stateless
 public class CustomerFacade implements CustomerFacadeLocal {
 	@SuppressWarnings("unused")
 	@PersistenceContext(unitName = "classifieds")
 	private EntityManager manager;
 
 	@EJB
 	DomainFacadeLocal domainFacade;
 
 	@Override
 	public CustomerEntity create() throws Exception {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	@Override
 	public void delete(CustomerEntity entity) throws Exception {
 		// TODO Auto-generated method stub
 
 	}
 
 	@Override
 	public List<CustomerEntity> get(String query, int limit) throws Exception {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	@Override
 	public CustomerEntity get(Map<String, String> params) throws Exception {
 		Query query = manager
 				.createNamedQuery("selectCustomerByLoginAndDomain");
		System.out.println("CUSTOMER KKKKKKK : " + params.get("d") + ", "
				+ params.get("l"));
 		if (params != null) {
 			for (String key : params.keySet()) {
 				query.setParameter(key, params.get(key));
 			}
 		}
 		try {
 			return (CustomerEntity) query.getSingleResult();
 		} catch (NoResultException error) {
			/*
			 * TODO: if the customer does not exist, insert a new customer
			 * automatically. It should also add the default "welcome quotas",
			 * to be defined by an external rule from database. The below code
			 * is just a test.
			 */

 			// loading domain
 			Map<String, String> domainParams = new HashMap<String, String>();
			domainParams.put("par1", params.get("d"));
 			DomainEntity domain = domainFacade.get(domainParams);
 
			// insert a new customer
 			CustomerEntity customer = new CustomerEntity();
 			customer.setDomain(domain);
			customer.setLogin((String) params.get("l"));
 			customer.setQuotas(new ArrayList<QuotaEntity>());
 			manager.persist(customer);
 			return customer;
 		}
 	}
 
 	@Override
 	public void update(CustomerEntity entity) throws Exception {
 		// TODO Auto-generated method stub
 
 	}
 
 	@Override
 	public CustomerEntity create(CustomerEntity entity) throws Exception {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	@Override
 	public List<CustomerEntity> get(int limit) throws Exception {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	@Override
 	public List<CustomerEntity> get(Map<String, String> params, int limit)
 			throws Exception {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 }
