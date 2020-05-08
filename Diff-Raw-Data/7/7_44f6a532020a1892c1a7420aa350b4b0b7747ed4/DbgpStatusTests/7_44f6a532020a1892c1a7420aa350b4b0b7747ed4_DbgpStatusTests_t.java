 package org.eclipse.dltk.debug.dbgp.tests;
 
 import junit.framework.TestCase;
 
 import org.eclipse.dltk.dbgp.IDbgpStatus;
 import org.eclipse.dltk.dbgp.internal.DbgpStatus;
 
 public class DbgpStatusTests extends TestCase {
 	protected void setUp() throws Exception {
 		super.setUp();
 	}
 
 	protected void tearDown() throws Exception {
 		super.tearDown();
 	}
 	
 	public void testConstruction() {
		IDbgpStatus s = DbgpStatus.parse("running", "ok");
 		
 		assertTrue(s.isRunning());
 		assertTrue(s.reasonOk());
 	}
 
 	public void testEquals() {
		IDbgpStatus a = DbgpStatus.parse("running", "ok");
		IDbgpStatus b = DbgpStatus.parse("running", "ok");
 
 		assertEquals(a, b);
 	}
 }
