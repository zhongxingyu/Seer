 package org.biosemantics.conceptstore.dataimport;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Map;
 import java.util.Set;
 
 import org.apache.commons.io.FileUtils;
 import org.apache.commons.io.LineIterator;
 import org.biosemantics.conceptstore.domain.impl.ConceptType;
 import org.biosemantics.conceptstore.domain.impl.RlspType;
 import org.neo4j.graphdb.DynamicRelationshipType;
 import org.neo4j.unsafe.batchinsert.BatchInserterIndex;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 public class PubmedDataImport implements DataImport {
 	public PubmedDataImport(DataImportUtility dataImportUtility, BatchInserterIndex labelIndex, File inputFile) {
 		this.dataImportUtility = dataImportUtility;
 		this.inputFile = inputFile;
 		this.labelIndex = labelIndex;
 	}
 
 	@Override
 	public void importData() throws Exception {
 		importPredicates();
 		importRelationships();
 	}
 
	private void importRelationships() throws IOException {
 		LineIterator iterator = FileUtils.lineIterator(inputFile, "UTF-8");
 		try {
 			Map<String, Object> props = new HashMap<String, Object>();
 			int addedCounter = 0;
 			while (iterator.hasNext()) {
 				String line = iterator.nextLine();
 				String[] columns = line.split(" ");
 				if (columns.length == 4) {
 					String predicate = columns[1].trim();
 					predicates.add(predicate);
 				}
 			}
 			for (String predLblTxt : predicates) {
 				Long labelNode = dataImportUtility.getLabelNode(predLblTxt, ENG);
 				Long conceptNode = null;
 				if (labelNode == null) {
 					labelNode = dataImportUtility.createLabelNode(predLblTxt, ENG, props);
 					conceptNode = dataImportUtility.createConceptNode(ConceptType.PREDICATE.toString(), props);
 					dataImportUtility.createRelationship(conceptNode, labelNode, RlspType.HAS_LABEL, props);
 					addedCounter++;
 				} else {
 					conceptNode = dataImportUtility.getConceptNodeForLabelNode(labelNode);
 				}
 				predicateNodeMap.put(predLblTxt, conceptNode);
 			}
 			logger.info("{} predicates were added", addedCounter);
 
 		} finally {
 			LineIterator.closeQuietly(iterator);
 			labelIndex.flush();
 		}
 
 	}
 
	private void importPredicates() throws IOException {
 		LineIterator iterator = FileUtils.lineIterator(inputFile, "UTF-8");
 		Set<String> missingCuis = new HashSet<String>();
 		int ctr = 0;
 		try {
 			Map<String, Object> props = new HashMap<String, Object>();
 			while (iterator.hasNext()) {
 				String line = iterator.nextLine();
 				String[] columns = line.split(" ");
 				if (columns.length == 4) {
 					String srcCui = columns[0].trim();
 					String predicateText = columns[1].trim();
 					String tgtCui = columns[2].trim();
 					String pmid = columns[3].trim();
 					Long srcNotationNode = dataImportUtility.getNotationNode(srcCui);
 					if (srcNotationNode == null) {
 						missingCuis.add(srcCui);
 						continue;
 					}
 					Long tgtNotationNode = dataImportUtility.getNotationNode(tgtCui);
 					if (tgtNotationNode == null) {
 						missingCuis.add(tgtCui);
 						continue;
 					}
 					Long predicateConceptNode = predicateNodeMap.get(predicateText);
 					Long srcConceptNode = dataImportUtility.getConceptNodeForNotationNode(srcNotationNode);
 					Long tgtConceptNode = dataImportUtility.getConceptNodeForNotationNode(tgtNotationNode);
 					props.put("sources", new String[] { pmid });
 					dataImportUtility.createRelationship(srcConceptNode, tgtConceptNode,
 							DynamicRelationshipType.withName(predicateConceptNode.toString()), props);
 					if (++ctr % 10000 == 0) {
 						logger.debug("{}", ctr);
 					}
 				}
 			}
 		} finally {
 			LineIterator.closeQuietly(iterator);
 		}
 		logger.info("{} lines iterated", ctr);
 		logger.info("MISSING CUIS = {}", missingCuis);
 	}
 
 	private File inputFile;
 	private Set<String> predicates = new HashSet<String>();
 	private DataImportUtility dataImportUtility;
 	private BatchInserterIndex labelIndex;
 	private Map<String, Long> predicateNodeMap = new HashMap<String, Long>();
 
 	private static final String ENG = "ENG";
 	private static final Logger logger = LoggerFactory.getLogger(PubmedDataImport.class);
 
 }
