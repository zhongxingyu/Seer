 package it.unibz.krdb.obda.owlrefplatform.core.abox;
 
 import it.unibz.krdb.obda.model.BNode;
 import it.unibz.krdb.obda.model.CQIE;
 import it.unibz.krdb.obda.model.Constant;
 import it.unibz.krdb.obda.model.Function;
 import it.unibz.krdb.obda.model.NewLiteral;
 import it.unibz.krdb.obda.model.OBDADataFactory;
 import it.unibz.krdb.obda.model.OBDAException;
 import it.unibz.krdb.obda.model.OBDAMappingAxiom;
 import it.unibz.krdb.obda.model.OBDASQLQuery;
 import it.unibz.krdb.obda.model.ObjectConstant;
 import it.unibz.krdb.obda.model.Predicate;
 import it.unibz.krdb.obda.model.Predicate.COL_TYPE;
 import it.unibz.krdb.obda.model.URIConstant;
 import it.unibz.krdb.obda.model.ValueConstant;
 import it.unibz.krdb.obda.model.impl.OBDADataFactoryImpl;
 import it.unibz.krdb.obda.ontology.Assertion;
 import it.unibz.krdb.obda.ontology.BinaryAssertion;
 import it.unibz.krdb.obda.ontology.ClassAssertion;
 import it.unibz.krdb.obda.ontology.ClassDescription;
 import it.unibz.krdb.obda.ontology.DataPropertyAssertion;
 import it.unibz.krdb.obda.ontology.DataType;
 import it.unibz.krdb.obda.ontology.Description;
 import it.unibz.krdb.obda.ontology.OClass;
 import it.unibz.krdb.obda.ontology.ObjectPropertyAssertion;
 import it.unibz.krdb.obda.ontology.Ontology;
 import it.unibz.krdb.obda.ontology.OntologyFactory;
 import it.unibz.krdb.obda.ontology.Property;
 import it.unibz.krdb.obda.ontology.PropertySomeRestriction;
 import it.unibz.krdb.obda.ontology.impl.OntologyFactoryImpl;
 import it.unibz.krdb.obda.ontology.impl.OntologyImpl;
 import it.unibz.krdb.obda.ontology.impl.PropertyImpl;
 import it.unibz.krdb.obda.ontology.impl.PropertySomeRestrictionImpl;
 import it.unibz.krdb.obda.owlrefplatform.core.abox.SemanticIndexRecord.OBJType;
 import it.unibz.krdb.obda.owlrefplatform.core.abox.SemanticIndexRecord.SITable;
 import it.unibz.krdb.obda.owlrefplatform.core.dag.DAG;
 import it.unibz.krdb.obda.owlrefplatform.core.dag.DAGConstructor;
 import it.unibz.krdb.obda.owlrefplatform.core.dag.DAGNode;
 import it.unibz.krdb.obda.owlrefplatform.core.dag.DAGOperations;
 import it.unibz.krdb.obda.owlrefplatform.core.dag.Interval;
 
 import java.io.BufferedWriter;
 import java.io.IOException;
 import java.io.OutputStream;
 import java.io.OutputStreamWriter;
 import java.math.BigDecimal;
 import java.net.URI;
 import java.sql.Connection;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.Statement;
 import java.sql.Timestamp;
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.LinkedHashMap;
 import java.util.LinkedHashSet;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 import java.util.Properties;
 import java.util.Queue;
 import java.util.Set;
 
 import org.antlr.misc.IntervalSet;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import com.hp.hpl.jena.iri.IRI;
 import com.hp.hpl.jena.iri.IRIFactory;
 
 /**
  * Store ABox assertions in the DB
  * 
  */
 public class RDBMSSIRepositoryManager implements RDBMSDataRepositoryManager {
 
 	private static final long serialVersionUID = -6494667662327970606L;
 
 	private final static Logger log = LoggerFactory.getLogger(RDBMSSIRepositoryManager.class);
 
 /**
  * Metadata tables 
  */
 	public final static String index_table = "IDX";
 
 	public final static String interval_table = "IDXINTERVAL";
 
 	public final static String emptyness_index_table = "NONEMPTYNESSINDEX";
 	
 	public final static String uri_id_table = "URIID";
 
 /**
  *  Data tables
  */
 	
 	public static final String class_table = "QUEST_CLASS_ASSERTION";
 
 	public static final String role_table = "QUEST_OBJECT_PROPERTY_ASSERTION";
 	
 	public static final String attribute_table_literal = "QUEST_DATA_PROPERTY_LITERAL_ASSERTION";
 
 	public static final String attribute_table_string = "QUEST_DATA_PROPERTY_STRING_ASSERTION";
 
 	public static final String attribute_table_integer = "QUEST_DATA_PROPERTY_INTEGER_ASSERTION";
 
 	public static final String attribute_table_decimal = "QUEST_DATA_PROPERTY_DECIMAL_ASSERTION";
 
 	public static final String attribute_table_double = "QUEST_DATA_PROPERTY_DOUBLE_ASSERTION";
 
 	public static final String attribute_table_datetime = "QUEST_DATA_PROPERTY_DATETIME_ASSERTION";
 
 	public static final String attribute_table_boolean = "QUEST_DATA_PROPERTY_BOOLEAN_ASSERTION";
 	
 /**
  *  CREATE metadata tables
  */
 	
	private final static String create_idx = "CREATE TABLE " + index_table + " ( " + "URI VARCHAR(400), "
 			+ "IDX INTEGER, ENTITY_TYPE INTEGER" + ")";
 
	private final static String create_interval = "CREATE TABLE " + interval_table + " ( " + "URI VARCHAR(400), " + "IDX_FROM INTEGER, "
 			+ "IDX_TO INTEGER, " + "ENTITY_TYPE INTEGER" + ")";
 
 	private final static String create_emptyness_index = "CREATE TABLE " + emptyness_index_table + " ( TABLEID INTEGER, IDX INTEGER, "
 			+ " TYPE1 INTEGER, TYPE2 INTEGER )";
 	
	private final static String create_uri_id = "CREATE TABLE " + uri_id_table + " ( " + "ID INTEGER, " + "URI VARCHAR(400) " + ")";
 
 /**
  * DROP metadata tables 	
  */
 	
 	private final static String drop_idx = "DROP TABLE " + index_table + "";
 	private final static String drop_interval = "DROP TABLE " + interval_table + "";
 	private final static String drop_emptyness = "DROP TABLE " + emptyness_index_table + "";
 	private final static String drop_uri_id = "DROP TABLE " + uri_id_table + "";
 
 /**
  *  INSERT metadata
  */
 	
 	private final static String insert_idx_query = "INSERT INTO " + index_table + "(URI, IDX, ENTITY_TYPE) VALUES(?, ?, ?)";
 	private final static String uriid_insert = "INSERT INTO " + uri_id_table + "(ID, URI) VALUES(?, ?)";
 	private final static String insert_interval_query = "INSERT INTO " + interval_table
 			+ "(URI, IDX_FROM, IDX_TO, ENTITY_TYPE) VALUES(?, ?, ?, ?)";
 	
 /**
  *  CREATE data tables
  */
 	
 	public static final String class_table_create = "CREATE TABLE " + class_table + " ( " + "\"URI\" INTEGER NOT NULL, "
 			+ "\"IDX\"  SMALLINT NOT NULL, " + " ISBNODE BOOLEAN NOT NULL DEFAULT FALSE " + ")";
 
 	public static final String role_table_create = "CREATE TABLE " + role_table + " ( " + "\"URI1\" INTEGER NOT NULL, "
 			+ "\"URI2\" INTEGER NOT NULL, " + "\"IDX\"  SMALLINT NOT NULL, " + "ISBNODE BOOLEAN NOT NULL DEFAULT FALSE, "
 			+ "ISBNODE2 BOOLEAN NOT NULL DEFAULT FALSE)";
 
 	public static final String attribute_table_literal_create = "CREATE TABLE " + attribute_table_literal + " ( "
 			+ "\"URI\" INTEGER NOT NULL, " + "VALUE VARCHAR(1000) NOT NULL, " + "LANG VARCHAR(20), " + "\"IDX\"  SMALLINT NOT NULL"
 			+ ", ISBNODE BOOLEAN  NOT NULL DEFAULT FALSE " + ")";
 	public static final String attribute_table_string_create = "CREATE TABLE " + attribute_table_string + " ( "
 			+ "\"URI\" INTEGER  NOT NULL, " + "VALUE VARCHAR(1000), " + "\"IDX\"  SMALLINT  NOT NULL"
 			+ ", ISBNODE BOOLEAN  NOT NULL DEFAULT FALSE " + ")";
 	public static final String attribute_table_integer_create = "CREATE TABLE " + attribute_table_integer + " ( "
 			+ "\"URI\" INTEGER  NOT NULL, " + "VALUE BIGINT NOT NULL, " + "\"IDX\"  SMALLINT NOT NULL"
 			+ ", ISBNODE BOOLEAN  NOT NULL DEFAULT FALSE " + ")";
 	public static final String attribute_table_decimal_create = "CREATE TABLE " + attribute_table_decimal + " ( "
 			+ "\"URI\" INTEGER NOT NULL, " + "VALUE DECIMAL NOT NULL, " + "\"IDX\"  SMALLINT NOT NULL"
 			+ ", ISBNODE BOOLEAN  NOT NULL DEFAULT FALSE " + ")";
 	public static final String attribute_table_double_create = "CREATE TABLE " + attribute_table_double + " ( "
 			+ "\"URI\" INTEGER NOT NULL, " + "VALUE DOUBLE PRECISION NOT NULL, " + "\"IDX\"  SMALLINT NOT NULL"
 			+ ", ISBNODE BOOLEAN  NOT NULL DEFAULT FALSE " + ")";
 	public static final String attribute_table_datetime_create = "CREATE TABLE " + attribute_table_datetime + " ( "
 			+ "\"URI\" INTEGER NOT NULL, " + "VALUE TIMESTAMP NOT NULL, " + "\"IDX\"  SMALLINT NOT NULL"
 			+ ", ISBNODE BOOLEAN  NOT NULL DEFAULT FALSE " + ")";
 	public static final String attribute_table_boolean_create = "CREATE TABLE " + attribute_table_boolean + " ( "
 			+ "\"URI\" INTEGER NOT NULL, " + "VALUE BOOLEAN NOT NULL, " + "\"IDX\"  SMALLINT NOT NULL"
 			+ ", ISBNODE BOOLEAN  NOT NULL DEFAULT FALSE " + ")";
 
 /**
  *  DROP data tables	
  */
 	
 	public static final String class_table_drop = "DROP TABLE " + class_table;
 	public static final String role_table_drop = "DROP TABLE " + role_table;
 
 	public static final String attribute_table_literal_drop = "DROP TABLE " + attribute_table_literal;
 	public static final String attribute_table_string_drop = "DROP TABLE " + attribute_table_string;
 	public static final String attribute_table_integer_drop = "DROP TABLE " + attribute_table_integer;
 	public static final String attribute_table_decimal_drop = "DROP TABLE " + attribute_table_decimal;
 	public static final String attribute_table_double_drop = "DROP TABLE " + attribute_table_double;
 	public static final String attribute_table_datetime_drop = "DROP TABLE " + attribute_table_datetime;
 	public static final String attribute_table_boolean_drop = "DROP TABLE " + attribute_table_boolean;
 
 /**
  *  INSERT data 	
  */
 	
 	public static final String class_insert = "INSERT INTO " + class_table + " (URI, IDX, ISBNODE) VALUES (?, ?, ?)";
 	public static final String role_insert = "INSERT INTO " + role_table + " (URI1, URI2, IDX, ISBNODE, ISBNODE2) VALUES (?, ?, ?, ?, ?)";
 
 	public static final String attribute_table_literal_insert = "INSERT INTO " + attribute_table_literal
 			+ " (URI, VALUE, LANG, IDX, ISBNODE) VALUES (?, ?, ?, ?, ?)";
 	public static final String attribute_table_string_insert = "INSERT INTO " + attribute_table_string
 			+ " (URI, VALUE, IDX, ISBNODE) VALUES (?, ?, ?, ?)";
 	public static final String attribute_table_integer_insert = "INSERT INTO " + attribute_table_integer
 			+ " (URI, VALUE, IDX, ISBNODE) VALUES (?, ?, ?, ?)";
 	public static final String attribute_table_decimal_insert = "INSERT INTO " + attribute_table_decimal
 			+ " (URI, VALUE, IDX, ISBNODE) VALUES (?, ?, ?, ?)";
 	public static final String attribute_table_double_insert = "INSERT INTO " + attribute_table_double
 			+ " (URI, VALUE, IDX, ISBNODE) VALUES (?, ?, ?, ?)";
 	public static final String attribute_table_datetime_insert = "INSERT INTO " + attribute_table_datetime
 			+ " (URI, VALUE, IDX, ISBNODE) VALUES (?, ?, ?, ?)";
 	public static final String attribute_table_boolean_insert = "INSERT INTO " + attribute_table_boolean
 			+ " (URI, VALUE, IDX, ISBNODE) VALUES (?, ?, ?, ?)";
 
 /**
  *  Indexes
  */
 	
 	public static final String indexclass_composite = "CREATE INDEX idxclassfull ON " + class_table + " (URI, IDX, ISBNODE)";
 	public static final String indexrole_composite1 = "CREATE INDEX idxrolefull1 ON " + role_table + " (URI1, URI2, IDX, ISBNODE, ISBNODE2)";
 	public static final String indexrole_composite2 = "CREATE INDEX idxrolefull2 ON " + role_table + " (URI2, URI1, IDX, ISBNODE2, ISBNODE)";
 
 
 	public static final String indexclass1 = "CREATE INDEX idxclass1 ON " + class_table + " (URI)";
 	public static final String indexclass2 = "CREATE INDEX idxclass2 ON " + class_table + " (IDX)";
 	public static final String indexclassfull2 = "CREATE INDEX idxclassfull2 ON " + class_table + " (URI, IDX)";
 
 	public static final String indexrole1 = "CREATE INDEX idxrole1 ON " + role_table + " (URI1)";
 	public static final String indexrole2 = "CREATE INDEX idxrole2 ON " + role_table + " (IDX)";
 	public static final String indexrole3 = "CREATE INDEX idxrole3 ON " + role_table + " (URI2)";
 	public static final String indexrolefull22 = "CREATE INDEX idxrolefull22 ON " + role_table + " (URI1, URI2, IDX)";
 
 	public static final String attribute_literal_index = "IDX_LITERAL_ATTRIBUTE";
 	public static final String attribute_string_index = "IDX_STRING_ATTRIBUTE";
 	public static final String attribute_integer_index = "IDX_INTEGER_ATTRIBUTE";
 	public static final String attribute_decimal_index = "IDX_DECIMAL_ATTRIBUTE";
 	public static final String attribute_double_index = "IDX_DOUBLE_ATTRIBUTE";
 	public static final String attribute_datetime_index = "IDX_DATETIME_ATTRIBUTE";
 	public static final String attribute_boolean_index = "IDX_BOOLEAN_ATTRIBUTE";
 
 	public static final String indexattribute_literal1 = "CREATE INDEX " + attribute_literal_index + "1" + " ON " + attribute_table_literal
 			+ " (URI)";
 	public static final String indexattribute_string1 = "CREATE INDEX " + attribute_string_index + "1" + " ON " + attribute_table_string
 			+ " (URI)";
 	public static final String indexattribute_integer1 = "CREATE INDEX " + attribute_integer_index + "1" + " ON " + attribute_table_integer
 			+ " (URI)";
 	public static final String indexattribute_decimal1 = "CREATE INDEX " + attribute_decimal_index + "1" + " ON " + attribute_table_decimal
 			+ " (URI)";
 	public static final String indexattribute_double1 = "CREATE INDEX " + attribute_double_index + "1" + " ON " + attribute_table_double
 			+ " (URI)";
 	public static final String indexattribute_datetime1 = "CREATE INDEX " + attribute_datetime_index + "1" + " ON "
 			+ attribute_table_datetime + " (URI)";
 	public static final String indexattribute_boolean1 = "CREATE INDEX " + attribute_boolean_index + "1" + " ON " + attribute_table_boolean
 			+ " (URI)";
 
 	public static final String indexattribute_literal2 = "CREATE INDEX " + attribute_literal_index + "2" + " ON " + attribute_table_literal
 			+ " (IDX)";
 	public static final String indexattribute_string2 = "CREATE INDEX " + attribute_string_index + "2" + " ON " + attribute_table_string
 			+ " (IDX)";
 	public static final String indexattribute_integer2 = "CREATE INDEX " + attribute_integer_index + "2" + " ON " + attribute_table_integer
 			+ " (IDX)";
 	public static final String indexattribute_decimal2 = "CREATE INDEX " + attribute_decimal_index + "2" + " ON " + attribute_table_decimal
 			+ " (IDX)";
 	public static final String indexattribute_double2 = "CREATE INDEX " + attribute_double_index + "2" + " ON " + attribute_table_double
 			+ " (IDX)";
 	public static final String indexattribute_datetime2 = "CREATE INDEX " + attribute_datetime_index + "2" + " ON "
 			+ attribute_table_datetime + " (IDX)";
 	public static final String indexattribute_boolean2 = "CREATE INDEX " + attribute_boolean_index + "2" + " ON " + attribute_table_boolean
 			+ " (IDX)";
 
 	public static final String indexattribute_literal3 = "CREATE INDEX " + attribute_literal_index + "3" + " ON " + attribute_table_literal
 			+ " (VALUE)";
 	public static final String indexattribute_string3 = "CREATE INDEX " + attribute_string_index + "3" + " ON " + attribute_table_string
 			+ " (VALUE)";
 	public static final String indexattribute_integer3 = "CREATE INDEX " + attribute_integer_index + "3" + " ON " + attribute_table_integer
 			+ " (VALUE)";
 	public static final String indexattribute_decimal3 = "CREATE INDEX " + attribute_decimal_index + "3" + " ON " + attribute_table_decimal
 			+ " (VALUE)";
 	public static final String indexattribute_double3 = "CREATE INDEX " + attribute_double_index + "3" + " ON " + attribute_table_double
 			+ " (VALUE)";
 	public static final String indexattribute_datetime3 = "CREATE INDEX " + attribute_datetime_index + "3" + " ON "
 			+ attribute_table_datetime + " (VALUE)";
 	public static final String indexattribute_boolean3 = "CREATE INDEX " + attribute_boolean_index + "3" + " ON " + attribute_table_boolean
 			+ " (VALUE)";
 
 /**
  *  DROP indexes	
  */
 	
 	public static final String dropindexclass1 = "DROP INDEX \"idxclass1\"";
 	public static final String dropindexclass2 = "DROP INDEX \"idxclass2\"";
 
 	public static final String dropindexrole1 = "DROP INDEX \"idxrole1\"";
 	public static final String dropindexrole2 = "DROP INDEX \"idxrole2\"";
 	public static final String dropindexrole3 = "DROP INDEX \"idxrole3\"";
 
 	public static final String dropindexattribute_literal1 = "DROP INDEX " + attribute_literal_index + "1";
 	public static final String dropindexattribute_string1 = "DROP INDEX " + attribute_string_index + "1";
 	public static final String dropindexattribute_integer1 = "DROP INDEX " + attribute_integer_index + "1";
 	public static final String dropindexattribute_decimal1 = "DROP INDEX " + attribute_decimal_index + "1";
 	public static final String dropindexattribute_double1 = "DROP INDEX " + attribute_double_index + "1";
 	public static final String dropindexattribute_datetime1 = "DROP INDEX " + attribute_datetime_index + "1";
 	public static final String dropindexattribute_boolean1 = "DROP INDEX " + attribute_boolean_index + "1";
 
 	public static final String dropindexattribute_literal2 = "DROP INDEX " + attribute_literal_index + "2";
 	public static final String dropindexattribute_string2 = "DROP INDEX " + attribute_string_index + "2";
 	public static final String dropindexattribute_integer2 = "DROP INDEX " + attribute_integer_index + "2";
 	public static final String dropindexattribute_decimal2 = "DROP INDEX " + attribute_decimal_index + "2";
 	public static final String dropindexattribute_double2 = "DROP INDEX " + attribute_double_index + "2";
 	public static final String dropindexattribute_datetime2 = "DROP INDEX " + attribute_datetime_index + "2";
 	public static final String dropindexattribute_boolean2 = "DROP INDEX " + attribute_boolean_index + "2";
 
 	public static final String dropindexattribute_literal3 = "DROP INDEX " + attribute_literal_index + "3";
 	public static final String dropindexattribute_string3 = "DROP INDEX " + attribute_string_index + "3";
 	public static final String dropindexattribute_integer3 = "DROP INDEX " + attribute_integer_index + "3";
 	public static final String dropindexattribute_decimal3 = "DROP INDEX " + attribute_decimal_index + "3";
 	public static final String dropindexattribute_double3 = "DROP INDEX " + attribute_double_index + "3";
 	public static final String dropindexattribute_datetime3 = "DROP INDEX " + attribute_datetime_index + "3";
 	public static final String dropindexattribute_boolean3 = "DROP INDEX " + attribute_boolean_index + "3";
 
 	public static final String analyze = "ANALYZE";
 
 	public static final String select_mapping_class = "SELECT \"URI\" as X FROM " + class_table;
 
 	public static final String select_mapping_class_role_left = "SELECT \"URI1\" as X FROM " + role_table;
 
 	public static final String select_mapping_class_role_right = "SELECT \"URI2\" as X FROM " + role_table;
 
 	public static final String select_mapping_class_attribute_literal_left = "SELECT \"URI\" as X FROM " + attribute_table_literal;
 	public static final String select_mapping_class_attribute_string_left = "SELECT \"URI\" as X FROM " + attribute_table_string;
 	public static final String select_mapping_class_attribute_integer_left = "SELECT \"URI\" as X FROM " + attribute_table_integer;
 	public static final String select_mapping_class_attribute_decimal_left = "SELECT \"URI\" as X FROM " + attribute_table_decimal;
 	public static final String select_mapping_class_attribute_double_left = "SELECT \"URI\" as X FROM " + attribute_table_double;
 	public static final String select_mapping_class_attribute_datetime_left = "SELECT \"URI\" as X FROM " + attribute_table_datetime;
 	public static final String select_mapping_class_attribute_boolean_left = "SELECT \"URI\" as X FROM " + attribute_table_boolean;
 
 	public static final String select_mapping_role = "SELECT \"URI1\" as X, \"URI2\" as Y FROM " + role_table;
 
 	public static final String select_mapping_role_inverse = "SELECT \"URI2\" as X, \"URI1\" as Y FROM " + role_table;
 
 	public static final String select_mapping_attribute_literal = "SELECT \"URI\" as X, VALUE as Y, LANG as Z FROM "
 			+ attribute_table_literal;
 
 	public static final String select_mapping_attribute_string = "SELECT \"URI\" as X, VALUE as Y FROM " + attribute_table_string;
 	public static final String select_mapping_attribute_integer = "SELECT \"URI\" as X, VALUE as Y FROM " + attribute_table_integer;
 	public static final String select_mapping_attribute_decimal = "SELECT \"URI\" as X, VALUE as Y FROM " + attribute_table_decimal;
 	public static final String select_mapping_attribute_double = "SELECT \"URI\" as X, VALUE as Y FROM " + attribute_table_double;
 	public static final String select_mapping_attribute_datetime = "SELECT \"URI\" as X, VALUE as Y FROM " + attribute_table_datetime;
 	public static final String select_mapping_attribute_boolean = "SELECT \"URI\" as X, VALUE as Y FROM " + attribute_table_boolean;
 
 	public static final String whereSingleCondition = "IDX = %d";
 
 	public static final String whereIntervalCondition = "IDX >= %d AND IDX <= %d";
 
 	private static final OBDADataFactory dfac = OBDADataFactoryImpl.getInstance();
 
 	private static final OntologyFactory ofac = OntologyFactoryImpl.getInstance();
 
 	private static final IRIFactory ifac = OBDADataFactoryImpl.getIRIFactory();
 
 	private Map<IRI, Integer> classIndexes = new LinkedHashMap<IRI, Integer>();
 
 	private Map<IRI, Integer> roleIndexes = new LinkedHashMap<IRI, Integer>();
 
 	private Map<Description, Description> equivalentIndex = new HashMap<Description, Description>();
 
 	private Map<IRI, List<Interval>> classIntervals = new LinkedHashMap<IRI, List<Interval>>();
 
 	private Map<IRI, List<Interval>> roleIntervals = new LinkedHashMap<IRI, List<Interval>>();
 
 	private LinkedHashSet<String> uriIds = new LinkedHashSet<String> ();
 	
 	private Properties config;
 
 	private DAG dag;
 
 	// private DAG pureIsa;
 
 	private Ontology aboxDependencies;
 
 	private Ontology ontology;
 
 	private boolean isIndexed;
 
 	final static int CLASS_TYPE = 1;
 
 	final static int ROLE_TYPE = 2;
 
 	private static final boolean mergeUniions = false;
 
 	// private HashMap<Integer, Boolean> emptynessIndexes = new HashMap<Integer,
 	// Boolean>();
 
 	private HashSet<SemanticIndexRecord> nonEmptyEntityRecord = new HashSet<SemanticIndexRecord>();
 
 	private List<RepositoryChangedListener> changeList;
 
 	public RDBMSSIRepositoryManager() {
 		this(null);
 	}
 
 	public RDBMSSIRepositoryManager(Set<Predicate> vocabulary) {
 
 		if (vocabulary != null) {
 			setVocabulary(vocabulary);
 		}
 
 		changeList = new LinkedList<RepositoryChangedListener>();
 	}
 
 	public void addRepositoryChangedListener(RepositoryChangedListener list) {
 		this.changeList.add(list);
 	}
 
 	// public HashMap<Predicate, Integer> getIndexes() {
 	// return indexes;
 	// }
 
 	// public HashMap<Integer, Boolean> getEmptynessIndexes() {
 	// return emptynessIndexes;
 	// }
 
 	public boolean getIsIndexed() {
 		return this.isIndexed;
 	}
 
 	public boolean getmergeUnions() {
 		return this.mergeUniions;
 	}
 
 	@Override
 	public void setConfig(Properties config) {
 		this.config = config;
 	}
 
 	public DAG getDAG() {
 		return dag;
 	}
 
 	@Override
 	public void setTBox(Ontology ontology) {
 
 		this.ontology = ontology;
 
 		log.debug("Ontology: {}", ontology.toString());
 
 		/*
 		 * 
 		 * PART 1: Collecting relevant nodes for mappings
 		 */
 
 		/*
 		 * Collecting relevant nodes for each role. For a Role P, the relevant
 		 * nodes are, the DAGNode for P, and the top most inverse children of P
 		 */
 
 		dag = DAGConstructor.getISADAG(ontology);
 
 		// USE THE DAG GRAPHS FOR DEBUGGING
 		//
 		// try {
 		// GraphGenerator.dumpISA(dag, "given");
 		// } catch (IOException e1) {
 		// e1.printStackTrace();
 		// }
 
 		dag.clean();
 
 		DAG pureIsa = DAGConstructor.filterPureISA(dag);
 		aboxDependencies = DAGConstructor.getSigmaOntology(dag);
 
 		pureIsa.clean();
 		pureIsa.index();
 
 		DAGOperations.buildAncestors(dag);
 		DAGOperations.buildDescendants(dag);
 
 		/***
 		 * Copying the equivalences that might bet lost from the translation
 		 */
 		for (Description d : dag.equi_mappings.keySet()) {
 			pureIsa.equi_mappings.put(d, dag.equi_mappings.get(d));
 			equivalentIndex.put(d, dag.equi_mappings.get(d));
 		}
 
 		/*
 		 * Creating cache of semantic indexes and ranges
 		 */
 		Set<Description> descriptions = pureIsa.allnodes.keySet();
 		for (Description description : descriptions) {
 			if (description instanceof OClass) {
 
 				OClass cdesc = (OClass) description;
 				DAGNode node = pureIsa.get(description);
 				int idx = node.getIndex();
 				List<Interval> intervals = node.getRange().getIntervals();
 
 				IRI iri = cdesc.getPredicate().getName();
 				classIndexes.put(iri, idx);
 				classIntervals.put(iri, intervals);
 
 			} else if (description instanceof PropertyImpl) {
 				PropertyImpl cdesc = (PropertyImpl) description;
 
 				if (cdesc.isInverse()) {
 					/* Inverses don't get indexes or intervals */
 					continue;
 				}
 
 				DAGNode node = pureIsa.get(description);
 				int idx = node.getIndex();
 				List<Interval> intervals = node.getRange().getIntervals();
 
 				IRI iri = cdesc.getPredicate().getName();
 				roleIndexes.put(iri, idx);
 				roleIntervals.put(iri, intervals);
 
 			} 
 		}
 
 		// try {
 		// GraphGenerator.dumpISA(dag, "no-cycles");
 		// GraphGenerator.dumpISA(pureIsa, "isa-indexed");
 		//
 		// } catch (IOException e) {
 		//
 		// }
 
 	}
 
 	@Override
 	public String getType() {
 		return TYPE_SI;
 	}
 
 	@Override
 	public void getTablesDDL(OutputStream outstream) throws IOException {
 		log.debug("Generating DDL for ABox tables");
 
 		BufferedWriter out = new BufferedWriter(new OutputStreamWriter(outstream));
 
 		out.append(create_idx);
 		out.append(";\n");
 
 		out.append(create_interval);
 		out.append(";\n");
 
 		out.append(class_table_create);
 		out.append(";\n");
 
 		out.append(role_table_create);
 		out.append(";\n");
 
 		out.append(attribute_table_literal_create);
 		out.append(";\n");
 		out.append(attribute_table_string_create);
 		out.append(";\n");
 		out.append(attribute_table_integer_create);
 		out.append(";\n");
 		out.append(attribute_table_decimal_create);
 		out.append(";\n");
 		out.append(attribute_table_double_create);
 		out.append(";\n");
 		out.append(attribute_table_datetime_create);
 		out.append(";\n");
 		out.append(attribute_table_boolean_create);
 		out.append(";\n");
 
 		out.flush();
 	}
 
 	@Override
 	public void getIndexDDL(OutputStream outstream) throws IOException {
 
 		BufferedWriter out = new BufferedWriter(new OutputStreamWriter(outstream));
 
 		out.append(indexclass1);
 		out.append(";\n");
 		out.append(indexclass2);
 		out.append(";\n");
 		out.append(indexrole1);
 		out.append(";\n");
 		out.append(indexrole2);
 		out.append(";\n");
 		out.append(indexrole3);
 		out.append(";\n");
 
 		out.append(indexattribute_literal1);
 		out.append(";\n");
 		out.append(indexattribute_string1);
 		out.append(";\n");
 		out.append(indexattribute_integer1);
 		out.append(";\n");
 		out.append(indexattribute_decimal1);
 		out.append(";\n");
 		out.append(indexattribute_double1);
 		out.append(";\n");
 		out.append(indexattribute_datetime1);
 		out.append(";\n");
 		out.append(indexattribute_boolean1);
 		out.append(";\n");
 
 		out.append(indexattribute_literal2);
 		out.append(";\n");
 		out.append(indexattribute_string2);
 		out.append(";\n");
 		out.append(indexattribute_integer2);
 		out.append(";\n");
 		out.append(indexattribute_decimal2);
 		out.append(";\n");
 		out.append(indexattribute_double2);
 		out.append(";\n");
 		out.append(indexattribute_datetime2);
 		out.append(";\n");
 		out.append(indexattribute_boolean2);
 		out.append(";\n");
 
 		out.append(indexattribute_literal3);
 		out.append(";\n");
 		out.append(indexattribute_string3);
 		out.append(";\n");
 		out.append(indexattribute_integer3);
 		out.append(";\n");
 		out.append(indexattribute_decimal3);
 		out.append(";\n");
 		out.append(indexattribute_double3);
 		out.append(";\n");
 		out.append(indexattribute_datetime3);
 		out.append(";\n");
 		out.append(indexattribute_boolean3);
 		out.append(";\n");
 
 		out.flush();
 	}
 
 	@Override
 	public void getSQLInserts(Iterator<Assertion> data, OutputStream outstream) throws IOException {
 
 		BufferedWriter out = new BufferedWriter(new OutputStreamWriter(outstream));
 
 		String role_insert_str = role_insert.replace("?", "%s");
 
 		String attribute_insert_literal_str = attribute_table_literal_insert.replace("?", "%s");
 		String attribute_insert_string_str = attribute_table_string_insert.replace("?", "%s");
 		String attribute_insert_integer_str = attribute_table_integer_insert.replace("?", "%s");
 		String attribute_insert_decimal_str = attribute_table_decimal_insert.replace("?", "%s");
 		String attribute_insert_double_str = attribute_table_double_insert.replace("?", "%s");
 		String attribute_insert_date_str = attribute_table_datetime_insert.replace("?", "%s");
 		String attribute_insert_boolean_str = attribute_table_boolean_insert.replace("?", "%s");
 
 		String cls_insert_str = class_insert.replace("?", "%s");
 
 		while (data.hasNext()) {
 			Assertion ax = data.next();
 
 			if (ax instanceof BinaryAssertion) {
 
 				BinaryAssertion binaryAssertion = (BinaryAssertion) ax;
 				Constant c1 = binaryAssertion.getValue1();
 				Constant c2 = binaryAssertion.getValue2();
 
 				if (c2 instanceof ValueConstant) {
 
 					DataPropertyAssertion attributeABoxAssertion = (DataPropertyAssertion) ax;
 					String prop = attributeABoxAssertion.getAttribute().getName().toString();
 
 					String uri;
 
 					boolean c1isBNode = c1 instanceof BNode;
 
 					if (c1isBNode)
 						uri = ((BNode) c1).getName();
 					else
 						uri = ((URIConstant) c1).getURI().toString();
 
 					ValueConstant value = attributeABoxAssertion.getValue();
 					String lit = value.getValue();
 					String lang = value.getLanguage();
 					if (lang != null)
 						lang = lang.toLowerCase();
 
 					Predicate.COL_TYPE attributeType = value.getType();
 
 					// Predicate propPred =
 					// dfac.getDataPropertyPredicate(ifac.construct(prop));
 					// Property propDesc = ofac.createProperty(propPred);
 
 					int idx = getIndex(attributeABoxAssertion.getPredicate().getName(), 2);
 
 					// DAGNode node = pureIsa.getRoleNode(propDesc);
 					// int idx = node.getIndex();
 
 					switch (attributeType) {
 					case LITERAL:
 						out.append(String.format(attribute_insert_literal_str, getQuotedString(uri), getQuotedString(lit),
 								getQuotedString(lang), idx, c1isBNode));
 						break;
 					case STRING:
 						out.append(String.format(attribute_insert_string_str, getQuotedString(uri), getQuotedString(lit), idx, c1isBNode));
 						break;
 					case INTEGER:
 						out.append(String.format(attribute_insert_integer_str, getQuotedString(uri), Long.parseLong(lit), idx, c1isBNode));
 						break;
 					case DECIMAL:
 						out.append(String.format(attribute_insert_decimal_str, getQuotedString(uri), parseBigDecimal(lit), idx, c1isBNode));
 						break;
 					case DOUBLE:
 						out.append(String.format(attribute_insert_double_str, getQuotedString(uri), Double.parseDouble(lit), idx, c1isBNode));
 						break;
 					case DATETIME:
 						out.append(String.format(attribute_insert_date_str, getQuotedString(uri), parseTimestamp(lit), idx, c1isBNode));
 						break;
 					case BOOLEAN:
 						out.append(String.format(attribute_insert_boolean_str, getQuotedString(uri), Boolean.parseBoolean(lit), idx,
 								c1isBNode));
 						break;
 					}
 				} else if (c2 instanceof ObjectConstant) {
 
 					ObjectPropertyAssertion roleABoxAssertion = (ObjectPropertyAssertion) ax;
 					String prop = roleABoxAssertion.getRole().getName().toString();
 					String uri1;
 					String uri2;
 
 					boolean c1isBNode = c1 instanceof BNode;
 					boolean c2isBNode = c2 instanceof BNode;
 
 					if (c1isBNode)
 						uri1 = ((BNode) c1).getName();
 					else
 						uri1 = ((URIConstant) c1).getURI().toString();
 
 					if (c2isBNode)
 						uri2 = ((BNode) c2).getName();
 					else
 						uri2 = ((URIConstant) c2).getURI().toString();
 
 					/***
 					 * Dealing with any equivalent canonical properties. If
 					 * there is any, and it is inverse we need to invert the
 					 * value positions.
 					 */
 
 					Predicate propPred = dfac.getObjectPropertyPredicate(ifac.construct(prop));
 					Property propDesc = ofac.createProperty(propPred);
 
 					if (dag.equi_mappings.containsKey(propDesc)) {
 						Property desc = (Property) dag.equi_mappings.get(propDesc);
 						if (desc.isInverse()) {
 							String tmp = uri1;
 							boolean tmpIsBnode = c1isBNode;
 
 							uri1 = uri2;
 							c1isBNode = c2isBNode;
 							uri2 = tmp;
 							c2isBNode = tmpIsBnode;
 						}
 					}
 					// DAGNode node = pureIsa.getRoleNode(propDesc);
 					// int idx = node.getIndex();
 
 					int idx = getIndex(roleABoxAssertion.getPredicate().getName(), 2);
 
 					out.append(String.format(role_insert_str, getQuotedString(uri1), getQuotedString(uri2), idx, c1isBNode, c2isBNode));
 
 				}
 			} else if (ax instanceof ClassAssertion) {
 
 				ClassAssertion classAssertion = (ClassAssertion) ax;
 				Constant c1 = classAssertion.getObject();
 
 				String uri;
 
 				boolean c1isBNode = c1 instanceof BNode;
 
 				if (c1isBNode)
 					uri = ((BNode) c1).getName();
 				else
 					uri = ((URIConstant) c1).getURI().toString();
 
 				// Predicate clsPred = classAssertion.getConcept();
 				// ClassDescription clsDesc = ofac.createClass(clsPred);
 				//
 				int idx = getIndex(classAssertion.getPredicate().getName(), 1);
 				//
 				// DAGNode node = pureIsa.getClassNode(clsDesc);
 				// int idx = node.getIndex();
 
 				out.append(String.format(cls_insert_str, getQuotedString(uri), idx, c1isBNode));
 			}
 			out.append(";\n");
 		}
 		out.flush();
 	}
 
 	/***
 	 * Returns the index (semantic index) for a class or property. The IRI
 	 * identifies a class if ype = 1, identifies a property if type = 2.
 	 * 
 	 * @param name
 	 * @param i
 	 * @return
 	 */
 	private int getIndex(IRI name, int type) {
 		if (type == 1) {
 			Integer index = classIndexes.get(name);
 		
 			if (index == null) {
 				/* direct name is not indexed, maybe there is an equivalent */
 				OClass c = (OClass)ofac.createClass(name);
 				OClass equivalent = (OClass)equivalentIndex.get(c);
 				return classIndexes.get(equivalent.getPredicate().getName());
 			}
 					
 			return index;
 		} else if (type == 2) {
 			Integer index = roleIndexes.get(name);
 			
 			if (index == null) {
 				/* direct name is not indexed, maybe there is an equivalent, we need to test
 				 * with object properties and data properties */
 				Property c = ofac.createObjectProperty(name);
 				Property equivalent = (Property)equivalentIndex.get(c);
 				
 				Integer index1 = roleIndexes.get(equivalent.getPredicate().getName());
 				
 				if (index1 != null)
 					return index1;
 				
 				/* object property equivalent failed, we now look for data property equivalent */
 				
 				c = ofac.createDataProperty(name);
 				equivalent = (Property)equivalentIndex.get(c);
 				
 				index1 = roleIndexes.get(equivalent.getPredicate().getName());
 				return index1;
 			}
 			
 			return index;
 				
 		}
 		throw new RuntimeException("Could not find index for: IRI =  " + name + " type = " + type);
 	}
 
 	/***
 	 * Returns the intervals (semantic index) for a class or property. The IRI
 	 * identifies a class if type = 1, identifies a property if type = 2.
 	 * 
 	 * @param name
 	 * @param i
 	 * @return
 	 */
 	private List<Interval> getIntervals(IRI name, int type) {
 		if (type == 1) {
 			return classIntervals.get(name);
 		} else if (type == 2) {
 			return roleIntervals.get(name);
 		}
 		throw new RuntimeException("Could not find semantic index intervals for: IRI =  " + name + " type = " + type);
 	}
 
 	@Override
 	public void createDBSchema(Connection conn, boolean dropExisting) throws SQLException {
 
 		log.debug("Creating data tables");
 
 		Statement st = conn.createStatement();
 
 		if (dropExisting) {
 			try {
 				log.debug("Droping existing tables");
 				dropDBSchema(conn);
 			} catch (SQLException e) {
 				log.debug(e.getMessage(), e);
 			}
 		}
 
 		if (isDBSchemaDefined(conn)) {
 			log.debug("Schema already exists. Skipping creation");
 			return;
 		}
 		st.addBatch(create_uri_id);
 		
 		st.addBatch(create_idx);
 		st.addBatch(create_interval);
 		st.addBatch(create_emptyness_index);
 
 		st.addBatch(class_table_create);
 		st.addBatch(role_table_create);
 
 		st.addBatch(attribute_table_literal_create);
 		st.addBatch(attribute_table_string_create);
 		st.addBatch(attribute_table_integer_create);
 		st.addBatch(attribute_table_decimal_create);
 		st.addBatch(attribute_table_double_create);
 		st.addBatch(attribute_table_datetime_create);
 		st.addBatch(attribute_table_boolean_create);
 
 		st.executeBatch();
 		st.close();
 	}
 
 	@Override
 	public void createIndexes(Connection conn) throws SQLException {
 		log.debug("Creating indexes");
 		Statement st = conn.createStatement();
 
 //		st.addBatch(indexclass1);
 //		st.addBatch(indexclass2);
 //		st.addBatch(indexrole1);
 //		st.addBatch(indexrole2);
 //		st.addBatch(indexrole3);
 
 		st.addBatch(indexattribute_literal1);
 		st.addBatch(indexattribute_string1);
 		st.addBatch(indexattribute_integer1);
 		st.addBatch(indexattribute_decimal1);
 		st.addBatch(indexattribute_double1);
 		st.addBatch(indexattribute_datetime1);
 		st.addBatch(indexattribute_boolean1);
 
 		st.addBatch(indexattribute_literal2);
 		st.addBatch(indexattribute_string2);
 		st.addBatch(indexattribute_integer2);
 		st.addBatch(indexattribute_decimal2);
 		st.addBatch(indexattribute_double2);
 		st.addBatch(indexattribute_datetime2);
 		st.addBatch(indexattribute_boolean2);
 
 		st.addBatch(indexattribute_literal3);
 		st.addBatch(indexattribute_string3);
 		st.addBatch(indexattribute_integer3);
 		st.addBatch(indexattribute_decimal3);
 		st.addBatch(indexattribute_double3);
 		st.addBatch(indexattribute_datetime3);
 		st.addBatch(indexattribute_boolean3);
 
 		st.addBatch(indexclass_composite);
 		st.addBatch(indexrole_composite1);
 		st.addBatch(indexrole_composite2);
 		
 		st.addBatch(indexclassfull2);
 		st.addBatch(indexrolefull22);
 		
 		st.executeBatch();
 		
 		log.debug("Executing ANALYZE");
 		st.addBatch(analyze);
 		st.executeBatch();
 		
 		st.close();
 
 		isIndexed = true;
 	}
 
 	@Override
 	public void dropDBSchema(Connection conn) throws SQLException {
 
 //		Statement st = conn.createStatement();
 //
 //		st.addBatch(drop_idx);
 //		st.addBatch(drop_interval);
 //		st.addBatch(drop_emptyness);
 //
 //		st.addBatch(class_table_drop);
 //		st.addBatch(role_table_drop);
 //
 //		st.addBatch(attribute_table_literal_drop);
 //		st.addBatch(attribute_table_string_drop);
 //		st.addBatch(attribute_table_integer_drop);
 //		st.addBatch(attribute_table_decimal_drop);
 //		st.addBatch(attribute_table_double_drop);
 //		st.addBatch(attribute_table_datetime_drop);
 //		st.addBatch(attribute_table_boolean_drop);
 //
 //		st.executeBatch();
 //		st.close();
 	}
 
 	@Override
 	public int insertData(Connection conn, Iterator<Assertion> data, int commitLimit, int batchLimit) throws SQLException {
 		log.debug("Inserting data into DB");
 
 		// The precondition for the limit number must be greater or equal to
 		// one.
 		commitLimit = (commitLimit < 1) ? 1 : commitLimit;
 		batchLimit = (batchLimit < 1) ? 1 : batchLimit;
 
 		boolean oldAutoCommit = conn.getAutoCommit();
 		conn.setAutoCommit(false);
 
 		// Create the insert statement for all assertions (i.e, concept, role
 		// and attribute)
 		PreparedStatement uriidStm = conn.prepareStatement(uriid_insert);
 		PreparedStatement classStm = conn.prepareStatement(class_insert);
 		PreparedStatement roleStm = conn.prepareStatement(role_insert);
 		PreparedStatement attributeLiteralStm = conn.prepareStatement(attribute_table_literal_insert);
 		PreparedStatement attributeStringStm = conn.prepareStatement(attribute_table_string_insert);
 		PreparedStatement attributeIntegerStm = conn.prepareStatement(attribute_table_integer_insert);
 		PreparedStatement attributeDecimalStm = conn.prepareStatement(attribute_table_decimal_insert);
 		PreparedStatement attributeDoubleStm = conn.prepareStatement(attribute_table_double_insert);
 		PreparedStatement attributeDateStm = conn.prepareStatement(attribute_table_datetime_insert);
 		PreparedStatement attributeBooleanStm = conn.prepareStatement(attribute_table_boolean_insert);
 
 		// For counting the insertion
 		InsertionMonitor monitor = new InsertionMonitor();
 
 		int batchCount = 0;
 		int commitCount = 0;
 
 		while (data.hasNext()) {
 			Assertion ax = data.next();
 
 			try {
 
 				// log.debug("Inserting statement: {}", ax);
 				batchCount += 1;
 				commitCount += 1;
 
 				addPreparedStatement(uriidStm, classStm, roleStm, attributeLiteralStm, attributeStringStm, attributeIntegerStm, attributeDecimalStm,
 						attributeDoubleStm, attributeDateStm, attributeBooleanStm, monitor, ax);
 
 				/*
 				 * Register non emptyness
 				 */
 				int index = 0;
 				if (ax instanceof ClassAssertion) {
 					index = getIndex(ax.getPredicate().getName(), 1);
 				} else {
 					index = getIndex(ax.getPredicate().getName(), 2);
 				}
 				SemanticIndexRecord record = SemanticIndexRecord.getRecord(ax, index);
 				nonEmptyEntityRecord.add(record);
 
 			} catch (Exception e) {
 				monitor.fail(ax.getPredicate());
 			}
 
 			// Check if the batch count is already in the batch limit
 			if (batchCount == batchLimit) {
 				executeBatch(uriidStm);
 				executeBatch(roleStm);
 				executeBatch(attributeLiteralStm);
 				executeBatch(attributeStringStm);
 				executeBatch(attributeIntegerStm);
 				executeBatch(attributeDecimalStm);
 				executeBatch(attributeDoubleStm);
 				executeBatch(attributeDateStm);
 				executeBatch(attributeBooleanStm);
 				executeBatch(classStm);
 				batchCount = 0; // reset the counter
 			}
 
 			// Check if the commit count is already in the commit limit
 			if (commitCount == commitLimit) {
 				conn.commit();
 				commitCount = 0; // reset the counter
 			}
 		}
 
 		// Execute the rest of the batch
 		executeBatch(uriidStm);
 		executeBatch(roleStm);
 		executeBatch(attributeLiteralStm);
 		executeBatch(attributeStringStm);
 		executeBatch(attributeIntegerStm);
 		executeBatch(attributeDecimalStm);
 		executeBatch(attributeDoubleStm);
 		executeBatch(attributeDateStm);
 		executeBatch(attributeBooleanStm);
 		executeBatch(classStm);
 
 	
 		// Close all open statements
 		closeStatement(uriidStm);
 		closeStatement(roleStm);
 		closeStatement(attributeLiteralStm);
 		closeStatement(attributeStringStm);
 		closeStatement(attributeIntegerStm);
 		closeStatement(attributeDecimalStm);
 		closeStatement(attributeDoubleStm);
 		closeStatement(attributeDateStm);
 		closeStatement(attributeBooleanStm);
 		closeStatement(classStm);
 
 		// Commit the rest of the batch insert
 		conn.commit();
 
 		conn.setAutoCommit(oldAutoCommit);
 
 		// Print the monitoring log
 		monitor.printLog();
 
 		fireRepositoryChanged();
 
 		return monitor.getSuccessCount();
 	}
 
 	private void fireRepositoryChanged() {
 		for (RepositoryChangedListener listener : changeList) {
 			listener.repositoryChanged();
 		}
 	}
 
 	private void addPreparedStatement(PreparedStatement uriidStm, PreparedStatement classStm, PreparedStatement roleStm, PreparedStatement attributeLiteralStm,
 			PreparedStatement attributeStringStm, PreparedStatement attributeIntegerStm, PreparedStatement attributeDecimalStm,
 			PreparedStatement attributeDoubleStm, PreparedStatement attributeDateStm, PreparedStatement attributeBooleanStm,
 			InsertionMonitor monitor, Assertion ax) throws SQLException {
 		int uri_id = 0;
 		int uri2_id = 0;
 		boolean newUri = false;
 		if (ax instanceof BinaryAssertion) {
 			// Get the data property assertion
 			BinaryAssertion attributeAssertion = (BinaryAssertion) ax;
 			Predicate predicate = attributeAssertion.getPredicate();
 
 			Constant object = attributeAssertion.getValue2();
 			Predicate.COL_TYPE attributeType = object.getType();
 
 			// Construct the database INSERT statements
 			ObjectConstant subject = (ObjectConstant) attributeAssertion.getValue1();
 
 			String uri = subject.getValue();
 			newUri = uriIds.add(uri);
 			if (newUri) {
 				uri_id = uriIds.size();
 				uriidStm.setInt(1, uri_id);
 				uriidStm.setString(2, uri);
 				uriidStm.addBatch();
 			} else {
 				uri_id = idOfURI(uri);
 			}
 			
 			boolean c1isBNode = subject instanceof BNode;
 
 			int idx = getAttributeIndex(predicate);
 
 			// The insertion is based on the datatype from TBox
 			String value = object.getValue();
 			String lang = object.getLanguage();
 
 			switch (attributeType) {
 			case BNODE:
 			case OBJECT:
 				// Get the object property assertion
 				String uri2 = object.getValue();
 				boolean c2isBNode = object instanceof BNode;
 
 				if (isInverse(predicate)) {
 
 					/* Swapping values */
 
 					String tmp = uri;
 					uri = uri2;
 					uri2 = tmp;
 
 					boolean tmpb = c1isBNode;
 					c1isBNode = c2isBNode;
 					c2isBNode = tmpb;
 				}
 
 				// Construct the database INSERT statement
 				// replace URIs with their ids
 				
 				newUri = uriIds.add(uri);
 				if (newUri) {
 					uri_id = uriIds.size();
 					uriidStm.setInt(1, uri_id);
 					uriidStm.setString(2, uri);
 					uriidStm.addBatch();
 				} else {
 					uri_id = idOfURI(uri);
 				}
 				newUri = uriIds.add(uri2);
 				if (newUri) {
 					uri2_id = uriIds.size();
 					uriidStm.setInt(1, uri2_id);
 					uriidStm.setString(2, uri2);
 					uriidStm.addBatch();
 				} else {
 					uri2_id = idOfURI(uri2);
 				}
 				
 				
 				//roleStm.setString(1, uri);
 				//roleStm.setString(2, uri2);
 				roleStm.setInt(1, uri_id);
 				roleStm.setInt(2, uri2_id);
 				
 				
 				roleStm.setInt(3, idx);
 				roleStm.setBoolean(4, c1isBNode);
 				roleStm.setBoolean(5, c2isBNode);
 				roleStm.addBatch();
 
 				// log.debug("role");
 
 				// log.debug("inserted: {} {}", uri, uri2);
 				// log.debug("inserted: {} property", idx);
 
 				break;
 			case LITERAL:
 			case LITERAL_LANG:
 				setInputStatement(attributeLiteralStm, uri_id, value, lang, idx, c1isBNode);
 				// log.debug("literal");
 				break;
 			case STRING:
 				setInputStatement(attributeStringStm, uri_id, value, idx, c1isBNode);
 				// log.debug("string");
 				break;
 			case INTEGER:
 				if (value.charAt(0) == '+')
 					value = value.substring(1, value.length());
 				setInputStatement(attributeIntegerStm, uri_id, Long.parseLong(value), idx, c1isBNode);
 				// log.debug("Int");
 				break;
 			case DECIMAL:
 				setInputStatement(attributeDecimalStm, uri_id, parseBigDecimal(value), idx, c1isBNode);
 				// log.debug("BigDecimal");
 				break;
 			case DOUBLE:
 				setInputStatement(attributeDoubleStm, uri_id, Double.parseDouble(value), idx, c1isBNode);
 				// log.debug("Double");
 				break;
 			case DATETIME:
 				setInputStatement(attributeDateStm, uri_id, parseTimestamp(value), idx, c1isBNode);
 				// log.debug("Date");
 				break;
 			case BOOLEAN:
 				value = getBooleanString(value); // PostgreSQL
 													// abbreviates the
 													// boolean value to
 													// 't' and 'f'
 				setInputStatement(attributeBooleanStm, uri_id, Boolean.parseBoolean(value), idx, c1isBNode);
 				// log.debug("boolean");
 				break;
 			case UNSUPPORTED:
 			default:
 				log.warn("Ignoring assertion: {}", ax);
 			}
 			monitor.success(); // advanced the success counter
 
 		} else if (ax instanceof ClassAssertion) {
 			// Get the class assertion
 			ClassAssertion classAssertion = (ClassAssertion) ax;
 			Predicate concept = classAssertion.getConcept();
 
 			// Construct the database INSERT statements
 			ObjectConstant c1 = classAssertion.getObject();
 
 			String uri;
 
 			boolean c1isBNode = c1 instanceof BNode;
 
 			if (c1isBNode)
 				uri = ((BNode) c1).getName();
 			else
 				uri = ((URIConstant) c1).getURI().toString();
 
 			// Construct the database INSERT statement
 			newUri = uriIds.add(uri);
 			if (newUri) {
 				uri_id = uriIds.size();
 				uriidStm.setInt(1, uri_id);
 				uriidStm.setString(2, uri);
 				uriidStm.addBatch();
 			} else {
 				uri_id = idOfURI(uri);
 			}
 
 			classStm.setInt(1, uri_id);
 			int conceptIndex = getConceptIndex(concept);
 			classStm.setInt(2, conceptIndex);
 			classStm.setBoolean(3, c1isBNode);
 			classStm.addBatch();
 
 			// log.debug("inserted: {} {} class", uri, conceptIndex);
 
 			monitor.success(); // advanced the success counter
 
 			// log.debug("Class");
 		}
 	}
 
 	private int idOfURI(String uri) {
 		int i = 0;
 		for (String s : uriIds) {
 		    ++i;
 		    if (s.equals(uri)) {
 		        break;
 		    }
 		}
 		return i;
 	}
 
 	private void closeStatement(PreparedStatement statement) throws SQLException {
 		statement.close();
 	}
 
 	private void executeBatch(PreparedStatement statement) throws SQLException {
 		statement.executeBatch();
 		statement.clearBatch();
 	}
 
 	private boolean isInverse(Predicate role) {
 		Property property = ofac.createProperty(role);
 		if (dag.equi_mappings.containsKey(property)) {
 			Property desc = (Property) dag.equi_mappings.get(property);
 			if (desc.isInverse()) {
 				return true;
 			}
 		}
 		return false;
 	}
 
 	private int getConceptIndex(Predicate concept) {
 		return getIndex(concept.getName(), 1);
 		// Integer idxc = indexes.get(concept);
 		// if (idxc == null) {
 		// ClassDescription description = ofac.createClass(concept);
 		// DAGNode node = pureIsa.getClassNode(description);
 		// if (node == null) {
 		// log.error("Found class without node: " + concept);
 		// }
 		// idxc = new Integer(node.getIndex());
 		// indexes.put(concept, idxc);
 		// }
 		// return idxc.intValue();
 	}
 
 	// /***
 	// * We register that the mappings that store data related to this assertion
 	// * is not empty. Given any assertion there are two mappings that become
 	// * non-empty. the "DL" style mapping and the "Triple" style mapping. For
 	// * example, given the assertion Class('mariano'), where mariano is a URI,
 	// * the mappings with the following head become non-empty:
 	// * <p>
 	// * Class(uri(x))
 	// * <p>
 	// * triple(uri(x),uri(rdf:type),uri(Class))
 	// *
 	// * <p>
 	// * Note, this method also takes into account the ontology in that an ABox
 	// * assertion about a role R affects non-emptyness about any S such that R
 	// * subPropertyOf S, similar for concept hierarchies, domain, range and
 	// * inverse inferences.
 	// */
 	// private void assertNonEmptyness(Assertion assertion) {
 	//
 	// if (assertion instanceof BinaryAssertion) {
 	// BinaryAssertion ba = (BinaryAssertion) assertion;
 	//
 	// /*
 	// * getting the data for each term
 	// */
 	// Predicate p = ba.getPredicate();
 	//
 	// Predicate typeSubject = dfac.getTypePredicate(ba.getValue1().getType());
 	// Predicate typePredicate = dfac.getUriTemplatePredicate(1);
 	// Predicate typeObject = dfac.getTypePredicate(ba.getValue2().getType());
 	//
 	// Function fsubject;
 	// if (ba.getValue1().getType() != COL_TYPE.LITERAL_LANG) {
 	// fsubject = dfac.getFunctionalTerm(typeSubject, dfac.getVariable("x"));
 	// } else {
 	// fsubject = dfac.getFunctionalTerm(typeSubject, dfac.getVariable("x"),
 	// dfac.getVariable("x2"));
 	// }
 	//
 	// Function fpredicate = dfac.getFunctionalTerm(typePredicate,
 	// dfac.getURIConstant(p.getName()));
 	// Function fobject;
 	//
 	// if (ba.getValue2().getType() != COL_TYPE.LITERAL_LANG) {
 	// fobject = dfac.getFunctionalTerm(typeObject, dfac.getVariable("y"));
 	// } else {
 	// fobject = dfac.getFunctionalTerm(typeObject, dfac.getVariable("y"),
 	// dfac.getVariable("y2"));
 	// }
 	//
 	// // DL style head
 	// Function headDL = dfac.getFunctionalTerm(p, fsubject, fobject);
 	//
 	// // Triple style head
 	// Function headTriple =
 	// dfac.getFunctionalTerm(OBDAVocabulary.QUEST_TRIPLE_PRED, fsubject,
 	// fpredicate, fobject);
 	//
 	// assertNonEmptyness(headDL);
 	// assertNonEmptyness(headTriple);
 	//
 	// /*
 	// * Dealing with emptyness of upper level roles in the hierarchy
 	// */
 	//
 	// DAGNode node = dag.get(ofac.createProperty(p));
 	// Set<DAGNode> parents = new HashSet<DAGNode>(node.getAncestors());
 	//
 	// Set<DAGNode> equivalents = new HashSet<DAGNode>();
 	// for (DAGNode parent : parents)
 	// equivalents.addAll(parent.getEquivalents());
 	//
 	// parents.addAll(equivalents);
 	//
 	// for (DAGNode parent : parents) {
 	// Description desc = parent.getDescription();
 	// Property parentProp = (Property) desc;
 	// Predicate parentP = parentProp.getPredicate();
 	// boolean inverse = parentProp.isInverse();
 	//
 	// fpredicate = dfac.getFunctionalTerm(typePredicate,
 	// dfac.getURIConstant(parentP.getName()));
 	//
 	// // DL style head
 	// if (!inverse)
 	// headDL = dfac.getFunctionalTerm(parentP, fsubject, fobject);
 	// else
 	// headDL = dfac.getFunctionalTerm(parentP, fobject, fsubject);
 	//
 	// // Triple style head
 	// if (!inverse)
 	// headTriple = dfac.getFunctionalTerm(OBDAVocabulary.QUEST_TRIPLE_PRED,
 	// fsubject, fpredicate, fobject);
 	// else
 	// headTriple = dfac.getFunctionalTerm(OBDAVocabulary.QUEST_TRIPLE_PRED,
 	// fobject, fpredicate, fsubject);
 	//
 	// assertNonEmptyness(headDL);
 	// assertNonEmptyness(headTriple);
 	// }
 	//
 	// /*
 	// * Dealing with domain and range inferences
 	// */
 	//
 	// /*
 	// * First domain (inverse false for \exists R)
 	// */
 	// Description d = ofac.createPropertySomeRestriction(p, false);
 	// DAGNode dagNode = dag.get(d);
 	// parents = new HashSet<DAGNode>(dagNode.getAncestors());
 	// parents.addAll(dagNode.getEquivalents());
 	//
 	// equivalents = new HashSet<DAGNode>();
 	// for (DAGNode parent : parents)
 	// equivalents.addAll(parent.getEquivalents());
 	//
 	// parents.addAll(equivalents);
 	//
 	// for (DAGNode parent : parents) {
 	// // DL style head
 	// ClassDescription classDescription = (ClassDescription)
 	// parent.getDescription();
 	//
 	// assertNonEmptynessOfClassExpression(typePredicate, typeObject, fsubject,
 	// classDescription);
 	// }
 	//
 	// /*
 	// * First range (inverse true for \exists R^-)
 	// */
 	// d = ofac.createPropertySomeRestriction(p, true);
 	// dagNode = dag.get(d);
 	// parents = new HashSet<DAGNode>(dagNode.getAncestors());
 	// parents.addAll(dagNode.getEquivalents());
 	//
 	// equivalents = new HashSet<DAGNode>();
 	// for (DAGNode parent : parents)
 	// equivalents.addAll(parent.getEquivalents());
 	//
 	// parents.addAll(equivalents);
 	//
 	// for (DAGNode parent : parents) {
 	// // DL style head
 	// ClassDescription classDescription = (ClassDescription)
 	// parent.getDescription();
 	//
 	// assertNonEmptynessOfClassExpression(typePredicate, typeObject, fsubject,
 	// classDescription);
 	// }
 	//
 	// } else if (assertion instanceof UnaryAssertion) {
 	// UnaryAssertion ua = (UnaryAssertion) assertion;
 	//
 	// /*
 	// * getting the data for each term
 	// */
 	// Predicate p = ua.getPredicate();
 	//
 	// Predicate typeSubject = dfac.getTypePredicate(ua.getValue().getType());
 	// Predicate typePredicate = dfac.getUriTemplatePredicate(1);
 	// Predicate typeObject = dfac.getUriTemplatePredicate(1);
 	//
 	// Function fsubject;
 	// if (ua.getValue().getType() != COL_TYPE.LITERAL_LANG) {
 	// fsubject = dfac.getFunctionalTerm(typeSubject, dfac.getVariable("x"));
 	// } else {
 	// fsubject = dfac.getFunctionalTerm(typeSubject, dfac.getVariable("x"),
 	// dfac.getVariable("x2"));
 	// }
 	//
 	// Function fpredicate = dfac.getFunctionalTerm(typeObject,
 	// dfac.getURIConstant(ifac.construct(OBDAVocabulary.RDF_TYPE)));
 	// Function fobject = dfac.getFunctionalTerm(typePredicate,
 	// dfac.getURIConstant(p.getName()));
 	//
 	// // DL style head
 	// Function headDL = dfac.getFunctionalTerm(p, fsubject);
 	//
 	// // Triple style head
 	// Function headTriple =
 	// dfac.getFunctionalTerm(OBDAVocabulary.QUEST_TRIPLE_PRED, fsubject,
 	// fpredicate, fobject);
 	//
 	// assertNonEmptyness(headDL);
 	// assertNonEmptyness(headTriple);
 	//
 	// /*
 	// * Asserting non-emptyness for all the super classes of the current
 	// * class
 	// */
 	//
 	// DAGNode node = dag.get(ofac.createClass(p));
 	// Set<DAGNode> parents = new HashSet<DAGNode>(node.getAncestors());
 	// Set<DAGNode> equivalents = new HashSet<DAGNode>();
 	// for (DAGNode parent : parents)
 	// equivalents.addAll(parent.getEquivalents());
 	//
 	// parents.addAll(equivalents);
 	//
 	// for (DAGNode parent : parents) {
 	// // DL style head
 	// ClassDescription classDescription = (ClassDescription)
 	// parent.getDescription();
 	//
 	// assertNonEmptynessOfClassExpression(typePredicate, typeObject, fsubject,
 	// classDescription);
 	// }
 	//
 	// }
 	// }
 	//
 	// private void assertNonEmptynessOfClassExpression(Predicate typePredicate,
 	// Predicate typeObject, Function fsubject,
 	// ClassDescription classDescription) {
 	// Function fpredicate;
 	// Function fobject;
 	// Function headDL;
 	// Function headTriple;
 	// if (classDescription instanceof OClass) {
 	//
 	// OClass className = (OClass) classDescription;
 	//
 	// Predicate predicate = className.getPredicate();
 	// headDL = dfac.getFunctionalTerm(predicate, fsubject);
 	//
 	// fpredicate = dfac.getFunctionalTerm(typeObject,
 	// dfac.getURIConstant(ifac.construct(OBDAVocabulary.RDF_TYPE)));
 	// fobject = dfac.getFunctionalTerm(typePredicate,
 	// dfac.getURIConstant(predicate.getName()));
 	//
 	// // Triple style head
 	// headTriple = dfac.getFunctionalTerm(OBDAVocabulary.QUEST_TRIPLE_PRED,
 	// fsubject, fpredicate, fobject);
 	//
 	// assertNonEmptyness(headDL);
 	// assertNonEmptyness(headTriple);
 	// } else if (classDescription instanceof PropertySomeRestriction) {
 	// PropertySomeRestriction className = (PropertySomeRestriction)
 	// classDescription;
 	//
 	// Predicate predicate = className.getPredicate();
 	//
 	// fpredicate = dfac.getFunctionalTerm(typeObject,
 	// dfac.getURIConstant(ifac.construct(predicate.toString())));
 	// fobject = dfac.getFunctionalTerm(typePredicate, dfac.getVariable("X2"));
 	//
 	// if (!className.isInverse())
 	// headDL = dfac.getFunctionalTerm(predicate, fsubject, fobject);
 	// else
 	// headDL = dfac.getFunctionalTerm(predicate, fobject, fsubject);
 	//
 	// // Triple style head
 	// if (!className.isInverse())
 	// headTriple = dfac.getFunctionalTerm(OBDAVocabulary.QUEST_TRIPLE_PRED,
 	// fsubject, fpredicate, fobject);
 	// else
 	// headTriple = dfac.getFunctionalTerm(OBDAVocabulary.QUEST_TRIPLE_PRED,
 	// fobject, fpredicate, fsubject);
 	//
 	// assertNonEmptyness(headDL);
 	// assertNonEmptyness(headTriple);
 	// } else if (classDescription instanceof DataType) {
 	// DataType className = (DataType) classDescription;
 	//
 	// Predicate predicate = className.getPredicate();
 	//
 	// headDL = dfac.getFunctionalTerm(predicate, fsubject,
 	// dfac.getVariable("X2"));
 	//
 	// fpredicate = dfac.getFunctionalTerm(typeObject,
 	// dfac.getURIConstant(ifac.construct(predicate.toString())));
 	// fobject = dfac.getFunctionalTerm(typePredicate, dfac.getVariable("X2"));
 	//
 	// // Triple style head
 	//
 	// headTriple = dfac.getFunctionalTerm(OBDAVocabulary.QUEST_TRIPLE_PRED,
 	// fsubject, fpredicate, fobject);
 	// assertNonEmptyness(headDL);
 	// assertNonEmptyness(headTriple);
 	// }
 	// }
 
 	// private void assertNonEmptyness(Function headDL) {
 	// int hash1 = getIndexHash(headDL);
 	// emptynessIndexes.put(hash1, false);
 	// }
 
 	private int getAttributeIndex(Predicate attribute) {
 		return getIndex(attribute.getName(), 2);
 
 	}
 
 	private String getBooleanString(String value) {
 		if (value.equalsIgnoreCase("t") || value.equals("1")) {
 			return "true";
 		} else if (value.equalsIgnoreCase("f") || value.equals("0")) {
 			return "false";
 		} else {
 			return value; // nothing change
 		}
 	}
 
 	private void setInputStatement(PreparedStatement stm, int uri, String value, String lang, int idx, boolean isBnode)
 			throws SQLException {
 
 		// log.debug("inserted: {} {}", uri, value);
 		// log.debug("inserted: {} {}", lang, idx);
 		stm.setInt(1, uri);
 		stm.setString(2, value);
 		stm.setString(3, lang);
 		stm.setInt(4, idx);
 		stm.setBoolean(5, isBnode);
 
 		stm.addBatch();
 	}
 
 	private void setInputStatement(PreparedStatement stm, int uri, String value, int idx, boolean isBnode) throws SQLException {
 		// log.debug("inserted: {} {}", uri, value);
 		// log.debug("inserted: {}", idx);
 		stm.setInt(1, uri);
 		stm.setString(2, value);
 		stm.setInt(3, idx);
 		stm.setBoolean(4, isBnode);
 
 		stm.addBatch();
 	}
 
 	private void setInputStatement(PreparedStatement stm, int uri, int value, int idx, boolean isBnode) throws SQLException {
 		// log.debug("inserted: {} {}", uri, value);
 		// log.debug("inserted: {}", idx);
 		stm.setInt(1, uri);
 		stm.setInt(2, value);
 		stm.setInt(3, idx);
 		stm.setBoolean(4, isBnode);
 
 		stm.addBatch();
 	}
 
 	private void setInputStatement(PreparedStatement stm, int uri, BigDecimal value, int idx, boolean isBnode) throws SQLException {
 		// log.debug("inserted: {} {}", uri, value);
 		// log.debug("inserted: {}", idx);
 		stm.setInt(1, uri);
 		stm.setBigDecimal(2, value);
 		stm.setInt(3, idx);
 		stm.setBoolean(4, isBnode);
 
 		stm.addBatch();
 	}
 
 	private void setInputStatement(PreparedStatement stm, int uri, double value, int idx, boolean isBnode) throws SQLException {
 		// log.debug("inserted: {} {}", uri, value);
 		// log.debug("inserted: {}", idx);
 		stm.setInt(1, uri);
 		stm.setDouble(2, value);
 		stm.setInt(3, idx);
 		stm.setBoolean(4, isBnode);
 
 		stm.addBatch();
 	}
 
 	private void setInputStatement(PreparedStatement stm, int uri, Timestamp value, int idx, boolean isBnode) throws SQLException {
 		// log.debug("inserted: {} {}", uri, value);
 		// log.debug("inserted: {}", idx);
 		stm.setInt(1, uri);
 		stm.setTimestamp(2, value);
 		stm.setInt(3, idx);
 		stm.setBoolean(4, isBnode);
 
 		stm.addBatch();
 	}
 
 	private void setInputStatement(PreparedStatement stm, int uri, boolean value, int idx, boolean isBnode) throws SQLException {
 		// log.debug("inserted: {} {}", uri, value);
 		// log.debug("inserted: {}", idx);
 		stm.setInt(1, uri);
 		stm.setBoolean(2, value);
 		stm.setInt(3, idx);
 		stm.setBoolean(4, isBnode);
 
 		stm.addBatch();
 	}
 
 	private BigDecimal parseBigDecimal(String value) {
 		return new BigDecimal(value);
 	}
 
 	private Timestamp parseTimestamp(String lit) {
 		final String[] formatStrings = { "yyyy-MM-dd HH:mm:ss.SS", "yyyy-MM-dd HH:mm:ss.S", "yyyy-MM-dd HH:mm:ss", "yyyy-MM-dd",
 				"yyyy-MM-dd'T'HH:mm:ssz", };
 
 		for (String formatString : formatStrings) {
 			try {
 				long time = new SimpleDateFormat(formatString).parse(lit).getTime();
 				Timestamp ts = new Timestamp(time);
 				return ts;
 			} catch (ParseException e) {
 			}
 		}
 		return null; // the string can't be parsed to one of the datetime
 						// formats.
 	}
 
 	// Attribute datatype from TBox
 	private COL_TYPE getAttributeType(Predicate attribute) {
 		PropertySomeRestriction role = ofac.getPropertySomeRestriction(attribute, true);
 		DAGNode roleNode = dag.get(role);
 		Set<DAGNode> ancestors = roleNode.getAncestors();
 
 		for (DAGNode node : ancestors) {
 			Description desc = node.getDescription();
 			if (desc instanceof DataType) {
 				DataType datatype = (DataType) desc;
 				return datatype.getPredicate().getType(0); // TODO Put some
 															// check for
 															// multiple types
 			}
 		}
 		return COL_TYPE.LITERAL;
 	}
 
 	@Override
 	public Ontology getABoxDependencies() {
 		return aboxDependencies;
 	}
 
 	@Override
 	public void loadMetadata(Connection conn) throws SQLException {
 		log.debug("Loading semantic index metadata from the database *");
 
 		classIndexes.clear();
 		classIntervals.clear();
 		roleIndexes.clear();
 		roleIntervals.clear();
 
 		nonEmptyEntityRecord.clear();
 
 		IRIFactory f = IRIFactory.iriImplementation();
 
 		/* Fetching the index data */
 		Statement st = conn.createStatement();
 		ResultSet res = st.executeQuery("SELECT * FROM " + index_table);
 		while (res.next()) {
 			String string = res.getString(1);
 			if (string.startsWith("file:/"))
 				continue;
 			IRI iri = f.construct(string);
 			int idx = res.getInt(2);
 			int type = res.getInt(3);
 			if (type == 1) {
 				classIndexes.put(iri, idx);
 			} else if (type == 2) {
 				roleIndexes.put(iri, idx);
 			} else {
 				throw new RuntimeException("Error loading semantic index map. IRI type was " + type
 						+ ". Expected 1 for class or 2 for property.");
 			}
 		}
 		res.close();
 
 		/*
 		 * fecthing the intervals data, note that a given IRI can have one ore
 		 * more intervals (a set) hence we need to go through several rows to
 		 * collect all of them. To do this we sort the table by URI (to get all
 		 * the intervals for a given IRI in sequence), then we collect all the
 		 * intervals row by row until we change URI, at that switch we store the
 		 * interval
 		 */
 
 		res = st.executeQuery("SELECT * FROM " + interval_table + " ORDER BY URI, ENTITY_TYPE");
 
 		List<Interval> currentSet = new LinkedList<Interval>();
 		String previousIRIStr = null;
 		int previousType = 0;
 		IRI previousIRI = null;
 		while (res.next()) {
 
 			String iristr = res.getString(1);
 			if (iristr.startsWith("file:/"))
 				continue;
 
 			IRI iri = f.construct(iristr);
 			int low = res.getInt(2);
 			int high = res.getInt(3);
 			int type = res.getInt(4);
 
 			if (previousIRIStr == null) {
 				currentSet = new LinkedList<Interval>();
 				previousIRIStr = iristr;
 				previousType = type;
 				previousIRI = iri;
 			}
 
 			if ((!iristr.equals(previousIRIStr) || previousType != type)) {
 				/*
 				 * we switched URI or type, time to store the collected
 				 * intervals and clear the set
 				 */
 				if (previousType == 1) {
 					classIntervals.put(previousIRI, currentSet);
 				} else if (previousType == 2) {
 					roleIntervals.put(previousIRI, currentSet);
 				} else {
 					throw new RuntimeException("Error loading semantic index intervals. IRI type was " + previousType
 							+ ". Expected 1 for class or 2 for property.");
 				}
 
 				currentSet = new LinkedList<Interval>();
 				previousIRIStr = iristr;
 				previousType = type;
 				previousIRI = iri;
 			}
 
 			currentSet.add(new Interval(low, high));
 
 		}
 
 		if (previousType == 1) {
 			classIntervals.put(previousIRI, currentSet);
 		} else if (previousType == 2) {
 			roleIntervals.put(previousIRI, currentSet);
 		} else {
 			throw new RuntimeException("Error loading semantic index intervals. IRI type was " + previousType
 					+ ". Expected 1 for class or 2 for property.");
 		}
 
 		res.close();
 
 		/**
 		 * Restoring the emptyness index
 		 */
 		res = st.executeQuery("SELECT * FROM " + emptyness_index_table);
 		while (res.next()) {
 			int table = res.getInt(1);
 			int type1 = res.getInt(3);
 			int type2 = res.getInt(4);
 			int idx = res.getInt(2);
 			SemanticIndexRecord r = new SemanticIndexRecord(table, type1, type2, idx);
 			nonEmptyEntityRecord.add(r);
 		}
 
 		res.close();
 
 	}
 
 	@Override
 	public boolean checkMetadata(Connection conn) throws SQLException {
 		return true;
 	}
 
 	@Override
 	public Collection<OBDAMappingAxiom> getMappings() throws OBDAException {
 
 		// try {
 		// GraphGenerator.dumpISA(dag,"sidag");
 		// } catch (IOException e) {
 		// // e.printStackTrace();
 		// }
 
 		Set<DAGNode> roleNodes = new HashSet<DAGNode>();
 		Map<DAGNode, List<DAGNode>> roleInverseMaps = new HashMap<DAGNode, List<DAGNode>>();
 
 		Set<Predicate> roles = ontology.getRoles();
 		for (Predicate rolepred : roles) {
 
 			DAGNode node = dag.getRoleNode(ofac.createProperty(rolepred));
 			// We only map named roles
 			if (!(node.getDescription() instanceof Property) || ((Property) node.getDescription()).isInverse()) {
 				continue;
 			}
 			roleNodes.add(node);
 
 			List<DAGNode> roleInverseChildren = roleInverseMaps.get(node);
 			if (roleInverseChildren == null) {
 				roleInverseChildren = new LinkedList<DAGNode>();
 				roleInverseMaps.put(node, roleInverseChildren);
 			}
 
 			/*
 			 * collecting the top most inverse children, we do a bredth first
 			 * traversal, stopping a branch when we find an inverse child.
 			 * 
 			 * Collecting the top most allows us to avoid redundancy elimination
 			 */
 			Queue<DAGNode> childrenQueue = new LinkedList<DAGNode>();
 			childrenQueue.addAll(node.getChildren());
 			childrenQueue.addAll(node.getEquivalents());
 
 			while (!childrenQueue.isEmpty()) {
 				DAGNode child = childrenQueue.poll();
 				if ((child.getDescription() instanceof Property) && ((Property) child.getDescription()).isInverse()) {
 					roleInverseChildren.add(child);
 				} else {
 					childrenQueue.addAll(child.getChildren());
 				}
 			}
 
 			/* Removing redundant nodes */
 
 			HashSet<DAGNode> inverseRedundants = new HashSet<DAGNode>();
 			for (DAGNode inverseNode : roleInverseChildren) {
 				Property role = ((Property) inverseNode.getDescription());
 				for (DAGNode possibleRedundantNode : roleInverseChildren) {
 					Property possibleRedundantRole = ((Property) possibleRedundantNode.getDescription());
 					if (dag.getRoleNode(role).getDescendants().contains(possibleRedundantRole))
 						inverseRedundants.add(possibleRedundantNode);
 				}
 			}
 			roleInverseChildren.removeAll(inverseRedundants);
 
 		}
 
 		/*
 		 * Collecting relevant nodes for each class, that is, the Node itself,
 		 * and each exists R such that there is no other exists P, such that R
 		 * isa P
 		 * 
 		 * Here we cannot collect only the top most, so we do it in two passes.
 		 * First we callect all exsts R children, then we remove redundant ones.
 		 */
 
 		// TODO this part can be optimized if we know some existing dependencies
 		// (e.g., coming from given mappings)
 
 		Set<DAGNode> classNodesMaps = new HashSet<DAGNode>();
 		Map<DAGNode, Set<DAGNode>> classExistsMaps = new HashMap<DAGNode, Set<DAGNode>>();
 		for (DAGNode node : dag.getClasses()) {
 			// we only map named classes
 			if (!(node.getDescription() instanceof OClass)) {
 				continue;
 			}
 			classNodesMaps.add(node);
 
 			Set<DAGNode> existChildren = classExistsMaps.get(node);
 			if (existChildren == null) {
 				existChildren = new HashSet<DAGNode>();
 				classExistsMaps.put(node, existChildren);
 			}
 
 			/* Collecting Exists R children */
 			for (DAGNode child : node.getDescendants()) {
 				if (child.getDescription() instanceof PropertySomeRestrictionImpl) {
 					existChildren.add(child);
 				}
 			}
 
 			/*
 			 * Cleaning exists children (removing any exists R implied by the
 			 * role hierarchy )
 			 */
 			// Set<DAGNode> existChildren = classExistsMaps.get(node);
 			Set<DAGNode> redundantNodes = new HashSet<DAGNode>();
 			for (DAGNode existsnode : existChildren) {
 				/* Here we have ES */
 				PropertySomeRestriction existsDesc = (PropertySomeRestriction) existsnode.getDescription();
 				Property role = ofac.createProperty(existsDesc.getPredicate(), existsDesc.isInverse());
 				DAGNode roleNode = dag.getRoleNode(role);
 
 				for (DAGNode possiblyRedundantNode : existChildren) {
 					/* Here we have ER */
 					PropertySomeRestriction existsDesc2 = (PropertySomeRestriction) possiblyRedundantNode.getDescription();
 					Property role2 = ofac.createProperty(existsDesc2.getPredicate(), existsDesc2.isInverse());
 					DAGNode roleNode2 = dag.getRoleNode(role2);
 
 					if (roleNode.getDescendants().contains(roleNode2))
 						/*
 						 * The DAG implies that R ISA S, so we remove ER
 						 */
 						redundantNodes.add(possiblyRedundantNode);
 				}
 			}
 			existChildren.removeAll(redundantNodes);
 		}
 
 		/*
 		 * We collected all classes and properties that need mappings, and the
 		 * nodes that are relevant for each of their mappings
 		 */
 
 		/*
 		 * PART 2: Creating the mappings
 		 * 
 		 * Note, at every step we always use the pureIsa dag to get the indexes
 		 * and ranges for each class.
 		 */
 
 		// Creating the mappings for each role
 
 		Map<Predicate, List<OBDAMappingAxiom>> mappings = new HashMap<Predicate, List<OBDAMappingAxiom>>();
 
 		for (DAGNode roleNode : roleNodes) {
 
 			// Get the description of the role node, i.e., a Property object.
 			Property property = (Property) roleNode.getDescription();
 
 			// Get the property predicate
 			Predicate role = property.getPredicate();
 
 			// Get the indexed node (from the pureIsa dag)
 			// DAGNode indexedNode = pureIsa.getRoleNode(property);
 
 			// We need to make sure we make no mappings for Auxiliary roles
 			// introduced by the Ontology translation process.
 			if (role.toString().contains(OntologyImpl.AUXROLEURI)) {
 				continue;
 			}
 
 			List<OBDAMappingAxiom> currentMappings = new LinkedList<OBDAMappingAxiom>();
 			mappings.put(role, currentMappings);
 
 			COL_TYPE type = role.getType(1);
 
 			CQIE targetQuery;
 			String sourceQuery;
 			OBDAMappingAxiom basicmapping;
 
 			/***
 			 * Generating one mapping for each supported cases, i.e., the second
 			 * component is an object, or one of the supported datatypes. For
 			 * each case we will construct 1 target query, one source query and
 			 * the mapping axiom.
 			 * 
 			 * The resulting mapping will be added to the list. In this way,
 			 * each property can act as an object or data property of any type.
 			 */
 
 			targetQuery = constructTargetQuery(role, COL_TYPE.OBJECT, COL_TYPE.OBJECT);
 			sourceQuery = constructSourceQuery(role, COL_TYPE.OBJECT, COL_TYPE.OBJECT);
 
 			basicmapping = dfac.getRDBMSMappingAxiom(sourceQuery, targetQuery);
 			if (!isMappingEmpty(role.getName(), COL_TYPE.OBJECT, COL_TYPE.OBJECT, 2))
 				currentMappings.add(basicmapping);
 
 			/*
 			 * object, bnode object, object bnode, object bnode, bnode
 			 */
 
 			targetQuery = constructTargetQuery(role, COL_TYPE.OBJECT, COL_TYPE.BNODE);
 			sourceQuery = constructSourceQuery(role, COL_TYPE.OBJECT, COL_TYPE.BNODE);
 
 			basicmapping = dfac.getRDBMSMappingAxiom(sourceQuery, targetQuery);
 			if (!isMappingEmpty(role.getName(), COL_TYPE.OBJECT, COL_TYPE.BNODE, 2))
 				currentMappings.add(basicmapping);
 
 			targetQuery = constructTargetQuery(role, COL_TYPE.BNODE, COL_TYPE.OBJECT);
 			sourceQuery = constructSourceQuery(role, COL_TYPE.BNODE, COL_TYPE.OBJECT);
 
 			basicmapping = dfac.getRDBMSMappingAxiom(sourceQuery, targetQuery);
 			if (!isMappingEmpty(role.getName(), COL_TYPE.BNODE, COL_TYPE.OBJECT, 2))
 				currentMappings.add(basicmapping);
 
 			targetQuery = constructTargetQuery(role, COL_TYPE.BNODE, COL_TYPE.BNODE);
 			sourceQuery = constructSourceQuery(role, COL_TYPE.BNODE, COL_TYPE.BNODE);
 
 			basicmapping = dfac.getRDBMSMappingAxiom(sourceQuery, targetQuery);
 			if (!isMappingEmpty(role.getName(), COL_TYPE.BNODE, COL_TYPE.BNODE, 2))
 				currentMappings.add(basicmapping);
 
 			/*
 			 * object, type
 			 */
 
 			targetQuery = constructTargetQuery(role, COL_TYPE.OBJECT, COL_TYPE.LITERAL);
 			sourceQuery = constructSourceQuery(role, COL_TYPE.OBJECT, COL_TYPE.LITERAL);
 			basicmapping = dfac.getRDBMSMappingAxiom(sourceQuery, targetQuery);
 			if (!isMappingEmpty(role.getName(), COL_TYPE.OBJECT, COL_TYPE.LITERAL, 2))
 				currentMappings.add(basicmapping);
 
 			targetQuery = constructTargetQuery(role, COL_TYPE.OBJECT, COL_TYPE.LITERAL_LANG);
 			sourceQuery = constructSourceQuery(role, COL_TYPE.OBJECT, COL_TYPE.LITERAL_LANG);
 			basicmapping = dfac.getRDBMSMappingAxiom(sourceQuery, targetQuery);
 			if (!isMappingEmpty(role.getName(), COL_TYPE.OBJECT, COL_TYPE.LITERAL_LANG, 2))
 				currentMappings.add(basicmapping);
 
 			targetQuery = constructTargetQuery(role, COL_TYPE.OBJECT, COL_TYPE.BOOLEAN);
 			sourceQuery = constructSourceQuery(role, COL_TYPE.OBJECT, COL_TYPE.BOOLEAN);
 			basicmapping = dfac.getRDBMSMappingAxiom(sourceQuery, targetQuery);
 			if (!isMappingEmpty(role.getName(), COL_TYPE.OBJECT, COL_TYPE.BOOLEAN, 2))
 				currentMappings.add(basicmapping);
 
 			targetQuery = constructTargetQuery(role, COL_TYPE.OBJECT, COL_TYPE.DATETIME);
 			sourceQuery = constructSourceQuery(role, COL_TYPE.OBJECT, COL_TYPE.DATETIME);
 			basicmapping = dfac.getRDBMSMappingAxiom(sourceQuery, targetQuery);
 			if (!isMappingEmpty(role.getName(), COL_TYPE.OBJECT, COL_TYPE.DATETIME, 2))
 				currentMappings.add(basicmapping);
 
 			targetQuery = constructTargetQuery(role, COL_TYPE.OBJECT, COL_TYPE.DECIMAL);
 			sourceQuery = constructSourceQuery(role, COL_TYPE.OBJECT, COL_TYPE.DECIMAL);
 			basicmapping = dfac.getRDBMSMappingAxiom(sourceQuery, targetQuery);
 			if (!isMappingEmpty(role.getName(), COL_TYPE.OBJECT, COL_TYPE.DECIMAL, 2))
 				currentMappings.add(basicmapping);
 
 			targetQuery = constructTargetQuery(role, COL_TYPE.OBJECT, COL_TYPE.DOUBLE);
 			sourceQuery = constructSourceQuery(role, COL_TYPE.OBJECT, COL_TYPE.DOUBLE);
 			basicmapping = dfac.getRDBMSMappingAxiom(sourceQuery, targetQuery);
 			if (!isMappingEmpty(role.getName(), COL_TYPE.OBJECT, COL_TYPE.DOUBLE, 2))
 				currentMappings.add(basicmapping);
 
 			targetQuery = constructTargetQuery(role, COL_TYPE.OBJECT, COL_TYPE.INTEGER);
 			sourceQuery = constructSourceQuery(role, COL_TYPE.OBJECT, COL_TYPE.INTEGER);
 			basicmapping = dfac.getRDBMSMappingAxiom(sourceQuery, targetQuery);
 			if (!isMappingEmpty(role.getName(), COL_TYPE.OBJECT, COL_TYPE.INTEGER, 2))
 				currentMappings.add(basicmapping);
 
 			targetQuery = constructTargetQuery(role, COL_TYPE.OBJECT, COL_TYPE.STRING);
 			sourceQuery = constructSourceQuery(role, COL_TYPE.OBJECT, COL_TYPE.STRING);
 			basicmapping = dfac.getRDBMSMappingAxiom(sourceQuery, targetQuery);
 			if (!isMappingEmpty(role.getName(), COL_TYPE.OBJECT, COL_TYPE.STRING, 2))
 				currentMappings.add(basicmapping);
 
 			/*
 			 * bnode, type
 			 */
 
 			targetQuery = constructTargetQuery(role, COL_TYPE.BNODE, COL_TYPE.LITERAL);
 			sourceQuery = constructSourceQuery(role, COL_TYPE.BNODE, COL_TYPE.LITERAL);
 			basicmapping = dfac.getRDBMSMappingAxiom(sourceQuery, targetQuery);
 			if (!isMappingEmpty(role.getName(), COL_TYPE.BNODE, COL_TYPE.LITERAL, 2))
 				currentMappings.add(basicmapping);
 			;
 
 			targetQuery = constructTargetQuery(role, COL_TYPE.BNODE, COL_TYPE.LITERAL_LANG);
 			sourceQuery = constructSourceQuery(role, COL_TYPE.BNODE, COL_TYPE.LITERAL_LANG);
 			basicmapping = dfac.getRDBMSMappingAxiom(sourceQuery, targetQuery);
 			if (!isMappingEmpty(role.getName(), COL_TYPE.BNODE, COL_TYPE.LITERAL_LANG, 2))
 				currentMappings.add(basicmapping);
 			;
 
 			targetQuery = constructTargetQuery(role, COL_TYPE.BNODE, COL_TYPE.BOOLEAN);
 			sourceQuery = constructSourceQuery(role, COL_TYPE.BNODE, COL_TYPE.BOOLEAN);
 			basicmapping = dfac.getRDBMSMappingAxiom(sourceQuery, targetQuery);
 			if (!isMappingEmpty(role.getName(), COL_TYPE.BNODE, COL_TYPE.BOOLEAN, 2))
 				currentMappings.add(basicmapping);
 			;
 
 			targetQuery = constructTargetQuery(role, COL_TYPE.BNODE, COL_TYPE.DATETIME);
 			sourceQuery = constructSourceQuery(role, COL_TYPE.BNODE, COL_TYPE.DATETIME);
 			basicmapping = dfac.getRDBMSMappingAxiom(sourceQuery, targetQuery);
 			if (!isMappingEmpty(role.getName(), COL_TYPE.BNODE, COL_TYPE.DATETIME, 2))
 				currentMappings.add(basicmapping);
 			;
 
 			targetQuery = constructTargetQuery(role, COL_TYPE.BNODE, COL_TYPE.DECIMAL);
 			sourceQuery = constructSourceQuery(role, COL_TYPE.BNODE, COL_TYPE.DECIMAL);
 			basicmapping = dfac.getRDBMSMappingAxiom(sourceQuery, targetQuery);
 			if (!isMappingEmpty(role.getName(), COL_TYPE.BNODE, COL_TYPE.DECIMAL, 2))
 				currentMappings.add(basicmapping);
 			;
 
 			targetQuery = constructTargetQuery(role, COL_TYPE.BNODE, COL_TYPE.DOUBLE);
 			sourceQuery = constructSourceQuery(role, COL_TYPE.BNODE, COL_TYPE.DOUBLE);
 			basicmapping = dfac.getRDBMSMappingAxiom(sourceQuery, targetQuery);
 			if (!isMappingEmpty(role.getName(), COL_TYPE.BNODE, COL_TYPE.DOUBLE, 2))
 				currentMappings.add(basicmapping);
 			;
 
 			targetQuery = constructTargetQuery(role, COL_TYPE.BNODE, COL_TYPE.INTEGER);
 			sourceQuery = constructSourceQuery(role, COL_TYPE.BNODE, COL_TYPE.INTEGER);
 			basicmapping = dfac.getRDBMSMappingAxiom(sourceQuery, targetQuery);
 			if (!isMappingEmpty(role.getName(), COL_TYPE.BNODE, COL_TYPE.INTEGER, 2))
 				currentMappings.add(basicmapping);
 			;
 
 			targetQuery = constructTargetQuery(role, COL_TYPE.BNODE, COL_TYPE.STRING);
 			sourceQuery = constructSourceQuery(role, COL_TYPE.BNODE, COL_TYPE.STRING);
 			basicmapping = dfac.getRDBMSMappingAxiom(sourceQuery, targetQuery);
 			if (!isMappingEmpty(role.getName(), COL_TYPE.BNODE, COL_TYPE.STRING, 2))
 				currentMappings.add(basicmapping);
 			;
 
 		}
 
 		/*
 		 * Creating mappings for each concept
 		 */
 
 		for (DAGNode classNode : classNodesMaps) {
 
 			Predicate classuri = ((OClass) classNode.getDescription()).getPredicate();
 
 			List<OBDAMappingAxiom> currentMappings = new LinkedList<OBDAMappingAxiom>();
 
 			mappings.put(classuri, currentMappings);
 
 			// Mapping head
 
 			Function head = dfac.getAtom(dfac.getPredicate(ifac.construct("m"), 1), dfac.getVariable("X"));
 			Function body1 = dfac.getAtom(classuri, dfac.getFunctionalTerm(dfac.getUriTemplatePredicate(1), dfac.getVariable("X")));
 
 			Function body2 = dfac.getAtom(classuri, dfac.getFunctionalTerm(dfac.getBNodeTemplatePredicate(1), dfac.getVariable("X")));
 
 			/*
 			 * This target query is shared by all mappings for this class
 			 */
 
 			/* FOR URI */
 			CQIE targetQuery1 = dfac.getCQIE(head, body1);
 
 			/* FOR BNODE */
 			CQIE targetQuery2 = dfac.getCQIE(head, body2);
 
 			/*
 			 * First mapping: Getting the SQL for the *BASIC* mapping using
 			 * ranges
 			 */
 
 			StringBuffer sql1 = new StringBuffer();
 			sql1.append(select_mapping_class);
 			sql1.append(" WHERE ");
 			sql1.append(" ISBNODE = FALSE AND ");
 
 			/* FOR BNODE */
 
 			StringBuffer sql2 = new StringBuffer();
 			sql2.append(select_mapping_class);
 			sql2.append(" WHERE ");
 			sql2.append(" ISBNODE = TRUE AND ");
 
 			/*
 			 * Getting the indexed node (from the pureIsa dag)
 			 */
 			// DAGNode indexedNode = pureIsa.getClassNode((OClass)
 			// classNode.getDescription());
 
 			List<Interval> intervals = getIntervals(classuri.getName(), 1);
 			if (intervals == null) {
 				log.warn("Found URI with no mappings, the ontology might not match the respository. Ill URI: {}", classuri.getName());
 				continue;
 			}
 			if (intervals.size() > 1)
 				sql1.append("(");
 			appendIntervalString(intervals.get(0), sql1);
 
 			for (int intervali = 1; intervali < intervals.size(); intervali++) {
 				sql1.append(" OR ");
 				appendIntervalString(intervals.get(intervali), sql1);
 			}
 			if (intervals.size() > 1)
 				sql1.append(")");
 
 			/* FOR BNODE */
 
 			OBDAMappingAxiom basicmapping = dfac.getRDBMSMappingAxiom(sql1.toString(), targetQuery1);
 			if (!isMappingEmpty(classuri.getName(), COL_TYPE.OBJECT, COL_TYPE.OBJECT, 1))
 				currentMappings.add(basicmapping);
 			;
 
 			if (intervals.size() > 1)
 				sql2.append("(");
 			appendIntervalString(intervals.get(0), sql2);
 
 			for (int intervali = 1; intervali < intervals.size(); intervali++) {
 				sql2.append(" OR ");
 				appendIntervalString(intervals.get(intervali), sql2);
 			}
 			if (intervals.size() > 1)
 				sql2.append(")");
 
 			basicmapping = dfac.getRDBMSMappingAxiom(sql2.toString(), targetQuery2);
 			if (!isMappingEmpty(classuri.getName(), COL_TYPE.BNODE, COL_TYPE.OBJECT, 1))
 				currentMappings.add(basicmapping);
 			;
 
 		}
 
 		/*
 		 * PART 4: Optimizing.
 		 */
 
 		// Merging multiple mappings into 1 with UNION ALL to minimize the
 		// number of the mappings.
 
 		if (mergeUniions) {
 			for (Predicate predicate : mappings.keySet()) {
 
 				List<OBDAMappingAxiom> currentMappings = mappings.get(predicate);
 
 				/* Getting the current head */
 				CQIE targetQuery = (CQIE) currentMappings.get(0).getTargetQuery();
 
 				/* Computing the merged SQL */
 				StringBuffer newSQL = new StringBuffer();
 				newSQL.append(((OBDASQLQuery) currentMappings.get(0).getSourceQuery()).toString());
 				for (int mapi = 1; mapi < currentMappings.size(); mapi++) {
 					newSQL.append(" UNION ALL ");
 					newSQL.append(((OBDASQLQuery) currentMappings.get(mapi).getSourceQuery()).toString());
 				}
 
 				/* Replacing the old mappings */
 				OBDAMappingAxiom mergedMapping = dfac.getRDBMSMappingAxiom(newSQL.toString(), targetQuery);
 				currentMappings.clear();
 				currentMappings.add(mergedMapping);
 			}
 		}
 
 		/*
 		 * Collecting the result
 		 */
 		Collection<OBDAMappingAxiom> result = new LinkedList<OBDAMappingAxiom>();
 		for (Predicate predicate : mappings.keySet()) {
 			log.debug("Predicate: {} Mappings: {}", predicate, mappings.get(predicate).size());
 			result.addAll(mappings.get(predicate));
 		}
 		log.debug("Total: {} mappings", result.size());
 		return result;
 	}
 
 	/***
 	 * We use 1 for classes 2 for proeprties. Tells you if there has been
 	 * inserts that can make this mapping non empty.
 	 * 
 	 * @param predicate
 	 * @param type1
 	 * @param type2
 	 * @param classPredicate
 	 * @return
 	 */
 	public boolean isMappingEmpty(IRI iri, COL_TYPE type1, COL_TYPE type2, int classPredicate) {
 
 		OBJType t1 = null;
 		OBJType t2 = OBJType.URI;
 
 		if (type1 == COL_TYPE.OBJECT) {
 			t1 = OBJType.URI;
 		} else {
 			t1 = OBJType.BNode;
 		}
 
 		SITable table = null;
 		if (classPredicate == 1) {
 			table = SITable.CLASS;
 		} else if (type2 == COL_TYPE.OBJECT) {
 			table = SITable.OPROP;
 		} else if (type2 == COL_TYPE.BNODE) {
 			table = SITable.OPROP;
 			t2 = OBJType.BNode;
 		} else if (type2 == COL_TYPE.BOOLEAN)
 			table = SITable.DPROPBool;
 		else if (type2 == COL_TYPE.DATETIME)
 			table = SITable.DPROPDate;
 		else if (type2 == COL_TYPE.DECIMAL)
 			table = SITable.DPROPDeci;
 		else if (type2 == COL_TYPE.DOUBLE)
 			table = SITable.DPROPDoub;
 		else if (type2 == COL_TYPE.INTEGER)
 			table = SITable.DPROPInte;
 		else if (type2 == COL_TYPE.LITERAL || type2 == COL_TYPE.LITERAL_LANG)
 			table = SITable.DPROPLite;
 		else if (type2 == COL_TYPE.STRING)
 			table = SITable.DPROPStri;
 
 		boolean empty = true;
 		List<Interval> intervals = getIntervals(iri, classPredicate);
 
 		for (Interval interval : intervals) {
 			for (int i = interval.getStart(); i <= interval.getEnd(); i++) {
 
 				SemanticIndexRecord record = new SemanticIndexRecord(table, t1, t2, i);
 				empty = empty && !nonEmptyEntityRecord.contains(record);
 				if (!empty)
 					break;
 			}
 			if (!empty)
 				break;
 		}
 
 		return empty;
 	}
 
 	private CQIE constructTargetQuery(Predicate predicate, COL_TYPE type1, COL_TYPE type2) {
 		// Initialize the predicate and term objects.
 		Predicate headPredicate, bodyPredicate = null;
 		List<NewLiteral> headTerms = new ArrayList<NewLiteral>();
 		List<NewLiteral> bodyTerms = new ArrayList<NewLiteral>();
 
 		List<Function> bodyAtoms = new LinkedList<Function>();
 
 		headPredicate = dfac.getPredicate(ifac.construct("m"), 2, new COL_TYPE[] { COL_TYPE.STRING, COL_TYPE.OBJECT });
 		headTerms.add(dfac.getVariable("X"));
 		headTerms.add(dfac.getVariable("Y"));
 
 		bodyPredicate = predicate; // the body
 
 		Function subjectTerm;
 		if (type1 == COL_TYPE.OBJECT) {
 
 			subjectTerm = dfac.getFunctionalTerm(dfac.getUriTemplatePredicate(1), dfac.getVariable("X"));
 
 		} else if (type1 == COL_TYPE.BNODE) {
 
 			subjectTerm = dfac.getFunctionalTerm(dfac.getBNodeTemplatePredicate(1), dfac.getVariable("X"));
 
 		} else {
 			throw new RuntimeException("Unsupported object type: " + type1);
 		}
 		bodyTerms.add(subjectTerm);
 
 		Function objectTerm;
 		if (type2 == COL_TYPE.BNODE) {
 
 			objectTerm = dfac.getFunctionalTerm(dfac.getBNodeTemplatePredicate(1), dfac.getVariable("Y"));
 
 		} else if (type2 == COL_TYPE.OBJECT) {
 
 			objectTerm = dfac.getFunctionalTerm(dfac.getUriTemplatePredicate(1), dfac.getVariable("Y"));
 
 		} else if (type2 == COL_TYPE.LITERAL) {
 
 			objectTerm = dfac.getFunctionalTerm(dfac.getDataTypePredicateLiteral(), dfac.getVariable("Y"));
 
 		} else if (type2 == COL_TYPE.LITERAL_LANG) {
 
 			objectTerm = dfac.getFunctionalTerm(dfac.getDataTypePredicateLiteral(), dfac.getVariable("Y"), dfac.getVariable("Z"));
 
 		} else if (type2 == COL_TYPE.BOOLEAN) {
 
 			objectTerm = dfac.getFunctionalTerm(dfac.getDataTypePredicateBoolean(), dfac.getVariable("Y"));
 
 		} else if (type2 == COL_TYPE.DATETIME) {
 
 			objectTerm = dfac.getFunctionalTerm(dfac.getDataTypePredicateDateTime(), dfac.getVariable("Y"));
 			bodyTerms.add(objectTerm);
 
 		} else if (type2 == COL_TYPE.DECIMAL) {
 
 			objectTerm = dfac.getFunctionalTerm(dfac.getDataTypePredicateDecimal(), dfac.getVariable("Y"));
 
 		} else if (type2 == COL_TYPE.DOUBLE) {
 
 			objectTerm = dfac.getFunctionalTerm(dfac.getDataTypePredicateDouble(), dfac.getVariable("Y"));
 
 		} else if (type2 == COL_TYPE.INTEGER) {
 
 			objectTerm = dfac.getFunctionalTerm(dfac.getDataTypePredicateInteger(), dfac.getVariable("Y"));
 
 		} else if (type2 == COL_TYPE.STRING) {
 
 			objectTerm = dfac.getFunctionalTerm(dfac.getDataTypePredicateString(), dfac.getVariable("Y"));
 
 		} else {
 			throw new RuntimeException("Unsuported type: " + type2);
 		}
 		bodyTerms.add(objectTerm);
 
 		Function head = dfac.getAtom(headPredicate, headTerms);
 		Function body = dfac.getAtom(bodyPredicate, bodyTerms);
 		bodyAtoms.add(0, body);
 		return dfac.getCQIE(head, bodyAtoms);
 	}
 
 	private String constructSourceQuery(Predicate predicate, COL_TYPE type1, COL_TYPE type2) throws OBDAException {
 		StringBuffer sql = new StringBuffer();
 		switch (type2) {
 		case OBJECT:
 			sql.append(select_mapping_role);
 			break;
 		case BNODE:
 			sql.append(select_mapping_role);
 			break;
 		case LITERAL:
 			sql.append(select_mapping_attribute_literal);
 			break;
 		case LITERAL_LANG:
 			sql.append(select_mapping_attribute_literal);
 			break;
 		case STRING:
 			sql.append(select_mapping_attribute_string);
 			break;
 		case INTEGER:
 			sql.append(select_mapping_attribute_integer);
 			break;
 		case DECIMAL:
 			sql.append(select_mapping_attribute_decimal);
 			break;
 		case DOUBLE:
 			sql.append(select_mapping_attribute_double);
 			break;
 		case DATETIME:
 			sql.append(select_mapping_attribute_datetime);
 			break;
 		case BOOLEAN:
 			sql.append(select_mapping_attribute_boolean);
 			break;
 		}
 
 		sql.append(" WHERE ");
 
 		/*
 		 * If the mapping is for something of type Literal we need to add IS
 		 * NULL or IS NOT NULL to the language column. IS NOT NULL might be
 		 * redundant since we have another stage in Quest where we add IS NOT
 		 * NULL for every variable in the head of a mapping.
 		 */
 
 		if (type1 == COL_TYPE.BNODE) {
 			sql.append("ISBNODE = TRUE AND ");
 		} else if (type1 == COL_TYPE.OBJECT) {
 			sql.append("ISBNODE = FALSE AND ");
 		}
 
 		if (type2 == COL_TYPE.BNODE) {
 			sql.append("ISBNODE2 = TRUE AND ");
 		} else if (type2 == COL_TYPE.OBJECT) {
 			sql.append("ISBNODE2 = FALSE AND ");
 		} else if (type2 == COL_TYPE.LITERAL) {
 			sql.append("LANG IS NULL AND ");
 		} else if (type2 == COL_TYPE.LITERAL_LANG) {
 			sql.append("LANG IS NOT NULL AND ");
 		}
 
 		/*
 		 * Generating the interval conditions for semantic index
 		 */
 
 		List<Interval> intervals = getIntervals(predicate.getName(), 2);
 		if (intervals == null)
 			throw new OBDAException("Could not create mapping for predicate: " + predicate.getName()
 					+ ". Couldn not find semantic index intervals for the predicate.");
 		if (intervals.size() > 1)
 			sql.append("(");
 
 		appendIntervalString(intervals.get(0), sql);
 		for (int intervali = 1; intervali < intervals.size(); intervali++) {
 			sql.append(" OR ");
 			appendIntervalString(intervals.get(intervali), sql);
 		}
 		if (intervals.size() > 1)
 			sql.append(")");
 
 		return sql.toString();
 	}
 
 	// /***
 	// * Constructs the mappings for all roles (or object properties) in the DAG
 	// * node list. The string buffer stores the mapping string, if any. The
 	// * method returns true if it finds at least one role node.
 	// *
 	// * @param nodeList
 	// * The list of existential class nodes.
 	// * @param buffer
 	// * The string buffer to stores the mapping string
 	// * @return Returns true if the method finds at least one role node, or
 	// false
 	// * otherwise.
 	// */
 	// private boolean createMappingForRole(Set<DAGNode> nodeList,
 	// StringBuffer buffer) {
 	//
 	// boolean hasRoleNode = false; // A flag if there is at least one role
 	//
 	// buffer.append(select_mapping_class_role_left);
 	// buffer.append(" WHERE ");
 	// buffer.append(" ISBNODE = FALSE AND (");
 	//
 	// boolean multipleIntervals = false; // A flag to tell there are more than
 	// // one SI intervals.
 	// for (DAGNode node : nodeList) {
 	// PropertySomeRestriction property = (PropertySomeRestriction) node
 	// .getDescription();
 	// boolean isObjectProperty = property.getPredicate().getType(1) ==
 	// COL_TYPE.OBJECT;
 	// if (isObjectProperty) {
 	// if (!property.isInverse()) {
 	//
 	// Property role = ofac.createProperty(
 	// property.getPredicate(), false);
 	// DAGNode indexedNode = pureIsa.getRoleNode(role);
 	// if (indexedNode != null) {
 	// hasRoleNode = true;
 	// List<Interval> intervals = indexedNode.getRange()
 	// .getIntervals();
 	// for (int i = 0; i < intervals.size(); i++) {
 	// if (multipleIntervals) {
 	// buffer.append(" OR ");
 	// }
 	// appendIntervalString(intervals.get(i), buffer);
 	// multipleIntervals = true;
 	// }
 	// }
 	// }
 	// }
 	// }
 	// buffer.append(")");
 	// return hasRoleNode;
 	// }
 
 	// /**
 	// * Constructs the mappings for all inverse roles (or inverse object
 	// * properties) in the DAG node list. The string buffer stores the mapping
 	// * string, if any. The method returns true if it finds at least one
 	// inverse
 	// * role node.
 	// *
 	// * @param nodeList
 	// * The list of existential class nodes.
 	// * @param buffer
 	// * The string buffer to stores the mapping string
 	// * @return Returns true if the method finds at least one inverse role
 	// node,
 	// * or false otherwise.
 	// */
 	// private boolean createMappingForInverseRole(Set<DAGNode> nodeList,
 	// StringBuffer buffer) {
 	//
 	// boolean hasInverseRoleNode = false; // A flag if there is at least one
 	// // inverse role.
 	//
 	// buffer.append(select_mapping_class_role_right);
 	// buffer.append(" WHERE ");
 	// buffer.append(" ISBNODE2 = FALSE AND (");
 	//
 	// boolean multipleIntervals = false; // A flag to tell there are more than
 	// // one SI intervals.
 	// for (DAGNode node : nodeList) {
 	// PropertySomeRestriction property = (PropertySomeRestriction) node
 	// .getDescription();
 	// boolean isObjectProperty = property.getPredicate().getType(1) ==
 	// COL_TYPE.OBJECT;
 	// if (isObjectProperty) {
 	// if (property.isInverse()) {
 	// Property role = ofac.createProperty(
 	// property.getPredicate(), false);
 	// DAGNode indexedNode = pureIsa.getRoleNode(role);
 	// if (indexedNode != null) {
 	// hasInverseRoleNode = true;
 	// List<Interval> intervals = indexedNode.getRange()
 	// .getIntervals();
 	// for (int i = 0; i < intervals.size(); i++) {
 	// if (multipleIntervals) {
 	// buffer.append(" OR ");
 	// }
 	// appendIntervalString(intervals.get(i), buffer);
 	// multipleIntervals = true;
 	// }
 	// }
 	// }
 	// }
 	// }
 	// buffer.append(")");
 	//
 	// return hasInverseRoleNode;
 	// }
 
 	// /**
 	// * Constructs the mappings for all data properties with range rdfs:Literal
 	// * in the DAG node list. The string buffer stores the mapping string, if
 	// * any. The method returns true if it finds at least one of the node.
 	// *
 	// * @param nodeList
 	// * The list of existential class nodes.
 	// * @param buffer
 	// * The string buffer to stores the mapping string
 	// * @return Returns true if the method finds at least one data property
 	// node
 	// * with rdfs:Literal as the range, or false otherwise.
 	// */
 	// private boolean createMappingForLiteralDataType(Set<DAGNode> nodeList,
 	// StringBuffer buffer) {
 	//
 	// boolean hasLiteralNode = false; // A flag if there is at least one DP
 	// // with range rdfs:Literal
 	//
 	// buffer.append(select_mapping_class_attribute_literal_left);
 	// buffer.append(" WHERE ");
 	//
 	// boolean multipleIntervals = false; // A flag to tell there are more than
 	// // one SI interval.
 	// for (DAGNode node : nodeList) {
 	// Predicate property = ((PropertySomeRestriction) node
 	// .getDescription()).getPredicate();
 	// boolean isObjectProperty = (property.getType(1) == COL_TYPE.OBJECT);
 	// if (isObjectProperty) {
 	// continue;
 	// }
 	//
 	// PropertySomeRestriction existsDesc = (PropertySomeRestriction) node
 	// .getDescription();
 	// Property role = ofac.createProperty(existsDesc.getPredicate(),
 	// false);
 	// DAGNode indexedNode = pureIsa.getRoleNode(role); // Get the
 	// // indexed
 	// // node.
 	// if (indexedNode == null) {
 	// continue;
 	// }
 	// hasLiteralNode = true;
 	// List<Interval> intervals = indexedNode.getRange().getIntervals();
 	// for (int i = 0; i < intervals.size(); i++) {
 	// if (multipleIntervals) {
 	// buffer.append(" OR ");
 	// }
 	// appendIntervalString(intervals.get(i), buffer);
 	// multipleIntervals = true;
 	// }
 	// }
 	//
 	// return hasLiteralNode;
 	// }
 
 	// /**
 	// * Constructs the mappings for all data properties with range xsd:string
 	// in
 	// * the DAG node list. The string buffer stores the mapping string, if any.
 	// * The method returns true if it finds at least one of the node.
 	// *
 	// * @param nodeList
 	// * The list of existential class nodes.
 	// * @param buffer
 	// * The string buffer to stores the mapping string
 	// * @return Returns true if the method finds at least one data property
 	// node
 	// * with xsd:string as the range, or false otherwise.
 	// */
 	// private boolean createMappingForStringDataType(Set<DAGNode> nodeList,
 	// StringBuffer buffer) {
 	//
 	// boolean hasStringNode = false; // A flag if there is at least one DP
 	// // with range xsd:string
 	//
 	// buffer.append(select_mapping_class_attribute_string_left);
 	// buffer.append(" WHERE ");
 	//
 	// boolean multipleIntervals = false; // A flag to tell there are more than
 	// // one SI interval.
 	// for (DAGNode node : nodeList) {
 	// Predicate property = ((PropertySomeRestriction) node
 	// .getDescription()).getPredicate();
 	// boolean isObjectProperty = (property.getType(1) == COL_TYPE.OBJECT);
 	// if (isObjectProperty) {
 	// continue;
 	// }
 	// PropertySomeRestriction existsDesc = (PropertySomeRestriction) node
 	// .getDescription();
 	// Property role = ofac.createProperty(existsDesc.getPredicate(),
 	// false);
 	// DAGNode indexedNode = pureIsa.getRoleNode(role); // Get the
 	// // indexed
 	// // node.
 	// if (indexedNode == null) {
 	// continue;
 	// }
 	// hasStringNode = true;
 	// List<Interval> intervals = indexedNode.getRange().getIntervals();
 	// for (int i = 0; i < intervals.size(); i++) {
 	// if (multipleIntervals) {
 	// buffer.append(" OR ");
 	// }
 	// appendIntervalString(intervals.get(i), buffer);
 	// multipleIntervals = true;
 	// }
 	// }
 	//
 	// return hasStringNode;
 	// }
 
 	// /**
 	// * Constructs the mappings for all data properties with range xsd:int in
 	// the
 	// * DAG node list. The string buffer stores the mapping string, if any. The
 	// * method returns true if it finds at least one of the node.
 	// *
 	// * @param nodeList
 	// * The list of existential class nodes.
 	// * @param buffer
 	// * The string buffer to stores the mapping string
 	// * @return Returns true if the method finds at least one data property
 	// node
 	// * with xsd:int as the range, or false otherwise.
 	// */
 	// private boolean createMappingForIntegerDataType(Set<DAGNode> nodeList,
 	// StringBuffer buffer) {
 	//
 	// boolean hasIntegerNode = false; // A flag if there is at least one DP
 	// // with range xsd:int
 	//
 	// buffer.append(select_mapping_class_attribute_integer_left);
 	// buffer.append(" WHERE ");
 	//
 	// boolean multipleIntervals = false; // A flag to tell there are more than
 	// // one SI interval.
 	// for (DAGNode node : nodeList) {
 	// Predicate property = ((PropertySomeRestriction) node
 	// .getDescription()).getPredicate();
 	// boolean isObjectProperty = (property.getType(1) == COL_TYPE.OBJECT);
 	// if (isObjectProperty) {
 	// continue;
 	// }
 	//
 	// PropertySomeRestriction existsDesc = (PropertySomeRestriction) node
 	// .getDescription();
 	// Property role = ofac.createProperty(existsDesc.getPredicate(),
 	// false);
 	// DAGNode indexedNode = pureIsa.getRoleNode(role); // Get the
 	// // indexed
 	// // node.
 	// if (indexedNode == null) {
 	// continue;
 	// }
 	// hasIntegerNode = true;
 	// List<Interval> intervals = indexedNode.getRange().getIntervals();
 	// for (int i = 0; i < intervals.size(); i++) {
 	// if (multipleIntervals) {
 	// buffer.append(" OR ");
 	// }
 	// appendIntervalString(intervals.get(i), buffer);
 	// multipleIntervals = true;
 	// }
 	// }
 	//
 	// return hasIntegerNode;
 	// }
 
 	// /**
 	// * Constructs the mappings for all data properties with range xsd:decimal
 	// in
 	// * the DAG node list. The string buffer stores the mapping string, if any.
 	// * The method returns true if it finds at least one of the node.
 	// *
 	// * @param nodeList
 	// * The list of existential class nodes.
 	// * @param buffer
 	// * The string buffer to stores the mapping string
 	// * @return Returns true if the method finds at least one data property
 	// node
 	// * with xsd:decimal as the range, or false otherwise.
 	// */
 	// private boolean createMappingForDecimalDataType(Set<DAGNode> nodeList,
 	// StringBuffer buffer) {
 	//
 	// boolean hasDecimalNode = false; // A flag if there is at least one DP
 	// // with range xsd:decimal
 	//
 	// buffer.append(select_mapping_class_attribute_decimal_left);
 	// buffer.append(" WHERE ");
 	//
 	// boolean multipleIntervals = false; // A flag to tell there are more than
 	// // one SI interval.
 	// for (DAGNode node : nodeList) {
 	// Predicate property = ((PropertySomeRestriction) node
 	// .getDescription()).getPredicate();
 	// boolean isObjectProperty = (property.getType(1) == COL_TYPE.OBJECT);
 	// if (isObjectProperty) {
 	// continue;
 	// }
 	//
 	// PropertySomeRestriction existsDesc = (PropertySomeRestriction) node
 	// .getDescription();
 	// Property role = ofac.createProperty(existsDesc.getPredicate(),
 	// false);
 	// DAGNode indexedNode = pureIsa.getRoleNode(role); // Get the
 	// // indexed
 	// // node.
 	// if (indexedNode == null) {
 	// continue;
 	// }
 	// hasDecimalNode = true;
 	// List<Interval> intervals = indexedNode.getRange().getIntervals();
 	// for (int i = 0; i < intervals.size(); i++) {
 	// if (multipleIntervals) {
 	// buffer.append(" OR ");
 	// }
 	// appendIntervalString(intervals.get(i), buffer);
 	// multipleIntervals = true;
 	// }
 	// }
 	//
 	// return hasDecimalNode;
 	// }
 
 	// /**
 	// * Constructs the mappings for all data properties with range xsd:double
 	// in
 	// * the DAG node list. The string buffer stores the mapping string, if any.
 	// * The method returns true if it finds at least one of the node.
 	// *
 	// * @param nodeList
 	// * The list of existential class nodes.
 	// * @param buffer
 	// * The string buffer to stores the mapping string
 	// * @return Returns true if the method finds at least one data property
 	// node
 	// * with xsd:double as the range, or false otherwise.
 	// */
 	// private boolean createMappingForDoubleDataType(Set<DAGNode> nodeList,
 	// StringBuffer buffer) {
 	//
 	// boolean hasDoubleNode = false; // A flag if there is at least one DP
 	// // with range xsd:double
 	//
 	// buffer.append(select_mapping_class_attribute_double_left);
 	// buffer.append(" WHERE ");
 	//
 	// boolean multipleIntervals = false; // A flag to tell there are more than
 	// // one SI interval.
 	// for (DAGNode node : nodeList) {
 	// Predicate property = ((PropertySomeRestriction) node
 	// .getDescription()).getPredicate();
 	// boolean isObjectProperty = (property.getType(1) == COL_TYPE.OBJECT);
 	// if (isObjectProperty) {
 	// continue;
 	// }
 	// PropertySomeRestriction existsDesc = (PropertySomeRestriction) node
 	// .getDescription();
 	// Property role = ofac.createProperty(existsDesc.getPredicate(),
 	// false);
 	// DAGNode indexedNode = pureIsa.getRoleNode(role); // Get the
 	// // indexed
 	// // node.
 	// if (indexedNode == null) {
 	// continue;
 	// }
 	// hasDoubleNode = true;
 	// List<Interval> intervals = indexedNode.getRange().getIntervals();
 	// for (int i = 0; i < intervals.size(); i++) {
 	// if (multipleIntervals) {
 	// buffer.append(" OR ");
 	// }
 	// appendIntervalString(intervals.get(i), buffer);
 	// multipleIntervals = true;
 	// }
 	// }
 	//
 	// return hasDoubleNode;
 	// }
 
 	// /**
 	// * Constructs the mappings for all data properties with range xsd:date in
 	// * the DAG node list. The string buffer stores the mapping string, if any.
 	// * The method returns true if it finds at least one of the node.
 	// *
 	// * @param nodeList
 	// * The list of existential class nodes.
 	// * @param buffer
 	// * The string buffer to stores the mapping string
 	// * @return Returns true if the method finds at least one data property
 	// node
 	// * with xsd:date as the range, or false otherwise.
 	// */
 	// private boolean createMappingForDateDataType(Set<DAGNode> nodeList,
 	// StringBuffer buffer) {
 	//
 	// boolean hasDateNode = false; // A flag if there is at least one DP with
 	// // range xsd:date
 	//
 	// buffer.append(select_mapping_class_attribute_datetime_left);
 	// buffer.append(" WHERE ");
 	//
 	// boolean multipleIntervals = false; // A flag to tell there are more than
 	// // one SI interval.
 	// for (DAGNode node : nodeList) {
 	// Predicate property = ((PropertySomeRestriction) node
 	// .getDescription()).getPredicate();
 	// boolean isObjectProperty = (property.getType(1) == COL_TYPE.OBJECT);
 	// if (isObjectProperty) {
 	// continue;
 	// }
 	// PropertySomeRestriction existsDesc = (PropertySomeRestriction) node
 	// .getDescription();
 	// Property role = ofac.createProperty(existsDesc.getPredicate(),
 	// false);
 	// DAGNode indexedNode = pureIsa.getRoleNode(role); // Get the
 	// // indexed
 	// // node.
 	// if (indexedNode == null) {
 	// continue;
 	// }
 	// hasDateNode = true;
 	// List<Interval> intervals = indexedNode.getRange().getIntervals();
 	// for (int i = 0; i < intervals.size(); i++) {
 	// if (multipleIntervals) {
 	// buffer.append(" OR ");
 	// }
 	// appendIntervalString(intervals.get(i), buffer);
 	// multipleIntervals = true;
 	// }
 	// }
 	//
 	// return hasDateNode;
 	// }
 
 	// /**
 	// * Constructs the mappings for all data properties with range xsd:boolean
 	// in
 	// * the DAG node list. The string buffer stores the mapping string, if any.
 	// * The method returns true if it finds at least one of the node.
 	// *
 	// * @param nodeList
 	// * The list of existential class nodes.
 	// * @param buffer
 	// * The string buffer to stores the mapping string
 	// * @return Returns true if the method finds at least one data property
 	// node
 	// * with xsd:boolean as the range, or false otherwise.
 	// */
 	// private boolean createMappingForBooleanDataType(Set<DAGNode> nodeList,
 	// StringBuffer buffer) {
 	//
 	// boolean hasBooleanNode = false; // A flag if there is at least one DP
 	// // with range xsd:boolean
 	//
 	// buffer.append(select_mapping_class_attribute_boolean_left);
 	// buffer.append(" WHERE ");
 	//
 	// boolean multipleIntervals = false; // A flag to tell there are more than
 	// // one SI interval.
 	// for (DAGNode node : nodeList) {
 	// Predicate property = ((PropertySomeRestriction) node
 	// .getDescription()).getPredicate();
 	// boolean isObjectProperty = (property.getType(1) == COL_TYPE.OBJECT);
 	// if (isObjectProperty) {
 	// continue;
 	// }
 	// PropertySomeRestriction existsDesc = (PropertySomeRestriction) node
 	// .getDescription();
 	// Property role = ofac.createProperty(existsDesc.getPredicate(),
 	// false);
 	// DAGNode indexedNode = pureIsa.getRoleNode(role); // Get the
 	// // indexed
 	// // node.
 	// if (indexedNode == null) {
 	// continue;
 	// }
 	// hasBooleanNode = true;
 	// List<Interval> intervals = indexedNode.getRange().getIntervals();
 	// for (int i = 0; i < intervals.size(); i++) {
 	// if (multipleIntervals) {
 	// buffer.append(" OR ");
 	// }
 	// appendIntervalString(intervals.get(i), buffer);
 	// multipleIntervals = true;
 	// }
 	// }
 	//
 	// return hasBooleanNode;
 	// }
 
 	private void appendIntervalString(Interval interval, StringBuffer out) {
 		if (interval.getStart() == interval.getEnd()) {
 			out.append(String.format(whereSingleCondition, interval.getStart()));
 		} else {
 			out.append(String.format(whereIntervalCondition, interval.getStart(), interval.getEnd()));
 		}
 	}
 
 	@Override
 	public void collectStatistics(Connection conn) throws SQLException {
 
 		Statement st = conn.createStatement();
 
 		st.addBatch(analyze);
 
 		st.executeBatch();
 		st.close();
 
 	}
 
 	@Override
 	public void getDropDDL(OutputStream out) throws IOException {
 		// TODO Auto-generated method stub
 
 	}
 
 	@Override
 	public void getMetadataSQLInserts(OutputStream outstream) throws IOException {
 
 		// BufferedWriter out = new BufferedWriter(new
 		// OutputStreamWriter(outstream));
 		//
 		// String insert_query = this.insert_idx_query.replace("?", "%s");
 		//
 		// index
 		//
 		// for (DAGNode node : dag.getClasses()) {
 		//
 		// ClassDescription description = (ClassDescription)
 		// node.getDescription();
 		//
 		//
 		// DAGNode node2 = pureIsa.getClassNode(description);
 		// if (node2 != null) {
 		// node = node2;
 		// }
 		//
 		// String uri = description.toString();
 		//
 		// for (Interval it : node.getRange().getIntervals()) {
 		//
 		// out.append(String.format(insert_query, getQuotedString(uri),
 		// node.getIndex(), it.getStart(), it.getEnd(), CLASS_TYPE));
 		// out.append(";\n");
 		// }
 		// }
 		//
 		// for (DAGNode node : dag.getRoles()) {
 		// Property description = (Property) node.getDescription();
 		//
 		// /*
 		// * we always prefer the pureISA node since it can have extra data
 		// * (indexes)
 		// */
 		// DAGNode node2 = pureIsa.getRoleNode(description);
 		// if (node2 != null) {
 		// node = node2;
 		// }
 		//
 		// String uri = description.toString();
 		//
 		// for (Interval it : node.getRange().getIntervals()) {
 		// out.append(String.format(insert_query, getQuotedString(uri),
 		// node.getIndex(), it.getStart(), it.getEnd(), ROLE_TYPE));
 		// out.append(";\n");
 		// }
 		// }
 		//
 		// out.flush();
 	}
 
 	/***
 	 * Inserts the metadata about semantic indexes and intervals into the
 	 * database. The metadata is later used to reconstruct a semantic index
 	 * repository.
 	 */
 	@Override
 	public void insertMetadata(Connection conn) throws SQLException {
 
 		log.debug("Inserting semantic index metadata. This will allow the repository to reconstruct itself afterwards.");
 
 		boolean commitval = conn.getAutoCommit();
 
 		conn.setAutoCommit(false);
 
 		PreparedStatement stm = conn.prepareStatement(insert_idx_query);
 		Statement st = conn.createStatement();
 
 		try {
 
 			/* dropping previous metadata */
 
 			st.executeUpdate("DELETE FROM " + index_table);
 			st.executeUpdate("DELETE FROM " + interval_table);
 			st.executeUpdate("DELETE FROM " + emptyness_index_table);
 
 			/* inserting index data for classes and roeles */
 
 			for (IRI concept : classIndexes.keySet()) {
 				stm.setString(1, concept.toString());
 				stm.setInt(2, classIndexes.get(concept));
 				stm.setInt(3, CLASS_TYPE);
 				stm.addBatch();
 			}
 			stm.executeBatch();
 
 			for (IRI role : roleIndexes.keySet()) {
 				stm.setString(1, role.toString());
 				stm.setInt(2, roleIndexes.get(role));
 				stm.setInt(3, ROLE_TYPE);
 				stm.addBatch();
 			}
 			stm.executeBatch();
 			stm.clearBatch();
 			stm.close();
 
 			/*
 			 * Inserting interval metadata
 			 */
 
 			stm = conn.prepareStatement(insert_interval_query);
 			for (IRI concept : classIntervals.keySet()) {
 				for (Interval it : classIntervals.get(concept)) {
 					stm.setString(1, concept.toString());
 					stm.setInt(2, it.getStart());
 					stm.setInt(3, it.getEnd());
 					stm.setInt(4, CLASS_TYPE);
 					stm.addBatch();
 				}
 			}
 			stm.executeBatch();
 
 			for (IRI role : roleIntervals.keySet()) {
 				for (Interval it : roleIntervals.get(role)) {
 					stm.setString(1, role.toString());
 					stm.setInt(2, it.getStart());
 					stm.setInt(3, it.getEnd());
 					stm.setInt(4, ROLE_TYPE);
 					stm.addBatch();
 				}
 			}
 			stm.executeBatch();
 
 			/* Inserting emptyness index metadata */
 
 			stm = conn.prepareStatement("INSERT INTO " + emptyness_index_table + " (TABLEID, IDX, TYPE1, TYPE2) VALUES (?, ?, ?, ?)");
 			for (SemanticIndexRecord record : nonEmptyEntityRecord) {
 				stm.setInt(1, record.table.ordinal());
 				stm.setInt(2, record.idx);
 				stm.setInt(3, record.type1.ordinal());
 				stm.setInt(4, record.type2.ordinal());
 				stm.addBatch();
 			}
 			stm.executeBatch();
 
 			stm.close();
 
 			conn.commit();
 
 		} catch (SQLException e) {
 			try {
 				st.close();
 			} catch (Exception e2) {
 
 			}
 
 			try {
 				stm.close();
 			} catch (Exception e2) {
 
 			}
 			// If there is a big error, restore everything as it was
 			try {
 				conn.rollback();
 			} catch (Exception e2) {
 
 			}
 			conn.setAutoCommit(commitval);
 			throw e;
 		}
 
 	}
 
 	@Override
 	public void setVocabulary(Set<Predicate> vocabulary) {
 		// TODO
 
 		/* This method should initialize the vocabulary of the DAG */
 
 	}
 
 	/*
 	 * Utilities
 	 */
 
 	private String getQuotedString(String str) {
 		StringBuffer bf = new StringBuffer();
 		bf.append("'");
 		bf.append(str);
 		bf.append("'");
 		return bf.toString();
 	}
 
 	private String getQuotedString(URI str) {
 		StringBuffer bf = new StringBuffer();
 		bf.append("'");
 		bf.append(str.toString());
 		bf.append("'");
 		return bf.toString();
 	}
 
 	@Override
 	public void dropIndexes(Connection conn) throws SQLException {
 		log.debug("Droping indexes");
 
 		Statement st = conn.createStatement();
 
 		st.addBatch(dropindexclass1);
 		st.addBatch(dropindexclass2);
 		// st.addBatch(dropindexclass3);
 		// st.addBatch(dropindexclass4);
 		st.addBatch(dropindexrole1);
 		st.addBatch(dropindexrole2);
 		st.addBatch(dropindexrole3);
 
 		st.addBatch(dropindexattribute_literal1);
 		st.addBatch(dropindexattribute_string1);
 		st.addBatch(dropindexattribute_integer1);
 		st.addBatch(dropindexattribute_decimal1);
 		st.addBatch(dropindexattribute_double1);
 		st.addBatch(dropindexattribute_datetime1);
 		st.addBatch(dropindexattribute_boolean1);
 
 		st.addBatch(dropindexattribute_literal2);
 		st.addBatch(dropindexattribute_string2);
 		st.addBatch(dropindexattribute_integer2);
 		st.addBatch(dropindexattribute_decimal2);
 		st.addBatch(dropindexattribute_double2);
 		st.addBatch(dropindexattribute_datetime2);
 		st.addBatch(dropindexattribute_boolean2);
 
 		st.addBatch(dropindexattribute_literal3);
 		st.addBatch(dropindexattribute_string3);
 		st.addBatch(dropindexattribute_integer3);
 		st.addBatch(dropindexattribute_decimal3);
 		st.addBatch(dropindexattribute_double3);
 		st.addBatch(dropindexattribute_datetime3);
 		st.addBatch(dropindexattribute_boolean3);
 
 		st.executeBatch();
 		st.close();
 
 		isIndexed = false;
 	}
 
 	@Override
 	public boolean isIndexed(Connection conn) {
 		return isIndexed;
 	}
 
 	@Override
 	public boolean isDBSchemaDefined(Connection conn) throws SQLException {
 		Statement st = conn.createStatement();
 		boolean exists = true;
 		try {
 			st.executeQuery(String.format("SELECT 1 FROM %s WHERE 1=0", class_table));
 			st.executeQuery(String.format("SELECT 1 FROM %s WHERE 1=0", role_table));
 			st.executeQuery(String.format("SELECT 1 FROM %s WHERE 1=0", attribute_table_literal));
 			st.executeQuery(String.format("SELECT 1 FROM %s WHERE 1=0", attribute_table_string));
 			st.executeQuery(String.format("SELECT 1 FROM %s WHERE 1=0", attribute_table_integer));
 			st.executeQuery(String.format("SELECT 1 FROM %s WHERE 1=0", attribute_table_decimal));
 			st.executeQuery(String.format("SELECT 1 FROM %s WHERE 1=0", attribute_table_double));
 			st.executeQuery(String.format("SELECT 1 FROM %s WHERE 1=0", attribute_table_datetime));
 			st.executeQuery(String.format("SELECT 1 FROM %s WHERE 1=0", attribute_table_boolean));
 		} catch (SQLException e) {
 			exists = false;
 			log.debug(e.getMessage());
 		} finally {
 			try {
 				st.close();
 			} catch (SQLException e) {
 
 			}
 		}
 		return exists;
 	}
 
 	@Override
 	public long loadWithFile(Connection conn, final Iterator<Assertion> data) throws SQLException, IOException {
 		//
 		// log.debug("Insert data into schemas using temporary files");
 		//
 		// File tempFileDataPropertiesLiteral =
 		// File.createTempFile("quest-copy-dataprop-literal", ".tmp");
 		// File tempFileDataPropertiesString =
 		// File.createTempFile("quest-copy-dataprop-string", ".tmp");
 		// File tempFileDataPropertiesInteger =
 		// File.createTempFile("quest-copy-dataprop-integer", ".tmp");
 		// File tempFileDataPropertiesDecimal =
 		// File.createTempFile("quest-copy-dataprop-decimal", ".tmp");
 		// File tempFileDataPropertiesDouble =
 		// File.createTempFile("quest-copy-dataprop-double", ".tmp");
 		// File tempFileDataPropertiesDate =
 		// File.createTempFile("quest-copy-dataprop-date", ".tmp");
 		// File tempFileDataPropertiesBoolean =
 		// File.createTempFile("quest-copy-dataprop-boolean", ".tmp");
 		// File tempFileObjectProperties =
 		// File.createTempFile("quest-copy-oprop", ".tmp");
 		//
 		// BufferedWriter outObjectProperties = null;
 		// BufferedWriter outDataPropertiesLiteral = null;
 		// BufferedWriter outDataPropertiesString = null;
 		// BufferedWriter outDataPropertiesInteger = null;
 		// BufferedWriter outDataPropertiesDecimal = null;
 		// BufferedWriter outDataPropertiesDouble = null;
 		// BufferedWriter outDataPropertiesDate = null;
 		// BufferedWriter outDataPropertiesBoolean = null;
 		// try {
 		// outObjectProperties = new BufferedWriter(new OutputStreamWriter(new
 		// FileOutputStream(tempFileObjectProperties)));
 		// outDataPropertiesLiteral = new BufferedWriter(new
 		// OutputStreamWriter(new
 		// FileOutputStream(tempFileDataPropertiesLiteral)));
 		// outDataPropertiesString = new BufferedWriter(new
 		// OutputStreamWriter(new
 		// FileOutputStream(tempFileDataPropertiesString)));
 		// outDataPropertiesInteger = new BufferedWriter(new
 		// OutputStreamWriter(new
 		// FileOutputStream(tempFileDataPropertiesInteger)));
 		// outDataPropertiesDecimal = new BufferedWriter(new
 		// OutputStreamWriter(new
 		// FileOutputStream(tempFileDataPropertiesDecimal)));
 		// outDataPropertiesDouble = new BufferedWriter(new
 		// OutputStreamWriter(new
 		// FileOutputStream(tempFileDataPropertiesDouble)));
 		// outDataPropertiesDate = new BufferedWriter(new OutputStreamWriter(new
 		// FileOutputStream(tempFileDataPropertiesDate)));
 		// outDataPropertiesBoolean = new BufferedWriter(new
 		// OutputStreamWriter(new
 		// FileOutputStream(tempFileDataPropertiesBoolean)));
 		// } catch (FileNotFoundException e) {
 		// log.error(e.getMessage());
 		// log.debug(e.getMessage(), e);
 		// return -1;
 		// }
 		//
 		// File tempFileType = File.createTempFile("quest-copy-type", ".tmp");
 		// BufferedWriter outType = null;
 		// try {
 		// outType = new BufferedWriter(new OutputStreamWriter(new
 		// FileOutputStream(tempFileType)));
 		// } catch (FileNotFoundException e) {
 		// log.error(e.getMessage());
 		// log.debug(e.getMessage(), e);
 		// return -1;
 		// }
 		//
 		// final long[] counts = new long[3];
 		//
 		// final HashMap<Predicate, Integer> indexes = new HashMap<Predicate,
 		// Integer>(this.ontology.getVocabulary().size() * 2);
 		//
 		// int insertscount = 0;
 		//
 		// try {
 		// while (data.hasNext()) {
 		//
 		// Assertion ax = data.next();
 		//
 		// insertscount += 1;
 		//
 		// if (ax instanceof DataPropertyAssertion) {
 		//
 		// DataPropertyAssertion attributeABoxAssertion =
 		// (DataPropertyAssertion) ax;
 		// Predicate attribute = attributeABoxAssertion.getAttribute();
 		// Predicate.COL_TYPE attributeType = getAttributeType(attribute);
 		//
 		// ObjectConstant c1 = attributeABoxAssertion.getObject();
 		// String uri;
 		//
 		// boolean c1isBNode = c1 instanceof BNode;
 		//
 		// if (c1isBNode)
 		// uri = ((BNode) c1).getName();
 		// else
 		// uri = ((URIConstant) c1).getURI().toString();
 		//
 		// String lit = attributeABoxAssertion.getValue().getValue();
 		// String lang = attributeABoxAssertion.getValue().getLanguage();
 		//
 		//
 		// int idx = getIndex(attribute.getName(), 2);
 		//
 		//
 		// switch (attributeType) {
 		// case LITERAL:
 		// appendStringToPropertyFile(outDataPropertiesLiteral, new String[] {
 		// uri, lit, lang, String.valueOf(idx) });
 		// break;
 		// case STRING:
 		// appendStringToPropertyFile(outDataPropertiesString, new String[] {
 		// uri, lit, String.valueOf(idx) });
 		// break;
 		// case INTEGER:
 		// appendStringToPropertyFile(outDataPropertiesString, new String[] {
 		// uri, lit, String.valueOf(idx) });
 		// break;
 		// case DECIMAL:
 		// appendStringToPropertyFile(outDataPropertiesString, new String[] {
 		// uri, lit, String.valueOf(idx) });
 		// break;
 		// case DOUBLE:
 		// appendStringToPropertyFile(outDataPropertiesString, new String[] {
 		// uri, lit, String.valueOf(idx) });
 		// break;
 		// case DATETIME:
 		// appendStringToPropertyFile(outDataPropertiesString, new String[] {
 		// uri, lit, String.valueOf(idx) });
 		// break;
 		// case BOOLEAN:
 		// appendStringToPropertyFile(outDataPropertiesString, new String[] {
 		// uri, lit, String.valueOf(idx) });
 		// break;
 		// }
 		//
 		// } else if (ax instanceof ObjectPropertyAssertion) {
 		//
 		// ObjectPropertyAssertion roleABoxAssertion = (ObjectPropertyAssertion)
 		// ax;
 		//
 		// ObjectConstant c1 = roleABoxAssertion.getFirstObject();
 		// String uri1;
 		//
 		// boolean c1isBNode = c1 instanceof BNode;
 		//
 		// if (c1isBNode)
 		// uri1 = ((BNode) c1).getName();
 		// else
 		// uri1 = ((URIConstant) c1).getURI().toString();
 		//
 		// ObjectConstant c2 = roleABoxAssertion.getFirstObject();
 		// String uri2;
 		//
 		// boolean c2isBNode = c2 instanceof BNode;
 		//
 		// if (c2isBNode)
 		// uri2 = ((BNode) c2).getName();
 		// else
 		// uri2 = ((URIConstant) c2).getURI().toString();
 		//
 		// Predicate propPred = roleABoxAssertion.getRole();
 		// Property propDesc = ofac.createProperty(propPred);
 		//
 		// if (dag.equi_mappings.containsKey(propDesc)) {
 		// Property desc = (Property) dag.equi_mappings.get(propDesc);
 		// if (desc.isInverse()) {
 		// String tmp = uri1;
 		// uri1 = uri2;
 		// uri2 = tmp;
 		// }
 		// }
 		//
 		// int idx = -1;
 		// Integer idxc = indexes.get(propPred);
 		// if (idxc == null) {
 		//
 		// DAGNode node = pureIsa.getRoleNode(propDesc);
 		// if (node == null) {
 		// Property desc = (Property) dag.equi_mappings.get(propDesc);
 		//
 		// if (desc == null) {
 		// log.error("Property class without node: " + propDesc);
 		// }
 		// Property desinv = ofac.createProperty(desc.getPredicate(),
 		// !desc.isInverse());
 		// DAGNode node2 = (pureIsa.getRoleNode(desinv));
 		// idx = node2.getIndex();
 		// } else {
 		// idx = node.getIndex();
 		// }
 		// indexes.put(roleABoxAssertion.getRole(), idx);
 		// } else {
 		// idx = idxc;
 		// }
 		//
 		// outObjectProperties.append(uri1);
 		// outObjectProperties.append('\t');
 		// outObjectProperties.append(uri2);
 		// outObjectProperties.append('\t');
 		// outObjectProperties.append(String.valueOf(idx));
 		// outObjectProperties.append('\n');
 		//
 		// } else if (ax instanceof ClassAssertion) {
 		//
 		// ClassAssertion cassertion = (ClassAssertion) ax;
 		// Predicate pred = cassertion.getConcept();
 		//
 		// int idx = -1;
 		// Integer idxc = indexes.get(cassertion.getConcept());
 		// if (idxc == null) {
 		// Predicate clsPred = cassertion.getConcept();
 		// ClassDescription clsDesc = ofac.createClass(clsPred);
 		// DAGNode node = pureIsa.getClassNode(clsDesc);
 		// if (node == null) {
 		// String cls = cassertion.getConcept().getName().toString();
 		// log.error("Found class without node: " + cls.toString());
 		// }
 		// idx = node.getIndex();
 		// indexes.put(pred, idx);
 		// } else {
 		// idx = idxc;
 		// }
 		//
 		// ObjectConstant c1 = cassertion.getObject();
 		// String uri1;
 		//
 		// boolean c1isBNode = c1 instanceof BNode;
 		//
 		// if (c1isBNode)
 		// uri1 = ((BNode) c1).getName();
 		// else
 		// uri1 = ((URIConstant) c1).getURI().toString();
 		//
 		// outType.append(uri1);
 		// outType.append('\t');
 		// outType.append(String.valueOf(idx));
 		// outType.append('\n');
 		// }
 		// }
 		// outType.flush();
 		// outType.close();
 		//
 		// outObjectProperties.flush();
 		// outObjectProperties.close();
 		//
 		// outDataPropertiesLiteral.flush();
 		// outDataPropertiesLiteral.close();
 		//
 		// outDataPropertiesString.flush();
 		// outDataPropertiesString.close();
 		//
 		// outDataPropertiesInteger.flush();
 		// outDataPropertiesInteger.close();
 		//
 		// outDataPropertiesDecimal.flush();
 		// outDataPropertiesDecimal.close();
 		//
 		// outDataPropertiesDouble.flush();
 		// outDataPropertiesDouble.close();
 		//
 		// outDataPropertiesDate.flush();
 		// outDataPropertiesDate.close();
 		//
 		// outDataPropertiesBoolean.flush();
 		// outDataPropertiesBoolean.close();
 		// log.debug("Finished reading input assertions.");
 		// } catch (IOException e) {
 		// log.error(e.getMessage());
 		// log.debug(e.getMessage(), e);
 		// } finally {
 		// // NO-OP
 		// }
 		//
 		// /*
 		// * All data has been generated. Sending the data to the database.
 		// */
 		//
 		// final CopyManager cm = new CopyManager((BaseConnection) conn);
 		//
 		// try {
 		// log.debug("Inserting object properties");
 		// FileReader inprop = new FileReader(tempFileObjectProperties);
 		// counts[0] = cm.copyIn("COPY " + role_table + " FROM STDIN", inprop);
 		// } catch (Exception e) {
 		// log.error(e.getMessage());
 		// } finally {
 		// try {
 		// tempFileObjectProperties.delete();
 		// } catch (Exception e) {
 		// // NO-OP
 		// }
 		// }
 		//
 		// try {
 		// log.debug("Inserting data properties");
 		//
 		// counts[1] = 0; // init
 		// FileReader inprop = new FileReader(tempFileDataPropertiesLiteral);
 		// counts[1] += cm.copyIn("COPY " + attribute_table_literal +
 		// " FROM STDIN", inprop);
 		// inprop = new FileReader(tempFileDataPropertiesString);
 		// counts[1] += cm.copyIn("COPY " + attribute_table_string +
 		// " FROM STDIN", inprop);
 		// inprop = new FileReader(tempFileDataPropertiesInteger);
 		// counts[1] += cm.copyIn("COPY " + attribute_table_integer +
 		// " FROM STDIN", inprop);
 		// inprop = new FileReader(tempFileDataPropertiesDecimal);
 		// counts[1] += cm.copyIn("COPY " + attribute_table_decimal +
 		// " FROM STDIN", inprop);
 		// inprop = new FileReader(tempFileDataPropertiesDouble);
 		// counts[1] += cm.copyIn("COPY " + attribute_table_double +
 		// " FROM STDIN", inprop);
 		// inprop = new FileReader(tempFileDataPropertiesDate);
 		// counts[1] += cm.copyIn("COPY " + attribute_table_datetime +
 		// " FROM STDIN", inprop);
 		// inprop = new FileReader(tempFileDataPropertiesBoolean);
 		// counts[1] += cm.copyIn("COPY " + attribute_table_boolean +
 		// " FROM STDIN", inprop);
 		// } catch (Exception e) {
 		// log.error(e.getMessage());
 		// } finally {
 		// try {
 		// tempFileDataPropertiesLiteral.delete();
 		// tempFileDataPropertiesString.delete();
 		// tempFileDataPropertiesInteger.delete();
 		// tempFileDataPropertiesDecimal.delete();
 		// tempFileDataPropertiesDouble.delete();
 		// tempFileDataPropertiesDate.delete();
 		// tempFileDataPropertiesBoolean.delete();
 		// } catch (Exception e) {
 		// // NO-OP
 		// }
 		// }
 		//
 		// try {
 		// log.debug("Inserting type assertions");
 		// FileReader intype = new FileReader(tempFileType);
 		// counts[2] = cm.copyIn("COPY " + class_table + " FROM STDIN", intype);
 		// } catch (Exception e) {
 		// log.error(e.getMessage());
 		// } finally {
 		// try {
 		// tempFileType.delete();
 		// } catch (Exception e) {
 		// // NO-OP
 		// }
 		// }
 		//
 		// if (insertscount != (counts[0] + counts[1] + counts[2])) {
 		// log.warn("Warning, effective inserts are different than the elements in the stream: in {}, effective: {}",
 		// insertscount,
 		// counts[0] + counts[1] + counts[2]);
 		// }
 		// return counts[0] + counts[1] + counts[2];
 		// }
 		//
 		// private void appendStringToPropertyFile(BufferedWriter writer,
 		// String[] input) throws IOException {
 		// for (int i = 0; i < input.length; i++) {
 		// writer.append(input[i]);
 		// if (i != input.length - 1) {
 		// writer.append('\t');
 		// } else {
 		// writer.append('\n');
 		// }
 		// }
 
 		return 0;
 
 	}
 
 	//
 	class InsertionMonitor {
 
 		private int success = 0;
 		private Map<Predicate, Integer> failures = new HashMap<Predicate, Integer>();
 
 		void success() {
 			success++;
 		}
 
 		void fail(Predicate predicate) {
 			Integer counter = failures.get(predicate);
 			if (counter == null) {
 				counter = new Integer(0);
 			}
 			failures.put(predicate, counter + 1);
 		}
 
 		int getSuccessCount() {
 			return success;
 		}
 
 		Map<Predicate, Integer> getFailureCount() {
 			return failures;
 		}
 
 		public void printLog() {
 			log.debug("Total successful insertions: " + success + ".");
 			int totalFailures = 0;
 			for (Predicate predicate : failures.keySet()) {
 				int failure = failures.get(predicate);
 				log.warn("Failed to insert data for predicate {} ({} tuples).", predicate, failure);
 				totalFailures += failure;
 			}
 			if (totalFailures > 0) {
 				log.warn("Total failed insertions: " + totalFailures + ". (REASON: datatype mismatch between the ontology and database).");
 			}
 		}
 	}
 
 	// @Override
 	// public boolean isEmpty(Function atom) {
 	// int index = getIndexHash(atom);
 	//
 	// Boolean empty = emptynessIndexes.get(index);
 	// if (empty == null)
 	// return true;
 	// return empty;
 	// }
 
 	public LinkedHashSet<String> getUriIds(){
 		return uriIds;
 	}
 	
 	/***
 	 * A hashing for indexing functions. Implemented using String hashing code.
 	 * 
 	 * @param f
 	 * @return
 	 */
 	public static int getIndexHash(Function f) {
 		int hash = 0;
 
 		hash = f.getPredicate().hashCode() * (31 ^ f.getArity());
 		for (int i = 0; i < f.getArity(); i++) {
 			NewLiteral term = f.getTerm(i);
 			int termhash = getHash((Function) term);
 			hash += termhash * (31 ^ (f.getArity() - (i + 1)));
 		}
 
 		/*
 		 * Compensating in the hash for the arity of the function. We asume
 		 * functions of arity 3 (triple) other functions need to be padded,
 		 * hence the xtra sums.
 		 */
 		if (f.getArity() == 1) {
 			hash += 31 ^ 1;
 			hash += 31 ^ 0;
 		} else if (f.getArity() == 2) {
 			hash += 31 ^ 0;
 		}
 
 		return hash;
 	}
 
 	private static int getHash(Function f) {
 		int hash;
 		if (f.getReferencedVariables().isEmpty()) {
 			hash = f.hashCode();
 		} else
 			hash = (f.getPredicate().hashCode() + f.getTerms().size());
 		return hash;
 	}
 
 }
