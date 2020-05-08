 package org.rubypeople.rdt.refactoring.core.renameclass;
 
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.Map;
 
import org.eclipse.core.internal.resources.File;
 import org.rubypeople.rdt.refactoring.editprovider.FileNameChangeProvider;
 
 public class RenameClassFileNameChangeProvider extends FileNameChangeProvider {
 
 	private final RenameClassConfig config;
 
 	public RenameClassFileNameChangeProvider(RenameClassConfig renameClassConfig) {
 		this.config = renameClassConfig;
 	}
 
 	@Override
	public Map<String, String> getFilesToRename(Collection<File> objects) {
 		HashMap<String, String> filesToRename = new HashMap<String, String>();
		for (File file : objects) {
 			String name = file.getName();
 			name = name.replaceAll("\\." + file.getFileExtension() + "$", "");
 			if(name.equals(config.getSelectedNode().getCPath().getName())) {
 				filesToRename.put(file.getFullPath().toString(), config.getNewName() + ".rb");
 			}
 		}
 		return filesToRename;
 	}
 
 }
