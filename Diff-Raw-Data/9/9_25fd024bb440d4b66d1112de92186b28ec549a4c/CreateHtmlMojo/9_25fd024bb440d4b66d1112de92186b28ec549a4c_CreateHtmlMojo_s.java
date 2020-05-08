 package karstenroethig.db.plugin;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import karstenroethig.db.core.dto.Attribute;
 import karstenroethig.db.core.dto.Database;
 import karstenroethig.db.core.dto.Entity;
 import karstenroethig.db.core.formatter.SimpleDatatypeFormatter;
 import karstenroethig.db.plugin.html.HtmlResourceLocator;
 
 import org.apache.maven.plugin.MojoExecutionException;
 import org.apache.maven.plugins.annotations.LifecyclePhase;
 import org.apache.maven.plugins.annotations.Mojo;
 import org.apache.maven.plugins.annotations.Parameter;
 
 @Mojo( name = "create-html", defaultPhase = LifecyclePhase.PACKAGE )
 public class CreateHtmlMojo extends AbstractCreateWithVelocityMojo {
 
     @Parameter(
         defaultValue = "${project.build.directory}/html",
         property = "outputDir",
         required = true
     )
     private File outputDirectory;
 	
 	@Parameter(
 		defaultValue = "${project.artifactId}"
 	)
 	private String artifactId;
 
     private ResourceLocator resourceLocator = new HtmlResourceLocator();
 
     public void execute() throws MojoExecutionException {
 
     	super.execute();
     	
    	Database database = getDatebase();
 
         getLog().info( "creating 'single'" );
         createHtml( database, new File( outputDirectory, "single"), false );
         
         getLog().info( "creating 'overview'" );
         createHtml( database, new File( outputDirectory, "overview"), true );
     }
     
     private void createHtml( Database database, File outputDirectory, boolean withOverview ) throws MojoExecutionException {
     	
     	String artifactVersion = artifactId + "-" + database.getVersion();
     	
     	try {
 
             copyFile( "print.png", outputDirectory, "resources", null );
             copyFile( "database.png", outputDirectory, "resources", null );
             copyFile( "consulting.png", outputDirectory, "resources", null );
             copyFile( "search.png", outputDirectory, "resources", null );
             copyFile( "plus.png", outputDirectory, "resources", null );
             copyFile( "minus.png", outputDirectory, "resources", null );
             copyFile( "style.css", outputDirectory, "resources", null );
             copyFile( "dbmodel.js", outputDirectory, "resources", null );
             copyFile( "jquery-1.10.0.min.js", outputDirectory, "resources", null );
             
             if( withOverview ) {
             	
             	String targetSubDirectory = artifactVersion + "/resources";
             	
             	copyFile( "print.png", outputDirectory, targetSubDirectory, null );
                 copyFile( "database.png", outputDirectory, targetSubDirectory, null );
                 copyFile( "consulting.png", outputDirectory, targetSubDirectory, null );
                 copyFile( "search.png", outputDirectory, targetSubDirectory, null );
                 copyFile( "plus.png", outputDirectory, targetSubDirectory, null );
                 copyFile( "minus.png", outputDirectory, targetSubDirectory, null );
                 copyFile( "style.css", outputDirectory, targetSubDirectory, null );
                 copyFile( "dbmodel.js", outputDirectory, targetSubDirectory, null );
                 copyFile( "jquery-1.10.0.min.js", outputDirectory, targetSubDirectory, null );
             }
 
         } catch( IOException ex ) {
             throw new MojoExecutionException( "Error copying files", ex );
         }
     	
     	try {
     		
     		/*
     		 * Overview
     		 */
     		if( withOverview ) {
 
         		Map<String, Object> overviewParams = new HashMap<String, Object>();
 
         		overviewParams.put( "artifactVersion", artifactVersion );
 
                 evaluateFileWithVelocity( "overview.html", outputDirectory, null, "index.html", overviewParams );
     		}
 
     		/*
     		 * Database
     		 */
             List<String> entityPropertyKeys = new ArrayList<String>();
 
             for( Entity entity : database.getEntities() ) {
 
                 for( String key : entity.getPropertyKeys() ) {
 
                     if( entityPropertyKeys.contains( key ) == false ) {
                         entityPropertyKeys.add( key );
                     }
 
                 }
 
             }
 
             Collections.sort( entityPropertyKeys );
 
             Map<String, Object> databaseParams = new HashMap<String, Object>();
 
             databaseParams.put( "entityPropertyKeys", entityPropertyKeys );
             databaseParams.put( "withOverview", withOverview );
             
             String targetSubDirectory = withOverview ? artifactVersion : null;
 
             evaluateFileWithVelocity( "database.html", outputDirectory, targetSubDirectory, "index.html", databaseParams );
 
             /*
              * Entities
              */
             targetSubDirectory = withOverview ? artifactVersion + "/entities" : "entities";
             
             for( Entity entity : database.getEntities() ) {
 
                 List<String> attributePropertyKeys = new ArrayList<String>();
 
                 for( Attribute attribute : entity.getAttributes() ) {
 
                     for( String key : attribute.getPropertyKeys() ) {
 
                         if( attributePropertyKeys.contains( key ) == false ) {
                             attributePropertyKeys.add( key );
                         }
 
                     }
 
                 }
                 
                 Collections.sort( attributePropertyKeys );
 
                 SimpleDatatypeFormatter datatypeFormatter = new SimpleDatatypeFormatter();
 
                 Map<String, Object> entityParams = new HashMap<String, Object>();
 
                 entityParams.put( "entity", entity );
                 entityParams.put( "attributePropertyKeys", attributePropertyKeys );
                 entityParams.put( "datatypeFormatter", datatypeFormatter );
                 entityParams.put( "withOverview", withOverview );
 
                 evaluateFileWithVelocity( "entity.html", outputDirectory, targetSubDirectory, entity.getName() + ".html",
                     entityParams );
             }
             
             /*
              * Search
              */
             Map<String, Object> searchParams = new HashMap<String, Object>();
 
             searchParams.put( "withOverview", withOverview );
             
             targetSubDirectory = withOverview ? artifactVersion : null;
 
             evaluateFileWithVelocity( "search.html", outputDirectory, targetSubDirectory, null, searchParams );
     		
     	} catch( Exception ex ) {
             throw new MojoExecutionException( "Error creating files", ex );
         }
     }
     
     @Override
     protected ResourceLocator getResourceLocator() {
     	return resourceLocator;
     }
 
     @Override
     protected File getOutputDirectory() {
         return outputDirectory;
     }
 }
