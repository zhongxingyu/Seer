 package pl.softwaremill.timesheet_exporter.datacollector;
 
 import com.google.code.tinypmclient.Iteration;
 import com.google.code.tinypmclient.Project;
 import com.google.code.tinypmclient.TinyPM;
 import org.joda.time.DateTime;
 import org.joda.time.Days;
 import org.testng.annotations.BeforeMethod;
 import org.testng.annotations.DataProvider;
 import org.testng.annotations.Test;
 import pl.softwaremill.timesheet_exporter.settings.ExporterSettings;
 
 import java.util.Arrays;
 import java.util.List;
 
 import static org.fest.assertions.Assertions.assertThat;
 import static org.mockito.Mockito.mock;
 import static org.mockito.Mockito.when;
 
 public class TinyPMDataCollectorShould {
 
     private TinyPMDataCollector dataCollector;
     private TinyPM tinyPmMock;
 
     @BeforeMethod
     public void init() {
         tinyPmMock = mock(TinyPM.class);
 
         dataCollector = new TinyPMDataCollector(new ExporterSettings());
         dataCollector.tinyPM = tinyPmMock;
     }
 
     @Test(dataProvider = "dateRangesProvider")
     public void checkIfIterationIsInGivenMonth(int startYear, int startMonth, int endyear, int endMonth, int yearToCheck, int monthToCheck, boolean expectedResult) {
         // given
        Iteration iteration = prepareIterationInMonths(startYear, startMonth, endyear, endMonth);
 
         // when
         boolean result = dataCollector.iterationIsInAGivenYearAndMonth(iteration, yearToCheck, monthToCheck);
 
         // then
         assertThat(result).isEqualTo(expectedResult);
     }
 
     @DataProvider(name = "dateRangesProvider")
     public Object[][] provideData() {
         return new Object[][]{
                 {2012, 1, 2012, 1, 2012, 1, true},
                 {2012, 1, 2012, 10, 2012, 10, true},
                 {2012, 1, 2012, 10, 2012, 11, false},
                 {2012, 5, 2012, 5, 2012, 5, true},
                 {2012, 5, 2012, 5, 2012, 6, false},
                 {2012, 1, 2012, 12, 2012, 12, true},
                 {2012, 1, 2012, 12, 2012, 12, true},
                 {2012, 1, 2013, 11, 2013, 12, false},
                 {2012, 2, 2012, 12, 2012, 1, false},
         };
     }
 
    private Iteration prepareIterationInMonths(int startYear, int startMonth, int endYear, int endMonth) {
 
         DateTime startDate = new DateTime().withDate(startYear, startMonth, 1);
         DateTime endDate = new DateTime().withDate(endYear, endMonth, 1);
 
         int duration = Days.daysBetween(startDate, endDate).getDays();
        Iteration iteration = new Iteration();
         iteration.setStartDate(startDate.toDate());
         iteration.setDuration(duration + 1);
 
         return iteration;
     }
 
     @Test
     public void loadValidProjectCodes() {
 
         // given
         String code1 = "Project1";
         String code2 = "Project2";
         List<String> projectCodes = Arrays.asList(code1, code2);
 
         when(tinyPmMock.getAllProjects()).thenReturn(Arrays.asList(projectWithCode(code1), projectWithCode(code2)));
         when(tinyPmMock.getProject(code1)).thenReturn(projectWithCode(code1));
         when(tinyPmMock.getProject(code2)).thenReturn(projectWithCode(code2));
 
         // when
         List<Project> projects = dataCollector.loadRequestedProjects(projectCodes);
 
         // then
         assertThat(projects).onProperty("code").containsExactly(code1, code2);
     }
 
     @Test(expectedExceptions = RuntimeException.class,
             expectedExceptionsMessageRegExp = "Some projects were not found using given project codes. Available project codes are: \n" +
                     "Project1, Project2")
     public void failWhenLoadingOneInvalidProjectCode() {
 
         // given
         String code1 = "Project1";
         String code2 = "Project2";
         String invalidCode = "Invalid";
 
         List<String> projectCodes = Arrays.asList(code1, invalidCode);
 
         when(tinyPmMock.getAllProjects()).thenReturn(Arrays.asList(projectWithCode(code1), projectWithCode(code2)));
         when(tinyPmMock.getProject(code1)).thenReturn(projectWithCode(code1));
         when(tinyPmMock.getProject(code2)).thenReturn(projectWithCode(code2));
 
         // when
         List<Project> projects = dataCollector.loadRequestedProjects(projectCodes);
     }
 
     private Project projectWithCode(String code) {
         Project project = new Project();
         project.setCode(code);
         return project;
     }
 
 
 }
