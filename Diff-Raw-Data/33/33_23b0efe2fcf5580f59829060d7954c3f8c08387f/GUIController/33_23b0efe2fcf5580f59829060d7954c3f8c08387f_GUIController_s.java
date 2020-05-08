 package com.mayroro.controllers;
 
 import java.io.IOException;
 import java.net.URL;
 import java.util.List;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import javax.servlet.http.HttpSession;
 
 import org.springframework.stereotype.Controller;
 import org.springframework.web.bind.annotation.PathVariable;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.servlet.ModelAndView;
 
 import com.google.api.client.auth.oauth2.draft10.AccessTokenResponse;
 import com.google.api.client.googleapis.auth.oauth2.draft10.GoogleAuthorizationRequestUrl;
 import com.google.api.client.googleapis.auth.oauth2.draft10.GoogleAccessTokenRequest.GoogleAuthorizationCodeGrant;
 import com.google.gdata.client.docs.DocsService;
 import com.google.gdata.data.PlainTextConstruct;
 import com.google.gdata.data.spreadsheet.SpreadsheetEntry;
 import com.google.gdata.data.spreadsheet.SpreadsheetFeed;
 import com.mayroro.util.ConstFunc;
 import com.mayroro.util.UserInfo;
 
 @Controller
 @RequestMapping("")
 public class GUIController {
 	@RequestMapping(value={"", "index"})
 	public ModelAndView indexController(HttpServletRequest req, HttpServletResponse res) {
 		ModelAndView mv = new ModelAndView("index");
 		
 		String loginUrl = new GoogleAuthorizationRequestUrl(ConstFunc.CLIENT_ID, ConstFunc.CALLBACK_URL, ConstFunc.SCOPE).build();
 		mv.addObject("loginUrl", loginUrl);
 		
 		return mv;
 	}
 	
 	@RequestMapping("login")
 	public ModelAndView login(HttpServletRequest req, HttpServletResponse res){
 		ModelAndView mv = new ModelAndView("redirect:/home");
 		
 		// Check for errors
 		String error = req.getParameter("error");
 		if (error != null)
 			return new ModelAndView("forward:/error?type="+error);
 		
 		// Get authorization code
 		String authorizationCode = req.getParameter("code");
 		
 		if(authorizationCode == null)
 			return new ModelAndView("forward:/error?type=no_authorization_code");
 		
 		// Exchange for an access and refresh token
 
 		GoogleAuthorizationCodeGrant authRequest = new GoogleAuthorizationCodeGrant(ConstFunc.TRANSPORT, ConstFunc.JSON_FACTORY, ConstFunc.CLIENT_ID, ConstFunc.CLIENT_SECRET,
 					authorizationCode, ConstFunc.CALLBACK_URL);
 		authRequest.useBasicAuthorization = false;
 		AccessTokenResponse authResponse = null;
 		try{
 			authResponse = authRequest.execute();
 			if(authResponse.accessToken == null)
 				throw new IOException();
 		} catch (IOException e) {
 			return new ModelAndView("forward:/error?type=no_access_token");
 		}
 		String accessToken = authResponse.accessToken;
 		
 		System.out.println("accessToken: "+accessToken);
 		
 		// Get user data
 		
 		UserInfo ui = new UserInfo();
 		try {
 			ui = UserInfo.build(accessToken, authResponse.refreshToken);
 		} catch (IOException e) {
 			return new ModelAndView("forward:/error?type=user_info");
 		}
 		
 		HttpSession session = req.getSession();
 		session.setAttribute("userInfo", ui);
 		
 		return mv;
 	}
 	
 	@RequestMapping("home")
 	public ModelAndView welcomeHandler(HttpServletRequest req, HttpServletResponse res) {
 		ModelAndView mv = new ModelAndView("home");
 		
 		HttpSession session = req.getSession();
 		UserInfo ui = (UserInfo)session.getAttribute("userInfo");
 		
 		String accessToken = ui.getAccess_token();
		String refreshToken = ui.getRefresh_token();
		
		System.out.println("AT: "+accessToken);
		System.out.println("RT: "+refreshToken);
 		
 		DocsService service = new DocsService("mayRoro");
 		service.setHeader("Authorization", "OAuth "+accessToken);
 		
 		try {
 			URL metafeedUrl = new URL("https://spreadsheets.google.com/feeds/spreadsheets/private/full?title="+ConstFunc.SPREADSHEET_PREFIX);
 			SpreadsheetFeed feed = service.getFeed(metafeedUrl, SpreadsheetFeed.class);
 			List<SpreadsheetEntry> spreadsheets = feed.getEntries();
 			for (int i = 0; i < spreadsheets.size(); i++) {
 				SpreadsheetEntry entry = spreadsheets.get(i);
 				entry.setTitle(new PlainTextConstruct(entry.getTitle().getPlainText().substring(9)));
				System.out.println("\t" + entry.getTitle().getPlainText());
				System.out.println(entry.getKey());
 			}
 			mv.addObject("spreadsheets", spreadsheets);
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 		
 //		************* Seznam vseh map *************
 //		try {
 //			URL metafeedUrl = new URL("https://docs.google.com/feeds/default/private/full/-/folder");
 //			Feed feed = service.getFeed(metafeedUrl, Feed.class);
 //			List<Entry> spreadsheets = feed.getEntries();
 //			for (int i = 0; i < spreadsheets.size(); i++) {
 //				Entry entry = spreadsheets.get(i);
 //				System.out.println("\t" + entry.getTitle().getPlainText());
 //			}
 //			mv.addObject("spreadsheets", spreadsheets);
 //		} catch (Exception e) {
 //			e.printStackTrace();
 //		}
 		
 		return mv;
 	}
 	
 	@RequestMapping("maut/{spreadsheetID}")
 	public ModelAndView maut(HttpServletRequest req, HttpServletResponse res, @PathVariable int spreadsheetID){
 		ModelAndView mv = new ModelAndView("maut");
 		
 		
 		
 		return mv;
 	}
 }
