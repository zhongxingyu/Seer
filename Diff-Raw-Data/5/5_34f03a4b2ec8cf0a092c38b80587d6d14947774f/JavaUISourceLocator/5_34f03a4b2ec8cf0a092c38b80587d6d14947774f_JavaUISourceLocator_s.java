 package org.eclipse.jdt.debug.ui;
 
 /*
  * (c) Copyright IBM Corp. 2000, 2001.
  * All Rights Reserved.
  */
  
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.debug.core.DebugException;
 import org.eclipse.debug.core.ILaunchConfiguration;
 import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
 import org.eclipse.debug.core.model.IPersistableSourceLocator;
 import org.eclipse.debug.core.model.IStackFrame;
 import org.eclipse.jdt.core.IJavaProject;
 import org.eclipse.jdt.core.JavaCore;
 import org.eclipse.jdt.core.JavaModelException;
 import org.eclipse.jdt.debug.core.IJavaStackFrame;
 import org.eclipse.jdt.internal.debug.ui.JDIDebugUIPlugin;
 import org.eclipse.jdt.internal.debug.ui.launcher.LauncherMessages;
 import org.eclipse.jdt.internal.debug.ui.launcher.SourceLookupBlock;
 import org.eclipse.jdt.launching.JavaRuntime;
 import org.eclipse.jdt.launching.sourcelookup.IJavaSourceLocation;
 import org.eclipse.jdt.launching.sourcelookup.JavaSourceLocator;
 import org.eclipse.jface.dialogs.Dialog;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.graphics.FontMetrics;
 import org.eclipse.swt.graphics.GC;
 import org.eclipse.swt.layout.GridData;
 import org.eclipse.swt.layout.GridLayout;
 import org.eclipse.swt.widgets.Button;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Control;
 import org.eclipse.swt.widgets.Label;
 import org.eclipse.swt.widgets.Shell;
 
 /**
  * A source locator that prompts the user to find source when source cannot
  * be found on the current source lookup path.
  * <p>
  * This class is intended to be instantiated. This class is not
  * intended to be subclassed.
  * </p>
  * @since 2.0
  */
 
 public class JavaUISourceLocator implements IPersistableSourceLocator {
 
 	/**
 	 * Identifier for the 'Prompting Java Source Locator' extension
 	 * (value <code>"org.eclipse.jdt.debug.ui.javaSourceLocator"</code>).
 	 */
 	public static final String ID_PROMPTING_JAVA_SOURCE_LOCATOR = IJavaDebugUIConstants.PLUGIN_ID + ".javaSourceLocator"; //$NON-NLS-1$
 	
 	/**
 	 * The project being debugged.
 	 */
 	private IJavaProject fJavaProject; 
 	
 	/**
 	 * Underlying source locator.
 	 */
 	private JavaSourceLocator fSourceLocator;
 	
 	/**
 	 * Whether the user should be prompted for source.
 	 * Initially true, until the user checks the 'do not
 	 * ask again' box.
 	 */
 	private boolean fAllowedToAsk;
 	
 	/**
 	 * Constructs an empty source locator.
 	 */
 	public JavaUISourceLocator() {
 		fSourceLocator = new JavaSourceLocator();
 		fAllowedToAsk= true;
 	}
 
 	/**
 	 * Constructs a new source locator that looks in the
 	 * specified project for source, and required projects, if
 	 * <code>includeRequired</code> is <code>true</code>.
 	 * 
 	 * @param projects the projects in which to look for source
 	 * @param includeRequired whether to look in required projects
 	 * 	as well
 	 */
 	public JavaUISourceLocator(IJavaProject[] projects, boolean includeRequired) throws JavaModelException {
 		fSourceLocator = new JavaSourceLocator(projects, includeRequired);
 		fAllowedToAsk = true;
 	}	
 		
 	/**
 	 * Constructs a source locator that searches for source
 	 * in the given Java project, and all of its required projects,
 	 * as specified by its build path or default source lookup
 	 * settings.
 	 * 
 	 * @param project Java project
 	 * @exception CoreException if unable to read the project's
 	 * 	 build path
 	 */
 	public JavaUISourceLocator(IJavaProject project) throws CoreException {
 		fJavaProject= project;
 		IJavaSourceLocation[] sls = JavaSourceLocator.getDefaultSourceLocations(project);
 		fSourceLocator= new JavaSourceLocator(project);
 		if (sls != null) {
 			fSourceLocator.setSourceLocations(sls);
 		}
 		fAllowedToAsk= true;
 	}
 
 	/**
 	 * @see org.eclipse.debug.core.model.ISourceLocator#getSourceElement(IStackFrame)
 	 */
 	public Object getSourceElement(IStackFrame stackFrame) {
 		Object res= fSourceLocator.getSourceElement(stackFrame);
 		if (res == null && fAllowedToAsk) {
 			IJavaStackFrame frame= (IJavaStackFrame)stackFrame.getAdapter(IJavaStackFrame.class);
 			if (frame != null) {
 				showDebugSourcePage(frame);
 				res= fSourceLocator.getSourceElement(stackFrame);
 			}
 		}
 		return res;
 	}
 	
 	/**
	 * Prompts to locate the source of the given type.
 	 * 
 	 * @param typeName the name of the type for which source
 	 *  could not be located
 	 */
 	private void showDebugSourcePage(final IJavaStackFrame frame) {
 		Runnable prompter = new Runnable() {
 			public void run() {
 				try {
 					SourceLookupDialog dialog= new SourceLookupDialog(JDIDebugUIPlugin.getActiveWorkbenchShell(), frame.getDeclaringTypeName(), frame.getLaunch().getLaunchConfiguration(), JavaUISourceLocator.this);
 					dialog.open();
 					fAllowedToAsk= !dialog.isNotAskAgain();
 				} catch (DebugException e) {
 					JDIDebugUIPlugin.log(e);
 				}
 			}
 		};
 		JDIDebugUIPlugin.getStandardDisplay().syncExec(prompter);
 	}
 	
 	/**
 	 * Dialog that prompts for source lookup path.
 	 */
 	private static class SourceLookupDialog extends Dialog {
 		
 		private SourceLookupBlock fSourceLookupBlock;
 		private JavaUISourceLocator fLocator;
 		private ILaunchConfiguration fConfiguration;
 		private String fTypeName;
 		private boolean fNotAskAgain;
 		private Button fAskAgainCheckBox;
 		
 		public SourceLookupDialog(Shell shell, String typeName, ILaunchConfiguration configuration, JavaUISourceLocator locator) {
 			super(shell);
 			fSourceLookupBlock= new SourceLookupBlock();
 			fTypeName= typeName;
 			fNotAskAgain= false;
 			fAskAgainCheckBox= null;
 			fLocator = locator;
 			fConfiguration = configuration;
 		}
 		
 		public boolean isNotAskAgain() {
 			return fNotAskAgain;
 		}
 				
 				
 		protected Control createDialogArea(Composite parent) {
 			getShell().setText(LauncherMessages.getString("JavaUISourceLocator.selectprojects.title")); //$NON-NLS-1$
 			
 			Composite composite= (Composite) super.createDialogArea(parent);
 			composite.setLayout(new GridLayout());
 			
 			Label message= new Label(composite, SWT.LEFT + SWT.WRAP);
 			message.setText(LauncherMessages.getFormattedString("JavaUISourceLocator.selectprojects.message", fTypeName)); //$NON-NLS-1$
 			GridData data= new GridData();
 			data.widthHint= convertWidthInCharsToPixels(message, 70);
 			message.setLayoutData(data);
 
 			fSourceLookupBlock.createControl(composite);
 			Control inner = fSourceLookupBlock.getControl();
 			fSourceLookupBlock.initializeFrom(fConfiguration);
 			inner.setLayoutData(new GridData(GridData.FILL_BOTH));
 			fAskAgainCheckBox= new Button(composite, SWT.CHECK + SWT.WRAP);
 			data= new GridData();
 			data.widthHint= convertWidthInCharsToPixels(fAskAgainCheckBox, 70);
 			fAskAgainCheckBox.setLayoutData(data);
 			fAskAgainCheckBox.setText(LauncherMessages.getString("JavaUISourceLocator.askagain.message")); //$NON-NLS-1$
 			
 			return composite;
 		}
 		
 		/**
 		 * @see Dialog#convertWidthInCharsToPixels(FontMetrics, int)
 		 */
 		protected int convertWidthInCharsToPixels(Control control, int chars) {
 			GC gc = new GC(control);
 			gc.setFont(control.getFont());
 			FontMetrics fontMetrics= gc.getFontMetrics();
 			gc.dispose();
 			return Dialog.convertWidthInCharsToPixels(fontMetrics, chars);
 		}	
 
 		protected void okPressed() {
 			try {
 				if (fAskAgainCheckBox != null) {
 					fNotAskAgain= fAskAgainCheckBox.getSelection();
 				}
 				ILaunchConfigurationWorkingCopy wc = fConfiguration.getWorkingCopy();
 				fSourceLookupBlock.performApply(wc);
 				if (!fConfiguration.contentsEqual(wc)) {
 					fConfiguration = wc.doSave();
 					fLocator.initializeDefaults(fConfiguration);
 				}
 			} catch (CoreException e) {
 				JDIDebugUIPlugin.log(e);
 			}
 			super.okPressed();
 		}
 	}
 	
 	/**
 	 * @see IPersistableSourceLocator#getMemento()
 	 */
 	public String getMemento() throws CoreException {
 		String memento = fSourceLocator.getMemento();
 		String handle = fJavaProject.getHandleIdentifier();
 		memento = handle + '\n' + memento;
 		return memento;
 	}
 
 	/**
 	 * @see IPersistableSourceLocator#initializeDefaults(ILaunchConfiguration)
 	 */
 	public void initializeDefaults(ILaunchConfiguration configuration)
 		throws CoreException {
 			fSourceLocator.initializeDefaults(configuration);
 			fJavaProject = JavaRuntime.getJavaProject(configuration);
 	}
 
 	/**
 	 * @see IPersistableSourceLocator#initiatlizeFromMemento(String)
 	 */
 	public void initiatlizeFromMemento(String memento) throws CoreException {
 		int index = memento.indexOf('\n');
 		String handle = memento.substring(0, index);
 		String rest = memento.substring(index + 1);
 		fJavaProject = (IJavaProject)JavaCore.create(handle);
 		fSourceLocator.initiatlizeFromMemento(rest);
 	}
 	
 	/**
 	 * @see JavaSourceLocator#getSourceLocations()
 	 */
 	public IJavaSourceLocation[] getSourceLocations() {
 		return fSourceLocator.getSourceLocations();
 	}
 	
 	/**
 	 * @see JavaSourceLocator#setSourceLocations(IJavaSourceLocation[])
 	 */
 	public void setSourceLocations(IJavaSourceLocation[] locations) {
 		fSourceLocator.setSourceLocations(locations);
 	}
 }
 
