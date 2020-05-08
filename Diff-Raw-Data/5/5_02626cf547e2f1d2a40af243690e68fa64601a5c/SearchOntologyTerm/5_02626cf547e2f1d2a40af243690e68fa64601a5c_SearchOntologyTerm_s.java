 package edu.uga.radiant.ajax;
 
 import java.util.Iterator;
 import java.util.Map;
 
 import org.apache.log4j.Logger;
 import org.semanticweb.owlapi.model.OWLClass;
 
 import com.opensymphony.xwork2.ActionContext;
 import com.opensymphony.xwork2.ActionSupport;
 
 import edu.uga.radiant.ontology.OntologyManager;
 import edu.uga.radiant.ontology.SearchOntology;
 import edu.uga.radiant.printTree.LoadOWLTree;
 import edu.uga.radiant.util.RadiantToolConfig;
 import edu.uga.radiant.util.SortValueMap;
 
 public class SearchOntologyTerm extends ActionSupport {
 	
 	/**
 	 * 
 	 */
 	private static final long serialVersionUID = 1L;
 	private String term;
 	private String errormsg;
 	private String innerHtml;
 	
 	public String execute() {
 		
 	    Logger logger = RadiantToolConfig.getLogger();
 		errormsg = "";
 		
 		@SuppressWarnings("rawtypes")
 		Map session = ActionContext.getContext().getSession();
 		OntologyManager mgr = (OntologyManager) session.get("OntologyManager");
 		
 		StringBuffer buf = new StringBuffer();
 		SearchOntology search = new SearchOntology(mgr);
 		
 		
 		logger.debug("term = " + term);
 		SortValueMap<String, Double> searchTerms = search.search(term);
 		
 		
 		logger.debug("searchTerms size = " + searchTerms.size());
 		
 	    Iterator<String> it = searchTerms.keySet().iterator();
	    buf.append("<ol id='searchReslutSelectable'>");
 	    int i = 0;
 	    while(it.hasNext() && i < 20){
 	        String iri = it.next();
 	        OWLClass cls = mgr.getConceptClass(iri);
 	        String definition = mgr.getClassDefinition(cls);
 	        String label = mgr.getClassLabel(cls);
 	        String fragmentData = LoadOWLTree.charReplace(iri);
 	        String score = searchTerms.get(iri).toString();
 	        if (score.length() > 6) score = score.substring(0, 6); 
	        buf.append("<li data='" + fragmentData + "' class=\"ui-widget-content ontologySearchResullts\" style=\"margin:6px;padding:2px;\"><span value=\"" + fragmentData + "\" style='width:50%;float:left'><b>" + label + " : " + score + "</b></span><br/><" + iri + ">" + definition + "</li>");        
 	        i++;
 	    }
 	    buf.append("<ol>");
 	    innerHtml = buf.toString();
 	    
 	    return SUCCESS;
 	}
 
 	public void setErrormsg(String errormsg) {
 		this.errormsg = errormsg;
 	}
 
 	public String getErrormsg() {
 		return errormsg;
 	}
 
 	public void setInnerHtml(String innerHtml) {
 		this.innerHtml = innerHtml;
 	}
 
 	public String getInnerHtml() {
 		return innerHtml;
 	}
 
 	public void setTerm(String term) {
 		this.term = term;
 	}
 
 	public String getTerm() {
 		return term;
 	}
 
 }
