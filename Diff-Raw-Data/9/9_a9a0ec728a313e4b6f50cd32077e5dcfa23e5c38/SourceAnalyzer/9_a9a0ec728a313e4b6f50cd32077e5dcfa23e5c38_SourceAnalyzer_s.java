 /*******************************************************************************
  * Copyright (c) 2009-2012 WalWare/StatET-Project (www.walware.de/goto/statet)
  * and others. All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * Contributors:
  *     Stephan Wahlbrink - initial API and implementation
  *******************************************************************************/
 
 package de.walware.docmlet.tex.internal.core.model;
 
 import static de.walware.docmlet.tex.core.model.ILtxSourceElement.C2_SECTIONING;
 import static de.walware.ecommons.ltk.IModelElement.MASK_C1;
 import static de.walware.ecommons.ltk.IModelElement.MASK_C2;
 
 import java.lang.reflect.InvocationTargetException;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import de.walware.ecommons.collections.ConstList;
 import de.walware.ecommons.ltk.AstInfo;
 import de.walware.ecommons.ltk.ISourceStructElement;
 
 import de.walware.docmlet.tex.core.ast.ControlNode;
 import de.walware.docmlet.tex.core.ast.Embedded;
 import de.walware.docmlet.tex.core.ast.Environment;
 import de.walware.docmlet.tex.core.ast.SourceComponent;
 import de.walware.docmlet.tex.core.ast.TexAst;
 import de.walware.docmlet.tex.core.ast.TexAst.NodeType;
 import de.walware.docmlet.tex.core.ast.TexAstNode;
 import de.walware.docmlet.tex.core.ast.TexAstVisitor;
 import de.walware.docmlet.tex.core.ast.Text;
 import de.walware.docmlet.tex.core.commands.IPreambleDefinitions;
 import de.walware.docmlet.tex.core.commands.LtxPrintCommand;
 import de.walware.docmlet.tex.core.commands.TexCommand;
 import de.walware.docmlet.tex.core.model.EmbeddedReconcileItem;
 import de.walware.docmlet.tex.core.model.ILtxSourceElement;
 import de.walware.docmlet.tex.core.model.ILtxSourceUnit;
 import de.walware.docmlet.tex.core.model.TexElementName;
 import de.walware.docmlet.tex.core.model.TexLabelAccess;
 import de.walware.docmlet.tex.internal.core.model.LtxSourceElement.EmbeddedRef;
 import de.walware.docmlet.tex.internal.core.model.RefLabelAccess.Shared;
 
 
 public class SourceAnalyzer extends TexAstVisitor {
 	
 	
 	private static final Integer ONE = 1;
 	
 	
 	private String fInput;
 	
 	private LtxSourceElement.Container fElement;
 	
 	private final StringBuilder fTitleBuilder = new StringBuilder();
 	private boolean fTitleDoBuild;
 	private LtxSourceElement.Container fTitleElement;
 	private final Map<String, Integer> fStructNamesCounter = new HashMap<String, Integer>();
 	
 	private Map<String, RefLabelAccess.Shared> fLabels = new HashMap<String, RefLabelAccess.Shared>();
 	private final List<EmbeddedReconcileItem> fEmbeddedItems = new ArrayList<EmbeddedReconcileItem>();
 	
 	private int fMinSectionLevel;
 	private int fMaxSectionLevel;
 	
 	
 	public void clear() {
 		fInput = null;
 		fElement = null;
 		
 		fTitleBuilder.setLength(0);
 		fTitleDoBuild = false;
 		fTitleElement = null;
 		
 		if (fLabels == null || !fLabels.isEmpty()) {
 			fLabels = new HashMap<String, RefLabelAccess.Shared>();
 		}
 		fEmbeddedItems.clear();
 		
 		fMinSectionLevel = Integer.MAX_VALUE;
 		fMaxSectionLevel = Integer.MIN_VALUE;
 	}
 	
 	public LtxSourceModelInfo createModel(final ILtxSourceUnit su, final String input,
 			final AstInfo ast,
 			Map<String, TexCommand> customCommands,
 			Map<String, TexCommand> customEnvs) {
 		clear();
 		fInput = input;
 		if (!(ast.root instanceof TexAstNode)) {
 			return null;
 		}
 		final ISourceStructElement root = fElement = new LtxSourceElement.SourceContainer(
 				ILtxSourceElement.C2_SOURCE_FILE, su, (TexAstNode) ast.root);
 		try {
 			((TexAstNode) ast.root).acceptInTex(this);
 			
 			final Map<String, RefLabelAccess.Shared> labels;
 			if (fLabels.isEmpty()) {
 				labels = Collections.emptyMap();
 			}
 			else {
 				labels = fLabels;
 				fLabels = null;
 				for (final Shared access : labels.values()) {
 					access.finish();
 				}
 			}
 			if (fMinSectionLevel == Integer.MAX_VALUE) {
 				fMinSectionLevel = 0;
 				fMaxSectionLevel = 0;
 			}
 			if (customCommands != null) {
 				customCommands = Collections.unmodifiableMap(customCommands);
 			}
 			else {
 				customCommands = Collections.emptyMap();
 			}
 			if (customEnvs != null) {
 				customEnvs = Collections.unmodifiableMap(customEnvs);
 			}
 			else {
 				customEnvs = Collections.emptyMap();
 			}
 			final LtxSourceModelInfo model = new LtxSourceModelInfo(ast, root,
 					fMinSectionLevel, fMaxSectionLevel, labels, customCommands, customEnvs );
 			return model;
 		}
 		catch (final InvocationTargetException e) {
 			throw new IllegalStateException();
 		}
 	}
 	
 	public List<EmbeddedReconcileItem> getEmbeddedItems() {
 		return fEmbeddedItems;
 	}
 	
 	
 	private void exitContainer(final int stop, final boolean forward) {
 		fElement.fLength = ((forward) ?
 						readLinebreakForward((stop >= 0) ? stop : fElement.fOffset + fElement.fLength, fInput.length()) :
 						readLinebreakBackward((stop >= 0) ? stop : fElement.fOffset + fElement.fLength, 0) ) -
 				fElement.fOffset;
 		final List<LtxSourceElement> children = fElement.fChildren;
 		if (!children.isEmpty()) {
 			for (final LtxSourceElement element : children) {
 				if ((element.getElementType() & MASK_C2) == C2_SECTIONING) {
 					final Map<String, Integer> names = fStructNamesCounter;
 					final String name = element.getElementName().getDisplayName();
 					final Integer occ = names.get(name);
 					if (occ == null) {
 						names.put(name, ONE);
 					}
 					else {
 						names.put(name, Integer.valueOf(
 								(element.fOccurrenceCount = occ + 1) ));
 					}
 				}
 			}
 			fStructNamesCounter.clear();
 		}
 		fElement = fElement.getModelParent();
 	}
 	
 	private int readLinebreakForward(int offset, final int limit) {
 		if (offset < limit) {
 			switch(fInput.charAt(offset)) {
 			case '\n':
 				if (++offset < limit && fInput.charAt(offset) == '\r') {
 					return ++offset;
 				}
 				return offset;
 			case '\r':
 				if (++offset < limit && fInput.charAt(offset) == '\n') {
 					return ++offset;
 				}
 				return offset;
 			}
 		}
 		return offset;
 	}
 	private int readLinebreakBackward(int offset, final int limit) {
 		if (offset > limit) {
 			switch(fInput.charAt(offset-1)) {
 			case '\n':
 				if (--offset > limit && fInput.charAt(offset-1) == '\r') {
 					return --offset;
 				}
 				return offset;
 			case '\r':
 				if (--offset < limit && fInput.charAt(offset-1) == '\n') {
 					return --offset;
 				}
 				return offset;
 			}
 		}
 		return offset;
 	}
 	
 	@Override
 	public void visit(final SourceComponent node) throws InvocationTargetException {
 		fElement.fOffset = node.getOffset();
 		node.acceptInTexChildren(this);
 		if (fTitleElement != null) {
 			finishTitleText();
 		}
 		while ((fElement.getElementType() & MASK_C1) != ILtxSourceElement.C1_SOURCE) {
 			exitContainer(node.getStopOffset(), true);
 		}
 		exitContainer(node.getStopOffset() - fElement.fOffset, true);
 	}
 	
 	@Override
 	public void visit(final Environment node) throws InvocationTargetException {
 		final TexCommand command = node.getBeginNode().getCommand();
 		
 		if ((command.getType() & TexCommand.MASK_C2) == TexCommand.C2_ENV_DOCUMENT_BEGIN) {
 			if (fTitleElement != null) {
 				finishTitleText();
 			}
 			while ((fElement.getElementType() & MASK_C1) != ILtxSourceElement.C1_SOURCE) {
 				exitContainer(node.getOffset(), false);
 			}
 		}
 		
 		node.acceptInTexChildren(this);
 		
 		if ((command.getType() & TexCommand.MASK_C2) == TexCommand.C2_ENV_DOCUMENT_BEGIN) {
 			if (fTitleElement != null) {
 				finishTitleText();
 			}
 			while ((fElement.getElementType() & MASK_C1) != ILtxSourceElement.C1_SOURCE) {
 				exitContainer((node.getEndNode() != null) ?
 						node.getEndNode().getOffset() : node.getStopOffset(), false );
 			}
 		}
 		
 		{	final TexAstNode beginLabel = getLabelNode(node.getBeginNode());
 			if (beginLabel != null) {
 				final EnvLabelAccess[] access;
 				final TexAstNode endLabel = getLabelNode(node.getEndNode());
 				if (endLabel != null) {
 					access = new EnvLabelAccess[2];
 					access[0] = new EnvLabelAccess(node.getBeginNode(), beginLabel);
 					access[1] = new EnvLabelAccess(node.getEndNode(), endLabel);
 				}
 				else {
 					access = new EnvLabelAccess[1];
 					access[0] = new EnvLabelAccess(node.getBeginNode(), endLabel);
 				}
 				final ConstList<TexLabelAccess> list = new ConstList<TexLabelAccess>(access);
 				for (int i = 0; i < access.length; i++) {
 					access[i].fAll = list;
 					access[i].getNode().addAttachment(access[i]);
 				}
 			}
 		}
 	}
 	
 	@Override
 	public void visit(final ControlNode node) throws InvocationTargetException {
 		final TexCommand command = node.getCommand();
 		COMMAND: if (command != null) {
 			switch (command.getType() & TexCommand.MASK_MAIN) {
 			case TexCommand.PREAMBLE:
 				if (command == IPreambleDefinitions.PREAMBLE_documentclass_COMMAND) {
 					if (fTitleElement != null) {
 						finishTitleText();
 					}
 					while ((fElement.getElementType() & MASK_C1) != ILtxSourceElement.C1_SOURCE) {
 						exitContainer(node.getOffset(), false);
 					}
 					initElement(new LtxSourceElement.StructContainer(
 							ILtxSourceElement.C2_PREAMBLE, fElement, node ));
 					fElement.fName = TexElementName.create(TexElementName.TITLE, "Preamble");
 				}
 				break;
 			case TexCommand.SECTIONING:
 				if ((fElement.getElementType() & MASK_C2) == ILtxSourceElement.C2_PREAMBLE) {
 					exitContainer(node.getOffset(), false);
 				}
 				if ((fElement.getElementType() & MASK_C2) == ILtxSourceElement.C2_SECTIONING
 						|| (fElement.getElementType() & MASK_C1) == ILtxSourceElement.C1_SOURCE ) {
 					final int level = (command.getType() & 0xf0) >> 4;
 					if (level > 5) {
 						break COMMAND;
 					}
 					if (fTitleElement != null) {
 						finishTitleText();
 						break COMMAND;
 					}
 					
 					while ((fElement.getElementType() & MASK_C2) == ILtxSourceElement.C2_SECTIONING
 							&& (fElement.getElementType() & 0xf) >= level) {
 						exitContainer(node.getOffset(), false);
 					}
 					initElement(new LtxSourceElement.StructContainer(
 							ILtxSourceElement.C2_SECTIONING | level, fElement, node ));
 					
 					fMinSectionLevel = Math.min(fMinSectionLevel, level);
 					fMaxSectionLevel = Math.max(fMaxSectionLevel, level);
 					
 					final int count = node.getChildCount();
 					if (count > 0) {
 						fTitleElement = fElement;
 						fTitleDoBuild = true;
 						final TexAstNode titleNode = node.getChild(0);
 						fTitleElement.fNameRegion = TexAst.getInnerRegion(titleNode);
 						node.getChild(0).acceptInTex(this);
 						if (fTitleElement != null) {
 							finishTitleText();
 						}
 						for (int i = 1; i < count; i++) {
 							node.getChild(i).acceptInTex(this);
 						}
 					}
 					else {
 						fElement.fName = TexElementName.create(TexElementName.TITLE, "");
 					}
 					fElement.fLength = Math.max(fElement.fLength, node.getLength());
 					return;
 				}
 				break;
 			case TexCommand.LABEL:
 				if ((command.getType() & TexCommand.MASK_C2) == TexCommand.C2_LABEL_REFLABEL) {
 					final TexAstNode nameNode = getLabelNode(node);
 					if (nameNode != null) {
 						final String label = nameNode.getText();
 						RefLabelAccess.Shared shared = fLabels.get(label);
 						if (shared == null) {
 							shared = new RefLabelAccess.Shared(label);
 							fLabels.put(label, shared);
 						}
 						final RefLabelAccess access = new RefLabelAccess(shared, node, nameNode);
 						if ((command.getType() & TexCommand.MASK_C3) == TexCommand.C3_LABEL_REFLABEL_DEF) {
 							access.fFlags |= RefLabelAccess.A_WRITE;
 						}
 						node.addAttachment(access);
 					}
 					final boolean prevDoBuild = fTitleDoBuild;
 					fTitleDoBuild = false;
 					node.acceptInTexChildren(this);
 					if (prevDoBuild && fTitleElement != null) {
 						fTitleDoBuild = true;
 					}
 					
 					fElement.fLength = node.getStopOffset() - fElement.getOffset();
 					return;
 				}
 			case TexCommand.SYMBOL:
 			case TexCommand.MATHSYMBOL:
 				if (command instanceof LtxPrintCommand
 						&& command.getArguments().isEmpty()
 						&& fTitleDoBuild) {
 					final String text = ((LtxPrintCommand) command).getText();
 					if (text != null) {
 						if (text.length() == 1 && Character.getType(text.charAt(0)) == Character.NON_SPACING_MARK) {
 							final int size = fTitleBuilder.length();
 							node.acceptInTexChildren(this);
 							if (fTitleElement != null && fTitleBuilder.length() == size + 1) {
 								fTitleBuilder.append(text);
 							}
 							
 							fElement.fLength = node.getStopOffset() - fElement.getOffset();
 							return;
 						}
 						fTitleBuilder.append(text);
 					}
 				}
 				break;
 			}
 		}
 		
 		node.acceptInTexChildren(this);
 		
 		fElement.fLength = node.getStopOffset() - fElement.getOffset();
 	}
 	
 	private void initElement(final LtxSourceElement.Container element) {
 		if (fElement.fChildren.isEmpty()) {
 			fElement.fChildren = new ArrayList<LtxSourceElement>();
 		}
 		fElement.fChildren.add(element);
 		fElement = element;
 	}
 	
 	@Override
 	public void visit(final Text node) throws InvocationTargetException {
 		if (fTitleDoBuild) {
 			fTitleBuilder.append(fInput, node.getOffset(), node.getStopOffset());
 			if (fTitleBuilder.length() >= 100) {
 				finishTitleText();
 			}
 		}
 		
 		fElement.fLength = node.getStopOffset() - fElement.getOffset();
 	}
 	
 	@Override
 	public void visit(final Embedded node) throws InvocationTargetException {
 		if (!node.isInline()) {
 			if (fTitleDoBuild) {
 				fTitleBuilder.append(fInput, node.getOffset(), node.getStopOffset());
 				if (fTitleBuilder.length() >= 100) {
 					finishTitleText();
 				}
 			}
 			if (fElement.fChildren.isEmpty()) {
 				fElement.fChildren = new ArrayList<LtxSourceElement>();
 			}
 			final EmbeddedRef element = new LtxSourceElement.EmbeddedRef(node.getText(), fElement,
 					node );
 			element.fOffset = node.getOffset();
 			element.fLength = node.getLength();
 			element.fName = TexElementName.create(0, ""); //$NON-NLS-1$
 			fElement.fChildren.add(element);
 			fEmbeddedItems.add(new EmbeddedReconcileItem(node, element));
 		}
 		fElement.fLength = node.getStopOffset() - fElement.getOffset();
 	}
 	
 	
 	private TexAstNode getLabelNode(TexAstNode node) {
 		if (node != null && node.getNodeType() == NodeType.CONTROL && node.getChildCount() > 0) {
 			node = node.getChild(0);
 			if (node.getNodeType() == NodeType.LABEL) {
 				return node;
 			}
 			if (node.getNodeType() == NodeType.GROUP && node.getChildCount() > 0) {
 				node = node.getChild(0);
 				if (node.getNodeType() == NodeType.LABEL) {
 					return node;
 				}
 			}
 		}
 		return null;
 	}
 	
 	private void finishTitleText() {
 		{	boolean wasWhitespace = false;
 			int idx = 0;
 			while (idx < fTitleBuilder.length()) {
				if (wasWhitespace) {
					if (fTitleBuilder.charAt(idx) == ' ') {
 						fTitleBuilder.deleteCharAt(idx);
 					}
 					else {
 						idx++;
 					}
 				}
 				else {
					wasWhitespace = (fTitleBuilder.charAt(idx) == ' ');
 					idx++;
 				}
 			}
 		}
 		fTitleElement.fName = TexElementName.create(TexElementName.TITLE, fTitleBuilder.toString());
 		fTitleBuilder.setLength(0);
 		fTitleElement = null;
 		fTitleDoBuild = false;
 	}
 	
 }
