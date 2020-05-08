 package swp_compiler_ss13.fuc.parser.parser.tables;
 
 import java.util.HashMap;
 import java.util.Map;
 
 import swp_compiler_ss13.fuc.parser.grammar.Terminal;
 import swp_compiler_ss13.fuc.parser.parser.states.LRParserState;
 import swp_compiler_ss13.fuc.parser.parser.tables.actions.ALRAction;
 import swp_compiler_ss13.fuc.parser.parser.tables.actions.Error;
 
 /**
  * This class stores associations of the form (current {@link LRParserState},
  * current {@link Terminal}) -> next {@link ALRAction}.
  */
 public class LRActionTable {
 	// --------------------------------------------------------------------------
 	// --- variables and constants ----------------------------------------------
 	// --------------------------------------------------------------------------
 	private final Map<LRTableKey, ALRAction> table = new HashMap<>();
 
 	// --------------------------------------------------------------------------
 	// --- constructors ---------------------------------------------------------
 	// --------------------------------------------------------------------------
 	/**
 	 * @see LRActionTable
 	 */
 	public LRActionTable() {
 
 	}
 
 	// --------------------------------------------------------------------------
 	// --- getter/setter --------------------------------------------------------
 	// --------------------------------------------------------------------------
 	/**
 	 * @param curState
 	 * @param curTerminal
 	 * @return The formerly stored {@link ALRAction} associated with the given
 	 *         {@link LRParserState} and {@link Terminal}. If there is no entry,
 	 *         an {@link Error} is returned.
 	 * @see #getWithNull(LRParserState, Terminal)
 	 */
 	public ALRAction get(LRParserState curState, Terminal curTerminal) {
 		LRTableKey key = new LRTableKey(curState, curTerminal);
 		ALRAction result = table.get(key);
 		if (result == null) {
			return new Error("No entry for " + key.getSymbol() + " in parsetable!");
 		} else {
 			return result;
 		}
 	}
 
 	/**
 	 * @param curState
 	 * @param curTerminal
 	 * @return The formerly stored {@link ALRAction} associated with the given
 	 *         {@link LRParserState} and {@link Terminal}. If there is no entry,
 	 *         <code>null</code> is returned!
 	 */
 	public ALRAction getWithNull(LRParserState curState, Terminal curTerminal) {
 		LRTableKey key = new LRTableKey(curState, curTerminal);
 		return table.get(key);
 	}
 
 	/**
 	 * Sets the given action for the state/terminal combination. Throws a
 	 * {@link DoubleEntryException} if there already is a action for this
 	 * combination.
 	 * 
 	 * @param action
 	 * @param curState
 	 * @param curTerminal
 	 * @throws DoubleEntryException
 	 */
 	public void set(ALRAction action, LRParserState curState,
 			Terminal curTerminal) throws DoubleEntryException {
 		LRTableKey key = new LRTableKey(curState, curTerminal);
 		if (table.containsKey(key)) {
 			ALRAction oldAction = table.get(key);
 			throw new DoubleEntryException("There already is a state "
 					+ oldAction + " for the key " + key + "! (new state: "
 					+ action + ")");
 		}
 		table.put(key, action);
 	}
 
 	/**
 	 * Sets the given state for the state/terminal combination. This method
 	 * overwrites previous stored actions without further notice.
 	 * 
 	 * @param action
 	 * @param curState
 	 * @param curTerminal
 	 * @throws DoubleEntryException
 	 */
 	public void setHard(ALRAction action, LRParserState curState,
 			Terminal curTerminal) {
 		LRTableKey key = new LRTableKey(curState, curTerminal);
 		table.put(key, action);
 	}
 }
