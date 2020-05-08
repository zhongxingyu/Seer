 /*
  * To change this license header, choose License Headers in Project Properties.
  * To change this template file, choose Tools | Templates
  * and open the template in the editor.
  */
 package net.rim.cso.picketlinktest;
 
 import javax.annotation.PostConstruct;
 import javax.ejb.Singleton;
 import javax.ejb.Startup;
 import javax.inject.Inject;
 import net.rim.cso.picketlinktest.model.LdapUser;
 import org.picketlink.idm.IdentityManagementException;
 import org.picketlink.idm.IdentityManager;
 import org.picketlink.idm.PartitionManager;
 import org.picketlink.idm.RelationshipManager;
 import org.picketlink.idm.model.basic.Role;
 
 /**
  *
  * @author jpicklyk
  */
 @Startup
 @Singleton
 public class IDMInitializer {
 
     @Inject
     private PartitionManager partitionManager;
 
     @PostConstruct
     public void init() {
         IdentityManager im = this.partitionManager.createIdentityManager();
        //initializeRole("jpicklyk", ApplicationRole.ADMINISTRATOR);
 
     }
/*
     private void initializeRole(String user, ApplicationRole roleName) {
         try {
             IdentityManager im = this.partitionManager.createIdentityManager();
 
             LdapUser ldapUser = HybridModel.getLdapUser(im, user);
             Role role = HybridModel.getRole(im, roleName.name());
             if (role == null) {
                 role = new Role(roleName.name());
                 im.add(role);
             }
 
             RelationshipManager relationshipManager = this.partitionManager.createRelationshipManager();
 
             if (!HybridModel.hasRole(relationshipManager, ldapUser, role)) {
                 HybridModel.grantRole(relationshipManager, ldapUser, role);
             }
 
         } catch (IdentityManagementException ex) {
             ex.printStackTrace();
         }
     }
    */
 }
