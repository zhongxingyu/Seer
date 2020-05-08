 package org.jboss.tools.drools.ui.bot.test.functional;
 
 import org.apache.log4j.Logger;
 import org.jboss.reddeer.eclipse.ui.console.ConsoleView;
 import org.jboss.reddeer.eclipse.ui.perspectives.JavaPerspective;
 import org.jboss.reddeer.junit.runner.RedDeerSuite;
 import org.jboss.reddeer.swt.wait.WaitUntil;
 import org.jboss.tools.drools.reddeer.dialog.DroolsRuntimeDialog;
 import org.jboss.tools.drools.reddeer.preference.DroolsRuntimesPreferencePage;
 import org.jboss.tools.drools.reddeer.wizard.NewDroolsProjectWizard;
 import org.jboss.tools.drools.ui.bot.test.annotation.UsePerspective;
 import org.jboss.tools.drools.ui.bot.test.util.ApplicationIsTerminated;
 import org.jboss.tools.drools.ui.bot.test.util.RunUtility;
 import org.jboss.tools.drools.ui.bot.test.util.TestParent;
 import org.junit.Assert;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 
 @RunWith(RedDeerSuite.class)
 public class DefaultRuntimeTest extends TestParent {
 	
 	private static final Logger LOGGER = Logger.getLogger(RulesManagementTest.class);
    private static final String DEBUG_REGEX = "(SLF4J: .*[\r\n]+)+?" +
            "(kmodules: file:(/.*)+/kmodule.xml[\r\n]+)?";
    private static final String SUCCESSFUL_RUN_REGEX = DEBUG_REGEX + "Hello World[\r\n]+Goodbye cruel world[\r\n]+";
 	
 	@Test
     @UsePerspective(JavaPerspective.class)
     public void testRunRulesWithDefaultRuntime() {
         final String runtimeName = "testRunRulesWithDefaultRuntime";
         final String projectName = "testRunRulesWithDefaultRuntime";
         DroolsRuntimesPreferencePage pref = new DroolsRuntimesPreferencePage();
         pref.open();
         DroolsRuntimeDialog dialog = pref.addDroolsRuntime();
         dialog.setName(runtimeName);
         dialog.createNewRuntime(createTempDir("testRunRulesWithDefaultRuntime"));
         dialog.ok();
         pref.setDroolsRuntimeAsDefault(runtimeName);
         pref.okCloseWarning();
 
         NewDroolsProjectWizard wiz = new NewDroolsProjectWizard();
         wiz.createDefaultProjectWithAllSamples(projectName);
 
         ConsoleView console = new ConsoleView();
         console.open();
 
         RunUtility.runAsJavaApplication(projectName, "src/main/java", "com.sample", "DroolsTest.java");
         new WaitUntil(new ApplicationIsTerminated());
         waitASecond(); // this is quite annoying - the text is updated AFTER the application is terminated
 
         console.open();
         String consoleText = console.getConsoleText();
         Assert.assertNotNull("Console text was empty.", consoleText);
         LOGGER.debug(consoleText);
         Assert.assertTrue("Unexpected text in console\n" + consoleText, consoleText.matches(SUCCESSFUL_RUN_REGEX));
     }
 }
