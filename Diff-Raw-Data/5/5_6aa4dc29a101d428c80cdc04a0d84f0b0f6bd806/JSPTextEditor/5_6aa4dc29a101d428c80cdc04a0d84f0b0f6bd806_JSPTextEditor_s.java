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
 package org.jboss.tools.jst.jsp.jspeditor;
 
 import java.lang.reflect.Method;
 import java.util.Arrays;
 import java.util.Properties;
 
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.jface.text.IDocument;
 import org.eclipse.jface.text.IRegion;
 import org.eclipse.jface.text.ITextListener;
 import org.eclipse.jface.text.ITextSelection;
 import org.eclipse.jface.text.ITextViewer;
 import org.eclipse.jface.text.TextEvent;
 import org.eclipse.jface.text.formatter.IContentFormatter;
 import org.eclipse.jface.text.source.IAnnotationModel;
 import org.eclipse.jface.text.source.IOverviewRuler;
 import org.eclipse.jface.text.source.ISourceViewer;
 import org.eclipse.jface.text.source.IVerticalRuler;
 import org.eclipse.jface.text.source.SourceViewerConfiguration;
 import org.eclipse.jface.viewers.DoubleClickEvent;
 import org.eclipse.jface.viewers.IDoubleClickListener;
 import org.eclipse.jface.viewers.ISelection;
 import org.eclipse.jface.viewers.ISelectionChangedListener;
 import org.eclipse.jface.viewers.IStructuredSelection;
 import org.eclipse.jface.viewers.SelectionChangedEvent;
 import org.eclipse.jst.jsp.ui.StructuredTextViewerConfigurationJSP;
 import org.eclipse.jst.jsp.ui.internal.JSPUIPlugin;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.custom.StyledText;
 import org.eclipse.swt.dnd.DND;
 import org.eclipse.swt.dnd.DragSource;
 import org.eclipse.swt.dnd.DropTarget;
 import org.eclipse.swt.dnd.DropTargetAdapter;
 import org.eclipse.swt.dnd.DropTargetEvent;
 import org.eclipse.swt.dnd.DropTargetListener;
 import org.eclipse.swt.dnd.FileTransfer;
 import org.eclipse.swt.dnd.HTMLTransfer;
 import org.eclipse.swt.dnd.TextTransfer;
 import org.eclipse.swt.dnd.Transfer;
 import org.eclipse.swt.events.FocusAdapter;
 import org.eclipse.swt.events.FocusEvent;
 import org.eclipse.swt.graphics.Point;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Display;
 import org.eclipse.swt.widgets.Label;
 import org.eclipse.ui.IEditorInput;
 import org.eclipse.ui.IEditorPart;
 import org.eclipse.ui.editors.text.ILocationProvider;
 import org.eclipse.ui.texteditor.ITextEditor;
 import org.eclipse.ui.texteditor.ITextEditorActionConstants;
 import org.eclipse.ui.views.contentoutline.IContentOutlinePage;
 import org.eclipse.ui.views.properties.IPropertySheetPage;
 import org.eclipse.ui.views.properties.PropertySheetPage;
 import org.eclipse.ui.views.properties.PropertySheetSorter;
 import org.eclipse.wst.html.ui.StructuredTextViewerConfigurationHTML;
 import org.eclipse.wst.sse.core.internal.provisional.IStructuredModel;
 import org.eclipse.wst.sse.core.internal.provisional.IndexedRegion;
 import org.eclipse.wst.sse.core.internal.provisional.text.IStructuredDocument;
 import org.eclipse.wst.sse.core.internal.provisional.text.ITextRegion;
 import org.eclipse.wst.sse.ui.StructuredTextEditor;
 import org.eclipse.wst.sse.ui.StructuredTextViewerConfiguration;
 import org.eclipse.wst.sse.ui.internal.IModelProvider;
 import org.eclipse.wst.sse.ui.internal.StructuredTextViewer;
 import org.eclipse.wst.sse.ui.internal.actions.StructuredTextEditorActionConstants;
 import org.eclipse.wst.sse.ui.internal.contentassist.ContentAssistUtils;
 import org.eclipse.wst.sse.ui.internal.contentoutline.ConfigurableContentOutlinePage;
 import org.eclipse.wst.sse.ui.internal.properties.ConfigurablePropertySheetPage;
 import org.eclipse.wst.sse.ui.internal.provisional.extensions.ConfigurationPointCalculator;
 import org.eclipse.wst.sse.ui.views.contentoutline.ContentOutlineConfiguration;
 import org.eclipse.wst.xml.core.internal.document.AttrImpl;
 import org.eclipse.wst.xml.core.internal.document.ElementImpl;
 import org.jboss.tools.common.core.resources.XModelObjectEditorInput;
 import org.jboss.tools.common.meta.action.XActionInvoker;
 import org.jboss.tools.common.model.XModelBuffer;
 import org.jboss.tools.common.model.XModelObject;
 import org.jboss.tools.common.model.XModelTransferBuffer;
 import org.jboss.tools.common.model.filesystems.impl.FileAnyImpl;
 import org.jboss.tools.common.model.filesystems.impl.FolderImpl;
 import org.jboss.tools.common.model.ui.dnd.ModelTransfer;
 import org.jboss.tools.common.model.ui.editor.IModelObjectEditorInput;
 import org.jboss.tools.common.model.ui.editors.dnd.DropCommandFactory;
 import org.jboss.tools.common.model.ui.editors.dnd.DropData;
 import org.jboss.tools.common.model.ui.editors.dnd.IDropCommand;
 import org.jboss.tools.common.model.ui.editors.dnd.JSPTagProposalFactory;
 import org.jboss.tools.common.model.ui.editors.dnd.context.DropContext;
 import org.jboss.tools.common.model.ui.editors.dnd.context.InnerDragBuffer;
 import org.jboss.tools.common.model.ui.texteditors.TextMerge;
 import org.jboss.tools.common.model.ui.texteditors.dnd.TextEditorDrop;
 import org.jboss.tools.common.model.ui.texteditors.dnd.TextEditorDropProvider;
 import org.jboss.tools.common.model.ui.views.palette.IIgnoreSelection;
 import org.jboss.tools.common.model.ui.views.palette.PaletteInsertHelper;
 import org.jboss.tools.common.model.util.XModelObjectLoaderUtil;
 import org.jboss.tools.common.text.xml.IOccurrencePreferenceProvider;
 import org.jboss.tools.common.text.xml.XmlEditorPlugin;
 import org.jboss.tools.common.text.xml.ui.FreeCaretStyledText;
 import org.jboss.tools.jst.jsp.HTMLTextViewerConfiguration;
 import org.jboss.tools.jst.jsp.JSPTextViewerConfiguration;
 import org.jboss.tools.jst.jsp.JspEditorPlugin;
 import org.jboss.tools.jst.jsp.editor.IJSPTextEditor;
 import org.jboss.tools.jst.jsp.editor.ITextFormatter;
 import org.jboss.tools.jst.jsp.editor.IVisualContext;
 import org.jboss.tools.jst.jsp.editor.IVisualController;
 import org.jboss.tools.jst.jsp.outline.JSPContentOutlineConfiguration;
 import org.jboss.tools.jst.jsp.outline.JSPPropertySheetConfiguration;
 import org.jboss.tools.jst.jsp.preferences.VpePreference;
 import org.jboss.tools.jst.jsp.support.kb.WTPTextJspKbConnector;
 import org.jboss.tools.jst.jsp.text.xpl.IStructuredTextOccurrenceStructureProvider;
 import org.jboss.tools.jst.jsp.text.xpl.StructuredTextOccurrenceStructureProviderRegistry;
 import org.jboss.tools.jst.jsp.ui.action.ExtendedFormatAction;
 import org.jboss.tools.jst.jsp.ui.action.IExtendedAction;
 import org.jboss.tools.jst.web.tld.VpeTaglibManager;
 import org.jboss.tools.jst.web.tld.VpeTaglibManagerProvider;
 import org.w3c.dom.DocumentType;
 import org.w3c.dom.NamedNodeMap;
 import org.w3c.dom.Node;
 import org.w3c.dom.Text;
 
 /**
  * @author Jeremy
  *
  */
 public class JSPTextEditor extends StructuredTextEditor implements
 		ITextListener, IJSPTextEditor, ITextFormatter,
 		IOccurrencePreferenceProvider {
 	private IStructuredTextOccurrenceStructureProvider fOccurrenceModelUpdater;
 
 	TextEditorDrop dnd = new TextEditorDrop();
 
 	JSPMultiPageEditor parentEditor;
 
 	long timeStamp = -1;
 
 	long savedTimeStamp = -1;
 
 	IVisualController vpeController;
 	//Added By Max Areshkau
 	//Fix for JBIDE-788
 	protected SourceEditorPageContext pageContext = null;
 
 	public JSPTextEditor(JSPMultiPageEditor parentEditor) {
 		JspEditorPlugin.getDefault().initDefaultPluginPreferences();
 		dnd.setTextEditorDropProvider(new TextEditorDropProviderImpl());
 		this.parentEditor = parentEditor;
 		super
 				.setSourceViewerConfiguration(new JSPTextViewerConfiguration());
 	}
 	
 	protected void setSourceViewerConfiguration(SourceViewerConfiguration config) {
 		if (config instanceof StructuredTextViewerConfigurationJSP) {
 			if (!(config instanceof JSPTextViewerConfiguration)) {
 				config = new JSPTextViewerConfiguration();
 			}
 		} else if (config instanceof StructuredTextViewerConfigurationHTML) {
 			if (!(config instanceof HTMLTextViewerConfiguration)) {
 				config = new HTMLTextViewerConfiguration();
 			}
 		} else {
 			config = new JSPTextViewerConfiguration();
 		}
 		super.setSourceViewerConfiguration(config);
 	}
 
 	/** This is *only* for allowing unit tests to access the source configuration. */
 	public SourceViewerConfiguration getSourceViewerConfigurationForTest () {
 		return getSourceViewerConfiguration();
 	}
 	//Added By Max Areshkau
 	//Fix for JBIDE-788
     public IVisualContext getPageContext() {
     	
 		if (pageContext==null) {
 			pageContext = new SourceEditorPageContext();
 		}
 		IDocument document = getTextViewer().getDocument();
 		int offset = this.getTextViewer().getTextWidget().getCaretOffset();
     	IndexedRegion treeNode = ContentAssistUtils.getNodeAt(this.getTextViewer(), offset);
     	Node node = (Node) treeNode;
 		pageContext.setReferenceNode(node);
     	pageContext.setDocument(document);
 		
 		return pageContext;
 	}
 	
 	protected void initializeDrop(ITextViewer textViewer) {
 
 		Composite c = textViewer.getTextWidget();
 		Label l = new Label(c, SWT.NONE);
 		l.dispose();
 	}
 
 	private ConfigurableContentOutlinePage fOutlinePage = null;
 
 	private OutlinePageListener fOutlinePageListener = null;
 
 	private IPropertySheetPage fPropertySheetPage;
 
 	public Object getAdapter(Class adapter) {
 		if (ISourceViewer.class.equals(adapter)) {
 			return JSPTextEditor.this.getSourceViewer();
 		} else if (IContentOutlinePage.class.equals(adapter)) {
 			if (fOutlinePage == null || fOutlinePage.getControl() == null
 					|| fOutlinePage.getControl().isDisposed()) {
 				IStructuredModel internalModel = getModel();
 				ContentOutlineConfiguration cfg = new JSPContentOutlineConfiguration(
 						this);
 				if (cfg != null) {
 					ConfigurableContentOutlinePage outlinePage = new ConfigurableContentOutlinePage();
 					outlinePage.setConfiguration(cfg);
 					if (internalModel != null) {
 						outlinePage.setInputContentTypeIdentifier(internalModel
 								.getContentTypeIdentifier());
 						outlinePage.setInput(internalModel);
 					}
 
 					if (fOutlinePageListener == null) {
 						fOutlinePageListener = new OutlinePageListener();
 					}
 
 					outlinePage
 							.addSelectionChangedListener(fOutlinePageListener);
 					outlinePage.addDoubleClickListener(fOutlinePageListener);
 
 					fOutlinePage = outlinePage;
 				}
 			}
 			return fOutlinePage;
 		} else if (IPropertySheetPage.class == adapter) {
 			if (fPropertySheetPage == null
 					|| fPropertySheetPage.getControl() == null
 					|| fPropertySheetPage.getControl().isDisposed()) {
 				JSPPropertySheetConfiguration cfg = new JSPPropertySheetConfiguration();
 				if (cfg != null) {
 					ConfigurablePropertySheetPage propertySheetPage = new ConfigurablePropertySheetPage();
 					propertySheetPage.setConfiguration(cfg);
 					fPropertySheetPage = propertySheetPage;
 					setSorter(cfg.getSorter(), propertySheetPage);
 				}
 			}
 			return fPropertySheetPage;
 		}
 		return super.getAdapter(adapter);
 	}
 
 	private void setSorter(PropertySheetSorter sorter,
 			ConfigurablePropertySheetPage sheet) {
 		try {
 			Method method = PropertySheetPage.class.getDeclaredMethod(
 					"setSorter", new Class[] { PropertySheetSorter.class });
 			method.setAccessible(true);
 			method.invoke(sheet, new Object[] { sorter });
 		} catch (Exception e) {
 			JspEditorPlugin.getPluginLog().logError(e);
 		}
 	}
 
 	public String getEditorId() {
 		return JSPUIPlugin.ID;
 	}
 
 	public IStructuredTextOccurrenceStructureProvider getOccurrencePreferenceProvider() {
 		return fOccurrenceModelUpdater;
 	}
 
 	public void createPartControl(Composite parent) {
 		super.createPartControl(parent);
 
 		StructuredTextOccurrenceStructureProviderRegistry registry = XmlEditorPlugin
 				.getDefault().getOccurrenceStructureProviderRegistry(
 						JspEditorPlugin.PLUGIN_ID);
 		fOccurrenceModelUpdater = registry
 				.getCurrentOccurrenceProvider(JspEditorPlugin.PLUGIN_ID);
 
 		if (fOccurrenceModelUpdater != null)
 			fOccurrenceModelUpdater.install(this, getTextViewer());
 
 		installActivePropmtSupport();
 
 		createDrop();
 		setModified(false);
 		getSourceViewer().removeTextListener(this);
 		getSourceViewer().addTextListener(this);
 
 		Object dtid = getSourceViewer().getTextWidget().getData("DropTarget");
 		if (dtid != null) {
 			if (dtid instanceof DropTarget) {
 				DropTarget dropTarget = (DropTarget) dtid;
 				dropTarget.addDropListener(new DropTargetAdapter() {
 					private FreeCaretStyledText getFreeCaretControl(
 							Object sourceOrTarget) {
 							if (sourceOrTarget == null)
 								return null;
 
 							Object control = null;
 
 							if (sourceOrTarget instanceof DropTarget) {
 								control = ((DropTarget) sourceOrTarget)
 										.getControl();
 							} else if (sourceOrTarget instanceof DragSource) {
 								control = ((DragSource) sourceOrTarget)
 										.getControl();
 							} else
 								return null;
 
 							if (control instanceof FreeCaretStyledText)
 								return (FreeCaretStyledText) control;
 						return null;
 					}
 
 					private int getPosition(StyledText t, int x, int y) {
 						try {
 							Point pp = t.toControl(x, y);
 							x = pp.x;
 							y = pp.y;
 							int lineIndex = (t.getTopPixel() + y)
 									/ t.getLineHeight();
 							if (lineIndex >= t.getLineCount()) {
 								return t.getCharCount();
 							} else {
 								int c = 0;
 								try {
 									c = t.getOffsetAtLocation(new Point(x, y));
 									if (c < 0)
 										c = 0;
 								} catch (Exception ex) {
 									c = t.getOffsetAtLine(lineIndex + 1)
 											- (t.getLineDelimiter() == null ? 0
 													: t.getLineDelimiter()
 															.length());
 								}
 								return c;
 							}
 						} catch (Exception e) {
 							return 0;
 						}
 					}
 
 					public void dragEnter(DropTargetEvent event) {
 						getFreeCaretControl(event.widget).enableFreeCaret(true);
 					}
 
 					public void dragLeave(DropTargetEvent event) {
 						getFreeCaretControl(event.widget).enableFreeCaret(false);
 					}
 
 					public void dragOperationChanged(DropTargetEvent event) {
 						getFreeCaretControl(event.widget).enableFreeCaret(false);
 					}
 
 					public void dragOver(DropTargetEvent event) {
 						FreeCaretStyledText fcst = getFreeCaretControl(event.widget);
 						int pos = getPosition(fcst, event.x, event.y);
 						Point p = fcst.getLocationAtOffset(pos);
 						fcst.myRedraw(p.x, p.y);
 					}
 
 					public void drop(DropTargetEvent event) {
 						getFreeCaretControl(event.widget).enableFreeCaret(false);
 					}
 				});
 			}
 		}
 	}
 
 	protected ISourceViewer createSourceViewer(Composite parent,
 			IVerticalRuler ruler, int styles) {
 		ISourceViewer sv = super.createSourceViewer(parent, ruler, styles);
 		sv.getTextWidget().addFocusListener(new TextFocusListener());
 		return sv;
 	}
 
 	protected StructuredTextViewer createStructedTextViewer(Composite parent,
 			IVerticalRuler verticalRuler, int styles) {
 		return new JSPStructuredTextViewer(parent, verticalRuler,
 				getOverviewRuler(), isOverviewRulerVisible(), styles,
 				parentEditor, this);
 	}
 
 	class TextFocusListener extends FocusAdapter {
 		public void focusLost(FocusEvent e) {
 			if (JSPTextEditor.super.isDirty()) {
 				Display.getDefault().syncExec(new Runnable() {
 					public void run() {
 						try {
 							Thread.sleep(200);
 						} catch (InterruptedException exc) {
 							JspEditorPlugin.getPluginLog().logError(exc);
 						}
 						save();
 					}
 				});
 			}
 		}
 	}
 
 	public void save() {
 		if (!lock && isModified()) {
 			lock = true;
 			try {
 				FileAnyImpl f = (FileAnyImpl) getModelObject();
 				if (f != null)
 					f.edit(getSourceViewer().getDocument().get());
 			} catch (Exception e) {
 				JspEditorPlugin.getPluginLog().logError(e);
 			} finally {
 				setModified(false);
 				lock = false;
 			}
 		}
 	}
 
 	boolean modified = false;
 
 	public void setModified(boolean set) {
 		if (this.modified != set) {
 			this.modified = set;
 			if (set) {
 				XModelObject o = getModelObject();
 				if (o != null)
 					o.setModified(true);
 			}
 			super.firePropertyChange(IEditorPart.PROP_DIRTY);
 		}
 	}
 	
 	public void updateModification() {
 		//added by Max Areshkau
 		//Fix for JBIDE-788
 		getPageContext().refreshBundleValues();
 		
 		XModelObject object = getModelObject();
 		if (object != null && !object.isModified() && isModified()) {
 			setModified(false);
 		} else {
 			firePropertyChange(ITextEditor.PROP_DIRTY);
 		}
 	}
 
 	public boolean isModified() {
 		return modified;
 	}
 
 	protected void doSetInput(IEditorInput input) throws CoreException {
 		super.doSetInput(XModelObjectEditorInput.checkInput(input));
 		if (getSourceViewer() != null
 				&& getSourceViewer().getDocument() != null) {
 			getSourceViewer().removeTextListener(this);
 			getSourceViewer().addTextListener(this);
 		}
 		if (listener != null)
 			listener.dispose();
 		listener = null;
 		XModelObject o = getModelObject();
 		if (o instanceof FileAnyImpl) {
 			listener = new BodyListenerImpl((FileAnyImpl) o);
 		}
 	}
 
 	boolean lock = false;
 
 	public boolean isDirty() {
 		if (getEditorInput() instanceof IModelObjectEditorInput) {
 			XModelObject o = getModelObject();
 			if (o != null && o.isModified())
 				return true;
 			else {
 				return isModified();
 			}
 		} else {
 			return super.isDirty();
 		}
 	}
 
 	public void doSave(IProgressMonitor monitor) {
 		XModelObject o = getModelObject();
 		super.doSave(monitor);
 		if (o != null && (monitor == null || !monitor.isCanceled())) {
 			if (o != null)
 				save();
 			if (getEditorInput() instanceof ILocationProvider) {
 				XModelObject p = o.getParent();
 				if (p instanceof FolderImpl) {
 					((FolderImpl) p).saveChild(o);
 				}
 			} else {
 				o.setModified(false);
 				XModelObjectLoaderUtil.updateModifiedOnSave(o);
 			}
 			super.firePropertyChange(IEditorPart.PROP_DIRTY);
 		}
 	}
 
 	public void firePropertyChangeDirty() {
 		super.firePropertyChange(IEditorPart.PROP_DIRTY);
 	}
 
 	public XModelObject getModelObject() {
 		if (getEditorInput() instanceof IModelObjectEditorInput) {
 			return ((IModelObjectEditorInput) getEditorInput())
 					.getXModelObject();
 		}
 		return null;
 	}
 
 	class TextEditorDropProviderImpl implements TextEditorDropProvider {
 
 		public ISourceViewer getSourceViewer() {
 			return JSPTextEditor.this.getSourceViewer();
 		}
 
 		public XModelObject getModelObject() {
 			return JSPTextEditor.this.getModelObject();
 		}
 
 		public void insert(Properties p) {
 			PaletteInsertHelper.insertIntoEditor(getSourceViewer(), p);
 		}
 
 	}
 
 	public void textChanged(TextEvent event) {
 		if (event.getDocumentEvent() != null) {
 			setModified(true);
 		}
 	}
 
 	public void doRevertToSaved() {
 		save();
 		XModelObject o = getModelObject();
 		if (o == null) {
 			super.doRevertToSaved();
 			return;
 		}
 		Properties p = new Properties();
 		XActionInvoker.invoke("DiscardActions.Discard", o, p);
 		if (!"true".equals(p.getProperty("done")))
 			return;
 		super.doRevertToSaved();
 		if (o.isModified())
 			o.setModified(false);
 		modified = false;
 		firePropertyChange(IEditorPart.PROP_DIRTY);
 		updatePartControl(getEditorInput());
 	}
 
 	public IAnnotationModel getAnnotationModel() {
 		return getSourceViewer().getAnnotationModel();
 	}
 
 	private WTPTextJspKbConnector wtpTextJspKbConnector;
 
 	private void installActivePropmtSupport() {
 		try {
 			IDocument document = getTextViewer().getDocument();
 			IStructuredModel model = null;
 			if (getDocumentProvider() instanceof IModelProvider) {
 				model = ((IModelProvider) getDocumentProvider())
 						.getModel(getEditorInput());
 			} else {
 				if (document instanceof IStructuredDocument) {
 					model = getModel();
 				}
 			}
 			if (wtpTextJspKbConnector == null
 					&& model != null
 					&& (getContentType().toLowerCase().indexOf("jsp") != -1 || getContentType()
 							.toLowerCase().indexOf("html") != -1)) {
 				wtpTextJspKbConnector = new WTPTextJspKbConnector(
 						getEditorInput(), document, model);
 				wtpTextJspKbConnector.setTaglibManagerProvider(parentEditor);
 			}
 		} catch (Exception x) {
 			JspEditorPlugin.getPluginLog().logError("Error while installing active prpmpting support", x);
 		}
 	}
 
 	private String getContentType() {
 		String type = null;
 		try {
 			type = getModel().getContentTypeIdentifier();
 		} finally {
 			if (type == null)
 				type = "";
 		}
 		return type;
 	}
 
 	public WTPTextJspKbConnector getWTPTextJspKbConnector() {
 		return wtpTextJspKbConnector;
 	}
 
 	public static class JSPStructuredTextViewer extends StructuredTextViewer
 			implements VpeTaglibManagerProvider, IIgnoreSelection {
 
 		boolean insertFromPallete = false;
 
 		private VpeTaglibManagerProvider provider;
 
 		private JSPTextEditor editor;
 
 		private boolean ignore = false;
 
 		public JSPStructuredTextViewer(Composite parent,
 				IVerticalRuler verticalRuler, int styles) {
 			super(parent, verticalRuler, null, false, styles);
 		}
 
 		public JSPStructuredTextViewer(Composite parent,
 				IVerticalRuler verticalRuler, IOverviewRuler overviewRuler,
 				boolean showAnnotationsOverview, int styles,
 				VpeTaglibManagerProvider provider, JSPTextEditor editor) {
 			super(parent, verticalRuler, overviewRuler,
 					showAnnotationsOverview, styles);
 			this.provider = provider;
 			this.editor = editor;
 		}
 
 		protected StyledText createTextWidget(Composite parent, int styles) {
 			return new FreeCaretStyledText(parent, styles);
 		}
 
 		public VpeTaglibManager getTaglibManager() {
 			//added by Max Areshkau
 			//Fix for JBIDE-788
 			if (getEditor() != null) {
 				if(getEditor().getPageContext() instanceof VpeTaglibManager)
 				
 					
 				return (VpeTaglibManager)getEditor().getPageContext();
 			}
 			return null;
 		}
 
 		public boolean doesIgnore() {
 			return ignore;
 		}
 
 		public void setIgnore(boolean ignore) {
 			this.ignore = ignore;
 		}
 
 		public void doOperation(int operation) {
 			if (operation == UNDO || operation == REDO
 					|| operation == FORMAT_DOCUMENT
 					|| operation == FORMAT_ACTIVE_ELEMENTS) {
 				if (editor.getVPEController() != null) {
 					editor.getVPEController().preLongOperation();
 				}
 			}
 
 			super.doOperation(operation);
 
 			if (operation == UNDO || operation == REDO
 					|| operation == FORMAT_DOCUMENT
 					|| operation == FORMAT_ACTIVE_ELEMENTS) {
 				if (editor.getVPEController() != null) {
 					editor.getVPEController().postLongOperation();
 				}
 			}
 		}
 				
 		protected void handleDispose() {
 			if (editor != null && editor.getSourceViewer() != null && editor.getSourceViewer().getTextWidget() != null && editor.getVPEController() != null) {
 				StyledText widget = editor.getSourceViewer().getTextWidget();
 				widget.removeSelectionListener(editor.getVPEController());
 			}
 			super.handleDispose();
 		}
 	
 
 		/**
 		 * @return the editor
 		 */
 		//Added By Max Areshkau
 		//Fix for JBIDE-788
 		public JSPTextEditor getEditor() {
 			return editor;
 		}
 
 		/**
 		 * @param editor the editor to set
 		 */
 		//Added By Max Areshkau
 		//Fix for JBIDE-788
 		public void setEditor(JSPTextEditor editor) {
 			this.editor = editor;
 		}
 	
 	}
 	public JSPMultiPageEditor getParentEditor() {
 		return parentEditor;
 	}
 
 	public void setVPEController(IVisualController c) {
 		vpeController = c;
 	}
 
 	public IVisualController getVPEController() {
 		return vpeController;
 	}
 
 	public void runDropCommand(final String flavor, final String data) {
 		XModelBuffer b = XModelTransferBuffer.getInstance().getBuffer();
 		final XModelObject o = b == null ? null : b.source();
 		Display.getDefault().asyncExec(new Runnable() {
 			public void run() {
				if (parentEditor.getVisualEditor().getController() != null) {
 					if(o != null && !XModelTransferBuffer.getInstance().isEnabled()) {
 						XModelTransferBuffer.getInstance().enable();
 						XModelTransferBuffer.getInstance().getBuffer().addSource(o);
 					}
 					try {
 						DropData dropData = new DropData(flavor,
 							data,
 							getEditorInput(), getSourceViewer(),
 							getSelectionProvider());
 						dropData.setAttributeName(dropContext.getAttributeName());
 						IDropCommand dropCommand = DropCommandFactory.getInstance().getDropCommand(flavor, JSPTagProposalFactory.getInstance());
 
 						boolean promptAttributes = "yes".equals(VpePreference.ALWAYS_REQUEST_FOR_ATTRIBUTE.getValue());
 						dropCommand.getDefaultModel().setPromptForTagAttributesRequired(promptAttributes);
 						dropCommand.execute(dropData);
 					} finally {
 						XModelTransferBuffer.getInstance().disable();
 					}
 				}
			}
 		});
 	}
 
 	private void createDrop() {
 		DropTarget target = new DropTarget(getSourceViewer().getTextWidget(),
 				DND.DROP_MOVE | DND.DROP_COPY);
 		Transfer[] types = new Transfer[] { ModelTransfer.getInstance(),
 				HTMLTransfer.getInstance(), TextTransfer.getInstance(),
 				FileTransfer.getInstance() };
 		target.setTransfer(types);
 		target.addDropListener(new DTL());
 	}
 
 	DropContext dropContext = new DropContext();
 
 	class DTL implements DropTargetListener {
 		int lastpos = -1;
 
 		int lastdetail = -1;
 
 		public void dragEnter(DropTargetEvent event) {
 			lastpos = -1;
 		}
 
 		public void dragLeave(DropTargetEvent event) {
 			lastpos = -1;
 		}
 
 		public void dragOperationChanged(DropTargetEvent event) {
 		}
 
 		public void dragOver(DropTargetEvent event) {
 			if (!isEditable()
 					|| (getModelObject() != null && !getModelObject()
 							.isObjectEditable())) {
 				event.detail = DND.DROP_NONE;
 				return;
 			}
 			dropContext.setDropTargetEvent(event);
 			if (dropContext.getFlavor() == null) {
 				event.detail = DND.DROP_NONE;
 				return;
 			}
 			// Drop from VPE to Source is forbidden
 			if (dropContext.getFlavor().equals("text/html")) {
 				if (InnerDragBuffer.object != null) {
 					event.detail = DND.DROP_NONE;
 				}
 				return;
 			}
 			int pos = getPosition(event.x, event.y);
 			if (lastpos == pos && pos >= 0) {
 				pos = lastpos;
 				event.detail = lastdetail;
 				return;
 			}
 			lastpos = pos;
 			dropContext.clean();
 			getSourceViewer().getDocument();
 			IndexedRegion region = getModel().getIndexedRegion(pos);
 			if (region instanceof ElementImpl) {
 				ElementImpl jspElement = (ElementImpl) region;
 				NamedNodeMap attributes = jspElement.getAttributes();
 				if (pos == jspElement.getStartOffset()
 						|| pos == jspElement.getEndStartOffset()) {
 					event.detail = lastdetail = DND.DROP_MOVE;
 					return;
 				}
 				for (int i = 0; i < attributes.getLength(); i++) {
 					Node attribute = attributes.item(i);
 					if (attribute instanceof AttrImpl) {
 						AttrImpl jspAttr = (AttrImpl) attribute;
 						ITextRegion valueRegion = jspAttr.getValueRegion();
 						if (valueRegion == null) {
 							event.detail = lastdetail = DND.DROP_NONE;
 							return;
 						}
 						int startPos = jspElement.getStartOffset()
 								+ valueRegion.getStart();
 						int endPos = jspElement.getStartOffset()
 								+ valueRegion.getTextEnd();
 						if (pos > startPos && pos < endPos) {
 							dropContext.setOverAttributeValue(true);
 							dropContext.setAttributeName(jspAttr.getNodeName());
 							event.detail = lastdetail = DND.DROP_MOVE;
 							return;
 						}
 					}
 				}
 				event.detail = lastdetail = DND.DROP_NONE;
 			} else if (region instanceof Text
 					&& isInsideResponseRedirect((Text) region, pos
 							- region.getStartOffset())) {
 				dropContext.setOverAttributeValue(true);
 				event.detail = lastdetail = DND.DROP_MOVE;
 			} else if (region instanceof Text) {
 				event.detail = lastdetail = DND.DROP_MOVE;
 			} else if (region instanceof DocumentType) {
 				event.detail = lastdetail = DND.DROP_NONE;
 			} else if (region == null) {
 				//new place
 				event.detail = lastdetail = DND.DROP_MOVE;
 			}
 		}
 
 		public void drop(DropTargetEvent event) {
 			int offset = getPosition(event.x, event.y);
 			selectAndReveal(offset, 0);
 			dropContext.runDropCommand(JSPTextEditor.this, event);
 		}
 
 		public void dropAccept(DropTargetEvent event) {
 		}
 
 	}
 
 	private int getPosition(int x, int y) {
 		try {
 			ISourceViewer v = getSourceViewer();
 			StyledText t = v.getTextWidget();
 			Point pp = t.toControl(x, y);
 			x = pp.x;
 			y = pp.y;
 			int lineIndex = (t.getTopPixel() + y) / t.getLineHeight();
 			if (lineIndex >= t.getLineCount()) {
 				return t.getCharCount();
 			} else {
 				int c = 0;
 				try {
 					c = t.getOffsetAtLocation(new Point(x, y));
 					if (c < 0)
 						c = 0;
 				} catch (Exception ex) {
 					c = t.getOffsetAtLine(lineIndex + 1)
 							- (t.getLineDelimiter() == null ? 0 : t
 									.getLineDelimiter().length());
 				}
 				return c;
 			}
 		} catch (Exception e) {
 			return 0;
 		}
 	}
 
 	public String[] getConfigurationPoints() {
 		String contentTypeIdentifierID = null;
 		if (getModel() != null)
 			contentTypeIdentifierID = getModel().getContentTypeIdentifier();
 		return ConfigurationPointCalculator.getConfigurationPoints(this,
 				contentTypeIdentifierID, ConfigurationPointCalculator.SOURCE,
 				StructuredTextEditor.class);
 	}
 
 	public void formatTextRegion(IDocument document, IRegion region) {
 		SourceViewerConfiguration conf = getSourceViewerConfiguration();
 
 		if (conf instanceof StructuredTextViewerConfiguration) {
 			StructuredTextViewerConfiguration stvc = (StructuredTextViewerConfiguration) conf;
 			IContentFormatter f = stvc.getContentFormatter(getSourceViewer());
 			f.format(document, region);
 		}
 	}
 
 	Point storedSelection = new Point(0, 0);
 
 	protected void handleCursorPositionChanged() {
 		super.handleCursorPositionChanged();
 		ISelection selection = getSelectionProvider().getSelection();
 		Point p = getTextViewer().getTextWidget().getSelection();
 		if (storedSelection == null || !storedSelection.equals(p)) {
 			storedSelection = p;
 			if (selection instanceof ITextSelection) {
 				ITextSelection ts = (ITextSelection) selection;
 				if (ts.getLength() == 0) {
 					if (vpeController != null) {
 						vpeController
 								.selectionChanged(new SelectionChangedEvent(
 										getSelectionProvider(),
 										getSelectionProvider().getSelection()));
 					}
 				}
 			}
 		}
 	}
 
 	static int firingSelectionFailedCount = 0;
 
 	private class OutlinePageListener implements IDoubleClickListener,
 			ISelectionChangedListener {
 		public void doubleClick(DoubleClickEvent event) {
 			if (event.getSelection().isEmpty())
 				return;
 
 			int start = -1;
 			int length = 0;
 			if (event.getSelection() instanceof IStructuredSelection) {
 				ISelection currentSelection = getSelectionProvider()
 						.getSelection();
 				if (currentSelection instanceof IStructuredSelection) {
 					Object current = ((IStructuredSelection) currentSelection)
 							.toArray();
 					Object newSelection = ((IStructuredSelection) event
 							.getSelection()).toArray();
 					if (!current.equals(newSelection)) {
 						IStructuredSelection selection = (IStructuredSelection) event
 								.getSelection();
 						Object o = selection.getFirstElement();
 						if (o instanceof IndexedRegion) {
 							start = ((IndexedRegion) o).getStartOffset();
 							length = ((IndexedRegion) o).getEndOffset() - start;
 						} else if (o instanceof ITextRegion) {
 							start = ((ITextRegion) o).getStart();
 							length = ((ITextRegion) o).getEnd() - start;
 						} else if (o instanceof IRegion) {
 							start = ((ITextRegion) o).getStart();
 							length = ((ITextRegion) o).getLength();
 						}
 					}
 				}
 			} else if (event.getSelection() instanceof ITextSelection) {
 				start = ((ITextSelection) event.getSelection()).getOffset();
 				length = ((ITextSelection) event.getSelection()).getLength();
 			}
 			if (start > -1) {
 				getSourceViewer().setRangeIndication(start, length, false);
 				selectAndReveal(start, length);
 			}
 		}
 
 		public void selectionChanged(SelectionChangedEvent event) {
 			if (event.getSelection().isEmpty() || isFiringSelection())
 				return;
 
 			boolean ignoreSelection = false;
 			if (getSourceViewer() != null
 					&& getSourceViewer() instanceof IIgnoreSelection) {
 				IIgnoreSelection is = ((IIgnoreSelection) getSourceViewer());
 				ignoreSelection = is.doesIgnore();
 			}
 			if (getSourceViewer() != null
 					&& getSourceViewer().getTextWidget() != null
 					&& !getSourceViewer().getTextWidget().isDisposed()
 					&& !getSourceViewer().getTextWidget().isFocusControl()
 					&& !ignoreSelection) {
 				int start = -1;
 				int length = 0;
 				if (event.getSelection() instanceof IStructuredSelection) {
 					ISelection current = getSelectionProvider().getSelection();
 					if (current instanceof IStructuredSelection) {
 						Object[] currentSelection = ((IStructuredSelection) current)
 								.toArray();
 						Object[] newSelection = ((IStructuredSelection) event
 								.getSelection()).toArray();
 						if (!Arrays.equals(currentSelection, newSelection)) {
 							if (newSelection.length > 0) {
 								/*
 								 * No ordering is guaranteed for multiple
 								 * selection
 								 */
 								Object o = newSelection[0];
 								if (o instanceof IndexedRegion) {
 									start = ((IndexedRegion) o)
 											.getStartOffset();
 									int end = ((IndexedRegion) o)
 											.getEndOffset();
 									if (newSelection.length > 1) {
 										for (int i = 1; i < newSelection.length; i++) {
 											start = Math
 													.min(
 															start,
 															((IndexedRegion) newSelection[i])
 																	.getStartOffset());
 											end = Math
 													.max(
 															end,
 															((IndexedRegion) newSelection[i])
 																	.getEndOffset());
 										}
 										length = end - start;
 									}
 								} else if (o instanceof ITextRegion) {
 									start = ((ITextRegion) o).getStart();
 									int end = ((ITextRegion) o).getEnd();
 									if (newSelection.length > 1) {
 										for (int i = 1; i < newSelection.length; i++) {
 											start = Math
 													.min(
 															start,
 															((ITextRegion) newSelection[i])
 																	.getStart());
 											end = Math
 													.max(
 															end,
 															((ITextRegion) newSelection[i])
 																	.getEnd());
 										}
 										length = end - start;
 									}
 								} else if (o instanceof IRegion) {
 									start = ((IRegion) o).getOffset();
 									int end = start + ((IRegion) o).getLength();
 									if (newSelection.length > 1) {
 										for (int i = 1; i < newSelection.length; i++) {
 											start = Math.min(start,
 													((IRegion) newSelection[i])
 															.getOffset());
 											end = Math
 													.max(
 															end,
 															((IRegion) newSelection[i])
 																	.getOffset()
 																	+ ((IRegion) newSelection[i])
 																			.getLength());
 										}
 										length = end - start;
 									}
 								}
 							}
 						}
 					}
 				} else if (event.getSelection() instanceof ITextSelection) {
 					start = ((ITextSelection) event.getSelection()).getOffset();
 				}
 				if (start > -1) {
 					updateRangeIndication0(event.getSelection());
 					selectAndReveal(start, length);
 				}
 			}
 		}
 
 		Method m = null;
 
 		private boolean isFiringSelection() {
 			if (getSelectionProvider() == null)
 				return false;
 			if (firingSelectionFailedCount > 0)
 				return false;
 			try {
 				if (m == null) {
 					Class c = getSelectionProvider().getClass();
 					m = c.getDeclaredMethod("isFiringSelection", new Class[0]);
 					m.setAccessible(true);
 				}
 				Boolean b = (Boolean) m.invoke(getSelectionProvider(),
 						new Object[0]);
 				return b.booleanValue();
 			} catch (Exception e) {
 				firingSelectionFailedCount++;
 				JspEditorPlugin.getPluginLog().logError(e);
 			}
 			return false;
 		}
 	}
 
 	private void updateRangeIndication0(ISelection selection) {
 		if (selection instanceof IStructuredSelection
 				&& !((IStructuredSelection) selection).isEmpty()) {
 			Object[] objects = ((IStructuredSelection) selection).toArray();
 			if (objects.length > 0) {
 				int start = ((IndexedRegion) objects[0]).getStartOffset();
 				int end = ((IndexedRegion) objects[objects.length - 1])
 						.getEndOffset();
 				getSourceViewer().setRangeIndication(start, end - start, false);
 			} else {
 				getSourceViewer().removeRangeIndication();
 			}
 		} else {
 			if (selection instanceof ITextSelection) {
 				getSourceViewer().setRangeIndication(
 						((ITextSelection) selection).getOffset(),
 						((ITextSelection) selection).getLength(), false);
 			} else {
 				getSourceViewer().removeRangeIndication();
 			}
 		}
 	}
 
 	protected IExtendedAction createExtendedAction(String actionID) {
 		if (StructuredTextEditorActionConstants.ACTION_NAME_FORMAT_DOCUMENT
 				.equals(actionID)
 				|| ITextEditorActionConstants.UNDO.equals(actionID)
 				|| ITextEditorActionConstants.REDO.equals(actionID)) {
 			return new ExtendedFormatAction(this, actionID);
 		}
 		return null;
 	}
 
 	protected void initializeEditor() {
 		super.initializeEditor();
 		getPreferenceStore();
 	}
 
 	public void dispose() {
 		// some things in the configuration need to clean
 		// up after themselves
 		getSourceViewer().removeTextListener(this);
 		if (fOutlinePage != null) {
 			if (fOutlinePage instanceof ConfigurableContentOutlinePage && fOutlinePageListener != null) {
 				((ConfigurableContentOutlinePage) fOutlinePage).removeDoubleClickListener(fOutlinePageListener);
 			}
 			if (fOutlinePageListener != null) {
 				fOutlinePage.removeSelectionChangedListener(fOutlinePageListener);
 			}
 		}
 		fOutlinePage=null;
 		fOutlinePageListener=null;
 		if (fOccurrenceModelUpdater != null) {
 			fOccurrenceModelUpdater.uninstall();
 			fOccurrenceModelUpdater = null;
 		}
 		super.dispose();
 		if (wtpTextJspKbConnector != null) {
 			wtpTextJspKbConnector.setTaglibManagerProvider(null);
 			wtpTextJspKbConnector = null;
 		}
 		if (listener != null)
 			listener.dispose();
 		listener = null;
 	}
 
 	BodyListenerImpl listener = null;
 
 	class BodyListenerImpl implements FileAnyImpl.BodyListener {
 		FileAnyImpl file;
 
 		BodyListenerImpl(FileAnyImpl file) {
 			this.file = file;
 			file.addListener(this);
 		}
 
 		public void bodyChanged(String body) {
 			setText(body);
 		}
 
 		public void dispose() {
 			file.removeListener(this);
 		}
 	}
 
 	public void setText(String text) {
 		if (getSourceViewer() == null
 				|| getSourceViewer().getDocument() == null)
 			return;
 		String txt = getSourceViewer().getDocument().get();
 		if (txt != null && txt.length() > 0) {
 			if (!TextMerge.replace(getSourceViewer().getDocument(), text)) {
 				getSourceViewer().getDocument().set(text);
 			}
 		} else {
 			getSourceViewer().getDocument().set(text);
 		}
 	}
 
 	boolean isInsideResponseRedirect(Text textNode, int off) {
 		if (off < 0)
 			return false;
 		String START = "response.sendRedirect(\"";
 		String END = "\")";
 		String text = textNode.getNodeValue();
 		int i = 0;
 		while (i < text.length() && i < off) {
 			int i1 = text.indexOf(START, i);
 			if (i1 < 0 || i1 + START.length() > off)
 				return false;
 			int i2 = text.indexOf(END, i1 + START.length());
 			if (i2 < 0 || i2 >= off)
 				return true;
 			i = i2 + END.length();
 		}
 		return false;
 	}
 }
