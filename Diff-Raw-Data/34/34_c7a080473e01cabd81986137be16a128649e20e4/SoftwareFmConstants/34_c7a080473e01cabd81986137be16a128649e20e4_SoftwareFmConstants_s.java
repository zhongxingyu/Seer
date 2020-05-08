 package org.softwareFm.eclipse.constants;
 
 import org.softwareFm.common.url.IUrlGenerator;
 import org.softwareFm.common.url.UrlGenerator;
 
 public class SoftwareFmConstants {
	public static final String urlPrefix = "softwarefm";
 
 	public static final String softwareFmServerUrl = "server1.softwarefm.com";
 	public static final String gitProtocolAndGitServerName = "git://git.softwarefm.com:80/";
 
 	public static final String groupIdKey = "groupId";
 	public static final String artifactIdKey = "artifactId";
 
 	public static final String usagePrefix = "command/Usage";
 
 	public static final String digest = "digest";
 	public static final String artifactId = "artifactId";
 	public static final String groupId = "groupId";
 	public static final String version = "version";
 
 	public static final String snipperFeedType = "feedType.snippet";
 	public static final String snipperTemplateKey = "snippet.template";
 	public static final String projectCryptoKey = "projectCrypto";
 	public static final String projectDirectoryName = "project";
 	public static final long usageRefreshTimeMs = 10 * 60 * 60 * 1000;// 10 hours
 
 	public static IUrlGenerator jarUrlGenerator(String prefix) {
 		return new UrlGenerator(prefix + "/jars/{0}/{1}/{2}", SoftwareFmConstants.digest);
 	}
 
 }
