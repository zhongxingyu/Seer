 package com.github.paulmoloney.maven.plugins.enforcer;
 
 /*
  * Licensed to the Apache Software Foundation (ASF) under one
  * or more contributor license agreements.  See the NOTICE file
  * distributed with this work for additional information
  * regarding copyright ownership.  The ASF licenses this file
  * to you under the Apache License, Version 2.0 (the
  * "License"); you may not use this file except in compliance
  * with the License.  You may obtain a copy of the License at
  *
  *  http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing,
  * software distributed under the License is distributed on an
  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
  * KIND, either express or implied.  See the License for the
  * specific language governing permissions and limitations
  * under the License.
  */
 
 import java.util.Arrays;
 import java.util.Iterator;
 import java.util.List;
 
 import org.apache.maven.artifact.versioning.ArtifactVersion;
 import org.apache.maven.artifact.versioning.DefaultArtifactVersion;
 import org.apache.maven.enforcer.rule.api.EnforcerRuleException;
 import org.apache.maven.enforcer.rule.api.EnforcerRuleHelper;
 import org.apache.maven.plugin.MojoExecutionException;
 import org.apache.maven.plugin.logging.Log;
 import org.apache.maven.plugins.annotations.Parameter;
 import org.apache.maven.toolchain.Toolchain;
 import org.codehaus.plexus.compiler.manager.CompilerManager;
 import org.codehaus.plexus.compiler.manager.NoSuchCompilerException;
 import org.codehaus.plexus.component.repository.exception.ComponentLookupException;
 import org.codehaus.plexus.util.StringUtils;
 
 import com.github.paulmoloney.maven.plugins.utils.DefaultProcessExecutor;
 import com.github.paulmoloney.maven.plugins.utils.ProcessExecutor;
 import com.github.paulmoloney.maven.plugins.utils.ProcessExecutorException;
 
 /** This rule checks that the Java compiler version matched in toolchains.xml is allowed.
  * @author <a href="mailto:">Paul Moloney</a>
  * @version $Id: RuleJavaVersionToolchainAware.java $
  */
 public class RuleJavaVersionToolchainAware extends AbstractToolChainAwareRule {
     @Parameter( property = "maven.compiler.compilerId", defaultValue = "javac" )
     private String compilerId;
 
     /*
      * From plexus
      */
     private CompilerManager compilerManager;
 
     @Parameter(property = "maven.compiler.compilerArgument", defaultValue = "-version" )
     private String compilerArgument;
 
     /**
      * If a suitable compiler from toolchains.xml can not be found, then try to match based on typical environmental variables
      */
     @Parameter (defaultValue = "true")
     private boolean isFallBackAllowed;
 
     private ProcessExecutor process;
     
 	/** 
 	* This particular rule determines if the specified Java compiler version referenced in the toolchains.xml is an appropriate version
 	* @see org.apache.maven.enforcer.rule.api.EnforcerRule&#execute(org.apache.maven.enforcer.rule.api.EnforcerRuleHelper)
 	*/
     public void execute( EnforcerRuleHelper helper ) throws EnforcerRuleException {
     	try
     	{
     	    super.init(helper);
     	    try 
     	    {
 	            compilerManager = (CompilerManager) helper.getComponent(CompilerManager.class);
     	    }
     	    catch (ComponentLookupException e)
     	    {
     	        throw new MojoExecutionException ("Unable to retrieve component", e);
     	    }
     	    if (null == compilerId || "".equals(compilerId))
     	    {
     	    	compilerId = "javac";
     	    }
     	    if (null == compilerArgument || "".equals(compilerArgument))
     	    {
     	    	compilerArgument = "-version";
     	    }
     	    /*try
     	    {
     	        compilerId = (String) helper.evaluate("maven.compiler.compilerId");
     	    }
     	    catch (ExpressionEvaluationException e)
     	    {
     	        throw new MojoExecutionException ("Unable to determine compiler id", e);
     	    }*/
     	}
     	catch (MojoExecutionException e)
     	{
     		throw new EnforcerRuleException("Error initialising mojo", e);
     	}
 	    String java_version;
 	    final Log log = helper.getLog();
 
         log.debug( "Using compiler id'" + getCompilerId() + "'." );
 
         try
         {
             getCompilerManager().getCompiler( getCompilerId() );
         }
         catch ( NoSuchCompilerException e )
         {
             throw new EnforcerRuleException( "No compiler with id: '" + e.getCompilerId() + "'." );
         }
 
         try
         {
             Toolchain tc = findToolChain("jdk", helper, null);
             if (tc != null)
             {
         	    executable = tc.findTool( getCompilerId() );
//        	    if (null != executable)
//        	    {
//        	    	executable = executable + getExecutableExtension();
//        	    }
             }
         }
         catch (MojoExecutionException e)
         {
         	throw new EnforcerRuleException("", e);
         }
 
         if (null == executable && isFallBackAllowed())
         {
         	executable = findToolExecutable(getCompilerId() + getExecutableExtension(), log, "java.home",
         			new String [] { "../bin", "bin", "../sh" },
         			new String [] { "JDK_HOME", "JAVA_HOME" }, new String[] { "bin", "sh" }
         	    );
         }
 
         if (null == executable || "".equals(executable.trim()))
         {
             throw new EnforcerRuleException("No valid executable found, aborting");
         }
         setProcess(process);
         java_version = runToolAndRetrieveVersion(process, log);
 
 	    String clean_java_version = normalizeJDKVersion( java_version );
 	    log.debug( "Normalized Java Version: " + clean_java_version );
 	
 	    ArtifactVersion detectedJdkVersion = new DefaultArtifactVersion(clean_java_version );
 	    log.debug( "Parsed Version: Major: " + detectedJdkVersion.getMajorVersion() + " Minor: "
 	        + detectedJdkVersion.getMinorVersion() + " Incremental: " + detectedJdkVersion.getIncrementalVersion()
 	        + " Build: " + detectedJdkVersion.getBuildNumber() + "Qualifier: " + detectedJdkVersion.getQualifier() );
 	
 	    log.debug("Rule requires: " + version);
 	    enforceVersion( log, "JDK", getVersion(), detectedJdkVersion );
     } 
 
 	/**
 	* Converts a jdk string from 1.5.0-11b12 to a single 3 digitversion like 1.5.0-11
 	*
 	* @param theJdkVersion to be converted.
 	* @return the converted string.
 	*/
     private String normalizeJDKVersion( String theJdkVersion ) {
 	    theJdkVersion = theJdkVersion.replaceAll( "_|-", "." );
 	    String tokenArray[] = StringUtils.split( theJdkVersion, "." );
 	    List<String> tokens = Arrays.asList( tokenArray );
 	    StringBuffer buffer = new StringBuffer( theJdkVersion.length());
 	    Iterator<String> iter = tokens.iterator();
 	    
 		for ( int i = 0; i < tokens.size() && i < 4; i++ ) {
 	        String section = (String) iter.next();
 	        section = section.replaceAll( "[^0-9]", "" );
 	        if ( StringUtils.isNotEmpty( section ) ) {
 	            buffer.append( Integer.parseInt( section ) );
 	            if ( i != 2 ) {
 	                buffer.append( '.' );
 	            } else {
 	                buffer.append( '-' );
 	            }
 	       }
 	    }
 	
 	    String version = buffer.toString();
 	    version = StringUtils.stripEnd( version, "-" );
 	    return StringUtils.stripEnd( version, "." );
     }
 
     /**
      * Runs the specified java compiler to find out its version
      * @param process to run
      * @param log to write to
      * @return the version of specified executable
      * @throws EnforcerRuleException if version can not be determined
      */
     private String runToolAndRetrieveVersion(ProcessExecutor process, final Log log) throws EnforcerRuleException
     {
         try {    	
     	    String firstLine = process.runApplication();
 
             String [] output = firstLine.split("\\s");
             if (output.length > 1)
             {
         	    log.debug(executable + " version: " + output[1]);
         	    return output[1];
             }
             throw new EnforcerRuleException("No valid version could be determined for " + process.toString());
         }
         catch (ProcessExecutorException e)
         {
         	throw new EnforcerRuleException("Error determining version", e);
         }
     }
 
     private String getCompilerArgument()
     {
         return compilerArgument;
     }
 
     private CompilerManager getCompilerManager()
     {
     	return compilerManager;
     }
 
     private String getCompilerId()
     {
     	return compilerId;
     }
 
     private boolean isFallBackAllowed() {
     	return isFallBackAllowed;
     }
     
     protected void setCompilerId(String compilerId) {
     	if (null == compilerId || "".equals(compilerId.trim())) {
     		throw new IllegalArgumentException("CompilerId cannot be null or empty");
     	}
     	this.compilerId = compilerId;
     }
     
     protected void setProcess(ProcessExecutor process)
     {
     	if (null != process)
     	{
     	    this.process = process;	
     	}
     	else
     	{
     	    this.process = new DefaultProcessExecutor().createExecutor(executable, getCompilerArgument());
     	}
     }
     
     protected void setFallback(boolean isFallBackAllowed)
     {
     	this.isFallBackAllowed = isFallBackAllowed;
     }
 }
