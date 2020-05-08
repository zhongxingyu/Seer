 /***
  * 
  * Copyright (c) 2009 Caelum - www.caelum.com.br/opensource All rights reserved.
  * 
  * Redistribution and use in source and binary forms, with or without
  * modification, are permitted provided that the following conditions are met:
  * 1. Redistributions of source code must retain the above copyright notice,
  * this list of conditions and the following disclaimer. 2. Redistributions in
  * binary form must reproduce the above copyright notice, this list of
  * conditions and the following disclaimer in the documentation and/or other
  * materials provided with the distribution. 3. Neither the name of the
  * copyright holders nor the names of its contributors may be used to endorse or
  * promote products derived from this software without specific prior written
  * permission.
  * 
  * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
  * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
  * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
  * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
  * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
  * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
  * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
  * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
  * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
  * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
  * POSSIBILITY OF SUCH DAMAGE.
  */
 package br.com.caelum.integracao.client.project;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.util.Arrays;
 import java.util.List;
 import java.util.Scanner;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
import br.com.caelum.integracao.command.CommandToExecute;
 import br.com.caelum.integracao.server.scm.ScmControl;
 import br.com.caelum.integracao.server.scm.ScmException;
 
 public class Project {
 
 	private final Logger logger = LoggerFactory.getLogger(Project.class);
 	private String name;
 
 	private String uri;
 	private Class<?> scmType;
 	private CommandToExecute executing;
 
 	public void setName(String name) {
 		this.name = name;
 	}
 
 	public String getName() {
 		return name;
 	}
 
 	public void setUri(String uri) {
 		this.uri = uri;
 	}
 
 	public String getUri() {
 		return uri;
 	}
 
 	public void setScmType(String scmControl) throws ClassNotFoundException {
 		this.scmType = Class.forName(scmControl);
 	}
 
 	public ScmControl getControl(File baseDirectory) throws ScmException {
 		logger.debug("Creating scm control " + scmType + " for project " + getName());
 		try {
 			return (ScmControl) scmType.getDeclaredConstructor(String.class, File.class, String.class).newInstance(uri,
 					baseDirectory, name);
 		} catch (Exception ex) {
 			throw new ScmException("Unable to checkout project", ex);
 		}
 	}
 
 	public ProjectRunResult run(File baseDirectory, List<String> command, File output) throws IOException {
 		logger.debug("Executing " + command + " to " + baseDirectory + "/" + name);
 
 		File workDir = new File(baseDirectory, name);
 		String[] commands = command.toArray(new String[command.size()]);
 		logger.debug("Ready to execute " + Arrays.toString(commands) + " @ " + workDir.getAbsolutePath()
 				+ " using log=" + output.getAbsolutePath());
 		this.executing = new CommandToExecute(commands).at(workDir).logTo(output);
 		int result = this.executing.run();
 		return new ProjectRunResult(content(output), result);
 
 	}
 
 	public ProjectRunResult checkout(File baseDirectory, String desiredRevision, File output) throws IOException,
 			ScmException {
 		logger.debug("Checking out project @ " + uri + ", revision=" + desiredRevision + " to " + baseDirectory + "/"
 				+ name);
 		File checkoutLog = File.createTempFile("checkout-client-", ".txt");
 		ScmControl control = getControl(baseDirectory);
 		int result = control.checkoutOrUpdate(desiredRevision, checkoutLog);
 		return new ProjectRunResult(content(checkoutLog), result);
 
 	}
 
 	private String content(File tmp) throws FileNotFoundException {
 		Scanner sc = new Scanner(new FileInputStream(tmp)).useDelimiter("117473826478234211");
 		String content;
 		if (!sc.hasNext()) {
 			content = "";
 		} else {
 			content = sc.next();
 		}
 		return content;
 	}
 
 	public void stop() {
 		if (this.executing != null) {
 			executing.stop();
 		}
 	}
 	public String getExecutingCommand() {
 		if(executing!=null) {
 			return executing.getName();
 		}
 		return "";
 	}
 
 }
