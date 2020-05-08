 package htmlscanner;
 
 import java.io.File;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Map.Entry;
 
 import javax.management.RuntimeErrorException;
 
 import org.apache.commons.lang.math.NumberUtils;
 import org.ho.yaml.Yaml;
 
 import scanner.ScanResult;
 
 import com.google.common.base.Preconditions;
 import com.google.common.collect.ImmutableList;
 import com.google.common.collect.Lists;
 import com.google.common.collect.Maps;
 
 public class Automaton {
 	Map<String, Map<String, Object>> semanticActions = Maps.newHashMap();
 	Map<String, Map<Character, String>> transitions = Maps.newHashMap();
 	Map<String, String> reservedWords = Maps.newHashMap();
 
 	@SuppressWarnings({ "unchecked", "rawtypes" })
 	public void init(String fileName) {
 		try {
 			Map<String, Object> config = Yaml.loadType(new File(fileName),
 					HashMap.class);
 			semanticActions = (Map<String, Map<String, Object>>) config
 					.get("semanticActions");
 			Map<String, String> aliases = (Map<String, String>) config
 					.get("aliases");
 			Map aux = (Map) config.get("transitions");
 
 			reservedWords = (Map<String, String>) config.get("reservedWords");
 
 			for (Object obj : aux.entrySet()) {
 				Entry ent = (Entry) obj;
 				Map<Character, String> value = Maps.newHashMap();
 				for (Object obj0 : ((Map) ent.getValue()).entrySet()) {
 					Entry ent1 = (Entry) obj0;
 					String keyValue = String.valueOf(ent1.getKey());
 					StringBuilder str = new StringBuilder();
 					for (String keyValuePart : keyValue.split("\\+")) {
 						if (aliases.containsKey(keyValuePart)) {
 							str.append(String.valueOf(aliases.get(keyValuePart)));
 						} else {
 							str.append(keyValuePart);
 						}
 					}
 					String key = str.toString();
 					for (char c : key.toCharArray()) {
 						value.put(c, String.valueOf(ent1.getValue()));
 					}
 				}
 				transitions.put((String) ent.getKey(), value);
 			}
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 	}
 
 	protected int initialPos;
 	int pos = 0;
 	char index;
 	String state = "initial";
 	List<Character> temp = Lists.newArrayList();
 	private List<String> deltaExec = Lists.newArrayList();;
 
 	public List<String> getDeltaExec() {
 		return ImmutableList.copyOf(deltaExec);
 	}
 
 	String getTempString() {
 		StringBuilder str = new StringBuilder();
 		for (Character a : temp) {
 			str.append(a);
 		}
 		return str.toString();
 	}
 
 	@Override
 	public String toString() {
 		return "Automaton [pos=" + pos + ", index='" + index + "', state="
 				+ state.toUpperCase() + ", temp=\"" + temp + "\"]";
 	}
 
 	public void execEpsilon() {
 		// volver a "initial", ejecucion epsilon
 		state = "initial";
 	}
 
 	public void goBack() {
 		// volver el puntero 1 para atras
 		pos--;
 	}
 
 	public void cleanTemp() {
 		// vaciar temporal
 		temp.clear();
 		// restart pos inicial temporal
 		initialPos = -1;
 	}
 
 	public ScanResult scan(String input) {
 		List<Token> tokens = Lists.newArrayList();
 		List<String> errors = Lists.newArrayList();
 		try {
 			setInputToScan(input);
 			Token t = null;
 			while ((t = getNextToken()) != null) {
 				if (t.code.equals(TokenType.ERROR)) {
 					errors.add(t.getValue());
 				} else {
 					tokens.add(t);
 				}
 			}
 		} catch (Exception e) {
 			System.err.println("ERROR:     estado automata:\n" + toString());
 			e.printStackTrace();
 		}
 		ScanResult result = new ScanResult();
 		result.setErrors(errors);
 		result.setTokens(tokens);
 		result.setDeltaExec(deltaExec);
 		return result;
 	}
 
 	private boolean getBooleanValue(Map<String, Object> action, String key) {
 		return action.containsKey(key) ? (Boolean) action.get(key) : false;
 	}
 
 	String inputToScan;
 
 	public void setInputToScan(String string) {
 		inputToScan = string;
 	}
	int column = 0;
	int line = 0;
 	public Token getNextToken() {
 		try {
 			while (pos <= inputToScan.length()) {
 				if (pos < inputToScan.length()) {
 					index = inputToScan.charAt(pos++);
 				} else {
 					index = '$'; // end of file
 					pos++;
 				}
				
 				if(index == '\n'){
 					line++;
					column = 0;
 				}else{
 					column++;
 				}
 				Preconditions.checkState(transitions.containsKey(state),
 						"can't find state %s in settings", state);
 				String nextState = transitions.get(state).get(index);
 				deltaExec.add(String.format("Delta(%S, '%c') = %S", state,
 						index, nextState));
 				if (nextState == null) {
 					return new Token(TokenType.ERROR, String.format(
 							"ERROR pos %d (%d,%d)", pos, line, column));
 				} else {
 					state = nextState;
 					if (semanticActions.containsKey(state)) {
 						Map<String, Object> action = semanticActions.get(state);
 						if (action.containsKey("discard")
 								&& (Boolean) action.get("discard")) {
 							execEpsilon();
 						} else {
 							if (action.containsKey("save_value")
 									&& (Boolean) action.get("save_value")) {
 								temp.add(index);
 								if (initialPos < 0) {
 									initialPos = pos - 1;
 								}
 							} else { // Estado final y por lo tanto epsilon a
 										// inicial
 								String tokenId = (String) action
 										.get("token_id");
 								boolean reset = getBooleanValue(action, "reset");
 								boolean clean = getBooleanValue(action, "clean");
 								boolean haveValue = getBooleanValue(action,
 										"have_value");
 								Token token = null;
 								if (haveValue) {
 									String value = getTempString();
 									if (action.containsKey("limit")) {
 										Preconditions.checkState(NumberUtils
 												.isNumber(String.valueOf(action
 														.get("limit"))));
 										int limit = (Integer) action
 												.get("limit");
 										if(limit< value.length()){
 											System.err.println(String.format(
 													"ERROR pos %d: token exceed maximum length %d", pos, limit));
 										}
 										
 										value = value
 												.substring(0, Math.min(
 														value.length(), limit));
 									}
 									/* Validacion de palabras reservadas */
 									if ((tokenId.equals(TokenType.ID.name()) || tokenId.equals(TokenType.CDATA.name())) && reservedWords.containsKey(value)) {
 										String reservedId = reservedWords.get(value);
 										token = new Token(TokenType.valueOf(reservedId));
 									}
 									else{
 										token = new Token(TokenType.valueOf(tokenId), value);
 									}
 								} else {
 									token = new Token(TokenType.valueOf(tokenId));
 								}
 								if (reset) {
 									goBack();
 								}
 								if (clean) {
 									cleanTemp();
 								}
 								execEpsilon();
 								return token;
 							}
 						}
 					}
 				}
 			}
 		} catch (Exception e) {
 			System.err.println("ERROR:     estado automata:\n" + toString());
 			throw new RuntimeException(e);
 		}
 		return null;
 	}
 
 }
