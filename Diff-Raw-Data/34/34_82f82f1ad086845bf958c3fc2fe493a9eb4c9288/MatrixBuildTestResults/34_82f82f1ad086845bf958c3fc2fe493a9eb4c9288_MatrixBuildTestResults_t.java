 
 package com.redhat.engineering.jenkins.testparser.results;
 
 import hudson.matrix.Combination;
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
     
     // stores list of all tests of children runs
     private List<TestResults> runResults = new ArrayList<TestResults>();
     
     // stores list of all runs
     private List<String> runs = new ArrayList<String>();
     
     // stores mapping matrix run -> it`s combination
     private Map<String, Combination> runCombinations = new HashMap<String,Combination>();
     
     // stores mapping matrix run -> matrix run`s test results
    private Map<String, MatrixRunTestResults> mapRunResults = new HashMap<String, MatrixRunTestResults>();
     
     private Filter filter; 
     
     // these variables are updated from filtered field by tally()
     private int passedTestCount;
     private int failedTestCount;
     private int skippedTestCount;
     private int totalTestCount;
     private int failedConfigCount;
     private int skippedConfigCount;
     private long duration;    
     private Map<String, PackageResult> packageMap = new HashMap<String, PackageResult>();
    
     
 
     public MatrixBuildTestResults(String name) {
 	super(name);
     }
     
     /** {@inheritDoc}
      */
     @Override
     public void setOwner(AbstractBuild<?, ?> owner) throws IllegalArgumentException{
 	
 	if(owner instanceof MatrixBuild ){
 	    this.owner = owner;
 	} else{
 	    throw new IllegalArgumentException("Owner of MatrixBuildTestResuls"+ 
 		    " must be matrix build");
 	}
     }
     
     
     
     /**
      * Add test results of child matrix run to this build`s results. 
      * Duplicates are not added.
      * 
      * @param mrun	    matrix run to which results correspond to
      * @param combination   matrix run`s combination 
      * @param results	 
      * @return		    false if this run is already mapped to results
      */
    
     public boolean addMatrixRunTestResults(String mrun, Combination combination, TestResults tr){
 	
 	
 	if(! (tr instanceof MatrixRunTestResults)) {
 	    return false;
 	}
 	 
 	MatrixRunTestResults results = (MatrixRunTestResults) tr;
 	
 	// test if already added
	if(this.mapRunResults.get(mrun) == null){
 	    
 	    // add run to runs of this build
 	    this.runs.add(mrun);
 	    
 	    // add mapping mrun -> it`s combination
 	    this.runCombinations.put(mrun, combination);
 	    
 	    // add mapping mrun -> mrun`s test results to mrunResults
	    this.mapRunResults.put(mrun, results);
 	    
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
 	    
 	    /* update filtered fields and  calculated fields - important
 	     * to do in this order 
 	     */
 	    this.updateFiltered();	    
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
     
     public void addFilter(Filter filter){
 	this.filter = filter;
 	updateFiltered();
	tally();
     }
 
     public void removeFilter(){
 	this.filter = null;
 	updateFiltered();
     }
     
     /**
      * Updates filtered fields. If filter is null, all original values from all
      * children runs are added, if filter is present, results from only those 
      * runs are included that pass the filter.
      */
    //FIXME: broken
     public void updateFiltered(){
 	
 	passedTests.clear();
 	failedTests.clear();
 	skippedTests.clear();
 	failedConfigurationMethods.clear();
 	skippedConfigurationMethods.clear();
 	runResults.clear();
 	testList.clear();
 
 	for (String mrun: runs){
 	    // either filter is null => add all, or filter is present, then filter
	    if(filter == null || filter.isIncluded(runCombinations.get(mrun)) ){
		passedTests.addAll(mapRunResults.get(mrun).getPassedTests());
		failedTests.addAll(mapRunResults.get(mrun).getFailedTests());
		skippedTests.addAll(mapRunResults.get(mrun).getSkippedTests());
		failedConfigurationMethods.addAll(mapRunResults.get(mrun).getFailedConfigs());
		skippedConfigurationMethods.addAll(mapRunResults.get(mrun).getSkippedConfigs());
		runResults.add(mapRunResults.get(mrun));
		testList.addAll(mapRunResults.get(mrun).getTestList());		    
 	    }
 	}
     }
 	    
 	    
 }
