 /**
  *    Copyright 2012-2013 Trento RISE
  *
  *    Licensed under the Apache License, Version 2.0 (the "License");
  *    you may not use this file except in compliance with the License.
  *    You may obtain a copy of the License at
  *
  *        http://www.apache.org/licenses/LICENSE-2.0
  *
  *    Unless required by applicable law or agreed to in writing, software
  *    distributed under the License is distributed on an "AS IS" BASIS,
  *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  *    See the License for the specific language governing permissions and
  *    limitations under the License.
  */
 
 package eu.trentorise.smartcampus.communicatorservice.controller;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import javax.servlet.http.HttpSession;
 
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.beans.factory.annotation.Value;
 import org.springframework.stereotype.Controller;
 import org.springframework.web.bind.annotation.PathVariable;
 import org.springframework.web.bind.annotation.RequestBody;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 import org.springframework.web.bind.annotation.RequestParam;
 import org.springframework.web.bind.annotation.ResponseBody;
 
 import eu.trentorise.smartcampus.communicator.model.AppAccount;
 import eu.trentorise.smartcampus.communicator.model.AppSignature;
 import eu.trentorise.smartcampus.communicator.model.CloudToPushType;
 import eu.trentorise.smartcampus.communicator.model.Configuration;
 import eu.trentorise.smartcampus.communicator.model.Notification;
 import eu.trentorise.smartcampus.communicator.model.NotificationAuthor;
 import eu.trentorise.smartcampus.communicator.model.UserAccount;
 import eu.trentorise.smartcampus.communicator.model.UserSignature;
 import eu.trentorise.smartcampus.communicatorservice.manager.AppAccountManager;
 import eu.trentorise.smartcampus.communicatorservice.manager.NotificationManager;
 import eu.trentorise.smartcampus.communicatorservice.manager.UserAccountManager;
 import eu.trentorise.smartcampus.exceptions.AlreadyExistException;
 import eu.trentorise.smartcampus.exceptions.SmartCampusException;
 import eu.trentorise.smartcampus.presentation.common.exception.DataException;
 import eu.trentorise.smartcampus.presentation.common.exception.NotFoundException;
 import eu.trentorise.smartcampus.resourceprovider.controller.SCController;
 import eu.trentorise.smartcampus.resourceprovider.model.AuthServices;
 
 @Controller
 public class AccountController extends SCController {
 
 	@Autowired
 	UserAccountManager userAccountManager;
 
 	@Autowired
 	NotificationManager notificationManager;
 
 	@Autowired
 	AppAccountManager appAccountManager;
 
 	@Autowired
 	@Value("${gcm.sender.key}")
 	private String gcm_sender_key;
 
 	@Autowired
 	@Value("${gcm.sender.id}")
 	private String gcm_sender_id;
 
 	@Autowired
 	@Value("${gcm.registration.id.default.key}")
 	private String gcm_registration_id_default_key;
 
 	@Autowired
 	@Value("${gcm.registration.id.default.value}")
 	private String gcm_registration_id_default_value;
 
 	@Autowired
 	private AuthServices services;
 
 	@Override
 	protected AuthServices getAuthServices() {
 		return services;
 	}
 	
 	
 	
 
 	// TODO appName or id required
 	// TODO client flow
 	@RequestMapping(method = RequestMethod.POST, value = "/register/app/{appid}")
 	public @ResponseBody
 	boolean registerApp(HttpServletRequest request,
 			@RequestBody AppSignature signature, @PathVariable String appid,
 			HttpSession session) throws DataException, IOException,
 			NotFoundException, SmartCampusException, AlreadyExistException {
 
 		String senderId = signature.getSenderId();
 		String apikey = signature.getApiKey();
 		String appId = signature.getAppId();
 
 		List<Configuration> listConf = new ArrayList<Configuration>();
 
 		// set value of sender/serverside app registration code
 		if (apikey == null)
 			throw new NotFoundException();
 		// if app is not registered?use ours?
 
 		Map<String, String> listvalue = new HashMap<String, String>();
 		listvalue.put(gcm_sender_key, apikey);
 		listvalue.put(gcm_sender_id, senderId);
 
 		Configuration e = new Configuration(CloudToPushType.GOOGLE, listvalue);
 		listConf.add(e);
 		
 
 		AppAccount appAccount;
 		List<AppAccount> listApp = appAccountManager.getAppAccounts(appId);
 		if (listApp.isEmpty()) {
 			appAccount = new AppAccount();
 			appAccount.setAppId(appId);
 			appAccount.setConfigurations(listConf);
 			appAccountManager.save(appAccount);
 		} else {
 			appAccount = listApp.get(0);
 			appAccount.setConfigurations(listConf);
 			appAccountManager.update(appAccount);
 		}
 
 		return true;
 
 	}
 
 	// TODO appName or id required
 	// TODO client flow, userid required as input
 	@RequestMapping(method = RequestMethod.POST, value = "/register/user/{appid}")
 	public @ResponseBody
 	boolean registerUserToPush(HttpServletRequest request,
 			@PathVariable String appid, @RequestBody UserSignature signature,
 			HttpSession session) throws DataException, IOException,
 			NotFoundException, SmartCampusException, AlreadyExistException {
 
 		UserAccount userAccount;
 		String userId = getUserId();
 
 		// String registrationId = request.getHeader(REGISTRATIONID_HEADER);
 
 		String registrationId = signature.getRegistrationId();
 		String appName = signature.getAppName();
 
 		List<UserAccount> listUser = userAccountManager.findByUserIdAndAppName(
 				userId, appName);
 
 		if (listUser.isEmpty()) {
 			userAccount = new UserAccount();
 			userAccount.setAppId(appid);
 			userAccount.setUserId(userId);
 			try {
 				userAccountManager.save(userAccount);
 			} catch (AlreadyExistException e1) {
 				throw new AlreadyExistException(e1);
 			}
 		} else {
 
 			userAccount = listUser.get(0);
 		}
 
 		List<Configuration> listConf = new ArrayList<Configuration>();
 
 		// set value of sender/serverside user registration code
 		if (registrationId == null)
 			registrationId = gcm_registration_id_default_value;
 		// if user is not registered?use ours?
 
 		// ask type of device
 		
 		Map<String, String> listvalue = new HashMap<String, String>();
 		listvalue.put(gcm_registration_id_default_key, registrationId);
 		
 
 		Configuration e = new Configuration(CloudToPushType.GOOGLE, listvalue);
 		listConf.add(e);	
 
 		userAccount.setConfigurations(listConf);
 		userAccountManager.update(userAccount);
 
 		Notification not = new Notification();
 		not.setDescription("Sei Registrato alle notifiche push");
 		not.setTitle("Sei Registrato alle notifiche push");
 		not.setType(appName);
 		not.setUser(String.valueOf(userAccount.getUserId()));
 		not.setId(null);
 		NotificationAuthor notAuth=new NotificationAuthor();
 		notAuth.setAppId(appid);
 		not.setAuthor(notAuth);
 
 		try {
 			notificationManager.create(not);
 		} catch (NotFoundException e1) {
 			e1.printStackTrace();
 		}
 
 		return true;
 
 	}
 
 	// TODO DELETE method instead of GET
 	// TODO client flow, userid required as input
	@RequestMapping(method = RequestMethod.GET, value = "/unregister/user/{appId}")
 	public @ResponseBody
 	boolean unregisterUserToPush(HttpServletRequest request,
 			@PathVariable String appid,
 			HttpSession session) throws DataException, IOException,
 			NotFoundException, SmartCampusException, AlreadyExistException {
 
 		String userId = getUserId();
 		UserAccount userAccount;
 
 		List<UserAccount> listUser = userAccountManager.findByUserIdAndAppName(
 				userId, appid);
 
 		if (!listUser.isEmpty()) {
 			userAccount = listUser.get(0);
 
 			userAccount.setConfigurations(null);
 			userAccountManager.update(userAccount);
 
 		}
 
 		return true;
 
 	}
 
 	// TODO DELETE method instead of GET
 	// TODO client flow, userid required as input
	@RequestMapping(method = RequestMethod.GET, value = "/unregister/app/{appId}")
 	public @ResponseBody
 	boolean unregisterAppToPush(HttpServletRequest request,
 			@PathVariable String appId, HttpSession session)
 			throws DataException, IOException, NotFoundException,
 			SmartCampusException, AlreadyExistException {
 
 		String userId = getUserId();
 		UserAccount userAccount;
 
 		List<UserAccount> listUser = userAccountManager.findByUserIdAndAppName(
 				userId, appId);
 
 		if (!listUser.isEmpty()) {
 			userAccount = listUser.get(0);
 
 			userAccount.setConfigurations(null);
 			userAccountManager.update(userAccount);
 
 		}
 
 		return true;
 
 	}
 
 	@RequestMapping(method = RequestMethod.POST, value = "/send/app/{appId}")
 	public @ResponseBody
 	void sendAppNotification(HttpServletRequest request,
 			HttpServletResponse response, HttpSession session,@RequestParam(value="users", required=true) String[] userIds,
 			@RequestBody Notification notification,
 			@PathVariable("appId") String appId) throws DataException,
 			IOException, NotFoundException {
 
 
 		NotificationAuthor author = new NotificationAuthor();
 		author.setAppId(appId);
 
 		notification.setType(appId);
 
 		for (String receiver : userIds) {
 			notification.setId(null);
 			notification.setUser(receiver);
 			notificationManager.create(notification);
 		}
 	}
 
 	@RequestMapping(method = RequestMethod.POST, value = "/send/user")
 	public @ResponseBody
 	void sendUserNotification(HttpServletRequest request,
 			HttpServletResponse response, HttpSession session,@RequestParam(value="users", required=true) String[] userIds,
 			@RequestBody Notification notification) throws DataException,
 			IOException, NotFoundException {
 
 	
 		String userId = getUserId();
 		NotificationAuthor author = new NotificationAuthor();
 		author.setUserId(userId);
 
 		notification.setType(userId);
 
 		for (String receiver : userIds) {
 			notification.setId(null);
 			notification.setUser(receiver);
 			notificationManager.create(notification);
 		}
 	}
 
 	// TODO client flow
 	@RequestMapping(method = RequestMethod.GET, value = "/configuration/app/{appid}")
 	public @ResponseBody
 	Map<String, String> requestAppConfigurationToPush(
 			HttpServletRequest request, @PathVariable String appid,
 			HttpSession session) throws DataException, IOException,
 			NotFoundException, SmartCampusException, AlreadyExistException {
 
 		Map<String, String> result=new HashMap<String, String>();
 		AppAccount index = appAccountManager.getAppAccount(appid);
 
 		
 			if (index != null && !index.getConfigurations().isEmpty()) {
 				for(Configuration x: index.getConfigurations()){
 					result.putAll(x.getListValue());
 				}				
 			}
 	
 
 		return result;
 
 	}
 
 	// TODO client flow
 	@RequestMapping(method = RequestMethod.GET, value = "/configuration/user/{appid}")
 	public @ResponseBody
 	Map<String, String> requestUserConfigurationToPush(
 			HttpServletRequest request, @PathVariable String appid,
 			 HttpSession session)
 			throws DataException, IOException, NotFoundException,
 			SmartCampusException, AlreadyExistException {
 
 		
 		Map<String, String> result=new HashMap<String, String>();
 		
 		String userid = getUserId();
 		List<UserAccount> list = userAccountManager.findByUserIdAndAppName(userid, appid);
 		for(UserAccount index : list){
 			if (index != null && !index.getConfigurations().isEmpty()) {
 				for(Configuration x: index.getConfigurations()){
 					result.putAll(x.getListValue());
 				}	
 			}
 	
 		}
 		return result;
 
 	}
 
 }
