 package com.alexrnl.commons.arguments;
 
 import java.io.PrintStream;
 import java.lang.reflect.Field;
 import java.util.Arrays;
 import java.util.SortedSet;
 import java.util.TreeSet;
 import java.util.logging.Logger;
 
 import com.alexrnl.commons.utils.StringUtils;
 import com.alexrnl.commons.utils.object.ReflectUtils;
 
 /**
  * Class which allow to parse string arguments and store them in a target object.
  * @author Alex
  */
 public class Arguments {
 	/** Logger */
 	private static Logger				lg							= Logger.getLogger(Arguments.class.getName());
 	
 	/** The tab character */
 	private static final Character		TAB							= '\t';
 	/** The number of tabs between the name of the argument and their description */
 	private static final int			NB_TABS_BEFORE_DESCRIPTION	= 4;
 	
 	/** The name of the program. */
 	private final String				programName;
 	/** The object which holds the target */
 	private final Object				target;
 	/** The list of parameters in the target */
 	private final SortedSet<Parameter>	parameters;
 	
 	/**
 	 * Constructor #1.<br />
 	 * @param programName
 	 *        the name of the program.
 	 * @param target
 	 *        the object which holds the target.
 	 */
 	public Arguments (final String programName, final Object target) {
 		super();
 		this.programName = programName;
 		this.target = target;
 		this.parameters = retrieveParameters(target);
 	}
 	
 	/**
 	 * Parse and build a list with the parameters field marked with the {@link Param} annotation in
 	 * the object specified.
 	 * @param obj
 	 *        the object to parse for parameters.
 	 * @return the list of parameters associated to this object.
 	 */
 	private static SortedSet<Parameter> retrieveParameters (final Object obj) {
 		final SortedSet<Parameter> params = new TreeSet<>();
 		for (final Field field : ReflectUtils.retrieveFields(obj.getClass(), Param.class)) {
 			field.setAccessible(true);
 			params.add(new Parameter(field, field.getAnnotation(Param.class)));
 		}
 		return params;
 	}
 	
 	/**
 	 * Parse the arguments and set the target in the target object.
 	 * @param arguments
 	 *        the arguments to parse.
 	 */
 	public void parse (final String... arguments) {
 		parse(Arrays.asList(arguments));
 	}
 	
 	/**
 	 * Parse the arguments and set the target in the target object.
 	 * @param arguments
 	 *        the arguments to parse.
 	 */
 	public void parse (final Iterable<String> arguments) {
 		// TODO
 	}
 	
 	/**
 	 * Display the parameter's usage on specified output.
 	 * @param out
 	 *        the stream to use to display the parameters.
 	 */
 	public void usage (final PrintStream out) {
 		out.println(this);
 	}
 	
 	/**
 	 * Display the parameter's usage on the standard output.
 	 */
 	public void usage () {
 		usage(System.out);
 	}
 	
 	@Override
 	public String toString () {
 		final StringBuilder usage = new StringBuilder(programName).append(" usage as follow:");
 		for (final Parameter param : parameters) {
			usage.append(StringUtils.NEW_LINE).append(TAB);
 			if (!param.isRequired()) {
 				usage.append('[');
 			} else {
 				usage.append(' ');
 			}
 			usage.append("  ").append(StringUtils.separateWith(", ", param.getNames()));
 			int nbTabs = NB_TABS_BEFORE_DESCRIPTION;
 			while (nbTabs-- > 0) {
 				usage.append(TAB);
 			}
 			usage.append(param.getDescription());
			if (!param.isRequired()) {
				usage.append("  ]");
			}
 		}
 		return usage.toString();
 	}
 }
