 package org.apache.maven.plugin.testing;
 
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
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.Reader;
 import java.lang.reflect.AccessibleObject;
 import java.lang.reflect.Field;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import org.apache.maven.DefaultMaven;
 import org.apache.maven.Maven;
 import org.apache.maven.execution.DefaultMavenExecutionRequest;
 import org.apache.maven.execution.DefaultMavenExecutionResult;
 import org.apache.maven.execution.MavenExecutionRequest;
 import org.apache.maven.execution.MavenExecutionRequestPopulationException;
 import org.apache.maven.execution.MavenExecutionRequestPopulator;
 import org.apache.maven.execution.MavenExecutionResult;
 import org.apache.maven.execution.MavenSession;
 import org.apache.maven.monitor.logging.DefaultLog;
 import org.apache.maven.plugin.Mojo;
 import org.apache.maven.plugin.MojoExecution;
 import org.apache.maven.plugin.PluginParameterExpressionEvaluator;
 import org.apache.maven.plugin.descriptor.PluginDescriptor;
 import org.apache.maven.plugin.descriptor.PluginDescriptorBuilder;
 import org.apache.maven.plugin.logging.Log;
 import org.apache.maven.project.MavenProject;
 import org.apache.maven.project.ProjectBuilder;
 import org.apache.maven.project.ProjectBuildingException;
 import org.apache.maven.project.ProjectBuildingRequest;
 import org.apache.maven.project.ProjectBuildingResult;
 import org.apache.maven.settings.building.SettingsBuildingException;
 import org.codehaus.plexus.ContainerConfiguration;
 import org.codehaus.plexus.DefaultContainerConfiguration;
 import org.codehaus.plexus.DefaultPlexusContainer;
 import org.codehaus.plexus.PlexusContainer;
 import org.codehaus.plexus.PlexusContainerException;
 import org.codehaus.plexus.PlexusTestCase;
 import org.codehaus.plexus.classworlds.ClassWorld;
 import org.codehaus.plexus.component.configurator.ComponentConfigurator;
 import org.codehaus.plexus.component.configurator.expression.ExpressionEvaluator;
 import org.codehaus.plexus.component.repository.ComponentDescriptor;
 import org.codehaus.plexus.component.repository.exception.ComponentLookupException;
 import org.codehaus.plexus.configuration.PlexusConfiguration;
 import org.codehaus.plexus.configuration.xml.XmlPlexusConfiguration;
 import org.codehaus.plexus.logging.LoggerManager;
 import org.codehaus.plexus.util.InterpolationFilterReader;
 import org.codehaus.plexus.util.ReaderFactory;
 import org.codehaus.plexus.util.ReflectionUtils;
 import org.codehaus.plexus.util.xml.XmlStreamReader;
 import org.codehaus.plexus.util.xml.Xpp3Dom;
 import org.codehaus.plexus.util.xml.Xpp3DomBuilder;
 import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
 import org.sonatype.aether.util.DefaultRepositorySystemSession;
 
 /**
  * TODO: add a way to use the plugin POM for the lookup so that the user doesn't have to provide the a:g:v:goal as the
  * role hint for the mojo lookup. TODO: standardize the execution of the mojo and looking at the results, but could
  * simply have a template method for verifying the state of the mojo post execution TODO: need a way to look at the
  * state of the mojo without adding getters, this could be where we finally specify the expressions which extract values
  * from the mojo. TODO: create a standard directory structure for picking up POMs to make this even easier, we really
  * just need a testing descriptor and make this entirely declarative!
  * 
  * @author jesse
  * @version $Id: AbstractMojoTestCase.java 805786 2009-08-19 13:04:24Z bentmann $
  */
 public abstract class AbstractMojoTestCase
     extends PlexusTestCase
 {
     private ComponentConfigurator configurator;
 
     private PlexusContainer container;
 
     private PluginDescriptor descriptor;
 
     /*
      * for the harness I think we have decided against going the route of using the maven project builder. instead I
      * think we are going to try and make an instance of the localrespository and assign that to either the project stub
      * or into the mojo directly with injection...not sure yet though.
      */
     // private MavenProjectBuilder projectBuilder;
 
     protected void setUp()
         throws Exception
     {
         configurator = getContainer().lookup( ComponentConfigurator.class, "basic" );
 
         descriptor = new PluginDescriptorBuilder().build( getPluginReader() );
 
         for ( ComponentDescriptor<?> desc : descriptor.getComponents() )
         {
             getContainer().addComponentDescriptor( desc );
         }
     }
 
     protected InputStream getPublicDescriptorStream()
         throws Exception
     {
         return new FileInputStream( new File( getPluginDescriptorPath() ) );
     }
 
     protected InterpolationFilterReader getPluginReader()
         throws IOException
     {
         InputStream is = getClass().getResourceAsStream( "/" + getPluginDescriptorLocation() );
 
         XmlStreamReader reader = ReaderFactory.newXmlReader( is );
 
         return new InterpolationFilterReader( new BufferedReader( reader ),
                                               getContainer().getContext().getContextData() );
     }
 
     protected String getPluginDescriptorPath()
     {
         return getBasedir() + "/target/classes/META-INF/maven/plugin.xml";
     }
 
     protected String getPluginDescriptorLocation()
     {
         return "META-INF/maven/plugin.xml";
     }
 
     protected void setupContainer()
     {
         ClassWorld classWorld = new ClassWorld( "plexus.core", Thread.currentThread().getContextClassLoader() );
 
         ContainerConfiguration cc =
             new DefaultContainerConfiguration().setClassWorld( classWorld ).setName( "embedder" );
         try
         {
             container = new DefaultPlexusContainer( cc );
         }
         catch ( PlexusContainerException e )
         {
             e.printStackTrace();
             fail( "Failed to create plexus container." );
         }
     }
 
     protected PlexusContainer getContainer()
     {
         if ( container == null )
         {
             setupContainer();
         }
 
         return container;
     }
 
     /**
      * Lookup the mojo leveraging the subproject pom
      * 
      * @param goal
      * @param pluginPom
      * @return a Mojo instance
      * @throws Exception
      */
     protected Mojo lookupMojo( String goal, String pluginPom )
         throws Exception
     {
         return lookupMojo( goal, new File( pluginPom ) );
     }
 
     /**
      * Lookup an empty mojo
      * 
      * @param goal
      * @param pluginPom
      * @return a Mojo instance
      * @throws Exception
      */
     protected Mojo lookupEmptyMojo( String goal, String pluginPom )
         throws Exception
     {
         return lookupEmptyMojo( goal, new File( pluginPom ) );
     }
 
     /**
      * Lookup the mojo leveraging the actual subprojects pom
      * 
      * @param goal
      * @param pom
      * @return a Mojo instance
      * @throws Exception
      */
     protected Mojo lookupMojo( String goal, File pom )
         throws Exception
     {
         File pluginPom = new File( getBasedir(), "pom.xml" );
 
         Xpp3Dom pluginPomDom = Xpp3DomBuilder.build( ReaderFactory.newXmlReader( pluginPom ) );
 
         String artifactId = pluginPomDom.getChild( "artifactId" ).getValue();
 
         String groupId = resolveFromRootThenParent( pluginPomDom, "groupId" );
 
         String version = resolveFromRootThenParent( pluginPomDom, "version" );
 
         PlexusConfiguration pluginConfiguration = extractPluginConfiguration( artifactId, pom );
 
         PlexusConfiguration parametersConfiguration = extractParameterConfiguration( goal );
 
         return lookupMojo( groupId, artifactId, version, pluginPom, goal, pluginConfiguration, parametersConfiguration );
     }
 
     /**
      * Lookup the mojo leveraging the actual subprojects pom
      * 
      * @param goal
      * @param pom
      * @return a Mojo instance
      * @throws Exception
      */
     protected Mojo lookupEmptyMojo( String goal, File pom )
         throws Exception
     {
         File pluginPom = new File( getBasedir(), "pom.xml" );
 
         Xpp3Dom pluginPomDom = Xpp3DomBuilder.build( ReaderFactory.newXmlReader( pluginPom ) );
 
         String artifactId = pluginPomDom.getChild( "artifactId" ).getValue();
 
         String groupId = resolveFromRootThenParent( pluginPomDom, "groupId" );
 
         String version = resolveFromRootThenParent( pluginPomDom, "version" );
 
         return lookupMojo( groupId, artifactId, version, goal, null );
     }
 
     /**
      * lookup the mojo while we have all of the relavent information
      * 
      * @param groupId
      * @param artifactId
      * @param version
      * @param pom
      * @param goal
      * @param pluginConfiguration
      * @param parametersConfiguration
      * @return a Mojo instance
      * @throws Exception
      */
     protected Mojo lookupMojo( String groupId, String artifactId, String version, File pom, String goal,
                                PlexusConfiguration pluginConfiguration, PlexusConfiguration parametersConfiguration )
         throws Exception
     {
         validateContainerStatus();
 
         // pluginkey = groupId : artifactId : version : goal
 
         Mojo mojo = (Mojo) lookup( Mojo.ROLE, groupId + ":" + artifactId + ":" + version + ":" + goal );
 
         LoggerManager loggerManager = (LoggerManager) getContainer().lookup( LoggerManager.class );
 
         Log mojoLogger = new DefaultLog( loggerManager.getLoggerForComponent( Mojo.ROLE ) );
 
         mojo.setLog( mojoLogger );
 
         if ( pluginConfiguration != null )
         {
             /*
              * requires v10 of plexus container for lookup on expression evaluator ExpressionEvaluator evaluator =
              * (ExpressionEvaluator) getContainer().lookup( ExpressionEvaluator.ROLE, "stub-evaluator" );
              */
             ExpressionEvaluator evaluator = new ResolverExpressionEvaluatorStub();
 
             configurator.configureComponent( mojo, pluginConfiguration, evaluator, getContainer().getContainerRealm() );
         }
 
         if ( parametersConfiguration != null )
         {
             MavenExecutionRequest request = getRequest( pom );
 
             MavenSession session = getSession( request );
 
             MojoExecution execution = getMojoExecution( goal );
 
             ProjectBuilder projectBuilder = getContainer().lookup( ProjectBuilder.class );
 
             session.setProjects( getProjectsForMavenReactor( projectBuilder, request ) );
 
             ExpressionEvaluator expressionEvaluator = new PluginParameterExpressionEvaluator( session, execution );
 
             configurator.configureComponent( mojo, parametersConfiguration, expressionEvaluator,
                                              descriptor.getClassRealm() );
         }
 
         return mojo;
     }
 
     /*
      * Method for backward-compatibility in case anyone is using this method directly
      */
     protected Mojo lookupMojo( String groupId, String artifactId, String version, String goal,
                                PlexusConfiguration pluginConfiguration )
         throws Exception
     {
         return lookupMojo( groupId, artifactId, version, null, goal, pluginConfiguration, null );
     }
 
     protected MavenExecutionRequest getRequest( File pom )
         throws MavenExecutionRequestPopulationException, ComponentLookupException
     {
         DefaultMavenExecutionRequest request = new DefaultMavenExecutionRequest();
 
         MavenExecutionRequestPopulator populator = getContainer().lookup( MavenExecutionRequestPopulator.class );
 
         populator.populateDefaults( request );
 
         request.setPom( pom );
 
         request.setSystemProperties( System.getProperties() );
 
         return request;
     }
 
     protected MavenSession getSession( MavenExecutionRequest request )
         throws ComponentLookupException, MavenExecutionRequestPopulationException, SettingsBuildingException
     {
         DefaultMaven maven = (DefaultMaven) getContainer().lookup( Maven.class );
 
         MavenExecutionResult result = new DefaultMavenExecutionResult();
 
         DefaultRepositorySystemSession repoSession =
             (DefaultRepositorySystemSession) maven.newRepositorySession( request );
 
         MavenSession session = new MavenSession( container, repoSession, request, result );
 
         return session;
     }
 
     protected MojoExecution getMojoExecution( String goal )
     {
         return new MojoExecution( descriptor.getMojo( goal ), "default-cli", MojoExecution.Source.CLI );
     }
 
     /**
      * Return a list of projects based on the provided POM. This way project properties can be properly injected as mojo
      * parameters.
      * 
      * @param projectBuilder
      * @param request
      * @return
      * @throws ProjectBuildingException
      */
     private List<MavenProject> getProjectsForMavenReactor( ProjectBuilder projectBuilder, MavenExecutionRequest request )
         throws ProjectBuildingException
     {
         List<MavenProject> projects = new ArrayList<MavenProject>();
 
         // If no POM was provided, there's no way to extract project properties, so just return an empty list.
         if ( request.getPom() == null )
             return projects;
 
         List<File> files = Arrays.asList( request.getPom().getAbsoluteFile() );
 
         ProjectBuildingRequest projectBuildingRequest = request.getProjectBuildingRequest();
 
         List<ProjectBuildingResult> results =
             projectBuilder.build( files, request.isRecursive(), projectBuildingRequest );
 
         for ( ProjectBuildingResult result : results )
         {
             projects.add( result.getProject() );
         }
 
         return projects;
     }
 
     /**
      * Extract the parameters configuration from the specified Mojo
      * 
      * @param mojo Specifies the mojo/goal where the parameters will be extracted from
      * @return A plexus configuration object that can be passed on to an expression evaluator
      * @throws XmlPullParserException
      * @throws IOException
      */
     protected PlexusConfiguration extractParameterConfiguration( String mojo )
         throws XmlPullParserException, IOException
     {
         PlexusConfiguration parameterConfigurations = null;
         PlexusConfiguration c = new XmlPlexusConfiguration( Xpp3DomBuilder.build( getPluginReader() ) );
         PlexusConfiguration mojos = c.getChild( "mojos" );
         if ( mojos != null )
         {
             PlexusConfiguration[] mojosChildren = mojos.getChildren( "mojo" );
             for ( PlexusConfiguration mojoConfig : mojosChildren )
             {
                if ( mojoConfig.getChild( "build" ) != null
                    && mojoConfig.getChild( "build" ).getValue().equalsIgnoreCase( mojo ) )
                 {
                     parameterConfigurations = mojoConfig.getChild( "configuration" );
                 }
             }
         }
         return parameterConfigurations;
     }
 
     /**
      * @param artifactId
      * @param pom
      * @return the plexus configuration
      * @throws Exception
      */
     protected PlexusConfiguration extractPluginConfiguration( String artifactId, File pom )
         throws Exception
     {
         Reader reader = ReaderFactory.newXmlReader( pom );
 
         Xpp3Dom pomDom = Xpp3DomBuilder.build( reader );
 
         return extractPluginConfiguration( artifactId, pomDom );
     }
 
     /**
      * @param artifactId
      * @param pomDom
      * @return the plexus configuration
      * @throws Exception
      */
     protected PlexusConfiguration extractPluginConfiguration( String artifactId, Xpp3Dom pomDom )
         throws Exception
     {
         Xpp3Dom pluginConfigurationElement = null;
 
         Xpp3Dom buildElement = pomDom.getChild( "build" );
         if ( buildElement != null )
         {
             Xpp3Dom pluginsRootElement = buildElement.getChild( "plugins" );
 
             if ( pluginsRootElement != null )
             {
                 Xpp3Dom[] pluginElements = pluginsRootElement.getChildren();
 
                 for ( int i = 0; i < pluginElements.length; i++ )
                 {
                     Xpp3Dom pluginElement = pluginElements[i];
 
                     String pluginElementArtifactId = pluginElement.getChild( "artifactId" ).getValue();
 
                     if ( pluginElementArtifactId.equals( artifactId ) )
                     {
                         pluginConfigurationElement = pluginElement.getChild( "configuration" );
 
                         break;
                     }
                 }
 
                 if ( pluginConfigurationElement == null )
                 {
                     throw new ConfigurationException( "Cannot find a configuration element for a plugin with an "
                         + "artifactId of " + artifactId + "." );
                 }
             }
         }
 
         if ( pluginConfigurationElement == null )
         {
             throw new ConfigurationException( "Cannot find a configuration element for a plugin with an artifactId of "
                 + artifactId + "." );
         }
 
         return new XmlPlexusConfiguration( pluginConfigurationElement );
     }
 
     /**
      * Configure the mojo
      * 
      * @param mojo
      * @param artifactId
      * @param pom
      * @return a Mojo instance
      * @throws Exception
      */
     protected Mojo configureMojo( Mojo mojo, String artifactId, File pom )
         throws Exception
     {
         validateContainerStatus();
 
         PlexusConfiguration pluginConfiguration = extractPluginConfiguration( artifactId, pom );
 
         ExpressionEvaluator evaluator = new ResolverExpressionEvaluatorStub();
 
         configurator.configureComponent( mojo, pluginConfiguration, evaluator, getContainer().getContainerRealm() );
 
         return mojo;
     }
 
     /**
      * Configure the mojo with the given plexus configuration
      * 
      * @param mojo
      * @param pluginConfiguration
      * @return a Mojo instance
      * @throws Exception
      */
     protected Mojo configureMojo( Mojo mojo, PlexusConfiguration pluginConfiguration )
         throws Exception
     {
         validateContainerStatus();
 
         ExpressionEvaluator evaluator = new ResolverExpressionEvaluatorStub();
 
         configurator.configureComponent( mojo, pluginConfiguration, evaluator, getContainer().getContainerRealm() );
 
         return mojo;
     }
 
     /**
      * Convenience method to obtain the value of a variable on a mojo that might not have a getter. NOTE: the caller is
      * responsible for casting to to what the desired type is.
      * 
      * @param object
      * @param variable
      * @return object value of variable
      * @throws IllegalArgumentException
      */
     protected Object getVariableValueFromObject( Object object, String variable )
         throws IllegalAccessException
     {
         Field field = ReflectionUtils.getFieldByNameIncludingSuperclasses( variable, object.getClass() );
 
         field.setAccessible( true );
 
         return field.get( object );
     }
 
     /**
      * Convenience method to obtain all variables and values from the mojo (including its superclasses) Note: the values
      * in the map are of type Object so the caller is responsible for casting to desired types.
      * 
      * @param object
      * @return map of variable names and values
      */
     protected Map getVariablesAndValuesFromObject( Object object )
         throws IllegalAccessException
     {
         return getVariablesAndValuesFromObject( object.getClass(), object );
     }
 
     /**
      * Convenience method to obtain all variables and values from the mojo (including its superclasses) Note: the values
      * in the map are of type Object so the caller is responsible for casting to desired types.
      * 
      * @param clazz
      * @param object
      * @return map of variable names and values
      */
     protected Map getVariablesAndValuesFromObject( Class clazz, Object object )
         throws IllegalAccessException
     {
         Map map = new HashMap();
 
         Field[] fields = clazz.getDeclaredFields();
 
         AccessibleObject.setAccessible( fields, true );
 
         for ( int i = 0; i < fields.length; ++i )
         {
             Field field = fields[i];
 
             map.put( field.getName(), field.get( object ) );
 
         }
 
         Class superclass = clazz.getSuperclass();
 
         if ( !Object.class.equals( superclass ) )
         {
             map.putAll( getVariablesAndValuesFromObject( superclass, object ) );
         }
 
         return map;
     }
 
     /**
      * Convenience method to set values to variables in objects that don't have setters
      * 
      * @param object
      * @param variable
      * @param value
      * @throws IllegalAccessException
      */
     protected void setVariableValueToObject( Object object, String variable, Object value )
         throws IllegalAccessException
     {
         Field field = ReflectionUtils.getFieldByNameIncludingSuperclasses( variable, object.getClass() );
 
         field.setAccessible( true );
 
         field.set( object, value );
     }
 
     /**
      * sometimes the parent element might contain the correct value so generalize that access TODO find out where this
      * is probably done elsewhere
      * 
      * @param pluginPomDom
      * @param element
      * @return
      * @throws Exception
      */
     private String resolveFromRootThenParent( Xpp3Dom pluginPomDom, String element )
         throws Exception
     {
         Xpp3Dom elementDom = pluginPomDom.getChild( element );
 
         // parent might have the group Id so resolve it
         if ( elementDom == null )
         {
             Xpp3Dom pluginParentDom = pluginPomDom.getChild( "parent" );
 
             if ( pluginParentDom != null )
             {
                 elementDom = pluginParentDom.getChild( element );
 
                 if ( elementDom == null )
                 {
                     throw new Exception( "unable to determine " + element );
                 }
 
                 return elementDom.getValue();
             }
 
             throw new Exception( "unable to determine " + element );
         }
 
         return elementDom.getValue();
     }
 
     /**
      * We should make sure this is called in each method that makes use of the container, otherwise we throw ugly NPE's
      * crops up when the subclassing code defines the setUp method but doesn't call super.setUp()
      * 
      * @throws Exception
      */
     private void validateContainerStatus()
         throws Exception
     {
         if ( getContainer() != null )
         {
             return;
         }
 
         throw new Exception( "container is null, make sure super.setUp() is called" );
     }
 }
