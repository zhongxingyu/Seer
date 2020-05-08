 
 package com.redhat.engineering.jenkins.testparser.results;
 
 import hudson.matrix.MatrixBuild;
 import hudson.matrix.MatrixRun;
 import hudson.model.AbstractBuild;
 import hudson.model.Result;
 import java.util.*;
 
 /**
  * This is class that stores mapping Matrix Run -> Test Results
  * 
  * TODO: handle change of configuration (add/remove axis ...)
  * 
  * @author Jan Rusnacko (jrusnack at redhat.com)
  */
 public class MatrixBuildTestResults extends BaseResult implements TestResults {
     
     private List<MethodResult> passedTests = new ArrayList<MethodResult>();
     private List<MethodResult> failedTests = new ArrayList<MethodResult>();
     private List<MethodResult> skippedTests = new ArrayList<MethodResult>();
     private List<MethodResult> failedConfigurationMethods = new ArrayList<MethodResult>();
     private List<MethodResult> skippedConfigurationMethods = new ArrayList<MethodResult>();
     private List<TestResult> testList = new ArrayList<TestResult>();
     private int passedTestCount;
     private int failedTestCount;
     private int skippedTestCount;
     private int totalTestCount;
     private int failedConfigCount;
     private int skippedConfigCount;
     private long duration;
     
     private Map<String, PackageResult> packageMap = new HashMap<String, PackageResult>();
     
     // stores list of all run`s tests: only one for freestyle project, multiple for matrix
     private List<TestResults> runResults = new ArrayList<TestResults>();
     
     // stores list of all runs; only one for freestyle project, multiple for matrix
     private List<String> runs = new ArrayList<String>();
     
     // stores mapping matrix run -> matrix run`s test results
     private Map<String, MatrixRunTestResults> mrunResults = new HashMap<String, MatrixRunTestResults>();
       
     
 
     public MatrixBuildTestResults(String name) {
 	super(name);
 	failedConfigCount = 0;
 	skippedConfigCount = 0;
     }
     
     /** {@inheritDoc}
      */
     @Override
     public void setOwner(AbstractBuild<?, ?> owner) {
 	
 	if(owner instanceof MatrixBuild ){
 	    this.owner = owner;
 	} else{
 	    throw new IllegalArgumentException("Owner of MatrixRunTestResuls"+ 
 		    " must be matrix run");
 	}
     }
     
     /**
      * Add test results of child matrix run to this build`s results. 
      * Duplicates are not added.
      * 
      * @param mrun	matrix run to which results correspond to
      * @param results	
      * @return		false if this run is already mapped to results
      */
    public boolean addMatrixRunTestResults(MatrixRun mrun, TestResults tr){
 	
	if(! (tr instanceof MatrixRunTestResults)) {
 	    return false;
 	}
 	
	MatrixRunTestResults results = (MatrixRunTestResults) tr;
	
 	// test if already added
 	if(this.mrunResults.get(mrun.toString()) == null){
 	    
 	    // add run to runs of this build
 	    this.runs.add(mrun.toString());
 	    
 	    // add run`s results to results
 	    this.runResults.add(results);
 	    
 	    // add mapping mrun -> mrun`s test results to mrunResults
 	    this.mrunResults.put(mrun.getDisplayName(), results);
 	    
 	    // add all tests from run test list to build`s test list
 	    for(TestResult res : results.getTestList()){
 		this.testList.add(res);
 	    }
 	    
 	    // add all failed configs
 	    for(MethodResult res: results.getFailedConfigs()){
 		this.failedConfigurationMethods.add(res);
 	    }
 	    
 	    // add all skipped configs
 	    for(MethodResult res: results.getSkippedConfigs()){
 		this.skippedConfigurationMethods.add(res);
 	    }
 	    
 	    // add all failed tests
 	    for(MethodResult res: results.getFailedTests()){
 		this.failedTests.add(res);
 	    }
 	    
 	    // add all skipped tests
 	    for(MethodResult res: results.getSkippedTests()){
 		this.skippedTests.add(res);
 	    }
 	    
 	    //add all passed tests
 	    for(MethodResult res: results.getPassedTests()){
 		this.passedTests.add(res);
 	    }
 	    
 	    // update calculated fields
 	    this.tally();
 	    
 	    if (results.getFailedTestCount() > 0){
 		owner.setResult(Result.UNSTABLE);
 	    } else {
 		owner.setResult(Result.SUCCESS);
 	    }
 	    return true;
 	}
 	return false;
     }
 
     /** {@inheritDoc}
      */
     @Override
     public int getFailedConfigCount() {
 	return failedConfigCount;
     }
     
     /** {@inheritDoc}
      */
     @Override
     public int getSkippedConfigCount(){
 	return skippedConfigCount;
     }
 
     /** {@inheritDoc}
      */
     @Override
     public boolean isMatrixBuildTestResult() {
 	return true;
     }
 
     /** {@inheritDoc}
      */
     @Override
     public List<MethodResult> getFailedConfigs() {
 	return failedConfigurationMethods;
     }
 
     /** {@inheritDoc}
      */
     @Override
     public List<MethodResult> getSkippedConfigs() {
 	return skippedConfigurationMethods;
     }
     
     /** {@inheritDoc}
      */
     @Override
     public List<MethodResult> getFailedTests(){
 	return failedTests;
     }
 
     /** {@inheritDoc}
      */
     @Override
     public List<MethodResult> getSkippedTests(){
 	return skippedTests;
     }
 
     /** {@inheritDoc}
      */
     @Override
     public List<MethodResult> getPassedTests(){
 	return passedTests;
     }
     
     /** {@inheritDoc}
      */
     @Override
     public List<TestResult> getTestList(){
 	return testList;
     }
     
     /** {@inheritDoc}
      */
     @Override
     public int getTotalTestCount(){
 	return totalTestCount;
     }
 	  
     /** {@inheritDoc}
      */
     @Override
     public int getPassedTestCount(){
 	return passedTestCount;
     }
     
     /** {@inheritDoc}
      */
     @Override
     public int getFailedTestCount(){
 	return failedTestCount;
     }
     
     /** {@inheritDoc}
      */
     @Override
     public int getSkippedTestCount(){
 	return skippedTestCount;
     }
      
     /** {@inheritDoc}
      */
     @Override
     public List<TestResults> getRunResults(){
 	    return runResults;
     }
     
     /** {@inheritDoc}
      */
     @Override
     public String toString(){
 	return String.format("TestResults {name='%s', totalTests=%d, " +
           "failedTests=%d, skippedTests=%d}", name, totalTestCount, failedTestCount,
           skippedTestCount);
     }
 
     /** {@inheritDoc}
      */
     @Override
     public Set<String>  getPackageNames() {
 	return packageMap.keySet();
     }
 
     /** 
      * Don`t do anything, because this class only aggregates test results from
      * child runs, so we do not add tests directly (call addMatrixRunTestResults)
      */
     @Override
     public Map<String, PackageResult> getPackageMap() {
 	return packageMap;
     }
 
 
     /** 
      * {@inheritDoc}
      */
     @Override
     public void addUniqueTests(List<TestResult> testList) {
 	
 //	Set<TestResult> tmpSet = new HashSet<TestResult>(this.testList);
 //	tmpSet.addAll(testList);
 //	this.testList = new ArrayList<TestResult>(tmpSet);
     }
 
     /** {@inheritDoc}
      */
     @Override
     public void tally() {
 	failedConfigCount = failedConfigurationMethods.size();
 	skippedConfigCount = skippedConfigurationMethods.size();
 	failedTestCount = failedTests.size();
 	passedTestCount = passedTests.size();
 	skippedTestCount = skippedTests.size();
 	totalTestCount = passedTestCount + failedTestCount + skippedTestCount;
 	packageMap.clear();
 	for (TestResult _test : testList) {
 	    for (ClassResult _class : _test.getClassList()) {
 		String pkg = _class.getName();
 		int lastDot = pkg.lastIndexOf('.');
 		if (lastDot == -1) {
 		pkg = "No Package";
 		} else {
 		pkg = pkg.substring(0, lastDot);
 		}
 		if (packageMap.containsKey(pkg)) {
 		List<ClassResult> classResults = packageMap.get(pkg).getClassList();
 		if (!classResults.contains(_class)) {
 		    classResults.add(_class);
 		}
 		} else {
 		PackageResult tpkg = new PackageResult(pkg);
 		tpkg.getClassList().add(_class);
 		tpkg.setParent(this);
 		packageMap.put(pkg, tpkg);
 		}
 	    }
 	}
 	duration = 0;
 	for (PackageResult pkgResult : packageMap.values()) {
 	    pkgResult.tally();
 	    duration += pkgResult.getDuration();
 	}
     }
 
     @Override
     public boolean isRunTestResult() {
 	return false;
     }
 
     @Override
     public List<String> getRuns() {
 	return runs;
     }
 
     
 }
