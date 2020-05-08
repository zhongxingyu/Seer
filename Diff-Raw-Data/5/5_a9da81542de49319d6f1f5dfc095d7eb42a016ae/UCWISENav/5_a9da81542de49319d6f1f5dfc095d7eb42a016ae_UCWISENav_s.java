 package studentview.views;
 
 //Andy Carle, Berkeley Institute of Design, UC Berkeley 
 
 import java.io.IOException;
 import java.net.URL;
 import java.util.Vector;
 
 import org.eclipse.core.resources.IFile;
 import org.eclipse.core.resources.IResource;
 import org.eclipse.core.resources.IResourceVisitor;
 import org.eclipse.core.resources.ResourcesPlugin;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.FileLocator;
 import org.eclipse.core.runtime.IPath;
 import org.eclipse.core.runtime.Path;
 import org.eclipse.core.runtime.Platform;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.custom.StackLayout;
 import org.eclipse.swt.events.SelectionAdapter;
 import org.eclipse.swt.events.SelectionEvent;
 import org.eclipse.swt.graphics.Image;
 import org.eclipse.swt.layout.RowData;
 import org.eclipse.swt.layout.RowLayout;
 import org.eclipse.swt.widgets.Button;
 import org.eclipse.swt.widgets.Combo;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Group;
 import org.eclipse.swt.widgets.Label;
 import org.eclipse.swt.widgets.Layout;
 import org.eclipse.ui.part.ViewPart;
 import org.osgi.framework.Bundle;
 
 import studentview.model.Step;
 import studentview.model.Sequence;
 import studentview.model.Step.ExerciseType;
 
 
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
 
 public class UCWISENav extends ViewPart{
 
 	/**
 	 * The ID of the view as specified by the extension.
 	 */
 	public static final String ID = "studentview.views.SampleView";
 
 	
 	Label title;
 	Vector<Sequence> segments = new Vector<Sequence>();
 	Vector<SequenceWidget> isagroups = new Vector<SequenceWidget>();
 		
 	Group hidden;
 	RowData rowdata = new RowData();
 	StackLayout stackLayout;
 	Group isaHolder;
 
 	/**
 	 * The constructor.
 	 */
 	public UCWISENav() {
 		
 	}
 
 	/**
 	 * This is a callback that will allow us
 	 * to create the viewer and initialize it.
 	 */
 	
 	public void parseISA(IFile file){
 		Sequence s = Sequence.parseISA(file);
 		if (s == null) System.err.println("Failed to parse file: " + file.getName());		
 		segments.add(s);
 	}
 	
 	public void createPartControl(Composite rootparent) {
 		
 		try {
 			ResourcesPlugin.getWorkspace().getRoot().accept(new IResourceVisitor() {			
 				@Override
 				public boolean visit(IResource resource) throws CoreException {
 					if (!(resource.getType() == IResource.FILE)) return true;
					if (resource.getFileExtension().equalsIgnoreCase("isa")) parseISA((IFile)resource);
 					return true;
 				}
 			});
 		} catch (CoreException e1) {
 			System.err.println("Core Exception!!!");
 			e1.printStackTrace();
 		}
 		
 		String selectionImage = "";
 		try{
 			String filename = "icons/selection.gif";
 			Bundle bun = Platform.getBundle("StudentView");
 			IPath ip = new Path(filename);		
 			URL url = FileLocator.find(bun, ip, null);
 			URL res = FileLocator.resolve(url);
 			selectionImage = res.getPath();
 		}catch(IOException e){
 			System.err.println("Could not find image file.");
 			e.printStackTrace();
 		}
 		Image selection = new Image(rootparent.getDisplay(), selectionImage);
 	
 		rootparent.setLayout(setupLayout());
 		rootparent.setLayoutData(rowdata);
 		
 		title = new Label(rootparent, SWT.WRAP);
 		title.setText("UCWISE -- Now with more Eclipse");
 		
 		final Combo combo = new Combo(rootparent, SWT.READ_ONLY);
 		combo.addSelectionListener(new SelectionAdapter() {
 			@Override
 			public void widgetSelected(SelectionEvent e){
 				int sel = combo.getSelectionIndex();
 				if (sel >= 0 && sel < isagroups.size()){					
 					stackLayout.topControl = isagroups.get(sel).group;
 					isaHolder.layout();
 				}
 			}
 		});
 		
 		isaHolder = new Group(rootparent, SWT.SHADOW_NONE);
 		stackLayout = new StackLayout();
 		isaHolder.setLayout(stackLayout);
 		//isaHolder.setLayoutData(new GridData(SWT.FILL, SWT.TOP, false, true, 2, 1));
 		isaHolder.setLayoutData(new RowData());
 		
 		
 		for (Sequence seg : segments){			
 			SequenceWidget parent = new SequenceWidget(isaHolder, SWT.SHADOW_NONE, seg);
 			Group buttons = new Group(parent.group, SWT.SHADOW_NONE);
 		
 			parent.back = new Button(buttons, SWT.ARROW|SWT.LEFT);
 			parent.currentStep = new Label(buttons, SWT.WRAP);
 			parent.next = new Button(buttons, SWT.ARROW|SWT.RIGHT);
 		
 			RowLayout buttonsLO = new RowLayout();
 			buttonsLO.justify = true;
 			buttons.setLayout(buttonsLO);
 			parent.currentStep.setText("Introduction");
 			parent.back.addSelectionListener(parent);
 			parent.next.addSelectionListener(parent);
 		
 			parent.back.setEnabled(false);
 			
 			
 			Label intro = new Label(parent.group, SWT.WRAP);
 			intro.setText(seg.getIntro());
 			parent.intro = intro;
 			intro.setLayoutData(new RowData(title.getSize().x, 100));
 		
 			for (Step e : seg.getExercises()){
 				Group stepline = new Group(parent.group, SWT.SHADOW_NONE);
 				Label sel = new Label(stepline, SWT.WRAP);			
 				sel.setImage(selection);
 				sel.setVisible(false);
 				Label step = new Label(stepline, SWT.WRAP); 
 				step.setText(e.getName());
 				step.addMouseListener(parent);			
 			
 				Button test = null;
 				Button reset = null;
 				if (e.getTestname() != null && !("".equalsIgnoreCase(e.getTestname().trim()))){
 					test = new Button(stepline, 0);
 					test.setText("Run Tests");
 					test.addSelectionListener(parent);
 				}
 				
 				if (e.getType() == ExerciseType.EDIT){
 					reset = new Button(stepline, 0);				
 					reset.setText("Reset Exercise");					
 					reset.addSelectionListener(parent);
 				}							
 			
 				stepline.setLayout(new RowLayout());			
 			
 				StepWidgets widge = new StepWidgets(sel, step, e, stepline, test, reset);
 				parent.steps.add(widge);
 			}		
 			parent.group.setLayout(setupLayout());
 			combo.add(seg.getName());
 			isagroups.add(parent);
 		}
 		
 		/*for (ISAGroup g : isagroups){
 			g.group.setVisible(false);			
 			((RowData) g.group.getLayoutData()).exclude = true;
 		}*/
 		
 		if (isagroups.size() > 0){			
 			stackLayout.topControl = isagroups.get(0).group;
 			combo.select(0);
 		}
 		isaHolder.layout();
 	}
 	
 	private Layout setupLayout(){
 		RowLayout layout = new RowLayout();
 		layout.wrap = true;
 		layout.pack = true;
 		layout.fill = true;		
 		layout.justify = false;;
 		layout.type = SWT.VERTICAL;
 		layout.spacing = 10;		
 		return layout;		
 	}
 
 	/**
 	 * Passing the focus request to the viewer's control.
 	 */
 	public void setFocus() {
 		title.setFocus();
 	}
 	
 
 	
 }
