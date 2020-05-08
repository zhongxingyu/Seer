 package com.sun.identity.admin.model;
 
 import com.icesoft.faces.context.effects.Effect;
 import com.sun.identity.entitlement.Entitlement;
 import com.sun.identity.entitlement.EntitlementCondition;
 import com.sun.identity.entitlement.EntitlementException;
 import com.sun.identity.entitlement.EntitlementSubject;
 import com.sun.identity.entitlement.Privilege;
 import com.sun.identity.entitlement.ResourceAttributes;
 import com.sun.identity.entitlement.opensso.OpenSSOPrivilege;
 import java.io.Serializable;
 import java.util.Comparator;
 import java.util.Date;
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
 
     public Effect getActionCellEffect() {
         return actionCellEffect;
     }
 
     public void setActionCellEffect(Effect actionCellEffect) {
         this.actionCellEffect = actionCellEffect;
     }
 
     public Effect getExceptionsCellEffect() {
         return exceptionsCellEffect;
     }
 
     public void setExceptionsCellEffect(Effect exceptionsCellEffect) {
         this.exceptionsCellEffect = exceptionsCellEffect;
     }
 
     public Date getBirth() {
         return birth;
     }
 
     public void setBirth(Date birth) {
         this.birth = birth;
     }
 
     public Effect getBirthCellEffect() {
         return birthCellEffect;
     }
 
     public void setBirthCellEffect(Effect birthCellEffect) {
         this.birthCellEffect = birthCellEffect;
     }
 
     public Effect getAuthorCellEffect() {
         return authorCellEffect;
     }
 
     public void setAuthorCellEffect(Effect authorCellEffect) {
         this.authorCellEffect = authorCellEffect;
     }
 
     public String getAuthor() {
         return author;
     }
 
     public void setAuthor(String author) {
         this.author = author;
     }
 
     public Date getModified() {
         return modified;
     }
 
     public void setModified(Date modified) {
         this.modified = modified;
     }
 
     public String getModifier() {
         return modifier;
     }
 
     public void setModifier(String modifier) {
         this.modifier = modifier;
     }
 
     public Effect getModifierCellEffect() {
         return modifierCellEffect;
     }
 
     public void setModifierCellEffect(Effect modifierCellEffect) {
         this.modifierCellEffect = modifierCellEffect;
     }
 
     public Effect getModifiedCellEffect() {
         return modifiedCellEffect;
     }
 
     public void setModifiedCellEffect(Effect modifiedCellEffect) {
         this.modifiedCellEffect = modifiedCellEffect;
     }
 
     public Effect getExportCellEffect() {
         return exportCellEffect;
     }
 
     public void setExportCellEffect(Effect exportCellEffect) {
         this.exportCellEffect = exportCellEffect;
     }
 
     public Effect getDescriptionCellEffect() {
         return descriptionCellEffect;
     }
 
     public void setDescriptionCellEffect(Effect descriptionCellEffect) {
         this.descriptionCellEffect = descriptionCellEffect;
     }
 
     public Effect getApplicationCellEffect() {
         return applicationCellEffect;
     }
 
     public void setApplicationCellEffect(Effect applicationCellEffect) {
         this.applicationCellEffect = applicationCellEffect;
     }
 
     public static abstract class PrivilegeComparator implements Comparator {
         private boolean ascending;
 
         public PrivilegeComparator(boolean ascending) {
             this.ascending = ascending;
         }
 
         public boolean isAscending() {
             return ascending;
         }
 
         public void setAscending(boolean ascending) {
             this.ascending = ascending;
         }
 
     }
 
     public static class NameComparator extends PrivilegeComparator {
 
         public NameComparator(boolean ascending) {
             super(ascending);
         }
 
         public int compare(Object o1, Object o2) {
             PrivilegeBean pb1 = (PrivilegeBean) o1;
             PrivilegeBean pb2 = (PrivilegeBean) o2;
 
            if (!isAscending()) {
                 return pb1.getName().compareTo(pb2.getName());
             } else {
                 return pb2.getName().compareTo(pb1.getName());
             }
         }
     }
 
     public static class DescriptionComparator extends PrivilegeComparator {
 
         public DescriptionComparator(boolean ascending) {
             super(ascending);
         }
 
         public int compare(Object o1, Object o2) {
             PrivilegeBean pb1 = (PrivilegeBean) o1;
             PrivilegeBean pb2 = (PrivilegeBean) o2;
 
            String d1 = (pb1.getDescription() == null) ? "" : pb1.getDescription();
            String d2 = (pb2.getDescription() == null) ? "" : pb2.getDescription();

            if (!isAscending()) {
                return d1.compareTo(d2);
             } else {
                return d2.compareTo(d1);
             }
         }
     }
 
     public static class ApplicationComparator extends PrivilegeComparator {
 
         public ApplicationComparator(boolean ascending) {
             super(ascending);
         }
 
         public int compare(Object o1, Object o2) {
             PrivilegeBean pb1 = (PrivilegeBean) o1;
             PrivilegeBean pb2 = (PrivilegeBean) o2;
 
            if (!isAscending()) {
                 return pb1.getViewEntitlement().getViewApplication().getName().compareTo(pb2.getViewEntitlement().getViewApplication().getName());
             } else {
                 return pb2.getViewEntitlement().getViewApplication().getName().compareTo(pb1.getViewEntitlement().getViewApplication().getName());
             }
         }
     }
 
     public static class BirthComparator extends PrivilegeComparator {
 
         public BirthComparator(boolean ascending) {
             super(ascending);
         }
 
         public int compare(Object o1, Object o2) {
             PrivilegeBean pb1 = (PrivilegeBean) o1;
             PrivilegeBean pb2 = (PrivilegeBean) o2;
 
            if (!isAscending()) {
                 return pb1.getBirth().compareTo(pb2.getBirth());
             } else {
                 return pb2.getBirth().compareTo(pb1.getBirth());
             }
         }
     }
 
     public static class ModifiedComparator extends PrivilegeComparator {
 
         public ModifiedComparator(boolean ascending) {
             super(ascending);
         }
 
         public int compare(Object o1, Object o2) {
             PrivilegeBean pb1 = (PrivilegeBean) o1;
             PrivilegeBean pb2 = (PrivilegeBean) o2;
 
            if (!isAscending()) {
                 return pb1.getModified().compareTo(pb2.getModified());
             } else {
                 return pb2.getModified().compareTo(pb1.getModified());
             }
         }
     }
 
     public static class AuthorComparator extends PrivilegeComparator {
 
         public AuthorComparator(boolean ascending) {
             super(ascending);
         }
 
         public int compare(Object o1, Object o2) {
             PrivilegeBean pb1 = (PrivilegeBean) o1;
             PrivilegeBean pb2 = (PrivilegeBean) o2;
 
            if (!isAscending()) {
                 return pb1.getAuthor().compareTo(pb2.getAuthor());
             } else {
                 return pb2.getAuthor().compareTo(pb1.getAuthor());
             }
         }
     }
 
     public static class ModifierComparator extends PrivilegeComparator {
 
         public ModifierComparator(boolean ascending) {
             super(ascending);
         }
 
         public int compare(Object o1, Object o2) {
             PrivilegeBean pb1 = (PrivilegeBean) o1;
             PrivilegeBean pb2 = (PrivilegeBean) o2;
 
            if (!isAscending()) {
                 return pb1.getModifier().compareTo(pb2.getModifier());
             } else {
                 return pb2.getModifier().compareTo(pb1.getModifier());
             }
         }
     }
 
 
     private String name = null;
     private String description = null;
     private ViewEntitlement viewEntitlement = new ViewEntitlement();
     private ViewCondition viewCondition = null;
     private ViewSubject viewSubject = null;
     private Date birth;
     private Date modified;
     private String author;
     private String modifier;
     private Effect nameCellEffect = null;
     private Effect descriptionCellEffect = null;
     private Effect resourcesCellEffect = null;
     private Effect subjectCellEffect = null;
     private Effect conditionCellEffect = null;
     private Effect removeCellEffect = null;
     private Effect actionCellEffect = null;
     private Effect exceptionsCellEffect = null;
     private Effect birthCellEffect = null;
     private Effect modifierCellEffect = null;
     private Effect modifiedCellEffect = null;
     private Effect authorCellEffect = null;
     private Effect exportCellEffect = null;
     private Effect applicationCellEffect = null;
 
     public PrivilegeBean() {
         // empty
     }
 
     public PrivilegeBean(
                 Privilege p,
                 Map<String,ViewApplication> viewApplications,
                 SubjectFactory subjectFactory,
                 ConditionTypeFactory conditionTypeFactory) {
 
         name = p.getName();
         description = p.getDescription();
 
         // entitlement
         viewEntitlement = new ViewEntitlement(p.getEntitlement(), viewApplications);
 
         // subjects
         viewSubject = subjectFactory.getViewSubject(p.getSubject());
 
         // conditions
         viewCondition = conditionTypeFactory.getViewCondition(p.getCondition());
 
         // created, modified
         birth = new Date(p.getCreationDate());
         author = p.getCreatedBy();
         modified = new Date(p.getLastModifiedDate());
         modifier = p.getLastModifiedBy();
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
 
         try {
             Privilege p = new OpenSSOPrivilege(
                 name,
                 entitlement,
                 eSubject,
                 condition,
                 attrs);
             p.setDescription(description);
             return p;
         } catch (EntitlementException ee) {
             throw new RuntimeException(ee);
         }
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
