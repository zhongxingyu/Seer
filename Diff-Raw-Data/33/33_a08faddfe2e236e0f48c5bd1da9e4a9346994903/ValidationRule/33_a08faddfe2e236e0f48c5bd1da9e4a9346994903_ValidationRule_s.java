 package dfh.cli;
 
 /**
  * Test to be applied to particular command line values.
  * <p>
  * <b>Creation date:</b> Nov 4, 2011
  * 
  * @author David Houghton
  * 
  * @param <K>
  *            the type of object parsed out of the argument if validation
  *            succeeds
  */
 public interface ValidationRule<K> {
 	/**
 	 * Applies the validation test to a particular argument.
 	 * 
 	 * @param arg
 	 *            command line argument
	 * @return result of parsing the argument
 	 * @throws ValidationException
 	 *             any error message generated if validation fails
 	 */
 	public void test(K arg) throws ValidationException;
 }
