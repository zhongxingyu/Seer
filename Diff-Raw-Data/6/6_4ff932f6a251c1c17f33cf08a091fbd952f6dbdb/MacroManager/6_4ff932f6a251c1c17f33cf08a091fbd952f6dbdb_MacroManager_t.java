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
 package net.rptools.maptool.client.macro;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import net.rptools.maptool.client.MapTool;
 import net.rptools.maptool.client.MapToolMacroContext;
 import net.rptools.maptool.client.functions.AbortFunction;
 import net.rptools.maptool.client.functions.AssertFunction;
 import net.rptools.maptool.client.macro.impl.AddTokenStateMacro;
 import net.rptools.maptool.client.macro.impl.AliasMacro;
 import net.rptools.maptool.client.macro.impl.ChangeColorMacro;
 import net.rptools.maptool.client.macro.impl.ClearAliasesMacro;
 import net.rptools.maptool.client.macro.impl.ClearMacro;
 import net.rptools.maptool.client.macro.impl.EmitMacro;
 import net.rptools.maptool.client.macro.impl.EmoteMacro;
 import net.rptools.maptool.client.macro.impl.GotoMacro;
 import net.rptools.maptool.client.macro.impl.HelpMacro;
 import net.rptools.maptool.client.macro.impl.ImpersonateMacro;
 import net.rptools.maptool.client.macro.impl.LoadAliasesMacro;
 import net.rptools.maptool.client.macro.impl.LoadTokenStatesMacro;
 import net.rptools.maptool.client.macro.impl.LookupTableMacro;
 import net.rptools.maptool.client.macro.impl.OOCMacro;
 import net.rptools.maptool.client.macro.impl.RollAllMacro;
 import net.rptools.maptool.client.macro.impl.RollGMMacro;
 import net.rptools.maptool.client.macro.impl.RollMeMacro;
 import net.rptools.maptool.client.macro.impl.RollSecretMacro;
 import net.rptools.maptool.client.macro.impl.RunTokenMacroMacro;
 import net.rptools.maptool.client.macro.impl.RunTokenSpeechMacro;
 import net.rptools.maptool.client.macro.impl.SaveAliasesMacro;
 import net.rptools.maptool.client.macro.impl.SaveTokenStatesMacro;
 import net.rptools.maptool.client.macro.impl.SayMacro;
 import net.rptools.maptool.client.macro.impl.SelfMacro;
 import net.rptools.maptool.client.macro.impl.SetTokenPropertyMacro;
 import net.rptools.maptool.client.macro.impl.SetTokenStateMacro;
 import net.rptools.maptool.client.macro.impl.ToGMMacro;
 import net.rptools.maptool.client.macro.impl.UndefinedMacro;
 import net.rptools.maptool.client.macro.impl.WhisperMacro;
 import net.rptools.maptool.client.macro.impl.WhisperReplyMacro;
 import net.rptools.maptool.model.Token;
 import net.rptools.maptool.model.Zone;
 import net.rptools.maptool.util.StringUtil;
 
 /**
  * @author drice
  * 
  * TODO To change the template for this generated type comment go to Window -
  * Preferences - Java - Code Style - Code Templates
  */
 public class MacroManager {
 
 	private static final int MAX_RECURSE_COUNT = 10;
 
 	private static Macro UNDEFINED_MACRO = new UndefinedMacro();
 
 	private static Map<String, Macro> MACROS = new HashMap<String, Macro>();
 
 	private static Map<String, String> aliasMap = new HashMap<String, String>();
 
 	static {
 		registerMacro(new SayMacro());
 		registerMacro(new HelpMacro());
 		registerMacro(new GotoMacro());
 		registerMacro(new ClearMacro());
 		registerMacro(new RollMeMacro());
 		registerMacro(new RollAllMacro());
 		registerMacro(new RollGMMacro());
 		registerMacro(new WhisperMacro());
 		registerMacro(new EmoteMacro());
 		registerMacro(new AliasMacro());
 		registerMacro(new LoadAliasesMacro());
 		registerMacro(new SaveAliasesMacro());
 		registerMacro(new ClearAliasesMacro());
 		registerMacro(new AddTokenStateMacro());
 		registerMacro(new LoadTokenStatesMacro());
 		registerMacro(new SaveTokenStatesMacro());
 		registerMacro(new SetTokenStateMacro());
 		registerMacro(new SetTokenPropertyMacro());
 		registerMacro(new RollSecretMacro());
 		registerMacro(new EmitMacro());
 		registerMacro(new SelfMacro());
 		registerMacro(new ImpersonateMacro());
 		registerMacro(new RunTokenMacroMacro());
 		registerMacro(new RunTokenSpeechMacro());
 		registerMacro(new LookupTableMacro());
 		registerMacro(new ToGMMacro());
 		registerMacro(new OOCMacro());
 		registerMacro(new ChangeColorMacro());
 		registerMacro(new WhisperReplyMacro());
 
 		registerMacro(UNDEFINED_MACRO);
 	}
 
 	public static void setAlias(String key, String value) {
 		aliasMap.put(key, value);
 	}
 
 	public static void removeAlias(String key) {
 		aliasMap.remove(key);
 	}
 
 	public static void removeAllAliases() {
 		aliasMap.clear();
 	}
 	
 	public static Map<String, String> getAliasMap() {
 		return Collections.unmodifiableMap(aliasMap);
 	}
 	
 	public static Set<Macro> getRegisteredMacros() {
 		Set<Macro> ret = new HashSet<Macro>();
 		ret.addAll(MACROS.values());
 		return ret;
 	}
 
 	public static Macro getRegisteredMacro(String name) {
 		Macro ret = MACROS.get(name);
 		if (ret == null)
 			return UNDEFINED_MACRO;
 		return ret;
 	}
 
 	public static void registerMacro(Macro macro) {
 		MacroDefinition def = macro.getClass().getAnnotation(
 				MacroDefinition.class);
 
 		if (def == null)
 			return;
 
 		MACROS.put(def.name(), macro);
 		for (String alias : def.aliases()) {
 			MACROS.put(alias, macro);
 		}
 	}
 
 	public static void executeMacro(String command) {
 		executeMacro(command, null);
 	}
 	
 	public static void executeMacro(String command, MapToolMacroContext macroExecutionContext) {
 
 		MacroContext context = new MacroContext();
 		context.addTransform(command);
 		
 		try {
 			command = preprocess(command);
 			context.addTransform(command);
 	
 			int recurseCount = 0;
 			while (recurseCount < MAX_RECURSE_COUNT) {
 	
 				recurseCount++;
 	
 				command = command.trim();
 				if (command == null || command.length() == 0) {
 					return;
 				}
 				
 				if (command.charAt(0) == '/') {
 					command = command.substring(1);
 				} else {
 					// Default to a say
 					command = "s " + command;
 				}
 	
 
 				
 				// Macro name is the first word
 				List<String> cmd = StringUtil.splitNextWord(command);
 				String key = cmd.get(0);
 				String details = cmd.size() > 1 ? cmd.get(1) : "";
 	
 				Macro macro = getRegisteredMacro(key);
 				MacroDefinition def = macro.getClass().getAnnotation(
 						MacroDefinition.class);
 
 				boolean trustedPath = macroExecutionContext == null ? false : macroExecutionContext.isTrusted();
 				String macroButtonName = macroExecutionContext == null ? "<chat>" : 
 						macroExecutionContext.getName() + "@" + macroExecutionContext.getSouce();
 				
 				// Preprocess line if required.
 				if (def == null || def.expandRolls()) {
 					// TODO: fix this, wow I really hate this, it's very, very ugly.
					Token tokenInContext = null;
					if (MapTool.getFrame().getCurrentZoneRenderer() != null) {
						tokenInContext = MapTool.getFrame().getCurrentZoneRenderer().getZone().resolveToken(MapTool.getFrame().getCommandPanel().getIdentity());
					}
 					
 					details = MapTool.getParser().parseLine(tokenInContext, details, macroExecutionContext);
 					trustedPath = MapTool.getParser().isMacroPathTrusted();
 					
 				}
 				context.addTransform(key + " " + details);
 				postprocess(details);
 				
 				context.addTransform(key + " " + details);
 				if (macro != UNDEFINED_MACRO) {
 					executeMacro(context, macro, details, trustedPath, macroButtonName);
 					return;
 				}
 
 				// Is it an alias ?
 				String alias = aliasMap.get(key);
 				if (alias == null) {
 
 					executeMacro(context, UNDEFINED_MACRO, command, trustedPath, macroButtonName);
 					return;
 				}
 				
 				command = resolveAlias(alias, details);
 				context.addTransform(command);
 				
 				continue;
 			}
 		} catch (AbortFunction.AbortFunctionException afe) {
 			// Do nothing, just silently exit
 			return;
 		} catch (AssertFunction.AssertFunctionException afe) {
 			MapTool.addLocalMessage("Macro-defined error: " + afe.getMessage());
 			return;
 		} catch (Exception e) {
 			MapTool.addLocalMessage("Could not execute the command: " + e.getMessage());
 			System.err.println("Exception executing command: " + command);
 			e.printStackTrace();
 			return;
 		}
 		
 		// We'll only get here if the recurseCount is exceeded
 		MapTool.addLocalMessage("'" + command
 				+ "': Too many resolves, perhaps an infinite loop?");
 		
 	}
 
 	static String postprocess(String command) {
 		command = command.replace("\n", "<br>");
 		
 		return command;
 		
 	}
 	
 	static String preprocess(String command) {
 		return command;
 	}
 	
 	// Package level for testing
 	static String resolveAlias(String aliasText, String details) {
 		
 		return performSubstitution(aliasText, details);
 	}
 
 	private static final Pattern SUBSTITUTION_PATTERN = Pattern.compile("\\$\\{([^\\}]+)\\}|\\$(\\w+)");
 	// Package level for testing
 	static String performSubstitution(String text, String details){
 		
 		List<String> detailList = split(details);
 
 		StringBuffer buffer = new StringBuffer();
 		Matcher matcher = SUBSTITUTION_PATTERN.matcher(text);
 		while (matcher.find()) {
 			
 			String replacement = details;
 			String replIndexStr = matcher.group(1);
 			if (replIndexStr == null) {
 				replIndexStr = matcher.group(2);
 			}
 			if (!"*".equals(replIndexStr)) {
 				try {
 					int replaceIndex = Integer.parseInt(replIndexStr);
 					if (replaceIndex > detailList.size() || replaceIndex < 1) {
 						replacement = "";
 					} else {
 						// 1-based
 						replacement = detailList.get(replaceIndex-1);
 					}
 				} catch (NumberFormatException nfe) {
 					
 					// Try an alias lookup
 					replacement = aliasMap.get(replIndexStr);
 					if (replacement == null) {
 						replacement = "(error: " + replIndexStr + " is not found)";
 					}
 				}
 			}
 		    matcher.appendReplacement(buffer, replacement);
 		 }
 		 matcher.appendTail(buffer);	
 		 
 		return buffer.toString();
 	}
 	
 	// Package level for testing
 	// TODO: This should probably go in a util class in rplib
 	static List<String> split(String line) {
 		
 		List<String> list = new ArrayList<String>();
 		StringBuilder currentWord = new StringBuilder();
 		boolean isInQuote=false;
 		char previousChar = 0;
 		for (int i = 0; i < line.length(); i++) {
 			
 			char ch = line.charAt(i);
 			
 			try {
 				// Word boundaries
 				if (Character.isWhitespace(ch) && !isInQuote) {
 					if (currentWord.length() > 0) {
 						list.add(currentWord.toString());
 					}
 					currentWord.setLength(0);
 					continue;
 				}
 				
 				// Quoted boundary
 				if (ch == '"' && previousChar != '\\') {
 					
 					if (isInQuote) {
 						isInQuote = false;
 						if (currentWord.length() > 0) {
 							list.add(currentWord.toString());
 							currentWord.setLength(0);
 						}
 					} else {
 						isInQuote = true;
 					}
 					
 					continue;
 				}
 				
 				if (ch == '\\') {
 					continue;
 				}
 				
 				currentWord.append(ch);
 				
 			} finally {				
 				previousChar = ch;
 			}
 		}
 		
 		if (currentWord.length() > 0) {
 			list.add(currentWord.toString());
 		}
 		
 		return list;
 	}
 	
 	private static void executeMacro(MacroContext context, Macro macro, String parameter, boolean trusted, String macroName) {
 		macro.execute(context, parameter, trusted, macroName);
 	}
 
 }
