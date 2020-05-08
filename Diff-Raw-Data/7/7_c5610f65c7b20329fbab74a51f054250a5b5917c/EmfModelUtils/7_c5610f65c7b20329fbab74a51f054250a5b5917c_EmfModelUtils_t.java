 /*
  * File EmfModelUtil.java.
  * Created by Donat Csikos<dcsikos@cern.ch> at Jul 5, 2012.
  *
  * Copyright CERN 2012, All Rights Reserved.
  */
 package cern.devtools.depanalysis.modelfinder;
 
 import java.util.Iterator;
 import java.util.LinkedList;
 import java.util.List;
 
 import org.eclipse.emf.common.util.EList;
 import org.eclipse.emf.ecore.EObject;
 
 import cern.devtools.depanalysis.javamodel.ApiClass;
 import cern.devtools.depanalysis.javamodel.Dependency;
 import cern.devtools.depanalysis.javamodel.DependencyType;
 import cern.devtools.depanalysis.javamodel.Field;
 import cern.devtools.depanalysis.javamodel.JavaModelFactory;
 import cern.devtools.depanalysis.javamodel.JavaModelPackage;
 import cern.devtools.depanalysis.javamodel.Method;
 import cern.devtools.depanalysis.javamodel.NamedElement;
 import cern.devtools.depanalysis.javamodel.Package;
 import cern.devtools.depanalysis.javamodel.Project;
 import cern.devtools.depanalysis.javamodel.Workspace;
 
 public class EmfModelUtils {
 
 	@SuppressWarnings("unchecked")
 	public static <T extends NamedElement> T createNamedElement(int type, Workspace workspace, EObject container,
 			String handler, String name, Object... data) {
 		T newElement = null;
 
 		switch (type) {
 		case JavaModelPackage.PROJECT:
 			newElement = (T) JavaModelFactory.eINSTANCE.createProject();
 			workspace.getProjects().add((Project) newElement);
 			break;
 
 		case JavaModelPackage.PACKAGE:
 			newElement = (T) JavaModelFactory.eINSTANCE.createPackage();
 			((Project) container).getPackages().add((Package) newElement);
 			break;
 
 		case JavaModelPackage.API_CLASS:
 			newElement = (T) JavaModelFactory.eINSTANCE.createApiClass();
 			newElement.setData(data[0]);
 			((Package) container).getClasses().add((ApiClass) newElement);
 			break;
 
 		case JavaModelPackage.METHOD:
 			newElement = (T) JavaModelFactory.eINSTANCE.createMethod();
 			((ApiClass) container).getMethods().add((Method) newElement);
 			break;
 
 		case JavaModelPackage.FIELD:
 			newElement = (T) JavaModelFactory.eINSTANCE.createField();
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
 
 	public static void deleteNamedElement(Workspace workspace, NamedElement elem) {
 		if (elem instanceof Project) {
 			Project project = (Project) elem;
 			workspace.getProjects().remove(project);
 		} else if (elem instanceof Package) {
 			Package pkg = (Package) elem;
 			pkg.getProject().getPackages().remove(pkg);
 		} else if (elem instanceof ApiClass) {
 			ApiClass ac = (ApiClass) elem;
 			ac.getPackage().getClasses().remove(ac);
 		} else if (elem instanceof Method) {
 			Method m = (Method) elem;
 			m.getClass_().getMethods().remove(m);
		} 
		else if (elem instanceof Field) {
			Field f = (Field) elem;
			f.getClass_().getFields().remove(f);
		}
		else {
 			throw new RuntimeException("Cannot delete this item: " + elem);
 		}
 	}
 
 	public static Dependency createDependency(Workspace workspace, NamedElement from, NamedElement to,
 			DependencyType type) {
 		if (dependencyExists(from, to, type)) {
 			return null;
 		}
 
 		Dependency d = JavaModelFactory.eINSTANCE.createDependency();
 		d.setType(type);
 		d.setFrom(from);
 		d.setTo(to);
 		workspace.getDependencties().add(d);
 		return d;
 	}
 
 	private static boolean dependencyExists(NamedElement emfFrom, NamedElement emfTo, DependencyType type) {
 		for (Dependency d : emfFrom.getOutgoingDependencies()) {
 			if (d.getTo().equals(emfTo) && d.getType().equals(type)) {
 				return true;
 			}
 		}
 		return false;
 	}
 
 	public static void deleteAllDependencies(Workspace workspace, NamedElement elem) {
 		deleteDependencies(workspace, elem.getOutgoingDependencies());
 		deleteDependencies(workspace, elem.getIncomingDependencies());
 	}
 	
 	public static void deleteOutgoingDependencies(Workspace workspace, NamedElement elem) {
 		deleteDependencies(workspace, elem.getOutgoingDependencies());
 	}
 
 	private static void deleteDependencies(Workspace workspace, List<Dependency> depsToDelete) {
 		for (Dependency d : depsToDelete) {
 			workspace.getDependencties().remove(d);
 		}
 		
 		LinkedList<Pair<Dependency, EList<Dependency>>> depList = new LinkedList<Pair<Dependency, EList<Dependency>>>();
 
 		Iterator<Dependency> iter = depsToDelete.iterator();
 		while (iter.hasNext()) {
 			Dependency d = iter.next();
 			depList.add(Pair.newInstance(d, d.getFrom().getOutgoingDependencies()));
 			depList.add(Pair.newInstance(d, d.getTo().getIncomingDependencies()));
 		}
 
 		for (Pair<Dependency, EList<Dependency>> p : depList) {
 			p.getSecond().remove(p.getFirst());
 		}
 	}
 	
 	/**
 	 * Testing remove dependency functionality.
 	 */
 	public static void main(String[] args) {
 		
 		Workspace w = JavaModelFactory.eINSTANCE.createWorkspace();
 		Project p1 = JavaModelFactory.eINSTANCE.createProject();
 		Project p2 = JavaModelFactory.eINSTANCE.createProject();
 		w.getProjects().add(p1);
 		w.getProjects().add(p2);
 		createDependency(w, p1, p2, DependencyType.METHOD_CALL);
 		
 		System.out.println("ws " + w.getDependencties().size());
 		System.out.println("pi " + p1.getIncomingDependencies().size());
 		System.out.println("po " + p1.getOutgoingDependencies().size());
 		
 		deleteAllDependencies(w, p1);
 		
 		System.out.println("ws " + w.getDependencties().size());
 		System.out.println("pi " + p1.getIncomingDependencies().size());
 		System.out.println("po " + p1.getOutgoingDependencies().size());
 		
 	}
 }
