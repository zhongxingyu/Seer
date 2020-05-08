 /**
  * SAHARA Rig Client
  * 
  * Software abstraction of physical rig to provide rig session control
  * and rig device control. Automatically tests rig hardware and reports
  * the rig status to ensure rig goodness.
  *
  * @license See LICENSE in the top level directory for complete license terms.
  *
  * Copyright (c) 2010, University of Technology, Sydney
  * All rights reserved.
  *
  * Redistribution and use in source and binary forms, with or without 
  * modification, are permitted provided that the following conditions are met:
  *
  *  * Redistributions of source code must retain the above copyright notice, 
  *    this list of conditions and the following disclaimer.
  *  * Redistributions in binary form must reproduce the above copyright 
  *    notice, this list of conditions and the following disclaimer in the 
  *    documentation and/or other materials provided with the distribution.
  *  * Neither the name of the University of Technology, Sydney nor the names 
  *    of its contributors may be used to endorse or promote products derived from 
  *    this software without specific prior written permission.
  * 
  * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" 
  * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE 
  * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE 
  * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE 
  * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL 
  * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR 
  * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER 
  * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, 
  * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE 
  * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
  *
  * @author <First> <Last> (tmachet)
  * @date <Day> <Month> 2010
  *
  * Changelog:
  * - 01/03/2010 - tmachet - Initial file creation.
  */
 package au.edu.uts.eng.remotelabs.rigclient.action.detect.tests;
 
 import java.lang.reflect.Field;
 
 import junit.framework.TestCase;
 
 import org.easymock.EasyMock;
 import org.junit.After;
 import org.junit.Before;
 import org.junit.Test;
 
 import au.edu.uts.eng.remotelabs.rigclient.action.detect.RDPActivityDetectorAction;
 import au.edu.uts.eng.remotelabs.rigclient.util.ConfigFactory;
 import au.edu.uts.eng.remotelabs.rigclient.util.IConfig;
 import au.edu.uts.eng.remotelabs.rigclient.util.LoggerFactory;
 
 /**
  * @author tmachet
  *
  */
 public class RDPActivityDetectorActionTester extends TestCase
 {
 
     /** Object of class under test. */
     private RDPActivityDetectorAction RDPDetect;
 
     /** Mock configuration class. */
     private IConfig mockConfig;
     
 /**
      * @throws java.lang.Exception
      */
     @Override
     @Before
     public void setUp() throws Exception
     {
         this.mockConfig = EasyMock.createMock(IConfig.class);
         EasyMock.expect(this.mockConfig.getProperty("Logger_Type")).andReturn("SystemErr");
         EasyMock.expect(this.mockConfig.getProperty("Log_Level")).andReturn("DEBUG");
         EasyMock.expect(this.mockConfig.getProperty("Default_Log_Format", "[__LEVEL__] - [__ISO8601__] - __MESSAGE__"))
                 .andReturn("[__LEVEL__] - [__ISO8601__] - __MESSAGE__");
         EasyMock.expect(this.mockConfig.getProperty("FATAL_Log_Format")).andReturn(null);
         EasyMock.expect(this.mockConfig.getProperty("PRIORITY_Log_Format")).andReturn(null);
         EasyMock.expect(this.mockConfig.getProperty("ERROR_Log_Format")).andReturn(null);
         EasyMock.expect(this.mockConfig.getProperty("WARN_Log_Format")).andReturn(null);
         EasyMock.expect(this.mockConfig.getProperty("INFO_Log_Format")).andReturn(null);
         EasyMock.expect(this.mockConfig.getProperty("DEBUG_Log_Format")).andReturn(null);
         EasyMock.replay(this.mockConfig);
 
         final Field configField = ConfigFactory.class.getDeclaredField("instance");
         configField.setAccessible(true);
         configField.set(null, this.mockConfig);
 
         LoggerFactory.getLoggerInstance();
         final String os = System.getProperty("os.name");
         if (os.startsWith("Windows"))
         {
             this.RDPDetect = new RDPActivityDetectorAction();
         }
         else
         {
             this.RDPDetect = null;
         }
 }
 
     /**
      * @throws java.lang.Exception
      */
     @Override
     @After
     public void tearDown() throws Exception
     {
         super.tearDown();
     }
 
     /**
      * Test method for {@link au.edu.uts.eng.remotelabs.rigclient.action.detect.RDPActivityDetectorAction#detectActivity()}.
      */
     @Test
     public void testDetectActivity()
     {
         if (this.RDPDetect != null)
         {
             Boolean result = this.RDPDetect.detectActivity();
             assertTrue(result);
         }
     }
 
     /**
      * Test method for {@link au.edu.uts.eng.remotelabs.rigclient.action.detect.RDPActivityDetectorAction#getActionType()}.
      */
     @Test
     public void testGetActionType()
     {
         if (this.RDPDetect != null)
         {
             String result = this.RDPDetect.getActionType();
             assertEquals(result,"RDP Activity Detector Action");
         }
     }
 }
