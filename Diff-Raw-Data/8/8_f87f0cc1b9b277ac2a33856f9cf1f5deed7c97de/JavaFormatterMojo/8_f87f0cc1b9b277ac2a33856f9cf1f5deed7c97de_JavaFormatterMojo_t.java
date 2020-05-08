 package maven.plugin.javafmt;
 
 /*
  * #%L
  * Java Formatter Maven Plugin
  * %%
  * Copyright (C) 2013 FocusSNS
  * %%
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  * 
  *      http://www.apache.org/licenses/LICENSE-2.0
  * 
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  * #L%
  */
 
 import java.io.File;
 import java.io.IOException;
 import java.net.URI;
 import java.net.URL;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.Map;
 
 import org.apache.commons.io.FileUtils;
 import org.apache.maven.plugin.AbstractMojo;
 import org.apache.maven.plugin.MojoExecutionException;
 import org.apache.maven.plugin.MojoFailureException;
 import org.apache.maven.plugins.annotations.LifecyclePhase;
 import org.apache.maven.plugins.annotations.Mojo;
 import org.apache.maven.plugins.annotations.Parameter;
 import org.eclipse.jdt.core.JavaCore;
 import org.eclipse.jdt.core.ToolFactory;
 import org.eclipse.jdt.core.formatter.CodeFormatter;
 import org.eclipse.jface.text.BadLocationException;
 import org.eclipse.jface.text.Document;
 import org.eclipse.jface.text.IDocument;
 import org.eclipse.jface.text.IRegion;
 import org.eclipse.text.edits.TextEdit;
 
 @Mojo(name = "format", defaultPhase = LifecyclePhase.PROCESS_SOURCES)
 public class JavaFormatterMojo extends AbstractMojo {
     private static final String LINE_ENDING_CRLF_CHARS = "\r\n";
     @Parameter(property = "basedir")
     private String basedir;
     @Parameter(property = "fileEncoding", defaultValue = "${project.build.sourceEncoding}")
     private String fileEncoding;
     @Parameter(property = "codeStyleFile")
     private String codeStyleFile;
     @Parameter(property = "compilerSource", required = true, defaultValue = "${maven.compiler.source}")
     private String compilerSource;
     @Parameter(property = "compilerCompliance", required = true, defaultValue = "${maven.compiler.source}")
     private String compilerCompliance;
     @Parameter(property = "compilerTargetPlatform", required = true, defaultValue = "${maven.compiler.target}")
     private String compilerTargetPlatform;
 
     public void setBasedir(String basedir) {
         this.basedir = basedir;
     }
 
     public void setFileEncoding(String fileEncoding) {
         this.fileEncoding = fileEncoding;
     }
 
     @Override
     public void execute() throws MojoExecutionException, MojoFailureException {
         try {
             CodeFormatter codeFormatter = getCodeFormatter();
             //
             File srcDir = new File(basedir, "src");
             Iterator<File> iter = FileUtils.listFiles(srcDir, new String[] { "java" }, true).iterator();
             while (iter.hasNext()) {
                 File javaFile = iter.next();
                 getLog().info("Format Java File : " + javaFile.getAbsolutePath());
                 //
                 formatJavaFile(codeFormatter, javaFile);
             }
         } catch (Exception e) {
             throw new MojoExecutionException("Java Code Format Exception!", e);
         }
     }
 
     protected void formatJavaFile(CodeFormatter codeFormatter, File javaFile) throws IOException, BadLocationException, IOException {
         //
         String javaCode = FileUtils.readFileToString(javaFile, fileEncoding);
         IDocument javaDoc = new Document(javaCode);
         //
         javaDoc = organizeImports(javaDoc);
         //
         javaDoc = formatCode(codeFormatter, javaDoc);
         //
         FileUtils.write(javaFile, javaDoc.get(), fileEncoding);
     }
 
     protected IDocument organizeImports(IDocument javaDoc) throws BadLocationException {
         //
         int firstImportCount = 0;
         int lastImportCount = 0;
         int linesNumber = javaDoc.getNumberOfLines();
         JavaImports javaImports = new JavaImports();
         //
         for (int lineNumber = 0; lineNumber < linesNumber; lineNumber++) {
             IRegion region = javaDoc.getLineInformation(lineNumber);
             String line = javaDoc.get(region.getOffset(), region.getLength());
             //
             if (line.startsWith("import")) {
                 //
                 javaImports.add(line);
                 //
                 if (firstImportCount == 0) {
                     firstImportCount = lineNumber;
                 }
                 //
                 lastImportCount = lineNumber;
             }
         }
         //
         StringBuilder javaCodeBuilder = new StringBuilder();
         for (int lineNumber = 0; lineNumber < linesNumber; lineNumber++) {
             //
             IRegion region = javaDoc.getLineInformation(lineNumber);
             String line = javaDoc.get(region.getOffset(), region.getLength());
             //
            if (firstImportCount < lastImportCount &&
                    lineNumber >= firstImportCount &&
                    lineNumber <= lastImportCount) {
                 continue;
             }
             //
            if (firstImportCount < lastImportCount &&
                    lineNumber == lastImportCount + 1) {
                 javaCodeBuilder.append(javaImports);
             }
             javaCodeBuilder.append(line).append(LINE_ENDING_CRLF_CHARS);
         }
         //
         javaDoc.set(javaCodeBuilder.toString());
         //
         return javaDoc;
     }
 
     protected IDocument formatCode(CodeFormatter codeFormatter, IDocument javaDoc) throws BadLocationException {
         //
         String javaCode = javaDoc.get();
         int kind = CodeFormatter.K_COMPILATION_UNIT + CodeFormatter.F_INCLUDE_COMMENTS;
         //
         TextEdit textEdit = codeFormatter.format(kind, javaCode, 0, javaCode.length(), 0, LINE_ENDING_CRLF_CHARS);
         //
         textEdit.apply(javaDoc);
         //
         return javaDoc;
     }
 
     protected CodeFormatter getCodeFormatter() throws Exception {
         //
         Map<String, String> defaultOptions = new HashMap<String, String>();
         defaultOptions.put(JavaCore.COMPILER_SOURCE, compilerSource);
         defaultOptions.put(JavaCore.COMPILER_COMPLIANCE, compilerCompliance);
         defaultOptions.put(JavaCore.COMPILER_CODEGEN_TARGET_PLATFORM, compilerTargetPlatform);
         //
         URL url = getClass().getClassLoader().getResource("META-INF/java-code-style.xml");
         if (codeStyleFile != null) {
             url = URI.create("file:/" + codeStyleFile).toURL();
         }
         JavaCodeStyle javaCodeStyle = new JavaCodeStyle(defaultOptions, url.openStream());
         //
         return ToolFactory.createCodeFormatter(javaCodeStyle);
     }
 
 }
