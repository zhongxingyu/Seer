 /*******************************************************************************
  * Copyright (c) 2004, 2014 QNX Software Systems and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     QNX Software Systems - Initial API and implementation
  *     Wind River Systems - Adapted to TCF
  *******************************************************************************/
 package org.eclipse.tcf.internal.cdt.ui.breakpoints;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 import java.util.TreeSet;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import org.eclipse.core.runtime.IAdaptable;
 import org.eclipse.debug.core.DebugPlugin;
 import org.eclipse.debug.core.ILaunch;
 import org.eclipse.debug.core.ILaunchConfiguration;
 import org.eclipse.debug.core.ILaunchManager;
 import org.eclipse.debug.ui.DebugUITools;
 import org.eclipse.debug.ui.IDebugUIConstants;
 import org.eclipse.jface.dialogs.IDialogSettings;
 import org.eclipse.jface.fieldassist.ControlDecoration;
 import org.eclipse.jface.fieldassist.FieldDecoration;
 import org.eclipse.jface.fieldassist.FieldDecorationRegistry;
 import org.eclipse.jface.resource.ImageDescriptor;
 import org.eclipse.jface.viewers.CheckStateChangedEvent;
 import org.eclipse.jface.viewers.CheckboxTreeViewer;
 import org.eclipse.jface.viewers.ICheckStateListener;
 import org.eclipse.jface.viewers.ITreeContentProvider;
 import org.eclipse.jface.viewers.LabelProvider;
 import org.eclipse.jface.viewers.Viewer;
 import org.eclipse.jface.window.Window;
 import org.eclipse.osgi.util.NLS;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.events.DisposeEvent;
 import org.eclipse.swt.events.DisposeListener;
 import org.eclipse.swt.events.ModifyEvent;
 import org.eclipse.swt.events.ModifyListener;
 import org.eclipse.swt.events.SelectionEvent;
 import org.eclipse.swt.events.SelectionListener;
 import org.eclipse.swt.graphics.Image;
 import org.eclipse.swt.layout.GridData;
 import org.eclipse.swt.layout.GridLayout;
 import org.eclipse.swt.widgets.Button;
 import org.eclipse.swt.widgets.Combo;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Event;
 import org.eclipse.swt.widgets.Label;
 import org.eclipse.swt.widgets.Link;
 import org.eclipse.swt.widgets.Listener;
 import org.eclipse.swt.widgets.Shell;
 import org.eclipse.tcf.internal.cdt.ui.Activator;
 import org.eclipse.tcf.internal.cdt.ui.ImageCache;
 import org.eclipse.tcf.internal.cdt.ui.preferences.BreakpointPreferencePage;
 import org.eclipse.tcf.internal.debug.model.TCFLaunch;
 import org.eclipse.tcf.internal.debug.ui.model.TCFChildren;
 import org.eclipse.tcf.internal.debug.ui.model.TCFModel;
 import org.eclipse.tcf.internal.debug.ui.model.TCFModelManager;
 import org.eclipse.tcf.internal.debug.ui.model.TCFNode;
 import org.eclipse.tcf.internal.debug.ui.model.TCFNodeExecContext;
 import org.eclipse.tcf.protocol.IChannel;
 import org.eclipse.tcf.protocol.IToken;
 import org.eclipse.tcf.services.IContextQuery;
 import org.eclipse.tcf.services.IRunControl;
 import org.eclipse.tcf.util.TCFDataCache;
 import org.eclipse.tcf.util.TCFTask;
 import org.eclipse.ui.dialogs.PreferencesUtil;
 
 public class TCFThreadFilterEditor {
 
     public static final String PLUGIN_ID="org.eclipse.tcf.internal.cdt.ui.breakpoints.TCFThreadFilterEditor"; //$NON-NLS-1$
 
     private static class Context {
         private final String fName;
         private final String fId;
         private final String fParentId;
         private final boolean fIsContainer;
         private final String fScopeId;
         private final String fSessionId;
         private final String fBpGroup;
         private final Object fAdditionalInfo;
 
         Context(IRunControl.RunControlContext ctx, Context parent) {
             this(ctx, parent.fSessionId);
         }
 
         Context(IRunControl.RunControlContext ctx, String sessionId) {
             fName =  ctx.getName();
             fSessionId = sessionId;
             fScopeId = sessionId != null ? sessionId + '/' + ctx.getID() : ctx.getID();
             fId = ctx.getID();
             fParentId = ctx.getParentID();
             fIsContainer = !ctx.hasState();
             fBpGroup = ctx.getBPGroup();
             fAdditionalInfo = ctx.getProperties().get("AdditionalInfo"); //$NON-NLS-1$
         }
 
 
         @Override
         public boolean equals(Object obj) {
             return obj instanceof Context && fScopeId.equals(((Context)obj).fScopeId);
         }
 
         @Override
         public int hashCode() {
             return fScopeId.hashCode();
         }
     }
 
     public class CheckHandler implements ICheckStateListener {
         public void checkStateChanged(CheckStateChangedEvent event) {
             Object element = event.getElement();
             boolean checked = event.getChecked();
             if (checked) {
                 getThreadViewer().expandToLevel(element, 1);
             }
             if (element instanceof Context) {
                 Context ctx = (Context) element;
                 checkContext(ctx, checked);
                 updateParentCheckState(ctx);
             }
             else if (element instanceof ILaunch) {
                 checkLaunch((ILaunch) element, checked);
             }
         }
 
         private void checkLaunch(ILaunch launch, boolean checked) {
             getThreadViewer().setChecked(launch, checked);
             getThreadViewer().setGrayed(launch, false);
             Object[] threads = fContentProvider.getChildren(launch);
             for (int i = 0; i < threads.length; i++) {
                 checkContext((Context) threads[i], checked);
             }
         }
 
         /**
          * Check or uncheck a context in the tree viewer. When a container
          * is checked, attempt to check all of the containers threads by
          * default. When a container is unchecked, uncheck all its threads.
          */
         private void checkContext(Context ctx, boolean checked) {
             if (ctx.fIsContainer) {
                 Object[] threads = fContentProvider.getChildren(ctx);
                 for (int i = 0; i < threads.length; i++) {
                     checkContext((Context) threads[i], checked);
                 }
             }
             checkThread(ctx, checked);
         }
 
         /**
          * Check or uncheck a thread.
          */
         private void checkThread(Context thread, boolean checked) {
             getThreadViewer().setChecked(thread, checked);
             getThreadViewer().setGrayed(thread, false);
         }
 
         private void updateParentCheckState(Context thread) {
             Object[] threads;
             Object parent = getContainer(thread);
             if (parent == null) {
                 parent = getLaunch(thread);
                 if (parent == null) return;
             }
             threads = fContentProvider.getChildren(parent);
             int checkedNumber = 0;
             int grayedNumber = 0;
             for (int i = 0; i < threads.length; i++) {
                 if (getThreadViewer().getGrayed(threads[i])) {
                     ++grayedNumber;
                 }
                 else if (getThreadViewer().getChecked(threads[i])) {
                     ++checkedNumber;
                 }
             }
             if (checkedNumber + grayedNumber == 0) {
                 getThreadViewer().setChecked(parent, false);
                 getThreadViewer().setGrayed(parent, false);
             }
             else if (checkedNumber == threads.length) {
                 getThreadViewer().setChecked(parent, true);
                 getThreadViewer().setGrayed(parent, false);
             }
             else {
                 getThreadViewer().setGrayChecked(parent, true);
             }
             if (parent instanceof Context) {
                 updateParentCheckState((Context) parent);
             }
         }
     }
 
     public class ThreadFilterContentProvider implements ITreeContentProvider {
 
         public Object[] getChildren(Object parent) {
             if (parent instanceof Context) {
                 return filterList(syncGetThreads((Context) parent));
             }
             if (parent instanceof ILaunch) {
                 return filterList(syncGetContainers((TCFLaunch) parent));
             }
             if (parent instanceof ILaunchManager) {
                 return filterList(getLaunches());
             }
             return new Object[0];
         }
 
         public Object[] filterList(Object[] resultArray) {
             ArrayList<Object> filteredList = new ArrayList<Object>();
             String filterExpr = null;
             if (scopeExprCombo != null) {
                 filterExpr = scopeExprCombo.getText();
             }
             for (Object obj : resultArray) {
                 if (obj instanceof ILaunch || obj instanceof ILaunchManager) {
                     filteredList.add(obj);
                 } else if (obj instanceof Context) {
                    Context context = (Context)obj;
                    // Add element to list if:
                    // Check if context in result of query expression (if query expression was specitifed).
                    // Also check if breakpoint group is valid on context.
                    // Finally, check if contexts' children are not filtered out.
                    if ( (filterExpr == null || filterExpr.length() == 0 || fContextList.contains(context.fId))
                         && context.fBpGroup != null)
                    {
                        filteredList.add(obj);
                       fFilteredContexts.add(obj);
                    } else if (context.fIsContainer) {
                        Object[] childArray = getChildren(obj);
                        if (childArray != null && childArray.length != 0) {
                            filteredList.add(obj);
                           fFilteredContexts.add(obj);
                        }
                    }
                 }
             }
             return filteredList.toArray(new Object[filteredList.size()]);
         }
 
         public Object getParent(Object element) {
             if (element instanceof Context) {
                 Context ctx = (Context) element;
                 if (ctx.fParentId == null) {
                     return DebugPlugin.getDefault().getLaunchManager();
                 }
                 else {
                     return getContainer(ctx);
                 }
             }
             return null;
         }
 
         public boolean hasChildren(Object element) {
             return getChildren(element).length > 0;
         }
 
         public Object[] getElements(Object inputElement) {
            return getChildren(inputElement);
         }
 
         public void dispose() {
         }
 
         public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
         }
     }
 
     public class ThreadFilterLabelProvider extends LabelProvider  {
 
         @Override
         public Image getImage(Object element) {
             if (element instanceof Context) {
                 Context ctx = (Context) element;
                 if (ctx.fIsContainer) {
                     return DebugUITools.getImage(IDebugUIConstants.IMG_OBJS_DEBUG_TARGET);
                 }
                 else {
                     return DebugUITools.getImage(IDebugUIConstants.IMG_OBJS_THREAD_RUNNING);
                 }
             }
             if (element instanceof ILaunch) {
                 ImageDescriptor desc = DebugUITools.getDefaultImageDescriptor(element);
                 if (desc != null) return ImageCache.getImage(desc);
             }
             return null;
         }
 
         @Override
         public String getText(Object element) {
             if (element instanceof Context) {
                 Context ctx = (Context) element;
                 String s = ctx.fName;
                 if (s == null) s = ctx.fId;
                 if (ctx.fAdditionalInfo != null) s += ctx.fAdditionalInfo.toString();
                 return s;
             }
             if (element instanceof ILaunch) {
                 ILaunchConfiguration config = ((ILaunch) element).getLaunchConfiguration();
                 if (config != null) return config.getName();
             }
             return "?"; //$NON-NLS-1$
         }
     }
 
     private TCFBreakpointThreadFilterPage fPage;
     private CheckboxTreeViewer fThreadViewer;
     private final ThreadFilterContentProvider fContentProvider;
     private final CheckHandler fCheckHandler;
     private final List<Context> fContexts = new ArrayList<Context>();
     private final Set<Object> fFilteredContexts = new HashSet<Object>();
     private final Map<TCFLaunch, Context[]> fContainersPerLaunch = new HashMap<TCFLaunch, Context[]>();
     private final Map<Context, Context[]> fContextsPerContainer = new HashMap<Context, Context[]>();
     private Combo scopeExprCombo;
     private ControlDecoration scopeExpressionDecoration;
     private final Set<String> fContextList = new TreeSet<String>();
     private Link preferencesLink;
 
     /**
      * Returns the dialog settings or <code>null</code> if none
      *
      * @param create
      *            whether to create the settings
      */
     private IDialogSettings getDialogSettings(boolean create) {
         IDialogSettings settings = Activator.getDefault()
                 .getDialogSettings();
         IDialogSettings section = settings.getSection(this.getClass()
                 .getName());
         if (section == null & create) {
             section = settings.addNewSection(this.getClass().getName());
         }
         return section;
     }
 
     public TCFThreadFilterEditor(Composite parent, TCFBreakpointThreadFilterPage page) {
         fPage = page;
         fContentProvider = new ThreadFilterContentProvider();
         fCheckHandler = new CheckHandler();
         Composite mainComposite = new Composite(parent, SWT.NONE);
         mainComposite.setFont(parent.getFont());
         GridData gd = new GridData(SWT.FILL, SWT.FILL, true, true);
         gd.widthHint = 500;
         mainComposite.setLayoutData(gd);
         mainComposite.setLayout(new GridLayout(2,false));
         createThreadViewer(mainComposite);
     }
 
     protected TCFBreakpointThreadFilterPage getPage() {
         return fPage;
     }
 
     private String getBPFilterExpression() {
         return fPage.getFilterExtension().getPropertiesFilter();
     }
 
     private String[] getAvailableAttributes() {
         String[] result = null;
         TCFLaunch launch = (TCFLaunch)getAttributeLaunch();
         if (launch == null || launch.isTerminated()) {
             return result;
         }
         final IChannel channel = launch.getChannel();
         if (channel == null) {
             return result;
         }
         result = new TCFTask<String[]>(channel) {
             public void run() {
                 IContextQuery service = channel.getRemoteService(IContextQuery.class);
                 service.getAttrNames(new IContextQuery.DoneGetAttrNames() {
                     public void doneGetAttrNames(IToken token, Exception error, String[] attributes) {
                         if (error != null) {
                             done(null);
                         }
                         done(attributes);
                     }
                 });
                 return;
             }
         }.getE();
         return result;
     }
 
     /**
      * Validate a Context query using the context query service.
      *   If the query is valid, also get the list of filtered contexts.
      *
      * @param query  The query to validate
      * @return       Error String if validation has failed, else null.
      */
      private String getQueryFilteredContexts (final String query, final Set<String> contextList) {
 
          TCFLaunch launch = (TCFLaunch)getAttributeLaunch();
          if (launch == null || launch.isTerminated()) {
              return Messages.TCFThreadFilterEditorNoOpenChannel;
          }
          final IChannel channel = launch.getChannel();
 
          if (channel == null){
              return Messages.TCFThreadFilterEditorNoOpenChannel;
          }
          String result = new TCFTask<String>() {
              public void run() {
                  IContextQuery service = channel.getRemoteService(IContextQuery.class);
                  service.query(query, new IContextQuery.DoneQuery() {
                      public void doneQuery (IToken token, Exception error, String[] contexts) {
                          if (error != null) {
                              done(TCFModel.getErrorMessage(error, false));
                          }
                          else {
                              for (String context : contexts) {
                                  contextList.add(context);
                              }
                              done(null);
                          }
                      }
                  });
                  return;
              }
          }.getE();
          return result;
      }
 
     String validateBasicSyntax(String expression) {
 
         String result = null;
         // Ignore content in double-quotes.
         Pattern p = Pattern.compile("\"((?:\\\\\\\\|\\\\\"|[^\"])*)\""); //$NON-NLS-1$
         Matcher m = p.matcher(expression);
         expression = m.replaceAll("temp"); //$NON-NLS-1$
 
         // No whitespace
         if (expression.matches("^(.*?)(\\s)(.*)$")) //$NON-NLS-1$
             return Messages.TCFThreadFilterEditorFormatError;
 
         // Check characters around equals.
         if (expression.matches("^(.*?)[^a-zA-Z0-9_]=[^a-zA-Z0-9_](.*)$")) //$NON-NLS-1$
             return Messages.TCFThreadFilterEditorUnbalancedParameters;
 
         return result;
     }
 
     private class ExpressionModifier implements ModifyListener {
         public void modifyText(ModifyEvent e) {
             String expression = scopeExprCombo.getText();
             String error = validateBasicSyntax(expression);
             if (error != null) {
                 scopeExpressionDecoration.show();
                 fPage.setErrorMessage(error);
                 fPage.setValid(false);
             }
             else {
                 fContextList.clear();
                 if (expression != null && expression.length() != 0)
                     error = getQueryFilteredContexts (expression, fContextList);
                 if (error == null ) {
                     scopeExpressionDecoration.hide();
                     fPage.setErrorMessage(null);
                     fPage.setValid(true);
                 }
                 else if (error == Messages.TCFThreadFilterEditorNoOpenChannel) {
                     // if no open channel, allow to edit expression nonetheless
                     scopeExpressionDecoration.hide();
                     fPage.setErrorMessage(NLS.bind(Messages.TCFThreadFilterEditor_cannotValidate, error));
                     fPage.setValid(true);
                 }
                 else {
                     scopeExpressionDecoration.show();
                     fPage.setErrorMessage(error);
                     fPage.setValid(false);
                 }
                 scopeExprCombo.getParent().layout();
                 if (fThreadViewer != null) {
                     fThreadViewer.refresh();
                     fFilteredContexts.clear();
                     for (Context ctx : fContexts) {
                     	fCheckHandler.updateParentCheckState(ctx);
                     }
                 }
             }
         }
     }
 
     private class ExpressionSelectButton implements Listener {
 
         private Shell parentShell;
 
         public ExpressionSelectButton(Shell shell) {
             parentShell = shell;
         }
 
         public void handleEvent(Event event) {
             String[] attrsList = getAvailableAttributes();
             if (attrsList == null) {
                 fPage.setErrorMessage(NLS.bind(Messages.TCFThreadFilterEditor_cannotRetrieveAttrs, Messages.TCFThreadFilterEditorNoOpenChannel));
             }
             else if (attrsList.length == 0) {
                 fPage.setErrorMessage(Messages.TCFThreadFilterEditor_cannotEditExpr);
             }
             else {
                 String result = null;
                 TCFContextQueryExpressionDialog dlg = new TCFContextQueryExpressionDialog(parentShell, attrsList, scopeExprCombo.getText());
     
                 if (dlg.open() == Window.OK) {
                     result = dlg.getExpression();
                 }
                 if (result != null) {
                     scopeExprCombo.setText(result);
                 }
             }
         }
     }
 
     void setupScopeExpressionCombo() {
         if (scopeExprCombo == null) return;
 
         IDialogSettings settings = getDialogSettings(false);
         String bpContextQuery = getBPFilterExpression();
         String [] expresionList = null;
         if ( settings != null ) {
             expresionList = settings.getArray(Messages.TCFThreadFilterQueryExpressionStore);
             if ( expresionList != null ) {
                 int index;
                 // Find if there is a null entry.
                 for (index = 0; index < expresionList.length; index++) {
                     String member = expresionList[index];
                     if (member == null || member.length() == 0) {
                         break;
                     }
                 }
                 String[] copyList = new String[index];
                 int found = -1;
                 for (int loop = 0; loop < index; loop++) {
                     copyList[loop] = expresionList[loop];
                     if (bpContextQuery != null && copyList[loop].equals(bpContextQuery)) {
                         found = loop;
                     }
                 }
                 if (found != -1) {
                     scopeExprCombo.setItems(copyList);
                     scopeExprCombo.select(found);
                 }
                 else {
                     int pad = 0;
                     if (bpContextQuery != null) {
                         pad = 1;
                     }
                     String[] setList = new String[index+pad];
                     if (bpContextQuery != null) {
                         setList[0] = bpContextQuery;
                     }
                     System.arraycopy(copyList, 0, setList, pad, copyList.length);
                     scopeExprCombo.setItems(setList);
                     if (bpContextQuery != null) {
                         scopeExprCombo.select(0);
                     }
                 }
             }
             else if (bpContextQuery != null) {
                 scopeExprCombo.setItems(new String[]{bpContextQuery});
                 scopeExprCombo.select(0);
             }
         }
         else if (bpContextQuery != null) {
             scopeExprCombo.setItems(new String[]{bpContextQuery});
             scopeExprCombo.select(0);
         }
     }
 
     public class linkSelectAdapter implements SelectionListener {
 
         private Shell parentShell;
 
         public linkSelectAdapter(Shell shell) {
             parentShell = shell;
         }
         public void widgetSelected(SelectionEvent e) {
             PreferencesUtil.createPreferenceDialogOn(parentShell,
                     BreakpointPreferencePage.PLUGIN_ID,
                     new String[] { BreakpointPreferencePage.PLUGIN_ID },
                     null).open();
         }
         public void widgetDefaultSelected(SelectionEvent e) {
         }
     }
 
     private void createThreadViewer(Composite parent) {
         GridData twoColumnLayout = new GridData(SWT.FILL,0, true, false);
         twoColumnLayout.horizontalSpan = 2;
         GridData comboGridData = new GridData(SWT.FILL,0, true, false);
         comboGridData .horizontalIndent = 5;
         FieldDecoration fieldDecoration = FieldDecorationRegistry.getDefault().getFieldDecoration(FieldDecorationRegistry.DEC_ERROR);
 
         Label epressionLabel = new Label(parent, SWT.NONE);
         epressionLabel.setText(Messages.TCFThreadFilterQueryAdvancedLabel);
         epressionLabel.setFont(parent.getFont());
         epressionLabel.setLayoutData(twoColumnLayout);
         scopeExprCombo = new Combo(parent,SWT.DROP_DOWN);
         scopeExprCombo.setLayoutData(comboGridData);
         scopeExprCombo.addModifyListener(new ExpressionModifier());
         scopeExprCombo.addDisposeListener(new DisposeListener() {
             @Override
             public void widgetDisposed(DisposeEvent e) {
                 scopeExprCombo = null;
             }
         });
         scopeExpressionDecoration = new ControlDecoration(scopeExprCombo, SWT.LEFT, parent);
         scopeExpressionDecoration.hide();
         scopeExpressionDecoration.setDescriptionText(Messages.TCFThreadFilterEditorFormatError);
         scopeExpressionDecoration.setImage(fieldDecoration.getImage());
 
         setupScopeExpressionCombo();
         Button selectExpression = new Button(parent, SWT.PUSH);
         selectExpression.setText(Messages.TCFThreadFilterQueryButtonEdit);
         selectExpression.setLayoutData(new GridData(SWT.RIGHT,0, false, false));
         selectExpression.addListener(SWT.Selection, new ExpressionSelectButton(parent.getShell()));
 
         Label contextTreeLabel = new Label(parent, SWT.NONE);
         contextTreeLabel.setText(Messages.TCFThreadFilterQueryTreeViewLabel); //$NON-NLS-1$
         contextTreeLabel.setFont(parent.getFont());
         contextTreeLabel.setLayoutData(twoColumnLayout);
         GridData data = new GridData(GridData.FILL_BOTH);
         data.heightHint = 100;
         fThreadViewer = new CheckboxTreeViewer(parent, SWT.BORDER);
         fThreadViewer.addCheckStateListener(fCheckHandler);
         fThreadViewer.getTree().setLayoutData(data);
         fThreadViewer.getTree().setFont(parent.getFont());
 
         fThreadViewer.setContentProvider(fContentProvider);
         fThreadViewer.setLabelProvider(new ThreadFilterLabelProvider());
         fThreadViewer.setInput(DebugPlugin.getDefault().getLaunchManager());
         setInitialCheckedState();
 
         preferencesLink = new Link(parent, SWT.WRAP);
         preferencesLink.setLayoutData(twoColumnLayout);
         preferencesLink.setText(Messages.TCFThreadFilterEditor_defaultScopePrefsLink);
         preferencesLink.addSelectionListener(new linkSelectAdapter(parent.getShell()));
         parent.layout();
     }
 
     protected ILaunch getAttributeLaunch() {
         IAdaptable dbgContext = DebugUITools.getDebugContext();
         return (ILaunch)dbgContext.getAdapter(ILaunch.class);
     }
 
     protected ILaunch[] getLaunches() {
         Object input = fThreadViewer.getInput();
         if (!(input instanceof ILaunchManager)) {
             return new ILaunch[0];
         }
         List<ILaunch> tcfLaunches = new ArrayList<ILaunch>();
         ILaunch[] launches = ((ILaunchManager) input).getLaunches();
         for (int i = 0; i < launches.length; i++) {
             ILaunch launch = launches[i];
             if (launch instanceof TCFLaunch && !launch.isTerminated()) {
                 tcfLaunches.add(launch);
             }
         }
         return tcfLaunches.toArray(new ILaunch[tcfLaunches.size()]);
     }
 
     /**
      * Returns the root contexts that appear in the tree
      */
     protected Context[] getRootContexts() {
         Object input = fThreadViewer.getInput();
         if (!(input instanceof ILaunchManager)) {
             return new Context[0];
         }
         List<Object> targets = new ArrayList<Object>();
         ILaunch[] launches = ((ILaunchManager) input).getLaunches();
         for (int i = 0; i < launches.length; i++) {
             ILaunch launch = launches[i];
             if (launch instanceof TCFLaunch && !launch.isTerminated()) {
                 Context[] targetArray = syncGetContainers((TCFLaunch) launch);
                 targets.addAll(Arrays.asList(targetArray));
             }
         }
         return targets.toArray(new Context[targets.size()]);
     }
 
     protected final CheckboxTreeViewer getThreadViewer() {
         return fThreadViewer;
     }
 
     protected String getScopeExpression() {
         if (scopeExprCombo != null) {
             return  scopeExprCombo.getText();
         }
         return null;
     }
 
     /**
      * Sets the initial checked state of the tree viewer. The initial state
      * should reflect the current state of the breakpoint. If the breakpoint has
      * a thread filter in a given thread, that thread should be checked.
      */
     protected void setInitialCheckedState() {
         TCFBreakpointScopeExtension filterExtension = fPage.getFilterExtension();
         if (filterExtension == null) return;
         String[] ctxIds = filterExtension.getThreadFilters();
 
         // expand all to realize tree items
         getThreadViewer().expandAll();
 
         if (ctxIds == null) {
             ILaunch[] launches = getLaunches();
             for (ILaunch launch : launches) {
                 fCheckHandler.checkLaunch(launch, true);
             }
         }
         else if (ctxIds.length != 0) {
             for (int i = 0; i < ctxIds.length; i++) {
                 String id = ctxIds[i];
                 Context ctx = getContext(id);
                 if (ctx != null) {
                     fCheckHandler.checkContext(ctx, true);
                     fCheckHandler.updateParentCheckState(ctx);
                 }
                 else if (id.indexOf('/') < 0) {
                     for (Context context : fContexts) {
                         if (id.equals(context.fId)) {
                             fCheckHandler.checkContext(context, true);
                             fCheckHandler.updateParentCheckState(context);
                         }
                     }
                 }
             }
         }
         // expand checked items only
         getThreadViewer().setExpandedElements(getThreadViewer().getCheckedElements());
     }
 
     private Context getContainer(Context child) {
         String parentId = child.fSessionId != null ? child.fSessionId + '/' + child.fParentId : child.fParentId;
         return getContext(parentId);
     }
 
     private Context getContext(String id) {
         for (Context ctx : fContexts) {
             if (ctx.fScopeId.equals(id))
                 return ctx;
         }
         return null;
     }
 
     void updateExpressionsDialogSettings(IDialogSettings settings, String scopedExpression) {
         String[] list = settings.getArray(Messages.TCFThreadFilterQueryExpressionStore);
         if (list == null) {
             list = new String[20];
         }
         for (int i = 0; i < list.length; i++) {
             String member = list[i];
             if (member != null && member.equals(scopedExpression)) {
                 return;
             }
             else if (member == null) {
                 list[i] = scopedExpression;
                 settings.put(Messages.TCFThreadFilterQueryExpressionStore, list);
                 return;
             }
         }
         String[] copyList = new String[20];
         copyList[0] = scopedExpression;
         System.arraycopy(list, 0, copyList, 1, list.length-1);
         settings.put(Messages.TCFThreadFilterQueryExpressionStore, copyList);
     }
 
     protected void doStore() {
         IDialogSettings settings = getDialogSettings(true);
         String scopedExpression = getScopeExpression();
         if (scopedExpression.length() != 0)
             updateExpressionsDialogSettings(settings, scopedExpression);
         TCFBreakpointScopeExtension filterExtension = fPage.getFilterExtension();
         if (filterExtension == null) return;
         filterExtension.setPropertiesFilter(scopedExpression);
 
         CheckboxTreeViewer viewer = getThreadViewer();
         Object[] elements = viewer.getCheckedElements();
         String[] threadIds = null;
         List<String> checkedIds = new ArrayList<String>();
         for (int i = 0; i < elements.length; ++i) {
             if (elements[i] instanceof Context) {
                 Context ctx = (Context) elements[i];
                 if (!viewer.getGrayed(ctx)) {
                     checkedIds.add(ctx.fScopeId);
                 }
             }
         }
         if (checkedIds.size() != fFilteredContexts.size()) {
             threadIds = checkedIds.toArray(new String[checkedIds.size()]);
         }
         filterExtension.setThreadFilter(threadIds);
     }
 
     private Context[] syncGetContainers(final TCFLaunch launch) {
         Context[] result = fContainersPerLaunch.get(launch);
         if (result != null) return result;
         final String launchCfgName = launch.getLaunchConfiguration().getName();
         result = new TCFTask<Context[]>(launch.getChannel()) {
             public void run() {
                 List<Context> containers = new ArrayList<Context>();
                 TCFChildren children = TCFModelManager.getModelManager().getRootNode(launch).getChildren();
                 if (!children.validate(this)) return;
                 for (TCFNode node : children.toArray()) {
                     TCFNodeExecContext exeCtx = (TCFNodeExecContext) node;
                     TCFDataCache<IRunControl.RunControlContext> runCtxCache = exeCtx.getRunContext();
                     if (!runCtxCache.validate(this)) return;
                     IRunControl.RunControlContext runCtx = runCtxCache.getData();
                     containers.add(new Context(runCtx, launchCfgName));
                 }
                 done(containers.toArray(new Context[containers.size()]));
             }
         }.getE();
         fContainersPerLaunch.put(launch, result);
         fContexts.addAll(Arrays.asList(result));
         return result;
     }
 
     private Context[] syncGetThreads(final Context container) {
         Context[] result = fContextsPerContainer.get(container);
         if (result != null) return result;
         final TCFLaunch launch = getLaunch(container);
         result = new TCFTask<Context[]>(launch.getChannel()) {
             public void run() {
                 List<Context> contexts = new ArrayList<Context>();
                 TCFModel model = TCFModelManager.getModelManager().getModel(launch);
                 TCFChildren children = ((TCFNodeExecContext) model.getNode(container.fId)).getChildren();
                 if (!children.validate(this)) return;
                 for (TCFNode node : children.toArray()) {
                     TCFNodeExecContext exeCtx = (TCFNodeExecContext) node;
                     TCFDataCache<IRunControl.RunControlContext> runCtxCache = exeCtx.getRunContext();
                     if (!runCtxCache.validate(this)) return;
                     IRunControl.RunControlContext runCtx = runCtxCache.getData();
                     contexts.add(new Context(runCtx, container));
                 }
                 done(contexts.toArray(new Context[contexts.size()]));
             }
         }.getE();
         fContextsPerContainer.put(container, result);
         fContexts.addAll(Arrays.asList(result));
         return result;
     }
 
     private TCFLaunch getLaunch(Context container) {
         Context parent = getContainer(container);
         while (parent != null) {
             container = parent;
             parent = getContainer(container);
         }
         for (TCFLaunch launch : fContainersPerLaunch.keySet()) {
             Context[] containers = fContainersPerLaunch.get(launch);
             for (Context context : containers) {
                 if (context.fScopeId.equals(container.fScopeId)) {
                     return launch;
                 }
             }
         }
         return null;
     }
 }
