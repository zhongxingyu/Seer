 /*
  * Copyright 2009 Ben Gidley
  *
  *    Licensed under the Apache License, Version 2.0 (the "License");
  *    you may not use this file except in compliance with the License.
  *    You may obtain a copy of the License at
  *
  *        http://www.apache.org/licenses/LICENSE-2.0
  *
  *    Unless required by applicable law or agreed to in writing, software
  *    distributed under the License is distributed on an "AS IS" BASIS,
  *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  *    See the License for the specific language governing permissions and
  *    limitations under the License.
  */
 
 package uk.co.gidley.jmxmonitor;
 
 import org.testng.annotations.AfterMethod;
 import org.testng.annotations.BeforeMethod;
 import org.testng.annotations.Test;
 import uk.co.gidley.jmxmonitor.services.ThreadManager;
 
 import javax.management.InstanceAlreadyExistsException;
 import javax.management.MBeanRegistrationException;
 import javax.management.MalformedObjectNameException;
 import javax.management.NotCompliantMBeanException;
 import java.io.ByteArrayOutputStream;
 import java.io.IOException;
 import java.io.PrintStream;
 import java.io.PrintWriter;
 import java.lang.management.ManagementFactory;
 import java.lang.management.ThreadInfo;
 import java.lang.management.ThreadMXBean;
 import java.net.Socket;
 import java.util.Date;
 
 import static org.hamcrest.Matchers.containsString;
 import static org.hamcrest.Matchers.is;
 import static org.hamcrest.Matchers.not;
 import static org.hamcrest.MatcherAssert.assertThat;
 
 /**
  * Created by IntelliJ IDEA. User: ben Date: Dec 30, 2009 Time: 8:12:56 AM
  */
 public class JmxMonitorLocalMonitoringTest {
 	private ByteArrayOutputStream outputStream;
 	private PrintStream out;
 
 	@BeforeMethod
 	public void setupMonitorConsole() {
 		outputStream = new ByteArrayOutputStream();
 		out = System.out;
 		System.setOut(new PrintStream(outputStream));
 	}
 
 	@Test
 	public void testLocalMonitoring() throws IOException, InterruptedException, MBeanRegistrationException, InstanceAlreadyExistsException, NotCompliantMBeanException, MalformedObjectNameException {
 		Thread jmxMonitor = new Thread(new RunningJmxMonitor(), "JmxMonitor");
 		jmxMonitor.start();
 
 		// Wait for it to actually start
 		// Jmx Monitor should start and go into a loop doing nothing
 		// Check for startup within 10 seconds
 		long currentTime = (new Date()).getTime();
 		final ThreadMXBean thbean = ManagementFactory.getThreadMXBean();
 		boolean threadFound = false;
 
 		while (currentTime + 10 * 1000 > (new Date()).getTime()) {
 			ThreadInfo[] threadInfos = thbean.getThreadInfo(thbean.getAllThreadIds());
 			for (ThreadInfo threadInfo : threadInfos) {
 				if (threadInfo.getThreadName().equals(ThreadManager.SHUTDOWN_MONITOR_THREAD)) {
 					threadFound = true;
 					break;
 				}
 			}
 			Thread.sleep(500);
 		}
 
 		String output = outputStream.toString();
 		assertThat(output, not(containsString("usage: jmxMonitor\n" +
 				" -c <arg>   Configuration Path")));
 		assertThat(output, containsString(
 				"ConfigurationFile is src/test/resources/jmxLocalMonitoringTestConfiguration.properties"));
 		assertThat(threadFound, is(true));
 
 
 		// Shutdown
 
		Socket socket = new Socket("localhost", 8001);
 		PrintWriter printWriter = new PrintWriter(socket.getOutputStream(), true);
 		printWriter.write("stop");
 		printWriter.flush();
 	}
 
 	private class RunningJmxMonitor implements Runnable {
 		@Override
 		public void run() {
 			// Now start JMX Monitor configured to run against local host
 			JmxMonitor.main(
 					new String[] { "jmxmonitor", "-c", "src/test/resources/jmxLocalMonitoringTestConfiguration.properties" });
 		}
 	}
 
 	@AfterMethod
 	public void tearDownMonitorConsole() {
 		System.setOut(out);
 	}
 }
