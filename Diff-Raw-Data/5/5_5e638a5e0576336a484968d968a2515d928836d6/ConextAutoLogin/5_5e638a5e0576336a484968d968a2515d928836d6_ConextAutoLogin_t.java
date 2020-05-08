 package nl.proteon.liferay.surfnet.security.auth;
 
 import java.util.List;
 import java.util.Locale;
 
 import javax.portlet.PortletPreferences;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import nl.proteon.liferay.surfnet.security.opensocial.OpenSocialGroupLocalServiceUtil;
 import nl.proteon.liferay.surfnet.security.opensocial.model.OpenSocialGroup;
 
 import com.liferay.counter.service.CounterLocalServiceUtil;
 import com.liferay.portal.kernel.exception.PortalException;
 import com.liferay.portal.kernel.exception.SystemException;
 import com.liferay.portal.kernel.log.Log;
 import com.liferay.portal.kernel.log.LogFactoryUtil;
 import com.liferay.portal.kernel.util.DateUtil;
 import com.liferay.portal.kernel.util.LocaleUtil;
 import com.liferay.portal.kernel.util.StringPool;
 import com.liferay.portal.kernel.util.StringUtil;
 import com.liferay.portal.model.Group;
 import com.liferay.portal.model.Layout;
 import com.liferay.portal.model.LayoutConstants;
 import com.liferay.portal.model.LayoutTypePortlet;
 import com.liferay.portal.model.Role;
 import com.liferay.portal.model.User;
 import com.liferay.portal.security.auth.AutoLogin;
 import com.liferay.portal.security.auth.AutoLoginException;
 import com.liferay.portal.service.GroupLocalServiceUtil;
 import com.liferay.portal.service.LayoutLocalServiceUtil;
 import com.liferay.portal.service.PortletPreferencesLocalServiceUtil;
 import com.liferay.portal.service.RoleLocalServiceUtil;
 import com.liferay.portal.service.ServiceContext;
 import com.liferay.portal.service.UserGroupRoleLocalServiceUtil;
 import com.liferay.portal.service.UserLocalServiceUtil;
 import com.liferay.portal.util.PortalUtil;
 import com.liferay.portal.util.PortletKeys;
 import com.liferay.util.portlet.PortletProps;
 
 public class ConextAutoLogin implements AutoLogin {
 
 	@Override
 	public String[] login(HttpServletRequest request, HttpServletResponse response)
 			throws AutoLoginException {
 
 		String[] credentials = null;
 		
 		try {
 			long companyId = PortalUtil.getCompanyId(request);
 			
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
 				
 				UserLocalServiceUtil.updateUser(user);
 				
 			} else {
 				user = addUser(companyId, screenName, emailAddress, openId, firstName, middleName, lastName);				
 			}
 			
 			List<OpenSocialGroup> openSocialGroups = OpenSocialGroupLocalServiceUtil.getOpenSocialGroups(user.getUserId());
 
 			for(OpenSocialGroup openSocialGroup : openSocialGroups) {
 				Group group = null;
 				
 				group = getGroup(companyId, openSocialGroup.getTitle());
 				
 				if(group==null) {
 					
					group = addGroup(
 							user.getUserId(),
 							companyId, 
 							openSocialGroup.getTitle(),
 							openSocialGroup.getDescription(),
							"/" + openSocialGroup.getId().replace("urn-collab-group-surfteams.nl", "").replace("-", ":")
 							);
 				
 					Layout layout = LayoutLocalServiceUtil.addLayout(user.getUserId(), group.getGroupId(), true, 
 							-1, "our_page", "our_title", "", LayoutConstants.TYPE_PORTLET, false, 
 							"", new ServiceContext());
 					
 					LayoutTypePortlet layoutTypePortlet = (LayoutTypePortlet) layout.getLayoutType();
 					layoutTypePortlet.setLayoutTemplateId(user.getUserId(), "1_column");
 					String documentLibraryPortletId = layoutTypePortlet.addPortletId(user.getUserId(), PortletKeys.DOCUMENT_LIBRARY, "column-1", -1, false);
 				    
 				    long ownerId = PortletKeys.PREFS_OWNER_ID_DEFAULT;
 				    int ownerType = PortletKeys.PREFS_OWNER_TYPE_LAYOUT;
 				    
 				    PortletPreferences prefs = PortletPreferencesLocalServiceUtil.getPreferences(companyId, 
 				    		ownerId, ownerType, layout.getPlid(), documentLibraryPortletId);
 				    
 				    prefs.setValue("portletSetupShowBorders", "false");
 				    
 				    PortletPreferencesLocalServiceUtil.updatePreferences(ownerId, ownerType, layout
                             .getPlid(), documentLibraryPortletId, prefs);
 				    
 				    layout = LayoutLocalServiceUtil.updateLayout(layout.getGroupId(),
 				    		layout.isPrivateLayout(), layout.getLayoutId(),
 				    		layout.getTypeSettings());
 					
 				} else {
 					group = updateGroup(companyId, group.getGroupId(), openSocialGroup.getDescription());
 				}
 				Role role = RoleLocalServiceUtil.getRole(companyId, "Site Member");
 				
 				UserGroupRoleLocalServiceUtil.addUserGroupRoles(
 						user.getUserId(), 
 						group.getGroupId(), 
 						new long[] { role.getRoleId() });
 			}
 			
 			credentials = new String[3];
 
 			credentials[0] = String.valueOf(user.getUserId());
 			credentials[1] = user.getPassword();
 			credentials[2] = Boolean.TRUE.toString();
 		}
 		catch (Exception e) {
 			_log.error(e, e);
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
 		String password1 = "ASDF7890";
 		String password2 = "ASDF7890";
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
 		} catch (PortalException e) {
 			_log.error(e,e);
 		} catch (SystemException e) {
 			_log.error(e,e);
 		}
 		
 		return user;
 	}
 	
 	public Group getGroup(long companyId, String name) {
 		Group group = null;
 		
 		try {
 			group = GroupLocalServiceUtil.getGroup(companyId, name);
 		} catch (Exception e) {
 		}
 		return group;
 	}
 	
 	public Group addGroup(long userId, long companyId, String name, String description, String friendlyURL) {
 		Group group = null;
 		
 		try {
 			String className = Group.class.getName();
 			long classPK = CounterLocalServiceUtil.increment(className);
 			int type = 3;
 			boolean site = true;
 			boolean active = true;
 		
 			ServiceContext serviceContext = new ServiceContext();		
 
 			group = GroupLocalServiceUtil.addGroup(userId, className, classPK, name, 
 					description, type, friendlyURL, site, active, serviceContext);
 		} catch (PortalException e) {
 			_log.error(e,e);
 		} catch (SystemException e) {
 			_log.error(e,e);
 		}
 		return group;
 	}
 	
 	public Group updateGroup(long companyId, long groupId, String description) {
 		Group group = null;
 		
 		try {
 			group = GroupLocalServiceUtil.getGroup(groupId);
 
 			group.setDescription(description);
 			
 			group = GroupLocalServiceUtil.updateGroup(group);
 		} catch (SystemException e) {
 			_log.error(e,e);
 		} catch (PortalException e) {
 			_log.error(e,e);
 		}
 		return group;
 	}
 	
 	private static Log _log = LogFactoryUtil.getLog(ConextAutoLogin.class);
 }
