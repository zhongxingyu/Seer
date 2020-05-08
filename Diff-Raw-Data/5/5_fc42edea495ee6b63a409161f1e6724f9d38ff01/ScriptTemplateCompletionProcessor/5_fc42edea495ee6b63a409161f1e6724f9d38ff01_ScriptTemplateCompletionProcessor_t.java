 /*******************************************************************************
  * Copyright (c) 2005, 2007 IBM Corporation and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  
  *******************************************************************************/
 package org.eclipse.dltk.ui.templates;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.List;
 
 import org.eclipse.dltk.core.DLTKCore;
 import org.eclipse.dltk.core.DLTKLanguageManager;
 import org.eclipse.dltk.core.IDLTKLanguageToolkit;
 import org.eclipse.dltk.core.ISourceModule;
 import org.eclipse.dltk.internal.ui.editor.ScriptEditor;
 import org.eclipse.dltk.ui.DLTKPluginImages;
 import org.eclipse.dltk.ui.DLTKUIPlugin;
 import org.eclipse.dltk.ui.text.completion.ScriptContentAssistInvocationContext;
 import org.eclipse.jface.text.BadLocationException;
 import org.eclipse.jface.text.IDocument;
 import org.eclipse.jface.text.IInformationControlCreator;
 import org.eclipse.jface.text.IRegion;
 import org.eclipse.jface.text.ITextSelection;
 import org.eclipse.jface.text.ITextViewer;
 import org.eclipse.jface.text.Region;
 import org.eclipse.jface.text.contentassist.ICompletionProposal;
 import org.eclipse.jface.text.templates.Template;
 import org.eclipse.jface.text.templates.TemplateCompletionProcessor;
 import org.eclipse.jface.text.templates.TemplateContext;
 import org.eclipse.jface.text.templates.TemplateContextType;
 import org.eclipse.jface.text.templates.TemplateException;
 import org.eclipse.jface.text.templates.TemplateProposal;
 import org.eclipse.jface.window.Window;
 import org.eclipse.swt.graphics.Image;
 import org.eclipse.ui.IEditorPart;
 import org.eclipse.ui.part.IWorkbenchPartOrientation;
 
 public abstract class ScriptTemplateCompletionProcessor extends
 		TemplateCompletionProcessor {
 
 	private static final class ProposalComparator implements Comparator {
 		public int compare(Object o1, Object o2) {
 			return ((TemplateProposal) o2).getRelevance()
 					- ((TemplateProposal) o1).getRelevance();
 		}
 	}
 
	private static final Comparator comparator = new ProposalComparator();
 
 	private ScriptContentAssistInvocationContext context;
 
 	public ScriptTemplateCompletionProcessor(
 			ScriptContentAssistInvocationContext context) {
 		if (context == null) {
 			throw new IllegalArgumentException();
 		}
 
 		this.context = context;
 	}
 
 	protected ScriptContentAssistInvocationContext getContext() {
 		return this.context;
 	}
 
 	// TODO: add more customizations
 	public ICompletionProposal[] computeCompletionProposals(ITextViewer viewer,
 			int offset) {
 
 		ITextSelection selection = (ITextSelection) viewer
 				.getSelectionProvider().getSelection();
 
 		// adjust offset to end of normalized selection
 		if (selection.getOffset() == offset)
 			offset = selection.getOffset() + selection.getLength();
 
 		String prefix = extractPrefix(viewer, offset);
 		Region region = new Region(offset - prefix.length(), prefix.length());
 		TemplateContext context = createContext(viewer, region);
 		if (context == null)
 			return new ICompletionProposal[0];
 
 		// name of the selection variables {line, word}_selection
 		context.setVariable("selection", selection.getText()); //$NON-NLS-1$
 
 		Template[] templates = getTemplates(context.getContextType().getId());
 
 		List matches = new ArrayList();
 		for (int i = 0; i < templates.length; i++) {
 			Template template = templates[i];
 			try {
 				context.getContextType().validate(template.getPattern());
 			} catch (TemplateException e) {
 				continue;
 			}
 
 			// Addes check of startsWith
 			if (template.getName().startsWith(prefix)
 					&& template.matches(prefix, context.getContextType()
 							.getId()))
 				matches.add(createProposal(template, context, (IRegion) region,
 						getRelevance(template, prefix)));
 		}
 
		Collections.sort(matches, comparator);
 
 		return (ICompletionProposal[]) matches
 				.toArray(new ICompletionProposal[matches.size()]);
 	}
 
 	protected TemplateContext createContext(ITextViewer viewer, IRegion region) {
 		TemplateContextType contextType = getContextType(viewer, region);
 		if (contextType instanceof ScriptTemplateContextType) {
 			IDocument document = viewer.getDocument();
 
 			ISourceModule sourceModule = getContext().getSourceModule();
 			if (sourceModule == null) {
 				return null;
 			}
 			return ((ScriptTemplateContextType) contextType).createContext(
 					document, region.getOffset(), region.getLength(),
 					sourceModule);
 		}
 
 		return super.createContext(viewer, region);
 	}
 
 	protected ICompletionProposal createProposal(Template template,
 			TemplateContext context, IRegion region, int relevance) {
 		TemplateProposal proposal = new ScriptTemplateProposal(template,
 				context, region, getImage(template), relevance);
 		proposal.setInformationControlCreator(getInformationControlCreator());
 		return proposal;
 	}
 
 	private IInformationControlCreator getInformationControlCreator() {
 		int orientation = Window.getDefaultOrientation();
 		IEditorPart editor = getContext().getEditor();
 		if (editor == null)
 			editor = DLTKUIPlugin.getActivePage().getActiveEditor();
 		if (editor instanceof IWorkbenchPartOrientation)
 			orientation = ((IWorkbenchPartOrientation) editor).getOrientation();
 		IDLTKLanguageToolkit toolkit = null;
 		toolkit = DLTKLanguageManager.getLanguageToolkit(getContext()
 				.getLangaugeNatureID());
 		if ((toolkit == null) && (editor instanceof ScriptEditor))
 			toolkit = ((ScriptEditor) editor).getLanguageToolkit();
 		return new TemplateInformationControlCreator(orientation, toolkit);
 	}
 
 	protected abstract String getContextTypeId();
 
 	protected abstract ScriptTemplateAccess getTemplateAccess();
 
 	protected Template[] getTemplates(String contextTypeId) {
 		if (contextTypeId.equals(getContextTypeId())) {
 			return getTemplateAccess().getTemplateStore().getTemplates();
 		}
 
 		return new Template[0];
 	}
 
 	protected abstract char[] getIgnore();
 
 	protected TemplateContextType getContextType(ITextViewer viewer,
 			IRegion region) {
 
 		boolean contains = false;
 
 		// TODO: make smarter
 		try {
 			String trigger = getTrigger(viewer, region);
 
 			char[] ignore = getIgnore();
 			for (int i = 0; i < ignore.length; i++) {
 				if (trigger.indexOf(ignore[i]) != -1) {
 					contains = true;
 					// don't bother checking the rest of the array
 					break;
 				}
 			}
 		} catch (BadLocationException e) {
 			if (DLTKCore.DEBUG) {
 				e.printStackTrace();
 			}
 		}
 
 		if (!contains) {
 			return getTemplateAccess().getContextTypeRegistry().getContextType(
 					getContextTypeId());
 		}
 
 		return null;
 	}
 
 	protected Image getImage(Template template) {
 		return DLTKPluginImages.get(DLTKPluginImages.IMG_OBJS_TEMPLATE);
 	}
 
 	protected String getTrigger(ITextViewer viewer, IRegion region)
 			throws BadLocationException {
 		IDocument doc = viewer.getDocument();
 		IRegion line = doc.getLineInformationOfOffset(region.getOffset()
 				+ region.getLength());
 		int len = region.getOffset() + region.getLength() - line.getOffset();
 		String s = doc.get(line.getOffset(), len);
 
 		int spaceIndex = s.lastIndexOf(' ');
 		if (spaceIndex != -1) {
 			s = s.substring(spaceIndex);
 		}
 
 		return s;
 	}
 }
