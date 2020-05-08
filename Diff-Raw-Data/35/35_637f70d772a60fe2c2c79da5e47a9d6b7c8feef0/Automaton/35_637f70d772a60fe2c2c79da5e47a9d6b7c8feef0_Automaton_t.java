 package htmlscanner;
 
 import java.io.File;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Map.Entry;
 
 import org.ho.yaml.Yaml;
 
 import scanner.ScanResult;
 
 import com.google.common.collect.Lists;
 import com.google.common.collect.Maps;
 
 public class Automaton {
 	Map<String, Map<String, Object>> semanticActions = Maps.newHashMap();
 	Map<String, Map<Character, String>> transitions = Maps.newHashMap();
 	@SuppressWarnings({ "unchecked", "rawtypes" })
 	public void init(String fileName) {
 		try {
 			Map<String, Object> config = Yaml.loadType(new File(
 					fileName), HashMap.class);
 			semanticActions = (Map<String, Map<String, Object>>) config
 					.get("semanticActions");
 			Map aux= (Map) config
 					.get("transitions");
 			for(Object obj: aux.entrySet()){
 				Entry ent = (Entry)obj;
 				Map<Character,String> value = Maps.newHashMap();
 				for(Object obj0: ((Map)ent.getValue()).entrySet()){
 					Entry ent1 = (Entry) obj0;
 					value.put(String.valueOf(ent1.getKey()).charAt(0), String.valueOf(ent1.getValue()));
 				}
 				transitions.put((String)ent.getKey(), value);
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
 
 	public void goBackAndReset() {
 		// volver el puntero 1 para atras
 		pos--;
 		// vaciar temporal
 		temp.clear();
 		// restart pos inicial temporal
 		initialPos = -1;
 	}
 
 	
 	public ScanResult scan(String input) {
 		List<Token> tokens = Lists.newArrayList();
 		List<String> errors = Lists.newArrayList();
 		try {
 			while (pos <= input.length()) {
 				if (pos < input.length()) {
 					index = input.charAt(pos++);
 				} else {
 					index = '$'; // end of file
 					pos++;
 				}
 				String nextState = transitions.get(state).get(index);
 				//System.out.println(String.format("Delta(%S, '%c') = %S",
//						state, index, nextState));
 				if (nextState == null) {
 					errors.add(String.format("ERROR pos %d", pos));
 				} else {
 					state = nextState;
 					if (semanticActions.containsKey(state)) {
 						Map<String, Object> action = semanticActions
 								.get(state);
 						if (action.containsKey("save_value")
 								&& (Boolean) action.get("save_value")) {
 							//System.out.println("guardo valor");
 							temp.add(index);
 							if (initialPos < 0) {
 								initialPos = pos - 1;
 							}
 						} else { // Estado final y por lo tanto epsilon a
 									// inicial
 							String tokenId = (String) action.get("token_id");
 							boolean reset = (Boolean) action.get("reset");
 							boolean haveValue = (Boolean) action
 									.get("have_value");
 							Token token = new Token(tokenId,
 									haveValue ? getTempString() : null);
 							tokens.add(token);
 							if (haveValue) {
 								//System.out.println(String.format(
//										"==> en pos %d hasta %d, (%s, %s)",
//										initialPos, pos - 2,
//										token.getCode(), token.getValue()));
 							} else {
 								//System.out.println(String.format(
//										"==> en pos %d, (%s, null)",
//										pos - 1, token.getCode()));
 							}
 							if (reset) {
 								//System.out.println("reset");
 								goBackAndReset();
 							}
 							execEpsilon();
 						}
 					}
 				}
 			}
 		} catch (Exception e) {
 			System.err.println("ERROR:     estado automata:\n" + toString());
 			e.printStackTrace();
 		}
 		ScanResult result = new ScanResult();
 		result.setErrors(errors);
 		result.setTokens(tokens);
 		return result;
 	}
 	
 	
 
 }
