 package nz.co.searchwellington.openid;
 
 import java.util.List;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import nz.co.searchwellington.controllers.AnonUserService;
 import nz.co.searchwellington.controllers.LoggedInUserFilter;
 import nz.co.searchwellington.controllers.LoginResourceOwnershipService;
 import nz.co.searchwellington.controllers.UrlStack;
 import nz.co.searchwellington.model.User;
 import nz.co.searchwellington.repositories.UserRepository;
 import nz.co.searchwellington.urls.UrlBuilder;
 
 import org.apache.log4j.Logger;
 import org.openid4java.association.AssociationException;
 import org.openid4java.consumer.ConsumerException;
 import org.openid4java.consumer.ConsumerManager;
 import org.openid4java.consumer.VerificationResult;
 import org.openid4java.discovery.DiscoveryException;
 import org.openid4java.discovery.DiscoveryInformation;
 import org.openid4java.discovery.Identifier;
 import org.openid4java.message.AuthRequest;
 import org.openid4java.message.MessageException;
 import org.openid4java.message.ParameterList;
 import org.springframework.web.servlet.ModelAndView;
 import org.springframework.web.servlet.view.RedirectView;
 
 public class OpenIDLoginController extends AbstractExternalSigninController {
 	
 	static Logger log = Logger.getLogger(OpenIDLoginController.class);
 	private static final String OPENID_CLAIMED_IDENTITY_PARAMETER = "openid_claimed_identity";
 	
 	public ConsumerManager manager;
 	private UrlBuilder urlBuilder;
 	
 	
 	public OpenIDLoginController(UrlBuilder urlBuilder, UrlStack urlStack,
 			UserRepository userDAO,
 			LoginResourceOwnershipService loginResourceOwnershipService,
 			LoggedInUserFilter loggedInUserFilter,
 			AnonUserService anonUserService) throws ConsumerException {
 		manager = new ConsumerManager();
 		this.urlBuilder = urlBuilder;
 		this.urlStack = urlStack;
 		this.userDAO = userDAO;
 		this.loginResourceOwnershipService = loginResourceOwnershipService;
 		this.loggedInUserFilter = loggedInUserFilter;
 		this.anonUserService = anonUserService;
 	}
 
     
 	@SuppressWarnings("unchecked")
 	public ModelAndView login(HttpServletRequest request, HttpServletResponse response) throws Exception {
 		if (request.getParameter(OPENID_CLAIMED_IDENTITY_PARAMETER) != null) {
 			final String userSuppliedOpenID = request.getParameter(OPENID_CLAIMED_IDENTITY_PARAMETER);		
 			try {
 				// discover the OpenID authentication server's endpoint URL
 				List discoveries = manager.discover(userSuppliedOpenID);
 				
 				// attempt to associate with the OpenID provider and retrieve one service endpoint for authentication
 				DiscoveryInformation discovered = manager.associate(discoveries);
 	
 				// store the discovery information in the user's session for later use
 				request.getSession().setAttribute("discovered", discovered);
 	    	    	
 				// define the return path
 				String returnURL = urlBuilder.getOpenIDCallbackUrl();
 	    	
 				// generate an AuthRequest message to be sent to the OpenID provider
 				AuthRequest authReq = manager.authenticate(discovered, returnURL);
 	
 				// redirect the user to their provider for authentication    	
 				String destinationUrl = authReq.getDestinationUrl(true);
 				return new ModelAndView(new RedirectView(destinationUrl));
 				
 			} catch (Exception e) {
 				log.warn("Exception will processing claimed identifier: " + userSuppliedOpenID, e);
 			}
 		}
 		
 		return signinErrorView(request);
     }
 
 	
 	@Override
 	protected String getExternalUserIdentifierFromCallbackRequest(HttpServletRequest request) {
 		String openid = null;
 				
 		// extract the parameters from the authentication response
 		// (which comes in as a HTTP request from the OpenID provider)
 		ParameterList openidResp = new ParameterList(request.getParameterMap());
 
 		// retrieve the previously stored discovery information
 		DiscoveryInformation discovered = (DiscoveryInformation) request.getSession().getAttribute("discovered");
 
 		// extract the receiving URL from the HTTP request
 		StringBuffer receivingURL = request.getRequestURL();
 		String queryString = request.getQueryString();
 
 		if (queryString != null && queryString.length() > 0) {
 			receivingURL.append("?").append(request.getQueryString());
 		}
 
 		// verify the response
 		VerificationResult verification;
 		try {
 			verification = manager.verify(receivingURL.toString(), openidResp, discovered);
 			// examine the verification result and extract the verified identifier
 			Identifier verified = verification.getVerifiedId();		
 			if (verified != null) {
 				openid = verified.getIdentifier();			
 			}
 			return openid;
 						
 		} catch (MessageException e) {
 			log.warn("OpenID error in callback: " + e.getMessage());
 		} catch (DiscoveryException e) {
 			log.warn("OpenID error in callback: " + e.getMessage());
 		} catch (AssociationException e) {
 			log.warn("OpenID error in callback: " + e.getMessage());
 		}
 		return null;
 	}
 
 	
 	@Override
 	protected void decorateUserWithExternalSigninIndenfier(User user, Object externalIdentifier) {
 		final String openId = (String) externalIdentifier;
 		user.setOpenId(openId);
 	}
 
 
 	@Override
 	protected User getUserByExternalIdentifier(Object externalIdentifier) {
		// TODO Auto-generated method stub
		return null;
 	}
 	
 }
