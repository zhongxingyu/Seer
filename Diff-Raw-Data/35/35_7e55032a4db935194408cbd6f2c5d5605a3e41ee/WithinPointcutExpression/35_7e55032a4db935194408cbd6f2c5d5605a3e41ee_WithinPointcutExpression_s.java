 package chameleon.aspect.core.model.pointcut.expression;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import chameleon.aspect.core.model.pointcut.pattern.DeclarationPattern;
 import chameleon.core.declaration.Declaration;
 import chameleon.core.element.Element;
 import chameleon.core.lookup.LookupException;
 
public class WithinPointcutExpression extends DeclarationPointcutExpression<WithinPointcutExpression, Element<?>>{
 
 	public WithinPointcutExpression(DeclarationPattern pattern, Class<? extends Declaration> type) {
 		super(pattern);
 		_type = type;
 	}
 
 	@Override
	public Class<? extends Element<?>> joinPointType() throws LookupException {
		return (Class<? extends Element<?>>) Element.class;
 	}
 
 	@Override
 	public List<? extends Element> children() {
 		return new ArrayList<Element>();
 	}
 
 	@Override
 	public WithinPointcutExpression clone() {
 		return new WithinPointcutExpression(pattern().clone(), type());
 	}
 
 	@Override
	protected Declaration declaration(Element<?> joinpoint) throws LookupException {
		return joinpoint.nearestAncestor(type());
 	}
 	
 	public Class<? extends Declaration> type() {
 		return _type;
 	}
 
 	private Class<? extends Declaration> _type;
 }
