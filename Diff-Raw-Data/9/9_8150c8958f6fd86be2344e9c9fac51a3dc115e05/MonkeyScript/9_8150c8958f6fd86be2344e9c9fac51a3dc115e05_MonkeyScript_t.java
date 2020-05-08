 // Implement some things like exec
 package org.monkeyscript.lite;
 
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.net.URISyntaxException;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 
 import org.mozilla.javascript.Context;
 import org.mozilla.javascript.Script;
 import org.mozilla.javascript.Scriptable;
 import org.mozilla.javascript.ScriptableObject;
 
 public class MonkeyScript {
 	
 	protected static Global global;
 	
 	public static Global getGlobal() { return global; }
 	
 	public static void main(String args[]) {
 		global = new Global();
 		
 		try {
 			Context cx = Context.enter();
 			cx.setLanguageVersion(Context.VERSION_1_7);
 			boolean sealed = cx.isSealed();
 			
 			global.init(cx);
 			
 			Object[] array = new Object[args.length];
 			System.arraycopy(args, 0, array, 0, args.length);
 			Scriptable argsObj = cx.newArray(global, array);
 			global.defineProperty("_arguments", argsObj, ScriptableObject.DONTENUM);
 			
 			quickRunScript( cx, global, "wrench17.js" );
 			quickRunScript( cx, global, "monkeyscript.java.js" );
 			quickRunScript( cx, global, "monkeyscript.js" );
 			
 		} finally {
 			Context.exit();
 		}
 		
 	}
 	
 	protected static Object quickRunScript( Context cx, ScriptableObject scope, String fileName ) {
 		try {
 			InputStream is = MonkeyScript.class.getResourceAsStream( fileName );
 			InputStreamReader in = new InputStreamReader(is, "UTF-8");
			return cx.evaluateReader( scope, in, fileName, 1, null );
 		}
 		catch( IOException e ) { reportWarning("Failed to exec script "+fileName, e); }
 		catch( NullPointerException e ) { reportWarning("Failed to exec script "+fileName, e); }
 		return null;
 	}
 	
 	protected static void reportWarning(String message) {
 		Context.reportWarning(message);
 		System.out.println("[ WARNING: " + message + " ]");
 	}
 	
 	protected static void reportWarning(String message, Throwable t) {
 		Context.reportWarning(message, t);
 		System.out.println("[ WARNING: " + message + " ]");
 	}
 }
