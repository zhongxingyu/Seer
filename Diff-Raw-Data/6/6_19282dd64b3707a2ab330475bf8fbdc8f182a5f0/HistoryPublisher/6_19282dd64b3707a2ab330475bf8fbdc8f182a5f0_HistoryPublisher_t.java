 package com.attask.jenkins.testreport.examplepublisher;
 
 import com.attask.jenkins.testreport.TestDataPublisher;
 import com.attask.jenkins.testreport.TestResult;
 import com.attask.jenkins.testreport.TestResultAction;
 import hudson.Extension;
 import hudson.model.AbstractBuild;
 import hudson.model.Descriptor;
 import hudson.model.Run;
 import org.kohsuke.stapler.DataBoundConstructor;
 import org.kohsuke.stapler.Stapler;
 import org.kohsuke.stapler.StaplerRequest;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.List;
 
 /**
  * User: Joel Johnson
  * Date: 2/8/13
  * Time: 11:51 AM
  */
 public class HistoryPublisher extends TestDataPublisher {
 	private static final boolean INCLUDE_FUTURE_BUILDS = true;
 	private static final boolean ONLY_PREVIOUS_BUILDS = false;
    private static final int MAX_HISTORY_SIZE = 5;
 
 	public List<TestResult> history;
 	public int maxTime;
 	public int currentIndex;
 
 	@DataBoundConstructor
 	public HistoryPublisher() {
 
 	}
 
 	@Override
 	public String getDisplayName() {
 		return "History";
 	}
 
 	@Override
 	public boolean includeFloat(AbstractBuild<?, ?> build, TestResult testResult) {
 		int historyCount = 25;
 		StaplerRequest currentRequest = Stapler.getCurrentRequest();
 		if(currentRequest != null) {
 			String historyCountParam = currentRequest.getParameter("historyCount");
 			if(historyCountParam != null && !historyCountParam.isEmpty()) {
 				historyCount = Integer.parseInt(historyCountParam);
 			}
 		}
 
 		populateHistory(build, testResult, historyCount, INCLUDE_FUTURE_BUILDS);
 
 		return true;
 	}
 
 	@Override
 	public boolean includeSummary(AbstractBuild<?, ?> build, TestResult testResult) {
 		return false;
 	}
 
 	@Override
 	public boolean before(AbstractBuild<?, ?> build, Collection<TestResult> testResults) throws IOException, InterruptedException {
 		return false;
 	}
 
 	@Override
 	public boolean each(AbstractBuild<?, ?> build, TestResult testResult) throws IOException, InterruptedException {
 		StaplerRequest currentRequest = Stapler.getCurrentRequest();
 		if(currentRequest != null) {
 			if(currentRequest.getParameter("name") != null) {
 				return true;
 			}
 		}
 
 		populateHistory(build, testResult, 5, ONLY_PREVIOUS_BUILDS);
 		return true;
 	}
 
 	@Override
 	public boolean after(AbstractBuild<?, ?> build, Collection<TestResult> testResults) throws IOException, InterruptedException {
 		return false;
 	}
 
 	private void populateHistory(AbstractBuild<?, ?> build, TestResult testResult, int maxHistoryCount, boolean tryForward) {
 		List<TestResult> history = new ArrayList<TestResult>(maxHistoryCount);
 		int maxTime = Integer.MIN_VALUE;
 
 		int historyCount = maxHistoryCount;
 
 		if(tryForward) {
 			Run next = build.getNextBuild();
 			while(next != null && historyCount > (maxHistoryCount / 2)) {
 				TestResultAction action = next.getAction(TestResultAction.class);
 				if(action != null) {
 					TestResult oldTestResult = action.getTestResults().get(testResult.getName());
 					if(oldTestResult != null && oldTestResult.getName().equals(testResult.getName()) && TestResult.uniquifierMatches(testResult, oldTestResult)) {
 						historyCount--;
 						history.add(oldTestResult);
 						maxTime = Math.max(maxTime, oldTestResult.getTime());
 					}
 				}
 
 				next = next.getNextBuild();
 			}
 		}
 
 		Run previous = build;
 		TestResult current = null;
 		while(historyCount > 0 && previous != null) {
 			TestResultAction action = previous.getAction(TestResultAction.class);
 			if(action != null) {
 				TestResult oldTestResult = action.getTestResults().get(testResult.getName());
 				if(oldTestResult != null && oldTestResult.getName().equals(testResult.getName()) && TestResult.uniquifierMatches(testResult, oldTestResult)) {
 					historyCount--;
 					history.add(0, oldTestResult);
 					maxTime = Math.max(maxTime, oldTestResult.getTime());
 					if(previous == build) {
 						current = oldTestResult;
 					}
 				}
 			}
 			previous = previous.getPreviousBuild();
 		}
 		this.history = history;
 		this.maxTime = maxTime;
 
 		int indexOf = history.size()-1;
 		if(current != null) {
 			int i = -1;
 			//have to manually search for it since equals/hashcode only goes off of the name.
 			for (TestResult result : history) {
 				i++;
 				if(result.getRunId().equals(current.getRunId())) {
 					indexOf = i;
 					break;
 				}
 			}
 		}
 		this.currentIndex = indexOf;
 	}
 
    public int getMaxHistorySize() {
        return MAX_HISTORY_SIZE;
    }

 	@Extension
 	public static class DescriptorImpl extends Descriptor<TestDataPublisher> {
 		@Override
 		public String getDisplayName() {
 			return "History";
 		}
 	}
 }
