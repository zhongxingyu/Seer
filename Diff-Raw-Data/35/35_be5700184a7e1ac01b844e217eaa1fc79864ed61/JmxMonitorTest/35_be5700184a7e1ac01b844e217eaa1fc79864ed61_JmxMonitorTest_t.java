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
 
 import java.io.ByteArrayOutputStream;
 import java.io.IOException;
 import java.io.PrintStream;
 import java.io.PrintWriter;
 import java.lang.management.ManagementFactory;
 import java.lang.management.ThreadInfo;
 import java.lang.management.ThreadMXBean;
 import java.net.Socket;
 import java.util.Date;
 
 import static org.hamcrest.Matchers.*;
 import static org.hamcrest.MatcherAssert.assertThat;
 
 /**
  * Created by IntelliJ IDEA. User: ben Date: Dec 22, 2009 Time: 7:48:44 PM
  */
 public class JmxMonitorTest {
 	private ByteArrayOutputStream outputStream;
 	private PrintStream out;
 
 	@BeforeMethod
 	public void setupMonitorConsole() {
 		outputStream = new ByteArrayOutputStream();
 		out = System.out;
 		System.setOut(new PrintStream(outputStream));
 	}
 
 	@Test
 	public void testJmxMonitorMissingArg() {
 
 		JmxMonitor.main(new String[] { "jmxmonitor", "-c" });
 		assertThat(outputStream.toString(), containsString("usage: jmxMonitor\n" +
 				" -c <arg>   Configuration Path"));
 
 	}
 
 	@Test
 	public void testJmxMonitorMissingOption() {
 
 		JmxMonitor.main(new String[] { "jmxmonitor" });
 		assertThat(outputStream.toString(), containsString("usage: jmxMonitor\n" +
 				" -c <arg>   Configuration Path"));
 
 	}
 
 	@Test
 	public void testJmxMonitorSpareOption() {
 
 		JmxMonitor.main(new String[] { "jmxmonitor", "-c", "out.txt", "-d" });
 		assertThat(outputStream.toString(), containsString("usage: jmxMonitor\n" +
 				" -c <arg>   Configuration Path"));
 
 	}
 
 	@Test
 	public void testJmxMonitorValidOption() throws InterruptedException, IOException {
 		Thread jmxMonitor = new Thread(new RunningJmxMonitor(), "JmxMonitor");
 		jmxMonitor.start();
 
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
 		assertThat(threadFound, is(true));
 		String output = outputStream.toString();
 		assertThat(output, not(containsString("usage: jmxMonitor\n" +
 				" -c <arg>   Configuration Path")));
 		assertThat(output, containsString("ConfigurationFile is src/test/resources/noopConfiguration.properties"));
 
 
		Socket socket = new Socket("localhost", 18001);
 		PrintWriter printWriter = new PrintWriter(socket.getOutputStream(), true);
 		printWriter.write("stop");
 		printWriter.flush();
 
 		// The controlling process should stop in at most 5 seconds
 		currentTime = (new Date()).getTime();
 		boolean stopped = false;
 
 		jmxMonitor.join(5000);
 		assertThat(jmxMonitor.isAlive(), is(false));
 
 		// Finally verify the socket thread shut down
 		threadFound = false;
 		ThreadInfo[] threadInfos = thbean.getThreadInfo(thbean.getAllThreadIds());
 		for (ThreadInfo threadInfo : threadInfos) {
 			if (threadInfo.getThreadName().equals(ThreadManager.SHUTDOWN_MONITOR_THREAD)) {
 				threadFound = true;
 				break;
 			}
 		}
 		assertThat(threadFound, is(not(true)));
 
 	}
 
 
 	private class RunningJmxMonitor implements Runnable {
 		@Override
 		public void run() {
 			JmxMonitor.main(new String[] { "jmxmonitor", "-c", "src/test/resources/noopConfiguration.properties" });
 		}
 	}
 
 	@AfterMethod
 	public void tearDownMonitorConsole() {
 		System.setOut(out);
 	}
 }
