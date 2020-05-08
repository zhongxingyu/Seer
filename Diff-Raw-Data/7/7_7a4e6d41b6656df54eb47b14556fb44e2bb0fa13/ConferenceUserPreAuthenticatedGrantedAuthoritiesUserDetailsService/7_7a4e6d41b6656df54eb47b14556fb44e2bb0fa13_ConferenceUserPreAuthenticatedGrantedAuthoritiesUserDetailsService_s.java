 package org.jasig.portlet.blackboardvcportlet.security;
 
 import java.util.Collection;
 import java.util.Collections;
 import java.util.List;
 import java.util.Map;
 
 import org.apache.commons.lang.StringUtils;
 import org.jasig.portlet.blackboardvcportlet.dao.ConferenceUserDao;
 import org.jasig.portlet.blackboardvcportlet.data.ConferenceUser;
 import org.jasig.springframework.security.portlet.authentication.PortletAuthenticationDetails;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.security.core.Authentication;
 import org.springframework.security.core.GrantedAuthority;
 import org.springframework.security.core.userdetails.UserDetails;
 import org.springframework.security.web.authentication.preauth.PreAuthenticatedGrantedAuthoritiesUserDetailsService;
 import org.springframework.transaction.TransactionStatus;
 import org.springframework.transaction.support.TransactionCallback;
 import org.springframework.transaction.support.TransactionOperations;
 
 import com.google.common.collect.ImmutableList;
 
 
 public class ConferenceUserPreAuthenticatedGrantedAuthoritiesUserDetailsService extends
         PreAuthenticatedGrantedAuthoritiesUserDetailsService {
     
     private ConferenceUserDao conferenceUserDao;
     private TransactionOperations transactionOperations;
     private List<String> emailAttributeName = ImmutableList.of("mail");
     private List<String> displayNameAttributeName = ImmutableList.of("displayName");
     private List<String> uniqueUserAttributeNames = Collections.emptyList();
 
     @Autowired
     public void setConferenceUserDao(ConferenceUserDao conferenceUserDao) {
         this.conferenceUserDao = conferenceUserDao;
     }
 
     @Autowired
     public void setTransactionOperations(TransactionOperations transactionOperations) {
         this.transactionOperations = transactionOperations;
     }
 
     public void setEmailAttributeName(List<String> emailAttributeName) {
         this.emailAttributeName = emailAttributeName;
     }
 
     public void setDisplayNameAttributeName(List<String> displayNameAttributeName) {
         this.displayNameAttributeName = displayNameAttributeName;
     }
 
     public void setUniqueUserAttributeNames(List<String> uniqueUserAttributeNames) {
         this.uniqueUserAttributeNames = uniqueUserAttributeNames;
     }
 
     @Override
     protected UserDetails createuserDetails(Authentication token, Collection<? extends GrantedAuthority> authorities) {
         final PortletAuthenticationDetails authenticationDetails = (PortletAuthenticationDetails)token.getDetails();
         
         final ConferenceUser conferenceUser = this.setupConferenceUser(authenticationDetails);
         
         return new ConferenceSecurityUser(token.getName(), conferenceUser, authorities);
     }
 
     protected ConferenceUser setupConferenceUser(final PortletAuthenticationDetails authenticationDetails) {
         final String mail = getAttribute(authenticationDetails, this.emailAttributeName, true);
         final String displayName = getAttribute(authenticationDetails, this.displayNameAttributeName, false);
         
         return this.transactionOperations.execute(new TransactionCallback<ConferenceUser>() {
             @Override
             public ConferenceUser doInTransaction(TransactionStatus status) {
                 ConferenceUser user = conferenceUserDao.getUser(mail);
                 if (user == null) {
                     user = conferenceUserDao.createUser(mail, displayName);
                 }
                 
                 boolean modified = false;
                 
                 //Update with current display name
                 if (!StringUtils.equals(user.getDisplayName(), displayName)) {
                     modified = true;
                     user.setDisplayName(displayName);
                 }
                 
                 //Update all key user attributes
                 final Map<String, String> userInfo = authenticationDetails.getUserInfo();
                 final Map<String, String> userAttributes = user.getAttributes();
                 for (final String keyUserAttributeName : uniqueUserAttributeNames) {
                     final String value = userInfo.get(keyUserAttributeName);
                     if (StringUtils.isNotEmpty(value)) {
                         final String prevValue = userAttributes.put(keyUserAttributeName, value);
                         modified |= StringUtils.equals(prevValue, value);
                     }
                     else {
                         final String prevValue = userAttributes.remove(keyUserAttributeName);
                         modified |= prevValue != null;
                     }
                 }
                 
                 //Remove any attributes that are not part of the defined set
                 modified |= userAttributes.keySet().retainAll(uniqueUserAttributeNames);
                 
                 if (modified) {
                     //Persist modification
                     conferenceUserDao.updateUser(user);
                 }
                 
                 return user;
             }
         });
     }
     
     private String getAttribute(PortletAuthenticationDetails authenticationDetails, List<String> attributeNames, boolean required) {
         final Map<String, String> userInfo = authenticationDetails.getUserInfo();
         
         for (final String mailAttributeName : attributeNames) {
             final String value = userInfo.get(mailAttributeName);
             if (value != null) {
                 return value;
             }
         }
         
         if (required) {
             throw new IllegalStateException("Could not find required user attribute value in attributes: " + attributeNames);
         }
         
         return null;
     }
 }
