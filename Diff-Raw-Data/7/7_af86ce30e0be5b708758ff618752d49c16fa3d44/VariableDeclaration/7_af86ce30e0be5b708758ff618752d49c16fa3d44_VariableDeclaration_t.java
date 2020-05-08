 package chameleon.support.variable;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.rejuse.association.SingleAssociation;
 
 import chameleon.core.declaration.Declaration;
 import chameleon.core.declaration.DeclarationContainer;
 import chameleon.core.declaration.SimpleNameSignature;
 import chameleon.core.element.Element;
 import chameleon.core.expression.Expression;
 import chameleon.core.lookup.DeclarationSelector;
 import chameleon.core.lookup.LexicalLookupStrategy;
 import chameleon.core.lookup.LocalLookupStrategy;
 import chameleon.core.lookup.LookupException;
 import chameleon.core.lookup.LookupStrategy;
 import chameleon.core.namespace.NamespaceElementImpl;
 import chameleon.core.validation.Valid;
 import chameleon.core.validation.VerificationResult;
 import chameleon.core.variable.Variable;
 import chameleon.util.Util;
 
 public class VariableDeclaration<V extends Variable> extends NamespaceElementImpl<VariableDeclaration<V>,VariableDeclarator<?,V,?>> implements DeclarationContainer<VariableDeclaration<V>,VariableDeclarator<?,V,?>> {
 
 	public VariableDeclaration(String name) {
 		this(new SimpleNameSignature(name), null);
 	}
 	
 	public VariableDeclaration(String name, Expression expr) {
 		this(new SimpleNameSignature(name), expr);
 	}
 	
 	public VariableDeclaration(SimpleNameSignature sig, Expression expr) {
 		setInitialization(expr);
 		setSignature(sig);
 	}
 	
 	@Override
 	public VariableDeclaration clone() {
 		Expression expression = initialization();
 		Expression clonedExpression = null;
 		if(expression != null) {
 			clonedExpression = expression.clone();
 		}
 		return new VariableDeclaration(signature().clone(), clonedExpression);
 	}
 
 	 public void setSignature(SimpleNameSignature signature) {
 	    if(signature != null) {
 	    	_signature.connectTo(signature.parentLink());
 	    } else {
 	    	_signature.connectTo(null);
 	    }
 	  }
 	  
 	  /**
 	   * Return the signature of this member.
 	   */
 	  public SimpleNameSignature signature() {
 	    return _signature.getOtherEnd();
 	  }
 	  
 	  private SingleAssociation<VariableDeclaration, SimpleNameSignature> _signature = new SingleAssociation<VariableDeclaration, SimpleNameSignature>(this);
 
 
 	public List<Element> children() {
 		List<Element> result = new ArrayList<Element>();
 		Util.addNonNull(initialization(), result);
 		result.add(signature());
 		return result;
 	}
 
 	/**
 	 * EXPRESSION
 	 */
 	private SingleAssociation<VariableDeclaration,Expression> _expression = new SingleAssociation<VariableDeclaration,Expression>(this);
 
   
   public Expression initialization() {
     return _expression.getOtherEnd();
   }
   
   public void setInitialization(Expression expression) {
     if(expression != null) {
     	_expression.connectTo(expression.parentLink());
     }
     else {
       _expression.connectTo(null); 
     }
   }
   
   public V variable() {
   	Expression init = initialization();
 		Expression initClone = (init == null ? null : init.clone());
 		V result = parent().createVariable(signature().clone(),initClone);
   	result.setUniParent(parent());
  	result.setOrigin(this);
   	transform(result);
   	return result;
   }
   
   protected void transform(V variable) {
   }
 
 	public List<? extends Declaration> declarations() throws LookupException {
 		List<Variable> result = new ArrayList<Variable>();
 		result.add(variable());
 		return result;
 	}
 	
 	/**
 	 * Return a standard lexical context that is attached to this variable declaration,
 	 * and to a target context which is also attached to this variable declaration.
 	 */
 	public LookupStrategy linearContext() throws LookupException {
 		return new LexicalLookupStrategy(new LocalLookupStrategy<VariableDeclaration>(this),this);
 	}
 
 	public <D extends Declaration> List<D> declarations(DeclarationSelector<D> selector) throws LookupException {
 		List<D> result = new ArrayList<D>();
 		D element = selector.selection(variable());
 		if(element != null) {
 		  result.add(element);
 		}
 		return result;
 	}
 
 	@Override
 	public VerificationResult verifySelf() {
 		return checkNull(signature(), "The variable declaration has no signature", Valid.create());
 	}
   
 }
