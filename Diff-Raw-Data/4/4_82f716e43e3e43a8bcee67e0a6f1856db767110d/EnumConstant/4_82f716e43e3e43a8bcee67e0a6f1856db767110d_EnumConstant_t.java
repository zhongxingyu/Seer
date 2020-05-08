 package jnome.core.enumeration;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.rejuse.association.SingleAssociation;
 
 import chameleon.core.declaration.SimpleNameSignature;
 import chameleon.core.element.Element;
 import chameleon.core.expression.ActualArgument;
 import chameleon.core.expression.ActualArgumentList;
 import chameleon.core.member.FixedSignatureMember;
 import chameleon.core.member.Member;
 import chameleon.core.type.ClassBody;
 import chameleon.core.type.Type;
 import chameleon.core.validation.Valid;
 import chameleon.core.validation.VerificationResult;
 
 public class EnumConstant extends FixedSignatureMember<EnumConstant,Type,SimpleNameSignature,EnumConstant> {
 
 	public EnumConstant(SimpleNameSignature signature) {
 		super(signature);
 	}
 	
 	@Override
 	public EnumConstant clone() {
 		EnumConstant result = new EnumConstant(signature().clone());
 		return result;
 	}
 
 	public List<Member> getIntroducedMembers() {
 		List<Member> result = new ArrayList<Member>();
 		result.add(this);
 		return result;
 	}
 
 	public List<Element> children() {
     List<Element> result = new ArrayList<Element>();
     result.add(actualArgumentList());
     return result;
 	}
 
 	/**
 	 * ACTUAL PARAMETERS
 	 */
  private SingleAssociation<EnumConstant,ActualArgumentList> _parameters = new SingleAssociation<EnumConstant,ActualArgumentList>(this);
  
  public ActualArgumentList actualArgumentList() {
 	 return _parameters.getOtherEnd();
  }
 
   public void addParameter(ActualArgument parameter) {
   	actualArgumentList().addParameter(parameter);
   }
   
   public void addAllParameters(List<ActualArgument> parameters) {
   	for(ActualArgument param: parameters) {
   		addParameter(param);
   	}
   }
 
   public void removeParameter(ActualArgument parameter) {
   	actualArgumentList().removeParameter(parameter);
   }
 
   public List<ActualArgument> getActualParameters() {
     return actualArgumentList().getActualParameters();
   }
   
   public ClassBody getBody() {
   	return _body.getOtherEnd();
   }
   
   public void setBody(ClassBody body) {
   	if(body == null) {
   		_body.connectTo(null);
   	} else {
   		_body.connectTo(body.parentLink());
   	}
   }
   
   private SingleAssociation<EnumConstant,ClassBody> _body = new SingleAssociation<EnumConstant, ClassBody>(this);
 
 	@Override
 	public VerificationResult verifySelf() {
 		return Valid.create();
 	}
 
 }
