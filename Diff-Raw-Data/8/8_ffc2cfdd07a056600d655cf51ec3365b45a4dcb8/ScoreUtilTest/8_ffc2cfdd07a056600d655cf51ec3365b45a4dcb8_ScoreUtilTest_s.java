 package de.consistec.jobad.util;
 
 import static de.consistec.jobad.domain.Attribute.*;
 import static de.consistec.jobad.domain.Experience.*;
 import static org.junit.Assert.assertEquals;
 import static org.mockito.Mockito.when;
 
 import java.util.Arrays;
 import java.util.Map;
 import java.util.Set;
 
 import org.junit.Test;
 import org.mockito.Mockito;
 
 import com.google.common.collect.Sets;
 
 import de.consistec.jobad.domain.Attribute;
 import de.consistec.jobad.domain.Developer;
 import de.consistec.jobad.domain.Experience;
 
 public class ScoreUtilTest {
 
   private Set<Attribute> positiveAttributes = Sets.newHashSet(
       ENTHUSIASTIC, LOVES_CLEAN_CODE, SOLUTION_ORIENTED, TEAMPLAYER, OPEN_MINDED);
 
   private Set<Attribute> negativeAttributes = Sets.newHashSet(JOB_HOPPER, LONER);
 
   private Set<Experience> plusFactors = Sets.newHashSet(JAVA2EE, MAVEN, EJB3);
 
   @Test
   public void testScoreCalculationForSingleDev() {
     Developer dev = Mockito.mock(Developer.class);
     Set<Attribute> attributes = Sets.newHashSet(
         ENTHUSIASTIC, LOVES_CLEAN_CODE, SOLUTION_ORIENTED, LONER);
        Set<Experience> experience = Sets.newHashSet(JAVA2EE, GWT, MAVEN);
     when(dev.getAttributes()).thenReturn(attributes);
     when(dev.getExperience()).thenReturn(experience);
 
     Map<Developer, Integer> devScore = ScoreUtil.calculateDevScore(Arrays.asList(dev),
         positiveAttributes,
         negativeAttributes,
         plusFactors);
     assertEquals(1, devScore.size());
     assertEquals(4, devScore.get(dev).intValue());
   }
 
   @Test
   public void testScoreCalculationForTwoDevs() {
     Developer dev1 = Mockito.mock(Developer.class);
     Developer dev2 = Mockito.mock(Developer.class);
     Set<Attribute> attributes1 = Sets.newHashSet(
         ENTHUSIASTIC, LOVES_CLEAN_CODE, TEST_DRIVEN, LONER);
        Set<Experience> experience1 = Sets.newHashSet(JAVA2EE, GWT);
     Set<Attribute> attributes2 = Sets.newHashSet(
         ENTHUSIASTIC, LOVES_CLEAN_CODE, TEAMPLAYER, LONER);
        Set<Experience> experience2 = Sets.newHashSet(GWT);
     when(dev1.getAttributes()).thenReturn(attributes1);
     when(dev1.getExperience()).thenReturn(experience1);
     when(dev2.getAttributes()).thenReturn(attributes2);
     when(dev2.getExperience()).thenReturn(experience2);
 
     Map<Developer, Integer> devScore = ScoreUtil.calculateDevScore(Arrays.asList(dev1, dev2),
         positiveAttributes,
         negativeAttributes,
         plusFactors);
     assertEquals(2, devScore.size());
     assertEquals(1, devScore.get(dev1).intValue());
     assertEquals(2, devScore.get(dev2).intValue());
   }
 
 }
