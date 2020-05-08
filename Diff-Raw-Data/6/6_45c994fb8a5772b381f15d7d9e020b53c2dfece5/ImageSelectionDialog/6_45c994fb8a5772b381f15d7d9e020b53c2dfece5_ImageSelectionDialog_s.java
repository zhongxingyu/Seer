 /*******************************************************************************
  * Copyright (c) 2007 Exadel, Inc. and Red Hat, Inc.
  * Distributed under license by Red Hat, Inc. All rights reserved.
  * This program is made available under the terms of the
  * Eclipse Public License v1.0 which accompanies this distribution,
  * and is available at http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     Exadel, Inc. and Red Hat, Inc. - initial API and implementation
  ******************************************************************************/
 package org.jboss.tools.jst.css.dialog;
 
 import org.eclipse.core.resources.IFile;
 import org.eclipse.core.resources.IFolder;
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.core.runtime.Status;
 
 import org.eclipse.jface.dialogs.IDialogConstants;
 import org.eclipse.jface.viewers.DoubleClickEvent;
 import org.eclipse.jface.viewers.IDoubleClickListener;
 import org.eclipse.jface.viewers.ILabelProvider;
 import org.eclipse.jface.viewers.ISelection;
 import org.eclipse.jface.viewers.ISelectionChangedListener;
 import org.eclipse.jface.viewers.IStructuredSelection;
 import org.eclipse.jface.viewers.ITreeContentProvider;
 import org.eclipse.jface.viewers.SelectionChangedEvent;
 import org.eclipse.jface.viewers.StructuredSelection;
 import org.eclipse.jface.viewers.TreeViewer;
 import org.eclipse.jface.viewers.ViewerComparator;
 import org.eclipse.jface.viewers.ViewerFilter;
 import org.eclipse.jface.viewers.ViewerSorter;
 
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.SWTException;
 import org.eclipse.swt.custom.BusyIndicator;
 import org.eclipse.swt.events.PaintEvent;
 import org.eclipse.swt.events.PaintListener;
 import org.eclipse.swt.events.SelectionAdapter;
 import org.eclipse.swt.events.SelectionEvent;
 import org.eclipse.swt.events.SelectionListener;
 import org.eclipse.swt.graphics.Color;
 import org.eclipse.swt.graphics.Cursor;
 import org.eclipse.swt.graphics.GC;
 import org.eclipse.swt.graphics.Image;
 import org.eclipse.swt.graphics.ImageData;
 import org.eclipse.swt.graphics.Point;
 import org.eclipse.swt.layout.GridData;
 import org.eclipse.swt.layout.GridLayout;
 import org.eclipse.swt.widgets.Canvas;
 import org.eclipse.swt.widgets.Combo;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Control;
 import org.eclipse.swt.widgets.Label;
 import org.eclipse.swt.widgets.Shell;
 import org.eclipse.swt.widgets.Text;
 import org.eclipse.swt.widgets.Tree;
 
 import org.eclipse.ui.PlatformUI;
 import org.eclipse.ui.dialogs.ElementTreeSelectionDialog;
 import org.eclipse.ui.dialogs.ISelectionStatusValidator;
 import org.eclipse.ui.dialogs.SelectionStatusDialog;
 import org.eclipse.ui.internal.WorkbenchMessages;
 
 import org.jboss.tools.jst.css.dialog.common.FileExtensionFilter;
 import org.jboss.tools.jst.jsp.messages.JstUIMessages;
 import org.jboss.tools.jst.jsp.util.Constants;
 
 import java.io.FileInputStream;
 import java.io.IOException;
 import java.io.InputStream;
 
 import java.util.ArrayList;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Set;
 
 
 /**
  * Class for creating images selection dialog
  *
  * @author dsakovich@exadel.com
  */
 @SuppressWarnings("restriction")
 public class ImageSelectionDialog extends SelectionStatusDialog {
     @SuppressWarnings("nls")
 	final static String[][] fileExtensions = {
             { "jpeg", "jpg", "jpe", "jfif" },
             { "gif" },
             { "bmp" },
             { "tif", "tiff" },
             { "png" },
             { "ico" }
         };
     private Combo filterCombo;
     private Canvas canvas;
     private IFile file;
     private Text resolution;
     private Color emptyColor;
     private TreeViewer fViewer;
     private ILabelProvider fLabelProvider;
     private ITreeContentProvider fContentProvider;
     private ISelectionStatusValidator fValidator = null;
     private ViewerComparator fComparator;
     private boolean fAllowMultiple = true;
     private boolean fDoubleClickSelects = true;
     private String fEmptyListMessage = WorkbenchMessages.ElementTreeSelectionDialog_nothing_available;
     private IStatus fCurrStatus = new Status(IStatus.OK, PlatformUI.PLUGIN_ID, IStatus.OK, Constants.EMPTY, null);
     private List<ViewerFilter> fFilters;
     private Object fInput;
     private boolean fIsEmpty;
     private int fWidth = 60;
     private int fHeight = 18;
 
     /**
      * Constructs an instance of <code>ImageSelectionDialog</code>.
      *
      * @param parent
      *                The parent shell for the dialog
      * @param labelProvider
      *                the label provider to render the entries
      * @param contentProvider
      *                the content provider to evaluate the tree structure
      */
     public ImageSelectionDialog(Shell parent, ILabelProvider labelProvider,
         ITreeContentProvider contentProvider) {
         super(parent);
 
         fLabelProvider = labelProvider;
         fContentProvider = contentProvider;
 
         setResult(new ArrayList<Object>(0));
         setStatusLineAboveButtons(true);
 
         int shellStyle = getShellStyle();
         setShellStyle(shellStyle | SWT.MAX | SWT.RESIZE);
     }
 
     /**
      * Sets the initial selection. Convenience method.
      *
      * @param selection
      *                the initial selection.
      */
     public void setInitialSelection(Object selection) {
         setInitialSelections(new Object[] { selection });
     }
 
     /**
      * Sets the message to be displayed if the list is empty.
      *
      * @param message
      *                the message to be displayed.
      */
     public void setEmptyListMessage(String message) {
         fEmptyListMessage = message;
     }
 
     /**
      * Specifies if multiple selection is allowed.
      *
      * @param allowMultiple
      */
     public void setAllowMultiple(boolean allowMultiple) {
         fAllowMultiple = allowMultiple;
     }
 
     /**
      * Specifies if default selected events (double click) are created.
      *
      * @param doubleClickSelects
      */
     public void setDoubleClickSelects(boolean doubleClickSelects) {
         fDoubleClickSelects = doubleClickSelects;
     }
 
     /**
      * Sets the sorter used by the tree viewer.
      *
      * @param sorter
      * @deprecated as of 3.3, use
      *             {@link ElementTreeSelectionDialog#setComparator(ViewerComparator)}
      *             instead
      */
     public void setSorter(ViewerSorter sorter) {
         fComparator = sorter;
     }
 
     /**
      * Sets the comparator used by the tree viewer.
      *
      * @param comparator
      * @since 3.3
      */
     public void setComparator(ViewerComparator comparator) {
         fComparator = comparator;
     }
 
     /**
      * Adds a filter to the tree viewer.
      *
      * @param filter
      *                a filter.
      */
     public void addFilter(String[] extention) {
         if (fFilters == null) {
             fFilters = new ArrayList<ViewerFilter>(4);
         }
 
         fFilters.add(new FileExtensionFilter(extention));
     }
 
     /**
      * Sets an optional validator to check if the selection is valid. The
      * validator is invoked whenever the selection changes.
      *
      * @param validator
      *                the validator to validate the selection.
      */
     public void setValidator(ISelectionStatusValidator validator) {
         fValidator = validator;
     }
 
     /**
      * Sets the tree input.
      *
      * @param input
      *                the tree input.
      */
     public void setInput(Object input) {
         fInput = input;
     }
 
     /**
      * Sets the size of the tree in unit of characters.
      *
      * @param width
      *                the width of the tree.
      * @param height
      *                the height of the tree.
      */
     public void setSize(int width, int height) {
         fWidth = width;
         fHeight = height;
     }
 
     /**
      * Validate the receiver and update the ok status.
      *
      */
     protected void updateOKStatus() {
         if (!fIsEmpty) {
             if (fValidator != null) {
                 fCurrStatus = fValidator.validate(getResult());
                 updateStatus(fCurrStatus);
             } else {
                 fCurrStatus = new Status(IStatus.OK, PlatformUI.PLUGIN_ID, IStatus.OK, "", null); //$NON-NLS-1$
             }
         } else {
             fCurrStatus = new Status(IStatus.ERROR, PlatformUI.PLUGIN_ID, IStatus.ERROR,
                     fEmptyListMessage, null);
         }
 
         updateStatus(fCurrStatus);
     }
 
     /*
      * (non-Javadoc)
      *
      * @see org.eclipse.jface.window.Window#open()
      */
     public int open() {
         fIsEmpty = evaluateIfTreeEmpty(fInput);
         super.open();
 
         return getReturnCode();
     }
 
     /**
      * Handles cancel button pressed event.
      */
     protected void cancelPressed() {
         setResult(null);
         super.cancelPressed();
     }
 
     /*
      * @see SelectionStatusDialog#computeResult()
      */
     protected void computeResult() {
         setResult(((IStructuredSelection) fViewer.getSelection()).toList());
     }
 
     /*
      * (non-Javadoc)
      *
      * @see org.eclipse.jface.window.Window#create()
      */
     public void create() {
         BusyIndicator.showWhile(null,
             new Runnable() {
                 public void run() {
                     access$superCreate();
                     fViewer.setSelection(new StructuredSelection(getInitialElementSelections()), true);
                     updateOKStatus();
                 }
             });
     }
 
     /*
      * @see Dialog#createDialogArea(Composite)
      */
     protected Control createDialogArea(Composite parent) {
         final Composite composite = new Composite(parent, SWT.NONE);
         GridLayout layout = new GridLayout();
         layout.numColumns = 2;
         layout.marginHeight = convertVerticalDLUsToPixels(IDialogConstants.VERTICAL_MARGIN);
         layout.marginWidth = convertHorizontalDLUsToPixels(IDialogConstants.HORIZONTAL_MARGIN);
         layout.verticalSpacing = convertVerticalDLUsToPixels(IDialogConstants.VERTICAL_SPACING);
         layout.horizontalSpacing = convertHorizontalDLUsToPixels(IDialogConstants.HORIZONTAL_SPACING);
         composite.setLayout(layout);
         composite.setLayoutData(new GridData(GridData.FILL_BOTH));
         super.applyDialogFont(composite);
 
         Label messageLabel = createMessageArea(composite);
         new Label(composite, SWT.NONE);
 
         TreeViewer treeViewer = createTreeViewer(composite);
 
         Composite comp = createPreview(composite);
 
         GridData browserData = new GridData(GridData.FILL_BOTH);
         browserData.widthHint = convertWidthInCharsToPixels(fWidth / 2);
         browserData.heightHint = convertHeightInCharsToPixels(fHeight);
         browserData.minimumWidth = convertWidthInCharsToPixels(fWidth / 2);
         browserData.minimumHeight = convertHeightInCharsToPixels(fHeight);
         comp.setLayoutData(browserData);
 
         GridData data = new GridData(GridData.FILL_BOTH);
         data.widthHint = convertWidthInCharsToPixels(fWidth);
         data.heightHint = convertHeightInCharsToPixels(fHeight);
 
         Tree treeWidget = treeViewer.getTree();
         treeWidget.setLayoutData(data);
         treeWidget.setFont(parent.getFont());
 
         filterCombo = new Combo(composite, SWT.READ_ONLY | SWT.BORDER);
         filterCombo.setLayoutData(new GridData(GridData.FILL, GridData.CENTER, false, false));
 
         filterCombo.add(JstUIMessages.ALL_FILES);
         filterCombo.add(JstUIMessages.ALL_IMAGE_FILES);
 
         for (int i = 0; i < fileExtensions.length; i++) {
             String[] str = fileExtensions[i];
             StringBuffer buf = new StringBuffer();
 
             for (int j = 0; j < str.length; j++) {
                 buf.append("*." + str[j].toUpperCase() + "; "); //$NON-NLS-1$ //$NON-NLS-2$
             }
 
             filterCombo.add(buf.toString());
         }
 
         filterCombo.select(0);
         filterCombo.addSelectionListener(new SelectionListener() {
                 public void widgetDefaultSelected(SelectionEvent e) {
                 }
 
                 public void widgetSelected(SelectionEvent e) {
                     if (fViewer != null) {
                         ViewerFilter[] filters = fViewer.getFilters();
 
                         if (filters != null) {
                             for (int i = 0; i < filters.length; i++) {
                                 fViewer.removeFilter(filters[i]);
                             }
                         }
 
                         if (filterCombo.getSelectionIndex() != 0) {
                             if (filterCombo.getSelectionIndex() == 1) {
                                 Set<String> s = new HashSet<String>();
 
                                 for (int i = 0; i < fileExtensions.length; i++) {
                                     String[] tmp = fileExtensions[i];
 
                                     for (int j = 0; j < tmp.length; j++)
                                         s.add(tmp[j]);
                                 }
 
                                 String[] allExtensions = new String[s.size()];
                                 s.toArray(allExtensions);
                                 fViewer.addFilter(new FileExtensionFilter(allExtensions));
                             } else {
                                 fViewer.addFilter(new FileExtensionFilter(
                                         fileExtensions[filterCombo.getSelectionIndex() - 2]));
                             }
                         }
 
                         fViewer.refresh();
 
                         if (fViewer.getTree().getItemCount() <= 0) {
                             fIsEmpty = true;
                         } else {
                             fIsEmpty = false;
                         }
 
                         if (fViewer.getTree().getSelectionCount() <= 0) {
                             file = null;
                             canvas.redraw();
                         }
 
                         updateOKStatus();
                     }
                 }
             });
 
         if (fIsEmpty) {
             messageLabel.setEnabled(false);
             treeWidget.setEnabled(false);
         }
 
         //JBIDE-3084, implementation of default validator
         fValidator = new ISelectionStatusValidator() {
                     public IStatus validate(Object[] selection) {
                         if ((selection != null) && (selection.length == 1)) {
                             Object selecObject = selection[0];
 
                             if (selecObject instanceof IFile) {
                                 return Status.OK_STATUS;
                             }
                         }
 
                         return new Status(IStatus.ERROR, PlatformUI.PLUGIN_ID, IStatus.ERROR,
                         		JstUIMessages.ImageSelectionDialog_InvalidImageFile, null);
                     }
                 };
 
         return composite;
     }
 
     /**
      * Creates the tree viewer.
      *
      * @param parent
      *                the parent composite
      * @return the tree viewer
      */
     protected TreeViewer createTreeViewer(Composite parent) {
         int style = SWT.BORDER | (fAllowMultiple ? SWT.MULTI : SWT.SINGLE);
 
         fViewer = new TreeViewer(new Tree(parent, style));
         fViewer.setContentProvider(fContentProvider);
         fViewer.setLabelProvider(fLabelProvider);
         fViewer.addSelectionChangedListener(new ISelectionChangedListener() {
                 @SuppressWarnings("unchecked")
                 public void selectionChanged(SelectionChangedEvent event) {
                     access$setResult(((IStructuredSelection) event.getSelection()).toList());
                     updateOKStatus();
                 }
             });
 
         fViewer.getTree().addSelectionListener(new SelectionListener() {
                 public void widgetDefaultSelected(SelectionEvent e) {
                 }
 
                 public void widgetSelected(SelectionEvent e) {
                     Object obj = getFirstResult();
 
                     if (obj instanceof IFile) {
                         file = (IFile) getFirstResult();
                     } else if (obj instanceof IFolder) {
                         file = null;
                     }
 
                     canvas.redraw();
                 }
             });
 
         fViewer.setComparator(fComparator);
 
         if (fFilters != null) {
             for (int i = 0; i != fFilters.size(); i++) {
                 fViewer.addFilter(fFilters.get(i));
             }
         }
 
         if (fDoubleClickSelects) {
             Tree tree = fViewer.getTree();
             tree.addSelectionListener(new SelectionAdapter() {
                     public void widgetDefaultSelected(SelectionEvent e) {
                         updateOKStatus();
 
                         if (fCurrStatus.isOK()) {
                             access$superButtonPressed(IDialogConstants.OK_ID);
                         }
                     }
                 });
         }
 
         fViewer.addDoubleClickListener(new IDoubleClickListener() {
                 public void doubleClick(DoubleClickEvent event) {
                     updateOKStatus();
 
                     // If it is not OK or if double click does not
                     // select then expand
                     if (!(fDoubleClickSelects && fCurrStatus.isOK())) {
                         ISelection selection = event.getSelection();
 
                         if (selection instanceof IStructuredSelection) {
                             Object item = ((IStructuredSelection) selection).getFirstElement();
 
                             if (fViewer.getExpandedState(item)) {
                                 fViewer.collapseToLevel(item, 1);
                             } else {
                                 fViewer.expandToLevel(item, 1);
                             }
                         }
                     }
                 }
             });
 
         fViewer.setInput(fInput);
 
         return fViewer;
     }
 
     /**
      * Returns the tree viewer.
      *
      * @return the tree viewer
      */
     protected TreeViewer getTreeViewer() {
         return fViewer;
     }
 
     /**
      * Set the result using the super class implementation of buttonPressed.
      *
      * @param id
      * @see org.eclipse.jface.dialogs.Dialog#buttonPressed(int)
      */
     protected void access$superButtonPressed(int id) {
         super.buttonPressed(id);
     }
 
     /**
      * Set the result using the super class implementation of setResult.
      *
      * @param result
      * @see SelectionStatusDialog#setResult(int, Object)
      */
     protected void access$setResult(List<Object> result) {
         super.setResult(result);
     }
 
     /**
      * @see org.eclipse.jface.window.Window#handleShellCloseEvent()
      */
     protected void handleShellCloseEvent() {
         super.handleShellCloseEvent();
 
         // Handle the closing of the shell by selecting the close icon
         if (getReturnCode() == CANCEL) {
             setResult(null);
         }
     }
 
     private void access$superCreate() {
         super.create();
     }
 
     /**
      * Create image preview control
      *
      * @param parent
      * @return composite
      */
     private Composite createPreview(Composite parent) {
         Composite composite = new Composite(parent, SWT.NONE);
         GridLayout layout = new GridLayout();
         layout.numColumns = 1;
         composite.setLayout(layout);
         composite.setLayoutData(new GridData(GridData.FILL_BOTH));
 
         Label label = new Label(composite, SWT.NONE);
         label.setLayoutData(new GridData(GridData.CENTER, GridData.CENTER, false, false));
         label.setText(JstUIMessages.IMAGE_PREVIEW);
         canvas = new Canvas(composite, SWT.BORDER);
 
         GridData browserData = new GridData(GridData.FILL_BOTH);
         browserData.widthHint = convertWidthInCharsToPixels((fWidth) / 2);
         browserData.heightHint = convertHeightInCharsToPixels(fHeight);
         canvas.setLayoutData(browserData);
 
         resolution = new Text(composite, SWT.READ_ONLY | SWT.CENTER);
         resolution.setLayoutData(new GridData(GridData.FILL, GridData.CENTER, false, false));
         resolution.setVisible(false);
 
         emptyColor = canvas.getForeground();
         List<?> list = getInitialElementSelections();
         if(!list.isEmpty()) {
         	Object o = list.iterator().next();
         	if(o instanceof IFile) {
         		file = (IFile)o;
         	}
         }
         canvas.addPaintListener(new PaintListener() {
                 public void paintControl(PaintEvent e) {
                     GC gc = new GC(canvas);
                     try {
                     	gc.setForeground(emptyColor);
                     	gc.fillRectangle(1, 1, canvas.getSize().x - 2, canvas.getSize().y - 2);
                     	//resolution.setText("");
                     	resolution.setVisible(false);
 
                     	if (file != null) {
                     		Cursor parentCursor = getShell().getCursor();
                    		final Cursor waitCursor = new Cursor(getShell().getDisplay(), SWT.CURSOR_WAIT);
                     		Point previewPoint = new Point(0, 0);
                     		Point labelPoint = canvas.getSize();
                     		InputStream stream = null;
 
                     		try {
                    			getShell().setCursor(waitCursor);
                     			stream = new FileInputStream(file.getLocation().toOSString());
 
                     			ImageData imageData = new ImageData(stream);
                     			stream.close();
 
                     			if (imageData != null) {
                     				Image image = new Image(getShell().getDisplay(), imageData);
 
                     				// set image in center
                     				Point imagePoint = new Point(image.getBounds().width,
                                         image.getBounds().height);
 
                     				String imageInfo = imagePoint.x + " x " + imagePoint.y + " px"; //$NON-NLS-1$ //$NON-NLS-2$
 
                     				// change resolution if image anymore image label
                     				if ((imagePoint.x > labelPoint.x) || (imagePoint.y > labelPoint.y)) {
                     					float ratioImage = (float) imagePoint.x / (float) imagePoint.y;
 
                     					if (((imagePoint.y > labelPoint.y) &&
                                             ((labelPoint.y * ratioImage) > labelPoint.x)) ||
                                             ((imagePoint.x > labelPoint.x) &&
                                             ((labelPoint.x / ratioImage) < labelPoint.y))) {
                     						imageData = imageData.scaledTo(labelPoint.x - 10,
                                                 (int) (labelPoint.x / ratioImage));
                     					} else {
                     						imageData = imageData.scaledTo((int) (labelPoint.y * ratioImage) -
                                                 10, labelPoint.y);
                     					}
 
                     					image.dispose();
                     					image = new Image(getShell().getDisplay(), imageData);
                     					imagePoint.x = image.getBounds().width;
                     					imagePoint.y = image.getBounds().height;
                     				}
 
                     				previewPoint.x = (labelPoint.x / 2) - (imagePoint.x / 2);
                     				previewPoint.y = (labelPoint.y / 2) - (imagePoint.y / 2);
                     				gc.drawImage(image, previewPoint.x, previewPoint.y);
                     				resolution.setVisible(true);
                     				resolution.setText(imageInfo);
                     				image.dispose();
                     			}
                     		} catch (IOException ev) {
                     			//ignore
                     		} catch (SWTException ex) {
                     			//ignore (if select not image file) 
                     		} finally {
                     			getShell().setCursor(parentCursor);
                     		}
                     	}
                 	} finally {
                         gc.dispose();
                 	}
                 }
             });
 
         return composite;
     }
 
     private boolean evaluateIfTreeEmpty(Object input) {
         Object[] elements = fContentProvider.getElements(input);
 
         if (elements.length > 0) {
             if (fFilters != null) {
                 for (int i = 0; i < fFilters.size(); i++) {
                     ViewerFilter curr = fFilters.get(i);
                     elements = curr.filter(fViewer, input, elements);
                 }
             }
         }
 
         return elements.length == 0;
     }
 }
