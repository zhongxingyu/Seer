 package org.sonatype.plugins.yuicompressor;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.io.Reader;
 import java.util.List;
 
 import org.apache.maven.plugin.MojoExecutionException;
 import org.codehaus.plexus.util.IOUtil;
 import org.mozilla.javascript.Context;
 import org.mozilla.javascript.Function;
 import org.mozilla.javascript.NativeArray;
 import org.mozilla.javascript.Scriptable;
 import org.mozilla.javascript.ScriptableObject;
 import org.sonatype.plexus.build.incremental.BuildContext;
 
 /**
  * @goal jslint
  * @phase process-resources
  */
 public class JSLintMojo
     extends AbstractProcessSourcesMojo
 {
     public static final String[] DEFAULT_INCLUDES = { "**/*.js" };
 
     /**
      * @parameter default-value="${basedir}/src/main/js"
      */
     private File sourceDirectory;
 
     /**
      * @parameter default-value="true"
      */
     private boolean fail;
 
     @Override
     protected void processSources( List<File> sources )
         throws MojoExecutionException
     {
         for ( File source : sources )
         {
             Context cx = Context.enter();
             try
             {
                 processSource( source, cx );
             }
             catch ( IOException e )
             {
                 throw new MojoExecutionException( "Could not execute jslint on " + source, e );
             }
             finally
             {
                 Context.exit();
             }
         }
     }
 
     protected void processSource( File source, Context cx )
         throws IOException, MojoExecutionException
     {
         Scriptable scope = cx.initStandardObjects();
 
         Reader fr = new InputStreamReader( getClass().getResourceAsStream( "/jslint.js" ) );
         cx.evaluateReader( scope, fr, "jslint.js", 0, null );
 
         Function jslint = (Function) scope.get( "JSLINT", scope );
 
         Scriptable options = cx.newObject( scope );
         // options.put( "eqeq", options, true );
 
         Object[] jsargs = { loadSource( source ), options };
 
         boolean passed = (Boolean) jslint.call( cx, scope, scope, jsargs );
 
         if ( !passed )
         {
             NativeArray errors = (NativeArray) jslint.get( "errors", jslint );
 
             for ( int i = 0; i < errors.getLength(); i++ )
             {
                 Scriptable error = (Scriptable) errors.get( i, errors );
                 int line = ( (Number) ScriptableObject.getProperty( error, "line" ) ).intValue();
                 int column = ( (Number) ScriptableObject.getProperty( error, "character" ) ).intValue();
                 String reason = (String) ScriptableObject.getProperty( error, "reason" );
 
                 int severity = fail ? BuildContext.SEVERITY_ERROR : BuildContext.SEVERITY_WARNING;
                 buildContext.addMessage( source, line, column, reason, severity, null );
             }
 
             if ( fail && errors.getLength() > 0 )
             {
                 throw new MojoExecutionException( "There were jslint errors" );
             }
         }
     }
 
     private String loadSource( File source )
         throws IOException
     {
         InputStream is = new FileInputStream( source );
         try
         {
             return IOUtil.toString( is );
         }
         finally
         {
             is.close();
         }
     }
 
     @Override
     protected String[] getDefaultIncludes()
     {
         return DEFAULT_INCLUDES;
     }
 
     @Override
     protected File getSourceDirectory()
     {
         return sourceDirectory;
     }
 
 }
