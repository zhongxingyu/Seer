 package nl.proteon.liferay.surfnet.security.auth;
 
 import java.util.Locale;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import com.liferay.portal.kernel.exception.PortalException;
 import com.liferay.portal.kernel.exception.SystemException;
 import com.liferay.portal.kernel.log.Log;
 import com.liferay.portal.kernel.log.LogFactoryUtil;
 import com.liferay.portal.kernel.util.DateUtil;
 import com.liferay.portal.kernel.util.LocaleUtil;
 import com.liferay.portal.kernel.util.StringPool;
 import com.liferay.portal.kernel.util.StringUtil;
 import com.liferay.portal.model.PasswordPolicy;
 import com.liferay.portal.model.User;
 import com.liferay.portal.security.auth.AutoLogin;
 import com.liferay.portal.security.auth.AutoLoginException;
 import com.liferay.portal.service.PasswordPolicyLocalServiceUtil;
 import com.liferay.portal.service.ServiceContext;
 import com.liferay.portal.service.UserLocalServiceUtil;
 import com.liferay.portal.util.PortalUtil;
 import com.liferay.util.portlet.PortletProps;
 
 public class ConextAutoLogin implements AutoLogin {
 
 	@Override
 	public String[] login(HttpServletRequest request, HttpServletResponse response)
 			throws AutoLoginException {
 
 		String[] credentials = null;
 		
 		if(request.getHeader(PortletProps.get("saml2.header.mapping.email")) != null) {
 		
 			try {
 				long companyId = PortalUtil.getCompanyId(request);
 				
 				PasswordPolicy passwordPolicy = PasswordPolicyLocalServiceUtil.getDefaultPasswordPolicy(companyId);
 				
 				if(passwordPolicy.getChangeRequired() || passwordPolicy.getChangeable()) {
 					_log.debug("Setting password policy password change required to: false");
 					passwordPolicy.setChangeRequired(false);
 					passwordPolicy.setChangeable(false);
 					PasswordPolicyLocalServiceUtil.updatePasswordPolicy(passwordPolicy);
 				}
 				
 				String emailAddress = StringPool.BLANK;
 				String firstName = StringPool.BLANK;
 				String lastName = StringPool.BLANK;
 				String middleName = StringPool.BLANK;
 				String screenName = StringPool.BLANK;
 				String openId = StringPool.BLANK;
 				
 				User user = null;
 				
 				if(!(request.getHeader(PortletProps.get("saml2.header.mapping.email")).equals(""))) {
 					emailAddress = request.getHeader(PortletProps.get("saml2.header.mapping.email"));
 				}
 				if(!(request.getHeader(PortletProps.get("saml2.header.mapping.screenname")).equals(""))) {
 					screenName = request.getHeader(PortletProps.get("saml2.header.mapping.screenname"));
 					screenName = StringUtil.replace(
 							screenName,
 							new String[] {StringPool.SLASH, StringPool.UNDERLINE, StringPool.SPACE},
 							new String[] {StringPool.PERIOD, StringPool.PERIOD, StringPool.PERIOD});
 				}
 				if(!(request.getHeader(PortletProps.get("saml2.header.mapping.id")).equals(""))) {
 					openId = request.getHeader(PortletProps.get("saml2.header.mapping.id"));
 				}
 				if(!(request.getHeader(PortletProps.get("saml2.header.mapping.fullname")).equals("")) 
 						&& PortletProps.get("saml2.header.mapping.firstname").equals("")
 						&& PortletProps.get("saml2.header.mapping.middlename").equals("")
 						&& PortletProps.get("saml2.header.mapping.lastname").equals("")) {
 				
 					String fullName = request.getHeader(PortletProps.get("saml2.header.mapping.fullname"));
 					
 					firstName = fullName.substring(0, fullName.indexOf(" "));
 					middleName = "";
 					lastName = fullName.substring(fullName.lastIndexOf(" ")+1);
 				
 				} else {
 					
 					firstName = request.getHeader(PortletProps.get("saml2.header.mapping.firstname"));
 					middleName = request.getHeader(PortletProps.get("saml2.header.mapping.middlename"));
 					lastName = request.getHeader(PortletProps.get("saml2.header.mapping.lastname"));
 				
 				} 
 				
 				firstName = StringUtil.upperCaseFirstLetter(firstName);
 				lastName = StringUtil.upperCaseFirstLetter(lastName);
 				
 				user = getUserByOpenId(companyId, openId);
 				
 				if(!(user==null)) {
 					user.setCompanyId(companyId);
 					user.setCreateDate(DateUtil.newDate());
 					user.setEmailAddress(emailAddress);
 					user.setFirstName(firstName);
 					user.setMiddleName(middleName);
 					user.setLastName(lastName);
 					user.setScreenName(screenName);
					user.setPasswordReset(false);
 					
 					UserLocalServiceUtil.updateUser(user);
 					
 				} else {
 					user = addUser(companyId, screenName, emailAddress, openId, firstName, middleName, lastName);
 				}
 				
 				_log.info("User " + user.getOpenId() + " logged in");
 					
 				credentials = new String[3];
 	
 				credentials[0] = String.valueOf(user.getUserId());
 				credentials[1] = user.getPassword();
 				credentials[2] = Boolean.TRUE.toString();
 			}
 			catch (Exception e) {
 				_log.error(e, e);
 			}
 		}
 		
 		return credentials;
 	}
 
 	public User getUserByOpenId(long companyId, String openId) {
 		User user = null;
 		
 		try {
 			user = UserLocalServiceUtil.getUserByOpenId(companyId, openId);
 		} catch (Exception e) {
 		}
 		return user;
 	}
 	
 	public User addUser(long companyId, String screenName, String emailAddress, 
 			String openId, String firstName, String middleName, String lastName) {
 		
 		User user = null;
 		
 		boolean autoPassword = true;
 		String password1 = "ASDF7890jkl";
 		String password2 = "ASDF7890jkl";
 		boolean autoScreenName = true;
 		long facebookId = 0;
 		int prefixId = -1;
 		int suffixId = -1;
 		boolean male = true;
 		int birthdayMonth = 1;
 		int birthdayDay = 1;
 		int birthdayYear = 1970;
 		long[] groupIds = null;
 		long[] organizationIds = null;
 		long[] roleIds = null;
 		long[] userGroupIds = null;
 		boolean sendEmail = false;
 		long creatorUserId = 0;
 		Locale locale = LocaleUtil.getDefault();
 		String jobTitle = "";
 		
 		ServiceContext serviceContext = new ServiceContext();
 		
 		try {
 			user = UserLocalServiceUtil.addUser(creatorUserId, companyId, autoPassword, password1, 
 					password2, autoScreenName, screenName, emailAddress, facebookId, 
 					openId, locale, firstName, 
 					middleName, lastName, prefixId, suffixId, male, birthdayMonth, birthdayDay, birthdayYear, jobTitle,
 					groupIds, organizationIds, roleIds, userGroupIds, sendEmail, serviceContext);
 			
 			PasswordPolicy passwordPolicy = user.getPasswordPolicy();
 			
 			if(passwordPolicy.getChangeRequired() || passwordPolicy.getChangeable()) {
 				_log.debug("Setting password policy password change required to false for user "+ user.getOpenId());
 				passwordPolicy.setChangeRequired(false);
 				passwordPolicy.setChangeable(false);
 				PasswordPolicyLocalServiceUtil.updatePasswordPolicy(passwordPolicy);
 			}
			
			user.setPasswordReset(false);
 		
 			user = UserLocalServiceUtil.updateUser(user);
 		} catch (PortalException e) {
 			_log.error(e,e);
 		} catch (SystemException e) {
 			_log.error(e,e);
 		}
 		
 		return user;
 	}
 	
 	private static Log _log = LogFactoryUtil.getLog(ConextAutoLogin.class);
 }
