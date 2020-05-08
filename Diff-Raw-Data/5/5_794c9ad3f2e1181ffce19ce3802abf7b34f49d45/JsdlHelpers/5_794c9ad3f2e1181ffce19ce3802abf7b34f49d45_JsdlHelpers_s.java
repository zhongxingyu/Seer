 
 
 package org.vpac.grisu.js.model.utils;
 
 import java.io.File;
 import java.io.IOException;
 import java.io.StringWriter;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Map;
 import java.util.TreeMap;
 
 import javax.xml.XMLConstants;
 import javax.xml.transform.OutputKeys;
 import javax.xml.transform.Source;
 import javax.xml.transform.Transformer;
 import javax.xml.transform.TransformerException;
 import javax.xml.transform.TransformerFactory;
 import javax.xml.transform.TransformerFactoryConfigurationError;
 import javax.xml.transform.dom.DOMSource;
 import javax.xml.transform.stream.StreamResult;
 import javax.xml.transform.stream.StreamSource;
 import javax.xml.validation.Schema;
 import javax.xml.validation.SchemaFactory;
 import javax.xml.validation.Validator;
 import javax.xml.xpath.XPath;
 import javax.xml.xpath.XPathConstants;
 import javax.xml.xpath.XPathExpressionException;
 import javax.xml.xpath.XPathFactory;
 
 import org.apache.log4j.Logger;
 import org.vpac.grisu.control.JobConstants;
 import org.vpac.grisu.control.exceptions.JsdlException;
 import org.w3c.dom.Attr;
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 import org.w3c.dom.Node;
 import org.w3c.dom.NodeList;
 import org.xml.sax.SAXException;
 
 /**
  * This is a pretty important helper class as it has got all the helper methods
  * to access/alter a jsdl document.
  * 
  * @author Markus Binsteiner
  * 
  */
 /**
  * @author markus
  *
  */
 /**
  * @author markus
  *
  */
 public class JsdlHelpers {
 
 	static final Logger myLogger = Logger
 			.getLogger(JsdlHelpers.class.getName());
 
 	public static final String USER_EXECUTION_HOST_FILESYSTEM = "userExecutionHostFs";
 	public static final String LOCAL_EXECUTION_HOST_FILESYSTEM = "localExecutionHostFs";
 
 	// TODO check whether access to this has to be synchronized
 	private final static XPath xpath = getXPath();
 
 	private final static XPath getXPath() {
 		XPath xpath = XPathFactory.newInstance().newXPath();
 		xpath.setNamespaceContext(new JSDLNamespaceContext());
 		return xpath;
 	}
 
 	/**
 	 * Checks whether the jsdl jobDescription against jsdl.xsd to see whether
 	 * it's valid xml
 	 * 
 	 * @param jobDescription
 	 *            the jsdl xml document
 	 * @return true if valid - false if not
 	 */
 	public static boolean validateJSDL(Document jobDescription) {
 
 		// TODO use static Schema for better performance
 		// create a SchemaFactory capable of understanding WXS schemas
 		SchemaFactory factory = SchemaFactory
 				.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
 
 		// load a WXS schema, represented by a Schema instance
 		Source schemaFile = new StreamSource(new File("jsdl.xsd"));
 		Schema schema = null;
 		try {
 			schema = factory.newSchema(schemaFile);
 		} catch (SAXException e1) {
 			// this should not happen
 			e1.printStackTrace();
 		}
 
 		// create a Validator instance, which can be used to validate an
 		// instance document
 		Validator validator = schema.newValidator();
 
 		// validate the DOM tree
 		try {
 			validator.validate(new DOMSource(jobDescription));
 		} catch (SAXException e) {
 			// instance document is invalid!
 			return false;
 		} catch (IOException e) {
 			// this should not happen
 			e.printStackTrace();
 		}
 		return true;
 
 	}
 
 	/**
 	 * Parses the jsdl document and returns the value of the JobName element
 	 * 
 	 * @param jsdl
 	 *            the jsdl document
 	 * @return the name of the job
 	 */
 	public static String getJobname(Document jsdl) {
 
 		String expression = "/jsdl:JobDefinition/jsdl:JobDescription/jsdl:JobIdentification/jsdl:JobName";
 		NodeList resultNodes = null;
 		try {
 			resultNodes = (NodeList) xpath.evaluate(expression, jsdl,
 					XPathConstants.NODESET);
 		} catch (XPathExpressionException e) {
 			myLogger.warn("No jobname in jsdl file.");
 			return null;
 		}
 
 		if (resultNodes.getLength() != 1) {
 			return null;
 		}
 
 		return resultNodes.item(0).getTextContent();
 	}
 
 	/**
 	 * Parses the jsdl document and returns the value of the Description element
 	 * 
 	 * @param jsdl
 	 *            the jsdl document
 	 * @return the description of the type of job
 	 */
 	public static String getDescription(Document jsdl) {
 
 		String expression = "/jsdl:JobDefinition/jsdl:JobDescription/jsdl:Application/jsdl:Description";
 		NodeList resultNodes = null;
 		try {
 			resultNodes = (NodeList) xpath.evaluate(expression, jsdl,
 					XPathConstants.NODESET);
 		} catch (XPathExpressionException e) {
 			myLogger.warn("No description in jsdl file.");
 			return null;
 		}
 
 		if (resultNodes.getLength() != 1) {
 			return null;
 		}
 
 		return resultNodes.item(0).getTextContent();
 	}
 
 	/**
 	 * Returns the content of the walltime element in the jsdl file.
 	 * The walltime specified in the jsdl document should be "walltime in seconds * no cpus". This is not what this method gives back.
 	 *
 	 * This is not what this method gives back.
 	 * The walltime this method gives back is the total walltime in seconds. 
 	 * 
 	 * @param jsdl
 	 *            the jsdl document
 	 * @return the walltime in seconds or -1 if there is no (or no valid) entry
 	 */
 	public static int getWalltime(Document jsdl) {
 
 		int cpus = getProcessorCount(jsdl);
 
 		String expression = "/jsdl:JobDefinition/jsdl:JobDescription/jsdl:Application/jsdl:TotalCPUTime";
 		NodeList resultNodes = null;
 		try {
 			resultNodes = (NodeList) xpath.evaluate(expression, jsdl,
 					XPathConstants.NODESET);
 		} catch (XPathExpressionException e) {
 			myLogger.warn("No jobname in jsdl file.");
 			return -1;
 		}
 
 		if (resultNodes.getLength() != 1) {
 			return -1;
 		}
 
 		int walltimeInSecs;
 		try {
 			walltimeInSecs = new Integer(resultNodes.item(0).getTextContent());
 		} catch (NumberFormatException e) {
 			myLogger.error("No valid number entry in the walltime element.");
 			return -1;
 		}
 
 		if (cpus > 1)
 			return walltimeInSecs / cpus;
 		else
 			return walltimeInSecs;
 	}
 
 	/**
 	 * Parses the jsdl document and returns the value of the TotalCPUCount
 	 * element
 	 * 
 	 * @param jsdl
 	 *            the jsdl document
 	 * @return the number of cpus used in this job
 	 */
 	public static int getProcessorCount(Document jsdl) {
 		
 		String expression = "/jsdl:JobDefinition/jsdl:JobDescription/jsdl:Application/jsdl:TotalCPUCount/jsdl:exact";
 		NodeList resultNodes = null;
 		try {
 			resultNodes = (NodeList) xpath.evaluate(expression, jsdl,
 					XPathConstants.NODESET);
 		} catch (XPathExpressionException e) {
 			myLogger.warn("No jobname in jsdl file.");
 			return -1;
 		}
 
 		if (resultNodes.getLength() != 1) {
 			myLogger.warn("This template doesn't specify a (correct) TotalCPUCount element. Please have a look at your template and replace a possible <TotalCPUCount>2</TotalCPUCount> elemnt with something like: <TotalCPUCount><exact>2</exact></TotalCPUCount> Trying old, incorrect implementation...");
 			// this is just for backwards compatibility because I got the TotalCPUCount element wrong before...
 			return getProcessorCount_OLD(jsdl);
 		}
 
 		int processorCount;
 		try {
 			processorCount = new Integer(resultNodes.item(0).getTextContent());
 		} catch (NumberFormatException e) {
 			myLogger.error("No valid number entry in the walltime element.");
 			return -1;
 		}
 
 		
 		return processorCount;
 
 	}
 	
 	// don't use that anymore -- this will be deleted soon.
 	private static int getProcessorCount_OLD(Document jsdl) {
 		String expression = "/jsdl:JobDefinition/jsdl:JobDescription/jsdl:Application/jsdl:TotalCPUCount";
 		NodeList resultNodes = null;
 		try {
 			resultNodes = (NodeList) xpath.evaluate(expression, jsdl,
 					XPathConstants.NODESET);
 		} catch (XPathExpressionException e) {
 			myLogger.warn("No jobname in jsdl file.");
 			return -1;
 		}
 
 		if (resultNodes.getLength() != 1) {
 			return -1;
 		} else {
 			myLogger.error("Template uses incorrect specification of TotalCPUCount element. Please replace <TotalCPUCount>2</TotalCPUCount> elemnt with something like: <TotalCPUCount><exact>2</exact></TotalCPUCount>.");
 		}
 
 		int processorCount;
 		try {
 			processorCount = new Integer(resultNodes.item(0).getTextContent());
 		} catch (NumberFormatException e) {
 			myLogger.error("No valid number entry in the walltime element.");
 			return -1;
 		}
 
 		return processorCount;
 	}
 	
 	public static long getTotalMemoryRequirement(Document jsdl) {
 		
 		String expression = "/jsdl:JobDefinition/jsdl:JobDescription/jsdl:Application/jsdl:TotalPhysicalMemory/jsdl:LowerBoundedRange";
 		NodeList resultNodes = null;
 		try {
 			resultNodes = (NodeList) xpath.evaluate(expression, jsdl,
 					XPathConstants.NODESET);
 		} catch (XPathExpressionException e) {
 			myLogger.warn("No jobname in jsdl file.");
 			return -1;
 		}
 
 		if (resultNodes.getLength() != 1) {
 			return -1;
 		}
 		
 		Long minMem;
 		
 		try {
 			minMem = new Long(resultNodes.item(0).getTextContent());
 		} catch (NumberFormatException e) {
 			myLogger.error("No valid entry in the MinTotalMemory element.", e);
 			return -1;
 		} 
 
 		return minMem;
 		
 	}
 
 	private static Node getApplicationNode(Document jsdl) {
 		String expression = "/jsdl:JobDefinition/jsdl:JobDescription/jsdl:Application/jsdl:ApplicationName";
 		NodeList resultNodes = null;
 		try {
 			resultNodes = (NodeList) xpath.evaluate(expression, jsdl,
 					XPathConstants.NODESET);
 		} catch (XPathExpressionException e) {
 			myLogger.warn("No application in jsdl file.");
 			return null;
 		}
 
 		if (resultNodes.getLength() != 1) {
 			return null;
 		}
 		return resultNodes.item(0);
 	}
 	
 	/**
 	 * Parses the jsdl document and returns the value of the ApplicationName
 	 * element. Be aware that this is not the name or path of the executable.
 	 * 
 	 * @param jsdl
 	 *            the jsdl document
 	 * @return the name of the applications
 	 */
 	public static String getApplicationName(Document jsdl) {
 
 		return getApplicationNode(jsdl).getTextContent();
 
 	}
 	
 	public static void setApplicationType(Document xmlTemplateDoc,
 			String application) {
 		
 		getApplicationNode(xmlTemplateDoc).setTextContent(application);
 		
 	}
 	
 	private static Node getApplicationVersionNode(Document jsdl) {
 		
 		String expression = "/jsdl:JobDefinition/jsdl:JobDescription/jsdl:Application/jsdl:ApplicationVersion";
 		NodeList resultNodes = null;
 		try {
 			resultNodes = (NodeList) xpath.evaluate(expression, jsdl,
 					XPathConstants.NODESET);
 		} catch (XPathExpressionException e) {
 			myLogger.warn("No application in jsdl file.");
 			return null;
 		}
 
 		if (resultNodes.getLength() != 1) {
 			return null;
 		}
 		return resultNodes.item(0);
 		
 	}
 	
 
 	/**
 	 * @param jsdl
 	 * @return
 	 */
 	public static String getApplicationVersion(Document jsdl) {
 		
 		Node appNode = getApplicationVersionNode(jsdl);
 		
 		if ( appNode != null ) {
 			return appNode.getTextContent();
 		}
 		
 		return null;
 	}
 	
 	/**
 	 * Parses the jsdl document and returns the value of the ApplicationVersion
 	 * element
 	 * 
 	 * @param jsdl
 	 *            the jsdl document
 	 * @return the version of the application to run
 	 */
 	public static void setApplicationVersion(Document jsdl, String version) {
 		getApplicationVersionNode(jsdl).setTextContent(version);
 	}
 
 
 //	/**
 //	 * Parses the jsdl document and returns the value of the ApplicationVersion
 //	 * element
 //	 * 
 //	 * @param jsdl
 //	 *            the jsdl document
 //	 * @return the version of the application to run
 //	 */
 //	public static String getApplicationVersion(Document jsdl) {
 //
 //		String expression = "/jsdl:JobDefinition/jsdl:JobDescription/jsdl:Application/jsdl:ApplicationVersion";
 //		NodeList resultNodes = null;
 //		try {
 //			resultNodes = (NodeList) xpath.evaluate(expression, jsdl,
 //					XPathConstants.NODESET);
 //		} catch (XPathExpressionException e) {
 //			myLogger.warn("No application in jsdl file.");
 //			return null;
 //		}
 //
 //		if (resultNodes.getLength() != 1) {
 //			return null;
 //		}
 //
 //		return resultNodes.item(0).getTextContent();
 //
 //	}
 
 	/**
 	 * Parses the jsdl document and returns the value of the
 	 * jsdl-posix:Executable element. This is the name (and path) of the
 	 * executable.
 	 * 
 	 * @param jsdl
 	 *            the jsdl document
 	 * @return the (path to the) application
 	 */
 	public static String getPosixApplication(Document jsdl) {
 
 		String expression = "/jsdl:JobDefinition/jsdl:JobDescription/jsdl:Application/jsdl-posix:POSIXApplication/jsdl-posix:Executable";
 		NodeList resultNodes = null;
 		try {
 			resultNodes = (NodeList) xpath.evaluate(expression, jsdl,
 					XPathConstants.NODESET);
 		} catch (XPathExpressionException e) {
 			myLogger.warn("No application in jsdl file.");
 			return null;
 		}
 
 		if (resultNodes.getLength() != 1) {
 			return null;
 		}
 
 		return resultNodes.item(0).getTextContent();
 
 	}
 
 	/**
 	 * Parses the jsdl document and returns the value of the
 	 * jsdl-posix:WorkingDirectory element. That information is of not much use unless you
 	 * know as well to which filesyste it's relative.
 	 * 
 	 * @param jsdl
 	 *            the jsdl document
 	 * @return the working directory (relative to $GLOBUS_HOME in our case)
 	 */
 	public static String getWorkingDirectory(Document jsdl) {
 
 		return getWorkingDirectoryElement(jsdl).getTextContent();
 
 	}
 	
 	public static String getAbsoluteWorkingDirectoryUrl(Document jsdl) {
 		
 		Element wd = getWorkingDirectoryElement(jsdl);
 		String fsName = wd.getAttribute("filesystemName");
 		String fsUrl = getFileSystemRootUrl(jsdl, fsName);
 		
 		if ( fsUrl.endsWith("/") || wd.getTextContent().startsWith("/") ) {
 			return fsUrl+wd.getTextContent();
 		} else {
 			return fsUrl+"/"+wd.getTextContent();
 		}
 		
 	}
 
 	public static Element getWorkingDirectoryElement(Document jsdl) {
 		String expression = "/jsdl:JobDefinition/jsdl:JobDescription/jsdl:Application/jsdl-posix:POSIXApplication/jsdl-posix:WorkingDirectory";
 		NodeList resultNodes = null;
 		try {
 			resultNodes = (NodeList) xpath.evaluate(expression, jsdl,
 					XPathConstants.NODESET);
 		} catch (XPathExpressionException e) {
 			myLogger.warn("No application in jsdl file.");
 			return null;
 		}
 
 		if (resultNodes.getLength() != 1) {
 			return null;
 		}
 
 		return (Element) resultNodes.item(0);
 	}
 
 	/**
 	 * Parses the jsdl document and returns the value of the jsdl-posix:Output
 	 * element.
 	 * 
 	 * @param jsdl
 	 *            the jsdl document
 	 * @return the path to the stdout file (relative to the working directory)
 	 */
 	public static String getPosixStandardOutput(Document jsdl) {
 
 		String expression = "/jsdl:JobDefinition/jsdl:JobDescription/jsdl:Application/jsdl-posix:POSIXApplication/jsdl-posix:Output";
 		NodeList resultNodes = null;
 		try {
 			resultNodes = (NodeList) xpath.evaluate(expression, jsdl,
 					XPathConstants.NODESET);
 		} catch (XPathExpressionException e) {
 			myLogger.warn("No output in jsdl file.");
 			return null;
 		}
 
 		if (resultNodes.getLength() != 1) {
 			return null;
 		}
 
 		return resultNodes.item(0).getTextContent();
 
 	}
 	
 	/**
 	 * Parses the jsdl document and returns the value of the jsdl-posix:Output
 	 * element.
 	 * 
 	 * @param jsdl
 	 *            the jsdl document
 	 * @return the path to the stdout file (relative to the working directory)
 	 */
 	public static String getPosixStandardInput(Document jsdl) {
 
 		String expression = "/jsdl:JobDefinition/jsdl:JobDescription/jsdl:Application/jsdl-posix:POSIXApplication/jsdl-posix:Input";
 		NodeList resultNodes = null;
 		try {
 			resultNodes = (NodeList) xpath.evaluate(expression, jsdl,
 					XPathConstants.NODESET);
 		} catch (XPathExpressionException e) {
 			myLogger.warn("No input in jsdl file.");
 			return null;
 		}
 
 		if (resultNodes.getLength() != 1) {
 			return null;
 		}
 
 		return resultNodes.item(0).getTextContent();
 
 	}
 
 	/**
 	 * Parses the jsdl document and returns the value of the jsdl-posix:Error
 	 * element.
 	 * 
 	 * @param jsdl
 	 *            the jsdl document
 	 * @return the path to the stderr file (relative to the working directory)
 	 */
 	public static String getPosixStandardError(Document jsdl) {
 
 		String expression = "/jsdl:JobDefinition/jsdl:JobDescription/jsdl:Application/jsdl-posix:POSIXApplication/jsdl-posix:Error";
 		NodeList resultNodes = null;
 		try {
 			resultNodes = (NodeList) xpath.evaluate(expression, jsdl,
 					XPathConstants.NODESET);
 		} catch (XPathExpressionException e) {
 			myLogger.warn("No output in jsdl file.");
 			return null;
 		}
 
 		if (resultNodes.getLength() != 1) {
 			return null;
 		}
 
 		return resultNodes.item(0).getTextContent();
 
 	}
 
 	// public static String getError(Document jsdl) {
 	//		
 	// String expression =
 	// "/jsdl:JobDefinition/jsdl:JobDescription/jsdl:Application/jsdl-posix:POSIXApplication/jsdl-posix:Error";
 	// NodeList resultNodes = null;
 	// try {
 	// resultNodes = (NodeList)xpath.evaluate(expression, jsdl,
 	// XPathConstants.NODESET);
 	// } catch (XPathExpressionException e) {
 	// myLogger.warn("No error in jsdl file.");
 	// return null;
 	// }
 	//
 	// if ( resultNodes.getLength() != 1 ) {
 	// return null;
 	// }
 	//
 	//		
 	// return resultNodes.item(0).getTextContent();
 	//		
 	// }
 
 	/**
 	 * Parses the jsdl document and returns an array of all the arguments that
 	 * are used on the jsdl-posix:Executable.
 	 * 
 	 * @param jsdl
 	 *            the jsdl document
 	 * @return a list of all arguments
 	 */
 	public static String[] getPosixApplicationArguments(Document jsdl) {
 
 		String expression = "/jsdl:JobDefinition/jsdl:JobDescription/jsdl:Application/jsdl-posix:POSIXApplication/jsdl-posix:Argument";
 		NodeList resultNodes = null;
 		try {
 			resultNodes = (NodeList) xpath.evaluate(expression, jsdl,
 					XPathConstants.NODESET);
 		} catch (XPathExpressionException e) {
 			myLogger.warn("No arguments in jsdl file.");
 			return null;
 		}
 
 		String[] arguments = new String[resultNodes.getLength()];
 		
 		for (int i = 0; i < resultNodes.getLength(); i++) {
 			arguments[i] = resultNodes.item(i).getTextContent();
 		}
 		return arguments;
 	}
 	
 	/**
 	 * Converts the jsdl xml document into string format
 	 * 
 	 * @param jsdl
 	 *            the jsdl document as a {@link Document}
 	 * @return the jsdl document as String
 	 * @throws TransformerFactoryConfigurationError
 	 *             if the {@link TransformerFactory} can't be created
 	 * @throws TransformerException
 	 *             if the {@link Transformer} throws an error while transforming
 	 *             the xml document
 	 */
 	public static String getJsdl(Document jsdl)
 			throws TransformerFactoryConfigurationError, TransformerException {
 
 		// TODO use static transformer to reduce overhead?
 		Transformer transformer = TransformerFactory.newInstance()
 				.newTransformer();
 		transformer.setOutputProperty(OutputKeys.INDENT, "yes");
 
 		// initialize StreamResult with File object to save to file
 		StreamResult result = new StreamResult(new StringWriter());
 		DOMSource source = new DOMSource(jsdl);
 		transformer.transform(source, result);
 
 		String jsdl_string = result.getWriter().toString();
 
 		return jsdl_string;
 	}
 
 	/**
 	 * Sets the name of the job (by changing the value of the jsdl:JobName
 	 * element). If there is already a name specified it will be overwritten.
 	 * 
 	 * @param jsdl
 	 *            the jsdl document
 	 * @param new_jobname
 	 *            the new name of the job.
 	 * @throws XPathExpressionException
 	 *             if the JobName element could not be found
 	 */
 	public static void setJobname(Document jsdl, String new_jobname)
 			throws XPathExpressionException {
 		String expression = "/jsdl:JobDefinition/jsdl:JobDescription/jsdl:JobIdentification/jsdl:JobName";
 		NodeList resultNodes = null;
 		try {
 			resultNodes = (NodeList) xpath.evaluate(expression, jsdl,
 					XPathConstants.NODESET);
 		} catch (XPathExpressionException e) {
 			myLogger.warn("No JobName node in jsdl file.");
 			// that's ok if we want to set the jobname
 		}
 
 		Node jobName = null;
 
 		// TODO what to do if that happens? Shouldn't though because the jsdl is
 		// validated against jsdl.xsd beforehand
 		if (resultNodes.getLength() > 1) {
 			return;
 		} else if (resultNodes.getLength() == 0) {
 			expression = "/jsdl:JobDefinition/jsdl:JobDescription/jsdl:JobIdentification";
 			try {
 				resultNodes = (NodeList) xpath.evaluate(expression, jsdl,
 						XPathConstants.NODESET);
 			} catch (XPathExpressionException e) {
 				myLogger.warn("No JobIdentification node in jsdl file.");
 				throw e;
 			}
 
 			if (resultNodes.getLength() != 1) {
 				throw new XPathExpressionException(
 						"No or more than one JobIdentification nodes in jsdl document.");
 			}
 
 			jobName = jsdl.createElement("jsdl:JobName");
 			jobName.setTextContent(new_jobname);
 			resultNodes.item(0).appendChild(jobName);
 		} else {
 			// replace the text content of the already existing JobName element
 			resultNodes.item(0).setTextContent(new_jobname);
 		}
 
 	}
 
 	/**
 	 * Parses the jsdl document and gets a list of all HostName elements (of
 	 * which we currently only use the one at index 0). This is not really used
 	 * in the proper way because Grisu allows the queue to be included in the
 	 * value as well (sque@brecca:ng2.vpac.org).
 	 * 
 	 * @param jsdl
 	 *            the jsdl docuemnt the jsdl Document
 	 * @return a list of hostnames
 	 */
 	public static String[] getCandidateHosts(Document jsdl) {
 
 		String hosts[] = null;
 
 //		try {
 //			String jsdl_string = SeveralXMLHelpers.toString(jsdl);
 //			System.out.println(jsdl_string);
 //		} catch (TransformerFactoryConfigurationError e1) {
 //			// TODO Auto-generated catch block
 //			e1.printStackTrace();
 //		} catch (TransformerException e1) {
 //			// TODO Auto-generated catch block
 //			e1.printStackTrace();
 //		}
 
 		String expression = "/jsdl:JobDefinition/jsdl:JobDescription/jsdl:Resources/jsdl:CandidateHosts/jsdl:HostName";
 		NodeList resultNodes = null;
 		try {
 			resultNodes = (NodeList) xpath.evaluate(expression, jsdl,
 					XPathConstants.NODESET);
 		} catch (XPathExpressionException e) {
 			myLogger.warn("No HostNames in jsdl file.");
 			return null;
 		}
 
 		int number_hosts = resultNodes.getLength();
 		if (number_hosts == 0)
 			return null;
 		hosts = new String[number_hosts];
 		for (int i = 0; i < number_hosts; i++) {
 			String host_line = resultNodes.item(i).getTextContent();
 			hosts[i] = host_line;
 		}
 
 		return hosts;
 	}
 
 	/**
 	 * Extracts the target filesystem url from a given StageIn element.
 	 * 
 	 * @param stageIn
 	 *            a DataStaging element from a jsdl file
 	 * @return the url of the file to stage in
 	 */
 	public static String extractTargetFromStageInElement(Element stageIn) {
 		String fileSystemName = ((Element) stageIn.getElementsByTagName(
 				"FileSystemName").item(0)).getTextContent();
 		String fileSystemUrl = getFileSystemRootUrl(stageIn.getOwnerDocument(),
 				fileSystemName);
 		String target = fileSystemUrl
 				+ File.separator
 				+ ((Element) stageIn.getElementsByTagName("FileName").item(0))
 						.getTextContent();
 
 		return target;
 	}
 
 	/**
 	 * Sets the name of the source file that gets staged into the job directory
 	 * 
 	 * @param stageIn
 	 *            the DataStaging element
 	 * @param source_url
 	 *            the url of the source file
 	 */
 	public static void setSourceForStageInElement(Element stageIn,
 			String source_url) {
 
 		Element source_uri = ((Element) ((Element) stageIn
 				.getElementsByTagName("Source").item(0)).getElementsByTagName(
 				"URI").item(0));
 		source_uri.setTextContent(source_url);
 
 	}
 
 	public static List<Element> getStageInElements(Document jsdl) {
 
 		String expression = "/jsdl:JobDefinition/jsdl:JobDescription/jsdl:DataStaging";
 		NodeList resultNodes = null;
 		try {
 			resultNodes = (NodeList) xpath.evaluate(expression, jsdl,
 					XPathConstants.NODESET);
 		} catch (XPathExpressionException e) {
 			myLogger.warn("No StageIn elements in jsdl file.");
 			return null;
 		}
 
 		List<Element> stageIns = new ArrayList<Element>();
 		// for every file staging element
 		for (int i = 0; i < resultNodes.getLength(); i++) {
 			stageIns.add((Element) resultNodes.item(i));
 
 		}
 		return stageIns;
 	}
 	
 	public static String getStageInSource(Element stageInElement) {
 		
 		NodeList sources = stageInElement.getElementsByTagName("Source");
 		if ( sources.getLength() != 1 ) {
 			// not implemented/possible?
 			myLogger.error("More than one/no source element in stageIn element.");
 			throw new JsdlException("More than one/no source element in stageIn element.");
 		}
 		
 		Element source = (Element)sources.item(0);
 		sources = source.getElementsByTagName("URI");
 		if ( sources.getLength() != 1 ) {
 			// not implemented/possible?
 			myLogger.error("More than one/no URI element in sources element.");
 			throw new JsdlException("More than one/no URI element in source element.");
 		}
 		Element uri = (Element)sources.item(0);
 		
 		return uri.getTextContent();
 	}
 	
 	public static Element getStageInTarget_relativePart(Element stageInElement) {
 		
 		NodeList filenames = stageInElement.getElementsByTagName("FileName");
 		if ( filenames.getLength() != 1 ) {
 			// not implemented/possible?
 			myLogger.error("More than one/no FileName element in stageIn element.");
 			throw new JsdlException("More than one/no FileName element in stageIn element.");
 		}
 		
 		Element filename = (Element)filenames.item(0);
 		return filename;
 	}
 	
 	public static Element getStageInTarget_filesystemPart(Element stageInElement) {
 		
 		NodeList filesystems = stageInElement.getElementsByTagName("FileSystemName");
 		
 		if ( filesystems.getLength() != 1 ) {
 			// not implemented/possible?
 			myLogger.error("More than one/no FileSystemName element in target element.");
 			throw new JsdlException("More than one/no FileSystemName element in target element.");
 		}
 		
 		Element filesystem = (Element)filesystems.item(0);
 		return filesystem;
 	}
 	
 	public static String getStageInTarget(Element stageInElement) {
 		
 		String fsNamePart = getStageInTarget_filesystemPart(stageInElement).getTextContent();
 		String relPart = getStageInTarget_relativePart(stageInElement).getTextContent();
 		
 		String fsRoot = getFileSystemRootUrl(stageInElement.getOwnerDocument(), fsNamePart);
 		
 		if ( fsRoot.endsWith("/") ) {
 			return fsRoot+relPart;
 		} else {
 			return fsRoot+"/"+relPart;
 		}
 
 	}
 
 	/**
 	 * Extracts all data staging elements and puts them in a map that has got
 	 * the source as key and the target as value.
 	 * 
 	 * @param jsdl
 	 *            the jsdl template document
 	 * @param fqan
 	 *            the fqan with which the job gets submitted or null if non-vo
 	 *            job
 	 * @return a map with all data stagings for this job
 	 */
 //	public static Map<String, String> getStageIns(Document jsdl) {
 //
 //		String expression = "/jsdl:JobDefinition/jsdl:JobDescription/jsdl:DataStaging/jsdl:Source/jsdl:URI";
 //		NodeList resultNodes = null;
 //		try {
 //			resultNodes = (NodeList) xpath.evaluate(expression, jsdl,
 //					XPathConstants.NODESET);
 //		} catch (XPathExpressionException e) {
 //			myLogger.warn("No StageIn elements in jsdl file.");
 //			return null;
 //		}
 //		String executionHostFileSystem = getUserExecutionHostFs(jsdl);
 //
 //		Map<String, String> stageIns = new HashMap<String, String>();
 //		// for every file staging element
 //		for (int i = 0; i < resultNodes.getLength(); i++) {
 //			String source = resultNodes.item(i).getTextContent();
 //			String target = null;
 //
 //			NodeList dataStagingNode = resultNodes.item(i).getParentNode()
 //					.getParentNode().getChildNodes();
 //			// if ( dataStagingNode.getLength() != 1 ) {
 //			// myLogger.warn("Did not found unique parent node for source node:
 //			// "+source);
 //			// return null;
 //			// }
 //			for (int j = 0; j < dataStagingNode.getLength(); j++) {
 //				if ("FileName".equals(dataStagingNode.item(j).getNodeName())) {
 //					// found target node
 //					target = executionHostFileSystem + File.separator
 //							+ dataStagingNode.item(j).getTextContent();
 //					break;
 //				}
 //			}
 //
 //			// now that we have got source and target:
 //			stageIns.put(source, target);
 //		}
 //		return stageIns;
 //
 //	}
 
 	// public static Map<String, Element> extractStageIns(Document jsdl) {
 	//		
 	// String expression =
 	// "/jsdl:JobDefinition/jsdl:JobDescription/jsdl:DataStaging";
 	// NodeList resultNodes = null;
 	// try {
 	// resultNodes = (NodeList)xpath.evaluate(expression, jsdl,
 	// XPathConstants.NODESET);
 	// } catch (XPathExpressionException e) {
 	// myLogger.warn("No StageIn elements in jsdl file.");
 	// return null;
 	// }
 	// Map<String, Element> stageIns = new HashMap<String, Element>();
 	//		
 	// for ( int i=0; i<resultNodes.getLength(); i++ ){
 	// Element stageIn = (Element)resultNodes.item(i);
 	// Element source_uri =
 	// ((Element)((Element)stageIn.getElementsByTagName("Source").item(0)).getElementsByTagName("URI").item(0));
 	//			
 	// stageIns.put(source_uri.getTextContent(), stageIn);
 	// }
 	//		
 	//		
 	// return stageIns;
 	// }
 
 	/**
 	 * This one does not really follow the jsdl syntax. But we use something
 	 * called "modules" within the APACGrid so here it is. I suppose I should
 	 * write my own extension to the jsdl standard, somthing like jsdl-apac.
 	 * Problem is, I don't know how...
 	 * 
 	 * Anyway. This parses a jsdl template document and extracts all
 	 * jsdl-posix:Module elements.
 	 * 
 	 * @param jsdl
 	 *            the jsdl document
 	 * @return all modules that have to be loaded for running this job
 	 */
 	public static String[] getModules(Document jsdl) {
 
 		String expression = "/jsdl:JobDefinition/jsdl:JobDescription/jsdl:Application/jsdl-posix:POSIXApplication/jsdl-posix:Module";
 		NodeList resultNodes = null;
 		try {
 			resultNodes = (NodeList) xpath.evaluate(expression, jsdl,
 					XPathConstants.NODESET);
 		} catch (XPathExpressionException e) {
 			myLogger.warn("No application in jsdl file.");
 			return null;
 		}
 
 		if (resultNodes.getLength() < 1) {
 			return null;
 		}
 
 		String[] modules = new String[resultNodes.getLength()];
 		for (int i = 0; i < resultNodes.getLength(); i++) {
 			modules[i] = resultNodes.item(i).getTextContent();
 		}
 
 		return modules;
 
 	}
 	
 	private static Element getEmailElement(Document jsdl) {
 		String expression = "/jsdl:JobDefinition/jsdl:JobDescription/jsdl-arcs:GrisuTemplate/jsdl-arcs:Email";
 		NodeList resultNodes = null;
 		try {
 			resultNodes = (NodeList) xpath.evaluate(expression, jsdl,
 					XPathConstants.NODESET);
 		} catch (XPathExpressionException e) {
 			myLogger.warn("No application in jsdl file.");
 			return null;
 		}
 
 		if (resultNodes.getLength() == 0 ) {
 			// try old version
 			myLogger.debug("Couldn't find email element. Trying old email element...");
 			return getEmailElement_OLD(jsdl);
 		}
 		if (resultNodes.getLength() != 1) {
 			return null;
 		}
 		return (Element)resultNodes.item(0);
 	}
 	
 	// don't use that anymore, this is wrong!
 	private static Element getEmailElement_OLD(Document jsdl) {
 		String expression = "/jsdl:JobDefinition/jsdl:JobDescription/jsdl:Application/jsdl-posix:POSIXApplication/jsdl-posix:Email";
 		NodeList resultNodes = null;
 		try {
 			resultNodes = (NodeList) xpath.evaluate(expression, jsdl,
 					XPathConstants.NODESET);
 		} catch (XPathExpressionException e) {
 			myLogger.warn("No application in jsdl file.");
 			return null;
 		}
 
 		if (resultNodes.getLength() != 1) {
 			return null;
 		} else {
 			myLogger.error("Found email, but it is in the wrong spot. Please change your template so the \"Email\" element is now under: \"/JobDescription/jsdl-arcs:GrisuTemplate/\"");
 		}
 		return (Element)resultNodes.item(0);
 	}
 	
 	public static boolean sendEmailOnJobStart(Document jsdl) {
 		
 		Element emailElement = getEmailElement(jsdl);
 		
 		String value = emailElement.getAttribute(JobConstants.SEND_EMAIL_ON_JOB_START_ATTRIBUTE_KEY);
 		
 		if ( "true".equals(value) ) {
 			return true;
 		} else {
 			return false;
 		}
 	}
 	
 	public static boolean sendEmailOnJobFinish(Document jsdl) {
 		
 		Element emailElement = getEmailElement(jsdl);
 		
 		String value = emailElement.getAttribute(JobConstants.SEND_EMAIL_ON_JOB_END_ATTRIBUTE_KEY);
 		
 		if ( "true".equals(value) ) {
 			return true;
 		} else {
 			return false;
 		}
 	}
 	
 	public static String getEmail(Document jsdl) {
 
 		Element emailElement = getEmailElement(jsdl);
 		if ( emailElement == null ) {
 			return null;
 		}
 
 		String email = emailElement.getTextContent();
 		return email;
 
 	}
 
 	/**
 	 * Parses a jsdl document and returns the jsdl-posix:JobType element
 	 * within.private
 	 * 
 	 * @param jsdl
 	 *            the jsdl document
 	 * @return the jobType for this job
 	 */
 	public static String getJobType(Document jsdl) {
 
		String expression = "/jsdl:JobDefinition/jsdl:JobDescription/jsdl:Application/jsdl-posix:POSIXApplication/jsdl-posix:JobType";
 		NodeList resultNodes = null;
 		try {
 			resultNodes = (NodeList) xpath.evaluate(expression, jsdl,
 					XPathConstants.NODESET);
 		} catch (XPathExpressionException e) {
			myLogger.warn("No jobtype in jsdl file.");
 			return null;
 		}
 
 		if (resultNodes.getLength() != 1) {
 			return null;
 		}
 
 		return resultNodes.item(0).getTextContent();
 
 	}
 
 	/**
 	 * Parses the jsdl document and returns the url of filesystem on which the
 	 * files are staged in using the EXECUTION_HOST_FILESYSTEM variable to
 	 * determine what the name is for that ("executionHostFs").
 	 * 
 	 * @param jsdl
 	 *            the jsdl document
 	 * @return the url of filesystem where all the files are staged in
 	 */
 	public static String getUserExecutionHostFs(Document jsdl) {
 
 		return getFileSystemRootUrl(jsdl, USER_EXECUTION_HOST_FILESYSTEM);
 
 	}
 
 	public static Element getUserExecutionHostFSElement(Document jsdl) {
 		return (Element) getMountSourceElement(jsdl,
 				USER_EXECUTION_HOST_FILESYSTEM).getParentNode();
 	}
 
 	/**
 	 * Parses a jsdl document and returns the urn of the filesystem that has
 	 * been requested
 	 * 
 	 * @param jsdl
 	 *            the jsdl document
 	 * @param fileSystemName
 	 *            the name of the filesystem
 	 * @return the url of the filesystem
 	 */
 	public static String getFileSystemRootUrl(Document jsdl,
 			String fileSystemName) {
 
 		myLogger.debug("Getting root url for filesystem: "+fileSystemName);
 		Element ms = getMountSourceElement(jsdl, fileSystemName);
 		return ms.getTextContent();
 	}
 
 	public static List<Element> getElementsWithFileSystemNameAttribute(
 			Document jsdl, String fileSystemName) {
 		String expression = "//@filesystemName";
 		NodeList resultNodes = null;
 		try {
 			resultNodes = (NodeList) xpath.evaluate(expression, jsdl,
 					XPathConstants.NODESET);
 		} catch (XPathExpressionException e) {
 			myLogger.warn("No application in jsdl file.");
 			return null;
 		}
 
 		ArrayList<Element> result = new ArrayList<Element>();
 		for (int i = 0; i < resultNodes.getLength(); i++) {
 
 			Element el = ((Attr) resultNodes.item(i)).getOwnerElement();
 			if ( fileSystemName.equals(el.getAttribute("filesystemName")) ) {
 				result.add(el);
 			}
 		}
 
 		return result;
 	}
 
 	public static Element getMountSourceElement(Document jsdl,
 			String fileSystemName) {
 		
 		
 		String expression = "/jsdl:JobDefinition/jsdl:JobDescription/jsdl:Resources/jsdl:FileSystem[@name='"
 				+ fileSystemName + "']/jsdl:MountSource";
 		NodeList resultNodes = null;
 		try {
 			resultNodes = (NodeList) xpath.evaluate(expression, jsdl,
 					XPathConstants.NODESET);
 		} catch (XPathExpressionException e) {
 			myLogger
 					.warn("No mountsource element with that name in jsdl file.");
 			return null;
 		}
 //		String expression = "/jsdl:JobDefinition/jsdl:JobDescription/jsdl:Resources/jsdl:FileSystem";
 //		NodeList resultNodes = null;
 //		try {
 //			resultNodes = (NodeList) xpath.evaluate(expression, jsdl,
 //					XPathConstants.NODESET);
 //		} catch (XPathExpressionException e) {
 //			myLogger
 //					.warn("No mountsource element with that name in jsdl file.");
 //			return null;
 //		}
 //		
 //		ArrayList<Element> fs = new ArrayList<Element>();
 //		for ( int i=0; i<resultNodes.getLength(); i++ ) {
 //			fs.add((Element)resultNodes.item(i));
 //		}
 //		
 //
 //		for ( Element el : fs ) {
 //			if ( fileSystemName.equals(el.getAttribute("name")) ) {
 //				NodeList ms = el.getElementsByTagName("MountSource");
 //				if ( ms.getLength() != 1 ) {
 //					myLogger
 //					.error("More than one or no matching filesystems found. That is not possible.");
 //					throw new JsdlException("More than one or no matching filesystems found. That is not possible.");
 //				}
 //				return (Element)ms;
 //			}
 //		}
 
 //		myLogger.debug("Jsdl:");
 //		myLogger.debug(SeveralXMLHelpers.toStringWithoutAnnoyingExceptions(jsdl));
 		
 
 		if ( resultNodes.getLength() == 0 ) {
 			myLogger.error("No matching file system found in jsdl:");
 			return null;
 		}
 		if (resultNodes.getLength() != 1) {
 			myLogger
 					.error("More than one or no matching filesystems found. That is not possible.");
 			for ( int i = 0; i<resultNodes.getLength(); i++ ) {
 				myLogger.error(resultNodes.item(i).getNodeName()+": "+resultNodes.item(i).getTextContent());
 			}
 			return null;
 		}
 		Element result = (Element) resultNodes.item(0);
 
 		return result;
 	}
 
 	/**
 	 * Adds a FileSystem element to the specified jsdl document. If a filesystem
 	 * with that name already exists, the new root url is that and it is
 	 * returned.
 	 * 
 	 * @param jsdl
 	 *            the jsdl document
 	 * @param fileSystemName
 	 *            the name of the (possibly new) filesystem
 	 * @return the filesystem element
 	 */
 	public static Element addOrRetrieveExistingFileSystemElement(Document jsdl,
 			String fileSystemName, String fileSystemRoot) {
 		String expression = "/jsdl:JobDefinition/jsdl:JobDescription/jsdl:Resources/jsdl:FileSystem[@name='"
 				+ fileSystemName + "']";
 		NodeList resultNodes = null;
 		try {
 			resultNodes = (NodeList) xpath.evaluate(expression, jsdl,
 					XPathConstants.NODESET);
 		} catch (XPathExpressionException e) {
 			myLogger.warn("No application in jsdl file. Good.");
 		}
 
 		if (resultNodes != null && resultNodes.getLength() == 1) {
 			myLogger
 					.info("There's already a filesystem with that name. Returning that one.");
 			Element fs = (Element) resultNodes.item(0);
 			NodeList childs = fs.getElementsByTagName("MountSource");
 			if ( childs.getLength() != 1 ) {
 				myLogger.error("Filesystem element has got more or less than one MountSource elements. That shouldn't be possible.");
 				return null;
 			}
 			((Element)childs.item(0)).setTextContent(fileSystemRoot);
 			return fs;
 		}
 		if (resultNodes != null && resultNodes.getLength() > 1) {
 			myLogger
 					.error("More than one filesystems found. That is not possible.");
 			for ( int i = 0; i<resultNodes.getLength(); i++ ) {
 				myLogger.error(resultNodes.item(i).getNodeName()+": "+resultNodes.item(i).getTextContent());
 			}
 			return null;
 		}
 		
 //		Element old = getUserExecutionHostFSElement(jsdl);
 //		System.out.println(old.getNamespaceURI());
 
 		String nsURL = new JSDLNamespaceContext().getNamespaceURI("jsdl");
 		// creating new one
 		Element filesystem = jsdl.createElementNS(nsURL, "FileSystem");
 //		Element filesystem = jsdl.createDElement("jsdl:FileSystem");
 		filesystem.setAttribute("name", fileSystemName);
 		Element mountSource = jsdl.createElementNS(nsURL, "MountSource");
 		Element filesystemtype = jsdl.createElementNS(nsURL, "FileSystemType");
 		filesystemtype.setTextContent("normal");
 		mountSource.setTextContent(fileSystemRoot);
 
 		filesystem.appendChild(mountSource);
 		filesystem.appendChild(filesystemtype);
 
 		// getting resources element
 		Element resources = getResourcesElement(jsdl);
 		resources.appendChild(filesystem);
 		return filesystem;
 	}
 
 	public static Element getResourcesElement(Document jsdl) {
 
 		String expression = "/jsdl:JobDefinition/jsdl:JobDescription/jsdl:Resources";
 		NodeList resultNodes = null;
 		try {
 			resultNodes = (NodeList) xpath.evaluate(expression, jsdl,
 					XPathConstants.NODESET);
 		} catch (XPathExpressionException e) {
 			myLogger.warn("No application in jsdl file. Good.");
 		}
 
 		if (resultNodes == null || resultNodes.getLength() == 0
 				|| resultNodes.getLength() > 1) {
 			myLogger
 					.info("No or more than one Resource elements found. That's not possible");
 			return null;
 		}
 
 		Element resources = (Element) resultNodes.item(0);
 		return resources;
 	}
 	
 	public static Element getTemplateTagInfoElement(Document jsdl, String templateTagName) {
 		
 		String expression = "/jsdl:JobDefinition/jsdl:JobDescription/jsdl-arcs:GrisuTemplate/jsdl-arcs:Info/jsdl-arcs:TemplateTag[@name='"
 				+ templateTagName + "']";
 		NodeList resultNodes = null;
 		try {
 			resultNodes = (NodeList) xpath.evaluate(expression, jsdl,
 					XPathConstants.NODESET);
 		} catch (XPathExpressionException e) {
 			myLogger.warn("No application in jsdl file. Good.");
 		}
 
 		if (resultNodes == null || resultNodes.getLength() == 0
 				|| resultNodes.getLength() > 1) {
 			myLogger
 					.info("No or more than one Resource elements found. That's not possible");
 			return null;
 		}
 
 		Element resources = (Element) resultNodes.item(0);
 		return resources;
 		
 	}
 	
 	/**
 	 * Returns a map of all infoitems for this template or null, if no infoItem exists
 	 * @param jsdl the jsdl
 	 * @return the map or null
 	 */
 	public static Map<String, String> getTemplateTagInfoItems(Document jsdl, String templateTagName) {
 		
 		Element info = getTemplateTagInfoElement(jsdl, templateTagName);
 		if ( info == null ) {
 			return null;
 		}
 		
 		NodeList infoItems = info.getElementsByTagName("InfoItem");
 		
 		int l = infoItems.getLength();
 		if ( infoItems == null || infoItems.getLength() == 0 ) {
 			return null;
 		}
 		
 		Map<String, String> result = new TreeMap<String, String>();
 		for ( int i=0; i<infoItems.getLength(); i++ ) {
 			String key = ((Element)(infoItems.item(i))).getAttribute("id");
 			if ( key != null && !"".equals(key) ) {
 				String value = ((Element)(infoItems.item(i))).getTextContent();
 				result.put(key, value);
 			}
 		}
 		if ( result.size() == 0 ) {
 			return null;
 		}
 		
 		return result;
 		
 	}
 
 }
