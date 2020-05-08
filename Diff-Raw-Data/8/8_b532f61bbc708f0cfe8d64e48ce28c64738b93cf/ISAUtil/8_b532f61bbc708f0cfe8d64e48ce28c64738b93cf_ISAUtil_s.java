 package edu.berkeley.eduride.base_plugin.isafile;
 
 import java.io.File;
 import java.io.IOException;
 import java.net.URI;
 import java.util.ArrayList;
 import java.util.HashMap;
 
 import javax.xml.parsers.ParserConfigurationException;
 import javax.xml.parsers.SAXParser;
 import javax.xml.parsers.SAXParserFactory;
 
 import org.eclipse.core.resources.IFile;
 import org.eclipse.core.resources.IMarker;
 import org.eclipse.core.resources.IProject;
 import org.eclipse.core.resources.IResource;
 import org.eclipse.core.resources.IResourceVisitor;
 import org.eclipse.core.resources.IWorkspaceRoot;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.QualifiedName;
 import org.xml.sax.SAXException;
 import org.xml.sax.SAXParseException;
 
 import edu.berkeley.eduride.base_plugin.EduRideBase;
 import edu.berkeley.eduride.base_plugin.model.Activity;
 import edu.berkeley.eduride.base_plugin.model.Step;
 import edu.berkeley.eduride.base_plugin.util.Console;
 
 public class ISAUtil {
 	
 	
 	public static QualifiedName isa_key = new QualifiedName("edu.berkeley.eduride","isAssignment");
 	
 
 	public static void setAsISA(IProject proj) throws CoreException {
 		if (proj != null) {
 			proj.setSessionProperty(isa_key, new Boolean(true));
 		}
 	}
 	
 	
 	// only checks if the property has already been set, yo, not the contained files.
 	public static boolean isISAProject(IProject proj) throws CoreException {
 		Object val = proj.getSessionProperty(isa_key);
 		if (val != null) {
 			return ((Boolean)val).booleanValue();
 		} else {
 			return false;
 		}
 	}
 	
 	public static boolean isISAFile(IResource res) throws CoreException {
 		if (res.getType() == IResource.FILE) {
 			return (isISAFile((IFile) res));
 		} else {
 			return false;
 		}
 	}
 	
 	public static boolean isISAFile(IFile ifile) throws CoreException {
 		String extension = ifile.getFileExtension();
 		if (extension != null) {
 			if (extension.equalsIgnoreCase("isa")) {
 				return true;
 			}
 		}
 		return false;
 
 	}
 	
 	
 	
 	
 	/**
 	 * Determines whether a file is located within a project that contains an .isa file
 	 */
 	public static boolean withinISAProject(IFile file) {
 		boolean containsISA = false;
 		if (file == null) {
 			return false;
 		}
 
 		IProject proj = file.getProject();
 		try {
 			if (isISAProject(proj)) {
 				return true;
 			}
 			
 			// look at the files in the contained project, see if there
 			// is an ISA file there.
 			proj.accept(new IResourceVisitor() {
 				@Override
 				public boolean visit(IResource resource) throws CoreException {
 					if (isISAFile(resource)) {
 						setAsISA(resource.getProject());
 						return false;
 					} else {
 						return true;
 					}
 				}
 			});
 
 			if (isISAProject(proj)) {
 				containsISA = true;
 			}
 		} catch (CoreException e) {
 			Console.err(e);
 		} catch (NullPointerException e) {
 			Console.err(e);
 			// used to be called when run in a non-ISA project ?? still happens?
 		} catch (Exception e) {
 			// whatever
 		}
 
 		return containsISA;
 	}
 	
 	
 	// 1 to 1 relationship between Activities and isaFiles
 	// ?when a file gets reparsed, it replaces the associated activity?
 	
 	public static Activity parseISA(IFile ifile) {
 		ISAParseHandler handler;
 		try {
 			// project is that which contains the isa file
 			IProject iproj = ifile.getProject();
 			String projectName = iproj.getName();
 			//String projectName2 = isaIfile.getFullPath().segment(0);
 			URI uri = ifile.getLocationURI();
 			File file = new File(uri);
 
 
 			SAXParserFactory factory = SAXParserFactory.newInstance();
 			SAXParser saxParser = factory.newSAXParser();
 			handler = new ISAParseHandler();
 			handler.setProjectName(projectName);
 			handler.setIProject(iproj);
 			handler.setIsafile(ifile);
 			saxParser.parse(file, handler);
 
 			Activity act = handler.getActivity();
 	
 			act.setIsaFile(ifile);
 			act.setIProject(iproj);
 			Activity.recordActivity(ifile, act);
 
 			postProcess(act);
 			
 			return act;
 
 			// THESE EXCEPTIONS NEED TO WARN USER (ISA AUTHOR) SOMEHOW
 //		} catch (ISAFormatException e) {
 //			// TODO: ISA file has bad content
 //			return null; // ?
 		} catch (SAXParseException e) {
 			// problem in the XML somewhere
 			String msg = "Parse Exception: tag " + e.getPublicId()
 					+ ", column " + e.getColumnNumber();
 			createISAFormatProblemMarker(ifile, e.getLineNumber(), msg);
 			return null;
 		} catch (SAXException e) {
 			String msg = "SAX Exception: " + e.getMessage();
 			createISAFormatProblemMarker(ifile, 1, msg);
 			return null;
 		} catch (IOException e) {
 			String msg = "IO Exception while parsing ISA file: " + e.getMessage();
 			createISAFormatProblemMarker(ifile, 1, msg);
 			return null;
 		} catch (ParserConfigurationException e) {
 			String msg = "Parse Configuration Exception: " + e.getMessage();
 			createISAFormatProblemMarker(ifile, 1, msg);
 			return null;
 		}
 	}
 
 	
 	
 
 	private static void postProcess(Activity activity) {
 
 		ArrayList<Step> steps = activity.getSteps();
 		for (Step step : steps) {
 			try {
 				postProcess(step);				
 			} catch (ISAFormatException e) {
 				String msg = "ISA post process Step problem for " + step.getName() + ": " + e.getMessage();
 				createISAFormatProblemMarker(activity.getIsaFile(), e.lineNumber(), msg);
 			}
 		}
 	}
 	
 	
 	
 	
 	private static void postProcess(Step step) throws ISAFormatException {
 		
 		if (step.isCODE()) {
 			codeStepCreated(step);
 		}
 
 		if (step.isHTML()) {
 			htmlStepCreated(step);
 		}
 		
 	}
 	
 
 	
 	private static void codeStepCreated (Step step) throws ISAFormatException {
 		for (StepCreatedListener l : stepCreatedListeners) {
 			l.codeStepCreated(step);
 		}
 	}
 	
 	
 	private static void htmlStepCreated (Step step) throws ISAFormatException {
 		for (StepCreatedListener l : stepCreatedListeners) {
 			l.htmlStepCreated(step);
 		}
 	}
 	
 	
 	
 	/*
 	 * ISA Format problem Markers utilities
 	 */
 	
 	private static final String ISA_MARKER_ID = EduRideBase.PLUGIN_ID
 			+ ".ISAFormatProblem";
 
 	public static void createISAFormatProblemMarker(IFile isafile, int line,
 			String msg) {
 		try {
 			IMarker m = isafile.createMarker(ISA_MARKER_ID);
 			m.setAttribute(IMarker.MESSAGE, msg);
 			m.setAttribute(IMarker.LINE_NUMBER, line);
 		} catch (CoreException e) {
 			// eh.
 		}
		String filename;
		if (isafile == null) {
			filename = "???";
		} else {
			filename = isafile.toString();
		}
		Console.err("ISA Format Error: (file " + filename + ", line " + line + ") " + msg);
 	}
 	
 
 	
 	/*
 	 * Step created listeners
 	 */
 
 	
 	private static ArrayList<StepCreatedListener> stepCreatedListeners = new ArrayList<StepCreatedListener>();
 	
 	public static boolean registerStepCreatedListener(StepCreatedListener l) {
 		boolean result = stepCreatedListeners.add(l);
 		// TODO call listener on all existing steps ?
 		return (result);
 	}
 	
 	public static boolean removeStepCreatedListener(StepCreatedListener l) {
 		return (stepCreatedListeners.remove(l));
 	}
 
 
 
 	//TODO listen for new projects getting created -- imported from archive, for example
 
 	
 }
