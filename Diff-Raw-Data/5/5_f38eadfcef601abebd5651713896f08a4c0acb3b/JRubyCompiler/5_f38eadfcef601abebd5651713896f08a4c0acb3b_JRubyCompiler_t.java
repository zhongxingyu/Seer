 /*
  * Licensed to the Apache Software Foundation (ASF) under one
  * or more contributor license agreements.  See the NOTICE file
  * distributed with this work for additional information
  * regarding copyright ownership.  The ASF licenses this file
  * to you under the Apache License, Version 2.0 (the
  * "License"); you may not use this file except in compliance
  * with the License.  You may obtain a copy of the License at
  *
  *   http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing,
  * software distributed under the License is distributed on an
  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
  * KIND, either express or implied.  See the License for the
  * specific language governing permissions and limitations
  * under the License.
  */
 
 package org.apache.myfaces.extensions.scripting.core.engine.compiler;
 
 import org.apache.myfaces.extensions.scripting.core.api.Configuration;
 import org.apache.myfaces.extensions.scripting.core.api.WeavingContext;
 import org.apache.myfaces.extensions.scripting.core.common.util.ClassLoaderUtils;
 import org.apache.myfaces.extensions.scripting.core.common.util.FileUtils;
 import org.apache.myfaces.extensions.scripting.core.engine.api.CompilationMessage;
 import org.apache.myfaces.extensions.scripting.core.engine.api.CompilationResult;
 
 import javax.script.ScriptEngine;
 import javax.script.ScriptEngineManager;
 import javax.script.ScriptException;
 import java.io.File;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.logging.Logger;
 
 import static org.apache.myfaces.extensions.scripting.core.api.ScriptingConst.ENGINE_TYPE_JSF_JRUBY;
 import static org.apache.myfaces.extensions.scripting.core.engine.api.CompilerConst.JRUBY_WILDARD;
 
 /**
  * @author Werner Punz (latest modification by $Author$)
  * @version $Revision$ $Date$
  *          <p/>
  *          A compiler for jruby which uses ruby and the standard JSR 223
  *          for compiling classes into java classes
  */
 
 public class JRubyCompiler implements org.apache.myfaces.extensions.scripting.core.engine.api.Compiler
 {
 
     private static final String ENGINE_JRUBY = "jruby";
 
     private static  Logger logger = Logger.getLogger(JRubyCompiler.class.getName());
 
     @Override
     public CompilationResult compile(File sourcePath, File targetPath, ClassLoader classLoader)
     {
         targetPath.mkdirs();
         List<String> sources = getSourceFiles();
         return compile(sourcePath, targetPath, sources);
     }
 
     public CompilationResult compile(File sourcePath, File targetPath, List<String> sources)
     {
         targetPath.mkdirs();

         String classPath = ClassLoaderUtils.buildClasspath(ClassLoaderUtils.getDefaultClassLoader());
         //capturing stdout technique from http://thinkingdigitally.com/archive/capturing-output-from-puts-in-ruby/
         StringBuilder commandString = new StringBuilder();
         commandString.append("require 'jruby/jrubyc'\n");
         commandString.append("require 'stringio' \n");
         commandString.append("module Kernel\n");
         commandString.append("  def capture_stdout\n");
         commandString.append("    out = StringIO.new\n");
         commandString.append("    $stdout = out\n");
         commandString.append("    yield\n");
         commandString.append("    return out\n");
         commandString.append("  ensure\n");
         commandString.append("    $stdout = STDOUT\n");
         commandString.append("  end\n");
         commandString.append("end\n");
         commandString.append("out = capture_stdout do\n");
         commandString.append("  options = Array.new \n");
         commandString.append("  options << '-d" + sourcePath.getAbsolutePath().replaceAll("\\s", "\\\\ ") + "'\n");
         commandString.append("  options<< '--javac' \n");
         commandString.append("  options<< '-t" + targetPath.getAbsolutePath().replaceAll("\\s", "\\\\ ") + "'\n");
         commandString.append("  options<< '-c" + classPath.replaceAll("\\s", "\\\\ ") + "'\n");
         for (String singleSource : sources)
         {
             commandString.append("  options<< '" + singleSource.replaceAll("\\s", "\\\\ ") + "'\n");
         }
         commandString.append("  $status = JRuby::Compiler::compile_argv(options) \n");
         commandString.append("end\n");
         commandString.append("$finalOut = out.string\n");
 
         //commandString.append("options<< '" + sources + "'\n");
         ScriptEngineManager manager = new ScriptEngineManager();
         ScriptEngine engine = manager.getEngineByName(ENGINE_JRUBY);
         try
         {
             //See: http://stackoverflow.com/questions/4183408/redirect-stdout-to-a-string-in-java
             engine.eval(commandString.toString());
             Long status = (Long) engine.get("status");
             String compilerOutput = (String) engine.get("finalOut");
             if (status.equals(0L))
             {
                 CompilationResult result = new CompilationResult("No Errors");
                 logger.info("No Compilation errors");
                 return result;
             } else
             {
                 CompilationResult result = new CompilationResult("Errors");
                 result.registerError(new CompilationMessage(status, compilerOutput));
                 logger.info(compilerOutput);
                 return result;
             }
         }
         catch (ScriptException e)
         {
             e.printStackTrace();
         }
 
         return null;
     }
 
     private List<String> getSourceFiles()
     {
         WeavingContext context = WeavingContext.getInstance();
         Configuration configuration = context.getConfiguration();
         List<File> sourceFiles = FileUtils.fetchSourceFiles(configuration.getWhitelistedSourceDirs
                 (ENGINE_TYPE_JSF_JRUBY), JRUBY_WILDARD);
         StringBuilder sources = new StringBuilder(sourceFiles.size() * 30);
         List<String> sourcesStr = new ArrayList<String>(sourceFiles.size());
         for (File sourceFile : sourceFiles)
         {
             sourcesStr.add(sourceFile.getAbsolutePath());
             //sources.append(" ");
         }
         return sourcesStr;
     }
 
 }
