 package xdi2.connector.template.mapping;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import xdi2.core.ContextNode;
 import xdi2.core.Graph;
 import xdi2.core.exceptions.Xdi2RuntimeException;
 import xdi2.core.features.dictionary.Dictionary;
 import xdi2.core.features.multiplicity.Multiplicity;
 import xdi2.core.impl.memory.MemoryGraphFactory;
 import xdi2.core.io.XDIReaderRegistry;
 import xdi2.core.xri3.impl.XDI3Segment;
 import xdi2.core.xri3.impl.XDI3SubSegment;
 
 public class TemplateMapping {
 
 	public static final XDI3Segment XRI_S_YOURSITE_CONTEXT = new XDI3Segment("+(https://yoursite.com/)");
 
 	private static final Logger log = LoggerFactory.getLogger(TemplateMapping.class);
 
 	private static TemplateMapping instance;
 
 	private Graph mappingGraph;
 
 	public TemplateMapping() {
 
 		this.mappingGraph = MemoryGraphFactory.getInstance().openGraph();
 
 		try {
 
 			XDIReaderRegistry.getAuto().read(this.mappingGraph, TemplateMapping.class.getResourceAsStream("mapping.xdi"));
 		} catch (Exception ex) {
 
 			throw new Xdi2RuntimeException(ex.getMessage(), ex);
 		}
 	}
 
 	public static TemplateMapping getInstance() {
 
 		if (instance == null) instance = new TemplateMapping();
 
 		return instance;
 	}
 
 	/**
 	 * Converts a yoursite.com data XRI to a native YourSite field identifier.
 	 * Example: $!(+(first_name)) --> first_name
 	 */
 	public String templateDataXriToTemplateFieldIdentifier(XDI3Segment templateDataXri) {
 
 		if (templateDataXri == null) throw new NullPointerException();
 
 		// convert
 
		String templateFieldIdentifier = Dictionary.instanceXriToNativeIdentifier(Multiplicity.baseArcXri(templateDataXri.getSubSegment(0)));
 
 		// done
 
 		if (log.isDebugEnabled()) log.debug("Converted " + templateDataXri + " to " + templateFieldIdentifier);
 
 		return templateFieldIdentifier;
 	}
 
 	/**
 	 * Maps and converts a yoursite.com data XRI to an XDI data XRI.
 	 * Example: $!(+(first_name)) --> +first$!(+name)
 	 */
 	public XDI3Segment templateDataXriToXdiDataXri(XDI3Segment templateDataXri) {
 
 		if (templateDataXri == null) throw new NullPointerException();
 
 		// map
 
 		XDI3SubSegment templateFieldXri = Dictionary.nativeIdentifierToInstanceXri(this.templateDataXriToTemplateFieldIdentifier(templateDataXri));
 
 		XDI3Segment templateDataDictionaryXri = new XDI3Segment("" + XRI_S_YOURSITE_CONTEXT + Dictionary.instanceXriToDictionaryXri(templateFieldXri));
 		ContextNode templateDataDictionaryContextNode = this.mappingGraph.findContextNode(templateDataDictionaryXri, false);
 		if (templateDataDictionaryContextNode == null) return null;
 
 		ContextNode xdiDataDictionaryContextNode = Dictionary.getCanonicalContextNode(templateDataDictionaryContextNode);
 		XDI3Segment xdiDataDictionaryXri = xdiDataDictionaryContextNode.getXri();
 
 		// convert
 
 		StringBuilder buffer = new StringBuilder();
 
 		for (int i=0; i<xdiDataDictionaryXri.getNumSubSegments(); i++) {
 
 			if (i + 1 < xdiDataDictionaryXri.getNumSubSegments()) {
 
				buffer.append(Multiplicity.entitySingletonArcXri(Dictionary.dictionaryXriToInstanceXri(xdiDataDictionaryXri.getSubSegment(i))));
 			} else {
 
				buffer.append(Multiplicity.attributeSingletonArcXri(Dictionary.dictionaryXriToInstanceXri(xdiDataDictionaryXri.getSubSegment(i))));
 			}
 		}
 
 		XDI3Segment xdiDataXri = new XDI3Segment(buffer.toString());
 
 		// done
 
 		if (log.isDebugEnabled()) log.debug("Mapped and converted " + templateDataXri + " to " + xdiDataXri);
 
 		return xdiDataXri;
 	}
 
 	/*
 	 * Getters and setters
 	 */
 
 	public Graph getMappingGraph() {
 
 		return this.mappingGraph;
 	}
 
 	public void setMappingGraph(Graph mappingGraph) {
 
 		this.mappingGraph = mappingGraph;
 	}
 }
