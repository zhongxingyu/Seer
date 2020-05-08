 package org.netvogue.server.webmvc.controllers;
 
 import java.util.Iterator;
 import java.util.LinkedHashSet;
 import java.util.Map;
 import java.util.Set;
 
 import org.netvogue.server.aws.core.UploadManager;
 import org.netvogue.server.neo4japi.common.ProductLines;
 import org.netvogue.server.neo4japi.common.ResultStatus;
 import org.netvogue.server.neo4japi.common.USER_TYPE;
 import org.netvogue.server.neo4japi.domain.Category;
 import org.netvogue.server.neo4japi.domain.Style;
 import org.netvogue.server.neo4japi.domain.User;
 import org.netvogue.server.neo4japi.service.BoutiqueService;
 import org.netvogue.server.neo4japi.service.LinesheetData;
 import org.netvogue.server.neo4japi.service.LinesheetService;
 import org.netvogue.server.neo4japi.service.StylesheetService;
 import org.netvogue.server.neo4japi.service.UserService;
 import org.netvogue.server.webmvc.domain.ImageURLsResponse;
 import org.netvogue.server.webmvc.domain.JsonRequest;
 import org.netvogue.server.webmvc.domain.JsonResponse;
 import org.netvogue.server.webmvc.domain.Linesheet;
 import org.netvogue.server.webmvc.domain.LinesheetJSONRequest;
 import org.netvogue.server.webmvc.domain.Linesheets;
 import org.netvogue.server.webmvc.domain.StyleJSONResponse;
 import org.netvogue.server.webmvc.domain.StyleResponse;
 import org.netvogue.server.webmvc.domain.Styles;
 import org.netvogue.server.webmvc.security.NetvogueUserDetailsService;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.core.convert.ConversionService;
 import org.springframework.stereotype.Controller;
 import org.springframework.web.bind.annotation.ModelAttribute;
 import org.springframework.web.bind.annotation.RequestBody;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 import org.springframework.web.bind.annotation.RequestParam;
 import org.springframework.web.bind.annotation.ResponseBody;
 
 @Controller
 public class LinesheetController {
 	@Autowired NetvogueUserDetailsService 	userDetailsService;
 	@Autowired BoutiqueService  			boutiqueService;
 	@Autowired UserService 					userService;
 	@Autowired LinesheetService				linesheetService;
 	@Autowired StylesheetService			stylesheetService;
 	@Autowired ConversionService			conversionService;
 
 	@Autowired
 	private UploadManager uploadManager;
 
	@RequestMapping(value={"/getlinesheets", "/getlinesheets/{profileid}"}, method=RequestMethod.GET)
 	public @ResponseBody Linesheets GetLinesheets(@ModelAttribute("profileid") String profileid, 
 												@RequestParam("linesheetname") String linesheetname,
 												@RequestParam("category") String categoryname) {
 		System.out.println("Get Linesheets: " + linesheetname);
 		Linesheets linesheets = new Linesheets();
 		
 		User user;
 		if(!profileid.isEmpty()) {
 			user = userService.getUserByUsername(profileid);
 			if(user == null || USER_TYPE.BOUTIQUE == user.getUserType()) {
 				return linesheets;
 			}
 		} else {
 			 user = userDetailsService.getUserFromSession();
 		}
 		
 		linesheets.setName(user.getName());
 		linesheets.setProfilepic(conversionService.convert(user.getProfilePicLink(), ImageURLsResponse.class));
 		Set<Linesheet> linesheetTemp = new LinkedHashSet<Linesheet>();
 		Iterable<LinesheetData> dbLinesheets;
 		if(linesheetname.isEmpty()) {
 			dbLinesheets = userService.getLinesheets(user);
 		} else {
 			dbLinesheets = userService.getLinesheets(user);
 			//dbCollections = userService.searchCollections(loggedinUser.getUsername(), stylesheetname, categoryname);
 		}
 		if(null == dbLinesheets) {
 			return linesheets;
 		}
 		Iterator<LinesheetData> first = dbLinesheets.iterator();
 		while ( first.hasNext() ){
 			LinesheetData dbSheet = first.next();
 			
 			Linesheet sheet = conversionService.convert(dbSheet.getLinesheet(), Linesheet.class);
 			sheet.setBrandname(dbSheet.getName());
 			linesheetTemp.add(sheet);
 		}
 		linesheets.setLinesheets(linesheetTemp);
 		
 		return linesheets;
 	}
 	
	@RequestMapping(value={"/linesheet/getstyles", "/linesheet/getstyles/{profileid}"}, method=RequestMethod.GET)
 	public @ResponseBody Styles GetStyles(@ModelAttribute("profileid") String profileid, 
 										  @RequestParam("linesheetid") String stylesheetid,
 										  @RequestParam(value="searchquery", required=false) String searchquery
 											 ) {
 		System.out.println("Get Styles: " + searchquery);
 		Styles styles = new Styles();
 		if(stylesheetid.isEmpty()) {
 			return styles;
 		}
 		
 		User user;
 		if(!profileid.isEmpty()) {
 			user = userService.getUserByUsername(profileid);
 			if(user == null || USER_TYPE.BOUTIQUE == user.getUserType()) {
 				return styles;
 			}
 		} else {
 			 user = userDetailsService.getUserFromSession();
 		}
 		
 		styles.setName(user.getName());
 		styles.setProfilepic(conversionService.convert(user.getProfilePicLink(), ImageURLsResponse.class));
 		//This must be stored in session attributes from last query..shoudn't get it from database every time - Azeez
 		org.netvogue.server.neo4japi.domain.Linesheet s = linesheetService.getLinesheet(stylesheetid);
 		if(null == s)
 			return styles;
 		styles.setStylesheetname(s.getLinesheetname());
 		Set<StyleResponse> stylesTemp = new LinkedHashSet<StyleResponse>();
 		Iterable<Style> dbStyles;
 		if(null == searchquery || searchquery.isEmpty()) {
 			dbStyles = linesheetService.getStyles(stylesheetid);
 		} else {
 			//Change this after implementing query
 			//dbStyles = collectionService.searchPhotoByName(stylesheetid, photoname);
 			dbStyles = linesheetService.getStyles(stylesheetid);
 		}
 		if(null == dbStyles) {
 			return styles;
 		}
 		Iterator<Style> first = dbStyles.iterator();
 		while ( first.hasNext() ){
 			Style dbStyle = first.next() ;
 			stylesTemp.add(conversionService.convert(dbStyle, StyleResponse.class));
 		}
 		styles.setStyles(stylesTemp);
 		
 		return styles;
 	}
 	
 	@RequestMapping(value="linesheet/create", method=RequestMethod.POST)
 	public @ResponseBody JsonResponse CreateLinesheet(@RequestBody LinesheetJSONRequest request) {
 		System.out.println("Create Linesheet:" + request.toString());
 		String error = "";
 		JsonResponse response = new JsonResponse();
 		
 		User loggedinUser = userDetailsService.getUserFromSession();
 		if(loggedinUser.getUserType() != USER_TYPE.BRAND) {
 			response.setError("Only brands can create Linesheets");
 			return response;
 		}
 		org.netvogue.server.neo4japi.domain.Linesheet newLinesheet = 
 				new org.netvogue.server.neo4japi.domain.Linesheet(request.getName(), loggedinUser, request.getDeliverydate()); 
 		
 		if(null != request.getCategory()) {
 			ProductLines productLine = ProductLines.getValueOf(request.getCategory());
 			Category cat = boutiqueService.getOrCreateCategory(productLine);
 			newLinesheet.setProductcategory(cat);
 		}
 		if(ResultStatus.SUCCESS == linesheetService.SaveLinesheet(newLinesheet, error)) {  
 			response.setStatus(true);
 			response.setIdcreated(newLinesheet.getLinesheetid());
 		}
 		else
 			response.setError(error);
 		
 		return response;
 	}
 	
 	@RequestMapping(value="linesheet/edit", method=RequestMethod.POST)
 	public @ResponseBody JsonResponse EditLinesheet(@RequestBody LinesheetJSONRequest request) {
 		System.out.println("Edit Linesheet");
 		String error = "";
 		JsonResponse response = new JsonResponse();
 		
 		if(null == request.getId() || request.getId().isEmpty()) {
 			response.setError("linesheet Id is empty");
 			return response;
 		} else if(null == request.getName()) {
 			response.setError("new name is empty");
 			return response;
 		}
 		
 		if(ResultStatus.SUCCESS == linesheetService.editLinesheet(request.getId(), request.getName(), request.getDeliverydate(), error))   
 			response.setStatus(true);
 		else
 				response.setError(error);	
 		return response;
 	}
 	
 	//Think about the categories as well
 	@RequestMapping(value="linesheet/delete", method=RequestMethod.POST)
 	public @ResponseBody JsonResponse DeleteLinesheet(@RequestBody String linesheetId) {
 		System.out.println("Delete Linesheet:"+ linesheetId);
 		String error = "";
 		JsonResponse response = new JsonResponse();
 		
 		if(null == linesheetId || linesheetId.isEmpty()) {
 			response.setError("linesheetId is empty");
 			return response;
 		}
 		if(ResultStatus.SUCCESS == linesheetService.deleteLinesheet(linesheetId, error)) {  
 			response.setStatus(true);
 		}
 		else
 			response.setError(error);
 		
 		return response;
 	}
 	
 	@RequestMapping(value ="linesheet/addstyle",method=RequestMethod.POST)
 	public @ResponseBody StyleJSONResponse AddStyle(@RequestBody JsonRequest request) throws Exception {
 		
 			System.out.println("Add new Style" + request.getValue());
 			String error = "";
 			StyleJSONResponse response = new StyleJSONResponse();
 			
 			User loggedinUser = userDetailsService.getUserFromSession();
 			if(loggedinUser.getUserType() != USER_TYPE.BRAND) {
 				response.setError("Only brands can create styles");
 				return response;
 			}
 			org.netvogue.server.neo4japi.domain.Linesheet linesheet = linesheetService.getLinesheet(request.getId());
 			if(null == linesheet) {
 				response.setError("No linesheet present with this id");
 				return response;
 			}
 			
 			Style styleToAdd = stylesheetService.getStyle(request.getValue(), request.getId());
 			if(null == styleToAdd) {
 				response.setError("No style available with this style id");
 				System.out.println("No style available with this style id");
 				return response;
 			}
 			linesheet.addStyles(styleToAdd);
 			if(ResultStatus.SUCCESS == linesheetService.SaveLinesheet(linesheet, error)) {  
 				response.setStatus(true);
 			}
 			else
 				response.setError(error);
 			
 			return response;
 	}
 	
 	//Change these things once the whole application is completed
 	//All these queries must be changed, as anyone can delete these things if they just have userid Azeez
 	@RequestMapping(value="linesheet/deletestyle", method=RequestMethod.POST)
 	public @ResponseBody JsonResponse DeleteStyle(@RequestBody String styleid) {
 		System.out.println("Delete Style:" + styleid);
 		String error = "";
 		
 		JsonResponse response = new JsonResponse();
 		if(!styleid.isEmpty()) {
 			if(ResultStatus.SUCCESS == linesheetService.deleteStyle(styleid, error)) {  
 				response.setStatus(true);
 			}
 			else
 				response.setError(error);
 		} else {
 			response.setError("styleid is empty");
 		}
 		
 		return response;
 	}
 }
