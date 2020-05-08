 /*
  * Copyright (c) 2003, 2004 Rafael Steil
  * All rights reserved.
  * 
  * Redistribution and use in source and binary forms, 
  * with or without modification, are permitted provided 
  * that the following conditions are met:
  * 
  * 1) Redistributions of source code must retain the above 
  * copyright notice, this list of conditions and the 
  * following  disclaimer.
  * 2)  Redistributions in binary form must reproduce the 
  * above copyright notice, this list of conditions and 
  * the following disclaimer in the documentation and/or 
  * other materials provided with the distribution.
  * 3) Neither the name of "Rafael Steil" nor 
  * the names of its contributors may be used to endorse 
  * or promote products derived from this software without 
  * specific prior written permission.
  * 
  * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT 
  * HOLDERS AND CONTRIBUTORS "AS IS" AND ANY 
  * EXPRESS OR IMPLIED WARRANTIES, INCLUDING, 
  * BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF 
  * MERCHANTABILITY AND FITNESS FOR A PARTICULAR 
  * PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL 
  * THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE 
  * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, 
  * EXEMPLARY, OR CONSEQUENTIAL DAMAGES 
  * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF 
  * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, 
  * OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER 
  * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER 
  * IN CONTRACT, STRICT LIABILITY, OR TORT 
  * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN 
  * ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF 
  * ADVISED OF THE POSSIBILITY OF SUCH DAMAGE
  * 
  * This file creation date: May 12, 2003 / 8:31:25 PM
  * The JForum Project
  * http://www.jforum.net
  */
 package net.jforum.view.forum;
 
 import java.util.Date;
 import java.util.HashMap;
 import java.util.List;
 
 import org.apache.log4j.Logger;
 
 import net.jforum.Command;
 import net.jforum.JForum;
 import net.jforum.SessionFacade;
 import net.jforum.entities.User;
 import net.jforum.entities.UserSession;
 import net.jforum.model.DataAccessDriver;
 import net.jforum.model.UserModel;
 import net.jforum.model.UserSessionModel;
 import net.jforum.repository.RankingRepository;
 import net.jforum.repository.SecurityRepository;
 import net.jforum.security.SecurityConstants;
 import net.jforum.util.I18n;
 import net.jforum.util.MD5;
 import net.jforum.util.concurrent.executor.QueuedExecutor;
 import net.jforum.util.mail.ActivationKeySpammer;
 import net.jforum.util.mail.EmailException;
 import net.jforum.util.mail.EmailSenderTask;
 import net.jforum.util.mail.LostPasswordSpammer;
 import net.jforum.util.preferences.ConfigKeys;
 import net.jforum.util.preferences.SystemGlobals;
 import net.jforum.view.forum.common.UserCommon;
 import net.jforum.view.forum.common.ViewCommon;
 
 /**
  * @author Rafael Steil
 * @version $Id: UserAction.java,v 1.29 2005/01/31 13:31:27 franklin_samir Exp $
  */
 public class UserAction extends Command 
 {
 	private static final Logger logger = Logger.getLogger(UserAction.class);
 	
 	public void edit() throws Exception 
 	{
 		int tmpId = SessionFacade.getUserSession().getUserId();
 		if (SessionFacade.isLogged() 
 				&& tmpId == this.request.getIntParameter("user_id")) {
 			int userId = this.request.getIntParameter("user_id");
 			UserModel um = DataAccessDriver.getInstance().newUserModel();
 			User u = um.selectById(userId);
 
 			this.context.put("u", u);
 			this.context.put("action", "editSave");
 			this.context.put("moduleAction", "user_form.htm");
 		} 
 		else {
 			this.profile();
 		}
 	}
 
 	public void editDone() throws Exception
 	{
 		this.context.put("editDone", true);
 		this.edit();
 	}
 
 	public void editSave() throws Exception 
 	{
 		int userId = this.request.getIntParameter("user_id");
 		List warns = UserCommon.saveUser(userId);
 
 		if (warns.size() > 0) {
 			this.context.put("warns", warns);
 			this.edit();
 		} 
 		else {
 			JForum.setRedirect(this.request.getContextPath()
 					+ "/user/editDone/" + userId
 					+ SystemGlobals.getValue(ConfigKeys.SERVLET_EXTENSION));
 		}
 	}
 	
 	private void registrationDisabled()
 	{
 		this.context.put("moduleAction", "message.htm");
 		this.context.put("message", I18n.getMessage("User.registrationDisabled"));
 	}
 
 	public void insert() 
 	{
 		if (!SystemGlobals.getBoolValue(ConfigKeys.REGISTRATION_ENABLED)) {
 			this.registrationDisabled();
 			return;
 		}
 		
 		this.context.put("action", "insertSave");
 		this.context.put("moduleAction", "user_new.htm");
 		this.context.put("username", this.request.getParameter("username"));
 		this.context.put("email", this.request.getParameter("email"));
 		
 		if (SystemGlobals.getBoolValue(ConfigKeys.CAPTCHA_REGISTRATION)){
 			//create a new image captcha
 			SessionFacade.getUserSession().createNewCaptcha();
 			this.context.put("captcha_reg", true);
 		}
 	}
 
 	public void insertSave() throws Exception 
 	{
 		if (!SystemGlobals.getBoolValue(ConfigKeys.REGISTRATION_ENABLED)) {
 			this.registrationDisabled();
 			return;
 		}
 		
 		User u = new User();
 		UserModel um = DataAccessDriver.getInstance().newUserModel();
 
 		String username = this.request.getParameter("username");
 		String password = this.request.getParameter("password");
 		String captchaResponse = this.request.getParameter("captchaResponse");
 
 		boolean error = false;
 		if (username == null || username.trim().equals("") 
 				|| password == null || password.trim().equals("")) {
 			this.context.put("error", I18n.getMessage("UsernamePasswordCannotBeNull"));
 			error = true;
 		}
 		
 		if (!error && username.length() > SystemGlobals.getIntValue(ConfigKeys.USERNAME_MAX_LENGTH)) {
 			this.context.put("error", I18n.getMessage("User.usernameTooBig"));
 			error = true;
 		}
 		
 		if (!error && username.indexOf('<') > -1 || username.indexOf('>') > -1) {
 			this.context.put("error", I18n.getMessage("User.usernameInvalidChars"));
 			error = true;
 		}
 
 		if (!error && um.isUsernameRegistered(this.request.getParameter("username"))) {
 			this.context.put("error", I18n.getMessage("UsernameExists"));
 			error = true;
 		}
 		
 		if (!error && !SessionFacade.getUserSession().validateCaptchaResponse(captchaResponse)){
 			this.context.put("error", I18n.getMessage("CaptchaResponseFails"));
 			error = true;
 		}
 
 		if (error) {
 			this.insert();
 
 			return;
 		}
 
 		u.setUsername(username);
 		u.setPassword(MD5.crypt(password));
 		u.setEmail(this.request.getParameter("email"));
 
 		if (SystemGlobals.getBoolValue(ConfigKeys.MAIL_USER_EMAIL_AUTH)) {
 			u.setActivationKey(MD5.crypt(username + System.currentTimeMillis()));
 		}
 
 		int userId = um.addNew(u);
 
 		if (SystemGlobals.getBoolValue(ConfigKeys.MAIL_USER_EMAIL_AUTH)) {
 			try {
 				//Send an email to new user
 				QueuedExecutor.getInstance().execute(
 						new EmailSenderTask(new ActivationKeySpammer(u)));
 			} 
 			catch (Exception e) {
 				logger.warn("Error while trying to send an email: " + e);
 				e.printStackTrace();
 			}
 
 			this.context.put("moduleAction", "message.htm");
 			this.context.put("message", I18n.getMessage("User.GoActivateAccountMessage"));
 		} 
 		else {
 			this.logNewRegisteredUserIn(userId, u);
 		}
 	}
 
 	public void activateAccount() throws Exception 
 	{
 		String hash = this.request.getParameter("hash");
 		int userId = (new Integer(this.request.getParameter("user_id"))).intValue();
 
 		String message = "";
 
 		UserModel um = DataAccessDriver.getInstance().newUserModel();
 		User u = um.selectById(userId);
 
 		boolean isOk = um.validateActivationKeyHash(userId, hash);
 		if (isOk) {
 			// make account active
 			um.writeUserActive(userId);
 			this.logNewRegisteredUserIn(userId, u);
 		} 
 		else {
 			message = I18n.getMessage("User.invalidActivationKey");
 			this.context.put("moduleAction", "message.htm");
 			this.context.put("message", message);
 		}
 
 	}
 
 	private void logNewRegisteredUserIn(int userId, User u) 
 	{
 		SessionFacade.setAttribute("logged", "1");
 
 		UserSession userSession = new UserSession();
 		userSession.setAutoLogin(true);
 		userSession.setUserId(userId);
 		userSession.setUsername(u.getUsername());
 		userSession.setLastVisit(new Date(System.currentTimeMillis()));
 		userSession.setStartTime(new Date(System.currentTimeMillis()));
 
 		SessionFacade.add(userSession);
 
 		// Finalizing.. show to user the congrats page
 		JForum.setRedirect(this.request.getContextPath()
 				+ "/user/registrationComplete"
 				+ SystemGlobals.getValue(ConfigKeys.SERVLET_EXTENSION));
 	}
 
 	public void registrationComplete() throws Exception 
 	{
 		int userId = SessionFacade.getUserSession().getUserId();
 
 		String profilePage = JForum.encodeUrlWithPathAndExtension("/user/edit/" + userId);
 		String homePage = JForum.encodeUrlWithPathAndExtension("/forums/list");
 
 		String message = I18n.getMessage("User.RegistrationCompleteMessage", 
 				new Object[] { profilePage, homePage });
 		this.context.put("message", message);
 		this.context.put("moduleAction", "registration_complete.htm");
 	}
 
 	public void validateLogin() throws Exception 
 	{
 		boolean validInfo = false;
 
 		String password = this.request.getParameter("password");
 		if (password.length() > 0) {
 			User user = this.validateLogin(this.request.getParameter("username"), password);
 
 			if (user != null) {
 				JForum.setRedirect(this.request.getContextPath()
 						+ "/forums/list"
 						+ SystemGlobals.getValue(ConfigKeys.SERVLET_EXTENSION));
 
 				SessionFacade.setAttribute("logged", "1");
 				
 				UserSession tmpUs = null;
 				String sessionId = SessionFacade.isUserInSession(user.getId());
 				
 				UserSession userSession = SessionFacade.getUserSession();
 				userSession.dataToUser(user);
 
 				// Check if the user is returning to the system
 				// before its last session has expired ( hypothesis )
 				if (sessionId != null) {
 					// Write its old session data
 					SessionFacade.storeSessionData(sessionId, JForum.getConnection());
 					tmpUs = SessionFacade.getUserSession(sessionId);
 					SessionFacade.remove(sessionId);
 				}
 				else {
 					UserSessionModel sm = DataAccessDriver.getInstance().newUserSessionModel();
 					tmpUs = sm.selectById(userSession, JForum.getConnection());
 				}
 
 				I18n.load(user.getLang());
 
 				// Autologin
 				if (this.request.getParameter("autologin") != null) {
 					userSession.setAutoLogin(true);
 					JForum.addCookie(SystemGlobals.getValue(ConfigKeys.COOKIE_AUTO_LOGIN), "1");
 					JForum.addCookie(SystemGlobals.getValue(ConfigKeys.COOKIE_USER_HASH), 
 							MD5.crypt(SystemGlobals.getValue(ConfigKeys.USER_HASH_SEQUENCE) + user.getId()));
 				}
 				else {
 					// Remove cookies for safety
 					JForum.addCookie(SystemGlobals.getValue(ConfigKeys.COOKIE_USER_HASH), null);
 					JForum.addCookie(SystemGlobals.getValue(ConfigKeys.COOKIE_AUTO_LOGIN), null);
 				}
 				
 				// TODO: copy'n paste from JForum.java. Move all to an helper class
 				if (tmpUs == null) {
 					userSession.setLastVisit(new Date(System.currentTimeMillis()));
 				}
 				else {
 					// Update last visit and session start time
 					userSession.setLastVisit(new Date(tmpUs.getStartTime().getTime() + tmpUs.getSessionTime()));
 				}
 				
 				SessionFacade.add(userSession);
 				SessionFacade.setAttribute(ConfigKeys.TOPICS_TRACKING, new HashMap());
 
 				JForum.addCookie(SystemGlobals.getValue(ConfigKeys.COOKIE_NAME_DATA), 
 						Integer.toString(user.getId()));
 
 				SecurityRepository.load(user.getId(), true);
 				validInfo = true;
 			}
 		}
 
 		// Invalid login
 		if (validInfo == false) {
 			this.context.put("invalidLogin", "1");
 			this.context.put("moduleAction", "forum_login.htm");
 
 			if (this.request.getParameter("returnPath") != null) {
 				this.context.put("returnPath",
 						this.request.getParameter("returnPath"));
 			}
 		} 
 		else if (ViewCommon.needReprocessRequest()) {
 			ViewCommon.reprocessRequest();
 		}
 		else if (this.request.getParameter("returnPath") != null) {
 			JForum.setRedirect(this.request.getParameter("returnPath"));
 		}
 	}
 
 	private User validateLogin(String name, String password) throws Exception 
 	{
 		UserModel um = DataAccessDriver.getInstance().newUserModel();
 		User user = um.validateLogin(name, password);
 		return user;
 	}
 
 	public void profile() throws Exception 
 	{
 		UserModel um = DataAccessDriver.getInstance().newUserModel();
 
 		User u = um.selectById(this.request.getIntParameter("user_id"));
 
 		this.context.put("moduleAction", "user_profile.htm");
 		this.context.put("karmaEnabled", SecurityRepository.canAccess(SecurityConstants.PERM_KARMA_ENABLED));
 		this.context.put("rank", new RankingRepository());
 		this.context.put("u", u);
 	}
 
 	public void logout() throws Exception 
 	{
 		JForum.setRedirect(this.request.getContextPath()
 				+ "/forums/list"
 				+ SystemGlobals.getValue(ConfigKeys.SERVLET_EXTENSION));
 
 		UserSession userSession = SessionFacade.getUserSession();
 		SessionFacade.storeSessionData(userSession.getSessionId(), JForum.getConnection());
 		
 		// Disable auto login
 		userSession.setAutoLogin(false);
 		userSession.setUserId(SystemGlobals.getIntValue(ConfigKeys.ANONYMOUS_USER_ID));
 
 		SessionFacade.setAttribute("logged", "0");
 		SessionFacade.add(userSession);
 
 		JForum.addCookie(SystemGlobals.getValue(ConfigKeys.COOKIE_AUTO_LOGIN), null);
 		JForum.addCookie(SystemGlobals.getValue(ConfigKeys.COOKIE_NAME_DATA),
 				SystemGlobals.getValue(ConfigKeys.ANONYMOUS_USER_ID));
 	}
 
 	public void login() throws Exception 
 	{
 		if (this.request.getParameter("returnPath") != null) {
 			this.context.put("returnPath", this.request.getParameter("returnPath"));
 		}
 
 		this.context.put("moduleAction", "forum_login.htm");
 	}
 
 	// Lost password form
 	public void lostPassword() 
 	{
 		this.context.put("moduleAction", "lost_password.htm");
 	}
 	
 	public User prepareLostPassword(String username, String email) throws Exception
 	{
 		User user = null;
 		UserModel um = DataAccessDriver.getInstance().newUserModel();
 
 		if (email != null && !email.trim().equals("")) {
 			username = um.getUsernameByEmail(email);
 		}
 
 		if (username != null && !username.trim().equals("")) {
 			List l = um.findByName(username, true);
 			if (l.size() > 0) {
 				user = (User)l.get(0);
 			}
 		}
 		
 		if (user == null) {
 			return null;
 		}
 		
 		String hash = MD5.crypt(user.getEmail() + System.currentTimeMillis());
 		um.writeLostPasswordHash(user.getEmail(), hash);
 		
 		user.setActivationKey(hash);
 		
 		return user;
 	}
 
 	// Send lost password email
 	public void lostPasswordSend() throws Exception 
 	{
 		String email = this.request.getParameter("email");
 		String username = this.request.getParameter("username");
 
 		User user = this.prepareLostPassword(username, email);
 		if (user == null) {
 			// user could not be found
 			this.context.put("message",
 					I18n.getMessage("PasswordRecovery.invalidUserEmail"));
 			this.lostPassword();
 			return;
 		}
 
 		try {
 			QueuedExecutor.getInstance().execute(
 					new EmailSenderTask(new LostPasswordSpammer(user, 
 							I18n.getMessage("PasswordRecovery.mailTitle"))));
 		} 
 		catch (EmailException e) {
 			logger.warn("Error while sending email: " + e);
 		}
 
 		this.context.put("moduleAction", "message.htm");
 		this.context.put("message", I18n.getMessage(
 										"PasswordRecovery.emailSent",
 										new String[] { 
 												this.request.getContextPath()
 												+ "/user/login"
 												+ SystemGlobals.getValue(ConfigKeys.SERVLET_EXTENSION) 
 											}));
 	}
 
 	// Recover user password ( aka, ask him a new one )
 	public void recoverPassword() throws Exception 
 	{
 		String hash = this.request.getParameter("hash");
 
 		this.context.put("moduleAction", "recover_password.htm");
 		this.context.put("recoverHash", hash);
 	}
 
 	public void recoverPasswordValidate() throws Exception 
 	{
 		String hash = this.request.getParameter("recoverHash");
 		String email = this.request.getParameter("email");
 
 		String message = "";
 
 		boolean isOk = DataAccessDriver.getInstance().newUserModel().validateLostPasswordHash(email, hash);
 		if (isOk) {
 			String password = this.request.getParameter("newPassword");
 			DataAccessDriver.getInstance().newUserModel().saveNewPassword(MD5.crypt(password), email);
 
 			message = I18n.getMessage("PasswordRecovery.ok",
 					new String[] { this.request.getContextPath()
 							+ "/user/login"
 							+ SystemGlobals.getValue(ConfigKeys.SERVLET_EXTENSION) });
 		} 
 		else {
 			message = I18n.getMessage("PasswordRecovery.invalidData");
 		}
 
 		this.context.put("moduleAction", "message.htm");
 		this.context.put("message", message);
 	}
 
 		
 	public void list() throws Exception
 	{
 		int start = this.preparePagination(DataAccessDriver.getInstance().newUserModel().getTotalUsers());
 		int usersPerPage = SystemGlobals.getIntValue(ConfigKeys.USERS_PER_PAGE);
 
 		this.context.put("users", DataAccessDriver.getInstance().newUserModel().selectAll(start ,usersPerPage));
 		this.context.put("moduleAction", "user_list.htm");
 	}
 	
 	private int preparePagination(int totalUsers)
 	{
		String s = this.request.getParameter("start");
 		int start = ViewCommon.getStartPage();
 		int usersPerPage = SystemGlobals.getIntValue(ConfigKeys.USERS_PER_PAGE);
 		
 		this.context.put("totalPages", new Double(Math.ceil( (double)totalUsers / usersPerPage )));
 		this.context.put("recordsPerPage", new Integer(usersPerPage));
 		this.context.put("totalRecords", new Integer(totalUsers));
 		this.context.put("thisPage", new Double(Math.ceil( (double)(start + 1) / usersPerPage )));
 		this.context.put("start", new Integer(start));
 		
 		return start;
 	}	
 }
