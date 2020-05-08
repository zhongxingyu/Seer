 package org.ow2.fractal.mind.ide.impl;
 
 import java.io.ByteArrayInputStream;
 import java.io.ByteArrayOutputStream;
 import java.io.File;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStreamWriter;
 import java.io.StringReader;
 import java.io.UnsupportedEncodingException;
 import java.rmi.activation.Activator;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.HashSet;
 import java.util.Set;
 
 import javax.xml.parsers.DocumentBuilder;
 import javax.xml.parsers.DocumentBuilderFactory;
 import javax.xml.parsers.ParserConfigurationException;
 
 import org.eclipse.core.resources.IFile;
 import org.eclipse.core.resources.IFolder;
 import org.eclipse.core.resources.IProject;
 import org.eclipse.core.resources.IResource;
 import org.eclipse.core.resources.ResourcesPlugin;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.core.runtime.Status;
 import org.eclipse.core.runtime.jobs.Job;
 import org.eclipse.emf.common.notify.Adapter;
 import org.eclipse.emf.common.notify.Notifier;
 import org.eclipse.emf.common.util.BasicEList;
 import org.eclipse.emf.common.util.ECollections;
 import org.eclipse.emf.common.util.EList;
 import org.eclipse.emf.ecore.util.EObjectContainmentWithInverseEList;
 import org.eclipse.emf.ecore.util.EObjectEList;
 import org.ow2.fractal.mind.ide.FamilyJobCST;
 import org.ow2.fractal.mind.ide.MindActivator;
 import org.ow2.fractal.mind.ide.MindIdeCore;
 import org.ow2.fractal.mind.ide.emf.mindide.MindAdl;
 import org.ow2.fractal.mind.ide.emf.mindide.MindFile;
 import org.ow2.fractal.mind.ide.emf.mindide.MindItf;
 import org.ow2.fractal.mind.ide.emf.mindide.MindObject;
 import org.ow2.fractal.mind.ide.emf.mindide.MindPackage;
 import org.ow2.fractal.mind.ide.emf.mindide.MindPathEntry;
 import org.ow2.fractal.mind.ide.emf.mindide.MindPathKind;
 import org.ow2.fractal.mind.ide.emf.mindide.MindProject;
 import org.ow2.fractal.mind.ide.emf.mindide.MindRootSrc;
 import org.ow2.fractal.mind.ide.emf.mindide.MindideFactory;
 import org.ow2.fractal.mind.ide.emf.mindide.MindidePackage;
 import org.w3c.dom.Element;
 import org.w3c.dom.Node;
 import org.w3c.dom.NodeList;
 import org.xml.sax.InputSource;
 import org.xml.sax.SAXException;
 
 public class MindProjectImpl extends org.ow2.fractal.mind.ide.emf.mindide.impl.MindProjectImpl {
 	private final class SaveMPEJob extends Job {
 		private final IProject p;
 
 		private SaveMPEJob(IProject p) {
 			super("Save mind path file for project "+p.getName());
 			this.p = p;
 		}
 
 		@Override
 		protected IStatus run(IProgressMonitor monitor) {
 				try {
 					writeFileEntries(p, getRawMinpath());
 				} catch (CoreException e) {
 					return new Status(Status.ERROR,MindActivator.ID,"Exception while write " + p.getFullPath(),e);
 					
 				} catch (IOException e) {
 					return new Status(Status.ERROR,MindActivator.ID,"Exception while write " + p.getFullPath(),e);
 				}
 				return Status.OK_STATUS;
 			}
 		@Override
 		public boolean belongsTo(Object family) {
 			return FamilyJobCST.FAMILY_ALL == family || FamilyJobCST.FAMILY_SAVE_MPE == family;
 		}
 	}
 
 	private static final class ChangeMindSRCVarJob extends Job {
 		private MindProjectImpl _mp;
 
 		private ChangeMindSRCVarJob(MindProjectImpl mp) {
 			super("Change the make file variable MIND_SRC for "+mp.getProject().getName());
 			_mp = mp;
 			setRule(mp.getProject());
 		}
 
 		@Override
 		protected IStatus run(IProgressMonitor monitor) {
 			try {
 				StringBuilder srcVar = new StringBuilder();
 				for (MindRootSrc rs : new ArrayList<MindRootSrc>(_mp._allsrc)) {
 					srcVar.append(toFile(rs));
 					srcVar.append(":");
 				}
 				if (srcVar.length() != 0)
 					srcVar.setLength(srcVar.length() - 1); //remove last collon if length > 0
 
 				MindMakefile mf = new MindMakefile(_mp.getProject());
 				mf.setVarAndSave(MindMakefile.MIND_SRC, srcVar.toString(), "all");
 			} catch (CoreException e) {
 				MindActivator.log( new Status(Status.ERROR, MindActivator.ID, getName(), e));
 			} catch (IOException e) {
 				MindActivator.log( new Status(Status.ERROR, MindActivator.ID, getName(), e));
 			}
 			return Status.OK_STATUS;
 		}
 
 		@Override
 		public boolean belongsTo(Object family) {
 			return FamilyJobCST.FAMILY_CHANGE_MAKEFILE_VAR_MIND_SRC == family || FamilyJobCST.FAMILY_ALL == family;
 		}
 		
 		private String toFile(MindRootSrc rs) {
 			IFolder f = MindIdeCore.getResource(rs);
			// for windows double \\
			return f.getLocation().toOSString().replaceAll("\\\\", "\\\\\\\\");
 		}
 	}
 	
 	private static final class ChangeMindCOMPVarJob extends Job {
 		
 		private MindProjectImpl _mp;
 
 		private ChangeMindCOMPVarJob(MindProjectImpl mp) {
 			super("Change the make file variable MIND_COMPONENTS for "+mp.getProject().getName());
 			_mp = mp;
 			setRule(mp.getProject());
 		}
 
 		@Override
 		protected IStatus run(IProgressMonitor monitor) {
 			try {
 				ArrayList<String> appli = new ArrayList<String>();
 				for (MindPathEntry mpe : _mp.getRawMinpath()) {
 					if (mpe.getEntryKind() == MindPathKind.APPLI) {
 						String mpename = mpe.getName();
 						appli.add(mpename);
 					}
 				}
 				StringBuilder srcVar = new StringBuilder();
 				for (String a : appli) {
 					srcVar.append(a);
 					srcVar.append("\\\n");
 				}
 				if (srcVar.length() != 0)
 					srcVar.setLength(srcVar.length() - 2); //remove last collon if length > 0
 				
 				MindMakefile mf = new MindMakefile(_mp.getProject());
 				mf.setVarAndSave(MindMakefile.MIND_COMPONENTS, srcVar.toString(), "all");
				MindIdeCore.rebuild(_mp);
 			} catch (CoreException e) {
 				MindActivator.log( new Status(Status.ERROR, MindActivator.ID, getName(), e));
 			} catch (IOException e) {
 				MindActivator.log( new Status(Status.ERROR, MindActivator.ID, getName(), e));
 			}
 			return Status.OK_STATUS;
 		}
 
 		
 
 		@Override
 		public boolean belongsTo(Object family) {
 			return FamilyJobCST.FAMILY_CHANGE_MAKEFILE_VAR_MIND_COMPONENT == family || FamilyJobCST.FAMILY_ALL == family;
 		}
 	}
 
 
 	public final static String UTF_8 = "UTF-8";	//$NON-NLS-1$
 	/**
 	 * Name of file containing project classpath
 	 */
 	public static final String MINDPATH_FILENAME = ".mindpath";  //$NON-NLS-1$
 	
 	/**
 	 * Value of the project's raw classpath if the .classpath file contains invalid entries.
 	 */
 	public static final EList<MindPathEntry> INVALID_MINDPATH = null;
 
 	
 	MindModelImpl _model;
 	private EList<MindRootSrc> _allsrc;
 	
 	public MindProjectImpl(IProject project, MindModelImpl model) {
 		this.project = project;
 		_model = model;
 	}
 
 	@Override
 	public void setMindpath(EList<MindPathEntry> mindpath) {
 		if (areMindpathsEqual(getMindpathentries(), mindpath)) return;
 		
 		getMindpathentries().clear();
 		getMindpathentries().addAll(mindpath);
 		
 		_model.syncMindPath(project, this, getRepo(), true);
 	}
 	
 	@Override
 	public EList<MindPathEntry> getRawMinpath() {
 		BasicEList<MindPathEntry> ret = new BasicEList<MindPathEntry>();
 		for (MindPathEntry mindPathEntry : new ArrayList<MindPathEntry>( getMindpathentries())) {
 			ret.add(new MindPathEntryCustomImpl(mindPathEntry));
 		}
 		return ret;
 	}
 	
 	@Override
 	public EList<MindPathEntry> getResolvedMindpath(
 			boolean ignoreUnresolvedEntry) {
 		return _model.getResolvedMindpath(this, ignoreUnresolvedEntry);
 	}
 	
 	@Override
 	public EList<MindProject> getUses() {
 		BasicEList<MindProject> ret = new BasicEList<MindProject>();
 		for (MindPathEntry mpe : mindpathentries) {
 			MindObject resolvedObject = mpe.getResolvedBy();
 			if (resolvedObject == null) continue;
 			MindProject  mp = null;
 			if (resolvedObject instanceof MindRootSrc) {
 				MindRootSrc s = (MindRootSrc) resolvedObject;
 				mp = s.getProject();
 			}else if (resolvedObject instanceof MindPackage) {
 				MindPackage mdPacakage = (MindPackage) resolvedObject;
 				MindRootSrc s = mdPacakage.getRootsrc();
 				if (s == null) continue;
 				mp = s.getProject();
 			}
 			else if (resolvedObject instanceof MindProject) {
 				mp = (MindProject) resolvedObject;
 			}
 			if (mp == null || ret.contains(mp)) continue;
 			ret.add(mp);
 		}
 		return ret;
 	}
 
 	@Override
 	public MindAdl resolveAdl(String componentName, String defaultPackage,
 			EList<String> imports) {
 		return (MindAdl) ResolveImpl.resolve(this, MindidePackage.Literals.MIND_ADL, componentName, defaultPackage, imports);
 	}
 	
 	@Override
 	public MindItf resolveIdl(String componentName, String defaultPackage,
 			EList<String> imports) {
 		return (MindItf) ResolveImpl.resolve(this, MindidePackage.Literals.MIND_ITF, componentName, defaultPackage, imports);
 	}
 	
 	@Override
 	public EList<MindAdl> resolvePossibleAdlInMindPath(String componentName) {
 		return ResolveImpl.resolve(this, MindidePackage.Literals.MIND_ADL, componentName);
 	}
 	
 	@Override
 	public EList<MindItf> resolvePossibleIdlInMindPath(String componentName) {
 		return ResolveImpl.resolve(this, MindidePackage.Literals.MIND_ITF, componentName);
 	}
 	
 	@Override
 	public EList<MindAdl> resolvePossibleAdlInPackage(String packageName) {
 		return ResolveImpl.resolveP(this, MindidePackage.Literals.MIND_ADL, packageName);
 	}
 	
 	@Override
 	public EList<MindItf> resolvePossibleIdlInPackage(String packageName) {
 		return ResolveImpl.resolveP(this, MindidePackage.Literals.MIND_ITF, packageName);
 	}
 	
 	@Override
 	public EList<MindAdl> resolvePossibleAdlInWorkspace(String componentName) {
 		return ResolveImpl.resolveInWorkspace(MindidePackage.Literals.MIND_ADL, componentName);
 	}
 	
 	@Override
 	public EList<MindItf> resolvePossibleIdlInWorkspace(String componentName) {
 		return ResolveImpl.resolveInWorkspace(MindidePackage.Literals.MIND_ITF, componentName);
 	}
 	
 	
 	@Override
 	public MindAdl findQualifiedComponent(String cn) {
 		return resolveAdl(cn, "", new BasicEList<String>());
 	}
 
 	/*
 	 * Reads and decode an XML classpath string
 	 */
 	public static EList<MindPathEntry> decodeMindpath(String xmlMindpath) throws IOException {
 		BasicEList<MindPathEntry> paths = new BasicEList<MindPathEntry>();
 		StringReader reader = new StringReader(xmlMindpath);
 		Element cpElement;
 		try {
 			DocumentBuilder parser = DocumentBuilderFactory.newInstance().newDocumentBuilder();
 			cpElement = parser.parse(new InputSource(reader)).getDocumentElement();
 		} catch (SAXException e) {
 			throw new IOException("Bad format");
 		} catch (ParserConfigurationException e) {
 			throw new IOException("Bad format");
 		} finally {
 			reader.close();
 		}
 
 		if (!cpElement.getNodeName().equalsIgnoreCase(MindPathEntryCustomImpl.TAG_MINDPATH)) { //$NON-NLS-1$
 			throw new IOException("Bad format");
 		}
 		NodeList list = cpElement.getElementsByTagName(MindPathEntryCustomImpl.TAG_MINDPATHENTRY); //$NON-NLS-1$
 		int length = list.getLength();
 
 		for (int i = 0; i < length; ++i) {
 			Node node = list.item(i);
 			if (node.getNodeType() == Node.ELEMENT_NODE) {
 				MindPathEntry entry = MindPathEntryCustomImpl.elementDecode((Element)node);
 				if (entry != null){
 					paths.add(entry);
 				}
 			}
 		}
 		return paths;
 	}
 	
 	/**
 	 * Returns the XML String encoding of the class path.
 	 */
 	protected static String encodeMindpath(EList<MindPathEntry> classpath, boolean indent) throws IOException {
 		ByteArrayOutputStream s = new ByteArrayOutputStream();
 		OutputStreamWriter writer = new OutputStreamWriter(s, "UTF8"); //$NON-NLS-1$
 		XMLWriter xmlWriter = new XMLWriter(writer, true/*print XML version*/);
 
 		xmlWriter.startTag(MindPathEntryCustomImpl.TAG_MINDPATH, indent);
 		for (int i = 0; i < classpath.size(); ++i) {
 			MindPathEntryCustomImpl.elementEncode(classpath.get(i), xmlWriter, indent, true);
 		}
 		xmlWriter.endTag(MindPathEntryCustomImpl.TAG_MINDPATH, indent, true/*insert new line*/);
 		writer.flush();
 		writer.close();
 		return s.toString("UTF8");//$NON-NLS-1$
 		
 	}
 	
 	/**
 	 * Record a shared persistent property onto a project.
 	 * Note that it is orthogonal to IResource persistent properties, and client code has to decide
 	 * which form of storage to use appropriately. Shared properties produce real resource files which
 	 * can be shared through a VCM onto a server. Persistent properties are not shareable.
 	 *
 	 * shared properties end up in resource files, and thus cannot be modified during
 	 * delta notifications (a CoreException would then be thrown).
 	 *
 	 * @param key String
 	 * @param value String
 	 * @see JavaProject#getSharedProperty(String key)
 	 * @throws CoreException
 	 */
 	public static void setSharedProperty(IProject p, String key, String value) throws CoreException {
 
 		IFile rscFile = p.getFile(key);
 		byte[] bytes = null;
 		try {
 			bytes = value.getBytes(UTF_8); // .classpath always encoded with UTF-8
 		} catch (UnsupportedEncodingException e) {
 			MindIdeCore.log(e, "Could not write "+key+" with UTF-8 encoding "); //$NON-NLS-1$
 			// fallback to default
 			bytes = value.getBytes();
 		}
 		InputStream inputStream = new ByteArrayInputStream(bytes);
 		// update the resource content
 		if (rscFile.exists()) {
 			if (rscFile.isReadOnly()) {
 				// provide opportunity to checkout read-only .classpath file (23984)
 				ResourcesPlugin.getWorkspace().validateEdit(new IFile[]{rscFile}, null);
 			}
 			rscFile.setContents(inputStream, IResource.FORCE, null);
 		} else {
 			rscFile.create(inputStream, IResource.FORCE, null);
 		}
 	}
 
 	/**
 	 * Returns a default class path.
 	 * This is the root of the project
 	 */
 	static protected EList<MindPathEntry> defaultMindpath(IProject p) {
 
 		MindPathEntry ret = MindideFactory.eINSTANCE.createMindPathEntry();
 		ret.setName(p.getFolder("src").getFullPath().toPortableString());
 		ret.setEntryKind(MindPathKind.SOURCE);
 		BasicEList<MindPathEntry> retL = new BasicEList<MindPathEntry>();
 		retL.add(ret);
 		return retL;
 	}
 	/*
 	 * Reads the classpath file entries of this project's .classpath file.
 	 * This includes the output entry.
 	 * As a side effect, unknown elements are stored in the given map (if not null)
 	 * Throws exceptions if the file cannot be accessed or is malformed.
 	 */
 	public static EList<MindPathEntry> readFileEntriesWithException(IProject p) throws CoreException, IOException {
 		File file = getMindPathFile(p);
 		if (!file.exists()) {
 			return setDefaultMindpath(p);
 		}
 		byte[] bytes;
 		
 		try {
 			bytes = UtilIO.getFileByteContent(file);
 		} catch (IOException e) {
 			if (!file.exists()) {
 				return setDefaultMindpath(p);
 			}
 			throw e;
 		}
 		
 		//if (hasUTF8BOM(bytes)) { // see https://bugs.eclipse.org/bugs/show_bug.cgi?id=240034
 		//	int length = bytes.length-IContentDescription.BOM_UTF_8.length;
 		//	System.arraycopy(bytes, IContentDescription.BOM_UTF_8.length, bytes = new byte[length], 0, length);
 		//}
 		String xmlMindpath;
 		try {
 			xmlMindpath = new String(bytes, UTF_8); // .mindpath always encoded with UTF-8
 		} catch (UnsupportedEncodingException e) {
 			MindIdeCore.log(e, "Could not read "+MINDPATH_FILENAME+" with UTF-8 encoding"); //$NON-NLS-1$
 			// fallback to default
 			xmlMindpath = new String(bytes);
 		}
 		return decodeMindpath(xmlMindpath);
 	}
 
 	private static File getMindPathFile(IProject p) throws IOException,
 			CoreException {
 		return MindIdeCore.getFile(p.getFile(MINDPATH_FILENAME));
 	}
 
 	
 
 	private static EList<MindPathEntry> setDefaultMindpath(IProject p) {
 		EList<MindPathEntry> newMindpath = defaultMindpath(p);
 		try {
 			setSharedProperty(p, MINDPATH_FILENAME, encodeMindpath(newMindpath, true));
 		} catch (Exception ignored) {
 		}
 		return newMindpath;
 	}
 
 	/*
 	 * Reads the Mindpath file entries of this project's .Mindpath file.
 	 * This includes the output entry.
 	 * As a side effect, unknown elements are stored in the given map (if not null)
 	 */
 	private static EList<MindPathEntry> readFileEntries(IProject p) {
 		try {
 			return readFileEntriesWithException(p);
 		} catch (CoreException e) {
 			MindIdeCore.log(e, "Exception while reading " + p.getFullPath().append(MINDPATH_FILENAME)); //$NON-NLS-1$
 			return INVALID_MINDPATH;
 		} catch (IOException e) {
 			MindIdeCore.log(e, "Exception while reading " + p.getFullPath().append(MINDPATH_FILENAME)); //$NON-NLS-1$
 			return INVALID_MINDPATH;
 		} 
 	}
 	
 	/**
 	 * Writes the Mindpath in a sharable format (VCM-wise) only when necessary, that is, if  it is semantically different
 	 * from the existing one in file. Will never write an identical one.
 	 *
 	 * @param newMindpath IMindpathEntry[]
 	 * @return boolean Return whether the .classpath file was modified.
 	 * @throws IOException 
 	 * @throws CoreException 
 	 * @throws JavaModelException
 	 */
 	public static  boolean writeFileEntries(IProject p, EList<MindPathEntry> newMindpath) throws CoreException, IOException  {
 
 		if (!p.isAccessible()) return false;
 
 		EList<MindPathEntry> fileEntries = readFileEntries(p);
 		if (fileEntries != INVALID_MINDPATH && areMindpathsEqual(newMindpath, fileEntries)) {
 			// no need to save it, it is the same
 			return false;
 		}
 
 		// actual file saving
 		setSharedProperty(p, MINDPATH_FILENAME, encodeMindpath(newMindpath, true));
 		return true;
 		
 	}
 	
 	/**
 	 * Compare current classpath with given one to see if any different.
 	 * Note that the argument classpath contains its binary output.
 	 * @param newMindpath EList<MindPathEntry>]
 	 * @param otherMindpath EList<MindPathEntry>
 	 * @return boolean true if equals
 	 */
 	public static boolean areMindpathsEqual(EList<MindPathEntry> newMindpath,
 			EList<MindPathEntry> otherMindpath) {
 		if (otherMindpath == null || otherMindpath.size() == 0)
 			return false;
 
 		int length = otherMindpath.size();
 		if (length != newMindpath.size())
 				return false;
 
 
 		// compare classpath entries
 		for (int i = 0; i < length; i++) {
 			if (!MindPathEntryCustomImpl.equals(otherMindpath.get(i), newMindpath.get(i)))
 				return false;
 		}
 		return true;
 	}
 
 	public void saveMPE() {
 		Job t = new SaveMPEJob(getProject());
 		t.schedule();
 	}
 
 	public void syncMindPathFile() {
 		IProject p = getProject();
 		try {
 			File f = getMindPathFile(getProject());
 			if (f.exists()) {
 				EList<MindPathEntry> mindPath = readFileEntries(getProject());
 				setMindpath(mindPath);
 			} else {
 				saveMPE();
 			}
 		} catch (IOException e) {
 			MindIdeCore.log(e, "Exception while sync " + p.getFullPath().append(MINDPATH_FILENAME)); //$NON-NLS-1$
 		} catch (CoreException e) {
 			MindIdeCore.log(e, "Exception while sync " + p.getFullPath().append(MINDPATH_FILENAME)); //$NON-NLS-1$
 		}
 		
 	}
 	
 	@Override
 	public EList<MindFile> getAllFiles() {
 		return ResolveImpl.getAllFiles(this);
 	}
 	
 	@Override
 	public MindPathEntry addMindPathImportPackageFromFile(MindFile file) {
 		MindPathEntry mpe = MindIdeCore.newMPEImport(file.getQualifiedName());
 		if (getMindpathentries().contains(mpe)) return null;
 		getMindpathentries().add(mpe);
 		return mpe;
 	}
 	
 	@Override
 	public MindPathEntry addMindPathProjectReferenceFromFile(MindFile file) {
 		MindProject fileProject = file.getPackage() == null ? null: file.getPackage().getRootsrc() == null ? null : file.getPackage().getRootsrc().getProject();
 		if (fileProject == null || fileProject == this) 
 			return null;
 		if (fileProject.getProject() == null)
 			return null;
 		MindPathEntry mpe = MindIdeCore.newMPEProject(fileProject.getProject());
 		if (getMindpathentries().contains(mpe)) return null;
 		getMindpathentries().add(mpe);
 		return mpe;
 	}
 	
 	public void resolveMPE(MindPathEntry mpe) {
 		if (_allsrc == null) {
 			getAllsrc();
 			return;
 		}
 		resolveMPE(this, _allsrc, mpe);
 	}
 		
 	static public void resolveMPE(MindProject currentProject, EList<MindRootSrc> allsrc, MindPathEntry mpe) {
 		
 		MindObject resolvedBy = mpe.getResolvedBy();
 		if (resolvedBy == null) return;
 		switch (mpe.getEntryKind()) {
 		case APPLI:
 			break;
 		case IMPORT_PACKAGE:
 			MindPackage mpackage = (MindPackage) resolvedBy;
 			addSourceOrLibrary(allsrc, mpackage.getRootsrc());
 			break;
 		case LIBRARY:
 			MindRootSrc rs = (MindRootSrc) resolvedBy;
 			addSourceOrLibrary(allsrc, rs);
 			break;
 		case PROJECT:
 			MindProject mp = (MindProject) resolvedBy;
 			if (mp != currentProject)
 				allsrc.addAll(mp.getAllsrc());
 			break;
 		case SOURCE:
 			rs = (MindRootSrc) resolvedBy;
 			addSourceOrLibrary(allsrc, rs);
 			break;
 
 		default:
 			break;
 		}
 	}
 	
 	public void unresolveMPE(MindPathEntry mpe) {
 		recomputeAllSrc();
 	}
 	
 	public void addSrcDep(EList<MindRootSrc> dep) {
 		if (_allsrc == null) {
 			getAllsrc();
 			return;
 		}
 		_allsrc.addAll(dep);
 	}
 	
 	public void removeSrcDep(EList<MindRootSrc> dep) {
 		recomputeAllSrc();
 	}
 	
 	@Override
 	public EList<MindRootSrc> getAllsrc() {
 		if (_allsrc == null) {
 			_allsrc = new EObjectEList<MindRootSrc>(MindRootSrc.class, this, MindidePackage.MIND_PROJECT__ALLSRC);
 			computeAllSrc(_allsrc);
 		}
 		return ECollections.unmodifiableEList(_allsrc);
 	}
 	
 	public void recomputeAllSrc() {
 		if (_allsrc == null)
 			getAllsrc();
 		else {
 			BasicEList<MindRootSrc> newList = new BasicEList<MindRootSrc>();
 			computeAllSrc(newList);
 			_allsrc.retainAll(newList);
 			_allsrc.addAll(newList);
 		}
 			
 	}
 
  	private void computeAllSrc(EList<MindRootSrc> allsrc) {
 		for (MindPathEntry mpe : getMindpathentries()) {
 			resolveMPE(this, allsrc, mpe);
 		}
 	}
 
 	static private void addSourceOrLibrary(EList<MindRootSrc> allsrc, MindRootSrc rs) {
 		if (rs == null) return;
 		if (allsrc.add(rs))
 			for (MindRootSrc drs : rs.getDependencies()) {
 				addSourceOrLibrary(allsrc, drs);
 			}
 	}
 
 	public void changeMINDSRC() {
 		Job r = new ChangeMindSRCVarJob(this);
 		r.schedule();
 	}
 	
 	public void changeMINDCOMP() {
 		Job r = new ChangeMindCOMPVarJob(this);
 		r.schedule();
 	}
 	
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	@Override
 	public EList<MindPathEntry> getMindpathentries() {
 		if (mindpathentries == null) {
 			mindpathentries = new EObjectContainmentWithInverseEList<MindPathEntry>(MindPathEntry.class, this, MindidePackage.MIND_PROJECT__MINDPATHENTRIES, MindidePackage.MIND_PATH_ENTRY__OWNER_PROJECT) {
 				@Override
 				protected boolean useEquals() {
 					return true;
 				}
 			};
 		}
 		return mindpathentries;
 	}
 }
