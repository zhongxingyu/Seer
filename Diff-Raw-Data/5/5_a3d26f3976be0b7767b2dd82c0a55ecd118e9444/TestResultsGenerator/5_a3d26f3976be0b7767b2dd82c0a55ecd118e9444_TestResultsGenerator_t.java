 package org.eclipse.releng.generators;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.util.Arrays;
 import java.util.StringTokenizer;
 import java.util.Vector;
 import java.util.Enumeration;
 
 import org.apache.tools.ant.Task;
 import org.apache.xerces.parsers.DOMParser;
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 import org.w3c.dom.NamedNodeMap;
 import org.w3c.dom.Node;
 import org.w3c.dom.NodeList;
 import org.xml.sax.SAXException;
 import org.eclipse.releng.Mailer;
 
 /**
  * @version 	1.0
  * @author Dean Roberts
  */
 public class TestResultsGenerator extends Task {
 
 	static final String elementName = "testsuite";
 	static final String testResultsToken = "%testresults%";
 	static final String compileLogsToken = "%compilelogs%";
 	private Vector dropTokens;
 	private String testResultsWithProblems = "\n";
 
 	private DOMParser parser = new DOMParser();
 	private ErrorTracker anErrorTracker;
 	private String testResultsTemplateString = "";
 	private String dropTemplateString = "";
 	private boolean testsRan = true;
 	//assume tests ran.  If no html files are found, this is set to false
 
 	//for backward compatability with old testManifest.xml
 	private boolean useNewFormat = false;
 
 	private Mailer mailer;
 
 	// Parameters
 	// build runs JUnit automated tests
 	private boolean isBuildTested;
 
 	// buildType used to determine if mail should be sent on
 	// successful build completion
 	private String buildType;
 
 	// Comma separated list of drop tokens
 	private String dropTokenList;
 
 	// Location of the xml files
 	private String xmlDirectoryName;
 
 	// Location of the html files
 	private String htmlDirectoryName;
 
 	// Location of the resulting index.php file.
 	private String dropDirectoryName;
 
 	// Location and name of the template index.php file.
 	private String testResultsTemplateFileName;
 
 	// Location and name of the template drop index.php file.
 	private String dropTemplateFileName;
 
 	// Name of the generated index php file.
 	private String testResultsHtmlFileName;
 
 	// Name of the generated drop index php file;
 	private String dropHtmlFileName;
 
 	// Arbitrary path used in the index.php page to href the
 	// generated .html files.
 	private String hrefTestResultsTargetPath;
 
 	// Aritrary path used in the index.php page to reference the compileLogs
 	private String hrefCompileLogsTargetPath;
 
 	// Location of compile logs base directory
 	private String compileLogsDirectoryName;
 
 	// Location and name of test manifest file
 	private String testManifestFileName;
 
 	public static void main(String[] args) {
 		TestResultsGenerator test = new TestResultsGenerator();
 		test.setUseNewFormat(true);
 		test.setDropTokenList(
 			"%sdk%,%tests%,%examples%,%runtime%,%jdt%,%teamextras%,%infocenter%");
 		test.getDropTokensFromList(test.dropTokenList);
 		test.setIsBuildTested(false);
 		test.setXmlDirectoryName("D:\\junk\\testresults\\xml");
 		test.setHtmlDirectoryName("D:\\junk\\testresults");
 		test.setDropDirectoryName("D:\\junk");
 		test.setTestResultsTemplateFileName(
 			"D:\\junk\\templateFiles\\testResults.php.template");
 		test.setDropTemplateFileName(
 			"D:\\junk\\templateFiles\\index.php.template");
 		test.setTestResultsHtmlFileName("testResults.php");
 //		test.setDropHtmlFileName("index.php");
 		test.setDropHtmlFileName("index.html");
 
 		test.setHrefTestResultsTargetPath("testresults");
 		test.setCompileLogsDirectoryName(
 			"D:\\junk\\compilelogs");
 		test.setHrefCompileLogsTargetPath("compilelogs");
 		test.setTestManifestFileName("D:\\junk\\testManifest.xml");
 		test.execute();
 	}
 
 	public void execute() {
 
 		anErrorTracker = new ErrorTracker();
 		anErrorTracker.loadFile(testManifestFileName);
 		getDropTokensFromList(dropTokenList);
 		testResultsTemplateString = readFile(testResultsTemplateFileName);
 		dropTemplateString = readFile(dropTemplateFileName);
 		System.out.println("Begin: Generating test results index page");
 		System.out.println("Parsing XML files");
 		parseXml();
 		System.out.println("Parsing compile logs");
 		parseCompileLogs();
 		System.out.println("End: Generating test results indx page");
 		writeTestResultsFile();
 		writeDropIndexFile();
 		mailResults();
 	}
 
 	public void parseCompileLogs() {
 
 		String replaceString = "";
 		replaceString =
 			processCompileLogsDirectory(
 				compileLogsDirectoryName,
 				replaceString);
 		if (replaceString == "") {
 			replaceString = "None";
 		}
 		testResultsTemplateString =
 			replace(testResultsTemplateString, compileLogsToken, replaceString);
 
 	}
 
 	private String processCompileLogsDirectory(
 		String directoryName,
 		String aString) {
 
 		File sourceDirectory = new File(directoryName);
 		String replaceString = aString;
 
 		File[] directories = sourceDirectory.listFiles();
 		Arrays.sort(directories);
 
 		for (int i = 0; i < directories.length; i++) {
 			if (directories[i].isDirectory()) {
 				File[] logFiles = directories[i].listFiles();
 
 				for (int j = 0; j < logFiles.length; j++) {
 					String longName = logFiles[j].getPath();
 					if (logFiles[j].isDirectory()) {
 						replaceString =
 							replaceString
 								+ processCompileLogsDirectory(longName, aString);
 					} else {
 						if (longName.endsWith(".log")) {
 							replaceString =
 								replaceString + readCompileLog(longName);
 						}
 					}
 				}
 			} else
 				replaceString =
 					replaceString
 						+ readCompileLog(directories[i].getAbsolutePath());
 
 		}
 		return replaceString;
 	}
 
 	private String readCompileLog(String log) {
 		String fileContents = readFile(log);
 
 		int errorCount = countCompileErrors(fileContents);
 		int warningCount = countCompileWarnings(fileContents);
 		if (errorCount != 0) {
 
 			//use wildcard in place of version number on directory names
 			String logName =
 				log.substring(getCompileLogsDirectoryName().length() + 1);
 			StringBuffer buffer = new StringBuffer(logName);
 			buffer.replace(
 				logName.indexOf("_") + 1,
 				logName.indexOf(File.separator, logName.indexOf("_") + 1),
 				"*");
 			logName = new String(buffer);
 
 			anErrorTracker.registerError(logName);
 		}
 		return formatCompileErrorRow(log, errorCount, warningCount);
 
 	}
 
 	private String readFile(String fileName) {
 
 		try {
 			FileInputStream aStream = new FileInputStream(fileName);
 			byte[] aByteArray = new byte[aStream.available()];
 			aStream.read(aByteArray);
 			aStream.close();
 			return new String(aByteArray);
 		} catch (FileNotFoundException e) {
 			System.out.println("File not found: " + fileName);
 			return "";
 		} catch (IOException e) {
 			System.out.println("IOException: " + fileName);
 			return "";
 		}
 	}
 
 	private int countCompileErrors(String aString) {
 		return extractNumber(aString, "error");
 	}
 
 	private int countCompileWarnings(String aString) {
 		return extractNumber(aString, "warning");
 	}
 
 	private int extractNumber(String aString, String endToken) {
 		int endIndex = aString.lastIndexOf(endToken);
 		if (endIndex == -1) {
 			return 0;
 		}
 
 		int startIndex = endIndex;
 		while (startIndex >= 0
 			&& aString.charAt(startIndex) != '('
 			&& aString.charAt(startIndex) != ',') {
 			startIndex--;
 		};
 
 		String count = aString.substring(startIndex + 1, endIndex).trim();
 		try {
 			return Integer.parseInt(count);
 		} catch (NumberFormatException e) {
 			return 0;
 		}
 
 	}
 
 	private String verifyAllTestsRan(String directory) {
 		Vector missingTestLogs = new Vector();
 		Enumeration enumeration = (anErrorTracker.getTestLogs()).elements();
 
 		String replaceString="";
 		while (enumeration.hasMoreElements()) {
 			String testLogName = enumeration.nextElement().toString();
 
 			if (new File(directory + File.separator + testLogName)
 				.exists()) 
 				continue;
 
 			anErrorTracker.registerError(testLogName);
 			replaceString = replaceString + formatRow(testLogName, -1, false);
 			testResultsWithProblems=testResultsWithProblems.concat("\n" + testLogName.substring(0,testLogName.length()-4) +" (file missing)");
 
 			
 		}
 		return replaceString;
 	}
 
 	public void parseXml() {
 
 		File sourceDirectory = new File(xmlDirectoryName);
 
 		if (sourceDirectory.exists()) {
 
 			String replaceString = "";
 
 			File[] xmlFileNames = sourceDirectory.listFiles();
 			Arrays.sort(xmlFileNames)	;	
 
 			for (int i = 0; i < xmlFileNames.length; i++) {
 				if (xmlFileNames[i].getPath().endsWith(".xml")) {
 					String fullName = xmlFileNames[i].getPath();
 					int errorCount = countErrors(fullName);
 					if (errorCount != 0) {
 						String testName =
 							xmlFileNames[i].getName().substring(
 								0,
 								xmlFileNames[i].getName().length() - 4);
 						testResultsWithProblems =
 							testResultsWithProblems.concat("\n" + testName);
 						anErrorTracker.registerError(
 							fullName.substring(
 								getXmlDirectoryName().length() + 1));
 					}
 
 					replaceString =
 						replaceString
 							+ formatRow(xmlFileNames[i].getPath(), errorCount,true);
 				}
 			}
 			//check for missing test logs
 			replaceString=replaceString+verifyAllTestsRan(xmlDirectoryName);
 			
 			testResultsTemplateString =
 				replace(
 					testResultsTemplateString,
 					testResultsToken,
 					replaceString);
 			testsRan = true;
 
 		} else {
 			testsRan = false;
 			System.out.println(
 				"Test results not found in "
 					+ sourceDirectory.getAbsolutePath());
 		}
 
 	}
 
 	private String replace(
 		String source,
 		String original,
 		String replacement) {
 
 		int replaceIndex = source.indexOf(original);
 		if (replaceIndex > -1) {
 			String resultString = source.substring(0, replaceIndex);
 			resultString = resultString + replacement;
 			resultString =
 				resultString
 					+ source.substring(replaceIndex + original.length());
 			return resultString;
 		} else {
 			System.out.println("Could not find token: " + original);
 			return source;
 		}
 
 	}
 
	protected void writeDropIndexFile() {
 
 		String[] types = anErrorTracker.getTypes();
 		for (int i = 0; i < types.length; i++) {
 			PlatformStatus[] platforms = anErrorTracker.getPlatforms(types[i]);
 			String replaceString = processDropRows(platforms);
 			dropTemplateString =
 				replace(
 					dropTemplateString,
 					dropTokens.get(i).toString(),
 					replaceString);
 		}
 
 		String outputFileName =
 			dropDirectoryName + File.separator + dropHtmlFileName;
 		writeFile(outputFileName, dropTemplateString);
 
 	}
 
	protected String processDropRows(PlatformStatus[] platforms) {
 
 		String result = "";
 		for (int i = 0; i < platforms.length; i++) {
 			result = result + processDropRow(platforms[i]);
 		}
 
 		return result;
 	}
 
 	protected String processDropRow(PlatformStatus aPlatform) {
 
 		String imageName = "";
 
 		if (aPlatform.hasErrors()) {
 			imageName =
 				"<a href=\"testResults.php\"><img src = \"FAIL.gif\" width=19 height=23></a>";
 		} else {
 			if (testsRan) {
 				imageName = "<img src = \"OK.gif\" width=19 height=23>";
 			} else {
 				if (isBuildTested) {
 					imageName =
 						"<font size=\"-1\" color=\"#FF0000\">pending</font>";
 				} else {
 					imageName = "<img src = \"OK.gif\" width=19 height=23>";
 				}
 			}
 		}
 
 		String result = "<tr>";
 
 		result = result + "<td><div align=left>" + imageName + "</div></td>\n";
 		result = result + "<td>" + aPlatform.getName() + "</td>";
 
 		if (isUseNewFormat()){
 			result = result + "<td><div align=\"center\">(<a href=\"download.php?dropFile="+aPlatform.getFileName() +"\">http</a>)\n";
 			result = result + "&nbsp;&nbsp;<?php echo \"(<a href='ftp://$SERVER_NAME/@buildlabel@/"+aPlatform.getFileName() +"'>ftp</a>)</div></td>\" ?>\n";
 		}
 
 		result = result + "<td>" + aPlatform.getFileName() + "</td>\n";
 		result = result + "</tr>\n";
 
 		return result;
 	}
 
 	private void writeTestResultsFile() {
 
 		String outputFileName =
 			dropDirectoryName + File.separator + testResultsHtmlFileName;
 		writeFile(outputFileName, testResultsTemplateString);
 	}
 
 	private void writeFile(String outputFileName, String contents) {
 		try {
 			FileOutputStream outputStream =
 				new FileOutputStream(outputFileName);
 			outputStream.write(contents.getBytes());
 			outputStream.close();
 		} catch (FileNotFoundException e) {
 			System.out.println(
 				"File not found exception writing: " + outputFileName);
 		} catch (IOException e) {
 			System.out.println("IOException writing: " + outputFileName);
 		}
 
 	}
 
 	public void setTestResultsHtmlFileName(String aString) {
 		testResultsHtmlFileName = aString;
 	}
 
 	public String getTestResultsHtmlFileName() {
 		return testResultsHtmlFileName;
 	}
 
 	public void setTestResultsTemplateFileName(String aString) {
 		testResultsTemplateFileName = aString;
 	}
 
 	public String getTestResultsTemplateFileName() {
 		return testResultsTemplateFileName;
 	}
 
 	public void setXmlDirectoryName(String aString) {
 		xmlDirectoryName = aString;
 	}
 
 	public String getXmlDirectoryName() {
 		return xmlDirectoryName;
 	}
 
 	public void setHtmlDirectoryName(String aString) {
 		htmlDirectoryName = aString;
 	}
 
 	public String getHtmlDirectoryName() {
 		return htmlDirectoryName;
 	}
 
 	public void setDropDirectoryName(String aString) {
 		dropDirectoryName = aString;
 	}
 
 	public String getDropDirectoryName() {
 		return dropDirectoryName;
 	}
 
 	private String formatCompileErrorRow(
 		String fileName,
 		int errorCount,
 		int warningCount) {
 
 		String aString = "";
 		if (errorCount == 0 && warningCount == 0) {
 			return aString;
 		}
 
 		int i = fileName.indexOf(getHrefCompileLogsTargetPath());
 
 		String shortName =
 			fileName.substring(i + getHrefCompileLogsTargetPath().length());
 
 		aString = aString + "<tr><td>";
 
 		aString =
 			aString
 				+ "<a href="
 				+ "\""
 				+ getHrefCompileLogsTargetPath()
 				+ shortName
 				+ "\">"
 				+ shortName
 				+ "</a>";
 
 		// aString = aString + fileName;
 		aString = aString + "</td><td>";
 		aString = aString + errorCount;
 		aString = aString + "</td><td>";
 		aString = aString + warningCount;
 		aString = aString + "</td></tr>";
 
 		return aString;
 	}
 
 	private String formatRow(String fileName, int errorCount, boolean link) {
 
 		// replace .xml with .html
 
 		String aString = "";
 		if (!link) {
 			return "<tr><td>" + fileName + " (missing)" + "</td><td>" + "DNF";
 		}
 
 		if (fileName.endsWith(".xml")) {
 
 			int begin = fileName.lastIndexOf(File.separatorChar);
 			int end = fileName.lastIndexOf(".xml");
 
 			String shortName = fileName.substring(begin + 1, end);
 
 			aString = aString + "<tr><td>";
 			aString =
 				aString
 					+ "<a href="
 					+ "\""
 					+ hrefTestResultsTargetPath
 					+ "/"
 					+ shortName
 					+ ".html"
 					+ "\">"
 					+ shortName
 					+ "</a>";
 
 			aString = aString + "</td><td>";
 
 			if (errorCount == -1)
 				aString = aString + "DNF";
 
 			else
 				aString = aString + String.valueOf(errorCount);
 			aString = aString + "</td></tr>";
 		}
 
 		return aString;
 
 	}
 
 	private int countErrors(String fileName) {
 
 		try {
 			parser.parse(fileName);
 
 			Document document = parser.getDocument();
 			NodeList elements = document.getElementsByTagName(elementName);
 
 			int elementCount = elements.getLength();
 			if (elementCount == 0)
 				return -1;
 			int errorCount = 0;
 			for (int i = 0; i < elementCount; i++) {
 				Element element = (Element) elements.item(i);
 				NamedNodeMap attributes = element.getAttributes();
 				Node aNode = attributes.getNamedItem("errors");
 				errorCount =
 					errorCount + Integer.parseInt(aNode.getNodeValue());
 				aNode = attributes.getNamedItem("failures");
 				errorCount =
 					errorCount + Integer.parseInt(aNode.getNodeValue());
 
 			}
 			return errorCount;
 		} catch (IOException e) {
 			System.out.println("IOException: " + fileName);
 			// e.printStackTrace();
 			return 0;
 		} catch (SAXException e) {
 			System.out.println("SAXException: " + fileName);
 			// e.printStackTrace();
 			return 0;
 		}
 	}
 
 	private void mailResults() {
 		//send a different message for the following cases:
 		//build is not tested at all
 		//build is tested, tests have not run
 		//build is tested, tests have run with error and or failures
 		//build is tested, tests have run with no errors or failures
 		try {
 			mailer = new Mailer();
 		} catch (NoClassDefFoundError e) {
 			return;
 		}
 		String buildDownloadUrl = mailer.getBuildProperties().getDownloadUrl()+"/"+mailer.getBuildProperties().getBuildLabel();
 		String subject = "Build is complete.  ";
 		String message = "The build is complete.  \n\n"+buildDownloadUrl;
 
 		if (testsRan) {
 			subject = "Automated JUnit Testing complete.  ";
 			message = "Automated JUnit testing is complete.  ";
 			subject =
 				subject.concat(
 					(testResultsWithProblems.endsWith("\n"))
 						? "All tests pass"
 						: "Test failures/errors occurred.");
 			message =
 				message.concat(
 					(testResultsWithProblems.endsWith("\n"))
 						? "All tests pass"
 						: "Test failures/errors occurred in the following:  "
 							+ testResultsWithProblems)+"\n\n"+buildDownloadUrl+"/"+testResultsHtmlFileName;
 		} else if (isBuildTested && (!buildType.equals("N"))) {
 			subject = subject.concat("Automated JUnit testing is starting.");
 			message = "The " + subject+"\n\n"+buildDownloadUrl;
 		}
 
 		if (subject.endsWith("Test failures/errors occurred."))
 			mailer.sendMessage(subject, message);
 		else if (!buildType.equals("N"))
 			mailer.sendMessage(subject, message);
 
 	}
 
 	/**
 	 * Gets the hrefTestResultsTargetPath.
 	 * @return Returns a String
 	 */
 	public String getHrefTestResultsTargetPath() {
 		return hrefTestResultsTargetPath;
 	}
 
 	/**
 	 * Sets the hrefTestResultsTargetPath.
 	 * @param hrefTestResultsTargetPath The hrefTestResultsTargetPath to set
 	 */
 	public void setHrefTestResultsTargetPath(String htmlTargetPath) {
 		this.hrefTestResultsTargetPath = htmlTargetPath;
 	}
 
 	/**
 	 * Gets the compileLogsDirectoryName.
 	 * @return Returns a String
 	 */
 	public String getCompileLogsDirectoryName() {
 		return compileLogsDirectoryName;
 	}
 
 	/**
 	 * Sets the compileLogsDirectoryName.
 	 * @param compileLogsDirectoryName The compileLogsDirectoryName to set
 	 */
 	public void setCompileLogsDirectoryName(String compileLogsDirectoryName) {
 		this.compileLogsDirectoryName = compileLogsDirectoryName;
 	}
 
 	/**
 	 * Gets the hrefCompileLogsTargetPath.
 	 * @return Returns a String
 	 */
 	public String getHrefCompileLogsTargetPath() {
 		return hrefCompileLogsTargetPath;
 	}
 
 	/**
 	 * Sets the hrefCompileLogsTargetPath.
 	 * @param hrefCompileLogsTargetPath The hrefCompileLogsTargetPath to set
 	 */
 	public void setHrefCompileLogsTargetPath(String hrefCompileLogsTargetPath) {
 		this.hrefCompileLogsTargetPath = hrefCompileLogsTargetPath;
 	}
 
 	/**
 	 * Gets the testManifestFileName.
 	 * @return Returns a String
 	 */
 	public String getTestManifestFileName() {
 		return testManifestFileName;
 	}
 
 	/**
 	 * Sets the testManifestFileName.
 	 * @param testManifestFileName The testManifestFileName to set
 	 */
 	public void setTestManifestFileName(String testManifestFileName) {
 		this.testManifestFileName = testManifestFileName;
 	}
 
 	/**
 	 * Gets the dropHtmlFileName.
 	 * @return Returns a String
 	 */
 	public String getDropHtmlFileName() {
 		return dropHtmlFileName;
 	}
 
 	/**
 	 * Sets the dropHtmlFileName.
 	 * @param dropHtmlFileName The dropHtmlFileName to set
 	 */
 	public void setDropHtmlFileName(String dropHtmlFileName) {
 		this.dropHtmlFileName = dropHtmlFileName;
 	}
 
 	/**
 	 * Gets the dropTemplateFileName.
 	 * @return Returns a String
 	 */
 	public String getDropTemplateFileName() {
 		return dropTemplateFileName;
 	}
 
 	/**
 	 * Sets the dropTemplateFileName.
 	 * @param dropTemplateFileName The dropTemplateFileName to set
 	 */
 	public void setDropTemplateFileName(String dropTemplateFileName) {
 		this.dropTemplateFileName = dropTemplateFileName;
 	}
 
 	protected void getDropTokensFromList(String list) {
 		int i = 0;
 		StringTokenizer tokenizer = new StringTokenizer(list, ",");
 		dropTokens = new Vector();
 
 		while (tokenizer.hasMoreTokens()) {
 			dropTokens.add(tokenizer.nextToken());
 		}
 	}
 
 	public String getDropTokenList() {
 		return dropTokenList;
 	}
 
 	public void setDropTokenList(String dropTokenList) {
 		this.dropTokenList = dropTokenList;
 	}
 
 	public boolean isBuildTested() {
 		return isBuildTested;
 	}
 
 	public void setIsBuildTested(boolean isBuildTested) {
 		this.isBuildTested = isBuildTested;
 	}
 
 	/**
 	 * Returns the buildType.
 	 * @return String
 	 */
 	public String getBuildType() {
 		return buildType;
 	}
 
 	/**
 	 * Sets the buildType.
 	 * @param buildType The buildType to set
 	 */
 	public void setBuildType(String buildType) {
 		this.buildType = buildType;
 	}
 
 	/**
 	 * @return boolean
 	 */
 	public boolean isUseNewFormat() {
 		return useNewFormat;
 }
 	/**
 	 * Sets the useNewFormat.
 	 * @param useNewFormat The useNewFormat to set
 	 */
 	public void setUseNewFormat(boolean useNewFormat) {
 		this.useNewFormat = useNewFormat;
 	}
 
 	/**
 	 * @return
 	 */
 	public boolean testsRan() {
 		return testsRan;
 	}
 
 	/**
 	 * @param b
 	 */
 	public void setTestsRan(boolean b) {
 		testsRan = b;
 	}
 
 	/**
 	 * @return
 	 */
 
 	/**
 	 * @return
 	 */
 	public Vector getDropTokens() {
 		return dropTokens;
 	}
 
 	/**
 	 * @param vector
 	 */
 	public void setDropTokens(Vector vector) {
 		dropTokens = vector;
 	}
 
 }
