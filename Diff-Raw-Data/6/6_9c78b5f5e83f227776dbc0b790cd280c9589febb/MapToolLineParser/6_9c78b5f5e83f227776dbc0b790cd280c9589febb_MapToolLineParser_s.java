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
 
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import net.rptools.common.expression.ExpressionParser;
 import net.rptools.common.expression.Result;
 import net.rptools.maptool.client.functions.AbortFunction;
 import net.rptools.maptool.client.functions.AddAllToInitiativeFunction;
 import net.rptools.maptool.client.functions.CurrentInitiativeFunction;
 import net.rptools.maptool.client.functions.InitiativeRoundFunction;
 import net.rptools.maptool.client.functions.InputFunction;
 import net.rptools.maptool.client.functions.MiscInitiativeFunction;
 import net.rptools.maptool.client.functions.RemoveAllFromInitiativeFunction;
 import net.rptools.maptool.client.functions.StrListFunctions;
 import net.rptools.maptool.client.functions.StrPropFunctions;
 import net.rptools.maptool.client.functions.TokenAddToInitiativeFunction;
 import net.rptools.maptool.client.functions.TokenBarFunction;
 import net.rptools.maptool.client.functions.TokenGMNameFunction;
 import net.rptools.maptool.client.functions.TokenHaloFunction;
 import net.rptools.maptool.client.functions.TokenInitFunction;
 import net.rptools.maptool.client.functions.TokenInitHoldFunction;
 import net.rptools.maptool.client.functions.TokenLabelFunction;
 import net.rptools.maptool.client.functions.LookupTableFunction;
 import net.rptools.maptool.client.functions.TokenNameFunction;
 import net.rptools.maptool.client.functions.StateImageFunction;
 import net.rptools.maptool.client.functions.TokenImage;
 import net.rptools.maptool.client.functions.TokenRemoveFromInitiativeFunction;
 import net.rptools.maptool.client.functions.TokenStateFunction;
 import net.rptools.maptool.client.functions.TokenVisibleFunction;
 import net.rptools.maptool.model.Token;
 import net.rptools.parser.ParserException;
 import net.rptools.parser.VariableResolver;
 import net.rptools.parser.function.Function;
 
 public class MapToolLineParser {
 
     private static final int PARSER_MAX_RECURSE = 50;
 
     private enum Output {
     	NONE,
     	PLAIN,
     	TOOLTIP,
     	EXPANDED,
     }
     
     private int parserRecurseDepth;
     
     /** MapTool functions to add to the parser.  */
     private static final Function[] mapToolParserFunctions = {
     	StateImageFunction.getInstance(),
     	LookupTableFunction.getInstance(),
     	TokenImage.getInstance(),
     	AddAllToInitiativeFunction.getInstance(),
     	MiscInitiativeFunction.getInstance(),
     	RemoveAllFromInitiativeFunction.getInstance(),
     	CurrentInitiativeFunction.getInstance(),
     	InitiativeRoundFunction.getInstance(),
     	InputFunction.getInstance(),
     	StrPropFunctions.getInstance(),
     	StrListFunctions.getInstance(),
     	AbortFunction.getInstance(),
     };
     
     /** MapTool functions to add to the parser when a token is in context. */
     private static final Function[] mapToolContextParserFunctions = {
     	TokenGMNameFunction.getInstance(),
     	TokenHaloFunction.getInstance(),
     	TokenLabelFunction.getInstance(),
     	TokenNameFunction.getInstance(),
     	TokenStateFunction.getInstance(),
     	TokenVisibleFunction.getInstance(),
     	TokenInitFunction.getInstance(),
     	TokenInitHoldFunction.getInstance(),
     	TokenAddToInitiativeFunction.getInstance(),
     	TokenRemoveFromInitiativeFunction.getInstance(),
     	TokenBarFunction.getInstance(),
     };
 
     
     public String parseLine(String line) throws ParserException {
     	return parseLine(null, line);
     }
 
     // This is starting to get ridiculous... I sense an incoming rewrite using ANTLR
     private static final Pattern roll_pattern = Pattern.compile("\\[\\s*(?:((?:[^\\]:(]|\\((?:[^()\"]|\"[^\"]*\"|\\((?:[^)\"]|\"[^\"]*\")+\\))+\\))*):\\s*)?((?:[^\\]\"]|\"[^\"]*\")*?)\\s*]|\\{\\s*((?:[^}\"]|\"[^\"]*\")*?)\\s*}");
 	private static final Pattern opt_pattern = Pattern.compile("(\\w+(?:\\((?:[^()\"]|\"[^\"]*\"|\\((?:[^()\"]|\"[^\"]*\")+\\))+\\))?)\\s*,\\s*");
 
     public String parseLine(Token tokenInContext, String line) throws ParserException {
 
     	if (line == null) {
     		return "";
     	}
     	
     	line = line.trim();
     	if (line.length() == 0) {
     		return "";
     	}
     	
     	// Keep the same context for this line
     	MapToolVariableResolver resolver = new MapToolVariableResolver(tokenInContext);
 
     	StringBuilder builder = new StringBuilder();
     	Matcher matcher = roll_pattern.matcher(line);
     	int start;
     	
     	for (start = 0; matcher.find(start); start = matcher.end()) {
     		builder.append(line.substring(start, matcher.start())); // add everything before the roll
     		Output output = Output.TOOLTIP;
     		int count = 1; // used for C option
    		String separator = ", ", text = null; // used for C and T options, respectively
     		
     		if (matcher.group().startsWith("[")) {
     			String opts = matcher.group(1);
     			String roll = matcher.group(2);
     			if (opts != null) {
     				Matcher opt_matcher = opt_pattern.matcher(opts + ",");
     				int region_end = opt_matcher.regionEnd();
     				for ( ; opt_matcher.lookingAt(); opt_matcher.region(opt_matcher.end(), region_end)) {
     					String opt = opt_matcher.group(1);
 						if (opt.equalsIgnoreCase("h") || opt.equalsIgnoreCase("hide") || opt.equalsIgnoreCase("hidden"))
 							output = Output.NONE;
 						else if (opt.equalsIgnoreCase("p") || opt.equalsIgnoreCase("plain"))
 							output = Output.PLAIN;
 						else if (opt.equalsIgnoreCase("e") || opt.equalsIgnoreCase("expanded"))
 							output = Output.EXPANDED;
 						else if (opt.startsWith("t") || opt.startsWith("T")) {
 							Matcher m = Pattern.compile("t(?:ooltip)?(?:\\(((?:[^()\"]|\"[^\"]*\"|\\((?:[^()\"]|\"[^\"]*\")*\\))+?)\\))?", Pattern.CASE_INSENSITIVE).matcher(opt);
 							if (m.matches()) {
 								output = Output.TOOLTIP;
 								
 								text = m.group(1);
 								if (text != null)
 									text = parseExpression(resolver, tokenInContext, text).getValue().toString();
 							} else {
 								throw new ParserException("Invalid option: " + opt);
 							}
 						} else if (opt.startsWith("c") || opt.startsWith("C")) {
 							Matcher m = Pattern.compile("c(?:ount)?\\(((?:[^()\"]|\"[^\"]*\"|\\((?:[^()\"]|\"[^\"]*\")*\\))+?)\\)", Pattern.CASE_INSENSITIVE).matcher(opt);
 							if (m.matches()) {
 								String args[] = m.group(1).split(",", 2);
 								Result result = parseExpression(resolver, tokenInContext, args[0]);
 								try {
 									count = ((Number)result.getValue()).intValue();
 									if (count < 0)
 										throw new ParserException("Invalid count: " + String.valueOf(count));
 								} catch (ClassCastException e) {
 									throw new ParserException("Invalid count: " + result.getValue().toString());
 								}
 								
 								if (args.length > 1) {
 									separator = args[1];
 								}
 							} else {
 								throw new ParserException("Invalid option: " + opt);
 							}
 						} else {
 							throw new ParserException("Invalid option: " + opt);
 						}
 	    			}
     				
     				if (!opt_matcher.hitEnd()) {
     					throw new ParserException("Invalid option: " + opts.substring(opt_matcher.regionStart()));
     				}
     			}
 
     	    	StringBuilder expressionBuilder = new StringBuilder();
     			for (int i = 0; i < count; i++) {
     				if (i != 0 && output != Output.NONE) {
//    					expressionBuilder.append(parseExpression(resolver, tokenInContext, separator).getValue());
    					expressionBuilder.append(separator);
     				}
     				
     				resolver.setVariable("roll.count", i + 1);
         			Result result;
 	    			switch (output) {
 	    			case NONE:
 	    				parseExpression(resolver, tokenInContext, roll);
 	    				break;
 	    			case PLAIN:
 	        			result = parseExpression(resolver, tokenInContext, roll);
 	        			expressionBuilder.append(result != null ? result.getValue().toString() : "");
 	    				break;
 	    			case TOOLTIP:
 	        			String tooltip = roll + " = ";
 	        			String output_text = null;
 	        			if (text == null) {
 		        			result = parseExpression(resolver, tokenInContext, roll);
 		        			if (result != null) {
 		        				tooltip += result.getDetailExpression();
 		        				output_text = result.getValue().toString();
 		        			}
 	        			} else {
 	        				tooltip += expandRoll(resolver, tokenInContext, roll);
 	        				output_text = text;
 	        			}
 	        			tooltip = tooltip.replaceAll("'", "&#39;");
 	        			expressionBuilder.append(output_text != null ? "\036" + tooltip + "\037" + output_text + "\036" : "");
 	    				break;
 	    			case EXPANDED:
 	    				expressionBuilder.append("\036" + roll + " = " + expandRoll(resolver, tokenInContext, roll) + "\36" );
 	    				break;
 	    			}
     			}
     			builder.append(expressionBuilder);
     		} else if (matcher.group().startsWith("{")) {
     			String roll = matcher.group(3);
     			Result result = parseExpression(resolver, tokenInContext, roll);
     			builder.append(result != null ? result.getValue().toString() : "");
     		}
     	}
     	
     	builder.append(line.substring(start));
     	
    		return builder.toString();
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
     
     private ExpressionParser createParser(VariableResolver resolver, boolean hasTokenInContext) {
     	ExpressionParser parser = new ExpressionParser(resolver);
         parser.getParser().addFunctions(mapToolParserFunctions);
         if (hasTokenInContext) {
             parser.getParser().addFunctions(mapToolContextParserFunctions);
         }
     	return parser;
     }
 }
