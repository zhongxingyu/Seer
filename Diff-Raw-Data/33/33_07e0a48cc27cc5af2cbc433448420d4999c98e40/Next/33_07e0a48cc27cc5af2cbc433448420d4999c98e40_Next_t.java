 package edu.tum.lua.stdlib;
 
 import static edu.tum.lua.Preconditions.checkArguments;
 
 import java.util.Arrays;
import java.util.Collections;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map.Entry;
 
 import edu.tum.lua.LuaRuntimeException;
 import edu.tum.lua.types.LuaFunctionNative;
 import edu.tum.lua.types.LuaTable;
 import edu.tum.lua.types.LuaType;
 
 public class Next extends LuaFunctionNative {
 
 	LuaType[][] expectedTypes = { { LuaType.TABLE } };
 	private Object index;
 	private LuaTable table;
 
 	private Iterator<Entry<Object, Object>> iter;
 	private Entry<Object, Object> nextEntry;
 
 	@Override
 	public List<Object> apply(List<Object> arguments) {
 		/*
 		 * Usage: Next.apply(LuaTable table, [KeyableValue index]). Returns the
 		 * next index and its associated value.
 		 */
 
 		checkArguments("next", arguments, expectedTypes);
 
 		table = (LuaTable) arguments.get(0);
 		// If the second argument is absent, then it is interpreted as nil.
 		index = (arguments.size() == 2) ? arguments.get(1) : null;
 
 		iter = table.getIterator();
 
 		// When called with nil as its second argument, next returns an initial
 		// index and its associated value
 		if (index == null) {
 
 			// With nil in an empty table, next returns nil.
 			if (!iter.hasNext()) {
				return Collections.singletonList(null);
 			}
 			nextEntry = iter.next();
 			return Arrays.asList(nextEntry.getKey(), nextEntry.getValue());
 		}
 
 		// Return next index and next associated value
 		while (iter.hasNext()) {
 
 			nextEntry = iter.next();
 
 			if (nextEntry.getKey().equals(index)) {
 				// When called with the last index, next returns nil.
 				if (!iter.hasNext()) {
					return Collections.singletonList(null);
 				}
 				nextEntry = iter.next();
 				return Arrays.asList(nextEntry.getKey(), nextEntry.getValue());
 			}
 
 		}
 		// If index is not valid throw error
 		throw new LuaRuntimeException("Invalid key for next");
 	}
 }
