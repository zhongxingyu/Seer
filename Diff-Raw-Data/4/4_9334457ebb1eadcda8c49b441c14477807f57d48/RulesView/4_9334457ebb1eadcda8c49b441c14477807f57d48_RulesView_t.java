 package org.drools.ide.view.rules;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 
 import org.drools.ide.DRLInfo;
 import org.drools.ide.DroolsIDEPlugin;
 import org.drools.ide.core.DroolsElement;
 import org.drools.ide.core.DroolsModelBuilder;
 import org.drools.ide.core.Function;
 import org.drools.ide.core.Global;
 import org.drools.ide.core.Package;
 import org.drools.ide.core.Query;
 import org.drools.ide.core.Rule;
 import org.drools.ide.core.RuleSet;
 import org.drools.ide.core.Template;
 import org.drools.ide.core.ui.DroolsContentProvider;
 import org.drools.ide.core.ui.DroolsLabelProvider;
 import org.drools.ide.core.ui.DroolsTreeSorter;
 import org.drools.ide.core.ui.FilterActionGroup;
 import org.drools.lang.descr.FactTemplateDescr;
 import org.drools.lang.descr.FunctionDescr;
 import org.drools.lang.descr.QueryDescr;
 import org.drools.lang.descr.RuleDescr;
 import org.eclipse.core.resources.IFile;
 import org.eclipse.core.resources.IProject;
 import org.eclipse.core.resources.IResource;
 import org.eclipse.core.resources.IResourceChangeEvent;
 import org.eclipse.core.resources.IResourceChangeListener;
 import org.eclipse.core.resources.IResourceDelta;
 import org.eclipse.core.resources.IResourceDeltaVisitor;
 import org.eclipse.core.resources.IResourceVisitor;
 import org.eclipse.core.resources.ResourcesPlugin;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.jdt.core.IJavaProject;
 import org.eclipse.jdt.core.JavaCore;
 import org.eclipse.jface.viewers.DoubleClickEvent;
 import org.eclipse.jface.viewers.IDoubleClickListener;
 import org.eclipse.jface.viewers.ISelection;
 import org.eclipse.jface.viewers.IStructuredSelection;
 import org.eclipse.jface.viewers.StructuredSelection;
 import org.eclipse.jface.viewers.TreeViewer;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.ui.IEditorPart;
 import org.eclipse.ui.IMemento;
 import org.eclipse.ui.IViewSite;
 import org.eclipse.ui.PartInitException;
 import org.eclipse.ui.forms.editor.FormEditor;
 import org.eclipse.ui.ide.IDE;
 import org.eclipse.ui.part.ViewPart;
 import org.eclipse.ui.texteditor.ITextEditor;
 
 public class RulesView extends ViewPart implements IDoubleClickListener, IResourceVisitor, IResourceChangeListener {
 
 	private final RuleSet ruleSet = DroolsModelBuilder.createRuleSet();
 
 	private Map resourcesMap = new HashMap();
 	private TreeViewer treeViewer;
 	
 	public void createPartControl(Composite parent) {
 		treeViewer = new TreeViewer(parent, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL);
 		treeViewer.setContentProvider(new DroolsContentProvider());
 		treeViewer.setLabelProvider(new DroolsLabelProvider());
 		treeViewer.setSorter(new DroolsTreeSorter());
 		treeViewer.addDoubleClickListener(this);
 		treeViewer.setUseHashlookup(true);
 		treeViewer.setInput(ruleSet);
 		FilterActionGroup filterActionGroup = new FilterActionGroup(
 			treeViewer, "org.drools.ide.view.rules.RulesView");
 		filterActionGroup.fillActionBars(getViewSite().getActionBars());
 	}
 	
 	public void init(IViewSite site, IMemento memento) throws PartInitException {
 		super.init(site, memento);
 		try {
 			ResourcesPlugin.getWorkspace().getRoot().accept(this);
 		} catch (CoreException e) {
 			DroolsIDEPlugin.log(e);
 		}
 		ResourcesPlugin.getWorkspace().addResourceChangeListener(this);
 	}
 	
	public void dispose() {
		ResourcesPlugin.getWorkspace().removeResourceChangeListener(this);
	}
	
 	public void setFocus() {
 		treeViewer.getControl().setFocus();
 	}
 
 	public boolean visit(IResource resource) throws CoreException {
 		return updateResource(resource);
 	}
 	
 	private boolean updateResource(IResource resource) {
     	IProject project = resource.getProject();
     	if (project != null) {
             IJavaProject javaProject = JavaCore.create(project);
             if (!javaProject.exists()) {
             	return false;
             }
     		if (resource instanceof IFile 
     				&& "drl".equals(resource.getFileExtension())
     				&& javaProject.isOnClasspath(resource)) {
     			try {
     				IFile file = (IFile) resource;
     				DRLInfo drlInfo = DroolsIDEPlugin.getDefault().parseResource(resource, false);
     				String packageName = drlInfo.getPackageName();
     				Package pkg = ruleSet.getPackage(packageName);
     				if (pkg == null) {
     					pkg = DroolsModelBuilder.addPackage(ruleSet, packageName, 0, 0);
     				}
     				// add rules
     				List rules = drlInfo.getPackageDescr().getRules();
     				for (Iterator iterator = rules.iterator(); iterator.hasNext();) {
     					RuleDescr ruleDescr = (RuleDescr) iterator.next();
     					boolean isQuery = ruleDescr instanceof QueryDescr;
     					String ruleName = ruleDescr.getName();
     					if (!isQuery) {
     						Rule rule = DroolsModelBuilder.addRule(pkg, ruleName, file, 0, 0, null);
 	    					// create link between resource and created rule nodes
 	    					List droolsElements = (List) resourcesMap.get(file);
 	    					if (droolsElements == null) {
 	    						droolsElements = new ArrayList();
 	    						resourcesMap.put(file, droolsElements);
 	    					}
 	    					droolsElements.add(rule);
     					} else {
     						Query query = DroolsModelBuilder.addQuery(pkg, ruleName, file, 0, 0);
 	    					// create link between resource and created rule nodes
 	    					List droolsElements = (List) resourcesMap.get(file);
 	    					if (droolsElements == null) {
 	    						droolsElements = new ArrayList();
 	    						resourcesMap.put(file, droolsElements);
 	    					}
 	    					droolsElements.add(query);
     					}
     				}
     				// add templates
     				List templates = drlInfo.getPackageDescr().getFactTemplates();
     				for (Iterator iterator = templates.iterator(); iterator.hasNext();) {
     					String templateName = ((FactTemplateDescr) iterator.next()).getName();
 						Template template = DroolsModelBuilder.addTemplate(pkg, templateName, file, 0, 0);
     					// create link between resource and created rule nodes
     					List droolsElements = (List) resourcesMap.get(file);
     					if (droolsElements == null) {
     						droolsElements = new ArrayList();
     						resourcesMap.put(file, droolsElements);
     					}
     					droolsElements.add(template);
     				}
     				// add globals
     				Map globals = drlInfo.getPackageDescr().getGlobals();
     				for (Iterator iterator = globals.keySet().iterator(); iterator.hasNext();) {
     					String globalName = (String) iterator.next();
     					Global global = DroolsModelBuilder.addGlobal(pkg, globalName, file, 0, 0);
     					// create link between resource and created rule nodes
     					List droolsElements = (List) resourcesMap.get(file);
     					if (droolsElements == null) {
     						droolsElements = new ArrayList();
     						resourcesMap.put(file, droolsElements);
     					}
     					droolsElements.add(global);
     				}
     				// add functions
     				List functions = drlInfo.getPackageDescr().getFunctions();
     				for (Iterator iterator = functions.iterator(); iterator.hasNext();) {
     					String functionName = ((FunctionDescr) iterator.next()).getName();
     					Function function = DroolsModelBuilder.addFunction(pkg, functionName, file, 0, 0);
     					// create link between resource and created rule nodes
     					List droolsElements = (List) resourcesMap.get(file);
     					if (droolsElements == null) {
     						droolsElements = new ArrayList();
     						resourcesMap.put(file, droolsElements);
     					}
     					droolsElements.add(function);
     				}
     			} catch (Throwable t) {
     				DroolsIDEPlugin.log(t);
     			}
     			return false;
     		}
         }
         return true;
 	}
 
 	public void resourceChanged(final IResourceChangeEvent event) {
 		try {
 			if (event.getType() == IResourceChangeEvent.POST_CHANGE) {
 				IResourceDelta delta = event.getDelta();
 				if (delta != null) {
 					delta.accept(new IResourceDeltaVisitor() {
 						public boolean visit(IResourceDelta delta) throws CoreException {
 							IResource resource = delta.getResource();
 							removeElementsFromResource(resource);
 							boolean result = true;
 							if (delta.getKind() != IResourceDelta.REMOVED) {
 								result = updateResource(resource);
 							}
 							treeViewer.getControl().getDisplay().asyncExec(
 						        new Runnable() {
 									public void run() {
 										treeViewer.refresh();
 									}
 								}
 					        );
 							return result;
 						}
 					});
 				}
 			} else if (event.getType() == IResourceChangeEvent.PRE_DELETE) {
 				IResource resource = event.getResource();
 				if (resource != null) {
 					resource.accept(new IResourceVisitor() {
 						public boolean visit(IResource resource) throws CoreException {
 							removeElementsFromResource(resource);
 							return true;
 						}
 					});
 				}
 			} else if (event.getType() == IResourceChangeEvent.PRE_CLOSE) {
 				IResource resource = event.getResource();
 				if (resource != null) {
 					resource.accept(new IResourceVisitor() {
 						public boolean visit(IResource resource) throws CoreException {
 							removeElementsFromResource(resource);
 							return true;
 						}
 					});
 				}
 			}
 		} catch (Throwable t) {
 			DroolsIDEPlugin.log(t);
 		}
 	}
 	
 	private void removeElementsFromResource(IResource resource) {
 		List droolsElements = (List) resourcesMap.get(resource);
 		if (droolsElements != null) {
 			for (Iterator iterator = droolsElements.iterator(); iterator.hasNext();) {
 				DroolsModelBuilder.removeElement((DroolsElement) iterator.next());
 			}
 			resourcesMap.remove(resource);
 		}
 	}
 
 	public void doubleClick(DoubleClickEvent event) {
 		ISelection selection = event.getSelection();
 		if (selection instanceof IStructuredSelection) {
 			Object selected = ((StructuredSelection) selection).getFirstElement();
 			if (selected != null && selected instanceof DroolsElement) {
 				DroolsElement droolsSelected = (DroolsElement) selected;
 				IFile file = droolsSelected.getFile();
 				if (file != null) {
 					try {
 						IEditorPart editor = IDE.openEditor(getSite().getPage(), file);
 						if (editor instanceof FormEditor) {
 							editor = ((FormEditor) editor).getActiveEditor();
 						}
 						if (editor instanceof ITextEditor) {
 							((ITextEditor)editor).selectAndReveal(
 								droolsSelected.getOffset(), droolsSelected.getLength());
 						}
 					} catch (Throwable t) {
 						DroolsIDEPlugin.log(t);
 					}
 				}
 			}
 		}
 	}
 
 }
