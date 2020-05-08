 package mylynkanban.views;
 
 
 import java.text.SimpleDateFormat;
 import java.util.Calendar;
 import java.util.Collection;
 import java.util.Date;
 import java.util.LinkedList;
 import java.util.Set;
 
 import mylynkanban.Activator;
 
 import org.eclipse.mylyn.context.core.AbstractContextListener;
 import org.eclipse.mylyn.context.core.ContextCore;
 import org.eclipse.mylyn.context.core.IInteractionContext;
 import org.eclipse.mylyn.internal.tasks.core.AbstractTask;
 import org.eclipse.mylyn.internal.tasks.core.AbstractTaskCategory;
 import org.eclipse.mylyn.internal.tasks.core.AbstractTaskContainer;
 import org.eclipse.mylyn.internal.tasks.core.DateRange;
 import org.eclipse.mylyn.internal.tasks.core.ITaskListChangeListener;
 import org.eclipse.mylyn.internal.tasks.core.TaskContainerDelta;
 import org.eclipse.mylyn.internal.tasks.core.TaskList;
 import org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.browser.Browser;
 import org.eclipse.swt.browser.ProgressEvent;
 import org.eclipse.swt.browser.ProgressListener;
 import org.eclipse.swt.layout.GridData;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Display;
 import org.eclipse.ui.part.ViewPart;
 
 
 /**
  * This sample class demonstrates how to plug-in a new
  * workbench view. The view shows data obtained from the
  * model. The sample creates a dummy model on the fly,
  * but a real implementation would connect to the model
  * available either in this or another plug-in (e.g. the workspace).
  * The view is connected to the model using a content provider.
  * <p>
  * The view uses a label provider to define how model
  * objects should be presented in the view. Each
  * view can present the same model objects using
  * different labels and icons, if needed. Alternatively,
  * a single label provider can be shared between views
  * in order to ensure that objects of the same type are
  * presented in the same way everywhere.
  * <p>
  */
 // TODO: if task contains context - than it is inprog
 public class MylynKanbanView extends ViewPart {
 
 	/**
 	 * The ID of the view as specified by the extension.
 	 */
 	public static final String ID = "mylynkanban.views.MylynKanbanView";
 	
 	private Browser browser;
 	
 	@Override
 	public void createPartControl(Composite parent) {
 		final Display disp = Display.getCurrent();
 		try {
			browser = new Browser(parent, SWT.WEBKIT); 
 		} catch (Error e) {
 			try {
				browser = new Browser(parent, SWT.MOZILLA);
 			} catch (Error e2) {
 				browser = new Browser(parent, SWT.NONE);
 			}
 		}
 		
 		browser.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 2, 1));
 		browser.getBrowserType();
 		browser.setUrl("http://localhost:9999/Kanban.html");
 	}
 
 	@Override
 	public void setFocus() {
 		browser.setFocus();
 	}
 }
