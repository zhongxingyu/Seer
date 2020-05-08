 package org.oobium.build.workspace;
 
 import static org.oobium.utils.FileUtils.copyJarEntry;
 import static org.oobium.utils.FileUtils.createJar;
 import static org.oobium.utils.FileUtils.extract;
 import static org.oobium.utils.FileUtils.writeFile;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.Set;
 import java.util.TreeSet;
 
 import org.oobium.build.BuildBundle;
 import org.oobium.logging.LogProvider;
 import org.oobium.logging.Logger;
 import org.oobium.utils.Config.Mode;
 import org.oobium.utils.FileUtils;
 
 public class ClientExporter {
 
 	private final Logger logger;
 	private final Workspace workspace;
 	private final Application application;
 	private final File exportDir;
 	private final File tmpDir;
 
 	private Project target;
 	private Mode mode;
 	private boolean clean;
 	private boolean includeSource;
 	
 	public ClientExporter(Workspace workspace, Application application) {
 		this.logger = LogProvider.getLogger(BuildBundle.class);
 		this.workspace = workspace;
 		this.application = application;
 		this.exportDir = workspace.getExportDir();
 		this.tmpDir = new File(exportDir, "tmp");
 		
 		this.mode = Mode.PROD;
 	}
 
 	public void setTarget(Project project) {
 		this.target = project;
 	}
 	
 	private String relativePath(File binFile, int binPathLength) {
 		String relativePath = binFile.getAbsolutePath().substring(binPathLength);
 		if('\\' == File.separatorChar) {
 			relativePath = relativePath.replace('\\', '/');
 		}
 		return relativePath;
 	}
 
 	private String relativeSrcPath(File srcFile, File binFile, int binPathLength) {
 		String relativePath = binFile.getParent().substring(binPathLength) + File.separator + srcFile.getName();
 		if('\\' == File.separatorChar) {
 			relativePath = relativePath.replace('\\', '/');
 		}
 		return relativePath;
 	}
 	
 	public void includeSource(boolean include) {
 		this.includeSource = include;
 	}
 	
 	/**
 	 * Export the application, configured for the given mode.
 	 * @return a File object for the exported jar.
 	 * @throws IOException
 	 */
 	public File[] export() throws IOException {
 		if(exportDir.exists()) {
 			if(clean) {
 				FileUtils.deleteContents(exportDir);
 			}
 		} else {
 			exportDir.mkdirs();
 		}
 		
 		if(tmpDir.exists()) {
 			FileUtils.deleteContents(tmpDir);
 		} else {
 			tmpDir.mkdirs();
 		}
 
 		try {
 			File targetDir;
 			if(target == null) {
 				targetDir = exportDir;
 			} else {
 				targetDir = new File(target.file, "oobium");
 			}
 
 			Map<String, File> files = new HashMap<String, File>();
 
 			// export core oobium jar
 			File oobiumJar = new File(targetDir, "org.oobium.android.jar");
 			if(oobiumJar.exists()) {
 				oobiumJar = null;
 			} else {
 				addFiles("org.oobium.client", files);
 				addFiles("org.oobium.http", "org.oobium.http.constants", files);
 				addFile("org.oobium.logging", "org.oobium.logging.Logger", files);
 				addFile("org.oobium.logging", "org.oobium.logging.LogProvider", files);
 				addAndroidLogger(files);
 				addFiles("org.oobium.persist", files);
 				addFiles("org.oobium.persist.http", files);
 				addFiles("org.oobium.utils", files);
 	
 				oobiumJar = createJar(oobiumJar, files);
 			}
 			
 			files = new HashMap<String, File>();
 
 			// export application jar
 			Set<Bundle> bundles = new TreeSet<Bundle>();
 			Set<Bundle> deps = application.getDependencies(workspace, mode);
 			bundles.addAll(deps);
 			bundles.add(application);
 			
 			int len = application.bin.getAbsolutePath().length() + 1;
 			for(File model : application.findModels()) {
 				File genModel = application.getGenModel(model);
 				File modelClass = application.getBinFile(model);
 				File genModelClass = application.getBinFile(genModel);
 				files.put(relativePath(modelClass, len), modelClass);
 				files.put(relativePath(genModelClass, len), genModelClass);
 				if(includeSource) {
 					files.put(relativeSrcPath(model, modelClass, len), model);
 					files.put(relativeSrcPath(genModel, genModelClass, len), genModel);
 				}
 			}
 	
 			String name = application.name + ".android.jar";
 			logger.info("creating client jar: " + name);
 			File applicationJar = createJar(targetDir, name, files);
 			if(target != null) {
 				target.addBuildPath("oobium/" + name, "lib");
 			}
 
 			if(oobiumJar == null) {
 				return new File[] { applicationJar };
 			} else {
 				if(target != null) {
 					target.addBuildPath("oobium/" + oobiumJar.getName(), "lib");
 				}
 				return new File[] { oobiumJar, applicationJar };
 			}
 		} finally {
 			FileUtils.delete(tmpDir);
 		}
 	}
 
 	private void addAndroidLogger(Map<String, File> files) {
 		File file = writeFile(tmpDir, "LoggerImpl.class", getClass().getResourceAsStream("LoggerImpl.class.android"));
 		files.put("org/oobium/logging/LoggerImpl.class", file);
 		if(includeSource) {
 			file = writeFile(tmpDir, "LoggerImpl.java", getClass().getResourceAsStream("LoggerImpl.java.android"));
 			files.put("org/oobium/logging/LoggerImpl.java", file);
 		}
 	}
 	
 	private void addFiles(String bundleName, Map<String, File> files) throws IOException {
 		Bundle bundle = workspace.getBundle(bundleName);
 		if(bundle.isJar) {
 			files.putAll(extract(bundle.file, new File(tmpDir, bundle.name)));
 		} else {
 			files.putAll(bundle.getBuildFiles(includeSource));
 		}
 	}
 
 	private void addFile(String bundleName, String className, Map<String, File> files) throws IOException {
 		Bundle bundle = workspace.getBundle(bundleName);
 		if(bundle.isJar) {
 			String entryName = className.replace('.', '/') + ".class";
 			String dstName = bundle.file.getName();
 			dstName = dstName.substring(0, dstName.lastIndexOf('.'));
			File dir = new File(tmpDir, dstName);
			if(!dir.isDirectory()) {
				dir.mkdirs();
			}
			File dst = copyJarEntry(bundle.file, entryName, dir);
 			files.put(entryName, dst);
 		} else {
 			int len = bundle.bin.getAbsolutePath().length() + 1;
 			File srcFile = new File(bundle.src, className.replace('.', File.separatorChar) + ".java");
 			File binFile = bundle.getBinFile(srcFile);
 			files.put(relativePath(binFile, len), binFile);
 			if(includeSource) {
 				files.put(relativeSrcPath(srcFile, binFile, len), srcFile);
 			}
 		}
 	}
 	
 	private void addFiles(String bundleName, String packageName, Map<String, File> files) throws IOException {
 		Bundle bundle = workspace.getBundle(bundleName);
 		if(bundle.isJar) {
 			String regex = packageName.replace('.', '/') + "/.+";
 			files.putAll(extract(bundle.file, new File(tmpDir, bundle.name), regex));
 		} else {
 			File pkg = new File(bundle.src, packageName.replace('.', File.separatorChar));
 			if(pkg.isDirectory()) {
 				int len = bundle.bin.getAbsolutePath().length() + 1;
 				for(File srcFile : pkg.listFiles()) {
 					File binFile = bundle.getBinFile(srcFile);
 					files.put(relativePath(binFile, len), binFile);
 					if(includeSource) {
 						files.put(relativeSrcPath(srcFile, binFile, len), srcFile);
 					}
 				}
 			}
 		}
 	}
 	
 }
