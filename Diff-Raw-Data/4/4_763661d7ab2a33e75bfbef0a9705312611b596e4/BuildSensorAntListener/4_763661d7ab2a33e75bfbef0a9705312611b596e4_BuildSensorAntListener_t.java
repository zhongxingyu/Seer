 package org.hackystat.sensor.ant.antbuild;
 
 import java.util.ArrayList;
 import java.util.EmptyStackException;
 import java.util.Iterator;
 import java.util.Map;
 import java.util.Stack;
 import java.util.TreeMap;
 
 import javax.xml.datatype.XMLGregorianCalendar;
 
 import org.apache.tools.ant.BuildEvent;
 import org.apache.tools.ant.BuildException;
 import org.apache.tools.ant.BuildListener;
 import org.apache.tools.ant.Task;
 import org.hackystat.sensor.ant.util.LongTimeConverter;
 import org.hackystat.sensorshell.SensorProperties;
 import org.hackystat.sensorshell.SensorShell;
 import org.hackystat.sensorshell.usermap.SensorShellMap;
 
 /**
  * Ant build sensor. It's implemented as an ant listener. Note that whether is sensor is actually
  * enabled is determined by sensor.properties file.
  * 
  * @author (Cedric) Qin Zhang
  * @version $Id: BuildSensorAntListener.java,v 1.17 2005/10/28 04:09:59 qzhang Exp $
  */
 public class BuildSensorAntListener implements BuildListener {
 
   private boolean verbose;
   private boolean debug;
 
   private String tool;
   private String toolAccount;
 
   private SensorShell shell = null;
 
   /** Stack of task names. */
   private Stack<String> taskNameStack = new Stack<String>();
 
   // the following two stacks are always synchronized.
   /** List of messages from single tasks. */
   private Stack<ArrayList<String>> messagesStack = new Stack<ArrayList<String>>();
   /** Stack of target names. */
   private ArrayList<String> targetNameStack = new ArrayList<String>();
 
   //private BuildResult buildResult = new BuildResult();
 
   // build context
   private long startTimeMillis;
   private String configuration = "Unknown";
   private String startType = "Unknown";
   // private String keyValuePairs = ""; //TODO: this infomation not sent to the server.
   private String lastTargetName = "Unknown";
 
   /**
    * Constructs this instance. Note that the protect level is set to package private on purpose
    * (i.e. Ant cannot use reflection to instantiate this class), which prevents user to install this
    * listener through ant command line with "-l" option.
    * 
    * @param verbose Verbose mode.
    * @param debug Debug mode.
    * @param monitorCheckstyle True if we should treat checkstyle specially.
    * @param monitorCompilation True if we should treat javac specially.
    * @param monitorJUnit True if we should treat junit specially.
    * @param keyValueMap Key value map.
    * @param tool The tool in the UserMap file to use, null if not using UserMap.
    * @param toolAccount The tool account in the UserMap file to user, null if not using UserMap.
    */
   BuildSensorAntListener(boolean verbose, boolean debug, boolean monitorCheckstyle,
       boolean monitorCompilation, boolean monitorJUnit, Map<String, String> keyValueMap,
       String tool, String toolAccount) {
 
     this.verbose = verbose;
     this.debug = debug;
 
     this.tool = tool;
     this.toolAccount = toolAccount;
 
     this.processKeyValueMap(keyValueMap);
 
     try {
       if (isUsingUserMap()) {
         // get shell from SensorShellMap/UserMap
         SensorShellMap map = new SensorShellMap(this.tool);
         this.shell = map.getUserShell(this.toolAccount);
       }
       else {
         SensorProperties sensorProps = new SensorProperties();
         this.shell = new SensorShell(sensorProps, false, "Ant");
       }
     }
     catch (Exception e) {
       throw new BuildException(e.getMessage(), e);
     }
 
     this.startTimeMillis = System.currentTimeMillis();
   }
 
   /**
    * Processes key value pairs.
    * 
    * @param keyValueMap Key value pairs.
    */
   private void processKeyValueMap(Map<String, String> keyValueMap) {
     StringBuffer buffer = new StringBuffer(64);
     for (Iterator<Map.Entry<String, String>> i = keyValueMap.entrySet().iterator(); i.hasNext();) {
      Map.Entry<String, String> entry = i.next();
      String key = entry.getKey();
       String value = (String) entry.getValue();
       if ("configuration".equalsIgnoreCase(key)) {
         this.configuration = value;
       }
       else if ("buildStartType".equalsIgnoreCase(key)) {
         this.startType = value;
       }
       else {
         buffer.append(key).append('=').append(value).append(',');
       }
     }
 
 //    if (buffer.length() > 0) {
 //      // this.keyValuePairs = buffer.substring(0, buffer.length() - 1); //get rid of last ','
 //    }
   }
 
   /**
    * Logs debug message.
    * 
    * @param message The debug message.
    */
   private void logDebugMessage(String message) {
     if (this.debug) {
       System.out.print("[Ant Build Sensor DEBUG] ");
       System.out.println(message);
     }
   }
 
   /**
    * Callback function when ant starts the build. Not used in this class.
    * 
    * @param buildEvent The build event object.
    */
   public void buildStarted(BuildEvent buildEvent) {
     // We don't set build start time here, because this listener is installed through an ant
     // task, which means build has already started when this listener is installed.
   }
 
   /**
    * Callback function when ant finishes the build. It sends out the build sensor data if build
    * sensor is enabled.
    * 
    * @param buildEvent The build event object.
    */
   public void buildFinished(BuildEvent buildEvent) {
     long endTimeMillis = System.currentTimeMillis();
     String workingDirectory = buildEvent.getProject().getBaseDir().getAbsolutePath();
 
     Map<String, String> keyValMap = new TreeMap<String, String>();
     keyValMap.put("Tool", "Ant");
     XMLGregorianCalendar startTime = LongTimeConverter.convertLongToGregorian(this.startTimeMillis);
     keyValMap.put("Timestamp", startTime.toString());
     keyValMap.put("Resource", workingDirectory);
     keyValMap.put("SensorDataType", "Build");
     keyValMap.put("DevEvent-Type", "Build");
     keyValMap.put("Target", this.lastTargetName);
 
     // put result in the map
     if (buildEvent.getException() == null) {
       keyValMap.put("Result", "Success");      
     }
     else {
       keyValMap.put("Result", "Failure");
     }
 
     // optional
     XMLGregorianCalendar endTime = LongTimeConverter.convertLongToGregorian(endTimeMillis);
     keyValMap.put("EndTime", endTime.toString());
 
     System.out.println("Sending build result to Hackystat server... ");
     try {
       this.shell.add(keyValMap);
     }
     catch (Exception e) {
       e.printStackTrace();
       throw new BuildException("Error occurred adding data to SensorShell.", e);
     }
 
     if (this.shell.send() > 0) {
       System.out.print("Done!");
     }
     else {
       System.out.println("\nUnable to send build result.");
       System.out.println("\nBuild result is cached offline. It will be sent next time.");
     }
 
 //        try {
 //      long endTimeMillis = System.currentTimeMillis();
 //      String workingDirectory = buildEvent.getProject().getBaseDir().getAbsolutePath();
 //      BuildContext context = new BuildContext(this.startTimeMillis, endTimeMillis,
 //          this.configuration, this.startType, this.lastTargetName, workingDirectory);
 //
 //      // Handle possible generic exception here.
 //      // Record exception only if there is no other failure record to avoid duplication.
 //      Throwable exception = buildEvent.getException();
 //      if (this.buildResult.getBuildFailures().size() == 0 && exception != null) {
 //        this.buildResult.addFailureRecord("", "Generic", exception.getMessage());
 //      }
 //
 //      BuildReport buildReport = new BuildReport(context, this.buildResult);
 //
 //      // Remove this listener. Since we are listening messageLogged event, sometimes I get
 //      // logging infinite loop error if I send something to System.out or System.err.
 //      buildEvent.getProject().removeBuildListener(this);
 //
 //      // Send build result to Hackystat server.
 //      if (verbose) {
 //        System.out.println(buildReport.toString());
 //      }
 //      System.out.print("Sending build result to Hackystat server... ");
 //      if (buildReport.sendData(this.shell)) {
 //        System.out.println("Done!");
 //      }
 //      else {
 //        System.out.println("\nUnable to send build result.");
 //        System.out.println("\nBuild result is cached offline. It will be sent next time.");
 //      }
 //    }
 //    catch (Exception ex) {
 //      Project project = buildEvent.getProject();
 //      project.log("[Ant Build Sensor] Error detected.", Project.MSG_ERR);
 //      project.log("[Ant Build Sensor] Error message is: " + ex.getMessage(), Project.MSG_ERR);
 //      project.log("[Ant Build Sensor] Please report error to Hackystat group.", Project.MSG_ERR);
 //    }
   }
 
   /**
    * Callback function when ant starts a build target. It's used to record last ant target invoked.
    * 
    * @param buildEvent The build event object.
    */
   public void targetStarted(BuildEvent buildEvent) {
     String targetName = buildEvent.getTarget().getName();
     this.targetNameStack.add(targetName);
     this.logDebugMessage("TargetStarted - " + targetName);
   }
 
   /**
    * Callback function when ant finishes a build target.
    * 
    * @param buildEvent The build event object.
    */
   public void targetFinished(BuildEvent buildEvent) {
     String targetName = buildEvent.getTarget().getName();
     this.logDebugMessage("TargetFinished - " + targetName);
 
     int size = this.targetNameStack.size();
     if (size > 0) {
       this.targetNameStack.remove(size - 1);
     }
 
     // TODO: This scheme to get top level build target works ok when build is successful
     // But if build failed, you only get the last target invoked by ANT.
     // This is a possible problem to handle in the next release.
     if (targetName != null) {
       this.lastTargetName = targetName;
     }
   }
 
   /**
    * Callback function when ant starts a build task. Not used in this class.
    * 
    * @param buildEvent The build event object.
    */
   public void taskStarted(BuildEvent buildEvent) {
     // System.out.println("==>TaskStarted: " + buildEvent.getTask().getTaskName());
     Task task = buildEvent.getTask();
     String taskName = task.getTaskName();
     this.logDebugMessage("TaskStarted - " + taskName);
     this.taskNameStack.push(taskName);
     this.messagesStack.push(new ArrayList<String>());
 
     //    if (this.isTask(task, TASK_CHECKSTYLE)) {
     //      this.buildResult.setCheckstyleRan();
     //    }
     //    else if (this.isTask(task, TASK_COMPILATION)) {
     //      this.buildResult.setCompilationRan();
     //    }
     //    else if (this.isTask(task, TASK_JUNIT)) {
     //      this.buildResult.setUnitTestRan();
     //    }
   }
 
   /**
    * Callback function when ant finishes a build task.
    * 
    * @param buildEvent The build event object.
    */
   public void taskFinished(BuildEvent buildEvent) {
     String taskName = buildEvent.getTask().getTaskName();
     this.logDebugMessage("TaskFinished - " + taskName + ";  error = "
         + (buildEvent.getException() != null));
 
     // when you install the listener in a task, you will never be able to get that TaskStart event.
     // The first event you hear is TaskFinished, this is to handle this special case.
     if (this.taskNameStack.isEmpty()) {
       return;
     }
 
     // In version 6 build script, javac is executed in local.build.xml, all junit and checkstyle
     // are executed in build.util.xml file (in hackyBuild). Therefore, only for compilation task
     // the workingDirectory is correct. For junit and checkstyle task, the working directory is
     // always hackyBuild.
     // In version 7 hackystat build script, the working diretory will always be hackyBuild/version7.
     // String workingDirectory = buildEvent.getProject().getBaseDir().getAbsolutePath();
 
     this.taskNameStack.pop();
     ArrayList<String> messages = this.messagesStack.pop();
 
     Task task = buildEvent.getTask();
     Throwable exception = buildEvent.getException();
 
     //    if (this.isTask(task, TASK_CHECKSTYLE) && (this.monitorCheckstyle)) {
     //      String moduleName = this.guessModuleName(TASK_CHECKSTYLE);
     //      CheckstyleOutputParser parser = new CheckstyleOutputParser(messages);
     //      List failureRecords = parser.getFailureRecords();
     //      // record checkstyle errors
     //      for (Iterator i = failureRecords.iterator(); i.hasNext();) {
     //        CheckstyleFailureRecord record = (CheckstyleFailureRecord) i.next();
     //        String msg = record.getFileName() + "::" + record.getLineNumber() + "::"
     //            + record.getMessage();
     //        this.buildResult.addFailureRecord(moduleName, "Checkstyle", msg);
     //      }
     //      // in case we are unable to parse checkstyle output, we still need to record error.
     //      if (failureRecords.size() == 0 && exception != null) {
     //        this.buildResult.addFailureRecord(moduleName, "Checkstyle", null);
     //      }
     //    }
     //    else if (this.isTask(task, TASK_COMPILATION) && (this.monitorCompilation)) {
     //      String moduleName = this.guessModuleName(TASK_COMPILATION);
     //      CompilationOutputParser parser = new CompilationOutputParser(messages);
     //      List failureRecords = parser.getFailureRecords();
     //      // record compilaton errors
     //      for (Iterator i = failureRecords.iterator(); i.hasNext();) {
     //        CompilationFailureRecord record = (CompilationFailureRecord) i.next();
     //        String msg = record.getFileName() + "::" + record.getLineNumber() + "::"
     //            + record.getMessage();
     //        this.buildResult.addFailureRecord(moduleName, "Compilation", msg);
     //      }
     //      // in case we are unable to parse compilation output, we still need to record error.
     //      if (failureRecords.size() == 0 && exception != null) {
     //        this.buildResult.addFailureRecord(moduleName, "Compilation", null);
     //      }
     //    }
     //    else if (this.isTask(task, TASK_JUNIT) && (this.monitorJUnit)) {
     //      String moduleName = this.guessModuleName(TASK_JUNIT);
     //      JUnitOutputParser parser = new JUnitOutputParser(messages);
     //      List failureRecords = parser.getFailureRecords();
     //      // record junit errors
     //      for (Iterator i = failureRecords.iterator(); i.hasNext();) {
     //        JUnitFailureRecord record = (JUnitFailureRecord) i.next();
     //        StringBuffer messageBuffer = new StringBuffer(record.getClassName());
     //        if (record.getMethodName() != null) {
     //          messageBuffer.append("::" + record.getMethodName());
     //        }
     //        if (record.getMessage() != null) {
     //          messageBuffer.append(":: " + record.getMessage());
     //        }
     //        this.buildResult.addFailureRecord(moduleName, "JUnit", messageBuffer.toString());
     //      }
     //      // in case we are unable to parse junit output, we still need to record error.
     //      if (failureRecords.size() == 0 && exception != null) {
     //        this.buildResult.addFailureRecord(moduleName, "JUnit", null);
     //      }
     //    }
     //    else {
     //      // We might encountered a generic error, if exception != null.
     //      // We only record the exception once to avoid duplicated build error entries.
     //      if (this.buildResult.getBuildFailures().size() == 0 && exception != null) {
     //        this.buildResult.addFailureRecord(this.guessModuleName(TASK_UNKNOWN), "Generic",
     //                                          buildEvent.getException().getMessage());
     //      }
     //    }
   }
 
   /**
    * Callback function when ant logs a message.
    * 
    * @param buildEvent The build event object.
    */
   public void messageLogged(BuildEvent buildEvent) {
     try {
       Task task = buildEvent.getTask();
       if (task != null && task.getTaskName().equals(this.taskNameStack.peek())) {
         String message = buildEvent.getMessage();
         if (message != null) {
           ArrayList<String> list = this.messagesStack.peek();
           list.add(message);
         }
       }
     }
     catch (EmptyStackException ex) {
       // The TaskStarted event for the "task" in which this message is issued was not caught
       // by this Ant listener, therefore, the stack is empty and we do nothing.
       // The asymmetry is caused by the fact this listener is installed using an ant task!
 
       // FindBugs does not approve of this empty catch
       ex.getMessage();
     }
   }
 
 //  private static final int TASK_UNKNOWN = -1;
 //  private static final int TASK_CHECKSTYLE = 1;
 //  private static final int TASK_COMPILATION = 2;
 //  private static final int TASK_JUNIT = 3;
 //
 //  /**
 //   * Determines if the task is the type of task specified. Note that this method assumes certain
 //   * knowledge, such as compilation task is called "javac". This may not be the case everywhere.
 //   * 
 //   * @param task An ant task.
 //   * @param taskType Task type.
 //   * @return True if the task is of the specified type.
 //   */
 //  private boolean isTask(Task task, int taskType) {
 //    if (task != null) {
 //      String taskName = task.getTaskName().toLowerCase();
 //      if (taskType == TASK_CHECKSTYLE) {
 //        return "checkstyle".equals(taskName);
 //      }
 //      else if (taskType == TASK_COMPILATION) {
 //        // hackystat presetdef "javac" with "hackyBuild.javac" in build.xml
 //        // As a result, we can no long listen for javac; we have to listen for hackyBuild.javac
 //        return "javac".equals(taskName) || taskName.endsWith(".javac");
 //      }
 //      else if (taskType == TASK_JUNIT) {
 //        return "junit".equals(taskName);
 //      }
 //      else {
 //        throw new RuntimeException("Ant build sensor, internal assertion failed, code 1, "
 //            + "taskName=" + taskName + ", taskType=" + taskType);
 //      }
 //    }
 //    else {
 //      return false;
 //    }
 //  }
 
   /**
    * Guess the module name for the specified task. Note that this method is very hackystat specific,
    * and it depends on how the build files are constructed. Be very careful. Probably in the future
    * we need to find a way not to hard code the knowledge.
    * 
    * @param taskType The task type.
    * @return The module name. If we cannot guess the module name, an empty string is returned. Note
    *         that null is never returned.
    */
   private String guessModuleName(int taskType) {
     if (this.debug) {
       System.out.print("[ANT DEBUG TARGET NAME STACK] ");
       for (int i = this.targetNameStack.size() - 1; i >= 0; i--) {
         System.out.print(this.targetNameStack.get(i));
         System.out.print(";  ");
       }
       System.out.println();
     }
 
     // Note if compile task is call by hackyXXX.shit, then there is exception.
     // e.g. the overly complex and nightmarish CGQM module.
     // String moduleName = "";
     // for (int i = this.targetNameStack.size() - 1; i >= 0; i--) {
     // String targetName = (String) this.targetNameStack.get(i);
     // if (targetName.startsWith("hacky")) {
     // if (taskType == TASK_CHECKSTYLE && targetName.endsWith(".checkstyle")) {
     // moduleName = targetName.substring(0, targetName.length() - 11);
     // break;
     // }
     // else if (taskType == TASK_COMPILATION && targetName.endsWith(".compile")) {
     // moduleName = targetName.substring(0, targetName.length() - 8);
     // break;
     // }
     // else if (taskType == TASK_JUNIT && targetName.endsWith(".junit")) {
     // moduleName = targetName.substring(0, targetName.length() - 6);
     // break;
     // }
     // else if (taskType == TASK_UNKNOWN) {
     // int firstDotIndex = targetName.indexOf('.');
     // if (firstDotIndex > 0) {
     // moduleName = targetName.substring(0, firstDotIndex);
     // break;
     // }
     // }
     // else {
     // throw new RuntimeException("Ant build sensor, internal assertion failed, code 2, "
     // + "taskType=" + taskType + ", targetName=" + targetName);
     // }
     // } //outer if
     // }
 
     // If hackyXXX.A calls hackyYYY.A, which inturn calls javac
     // then the module name = "hackyYYY" which may or may not be what you want.
     String moduleName = "";
     for (int i = this.targetNameStack.size() - 1; i >= 0; i--) {
       String targetName = (String) this.targetNameStack.get(i);
       if (targetName.startsWith("hacky")) {
         int firstDotIndex = targetName.indexOf('.');
         if (firstDotIndex > 0) {
           moduleName = targetName.substring(0, firstDotIndex);
           break;
         }
       } // outer if
     }
     return moduleName;
   }
   
   /**
    * Gets whether or not this sensor instance is using a mapping in the UserMap.
    * 
    * @return Returns true of the tool and tool account are set, otherwise false.
    */
   private boolean isUsingUserMap() {
     return (this.tool != null && this.toolAccount != null);
   }
 }
