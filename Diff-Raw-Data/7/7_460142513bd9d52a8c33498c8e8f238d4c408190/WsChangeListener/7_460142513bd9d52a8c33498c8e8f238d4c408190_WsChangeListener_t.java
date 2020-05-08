 /*
  * File WsChangeListener.java.
  * Created by Donat Csikos<dcsikos@cern.ch> at 25 May 2012.
  */
 package cern.devtools.depanalysis.modelfinder;
 
 import java.util.logging.Logger;
 
 import org.eclipse.core.resources.IProject;
 import org.eclipse.core.resources.IWorkspace;
 import org.eclipse.core.resources.IWorkspaceRoot;
 import org.eclipse.core.resources.ResourcesPlugin;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.emf.ecore.EObject;
 import org.eclipse.jdt.core.ElementChangedEvent;
 import org.eclipse.jdt.core.ICompilationUnit;
 import org.eclipse.jdt.core.IElementChangedListener;
 import org.eclipse.jdt.core.IField;
 import org.eclipse.jdt.core.IJavaElement;
 import org.eclipse.jdt.core.IJavaElementDelta;
 import org.eclipse.jdt.core.IJavaProject;
 import org.eclipse.jdt.core.IMethod;
 import org.eclipse.jdt.core.IPackageFragment;
 import org.eclipse.jdt.core.IPackageFragmentRoot;
 import org.eclipse.jdt.core.IType;
 import org.eclipse.jdt.core.JavaCore;
 import org.eclipse.jdt.core.JavaModelException;
 
 import cern.devtools.depanalysis.javamodel.ApiClass;
 import cern.devtools.depanalysis.javamodel.Field;
 import cern.devtools.depanalysis.javamodel.JavaModelFactory;
 import cern.devtools.depanalysis.javamodel.JavaModelPackage;
 import cern.devtools.depanalysis.javamodel.Method;
 import cern.devtools.depanalysis.javamodel.NamedElement;
 import cern.devtools.depanalysis.javamodel.Package;
 import cern.devtools.depanalysis.javamodel.Project;
 import cern.devtools.depanalysis.javamodel.Workspace;
 
 public class WsChangeListener implements IElementChangedListener {
 
 	/**
 	 * Logger.
 	 */
 	private static Logger LOG = Logger.getLogger(WsChangeListener.class.getCanonicalName());
 
 	/**
 	 * EMF model to expose.
 	 */
 	private Workspace workspace;
 
 	public WsChangeListener() {
 		reset();
 	}
 
 	@Override
 	public void elementChanged(ElementChangedEvent event) {
 		try {
 			IJavaElementDelta delta = event.getDelta();
 			traverseJavaModelRecursive(delta);
 			printModel(this.workspace);
 		} catch (CoreException e) {
 			LOG.warning("Error on model change. Desc: " + e.getMessage());
 			LOG.info("Rebuilding entire model...");
 			reset();
 		}
 	}
 
 	public Workspace getWorkspaceModel() {
 		return workspace;
 	}
 
 	public void reset() {
 		try {
 			// Initialise the model object
 			workspace = JavaModelFactory.eINSTANCE.createWorkspace();
 			// load existing project information from the workspace
 			loadInitialState();
 			// print the initial state
 			printModel(workspace);
 		} catch (Throwable e) {
 			LOG.warning("Error on reset. Desc: " + e.getMessage());
 		}
 	}
 
 	private NamedElement createNamedElement(int type, EObject container, String handler, String name)
 			throws JavaModelException {
 		NamedElement newElement = null;
 
 		// instantiation
 		switch (type) {
 		case JavaModelPackage.PROJECT:
 			newElement = JavaModelFactory.eINSTANCE.createProject();
 			workspace.getProjects().add((Project) newElement);
 			break;
 
 		case JavaModelPackage.PACKAGE:
 			newElement = JavaModelFactory.eINSTANCE.createPackage();
 			((Project) container).getPackages().add((Package) newElement);
 			break;
 
 		case JavaModelPackage.API_CLASS:
 			newElement = JavaModelFactory.eINSTANCE.createApiClass();
 			((Package) container).getClasses().add((ApiClass) newElement);
 			break;
 
 		case JavaModelPackage.METHOD:
 			newElement = JavaModelFactory.eINSTANCE.createMethod();
 			((ApiClass) container).getMethods().add((Method) newElement);
 			break;
 
 		case JavaModelPackage.FIELD:
 			newElement = JavaModelFactory.eINSTANCE.createField();
 			((ApiClass) container).getFields().add((Field) newElement);
 			break;
 
 		default:
 			throw new RuntimeException("Not vaid type specified (not present in JavaModelPackage).");
 		}
 
 		// set common attributes
 		newElement.setName(name);
 		newElement.setHandler(handler);
 
 		// set workspace reference and return
 		workspace.getElements().add(newElement);
 		return newElement;
 	}
 
 	private NamedElement find(String handle) {
 		for (NamedElement elem : workspace.getElements()) {
 			if (elem.getHandler().contentEquals(handle)) {
 				return elem;
 			}
 		}
 		return null;
 	}
 
 	private void loadInitialState() throws CoreException {
 		IWorkspace jdtWorkspace = ResourcesPlugin.getWorkspace();
 		IWorkspaceRoot jdtWsRoot = jdtWorkspace.getRoot();
 		IProject[] jdtProjects = jdtWsRoot.getProjects();
 
 		for (IProject jdtProject : jdtProjects) {
 			if (jdtProject.exists() && jdtProject.isOpen()
 					&& jdtProject.isNatureEnabled("org.eclipse.jdt.core.javanature")) {
 				IJavaProject jdtJavaProject = JavaCore.create(jdtProject);
 
 				// add existing projects to the model
 				Project emfProject = (Project) createNamedElement(JavaModelPackage.PROJECT, workspace,
 						jdtJavaProject.getHandleIdentifier(), jdtJavaProject.getElementName());
 
 				for (IPackageFragment jdtPackage : jdtJavaProject.getPackageFragments()) {
 					if (jdtPackage.getKind() == IPackageFragmentRoot.K_SOURCE) {
 						// add package to the project
 						Package emfPackage = (Package) createNamedElement(JavaModelPackage.PACKAGE, emfProject,
 								jdtPackage.getHandleIdentifier(), jdtPackage.getElementName());
 
 						for (ICompilationUnit cu : jdtPackage.getCompilationUnits()) {
							for (IType jdtType : cu.getAllTypes()) {
 								// add classes to the package
 								ApiClass emfClass = (ApiClass) createNamedElement(JavaModelPackage.API_CLASS,
										emfPackage, jdtType.getHandleIdentifier(), jdtType.getTypeQualifiedName());
 
								
 								for (IMethod jdtMethod : jdtType.getMethods()) {
 									// add methods to the class
 									createNamedElement(JavaModelPackage.METHOD, emfClass,
 											jdtMethod.getHandleIdentifier(), jdtMethod.getElementName());
 								}
 								for (IField jdtField : jdtType.getFields()) {
 									// add fields to the class
 									createNamedElement(JavaModelPackage.FIELD, emfClass,
 											jdtField.getHandleIdentifier(), jdtField.getElementName());
 								}
 							}
 						}
 					}
 				}
 			}
 		}
 	}
 
 	private void traverseJavaModelRecursive(IJavaElementDelta elem) throws CoreException {
 		switch (elem.getKind()) {
 		case IJavaElementDelta.ADDED:
 			addJavaElement(elem.getElement());
 			break;
 		case IJavaElementDelta.REMOVED:
 			removeJavaElement(elem.getElement());
 			break;
 		default:
 
 		}
 
 		for (IJavaElementDelta child : elem.getAffectedChildren()) {
 			traverseJavaModelRecursive(child);
 		}
 	}
 
 	private void addJavaElement(IJavaElement jdtElem) throws CoreException {
 		if (jdtElem instanceof IJavaProject) {
 			createNamedElement(JavaModelPackage.PROJECT, workspace, jdtElem.getHandleIdentifier(),
 					jdtElem.getElementName());
 		} else if (jdtElem instanceof IPackageFragment) {
 			IPackageFragment jdtPackage = (IPackageFragment) jdtElem;
 			Project emfContainer = (Project) find(jdtPackage.getJavaProject().getHandleIdentifier());
 			createNamedElement(JavaModelPackage.PACKAGE, emfContainer, jdtElem.getHandleIdentifier(),
 					jdtPackage.getElementName());
 		} else if (jdtElem instanceof IType) {
 			IType jdtType = (IType) jdtElem;
 			Package emfContainer = (Package) find(jdtType.getPackageFragment().getHandleIdentifier());
 			createNamedElement(JavaModelPackage.API_CLASS, emfContainer, jdtType.getHandleIdentifier(),
 					jdtType.getTypeQualifiedName());
 		} else if (jdtElem instanceof IMethod) {
 			IMethod jdtMethod = (IMethod) jdtElem;
 			ApiClass emfContainer = (ApiClass) find(jdtMethod.getDeclaringType().getHandleIdentifier());
 			createNamedElement(JavaModelPackage.METHOD, emfContainer, jdtMethod.getHandleIdentifier(),
 					jdtMethod.getElementName() + jdtMethod.getSignature());
 		} else if (jdtElem instanceof IField) {
 			IField jdtField = (IField) jdtElem;
 			System.out.println(jdtField.getDeclaringType());
 			
 			ApiClass emfContainer = (ApiClass) find(jdtField.getDeclaringType().getHandleIdentifier());
 			createNamedElement(JavaModelPackage.FIELD, emfContainer, jdtField.getHandleIdentifier(),
 					jdtField.getElementName());
 		}
 	}
 
 	private void removeJavaElement(IJavaElement jdtElem) throws CoreException {
 		NamedElement emfElem = find(jdtElem.getHandleIdentifier());
 		if (jdtElem instanceof IJavaProject) {
 			workspace.getProjects().remove(emfElem);
 		} else if (jdtElem instanceof IPackageFragment) {
 			Package emfPackage = (Package) emfElem;
 			emfPackage.getProject().getPackages().remove(emfPackage);
 		} else if (jdtElem instanceof IType) {
 			ApiClass emfClass = (ApiClass) emfElem;
 			emfClass.getPackage().getClasses().remove(emfClass);
 		} else if (jdtElem instanceof ICompilationUnit) {
 			for (IType jdtType : ((ICompilationUnit) jdtElem).getAllTypes()) {
 				removeJavaElement(jdtType);
 			}
 		} else if (jdtElem instanceof IMethod) {
 			Method emfMethod = (Method) emfElem;
 			emfMethod.getClass_().getMethods().remove(emfMethod);
 		} else if (jdtElem instanceof IField) {
 			Field emfField = (Field) emfElem;
 			emfField.getClass_().getFields().remove(emfField);
 		}
 
 		workspace.getElements().remove(emfElem);
 	}
 
 	public static void printModel(Workspace workspace) {
 		System.out.println("-- workspace structure --");
 		for (Project p : workspace.getProjects()) {
 			System.out.println(p);
 			for (Package pkg : p.getPackages()) {
 				System.out.println("\t" + pkg);
 
 				for (ApiClass ac : pkg.getClasses()) {
 					System.out.println("\t\t" + ac);
 					for (Method m : ac.getMethods()) {
 						System.out.println("\t\t\t" + m);
 					}
 					for (Field f : ac.getFields()) {
 						System.out.println("\t\t\t" + f);
 					}
 				}
 			}
 		}
 	}
 
 }
