 /** ==== BEGIN LICENSE =====
    Copyright 2012 - BeeQueue.org
 
    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at
 
        http://www.apache.org/licenses/LICENSE-2.0
 
    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an &quot;AS IS&quot; BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
  
  *  ===== END LICENSE ====== */
 package org.beequeue.sql;
 
import java.io.PrintWriter;
import java.io.StringWriter;


 import org.beequeue.coordinator.db.DbCoordinator;
 import org.beequeue.util.BeeException;
 import org.beequeue.util.Throwables;
 import org.junit.Assert;
 import org.junit.Test;
 
 public class BeeExceptionTest {
 
 	@Test
 	public void test() {
 		String s = Throwables.toString(new BeeException("abc").memo("dbcoordinator",new DbCoordinator()));
 		boolean starts = s.startsWith(
 		"org.beequeue.util.BeeException: abc\n" +
		"dbcoordinator: {\"type\":\"db\",\"driver\":null,\"url\":null,\"user\":null,\"password\":null,\"initSql\":null}\n" +
 		"	at org.beequeue");
 		System.out.println(s);
 		Assert.assertTrue(starts);
 	}
 
 }
