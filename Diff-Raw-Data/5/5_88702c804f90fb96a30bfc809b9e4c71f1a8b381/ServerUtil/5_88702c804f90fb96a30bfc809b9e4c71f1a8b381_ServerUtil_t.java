 package com.photon.phresco.service.util;
 
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.StringWriter;
 import java.security.MessageDigest;
 import java.security.NoSuchAlgorithmException;
 import java.util.ArrayList;
 import java.util.Enumeration;
 import java.util.List;
 import java.util.UUID;
 import java.util.jar.JarEntry;
 import java.util.jar.JarFile;
 import java.util.jar.JarInputStream;
 import java.util.zip.ZipEntry;
 
 import javax.xml.bind.JAXBException;
 import javax.xml.parsers.DocumentBuilder;
 import javax.xml.parsers.DocumentBuilderFactory;
 import javax.xml.transform.OutputKeys;
 import javax.xml.transform.Transformer;
 import javax.xml.transform.TransformerFactory;
 import javax.xml.transform.dom.DOMSource;
 import javax.xml.transform.stream.StreamResult;
 
 import org.apache.commons.codec.binary.Base64;
 import org.w3c.dom.Element;
 
 import com.photon.phresco.commons.model.ArtifactGroup;
 import com.photon.phresco.commons.model.ArtifactInfo;
 import com.photon.phresco.exception.PhrescoException;
 import com.photon.phresco.util.Constants;
 import com.photon.phresco.util.FileUtil;
 import com.photon.phresco.util.Utility;
 import com.phresco.pom.util.PomProcessor;
 
 public class ServerUtil {
 
     private static JarInputStream jarInputStream = null;
 
     /**
      * Get the artifact information from jar inputstream
      * 
      * @param inputJarStream
      * @param fileName
      * @return
      * @throws PhrescoException
      */
     public static ArtifactGroup getArtifactinfo(InputStream inputJarStream)
             throws PhrescoException {
         File jarFile = writeFileFromStream(inputJarStream, null, "jar");
         ArtifactGroup artifactInfo = getArtifactInfoFromJar(jarFile); 
         FileUtil.delete(jarFile);
         return artifactInfo;
     }
 
     /**
      * Find archetype info from the given jar file
      * 
      * @return
      * @throws IOException
      * @throws JAXBException
      */
     public static ArtifactGroup getArtifactInfoFromJar(File jarFile)
             throws PhrescoException {
         ArtifactGroup info = null;
         String pomFile = null;
         try {
             JarFile jarfile = new JarFile(jarFile);
             for (Enumeration<JarEntry> em = jarfile.entries(); em
                     .hasMoreElements();) {
                 JarEntry jarEntry = em.nextElement();
                 if (jarEntry.getName().endsWith("pom.xml")) {
                     pomFile = jarEntry.getName();
                 }
             }
             if (pomFile != null) {
                 ZipEntry entry = jarfile.getEntry(pomFile);
                 InputStream inputStream = jarfile.getInputStream(entry);
                 PomProcessor processor = new PomProcessor(inputStream);
                 info = new ArtifactGroup();
                 info.setGroupId(processor.getGroupId());
                 info.setArtifactId(processor.getArtifactId());
                 ArtifactInfo artifactInfo = new ArtifactInfo();
                 artifactInfo.setVersion(processor.getVersion());
                 List<ArtifactInfo> artifactInfos = new ArrayList<ArtifactInfo>();
                 artifactInfos.add(artifactInfo);
                 info.setVersions(artifactInfos);
             }
         } catch (Exception e) {
             throw new PhrescoException(e);
         }
         return info;
     }
 
     /**
      * Validate the given jar is valid maven jar
      * 
      * @return
      * @throws PhrescoException
      */
     public static boolean validateMavenJar(InputStream inputJar)
             throws PhrescoException {
         boolean returnValue = false;
         try {
             jarInputStream = new JarInputStream(inputJar);
             JarEntry nextJarEntry = jarInputStream.getNextJarEntry();
             while ((nextJarEntry = jarInputStream.getNextJarEntry()) != null) {
                 if (nextJarEntry.getName().contains("pom.xml")) {
                     returnValue = true;
                     break;
                 }
             }
         } catch (Exception e) {
             throw new PhrescoException(e);
         }
 
         return returnValue;
     }
 
     /**
      * Validate the given jar is valid maven archetype jar
      * 
      * @return
      * @throws PhrescoException
      */
     public static boolean validateArchetypeJar(InputStream inputJar)
             throws PhrescoException {
         try {
             jarInputStream = new JarInputStream(inputJar);
             JarEntry nextJarEntry = jarInputStream.getNextJarEntry();
             while ((nextJarEntry = jarInputStream.getNextJarEntry()) != null) {
                 if (nextJarEntry.getName().equals(
                         ServerConstants.ARCHETYPE_METADATA_FILE)
                         || nextJarEntry.getName().equals(
                                 ServerConstants.ARCHETYPE_FILE)) {
                     return true;
                 }
             }
         } catch (Exception e) {
             throw new PhrescoException(e);
         }
         return false;
     }
 
     /**
      * Validate the given jar is valid maven plugin jar
      * 
      * @return
      * @throws PhrescoException
      */
     public static boolean validatePluginJar(InputStream inputJar)
             throws PhrescoException {
         try {
             jarInputStream = new JarInputStream(inputJar);
             JarEntry nextJarEntry = jarInputStream.getNextJarEntry();
             while ((nextJarEntry = jarInputStream.getNextJarEntry()) != null) {
                 if (nextJarEntry.getName().equals(
                         ServerConstants.PLUGIN_COMPONENTS_XML_FILE)
                         || nextJarEntry.getName().equals(
                                 ServerConstants.PLUGIN_XML_FILE)) {
                     return true;
                 }
             }
         } catch (Exception e) {
             throw new PhrescoException(e);
         }
         return false;
     }
 
     /**
      * To Get Temp FolderPath
      * 
      * @return
      */
     public static String getTempFolderPath() {
 
         String tempFolderPath = "";
         String systemTempFolder = System.getProperty(Constants.JAVA_TMP_DIR);
         String uuid = UUID.randomUUID().toString();
 
         // handled the file separator since java.io.tmpdir does not return
         // the last file separator in linux and Mac OS
         if ((systemTempFolder.endsWith(File.separator))) {
            tempFolderPath = systemTempFolder + Constants.PHRESCO;
         } else {
             tempFolderPath = systemTempFolder + File.separator
                    + Constants.PHRESCO;
         }
 
         tempFolderPath = tempFolderPath + File.separator + uuid;
         File tempFolder = new File(tempFolderPath);
         tempFolder.mkdirs();
 
         return tempFolderPath;
     }
 
     /**
      * To Encrypt The Given String
      * 
      * @param inputString
      * @return
      */
     public static String encryptString(String inputString) {
         byte[] encodeBase64 = Base64.encodeBase64(inputString.getBytes());
         String encodedString = new String(encodeBase64);
 
         return encodedString;
     }
 
     /**
      * To Decrypt The Given String
      * 
      * @param inputString
      * @return
      */
     public static String decryptString(String inputString) {
         byte[] decodedBytes = Base64.decodeBase64(inputString);
         String decodedString = new String(decodedBytes);
 
         return decodedString;
     }
 
     /**
      * To write the given input stream to file
      * 
      * @param inputStream
      * @param path
      * @return
      * @throws PhrescoException
      */
     public static File writeFileFromStream(InputStream inputStream, String path, String packaging)
             throws PhrescoException {
         File artifactFile = null;
         FileOutputStream fileOutStream = null;
         if (path == null) {
             artifactFile = new File(ServerUtil.getTempFolderPath() + "/"
                     + "temp" + "." + packaging);
         } else {
             artifactFile = new File(path);
         }
         try {
             fileOutStream = new FileOutputStream(artifactFile);
             byte buf[] = new byte[1024];
             int len;
             while ((len = inputStream.read(buf)) > 0) {
                 fileOutStream.write(buf, 0, len);
             }
         } catch (IOException e) {
             throw new PhrescoException(e);
         } finally {
             Utility.closeStream(inputStream);
             Utility.closeStream(fileOutStream);
         }
         return artifactFile;
 
     }
 
     /**
      * To create pom.xml file for artifact upload
      * 
      * @param info
      * @return
      * @throws PhrescoException
      */
     public static File createPomFile(ArtifactGroup info) throws PhrescoException {
         FileWriter writer = null;
         File pomFile = new File(getTempFolderPath(), ServerConstants.POM_FILE_NAME);
         DocumentBuilderFactory domFactory = DocumentBuilderFactory
                 .newInstance();
         try {
             DocumentBuilder domBuilder = domFactory.newDocumentBuilder();
 
             org.w3c.dom.Document newDoc = domBuilder.newDocument();
             Element rootElement = newDoc.createElement(ServerConstants.POM_PROJECT);
             newDoc.appendChild(rootElement);
 
             Element modelVersion = newDoc.createElement(ServerConstants.POM_MODELVERSION);
             modelVersion.setTextContent(ServerConstants.POM_MODELVERSION_VAL);
             rootElement.appendChild(modelVersion);
 
             Element groupId = newDoc.createElement(ServerConstants.POM_GROUPID);
             groupId.setTextContent(info.getGroupId());
             rootElement.appendChild(groupId);
 
             Element artifactId = newDoc.createElement(ServerConstants.POM_ARTIFACTID);
             artifactId.setTextContent(info.getArtifactId());
             rootElement.appendChild(artifactId);
 
             Element version = newDoc.createElement(ServerConstants.POM_VERSION);
             version.setTextContent(info.getVersions().get(0).getVersion());
             rootElement.appendChild(version);
 
             Element packaging = newDoc.createElement(ServerConstants.POM_PACKAGING);
             packaging.setTextContent(info.getPackaging());
             rootElement.appendChild(packaging);
 
             Element description = newDoc.createElement(ServerConstants.POM_DESC);
             description.setTextContent(ServerConstants.POM_DESC_VAL);
             rootElement.appendChild(description);
 
             TransformerFactory transfac = TransformerFactory.newInstance();
             Transformer trans = transfac.newTransformer();
             trans.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, ServerConstants.POM_OMMIT);
             trans.setOutputProperty(OutputKeys.INDENT, ServerConstants.POM_DESC);
 
             StringWriter sw = new StringWriter();
             StreamResult result = new StreamResult(sw);
             DOMSource source = new DOMSource(newDoc);
             trans.transform(source, result);
             String xmlString = sw.toString();
             writer = new FileWriter(pomFile);
             writer.write(xmlString);
             writer.close();
         } catch (Exception e) {
             throw new PhrescoException(e);
         } finally {
             Utility.closeStream(writer);
         }
 
         return pomFile;
     }
     
     
     /**
      * Create Content Url Using given artifactinfo
      * @param groupId
      * @param artifactId
      * @param version
      * @param packaging
      * @return
      */
     public static String createContentURL(String groupId, String artifactId, String version, String packaging) {
     	return groupId.replace(".", "/") + "/" + artifactId + "/" + version + "/" + artifactId + "-" + version + "." + packaging;
     }
     
     /**
      * Returns one way hashed string using SALT
 	 * @param userName
 	 * @param password
 	 * @return
 	 * @throws PhrescoException
 	 */
 	public static String encodeUsingHash(String userName, String password) throws PhrescoException {
 	   	String salt = password + userName;
 	   	StringBuffer stringBuffer = new StringBuffer();
 	   	byte[] bytes = salt.getBytes();
 		MessageDigest msgDigest;
 		try {
 			msgDigest = MessageDigest.getInstance("MD5");
 			msgDigest.reset();
 			msgDigest.update(bytes);
 			byte messageDigests[] = msgDigest.digest();
 			for (int i = 0; i < messageDigests.length; i++) {
 			stringBuffer.append(Integer.toHexString(0xFF & messageDigests[i]));
 			}
 		} catch (NoSuchAlgorithmException e) {
 			throw new PhrescoException(e);
 		}
 		return stringBuffer.toString();
    }
    
 }
