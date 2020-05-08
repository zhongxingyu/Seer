 package com.sun.identity.admin.handler;
 
 import com.sun.identity.admin.dao.PolicyDao;
 import com.sun.identity.admin.model.PhaseEventAction;
 import com.sun.identity.admin.model.PolicyFilterHolder;
 import com.sun.identity.admin.model.PolicyManageBean;
 import com.sun.identity.admin.model.PolicyWizardBean;
 import com.sun.identity.admin.model.PrivilegeBean;
 import com.sun.identity.admin.model.QueuedActionBean;
 import java.io.Serializable;
 import javax.faces.event.ActionEvent;
 import javax.faces.event.PhaseId;
 import javax.faces.event.ValueChangeEvent;
 
 public class PolicyManageHandler implements Serializable {
 
     private PolicyManageBean policyManageBean;
     private QueuedActionBean queuedActionBean;
     private PolicyDao policyDao;
     private PolicyWizardBean policyEditWizardBean;
 
     public PrivilegeBean getPrivilegeBean(ActionEvent event) {
         PrivilegeBean pb = (PrivilegeBean) event.getComponent().getAttributes().get("privilegeBean");
         assert(pb != null);
 
         return pb;
     }
 
     public PolicyFilterHolder getPolicyFilterHolder(ActionEvent event) {
         PolicyFilterHolder pfh = (PolicyFilterHolder) event.getComponent().getAttributes().get("policyFilterHolder");
         assert(pfh != null);
 
         return pfh;
     }
 
     public PolicyManageBean getPolicyManageBean() {
         return policyManageBean;
     }
 
     public void setPolicyManageBean(PolicyManageBean policyManageBean) {
         this.policyManageBean = policyManageBean;
     }
 
     public void sortTableListener(ActionEvent event) {
         PhaseEventAction pea = new PhaseEventAction();
         pea.setDoBeforePhase(true);
         pea.setPhaseId(PhaseId.RENDER_RESPONSE);
         pea.setAction("#{policyManageHandler.handleSort}");
         pea.setParameters(new Class[]{});
         pea.setArguments(new Object[]{});
 
         queuedActionBean.getPhaseEventActions().add(pea);
     }
 
     private void addResetEvent() {
         PhaseEventAction pea = new PhaseEventAction();
         pea.setDoBeforePhase(true);
         pea.setPhaseId(PhaseId.RENDER_RESPONSE);
         pea.setAction("#{policyManageHandler.handleReset}");
         pea.setParameters(new Class[]{});
         pea.setArguments(new Object[]{});
 
         queuedActionBean.getPhaseEventActions().add(pea);
     }
 
     public void handleSort() {
         policyManageBean.getPolicyManageTableBean().sort();
     }
 
     public void handleReset() {
         policyManageBean.reset();
     }
 
     public void viewOptionsListener(ActionEvent event) {
         policyManageBean.setViewOptionsPopupVisible(!policyManageBean.isViewOptionsPopupVisible());
     }
 
     public void addPolicyFilterListener(ActionEvent event) {
         getPolicyManageBean().newPolicyFilterHolder();
         addResetEvent();
     }
 
     public void policyFilterChangedListener(ValueChangeEvent event) {
         addResetEvent();
     }
 
     public void removePolicyFilterListener(ActionEvent event) {
         PolicyFilterHolder pfh = getPolicyFilterHolder(event);
         getPolicyManageBean().getPolicyFilterHolders().remove(pfh);
         addResetEvent();
     }
 
     public void editListener(ActionEvent event) {
         PrivilegeBean pb = getPrivilegeBean(event);
         policyEditWizardBean.reset();
         policyEditWizardBean.setPrivilegeBean(pb);
         policyEditWizardBean.setAllEnabled(true);
         policyEditWizardBean.gotoStep(4);
     }
 
     public void removeListener(ActionEvent event) {
         PrivilegeBean pb = getPrivilegeBean(event);
         assert (pb != null);
 
         addRemoveAction(pb);
     }
 
     public void handlePolicyRemove(PrivilegeBean pb) {
         policyManageBean.getPrivilegeBeans().remove(pb);
         policyDao.removePrivilege(pb.getName());
     }
 
     private void addRemoveAction(PrivilegeBean pb) {
         PhaseEventAction pea = new PhaseEventAction();
         pea.setDoBeforePhase(false);
         pea.setPhaseId(PhaseId.RENDER_RESPONSE);
         pea.setAction("#{policyManageHandler.handlePolicyRemove}");
         pea.setParameters(new Class[]{PrivilegeBean.class});
         pea.setArguments(new Object[]{pb});
 
         queuedActionBean.getPhaseEventActions().add(pea);
     }
 
     public void setQueuedActionBean(QueuedActionBean queuedActionBean) {
         this.queuedActionBean = queuedActionBean;
     }
 
     public void setPolicyDao(PolicyDao policyDao) {
         this.policyDao = policyDao;
     }
 
     public void setPolicyEditWizardBean(PolicyWizardBean policyEditWizardBean) {
         this.policyEditWizardBean = policyEditWizardBean;
     }
 }
 
