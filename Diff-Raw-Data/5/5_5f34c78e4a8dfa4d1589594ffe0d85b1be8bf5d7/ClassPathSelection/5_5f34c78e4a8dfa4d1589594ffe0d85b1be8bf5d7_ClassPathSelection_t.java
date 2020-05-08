 /*******************************************************************************
  * Copyright (c) 2003, 2007 IBM Corporation and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     IBM Corporation - initial API and implementation
  *******************************************************************************/
 package org.eclipse.jst.j2ee.application.internal.operations;
 
 
 
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 
 import org.eclipse.core.resources.IContainer;
 import org.eclipse.core.resources.IFile;
 import org.eclipse.core.resources.IProject;
 import org.eclipse.core.resources.IResource;
 import org.eclipse.core.resources.ResourcesPlugin;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IPath;
 import org.eclipse.core.runtime.Path;
 import org.eclipse.emf.common.util.URI;
 import org.eclipse.jdt.core.IClasspathAttribute;
 import org.eclipse.jdt.core.IClasspathContainer;
 import org.eclipse.jdt.core.IClasspathEntry;
 import org.eclipse.jdt.core.JavaCore;
 import org.eclipse.jem.util.emf.workbench.ProjectUtilities;
 import org.eclipse.jst.common.jdt.internal.javalite.IJavaProjectLite;
 import org.eclipse.jst.common.jdt.internal.javalite.JavaCoreLite;
 import org.eclipse.jst.j2ee.classpathdep.ClasspathDependencyUtil;
 import org.eclipse.jst.j2ee.classpathdep.IClasspathDependencyConstants;
 import org.eclipse.jst.j2ee.classpathdep.IClasspathDependencyConstants.DependencyAttributeType;
 import org.eclipse.jst.j2ee.commonarchivecore.internal.Archive;
 import org.eclipse.jst.j2ee.commonarchivecore.internal.CommonArchiveResourceHandler;
 import org.eclipse.jst.j2ee.commonarchivecore.internal.CommonarchiveFactory;
 import org.eclipse.jst.j2ee.commonarchivecore.internal.Container;
 import org.eclipse.jst.j2ee.commonarchivecore.internal.EARFile;
 import org.eclipse.jst.j2ee.commonarchivecore.internal.exception.ManifestException;
 import org.eclipse.jst.j2ee.commonarchivecore.internal.exception.OpenFailureException;
 import org.eclipse.jst.j2ee.commonarchivecore.internal.helpers.ArchiveManifest;
 import org.eclipse.jst.j2ee.commonarchivecore.internal.strategy.LoadStrategy;
 import org.eclipse.jst.j2ee.commonarchivecore.internal.strategy.ZipFileLoadStrategyImpl;
 import org.eclipse.jst.j2ee.commonarchivecore.internal.util.ArchiveUtil;
 import org.eclipse.jst.j2ee.componentcore.J2EEModuleVirtualComponent;
 import org.eclipse.jst.j2ee.internal.archive.operations.ComponentLoadStrategyImpl;
 import org.eclipse.jst.j2ee.internal.archive.operations.EARComponentLoadStrategyImpl;
 import org.eclipse.jst.j2ee.internal.classpathdep.ClasspathDependencyEnablement;
 import org.eclipse.jst.j2ee.internal.plugin.IJ2EEModuleConstants;
 import org.eclipse.jst.j2ee.internal.plugin.J2EEPlugin;
 import org.eclipse.jst.j2ee.internal.project.J2EEProjectUtilities;
 import org.eclipse.jst.j2ee.model.IModelProvider;
 import org.eclipse.jst.j2ee.model.ModelProviderManager;
 import org.eclipse.jst.j2ee.project.JavaEEProjectUtilities;
 import org.eclipse.jst.javaee.ejb.EJBJar;
 import org.eclipse.wst.common.componentcore.ComponentCore;
 import org.eclipse.wst.common.componentcore.UnresolveableURIException;
 import org.eclipse.wst.common.componentcore.internal.impl.ModuleURIUtil;
 import org.eclipse.wst.common.componentcore.internal.resources.VirtualArchiveComponent;
 import org.eclipse.wst.common.componentcore.internal.util.ComponentUtilities;
 import org.eclipse.wst.common.componentcore.resources.IVirtualComponent;
 import org.eclipse.wst.common.componentcore.resources.IVirtualReference;
 
 
 
 public class ClassPathSelection {
 	protected Archive archive;
 	protected org.eclipse.jst.j2ee.commonarchivecore.internal.helpers.ArchiveManifest manifest;
 	protected IVirtualComponent component;
 	protected IProject earProject;
 	protected IVirtualComponent earComponent;
 	protected List classpathElements;
 	protected Map urisToElements;
 	protected boolean modified;
 	private String targetProjectName;
 	protected Map ejbToClientJARs = null;
 	protected Map clientToEJBJARs = null;
 	public static final int FILTER_EJB_SERVER_JARS = 0;
 	public static final int FILTER_EJB_CLIENT_JARS = 1;
 	public static final int FILTER_NONE = 2;
 
 	protected int filterLevel = 2;
 
 	protected static Comparator comparator = new Comparator() {
 		/**
 		 * @see Comparator#compare(Object, Object)
 		 */
 		public int compare(Object o1, Object o2) {
 			int retVal = 0;
 			if (o1 instanceof Archive)
 			{
 				Archive a1 = (Archive) o1;
 				Archive a2 = (Archive) o2;
 				retVal = a1.getURI().compareTo(a2.getURI());
 			}
 			else if (o1 instanceof IVirtualReference)
 			{
 				IVirtualReference ref1 = (IVirtualReference) o1;
 				IVirtualReference ref2 = (IVirtualReference) o2;
 				retVal = ref1.getArchiveName().compareTo(ref2.getArchiveName());
 			}
 			else
 			{
 				retVal = o1.toString().compareTo(o2.toString());
 			}
 			return retVal;
 		}
 	};
 
 	public ClassPathSelection(Archive anArchive, String targetProjectName, EARFile earFile) {
 		super();
 		archive = anArchive;
 		this.targetProjectName = targetProjectName;
 		initializeEARProject(earFile);
 		initializeElements();
 	}
 
 	/**
 	 * ClassPathSelection constructor comment.
 	 */
 	public ClassPathSelection(Archive anArchive, EARFile earFile) {
 		super();
 		archive = anArchive;
 		initializeEARProject(earFile);
 		initializeElements();
 	}
 
 	/**
 	 * Creates without an EAR component.
 	 */
 	public ClassPathSelection(IVirtualComponent aComponent) {
 		super();
 		component = aComponent;
 		targetProjectName = aComponent.getProject().getName();
 		initializeElements();
 	}
 	
 	/**
 	 * ClassPathSelection constructor comment.
 	 */
 	public ClassPathSelection(IVirtualComponent aComponent, IVirtualComponent anEarComponent) {
 		this(aComponent);
 		earComponent = anEarComponent;
 		earProject = earComponent.getProject();
 		initializeElements();
 	}
 
 	public ClassPathSelection(IVirtualComponent aComponent, IVirtualComponent anEarComponent, ArchiveManifest aManifest) {
 		this(aComponent);
 		earComponent = anEarComponent;
 		earProject = earComponent.getProject();
 		manifest = aManifest;
 		initializeElements();
 	}
 	
 	public ClassPathSelection(IVirtualComponent aComponent, ArchiveManifest aManifest) {
 		super();
 		component = aComponent;
 		targetProjectName = aComponent.getProject().getName();
 		manifest = aManifest;
 		initializeElements();
 	}
 	
 	
 	/**
 	 * ClassPathSelection constructor comment.
 	 */
 	public ClassPathSelection() {
 		super();
 	}
 
 	protected ClasspathElement createElement(Archive referencingArchive, Archive referencedArchive, String cpEntry) {
 		ClasspathElement element = new ClasspathElement(referencingArchive);
 		element.setValid(true);
 				
 		String uriString = referencedArchive.getURI();
 		URI uri = URI.createURI(uriString);
 
 		boolean hasAbsolutePath = uri.hasAbsolutePath();
 		if( hasAbsolutePath ){
 			uriString = uri.lastSegment();
 		}
 		
 		//element.setText(referencedArchive.getURI());
 		element.setText(uriString);
 		element.setTargetArchive(referencedArchive);
 		element.setEarProject(earProject);
 		if( earComponent != null ){
 			IContainer earConentFolder = earComponent.getRootFolder().getUnderlyingFolder();
 			if( earConentFolder.getType() == IResource.FOLDER ){
 				element.setEarContentFolder( earConentFolder.getName());
 			}else {
 				element.setEarContentFolder( "" ); //$NON-NLS-1$
 			}
 		}
 	
 		setProjectValues(element, referencedArchive);
 		if (cpEntry != null)
 			element.setValuesSelected(cpEntry);
 		setType(element, referencedArchive);
 		return element;
 	}
 
 	protected ClasspathElement createElement(IVirtualComponent referencingArchive, IVirtualReference referencedArchive, String cpEntry) {
 		ClasspathElement element = new ClasspathElement(referencingArchive);
 		element.setValid(true);
 				
 		String uriString = referencedArchive.getArchiveName();
 		
 		element.setText(uriString);
 		element.setTargetComponent(referencedArchive.getReferencedComponent());
 		element.setEarProject(earProject);
 		if( earComponent != null ){
 			IContainer earConentFolder = earComponent.getRootFolder().getUnderlyingFolder();
 			if( earConentFolder.getType() == IResource.FOLDER ){
 				element.setEarContentFolder( earConentFolder.getName());
 			}else {
 				element.setEarContentFolder( "" ); //$NON-NLS-1$
 			}
 		}
 	
 		setProjectValues(element, referencedArchive);
 		if (cpEntry != null)
 			element.setValuesSelected(cpEntry);
 		setType(element, referencedArchive);
 		return element;
 	}
 
 	protected ClasspathElement createInvalidElement(String cpEntry) {
 		ClasspathElement element = new ClasspathElement(archive);
 		element.setValid(false);
 		element.setSelected(true);
 		element.setRelativeText(cpEntry);
 		element.setText(cpEntry);
 		element.setEarProject(earProject);
 		setInvalidProject(element);
 		return element;
 	}
 	
 	public ClasspathElement createProjectElement(IProject project) {
 		ClasspathElement element = new ClasspathElement(project);
 		element.setValid(true);
 		element.setSelected(true);
 		element.setText(project.getName());
 		element.setProject(project);
 		addClasspathElement(element,element.getProjectName());
 		return element;
 	}
 	
 	public ClasspathElement createProjectElement(IProject project, boolean existingEntry ) {
 		ClasspathElement element = new ClasspathElement(project);
 		element.setValid(true);
 		element.setSelected(existingEntry);
 		element.setText(project.getName());
 		element.setProject(project);
 		addClasspathElement(element,element.getProjectName());
 		return element;
 	}
 	
 	
 	public ClasspathElement createArchiveElement(URI uri, String name, String cpEntry) {
 		ClasspathElement element = new ClasspathElement(uri);
 		element.setValid(false);
 		element.setRelativeText(name);
 		if (cpEntry != null)
 			element.setValuesSelected(cpEntry);		
 		element.setText(name);
 		element.setEarProject(earProject);
 		return element;
 	}
 	
 	public void buildClasspathComponentDependencyMap(final IVirtualComponent comp, final Map pathToComp) {
 		if (comp != null && comp instanceof J2EEModuleVirtualComponent) {
 			J2EEModuleVirtualComponent j2eeComp = (J2EEModuleVirtualComponent) comp;
 			IVirtualReference[] cpRefs = j2eeComp.getJavaClasspathReferences();
 
 			for (int i = 0; i < cpRefs.length; i++) {
 				// only ../ mappings supported at this level
 				if (!cpRefs[i].getRuntimePath().equals(IClasspathDependencyConstants.RUNTIME_MAPPING_INTO_CONTAINER_PATH)) {
 					continue;
 				}
 				
 				final IVirtualComponent referencedComponent = cpRefs[i].getReferencedComponent();
 				final IPath path = ClasspathDependencyUtil.getClasspathVirtualReferenceLocation(cpRefs[i]); 
 				final IVirtualComponent existingComp = (IVirtualComponent) pathToComp.get(path);
 				if (existingComp != null) {
 					// replace with a temp VirtualArchiveComponent whose IProject is set to a new pseudo name that is
 					// the concatenation of all project contributions for that archive
 					if (existingComp instanceof VirtualArchiveComponent) {
 						final VirtualArchiveComponent oldComp = (VirtualArchiveComponent) existingComp;
 						final IVirtualComponent newComp = updateDisplayVirtualArchiveComponent(oldComp, cpRefs[i]);
 						pathToComp.put(path, newComp);
 					}
 				} else {
 					pathToComp.put(path, referencedComponent);
 				}
 			}
 		}
 	}
 	
 	/**
 	 * Create a new VirtualArchiveComponent (used only for display purposes) whose IProject is set to a dummy value whose
 	 * name is a concatentation of the existing name and the name of the project associated with the new ref.
 	 * This is used to represent the case where a single unique archive is referenced/contributed by multiple dependent projects.
 	 */ 
 	public static VirtualArchiveComponent updateDisplayVirtualArchiveComponent(final VirtualArchiveComponent oldComp, final IVirtualReference newRef) {
 		final String newProjName = oldComp.getProject().getName() + " " + newRef.getReferencedComponent().getProject().getName();   //$NON-NLS-1$
 		final IProject newProj = ResourcesPlugin.getWorkspace().getRoot().getProject(newProjName);
 		final VirtualArchiveComponent newComponent = (VirtualArchiveComponent) ComponentCore.createArchiveComponent(newProj, oldComp.getName());
 		return newComponent;
 	}
 	
 	public ClasspathElement[] createClasspathEntryElements(final IVirtualComponent comp, final IPath archiveRuntimePath, final IPath classFolderRuntimePath) throws CoreException {
 		final List elements = new ArrayList();
 		if (comp != null && comp.getProject().isAccessible()) {
 			final IProject project = comp.getProject();
 			if (project.hasNature(JavaCore.NATURE_ID)) {
 				final IJavaProjectLite javaProjectLite = JavaCoreLite.create(project);
 				final boolean isWebApp = JavaEEProjectUtilities.isDynamicWebProject(project);
 				final boolean webLibsOnly = isWebApp && !ClasspathDependencyEnablement.isAllowClasspathComponentDependency();
 				final Map taggedEntries = ClasspathDependencyUtil.getRawComponentClasspathDependencies(javaProjectLite, DependencyAttributeType.CLASSPATH_COMPONENT_DEPENDENCY, webLibsOnly);
 				
 				Iterator i = taggedEntries.keySet().iterator();
 				while (i.hasNext()) {
 					final IClasspathEntry entry = (IClasspathEntry) i.next();
 					final IClasspathAttribute attrib = (IClasspathAttribute) taggedEntries.get(entry);
 					final boolean isClassFolder = ClasspathDependencyUtil.isClassFolderEntry(entry);
 					final IPath runtimePath = ClasspathDependencyUtil.getRuntimePath(attrib, isWebApp, isClassFolder); 
 					if (runtimePath != null && ((isClassFolder && !runtimePath.equals(classFolderRuntimePath)) || (!isClassFolder && !runtimePath.equals(archiveRuntimePath)))) {
 						// if runtime path does not match target runtime path, skip
 						continue;
 					}
 					final ClasspathElement element = createClasspathElementForEntry(project, entry);
 					element.setSelected(true);
 					addClasspathElement(element, element.getArchiveURI().toString());
 				}
 				
 				final List potentialEntries = ClasspathDependencyUtil.getPotentialComponentClasspathDependencies(javaProjectLite);
 				i = potentialEntries.iterator();
 				while (i.hasNext()) {
 					final IClasspathEntry entry = (IClasspathEntry) i.next();
 					if (isWebApp && classFolderRuntimePath.equals(IClasspathDependencyConstants.RUNTIME_MAPPING_INTO_COMPONENT_PATH) && ClasspathDependencyUtil.isClassFolderEntry(entry)) {
 						// don't display class folder dependencies for dynamic web projects on the non-web lib dependency page
 						continue;
 					}
 					final ClasspathElement element = createClasspathElementForEntry(project, entry);
 					element.setSelected(false);
 					addClasspathElement(element, element.getArchiveURI().toString());
 				}
 			}
 		}
 		return (ClasspathElement[]) elements.toArray(new ClasspathElement[elements.size()]);
 	}
 
 	private ClasspathElement createClasspathElementForEntry(final IProject project, final IClasspathEntry entry) {
 		final IPath entryPath = entry.getPath();
 		final URI archiveURI = URI.createURI(entryPath.toString());
 		final int kind = entry.getEntryKind();
 		String elementName = entryPath.toString();
 		if (kind == IClasspathEntry.CPE_CONTAINER) {
 			try {
 				final IClasspathContainer container = JavaCore.getClasspathContainer(entryPath, JavaCore.create(project));
 				if (container != null) {
 					elementName = container.getDescription();
 				}
 			} catch (CoreException ce) {
 			}
 		}
 
 		ClasspathElement element = createClasspathEntryElement(project, archiveURI, elementName, entry);
 		return element;
 	}
 		
 	/**
 	 * @param element
 	 */
 	private void setInvalidProject(ClasspathElement element) {
 		IProject earProj = element.getEarProject();
 		//IVirtualComponent[] component = ComponentUtilities.getComponent(earProj.getName());
 		IVirtualComponent refEarComponent = ComponentUtilities.getComponent(earProj.getName());
 		
 		IVirtualReference[] references = J2EEProjectUtilities.getComponentReferences(refEarComponent);
 		String moduleName = element.getRelativeText();
 		if(moduleName != null) {
 			IVirtualComponent modComponent = null;
 			for (int cnt=0; cnt < references.length; cnt++)
 			{
 				if (moduleName.equals(references[cnt].getArchiveName()))
 				{
 					modComponent = references[cnt].getReferencedComponent();
 				}
 			}
 			if(modComponent != null) {
 				IProject mappedProject = modComponent.getProject();
 				element.setProject(mappedProject);
 			}
 		}
 	}
 
 	/**
 	 * Insert the method's description here. Creation date: (8/22/2001 1:17:21 PM)
 	 * 
 	 * @return java.util.List
 	 */
 	public java.util.List getClasspathElements() {
 		if(classpathElements == null)
 			classpathElements = new ArrayList();
 		return classpathElements;
 	}
 	
 	public java.util.List getSelectedClasspathElements() {
 		ArrayList list = new ArrayList();
 		Iterator it = getClasspathElements().iterator();
 		while(it.hasNext()) {
 			ClasspathElement element = (ClasspathElement)it.next();
 			if( element.isSelected() ){
 				list.add(element);
 			}
 		}
 		return list;
 	}	
 
 	/**
 	 * Adapter method to convert the manifest class path entries which map to a project to a list of
 	 * classpath entries for a java build path
 	 */
 	protected IClasspathEntry[] getClasspathEntries(boolean filterSelected) {
 		List result = new ArrayList();
 		IClasspathEntry[] array = null;
 		ClasspathElement element = null;
 		if(classpathElements != null) {
 		for (int i = 0; i < classpathElements.size(); i++) {
 			element = (ClasspathElement) classpathElements.get(i);
 			if (filterSelected && !element.isSelected())
 				continue;
 			array = ((ClasspathElement) classpathElements.get(i)).newClasspathEntries();
 			if (array == null)
 				continue;
 			for (int j = 0; j < array.length; j++) {
 				if (!result.contains(array[j]))
 					result.add(array[j]);
 			}
 		}
 		return (IClasspathEntry[]) result.toArray(new IClasspathEntry[result.size()]);
 		}
 		return null;
 	}
 
 	/**
 	 * Adapter method to convert the manifest class path entries which map to a project to a list of
 	 * classpath entries for a java build path
 	 */
 	public IClasspathEntry[] getClasspathEntriesForAll() {
 		return getClasspathEntries(false);
 	}
 
 	/**
 	 * Adapter method to convert the manifest class path entries which map to a project to a list of
 	 * classpath entries for a java build path
 	 */
 	public IClasspathEntry[] getClasspathEntriesForSelected() {
 		return getClasspathEntries(true);
 	}
 
 	protected EARFile getEARFile() {
 		if (archive == null)
 			return null;
 
 		Container parent = archive.getContainer();
 		if (parent != null && parent.isEARFile())
 			return (EARFile) parent;
 		return null;
 	}
 
 	protected static IProject getEARProject(Archive anArchive) {
 		Container c = anArchive.getContainer();
 		if (!c.isEARFile())
 			return null;
 		EARFile ear = (EARFile) c;
 		LoadStrategy loader = ear.getLoadStrategy();
 		if (!(loader instanceof EARComponentLoadStrategyImpl))
 			return null;
 
 		return ((EARComponentLoadStrategyImpl) loader).getComponent().getProject();
 	}
 
 	public Archive getArchive() {
 		return archive;
 	}
 
 	protected IProject getProject(Archive anArchive) {
 		IVirtualComponent comp = getComponent(anArchive);
 		if (comp != null) {
 			return comp.getProject();
 		}
 		return null;
 	}
 	
 	protected IVirtualComponent getComponent(Archive anArchive) {
 		LoadStrategy loader = anArchive.getLoadStrategy();
 		if (loader instanceof ComponentLoadStrategyImpl)
 			return ((ComponentLoadStrategyImpl) loader).getComponent();
 		return null;
 	}
 
 	public String getText() {
 		return archive.getURI();
 	}
 
 	protected Archive getArchive(String uri, List archives) {
 		for (int i = 0; i < archives.size(); i++) {
 			Archive anArchive = (Archive) archives.get(i);
 			
 			String archiveURIString = anArchive.getURI();
 			URI archiveURI = URI.createURI(archiveURIString);
 			boolean hasAbsolutePath = archiveURI.hasAbsolutePath();
 			if( hasAbsolutePath ){
 				archiveURIString = archiveURI.lastSegment();
 			}
 			if (archiveURIString.equals(uri))
 				return anArchive;
 		}
 		return null;
 	}
 
 	protected IVirtualReference getVirtualReference(String uri, List archives) {
 		for (int i = 0; i < archives.size(); i++) {
 			IVirtualReference anArchive = (IVirtualReference) archives.get(i);
 			
 			String archiveURIString = anArchive.getArchiveName();
 			if (archiveURIString.equals(uri))
 				return anArchive;
 		}
 		return null;
 	}
 
 	public static boolean isValidDependency(IVirtualComponent referencedJAR, IVirtualComponent referencingJAR) {
 		//No other modules should reference wars
 		if (JavaEEProjectUtilities.isDynamicWebComponent(referencedJAR))
 			return false;
 
 		if ( referencedJAR.getName().equals( referencingJAR.getName() ) )
 			return false;		
 
 		//Clients can reference all but the WARs, which we've already covered
 		// above; WARs and EJB JARs
 		//can reference all but WARs, above, or ApplicationClients
 		return JavaEEProjectUtilities.isApplicationClientComponent(referencingJAR) || !JavaEEProjectUtilities.isApplicationClientComponent(referencedJAR);
 	}
 
 	protected void initializeElements() {
 //		ejbToClientJARs = J2EEProjectUtilities.collectEJBClientJARs(getEARFile());
 		ejbToClientJARs = new HashMap();
 		IVirtualComponent currentComponent = null;
 		IVirtualComponent clientComponent = null;
 		Object rootModelObject = null;
 		IModelProvider modelProvider = null;
 		String ejbClientJarName = null;
 		List archives = null;
 		IVirtualReference other = null;
 		ClasspathElement element = null;
 		String[] cp = new String[0];
 		
 		if (earComponent != null) {
 			IVirtualReference[] references = earComponent.getReferences();		
 			for (int cnt=0; cnt<references.length; cnt++)
 			{
 				clientComponent = null;
 				modelProvider = null;
 				rootModelObject = null;
 				ejbClientJarName = null;
 				currentComponent = references[cnt].getReferencedComponent();
 				if (JavaEEProjectUtilities.isEJBComponent(currentComponent))
 				{
 					if(currentComponent.isBinary()){
 						//TODO add binary support
 						continue;
 					}
 					modelProvider = ModelProviderManager.getModelProvider(currentComponent);
 					if(modelProvider==null) {
 						continue;
 					}
 					rootModelObject = modelProvider.getModelObject();
 					if (rootModelObject instanceof EJBJar)
 					{
 						ejbClientJarName = ((EJBJar)rootModelObject).getEjbClientJar();
 					}
 					else if (rootModelObject instanceof org.eclipse.jst.j2ee.ejb.EJBJar)
 					{
 						ejbClientJarName = ((org.eclipse.jst.j2ee.ejb.EJBJar)rootModelObject).getEjbClientJar();
 					}
 					if (ejbClientJarName != null)
 					{
 						clientComponent = J2EEProjectUtilities.getModule(earComponent, ejbClientJarName);
 					}
 					if (clientComponent != null)
 					{
 						ejbToClientJARs.put(currentComponent, clientComponent);
 					}
 				}
 			}
 			clientToEJBJARs = reverse(ejbToClientJARs);
 			classpathElements = new ArrayList();
 			urisToElements = new HashMap();
 
 			
 			try {
 				//			cp = archive.getManifest().getClassPathTokenized();
 				
 				if( manifest == null ){
 					manifest = J2EEProjectUtilities.readManifest(component.getProject());
 				}
 				cp = manifest.getClassPathTokenized();
 			} catch (ManifestException ex) {
 				J2EEPlugin.logError(ex);
 			}
 			String projectUri = earComponent.getReference(component.getName()).getArchiveName();
 			archives = new ArrayList(Arrays.asList(earComponent.getReferences()));
 			
 			for (int i = 0; i < cp.length; i++) {
 				String cpEntry = cp[i];
 				String uri = ArchiveUtil.deriveEARRelativeURI(cpEntry, projectUri);
 
 				other = getVirtualReference(uri, archives);
 				if (other != null && isValidDependency(other.getReferencedComponent(), component)) {
 					element = createElement(component, other, cpEntry);
 					archives.remove(other);
 				} else {
 					element = createInvalidElement(cpEntry);
 					if (element.representsImportedJar()) {
 						element.setValid(true);
 						element.setProject(getProject(archive));
 					}
 					if (other != null)
 						element.setProject(other.getReferencedComponent().getProject());
 
 					if( other == null ){
 						//making a best guess for the project name
 						if( element.getProject() == null ){
 							int index = cpEntry.indexOf(IJ2EEModuleConstants.JAR_EXT);
 							// if jar is nested in a folder you must not look for
 							// project (segments in project name cause assertion
 							// error)
 							boolean isMultiSegment = cpEntry
 									.indexOf(File.pathSeparator) == -1;
 							if (!isMultiSegment && index > 0) {
 								String projectName = cpEntry.substring(0, index);
 								IPath projectPath = new Path(projectName);
 								//if there are multiple segments and no reference archive is found
 								//then either this is pointing to a jar in the EAR that doesn't exist
 								//or the DependecyGraphManager is stale
 								if(projectPath.segmentCount() > 1){
 									if(earComponent != null && earComponent.getProject() != null){
 										element.setProject(earComponent.getProject());
 									}
 								} else {
 									IProject project = ProjectUtilities.getProject( projectName );
 									if( project != null && project.exists() )
 										element.setProject( project );
 								}
 							}
 						}
 					}
 				}
 				addClasspathElement(element, uri);
 			}
 		}
 		
 		// Add resolved contributions from tagged classpath entries
 		// XXX Don't show resolved contributions from tagged classpath entries on this project's classpath; we should elements corresponding to the raw entries instead
 		//createClasspathComponentDependencyElements(comp);
 		
 		// Add elements for raw classpath entries (either already tagged or potentially taggable) 
 		if(ClasspathDependencyEnablement.isAllowClasspathComponentDependency()){
 			try {
 			    createClasspathEntryElements(component, IClasspathDependencyConstants.RUNTIME_MAPPING_INTO_CONTAINER_PATH, IClasspathDependencyConstants.RUNTIME_MAPPING_INTO_COMPONENT_PATH);
 			} catch (CoreException ce) {
 				J2EEPlugin.logError(ce);
 			}
 		}
 		
 		if (earComponent != null) {
 			if(archives !=null){
 				Collections.sort(archives, comparator);
 				//Anything that remains in the list of available archives that is valid should be
 				//available for selection
 				for (int i = 0; i < archives.size(); i++) {
 					other = (IVirtualReference) archives.get(i);
 	
 					if (other != archive && isValidDependency(other.getReferencedComponent(), component)) {
 						IProject project = other.getReferencedComponent().getProject();
 						if (null == targetProjectName || null == project || !project.getName().equals(targetProjectName)) {
 							element = createElement(component, other, null);
 							element.setProject(other.getReferencedComponent().getProject());
 							addClasspathElement(element, other.getArchiveName());
 						}
 					}
 				}
 			}
 			IVirtualReference[] newrefs = earComponent.getReferences();
 			for( int i=0; i < newrefs.length; i++){
 				IVirtualReference ref = newrefs[i];
 				IVirtualComponent referencedComponent = ref.getReferencedComponent();
 				boolean isBinary = referencedComponent.isBinary();
 				if( isBinary ){
 
 					/**
 					 * Warning clean-up 12/05/2005
 					 */   
 					//String uri = J2EEProjectUtilities.getResolvedPathForArchiveComponent(referencedComponent.getName()).toString();
 					String unresolvedURI = ref.getArchiveName();
 					if(unresolvedURI == null){
 						try {
 							unresolvedURI = ModuleURIUtil.getArchiveName(URI.createURI(ModuleURIUtil.getHandleString(referencedComponent)));
 						} catch (UnresolveableURIException e) {
 							J2EEPlugin.logError(e);
 						}
 					}
 
 					if(unresolvedURI != null){
 						URI archiveURI = URI.createURI(unresolvedURI);	
 
 						boolean  alreadyInList = false;
 						Iterator iter = getClasspathElements().iterator();
 						while(iter.hasNext()){
 							ClasspathElement tmpelement = (ClasspathElement)iter.next();
 							if(unresolvedURI.endsWith(tmpelement.getText())){
 								alreadyInList = true;
 								break;
 							}
 						}
 
 						if( !alreadyInList ){
 							if( inManifest(cp, archiveURI.lastSegment())){
 								element = createArchiveElement(URI.createURI(ModuleURIUtil.getHandleString(referencedComponent)), archiveURI.lastSegment(), archiveURI.lastSegment());
 								addClasspathElement(element, unresolvedURI);
 							}else{
 								element = createArchiveElement(URI.createURI(ModuleURIUtil.getHandleString(referencedComponent)), archiveURI.lastSegment(), null);
 								addClasspathElement(element, unresolvedURI);							
 							}
 						}
 					}
 				}
 			}
 		}	
 	}
 	
 	public ClasspathElement createClasspathArchiveElement(final IProject project, URI archiveURI, String unresolvedURI) {
 		final ClasspathElement element = createArchiveElement(archiveURI, archiveURI.lastSegment(), archiveURI.lastSegment());
 		element.setProject(project);
 		element.setClasspathDependency(true);
 		return element;
 	}
 	
 	public ClasspathElement createClasspathEntryElement(final IProject project, URI archiveURI, String elementName, IClasspathEntry entry) {
 		final ClasspathElement element = createArchiveElement(archiveURI, elementName, elementName);
 		element.setProject(project);
 		element.setClasspathEntry(true, entry);
 		element.setValid(true);
 		return element;
 	}
 
 	boolean inManifest(String[] cp, String archiveName ){
 		boolean result = false;
 		String cpEntry = ""; //$NON-NLS-1$
 		for (int i = 0; i < cp.length; i++) {
 			cpEntry = cp[i];
 			if( archiveName.equals(cpEntry)){
 				result = true;
 			}
 		}
 		return result;
 	}
 		
 	protected List loadClassPathArchives(){
         /**
          * Warning clean-up 12/05/2005
          */   
 		//LoadStrategy loadStrat = archive.getLoadStrategy();
 		
 		List archives = new ArrayList();
 		
 		if( earComponent!= null){
 			IVirtualReference[] newrefs = earComponent.getReferences();
 			for( int i=0; i < newrefs.length; i++){
 				IVirtualReference ref = newrefs[i];
 				IVirtualComponent referencedComponent = ref.getReferencedComponent();
 				boolean isBinary = referencedComponent.isBinary();
 			
 				if( isBinary ){
 					String uri = J2EEProjectUtilities.getResolvedPathForArchiveComponent(referencedComponent.getName()).toString();
 		
 					try {
 						ZipFileLoadStrategyImpl strat = createLoadStrategy(uri);
 						Archive archive = null;
 						try {
 							archive = CommonarchiveFactory.eINSTANCE.primOpenArchive(strat, uri);
 						} catch (OpenFailureException e) {
 							J2EEPlugin.logError(e);
 						}
 
 						archives.add(archive);
 						
 					} catch (FileNotFoundException e) {
 						J2EEPlugin.logError(e);
 					} catch (IOException e) {
 						J2EEPlugin.logError(e);
 					}
 				}
 				
 			}
 		}
 		return archives;
 	}
 	
 	Archive getClassPathArchive(String uri, List archives){
 		for (int i = 0; i < archives.size(); i++) {
 			Archive anArchive = (Archive) archives.get(i);
 			
 			String archiveURIString = anArchive.getURI();
 			URI archiveURI = URI.createURI(archiveURIString);
 			boolean hasAbsolutePath = archiveURI.hasAbsolutePath();
 			if( hasAbsolutePath ){
 				archiveURIString = archiveURI.lastSegment();
 			}
 			if (archiveURIString.equals(uri))
 				return anArchive;
 		}
 		return null;
 	}
 	
 	boolean  isClassPathArchive(String uri, List archives){
 		for (int i = 0; i < archives.size(); i++) {
 			Archive anArchive = (Archive) archives.get(i);
 			
 			String archiveURIString = anArchive.getURI();
 			URI archiveURI = URI.createURI(archiveURIString);
 	         /**
 	          * Warning clean-up 12/05/2005
 	          */   
 			//boolean hasAbsolutePath = archiveURI.hasAbsolutePath();
 			if( archiveURI.lastSegment().equals(uri) ){
 				return true;
 			}
 		}
 		return false;
 	}	
 	
 	public ZipFileLoadStrategyImpl createLoadStrategy(String uri) throws FileNotFoundException, IOException {
 		String filename = uri.replace('/', java.io.File.separatorChar);
 		java.io.File file = new java.io.File(filename);
 		if (!file.exists()) {
 			throw new FileNotFoundException(CommonArchiveResourceHandler.getString(CommonArchiveResourceHandler.file_not_found_EXC_, (new Object[]{uri, file.getAbsolutePath()}))); // = "URI Name: {0}; File name: {1}"
 		}
 		if (file.isDirectory()) {
 			throw new FileNotFoundException(CommonArchiveResourceHandler.getString(CommonArchiveResourceHandler.file_not_found_EXC_, (new Object[]{uri, file.getAbsolutePath()}))); // = "URI Name: {0}; File name: {1}"
 		}
 		return new org.eclipse.jst.j2ee.commonarchivecore.internal.strategy.ZipFileLoadStrategyImpl(file);
 	}
 	
 	
 	private void initializeEARProject(EARFile earFile) {
 		LoadStrategy loadStrat = earFile.getLoadStrategy();
 		if (loadStrat instanceof EARComponentLoadStrategyImpl){
 			earComponent = ((EARComponentLoadStrategyImpl) loadStrat).getComponent();
 			earProject = ((EARComponentLoadStrategyImpl) loadStrat).getComponent().getProject();
 		}	
 	}
 
 	private void setType(ClasspathElement element, Archive other) {
 		if (other == null)
 			return;
 		else if (clientToEJBJARs.containsKey(other))
 			element.setJarType(ClasspathElement.EJB_CLIENT_JAR);
 		else if (other.isEJBJarFile())
 			element.setJarType(ClasspathElement.EJB_JAR);
 	}
 
 	private void setType(ClasspathElement element, IVirtualReference other) {
 		if (other == null)
 			return;
 		else if (clientToEJBJARs.containsKey(other.getReferencedComponent()))
 			element.setJarType(ClasspathElement.EJB_CLIENT_JAR);
 		else if (JavaEEProjectUtilities.isEJBComponent(other.getReferencedComponent()))
 			element.setJarType(ClasspathElement.EJB_JAR);
 	}
 
 	/**
 	 * @param localejbToClientJARs
 	 * @return
 	 */
 	private Map reverse(Map localejbToClientJARs) {
 		if (localejbToClientJARs == null || localejbToClientJARs.isEmpty())
 			return Collections.EMPTY_MAP;
 		Map result = new HashMap();
 		Iterator iter = localejbToClientJARs.entrySet().iterator();
 		while (iter.hasNext()) {
 			Map.Entry entry = (Map.Entry) iter.next();
 			result.put(entry.getValue(), entry.getKey());
 		}
 		return result;
 	}
 
 	public void addClasspathElement(ClasspathElement element, String uri) {
 		getClasspathElements().add(element);
 		getUrisToElements().put(uri, element);
 		element.setParentSelection(this);
 	}
 
 	/**
 	 * Insert the method's description here. Creation date: (8/22/2001 6:05:11 PM)
 	 * 
 	 * @return boolean
 	 */
 	public boolean isModified() {
 		return modified;
 	}
 
 	/**
 	 * Insert the method's description here. Creation date: (8/22/2001 6:05:11 PM)
 	 * 
 	 * @param newModified
 	 *            boolean
 	 */
 	public void setModified(boolean newModified) {
 		modified = newModified;
 	}
 
 	protected void setProjectValues(ClasspathElement element, Archive referencedArchive) {
 		IProject p = getProject(referencedArchive);
 		if (p == null)
 			return;
 
 		element.setProject(p);
 		//Handle the imported jars in the project
 		String[] cp = null;
 		try {
 			cp = referencedArchive.getManifest().getClassPathTokenized();
 		} catch (ManifestException mfEx) {
 			J2EEPlugin.logError(mfEx);
 			cp = new String[]{};
 		}
 		List paths = new ArrayList(cp.length);
 		for (int i = 0; i < cp.length; i++) {
 
 			IFile file = null;
 			try {
 				file = p.getFile(cp[i]);
 			} catch (IllegalArgumentException invalidPath) {
 				continue;
 			}
 			if (file.exists())
 				paths.add(file.getFullPath());
 		}
 		if (!paths.isEmpty())
 			element.setImportedJarPaths(paths);
 	}
 
 	protected void setProjectValues(ClasspathElement element, IVirtualReference referencedArchive) {
 		IProject p = referencedArchive.getReferencedComponent().getProject();
 		if (p == null)
 			return;
 
 		element.setProject(p);
 		
 		IVirtualComponent comp = ComponentCore.createComponent(p);
 		if( comp == null )
 			return;
 		
 		//Handle the imported jars in the project
 		String[] cp = null;
 		try {
 //			cp = referencedArchive.getManifest().getClassPathTokenized();
 			ArchiveManifest referencedManifest = null;
 			
 			if( comp.isBinary() ){
 				referencedManifest = J2EEProjectUtilities.readManifest(comp);
 			}else{
 				referencedManifest = J2EEProjectUtilities.readManifest(p);
 			}
 			if( referencedManifest != null )
 				cp = referencedManifest.getClassPathTokenized();
 		} catch (ManifestException mfEx) {
 			J2EEPlugin.logError(mfEx);
 			cp = new String[]{};
 		}
 		if( cp != null ){
 			List paths = new ArrayList(cp.length);
 			for (int i = 0; i < cp.length; i++) {
 	
 				IFile file = null;
 				try {
 					file = p.getFile(cp[i]);
 				} catch (IllegalArgumentException invalidPath) {
 					continue;
 				}
 				if (file.exists())
 					paths.add(file.getFullPath());
 			}
 			if (!paths.isEmpty())
 				element.setImportedJarPaths(paths);
 			}
 	}
 
 	@Override
 	public String toString() {
 		StringBuffer sb = new StringBuffer();
 		for (int i = 0; i < classpathElements.size(); i++) {
 			ClasspathElement element = (ClasspathElement) classpathElements.get(i);
 			if (element.isSelected() && !element.isClasspathDependency() && !element.isClasspathEntry()) {
 				sb.append(element.getRelativeText());
 				sb.append(" "); //$NON-NLS-1$
 			}
 		}
 		//Remove the trailing space
 		if (sb.length() > 0)
 			sb.deleteCharAt(sb.length() - 1);
 		return sb.toString();
 	}
 
 	public void setAllSelected(boolean selected) {
 		setAllSelected(classpathElements, selected);
 	}
 
 	public void setAllSelected(List elements, boolean selected) {
 		for (int i = 0; i < elements.size(); i++) {
 			ClasspathElement elmt = (ClasspathElement) elements.get(i);
 			elmt.setSelected(selected);
 		}
 	}
 
 	/* borrowed code from jdt */
 	protected List moveUp(List elements, List move) {
 		int nElements = elements.size();
 		List res = new ArrayList(nElements);
 		Object floating = null;
 		for (int i = 0; i < nElements; i++) {
 			Object curr = elements.get(i);
 			if (move.contains(curr)) {
 				res.add(curr);
 			} else {
 				if (floating != null) {
 					res.add(floating);
 				}
 				floating = curr;
 			}
 		}
 		if (floating != null) {
 			res.add(floating);
 		}
 		return res;
 	}
 
 	/* borrowed code from jdt */
 	public void moveUp(List toMoveUp) {
 		setModifiedIfAnySelected(toMoveUp);
 		if (toMoveUp.size() > 0)
 			classpathElements = moveUp(classpathElements, toMoveUp);
 	}
 
 	/* borrowed code from jdt */
 	public void moveDown(List toMoveDown) {
 		setModifiedIfAnySelected(toMoveDown);
 		if (toMoveDown.size() > 0)
 			classpathElements = reverse(moveUp(reverse(classpathElements), toMoveDown));
 
 	}
 
 	/* borrowed code from jdt */
 	protected List reverse(List p) {
 		List reverse = new ArrayList(p.size());
 		for (int i = p.size() - 1; i >= 0; i--) {
 			reverse.add(p.get(i));
 		}
 		return reverse;
 	}
 
 	public ClasspathElement getClasspathElement(String uri) {
 		if (urisToElements == null)
 			return null;
 		return (ClasspathElement) urisToElements.get(uri);
 	}
 
 	public ClasspathElement getClasspathElement(IVirtualComponent archiveComponent) {
 		if (archiveComponent != null) {
 			for (int i = 0; i < classpathElements.size(); i++) {
 				ClasspathElement elmnt = (ClasspathElement) classpathElements.get(i);
 				if (archiveComponent.equals(elmnt.getComponent()))
 					return elmnt;
 			}
 		}
 		return null;
 	}
 
 	public ClasspathElement getClasspathElement(IProject archiveProject) {
 		if (archiveProject != null) {
 			for (int i = 0; i < classpathElements.size(); i++) {
 				ClasspathElement elmnt = (ClasspathElement) classpathElements.get(i);
 				if (archiveProject.equals(elmnt.getProject()))
 					return elmnt;
 			}
 		}
 		return null;
 	}
 
 	public boolean hasDirectOrIndirectDependencyTo(IProject archiveProject) {
 		ClasspathElement element = getClasspathElement(archiveProject);
 		if (element == null)
 			return false;
 		Archive anArchive = null;
 		if (element.isValid()) {
 			try {
 				anArchive = (Archive) getEARFile().getFile(element.getText());
 			} catch (FileNotFoundException e) {
 			}
 		}
 		return anArchive != null && archive.hasClasspathVisibilityTo(anArchive);
 	}
 
 	public boolean hasDirectOrIndirectDependencyTo(String jarName) {
 		ClasspathElement element = getClasspathElement(jarName);
 		if (element == null)
 			return false;
 		Archive anArchive = null;
 		if (element.isValid()) {
 			try {
				EARFile earFile = getEARFile();
				if( earFile != null ){
					anArchive = (Archive) earFile.getFile(element.getText());
				}
 			} catch (FileNotFoundException e) {
 			}
 		}
 		return anArchive != null && archive.hasClasspathVisibilityTo(anArchive);
 	}
 
 
 	public boolean isAnyJarSelected(int type) {
 		if (classpathElements != null) {
 			for (int i = 0; i < classpathElements.size(); i++) {
 				ClasspathElement element = (ClasspathElement) classpathElements.get(i);
 				if (element.getJarType() == type && element.isSelected())
 					return true;
 			}
 		}
 		return false;
 	}
 
 
 	public boolean isAnyEJBJarSelected() {
 		return isAnyJarSelected(ClasspathElement.EJB_JAR);
 	}
 
 
 
 	public boolean isAnyEJBClientJARSelected() {
 		return isAnyJarSelected(ClasspathElement.EJB_CLIENT_JAR);
 
 	}
 
 	/**
 	 * @return
 	 */
 	public int getFilterLevel() {
 		return filterLevel;
 	}
 
 	/**
 	 * @param i
 	 */
 	public void setFilterLevel(int i) {
 		filterLevel = i;
 	}
 
 	/**
 	 * This method selects or deselects indivual elements based on the filter level, and
 	 * additionally sets the filter level.
 	 * 
 	 * @param i
 	 */
 	public void selectFilterLevel(int level) {
 		setFilterLevel(level);
 		switch (level) {
 			case FILTER_EJB_CLIENT_JARS :
 				invertClientJARSelections(ClasspathElement.EJB_CLIENT_JAR);
 				break;
 			case FILTER_EJB_SERVER_JARS :
 				invertClientJARSelections(ClasspathElement.EJB_JAR);
 				break;
 			default :
 				break;
 		}
 	}
 
 	public void invertClientJARSelection(IProject aProject, IProject opposite) {
 		ClasspathElement element = getClasspathElement(aProject);
 		ClasspathElement oppositeElement = (opposite == null ? null : getClasspathElement(opposite));
 		if (element.isSelected())
 			invertSelectionIfPossible(element, oppositeElement);
 	}
 
 	private void invertClientJARSelections(int elementType) {
 		if (classpathElements == null)
 			return;
 
 		for (int i = 0; i < classpathElements.size(); i++) {
 			ClasspathElement element = (ClasspathElement) classpathElements.get(i);
 			if (element.getJarType() == elementType && element.isSelected()) {
 				invertSelectionIfPossible(element, null);
 			}
 		}
 	}
 
 	/**
 	 * @param element
 	 * @param elementType
 	 */
 	private void invertSelectionIfPossible(ClasspathElement element, ClasspathElement opposite) {
 		if (element == null)
 			return;
 		ClasspathElement innerOpposite = opposite;
 		if (innerOpposite == null)
 			innerOpposite = getOppositeElement(element);
 		if (innerOpposite != null) {
 			innerOpposite.setSelected(true);
 			element.setSelected(false);
 		}
 	}
 
 	/**
 	 * If the element represents an EJB client JAR, returns the corresponding server JAR. If the
 	 * element represents an EJB server JAR, returns the corresponding client JAR.
 	 */
 	public ClasspathElement getOppositeElement(ClasspathElement element) {
 		String uri = element.getText();
 		IVirtualComponent target = element.getTargetComponent();
 		if (uri == null || target == null)
 			return null;
 		IVirtualComponent oppositeJAR = null;
 		switch (element.getJarType()) {
 			case (ClasspathElement.EJB_CLIENT_JAR) :
 				oppositeJAR = (IVirtualComponent) clientToEJBJARs.get(target);
 				break;
 			case (ClasspathElement.EJB_JAR) :
 				oppositeJAR = (IVirtualComponent) ejbToClientJARs.get(target);
 				break;
 			default :
 				break;
 		}
 		if (oppositeJAR != null)
 			return getClasspathElement(oppositeJAR);
 
 		return null;
 	}
 
 	private void setModifiedIfAnySelected(List elements) {
 		for (int i = 0; i < elements.size(); i++) {
 			ClasspathElement element = (ClasspathElement) elements.get(i);
 			if (element.isSelected())
 				setModified(true);
 		}
 	}
 
 	public boolean isMyClientJAR(ClasspathElement element) {
 		if (element == null || ejbToClientJARs == null)
 			return false;
 		IVirtualComponent myClientJar = (IVirtualComponent) ejbToClientJARs.get(component);
 		return myClientJar != null && myClientJar == element.getTargetComponent();
 	}
 
 	public Map getUrisToElements() {
 		if(urisToElements == null)
 			urisToElements = new HashMap();
 		return urisToElements;
 	}
 
 }
