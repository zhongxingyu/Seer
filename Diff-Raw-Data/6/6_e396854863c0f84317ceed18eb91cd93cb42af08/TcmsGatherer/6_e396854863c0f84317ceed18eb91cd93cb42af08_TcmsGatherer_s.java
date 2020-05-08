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
 import com.redhat.nitrate.Env.Value;
 import hudson.FilePath;
 import hudson.matrix.Combination;
 import hudson.matrix.MatrixRun;
 import hudson.model.AbstractBuild;
 import java.io.*;
 import java.util.*;
 import redstone.xmlrpc.XmlRpcStruct;
 
 
 /**
  *
  * @author asaleh
  */
 public class TcmsGatherer implements Iterable<CommandWrapper>, Serializable{
     private static final long serialVersionUID = 7501429501311908358L;
 
     private int run_id;
     private int build_id;
     private TcmsProperties properties;
     CommandWrapper build_s;
     private TcmsEnvironment environment;
 
     
     LinkedList<CommandWrapper> list = new LinkedList<CommandWrapper>();
     HashMap<String,LinkedList<CommandWrapper>> commands_sorted = new HashMap<String,LinkedList<CommandWrapper>>();
     
     
     public TcmsGatherer( TcmsProperties properties,TcmsEnvironment env) {
         this.properties = properties;
         this.build_s=null;
         this.environment =env;
     }
 
     public void setProperties(TcmsProperties properties) {
         this.properties = properties;
     }
 
     
     private static TestCase.create tcmsCreateCase(MethodResult result,TcmsProperties properties) {
         TestCase.create create = new TestCase.create();
         create.product = properties.getProductID();
         create.category = properties.getCategoryID();
         create.priority = properties.getPriorityID();
        create.summary = result.getDisplayName();
         create.plan = properties.getPlanID();
         create.is_automated = 1;
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
     
     private static TestCaseRun.create tcmsCreateCaseRun(MethodResult result,int status) {
         TestCaseRun.create c = new TestCaseRun.create();
         c.run = -1;
         c.caseVar = -1;
         c.build = -1;
         c.case_run_status = status;
         if(result.getException() !=null){
             c.notes = result.getException().getExceptionName();
             c.notes += "\n\n";
             c.notes += result.getException().getStackTrace();
             
         }
         
         return c;
     }
     
     private static TestRun.link_env_value tcmsLinkValue(TcmsEnvironment env,String property,String value) {
         TestRun.link_env_value c = new TestRun.link_env_value();
         c.run_id = -1;
         Hashtable<String,Value> prop = env.getValues().get(property);
         if(prop!=null){
             if(prop.containsKey(value)){
                 c.env_value_id = prop.get(value).id;
                 return c;
             }
         }
         return null;
     }
     
     private void CreateTestCaseRun(MethodResult result, int status, CommandWrapper run, CommandWrapper build) {
         
         CommandWrapper dependency = null;
         TestCaseRun.create c_case_run = tcmsCreateCaseRun(result,status);
          
         TestCase.create c_case = tcmsCreateCase(result,properties);
         
         String name_of_case = (new TestCase.create()).name(); // mne sa to nechce hladat
         
         boolean case_not_found =true;
         if(commands_sorted.get(name_of_case)!=null){
             for(CommandWrapper w:commands_sorted.get(name_of_case)){
                 if(w.current.equals(c_case)){
                     dependency =w;
                     case_not_found = false;
                 }
             }
         }
         if(case_not_found){
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
         
         for(Map.Entry<String,String> variable:variables.entrySet()){
             CommandWrapper link = add(tcmsLinkValue(environment,variable.getKey(),variable.getValue()),Object.class);
             if(link!=null){
                 link.addDependecy(run_s);
             }
         }
         
         gatherTestInfo(results, run_s,build_s);
 
     }
 
     public void clear() {
         list.clear();
         commands_sorted.clear();
         this.build_s=null;
     }
 
     private CommandWrapper add(TcmsCommand current,Class result_class) {
         if(current!=null){
             CommandWrapper script = CommandWrapper.wrap(current,result_class,properties,environment);
             list.add(script);
             
           
 
             if(commands_sorted.containsKey(current.name())==false){
                 commands_sorted.put(current.name(), new LinkedList<CommandWrapper>());
             }
             commands_sorted.get(current.name()).add(script);
 
             return script;
         } return null;
     }
 
     public Iterator<CommandWrapper> iterator() {
         return list.listIterator();
     }
 
     public LinkedList<CommandWrapper> getCommandList(String c){
         return commands_sorted.get(c);
     }
     
     /**
      * Returns list of all CommandWrappers with name <code>name</code> and 
      * have dependency with hashCode <code>hashCode</code>.
      * 
      * @param name
      * @param hashCode
      * @return 
      */
     public LinkedList<CommandWrapper> getCommandList(String name, Integer hashCode){
         LinkedList<CommandWrapper> commandList = new LinkedList<CommandWrapper>();
         for(CommandWrapper cw : commands_sorted.get(name)){
             if(cw.hasDependency(hashCode)) commandList.add(cw);
         }
         return commandList;
     }
     
     public Set<String> getComandClasses(){
         return commands_sorted.keySet();
     }
     
     public boolean isEmpty(){
         return commands_sorted.isEmpty();
     }
 
     
        /**
    * Always treat de-serialization as a full-blown constructor, by
    * validating the final state of the de-serialized object.
    */
    private void readObject(
      ObjectInputStream aInputStream
    ) throws ClassNotFoundException, IOException {
      //always perform the default de-serialization first
      aInputStream.defaultReadObject();
 
      
   }
 
     /**
     * This is the default implementation of writeObject.
     * Customise if necessary.
     */
     private void writeObject(
       ObjectOutputStream aOutputStream
     ) throws IOException {
       //perform the default serialization for all non-transient, non-static fields
       aOutputStream.defaultWriteObject();
     }
 }
