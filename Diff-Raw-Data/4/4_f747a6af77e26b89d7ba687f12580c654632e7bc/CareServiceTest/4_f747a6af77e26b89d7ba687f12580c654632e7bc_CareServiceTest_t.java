 package org.motechproject.care.reporting.service;
 
 import org.junit.Before;
 import org.junit.Test;
 import org.mockito.Mock;
 import org.motechproject.care.reporting.domain.dimension.ChildCase;
 import org.motechproject.care.reporting.domain.dimension.Flw;
 import org.motechproject.care.reporting.domain.dimension.FlwGroup;
 import org.motechproject.care.reporting.domain.dimension.MotherCase;
 import org.motechproject.care.reporting.domain.measure.NewForm;
 import org.motechproject.care.reporting.repository.Repository;
 
 import static junit.framework.Assert.assertEquals;
 import static org.mockito.Mockito.verify;
 import static org.mockito.Mockito.when;
 import static org.mockito.MockitoAnnotations.initMocks;
 import static org.motechproject.care.reporting.utils.TestUtils.assertReflectionEqualsWithIgnore;
 
 public class CareServiceTest {
 
     @Mock
     Repository dbRepository;
 
     CareService service;
 
     @Before
     public void setUp(){
         initMocks(this);
         service = new CareService(dbRepository);
     }
 
     @Test
     public void shouldReturnMotherIfExistsInRepository(){
         MotherCase expectedMotherCase = new MotherCase();
         expectedMotherCase.setCaseId("1");
 
         when(dbRepository.get(MotherCase.class, "caseId", "1")).thenReturn(expectedMotherCase);
 
         MotherCase actualMotherCase = service.getMotherCase("1");
 
         assertEquals(expectedMotherCase, actualMotherCase);
     }
 
     @Test
     public void shouldReturnNewMotherIfNotExistsInRepository(){
         MotherCase expectedMotherCase = new MotherCase();
         expectedMotherCase.setCaseId("1");
 
         when(dbRepository.get(MotherCase.class, "caseId", "1")).thenReturn(null);
 
         MotherCase actualMotherCase = service.getMotherCase("1");
 
         assertReflectionEqualsWithIgnore(expectedMotherCase, actualMotherCase, new String[]{"creationTime"});
     }
 
     @Test
     public void shouldReturnChildIfExistsInRepository(){
         ChildCase expectedChildCase = new ChildCase();
         expectedChildCase.setCaseId("1");
 
         when(dbRepository.get(ChildCase.class, "caseId", "1")).thenReturn(expectedChildCase);
 
         ChildCase actualChildCase = service.getChildCase("1");
 
         assertEquals(expectedChildCase, actualChildCase);
     }
 
     @Test
     public void shouldReturnNewChildIfNotExistsInRepository(){
         ChildCase expectedChildCase = new ChildCase();
         expectedChildCase.setCaseId("1");
 
         when(dbRepository.get(MotherCase.class, "caseId", "1")).thenReturn(null);
 
         ChildCase actualChildCase = service.getChildCase("1");
 
        assertReflectionEqualsWithIgnore(expectedChildCase, actualChildCase, new String[]{"creationTime"});
     }
 
     @Test
     public void shouldReturnFlwIfExistsInRepository(){
         Flw expectedFlw = new Flw();
         expectedFlw.setFlwId("1");
 
         when(dbRepository.get(Flw.class, "flwId", "1")).thenReturn(expectedFlw);
 
         Flw actualFlw = service.getFlw("1");
 
         assertEquals(expectedFlw, actualFlw);
     }
 
     @Test
     public void shouldReturnNewFlwIfNotExistsInRepository(){
         Flw expectedFlw = new Flw();
         expectedFlw.setFlwId("1");
 
         when(dbRepository.get(Flw.class, "flwId", "1")).thenReturn(null);
 
         Flw actualFlw = service.getFlw("1");
 
         assertReflectionEqualsWithIgnore(expectedFlw, actualFlw, new String[]{"creationTime"});
     }
 
     @Test
     public void shouldSaveInstance(){
         NewForm newForm = new NewForm();
         newForm.setFullName("fullName");
 
         service.save(newForm);
 
         verify(dbRepository).save(newForm);
 
     }
 
     @Test
     public void shouldGetGroupIfExists(){
         String fieldName = "groupId";
         String fieldValue = "groupId";
         FlwGroup flwGroup = new FlwGroup();
         when(dbRepository.get(FlwGroup.class, fieldName, fieldValue)).thenReturn(flwGroup);
 
         FlwGroup actualGroup = service.getGroup(fieldValue);
 
         verify(dbRepository).get(FlwGroup.class, fieldName, fieldValue);
         assertEquals(flwGroup, actualGroup);
     }
 
     @Test
     public void shouldGetNewGroupIfItDoesNotExist(){
         String fieldName = "groupId";
         String fieldValue = "groupId";
         when(dbRepository.get(FlwGroup.class, fieldName, fieldValue)).thenReturn(null);
 
         FlwGroup actualGroup = service.getGroup(fieldValue);
 
         verify(dbRepository).get(FlwGroup.class, fieldName, fieldValue);
         assertEquals(fieldValue, actualGroup.getGroupId());
     }
 
 
     @Test
     public void shouldGetACaseIfExists(){
         String fieldName = "fieldName";
         String fieldValue = "fieldValue";
         MotherCase motherCase = new MotherCase();
         when(dbRepository.get(MotherCase.class, fieldName, fieldValue)).thenReturn(motherCase);
 
         MotherCase actualMotherCase = service.get(MotherCase.class, fieldName, fieldValue);
 
         verify(dbRepository).get(MotherCase.class, fieldName, fieldValue);
         assertEquals(motherCase, actualMotherCase);
     }
 
     @Test
     public void shouldGetANewCaseIfItDoesNotExist(){
         String fieldName = "caseId";
         String fieldValue = "fieldValue";
         when(dbRepository.get(MotherCase.class, fieldName, fieldValue)).thenReturn(null);
 
         MotherCase actualMotherCase = service.get(MotherCase.class, fieldName, fieldValue);
 
         verify(dbRepository).get(MotherCase.class, fieldName, fieldValue);
         assertEquals(fieldValue, actualMotherCase.getCaseId());
     }
 }
