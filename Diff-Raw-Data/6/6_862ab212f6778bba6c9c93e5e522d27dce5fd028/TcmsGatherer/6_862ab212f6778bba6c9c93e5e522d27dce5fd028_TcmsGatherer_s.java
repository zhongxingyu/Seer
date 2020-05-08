 /*
  * Copyright (C) 2012 Red Hat, Inc.     
  * 
  * This copyrighted material is made available to anyone wishing to use, 
  * modify, copy, or redistribute it subject to the terms and conditions of the 
  * GNU General Public License v.2.
  * 
  * Authors: Adam Saleh (asaleh at redhat dot com)
  *          Jan Rusnacko (jrusnack at redhat dot com)
  */
 package NitrateIntegration;
 
 import NitrateIntegration.CommandWrapper.CommandWrapper;
 import com.redhat.engineering.jenkins.testparser.results.MethodResult;
 import com.redhat.engineering.jenkins.testparser.results.TestResults;
 import com.redhat.nitrate.TcmsCommand;
 import com.redhat.nitrate.command.Build;
 import com.redhat.nitrate.command.Env.Value;
 import com.redhat.nitrate.command.TestCase;
 import com.redhat.nitrate.command.TestCaseRun;
 import com.redhat.nitrate.command.TestRun;
 import hudson.model.AbstractBuild;
 import java.io.IOException;
 import java.util.*;
 
 /**
  *
  * @author asaleh
  */
 public class TcmsGatherer implements Iterable<CommandWrapper>{
 
     private int run_id;
     private int build_id;
     private TcmsProperties properties;
     private CommandWrapper build_s;
     private TcmsEnvironment environment;
     
     private LinkedList<CommandWrapper> list = new LinkedList<CommandWrapper>();
     private HashMap<String, LinkedList<CommandWrapper>> commands_sorted = new HashMap<String, LinkedList<CommandWrapper>>();
 
     public TcmsGatherer(TcmsProperties properties, TcmsEnvironment env) {
         this.properties = properties;
         this.build_s = null;
         this.environment = env;
     }
 
     public void setProperties(TcmsProperties properties) {
         this.properties = properties;
     }
 
     public void setEnvironment(TcmsEnvironment environment) {
         this.environment = environment;
     }
     
 
     private static TestCase.create tcmsCreateCase(MethodResult result, TcmsProperties properties) {
         TestCase.create create = new TestCase.create();
         create.product = properties.getProductID();
         create.category = properties.getCategoryID();
         create.priority = properties.getPriorityID();
         create.summary = "";
         if (result.getParent() != null) {
             create.summary += result.getParent().getName() + ".";
         }
         if (result.getParameters() != null) {
             String args = "";
             for (String param : result.getParameters()) {
                 args += param + " ";
             }
             create.arguments = args;
         }

         create.summary += result.getDisplayName();
         create.plan = properties.getPlanID();
         create.is_automated = 1;
         return create;
     }
 
     private static Build.create tcmsCreateBuild(AbstractBuild build, TcmsProperties properties) {
         Build.create create = new Build.create();
         create.product = properties.getProductID();
         create.name = build.getId();
         create.description = build.getDescription();
         return create;
     }
 
     private static TestRun.create tcmsCreateRun(AbstractBuild run, TcmsProperties properties, Map<String, String> variables) {
         TestRun.create create = new TestRun.create();
         create.product = properties.getProductID();
         create.product_version = properties.getProduct_vID();
         create.plan = properties.getPlanID();
         create.build = -1;
         create.manager = properties.getManagerId();
         create.summary = "Build " + run.getDisplayName();
 
         if (variables != null && variables.isEmpty() == false) {
             for (Iterator it = variables.entrySet().iterator(); it.hasNext();) {
                 Map.Entry<String, String> e = (Map.Entry<String, String>) it.next();
                 create.summary += ", " + e.getKey() + "=" + e.getValue();
             }
         }
 
         return create;
     }
 
     private static TestCaseRun.create tcmsCreateCaseRun(MethodResult result, int status) {
         TestCaseRun.create c = new TestCaseRun.create();
         c.run = -1;
         c.caseVar = -1;
         c.build = -1;
         c.case_run_status = status;
         if (result.getException() != null) {
             c.notes = result.getException().getExceptionName();
             c.notes += "\n\n";
             c.notes += result.getException().getStackTrace();
             c.notes += "\n\n";
             c.notes += result.getException().getMessage();
 
         }
 
         if (result.getParameters() != null) {
             Object parameters = result.getParameters();
         }
         return c;
     }
 
     private static TestRun.link_env_value tcmsLinkValue(TcmsEnvironment env, String property, String value) {
         if (env.isEmpty()) {
             return null;
         }
 
         TestRun.link_env_value c = new TestRun.link_env_value();
         c.run_id = -1;
         Hashtable<String, Value> prop = env.getValues().get(property);
         if (prop != null) {
             if (prop.containsKey(value)) {
                 c.env_value_id = prop.get(value).id;
                 return c;
             }
         }
         return null;
     }
     
     private static TestRun.update tcmsUpdateRunStatusToFinished(){
         TestRun.update u = new TestRun.update();
         u.id = -1;
         u.values.status = 1;
         return u;
     }
 
     private void CreateTestCaseRun(MethodResult result, int status, CommandWrapper run, CommandWrapper build) {
 
         CommandWrapper dependency = null;
         TestCaseRun.create c_case_run = tcmsCreateCaseRun(result, status);
 
         TestCase.create c_case = tcmsCreateCase(result, properties);
 
         String name_of_case = (new TestCase.create()).name(); // mne sa to nechce hladat
 
         boolean case_not_found = true;
         if (commands_sorted.get(name_of_case) != null) {
             for (CommandWrapper w : commands_sorted.get(name_of_case)) {
                 if (w.current.equals(c_case)) {
                     dependency = w;
                     case_not_found = false;
                 }
             }
         }
         if (case_not_found) {
             dependency = add(c_case, TestCase.class);
         }
         CommandWrapper case_run = add(c_case_run, TestCaseRun.class);
         case_run.addDependecy(dependency);
         case_run.addDependecy(run);
         case_run.addDependecy(build);
     }
 
     private void gatherTestInfo(TestResults results, CommandWrapper run, CommandWrapper build) {
 
         for (MethodResult result : results.getFailedTests()) {
             CreateTestCaseRun(result, TestCaseRun.FAILED, run, build);
         }
         for (MethodResult result : results.getPassedTests()) {
             CreateTestCaseRun(result, TestCaseRun.PASSED, run, build);
         }
         for (MethodResult result : results.getSkippedTests()) {
             CreateTestCaseRun(result, TestCaseRun.WAIVED, run, build);
         }
 
     }
 
     public synchronized void gather(TestResults results, AbstractBuild build, AbstractBuild run, Map<String, String> variables) throws IOException {
 
         if (results == null) {
             return;
         }
 
         if (build_s == null) {
             build_s = add(tcmsCreateBuild(build, properties), Build.class);
         }
         CommandWrapper run_s = add(tcmsCreateRun(run, properties, variables), TestRun.class);
         run_s.addDependecy(build_s);        
         
         CommandWrapper upd_s = add(tcmsUpdateRunStatusToFinished(), TestRun.class);
         upd_s.addDependecy(run_s);
 
         for (Map.Entry<String, String> variable : variables.entrySet()) {
             CommandWrapper link = add(tcmsLinkValue(environment, variable.getKey(), variable.getValue()), Object.class);
             if (link != null) {
                 link.addDependecy(run_s);
             }
         }
 
         gatherTestInfo(results, run_s, build_s);
 
     }
 
     public void clear() {
         list.clear();
         commands_sorted.clear();
         this.build_s = null;
     }
 
     private CommandWrapper add(TcmsCommand current, Class result_class) {
         if (current != null) {
             CommandWrapper script = CommandWrapper.wrap(current, result_class, properties, environment);
             list.add(script);
 
 
 
             if (commands_sorted.containsKey(current.name()) == false) {
                 commands_sorted.put(current.name(), new LinkedList<CommandWrapper>());
             }
             commands_sorted.get(current.name()).add(script);
 
             return script;
         }
         return null;
     }
 
     public Iterator<CommandWrapper> iterator() {
         return list.listIterator();
     }
 
     public LinkedList<CommandWrapper> getCommandList(String c) {
         return commands_sorted.get(c);
     }
 
     /**
      * Returns list of all CommandWrappers with name
      * <code>name</code> and have dependency with hashCode
      * <code>hashCode</code>.
      *
      * @param name
      * @param hashCode
      * @return
      */
     public LinkedList<CommandWrapper> getCommandList(String name, Integer hashCode) {
         LinkedList<CommandWrapper> commandList = new LinkedList<CommandWrapper>();
         /**
          * commands_sorted might not contain name, which caused issue #54 (see Github)
          */
         if (commands_sorted != null && commands_sorted.get(name)!=null) { 
             for (CommandWrapper cw : commands_sorted.get(name)) {
                 if (cw.hasDependency(hashCode)) {
                     commandList.add(cw);
                 }
             }
         }
         return commandList;
     }
 
     public Set<String> getComandClasses() {
         return commands_sorted.keySet();
     }
 
     public boolean isEmpty() {
         return commands_sorted.isEmpty();
     }
 
 }
