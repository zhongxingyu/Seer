 package ske.mag.jenkins.buildscreen;
 
import hudson.model.Run;
 import java.util.Collection;
 import java.util.List;
 import jenkins.model.Jenkins;
 import ske.mag.jenkins.buildscreen.comparators.ClaimComparator;
 import ske.mag.jenkins.buildscreen.comparators.LastSuccesfullBuildComparator;
 import static com.google.common.collect.Ordering.from;
import hudson.model.AbstractBuild;
import static hudson.model.Result.FAILURE;
import static hudson.model.Result.UNSTABLE;
 import static ske.mag.jenkins.buildscreen.collections.CollectionFilter.filter;
import hudson.util.RunList;
 
 public class FailedJobs {
 
 	public static List<FailedJob> brokenBuilds(RunList<Run> allBuilds) {
		return sortByClaimAndLastSuccessfulBuild(convertList(filter(allBuilds).byResult(FAILURE).byLastCompletedBuilds()));
 	}
 
 	public static List<FailedJob> failedBuilds(Collection<Run> allBuilds) {
		return sortByClaimAndLastSuccessfulBuild(convertList(filter(allBuilds).byResult(UNSTABLE).byLastCompletedBuilds()));
 	}
 
 	private static Collection<FailedJob> convertList(Collection<AbstractBuild> buildsToConvert) {
 		return transform(buildsToConvert).toFailedJobs();
 	}
 
 	private static List<FailedJob> sortByClaimAndLastSuccessfulBuild(Collection<FailedJob> jobsToSort) {
 		return from(new ClaimComparator()).compound(new LastSuccesfullBuildComparator()).sortedCopy(jobsToSort);
 	}
 
 	private static AbstractBuildTransformer transform(Collection<AbstractBuild> buildsToConvert) {
 		return new AbstractBuildTransformer(Jenkins.getInstance(), buildsToConvert);
 	}
 }
