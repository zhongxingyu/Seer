 /**
  * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
  *
  * The software in this package is published under the terms of the CPAL v1.0
  * license, a copy of which has been included with this distribution in the
  * LICENSE.txt file.
  */
 
 package org.mule.tooling.jubula;
 
 import java.io.File;
 
 import org.apache.maven.plugin.AbstractMojo;
 import org.apache.maven.plugin.MojoExecutionException;
 import org.mule.tooling.jubula.cliexecutor.JubulaCliExecutor;
 import org.mule.tooling.jubula.cliexecutor.JubulaCliExecutorFactory;
 import org.mule.tooling.jubula.cliexecutor.SyncCallback;
 
 /**
  * Goal that runs Jubula Functional Tests.
  * 
  * @goal test
  * @phase integration-test
  */
 public class JubulaMojo extends AbstractMojo {
 
 	/**
 	 * Where the .js files will be located for running.
 	 * 
 	 * @parameter expression="${project.build.directory}"
 	 * @readonly
 	 * @required
 	 */
 	private File buildDirectory;
 
 	/**
 	 * The id for the aut being run.
 	 * 
 	 * @parameter
 	 * @required
 	 */
 	private String autId;
 
 	/**
 	 * The working directory of the RCP under test.
 	 * 
 	 * @parameter
 	 * @required
 	 */
 	private String rcpWorkingDir;
 
 	/**
 	 * The name of the executable file of the RCP.
 	 * 
 	 * @parameter
 	 * @required
 	 */
 	private String executableFileName;
 
 	/**
 	 * The keyboard layout
 	 * 
 	 * @parameter default-value="EN_US"
 	 * @required
 	 */
 	private String keyboardLayout;
 
 	/**
 	 * The address where the aut agent will be listening (localhost:60000 by
 	 * default)
 	 * 
 	 * @parameter default-value="localhost:60000"
 	 * @required
 	 */
 	private String autAgentAddress;
 
 	/**
 	 * The test project name
 	 * 
 	 * @parameter
 	 * @required
 	 */
 	private String projectName;
 
 	/**
 	 * The test project version (1.0 by default)
 	 * 
 	 * @parameter default-value="1.0"
 	 * @required
 	 */
 	private String projectVersion;
 
 	/**
 	 * The address of the database containing the jubula data (jdbc:..).
 	 * 
 	 * @parameter
 	 * @required
 	 */
 	private String databaseUrl;
 
 	/**
 	 * The username to access the db
 	 * 
 	 * @parameter
 	 * @required
 	 */
 	private String databaseUser;
 
 	/**
 	 * The password to access the db
 	 * 
 	 * @parameter
 	 * @required
 	 */
 	private String databasePassword;
 
 	/**
 	 * The job to run
 	 * 
 	 * @parameter
 	 * @required
 	 */
 	private String testJob;
 
 	@Override
 	public void execute() throws MojoExecutionException {
 		JubulaMavenPluginContext.initializeContext(buildDirectory);
 
 		String jubulaInstallationPath = JubulaMavenPluginContext
 				.pathToJubulaInstallationDirectory();
 		JubulaCliExecutor jubulaCliExecutor = new JubulaCliExecutorFactory()
 				.getNewInstance(jubulaInstallationPath);
 
 		SyncCallback startAutAgentCallback = new SyncCallback();
 		// start the aut agent
 		jubulaCliExecutor.startAutAgent(startAutAgentCallback);
 
 		// now we should wait until the aut agent is live, but there's no
 		// indication of it, so... wait for a while
 		safeSleep(5000);
 
 		String workspacePath = new File(buildDirectory,
 				JubulaMavenPluginContext.RCPWORKSPACE_DIRECTORY_NAME)
 				.getAbsolutePath();
 		SyncCallback startAutCallback = new SyncCallback();
 		String[] hostAndPort = autAgentAddress.split(":");
 		if (hostAndPort.length != 2)
 			throw new MojoExecutionException(
 					"Please provide the AUT Agent address as <host>:<port>");
 
 		String autAgentHost = hostAndPort[0];
 		String autAgentPort = hostAndPort[1];
 
 		try {
 			jubulaCliExecutor.startAut(autId, rcpWorkingDir,
 					executableFileName, workspacePath, keyboardLayout,
 					autAgentHost, autAgentPort, startAutCallback);
 
 			// now we should wait until the aut is live, but there's no
 			// indication of it, so... wait for ANOTHER while
 			// TODO - maybe make this configurable by parameter
 			safeSleep(20000);
 
 			String datadir = ".";
 			String resultsDir = new File(buildDirectory,
 					JubulaMavenPluginContext.RESULTS_DIRECTORY_NAME)
 					.getAbsolutePath();
 			boolean runTests = jubulaCliExecutor.runTests(projectName,
 					projectVersion, workspacePath, databaseUrl, databaseUser,
 					databasePassword, autAgentHost, autAgentPort,
 					keyboardLayout, testJob, datadir, resultsDir);
 
			if (!runTests)
				throw new MojoExecutionException(
						"There were errors running the tests");
 		} finally {
 			jubulaCliExecutor.stopAutAgent();
 		}
 	}
 
 	private void safeSleep(long millis) {
 		try {
 			Thread.sleep(millis);
 		} catch (InterruptedException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 	}
 
 }
