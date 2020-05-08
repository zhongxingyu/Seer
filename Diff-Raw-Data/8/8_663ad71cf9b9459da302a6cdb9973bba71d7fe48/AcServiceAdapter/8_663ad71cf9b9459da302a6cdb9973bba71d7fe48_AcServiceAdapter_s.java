 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package eu.trentorise.smartcampus.ac.provider.adapters;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.List;
 import java.util.Map;
 
 import javax.annotation.PostConstruct;
 import javax.servlet.http.HttpServletRequest;
 import javax.xml.bind.JAXBException;
 
 import org.apache.cxf.endpoint.Client;
 import org.apache.cxf.frontend.ClientProxy;
 import org.apache.cxf.jaxws.JaxWsProxyFactoryBean;
 import org.apache.cxf.transport.http.HTTPConduit;
 import org.apache.cxf.transports.http.configuration.HTTPClientPolicy;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.beans.factory.annotation.Value;
 import org.springframework.stereotype.Component;
 
 import eu.trentorise.smartcampus.ac.provider.AcProviderService;
 import eu.trentorise.smartcampus.ac.provider.AcServiceException;
 import eu.trentorise.smartcampus.ac.provider.model.Attribute;
 import eu.trentorise.smartcampus.ac.provider.model.Authority;
 import eu.trentorise.smartcampus.ac.provider.model.User;
 
 /**
  * 
  * @author Viktor Pravdin
  */
 @Component
 public class AcServiceAdapter {
 
 	private static final long DAY = 24L * 3600 * 1000;
 	private static final long TOKEN_DEADLINE = 2 * DAY;
 
 	@Value("${ac.endpoint.url}")
 	private String endpointUrl;
 	@Autowired
 	private AttributesAdapter attrAdapter;
 	private AcProviderService service;
 
 	@PostConstruct
 	private void init() throws JAXBException, AcServiceException {
 		JaxWsProxyFactoryBean factory = new JaxWsProxyFactoryBean();
 		factory.setServiceClass(AcProviderService.class);
 		factory.setAddress(endpointUrl);
 
 		service = (AcProviderService) factory.create();
 
 		Client client = ClientProxy.getClient(service);
 		if (client != null) {
 			HTTPConduit conduit = (HTTPConduit) client.getConduit();
 			HTTPClientPolicy policy = new HTTPClientPolicy();
 			policy.setConnectionTimeout(10000);
 			policy.setReceiveTimeout(10000);
 			policy.setAllowChunking(false);
 			conduit.setClient(policy);
 		}
 		attrAdapter.init();
 	}
 
 	public String updateUser(String authorityUrl, HttpServletRequest req)
 			throws AcServiceException {
 		Authority auth = service.getAuthorityByUrl(authorityUrl);
 		if (auth == null) {
 			throw new IllegalArgumentException("Unknown authority URL: "
 					+ authorityUrl);
 		}
 		Map<String, String> attributes = attrAdapter.getAttributes(
 				auth.getName(), req);
 		List<String> ids = attrAdapter.getIdentifyingAttributes(auth.getName());
 		// Try to find an already existing user
 		List<Attribute> list = new ArrayList<Attribute>();
 		for (String key : ids) {
 			if (!attributes.containsKey(key)) {
 				throw new IllegalArgumentException(
 						"The required attribute is missing: " + key);
 			}
 			Attribute a = new Attribute();
 			a.setAuthority(auth);
 			a.setKey(key);
 			a.setValue(attributes.get(key));
 			list.add(a);
 		}
 		List<User> users = service.getUsersByAttributes(list);
 		if (users == null)
 			users = new ArrayList();
 		if (users.size() > 1) {
 			throw new IllegalArgumentException(
 					"The request attributes identify more than one user");
 		}
 		list.clear();
 		for (String key : attributes.keySet()) {
 			String value = attributes.get(key);
 			Attribute attr = new Attribute();
 			attr.setAuthority(auth);
 			attr.setKey(key);
 			attr.setValue(value);
 			list.add(attr);
 		}
 		String token = null;
 		Long expirationDate = null;
 		if (users.isEmpty()) {
 			token = service.generateAuthToken();
 			service.createUser(token, System.currentTimeMillis()
 					+ TOKEN_DEADLINE, list);
 		} else {
 			User user = users.get(0);
 			if (service.isValidUser(user.getAuthToken())) {
 				token = user.getAuthToken();
 				expirationDate = user.getExpTime();
 			} else {
 				token = service.generateAuthToken();
 				expirationDate = System.currentTimeMillis() + TOKEN_DEADLINE;
 			}
 			service.updateUser(user.getId(), token, expirationDate, list);
			// token = user.getAuthToken();
 		}
 		return token;
 	}
 
 	public boolean deleteToken(String token) throws AcServiceException {
 		return service.removeUser(token);
 	}
 
 	protected Authority getAuthorityByName(String name)
 			throws AcServiceException {
 		return service.getAuthorityByName(name);
 	}
 
 	protected Authority getAuthorityByUrl(String url) throws AcServiceException {
 		return service.getAuthorityByUrl(url);
 	}
 
 	protected void createAuthority(Authority auth) throws AcServiceException {
 		service.createAuthority(auth);
 	}
 
 	protected Collection<Authority> getAuthorities() throws AcServiceException {
 		return service.getAuthorities();
 	}
 }
