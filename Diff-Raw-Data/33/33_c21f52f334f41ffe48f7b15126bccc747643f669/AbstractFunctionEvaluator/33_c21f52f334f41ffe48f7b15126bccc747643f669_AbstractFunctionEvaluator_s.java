 package org.eclipse.jubula.client.core.functions;
 
 import org.eclipse.jubula.client.core.i18n.Messages;
 import org.eclipse.jubula.tools.exception.InvalidDataException;
 import org.eclipse.jubula.tools.messagehandling.MessageIDs;
 import org.eclipse.osgi.util.NLS;
 
 /**
  * Abstract base class for methods useful in Evaluators
  * 
 *
 * @author al
  * @created Feb 22, 2012
 * @version $Revision: 1.1 $
  */
 public abstract class AbstractFunctionEvaluator 
     implements IFunctionEvaluator {
 
     /**
     * @param arguments evaluate() parameter
     * @param numParamsExpected how many parameters does the Evaluator expect
     * @throws InvalidDataException if the are not exactly numParamsExpected
     * parameters in arguments
      */
    protected void validateParamCount(String[] arguments, 
        int numParamsExpected)
         throws InvalidDataException {
        if (arguments.length != 2) {
            throw new InvalidDataException(
                    NLS.bind(Messages.WrongNumFunctionArgs, 
                            new Integer[] {
                                numParamsExpected, arguments.length}),
                     MessageIDs.E_WRONG_NUM_FUNCTION_ARGS);
         }
     }

 }
