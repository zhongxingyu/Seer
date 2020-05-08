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
 package org.jboss.tools.jst.jsp.outline.cssdialog.common;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 import java.util.Map.Entry;
 
 import org.eclipse.core.filebuffers.FileBuffers;
 import org.eclipse.core.filebuffers.IFileBuffer;
 import org.eclipse.core.filebuffers.LocationKind;
 import org.eclipse.core.resources.IFile;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.jface.text.IDocument;
 import org.eclipse.jface.text.IDocumentExtension3;
 import org.eclipse.jface.text.IDocumentPartitioner;
 import org.eclipse.wst.css.core.internal.provisional.document.ICSSDocument;
 import org.eclipse.wst.css.core.internal.provisional.document.ICSSModel;
 import org.eclipse.wst.css.core.internal.provisional.document.ICSSNode;
 import org.eclipse.wst.css.core.internal.provisional.document.ICSSStyleSheet;
 import org.eclipse.wst.css.core.internal.text.StructuredTextPartitionerForCSS;
 import org.eclipse.wst.sse.core.StructuredModelManager;
 import org.eclipse.wst.sse.core.internal.provisional.IModelManager;
 import org.eclipse.wst.sse.core.internal.provisional.IStructuredModel;
 import org.eclipse.wst.sse.core.internal.provisional.IndexedRegion;
 import org.eclipse.wst.sse.core.internal.provisional.exceptions.ResourceInUse;
 import org.eclipse.wst.sse.core.internal.provisional.text.IStructuredPartitioning;
 import org.jboss.tools.jst.jsp.JspEditorPlugin;
 import org.w3c.dom.css.CSSRule;
 import org.w3c.dom.css.CSSRuleList;
 import org.w3c.dom.css.CSSStyleDeclaration;
 import org.w3c.dom.css.CSSStyleRule;
 import org.w3c.dom.css.CSSStyleSheet;
 
 /**
  * CSS class model.
  * 
  * 
  */
 public class CSSModel implements ICSSDialogModel {
 
 	private IStructuredModel model = null;
 	private IFile styleFile = null;
 
 	private CSSStyleSheet styleSheet = null;
 	private static final String COPY_SUFFIX = "_copy"; //$NON-NLS-1$
 	private boolean copy = false;
 	
 	// workaround for JBIDE-4407
 	private String oldText = null;
 
 	/**
 	 * Constructor.
 	 * 
 	 * @param styleFile
 	 *            CSS style class that should initialize CSS model
 	 */
 	public CSSModel(final IFile file) {
 		this.styleFile = file;
 		if (file != null)
 			init();
 	}
 
 	protected void init() {
 
 		try {
 			if (model != null) {
 				release();
 			}
 			copy = false;
 			IModelManager modelManager = StructuredModelManager
 					.getModelManager();
 			model = modelManager.getExistingModelForEdit(styleFile);
 
 			if (model == null) {
 				model = modelManager.getModelForEdit(styleFile);
 			} else {
 
 				copy = true;
 				// copy the model
 				model = modelManager.copyModelForEdit(model.getId(), model
 						.getId()
 						+ COPY_SUFFIX);
 
 				// set the correct location
 				model.setBaseLocation(styleFile.getLocation().toString());
 
 				// some steps to prepare document ( it is necessary to correct
 				// work of highlight in preview tab )
 				IDocumentPartitioner partitioner = new StructuredTextPartitionerForCSS();
 				((IDocumentExtension3) model.getStructuredDocument())
 						.setDocumentPartitioner(
 								IStructuredPartitioning.DEFAULT_STRUCTURED_PARTITIONING,
 								partitioner);
 				partitioner.connect(model.getStructuredDocument());
 
 			}
 			// workaround for JBIDE-4407
 			oldText = model.getStructuredDocument().get();
 
 			if (model instanceof ICSSModel) {
 				ICSSModel cssModel = (ICSSModel) model;
 
 				if (cssModel.getDocument() instanceof CSSStyleSheet) {
 					styleSheet = (CSSStyleSheet) cssModel.getDocument();
 					prepareModel(styleSheet);
 				}
 			}
 		} catch (IOException e) {
 			JspEditorPlugin.getPluginLog().logError(e.getMessage());
 		} catch (CoreException e) {
 			JspEditorPlugin.getPluginLog().logError(e.getMessage());
 		} catch (ResourceInUse e) {
 			JspEditorPlugin.getPluginLog().logError(e.getMessage());
 		}
 
 	}
 
 	public String addCSSRule(final String selector) {
 		String selectorLabel = null;
 		if ((styleSheet != null) && (selector != null)
 				&& !selector.equals(Constants.EMPTY)) {
 			CSSStyleRule rule = (CSSStyleRule) ((ICSSDocument) styleSheet)
 					.createCSSRule(selector + Constants.LEFT_BRACE + Constants.RIGHT_BRACE);
 			((ICSSStyleSheet) styleSheet).appendRule(rule);
 
 			for (Map.Entry<String, CSSStyleRule> ruleEntry : getRulesMapping()
 					.entrySet()) {
 				if (ruleEntry.getValue() == rule)
 					selectorLabel = ruleEntry.getKey();
 			}
 
 		}
 		return selectorLabel;
 	}
 
 	protected CSSStyleRule getCSSStyle(final String selectorLabel) {
 		if (selectorLabel != null) {
 			final CSSStyleRule rule = getRulesMapping().get(selectorLabel);
 			if (rule != null)
 				return rule;
 
 		}
 
 		return null;
 	}
 
 	public IDocument getDocument() {
 		if (model != null)
 			return model.getStructuredDocument();
 		return null;
 	}
 
 	public IFile getFile() {
 		return styleFile;
 	}
 
 	public IndexedRegion getIndexedRegion(final String selectorLabel) {
 		if (selectorLabel != null) {
 			final CSSStyleRule rule = getRulesMapping().get(selectorLabel);
 			if (rule != null)
 				if (rule instanceof IndexedRegion)
 					return (IndexedRegion) rule;
 
 		}
 		return null;
 	}
 
 	public String getSelectorLabel(final int offset) {
 		ICSSNode node = (ICSSNode) model.getIndexedRegion(offset);
 
 		while (node != null) {
 
 			if (node.getNodeType() == ICSSNode.STYLERULE_NODE) {
 				break;
 			} else if (node.getNodeType() == ICSSNode.STYLESHEET_NODE) {
 				node = ((ICSSStyleSheet) node).getFirstChild();
 				break;
 			}
 
 			node = node.getParentNode();
 		}
 
 		if (node != null) {
 			for (final Entry<String, CSSStyleRule> rule : getRulesMapping()
 					.entrySet()) {
 				if (node.equals(rule.getValue()))
 					return rule.getKey();
 			}
 		}
 		return null;
 	}
 
 	public List<String> getSelectorLabels() {
 
 		List<String> selectorLabels;
 
 		selectorLabels = new ArrayList<String>(getRulesMapping().keySet());
 
 		Collections.sort(selectorLabels);
 
 		return selectorLabels;
 	}
 
 	public void reinit() {
 		init();
 
 	}
 
 	public void release() {
 
 		if ((model != null) /* && !modelManager.isShared(model.getId()) */) {
 			model.releaseFromEdit();
 		}
 		model = null;
 
 		// workaround for JBIDE-4407
 		oldText = null;
 
 	}
 
 	public void save() {
 		try {
 
 			/*
 			 * it is necessary not to dialog appears when "dirty" css file is
 			 * being saved ( test case : 1) open css file 2) make same changes
 			 * 3) open css dialog 4) make some changes 5)press ok )
 			 * 
 			 * 
 			 * it is necessary to distinguish real model from copy. For real
 			 * model next step reject all changes
 			 */
 			if (copy) {
 				IFileBuffer buffer = FileBuffers.getTextFileBufferManager()
 						.getFileBuffer(styleFile.getFullPath(),
 								LocationKind.NORMALIZE);
 				buffer.setDirty(false);
 			}
 
 			// workaround for JBIDE-4407
 			// if the model is unchanged the text CSS editor
 			// loses highlighting. When the problem will be fixed on 
 			// the WTP side, the following checking will not be needed.
 			String newText = model.getStructuredDocument().get();
 			if (!oldText.equals(newText)) {
 				model.save();
 				oldText = newText;
 			}
 		} catch (IOException e) {
 			JspEditorPlugin.getPluginLog().logError(e.getMessage());
 		} catch (CoreException e) {
 			JspEditorPlugin.getPluginLog().logError(e.getMessage());
 		}
 
 	}
 
 	public void setFile(final IFile file) {
 		this.styleFile = file;
 	}
 
 	public void updateCSSStyle(final String selectorLabel,
 			final StyleAttributes styleAttributes) {
 		if ((styleSheet != null) && (selectorLabel != null)
 				&& !selectorLabel.equals(Constants.EMPTY)) {
 			final CSSStyleRule rule = getRulesMapping().get(selectorLabel);
 			if (rule != null) {
 
 				final CSSStyleDeclaration declaration = rule.getStyle();
 
 				// set properties
 				final Set<Entry<String, String>> set = styleAttributes
 						.entrySet();
 
 				if ((set.size() == 0) && (declaration.getLength() > 0)) {
 					declaration.setCssText(Constants.EMPTY);
 				} else {
 					for (final Map.Entry<String, String> me : set) {
 						if ((me.getValue() == null)
 								|| (me.getValue().length() == 0)) {
 							declaration.removeProperty(me.getKey());
 						} else {
 							declaration.setProperty(me.getKey(), me.getValue(),
 									Constants.EMPTY);
 						}
 					}
 				}
 			}
 		}
 
 	}
 
 	public Map<String, String> getClassProperties(final String selectorLabel) {
 		final CSSStyleRule rule = getRulesMapping().get(selectorLabel);
 		Map<String, String> styleMap = new HashMap<String, String>();
 		if (rule != null) {
 			final CSSStyleDeclaration declaration = rule.getStyle();
 			for (int i = 0; i < declaration.getLength(); i++) {
 				String propperty = declaration.item(i);
 				String value = declaration.getPropertyValue(propperty);
 				styleMap.put(propperty, value);
 			}
 		}
 
 		return styleMap;
 
 	}
 
 	private void prepareModel(final CSSStyleSheet styleSheet) {
 
 		final CSSRuleList rules = styleSheet.getCssRules();
 		if ((rules != null) && (rules.getLength() > 0)) {
 			final CSSRule rule = rules.item(rules.getLength() - 1);
 			final String text = rule.getCssText();
			if ((text != null) && (!text.endsWith(Constants.RIGHT_BRACE))) {
 				rule.setCssText(text + "\n" + Constants.RIGHT_BRACE); //$NON-NLS-1$
 			}
 		}
 	}
 
 	/**
 	 * get mapping key is label ( label = class name + sequence number of such
 	 * css class ) value is CSSStyleRule
 	 * 
 	 * now rule mapping is generated always ... keeping of ruleMapping is more
 	 * right but it demands more complex synchronization data
 	 * 
 	 */
 	protected Map<String, CSSStyleRule> getRulesMapping() {
 
 		final Map<String, CSSStyleRule> rulesMapping = new HashMap<String, CSSStyleRule>();
 		if (styleSheet != null) {
 			final CSSRuleList list = styleSheet.getCssRules();
 
 			final Map<String, Integer> frequencyMap = new HashMap<String, Integer>();
 
 			if (list != null) {
 				for (int i = 0; i < list.getLength(); i++) {
 					if (list.item(i) instanceof CSSStyleRule) {
 
 						final CSSStyleRule rule = ((CSSStyleRule) list.item(i));
 
 						Integer freq = frequencyMap.get(rule.getSelectorText());
 
 						freq = freq == null ? 1 : freq + 1;
 
 						frequencyMap.put(rule.getSelectorText(), freq);
 
 						final String ruleLabel = rule.getSelectorText()
 								+ (freq > 1 ? Constants.START_BRACKET + freq
 										+ Constants.END_BRACKET
 										: Constants.EMPTY);
 
 						rulesMapping.put(ruleLabel, rule);
 
 					}
 				}
 			}
 
 		}
 
 		return rulesMapping;
 	}
 
 	public String getCSSRuleText(final String selectorLabel) {
 		final CSSStyleRule rule = getCSSStyle(selectorLabel);
 		if (rule != null)
 			return rule.getCssText();
 		return null;
 	}
 
 	public String getCSSStyleText(final String selectorLabel) {
 		final CSSStyleRule rule = getCSSStyle(selectorLabel);
 		if (rule != null)
 			return rule.getStyle().getCssText();
 		return null;
 	}
 
 }
