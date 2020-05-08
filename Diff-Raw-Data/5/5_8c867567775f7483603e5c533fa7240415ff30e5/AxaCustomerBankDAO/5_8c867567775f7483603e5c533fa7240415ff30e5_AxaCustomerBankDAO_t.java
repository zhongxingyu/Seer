 package com.santoris.bsimple.axa.dao.bank;
 
 import java.util.List;
 
 import org.apache.log4j.Logger;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.beans.factory.annotation.Value;
 import org.springframework.stereotype.Repository;
 import org.springframework.web.client.RestTemplate;
 
 import com.santoris.bsimple.axa.model.AxaAccount;
 import com.santoris.bsimple.axa.model.AxaCustomer;
 
 @Repository
 public class AxaCustomerBankDAO {
 	
 	private Logger logger  = Logger.getLogger(getClass());
 	
 	@Autowired
 	private AxaAccountBankDAO accountDAO;
 	
 	@Value("${base_url}")
 	private String baseUrl;
 	
 	@Value("${client_id}")
 	private String clientId;
 	
 	@Value("${access_token}")
 	private String accessToken;
 	
 	private RestTemplate restTemplate = new RestTemplate();
 	
 	public AxaCustomer getCustomerById(Long customerId) {
		AxaCustomer customer = restTemplate.getForObject(baseUrl + "/customers/{customerId}?client_id={clientId}&access_token={accessToken}&customer_id={customerId}",
 				AxaCustomer.class,
				customerId,clientId,accessToken,customerId);
 		
 		List<AxaAccount> accounts = accountDAO.getAllAccountsByCustomer(customerId);
 		customer.setAccounts(accounts);
 		
 		return customer;
 	}
 	
 }
