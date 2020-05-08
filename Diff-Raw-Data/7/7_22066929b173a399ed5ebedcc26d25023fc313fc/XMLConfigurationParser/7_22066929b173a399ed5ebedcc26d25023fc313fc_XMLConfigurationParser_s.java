 
 package com.zanoccio.axirassa.overlord;
 
 import java.io.File;
 import java.io.InputStream;
 import java.net.URL;
 
 import javax.xml.parsers.DocumentBuilder;
 import javax.xml.parsers.DocumentBuilderFactory;
 
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 import org.w3c.dom.NamedNodeMap;
 import org.w3c.dom.Node;
 import org.w3c.dom.NodeList;
 
 import com.zanoccio.axirassa.overlord.exceptions.DuplicateGroupException;
 import com.zanoccio.axirassa.overlord.exceptions.DuplicateTargetException;
 import com.zanoccio.axirassa.overlord.exceptions.EmptyExecutionGroupException;
 import com.zanoccio.axirassa.overlord.exceptions.NoExecutionTargetsException;
 import com.zanoccio.axirassa.overlord.exceptions.NoGroupsException;
 import com.zanoccio.axirassa.overlord.exceptions.OverlordException;
 import com.zanoccio.axirassa.overlord.exceptions.OverlordParsingException;
 import com.zanoccio.axirassa.overlord.exceptions.OverlordTargetClassNotFoundException;
 import com.zanoccio.axirassa.overlord.exceptions.UnknownExecutionTargetException;
 import com.zanoccio.javakit.ClassPathEntityResolver;
 
 public class XMLConfigurationParser {
 
 	private final URL configfile;
 	private Document dom;
 	private Element docroot;
 	private final OverlordConfiguration configuration;
 	private final InputStream inputstream;
 
 
 	public XMLConfigurationParser(URL configfile, InputStream stream, OverlordConfiguration configuration) {
 		this.configfile = configfile;
 		this.inputstream = stream;
 		this.configuration = configuration;
 	}
 
 
 	public OverlordConfiguration parse() throws OverlordException {
 		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
 
 		setClassPathAndBaseDirectory();
 
 		try {
 			DocumentBuilder db = dbf.newDocumentBuilder();
 			db.setEntityResolver(new ClassPathEntityResolver());
 			dom = db.parse(inputstream);
 		} catch (Exception e) {
 			throw new OverlordParsingException(dom, e);
 		}
 
 		docroot = dom.getDocumentElement();
 
 		createExecutionTargets();
 		createExecutionGroups();
 
 		return configuration;
 	}
 
 
 	private void setClassPathAndBaseDirectory() {
 		// if the configfile is within a JAR, we use it to set the classpath
 		String jarfile = retrieveJarFile(configfile);
 		if (jarfile != null) {
 			configuration.setClassPath(stripPrefix(jarfile));
 			configuration.setBaseDirectory(stripPrefix(new File(jarfile).getParent()));
 		} else {
 			configuration.setBaseDirectory(new File(configfile.getPath()).getParent());
 		}
 	}
 
 
 	private String stripPrefix(String path) {
		return path.replaceFirst("^file:\\\\", "").replaceFirst("^file:/", "");
 	}
 
 
 	private String retrieveJarFile(URL file) {
 		String[] components = file.getPath().split("!", 2);
 
 		if (components.length < 2)
 			return null;
 
 		if (components[0].toLowerCase().endsWith(".jar"))
 			return components[0];
 
 		return null;
 	}
 
 
 	public OverlordConfiguration getConfiguration() {
 		return configuration;
 	}
 
 
 	private void createExecutionTargets() throws OverlordException {
 		NodeList targetlist = docroot.getElementsByTagName(XMLName.TARGET.toString());
 		if (targetlist.getLength() < 1)
 			throw new NoExecutionTargetsException(dom);
 
 		for (Node targetnode : new IterableNodeList(targetlist)) {
 			ExecutionTarget target = createExecutionTarget(targetnode);
 
 			// check that this execution target doesn't already exist
 			if (configuration.hasExecutionTarget(target.getCanonicalName()))
 				throw new DuplicateTargetException(target, dom);
 
 			configuration.addExecutionTarget(target);
 		}
 	}
 
 
 	private ExecutionTarget createExecutionTarget(Node node) throws OverlordException {
 		NamedNodeMap attributes = node.getAttributes();
 
 		Node namenode = attributes.getNamedItem(XMLName.NAME.toString());
 		Node classnode = attributes.getNamedItem(XMLName.CLASS.toString());
 
 		String name = namenode.getTextContent();
 		String classname = classnode.getTextContent();
 
 		ExecutionTarget target = null;
 
 		try {
 			target = new ExecutionTarget(name, classname);
 		} catch (ClassNotFoundException e) {
 			throw new OverlordTargetClassNotFoundException(name, node.getOwnerDocument(), e);
 		}
 
 		// apply any options
 		NodeList children = node.getChildNodes();
 		for (Node child : new IterableNodeList(children))
 			if (child.getNodeName().equals(XMLName.JVMOPTIONS.toString()))
 				target.setOptions(TargetJVMOptions.populate(target.getJVMOptions(), child));
 
 		return target;
 	}
 
 
 	private void createExecutionGroups() throws OverlordException {
 		NodeList grouplist = docroot.getElementsByTagName(XMLName.GROUP.toString());
 		if (grouplist.getLength() < 1)
 			throw new NoGroupsException(dom);
 
 		for (Node groupnode : new IterableNodeList(grouplist)) {
 			ExecutionGroup group = createExecutionGroup(groupnode);
 
 			if (configuration.hasExecutionGroup(group.getCanonicalName()))
 				throw new DuplicateGroupException(group, dom);
 
 			configuration.addExecutionGroup(group);
 		}
 	}
 
 
 	private ExecutionGroup createExecutionGroup(Node node) throws OverlordException {
 		// create an empty execution group
 		NamedNodeMap attributes = node.getAttributes();
 
 		Node namenode = attributes.getNamedItem(XMLName.NAME.toString());
 		String name = namenode.getTextContent();
 
 		ExecutionGroup group = new ExecutionGroup(name);
 
 		// fill it with execution targets
 		NodeList childnodes = node.getChildNodes();
 
 		if (childnodes.getLength() < 1)
 			throw new EmptyExecutionGroupException(name, node.getOwnerDocument());
 
 		for (Node childnode : new IterableNodeList(childnodes))
 			if (childnode.getNodeName().equals(XMLName.EXECUTE.toString()))
 				group.addExecutionSpecification(createExecutionSpecification(childnode));
 
 		return group;
 	}
 
 
 	private ExecutionSpecification createExecutionSpecification(Node node) throws OverlordException {
 		NamedNodeMap attributes = node.getAttributes();
 
 		String targetname = attributes.getNamedItem(XMLName.TARGET.toString()).getTextContent();
 		String instances = attributes.getNamedItem(XMLName.INSTANCES.toString()).getTextContent();
 		int instancecount = Integer.parseInt(instances);
 
 		ExecutionTarget target = configuration.getExecutionTarget(targetname);
 
 		if (target == null)
 			throw new UnknownExecutionTargetException(targetname, node.getOwnerDocument());
 
 		ExecutionSpecification spec = new ExecutionSpecification(configuration, target);
 		spec.setInstances(instancecount);
 
 		return spec;
 	}
 }
