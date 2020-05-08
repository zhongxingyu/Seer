 /******************************************************
  * JDBCMetrics for Jenkins
  * 
  *
  * Copyright (C) 2013 by Peter Hedenskog (http://peterhedenskog.com)
  *
  ******************************************************
  *
  * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in 
  * compliance with the License. You may obtain a copy of the License at
  * 
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software distributed under the License is 
  * distributed  on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.   
  * See the License for the specific language governing permissions and limitations under the License.
  *
  *******************************************************
  */
 package com.soulgalore.jenkins.plugins.jdbcmetrics;
 
 import hudson.Extension;
 import hudson.Launcher;
 import hudson.model.BuildListener;
 import hudson.model.AbstractBuild;
 import hudson.model.AbstractProject;
 import hudson.tasks.BuildStepDescriptor;
 import hudson.tasks.Builder;
 import hudson.util.FormValidation;
 
 import java.io.IOException;
 import java.io.PrintStream;
 import java.net.MalformedURLException;
 import java.net.URL;
 
 import javax.servlet.ServletException;
 
 import org.kohsuke.stapler.DataBoundConstructor;
 import org.kohsuke.stapler.QueryParameter;
 
 import com.google.inject.Guice;
 import com.google.inject.Injector;
 import com.soulgalore.crawler.core.Crawler;
 import com.soulgalore.crawler.core.CrawlerConfiguration;
 import com.soulgalore.crawler.core.CrawlerResult;
 import com.soulgalore.crawler.core.HTMLPageResponse;
 import com.soulgalore.crawler.core.PageURL;
 import com.soulgalore.crawler.guice.CrawlModule;
 import com.soulgalore.jenkins.plugins.jdbcmetrics.blocks.EnableAuthBlock;
 import com.soulgalore.jenkins.plugins.jdbcmetrics.blocks.EnableCrawlerInternalsBlock;
 import com.soulgalore.jenkins.plugins.jdbcmetrics.blocks.EnableCrawlerPathBlock;
 import com.soulgalore.jenkins.plugins.jdbcmetrics.blocks.EnableHeaderNameBlock;
 
 /**
  * Plugin hat checks the amount of database reads & writes per page by crawling
  * your site, sends a specific request header and fetch response headers created
  * by https://github.com/soulgalore/jdbcmetrics. If the number of database reads
  * or writes are more than the configured limit, the plugin will fail.
  */
 public class JDBCMetricsBuilder extends Builder {
 
 	private final String url;
 	private final int level;
 	private final String headerName;
 	private final int maxReads;
 	private final int maxWrites;
 	private final String login;
 	private final String password;
 	private final boolean checkHeader;
 	private final boolean checkAuth;
 	private final boolean checkCrawler;
 	private final boolean checkCrawlerPath;
 	private final String httpThreads;
 	private final String threadsPool;
 	private final String socketTimeout;
 	private final String connectionTimeout;
 	private final String followPath;
 	private final String notFollowPath;
 
 	/**
 	 * If no request header name is configured, this will be sent to the server,
 	 * with the values of <em>true</em> so that JDBCMetrics will send back the
 	 * number of database reads & writes.
 	 */
 	public final static String DEFAULT_HEADER_NAME = "jdbcmetrics";
 
 	/**
 	 * The header name that holds the number of database reads for a page.
 	 */
 	public final static String JDBC_READ_HEADER_NAME = "nr-of-reads";
 
 	/**
 	 * The header name that holds the number of database writes for a page.
 	 */
 	public final static String JDBC_WRITE_HEADER_NAME = "nr-of-writes";
 
 	@DataBoundConstructor
 	public JDBCMetricsBuilder(String url, int level, int maxReads,
 			int maxWrites, EnableAuthBlock checkAuth,
 			EnableHeaderNameBlock checkHeader,
 			EnableCrawlerInternalsBlock checkCrawler,
 			EnableCrawlerPathBlock checkCrawlerPath) {
 
 		this.url = url;
 		this.level = level;
 		this.maxReads = maxReads;
 		this.maxWrites = maxWrites;
 
 		this.headerName = checkHeader == null ? DEFAULT_HEADER_NAME
 				: checkHeader.getHeaderName();
 		this.checkHeader = checkHeader == null ? false : true;
 
 		this.login = checkAuth == null ? "" : checkAuth.getLogin();
 		this.password = checkAuth == null ? "" : checkAuth.getPassword();
 		this.checkAuth = checkAuth == null ? false : true;
 
 		this.httpThreads = checkCrawler == null ? "" : checkCrawler
 				.getHttpThreads();
 		this.threadsPool = checkCrawler == null ? "" : checkCrawler
 				.getThreadsPool();
 		this.socketTimeout = checkCrawler == null ? "" : checkCrawler
 				.getSocketTimeout();
 		this.connectionTimeout = checkCrawler == null ? "" : checkCrawler
 				.getConnectionTimeout();
 		this.checkCrawler = checkCrawler == null ? false : true;
 
 		this.followPath = checkCrawlerPath == null ? "" : checkCrawlerPath
 				.getFollowPath();
 		this.notFollowPath = checkCrawlerPath == null ? "" : checkCrawlerPath
 				.getNotFollowPath();
 		this.checkCrawlerPath = checkCrawlerPath == null ? false : true;
 
 	}
 
 	public String getConnectionTimeout() {
 		return connectionTimeout;
 	}
 
 	@Override
 	public DescriptorImpl getDescriptor() {
 		return (DescriptorImpl) super.getDescriptor();
 	}
 
 	public String getFollowPath() {
 		return followPath;
 	}
 
 	public String getHeaderName() {
 		return headerName;
 	}
 
 	public String getHttpThreads() {
 		return httpThreads;
 	}
 
 	public int getLevel() {
 		return level;
 	}
 
 	public String getLogin() {
 		return login;
 	}
 
 	public int getMaxReads() {
 		return maxReads;
 	}
 
 	public int getMaxWrites() {
 		return maxWrites;
 	}
 
 	public String getNotFollowPath() {
 		return notFollowPath;
 	}
 
 	public String getPassword() {
 		return password;
 	}
 
 	public String getSocketTimeout() {
 		return socketTimeout;
 	}
 
 	public String getThreadsPool() {
 		return threadsPool;
 	}
 
 	public String getUrl() {
 		return url;
 	}
 
 	public boolean isCheckAuth() {
 		return checkAuth;
 	}
 
 	public boolean isCheckCrawler() {
 		return checkCrawler;
 	}
 
 	public boolean isCheckCrawlerPath() {
 		return checkCrawlerPath;
 	}
 
 	public boolean isCheckHeader() {
 		return checkHeader;
 	}
 
 	@Override
 	public boolean perform(AbstractBuild build, Launcher launcher,
 			BuildListener listener) {
 
 		PrintStream logger = listener.getLogger();
 
 		if (!setupAuth(logger))
 			return false;
 
 		setupCrawlerInternals();
 
 		logger.println("Start crawling the URL:s ...");
 		final CrawlerResult result = crawl();
 
 		return verifyResult(result, logger);
 		
 	}
 
 	private boolean verifyResult(CrawlerResult result, PrintStream logger) {
 		
 		boolean isBreakingTheLaw = false;
 		
 		int totalWrites = 0;
 		int totalReads = 0;
 		
 		for (HTMLPageResponse response : result.getVerifiedURLResponses()) {
 
 			if (response.getHeaderValue(JDBC_READ_HEADER_NAME) == null
 					|| response.getHeaderValue(JDBC_WRITE_HEADER_NAME) == null) {
 				missingHeaders(logger, response);
 				return false;
 			}
 
 			int reads = Integer.parseInt(response
 					.getHeaderValue(JDBC_READ_HEADER_NAME));
 			int writes = Integer.parseInt(response
 					.getHeaderValue(JDBC_WRITE_HEADER_NAME));
 
 			if (reads > maxReads || writes > maxWrites)
 				isBreakingTheLaw = true;
 
 			logger.println(
 					response.getPageUrl().getUrl() + " reads:" + reads
 							+ " writes:" + writes);
 		}
 
 		logger.println(result.getVerifiedURLResponses().size()
 				+ " urls generated " + totalReads + " database reads & "
 				+ totalWrites + " database writes");
 		
 		if (isBreakingTheLaw)
 			return false;
 		else
 			return true;
 	}
 	
 	private void missingHeaders(PrintStream logger, HTMLPageResponse response) {
 
 		logger.println("Missing JDBCMetrics information from the server. The server should listen on request header ["
 				+ headerName
 				+ "]"
 				+ " . More information about JDBCMetrics here: https://github.com/soulgalore/jdbcmetrics");
 		logger.println("Got the following headers (for page "
 				+ response.getPageUrl().getUri() + " ):");
 
 		logger.println("-------------");
 		for (String key : response.getResponseHeaders().keySet()) {
 			logger.println(key + " : " + response.getHeaderValue(key));
 		}
 		logger.println("-------------");
 	}
 
 	private CrawlerResult crawl() {
 
 		CrawlerConfiguration configuration = CrawlerConfiguration.builder()
 				.setMaxLevels(level).setVerifyUrls(true)
 				.setOnlyOnPath(followPath).setNotOnPath(notFollowPath)
 				.setRequestHeaders(headerName + ":true").setStartUrl(url)
 				.build();
 
 		final Injector injector = Guice.createInjector(new CrawlModule());
 		final Crawler crawler = injector.getInstance(Crawler.class);
 
 		try {
 		return crawler.getUrls(configuration);
 		}
 		finally {
 			crawler.shutdown();
 		}
 	}
 
 	private void setupCrawlerInternals() {
 		if (!"".equals(httpThreads))
 			System.setProperty(CrawlerConfiguration.MAX_THREADS_PROPERTY_NAME,
 					httpThreads);
 		else if (!"".equals(threadsPool))
 			System.setProperty("com.soulgalore.crawler.threadsinworkingpool",
 					threadsPool);
 		else if (!"".equals(socketTimeout))
 			System.setProperty(
 					CrawlerConfiguration.SOCKET_TIMEOUT_PROPERTY_NAME,
 					socketTimeout);
 		else if (!"".equals(connectionTimeout))
 			System.setProperty(
 					CrawlerConfiguration.CONNECTION_TIMEOUT_PROPERTY_NAME,
 					connectionTimeout);
 	}
 
 	private boolean setupAuth(PrintStream logger) {
 
 		if (!"".equals(login) && !"".equals(password)) {
			logger.println("Will use Basic auth: " + login + " " + password);
 			try {
 				URL u = new URL(url);
 				String host = u.getHost()
 						+ (u.getPort() != -1 ? ":" + u.getPort() : ":80");
 				System.setProperty("com.soulgalore.crawler.auth", host + ":"
						+ login + ": ***SECRET***");

				logger.println("Will use:"
						+ System.getProperty("com.soulgalore.crawler.auth"));
 			} catch (MalformedURLException e) {
 				logger.println(e.toString());
 				return false;
 			}
 
 		}
 		return true;
 
 	}
 
 	@Extension
 	public static final class DescriptorImpl extends
 			BuildStepDescriptor<Builder> {
 
 		public FormValidation doCheckUrl(@QueryParameter String value)
 				throws IOException, ServletException {
 			if (value.length() == 0)
 				return FormValidation.error("Please set a start url");
 			if ((!value.startsWith("http://"))
 					&& (!value.startsWith("https://")))
 				return FormValidation
 						.warning("The url must start with http:// or https:// !");
 			return FormValidation.ok();
 		}
 
 		public String getDisplayName() {
 			return "JDBCMetrics";
 		}
 
 		public boolean isApplicable(Class<? extends AbstractProject> aClass) {
 			return true;
 		}
 	}
 }
