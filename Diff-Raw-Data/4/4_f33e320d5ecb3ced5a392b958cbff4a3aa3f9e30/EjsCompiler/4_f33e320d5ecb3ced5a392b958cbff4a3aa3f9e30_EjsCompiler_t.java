 package net.rcode.assetserver.ejs;
 
 import java.io.File;
 import java.io.IOException;
 import java.io.Reader;
 
 import net.rcode.assetserver.ejs.EjsParser.LocationInfo;
 import net.rcode.assetserver.util.IOUtil;
 
 import org.mozilla.javascript.Context;
 import org.mozilla.javascript.Function;
 import org.mozilla.javascript.Scriptable;
 import org.mozilla.javascript.ScriptableObject;
 
 /**
  * Compiles an EJS source file into a Rhino script for execution.
  * @author stella
  *
  */
 public class EjsCompiler {
 	private EjsRuntime runtime;
 	
 	/**
 	 * The shared template generator.  Has signature function(fragments) and
 	 * iterates over the fragments, evaluating or outputting each
 	 */
 	private Function evaluatorFunction;
 	
 	public EjsCompiler(EjsRuntime runtime) {
 		this.runtime=runtime;
 		
 		CharSequence evaluatorDefn=IOUtil.slurpResource(EjsCompiler.class, "ejsevaluator.js");
 		Context cx=runtime.enter();
 		try {
 			this.evaluatorFunction=cx.compileFunction(runtime.getSharedScope(), evaluatorDefn.toString(), 
 					"ejsevaluator.js", 1, null);
 		} finally {
 			runtime.exit();
 		}
 	}
 	
 	public EjsRuntime getRuntime() {
 		return runtime;
 	}
 	
 	/**
 	 * Convenience method to compile a resource
 	 * @param scope
 	 * @param relativeTo
 	 * @param resourceName
 	 * @return template function
 	 */
 	public Function compileTemplate(Scriptable scope, Class<?> relativeTo, String resourceName) {
 		return compileTemplate(scope, IOUtil.slurpResource(relativeTo, resourceName), resourceName);
 	}
 	
 	/**
 	 * Convenience to compile from a Reader
 	 * @param scope
 	 * @param in
 	 * @param sourceName
 	 * @return
 	 * @throws IOException
 	 */
 	public Function compileTemplate(Scriptable scope, Reader in, String sourceName) throws IOException {
 		CharSequence source=IOUtil.slurpReader(in);
 		return compileTemplate(scope, source, sourceName);
 	}
 	
 	/**
 	 * Convenience to compile a file.
 	 * @param scope
 	 * @param file
 	 * @return Function
 	 * @throws IOException 
 	 */
 	public Function compileTemplate(Scriptable scope, File file, String encoding) throws IOException {
 		CharSequence source=IOUtil.slurpFile(file, encoding);
 		return compileTemplate(scope, source, file.toString());
 	}
 	
 	/**
 	 * Compile the template to a Function that when invoked produces the template
 	 * output.  The returned function has the following signature:
 	 * <pre>
 	 * 	function(write)
 	 * </pre>
 	 * The write argument is a function that must take a single String coercible
 	 * argument and output it to its destination.
 	 * 
 	 * @param source
 	 * @param sourceName
 	 * @return Function that generates contents
 	 */
 	public Function compileTemplate(final Scriptable scope, CharSequence source, final String sourceName) {
 		final Context cx=runtime.enter();
 		try {
 			// Create the fragments array
 			final Scriptable fragments=cx.newArray(scope, 0);
 			
 			EjsParser.Events events=new EjsParser.Events() {
 				int fragmentIndex=0;
 				
 				public void handleLiteral(CharSequence text, LocationInfo location) {
 					int index=fragmentIndex++;
 					ScriptableObject.putProperty(fragments, index, text.toString());
 				}
 				public void handleInterpolation(CharSequence script, LocationInfo location) {
 					// Compile the generator function
 					StringBuilder interpDefn=new StringBuilder(script.length()+50);
					interpDefn.append("function(){var expr=")
						.append(script).append(";return (expr===null||expr===undefined) ? null : String(expr);}");
 					Function interpFunction=cx.compileFunction(scope, interpDefn.toString(), 
 							sourceName, location.getLineStart(), null);
 					int index=fragmentIndex++;
 					ScriptableObject.putProperty(fragments, index, interpFunction);
 				}
 				public void handleBlock(CharSequence script, LocationInfo location) {
 					StringBuilder blockDefn=new StringBuilder(script.length()+50);
 					blockDefn.append("function(){").append(script)
 						.append("; return '';}");
 					Function blockFunction=cx.compileFunction(scope, blockDefn.toString(), 
 							sourceName, location.getLineStart(), null);
 					int index=fragmentIndex++;
 					ScriptableObject.putProperty(fragments, index, blockFunction);
 				}
 			};
 			
 			EjsParser parser=new EjsParser(events);
 			parser.parse(source);
 			
 			// Call the evaluator generator with the fragments to get the generator
 			return (Function) evaluatorFunction.call(cx, scope, null, new Object[] { fragments });
 		} finally {
 			runtime.exit();
 		}
 	}
 }
