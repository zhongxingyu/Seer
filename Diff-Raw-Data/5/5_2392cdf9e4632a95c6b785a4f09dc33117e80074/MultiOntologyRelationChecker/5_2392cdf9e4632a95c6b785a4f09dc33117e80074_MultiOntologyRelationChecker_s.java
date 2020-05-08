 package com.centeropenmiddleware.semwidgets.snippets.relationCheck;
 
 import java.io.IOException;
 import java.net.URISyntaxException;
 import java.net.URL;
 import java.util.Arrays;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.HashSet;
 import org.coode.owlapi.manchesterowlsyntax.ManchesterOWLSyntaxClassExpressionParser;
 import org.semanticweb.HermiT.Reasoner;
 import org.semanticweb.owlapi.apibinding.OWLManager;
 import org.semanticweb.owlapi.expression.OWLEntityChecker;
 import org.semanticweb.owlapi.expression.ParserException;
 import org.semanticweb.owlapi.expression.ShortFormEntityChecker;
 import org.semanticweb.owlapi.model.AddAxiom;
 import org.semanticweb.owlapi.model.IRI;
 import org.semanticweb.owlapi.model.OWLClass;
 import org.semanticweb.owlapi.model.OWLClassExpression;
 import org.semanticweb.owlapi.model.OWLDataFactory;
 import org.semanticweb.owlapi.model.OWLOntology;
 import org.semanticweb.owlapi.model.OWLOntologyCreationException;
 import org.semanticweb.owlapi.model.OWLOntologyManager;
 import org.semanticweb.owlapi.model.RemoveAxiom;
 import org.semanticweb.owlapi.util.BidirectionalShortFormProviderAdapter;
 import org.semanticweb.owlapi.util.DefaultPrefixManager;
 import org.semanticweb.owlapi.util.OWLOntologyMerger;
 import org.semanticweb.owlapi.util.SimpleShortFormProvider;
 import uk.ac.manchester.cs.owl.owlapi.mansyntaxrenderer.ManchesterOWLSyntaxPrefixNameShortFormProvider;
 
 
 public class MultiOntologyRelationChecker extends RelationChecker {
 
 	private IRI mergedIRI;
 	private OWLOntologyMerger ontologyMerger;
 	private OWLOntology mergedOntology;
 	private OWLOntologyManager ontologyManager;
 	private OWLDataFactory factory;
 	private HashMap<URL, OWLOntology> ontologiesUsed;
 	private OWLOntology expressionEquivalences;
 	private IRI eqIRI;
 	private Integer auxiliarConcepts;
 	private static String auxConceptName = "auxConceptNameAddedForEquivalenceAndExpressionHandling";
 
 	public MultiOntologyRelationChecker() throws OWLOntologyCreationException {
 		this.init();
 		this.updateReasoner();
 	}
 
 	public MultiOntologyRelationChecker(Collection<URL> ontologyURLs) throws OWLOntologyCreationException, URISyntaxException {
 		this.init();
 		for (URL ontologyURL : ontologyURLs)
 			this.ontologiesUsed.put(ontologyURL, this.ontologyManager.loadOntology(IRI.create(ontologyURL)));
 		this.updateReasoner();
 	}
 
 	private void init() throws OWLOntologyCreationException {
 		this.auxiliarConcepts = 0;
 		this.eqIRI = IRI.create("http://centeropenmiddleware.com/semwidgets/eqs/");
 		this.mergedIRI = IRI.create("http://centeropenmiddleware.com/semwidgets/merged/");
 		this.ontologyManager = OWLManager.createOWLOntologyManager();
 		this.expressionEquivalences = this.ontologyManager.createOntology(this.eqIRI);
 		this.factory = this.ontologyManager.getOWLDataFactory();
 		this.ontologyMerger = new OWLOntologyMerger(this.ontologyManager);
 		this.mergedOntology = this.ontologyMerger.createMergedOntology(this.ontologyManager, this.mergedIRI);
 		this.ontologiesUsed = new HashMap<URL, OWLOntology>();
 	}
 
 	private void updateReasoner() throws OWLOntologyCreationException {
 		this.ontologyManager.removeOntology(this.mergedOntology);
 		this.mergedOntology = this.ontologyMerger.createMergedOntology(this.ontologyManager, this.mergedIRI);
 		this.reasoner = new Reasoner.ReasonerFactory().createReasoner(this.mergedOntology);
 		this.reasoner.precomputeInferences();
 		OWLEntityChecker oec = new ShortFormEntityChecker(new BidirectionalShortFormProviderAdapter(this.ontologyManager,
 				Collections.singleton(this.mergedOntology), new SimpleShortFormProvider()));
 		this.parser = new ManchesterOWLSyntaxClassExpressionParser(this.factory, oec);
 		OWLEntityChecker oec2 = new ShortFormEntityChecker(new BidirectionalShortFormProviderAdapter(this.ontologyManager,
 				Collections.singleton(this.mergedOntology), new ManchesterOWLSyntaxPrefixNameShortFormProvider(this.ontologyManager, this.mergedOntology)));
 		this.fullParser = new ManchesterOWLSyntaxClassExpressionParser(this.factory, oec2);
 	}
 
 	public void addOntology(URL ontologyURL) throws IOException, OWLOntologyCreationException, URISyntaxException {
 		if (this.ontologiesUsed.containsKey(ontologyURL))
 			return;
 		this.ontologiesUsed.put(ontologyURL, this.ontologyManager.loadOntology(IRI.create(ontologyURL)));
 		this.updateReasoner();
 	}
 
 	public void removeOntology(URL ontologyURL) throws OWLOntologyCreationException {
 		if (!this.ontologiesUsed.containsKey(ontologyURL))
 			return;
 		this.ontologyManager.removeOntology(this.ontologiesUsed.get(ontologyURL));
 		this.ontologiesUsed.remove(ontologyURL);
 		this.updateReasoner();
 	}
 
 	protected void addEquivalences(Collection<String> equivalentConcepts) throws OWLOntologyCreationException {
 		this.ontologyManager.applyChange(new AddAxiom(this.expressionEquivalences, this.factory.getOWLEquivalentClassesAxiom(this
 				.parseAddAll(equivalentConcepts))));
 		this.updateReasoner();
 	}
 
 	protected void removeEquivalences(Collection<String> equivalentConcepts) throws OWLOntologyCreationException {
 		this.ontologyManager.applyChange(new RemoveAxiom(this.expressionEquivalences, this.factory.getOWLEquivalentClassesAxiom(this
 				.parseAddAll(equivalentConcepts))));
 		this.updateReasoner();
 	}
 
 	protected void purgeAuxiliarConcepts(Collection<String> equivalentConcepts) throws OWLOntologyCreationException {
 		for (String classString : equivalentConcepts) {
 			OWLClassExpression classOWL = this.factory.getOWLClass(classString, new DefaultPrefixManager(this.eqIRI.toString()));
 			this.ontologyManager.applyChange(new RemoveAxiom(this.expressionEquivalences, this.factory.getOWLDeclarationAxiom(classOWL.asOWLClass())));
 		}
 		this.updateReasoner();
 	}
 
 	private HashSet<OWLClassExpression> parseAddAll(Collection<String> equivalentConcepts) {
 		HashSet<OWLClassExpression> result = new HashSet<OWLClassExpression>();
 		for (String classString : equivalentConcepts) {
 			OWLClassExpression classOWL;
 			try {
 				classOWL = this.parse(classString);
 			} catch (ParserException e) {
 				classOWL = this.factory.getOWLClass(classString, new DefaultPrefixManager(this.eqIRI.toString()));
 				this.ontologyManager.applyChange(new AddAxiom(this.expressionEquivalences, this.factory.getOWLDeclarationAxiom(classOWL.asOWLClass())));
 			}
 			result.add(classOWL);
 		}
 		return result;
 	}
 
 	// FIXME: creating a new object is strongly advised when "many" changes to active ontologies are made to keep the number of memory leaks low
 	public OWLClassExpression getNamedCandidate(String classExpressionString) throws ParserException, OWLOntologyCreationException {
 		OWLClassExpression result = this.getCandidate(classExpressionString);
 		if (!result.isAnonymous())
 			return result;
 		for (OWLClass r : this.equivalentConcepts(classExpressionString))
 			return r;
		this.addEquivalences(Arrays.asList(classExpressionString, MultiOntologyRelationChecker.auxConceptName + this.auxiliarConcepts.toString()));
 		this.auxiliarConcepts++;
		return result;
 	}
 
 }
