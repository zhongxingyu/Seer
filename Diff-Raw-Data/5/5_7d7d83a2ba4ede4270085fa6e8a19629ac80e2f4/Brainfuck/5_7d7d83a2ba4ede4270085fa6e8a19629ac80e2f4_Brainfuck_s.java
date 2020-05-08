 package bfrc;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.lang.reflect.Constructor;
 import java.util.Properties;
 
 import bfrc.ast.BlockNode;
 import bfrc.backend.Backend;
 import bfrc.lexer.Lexer;
 import bfrc.parser.Parser;
 
 public class Brainfuck {
 	public static void main(String[] args) throws IOException {
 		Properties props = new Properties();
 		String inFile = null;
 		String outFile = null;
 		
 		// load defaults
 		loadProperties(props, "default");
 		
 		// parse arguments
 		for (String arg : args) {
 			if (arg.startsWith("-")) {
 				String name = arg.substring(1);
				if (!loadProperties(props, name))
					System.err.println("could not find configuration: " + name);
 			} else {
 				if (inFile == null)
 					inFile = arg;
 				else if (outFile == null)
 					outFile = arg;
 				else
 					usage();
 			}
 		}
 		if (inFile == null)
 			usage();
 		if (outFile == null)
 			outFile = inFile;
 		
 		// construct instances
 		String lexerClass = props.getProperty("bfrc.lexer");
 		Lexer l = instantiate(Lexer.class, lexerClass, String.class, inFile);
 
 		String parserClass = props.getProperty("bfrc.parser");
 		Parser p = instantiate(Parser.class, parserClass);
 
 		String backendClass = props.getProperty("bfrc.backend");
 		Backend b = instantiate(Backend.class, backendClass, String.class, outFile);
 		
 		// do the compilation
 		BlockNode ast = p.parse(l);
 		b.write(ast);
 	}
 	
 	private static boolean loadProperties(Properties props, String fileName) throws IOException {
 		InputStream in = Brainfuck.class.getClassLoader()
 								.getResourceAsStream("bfrc/" + fileName + ".config");
 		if (in != null) {
 			props.load(in);
 			return true;
 		} else {
 			return false;
 		}
 	}
 	
 	private static <T> T instantiate(Class<T> type, String className) {
 		return instantiate(type, className, null, null);
 	}
 	
 	@SuppressWarnings("unchecked")
 	private static <T, P> T instantiate(Class<T> type, String className, Class<P> paramType, P param) {
 		if (className == null || className.trim().isEmpty())
 			throw new RuntimeException("obligatory instance '" + type.getSimpleName() +
 					"' not provided, use appropriate -config arguments");
 		
 		ClassLoader cl = Brainfuck.class.getClassLoader();
 		try {
 			// load class by name
 			Class<?> c = cl.loadClass(className);
 			// check that class is of requested type
 			if (!type.isAssignableFrom(c))
 				throw new IllegalArgumentException(className +
 						" is no suitable " + type.getSimpleName());
 			Object instance;
 			if (paramType == null) {
 				instance = c.newInstance();
 			} else {
 				Constructor<?> cons = c.getConstructor(paramType);
 				instance = cons.newInstance(param);
 			}
 			return (T) instance;
 		} catch (Exception e) {
 			throw new RuntimeException("could not create instance for " +
 					type.getSimpleName() + " '" + className + "'", e);
 		}
 	}
 	
 	private static void usage() {
 		System.err.println("usage: {-config} inputFile [outputFile]");
 		System.exit(1);
 	}
 }
