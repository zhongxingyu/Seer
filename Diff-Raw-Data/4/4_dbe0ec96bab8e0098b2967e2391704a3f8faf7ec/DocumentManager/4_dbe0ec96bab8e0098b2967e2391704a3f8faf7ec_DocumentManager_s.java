 package com.phybots.picode.ui.editor;
 
 import java.awt.Color;
 import java.io.IOException;
 import java.util.Iterator;
 import java.util.SortedSet;
 import java.util.TreeSet;
 import java.util.regex.Pattern;
 
 import javax.swing.SwingUtilities;
 import javax.swing.event.DocumentEvent;
 import javax.swing.event.DocumentEvent.EventType;
 import javax.swing.event.DocumentListener;
 import javax.swing.text.AttributeSet;
 import javax.swing.text.BadLocationException;
 import javax.swing.text.DefaultStyledDocument;
 import javax.swing.text.Element;
 import javax.swing.text.SimpleAttributeSet;
 import javax.swing.text.StyleConstants;
 import javax.swing.text.StyleContext;
 import javax.swing.text.StyledDocument;
 import javax.swing.text.StyledEditorKit;
 
 import com.phybots.picode.PicodeMain;
 import com.phybots.picode.api.PoseLibrary;
 import com.phybots.picode.api.Pose;
 import com.phybots.picode.parser.ASTtoHTMLConverter;
 import com.phybots.picode.parser.PdeParser;
 import com.phybots.picode.parser.PdeWalker;
 import com.phybots.picode.ui.editor.Decoration.Type;
 import com.phybots.picode.ui.editor.UndoManager.EditType;
 
 import processing.app.SketchCode;
 import processing.app.SketchException;
 import processing.mode.java.preproc.PdeEmitter;
 import processing.mode.java.preproc.PdePreprocessor;
 import processing.mode.java.preproc.PdeTokenTypes;
 import processing.mode.java.preproc.TokenUtil;
 import antlr.CommonASTWithHiddenTokens;
 import antlr.CommonHiddenStreamToken;
 import antlr.ExtendedCommonASTWithHiddenTokens;
 import antlr.collections.AST;
 
 public class DocumentManager implements DocumentListener {
 
 	static SimpleAttributeSet defaultAttrs;
 	private static SimpleAttributeSet commentAttrs;
 	private static SimpleAttributeSet keywordAttrs;
 	private static SimpleAttributeSet errorAttrs;
 	private static Pattern photoApiPattern;
 
 	private PicodeMain picodeMain;
 	private PicodeEditor picodeEditor;
 
 	private StyleContext sc;
 	StyledDocument doc;
 	private SortedSet<Decoration> decorations;
 	private PdeParser parser;
 	private AST ast;
 
 	private boolean isInlinePhotoEnabled = true;
 	private UndoManager undoManager;
 	private String lastRemovedText;
 
 	static {
 		defaultAttrs = new SimpleAttributeSet();
 		StyleConstants.setFontFamily(defaultAttrs, PicodeEditor.getDefaultFont().getFamily());
 		StyleConstants.setFontSize(defaultAttrs, PicodeEditor.getDefaultFont().getSize());
 
 		commentAttrs = new SimpleAttributeSet(defaultAttrs);
 		commentAttrs.addAttribute(StyleConstants.Foreground,
 				new Color(0xcc, 0x33, 0x00));
 
 		keywordAttrs = new SimpleAttributeSet(defaultAttrs);
 		keywordAttrs.addAttribute(StyleConstants.Foreground,
 				new Color(0x80, 0x00, 0x80));
 		keywordAttrs.addAttribute(StyleConstants.Bold, true);
 
 		errorAttrs = new SimpleAttributeSet(defaultAttrs);
 		errorAttrs.addAttribute(StyleConstants.Background,
 				new Color(0xcc, 0xcc, 0xcc));
 
 		photoApiPattern = Pattern.compile("Picode\\.pose\\(\"(.+?)\"\\)");
 	}
 
 	public DocumentManager(PicodeMain picodeMain, PicodeEditor picodeEditor) {
 		this.picodeMain = picodeMain;
 		this.picodeEditor = picodeEditor;
 		initialize();
 	}
 
 	private void initialize() {
 		sc = new StyleContext();
 		sc.getStyle(StyleContext.DEFAULT_STYLE).addAttributes(defaultAttrs);
 		doc = new DefaultStyledDocument(sc) {
 			private static final long serialVersionUID = 6958151177602959743L;
 
 			@Override
 			public void remove(int offset, int length) throws BadLocationException {
 				lastRemovedText = doc.getText(offset, length);
 				super.remove(offset, length);
 			}
 
 			@Override
 			public void replace(int offset, int length, String text,
                     AttributeSet attrs) throws BadLocationException {
 				lastRemovedText = doc.getText(offset, length);
 				super.replace(offset, length, text, attrs);
 			}
 		};
 		decorations = new TreeSet<Decoration>();
 		parser = picodeMain.getSketch().getParser();
 
 		picodeEditor.setEditorKit(new StyledEditorKit());
 		picodeEditor.setDocument(doc);
 
 		undoManager = new UndoManager(this);
 
 		String codeString = picodeEditor.getCode().getProgram();
 		try {
 			doc.insertString(0, codeString, defaultAttrs);
 		} catch (BadLocationException e) {
 			e.printStackTrace();
 		}
 
 		update();
 	}
 
 	public boolean isInlinePhotoEnabled() {
 		return isInlinePhotoEnabled;
 	}
 
 	public void setInlinePhotoEnabled(boolean isInlinePhotoEnabled) {
 		this.isInlinePhotoEnabled = isInlinePhotoEnabled;
 
 		// Remove existing photo decorations.
 		if (!isInlinePhotoEnabled) {
 			for (Iterator<Decoration> it = decorations.iterator(); it.hasNext();) {
 				Decoration decoration = it.next();
 				if (decoration.getType() == Type.POSE) {
 					setCharacterAttributes(
 							decoration.getOffset(),
 							decoration.getLenth(),
 							defaultAttrs);
 					it.remove();
 				}
 			}
 
 		// Add photo decorations.
 		} else {
 			update();
 		}
 	}
 
 	public void insertUpdate(DocumentEvent e) {
 		onUpdate(e);
 	}
 
 	public void removeUpdate(DocumentEvent e) {
 		onUpdate(e);
 	}
 
 	public void changedUpdate(DocumentEvent e) {
 	}
 
 	public Decoration getDecoration(int index) {
 		for (Decoration decoration : decorations) {
 			if (index >= decoration.getOffset() &&
 					index < decoration.getOffset() + decoration.getLenth()) {
 				return decoration;
 			}
 		}
 		return null;
 	}
 
 	public SortedSet<Decoration> getDecorations() {
 		return new TreeSet<Decoration>(decorations);
 	}
 
 	public String getHTML(String rootPath) {
 		ASTtoHTMLConverter converter = new ASTtoHTMLConverter(parser.getPreprocessor());
 		String name = picodeEditor.getCode().getPrettyName();
 		if (name == null) {
 			name = picodeEditor.getCode().getFileName();
 		}
 		return converter.convert(ast, name, rootPath);
 	}
 
 	public void undo() {
 		if (undoManager.canUndo()) {
 			undoManager.undo();
 		}
 	}
 
 	public boolean canUndo() {
 		return undoManager.canUndo();
 	}
 
 	public void redo() {
 		if (undoManager.canRedo()) {
 			undoManager.redo();
 		}
 	}
 
 	public boolean canRedo() {
 		return undoManager.canRedo();
 	}
 
 	private void setCharacterAttributes(int startIndex, int length, SimpleAttributeSet attrs) {
 		if (length <= 0) {
 			return;
 		}
 		try {
 			String text = doc.getText(startIndex, length);
 			doc.remove(startIndex, length);
 			doc.insertString(startIndex, text, attrs);
 		} catch (BadLocationException e) {
 			e.printStackTrace();
 		}
 	}
 
 	private void onUpdate(final DocumentEvent de) {
 		SwingUtilities.invokeLater(new Runnable() {
 			public void run() {
 				UndoManager.Edit edit = new UndoManager.Edit();
 				edit.offset = de.getOffset();
 				try {
 					if (de.getType() == EventType.INSERT) {
 						edit.type = EditType.INSERT;
 						edit.afterText = doc.getText(
 								de.getOffset(), de.getLength());
 					} else if (de.getType() == EventType.REMOVE) {
 						edit.type = EditType.REMOVE;
 						edit.beforeText = lastRemovedText;
 					}
 				} catch (BadLocationException e) {
 					e.printStackTrace();
 				}
 
 				Decoration existingIcon = null;
 				Element paragraph = doc.getParagraphElement(de.getOffset());
 				for (int i = 0; i < paragraph.getElementCount(); i++) {
 					Element element = paragraph.getElement(i);
 
 					// Check if the input text is in the pending state by IME.
 					AttributeSet attrs = element.getAttributes();
 					if (attrs != null
 							&& attrs.isDefined(StyleConstants.ComposedTextAttribute)) {
 						return;
 					}
 
 					// Check if the text is inserted just before/after the photo.
 					if (de.getType() == EventType.INSERT) {
 						// [inserted][element]
 						if (element.getStartOffset() == de.getOffset() + de.getLength()) {
 							if (element.getAttributes().isDefined(StyleConstants.IconAttribute)) {
 								existingIcon = getDecoration(de.getOffset());
 							}
 						// [element][inserted]
 						} else if (element.getEndOffset() == de.getOffset()) {
 							if (element.getAttributes().isDefined(StyleConstants.IconAttribute)) {
 								existingIcon = getDecoration(element.getStartOffset());
 							}
 						}
 					}
 				}
 
 				// Replace the photo if applicable.
 				if (existingIcon != null) {
 					try {
 						String insertedText = doc.getText(de.getOffset(), de.getLength());
 						if (photoApiPattern.matcher(insertedText).matches()) {
 							edit.type = EditType.REPLACE;
 							edit.offset = existingIcon.getOffset();
 							edit.beforeText = doc.getText(
 									existingIcon.getOffset(),
 									existingIcon.getLenth());
 							doc.removeDocumentListener(DocumentManager.this);
 							// Remove the API call body.
 							doc.remove(existingIcon.getOffset(),
 									existingIcon.getLenth() + de.getLength());
 							// Insert new API call.
 							doc.insertString(
 									existingIcon.getOffset(),
 									insertedText,
 									defaultAttrs);
 							doc.addDocumentListener(DocumentManager.this);
 						}
 					} catch (BadLocationException e) {
 						e.printStackTrace();
 					}
 				}
 				undoManager.registerEdit(edit);
 				picodeMain.getFrame().getBtnUndo().setEnabled(canUndo());
 				picodeMain.getFrame().getBtnRedo().setEnabled(canRedo());
 
 		        // Update code.
 				syncCode();
 				update(de);
 			}
 		});
 	}
 
 	public void update() {
 		update(null);
 	}
 
 	public void syncCode() {
 		String newCode;
 		try {
 			newCode = doc.getText(0, doc.getLength());
 		} catch (BadLocationException e) {
 			e.printStackTrace();
 			return;
 		}
 		picodeEditor.getCode().setProgram(newCode);
 		picodeMain.getSketch().setModified(true);
 	}
 
 	private void update(final DocumentEvent e) {
 		int caretPosition = picodeEditor.getCaretPosition();
 		doc.removeDocumentListener(DocumentManager.this);
 
 		try {
 			if (e != null && e.getType() == EventType.REMOVE) {
 				removeDecoration(e, false);
 			}
 			updateDecoration();
 			picodeMain.getPintegration().statusEmpty();
 		} catch (SketchException se) {
			removeDecoration(e, true);
 			updateErrorDecoration(se);
 			picodeMain.getPintegration().statusError(se);
 		} finally {
 			doc.addDocumentListener(DocumentManager.this);
 			picodeEditor.setCaretPosition(caretPosition);
 		}
 	}
 
 	private void updateErrorDecoration(SketchException se) {
 		int line = se.getCodeLine();
 		if (line >= 0) {
 
 			Iterator<Decoration> it = decorations.iterator();
 			while (it.hasNext()) {
 				Decoration decoration = it.next();
 				if (decoration.getType() == Type.ERROR &&
 						Integer.valueOf(decoration.getOption().toString()) == line) {
 					it.remove();
 				}
 			}
 
 			int startIndex = parser.getIndex(line);
 			if (startIndex < 0) startIndex = 0;
 			int endIndex = doc.getParagraphElement(startIndex).getEndOffset();
 			if (endIndex >= doc.getLength()) endIndex --;
 			int length = endIndex - startIndex;
 			if (length > 0) {
 				decorations.add(new Decoration(startIndex, length, Type.ERROR, line));
 				setCharacterAttributes(startIndex, length, errorAttrs);
 			}
 		}
 	}
 
 	private void updateDecoration() throws SketchException {
 		SketchCode code = picodeEditor.getCode();
 		ast = parser.parse(code);
 		picodeMain.getFrame().setNumberOfLines(code.getLineCount());
 
 		decorations.clear();
 		decorate(ast);
 		int index = 0;
 		SketchException se = null;
 		for (Decoration decoration : decorations) {
 
 			int startIndex = decoration.getOffset();
 			int length = decoration.getLenth();
 			if (startIndex > index) {
 				setCharacterAttributes(index, startIndex - index, defaultAttrs);
 			}
 
 			SimpleAttributeSet attrs = null;
 			switch (decoration.getType()) {
 			case COMMENT:
 				attrs = commentAttrs;
 				break;
 			case KEYWORD:
 				attrs = keywordAttrs;
 				break;
 			case POSE:
 				if (!isInlinePhotoEnabled) {
 					attrs = defaultAttrs;
 					break;
 				}
 				PoseLibrary poseLibrary = PoseLibrary.getInstance();
 				String poseName = decoration.getOption().toString();
 				Pose pose = poseLibrary.get(poseName);
 				// System.out.println("Decorating pose: " + poseName);
 				if (!poseLibrary.contains(poseName)) {
 					try {
 						pose = PoseLibrary.load(poseName);
 					} catch (IOException e) {
 						se = new SketchException(
 								e.getMessage(),
 								picodeMain.getFrame().getCurrentEditorIndex(),
 								parser.getLine(startIndex), parser.getColumn(startIndex));
 					}
 				}
 				if (pose != null) {
 					attrs = pose.getCharacterAttributes();
 				}
 				break;
 			default:
 				break;
 			}
 
 			if (attrs != null) {
 				setCharacterAttributes(startIndex, length, attrs);
 			}
 
 			index = startIndex + length;
 			// System.out.print("Added decoration / ");
 			// System.out.println(decoration);
 		}
 		setCharacterAttributes(index, doc.getLength() - index, defaultAttrs);
 		if (se != null) {
 			throw se;
 		}
 	}
 
 	private void removeDecoration(DocumentEvent e, boolean forceRemoval) {
 		removeDecoration(e.getOffset(), e.getLength(), forceRemoval);
 	}
 
 	private void removeDecoration(int startIndex, int length, boolean forceRemoval) {
 		int esi = startIndex, eei = esi + length;
 		Iterator<Decoration> it = decorations.iterator();
 		Decoration decoration;
 		while (it.hasNext()) {
 			decoration = it.next();
 			if (forceRemoval || decoration.getType() == Type.POSE) {
 				int dsi = decoration.getOffset(), dei = dsi + decoration.getLenth();
 				if ((esi >= dsi && esi < dei) || (eei >= dsi && eei < dei)) {
 					setCharacterAttributes(
 							decoration.getOffset(),
 							decoration.getLenth() - length,
 							defaultAttrs);
 					it.remove();
 				}
 			}
 		}
 	}
 
 	private void decorateKeyword(AST ast) {
 		decorations.add(new Decoration(
 				parser.getIndex(ast),
 				ast.getText().length(),
 				Decoration.Type.KEYWORD));
 	}
 
 	private void decorateKeyword(int index, int length) {
 		decorations.add(new Decoration(
 				index,
 				length,
 				Decoration.Type.KEYWORD));
 	}
 
 	private void decorateComment(CommonHiddenStreamToken tok) {
 		decorations.add(new Decoration(
 				parser.getIndex(tok),
 				tok.getText().length(),
 				Decoration.Type.COMMENT));
 	}
 
 	/**
 	 * Copied from {@link processing.mode.java.preproc.PdeEmitter}
 	 *
 	 * @param ast
 	 * @param sb
 	 */
 	private void decorate(AST ast) {
 		if (ast == null) {
 			return;
 		}
 
 		final AST child1 = ast.getFirstChild();
 		AST child2 = null;
 		AST child3 = null;
 		if (child1 != null) {
 			child2 = child1.getNextSibling();
 			if (child2 != null) {
 				child3 = child2.getNextSibling();
 			}
 		}
 
 		switch (ast.getType()) {
 		case PdeWalker.ROOT_ID:
 			dumpHiddenTokens(parser.getPreprocessor().getInitialHiddenToken());
 			decorateChildren(ast);
 			break;
 
 		case PdeTokenTypes.PACKAGE_DEF:
 		case PdeTokenTypes.IMPORT:
 		case PdeTokenTypes.STATIC_IMPORT:
 		case PdeTokenTypes.LITERAL_return:
 		case PdeTokenTypes.LITERAL_throw:
 			decorateKeyword(ast);
 			dumpHiddenAfter(ast);
 			decorate(child1);
 			break;
 
 		case PdeTokenTypes.CLASS_DEF:
 		case PdeTokenTypes.INTERFACE_DEF:
 			decorate(getChild(ast, PdeTokenTypes.MODIFIERS));
 			decorateKeyword(ast);
 			dumpHiddenBefore(getChild(ast, PdeTokenTypes.IDENT));
 			decorate(getChild(ast, PdeTokenTypes.IDENT));
 			decorate(getChild(ast, PdeTokenTypes.TYPE_PARAMETERS));
 			decorate(getChild(ast, PdeTokenTypes.EXTENDS_CLAUSE));
 			decorate(getChild(ast, PdeTokenTypes.IMPLEMENTS_CLAUSE));
 			decorate(getChild(ast, PdeTokenTypes.OBJBLOCK));
 			break;
 
 		case PdeTokenTypes.EXTENDS_CLAUSE:
 		case PdeTokenTypes.IMPLEMENTS_CLAUSE:
 			if (hasChildren(ast)) {
 				decorateKeyword(ast);
 				dumpHiddenBefore(getBestPrintableNode(ast, false));
 				decorateChildren(ast);
 			}
 			break;
 
 		case PdeTokenTypes.DOT:
 			decorate(child1);
 			dumpHiddenAfter(ast);
 			decorate(child2);
 			break;
 
 		case PdeTokenTypes.METHOD_CALL:
 			if (handlePicodeMethodCall(child1, child2)) {
 				break;
 			}
 		case PdeTokenTypes.MODIFIERS:
 		case PdeTokenTypes.OBJBLOCK:
 		case PdeTokenTypes.CTOR_DEF:
 		// case PdeWalker.METHOD_DEF:
 		case PdeTokenTypes.PARAMETERS:
 		case PdeTokenTypes.PARAMETER_DEF:
 		case PdeTokenTypes.VARIABLE_PARAMETER_DEF:
 		case PdeTokenTypes.VARIABLE_DEF:
 		case PdeTokenTypes.TYPE:
 		case PdeTokenTypes.SLIST:
 		case PdeTokenTypes.ELIST:
 		case PdeTokenTypes.ARRAY_DECLARATOR:
 		case PdeTokenTypes.TYPECAST:
 		case PdeTokenTypes.EXPR:
 		case PdeTokenTypes.ARRAY_INIT:
 		case PdeTokenTypes.FOR_INIT:
 		case PdeTokenTypes.FOR_CONDITION:
 		case PdeTokenTypes.FOR_ITERATOR:
 		case PdeTokenTypes.INSTANCE_INIT:
 		case PdeTokenTypes.INDEX_OP:
 		case PdeTokenTypes.SUPER_CTOR_CALL:
 		case PdeTokenTypes.CTOR_CALL:
 		case PdeTokenTypes.METHOD_DEF:
 		case PdeTokenTypes.LABELED_STAT:
 		case PdeTokenTypes.CASE_GROUP:
 		case PdeTokenTypes.TYPE_ARGUMENTS:
 		case PdeTokenTypes.TYPE_PARAMETERS:
 		case PdeTokenTypes.TYPE_ARGUMENT:
 		case PdeTokenTypes.TYPE_PARAMETER:
 		case PdeTokenTypes.ANNOTATION:
 		case PdeTokenTypes.ANNOTATION_ARRAY_INIT:
 			decorateChildren(ast);
 			break;
 
 		// if we have two children, it's of the form "a=0"
 		// if just one child, it's of the form "=0" (where the
 		// lhs is above this AST).
 		case PdeTokenTypes.ASSIGN:
 			if (child2 != null) {
 				decorate(child1);
 				dumpHiddenAfter(ast);
 				decorate(child2);
 			} else {
 				dumpHiddenAfter(ast);
 				decorate(child1);
 			}
 			break;
 
 		// binary operators:
 		case PdeTokenTypes.PLUS:
 		case PdeTokenTypes.MINUS:
 		case PdeTokenTypes.DIV:
 		case PdeTokenTypes.MOD:
 		case PdeTokenTypes.NOT_EQUAL:
 		case PdeTokenTypes.EQUAL:
 		case PdeTokenTypes.LE:
 		case PdeTokenTypes.GE:
 		case PdeTokenTypes.LOR:
 		case PdeTokenTypes.LAND:
 		case PdeTokenTypes.BOR:
 		case PdeTokenTypes.BXOR:
 		case PdeTokenTypes.BAND:
 		case PdeTokenTypes.SL:
 		case PdeTokenTypes.SR:
 		case PdeTokenTypes.BSR:
 		case PdeTokenTypes.LITERAL_instanceof:
 		case PdeTokenTypes.PLUS_ASSIGN:
 		case PdeTokenTypes.MINUS_ASSIGN:
 		case PdeTokenTypes.STAR_ASSIGN:
 		case PdeTokenTypes.DIV_ASSIGN:
 		case PdeTokenTypes.MOD_ASSIGN:
 		case PdeTokenTypes.SR_ASSIGN:
 		case PdeTokenTypes.BSR_ASSIGN:
 		case PdeTokenTypes.SL_ASSIGN:
 		case PdeTokenTypes.BAND_ASSIGN:
 		case PdeTokenTypes.BXOR_ASSIGN:
 		case PdeTokenTypes.BOR_ASSIGN:
 		case PdeTokenTypes.LT:
 		case PdeTokenTypes.GT:
 			decorateBinaryOperator(ast);
 			break;
 
 		case PdeTokenTypes.LITERAL_for:
 			decorateKeyword(ast);
 			dumpHiddenAfter(ast);
 			if (child1.getType() == PdeTokenTypes.FOR_EACH_CLAUSE) {
 				decorateChildren(child1);
 				decorate(child2);
 			} else {
 				decorateChildren(ast);
 			}
 			break;
 
 		case PdeTokenTypes.POST_INC:
 		case PdeTokenTypes.POST_DEC:
 		case PdeTokenTypes.BNOT:
 		case PdeTokenTypes.LNOT:
 		case PdeTokenTypes.INC:
 		case PdeTokenTypes.DEC:
 		case PdeTokenTypes.UNARY_MINUS:
 		case PdeTokenTypes.UNARY_PLUS:
 		case PdeTokenTypes.WILDCARD_TYPE:
 			decorate(child1);
 			dumpHiddenAfter(ast);
 			break;
 
 		case PdeTokenTypes.LITERAL_new:
 		case PdeTokenTypes.LITERAL_switch:
 		case PdeTokenTypes.LITERAL_case:
 		case PdeTokenTypes.LITERAL_default:
 		case PdeTokenTypes.LITERAL_synchronized:
 		case PdeTokenTypes.LITERAL_assert:
 		case PdeTokenTypes.LITERAL_throws:
 		case PdeTokenTypes.LITERAL_while:
 		case PdeTokenTypes.LITERAL_try:
 		case PdeTokenTypes.LITERAL_catch:
 		case PdeTokenTypes.LITERAL_finally:
 			decorateKeyword(ast);
 			dumpHiddenAfter(ast);
 			decorateChildren(ast);
 			break;
 
 		case PdeTokenTypes.STATIC_INIT:
 			decorateKeyword(ast);
 			dumpHiddenBefore(getBestPrintableNode(ast, false));
 			decorate(child1);
 			break;
 
 		case PdeTokenTypes.NUM_INT:
 		case PdeTokenTypes.CHAR_LITERAL:
 		case PdeTokenTypes.STRING_LITERAL:
 		case PdeTokenTypes.NUM_FLOAT:
 		case PdeTokenTypes.NUM_LONG:
 		case PdeTokenTypes.IDENT:
 		case PdeTokenTypes.WEBCOLOR_LITERAL:
 		case PdeTokenTypes.NUM_DOUBLE:
 			dumpHiddenAfter(ast);
 			break;
 
 		case PdeTokenTypes.LITERAL_private:
 		case PdeTokenTypes.LITERAL_public:
 		case PdeTokenTypes.LITERAL_protected:
 		case PdeTokenTypes.LITERAL_static:
 		case PdeTokenTypes.LITERAL_transient:
 		case PdeTokenTypes.LITERAL_native:
 		case PdeTokenTypes.LITERAL_threadsafe:
 		case PdeTokenTypes.LITERAL_volatile:
 		case PdeTokenTypes.LITERAL_class:
 		case PdeTokenTypes.FINAL:
 		case PdeTokenTypes.ABSTRACT:
 		case PdeTokenTypes.LITERAL_package:
 		case PdeTokenTypes.LITERAL_void:
 		case PdeTokenTypes.LITERAL_boolean:
 		case PdeTokenTypes.LITERAL_byte:
 		case PdeTokenTypes.LITERAL_char:
 		case PdeTokenTypes.LITERAL_short:
 		case PdeTokenTypes.LITERAL_int:
 		case PdeTokenTypes.LITERAL_float:
 		case PdeTokenTypes.LITERAL_long:
 		case PdeTokenTypes.LITERAL_double:
 		case PdeTokenTypes.LITERAL_true:
 		case PdeTokenTypes.LITERAL_false:
 		case PdeTokenTypes.LITERAL_null:
 		case PdeTokenTypes.SEMI:
 		case PdeTokenTypes.LITERAL_this:
 		case PdeTokenTypes.LITERAL_super:
 		case PdeTokenTypes.LITERAL_color:
 			decorateKeyword(ast);
 			dumpHiddenAfter(ast);
 			break;
 
 		case PdeTokenTypes.EMPTY_STAT:
 		case PdeTokenTypes.EMPTY_FIELD:
 			break;
 
 		case PdeTokenTypes.LITERAL_continue:
 		case PdeTokenTypes.LITERAL_break:
 			decorateKeyword(ast);
 			dumpHiddenAfter(ast);
 			if (child1 != null) {// maybe label
 				decorate(child1);
 			}
 			break;
 
 		// yuck: Distinguish between "import x.y.*" and "x = 1 * 3"
 		case PdeTokenTypes.STAR:
 			if (hasChildren(ast)) { // the binary mult. operator
 				decorateBinaryOperator(ast);
 			} else { // the special "*" in import:
 				dumpHiddenAfter(ast);
 			}
 			break;
 
 		case PdeTokenTypes.LITERAL_if:
 			printIfThenElse(ast);
 			break;
 
 		case PdeTokenTypes.LITERAL_do:
 			decorateKeyword(ast);
 			dumpHiddenAfter(ast);
 			decorate(child1); // an SLIST
 			decorateKeyword(ast);
 			dumpHiddenBefore(getBestPrintableNode(child2, false));
 			decorate(child2); // an EXPR
 			break;
 
 		// the dreaded trinary operator
 		case PdeTokenTypes.QUESTION:
 			decorate(child1);
 			dumpHiddenAfter(ast);
 			decorate(child2);
 			decorate(child3);
 			break;
 
 		// allow for stuff like int(43.2).
 		case PdeTokenTypes.CONSTRUCTOR_CAST:
 			dumpHiddenAfter(child1.getFirstChild());
 			decorate(child2);
 			break;
 
 		case PdeTokenTypes.TYPE_LOWER_BOUNDS:
 		case PdeTokenTypes.TYPE_UPPER_BOUNDS:
 			decorateKeyword(ast);
 			dumpHiddenBefore(getBestPrintableNode(ast, false));
 			decorateChildren(ast);
 			break;
 
 		case PdeTokenTypes.ANNOTATION_MEMBER_VALUE_PAIR:
 			decorate(ast.getFirstChild());
 			dumpHiddenBefore(getBestPrintableNode(ast.getFirstChild()
 					.getNextSibling(), false));
 			decorate(ast.getFirstChild().getNextSibling());
 			break;
 
 		default:
 			System.err.println("Unrecognized type:" + ast.getType() + " ("
 					+ TokenUtil.nameOf(ast) + ")");
 			break;
 		}
 	}
 
 	private boolean handlePicodeMethodCall(AST dot, AST elist) {
 
 		if (dot.getType() != PdeTokenTypes.DOT
 				|| elist.getType() != PdeTokenTypes.ELIST) {
 			return false;
 		}
 
 		AST className = dot.getFirstChild();
 		if (!"Picode".equals(className.getText())) {
 			return false;
 		}
 
 		AST methodName = className.getNextSibling();
 		if ("pose".equals(methodName.getText())) {
 			return handlePicodePoseMethodCall(className, elist);
 		}
 		return false;
 	}
 
 	private boolean handlePicodePoseMethodCall(AST className, AST elist) {
 		if (elist.getNumberOfChildren() != 1) {
 			return false;
 		}
 		AST parameterExpression = elist.getFirstChild();
 		if (parameterExpression.getNumberOfChildren() != 1) {
 			return false;
 		}
 		AST poseFileName = parameterExpression.getFirstChild();
 		if (poseFileName.getType() != PdeTokenTypes.STRING_LITERAL) {
 			return false;
 		}
 		String fileName = poseFileName.getText();
 		fileName = fileName.substring(1, fileName.length() - 1);
 		int startIndex = parser.getIndex(className);
 		int length = getPicodeMethodCallLength(className, poseFileName);
 		decorations.add(new Decoration(
 				startIndex,
 				length,
 				Type.POSE,
 				fileName));
 		return true;
 	}
 
 	private int getPicodeMethodCallLength(AST className, AST lastParameter) {
 		int startIndex = parser.getIndex(className);
 		int endIndex = parser.getIndex(lastParameter)
 				+ lastParameter.getText().length();
 		if (lastParameter instanceof antlr.CommonASTWithHiddenTokens) {
 			CommonHiddenStreamToken t = ((antlr.CommonASTWithHiddenTokens) lastParameter)
 					.getHiddenAfter();
 			PdePreprocessor pp = parser.getPreprocessor();
 			for (; t != null; t = pp.getHiddenAfter(t)) {
 				if (")".equals(t.getText())) {
 					endIndex = parser.getIndex(t) + 1;
 					break;
 				}
 			}
 		}
 		return endIndex - startIndex;
 	}
 
 	/**
 	 * Find a child of the given AST that has the given type
 	 *
 	 * @returns a child AST of the given type. If it can't find a child of the
 	 *          given type, return null.
 	 */
 	private AST getChild(final AST ast, final int childType) {
 		AST child = ast.getFirstChild();
 		while (child != null) {
 			if (child.getType() == childType) {
 				// debug.println("getChild: found:" + name(ast));
 				return child;
 			}
 			child = child.getNextSibling();
 		}
 		return null;
 	}
 
 	/**
 	 * Tells whether an AST has any children or not.
 	 *
 	 * @return true iff the AST has at least one child
 	 */
 	private boolean hasChildren(final AST ast) {
 		return (ast.getFirstChild() != null);
 	}
 
 	/**
 	 * Gets the best node in the subtree for printing. This really means the
 	 * next node which could potentially have hiddenBefore data. It's usually
 	 * the first printable leaf, but not always.
 	 *
 	 * @param includeThisNode
 	 *            Should this node be included in the search? If false, only
 	 *            descendants are searched.
 	 *
 	 * @return the first printable leaf node in an AST
 	 */
 	private AST getBestPrintableNode(final AST ast,
 			final boolean includeThisNode) {
 		AST child;
 
 		if (includeThisNode) {
 			child = ast;
 		} else {
 			child = ast.getFirstChild();
 		}
 
 		if (child != null) {
 
 			switch (child.getType()) {
 
 			// the following node types are printing nodes that print before
 			// any children, but then also recurse over children. So they
 			// may have hiddenBefore chains that need to be printed first. Many
 			// statements and all unary expression types qualify. Return these
 			// nodes directly
 			case PdeTokenTypes.CLASS_DEF:
 			case PdeTokenTypes.LITERAL_if:
 			case PdeTokenTypes.LITERAL_new:
 			case PdeTokenTypes.LITERAL_for:
 			case PdeTokenTypes.LITERAL_while:
 			case PdeTokenTypes.LITERAL_do:
 			case PdeTokenTypes.LITERAL_break:
 			case PdeTokenTypes.LITERAL_continue:
 			case PdeTokenTypes.LITERAL_return:
 			case PdeTokenTypes.LITERAL_switch:
 			case PdeTokenTypes.LITERAL_try:
 			case PdeTokenTypes.LITERAL_throw:
 			case PdeTokenTypes.LITERAL_synchronized:
 			case PdeTokenTypes.LITERAL_assert:
 			case PdeTokenTypes.BNOT:
 			case PdeTokenTypes.LNOT:
 			case PdeTokenTypes.INC:
 			case PdeTokenTypes.DEC:
 			case PdeTokenTypes.UNARY_MINUS:
 			case PdeTokenTypes.UNARY_PLUS:
 				return child;
 
 				// Some non-terminal node types (at the moment, I only know of
 				// MODIFIERS, but there may be other such types), can be
 				// leaves in the tree but not have any children. If this is
 				// such a node, move on to the next sibling.
 			case PdeTokenTypes.MODIFIERS:
 				if (child.getFirstChild() == null) {
 					return getBestPrintableNode(child.getNextSibling(), false);
 				}
 				// new jikes doesn't like fallthrough, so just duplicated here:
 				return getBestPrintableNode(child, false);
 
 			default:
 				return getBestPrintableNode(child, false);
 			}
 		}
 
 		return ast;
 	}
 
 	/**
 	 * Dump the list of hidden tokens linked to before the AST node passed in.
 	 * The only time hidden tokens need to be dumped with this function is when
 	 * dealing parts of the tree where automatic tree construction was turned
 	 * off with the ! operator in the grammar file and the nodes were manually
 	 * constructed in such a way that the usual tokens don't have the necessary
 	 * hiddenAfter links.
 	 */
 	private void dumpHiddenBefore(final AST ast) {
 		antlr.CommonHiddenStreamToken child = null, parent = ((CommonASTWithHiddenTokens) ast)
 				.getHiddenBefore();
 		if (parent == null) {
 			return;
 		}
 
 		// traverse back to the head of the list of tokens before this node
 		do {
 			child = parent;
 			parent = child.getHiddenBefore();
 		} while (parent != null);
 
 		// dump that list
 		dumpHiddenTokens(child);
 	}
 
 	/**
 	 * Dump the list of hidden tokens linked to after the AST node passed in.
 	 * Most hidden tokens are dumped from this function.
 	 */
 	private void dumpHiddenAfter(final AST ast) {
 		if (!(ast instanceof antlr.CommonASTWithHiddenTokens)) {
 			return;
 		}
 		dumpHiddenTokens(((antlr.CommonASTWithHiddenTokens) ast)
 				.getHiddenAfter());
 	}
 
 	/**
 	 * Dump the list of hidden tokens linked to from the token passed in.
 	 */
 	private void dumpHiddenTokens(CommonHiddenStreamToken t) {
 		for (; t != null; t = parser.getPreprocessor().getHiddenAfter(t)) {
 			switch (t.getType()) {
 			case PdeTokenTypes.SL_COMMENT:
 			case PdeTokenTypes.ML_COMMENT:
 				decorateComment(t);
 				break;
 			default:
 				break;
 			}
 		}
 	}
 
 	private boolean decorateChildren(AST ast) {
 		boolean ret = false;
 		AST child = ast.getFirstChild();
 		while (child != null) {
 			ret = true;
 			decorate(child);
 			child = child.getNextSibling();
 		}
 		return ret;
 	}
 
 	private void decorateBinaryOperator(AST ast) {
 		decorate(ast.getFirstChild());
 		if (!PdeEmitter.OTHER_COPIED_TOKENS.get(ast.getType())) {
 			dumpHiddenAfter(ast);
 		}
 		decorate(ast.getFirstChild().getNextSibling());
 	}
 
 	private void printIfThenElse(AST literalIf) {
 		decorateKeyword(literalIf);
 		dumpHiddenAfter(literalIf);
 
 		AST condition = literalIf.getFirstChild();
 		decorate(condition); // the "if" condition: an EXPR
 
 		// the "then" clause is either an SLIST or an EXPR
 		AST thenPath = condition.getNextSibling();
 		decorate(thenPath);
 
 		AST lastChild = thenPath;
 		while (lastChild.getNumberOfChildren() > 0) {
 			lastChild = lastChild.getFirstChild();
 			while (lastChild.getNextSibling() != null) {
 				lastChild = lastChild.getNextSibling();
 			}
 		}
 		CommonASTWithHiddenTokens lastTokens = (CommonASTWithHiddenTokens) lastChild;
 
 		// optional "else" clause: an SLIST or an EXPR
 		// what could be simpler?
 		AST elsePath = thenPath.getNextSibling();
 		if (elsePath != null) {
 			AST bestPrintableNode = getBestPrintableNode(elsePath, true);
 			dumpHiddenBefore(bestPrintableNode);
 			CommonASTWithHiddenTokens elseTokens = (CommonASTWithHiddenTokens) elsePath;
 			CommonHiddenStreamToken elseTokenHiddenBefore = elseTokens.getHiddenBefore();
 
 			int elseStartIndex = parser.getIndex(lastTokens);
 			String hiddenAfterString = ((ExtendedCommonASTWithHiddenTokens) lastTokens)
 					.getHiddenAfterString();
 			elseStartIndex += lastTokens.getText().length() + hiddenAfterString.length();
 			
 			int elseEndIndex = parser.getIndex(elseTokens);
 
 			// Look for "else" string in the code.
 			if (elseEndIndex < elseStartIndex) {
 				AST next = literalIf.getNextSibling();
 				if (next == null)
 					elseEndIndex = -1;
 				else
 					elseEndIndex = parser.getIndex(next);
 				if (elseEndIndex < elseStartIndex) {
 					elseEndIndex = doc.getLength();
 				}
 				try {
 					String text = doc.getText(elseStartIndex, elseEndIndex - elseStartIndex);
 					elseEndIndex = elseStartIndex + text.indexOf("else") + "else".length();
 				} catch (BadLocationException e) {
 					elseEndIndex = elseStartIndex;
 				}
 			}
 			decorateKeyword(elseStartIndex, elseEndIndex - elseStartIndex);
 
 			if (elsePath.getType() == PdeTokenTypes.SLIST
 					&& elsePath.getNumberOfChildren() == 0
 					&& elseTokenHiddenBefore == null) {
 				CommonHiddenStreamToken hiddenAfter = elseTokens.getHiddenAfter();
 				if (hiddenAfter != null) {
 					dumpHiddenTokens(hiddenAfter);
 				}
 			} else {
 				decorate(elsePath);
 			}
 		}
 	}
 }
