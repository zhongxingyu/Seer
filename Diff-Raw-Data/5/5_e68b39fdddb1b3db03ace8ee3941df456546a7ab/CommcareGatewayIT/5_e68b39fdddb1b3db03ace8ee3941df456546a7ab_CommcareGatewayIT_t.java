 package org.wv.stepsovc.commcare.gateway;
 
 import junit.framework.Assert;
 import org.junit.Ignore;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.test.context.ContextConfiguration;
 import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
 import org.wv.stepsovc.commcare.domain.CaseType;
 import org.wv.stepsovc.commcare.domain.User;
 import org.wv.stepsovc.commcare.factories.GroupFactory;
 import org.wv.stepsovc.commcare.repository.AllGroups;
 import org.wv.stepsovc.commcare.repository.AllUsers;
 import org.wv.stepsovc.commcare.vo.BeneficiaryInformation;
 import org.wv.stepsovc.commcare.vo.CareGiverInformation;
 
 import java.util.HashMap;
 import java.util.UUID;
 
 import static junit.framework.Assert.assertEquals;
 import static org.junit.Assert.assertNotNull;
 import static org.junit.Assert.assertNull;
 import static org.wv.stepsovc.commcare.gateway.CommcareGateway.*;
 
 @RunWith(SpringJUnit4ClassRunner.class)
 @ContextConfiguration("classpath*:applicationContext-stepsovc-commcare-api.xml")
 public class CommcareGatewayIT {
 
     @Autowired
     CommcareGateway commcareGateway;
     @Autowired
     AllGroups allGroups;
     @Autowired
     AllUsers allUsers;
 
     private User newUser1;
     private User newUser2;
     private String userId1;
     private String userId2;
     private String groupName = "group1";
 
     @Ignore
     public void shouldCreateNewBeneficiary() throws Exception {
         BeneficiaryInformation beneficiaryInformation = getBeneficiaryInformation("7ac0b33f0dac4a81c6d1fbf1bd9dfee0", "ggg2", UUID.randomUUID().toString(), "new-test-case-6", "new-test-case-6", "cg1", null);
         commcareGateway.createNewBeneficiary(beneficiaryInformation);
     }
 
    @Ignore
     public void shouldRegisterNewCareGiverUser() throws InterruptedException {
         String testCareGiverName = "testCareGiverName";
         CareGiverInformation careGiverInformation = new CareGiverInformation();
         careGiverInformation.setId("testCareGiverId");
         careGiverInformation.setCode("testCareGiverCode");
         careGiverInformation.setName(testCareGiverName);
         careGiverInformation.setPassword("12345");
         careGiverInformation.setPhoneNumber("1234567890");
 
         assertNull(allUsers.getUserByName(testCareGiverName));
         commcareGateway.registerUser(careGiverInformation);
        Thread.sleep(2000);
         User newCareGiver = allUsers.get("testCareGiverId");
         assertNotNull(newCareGiver);
         allUsers.remove(newCareGiver);
     }
 
     @Test
     public void shouldConvertObjectToXml() {
         BeneficiaryInformation beneficiaryInformation = getBeneficiaryInformation("f98589102c60fcc2e0f3c422bb361ebd", "cg1", "c7264b49-4e3d-4659-8df3-7316539829cb", "test-case", "XYZ/123", "cg1", "hw1");
         assertConversion(CommcareGateway.BENEFICIARY_FORM_KEY, beneficiaryInformation, BENEFICIARY_CASE_FORM_TEMPLATE_PATH, getExpectedBeneficiaryCaseXml());
         assertConversion(CommcareGateway.BENEFICIARY_FORM_KEY, beneficiaryInformation, OWNER_UPDATE_FORM_TEMPLATE_PATH, getExpectedUpdateOwnerXml());
 
         CareGiverInformation careGiverInformation = getCareGiverInformation("7ac0b33f0dac4a81c6d1fbf1bd9dfee0", "EW/123", "cg1", "9089091");
         assertConversion(CommcareGateway.CARE_GIVER_FORM_KEY, careGiverInformation, USER_REGISTRATION_FORM_TEMPLATE_PATH, getExpectedUserFormXml());
     }
 
     @Test
     public void shouldCreateGroupWithUsers() {
 
         newUser1 = createUser("newUser1");
         newUser2 = createUser("newUser2");
 
         allUsers.add(newUser1);
         allUsers.add(newUser2);
 
         userId1 = allUsers.getUserByName(newUser1.getUsername()).getId();
         userId2 = allUsers.getUserByName(newUser2.getUsername()).getId();
 
         String domainName = "stepsovc";
 
         allGroups.add(GroupFactory.createGroup(groupName, new String[]{userId1, userId2}, domainName));
         Assert.assertNotNull(allGroups.getGroupByName(groupName));
         allGroups.removeByName(groupName);
         allUsers.removeByName(newUser1.getUsername());
         allUsers.removeByName(newUser2.getUsername());
     }
 
     private User createUser(String userName) {
         User newUser = new User();
         newUser.setBase_doc("CouchUser");
         newUser.setDomain("stepsovc");
         newUser.setUsername(userName);
 
         return newUser;
     }
 
     private void assertConversion(String key, Object entity, String formPath, String expectedXML) {
         HashMap<String, Object> model = new HashMap<String, Object>();
         model.put(key, entity);
         String actualXML = commcareGateway.getXmlFromObject(formPath, model);
         assertEquals(expectedXML, actualXML);
     }
 
     private String getExpectedUserFormXml() {
         return "<Registration xmlns=\"http://openrosa.org/user/registration\">\n" +
                 "\n" +
                 "    <username>EW/123</username>\n" +
                 "    <password>sha1$90a7b$47a168cca627c6a34f4e349ea4b1fb3d01702c68</password>\n" +
                 "    <uuid>7ac0b33f0dac4a81c6d1fbf1bd9dfee0</uuid>\n" +
                 "    <date>01-01-2012</date>\n" +
                 "\n" +
                 "    <registering_phone_id>9089091</registering_phone_id>\n" +
                 "\n" +
                 "    <user_data type=\"hash\">\n" +
                 "        <name>cg1</name>\n" +
                 "    </user_data>\n" +
                 "</Registration>";
     }
 
     private CareGiverInformation getCareGiverInformation(String cgId, String cgCode, String cgName, String phoneNumber) {
         CareGiverInformation careGiverInformation = new CareGiverInformation();
         careGiverInformation.setId(cgId);
         careGiverInformation.setCode(cgCode);
         careGiverInformation.setName(cgName);
         careGiverInformation.setPhoneNumber(phoneNumber);
         return careGiverInformation;
     }
 
     private BeneficiaryInformation getBeneficiaryInformation(String caregiverId, String caregiverCode, String caseId, String caseName, String beneficiaryCode, String caregiverName, String ownerId) {
 
         BeneficiaryInformation beneficiaryInformation = new BeneficiaryInformation();
         beneficiaryInformation.setBeneficiaryId(caseId);
         beneficiaryInformation.setBeneficiaryCode(beneficiaryCode);
         beneficiaryInformation.setBeneficiaryName(caseName);
         beneficiaryInformation.setBeneficiaryDob("12-12-1988");
         beneficiaryInformation.setBeneficiarySex("male");
         beneficiaryInformation.setBeneficiaryTitle("MR");
         beneficiaryInformation.setReceivingOrganization("XAQ");
         beneficiaryInformation.setCareGiverCode(caregiverCode);
         beneficiaryInformation.setCareGiverId(caregiverId);
         beneficiaryInformation.setCareGiverName(caregiverName);
         beneficiaryInformation.setCaseType(CaseType.BENEFICIARY_CASE.getType());
         beneficiaryInformation.setDateModified("2012-05-02T22:18:45.071+05:30");
         beneficiaryInformation.setOwnerId(ownerId);
         return beneficiaryInformation;
     }
 
     private String getExpectedBeneficiaryCaseXml() {
 
         return "<?xml version=\"1.0\"?>\n" +
                 "<data xmlns=\"http://openrosa.org/formdesigner/A6E4F029-A971-41F1-80C1-9DDD5CC24571\" uiVersion=\"1\" version=\"12\"\n" +
                 "      name=\"Registration\">\n" +
                 "    <beneficiary_information>\n" +
                 "        <beneficiary_name>test-case</beneficiary_name>\n" +
                 "        <beneficiary_code>XYZ/123</beneficiary_code>\n" +
                 "        <beneficiary_dob>12-12-1988</beneficiary_dob>\n" +
                 "        <receiving_organization>XAQ</receiving_organization>\n" +
                 "        <sex>male</sex>\n" +
                 "        <title>MR</title>\n" +
                 "    </beneficiary_information>\n" +
                 "    <caregiver_information>\n" +
                 "        <caregiver_code>cg1</caregiver_code>\n" +
                 "        <caregiver_name>cg1</caregiver_name>\n" +
                 "    </caregiver_information>\n" +
                 "    <form_type>beneficiary_registration</form_type>\n" +
                 "    <case>\n" +
                 "        <case_id>c7264b49-4e3d-4659-8df3-7316539829cb</case_id>\n" +
                 "        <date_modified>2012-05-02T22:18:45.071+05:30</date_modified>\n" +
                 "        <create>\n" +
                 "            <case_type_id>beneficiary</case_type_id>\n" +
                 "            <case_name>XYZ/123</case_name>\n" +
                 "            <user_id>f98589102c60fcc2e0f3c422bb361ebd</user_id>\n" +
                 "            <external_id>XYZ/123</external_id>\n" +
                 "        </create>\n" +
                 "        <update>\n" +
                 "            <beneficiary_dob>12-12-1988</beneficiary_dob>\n" +
                 "            <title>MR</title>\n" +
                 "            <beneficiary_name>test-case</beneficiary_name>\n" +
                 "            <receiving_organization>XAQ</receiving_organization>\n" +
                 "            <caregiver_name>cg1</caregiver_name>\n" +
                 "            <sex>male</sex>\n" +
                 "            <form_type>beneficiary_registration</form_type>\n" +
                 "            <beneficiary_code>XYZ/123</beneficiary_code>\n" +
                 "            <caregiver_code>cg1</caregiver_code>\n" +
                 "        </update>\n" +
                 "    </case>\n" +
                 "    <meta>\n" +
                 "        <username>cg1</username>\n" +
                 "        <userID>f98589102c60fcc2e0f3c422bb361ebd</userID>\n" +
                 "    </meta>\n" +
                 "</data>";
     }
 
     private String getExpectedUpdateOwnerXml() {
         return "<?xml version='1.0' ?>\n" +
                 "<data uiVersion=\"1\" version=\"89\" name=\"update\"\n" +
                 "      xmlns=\"http://openrosa.org/formdesigner/E45F3EFD-A7BE-42A6-97C2-5133E766A2AA\">\n" +
                 "    <owner_id>hw1</owner_id>\n" +
                 "    <form_type>custom_update</form_type>\n" +
                 "    <case>\n" +
                 "        <case_id>c7264b49-4e3d-4659-8df3-7316539829cb</case_id>\n" +
                 "        <date_modified>2012-05-02T22:18:45.071+05:30</date_modified>\n" +
                 "        <update>\n" +
                 "            <form_type>custom_update</form_type>\n" +
                 "            <owner_id>hw1</owner_id>\n" +
                 "        </update>\n" +
                 "    </case>\n" +
                 "    <meta>\n" +
                 "        <username>cg1</username>\n" +
                 "        <userID>f98589102c60fcc2e0f3c422bb361ebd</userID>\n" +
                 "    </meta>\n" +
                 "</data>";
     }
 }
