 /**
  * 
  */
 package org.geworkbench.bison.datastructure.biocollections;
 
 import java.io.File;
 import java.io.Serializable;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 import java.util.TreeSet;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.geworkbench.bison.datastructure.bioobjects.markers.goterms.GOTerm;
 import org.geworkbench.bison.datastructure.bioobjects.markers.goterms.GeneOntologyTree;
import org.geworkbench.bison.datastructure.bioobjects.microarray.CSMicroarray;
 
 /**
  * Go Terms Analysis Result.
  * interface DSAncillaryDataSet is the minimal requirement to fit in to geWorkbench analysis's result output framework
  * 
  * wrapping for the result from ontologizer 2.0.
  * 
  * @author zji
  * @version $Id$
  */
public class GoAnalysisResult extends CSAncillaryDataSet<CSMicroarray> {
 	private static final long serialVersionUID = -337000604982427702L;
 	static Log log = LogFactory.getLog(GoAnalysisResult.class);
 	
 	Set<String> changedGenes;
 	Set<String> referenceGenes;
 
 	static class ResultRow implements Serializable {
 		private static final long serialVersionUID = 8340126713281389148L;
 		
 		String name;
 		String namespace;
 		double p;
 		double pAdjusted;
 		int popCount;
 		int studyCount;
 		
 		ResultRow(String name, String namespace, double p, double pAdjusted, int popCount, int studyCount) {
 			this.name = name;
 			this.namespace = namespace;
 			this.p = p;
 			this.pAdjusted = pAdjusted;
 			this.popCount = popCount;
 			this.studyCount = studyCount;
 		}
 		
 		public String toString() {
 			return name+"|"+namespace+"|"+p+"|"+pAdjusted+"|"+popCount+"|"+studyCount;
 		}
 	}
 
 	ResultRow getRow(Integer goId) {
 		return result.get(goId);
 	}
 	
 	private Map<Integer, ResultRow> result = null;
 	
 	Map<Integer, ResultRow> getResult() {return result; }
 
 	public int getCount() {
 		if(result!=null)
 			return result.size();
 		else return 0;
 	}
 
 	/**
 	 * Constructor for a result not populated yet.
 	 * @param parent
 	 * @param label
 	 */
	public GoAnalysisResult(DSDataSet<CSMicroarray> parent, String label) {
 		super(parent, label);
 		referenceGenes = new HashSet<String>();
 		changedGenes = new HashSet<String>();
 
 		result = new HashMap<Integer, ResultRow>();
 	}
 
 	void addResultRow(int goId, ResultRow row) {
 		result.put(goId, row);
 	}
 	
 	public File getDataSetFile() {
 		// no use. required by the interface
 		return null;
 	}
 
 	public void setDataSetFile(File file) {
 		// no use. required by the interface
 	}
 	
 	/**
 	 * return list of child IDs for a GO term ID
 	 * @param goTermId
 	 * @return
 	 */
 	static public List<Integer> getOntologyChildren(int goTermId) {
 		List<Integer> list = new ArrayList<Integer>();
 		
 		if(goTermId==0) {
 			for(Integer id: getNamespaceIds()) {
 				list.add(id);
 			}
 			return list;
 		}
 
 		GeneOntologyTree geneOntologyTree = GeneOntologyTree.getInstanceUntilAvailable();
 		for(GOTerm g: geneOntologyTree.getTerm(goTermId).getChildren()) {
 			list.add(g.getId());
 		}
 		return list;
 	}
 	
 	/**
 	 * return GO term name for a given GO term ID
 	 * 
 	 */
 	static public String getGoTermName(int goTermId) {
 		GeneOntologyTree geneOntologyTree = GeneOntologyTree.getInstanceUntilAvailable();
 		GOTerm goTerm = geneOntologyTree.getTerm(goTermId);
 		if(goTerm!=null)
 			return goTerm.getName();
 		else
 			return null;
 	}
 	
 	static public  Set<Integer>  getNamespaceIds() {
 		if(namespaceIds.size()>0) {
 			return namespaceIds;
 		} else {
 			GeneOntologyTree geneOntologyTree = GeneOntologyTree.getInstanceUntilAvailable();
 			for(int i=0; i<geneOntologyTree.getNumberOfRoots(); i++)
 				namespaceIds.add(geneOntologyTree.getRoot(i).getId());
 			return namespaceIds;
 		}
 	}
 
 	private HashMap<Integer, Set<String>> term2Gene = new HashMap<Integer, Set<String> >();
 	private static Set<Integer> namespaceIds = new TreeSet<Integer>();
 	
 	private static final Set<String> namespace;
 	static {
 		namespace = new TreeSet<String>();
 		namespace.add("molecular_function");
 		namespace.add("biological_process");
 		namespace.add("cellular_component");
 	};
 
 	// used by GoAnalysis to build up the result object
 	public void addChangedGenes(String gene) {
 		changedGenes.add(gene);
 	}
 
 	// used by GoAnalysis to build up the result object
 	public void addReferenceGenes(String gene) {
 		referenceGenes.add(gene);
 	}
 
 	// used by GoAnalysis to build up the result object
 	public void addResultRow(int termId, String termName, String namespace, double p, double adjustedP, int popCount, int studyCount) {
 		if(termId==0)return;
 		
 		ResultRow row = new ResultRow(termName, namespace, p, adjustedP, popCount, studyCount);
 		result.put(termId, row);
 	}
 	
 	public String getRowAsString(int goId) {
 		GoAnalysisResult.ResultRow row = getRow(goId);
 		if(row==null)return null;
 		
 		return row.name+" ("+row.studyCount+"/"+row.popCount+") ("+row.pAdjusted+")";
 	}
 	
 	public Set<String> getChangedGenes() {
 		return changedGenes;
 	}
 
 	public Set<String> getReferenceGenes() {
 		return referenceGenes;
 	}
 	
 	public Object[][] getResultAsArray() {
 		int rowCount = getCount();
 		List<Object[]> rows = new ArrayList<Object[]>();
 		
 		for(Integer goId: result.keySet()) {
 			GoAnalysisResult.ResultRow resultRow = getRow(goId);
 			Object[] array = new Object[COLUMN_COUNT];
 			array[TABLE_COLUMN_INDEX_GO_ID] = goId;
 			array[TABLE_COLUMN_INDEX_GO_TERM_NAME] = resultRow.name;
 			array[TABLE_COLUMN_INDEX_NAMESPACE] = resultRow.namespace;
 			array[TABLE_COLUMN_INDEX_P] = resultRow.p;
 			array[TABLE_COLUMN_INDEX_ADJUSTED_P] = resultRow.pAdjusted;
 			array[TABLE_COLUMN_INDEX_POP_COUNT] = resultRow.popCount;
 			array[TABLE_COLUMN_INDEX_STUDY_COUNT] = resultRow.studyCount;
 			rows.add(array);
 		}
 		log.debug("total rows: "+rowCount);
 		Collections.sort(rows, new AdjustedPComparator());
 		int row = 0;
 		Object[][] data = new Object[rowCount][COLUMN_COUNT];
 		for(Object[] array: rows) {
 			data[row] = array;
 			row++;
 		}
 		return data;
 	}
 
 	// only used for getRwoAsArray
 	private static final int COLUMN_COUNT = 7;
 	private static final int TABLE_COLUMN_INDEX_GO_ID = 0;
 	private static final int TABLE_COLUMN_INDEX_GO_TERM_NAME = 1;
 	private static final int TABLE_COLUMN_INDEX_NAMESPACE = 2;
 	private static final int TABLE_COLUMN_INDEX_P = 3;
 	private static final int TABLE_COLUMN_INDEX_ADJUSTED_P = 4;
 	private static final int TABLE_COLUMN_INDEX_POP_COUNT = 5;
 	private static final int TABLE_COLUMN_INDEX_STUDY_COUNT = 6;
 	
 	private static class AdjustedPComparator implements Comparator<Object[]> {
 
 		public int compare(Object[] o1, Object[] o2) {
 			Double d1 = (Double) o1[TABLE_COLUMN_INDEX_ADJUSTED_P];
 			Double d2 = (Double) o2[TABLE_COLUMN_INDEX_ADJUSTED_P];
 			if(d1<d2)return -1;
 			else if(d1>d2)return 1;
 			else return 0;
 		}
 	}
 
 	public static void parseOboFile(String oboFilename2) {
 		// TODO place holder for future feature of multiple gene ontology trees
 	}
 
 	public void setTerm2Gene(HashMap<Integer, Set<String>> term2Gene2) {
 		term2Gene = term2Gene2;
 	}
 	
 	public Set<String> getAnnotatedGenes(int goTermId) { return term2Gene.get(goTermId); }
 }
