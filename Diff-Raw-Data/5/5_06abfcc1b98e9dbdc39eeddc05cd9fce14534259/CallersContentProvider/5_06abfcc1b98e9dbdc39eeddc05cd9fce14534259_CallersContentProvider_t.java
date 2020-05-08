 /**
  * Created on 13-jun-07
  * @author Tim Vermeiren
  */
 package chameleon.eclipse.presentation.callhierarchy;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.HashSet;
 
 import org.eclipse.jface.viewers.ITreeContentProvider;
 import org.eclipse.jface.viewers.Viewer;
 import org.rejuse.predicate.SafePredicate;
 
 import chameleon.core.declaration.Declaration;
 import chameleon.core.reference.CrossReference;
 import chameleon.eclipse.project.ChameleonProjectNature;
 import chameleon.exception.ModelException;
 
 /**
  * A context provider that calculates all the declarations that contain
  * a cross-reference to a given declaration.
  * 
  * @author Tim Vermeiren
  */
 public class CallersContentProvider implements ITreeContentProvider {
 	
 	private ChameleonProjectNature projectNature;
 	
 	public CallersContentProvider(ChameleonProjectNature projectNature) {
 		this.projectNature = projectNature;
 	}
 	
 	/**
 	 * Returns all the declarations that contain a cross-reference to the given declaration 
 	 * (if inputObject is a Declaration)
 	 */
 	public Object[] getChildren(Object inputObject) {
 		if(inputObject instanceof Declaration){
 			final Declaration declaration = (Declaration)inputObject;
 			// get all invocations in this project:
 			Collection<CrossReference> invocations = getInvocations();
 			// System.out.println("found "+ invocations.size()+" invocations");
 			// build a predicate to search for matching invocations:
 			SafePredicate<CrossReference> predicate = new SafePredicate<CrossReference>(){
 				@Override
 				public boolean eval(CrossReference invocation) {
 					try {
 						return declaration.equals(invocation.getElement().declarator());
 					} catch (ModelException e) {
 						e.printStackTrace();
 						return false;
 					}
 				}
 			};
 			// filter invocations
 			predicate.filter(invocations);
 			// get the methods containing the invocations:
 			Collection<Declaration> result = new HashSet<Declaration>();
 			for(CrossReference invocation : invocations){
 				Declaration callingDeclaration = invocation.nearestAncestor(Declaration.class);
 				if(callingDeclaration != null) {
 					result.add(callingDeclaration);
 				}
 			}
 			return result.toArray();
 		} else if(inputObject instanceof RootDeclaration){
 			Declaration declaration = ((RootDeclaration)inputObject).getDeclaration();
 			return new Object[]{declaration};
 		}
 		return null;
 	}
 	
 	/**
 	 * Cashing of the method invocations of this project (for performance reasons)
 	 */
 	private Collection<CrossReference> _cachedInvocations;
 	
 	/**
 	 * Returns all the method invocations in the current project.
 	 * 
 	 */
 	private Collection<CrossReference> getInvocations() {
 		if(_cachedInvocations == null){
			_cachedInvocations = projectNature.getModel().descendants(CrossReference.class);
 		}
		return new HashSet<CrossReference>(_cachedInvocations);
 	}
 
 	public Object getParent(Object inputObject) {
 		return null;
 	}
 
 	public boolean hasChildren(Object inputObject) {
 		return true;
 	}
 
 	public Object[] getElements(Object inputObject) {
 		return getChildren(inputObject);
 		// return new Object[]{inputObject}; 
 	}
 	
 	
 	public void dispose() {
 		// NOP
 	}
 
 	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
 		// NOP
 	}
 
 	
 }
