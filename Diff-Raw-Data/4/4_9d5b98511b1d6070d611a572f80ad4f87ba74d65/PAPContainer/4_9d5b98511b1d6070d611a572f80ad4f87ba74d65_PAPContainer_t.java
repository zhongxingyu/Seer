 package org.glite.authz.pap.repository;
 
 import java.util.Date;
 import java.util.List;
 
 import org.glite.authz.pap.common.PAP;
 import org.glite.authz.pap.common.PAPConfiguration;
 import org.glite.authz.pap.common.xacml.utils.PolicySetHelper;
 import org.glite.authz.pap.monitoring.MonitoredProperties;
 import org.glite.authz.pap.repository.dao.DAOFactory;
 import org.glite.authz.pap.repository.dao.PolicyDAO;
 import org.glite.authz.pap.repository.dao.PolicySetDAO;
 import org.glite.authz.pap.repository.exceptions.AlreadyExistsException;
 import org.glite.authz.pap.repository.exceptions.NotFoundException;
 import org.glite.authz.pap.repository.exceptions.RepositoryException;
 import org.opensaml.xacml.policy.PolicySetType;
 import org.opensaml.xacml.policy.PolicyType;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 public class PAPContainer {
 
     @SuppressWarnings("unused")
     private static final Logger log = LoggerFactory.getLogger(PAPContainer.class);
 
     private final PAP pap;
     private final String papId;
     private final PolicyDAO policyDAO;
     private final PolicySetDAO policySetDAO;
     private final String rootPolicySetId;
 
     public PAPContainer(PAP pap) {
         this.pap = pap;
         papId = pap.getPapId();
         rootPolicySetId = papId;
         policySetDAO = DAOFactory.getDAOFactory().getPolicySetDAO();
         policyDAO = DAOFactory.getDAOFactory().getPolicyDAO();
     }
     
     public void addPolicy(String policySetId, PolicyType policy) throws NotFoundException, AlreadyExistsException , RepositoryException {
         
         if (!policySetDAO.exists(papId, policySetId))
             throw new NotFoundException("PolicySetId \"" + policySetId + "\" not found");
         
         String policyId = policy.getPolicyId();
         
         policyDAO.store(papId, policy);
         
         try {
             modifyReference(policySetId, policyId, true, true);
         } catch (RepositoryException e) {
             policyDAO.delete(papId, policyId);
             throw e;
         }
         
         updatePAPPolicyLastModificationTime();
         
         notifyPoliciesAdded(1);
     }
     
     public void deleteAllPolicies() {
         int numOfDeletedPolicies = policyDAO.deleteAll(papId);
         updatePAPPolicyLastModificationTime();
         notifyPoliciesDeleted(numOfDeletedPolicies);
     }
     
     public void deleteAllPolicySets() {
         policySetDAO.deleteAll(papId);
     }
 
     public void deletePolicy(String id) throws NotFoundException, RepositoryException {
         policyDAO.delete(papId, id);
         updatePAPPolicyLastModificationTime();
         notifyPoliciesDeleted(1);
     }
 
     public void deletePolicySet(String id) throws NotFoundException, RepositoryException {
         policySetDAO.delete(papId, id);
     }
 
     public List<PolicyType> getAllPolicies() {
         return policyDAO.getAll(papId);
     }
 
     public List<PolicySetType> getAllPolicySets() {
         List<PolicySetType> policySetList = policySetDAO.getAll(papId);
 
         // place the PAP root PolicySet as the first element
         for (PolicySetType policySetElement : policySetList) {
             if (policySetElement.getPolicySetId().equals(rootPolicySetId)) {
 
                 int currentIndex = policySetList.indexOf(policySetElement);
 
                 if (currentIndex != 0) { // swap elements
                     PolicySetType tempPolicySet = policySetList.get(0);
                     policySetList.set(0, policySetElement);
                     policySetList.set(currentIndex, tempPolicySet);
                 }
                 break;
             }
         }
         return policySetList;
     }
 
     public int getNumberOfPolicies() {
         return policyDAO.getNumberOfPolicies(papId);
     }
 
     public PAP getPAP() {
         return this.pap;
     }
 
     public PolicySetType getPAPRootPolicySet() {
         return policySetDAO.getById(papId, getPAPRootPolicySetId());
     }
 
     public String getPAPRootPolicySetId() {
         return rootPolicySetId;
     }
 
     public PolicyType getPolicy(String id) {
         return policyDAO.getById(papId, id);
     }
 
     public PolicySetType getPolicySet(String id) {
         return policySetDAO.getById(papId, id);
     }
 
     public boolean hasPolicy(String id) {
         return policyDAO.exists(papId, id);
     }
 
     public boolean hasPolicySet(String id) {
         return policySetDAO.exists(papId, id);
     }
 
     public void removePolicyAndReferences(String policyId) throws NotFoundException, RepositoryException {
         
         if (!policyDAO.exists(papId, policyId))
             throw new NotFoundException("PolicyId \"" + policyId + "\" does not exists");
         
         List<PolicySetType> policySetList = policySetDAO.getAll(papId);
         
         try {
             for (PolicySetType policySet : policySetList) {
                 modifyReference(policySet.getPolicySetId(), policyId, true, false);
             }
         } catch (NotFoundException e) {
             // nothing to do
         }
         
         policyDAO.delete(papId, policyId);
         
         updatePAPPolicyLastModificationTime();
         
         notifyPoliciesDeleted(1);
     }
     
     public void storePolicy(PolicyType policy) {
         policyDAO.store(papId, policy);
         updatePAPPolicyLastModificationTime();
         notifyPoliciesAdded(1);
     }
     
     public void storePolicySet(PolicySetType policySet) {
         policySetDAO.store(papId, policySet);
     }
 
     public void updatePolicy(PolicyType policy) {
         policyDAO.update(papId, policy);
     }
     
     public void updatePolicySet(PolicySetType policySet) {
         policySetDAO.update(papId, policySet);
     }
     
     private synchronized void addReference(String policySetId, String referenceId, boolean isPolicyReference) throws NotFoundException,
             AlreadyExistsException , RepositoryException {
 
         PolicySetType policySet = policySetDAO.getById(papId, policySetId);
         
         if (PolicySetHelper.referenceIdExists(policySet, referenceId))
             throw new AlreadyExistsException("Reference id \"" + referenceId + "\" alredy exists");
         
         if (isPolicyReference)
             PolicySetHelper.addPolicyReference(policySet, referenceId);
         else
             PolicySetHelper.addPolicySetReference(policySet, referenceId);
         
         policySetDAO.update(papId, policySet);
 
     }
     
     private synchronized void modifyReference(String policySetId, String referenceId, boolean isPolicyReference, boolean isAddOperation)
             throws NotFoundException, AlreadyExistsException, RepositoryException {
         
         if (isAddOperation)
             addReference(policySetId, referenceId, isPolicyReference);
         else
             removeReference(policySetId, referenceId, isPolicyReference);
         
     }
     
     private void notifyPoliciesAdded(int numOfAddedPolicies) {
         
         String propName;
         
         if (PAP.LOCAL_PAP_ID.equals(papId)) {
             propName = MonitoredProperties.NUM_OF_LOCAL_POLICIES_PROP_NAME;
         } else {
             propName = MonitoredProperties.NUM_OF_REMOTE_POLICIES_PROP_NAME;
         }
         
         synchronized (this) {
             Integer numOfPoliciesInteger = (Integer) PAPConfiguration.instance().getMonitoringProperty(propName);
             
             if (numOfPoliciesInteger == null) {
                 return;
             }
             
             int numOfPolicies = numOfPoliciesInteger.intValue() + numOfAddedPolicies;
             numOfPoliciesInteger = new Integer(numOfPolicies);
             PAPConfiguration.instance().setMonitoringProperty(propName, numOfPoliciesInteger);
             
             propName = MonitoredProperties.NUM_OF_POLICIES_PROP_NAME;
             
             numOfPoliciesInteger = (Integer) PAPConfiguration.instance().getMonitoringProperty(propName);
             numOfPolicies = numOfPoliciesInteger.intValue() + numOfAddedPolicies;
             numOfPoliciesInteger = new Integer(numOfPolicies);
             PAPConfiguration.instance().setMonitoringProperty(propName, numOfPoliciesInteger);
         }
     }
     
     private void notifyPoliciesDeleted(int numOfDeletedPolicies) {
         
         String propName;
         
         if (PAP.LOCAL_PAP_ID.equals(papId)) {
             propName = MonitoredProperties.NUM_OF_LOCAL_POLICIES_PROP_NAME;
         } else {
             propName = MonitoredProperties.NUM_OF_REMOTE_POLICIES_PROP_NAME;
         }
         
         synchronized (this) {
             Integer numOfPoliciesInteger = (Integer) PAPConfiguration.instance().getMonitoringProperty(propName);
             
             if (numOfPoliciesInteger == null) {
                 return;
             }
             
             int numOfPolicies = numOfPoliciesInteger.intValue() - numOfDeletedPolicies;
             numOfPoliciesInteger = new Integer(numOfPolicies);
             PAPConfiguration.instance().setMonitoringProperty(propName, numOfPoliciesInteger);
             
             propName = MonitoredProperties.NUM_OF_POLICIES_PROP_NAME;
             
             numOfPoliciesInteger = (Integer) PAPConfiguration.instance().getMonitoringProperty(propName);
             numOfPolicies = numOfPoliciesInteger.intValue() - numOfDeletedPolicies;
             numOfPoliciesInteger = new Integer(numOfPolicies);
             PAPConfiguration.instance().setMonitoringProperty(propName, numOfPoliciesInteger);
         }
     }
     
     private void notifyPolicyLastModificationTimeUpdate() {
         
         if (!PAP.LOCAL_PAP_ID.equals(papId)) {
             return;
         }
 
         PAPConfiguration.instance().setMonitoringProperty(MonitoredProperties.POLICY_LAST_MODIFICATION_TIME_PROP_NAME,
                 pap.getPolicyLastModificationTimeString());
     }
     
     private synchronized void removeReference(String policySetId, String referenceId, boolean isPolicyReference)
             throws NotFoundException, RepositoryException {
 
         PolicySetType policySet = policySetDAO.getById(papId, policySetId);
         
         if (isPolicyReference)
             PolicySetHelper.deletePolicyReference(policySet, referenceId);
         else
             PolicySetHelper.deletePolicySetReference(policySet, referenceId);
         
         policySetDAO.update(papId, policySet);
     }
     
     private void updatePAPPolicyLastModificationTime() {
         pap.setPolicyLastModificationTime(new Date());
        RepositoryManager.getDAOFactory().getPAPDAO().update(pap);
         notifyPolicyLastModificationTimeUpdate();
     }
     
 }
