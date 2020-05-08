 package pl.kbaranski.hudson.jiraVersionRelease;
 
 import hudson.Extension;
 import hudson.Launcher;
 import hudson.model.BuildListener;
 import hudson.model.JobProperty;
 import hudson.model.AbstractBuild;
 import hudson.model.AbstractProject;
 import hudson.tasks.BuildStepDescriptor;
 import hudson.tasks.BuildStepMonitor;
 import hudson.tasks.Notifier;
 import hudson.tasks.Publisher;
 import hudson.util.CopyOnWriteList;
 
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.util.Calendar;
 import java.util.logging.Logger;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import javax.xml.rpc.ServiceException;
 
 import net.sf.json.JSONObject;
 
 import org.kohsuke.stapler.DataBoundConstructor;
 import org.kohsuke.stapler.StaplerRequest;
 
 import pl.kbaranski.hudson.jiraVersionRelease.soap.JiraSoapService;
 import pl.kbaranski.hudson.jiraVersionRelease.soap.JiraSoapServiceService;
 import pl.kbaranski.hudson.jiraVersionRelease.soap.JiraSoapServiceServiceLocator;
 import pl.kbaranski.hudson.jiraVersionRelease.soap.RemoteAuthenticationException;
 import pl.kbaranski.hudson.jiraVersionRelease.soap.RemoteException;
 import pl.kbaranski.hudson.jiraVersionRelease.soap.RemoteVersion;
import pl.kbaranski.hudson.jiraVersionRelease.Messages;
 
 public class JiraVersionReleasePublisher extends Notifier {
 
 	private String instanceName;
 
 	private String projectKey;
 
 	private String prefixRegexp;
 
 	public static final DescriptorImpl DESCRIPTOR = new DescriptorImpl();
 
 	private final static Logger LOG = Logger.getLogger(JiraVersionReleasePublisher.class.getName());
 
 	@DataBoundConstructor
 	public JiraVersionReleasePublisher(String instanceName,
 			String projectKey, String prefixRegexp) {
 		this.instanceName = instanceName;
 		this.projectKey = projectKey;
 		this.prefixRegexp = prefixRegexp;
 	}
 
 	public String getInstanceName() {
 		return instanceName;
 	}
 
 	public void setInstanceName(String instanceName) {
 		this.instanceName = instanceName;
 	}
 
 	public String getProjectKey() {
 		return projectKey;
 	}
 
 	public String getPrefixRegexp() {
 		return prefixRegexp;
 	}
 
 	@Override
 	public boolean needsToRunAfterFinalized() {
 		return true;
 	}
 
 	public BuildStepMonitor getRequiredMonitorService() {
 		return BuildStepMonitor.BUILD;
 	}
 
 	public TrackerInstance getCurrentTracker() {
 		if (this.instanceName == null)
 			return null;
 		for (TrackerInstance ti : DESCRIPTOR.getInstances()) {
 			if (instanceName.equals(ti.getInstanceName())) {
 				return ti;
 			}
 		}
 		return null;
 	}
 
 	@Override
 	public boolean perform(AbstractBuild<?, ?> build, Launcher launcher,
 			BuildListener listener) {
 		JiraSoapServiceService serviceLocator = new JiraSoapServiceServiceLocator();
 
 		try {
 			// Wzorce itp wykorzystywane przy sprawdzaniu wersji
 			Pattern versionNumberPrefixPattern = Pattern.compile(prefixRegexp);
 			String versionNumberFullRegex = prefixRegexp + build.getNumber();
 			String newVerName = null;
 
 			// Podłączenie do JIRA + logowanie
 			TrackerInstance trackerInstance = getCurrentTracker();
 			JiraSoapService soapService = serviceLocator
 					.getJirasoapserviceV2(trackerInstance.getUrl());
 			String soapToken = soapService.login(trackerInstance.getUser(), trackerInstance.getPass());
 
 			// Pobranie zdalnych wersji i ich sprawdzanie
 			RemoteVersion[] versions = soapService.getVersions(soapToken,
 					this.projectKey);
 			for (RemoteVersion version : versions) {
 				// Z wersjami wydanymi nic nie chcemy robic
 				if (version.isReleased() || version.isArchived()) {
 					continue;
 				}
 				// Pomijamy również wersje nie pasujące do wzorca
 				if (!Pattern.matches(versionNumberFullRegex, version.getName())) {
 					// System.out.println(version.getName() +
 					// " nie pasuje do wzorca: " + versionNumberFullRegex);
 					continue;
 				}
 				// Teraz wiemy już, że znaleziona wersja, to ta odpowiadająca
 				// poprzednio zbudowanej wersji w Hudson
 				Matcher matcher = versionNumberPrefixPattern.matcher(version
 						.getName());
 				if (matcher.find()) {
 					newVerName = matcher.group();
 					version.setReleased(true);
 					version.setReleaseDate(Calendar.getInstance());
 					soapService.releaseVersion(soapToken, projectKey, version);
 					listener.getLogger().println(
 							"JIRA: Wydano wersje " + version.getName()
 									+ " w projekcie " + this.getProjectKey());
 					break;
 				}
 			}
 
 			if (newVerName != null) {
 				// tworzymy nową wersję
 				String newVersionFullName = newVerName
 						+ (build.getNumber() + 1);
 				RemoteVersion newVer = new RemoteVersion();
 				newVer.setName(newVersionFullName);
 				soapService.addVersion(soapToken, projectKey, newVer);
 				listener.getLogger().println(
 						"JIRA: Utworzono wersje " + newVersionFullName
 								+ " w projekcie " + this.getProjectKey());
 				return true;
 			} else {
 				listener.getLogger()
 						.println(
 								"JIRA: W JIRA nie odnaleziono wersji odpowiadającej biezacemu numerowi kopilacji");
 			}
 			soapService.logout(soapToken);
 		} catch (ServiceException e) {
 			System.out.println(new StringBuilder().append("[")
 					.append(e.getClass().getSimpleName()).append("] ")
 					.append(e.getMessage()).append(": \n")
 					.append(e.getStackTrace()).toString());
 		} catch (RemoteAuthenticationException e) {
 			listener.getLogger().println(
 					"JIRA: Nie uwierzytelniono uzytkownika");
 			System.out.println(new StringBuilder().append("[")
 					.append(e.getClass().getSimpleName()).append("] ")
 					.append(e.getMessage()).append(": \n")
 					.append(e.getStackTrace()).toString());
 		} catch (RemoteException e) {
 			System.out.println(new StringBuilder().append("[")
 					.append(e.getClass().getSimpleName()).append("] ")
 					.append(e.getMessage()).append(": \n")
 					.append(e.getStackTrace()).toString());
 		} catch (java.rmi.RemoteException e) {
 			System.out.println(new StringBuilder().append("[")
 					.append(e.getClass().getSimpleName()).append("] ")
 					.append(e.getMessage()).append(": \n")
 					.append(e.getStackTrace()).toString());
 		} catch (Exception e) {
 			System.out.println(new StringBuilder().append("[")
 					.append(e.getClass().getSimpleName()).append("] ")
 					.append(e.getMessage()).append(": \n")
 					.append(e.getStackTrace()).toString());
 		}
 		return false;
 	}
 
 	@Override
 	public DescriptorImpl getDescriptor() {
 		return (DescriptorImpl) super.getDescriptor();
 	}
 
 	/**
 	 * Descriptor for {@link JiraVersionReleasePublisher}. Used as a singleton.
 	 * The class is marked as public so that it can be accessed from views.
 	 *
 	 * <p>
 	 * See
 	 * <tt>views/hudson/plugins/hello_world/JiraVersionReleasePublisher/*.jelly</tt>
 	 * for the actual HTML fragment for the configuration screen.
 	 */
 	@Extension
 	public static final class DescriptorImpl extends
 			BuildStepDescriptor<Publisher> {
 
 		private final CopyOnWriteList<TrackerInstance> instances = new CopyOnWriteList<TrackerInstance>();
 
 
 		public DescriptorImpl() {
 			super(JiraVersionReleasePublisher.class);
 			load();
 		}
 
 		@Override
 		public String getDisplayName() {
			return Messages.displayName();
 		}
 
 		@Override
 		public String getHelpFile() {
 			return "/plugin/jiraVersionRelease/help-config.html";
 		}
 
 		@Override
 		public boolean isApplicable(@SuppressWarnings("rawtypes") Class<? extends AbstractProject> jobType) {
 			return AbstractProject.class.isAssignableFrom(jobType);
 		}
 
 		public void setInstances(TrackerInstance instance) {
 			instances.add(instance);
 		}
 
 		public TrackerInstance[] getInstances() {
 			return instances.toArray(new TrackerInstance[instances.size()]);
 		}
 	}
 }
