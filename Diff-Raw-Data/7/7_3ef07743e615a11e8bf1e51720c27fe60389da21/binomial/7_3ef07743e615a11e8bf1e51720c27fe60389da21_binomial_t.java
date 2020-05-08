 package jmathlib.toolbox.polynomial;
 
 import jmathlib.core.tokens.numbertokens.DoubleNumberToken;
 import jmathlib.core.tokens.Token;
 import jmathlib.core.tokens.OperandToken;
 import jmathlib.core.functions.ExternalFunction;
 
 /**External function to calculate the set of binomial
    coefficents for the equation (x+y)^r*/
 public class binomial extends ExternalFunction
 {
 	/**calculate the number of permutations
 	@param operand[0] = the order of the equation
 	@return the coefficients as a vector
 	*/
 	public OperandToken evaluate(Token[] operands)
 	{
 		OperandToken result = new DoubleNumberToken(0);
 		if(operands.length >= 1 && operands[0] instanceof DoubleNumberToken)
 		{
 			double val = ((DoubleNumberToken)operands[0]).getValueRe();
 			int order = (new Double(val)).intValue();
 
 			double[][] results = new double[1][order + 1];
 			
 			DoubleNumberToken total   = ((DoubleNumberToken)operands[0]);
 			for(int count = 0; count <= order; count++)
 			{
 				//comb(x y) = y!/(x! * (y-x)!)
 				DoubleNumberToken objects = new DoubleNumberToken(count);
 				
 				//result = x!
 				OperandToken temp = objects.factorial();
 	
 				//temp2 = y-x
 				OperandToken temp2 = ((OperandToken)total.clone());
 				temp2 = temp2.subtract(objects);
 	
 				//temp2 = (y-x)!
 				temp2 = temp2.factorial();
 	
 				//temp = x! * (y-x)!
 				temp = temp.multiply(temp2);
 	
 				//temp2 = y! / (x! * (y-x)!)
 				temp2 = total.factorial();
 	
 				temp2 = temp2.divide(temp);
 
 				results[0][count] = ((DoubleNumberToken)temp2).getValueRe();
 			}
 			result = new DoubleNumberToken(results);
 		}
 		return result;
 	}
 }
 
 /*
 @GROUP
 polynomial
 @SYNTAX
Answer=binomial(value)
 @DOC
 Calculates the binomial coefficients of (x+y)^value.
 @NOTES
 @EXAMPLES
 <programlisting>
binomial(3) = [1, 3, 3, 1]
binomial(4) = [1, 4, 6, 4, 1]
 </programlisting>
 @SEE
 poly, roots
 */
 
