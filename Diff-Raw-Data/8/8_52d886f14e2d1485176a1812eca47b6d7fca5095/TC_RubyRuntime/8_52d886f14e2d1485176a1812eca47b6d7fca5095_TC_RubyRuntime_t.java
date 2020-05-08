 package org.rubypeople.rdt.internal.launching;
 
 import java.io.File;
 
 import org.eclipse.core.resources.IFolder;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IPath;
 import org.eclipse.core.runtime.Path;
 import org.rubypeople.rdt.core.tests.ModifyingResourceTest;
 import org.rubypeople.rdt.core.tests.util.Util;
 import org.rubypeople.rdt.launching.IVMInstall;
 import org.rubypeople.rdt.launching.IVMInstallType;
 import org.rubypeople.rdt.launching.RubyRuntime;
 import org.rubypeople.rdt.launching.VMStandin;
 
 public class TC_RubyRuntime extends ModifyingResourceTest {
 
 	private static final String VM_TYPE_ID = "org.rubypeople.rdt.launching.StandardVMType";
 
 	private IVMInstallType vmType;
 	private IFolder folderOne;
 	private IFolder folderTwo;
 	
 	public TC_RubyRuntime(String name) {
 		super(name);
 	}
 
 	@Override
 	protected void setUp() throws Exception {
 		super.setUp();
 		RubyRuntime.getVMInstallTypes();
 		vmType = RubyRuntime.getVMInstallType(VM_TYPE_ID);
 		RubyRuntime.setDefaultVMInstall(null, null, true);
 		IVMInstall[] installs = vmType.getVMInstalls();
 		for (int i = 0; i < installs.length; i++) {
 			vmType.disposeVMInstall(installs[i].getId());
 		}
 		LaunchingPlugin.getDefault().setIgnoreVMDefPropertyChangeEvents(true);
 		createProject("/rubyRuntime");
 		folderOne = createFolder("/rubyRuntime/interpreterOne");
 		createFolder("/rubyRuntime/interpreterOne/lib");
 		createFolder("/rubyRuntime/interpreterOne/bin");
 		createFile("/rubyRuntime/interpreterOne/bin/ruby", "");
 		folderTwo = createFolder("/rubyRuntime/interpreterTwo");
 		createFolder("/rubyRuntime/interpreterTwo/lib");
 		createFolder("/rubyRuntime/interpreterTwo/bin");
 		createFile("/rubyRuntime/interpreterTwo/bin/ruby", "");
 	}
 	
 	@Override
 	protected void tearDown() throws Exception {
 		RubyRuntime.setDefaultVMInstall(null, null, true);
 		IVMInstall[] installs = vmType.getVMInstalls();
 		for (int i = 0; i < installs.length; i++) {
 			vmType.disposeVMInstall(installs[i].getId());
 		}
 		vmType = null;
 		RubyRuntime.getPreferences().setValue(RubyRuntime.PREF_VM_XML, "");
 		deleteProject("/rubyRuntime");
 		super.tearDown();
 	}
 
 	public void testGetInstalledInterpreters() {
 		String vmOneName = "InterpreterOne";
 		String vmOneId = vmOneName;
 		String vmTwoName = "InterpreterTwo";
 		String vmTwoId = vmTwoName;
 		try {
 			VMStandin standin = new VMStandin(vmType, vmOneId);
 			standin.setInstallLocation(folderOne.getLocation().toFile());
 			standin.setName(vmOneName);
 			standin.convertToRealVM();
 
 			VMStandin standin2 = new VMStandin(vmType, vmTwoId);
 			standin2.setInstallLocation(folderTwo.getLocation().toFile());
 			standin2.setName(vmTwoName);
 			standin2.convertToRealVM();
 
 			IVMInstall[] installs = vmType.getVMInstalls();
 			assertEquals(2, installs.length);
 			assertEquals(vmOneName, installs[0].getName());
 			assertEquals(vmTwoName, installs[1].getName());
 		} finally {
 			vmType.disposeVMInstall(vmOneId);
 			vmType.disposeVMInstall(vmTwoId);
 		}
 	}
 
 	public void testSetInstalledInterpreters() throws CoreException {
 		String vmOneName = "InterpreterOne";
 		String vmOneId = vmOneName;
 		String vmTwoName = "InterpreterTwo";
 		String vmTwoId = vmTwoName;
 		try {
 			VMStandin standin = new VMStandin(vmType, vmOneId);
 			standin.setInstallLocation(folderOne.getLocation().toFile());
 			standin.setName(vmOneName);
 			RubyRuntime.getVMInstallTypes();
 			IVMInstall one = standin.convertToRealVM();
 			RubyRuntime.setDefaultVMInstall(one, null,true);
 			assertEquals(1, vmType.getVMInstalls().length);
 			IPath vmOneLocation = folderOne.getLocation();
 			assertEquals(
 					"XML should indicate only one interpreter with it being the one selected.",
 					Util.convertToIndependantLineDelimiter("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\r\n" +
 					"<vmSettings defaultVM=\"43,org.rubypeople.rdt.launching.StandardVMType14," + vmOneId + "\">\r\n" +
 					"<vmType id=\"org.rubypeople.rdt.launching.StandardVMType\">\r\n" +
 					vmToXML(vmOneId, vmOneName, vmOneLocation) +
 					"</vmType>\r\n" +
 					"</vmSettings>\r\n"),
 					Util.convertToIndependantLineDelimiter(getVMsXML()));			
 			IPath vmTwoLocation = folderTwo.getLocation();
 			VMStandin standin2 = new VMStandin(vmType, vmTwoId);
 			standin2.setInstallLocation(vmTwoLocation.toFile());
 			standin2.setName(vmTwoName);
 			IVMInstall two = standin2.convertToRealVM();
 			RubyRuntime.saveVMConfiguration();
 			assertEquals(2, vmType.getVMInstalls().length);
 			assertEquals(
 					"XML should indicate both interpreters with the first one being selected.",
 					Util.convertToIndependantLineDelimiter("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\r\n" +
 					"<vmSettings defaultVM=\"43,org.rubypeople.rdt.launching.StandardVMType14," + vmOneId + "\">\r\n" +
 					"<vmType id=\"org.rubypeople.rdt.launching.StandardVMType\">\r\n" +
 					vmToXML(vmOneId, vmOneName, vmOneLocation) +
 					vmToXML(vmTwoId, vmTwoName, vmTwoLocation) +
 					"</vmType>\r\n" +
 					"</vmSettings>\r\n"),
 					Util.convertToIndependantLineDelimiter(getVMsXML()));
 
 			RubyRuntime.setDefaultVMInstall(two, null,true);
 			assertEquals(2, vmType.getVMInstalls().length);
 			assertEquals(
 					"XML should indicate both interpreters with the second one being selected.",
 					Util.convertToIndependantLineDelimiter("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\r\n" +
 					"<vmSettings defaultVM=\"" + RubyRuntime.getCompositeIdFromVM(standin2) + "\">\r\n" +
 					"<vmType id=\"org.rubypeople.rdt.launching.StandardVMType\">\r\n" +
 					vmToXML(vmOneId, vmOneName, vmOneLocation) +
 					vmToXML(vmTwoId, vmTwoName, vmTwoLocation) +
 					"</vmType>\r\n" +
 					"</vmSettings>\r\n"),
 					Util.convertToIndependantLineDelimiter(getVMsXML()));
 		} finally {
 			vmType.disposeVMInstall(vmOneId);
 			vmType.disposeVMInstall(vmTwoId);
 		}
 	}
 
 	private String getVMsXML() {
 		return RubyRuntime.getPreferences().getString(RubyRuntime.PREF_VM_XML);
 	}
 	
 	private String vmToXML(String id, String name, IPath location) {
 		StringBuffer xml = new StringBuffer();
 		xml.append("<vm id=\"");
 		xml.append(id);
 		xml.append("\" name=\"");
 		xml.append(name);
 		xml.append("\" path=\"");
 		xml.append(location.toOSString());
		xml.append("\" vmargs=\"-e STDOUT.sync=true -e STDERR.sync=true -e load(ARGV.shift)\">\r\n");
 		xml.append("<libraryLocations>\r\n");
 		xml.append("<libraryLocation src=\"");
 		xml.append(location.toPortableString());
 		xml.append("/lib/ruby/site_ruby/1.8\"/>\r\n");
 		xml.append("<libraryLocation src=\"");
 		xml.append(location.toPortableString());
 		xml.append("/lib/ruby/1.8\"/>\r\n");
 		File file = LaunchingPlugin.getFileInPlugin(new Path("ruby" + File.separator + id + File.separator + "lib"));
 		xml.append("<libraryLocation src=\"");
 		xml.append(Path.fromOSString(file.toString()).toPortableString());
 		xml.append("\"/>\r\n");
 		xml.append("</libraryLocations>\r\n");
 		xml.append("</vm>\r\n");
 		return xml.toString();
 	}
 }
