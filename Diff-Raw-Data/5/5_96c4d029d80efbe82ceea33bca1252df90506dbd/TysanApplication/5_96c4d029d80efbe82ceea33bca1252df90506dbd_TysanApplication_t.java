 /**
  * Tysan Clan Website
  * Copyright (C) 2008-2011 Jeroen Steenbeeke and Ties van de Ven
  *
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 package com.tysanclan.site.projectewok;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.HashSet;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Properties;
 import java.util.Set;
 
 import javax.servlet.http.HttpServletRequest;
 
 import org.apache.wicket.Application;
 import org.apache.wicket.Page;
 import org.apache.wicket.Session;
 import org.apache.wicket.protocol.http.WebApplication;
 import org.apache.wicket.request.Request;
 import org.apache.wicket.request.Response;
 import org.apache.wicket.request.http.WebRequest;
 import org.apache.wicket.request.resource.CssResourceReference;
 import org.apache.wicket.spring.injection.annot.SpringComponentInjector;
import org.odlabs.wiquery.ui.themes.WiQueryCoreThemeResourceReference;
 import org.quartz.Scheduler;
 import org.quartz.SchedulerException;
 import org.quartz.impl.StdSchedulerFactory;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.context.ApplicationContext;
 import org.springframework.context.support.ClassPathXmlApplicationContext;
 import org.springframework.web.context.support.WebApplicationContextUtils;
 
 import com.tysanclan.site.projectewok.auth.TysanSecurity;
 import com.tysanclan.site.projectewok.pages.AboutPage;
 import com.tysanclan.site.projectewok.pages.AccessDeniedPage;
 import com.tysanclan.site.projectewok.pages.CharterPage;
 import com.tysanclan.site.projectewok.pages.ForumOverviewPage;
 import com.tysanclan.site.projectewok.pages.ForumPage;
 import com.tysanclan.site.projectewok.pages.ForumThreadPage;
 import com.tysanclan.site.projectewok.pages.GroupPage;
 import com.tysanclan.site.projectewok.pages.GroupsPage;
 import com.tysanclan.site.projectewok.pages.HistoryPage;
 import com.tysanclan.site.projectewok.pages.JoinOverviewPage;
 import com.tysanclan.site.projectewok.pages.MemberPage;
 import com.tysanclan.site.projectewok.pages.NewsPage;
 import com.tysanclan.site.projectewok.pages.PasswordRequestConfirmationPage;
 import com.tysanclan.site.projectewok.pages.RealmPage;
 import com.tysanclan.site.projectewok.pages.RegistrationPage;
 import com.tysanclan.site.projectewok.pages.RegulationPage;
 import com.tysanclan.site.projectewok.pages.RosterPage;
 import com.tysanclan.site.projectewok.pages.SessionTimeoutPage;
 import com.tysanclan.site.projectewok.pages.TysanErrorPage;
 import com.tysanclan.site.projectewok.pages.VacationPage;
 import com.tysanclan.site.projectewok.pages.forum.ActivationPage;
 import com.tysanclan.site.projectewok.pages.member.BugOverviewPage;
 import com.tysanclan.site.projectewok.pages.member.SubscriptionPaymentResolvedPage;
 import com.tysanclan.site.projectewok.pages.member.ViewBugPage;
 import com.tysanclan.site.projectewok.pages.member.admin.MinecraftWhiteListPage;
 import com.tysanclan.site.projectewok.pages.member.admin.OldExpensesPage;
 import com.tysanclan.site.projectewok.pages.member.admin.ProcessPaymentRequestPage;
 import com.tysanclan.site.projectewok.pages.member.admin.RandomContentGenerationPage;
 import com.tysanclan.site.projectewok.pages.member.admin.TangoImporterPage;
 import com.tysanclan.site.projectewok.tasks.AcceptanceVoteStartTask;
 import com.tysanclan.site.projectewok.tasks.AcceptanceVoteStopTask;
 import com.tysanclan.site.projectewok.tasks.AchievementProposalTask;
 import com.tysanclan.site.projectewok.tasks.ActivationExpirationTask;
 import com.tysanclan.site.projectewok.tasks.AutomaticPromotionTask;
 import com.tysanclan.site.projectewok.tasks.ChancellorElectionChecker;
 import com.tysanclan.site.projectewok.tasks.ChancellorElectionResolutionTask;
 import com.tysanclan.site.projectewok.tasks.CheckSubscriptionsDueTask;
 import com.tysanclan.site.projectewok.tasks.EmailChangeConfirmationExpirationTask;
 import com.tysanclan.site.projectewok.tasks.GroupLeaderElectionResolutionTask;
 import com.tysanclan.site.projectewok.tasks.MemberApplicationResolutionTask;
 import com.tysanclan.site.projectewok.tasks.MembershipExpirationTask;
 import com.tysanclan.site.projectewok.tasks.MumbleServerUpdateTask;
 import com.tysanclan.site.projectewok.tasks.NoAccountExpireTask;
 import com.tysanclan.site.projectewok.tasks.PasswordRequestExpirationTask;
 import com.tysanclan.site.projectewok.tasks.RegulationChangeResolutionTask;
 import com.tysanclan.site.projectewok.tasks.ResolveImpeachmentTask;
 import com.tysanclan.site.projectewok.tasks.ResolveRoleTransferTask;
 import com.tysanclan.site.projectewok.tasks.ResolveTruthsayerComplaintTask;
 import com.tysanclan.site.projectewok.tasks.SenateElectionChecker;
 import com.tysanclan.site.projectewok.tasks.SenateElectionResolutionTask;
 import com.tysanclan.site.projectewok.tasks.TruthsayerAcceptanceVoteResolver;
 import com.tysanclan.site.projectewok.tasks.TwitterQueueTask;
 import com.tysanclan.site.projectewok.tasks.TwitterSearchTask;
 import com.tysanclan.site.projectewok.tasks.TwitterTimelineTask;
 import com.tysanclan.site.projectewok.tasks.UntenabilityVoteResolutionTask;
 import com.tysanclan.site.projectewok.tasks.WarnInactiveMembersTask;
 import com.tysanclan.site.projectewok.util.scheduler.TysanScheduler;
 import com.tysanclan.site.projectewok.util.scheduler.TysanTask;
 
 /**
  * @author Jeroen Steenbeeke
  */
 public class TysanApplication extends WebApplication {
 	private static Logger log = LoggerFactory.getLogger(TysanApplication.class);
 
 	private static String version = null;
 
 	public static final String MASTER_KEY = "Sethai Janora Kumirez Dechai";
 
 	public final List<SiteWideNotification> notifications = new LinkedList<SiteWideNotification>();
 
 	private static ApplicationContext testContext = null;
 
 	/**
 	 * Creates a new application object for the Tysan website
 	 */
 	public TysanApplication() {
 		this(false);
 	}
 
 	public static ApplicationContext getApplicationContext() {
 		if (testContext != null)
 			return testContext;
 
 		TysanApplication ta = (TysanApplication) Application.get();
 		return WebApplicationContextUtils.getWebApplicationContext(ta
 				.getServletContext());
 	}
 
 	public static String getApplicationVersion() {
 		if (version == null) {
 			try {
 				Properties props = new Properties();
 
 				InputStream stream = get().getServletContext()
 						.getResourceAsStream("/META-INF/MANIFEST.MF");
 
 				if (stream != null) {
 
 					props.load(stream);
 
 					version = props.getProperty("Implementation-Build");
 				} else {
 					version = "SNAPSHOT";
 				}
 			} catch (IOException ioe) {
 				version = "SNAPSHOT";
 			}
 		}
 
 		return version;
 	}
 
 	private final boolean testMode;
 
 	/**
 	 * Creates a new application object for the Tysan website
 	 * 
 	 * @param isTestMode
 	 *            Whether or not we are running this site in test mode
 	 */
 	public TysanApplication(boolean isTestMode) {
 		this.testMode = isTestMode;
 	}
 
 	/**
 	 * @see org.apache.wicket.Application#getHomePage()
 	 */
 	@Override
 	public Class<? extends Page> getHomePage() {
 		return NewsPage.class;
 	}
 
 	/**
 	 * @see org.apache.wicket.protocol.http.WebApplication#init()
 	 */
 	@Override
 	protected void init() {
 		super.init();
 
 		ApplicationContext relevantCtx;
 
 		getComponentInstantiationListeners().add(new TysanSecurity());
 		if (testMode) {
 			relevantCtx = new ClassPathXmlApplicationContext(
 					new String[] { "services-mock.xml" });
 			getComponentInstantiationListeners().add(
 					new SpringComponentInjector(this, relevantCtx, true));
 			testContext = relevantCtx;
 		} else {
 			SpringComponentInjector injector = new SpringComponentInjector(this);
 			getComponentInstantiationListeners().add(injector);
 		}
 
 		mountBookmarkablePages();
 
 		getApplicationSettings().setAccessDeniedPage(AccessDeniedPage.class);
 		getApplicationSettings().setPageExpiredErrorPage(
 				SessionTimeoutPage.class);
 
 		if (!testMode) {
 			scheduleDefaultTasks();
 		}
 
 		if (usesDeploymentConfig())
 			getApplicationSettings().setInternalErrorPage(TysanErrorPage.class);
 
		addResourceReplacement(WiQueryCoreThemeResourceReference.get(),
				new CssResourceReference(TysanApplication.class,
						"themes/ui-darkness/jquery-ui-1.10.2.custom.css"));
 	}
 
 	/**
 	 * 
 	 */
 	private void scheduleDefaultTasks() {
 		TysanScheduler.getScheduler().setApplication(this);
 
 		TysanTask[] tasks = { new AcceptanceVoteStartTask(),
 				new AcceptanceVoteStopTask(), new ActivationExpirationTask(),
 				new AutomaticPromotionTask(), new ChancellorElectionChecker(),
 				new ChancellorElectionResolutionTask(),
 				new EmailChangeConfirmationExpirationTask(),
 				new GroupLeaderElectionResolutionTask(),
 				new MemberApplicationResolutionTask(),
 				new MembershipExpirationTask(),
 				new PasswordRequestExpirationTask(),
 				new RegulationChangeResolutionTask(),
 				new ResolveImpeachmentTask(), new SenateElectionChecker(),
 				new SenateElectionResolutionTask(),
 				new TruthsayerAcceptanceVoteResolver(),
 				new UntenabilityVoteResolutionTask(), new TwitterQueueTask(),
 				new TwitterTimelineTask(), new TwitterSearchTask(),
 				new AchievementProposalTask(), new WarnInactiveMembersTask(),
 				new ResolveRoleTransferTask(), new CheckSubscriptionsDueTask(),
 				new ResolveTruthsayerComplaintTask(),
 				new MumbleServerUpdateTask() };
 
 		for (TysanTask task : tasks) {
 			TysanScheduler.getScheduler().scheduleTask(task);
 
 		}
 
 		if (System.getProperty("tysan.debug") != null) {
 			TysanScheduler.getScheduler().scheduleTask(
 					new NoAccountExpireTask());
 		}
 	}
 
 	/**
 	 * 
 	 */
 	private void mountBookmarkablePages() {
 
 		mountPage("/news", NewsPage.class);
 		mountPage("/charter", CharterPage.class);
 		mountPage("/about", AboutPage.class);
 		mountPage("/history", HistoryPage.class);
 		mountPage("/member/${userid}", MemberPage.class);
 		mountPage("/members", RosterPage.class);
 		mountPage("/regulations", RegulationPage.class);
 		mountPage("/realm/${id}", RealmPage.class);
 
 		mountPage("groups", GroupsPage.class);
 
 		mountPage("join", JoinOverviewPage.class);
 
 		mountPage("vacation", VacationPage.class);
 
 		mountPage("/threads/${threadid/${pageid}", ForumThreadPage.class);
 		mountPage("/group/${groupid}", GroupPage.class);
 
 		mountPage("/forums/${forumid}/${pageid}", ForumPage.class);
 		mountPage("/listforums", ForumOverviewPage.class);
 		mountPage("/register", RegistrationPage.class);
 		mountPage("/activation/${key}", ActivationPage.class);
 
 		mountPage("/resetpassword/${key}",
 				PasswordRequestConfirmationPage.class);
 
 		mountPage("/accessdenied", AccessDeniedPage.class);
 
 		mountPage("/mc-whitelist", MinecraftWhiteListPage.class);
 
 		mountPage("/tracker/${mode}", BugOverviewPage.class);
 
 		mountPage("/processPaymentRequest/${requestId}/${confirmationKey}",
 				ProcessPaymentRequestPage.class);
 
 		mountPage(
 				"/processSubscriptionPayment/${paymentId}/${confirmationKey}",
 				SubscriptionPaymentResolvedPage.class);
 
 		mountPage("/bug/${id}", ViewBugPage.class);
 		mountPage("/feature/${id}", ViewBugPage.class);
 
 		if (System.getProperty("tysan.install") != null) {
 			mountPage("/tangoimport", TangoImporterPage.class);
 			mountPage("/randomcontent", RandomContentGenerationPage.class);
 			mountPage("/oldexpenses", OldExpensesPage.class);
 		}
 	}
 
 	/**
 	 * @return The current TysanApplication
 	 */
 	public static TysanApplication get() {
 		return (TysanApplication) Application.get();
 	}
 
 	@Override
 	public Session newSession(Request request, Response response) {
 		return new TysanSession(request);
 	}
 
 	@Override
 	public WebRequest newWebRequest(HttpServletRequest servletRequest,
 			String filterPath) {
 		WebRequest request = super.newWebRequest(servletRequest, filterPath);
 		getSessionStore().setAttribute(
 				request,
 				"wickery-theme",
 				new CssResourceReference(TysanApplication.class,
 						"themes/ui-darkness/jquery-ui-1.7.2.custom.css"));
 
 		return request;
 	}
 
 	/**
 	 * @see org.apache.wicket.Application#onDestroy()
 	 */
 	@Override
 	protected void onDestroy() {
 		super.onDestroy();
 
 		try {
 			Scheduler scheduler = StdSchedulerFactory.getDefaultScheduler();
 			scheduler.shutdown();
 		} catch (SchedulerException e) {
 			log.error("Could not stop Quartz Scheduler", e);
 		}
 	}
 
 	public void notify(SiteWideNotification notification) {
 		synchronized (notifications) {
 			notifications.add(notification);
 		}
 	}
 
 	public List<SiteWideNotification> getActiveNotifications() {
 		synchronized (notifications) {
 			Set<SiteWideNotification> exit = new HashSet<SiteWideNotification>();
 
 			for (SiteWideNotification next : notifications) {
 				if (next.isExpired()) {
 					exit.add(next);
 				}
 			}
 
 			notifications.removeAll(exit);
 
 			return notifications;
 		}
 	}
 
 	public static class VersionDescriptor implements
 			Comparable<VersionDescriptor> {
 		public static VersionDescriptor of(String ver) {
 			String[] versionData = ver.split("\\.", 4);
 
 			int major = Integer.parseInt(versionData[0]);
 			int minor = Integer.parseInt(versionData[1]);
 			int date = Integer.parseInt(versionData[2]);
 			int time = Integer.parseInt(versionData[3]);
 
 			return new VersionDescriptor(major, minor, date, time);
 		}
 
 		private final Integer major;
 
 		private final Integer minor;
 
 		private final Integer date;
 
 		private final Integer time;
 
 		public VersionDescriptor(int major, int minor, int date, int time) {
 			super();
 			this.major = major;
 			this.minor = minor;
 			this.date = date;
 			this.time = time;
 		}
 
 		public int getMajor() {
 			return major;
 		}
 
 		public int getMinor() {
 			return minor;
 		}
 
 		public int getDate() {
 			return date;
 		}
 
 		public int getTime() {
 			return time;
 		}
 
 		@Override
 		public int compareTo(VersionDescriptor o) {
 			int compareMajor = major.compareTo(o.major);
 			int compareMinor = minor.compareTo(o.minor);
 			int compareDate = date.compareTo(o.date);
 			int compareTime = time.compareTo(o.time);
 
 			if (compareMajor != 0)
 				return compareMajor;
 
 			if (compareMinor != 0)
 				return compareMinor;
 
 			if (compareDate != 0)
 				return compareDate;
 
 			if (compareTime != 0)
 				return compareTime;
 
 			return 0;
 		}
 
 		@Override
 		public int hashCode() {
 			final int prime = 31;
 			int result = 1;
 			result = prime * result + ((date == null) ? 0 : date.hashCode());
 			result = prime * result + ((major == null) ? 0 : major.hashCode());
 			result = prime * result + ((minor == null) ? 0 : minor.hashCode());
 			result = prime * result + ((time == null) ? 0 : time.hashCode());
 			return result;
 		}
 
 		@Override
 		public boolean equals(Object obj) {
 			if (this == obj)
 				return true;
 			if (obj == null)
 				return false;
 			if (getClass() != obj.getClass())
 				return false;
 			VersionDescriptor other = (VersionDescriptor) obj;
 			if (date == null) {
 				if (other.date != null)
 					return false;
 			} else if (!date.equals(other.date))
 				return false;
 			if (major == null) {
 				if (other.major != null)
 					return false;
 			} else if (!major.equals(other.major))
 				return false;
 			if (minor == null) {
 				if (other.minor != null)
 					return false;
 			} else if (!minor.equals(other.minor))
 				return false;
 			if (time == null) {
 				if (other.time != null)
 					return false;
 			} else if (!time.equals(other.time))
 				return false;
 			return true;
 		}
 
 		@Override
 		public String toString() {
 			return major + "." + minor + "." + date + "." + time;
 		}
 
 		public VersionDescriptor next() {
 
 			return new VersionDescriptor(major, minor, date, time + 1);
 		}
 
 	}
 }
