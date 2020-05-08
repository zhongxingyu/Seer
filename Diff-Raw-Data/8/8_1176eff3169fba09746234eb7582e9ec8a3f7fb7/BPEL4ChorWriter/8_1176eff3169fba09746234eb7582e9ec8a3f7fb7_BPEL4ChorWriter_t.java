 package org.bpel4chor.utils;
 
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.OutputStream;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import javax.wsdl.WSDLException;
 import javax.xml.namespace.QName;
 import javax.xml.stream.XMLOutputFactory;
 import javax.xml.stream.XMLStreamException;
 import javax.xml.stream.XMLStreamWriter;
 
 import org.apache.log4j.Logger;
 import org.bpel4chor.interfaces.INamespaceMap;
 import org.bpel4chor.model.grounding.impl.Grounding;
 import org.bpel4chor.model.topology.impl.MessageLink;
 import org.bpel4chor.model.topology.impl.Participant;
 import org.bpel4chor.model.topology.impl.ParticipantSet;
 import org.bpel4chor.model.topology.impl.ParticipantType;
 import org.bpel4chor.model.topology.impl.Topology;
 import org.eclipse.bpel.model.BPELPlugin;
 import org.eclipse.bpel.model.Process;
 import org.eclipse.bpel.model.resource.BPELResource;
 import org.eclipse.bpel.model.resource.BPELResourceFactoryImpl;
 import org.eclipse.emf.common.util.URI;
 import org.eclipse.emf.ecore.resource.Resource;
 import org.eclipse.emf.ecore.resource.ResourceSet;
 import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
 import org.eclipse.wst.wsdl.Definition;
 import org.eclipse.wst.wsdl.internal.impl.DefinitionImpl;
 import org.eclipse.wst.wsdl.internal.util.WSDLResourceFactoryImpl;
 import org.eclipse.xsd.util.XSDResourceFactoryImpl;
 
 import de.uni_stuttgart.iaas.bpel.model.utilities.ZipUtil;
 
 /**
  * Writer for BPEL4Chor(PBD, topology, grounding)
  * 
  * <p>
  * The writer will export topology and grounding file to the directory that is
  * set in the {@link #writeDir} or default C:\tmp\bpel4chor\.
  * 
  * @created Oct 6, 2011
  * @author Daojun Cui
  */
 public class BPEL4ChorWriter {
 	
 	/**
 	 * Directory to write bpel4chor artifacts
 	 */
 	protected static final String writeDir = BPEL4ChorConstants.BPEL4CHOR_DEFAULT_WRITE_DIR;
 	
 	/** whether to use prefix for bpel element */
 	protected static boolean useNSPrefix = false;
 	
 	protected static Logger log = Logger.getLogger(BPEL4ChorWriter.class);
 	
 	/**
 	 * check export directory, if it not exists, create it.
 	 */
 	static {
 		File dir = new File(BPEL4ChorWriter.writeDir);
 		if (!dir.exists()) {
 			dir.mkdirs();
 		}
 		
 		// initialize the eclipse workbench
 		BPELPlugin bpelPlugin = new BPELPlugin();
 		// setup the extension to factory map, so that the proper
 		// ResourceFactory can be used to read the file.
 		Resource.Factory.Registry.INSTANCE.getExtensionToFactoryMap().put("pbd", new BPELResourceFactoryImpl());
 		Resource.Factory.Registry.INSTANCE.getExtensionToFactoryMap().put("bpel", new BPELResourceFactoryImpl());
 		Resource.Factory.Registry.INSTANCE.getExtensionToFactoryMap().put("wsdl", new WSDLResourceFactoryImpl());
 		Resource.Factory.Registry.INSTANCE.getExtensionToFactoryMap().put("xsd", new XSDResourceFactoryImpl());
 	}
 	
 	
 	/**
 	 * Output BPEL4Chor artifacts - PBDs, WSDL definitions, topology, grounding.
 	 * 
 	 * @param participant2FragProc The map of participant to fragment process
 	 * @param participant2Defn The map of participant to fragment definition
 	 * @param topology The Topology
 	 * @param grounding The Grounding
 	 * @param outputPath The path where artifacts should be output to
 	 * @param destDir The destine directory where we put the zip file into
 	 * @param destZipName The name of the zip file
 	 * @throws IOException
 	 * @throws WSDLException
 	 * @throws XMLStreamException
 	 */
 	public static void writeBPEL4Chor(Map<String, Process> participant2FragProc, Map<String, Definition> participant2Defn, Topology topology, Grounding grounding, String outputPath, String destDir, String destZipName) throws IOException, WSDLException, XMLStreamException {
 		
 		// create output dir
 		File outputDir = new File(outputPath);
 		if (!outputDir.exists()) {
 			outputDir.mkdir();
 		}
 		
 		// write WSDL definition
 		for (Map.Entry<String, Definition> entry : participant2Defn.entrySet()) {
 			
 			String defnFileName = entry.getKey() + ".wsdl";
 			FileOutputStream defnOutputStream = new FileOutputStream(new File(outputDir, defnFileName));
 			Definition wsdlDef = entry.getValue();
 			BPEL4ChorWriter.writeWSDL(wsdlDef, defnOutputStream);
 			
 			BPEL4ChorWriter.log.info("write wsdl result: " + outputDir.getAbsolutePath() + File.separator + defnFileName);
 		}
 		
 		// write pbd
 		for (Process fragProcess : participant2FragProc.values()) {
 			
 			String pbdFileName = fragProcess.getName() + ".pbd";
 			FileOutputStream procOutputStream = new FileOutputStream(new File(outputDir, pbdFileName));
 			BPEL4ChorWriter.writePBD(fragProcess, procOutputStream);
 			
 			BPEL4ChorWriter.log.info("write pbd result: " + outputDir.getAbsolutePath() + File.separator + pbdFileName);
 		}
 		
 		// write topology
 		String topologyFileName = "topology.xml";
 		FileOutputStream topologyOutputStream = new FileOutputStream(new File(outputDir, topologyFileName));
 		BPEL4ChorWriter.writeTopology(topology, topologyOutputStream);
 		
 		BPEL4ChorWriter.log.info("write topology result: " + outputDir.getAbsolutePath() + File.separator + topologyFileName);
 		
 		// write grounding
 		String groundingFileName = "grounding.xml";
 		FileOutputStream groundingOutputStream = new FileOutputStream(new File(outputDir, groundingFileName));
 		BPEL4ChorWriter.writeGrounding(grounding, groundingOutputStream);
 		
 		BPEL4ChorWriter.log.info("write grounding result: " + outputDir.getAbsolutePath() + File.separator + groundingFileName);
 		
 		// compress the files under output directory and create zip file inside
 		// the destine directory.
 		ZipUtil.zip(outputPath, destDir, destZipName);
 	}
 	
 	// ********************************************************
 	// Participant Behavior Description
 	// ********************************************************
 	
 	/**
 	 * Write the pbd to file with the given BPEL process
 	 * 
 	 * @param process participant behavior description
 	 * @param fileName the name for the file to create
 	 * @return the created file name
 	 * @throws IOException
 	 */
 	public static void writePBD(Process process, String fileName) throws IOException {
 		
 		File targetFile = new File(fileName);
 		if (!targetFile.exists()) {
 			targetFile.createNewFile();
 		}
 		
 		FileOutputStream outputStream = new FileOutputStream(new File(fileName));
 		BPEL4ChorWriter.writePBD(process, outputStream);
 	}
 	
 	/**
 	 * Convert process into process-behavior-description and write to the
 	 * outputStream
 	 * 
 	 * @param process
 	 * @param outputStream
 	 * @throws IOException
 	 */
 	public static void writePBD(Process process, OutputStream outputStream) throws IOException {
 		
 		//
 		// BPELResource is just needed for providing bpel process to the
 		// BPEL/PBD writer, the URI is not used, we use "any.bpel" to bypass the
 		// Exception because of no URI presents.
 		//
 		boolean useNSPrefix = false;
 		ResourceSet resourceSet = new ResourceSetImpl();
 		URI uri = URI.createFileURI(process.getName() + ".bpel");
 		BPELResource resource = (BPELResource) resourceSet.createResource(uri);
 		resource.setOptionUseNSPrefix(useNSPrefix);
 		resource.getContents().add(process);
 		
 		PBDWriter writer = new PBDWriter();
 		Map args = new HashMap();
 		
 		args.put("", "");
 		writer.write(resource, outputStream, args);
 	}
 	
 	// ********************************************************
 	// Topology
 	// ********************************************************
 	
 	/**
 	 * Write the topology model to the given XML OutputStream.
 	 * 
 	 * @param topology
 	 * @param outputStream
 	 * @throws XMLStreamException
 	 * 
 	 */
 	public static void writeTopology(Topology topology, OutputStream outputStream) throws XMLStreamException {
 		
 		final String topoPrefix = "";
 		final String topoNamespaceURI = BPEL4ChorConstants.TOPOLOGY_XMLNS;
 		final String topoLocalname = "topology";
 		
 		// Create a XMLOutputFactory
 		XMLOutputFactory factory = XMLOutputFactory.newInstance();
 		
 		// Create XMLStreamWriter
 		XMLStreamWriter streamWriter = factory.createXMLStreamWriter(outputStream, "UTF-8");
 		
 		// write document open tag
 		streamWriter.writeStartDocument("UTF-8", "1.0");
 		streamWriter.writeCharacters(BPEL4ChorWriter.getNewLine(1));
 		
 		// write topology open tag and attributes
 		streamWriter.setDefaultNamespace(BPEL4ChorConstants.TOPOLOGY_XMLNS);
 		streamWriter.writeStartElement(topoPrefix, topoLocalname, topoNamespaceURI);
 		streamWriter.writeAttribute("name", topology.getName());
 		streamWriter.writeAttribute("targetNamespace", topology.getTargetNamespace());
 		streamWriter.writeDefaultNamespace(BPEL4ChorConstants.TOPOLOGY_XMLNS);
 		BPEL4ChorWriter.writeNamespaces(streamWriter, topology.getNamespaceMap());
 		BPEL4ChorWriter.writeExtAttributes(streamWriter, topology.getExtAttributes());
 		streamWriter.writeCharacters(BPEL4ChorWriter.getNewLine(1));
 		
 		// write topology sub elements
 		BPEL4ChorWriter.writeXMLNodeTopologyParticipantTypes(streamWriter, topology, 1);
 		streamWriter.writeCharacters(BPEL4ChorWriter.getNewLine(1));
 		
 		BPEL4ChorWriter.writeXMLNodeTopologyParticipants(streamWriter, topology, 1);
 		streamWriter.writeCharacters(BPEL4ChorWriter.getNewLine(1));
 		
 		BPEL4ChorWriter.writeXMLNodeTopologyMessageLinks(streamWriter, topology, 1);
 		streamWriter.writeCharacters(BPEL4ChorWriter.getNewLine(1));
 		
 		// write topology end tag
 		streamWriter.writeEndElement();
 		
 		// write document end tag
 		streamWriter.writeEndDocument();
 		
 		// flush out to the file
 		streamWriter.close();
 		
 	}
 	
 	/**
 	 * Write the topology model to default XML OutputStream with the given
 	 * fileName.
 	 * 
 	 * @param topology
 	 * @param fileName The File Name, without path
 	 * @return the created file name with path
 	 * @throws XMLStreamException
 	 * @throws FileNotFoundException
 	 */
 	public static String writeTopology(Topology topology, String fileName) throws XMLStreamException, FileNotFoundException {
 		// default dir + fileName
 		File topologyFile = new File(BPEL4ChorWriter.writeDir, fileName);
 		FileOutputStream outpuStream;
 		
 		// write to outputstream
 		outpuStream = new FileOutputStream(topologyFile);
 		BPEL4ChorWriter.writeTopology(topology, outpuStream);
 		
 		BPEL4ChorWriter.log.info("Result: " + topologyFile.getAbsolutePath());
 		return topologyFile.getAbsolutePath();
 		
 	}
 	
 	/**
 	 * Create the XML Node of participantTypes in topology
 	 * 
 	 * @param streamWriter
 	 * @param topology
 	 * @param tabIndent tab indent
 	 * @throws XMLStreamException
 	 */
 	private static void writeXMLNodeTopologyParticipantTypes(XMLStreamWriter streamWriter, Topology topology, int tabIndent) throws XMLStreamException {
 		// prefix, namespaceUri, localName for participantTypes and
 		// participantType
 		final String prefix = "";
 		final String namespaceURI = "";
 		final String partTypesLocalName = "participantTypes";
 		final String partTypeLocalName = "participantType";
 		
 		// start element participantTypes
 		streamWriter.writeCharacters(BPEL4ChorWriter.getTabs(tabIndent));
 		streamWriter.writeStartElement(prefix, partTypesLocalName, namespaceURI);
 		streamWriter.writeCharacters(BPEL4ChorWriter.getNewLine(1));
 		
 		// elements participantType
 		for (ParticipantType partType : topology.getParticipantTypes()) {
 			streamWriter.writeCharacters(BPEL4ChorWriter.getTabs(tabIndent + 1));
 			streamWriter.writeEmptyElement(prefix, partTypeLocalName, namespaceURI);
 			streamWriter.writeAttribute("name", partType.getName());
 			
 			if (partType.getParticipantBehaviorDescription() != null) {
 				streamWriter.writeAttribute("participantBehaviorDescription", BPEL4ChorWriter.qNameToString(partType.getParticipantBehaviorDescription()));
 			}
 			
 			if (partType.getProcessLanguage() != null) {
 				streamWriter.writeAttribute("processLanguage", partType.getProcessLanguage());
 			}
 			
 			streamWriter.writeCharacters(BPEL4ChorWriter.getNewLine(1));
 		}
 		
 		// end element participantTypes
 		streamWriter.writeCharacters(BPEL4ChorWriter.getTabs(tabIndent));
 		streamWriter.writeEndElement();
 		streamWriter.writeCharacters(BPEL4ChorWriter.getNewLine(1));
 	}
 	
 	/**
 	 * Create the XML Node for topology participants
 	 * 
 	 * @param streamWriter
 	 * @param topology
 	 * @param tabIndent how many tab the content should indent
 	 * @throws XMLStreamException
 	 */
 	private static void writeXMLNodeTopologyParticipants(XMLStreamWriter streamWriter, Topology topology, int tabIndent) throws XMLStreamException {
 		
 		// prefix, namespaceUri, localName for participants(with participant and
 		// participantSet)
 		final String prefix = "";
 		final String namespaceURI = "";
 		final String participantsLocalName = "participants";
 		
 		// start element participants
 		streamWriter.writeCharacters(BPEL4ChorWriter.getTabs(tabIndent));
 		streamWriter.writeStartElement(prefix, participantsLocalName, namespaceURI);
 		streamWriter.writeCharacters(BPEL4ChorWriter.getNewLine(1));
 		
 		// 1. participant
 		for (Participant p : topology.getParticipants()) {
 			BPEL4ChorWriter.writeXMLNodeTopologyParticipant(streamWriter, p, tabIndent + 1);
 		}
 		
 		// 2. participantSets
 		for (ParticipantSet pSet : topology.getParticipantSets()) {
 			BPEL4ChorWriter.writeXMLNodeTopologyParticipantSet(streamWriter, pSet, tabIndent + 1);
 		}
 		
 		// end element participants
 		streamWriter.writeCharacters(BPEL4ChorWriter.getTabs(tabIndent));
 		streamWriter.writeEndElement();
 		streamWriter.writeCharacters(BPEL4ChorWriter.getNewLine(1));
 		
 	}
 	
 	/**
 	 * Create the XML Node for topology messageLinks
 	 * 
 	 * @param streamWriter
 	 * @param topology
 	 * @param tabIndent tab indent
 	 * @throws XMLStreamException
 	 */
 	private static void writeXMLNodeTopologyMessageLinks(XMLStreamWriter streamWriter, Topology topology, int tabIndent) throws XMLStreamException {
 		// prefix, namespaceUri, localName for messageLinks
 		final String prefix = "";
 		final String namespaceURI = "";
 		final String messageLinkssLocalName = "messageLinks";
 		final String messageLinkLocalName = "messageLink";
 		
 		// start element messageLinks
 		streamWriter.writeCharacters(BPEL4ChorWriter.getTabs(tabIndent));
 		streamWriter.writeStartElement(prefix, messageLinkssLocalName, namespaceURI);
 		streamWriter.writeCharacters(BPEL4ChorWriter.getNewLine(1));
 		
 		// sub element topology messageLink
 		for (MessageLink ml : topology.getMessageLinks()) {
 			streamWriter.writeCharacters(BPEL4ChorWriter.getTabs(tabIndent + 1));
 			streamWriter.writeEmptyElement(prefix, messageLinkLocalName, namespaceURI);
 			streamWriter.writeAttribute("name", ml.getName());
 			streamWriter.writeAttribute("messageName", ml.getMessageName());
 			if (ml.getSender() != null) {
 				streamWriter.writeAttribute("sender", ml.getSender());
 			}
 			if ((ml.getSenders() != null) && !ml.getSenders().isEmpty()) {
 				streamWriter.writeAttribute("senders", BPEL4ChorWriter.namesToString(ml.getSenders()));
 			}
 			if (ml.getSendActivity() != null) {
 				streamWriter.writeAttribute("sendActivity", ml.getSendActivity());
 			}
 			if (ml.getReceiver() != null) {
 				streamWriter.writeAttribute("receiver", ml.getReceiver());
 			}
 			if (ml.getReceiveActivity() != null) {
 				streamWriter.writeAttribute("receiveActivity", ml.getReceiveActivity());
 			}
 			streamWriter.writeCharacters(BPEL4ChorWriter.getNewLine(1));
 		}
 		
 		// end element messageLink
 		streamWriter.writeCharacters(BPEL4ChorWriter.getTabs(tabIndent));
 		streamWriter.writeEndElement();
 		streamWriter.writeCharacters(BPEL4ChorWriter.getNewLine(1));
 	}
 	
 	/**
 	 * Create XML Node of topology participant
 	 * 
 	 * @param streamWriter
 	 * @param p
 	 * @param tabIndent
 	 * @throws XMLStreamException
 	 */
 	private static void writeXMLNodeTopologyParticipant(XMLStreamWriter streamWriter, Participant p, int tabIndent) throws XMLStreamException {
 		// prefix, namespaceUri, localName for participant
 		final String prefix = "";
 		final String namespaceURI = "";
 		final String participantLocalName = "participant";
 		
 		streamWriter.writeCharacters(BPEL4ChorWriter.getTabs(tabIndent));
 		streamWriter.writeEmptyElement(prefix, participantLocalName, namespaceURI);
 		streamWriter.writeAttribute("name", p.getName());
 		streamWriter.writeAttribute("type", p.getType());
 		if ((p.getSelects() != null) && !p.getSelects().isEmpty()) {
 			streamWriter.writeAttribute("selects", BPEL4ChorWriter.namesToString(p.getSelects()));
 		}
 		if (p.getScope() != null) {
 			streamWriter.writeAttribute("scope", BPEL4ChorWriter.qNameToString(p.getScope()));
 		}
 		if (p.getContainment() != null) {
 			streamWriter.writeAttribute("containment", p.getContainment());
 		}
 		if ((p.getForEach() != null) && !p.getForEach().isEmpty()) {
 			streamWriter.writeAttribute("forEach", BPEL4ChorWriter.qNamesToString(p.getForEach()));
 		}
 		streamWriter.writeCharacters(BPEL4ChorWriter.getNewLine(1));
 	}
 	
 	/**
 	 * Create the XML Node for topology participantSet
 	 * 
 	 * @param streamWriter
 	 * @param pSet participantSet
 	 * @param tabIndent
 	 * @throws XMLStreamException
 	 */
 	private static void writeXMLNodeTopologyParticipantSet(XMLStreamWriter streamWriter, ParticipantSet pSet, int tabIndent) throws XMLStreamException {
 		// prefix, namespaceUri, localName for participantSet
 		final String prefix = "";
 		final String namespaceURI = "";
 		final String participantSetLocalName = "participantSet";
 		
 		// start element participantSet
 		streamWriter.writeCharacters(BPEL4ChorWriter.getTabs(tabIndent));
 		streamWriter.writeStartElement(prefix, participantSetLocalName, namespaceURI);
 		
 		// attribute name, type, scope, forEach
 		streamWriter.writeAttribute("name", pSet.getName());
 		streamWriter.writeAttribute("type", pSet.getType());
 		if (pSet.getScope() != null) {
 			streamWriter.writeAttribute("scope", BPEL4ChorWriter.qNameToString(pSet.getScope()));
 		}
 		if (pSet.getForEach() != null) {
 			streamWriter.writeAttribute("forEach", BPEL4ChorWriter.qNamesToString(pSet.getForEach()));
 		}
 		
 		streamWriter.writeCharacters(BPEL4ChorWriter.getNewLine(1));
 		
 		// 1. sub-participant
 		for (Participant p : pSet.getParticipantList()) {
 			BPEL4ChorWriter.writeXMLNodeTopologyParticipant(streamWriter, p, tabIndent + 1);
 		}
 		
 		// 2. sub-participantSet
 		for (ParticipantSet subPSet : pSet.getParticipantSetList()) {
 			BPEL4ChorWriter.writeXMLNodeTopologyParticipantSet(streamWriter, subPSet, tabIndent + 1);
 		}
 		
 		// end element participantSet
 		streamWriter.writeCharacters(BPEL4ChorWriter.getTabs(tabIndent));
 		streamWriter.writeEndElement();
 		streamWriter.writeCharacters(BPEL4ChorWriter.getNewLine(1));
 		
 	}
 	
 	// ********************************************************
 	// Grounding
 	// ********************************************************
 	
 	/**
 	 * Write the Grounding model to xml outputStream
 	 * 
 	 * @param grounding
 	 * @param outputStream
 	 * @throws XMLStreamException
 	 */
 	public static void writeGrounding(Grounding grounding, OutputStream outputStream) throws XMLStreamException {
 		final String grouPrefix = "";
 		final String grouNamespaceURI = BPEL4ChorConstants.GROUNDING_XMLNS;
 		final String grouLocalname = "grounding";
 		
 		// Create a XMLOutputFactory
 		XMLOutputFactory factory = XMLOutputFactory.newInstance();
 		
 		// Create XMLStreamWriter
 		XMLStreamWriter streamWriter = factory.createXMLStreamWriter(outputStream, "UTF-8");
 		
 		// write document open tag
 		streamWriter.writeStartDocument("UTF-8", "1.0");
 		streamWriter.writeCharacters(BPEL4ChorWriter.getNewLine(1));
 		
 		// write grounding open tag
 		streamWriter.setDefaultNamespace(BPEL4ChorConstants.GROUNDING_XMLNS);
 		streamWriter.writeStartElement(grouPrefix, grouLocalname, grouNamespaceURI);
 		streamWriter.writeAttribute("topology", BPEL4ChorWriter.qNameToString(grounding.getTopology()));
 		streamWriter.writeAttribute("targetNamespace", grounding.getTargetNamespace());
 		streamWriter.writeDefaultNamespace(BPEL4ChorConstants.GROUNDING_XMLNS);
 		BPEL4ChorWriter.writeNamespaces(streamWriter, grounding.getNamespaceMap());
 		BPEL4ChorWriter.writeExtAttributes(streamWriter, grounding.getExtAttributes());
 		streamWriter.writeCharacters(BPEL4ChorWriter.getNewLine(1));
 		
 		// write grounding sub elements
 		BPEL4ChorWriter.writeXMLNodeGroundingMessageLinks(streamWriter, grounding, 1);
 		streamWriter.writeCharacters(BPEL4ChorWriter.getNewLine(1));
 		
 		BPEL4ChorWriter.writeXMLNodeGroundingParticipantRefs(streamWriter, grounding, 1);
 		streamWriter.writeCharacters(BPEL4ChorWriter.getNewLine(1));
 		
 		BPEL4ChorWriter.writeXMLNodeGroundingProperties(streamWriter, grounding, 1);
 		streamWriter.writeCharacters(BPEL4ChorWriter.getNewLine(1));
 		
 		// write grounding end tag
 		streamWriter.writeEndElement();
 		
 		// write document end tag
 		streamWriter.writeEndDocument();
 		
 		// flush out to the file
 		streamWriter.close();
 		
 	}
 	
 	/**
 	 * Write the Grounding model to default outputStream with the given
 	 * fileName.
 	 * 
 	 * @param grounding
 	 * @param fileName
 	 * @return the created file name
 	 * @throws FileNotFoundException
 	 * @throws XMLStreamException
 	 */
 	public static String writeGrounding(Grounding grounding, String fileName) throws FileNotFoundException, XMLStreamException {
 		// create file reference
 		File groundingFile = new File(BPEL4ChorWriter.writeDir, fileName);
 		
 		FileOutputStream outputStream;
 		outputStream = new FileOutputStream(groundingFile);
 		BPEL4ChorWriter.writeGrounding(grounding, outputStream);
 		
 		System.out.println("Result: " + groundingFile.getAbsolutePath());
 		return groundingFile.getAbsolutePath();
 		
 	}
 	
 	/**
 	 * Write a XML Node for grounding MessageLinks
 	 * 
 	 * @param streamWriter
 	 * @param grounding
 	 * @param tabIndent
 	 * @throws XMLStreamException
 	 */
 	private static void writeXMLNodeGroundingMessageLinks(XMLStreamWriter streamWriter, Grounding grounding, int tabIndent) throws XMLStreamException {
 		final String prefix = "";
 		final String namespaceURI = "";
 		final String localName = "messageLinks";
 		
 		// start element of messageLinks
 		streamWriter.writeCharacters(BPEL4ChorWriter.getTabs(tabIndent));
 		streamWriter.writeStartElement(prefix, localName, namespaceURI);
 		streamWriter.writeCharacters(BPEL4ChorWriter.getNewLine(1));
 		
 		// sub element messageLink
 		for (org.bpel4chor.model.grounding.impl.MessageLink ml : grounding.getMessageLinks()) {
 			// start messageLink element
 			streamWriter.writeCharacters(BPEL4ChorWriter.getTabs(tabIndent + 1));
 			streamWriter.writeEmptyElement(prefix, "messageLink", namespaceURI);
 			streamWriter.writeAttribute("name", ml.getName());
 			streamWriter.writeAttribute("portType", BPEL4ChorWriter.qNameToString(ml.getPortType()));
 			streamWriter.writeAttribute("operation", ml.getOperation());
 			streamWriter.writeCharacters(BPEL4ChorWriter.getNewLine(1));
 		}
 		
 		// end element of messageLinks
 		streamWriter.writeCharacters(BPEL4ChorWriter.getTabs(tabIndent));
 		streamWriter.writeEndElement();
 		streamWriter.writeCharacters(BPEL4ChorWriter.getNewLine(1));
 		
 	}
 	
 	/**
 	 * Write a XML Node for grounding participantRefs
 	 * 
 	 * @param streamWriter
 	 * @param grounding
 	 * @param tabIndent
 	 */
 	private static void writeXMLNodeGroundingParticipantRefs(XMLStreamWriter streamWriter, Grounding grounding, int tabIndent) throws XMLStreamException {
 		final String prefix = "";
 		final String namespaceURI = "";
 		final String localName = "participantRefs";
 		
 		if (grounding.getParticipantRefs().isEmpty()) {
 			return;
 		}
 		
 		// start element of participantRefs
 		streamWriter.writeCharacters(BPEL4ChorWriter.getTabs(tabIndent));
 		streamWriter.writeStartElement(prefix, localName, namespaceURI);
 		streamWriter.writeCharacters(BPEL4ChorWriter.getNewLine(1));
 		
 		// sub element participantRef
 		for (org.bpel4chor.model.grounding.impl.ParticipantRef pRef : grounding.getParticipantRefs()) {
 			// start messageLink element
 			streamWriter.writeCharacters(BPEL4ChorWriter.getTabs(tabIndent + 1));
 			streamWriter.writeEmptyElement(prefix, "participantRef", namespaceURI);
 			streamWriter.writeAttribute("name", pRef.getName());
 			streamWriter.writeAttribute("WSDLproperty", BPEL4ChorWriter.qNameToString(pRef.getWSDLproperty()));
 			streamWriter.writeCharacters(BPEL4ChorWriter.getNewLine(1));
 		}
 		
 		// end element of participantRefs
 		streamWriter.writeCharacters(BPEL4ChorWriter.getTabs(tabIndent));
 		streamWriter.writeEndElement();
 		streamWriter.writeCharacters(BPEL4ChorWriter.getNewLine(1));
 	}
 	
 	/**
 	 * Write a XML Node for grounding properties
 	 * 
 	 * @param streamWriter
 	 * @param grounding
 	 * @param tabIndent
 	 */
 	private static void writeXMLNodeGroundingProperties(XMLStreamWriter streamWriter, Grounding grounding, int tabIndent) throws XMLStreamException {
 		final String prefix = "";
 		final String namespaceURI = "";
 		final String localName = "properties";
 		
 		if (grounding.getProperties().isEmpty()) {
 			return;
 		}
 		
 		// start element of participantRefs
 		streamWriter.writeCharacters(BPEL4ChorWriter.getTabs(tabIndent));
 		streamWriter.writeStartElement(prefix, localName, namespaceURI);
 		streamWriter.writeCharacters(BPEL4ChorWriter.getNewLine(1));
 		
 		// sub element participantRef
 		for (org.bpel4chor.model.grounding.impl.Property property : grounding.getProperties()) {
 			// start messageLink element
 			streamWriter.writeCharacters(BPEL4ChorWriter.getTabs(tabIndent + 1));
 			streamWriter.writeEmptyElement(prefix, "property", namespaceURI);
 			streamWriter.writeAttribute("name", property.getName());
 			streamWriter.writeAttribute("WSDLproperty", BPEL4ChorWriter.qNameToString(property.getWSDLproperty()));
 			streamWriter.writeCharacters(BPEL4ChorWriter.getNewLine(1));
 		}
 		
 		// end element of participantRefs
 		streamWriter.writeCharacters(BPEL4ChorWriter.getTabs(tabIndent));
 		streamWriter.writeEndElement();
 		streamWriter.writeCharacters(BPEL4ChorWriter.getNewLine(1));
 	}
 	
 	/**
 	 * Write the BPEL process to BPEL file using BPELResource
 	 * 
 	 * @param process
 	 * @param outputStream
 	 * @param outputDir
 	 * @throws IOException
 	 */
 	public static void writeAbstractBPEL(Process process, OutputStream outputStream, String outputDir) throws IOException {
 		
 		//
 		// BPELResource is just needed for providing bpel process to the
 		// BPEL/PBD writer, the URI is not used, we use "any.bpel" to bypass the
 		// Exception because of no URI presents.
 		//
 		boolean useNSPrefix = false;
 		ResourceSet resourceSet = new ResourceSetImpl();
 		URI uri = URI.createFileURI(outputDir + process.getName() + ".bpel");
 		BPELResource resource = (BPELResource) resourceSet.createResource(uri);
 		resource.setOptionUseNSPrefix(useNSPrefix);
 		resource.getContents().add(process);
 		
 		AbstractBPELWriter writer = new AbstractBPELWriter();
 		Map<String, Boolean> args = new HashMap<String, Boolean>();
 		args.put(AbstractBPELWriter.SKIP_AUTO_IMPORT, true);
 		writer.write(resource, outputStream, args);
 	}
 	
 	/**
 	 * Write the BPEL process to BPEL file using BPELResource
 	 * 
 	 * @param process
 	 * @param outputStream
 	 * @throws IOException
 	 */
 	public static void writeBPEL(Process process, OutputStream outputStream) throws IOException {
 		
 		ResourceSet resourceSet = new ResourceSetImpl();
 		URI uri = URI.createFileURI(process.getName() + ".bpel");
 		BPELResource resource = (BPELResource) resourceSet.createResource(uri);
 		resource.getContents().add(process);
 		
 		Map argsMap = new HashMap();
 		argsMap.put("", "");// this args map prevents a NPE in the release 1.0
 		resource.save(outputStream, argsMap);
 	}
 	
 	/**
 	 * Write the BPEL process model to DOM model, then write the DOM model to
 	 * output stream.
 	 * 
 	 * @param bpelResource
 	 * @param outputStream The OutputStream
 	 * @throws IOException
 	 */
 	public static void writeBPEL(BPELResource bpelResource, OutputStream outputStream) throws IOException {
 		
 		if (bpelResource.getContents().isEmpty()) {
 			throw new IllegalStateException("The bpelResource is empty");
 		}
 		
 		Map argsMap = new HashMap();
 		argsMap.put("", "");// this args map prevents a NPE in the release 1.0
 		bpelResource.save(outputStream, argsMap);
 		
 	}
 	
 	/**
 	 * Write WSDL to outputStream
 	 * 
 	 * @param defn
 	 * @param outputStream
 	 * @throws WSDLException
 	 * @throws IOException
 	 */
 	public static void writeWSDL(Definition defn, OutputStream outputStream) throws WSDLException, IOException {
 		Resource resource = ((DefinitionImpl) defn).eResource();
 		
 		//CHECK wirklich einen fehler werfen?, processname unten durch wsdlname ersetz, wird nun ausgelesen... sicher?
 //		if ((processName == null) || processName.isEmpty()) {
 //			throw new IllegalStateException();
 //		}
 		
 		if (resource == null) {
 			String baseURI = defn.getDocumentBaseURI();
			String wsdlName;
 			if (baseURI == null) {
				wsdlName = "output.wsdl";
			} else {
				wsdlName = baseURI.substring(baseURI.lastIndexOf("/") + 1);
 			}
 			ResourceSet rs = new ResourceSetImpl();
 			rs.getResourceFactoryRegistry().getExtensionToFactoryMap().put("wsdl", new WSDLResourceFactoryImpl());
 			resource = rs.createResource(URI.createFileURI(wsdlName));
 			resource.getContents().add(defn);
 		}
 		
 		Map args = new HashMap();
 		args.put("", "");
 		resource.save(outputStream, args);
 	}
 	
 	// ********************************************************
 	// Utils
 	// ********************************************************
 	
 	/**
 	 * Write the namespace attributes
 	 */
 	private static void writeNamespaces(XMLStreamWriter streamWriter, INamespaceMap<String, String> namespaceMap) throws XMLStreamException {
 		if ((namespaceMap == null) || namespaceMap.isEmpty()) {
 			return;
 		}
 		
 		Set<String> prefixSet = namespaceMap.keySet();
 		for (String prefix : prefixSet) {
 			String namespaceURI = namespaceMap.get(prefix);
 			
 			// String attrName = prefix.equals("")?"xmlns":"xmlns:"+prefix;
 			// streamWriter.writeAttribute(attrName, namespaceURI);
 			streamWriter.writeNamespace(prefix, namespaceURI);
 		}
 	}
 	
 	/**
 	 * Write the extra attributes
 	 * 
 	 * @param streamWriter
 	 * @param attributes
 	 * @throws XMLStreamException
 	 */
 	private static void writeExtAttributes(XMLStreamWriter streamWriter, INamespaceMap<String, String> attributes) throws XMLStreamException {
 		if ((attributes == null) || attributes.isEmpty()) {
 			return;
 		}
 		
 		Set<String> attrNameSet = attributes.keySet();
 		for (String attrName : attrNameSet) {
 			String attrVal = attributes.get(attrName);
 			// bad idea to insert the indent here, because between the
 			// attributes, there can not be any interruption, such as 'tab'
 			// streamWriter.writeCharacters(getTabs(tabIndent));
 			streamWriter.writeAttribute(attrName, attrVal);
 		}
 	}
 	
 	/**
 	 * String wild-card for tab, "\t".
 	 * 
 	 * @param count
 	 * @return tabs string according to the count
 	 */
 	private static String getTabs(int count) {
 		String tabs = "";
 		for (int i = 0; i < count; i++) {
 			tabs += "\t";
 		}
 		return tabs;
 	}
 	
 	/**
 	 * String of wild-card for new line, "\n".
 	 * 
 	 * @param count
 	 * @return newLines string according to the count
 	 */
 	private static String getNewLine(int count) {
 		String newLine = "";
 		for (int i = 0; i < count; i++) {
 			newLine += "\n";
 		}
 		return newLine;
 	}
 	
 	/**
 	 * Get the qName string with the form "prefox:localName" or "localName".
 	 * 
 	 * <p>
 	 * <b>Note</b>: the namespaceUri have been handled as topology is created,
 	 * in that case, the namespaceUri must have been stored in the namespace map
 	 * of topology. so we do not need to care about it here.
 	 * 
 	 * @param qName
 	 * @return prefixed name
 	 */
 	private static String qNameToString(QName qName) {
 		if ((qName.getPrefix() == null) || (qName.getPrefix().length() == 0)) {
 			return qName.getLocalPart();
 		} else {
 			return qName.getPrefix() + ":" + qName.getLocalPart();
 		}
 		
 	}
 	
 	/**
 	 * Get the qNames string with the form "qName1 qName2 qName3 ..."
 	 * 
 	 * @param qNames
 	 * @return
 	 */
 	private static String qNamesToString(List<QName> qNames) {
 		StringBuffer sb = new StringBuffer();
 		for (QName qName : qNames) {
 			sb.append(BPEL4ChorWriter.qNameToString(qName) + " ");
 		}
 		return sb.toString().trim();
 	}
 	
 	/**
 	 * Get the string of the name list, with the form "name1 name2 name3 ..."
 	 * 
 	 * @param names
 	 * @return
 	 */
 	private static String namesToString(List<String> names) {
 		StringBuffer sb = new StringBuffer();
 		for (String name : names) {
 			sb.append(name + " ");
 		}
 		return sb.toString().trim();
 	}
 	
 	/**
 	 * Print topology to console
 	 * 
 	 * @param topology
 	 * @throws XMLStreamException
 	 */
 	public static void printTopology(Topology topology) throws XMLStreamException {
 		BPEL4ChorWriter.writeTopology(topology, System.out);
 	}
 	
 	/**
 	 * Print grounding to console
 	 * 
 	 * @param grounding
 	 * @throws XMLStreamException
 	 */
 	public static void printGrounding(Grounding grounding) throws XMLStreamException {
 		BPEL4ChorWriter.writeGrounding(grounding, System.out);
 	}
 	
 }
