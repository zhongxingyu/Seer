 /* 
  * Copyright 2011 Antidot opensource@antidot.net
  * https://github.com/antidot/db2triples
  * 
  * DB2Triples is free software; you can redistribute it and/or 
  * modify it under the terms of the GNU General Public License as 
  * published by the Free Software Foundation; either version 2 of 
  * the License, or (at your option) any later version.
  * 
  * DB2Triples is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  * 
  * You should have received a copy of the GNU General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 /***************************************************************************
  *
  * R2RML : R2RML Engine
  *
  * The R2RML engine is the link between a R2RML Mapping object
  * and a database connection. It constructs the final RDF graph
  * from the R2RML mapping document by extracting data in database.
  *
  *
  ****************************************************************************/
 package net.antidot.semantic.rdf.rdb2rdf.r2rml.core;
 
 import java.io.UnsupportedEncodingException;
 import java.sql.Connection;
 import java.sql.ResultSet;
 import java.sql.ResultSetMetaData;
 import java.sql.SQLException;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Map;
 import java.util.Set;
 
 import net.antidot.semantic.rdf.model.impl.sesame.SesameDataSet;
 import net.antidot.semantic.rdf.model.tools.RDFDataValidator;
 import net.antidot.semantic.rdf.rdb2rdf.commons.SQLToXMLS;
 import net.antidot.semantic.rdf.rdb2rdf.r2rml.core.R2RMLVocabulary.R2RMLTerm;
 import net.antidot.semantic.rdf.rdb2rdf.r2rml.exception.R2RMLDataError;
 import net.antidot.semantic.rdf.rdb2rdf.r2rml.model.GraphMap;
 import net.antidot.semantic.rdf.rdb2rdf.r2rml.model.ObjectMap;
 import net.antidot.semantic.rdf.rdb2rdf.r2rml.model.PredicateMap;
 import net.antidot.semantic.rdf.rdb2rdf.r2rml.model.PredicateObjectMap;
 import net.antidot.semantic.rdf.rdb2rdf.r2rml.model.R2RMLMapping;
 import net.antidot.semantic.rdf.rdb2rdf.r2rml.model.ReferencingObjectMap;
 import net.antidot.semantic.rdf.rdb2rdf.r2rml.model.SubjectMap;
 import net.antidot.semantic.rdf.rdb2rdf.r2rml.model.TermMap;
 import net.antidot.semantic.rdf.rdb2rdf.r2rml.model.TermMap.TermMapType;
 import net.antidot.semantic.rdf.rdb2rdf.r2rml.model.TermType;
 import net.antidot.semantic.rdf.rdb2rdf.r2rml.model.TriplesMap;
 import net.antidot.semantic.rdf.rdb2rdf.r2rml.tools.R2RMLToolkit;
 import net.antidot.semantic.xmls.xsd.XSDType;
 import net.antidot.sql.model.db.ColumnIdentifier;
 import net.antidot.sql.model.db.ColumnIdentifierImpl;
 import net.antidot.sql.model.tools.SQLToolkit;
 import net.antidot.sql.model.type.SQLType;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.openrdf.model.BNode;
 import org.openrdf.model.Resource;
 import org.openrdf.model.Statement;
 import org.openrdf.model.URI;
 import org.openrdf.model.Value;
 import org.openrdf.model.ValueFactory;
 import org.openrdf.model.impl.ValueFactoryImpl;
 
 public class R2RMLEngine {
 
 	// Log
 	private static Log log = LogFactory.getLog(R2RMLEngine.class);
 
 	// SQL Connection
 	private Connection conn;
 	// Current logical table
 	private ResultSet rows;
 	// Current referencing table
 	private ResultSet referencingRows;
 	// Current meta data of logical table
 	private ResultSetMetaData meta;
 	// A base IRI used in resolving relative IRIs produced by the R2RML mapping.
 	private String baseIRI;
 
 	private Map<TermMap, Map<Integer, ResultSet>> sameRows;
 	private Map<TermMap, Map<Integer, Value>> sameGeneratedRDFTerm;
 
 	// Value factory
 	private static ValueFactory vf = new ValueFactoryImpl();
 
 	public R2RMLEngine(Connection conn) {
 		super();
 		if (conn == null)
 			throw new IllegalStateException(
 					"[R2RMLEngine:R2RMLEngine] SQL connection does not exists.");
 		this.conn = conn;
 		rows = null;
 	}
 
 	/**
 	 * Execute R2RML Mapping from a R2RML file in order to generate a RDF
 	 * dataset. This dataset is built with Sesame API.
 	 * 
 	 * @param r2rmlMapping
 	 * @param baseIRI
 	 * @return
 	 * @throws SQLException
 	 * @throws R2RMLDataError
 	 * @throws UnsupportedEncodingException
 	 */
 	public SesameDataSet runR2RMLMapping(R2RMLMapping r2rmlMapping,
 			String baseIRI, String pathToNativeStore) throws SQLException,
 			R2RMLDataError, UnsupportedEncodingException {
 		log.debug("[R2RMLEngine:runR2RMLMapping] Run R2RML mapping... ");
 		if (r2rmlMapping == null)
 			throw new IllegalArgumentException(
 					"[R2RMLEngine:runR2RMLMapping] No R2RML Mapping object found.");
 		if (baseIRI == null)
 			throw new IllegalArgumentException(
 					"[R2RMLEngine:runR2RMLMapping] No base IRI found.");
 
 		SesameDataSet sesameDataSet = null;
 		// Update baseIRI
 		this.baseIRI = baseIRI;
 		// Check if use of native store is required
 		if (pathToNativeStore != null) {
 			log.debug("[R2RMLEngine:runR2RMLMapping] Use native store "
 					+ pathToNativeStore);
 			sesameDataSet = new SesameDataSet(pathToNativeStore, false);
 		} else {
 			sesameDataSet = new SesameDataSet();
 		}
 		// Update inverse expression settings
 		sameRows = new HashMap<TermMap, Map<Integer, ResultSet>>();
 		sameGeneratedRDFTerm = new HashMap<TermMap, Map<Integer, Value>>();
 
 		// Explore R2RML Mapping TriplesMap objects
 		generateRDFTriples(sesameDataSet, r2rmlMapping);
 
 		// Close connection to logical table
 		if (rows != null) {
 			rows.getStatement().close();
 			rows.close();
 		}
 		log.debug("[R2RMLEngine:runR2RMLMapping] R2RML mapping done. ");
 		return sesameDataSet;
 	}
 
 	/**
 	 * This process adds RDF triples to the output dataset. Each generated
 	 * triple is placed into one or more graphs of the output dataset. The
 	 * generated RDF triples are determined by the following algorithm.
 	 * 
 	 * @param sesameDataSet
 	 * @param r2rmlMapping
 	 * @throws SQLException
 	 * @throws R2RMLDataError
 	 * @throws UnsupportedEncodingException
 	 */
 	private void generateRDFTriples(SesameDataSet sesameDataSet,
 			R2RMLMapping r2rmlMapping) throws SQLException, R2RMLDataError,
 			UnsupportedEncodingException {
 		log.debug("[R2RMLEngine:generateRDFTriples] Generate RDF triples... ");
 		int delta = 0;
 		for (TriplesMap triplesMap : r2rmlMapping.getTriplesMaps()) {
 			genereateRDFTriplesFromTriplesMap(sesameDataSet, triplesMap);
 			log.info("[R2RMLEngine:generateRDFTriples] "
 					+ (sesameDataSet.getSize() - delta)
 					+ " triples generated for " + triplesMap.getName());
 			delta = sesameDataSet.getSize();
 		}
 	}
 
 	private void genereateRDFTriplesFromTriplesMap(SesameDataSet sesameDataSet,
 			TriplesMap triplesMap) throws SQLException, R2RMLDataError,
 			UnsupportedEncodingException {
 		log.debug("[R2RMLEngine:genereateRDFTriplesFromTriplesMap] Generate RDF triples from triples map... ");
 		// 1. Let sm be the subject map of the triples map
 		SubjectMap sm = triplesMap.getSubjectMap();
 		// Check inverse expression
 		// 2. Let rows be the result of evaluating the effective SQL query
 		rows = constructLogicalTable(triplesMap);
 		meta = extractMetaDatas(rows);
 		// 3. Let classes be the class IRIs of sm
 		Set<URI> classes = sm.getClassIRIs();
 		// 4. Let sgm be the set of graph maps of sm
 		Set<GraphMap> sgm = sm.getGraphMaps();
 		// 5. For each logical table row in rows, apply the following method
 		while (rows.next()) {
 			generateRDFTriplesFromRow(sesameDataSet, triplesMap, sm, classes,
 					sgm);
 		}
 		// 6. For each referencing object map of a predicate-object map of the
 		// triples map,
 		// apply the following method
 		for (PredicateObjectMap predicateObjectMap : triplesMap
 				.getPredicateObjectMaps())
 			for (ReferencingObjectMap referencingObjectMap : predicateObjectMap
 					.getReferencingObjectMaps()) {
 				generateRDFTriplesFromReferencingObjectMap(sesameDataSet,
 						triplesMap, sm, sgm, predicateObjectMap,
 						referencingObjectMap);
 			}
 	}
 
 	/*
 	 * An inverse expression MUST satisfy the following condition.
 	 */
 	private void performInverseExpression(Map<ColumnIdentifier, byte[]> dbValues,
 			TermMap tm, String effectiveSQLQuery) throws SQLException,
 			R2RMLDataError, UnsupportedEncodingException {
 		// Every column reference in the inverse expression MUST
 		// be an existing column in t
 		String inverseExpression = tm.getInverseExpression();
 		if (inverseExpression == null)
 			return;
 		Set<ColumnIdentifier> columnReferences = R2RMLToolkit
 				.extractColumnNamesFromInverseExpression(inverseExpression);
 		Set<ColumnIdentifier> existingColumns = getExistingColumnNames();
 		for (ColumnIdentifier referencedColumns : columnReferences) {
 		    boolean found = false;
 		    for (ColumnIdentifier existingColumn : existingColumns) {
 			if(existingColumn.equals(referencedColumns)) {
 			    found = true;
 			    break;
 			}
 		    }
 		    if (!found)
 			throw new R2RMLDataError("[R2RMLEngine:checkInverseExpression] Every column"
 								+ " reference in the inverse expression must be an existing column : "
 								+ referencedColumns + " does not exist.");
 		}
 		// Let instantiation(r)
 		String instantiation = R2RMLToolkit
 				.extractColumnValueFromInverseExpression(inverseExpression,
 						dbValues, columnReferences);
 		// let same-term(r) be the set of logical table rows in t that are
 		// the result of executing the following SQL query over the SQL
 		// connection
 		ResultSet sameTerm = constructInversionTable(instantiation,
 				effectiveSQLQuery);
 		// For every logical table row r in t whose generated RDF term g is not
 		// NULL, same-term(r)
 		// MUST be exactly the set of logical table rows in t whose generated
 		// RDF term is also g
 		// => Save similar rows
 		if (sameRows.containsKey(tm)) {
 			sameRows.get(tm).put(rows.getRow(), sameTerm);
 		} else {
 			Map<Integer, ResultSet> sameRowsMap = new HashMap<Integer, ResultSet>();
 			sameRowsMap.put(rows.getRow(), sameTerm);
 			sameRows.put(tm, sameRowsMap);
 		}
 
 	}
 
 	private Set<ColumnIdentifier> getExistingColumnNames() throws SQLException {
 		Set<ColumnIdentifier> result = new HashSet<ColumnIdentifier>();
 		for (int i = 1; i <= meta.getColumnCount(); i++)
 		    result.add(ColumnIdentifierImpl.buildFromJDBCResultSet(meta, i));
 		if (referencingRows != null)
 			for (int i = 1; i <= referencingRows.getMetaData().getColumnCount(); i++)
 			    	result.add(ColumnIdentifierImpl.buildFromJDBCResultSet(referencingRows.getMetaData(), i));
 		return result;
 	}
 
 	private ResultSetMetaData extractMetaDatas(ResultSet rows2)
 			throws SQLException {
 		ResultSetMetaData meta = rows.getMetaData();
 		// Tests the presence of duplicate column names in the SELECT list of
 		// the SQL query
 		Set<String> columnsNames = new HashSet<String>();
 		for (int i = 1; i <= meta.getColumnCount(); i++) {
 			String columnName = meta.getColumnLabel(i);
 			if (!columnsNames.contains(columnName))
 				columnsNames.add(columnName);
 			else
 				throw new SQLException(
 						"[R2RMLEngine:extractMetaDatas] duplicate column names in the "
 								+ "SELECT list of the SQL query");
 
 		}
 		return meta;
 	}
 
 	private void generateRDFTriplesFromReferencingObjectMap(
 			SesameDataSet sesameDataSet, TriplesMap triplesMap, SubjectMap sm,
 			Set<GraphMap> sgm, PredicateObjectMap predicateObjectMap,
 			ReferencingObjectMap referencingObjectMap) throws SQLException,
 			R2RMLDataError, UnsupportedEncodingException {
 		// 1. Let psm be the subject map of the parent triples map of the
 		// referencing object map
 		SubjectMap psm = referencingObjectMap.getParentTriplesMap()
 				.getSubjectMap();
 		// 2. Let pogm be the set of graph maps of the predicate-object map
 		Set<GraphMap> pogm = predicateObjectMap.getGraphMaps();
 		// 3. Let n be the number of columns in the logical table of the triples
 		// map
 		int n = meta.getColumnCount();
 		// 4. Let rows be the result of evaluating the joint SQL query of the
 		// referencing object map
 		referencingRows = constructJointTable(referencingObjectMap);
 		// 5. For each row in rows, apply the following method
 		while (referencingRows.next())
 			generateRDFTriplesFromReferencingRow(sesameDataSet, triplesMap, sm,
 					psm, pogm, sgm, predicateObjectMap, n);
 	}
 
 	private void generateRDFTriplesFromReferencingRow(
 			SesameDataSet sesameDataSet, TriplesMap triplesMap, SubjectMap sm,
 			SubjectMap psm, Set<GraphMap> pogm, Set<GraphMap> sgm,
 			PredicateObjectMap predicateObjectMap, int n) throws SQLException,
 			R2RMLDataError, UnsupportedEncodingException {
 		// 1. Let child_row be the logical table row derived by
 		// taking the first n columns of row : see step 3, 4, 6 and 7
 		// 2. Let parent_row be the logical table row derived by taking all
 		// but the first n columns of row : see step 5
 		// 3. Let subject be the generated RDF term that results from
 		// applying sm to child_row
 		Map<ColumnIdentifier, byte[]> smFromRow = applyValueToChildRow(sm, n);
 		boolean nullFound = false;
 		for (ColumnIdentifier value : smFromRow.keySet())
 			if (smFromRow.get(value) == null) {
 				log.debug("[R2RMLEngine:genereateRDFTriplesFromRow] NULL found, this object will be ignored.");
 				nullFound = true;
 				break;
 			}
 		if (nullFound)
 			return;
 		Resource subject = (Resource) extractValueFromTermMap(sm, smFromRow,
 				triplesMap);
 		log.debug("[R2RMLEngine:generateRDFTriplesFromReferencingRow] Generate subject : "
 				+ subject.stringValue());
 		// 4. Let predicates be the set of generated RDF terms that result from
 		// applying each of the predicate-object map's predicate maps to
 		// child_row
 		Set<URI> predicates = new HashSet<URI>();
 		for (PredicateMap pm : predicateObjectMap.getPredicateMaps()) {
 			Map<ColumnIdentifier, byte[]> pmFromRow = applyValueToChildRow(pm, n);
 			URI predicate = (URI) extractValueFromTermMap(pm, pmFromRow,
 					triplesMap);
 			log.debug("[R2RMLEngine:generateRDFTriplesFromReferencingRow] Generate predicate : "
 					+ predicate);
 			predicates.add(predicate);
 		}
 		// 5. Let object be the generated RDF term that results from applying
 		// psm to parent_row
 		Map<ColumnIdentifier, byte[]> omFromRow = applyValueToParentRow(psm, n);
 		Resource object = (Resource) extractValueFromTermMap(psm, omFromRow,
 				psm.getOwnTriplesMap());
 		log.debug("[R2RMLEngine:generateRDFTriplesFromReferencingRow] Generate object : "
 				+ object);
 		// 6. Let subject_graphs be the set of generated RDF terms that result
 		// from applying each graph map of sgm to child_row
 		Set<URI> subject_graphs = new HashSet<URI>();
 		for (GraphMap graphMap : sgm) {
 			Map<ColumnIdentifier, byte[]> sgmFromRow = applyValueToChildRow(graphMap, n);
 			URI subject_graph = (URI) extractValueFromTermMap(graphMap,
 					sgmFromRow, triplesMap);
 			log.debug("[R2RMLEngine:generateRDFTriplesFromReferencingRow] Generate subject graph : "
 					+ subject_graph);
 			subject_graphs.add(subject_graph);
 		}
 		// 7. Let predicate-object_graphs be the set of generated RDF terms
 		// that result from applying each graph map in pogm to child_row
 		Set<URI> predicate_object_graphs = new HashSet<URI>();
 		for (GraphMap graphMap : pogm) {
 			Map<ColumnIdentifier, byte[]> pogmFromRow = applyValueToChildRow(graphMap, n);
 			URI predicate_object_graph = (URI) extractValueFromTermMap(
 					graphMap, pogmFromRow, triplesMap);
 			log.debug("[R2RMLEngine:generateRDFTriplesFromReferencingRow] Generate predicate object graph : "
 					+ predicate_object_graph);
 			predicate_object_graphs.add(predicate_object_graph);
 		}
 		// 8. For each predicate in predicates, add triples to the output
 		// dataset
 		for (URI predicate : predicates) {
 			// If neither sgm nor pogm has any graph maps: rr:defaultGraph;
 			// otherwise: union of subject_graphs and predicate-object_graphs
 			Set<URI> targetGraphs = new HashSet<URI>();
 			targetGraphs.addAll(subject_graphs);
 			targetGraphs.addAll(predicate_object_graphs);
 			addTriplesToTheOutputDataset(sesameDataSet, subject, predicate,
 					object, targetGraphs);
 		}
 	}
 
 	/**
 	 * Parent_row is the logical table row derived by taking all but the first n
 	 * columns of row.
 	 * 
 	 * @param tm
 	 * @param n
 	 * @return
 	 * @throws SQLException
 	 * @throws R2RMLDataError
 	 */
 	private Map<ColumnIdentifier, byte[]> applyValueToParentRow(TermMap tm, int n)
 			throws SQLException, R2RMLDataError {
 		Map<ColumnIdentifier, byte[]> result = new HashMap<ColumnIdentifier, byte[]>();
 		Set<ColumnIdentifier> columns = tm.getReferencedColumns();
 		ResultSetMetaData referencingMetas = referencingRows.getMetaData();
 		for (ColumnIdentifier column : columns) {
 			int m = -1;
 			for (int i = 1; i <= referencingMetas.getColumnCount(); i++) {
 			    ColumnIdentifier refCol = ColumnIdentifierImpl.buildFromJDBCResultSet(referencingMetas, i);
 			    if(refCol.equals(column)) {
 				m = i;
 			    }
 
 			}
 			if (m == -1)
 				throw new R2RMLDataError(
 						"[R2RMLEngine:applyValueToParentRow] Unknown " + column
 								+ "in parent row.");
 			if (m >= n) {
 				byte[] value = referencingRows.getBytes(m);
 				result.put(column, value);
 			}
 		}
 		return result;
 	}
 
 	/**
 	 * Child_row is the logical table row derived by taking the first n columns
 	 * of row.
 	 * 
 	 * @param tm
 	 * @param n
 	 * @return
 	 * @throws SQLException
 	 * @throws R2RMLDataError
 	 */
 	private Map<ColumnIdentifier, byte[]> applyValueToChildRow(TermMap tm, int n)
 			throws SQLException, R2RMLDataError {
 		Map<ColumnIdentifier, byte[]> result = new HashMap<ColumnIdentifier, byte[]>();
 		Set<ColumnIdentifier> columns = tm.getReferencedColumns();
 		ResultSetMetaData referencingMetas = referencingRows.getMetaData();
 		for (ColumnIdentifier column : columns) {
 			int m = -1;
 			for (int i = 1; i <= referencingMetas.getColumnCount(); i++) {
 				ColumnIdentifier refCol = ColumnIdentifierImpl.buildFromJDBCResultSet(referencingMetas, i);
 				if ( refCol.equals(column)) {
 				    m = i;
 				    break;
 				}
 			}
 			if (m == -1)
 				throw new R2RMLDataError(
 						"[R2RMLEngine:applyValueToChildRow] Unknown " + column
 								+ "in child row.");
 			if (m <= n) {
 				byte[] value = referencingRows.getBytes(m);
 				result.put(column, value);
 			}
 		}
 		return result;
 	}
 
 	private void generateRDFTriplesFromRow(SesameDataSet sesameDataSet,
 			TriplesMap triplesMap, SubjectMap sm, Set<URI> classes,
 			Set<GraphMap> sgm) throws SQLException, R2RMLDataError,
 			UnsupportedEncodingException {
 
 		// 1. Let subject be the generated RDF term that results from applying
 		// sm to row
 		Map<ColumnIdentifier, byte[]> smFromRow = applyValueToRow(sm);
 		Resource subject = (Resource) extractValueFromTermMap(sm, smFromRow,
 				triplesMap);
 		if (subject == null) {
 			log.debug("[R2RMLEngine:genereateRDFTriplesFromRow] NULL found, this subject will be ignored.");
 			return;
 		} else
 			log.debug("[R2RMLEngine:genereateRDFTriplesFromRow] Generate subject : "
 					+ subject.stringValue());
 
 		// 2. Let subject_graphs be the set of the generated RDF terms
 		// that result from applying each term map in sgm to row
 		Set<URI> subject_graphs = new HashSet<URI>();
 		for (GraphMap graphMap : sgm) {
 			Map<ColumnIdentifier, byte[]> sgmFromRow = applyValueToRow(graphMap);
 			URI subject_graph = (URI) extractValueFromTermMap(graphMap,
 					sgmFromRow, triplesMap);
 			log.debug("[R2RMLEngine:genereateRDFTriplesFromRow] Generate subject graph : "
 					+ subject_graph);
 			subject_graphs.add(subject_graph);
 		}
 		// 3. For each classIRI in classes, add triples to the output dataset
 		for (URI classIRI : sm.getClassIRIs()) {
 			URI predicate = vf.createURI(R2RMLVocabulary.RDF_NAMESPACE
 					+ R2RMLTerm.TYPE);
 			addTriplesToTheOutputDataset(sesameDataSet, subject, predicate,
 					classIRI, subject_graphs);
 		}
 		// 4. For each predicate-object map of the triples map, apply the
 		// following method
 		for (PredicateObjectMap predicateObjectMap : triplesMap
 				.getPredicateObjectMaps())
 			generateRDFTriplesFromPredicateObjectMap(sesameDataSet, triplesMap,
 					subject, subject_graphs, predicateObjectMap);
 
 	}
 
 	private Value extractValueFromTermMap(TermMap tm,
 			Map<ColumnIdentifier, byte[]> smFromRow, TriplesMap triplesMap)
 			throws SQLException, R2RMLDataError, UnsupportedEncodingException {
 		Value result = null;
 		if (tm.getInverseExpression() != null) {
 			
 			if (!sameRows.containsKey(tm)) {
 				// First perform of inversion expression
 				performInverseExpression(smFromRow, tm, triplesMap
 						.getLogicalTable().getEffectiveSQLQuery());
 				// Generate RDF Term
 				String value = tm.getValue(smFromRow, meta);
 				if (value == null)
 					return null;
 				result = (Resource) generateRDFTerm(tm, value);
 				// Store Generated RDF Term
 				Map<Integer, Value> sameGeneratedRDFTermMap = new HashMap<Integer, Value>();
 				sameGeneratedRDFTermMap.put(rows.getRow(), result);
 				sameGeneratedRDFTerm.put(tm, sameGeneratedRDFTermMap);
 			} else {
 				// Check if the current row exists in sameRows
 				Map<Integer, ResultSet> rowsMap = sameRows.get(tm);
 
 				for (Integer rm : rowsMap.keySet()) {
 					if (SQLToolkit.containsTheSameRow(rows, rowsMap.get(rm))) {
 						log.debug("[R2RMLEngine:extractSubject] Generate subject graph already exists thanks to inversion expression.");
 						result = (Resource) sameGeneratedRDFTerm.get(rm);
 						break;
 					}
 				}
 				if (result == null) {
 					// Generated RDF Term not found : run inversion search
 					performInverseExpression(smFromRow, tm, triplesMap
 							.getLogicalTable().getEffectiveSQLQuery());
 					// Generate RDF Term
 					String value = tm.getValue(smFromRow, meta);
 					if (value == null)
 						return null;
 					result = (Resource) generateRDFTerm(tm, value);
 					// Store Generated RDF Term
 					if (sameGeneratedRDFTerm.containsKey(tm)) {
 						sameGeneratedRDFTerm.get(tm).put(rows.getRow(), result);
 					} else {
 						Map<Integer, Value> sameGeneratedRDFTermMap = new HashMap<Integer, Value>();
 						sameGeneratedRDFTermMap.put(rows.getRow(), result);
 						sameGeneratedRDFTerm.put(tm, sameGeneratedRDFTermMap);
 					}
 				}
 			}
 		} else {
 			String value = tm.getValue(smFromRow, meta);
 			result = generateRDFTerm(tm, value);
 		}
 		return result;
 	}
 
 	private void generateRDFTriplesFromPredicateObjectMap(
 			SesameDataSet sesameDataSet, TriplesMap triplesMap,
 			Resource subject, Set<URI> subjectGraphs,
 			PredicateObjectMap predicateObjectMap) throws SQLException,
 			R2RMLDataError, UnsupportedEncodingException {
 		// 1. Let predicates be the set of generated RDF terms that result
 		// from applying each of the predicate-object map's predicate maps to
 		// row
 		Set<URI> predicates = new HashSet<URI>();
 		for (PredicateMap pm : predicateObjectMap.getPredicateMaps()) {
 			Map<ColumnIdentifier, byte[]> pmFromRow = applyValueToRow(pm);
 			boolean nullFound = false;
 			for (ColumnIdentifier value : pmFromRow.keySet())
 				if (pmFromRow.get(value) == null) {
 					log.debug("[R2RMLEngine:genereateRDFTriplesFromRow] NULL found, this object will be ignored.");
 					nullFound = true;
 					break;
 				}
 			if (nullFound)
 				continue;
 			URI predicate = (URI) extractValueFromTermMap(pm, pmFromRow,
 					triplesMap);
 			log.debug("[R2RMLEngine:genereateRDFTriplesFromRow] Generate predicate : "
 					+ predicate);
 			predicates.add(predicate);
 		}
 		// 2. Let objects be the set of generated RDF terms that result from
 		// applying each of
 		// the predicate-object map's object maps (but not referencing object
 		// maps) to row
 		Set<Value> objects = new HashSet<Value>();
 		for (ObjectMap om : predicateObjectMap.getObjectMaps()) {
 			Map<ColumnIdentifier, byte[]> omFromRow = applyValueToRow(om);
 			boolean nullFound = false;
 			for (ColumnIdentifier value : omFromRow.keySet())
 				if (omFromRow.get(value) == null) {
 					log.debug("[R2RMLEngine:genereateRDFTriplesFromRow] NULL found, this object will be ignored.");
 					nullFound = true;
 					break;
 				}
 			if (nullFound)
 				continue;
 			Value object = extractValueFromTermMap(om, omFromRow, triplesMap);
 			log.debug("[R2RMLEngine:genereateRDFTriplesFromRow] Generate object : "
 					+ object);
 			objects.add(object);
 		}
 		// 3. Let pogm be the set of graph maps of the predicate-object map
 		Set<GraphMap> pogm = predicateObjectMap.getGraphMaps();
 		// 4. Let predicate-object_graphs be the set of generated RDF
 		// terms that result from applying each graph map in pogm to row
 		Set<URI> predicate_object_graphs = new HashSet<URI>();
 		// 4+. Add graph of subject graphs set
 		if (subjectGraphs != null)
 			predicate_object_graphs.addAll(subjectGraphs);
 		for (GraphMap graphMap : pogm) {
 			Map<ColumnIdentifier, byte[]> pogmFromRow = applyValueToRow(graphMap);
 			URI predicate_object_graph = (URI) extractValueFromTermMap(
 					graphMap, pogmFromRow, triplesMap);
 			log.debug("[R2RMLEngine:genereateRDFTriplesFromRow] Generate predicate object graph : "
 					+ predicate_object_graph);
 			predicate_object_graphs.add(predicate_object_graph);
 		}
 		// 5. For each possible combination <predicate, object> where predicate
 		// is a member
 		// of predicates and object is a member of objects,
 		// add triples to the output dataset
 		for (URI predicate : predicates) {
 			for (Value object : objects) {
 				addTriplesToTheOutputDataset(sesameDataSet, subject, predicate,
 						object, predicate_object_graphs);
 			}
 		}
 	}
 
 	/**
 	 * “Add triples to the output dataset” is a process that takes the following
 	 * inputs: Subject, an IRI or blank node or empty Predicate, an IRI or empty
 	 * Object, an RDF term or empty Target graphs, a set of zero or more IRIs
 	 * 
 	 * @param sesameDataSet
 	 * @param subject
 	 * @param predicate
 	 * @param object
 	 * @param targetGraphs
 	 */
 	private void addTriplesToTheOutputDataset(SesameDataSet sesameDataSet,
 			Resource subject, URI predicate, Value object, Set<URI> targetGraphs) {
 
 		// 1. If Subject, Predicate or Object is empty, then abort these steps.
 		if (subject == null || predicate == null || object == null)
 			return;
 		// 2. Otherwise, generate an RDF triple <Subject, Predicate, Object>
 		Statement triple = null;
 		if (targetGraphs.isEmpty()) {
 			// add the triple to the default graph of the output dataset.
 			triple = vf.createStatement(subject, predicate, object);
 			sesameDataSet.addStatement(triple);
 			log.debug("[R2RMLEngine:addStatement] Added new statement : " + triple);
 		}
 		for (URI targetGraph : targetGraphs) {
 			if (targetGraph.stringValue().equals(
 					R2RMLVocabulary.R2RML_NAMESPACE + R2RMLTerm.DEFAULT_GRAPH)) {
 				// 3. If the set of target graphs includes rr:defaultGraph,
 				// add the triple to the default graph of the output dataset.
 				triple = vf.createStatement(subject, predicate, object);
 			} else {
 				// 4. For each IRI in the set of target graphs that is not equal
 				// to rr:defaultGraph,
 				// add the triple to a named graph of that name in the output
 				// dataset.
 				triple = vf.createStatement(subject, predicate, object,
 						targetGraph);
 			}
 			sesameDataSet.addStatement(triple);
 			log.debug("[R2RMLEngine:addStatement] Added new statement : " + triple);
 		}
 	}
 
 	private Map<ColumnIdentifier, byte[]> applyValueToRow(TermMap tm) throws SQLException {
 		Map<ColumnIdentifier, byte[]> result = new HashMap<ColumnIdentifier, byte[]>();
 		Set<ColumnIdentifier> columns = tm.getReferencedColumns();
 		for (ColumnIdentifier column : columns) {
 		    log.debug("[R2RMLEngine:applyValueToRow] Iterate over : "
 				+ column);
 
 		    int i;
 		    final ResultSetMetaData metaData = rows.getMetaData();
 		    int n = metaData.getColumnCount();
 		    boolean found = false;
 		    for (i = 1; i <= n; i++) {
 			ColumnIdentifier cId = ColumnIdentifierImpl.buildFromJDBCResultSet(metaData, i);
 			if(cId.equals(column)) {
 				log.debug("[R2RMLEngine:applyValueToRow] Value found : "
 					+ rows.getString(i));
 				result.put(cId, rows.getBytes(i));
 				found = true;
 				break;
 			}
 		    }
 		    if (!found)
 			// Second chance fails...
 			throw new SQLException("[R2RMLEngine:applyValueToRow] Unknown column : "
 								+ column);
 		}
 		return result;
 	}
 
 	/**
 	 * A term map is a function that generates an RDF term from a logical table
 	 * row. The result of that function can be: Empty – if any of the referenced
 	 * columns of the term map has a NULL value, An RDF term – the common case,
 	 * A data error.
 	 * 
 	 * The term generation rules, applied to a value, are described in this
 	 * algorithm.
 	 * 
 	 * @param termMap
 	 * @return
 	 * @throws R2RMLDataError
 	 * @throws SQLException
 	 */
 	private Value generateRDFTerm(TermMap termMap, String value)
 			throws R2RMLDataError, SQLException {
 		// 1. If value is NULL, then no RDF term is generated.
 		if (termMap == null)
 			return null;
 		switch (termMap.getTermType()) {
 		case IRI:
 			// 2. Otherwise, if the term map's term type is rr:IRI
 			URI iri = generateIRITermType(termMap, value);
 			log.debug("[R2RMLEngine:generateRDFTerm] Generated IRI RDF Term : "
 					+ iri);
 			return (Value) iri;
 
 		case BLANK_NODE:
 			// 3. Otherwise, if the term map's term type is rr:BlankNode
 			BNode bnode = generateBlankNodeTermType(termMap, value);
 			log.debug("[R2RMLEngine:generateRDFTerm] Generated Blank Node RDF Term : "
 					+ bnode);
 			return (Value) bnode;
 
 		case LITERAL:
 			// 4. Otherwise, if the term map's term type is rr:Literal
 			Value valueObj = generateLiteralTermType(termMap, value);
 			log.debug("[R2RMLEngine:generateRDFTerm] Generated Literal RDF Term : "
 					+ valueObj);
 			return valueObj;
 
 		default:
 			// Unknow Term type
 			throw new IllegalStateException(
 					"[R2RMLEngine:generateRDFTerm] Unkonw term type : no rule define for this case.");
 		}
 	}
 
 	private Value generateLiteralTermType(TermMap termMap, String value)
 			throws R2RMLDataError, SQLException {
 		// 1. If the term map has a specified language tag, then return a plain
 		// literal
 		// with that language tag and with the natural RDF lexical form
 		// corresponding to value.
 		if (termMap.getLanguageTag() != null) {
 			if (!RDFDataValidator.isValidLanguageTag(termMap.getLanguageTag()))
 				throw new R2RMLDataError(
 						"[R2RMLEngine:generateLiteralTermType] This language tag is not valid : "
 								+ value);
 			return vf.createLiteral(value, termMap.getLanguageTag());
 		} else if (termMap.getDataType() != null) {
 			// 2. Otherwise, if the term map has a non-empty specified datatype
 			// that is different from the natural RDF datatype corresponding to
 			// the term map's
 			// implicit SQL datatype,
 			// then return the datatype-override RDF literal corresponding to
 			// value and the specified datatype.
 			if (!RDFDataValidator.isValidDatatype(termMap.getDataType()
 					.getAbsoluteStringURI()))
 				throw new R2RMLDataError(
 				// If the typed literal is ill-typed, then a data error is
 				// raised.
 						"[R2RMLEngine:generateLiteralTermType] This datatype is not valid : "
 								+ value);
 			SQLType implicitDatatype = extractImplicitDatatype((ObjectMap) termMap);
 			// Convert implicit datatype into XSD
 			XSDType implicitXSDType = SQLToXMLS
 					.getEquivalentType(implicitDatatype);
 			if (implicitXSDType != termMap.getDataType()) {
 				// Type overidden
 				log.debug("[R2RMLEngine:generateLiteralTermType] Type will be overidden : "
 						+ termMap.getDataType() + " != " + implicitXSDType);
 
 			}
 			// Lexical RDF Natural form
 			// value =
 			// XSDLexicalTransformation.extractNaturalRDFFormFrom(termMap.getDataType(),
 			// value);
 			URI datatype = vf.createURI(termMap.getDataType()
 					.getAbsoluteStringURI());
 			return vf.createLiteral(value, datatype);
 
 		} else {
 			// 3. Otherwise, return the natural RDF literal corresponding to
 			// value.
 			return extractNaturalLiteralFormFrom(termMap, value);
 		}
 	}
 
 	private BNode generateBlankNodeTermType(TermMap termMap, String value) {
 		// 1. Return a blank node whose blank node identifier is
 		// the natural RDF lexical form corresponding to value. (Note: scope of
 		// blank nodes)
 		// In db2triples and contrary to the R2RML norm, we accepts
 		// auto-assignments of blank nodes.
 		if (value == null)
 			return vf.createBNode();
		else
			return vf.createBNode(value);
 	}
 
 	private URI generateIRITermType(TermMap termMap, String value)
 			throws R2RMLDataError, SQLException {
 		// 1. Let value be the natural RDF lexical form corresponding to value.
 		// 2. If value is a valid absolute IRI [RFC3987], then return an IRI
 		// generated from value.
 
 		if (RDFDataValidator.isValidURI(value)) {
 			URI result = vf.createURI(value);
 			log.debug("[R2RMLEngine:generateIRITermType] Valid generated IRI : "
 					+ value);
 			return result;
 		} else {
 			String prependedValue = baseIRI + value;
 			if (RDFDataValidator.isValidURI(prependedValue)) {
 				// Otherwise, prepend value with the base IRI. If the result is
 				// a valid absolute IRI [RFC3987], then return an IRI generated
 				// from the result.
 				log.debug("[R2RMLEngine:generateIRITermType] Valid generated IRI : "
 						+ prependedValue);
 				URI result = vf.createURI(prependedValue);
 				return result;
 			} else {
 				// 4. Otherwise, raise a data error.
 				throw new R2RMLDataError(
 						"[R2RMLEngine:generateIRITermType] This relative URI "
 								+ value + " or this absolute URI " + baseIRI
 								+ value + " is not valid.");
 			}
 		}
 	}
 
 	private SQLType extractImplicitDatatype(TermMap objectMap)
 			throws SQLException {
 		SQLType result = null;
 		if (objectMap.getTermMapType() != TermMapType.TEMPLATE_VALUED
 				&& objectMap.getTermType() == TermType.LITERAL
 				&& objectMap.getConstantValue() == null) {
 			for (int i = 1; i <= meta.getColumnCount(); i++) {
 				ColumnIdentifier referencedColumn = objectMap.getReferencedColumns()
 					.iterator().next();
 				ColumnIdentifier curCol = ColumnIdentifierImpl.buildFromJDBCResultSet(meta, i);
 			    	if(curCol.equals(referencedColumn)) {
 			    	    result = SQLType.toSQLType(meta.getColumnType(i));
 			    	    log.debug("[R2RMLEngine:extractImplicitDatatype] Extracted implicit datatype :  "
 								+ result);
 				}
 			}
 		}
 		return result;
 	}
 
 	/**
 	 * The natural RDF literal corresponding to a SQL data value is the result
 	 * of applying the following method.
 	 * 
 	 * @param termMap
 	 * @param value
 	 * @return
 	 * @throws SQLException
 	 * @throws R2RMLDataError
 	 */
 	private Value extractNaturalLiteralFormFrom(TermMap termMap, String value)
 			throws SQLException, R2RMLDataError {
 		// 1. Let dt be the SQL datatype of the SQL data value.
 		SQLType dt = extractImplicitDatatype(termMap);
 		// 2. If dt is a character string type, then the result is a plain
 		// literal without
 		// language tag whose lexical form is the SQL data value.
 		XSDType xsdType = SQLToXMLS.getEquivalentType(dt);
 		// Extract RDF natural lexical form
 		if (dt == null || dt.isStringType()) {
 			if (xsdType == null) {
 				// 4. Otherwise, the result is a plain literal without
 				// language
 				// tag whose lexical form is the SQL data value CAST TO
 				// STRING.
 
 				// CAST TO STRING is implicit is this treatment
 				return vf.createLiteral(value);
 			}
 			return vf.createLiteral(value);
 		} else if (xsdType != null) {
 			// 3. Otherwise, if dt is listed in the table below: The result
 			// is a
 			// typed literal whose datatype IRI is the IRI indicated in the
 			// RDF
 			return vf.createLiteral(value,
 					vf.createURI(xsdType.getAbsoluteStringURI()));
 		} else {
 			// 4. Otherwise, the result is a plain literal without language
 			// tag whose lexical form is the SQL data value CAST TO STRING.
 
 			// CAST TO STRING is implicit is this treatment
 			return vf.createLiteral(value);
 		}
 
 	}
 
 	/**
 	 * Construct logical table. Note : Run SQL Query against database calling
 	 * the method Connection.commit can close the ResultSet objects that have
 	 * been created during the current transaction. In some cases, however, this
 	 * may not be the desired behavior. The ResultSet property holdability gives
 	 * the application control over whether ResultSet objects (cursors) are
 	 * closed when commit is called.
 	 * 
 	 * @param triplesMap
 	 * @throws SQLException
 	 */
 	private ResultSet constructLogicalTable(TriplesMap triplesMap)
 			throws SQLException {
 		log.debug("[R2RMLEngine:constructLogicalTable] Run effective SQL Query : "
 				+ triplesMap.getLogicalTable().getEffectiveSQLQuery());
 		ResultSet rs = null;
 		java.sql.Statement s = conn.createStatement(
 				ResultSet.HOLD_CURSORS_OVER_COMMIT, ResultSet.CONCUR_READ_ONLY);
 		if (triplesMap.getLogicalTable().getEffectiveSQLQuery() != null) {
 
 			s.executeQuery(triplesMap.getLogicalTable().getEffectiveSQLQuery());
 			rs = s.getResultSet();
 			if (rs == null)
 				throw new IllegalStateException(
 						"[R2RMLEngine:constructLogicalTable] SQL request "
 								+ "failed : result of effective SQL query is null.");
 
 		} else {
 			throw new IllegalStateException(
 					"[R2RMLEngine:constructLogicalTable] No effective SQL query has been found.");
 		}
 		// Commit to held logical table (read-only)
 		conn.setAutoCommit(false);
 		conn.commit();
 		return rs;
 	}
 
 	private ResultSet constructJointTable(ReferencingObjectMap refObjectMap)
 			throws SQLException {
 		log.debug("[R2RMLEngine:constructJointTable] Run joint SQL Query : "
 				+ refObjectMap.getJointSQLQuery());
 		ResultSet rs = null;
 		java.sql.Statement s = conn.createStatement(
 				ResultSet.HOLD_CURSORS_OVER_COMMIT, ResultSet.CONCUR_READ_ONLY);
 		if (refObjectMap.getJointSQLQuery() != null) {
 			s.executeQuery(refObjectMap.getJointSQLQuery());
 			rs = s.getResultSet();
 			if (rs == null)
 				throw new IllegalStateException(
 						"[R2RMLEngine:constructJointTable] SQL request "
 								+ "failed : result of effective SQL query is null.");
 		} else {
 			throw new IllegalStateException(
 					"[R2RMLEngine:constructJointTable] No effective SQL query has been found.");
 		}
 		return rs;
 	}
 
 	private ResultSet constructInversionTable(String instantiation,
 			String effectiveSQLQuery) throws SQLException {
 		String sqlQuery = "SELECT * FROM (" + effectiveSQLQuery
 				+ ") AS tmp WHERE " + instantiation + ";";
 		log.debug("[R2RMLEngine:constructInversionTable] Run inversion SQL Query : "
 				+ sqlQuery);
 		ResultSet rs = null;
 		java.sql.Statement s = conn.createStatement(
 				ResultSet.HOLD_CURSORS_OVER_COMMIT, ResultSet.CONCUR_READ_ONLY);
 		s.executeQuery(sqlQuery);
 		rs = s.getResultSet();
 		if (rs == null)
 			throw new IllegalStateException(
 					"[R2RMLEngine:constructInversionTable] SQL request "
 							+ "failed : result of effective SQL query is null.");
 		return rs;
 	}
 }
