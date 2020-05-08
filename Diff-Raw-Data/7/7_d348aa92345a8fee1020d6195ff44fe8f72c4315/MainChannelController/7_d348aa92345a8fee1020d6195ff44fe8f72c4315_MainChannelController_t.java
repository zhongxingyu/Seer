 package com.amatic.rc.controller;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Map;
 import java.util.logging.Logger;
 
 import javax.annotation.Resource;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import javax.servlet.http.HttpSession;
 
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Controller;
 import org.springframework.ui.ModelMap;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 
 import com.amatic.rc.constants.WebConstants;
 import com.amatic.rc.dto.Channel;
 import com.amatic.rc.dto.Theme;
 import com.amatic.rc.dto.User;
 import com.amatic.rc.service.ThemeService;
 import com.amatic.rc.service.UChannelService;
 import com.amatic.rc.service.UserService;
 import com.dyuproject.openid.OpenIdUser;
 import com.google.appengine.api.channel.ChannelService;
 import com.google.appengine.api.channel.ChannelServiceFactory;
 
 @Controller
 public class MainChannelController {
 
 	Logger logger = Logger.getLogger(MainChannelController.class.getName());
 
 	List<Integer> sessions = new ArrayList<Integer>();
 
 	@Autowired
 	private ThemeService themeService;
 
 	@Autowired
 	private UChannelService uChannelService;
 
 	@Resource(name = "OIdUserBean")
 	OpenIdUser oIdUserBean;
 
 	@Autowired
 	private UserService userService;
 
 	@SuppressWarnings("unchecked")
 	@RequestMapping(value = { "/index", "/" }, method = { RequestMethod.GET,
 			RequestMethod.POST })
 	public String maininit(ModelMap model, HttpServletRequest request,
 			HttpServletResponse response) throws IOException {
 
 		HttpSession session = request.getSession();
 
 		ChannelService channelService = ChannelServiceFactory
 				.getChannelService();
 		String channelKey = "xyz";
 		String token = channelService.createChannel(channelKey);
 		session.setAttribute("channelKey", channelKey);
 		logger.info("channel definitions done");
 
 		model.addAttribute("token", token);
 
 		// Getting first 32 elements
 		List<Theme> listThemes = themeService.loadAllThemes();
 
 		model.addAttribute("lastPlayedList", listThemes);
 
 		// Saltando Uservalidation
 
 		User user = (User) session
 				.getAttribute(WebConstants.SessionConstants.RC_USER);
 //		// for Refs
 //		if (user != null) {
 //			user = this.userService.findUser(user.getMail());
 //		}
 //		if (user == null) {
 //			user = new User();
 //			user.setMail((String) oIdUserBean.getAttribute("email"));
 //			user.setName((String) oIdUserBean.getAttribute("nickname"));
 //
 //			session.setAttribute(WebConstants.SessionConstants.RC_USER, user);
 //
 //			try {
 //				user = this.userService.findUser(user.getMail());
 //			} catch (com.googlecode.objectify.NotFoundException nf) {
 //				this.userService.create(user, false);
 //			}
 //
 //			user.setNewUser(true);
 //		}
 
 		model.addAttribute("user", user);
		if (user != null){
			// loading <Ref> attributes
			user = userService.findUser(user.getMail());
			List<Channel> userChannels = user.getChannelsDeref();

			model.addAttribute("userChannels", userChannels);
		}
 
 		List<Channel> lastChannels = uChannelService.getLastChannels();
 
 		model.addAttribute("lastChannels", lastChannels);
 
 		// return "loggedChannel";
 		return "mainChannel";
 	}
 	
     @SuppressWarnings("unchecked")
     @RequestMapping(value = "/preAuth", method = RequestMethod.GET)
     public void getPreAuth(ModelMap model, HttpServletRequest request,
             HttpServletResponse response) throws IOException {
 
         User userA = (User) request.getSession().getAttribute(
                 WebConstants.SessionConstants.RC_USER);
         if (userA == null) {
             OpenIdUser userOId = (OpenIdUser) request.getSession()
                     .getAttribute(OpenIdUser.ATTR_NAME);
             Map<String, Object> mUserOId = (Map<String, Object>) userOId
                     .getAttributes().get("info");
             String mail;
             if (mUserOId.get("email") != null) {
                 mail = (String) mUserOId.get("email");
             } else {
                 mail = userOId.getIdentity();
             }
             try {
                 userA = this.userService.findUser(mail);
                 userA.setIpAddress(this.getClienAddress(request));
                 userA = this.userService.updateUserIp(userA);
             } catch (com.googlecode.objectify.NotFoundException nf) {
                 userA = new User();
                 userA.setMail(mail);
                 userA.setIpAddress(this.getClienAddress(request));
                 this.userService.create(userA, false);
                 User.setCont(0);
                 userA.setNewUser(true);
             }
 
             request.getSession().setAttribute(
                     WebConstants.SessionConstants.RC_USER, userA);
         }
 
         response.sendRedirect("/");
     }
 
     public String getClienAddress(HttpServletRequest request) {
         request.getHeader("VIA");
         String ipAddress = request.getHeader("X-FORWARDED-FOR");
         if (ipAddress == null) {
             ipAddress = request.getRemoteAddr();
         }
         return ipAddress;
     }
 
 }
