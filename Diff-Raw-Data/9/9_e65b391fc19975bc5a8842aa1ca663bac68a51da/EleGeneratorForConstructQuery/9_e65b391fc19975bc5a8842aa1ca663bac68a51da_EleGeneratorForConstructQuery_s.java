 package eu.play_project.play_platformservices_querydispatcher.epsparql.visitor.realtime;
 
 import static eu.play_project.play_platformservices_querydispatcher.epsparql.visitor.realtime.CentralCounter.getCentralCounter;
 
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 
 import com.hp.hpl.jena.graph.Node;
 import com.hp.hpl.jena.graph.Node_ANY;
 import com.hp.hpl.jena.graph.Triple;
 import com.hp.hpl.jena.query.Query;
 import com.hp.hpl.jena.sparql.syntax.Element;
 import com.hp.hpl.jena.sparql.syntax.ElementEventBinOperator;
 import com.hp.hpl.jena.sparql.syntax.ElementEventGraph;
 
 import eu.play_project.play_platformservices.QueryTemplateImpl;
 import eu.play_project.play_platformservices.api.QueryTemplate;
 import eu.play_project.play_platformservices_querydispatcher.AgregatedVariableTypes;
 import eu.play_project.play_platformservices_querydispatcher.Variable;
 import eu.play_project.play_platformservices_querydispatcher.AgregatedVariableTypes.AgregatedEventType;
 import eu.play_project.play_platformservices_querydispatcher.api.EleGenerator;
 import eu.play_project.play_platformservices_querydispatcher.epsparql.visitor.VariableVisitor;
 import eu.play_project.querydispatcher.epsparql.Test.helpers.GenerateConstructResultVisitor;
 import fr.inria.eventcloud.api.Quadruple;
 
 public class EleGeneratorForConstructQuery implements EleGenerator {
 	private Query inputQuery;
 	private String elePattern;
 	private Element currentElement = null;
 	private CentralCounter centralCounter;
 	private Iterator<Element> eventQueryIter;
 	private EventTypeVisitor eventTypeVisitor;
 	private BinOperatorVisitor binOperatorVisitor;
 	private FilterExpressionCodeGenerator filterExpressionVisitor;
 	private TriplestoreQueryVisitor triplestoreQueryVisitor;
 	private Iterator<ElementEventBinOperator> binOperatorIter;
 	private SimpleEventPatternVisitor simpleEventPatternVisitor;
 	private Node graph = Node.createURI("urn:placeholder");
 	private String patternId;
 	
 	//Helper methods.
 	private Map<String, AgregatedEventType> variableAgregatedType;
 	private QueryTemplate queryTemplate;
 
 	@Override
 	public void generateQuery(Query inQuery) {
 		//Detect eventtypes
 		variableAgregatedType = new AgregatedVariableTypes().detectType(inQuery);
 
 		elePattern = "";
 		this.inputQuery = inQuery;
 		eventQueryIter = inQuery.getEventQuery().iterator();
 		binOperatorIter = inQuery.getEventBinOperator().iterator();
 		simpleEventPatternVisitor =  new SimpleEventPatternVisitor();
 		centralCounter = getCentralCounter();
 		eventTypeVisitor = new EventTypeVisitor();
 		triplestoreQueryVisitor = new TriplestoreQueryVisitor(centralCounter);
 		filterExpressionVisitor = new FilterExpressionCodeGenerator();
 		binOperatorVisitor =  new BinOperatorVisitor();
 		
 		//queryTemplate = new QueryTemplate(); FIXME
 		
 		ElePattern();
 	}
 	
 	private void ElePattern(){
 		Complex();
 		elePattern += "<-";
 		SimpleEventPattern();
 		
 		while(binOperatorIter.hasNext()){
 			binOperatorIter.next().visit(binOperatorVisitor);
 			elePattern += binOperatorVisitor.getBinOperator();
 			SimpleEventPattern();
 		}
 	}
 	
 	private void Complex() {
 		elePattern += "complex(" + centralCounter.getNextCeid() + "," + patternId + ") do (";
 		GenerateConstructResult();
 		SaveSharedVariabelValues();
 		//DecrementReferenceCounter();
 		elePattern += ")";
 	}
 
 	private void GenerateConstructResult() {
 		GenerateConstructResultVisitor generateConstructResultVisitor = new GenerateConstructResultVisitor();
 		Iterator<Triple> iter = inputQuery.getConstructTemplate().getTriples()
 				.iterator();
 		Triple triple;
 		while (iter.hasNext()) {
 			triple = iter.next();
 			if (!containsSharedVariablesTest(triple)) {
 				elePattern += "generateConstructResult("
 						+ triple.getSubject().visitWith(
 								generateConstructResultVisitor)
 						+ ","
 						+ triple.getPredicate().visitWith(
 								generateConstructResultVisitor)
 						+ ","
 						+ triple.getObject().visitWith(
 								generateConstructResultVisitor) + ","
 						+ centralCounter.getCeid() + ")";
 				if (iter.hasNext()) {
 					elePattern += ",";
 				}
 				//Use template
 			}else{
 				QueryTemplateImpl queryTemplate = new QueryTemplateImpl();
 				GenerateConstructResulTemplatetVisitor gtv =  new GenerateConstructResulTemplatetVisitor();
 				Node subject, predicate, object;
 				
 				gtv =  new GenerateConstructResulTemplatetVisitor();
 				triple.getSubject().visitWith(gtv);
 				subject = gtv.getTemplate().getSubject();
 				
 				gtv =  new GenerateConstructResulTemplatetVisitor();
 				triple.getPredicate().visitWith(gtv);
 				predicate = gtv.getTemplate().getPredicate();
 				
 				gtv =  new GenerateConstructResulTemplatetVisitor();
 				triple.getObject().visitWith(gtv);
 				object = gtv.getTemplate().getObject();
 				
 				queryTemplate.appendLine(new Quadruple(graph, subject, predicate, object));
 			}
 		}
 	}
 
 	private boolean containsSharedVariablesTest(Triple triple){
 		boolean result = false;
 		if(triple.getSubject().isVariable()){
 			if(variableAgregatedType.keySet().contains(triple.getSubject().toString().substring(1))){
 				if(variableAgregatedType.get(triple.getSubject().toString().substring(1)).equals(AgregatedEventType.CRH) || variableAgregatedType.get(triple.getSubject().toString().substring(1)).equals(AgregatedEventType.RH)){
 					result = true;
 				}
 			}
 
 		}else if(triple.getPredicate().isVariable()){
 			if(variableAgregatedType.keySet().contains(triple.getPredicate().toString().substring(1))){
 				if(variableAgregatedType.get(triple.getPredicate().toString().substring(1)).equals(AgregatedEventType.CRH) || variableAgregatedType.get(triple.getPredicate().toString().substring(1)).equals(AgregatedEventType.RH)){
 					result = true;
 				}
 			}
 		}else if(triple.getObject().isVariable()){
 			if(variableAgregatedType.keySet().contains(triple.getObject().toString().substring(1))){
 				if(variableAgregatedType.get(triple.getObject().toString().substring(1)).equals(AgregatedEventType.CRH) || variableAgregatedType.get(triple.getObject().toString().substring(1)).equals(AgregatedEventType.RH)){
 					result = true;
 				}
 			}
 		}
 		return result;
 	}
 		
 	public void SaveSharedVariabelValues() {
 		StringBuffer tmpEle = new StringBuffer();
 
 		Iterator<String> iter = variableAgregatedType.keySet().iterator();
 
 		while (iter.hasNext()) {
 			String key = iter.next();
 
 			// Check if variable is in real time and historical part.
 			if (variableAgregatedType.get(key).equals(AgregatedEventType.RH) || variableAgregatedType.get(key).equals(AgregatedEventType.CRH)) {
 				if (!elePattern.endsWith(",")) {
 					elePattern += ",";
 				}
 				tmpEle.append("variabeValuesAdd(" + patternId + ",'" + key + "'," + "V" + key + ")");
 			}
 
 		}
 
 		elePattern += tmpEle.toString();
 	}
 	
 	private void DecrementReferenceCounter(){
 		//TODO sobermeier do it for all triples
 		elePattern +=  ",decrementReferenceCounter(ViD1)";
 	}
 	private void SimpleEventPattern() {
 	
 		elePattern += "(";
 		currentElement = eventQueryIter.next();
 		currentElement.visit(eventTypeVisitor);
 		elePattern += eventTypeVisitor.getEventType();
 		elePattern += "(";
 		String triplestoreVariable = centralCounter.getNextTriplestoreVariable();
 		elePattern += triplestoreVariable;
 		elePattern += ") 'WHERE' (";
 		AdditionalConditions();
 		elePattern += "))";
 	}
 	
 	private void AdditionalConditions(){
 		TriplestoreQuery();
 		FilterExpression();
 		ReferenceCounter();
		elePattern += ", ";
 		//PerformanceMeasurement();
 		
 		if(!binOperatorIter.hasNext()){
 			elePattern += ",";
 			GenerateCEID();
 		}
 	}
 	
 	private void ReferenceCounter(){
 		elePattern += " incrementReferenceCounter(" + centralCounter.getTriplestoreVariable() + ")";
 	}
 	
 	private void PerformanceMeasurement(){
 		elePattern += "measure(" +  patternId + ")";
 	}
 	
 	private void TriplestoreQuery(){
 		currentElement.visit(triplestoreQueryVisitor);
 		elePattern += triplestoreQueryVisitor.getTriplestoreQueryGraphTerms();
 	}
 
 	private void FilterExpression(){
 		filterExpressionVisitor.startVisit(((ElementEventGraph)currentElement).getFilterExp());
 		if(!elePattern.endsWith(",") && !filterExpressionVisitor.getEle().equals("")){ // This filter is optional. No value needed.
 			elePattern += "," + filterExpressionVisitor.getEle();
 		}else if(!filterExpressionVisitor.getEle().equals("")){
 			elePattern += filterExpressionVisitor.getEle();
 		}
 	}
 		
 	private void GenerateCEID(){
 		elePattern += "random(1000000, 9000000, " + centralCounter.getCeid() + ")";
 	}
 
 	@Override
 	public String getEle() {
 		return elePattern;
 	}
 
 	@Override
 	public ArrayList<String[]> getEventProperties() {
 		return null;
 	}
 	
 	@Override
 	public void setPatternId(String patternId) {
 		// Add escape sequence for prolog
 		String tmp= patternId.replace(":", "\\:");
 		this.patternId = tmp.replace(".", "\\.");
 	}
 
 	@Override
 	public QueryTemplate getQueryTemplate() {
 		
 		return this.queryTemplate;
 	}
 }
