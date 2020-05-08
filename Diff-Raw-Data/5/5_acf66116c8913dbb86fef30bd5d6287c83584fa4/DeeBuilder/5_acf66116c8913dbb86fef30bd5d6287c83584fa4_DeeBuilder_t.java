 package mmrnmhrm.core.build;
 
 import static melnorme.miscutil.Assert.assertFail;
 
 import java.io.ByteArrayInputStream;
 import java.io.File;
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Map;
 
 import melnorme.miscutil.StringUtil;
 import mmrnmhrm.core.DeeCore;
 import mmrnmhrm.core.launch.DeeDmdInstallType;
 import mmrnmhrm.core.model.DeeModel;
 import mmrnmhrm.core.model.DeeNameRules;
 import mmrnmhrm.core.model.DeeProjectOptions;
 
 import org.eclipse.core.resources.IContainer;
 import org.eclipse.core.resources.IFile;
 import org.eclipse.core.resources.IFolder;
 import org.eclipse.core.resources.IProject;
 import org.eclipse.core.resources.IResource;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IPath;
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.dltk.core.IBuildpathEntry;
 import org.eclipse.dltk.core.IScriptProject;
import org.eclipse.dltk.core.environment.EnvironmentPathUtils;
 
 import dtool.Logg;
 
 public class DeeBuilder {
 	
 	public static String getDemoBuildCommands(IScriptProject deeProj,
 			DeeProjectOptions overlayOptions, IProgressMonitor monitor) {
 		DeeBuilder builder = new DeeBuilder();
 		try {
 			//builder.dontCollectModules = true;
 			builder.collectBuildUnits(deeProj, monitor);
 		} catch (CoreException e) {
 			DeeCore.log(e);
 			return "Cannot determine preview: " + e ;
 		}
 		//builder.buildModules = Collections.singletonList("<<files.d>>");
 		//DeeProjectOptions options = DeeModel.getDeeProjectInfo(deeProj);
 		
 		//buildCommands = buildCommands.replace("$DEEBUILDER.SRCMODULES", "#DEEBUILDER.SRCMODULES#");
 		String buildCommands = builder.postProcessBuildCommands(overlayOptions);
 		//buildCommands.replace("#DEEBUILDER.SRCMODULES#", "$DEEBUILDER.SRCMODULES");
 		return buildCommands;
 	}
 	
 	public static String getDefaultRebuildBuildFileData() {
 		return 
 		"-oq$DEEBUILDER.OUTPUTPATH\n"
 		+"-of$DEEBUILDER.OUTPUTEXE\n"
 		//+"$DEEBUILDER.EXTRAOPTS\n"
 		+"$DEEBUILDER.SRCLIBS.-I\n"
 		+"$DEEBUILDER.SRCFOLDERS.-I\n"
 		+"$DEEBUILDER.SRCMODULES\n";
 	}
 
 	
 	private boolean dontCollectModules;
 	
 	private List<String> libraryEntries;
 	private List<String> folderEntries;
 	private List<String> buildModules;
 	private IPath compilerPath;
 	private IPath standardLibPath;
 
 	public DeeBuilder() {
 		dontCollectModules = false;
 
 		buildModules = new ArrayList<String>();
 		libraryEntries = new ArrayList<String>();
 		folderEntries = new ArrayList<String>();
 	}
 	
 
 	private DeeProjectOptions getProjectOptions(IScriptProject deeProj) {
 		return DeeModel.getDeeProjectInfo(deeProj);
 	}
 
 
 	public void collectBuildUnits(IScriptProject deeProj, IProgressMonitor monitor) throws CoreException  {
 		
 		IBuildpathEntry[] buildpathEntries = deeProj.getResolvedBuildpath(true);
 
 		for (int i = 0; i < buildpathEntries.length; i++) {
 			IBuildpathEntry entry = buildpathEntries[i];
 			Logg.builder.println("Builder:: In entry: " + entry);
 
 			
 			if(entry.getEntryKind() == IBuildpathEntry.BPE_SOURCE) {
 				processSourceEntry(deeProj, entry, monitor);
 			} else if(entry.getEntryKind() == IBuildpathEntry.BPE_LIBRARY) {
 				processLibraryEntry(entry);
 			}
 		}
 		
 		if(compilerPath == null)
 			throw DeeCore.createCoreException("Could not find a D Compiler in the project path", null);
 	}
 
 	private void processLibraryEntry(IBuildpathEntry entry) throws CoreException {
 		
 		if(entry.getPath().matchingFirstSegments(IBuildpathEntry.BUILTIN_EXTERNAL_ENTRY) == 1) {
 			// XXX: This entry has the compiler path, but has a bug in which the path device is lost
 			//compilerPath = standardLibPath.removeLastSegments(2).append("bin");
 		} else if (DeeDmdInstallType.isStandardLibraryEntry(entry)) {
 			// FIXME: BUILDER: Support other kinds of install locations
 			standardLibPath = entry.getPath();
 			compilerPath = standardLibPath.removeLastSegments(2).append("bin");
 
 		} else if(!entry.isExternal()) {
 			IPath projectBasedPath = entry.getPath().removeFirstSegments(1);
 			libraryEntries.add(projectBasedPath.toOSString());
 		}
 	}
 
 	private void processSourceEntry(IScriptProject deeProj, IBuildpathEntry entry, IProgressMonitor monitor)
 			throws CoreException {
 		IProject project = deeProj.getProject();
 
 		if(entry.isExternal()) {
 			throw DeeCore.createCoreException("Unsupported external source entry" + entry, null);
 		}
 		
 		IPath projectBasedPath = entry.getPath().removeFirstSegments(1);
 		IContainer entryContainer = (IContainer) project.findMember(projectBasedPath);
 
 		
 		String containerPathStr = entryContainer.isLinked(IResource.CHECK_ANCESTORS) ?
 				entryContainer.getLocation().toOSString()
 				: projectBasedPath.toOSString();
 		
 		folderEntries.add(containerPathStr);
 		if(dontCollectModules)	
 			return;
 
 		if(entryContainer != null)
 			proccessSourceFolder(entryContainer, monitor);
 		
 	}
 
 	protected void proccessSourceFolder(IContainer container,
 			IProgressMonitor monitor) throws CoreException {
 		
 		IResource[] members = container.members(false);
 		for (int i = 0; i < members.length; i++) {
 			IResource resource = members[i];
 			if(resource.getType() == IResource.FOLDER) {
 				proccessSourceFolder((IFolder) resource, monitor);
 			} else if(resource.getType() == IResource.FILE) {
 				processResource((IFile) resource);
 			} else {
 				assertFail();
 			}
 		}
 	}
 
 	
 	protected void processResource(IFile file) {
 		String modUnitName = file.getName();
 		IPath projectRelativePath = file.getProjectRelativePath();
 		if(DeeNameRules.isValidCompilationUnitName(modUnitName)) {
 			String resourcePathStr = file.isLinked(IResource.CHECK_ANCESTORS) ?
 					file.getLocation().toOSString()
 					: projectRelativePath.toOSString();
 			
 			buildModules.add(resourcePathStr);
 			//addCompileBuildUnit(resource);
 		} else {
 		}
 		//String extName = projectRelativePath.getFileExtension();
 		//String modName = projectRelativePath.removeFileExtension().lastSegment();
 	}
 
 	protected void compileModules(IScriptProject deeProj) throws CoreException {
 		
 		DeeProjectOptions options = getProjectOptions(deeProj);
 		//IFolder outputFolder = options.getOutputFolder();
 		
 		String buildCommands = postProcessBuildCommands(options);
 
 		Logg.main.println("--------  Build Commands:  --------\n" + buildCommands);
 		DeeProjectBuilder.buildListener.clear(); // This will wait some ms due to race
 		DeeProjectBuilder.buildListener.println("--------  Build Commands:  --------\n" + buildCommands);
 
 		
 		IFile file = deeProj.getProject().getFile(options.getBuildFile());
 		
 		byte[] buf = buildCommands.getBytes();
 		InputStream is = new ByteArrayInputStream(buf);
 		if(file.exists() == false) {
 			file.create(is, false, null);
 		} else {
 			file.setContents(is, IResource.NONE, null);
 		}
 	}
 
 	private String postProcessBuildCommands(DeeProjectOptions options) {
 		StringBuilder strb = new StringBuilder(options.getBuildCommands());
 		
 		IPath outputPath = options.getOutputFolder().getProjectRelativePath();
 		String outputDir = outputPath.toOSString();
 		while(StringUtil.replace(strb, "$DEEBUILDER.OUTPUTPATH", outputDir));
 		
 		String outputExe = outputPath.append(options.getArtifactName()).toOSString();
 		while(StringUtil.replace(strb, "$DEEBUILDER.OUTPUTEXE", outputExe));
 		
 		
 /*		{
 		String optionsStr = "";
 		String[] extrasOpts = options.getBuildCommands().split("\r\n|\n");
 		for (String opt : extrasOpts) {
 			optionsStr += opt + "\n";
 		}
 		while(StringUtil.replace(strb, "$DEEBUILDER.EXTRAOPTS", optionsStr));
 		}
 */
 		{
 		String srcLibs = "";
 		for (String srcLib : libraryEntries) {
 			srcLibs += "-I" + srcLib + "\n";
 		}
 		while(StringUtil.replace(strb, "$DEEBUILDER.SRCLIBS.-I", srcLibs));
 		}
 		
 		{
 		String srcFolders = "";
 		for (String srcfolder : folderEntries) {
 			srcFolders += "-I" + srcfolder + "\n";
 		}
 		while(StringUtil.replace(strb, "$DEEBUILDER.SRCFOLDERS.-I", srcFolders));
 		}
 
 
 		{
 		String srcModules = "";
 		for (String srcModule : buildModules) {
 			srcModules += srcModule + "\n";
 		}
 		while(StringUtil.replace(strb, "$DEEBUILDER.SRCMODULES", srcModules));
 		}
 		
 		return strb.toString();
 	}
 
 	
 	public void runBuilder(IScriptProject deeProj, IProgressMonitor monitor)
 			throws CoreException {
 
 		DeeProjectOptions options = getProjectOptions(deeProj);
 		IPath workDir = deeProj.getProject().getLocation();
 
 		//String buildToolExePath = splitSpaces(options.compilerOptions.buildToolCmdLine);
 		//String[] cmdLine = { buildToolExePath, options.getBuilderCommandLine() };
 		
 		String[] cmdLine = options.getBuilderFullCommandLine();
 		
 		// Substitute vars in cmdLine
 		for (int i = 0; i < cmdLine.length; i++) {
			String localCompilerPath = EnvironmentPathUtils.getLocalPath(compilerPath).toOSString();
			cmdLine[i] = cmdLine[i].replace("$DEEBUILDER.COMPILERPATH", localCompilerPath);
 			//cmdLine[i] = cmdLine[i].replace("$DEEBUILDER.COMPILEREXEPATH", compilerPath.toOSString()); // TODO
 		}
 		
 		final ProcessBuilder builder = new ProcessBuilder(cmdLine);
 
 		// XXX: Note: Apperently this has no effect, the intended path is not used
 		addCompilerPathToBuilder(builder); 
 
 		if(cmdLine.toString().length() > 30000)
 			throw DeeCore.createCoreException(
 					"D Build: Error cannot build: cmd-line too big", null);
 
 		
 		builder.directory(workDir.toFile());
 		
 		try {
 			Process proc = builder.start();
 			ProcessUtil.waitForProcess(monitor, proc);
 			
 		} catch (IOException e) {
 			throw DeeCore.createCoreException("D Build: Error exec'ing.", e);
 		} catch (InterruptedException e) {
 			throw DeeCore.createCoreException("D Build: Interrupted.", e);
 		}
 	}
 
 	private void addCompilerPathToBuilder(final ProcessBuilder builder) {
 		if(compilerPath == null)
 			return;
 		Map<String, String> env = builder.environment();
 		String pathName = "PATH";
 		String pathStr = env.get(pathName);
 		if(pathStr == null) {
 			pathName = "Path";
 			pathStr = env.get(pathName);
 		}
 		if(pathStr == null) {
 			pathName = "path";
 			pathStr = env.get(pathName);
 		}
 		pathStr = compilerPath.toOSString() + File.pathSeparator + pathStr;
 		env.put(pathName, pathStr);
 	}
 	
 }
