 /*
  * XsdSchemaGenerator.java
  *
  * Created on 27. Januar 2007, 12:02
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
 
 package de.dfki.dmas.owls2wsdl.core;
 
 // XSD generation
 import java.io.OutputStream;
 import java.io.OutputStreamWriter;
 import java.io.Writer;
 import java.util.ArrayList;
 import java.util.Enumeration;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.StringTokenizer;
 
 import org.exolab.castor.util.LocalConfiguration;
 import org.exolab.castor.xml.schema.Annotation;
 import org.exolab.castor.xml.schema.ComplexType;
 import org.exolab.castor.xml.schema.Documentation;
 import org.exolab.castor.xml.schema.ElementDecl;
 import org.exolab.castor.xml.schema.Facet;
 import org.exolab.castor.xml.schema.Group;
 import org.exolab.castor.xml.schema.Order;
 import org.exolab.castor.xml.schema.Particle;
 import org.exolab.castor.xml.schema.Schema;
 import org.exolab.castor.xml.schema.SchemaException;
 import org.exolab.castor.xml.schema.SchemaNames;
 import org.exolab.castor.xml.schema.SimpleType;
 import org.exolab.castor.xml.schema.SimpleTypesFactory;
 import org.exolab.castor.xml.schema.XMLType;
 import org.exolab.castor.xml.schema.writer.SchemaWriter;
 
 /**
  * Collects further code from AbstractDatatype and AbstractDatatypeKB
  * 
  * @author Oliver Fourman
  */
 public class XsdSchemaGenerator {
 
 	private Schema schema;
 	private boolean hack_01; // Use only one element declaration
 	private HashSet<String> hack_02; // For ABSTRACT elements reinit. hack01
 										// (more
 	// element declarations)
 	private HashSet<AbstractDatatype> renamedTypes; // temp. registry to lookup
 													// renamed types:
 	// +"Type"
 
 	private boolean useHierarchyPattern; // subclassing
 	// private boolean useAbstractSubstitution; // abstract superclasses
 	// private boolean useAnonRestrictionTypes; //
 	private boolean printOwlInformation; // to enhance matchmaking
 	private boolean printAnnotations; // standard xsd annotations
 
 	private int depth;
 	private String xsdInheritance;
 	private String DEFAULT_XSD;
 
 	private int thingNo = 1;
 
 	// types that could be deleted from schema due of name changes (+"Type")
 	private HashSet<ComplexType> obsoleteComplexTypes;
 
 	private static final String URI_THING = "http://www.w3.org/2002/07/owl#Thing";
 	private static final boolean DEBUGFLAG = false;
 
 	/**
 	 * Creates a new instance of XsdSchemaGenerator
 	 * 
 	 * @param schemaID
 	 *            Necessary in castors schema model
 	 * @param useHierarchyPattern
 	 *            Flag to enable the hierarchy pattern (xsd pattern)
 	 * @param depth
 	 *            Indicates the depth, that should be used to inherit elements
 	 *            from owl superclasses and intersections
 	 */
 	public XsdSchemaGenerator(String schemaID, boolean useHierarchyPattern,
 			int depth, String xsdInheritance) {
 		this.schema = new Schema("xsd", Schema.DEFAULT_SCHEMA_NS);
 		this.schema.setId(schemaID);
 		java.util.Date now = new java.util.Date();
 		this.schema.setVersion("OWLS2WSDL " + now);
 		this.schema.addNamespace("tns",
 				"http://schemas.dmas.dfki.de/venetianblind");
 		this.hack_01 = false;
 		this.hack_02 = new HashSet<String>();
 		this.renamedTypes = new HashSet<AbstractDatatype>();
 		this.useHierarchyPattern = useHierarchyPattern;
 		// TODO: find a better way to enforce the use of hierarchy
 		this.useHierarchyPattern = true;
 		this.printOwlInformation = false;
 		this.printAnnotations = false;
 		this.depth = depth;
 		if(this.depth == 0) {
 			this.depth = 10;
 		}
 		this.xsdInheritance = xsdInheritance;
 		this.DEFAULT_XSD = AbstractDatatype.DEFAULT_XSDTYPE;
 		this.obsoleteComplexTypes = new HashSet<ComplexType>();
 	}
 
 	/**
 	 * Creates a new instance of XsdSchemaGenerator
 	 * 
 	 * @param schemaID
 	 *            Necessary in castors schema model
 	 * @param useHierarchyPattern
 	 *            Flag to switch on the hierarchy pattern (xsd pattern)
 	 * @param depth
 	 *            Indicates the depth, that should be used to inherit elements
 	 *            from owl superclasses and intersections
 	 */
 	public XsdSchemaGenerator(String schemaID, boolean useHierarchyPattern,
 			int depth, String xsdInheritance, String xsdDefaultType) {
 		this(schemaID, useHierarchyPattern, depth, xsdInheritance);
 		this.DEFAULT_XSD = xsdDefaultType;
 	}
 
 	/**
 	 * Set flag to enable XML Schema hierarchy pattern.
 	 */
 	public void setHierarchyPattern(boolean flag) {
 		this.useHierarchyPattern = flag;
 	}
 
 	/**
 	 * Set flag to enable meta information about owl language constructs
 	 * (logic).
 	 */
 	public void enableOwlInformation() {
 		this.printOwlInformation = true;
 	}
 
 	public void disableOwlInformation() {
 		this.printOwlInformation = false;
 	}
 
 	public void enableAnnotations() {
 		this.printAnnotations = true;
 	}
 
 	public void disableAnnotations() {
 		this.printAnnotations = false;
 	}
 
 	public void setSchema(Schema schema) {
 		this.schema = schema;
 	}
 
 	public Schema getSchema() {
 		return this.schema;
 	}
 
 	/**
 	 * Helper method
 	 */
 	private String extractLocalName(String name) {
 		if (name.contains("#")) {
 			int index = name.indexOf("#");
 			return name.substring(index + 1);
 		}
 		return name;
 	}
 
 	public void printXSD(OutputStream out) {
 		Writer writer;
 		SchemaWriter sw;
 		try {
 			LocalConfiguration.getInstance().getProperties()
 					.setProperty("org.exolab.castor.indent", "true");
 			writer = new OutputStreamWriter(out, "UTF-8");
 			sw = new SchemaWriter(writer);
 			sw.write(this.schema);
 			LocalConfiguration.getInstance().getProperties()
 					.setProperty("org.exolab.castor.indent", "false");
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 	}
 
 	public void appendToSchema(AbstractDatatype curtype) throws Exception {
 		this.hack_01 = false;
 		if (this.schema.getElementDecl(curtype.getLocalName()) != null) {
 			System.out
 					.println("[xsdgen] appendToSchema, remove abstract attribute");
 			this.schema.getElementDecl(curtype.getLocalName()).setAbstract(
 					false);
 		}
 		this.toXSD(curtype);
 	}
 
 	private SimpleType addSimpleType(AbstractDatatype curtype, String elemName)
 			throws SchemaException {
 		String simpleTypeName = null;
 		if (curtype.getLocalName().equals(elemName)) { // in der Regel bei
 														// Parametertypen
 			simpleTypeName = curtype.getLocalName() + "Type";
 			this.renamedTypes.add(curtype);
 		} else {
 			simpleTypeName = curtype.getLocalName();
 		}
 
 		if (this.schema.getSimpleType(curtype.getLocalName()) != null) {
 			if (this.renamedTypes.contains(curtype)) {
 				this.schema.getSimpleType(curtype.getLocalName()).setName(
 						curtype.getLocalName() + "Type");
 				System.out
 						.println("[xsdgen] addSimpleType, rename and use existing type "
 								+ simpleTypeName);
 				return this.schema.getSimpleType(curtype.getLocalName()
 						+ "Type");
 			} else {
 				System.out.println("[xsdgen] addSimpleType, use existing type "
 						+ simpleTypeName);
 				return this.schema.getSimpleType(curtype.getLocalName());
 			}
 		} else if (this.schema.getSimpleType(curtype.getLocalName() + "Type") != null) {
 			if (this.renamedTypes.contains(curtype)) {
 				System.out
 						.println("[xsdgen] addSimpleType, use existing renamed type "
 								+ simpleTypeName);
 				return this.schema.getSimpleType(curtype.getLocalName()
 						+ "Type");
 			}
 		} else {
 			System.out.println("[xsdgen] addSimpleType, build and add type "
 					+ curtype);
 		}
 
 		String xsdTypeString = curtype.determineXsdType(this.xsdInheritance,
 				this.DEFAULT_XSD);
 		System.out.println("    XSD-DETERMINATION: " + xsdTypeString);
 		System.out.println("     this.DEFAULT_XSD: " + this.DEFAULT_XSD);
 
 		SimpleTypesFactory stf = new SimpleTypesFactory();
 		SimpleType baseType = stf.getBuiltInType(this
 				.extractLocalName(xsdTypeString));
 		SimpleType simpleAnyType = stf.getBuiltInType(this.schema
 				.getBuiltInTypeName(SimpleTypesFactory.ANYURI_TYPE));
 
 		if (baseType == null) {
 			System.out.println("[i] set baseType to anyType");
 			baseType = simpleAnyType;
 		}
 
 		SimpleType simpleType = null;
 
 		if (curtype.getIndividualRange().isEmpty()) {
 			System.out.println("[i] Build SIMPLE TYPE (" + simpleTypeName
 					+ ") only");
 			simpleType = this.schema.createSimpleType(simpleTypeName, baseType);
 		} else {
 			System.out.println("[i] Build SIMPLE TYPE (" + simpleTypeName
 					+ ") with restriction values.");
 			System.out.println("    Count of rangeTypes: "
 					+ curtype.getIndividualRangeTypes().size());
 
 			if (curtype.getIndividualRangeTypes().size() == 1) {
 				String dependencyTypeURL = curtype.getIndividualRangeTypes()
 						.get(0).toString();
 				if (curtype.getUrl().equals(dependencyTypeURL)) {
 					System.out
 							.println("[i] type equals enumeration type, set same baseType");
 					simpleType = this.schema.createSimpleType(simpleTypeName,
 							baseType);
 				} else {
 					System.out
 							.println("[i] Restriction enumeration type different from simple type");
 					if (AbstractDatatypeKB.getInstance()
 							.getAbstractDatatypeKBData()
 							.containsKey(dependencyTypeURL)) {
 						AbstractDatatype dependencyAbstractType = AbstractDatatypeKB
 								.getInstance().data.get(dependencyTypeURL);
 						String dtypeString = dependencyAbstractType
 								.determineXsdType(this.xsdInheritance,
 										this.DEFAULT_XSD);
 						if (dtypeString.equals(this.DEFAULT_XSD)) {
 							dtypeString = curtype.determineXsdType(
 									this.xsdInheritance, this.DEFAULT_XSD);
 						}
 						baseType = stf.getBuiltInType(this
 								.extractLocalName(dtypeString));
 						simpleType = this.schema.createSimpleType(
 								simpleTypeName, baseType);
 					} else { // type a, e.g. Thing, use simple type
 						simpleType = this.schema.createSimpleType(
 								simpleTypeName, baseType);
 					}
 				}
 			} else {
 				System.out
 						.println("[?] different type in restriction set found!");
 				simpleType = this.schema.createSimpleType(simpleTypeName,
 						simpleAnyType);
 			}
 
 			// SimpleType simpleTypeWithValues =
 			// this.schema.createSimpleType(simpleTypeName+"Data", simpleType);
 
 			for (Iterator<String> it = curtype.getIndividualRange().keySet()
 					.iterator(); it.hasNext();) {
 				String individualURI = it.next().toString();
 				String value = this.extractLocalName(individualURI);
 				simpleType.addFacet(new Facet(Facet.ENUMERATION, value));
 			}
 
 			// if(this.schema.getSimpleType(simpleTypeWithValues.getName()) ==
 			// null) {
 			// this.schema.addSimpleType(simpleTypeWithValues);
 			// }
 		}
 
 		// if(this.hack_02.contains(curtype.getLocalName())) {
 		// simpleType.setName(curtype.getLocalName()+"Type");
 		// }
 
 		if (this.schema.getSimpleType(simpleType.getName()) == null) {
 			this.schema.addSimpleType(simpleType);
 			System.out.println("[i] add simpleType " + simpleType.getName()
 					+ " (1)");
 		}
 		return simpleType;
 	}
 
 	private SimpleType addSimpleDATAType(AbstractDatatype curtype)
 			throws SchemaException {
 		System.out.println("           XSD for: " + curtype);
 		String xsdTypeString = curtype.determineXsdType(this.xsdInheritance,
 				this.DEFAULT_XSD);
 		System.out.println(" XSD-DETERMINATION: " + xsdTypeString);
 
 		SimpleTypesFactory stf = new SimpleTypesFactory();
 		SimpleType baseType = stf.getBuiltInType(this
 				.extractLocalName(xsdTypeString));
 		SimpleType simpleAnyType = stf.getBuiltInType(this.schema
 				.getBuiltInTypeName(SimpleTypesFactory.ANYURI_TYPE));
 
 		if (baseType == null) {
 			System.out.println("[i] set baseType to anyType");
 			baseType = simpleAnyType;
 		}
 
 		if (!curtype.getLocalName().equals(curtype.getBaseName())) {
 			if (this.schema.getSimpleType(curtype.getBaseName() + "DATA") == null) {
 				AbstractDatatype abstractBaseType = AbstractDatatypeKB
 						.getInstance().getAbstractDatatypeKBData()
 						.get(curtype.getUrl());
 				System.out
 						.println("[i] have to add SimpleDATAType for not yet existing basetype: "
 								+ abstractBaseType.toString());
 				this.addSimpleDATAType(abstractBaseType);
 			}
 			System.out.println("[i] getSimpleType: " + curtype.getBaseName()
 					+ "DATA");
 			baseType = this.schema
 					.getSimpleType(curtype.getBaseName() + "DATA");
 		}
 
 		System.out.println("[i] Build SIMPLE TYPE (" + curtype.getLocalName()
 				+ ") with restriction values.");
 		System.out.println("    Count of rangeTypes: "
 				+ curtype.getIndividualRangeTypes().size());
 		SimpleType simpleDATAType = null;
 
 		if (curtype.getIndividualRangeTypes().size() == 1) {
 			String dependencyTypeURL = curtype.getIndividualRangeTypes().get(0)
 					.toString();
 			if (curtype.getUrl().equals(dependencyTypeURL)) {
 				System.out
 						.println("[i] type equals enumeration type, set same baseType");
 				simpleDATAType = this.schema.createSimpleType(
 						curtype.getLocalName() + "DATA", baseType);
 			} else {
 				System.out
 						.println("[i] Restriction enumeration type different from simple type");
 				if (AbstractDatatypeKB.getInstance()
 						.getAbstractDatatypeKBData()
 						.containsKey(dependencyTypeURL)) {
 					AbstractDatatype dependencyAbstractType = AbstractDatatypeKB
 							.getInstance().data.get(dependencyTypeURL);
 					String dtypeString = dependencyAbstractType
 							.determineXsdType(this.xsdInheritance,
 									this.DEFAULT_XSD);
 					if (dtypeString.equals(this.DEFAULT_XSD)) {
 						dtypeString = curtype.determineXsdType(
 								this.xsdInheritance, this.DEFAULT_XSD);
 					}
 					baseType = stf.getBuiltInType(this
 							.extractLocalName(dtypeString));
 					simpleDATAType = this.schema.createSimpleType(
 							curtype.getLocalName() + "DATA", baseType);
 				} else { // type not in KB, e.g. Thing, use simple type
 					simpleDATAType = this.schema.createSimpleType(
 							curtype.getLocalName() + "DATA", baseType);
 				}
 			}
 		} else {
 			System.out.println("[?] different type in restriction set found!");
 			simpleDATAType = this.schema.createSimpleType(
 					curtype.getLocalName() + "DATA", simpleAnyType);
 		}
 
 		for (Iterator<String> it = curtype.getIndividualRange().keySet()
 				.iterator(); it.hasNext();) {
 			String individualURI = it.next().toString();
 			String value = this.extractLocalName(individualURI);
 			simpleDATAType.addFacet(new Facet(Facet.ENUMERATION, value));
 		}
 
 		if (this.schema.getSimpleType(simpleDATAType.getName()) == null) {
 			this.schema.addSimpleType(simpleDATAType);
 			System.out.println("[i] add simpleType " + simpleDATAType.getName()
 					+ " (1data)");
 		}
 		return simpleDATAType;
 	}
 
 	/**
 	 * Show used OWL terms. (experimental)
 	 */
 	private void createRestrictionEnvironment() throws SchemaException {
 		SimpleTypesFactory stf = new SimpleTypesFactory();
 		SimpleType anyType = stf.getBuiltInType(this.schema
 				.getBuiltInTypeName(SimpleTypesFactory.ANYURI_TYPE));
 		SimpleType nonNegativeIntegerType = stf
 				.getBuiltInType(this.schema
 						.getBuiltInTypeName(SimpleTypesFactory.NON_NEGATIVE_INTEGER_TYPE));
 
 		SimpleType OntProperty = this.schema.createSimpleType("OntProperty",
 				anyType);
 		SimpleType Cardinality = this.schema.createSimpleType("Cardinality",
 				nonNegativeIntegerType);
 		SimpleType MinCardinality = this.schema.createSimpleType(
 				"MinCardinality", nonNegativeIntegerType);
 		SimpleType MaxCardinality = this.schema.createSimpleType(
 				"MaxCardinality", nonNegativeIntegerType);
 		SimpleType DataLiteral = this.schema.createSimpleType("DataLiteral",
 				anyType);
 		this.schema.addSimpleType(OntProperty);
 		this.schema.addSimpleType(Cardinality);
 		this.schema.addSimpleType(MinCardinality);
 		this.schema.addSimpleType(MaxCardinality);
 		this.schema.addSimpleType(DataLiteral);
 
 		ElementDecl rdfsLiteral = new ElementDecl(this.schema, "rdfsLiteral");
 		rdfsLiteral.setType(anyType);
 
 		Documentation rdfsLiteralDoc = new Documentation();
 		rdfsLiteralDoc
 				.setSource("The class rdfs:Literal is the class of literal values such as strings and integers. Property values such as textual strings are examples of RDF literals. Literals may be plain or typed. A typed literal is an instance of a datatype class. This specification does not define the class of plain literals. rdfs:Literal is an instance of rdfs:Class. rdfs:Literal is a subclass of rdfs:Resource.");
 		Annotation rdfsLiteralAnnotation = new Annotation();
 		rdfsLiteralAnnotation.addDocumentation(rdfsLiteralDoc);
 		rdfsLiteral.addAnnotation(rdfsLiteralAnnotation);
 
 		ElementDecl datatypeID = new ElementDecl(this.schema, "datatypeID");
 		datatypeID.setType(anyType);
 
 		ElementDecl oneOfDataLiteral = new ElementDecl(this.schema,
 				"oneOfDataLiteral");
 		oneOfDataLiteral.setType(DataLiteral);
 		oneOfDataLiteral.setMaxOccurs(Particle.UNBOUNDED);
 
 		ComplexType DataRange = this.schema.createComplexType("DataRange");
 		Group dataRangeGroup = new Group();
 		dataRangeGroup.setOrder(Order.choice);
 		DataRange.addGroup(dataRangeGroup);
 		dataRangeGroup.addElementDecl(datatypeID);
 		dataRangeGroup.addElementDecl(rdfsLiteral);
 		dataRangeGroup.addElementDecl(oneOfDataLiteral);
 		this.schema.addComplexType(DataRange);
 
 		ElementDecl dataRangeElement = new ElementDecl(this.schema, "range");
 		dataRangeElement.setType(DataRange);
 		dataRangeElement.setMaxOccurs(Particle.UNBOUNDED);
 
 		ComplexType AllValuesFrom = this.schema
 				.createComplexType("AllValuesFrom");
 		Group allValuesFromGroup = new Group();
 		allValuesFromGroup.setOrder(Order.seq);
 		AllValuesFrom.addGroup(allValuesFromGroup);
 		allValuesFromGroup.addElementDecl(dataRangeElement);
 		this.schema.addComplexType(AllValuesFrom);
 
 		ComplexType SomeValuesFrom = this.schema
 				.createComplexType("SomeValuesFrom");
 		Group someValuesFromGroup = new Group();
 		someValuesFromGroup.setOrder(Order.seq);
 		SomeValuesFrom.addGroup(someValuesFromGroup);
 		someValuesFromGroup.addElementDecl(dataRangeElement);
 		this.schema.addComplexType(SomeValuesFrom);
 
 		ElementDecl dataLiteralRangeElement = new ElementDecl(this.schema,
 				"range");
 		dataLiteralRangeElement.setType(DataLiteral);
 
 		ComplexType HasValue = this.schema.createComplexType("HasValue");
 		Group hasValueGroup = new Group();
 		hasValueGroup.setOrder(Order.seq);
 		HasValue.addGroup(hasValueGroup);
 		hasValueGroup.addElementDecl(dataLiteralRangeElement);
 		this.schema.addComplexType(HasValue);
 
 		// ===
 		ElementDecl allValuesFromElement = new ElementDecl(this.schema,
 				"allValuesFrom");
 		allValuesFromElement.setType(AllValuesFrom);
 		ElementDecl someValuesFromElement = new ElementDecl(this.schema,
 				"someValuesFrom");
 		someValuesFromElement.setType(SomeValuesFrom);
 		ElementDecl hasValueElement = new ElementDecl(this.schema, "hasValue");
 		hasValueElement.setType(HasValue);
 		ElementDecl cardinalityElement = new ElementDecl(this.schema,
 				"cardinality");
 		cardinalityElement.setType(Cardinality);
 		ElementDecl minCardinalityElement = new ElementDecl(this.schema,
 				"minCardinality");
 		minCardinalityElement.setType(MinCardinality);
 		ElementDecl maxCardinalityElement = new ElementDecl(this.schema,
 				"maxCardinality");
 		maxCardinalityElement.setType(MaxCardinality);
 
 		ComplexType RestrictionComponent = this.schema
 				.createComplexType("RestrictionComponent");
 		Group rcGroup = new Group();
 		rcGroup.setOrder(Order.choice);
 		rcGroup.addElementDecl(allValuesFromElement);
 		rcGroup.addElementDecl(someValuesFromElement);
 		rcGroup.addElementDecl(hasValueElement);
 		rcGroup.addElementDecl(cardinalityElement);
 		rcGroup.addElementDecl(minCardinalityElement);
 		rcGroup.addElementDecl(maxCardinalityElement);
 		RestrictionComponent.addGroup(rcGroup);
 		this.schema.addComplexType(RestrictionComponent);
 
 		// ===
 
 		ElementDecl onPropertyElement = new ElementDecl(this.schema,
 				"onProperty");
 		onPropertyElement.setType(OntProperty);
 		ElementDecl restrictedByElement = new ElementDecl(this.schema,
 				"restrictedBy");
 		restrictedByElement.setType(RestrictionComponent);
 		restrictedByElement.setMaxOccurs(Particle.UNBOUNDED);
 
 		ComplexType Restriction = new ComplexType(this.schema, "Restriction");
 		Restriction.setAbstract(true);
 		Group restrictionGroup = new Group();
 		restrictionGroup.setOrder(Order.seq);
 		restrictionGroup.addElementDecl(onPropertyElement);
 		restrictionGroup.addElementDecl(restrictedByElement);
 		Restriction.addGroup(restrictionGroup);
 		this.schema.addComplexType(Restriction);
 	}
 
 	private void addSchemaAnnotation(String text) {
 		Annotation currentSchemaAnnotation = new Annotation();
 
 		// collect documentation from previous used types (Bugfix, noch nicht in
 		// Castor 1.1!)
 		@SuppressWarnings("unchecked")
 		Enumeration<Annotation> annotations = this.schema.getAnnotations();
 		while (annotations.hasMoreElements()) {
 			Annotation annotation = annotations.nextElement();
 			@SuppressWarnings("unchecked")
 			Enumeration<Documentation> docs = annotation.getDocumentation();
 			while (docs.hasMoreElements()) {
 				Documentation doc = docs.nextElement();
 				System.out.println("Annotation /COLLECT DOC: "
						+ doc.getSource());
 				currentSchemaAnnotation.addDocumentation(doc);
 			}
 			this.schema.removeAnnotation(annotation);
 		}
 
 		// NEW
 		Documentation currentSchemaDocumentation = new Documentation();
		currentSchemaDocumentation.setSource(text);
 		System.out.println("Annotation /NEW DOC: "
 				+ currentSchemaDocumentation.getSource());
 		currentSchemaAnnotation.addDocumentation(currentSchemaDocumentation);
 		this.schema.addAnnotation(currentSchemaAnnotation);
 	}
 
 	private Group addChoiceGroup(AbstractDatatype curtype, Group group)
 			throws SchemaException {
 		System.out.println("[xsdgen] ComplexType, CHOICE FALL-1: "
 				+ curtype.getUrl());
 		// choice
 		// +- element of simple-enum-type
 		// +- sequence group
 
 		Group choicegroup = new Group();
 		choicegroup.setOrder(Order.choice);
 
 		System.out.println("_________________________________________");
 		curtype.printDatatype();
 		System.out.println("BASENAME: " + curtype.getBaseName());
 		System.out.println("_________________________________________");
 
 		if (this.schema.getComplexType(curtype.getLocalName()) != null) {
 			if (this.schema.getComplexType(curtype.getLocalName())
 					.isRestricted()) {
 				System.out.println("RESTRICTED TYPE FOUND: "
 						+ this.schema.getComplexType(curtype.getLocalName())
 								.getBaseType().getName());
 			}
 
 			if (this.schema.getComplexType(curtype.getLocalName())
 					.isRedefined()) {
 				System.out.println("REDEFINED TYPE FOUND: "
 						+ this.schema.getComplexType(curtype.getLocalName())
 								.getReferenceId());
 			}
 		}
 
 		SimpleType simpleDATAType = this.addSimpleDATAType(curtype);
 
 		ElementDecl idElement = new ElementDecl(this.schema);
 		String idName = curtype.getBaseName() + "ID";
 		String firstLetter = String.valueOf(idName.charAt(0)).toLowerCase();
 		idName = firstLetter + idName.substring(1);
 		idElement.setName(idName);
 		idElement.setType(simpleDATAType);
 		idElement.setMinOccurs(1);
 		choicegroup.addElementDecl(idElement);
 
 		SimpleTypesFactory stf = new SimpleTypesFactory();
 		SimpleType simpleStringType = stf.getBuiltInType(this.schema
 				.getBuiltInTypeName(SimpleTypesFactory.STRING_TYPE));
 
 		// for choice types where instances are referenced by id/name
 		// we need an additional element name in sequence group
 		ElementDecl subEl_ID = new ElementDecl(this.schema);
 		subEl_ID.setName("name");
 		// subEl_ID.setTypeReference("xsd:string");
 		subEl_ID.setType(simpleStringType);
 		subEl_ID.setMinOccurs(0);
 		group.addElementDecl(subEl_ID);
 
 		choicegroup.addGroup(group);
 		return choicegroup;
 	}
 
 	private void markObsoleteComplexTypeForDeletion(ComplexType obsoleteType) {
 		if (!this.obsoleteComplexTypes.contains(obsoleteType)) {
 			this.obsoleteComplexTypes.add(obsoleteType);
 		}
 	}
 
 	public void deleteObsoleteTypesFromSchema() {
 		this.changeElementTypesOfObsoleteTypes();
 		for (Iterator<ComplexType> it = this.obsoleteComplexTypes.iterator(); it
 				.hasNext();) {
 			ComplexType obsoleteType = it.next();
 			String obsoleteTypeName = obsoleteType.getName();
 			if (this.schema.removeComplexType(obsoleteType)) {
 				System.out.println("[xsdgen] obsolete ComplexType ("
 						+ obsoleteTypeName + ") removed.");
 			}
 		}
 	}
 
 	private void changeElementTypesOfObsoleteTypes() {
 		@SuppressWarnings("unchecked")
 		Enumeration<ComplexType> cts = this.schema.getComplexTypes();
 		while (cts.hasMoreElements()) {
 			ComplexType complexType = cts.nextElement();
 			System.out.println("[xsdgen] process CT " + complexType.getName());
 			@SuppressWarnings("unchecked")
 			Enumeration<Object> particles = complexType.enumerate();
 			while (particles.hasMoreElements()) {
 				Object obj = particles.nextElement();
 				if (obj instanceof org.exolab.castor.xml.schema.Group) {
 					Group grp = (Group) obj;
 					@SuppressWarnings("unchecked")
 					Enumeration<Object> grpEnumeration = grp.enumerate();
 					while (grpEnumeration.hasMoreElements()) {
 						Object grpObj = grpEnumeration.nextElement();
 						if (grpObj instanceof org.exolab.castor.xml.schema.ElementDecl) {
 							ElementDecl elem = (ElementDecl) grpObj;
 							if (elem.getType() == null) {
 								System.out
 										.println("[xsdgen]   Type of element ("
 												+ elem.getName() + ") is null.");
 							} else {
 								String typeName = elem.getType().getName();
 								if (elem.getType().isComplexType()
 										&& this.obsoleteComplexTypes
 												.contains(elem.getType())) {
 									elem.setType(this.schema
 											.getComplexType(typeName + "Type"));
 									System.out.println("[xsdgen] "
 											+ elem.getName()
 											+ " type changed to (" + typeName
 											+ "Type)");
 								}
 							}
 						} else {
 							System.out.println("[?] unknown GRP ENUM-OBJ: "
 									+ grpObj.getClass().getName());
 						}
 					}
 				}
 			}
 		}
 	}
 
 	private SimpleType addSimpleThingType() throws SchemaException {
 		if (this.schema.getSimpleType("ThingType") != null) {
 			return this.schema.getSimpleType("ThingType");
 		}
 		SimpleTypesFactory stf = new SimpleTypesFactory();
 		// SimpleType anySimpleType = stf.getBuiltInType(
 		// this.schema.getBuiltInTypeName( SimpleTypesFactory.ANYSIMPLETYPE_TYPE
 		// ) );
 		SimpleType anyURIType = stf.getBuiltInType(this.schema
 				.getBuiltInTypeName(SimpleTypesFactory.ANYURI_TYPE));
 
 		SimpleType simpleThingType = this.schema.createSimpleType("ThingType",
 				anyURIType);
 
 		// In Eigenschaften Defaultwert f�r Thing einf�hren (future work)
 		// SimpleType defaultType =
 		// stf.getBuiltInType(this.extractLocalName(this.DEFAULT_XSD));
 		// SimpleType simpleThingType = this.schema.createSimpleType("Thing",
 		// defaultType);
 
 		this.schema.addSimpleType(simpleThingType);
 		System.out.println("[i] add SimpleThingType "
 				+ simpleThingType.getName() + " ("
 				+ simpleThingType.getBaseType().getName() + ")");
 		return simpleThingType;
 	}
 
 	private ComplexType addComplexType(AbstractDatatype curtype, String typeName)
 			throws SchemaException {
 		System.out.println("[xsdgen] addComplexType " + curtype.getLocalName()
 				+ ", " + typeName);
 
 		if (this.schema.getComplexType(typeName) != null) {
 			System.out.println("[xsdgen] use existing ComplexType " + typeName);
 			return this.schema.getComplexType(typeName);
 		}
 
 		System.out.println("[xsdgen] add ComplexType (" + typeName + ") for "
 				+ curtype.getUrl());
 
 		Group group = new Group();
 		group.setOrder(Order.seq);
 
 		ComplexType complexType = this.schema.createComplexType(typeName);
 		// new ComplexType(this.schema, typeName);
 
 		// CHECK FOR ADD. CHOICE CASES
 		// 1. elem has restricted range
 		// 2. type has given values
 		if (!curtype.getIndividualRange().isEmpty()) {
 			System.out.println("[i] add Choice + Sequence Group for "
 					+ curtype.getLocalName());
 			Group choicegroup = this.addChoiceGroup(curtype, group);
 			complexType.addGroup(choicegroup);
 		} else {
 			System.out.println("[i] add Sequence Group for "
 					+ curtype.getLocalName());
 			complexType.addGroup(group);
 		}
 
 		this.schema.addComplexType(complexType); // An der Stelle ins
 													// Schema-Modell setzen, um
 													// eine Rekursionsabbruch zu
 													// erzielen.
 
 		/**
 		 * PROPERTIES
 		 */
 		System.out.println("\n*********** " + "Properties for "
 				+ curtype.getLocalName() + " *********\n");
 
 		Iterator<AbstractDatatypeElement> pit = curtype.getProperties(depth)
 				.iterator();
 		while (pit.hasNext()) {
 			AbstractDatatypeElement elem = pit.next();
 			System.out.println("=========================================");
 			System.out.println("[xsdgen] AbstractDatatypeElement: "
 					+ elem.toString());
 			elem.printData();
 			System.out.println("=========================================");
 
 			ElementDecl subEl = new ElementDecl(this.schema);
 
 			if (elem.getMaxOccurs() == 0) {
 				subEl.setMaxOccurs(0);
 			} else if (elem.getMaxOccurs() > 1) {
 				subEl.setMaxOccurs(elem.getMaxOccurs());
 			}
 
 			if (elem.getMinOccurs() == 0) {
 				subEl.setMinOccurs(0);
 			} else if (elem.getMinOccurs() > 1) {
 				subEl.setMinOccurs(elem.getMinOccurs());
 			}
 
 			if (!elem.getRestrictions().isEmpty() && this.printAnnotations) {
 				for (Iterator<AbstractDatatypeRestrictionElement> it = elem
 						.getRestrictions().iterator(); it.hasNext();) {
 					AbstractDatatypeRestrictionElement resElem = it.next();
 					Annotation annotation = new Annotation();
 					Documentation doc = new Documentation();
 					doc.setSource("OWL Restriction "
 							+ resElem.getRestrictionType() + "="
 							+ resElem.getRestrictionValue());
 					annotation.addDocumentation(doc);
 					subEl.addAnnotation(annotation);
 				}
 			}
 
 			if (elem.isPrimitive()) {
 				System.out.println("[xsdgen] " + elem.getLocalName()
 						+ " Type: " + elem.getType());
 				SimpleTypesFactory stf = new SimpleTypesFactory();
 				SimpleType baseType = null;
 				baseType = stf.getBuiltInType(this.extractLocalName(elem
 						.getType()));
 				if (baseType == null) {
 					baseType = stf
 							.getBuiltInType(this.schema
 									.getBuiltInTypeName(SimpleTypesFactory.ANYURI_TYPE));
 				}
 				subEl.setName(elem.getLocalName());
 				subEl.setType(baseType);
 				if (group.getElementDecl(subEl.getName()) == null) {
 					group.addElementDecl(subEl);
 				}
 
 				if (elem.getRestrictedRange().size() == 1) {
 					subEl.setFixedValue(elem.getRestrictedRange().get(0)
 							.toString());
 				}
 			} else if (elem.getOwlSource().equals("DATATYPE")
 					|| elem.getOwlSource().equals("RESTRICTION-ON-DATATYPE")
 					|| elem.getOwlSource().equals("RESTRICTION")) {
 
 				// TESTE, OB XSDTYPE GESETZT, ODER �BER HIERARCHY GEERBT WERDEN
 				// KANN. -> SIMPLE TYPE
 				// WENN KEIN XSDTYPE GESETZT UND PROPERTIES �BER DEPTH ANGEZOGEN
 				// WERDEN -> COMPLEX TYPE
 
 				System.out.println("[i] [simple] subEl.setName: "
 						+ elem.getLocalName() + " and SIMPLE-Type: "
 						+ elem.getType());
 
 				SimpleTypesFactory stf = new SimpleTypesFactory();
 				SimpleType baseType = null;
 
 				baseType = stf.getBuiltInType(this.extractLocalName(elem
 						.getType()));
 
 				if (baseType == null) {
 					baseType = stf
 							.getBuiltInType(this.schema
 									.getBuiltInTypeName(SimpleTypesFactory.ANYURI_TYPE));
 
 					subEl.setName(elem.getLocalName());
 
 					// Try to get baseType over Type definition in KB
 					if (AbstractDatatypeKB.getInstance()
 							.getAbstractDatatypeKBData()
 							.containsKey(elem.getType())) {
 						System.out
 								.println("DATATYPE PROPERTY WITH COMPLEX DATATYPE FOUND.");
 						if (AbstractDatatypeKB.getInstance()
 								.getAbstractDatatypeKBData()
 								.get(elem.getType()).getXsdType() != null) {
 							String xsdtype = AbstractDatatypeKB.getInstance()
 									.getAbstractDatatypeKBData()
 									.get(elem.getType()).getXsdType();
 							System.out
 									.println("SEMI-AUTOMATIC SET TYPE FOUND: "
 											+ xsdtype);
 							baseType = stf.getBuiltInType(this
 									.extractLocalName(xsdtype));
 						}
 					}
 					else {
 						StringTokenizer st = new StringTokenizer(
 								elem.getType(), "!");
 						String firstWord = st.nextToken();
 						if (firstWord.equals("enumeration")) {
 							subEl.setName(elem.getLocalName());
 							baseType = this.schema.getSimpleType(elem
 									.getLocalName() + "Type");
 							if (baseType == null) {
 								System.out
 										.println("This type is an enumeration, getting elements...");
 								List<Facet> values = new LinkedList<Facet>();
 								String internalType = null;
 								while (st.hasMoreTokens()) {
 									String literal = st.nextToken();
 									StringTokenizer literalParts = new StringTokenizer(
 											literal, "^^");
 									values.add(new Facet("enumeration",
 											literalParts.nextToken()));
 									if (internalType == null) {
 										internalType = literalParts.nextToken();
 									}
 								}
 								baseType = this.schema
 										.createSimpleType(
 												elem.getLocalName() + "Type",
 												stf.getBuiltInType(this
 														.extractLocalName(internalType)));
 								Iterator<Facet> it = values.iterator();
 								while (it.hasNext()) {
 									baseType.addFacet(it.next());
 								}
 								this.schema.addSimpleType(baseType);
 								if (group.getElementDecl(subEl.getName()) == null) {
 									group.addElementDecl(subEl);
 								}
 							}
 						}
 					}
 				}
 
 				// FIXED
 				if (elem.getRestrictedRange().size() == 1) {
 					// HACK...
 					if (elem.getName().contains("Cardinality")
 							|| elem.getName().contains("MinCardinality")
 							|| elem.getName().contains("MaxCardinality")) {
 						baseType = stf
 								.getBuiltInType(stf
 										.getBuiltInTypeName(SimpleTypesFactory.NON_NEGATIVE_INTEGER_TYPE));
 					}
 
 					subEl.setType(baseType);
 
 					subEl.setFixedValue(elem.getRestrictedRange().get(0)
 							.toString());
 				}
 				// RESTRICTED SIMPLE TYPE
 				else if (elem.getRestrictedRange().size() >= 1) {
 					SimpleType simpleType = this.schema.createSimpleType(
 							elem.getLocalName() + "Type", baseType);
 					for (Iterator<String> it = elem.getRestrictedRange()
 							.iterator(); it.hasNext();) {
 						String rangeURI = it.next().toString();
 						System.out.println("[XSD GEN] RANGE: " + rangeURI);
 						String value = this.extractLocalName(rangeURI);
 						simpleType
 								.addFacet(new Facet(Facet.ENUMERATION, value));
 					}
 					this.schema.addSimpleType(simpleType);
 					System.out.println("[i] add simpleType "
 							+ simpleType.getName() + " (2)");
 					subEl.setType(simpleType);
 				} else {
 					subEl.setType(baseType);
 				}
 
 				if (group.getElementDecl(subEl.getName()) == null) {
 					group.addElementDecl(subEl);
 				}
 			} else if (elem.getOwlSource().equals("UNION")
 					|| elem.getOwlSource().equals("INTERSECTION")) {
 				if (this.printOwlInformation) {
 					SimpleTypesFactory stf = new SimpleTypesFactory();
 					System.out.println("[xsdgen] [meta] subEl.setName: "
 							+ elem.getLocalName() + " and META-Type: "
 							+ elem.getType());
 
 					subEl.setName(elem.getLocalName());
 					subEl.setMinOccurs(0);
 					subEl.setMaxOccurs(0);
 
 					// subEl.setNillable(true);
 
 					if (AbstractDatatypeKB.getInstance()
 							.getAbstractDatatypeKBData()
 							.getMeta(elem.getType()).getProperties().isEmpty()) {
 						System.out.println("[xsdgen] [meta] SimpleType");
 						SimpleType simpleType = stf
 								.getBuiltInType(this.schema
 										.getBuiltInTypeName(SimpleTypesFactory.ANYURI_TYPE));
 						subEl.setType(simpleType);
 					} else {
 						// specify/init base types
 						if (elem.getOwlSource().equals("UNION")) {
 							if (this.schema.getComplexType("Union") == null) {
 								ComplexType unionType = this.schema
 										.createComplexType("Union");
 								unionType.setAbstract(true);
 								this.schema.addComplexType(unionType);
 							}
 						} else if (elem.getOwlSource().equals("INTERSECTION")) {
 							if (this.schema.getComplexType("Intersection") == null) {
 								ComplexType intersectionType = this.schema
 										.createComplexType("Intersection");
 								intersectionType.setAbstract(true);
 								this.schema.addComplexType(intersectionType);
 							}
 						} else {
 							System.out
 									.println("[xsdgen] [meta] can't create base type");
 						}
 
 						//
 						// build Meta ComplexType (Intersection or Union with
 						// elements)
 						//
 						AbstractDatatype aMetaType = AbstractDatatypeKB
 								.getInstance().getAbstractDatatypeKBData()
 								.getMeta(elem.getType());
 						System.out.println("[xsdgen] [meta] add ComplexType "
 								+ aMetaType.getLocalName());
 
 						ComplexType subMetaType = this.addComplexType(
 								aMetaType, aMetaType.getLocalName());
 
 						// set base type
 						if (elem.getOwlSource().equals("UNION")) {
 							subMetaType.setComplexContent(true);
 							subMetaType
 									.setDerivationMethod(SchemaNames.EXTENSION); // hier
 																					// werden
 																					// ja
 																					// Elemente
 																					// hinzugef�gt.
 							subMetaType.setRestriction(false);
 							subMetaType.setBase("Union");
 							subMetaType.setBaseType(this.schema
 									.getComplexType("Union"));
 						} else if (elem.getOwlSource().equals("INTERSECTION")) {
 							subMetaType.setComplexContent(true);
 							subMetaType
 									.setDerivationMethod(SchemaNames.EXTENSION); // hier
 																					// werden
 																					// ja
 																					// Elemente
 																					// hinzugef�gt.
 							subMetaType.setRestriction(false);
 							subMetaType.setBase("Intersection");
 							subMetaType.setBaseType(this.schema
 									.getComplexType("Intersection"));
 						}
 
 						subEl.setType(subMetaType);
 
 						if (this.printAnnotations) {
 							Annotation annotation = new Annotation();
 							Documentation doc = new Documentation();
 							doc.setSource("OWL Class " + aMetaType.getUrl());
 							annotation.addDocumentation(doc);
 							subMetaType.addAnnotation(annotation);
 						}
 
 						if (elem.getRestrictedRange().isEmpty()) {
 							System.out
 									.println("META has no RestrictedRange, element has specified value(s) and no more subproperties ");
 						} else {
 							System.out.println("META has RestrictedRange!");
 						}
 					}
 				}
 			} else if (elem.getOwlSource().equals("INTERSECTION-RESTRICTION")
 					|| elem.getOwlSource().equals("UNION-RESTRICTION")) {
 				// Complex Type with 2 elements: onProperty(OntProperty),
 				// restrictedBy(RestrictionComponent)
 
 				SimpleTypesFactory stf = new SimpleTypesFactory();
 				SimpleType anyType = stf.getBuiltInType(this.schema
 						.getBuiltInTypeName(SimpleTypesFactory.ANYURI_TYPE));
 
 				if (this.schema.getComplexType("Restriction") == null) {
 					this.createRestrictionEnvironment();
 				}
 
 				AbstractDatatype metatype = AbstractDatatypeKB.getInstance().data
 						.getMeta(elem.getType());
 				Group restrictionGroup = new Group();
 				restrictionGroup.setOrder(Order.all);
 
 				// 1.3.2007: No specialization pattern possible at this time,
 				// use additional element.
 				// 11.4.: Rel�st :) base type m�glich.
 				// ElementDecl isRestrictionElement = new
 				// ElementDecl(this.schema, "isRestriction");
 				// isRestrictionElement.setType(this.schema.getComplexType("Restriction"));
 				// restrictionGroup.addElementDecl(isRestrictionElement);
 
 				if (this.schema.getComplexType(metatype.getLocalName()) == null) {
 					for (Iterator<AbstractDatatypeElement> metaIt = metatype
 							.getProperties().iterator(); metaIt.hasNext();) {
 						AbstractDatatypeElement atypeElem = metaIt.next();
 						if (atypeElem.getOwlSource().equals("ONPROPERTY")) {
 							// SimpleType onPropertyType =
 							// this.schema.createSimpleType(this.extractLocalName(atypeElem.getType()),
 							// anyType);
 							ElementDecl metaEl = new ElementDecl(this.schema,
 									"onProperty");
 							metaEl.setType(anyType);
 							metaEl.setFixedValue(atypeElem.getName());
 							restrictionGroup.addElementDecl(metaEl);
 						} else {
 							ElementDecl metaEl = new ElementDecl(this.schema,
 									atypeElem.getLocalName());
 
 							if (atypeElem.getType().equals("Cardinality")) {
 								metaEl.setType(this.schema
 										.getSimpleType("Cardinality"));
 							} else {
 								if (this.schema.getSimpleType(atypeElem
 										.getLocalTypeName()) != null) {
 									metaEl.setType(this.schema
 											.getSimpleType(atypeElem
 													.getLocalTypeName()));
 								} else if (this.schema.getComplexType(atypeElem
 										.getLocalTypeName()) != null) {
 									metaEl.setType(this.schema
 											.getComplexType(atypeElem
 													.getLocalTypeName()));
 								} else {
 									SimpleType componentType = this.schema
 											.createSimpleType(atypeElem
 													.getLocalTypeName(),
 													anyType);
 									metaEl.setType(componentType);
 								}
 							}
 
 							if (!atypeElem.getRestrictedRange().isEmpty()) {
 								metaEl.setFixedValue(atypeElem
 										.getRestrictedRange().get(0).toString());
 							}
 							restrictionGroup.addElementDecl(metaEl);
 							// for(Iterator
 							// facetIt=atypeElem.getRestrictedRange().iterator();
 							// facetIt.hasNext(); ) {
 							// componentType.addFacet(new
 							// Facet(Facet.ENUMERATION,
 							// facetIt.next().toString()));
 							// }
 						}
 					}
 
 					ComplexType subMetaCT = this.schema
 							.createComplexType(metatype.getLocalName());
 					subMetaCT.setComplexContent(true);
 					subMetaCT.setDerivationMethod(SchemaNames.RESTRICTION); // EXTENSION
 																			// !?
 					subMetaCT.setRestriction(true);
 					subMetaCT.setBase("Restriction");
 					subMetaCT.setBaseType(this.schema.getType("Restriction"));
 					subMetaCT.addGroup(restrictionGroup);
 
 					this.schema.addComplexType(subMetaCT);
 				}
 
 				subEl.setName(elem.getLocalName());
 				subEl.setType(this.schema.getComplexType(metatype
 						.getLocalName()));
 			} else {
 				// OBJECT.PROPERTY -> COMPLEX.TYPE
 				// INTERSECTION-TYPE, INTERSECTION-CLASS
 
 				System.out.println("[i] [complex] subEl.setName: "
 						+ elem.getLocalName() + " and Type: "
 						+ elem.getLocalTypeName());
 				subEl.setName(elem.getLocalName());
 
 				if (AbstractDatatypeKB.getInstance()
 						.getAbstractDatatypeKBData()
 						.containsKey(elem.getType())) {
 					System.out.println("REGISTERED DATATYPE FROM KB: "
 							+ elem.getType());
 
 					if (elem.getOwlSource().equals("UNIONCLASS")) {
 						System.out.println("UNIONCLASS!");
 						if (this.printAnnotations) {
 							Annotation annotation = new Annotation();
 							Documentation doc = new Documentation();
 							doc.setSource("unionclass (unionOf)");
 							annotation.addDocumentation(doc);
 							subEl.addAnnotation(annotation);
 						}
 					}
 
 					// check if element is another ComplexType (recursion)
 
 					// 1st: No more properties ~> SimpleType
 					if (AbstractDatatypeKB.getInstance().data
 							.get(elem.getType()).getProperties(this.depth)
 							.isEmpty()) {
 						// searching for meta information in restriction list
 						for (Iterator<AbstractDatatypeRestrictionElement> it = elem
 								.getRestrictions().iterator(); it.hasNext();) {
 							AbstractDatatypeRestrictionElement restriction = it
 									.next();
 							if (restriction.getRestrictionType().equals(
 									"isFunctional")) {
 								subEl.setMinOccurs(0);
 								subEl.setMaxOccurs(1);
 								if (this.printAnnotations) {
 									Annotation annotation = new Annotation();
 									Documentation doc = new Documentation();
 									doc.setSource("isFunctional"); // ,
 																	// inherited
 																	// by
 																	// "+restriction.getInheritedBy());
 									annotation.addDocumentation(doc);
 									subEl.addAnnotation(annotation);
 								}
 							}
 							if (restriction.getRestrictionType().equals(
 									"isTransitive")) {
 								if (this.printAnnotations) {
 									Annotation annotation = new Annotation();
 									Documentation doc = new Documentation();
 									doc.setSource("isTransitive");
 									annotation.addDocumentation(doc);
 									subEl.addAnnotation(annotation);
 								}
 							}
 						}
 
 						SimpleTypesFactory stf = new SimpleTypesFactory();
 						// SimpleType simpleAnyType = stf
 						// .getBuiltInType(this.schema
 						// .getBuiltInTypeName(SimpleTypesFactory.ANYURI_TYPE));
 						SimpleType simpleType = null;
 
 						//
 						// Check if we must create an element specific
 						// SimpleType (element restrictions!)
 						//
 						if (!elem.getRestrictedRange().isEmpty()) {
 							// element has specific value(s) and no more
 							// subproperties
 							// ~> do not run recursion! build SimpleType with
 							// element restrictions
 							System.out
 									.println("[xsdgen] SimpleType with restricted range: "
 											+ curtype.getLocalName()
 											+ " ("
 											+ curtype.getUrl() + ")");
 
 							String restrictedSimpleTypeName = curtype
 									.getLocalName() + elem.getLocalTypeName(); // elem.getLocalName()+elem.getLocalTypeName();
 																				// //
 																				// StEmilionSpecificType
 							System.out.println("[xsdgen] New SimpleType name: "
 									+ restrictedSimpleTypeName);
 
 							// Pattern: baseType direkt von der restriktierten
 							// Klasse ableiten
 							AbstractDatatype abstractBaseType = AbstractDatatypeKB
 									.getInstance().getAbstractDatatypeKBData()
 									.get(elem.getType());
 							SimpleType baseType = this.addSimpleType(
 									abstractBaseType, null);
 							// Zweiter Ansatz: baseType der zu restriktierenden
 							// Klasse �bernehmen
 							if (baseType == null) {
 								String primitiveBaseTypeString = AbstractDatatypeKB
 										.getInstance()
 										.getAbstractDatatypeKBData()
 										.get(elem.getType())
 										.determineXsdType(this.xsdInheritance,
 												this.DEFAULT_XSD);
 								baseType = stf
 										.getBuiltInType(this
 												.extractLocalName(primitiveBaseTypeString));
 							}
 
 							simpleType = this.schema.createSimpleType(
 									restrictedSimpleTypeName, baseType);
 
 							for (Iterator<String> it = elem
 									.getRestrictedRange().iterator(); it
 									.hasNext();) {
 								// System.out.println("[XSD GEN] RANGE: "+elem.getRestrictedRange().get(i).toString());
 								// String value =
 								// this.extractLocalName(elem.getRestrictedRange().get(i).toString());
 								String rangeURI = it.next().toString();
 								System.out.println("[xsdgen] RANGE: "
 										+ rangeURI);
 								String value = this.extractLocalName(rangeURI);
 								simpleType.addFacet(new Facet(
 										Facet.ENUMERATION, value));
 							}
 
 							if (this.schema.getSimpleType(simpleType.getName()) == null) {
 								this.schema.addSimpleType(simpleType);
 								System.out.println("[i] addSimpleType "
 										+ simpleType.getName() + " (3)");
 								/*
 								 * if(this.useHierarchyPattern) { ElementDecl
 								 * baseTypeElDec = new ElementDecl(this.schema,
 								 * baseType.getName());
 								 * baseTypeElDec.setAbstract(true);
 								 * this.schema.addElementDecl(baseTypeElDec);
 								 * ElementDecl simpleTypeElDec = new
 								 * ElementDecl(this.schema,
 								 * simpleType.getName());
 								 * simpleTypeElDec.setSubstitutionGroup
 								 * (baseType.getName());
 								 * this.schema.addElementDecl(simpleTypeElDec);
 								 * }
 								 */
 							}
 							subEl.setType(simpleType);
 						} else {
 							// element has no specific value(s) and no more
 							// subproperties
 							// ~> do not run recursion! build SimpleType with
 							// possible type range values
 							// *) Bemerkung: vorher war ein Rekursionslauf
 							// n�tig, jetzt Abfrage �ber KB.
 							AbstractDatatype curelemtype = AbstractDatatypeKB
 									.getInstance().data.get(elem.getType());
 							System.out.println("[xsdgen] SimpleType "
 									+ curelemtype.getUrl());
 							simpleType = this.addSimpleType(curelemtype,
 									subEl.getName());
 							subEl.setType(simpleType);
 						}
 					} else { // ComplexType
 						System.out.println("[i] ComplexType "
 								+ elem.getLocalName() + " (" + elem.getType()
 								+ ")");
 
 						AbstractDatatype type = AbstractDatatypeKB
 								.getInstance().getAbstractDatatypeKBData()
 								.get(elem.getType());
 						System.out.println("TYPE                     : "
 								+ type.toString());
 						System.out.println("ANZAHL INDIVUDALS VON TYP: "
 								+ type.getIndividualRange().keySet().size());
 						System.out.println("RESTRICTED RANGE         : "
 								+ elem.getRestrictedRange().size());
 
 						ComplexType subCT = null;
 						System.out.println("[xsdgen] add new ComplexType ("
 								+ type.getLocalName() + ") for sub element.");
 						if (type.getLocalName().equals(subEl.getName())) {
 							subCT = this.addComplexType(type,
 									type.getLocalName() + "Type");
 						} else {
 							subCT = this.addComplexType(type,
 									type.getLocalName());
 						}
 
 						if (elem.getRestrictedRange().isEmpty()) {
 							subEl.setType(subCT);
 						} else {
 							AbstractDatatype subtype = new AbstractDatatype(
 									type); // CopyConstructor
 
 							// subCT is basetype
 							System.out
 									.println("[xsdgen] ComplexType with restricted range: "
 											+ curtype.getLocalName()
 											+ " ("
 											+ curtype.getUrl() + ")");
 
 							String subTypeName = curtype.getLocalName()
 									+ type.getLocalName();
 							System.out.println("[i] Neuer Subtype: "
 									+ subTypeName);
 							subtype.setLocalName(subTypeName);
 							subtype.removeAllRanges();
 							for (int i = 0; i < elem.getRestrictedRange()
 									.size(); i++) {
 								subtype.addIndividualRange(
 										elem.getRestrictedRange().get(i)
 												.toString(), elem.getType());
 							}
 
 							ComplexType subComplexType = this.addComplexType(
 									subtype, subtype.getLocalName());
 							subComplexType.setBase(type.getLocalName());
 							subComplexType.setBaseType(subCT);
 							subComplexType
 									.setDerivationMethod(SchemaNames.RESTRICTION);
 							System.out
 									.println("[xsdgen] NEW restricted Sub ComplexType for element added: "
 											+ subComplexType.getName());
 							subEl.setType(subComplexType);
 						}
 
 						if (elem.getMinOccurs() == -1
 								&& elem.getMaxOccurs() == -1
 								&& !elem.getOwlSource().equals(
 										"INTERSECTION-TYPE")) {
 							subEl.setMaxOccurs(Particle.UNBOUNDED);
 						}
 					}
 				} else {
 					System.out.println("NOT IN KB: " + elem.getType()
 							+ " Build THING ...");
 					SimpleType simpleType = null;
 
 					if (!elem.getRestrictedRange().isEmpty()) {
 						// element has specific value(s) and no more
 						// subproperties
 						// ~> build SimpleType Thing with element restrictions!
 						System.out
 								.println("[XSD GEN] SimpleType for Restriction.");
 
 						SimpleTypesFactory stf = new SimpleTypesFactory();
 						SimpleType baseType = stf
 								.getBuiltInType(this.DEFAULT_XSD);
 
 						if (elem.getLocalTypeName().equals("Thing")) {
 							simpleType = this.schema.createSimpleType(
 									elem.getLocalTypeName()
 											+ Integer.toString(thingNo++),
 									baseType);
 						} else {
 							simpleType = this.schema.createSimpleType(
 									elem.getLocalTypeName(), baseType);
 						}
 						for (Iterator<String> it = elem.getRestrictedRange()
 								.iterator(); it.hasNext();) {
 							// System.out.println("[XSD GEN] RANGE: "+elem.getRestrictedRange().get(i).toString());
 							// String value =
 							// this.extractLocalName(elem.getRestrictedRange().get(i).toString());
 							String rangeURI = it.next().toString();
 							System.out.println("[XSD GEN] RANGE: " + rangeURI);
 							String value = this.extractLocalName(rangeURI);
 							simpleType.addFacet(new Facet(Facet.ENUMERATION,
 									value));
 						}
 						if (this.schema.getSimpleType(simpleType.getName()) == null) {
 							this.schema.addSimpleType(simpleType);
 							System.out.println("[i] add simpleType "
 									+ simpleType.getName() + " (4)");
 						}
 					} else {
 						if (elem.getType().equals(
 								"http://www.w3.org/2002/07/owl#Thing")) {
 							System.out
 									.println("[xsdgen] explicit defined thing found.");
 							simpleType = this.addSimpleThingType();
 						} else if (elem.getType().equals("null")) {
 							// System.out.println("[xsdgen] owl property has range null, interpreting #Thing");
 							// simpleType = this.addSimpleThingType();
 							// --> No ElementType ! (best solution)
 						}
 					}
 					subEl.setType(simpleType);
 				}
 			}
 
 			// http://www.w3schools.com/schema/schema_example.asp
 			// The default value for both maxOccurs and minOccurs is 1!
 
 			System.out.println("ELEM MIN OCCURS: " + elem.getMinOccurs());
 			System.out.println("ELEM MAX OCCURS: " + elem.getMaxOccurs());
 			// subEl.setMinOccurs(elem.getMinOccurs());
 			// subEl.setMaxOccurs(elem.getMaxOccurs());
 			// subEl.setMinOccurs(0);
 			// subEl.setMaxOccurs(Particle.UNBOUNDED);
 
 			System.out.println("[xsdgen] current Element: " + subEl.getName());
 
 			if (group.getElementDecl(subEl.getName()) == null) {
 				if (elem.getOwlSource().equals("UNION")
 						|| elem.getOwlSource().equals("INTERSECTION")) {
 					if (this.printOwlInformation) {
 						System.out.println("[xsdgen] ADD META Element "
 								+ subEl.getName() + " ("
 								+ subEl.getType().getName() + ") to group.");
 						group.addElementDecl(subEl);
 					}
 				} else {
 					if (subEl.getType() != null) {
 						System.out.println("[xsdgen] ADD Element "
 								+ subEl.getName() + " ("
 								+ subEl.getType().getName() + ") to group.");
 					} else {
 						System.out.println("[xsdgen] ADD Element "
 								+ subEl.getName()
 								+ " (without type declaration!) to group.");
 					}
 					group.addElementDecl(subEl);
 				}
 			} else {
 				System.out.println("[xsdgen] Element " + subEl.getName() + " ("
 						+ subEl.getType().getName() + ") already in group.");
 			}
 		} // END WHILE
 
 		// complexType.setName(typeName);
 		// this.schema.addComplexType(complexType); // nach oben verschieben
 		// wegen Rekursionsabbruch!
 
 		if (useHierarchyPattern && curtype.getParentList().size() > 0) {
 			System.out.println("[HierarchyPattern] " + curtype.getUrl() + " (");
 			String parentURI = curtype.getParentList().get(0).toString();
 			System.out.println("[HierarchyPattern] parent:" + parentURI);
 			String abstractElementBaseName = this.extractLocalName(parentURI);
 
 			ElementDecl baseElement = this.schema
 					.getElementDecl(abstractElementBaseName);
 			if (baseElement == null) {
 				baseElement = new ElementDecl(this.schema,
 						abstractElementBaseName);
 				this.schema.addElementDecl(baseElement);
 			}
 			baseElement.setAbstract(true);
 			this.hack_02.add(abstractElementBaseName);
 
 			XMLType baseType = null;
 
 			if (parentURI.equals(URI_THING) && baseType == null) {
 				System.out
 						.println("[i] missing parent is Thing, add simple ThingType");
 				baseType = this.addSimpleThingType();
 				abstractElementBaseName = "Thing";
 			}
 
 			if (this.schema.getType(abstractElementBaseName + "Type") == null) {
 				System.out.println("[HierarchyPattern] missing base type: "
 						+ abstractElementBaseName + "Type");
 				System.out
 						.println("[HierarchyPattern] parentURI: " + parentURI);
 				if (AbstractDatatypeKB.getInstance()
 						.getAbstractDatatypeKBData().containsKey(parentURI)) {
 					AbstractDatatype abstractBaseType = AbstractDatatypeKB
 							.getInstance().getAbstractDatatypeKBData()
 							.get(parentURI);
 					if (abstractBaseType.isComplexType(this.depth)) {
 						baseType = this.addComplexType(abstractBaseType,
 								abstractBaseType.getLocalName() + "Type");
 					} else {
 						baseType = this.addSimpleType(abstractBaseType,
 								abstractBaseType.getLocalName() + "Type");
 					}
 				}
 			} else {
 				baseType = this.schema
 						.getType(abstractElementBaseName + "Type");
 			}
 
 			baseElement.setType(baseType);
 			System.out.println("baseType: " + baseType.getName());
 
 			if (this.schema.getElementDecl(curtype.getLocalName()) != null) {
 				this.schema.getElementDecl(curtype.getLocalName())
 						.setSubstitutionGroup(abstractElementBaseName);
 			}
 
 			if (baseType.isComplexType()) {
 				if (curtype.isComplexType(this.depth)) {
 					complexType.setComplexContent(true);
 					complexType.setDerivationMethod(SchemaNames.RESTRICTION);
 					complexType.setRestriction(true);
 					complexType.setBase(abstractElementBaseName + "Type");
 					complexType.setBaseType(baseType);
 				}
 			}
 		}
 
 		return complexType;
 	}
 
 	/**
 	 * Recursive method, that adds a datatype to schema definition
 	 * 
 	 * @param curtype
 	 *            The AbstractDatatype object that is used to build an element
 	 */
 	public void toXSD(AbstractDatatype curtype) throws SchemaException {
 		ArrayList<AbstractDatatype> dependencies = new ArrayList<AbstractDatatype>();
 		System.out.println();
 		System.out.println("[xsdgen] to XSD: " + curtype.getUrl());
 		System.out.println("[xsdgen] to XSD, depth: " + this.depth);
 		System.out.println("[xsdgen] to XSD, property count: "
 				+ curtype.getProperties(this.depth).size());
 
 		if (curtype.getProperties(this.depth).isEmpty()) // !curtype.hasElements(this.depth)
 															// )
 		{
 			SimpleType simpleType = this.addSimpleType(curtype,
 					curtype.getLocalName());
 			ElementDecl elDecl = new ElementDecl(this.schema);
 			elDecl.setName(curtype.getLocalName());
 			elDecl.setType(simpleType);
 
 			if (this.hack_02.contains(curtype.getLocalName())) {
 				elDecl.setAbstract(true);
 			}
 			if (this.schema.getElementDecl(curtype.getLocalName()) == null) {
 				this.schema.addElementDecl(elDecl);
 			}
 			this.addSchemaAnnotation("Translation (OWL2XSD-SimpleType) of "
 					+ curtype.getUrl());
 		} else {
 			System.out.println("[xsdgen] add new ComplexType ("
 					+ curtype.getLocalName() + ")Type for " + curtype.getUrl());
 			ComplexType complexType = this.addComplexType(curtype,
 					curtype.getLocalName() + "Type");
 
 			// correct ComplexTypes when changing Names (add. "Type") due
 			// composing schemas for WSDL.
 			ComplexType complexSubType = this.schema.getComplexType(curtype
 					.getLocalName());
 			if (complexSubType != null) {
 				System.out.println("Loaded complexSubType Id:"
 						+ complexSubType.getId() + ", Name: "
 						+ complexSubType.getName());
 				if (complexSubType.isTopLevel()) {
 					System.out.println("[xsdgen] complexSubType "
 							+ complexSubType + " is top-level type.");
 
 					ElementDecl elem = (ElementDecl) complexSubType.getParent();
 					System.out.println("[xsdgen] complexSubType Parent: "
 							+ elem.getName());
 					elem.setType(complexType);
 
 					// mark for deletion at the end of WSDL generation
 					this.markObsoleteComplexTypeForDeletion(complexSubType);
 				}
 
 				// System.out.println("[xsdgen] use existing ComplexType ("+curtype.getLocalName()
 				// +")Type for "+curtype.getUrl());
 				// complexType =
 				// this.schema.getComplexType(curtype.getLocalName());
 				// System.out.println("Loaded CT Id:"+complexType.getId()+", Name: "+complexType.getName());
 				// complexType.setName(curtype.getLocalName()+"Type");
 				// System.out.println("Changed CT Id:"+complexType.getId()+", Name: "+complexType.getName());
 			}
 
 			// try {
 			// this.schema.validate();
 			// }
 			// catch(ValidationException ve) {
 			// ve.printStackTrace();
 			// }
 
 			// ComplexType complexType =
 			// this.schema.getComplexType(curtype.getLocalName());
 			// if(complexType == null ) {
 			// complexType = this.addComplexType(curtype,
 			// curtype.getLocalName());
 			// }
 
 			/**
 			 * Hack to make it WSDL4J conform (axis WSDL2Java, WSDL2OWL-S)
 			 * Message parts must be named complex types - not elements.
 			 */
 			if (this.hack_01 == false) {
 				ElementDecl elDecl = new ElementDecl(this.schema);
 				elDecl.setName(curtype.getLocalName());
 				elDecl.setType(complexType);
 				this.hack_01 = true; // einmalig.
 
 				if (useHierarchyPattern) {
 					/*
 					 * Hierarchy Pattern: use of SubstitionGroup element
 					 */
 					if (curtype.getIntersectionList().size() == 1) {
 						if (curtype.getIntersectionList().get(0).toString()
 								.equals(curtype.getUrl())) {
 							System.out
 									.println("[xsdgen] not using HierarchyPattern, intersection with same type.");
 						} else {
 							System.out
 									.println("[xsdgen] use HierarchyPattern: getIntersectionList == 1");
 
 							String abstractElementName = this
 									.extractLocalName(curtype
 											.getIntersectionList().get(0)
 											.toString());
 							elDecl.setSubstitutionGroup(abstractElementName);
 							System.out.println("HACK2: " + abstractElementName);
 							this.hack_02.add(abstractElementName);
 
 							AbstractDatatype subtype = AbstractDatatypeKB
 									.getInstance().data.get(curtype
 									.getIntersectionList().get(0).toString());
 
 							if (subtype.isComplexType(this.depth)) {
 								complexType.setComplexContent(true);
 								complexType
 										.setDerivationMethod(SchemaNames.RESTRICTION);
 								complexType.setRestriction(true);
 								complexType.setBase(abstractElementName
 										+ "Type");
 								complexType.setBaseType(this.schema
 										.getType(abstractElementName + "Type"));
 							} else {
 								if (this.printAnnotations) {
 									Annotation annotation = new Annotation();
 									Documentation doc = new Documentation();
 									doc.setSource("hasSimpleIntersection "
 											+ abstractElementName);
 									annotation.addDocumentation(doc);
 									complexType.addAnnotation(annotation);
 								}
 								System.out
 										.println("[i] can't set ComplexContent (hierarchy pattern), parent is SimpleType");
 							}
 							dependencies.add(subtype);
 						}
 					} else if (curtype.getParentList().size() == 1) {
 
 						String parentURI = curtype.getParentList().get(0)
 								.toString();
 						String abstractElementName = this
 								.extractLocalName(parentURI);
 						System.out
 								.println("[xsdgen] useHierarchyPattern: getParentList == 1, "
 										+ parentURI);
 
 						ElementDecl parentElement = null;
 						if (this.schema.getElementDecl(abstractElementName) == null) {
 							parentElement = new ElementDecl(this.schema,
 									abstractElementName);
 							parentElement.setAbstract(true);
 						} else {
 							parentElement = this.schema
 									.getElementDecl(abstractElementName);
 						}
 
 						elDecl.setSubstitutionGroup(abstractElementName);
 						if (this.schema.getElementDecl(abstractElementName) == null) {
 							this.hack_02.add(abstractElementName);
 						}
 
 						System.out
 								.println("[xsdgen] build hierarchy ref for type: "
 										+ curtype.getUrl());
 						System.out.println("[xsdgen] found one parent type: "
 								+ parentURI);
 
 						if (parentURI.equals(URI_THING)) {
 							System.out.println("[i] parent is Thing");
 							SimpleType thingType = this.addSimpleThingType();
 							parentElement.setType(thingType);
 							if (this.printAnnotations) {
 								Annotation annotation = new Annotation();
 								Documentation doc = new Documentation();
 								doc.setSource("parent is " + URI_THING);
 								annotation.addDocumentation(doc);
 								complexType.addAnnotation(annotation);
 							}
 						} else {
 							// Check knowledge base for parentType
 							AbstractDatatype parentType = AbstractDatatypeKB
 									.getInstance().getAbstractDatatypeKBData()
 									.get(parentURI);
 
 							if (parentType.isComplexType(this.depth)) {
 								// dependencies.add(parentType);
 								ComplexType complexBaseType = this
 										.addComplexType(parentType,
 												abstractElementName + "Type");
 								parentElement.setType(complexBaseType);
 
 								complexType.setComplexContent(true);
 								// complexType.setContentType(ContentType.mixed);
 								complexType
 										.setDerivationMethod(SchemaNames.RESTRICTION);
 								complexType.setRestriction(true);
 								complexType.setBase(abstractElementName
 										+ "Type");
 								complexType.setBaseType(complexBaseType);
 							} else {
 								SimpleType simpleBaseType = this.addSimpleType(
 										parentType, abstractElementName
 												+ "Type");
 								parentElement.setType(simpleBaseType);
 								if (this.printAnnotations) {
 									Annotation annotation = new Annotation();
 									Documentation doc = new Documentation();
 									doc.setSource("parent is SimpleType "
 											+ abstractElementName);
 									annotation.addDocumentation(doc);
 									complexType.addAnnotation(annotation);
 								}
 								System.out
 										.println("[i] can't set ComplexContent (hierarchy pattern), parent is SimpleType");
 							}
 						}
 						if (this.schema.getElementDecl(parentElement.getName()) == null) {
 							this.schema.addElementDecl(parentElement);
 						}
 					} else {
 						System.out
 								.println("[e] more than one intersection/parent classes. use mixin pattern!");
 					}
 				}
 
 				// elDecl.setMaxOccurs(Particle.UNBOUNDED); // jedes Element
 				// muss mind. einmal vorkommen. Geht aber hier nicht wegen META
 				// Element.
 
 				if (this.schema.getElementDecl(elDecl.getName()) == null) {
 					this.schema.addElementDecl(elDecl);
 					this.addSchemaAnnotation("Translation (OWL2XSD-ComplexType) of "
 							+ curtype.getUrl());
 					System.out.println("[xsdgen] element " + elDecl.getName()
 							+ " added.");
 				} else {
 					System.out.println("[xsdgen] element " + elDecl.getName()
 							+ " already in schema.");
 				}
 			} else {
 				if (useHierarchyPattern) {
 					/*
 					 * Element Hierarchy Pattern
 					 */
 					if (curtype.getIntersectionList().size() == 1) {
 						this.hack_02.add(this.extractLocalName(curtype
 								.getIntersectionList().get(0).toString()));
 					} else if (curtype.getParentList().size() == 1) {
 						this.hack_02.add(this.extractLocalName(curtype
 								.getParentList().get(0).toString()));
 					} else if (curtype.getIntersectionList().size() > 1) {
 						System.out
 								.println("[e] more than one intersection classes. use mixin pattern!");
 					} else if (curtype.getParentList().size() > 1) {
 						System.out
 								.println("[e] more than one parent classes. use mixin pattern!");
 					} else {
 						System.out
 								.println("[i] no intersection class available.");
 					}
 
 					if (this.hack_02.contains(curtype.getLocalName())) {
 
 						complexType.setName(curtype.getLocalName() + "Type"); // 25.1.2007
 																				// +"Type");
 						if (schema.getComplexType(complexType.getName()) == null) {
 							this.schema.addComplexType(complexType);
 							System.out.println("[xsdgen]-c1: "
 									+ complexType.getName());
 						} else {
 							System.out.println("[xsdgen]-c1 "
 									+ complexType.getName()
 									+ " already in schema.");
 						}
 
 						ElementDecl elDec = new ElementDecl(this.schema);
 						elDec.setName(curtype.getLocalName());
 						// elDec.setTypeReference(complexType.getReferenceId());
 						elDec.setType(complexType);
 						elDec.setAbstract(true);
 
 						if (curtype.getIntersectionList().size() == 1) {
 							if (curtype.getIntersectionList().get(0).toString()
 									.equals(curtype.getUrl())) {
 								System.out
 										.println("[xsdgen] not using HierarchyPattern, intersection with same type.");
 							} else {
 								System.out
 										.println("[xsdgen] use HierarchyPattern: getIntersectionList == 1");
 								String abstractElementName = this
 										.extractLocalName(curtype
 												.getIntersectionList().get(0)
 												.toString());
 								elDec.setSubstitutionGroup(abstractElementName);
 								this.hack_02.add(abstractElementName);
 
 								complexType.setComplexContent(true);
 								complexType
 										.setDerivationMethod(SchemaNames.RESTRICTION);
 								complexType.setRestriction(true);
 								complexType.setBase(abstractElementName
 										+ "Type");
 								complexType.setBaseType(this.schema
 										.getType(abstractElementName + "Type"));
 
 								System.out.println("     AbstractElement: "
 										+ abstractElementName);
 								AbstractDatatype supertype = AbstractDatatypeKB
 										.getInstance().data.get(curtype
 										.getIntersectionList().get(0)
 										.toString());
 								dependencies.add(supertype);
 							}
 						} else if (curtype.getParentList().size() == 1) {
 							String parentURI = curtype.getParentList().get(0)
 									.toString();
 							String abstractElementName = this
 									.extractLocalName(parentURI);
 							System.out
 									.println("[xsdgen] useHierarchyPattern: getParentList == 1, "
 											+ parentURI);
 
 							ElementDecl parentElement = null;
 							if (this.schema.getElementDecl(abstractElementName) == null) {
 								parentElement = new ElementDecl(this.schema,
 										abstractElementName);
 								parentElement.setAbstract(true);
 							} else {
 								parentElement = this.schema
 										.getElementDecl(abstractElementName);
 							}
 
 							elDec.setSubstitutionGroup(abstractElementName);
 							if (this.schema.getElementDecl(abstractElementName) == null) {
 								this.hack_02.add(abstractElementName);
 							}
 
 							System.out
 									.println("[xsdgen] build hierarchy ref for type: "
 											+ curtype.getUrl());
 							System.out.println("[xsdgen] found parent type: "
 									+ parentURI);
 
 							if (parentURI.equals(URI_THING)) {
 								System.out.println("[i] parent is Thing");
 								SimpleType thingType = this
 										.addSimpleThingType();
 								parentElement.setType(thingType);
 								if (this.printAnnotations) {
 									Annotation annotation = new Annotation();
 									Documentation doc = new Documentation();
 									doc.setSource("parent is " + URI_THING);
 									annotation.addDocumentation(doc);
 									complexType.addAnnotation(annotation);
 								}
 							} else {
 								// Check knowledge base for parentType
 								AbstractDatatype parentType = AbstractDatatypeKB
 										.getInstance()
 										.getAbstractDatatypeKBData()
 										.get(parentURI);
 
 								if (parentType.isComplexType(this.depth)) {
 									// dependencies.add(parentType);
 									ComplexType complexBaseType = this
 											.addComplexType(parentType,
 													abstractElementName
 															+ "Type");
 									parentElement.setType(complexBaseType);
 
 									complexType.setComplexContent(true);
 									// complexType.setContentType(ContentType.mixed);
 									complexType
 											.setDerivationMethod(SchemaNames.RESTRICTION);
 									complexType.setRestriction(true);
 									complexType.setBase(abstractElementName
 											+ "Type");
 									complexType.setBaseType(complexBaseType);
 								} else {
 									SimpleType simpleBaseType = this
 											.addSimpleType(parentType,
 													abstractElementName
 															+ "Type");
 									parentElement.setType(simpleBaseType);
 									if (this.printAnnotations) {
 										Annotation annotation = new Annotation();
 										Documentation doc = new Documentation();
 										doc.setSource("parent is SimpleType "
 												+ abstractElementName);
 										annotation.addDocumentation(doc);
 										complexType.addAnnotation(annotation);
 									}
 									System.out
 											.println("[i] can't set ComplexContent (hierarchy pattern), parent is SimpleType");
 								}
 							}
 							if (this.schema.getElementDecl(parentElement
 									.getName()) == null) {
 								this.schema.addElementDecl(parentElement);
 							}
 						}
 
 						// min/max Occurs ???
 
 						if (this.schema.getElementDecl(elDec.getName()) == null) {
 							this.schema.addElementDecl(elDec);
 							this.addSchemaAnnotation("Translation (OWL2XSD-ComplexType) of "
 									+ curtype.getUrl());
 							System.out.println("[xsdgen] element "
 									+ elDec.getName() + " added.");
 						} else {
 							System.out.println("[xsdgen] element "
 									+ elDec.getName() + " already in schema.");
 						}
 					} else {
 						if (schema.getComplexType(complexType.getName()) == null) {
 							this.schema.addComplexType(complexType);
 							System.out.println("[xsdgen]-c2: "
 									+ complexType.getName());
 						} else {
 							System.out.println("[xsdgen]-c2 "
 									+ complexType.getName()
 									+ " already in schema");
 						}
 					}
 				} else {
 					if (complexType.getName() == null) {
 						complexType.setName(curtype.getLocalName());
 					}
 
 					if (schema.getComplexType(complexType.getName()) == null) {
 						this.schema.addComplexType(complexType);
 						System.out.println("[xsdgen]-c3: "
 								+ complexType.getName());
 					} else {
 						System.out.println("[xsdgen]-c3 "
 								+ complexType.getName() + " already in schema");
 					}
 				}
 
 				// if(schema.getComplexType(complexType.getName()) == null) {
 				// this.schema.addComplexType(complexType);
 				// System.out.println(">>>> NEW COMPLEXTYPE-2: "+complexType.getName());
 				// }
 			}
 
 			if (DEBUGFLAG) {
 				System.out.println("[xsdgen] SCHEMA CONTENT =================");
 				@SuppressWarnings("unchecked")
 				Enumeration<ElementDecl> allElDecls = this.schema
 						.getElementDecls();
 				while (allElDecls.hasMoreElements()) {
 					System.out.println("[xsdgen] ELEM: "
 							+ ((ElementDecl) allElDecls.nextElement())
 									.getName());
 				}
 
 				@SuppressWarnings("unchecked")
 				Enumeration<ComplexType> cte = this.schema.getComplexTypes();
 				while (cte.hasMoreElements()) {
 					ComplexType temp = cte.nextElement();
 					System.out.println("[xsdgen] CT " + temp.getName());
 				}
 			}
 
 			// Rekursion am Ende, um die angepassten Range Restrictions korrekt
 			// zu setzen.
 			// Abfrage: wenn schon ein Element im Schema, kann es nicht
 			// hinzugef�gt werden.
 			//
 			for (int i = 0; i < dependencies.size(); i++) {
 				System.out
 						.println("=======================================================================");
 				System.out.println("REKURSION with "
 						+ dependencies.get(i).getLocalName());
 				this.toXSD(dependencies.get(i));
 			}
 		}
 	}
 
 }
