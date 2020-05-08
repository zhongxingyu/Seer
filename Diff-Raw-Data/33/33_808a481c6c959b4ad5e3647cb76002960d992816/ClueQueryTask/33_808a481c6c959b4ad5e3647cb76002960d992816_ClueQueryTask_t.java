 /**
  * 
  */
 package app;
 
 import java.util.ArrayList;
 
 import javax.swing.SwingWorker;
 
 import org.apache.log4j.Logger;
 
 import lombok.AccessLevel;
 import lombok.Getter;
 import lombok.Setter;
 
 import com.hp.hpl.jena.query.Query;
 import com.hp.hpl.jena.query.QueryExecution;
 import com.hp.hpl.jena.query.QueryExecutionFactory;
 import com.hp.hpl.jena.query.QueryFactory;
 
 import com.hp.hpl.jena.rdf.model.InfModel;
 import com.hp.hpl.jena.rdf.model.Literal;
 import com.hp.hpl.jena.rdf.model.LiteralRequiredException;
 import com.hp.hpl.jena.rdf.model.Model;
 import com.hp.hpl.jena.rdf.model.ModelFactory;
 import com.hp.hpl.jena.rdf.model.Property;
 import com.hp.hpl.jena.rdf.model.RDFNode;
 import com.hp.hpl.jena.rdf.model.Resource;
 import com.hp.hpl.jena.rdf.model.Selector;
 import com.hp.hpl.jena.rdf.model.SimpleSelector;
 import com.hp.hpl.jena.rdf.model.Statement;
 import com.hp.hpl.jena.rdf.model.StmtIterator;
 import com.hp.hpl.jena.reasoner.Reasoner;
 import com.hp.hpl.jena.reasoner.ReasonerRegistry;
 
 import com.hp.hpl.jena.vocabulary.RDFS;
 
 import com.hp.hpl.jena.sparql.engine.http.QueryExceptionHTTP;
 
 import exception.NoResourcesSelectedException;
 import framework.Clue;
 import framework.Pop;
 import framework.Solution;
 
 /**
  * @author Ben Griffiths
  *
  */
 
 public class ClueQueryTask extends SwingWorker<ArrayList<Solution>, Void> {
 	private static Logger log = Logger.getLogger(ClueQueryTask.class);
 	private final String ENDPOINT_URI = "http://dbpedia-live.openlinksw.com/sparql"; // http://dbpedia.org/sparql
 	
 	private final String DBPEDIA_PROPERTY_PREFIX_DECLARATION = "PREFIX dbpprop: <http://dbpedia.org/property/>"; // the 'old' property ontology
 	private final String RDFS_PREFIX_DECLARATION = "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>";
 	private final String RDF_PREFIX_DECLARATION = "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>";
 	private final String FOAF_PREFIX_DECLARATION = "PREFIX foaf: <http://xmlns.com/foaf/0.1/>";
 	
 	private final int LANGUAGE_TAG_LENGTH = 3;
 	private final String LANGUAGE_TAG = "@";
 	private final String ENG_LANG = "en";
 	@Getter(AccessLevel.PRIVATE) @Setter(AccessLevel.PRIVATE) private Model schema;
 	@Getter(AccessLevel.PRIVATE) @Setter(AccessLevel.PRIVATE) private Reasoner reasoner;
 	@Getter(AccessLevel.PRIVATE) @Setter(AccessLevel.PRIVATE) private InfModel infModel;
 	
 	@Getter(AccessLevel.PRIVATE) @Setter(AccessLevel.PRIVATE) private ArrayList<String> recognisedResourceUris;
 	
 	@Getter(AccessLevel.PRIVATE) @Setter(AccessLevel.PRIVATE) private ArrayList<Resource> extractedResources; // Resources whose labels have been extracted from DBpedia
 	
 	@Getter(AccessLevel.PUBLIC) @Setter(AccessLevel.PRIVATE) private  ArrayList<Solution> solutions; // list of candidate solutions wrapped in Solution objects
 	
 	@Getter(AccessLevel.PUBLIC) @Setter(AccessLevel.PRIVATE) private Clue clue;
 	@Getter(AccessLevel.PRIVATE) @Setter(AccessLevel.PRIVATE) private ArrayList<String> clueFragments;
 	
 	@Getter(AccessLevel.PRIVATE) @Setter(AccessLevel.PRIVATE) private Query query;
 	@Getter(AccessLevel.PRIVATE) @Setter(AccessLevel.PRIVATE) private QueryExecution queryExecution;
 	@Getter(AccessLevel.PRIVATE) @Setter(AccessLevel.PUBLIC) private String whereClause;
 	
 	public ClueQueryTask(Clue clue, ArrayList<String> clueFragments, ArrayList<String> recognisedResourceUris) {
 		super();
 		/*if(recognisedResourceUris == null || recognisedResourceUris.size() == 0)
 			throw new NoResourcesSelectedException();
 		*/
 		this.setClue(clue);
 		this.setClueFragments(clueFragments);
 		this.setRecognisedResourceUris(recognisedResourceUris);
 		this.setSolutions(new ArrayList<Solution>());
 		this.setExtractedResources(new ArrayList<Resource>());
 		this.setSchema(ModelLoader.getModel()); // retrieve a reference to the local ontology
 		Reasoner reasoner = ReasonerRegistry.getOWLMicroReasoner();
 	    this.setReasoner(reasoner.bindSchema(schema));
 	}
 	
 	@Override
 	protected ArrayList<Solution> doInBackground() throws Exception {
 		int progress = 0;
         this.setProgress(progress); // Initialise progress property of SwingWorker
  
         int combinedLengthOfQueries = this.getRecognisedResourceUris().size();
         
         int taskLength = 0;
         try {
         	taskLength = (100 / combinedLengthOfQueries);
         }
         catch(ArithmeticException e) { // will be thrown if combinedLengthOfQueries is 0
         	throw new NoResourcesSelectedException("No entities were recognised in the clue");
         }
         
         for(String resourceUri : this.getRecognisedResourceUris()) {
         	
 			Model data;
 			try {
 				data = this.constructModelFromRemoteStore(resourceUri); // Query DBpedia for triples that include this resource
 				this.setInfModel(ModelFactory.createInfModel(reasoner, data));
 			    this.extractCandidateSolutions(resourceUri); // adds any candidate solutions from the model to the solutions list
 			}
 			catch(QueryExceptionHTTP e) {
 				log.debug("Extraction of recognised resource <" + resourceUri + "> from DBpedia failed.");
 				log.debug(e.getResponseMessage());
 			}
 			
         	progress += taskLength;
             this.setProgress(progress); // one query has been completed
         }
         return this.getSolutions();
 	}
 	
     /*
      * Executed on EDT
      */
     @Override
     public void done() {
     	this.setProgress(100);
     }
     
     private Model constructModelFromRemoteStore(String resourceUri) throws QueryExceptionHTTP {
 		String sparqlQuery = RDFS_PREFIX_DECLARATION + " " +
 				RDF_PREFIX_DECLARATION + " " +
 				DBPEDIA_PROPERTY_PREFIX_DECLARATION +
 				" construct {<" + resourceUri + "> ?predicate ?object." +
 				" 			?object rdfs:label ?label." +
 				" 			?object dbpprop:name ?name." +
 				"			?object rdf:type ?objectType." +
 				"			?objectType rdfs:label ?objectTypeLabel." +
 				" 			?subject ?anotherPredicate <" + resourceUri + ">." +
 				"			?subject rdfs:label ?anotherLabel." +
 				"			?subject dbpprop:name ?anotherName." +
 				"			?subject rdf:type ?subjectType." +
 				"			?subjectType rdfs:label ?subjectTypeLabel." +
 				"}" +
 				" where {" +
 				" {<" + resourceUri + "> ?predicate ?object." +
 				"  ?object rdfs:label ?label." +
 				"  ?object rdf:type ?objectType." +
 				"  ?objectType rdfs:label ?objectTypeLabel.}" +
 				" UNION" +
 				" {<" + resourceUri + "> ?predicate ?object." +
 				"  ?object dbpprop:name ?name." +
 				"  ?object rdf:type ?objectType." +
 				"  ?objectType rdfs:label ?objectTypeLabel.}" +
 				" UNION" +
 				" {?subject ?anotherPredicate <" + resourceUri + ">." +
 				" ?subject rdfs:label ?anotherLabel. " +
 				" ?subject rdf:type ?subjectType." +
 				" ?subjectType rdfs:label ?subjectTypeLabel.}" +
 				" UNION" +
 				" {?subject ?anotherPredicate <" + resourceUri + ">." +
 				" ?subject dbpprop:name ?anotherName. " +
 				" ?subject rdf:type ?subjectType." +
 				" ?subjectType rdfs:label ?subjectTypeLabel.}" +
 				"}" +
 				"LIMIT 50000";
 		 
 		log.debug("Constructing model around " + resourceUri);
 		
 		Query query = QueryFactory.create(sparqlQuery);
 		QueryExecution queryExecution = QueryExecutionFactory.sparqlService(ENDPOINT_URI, query);
 		
 		Model model = queryExecution.execConstruct();
 		
 		queryExecution.close();
 		
 		/* Construct a second model to gather labels of the recognised resource */
 		String secondSparqlQuery = FOAF_PREFIX_DECLARATION + " " +
 				RDFS_PREFIX_DECLARATION + " " +
 				DBPEDIA_PROPERTY_PREFIX_DECLARATION +
 				" construct {<" + resourceUri + "> foaf:givenName ?givenName." +
 				" 		<" + resourceUri + "> foaf:surname ?surname." +
 				" 		<" + resourceUri + "> rdfs:label ?label." +
 				" 		<" + resourceUri + "> dbpprop:name ?name." +
 				" }" +
 				" where {" +
 				"	 {<" + resourceUri + "> foaf:givenName ?givenName.}" +
 				"	 UNION" +
 				"	 {<" + resourceUri + "> foaf:surname ?surname.}" +
 				"	 UNION" +
 				"	 {<" + resourceUri + "> rdfs:label ?label.}" +
 				"	 UNION" +
 				"	 {<" + resourceUri + "> dbpprop:name ?name.}" +
 				"}";
 		
 		Query secondQuery = QueryFactory.create(secondSparqlQuery);
 		QueryExecution secondQueryExecution = QueryExecutionFactory.sparqlService(ENDPOINT_URI, secondQuery);
 		
 		log.debug("Constructing second model around " + resourceUri);
 		
 		Model secondModel = secondQueryExecution.execConstruct();
 		
 		secondQueryExecution.close();
 		
 		Model mergedModel = model.union(secondModel);
 
 		return mergedModel;
 	}
 	
 	private void extractCandidateSolutions(String rootResourceUri) {
 		/* First, check the labels of the resource around which the model was constructed */
 		this.extractSolutionsFromRootResource(rootResourceUri);
 		
 		/* Now list statements from the model in which the predicate is a pop:relationalProperty */
 		Selector propertiesOfInterestSelector = new SimpleSelector(null, Pop.relationalProperty, (RDFNode)null);
 		StmtIterator statements = this.getInfModel().listStatements(propertiesOfInterestSelector);
 		
 		while(statements.hasNext()) {
 			Statement thisStatement = statements.nextStatement();
 			Resource subjectOfStatement = thisStatement.getSubject();
 			RDFNode objectOfStatement = thisStatement.getObject();
 
 			Selector selector = new CandidateSelector(subjectOfStatement, null, objectOfStatement);
 			
 			StmtIterator statementsOfInterest = this.getInfModel().listStatements(selector);
 			
 			while(statementsOfInterest.hasNext()) {
 				Statement statementOfInterest = statementsOfInterest.nextStatement();
 				Property thisPredicate = statementOfInterest.getPredicate();
 				
 				Resource thisPredicateInModel = this.getInfModel().getResource(thisPredicate.getURI());
 				
 				StmtIterator labelProperties = thisPredicateInModel.listProperties(RDFS.label);
 				
 				if(labelProperties != null) {
 					while(labelProperties.hasNext()) {
 						RDFNode predicateLabelValue = labelProperties.nextStatement().getObject();
 						String rawPredicateLabel = predicateLabelValue.toString();
 						String predicateLabel = stripLanguageTag(rawPredicateLabel);
 						if(this.getClueFragments().contains(toProperCase(predicateLabel))) {
 							Resource r = thisStatement.getResource();
 							RDFNode objectOfInterest = thisStatement.getObject();
 							/*
 							 * Either a string has been identified which may be a solution or a resource has been identified
 							 * whose label may represent a solution
 							 * *********** SHOULD I ALSO TEST THE LABELS OF SUBJECTS????????? ********************
 							 */
 							if(objectOfInterest.isLiteral())// a string has been identified which may be a solution
 								this.extractSolutionFromLiteral(r, objectOfInterest.asResource());								
 							else this.extractSolutionsFromSubjectAndObject(subjectOfStatement, objectOfStatement.asResource()); 
 						}
 					}
 				}
 			}
 		}
 	}
 	
 	private void extractSolutionsFromRootResource(String rootResourceUri) {
 		Resource rootResource = this.getInfModel().getResource(rootResourceUri);
 		Selector rootResourceLabelSelector = new SimpleSelector(rootResource, RDFS.label, (RDFNode)null);
 		StmtIterator rootLabels = this.getInfModel().listStatements(rootResourceLabelSelector);
 		
 		/* Logging */
 		log.debug("rootResourceUri = " + rootResourceUri);
 		log.debug("rootResource is null = " + (rootResource == null));
 		log.debug("rootLabels.hasNext() = " + (rootLabels.hasNext()));
 		
 		while(rootLabels.hasNext()) {
 			Statement stmnt = rootLabels.nextStatement();
 			Literal rootLabelLiteral;
 			try {
 				rootLabelLiteral = stmnt.getLiteral();
 			}
 			catch(LiteralRequiredException e) {
 				continue;
 			}
 			
 			this.constructSolution(rootLabelLiteral.toString(), rootResource, rootResource);
 			log.debug("Found label of root resource: " + rootLabelLiteral.toString());
 		}
 	}
 	
 	private void extractSolutionFromLiteral(Resource resource, Resource literalResource) {
 		Resource clueResource, solutionResource;
 		if(this.getRecognisedResourceUris().contains(resource.getURI())) {
 			clueResource = resource;
 			solutionResource = literalResource;
 		}
 		else {
 			clueResource = literalResource;
 			solutionResource = resource;
 		}
 		
 		/* Trialling http://dbpedia.org/resource/ only... */
 		String solutionResourceNameSpace = solutionResource.getNameSpace();
 		String clueResourceNameSpace = clueResource.getNameSpace();
 		if(!solutionResourceNameSpace.contains("http://dbpedia.org/resource/"))
 			return;
 		if(!clueResourceNameSpace.contains("http://dbpedia.org/resource/"))
 			return;
 		
 		this.constructSolution(literalResource.toString(), solutionResource, clueResource);
 	}
 	
 	private void extractSolutionsFromSubjectAndObject(Resource subject, Resource object) {
 		Resource clueResource, solutionResource;
 		StmtIterator candidateLabels = object.listProperties(RDFS.label);
 		while(candidateLabels.hasNext()) {
 			Statement s = candidateLabels.nextStatement();
 			RDFNode candidateLabelValue = null;
 			
 			String lang = "LITERAL_REQUIRED_EXCEPTION"; // will remain with this value if a 
 			try {										// LiteralRequiredException is thrown	
 				lang = s.getLanguage(); // we only want English-language labels
 			}
 			catch(LiteralRequiredException e) {
 				/* The pop ontology treats some properties in the dbpprop namespace as subProperties
 				 * of rdfs:label, because they are often used in place of rdfs:label in the DBpedia
 				 * dataset. However, sometimes they are given resources as values. In such cases,
 				 * we ignore the resource and move on to the next value of rdfs:label
 				 */
 			}
 			if(lang == null || lang.equals(this.ENG_LANG)) {
 				if(candidateLabelValue == null)
 					candidateLabelValue = s.getObject();
 				String rawCandidateLabel = candidateLabelValue.toString();
 				String candidateLabel = stripLanguageTag(rawCandidateLabel);
 				
 				Resource res = s.getSubject();
 				
 				if(this.getRecognisedResourceUris().contains(res.getURI())) {
 					clueResource = res;
 					solutionResource = subject;
 				}
 				else {
 					clueResource = subject;
 					solutionResource = res;
 				}
 
 				/* We are only interested in resources in the namespace http://dbpedia.org/resource/ */
 				String solutionResourceNameSpace = solutionResource.getNameSpace();
 				String clueResourceNameSpace = clueResource.getNameSpace();
 				if(!solutionResourceNameSpace.contains("http://dbpedia.org/resource/"))
 					return;
 				if(!clueResourceNameSpace.contains("http://dbpedia.org/resource/"))
 					return;
 				
 				this.constructSolution(candidateLabel, solutionResource, clueResource);
 			}
 	}
 }
 	/**
 	 * constructSolution - instantiates a Solution object with the solutionText argument and, if it is not already contained 
 	 * in the solutions list, adds it to that list. If the solutionText contains any spaces, then it is fragmented into every
 	 * possible combination of sequences of sequential words within the text, and each fragment is used to create a further 
 	 * Solution object
 	 * @param solutionText
 	 * @param solutionResource
 	 * @param clueResource
 	 */
 	private void constructSolution(String solutionText, Resource solutionResource, Resource clueResource) {
 		ArrayList<String> potentialSolutions = new ArrayList<String>();
 		potentialSolutions.add(solutionText);
 		
 		String[] solutionTextFragments = solutionText.split(" ");
 		for(int i = 0; i < solutionTextFragments.length; i++) {
 			String thisFragment = solutionTextFragments[i];
 			potentialSolutions.add(thisFragment);
 			for(int j = i + 1; j < solutionTextFragments.length; j++) {
 				thisFragment = thisFragment + " " + solutionTextFragments[j];
 				potentialSolutions.add(thisFragment);
 			}
 		}
 		
 		for(String potentialSolution : potentialSolutions) {
 			Solution solution = new SolutionImpl(potentialSolution, solutionResource, clueResource,
 					this.getInfModel(), this.getClue());
 			if(!(this.getSolutions().contains(solution))) {
 				this.getSolutions().add(solution);
				//log.debug("New solution found: " + solution.toString());
 			}
 		}
 	}
 
 	/*
 	 * THIS CODE IS DUPLICATED IN THE SIMPLEENTITYRECOGNISER CLASS - REFACTOR IT OUT SOMEWHERE?
 	 */
 	private String stripLanguageTag(String solutionText) {
 		int positionOfLanguageTag = solutionText.length() - LANGUAGE_TAG_LENGTH;
 		if(solutionText.length() > LANGUAGE_TAG_LENGTH) {
 			if(solutionText.substring(positionOfLanguageTag, positionOfLanguageTag + 1).equals(LANGUAGE_TAG))
 				return solutionText.substring(0, positionOfLanguageTag);
 		}
 		return solutionText;
 	}
 	
 	/*
 	 * DUPLICATED FROM ENTITYRECOGNISERIMPL CLASS
 	 */
 	private String toProperCase(String thisWord) {
 		String thisWordInProperCase = thisWord.substring(0, 1).toUpperCase();
 		if(thisWord.length() > 1) {
 			int index = 1; // start at the second letter of the word
 			while(index < thisWord.length()) {
 				String nextCharacter = thisWord.substring(index, index + 1);
 				thisWordInProperCase += nextCharacter;
 				if((nextCharacter.equals(" ")) && (index < (thisWord.length() - 1))) {
 					 index++; // the next character needs to be capitalised
 					 nextCharacter = thisWord.substring(index, index + 1);
 					 thisWordInProperCase += nextCharacter.toUpperCase();
 				}
 				index++;
 			}
 		}
 		return thisWordInProperCase;
 	}
 }
