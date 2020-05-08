 package starter;
 
 import java.lang.reflect.Method;
 
 import utils.cmd.line.parser.CmdLineParser;
 import utils.cmd.line.parser.CmdLineParser.IllegalOptionValueException;
 import utils.cmd.line.parser.CmdLineParser.Option;
 import utils.cmd.line.parser.CmdLineParser.UnknownOptionException;
 
 
 public enum Argument {
 
 	HELP ("help", Boolean.FALSE, Boolean.class),
 	YEARS ('y',"years", new Integer(1), Integer.class),
 	CURRENT_EXPERIMENT ('e', "cur_experiment", new Integer(-1), Integer.class),
 	NUMBER_OF_EXPERIMENTS ('E', "number_of_experiments", new Integer(-1), Integer.class),
 	CAPACITY_MULTIPLIER ('M', "capacity_multiplier", new Double(1), Double.class),
 	ZONE_MULTIPLIER ('z', "zone_multiplier", new Integer(1), Integer.class),
 	PROJECT_PATH ("project_path", Shared.PROJECT_PATH, String.class),
 	VIABILITY ("viability", Shared.DEFAULT_VIABILITY_FILE, String.class),
 	POSTERITY ("posterity", Shared.DEFAULT_POSTERITY_FILE, String.class),
 	MOVE_POSSIBILITIES ("map", Shared.DEFAULT_MAP_FILE, String.class),
 	SCENARIO ("scenario", Shared.DEFAULT_SCENARIO_FILE, String.class),
 	INITIATION ("initiation", Shared.DEFAULT_INITIATION_FILE, String.class),
 	STATISTIC ('S', "statistic", Shared.DEFAULT_STATISTIC_MODE, String.class),
 	EXPERIMENTS_SERIES_NAME ("name", Shared.DEFAULT_NAME, String.class),
 	POINT_NUMBER('p', "point", new Integer(-1), Integer.class),
 	DIMENSIONS_TO_TEST("dimensions", Shared.DEFAULT_DIMENSIONS_TO_TEST, String.class);
 	
 	private static CmdLineParser parser;
 	private Character shortName;
 	private String fullName;
 	private Object defaultValue;
 	private Option option;
 	private Class<?> valueClass;
	private Object value=null;
 	
 	static {
 		parser = new CmdLineParser();
 		for (Argument argument : Argument.values()) {
 			try {
 				if (argument.shortName == null) {
 					Method addMethod = getShortAddMethod(argument.valueClass);
 					argument.option = (Option) addMethod.invoke(parser, argument.fullName);
 				}
 				else {
 					Method addMethod = getLongAddMethod(argument.valueClass);
 					argument.option = (Option) addMethod.invoke(parser, argument.shortName, argument.fullName);
 				}
 			}
 			catch (Exception e) {
 				Shared.problemsLogger.error(Shared.printStack(e));
 				e.printStackTrace();
 			}
 		}
 	
 	}
 		
 	private Argument(char shortName, String fullName, Object defaultValue, Class<?> valueClass) {
 		this.shortName = shortName;
 		this.fullName = fullName;
 		this.defaultValue = defaultValue;
 		this.valueClass = valueClass;
 	}
 	
 	private Argument(String fullName, Object defaultValue, Class<?> valueClass) {
 		this.shortName = null;
 		this.fullName = fullName;
 		this.defaultValue = defaultValue;
 		this.valueClass = valueClass;
 	}
 	
 	private static Method getShortAddMethod(Class<?> valueClass) throws NoSuchMethodException, SecurityException {
 		if (valueClass.equals(Boolean.class))
 			return CmdLineParser.class.getMethod("addBooleanOption", String.class);
 		if (valueClass.equals(Integer.class))
 			return CmdLineParser.class.getMethod("addIntegerOption", String.class);
 		if (valueClass.equals(Double.class))
 			return CmdLineParser.class.getMethod("addDoubleOption", String.class);
 		if (valueClass.equals(String.class))
 			return CmdLineParser.class.getMethod("addStringOption", String.class);
 		return null;
 	}
 	
 	private static Method getLongAddMethod(Class<?> valueClass) throws NoSuchMethodException, SecurityException {
 		if (valueClass.equals(Boolean.class))
 			return CmdLineParser.class.getMethod("addBooleanOption", char.class, String.class);
 		if (valueClass.equals(Integer.class))
 			return CmdLineParser.class.getMethod("addIntegerOption", char.class, String.class);
 		if (valueClass.equals(Double.class))
 			return CmdLineParser.class.getMethod("addDoubleOption", char.class, String.class);
 		if (valueClass.equals(String.class))
 			return CmdLineParser.class.getMethod("addStringOption", char.class, String.class);
 		return null;
 	}
 	
 	public static void parse(String[] arguments) throws IllegalOptionValueException, UnknownOptionException {
 		parser.parse(arguments);
 	}
 
 	public Object getValue() {
		if (value == null)
			value = parser.getOptionValue(option, defaultValue);
		return value;
 	}
 	
 }
