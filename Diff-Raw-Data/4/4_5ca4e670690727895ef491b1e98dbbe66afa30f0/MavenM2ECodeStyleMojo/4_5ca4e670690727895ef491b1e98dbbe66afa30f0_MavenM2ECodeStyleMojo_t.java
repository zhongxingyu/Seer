 package com.despegar.maven.plugin.eclipse.maven;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileReader;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.io.PrintWriter;
 import java.io.StringReader;
 import java.util.Map.Entry;
 
 import org.apache.commons.io.IOUtils;
 import org.apache.http.HttpResponse;
 import org.apache.http.client.ClientProtocolException;
 import org.apache.http.client.HttpClient;
 import org.apache.http.client.methods.HttpGet;
 import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
 import org.apache.maven.plugin.AbstractMojo;
 import org.apache.maven.plugin.MojoExecutionException;
 import org.apache.maven.plugin.MojoFailureException;
 import org.sonatype.plexus.build.incremental.BuildContext;
 
 import com.despegar.tools.hash.HashHelper;
 
 /**
  * @phase process-resources
  * @goal configure
  * 
  * @author gkondolf@despegar.com
  */
 public class MavenM2ECodeStyleMojo extends AbstractMojo {
 
 	private static final String ECLIPSE_SETTINGS_FOLDER = ".settings";
 	// private static final String MAVEN_CODE_STYLE_BASE_URL_PROPERTY =
 	// "m2e.codestyle.base.url";
 	private static final String CORE_PREFS_FILE = "org.eclipse.core.resources.prefs";
 	private static final String JDT_CORE_PREFS_FILE = "org.eclipse.jdt.core.prefs";
 	private static final String JDT_UI_PREFS_FILE = "org.eclipse.jdt.ui.prefs";
 
 	/**
 	 * @component
 	 */
 	private BuildContext buildContext;
 
 	/**
 	 * Code style base url.
 	 * 
 	 * @parameter expression="${codeStyleBaseUrl}"
 	 * @required
 	 */
 	private String codeStyleBaseUrl;
 
 	/**
 	 * Base Directory.
 	 * 
 	 * @parameter expression="${project.basedir}"
 	 * @required
 	 */
 	private File baseDir;
 
	private HttpClient httpClient = new DefaultHttpClient(new ThreadSafeClientConnManager());
 
 	public void execute() throws MojoExecutionException, MojoFailureException {
 		if (isRunningFromEclipse()) {
 			File pomFile = new File(baseDir, "pom.xml");
 			if (buildContext.hasDelta(pomFile) || !buildContext.isIncremental()) {
 				getLog().info(
 						"Using code-style base-url [" + codeStyleBaseUrl
 								+ "]...");
 
 				File sttDir = new File(baseDir, ECLIPSE_SETTINGS_FOLDER);
 				if (sttDir.exists()) {
 					sttDir.mkdir();
 				}
 
 				configureJDTUIPrefs(sttDir, codeStyleBaseUrl);
 				configureJDTCorePrefs(sttDir, codeStyleBaseUrl);
 				configureCorePrefs(sttDir, codeStyleBaseUrl);
 
 				getLog().info("Refreshing newly created settings...");
 				buildContext.refresh(sttDir);
 			} else {
 				getLog().info(
 						"pom.xml didn't change or incremental build detected, nothing to do.");
 			}
 		} else {
 			getLog().info("Eclipse not detected, exiting!");
 		}
 	}
 
 	private boolean isRunningFromEclipse() {
 		int hitCount = 0;
 		for (Entry<Object, Object> p : System.getProperties().entrySet()) {
 			String key = p.getKey().toString().trim().toLowerCase();
 			if (key.startsWith("eclipse.vmargs")
 					|| key.startsWith("eclipse.vm")
 					|| key.startsWith("eclipse.startTime")
 					|| key.startsWith("eclipse.commands")
 					|| key.startsWith("eclipse.launcher")) {
 				hitCount++;
 			}
 			// getLog().info(">> " + p.getKey() + " = " + p.getValue());
 		}
 
 		return hitCount >= 3;
 	}
 
 	private void configureJDTUIPrefs(File dir, String baseUrl) {
 		downloadAndWriteFile(dir, baseUrl, JDT_UI_PREFS_FILE);
 	}
 
 	private void configureJDTCorePrefs(File dir, String baseUrl) {
 		downloadAndWriteFile(dir, baseUrl, JDT_CORE_PREFS_FILE);
 	}
 
 	private void configureCorePrefs(File dir, String baseUrl) {
 		downloadAndWriteFile(dir, baseUrl, CORE_PREFS_FILE);
 	}
 
 	private void downloadAndWriteFile(File dir, String baseUrl, String fileName) {
 
 		getLog().info("Checking new version of [" + fileName + "]...");
 		String content = getUrlContentAsString(baseUrl + "/" + fileName);
 
 		if (content != null) {
 
 			File target = new File(dir, fileName);
 			if (target.exists()) {
 				String md5Old = HashHelper
 						.md5(cleanUpString(readTextFile(target)));
 				getLog().info(
 						"Fingerprint for [" + fileName + "] = {" + md5Old + "}");
 
 				String md5New = HashHelper.md5(cleanUpString(content));
 				getLog().info(
 						"Fingerprint for [" + fileName + "] = {" + md5New + "}");
 
 				if (!md5New.equals(md5Old)) {
 					writeTextFile(target, content);
 					getLog().info("File [" + fileName + "] updated.");
 				} else {
 					getLog().info("File [" + fileName + "] didn't change!");
 				}
 			} else {
 				writeTextFile(target, content);
 				getLog().info("New file [" + fileName + "] created.");
 			}
 		}
 	}
 
 	private String readTextFile(File file) {
 		StringBuilder sb = new StringBuilder();
 
 		if (file.exists()) {
 			FileReader inFile = null;
 			try {
 				inFile = new FileReader(file);
 				BufferedReader in = new BufferedReader(inFile);
 				String text = null;
 				while ((text = in.readLine()) != null) {
 					sb.append(text)
 							.append(System.getProperty("line.separator"));
 				}
 			} catch (IOException e) {
 				getLog().warn(
 						"Exception reading file [" + file.getName() + "]", e);
 			} finally {
 				IOUtils.closeQuietly(inFile);
 			}
 		}
 
 		return sb.toString();
 	}
 
 	private void writeTextFile(File file, String content) {
 		FileWriter outFile = null;
 		try {
 			outFile = new FileWriter(file);
 			PrintWriter out = new PrintWriter(outFile);
 			out.write(content);
 		} catch (IOException e) {
 			getLog().warn("Exception writing file [" + file.getName() + "]", e);
 		} finally {
 			IOUtils.closeQuietly(outFile);
 		}
 	}
 
 	private String cleanUpString(String content) {
 		BufferedReader br = new BufferedReader(new StringReader(content));
 		StringBuilder sb = new StringBuilder();
 		String line = null;
 		try {
 			while ((line = br.readLine()) != null) {
 				sb.append(line).append("\n");
 			}
 		} catch (IOException e) {
 		}
 
 		return sb.toString();
 	}
 
 	private String getUrlContentAsString(String url) {
 
 		HttpResponse httpResponse = null;
 		try {
 			httpResponse = httpClient.execute(new HttpGet(url));
 		} catch (ClientProtocolException e1) {
 			throw new RuntimeException(e1);
 		} catch (IOException e1) {
 			throw new RuntimeException(e1);
 		}
 		if (httpResponse.getStatusLine().getStatusCode() == 404)
 			return null;
 
 		String response = null;
 		try {
 			response = IOUtils.toString(httpResponse.getEntity().getContent(),
 					"UTF-8");
 		} catch (IllegalStateException e) {
 			throw new RuntimeException(e);
 		} catch (IOException e) {
 			throw new RuntimeException(e);
 		}
 		return response;
 	}
 
 }
