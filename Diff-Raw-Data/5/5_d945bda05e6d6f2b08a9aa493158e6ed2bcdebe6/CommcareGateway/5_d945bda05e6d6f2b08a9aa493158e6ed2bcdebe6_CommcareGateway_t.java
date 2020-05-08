 package org.wv.stepsovc.commcare.gateway;
 
 import org.apache.velocity.app.VelocityEngine;
 import org.motechproject.http.client.service.HttpClientService;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Component;
 import org.springframework.ui.velocity.VelocityEngineUtils;
 import org.wv.stepsovc.commcare.domain.Group;
 import org.wv.stepsovc.commcare.factories.GroupFactory;
 import org.wv.stepsovc.commcare.repository.AllGroups;
 import org.wv.stepsovc.commcare.repository.AllUsers;
 import org.wv.stepsovc.commcare.vo.BeneficiaryInformation;
 import org.wv.stepsovc.commcare.vo.CaregiverInformation;
 
 import java.util.HashMap;
 import java.util.Map;
 
 
 @Component
 public class CommcareGateway {
 
     public static final String BENEFICIARY_CASE_FORM_TEMPLATE_PATH = "/templates/beneficiary-case-form.xml";
 
     public static final String OWNER_UPDATE_FORM_TEMPLATE_PATH = "/templates/update-owner-form.xml";
 
     public static final String USER_REGISTRATION_FORM_TEMPLATE_PATH = "/templates/user-registration-form.xml";
 
     public static final String BENEFICIARY_FORM_KEY = "beneficiary";
 
     public static final String CARE_GIVER_FORM_KEY = "caregiver";
 
     public static final String COMMCARE_URL = "http://192.168.42.32:8000/a/stepsovc/receiver";
 
     public static final String ALL_USERS_GROUP = "ALL_USERS";
 
     @Autowired
     private VelocityEngine velocityEngine;
     @Autowired
     private AllGroups allGroups;
     @Autowired
     private AllUsers allUsers;
 
     private Map model;
 
     @Autowired
     private HttpClientService httpClientService;
 
 
     public String getUserId(String name) {
         return allUsers.getUserByName(name) == null ? null : allUsers.getUserByName(name).getId();
     }
 
     private String getGroupId(String name) {
         return allGroups.getGroupByName(name) == null ? null : allGroups.getGroupByName(name).getId();
     }
 
     public boolean createGroup(String groupName, String[] commcareUserIds, String domain) {
 
         if (allGroups.getGroupByName(groupName) != null)
             return false;
         Group newGroup = GroupFactory.createGroup(groupName, commcareUserIds, domain);
         allGroups.add(newGroup);
         return true;
     }
 
     public void addGroupOwnership(BeneficiaryInformation beneficiaryInformation, String groupName) {
        beneficiaryInformation.setOwnerId(beneficiaryInformation.getCareGiverId() + "," + getGroupId(groupName));
         postOwnerUpdate(beneficiaryInformation);
     }
 
     public void removeGroupOwnership(BeneficiaryInformation beneficiaryInformation, String groupName) {
         int indexOfGroupId = beneficiaryInformation.getOwnerId().indexOf(getGroupId(groupName));
         if (indexOfGroupId != -1) {
             beneficiaryInformation.setOwnerId(removeGroupIdFromOwnerId(getGroupId(groupName), beneficiaryInformation.getOwnerId(), indexOfGroupId));
         }
         postOwnerUpdate(beneficiaryInformation);
     }
 
     public void addUserOwnership(BeneficiaryInformation beneficiaryInformation, String userId) {
        beneficiaryInformation.setOwnerId(beneficiaryInformation.getCareGiverId() + "," + userId);
         postOwnerUpdate(beneficiaryInformation);
     }
 
     private String removeGroupIdFromOwnerId(String groupId, String ownerId, int indexOfGroupId) {
         return ownerId.substring(0, indexOfGroupId - 1) + ownerId.substring(indexOfGroupId + groupId.length());
     }
 
     public void createCase(BeneficiaryInformation beneficiaryInformation) {
         Map<String, Object> model = new HashMap<String, Object>();
         model.put(BENEFICIARY_FORM_KEY, beneficiaryInformation);
         httpClientService.post(COMMCARE_URL, getXmlFromObject(BENEFICIARY_CASE_FORM_TEMPLATE_PATH, model));
     }
 
     public void registerUser(CaregiverInformation careGiverInformation) {
         Map<String, Object> model = new HashMap<String, Object>();
         model.put(CARE_GIVER_FORM_KEY, careGiverInformation);
         httpClientService.post(COMMCARE_URL, getXmlFromObject(USER_REGISTRATION_FORM_TEMPLATE_PATH, model));
     }
 
     void postOwnerUpdate(BeneficiaryInformation beneficiaryInformation) {
         Map<String, Object> model = new HashMap<String, Object>();
         model.put(BENEFICIARY_FORM_KEY, beneficiaryInformation);
         httpClientService.post(COMMCARE_URL, getXmlFromObject(OWNER_UPDATE_FORM_TEMPLATE_PATH, model));
     }
 
     public String getXmlFromObject(String templatePath, Map model) {
         return VelocityEngineUtils.mergeTemplateIntoString(velocityEngine, templatePath, "UTF-8", model);
     }
 }
