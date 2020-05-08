 package com.photon.phresco.impl;
 
 import java.io.*;
 import java.util.*;
 
 import javax.xml.bind.JAXBException;
 
 import org.apache.commons.collections.CollectionUtils;
 import org.apache.commons.io.FileUtils;
 import org.apache.commons.lang.StringUtils;
 import org.jdom.Document;
 import org.jdom.Element;
 import org.jdom.JDOMException;
 import org.jdom.input.SAXBuilder;
 import org.jdom.output.*;
 
 import com.google.gson.Gson;
 import com.photon.phresco.api.ApplicationProcessor;
 import com.photon.phresco.commons.model.ApplicationInfo;
 import com.photon.phresco.commons.model.ArtifactGroup;
 import com.photon.phresco.commons.model.ProjectInfo;
 import com.photon.phresco.configuration.Configuration;
 import com.photon.phresco.exception.PhrescoException;
 import com.photon.phresco.service.pom.POMConstants;
 import com.photon.phresco.util.Constants;
 import com.photon.phresco.util.FileUtil;
 import com.photon.phresco.util.ProjectUtils;
 import com.photon.phresco.util.Utility;
 import com.phresco.pom.exception.PhrescoPomException;
 import com.phresco.pom.model.*;
 import com.phresco.pom.util.PomProcessor;
 
 public class AndroidApplicationProcessor implements ApplicationProcessor {
 
 	private static final String NODE_APP_NAME = "app_name";
 	private static final String PATH_STRINGS_XML = "/values/strings.xml";
 	private static final String HYPHEN_SPLIT = "-";
 	private static final String NAME = "name";
 	private static final String NODE_STRING = "string";
 	private static final String UNIT_TEST_POM_XML = "/test/unit/pom.xml";
 	private static final String PERFORMANCE_TEST_POM_XML = "/test/performance/pom.xml";
 	private static final String FUNCTIONAL_TEST_POM_XML = "/test/functional/pom.xml";
 	private static final String POM = "pom.xml";
 	private String ANDROID_VERSION = "android.version";
 
 	@Override
 	public void preCreate(ApplicationInfo appInfo) throws PhrescoException {
 		// TODO Auto-generated method stub
 
 	}
 
 	@Override
 	public void preUpdate(ApplicationInfo appInfo) throws PhrescoException {
 		// TODO Auto-generated method stub
 
 	}
 
 	@Override
 	public void postCreate(ApplicationInfo appInfo) throws PhrescoException {
 		try {
 			String projectHome = Utility.getProjectHome() + appInfo.getAppDirName();
 			File path = new File(projectHome);
 			updateAndroidVersion(path, appInfo);
 			File projHome = new File(projectHome);
 			updatePOM(projHome);
 			
 			// update String.xml app_name with user defined name
 			updateAppName(projHome, appInfo.getName());
 		} catch (Exception e) {
 			throw new PhrescoException(e);
 		}
 	}
 
 	@Override
 	public void postUpdate(ApplicationInfo appInfo, List<ArtifactGroup> artifactGroups, List<ArtifactGroup> deletedFeatures) throws PhrescoException {
 		//		extractPilots(info, path, technology);
 		File pomFile = new File(Utility.getProjectHome() + appInfo.getAppDirName() + File.separator + Constants.POM_NAME);
 		String projectHome = Utility.getProjectHome() + appInfo.getAppDirName();
 		ProjectUtils projectUtils = new ProjectUtils();
 		if(CollectionUtils.isNotEmpty(artifactGroups)) {
 			projectUtils.updatePOMWithPluginArtifact(pomFile, artifactGroups);
 		}
 		if(CollectionUtils.isNotEmpty(deletedFeatures)) {
 			projectUtils.deleteFeatureDependencies(appInfo, deletedFeatures);
 		}
 		BufferedReader breader = projectUtils.ExtractFeature(appInfo);
 		try {
 		String line = "";
 			while ((line = breader.readLine()) != null) {
 				if (line.startsWith("[ERROR]")) {
 					System.out.println(line);
 				}
 			}
 		} catch (IOException e) {
 			throw new PhrescoException(e);
 		}
 		File projHome = new File(projectHome);
 		updatePOM(projHome);
 		
 		// update String.xml app_name with user defined name
 		updateAppName(projHome, appInfo.getName());
 	}
 
 	public void updateAndroidVersion(File path, ApplicationInfo appInfo) throws PhrescoException {
 		File pomPath = new File(path ,Constants.POM_NAME);
 		if(!pomPath.exists()) {
 			return;
 		}
 		PomProcessor processor;
 		List<File> fileList = new ArrayList<File>();
 		fileList.add(new File(path, Constants.POM_NAME));
 		fileList.add(new File(path, FUNCTIONAL_TEST_POM_XML));
 		fileList.add(new File(path, PERFORMANCE_TEST_POM_XML));
 		fileList.add(new File(path, UNIT_TEST_POM_XML));
 		for (File pomFile : fileList) {
 			if(pomFile.exists()) {
 				try {
 					processor = new PomProcessor(pomFile);
 					String selectedVersion = appInfo.getTechInfo().getVersion();
 					processor.setProperty(ANDROID_VERSION, selectedVersion);
 					processor.save();
 				} catch (Exception e) {
 					throw new PhrescoException(e);
 				}
 			}
 		}
 	}
 
 	@Override
 	public void postConfiguration(ApplicationInfo appInfo, List<Configuration> configurations) throws PhrescoException {
 		// TODO Auto-generated method stub
 
 	}
 
 	@Override
 	public List<Configuration> preFeatureConfiguration(ApplicationInfo appInfo,
 			String featureName) throws PhrescoException {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	@Override
 	public void postFeatureConfiguration(ApplicationInfo appInfo,
 			List<Configuration> configs, String featureName)
 	throws PhrescoException {
 		// TODO Auto-generated method stub
 
 	}
 
 	@Override
 	public void preBuild(ApplicationInfo appInfo) throws PhrescoException {
 		// TODO Auto-generated method stub
 
 	}
 
 	@Override
 	public void postBuild(ApplicationInfo appInfo) throws PhrescoException {
 		// TODO Auto-generated method stub
 
 	}
 	
 	private boolean updateAppName(File appPath, String appName) throws PhrescoException {
 		try {
 			File projectHomePOM = new File(appPath, POM);
 			PomProcessor processor = new PomProcessor(projectHomePOM);
 			List<Resource> resources = processor.getModel().getBuild().getResources().getResource();
 			String directory = "";
 			if (resources != null) {
 				Resource resource = resources.get(0);
 				directory = resource.getDirectory();
 			} 
 			File stringsXml = new File(appPath, directory + PATH_STRINGS_XML);
 			List<String> appNames = Arrays.asList(appName.split(HYPHEN_SPLIT));
 			return updateAppName(stringsXml, NODE_APP_NAME, appNames.get(0));
 		} catch (Exception e) {
 			throw new PhrescoException(e);
 		}
 	}
 	
 	/**
 	 * @param stringsXml file path where the strings.xml present.
 	 * @return 
 	 * @throws PhrescoException
 	 */
 	private boolean updateAppName(File stringsXml, String stringId, String appName) throws PhrescoException {
 		try {
 			if (stringsXml.isFile()) {
 				SAXBuilder builder = new SAXBuilder();
 				Document document = (Document) builder.build(stringsXml);
 				Element rootNode = document.getRootElement();
 				List list = rootNode.getChildren(NODE_STRING);
 				for (int i = 0; i < list.size(); i++) {
 					Element node = (Element) list.get(i);
 					String name = node.getAttributeValue(NAME);
 					if (stringId.equals(name)) { // "app_name"
 						node.setText(appName); //Shopping Cart
 						XMLOutputter xmlOutput = new XMLOutputter();
 						// display nice nice
 						xmlOutput.setFormat(Format.getPrettyFormat());
 						xmlOutput.output(document, new FileWriter(stringsXml));
 						break;
 					}
 				}
 				return true;
 			}
 		} catch (Exception e) {
 			throw new PhrescoException(e);
 		}
 		return false;
 	}
 	
 	/**
 	 * @param path
 	 *            file path where the pom.xml present.
 	 * @return 
 	 * @throws PhrescoException
 	 * @throws PhrescoPomException 
 	 * @throws JAXBException 
 	 */
 	private boolean updatePOM(File path) throws PhrescoException {
 		File projectHome = new File(path, POM);
 		File testFunctionalPom = new File(path, FUNCTIONAL_TEST_POM_XML);
 		File testUnitPom = new File(path, UNIT_TEST_POM_XML);
 		File testPerformancePom = new File(path, PERFORMANCE_TEST_POM_XML);
 		boolean functionalTest = updateTestPom(testFunctionalPom, projectHome);
 		boolean unitTest = updateTestPom(testUnitPom, projectHome);
 		boolean performanceTest = updateTestPom(testPerformancePom, projectHome);
 		if (Boolean.TRUE.equals(functionalTest) || Boolean.TRUE.equals(unitTest) || Boolean.TRUE.equals(performanceTest)  ) {
 			return true;
 		} else {
 			return false;
 		}
 	}
 		
 	/**
 	 * @param testPom
 	 * @param projectPom
 	 * @param projectHome 
 	 * @return 
 	 * @throws PhrescoException 
 	 * @throws JDOMException
 	 * @throws IOException
 	 * @throws JAXBException 
 	 * @throws PhrescoPomException 
 	 */
 	private boolean updateTestPom(File projectPom, File projectHome) throws PhrescoException {
 		SAXBuilder builder = new SAXBuilder();
 		if(!projectPom.exists()) {
 			return false;
 		}
 		try {
 			String oldArtifactId = getOldArtifactId(projectHome.getParent());
 			Document projectDoc = builder.build(projectHome);
 			Element projectRootNode = projectDoc.getRootElement();
 			Element artifact = getNode(projectRootNode, POMConstants.ARTIFACT_ID);
 			Element group = getNode(projectRootNode, POMConstants.GROUP_ID);
 			Element version = getNode(projectRootNode, POMConstants.VERSION);
 			Element name = getNode(projectRootNode, POMConstants.NAME);
 			PomProcessor processor = new PomProcessor(projectPom);
 			processor.setArtifactId(artifact.getText());
 			processor.setName(name.getText());
 			if(StringUtils.isNotEmpty(oldArtifactId)) {
 				processor.deleteDependency(group.getText(), oldArtifactId, POMConstants.JAR);
 				processor.deleteDependency(group.getText(), oldArtifactId, POMConstants.APK);
 			}
 			processor.addDependency(group.getText(), artifact.getText(),  version.getText() , POMConstants.PROVIDED , POMConstants.JAR, "");
 			processor.addDependency(group.getText(), artifact.getText(),  version.getText() , POMConstants.PROVIDED , POMConstants.APK, "");
 			processor.save();
 		
 		} catch (JDOMException e) {
 			throw new PhrescoException(e);
 		} catch (IOException e) {
 			throw new PhrescoException(e);
 		} catch (PhrescoPomException e) {
 			throw new PhrescoException(e);
 		}
 		return true;
 	}
 
 	private String getOldArtifactId(String projectHome) throws PhrescoException {
 		File backupFile = new File(projectHome + File.separator + Constants.DOT_PHRESCO_FOLDER +File.separator + Constants.PROJECT_INFO_BACKUP_FILE);
 		if(!backupFile.exists()) {
 			return null;
 		}
 		BufferedReader reader = null;
 		try {
 			reader = new BufferedReader(new FileReader(backupFile));
 			ProjectInfo projectInfo = new Gson().fromJson(reader, ProjectInfo.class);
 			return projectInfo.getAppInfos().get(0).getCode();
 		} catch (FileNotFoundException e) {
 			throw new PhrescoException(e);
 		} finally {
 			Utility.closeReader(reader);
 		}
 	}
 
 	/**
 	 * @param rootNode
 	 * @return
 	 */
 	@SuppressWarnings("rawtypes")
 	private static Element getNode(Element rootNode, String nodeName) {
 		Element dependencies = rootNode.getChild(nodeName);
 		// sometime, this doesn't work. So as workaround this stint.
 		if (dependencies == null) {
 			List children = rootNode.getChildren();
 			for (Object object : children) {
 				if ((object instanceof Element) && ((Element) object).getName().equals(nodeName)) {
 					dependencies = (Element) object;
 					break;
 				}
 			}
 		}
 		return dependencies;
 	}
 
     @Override
     public List<Configuration> preConfiguration(ApplicationInfo appInfo,
             String featureName, String envName) throws PhrescoException {
         // TODO Auto-generated method stub
         return null;
     }
 }
