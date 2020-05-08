 package fi.smaa.jsmaadd;
 
 import java.io.BufferedInputStream;
 import java.io.BufferedOutputStream;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Map;
 
 import javax.xml.parsers.DocumentBuilder;
 import javax.xml.parsers.DocumentBuilderFactory;
 import javax.xml.parsers.ParserConfigurationException;
 import javax.xml.transform.Result;
 import javax.xml.transform.Source;
 import javax.xml.transform.Transformer;
 import javax.xml.transform.TransformerConfigurationException;
 import javax.xml.transform.TransformerException;
 import javax.xml.transform.TransformerFactory;
 import javax.xml.transform.TransformerFactoryConfigurationError;
 import javax.xml.transform.dom.DOMSource;
 import javax.xml.transform.stream.StreamResult;
 
 import org.kohsuke.args4j.CmdLineException;
 import org.kohsuke.args4j.CmdLineParser;
 import org.w3c.dom.Attr;
 import org.w3c.dom.DOMImplementation;
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 import org.w3c.dom.Node;
 import org.w3c.dom.NodeList;
 import org.xml.sax.SAXException;
 
 import fi.smaa.jsmaa.SMAA2Results;
 import fi.smaa.jsmaa.SMAA2SimulationThread;
 import fi.smaa.jsmaa.SMAASimulator;
 import fi.smaa.jsmaa.model.Alternative;
 import fi.smaa.jsmaa.model.CardinalCriterion;
 import fi.smaa.jsmaa.model.Criterion;
 import fi.smaa.jsmaa.model.ImpactMatrix;
 import fi.smaa.jsmaa.model.Interval;
 import fi.smaa.jsmaa.model.SMAAModel;
 import fi.smaa.jsmaa.model.ScaleCriterion;
 
 public class SMAADDMain {
 	
 	private static final String ALTFILE = "alternatives.xml";
 	private static final String CRITFILE = "criteria.xml";
 	private static final String IMPACTFILE = "performanceTable.xml";
 	private static final int ITERATIONS = 10000;
 	private static final String RANKACCFILE = "rankAcceptabilities.xml";
 	private static final String CWFILE = "centralWeights.xml";
 	private static final String CONFFACFILE = "confidenceFactors.xml";
 	private static final String MSGSFILE = "messages.xml";
 	
 	private List<Alternative> alts;
 	private List<Criterion> crit;
 	private ImpactMatrix impactMatrix;
 	private SMAAModel model;
 	private SMAA2Results results;
 
 	public static void main(String[] args) {
 		MyOptions opts = new MyOptions();
 		CmdLineParser parser = new CmdLineParser(opts);
 		try {
 			parser.parseArgument(args);		
 			if (opts.getInputDir() == null || opts.getOutputDir() == null) {
 				printUsage();
 				return;
 			}
 		} catch(CmdLineException e ) {
 			System.err.println(e.getMessage());
 			printUsage();			
 			return;
 		}
 		SMAADDMain main = null;
 		try {
 			main = new SMAADDMain();
 			main.loadInputFiles(opts.getInputDir());
 		} catch (SAXException e) {
 			System.err.println("Invalid format in input file - " + e.getMessage());
 			return;
 		} catch (Exception e) {
 			System.err.println("Unable to load input file: " + e.getMessage());
 			return;
 		}
 		main.constructModel();
 		main.compute();		
 		try {
 			main.writeResults(opts.getOutputDir());
 		} catch (Exception e) {
 			System.err.println("Unable to write output file: " + e.getMessage());
 		}	
 	}
 
 	private static void printUsage() {
 		System.err.println("Usage: java -jar JARFILE -i inputDir -o outputDir");
 		
 	}
 
 	private void writeResults(String outputDir) throws ParserConfigurationException, TransformerException, IOException {
 		writeRankAcceptabilities(outputDir);	
 		writeCentralWeights(outputDir);
 		writeConfidenceFactors(outputDir);
 		List<OutputMessage> msgs = new ArrayList<OutputMessage>();
 		msgs.add(new OutputMessage("OK", "Done", OutputMessage.Type.LOG));
 		writeMessages(outputDir, msgs);
 	}
 
 	private void writeConfidenceFactors(String outputDir) throws ParserConfigurationException, TransformerConfigurationException, TransformerFactoryConfigurationError, TransformerException, IOException {
 		String fileName = outputDir + File.separator + CONFFACFILE;
 		BufferedOutputStream bufStream = openFile(fileName);		
 		Document doc = createDocument();
 		
 		Node topNode = doc.createElement("alternativesCriteriaValues");
 		Attr conceptAttrib = doc.createAttribute("mcdaConcept");
 		conceptAttrib.setValue("confidenceFactors");
 		topNode.getAttributes().setNamedItem(conceptAttrib);
 		
 		doc.getDocumentElement().appendChild(topNode);
 		
 		Map<Alternative, Double> cfs = results.getConfidenceFactors();		
 		for (Alternative a : alts) {			
 			Node cwNode = doc.createElement("alternativeCriteriaValue");
 			Node altNode = doc.createElement("alternativeID");
 			altNode.setTextContent(a.getName());
 			cwNode.appendChild(altNode);
 			
 			Node valueNode = doc.createElement("value");			
 			Node realNode = doc.createElement("real");
 			realNode.setTextContent(cfs.get(a).toString());
 			valueNode.appendChild(realNode);
 			cwNode.appendChild(valueNode);
 			topNode.appendChild(cwNode);
 		}		
 		
 		writeDocument(bufStream, doc);
 		bufStream.close();			
 	}
 
 	private void writeCentralWeights(String outputDir) throws ParserConfigurationException, TransformerConfigurationException, TransformerFactoryConfigurationError, TransformerException, IOException {
 		String fileName = outputDir + File.separator + CWFILE;
 		BufferedOutputStream bufStream = openFile(fileName);		
 		Document doc = createDocument();
 		
 		Node topNode = doc.createElement("alternativesCriteriaValues");
 		Attr conceptAttrib = doc.createAttribute("mcdaConcept");
 		conceptAttrib.setValue("centralWeights");
 		topNode.getAttributes().setNamedItem(conceptAttrib);
 		
 		doc.getDocumentElement().appendChild(topNode);		
 		for (Alternative a : alts) {
 			Map<Criterion, Double> cws = results.getCentralWeightVectors().get(a);
 			
 			Node cwNode = doc.createElement("alternativeCriteriaValue");
 			Node altNode = doc.createElement("alternativeID");
 			altNode.setTextContent(a.getName());
 			cwNode.appendChild(altNode);
 			
 			for (Criterion c : crit) {
 				Node critNode = doc.createElement("criterionValue");
 				Node critIdNode = doc.createElement("criterionID");
 				critIdNode.setTextContent(c.getName());
 				critNode.appendChild(critIdNode);
 
 				Node valueNode = doc.createElement("value");			
 				Node realNode = doc.createElement("real");
 				realNode.setTextContent(cws.get(c).toString());
 				valueNode.appendChild(realNode);
 				critNode.appendChild(valueNode);
 				
 				
 				cwNode.appendChild(critNode);
 			}
 			topNode.appendChild(cwNode);
 		}
 		writeDocument(bufStream, doc);
 		bufStream.close();		
 	}
 	
 	private void writeMessages(String outputDir, List<OutputMessage> msgs) throws ParserConfigurationException, TransformerConfigurationException, TransformerFactoryConfigurationError, TransformerException, IOException {
 		String fileName = outputDir + File.separator + MSGSFILE;
 		BufferedOutputStream bufStream = openFile(fileName);		
 		Document doc = createDocument();
 
 		Node topNode = doc.createElement("methodMessages");
 		doc.getDocumentElement().appendChild(topNode);			
 		for (OutputMessage m : msgs) {
 			topNode.appendChild(m.createXmlNode(doc));
 		}
 
 		writeDocument(bufStream, doc);
 		bufStream.close();
 	}
 
 	private void writeRankAcceptabilities(String outputDir)
 			throws FileNotFoundException, ParserConfigurationException,
 			TransformerFactoryConfigurationError,
 			TransformerConfigurationException, TransformerException,
 			IOException {
 		String fileName = outputDir + File.separator + RANKACCFILE;
 		BufferedOutputStream bufStream = openFile(fileName);		
 		Document doc = createDocument();
 		
 		Node topNode = doc.createElement("alternativesValues");
 		Attr conceptAttrib = doc.createAttribute("mcdaConcept");
 		conceptAttrib.setValue("rankAcceptabilities");
 		topNode.getAttributes().setNamedItem(conceptAttrib);	
 		
 		doc.getDocumentElement().appendChild(topNode);		
 		
 		for (Alternative a : alts) {
 			for (int i=0;i<alts.size();i++) {
 				Node avalNode = doc.createElement("alternativeValue");
 				topNode.appendChild(avalNode);
 				Node altIdNode = doc.createElement("alternativeID");
 				altIdNode.setTextContent(a.getName());
 				avalNode.appendChild(altIdNode);
 				List<Double> racs = results.getRankAcceptabilities().get(a);
 				
 				Node valuesNode = doc.createElement("values");
 				avalNode.appendChild(valuesNode);
 
 				
 				Node rankNode = doc.createElement("value");
 				Attr rankAttrib = doc.createAttribute("mcdaConcept");
 				rankAttrib.setValue("rank");
 				rankNode.getAttributes().setNamedItem(rankAttrib);
 				valuesNode.appendChild(rankNode);
 
 				Node integerNode = doc.createElement("integer");
 				integerNode.setTextContent(new Integer(i+1).toString());
 				rankNode.appendChild(integerNode);				
 
 				Node accNode = doc.createElement("value");				
 				Attr accAttrib = doc.createAttribute("mcdaConcept");
 				accAttrib.setValue("acceptability");
 				accNode.getAttributes().setNamedItem(accAttrib);
 				valuesNode.appendChild(accNode);
 
 				Node realNode = doc.createElement("real");
 				realNode.setTextContent(racs.get(i).toString());
 				accNode.appendChild(realNode);				
 			}
 		}
 		writeDocument(bufStream, doc);
 		bufStream.close();
 	}
 
 	private BufferedOutputStream openFile(String fileName)
 			throws FileNotFoundException {
 		File altFile = new File(fileName);
 		BufferedOutputStream bufStream = new BufferedOutputStream(new FileOutputStream(altFile));
 		return bufStream;
 	}
 
 	private void writeDocument(BufferedOutputStream bufStream, Document doc)
 			throws TransformerFactoryConfigurationError,
 			TransformerConfigurationException, TransformerException {
 		TransformerFactory xformFactory = TransformerFactory.newInstance();  
 		Transformer idTransform = xformFactory.newTransformer();
 		Source input = new DOMSource(doc);
 		Result output = new StreamResult(bufStream);
 		idTransform.transform(input, output);		
 	}
 
 	private Document createDocument() throws ParserConfigurationException {
 		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
 		DocumentBuilder builder = factory.newDocumentBuilder();
 		DOMImplementation impl = builder.getDOMImplementation();
 		Document doc = impl.createDocument("http://www.decision-deck.org/2009/XMCDA-2.0.0", "xmcda:XMCDA", null);
 		return doc;
 	}
 
 	private void compute() {
 		SMAASimulator simul = new SMAASimulator(model, new SMAA2SimulationThread(model, ITERATIONS));
 		simul.restart();
 		while (simul.isRunning()) {
 			try {
 				Thread.sleep(100);
 			} catch (InterruptedException e) {
 				e.printStackTrace();
 			}
 		}
 		results = (SMAA2Results) simul.getResults();
 	}
 
 	private void constructModel() {
 		model = new SMAAModel("smaa-2 model");
 		model.setAlternatives(alts);
 		model.setCriteria(crit);
 		for (Criterion c : crit) {
 			for (Alternative a : alts) {
 				model.setMeasurement((CardinalCriterion) c, a, 
 						impactMatrix.getMeasurement((CardinalCriterion) c, a));
 			}
 		}
 	}
 
 	private void loadInputFiles(String inputDir) throws ParserConfigurationException, SAXException, IOException, Exception {
 		alts = loadAlternatives(inputDir);
 		crit = loadCriteria(inputDir);
 		impactMatrix = loadImpactMatrix(alts, crit, inputDir);
 	}
 
 	private static ImpactMatrix loadImpactMatrix(List<Alternative> alts,
 			List<Criterion> crit, String inputDir) throws ParserConfigurationException, SAXException, IOException, Exception {
 		File altFile = new File(inputDir + File.separator + IMPACTFILE);
 		BufferedInputStream bufStream = new BufferedInputStream(new FileInputStream(altFile));
 
 		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
 		DocumentBuilder db = dbf.newDocumentBuilder();
 		Document dom = db.parse(bufStream);
 		
 		NodeList list = dom.getElementsByTagName("alternativePerformances");
 		
 		ImpactMatrix mat = new ImpactMatrix(alts, crit);
 		
 		for(int i=0;i<list.getLength();i++){
 			Element ele = (Element)list.item(i);
 			String altName =
 				ele.getElementsByTagName("alternativeID").item(0).getChildNodes().item(0).getNodeValue();
 			Alternative alt = findAlternative(alts, altName);
 			
 			NodeList critNode = ele.getElementsByTagName("performance");
 			for (int j=0;j<critNode.getLength();j++) {
 				Element perfElem = (Element) critNode.item(j);
 				String critName =
 					perfElem.getElementsByTagName("criterionID").item(0).getChildNodes().item(0).getNodeValue();
 				CardinalCriterion c = (CardinalCriterion) findCriterion(crit, critName);
 				
 				Element valueElem = (Element)perfElem.getElementsByTagName("value").item(0);
 				
 				NodeList ivalElems = valueElem.getElementsByTagName("interval");
 				Interval ival = null;
 				if (ivalElems.getLength() == 0) {
 					// real
 					String realElem = valueElem.getElementsByTagName("real").item(0).getChildNodes().item(0).getNodeValue();
 					Double val = Double.parseDouble(realElem);
 					ival = new Interval(val, val);
 				} else {
 					Element lbValueElem = (Element) valueElem.getElementsByTagName("lowerBound").item(0);
 					String lbound = lbValueElem.getElementsByTagName("real").item(0).getChildNodes().item(0).getNodeValue();
 					Double dlb = Double.parseDouble(lbound);
 					
 					Element ubValueElem = (Element) valueElem.getElementsByTagName("upperBound").item(0);
 					String ubound = ubValueElem.getElementsByTagName("real").item(0).getChildNodes().item(0).getNodeValue();
 					Double dub = Double.parseDouble(ubound);
 
 					ival = new Interval(dlb, dub);
 				}
 				mat.setMeasurement(c, alt, ival);
 			}
 		}
 		
 		for (Alternative a : alts) {
 			for (Criterion c : crit) {
 				if (mat.getMeasurement((CardinalCriterion) c, a) == null) {
 					throw new Exception("Missing measurement for alternative " + a + " on criterion " + c);
 				}
 			}
 		}
 		
 		bufStream.close();
 		return mat;
 	}
 
 	private static Criterion findCriterion(List<Criterion> crit, String critName) throws Exception {
 		for (Criterion c :crit) {
 			if (c.getName().equals(critName)) {
 				return c;
 			}
 		}
		throw new Exception("Unknown criterion in performance table");
 	}
 
 	private static Alternative findAlternative(List<Alternative> alts, String altName) throws Exception {
 		for (Alternative a : alts) {
 			if (a.getName().equals(altName)) {
 				return a;
 			}
 		}
		System.out.println(alts + " find " + altName);
		throw new Exception("Unknown alternative in performance table");
 	}
 
 	private static List<Alternative> loadAlternatives(String inputDir)
 			throws FileNotFoundException, ParserConfigurationException,
 			SAXException, IOException {
 		File altFile = new File(inputDir + File.separator + ALTFILE);
 		BufferedInputStream bufStream = new BufferedInputStream(new FileInputStream(altFile));
 
 		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
 		DocumentBuilder db = dbf.newDocumentBuilder();
 		Document dom = db.parse(bufStream);
 		
 		NodeList list = dom.getElementsByTagName("alternative");
 		List<Alternative> alts = new ArrayList<Alternative>();
 		for(int i=0;i<list.getLength();i++){ 
 			Node item = list.item(i);
 			String altName = item.getAttributes().getNamedItem("id").getNodeValue();			
 			Alternative alt = new Alternative(altName);
 			alts.add(alt);
 		}
 		bufStream.close();
 		return alts;
 	}
 	
 	private static List<Criterion> loadCriteria(String inputDir)
 	throws FileNotFoundException, ParserConfigurationException,
 	SAXException, IOException {
 		File altFile = new File(inputDir + File.separator + CRITFILE);
 		BufferedInputStream bufStream = new BufferedInputStream(new FileInputStream(altFile));
 
 		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
 		DocumentBuilder db = dbf.newDocumentBuilder();
 		Document dom = db.parse(bufStream);
 
 		NodeList list = dom.getElementsByTagName("criterion");
 		List<Criterion> crits = new ArrayList<Criterion>();
 		for(int i=0;i<list.getLength();i++){ 
 			Node item = list.item(i);
 			String critName = item.getAttributes().getNamedItem("id").getNodeValue();
 			CardinalCriterion crit = new ScaleCriterion(critName, true);
 			crits.add(crit);
 		}
 		bufStream.close();
 		return crits;
 	}	
 }
