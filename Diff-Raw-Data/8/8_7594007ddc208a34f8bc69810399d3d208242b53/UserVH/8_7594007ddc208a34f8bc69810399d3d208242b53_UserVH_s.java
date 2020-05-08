 /*
  * Copyright (c) 2003, Rafael Steil
  * All rights reserved.
 
  * Redistribution and use in source and binary forms, 
  * with or without modification, are permitted provided 
  * that the following conditions are met:
 
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
 
 import java.util.List;
 
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
 import net.jforum.util.mail.EmailException;
 import net.jforum.util.mail.EmailSenderTask;
 import net.jforum.util.mail.LostPasswordSpammer;
 import net.jforum.util.preferences.ConfigKeys;
 import net.jforum.util.preferences.SystemGlobals;
 
 /**
  * @author Rafael Steil
 * @version $Id: UserVH.java,v 1.15 2004/08/03 14:30:42 pieter2 Exp $
  */
 public class UserVH extends Command 
 {
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
 		ViewCommon.saveUser(userId);
 
 		JForum.setRedirect(JForum.getRequest().getContextPath() +"/user/editDone/"+ userId +".page");
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
 		
		if (username == null || username.equals("") || password == null || password.equals("")) {
 			JForum.getContext().put("error", I18n.getMessage("UsernamePasswordCannotBeNull"));
 			error = true;
 		}
 		
		if (um.isUsernameRegistered(JForum.getRequest().getParameter("username"))) {
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
 		
 		int userId = um.addNew(u);
 		
 		SessionFacade.setAttribute("logged", "1");
 		
 		UserSession userSession = new UserSession();
 		userSession.setAutoLogin(true);
 		userSession.setUserId(userId);
 		userSession.setUsername(u.getUsername());
 		userSession.setLastVisit(System.currentTimeMillis());
 		userSession.setStartTime(System.currentTimeMillis());
 		
 		SessionFacade.add(userSession);
 		
 		// Finalizing.. show to user the congrats page
 		JForum.setRedirect(JForum.getRequest().getContextPath() +"/user/registrationComplete.page");
 	}
 	
 	public void registrationComplete() throws Exception
 	{
 		int userId = SessionFacade.getUserSession().getUserId();
 		
 		String profilePage = JForum.getRequest().getContextPath() +"/user/edit/"+ userId +".page";
 		String homePage = JForum.getRequest().getContextPath() +"/forums/list.page";
 		
 		String message = I18n.getMessage("User.RegistrationCompleteMessage", new Object[] { profilePage, homePage });
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
 				JForum.setRedirect(JForum.getRequest().getContextPath() +"/forums/list.page");
 				
 				SessionFacade.setAttribute("logged", "1");
 				
 				UserSession userSession = SessionFacade.getUserSession();
 				userSession.setUserId(user.getId());
 				userSession.setUsername(JForum.getRequest().getParameter("username"));
 				userSession.setPrivateMessages(user.getPrivateMessagesCount());
 				
 				// Autologin
 				if (JForum.getRequest().getParameter("autologin") != null) {
 					userSession.setAutoLogin(true);
 					JForum.addCookie(SystemGlobals.getValue(ConfigKeys.COOKIE_AUTO_LOGIN), "1");
 				}
 				
 				SessionFacade.add(userSession);
 				
 				JForum.addCookie(SystemGlobals.getValue(ConfigKeys.COOKIE_NAME_DATA), Integer.toString(user.getId()));
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
 				JForum.getContext().put("returnPath", JForum.getRequest().getParameter("returnPath"));
 			}
 		}
 		else if (JForum.getRequest().getParameter("returnPath") != null) {
 			JForum.setRedirect(JForum.getRequest().getParameter("returnPath"));
 		}
 	}
 	
 	private User validateLogin(String name, String password) throws Exception {
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
 		JForum.setRedirect(JForum.getRequest().getContextPath() +"/forums/list.page");
 		
 		// Disable auto login
 		UserSession userSession = SessionFacade.getUserSession();
 		userSession.setAutoLogin(false);
 		userSession.setUserId(SystemGlobals.getIntValue(ConfigKeys.ANONYMOUS_USER_ID));
 
 		SessionFacade.setAttribute("logged", "0");
 		SessionFacade.add(userSession);
 
 		JForum.addCookie(SystemGlobals.getValue(ConfigKeys.COOKIE_AUTO_LOGIN), null);
 		JForum.addCookie(SystemGlobals.getValue(ConfigKeys.COOKIE_NAME_DATA), SystemGlobals.getValue(ConfigKeys.ANONYMOUS_USER_ID));
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
 	
 	// Send lost password email
 	public void lostPasswordSend() throws Exception
 	{
 		String email = JForum.getRequest().getParameter("email");
 		String username = JForum.getRequest().getParameter("username");
 		
 		boolean exists = false;
 		UserModel um = DataAccessDriver.getInstance().newUserModel();
 		
 		if (email != null && !email.equals("")) {
 			exists = (!um.getUsernameByEmail(email).equals(""));
 		}
 		else if (username != null && !username.equals("")) {
 			List l = um.findByName(username, true);
 			if (l.size() > 0) {
 				exists = true;
 				email = ((User)l.get(0)).getEmail();
 			}
 		}
 		
 		if (!exists) {
 			JForum.getContext().put("message", I18n.getMessage("PasswordRecovery.invalidUserEmail"));
 			this.lostPassword();
 			return;
 		}
 		
 		String hash = MD5.crypt(email + System.currentTimeMillis());
 		um.writeLostPasswordHash(email, hash);
 		
 		try {
 			QueuedExecutor.getInstance().execute(new EmailSenderTask(
 				new LostPasswordSpammer(um.getUsernameByEmail(email), email, hash)));
 		}
 		catch (EmailException e) {
 			// I don't care about this error
 		}
 		
 		JForum.getContext().put("moduleAction", "message.htm");
 		JForum.getContext().put("message", I18n.getMessage("PasswordRecovery.emailSent", 
 			new String[] { JForum.getRequest().getContextPath() + "/user/login.page" }));
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
 		
 		String message= "";
 		
 		boolean isOk = DataAccessDriver.getInstance().newUserModel().validateLostPasswordHash(email, hash);
 		if (isOk) {
 			String password = JForum.getRequest().getParameter("newPassword");
 			DataAccessDriver.getInstance().newUserModel().saveNewPassword(MD5.crypt(password), email);
 			
 			message = I18n.getMessage("PasswordRecovery.ok", new String[] { "/user/login.page" });
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
