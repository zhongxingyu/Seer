 package org.dhappy.mimis;
 
 import java.util.logging.Logger;
 import java.util.logging.Level;
 
 import java.util.List;
 
 import java.io.BufferedReader;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.io.IOException;
 
 import javax.script.ScriptEngineManager;
 import javax.script.ScriptEngineFactory;
 import javax.script.ScriptEngine;
 import javax.script.ScriptException;
 
 import java.security.AccessController;
 import java.security.PrivilegedAction;
 
 import netscape.javascript.JSObject;
 
 import javax.swing.JApplet;
 
 public class ScriptRunnerApplet extends JApplet {
     private static Logger log =
 	Logger.getLogger( ScriptRunnerApplet.class.getName() );
 
     ScriptEngineManager engines = new ScriptEngineManager();
     ScriptEngine js = engines.getEngineByName( "javascript" );
 
     public void init() {
 	log.info( "Initialized: " + ScriptRunnerApplet.class.getName() );
     }
 
     public void start() {
         js.put( "hostApplet", this );
 
 	String script = this.getParameter( "script" );
 	if( script != null ) {
 	    eval( script );
 	}
     }
 
     public void eval( JSObject script ) {
 	log.info( "Eval script" );
     }
 
     public Object eval( String script ) {
 	InputStream stream = null;
 	
 	String line;
 	try {
 	    try {
 		stream = ScriptRunnerApplet.class.getResourceAsStream( script );
 		if( stream == null ) {
 		    log.log( Level.WARNING, "Could not get: " + script );
 		} else {
                     final BufferedReader reader = new BufferedReader( new InputStreamReader( stream ) );
 
                     return AccessController.doPrivileged( new PrivilegedAction() {
                             public Object run() {
                                 try {
                                     return js.eval( reader );
                                 } catch( ScriptException se ) {
                                     log.log( Level.WARNING, se.getMessage(), se );
                                 }
                                 return null;
                             }
                         } );
 		}
 	    } finally {
 		if( stream != null ) {
 		    stream.close();
 		}
 	    }
 	} catch( IOException ioe ) {
 	    log.warning( ioe.getMessage() );
 	}
         return null;
     }
 
     public Object guidClicked( final int guid ) {
         return AccessController.doPrivileged( new PrivilegedAction() {
                 public Object run() {
                     try {
                         String callback = "guidClicked(" + guid + ")";
                         log.info( "ScriptRunnerApplet/guidClicked: " + callback );
                         return js.eval( callback );
                     } catch( ScriptException se ) {
                        log.log( Level.WARNING, se.getMessage(), se );
                     }
                     return null;
                 }
             } );
     }
 
     public void stop() {
 	log.info( "Stop: " + ScriptRunnerApplet.class.getName() );
     }
 
     public void destroy() {
 	log.info( "Destroying: " + ScriptRunnerApplet.class.getName() );
     }
 
     public static void main( String[] args ) {
 	log.info( "Instantiated: " + ScriptRunnerApplet.class.getName() );
     }
 }
