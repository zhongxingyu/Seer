 package org.monkeyscript.lite;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.InputStreamReader;
 import java.io.PrintStream;
 import java.io.IOException;
 import java.io.FileNotFoundException;
 import java.io.UnsupportedEncodingException;
 
 import org.mozilla.javascript.*;
 
 public class Global extends ImporterTopLevel {
 	
     public Global() {}
     public Global(Context cx) { init(cx); }
 	public void init(ContextFactory factory) {
 		factory.call(new ContextAction() {
 			public Object run(Context cx) {
 				init(cx);
 				return null;
 			}
 		});
 	}
 	
 	public void init(Context cx) {
 		boolean sealed = cx.isSealed();
 		initStandardObjects( cx, sealed );
 		defineProperty("global", this, ScriptableObject.DONTENUM);
 		NativeBlob.init( this, sealed );
 		//NativeBuffer.init( this, sealed );
 		String[] functions = {
 			"exec",
 			"print",
 		};
 		defineFunctionProperties(functions, Global.class, ScriptableObject.DONTENUM);
 	}
 	
 	public static Object print(Context cx, Scriptable thisObj, Object[] args, Function funObj) {
         PrintStream out = System.out;
         for (int i=0; i < args.length; i++) {
             if (i > 0)
                 out.print(" ");
 
             // Convert the arbitrary JavaScript value into a string form.
             String s = Context.toString(args[i]);
 
             out.print(s);
         }
         out.println();
         return Context.getUndefinedValue();
     }
     
 	/**
 	 * MonkeyScript exec();
 	 */
 	public static Object exec( Context cx, Scriptable thisObj, Object[] args, Function funObj ) {
 		if ( args.length == 0 )
 			throw ScriptRuntime.typeError("exec() called without name of script to execute");
 		try {
 			String scriptFileName = (String) args[0];
 			File scriptFile = new File(scriptFileName);
 			if ( scriptFile.canRead() ) {
 				FileInputStream is = new FileInputStream(scriptFile);
 				ScriptReader in = new ScriptReader(is);
				return cx.evaluateReader( thisObj, in, scriptFile.getAbsolutePath(), 0, null );
 			} else {
 				throw jsIOError("exec() called with name of a script that doesn't exist or cannot be read");
 			}
 		} catch( FileNotFoundException e ) {
 			throw jsIOError("exec() called with name of a script that doesn't exist");
 		} catch( UnsupportedEncodingException e ) {
 			throw jsIOError("Unsupported character encodign for exec(): " + e.getMessage());
 		} catch( IOException e ) {
 			throw jsIOError(e.getMessage());
 		}
 	}
 	
 	public static EcmaError jsIOError(String message) {
 		return ScriptRuntime.constructError("IOError", message);
 	}
 	
 }
