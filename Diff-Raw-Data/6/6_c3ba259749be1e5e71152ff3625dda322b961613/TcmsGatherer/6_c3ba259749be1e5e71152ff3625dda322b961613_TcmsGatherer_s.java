 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package NitrateIntegration;
 
 import NitrateIntegration.CommandWrapper;
 import com.redhat.engineering.jenkins.testparser.Parser;
 import com.redhat.engineering.jenkins.testparser.results.MethodResult;
 import com.redhat.engineering.jenkins.testparser.results.TestResults;
 import com.redhat.nitrate.*;
 import hudson.FilePath;
 import hudson.matrix.Combination;
 import hudson.matrix.MatrixRun;
 import hudson.model.AbstractBuild;
 import java.io.IOException;
 import java.io.PrintStream;
 import java.util.*;
 import redstone.xmlrpc.XmlRpcStruct;
 
 import java.io.Serializable;
 
 /**
  *
  * @author asaleh
  */
 public class TcmsGatherer implements Iterable<CommandWrapper>, Serializable{
 
     private int run_id;
     private int build_id;
     private TcmsProperties properties;
     CommandWrapper build_s;
     
     LinkedList<CommandWrapper> list = new LinkedList<CommandWrapper>();
     HashMap<TcmsCommand,CommandWrapper> commands = new HashMap<TcmsCommand,CommandWrapper>();
     
     HashMap<String,LinkedList<CommandWrapper>> commands_sorted = new HashMap<String,LinkedList<CommandWrapper>>();
     
     
     public TcmsGatherer( TcmsProperties properties) {
         this.properties = properties;
         this.build_s=null;
     }
 
     private static TestCase.create tcmsCreateCase(MethodResult result,TcmsProperties properties) {
         TestCase.create create = new TestCase.create();
         create.product = properties.getProductID();
         create.category = properties.getCategoryID();
         create.priority = properties.getPriorityID();
         create.summary = result.getDisplayName();
         create.plan = properties.getPlanID();
         return create;
     }
 
     private static Build.create tcmsCreateBuild(AbstractBuild build,TcmsProperties properties) {
         Build.create create = new Build.create();
         create.product = properties.getProductID();
         create.name = build.getId();
         create.description = build.getDescription();
         return create;
     }
 
     private static TestRun.create tcmsCreateRun(AbstractBuild run,TcmsProperties properties,Map<String,String> variables) {
         TestRun.create create = new TestRun.create();
         create.product = properties.getProductID();
         create.product_version = properties.getProduct_vID();
         create.plan = properties.getPlanID();
         create.build = -1;
         create.manager = properties.getManagerId();
         create.summary = "Build " + run.getDisplayName();
         if(variables!=null && variables.isEmpty() == false){ 
             for (Iterator it =variables.entrySet().iterator(); it.hasNext();) {
                 Map.Entry<String,String> e = (Map.Entry<String,String>) it.next();
                 create.summary += ", "+ e.getKey()+"="+e.getValue();
             }
         }
         return create;
     }
     
     private static TestCaseRun.create tcmsCreateCaseRun(int status) {
         TestCaseRun.create c = new TestCaseRun.create();
         c.run = -1;
         c.caseVar = -1;
         c.build = -1;
         c.case_run_status = status;
         return c;
     }
     
     
     private void CreateTestCaseRun(MethodResult result, int status, CommandWrapper run, CommandWrapper build) {
         
         CommandWrapper dependency = null;
         TestCaseRun.create c_case_run = tcmsCreateCaseRun(status);
          
         TestCase.create c_case = tcmsCreateCase(result,properties);
         if(commands.containsKey(c_case)){
             dependency =commands.get(c_case);
         }else{
             dependency = add(c_case,TestCase.class);
         }
         CommandWrapper case_run = add(c_case_run,TestCaseRun.class);
         case_run.addDependecy(dependency);
         case_run.addDependecy(run);
         case_run.addDependecy(build);
     }
 
     private void gatherTestInfo(TestResults results, CommandWrapper run, CommandWrapper build) {
 
         for (MethodResult result : results.getFailedTests()) {
             CreateTestCaseRun(result, TestCaseRun.FAILED, run,build);
         }
         for (MethodResult result : results.getPassedTests()) {
             CreateTestCaseRun(result, TestCaseRun.PASSED, run,build);
         }
         for (MethodResult result : results.getSkippedTests()) {
             CreateTestCaseRun(result, TestCaseRun.WAIVED, run,build);
         }
 
     }
 
     public synchronized void gather(TestResults results , AbstractBuild build, AbstractBuild run,Map<String,String> variables) throws IOException, InterruptedException {
 
         if (results == null) {
             return;
         }
      
         if(build_s==null) build_s = add(tcmsCreateBuild(build,properties),Build.class);
         CommandWrapper run_s =  add(tcmsCreateRun(run,properties,variables),TestRun.class);
         run_s.addDependecy(build_s);
         gatherTestInfo(results, run_s,build_s);
 
     }
 
     public void clear() {
         list.clear();
         commands.clear();
         commands_sorted.clear();
     }
 
     private CommandWrapper add(TcmsCommand current,Class result_class) {
         CommandWrapper script = CommandWrapper.wrap(current,result_class,properties);
         list.add(script);
         commands.put(current,script);
         
        if(commands_sorted.containsKey(current.getClass())==false){
            
             //FIXME: c.getName is wrong
             commands_sorted.put(current.name(), new LinkedList<CommandWrapper>());
         }
         commands_sorted.get(current.name()).add(script);
         
         return script;
     }
 
     public Iterator<CommandWrapper> iterator() {
         return list.listIterator();
     }
 
     public LinkedList<CommandWrapper> getCommandList(String c){
         LinkedList a = commands_sorted.get(c);
         return commands_sorted.get(c);
     }
     
     public Set<String> getComandClasses(){
         return commands_sorted.keySet();
     }
 
    public String  toString(){
        return "sdf";
    }
 }
