 import java.util.*;
 import java.util.jar.*;
 import java.net.*;
 import java.lang.reflect.*;
 
 public class RunJar {
 	
 	public static void main(String[] args) {
 		try {
 			String jarfile = args[0];
 			args = java.util.Arrays.copyOfRange(args, 1, args.length);
 			JarFile jar = new JarFile(jarfile);
 			Manifest mf = jar.getManifest();
 			String mainClass = mf.getMainAttributes().getValue("Main-Class");
 			URLClassLoader ucl = new URLClassLoader(
 					new URL[] { new URL("file:///" + jarfile) },
 					new Object().getClass().getClassLoader());
 			Class klass = ucl.loadClass(mainClass);
 			Method main = klass.getMethod("main", String[].class);
 			try {
 				main.invoke(null, new Object[] { args });
 			} catch (InvocationTargetException ex) {
				throw (Exception)ex.getCause();
 			}
		} catch (Exception ex) {
 			ex.printStackTrace();
 		}
 	}
 	
 }
