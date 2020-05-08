 /*
  * To change this license header, choose License Headers in Project Properties.
  * To change this template file, choose Tools | Templates
  * and open the template in the editor.
  */
 
 package net.rim.cso.picketlinktest;
 
 import java.util.Properties;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import net.rim.cso.picketlinktest.model.LdapUser;
 import javax.enterprise.context.ApplicationScoped;
 import javax.enterprise.inject.Produces;
 import javax.inject.Inject;
 import javax.naming.InitialContext;
 import javax.naming.NamingException;
 import net.rim.cso.picketlinktest.model.AttributeReferenceTypeEntity;
 import net.rim.cso.picketlinktest.model.RelationshipIdentityTypeReferenceEntity;
 import net.rim.cso.picketlinktest.model.RelationshipTypeEntity;
 import net.rim.cso.picketlinktest.model.RoleTypeEntity;
 
 import org.picketlink.idm.config.IdentityConfiguration;
 import org.picketlink.idm.config.IdentityConfigurationBuilder;
import org.picketlink.idm.model.IdentityType;
 import org.picketlink.idm.model.Relationship;
 import org.picketlink.idm.model.basic.Group;
 import org.picketlink.internal.EEJPAContextInitializer;
 
 /**
  *
  * @author jpicklyk
  */
 @ApplicationScoped
 public class IDMConfiguration {
     
         
     @Inject
     private EEJPAContextInitializer contextInitializer;
     
     Properties ldapProperties;
     /**
      * <p>
      *     We use this method to produce a {@link IdentityConfiguration} configured with a LDAP store.
      * </p>
      *
      * @return
      */
     @Produces
     public IdentityConfiguration configure() {
         InitialContext ic;
         try {
             ic = new InitialContext();
             ldapProperties = (Properties) ic.lookup("ldapProperties");
         } catch (NamingException ex) {
             Logger.getLogger(IDMConfiguration.class.getName()).log(Level.SEVERE, null, ex);
         }
         
         IdentityConfigurationBuilder builder = new IdentityConfigurationBuilder();
         builder
             .named("default")
                 .stores()
                     .jpa()
                         .mappedEntity(
                                 RelationshipTypeEntity.class,
                                 RelationshipIdentityTypeReferenceEntity.class,
                                 RoleTypeEntity.class,
                                 AttributeReferenceTypeEntity.class
                         )
                         .addContextInitializer(contextInitializer)
                         .supportGlobalRelationship(Relationship.class)
                         .supportAttributes(true)
                     .ldap()
                         .baseDN(ldapProperties.getProperty("baseDN"))
                         .bindDN(ldapProperties.getProperty("bindDN"))
                         .bindCredential(ldapProperties.getProperty("bindCredentials"))
                         .url(ldapProperties.getProperty("url"))
                         .activeDirectory(true)
                         .supportCredentials(true)
                        .supportType(IdentityType.class)
                         .addCredentialHandler(LdapUserCredentialHandler.class)
                         
                         .mapping(LdapUser.class)
                             .baseDN(ldapProperties.getProperty("user_dn_suffix"))
                             .objectClasses("person", "organizationalPerson", "user")
                             .attribute("loginName", "sAMAccountName")
                             .attribute("firstName", "givenName")
                             .attribute("lastName", "sn")
                             .attribute("email", "mail")
                             .attribute("fullName", "cn", true)
                             .readOnlyAttribute("createdDate", "whenCreated")                     
                         .mapping(Group.class)
                             .baseDN(ldapProperties.getProperty("group_dn_suffix"))
                             .objectClasses("group")
                             .attribute("name", "cn", true)
                             .readOnlyAttribute("createdDate", "whenCreated")
                             .parentMembershipAttributeName("member");
                         
         
         return builder.build();
     }
 }
