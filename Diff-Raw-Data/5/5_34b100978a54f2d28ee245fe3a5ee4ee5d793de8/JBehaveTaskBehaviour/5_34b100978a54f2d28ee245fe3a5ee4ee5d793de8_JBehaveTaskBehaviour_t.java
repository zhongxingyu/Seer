 /*
  * Created on 19-Jul-2004
  * 
  * (c) 2003-2004 ThoughtWorks
  * 
  * See license.txt for licence details
  */
 package jbehave.ant;
 
 import jbehave.core.Run;
 import jbehave.core.Block;
 import jbehave.core.minimock.UsingMiniMock;
 import jbehave.core.minimock.Constraint;
 
 import org.apache.tools.ant.Project;
 import org.apache.tools.ant.Task;
 import org.apache.tools.ant.BuildException;
 import org.apache.tools.ant.types.Path;
 import java.io.File;
 import java.util.Arrays;
 import java.util.List;
 import java.util.Collection;
 
 import net.sf.cotta.utils.ClassPathLocator;
 import net.sf.cotta.utils.ClassPath;
 
 
 public class JBehaveTaskBehaviour extends UsingMiniMock {
     private JBehaveTask task;
     private StubCommandRunner runner = new StubCommandRunner();
 
     public void setUp() {
         task = new JBehaveTask(runner);
         Project project = new Project();
         project.setCoreLoader(getClass().getClassLoader());
         task.setProject(project);
         Path path = task.createClasspath();
         addToPathContains(path, getClass());
         addToPathContains(path, Run.class);
     }
 
     private void addToPathContains(Path path, Class aClass) {
         ClassPathLocator behaviourClassPathLocator = new ClassPathLocator(aClass);
         path.createPathElement().setLocation(new File(behaviourClassPathLocator.locate().path()));
     }
 
     public void shouldRunASingleBehaviourClass() throws Exception {
         BehaviourClassDetails behaviourClass = task.createVerify();
         behaviourClass.setName(BehaviourClassOne.class.getName());
         runner.valueToReturn = 0;
 
         task.execute();
 
         ensureThat(runner.taskLog, sameInstanceAs(task));
         String[] actualCommand = runner.commandLineLog;
        ensureThat(actualCommand[0].contains("java"), eq(true));
         List list = Arrays.asList(actualCommand);
         ensureThat(list, collectionContains(BehaviourClassOne.class.getName()));
     }
 
     private Constraint collectionContains(final Object item) {
         return new Constraint() {
             public boolean matches(Object arg) {
                 return ((Collection)arg).contains(item);
             }
 
             public String toString() {
                return "Collection that contains <" + item + ">";
             }
         };
     }
 
     public void shouldRunMultipleBehaviourClasses() throws Exception {
         BehaviourClassDetails spec = task.createVerify();
         spec.setName(BehaviourClassOne.class.getName());
         BehaviourClassDetails spec2 = task.createVerify();
         spec2.setName(BehaviourClassTwo.class.getName());
         runner.valueToReturn = 0;
 
         task.execute();
 
         List list = Arrays.asList(runner.commandLineLog);
         ensureThat(list, collectionContains(BehaviourClassOne.class.getName()));
         ensureThat(list, collectionContains(BehaviourClassTwo.class.getName()));
     }
 
     public void shouldUseClasspathFromClasspathElement() throws Exception {
         Path path = task.createClasspath();
         Path.PathElement element = path.createPathElement();
         ClassPath classPath = new ClassPathLocator(String.class).locate();
         String pathToRuntimeJar = classPath.path();
         element.setPath(pathToRuntimeJar);
         task.createVerify().setName(BehaviourClassOne.class.getName());
 
         task.execute();
 
         List list = Arrays.asList(runner.commandLineLog);
         int classPathSwitchElement = list.indexOf("-classpath");
         ensureThat(classPathSwitchElement, not(eq(-1)));
         String classPaths = (String) list.get(classPathSwitchElement + 1);
         String[] classPathArray = classPaths.split(File.pathSeparator);
         ensureThat(Arrays.asList(classPathArray), collectionContains(pathToRuntimeJar));
     }
 
     public void shouldFailTheBuildWhenVerificationFails() throws Exception {
         final String behaviourClassName = FailingBehaviourClass.class.getName();
         task.createVerify().setName(behaviourClassName);
         runner.valueToReturn = 1;
 
         ensureThrows(BuildException.class, new Block() {
             public void run() throws Exception {
                 task.execute();
             }
         });
     }
 
 /* TODO
     public void shouldFailTheBuildWhenFirstSpecFails() throws Exception {
         // setup
         task.createVerify().setName("jbehave.extensions.ant.FailingSpec");
         task.createVerify().setName("jbehave.extensions.ant.SpecOne");
         BehaviourClassOne.wasCalled = false; // i hate this!
 
         // execute
         ensureThrows(BuildException.class, new Block() {
             public void run() {
                 task.execute();
             }
         });
 
         // verify
         Ensure.that("SpecOne should not have been run", !BehaviourClassOne.wasCalled);
     }
 
 */
 
     private static class StubCommandRunner implements CommandRunner {
         private int valueToReturn;
         private Task taskLog;
         private String[] commandLineLog;
 
         public int fork(Task task, String[] commandline) {
             taskLog = task;
             commandLineLog = commandline;
             return valueToReturn;
         }
     }
 }
