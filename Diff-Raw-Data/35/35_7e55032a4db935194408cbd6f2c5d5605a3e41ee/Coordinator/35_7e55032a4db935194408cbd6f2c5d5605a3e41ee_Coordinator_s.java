 package chameleon.aspect.oo.model.advice.weave.transform.runtime;
 
 import java.util.List;
 
 import chameleon.core.element.Element;
 import chameleon.core.lookup.LookupException;
 import chameleon.oo.variable.FormalParameter;
 
 /**
  * Represents a coordinator responsible for transforming advice and join points to insert runtime checks.
  * 
  * 	@author Jens De Temmerman
  *  @author Marko van Dooren
  *
  * 	@param <T>	The type of the advice that is being transformed (e.g. NormalMethod for MethodInvocations)
  */
public interface Coordinator<T extends Element<?>> {
 	
 	/**
 	 * 	Transform the given advice element to add all applicable runtime checks
 	 * 
 	 * 	@param element		The advice element
 	 * 	@param parameters	The list of formal parameters to implement
 	 * @throws LookupException 
 	 */
 	public void transform(T element, List<FormalParameter> parameters) throws LookupException;
 }
