 package cz.cuni.mff.d3s.adapt.bookstore.agent;
 
 import java.lang.instrument.ClassFileTransformer;
 import java.lang.instrument.IllegalClassFormatException;
 import java.net.URL;
 import java.security.ProtectionDomain;
 
 import ch.usi.dag.disl.DiSL;
 
 public class ClassTransformer implements ClassFileTransformer {
 	private DiSL disl;
 	private boolean actuallyTransform = false;
 
 	public ClassTransformer(Class<?> snippetClass) {
 		String snippetClassResource = getClassResourceUrl(snippetClass);
 		try {
 			if (InstrumentationAgent.DEBUG) {
 				System.err.printf(
 						"DiSL will instrument with following snippet:\n"
 								+ "  class: %s\n  resource: %s\n",
 						snippetClass.getName(), snippetClassResource);
 			}
 
 			System.setProperty("disl.classes", snippetClassResource);
 
 			disl = new DiSL(false);
 		} catch (Throwable e) {
 			System.err.printf("DiSL initialization failed.\n");
 			e.printStackTrace();
 			System.exit(1);
 		}
 	}
 
 	private String getClassResourceUrl(Class<?> cls) {
 		String name = cls.getName();
 		String filename = name.replace('.', '/') + ".class";
 		URL resource = cls.getClassLoader().getResource(filename);
 		return resource.toString();
 	}
 
 	public void enableTransformation() {
 		actuallyTransform = true;
 	}
 
 	@Override
 	public byte[] transform(ClassLoader loader, String classname,
 			Class<?> theClass, ProtectionDomain domain, byte[] bytecode)
 			throws IllegalClassFormatException {
 		try {
 			if (actuallyTransform) {
				boolean isBuiltIn = classname.startsWith("java.") || classname.startsWith("javax.") || classname.startsWith("sun.");
 				
 				if (!isBuiltIn && InstrumentationAgent.DEBUG) {
 						System.err.printf("ClassTransformer.transform(%s)\n",
 								classname);
 				}
 				
				if (!classname.startsWith("cz.")) {
 					return bytecode;
 				}
 			
 				return disl.instrument(bytecode);
 			} else {
 				InstrumentationAgent.registerClassLoader(loader);
 				return bytecode;
 			}
 		} catch (Throwable e) {
 			System.err.printf("DiSL instrumentation failed:\n");
 			e.printStackTrace();
 			return bytecode;
 		}
 	}
 
 }
