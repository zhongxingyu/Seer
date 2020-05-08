 package lorian.graph;
 
 import java.lang.reflect.Method;
 import java.net.URL;
 import java.net.URLClassLoader;
 import java.util.ArrayList;
 import java.util.List;
 
 import javax.swing.JOptionPane;
 import javax.swing.UIManager;
 
 import lorian.graph.fileio.GraphFileWriter;
 import lorian.graph.function.Function;
 import lorian.graph.function.Function2Var;
 import lorian.graph.function.ParameterFunction;
 
 import org.eclipse.jdt.internal.jarinjarloader.*;
 import org.eclipse.swt.widgets.Display;
 
 public class GraphMain {
 	private static String sSwtVersion = "4.3";
 
 	public static void main(String[] args) throws Throwable {
 		System.out.println("Graph v" + GraphFunctionsFrame.version);
 		
 		boolean use_swing = false;
 		boolean load_libs = true;
		String forceLangName = null; 
 		for(int i = 0; i < args.length; i++)
 		{
 			String arg = args[i];
 			if(arg.equalsIgnoreCase("-swt"))
 			{
 				use_swing = false;
 			}
 			else if(arg.equalsIgnoreCase("-swing"))
 			{
 				use_swing = true;
 			}
 			else if(arg.equalsIgnoreCase("-language"))
 			{
 				if(i+1 < args.length)
 				{
					forceLangName = args[++i];
 				}
 			}
 			else if(arg.equalsIgnoreCase("-no-libs"))
 			{
 				load_libs = false;
 			}
 		}
 		
 		if(load_libs)
 		{
 			try {
 				ClassLoader cl = getClassloader();
 				Thread.currentThread().setContextClassLoader(cl);
 			} catch (JarinJarLoadFailed ex) {
 				String reason = ex.getMessage();
 				System.err.println("Launch failed: " + reason);
 				UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
 				JOptionPane.showMessageDialog(null, "Launch failed: " + reason, "Launching UI Failed", JOptionPane.ERROR_MESSAGE);
 				return;
 			}
 		}
 		else
 		{
 			System.out.println("Warning: Not loading any libraries.");
 		}
 		
 		if(use_swing)
 			GraphFunctionsFrame.funcframe = new GraphFunctionsFrame(false, false, false);
 		else
 		{
			new GraphSwtFrame(new Display(), forceLangName);
 		}
 	}
 
 	private static ClassLoader getClassloader() throws GraphMain.JarinJarLoadFailed {
 		String swtFileName = getSwtJarName();
 		String gluegenFileName = getGluegenJarName();
 		String joglFileName = getJoglJarName();
 
 		
 		try {
 			//URL[] allNativeJarsUrl = new URL[] { new URL("rsrc:" + swtFileName), new URL("rsrc:" + gluegenFileName), new URL("rsrc:" + joglFileName) };
 
 			URLClassLoader cl = (URLClassLoader) GraphMain.class.getClassLoader();
 			URL.setURLStreamHandlerFactory(new RsrcURLStreamHandlerFactory(cl));
 			Method addUrlMethod = URLClassLoader.class.getDeclaredMethod("addURL", new Class[] { URL.class });
 			addUrlMethod.setAccessible(true);
 
 			URL swtFileUrl = new URL("rsrc:" + swtFileName);
 			URL gluegenFileUrl = new URL("rsrc:" + gluegenFileName);
 			URL joglFileUrl = new URL("rsrc:" + joglFileName);
 			URL gluegenRtFileUrl = new URL("rsrc:gluegen-rt.jar");
 			URL joglAllFileUrl = new URL("rsrc:jogl-all.jar");
 			
 			
 			addUrlMethod.invoke(cl, new Object[] {  swtFileUrl });
 			addUrlMethod.invoke(cl, new Object[] {  gluegenRtFileUrl });
 			addUrlMethod.invoke(cl, new Object[] {  joglAllFileUrl });
 			addUrlMethod.invoke(cl, new Object[] {  gluegenFileUrl });
 			addUrlMethod.invoke(cl, new Object[] {  joglFileUrl });
 			
 			return cl;
 		} catch (Exception exx) {
 			throw new JarinJarLoadFailed(exx.getClass().getSimpleName() + ": " + exx.getMessage());
 		}
 	}
 
 	private static String getSwtJarName() throws GraphMain.JarinJarLoadFailed {
 		String osName = System.getProperty("os.name").toLowerCase();
 		String swtFileNameOsPart = (osName.contains("linux")) || (osName.contains("nix")) ? "linux" : osName.contains("mac") ? "osx" : osName.contains("win") ? "win" : "";
 
 		if ("".equals(swtFileNameOsPart)) {
 			throw new JarinJarLoadFailed("Unknown OS name: " + osName);
 		}
 		String swtFileNameArchPart = System.getProperty("os.arch").toLowerCase().contains("64") ? "64" : "32";
 		String swtFileName = "swt-" + swtFileNameOsPart + swtFileNameArchPart + "-" + sSwtVersion + ".jar";
 		System.out.printf("Loading %s...\n", swtFileName);
 		return swtFileName;
 	}
 
 	private static String getGluegenJarName() throws GraphMain.JarinJarLoadFailed {
 		String osName = System.getProperty("os.name").toLowerCase();
 		String gluegenFileNameOsPart = (osName.contains("linux")) || (osName.contains("nix")) ? "linux" : osName.contains("mac") ? "macosx" : osName.contains("win") ? "windows" : "";
 
 		if ("".equals(gluegenFileNameOsPart)) {
 			throw new JarinJarLoadFailed("Unknown OS name: " + osName);
 		}
 		String gluegenFileNameArchPart = System.getProperty("os.arch").toLowerCase().contains("64") ? "amd64" : "i586";
 		String gluegenFileName = "gluegen-rt-natives-" + gluegenFileNameOsPart + "-" + gluegenFileNameArchPart + ".jar";
 		System.out.printf("Loading %s...\n", gluegenFileName);
 		return gluegenFileName;
 	}
 
 	private static String getJoglJarName() throws GraphMain.JarinJarLoadFailed {
 		String osName = System.getProperty("os.name").toLowerCase();
 		String joglFileNameOsPart = (osName.contains("linux")) || (osName.contains("nix")) ? "linux" : osName.contains("mac") ? "macosx" : osName.contains("win") ? "windows" : "";
 
 		if ("".equals(joglFileNameOsPart)) {
 			throw new JarinJarLoadFailed("Unknown OS name: " + osName);
 		}
 		String joglFileNameArchPart = System.getProperty("os.arch").toLowerCase().contains("64") ? "amd64" : "i586";
 		String joglFileName = "jogl-all-natives-" + joglFileNameOsPart + "-" + joglFileNameArchPart + ".jar";
 		System.out.printf("Loading %s...\n", joglFileName);
 		return joglFileName;
 	}
 
 	private static class JarinJarLoadFailed extends Exception {
 		private static final long serialVersionUID = 1L;
 
 		private JarinJarLoadFailed(String message) {
 			super(message);
 		}
 	}
 }
