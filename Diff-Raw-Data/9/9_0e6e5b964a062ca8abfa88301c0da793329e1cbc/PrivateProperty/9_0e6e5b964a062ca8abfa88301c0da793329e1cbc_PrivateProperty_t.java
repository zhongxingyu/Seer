 package chameleon.support.modifier;
 
 import org.rejuse.property.PropertyMutex;
 import org.rejuse.property.PropertyUniverse;
 
 import chameleon.core.element.Element;
 import chameleon.core.lookup.LookupException;
 import chameleon.core.property.ChameleonProperty;
 import chameleon.core.scope.LexicalScope;
 import chameleon.core.scope.Scope;
 import chameleon.core.scope.ScopeProperty;
 import chameleon.oo.type.Type;
 
 public class PrivateProperty extends ScopeProperty {
 	
 	public final static String ID = "accessibility.private";
 	
 	public PrivateProperty(PropertyUniverse<ChameleonProperty> universe, PropertyMutex<ChameleonProperty> family) {
 		this(ID, universe, family);
   }
 	public PrivateProperty(String name, PropertyUniverse<ChameleonProperty> universe, PropertyMutex<ChameleonProperty> family) {
 		super(name, universe, family);
 	}
 
 	public Scope scope(Element element) throws LookupException {
 		try {
			return new LexicalScope(((Type)element.farthestAncestor(Type.class)));
 		} catch (ClassCastException exc) {
 			throw new LookupException("Private property does not support elements that are no TypeDescendant.");
 		}
 	}
 
 }
