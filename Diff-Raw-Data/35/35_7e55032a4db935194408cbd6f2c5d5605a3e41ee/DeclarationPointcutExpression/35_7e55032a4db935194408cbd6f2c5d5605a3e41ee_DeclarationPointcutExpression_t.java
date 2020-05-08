 package chameleon.aspect.core.model.pointcut.expression;
 
 import chameleon.aspect.core.model.pointcut.pattern.DeclarationPattern;
 import chameleon.core.declaration.Declaration;
 import chameleon.core.element.Element;
 import chameleon.core.lookup.LookupException;
 
public abstract class DeclarationPointcutExpression<E extends DeclarationPointcutExpression<E,J>,J extends Element> extends AbstractPointcutExpression<E,J> {
 	
 	public DeclarationPointcutExpression(DeclarationPattern pattern) {
 		this._pattern = pattern;
 	}
 
 	@Override
 	public abstract E clone();
 
 	private DeclarationPattern _pattern;
 	
 	public DeclarationPattern pattern() {
 		return _pattern;
 	}
 	
 	@Override
 	protected MatchResult match(J joinpoint) throws LookupException {
 		if(pattern().eval(declaration(joinpoint))) {
 			return new MatchResult<Element>(this, joinpoint);
 		} else {
 			return MatchResult.noMatch();
 		}
 	}
 
 	protected abstract Declaration declaration(J joinpoint) throws LookupException;
 
 }
