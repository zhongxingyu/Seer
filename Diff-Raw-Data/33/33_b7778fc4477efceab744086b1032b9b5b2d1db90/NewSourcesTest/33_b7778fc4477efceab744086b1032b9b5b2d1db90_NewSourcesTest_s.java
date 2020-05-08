 package org.fedoraproject.eclipse.packager.tests;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileReader;
 import java.io.FileWriter;
 import java.io.IOException;
 
 import org.eclipse.core.resources.IResource;
 import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Path;
 import org.eclipse.swt.widgets.Shell;
 import org.eclipse.ui.PlatformUI;
 import org.fedoraproject.eclipse.packager.cvs.NewSourcesHandler;
 
 public class NewSourcesTest extends AbstractTest {
 	protected IResource resource;
 
 	protected void runHandler() throws Exception {
 		handler = new NewSourcesHandler();
 		handler.setDebug(true);
 		handler.setResource(resource);
 		Shell aShell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
 		handler.setShell(aShell);
 		handler.execute(null);		
 		handler.waitForJob();
 	}
 	
 	protected IResource makeFile(String name, Integer contents) throws IOException, CoreException {
		File newSource = new File(branch.getLocation().toOSString() + Path.SEPARATOR + name);
 		newSource.createNewFile();
 		if (contents != null) {
 			FileWriter out = new FileWriter(newSource);
 			out.write(contents);
 			out.close();
 		}
 		branch.refreshLocal(IResource.DEPTH_INFINITE, null);
 		IResource result = branch.findMember(name);
 		if (result == null) {
 			throw new IOException("Can't create test source");
 		}
 		return result;
 	}
 	
 	protected String readFile(String name) throws IOException {
 		String result = "";
 		File file = branch.findMember(name).getLocation().toFile();
 		BufferedReader br = new BufferedReader(new FileReader(file));
 		String line = br.readLine();
 		while (line != null) {
 			result += line + "\n";
 			line = br.readLine();
 		}
 		br.close();
 		return result.trim();
 	}
 
 	public void testEmpty() throws Exception {
 		String message = "REMOVEME is empty";
 		resource = makeFile("REMOVEME", null);
 		String oldSources = readFile("sources");
 		String oldCvsignore = readFile(".cvsignore");
 		runHandler();
 		assertEquals(message, handler.waitForJob().getMessage());
 		assertEquals(oldSources, readFile("sources"));
 		assertEquals(oldCvsignore, readFile(".cvsignore"));
 	}
 	
 	public void testNewFile() throws Exception {
 		String newLine = "fcd3dfe8777d16d64235bc7ae6bdcb8a  REMOVEME";
 		resource = makeFile("REMOVEME", 0x90);
 		runHandler();
 		assertEquals(newLine, readFile("sources"));
 		assertEquals("REMOVEME", readFile(".cvsignore"));
 	}
 	
 	public void testUpdate() throws Exception {
 		String newLine = "fcd3dfe8777d16d64235bc7ae6bdcb8a  REMOVEME";
 		resource = makeFile("REMOVEME", 0x99);
 		runHandler();
 		resource = makeFile("REMOVEME", 0x90);
 		runHandler();
 		assertEquals(newLine, readFile("sources"));
 		assertEquals("REMOVEME", readFile(".cvsignore"));
 	}
 
 }
