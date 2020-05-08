 package machine.turing;
 import java.io.File;
 import java.io.IOException;
 import java.util.ArrayList;
 
 import javax.xml.parsers.DocumentBuilder;
 import javax.xml.parsers.DocumentBuilderFactory;
 import javax.xml.parsers.ParserConfigurationException;
 import javax.xml.transform.OutputKeys;
 import javax.xml.transform.Transformer;
 import javax.xml.transform.TransformerConfigurationException;
 import javax.xml.transform.TransformerException;
 import javax.xml.transform.TransformerFactory;
 import javax.xml.transform.dom.DOMSource;
 import javax.xml.transform.stream.StreamResult;
 
 import machine.Machine;
 
 import org.w3c.dom.Attr;
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 import org.w3c.dom.Node;
 import org.w3c.dom.NodeList;
 
 import Tape.ConsoleTape;
 import Tape.LEGOTape;
 import Tape.MasterRobot;
 import Tape.SlaveRobot;
 import Tape.Tape;
 import Tape.TapeException;
 
 /** This class represents a Turing machine with it's states, edges and transitions
  * 
  * @author David Wille
  * 
  */
 public class TuringMachine extends Machine{
 	protected ArrayList<State> states;
 	protected ArrayList<Edge> edges;
 
 	/**
 	 * Constructs an untitled Turing machine
 	 */
 	public TuringMachine() {
 		this("Untitled Turing machine");
 	}
 
 	/**
 	 * Constructs an empty Turing machine with a name
 	 * @param name Name for the Turing machine
 	 */
 	public TuringMachine(String name) {
 		this(name, new ArrayList<State>(), new ArrayList<Edge>(), new ArrayList<Tape>());
 	}
 
 	/**
 	 * Constructs a new Turing machine with edges, states and transitions
 	 * @param states All states of the Turing machine
 	 * @param edges All edges of the Turing machine
 	 * @param name The name of the Turing machine
 	 * @param tapes The number of tapes used in the Turing machine
 	 */
 	public TuringMachine(String name, ArrayList<State> states, ArrayList<Edge> edges, ArrayList<Tape> tapes) {
 		super(name);
 		this.states = states;
 		this.edges = edges;
 		this.tapes = tapes;
 	}
 
 	/**
 	 * Returns the Turing machine's states
 	 * @return Turing Machine's states
 	 */
 	public ArrayList<State> getStates() {
 		return this.states;
 	}
 
 	/**
 	 * Returns the Turing machine's edges
 	 * @return Turing Machine's edges
 	 */
 	public ArrayList<Edge> getEdges() {
 		return this.edges;
 	}
 
 	/**
 	 * Reads a Turing machine from a XML file
 	 * @param filename File to read the Turing machine from (with or without .xml at the end)
 	 * @return Turing machine object
 	 */
 	public static TuringMachine load(String filename) throws IOException {
 		if (!filename.endsWith(".xml")) {
 			throw new IOException("Wrong file extension of file '" + filename + "'. Must be '.xml'");
 		}
 
 		//parse document
 		System.out.println("Loading file '" + filename + "'...");
 		File file = new File(filename);
 		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
 		Document doc = null;
 		try {
 			DocumentBuilder db = dbf.newDocumentBuilder();
 			doc = db.parse(file);
 		}
 		catch (Exception e) {
 			IOException ioex = new IOException("Parsing XML failed. Check the syntax of the file '" + filename + "'.");
 			ioex.initCause(e);
 			throw ioex;
 		}
 		doc.getDocumentElement().normalize();
 
 		// read Turing machine's name
 		String machineName = doc.getDocumentElement().getAttribute("name");
 		String machineXMLVersion = doc.getDocumentElement().getAttribute("xml-version");
 
 		if (!machineXMLVersion.equals("2")) {
 			throw new IOException("The file format version of the file '" + filename 
 					+ "' is not supported by this program. Please convert the file before opening.");
 		}
 
 		//load the rest
 		TuringMachine machine = new TuringMachine(machineName);
 		System.out.println("Loading tape configuration...");
 		machine.loadTapesConfig(doc);
 		System.out.println("Loading states...");
 		machine.loadStates(doc);
 		System.out.println("Loading edges and transitions...");
 		machine.loadEdges(doc);
 		System.out.println("File '" + filename + "' successfully loaded.");
 
 
 		return machine;
 	}
 
 	/**
 	 * Loads the tapes' configuration from a xml Document 
 	 * @param doc Document to load the configuration from
 	 * @throws IOException Exception if any problems occur while reading the configuration
 	 */
 	private void loadTapesConfig(Document doc) throws IOException{
 		//Iterate through a list of tape nodes
 		NodeList tapeList = doc.getElementsByTagName("tape");
 		for (int i = 0; i < tapeList.getLength(); i++) {
 			Node tapeNode = tapeList.item(i);
 
 			if (tapeNode.getNodeType() != Node.ELEMENT_NODE) {
 				break; //ignore attributes etc.
 			}
 			Element tapeElement = (Element) tapeNode;
 
 			//Get tape type and name
 			String tapeType = tapeElement.getAttribute("type");
 			String tapeName = InOut.getTagValue("name", tapeElement);
 
 			//Get input word
 			String inputWord = "";
 
 			Element inputElement = InOut.getChildElement("input", tapeElement);
 
 			NodeList inputSymbolList = inputElement.getElementsByTagName("symbol");
 			for (int k = 0; k < inputSymbolList.getLength(); k++) {
 				Node inputSymbolNode = inputSymbolList.item(k);
 				if (inputSymbolNode.getNodeType() == Node.ELEMENT_NODE) {
 					String symbolString = inputSymbolNode.getTextContent();
 					if (inputSymbolNode.getTextContent().length() != 1) {
 						throw new IOException("Expected exactly one character per symbol in input word of tape '" 
 								+ tapeName + "' but found the string '" + symbolString + "' with a length of "
 								+ symbolString.length() + " instead.");
 					}
 					inputWord += inputSymbolNode.getTextContent().charAt(0);
 				}
 			}
 
 			//create the tape
 			Tape tape = null;
 			if (tapeType.equals("LEGO")) {
 				String masterName = InOut.getTagValue("master", tapeElement);
 				String masterMacAddress = InOut.getChildElement("master", tapeElement).getAttribute("mac-address");
 				String slaveName = InOut.getTagValue("slave", tapeElement);
 				String slaveMacAddress = InOut.getChildElement("slave", tapeElement).getAttribute("mac-address");
 
 				MasterRobot master = new MasterRobot(masterName, masterMacAddress);
 				SlaveRobot slave = new SlaveRobot(slaveName, slaveMacAddress);
 
 				tape = new LEGOTape(master, slave);
 			}
 			else if (tapeType.equals("console")) {
 				tape = new ConsoleTape();
 			}
 			else if (tapeType.equals("gui")) {
 				throw new IOException("GUI tape not implemented yet.");
 			}
 			else {
 				throw new IOException("Unsupported tape type '" + tapeType + "' for tape ' " + tapeName + "'.");
 			}
 
 			tape.setName(tapeName);
 			tape.setInputWord(inputWord);
 			this.tapes.add(tape);
 			System.out.println(tape); //TODO: remove debug output
 		} //end for (next tape)
 
 		//Check if tape count attribute matches the number of tapes
 		String machineTapesCountString = doc.getDocumentElement().getAttribute("tapes");
 		int machineTapesCount = Integer.parseInt(machineTapesCountString);
 
 		if (machineTapesCount != this.tapes.size()) {
 			throw new IOException("The tapes count attribute of the machine did not match the number of tapes actually defined (attribute is " 
 					+ machineTapesCountString + " but " + this.tapes.size() + " tapes were defined).");
 		}
 	}
 
 	/**
 	 * Loads the states from an xml Document
 	 * @param doc The document where to get the states
 	 * @throws IOException Exception if any problem occurs while reading the states
 	 */
 	private void loadStates(Document doc) throws IOException {
 		// get list of nodes
 		NodeList stateList = doc.getElementsByTagName("state");
 		for (int i = 0; i < stateList.getLength(); i++) {
 			Node currentNode = stateList.item(i);
 
 			if (currentNode.getNodeType() == Node.ELEMENT_NODE) {
 				Element currentElement = (Element) currentNode;
 				String id = currentElement.getAttribute("id");
 				if (this.getStateById(id) != null) {
 					throw new IOException("State ID '" + id + "' already exists! Please check your xml file!");
 				}
 
 				State.Type type = null;
 				String typeString = currentElement.getAttribute("type");
 				if (typeString.equals("start")) {
 					type = State.Type.START;
 				}
 				else if (typeString.equals("normal")) {
 					type = State.Type.NORMAL;
 				}
 				else if (typeString.equals("final")) {
 					type = State.Type.FINAL;
 				}
 				else {
 					throw new IOException("Unsupported state type '" + typeString + "' for the state with id " + id + ".  Expected 'start', 'final' or 'normal'.");
 				}
 				String name = InOut.getTagValue("name", currentElement);
 				State state = new State(id, name, type);
 				states.add(state);
 				System.out.println(" " + state); //TODO: remove debug output
 			}
 		} //end for (next state)
 	}
 
 	/**
 	 * Loads the edges from an xml Document
 	 * @param doc The document where to get the edges
 	 * @throws IOException Exception if any problem occurs while reading the edges
 	 */
 	private void loadEdges(Document doc) throws IOException {
 		// get list of edges
 		NodeList edgeList = doc.getElementsByTagName("edge");
 		for (int i = 0; i < edgeList.getLength(); i++) {
 			Node edgeNode = edgeList.item(i);
 
 			if (edgeNode.getNodeType() == Node.ELEMENT_NODE) {
 				Element edgeElement = (Element) edgeNode;
 				// get from and to
 				String fromId = edgeElement.getAttribute("from");
 				String toId = edgeElement.getAttribute("to");
 				State from = this.getStateById(fromId);
 				State to = this.getStateById(toId);
 
 				if (from == null) {
 					throw new IOException("Invalid edge starting point: No such state with ID '" + fromId + "'.");
 				}
 				if (to == null) {
 					throw new IOException("Invalid edge end point: No such state with ID '" + fromId + "'.");
 				}
 
 				// get transitions
 				NodeList transitionList = edgeElement.getElementsByTagName("transition");
 				ArrayList<Transition> transitions = new ArrayList<Transition>();
 				for (int j = 0; j < transitionList.getLength(); j++) {
 					Node transitionNode = transitionList.item(j);
 					if (transitionNode.getNodeType() == Node.ELEMENT_NODE) {
 						Element transitionElement = (Element) transitionNode;
 						Transition transition = this.loadTransition(transitionElement);
 						transitions.add(transition);
 					}
 				}
 
 				Edge edge = new Edge(from, to, transitions);
 				edges.add(edge);
 				System.out.println(" " + edge); //TODO: remove debug output
 
 				// write edges that start at a state
 				for (int j = 0; j < states.size(); j++) { //TODO: review
 					ArrayList<Edge> tempStartEdges = new ArrayList<Edge>();
 					for (int k = 0; k < edges.size(); k++) {
 						State tempStateFrom = edges.get(k).getFrom(); 
 						if (tempStateFrom.getId().equals(states.get(j).getId())) {
 							tempStartEdges.add(edges.get(k));
 						}
 					}
 					states.get(j).setEdge(tempStartEdges);
 				}
 			}
 		} //end for (next edge)
 
 		//check transition ids integrity
 		for (Edge edge : this.edges) {
 			for(Transition transition : edge.getTransitions()) {
 				if (this.getTransitionById(transition.getId()) != transition) {
 					throw new IOException("Transition ID '" + transition.getId() + "' is used more than once! Please check your xml file!");
 				}
 			}
 		}
 	}
 
 	private Transition loadTransition(Element transitionElement) throws IOException {
 		String id = transitionElement.getAttribute("id");
 
 		ArrayList<Character> read = this.loadSymbolList("read", "symbol", transitionElement);
 		ArrayList<Character> write = this.loadSymbolList("write", "symbol", transitionElement);
 		ArrayList<Character> action = this.loadSymbolList("action", "direction", transitionElement);
 
 		//check number of symbols
 		if (read.size() != this.getNumberOfTapes()) {
 			throw new IOException("Expected " + this.getNumberOfTapes() + " read symbols for Transition ID '" + id + "', but found " + read.size() + ". Make sure that the number of symbols matches the number of tapes.");
 		}
 		if (write.size() != this.getNumberOfTapes()) {
 			throw new IOException("Expected " + this.getNumberOfTapes() + " write symbols for Transition ID '" + id + "', but found " + read.size() + ". Make sure that the number of symbols matches the number of tapes.");
 		}
 		if (action.size() != this.getNumberOfTapes()) {
 			throw new IOException("Expected " + this.getNumberOfTapes() + " action symbols for Transition ID '" + id + "', but found " + read.size() + ". Make sure that the number of symbols matches the number of tapes.");
 		}
 		
 		// check actions
 		for (Character c : action) {
 			if (!(c=='L' || c=='N' || c=='R')) {
 				throw new IOException("Unsupported action '" + c + "'. Must be 'L', 'N' or 'R' (Transition ID '" + id + ")");
 			}
 		}
 
 		return new Transition(id, read, write, action);
 	}
 
 	private ArrayList<Character> loadSymbolList(String tag, String symboltag, Element transitionElement) throws IOException {
 		String id = transitionElement.getAttribute("id");
 		ArrayList<Character> symbols = new ArrayList<Character>();
 
 		Element tagElement = InOut.getChildElement(tag, transitionElement);
 		NodeList symbolList = tagElement.getChildNodes();
 		for (int i = 0; i < symbolList.getLength(); i++) {
 			Node symbolNode = symbolList.item(i);
 			if (symbolNode.getNodeType() == Node.ELEMENT_NODE && symbolNode.getNodeName().equals(symboltag)) {
 				String symbolString = symbolNode.getTextContent();
 				if (symbolString.length() != 1) {
 					throw new IOException("Expected exactly one character per symbol in the '" 
 							+ tag + "' section of transition with ID '" + id + "' but found the string '" 
 							+ symbolString + "' with a length of " + symbolString.length() + " instead.");
 				}
 				symbols.add(symbolString.charAt(0));
 			}
 		}
 		return symbols;
 	}
 
 	/**
 	 * Writes the Turing machine to a XML file with a given name
	 * @param filename File writing to (with or without .xml at the end)
 	 */
 	public void save(String filename) throws IOException {
 		System.out.println("Saving file '" + filename + "'...");
 
 		if (!filename.endsWith(".xml")) {
 			throw new IOException("Wrong file extension of file '" + filename + "'. Must be '.xml'");
 		}
 
 		TransformerFactory transformerFactory = TransformerFactory.newInstance();
 
 		Transformer transformer = null;
 		DocumentBuilder docBuilder = null;
 		try {
 			transformer = transformerFactory.newTransformer();
 			transformer.setOutputProperty(OutputKeys.ENCODING, "utf-8");
 			transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no");
 			transformer.setOutputProperty(OutputKeys.INDENT, "yes");
 			transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");
 
 			DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
 			docBuilder = docFactory.newDocumentBuilder();
 		} catch (TransformerConfigurationException e) {
 			IOException ex = new IOException("Cannot write XML files. Serious configuration error.");
 			ex.initCause(e);
 		} catch (ParserConfigurationException e) {
 			IOException ex = new IOException("Cannot write XML files. Serious configuration error.");
 			ex.initCause(e);
 		}
 
 		Document doc = docBuilder.newDocument();
 		// create root element
 		Element rootElement = doc.createElement("machine");
 		doc.appendChild(rootElement);
 
 		// save name of machine
 		Attr attrName = doc.createAttribute("name");
 		attrName.setValue(this.getName());
 		rootElement.setAttributeNode(attrName);
 
 		// save number tapes of machine
 		Attr attrTape = doc.createAttribute("tapes");
 		attrTape.setValue(String.valueOf(this.getTapes().size()));
 		rootElement.setAttributeNode(attrTape);
 		
 		// save xml version number
 		Attr attrXMLVersion = doc.createAttribute("xml-version");
 		attrXMLVersion.setValue("2");
 		rootElement.setAttributeNode(attrXMLVersion);
 		
 		//save the rest
 		System.out.println("Saving tape configuration...");
 		this.saveTapesConfig(doc, rootElement);
 		System.out.println("Saving states...");
 		this.saveStates(doc, rootElement);
 		System.out.println("Saving edges and transitions...");
 		this.saveEdges(doc, rootElement);
 		System.out.println("Saving to file...");
 		StreamResult result = new StreamResult(new File(filename));
 		DOMSource source = new DOMSource(doc);
 		try {
 			transformer.transform(source, result);
 		} catch (TransformerException e) {
 			IOException ioex = new IOException("Error while writing the file '" + filename + "' to disk.");
 			ioex.initCause(e);
 			throw ioex;
 		}
 		System.out.println("File '" + filename + "' successfully saved.");
 	}
 	
 	private void saveTapesConfig(Document doc, Element rootElement) {
 		for (Tape tape : this.tapes) {
 			// tape element
 			Element tapeElement = doc.createElement("tape");
 			rootElement.appendChild(tapeElement);
 			
 			// save tape type number
 			Attr attrType = doc.createAttribute("type");
 			String tapeType = "";
 			if (tape.getType() == Tape.Type.LEGO) {
 				tapeType = "LEGO";
 			}
 			else if (tape.getType() == Tape.Type.CONSOLE) {
 				tapeType = "console";
 			}
 			else if (tape.getType() == Tape.Type.GUI) {
 				tapeType = "gui";
 			}
 			attrType.setValue(tapeType);
 			tapeElement.setAttributeNode(attrType);
 			
 			//save tape name
 			Element nameElement = doc.createElement("name");
 			nameElement.appendChild(doc.createTextNode(tape.getName()));
 			tapeElement.appendChild(nameElement);
 						
 			if (tape.getType().equals("LEGO")) {
 				LEGOTape lego_tape = (LEGOTape) tape;
 				
 				//save master robot name and mac address
 				Element masterElement = doc.createElement("master");
 				Attr attrMasterMac = doc.createAttribute("mac-address");
 				attrMasterMac.setValue(lego_tape.getMaster().getMacAddress());
 				masterElement.setAttributeNode(attrMasterMac);
 				masterElement.appendChild(doc.createTextNode(lego_tape.getMaster().getName()));
 				tapeElement.appendChild(masterElement);
 				
 				//save slave robot name and mac address
 				Element slaveElement = doc.createElement("slave");
 				Attr attrSlaveMac = doc.createAttribute("mac-address");
 				attrSlaveMac.setValue(lego_tape.getSlave().getMacAddress());
 				slaveElement.setAttributeNode(attrSlaveMac);
 				slaveElement.appendChild(doc.createTextNode(lego_tape.getSlave().getName()));
 				tapeElement.appendChild(slaveElement);
 			}
 			
 			// save input word
 			Element inputElement = doc.createElement("input");
 			tapeElement.appendChild(inputElement);
 
 			for (char symbol : tape.getInputWord().toCharArray()) {
 				Element symbolElement = doc.createElement("symbol");
 				symbolElement.appendChild(doc.createTextNode("" + symbol));
 				inputElement.appendChild(symbolElement);
 			}
 		}
 	}
 	
 	private void saveStates(Document doc, Element rootElement) {
 		for(State state : this.states) {
 			// state element
 			Element stateElement = doc.createElement("state");
 			rootElement.appendChild(stateElement);
 
 			// save id of state
 			Attr attrStateId = doc.createAttribute("id");
 			attrStateId.setValue(state.getId());
 			stateElement.setAttributeNode(attrStateId);
 
 			// save type of state
 			Attr attrType = doc.createAttribute("type");
 			String type;
 			switch(state.getType()) {
 			case START:
 				type = "start";
 				break;
 			case NORMAL:
 				type = "normal";
 				break;
 			case FINAL:
 				type = "final";
 				break;
 			default:
 				type = "normal";
 				break;
 			}
 			attrType.setValue(type);
 			stateElement.setAttributeNode(attrType);
 
 			// state name element
 			Element nameElement = doc.createElement("name");
 			nameElement.appendChild(doc.createTextNode(state.getName()));
 			stateElement.appendChild(nameElement);
 		}
 	}
 	
 	private void saveEdges(Document doc, Element rootElement) {
 		for (Edge edge : this.getEdges()) {
 			Element edgeElement = doc.createElement("edge");
 			rootElement.appendChild(edgeElement);
 			
 			// save from of edge
 			Attr attrEdgeFrom = doc.createAttribute("from");
 			attrEdgeFrom.setValue(edge.getFrom().getId());
 			edgeElement.setAttributeNode(attrEdgeFrom);
 			
 			// save to of edge
 			Attr attrEdgeTo = doc.createAttribute("to");
 			attrEdgeTo.setValue(edge.getTo().getId());
 			edgeElement.setAttributeNode(attrEdgeTo);
 			
 			for (Transition transition : edge.getTransitions()) {
 				this.saveTransition(transition, doc, edgeElement);
 			}
 		}
 	}
 	
 	private void saveTransition(Transition transition, Document doc, Element edgeElement) {
 		// transition element
 		Element transitionElement = doc.createElement("transition");
 		edgeElement.appendChild(transitionElement);
 
 		// save id of transition
 		Attr attrTransitionId = doc.createAttribute("id");
 		attrTransitionId.setValue(transition.getId());
 		transitionElement.setAttributeNode(attrTransitionId);
 
 		// edge read elements
 		ArrayList<Character> read = transition.getRead();
 		Element readElement = doc.createElement("read");
 		transitionElement.appendChild(readElement);
 		for (int k = 0; k < read.size(); k++) {
 			Element symbolElement = doc.createElement("symbol");
 			symbolElement.appendChild(doc.createTextNode("" + read.get(k)));
 			readElement.appendChild(symbolElement);
 		}
 
 		// edge write elements
 		ArrayList<Character> write = transition.getWrite();
 		Element writeElement = doc.createElement("write");
 		transitionElement.appendChild(writeElement);
 		for (int k = 0; k < write.size(); k++) {
 			Element symbolElement = doc.createElement("symbol");
 			symbolElement.appendChild(doc.createTextNode("" + write.get(k)));
 			writeElement.appendChild(symbolElement);
 		}
 
 		// edge action elements
 		ArrayList<Character> action = transition.getAction();
 		Element actionElement = doc.createElement("action");
 		transitionElement.appendChild(actionElement);
 		for (int k = 0; k < action.size(); k++) {
 			Element symbolElement = doc.createElement("direction");
 			symbolElement.appendChild(doc.createTextNode("" + action.get(k)));
 			actionElement.appendChild(symbolElement);
 		}
 	}
 
 	/**
 	 * Gets a state of the TuringMachine with a given ID
 	 * @param id Id to be searched
 	 * @return The State with the given ID, or null if the state doesn't exist
 	 */
 	public State getStateById(String id) {
 		for (State state : this.states) {
 			if (state.getId().equals(id)) {
 				return state;
 			}
 		}
 		return null;
 	}
 
 	/**
 	 * Gets a transition of the TuringMachine with a given ID
 	 * @param id Id to be searched
 	 * @return The Transition with the given ID, or null if the state doesn't exist
 	 */
 	public Transition getTransitionById(String id) {
 		for (Edge edge : this.edges) {
 			for (Transition transition : edge.transitions) {
 				if (transition.getId().equals(id)) {
 					return transition;
 				}
 			}
 		}
 		return null;
 	}
 
 	/**
 	 * Gives a string representation of the Turing Machine
 	 */
 	@Override
 	public String toString() {
 		String str = "";
 		str += "TM name: " + this.getName() + ", ";
 		str += "Tapes count:" + this.getTapes() + "\n";
 		str += "Tapes: " + tapes + "\n";
 		str += "States: " + states + "\n";
 		str += "Edges:\n" + edges + "\n";
 		return str;
 	}
 
 }
