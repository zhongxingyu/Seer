 /**
  * 
  */
 package org.opf_labs.arc_cd.cdrdao;
 
 import java.util.Arrays;
 import java.util.Collections;
 import java.util.List;
 
 import com.google.common.base.Preconditions;
 
 /**
  * Class encapsualtes the minimal details of a cdrdai executable,a path
  * to the executable file (tested in the factory methods), the version 
  * of cdrdao, and a string array holding the available cd devices. 
  * 
  * @author  <a href="mailto:carl@openplanetsfoundation.org">Carl Wilson</a>.</p>
  *          <a href="https://github.com/carlwilson">carlwilson AT github</a>.</p>
  * @version 0.1
  * 
  * Created 6 Oct 2013:22:43:32
  */
 
 final class CdrdaoExecutable {
 	/** Cdrdao Executable default instance */
 	public static final CdrdaoExecutable DEFAULT = new CdrdaoExecutable();
 	private final String command;
 	private final String version;
 	private final List<String> devices;
 	
 	private CdrdaoExecutable() {
		this("", "UNKOWN", Arrays.asList(new String[] {}));
 	}
 	
 	private CdrdaoExecutable(final String command, final String version, final List<String> devices) {
 		this.command = command;
 		this.version = version;
 		this.devices = Collections.unmodifiableList(devices);
 	}
 	
 	static CdrdaoExecutable getNewInstance(final String command, final String version, final List<String> devices) {
 		Preconditions.checkNotNull(command, "command == null");
 		Preconditions.checkNotNull(version, "version == null");
 		Preconditions.checkNotNull(devices, "devices == null");
 		Preconditions.checkArgument(!command.isEmpty(), "command.isEmpty()");
 		Preconditions.checkArgument(!version.isEmpty(), "version.isEmpty()");
 		return new CdrdaoExecutable(command, version, devices);
 	}
 	/**
 	 * @return command to invoke cdrdao
 	 */
 	public String getCommand() {
 		return this.command;
 	}
 
 	/**
 	 * @return the version of cdrdao
 	 */
 	public String getVersion() {
 		return this.version;
 	}
 
 	/**
 	 * @return the list of CD devices detected
 	 */
 	public List<String> getDevices() {
 		return this.devices;
 	}
 	
 	/**
 	 * @return the default CD device
 	 */
 	public String getDefaultDevice() {
 		return this.devices.get(0);
 	}
 }
