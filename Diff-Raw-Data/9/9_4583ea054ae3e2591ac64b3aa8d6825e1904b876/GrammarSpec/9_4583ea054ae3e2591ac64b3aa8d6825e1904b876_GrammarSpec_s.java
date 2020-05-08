 package swp_compiler_ss13.fuc.parser.grammar;
 
 import java.lang.reflect.Field;
 import java.util.LinkedList;
 import java.util.List;
 
 /**
  * This is a helper class for defining {@link Grammar}s directly in Java.<br/>
  * Usage: simply derive from this class and instantiate {@link Terminal}s,
  * {@link NonTerminal}s and {@link Production}s as class members. This class
  * creates the {@link Grammar} automatically. For an example, see
  * {@link ProjectGrammar}
  * 
  * @author Gero
  */
 public class GrammarSpec {
 	// --------------------------------------------------------------------------
 	// --- variables and constants
 	// ----------------------------------------------
 	// --------------------------------------------------------------------------
 	private Grammar grammar = null;
 
 	// --------------------------------------------------------------------------
 	// --- constructors
 	// ---------------------------------------------------------
 	// --------------------------------------------------------------------------
 	public GrammarSpec() {
 		try {
 			List<Terminal> terminals = getAllMembers(Terminal.class);
 			List<NonTerminal> nonTerminals = getAllMembers(NonTerminal.class);
 			List<Production> productions = getAllMembers(Production.class);
 			this.grammar = new Grammar(terminals, nonTerminals, productions);
 		} catch (IllegalArgumentException err) {
 			err.printStackTrace();
 		} catch (IllegalAccessException err) {
 			err.printStackTrace();
 		}
 	}
 
 	// --------------------------------------------------------------------------
 	// --- methods
 	// --------------------------------------------------------------
 	// --------------------------------------------------------------------------
 	private <T> List<T> getAllMembers(Class<T> clazz)
 			throws IllegalArgumentException, IllegalAccessException {
 		List<T> result = new LinkedList<>();
 		for (Field field : this.getClass().getDeclaredFields()) {
 			Object obj = field.get(this);
 			if (clazz.isInstance(obj)) {
 				result.add(clazz.cast(obj));
 			}
 		}
 		return result;
 	}
 
 	// --------------------------------------------------------------------------
 	// --- getter/setter
 	// --------------------------------------------------------
 	// --------------------------------------------------------------------------
 	public Grammar getGrammar() {
 		return grammar;
 	}
 }
