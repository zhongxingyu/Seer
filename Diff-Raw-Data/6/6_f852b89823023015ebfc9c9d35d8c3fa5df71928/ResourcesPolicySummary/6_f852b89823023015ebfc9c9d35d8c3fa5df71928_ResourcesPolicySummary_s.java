 package com.sun.identity.admin.model;
 
 public class ResourcesPolicySummary extends PolicySummary {
 
     public ResourcesPolicySummary(PolicyWizardBean policyWizardBean) {
         super(policyWizardBean);
     }
 
     public String getLabel() {
         // TODO: localize
         return "Resources";
     }
 
     public String getValue() {
        return Integer.toString(getPolicyWizardBean().getPrivilegeBean().getViewEntitlement().getResources().size());
     }
 
     public String getIcon() {
         return "../image/url.png";
     }
 
     public String getTemplate() {
         return "/admin/facelet/template/policy-summary-resources.xhtml";
     }
 
     public PolicyWizardStep getGotoStep() {
         return PolicyWizardStep.RESOURCES;
     }
 
 }
