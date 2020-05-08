 package net.codjo.test.release.task.excel;
 import net.codjo.test.common.PathUtil;
 import net.codjo.test.release.TestEnvironment;
 import net.codjo.test.release.TestEnvironmentMock;
 import static net.codjo.test.release.task.AgfTask.BROADCAST_LOCAL_DIR;
 import static net.codjo.test.release.task.AgfTask.TEST_DIRECTORY;
 import java.io.File;
 import java.io.IOException;
 import org.apache.tools.ant.BuildException;
 import org.apache.tools.ant.Project;
 import org.junit.After;
 import static org.junit.Assert.assertTrue;
 import static org.junit.Assert.fail;
 import org.junit.Before;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 
 import com.googlecode.junit.ext.JunitExtRunner;
 import com.googlecode.junit.ext.RunIf;
 /**
  *
  */
 @RunWith(JunitExtRunner.class)
 @RunIf(value = ExcelChecker.class)
 public class AssertExcelTaskTest {
     private AssertExcelTask task;
 
 
     @Before
     public void setUp() throws Exception {
         Project project = new Project();
         project.setProperty(TEST_DIRECTORY, PathUtil.findResourcesFileDirectory(getClass()).getPath());
         project.setProperty(BROADCAST_LOCAL_DIR, PathUtil.findResourcesFileDirectory(getClass()).getPath());
         project.addReference(TestEnvironment.class.getName(), new TestEnvironmentMock(project));
 
         task = new AssertExcelTask();
         task.setProject(project);
         task.open();
     }
 
 
     @After
     public void tearDown() throws Exception {
         task.close();
     }
 
 
     @Test
     public void test_fileAlreadyOpenedInExcel_ok() throws Exception {
         openExcelFile("actual.xls");
 
         task.setExpected("expected_ok.xls");
         task.execute();
     }
 
 
     @Test
     public void test_fileAlreadyOpenedInExcel_notOk() throws Exception {
         openExcelFile("actual.xls");
 
         task.setExpected("expected_bad_sheets.xls");
 
         try {
             task.execute();
             fail();
         }
         catch (BuildException e) {
             assertTrue(e.getLocalizedMessage().startsWith(
                  "Les deux classeurs Excel ne contiennent pas les m\u0234mes feuilles"));
         }
     }
 
 
     private void openExcelFile(String excelFileName) throws IOException, InterruptedException {
         File actual = new File(PathUtil.findResourcesFileDirectory(getClass()), excelFileName);
         Process process = Runtime.getRuntime()
               .exec("rundll32.exe url.dll,FileProtocolHandler \"" + actual.getAbsolutePath() + "\"");
         process.waitFor();
     }
 }
