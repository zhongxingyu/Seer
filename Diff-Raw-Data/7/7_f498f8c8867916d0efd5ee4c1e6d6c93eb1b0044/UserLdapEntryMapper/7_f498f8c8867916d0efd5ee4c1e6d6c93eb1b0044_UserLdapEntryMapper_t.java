 package org.mule.galaxy.security.ldap;
 
 import javax.naming.NamingException;
 import javax.naming.directory.Attribute;
 import javax.naming.directory.Attributes;
 
 import org.acegisecurity.ldap.LdapEntryMapper;
import org.apache.commons.lang.StringUtils;
 import org.mule.galaxy.security.User;
 
 public class UserLdapEntryMapper implements LdapEntryMapper {
 
     private String nameAttribute = "cn";
     private String emailAttribute = "email";
     private String usernameAttribute = "uid";
 
     public Object mapAttributes(String dn, Attributes attributes) throws NamingException {
         User user = new User();
         user.setId(getValueOrNull(attributes, getUsernameAttribute()));
         user.setUsername(user.getId());
         user.setEmail(getValueOrNull(attributes, getEmailAttribute()));
         user.setName(getValueOrNull(attributes, getNameAttribute()));
        
        if (StringUtils.isEmpty(user.getId())) {
            throw new NamingException("The username LDAP attribute value was empty. Is '" + getUsernameAttribute() +
                                      "' really the attribute with the username? Please check your configuration.");
        }
         return user;
     }
 
     private String getValueOrNull(Attributes attributes, String key) {
         Attribute attribute = attributes.get(key);
         
         if (attribute != null) {
             try {
                 return attribute.get().toString();
             } catch (NamingException e) {
                 return null;
             }
         }
         
         return null;
     }
 
     public String getNameAttribute() {
         return nameAttribute;
     }
 
     public void setNameAttribute(String nameAttribute) {
         this.nameAttribute = nameAttribute;
     }
 
     public String getEmailAttribute() {
         return emailAttribute;
     }
 
     public void setEmailAttribute(String emailAttribute) {
         this.emailAttribute = emailAttribute;
     }
 
     public String getUsernameAttribute() {
         return usernameAttribute;
     }
 
     public void setUsernameAttribute(String usernameAttribute) {
         this.usernameAttribute = usernameAttribute;
     }
 
 }
