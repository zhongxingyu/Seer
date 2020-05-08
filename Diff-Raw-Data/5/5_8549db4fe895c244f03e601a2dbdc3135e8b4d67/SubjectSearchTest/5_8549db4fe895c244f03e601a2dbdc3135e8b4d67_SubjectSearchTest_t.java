 package gov.nih.nci.caintegrator.studyQueryService.test.germline;
 
 import gov.nih.nci.caintegrator.studyQueryService.germline.FindingsManager;
 import gov.nih.nci.caintegrator.domain.study.bean.StudyParticipant;
 
 import java.util.Collection;
 import java.util.Iterator;
 
 import junit.framework.Test;
 import junit.framework.TestSuite;
 
 /**
  * Author: Ram Bhattaru
  * Date:   Aug 16, 2006
  * Time:   8:05:49 AM
  */
 public class SubjectSearchTest extends GenotypeFindingTest {
 
     public void testSubjectSearch() {
         //setUpStudyParticipantAttributesCriteria();
         //setUpPopulationCriteria();
         //setUpAnalysisGroupCriteria();
         //executeSearch();
 
         Collection<String> caseStatus = FindingsManager.getGenotypeFindingQCStatus();
         System.out.println("Case Status: " + caseStatus);
 
         //FindingsManager.getCaseControlStatus();
 
        Collection<Integer> upperRangeLimits = FindingsManager.getAgeUpperLimitValues();
         System.out.println("Age UpperLimits: " + upperRangeLimits);
 
        Collection<Integer> lowerRangeLimits = FindingsManager.getAgeLowerLimitValues();
         System.out.println("Age LowerLimits: " + lowerRangeLimits );
 
     }
 
     private void executeSearch() {
         try {
             Collection<StudyParticipant> subjects = FindingsManager.getStudySubjects(spCrit, 0, 40 );
             System.out.println("Number of Subjects Retrieved: " + subjects.size());
             for (Iterator<StudyParticipant> iterator = subjects.iterator(); iterator.hasNext();) {
                 StudyParticipant subject =  iterator.next();
                 System.out.println("STUDY PARTICIPANT DE_IDENTIFIER ID: " +
                                     subject.getStudySubjectIdentifier());
                 System.out.println("GENDER: " + subject.getAdministrativeGenderCode());
                 System.out.println("AGE (PLEASE CONFIRM THIS - WHICH AGE -): " + subject.getAgeAtEnrollment());
                 System.out.println("AFFECTION STATUS: (PLEASE CONFIRM THIS -): " + subject.getSurvivalStatus());
                 System.out.println("FAMILY HISTORY: " + subject.getFamilyHistory());
                 System.out.println("Population Name: "+ subject.getPopulation().getName());
                 System.out.println("Enroll Age:" + subject.getAgeAtEnrollment().getAbsoluteValue());
             }
         } catch (Throwable t)  {
            System.out.println("CGEMS Exception in getting Study Subjects: " + t.toString());
            t.printStackTrace();
        }
     }
 
     public static Test suite() {
         TestSuite suit =  new TestSuite();
         suit.addTest(new TestSuite(SubjectSearchTest.class));
         return suit;
     }
 
 }
