 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package eu.trentorise.smartcampus.ac.provider.controllers;
 
 import java.io.IOException;
 import java.util.List;
 import java.util.Map;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
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
 
 /**
  * 
  * @author Viktor Pravdin
  */
 @Controller
 public class AcSpWeb {
 
 	@Autowired
 	private AcServiceAdapter service;
 	@Autowired
 	private AttributesAdapter attrAdapter;
 	@Value("${ac.redirect.hosts}")
 	private String redirectHosts;
 	private static String defaultHosts = null;
 
 	@RequestMapping(method = RequestMethod.GET, value = "/getToken")
 	public String showAuthorities(Model model, HttpServletRequest request) {
 		// FOR TESTING PURPOSES
 		if (request.getParameter("TESTING") != null) {
 			request.getSession().setAttribute("TESTING", true);
 		}
 		Map<String, String> authorities = attrAdapter.getAuthorityUrls();
 		model.addAttribute("authorities", authorities);
 
 		String redirect = request.getParameter("redirect");
 		if (redirect != null && !redirect.isEmpty()) {
 			if (!checkRedirect(redirect, redirectHosts, getDefaultHost(request))) {
 				throw new IllegalArgumentException("Incorrect redirect URL: "
 						+ redirect);
 			}
 			model.addAttribute("redirect", redirect);
 		} else {
 			model.addAttribute("redirect", "");
 		}
 		return "authorities";
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
 	public String getToken(@PathVariable("authorityUrl") String authorityUrl,
 			HttpServletRequest request) throws AcServiceException {
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
 						request.setAttribute(a, "dummyvalue");
 					}
 					// set some values to test alias
 
 					// attribute with alias : see authorities.xml FBK authority
					request.setAttribute("AJP_cn", "dummyvalue");
 
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
 
 		String token = service.updateUser(authorityUrl, request);
 		return "redirect:" + target + "#" + token;
 	}
 
 	@RequestMapping(method = RequestMethod.DELETE, value = "/invalidateToken/{token}")
 	public void deleteToken(@RequestParam("token") String token)
 			throws AcServiceException {
 		service.deleteToken(token);
 	}
 
 	@RequestMapping("/success")
 	public void success() {
 	}
 
 	@ExceptionHandler(IllegalArgumentException.class)
 	public void badRequest(IllegalArgumentException ex, HttpServletResponse resp)
 			throws IOException {
 		resp.sendError(HttpServletResponse.SC_BAD_REQUEST, ex.getMessage());
 	}
 
 }
