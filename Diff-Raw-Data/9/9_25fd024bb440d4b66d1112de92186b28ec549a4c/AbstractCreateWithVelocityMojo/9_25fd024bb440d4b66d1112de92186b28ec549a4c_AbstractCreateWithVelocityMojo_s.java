 package karstenroethig.db.plugin;
 
 import java.io.File;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.StringWriter;
 import java.net.URL;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import karstenroethig.db.core.DatabaseModel;
 import karstenroethig.db.core.dto.Database;
 import karstenroethig.db.core.locator.AbstractDatabaseLocator;
 import karstenroethig.db.core.utils.DateUtils;
 import karstenroethig.db.plugin.utils.TextUtils;
 
 import org.apache.commons.io.FileUtils;
 import org.apache.commons.io.IOUtils;
 import org.apache.commons.lang3.StringUtils;
 import org.apache.maven.plugin.AbstractMojo;
 import org.apache.maven.plugin.MojoExecutionException;
 import org.apache.maven.plugins.annotations.Parameter;
 import org.apache.velocity.VelocityContext;
 import org.apache.velocity.app.VelocityEngine;
 
 public abstract class AbstractCreateWithVelocityMojo extends AbstractMojo {
 
     @Parameter(
         property = "databaseLocatorClass",
         required = true
     )
     private String databaseLocatorClass;
     
     private Database database = null;
 
     private VelocityEngine velocityEngine = null;
     
     private TextUtils textUtils = new TextUtils();
 
     public void execute() throws MojoExecutionException {
 
         // Check class in classpath
         Class databaseLocatorClazz;
 
         try {
             databaseLocatorClazz = Class.forName( databaseLocatorClass );
         } catch( ClassNotFoundException ex ) {
             throw new MojoExecutionException( "Class not found: " + databaseLocatorClass, ex );
         }
 
         // Create instance
         Object databaseLocatorInstance;
 
         try {
             databaseLocatorInstance = databaseLocatorClazz.newInstance();
         } catch( Exception ex ) {
             throw new MojoExecutionException( "Could not create instance of class " + databaseLocatorClass, ex );
         }
 
         // Check instance
         AbstractDatabaseLocator databaseLocator;
 
         if( databaseLocatorInstance instanceof AbstractDatabaseLocator ) {
             databaseLocator = ( AbstractDatabaseLocator )databaseLocatorInstance;
         } else {
             throw new MojoExecutionException( "databaseLocatorClass is no instance of " +
                 AbstractDatabaseLocator.class.getName() );
         }
 
         // Load database model
         database = DatabaseModel.loadDatabaseModel( databaseLocator );
 
         if( database == null ) {
             throw new MojoExecutionException( "Could not load database model. Please run common test classes." );
         }
 
         // Create output directory
         File outputDirectory = getOutputDirectory();
         
         if( outputDirectory != null && !outputDirectory.exists() ) {
             outputDirectory.mkdirs();
         }
 
     }
 
     protected void copyFile( String sourceFilename, File outputDirectory, String targetSubDirectory,
         String targetFilename ) throws IOException {
 
         if( StringUtils.isBlank( targetFilename ) ) {
             targetFilename = sourceFilename;
         }
 
         URL url = getResourceLocator().resolve( sourceFilename );
 
         File targetFile = null;
 
         if( StringUtils.isNotBlank( targetSubDirectory ) ) {
 
             File targetDirectory = new File( outputDirectory, targetSubDirectory );
             targetFile = new File( targetDirectory, targetFilename );
 
         } else {
 
             targetFile = new File( outputDirectory, targetFilename );
 
         }
 
         FileUtils.copyURLToFile( url, targetFile );
 
         getLog().info( "Datei wurde kopiert: " + targetFile.getName() );
 
     }
 
     protected void evaluateFileWithVelocity( String sourceFilename, File outputDirectory, String targetSubDirectory,
         String targetFilename, Map<String, Object> params ) throws Exception {
 
         if( velocityEngine == null ) {
             velocityEngine = new VelocityEngine();
             velocityEngine.init();
         }
 
         // use source filename if target filename is blank
         if( StringUtils.isBlank( targetFilename ) ) {
             targetFilename = sourceFilename;
         }
 
         // create target file
         File targetFile = null;
 
         if( StringUtils.isNotBlank( targetSubDirectory ) ) {
 
             File targetDirectory = new File( outputDirectory, targetSubDirectory );
             targetFile = new File( targetDirectory, targetFilename );
 
         } else {
 
             targetFile = new File( outputDirectory, targetFilename );
 
         }
 
         // load template from source file
         URL url = getResourceLocator().resolve( sourceFilename );
         String template = StringUtils.EMPTY;
         InputStream in = null;
 
         try {
             in = url.openStream();
 
             List<String> lines = IOUtils.readLines( in );
 
             StringBuffer templateBuffer = new StringBuffer();
 
             for( String line : lines ) {
                 templateBuffer.append( line );
                 templateBuffer.append( "\n" );
             }
 
             template = templateBuffer.toString();
 
         } finally {
             IOUtils.closeQuietly( in );
         }
 
         // expand params for usage in templates
         if( params == null ) {
         	params = new HashMap<String, Object>();
         }
         
         params.putAll( getDefaultVelocityParams() );
         
         // evaluate and write to target file
         VelocityContext context = new VelocityContext( params );
         StringWriter sw = new StringWriter();
 
         velocityEngine.evaluate( context, sw, "LOG", template );
 
         String fileContent = sw.toString();
 
         FileUtils.writeStringToFile( targetFile, fileContent );
 
         getLog().info( "Datei wurde erstellt: " + targetFile.getName() );
 
     }
     
     private Map<String, Object> getDefaultVelocityParams() {
     	
     	Map<String, Object> params = new HashMap<String, Object>();
     	
     	String createDateStr = DateUtils.formatDate( database.getCreateDate() );
     	
     	params.put( "database", database );
     	params.put( "createDateStr", createDateStr );
     	params.put( "textUtils", textUtils );
     	
     	return params;
     }
     
    protected Database getDatebase() {
     	return database;
     }
     
     protected abstract ResourceLocator getResourceLocator();
 
     protected abstract File getOutputDirectory();
     
 }
