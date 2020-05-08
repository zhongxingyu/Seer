 package edu.uci.lighthouse.ui.views.actions;
 
 import org.apache.log4j.Logger;
 import org.eclipse.core.resources.IProject;
 import org.eclipse.core.resources.IWorkspace;
 import org.eclipse.core.resources.ResourcesPlugin;
 import org.eclipse.draw2d.IFigure;
 import org.eclipse.jdt.core.IJavaElement;
 import org.eclipse.jdt.core.IJavaModel;
 import org.eclipse.jdt.core.IJavaProject;
 import org.eclipse.jdt.core.IType;
 import org.eclipse.jdt.core.JavaCore;
 import org.eclipse.jdt.core.JavaModelException;
 import org.eclipse.jdt.ui.JavaUI;
 import org.eclipse.jface.action.Action;
 import org.eclipse.jface.action.ContributionItem;
 import org.eclipse.swt.events.MouseEvent;
 import org.eclipse.swt.events.MouseListener;
 import org.eclipse.ui.IEditorPart;
 import org.eclipse.ui.PartInitException;
 import org.eclipse.ui.plugin.AbstractUIPlugin;
 import org.eclipse.zest.core.widgets.IContainer;
 
 import edu.uci.lighthouse.model.LighthouseClass;
 import edu.uci.lighthouse.model.LighthouseEntity;
 import edu.uci.lighthouse.model.LighthouseField;
 import edu.uci.lighthouse.model.LighthouseMethod;
 import edu.uci.lighthouse.model.LighthouseModel;
 import edu.uci.lighthouse.model.LighthouseModelManager;
 import edu.uci.lighthouse.ui.graph.UmlClassFigureTest;
 
 public class OpenInEditorAction extends ContributionItem implements MouseListener {
 	
 	protected IContainer container;
 	
 	private static Logger logger = Logger.getLogger(OpenInEditorAction.class);
 	
 	private static final String ICON = "icons/full/obj16/jcu_obj.gif";
 	private static final String DESCRIPTION = "Open in the editor";
 	
 	public OpenInEditorAction(IContainer container) {
 		//super(new Action(){});
 		this.container = container;
 		container.getGraph().addMouseListener(this);
 	}
 
 	@Override
 	public void dispose() {	
 		logger.info("dispose()");
 		container.getGraph().removeMouseListener(this);
 		super.dispose();
 	}
 
 	private final class OpenInEditorAction2 extends Action{
 		public OpenInEditorAction2(){
 			init();
 		}
 		private void init() {
 			setToolTipText(DESCRIPTION);
 			setImageDescriptor(AbstractUIPlugin.imageDescriptorFromPlugin(
 					"org.eclipse.jdt.ui", ICON));
 		}		
 	}
 	
 	private void showInEditor(LighthouseEntity e) {
 		LighthouseModelManager manager = new LighthouseModelManager(LighthouseModel
 				.getInstance());
 		LighthouseClass c = manager.getMyClass(e);
 		if (c != null) {
 			try {
 				IWorkspace workspace = ResourcesPlugin.getWorkspace();
 				IProject project = workspace.getRoot().getProject(c.getProjectName());
 				IJavaProject javaProject = JavaCore.create(project);
 				String classFqn = c.getFullyQualifiedName().replace(c.getProjectName()+".","");
				classFqn = classFqn.replaceAll("\\$.*", "");
 				IType type = javaProject.findType(classFqn);
 				if (type != null) {
 					IJavaElement target = (IJavaElement) type;
 					IJavaElement[] elements = new IJavaElement[0];
 					if (e instanceof LighthouseMethod) {
 						elements = type.getMethods();
 					} else if (e instanceof LighthouseField) {
 						elements = type.getFields();
 					} 
 					String shortName = e.getShortName();
 					if (shortName.contains("<init>")){
 						shortName = shortName.replace("<init>", c.getShortName());
 					}
 					shortName = shortName.replaceAll("[\\<\\(].*", "");
 					for (IJavaElement element : elements){
						logger.debug("shortname:"+shortName + " java:"+element.getElementName());
 						if (element.getElementName().equals(shortName)){
 							target = element;
 							break;
 						}
 					}
 					IEditorPart javaEditor = JavaUI
 					.openInEditor(target);
 				}
 			} catch (Exception ex) {
 				logger.error(ex,ex);
 			}
 		}
 	}
 	
 	private void showInEditor2(LighthouseEntity e) {
 		LighthouseModelManager manager = new LighthouseModelManager(LighthouseModel
 				.getInstance());
 		LighthouseClass c = manager.getMyClass(e);
 		if (c != null) {
 			try {
 				IWorkspace workspace = ResourcesPlugin.getWorkspace();
 				IJavaModel javaModel = JavaCore.create(workspace.getRoot());
 				IJavaProject[] projects = javaModel.getJavaProjects();
 				for (int i = 0; i < projects.length; i++) {
 					IType type = projects[i].findType(c.getFullyQualifiedName());
 					if (type != null && type instanceof IJavaElement) {
 						IJavaElement element = (IJavaElement) type;
 
 						IJavaElement[] elements;
 						if (e instanceof LighthouseMethod) {
 							elements = type.getMethods();
 						} else if (e instanceof LighthouseField) {
 							elements = type.getFields();
 						} else {
 							elements = new IJavaElement[0];
 						}
 						for (int j = 0; j < elements.length; j++) {
 							if (elements[j].toString().replaceAll(" ", "")
 									.indexOf(e.getShortName()) != -1) {
 								element = elements[j];
 								break;
 							}
 						}
 						IEditorPart javaEditor = JavaUI
 								.openInEditor(element);
 						break;
 					}
 				}
 			} catch (JavaModelException ex) {
 				ex.printStackTrace();
 			} catch (PartInitException ex) {
 				ex.printStackTrace();
 			}
 		}
 	}
 
 	@Override
 	public void mouseDoubleClick(MouseEvent e) {
 		IFigure fig = container.getGraph().getFigureAt(e.x, e.y);
 		if (fig instanceof UmlClassFigureTest){
 			UmlClassFigureTest classFig = (UmlClassFigureTest)fig;
 			LighthouseEntity entity = classFig.findLighthouseEntityAt(e.x, e.y);
 			if (entity != null){
 				showInEditor(entity);
 			}
 		}
 	}
 
 	@Override
 	public void mouseDown(MouseEvent e) {
 	}
 
 	@Override
 	public void mouseUp(MouseEvent e) {
 	}
 }
