 package com.attask.jenkins.testreport;
 
 import hudson.matrix.MatrixBuild;
 import hudson.matrix.MatrixRun;
 import hudson.model.AbstractBuild;
 import hudson.model.Run;
 import hudson.tasks.test.AbstractTestResultAction;
 import org.kohsuke.stapler.StaplerRequest;
 import org.kohsuke.stapler.StaplerResponse;
 import org.kohsuke.stapler.export.Exported;
 import org.kohsuke.stapler.export.ExportedBean;
 
 import javax.servlet.ServletOutputStream;
 import java.io.IOException;
 import java.util.*;
 
 /**
  * User: Joel Johnson
  * Date: 1/19/13
  * Time: 2:06 PM
  */
 @ExportedBean
 public class TestResultAction extends AbstractTestResultAction {
 	private final String buildId;
 	private final Map<String, TestResult> testResults;
 	private final Map<TestStatus, List<TestResult>> testResultByStatus;
 
 	private final String uniquifier;
 	private final String urlName;
 
 	public TestResultAction(AbstractBuild<?, ?> build, List<TestResult> testResults, String uniquifier, String url) {
 		super(build);
 		if(build == null) {
 			throw new NullPointerException("build");
 		}
 		if(testResults == null) {
 			throw new NullPointerException("testResults");
 		}
 		if(url == null) {
 			throw new NullPointerException("url");
 		}
 
 		this.buildId = TestResult.getRealExternalizableId(build);
 		this.testResults = new HashMap<String, TestResult>(testResults.size());
 		this.testResultByStatus = new HashMap<TestStatus, List<TestResult>>();
 		for (TestResult testResult : testResults) {
 			this.testResults.put(testResult.getName(), testResult);
 
 			List<TestResult> testResultList = this.testResultByStatus.get(testResult.getStatus());
 			if(testResultList == null) {
 				testResultList = new ArrayList<TestResult>();
 				this.testResultByStatus.put(testResult.getStatus(), testResultList);
 			}
 			testResultList.add(testResult);
 		}
 		for (List<TestResult> results : this.testResultByStatus.values()) {
 			Collections.sort(results);
 		}
 		this.uniquifier = uniquifier;
 		this.urlName = url;
 	}
 
 	@SuppressWarnings("UnusedDeclaration") //used in index.jelly
 	public Run findBuild() {
 		if(buildId.contains("$$")) {
 			String[] ids = buildId.split("\\$\\$", 2);
 			if(ids.length == 2) {
 				String parentMatrixId = ids[0];
 				String childMatrixId = ids[1];
 
 				Run<?, ?> run = Run.fromExternalizableId(parentMatrixId);
 				if(run != null) {
 					if(run instanceof MatrixBuild) {
 						List<MatrixRun> runs = ((MatrixBuild) run).getRuns();
 						for (MatrixRun matrixRun : runs) {
 							if(matrixRun.getExternalizableId().equals(childMatrixId)) {
 								return matrixRun;
 							}
 						}
 					}
 				}
 			}
 		}
 		return Run.fromExternalizableId(buildId);
 	}
 
 	public void doGetStackTrace(StaplerRequest request, StaplerResponse response) throws IOException {
 		String name = request.getParameter("name");
 		TestResult testResult = testResults.get(name);
 		ServletOutputStream outputStream = response.getOutputStream();
 		outputStream.print(testResult.getStackTrace());
 		outputStream.flush();
 	}
 
 	@Exported
 	public String getBuildId() {
 		return buildId;
 	}
 
 	@Exported
 	public Map<String, TestResult> getTestResults() {
 		return testResults;
 	}
 
 	@Exported
 	public List<TestResult> getFailures() {
 		List<TestResult> failures = testResultByStatus.get(TestStatus.FAILED);
 		return failures == null ? Collections.<TestResult>emptyList() : failures;
 	}
 
 	public Collection<TestResult> findAllResults() {
 		TreeSet<TestResult> result = new TreeSet<TestResult>();
 		for (Collection<TestResult> testResults : testResultByStatus.values()) {
 			result.addAll(testResults);
 		}
 		return result;
 	}
 
 	@Override
 	public Object getResult() {
 		return this;
 	}
 
 	@Override
 	public int getFailCount() {
 		return getFailures().size();
 	}
 
 	@Override
 	public int getTotalCount() {
 		return testResults.values().size();
 	}
 
 	@Override
 	public int getSkipCount() {
 		List<TestResult> skipped = testResultByStatus.get(TestStatus.SKIPPED);
 		return skipped == null ? 0 :skipped.size();
 	}
 
 	@Override
 	public AbstractTestResultAction getPreviousResult() {
 		Run build = findBuild();
 		if(build == null) {
 			return null;
 		}
 
		Run previousBuild;
		while((previousBuild = build.getPreviousBuild()) != null) {
 			TestResultAction action = previousBuild.getAction(TestResultAction.class);
 			if(action != null) {
 				return action;
 			}
 		}
 
 		return null;
 	}
 
 	@Exported
 	public String getUniquifier() {
 		return uniquifier;
 	}
 
 	@Override
 	public String getIconFileName() {
 		return "clipboard.png";
 	}
 
 	@Override
 	public String getDisplayName() {
 		return "Test Result";
 	}
 
 	@Exported
 	@Override
 	public String getUrlName() {
 		return urlName;
 	}
 }
