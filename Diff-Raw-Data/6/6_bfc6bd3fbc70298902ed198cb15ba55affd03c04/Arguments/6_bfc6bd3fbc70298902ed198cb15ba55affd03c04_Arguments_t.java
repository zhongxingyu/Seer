 package com.alexrnl.commons.arguments;
 
 import java.io.PrintStream;
 import java.lang.reflect.Field;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 import java.util.SortedSet;
 import java.util.TreeSet;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import com.alexrnl.commons.arguments.parsers.ByteParser;
 import com.alexrnl.commons.arguments.parsers.CharParser;
 import com.alexrnl.commons.arguments.parsers.ClassParser;
 import com.alexrnl.commons.arguments.parsers.DoubleParser;
 import com.alexrnl.commons.arguments.parsers.FloatParser;
 import com.alexrnl.commons.arguments.parsers.IntParser;
 import com.alexrnl.commons.arguments.parsers.LongParser;
 import com.alexrnl.commons.arguments.parsers.ParameterParser;
 import com.alexrnl.commons.arguments.parsers.PathParser;
 import com.alexrnl.commons.arguments.parsers.ShortParser;
 import com.alexrnl.commons.arguments.parsers.StringParser;
 import com.alexrnl.commons.arguments.parsers.WByteParser;
 import com.alexrnl.commons.arguments.parsers.WCharParser;
 import com.alexrnl.commons.arguments.parsers.WDoubleParser;
 import com.alexrnl.commons.arguments.parsers.WFloatParser;
 import com.alexrnl.commons.arguments.parsers.WIntegerParser;
 import com.alexrnl.commons.arguments.parsers.WLongParser;
 import com.alexrnl.commons.arguments.parsers.WShortParser;
 import com.alexrnl.commons.error.ExceptionUtils;
 import com.alexrnl.commons.utils.StringUtils;
 import com.alexrnl.commons.utils.object.ReflectUtils;
 
 /**
  * Class which allow to parse string arguments and store them in a target object.
  * @author Alex
  */
 public class Arguments {
 	/** Logger */
 	private static Logger							lg							= Logger.getLogger(Arguments.class.getName());
 	
 	/** The tab character */
 	private static final Character					TAB							= '\t';
 	/** The number of tabs between the name of the argument and their description */
 	private static final int						NB_TABS_BEFORE_DESCRIPTION	= 4;
 	/** The short name for the help command */
 	public static final String						HELP_SHORT_NAME				= "-h";
 	/** The long name for the help command */
 	public static final String						HELP_LONG_NAME				= "--help";
 	/** The default parameter parsers */
 	private static final List<ParameterParser>		DEFAULT_PARSERS;
 	static {
 		DEFAULT_PARSERS = Collections.unmodifiableList(Arrays.asList(new ParameterParser[] {
 				// primitive types
 				new ByteParser(),
 				new CharParser(),
 				new DoubleParser(),
 				new FloatParser(),
 				new IntParser(),
 				new LongParser(),
 				new ShortParser(),
 				// wrappers
 				new WByteParser(),
 				new WCharParser(),
 				new WDoubleParser(),
 				new WFloatParser(),
 				new WIntegerParser(),
 				new WLongParser(),
 				new WShortParser(),
 				// others
 				new StringParser(),
 				new ClassParser(),
 				new PathParser()
 		}));
 		
 	}
 	
 	/** The name of the program */
 	private final String							programName;
 	/** The object which holds the target */
 	private final Object							target;
 	/** The list of parameters in the target */
 	private final SortedSet<Parameter>				parameters;
 	/** The output to use to display the usage of the arguments TODO replace with custom interface? */
 	private final PrintStream						out;
 	/** Map with the parameter parsers to use */
 	private final Map<Class<?>, ParameterParser>	parsers;
 	
 	/**
 	 * Constructor #1.<br />
 	 * @param programName
 	 *        the name of the program.
 	 * @param target
 	 *        the object which holds the target.
 	 */
 	public Arguments (final String programName, final Object target) {
 		this(programName, target, System.out);
 	}
 
 	/**
 	 * Constructor #1.<br />
 	 * @param programName
 	 *        the name of the program.
 	 * @param target
 	 *        the object which holds the target.
 	 * @param out
 	 *        the output stream to use for displaying argument's usage.
 	 */
 	public Arguments (final String programName, final Object target, final PrintStream out) {
 		super();
 		this.programName = programName;
 		this.target = target;
 		this.parameters = Collections.unmodifiableSortedSet(retrieveParameters(target));
 		this.out = out;
 		this.parsers = new HashMap<>();
 		for (final ParameterParser parser : DEFAULT_PARSERS) {
 			if (addParameterParser(parser)) {
 				lg.warning("Default parsers override each other for class " + parser.getFieldType());
 			}
 		}
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
 			final Parameter param = new Parameter(field, field.getAnnotation(Param.class));
 			if (params.contains(param)) {
 				lg.warning("Could not add the parameter " + param.getNames() + ". The probable " +
 						"cause is that the first name has already been used by another parameter");
 				continue;
 			}
 			params.add(param);
 		}
 		return params;
 	}
 	
 	/**
 	 * Add a parameter parser to the current arguments.
 	 * @param parser
 	 *        the parser to add.
 	 * @return <code>true</code> if a previous parser was already set for this field type.
 	 * @see ParameterParser
 	 */
 	public boolean addParameterParser (final ParameterParser parser) {
 		final boolean override = parsers.containsKey(parser.getFieldType());
 		parsers.put(parser.getFieldType(), parser);
 		return override;
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
 	 * @throws IllegalArgumentException
 	 *         if there is an error (or several) during parsing the arguments.
 	 */
 	public void parse (final Iterable<String> arguments) throws IllegalArgumentException {
 		if (lg.isLoggable(Level.INFO)) {
 			lg.info("Parsing arguments " + arguments.toString());
 		}
 		
 		final Set<Parameter> requiredParameters = getRequiredParameters();
 		initializeBooleansParameters();
 		final List<String> errors = new LinkedList<>();
 		
 		// Parse arguments provided
 		final Iterator<String> iterator = arguments.iterator();
 		boolean helpRequested = false;
 		while (iterator.hasNext()) {
 			final String argument = iterator.next();
 			if (lg.isLoggable(Level.INFO)) {
 				lg.info("Processing argument " + argument);
 			}
 			
 			if (isHelp(argument)) {
 				helpRequested = true;
 				continue;
 			}
 			
 			final Parameter currentParameter = getParameterByName(argument);
 			if (currentParameter == null) {
 				errors.add("No parameter with name " + argument + " found.");
 				continue;
 			}
 			
 			// If the parameter is a boolean, then its presence is enough
 			final Class<?> parameterType = currentParameter.getField().getType();
 			if (checkBooleanParameter(currentParameter, requiredParameters)) {
 				continue;
 			}
 			
 			// The parameter will take the next argument as value
 			if (!iterator.hasNext()) {
 				errors.add("No value found for parameter " + argument + ".");
 				continue;
 			}
 			final String value = iterator.next();
 			if (parsers.containsKey(parameterType)) {
 				try {
 					parsers.get(parameterType).parse(target, currentParameter.getField(), value);
 					requiredParameters.remove(currentParameter);
 				} catch (final IllegalArgumentException e) {
					errors.add("Value " + value + " could not be assigned to parameter " + argument);
 					lg.warning("Parameter " + argument + " value could not be set: "
 							+ ExceptionUtils.display(e));
 				}
 			} else {
				errors.add("No parser found for type " + parameterType.getName() + " (parameter "
						+ argument + ").");
 			}
 		}
 		
 		errorAndHelpProcessing(requiredParameters, errors, helpRequested);
 	}
 
 	/**
 	 * Check if the name of the argument is an alias of the help command.
 	 * @param name
 	 *        the name to check.
 	 * @return <code>true</code> if the argument is the help command.
 	 */
 	private static boolean isHelp (final String name) {
 		return HELP_SHORT_NAME.equals(name) || HELP_LONG_NAME.equals(name);
 	}
 	/**
 	 * Build a set with the reference of required parameters.
 	 * @return the required parameters of the target.
 	 */
 	private Set<Parameter> getRequiredParameters () {
 		final Set<Parameter> requiredParameters = new HashSet<>(parameters.size(), 1.0f);
 		for (final Parameter parameter : parameters) {
 			if (parameter.isRequired()) {
 				requiredParameters.add(parameter);
 			}
 		}
 		return requiredParameters;
 	}
 
 	/**
 	 * Initialize all Booleans parameters to false.
 	 */
 	private void initializeBooleansParameters () {
 		for (final Parameter parameter : parameters) {
 			if (parameter.getField().getType().equals(Boolean.class)) {
 				try {
 					parameter.getField().set(target, Boolean.FALSE);
 				} catch (IllegalArgumentException | IllegalAccessException e) {
 					lg.warning("Parameter " + parameter.getNames() + " value could not be " +
 							"initialize to false: " + ExceptionUtils.display(e));
 				}
 			}
 		}
 	}
 	
 	/**
 	 * Find a parameter according to its name.
 	 * @param name
 	 *        the name of the parameter to find.
 	 * @return the parameter which has the specified name.
 	 */
 	private Parameter getParameterByName (final String name) {
 		for (final Parameter parameter : parameters) {
 			if (parameter.getNames().contains(name)) {
 				return parameter;
 			}
 		}
 		return null;
 	}
 	
 	/**
 	 * Check if the current parameter is a boolean and process it accordingly.
 	 * @param parameter
 	 *        the parameter to process.
 	 * @param requiredParameters
 	 *        the list of required parameter, updated if necessary.
 	 * @return <code>true</code> if the parameter was a boolean parameter and has been processed.
 	 */
 	private boolean checkBooleanParameter (final Parameter parameter, final Set<Parameter> requiredParameters) {
 		final Class<?> type = parameter.getField().getType();
 		if (type.equals(Boolean.class) || type.equals(boolean.class)) {
 			try {
 				if (type.equals(boolean.class)) {
 					parameter.getField().setBoolean(target, true);
 				} else {
 					parameter.getField().set(target, Boolean.TRUE);
 				}
 				requiredParameters.remove(parameter);
 			} catch (IllegalArgumentException | IllegalAccessException e) {
 				lg.warning("Parameter " + parameter.getNames() + " boolean value could not be set: "
 						+ ExceptionUtils.display(e));
 			}
 			return true;
 		}
 		return false;
 	}
 
 	/**
 	 * Post processing of argument.<br />
 	 * Display error messages and/or help if required, and throw an {@link IllegalArgumentException}
 	 * if the parsing failed.
 	 * @param requiredParameters
 	 *        the set with the required parameters which were not set by the arguments provided.
 	 * @param errors
 	 *        the errors messages generated by the current set of arguments.
 	 * @param helpRequested
 	 *        if the help was requested.
 	 * @throws IllegalArgumentException
 	 *         if the parsing of arguments has failed.
 	 */
 	private void errorAndHelpProcessing (final Set<Parameter> requiredParameters,
 			final List<String> errors, final boolean helpRequested) throws IllegalArgumentException {
 		// Check that all required arguments were set
 		if (!requiredParameters.isEmpty()) {
 			final List<Set<String>> listParamNames = new ArrayList<>(requiredParameters.size());
 			for (final Parameter parameter : requiredParameters) {
 				listParamNames.add(parameter.getNames());
 			}
 			errors.add("The following parameters were not set: "
 					+ StringUtils.separateWith(", ", listParamNames) + ".");
 		}
 		
 		IllegalArgumentException e = null;
 		if (!errors.isEmpty() && !helpRequested) {
 			final String errorsMessage = StringUtils.separateWith("\n", errors);
 			out.println(errorsMessage);
 			e = new IllegalArgumentException(errorsMessage);
 		}
 		if (helpRequested || !errors.isEmpty()) {
 			usage();
 		}
 		if (e != null) {
 			throw e;
 		}
 	}
 
 	/**
 	 * Display the parameter's usage on the output specified in the constructor.
 	 */
 	public void usage () {
 		out.println(this);
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
