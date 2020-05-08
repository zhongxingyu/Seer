 /**
  * Copyright 2011 Matthias Nuessler <m.nuessler@web.de>
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
 package org.nuessler.maven.plugin.cakupan;
 
 import java.io.File;
 import java.util.Collections;
 import java.util.List;
 
 import org.apache.commons.collections.CollectionUtils;
 import org.apache.commons.lang3.ArrayUtils;
 import org.apache.commons.lang3.StringUtils;
 import org.apache.maven.plugin.MojoExecutionException;
 import org.apache.maven.plugin.MojoFailureException;
 import org.codehaus.plexus.util.DirectoryScanner;
 
 import com.cakupan.xslt.exception.XSLTCoverageException;
 import com.cakupan.xslt.instrument.InstrumentXSLT;
 import com.cakupan.xslt.util.CoverageIOUtil;
 import com.cakupan.xslt.util.XSLTCakupanUtil;
 
 /**
  * Instrument XSLT files for test coverage.
  *
  * @author Matthias Nuessler
  * @goal instrument
  * @phase process-test-classes
  * @requiresDependencyResolution test
  */
 public class CakupanInstrumentMojo extends AbstractCakupanMojo {
 
     /**
      * @parameter default-value="${project.build.outputDirectory}"
      */
     private File buildOutputDirectory;
 
     /**
      * A list of &lt;include> elements specifying the XSLT files (by pattern)
      * that should be included in instrumenting. When not specified, the default
      * includes will be <code><br/>
      * &lt;xsltIncludes><br/>
      * &nbsp;&lt;include>**&#47;*.xsl&lt;/include><br/>
      * &lt;/xsltIncludes><br/>
      * </code>
      *
      * @parameter
      */
     private List<String> xsltIncludes;
 
     /**
      * A list of &lt;exclude> elements specifying the XSLT files (by pattern)
      * that should be included in instrumenting. When not specified, the default
      * includes will be <code><br/>
      * &lt;xsltExcludes><br/>
      * &nbsp;&lt;exclude>**&#47;*.xsl&lt;/exclude><br/>
      * &lt;/xsltExcludes><br/>
      * </code>
      *
      * @parameter
      */
     private List<String> xsltExcludes;
 
     @Override
     public void execute() throws MojoExecutionException, MojoFailureException {
         File instrumentDestDir = getInstrumentDestDir();
         getLog().info("instrumentDestDir: " + instrumentDestDir);
         getLog().info("buildOutputDirectory: " + buildOutputDirectory);
 
         if (!instrumentDestDir.exists()) {
             instrumentDestDir.mkdirs();
         }
        if (!buildOutputDirectory.exists()) {
            buildOutputDirectory.mkdirs();
        }
 
         if (CollectionUtils.isEmpty(xsltIncludes)) {
             getLog().debug("No xslt-includes given, using defaults");
             xsltIncludes = Collections.singletonList("**/*.xsl");
         } else {
             getLog().debug("XSLT includes: " + StringUtils.join(xsltIncludes.iterator(), ", "));
         }
 
         InstrumentXSLT instrumentXslt = new InstrumentXSLT();
         getLog().info("Start instrumenting XSLTs.");
         CoverageIOUtil.setDestDir(instrumentDestDir);
 
         DirectoryScanner scanner = new DirectoryScanner();
         scanner.setBasedir(buildOutputDirectory);
         scanner.setCaseSensitive(true);
         scanner.addDefaultExcludes();
         if (CollectionUtils.isNotEmpty(xsltIncludes)) {
             scanner.setIncludes(xsltIncludes.toArray(new String[xsltIncludes.size()]));
         }
         if (CollectionUtils.isNotEmpty(xsltExcludes)) {
             scanner.setExcludes(xsltExcludes.toArray(new String[xsltExcludes.size()]));
         }
         scanner.scan();
         String[] includedFiles = scanner.getIncludedFiles();
 
         if (ArrayUtils.isEmpty(includedFiles)) {
            getLog().info("No XSLT files found in build output directory. Did you run 'mvn compile'?");
             return;
         }
 
         for (String includedFile : includedFiles) {
             try {
                getLog().info("Instrumenting XSLT file: " + includedFile);
                 instrumentXslt.initCoverageMap(new File(buildOutputDirectory, includedFile).getCanonicalPath());
             } catch (Exception e) {
                 getLog().error(e);
                 throw new MojoExecutionException("Instrumenting file [" + includedFile + "] failed!");
             }
         }
 
         try {
             XSLTCakupanUtil.dumpCoverageStats();
         } catch (XSLTCoverageException e) {
             throw new MojoExecutionException("Dump coverage file failed!");
         } finally {
             XSLTCakupanUtil.cleanCoverageStats();
         }
     }
 
 }
