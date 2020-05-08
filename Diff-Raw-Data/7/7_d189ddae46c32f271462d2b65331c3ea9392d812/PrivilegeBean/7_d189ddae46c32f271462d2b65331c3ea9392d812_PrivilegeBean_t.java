 package com.sun.identity.admin.model;
 
 import com.sun.identity.entitlement.Entitlement;
 import com.sun.identity.entitlement.EntitlementCondition;
 import com.sun.identity.entitlement.EntitlementSubject;
 import com.sun.identity.entitlement.Privilege;
 import com.sun.identity.entitlement.ResourceAttributes;
 import com.sun.identity.entitlement.opensso.OpenSSOPrivilege;
 import java.io.Serializable;
 import java.util.Set;
 
 public class PrivilegeBean implements Serializable {
 
     private String name = "myPolicy" + System.currentTimeMillis();
     private String description = null;
     private ViewEntitlement viewEntitlement = new ViewEntitlement();
     private ViewCondition viewCondition = null;
     private ViewSubject viewSubject = null;
 
     public PrivilegeBean() {
         // empty
     }
 
     public PrivilegeBean(Privilege p) {
         name = p.getName();
         description = null; // TODO
 
     // TODO: populate other fields from privilege
     }
 
     public String getName() {
         return name;
     }
 
     public void setName(String name) {
         this.name = name;
     }
 
     public String getDescription() {
         return description;
     }
 
     public void setDescription(String description) {
         this.description = description;
     }
 
     public Privilege toPrivilege() {
         // subjects
         EntitlementSubject eSubject = null;
         if (viewSubject != null) {
             eSubject = viewSubject.getEntitlementSubject();
         }
 
         // resources / actions
         Entitlement entitlement = viewEntitlement.getEntitlement();
 
         // conditions
         EntitlementCondition condition = null;
         if (getViewCondition() != null) {
             condition = getViewCondition().getEntitlementCondition();
         }
 
         // resource attrs
         // TODO
         Set<ResourceAttributes> attrs = null;
 
 
         Privilege privilege = new OpenSSOPrivilege(
                 name,
                 entitlement,
                 eSubject,
                 condition,
                 attrs);
 
         return privilege;
     }
 
     public ViewCondition getViewCondition() {
         return viewCondition;
     }
 
     public void setViewCondition(ViewCondition viewCondition) {
         this.viewCondition = viewCondition;
     }
 
     public ViewSubject getViewSubject() {
         return viewSubject;
     }
 
     public void setViewSubject(ViewSubject viewSubject) {
         this.viewSubject = viewSubject;
     }
 
     public ViewEntitlement getViewEntitlement() {
         return viewEntitlement;
     }
 
     public String getViewSubjectAsString() {
        if (viewSubject == null) {
            return "";
        }
         return viewSubject.toString();
     }
 
     public String getViewConditionAsString() {
        if (viewCondition == null) {
            return "";
        }
         return viewCondition.toString();
     }
 }
