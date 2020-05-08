 /*
  * DataTypeParser.java
  *
  * Created on 1. August 2006, 13:31
  *
  * http://www.w3.org/TR/owl-semantics/direct.html#description-interpretations
  *
  * Copyright (C) 2007
  * German Research Center for Artificial Intelligence (DFKI GmbH) Saarbruecken
  * Hochschule fuer Technik und Wirtschaft (HTW) des Saarlandes
  * Developed by Oliver Fourman, Ingo Zinnikus, Matthias Klusch
  *
  * The code is free for non-commercial use only.
  * You can redistribute it and/or modify it under the terms
  * of the Mozilla Public License version 1.1  as
  * published by the Mozilla Foundation at
  * http://www.mozilla.org/MPL/MPL-1.1.txt
  */
 
 package de.dfki.dmas.owls2wsdl.parser;
 
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.net.MalformedURLException;
 import java.net.URI;
 import java.net.URISyntaxException;
 import java.net.URL;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Iterator;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Vector;
 
 import org.mindswap.pellet.jena.PelletReasonerFactory;
 
 import com.hp.hpl.jena.datatypes.RDFDatatype;
 import com.hp.hpl.jena.ontology.ConversionException;
 import com.hp.hpl.jena.ontology.DataRange;
 import com.hp.hpl.jena.ontology.DatatypeProperty;
 import com.hp.hpl.jena.ontology.FunctionalProperty;
 import com.hp.hpl.jena.ontology.Individual;
 import com.hp.hpl.jena.ontology.ObjectProperty;
 import com.hp.hpl.jena.ontology.OntClass;
 import com.hp.hpl.jena.ontology.OntModel;
 import com.hp.hpl.jena.ontology.OntModelSpec;
 import com.hp.hpl.jena.ontology.OntProperty;
 import com.hp.hpl.jena.ontology.OntResource;
 import com.hp.hpl.jena.ontology.Restriction;
 import com.hp.hpl.jena.rdf.model.Literal;
 import com.hp.hpl.jena.rdf.model.ModelFactory;
 import com.hp.hpl.jena.rdf.model.Property;
 import com.hp.hpl.jena.rdf.model.RDFList;
 import com.hp.hpl.jena.rdf.model.RDFNode;
 import com.hp.hpl.jena.rdf.model.Resource;
 import com.hp.hpl.jena.rdf.model.SimpleSelector;
 import com.hp.hpl.jena.rdf.model.Statement;
 import com.hp.hpl.jena.util.iterator.ExtendedIterator;
 import com.hp.hpl.jena.util.iterator.Filter;
 import com.hp.hpl.jena.vocabulary.OWL;
 import com.hp.hpl.jena.vocabulary.RDFS;
 
 import de.dfki.dmas.owls2wsdl.core.OWLHelper;
 import de.dfki.dmas.owls2wsdl.core.OWLIndividualTypeFilter;
 //http://svn.mindswap.org/pellet/branches/pellet-explanation/examples/org/mindswap/pellet/examples/CompareReasoners.java
 //import org.mindswap.pellet.jena.OWLReasoner;
 //import org.mindswap.pellet.owlapi.Reasoner;
 //import org.mindswap.owl.OWLOntology;
 
 /**
  * Class to to extract datatype information.<br />
  * Operates via the Jena framework on the ontology data.
  * 
  * @version beta (prototype)
  * @author Oliver Fourman
  */
 public class DatatypeParser {
 	public static String OWL_NS = "http://www.w3.org/2002/07/owl#";
 
 	// depending which reasoning method is choosen we must supress internal
 	// classes of following namespaces!
 	private static String[] OWLNS = { "http://www.w3.org/2001/XMLSchema#",
 			"http://www.w3.org/1999/02/22-rdf-syntax-ns#",
 			"http://www.w3.org/2000/01/rdf-schema#",
 			"http://www.w3.org/2002/07/owl#" };
 
 	public String status;
 	private Vector<URI> pathList; // je nachdem ob im Konstruktor file oder dir
 
 	private OntModel dlModel;
 	private OntModel pelletModel;
 	private OntModel rdfsInfModel;
 
 	private OntClassKB ontclassKB;
 
 	// No declaring class properties
 	private ArrayList<DatatypeProperty> domainlessProperties;
 	// private HashMap collectedObjectProperties;
 
 	/**
 	 * filters all special OntClass types.
 	 */
 	private Filter<OntClass> ontClassSpecialsFilter;
 
 	// /**
 	// * Explicit parse these additional models.
 	// * Reason: some referencing problems during processing of enumerations
 	// with
 	// * external defined classes.
 	// */
 	// Vector modelList = new Vector();
 
 	private static OWLHelper owlHelper = new OWLHelper();
 
 	/** Creates a new instance of DataTypeParser */
 	public DatatypeParser() {
 		System.out.println("[C] DatatypeParser");
 
 		// --- build model ---
 		// OWL_DL_MEM_RDFS_INF: + SuperClass information
 		// OWL_DL_MEM_RULE_INF: additional owl classes
 
 		this.dlModel = ModelFactory.createOntologyModel(
 				OntModelSpec.OWL_DL_MEM, null);
 		this.pelletModel = ModelFactory
 				.createOntologyModel(PelletReasonerFactory.THE_SPEC); // DL
 																		// Reasoning!
 		this.rdfsInfModel = ModelFactory.createOntologyModel(
 				OntModelSpec.OWL_DL_MEM_RDFS_INF, null);
 
 		this.pathList = new Vector<URI>();
 		this.ontclassKB = new OntClassKB(this.pelletModel);
 		this.domainlessProperties = new ArrayList<DatatypeProperty>();
 
 		this.ontClassSpecialsFilter = new Filter<OntClass>() {
 			public boolean accept(OntClass o) {
 				if (o.isIntersectionClass() || o.isUnionClass()
 						|| o.isEnumeratedClass() || o.isComplementClass()
 						|| o.isRestriction()) {
 					return true;
 				}
 				return false;
 			}
 		};
 	}
 
 	/**
 	 * Creates a new instance of DataTypeParser
 	 * 
 	 * @param String
 	 *            filesystem path to owl file
 	 */
 	public DatatypeParser(String path) {
 		this();
 		try {
 			URI curPath = new URI(path);
 			if (curPath.getScheme().equals("http")) {
 				this.pathList.add(curPath);
 			} else {
 				File file = new File(curPath);
 				if (file.isDirectory()) {
 					System.out.println("Path to ontology directory: " + path);
 					this.buildFileList(file);
 					// File[] entries = file.listFiles();
 					// this.pathList.addAll(Arrays.asList(entries));
 				} else {
 					System.out.println("Add " + curPath + " to filelist.");
 					this.pathList.add(file.toURI());
 				}
 			}
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 	}
 
 	/**
 	 * Method to build recursively a file list when a directory is choosen.
 	 * Fills the pathList variable with paths.
 	 */
 	private void buildFileList(File file) throws URISyntaxException,
 			FileNotFoundException, Exception {
 		File[] entries = file.listFiles();
 		for (int i = 0; i < entries.length; i++) {
 			if (entries[i].isFile()) {
 				if (((File) entries[i]).getName().toLowerCase()
 						.endsWith(".owl")) {
 					this.pathList.add(new URI("file://"
 							+ ((File) entries[i]).toURI()));
 				}
 			} else if (entries[i].isDirectory()) {
 				this.buildFileList((File) entries[i]);
 			} else {
 				throw new Exception("No file and no directory.");
 			}
 		}
 	}
 
 	public void listOwlFiles() {
 		Iterator<URI> it = this.pathList.iterator();
 		while (it.hasNext()) {
 			System.out.println(it.next());
 		}
 	}
 
 	/**
 	 * Iterates over pathList variable.
 	 */
 	public void parse() throws Exception {
 		Iterator<URI> it = this.pathList.iterator();
 		while (it.hasNext()) {
 			URL curl = it.next().toURL();
 			this.parse(curl.toString());
 		}
 	}
 
 	/**
 	 * Parse given file.
 	 * 
 	 * @param File
 	 *            file to parse
 	 */
 	public void parse(File f) throws Exception {
 		try {
 			URL url = f.toURI().toURL();
 			this.parse(url.toString());
 		} catch (MalformedURLException me) {
 			System.out.println("[e] Inputfile error: " + me.getMessage());
 		}
 
 	}
 
 	/**
 	 * Parse referenced ontology. Main method!
 	 * 
 	 * @param String
 	 *            referenced owl file. Referenced in filesystem or per url.
 	 */
 	public void parse(String path) throws Exception {
 		URL curl = new URL(path);
 		System.out.println("URL: " + curl.getProtocol() + " Host: "
 				+ curl.getHost() + " Path: " + curl.getPath());
 
 		if (curl.getProtocol().equals("http")
 				|| curl.getProtocol().equals("file")) {
 
 			dlModel.read(curl.toString());
 			pelletModel.read(curl.toString());
 			rdfsInfModel.read(curl.toString());
 
 			// // Test Pellet Reasoner with loaded Process Ontology! (implicit
 			// with Actor model)
 			// OntClass Participant =
 			// pelletModel.getOntClass("http://www.daml.org/services/owl-s/1.1/Process.owl#Participant");
 			// for(Iterator instances = Participant.listInstances();
 			// instances.hasNext(); ) {
 			// Individual ind = (Individual) instances.next();
 			// System.out.println("PELLET TEST Instance: "+ind.getURI());
 			// for (Iterator it = ind.listRDFTypes(true); it.hasNext(); ) {
 			// Resource cls = (Resource) it.next();
 			// System.out.println(
 			// "Individual "+ind.getLocalName()+" has rdf:type " + cls );
 			// }
 			// }
 
 			// search correct base path
 			for (Iterator<String> it = dlModel.getNsPrefixMap().keySet()
 					.iterator(); it.hasNext();) {
 				String key = it.next().toString();
 				if (key.equals("")) {
 					URL tempUrl = new URL(dlModel.getNsPrefixURI(key));
 					System.out.println("BASEPATH: " + tempUrl.getProtocol()
 							+ "://" + tempUrl.getHost() + tempUrl.getPath());
 					if (!path.equals(tempUrl.getProtocol() + "://"
 							+ tempUrl.getHost() + tempUrl.getPath())) {
 						curl = new URL(tempUrl.getProtocol() + "://"
 								+ tempUrl.getHost() + tempUrl.getPath()); // NEW
 					}
 				}
 			}
 
 			System.out.println("URL: " + curl.getProtocol() + " Host: "
 					+ curl.getHost() + " Path: " + curl.getPath());
 			this.ontclassKB.addOntologyURI(curl.toURI().toString());
 
 			Object[] importList = dlModel.listImportedOntologyURIs(true)
 					.toArray();
 			for (int i = 0; i < importList.length; i++) {
 				this.ontclassKB.addOntologyURI(importList[i].toString());
 			}
 
 			// // === explicit import from namespace to fullfill some
 			// dependencies (try..)
 			// String[] defaultKeys = {"xsd", "rdf", "rdfs", "dc", "daml",
 			// "owl", ""};
 			// Arrays.sort(defaultKeys);
 			// for(Iterator it=omodel.getNsPrefixMap().keySet().iterator();
 			// it.hasNext(); ) {
 			// String key = it.next().toString();
 			// if(Arrays.binarySearch(defaultKeys, key) < 0) {
 			// System.out.println("NS: "+key+" "+omodel.getNsPrefixURI(key));
 			// if(omodel.getNsPrefixURI(key).endsWith(".owl#")) {
 			// String ontName = omodel.getNsPrefixURI(key).split("#")[0];
 			// if(omodel.hasLoadedImport(ontName)) {
 			// System.out.println("NS: hasLoadedImport: "+ontName);
 			// }
 			// else {
 			// System.out.println("NS: IMPORT Ontology: "+ontName);
 			// omodel.read(ontName);
 			// }
 			// }
 			// }
 			// }
 
 			// // --- add enumerated type models to model ---
 			// for(ExtendedIterator extIt=omodel.listEnumeratedClasses();
 			// extIt.hasNext(); ) {
 			// EnumeratedClass enumClass = (EnumeratedClass)extIt.next();
 			// for(ExtendedIterator oneOfIt = enumClass.listOneOf();
 			// oneOfIt.hasNext(); ) {
 			// OntResource ontRes = (OntResource)oneOfIt.next();
 			// if(ontRes.isIndividual()) {
 			// if(!modelList.contains(ontRes.asIndividual().getRDFType().getNameSpace())
 			// &&
 			// !ontRes.asIndividual().getRDFType().getNameSpace().equals("http://www.w3.org/2002/07/owl#")
 			// )
 			// {
 			// System.out.println("ADD MODEL: "+ontRes.asIndividual().getRDFType().getNameSpace()+", "+ontRes.asIndividual().getURI()+" ("+ontRes.asIndividual().getRDFType().getURI()+")");
 			// modelList.add(ontRes.asIndividual().getRDFType().getNameSpace());
 			// //omodel.getOntology(ontRes.asIndividual().getRDFType().getNameSpace());
 			// }
 			// }
 			// }
 			// }
 			// System.out.println("COUNT: "+this.modelList.size());
 			// for(Iterator it=modelList.iterator(); it.hasNext(); ) {
 			// this.parse(it.next().toString());
 			// }
 
 			// // --- 3. Collect in model ---
 			this.collectDisjoints();
 			this.collectDatatypeProperties();
 			this.collectObjectProperties();
 			this.collectMissingInstances();
 			Arrays.sort(OWLNS); // to exclude internal classes
 			this.collectClassInformationAndRestrictions();
 			this.collectEnumerationTypes();
 
 			// --- 2. inf model (alternative) ---
 			/*
 			 * Das InfModel ist gegenueber deinem urspruenglichen Model ergaenzt
 			 * um diejenigen Triplets, welche vom Reasoner abgeleitet wurden
 			 * (z.B. subclass-Beziehungen, aber auch einiges andere, was owl-dl
 			 * bietet).
 			 */
 			// Reasoner pellet = new PelletReasoner();
 			// OntModelSpec spec = new OntModelSpec(OntModelSpec.OWL_DL_MEM);
 			// spec.setReasoner( pellet );
 			// //OntModel infModel = ModelFactory.createOntologyModel(
 			// PelletReasonerFactory.THE_SPEC );
 			// OntModel infModel = ModelFactory.createOntologyModel( spec,
 			// omodel.getBaseModel() );
 			//
 			// // OntClass lang =
 			// omodel.getOntClass("http://www.daml.org/2003/09/factbook/factbook-ont#Language");
 			// // for(ExtendedIterator eit = lang.listSubClasses(false);
 			// eit.hasNext(); ) {
 			// //
 			// System.out.println(">>> listSubClasses: "+((OntClass)eit.next()).getURI());
 			// // }
 			// // OntClass lang2 =
 			// infModel.getOntClass("http://www.daml.org/2003/09/factbook/factbook-ont#Language");
 			// // for(ExtendedIterator eit = lang2.listSubClasses(false);
 			// eit.hasNext(); ) {
 			// //
 			// System.out.println(">>> listSubClasses: "+((OntClass)eit.next()).getURI());
 			// // }
 			// // OntClass lang3 =
 			// infModel.getOntClass("http://127.0.0.1/ontology/concept.owl#SupportedLanguage");
 			// // for(ExtendedIterator eit = lang3.listSuperClasses(false);
 			// eit.hasNext(); ) {
 			// //
 			// System.out.println(">>> listSuperClasses: "+((OntClass)eit.next()).getURI());
 			// // }
 			// //
 			// // ValidityReport validity = infmodel.validate();
 			// // if (validity.isValid()) {
 			// // System.out.println("OK");
 			// // }
 			// // else {
 			// // System.out.println("Conflicts");
 			// // }
 			// // this.collectClassInformationAndRestrictions(infmodel);
 		} else {
 			System.err.println("[e] " + path + " not an URL.");
 		}
 	}
 
 	/**
 	 * Resets all collected data and knowledgebase.
 	 */
 	public void reset() {
 		// this.collectedObjectProperties.clear();
 		this.domainlessProperties.clear();
 		this.pathList.removeAllElements();
 		this.ontclassKB = new OntClassKB(this.pelletModel);
 	}
 
 	// private void collectProperties(OntModel omodel)
 	// {
 	// Reasoner pellet = new PelletReasoner();
 	// OntModelSpec spec = new OntModelSpec(OntModelSpec.OWL_DL_MEM_TRANS_INF);
 	// spec.setReasoner( pellet );
 	// //PelletOptions.USE_PSEUDO_MODEL = false;
 	// //spec.setReasonerFactory( PelletReasonerFactory.THE_SPEC );
 	// OntModel infModel = ModelFactory.createOntologyModel( spec,
 	// omodel.getBaseModel() );
 	//
 	// for(ExtendedIterator<OntClass> classIt=infModel.listNamedClasses();
 	// classIt.hasNext(); ) {
 	// OntClass c = classIt.next();
 	// System.out.println("OntClass: "+c.getURI());
 	//
 	// for(ExtendedIterator<OntProperty> propIt =
 	// c.listDeclaredProperties(false); propIt.hasNext(); ) {
 	// OntProperty prop = propIt.next();
 	// System.out.println(" PROP: "+prop.getURI());
 	// }
 	// }
 	// //
 	// System.out.println("=== DECLARED PROPERTIES PART =======================");
 	// // for(ExtendedIterator dp_eit = tempoc.listDeclaredProperties(true);
 	// dp_eit.hasNext();) {
 	// // OntProperty prop = (OntProperty)dp_eit.next();
 	// // String rangeURI =
 	// "n/a, probably FunctionalProperty defined in Restriction part";
 	// // if(prop.getRange()!=null) {
 	// // rangeURI = prop.getRange().getURI();
 	// // if(!this.collectedIndividuals.containsKey(rangeURI)) {
 	// // System.out.println("[i] collect individuals for "+rangeURI);
 	// // this.collectedIndividuals.put(rangeURI,
 	// this.collectIndividuals(omodel, rangeURI));
 	// // }
 	// // }
 	// //
 	// System.out.println(">> PROPERTY for "+tempoc.getLocalName()+": "+prop.toString()+" ("+rangeURI+")");
 	// // }
 	// }
 
 	/**
 	 * Grab defined disjoints out of ont model.
 	 * 
 	 * @throws com.hp.hpl.jena.shared.NotFoundException
 	 */
 	private void collectDisjoints()
 			throws com.hp.hpl.jena.shared.NotFoundException {
 		System.out.println("\ncollectDisjoints:");
 		//
 		// =1= collect disjoints by own statement selector
 		//
 		SimpleSelector disjoint_sel = new SimpleSelector(null, null,
 				(RDFNode) null) {
 			public boolean selects(Statement s) {
 				return s.getPredicate().toString().endsWith("disjoint"); // SELECT;
 			}
 		};
 		ExtendedIterator<Statement> eit_disjoints = this.rdfsInfModel
 				.listStatements(disjoint_sel);
 		while (eit_disjoints.hasNext()) {
 			Statement stmt = eit_disjoints.next();
 			System.out.println("STMT: " + stmt);
 			this.ontclassKB.putDisjoint(stmt.getSubject().getURI(), stmt
 					.getObject().asNode().getURI());
 		}
 
 		//
 		// =2= collect disjoints by ontology model
 		//
 		ExtendedIterator<OntClass> eit_classes = this.rdfsInfModel
 				.listClasses();
 		while (eit_classes.hasNext()) {
 			OntClass cur = eit_classes.next();
 			OntClass disjoint = cur.getDisjointWith();
 			if (disjoint != null) {
 				System.out.println("[i] check for disjoints: " + cur.getURI()
 						+ ", DISJOINT: " + disjoint.getURI());
 				if (!cur.getNameSpace().equals(
 						"http://www.w3.org/2001/XMLSchema#")) {
 					this.ontclassKB
 							.putDisjoint(cur.getURI(), disjoint.getURI());
 				}
 			}
 		}
 	}
 
 	// /**
 	// * @obsolete !?!?!?
 	// */
 	// private Vector<String> buildSuperClassList(OntModel omodel, String uri)
 	// {
 	// Vector<String> temp = new Vector<String>();
 	// System.out.println("[-> buildSuperClassList with "+uri);
 	// try {
 	// OntClass curoc = omodel.getOntClass(uri);
 	// if(curoc==null) {
 	// System.out.println("CLASS NOT IN MODEL: "+uri);
 	// }
 	//
 	// ExtendedIterator<OntClass> eit =
 	// curoc.listSuperClasses(false).filterDrop(this.ontClassSpecialsFilter);
 	// while(eit.hasNext()) {
 	// OntClass superclass = (OntClass)eit.next();
 	// String classURI = superclass.getURI();
 	// temp.add(classURI);
 	// temp.addAll(this.buildSuperClassList(omodel, classURI));
 	// }
 	// }
 	// catch(java.lang.NullPointerException npe) {
 	// //System.out.println("EXCEPTION (NullPointerException): "+npe.toString()+" URI:"+uri);
 	// this.ontclassKB.addError(uri, npe.getMessage());
 	// }
 	// catch(com.hp.hpl.jena.ontology.ConversionException e) {
 	// //System.out.println("EXCEPTION: "+e.toString());
 	// this.ontclassKB.addError(uri, e.getMessage());
 	// }
 	// return temp;
 	// }
 
 	// private HashSet getIntersectionProperties(OntModel omodel,
 	// HashSet<OntClassContainer> ontClassSet) {
 	// Vector<OntClassContainer> classList = new Vector<OntClassContainer>();
 	// HashSet<OntProperty> propertySet = new HashSet<OntProperty>();
 	//
 	// for(Iterator<OntClassContainer> it = ontClassSet.iterator();
 	// it.hasNext(); ) {
 	// classList.add(this.ontclassKB.get(it.next().toString()));
 	// }
 	// if(classList.size() > 1) {
 	// // Intersection begins with all properties of one class
 	// propertySet.addAll( classList.get(0).getProperties() );
 	//
 	// for(int i=0; i<classList.size()-1; i++) {
 	// System.out.println("CLASS ("+i+"): "+classList.get(i).toString());
 	// int j=i+1;
 	// for(Iterator<OntProperty> propIt =
 	// classList.get(i).getProperties().iterator(); propIt.hasNext(); ) {
 	// OntProperty p = propIt.next();
 	// System.out.println("TEST PROP "+p.getURI()+" in Class "+classList.get(j).toString());
 	// int frequency = Collections.frequency( classList.get(j).getProperties(),
 	// p );
 	// System.out.println("FREQUENCY: "+frequency);
 	// if(frequency==0 && propertySet.contains(p)) {
 	// propertySet.remove(p);
 	// }
 	// }
 	//
 	// for(Iterator<String> superClassIt=this.buildSuperClassList(omodel,
 	// classList.get(i).getName()).iterator(); superClassIt.hasNext(); ) {
 	// for(Iterator
 	// propIt=this.ontclassKB.get(superClassIt.next().toString()).getProperties().iterator();
 	// propIt.hasNext(); ) {
 	// OntProperty p = (OntProperty)propIt.next();
 	// System.out.println("TEST PROP "+p.getURI()+" in Class "+classList.get(j).toString());
 	// int frequency = Collections.frequency( classList.get(j).getProperties(),
 	// p );
 	// System.out.println("FREQUENCY: "+frequency);
 	// if(frequency==0 && propertySet.contains(p)) {
 	// propertySet.remove(p);
 	// }
 	// }
 	// }
 	// }
 	// }
 	// return propertySet;
 	// }
 
 	// private void addOntClassContainer(OntClass declaringClass) {
 	// if(!this.ontclassKB.containsKey(declaringClass.getURI())) {
 	// String RDFS_comment = null;
 	// String OWL_versionInfo = null;
 	// if(declaringClass.hasProperty(RDFS.comment)) {
 	// RDFS_comment = declaringClass.getPropertyValue(RDFS.comment).toString();
 	// System.out.println("COMMENT: "+RDFS_comment);
 	// }
 	// if(declaringClass.hasProperty(OWL.versionInfo)) {
 	// OWL_versionInfo =
 	// declaringClass.getPropertyValue(OWL.versionInfo).toString();
 	// System.out.println("VERSION: "+OWL_versionInfo);
 	// }
 	// this.ontclassKB.addDatatype(new OntClassContainer(declaringClass,
 	// RDFS_comment, OWL_versionInfo));
 	// System.out.println("[i] OntClassContainer for class ("+declaringClass.getURI()+") created. Added to KB.");
 	// }
 	// else {
 	// System.out.println("[i] declaring class "+declaringClass.getURI()+" found in KB.");
 	// }
 	// }
 
 	/**
 	 * Creates a new OntClassContainer on the basis of the OntClass information.
 	 * Constructor processes already Union-, Intersection- und Enum classes!
 	 * 
 	 * @param declaring
 	 *            class
 	 * @return OntClassContainer
 	 */
 	private OntClassContainer getOntClassContainer(OntClass declaringClass) {
 		String classKey = declaringClass.getURI();
 		System.out.println("[i] getOntClassContainer " + classKey);
 		OntClassContainer curOntClassContainer = null;
 		if (!this.ontclassKB.containsKey(classKey)) {
 			String RDFS_comment = null;
 			String OWL_versionInfo = null;
 
 			if (declaringClass.hasProperty(RDFS.comment)) {
 				RDFS_comment = declaringClass.getPropertyValue(RDFS.comment)
 						.toString();
 				// System.out.println("COMMENT: "+RDFS_comment);
 			}
 			if (declaringClass.hasProperty(OWL.versionInfo)) {
 				OWL_versionInfo = declaringClass.getPropertyValue(
 						OWL.versionInfo).toString();
 				// System.out.println("VERSION: "+OWL_versionInfo);
 			}
 
 			curOntClassContainer = new OntClassContainer(declaringClass,
 					RDFS_comment, OWL_versionInfo);
 			for (Iterator<String> it = curOntClassContainer.validateLocalName()
 					.iterator(); it.hasNext();) {
 				String msg = it.next().toString();
 				System.out.println(">> VALIDATE: " + msg);
 				if (curOntClassContainer.getOntClass().getURI() == null
 						|| msg == null) {
 					System.out.println("Nothing");
 				} else {
 					this.ontclassKB.addError(curOntClassContainer.getOntClass()
 							.getURI(), msg);
 				}
 			}
 
 			// type collecting...
 			// for(StmtIterator stmtIt = declaringClass.listProperties();
 			// stmtIt.hasNext(); )
 			// {
 			// Statement temp = stmtIt.nextStatement();
 			// if( temp.getPredicate().getLocalName().equals("type") &&
 			// !temp.getObject().asNode().getLocalName().equals("Class")) {
 			//
 			// /**
 			// * default value: http://www.w3.org/2000/01/rdf-schema#Resource
 			// ("everything")
 			// */
 			// OntClass typeClass =
 			// (OntClass)temp.getObject().as(OntClass.class);
 			// curOntClassContainer.addTypeClass(typeClass);
 			//
 			// // System.out.println("STMT: "+temp.toString());
 			// // System.out.println("SUBJECT: "+temp.getSubject().getURI());
 			// // System.out.println("OBJECT : "+temp.getObject().toString());
 			// }
 			// }
 
 			// // einfacher :) ABER!: nicht vertretbar von der Geschwindigkeit!
 			// for(ExtendedIterator rdfTypeIt=declaringClass.listRDFTypes(true);
 			// rdfTypeIt.hasNext(); ) {
 			// Resource rdfTypeRes = (Resource)rdfTypeIt.next();
 			// if(rdfTypeRes.getNameSpace() != null) {
 			// if(Arrays.binarySearch(OWLNS,rdfTypeRes.getNameSpace()) < 0) {
 			// OntClass typeClass = (OntClass)rdfTypeRes.as(OntClass.class);
 			// System.out.println("RDFTYPE: "+typeClass.getURI());
 			// }
 			// }
 			// }
 
 
 			Vector<Individual> individualList = this.collectIndividuals(
 					this.rdfsInfModel, declaringClass.getURI());
 			if (!individualList.isEmpty()) {
 				System.out.println("[i] register individualList for "
 						+ curOntClassContainer.getName());
 				curOntClassContainer.setIndividualList(individualList);
 				this.ontclassKB
 						.addCollectedClassWithIndividuals(curOntClassContainer
 								.getName());
 			}
 
 			// register container
 			this.ontclassKB.addDatatype(curOntClassContainer);
 			System.out.println("[i] OntClassContainer for class ("
 					+ declaringClass.toString() + ") created. Added to KB.");
 		} else {
 			curOntClassContainer = this.ontclassKB.get(classKey);
 			System.out.println("[i] OntClassContainer for class ("
 					+ declaringClass.toString() + ") found in KB.");
 		}
 		return curOntClassContainer;
 	}
 
 	/**
 	 * If necessary do following steps:
 	 * <ol>
 	 * <li>create OntClassContainer</li>
 	 * <li>add DatatypeProperty</li>
 	 * <li>register new OntClassContainer in KB</li>
 	 * </ol>
 	 * 
 	 * @param DatatypeProperty
 	 * @param OntClass
 	 */
 	private void addDatatypeProperty(DatatypeProperty dtp,
 			OntClass declaringClass) {
 		OntClassContainer curOntClassContainer = this
 				.getOntClassContainer(declaringClass);
 		System.out.println("[i] Add DATATYPE-PROPERTY " + dtp.getLocalName()
 				+ " to DECLARING class (" + curOntClassContainer.getName()
 				+ ")");
 		curOntClassContainer.addProperty(dtp);
 	}
 
 	public void collectDatatypeProperties()
 			throws com.hp.hpl.jena.shared.NotFoundException {
 		System.out.println("\ncollectDatatypeProperties:");
 		DatatypeProperty curdtp = null;
 		ExtendedIterator<DatatypeProperty> eit_dtp = this.rdfsInfModel
 				.listDatatypeProperties();
 
 		while (eit_dtp.hasNext()) {
 			// curdtp = omodel.getDatatypeProperty(eit_dtp.next().toString());
 			curdtp = (DatatypeProperty) eit_dtp.next();
 
 			String range = "http://www.w3.org/2001/XMLSchema#anyURI";
 
 			//
 			//
 			Vector<String> curSuperClassListOfRange = new Vector<String>();
 			if (curdtp.getRange() != null) {
 				// for(ExtendedIterator eRangeIt=curdtp.listRange();
 				// eRangeIt.hasNext(); ) { } // not needed!
 				// range = ((OntResource)eRangeIt.next()).getURI();
 				range = curdtp.getRange().getURI();
 		        System.out.println("Analysing range of: " + curdtp);
 		        if(range == null) { // Probably oneOf
 		        	if(curdtp.getRange().isDataRange()) {
 		        		DataRange dataRange = curdtp.getRange().asDataRange();
 		        		RDFList listEnum = dataRange.getOneOf();
 		        		RDFNode firstEnum = listEnum.iterator().next(); // We assume that all items have the same (primitive) type
 		        		RDFDatatype	dt;
 		        		if(firstEnum.isLiteral()) {
 		        			dt = firstEnum.asLiteral().getDatatype();
 		        		}
 		        		else {
 			        		throw new UnsupportedOperationException();
 		        		}
 		        		ExtendedIterator<RDFNode> itEnum = listEnum.iterator();
 		        		List<Literal> ll = new LinkedList<Literal>();
 		        		while(itEnum.hasNext()) {
 		        			ll.add(itEnum.next().asLiteral());
 		        		} // TODO: Complete this! At the moment falling back to just serving the datatype
 		        		
 		        		range = dt.getURI();
 		        		curSuperClassListOfRange.add(range);
 		        	}
 		        	else {
 		        		throw new UnsupportedOperationException();
 		        	}
 		        }
 		        else {
 		        	if (this.rdfsInfModel.getOntClass(range) != null) {
 		        		curSuperClassListOfRange.add(range);
 		        		if (!range.split("#")[0].toString().equals(
 		        				"http://www.w3.org/2001/XMLSchema")) {
 		        			// curSuperClassListOfRange.addAll(this.buildSuperClassList(this.rdfsInfModel,
 		        			// range)); // disjoint specific
 		        		}
 		        	}
 
 		        }
 			}
 
 			System.out.println("PROPERTY: " + curdtp.getURI() + "(RANGE: "
 					+ range + ")");
 			//
 			// Domain, declared classes
 			//
 			int domainCount = 0;
 			for (ExtendedIterator<? extends OntResource> extDomainIt = curdtp
 					.listDomain(); extDomainIt.hasNext();) {
 				System.out.println("DOMAIN: "
 						+ extDomainIt.next().asClass().getURI());
 				domainCount++;
 			}
 
 			// if(curdtp.getDomain()!=null)
 			if (domainCount == 1) {
 				// OntClass anonymOntClass = (OntClass) curdtp.getDomain().as(
 				// OntClass.class );
 				OntClass anonymOntClass = curdtp.getDomain().asClass();
 
 				if (anonymOntClass.isUnionClass()) {
 					System.out.println("[i] Domain is UNION CLASS");
 					for (ExtendedIterator<? extends OntClass> unionIt = anonymOntClass
 							.asUnionClass().listOperands(); unionIt.hasNext();) {
 						OntClass unionClass = unionIt.next();
 						this.addDatatypeProperty(curdtp, unionClass);
 					}
 				} else if (anonymOntClass.isEnumeratedClass()) {
 					System.out.println("Domain is anonym ENUMERATION CLASS");
 					throw new UnsupportedOperationException("Domain is anonym ENUMERATION CLASS");
 				} else {
 					// listDeclaringClasses funktioniert nicht immer (zipcode
 					// bsp)
 					// ordne allen subClasses das property zu
 					System.out.println("[i] DOMAIN : "
 							+ ((OntResource) curdtp.getDomain()).getURI());
 					try {
 						/*
 						 * using at this point direct = true because we handle
 						 * the inference later to retrieve a inference value
 						 * (deepness), so only one class should received here!
 						 */
 						for (ExtendedIterator<? extends OntClass> eit_ldc = curdtp
 								.listDeclaringClasses(true); eit_ldc.hasNext();) {
 							OntClass declaringOntClass = eit_ldc.next();
 							System.out.println("[i] DECLARING CLASS: "
 									+ declaringOntClass.toString());
 
 							boolean DISJOINT_FOUND = false;
 							if (curdtp.getRange() != null) // damit
 															// Actorbeispiel
 															// funktioniert
 							{
 								if (curdtp.getRange().getRDFType() != null) {
 									// DISJOINT CHECK (find super classes of
 									// range type and test if current range)
 									Vector<String> disjointCheckList = new Vector<String>();
 									if (curdtp.getRange().getRDFType()
 											.getLocalName().equals("Class")) {
 										disjointCheckList
 												.addAll(curSuperClassListOfRange);
 									}
 
 									// CHECK FOR DISJOINT INDIRECT MATCH
 									for (int i = 0; i < disjointCheckList
 											.size(); i++) {
 										// System.out.println("CHECK1: "+declaringOntClass.getURI()
 										// + " AND " +
 										// disjointCheckList.get(i));
 										if (this.ontclassKB.isDisjoint(
 												declaringOntClass.getURI(),
 												disjointCheckList.get(i)
 														.toString())) {
 											System.out
 													.println("[DISJOINT found] "
 															+ declaringOntClass
 																	.getURI()
 															+ " AND "
 															+ disjointCheckList
 																	.get(i));
 											DISJOINT_FOUND = true;
 										}
 									}
 								}
 							} else {
 								continue;
 							}
 
 							if (declaringOntClass.getRDFType().getLocalName()
 									.equals("Class")
 									&& !DISJOINT_FOUND) {
 								this.addDatatypeProperty(curdtp,
 										declaringOntClass);
 							} else {
 								System.out.println("TYPE:"
 										+ declaringOntClass.getRDFType()
 												.getLocalName()); // -->
 																	// Restriction
 																	// oder
 																	// rdf:type!
 							}
 						} // listDeclaringClasses iterator
 					} catch (ConversionException ce) {
 						System.err
 								.println("[e] Warning /collectDatatypeProperties: ConversionException for "
 										+ anonymOntClass.getURI()
 										+ ": "
 										+ ce.toString());
 						this.ontclassKB.addError(anonymOntClass.getURI(),
 								ce.getMessage());
 						continue;
 					} catch (Exception e) {
 						System.err.println("[e] collectDatatypeProperties: "
 								+ e);
 						e.printStackTrace();
 						this.ontclassKB.addError(anonymOntClass.getURI(),
 								e.getMessage());
 						continue;
 					}
 				} // isUnionClass check
 			} // domain check
 			else if (domainCount > 1) {
 				System.out
 						.println("[i] more than one domain defined, intersectionOf(d1, d2, ...)");
 			} else {
 				System.out
 						.println("No declaring class for property (no domain)!");
 				this.domainlessProperties.add(curdtp);
 			}
 		}
 	}
 
 	// private void addFunctionalSubProperties(OntClassContainer
 	// curOntClassContainer, OntProperty p)
 	// {
 	// for(ExtendedIterator subit = p.listSubProperties(false); subit.hasNext();
 	// ) {
 	// OntProperty subproperty = (OntProperty) subit.next();
 	// // guckst Du hier: http://ontoworld.org/wiki/Rdfs:subClassOf
 	// //if(subproperty.isFunctionalProperty()) {
 	// System.out.println("[i] add SUB PROPERTY "+subproperty.getURI()+" to "+curOntClassContainer.getLocalName());
 	// curOntClassContainer.addProperty(subproperty);
 	// this.addFunctionalSubProperties(curOntClassContainer, subproperty);
 	// //}
 	// }
 	// }
 
 	public void collectObjectProperties() {
 		System.out.println("\ncollect ObjectProperties:");
 
 		ObjectProperty cur_op = null;
 		ExtendedIterator<ObjectProperty> eit_op = this.rdfsInfModel
 				.listObjectProperties();
 		while (eit_op.hasNext()) {
 			cur_op = eit_op.next();
 			if (cur_op.getNameSpace().equals(OWL_NS)) {
 				System.out.println("[i] OWL NS property found, next!");
 				continue;
 			}
 
 			// if(cur_op == null) {
 			// System.out.println("[i] ObjectProperty is null");
 			// }
 
 			System.out.println("[i] ObjectProperty found: " + cur_op.getURI());
 
 			String rangeURI = "n/a, probably FunctionalProperty defined in Restriction part";
 
 			if (cur_op.getRange() != null) {
 				if (cur_op.getRange().asClass().isUnionClass()) {
 					System.out.println("[i] RANGE IS UNION");
 				} else {
 					rangeURI = cur_op.getRange().getURI();
 
 					if (!this.ontclassKB.containsKey(rangeURI)) {
 						System.out.println("    New Range found: " + rangeURI);
 						// OntClass tempoc =
 						// this.rdfsInfModel.getOntClass(rangeURI);
 						// OntClassContainer curOntClassContainer =
 						// this.getOntClassContainer(tempoc);
 					} else {
 						System.out.println("    Range " + rangeURI
 								+ " already in OntClassKB.");
 					}
 				}
 			} else {
 				// eg. <owl:ObjectProperty
 				// rdf:ID="performedBy"><rdfs:subPropertyOf
 				// rdf:resource="#hasParticipant"/>...
 				// SuperProperties processed here "just" to create Container for
 				// range types in advance
 				// Translation functionality is located in
 				// OWLHelper.getPropertyRange(OntProperty)
 				boolean RANGEFOUND = false;
 				for (ExtendedIterator<? extends OntProperty> superPropIt = cur_op
 						.listSuperProperties(true); superPropIt.hasNext();) {
 					ObjectProperty super_prop = superPropIt.next()
 							.asObjectProperty();
 					System.out.println("   SuperProp: " + super_prop.getURI());
 					if (super_prop.getRange() != null) {
 						rangeURI = super_prop.getRange().getURI();
 						RANGEFOUND = true;
 
 						if (!this.ontclassKB.containsKey(rangeURI)) {
 							System.out
 									.println("    New Range (SuperProperty) found: "
 											+ rangeURI);
 							// OntClass tempoc =
 							// this.rdfsInfModel.getOntClass(rangeURI);
 							// OntClassContainer curOntClassContainer =
 							// this.getOntClassContainer(tempoc);
 						} else {
 							System.out.println("    Range " + rangeURI
 									+ " already in OntClassKB.");
 						}
 					}
 				}
 				if (!RANGEFOUND) {
 					System.out.println("    Range: no range");
 				}
 			}
 
 			// Filter<OntClass> ontClassDropFilter = new Filter<OntClass>() {
 			// public boolean accept(OntClass o) {
 			// if( o.isIntersectionClass() ||
 			// //((OntClass)o).isUnionClass() ||
 			// o.isEnumeratedClass() ||
 			// o.isRestriction() ) {
 			// return true;
 			// }
 			// else {
 			// return false;
 			// }
 			// }
 			// };
 
 			int domainCount = 0;
 			for (ExtendedIterator<? extends OntResource> extDomainIt = cur_op
 					.listDomain(); extDomainIt.hasNext();) {
 				System.out.println("DOMAIN: "
 						+ extDomainIt.next().asClass().getURI());
 				domainCount++;
 			}
 
 			if (domainCount == 1) {
 				System.out.println("Processing domain(s) for ObjectProperty");
 				// OntClass anonymOntClass = cur_op.getDomain().asClass();
 
 				// Bemerkung: direct value false zieht alle super properties an!
 				// manchmal werden nicht alle Domains angezogen, etwas
 				// verbessert mit getDomain, siehe unten
 				// Bsp.: listDeclaringClasses,
 				// http://www.mindswap.org/2003/owl/geo/geoCoordinateSystems20040307.owl#
 				ExtendedIterator<? extends OntClass> eit_ldc = cur_op
 						.listDeclaringClasses(true); // .filterDrop(ontClassDropFilter);
 				while (eit_ldc.hasNext()) {
 					OntClass tempoc = eit_ldc.next();
 					OntClassContainer curOntClassContainer = null;
 
 					if (tempoc.isUnionClass()) {
 						System.out.println("[i] declaring class is UnionClass");
 						for (ExtendedIterator<? extends OntClass> unionIt = tempoc
 								.asUnionClass().listOperands(); unionIt
 								.hasNext();) {
 							OntClass unionClass = unionIt.next();
 							System.out.println("  --> " + unionClass.getURI());
							// curOntClassContainer =
							// this.getOntClassContainer(unionClass);
							// System.out.println("[i] Add OBJECT-PROPERTY "+cur_op.getLocalName()+
							// " to DECLARING UNION CLASS ("+curOntClassContainer.getName()+")");
							// curOntClassContainer.addProperty(cur_op);
 						}
 					} else if (tempoc.isComplementClass()) {
 						if (tempoc.asComplementClass().getOperand()
 								.isRestriction()) {
 							System.out
 									.println("[i] Excluding Complement Restriction");
 						} else {
 							System.out
 									.println("[i] Excluding Complement Classes");
 						}
 					} else if (tempoc.isIntersectionClass()) {
 						System.out
 								.println("[i] declaring class is IntersectionClass");
 						for (ExtendedIterator<? extends OntClass> intSectIt = tempoc
 								.asIntersectionClass().listOperands(); intSectIt
 								.hasNext();) {
 							OntClass intSectClass = intSectIt.next();
 							if (intSectClass.isRestriction()) {
 								Restriction intSectRestriction = intSectClass
 										.asRestriction();
 								System.out.println(" --> Restriction");
 								owlHelper.printRestriction(intSectRestriction);
 							} else {
 								System.out.println(" --> "
 										+ intSectClass.getURI());
 								curOntClassContainer = this
 										.getOntClassContainer(tempoc);
 								System.out.println("[i] Add OBJECT-PROPERTY "
 										+ cur_op.getLocalName()
 										+ " to DECLARING CLASS ("
 										+ curOntClassContainer.getName()
 										+ "), classpart of intersection");
 								curOntClassContainer.addProperty(cur_op);
 							}
 						}
 					} else if (tempoc.isEnumeratedClass()) {
 						System.out
 								.println("[i] declaring class is EnumeratedClass");
 						for (ExtendedIterator<? extends OntResource> oneOfIt = tempoc
 								.asEnumeratedClass().listOneOf(); oneOfIt
 								.hasNext();) {
 							OntResource oneOf = oneOfIt.next();
 							System.out.println("  --> " + oneOf.getURI());
 						}
 					} else if (tempoc.isHierarchyRoot()) {
 						if (tempoc.isRestriction()) {
 							System.out.println("[i] declaring class "
 									+ tempoc.getURI() + " is HierarchyRoot");
 						}
 						else {
 							// This was not here in previous versions, but it doesn't make sense
 							// Thus, making it happen...
 							// TODO: Tidy it up a little, there is repetition.
 							curOntClassContainer = this
 									.getOntClassContainer(tempoc);
 							System.out.println("[i] Add OBJECT-PROPERTY "
 									+ cur_op.getLocalName()
 									+ " to DECLARING CLASS ("
 									+ curOntClassContainer.getName() + ")");
 							curOntClassContainer.addProperty(cur_op);
 						}
 					} else {
 						if (tempoc.getURI() == null) {
 							this.ontclassKB.addError("[?] unknown",
 									"unknown class for object property "
 											+ cur_op.getURI());
 						} else {
 							curOntClassContainer = this
 									.getOntClassContainer(tempoc);
 							System.out.println("[i] Add OBJECT-PROPERTY "
 									+ cur_op.getLocalName()
 									+ " to DECLARING CLASS ("
 									+ curOntClassContainer.getName() + ")");
 							curOntClassContainer.addProperty(cur_op);
 						}
 					}
 
 					// Noch nicht so ganz einig, wie SupProperties behandelt
 					// werden sollen
 					// Aber Beispiel wo es nicht pass:
 
 					// this.addFunctionalSubProperties(curOntClassContainer,
 					// cur_op);
 
 					// // CHECK GIVEN RESTRICTIONS FOR CURRENT OBJ-PROPERTY ON
 					// CURRENT CLASS
 					//
 					// for(ExtendedIterator eit = tempoc.listSuperClasses();
 					// eit.hasNext(); ) {
 					//
 					// OntClass sclass = (OntClass)eit.next();
 					// if(sclass.isRestriction()) {
 					// Restriction curr = sclass.asRestriction();
 					// System.out.println("[i] [RESTRICTION]");
 					// System.out.println("    [OnProperty]  : "+curr.getOnProperty().getURI());
 					// if(curr.hasProperty(OWL.maxCardinality) &&
 					// curr.getOnProperty().toString().equals(cur_op.toString())
 					// ) {
 					// //System.out.println("maxCardinality: "+((Literal)curr.getPropertyValue(OWL.maxCardinality)).getValue().toString());
 					// curOntClassContainer.setMinCardinality(curr.getOnProperty().getLocalName(),
 					// ((Literal)curr.getPropertyValue(OWL.maxCardinality)).getValue().toString());
 					// }
 					// }
 					// else {
 					// System.out.println("SUPER CLASS: "+sclass.toString());
 					// }
 					// }
 
 				} // END OF WHILE
 			} // DOMAIN CHECK
 			else if (domainCount > 1) {
 				System.out
 						.println("[i] more than one domain defined, intersectionOf(d1, d2, ...)");
 				//
 				// noch unklar wie Properties vererbt werden sollen. Eigentlich:
 				// nur wenn weitere
 				// Klassen aller Domainen der intersection erben, sollte vererbt
 				// werden.
 				// Im Moment: Jede Domain bekommt das Property und kann es
 				// weitergeben...
 				Vector<Statement> domains = this.getStatements(
 						this.rdfsInfModel, cur_op, RDFS.domain);
 				for (Iterator<Statement> it = domains.iterator(); it.hasNext();) {
 					Statement stmt = it.next();
 					OntClassContainer container = this
 							.getOntClassContainer(this.rdfsInfModel
 									.getOntClass(stmt.getObject().toString()));
 					if (!container.getProperties().contains(cur_op)) {
 						System.out.println("[s] Add OBJECT-PROPERTY "
 								+ cur_op.getLocalName()
 								+ " to DECLARING CLASS (" + container.getName()
 								+ ")");
 						container.addProperty(cur_op);
 						this.ontclassKB.addError(container.getOntClass()
 								.getURI(), "ObjectProperty " + cur_op.getURI()
 								+ " indirect over Statement view added.");
 					} else {
 						System.out.println("[ ] Property already added to "
 								+ container.getName());
 					}
 				}
 
 			} else {
 				System.out.println("[i] no domain");
 			}
 
 			// for(ExtendedIterator sp_it = cur_op.listSuperProperties(true);
 			// sp_it.hasNext(); ) {
 			// OntProperty superProp = (OntProperty)sp_it.next();
 			// System.out.println("SUPERPROP: "+superProp.getURI());
 			// if(superProp.hasDomain()) {
 			//
 			// }
 			// }
 
 			System.out.println();
 		}
 	}
 
 	/**
 	 * problem mit wine ont, obsolete da bei der Konvertierung in
 	 * AbstractDatatype functional properties gekennzeichnet werden
 	 */
 	public void collectFunctionalProperties(OntModel omodel) {
 		System.out.println("\ncollect FunctionalProperties");
 
 		ExtendedIterator<FunctionalProperty> eit_fp = omodel
 				.listFunctionalProperties();
 		while (eit_fp.hasNext()) {
 			OntProperty prop = eit_fp.next();
 			if (prop.isFunctionalProperty()) {
 				FunctionalProperty fp = (FunctionalProperty) prop;
 				System.out.println("FP: " + fp.getURI());
 				for (ExtendedIterator<? extends OntClass> eit = fp
 						.listDeclaringClasses(); eit.hasNext();) {
 					OntClass ontClass = eit.next();
 					System.out.println("---> " + ontClass.getURI());
 				}
 			}
 		}
 
 		// ExtendedIterator eit_p = omodel.listObjectProperties();
 		// while(eit_p.hasNext()) {
 		// OntProperty prop = (OntProperty)eit_p.next();
 		// System.out.println("PROP: "+prop.toString());
 		// if(prop.isFunctionalProperty()) {
 		// System.out.println("Fn");
 		// }
 		// else if(prop.isObjectProperty()) {
 		// System.out.println("Obj");
 		// }
 		// }
 
 	}
 
 	// private void collectSuperClasses(OntModel omodel) {
 	// System.out.println("\ncollect SuperClasses");
 	// //
 	// // GRAB CLASS DEFINITIONS IN MODEL
 	// //
 	// SimpleSelector sel = new SimpleSelector(null, null, (RDFNode) null) {
 	// public boolean selects(Statement s) {
 	// boolean SELECT = false;
 	// if(s.getPredicate().toString().endsWith("type") &&
 	// s.getObject().toString().endsWith("Class") && s.getSubject().as(
 	// OntClass.class ).getURI()!=null ) {
 	// SELECT = true;
 	// //System.out.println(">>> SELECT !!!! \n");
 	// }
 	// return SELECT;
 	// }
 	// };
 	//
 	// ExtendedIterator eit = omodel.listStatements(sel);
 	// while(eit.hasNext()) {
 	// Statement stmt = (Statement)eit.next();
 	// Resource subject = stmt.getSubject(); // <-- SEL CLASS
 	// //System.out.println(">>>>> Statement: "+stmt.toString());
 	// System.out.println();
 	// OntClass tempoc = subject.as( OntClass.class );
 	// OntClassContainer curOntClassContainer =
 	// this.getOntClassContainer(tempoc);
 	// for(ExtendedIterator superclass_it =
 	// tempoc.listSuperClasses(false).filterDrop(this.ontClassSpecialsFilter);
 	// superclass_it.hasNext(); ) {
 	// OntClass superclass = (OntClass)superclass_it.next();
 	//
 	// System.out.println("[i] SuperClass found: "+superclass.getURI());
 	// curOntClassContainer.addSuperClass(superclass);
 	//
 	// // SET PROPERTIES...
 	// Vector<String> superclasses = new Vector<String>();
 	// superclasses.add(superclass.getURI());
 	// superclasses.addAll(this.buildSuperClassList(omodel,
 	// superclass.getURI()));
 	//
 	// OntClassContainer superclassContainer = null;
 	// for(Iterator<String> it=superclasses.iterator(); it.hasNext(); )
 	// {
 	// String superclassURI = it.next().toString();
 	// System.out.println("[i] (collectSuperClasses): "+superclassURI);
 	// if(!this.ontclassKB.containsKey(superclassURI)) { //z.B.:
 	// http://www.w3.org/2002/07/owl#Thing
 	// OntClass supersuperclass = omodel.getOntClass(superclassURI);
 	// this.addOntClassContainer(supersuperclass);
 	// }
 	//
 	// for(Iterator
 	// propIt=this.ontclassKB.get(superclassURI).getProperties().iterator();
 	// propIt.hasNext(); ) {
 	// OntProperty prop = (OntProperty)propIt.next();
 	// System.out.println("[i] add SuperProperty "+prop.getLocalName()+" to "+curOntClassContainer.getLocalName());
 	// curOntClassContainer.addProperty(prop);
 	// }
 	// }
 	//
 	// }
 	// }
 	//
 	// }
 
 	public void showStatements(OntModel omodel) {
 		ExtendedIterator<Statement> eit = omodel.listStatements();
 		int i = 1;
 		while (eit.hasNext()) {
 			Statement s = eit.next();
 			// if(s.getPredicate().equals(RDFS.domain))
 			System.out.println(i + ") SUBJEKT (" + s.getSubject().toString()
 					+ ") OBJECT (" + s.getObject().toString() + ")");
 			i++;
 		}
 	}
 
 	private Vector<Statement> getStatements(OntModel omodel, Resource subject,
 			Property predicate) {
 		Vector<Statement> statements = new Vector<Statement>();
 		ExtendedIterator<Statement> eit = omodel.listStatements();
 		while (eit.hasNext()) {
 			Statement s = eit.next();
 			if (s.getPredicate().equals(predicate)
 					&& s.getSubject().equals(subject)) {
 				statements.add(s);
 			}
 		}
 		return statements;
 	}
 
 	public void collectClassInformationAndRestrictions() {
 		System.out.println("\ncollect Classes and Restrictions");
 
 		//
 		// GRAB CLASS DEFINITIONS IN MODEL
 		//
 		SimpleSelector sel = new SimpleSelector(null, null, (RDFNode) null) {
 			public boolean selects(Statement s) {
 				boolean SELECT = false;
 				// Check if internal class
 				if (s.getSubject().getNameSpace() != null) {
 					if (Arrays.binarySearch(OWLNS, s.getSubject()
 							.getNameSpace()) >= 0) {
 						return false;
 					}
 				}
 				if (s.getPredicate().toString().endsWith("type")
 						&& s.getObject().toString().endsWith("Class")
 						&& s.getSubject().as(OntClass.class).getURI() != null) {
 					SELECT = true;
 					// System.out.println(">>> SELECT !!!! \n");
 				}
 				return SELECT;
 			}
 		};
 
 		ArrayList<String> ontClassList = new ArrayList<String>();
 																	// listClasses()
 		// listStatements: zeigt auch implicit definierte Klassen an
 		// Statements gefunden werden.
 
 		this.ontClassSpecialsFilter = new Filter<OntClass>() {
 			public boolean accept(OntClass o) {
 				if (o.isIntersectionClass() || o.isUnionClass()
 						|| o.isEnumeratedClass() || o.isComplementClass()
 						|| o.isRestriction())
 					return true;
 				if (o.getNameSpace()
 						.equals("http://www.w3.org/2001/XMLSchema#")) {
 					System.out.println("Filter primitive XML Schema Type: "
 							+ o.getURI());
 					return true;
 				}
 				return false;
 			}
 		};
 
 		for (ExtendedIterator<OntClass> classIt = this.rdfsInfModel
 				.listClasses().filterDrop(this.ontClassSpecialsFilter); classIt
 				.hasNext();) {
 			OntClass tempoc = classIt.next();
 			ontClassList.add(tempoc.getURI());
 			System.out.println("[i] build explicit defined container for "
 					+ tempoc.getURI());
 			if (tempoc.getLocalName().equals("")) {
 				System.out
 						.println("[e] no local name found, don't build container");
 			} else {
 				/* OntClassContainer curOntClassContainer = */this
 						.getOntClassContainer(tempoc);
 			}
 		}
 
 		for (ExtendedIterator<Statement> eit = this.rdfsInfModel
 				.listStatements(sel); eit.hasNext();) {
 			Statement stmt = eit.next();
 			// System.out.println(">>>>> Statement: "+stmt.toString());
 			Resource subject = stmt.getSubject(); // <-- SEL CLASS
 			OntClass tempoc = subject.as(OntClass.class);
 
 			if (!ontClassList.contains(tempoc.getURI())) {
 				System.out.println("[i] build implicit defined container for "
 						+ tempoc.getURI());
 				if (!tempoc.getURI().contains("#")) {
 					System.out
 							.println("[e] Relative URIs are not permitted in RDF");
 					System.out.println("[i] Where we are?: " + stmt.toString());
 				} else {
 					// OntClassContainer curOntClassContainer =
 					// this.getOntClassContainer(tempoc);
 				}
 			}
 		}
 
 		int counter = 0;
 		int total = this.ontclassKB.getRegisteredDatatypes().size();
 		for (Iterator<OntClassContainer> it = this.ontclassKB
 				.getRegisteredDatatypes().values().iterator(); it.hasNext();) {
 			counter++;
 			OntClassContainer curOntClassContainer = it.next();
 			OntClass tempoc = curOntClassContainer.getOntClass();
 			System.out.println("Process #" + counter + "/" + total + " ("
 					+ tempoc.getURI() + ")");
 
 			OntClass simpleClass = this.dlModel.getOntClass(tempoc.getURI());
 			// speed it up by using no inference...
 
 			if (simpleClass != null) {
 				// einfacher :) ABER generelles Problem mit Geschwindigkeit!
 				// listRDFTypes/
 				for (ExtendedIterator<Resource> rdfTypeIt = simpleClass
 						.listRDFTypes(true); rdfTypeIt.hasNext();) {
 					Resource rdfTypeRes = rdfTypeIt.next();
 					if (rdfTypeRes.getNameSpace() != null) {
 						if (Arrays.binarySearch(OWLNS,
 								rdfTypeRes.getNameSpace()) < 0) {
 							OntClass typeClass = rdfTypeRes.as(OntClass.class);
 							curOntClassContainer.addTypeClass(typeClass);
 						}
 					}
 				}
 			} else {
 				for (ExtendedIterator<Resource> rdfTypeIt = tempoc
 						.listRDFTypes(true); rdfTypeIt.hasNext();) {
 					Resource rdfTypeRes = rdfTypeIt.next();
 					if (rdfTypeRes.getNameSpace() != null) {
 						if (Arrays.binarySearch(OWLNS,
 								rdfTypeRes.getNameSpace()) < 0) {
 							OntClass typeClass = rdfTypeRes.as(OntClass.class);
 							curOntClassContainer.addTypeClass(typeClass);
 						}
 					}
 				}
 			}
 
 			// System.out.println("=== INDIVIDUALS PART ===============================");
 			// Vector individualList = this.collectIndividuals(omodel,
 			// curOntClassContainer.getName());
 			// if(!individualList.isEmpty()) {
 			// System.out.println("[i] register individualList for "+curOntClassContainer.getName());
 			// curOntClassContainer.setIndividualList(individualList);
 			// this.ontclassKB.addCollectedClassWithIndividuals(curOntClassContainer.getName());
 			// }
 
 			// / ????
 			// System.out.println("[i] collect + register instances for "+curOntClassContainer.getName());
 			// Vector individualList = this.collectIndividuals(omodel,
 			// curOntClassContainer.getName());
 			// curOntClassContainer.setIndividualList(individualList);
 
 			System.out
 					.println("=== EQUIVALENT DEFINITIONS: Restrictions, Complement-, Intersection- and UnionClasses =======");
 
 			if (tempoc.hasProperty(OWL.equivalentClass)) {
 				System.out.println("[i] EQUIVALENT CLASS");
 				OntClass equivalent = tempoc.getEquivalentClass();
 				if (equivalent.isUnionClass()) {
 					System.out.println("    UNION");
 					for (ExtendedIterator<? extends OntClass> unionIt = equivalent
 							.asUnionClass().listOperands(); unionIt.hasNext();) {
 						curOntClassContainer.addEquivalentUnionClass(unionIt
 								.next());
 					}
 				} else if (equivalent.isIntersectionClass()) {
 					System.out.println("    INTERSECTION");
 					for (ExtendedIterator<? extends OntClass> intSectIt = equivalent
 							.asIntersectionClass().listOperands(); intSectIt
 							.hasNext();) {
 						System.out
 								.println("collectClassInformationAndRestrictions urOntClassContainer.addEquivalentIntersectionClass");
 						curOntClassContainer
 								.addEquivalentIntersectionClass(intSectIt
 										.next());
 					}
 				} else if (equivalent.isComplementClass()) {
 					System.out.println("    COMPLEMENT");
 				} else {
 					System.out.println("    SINGLE CLASS"); // Bsp.:
 															// food.owl#Wine
 															// equivalent zu
 															// wine.owl#Wine
 					curOntClassContainer
 							.addEquivalentIntersectionClass(equivalent);
 				}
 			}
 
 			System.out
 					.println("=== OWL SUPERCLASS DEFINITIONS: Restrictions, Complement-, Intersection- and UnionClasses =======");
 			try {
 
 				for (ExtendedIterator<OntClass> superclass_it = tempoc
 						.listSuperClasses(true); superclass_it.hasNext();) {
 					OntClass superclass = superclass_it.next();
 
 					// System.out.println("[i]      URL: "+superclass.getURI());
 					// System.out.println("[i] RDF-TYPE: "+superclass.getRDFType().getURI());
 					// // Restriction or Class
 
 					if (superclass.isRestriction()) {
 						curOntClassContainer
 								.addSubClassPropertyRestriction(superclass
 										.asRestriction());
 					} else if (superclass.isIntersectionClass()) {
 						if (superclass.getURI() == null) {
 							System.out
 									.println("[i] anonym subClassOf - IntersectionClass found.");
 							for (ExtendedIterator<? extends OntClass> intSectIt = superclass
 									.asIntersectionClass().listOperands(); intSectIt
 									.hasNext();) {
 								curOntClassContainer
 										.addSubClassIntersectionClass(intSectIt
 												.next());
 							}
 						} else {
 							// System.out.println("[i] subClassOf - Intersection (Super) Class found: "+superclass.getURI());
 							// Beispiel: wine.owl, Sauternes und Bordeaux
 							curOntClassContainer.addSuperClass(superclass);
 						}
 						// // set Properties // Problem: Intersection Class in
 						// Intersection
 						// HashSet intSecProperties =
 						// this.getIntersectionProperties(omodel,
 						// curOntClassContainer.getSubClassIntersectionClassSet());
 						// for(Iterator intSecIt=intSecProperties.iterator();
 						// intSecIt.hasNext(); ) {
 						// OntProperty prop = (OntProperty)intSecIt.next();
 						// System.out.println("adD_PROP:"+prop.getURI());
 						// curOntClassContainer.addProperty(prop);
 						// }
 					} else if (superclass.isUnionClass()) {
 						System.out
 								.println("[i] anonym subClassOf - UnionClass found.");
 
 						if (superclass.getURI() == null) {
 							System.out
 									.println("[i] anonym subClassOf - UnionClass found.");
 							for (ExtendedIterator<? extends OntClass> unionIt = superclass
 									.asUnionClass().listOperands(); unionIt
 									.hasNext();) {
 								curOntClassContainer
 										.addSubClassUnionClass(unionIt.next());
 
 								// Bemerkung: Properties, die in der Domain mit
 								// unionOf mehrere Klassen refernzieren
 								// werden bei listDeclaredProperties nur durch
 								// Der Reasoner scheitert bei komplizierteren
 								// Ontologien an der Stelle.
 								// for(ExtendedIterator
 								// unionPropIt=unionClass.listDeclaredProperties(false);
 								// unionPropIt.hasNext(); ) {
 								// curOntClassContainer.addProperty((OntProperty)unionPropIt.next());
 								// }
 								// Alternativ: Bereits gesammelten Properties
 								// aus KB abfragen
 								// Achtung!!!: geht an der Stelle nicht, weil
 								// noch nicht alle Klassen in KB
 								// Todo: Parse-Reihenfolge anpassen
 								// Parents.
 
 								// for(Iterator
 								// propIt=this.ontclassKB.get(unionClass.getURI()).getProperties().iterator();
 								// propIt.hasNext(); ) {
 								// OntProperty prop =
 								// (OntProperty)propIt.next();
 								// curOntClassContainer.addProperty(prop);
 								// }
 								// for(Iterator
 								// it=this.buildSuperClassList(omodel,
 								// unionClass.getURI()).iterator();
 								// it.hasNext(); ) {
 								// for(Iterator
 								// propIt=this.ontclassKB.get(it.next().toString()).getProperties().iterator();
 								// propIt.hasNext(); ) {
 								// OntProperty prop =
 								// (OntProperty)propIt.next();
 								// curOntClassContainer.addProperty(prop);
 								// }
 								// }
 							}
 						} else {
 							System.out
 									.println("[i] subClassOf - Union (Super) Class found: "
 											+ superclass.getURI());
 							curOntClassContainer.addSuperClass(superclass);
 						}
 					} else if (superclass.isComplementClass()) {
 						OntClass complementClass = superclass
 								.asComplementClass().getOperand();
 						System.out.println("[i] ComplementClass found: "
 								+ complementClass.getURI());
 						curOntClassContainer
 								.addSubClassComplementClass(complementClass);
 						//
 						// for(Iterator
 						// propIt=this.ontclassKB.get(complementClass.getURI()).getProperties().iterator();
 						// propIt.hasNext(); ) {
 						// OntProperty prop = (OntProperty)propIt.next();
 						// System.out.println("REMOVE PROPERTY: "+prop.getURI());
 						// curOntClassContainer.getProperties().remove(prop);
 						// }
 						//
 						// for(Iterator it=this.buildSuperClassList(omodel,
 						// complementClass.getURI()).iterator(); it.hasNext(); )
 						// {
 						// String superclassURI = it.next().toString();
 						// System.out.println("SUPER CLASS: "+superclassURI);
 						// if(this.ontclassKB.containsKey(superclassURI)) {
 						// System.out.println("COLLECT PROPS.");
 						// for(Iterator
 						// propIt=this.ontclassKB.get(superclassURI).getProperties().iterator();
 						// propIt.hasNext(); ) {
 						// OntProperty prop = (OntProperty)propIt.next();
 						// System.out.println("REMOVE PROPERTY -2 : "+prop.getURI());
 						// curOntClassContainer.getProperties().remove(prop);
 						// }
 						// }
 						// }
 					} else if (superclass.isClass()) {
 						System.out.println("[i] subClassOf found: "
 								+ superclass.getURI());
 						if (superclass
 								.getURI()
 								.equals("http://www.w3.org/2000/01/rdf-schema#Resource")) {
 							System.out
 									.println("[i] http://www.w3.org/2000/01/rdf-schema#Resource as default for RDFS reasoning found.");
 						} else {
 							curOntClassContainer.addSuperClass(superclass);
 						}
 					} else {
 						System.out.println("RDF-TYPE unkown: "
 								+ superclass.getRDFType().toString());
 					}
 				} // END for superclasses
 
 			} catch (ConversionException ce) {
 				// node wird nicht gefunden
 				System.err
 						.println("[e] ConversionException @ collectClassInformationAndRestrictions:\n"
 								+ tempoc.getURI() + ": " + ce.getMessage());
 				this.ontclassKB.addError(tempoc.getURI(), ce.getMessage());
 				// ce.printStackTrace();
 				continue;
 			} catch (Exception e) {
 				System.out.println("[e] Exception: " + e);
 				// e.printStackTrace();
 				this.ontclassKB.addError(tempoc.getURI(), e.getMessage());
 				System.out
 						.println("[i] OnClassContainer not added to ontclassKB, new error entry made.");
 				continue;
 			}
 
 			System.out.println();
 		} // end of while (class iterator)
 	}
 
 	private Vector<Individual> collectIndividuals(OntModel omodel,
 			String ontClassURI) {
 		Vector<Individual> individuals = new Vector<Individual>();
 		OWLIndividualTypeFilter f = new OWLIndividualTypeFilter(ontClassURI);
 
 		// Resource cls = omodel.getResource(ontClassURI);
 		for (ExtendedIterator<Individual> iit = omodel.listIndividuals()
 				.filterKeep(f); iit.hasNext();) {
 			Individual individual = iit.next();
 			System.out.println("[I] (" + ontClassURI + ") "
 					+ individual.getURI()); // toString());
 			individuals.add(individual);
 		}
 		return individuals;
 	}
 
 	public Vector<Individual> collectIndividuals(String inputFileName,
 			String ontClassURI) {
 		System.out.println("\ncollect Instances");
 		OntModel omodel = ModelFactory
 				.createOntologyModel(OntModelSpec.OWL_MEM);
 		omodel.read(inputFileName);
 		return this.collectIndividuals(omodel, ontClassURI);
 	}
 
 	private void collectMissingInstances() {
 		System.out.println("\ncollect Missing Instances");
 
 		// OntModel omodel = ModelFactory.createOntologyModel(
 		// OntModelSpec.OWL_MEM );
 		// omodel.read(inputFileName);
 
 		for (Iterator<OntClassContainer> it = this.ontclassKB
 				.getRegisteredDatatypes().values().iterator(); it.hasNext();) {
 			OntClassContainer curOntClassContainer = it.next();
 			if (curOntClassContainer.getIndividualList().isEmpty()) {
 				// System.out.println("[i] collect + register missing instances for "+curOntClassContainer.getName());
 				Vector<Individual> individualList = this.collectIndividuals(
 						this.rdfsInfModel, curOntClassContainer.getName());
 				curOntClassContainer.addIndividuals(individualList);
 				this.ontclassKB
 						.addCollectedClassWithIndividuals(curOntClassContainer
 								.getName());
 			}
 		}
 	}
 
 	private void collectEnumerationTypes() {
 		Vector<String> missingTypeList = new Vector<String>();
 		for (Iterator<OntClassContainer> it = this.ontclassKB
 				.getRegisteredDatatypes().values().iterator(); it.hasNext();) {
 			OntClassContainer curOntClassContainer = it.next();
 			if (!curOntClassContainer.getEquivalentEnumerationSet().isEmpty()) {
 				for (Iterator<OntResource> typeIt = curOntClassContainer
 						.getEquivalentEnumerationSet().iterator(); typeIt
 						.hasNext();) {
 					OntResource ontRes = typeIt.next();
 					if (!missingTypeList.contains(ontRes.getRDFType().getURI())) {
 						System.out.println("MISSING TYPE: " + ontRes.getURI()
 								+ " (" + ontRes.getRDFType().getURI() + ")");
 						missingTypeList.add(ontRes.getRDFType().getURI());
 					}
 				}
 			}
 		}
 	}
 
 	public void printCollectedData() {
 		this.ontclassKB.showAllRegisteredDatatypesFull();
 		// this.ontclassKB.showAllRegisteredDatatypes();
 		// this.ontclassKB.showAllTypesWithIndividuals();
 	}
 
 	public void printCollectedData(String uri)
 			throws java.lang.NullPointerException {
 		this.ontclassKB.get(uri).showData();
 	}
 
 	public void printErroneousData() {
 		this.ontclassKB.showErroneous();
 	}
 
 	public void getAbstractDatatypeKBData() {
 		System.out.println("============== getAbstractDatatypeKB ===========");
 
 		// obsolete wegen singleton: AbstractDatatypeKB.adt_kb = new
 		// AbstractDatatypeKB();
 
 		this.ontclassKB.syncAbstractDatatypeKB();
 	}
 
 	// private void parsePropertyValues(String path) throws Exception
 	// {
 	// URL curl = new URL(path);
 	// OntModel omodel =
 	// ModelFactory.createOntologyModel(OntModelSpec.OWL_DL_MEM, null);
 	// omodel.read(curl.toString());
 	//
 	// OntClass carbon = omodel.getOntClass(path);
 	// for(ExtendedIterator propIt=carbon.listDeclaredProperties(false);
 	// propIt.hasNext(); ) {
 	// OntProperty prop = (OntProperty)propIt.next();
 	// if(prop.isDatatypeProperty())
 	// System.out.println("DT PROP: "+prop.getURI());
 	// else if(prop.isObjectProperty())
 	// System.out.println("OT PROP: "+prop.getURI());
 	// else if(prop.isFunctionalProperty())
 	// System.out.println(" F PROP: "+prop.getURI());
 	// else
 	// System.out.println(" ? PROP: "+prop.getURI());
 	//
 	// for(NodeIterator nit = carbon.listPropertyValues(prop); nit.hasNext(); )
 	// {
 	// System.out.println(">>> "+((RDFNode)nit.next()).toString());
 	// }
 	// }
 	// }
 
 	// /**
 	// * Collects property values (constant ranges) for all OntClassContainer
 	// * 6.2.2007 moved to OntClassContainer, addProperty()
 	// */
 	// private void collectPropertyValues(OntModel omodel) {
 	// try {
 	// for(ExtendedIterator eit = omodel.listClasses(); eit.hasNext(); ) {
 	// OntClass ontClass = (OntClass)eit.next();
 	// for(ExtendedIterator propIt=ontClass.listDeclaredProperties(false);
 	// propIt.hasNext(); ) {
 	// OntProperty prop = (OntProperty)propIt.next();
 	// for(NodeIterator nit = ontClass.listPropertyValues(prop); nit.hasNext();
 	// ) {
 	// String value = ((RDFNode)nit.next()).toString();
 	// System.out.println("Class "+ontClass.getURI()+" has "+prop.getURI()+" with value "+value);
 	// //this.ontclassKB.get(ontClass.getURI()).addConstant(prop.getURI(),
 	// value);
 	// }
 	// }
 	// }
 	// }
 	// catch(com.hp.hpl.jena.ontology.ConversionException ce) {
 	// System.err.println("[e] collectPropertyValues: "+ce.getMessage());
 	// }
 	// }
 
 	// private void buildExample4CoordinateOntRestrictionProblem() {
 	//
 	// OntModel m = ModelFactory.createOntologyModel(OntModelSpec.OWL_DL_MEM,
 	// null);
 	//
 	// // m.setDynamicImports(true); // ?
 	// // m.setStrictMode(false); // -> [i] OnProperty:
 	// http://www.mindswap.org/2003/owl/geo/geoFeatures20040307.owl#hasCoordinateSystem,
 	// wrong ontology referenced :(
 	//
 	// m.read("http://www.mindswap.org/2003/owl/geo/geoCoordinateSystems.owl");
 	// // problems...
 	// //m.read("http://www.w3.org/TR/2003/PR-owl-guide-20031209/wine"); //
 	// workin...
 	//
 	// // === explicit import from namespace
 	// String[] defaultKeys = {"xsd", "rdf", "rdfs", "dc", "daml", "owl", ""};
 	// Arrays.sort(defaultKeys);
 	// for(Iterator it=m.getNsPrefixMap().keySet().iterator(); it.hasNext(); ) {
 	// String key = it.next().toString();
 	// if(Arrays.binarySearch(defaultKeys, key) < 0) {
 	// System.out.println("NS: "+key+" "+m.getNsPrefixURI(key));
 	// if(m.getNsPrefixURI(key).endsWith(".owl#")) {
 	// String ontName = m.getNsPrefixURI(key).split("#")[0];
 	// if(m.hasLoadedImport(ontName)) {
 	// System.out.println("NS: hasLoadedImport: "+ontName);
 	// }
 	// else {
 	// System.out.println("NS: IMPORT Ontology: "+ontName);
 	// m.read(ontName);
 	// }
 	// }
 	// }
 	// }
 	//
 	// //Reasoner reasoner = ReasonerRegistry.getOWLReasoner();
 	// PelletReasoner reasoner = new PelletReasoner();
 	// OntModelSpec spec = new OntModelSpec( OntModelSpec.OWL_DL_MEM );
 	// spec.setReasoner( reasoner );
 	// OntModel infModel = ModelFactory.createOntologyModel( spec,
 	// m.getBaseModel() );
 	//
 	//
 	// //OntClass exException =
 	// m.getOntClass("http://www.mindswap.org/2003/owl/geo/geoFeatures20040307.owl#SpatialDescription");
 	// OntClass exException =
 	// infModel.getOntClass("http://www.mindswap.org/2003/owl/geo/geoFeatures20040307.owl#SpatialDescription");
 	//
 	// OntClass exWorking =
 	// m.getOntClass("http://www.w3.org/TR/2003/PR-owl-guide-20031209/wine#Beaujolais");
 	// OntClass ex = exException;
 	//
 	// System.out.println("CLASS: "+ex.getURI());
 	// for(ExtendedIterator superclass_it = ex.listSuperClasses(false);
 	// superclass_it.hasNext(); )
 	// {
 	// OntClass superclass = (OntClass)superclass_it.next();
 	// System.out.println("[i] RDF-TYPE: "+superclass.getRDFType().getURI()); //
 	// Restriction or Class
 	// if(superclass.isRestriction()) {
 	// System.out.println("[i] OnProperty: "+superclass.asRestriction().getOnProperty().getURI());
 	// }
 	// }
 	// }
 	//
 	// private void buildExample4ListDomainProblem() {
 	// OntModel m = ModelFactory.createOntologyModel(OntModelSpec.OWL_DL_MEM,
 	// null);
 	// m.read("http://www.mindswap.org/2003/owl/geo/geoCoordinateSystems.owl");
 	//
 	// // 2 domain entries -> not working
 	// ObjectProperty cur_op =
 	// m.getObjectProperty("http://www.mindswap.org/2003/owl/geo/geoCoordinateSystems20040307.owl#hasCoordinateSystem");
 	//
 	// // 1 domain entry -> ok!
 	// //ObjectProperty cur_op =
 	// m.getObjectProperty("http://www.mindswap.org/2003/owl/geo/geoCoordinateSystems20040307.owl#hasCoordinateOrigin");
 	//
 	// Filter ontClassDropFilter = new Filter() {
 	// public boolean accept(Object o) {
 	// if( o instanceof OntClass ) {
 	// if( ((OntClass)o).isIntersectionClass() ||
 	// //((OntClass)o).isUnionClass() ||
 	// ((OntClass)o).isEnumeratedClass() ||
 	// ((OntClass)o).isComplementClass() ||
 	// ((OntClass)o).isRestriction() ) {
 	// return true;
 	// }
 	// }
 	// return false;
 	// }
 	// };
 	//
 	// if(cur_op.getDomain()!=null)
 	// {
 	// System.out.println("Proceccing domains.");
 	//
 	// // Bsp.: listDeclaringClasses,
 	// http://www.mindswap.org/2003/owl/geo/geoCoordinateSystems20040307.owl#
 	// ExtendedIterator eit_ldc =
 	// cur_op.listDeclaringClasses(true).filterDrop(ontClassDropFilter);
 	// while(eit_ldc.hasNext())
 	// {
 	// OntClass tempoc = (OntClass)eit_ldc.next();
 	// OntClassContainer curOntClassContainer = null;
 	//
 	// if(tempoc.isUnionClass()) {
 	// for(ExtendedIterator unionIt=tempoc.asUnionClass().listOperands();
 	// unionIt.hasNext(); ) {
 	// OntClass unionClass = (OntClass) unionIt.next();
 	// System.out.println("[i] OBJECT-PROPERTY (UnionClass) OP: "+cur_op.getLocalName());
 	// }
 	// }
 	// else {
 	// if(tempoc.getURI() == null) {
 	// System.out.println("[i] Unknown class for OP: "+cur_op.getURI());
 	// }
 	// else {
 	// System.out.println("[i] OBJECT-PROPERTY "+cur_op.getLocalName()+
 	// " for DECLARING CLASS "+tempoc.getURI());
 	// }
 	// }
 	// }
 	// }
 	// }
 
 	// /**
 	// * Test method for XsdSchemaGenerator class.
 	// * needs previous loaded owl type information
 	// */
 	// private static void testXSDGen() throws java.lang.Exception
 	// {
 	// XsdSchemaGenerator xsdgen = new XsdSchemaGenerator(
 	// "WSDLTYPE",
 	// true,
 	// 2,
 	// AbstractDatatype.InheritanceByNone,
 	// "http://www.w3.org/2001/XMLSchema#string"
 	// );
 	// // xsdgen.enableAnnotations();
 	// // xsdgen.enableOwlInformation();
 	//
 	// //
 	// AbstractDatatypeKB.getInstance().toXSD("http://127.0.0.1/ontology/StudentScenarioProtege.owl#Student",
 	// xsdgen, System.out);
 	// //
 	// AbstractDatatypeKB.getInstance().toXSD("http://127.0.0.1/ontology/Student.owl#MasterStudent",
 	// xsdgen, System.out);
 	// //
 	// AbstractDatatypeKB.getInstance().toXSD("http://127.0.0.1/ontology/Student.owl#ZipCode",
 	// xsdgen, System.out);
 	// //
 	// AbstractDatatypeKB.getInstance().toXSD("http://127.0.0.1/ontology/Student.owl#Student",
 	// xsdgen, System.out);
 	// //
 	// AbstractDatatypeKB.getInstance().toXSD("http://www.daml.org/services/owl-s/1.1/Process.owl#Process",
 	// xsdgen, System.out);
 	// //
 	// AbstractDatatypeKB.getInstance().toXSD("http://127.0.0.1/ontology/SUMO.owl#Man",
 	// xsdgen, System.out);
 	//
 	// //
 	// AbstractDatatypeKB.getInstance().getAbstractDatatypeKBData().get("http://127.0.0.1/ontology/portal.owl#Company").printDatatype();
 	// //
 	// AbstractDatatypeKB.getInstance().toXSD("http://127.0.0.1/ontology/portal.owl#Company",
 	// xsdgen, System.out);
 	//
 	// // // === Test Collection ===
 	// //
 	// AbstractDatatypeKB.getInstance().toXSD("http://www.daml.org/2001/10/html/zipcode-ont#ZipCode",
 	// xsdgen, System.out);
 	// //
 	// AbstractDatatypeKB.getInstance().toXSD("http://www.daml.org/2001/10/html/zipcode-ont#Association",
 	// xsdgen, System.out);
 	// //
 	// AbstractDatatypeKB.getInstance().toXSD("http://www.owl-ontologies.com/ex4mapping.owl#Student",
 	// xsdgen, System.out);
 	// //
 	// AbstractDatatypeKB.getInstance().toXSD("http://www.w3.org/TR/2003/PR-owl-guide-20031209/wine#Wine",
 	// xsdgen, System.out);
 	// //
 	// AbstractDatatypeKB.getInstance().toXSD("http://www.w3.org/TR/2003/PR-owl-guide-20031209/wine#RedWine",
 	// xsdgen, System.out);
 	// //
 	// AbstractDatatypeKB.getInstance().data.get("http://www.w3.org/TR/2003/PR-owl-guide-20031209/wine#WineGrape").removeAllProperties();
 	// //
 	// AbstractDatatypeKB.getInstance().toXSD("http://www.w3.org/TR/2003/PR-owl-guide-20031209/wine#Bordeaux",
 	// xsdgen, System.out);
 	// AbstractDatatypeKB.getInstance().toXSD("http://www.w3.org/TR/2003/PR-owl-guide-20031209/wine#StEmilion",
 	// xsdgen, System.out);
 	// //
 	// AbstractDatatypeKB.getInstance().toXSD("http://www.w3.org/TR/2003/PR-owl-guide-20031209/wine#NonFrenchWine",
 	// xsdgen, System.out);
 	// //
 	// AbstractDatatypeKB.getInstance().data.get("http://127.0.0.1/ontology/my_ontology.owl#ActionFilm").printDatatype();
 	// //
 	// AbstractDatatypeKB.getInstance().data.get("http://www.daml.org/services/owl-s/1.1/Process.owl#Process").printDatatype();
 	// //
 	// AbstractDatatypeKB.getInstance().getAbstractDatatypeKBData().getMeta("SufficientProcessUnion").printDatatype();
 	// //
 	// AbstractDatatypeKB.getInstance().data.get("http://www.daml.org/services/owl-s/1.1/Process.owl#OutputBinding").printDatatype();
 	// //
 	// AbstractDatatypeKB.getInstance().getAbstractDatatypeKBData().getMeta("SufficientOutputBindingIntersection").printDatatype();
 	// //
 	// AbstractDatatypeKB.getInstance().data.get("http://www.daml.org/services/owl-s/1.1/Process.owl#Participant").printDatatype();
 	// //
 	// AbstractDatatypeKB.getInstance().getAbstractDatatypeKBData().getMeta("schoolyearStudentCardinalityRestriction").printDatatype();
 	// //
 	// AbstractDatatypeKB.getInstance().data.get("http://127.0.0.1/ontology/SUMO.owl#UnitOfMeasure").printDatatype();
 	// //
 	// AbstractDatatypeKB.getInstance().data.get("http://127.0.0.1/ontology/ApothecaryOntology.owl#Patient").printDatatype();
 	// //
 	// AbstractDatatypeKB.getInstance().data.get("http://www.daml.org/services/owl-s/1.1/Process.owl#Process").printDatatype();
 	// //
 	// AbstractDatatypeKB.getInstance().data.getMeta("SufficientProcessUnion").printDatatype();
 	// //
 	// AbstractDatatypeKB.getInstance().data.get("http://127.0.0.1/ontology/books.owl#Newspaper").printDatatype();
 	// //
 	// AbstractDatatypeKB.getInstance().data.get("http://www.daml.org/2003/09/factbook/factbook-ont#Language").printDatatype();
 	// //
 	// AbstractDatatypeKB.getInstance().data.get("http://127.0.0.1/ontology/concept.owl#SupportedLanguage").printDatatype();
 	// //
 	// AbstractDatatypeKB.getInstance().data.get("http://127.0.0.1/ontology/Elements.owl#Carbon").printDatatype();
 	// //
 	// AbstractDatatypeKB.getInstance().toXSD("http://127.0.0.1/ontology/portal.owl#Serial-Publication",
 	// xsdgen, System.out);
 	// //
 	// AbstractDatatypeKB.getInstance().toXSD("http://127.0.0.1/ontology/ApothecaryOntology.owl#Patient",
 	// xsdgen, System.out);
 	// //
 	// AbstractDatatypeKB.getInstance().toXSD("http://www.daml.org/services/owl-s/1.1/Process.owl#Process",
 	// xsdgen, System.out);
 	// //
 	// AbstractDatatypeKB.getInstance().toXSD("http://127.0.0.1/ontology/books.owl#Newspaper",
 	// xsdgen, System.out);
 	// //
 	// AbstractDatatypeKB.getInstance().toXSD("http://127.0.0.1/ontology/concept.owl#SupportedLanguage",
 	// xsdgen, System.out);
 	// //
 	// AbstractDatatypeKB.getInstance().toXSD("http://127.0.0.1/ontology/Elements.owl#Carbon",
 	// xsdgen, System.out);
 	// //
 	// AbstractDatatypeKB.getInstance().toXSD("http://127.0.0.1/my_ontology.owl#Car",
 	// xsdgen, System.out);
 	// AbstractDatatypeKB.getInstance().toXSD("http://127.0.0.1/ontology/concept.owl#RecommendedPriceInEuro",
 	// xsdgen, System.out);
 	//
 	// // // === Scenario Student ===
 	// //
 	// AbstractDatatypeKB.getInstance().data.get("http://www.owl-ontologies.com/ex4mapping.owl#Student").printDatatype();
 	// //
 	// AbstractDatatypeKB.getInstance().data.get("http://www.owl-ontologies.com/ex4mapping.owl#HTWMasterStudent").printDatatype();
 	//
 	// }
 
 	/**
 	 * Get method for Pellet ontlogy model.
 	 * 
 	 * @return OntModel
 	 */
 	protected OntModel getPelletModel() {
 		return this.pelletModel;
 	}
 
 	// /**
 	// * Main method to test OWL DatatypeParser.
 	// */
 	// public static void main(String args[])
 	// {
 	// String path ="file:///D:/htw_kim/thesis/OWLS-MX/owls-tc2/ontology";
 	// String path2 ="file:///D:/development/owls/jena-tut5-ex.rdf";
 	// String path2ex
 	// ="file:/D:/htw_kim/thesis/OWLS-MX/owls-tc2/ontology/ActorDefault.owl";
 	// String path3ex
 	// ="file:/D:/htw_kim/thesis/OWLS-MX/owls-tc2/ontology/simplified_sumo.owl";
 	// String path4ex ="file:/D:/development/owls/zipcode-ont.owl";
 	// String path5ex
 	// ="file:/D:/development/owls/ontology/simplified_sumo2.owl";
 	// // dazu passend: http://www.xml-clearinghouse.de/standards/19/
 	// String path7a ="file:/D:/development/owls/ontology/wine.owl";
 	// String path7b ="http://localhost/ontology/wine.owl";
 	// String path8ex ="file:/D:/development/owls/ontology/Transportation.owl";
 	// String path9ex ="http://pedrorosa.home.sapo.pt/student.owl";
 	// String path10ex = "file:/D:/development/xsd/Student.owl";
 	//
 	// String path11ex = "file:/C:/Programme/Protege_3.1/test4owl2xsd.owl";
 	// String path12ex =
 	// "file:/C:/Programme/Protege_3.1/IntersectionTest2_savedInProtege.owl";
 	//
 	// // http://en.wikipedia.org/wiki/Suggested_Upper_Merged_Ontology
 	// // http://www.ontologyportal.org/
 	//
 	// final String ActorDefault = "http://127.0.0.1/ontology/ActorDefault.owl";
 	// final String ApothecaryOntology =
 	// "http://127.0.0.1/ontology/ApothecaryOntology.owl";
 	// final String books = "http://127.0.0.1/ontology/books.owl";
 	// final String concept = "http://127.0.0.1/ontology/concept.owl";
 	// final String Economy = "http://127.0.0.1/ontology/Economy.owl";
 	// final String Elements = "http://127.0.0.1/ontology/Elements.owl";
 	// final String EMAOntology = "http://127.0.0.1/ontology/EMAOntology.owl";
 	// final String EmergencyPhysicianOntology =
 	// "http://127.0.0.1/ontology/EmergencyPhysicianOntology.owl";
 	// final String Expression = "http://127.0.0.1/ontology/Expression.owl";
 	// final String extendedCamera =
 	// "http://127.0.0.1/ontology/extendedCamera.owl";
 	// final String finance_th_web =
 	// "http://127.0.0.1/ontology/finance_th_web.owl";
 	// final String food = "http://127.0.0.1/ontology/food.owl";
 	// final String geoCoordinateSystems =
 	// "http://127.0.0.1/ontology/geoCoordinateSystems.owl";
 	// final String geoCoordinateSystemsOrg =
 	// "http://www.mindswap.org/2003/owl/geo/geoCoordinateSystems20040307.owl";
 	//
 	// final String Geography = "http://127.0.0.1/ontology/Geography.owl";
 	// final String Government = "http://127.0.0.1/ontology/Government.owl";
 	//
 	// final String my_ontology = "http://127.0.0.1/ontology/my_ontology.owl";
 	// final String SUMO = "http://127.0.0.1/ontology/SUMO.owl";
 	// final String SUMO_FS =
 	// "file:/D:/htw_kim/thesis/OWLS-MX/owls-tc2/ontology/SUMO.owl";
 	// final String Midlevelontology =
 	// "http://127.0.0.1/ontology/Mid-level-ontology.owl";
 	//
 	// final String univbench = "http://127.0.0.1/ontology/univ-bench.owl";
 	// final String wine = "http://127.0.0.1/ontology/wine.owl";
 	//
 	// final String Student = "http://localhost/ontology/Student.owl";
 	// final String StudentMin = "http://localhost/ontology/StudentMin.owl";
 	// final String StudentScenario =
 	// "http://127.0.0.1/ontology/StudentScenario.owl";
 	// final String StudentScenarioProtege =
 	// "http://127.0.0.1/ontology/StudentScenarioProtege.owl";
 	//
 	// // owls2bpel examples
 	// final String GeoDataType =
 	// "http://www.laits.gmu.edu/geo/ontology/domain/GeoDataType.owl";
 	//
 	//
 	// String ex = wine;
 	// String fileName =
 	// ex.substring(ex.lastIndexOf("/")+1,ex.lastIndexOf("."))+"-MAP";
 	//
 	// String mode = "load";
 	// DatatypeParser p = new DatatypeParser();
 	//
 	// if(mode.equals("parse") || mode.equals("save"))
 	// {
 	// try {
 	// p.parse(ex);
 	//
 	// //p.buildExample4CoordinateOntRestrictionProblem();
 	// //p.buildExample4ListDomainProblem();
 	//
 	// //p.parsePropertyValues("http://127.0.0.1/ontology/Elements.owl#Carbon");
 	//
 	// //p.collectIndividuals("file:/D:/development/owls/ontology/wine.owl",
 	// // "file:/D:/development/owls/ontology/wine.owl#WineGrape");
 	//
 	// p.printCollectedData();
 	//
 	// // p.printErroneousData();
 	// //
 	// p.printCollectedData("http://www.w3.org/TR/2003/PR-owl-guide-20031209/wine#Wine");
 	// //
 	// p.printCollectedData("http://www.w3.org/TR/2003/PR-owl-guide-20031209/wine#NonFrenchWine");
 	// //
 	// p.printCollectedData("http://www.w3.org/TR/2003/PR-owl-guide-20031209/wine#Bordeaux");
 	// //
 	// p.printCollectedData("http://www.w3.org/TR/2003/PR-owl-guide-20031209/wine#StEmilion");
 	// //
 	// p.printCollectedData("file:/D:/htw_kim/thesis/OWLS-MX/owls-tc2/ontology/SUMO.owl#SetOrClass");
 	// //
 	// p.printCollectedData("file:/D:/htw_kim/thesis/OWLS-MX/owls-tc2/ontology/SUMO.owl#EuroDollar");
 	// //
 	// p.printCollectedData("file:/D:/htw_kim/thesis/OWLS-MX/owls-tc2/ontology/SUMO.owl#YearDuration");
 	// //
 	// p.printCollectedData("http://127.0.0.1/ontology/Mid-level-ontology.owl#LinguisticAttribute");
 	// // p.printCollectedData("http://www.w3.org/2002/07/owl#Thing");
 	// //
 	// p.printCollectedData("http://127.0.0.1/ontology/concept.owl#SupportedLanguage");
 	// // p.printCollectedData("http://127.0.0.1/ontology/Elements.owl#Carbon");
 	// }
 	// catch (Exception e) {
 	// e.printStackTrace();
 	// }
 	//
 	// p.getAbstractDatatypeKBData();
 	// //p.reset();
 	// }
 	// else if(mode.equals("load")) {
 	// AbstractDatatypeMapper.getInstance().loadAbstractDatatypeKB("file:/D:/tmp/KB/KB_"+fileName+".xml");
 	// }
 	// else {
 	// System.out.println("Mode not known.");
 	// }
 	//
 	// // TESTS
 	// if(mode.equals("load") || mode.equals("parse") || mode.equals("save")) {
 	// try {
 	// // AbstractDatatypeKB.getInstance().printFullStatus();
 	// // AbstractDatatypeKB.getInstance().printRegisteredDatatypes();
 	// // AbstractDatatypeKB.getInstance().printMetaDatatypes();
 	// testXSDGen();
 	// }
 	// catch(Exception e) {
 	// e.printStackTrace();
 	// }
 	// }
 	//
 	// //
 	// AbstractDatatypeKB.getInstance().data.get("http://www.w3.org/TR/2003/PR-owl-guide-20031209/wine#Wine").printDatatype();
 	// //
 	// AbstractDatatypeKB.getInstance().data.get("http://www.w3.org/TR/2003/PR-owl-guide-20031209/wine#NonFrenchWine").printDatatype();
 	// //
 	// AbstractDatatypeKB.getInstance().data.get("http://www.w3.org/TR/2003/PR-owl-guide-20031209/wine#StEmilion").printDatatype();
 	// //
 	// AbstractDatatypeKB.getInstance().data.getMeta("SufficientStEmilionIntersection").printDatatype();
 	//
 	// if(mode.equals("save")) {
 	// try {
 	// //String newfile =
 	// "D:\\tmp\\KB\\KB_"+ex.substring(ex.lastIndexOf("/")+1,ex.lastIndexOf("."))+".xml";
 	// FileOutputStream ausgabeStream = new
 	// FileOutputStream("D:\\tmp\\KB\\KB_"+fileName+".xml");
 	// AbstractDatatypeKB.getInstance().marshallAsXML(ausgabeStream, true);
 	// //System.out);
 	// }
 	// catch(Exception e) {
 	// System.out.println("Exception: "+e.toString());
 	// }
 	// }
 	// }
 }
