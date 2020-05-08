 /*******************************************************************************
  * Copyright (c) 2011-2012 Red Hat, Inc.
  * Distributed under license by Red Hat, Inc. All rights reserved.
  * This program is made available under the terms of the
  * Eclipse Public License v1.0 which accompanies this distribution,
  * and is available at http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     Red Hat, Inc. - initial API and implementation
  ******************************************************************************/
 package org.jboss.tools.jst.text.ext.hyperlink;
 
 import org.eclipse.jface.text.BadLocationException;
 import org.eclipse.jface.text.Document;
 import org.eclipse.jface.text.IDocument;
 import org.eclipse.jface.text.IInformationControl;
 import org.eclipse.jface.text.IInformationControlCreator;
 import org.eclipse.jface.text.IRegion;
 import org.eclipse.jface.text.ITextViewer;
 import org.eclipse.jface.text.hyperlink.IHyperlink;
 import org.eclipse.jface.viewers.StyledString;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.widgets.Shell;
 import org.jboss.tools.common.el.core.ELReference;
 import org.jboss.tools.common.el.core.resolver.ELSegment;
 import org.jboss.tools.common.el.core.resolver.ELSegmentImpl.VarOpenable;
 import org.jboss.tools.common.el.core.resolver.Var;
 import org.jboss.tools.common.text.ext.hyperlink.AbstractHyperlink;
 import org.jboss.tools.common.text.ext.hyperlink.HyperlinkRegion;
 import org.jboss.tools.common.text.ext.hyperlink.InformationControlManager;
 import org.jboss.tools.common.text.ext.hyperlink.xpl.HierarchyInformationControl;
 import org.jboss.tools.common.util.FileUtil;
 import org.jboss.tools.jst.text.ext.JSTExtensionsPlugin;
 
 public class ELVarListHyperlink extends AbstractHyperlink {
 	ITextViewer viewer;
 	ELHyperlink[] hyperlinks;
 	private ELReference reference;
 	private ELSegment segment;
 
 	public ELVarListHyperlink(ITextViewer viewer, ELReference reference, ELSegment segment, ELHyperlink[] hyperlinks) {
 		this.viewer = viewer;
 		this.hyperlinks = hyperlinks;
 		this.reference = reference;
 		this.segment = segment;
 		setDocument(viewer.getDocument());
 	}
 
 	/**
 	 * (non-Javadoc)
 	 * @see org.jboss.tools.common.text.ext.hyperlink.AbstractHyperlink#doGetHyperlinkRegion(int)
 	 */ 
 	@Override
 	protected IRegion doGetHyperlinkRegion(int offset) {
 		IRegion region = super.doGetHyperlinkRegion(offset);
 		return (region != null ? region : new HyperlinkRegion(
 					reference.getStartPosition()+segment.getSourceReference().getStartPosition(),
 					segment.getSourceReference().getLength()));
 	}
 
 	@Override
 	protected void doHyperlink(IRegion region) {
 		VarInformationControlManager.instance.showHyperlinks("Open Var Declaration", viewer, hyperlinks); //$NON-NLS-1$
 	}
 	
 	@Override
 	public String getHyperlinkText() {
 		return "Open Var Declaration"; //$NON-NLS-1$
 	}
 
 }
 
 class VarInformationControlManager extends InformationControlManager {
 	static VarInformationControlManager instance = new VarInformationControlManager();
 	
 	protected IInformationControlCreator getHierarchyPresenterControlCreator(final String title, final IHyperlink[] hyperlinks) {
 		return new IInformationControlCreator() {
 			public IInformationControl createInformationControl(Shell parent) {
 				int shellStyle= SWT.RESIZE;
 				int treeStyle= SWT.V_SCROLL | SWT.H_SCROLL;
 				HierarchyInformationControl iControl = new VarHierarchyInformationControl(parent, title, shellStyle, treeStyle, hyperlinks);
 				iControl.setInput(hyperlinks);
 				return iControl;
 			}
 		};
 	}
 }
 
 class VarHierarchyInformationControl extends HierarchyInformationControl {
 	public VarHierarchyInformationControl(Shell parent, String title, int shellStyle, int tableStyle, IHyperlink[] hyperlinks) {
 		super(parent, title, shellStyle, tableStyle, hyperlinks);
 	}
 
 	protected BeanTableLabelProvider createTableLableProvider() {
 		return new VarTableLabelProvider();
 	}
 
 	
 	class VarTableLabelProvider extends BeanTableLabelProvider {
 		
 		public StyledString getStyledText(Object element) {
 			StyledString sb = new StyledString();
 			if(element instanceof ELHyperlink) {
 				ELHyperlink el = (ELHyperlink)element;
 				VarOpenable v = (VarOpenable)el.openable;
 				Var var = v.getVar();
 				IDocument doc = new Document(FileUtil.getContentFromEditorOrFile(var.getFile()));
 				int line = -1;
 				try {
 					if(var.getDeclarationOffset() < doc.getLength()) {
 						line = doc.getLineOfOffset(var.getDeclarationOffset()) + 1;
 					}
 				} catch (BadLocationException e) {
 					JSTExtensionsPlugin.getDefault().logError(e);
 				}
 				sb.append(var.getFile().getName(), NAME_STYLE);
 				if(line > 0) {
 					sb.append(", line " + line, NAME_STYLE);
 				}
 				sb.append(" - " + var.getFile().getParent().getFullPath().toString(), PACKAGE_STYLE);
 			} else if(element instanceof IHyperlink){
 				sb.append(((IHyperlink)element).getHyperlinkText(), NAME_STYLE);
 			}
 			return sb;
 		}
 
 	}
	
	
 }
