 package pt.utl.ist.fenix.tools.file;
 
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.lang.reflect.Method;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Properties;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import pt.utl.ist.fenix.tools.file.filters.FileSetFilter;
 import pt.utl.ist.fenix.tools.file.filters.FileSetFilterException;
 import pt.utl.ist.fenix.tools.file.utils.FileUtils;
 import pt.utl.ist.fenix.tools.tree.TreeUtilities;
 import pt.utl.ist.fenix.tools.tree.TreeUtilities.TreeRecurseException;
 import pt.utl.ist.fenix.tools.util.PropertiesManager;
 import pt.utl.ist.fenix.tools.util.StringNormalizer;
 
 public abstract class AbstractFileManager implements IFileManager {
 
 	private static Logger logger = Logger.getLogger(AbstractFileManager.class.getName());
 
 	private Properties properties = null;
 
 	public AbstractFileManager() {
 		try {
 			logger.log(Level.INFO, "Loding properties from file /FileManagerConfiguration.properties");
 			properties = PropertiesManager.loadProperties("/FileManagerConfiguration.properties");
 		} catch (IOException e) {
 			throw new RuntimeException("Unable to read FileManager configuration properties", e);
 		}
 	}
 
 	public String getProperty(String propKey) {
 		return properties.getProperty(propKey);
 	}
 
 	/**
 	 * This method will rec-descend the FileSet passed in and invoke the method
 	 * denoted by m, existing in this class (or extension) for each found
 	 * fileset... It keeps track of the path in the tree, to avoid infinite
 	 * loops
 	 * 
 	 * @param fileSet
 	 * @param m
 	 */
 	public void recurseFileSetCallMethod(FileSet fileSet, Method m) {
 		try {
 			TreeUtilities.createTreeUtilities().recurseTreeCallMethod(fileSet, "getChildSets", this, m);
 		} catch (TreeRecurseException e) {
 			throw new RuntimeException(e);
 		}
 	}
 
 	/**
 	 * This method will rec-descend the FileSetDescriptor passed in and invoke
 	 * the method denoted by m, existing in this class (or extension) for each
 	 * found filesetdescriptor... It keeps track of the path in the tree, to
 	 * avoid infinite loops
 	 * 
 	 * @param fileSetDescriptor
 	 * @param m
 	 */
 	public void recurseFileSetDescriptorCallMethod(FileSetDescriptor fileSetDescriptor, Method m) {
 		try {
 			TreeUtilities.createTreeUtilities().recurseTreeCallMethod(fileSetDescriptor, "getChildSets",
 					this, m);
 		} catch (TreeRecurseException e) {
 			throw new RuntimeException(e);
 		}
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see pt.utl.ist.fenix.tools.file.IFileManager#changeFilePermissions(java.lang.String,
 	 *      java.lang.Boolean)
 	 */
 	public void changeFilePermissions(String uniqueId, Boolean privateFile) throws FileManagerException {
 		FileSetDescriptor setDescriptor = new FileSetDescriptor();
 		FileDescriptor fileDescriptor = new FileDescriptor();
 		fileDescriptor.setUniqueId(uniqueId);
 		setDescriptor.addContentFileDescriptor(fileDescriptor);
 		setDescriptor = listAllDescriptorsFromRoot(setDescriptor);
 		if (setDescriptor != null)
 			changePermissions(setDescriptor, privateFile);
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see pt.utl.ist.fenix.tools.file.IFileManager#deleteFile(java.lang.String)
 	 */
 	public void deleteFile(String uniqueId) throws FileManagerException {
 		FileSetDescriptor setDescriptor = new FileSetDescriptor();
 		FileDescriptor fileDescriptor = new FileDescriptor();
 		fileDescriptor.setUniqueId(uniqueId);
 		setDescriptor.addContentFileDescriptor(fileDescriptor);
 		setDescriptor = listAllDescriptorsFromRoot(setDescriptor);
 		if (setDescriptor != null)
 			deleteFileSet(setDescriptor);
 	}
 
 	public void formatDownloadUrls(FileSetDescriptor fileSetDescriptor) {
 		try {
 			recurseFileSetDescriptorCallMethod(fileSetDescriptor, this.getClass().getMethod(
 					"formatDownloadUrlAtLevel", new Class[] { FileSetDescriptor.class }));
 		} catch (SecurityException e) {
 			throw new RuntimeException(e);
 		} catch (NoSuchMethodException e) {
 			throw new RuntimeException(e);
 		}
 	}
 
 	public void formatDownloadUrlAtLevel(FileSetDescriptor fileSetDescriptor) throws FileManagerException {
 		for (FileDescriptor desc : fileSetDescriptor.getAllFileDescriptors()) {
 			desc.setDirectDownloadUrl(formatDownloadUrl(desc.getUniqueId(), desc.getFilename()));
 		}
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see pt.utl.ist.fenix.tools.file.IFileManager#retrieveFile(java.lang.String)
 	 */
 	
 	protected Collection<FileSetMetaData> createMetaData(String author, String title) {
 		Collection<FileSetMetaData> metadata = new ArrayList<FileSetMetaData>();
 		metadata.add(FileSetMetaData.createAuthorMeta(author));
		metadata.add(FileSetMetaData.createTitleMeta(pt.utl.ist.fenix.tools.util.FileUtils.getFilenameOnly(title)));
 		return metadata;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see pt.utl.ist.fenix.tools.file.IFileManager#saveFile(pt.utl.ist.fenix.tools.file.FilePath,
 	 *      java.lang.String, boolean, pt.utl.ist.fenix.tools.file.FileMetadata,
 	 *      java.io.File)
 	 */
 	public FileDescriptor saveFile(VirtualPath filePath, String originalFilename, boolean privateFile,
 			String author, String title, File fileToSave) {
 		Collection<FileSetMetaData> metadata = createMetaData(author, title);
 		return saveFile(filePath, originalFilename, privateFile, metadata, fileToSave);
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see pt.utl.ist.fenix.tools.file.IFileManager#saveFile(pt.utl.ist.fenix.tools.file.FilePath,
 	 *      java.lang.String, boolean, pt.utl.ist.fenix.tools.file.FileMetadata,
 	 *      java.io.File)
 	 */
 
 	public FileDescriptor saveFile(VirtualPath filePath, String originalFilename, boolean privateFile,
 			Collection<FileSetMetaData> fileMetadata, File fileToSave) {
 		FileSet fs = new FileSet();
 		fs.addMetaInfo(fileMetadata);
 		fs.addContentFile(fileToSave);
 		originalFilename = StringNormalizer.normalizePreservingCapitalizedLetters(pt.utl.ist.fenix.tools.util.FileUtils.getFilenameOnly(originalFilename));
 		FileSetDescriptor fsDescriptor = saveFileSet(filePath, originalFilename, privateFile, fs,
 				FileSetType.SIMPLE);
 		return fsDescriptor.getContentFileDescriptor(0);
 	}
 
 	public FileDescriptor saveFile(VirtualPath filePath, String originalFilename, boolean privateFile,
 			Collection<FileSetMetaData> fileMetadata, InputStream fileInputStream) {
 		File dirTemp;
		originalFilename = StringNormalizer.normalizePreservingCapitalizedLetters(pt.utl.ist.fenix.tools.util.FileUtils.getFilenameOnly(originalFilename));
 		
 		try {
 			dirTemp = FileUtils.createTemporaryDir("filemanager_", "_temp_persisted_stream");
 			File outFile = new File(dirTemp, originalFilename);
 			FileOutputStream fOutStream = new FileOutputStream(outFile);
 			FileUtils.copyInputStreamToOutputStream(fileInputStream, fOutStream);
 			FileDescriptor descriptor = saveFile(filePath, originalFilename, privateFile, fileMetadata, outFile);
 			FileUtils.deleteDirectory(dirTemp);
 			return descriptor;
 		} catch (IOException e) {
 			throw new RuntimeException("Error occured saving file", e);
 		}
 	}
 
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see pt.utl.ist.fenix.tools.file.IFileManager#saveFile(pt.utl.ist.fenix.tools.file.FilePath,
 	 *      java.lang.String, boolean, pt.utl.ist.fenix.tools.file.FileMetadata,
 	 *      java.io.InputStream)
 	 */
 	public FileDescriptor saveFile(VirtualPath filePath, String originalFilename, boolean privateFile,
 			String author, String title, InputStream fileInputStream) {
 		return saveFile(filePath, originalFilename, privateFile, createMetaData(author, title),
 				fileInputStream);
 	}
 
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see pt.utl.ist.fenix.tools.file.IFileManager#saveFileSet(pt.utl.ist.fenix.tools.file.VirtualPath,
 	 *      java.lang.String, boolean, pt.utl.ist.fenix.tools.file.FileSet,
 	 *      pt.utl.ist.fenix.tools.file.FileSetType)
 	 */
 	public FileSetDescriptor saveFileSet(VirtualPath virtualPath, String originalFileName,
 			boolean privateFile, FileSet fileSet, FileSetType fileSetType) {
 
 		if (fileSetType != null) {
 			Collection<FileSetFilter> filterChain = fileSetType.getFileSetFilterChain();
 			for (FileSetFilter filter : filterChain)
 				try {
 					filter.handleFileSet(fileSet);
 				} catch (FileSetFilterException e) {
 					throw new RuntimeException("Unable to run FileSetFilter " + filter.getClass().getName(),
 							e);
 				}
 		}
 
 		return internalSaveFileSet(virtualPath, originalFileName, privateFile, fileSet, fileSetType);
 	}
 
 	public abstract FileSetDescriptor internalSaveFileSet(VirtualPath virtualPath, String originalFileName,
 			boolean privateFile, FileSet fileSet, FileSetType fileSetType);
 
 	public abstract FileSet readFileSet(FileSetDescriptor fileSetDescriptor);
 
 	public abstract void deleteFileSet(FileSetDescriptor fileSetDescriptor);
 
 	public abstract FileSetDescriptor listAllDescriptorsFromRoot(FileSetDescriptor descriptor);
 
 	public abstract FileSetDescriptor getRootDescriptor(FileSetDescriptor innerChildDescriptor);
 
 	public abstract void changePermissions(FileSetDescriptor descriptor, Boolean isPrivateFile);
 }
