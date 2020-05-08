 package net.primitive.javascript.repl;
 
 import static net.primitive.javascript.interpreter.RuntimeContext.enterContext;
 import static net.primitive.javascript.interpreter.RuntimeContext.exitContext;
 
 import java.util.Map;
 
 import jline.Terminal;
 import jline.TerminalFactory;
 import jline.console.ConsoleReader;
 import net.primitive.javascript.core.PropertyDescriptor;
 import net.primitive.javascript.core.Scriptable;
 import net.primitive.javascript.core.ast.Program;
 import net.primitive.javascript.core.natives.JSObject;
 import net.primitive.javascript.core.parser.JavaScriptLexer;
 import net.primitive.javascript.core.parser.JavaScriptParser;
 import net.primitive.javascript.interpreter.ProgramVisitorImpl;
 import net.primitive.javascript.interpreter.RuntimeContext;
 
 import org.antlr.runtime.ANTLRStringStream;
 import org.antlr.runtime.CommonTokenStream;
 
 public class Main {
 
 	public static void main(String[] args) throws Exception {
 
 		Terminal terminal = TerminalFactory.create();
 		terminal.init();
 
 		ConsoleReader consoleReader = new ConsoleReader("newjs shell", System.in, System.out, terminal);
 		consoleReader.setPrompt("js> ");
 
 		consoleReader.println("ECMAScript 5 \"strict mode\". Use /? for mode details");
 
 		String line;
 		Scriptable globalObject = new JSObject();
 		net.primitive.javascript.core.jdk.Console.init(globalObject);
 		RuntimeContext currentContext = enterContext(globalObject);
 		while ((line = consoleReader.readLine()) != null) {
 
 			if ("/global".equals(line)) {
				for (Map.Entry<String, PropertyDescriptor> property : globalObject) {
					consoleReader.println(property.getKey() + "=>" + property.getValue());
				}
 				continue;
 			}
 
 			if ("/exit".equals(line)) {
 				break;
 			}
 
 			if ("/?".equals(line)) {
 				consoleReader.println("This is help for ECMAScript 5 \"strict mode\" shell");
 				consoleReader.println("/? - prints this help message");
 				consoleReader.println("/global - prints all global objects");
 				consoleReader.println("/exit - exits shell");
 				continue;
 			}
 
 			JavaScriptLexer lexer = new JavaScriptLexer(new ANTLRStringStream(line));
 
 			CommonTokenStream commonTokenStream = new CommonTokenStream(lexer);
 
 			JavaScriptParser javaScriptParser = new JavaScriptParser(commonTokenStream);
 			final Program program = javaScriptParser.program().result;
 
 			ProgramVisitorImpl visitor = new ProgramVisitorImpl(currentContext);
 
 			program.accept(visitor);
 		}
 
 		exitContext();
 		terminal.restore();
 
 	}
 
 }
