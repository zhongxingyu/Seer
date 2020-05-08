 package org.spoofax.test;
 
 import java.io.IOException;
 
 import junit.framework.TestCase;
 
 //import org.spoofax.compiler.ConcreteInterpreter;
 import org.spoofax.interpreter.InterpreterException;
 
 public abstract class AbstractCompilerTest extends TestCase {
	private String path;
 //	private ConcreteInterpreter intp;
 	
 	public void setUp(String string) throws Exception {
 		super.setUp();
		path = string;
 //		intp = new ConcreteInterpreter();
 	}
 	
 	protected void exec(String file) throws IOException, InterpreterException {
 //		String[] strpath = { path, "data/trunk/stratego-libraries/lib/spec" };
 //		intp.load("data/libstratego-lib.ctree");
 //		intp.loadConcrete(path + "/" + file, strpath, false);
 //		intp.setCurrent(intp.getFactory().parseFromString("Tree(Test(\"a\", \"b\"),1)"));
 //		intp.invoke("main_0_0");
 	}
 }
