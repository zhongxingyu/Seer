 package owl;
 
 import org.eclipse.emf.ecore.EObject;
 import org.eclipse.xtext.naming.DefaultDeclarativeQualifiedNameProvider;
 import org.eclipse.xtext.naming.IQualifiedNameConverter;
 import org.eclipse.xtext.naming.QualifiedName;
 
 import com.google.inject.Inject;
 
 public class OwlQualifiedNameProvider extends DefaultDeclarativeQualifiedNameProvider {
 	@Inject
 	private IQualifiedNameConverter qualifiedNameConverter;
 	
 	/**
 	 * The function returns a qualified name for a eobject.
 	 * 
 	 * @param pObject The object which name should be determined
 	 * @return The name of the object
 	 */
 	@Override
 	public QualifiedName getFullyQualifiedName(EObject pObject){
 		if(pObject instanceof OWLClass) {
			System.err.println("OWL Class");
 			return qualifiedNameConverter.toQualifiedName(((OWLClass)pObject).getLocalName());
 		}
 		
 		if(pObject instanceof OWLObjectProperty) {
			System.err.println("OWL Object Property");
 			return qualifiedNameConverter.toQualifiedName(((OWLObjectProperty)pObject).getLocalName());
 		}
 		
 		if(pObject instanceof OWLDatatypeProperty) {
			System.err.println("OWL Datatype Property");
 			return qualifiedNameConverter.toQualifiedName(((OWLDatatypeProperty)pObject).getLocalName());
 		}
 		
 		return super.getFullyQualifiedName(pObject);
 	}
 }
