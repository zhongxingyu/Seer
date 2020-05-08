 package fr.liglab.adele.cilia.internals;
 
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.io.InputStream;
 import java.net.URL;
 import java.util.ArrayList;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.Properties;
 import java.util.Set;
 
 import org.osgi.framework.BundleContext;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.w3c.dom.NamedNodeMap;
 import org.w3c.dom.Node;
 import org.w3c.dom.NodeList;
 
 import fr.liglab.adele.cilia.Adapter;
 import fr.liglab.adele.cilia.Binding;
 import fr.liglab.adele.cilia.Chain;
 import fr.liglab.adele.cilia.Component;
 import fr.liglab.adele.cilia.Mediator;
 import fr.liglab.adele.cilia.MediatorComponent;
 import fr.liglab.adele.cilia.Port;
 import fr.liglab.adele.cilia.exceptions.CiliaException;
 import fr.liglab.adele.cilia.exceptions.CiliaParserException;
 import fr.liglab.adele.cilia.framework.data.XmlTools;
 import fr.liglab.adele.cilia.model.AdapterImpl;
 import fr.liglab.adele.cilia.model.BindingImpl;
 import fr.liglab.adele.cilia.model.ChainImpl;
 import fr.liglab.adele.cilia.model.MediatorImpl;
 import fr.liglab.adele.cilia.model.PatternType;
 import fr.liglab.adele.cilia.util.ChainParser;
 import fr.liglab.adele.cilia.util.CiliaExtenderParser;
 
 public class CiliaChainInstanceParser implements ChainParser {
 
 	private final static String CHAIN_TYPE = "CiliaChainParser";
 
 	protected String ROOT_FILE = "cilia";
 	protected String ROOT_CHAIN = "chain";
 	protected String CHAIN_MEDIATORS = "mediators";
 	protected String CHAIN_BINDINGS = "bindings";
 	protected String CHAIN_ADAPTERS = "adapters";
 
 	protected String MEDIATOR = "mediator-instance";
 	protected String ADAPTER = "adapter-instance";
 	protected String MEDIATOR_TYPE = "type";
 	protected String MEDIATOR_Id = "id";
 
 	protected String BINDING = "binding";
 	protected String BINDING_from = "from";
 	protected String BINDING_to = "to";
 	protected String PORT_SEPARATOR = ":";
 	protected String BINDING_TYPE = "linker";
 	protected String BINDING_TYPE2 = "protocol";
 
 	protected static Logger log = LoggerFactory.getLogger("cilia.chain.parser");
 
 	private Set customParsers = new HashSet();
 
 	protected volatile static int mediatorNumbers = 0;
 
 	public CiliaChainInstanceParser() {
 	}
 
 	public CiliaChainInstanceParser(BundleContext context) {
 	}
 
 	public String getChainType() {
 		return "dscilia";
 	}
 
 	/**
 	 * 
 	 * @param chain
 	 * @return
 	 */
 	protected Node checkObject(Object chain) {
 		if (!(chain instanceof Node)) {
 			return null;
 		}
 		return (Node) chain;
 	}
 
 	/**
 	 * It will convert a Chain model to a Chain in a Node. TODO: use JAXB
 	 */
 	public Object convertChain(Chain chain) {
 		log.error("public Object convertChain(Chain chain){..} method is not implemented");
 		throw new UnsupportedOperationException(" This method is not implemented");
 	}
 
 	/**
 	 * 
 	 * @param xmlFile
 	 * @return
 	 */
 	public Chain[] obtainChains(URL xmlFile) throws CiliaException, FileNotFoundException {
 		List listChains = new ArrayList();
 		InputStream fis;
 		try {
 			fis = xmlFile.openStream();
 		} catch (IOException e) {
 			e.printStackTrace();
 			throw new CiliaException("Error when trying to open " + xmlFile.getPath()
 					+ " File.");
 		}
 		// First child is the root node.
 		Node node = XmlTools.streamToNode(fis).getFirstChild();
 		String rootName = node.getNodeName();
 		if (rootName.compareTo(ROOT_FILE) != 0) {
 			throw new CiliaException(xmlFile.getPath() + " Root element must be <"
 					+ ROOT_FILE + "> and is <" + rootName + ">");
 		}
 		log.debug("Found cilia tag");
 		Node possibleChain = node.getFirstChild();
 		while (possibleChain != null) {
 			String nodeName = possibleChain.getNodeName();
 			if (nodeName.compareTo(ROOT_CHAIN) == 0) {
 				log.debug("Found chain in file: ");
 				Chain newChain = parseChain(possibleChain);
 				if (newChain == null) {
 					log.warn("Found chain in file but is null: ");
 				} else {
 					listChains.add(newChain);
 				}
 			}
 
 			possibleChain = possibleChain.getNextSibling();
 		}
 		if (listChains.isEmpty()) {
 			return null;
 		}
 		Chain[] newArray = new Chain[listChains.size()];
 		for (int i = 0; i < listChains.size(); i++)
 			newArray[i] = (Chain) listChains.get(i);
 		return newArray;
 	}
 
 	/**
 	 * It will convert a Chain in a Node to a chain model. TODO: Use JAXB.
 	 */
 	public Chain parseChain(Object chain) throws CiliaException {
 		Node nchain = checkObject(chain);
 		Chain newChain = null;
 		Mediator[] mediators = null;
 		Adapter[] adapters = null;
 		Binding[] bindings = null;
 		if (nchain == null) {
 			throw new CiliaException("Object:" + chain
 					+ " is not instance of org.w3c.dom.Node");
 		}
 
 		if (nchain == null || nchain.getNodeName().compareTo(ROOT_CHAIN) != 0) {
 			log.debug(nchain.getNodeName() + "Node is not a chain");
 			return null;
 		}
 		// It must add mediators at first.
 		String id = getId(nchain);
 		String type = getType(nchain);
 		Properties props = getProperties(nchain);
 		newChain = new ChainImpl(id, type, null, props);
 		Node node = nchain.getFirstChild();
 		while (node != null) {
 			if (node.getNodeName().compareToIgnoreCase(CHAIN_MEDIATORS) == 0) {
 				mediators = getMediators(node);
 			}
 			node = node.getNextSibling();
 		}
 		// Add mediators to the new chain.
 		for (int i = 0; mediators != null && i < mediators.length; i++) {
 			newChain.add(mediators[i]);
 		}
 		// It must add adapters then.
 		Node nodeAdapters = nchain.getFirstChild();
 		while (nodeAdapters != null) {
 			if (nodeAdapters.getNodeName().compareToIgnoreCase(CHAIN_ADAPTERS) == 0) {
 				adapters = getAdapters(nodeAdapters);
 			}
 			nodeAdapters = nodeAdapters.getNextSibling();
 		}
 		// Add adapters to the new chain.
 		for (int i = 0; adapters != null && i < adapters.length; i++) {
 			newChain.add(adapters[i]);
 		}
 
 		// Add bindings to the chain.
 		Node nodeBindings = nchain.getFirstChild();
 		while (nodeBindings != null) {
 			if (nodeBindings.getNodeName().compareToIgnoreCase(CHAIN_BINDINGS) == 0) {
 				setBindings(newChain, nodeBindings);
 			}
 			nodeBindings = nodeBindings.getNextSibling();
 		}
 		return newChain;
 	}
 
 	/**
 	 * 
 	 * @param chain
 	 * @param node
 	 */
 	protected void setBindings(Chain chain, Node node) {
 		Node binding = node.getFirstChild();
 		do {
 			if (binding.getNodeName().compareToIgnoreCase(BINDING) == 0) {
 				computeBinding(chain, binding);
 			}
 		} while ((binding = binding.getNextSibling()) != null);
 
 	}
 
 	/**
 	 * 
 	 * @param chain
 	 * @param bindingNode
 	 */
 	protected void computeBinding(Chain chain, Node bindingNode) {
 		Binding bindingModel = null;
 		MediatorComponent sourceMediator;
 		MediatorComponent targetMediator;
 		String sourceMediatorId;
 		String sourceMediatorPort;
 
 		String targetMediatorId;
 		String targetMediatorPort;
 
 		String bindingId = getId(bindingNode);
 		String bindingType = getAttributeValue(bindingNode, BINDING_TYPE); //Use linker as reserved word.
 		if (bindingType==null) {
 			bindingType = getAttributeValue(bindingNode, BINDING_TYPE2); //try with protocol (deprecated)
 			if (bindingType != null) {
 				log.warn(BINDING_TYPE2 + " is deprecated as reserved word. Use " + BINDING_TYPE + " instead");
 			}
 		}
 		Properties bindingProperties = getProperties(bindingNode);
		bindingModel = new BindingImpl(bindingId, bindingType, null, bindingProperties);
 		//
 		String colport = getAttributeValue(bindingNode, BINDING_to);
 		String sendport = getAttributeValue(bindingNode, BINDING_from);
 		if ((colport == null && sendport != null)
 				|| (colport != null && sendport == null)) {
 			computeHalfBinding(chain, bindingNode, bindingModel);
 		} else {
 			// get SourceMediator
 			String[] targs = colport.split(PORT_SEPARATOR);
 			String[] src = sendport.split(PORT_SEPARATOR);
 			if (targs.length != 2) {
 				targetMediatorPort = "std";
 
 			} else {
 				targetMediatorPort = targs[1];
 			}
 			if (src.length != 2) {
 				sourceMediatorPort = "std";
 
 			} else {
 				sourceMediatorPort = src[1];
 			}
 
 			sourceMediatorId = src[0];
 			targetMediatorId = targs[0];
 
 			if (!analizeBindingData(sourceMediatorId, sourceMediatorPort,
 					targetMediatorId, targetMediatorPort))
 				return;
 			sourceMediator = chain.getMediator(sourceMediatorId);
 			if (sourceMediator == null) { // search if is an adapter.
 				sourceMediator = chain.getAdapter(sourceMediatorId);
 			}
 			targetMediator = chain.getMediator(targetMediatorId);
 			if (targetMediator == null) {// search if is an adapter.
 				targetMediator = chain.getAdapter(targetMediatorId);
 			}
 			if (sourceMediator == null) {
 				log.error("Binding not added. Mediator " + sourceMediatorId
 						+ " does not exist in chain" + chain.getId());
 				return;
 			}
 			if (targetMediator == null) {
 				log.error("Binding not added. Mediator " + targetMediatorId
 						+ "does not exist in chain" + chain.getId());
 				return;
 			}
 			chain.bind(sourceMediator.getOutPort(sourceMediatorPort),
 					targetMediator.getInPort(targetMediatorPort), bindingModel);
 		}
 	}
 
 	protected void computeHalfBinding(Chain chain, Node bindingNode, Binding bindingModel) {
 		String mediatorId = null;
 		String mediatorPort = null;
 		Port port = null;
 		String colport = getAttributeValue(bindingNode, BINDING_to);
 		String sendport = getAttributeValue(bindingNode, BINDING_from);
 		Mediator mediator = null;
 		// will add a bind.
 		if (colport == null && sendport != null) {
 			String[] ins = sendport.split(PORT_SEPARATOR);
 			if (ins.length != 2) {
 				mediatorPort = "std";
 			} else {
 				mediatorPort = ins[1];
 			}
 			mediatorId = ins[0];
 			mediator = chain.getMediator(mediatorId);
 			if (mediator == null) {
 				log.error("Binding not added, Mediator " + mediatorId
 						+ "is doesnt exist in chain.");
 				return;
 			}
 			port = mediator.getOutPort(mediatorPort);
 
 		} else if (colport != null && sendport == null) {
 			String[] ins = colport.split(PORT_SEPARATOR);
 			if (ins.length != 2) {
 				mediatorPort = "std";
 			} else {
 				mediatorPort = ins[1];
 			}
 			mediatorId = ins[0];
 			mediator = chain.getMediator(mediatorId);
 			if (mediator == null) {
 				log.error("Binding not added, Mediator " + mediatorId
 						+ "is doesnt exist in chain.");
 				return;
 			}
 			port = mediator.getInPort(mediatorPort);
 		} else {
 			// It must never perform this code.
 			log.error("Mediators in bindings must have ports. \"mediatorId:portName\"");
 			log.error("Binding not added.");
 		}
 		log.warn("Binding added: portName" + port.getName() + " type:"
 				+ bindingModel.getType() + "nature:" + port.getType());
 		log.warn("Half binding is not supported, from and to is need.");
 		chain.bind(port, bindingModel);
 	}
 
 	protected boolean analizeBindingData(String sourceMediatorId,
 			String sourceMediatorPort, String targetMediatorId, String targetMediatorPort) {
 		if (sourceMediatorId == null) {
 			log.error("source Mediator is null in binding, unable to bind");
 			return false;
 		}
 		if (sourceMediatorPort == null) {
 			log.error("source Mediator with outport, unable to bind");
 			return false;
 		}
 		if (targetMediatorId == null) {
 			log.error("target Mediator is null in binding, unable to bind");
 			return false;
 		}
 		if (targetMediatorPort == null) {
 			log.error("target Mediator without port, unable to bind");
 			return false;
 		}
 		return true;
 	}
 
 	/**
 	 * Get all mediators.
 	 * 
 	 * @param node
 	 * @return
 	 */
 	protected Mediator[] getMediators(Node node) {
 		Node mediatorNode = node.getFirstChild();
 		List mediatorsList = new ArrayList();
 		if (mediatorNode == null) {
 			return null;
 		}
 		do {
 			if (mediatorNode.getNodeName().compareTo(MEDIATOR) == 0) {
 				Component mediator = getMediator(mediatorNode);
 				if (mediator == null) {
 					log.error("Unable to obtain Mediator :");
 				} else {
 					mediatorsList.add(mediator);
 					log.debug("Adding Mediator:" + mediator.getId());
 				}
 			}
 			mediatorNode = mediatorNode.getNextSibling();
 		} while (mediatorNode != null);
 		if (mediatorsList.isEmpty()) {
 			return null;
 		}
 		log.debug("Number of mediators found:" + mediatorsList.size());
 		Mediator[] mediators = new Mediator[mediatorsList.size()];
 		for (int i = 0; i < mediatorsList.size(); i++)
 			mediators[i] = (Mediator) mediatorsList.get(i);
 		return mediators;
 	}
 
 	/**
 	 * Get all adapters.
 	 * 
 	 * @param node
 	 * @return
 	 */
 	protected Adapter[] getAdapters(Node node) {
 		Node mediatorNode = node.getFirstChild();
 		List mediatorsList = new ArrayList();
 		if (mediatorNode == null) {
 			return null;
 		}
 		do {
 			if (mediatorNode.getNodeName().compareTo(ADAPTER) == 0) {
 				Component mediator = getAdapter(mediatorNode);
 				if (mediator == null) {
 					log.error("Unable to obtain Adapter :");
 				} else {
 					mediatorsList.add(mediator);
 					log.debug("Adding Adapter:" + mediator.getId());
 				}
 			}
 			mediatorNode = mediatorNode.getNextSibling();
 		} while (mediatorNode != null);
 		if (mediatorsList.isEmpty()) {
 			return null;
 		}
 		log.debug("Number of mediators found:" + mediatorsList.size());
 		Adapter[] mediators = new Adapter[mediatorsList.size()];
 		for (int i = 0; i < mediatorsList.size(); i++)
 			mediators[i] = (Adapter) mediatorsList.get(i);
 		return mediators;
 	}
 
 	/**
 	 * Create a mediator from a given Node.
 	 * 
 	 * @param nmediator
 	 *            Mediator node.
 	 * @return the new mediator.
 	 */
 	protected Mediator getMediator(Node nmediator) {
 		mediatorNumbers++;
 		Mediator mediator = null;
 		String mediatorId = getId(nmediator);
 		String mediatorType = getType(nmediator);
 		;
 		Properties mediatorProperties = getMediatorProperties(nmediator);
 
 		if (mediatorType == null) {
 			log.error("Type not found in Mediator Node");
 			return null;
 		}
 		if (mediatorId == null) {
 			mediatorId = mediatorType + mediatorNumbers;
 		}
 
		mediator = new MediatorImpl(mediatorId, mediatorType, null,null, mediatorProperties, null);
 		mediator = (Mediator)extendersParsers(nmediator, mediator);
 
 		return mediator;
 	}
 
 	/**
 	 * Create an adapter from a given Node.
 	 * 
 	 * @param nadapter
 	 *            adapter node.
 	 * @return the new adapter.
 	 */
 	protected Adapter getAdapter(Node nadapter) {
 		mediatorNumbers++;
 		Adapter adapter = null;
 		PatternType pt = getPattern(nadapter);
 		String adapterId = getId(nadapter);
 		String adapterType = getType(nadapter);
 
 		Properties adapterProperties = getProperties(nadapter);
 		if (adapterType == null) {
 			log.error("Type not found in Mediator Node");
 			return null;
 		}
 		if (adapterId == null) {
 			adapterId = adapterType + mediatorNumbers;
 		}
 
 		// String pattern = getAttributeValue(nmediator, pattern)
		adapter = new AdapterImpl(adapterId, adapterType, null, adapterProperties, null, pt);
 		adapter = (Adapter)extendersParsers(nadapter, adapter);
 		return adapter;
 	}
 
 	private PatternType getPattern(Node nadapter) {
 		PatternType pt = PatternType.UNASSIGNED;
 		String pattern = getAttributeValue(nadapter, "pattern");
 		if ("in-only".equalsIgnoreCase(pattern)) {
 			pt = PatternType.IN_ONLY;
 		} else if ("out-only".equalsIgnoreCase(pattern)) {
 			pt = PatternType.OUT_ONLY;
 		} else if ("in-out".equalsIgnoreCase(pattern)) {
 			pt = PatternType.IN_OUT;
 		}
 		return pt;
 	}
 
 	protected Properties getMediatorProperties(Node mediator) {
 		Properties props = getProperties(mediator);
 		props.putAll(getPropertiesFrom(mediator, "processor"));
 		props.putAll(getPropertiesFrom(mediator, "scheduler"));
 		props.putAll(getPropertiesFrom(mediator, "dispatcher"));
 		props.putAll(getPropertiesFrom(mediator, "monitoring"));
 		log.debug("Creating " + getAttributeValue(mediator, "id")
 				+ " mediator with properties" + props);
 		return props;
 	}
 
 	/**
 	 * Get the properties.
 	 * 
 	 * @param node
 	 *            node wihi contain properties.
 	 * @return the properties found.
 	 */
 	protected Properties getProperties(Node node) {
 		Properties properties = new Properties();
 
 		Node child = node.getFirstChild();
 		while (child != null) {
 			if (child.getNodeName().compareTo("property") == 0) {
 				if (child.hasAttributes()) {
 					String name = null;
 					String value = null;
 					NamedNodeMap nmp = child.getAttributes();
 					// obtain Property name.
 					Node component = nmp.getNamedItem("name");
 					if (component != null && component.getNodeName() != null) {
 						name = component.getNodeValue();
 					}
 					// obtain Property value.
 					component = nmp.getNamedItem("value");
 					if (component != null && component.getNodeName() != null) {
 						value = component.getNodeValue();
 					}
 					
 					if ((name != null) && (value != null)) {
 						properties.put(name, value);
 					} else if ((name != null) && (value == null)) {
 						Object propEmb = getPropertyValue(child);
 						if (propEmb !=null) {
 							properties.put(name, propEmb);
 						}
 					}
 					
 				}
 			}
 			child = child.getNextSibling();
 		}
 		return properties;
 	}
 
 	/**
 	 * Get the properties.
 	 * 
 	 * @param componentNode
 	 *            node wihi contain properties.
 	 * @return the properties found.
 	 */
 	protected Properties getPropertiesFrom(Node componentNode, String preambule) {
 		Properties properties = new Properties();
 		NodeList nodelist = componentNode.getChildNodes();
 		Node child = null;
 		Node preambuleNode = null;
 		if (preambule != null) {
 			for (int i = 0; nodelist != null && i < nodelist.getLength(); i++) { // search
 				// preambule
 				// Node
 				// (<scheduler>,<dispatcher>..)
 				child = nodelist.item(i);
 				if (child != null
 						&& child.getNodeName().compareToIgnoreCase(preambule) == 0) {
 					preambuleNode = child;
 
 					break;
 				}
 				log.debug("properties in " + child.getNodeName() + "  in "
 						+ componentNode.getNodeName());
 			}
 		} else { // without preambule.
 			preambuleNode = componentNode;
 		}
 		if (preambuleNode == null) {
 			return properties;
 		}
 		Node propertyNode = preambuleNode.getFirstChild();
 		while (propertyNode != null) {
 
 			if (propertyNode.getNodeName().compareTo("property") == 0) {
 				if (propertyNode.hasAttributes()) {
 					String name = null;
 					String value = null;
 					NamedNodeMap nmp = propertyNode.getAttributes();
 					// obtain Property name.
 					Node component = nmp.getNamedItem("name");
 					if (component != null && component.getNodeName() != null) {
 						name = component.getNodeValue();
 					}
 					// obtain Property value.
 					component = nmp.getNamedItem("value");
 					if (component != null && component.getNodeName() != null) {
 						value = component.getNodeValue();
 					}
 
 					if (name != null) {
 						// TODO:REMOVE property.
 						if (preambule != null) { // if we have preambule (e.g.
 							// scheduler.<property>)
 							// name = preambule+"." + name;
 						}
 						//
 						if (value != null) {
 							properties.put(name, value);
 						} else { // we search dictionary value.
 							Object propEmb = getPropertyValue(propertyNode);
 							if (propEmb != null) {
 								properties.put(name, propEmb);
 							}
 						}
 					}
 				}
 			}
 			propertyNode = propertyNode.getNextSibling();
 		}
 		return properties;
 	}
 
 	protected Object getPropertyValue(Node propertyNode) {
 		Object returnValue = null;
 		PropertyType ptype = PropertyType.STRING;
 		// This method only acept Map/Array
 		Node node = propertyNode.getFirstChild();
 		log.debug("Obtaining  property value" + node.getNodeName());
 		while (node != null) {
 			if (node != null && node.getNodeName().compareToIgnoreCase("item") == 0) {
 				if (getAttributeValue(node, "key") != null) {
 					ptype = PropertyType.MAP;
 					break;
 				} else {
 					ptype = PropertyType.ARRAY;
 					break;
 				}
 			}
 			node = node.getNextSibling();
 		}
 		if (ptype == PropertyType.MAP) {
 			returnValue = getPropertyMap(propertyNode);
 			log.debug("Obtaining Map property value");
 		} else if (ptype == PropertyType.ARRAY) {
 			log.debug("Obtaining array property value");
 			returnValue = getPropertyArray(propertyNode);
 		} else {
 			log.error("Unsuported property value for " + propertyNode.getNodeName());
 		}
 		return returnValue;
 	}
 
 	private String[] getPropertyArray(Node arrayNode) {
 		String[] resultValue = null;
 		NodeList nlist = arrayNode.getChildNodes();
 		resultValue = new String[nlist.getLength()];
 		for (int i = 0; i < nlist.getLength(); i++) {
 			Node node = nlist.item(i);
 			String value = getAttributeValue(node, "value");
 			if (value != null) {
 				resultValue[i] = value;
 			} else {
 				log.error("Unable to obtain Array property" + arrayNode.getNodeName());
 				return null;
 			}
 		}
 		return resultValue;
 	}
 
 	private Map getPropertyMap(Node mapProperty) {
 		Properties map = new Properties();
 		Node node = mapProperty.getFirstChild();
 		
 		while (node != null) {
 			if (node.getNodeType() == Node.ELEMENT_NODE) {
 				String key = getAttributeValue(node, "key");
 				String value = getAttributeValue(node, "value");
 				if (key != null && value != null) {
 					map.put(key, value);
 				} else {
 					log.error("Unable to obtain Map property" + mapProperty.getNodeName());
 					return null;
 				}
 			}
 			node = node.getNextSibling();
 		}
 		return map;
 	}
 
 	protected String getType(Node node) {
 		NamedNodeMap attribs = node.getAttributes();
 		if (attribs == null) {
 			return null;
 		}
 		Node nodeType = attribs.getNamedItem("type");
 		String type = null;
 		if (nodeType != null) {
 			type = nodeType.getNodeValue();
 		}
 		return type;
 	}
 
 	protected String getId(Node node) {
 		NamedNodeMap attribs = node.getAttributes();
 		if (attribs == null) {
 			return null;
 		}
 		Node nodeiD = attribs.getNamedItem("id");
 		String id = null;
 		if (nodeiD != null) {
 			id = nodeiD.getNodeValue();
 		}
 		return id;
 	}
 
 	protected String getAttributeValue(Node node, String attrName) {
 		NamedNodeMap attribs = node.getAttributes();
 		if (attribs == null) {
 			return null;
 		}
 		Node nodeAttr = attribs.getNamedItem(attrName);
 		String value = null;
 		if (nodeAttr != null) {
 			value = nodeAttr.getNodeValue();
 		}
 		return value;
 	}
 
 	protected String getNodeValue(Node node) { // get the value of the Text node
 		// in element.
 		NodeList childNode = node.getChildNodes();
 		String value = null;
 		for (int i = 0; childNode != null && i < childNode.getLength(); i++) {
 			Node chnode = childNode.item(i);
 			if (chnode.getNodeType() == Node.TEXT_NODE) {
 				value = chnode.getNodeValue();
 				break;
 			}
 		}
 		return value;
 	}
 
 	private void bindExtenderParsers(CiliaExtenderParser parser) {
 		synchronized (customParsers) {
 			customParsers.add(parser);
 		}
 	}
 
 	private void unbindExtenderParser(CiliaExtenderParser parser) {
 		synchronized (customParsers) {
 			customParsers.remove(parser);
 		}
 	}
 
 	private Component extendersParsers(Node node, Component component) {
 		Set currentParsers = null;
 		synchronized (customParsers) {
 			currentParsers = new HashSet(customParsers);
 		}
 		Iterator it = currentParsers.iterator();
 		while (it.hasNext()) {
 			CiliaExtenderParser cep = (CiliaExtenderParser) it.next();
 			if (cep.canHandle(node)) {
 				try {
 					component =  cep.getComponent(node, component);
 				} catch (CiliaParserException e) {
 					e.printStackTrace();
 				}
 			}
 		}
 		return component;
 	}
 
 	public enum PropertyType {
 		STRING, MAP, ARRAY
 	};
 }
