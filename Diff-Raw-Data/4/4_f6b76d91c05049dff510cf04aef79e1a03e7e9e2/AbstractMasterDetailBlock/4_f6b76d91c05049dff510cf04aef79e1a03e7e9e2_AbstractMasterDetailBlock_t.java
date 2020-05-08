 package org.eclipse.jst.jsf.common.ui.internal.form;
 
 import java.util.List;
 
 import org.eclipse.jface.viewers.ISelectionChangedListener;
 import org.eclipse.jface.viewers.IStructuredSelection;
 import org.eclipse.jface.viewers.SelectionChangedEvent;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.custom.BusyIndicator;
 import org.eclipse.swt.custom.SashForm;
 import org.eclipse.swt.custom.StackLayout;
 import org.eclipse.swt.layout.GridData;
 import org.eclipse.swt.layout.GridLayout;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.ui.forms.widgets.Form;
 import org.eclipse.ui.forms.widgets.FormToolkit;
 
 /**
  * An alternative to MasterDetailBlock that doesn't use the ScrolledForm
  * that causes problems as described here:
  * 
  * http://dev.eclipse.org/newslists/news.eclipse.platform/msg73145.html
 
  * @author cbateman
  *
  */
 public abstract class AbstractMasterDetailBlock implements
         ISelectionChangedListener
 {
     private AbstractMasterForm        _masterForm;
     private Composite                 _detailsPanel;
     private AbstractDetailsForm       _curPage;
     private FormToolkit               _toolkit;
     private StackLayout               _detailLayout;
     private List<AbstractDetailsForm> _detailForms;
     private BlankDetailsForm          _blankDetails;
 
     /**
      * 
      */
     protected AbstractMasterDetailBlock()
     {
         super();
     }
 
     /**
      * Disposes the master detail form
      */
     public final void dispose()
     {
         _masterForm.dispose();
 
         for (final AbstractDetailsForm detailsForm : _detailForms)
         {
             detailsForm.dispose();
         }
 
         doDispose();
     }
 
     /**
      * Override to get custom dispose logic. Do not use this to dispose of
      * master or details forms. This is done automatically for you before
      * doDispose is called.
      */
     protected void doDispose()
     {
         // do nothing by default
     }
 
     /**
      * @param toolkit
      * @param form
      */
     public final void createContent(final FormToolkit toolkit, final Form form)
     {
         _toolkit = toolkit;
 
         final GridLayout layout = new GridLayout();
        layout.marginWidth = 5;
        layout.marginHeight = 5;
         form.getBody().setLayout(layout);
         final SashForm sashForm = new SashForm(form.getBody(), SWT.NULL);
         // sashForm.setData("form", managedForm); //$NON-NLS-1$
         _toolkit.adapt(sashForm, false, false);
         sashForm.setMenu(form.getBody().getMenu());
         sashForm.setLayoutData(new GridData(GridData.FILL_BOTH));
         initializeMasterPart(_toolkit, sashForm);
         createDetailsPart(sashForm);
 
         _masterForm.createHead(form);
         createToolBarActions(form);
         form.updateToolBar();
     }
 
     /**
      * @param toolkit
      * @return the master form.
      */
     protected abstract AbstractMasterForm createMasterPart(
             final FormToolkit toolkit);
 
     private void initializeMasterPart(final FormToolkit toolkit,
             final Composite parent)
     {
         _masterForm = createMasterPart(toolkit);
         _masterForm.initialize(this);
         _masterForm.createClientArea(parent);
     }
 
     private void createToolBarActions(final Form form)
     {
         _masterForm.contributeActions(form.getToolBarManager());
     }
 
     private void createDetailsPart(final Composite parent)
     {
         _detailsPanel = new Composite(parent, SWT.NONE);
         _detailLayout = new StackLayout();
         _detailsPanel.setLayout(_detailLayout);
 
         _detailForms = createDetailPages();
 
         for (final AbstractDetailsForm detailForm : _detailForms)
         {
             detailForm.initialize(_toolkit);
             detailForm.createContents(_detailsPanel);
         }
 
         // create default blank page
         _blankDetails = new BlankDetailsForm();
         _blankDetails.initialize(_toolkit);
         _blankDetails.createContents(_detailsPanel);
 
         _curPage = _blankDetails;
         _detailLayout.topControl = _curPage.getControl();
         _detailsPanel.layout();
     }
 
     public final void selectionChanged(final SelectionChangedEvent event)
     {
         final Object selectedObj = ((IStructuredSelection) event.getSelection())
                 .getFirstElement();
         final AbstractDetailsForm page = selectPage(selectedObj);
         if (page != null)
         {
             final AbstractDetailsForm fpage = page;
             BusyIndicator.showWhile(_detailsPanel.getDisplay(), new Runnable()
             {
                 public void run()
                 {
                     final AbstractDetailsForm oldPage = _curPage;
                     _curPage = fpage;
                     // commit the current page
                     if (oldPage != null && oldPage.isDirty())
                     {
                         oldPage.commit(false);
                     }
                     // refresh the new page
                     if (fpage.isStale())
                     {
                         fpage.refresh();
                     }
                     _curPage.selectionChanged(event.getSelection());
                     // _pageBook.showPage(_curPage.getTextSection().getControl());
                     _detailLayout.topControl = _curPage.getControl();
                     _detailsPanel.layout();
                 }
             });
         }
     }
 
     /**
      * @return a list of detail forms
      */
     protected abstract List<AbstractDetailsForm> createDetailPages();
 
     /**
      * @param forModel
      * @return the details form for 'forModel'. If implementer returns null,
      *         then a blank page will be provided
      */
     protected abstract AbstractDetailsForm doSelectPage(final Object forModel);
 
     private AbstractDetailsForm selectPage(final Object forModel)
     {
         final AbstractDetailsForm clientForm = doSelectPage(forModel);
 
         if (clientForm != null)
         {
             return clientForm;
         }
         return _blankDetails;
     }
 
 }
