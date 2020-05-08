 package de.objectcode.time4u.client.ui.controls;
 
 import java.text.MessageFormat;
 
 import org.eclipse.core.runtime.Assert;
 import org.eclipse.jface.viewers.CellEditor;
 import org.eclipse.jface.viewers.ComboViewer;
 import org.eclipse.jface.viewers.IContentProvider;
 import org.eclipse.jface.viewers.ILabelProvider;
 import org.eclipse.jface.viewers.ISelection;
 import org.eclipse.jface.viewers.IStructuredSelection;
 import org.eclipse.jface.viewers.StructuredSelection;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.events.FocusAdapter;
 import org.eclipse.swt.events.FocusEvent;
 import org.eclipse.swt.events.KeyAdapter;
 import org.eclipse.swt.events.KeyEvent;
 import org.eclipse.swt.events.SelectionAdapter;
 import org.eclipse.swt.events.SelectionEvent;
 import org.eclipse.swt.events.TraverseEvent;
 import org.eclipse.swt.events.TraverseListener;
 import org.eclipse.swt.layout.FillLayout;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Control;
 
 import de.objectcode.time4u.client.ui.dnd.ProjectTaskHolder;
 
 public class ComboViewerCellEditor extends CellEditor
 {
   Object m_selection;
   ComboViewer m_viewer;
   IContentProvider m_contentProvider;
   ILabelProvider m_labelProvider;
 
   private static final int defaultStyle = SWT.SIMPLE | SWT.READ_ONLY | SWT.DROP_DOWN;
 
   public ComboViewerCellEditor()
   {
     setStyle(defaultStyle);
   }
 
   public ComboViewerCellEditor(final Composite parent, final IContentProvider contentProvider,
       final ILabelProvider labelProvider)
   {
     this(parent, contentProvider, labelProvider, defaultStyle);
   }
 
   public ComboViewerCellEditor(final Composite parent, final IContentProvider contentProvider,
       final ILabelProvider labelProvider, final int style)
   {
     setStyle(style);
 
     m_contentProvider = contentProvider;
     m_labelProvider = labelProvider;
 
     create(parent);
   }
 
   @Override
   protected Control createControl(final Composite parent)
   {
     // Workaround for focus problem in GTK
     final Composite top = new Composite(parent, SWT.NO_FOCUS);
     top.setLayout(new FillLayout());
    m_viewer = new ComboViewer(top, getStyle());
     m_viewer.setContentProvider(m_contentProvider);
     m_viewer.setLabelProvider(m_labelProvider);
     m_viewer.getControl().setFont(parent.getFont());
 
    m_viewer.getCombo().addKeyListener(new KeyAdapter() {
       // hook key pressed - see PR 14201
       @Override
       public void keyPressed(final KeyEvent e)
       {
         keyReleaseOccured(e);
       }
     });
 
    m_viewer.getCombo().addSelectionListener(new SelectionAdapter() {
       @Override
       public void widgetDefaultSelected(final SelectionEvent event)
       {
         applyEditorValueAndDeactivate();
       }
 
       @Override
       public void widgetSelected(final SelectionEvent event)
       {
         final ISelection selection = m_viewer.getSelection();
         Assert.isTrue(selection != null && selection instanceof IStructuredSelection);
 
         m_selection = ((IStructuredSelection) selection).getFirstElement();
       }
     });
 
    m_viewer.getCombo().addTraverseListener(new TraverseListener() {
       public void keyTraversed(final TraverseEvent e)
       {
         if (e.detail == SWT.TRAVERSE_ESCAPE || e.detail == SWT.TRAVERSE_RETURN) {
           e.doit = false;
         }
       }
     });
 
    m_viewer.getCombo().addFocusListener(new FocusAdapter() {
       @Override
       public void focusLost(final FocusEvent e)
       {
         final Control focusControl = top.getDisplay().getFocusControl();
 
         if (focusControl != m_viewer.getControl()) {
           ComboViewerCellEditor.this.focusLost();
         }
       }
     });
     return top;
   }
 
   @Override
   protected Object doGetValue()
   {
     final ISelection selection = m_viewer.getSelection();
     Assert.isTrue(selection != null && selection instanceof IStructuredSelection);
 
     return ((IStructuredSelection) selection).getFirstElement();
   }
 
   @Override
   protected void doSetFocus()
   {
     m_viewer.getCombo().setFocus();
   }
 
   @Override
   protected void doSetValue(final Object value)
   {
     if (value instanceof ProjectTaskHolder) {
       m_viewer.setInput(((ProjectTaskHolder) value).getProject());
       m_viewer.setSelection(new StructuredSelection(((ProjectTaskHolder) value).getTask()));
     }
   }
 
   /**
    * Applies the currently selected value and deactiavates the cell editor
    */
   void applyEditorValueAndDeactivate()
   {
     // must set the selection before getting value
     final ISelection selection = m_viewer.getSelection();
     Assert.isTrue(selection != null && selection instanceof IStructuredSelection);
     m_selection = ((IStructuredSelection) selection).getFirstElement();
     final Object newValue = doGetValue();
     markDirty();
     final boolean isValid = isCorrect(newValue);
     setValueValid(isValid);
 
     if (!isValid) {
       // Since we don't have a valid index, assume we're using an 'edit'
       // combo so format using its text value
       setErrorMessage(MessageFormat.format(getErrorMessage(), new Object[] { m_viewer.getCombo().getText() }));
     }
 
     fireApplyEditorValue();
     deactivate();
   }
 
   @Override
   protected void focusLost()
   {
     if (isActivated()) {
       applyEditorValueAndDeactivate();
     }
   }
 
   @Override
   protected void keyReleaseOccured(final KeyEvent keyEvent)
   {
     if (keyEvent.character == '\u001b') { // Escape character
       fireCancelEditor();
     } else if (keyEvent.character == '\t') { // tab key
       applyEditorValueAndDeactivate();
     }
   }
 }
