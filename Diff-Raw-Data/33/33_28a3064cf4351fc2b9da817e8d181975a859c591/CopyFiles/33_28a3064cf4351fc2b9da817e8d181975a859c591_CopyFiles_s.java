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
 package br.com.caelum.integracao.client;
 
 import java.io.File;
 import java.io.IOException;
 import java.io.PrintWriter;
 import java.io.StringWriter;
 import java.util.List;
 
 import br.com.caelum.integracao.client.project.Project;
 import br.com.caelum.integracao.zip.Zipper;
 
 public class CopyFiles {
 
 	private final List<String> directoryToCopy;
 	private final Settings settings;
 	private final Project project;
 	private final PrintWriter output;
 
 	public CopyFiles(List<String> directoryToCopy, Settings settings, Project project, StringWriter zipOutput) {
 		this.directoryToCopy = directoryToCopy;
 		this.settings = settings;
 		this.project = project;
 		this.output = new PrintWriter(zipOutput, true);
 	}
 
 	public File zipThemAll() throws IOException {
 
 		File result = File.createTempFile("integra-copy-files-", ".zip");
 		result.delete();
 
 		if (directoryToCopy != null) {
 
 			File baseDirectory = new File(settings.getBaseDir(), project.getName());
 
 			Zipper zipper = new Zipper(baseDirectory).logTo(output);
 			for (String resourceToCopy : directoryToCopy) {
 				if (!resourceToCopy.trim().equals("")) {
 					output.println("Zipping files " + resourceToCopy);
					zipper.add(resourceToCopy);
 				}
 			}
			if (!zipper.zip(result)) {
 				output.println("Did not zip any files using patterns " + directoryToCopy + " based at " + baseDirectory.getAbsolutePath());
 			}
 			return result;
 		}
 
 		return result;
 
 	}
 
 }
