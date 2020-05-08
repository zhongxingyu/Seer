 package org.meta_environment.eclipse.files;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.io.InputStream;
 import java.net.MalformedURLException;
 import java.net.URL;
 
 import nl.cwi.sen1.configapi.types.Property;
 import nl.cwi.sen1.configapi.types.PropertyList;
 
 import org.eclipse.core.runtime.FileLocator;
 import org.eclipse.core.runtime.IPath;
 import org.eclipse.core.runtime.Path;
 import org.eclipse.imp.runtime.RuntimePlugin;
 import org.eclipse.imp.utils.StreamUtils;
 
 import toolbus.adapter.AbstractTool;
 import toolbus.adapter.eclipse.EclipseTool;
 import aterm.ATerm;
 import aterm.ATermList;
 import errorapi.types.ErrorList;
 import errorapi.types.SubjectList;
 import errorapi.types.subject.Subject;
 import errorapi.types.summary.Summary;
 
 public class IOJ extends EclipseTool {
 
 	private static class InstanceKeeper {
 		private static IOJ sInstance = new IOJ();
 		static {
 			sInstance.connect();
 		}
 	}
 
 	private errorapi.Factory errorFactory = errorapi.Factory.getInstance(factory);
 	private nl.cwi.sen1.configapi.Factory configFactory = nl.cwi.sen1.configapi.Factory.getInstance(factory);
 	
 	private IOJ() {
 		super("ioj");
 	}
 
 	static public IOJ getInstance() {
 		return InstanceKeeper.sInstance;
 	}
 
 	public ATerm readTextFile(String path) {
 		URL url = null;
 
 		try {
 			url = FileLocator.resolve(new URL(path));
 		} catch (Exception e) {
 			RuntimePlugin.getInstance().logException(
 					"Could resolve url " + path, e);
 		}
 
 		try {
 			InputStream fileContents = null;
 
 			if (url == null) {
 				/* TODO getFileContentsFromOS should be made obsolete */
 				fileContents = getFileContentsFromOS(path);
 			} else {
 				fileContents = url.openConnection().getInputStream();
 			}
 
 			String text = StreamUtils.readStreamContents(fileContents);
 			fileContents.close();
 			return factory.make("file-contents(<str>)", text);
 
 		} catch (Exception e) {
 			RuntimePlugin.getInstance().logException(
 					"Could not read file " + path, e);
 			return factory.make("failure(<term>)", getErrorSummary(
 					"Could not read file", path));
 		}
 	}
 
 	public ATerm getFilename(String Directory, String Name, String Extension) {
 		IPath path = new Path(Directory);
 		path = path.append(Name + Extension);
 		String fileName = path.toString();
 		return factory.make("filename(<str>)", fileName);
 	}
 
 	public ATerm getPathFilename(String Path) {
 		IPath path = new Path(Path);
 		String fileName = path.removeFileExtension().lastSegment();
 		if (fileName == null) {
 			fileName = "";
 		}
 		System.err.println("\ngetPathFilename for " + Path + " returned: "
 				+ fileName);
 		return factory.make("filename(<str>)", fileName);
 	}
 
 	public ATerm getPathDirectory(String Path) {
 		IPath path = new Path(Path);
 		int numberOfSegments = path.segmentCount();
 
 		String directory = "";
 		if (numberOfSegments > 1) {
 			directory = path.removeLastSegments(1).toString();
 		}
 		System.err.println("\ngetPathDirectory for " + Path + " returned: "
 				+ directory);
 		return factory.make("directory(<str>)", directory);
 	}
 
 	public ATerm getPathExtension(String Path) {
 		IPath path = new Path(Path);
 		String extension = path.getFileExtension();
 
 		if (extension == null) {
 			extension = "";
 		} else {
 			extension = "." + extension;
 		}
 
 		System.err.println("\ngetPathExtension for " + Path + " returned: "
 				+ extension);
 		return factory.make("extension(<str>)", extension);
 	}
 
 	public ATerm compareFiles(String fileName1, String fileName2) {
 		File file1 = new Path(fileName1).toFile();
 		File file2 = new Path(fileName2).toFile();
 
 		if (!file1.exists()) {
 			return factory.make("failure(<trm>)", getErrorSummary(
 					"File does not exist", file1.getPath()));
 		}
 		if (!file2.exists()) {
 			return factory.make("failure(<trm>)", getErrorSummary(
 					"File does not exist", file2.getPath()));
 		}
 
 		ATerm result = factory.make("different");
 		/*
 		 * TODO I'm not 100% sure that 'equals' is the appropriate way to
 		 * compare file handles.
 		 */
 		if (file1.equals(file2)) {
 			result = factory.make("equal");
 		}
 
 		return result;
 	}
 
 	public ATerm packTerm(ATerm term) {
 		return factory.make("term(<trm>)", AbstractTool.pack(term));
 	}
 
 	public ATerm unpackTerm(ATerm term) {
 		return factory.make("term(<trm>)", AbstractTool.unpack(term));
 	}
 
 	public ATerm findFile(ATermList directories, String fileName,
 			String extension) {
 		ATermList containingDirectories = factory.makeList();
 		PropertyList pl = configFactory.PropertyListFromTerm(directories);
 		
 		int l = pl.getLength();
 		for (int i = 0; i < l; i++) {
 			Property p = pl.getPropertyAt(i);
 			String dir = p.getPath();
 			String path = dir + "/" + fileName + extension;
 
 			try {
 				FileLocator.resolve(new URL(path));
 				containingDirectories = containingDirectories.append(factory
 						.make("<str>", dir));
 			} catch (MalformedURLException e) {
 				System.err.println("TODO: non-URL used as path: " + path);
 				RuntimePlugin.getInstance().logException(
 						"Malformed URL " + path, e);
 			} catch (IOException e) {
 				/*
 				 * TODO This exception occurs if the file could not be resolved.
 				 * Perhaps we need a better solution for this.
 				 */
 			}
 		}
 
 		if (containingDirectories.getLength() > 0) {
 			return factory.make("file-found(<list>)", containingDirectories);
		} else {
			return factory.make("file-not-found");
 		}
 	}
 
 	private InputStream getFileContentsFromOS(String path)
 			throws FileNotFoundException {
 		File f = new File(path);
 		FileInputStream fis = null;
 
 		fis = new FileInputStream(f);
 		System.err.println("\nTODO: legacy absolute OS path used: " + path);
 
 		return fis;
 	}
 
 	private ATerm getErrorSummary(String _description, String _subject) {
 		Subject subject = errorFactory.makeSubject_Subject(_subject);
 		SubjectList subjects = errorFactory.makeSubjectList(subject);
 		errorapi.types.error.Error error = errorFactory.makeError_Error(
 				_description, subjects);
 		ErrorList errors = errorFactory.makeErrorList(error);
 		Summary summary = errorFactory.makeSummary_Summary("IOJ tool", "ioj",
 				errors);
 		return summary.toTerm();
 	}
 
 }
