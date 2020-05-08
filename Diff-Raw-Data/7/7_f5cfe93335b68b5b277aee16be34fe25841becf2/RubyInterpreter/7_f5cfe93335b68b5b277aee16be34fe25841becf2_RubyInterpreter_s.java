 package org.rubypeople.rdt.internal.launching;
 
 import java.io.File;
 import java.io.IOException;
 import java.text.MessageFormat;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.List;
 
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IPath;
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.core.runtime.Status;
 
 public class RubyInterpreter {
 	public final String endOfOptionsDelimeter = " -- ";
 
 	protected IPath installLocation;
 	protected String name;
 
 	public RubyInterpreter(String aName, IPath validInstallLocation) {
 		name = aName;
 		installLocation = validInstallLocation;
 	}
 
 	public IPath getInstallLocation() {
 		return installLocation;
 	}
 
 	public void setInstallLocation(IPath validInstallLocation) {
 		installLocation = validInstallLocation;
 	}
 	
 	public String getName() {
 		return name;
 	}
 	
 	public void setName(String newName) {
 		name = newName;
 	}
 	
 	public String getCommand() throws IllegalCommandException {
 		if( new File(installLocation.toOSString()).isFile() ){
 			return installLocation.toOSString();
 		}
 		String errorMessage = MessageFormat.format(RdtLaunchingMessages.getString("RdtLaunchingPlugin.interpreterNotFound"), new Object[] {this.getName()}) ;
 		throw new IllegalCommandException(errorMessage) ;
 	}
 	
	//public Process exec(String arguments, File workingDirectory) throws IOException, IllegalCommandException {
	//	return Runtime.getRuntime().exec(this.getCommand() + " " +  arguments, null, workingDirectory);
	//}
    
     public Process exec(String arguments, File workingDirectory) throws CoreException {
        List argList = Arrays.asList(arguments.split("\\s+"));
         return exec(argList, workingDirectory);
     }
 
 	
 	public Process exec(List args, File workingDirectory) throws CoreException {
 
 		try {
 			RdtLaunchingPlugin.debug("Launching: " + args) ;
 			RdtLaunchingPlugin.debug("Working Dir: " + workingDirectory) ;
             List rubyCmd = new ArrayList();
             rubyCmd.add(this.getCommand());
             rubyCmd.addAll(args);
 			return Runtime.getRuntime().exec((String[]) rubyCmd.toArray(new String[0]), null, workingDirectory);
 		} catch (IOException e) {
 			throw new RuntimeException("Unable to execute interpreter: " + args + workingDirectory);
 		}
 		catch (IllegalCommandException ex) {
 			IStatus errorStatus = new Status(IStatus.ERROR, RdtLaunchingPlugin.PLUGIN_ID, IStatus.OK, ex.getMessage(), null);
 			throw new CoreException(errorStatus) ;
 		}
 
 	}
 	
 	public boolean equals(Object other) {
 		if (other instanceof RubyInterpreter) {
 			RubyInterpreter otherInterpreter = (RubyInterpreter) other;
 			if (name.equals(otherInterpreter.getName()))
 				return installLocation.equals(otherInterpreter.getInstallLocation());
 		}
 		
 		return false;
 	}
 }
