 package org.wv.stepsovc.commcare.gateway;
 
 import org.apache.commons.lang.ArrayUtils;
 import org.apache.velocity.app.VelocityEngine;
 import org.junit.Before;
 import org.junit.Test;
 import org.mockito.ArgumentCaptor;
 import org.mockito.Mock;
 import org.motechproject.http.client.service.HttpClientService;
 import org.springframework.test.util.ReflectionTestUtils;
 import org.wv.stepsovc.commcare.domain.Group;
 import org.wv.stepsovc.commcare.repository.AllGroups;
 import org.wv.stepsovc.commcare.repository.AllUsers;
 import org.wv.stepsovc.commcare.vo.BeneficiaryInformation;
 import org.wv.stepsovc.commcare.vo.CaregiverInformation;
 import org.wv.stepsovc.commcare.vo.CaseOwnershipInformation;
 import org.wv.stepsovc.commcare.vo.FacilityInformation;
 
 import java.util.HashMap;
 import java.util.Map;
 import java.util.UUID;
 
 import static fixture.TestFixture.*;
 import static fixture.XmlFixture.*;
 import static org.hamcrest.Matchers.is;
 import static org.junit.Assert.assertThat;
 import static org.mockito.Matchers.eq;
 import static org.mockito.Mockito.*;
 import static org.mockito.MockitoAnnotations.initMocks;
 import static org.wv.stepsovc.commcare.gateway.CommcareGateway.*;
 
 
 public class CommcareGatewayTest {
 
     @Mock
     HttpClientService mockHttpClientService;
 
     @Mock
     VelocityEngine mockVelocityEngine;
 
     @Mock
     AllGroups mockAllGroups;
 
     @Mock
     private AllUsers allUsers;
 
     private Map<String, Object> model;
 
     private CommcareGateway spyCommcareGateway;
 
     private String someUrl;
 
 
     @Before
     public void setup() {
         initMocks(this);
         someUrl = "http://localhost:8000";
         spyCommcareGateway = spy(new CommcareGateway());
         ReflectionTestUtils.setField(spyCommcareGateway, "httpClientService", mockHttpClientService);
         ReflectionTestUtils.setField(spyCommcareGateway, "velocityEngine", mockVelocityEngine);
         ReflectionTestUtils.setField(spyCommcareGateway, "allGroups", mockAllGroups);
         ReflectionTestUtils.setField(spyCommcareGateway, "allUsers", allUsers);
         ReflectionTestUtils.setField(spyCommcareGateway, "COMMCARE_RECIEVER_URL", someUrl);
         model = new HashMap<String, Object>();
 
     }
 
 
     @Test
     public void shouldSubmitBeneficiaryCaseForm() throws Exception {
         BeneficiaryInformation beneficiaryInformation = getBeneficiaryInformation("f98589102c60fcc2e0f3c422bb361ebd", "cg1", UUID.randomUUID().toString(), "Albie-case", "ABC", "cg1", "null");
         model.put(CommcareGateway.BENEFICIARY_FORM_KEY, beneficiaryInformation);
         doReturn(getExpectedBeneficiaryCaseXml()).when(spyCommcareGateway).getXmlFromObject(eq(BENEFICIARY_CASE_FORM_TEMPLATE_PATH), eq(model));
 
         spyCommcareGateway.createCase(beneficiaryInformation);
 
         verify(mockHttpClientService).post(someUrl, getExpectedBeneficiaryCaseXml());
     }
 
     @Test
     public void shouldCreateCareGiverInCommcare() {
         CaregiverInformation careGiverInformation = getCareGiverInformation("7ac0b33f0dac4a81c6d1fbf1bd9dfee0", "EW/123", "9089091");
         model.put(CommcareGateway.CARE_GIVER_FORM_KEY, careGiverInformation);
         doReturn(getExpectedUserFormXml()).when(spyCommcareGateway).getXmlFromObject(eq(USER_REGISTRATION_FORM_TEMPLATE_PATH), eq(model));
         spyCommcareGateway.registerCaregiver(careGiverInformation);
         verify(mockHttpClientService).post(someUrl, getExpectedUserFormXml());
     }
 
 
     @Test
     public void shouldSubmitUpdateOwnerFormForGroupOwnershipRequest() {
         CaseOwnershipInformation caseOwnershipInformation = getCaseOwnershipInformation(UUID.randomUUID().toString(), null, "f98589102c60fcc2e0f3c422bb361ebd", "cg1", null);
         String currentOwnerId = caseOwnershipInformation.getUserId();
         model.put(CommcareGateway.CASE_OWNERSHIP_FORM_KEY, caseOwnershipInformation);
         doReturn(getExpectedBeneficiaryCaseXml()).when(spyCommcareGateway).getXmlFromObject(eq(OWNER_UPDATE_FORM_TEMPLATE_PATH), eq(model));
         String someGroup = "someGroup";
         Group group = new Group();
         group.setId("somegroupId");
         doReturn(group).when(mockAllGroups).getGroupByName(someGroup);
         ArgumentCaptor<CaseOwnershipInformation> captor = ArgumentCaptor.forClass(CaseOwnershipInformation.class);
 
         spyCommcareGateway.addGroupOwnership(caseOwnershipInformation, someGroup);
 
         verify(spyCommcareGateway).postOwnerUpdate(captor.capture());
         verify(mockHttpClientService).post(someUrl, getExpectedBeneficiaryCaseXml());
 
         assertThat(captor.getValue().getOwnerId(), is(currentOwnerId + "," + group.getId()));
     }
 
     @Test
     public void shouldSendOwnershipUpdateXmlForAddUserOwnershipRequest() throws Exception {
         CaseOwnershipInformation caseOwnershipInformation = getCaseOwnershipInformation(UUID.randomUUID().toString(), null, "f98589102c60fcc2e0f3c422bb361ebd", "cg1", null);
         String currentOwnerId = caseOwnershipInformation.getUserId();
         model.put(CommcareGateway.CASE_OWNERSHIP_FORM_KEY, caseOwnershipInformation);
         doReturn(getExpectedBeneficiaryCaseXml()).when(spyCommcareGateway).getXmlFromObject(eq(OWNER_UPDATE_FORM_TEMPLATE_PATH), eq(model));
         String userId = "userId";
 
         ArgumentCaptor<CaseOwnershipInformation> captor = ArgumentCaptor.forClass(CaseOwnershipInformation.class);
         spyCommcareGateway.addUserOwnership(caseOwnershipInformation, userId);
 
         verify(spyCommcareGateway).postOwnerUpdate(captor.capture());
         verify(mockHttpClientService).post(spyCommcareGateway.getCOMMCARE_RECIEVER_URL(), getExpectedBeneficiaryCaseXml());
 
         assertThat(captor.getValue().getOwnerId(), is(currentOwnerId + "," + userId));
 
     }
 
     @Test
     public void shouldRegisterFacility() {
         FacilityInformation facilityInformation = spy(new FacilityInformation());
         model.put(FACILITY_FORM_KEY, facilityInformation);
         doReturn(getExpectedFacilityXml()).when(spyCommcareGateway).getXmlFromObject(FACILITY_REGISTRATION_FORM_TEMPLATE_PATH, model);
         spyCommcareGateway.registerFacilityUser(facilityInformation);
         verify(mockHttpClientService).post(spyCommcareGateway.getCOMMCARE_RECIEVER_URL(), getExpectedFacilityXml());
 
     }
 
 
     @Test
     public void shouldCreateFacilityCase() {
         FacilityInformation facilityInformation = spy(new FacilityInformation());
         model.put(FACILITY_FORM_KEY, facilityInformation);
         doReturn(getExpectedFacilityCaseXml()).when(spyCommcareGateway).getXmlFromObject(FACILITY_CASE_FORM_TEMPLATE_PATH, model);
         spyCommcareGateway.createFacilityCase(facilityInformation);
         verify(mockHttpClientService).post(spyCommcareGateway.getCOMMCARE_RECIEVER_URL(), getExpectedFacilityCaseXml());
 
     }
 
     @Test
     public void shouldCreateGroupIfNotExists() throws Exception {
 
         String[] newUsers = {"1", "2", "3"};
         String groupName = "All_Users";
         doReturn(null).when(mockAllGroups).getGroupByName(groupName);
         spyCommcareGateway.createOrUpdateGroup(groupName, newUsers);
         ArgumentCaptor<Group> groupCaptor = ArgumentCaptor.forClass(Group.class);
         verify(mockAllGroups).add(groupCaptor.capture());
 
         assertThat(groupCaptor.getValue().getUsers(), is(newUsers));
         assertThat(groupCaptor.getValue().getName(), is(groupName));
 
 
     }
 
     @Test
     public void shouldUpdateGroupIfExists() throws Exception {
 
         String[] newUsers = {"1", "2", "3"};
         String groupName = "All_Users";
         Group group = new Group();
         String[] existingUsers = {"4", "5"};
         group.setUsers(existingUsers);
         group.setName(groupName);
         doReturn(group).when(mockAllGroups).getGroupByName(groupName);
         spyCommcareGateway.createOrUpdateGroup(groupName, newUsers);
         ArgumentCaptor<Group> groupCaptor = ArgumentCaptor.forClass(Group.class);
         verify(mockAllGroups).update(groupCaptor.capture());
 
         assertThat(groupCaptor.getValue().getUsers(), is(ArrayUtils.addAll(existingUsers, newUsers)));
         assertThat(groupCaptor.getValue().getName(), is(groupName));
     }
 
     @Test
     public void shouldCreateOwnershipCase() {
        String ownerId = "ownerId";
        model.put(CommcareGateway.OWNER_ID_KEY, ownerId);
         String allUsersGrpId = "grp1";
         Group allUsersGroup = new Group();
         allUsersGroup.setId(allUsersGrpId);
 
         doReturn(allUsersGroup).when(mockAllGroups).getGroupByName(CommcareGateway.ALL_USERS_GROUP);
         doReturn(getExpectedOwnershipCaseXml()).when(spyCommcareGateway).getXmlFromObject(OWNERSHIP_CASE_REGISTER_FORM_TEMPLATE_PATH, model);
 
         spyCommcareGateway.createOwnershipCase();
         verify(mockHttpClientService).post(someUrl, getExpectedOwnershipCaseXml());
     }
 }
