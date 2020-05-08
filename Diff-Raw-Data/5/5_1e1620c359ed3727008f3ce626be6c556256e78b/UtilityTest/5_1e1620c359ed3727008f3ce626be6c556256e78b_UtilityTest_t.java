 package com.photon.phresco.util;
 
 import static org.junit.Assert.*;
 
 import java.io.BufferedReader;
 import java.io.BufferedWriter;
 import java.io.ByteArrayOutputStream;
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.FileReader;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.io.Reader;
 import java.lang.management.ManagementFactory;
 import java.lang.reflect.Constructor;
 import java.lang.reflect.InvocationTargetException;
 import java.util.ArrayList;
 import java.util.List;
 
 import org.apache.commons.io.FileUtils;
 import org.apache.commons.io.IOUtils;
 import org.junit.BeforeClass;
 import org.junit.Test;
 
 import com.google.gson.Gson;
 import com.google.gson.JsonIOException;
 import com.google.gson.JsonSyntaxException;
 import com.photon.phresco.commons.model.ApplicationInfo;
 import com.photon.phresco.commons.model.CIJob;
 import com.photon.phresco.commons.model.ContinuousDelivery;
 import com.photon.phresco.commons.model.ProjectDelivery;
 import com.photon.phresco.commons.model.ProjectInfo;
 import com.photon.phresco.exception.PhrescoException;
 import com.phresco.pom.exception.PhrescoPomException;
 import com.phresco.pom.util.PomProcessor;
 
 public class UtilityTest {
 		
 	@BeforeClass
     public static void before() throws IOException {
 		File projectFile = new File("src/test/resources/wp1-wordpress3.4.2");
 		File destDirectory = new File(Utility.getProjectHome());
 		FileUtils.copyDirectoryToDirectory(projectFile, destDirectory);
     }
 	
 	@Test
 	public void testInit() throws SecurityException, NoSuchMethodException, IllegalArgumentException, InstantiationException, IllegalAccessException, InvocationTargetException {
 		Constructor<Utility> reflected = Utility.class.getDeclaredConstructor();
 		reflected.setAccessible(true);
 		Utility initialize = reflected.newInstance();
 	}
 	
 	@Test
 	public void testIsEmpty() {
 		assertNotNull(Utility.isEmpty("notEmpty"));
 	}
 	
 	@Test
 	public void testCloseInputStream() {
 		InputStream iStream = IOUtils.toInputStream("test input stream");
 		Utility.closeStream(iStream);
 	}
 	
 	@Test
 	public void testCloseOutputStream() {
 		ByteArrayOutputStream oStream = new ByteArrayOutputStream();
 		oStream.write(0);
 		Utility.closeStream(oStream);
 	}
 	
 	@Test
 	public void testCloseInputReaderStream() {
 		InputStream iStream = IOUtils.toInputStream("test input stream");
 		Reader inputStream = new BufferedReader(new InputStreamReader(iStream));
 		Utility.closeStream(inputStream);
 	}
 	
 	@Test
 	public void testCloseFileWriterStream() throws IOException {
 		FileWriter wStream = new FileWriter("testFile");
 		Utility.closeStream(wStream);
 		File file = new File("testFile");
 		file.delete();
 	}
 	
 	@Test
 	public void testGetPhrescoHome() {
 		assertNotNull(Utility.getPhrescoHome());
 	}
 	
 	@Test
 	public void testGetPomProcessor() throws PhrescoException {
 		ApplicationInfo appInfo = getApplicationInfo("wp1-wordpress3.4.2");
 		assertNotNull(Utility.getPomProcessor(appInfo));
 	}
 		
 	@Test
 	public void testGetPomFileName() throws PhrescoException {
 		ApplicationInfo appInfo = getApplicationInfo("wp1-wordpress3.4.2");
 		assertNotNull(Utility.getPomFileName(appInfo));
 	}
 	
 	@Test
 	public void testGetLocalRepoPath() throws PhrescoException {
 		assertNotNull(Utility.getLocalRepoPath());
 	}
 	
 	@Test
 	public void testGetProjectHome() throws PhrescoException {
 		assertNotNull(Utility.getProjectHome());
 	}
 	
 	@Test
 	public void testGetPhrescoTemp() throws PhrescoException {
 		assertNotNull(Utility.getPhrescoTemp());
 	}
 	
 	@Test
 	public void testGetArchiveHome() throws PhrescoException {
 		assertNotNull(Utility.getArchiveHome());
 	}
 	
 	@Test
 	public void testGetSystemTemp() throws PhrescoException {
 		assertNotNull(Utility.getSystemTemp());
 	}
 	
 	@Test
 	public void testGetJenkinsHome() throws PhrescoException {
 		assertNotNull(Utility.getJenkinsHome());
 	}
 	
 	@Test
 	public void testGetJenkinsHomePluginDir() throws PhrescoException {
 		assertNotNull(Utility.getJenkinsHomePluginDir());
 	}
 	
 	@Test
 	public void testGetJenkinsTemplateDir() throws PhrescoException {
 		assertNotNull(Utility.getJenkinsTemplateDir());
 	}
 	
 	@Test
 	public void testCloseReader() throws PhrescoException {
 		InputStream iStream = IOUtils.toInputStream("test input stream");
 		BufferedReader reader = new BufferedReader(new InputStreamReader(iStream));
 		Utility.closeReader(reader);
 	}
 	
 	@Test
 	public void testCloseWriter() throws PhrescoException, IOException {
 		BufferedWriter writer = new BufferedWriter(new FileWriter("testFile"));
 		Utility.closeWriter(writer);
 		File file = new File("testFile");
 		file.delete();
 	}
 	
 	@Test
 	public void testExecuteCommand() throws PhrescoException, PhrescoPomException {
 		ApplicationInfo appInfo = getApplicationInfo("wp1-wordpress3.4.2");
 		PomProcessor processor = Utility.getPomProcessor(appInfo);
 		String directory = processor.getProperty(Constants.POM_PROP_KEY_FUNCTEST_DIR);
 		String workingDir = Utility.getProjectHome()+File.separator+"wp1-wordpress3.4.2"+directory;
 		Utility.executeCommand("mvn validate", workingDir);
 	}
 	
 	@Test
 	public void testExecuteStreamconsumerError() throws PhrescoPomException, PhrescoException {
 		String command = "cucumber -f junit -o target -f html -o target/cuke.html";
 		ApplicationInfo appInfo = getApplicationInfo("wp1-wordpress3.4.2");
 		PomProcessor processor = Utility.getPomProcessor(appInfo);
 		String directory = processor.getProperty(Constants.POM_PROP_KEY_FUNCTEST_DIR);
 		String baseDir = Utility.getProjectHome()+File.separator+"wp1-wordpress3.4.2";
 		String workingDir = baseDir+directory;
 		assertNotNull(Utility.executeStreamconsumer(command, workingDir, baseDir, "functional"));
 	}
 	
 	@Test
 	public void testExecuteStreamconsumer() throws PhrescoPomException, PhrescoException {
 		String command = "mvn clean test -f pom.xml";
 		ApplicationInfo appInfo = getApplicationInfo("wp1-wordpress3.4.2");
 		PomProcessor processor = Utility.getPomProcessor(appInfo);
 		String directory = processor.getProperty(Constants.POM_PROP_KEY_UNITTEST_DIR);
 		String baseDir = Utility.getProjectHome()+File.separator+"wp1-wordpress3.4.2";
 		String workingDir = baseDir+directory;
 		assertNotNull(Utility.executeStreamconsumer(command, workingDir, baseDir, "unit"));
 	}
 	
	//@Test
 	public void testExecuteconsumerSecondError() throws PhrescoPomException, FileNotFoundException {
 		File pomPath = new File(Utility.getProjectHome()+File.separator+"wp1-wordpress3.4.2"+File.separator+"pom.xml");
 		String baseDir = Utility.getProjectHome()+"wp1-wordpress3.4.2";
 		PomProcessor processor = new PomProcessor(pomPath);
 		String functionalTestDir = processor.getProperty(Constants.POM_PROP_KEY_FUNCTEST_DIR);
 		String command = "java -jar "+baseDir+functionalTestDir+"/lib/selenium-server-standalone-2.30.0.jar -role hub -hubConfig "+baseDir+functionalTestDir+"/hubconfig.json";
 		File LogDir = new File(Utility.getProjectHome()+File.separator+"wp1-wordpress3.4.2" + File.separator + Constants.DO_NOT_CHECKIN_DIRY + File.separator + Constants.LOG_DIRECTORY);
 		File logFile  = new File(LogDir + Constants.SLASH + Constants.HUB_LOG);
 		FileOutputStream fos = new FileOutputStream(logFile, false);
 		Utility.executeStreamconsumer(command, fos);
 	}
 	
 	@Test
 	public void testExecuteconsumerSecond() throws PhrescoPomException, FileNotFoundException {
 		String baseDir = "\""+Utility.getProjectHome()+"wp1-wordpress3.4.2/pom.xml\"";
 		String command = "mvn -f "+baseDir+" clean install -DskipTests";
 		File LogDir = new File(Utility.getProjectHome()+File.separator+"wp1-wordpress3.4.2" + File.separator + Constants.DO_NOT_CHECKIN_DIRY + File.separator + Constants.LOG_DIRECTORY);
 		File logFile  = new File(LogDir + Constants.SLASH + "output.log");
 		FileOutputStream fos = new FileOutputStream(logFile, false);
 		Utility.executeStreamconsumer(command, fos);
 	}
 	
	//@Test
 	public void testExecuteconsumerThirdError() throws PhrescoPomException, FileNotFoundException {
 		File pomPath = new File(Utility.getProjectHome()+File.separator+"wp1-wordpress3.4.2"+File.separator+"pom.xml");
 		String baseDir = Utility.getProjectHome()+"wp1-wordpress3.4.2";
 		PomProcessor processor = new PomProcessor(pomPath);
 		String functionalTestDir = processor.getProperty(Constants.POM_PROP_KEY_FUNCTEST_DIR);
 		String command = "java -jar "+baseDir+functionalTestDir+"/lib/selenium-server-standalone-2.30.0.jar -role hub -hubConfig "+baseDir+functionalTestDir+"/hubconfig.json";
 		File LogDir = new File(Utility.getProjectHome()+File.separator+"wp1-wordpress3.4.2" + File.separator + Constants.DO_NOT_CHECKIN_DIRY + File.separator + Constants.LOG_DIRECTORY);
 		File logFile  = new File(LogDir + Constants.SLASH + "output.log");
 		FileOutputStream fos = new FileOutputStream(logFile, false);
 		Utility.executeStreamconsumer(baseDir, command, fos);
 	}
 	
 	@Test
 	public void testExecuteconsumerThird() throws PhrescoPomException, FileNotFoundException {
 		String baseDir = Utility.getProjectHome()+"wp1-wordpress3.4.2";
 		String pomDir = "\""+Utility.getProjectHome()+"wp1-wordpress3.4.2/pom.xml\"";
 		String command = "mvn -f "+pomDir+" clean install -DskipTests";
 		File LogDir = new File(Utility.getProjectHome()+File.separator+"wp1-wordpress3.4.2" + File.separator + Constants.DO_NOT_CHECKIN_DIRY + File.separator + Constants.LOG_DIRECTORY);
 		File logFile  = new File(LogDir + Constants.SLASH + "output.log");
 		FileOutputStream fos = new FileOutputStream(logFile, false);
 		Utility.executeStreamconsumer(baseDir, command, fos);
 	}
 	
 	@Test
 	public void testWriteProcessid() {
 		String baseDir = Utility.getProjectHome()+File.separator+"wp1-wordpress3.4.2";
 		String processName = ManagementFactory.getRuntimeMXBean().getName();
 		String[] split = processName.split("@");
 		String processId = split[0].toString();
 		//Utility.writeProcessid(baseDir, "eclipse", processId);
 	}
 	
 	@Test
 	public void testGetBuildInfo() throws PhrescoException {
 		String buildInfoFile = Utility.getProjectHome()+"wp1-wordpress3.4.2"+File.separator+"do_not_checkin"+File.separator+"build"+File.separator+"build.info";
 		assertNotNull(Utility.getBuildInfo(6, buildInfoFile));
 	}
 	
 	@Test
 	public void testIsConnectionAlive() {
 		assertNotNull(Utility.isConnectionAlive("http", "localhost", 3306));
 	}
 	
 	@Test
 	public void testConvertToCommaDelimted() {
 		String[] list = {"test1","test2","test3"};
 		assertNotNull(Utility.convertToCommaDelimited(list));
 	}
 	
 	@Test
 	public void testWriteStreamAsFile() throws PhrescoException {
 		InputStream is = IOUtils.toInputStream("testFile");
 		File file = new File("testFile");
 		Utility.writeStreamAsFile(is, file);
 		file.delete();
 	}
 	
 	@Test
 	public void testGetCiJobInfo() throws PhrescoException {
 //		assertNotNull(Utility.getCiJobInfoPath("wp1-wordpress3.4.2", "", "read"));
 	}
 	
 	@Test
 	public void testGetProjectDeliveries() throws PhrescoException {
 //		File infoFile = new File(Utility.getCiJobInfoPath("wp1-wordpress3.4.2", "", "read"));
 //		assertNotNull(Utility.getProjectDeliveries(infoFile));
 	}
 	
 	@Test
 	public void testGetJobs() throws PhrescoException {
 		ContinuousDelivery cont = new ContinuousDelivery();
 		cont.setEnvName("Production");
 		cont.setName("wpunit");
 		CIJob job = new CIJob();
 		job.setNoOfUsers("1");
 		job.setAppName("thisApp");
 		job.setJobName("wpunit");
 		List<CIJob> jobs = new ArrayList<CIJob>();
 		cont.setJobs(jobs);
 		List<ContinuousDelivery> continuousDeliveries = new ArrayList<ContinuousDelivery>();
 		continuousDeliveries.add(cont);
 		ProjectInfo projectInfo = getProjectInfo("wp1-wordpress3.4.2");
 		File infoFile = new File(Utility.getCiJobInfoPath("wp1-wordpress3.4.2", "", "read"));
 //		File infoFile = new File("");
 		List<ProjectDelivery> ciJobInfo = Utility.getProjectDeliveries(infoFile);
 		for(ProjectDelivery del : ciJobInfo) {
 			del.setContinuousDeliveries(continuousDeliveries);
 			del.setId(projectInfo.getId());
 		}
 		assertNotNull(Utility.getJobs("wpunit", projectInfo.getId(), ciJobInfo));
 	}
 	
 	@Test
 	public void testSendEmail() throws PhrescoException {
 		Utility.sendTemplateEmail("rohan.lukose@photoninfotech.net", "phresco.do.not.reply@gmail.com", "Test", "Test", "phresco.do.not.reply@gmail.com", "phresco123", null);
 	}
 	
 	@Test(expected=PhrescoException.class)
 	public void testSendEmail1() throws PhrescoException {
 		Utility.sendTemplateEmail("abcxyz.com", "phresco.do.not.reply@gmail.com", "Test", "Test", "phresco.do.not.reply@gmail.com", "phresco123", null);
 	}
 	
 	
 	@Test
 	public void testKillProcess() throws PhrescoException {
 		String baseDir = Utility.getProjectHome()+File.separator+"wp1-wordpress3.4.2";
 //		Utility.killProcess(baseDir, "eclipse");
 	}
 	
 	private static ApplicationInfo getApplicationInfo(String appDirName) throws PhrescoException {
 		try {
 			ProjectInfo projectInfo = getProjectInfo(appDirName);
 			ApplicationInfo applicationInfo = projectInfo.getAppInfos().get(0);
 			return applicationInfo;
 		} catch (JsonIOException e) {
 			throw new PhrescoException(e);
 		}
 	}
 	
 	private static ProjectInfo getProjectInfo(String appDirName) throws PhrescoException {
 		StringBuilder builder  = new StringBuilder();
 		builder.append(Utility.getProjectHome())
 		.append(appDirName)
 		.append(File.separatorChar)
 		.append(".phresco")
 		.append(File.separatorChar)
 		.append("project.info");
 		try {
 			BufferedReader bufferedReader = new BufferedReader(new FileReader(builder.toString()));
 			Gson gson = new Gson();
 			ProjectInfo projectInfo = gson.fromJson(bufferedReader, ProjectInfo.class);
 			return projectInfo;
 		} catch (JsonSyntaxException e) {
 			throw new PhrescoException(e);
 		} catch (JsonIOException e) {
 			throw new PhrescoException(e);
 		} catch (FileNotFoundException e) {
 			throw new PhrescoException(e);
 		}
 	}
 	
 	
 }
