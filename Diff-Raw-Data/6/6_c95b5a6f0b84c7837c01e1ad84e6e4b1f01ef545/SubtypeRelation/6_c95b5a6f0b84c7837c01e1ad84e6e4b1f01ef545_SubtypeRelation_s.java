 package chameleon.oo.type.inheritance;
 
 import java.util.List;
 
 import org.rejuse.logic.ternary.Ternary;
 
 import chameleon.core.element.Element;
 import chameleon.core.lookup.LookupException;
 import chameleon.core.member.Member;
 import chameleon.core.member.MemberRelationSelector;
 import chameleon.core.modifier.Modifier;
 import chameleon.core.validation.BasicProblem;
 import chameleon.core.validation.Valid;
 import chameleon.core.validation.VerificationResult;
 import chameleon.oo.language.ObjectOrientedLanguage;
 import chameleon.oo.type.Type;
 import chameleon.oo.type.TypeReference;
 
 public class SubtypeRelation extends AbstractInheritanceRelation<SubtypeRelation> {
 
 	public SubtypeRelation(TypeReference ref) {
 		super(ref);
 	}
 
 	@Override
 	public SubtypeRelation clone() {
 		SubtypeRelation result = cloneThis();
 		for(Modifier<Modifier> modifier:modifiers()) {
 			result.addModifier(modifier.clone());
 		}
 		return result;
 	}
 	
 	protected SubtypeRelation cloneThis() {
 		return new SubtypeRelation(superClassReference().clone());
 	}
 
 	@Override
 	public Type superType() throws LookupException {
 		return superClass();
 	}
 	@Override
 	public VerificationResult verifySelf() {
 		try {
 			Type superClass = superClass();
 			if(superClass.is(language(ObjectOrientedLanguage.class).EXTENSIBLE) == Ternary.TRUE) {
 				return Valid.create();
 			} else {
 				return new SuperClassNotExtensible(this);
 			}
 		} catch (LookupException e) {
 			return new SuperClassNotExtensible(this);
 		}
 	}
 	
 	public static class SuperClassNotExtensible extends BasicProblem {
 
 		public SuperClassNotExtensible(Element element) {
 			super(element, "The super class is not extensible");
 		}
 		
 	}
 
 	@Override
 	public <D extends Member> List<D> membersDirectlyOverriddenBy(MemberRelationSelector<D> selector) throws LookupException {
 		return superClass().membersDirectlyOverriddenBy(selector);
 	}
 
 	@Override
 	public <D extends Member> List<D> membersDirectlyAliasedBy(MemberRelationSelector<D> selector) throws LookupException {
 		return superClass().membersDirectlyAliasedBy(selector);
 	}
 
 
 }
