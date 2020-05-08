 package org.atlasapi.feeds.radioplayer.upload;
 
 import static org.hamcrest.MatcherAssert.assertThat;
 import static org.hamcrest.Matchers.equalTo;
 import static org.hamcrest.Matchers.hasItem;
 import static org.hamcrest.Matchers.is;
 import static org.hamcrest.Matchers.startsWith;
 
 import java.io.File;
 import java.io.FilenameFilter;
 import java.util.List;
 import java.util.Set;
 import java.util.concurrent.Executor;
 
 import org.apache.ftpserver.FtpServer;
 import org.apache.ftpserver.FtpServerFactory;
 import org.apache.ftpserver.ftplet.Authentication;
 import org.apache.ftpserver.ftplet.AuthenticationFailedException;
 import org.apache.ftpserver.ftplet.Authority;
 import org.apache.ftpserver.ftplet.AuthorizationRequest;
 import org.apache.ftpserver.ftplet.FtpException;
 import org.apache.ftpserver.ftplet.User;
 import org.apache.ftpserver.ftplet.UserManager;
 import org.apache.ftpserver.listener.ListenerFactory;
 import org.apache.ftpserver.usermanager.UsernamePasswordAuthentication;
 import org.apache.ftpserver.usermanager.impl.WritePermission;
 import org.atlasapi.content.criteria.ContentQuery;
 import org.atlasapi.feeds.radioplayer.RadioPlayerService;
 import org.atlasapi.feeds.radioplayer.RadioPlayerServices;
 import org.atlasapi.persistence.content.query.KnownTypeQueryExecutor;
 import org.atlasapi.persistence.logging.AdapterLog;
 import org.atlasapi.persistence.logging.AdapterLogEntry;
 import org.jmock.Expectations;
 import org.jmock.Mockery;
 import org.joda.time.DateTime;
 import org.junit.Test;
 
 import com.google.common.collect.ImmutableList;
 import com.google.common.collect.ImmutableSet;
 import com.google.common.io.Files;
 import com.google.common.util.concurrent.MoreExecutors;
 import com.metabroadcast.common.time.DateTimeZones;
 
 public class RadioPlayerFileUploaderTest {
 
 	private static final String TEST_PASSWORD = "testpassword";
 	private static final String TEST_USERNAME = "test";
 
 	private static File dir;
 
 	private FtpServer server;
 
 	@Test
 	public void testRun() throws Exception {
 		try {
 			dir = Files.createTempDir();
 			System.out.println(dir);
 			//dir.deleteOnExit();
 			File files = new File(dir.getAbsolutePath() + File.separator + "files");
 			files.mkdir();
 			
 			startServer();
 
 			Mockery context = new Mockery();
 
 			final KnownTypeQueryExecutor queryExecutor = context.mock(KnownTypeQueryExecutor.class);
 			final AdapterLog log = context.mock(AdapterLog.class);
 
 			context.checking(new Expectations() {
 				{
 					allowing(queryExecutor).executeItemQuery(with(any(ContentQuery.class)));
 					will(returnValue(ImmutableList.of()));
 					allowing(log).record(with(any(AdapterLogEntry.class)));
 				}
 			});
 
 			RadioPlayerFileUploader uploader = new RadioPlayerFileUploader("localhost", 9521, "test", "testpassword", "files", queryExecutor, log);
 
 			Executor executor = MoreExecutors.sameThreadExecutor();
 
 			executor.execute(uploader);
 			
 			Set<String> uploaded = ImmutableSet.copyOf(files.list(new FilenameFilter() {
 				@Override
 				public boolean accept(File dir, String name) {
 					return name.endsWith("PI.xml");
 				}
 			}));
 			
 			assertThat(uploaded.size(), is(equalTo(RadioPlayerServices.services.size() * 10)));
 			
 			DateTime day = new DateTime(DateTimeZones.UTC).minusDays(2);
			for (int i = 0; i < 10; i++, day = day.plusDays(i)) {
 				for (RadioPlayerService service : RadioPlayerServices.services) {
 					assertThat(uploaded, hasItem(startsWith(String.format("%4d%02d%02d_%s", day.getYear(), day.getMonthOfYear(), day.getDayOfMonth(), service.getRadioplayerId()))));
 				}
 			}
 			
 		} finally {
 			server.stop();
 		}
 
 	}
 
 	private void startServer() throws FtpException {
 		FtpServerFactory serverFactory = new FtpServerFactory();
 
 		ListenerFactory factory = new ListenerFactory();
 
 		factory.setPort(9521);
 
 		serverFactory.addListener("default", factory.createListener());
 
 		serverFactory.setUserManager(new TestUserManager());
 
 		server = serverFactory.createServer();
 		
 		server.start();
 	}
 
 	private class TestUserManager implements UserManager {
 
 		@Override
 		public User getUserByName(String username) throws FtpException {
 			if (username == TEST_USERNAME) {
 				return testUser;
 			}
 			return null;
 		}
 
 		@Override
 		public String[] getAllUserNames() throws FtpException {
 			return new String[] { TEST_USERNAME };
 		}
 
 		@Override
 		public void delete(String username) throws FtpException {
 			// no-op
 		}
 
 		@Override
 		public void save(User user) throws FtpException {
 		}
 
 		@Override
 		public boolean doesExist(String username) throws FtpException {
 			return username.equals(TEST_USERNAME);
 		}
 
 		@Override
 		public User authenticate(Authentication authentication) throws AuthenticationFailedException {
 			if (authentication instanceof UsernamePasswordAuthentication) {
 				UsernamePasswordAuthentication upauth = (UsernamePasswordAuthentication) authentication;
 				if (upauth.getUsername().equals(TEST_USERNAME)) {
 					return testUser;
 				}
 			}
 			throw new AuthenticationFailedException();
 		}
 
 		@Override
 		public String getAdminName() throws FtpException {
 			return "admin";
 		}
 
 		@Override
 		public boolean isAdmin(String username) throws FtpException {
 			return username.equals("admin");
 		}
 
 	}
 
 	private final User testUser = new User() {
 
 		@Override
 		public String getName() {
 			return TEST_USERNAME;
 		}
 
 		@Override
 		public String getPassword() {
 			return TEST_PASSWORD;
 		}
 
 		@Override
 		public List<Authority> getAuthorities() {
 			return ImmutableList.<Authority> of(new WritePermission());
 		}
 
 		@Override
 		public List<Authority> getAuthorities(Class<? extends Authority> clazz) {
 			if (clazz.equals(WritePermission.class)) {
 				return ImmutableList.<Authority> of(new WritePermission());
 			}
 			return ImmutableList.<Authority> of();
 		}
 
 		@Override
 		public AuthorizationRequest authorize(AuthorizationRequest request) {
 			return new WritePermission().authorize(request);
 		}
 
 		@Override
 		public int getMaxIdleTime() {
 			return 0;
 		}
 
 		@Override
 		public boolean getEnabled() {
 			return true;
 		}
 
 		@Override
 		public String getHomeDirectory() {
 			return dir.getAbsolutePath();
 		}
 	};
 }
