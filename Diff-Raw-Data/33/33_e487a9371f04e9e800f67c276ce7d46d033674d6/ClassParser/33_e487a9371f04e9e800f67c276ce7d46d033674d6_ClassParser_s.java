 package com.alexrnl.commons.arguments.parsers;
 
 import java.util.logging.Logger;
 
 import com.alexrnl.commons.error.ExceptionUtils;
 
 /**
  * Parser for the {@link Class} field type.
  * @author Alex
  */
 public class ClassParser extends AbstractParser<Class> {
 	/** Logger */
 	private static Logger	lg	= Logger.getLogger(ClassParser.class.getName());
 	
 	/**
 	 * Constructor #1.<br />
 	 */
 	public ClassParser () {
 		super(Class.class);
 	}
 
 	@Override
 	protected Class getValue (final String parameter) throws IllegalArgumentException {
 		try {
 			return Class.forName(parameter);
 		} catch (final ClassNotFoundException e) {
 			lg.warning("Could not find class " + parameter + ": " + ExceptionUtils.display(e));
 			throw new IllegalArgumentException("Could not find class " + parameter, e);
 		}
 	}
 }
