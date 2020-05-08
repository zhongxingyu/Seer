 /**
  * SAHARA Rig Client
  * 
  * Software abstraction of physical rig to provide rig session control
  * and rig device control. Automatically tests rig hardware and reports
  * the rig status to ensure rig goodness.
  *
  * @license See LICENSE in the top level directory for complete license terms.
  *
  * Copyright (c) 2009, University of Technology, Sydney
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
  * @author Michael Diponio (mdiponio)
  * @date <day> <month> 2009
  *
  * Changelog:
  * - 01/12/2009 - mdiponio - Initial file creation.
  */
 package au.edu.uts.eng.remotelabs.rigclient.rig.tests;
 
 
 import static org.easymock.EasyMock.createMock;
 import static org.easymock.EasyMock.expect;
 import static org.easymock.EasyMock.replay;
 import static org.easymock.EasyMock.reset;
 
 import java.lang.reflect.Field;
 import java.util.List;
 
 import junit.framework.TestCase;
 
 import org.junit.Before;
 import org.junit.Test;
 
 import au.edu.uts.eng.remotelabs.rigclient.rig.AbstractRig;
 import au.edu.uts.eng.remotelabs.rigclient.rig.ConfiguredRig;
 import au.edu.uts.eng.remotelabs.rigclient.rig.IAccessAction;
 import au.edu.uts.eng.remotelabs.rigclient.rig.IActivityDetectorAction;
 import au.edu.uts.eng.remotelabs.rigclient.rig.INotifyAction;
 import au.edu.uts.eng.remotelabs.rigclient.rig.IResetAction;
 import au.edu.uts.eng.remotelabs.rigclient.rig.ISlaveAccessAction;
 import au.edu.uts.eng.remotelabs.rigclient.rig.ITestAction;
 import au.edu.uts.eng.remotelabs.rigclient.rig.internal.tests.MockAccessActionOne;
 import au.edu.uts.eng.remotelabs.rigclient.rig.internal.tests.MockAccessActionTwo;
 import au.edu.uts.eng.remotelabs.rigclient.util.ConfigFactory;
 import au.edu.uts.eng.remotelabs.rigclient.util.IConfig;
 
 /**
  * Tests the ConfiguredRig class. 
  */
 public class ConfiguredRigTester extends TestCase
 {
     /** Object of class under test. */
     private ConfiguredRig rig;
     
     /** Mock configuration. */
     private IConfig mockConfig;
 
     /**
      * @throws java.lang.Exception
      */
     @Override
     @Before
     public void setUp() throws Exception
     {
         /* Replace the default configuration class. */
         this.mockConfig = createMock(IConfig.class);
         
         ConfigFactory.getInstance();
         Field field = ConfigFactory.class.getDeclaredField("instance");        
         field.setAccessible(true);
         field.set(null, this.mockConfig);
     }
 
     @SuppressWarnings("unchecked")
     @Test
     public void testInit() throws Exception
     {
         reset(this.mockConfig);
         expect(this.mockConfig.getProperty("Logger_Type"))
             .andReturn("SystemErr");
         expect(this.mockConfig.getProperty("Log_Level"))
             .andReturn("DEBUG");
         expect(this.mockConfig.getProperty("Default_Log_Format", "[__LEVEL__] - [__ISO8601__] - __MESSAGE__"))
             .andReturn("[__LEVEL__] - [__ISO8601__] - __MESSAGE__");
         expect(this.mockConfig.getProperty("FATAL_Log_Format")).andReturn(null);
         expect(this.mockConfig.getProperty("PRIORITY_Log_Format")).andReturn(null);
         expect(this.mockConfig.getProperty("ERROR_Log_Format")).andReturn(null);
         expect(this.mockConfig.getProperty("WARN_Log_Format")).andReturn(null);
         expect(this.mockConfig.getProperty("INFO_Log_Format")).andReturn(null);
         expect(this.mockConfig.getProperty("DEBUG_Log_Format")).andReturn(null);
         expect(this.mockConfig.getProperty("Action_Package_Prefixes", ""))
             .andReturn("au.edu.uts.eng.remotelabs;au.edu.uts.eng.remotelabs.rigclient.rig.internal.tests;" +
                     "au.edu.uts.eng.remotelabs.rigclient.rig.internal");
         expect(this.mockConfig.getProperty("Access_Actions"))
             .andReturn("MockAccessActionOne;MockAccessActionTwo");
         expect(this.mockConfig.getProperty("Slave_Access_Actions"))
             .andReturn("");
         expect(this.mockConfig.getProperty("Notify_Actions"))
             .andReturn("");
         expect(this.mockConfig.getProperty("Detection_Actions"))
             .andReturn("");
         expect(this.mockConfig.getProperty("Reset_Actions"))
             .andReturn("");
         expect(this.mockConfig.getProperty("Test_Actions"))
             .andReturn("");
         expect(this.mockConfig.getProperty("Action_Failure_Threshold"))
             .andReturn("3");
         replay(this.mockConfig);
         
         this.rig = new ConfiguredRig();
         
         Field field = AbstractRig.class.getDeclaredField("accessActions");
         field.setAccessible(true);
         List<IAccessAction> access = (List<IAccessAction>)field.get(this.rig);
         assertEquals(2, access.size());
         assertTrue(access.get(0) instanceof MockAccessActionOne);
         assertTrue(access.get(1) instanceof MockAccessActionTwo);
         
         field = AbstractRig.class.getDeclaredField("slaveActions");
         field.setAccessible(true);
         List<ISlaveAccessAction> slave = (List<ISlaveAccessAction>)field.get(this.rig);
         assertEquals(0, slave.size());
         
         field = AbstractRig.class.getDeclaredField("notifyActions");
         field.setAccessible(true);
         List<INotifyAction> notify = (List<INotifyAction>)field.get(this.rig);
         assertEquals(0, notify.size());
         
         field = AbstractRig.class.getDeclaredField("detectionActions");
         field.setAccessible(true);
         List<IActivityDetectorAction> detect = (List<IActivityDetectorAction>)field.get(this.rig);
         assertEquals(0, detect.size());
         
         field = AbstractRig.class.getDeclaredField("resetActions");
         field.setAccessible(true);
         List<IResetAction> test = (List<IResetAction>)field.get(this.rig);
         assertEquals(0, test.size());
         
         field = AbstractRig.class.getDeclaredField("testActions");
         field.setAccessible(true);
         List<ITestAction> reset = (List<ITestAction>)field.get(this.rig);
         assertEquals(0, reset.size());
         
     }
 
 }
