 package org.sukrupa.app.students;
 
 import org.junit.Before;
 import org.junit.Test;
 import org.mockito.Mock;
 import org.sukrupa.student.StudentReferenceData;
 import org.sukrupa.student.Talent;
 import org.sukrupa.student.TalentRepository;
 
 import java.util.Arrays;
 import java.util.Collections;
 import java.util.List;
 
 import static java.util.Arrays.asList;
 import static org.hamcrest.MatcherAssert.assertThat;
 import static org.hamcrest.Matchers.is;
 import static org.mockito.Mockito.when;
 import static org.mockito.MockitoAnnotations.initMocks;
 import static org.sukrupa.platform.hamcrest.CollectionMatchers.hasOnly;
 import static org.sukrupa.student.TalentBuilder.talent;
 
 public class StudentReferenceDataTest {
 
     @Mock
     TalentRepository talentRepository;
 
     @Before
     public void setUp() {
         initMocks(this);
     }
 
 
     @Test
     public void shouldGetTheTalentsFromTheRepository() {
         StudentReferenceData studentReferenceData = new StudentReferenceData(talentRepository);
        when(talentRepository.listAllTalents()).thenReturn(asList(talent("talent1"),
                                                                   talent("talent2"),
                                                                   talent("talent3")));
 
         List<String> talentDescriptions = studentReferenceData.getTalentDescriptions();
 
         assertThat(talentDescriptions, hasOnly("talent1", "talent2", "talent3"));
     }
 
     @Test
     public void shouldReturnStaticTalentsIfNothingInTheRepository() {
         StudentReferenceData studentReferenceData = new StudentReferenceData(talentRepository);
         when(talentRepository.listAllTalents()).thenReturn(Collections.<Talent>emptyList());
 
         List<String> talentDescriptions = studentReferenceData.getTalentDescriptions();
 
         assertThat(talentDescriptions.size(), is(12));
     }
 
     @Test
     public void shouldWorkWithoutATalentRepository() {
         StudentReferenceData studentReferenceData = new StudentReferenceData();
 
         List<String> talentDescriptions = studentReferenceData.getTalentDescriptions();
 
         assertThat(talentDescriptions.size(), is(12));
     }
 
 
 }
