 package com.sun.identity.admin.model;
 
 public class SubjectsPolicySummary extends PolicySummary {
 
     public SubjectsPolicySummary(PolicyWizardBean policyWizardBean) {
         super(policyWizardBean);
     }
 
     public String getLabel() {
         // TODO: localize
         return "Subjects";
     }
 
     public String getValue() {
        ViewSubject vs = getPolicyWizardBean().getPrivilegeBean().getViewSubject();
        if (vs == null) {
            return "0";
        }
        return Integer.toString(vs.getSizeLeafs());
     }
 
     public boolean isExpandable() {
         return getPolicyWizardBean().getPrivilegeBean().getViewSubject().getSizeLeafs() > 0;
 
     }
     public String getIcon() {
         return "../image/role.png";
     }
 
     public String getTemplate() {
         return "/admin/facelet/template/policy-summary-subjects.xhtml";
     }
 
     public PolicyWizardStep getGotoStep() {
         return PolicyWizardStep.SUBJECTS;
     }
 
 }
