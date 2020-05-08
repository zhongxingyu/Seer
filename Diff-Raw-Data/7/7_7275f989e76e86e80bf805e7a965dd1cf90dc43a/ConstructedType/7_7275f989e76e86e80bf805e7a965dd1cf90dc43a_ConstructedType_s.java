 package chameleon.core.type;
 
 import chameleon.core.declaration.SimpleNameSignature;
 import chameleon.core.element.Element;
 import chameleon.core.type.generics.FormalTypeParameter;
 import chameleon.core.validation.Valid;
 import chameleon.core.validation.VerificationResult;
 import chameleon.exception.ChameleonProgrammerException;
 
 /**
  * This class represents types created as a result of looking up (resolving) a generic parameter, which itself is
  * not a type.
  * 
  * @author Marko van Dooren
  */
 public class ConstructedType extends TypeIndirection {
 
 	public ConstructedType(SimpleNameSignature sig, Type aliasedType, FormalTypeParameter param) {
 		super(sig, aliasedType);
 		if(param == null) {
 			throw new ChameleonProgrammerException("The formal type parameter corresponding to a constructed type cannot be null.");
 		}
 		_param = param;
 	}
 	
 	@Override
 	public boolean uniSameAs(Element type) {
 		return type == this || 
 		       ((type instanceof ConstructedType) && (((ConstructedType)type).parameter().equals(parameter())));
 	}
 	
 	public FormalTypeParameter parameter() {
 		return _param;
 	}
 	
 	private final FormalTypeParameter _param;
 
 	@Override
 	public ConstructedType clone() {
 		return new ConstructedType(signature().clone(), aliasedType(), parameter());
 	}
 
 	@Override
 	public VerificationResult verifySelf() {
 		return Valid.create();
 	}
 
 }
