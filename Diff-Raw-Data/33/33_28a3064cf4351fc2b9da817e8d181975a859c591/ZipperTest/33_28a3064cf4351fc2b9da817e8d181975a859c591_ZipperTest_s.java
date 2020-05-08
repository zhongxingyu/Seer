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
 
 import java.io.File;
 import java.io.IOException;
 import java.io.PrintWriter;
 import java.io.StringWriter;
 import java.util.regex.Pattern;
 
 import org.junit.Assert;
 import org.junit.Before;
 import org.junit.Test;
 
 import br.com.caelum.integracao.AtDirectoryTest;
 
 public class ZipperTest extends AtDirectoryTest{
 	
 	private PrintWriter writer;
 	private StringWriter content;
 	private String expected;
 	private File contentDir;
 	private File zipFile;
 	private File miscDir;
 
 	@Before
 	public void setupDir() throws IOException {
 		super.setupDir();
 		this.content = new StringWriter();
 		this.writer = new PrintWriter(content, true);
 		this.contentDir = new File(baseDir, "content-dir");
 		this.miscDir = new File(contentDir, "misc");
 		miscDir.mkdirs();
 		File content = new File(miscDir, "content.txt");
 		this.expected = "my content within the file";
 		givenA(content, expected);
 		this.zipFile = File.createTempFile("test", ".zip");
 		zipFile.deleteOnExit();
 	}
 	
 	@Test
 	public void zipsASingleFileWhenSayingAFixedFile() throws IOException {
 		
 		int zipped = new Zipper(miscDir).addExactly("content.txt").logTo(writer).zip(zipFile);
 		Assert.assertEquals(1, zipped);
 		
 		File output = new File(baseDir, "output");
 		output.mkdirs();
 		new Unzipper(output).logTo(writer).unzip(zipFile);
 		
 		Assert.assertEquals(expected, contentOf(new File(output, "content.txt")));
 		
 	}
 
 	@Test
 	public void zipsASingleFileWhenSayingAPattern() throws IOException {
 
 		int zipped = new Zipper(miscDir).addPattern(Pattern.compile(".*")).logTo(writer).zip(zipFile);
 		Assert.assertEquals(1, zipped);
 		
 		File output = new File(baseDir, "output");
 		output.mkdirs();
 		new Unzipper(output).logTo(writer).unzip(zipFile);
 		
 		Assert.assertEquals(expected, contentOf(new File(output, "content.txt")));
 		
 	}
 
 	@Test
 	public void zipsASingleDirectoryWhenSayingAPattern() throws IOException {
 		
 		int zipped = new Zipper(contentDir).addPattern(Pattern.compile(".*")).logTo(writer).zip(zipFile);
 		Assert.assertEquals(1, zipped);
 		
 		File output = new File(baseDir, "output");
 		output.mkdirs();
 		new Unzipper(output).logTo(writer).unzip(zipFile);
 		
 		Assert.assertEquals(expected, contentOf(new File(output, "misc/content.txt")));
 		
 	}
 
 	@Test
 	public void zipsASingleDirectoryWhenSayingItsName() throws IOException {
 		
 		int zipped = new Zipper(contentDir).addExactly("misc").logTo(writer).zip(zipFile);
 		Assert.assertEquals(1, zipped);
 		
 		File output = new File(baseDir, "output");
 		output.mkdirs();
 		new Unzipper(output).logTo(writer).unzip(zipFile);
 		
		System.out.println(this.content.toString());
		
 		Assert.assertEquals(expected, contentOf(new File(output, "misc/content.txt")));
 		
 	}
 
 }
