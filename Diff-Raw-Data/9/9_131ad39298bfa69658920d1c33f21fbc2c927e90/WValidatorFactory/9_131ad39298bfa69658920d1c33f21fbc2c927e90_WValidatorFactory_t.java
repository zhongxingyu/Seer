 package com.delcyon.capo.webapp.widgets;
 
 import eu.webtoolkit.jwt.WValidator;
 
 /** this is just a wrapper class around WValidator that less us use java 8 lambdas 
  * 
  * @author jeremiah
  *
  */
 public abstract class WValidatorFactory extends WValidator
 {
 	@Override
 	public Result validate(String input)
 	{
 		return validateFunction(input);
 	}
 
 	/**
 	 * method overridden by lambda expression
 	 * @param input
 	 * @return
 	 */
 	protected abstract Result validateFunction(String input);
 	
 	/**
 	 * factory method to to use a lambda to make a custom WValidator
 	 * {@link WValidatorInterface#validateFunction(String) validate} method.
 	 * @param (String input) -> Result 
 	 * @return WValidator
 	 */
 	public static WValidator validator(WValidatorInterface validatorInterface)
 	{
 		return new WValidatorFactory()
 		{
 			@Override
 			public Result validateFunction(String input)
 			{
 				return validatorInterface.validateFunction(input);
 			}
 		};
 	}
 	
 	/**
 	 * matches the validate method from WValidator
 	 *
 	 */
	public interface WValidatorInterface
 	{		
 		public abstract Result validateFunction(String input);
 
 	}
 }
