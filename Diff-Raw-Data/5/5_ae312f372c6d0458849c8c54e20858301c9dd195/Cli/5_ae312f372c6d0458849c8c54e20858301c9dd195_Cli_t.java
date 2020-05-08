 /*
  * dfh.cli -- a command line argument parsing library for Java
  * 
  * Copyright (C) 2012 David F. Houghton
  * 
  * This software is licensed under the LGPL. Please see accompanying NOTICE file
  * and lgpl.txt.
  */
 package dfh.cli;
 
 import java.io.BufferedInputStream;
 import java.io.ByteArrayOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.PrintStream;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.HashSet;
 import java.util.LinkedHashMap;
 import java.util.LinkedHashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Map.Entry;
 import java.util.Set;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 /**
  * For parsing CLI ARGS.
  * <p>
  * Note that this object is reusable but not synchronized. If you're going to be
  * parsing and reparsing in different threads, you'll need to add some
  * synchronization.
  * <p>
  * <b>Creation date:</b> Nov 4, 2011
  * 
  * @author David Houghton
  * 
  */
 public class Cli {
 	/**
 	 * If provided as the first element of a specification array, these types
 	 * determine how the remainder of the array will be parsed.
 	 * 
 	 * @author David Houghton
 	 * 
 	 */
 	public enum Opt {
 		/**
 		 * executable NAME
 		 */
 		NAME,
 		/**
 		 * the elements of this array are the names of arguments
 		 */
 		ARGS,
 		/**
 		 * if provided in the {@link #ARGS} argument list, the next String is
 		 * the NAME given to any additional arguments
 		 */
 		STAR,
 		/**
 		 * At least one slurpy argument required.
 		 */
 		PLUS,
 		/**
 		 * The next string will be taken as a usage abstract. If a third element
 		 * appears in the array, it will be taken as the long usage text.
 		 * Otherwise, if a second array is provided, usage text will be sought
 		 * here. A string will be taken as a resource name. This resource will
 		 * be loaded via the class's class loader. Alternatively, an
 		 * {@link InputStream} may be provided as the usage text source.
 		 */
 		USAGE,
 		/**
 		 * Marks a line of text to be inserted among the lines of the command
 		 * descriptions in the usage information. The second element in this
 		 * array, if present, should be a string providing the text to insert.
 		 * If no string is present, a blank line will be inserted.
 		 */
 		TEXT,
 		/**
 		 * Specifies version number, which will be reported with the --version
 		 * option. The version number is actually an arbitrary {@link String}:
 		 * whatever one gets when one calls <code>toString()</code> on the
 		 * objection immediately following this {@link Opt} in the
 		 * specification.
 		 */
 		VERSION,
 	};
 
 	/**
 	 * Constants that can be used to control the parsing of command options.
 	 * <p>
 	 * <b>Creation date:</b> Nov 7, 2011
 	 * 
 	 * @author David Houghton
 	 * 
 	 */
 	public enum Mod {
 		/**
 		 * Auto-generate help text. See {@link Cli#PREFERRED_HELP_FLAGS} and
 		 * {@link Cli#AUXILIARY_HELP_FLAGS}. If none of these is available, a
 		 * {@link ValidationException} is thrown.
 		 */
 		HELP,
 		/**
 		 * Throw a {@link RuntimeException} when option parsing or validation
 		 * fails rather than calling {@link System#exit(int)}.
 		 */
 		THROW_EXCEPTION
 	}
 
 	/**
 	 * Restriction modifiers. These may appear in the third sub-array in an
 	 * option specification alongside {@link ValidationRule ValidationRules}.
 	 * <p>
 	 * 
 	 * @author David F. Houghton - Feb 20, 2012
 	 * 
 	 */
 	public enum Res {
 		/**
 		 * Option so marked defines a member of a list
 		 */
 		REPEATABLE,
 		/**
 		 * Option so marked must be specified on command line
 		 */
 		REQUIRED,
 		/**
 		 * Option so marked defines a unique member of a collection
 		 */
 		SET
 	}
 
 	/**
 	 * If the {@link Mod#HELP} modifier is provided to
 	 * {@link Cli#Cli(Object[][][], Mod...)}, these are the preferred flags to
 	 * trigger help.
 	 */
 	public static final String[] PREFERRED_HELP_FLAGS = { "help", "h", "?" };
 	/**
 	 * If the {@link Mod#HELP} modifier is provided to
 	 * {@link Cli#Cli(Object[][][], Mod...)} and none of
 	 * {@link #PREFERRED_HELP_FLAGS} is available, these are the preferred flags
 	 * to trigger help.
 	 */
 	public static final String[] AUXILIARY_HELP_FLAGS = { "usage", "info",
 			"how-to-use" };
 	/**
 	 * If the executable version is provided via a {@link Opt#VERSION} line,
 	 * these are the flags to trigger the version command.
 	 */
 	public static final String[] PREFERRED_VERSION_FLAGS = { "version", "v" };
 	/**
 	 * The pattern command names must obey: some string of word characters (\w)
 	 * or hyphens neither beginning nor ending in a hyphen or an underscore.
 	 */
 	public static final Pattern COMMAND_NAME_PATTERN = Pattern.compile(
 			"[a-z0-9][\\w-]*(?<![_-])", Pattern.CASE_INSENSITIVE);
 	/**
 	 * Pattern which, if matched, causes a semicolon to be inserted before
 	 * default value or "required" in option description.
 	 */
 	public static final Pattern NEEDS_SEMICOLON = Pattern
 			.compile("[\\p{L}\\p{N}_\\s]$");
 
 	private List<String> argList;
 	private LinkedHashMap<String, Integer> argNames = new LinkedHashMap<String, Integer>();
 	private boolean isSlurpy = true;
 	private Map<String, Option<?>> options = new LinkedHashMap<String, Option<?>>();
 	private BooleanOption helpOption = null, versionOption = null;
 	private List<String> errors = new ArrayList<String>();
 	private String name = "EXECUTABLE";
 	private String usage = "", abstr = "";
 	private boolean argsSpecified = false;
 	private boolean nameSpecified = false;
 	private boolean usageSpecified = false;
 	private boolean slurpRequired = false;
 	private boolean throwException = false;
 	private String version = "undefined";
 	private boolean parsed = false;
 	private static Pattern linePattern = Pattern.compile("^.*$",
 			Pattern.MULTILINE);
 	private int margin = 80;
 
 	public Cli(Object[][][] spec, Mod... mods) {
 		for (Object[][] cmd : spec) {
 			try {
 				parseSpec(cmd);
 			} catch (ValidationException e) {
 				errors.add(e.getMessage());
 			}
 		}
 		boolean hasHelp = false;
 		for (Mod m : mods) {
 			if (m == Mod.THROW_EXCEPTION)
 				throwException = true;
 			else if (m == Mod.HELP)
 				hasHelp = true;
 		}
 		// add blank line
 		if ((hasHelp || versionOption != null) && !options.isEmpty())
 			options.put("_" + options.size(), new DummyOption(""));
 		if (versionOption != null) {
 			boolean added = false;
 			versionOption.setDescription("print " + name + " version");
 			try {
 				for (String s : PREFERRED_VERSION_FLAGS)
 					added = addAuxiliary(versionOption, s) || added;
 			} catch (ValidationException e) {
 				errors.add(e.getMessage());
 			}
 			if (!added) {
 				StringBuilder b = new StringBuilder();
 				b.append("could not version help command under any of the following names: ");
 				boolean nonInitial = false;
 				for (String s : PREFERRED_VERSION_FLAGS) {
 					if (nonInitial)
 						b.append(", ");
 					else
 						nonInitial = true;
 					b.append(s);
 				}
 				errors.add(b.toString());
 			}
 		}
 		if (hasHelp) {
 			boolean added = false;
 			helpOption = new BooleanOption();
 			helpOption.setDescription("print usage information");
 			try {
 				for (String s : PREFERRED_HELP_FLAGS)
 					added = addAuxiliary(helpOption, s) || added;
 				if (!added) {
 					for (String auxHelp : AUXILIARY_HELP_FLAGS) {
 						added = addAuxiliary(helpOption, auxHelp) || added;
 						if (added)
 							break;
 					}
 				}
 			} catch (ValidationException e) {
 				errors.add(e.getMessage());
 			}
 			if (!added) {
 				StringBuilder b = new StringBuilder();
 				b.append("could not add help command under any of the following names: ");
 				boolean nonInitial = false;
 				for (String s : PREFERRED_HELP_FLAGS) {
 					if (nonInitial)
 						b.append(", ");
 					else
 						nonInitial = true;
 					b.append(s);
 				}
 				for (String s : AUXILIARY_HELP_FLAGS)
 					b.append(", ").append(s);
 				errors.add(b.toString());
 			}
 		}
 		if (!errors.isEmpty())
 			usage(1);
 	}
 
 	protected boolean addAuxiliary(BooleanOption opt, String s)
 			throws ValidationException {
 		if (!options.containsKey(s)) {
 			opt.addName(s);
 			options.put(s, opt);
 			return true;
 		}
 		return false;
 	}
 
 	private void parseSpec(Object[][] cmd) throws ValidationException {
 		if (cmd.length == 0)
 			throw new ValidationException("empty specification row");
 		if (cmd[0][0] instanceof Opt) {
 			Opt o = (Opt) cmd[0][0];
 			switch (o) {
 			case NAME:
 				if (nameSpecified)
 					throw new ValidationException(
 							"executable name specified more than once");
 				nameSpecified = true;
 				if (cmd[0].length > 2)
 					throw new ValidationException(
 							"too many arguments in NAME specification line");
 				if (cmd[0].length == 1)
 					throw new ValidationException("no NAME provided");
 				if (!(cmd[0][1] instanceof String))
 					throw new ValidationException(
 							"NAME argument in specification must be a string");
 				name = cmd[0][1].toString();
 				break;
 			case ARGS:
 				if (argsSpecified)
 					throw new ValidationException(
 							"argument list specified more than once");
 				argsSpecified = true;
 				isSlurpy = false;
 				for (int i = 1; i < cmd[0].length; i++) {
 					Object next = cmd[0][i];
 					if (next instanceof Opt) {
 						if (next == Opt.STAR || next == Opt.PLUS) {
 							if (isSlurpy)
 								throw new ValidationException(
 										"only specify one of " + Opt.STAR
 												+ " or " + Opt.PLUS
 												+ " once in argument list");
 							isSlurpy = true;
 							slurpRequired = next == Opt.PLUS;
 						} else
 							throw new ValidationException("only " + Opt.STAR
 									+ " or " + Opt.PLUS
 									+ " can be specified in argument list");
 					} else if (next instanceof String) {
 						if (isSlurpy)
 							throw new ValidationException(
 									"no argument names should be provided after "
 											+ Opt.STAR);
 						String argName = (String) next;
 						if (argNames.containsKey(argName))
 							throw new ValidationException(
 									"duplicate argument name: " + argName);
 						argNames.put(argName, argNames.size());
 					}
 				}
 				break;
 			case USAGE:
 				if (usageSpecified)
 					throw new ValidationException(
 							"usage details specified more than once");
 				usageSpecified = true;
 				boolean usageDefined = false;
 				if (cmd[0].length > 1) {
 					if (cmd[0][1] instanceof String)
 						abstr = (String) cmd[0][1];
 					else
 						throw new ValidationException(
 								"abstract must be provided as a String");
 					if (cmd[0].length > 2) {
 						if (cmd[0][2] instanceof String)
 							usage = (String) cmd[0][2];
 						else
 							throw new ValidationException(
 									"usage must be provided as a String");
 						usageDefined = true;
 					}
 				}
 				if (!usageDefined) {
 					if (cmd.length > 1 && cmd[1].length > 0) {
 						InputStream is = null;
 						if (cmd[1][0] instanceof String) {
 							try {
 								is = Cli.class
 										.getClassLoader()
 										.getResourceAsStream((String) cmd[1][0]);
 							} catch (Exception e) {
 								throw new ValidationException(
 										"unable to load usage details from resource "
 												+ e);
 							}
 						} else if (cmd[1][0] instanceof InputStream) {
 							is = (InputStream) cmd[1][0];
 						}
 						try {
 							BufferedInputStream bis = new BufferedInputStream(
 									is);
 							ByteArrayOutputStream baos = new ByteArrayOutputStream();
 							byte[] buf = new byte[1024];
 							int count;
 							while ((count = bis.read(buf)) > -1) {
 								baos.write(buf, 0, count);
 							}
 							bis.close();
 							usage = new String(baos.toByteArray());
 						} catch (IOException e) {
 							throw new ValidationException(e.toString());
 						}
 					}
 				}
 				if (!usageDefined && "".equals(abstr))
 					throw new ValidationException(
 							"no usage or abstract specified");
 				break;
 			case TEXT:
 				if (cmd.length > 1)
 					throw new ValidationException("ill-formed specification; "
 							+ o
 							+ " line should consist of a single element array");
 				DummyOption dummy = null;
 				if (cmd[0].length == 1) {
 					dummy = new DummyOption("");
 				} else if (cmd[0].length == 2) {
 					Object obj = cmd[0][1];
 					if (obj instanceof String)
 						dummy = new DummyOption((String) obj);
 					else
 						throw new ValidationException(
 								"ill-formed specification; second element of a "
 										+ o + " line should be a String");
 				} else
 					throw new ValidationException(
 							"ill-formed specification; second element of a "
 									+ o + " cannot have more than 2 elements");
 				options.put("_" + options.size(), dummy);
 				break;
 			case VERSION:
 				if (versionOption != null)
 					throw new ValidationException(
 							o
 									+ " must be included only once in the specification");
 				if (cmd.length > 1)
 					throw new ValidationException("ill-formed specification; "
 							+ o
 							+ " line should consist of a single element array");
 				if (cmd[0].length != 2)
 					throw new ValidationException(o
 							+ " line must contain two objects");
 				versionOption = new BooleanOption();
 				version = cmd[0][1].toString();
 				break;
 			default:
 				throw new ValidationException("dfh.cli broken; unknown enum "
 						+ o);
 			}
 		} else {
 			Option<?> opt = optionForType(cmd[0], cmd.length == 3 ? cmd[2]
 					: null);
 			for (String s : opt.longNames) {
 				registerName(opt, s);
 			}
 			for (Character ch : opt.shortNames) {
 				String s = ch.toString();
 				registerName(opt, s);
 			}
 			if (cmd.length > 1) {
 				Object[] descAndName = cmd[1];
 				if (descAndName != null && descAndName.length > 0) {
 					if (descAndName.length > 2)
 						throw new ValidationException(
 								"description and name element of spec for --"
 										+ opt.name
 										+ " must contain only a description and an optional argument name");
 					opt.setDescription(descAndName[0].toString());
 					if (descAndName.length == 2)
 						opt.setArgDescription(descAndName[1].toString());
 				}
 			}
 		}
 	}
 
 	protected void registerName(Option<?> opt, String s)
 			throws ValidationException {
 		if (COMMAND_NAME_PATTERN.matcher(s).matches()) {
 			Option<?> old = options.get(s);
 			if (!(old == null || old == opt))
 				throw new ValidationException("option " + s
 						+ " used for two distinct options: --" + opt.name
 						+ " and --" + old.name);
 			options.put(s, opt);
 		} else
 			throw new ValidationException("option name " + s
 					+ " violates option name pattern " + COMMAND_NAME_PATTERN);
 	}
 
 	private Option<?> optionForType(Object[] base, Object[] restrictions)
 			throws ValidationException {
 		boolean isSet = false, isRepeatable = false, isRequired = false;
 		if (restrictions != null) {
 			for (Object o : restrictions) {
 				if (o instanceof Res) {
 					switch ((Res) o) {
 					case SET:
 						isSet = true;
 					case REPEATABLE:
 						isRepeatable = true;
 						break;
 					case REQUIRED:
 						isRequired = true;
 						break;
 					default:
 						throw new ValidationException(
 								"unexpected Res constant: " + o);
 					}
 				}
 			}
 		}
 		Class<?> cz = Boolean.class;
 		Object def = null;
 		boolean findDefault = false;
 		for (Object o : base) {
 			if (findDefault) {
 				if (def != null)
 					throw new ValidationException(
 							"base spec can contain only a single default value after the type specification");
 				def = o;
 			} else if (o instanceof Class) {
 				cz = (Class<?>) o;
 				findDefault = true;
 			}
 		}
 		Option<?> opt = null;
 		if (cz.equals(Boolean.class)) {
 			if (isRepeatable)
 				throw new ValidationException(
 						"boolean options are not repeatable");
 			opt = new BooleanOption();
 		} else if (cz.equals(Integer.class)) {
 			if (isSet)
 				opt = new IntegerSetOption();
 			else if (isRepeatable)
 				opt = new IntegerListOption();
 			else
 				opt = new IntegerOption();
 		} else if (cz.equals(Number.class) || cz.equals(Double.class)
 				|| cz.equals(Float.class)) {
 			if (isSet)
 				opt = new NumberSetOption();
 			else if (isRepeatable)
 				opt = new NumberListOption();
 			else
 				opt = new NumberOption();
 		} else if (cz.equals(String.class)) {
 			if (isSet)
 				opt = new StringSetOption();
 			else if (isRepeatable)
 				opt = new StringListOption();
 			else
 				opt = new StringOption();
 		} else {
 			throw new ValidationException(
 					"spec parser cannot parse arguments of type " + cz);
 		}
 		opt.setRequired(isRequired);
 		for (Object o : base) {
 			if (o instanceof String || o instanceof Character)
 				opt.addName(o);
 			else if (o instanceof Class)
 				break;
 			else {
 				throw new ValidationException(
 						"spec base must consist of strings or characters, optionally followed by a class, optionally followed by a default value");
 			}
 		}
 		if (isRequired && cz.equals(Boolean.class))
 			throw new ValidationException(
 					"--"
 							+ opt.name
 							+ " is boolean and marked as required; these are incompatible");
 		if (isRequired && def != null)
 			throw new ValidationException(
 					"--"
 							+ opt.name
 							+ " is marked as required and has a default value; these are incompatible");
 		opt.setDefault(def);
 		if (restrictions != null) {
 			for (Object o : restrictions) {
 				if (o == null)
 					throw new ValidationException("null found in spec for --"
 							+ opt.name);
 				if (o instanceof Res)
 					continue;
 				if (o instanceof ValidationRule<?>)
 					opt.addValidationRule((ValidationRule<?>) o);
 				else
 					throw new ValidationException(
 							"third element in spec for --"
 									+ opt.name
 									+ " must contain only integers or ValidationRule rules");
 			}
 		}
 		return opt;
 	}
 
 	/**
 	 * Parse the command line arguments into a set of option values and named
 	 * arguments.
 	 * 
 	 * @param args
 	 *            command line arguments
 	 */
 	public void parse(String... args) {
 		if (parsed)
 			throw new IllegalStateException(
 					"parse called twice without clearing Cli");
 		argList = new ArrayList<String>(args.length);
 		boolean endCommands = false, mustStore = false;
 		Option<?> lastCommand = null;
 		for (String s : args) {
 			try {
 				if (endCommands)
 					argList.add(s);
 				else if (mustStore) {
 					lastCommand.store(s);
 					mustStore = false;
 				} else {
 					if (s.startsWith("-")) {
 						if (s.equals("--"))
 							endCommands = true;
 						else {
 							ExtractedCommand ec = extractCommands(s);
 							lastCommand = ec.opt;
 							mustStore = !ec.stored;
 						}
 					} else if (lastCommand == null) {
 						endCommands = true;
 						argList.add(s);
 					} else {
 						argList.add(s);
 						endCommands = true;
 						lastCommand = null;
 					}
 				}
 			} catch (ValidationException e) {
 				errors.add(e.getMessage());
 			}
 			if (lastCommand != null) {
 				if (lastCommand == helpOption)
 					usage(0);
 				if (lastCommand == versionOption)
 					version();
 			}
 		}
 		if (argNames.size() > 0) {
 			if (argNames.size() < argList.size()) {
 				if (!isSlurpy) {
 					StringBuilder b = new StringBuilder();
 					b.append("only expected arguments: ");
 					boolean nonInitial = false;
 					for (String s : argNames.keySet()) {
 						if (nonInitial)
 							b.append(", ");
 						else
 							nonInitial = true;
 						b.append('<').append(s).append('>');
 					}
 					errors.add(b.toString());
 				}
 			} else if (argNames.size() > argList.size()) {
 				List<String> nameList = new ArrayList<String>(argNames.keySet());
 				for (int i = argList.size(), lim = isSlurpy && !slurpRequired ? argNames
 						.size() - 1 : argNames.size(); i < lim; i++) {
 					errors.add("argument " + (i + 1) + ", <" + nameList.get(i)
 							+ ">, not defined");
 				}
 			}
 		}
 		for (Option<?> opt : new LinkedHashSet<Option<?>>(options.values())) {
 			try {
 				opt.validate();
 				if (opt.isRequired()) {
 					if (opt.value == null) {
 						if (opt.def == null)
 							errors.add("--" + opt.name
 									+ " is required but has no defined value");
 						else
 							opt.assignDefault();
 					}
 				}
 				opt.terminalValidation();
 			} catch (ValidationException e) {
 				errors.add("--" + opt.name + ": " + e.getMessage());
 			}
 		}
 		if (!errors.isEmpty())
 			usage(1);
 		parsed = true;
 	}
 
 	/**
 	 * Prints out version information.
 	 */
 	public void version() {
 		PrintStream out = null;
 		ByteArrayOutputStream baos = null;
 		if (throwException) {
 			baos = new ByteArrayOutputStream();
 			out = new PrintStream(baos);
 		} else
 			out = System.out;
 		out.printf("%s %s%n", name, version);
 		if (throwException) {
 			out.close();
 			throw new RuntimeException(new String(baos.toByteArray()));
 		}
 		System.exit(0);
 	}
 
 	/**
 	 * Prints usage information to default stream and exits either by throwing a
 	 * {@link RuntimeException}, if {@link Mod#THROW_EXCEPTION} was provided in
 	 * {@link Cli#Cli(Object[][][], Mod...)}, or by calling
 	 * {@link System#exit(int)}.
 	 * 
 	 * @param status
 	 *            if equals 0 is equivalent to calling <code>--help</code>;
 	 *            errors only output if <code>status</code> is greater than 0
 	 */
 	public void usage(int status) {
 		PrintStream out = null;
 		ByteArrayOutputStream baos = null;
 		if (throwException) {
 			baos = new ByteArrayOutputStream();
 			out = new PrintStream(baos);
 		} else
 			out = status == 0 ? System.out : System.err;
 		usage(status, out);
 		if (throwException) {
 			out.close();
 			throw new RuntimeException(new String(baos.toByteArray()));
 		}
 		System.exit(status);
 	}
 
 	/**
 	 * Print usage information to given {@link PrintStream}.
 	 * <p>
 	 * 
 	 * @param status
 	 *            if equals 0 is equivalent to calling <code>--help</code>;
 	 *            errors only output if <code>status</code> is greater than 0
 	 * @param out
 	 *            sink for usage information
 	 */
 	public void usage(int status, PrintStream out) {
 		if (!(status == 0 || errors.isEmpty())) {
 			out.println("ERRORS");
 			for (String error : errors) {
 				List<String> list = split(error, margin() - 4);
 				out.printf("    %s%n", list.get(0));
 				if (list.size() == 2)
 					out.println(wrap(list.get(1), 6, margin()));
 			}
 			out.println();
 		}
 		int c1 = 1, c2 = 1;
 		Set<Option<?>> optionSet = new LinkedHashSet<Option<?>>(
 				options.values());
 		for (Option<?> c : optionSet) {
 			if (c instanceof DummyOption)
 				continue;
 			String optDesc = c.optionDescription();
 			String argDescription = c.argDescription();
 			if (optDesc.length() > c1)
 				c1 = optDesc.length();
 			if (argDescription.length() > c2)
 				c2 = argDescription.length();
 		}
 		String format = "    %-" + c1 + "s %-" + c2 + "s  %s%s";
 		int indent = 4 + c1 + c2 + 3, splitIndent = Math.max(margin(), indent);
 		String u = String.format("USAGE: %s%s%s", name(), optDigest(),
 				argDigest());
 		List<String> header = split(u, margin());
 		out.println(header.get(0));
 		if (header.size() == 2)
 			out.println(wrap(header.get(1), 4, margin()));
 		out.println();
 		if (!"".equals(abstr)) {
 			out.println(wrap(abstr, 2, margin()));
 			out.println();
 		}
 		for (Option<?> c : optionSet) {
 			if (c instanceof DummyOption) {
 				String s = c.description().trim();
 				if (s.length() == 0)
 					out.println();
 				else {
 					out.println(wrap(s, 4, margin()));
 				}
 			} else {
 				String optDesc = c.optionDescription();
 				String argDescription = c.argDescription();
 				String suffix = "";
 				if (c.def != null || c.isRequired()) {
 					StringBuilder b = new StringBuilder();
 					if (NEEDS_SEMICOLON.matcher(c.description()).find())
 						b.append(';');
 					if (c.isRequired())
 						b.append(" REQUIRED");
 					else
 						b.append(" default: ").append(c.def);
 					suffix = b.toString();
 				}
 				String line = String.format(format, optDesc, argDescription,
 						c.description(), suffix);
 				List<String> parts = split(line, splitIndent);
 				out.println(parts.get(0));
 				if (parts.size() == 2)
 					out.println(wrap(parts.get(1), indent, margin()));
 			}
 		}
 		if (status == 0 && !"".equals(usage)) {
 			out.println();
 			out.println(wrap(usage, 0, margin()));
 		}
 	}
 
 	/**
 	 * Column width at which {@link Cli} will attempt to wrap usage information.
 	 * 
 	 * @return word wrapping width
 	 */
 	public int margin() {
 		return margin;
 	}
 
 	private String optDigest() {
 		if (options.isEmpty())
 			return "";
 		return " [options]";
 	}
 
 	private String argDigest() {
 		if (argNames.isEmpty())
 			return isSlurpy ? (slurpRequired ? " <arg>+" : " <arg>*") : "";
 		StringBuilder b = new StringBuilder();
 		for (String s : argNames.keySet())
 			b.append(" <").append(s).append('>');
 		if (isSlurpy)
 			b.append(slurpRequired ? '+' : '*');
 		return b.toString();
 	}
 
 	/**
 	 * @return whether cli takes any arguments after the options
 	 */
 	private boolean hasArguments() {
 		return isSlurpy || !argNames.isEmpty();
 	}
 
 	private String name() {
 		return name;
 	}
 
 	/**
 	 * Return value for {@link Cli#extractCommands(String)}.
 	 * <p>
 	 * 
 	 * @author David F. Houghton - Mar 15, 2012
 	 * 
 	 */
 	private class ExtractedCommand {
 		Option<?> opt;
 		boolean stored;
 
 		ExtractedCommand(Option<?> opt, boolean stored) {
 			this.opt = opt;
 			this.stored = stored;
 		}
 	}
 
 	private ExtractedCommand extractCommands(String s)
 			throws ValidationException {
 		Option<?> c = null;
 		String arg = null;
 		if (s.startsWith("--")) {
 			s = s.substring(2);
 			int i = s.indexOf('=');
 			if (i > -1) {
 				arg = s.substring(i + 1);
 				s = s.substring(0, i);
 			}
 			if (COMMAND_NAME_PATTERN.matcher(s).matches()) {
 				c = options.get(s);
 				if (c == null)
 					throw new ValidationException("unknown option --" + s);
 				c.mark();
 			} else
 				throw new ValidationException("unknown option --" + s);
 		} else {
 			String bundle = s.substring(1);
 			if (bundle.length() == 0)
 				throw new ValidationException("malformed option '-'");
 			for (int i = 0; i < bundle.length(); i++) {
 				String sc = bundle.substring(i, i + 1);
 				if (COMMAND_NAME_PATTERN.matcher(sc).matches()) {
 					c = options.get(sc);
 					if (c == null)
 						throw new ValidationException("unknown option -" + sc);
 					c.mark();
 				} else
 					throw new ValidationException("unknown option -" + sc);
 			}
 		}
 		boolean stored = c instanceof BooleanOption;
 		if (arg != null) {
 			stored = true;
 			c.store(arg);
 		}
 		return new ExtractedCommand(c, stored);
 	}
 
 	/**
 	 * Register an error to report. This is useful if you do additional
 	 * validation beyond what is provided in the spec and find that the
 	 * combination of options provided is inconsistent or unusable in some way.
 	 * 
 	 * @param message
 	 */
 	public void error(String message) {
 		errors.add(message);
 	}
 
 	public Boolean bool(String string) {
 		parseCheck();
 		if (options.containsKey(string)) {
 			Option<?> opt = options.get(string);
 			try {
 				BooleanOption bopt = (BooleanOption) opt;
 				return bopt.value();
 			} catch (ClassCastException e) {
 				throw new RuntimeException("--" + opt.name + " not boolean");
 			}
 		}
 		throw new RuntimeException("unknown option --" + string);
 	}
 
 	public Integer integer(String string) {
 		parseCheck();
 		if (options.containsKey(string)) {
 			Option<?> opt = options.get(string);
 			try {
 				IntegerOption iopt = (IntegerOption) opt;
 				return iopt.value();
 			} catch (ClassCastException e) {
 				throw new RuntimeException("--" + opt.name + " not integer");
 			}
 		}
 		throw new RuntimeException("unknown option --" + string);
 	}
 
 	private void parseCheck() {
 		if (!parsed)
 			throw new RuntimeException("no arguments have been parsed");
 	}
 
 	public Collection<Integer> integerCollection(String string) {
 		parseCheck();
 		if (options.containsKey(string)) {
 			Option<?> opt = options.get(string);
 			if (opt instanceof IntegerListOption)
 				return ((IntegerListOption) opt).value();
 			else if (opt instanceof IntegerSetOption)
 				return ((IntegerSetOption) opt).value();
 			throw new RuntimeException("--" + opt.name
 					+ " not collection of integers");
 		}
 		throw new RuntimeException("unknown option --" + string);
 	}
 
 	public Number number(String string) {
 		parseCheck();
 		if (options.containsKey(string)) {
 			Option<?> opt = options.get(string);
 			if (opt instanceof NumberOption)
 				return ((NumberOption) opt).value();
 			if (opt instanceof IntegerOption)
 				return ((IntegerOption) opt).value();
 			throw new RuntimeException("--" + opt.name + " not number");
 		}
 		throw new RuntimeException("unknown option --" + string);
 	}
 
 	public Collection<? extends Number> numberCollection(String string) {
 		parseCheck();
 		if (options.containsKey(string)) {
 			Option<?> opt = options.get(string);
 			if (opt instanceof IntegerListOption)
 				return ((IntegerListOption) opt).value();
 			if (opt instanceof IntegerSetOption)
 				return ((IntegerSetOption) opt).value();
 			if (opt instanceof NumberListOption)
 				return ((NumberListOption) opt).value();
 			if (opt instanceof NumberSetOption)
 				return ((NumberSetOption) opt).value();
 			throw new RuntimeException("--" + opt.name
 					+ " not number collection");
 		}
 		throw new RuntimeException("unknown option --" + string);
 	}
 
 	/**
 	 * Returns collection of values pertaining to a particular option. Throws a
 	 * {@link RuntimeException} if this request does not match the option specs.
 	 * 
 	 * @param string
 	 *            option name
 	 * @return set of type-erased values
 	 */
 	public Collection<?> collection(String string) {
 		parseCheck();
 		Option<?> opt = options.get(string);
 		if (opt != null) {
 			if (opt instanceof IntegerListOption
 					|| opt instanceof IntegerSetOption
 					|| opt instanceof StringListOption
 					|| opt instanceof StringSetOption
 					|| opt instanceof NumberListOption
 					|| opt instanceof NumberSetOption)
 				return (Collection<?>) opt.value();
 			throw new RuntimeException("--" + opt.name
 					+ " is not a repeatable option");
 		}
 		throw new RuntimeException("unknown option --" + string);
 	}
 
 	public String string(String string) {
 		parseCheck();
 		if (options.containsKey(string)) {
 			Option<?> opt = options.get(string);
 			try {
 				StringOption sopt = (StringOption) opt;
 				return sopt.value();
 			} catch (ClassCastException e) {
 				throw new RuntimeException("--" + opt.name + " not string");
 			}
 		}
 		throw new RuntimeException("unknown option --" + string);
 	}
 
 	public Collection<String> stringCollection(String string) {
 		parseCheck();
 		if (options.containsKey(string)) {
 			Option<?> opt = options.get(string);
 			if (opt instanceof StringListOption)
 				return ((StringListOption) opt).value();
 			else if (opt instanceof StringSetOption)
 				return ((StringSetOption) opt).value();
 			throw new RuntimeException("--" + opt.name
 					+ " not collection of strings");
 		}
 		throw new RuntimeException("unknown option --" + string);
 	}
 
 	/**
 	 * Returns all arguments left after option parsing. @see
 	 * {@link #argument(String)} {@link #slurpedArguments()}
 	 * 
 	 * @return all arguments left after option parsing
 	 */
 	public List<String> argList() {
 		parseCheck();
 		return argList;
 	}
 
 	/**
 	 * Retrieves an argument by name. If this is a slurpy argument, it retrieves
 	 * the first value specified.
 	 * 
 	 * @param name
 	 * @return value of a given argument
 	 */
 	public String argument(String name) {
 		parseCheck();
 		Integer i = argNames.get(name);
 		if (i == null)
 			throw new RuntimeException("unknown argument: " + name);
 		if (isSlurpy && i.intValue() == argList.size())
 			return null;
 		return argList.get(i);
 	}
 
 	/**
 	 * @return all non-unique arguments
 	 */
 	public List<String> slurpedArguments() {
 		parseCheck();
 		if (!isSlurpy)
 			throw new RuntimeException("not slurpy argument list");
 		if (argNames.isEmpty())
 			return argList;
 		int i = argNames.size() - 1;
 		return argList.subList(i, argList.size());
 	}
 
 	/**
 	 * Call {@link #usage(int)} with parameter 1 if there are any errors in the
 	 * error list.
 	 */
 	public void errorCheck() {
 		if (!errors.isEmpty())
 			usage(1);
 	}
 
 	/**
 	 * Convenience method that calls {@link #error(String)} then
 	 * {@link #errorCheck()}. Useful for less verbose validation code if you
 	 * don't care to collect all errors before dumping the usage information.
 	 * 
 	 * @param error
 	 *            description of a validation error
 	 */
 	public void die(String error) {
 		errors.add(error);
 		errorCheck();
 	}
 
 	/**
 	 * @return whether any errors have been registered
 	 */
 	public boolean hasErrors() {
 		return !errors.isEmpty();
 	}
 
 	/**
 	 * Returns a string representation of all the options and their values. This
 	 * may be useful in debugging or logging.
 	 * 
 	 * @return a string representation of all the options and their values
 	 */
 	@SuppressWarnings("rawtypes")
 	public String dump() {
 		StringBuilder b = new StringBuilder();
 		if (!options.isEmpty()) {
 			boolean firstOption = true;
 			for (Option o : new LinkedHashSet<Option>(options.values())) {
 				if (o == helpOption || o instanceof DummyOption)
 					continue;
 				if (firstOption) {
 					b.append("options:\n");
 					firstOption = false;
 				}
 				b.append(o.name).append(": ");
 				if (o == versionOption)
 					b.append(version);
 				else if (o instanceof CollectionOption) {
 					Collection c = (Collection) o.value();
 					boolean first = true;
 					for (Object obj : c) {
 						if (first)
 							first = false;
 						else
 							b.append(", ");
 						b.append(obj);
 					}
 				} else
 					b.append(o.value());
 				b.append('\n');
 			}
 		}
 		if (hasArguments()) {
 			boolean firstArgument = true;
 			boolean commas = argNames.size() < argList.size();
 			for (Entry<String, Integer> e : argNames.entrySet()) {
 				if (firstArgument) {
 					b.append("arguments:\n");
 					firstArgument = false;
 				}
 				b.append(e.getKey()).append(": ");
 				b.append(argList.get(e.getValue()));
 				if (!commas || e.getValue() + 1 < argNames.size())
 					b.append('\n');
 			}
 			for (int i = argNames.size(); i < argList.size(); i++) {
 				if (firstArgument) {
 					b.append("arguments:\n");
 					firstArgument = false;
 				} else
 					b.append(", ");
 				b.append(argList.get(i));
 			}
 			b.append('\n');
 		}
 		return b.toString();
 	}
 
 	/**
 	 * Resets everything to its state prior to command line parsing.
 	 */
 	@SuppressWarnings({ "rawtypes", "unchecked" })
 	public void clear() {
 		if (!parsed)
 			return;
 		parsed = false;
 		argList.clear();
 		for (Option o : new HashSet<Option>(options.values())) {
 			if (o == helpOption || o == versionOption)
 				continue;
 			if (o instanceof DummyOption)
 				continue;
 			o.found = false;
 			if (o instanceof CollectionOption) {
 				CollectionOption co = (CollectionOption) o;
 				co.storageList.clear();
 				((Collection) co.value).clear();
 			} else {
 				o.value = null;
 				o.stored = null;
 			}
 		}
 	}
 
 	/**
 	 * Delegates to {@link #dump()}.
 	 */
 	@Override
 	public String toString() {
 		return dump();
 	}
 
 	/**
 	 * Breaks a formatted line into a pre-formatted part and possibly a part
 	 * that requires wrapping.
 	 * 
 	 * @param s
 	 * @param length
 	 * @return
 	 */
 	private static List<String> split(CharSequence s, int length) {
 		List<String> list = new ArrayList<String>(2);
 		if (s.length() > length) {
 			// try to split
 			String s1 = null, s2 = null;
 			// try to split inside the margin
 			for (int i = length - 1; i >= 0; i--) {
 				char c = s.charAt(i);
 				if (Character.isWhitespace(c)) {
 					s1 = s.subSequence(0, i).toString();
 					s2 = s.subSequence(i, s.length()).toString().trim();
 					break;
 				}
 			}
 			if (s1 == null) {
 				// okay, try to split *outside* the margin
 				for (int i = length; i < s.length(); i++) {
 					char c = s.charAt(i);
 					if (Character.isWhitespace(c)) {
 						s1 = s.subSequence(0, i).toString();
 						s2 = s.subSequence(i, s.length()).toString().trim();
 						break;
 					}
 				}
 			}
 			if (s1 == null)
 				// it was all one big word
 				list.add(s.toString());
 			else {
 				list.add(s1);
 				list.add(s2);
 			}
 		} else {
 			// no need to split
 			list.add(s.toString());
 		}
 		return list;
 	}
 
 	/**
 	 * Used in word-wrapping state machine.
 	 */
 	private enum State {
 		afterBreak, firstWord, inWord, whitespace
 	};
 
 	/**
 	 * Format a column of text, breaking only on whitespace.
 	 * 
 	 * @param s
 	 *            text to format
 	 * @param left
 	 *            left margin
 	 * @param right
 	 *            right margin
 	 * @return text formatted as well as possible to fit inside the margins
 	 */
 	private static String wrap(CharSequence s, int left, int right) {
 		StringBuilder b = new StringBuilder(s.length() * 2);
 		Matcher m = linePattern.matcher(s);
 		State state = State.afterBreak;
 		int x = 0;
 		while (m.find()) {
 			String line = m.group();
 			if (line.length() == 0) {
 				// paragraph break
 				if (state == State.afterBreak)
 					b.append('\n');
 				else
 					b.append("\n\n");
 				state = State.afterBreak;
 			} else if (Character.isWhitespace(line.charAt(0))) {
 				// preformatted line
 				if (state != State.afterBreak)
 					b.append('\n');
 				b.append(line).append('\n');
 				state = State.afterBreak;
 			} else {
 				// text block
 				line = line.trim().replaceAll("\\s++", " ");
 				if (state == State.afterBreak) {
 					indent(b, left);
 					x = left;
 					state = State.firstWord;
 				} else {
 					if (x >= right) {
 						b.append('\n');
 						indent(b, left);
 						x = left;
 						state = State.firstWord;
 					} else {
 						b.append(' ');
 						x++;
 						state = State.whitespace;
 					}
 				}
 				for (int i = 0; i < line.length(); i++) {
 					char c = line.charAt(i);
 					if (Character.isWhitespace(c)) {
 						if (x >= right) {
 							b.append('\n');
 							indent(b, left);
 							x = left;
 							state = State.afterBreak;
 						} else {
 							b.append(c);
 							x++;
 							state = State.whitespace;
 						}
 					} else {
 						switch (state) {
 						case afterBreak:
 							state = State.firstWord;
 						case inWord:
 						case firstWord:
 							b.append(c);
 							x++;
 							break;
 						case whitespace:
 							if (x >= right) {
 								b.append('\n');
 								indent(b, left);
								b.append(c);
								x = left + 1;
								state = State.firstWord;
 							} else if (x == s.length()) {
 								b.append(c);
 								b.append('\n');
 								indent(b, left);
 								x = left;
 								state = State.afterBreak;
 							} else {
 								// see whether the current word can fit inside
 								// the margin
 								boolean breaks = false;
 								for (int j = i + 1, y = x + 1; j < s.length(); j++, y++) {
 									char c2 = s.charAt(j);
 									if (Character.isWhitespace(c2))
 										break;
 									if (y > right) {
 										breaks = true;
 										break;
 									}
 								}
 								if (breaks) {
 									b.append('\n');
 									indent(b, left);
 									b.append(c);
 									x = left + 1;
 									state = State.firstWord;
 								} else {
 									b.append(c);
 									state = State.inWord;
 									x++;
 								}
 							}
 						}
 					}
 				}
 			}
 		}
 		return b.toString();
 	}
 
 	private static void indent(StringBuilder b, int left) {
 		for (int i = 0; i < left; i++)
 			b.append(' ');
 	}
 
 	/**
 	 * Sets column width at which {@link Cli} will attempt to wrap usage
 	 * information.
 	 * 
 	 * @param margin
 	 */
 	public void setMargin(int margin) {
 		this.margin = margin;
 	}
 }
