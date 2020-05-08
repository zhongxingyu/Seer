 /**
  * Copyright 2014 Eediom Inc.
  * 
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
 package org.araqne.logdb.query.expr;
 
 import static org.junit.Assert.*;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.araqne.logdb.QueryParseException;
 import org.junit.Test;
 /**
  * 
  * @author kyun
  *
  */
 public class StrJoinTest {
 
 	@Test
 	public void testStrJoin(){
 		StrJoin srtJoin = new StrJoin(null, expr(",", array(expr(1,2,3,4))));
 		assertEquals("1,2,3,4", srtJoin.eval(null));
 	}
 	
 	@Test
 	public void testStrJoinNull(){
 		
 		StrJoin strJoin = new StrJoin(null, expr(",", null));
 		assertEquals(null, strJoin.eval(null));
 		
 		//strjoin(",", null) => null
 	}
 	
 	@Test
 	public void testError90780(){
 		//args size 0
 		try {
 			new StrJoin(null, expr());
 			fail();
 		} catch (QueryParseException e) {
 			if (e.isDebugMode()) {
 				System.out.println(e.getMessage());
 			}
 			assertEquals("99000", e.getType());
 		}
 
 		//args size 1
 		try {
 			new StrJoin(null, expr(","));
 			fail();
 		} catch (QueryParseException e) {
 			if (e.isDebugMode()) {
 				System.out.println(e.getMessage());
 			}
 			assertEquals("99000", e.getType());
 		}
 
 		//args size 3
 		try {
 			new StrJoin(null, expr(null,1,2));
 			fail();
 		} catch (QueryParseException e) {
 			if (e.isDebugMode()) {
 				System.out.println(e.getMessage());
 			}
 			assertEquals("90780", e.getType());
 		}
 	}
 
 	@Test
 	public void testError90781(){
 		try {
 			new StrJoin(null, expr(null, null));
 			fail();
 		} catch (QueryParseException e) {
 			if (e.isDebugMode()) {
 				System.out.println(e.getMessage());
 			}
 			assertEquals("90781", e.getType());
 		}
 	}
 	
 	@Test
 	public void testManual() {
 		assertNull(FunctionUtil.parseExpr("strjoin(\",\", int(\"invalid\"))").eval(null));
 		assertEquals("1,2,3", FunctionUtil.parseExpr("strjoin(\",\", array(1, 2, 3))").eval(null));
 	}
 	
 	private Array array(List<Expression> expr){
 		return new Array(null, expr);
 	}
 
 	private List<Expression> expr(Object...object ){
 		List<Expression> expr = new ArrayList<Expression>();
 
 		for(Object o: object){
 			if(o == null)
				expr.add( new NullConstant(null, null));
 			if(o instanceof Expression)
 				expr.add((Expression)o);
 			else if(o instanceof String)
 				expr.add(new StringConstant((String)o));
 			else if(o instanceof Number)
 				expr.add(new NumberConstant((Number)o));
 			else if(o instanceof Boolean)
 				expr.add(new BooleanConstant((Boolean)o));
 		}
 
 		return expr;
 	}
 
 }
