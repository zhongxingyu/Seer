 package hudson.plugins.kagemai;
 
 import hudson.Launcher;
 import hudson.model.AbstractBuild;
 import hudson.model.BuildListener;
 import hudson.model.Descriptor;
 import hudson.model.Result;
 import hudson.plugins.kagemai.model.KagemaiIssue;
 import hudson.scm.ChangeLogSet.Entry;
 import hudson.tasks.Publisher;
 
 import java.io.IOException;
 import java.util.Collections;
 import java.util.HashSet;
 import java.util.List;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import org.kohsuke.stapler.StaplerRequest;
 
 /**
  * @author yamkazu
  * 
  */
 public class KagemaiPublisher extends Publisher {
 
 	KagemaiPublisher() {
 	}
 
 	@Override
 	public boolean perform(AbstractBuild<?, ?> build, Launcher launcher,
 			BuildListener listener) throws InterruptedException, IOException {
 
 		KagemaiSite site = KagemaiSite.get(build.getProject());
 
 		if (site == null) {
 			build.setResult(Result.FAILURE);
 			return true;
 		}
 
 		HashSet<Integer> bugIds = new HashSet<Integer>();
 		KagemaiProjectProperty mpp = build.getParent().getProperty(
 				KagemaiProjectProperty.class);
 		if (mpp != null && mpp.getSite() != null) {
 			String regex = mpp.getRegex();
 			Pattern pattern = Pattern.compile(regex);
 			for (Entry entry : build.getChangeSet()) {
 				Matcher matcher = pattern.matcher(entry.getMsg());
 				while (matcher.find()) {
 					try {
 						bugIds.add(Integer.valueOf(matcher.group(matcher
 								.groupCount())));
 					} catch (NumberFormatException e) {
 						continue;
 					}
 				}
 			}
 		} else {
 			build.setResult(Result.FAILURE);
 			return true;
 		}
 		KagemaiSession kagemaiSession = mpp.getKagemaiSession();
 		List<KagemaiIssue> issues = null;
 		if ((!bugIds.isEmpty()) && kagemaiSession != null) {
 			issues = kagemaiSession.getIssuesMap(bugIds);
 		}
		Collections.sort(issues);
 		KagemaiBuildAction action = new KagemaiBuildAction(build, issues, mpp
 				.getSiteName(), mpp.getProjectId());
 		build.addAction(action);
 
 		return true;
 	}
 
 	public Descriptor<Publisher> getDescriptor() {
 		return DESCRIPTOR;
 	}
 
 	public static final DescriptorImpl DESCRIPTOR = new DescriptorImpl();
 
 	public static final class DescriptorImpl extends Descriptor<Publisher> {
 
 		DescriptorImpl() {
 			super(KagemaiPublisher.class);
 		}
 
 		@Override
 		public String getDisplayName() {
 			return Messages.publisher_dispname();
 		}
 
 		@Override
 		public KagemaiPublisher newInstance(StaplerRequest req)
 				throws FormException {
 			return new KagemaiPublisher();
 		}
 
 	}
 }
