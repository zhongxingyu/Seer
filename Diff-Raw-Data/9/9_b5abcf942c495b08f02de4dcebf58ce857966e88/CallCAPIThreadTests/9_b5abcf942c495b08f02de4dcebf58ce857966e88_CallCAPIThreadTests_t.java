 /*
  * $Id$
  *
  * Copyright 2003-2006 Wanadoo Nederland B.V.
  * See the COPYRIGHT file for redistribution and use restrictions.
  */
 package org.xins.tests.client.async;
 
 
 import junit.framework.Test;
 import junit.framework.TestCase;
 import junit.framework.TestSuite;
 
 import com.mycompany.allinone.capi.*;
 import org.xins.client.InvalidRequestException;
 import org.xins.client.UnacceptableRequestException;
 import org.xins.client.async.CallCAPIThread;
 import org.xins.common.service.TargetDescriptor;
 
 /**
  * Tests the <code>CallCAPIThread</code>.
  *
  * @version $Revision$ $Date$
  * @author Anthony Goubard (<a href="mailto:anthony.goubard@nl.wanadoo.com">anthony.goubard@nl.wanadoo.com</a>)
  */
 public class CallCAPIThreadTests extends TestCase {
 
    //-------------------------------------------------------------------------
    // Class functions
    //-------------------------------------------------------------------------
 
    /**
     * Returns a test suite with all test cases defined by this class.
     *
     * @return
     *    the test suite, never <code>null</code>.
     */
    public static Test suite() {
       return new TestSuite(CallCAPIThreadTests.class);
    }
 
 
    //-------------------------------------------------------------------------
    // Constructor
    //-------------------------------------------------------------------------
 
    /**
     * Constructs a new <code>CallCAPIThreadTests</code> test suite with
     * the specified name. The name will be passed to the superconstructor.
     * 
     * 
     * @param name
     *    the name for this test suite.
     */
    public CallCAPIThreadTests(String name) {
       super(name);
    }
 
 
    //-------------------------------------------------------------------------
    // Methods
    //-------------------------------------------------------------------------
 
    public void testSuccessfulCallCAPIThread() throws Throwable {
 
       TargetDescriptor target = new TargetDescriptor("http://127.0.0.1:8080/", 5000, 1000, 4000);
       CAPI capi = new CAPI(target);
       RuntimePropsRequest request = new RuntimePropsRequest();
       request.setPrice(100);
       CallCAPIThread capiThread = new CallCAPIThread(capi, request);
       capiThread.start();
       capiThread.join();
       RuntimePropsResult result = (RuntimePropsResult) capiThread.getResult();
       if (capiThread.getException() != null) {
          capiThread.getException().printStackTrace();
          throw capiThread.getException();
       }
       assertNotNull(result);
       assertEquals(20.6f, result.getTaxes(), 0.01f);
       assertEquals("Euros", result.getCurrency());
      assertTrue(capiThread.getDuration() >= 0L);
    }
 
    public void testUnsuccessfulCallCAPIThread() throws Throwable {
 
       TargetDescriptor target = new TargetDescriptor("http://127.0.0.1:8080/", 5000, 1000, 4000);
       CAPI capi = new CAPI(target);
       RuntimePropsRequest request = new RuntimePropsRequest();
       CallCAPIThread capiThread = new CallCAPIThread(capi, request);
       capiThread.start();
       capiThread.join();
       RuntimePropsResult result = (RuntimePropsResult) capiThread.getResult();
       assertNull(result);
       assertNotNull(capiThread.getException());
       //capiThread.getException().printStackTrace();
       assertTrue(capiThread.getException() instanceof UnacceptableRequestException);
    }
 }
