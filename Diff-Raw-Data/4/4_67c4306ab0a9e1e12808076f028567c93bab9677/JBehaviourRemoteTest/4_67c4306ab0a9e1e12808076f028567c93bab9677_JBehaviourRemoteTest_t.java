 /**
  *  Copyright 2012 Yannick Roffin
  *
  *   Licensed under the Apache License, Version 2.0 (the "License");
  *   you may not use this file except in compliance with the License.
  *   You may obtain a copy of the License at
  *
  *       http://www.apache.org/licenses/LICENSE-2.0
  *
  *  Unless required by applicable law or agreed to in writing, software
  *  distributed under the License is distributed on an "AS IS" BASIS,
  *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  *  See the License for the specific language governing permissions and
  *   limitations under the License.
  */
 
 package org.jbehaviour.plugins.remote;
 
 import static org.junit.Assert.*;
 
 import java.io.File;
 
 import org.jbehaviour.exception.JBehaviourParsingError;
 import org.jbehaviour.exception.JBehaviourRuntimeError;
 import org.jbehaviour.impl.JBehaviourLauncher;
 import org.junit.After;
 import org.junit.Before;
 import org.junit.Test;
 
 public class JBehaviourRemoteTest {
 	private MockFtpServer ftpMock;
 	private MockSftpServer sshMock;
 
 	@Before
 	public void setup() throws Exception{
 		/**
 		 * FTP mock
 		 */
 		ftpMock = new MockFtpServer(21,"TestUsername", "TestPassword", new File("src/test/resources"));
 		ftpMock.addDir(new File(new File("src/test/resources/root").getAbsolutePath()));
 		ftpMock.start();
 
 		/**
 		 * SSH/SFTP/SCP Mock
 		 */
 		sshMock = new MockSftpServer();
 		try {
 			sshMock.start();
 	    } catch (Exception e) {
 	        e.printStackTrace();
 	    }
 	}
 
 	@After
 	public void tearDown() throws InterruptedException{
		if(ftpMock != null) ftpMock.stop();
		if(sshMock != null) sshMock.stop();
 	}
 
 	@Test
 	public void testRemoteSample() throws JBehaviourParsingError, JBehaviourRuntimeError {
 		assertEquals(true,(new JBehaviourLauncher()).registerAndExecute("src/test/resources/files/remote.story"));
 	}
 }
