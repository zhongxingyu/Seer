 package org.zend.php.zendserver.deployment.ui.actions;
 
 import java.io.File;
 import java.io.IOException;
 import java.net.URL;
 import java.util.Enumeration;
 
 import org.eclipse.core.runtime.FileLocator;
 import org.eclipse.core.runtime.Path;
 import org.eclipse.core.runtime.Platform;
 import org.osgi.framework.Bundle;
 import org.osgi.framework.FrameworkUtil;
 import org.zend.sdklib.internal.target.ZendTargetDetectMain;
 
 import swt.elevate.ElevatedProgram;
 import swt.elevate.ElevatedProgramFactory;
 
 /**
  * Run ZendSDK zetect target command line
  */
 public class ZendDetectTargetCmdLine {
 
 	private String getJavaPath() {
		String javaExecName = Platform.OS_WIN32.equals(Platform.getOS()) ? "java.exe" : "java"; //$NON-NLS-1$ //$NON-NLS-2$
 		File java = new File(System.getProperty("java.home") + "/bin/" + javaExecName); //$NON-NLS-1$ //$NON-NLS-2$
 		return java.getAbsolutePath();
 	}
 	
 	private String getZendCommandArgs(String targetId, String key) throws IOException {
 		StringBuilder command = new StringBuilder();
 		
 		Bundle zendSdk = FrameworkUtil.getBundle(ZendTargetDetectMain.class);
 		String zendSdkPath = getZendSdkClassPath(zendSdk);
 		
 		if (Platform.OS_WIN32.equals(Platform.getOS())) {
 			String arch = Platform.getOSArch();
 			command.append(" -Djava.library.path="); //$NON-NLS-1$
 			command.append('"').append(new File(zendSdkPath, "/lib/" + arch)).append('"'); //$NON-NLS-1$
 		}
 		
 		command.append(" -cp "); //$NON-NLS-1$
 		
 		StringBuilder cp = new StringBuilder();
 		cp.append('"');
 		String rootPath = "/"; //$NON-NLS-1$
 		if (Platform.inDevelopmentMode()) {
 			rootPath = "/bin"; //$NON-NLS-1$
 		}
 		String pathSeparator = System.getProperty("path.separator"); //$NON-NLS-1$
 		cp.append(new File(zendSdkPath, rootPath)).append(pathSeparator);
 		
 		Enumeration<?> libs = zendSdk.findEntries("/lib", "*.jar", false); //$NON-NLS-1$ //$NON-NLS-2$
 		if (libs != null) {
 			while (libs.hasMoreElements()) {
 				URL lib = (URL) libs.nextElement();
 				cp.append(new File(zendSdkPath, lib.getPath())).append(pathSeparator);
 			}
 		}
 		cp.append('"');
 		command.append(cp);
 		
 		command.append(" ").append(ZendTargetDetectMain.class.getName()).append(" "); //$NON-NLS-1$ //$NON-NLS-2$
 		if (targetId != null) {
 			command.append(" ").append(targetId).append(" "); //$NON-NLS-1$ //$NON-NLS-2$
 		
 			if (key != null) {
 				command.append(" ").append(key).append(" "); //$NON-NLS-1$ //$NON-NLS-2$
 			}
 		}
 		
 		return command.toString();
 	}
 
 	private String getZendSdkClassPath(Bundle bundle) throws IOException {
 		URL url = FileLocator.find(bundle, new Path("/"), null); //$NON-NLS-1$
 		url = FileLocator.resolve(url);
 		File location = new File(url.getFile());
 		
 		return location.getAbsolutePath();
 	}
 	
 	public String getFullCommandLine(String targetId, String key) throws IOException {
 		String java = getJavaPath();
 		String args = getZendCommandArgs(targetId, key);
 		return java + args;
 	}
 	
 	public boolean runElevated(String targetId, String key) throws IOException {
 		ElevatedProgram prog = ElevatedProgramFactory.getElevatedProgram();
 		String java = getJavaPath();
 		String args = getZendCommandArgs(targetId, key);
 		
 		boolean result = prog.launch("Zend SDK", java, args); //$NON-NLS-1$
 		return result;
 	}
 
 }
