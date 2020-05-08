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
 package br.com.caelum.integracao.server.plugin.copy;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.Arrays;
 import java.util.List;
 
 import org.apache.commons.httpclient.HttpException;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
import br.com.caelum.integracao.command.CommandToExecute;
 import br.com.caelum.integracao.http.Http;
 import br.com.caelum.integracao.http.Method;
 import br.com.caelum.integracao.server.Build;
 import br.com.caelum.integracao.server.Phase;
 import br.com.caelum.integracao.server.log.LogFile;
 import br.com.caelum.integracao.server.plugin.Plugin;
 import br.com.caelum.integracao.server.queue.Job;
 
 /**
  * Copies files from another machine to the server.
  * 
  * @author guilherme silveira
  */
 public class CopyFiles implements Plugin {
 
 	private final Logger logger = LoggerFactory.getLogger(CopyFiles.class);
 	private final String[] dirs;
 	private final Http http;
 
 	public CopyFiles(Http http, String[] dirs) {
 		this.http = http;
 		this.dirs = dirs;
 	}
 
 	public boolean after(Build build, Phase phase) {
 		LogFile logFile = null;
 		try {
 			logFile = new LogFile(build.getFile(phase.getName() + "/copy-files.txt"));
 			String projectName = build.getProject().getName();
 			logger.debug("Copying for project " + projectName + " dirs " + Arrays.toString(dirs));
 			List<Job> jobs = build.getJobsFor(phase);
 			boolean success = true;
 			for (Job job : jobs) {
 				success &= execute(job, projectName, build, logFile, phase);
 			}
 			return success;
 		} catch (IOException e) {
 			logger.error(e.getMessage(), e);
 			return false;
 		} finally {
 			try {
 				if (logFile != null) {
 					logFile.close();
 				}
 			} catch (IOException e) {
 				logger.error(e.getMessage(), e);
 			}
 		}
 	}
 
 	private boolean execute(Job job, String projectName, Build build, LogFile logFile, Phase phase) {
 		if (!job.isFinished()) {
 			return true;
 		}
 		String uri = job.getClient().getBaseUri();
 		logger.debug("Copying from server " + uri);
 		Method post = http.post(uri + "/plugin/CopyFiles/" + projectName);
 		for (int i = 0; i < dirs.length; i++) {
 			post.with("directory[" + i + "]", dirs[i]);
 		}
 		try {
 			post.send();
 			if (post.getResult() == 201) {
 				File tmp = File.createTempFile("integracao-copy-file-server-", ".zip");
 				post.saveContentToDisk(tmp);
 
 				File commandDirectory = build.getFile(phase.getName() + "/" + job.getCommand().getId());
 				commandDirectory.mkdirs();
 				File unzipResult = new File(commandDirectory, "copy-files-unzip.txt");
 				int result = new CommandToExecute("unzip", "-qo", tmp.getAbsolutePath()).at(commandDirectory)
 						.logTo(unzipResult).run();
 				if (result != 0) {
 					logFile.error("Unable to copy from server " + uri + " due to unzip returning " + result);
 					return false;
 				}
 			} else {
 				logFile.error("Unable to copy from server " + uri + " due to " + post.getResult());
 				return false;
 			}
 		} catch (HttpException e) {
 			logFile.error("Unable to copy from server " + uri, e);
 			return false;
 		} catch (IOException e) {
 			logFile.error("Unable to copy from server " + uri, e);
 			return false;
 		}
 		return true;
 	}
 
 	public boolean before(Build build) {
 		return true;
 	}
 
 }
