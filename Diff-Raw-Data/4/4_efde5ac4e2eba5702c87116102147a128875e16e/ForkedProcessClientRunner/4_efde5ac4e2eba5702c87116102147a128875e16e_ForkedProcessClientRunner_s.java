 /*
  * Copyright (c) 2013 mgm technology partners GmbH
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package com.mgmtp.perfload.core.daemon.util;
 
 import static com.google.common.base.Joiner.on;
 import static com.google.common.collect.Lists.newArrayList;
 import static com.google.common.collect.Lists.newArrayListWithCapacity;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.List;
 import java.util.concurrent.Callable;
 import java.util.concurrent.ExecutorService;
 import java.util.concurrent.Future;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import com.mgmtp.perfload.core.common.config.ProcessConfig;
 import com.mgmtp.perfload.core.common.util.LoggingGobbleCallback;
 import com.mgmtp.perfload.core.common.util.LoggingGobbleCallback.Level;
 import com.mgmtp.perfload.core.common.util.StreamGobbler;
 
 /**
  * {@link AbstractClientRunner} implementation that forks new client processes.
  * 
  * @author rnaegele
  */
 public class ForkedProcessClientRunner extends AbstractClientRunner {
 
 	private final Logger log = LoggerFactory.getLogger(getClass());
 	private final StreamGobbler gobbler;
 
 	/**
 	 * @param execService
 	 *            the {@link ExecutorService} for running client processes
 	 * @param gobbler
 	 *            captures stdout and stderr of the process
 	 */
 	public ForkedProcessClientRunner(final ExecutorService execService, final StreamGobbler gobbler) {
 		super(execService);
 		this.gobbler = gobbler;
 	}
 
 	@Override
 	public Future<Integer> runClient(final File clientDir, final ProcessConfig procConfig, final List<String> arguments) {
 		return execService.submit(new Callable<Integer>() {
 			@Override
 			public Integer call() throws IOException, InterruptedException {
 				File javaHome = new File(System.getenv("JAVA_HOME"));
 				if (!javaHome.exists()) {
 					throw new IOException("JAVA_HOME does not exist.");
 				}
 				File javaExe = new File(javaHome, new File("bin", "java").getPath());
 
 				List<String> commands = newArrayList();
 				commands.add(javaExe.getPath());
 				maybeAddMaxHeap(commands, procConfig.getJvmArgs());
				commands.add("-Dlogback.configurationFile=logback.xml");
 				commands.add("-DdaemonId=" + procConfig.getDaemonId());
 				commands.add("-DprocessId=" + procConfig.getProcessId());
 				commands.addAll(procConfig.getJvmArgs());
 				commands.addAll(buildClasspath());
 				commands.addAll(arguments);
 
 				log.info("Running test process: {}", on(' ').join(commands));
 				Process process = new ProcessBuilder(commands).directory(clientDir).start();
 
 				// Grab output of the process
 				String prefix = "[daemon" + procConfig.getDaemonId() + "|process" + procConfig.getProcessId() + "] ";
 				gobbler.addStream(process.getInputStream(), "UTF-8", new LoggingGobbleCallback(Level.INFO, prefix));
 				gobbler.addStream(process.getErrorStream(), "UTF-8", new LoggingGobbleCallback(Level.ERROR, prefix));
 
 				return process.waitFor();
 			}
 
 		});
 	}
 
 	private void maybeAddMaxHeap(final List<String> commands, final List<String> jvmArgs) {
 		for (String arg : jvmArgs) {
 			if (arg.startsWith("-Xmx")) {
 				return;
 			}
 		}
 		commands.add("-Xmx256m");
 	}
 
 	private List<String> buildClasspath() {
 		String version = getClass().getPackage().getImplementationVersion();
 		List<String> commands = newArrayListWithCapacity(3);
 		if (version == null) {
 			// No manifest i. e. we are running in an IDE
 			commands.add("-classpath");
 			String cp = System.getProperty("java.class.path");
 			commands.add(cp);
 			commands.add("com.mgmtp.perfload.core.client.LtProcess");
 		} else {
 			String jar = String.format("perfload-client-%s.jar", version);
 			commands.add("-jar");
 			commands.add(new File("lib", jar).getPath());
 		}
 		return commands;
 	}
 }
