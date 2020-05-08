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
 package br.com.caelum.integracao.zip;
 
 import java.io.BufferedInputStream;
 import java.io.BufferedOutputStream;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.PrintWriter;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.regex.Pattern;
 import java.util.zip.ZipEntry;
 import java.util.zip.ZipOutputStream;
 
 public class Zipper {
 
 	private static final int BUFFER = 4096;
 
 	private final File workDirectory;
 
 	private final List<Pattern> content = new ArrayList<Pattern>();
 	private final List<Pattern> ignore = new ArrayList<Pattern>();
 	private final List<String> fixedContent = new ArrayList<String>();
 
 	private PrintWriter log;
 	
 	public Zipper(File workDirectory) {
 		this.workDirectory = workDirectory;
 	}
 
 	public Zipper addExactly(String content) {
 		this.fixedContent.add(content);
 		return this;
 	}
 
 	public Zipper addPattern(Pattern content) {
 		this.content.add(content);
 		return this;
 	}
 
 	public int zip(File zipFile) throws IOException {
 		FileOutputStream target = new FileOutputStream(zipFile);
 		ZipOutputStream output = new ZipOutputStream(new BufferedOutputStream(target));
 		byte data[] = new byte[BUFFER];
 		int count = zip(data, output, workDirectory, "", false);
 		if(count!=0) {
 			output.close();
 		}
 		return count;
 	}
 
 	private int zip(byte[] data, ZipOutputStream output, File base, String currentPath, boolean matched) throws IOException {
 		int total = 0;
 		for (File file : base.listFiles()) {
 			String completeName = (currentPath.equals("")? "" : currentPath + "/") + file.getName();
 			if(file.isDirectory()) {
 				total += zip(data, output, file, completeName, matched || !shouldIgnore(completeName));
 				continue;
 			}
 			if(!matched && shouldIgnore(completeName)) {
 				continue;
 			}
 			total++;
 			FileInputStream input = new FileInputStream(file);
 			BufferedInputStream origin = new BufferedInputStream(input, BUFFER);
 			ZipEntry entry = new ZipEntry(completeName);
 			output.putNextEntry(entry);
 			int count;
 			while ((count = origin.read(data, 0, BUFFER)) != -1) {
 				output.write(data, 0, count);
 			}
 			origin.close();
 		}
 		return total;
 	}
 
 	private boolean shouldIgnore(String filePath) {
 		for(Pattern exclude :ignore) {
 			if(exclude.matcher(filePath).matches()) {
 				return true;
 			}
 		}
 		for(Pattern include : content) {
 			if(include.matcher(filePath).matches()) {
 				log.println("[a] " + filePath);
 				return false;
 			}
 		}
 		for(String include : fixedContent) {
 			if(include.equals(filePath)) {
 				log.println("[a] " + filePath);
 				return false;
 			}
 		}
 		return true;
 	}
 
 	public Zipper ignore(String pattern) {
 		this.ignore.add(Pattern.compile(pattern));
 		return this;
 	}
 	
 	public Zipper logTo(PrintWriter log) {
 		this.log = log;
 		return this;
 	}
 
 }
