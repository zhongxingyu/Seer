 /*
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  * http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License. 
  */
 package net.rptools.maptool.client;
 
 import java.math.BigDecimal;
 import java.util.ArrayList;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Set;
 import java.util.Stack;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import net.rptools.common.expression.ExpressionParser;
 import net.rptools.common.expression.Result;
 import net.rptools.maptool.client.functions.AbortFunction;
 import net.rptools.maptool.client.functions.AddAllToInitiativeFunction;
 import net.rptools.maptool.client.functions.AssertFunction;
 import net.rptools.maptool.client.functions.CurrentInitiativeFunction;
 import net.rptools.maptool.client.functions.EvalMacroFunctions;
 import net.rptools.maptool.client.functions.FindTokenFunctions;
 import net.rptools.maptool.client.functions.HasImpersonated;
 import net.rptools.maptool.client.functions.InitiativeRoundFunction;
 import net.rptools.maptool.client.functions.InputFunction;
 import net.rptools.maptool.client.functions.IsTrustedFunction;
 import net.rptools.maptool.client.functions.JSONMacroFunctions;
 import net.rptools.maptool.client.functions.MacroDialogFunctions;
 import net.rptools.maptool.client.functions.MacroFunctions;
 import net.rptools.maptool.client.functions.MacroLinkFunction;
 import net.rptools.maptool.client.functions.MiscInitiativeFunction;
 import net.rptools.maptool.client.functions.PlayerFunctions;
 import net.rptools.maptool.client.functions.RemoveAllFromInitiativeFunction;
 import net.rptools.maptool.client.functions.StrListFunctions;
 import net.rptools.maptool.client.functions.StrPropFunctions;
 import net.rptools.maptool.client.functions.StringFunctions;
 import net.rptools.maptool.client.functions.SwitchTokenFunction;
 import net.rptools.maptool.client.functions.TokenAddToInitiativeFunction;
 import net.rptools.maptool.client.functions.TokenBarFunction;
 import net.rptools.maptool.client.functions.TokenGMNameFunction;
 import net.rptools.maptool.client.functions.TokenHaloFunction;
 import net.rptools.maptool.client.functions.TokenInitFunction;
 import net.rptools.maptool.client.functions.TokenInitHoldFunction;
 import net.rptools.maptool.client.functions.TokenLabelFunction;
 import net.rptools.maptool.client.functions.LookupTableFunction;
 import net.rptools.maptool.client.functions.TokenLightFunctions;
 import net.rptools.maptool.client.functions.TokenNameFunction;
 import net.rptools.maptool.client.functions.StateImageFunction;
 import net.rptools.maptool.client.functions.TokenImage;
 import net.rptools.maptool.client.functions.TokenNoteFunctions;
 import net.rptools.maptool.client.functions.TokenPropertyFunctions;
 import net.rptools.maptool.client.functions.TokenRemoveFromInitiativeFunction;
 import net.rptools.maptool.client.functions.TokenSightFunctions;
 import net.rptools.maptool.client.functions.TokenSpeechFunctions;
 import net.rptools.maptool.client.functions.TokenStateFunction;
 import net.rptools.maptool.client.functions.TokenVisibleFunction;
 import net.rptools.maptool.client.ui.htmlframe.HTMLFrameFactory;
 import net.rptools.maptool.client.ui.zone.ZoneRenderer;
 import net.rptools.maptool.model.MacroButtonProperties;
 import net.rptools.maptool.model.Player;
 import net.rptools.maptool.model.Token;
 import net.rptools.maptool.model.Zone;
 import net.rptools.parser.ParserException;
 import net.rptools.parser.VariableResolver;
 import net.rptools.parser.function.Function;
 import net.sf.json.JSONArray;
 import net.sf.json.JSONObject;
 
 public class MapToolLineParser {
 
 	/** MapTool functions to add to the parser.  */
 	private static final Function[] mapToolParserFunctions = {
 		AbortFunction.getInstance(),
 		AssertFunction.getInstance(),
 		AddAllToInitiativeFunction.getInstance(),
 		CurrentInitiativeFunction.getInstance(),
 		EvalMacroFunctions.getInstance(),
 		FindTokenFunctions.getInstance(),
 		HasImpersonated.getInstance(),
 		InitiativeRoundFunction.getInstance(),
 		InputFunction.getInstance(),
 		IsTrustedFunction.getInstance(),
 		JSONMacroFunctions.getInstance(),
 		LookupTableFunction.getInstance(),
 		MacroDialogFunctions.getInstance(),
 		MacroFunctions.getInstance(), 
 		MacroLinkFunction.getInstance(),
 		MiscInitiativeFunction.getInstance(),
 		PlayerFunctions.getInstance(),
 		RemoveAllFromInitiativeFunction.getInstance(),
 		StateImageFunction.getInstance(),
 		StringFunctions.getInstance(),
 		StrListFunctions.getInstance(),
 		StrPropFunctions.getInstance(),
 		SwitchTokenFunction.getInstance(),
 		TokenAddToInitiativeFunction.getInstance(),
 		TokenBarFunction.getInstance(),
 		TokenGMNameFunction.getInstance(),
 		TokenHaloFunction.getInstance(),
 		TokenImage.getInstance(),
 		TokenInitFunction.getInstance(),
 		TokenInitHoldFunction.getInstance(),
 		TokenLabelFunction.getInstance(),
 		TokenLightFunctions.getInstance(),
 		TokenNameFunction.getInstance(),
 		TokenNoteFunctions.getInstance(),
 		TokenPropertyFunctions.getInstance(),
 		TokenRemoveFromInitiativeFunction.getInstance(),
 		TokenSightFunctions.getInstance(),
 		TokenSpeechFunctions.getInstance(),		
 		TokenStateFunction.getInstance(),
 		TokenVisibleFunction.getInstance(),
 	};
 
 	/** Name and Source or macros that come from chat. */
 	public static String CHAT_INPUT = "chat";
 	
 	/** Name of macro to divert calls to unknown macros on a lib macro to. */
 	public static String UNKNOWN_LIB_MACRO = "!!unknown-macro!!";
 
 	
 	/** Stack that holds our contexts. */
 	private final Stack<MapToolMacroContext> contextStack = new Stack<MapToolMacroContext>();
 
 	/** Was every context we entered during the macro trusted. */
 	private volatile boolean macroPathTrusted = false;
 	
 	/** The macro button index of the button on the impersonated token that called us. */
 	private volatile int macroButtonIndex = -1;
 	
 	private static final int PARSER_MAX_RECURSE = 50;
 	private int parserRecurseDepth;
 
 	private static final int MACRO_MAX_RECURSE = 50;	// Max number of recursive macro calls
 	private int macroRecurseDepth = 0;
 
 	private static final int MAX_LOOPS = 500;			// Max number of loop iterations
 
 	private enum Output {		// Mutually exclusive output formats
 		NONE,
 		RESULT,
 		TOOLTIP,
 		EXPANDED,
 		UNFORMATTED,
 	}
 
 	private enum LoopType {		// Mutually exclusive looping options
 		NO_LOOP,
 		COUNT,
 		FOR,
 		WHILE,
 		FOREACH,
 	}
 
 	private enum BranchType {	// Mutually exclusive branching options
 		NO_BRANCH,
 		IF,
 		SWITCH,
 	}
 
 	private enum CodeType {		// Mutually exclusive code-execution options
 		NO_CODE,
 		MACRO,
 		CODEBLOCK,
 	}
 	
 	private enum OutputLoc { 	// Mutually exclusive output location
 		CHAT,
 		DIALOG,
 		FRAME
 	}
 
 	
 	private enum ScanState {
 		SEARCHING_FOR_ROLL,
 		SEARCHING_FOR_QUOTE,
 		SEARCHING_FOR_CLOSE_BRACKET,
 		SKIP_NEXT_CHAR
 	}
 	
 	// Class to hold the inline rolls and where they start and end.
 	private static class InlineRollMatch {
 		final int start;
 		final int end;
 		final String match;
 		final int optEnd;
 		
 		InlineRollMatch(int start, int end, String match) {
 			this.start = start;
 			this.end = end;
 			this.match = match;
 			this.optEnd = -1;
 		}
 
 		InlineRollMatch(int start, int end, String match, int optEnd) {
 			this.start = start;
 			this.end = end;
 			this.match = match;
 			this.optEnd = optEnd;
 		}
 		
 		public int getStart() {
 			return start;
 		}
 		
 		public int getEnd() {
 			return end;
 		}
 		
 		public String getMatch() {
 			return match;
 		}
 		
 		public int getOptEnd() {
 			return optEnd;
 		}
 		
 		public String getOpt() {
 			if (optEnd > 0) {
 				return match.substring(1,optEnd-start);
 			} else {
 				return "";
 			}
 		}
 		
 		public String getRoll() {
 			if (optEnd > 0) {
 				return match.substring(optEnd+1-start, end-start);
 			} else {
 					return match.substring(1, end-start);
 			}
 		}
 	}
 
 	/*****************************************************************************
 	 * OptionType - defines roll options, including values for default parameters.
 	 *****************************************************************************/
 	// These items are only used in the enum below, but have to be declared out here
 	// because they must appear before being used in the enum definitions.
 	private static final String defaultLoopSep = "\", \"";
 	private static final Object nullParam = (Object)null;
 
 	/*
 	 * In order to add a new roll option, follow the instructions in the "todo" comments in this file.
 	 */
 	private enum OptionType {
 		/*
 		 * TODO: If you're adding a new option, make an entry in this table
 		 */
 
 		// The format is:
 		//   NAME   (nameRegex, minParams, maxParams, defaultValues...)
 		//
 		// You must provide (maxParams - minParams) default values (BigDecimal or String types).
 		NO_OPTION   ("",              0, 0),
 		// output formats
 		EXPANDED    ("e|expanded",    0, 0),
 		HIDDEN      ("h|hidden|hide", 0, 0),
 		RESULT      ("r|result",      0, 0),
 		UNFORMATTED ("u|unformatted", 0, 0),
 		TOOLTIP     ("t|tooltip",     0, 1, nullParam),
 		// loops
 		COUNT       ("c|count",       1, 2, defaultLoopSep),
 		FOR         ("for",           3, 5, BigDecimal.ONE, defaultLoopSep),
 		FOREACH     ("foreach",       2, 4, defaultLoopSep, ","),
 		WHILE       ("while",         1, 2, defaultLoopSep),
 		// branches
 		IF          ("if",            1, 1),
 		SWITCH      ("switch",        1, 1),
 		// code
 		CODE        ("code",          0, 0),
 		MACRO       ("macro",         1, 1),
 		// HTML Dockable Frame
 		FRAME		("frame",         1, 2, "\"\""),
 		// HTML Dialog
 		DIALOG      ("dialog",        1, 2, "\"\""),
 		// Run for another token
 		TOKEN       ("token",         1, 1);
 
 		protected final String nameRegex;
 		protected final int minParams, maxParams;
 		protected final Object[] defaultParams;
 
 		OptionType(String nameRegex, int minParams, int maxParams, Object... defaultParams) {
 			this.nameRegex = nameRegex;
 			this.minParams = minParams;
 			this.maxParams = maxParams;
 			if (defaultParams == null) {
 				// The Java 5 varargs facility has a small hack which we must work around.
 				// If you pass a single null argument, Java doesn't know whether you wanted a single variable arg of null,
 				// or if you meant to say that the variable argument array itself should be null.
 				// Java chooses the latter, but we want the former.
 				this.defaultParams = new Object[1];
 				this.defaultParams[0] = null;
 			} else {
 				this.defaultParams = defaultParams;
 			}
 			if (this.defaultParams.length != (maxParams - minParams)) {
 				System.out.println(String.format("Internal error: roll option %s specifies wrong number of default parameters", name()));
 			}
 		}
 
 		/** Obtain one of the enum values, or null if <code>strName</code> doesn't match any of them. */
 		protected static OptionType optionTypeFromName(String strName) {
 			for (OptionType rot : OptionType.values()) {
 				if (Pattern.compile("^\\s*" + rot.getNameRegex() + "\\s*$", Pattern.CASE_INSENSITIVE).matcher(strName).matches()) {
 					return rot;
 				}
 			}
 			return null;
 		}
 
 		/** Returns the regex that matches all valid names for this option. */
 		public String getNameRegex() { return nameRegex; }
 		public int getMinParams() { return minParams; }
 		public int getMaxParams() { return maxParams; }
 		
 		/** Returns a copy of the default params array for this option type. */
 		public Object[] getDefaultParams() {
 			Object[] retval = new Object[maxParams];
 			for (int i=minParams; i<maxParams; i++) {
 				retval[i] = defaultParams[i-minParams];
 			}
 			return retval;
 		}
 
 		public String toString() {
 			String retval = name() + ", default params: [";
 			boolean first = true;
 			for (Object p : defaultParams) {
 				if (first) {
 					first = false;
 				} else {
 					retval += ", ";
 				}
 				if (p==null)
 					retval += "null";
 				else if (p instanceof String)
 					retval += "\"" + p + "\"";
 				else
 					retval += p.toString();
 			}
 			retval += "]";
 			return retval;
 		}
 	}
 
 
 	/**********************************************************************************
 	 * OptionInfo class - holds extracted name and parameters for a roll option.
 	 **********************************************************************************/
 	private class OptionInfo {
 		private OptionType optionType;
 		private String optionName;
 		private int optionStart, optionEnd;
 		private String srcString;
 		private Object[] params;
 
 		/** Attempts to create an OptionInfo object by parsing the text in <code>optionString</code>
 		 *  beginning at position <code>start</code>. */
 		public OptionInfo(String optionString, int start) throws RollOptionException {
 			srcString = optionString;
 			optionStart = start;
 			parseOptionString(optionString, start);
 		}
 
 		/** Parses a roll option and sets the RollOptionType and parameters.
 		 *  <br>Missing optional parameters are set to the default for the type.
 		 *  @param optionString The string containing the option
 		 *  @param start Where in the string to begin parsing from 
 		 *  @throws RollOptionException if the option string can't be parsed.*/
 		private void parseOptionString(String optionString, int start) throws RollOptionException {
 			boolean paramsFound;	// does the option string have a "(" after the name?
 			int endOfString = optionString.length();
 
 			// Find the name
 			Pattern pattern = Pattern.compile("^\\s*(?:(\\w+)\\s*\\(|(\\w+))");	// matches "abcd(" or "abcd"
 			Matcher matcher = pattern.matcher(optionString);
 			matcher.region(start, endOfString);
 			if (! matcher.find()) {
 				throw new RollOptionException("Bad roll option");
 			}
 			paramsFound = (matcher.group(1) != null);
 			String name = paramsFound ? matcher.group(1).trim() : matcher.group(2).trim();
 			start = matcher.end();
 			matcher.region(start, endOfString);
 
 			// Get the option type and default params from the name
 			optionType = OptionType.optionTypeFromName(name);
 			if (optionType == null) {
 				throw new RollOptionException(String.format("Unknown option name \"%s\"", name));
 			}
 			optionName = name;
 			params = optionType.getDefaultParams();	// begin with default values for optional params
 
 			// If no params found (i.e. no "(" after option name), we're done
 			if (!paramsFound) {
 				if (optionType.getMinParams() == 0) {
 					optionEnd = start;
 					return;
 				} else {
 					throw new RollOptionException(String.format("Roll option \"%s\" requires a list of parameters in parentheses", 
 							optionName, optionType.getMinParams()));
 				}
 			}
 
 			// Otherwise, match the individual parameters one at a time
 			pattern = Pattern.compile( "^(?:((?:[^()\",]|\"[^\"]*\"|\\((?:[^()\"]|\"[^\"]*\")*\\))+)(,|\\))){1}?" );
 			matcher = pattern.matcher(optionString);
 			matcher.region(start, endOfString);
 			List<String> paramList = new ArrayList<String>();
 			boolean lastItem = false;	// true if last match ended in ")"
 
 			while (!lastItem) {
 				if (matcher.find()) {
 					String param = matcher.group(1).trim();
 					paramList.add(param);
 					lastItem = matcher.group(2).equalsIgnoreCase(")");
 					start = matcher.end();
 					matcher.region(start, endOfString);
 				} else {
 					throw new RollOptionException(String.format("Roll option %s: bad option parameters %s", optionName, srcString));
 				}
 			}
 
 			// Error checking
 			int min = optionType.getMinParams(), max = optionType.getMaxParams();
 			int numParamsFound = paramList.size();
 			if (numParamsFound < min || numParamsFound > max) {
 				throw new RollOptionException(String.format("Roll option %s must have %d to %d parameters; found %d: %s", 
 						optionName, min, max, numParamsFound, srcString));
 			}
 
 			// Fill in the found parameters, converting to BigDecimal if possible.
 			for (int i=0; i<numParamsFound; i++) {
 				params[i] = toNumIfPossible(paramList.get(i));
 			}
 
 			optionEnd = start;
 			return;
 		}
 
 		/** Converts a String to a BigDecimal if possible, otherwise returns original String. */
 		private Object toNumIfPossible(String s) {
 			Object retval = s;
 			try {
 				retval = new BigDecimal(Integer.decode(s));
 			} catch (NumberFormatException nfe) {
 				// Do nothing
 			}
 			return retval;
 		}
 
 		public String getName() { return optionName; }
 		public int getStart() { return optionStart; }
 		public int getEnd() { return optionEnd; }
 
 		/** Gets a parameter (Object type). */
 		public Object getObjectParam(int index) {
 			return params[index];
 		}
 		/** Gets the text of a parameter. */
 		public String getStringParam(int index) {
 			Object o = params[index];
 			return (o == null) ? null : o.toString();
 		}
 		/** Gets the text of a parameter if it is a valid identifier. 
 		 * @throws ParserException if the parameter text is not a valid identifier.*/
 		public String getIdentifierParam(int index) throws ParserException {
 			String s = params[index].toString();
 			if (!s.matches("[a-zA-Z]\\w*")) {	// MapTool doesn't allow variable names to start with '_'
 				throw new ParserException(String.format("\"%s\" is not a valid variable name", s));
 			}
 			return s;
 		}
 		/** Gets a parameter, casting it to BigDecimal. */
 		public BigDecimal getNumericParam(int index) {
 			return (BigDecimal)params[index];
 		}
 		/** Gets the integer value of a parameter. */
 		public int getIntParam(int index) {
 			return getNumericParam(index).intValue();
 		}
 
 		/** Returns a param, parsing it as an expression if it is a string. */
 		public Object getParsedParam(int index, MapToolVariableResolver res, Token tokenInContext) 
 		throws ParserException {
 			Object retval = params[index];
 			// No parsing is done if the param isn't a String (e.g. it's already a BigDecimal)
 			if (params[index] instanceof String) {
 				Result result = parseExpression(res, tokenInContext, (String)params[index]);
 				retval = result.getValue();
 			}
 			return retval;
 		}
 
 		/** Returns a param as int, parsing it as an expression if it is a string. */
 		public int getParsedIntParam(int index, MapToolVariableResolver res, Token tokenInContext)
 		throws ParserException {
 			Object retval = getParsedParam(index, res, tokenInContext);
 			if (! (retval instanceof BigDecimal))
 				throw new ParserException(String.format("\"%s\" is not a number.", retval.toString()));
 			return ((BigDecimal)retval).intValue();
 		}
 
 		public String toString() {
 			String retval = optionName + ": params: (";
 			boolean first = true;
 			for (Object p : params) {
 				if (first) {
 					first = false;
 				} else {
 					retval += ", ";
 				}
 				if (p==null)
 					retval += "null";
 				else if (p instanceof String)
 					retval += "\"" + p + "\"";
 				else
 					retval += p.toString();
 			}
 			retval += ")";
 			return retval;
 		}
 	} ///////////////////// end of OptionInfo class
 
 	/** Thrown when a roll option can't be parsed. */
 	@SuppressWarnings("serial")
 	public class RollOptionException extends Exception {
 		public String msg;
 
 		public RollOptionException(String msg) {
 			this.msg = msg;
 		}
 	}
 
 	/** Scans a string of options and builds OptionInfo objects for each option found.
 	 * @param optionString A string containing a comma-delimited list of roll options.
 	 * @throws RollOptionException if any of the options are unknown or don't match the template for that option type.*/
 	private List<OptionInfo> getRollOptionList(String optionString) throws  RollOptionException {
 		if (optionString == null) return null;
 
 		List<OptionInfo> list = new ArrayList<OptionInfo>();
 		optionString = optionString.trim();
 		int start = 0;
 		int endOfString = optionString.length();
 		boolean atEnd = false;
 		Pattern commaPattern = Pattern.compile("^\\s*,\\s*(?!$)");
 
 		while (start < endOfString) {
 			OptionInfo roi;
 			if (atEnd) {
 				// If last param didn't end with ",", there shouldn't have been another option
 				throw new RollOptionException("Roll option list can't end with a comma");
 			}
 			// Eat the next option from string, and add parsed option to list
 			roi = new OptionInfo(optionString, start);
 			list.add(roi);
 			start = roi.getEnd();
 			// Eat any "," sitting between options
 			Matcher matcher = commaPattern.matcher(optionString);
 			matcher.region(start, endOfString);
 			if (matcher.find()) {
 				start = matcher.end();
 				atEnd = false;
 			} else {
 				atEnd = true;
 			}
 		}
 
 		return list;
 	}
 
 
 	public String parseLine(String line) throws ParserException {
 		return parseLine(null, line);
 	}
 
 	public String parseLine(Token tokenInContext, String line) throws ParserException {
 		return parseLine(tokenInContext, line, null);
 	}
 
 	
 	public String parseLine(Token tokenInContext, String line, MapToolMacroContext context) throws ParserException {
 		return parseLine(null, tokenInContext, line, context);
 	}
 
 	public String parseLine(MapToolVariableResolver res, Token tokenInContext, String line) throws ParserException {
 		return  parseLine(res, tokenInContext, line, null);
 	}
 	
 	public String parseLine(MapToolVariableResolver res, Token tokenInContext, String line,
 			MapToolMacroContext context) throws ParserException {
 
 		if (line == null) {
 			return "";
 		}
 
 		line = line.trim();
 		if (line.length() == 0) {
 			return "";
 		}
 		
 		Stack<Token> contextTokenStack = new Stack<Token>();
 		enterContext(context);
  		try {
 			// Keep the same variable context for this line
 			MapToolVariableResolver resolver = (res==null) ? new MapToolVariableResolver(tokenInContext) : res;
 
 			StringBuilder builder = new StringBuilder();
 			
 			int start = 0;
 			
 			List<InlineRollMatch> matches = this.locateInlineRolls(line);
 			
 			for (InlineRollMatch match : matches) {
 				builder.append(line.substring(start, match.getStart())); // add everything before the roll
 
 				start = match.getEnd() + 1;
 				// These variables will hold data extracted from the roll options.
 				Output output = Output.TOOLTIP;
 				String text = null;	// used by the T option
 
 				OutputLoc outputTo = OutputLoc.CHAT;
 
 				LoopType loopType = LoopType.NO_LOOP;
 				int loopStart = 0, loopEnd = 0, loopStep = 1;
 				int loopCount = 0;
 				String loopSep = null;
 				String loopVar = null, loopCondition = null;
 				List<String> foreachList = new ArrayList<String>();
 
 				BranchType branchType = BranchType.NO_BRANCH;
 				Object branchCondition = null;
 
 				CodeType codeType = CodeType.NO_CODE;
 				String macroName = null;
 
 				String frameName = null;
 				String frameOpts = null;
 
 				if (match.getMatch().startsWith("[")) {
 				
 					
 					String opts = match.getOpt();
 					String roll = match.getRoll();
 					if (opts != null) {
 						// Turn the opts string into a list of OptionInfo objects.
 						List<OptionInfo> optionList = null;
 						try {
 							optionList = getRollOptionList(opts);
 						} catch (RollOptionException roe) {
 							doError(roe.msg, opts, roll);
 						}
 
 						// Scan the roll options and prepare variables for later use
 						for (OptionInfo option : optionList) {
 							String error = null;
 							/*
 							 * TODO: If you're adding a new option, add a new case here to collect info from the parameters.
 							 *       If your option uses parameters, use the option.getXxxParam() methods to get
 							 *       the text or parsed values of the parameters.
 							 */
 							switch (option.optionType) {
 
 							///////////////////////////////////////////////////
 							// OUTPUT FORMAT OPTIONS
 							///////////////////////////////////////////////////
 							case HIDDEN:
 								output = Output.NONE;
 								break;
 							case RESULT:
 								output = Output.RESULT;
 								break;
 							case EXPANDED:
 								output = Output.EXPANDED;
 								break;
 							case UNFORMATTED:
 								output = Output.UNFORMATTED;
 								break;
 							case TOOLTIP:
 								// T(display_text)
 								output = Output.TOOLTIP;
 								text = option.getStringParam(0);
 								break;
 
 							///////////////////////////////////////////////////
 							// LOOP OPTIONS
 							///////////////////////////////////////////////////
 							case COUNT:
 								// COUNT(num [, sep])
 								loopType = LoopType.COUNT;
 								error = null;
 								try {
 									loopCount = option.getParsedIntParam(0, resolver, tokenInContext);
 									if (loopCount < 0)
 										error = String.format("COUNT option requires a non-negative number (got %d)", loopCount);
 								} catch (ParserException pe) {
 									error = String.format("Error processing COUNT option: %s", pe.getMessage());
 								}
 								loopSep = option.getStringParam(1);
 
 								if (error != null) doError(error, opts, roll);
 								break;
 
 							case FOR:
 								// FOR(var, start, end [, step [, sep]])
 								loopType = LoopType.FOR;
 								error = null;
 								try {
 									loopVar = option.getIdentifierParam(0);
 									loopStart = option.getParsedIntParam(1, resolver, tokenInContext);
 									loopEnd = option.getParsedIntParam(2, resolver, tokenInContext);
 									try {
 										loopStep = option.getParsedIntParam(3, resolver, tokenInContext);
 									} catch (ParserException pe) {
 										// Build a more informative error message for this common mistake
 										String msg = pe.getMessage();
 										msg = msg + " To specify a non-default loop separator, " +
 										"you must use the format FOR(var,start,end,step,separator)";
 										throw new ParserException(msg);
 									}
 									loopSep = option.getStringParam(4);
 									if (loopStep != 0)
 										loopCount = (int)Math.floor(Math.abs( (loopEnd - loopStart)/loopStep + 1 ));
 
 									if (loopVar.equalsIgnoreCase(""))
 										error = "FOR variable name missing";
 									if (loopStep == 0)
 										error = "FOR loop step can't be zero";
 									if ((loopEnd < loopStart && loopStep > 0) || (loopEnd > loopStart && loopStep < 0))
 										error = String.format("FOR loop step size is in the wrong direction (start=%d, end=%d, step=%d)", 
 												loopStart, loopEnd, loopStep);
 								} catch (ParserException pe) {
 									error = String.format("Error processing FOR option: %s", pe.getMessage());
 								}
 
 								if (error != null) doError(error, opts, roll);
 								break;
 
 							case FOREACH:
 								// FOREACH(var, list [, outputDelim [, inputDelim]])
 								loopType = LoopType.FOREACH;
 								error = null;
 								try {
 									loopVar = option.getIdentifierParam(0);
 									String listString = option.getParsedParam(1, resolver, tokenInContext).toString();
 									loopSep = option.getStringParam(2);
 									String listDelim = option.getStringParam(3);
									if (listDelim.trim().startsWith("\"")) {
										listDelim = parseExpression(resolver, tokenInContext, listDelim).getValue().toString();
									}
									
 									foreachList = new ArrayList<String>();
 									if (listString.trim().startsWith("{") || listString.trim().startsWith("[")) {
 										// if String starts with [ or { it is either JSON try treat it as a JSON String
 										Object obj = JSONMacroFunctions.getInstance().convertToJSON(listString);
 										if (obj != null) {
 											if (obj instanceof JSONArray) {
 												for (Object o : ((JSONArray)obj).toArray()) {
 													foreachList.add(o.toString());
 												}
 											} else {
 												Set keySet = ((JSONObject)obj).keySet();
 												foreachList.addAll(keySet);
 											}
 										}
 									}
 									
 									// If we still dont have a list treat it list a string list
 									if (foreachList.size() == 0) {
 										StrListFunctions.parse(listString, foreachList, listDelim);
 									}
 									loopCount = foreachList.size();
 
 									if (loopVar.equalsIgnoreCase(""))
 										error = "FOREACH variable name missing";
 								} catch (ParserException pe) {
 									error = String.format("Error processing FOREACH option: %s", pe.getMessage());
 								}
 
 								if (error != null) doError(error, opts, roll);
 								break;
 
 							case WHILE:
 								// WHILE(cond [, sep])
 								loopType = LoopType.WHILE;
 								loopCondition = option.getStringParam(0);
 								loopSep = option.getStringParam(1);
 								break;
 
 							///////////////////////////////////////////////////
 							// BRANCH OPTIONS
 							///////////////////////////////////////////////////
 							case IF:
 								// IF(condition)
 								branchType = BranchType.IF;
 								branchCondition = option.getStringParam(0);
 								break;
 							case SWITCH:
 								// SWITCH(condition)
 								branchType = BranchType.SWITCH;
 								branchCondition = option.getObjectParam(0);
 								break;
 
 							///////////////////////////////////////////////////
 							// DIALOG AND FRAME OPTIONS
 							///////////////////////////////////////////////////
 							case FRAME:
 								codeType = CodeType.CODEBLOCK;
 								frameName = option.getParsedParam(0, resolver, tokenInContext).toString();
 								frameOpts = option.getParsedParam(1, resolver, tokenInContext).toString();
 								outputTo = OutputLoc.FRAME;
 								break;
 							case DIALOG:
 								codeType = CodeType.CODEBLOCK;
 								frameName = option.getParsedParam(0, resolver, tokenInContext).toString();
 								frameOpts = option.getParsedParam(1, resolver, tokenInContext).toString();
 								outputTo = OutputLoc.DIALOG;
 								break;
 							///////////////////////////////////////////////////
 							// CODE OPTIONS
 							///////////////////////////////////////////////////
 							case MACRO:
 								// MACRO("macroName@location")
 								codeType = CodeType.MACRO;
 								macroName = option.getStringParam(0);
 								break;
 							case CODE:
 								codeType = CodeType.CODEBLOCK;
 								break;
 							///////////////////////////////////////////////////
 							// MISC OPTIONS
 							///////////////////////////////////////////////////
 							case TOKEN:
 								if (!isMacroTrusted()) {
 									throw new ParserException("You do not have permission to use [token(): ]");
 								}
 								Token newToken = MapTool.getFrame().getCurrentZoneRenderer().getZone().resolveToken(
 										option.getParsedParam(0, resolver, tokenInContext).toString());
 								if (newToken != null) {
 									contextTokenStack.push(resolver.getTokenInContext());
 									resolver.setTokenIncontext(newToken);
 								}
 								break;							
 							default:
 								// should never happen
 								doError("Bad option found", opts, roll);
 							}
 						}
 					}
 
 					// Now that the options have been dealt with, process the body of the roll.
 					// We deal with looping first, then branching, then deliver the output.
 					StringBuilder expressionBuilder = new StringBuilder();
 					int iteration = 0;
 					boolean doLoop = true;
 					while (doLoop) {
 						int loopConditionValue;
 						Integer branchConditionValue = null;
 						Object branchConditionParsed = null;
 
 						// Process loop settings
 						if (iteration > MAX_LOOPS) {
 							doError("Too many loop iterations (possible infinite loop?)", opts, roll);
 						}
 
 						if (loopType != LoopType.NO_LOOP) {
 							// We only update roll.count in a loop statement.  This allows simple nested 
 							// statements to inherit roll.count from the outer statement.
 							resolver.setVariable("roll.count", iteration);
 						}
 
 						switch (loopType) {
 						/*
 						 * TODO: If you're adding a new looping option, add a new case to handle the iteration
 						 */
 						case NO_LOOP:
 							if (iteration > 0) {		// stop after first iteration
 								doLoop = false;
 							}
 							break;
 						case COUNT:
 							if (iteration == loopCount) {
 								doLoop = false;
 							}
 							break;
 						case FOR:
 							if (iteration != loopCount) {
 								resolver.setVariable(loopVar, new BigDecimal(loopStart + loopStep * iteration));
 							} else {
 								doLoop = false;
 								resolver.setVariable(loopVar, null);
 							}
 							break;
 						case FOREACH:
 							if (iteration != loopCount) {
 								String item = foreachList.get(iteration);
 								resolver.setVariable(loopVar, item);
 							} else {
 								doLoop = false;
 								resolver.setVariable(loopVar, null);
 							}
 							break;
 						case WHILE:
 							// This is a hack to get around a bug with the parser's comparison operators.
 							// The InlineTreeFormatter class in the parser chokes on comparison operators, because they're
 							// not listed in the operator precedence table.
 							//
 							// The workaround is that "non-deterministic" functions fully evaluate their arguments,
 							// so the comparison operators are reduced to a number by the time the buggy code is reached.
 							// The if() function defined in dicelib is such a function, so we use it here to eat
 							// any comparison operators.
 							String hackCondition = (loopCondition == null) ? null : String.format("if(%s, 1, 0)", loopCondition);
 							// Stop loop if the while condition is false
 							try {
 								Result result = parseExpression(resolver, tokenInContext, hackCondition);
 								loopConditionValue = ((Number)result.getValue()).intValue();
 								if (loopConditionValue == 0) {
 									doLoop = false;
 								}
 							} catch (Exception e) {
 								doError(String.format("Invalid condition in WHILE(%s) roll option", loopCondition), opts, roll);
 							}
 							break;
 						}
 
 						// Output the loop separator
 						if (doLoop && iteration != 0 && output != Output.NONE) {
 							expressionBuilder.append(parseExpression(resolver, tokenInContext, loopSep).getValue());
 						}
 
 						if (!doLoop) {
 							break;
 						}
 
 						iteration++;
 
 						// Extract the appropriate branch to evaluate.
 
 						// Evaluate the branch condition/expression
 						if (branchCondition != null) {
 							// This is a similar hack to the one used for the loopCondition above.
 							String hackCondition = (branchCondition == null) ? null : branchCondition.toString();
 							if (branchType == BranchType.IF) {
 								hackCondition = (hackCondition == null) ? null : String.format("if(%s, 1, 0)", hackCondition);
 							}
 							Result result = null;
 							try {
 								result = parseExpression(resolver, tokenInContext, hackCondition);
 							} catch (Exception e) {
 								doError(String.format("Invalid condition in %s(%s) roll option", branchType.toString(), 
 										branchCondition.toString()), opts, roll);
 							}
 							branchConditionParsed = result.getValue();
 							if (branchConditionParsed instanceof Number) {
 								branchConditionValue = ((Number)branchConditionParsed).intValue();
 							}
 						}
 
 						// Set up regexes for scanning through the branches.
 						// branchRegex then defines one matcher group for the parseable content of the branch.
 						String rollBranch = roll;
 						String branchRegex, branchSepRegex, branchLastSepRegex;
 						if (codeType != CodeType.CODEBLOCK) {
 							// matches any text not containing a ";" (skipping over strings) 
 							String noCodeRegex = "((?:[^\";]|\"[^\"]*\"|'[^']*')*)";
 							branchRegex = noCodeRegex;
 							branchSepRegex = ";";
 							branchLastSepRegex = ";?";	// The last clause doesn't have to end with a separator
 						} else {
 							// matches text inside braces "{...}", skipping over strings (one level of {} nesting allowed)
 							String codeRegex = "\\{((?:[^{}\"]|\"[^\"]*\"|'[^']*'|\\{(?:[^}\"]|\"[^\"]*\"|'[^']*')*})*)}";
 							branchRegex = codeRegex;
 							branchSepRegex = ";";
 							branchLastSepRegex = ";?";	// The last clause doesn't have to end with a separator
 						}
 
 						// Extract the branch to use
 						switch (branchType) {
 						/*
 						 * TODO: If you're adding a new branching option, add a new case to extract the branch text
 						 */
 						case NO_BRANCH:
 						{
 							// There's only one branch, so our regex is very simple
 							String testRegex = String.format("^\\s*%s\\s*$", branchRegex);
 							Matcher testMatcher = Pattern.compile(testRegex).matcher(roll);
 							if (testMatcher.find()) {
 								rollBranch = testMatcher.group(1);
 							} else {
 								doError("Error in body of roll.", opts, roll);
 							}
 							break;	
 						}
 						case IF:
 						{
 							// IF can have one or two branches.
 							// When there's only one branch and the condition is false, there's no output.
 							if (branchConditionValue == null) {
 								doError("Invalid IF condition: " + branchCondition 
 										+ ", evaluates to: " + branchConditionParsed.toString(), opts, roll);
 							}
 							int whichBranch = (branchConditionValue != 0) ? 0 : 1;
 							String testRegex = String.format("^\\s*%s\\s*(?:%s\\s*%s\\s*%s)?\\s*$", 
 									branchRegex, branchSepRegex, branchRegex, branchLastSepRegex);
 							Matcher testMatcher = Pattern.compile(testRegex).matcher(roll);
 							if (testMatcher.find()) {	// verifies that roll body is well-formed
 								rollBranch = testMatcher.group(1+whichBranch);
 								if (rollBranch == null) rollBranch = "''";	// quick-and-dirty way to get no output
 								rollBranch = rollBranch.trim();
 							} else {
 								doError("Error in roll for IF option", opts, roll);
 							}
 							break;
 						}
 						case SWITCH:
 						{
 							// We augment the branch regex to detect the "case xxx:" or "default:" prefixes,
 							// and search for a match.  An error is thrown if no case match is found.
 
 							// Regex matches 'default', 'case 123:', 'case "123":', 'case "abc":', but not 'case abc:'
 							branchRegex = "(?:case\\s*\"?((?<!\")(?:\\+|-)?[\\d]+(?!\")|(?<=\")[^\"]*(?=\"))\"?|(default))\\s*:\\s*" + branchRegex;
 							String caseTarget = branchConditionParsed.toString();
 							String testRegex = String.format("^(?:\\s*%s\\s*%s\\s*)*\\s*%s\\s*%s\\s*$", 
 									branchRegex, branchSepRegex, branchRegex, branchLastSepRegex);
 							Matcher testMatcher = Pattern.compile(testRegex).matcher(roll);
 							if (testMatcher.find()) {	// verifies that roll body is well-formed
 								String scanRegex = String.format("\\s*%s\\s*(?:%s)?", branchRegex, branchSepRegex);
 								Matcher scanMatcher = Pattern.compile(scanRegex).matcher(roll);
 								boolean foundMatch = false;
 								while (!foundMatch && scanMatcher.find()) {
 									String caseLabel = scanMatcher.group(1);	// "case (xxx):"
 									String def = scanMatcher.group(2);		// "(default):"
 									String branch = scanMatcher.group(3);
 									if (def != null) {
 										rollBranch = branch.trim();
 										foundMatch = true;;
 									}
 									if (caseLabel != null && caseLabel.matches(caseTarget)) {
 										rollBranch = branch.trim();
 										foundMatch = true;
 									}
 								}
 								if (!foundMatch) {
 									doError("SWITCH option found no match for " + caseTarget, opts, roll);
 								}
 							} else {
 								doError("Error in roll for SWITCH option", opts, roll);
 							}
 
 							break;
 						}
 						} // end of switch(branchType) statement
 
 						// Construct the output.  
 						// If a MACRO or CODE block is being used, we default to bare output as in the RESULT style.
 						// The output style NONE is also allowed in these cases.
 						Result result;
 						String output_text;
 						switch(codeType) {
 						case NO_CODE:
 							// If none of the code options are active, any of the formatting options can be used.
 							switch (output) {
 							/*
 							 * TODO: If you're adding a new formatting option, add a new case to build the output
 							 */
 							case NONE:
 								parseExpression(resolver, tokenInContext, rollBranch);
 								break;
 							case RESULT:
 								result = parseExpression(resolver, tokenInContext, rollBranch);
 								if (this.isMacroTrusted()) { 	
 									expressionBuilder.append(result != null ? result.getValue().toString() : "");
 								} else {
 									expressionBuilder.append(result != null ? result.getValue().toString().replaceAll("«|»|&#171;|&#187;|&laquo;|&raquo;|\036|\037", "") : "");
 								}
 								break;
 							case TOOLTIP:
 								String tooltip = rollBranch + " = ";
 								output_text = null;
 								result = parseExpression(resolver, tokenInContext, rollBranch);
 								tooltip += result.getDetailExpression();
 								if (text == null) {
 									output_text = result.getValue().toString();
 								} else {
 									if (!result.getDetailExpression().equals(result.getValue().toString())) {
 										tooltip += " = " + result.getValue();
 									}
 									resolver.setVariable("roll.result", result.getValue());
 									output_text = parseExpression(resolver, tokenInContext, text).getValue().toString();
 								}
 								tooltip = tooltip.replaceAll("'", "&#39;");
 								expressionBuilder.append(output_text != null ? "\036" + tooltip + "\037" + output_text + "\036" : "");
 								break;
 							case EXPANDED:
 								expressionBuilder.append("\036" + rollBranch + " = " + expandRoll(resolver, tokenInContext, rollBranch) + "\036");
 								break;
 							case UNFORMATTED:
 								output_text = rollBranch + " = " + expandRoll(resolver, tokenInContext, rollBranch);
 
 								// Escape quotes so that the result can be used in a title attribute
 								output_text = output_text.replaceAll("'", "&#39;");
 								output_text = output_text.replaceAll("\"", "&#34;");
 
 								expressionBuilder.append("\036\01u\02" + output_text + "\036");
 							}	// end of switch(output) statement
 							break;	// end of case NO_CODE in switch(codeType) statement
 							/*
 							 * TODO: If you're adding a new code option, add a new case to execute the code
 							 */
 						case MACRO:
 							// [MACRO("macroName@location"): args]
 							result = parseExpression(resolver, tokenInContext, macroName);
 							String callName = result.getValue().toString();
 							result = parseExpression(resolver, tokenInContext, rollBranch);
 							String macroArgs = result.getValue().toString();
 							output_text = runMacro(resolver, tokenInContext, callName, macroArgs);
 							if (output != Output.NONE) {
 								expressionBuilder.append(output_text);
 							}
 							resolver.setVariable("roll.count", iteration);	// reset this because called code might change it
 							break;
 
 						case CODEBLOCK:
 							output_text = runMacroBlock(resolver, tokenInContext, rollBranch);
 							resolver.setVariable("roll.count", iteration);	// reset this because called code might change it
 							if (output != Output.NONE) {
 								expressionBuilder.append(output_text);
 							}
 							break;
 						}
 					}
 					switch (outputTo) {
 					case FRAME:
 						HTMLFrameFactory.show(frameName, true, frameOpts, expressionBuilder.toString());
 						break;
 					case DIALOG:
 						HTMLFrameFactory.show(frameName, false, frameOpts, expressionBuilder.toString());
 						break;
 					case CHAT:
 						builder.append(expressionBuilder);
 						break;
 					}
 
 					// Revert to our previous token if [token(): ] was used
 					if (contextTokenStack.size() > 0) {
 						resolver.setTokenIncontext(contextTokenStack.pop());
 					}
 				} else if (match.getMatch().startsWith("{")) {
 					String roll = match.getRoll();
 					Result result = parseExpression(resolver, tokenInContext, roll);
 					if (isMacroTrusted()) {
 						builder.append(result != null ? result.getValue().toString() : "");
 					} else {
 						builder.append(result != null ? result.getValue().toString().replaceAll("«|»|&#171;|&#187;|&laquo;|&raquo;|\036|\037", "") : "");
 					}
 				}
 			}
 
 			builder.append(line.substring(start));
 
 			return builder.toString();
 		} finally {
 			exitContext();
 			// If we have exited the last context let the html frame we have (potentially)
 			// updated a token.
 			if (contextStackEmpty()) {
 				HTMLFrameFactory.tokenChanged(tokenInContext);
 			}
 		}
 
 	}
 	
 	
 	public Result parseExpression(String expression) throws ParserException {
 		return parseExpression(null, expression);
 	}
 
 	public Result parseExpression(Token tokenInContext, String expression) throws ParserException {
 
 		return parseExpression(new MapToolVariableResolver(tokenInContext), tokenInContext, expression);
 	}
 	public Result parseExpression(VariableResolver resolver, Token tokenInContext, String expression) throws ParserException {
 
 		if (parserRecurseDepth > PARSER_MAX_RECURSE) {
 			throw new ParserException("Max recurse limit reached");
 		}
 		try {
 			parserRecurseDepth ++;
 			return  createParser(resolver, tokenInContext == null ? false : true).evaluate(expression);
 		} catch (RuntimeException re) {
 
 			if (re.getCause() instanceof ParserException) {
 				throw (ParserException) re.getCause();
 			}
 
 			throw re;
 		} finally {
 			parserRecurseDepth--;
 		}
 	}	
 
 	public String expandRoll(String roll) {
 		return expandRoll(null, roll);
 	}
 
 	public String expandRoll(Token tokenInContext, String roll) {
 		return expandRoll(new MapToolVariableResolver(tokenInContext), tokenInContext, roll);
 	}
 
 	public String expandRoll(MapToolVariableResolver resolver, Token tokenInContext, String roll) {
 
 		try {
 			Result result = parseExpression(resolver, tokenInContext, roll);
 
 			StringBuilder sb = new StringBuilder();
 
 			if (result.getDetailExpression().equals(result.getValue().toString())) {
 				sb.append(result.getDetailExpression());
 			} else {
 				sb.append(result.getDetailExpression()).append(" = ").append(result.getValue());
 			}
 
 			return sb.toString();
 		} catch (ParserException e) {
 			return "Invalid expression: " + roll;
 		}
 
 	}    
 
 	/** Runs a macro from a specified location. */
 	public String runMacro(MapToolVariableResolver resolver, Token tokenInContext, String qMacroName, String args) 
 	throws ParserException {
 		
 		MapToolMacroContext macroContext;
 		
 		String macroBody = null;
 
 		String[] macroParts = qMacroName.split("@",2);
 		String macroLocation;
 
 		String macroName = macroParts[0];
 		if (macroParts.length == 1) {
 			macroLocation = null;
 		} else {
 			macroLocation = macroParts[1];
 		}
 
 		// For convenience to macro authors, no error on a blank macro name
 		if (macroName.equalsIgnoreCase(""))
 			return "";
 		
 		// IF the macro is a  @this, then we get the location of the current macro and use that.
 		if (macroLocation.equalsIgnoreCase("this")) {
 			macroLocation = getMacroSource();
 			if (macroLocation.equals(CHAT_INPUT)) {
 				macroLocation = "TOKEN";
 			}
 		}
 		
 		
 		if (macroLocation == null || macroLocation.length() == 0 || macroLocation.equals(CHAT_INPUT)) {
 			// Unqualified names are not allowed.
 			throw new ParserException(String.format("Must specify a location for the macro \"%s\" to be run.",macroName));
 		} else if (macroLocation.equalsIgnoreCase("TOKEN")) {
 			macroContext = new MapToolMacroContext(macroName, "token", MapTool.getPlayer().isGM());
 			// Search token for the macro
 			if (tokenInContext != null) {
 				MacroButtonProperties buttonProps = tokenInContext.getMacro(macroName, false);
 				
 				if (buttonProps == null) {
 					throw new ParserException("Macro not found (" + macroName + "@token)");
 				}
  				
 				macroBody = buttonProps.getCommand(); 
 			}
 
 // These choices are disabled because Lindharin's upcoming macro panel revamp will require
 // a rewrite of how we access campaign and global macros.
 //
 		} else if (macroLocation.equalsIgnoreCase("CAMPAIGN")) {
 			macroContext = new MapToolMacroContext(macroName, "campaign", MapTool.getPlayer().isGM());
 			throw new ParserException("Calling campaign macros is not currently supported.");
 //			CampaignPanel cp = MapTool.getFrame().getCampaignPanel();
 //			Component[] comps = cp.getComponents();
 //			for (Component comp : comps) {
 //				if (comp instanceof CampaignMacroButton) {
 //					CampaignMacroButton cmb = (CampaignMacroButton)comp;
 //					String lbl = cmb.getMacroLabel();
 //					if (lbl.equalsIgnoreCase(macroName)) {
 //						macroBody = cmb.getCommand();
 //						break;
 //					}
 //				}
 //			}			
 		} else if (macroLocation.equalsIgnoreCase("GLOBAL")) {
 			macroContext = new MapToolMacroContext(macroName, "global", MapTool.getPlayer().isGM());
 			throw new ParserException("Calling global macros is not currently supported.");
 //			GlobalPanel gp = MapTool.getFrame().getGlobalPanel();
 //			Component[] comps = gp.getComponents();
 //			for (Component comp : comps) {
 //				if (comp instanceof GlobalMacroButton) {
 //					GlobalMacroButton gmb = (GlobalMacroButton)comp;
 //					String lbl = gmb.getMacroLabel();
 //					if (lbl.equalsIgnoreCase(macroName)) {
 //						macroBody = gmb.getCommand();
 //						break;
 //					}
 //				}
 //			}
 		} else { // Search for a token called macroLocation (must start with "Lib:")
 			macroBody = getTokenLibMacro(macroName, macroLocation);
 			Token token = getTokenMacroLib(macroLocation);
 			boolean secure = true;
 
 			// If the token is not owned by everyone and all owners are GMs then we are in
 			// a secure context as players can not modify the macro so GM can sepcify what
 			// ever they want.
 			if (token != null) {
 				if (token.isOwnedByAll()) {
 					secure = false;
 				} else {
 					Set<String> gmPlayers = new HashSet<String>(); 
 					for (Object o : MapTool.getPlayerList()) {
 						Player p = (Player)o;
 						if (p.isGM()) {
 							gmPlayers.add(p.getName());
 						}
 					}
 					for (String owner : token.getOwners())  {
 						if (!gmPlayers.contains(owner)) {
 							secure = false;
 							break;
 						}
 					}
 				}
 			}
 			macroContext = new MapToolMacroContext(macroName, macroLocation, secure);
 	
 		}
 
 		// Error if macro not found
 		if (macroBody == null) {
 			throw new ParserException(String.format("Unknown macro \"%s\"", macroName));
 		}
 
 		MapToolVariableResolver macroResolver = new MapToolVariableResolver(tokenInContext);
 		macroResolver.setVariable("macro.args", args);
 		macroResolver.setVariable("macro.return", "");
 
 		// Call the macro
 		macroRecurseDepth++;
 		if (macroRecurseDepth > MACRO_MAX_RECURSE) {
 			throw new ParserException("Max macro recurse depth reached");
 		}
 		try {
 			String macroOutput = runMacroBlock(macroResolver, tokenInContext, macroBody, macroContext);
 			// Copy the return value of the macro into our current variable scope.
 			resolver.setVariable("macro.return", macroResolver.getVariable("macro.return"));
 			return macroOutput;
 		} finally {
 //			exitContext();
 			macroRecurseDepth--;
 		}
 	}
 
 
 	/** Executes a string as a block of macro code. */
 	String runMacroBlock(MapToolVariableResolver resolver, Token tokenInContext, String macroBody) throws ParserException {
 		return runMacroBlock(resolver, tokenInContext, macroBody, null);
 	}
 
 	/** Executes a string as a block of macro code. */
 	String runMacroBlock(MapToolVariableResolver resolver, Token tokenInContext, String macroBody, MapToolMacroContext context) throws ParserException {
 		String macroOutput = parseLine(resolver, tokenInContext, macroBody, context);
 		return macroOutput;
 	}
 	
 
 	
 	/** Searches all maps for a token and returns the body of the requested macro.
 	 * 
 	 * @param macro The name of the macro to fetch.
 	 * @param location The name of the token containing the macro.  Must begin with "lib:".
 	 * @return The body of the requested macro.
 	 * @throws ParserException if the token name is illegal, the token appears multiple times,
 	 * or if the caller doesn't have access to the token.
 	 */
 	public String getTokenLibMacro(String macro, String location) throws ParserException {
 		Token token = getTokenMacroLib(location);
 		if (token == null) {
 			throw new ParserException("Unknown Library token (" + location + ")");
 		}
 		MacroButtonProperties buttonProps = token.getMacro(macro, false);
 		if (buttonProps == null) {
 			// Try the "unknown macro"
 			buttonProps = token.getMacro(UNKNOWN_LIB_MACRO, false);
 			if (buttonProps == null) {
 				throw new ParserException("Unknown macro " + macro + "@" + location);
 			}
 		}
 
 		return buttonProps.getCommand();
 	}
 	
 	/** 
 	 * Searches all maps for a token and returns the the requested lib: macro.
 	 * 
 	 * @param macro The name of the macro to fetch.
 	 * @return The token which holds the library.
 	 * @throws ParserException if the token name is illegal, the token appears multiple times,
 	 * or if the caller doesn't have access to the token.
 	 */
 	public Token getTokenMacroLib(String location) throws ParserException {		
 		if (!location.matches("(?i)^lib:.*")) {
 			throw new ParserException("Macros from other tokens are only available if the token name starts with \"Lib:\"");
 		}
 		final String libTokenName = location;
 		Token libToken = null;
 		if (libTokenName != null && libTokenName.length() > 0) {
 			List<ZoneRenderer> zrenderers = MapTool.getFrame().getZoneRenderers();
 			for (ZoneRenderer zr : zrenderers) {
 				List<Token> tokenList = zr.getZone().getTokensFiltered(new Zone.Filter() {
 					public boolean matchToken(Token t) {
 						return t.getName().equalsIgnoreCase(libTokenName);
 					}});
 
 				for (Token token : tokenList) {
 					// If we are not the GM and the token is not visible to players then we don't
 					// let them get functions from it.	
 					if (!MapTool.getPlayer().isGM() && !token.isVisible()) {
 						throw new ParserException("Unable to execute macro from  " + libTokenName);
 					}
 					if (libToken != null) {
 						throw new ParserException("Duplicate " + libTokenName + " tokens");
 					}
 
 					libToken = token;
 				}
 			}
 			return libToken;
 		} 
 		return null;
 	}
 	
 
 	/**
 	 * Searches all maps for a token and returns the zone that the lib: macro is in.
 	 * 
 	 * @param macro The name of the macro to fetch.
 	 * @return The zone which holds the library.
 	 * @throws ParserException if the token name is illegal, the token appears multiple times,
 	 * or if the caller doesn't have access to the token.
 	 */
 	public Zone getTokenMacroLibZone(String location) throws ParserException {
 		if (!location.matches("(?i)^lib:.*")) {
 			throw new ParserException("Macros from other tokens are only available if the token name starts with \"Lib:\"");
 		}
 		final String libTokenName = location;
 		Zone libTokenZone = null;
 		if (libTokenName != null && libTokenName.length() > 0) {
 			List<ZoneRenderer> zrenderers = MapTool.getFrame().getZoneRenderers();
 			for (ZoneRenderer zr : zrenderers) {
 				List<Token> tokenList = zr.getZone().getTokensFiltered(new Zone.Filter() {
 					public boolean matchToken(Token t) {
 						return t.getName().equalsIgnoreCase(libTokenName);
 					}});
 
 				for (Token token : tokenList) {
 					// If we are not the GM and the token is not visible to players then we don't
 					// let them get functions from it.	
 					if (!MapTool.getPlayer().isGM() && !token.isVisible()) {
 						throw new ParserException("Unable to execute macro from  " + libTokenName);
 					}
 					if (libTokenZone != null) {
 						throw new ParserException("Duplicate " + libTokenName + " tokens");
 					}
 
 					libTokenZone = zr.getZone();
 				}
 			}
 			return libTokenZone;
 		} 
 		return null;
 	}
 	
 	/** Throws a helpful ParserException that shows the roll options and body.
 	 * @param msg The message
 	 * @param opts The roll options
 	 * @param roll The roll body */
 	void doError(String msg, String opts, String roll) throws ParserException {
 		throw new ParserException(errorString(msg, opts, roll));
 	}
 
 	/** Builds a formatted string showing the roll options and roll body. */
 	String errorString(String msg, String opts, String roll) {
 		String retval = "<br>&nbsp;&nbsp;&nbsp;" + msg;
 		retval += "<br>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<u>Statement options (if any)</u>: " + opts;
 		if (roll.length() <= 200) {
 			retval += "<br>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<u>Statement body</u>: " + roll;
 		} else {
 			retval += "<br>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<u>Statement body (first 200 characters)</u>: " + roll.substring(0,199);
 		}
 		return retval;
 	}
 
 	private ExpressionParser createParser(VariableResolver resolver, boolean hasTokenInContext) {
 		ExpressionParser parser = new ExpressionParser(resolver);
 		parser.getParser().addFunctions(mapToolParserFunctions);
 		return parser;
 	}
 	
 	
 	/** 
 	 * Enters a new context for the macro. 
 	 * @param context The context for the macro.
 	 * If context is null and there is a current context it is reentered. If context is null
 	 * and there is no current context then a new top level context is created.
 	 */
 	public void enterContext(MapToolMacroContext context) {
 		// First time through set our trusted path to same as first context.
 		// Any subsequent trips through we only change trusted path if conext
 		// is not trusted (if context == null on subsequent calls we dont change
 		// anything as trusted context will remain the same as it was before the call).
 		if (contextStack.size() == 0) {
 			macroPathTrusted = context == null ? false : context.isTrusted();
 			macroButtonIndex = context == null ? -1 : context.getMacroButtonIndex();
 		} else if (context != null){
 			if (!context.isTrusted()) {
 				macroPathTrusted = false;
 			}
 		}
 			
 		if (context == null) {
 			if (contextStack.size() == 0) {
 				context = new MapToolMacroContext(MapToolLineParser.CHAT_INPUT, MapToolLineParser.CHAT_INPUT, 
 						MapTool.getPlayer().isGM());
 			} else {
 				context = contextStack.peek();
 			}
 		}
 		contextStack.push(context);
 	}
 	
 	/** 
 	 * Leaves the current context reverting to the previous context.
 	 * @return The context that you leave.
 	 */
 	public MapToolMacroContext exitContext() {
 		return contextStack.pop();
 	}
 	
 	/**
 	 * Convenience method to enter a new trusted context.
 	 * @param name The name of the macro.
 	 * @param source Where the macro comes from.
 	 */
 	public void enterTrustedContext(String name, String source) {
 		enterContext(new MapToolMacroContext(name, source, true));
 	}
 	
 	/**
 	 * Gets the current context for the execution. It is save to enter a context
 	 * a second time.
 	 * @return the current context.
 	 */
 	public MapToolMacroContext getContext() {
 		return contextStack.peek();
 	}
 	
 	/**
 	 * Convenience method to enter a new insecure context.
 	 * @param name The name of the macro.
 	 * @param source Where the macro comes from.
 	 */	
 	public void enterUntrustedContext(String name, String source) {
 		enterContext(new MapToolMacroContext(name, source, false));
 	}
 
 	/**
 	 * Convenience method to enter a new context.
 	 * @param name The name of the macro.
 	 * @param source Where the macro comes from.
 	 * @param secure Is the context secure or not.
 	 */
 	public void enterContext(String name, String source, boolean secure) {
 		enterContext(new MapToolMacroContext(name, source, secure));
 	}
 
 	/**
 	 * Returns if the context stack is empty.
 	 * @return true if the context stack is empty.
 	 */
 	private boolean contextStackEmpty() {
 		return contextStack.size() == 0;
 	}
 	
 	/**
 	 * Gets the macro name for the current context.
 	 * @return The macro name for the current context.
 	 */
 	public String getMacroName() {
 		return contextStack.peek().getName();
 	}
 	
 	/**
 	 * Gets the name of the source for where the macro resides.
 	 * @return The name of the source for where the macro resides.
 	 */
 	public String getMacroSource() {
 		return contextStack.peek().getSouce();
 	}
 	
 	/**
 	 * Gets if the macro context is trusted or not.
 	 * @return if the macro context is trusted or not.
 	 */
 	public boolean isMacroTrusted() {
 		
 		return contextStack.peek().isTrusted();
 	}
 
 
 	/**
 	 * Locate the inline rolls within the input line.
 	 * @param line The line to search for the rolls in.
 	 * @return A list of the rolls.
 	 */
 	private List<InlineRollMatch> locateInlineRolls(String line) {
 
 		List<InlineRollMatch> matches = new ArrayList<InlineRollMatch>();
 		ScanState scanState = ScanState.SEARCHING_FOR_ROLL;
 		int startMatch = 0;
 		int quoteLevel = 0;
 		int bracketLevel = 0;
 		char quoteChar = ' ';
 		char bracketChar = ' ';
 		ScanState savedState = null;
 		int optEnd = -1;
 		
 		for (int i = 0, strMax = line.length(); i < strMax; i++) {
 			char c = line.charAt(i);
 			switch (scanState) {
 			case SEARCHING_FOR_ROLL:
 					if (c == '{' || c == '[') {
 						startMatch = i;
 						scanState = ScanState.SEARCHING_FOR_CLOSE_BRACKET;
 						bracketChar = c;
 						bracketLevel++;
 						optEnd = -1;
 					}
 					break;
 					
 				case SEARCHING_FOR_CLOSE_BRACKET:
 					if (c == bracketChar) {
 						bracketLevel++;
 					} else if (bracketChar == '[' && c == ']') {
 						bracketLevel--;
 						if (bracketLevel == 0) {
 							matches.add(new InlineRollMatch(startMatch, i, line.substring(startMatch, i+1), optEnd));
 							scanState = ScanState.SEARCHING_FOR_ROLL;
 						}
 					} else if (bracketChar == '{' && c == '}') {
 						bracketLevel--;
 						if (bracketLevel == 0) {
 							matches.add(new InlineRollMatch(startMatch, i, line.substring(startMatch, i+1), optEnd));
 							scanState = ScanState.SEARCHING_FOR_ROLL;
 						}						
 					} else if (c == '"' || c == '\'') {
 						quoteChar = c;
 						quoteLevel++;
 						scanState = ScanState.SEARCHING_FOR_QUOTE;
 					} else if (c == '\\') {
 						savedState = scanState;
 						scanState = ScanState.SKIP_NEXT_CHAR;
 					} else if (bracketChar == '[' && optEnd == -1 && c == ':') {
 						optEnd = i;
 					}
 					break;
 					
 				case SEARCHING_FOR_QUOTE:
 					if (c == quoteChar) {
 						scanState = ScanState.SEARCHING_FOR_CLOSE_BRACKET;
 					} else if (c == '\\') {
 						savedState = scanState;
 						scanState = ScanState.SKIP_NEXT_CHAR;
 					}
 					break;
 					
 				case SKIP_NEXT_CHAR:
 					scanState = savedState;
 					break;
 			}
 		}
 					
 		return matches;
 	
 	}
 	
 	/**
 	 * Gets if the whole of the macro path up to this point has been running in a
 	 * trusted context.
 	 * @return true if the whole of the macro path is running in trusted context.
 	 */
 	public boolean isMacroPathTrusted() {
 		return macroPathTrusted;
 	}
 	
 	/**
 	 * Gets the index of the macro button on the index token.
 	 * @return the index of the macro button.
 	 */
 	public int getMacroButtonIndex() {
 		return macroButtonIndex;
 	}
 }
