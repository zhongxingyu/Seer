 package com.sun.identity.admin.model;
 
 import com.icesoft.faces.context.effects.Effect;
 import com.sun.identity.entitlement.Entitlement;
 import com.sun.identity.entitlement.EntitlementCondition;
 import com.sun.identity.entitlement.EntitlementSubject;
 import com.sun.identity.entitlement.Privilege;
 import com.sun.identity.entitlement.ResourceAttributes;
 import com.sun.identity.entitlement.opensso.OpenSSOPrivilege;
 import java.io.Serializable;
 import java.util.Comparator;
 import java.util.Map;
 import java.util.Set;
 
 public class PrivilegeBean implements Serializable {
 
     public Effect getNameCellEffect() {
         return nameCellEffect;
     }
 
     public void setNameCellEffect(Effect nameCellEffect) {
         this.nameCellEffect = nameCellEffect;
     }
 
     public Effect getResourcesCellEffect() {
         return resourcesCellEffect;
     }
 
     public void setResourcesCellEffect(Effect resourcesCellEffect) {
         this.resourcesCellEffect = resourcesCellEffect;
     }
 
     public Effect getSubjectCellEffect() {
         return subjectCellEffect;
     }
 
     public void setSubjectCellEffect(Effect subjectCellEffect) {
         this.subjectCellEffect = subjectCellEffect;
     }
 
     public Effect getConditionCellEffect() {
         return conditionCellEffect;
     }
 
     public void setConditionCellEffect(Effect conditionCellEffect) {
         this.conditionCellEffect = conditionCellEffect;
     }
 
     public Effect getRemoveCellEffect() {
         return removeCellEffect;
     }
 
     public void setRemoveCellEffect(Effect removeCellEffect) {
         this.removeCellEffect = removeCellEffect;
     }
 
     public static class NameComparator implements Comparator {
 
         private boolean ascending;
 
         public NameComparator(boolean ascending) {
             this.ascending = ascending;
         }
 
         public int compare(Object o1, Object o2) {
             PrivilegeBean pb1 = (PrivilegeBean) o1;
             PrivilegeBean pb2 = (PrivilegeBean) o2;
 
             if (ascending) {
                 return pb1.getName().compareTo(pb2.getName());
             } else {
                 return pb2.getName().compareTo(pb1.getName());
             }
         }
     }
     private String name = "myPolicy" + System.currentTimeMillis();
     private String description = null;
     private ViewEntitlement viewEntitlement = new ViewEntitlement();
     private ViewCondition viewCondition = null;
     private ViewSubject viewSubject = null;
     private Effect nameCellEffect = null;
     private Effect resourcesCellEffect = null;
     private Effect subjectCellEffect = null;
     private Effect conditionCellEffect = null;
     private Effect removeCellEffect = null;
 
     public PrivilegeBean() {
         // empty
     }
 
     public PrivilegeBean(
                 Privilege p,
                 Map<String,ViewApplication> viewApplications,
                 SubjectFactory subjectFactory,
                 ConditionTypeFactory conditionTypeFactory) {
 
         name = p.getName();
         description = null; // TODO
 
         // entitlement (TODO: exceptions)
         viewEntitlement = new ViewEntitlement(p.getEntitlement(), viewApplications);
 
         // subjects
         viewSubject = subjectFactory.getViewSubject(p.getSubject());
 
         // conditions
         viewCondition = conditionTypeFactory.getViewCondition(p.getCondition());
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
 
     public String getViewSubjectToString() {
         if (viewSubject == null) {
             return "";
         }
         return viewSubject.toString();
     }
 
     @Override
     public int hashCode() {
         return getName().hashCode();
     }
 
     @Override
     public boolean equals(Object o) {
         if (!(o instanceof PrivilegeBean)) {
             return false;
         }
         PrivilegeBean other = (PrivilegeBean)o;
 
         return other.getName().equals(name);
     }
 }
