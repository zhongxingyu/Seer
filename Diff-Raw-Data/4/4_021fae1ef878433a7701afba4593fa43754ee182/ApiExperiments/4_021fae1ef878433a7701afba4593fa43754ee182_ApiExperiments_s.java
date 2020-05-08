 package de.age.projecttester;
 
 import static org.hamcrest.CoreMatchers.is;
 import static org.hamcrest.CoreMatchers.notNullValue;
 import static org.junit.Assert.*;
 
 import java.util.ArrayList;
 
 import org.eclipse.core.resources.IFile;
 import org.eclipse.core.resources.IProject;
 import org.eclipse.core.resources.IResource;
 import org.eclipse.core.resources.IResourceVisitor;
 import org.eclipse.core.resources.IWorkspace;
 import org.eclipse.core.resources.ResourcesPlugin;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.jdt.core.IJavaElement;
 import org.eclipse.jdt.core.IJavaProject;
 import org.eclipse.jdt.core.JavaCore;
 import org.junit.Test;
 
 public class ApiExperiments {
 
 	@Test
 	public void howToGetWorkspace() {
 		IWorkspace workspace = ResourcesPlugin.getWorkspace();
 		assertThat(workspace, is(notNullValue()));
 	}
 	
 	@Test
 	public void howToGetProjects() throws CoreException {
 		IWorkspace workspace = ResourcesPlugin.getWorkspace();
 		final ArrayList<IProject> projects = new ArrayList<IProject>();
 		workspace.getRoot().accept(new IResourceVisitor() {
 			
 			@Override
 			public boolean visit(IResource resource) throws CoreException {
 				if (resource instanceof IProject) {
 					projects.add((IProject) resource);
 					return false;
 				} else {
 					return true;
 				}
 			}
 		});
 		assertThat(projects.isEmpty(), is(false));
 	}
 	
 	@Test
 	public void howToGetOnlyJavaProjects() throws CoreException {
 		IWorkspace workspace = ResourcesPlugin.getWorkspace();
 		final ArrayList<IProject> projects = new ArrayList<IProject>();
 		workspace.getRoot().accept(new IResourceVisitor() {
 			
 			@Override
 			public boolean visit(IResource resource) throws CoreException {
 				if (resource instanceof IProject) {
 					if (((IProject) resource).hasNature(JavaCore.NATURE_ID))
 						projects.add((IProject) resource);
 					return false;
 				} else {
 					return true;
 				}
 			}
 		});
 		assertThat(projects.size(), is(1));
 	}
 	
 	@Test
 	public void howToGetClasses() throws CoreException {
 		IWorkspace workspace = ResourcesPlugin.getWorkspace();
 		final ArrayList<IJavaElement> javaElements = new ArrayList<IJavaElement>();
 		workspace.getRoot().accept(new IResourceVisitor() {
 			
 			@Override
 			public boolean visit(IResource resource) throws CoreException {
 				if (resource instanceof IProject) {
 					if (((IProject) resource).hasNature(JavaCore.NATURE_ID)) {
 						return true;
 					} else {
 						return false;
 					}
 				} else if (resource instanceof IFile) {
 					IJavaProject project = JavaCore.create((IProject) resource.getProject());
					javaElements.add(project.findElement(resource.getFullPath()));
 					return false;
 				} else {
 					return true;
 				}
 			}
 		});
 		assertThat(javaElements.isEmpty(), is(false));
 	}
 	
 }
