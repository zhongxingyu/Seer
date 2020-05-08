 package chameleon.core.method;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Iterator;
 import java.util.List;
 
 import org.rejuse.association.OrderedMultiAssociation;
 import org.rejuse.association.SingleAssociation;
 
 import chameleon.core.MetamodelException;
 import chameleon.core.declaration.Declaration;
 import chameleon.core.element.Element;
 import chameleon.core.lookup.DeclarationSelector;
 import chameleon.core.lookup.LookupException;
 import chameleon.core.namespace.NamespaceElement;
 import chameleon.core.namespace.NamespaceElementImpl;
 import chameleon.core.type.Type;
 import chameleon.core.type.generics.TypeParameter;
 import chameleon.core.type.generics.TypeParameterBlock;
 import chameleon.core.variable.FormalParameter;
 import chameleon.core.variable.VariableContainer;
 /**
  * A class of objects representing method headers. A method header contains for example the name and parameters of a method.
  * 
  * @author Marko van Dooren
  *
  * @param <E>
  * @param <P>
  * @param <S>
  */
 public abstract class MethodHeader<E extends MethodHeader, P extends NamespaceElement, S extends MethodSignature> extends NamespaceElementImpl <E,P> implements VariableContainer<E, P> { //extends Signature<E, P> 
   
   public E clone() {
     E result = cloneThis();
     for(FormalParameter param:formalParameters()) {
       result.addParameter(param.clone());
     }
     return result;
   }
 
   /**
    * Return the signature of the method of this method header. The signature is generated based on
    * the information in the header.
    * @return
    */
   public abstract S signature();
   
   protected abstract E cloneThis();
   
   public List<Element> children() {
   	List<Element> result = new ArrayList<Element>();
   	result.addAll(formalParameters());
   	return result;
   }
   
   public String name() {
   	return signature().name();
   }
   
   /*********************
    * FORMAL PARAMETERS *
    *********************/
 
   public List<FormalParameter> formalParameters() {
     return _parameters.getOtherEnds();
   }
 
 
   public void addParameter(FormalParameter arg) {
     _parameters.add(arg.parentLink());
   }
 
   public int getNbParameters() {
     return _parameters.size();
   }
 
   private OrderedMultiAssociation<MethodHeader,FormalParameter> _parameters = new OrderedMultiAssociation<MethodHeader,FormalParameter>(this);
   
   /**
    * Return the type of the formal parameters of this signature.
    * 
    * @return
    * @throws MetamodelException
    */
   public List<Type> getParameterTypes() throws LookupException {
     List<Type> result = new ArrayList<Type>();
     for(FormalParameter param:formalParameters()) {
       result.add(param.getType());
     }
     return result;
   }
 
 //  /**
 //   * Check whether or not this method contains a formal parameter with the given name.
 //   *
 //   * @param name
 //   *        The name that has to be checked.
 //   */
 //  /*@
 //   @ public behavior
 //   @
 //   @ post \result == (\exists FormalParameter fp; getFormalParameters.contains(fp);
 //   @                   fp.getName().equals(name);
 //   @*/
 //  public boolean containsParameterWithName(final String name) {
 //    return new SafePredicate() {
 //      public boolean eval(Object o) {
 //        return ((FormalParameter)o).getName().equals(name);
 //      }
 //    }.exists(getParameters());
 //  }
 
 //  public Type getNearestType() {
 //  	return parent().getNearestType();
 //  }
   
   public List<Declaration> declarations() {
     List<Declaration>  result = new ArrayList<Declaration>();
     result.addAll(formalParameters());
     result.addAll(typeParameters());
     return result;
   }
   
 	public <D extends Declaration> List<D> declarations(DeclarationSelector<D> selector) throws LookupException {
 		return selector.selection(formalParameters());
 	}
 
 
   public boolean sameParameterTypesAs(MethodHeader other) throws MetamodelException {
   	boolean result = false;
   	if (other != null) {
 			List<FormalParameter> mine = formalParameters();
 			List<FormalParameter> others = other.formalParameters();
 			result = mine.size() == others.size();
 			Iterator<FormalParameter> iter1 = mine.iterator();
 			Iterator<FormalParameter> iter2 = others.iterator();
 			while (result && iter1.hasNext()) {
         result = result && iter1.next().getType().equals(iter2.next().getType());
 			}
 		}
   	return result;
   }
 
   public NamespaceElement variableScopeElement() {
   	return parent();
   }
   
 //  public LookupStrategy lexicalLookupStrategy(Element element) {
 //  	return language().lookupFactory().createLexicalLookupStrategy(language().lookupFactory().createLocalLookupStrategy(this),this);
 //  }
   
 	private SingleAssociation<MethodHeader, TypeParameterBlock> _typeParameters = new SingleAssociation<MethodHeader, TypeParameterBlock>(this);
 	
 	public TypeParameterBlock parameterBlock() {
 		return _typeParameters.getOtherEnd();
 	}
 	
 	/**
 	 * Return the type parameters of this method header. If 
 	 * @return
 	 */
 	public List<TypeParameter> typeParameters() {
 		TypeParameterBlock parameterBlock = parameterBlock();
 		return (parameterBlock == null ? new ArrayList<TypeParameter>() :parameterBlock.parameters());
 	}
 	
 	public void addAllTypeParameters(Collection<TypeParameter> parameters) {
 		for(TypeParameter param:parameters) {
 			addTypeParameter(param);
 		}
 	}
 
 	public void addTypeParameter(TypeParameter parameter) {
 		TypeParameterBlock parameterBlock = parameterBlock();
 		if(parameterBlock == null) {
 			// Lazy init.
 			_typeParameters.connectTo(new TypeParameterBlock().parentLink());
 		}
 		parameterBlock.add(parameter);
 	}
 
 	public void removeTypeParameter(TypeParameter parameter) {
 		parameterBlock().add(parameter);
 	}
 	
 	public void replaceTypeParameter(TypeParameter oldParameter, TypeParameter newParameter) {
 		parameterBlock().replace(oldParameter, newParameter);
 	}
 
 
 }
