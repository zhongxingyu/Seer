 /*=============================================================================#
  # Copyright (c) 2009-2014 Stephan Wahlbrink (WalWare.de) and others.
  # All rights reserved. This program and the accompanying materials
  # are made available under the terms of the Eclipse Public License v1.0
  # which accompanies this distribution, and is available at
  # http://www.eclipse.org/legal/epl-v10.html
  # 
  # Contributors:
  #     Stephan Wahlbrink - initial API and implementation
  #=============================================================================*/
 
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
 
 import de.walware.ecommons.collections.ConstArrayList;
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
 	
 	
 	private static final Integer ONE= 1;
 	
 	
 	private String input;
 	
 	private LtxSourceElement.Container currentElement;
 	
 	private final StringBuilder titleBuilder= new StringBuilder();
 	private boolean titleDoBuild;
 	private LtxSourceElement.Container titleElement;
 	private final Map<String, Integer> structNamesCounter= new HashMap<>();
 	
 	private Map<String, RefLabelAccess.Shared> labels= new HashMap<>();
 	private final List<EmbeddedReconcileItem> embeddedItems= new ArrayList<>();
 	
 	private int minSectionLevel;
 	private int maxSectionLevel;
 	
 	
 	public void clear() {
 		this.input= null;
 		this.currentElement= null;
 		
 		this.titleBuilder.setLength(0);
 		this.titleDoBuild= false;
 		this.titleElement= null;
 		
 		if (this.labels == null || !this.labels.isEmpty()) {
 			this.labels= new HashMap<>();
 		}
 		this.embeddedItems.clear();
 		
 		this.minSectionLevel= Integer.MAX_VALUE;
 		this.maxSectionLevel= Integer.MIN_VALUE;
 	}
 	
 	public LtxSourceModelInfo createModel(final ILtxSourceUnit su, final String input,
 			final AstInfo ast,
 			Map<String, TexCommand> customCommands, Map<String, TexCommand> customEnvs) {
 		clear();
 		this.input= input;
 		if (!(ast.root instanceof TexAstNode)) {
 			return null;
 		}
 		final ISourceStructElement root= this.currentElement= new LtxSourceElement.SourceContainer(
 				ILtxSourceElement.C2_SOURCE_FILE, su, (TexAstNode) ast.root);
 		try {
 			((TexAstNode) ast.root).acceptInTex(this);
 			
 			final Map<String, RefLabelAccess.Shared> labels;
 			if (this.labels.isEmpty()) {
 				labels= Collections.emptyMap();
 			}
 			else {
 				labels= this.labels;
 				this.labels= null;
 				for (final Shared access : labels.values()) {
 					access.finish();
 				}
 			}
 			
 			if (this.minSectionLevel == Integer.MAX_VALUE) {
 				this.minSectionLevel= 0;
 				this.maxSectionLevel= 0;
 			}
 			
 			if (customCommands != null) {
 				customCommands= Collections.unmodifiableMap(customCommands);
 			}
 			else {
 				customCommands= Collections.emptyMap();
 			}
 			if (customEnvs != null) {
 				customEnvs= Collections.unmodifiableMap(customEnvs);
 			}
 			else {
 				customEnvs= Collections.emptyMap();
 			}
 			final LtxSourceModelInfo model= new LtxSourceModelInfo(ast, root,
 					this.minSectionLevel, this.maxSectionLevel, labels, customCommands, customEnvs );
 			return model;
 		}
 		catch (final InvocationTargetException e) {
 			throw new IllegalStateException();
 		}
 	}
 	
 	public List<EmbeddedReconcileItem> getEmbeddedItems() {
 		return this.embeddedItems;
 	}
 	
 	
 	private void exitContainer(final int stop, final boolean forward) {
 		this.currentElement.fLength= ((forward) ?
 						readLinebreakForward((stop >= 0) ? stop : this.currentElement.fOffset + this.currentElement.fLength, this.input.length()) :
 						readLinebreakBackward((stop >= 0) ? stop : this.currentElement.fOffset + this.currentElement.fLength, 0) ) -
 				this.currentElement.fOffset;
 		final List<LtxSourceElement> children= this.currentElement.fChildren;
 		if (!children.isEmpty()) {
 			for (final LtxSourceElement element : children) {
 				if ((element.getElementType() & MASK_C2) == C2_SECTIONING) {
 					final Map<String, Integer> names= this.structNamesCounter;
 					final String name= element.getElementName().getDisplayName();
 					final Integer occ= names.get(name);
 					if (occ == null) {
 						names.put(name, ONE);
 					}
 					else {
 						names.put(name, Integer.valueOf(
 								(element.fOccurrenceCount= occ + 1) ));
 					}
 				}
 			}
 			this.structNamesCounter.clear();
 		}
 		this.currentElement= this.currentElement.getModelParent();
 	}
 	
 	private int readLinebreakForward(int offset, final int limit) {
 		if (offset < limit) {
 			switch(this.input.charAt(offset)) {
 			case '\n':
 				if (++offset < limit && this.input.charAt(offset) == '\r') {
 					return ++offset;
 				}
 				return offset;
 			case '\r':
 				if (++offset < limit && this.input.charAt(offset) == '\n') {
 					return ++offset;
 				}
 				return offset;
 			}
 		}
 		return offset;
 	}
 	private int readLinebreakBackward(int offset, final int limit) {
 		if (offset > limit) {
 			switch(this.input.charAt(offset-1)) {
 			case '\n':
 				if (--offset > limit && this.input.charAt(offset-1) == '\r') {
 					return --offset;
 				}
 				return offset;
 			case '\r':
 				if (--offset < limit && this.input.charAt(offset-1) == '\n') {
 					return --offset;
 				}
 				return offset;
 			}
 		}
 		return offset;
 	}
 	
 	@Override
 	public void visit(final SourceComponent node) throws InvocationTargetException {
 		this.currentElement.fOffset= node.getOffset();
 		node.acceptInTexChildren(this);
 		if (this.titleElement != null) {
 			finishTitleText();
 		}
 		while ((this.currentElement.getElementType() & MASK_C1) != ILtxSourceElement.C1_SOURCE) {
 			exitContainer(node.getStopOffset(), true);
 		}
 		exitContainer(node.getStopOffset() - this.currentElement.fOffset, true);
 	}
 	
 	@Override
 	public void visit(final Environment node) throws InvocationTargetException {
 		final TexCommand command= node.getBeginNode().getCommand();
 		
 		if ((command.getType() & TexCommand.MASK_C2) == TexCommand.C2_ENV_DOCUMENT_BEGIN) {
 			if (this.titleElement != null) {
 				finishTitleText();
 			}
 			while ((this.currentElement.getElementType() & MASK_C1) != ILtxSourceElement.C1_SOURCE) {
 				exitContainer(node.getOffset(), false);
 			}
 		}
 		
 		node.acceptInTexChildren(this);
 		
 		if ((command.getType() & TexCommand.MASK_C2) == TexCommand.C2_ENV_DOCUMENT_BEGIN) {
 			if (this.titleElement != null) {
 				finishTitleText();
 			}
 			while ((this.currentElement.getElementType() & MASK_C1) != ILtxSourceElement.C1_SOURCE) {
 				exitContainer((node.getEndNode() != null) ?
 						node.getEndNode().getOffset() : node.getStopOffset(), false );
 			}
 		}
 		
 		{	final TexAstNode beginLabel= getLabelNode(node.getBeginNode());
 			if (beginLabel != null) {
 				final EnvLabelAccess[] access;
 				final TexAstNode endLabel= getLabelNode(node.getEndNode());
 				if (endLabel != null) {
 					access= new EnvLabelAccess[2];
 					access[0]= new EnvLabelAccess(node.getBeginNode(), beginLabel);
 					access[1]= new EnvLabelAccess(node.getEndNode(), endLabel);
 				}
 				else {
 					access= new EnvLabelAccess[1];
 					access[0]= new EnvLabelAccess(node.getBeginNode(), endLabel);
 				}
 				final ConstArrayList<TexLabelAccess> list= new ConstArrayList<TexLabelAccess>(access);
 				for (int i= 0; i < access.length; i++) {
 					access[i].fAll= list;
 					access[i].getNode().addAttachment(access[i]);
 				}
 			}
 		}
 	}
 	
 	@Override
 	public void visit(final ControlNode node) throws InvocationTargetException {
 		final TexCommand command= node.getCommand();
 		COMMAND: if (command != null) {
 			switch (command.getType() & TexCommand.MASK_MAIN) {
 			case TexCommand.PREAMBLE:
 				if (command == IPreambleDefinitions.PREAMBLE_documentclass_COMMAND) {
 					if (this.titleElement != null) {
 						finishTitleText();
 					}
 					while ((this.currentElement.getElementType() & MASK_C1) != ILtxSourceElement.C1_SOURCE) {
 						exitContainer(node.getOffset(), false);
 					}
 					initElement(new LtxSourceElement.StructContainer(
 							ILtxSourceElement.C2_PREAMBLE, this.currentElement, node ));
 					this.currentElement.fName= TexElementName.create(TexElementName.TITLE, "Preamble");
 				}
 				break;
 			case TexCommand.SECTIONING:
 				if ((this.currentElement.getElementType() & MASK_C2) == ILtxSourceElement.C2_PREAMBLE) {
 					exitContainer(node.getOffset(), false);
 				}
 				if ((this.currentElement.getElementType() & MASK_C2) == ILtxSourceElement.C2_SECTIONING
 						|| (this.currentElement.getElementType() & MASK_C1) == ILtxSourceElement.C1_SOURCE ) {
 					final int level= (command.getType() & 0xf0) >> 4;
 					if (level > 5) {
 						break COMMAND;
 					}
 					if (this.titleElement != null) {
 						finishTitleText();
 						break COMMAND;
 					}
 					
 					while ((this.currentElement.getElementType() & MASK_C2) == ILtxSourceElement.C2_SECTIONING
 							&& (this.currentElement.getElementType() & 0xf) >= level) {
 						exitContainer(node.getOffset(), false);
 					}
 					initElement(new LtxSourceElement.StructContainer(
 							ILtxSourceElement.C2_SECTIONING | level, this.currentElement, node ));
 					
 					this.minSectionLevel= Math.min(this.minSectionLevel, level);
 					this.maxSectionLevel= Math.max(this.maxSectionLevel, level);
 					
 					final int count= node.getChildCount();
 					if (count > 0) {
 						this.titleElement= this.currentElement;
 						this.titleDoBuild= true;
 						final TexAstNode titleNode= node.getChild(0);
 						this.titleElement.fNameRegion= TexAst.getInnerRegion(titleNode);
 						node.getChild(0).acceptInTex(this);
 						if (this.titleElement != null) {
 							finishTitleText();
 						}
 						for (int i= 1; i < count; i++) {
 							node.getChild(i).acceptInTex(this);
 						}
 					}
 					else {
						this.currentElement.fName= TexElementName.create(TexElementName.TITLE, "");
 					}
 					this.currentElement.fLength= Math.max(this.currentElement.fLength, node.getLength());
 					return;
 				}
 				break;
 			case TexCommand.LABEL:
 				if ((command.getType() & TexCommand.MASK_C2) == TexCommand.C2_LABEL_REFLABEL) {
 					final TexAstNode nameNode= getLabelNode(node);
 					if (nameNode != null) {
 						final String label= nameNode.getText();
 						RefLabelAccess.Shared shared= this.labels.get(label);
 						if (shared == null) {
 							shared= new RefLabelAccess.Shared(label);
 							this.labels.put(label, shared);
 						}
 						final RefLabelAccess access= new RefLabelAccess(shared, node, nameNode);
 						if ((command.getType() & TexCommand.MASK_C3) == TexCommand.C3_LABEL_REFLABEL_DEF) {
 							access.fFlags |= RefLabelAccess.A_WRITE;
 						}
 						node.addAttachment(access);
 					}
 					final boolean prevDoBuild= this.titleDoBuild;
 					this.titleDoBuild= false;
 					node.acceptInTexChildren(this);
 					if (prevDoBuild && this.titleElement != null) {
 						this.titleDoBuild= true;
 					}
 					
 					this.currentElement.fLength= node.getStopOffset() - this.currentElement.getOffset();
 					return;
 				}
 			case TexCommand.SYMBOL:
 			case TexCommand.MATHSYMBOL:
 				if (command instanceof LtxPrintCommand
 						&& command.getArguments().isEmpty()
 						&& this.titleDoBuild) {
 					final String text= ((LtxPrintCommand) command).getText();
 					if (text != null) {
 						if (text.length() == 1 && Character.getType(text.charAt(0)) == Character.NON_SPACING_MARK) {
 							final int size= this.titleBuilder.length();
 							node.acceptInTexChildren(this);
 							if (this.titleElement != null && this.titleBuilder.length() == size + 1) {
 								this.titleBuilder.append(text);
 							}
 							
 							this.currentElement.fLength= node.getStopOffset() - this.currentElement.getOffset();
 							return;
 						}
 						this.titleBuilder.append(text);
 					}
 				}
 				break;
 			}
 		}
 		
 		node.acceptInTexChildren(this);
 		
 		this.currentElement.fLength= node.getStopOffset() - this.currentElement.getOffset();
 	}
 	
 	private void initElement(final LtxSourceElement.Container element) {
 		if (this.currentElement.fChildren.isEmpty()) {
 			this.currentElement.fChildren= new ArrayList<>();
 		}
 		this.currentElement.fChildren.add(element);
 		this.currentElement= element;
 	}
 	
 	@Override
 	public void visit(final Text node) throws InvocationTargetException {
 		if (this.titleDoBuild) {
 			this.titleBuilder.append(this.input, node.getOffset(), node.getStopOffset());
 			if (this.titleBuilder.length() >= 100) {
 				finishTitleText();
 			}
 		}
 		
 		this.currentElement.fLength= node.getStopOffset() - this.currentElement.getOffset();
 	}
 	
 	@Override
 	public void visit(final Embedded node) throws InvocationTargetException {
 		if (node.isInline()) {
 			if (this.titleDoBuild) {
 				this.titleBuilder.append(this.input, node.getOffset(), node.getStopOffset());
 				if (this.titleBuilder.length() >= 100) {
 					finishTitleText();
 				}
 			}
 			this.embeddedItems.add(new EmbeddedReconcileItem(node, null));
 		}
 		else {
 			if (this.titleElement != null) {
 				finishTitleText();
 			}
 			if (this.currentElement.fChildren.isEmpty()) {
 				this.currentElement.fChildren= new ArrayList<>();
 			}
 			final EmbeddedRef element= new LtxSourceElement.EmbeddedRef(node.getText(),
 					this.currentElement, node );
 			element.fOffset= node.getOffset();
 			element.fLength= node.getLength();
 			element.fName= TexElementName.create(0, ""); //$NON-NLS-1$
 			this.currentElement.fChildren.add(element);
 			this.embeddedItems.add(new EmbeddedReconcileItem(node, element));
 		}
 		this.currentElement.fLength= node.getStopOffset() - this.currentElement.getOffset();
 	}
 	
 	
 	private TexAstNode getLabelNode(TexAstNode node) {
 		if (node != null && node.getNodeType() == NodeType.CONTROL && node.getChildCount() > 0) {
 			node= node.getChild(0);
 			if (node.getNodeType() == NodeType.LABEL) {
 				return node;
 			}
 			if (node.getNodeType() == NodeType.GROUP && node.getChildCount() > 0) {
 				node= node.getChild(0);
 				if (node.getNodeType() == NodeType.LABEL) {
 					return node;
 				}
 			}
 		}
 		return null;
 	}
 	
 	private void finishTitleText() {
 		{	boolean wasWhitespace= false;
 			int idx= 0;
 			while (idx < this.titleBuilder.length()) {
 				if (this.titleBuilder.charAt(idx) == ' ') {
 					if (wasWhitespace) {
 						this.titleBuilder.deleteCharAt(idx);
 					}
 					else {
 						wasWhitespace= true;
 						idx++;
 					}
 				}
 				else {
 					wasWhitespace= false;
 					idx++;
 				}
 			}
 		}
 		this.titleElement.fName= TexElementName.create(TexElementName.TITLE, this.titleBuilder.toString());
 		this.titleBuilder.setLength(0);
 		this.titleElement= null;
 		this.titleDoBuild= false;
 	}
 	
 }
