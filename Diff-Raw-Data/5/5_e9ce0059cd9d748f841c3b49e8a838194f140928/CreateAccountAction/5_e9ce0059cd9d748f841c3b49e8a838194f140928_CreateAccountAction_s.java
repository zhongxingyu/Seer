 /**
  * Copyright (c) 2000-2010 Liferay, Inc. All rights reserved.
  *
  * This library is free software; you can redistribute it and/or modify it under
  * the terms of the GNU Lesser General Public License as published by the Free
  * Software Foundation; either version 2.1 of the License, or (at your option)
  * any later version.
  *
  * This library is distributed in the hope that it will be useful, but WITHOUT
  * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
  * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
  * details.
  */
 package com.liferay.portlet.login.action;
 
 import com.liferay.util.mail.MailEngine;
 import com.liferay.portal.AddressCityException;
 import com.liferay.portal.AddressStreetException;
 import com.liferay.portal.AddressZipException;
 import com.liferay.portal.ContactFirstNameException;
 import com.liferay.portal.ContactFullNameException;
 import com.liferay.portal.ContactLastNameException;
 import com.liferay.portal.DuplicateUserEmailAddressException;
 import com.liferay.portal.DuplicateUserScreenNameException;
 import com.liferay.portal.EmailAddressException;
 import com.liferay.portal.NoSuchCountryException;
 import com.liferay.portal.NoSuchLayoutException;
 import com.liferay.portal.NoSuchListTypeException;
 import com.liferay.portal.NoSuchOrganizationException;
 import com.liferay.portal.NoSuchRegionException;
 import com.liferay.portal.OrganizationParentException;
 import com.liferay.portal.PhoneNumberException;
 import com.liferay.portal.RequiredFieldException;
 import com.liferay.portal.RequiredUserException;
 import com.liferay.portal.ReservedUserEmailAddressException;
 import com.liferay.portal.ReservedUserScreenNameException;
 import com.liferay.portal.TermsOfUseException;
 import com.liferay.portal.UserEmailAddressException;
 import com.liferay.portal.UserIdException;
 import com.liferay.portal.UserPasswordException;
 import com.liferay.portal.UserScreenNameException;
 import com.liferay.portal.UserSmsException;
 import com.liferay.portal.WebsiteURLException;
 import com.liferay.portal.kernel.captcha.CaptchaTextException;
 import com.liferay.portal.kernel.captcha.CaptchaUtil;
 import com.liferay.portal.kernel.servlet.SessionErrors;
 import com.liferay.portal.kernel.servlet.SessionMessages;
 import com.liferay.portal.kernel.util.Constants;
 import com.liferay.portal.kernel.util.ParamUtil;
 import com.liferay.portal.kernel.util.Validator;
 import com.liferay.portal.model.Company;
 import com.liferay.portal.model.CompanyConstants;
 import com.liferay.portal.model.Layout;
 import com.liferay.portal.model.User;
 import com.liferay.portal.model.Group;
 import com.liferay.portal.security.auth.PrincipalException;
 import com.liferay.portal.service.LayoutLocalServiceUtil;
 import com.liferay.portal.service.ServiceContext;
 import com.liferay.portal.service.ServiceContextFactory;
 import com.liferay.portal.service.UserLocalServiceUtil;
 import com.liferay.portal.service.UserServiceUtil;
 import com.liferay.portal.service.GroupLocalServiceUtil;
 import com.liferay.portal.struts.PortletAction;
 import com.liferay.portal.theme.ThemeDisplay;
 import com.liferay.portal.util.PortalUtil;
 import com.liferay.portal.util.PropsValues;
 import com.liferay.portal.util.WebKeys;
 import com.liferay.portlet.login.util.LoginUtil;
 
 import javax.portlet.ActionRequest;
 import javax.portlet.ActionResponse;
 import javax.portlet.PortletConfig;
 import javax.portlet.PortletURL;
 import javax.portlet.RenderRequest;
 import javax.portlet.RenderResponse;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import javax.servlet.http.HttpSession;
 
 import org.apache.struts.action.ActionForm;
 import org.apache.struts.action.ActionForward;
 import org.apache.struts.action.ActionMapping;
 
 /**
  * <a href="CreateAccountAction.java.html"><b><i>View Source</i></b></a>
  *
  * @author Brian Wing Shun Chan
  * @author Amos Fong
  */
 public class CreateAccountAction extends PortletAction {
 
     public void processAction(
             ActionMapping mapping, ActionForm form, PortletConfig portletConfig,
             ActionRequest actionRequest, ActionResponse actionResponse)
             throws Exception {
 
         String cmd = ParamUtil.getString(actionRequest, Constants.CMD);
 
         try {
             if (cmd.equals(Constants.ADD)) {
                 addUser(actionRequest, actionResponse);
             }
         } catch (Exception e) {
             if (e instanceof AddressCityException
                     || e instanceof AddressStreetException
                     || e instanceof AddressZipException
                     || e instanceof CaptchaTextException
                     || e instanceof ContactFirstNameException
                     || e instanceof ContactFullNameException
                     || e instanceof ContactLastNameException
                     || e instanceof DuplicateUserEmailAddressException
                     || e instanceof DuplicateUserScreenNameException
                     || e instanceof EmailAddressException
                     || e instanceof NoSuchCountryException
                     || e instanceof NoSuchListTypeException
                     || e instanceof NoSuchOrganizationException
                     || e instanceof NoSuchRegionException
                     || e instanceof OrganizationParentException
                     || e instanceof PhoneNumberException
                     || e instanceof RequiredFieldException
                     || e instanceof RequiredUserException
                     || e instanceof ReservedUserEmailAddressException
                     || e instanceof ReservedUserScreenNameException
                     || e instanceof TermsOfUseException
                     || e instanceof UserEmailAddressException
                     || e instanceof UserIdException
                     || e instanceof UserPasswordException
                     || e instanceof UserScreenNameException
                     || e instanceof UserSmsException
                     || e instanceof WebsiteURLException) {
 
                 SessionErrors.add(actionRequest, e.getClass().getName(), e);
             } else {
                 throw e;
             }
         }
 
         if (Validator.isNull(PropsValues.COMPANY_SECURITY_STRANGERS_URL)) {
             return;
         }
 
         ThemeDisplay themeDisplay = (ThemeDisplay) actionRequest.getAttribute(
                 WebKeys.THEME_DISPLAY);
 
         try {
             Layout layout = LayoutLocalServiceUtil.getFriendlyURLLayout(
                     themeDisplay.getScopeGroupId(), false,
                     PropsValues.COMPANY_SECURITY_STRANGERS_URL);
 
             String redirect = PortalUtil.getLayoutURL(layout, themeDisplay);
 
             sendRedirect(actionRequest, actionResponse, redirect);
         } catch (NoSuchLayoutException nsle) {
         }
     }
 
     public ActionForward render(
             ActionMapping mapping, ActionForm form, PortletConfig portletConfig,
             RenderRequest renderRequest, RenderResponse renderResponse)
             throws Exception {
 
         Company company = PortalUtil.getCompany(renderRequest);
 
         if (!company.isStrangers()) {
             throw new PrincipalException();
         }
 
         ThemeDisplay themeDisplay = (ThemeDisplay) renderRequest.getAttribute(
                 WebKeys.THEME_DISPLAY);
 
         renderResponse.setTitle(themeDisplay.translate("create-account"));
 
         return mapping.findForward("portlet.login.create_account");
     }
 
     protected void addUser(
             ActionRequest actionRequest, ActionResponse actionResponse)
             throws Exception {
 
         HttpServletRequest request = PortalUtil.getHttpServletRequest(
                 actionRequest);
         HttpSession session = request.getSession();
 
         ThemeDisplay themeDisplay = (ThemeDisplay) actionRequest.getAttribute(
                 WebKeys.THEME_DISPLAY);
 
         Company company = themeDisplay.getCompany();
 
         boolean autoPassword = true;
         String password1 = null;
         String password2 = null;
         boolean autoScreenName = isAutoScreenName();
         String screenName = ParamUtil.getString(actionRequest, "screenName");
         String emailAddress = ParamUtil.getString(
                 actionRequest, "emailAddress");
         String openId = ParamUtil.getString(actionRequest, "openId");
         String firstName = ParamUtil.getString(actionRequest, "firstName");
         String middleName = ParamUtil.getString(actionRequest, "middleName");
         String lastName = ParamUtil.getString(actionRequest, "lastName");
         int prefixId = ParamUtil.getInteger(actionRequest, "prefixId");
         int suffixId = ParamUtil.getInteger(actionRequest, "suffixId");
         boolean male = ParamUtil.get(actionRequest, "male", true);
         int birthdayMonth = ParamUtil.getInteger(
                 actionRequest, "birthdayMonth");
         int birthdayDay = ParamUtil.getInteger(actionRequest, "birthdayDay");
         int birthdayYear = ParamUtil.getInteger(actionRequest, "birthdayYear");
         String jobTitle = ParamUtil.getString(actionRequest, "jobTitle");
         long[] groupIds = null;
         long[] organizationIds = null;
         long[] roleIds = null;
         long[] userGroupIds = null;
         boolean sendEmail = false;
 
         ServiceContext serviceContext = ServiceContextFactory.getInstance(
                 User.class.getName(), actionRequest);
 
         if (PropsValues.LOGIN_CREATE_ACCOUNT_ALLOW_CUSTOM_PASSWORD) {
             autoPassword = false;
 
             password1 = ParamUtil.getString(actionRequest, "password1");
             password2 = ParamUtil.getString(actionRequest, "password2");
         }
 
         boolean openIdPending = false;
 
         Boolean openIdLoginPending = (Boolean) session.getAttribute(
                 WebKeys.OPEN_ID_LOGIN_PENDING);
 
         if ((openIdLoginPending != null)
                 && (openIdLoginPending.booleanValue())
                 && (Validator.isNotNull(openId))) {
 
             sendEmail = false;
             openIdPending = true;
         }
 
         if (PropsValues.CAPTCHA_CHECK_PORTAL_CREATE_ACCOUNT) {
             CaptchaUtil.check(actionRequest);
         }
     
         long mockFacebookId = 0;
         User user = UserServiceUtil.addUser(
                 company.getCompanyId(), autoPassword, password1, password2,
                 autoScreenName, screenName, emailAddress, mockFacebookId, openId,
                 themeDisplay.getLocale(),firstName, middleName, lastName, prefixId,
                 suffixId, male, birthdayMonth, birthdayDay, birthdayYear, jobTitle,
                 groupIds, organizationIds, roleIds, userGroupIds, sendEmail,
                 serviceContext);
                 
         user = UserLocalServiceUtil.updateActive(user.getUserId(), false);
 
         String from = "math.app.fpm@gmail.com";
        String to = "artem@fdevelopers.com";
 
         String subject="New user has been registered!";
 
         String userEmail = user.getEmailAddress();
         userEmail = userEmail.replace("@", "%40");
         StringBuilder body = new StringBuilder("\n");
         body.append(user.getUserId()).append("  ").append(user.getFullName());
         user.setLanguageId("uk");
         //add user to selected group
         int role = ParamUtil.getInteger(actionRequest, "roleSelect");        
         long users[] = new long[1];
         users[0] = user.getUserId();
         if (role == 0) {
             long groupId = ParamUtil.getLong(actionRequest, "student-groups");
             UserLocalServiceUtil.addRoleUsers(10505,users);
             UserLocalServiceUtil.addUserGroupUsers(19334,users);
             
             String groupRequest = ParamUtil.getString(actionRequest, "studentGroupRequest");
             if (groupRequest.isEmpty()) {
               UserLocalServiceUtil.addGroupUsers(groupId,users);
               Group group = GroupLocalServiceUtil.getGroup(groupId);
               body.append(" has registered in as a student of the group  ").append(group.getDescriptiveName());
             } else {
              //String newGroupName = user.getExpandoBridge().getAttribute("student-new-group-request").toString();
               body.append(" has registered in as a student, requested the NEW group ").append(groupRequest);
               subject += " (+ new group request)";
             }
             
             boolean isMemberCouncil = ParamUtil.getBoolean(actionRequest, "is-member-student-council");
             if (isMemberCouncil) {
                 //UserLocalServiceUtil.addGroupUsers( ,users);
                 UserLocalServiceUtil.addRoleUsers(10506,users);
                 body.append(" and member of the Student Council");
             }
         } else {
             UserLocalServiceUtil.addRoleUsers(15708,users);
             UserLocalServiceUtil.addUserGroupUsers(20001,users);
             body.append(" has registered in as an employer");
         }
         body.append("\n").append("http://primat.dp.ua/en/group/control_panel/manage?p_p_id=125&p_p_state=maximized&_125_advancedSearch=true&_125_andOperator=1")
                 .append("&_125_emailAddress=").append(userEmail).append("&_125_active=0");
         MailEngine.send(from, to, subject, body.toString());
         
         if (openIdPending) {
             session.setAttribute(
                     WebKeys.OPEN_ID_LOGIN, new Long(user.getUserId()));
 
             session.removeAttribute(WebKeys.OPEN_ID_LOGIN_PENDING);
         } else {
 
             // Session messages
 
             SessionMessages.add(request, "user_added", user.getEmailAddress());
             SessionMessages.add(
                     request, "user_added_password", user.getPasswordUnencrypted());
         }
 
         // Send redirect
 
         String login = null;
 
         if (company.getAuthType().equals(CompanyConstants.AUTH_TYPE_ID)) {
             login = String.valueOf(user.getUserId());
         } else if (company.getAuthType().equals(CompanyConstants.AUTH_TYPE_SN)) {
             login = user.getScreenName();
         } else {
             login = user.getEmailAddress();
         }
 
         sendRedirect(
                 actionRequest, actionResponse, themeDisplay, login,
                 user.getPasswordUnencrypted());
     }
 
     protected boolean isAutoScreenName() {
         return _AUTO_SCREEN_NAME;
     }
 
     protected void sendRedirect(
             ActionRequest actionRequest, ActionResponse actionResponse,
             ThemeDisplay themeDisplay, String login, String password)
             throws Exception {
 
         HttpServletRequest request = PortalUtil.getHttpServletRequest(
                 actionRequest);
 
         String redirect = PortalUtil.escapeRedirect(
                 ParamUtil.getString(actionRequest, "redirect"));
 
         if (Validator.isNotNull(redirect)) {
             HttpServletResponse response = PortalUtil.getHttpServletResponse(
                     actionResponse);
 
             LoginUtil.login(request, response, login, password, false, null);
         } else {
             PortletURL loginURL = LoginUtil.getLoginURL(
                     request, themeDisplay.getPlid());
 
             loginURL.setParameter("login", login);
 
             redirect = loginURL.toString();
         }
 
         actionResponse.sendRedirect(redirect);
     }
 
     protected boolean isCheckMethodOnProcessAction() {
         return _CHECK_METHOD_ON_PROCESS_ACTION;
     }
     private static final boolean _AUTO_SCREEN_NAME = false;
     private static final boolean _CHECK_METHOD_ON_PROCESS_ACTION = false;
 }
