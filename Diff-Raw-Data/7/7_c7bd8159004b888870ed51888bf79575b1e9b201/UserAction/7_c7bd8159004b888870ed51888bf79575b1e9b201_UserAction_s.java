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
 import java.util.List;
 
 import org.apache.log4j.Logger;
 
 import net.jforum.Command;
 import net.jforum.JForum;
 import net.jforum.SessionFacade;
 import net.jforum.entities.User;
 import net.jforum.entities.UserSession;
 import net.jforum.model.DataAccessDriver;
 import net.jforum.model.UserModel;
 import net.jforum.repository.RankingRepository;
 import net.jforum.repository.SecurityRepository;
 import net.jforum.util.I18n;
 import net.jforum.util.MD5;
 import net.jforum.util.concurrent.executor.QueuedExecutor;
 import net.jforum.util.mail.ActivationKeySpammer;
 import net.jforum.util.mail.EmailException;
 import net.jforum.util.mail.EmailSenderTask;
 import net.jforum.util.mail.LostPasswordSpammer;
 import net.jforum.util.preferences.ConfigKeys;
 import net.jforum.util.preferences.SystemGlobals;
 
 /**
  * @author Rafael Steil
 * @version $Id: UserAction.java,v 1.11 2004/10/10 16:49:51 rafaelsteil Exp $
  */
 public class UserAction extends Command 
 {
 	private static final Logger logger = Logger.getLogger(UserAction.class);
 	
 	public void edit() throws Exception 
 	{
 		int tmpId = SessionFacade.getUserSession().getUserId();
 		if (SessionFacade.isLogged() 
 				&& tmpId == Integer.parseInt(JForum.getRequest().getParameter("user_id"))) {
 			int userId = Integer.parseInt(JForum.getRequest().getParameter("user_id"));
 			UserModel um = DataAccessDriver.getInstance().newUserModel();
 			User u = um.selectById(userId);
 
 			JForum.getContext().put("u", u);
 			JForum.getContext().put("action", "editSave");
 			JForum.getContext().put("moduleAction", "user_form.htm");
 		} 
 		else {
 			this.profile();
 		}
 	}
 
 	public void editDone() throws Exception
 	{
 		JForum.getContext().put("editDone", true);
 		this.edit();
 	}
 
 	public void editSave() throws Exception 
 	{
 		int userId = Integer.parseInt(JForum.getRequest().getParameter("user_id"));
 		List warns = ViewCommon.saveUser(userId);
 
 		if (warns.size() > 0) {
 			JForum.getContext().put("warns", warns);
 			this.edit();
 		} 
 		else {
 			JForum.setRedirect(JForum.getRequest().getContextPath()
 					+ "/user/editDone/" + userId
 					+ SystemGlobals.getValue(ConfigKeys.SERVLET_EXTENSION));
 		}
 	}
 
 	public void insert() 
 	{
 		JForum.getContext().put("action", "insertSave");
 		JForum.getContext().put("moduleAction", "user_new.htm");
 	}
 
 	public void insertSave() throws Exception 
 	{
 		User u = new User();
 		UserModel um = DataAccessDriver.getInstance().newUserModel();
 
 		String username = JForum.getRequest().getParameter("username");
 		String password = JForum.getRequest().getParameter("password");
 
 		boolean error = false;
 		if (username == null || username.trim().equals("") 
 				|| password == null || password.trim().equals("")) {
 			JForum.getContext().put("error", I18n.getMessage("UsernamePasswordCannotBeNull"));
 			error = true;
 		}
 
 		if (!error && um.isUsernameRegistered(JForum.getRequest().getParameter("username"))) {
 			JForum.getContext().put("error", I18n.getMessage("UsernameExists"));
 			error = true;
 		}
 
 		if (error) {
 			this.insert();
 
 			return;
 		}
 
 		u.setUsername(username);
 		u.setPassword(MD5.crypt(password));
 		u.setEmail(JForum.getRequest().getParameter("email"));
 
 		if (SystemGlobals.getBoolValue(ConfigKeys.MAIL_USER_EMAIL_AUTH)) {
 			u.setActivationKey(MD5.crypt(username + System.currentTimeMillis()));
 		}
 
 		int userId = um.addNew(u);
 
 		if (SystemGlobals.getBoolValue(ConfigKeys.MAIL_USER_EMAIL_AUTH)) {
 			try {
 				//Send an email to new user
 				QueuedExecutor.getInstance().execute(
 						new EmailSenderTask(new ActivationKeySpammer(
 								userId, u.getUsername(), u.getEmail(), u.getActivationKey())));
 			} 
 			catch (Exception e) {
 				logger.warn("Error while trying to send an email: " + e);
 				e.printStackTrace();
 			}
 
 			JForum.getContext().put("moduleAction", "message.htm");
 			JForum.getContext().put("message", I18n.getMessage("User.GoActivateAccountMessage"));
 		} 
 		else {
 			this.logNewRegisteredUserIn(userId, u);
 		}
 	}
 
 	public void activateAccount() throws Exception 
 	{
 		String hash = JForum.getRequest().getParameter("hash");
 		int userId = (new Integer(JForum.getRequest().getParameter("user_id"))).intValue();
 
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
 			JForum.getContext().put("moduleAction", "message.htm");
 			JForum.getContext().put("message", message);
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
 		JForum.setRedirect(JForum.getRequest().getContextPath()
 				+ "/user/registrationComplete"
 				+ SystemGlobals.getValue(ConfigKeys.SERVLET_EXTENSION));
 	}
 
 	public void registrationComplete() throws Exception 
 	{
 		int userId = SessionFacade.getUserSession().getUserId();
 
 		String profilePage = JForum.getRequest().getContextPath()
 				+ "/user/edit/" + userId
 				+ SystemGlobals.getValue(ConfigKeys.SERVLET_EXTENSION);
 
 		String homePage = JForum.getRequest().getContextPath() 
 			+ "/forums/list"
 			+ SystemGlobals.getValue(ConfigKeys.SERVLET_EXTENSION);
 
 		String message = I18n.getMessage("User.RegistrationCompleteMessage", 
 				new Object[] { profilePage, homePage });
 		JForum.getContext().put("message", message);
 		JForum.getContext().put("moduleAction", "registration_complete.htm");
 	}
 
 	public void validateLogin() throws Exception 
 	{
 		boolean validInfo = false;
 
 		String password = JForum.getRequest().getParameter("password");
 		if (password.length() > 0) {
 			User user = validateLogin(JForum.getRequest().getParameter("username"), password);
 
 			if (user != null) {
 				JForum.setRedirect(JForum.getRequest().getContextPath()
 						+ "/forums/list"
 						+ SystemGlobals.getValue(ConfigKeys.SERVLET_EXTENSION));
 
 				SessionFacade.setAttribute("logged", "1");
 
 				UserSession userSession = SessionFacade.getUserSession();
 				userSession.setUserId(user.getId());
 				userSession.setUsername(JForum.getRequest().getParameter("username"));
 				userSession.setPrivateMessages(user.getPrivateMessagesCount());
 
 				userSession.setLang(user.getLang());
 				if (user.getLang() != null && !user.getLang().equals("")
 						&& !I18n.contains(user.getLang())) {
 					I18n.load(user.getLang());
 				}
 
 				// Autologin
 				if (JForum.getRequest().getParameter("autologin") != null) {
 					userSession.setAutoLogin(true);
 					JForum.addCookie(SystemGlobals.getValue(ConfigKeys.COOKIE_AUTO_LOGIN), "1");
 				}
 
 				SessionFacade.add(userSession);
 
 				JForum.addCookie(SystemGlobals.getValue(ConfigKeys.COOKIE_NAME_DATA), 
 						Integer.toString(user.getId()));
 				JForum.addCookie(SystemGlobals.getValue(ConfigKeys.COOKIE_USER_HASH), 
 						MD5.crypt(SystemGlobals.getValue(ConfigKeys.USER_HASH_SEQUENCE) + user.getId()));
 
 				SecurityRepository.load(user.getId(), true);
 				validInfo = true;
 			}
 		}
 
 		// Invalid login
 		if (validInfo == false) {
 			JForum.getContext().put("invalidLogin", "1");
 			JForum.getContext().put("moduleAction", "forum_login.htm");
 
 			if (JForum.getRequest().getParameter("returnPath") != null) {
 				JForum.getContext().put("returnPath",
 						JForum.getRequest().getParameter("returnPath"));
 			}
 		} 
 		else if (JForum.getRequest().getParameter("returnPath") != null) {
 			JForum.setRedirect(JForum.getRequest().getParameter("returnPath"));
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
 
 		User u = um.selectById(Integer.parseInt(JForum.getRequest().getParameter("user_id")));
 
 		JForum.getContext().put("moduleAction", "user_profile.htm");
 		JForum.getContext().put("rank", new RankingRepository());
 		JForum.getContext().put("u", u);
 	}
 
 	public void logout() throws Exception 
 	{
 		JForum.setRedirect(JForum.getRequest().getContextPath()
 				+ "/forums/list"
 				+ SystemGlobals.getValue(ConfigKeys.SERVLET_EXTENSION));
 
 		// Disable auto login
 		UserSession userSession = SessionFacade.getUserSession();
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
 		if (JForum.getRequest().getParameter("returnPath") != null) {
 			JForum.getContext().put("returnPath", JForum.getRequest().getParameter("returnPath"));
 		}
 
 		JForum.getContext().put("moduleAction", "forum_login.htm");
 	}
 
 	// Lost password form
 	public void lostPassword() 
 	{
 		JForum.getContext().put("moduleAction", "lost_password.htm");
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
 		
		String hash = MD5.crypt(email + System.currentTimeMillis());
		um.writeLostPasswordHash(email, hash);
 		
 		user.setActivationKey(hash);
 		
 		return user;
 	}
 
 	// Send lost password email
 	public void lostPasswordSend() throws Exception 
 	{
 		String email = JForum.getRequest().getParameter("email");
 		String username = JForum.getRequest().getParameter("username");
 
 		User user = this.prepareLostPassword(username, email);
 		if (user == null) {
 			// user could not be found
 			JForum.getContext().put("message",
 					I18n.getMessage("PasswordRecovery.invalidUserEmail"));
 			this.lostPassword();
 			return;
 		}
 
 		try {
 			QueuedExecutor.getInstance().execute(
 					new EmailSenderTask(new LostPasswordSpammer(user)));
 		} 
 		catch (EmailException e) {
 			logger.warn("Error while senting email: " + e);
 		}
 
 		JForum.getContext().put("moduleAction", "message.htm");
 		JForum.getContext().put("message", I18n.getMessage(
 										"PasswordRecovery.emailSent",
 										new String[] { 
 												JForum.getRequest().getContextPath()
 												+ "/user/login"
 												+ SystemGlobals.getValue(ConfigKeys.SERVLET_EXTENSION) 
 											}));
 	}
 
 	// Recover user password ( aka, ask him a new one )
 	public void recoverPassword() throws Exception 
 	{
 		String hash = JForum.getRequest().getParameter("hash");
 
 		JForum.getContext().put("moduleAction", "recover_password.htm");
 		JForum.getContext().put("recoverHash", hash);
 	}
 
 	public void recoverPasswordValidate() throws Exception 
 	{
 		String hash = JForum.getRequest().getParameter("recoverHash");
 		String email = JForum.getRequest().getParameter("email");
 
 		String message = "";
 
 		boolean isOk = DataAccessDriver.getInstance().newUserModel().validateLostPasswordHash(email, hash);
 		if (isOk) {
 			String password = JForum.getRequest().getParameter("newPassword");
 			DataAccessDriver.getInstance().newUserModel().saveNewPassword(MD5.crypt(password), email);
 
 			message = I18n.getMessage("PasswordRecovery.ok",
 					new String[] { JForum.getRequest().getContextPath()
 							+ "/user/login"
 							+ SystemGlobals.getValue(ConfigKeys.SERVLET_EXTENSION) });
 		} 
 		else {
 			message = I18n.getMessage("PasswordRecovery.invalidData");
 		}
 
 		JForum.getContext().put("moduleAction", "message.htm");
 		JForum.getContext().put("message", message);
 	}
 
 	public void list() throws Exception 
 	{
 		this.login();
 	}
 }
