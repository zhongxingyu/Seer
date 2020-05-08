 package edu.iastate.pdlreasoner.model.visitor;
 
 import java.util.Set;
 
 import edu.iastate.pdlreasoner.model.Atom;
 import edu.iastate.pdlreasoner.model.Concept;
 import edu.iastate.pdlreasoner.model.ContextualizedConcept;
 import edu.iastate.pdlreasoner.model.DLPackage;
 import edu.iastate.pdlreasoner.model.Negation;
 import edu.iastate.pdlreasoner.model.Top;
 import edu.iastate.pdlreasoner.struct.MultiValuedMap;
 import edu.iastate.pdlreasoner.util.CollectionUtil;
 
 public class ExternalConceptsExtractor extends ConceptTraverser {
 	
 	private final DLPackage m_HomePackage;
 	private MultiValuedMap<DLPackage, Concept> m_ExternalConcepts;
 	private Set<DLPackage> m_ExternalNegations;
 	
 	public ExternalConceptsExtractor(DLPackage homePackage) {
 		m_HomePackage = homePackage;
 		m_ExternalConcepts = new MultiValuedMap<DLPackage, Concept>();
 		m_ExternalNegations = CollectionUtil.makeSet();
 	}
 	
 	public MultiValuedMap<DLPackage, Concept> getExternalConcepts() {
 		return m_ExternalConcepts;
 	}
 	
 	public Set<DLPackage> getExternalNegationContexts() {
 		return m_ExternalNegations;
 	}
 	
 	private void visitContextualizedAtom(ContextualizedConcept c) {
 		DLPackage context = c.getContext();
 		if (!m_HomePackage.equals(context)) {
 			m_ExternalConcepts.add(context, c);				
 		}
 	}
 	
 	@Override
 	public void visit(Top top) {
 		visitContextualizedAtom(top);
 	}
 
 	@Override
 	public void visit(Atom atom) {
 		visitContextualizedAtom(atom);
 	}
 	
 	@Override
 	public void visit(Negation negation) {
 		DLPackage context = negation.getContext();
 		if (!m_HomePackage.equals(negation.getContext())) {
 			m_ExternalNegations.add(context);				
 		}
 	}
 
 }
