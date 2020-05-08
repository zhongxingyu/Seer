 package edu.tum.lua.junit.stdlib;
 
 import static org.junit.Assert.assertEquals;
 
 import java.util.Arrays;
 
 import org.junit.Test;
 
 import edu.tum.lua.stdlib.Next;
 import edu.tum.lua.types.LuaTable;
 
 public class NextTest {
 	/* Test the next(table [,index]) function of the Lua standard lib */
 
 	// TODO write Unit tests
 	// > next() >> exception
 
 	Next next;
 	LuaTable testTable;
 
 	@Test
 	public void testApplyListOfObject() {
 
 		next = new Next();
 
 		// > next({}) >> nil
		assertEquals(null, next.apply(new LuaTable()));
 
 		// > next({[1] = 4}) >> 1, 4
 		testTable = new LuaTable();
 		testTable.set(1.0, 4.0);
 		assertEquals(Arrays.asList(1.0, 4.0), next.apply(testTable));
 
 		// > next({[1] = 4}, nil) >> 1,4
 		assertEquals(Arrays.asList(1.0, 4.0), next.apply(testTable, null));
 
 		// > next({[1] = 4}, 1) >> nil
		assertEquals(null, next.apply(testTable, 1.0));
 
 		// > next({[1] = 4, [2] = 5}, nil) >> 1,4 or 2,5
 		// TODO
 
 		// > next({[1] = 4, [2] = 5}, 1) >> 2,5 or nil
 		testTable.set(2.0, 5.0);
 		// TODO
 	}
 }
