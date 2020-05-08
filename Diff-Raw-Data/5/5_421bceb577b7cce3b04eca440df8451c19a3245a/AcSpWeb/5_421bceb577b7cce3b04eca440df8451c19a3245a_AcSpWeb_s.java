 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package eu.trentorise.smartcampus.ac.provider.controllers;
 
 import java.io.IOException;
 import java.util.List;
 import java.util.Map;
 
 import javax.servlet.http.Cookie;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import org.apache.log4j.Logger;
 import org.owasp.esapi.errors.IntrusionException;
 import org.owasp.esapi.errors.ValidationException;
 import org.owasp.validator.html.PolicyException;
 import org.owasp.validator.html.ScanException;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.beans.factory.annotation.Value;
 import org.springframework.stereotype.Controller;
 import org.springframework.ui.Model;
 import org.springframework.web.bind.annotation.ExceptionHandler;
 import org.springframework.web.bind.annotation.PathVariable;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 import org.springframework.web.bind.annotation.RequestParam;
 
 import eu.trentorise.smartcampus.ac.provider.AcServiceException;
 import eu.trentorise.smartcampus.ac.provider.adapters.AcServiceAdapter;
 import eu.trentorise.smartcampus.ac.provider.adapters.AttributesAdapter;
 import eu.trentorise.smartcampus.ac.provider.utils.Utils;
 
 /**
  * 
  * @author Viktor Pravdin
  */
 @Controller
 public class AcSpWeb {
 
 	private static final Logger logger = Logger.getLogger(AcSpWeb.class);
 
 	@Autowired
 	private AcServiceAdapter service;
 	@Autowired
 	private AttributesAdapter attrAdapter;
 	@Value("${ac.redirect.hosts}")
 	private String redirectHosts;
 	private static String defaultHosts = null;
 
 	@Value("${secure.cookies}")
 	private String secureCookies;
 
 	@Autowired
 	private Utils utility;
 
 	@RequestMapping(method = RequestMethod.GET, value = "/getToken")
 	public String showAuthorities(
 			Model model,
 			HttpServletRequest request,
 			@RequestParam(value = "browser", required = false) String browserRequest)
 			throws ValidationException, IntrusionException, ScanException,
 			PolicyException {
 		// FOR TESTING PURPOSES
 		if (request.getParameter("TESTING") != null) {
 			request.getSession().setAttribute("TESTING", true);
 		}
 		// used to attach browser parameter to getToken urls
 		if (browserRequest != null) {
 			model.addAttribute("browser", "");
 		}
 		Map<String, String> authorities = attrAdapter.getAuthorityUrls();
 		model.addAttribute("authorities", authorities);
 
 		String redirect = request.getParameter("redirect");
 		if (redirect != null && !redirect.isEmpty()) {
 			if (!checkRedirect(redirect, redirectHosts, getDefaultHost(request))) {
 				throw new IllegalArgumentException("Incorrect redirect URL: "
 						+ redirect);
 			}
 			model.addAttribute("redirect", utility.sanitize(redirect));
 		} else {
 			model.addAttribute("redirect", "");
 		}
 		return "authorities";
 	}
 
 	private boolean isSecureCookiesEnvironment() {
 		if (secureCookies == null) {
 			logger.warn("secure.cookies configuration not present, it will be loaded default value: true");
 		} else if (!secureCookies.trim().equalsIgnoreCase("n")
 				&& !secureCookies.trim().equalsIgnoreCase("y")) {
 			logger.warn("secure.cookies setted with unknown value, it will be loaded default value: true");
 		} else {
 			logger.info("secure.cookies setted with value "
 					+ secureCookies.trim());
 		}
 
 		return secureCookies == null
 				|| !secureCookies.trim().equalsIgnoreCase("n");
 	}
 
 	private static String getDefaultHost(HttpServletRequest request) {
 		if (defaultHosts == null) {
 			String result = request.getServerPort() == 80 ? (request
 					.getServerName() + ",") : "";
 			defaultHosts = result + request.getServerName() + ":"
 					+ request.getServerPort();
 		}
 		return defaultHosts;
 	}
 
 	private static boolean checkRedirect(String redirect, String redirectHosts,
 			String _default) {
 		String hosts = redirectHosts != null ? redirectHosts : _default;
 		String[] array = hosts.split(",");
 		for (String s : array) {
 			if (redirect.matches("((https)|(http))://" + s + "/(.)*"))
 				return true;
 		}
 		return false;
 	}
 
 	@RequestMapping(method = RequestMethod.GET, value = "/getToken/{authorityUrl}")
 	public String getToken(
 			@PathVariable("authorityUrl") String authorityUrl,
 			HttpServletRequest request,
 			HttpServletResponse response,
 			@RequestParam(value = "browser", required = false) String browserRequest)
 			throws AcServiceException, IOException {
 		// FOR TESTING PURPOSES
 		if (request.getParameter("TESTING") != null
 				|| request.getSession().getAttribute("TESTING") != null) {
 			request.getSession().setAttribute("TESTING", true);
 			Map<String, String> authorities = attrAdapter.getAuthorityUrls();
 			for (String name : authorities.keySet()) {
 				if (authorityUrl.equals(authorities.get(name))) {
 					List<String> attrs = attrAdapter
 							.getIdentifyingAttributes(name);
 					for (String a : attrs) {
 						request.setAttribute(a, "sc-user");
 					}
 					// set some values to test alias
 
 					// attribute with alias : see authorities.xml FBK authority
 					request.setAttribute("givenName", "sc");
 
 					request.setAttribute("Shib-Application-ID", "dummyvalue");
 
 				}
 			}
 		}
 
 		String redirect = request.getParameter("redirect");
 
 		String target = "/ac/success";
 		if (redirect != null && !redirect.isEmpty()) {
 			if (!checkRedirect(redirect, redirectHosts, getDefaultHost(request))) {
 				throw new IllegalArgumentException("Incorrect redirect URL: "
 						+ redirect);
 			}
 			target = redirect;
 		}
 		String token = "";
 		try {
 			token = service.updateUser(authorityUrl, request);
 		} catch (SecurityException e) {
 			response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
 			target = "/ac/denied";
 		}
 
 		if (browserRequest != null) {
 			Cookie authCookie = new Cookie("auth_token", token);
 			authCookie.setPath("/");
 			// cookie set secure only in production environment
 			if (isSecureCookiesEnvironment()) {
 				authCookie.setSecure(true);
 			}
 
 			response.addCookie(authCookie);
 			return "redirect:" + target;
 		} else {
 			return "redirect:" + target + "#" + token;
 		}
 	}
 
 	@RequestMapping(method = RequestMethod.DELETE, value = "/invalidateToken/{token}")
 	public void deleteToken(@RequestParam("token") String token)
 			throws AcServiceException {
 		service.deleteToken(token);
 	}
 
 	@RequestMapping("/success")
 	public void success() {
 	}
 
 	@RequestMapping("/denied")
 	public void denied() {
 	}
 
 	@ExceptionHandler(IllegalArgumentException.class)
 	public void badRequest(IllegalArgumentException ex, HttpServletResponse resp)
 			throws IOException {
 		resp.sendError(HttpServletResponse.SC_BAD_REQUEST, ex.getMessage());
 	}
 }
