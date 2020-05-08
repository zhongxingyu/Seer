 package edu.acu.wip.cas;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import javax.naming.Context;
 import javax.naming.InitialContext;
 import javax.naming.NamingException;
 import javax.servlet.Filter;
 
 import org.jasig.cas.client.authentication.AuthenticationFilter;
 import org.jasig.cas.client.util.HttpServletRequestWrapperFilter;
 import org.jasig.cas.client.validation.Cas20ProxyReceivingTicketValidationFilter;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import com.google.inject.Singleton;
 import com.google.inject.servlet.ServletModule;
 
 public class CasServletModule extends ServletModule {
 	private static final Logger logger = LoggerFactory
 			.getLogger(CasServletModule.class);
 
 	private String applicationConfigContext;
	private Map<String,String> filterConfig = new HashMap<String,String>();
 
 	public CasServletModule(String applicationConfigContext) {
 		super();
 		this.applicationConfigContext = applicationConfigContext;
 	}
 
 	public CasServletModule() {
 		super();
 	}
 
 	private static final List<Class<? extends Filter>> CAS_FILTERS = new ArrayList<Class<? extends Filter>>();
 	static {
 		CAS_FILTERS.add(AuthenticationFilter.class);
 		CAS_FILTERS.add(Cas20ProxyReceivingTicketValidationFilter.class);
 		CAS_FILTERS.add(HttpServletRequestWrapperFilter.class);
 	}
 
 	@Override
 	protected void configureServlets() {
 		logger.info("configuring servlets");
 		bind(HashServlet.class).in(Singleton.class);
 		try {
 			Context ctx = new InitialContext();
 			for (String name : new String[] { "serverName",
 					"casServerLoginUrl", "casServerUrlPrefix" }) {
 				String ctxName = this.applicationConfigContext != null ? String
 						.format("cas/%s/%s", this.applicationConfigContext,
 								name) : String.format("cas/%s", name);
 				try {
 					filterConfig.put(name, (String) ctx.lookup(ctxName));
 				} catch (NamingException e) {
 					String topCtxName = String.format("cas/%s", name);
 					logger.info(String.format("Failed to find %s. Trying %s",
 							ctxName, topCtxName));
 					filterConfig.put(name, (String) ctx.lookup(topCtxName));
 				}
 			}
 		} catch (NamingException e) {
 			throw new RuntimeException(e);
 		}
 		for (Class<? extends Filter> c : CAS_FILTERS) {
 			bind(c).in(Singleton.class);
 			filter("/hash").through(c, filterConfig);
 		}
 		serve("/hash").with(HashServlet.class);
 	}
 
 	/**
 	 * protect the urls with CAS.
 	 * 
 	 * @param urlPattern
 	 * @param morePatterns
 	 */
 	public void protect(String urlPattern, String... morePatterns) {
 		for (Class<? extends Filter> c : CAS_FILTERS) {
			filter(urlPattern, morePatterns).through(c, filterConfig);
 		}
 	}
 
 	
 	/**
 	 * protect the urls with regular expressions with CAS.
 	 * 
 	 * @param regex
 	 * @param regexes
 	 */
 	public void protectRegex(String regex, String... regexes) {
 		for (Class<? extends Filter> c : CAS_FILTERS) {
			filterRegex(regex, regexes).through(c, filterConfig);
 		}
 	}
 }
