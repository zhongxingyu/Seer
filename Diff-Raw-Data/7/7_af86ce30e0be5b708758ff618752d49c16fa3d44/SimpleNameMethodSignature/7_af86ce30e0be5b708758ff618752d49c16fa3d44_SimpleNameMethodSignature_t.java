 package chameleon.support.member.simplename;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.rejuse.association.OrderedMultiAssociation;
 
 import chameleon.core.declaration.Signature;
 import chameleon.core.element.Element;
 import chameleon.core.lookup.LookupException;
 import chameleon.core.method.MethodSignature;
 import chameleon.core.namespace.NamespaceElement;
 import chameleon.core.type.Type;
 import chameleon.core.type.TypeReference;
 import chameleon.core.validation.BasicProblem;
 import chameleon.core.validation.Valid;
 import chameleon.core.validation.VerificationResult;
 
 public class SimpleNameMethodSignature extends MethodSignature<SimpleNameMethodSignature, NamespaceElement>{
 
   public SimpleNameMethodSignature(String name) {
     setName(name);
   }
   
   public String name() {
     return _name;
   }
   
   public void setName(String name) {
     _name = name;
     _nameHash = _name.hashCode();
   }
   
   public int nameHash() {
   	return _nameHash;
   }
   
   private int _nameHash;
   
   private String _name;
 
 ///*********************
 //* FORMAL PARAMETERS *
 //*********************/
 //
   public List<TypeReference> typeReferences() {
     return _parameterTypes.getOtherEnds();
   }
 
   public void add(TypeReference arg) {
    _parameterTypes.add(arg.parentLink());
   }
 
   public void remove(TypeReference arg) {
     _parameterTypes.remove(arg.parentLink());
    }
 
   public int getNbTypeReferences() {
    return _parameterTypes.size();
   }  
 
   private OrderedMultiAssociation<SimpleNameMethodSignature,TypeReference> _parameterTypes = new OrderedMultiAssociation<SimpleNameMethodSignature,TypeReference>(this);
 
   
   @Override
   public SimpleNameMethodSignature clone() {
   	SimpleNameMethodSignature result = new SimpleNameMethodSignature(name());
   	for(TypeReference ref: typeReferences()) {
   		result.add(ref.clone());
   	}
   	return result;
   }
 
   @Override
 	public boolean uniSameAs(Element other) throws LookupException {
 		boolean result = false;
 		if(other instanceof SimpleNameMethodSignature) {
 			SimpleNameMethodSignature sig = (SimpleNameMethodSignature) other;
 			result = name().equals(sig.name()) && sameParameterTypesAs(sig);
 		}
 		return result;
 	}
 
 	@Override
 	public List<Type> parameterTypes() throws LookupException {
 		List<Type> result = new ArrayList<Type>();
   	for(TypeReference ref: typeReferences()) {
   		result.add(ref.getType());
   	}
 		return result;
 	}
 
 	public List<? extends Element> children() {
 		return typeReferences();
 	}
 
 	@Override
 	public VerificationResult verifySelf() {
 		VerificationResult result = Valid.create();
 		if(name() == null) {
 			result = result.and(new BasicProblem(this, "The signature has no name."));
 		}
 		return result;
 	}
 
   @Override
   public String toString() {
   	StringBuffer result = new StringBuffer();
   	result.append(name());
   	result.append("(");
   	List<TypeReference> types = typeReferences();
   	int size = types.size();
 		if(size > 0) {
   		result.append(types.get(0).getName());
   	}
   	for(int i = 1; i < size; i++) {
   		result.append(",");
   		result.append(types.get(i).getName());
   	}
  	result.append(")");
   	return result.toString();
   }
 
 
 
 }
