 package uk.ac.abdn.erg.iain.strigidoc;
 
 import java.util.Set;
 
 import org.semanticweb.owlapi.model.OWLAnnotation;
 import org.semanticweb.owlapi.model.OWLAnnotationProperty;
 import org.semanticweb.owlapi.model.OWLClass;
 import org.semanticweb.owlapi.model.OWLClassExpression;
 import org.semanticweb.owlapi.model.OWLEntity;
 import org.semanticweb.owlapi.model.OWLOntology;
 import org.semanticweb.owlapi.model.OWLProperty;
 import org.semanticweb.owlapi.model.OWLPropertyExpression;
 
 public class OntObjectFactory {
 
 	public static OntObject createFromClass(OWLClass c, OWLOntology o) {
 		OntObject ob = createFromEntity(OntObject.OntObjectType.CLASS, (OWLEntity) c, o);
 		
 		Set<OWLClassExpression> sup = c.getSuperClasses(o);
 		Set<OWLClassExpression> sub = c.getSubClasses(o);
 		
		for ( OWLClassExpression s : sup ) {
			checkAndAddRelated(ob, (OWLEntity) s, o, true);
 		}
 		
 		for ( OWLClassExpression s : sub ) {
			checkAndAddRelated(ob, (OWLEntity) s, o, false);
 		}
 		
 		return ob;
 	}
 	
 	@SuppressWarnings({ "rawtypes", "unchecked" })
 	public static OntObject createFromProperty(OntObject.OntObjectType t, OWLProperty p, OWLOntology o) {
 		OntObject ob = createFromEntity(t, (OWLEntity) p, o);
 		
 		Set<OWLPropertyExpression> sup = p.getSuperProperties(o);
 		Set<OWLPropertyExpression> sub = p.getSubProperties(o);
 		
 		for ( OWLPropertyExpression s : sup ) {
			checkAndAddRelated(ob, (OWLEntity) s, o, true);
 		}
 		
 		for ( OWLPropertyExpression s : sub ) {
			checkAndAddRelated(ob, (OWLEntity) s, o, false);
 		}
 		
 		return ob;
 	}
 	
 	public static OntObject createFromAnnotationProperty(OWLAnnotationProperty p, OWLOntology o) {
 		
 		OntObject ob = createFromEntity(OntObject.OntObjectType.ANNOTATION_PROPERTY, (OWLEntity) p, o);
 		
 		Set<OWLAnnotationProperty> sup = p.getSuperProperties(o);
 		Set<OWLAnnotationProperty> sub = p.getSubProperties(o);
 		
 		for ( OWLAnnotationProperty s : sup ) {
			checkAndAddRelated(ob, (OWLEntity) s, o, true);
 		}
 		
 		for ( OWLAnnotationProperty s : sub ) {
			checkAndAddRelated(ob, (OWLEntity) s, o, false);
 		}
 		
 		return ob;
 		
 	}
 
 	private static OntObject createFromEntity(OntObject.OntObjectType t, OWLEntity e, OWLOntology o) {
 		
 		OntObject ob = new OntObject(t, e.getIRI().toString());
 		
 		Set<OWLAnnotation> anns = e.getAnnotations(o);
 		
 		for ( OWLAnnotation a : anns ) {
 			if (a.getProperty().getIRI().toString()
 					.equals("http://www.w3.org/2000/01/rdf-schema#comment")) {
 					String comment = a.getValue().toString();
 					comment = comment.substring(1, comment.length() - 1);
 					ob.setComment(comment);
 					break;
 				}
 		}
 		
 		return ob;
 	}
 	
	private static boolean checkAndAddRelated(OntObject ob, OWLEntity e, OWLOntology o, boolean sup) {
 		if ( ob.getIRIString().startsWith(o.getOntologyID().getOntologyIRI().toString()) ) {
 			if ( sup ) {
				ob.addSuper(e.getIRI().toString());
 			} else {
				ob.addSub(e.getIRI().toString());
 			}
 			return true;
 		} else {
 			return false;
 		}
 	}
 }
