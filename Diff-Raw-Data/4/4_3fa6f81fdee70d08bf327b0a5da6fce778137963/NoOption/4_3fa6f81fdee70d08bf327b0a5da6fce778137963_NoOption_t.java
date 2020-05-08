 /*
  *  You may not change or alter any portion of this comment or credits
  * of supporting developers from this source code or any supporting source code
  * which is considered copyrighted (c) material of the original comment or credit authors.
  * This program is distributed WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
  *
  */
 
 package cmdGA;
 import cmdGA.exceptions.IncorrectParameterTypeException;
 import cmdGA.parameterType.BooleanParameter;
 /**
  * Represents an option of a command line that do not accepts any arguments.
  * Its value can be only boolean.   
  * 
  * @author Javier Iserte <jiserte@unq.edu.ar>
  *
  */
 public class NoOption extends Option {
	protected Boolean value = false;
 	// CONSTRUCTORS
 	/**
 	 * Creates a new instance of NoOption
 	 * 
 	 * @param parser An instance of Parser that is linked to the option.
 	 * @param defaultValue The default value returned 
 	 * @param name The string that is used to call this option in the command line.
 	 * @param alias An alias for the name.
 	 */
 	public NoOption(Parser parser, Object defaultValue, String name, String alias) {
 		super(parser, defaultValue, name, alias, new BooleanParameter());
 	}
 	/**
 	 * Creates a new instance of NoOption
 	 * 
 	 * @param parser An instance of Parser that is linked to the option.
 	 * @param defaultValue The default value returned 
 	 * @param name The string that is used to call this option in the command line.
 	 * @param alias An alias for the name.
 	 */
 	public NoOption(Parser parser, Object defaultValue, String name) {
 		super(parser, defaultValue, name, new BooleanParameter());
 
 	}
 
 	// PUBLIC METHODS
 
 	/**
 	 * Parses that string that represents the arguments and set its value to the option.
 	 * This Option, do not needs an arguments. If any is found an IncorrectParameterTypeException is thrown.  
 	 * Calling this option, assumes that the return value is true.
 	 * 
 	 * @param string Is a string that represents the arguments for an option.
 	 * @throws IncorrectParameterTypeException
 	 */
 	@Override
 	public void setValue(String value) throws IncorrectParameterTypeException {
 		if (!value.trim().isEmpty()) { throw new IncorrectParameterTypeException ("Option " + this.getName() + " do not need any argument"); }
 		this.value = true;
 	}
 	/**
 	 * 
 	 * @return the value of the option
 	 */
 	public boolean getValue(){
		if (this.value==null) return (Boolean) this.defaultValue;
 		return this.value;
 	}
 }
