 package com.vectorsf.jvoice.ui.navigator.handler;
 
 import org.eclipse.core.resources.IResource;
 import org.eclipse.core.resources.ResourcesPlugin;
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.jdt.core.IAnnotation;
 import org.eclipse.jdt.core.ICompilationUnit;
 import org.eclipse.jdt.core.IImportDeclaration;
 import org.eclipse.jdt.core.IMemberValuePair;
 import org.eclipse.jdt.core.IPackageFragment;
 import org.eclipse.jdt.core.IType;
 import org.eclipse.jdt.core.ITypeRoot;
 import org.eclipse.jdt.core.JavaModelException;
 import org.eclipse.jdt.ui.JavaElementLabelProvider;
 import org.eclipse.jdt.ui.StandardJavaElementContentProvider;
 import org.eclipse.jface.dialogs.IDialogConstants;
 import org.eclipse.jface.dialogs.IMessageProvider;
 import org.eclipse.jface.dialogs.TitleAreaDialog;
 import org.eclipse.jface.viewers.ILabelProvider;
 import org.eclipse.jface.viewers.Viewer;
 import org.eclipse.jface.viewers.ViewerFilter;
 import org.eclipse.jface.window.Window;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.events.SelectionEvent;
 import org.eclipse.swt.events.SelectionListener;
 import org.eclipse.swt.layout.GridData;
 import org.eclipse.swt.layout.GridLayout;
 import org.eclipse.swt.widgets.Button;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Control;
 import org.eclipse.swt.widgets.Event;
 import org.eclipse.swt.widgets.Label;
 import org.eclipse.swt.widgets.Listener;
 import org.eclipse.swt.widgets.Shell;
 import org.eclipse.swt.widgets.Text;
 
 import com.vectorsf.jvoice.model.operations.ComponentBean;
 import com.vectorsf.jvoice.model.operations.OperationsFactory;
 
 public class BeanScopeAddDialog extends TitleAreaDialog {
 
 	private Text scopedNameText;
 	private Text beanClassText;
 	private Text beanNameText;
 	private IPackageFragment packageFragment;
 	private Shell shell;
 
 	private ComponentBean bean;
 
 	public BeanScopeAddDialog(Shell parentShell,
 			IPackageFragment packageFragment) {
 		super(parentShell);
 		this.packageFragment = packageFragment;
 		this.shell = parentShell;
 		this.bean = OperationsFactory.eINSTANCE.createComponentBean();
 	}
 
 	private Listener nameModifyListener = new Listener() {
 		@Override
 		public void handleEvent(Event e) {
			validatePage();

 		}
 	};
 
 	private boolean validatePage() {
 		String text = getScopedName();
 		if (text.isEmpty()) {
 			setErrorMessage(null);
 			setMessage("Enter a scope name");
 			getButton(IDialogConstants.OK_ID).setEnabled(false);
 			return false;
 		}
 
 		IStatus status = ResourcesPlugin.getWorkspace().validateName(text,
 				IResource.FILE);
 		if (!status.isOK()) {
 			setErrorMessage(status.getMessage());
 			getButton(IDialogConstants.OK_ID).setEnabled(false);
 			return false;
 		}
 
 		String beanClass = getBeanClassName();
 		if (beanClass.isEmpty()) {
 			setErrorMessage(null);
 			setMessage("Select Bean Class");
 			getButton(IDialogConstants.OK_ID).setEnabled(false);
 			return false;
 		}
 		setErrorMessage(null);
 		setMessage(null);
 		getButton(IDialogConstants.OK_ID).setEnabled(true);
		updateComponent(scopedNameText.getText());
 		return true;
 	}
 
 	@Override
 	public void create() {
 		super.create();
 		setTitle("Add Bean to Scope");
 		setMessage("Select Bean", IMessageProvider.INFORMATION);
 	}
 
 	@Override
 	protected Control createContents(Composite parent) {
 		super.createContents(parent);
 		getButton(IDialogConstants.OK_ID).setEnabled(false);
 		return parent;
 	}
 
 	@Override
 	protected Control createDialogArea(Composite parent) {
 		Composite area = (Composite) super.createDialogArea(parent);
 		Composite container = new Composite(area, SWT.NONE);
 		container.setLayoutData(new GridData(GridData.FILL_BOTH));
 		GridLayout layout = new GridLayout(3, false);
 		container.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
 		container.setLayout(layout);
 
 		createName(container);
 		createLabelName(container);
 		createValueName(container);
 
 		setHelpAvailable(false);
 		return area;
 	}
 
 	private void createName(Composite container) {
 		Label lbtName = new Label(container, SWT.NONE);
 		lbtName.setText("Scope Name:");
 
 		GridData dataName = new GridData();
 		dataName.grabExcessHorizontalSpace = true;
 		dataName.horizontalAlignment = GridData.FILL;
 		// dataName.horizontalSpan = 2;
 		scopedNameText = new Text(container, SWT.BORDER);
 		scopedNameText.setLayoutData(dataName);
 		scopedNameText.addListener(SWT.Modify, nameModifyListener);
 
 		new Label(container, SWT.NONE);
 	}
 
 	private void createLabelName(Composite container) {
 		Label lbtJavaName = new Label(container, SWT.NONE);
 		lbtJavaName.setText("Bean Class:");
 
 		GridData dataJavaName = new GridData();
 		dataJavaName.grabExcessHorizontalSpace = true;
 		dataJavaName.horizontalAlignment = GridData.FILL;
 		beanClassText = new Text(container, SWT.BORDER);
 		beanClassText.setLayoutData(dataJavaName);
 		beanClassText.setEditable(false);
 
 		// browse button on right
 		Button browse = new Button(container, SWT.PUSH);
 		browse.setText("...");
 		browse.addSelectionListener(new SelectionListener() {
 
 			@Override
 			public void widgetSelected(SelectionEvent e) {
 				StandardJavaElementContentProvider contentProvider = new StandardJavaElementContentProvider(
 						false);
 				ILabelProvider labelProvider = new JavaElementLabelProvider(
 						JavaElementLabelProvider.SHOW_BASICS);
 
 				ComponentsSelectionDialog dialog = new ComponentsSelectionDialog(
 						shell, labelProvider, contentProvider);
 				dialog.setInput(packageFragment);
 				dialog.addFilter(new ComponentFilter());
 				if (dialog.open() == Window.OK) {
 					ITypeRoot typeRoot = (ITypeRoot) dialog.getFirstResult();
 					updateComponent(typeRoot);
 					beanClassText.setText(bean.getFqdn());
 					beanNameText.setText(bean.getNameBean());
 				}
 				validatePage();
 			}
 
 			@Override
 			public void widgetDefaultSelected(SelectionEvent e) {
 			}
 
 		});
 	}
 
 	private void createValueName(Composite container) {
 		Label lbtValueName = new Label(container, SWT.NONE);
 		lbtValueName.setText("Bean Name:");
 
 		GridData dataValueName = new GridData();
 		dataValueName.grabExcessHorizontalSpace = true;
 		dataValueName.horizontalAlignment = GridData.FILL;
 		beanNameText = new Text(container, SWT.BORDER);
 		beanNameText.setLayoutData(dataValueName);
 		beanNameText.setEditable(false);
 		beanNameText.setEnabled(false);
 	}
 
 	@Override
 	protected boolean isResizable() {
 		return true;
 	}
 
 	private void updateComponent(ITypeRoot typeRoot) {
 		IType type = typeRoot.findPrimaryType();
 		String name = firstToLowercase(type.getElementName());
 
 		IAnnotation annotation = findAnnotation(typeRoot);
 		try {
 			IMemberValuePair[] memberValuePairs = annotation
 					.getMemberValuePairs();
 			if (memberValuePairs.length > 0) {
 				name = (String) memberValuePairs[0].getValue();
 			}
 		} catch (JavaModelException e) {
 		}
 
 		bean.setFqdn(type.getFullyQualifiedName());
 		bean.setNameBean(name);
 	}
 
 	private String firstToLowercase(String elementName) {
 		if (elementName.isEmpty()) {
 			return elementName;
 		}
 
 		char c = elementName.charAt(0);
 		if (Character.isUpperCase(c)) {
 			return Character.toLowerCase(c) + elementName.substring(1);
 		} else {
 			return elementName;
 		}
 	}
 
 	private void updateComponent(String componentName) {
 		bean.setName(componentName);
 	}
 
 	private IAnnotation findAnnotation(ITypeRoot unit) {
 		IType type = unit.findPrimaryType();
 
 		if (type == null) {
 			return null;
 		}
 
 		try {
 			for (IAnnotation annotation : type.getAnnotations()) {
 				String elementName = annotation.getElementName();
 				if (elementName
 						.equals("org.springframework.stereotype.Component")) {
 					return annotation;
 				} else if (elementName.equals("Component")
 						&& unit instanceof ICompilationUnit) {
 					for (IImportDeclaration _import : ((ICompilationUnit) unit)
 							.getImports()) {
 						String importedType = _import.getElementName();
 						if (importedType
 								.equals("org.springframework.stereotype.Component")) {
 							return annotation;
 						}
 						if (importedType
 								.equals("org.springframework.stereotype.*")) {
 							return annotation;
 						}
 					}
 				}
 			}
 		} catch (JavaModelException e) {
 			return null;
 		}
 
 		return null;
 	}
 
 	public class ComponentFilter extends ViewerFilter {
 
 		@Override
 		public boolean select(Viewer viewer, Object parentElement,
 				Object element) {
 			if (element instanceof ITypeRoot) {
 
 				return findAnnotation((ITypeRoot) element) != null;
 
 			}
 			return false;
 		}
 	}
 
 	public ComponentBean getComponentBean() {
 		return bean;
 	}
 
 	private String getScopedName() {
 		if (scopedNameText == null) {
 			return ""; //$NON-NLS-1$
 		}
 		return scopedNameText.getText().trim();
 	}
 
 	private String getBeanClassName() {
 		if (beanClassText == null) {
 			return ""; //$NON-NLS-1$
 		}
 		return beanClassText.getText().trim();
 	}
 
 }
