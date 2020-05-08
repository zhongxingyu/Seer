 package jIDE;
 
 import jason.asSyntax.Plan;
 import jason.asSyntax.parser.SimpleCharStream;
 import jason.asSyntax.parser.Token;
 import jason.asSyntax.parser.TokenMgrError;
 import jason.asSyntax.parser.as2j;
 import jason.asSyntax.parser.as2jTokenManager;
 
 import java.awt.Color;
 import java.awt.Font;
 import java.io.StringReader;
 
 import javax.swing.JTextPane;
 import javax.swing.event.DocumentEvent;
 import javax.swing.event.DocumentListener;
 import javax.swing.text.Element;
 import javax.swing.text.SimpleAttributeSet;
 import javax.swing.text.Style;
 import javax.swing.text.StyleConstants;
 import javax.swing.text.StyleContext;
 import javax.swing.text.StyledDocument;
 
 public class ASSyntaxHighLight { // extends Thread { //implements CaretListener {
 	//int offset = 0;
 	as2jTokenManager tm = new as2jTokenManager(new SimpleCharStream(new StringReader("")));
 	//Object refreshMonitor = new Object();
 	Style commentStyle, planLabelStyle, currentVarStyle, internalActionStyle, specialAnnot, errorStyle, noErrorStyle;
 	JTextPane editor;
 	ASStyles context;
 	ASDocListener docListener;
 	boolean running = true;
 	
 	JasonID jasonID;
 
 	//Token varToken;
 	//boolean paintOnlyCurVar = false;
 	
 	public ASSyntaxHighLight(JTextPane p, JasonID jasonID) {
 		//super("SyntaxColoring");
 		editor = p;
 		this.jasonID = jasonID;
 		
 		context = new ASStyles();
 		
 		// create some styles
 		commentStyle = context.addStyle(null, context.getStyle(ASStyles.DEFAULT_STYLE));
 		StyleConstants.setForeground(commentStyle, new Color(102, 153, 153));
 		StyleConstants.setItalic(commentStyle, true);
 		
 		planLabelStyle = context.addStyle(null, context.tokenStyles[as2j.ATOM]);
 		StyleConstants.setBold(planLabelStyle, true);
 		StyleConstants.setUnderline(planLabelStyle, true);
 		
 		currentVarStyle = context.addStyle(null, context.tokenStyles[as2j.VAR]);
 		StyleConstants.setBackground(currentVarStyle, Color.YELLOW);
 
 		internalActionStyle = context.addStyle(null, context.tokenStyles[as2j.ATOM]);
 		StyleConstants.setItalic(internalActionStyle, true);
 		
 		specialAnnot = context.addStyle(null, context.tokenStyles[as2j.ATOM]);
 		StyleConstants.setForeground(specialAnnot, Color.red);
 
 		errorStyle = context.addStyle(null, context.getStyle(ASStyles.DEFAULT_STYLE));
 		StyleConstants.setUnderline(errorStyle, true);
 
 		noErrorStyle = context.addStyle(null, context.getStyle(ASStyles.DEFAULT_STYLE));
 		StyleConstants.setUnderline(noErrorStyle, false);
 				
 		addDocListener();
 		//editor.addCaretListener(this);
 	}
 	
 	public void addDocListener() {
 		docListener = new ASDocListener();
 		editor.getDocument().addDocumentListener(docListener);		
 	}
 
 	public Font setFont(String font, int size) {
 		Font f = context.setFont(font, size);		
 		editor.setFont(context.getFont(context.tokenStyles[0]));
 		repainAll();
 		return f;
 	}
 	
 	public void repainAll() {
 		int offset = 0;
 		while (offset < editor.getDocument().getLength()) {
 			//refresh(offset);
 			offset = paintLine(offset);
 		}
 	}
 	
 	/*
 	public void refresh(int offset) {
 		//synchronized (refreshMonitor) {
 		this.offset = offset;
 		//	refreshMonitor.notifyAll();
 		//}
 		paintLine();
 	}
 	*/
 	
 	/*
 	public void stopRun() {
 		running = false;
 		refresh(0); // to wakeup the thread
 	}
 	*/
 	
 	/*
 	public void run() {
 		synchronized (refreshMonitor) {
 			while (running) {
 				try {
 					refreshMonitor.wait();
 					if (running) {
 						paintLine();
 					}
 				} catch (Exception ex) {
 					ex.printStackTrace();
 				}
 			}
 		}
 	}
 	*/
 
 	ASParserThread getParserThread() {
 		if (jasonID != null && jasonID.fASParser != null) {
 			return jasonID.fASParser;
 		} else {
 			return null;
 		}
 	}
 
 	/** use last change as offset */
 	int paintLine() {
 		return paintLine(docListener.lastChange);
 	}
 	
 	int paintLine(int offset) {
 		try {
 			StyledDocument sd = (StyledDocument) editor.getDocument();
 			Element ePar = sd.getParagraphElement(offset);
 			int eIni = ePar.getStartOffset();
 			int eEnd = ePar.getEndOffset();
 			String sPar = sd.getText(eIni, eEnd- eIni);
 			sd.setParagraphAttributes(eIni, eEnd-eIni+1, context.getStyle(StyleContext.DEFAULT_STYLE), false);
 //			System.out.println("$"+sPar+"$ line="+getParLineNumber(ePar)+" error line="+jasonID.fASParser.getErrorLine());
 			
 			if (sPar.trim().startsWith("//")) {
 				sd.setCharacterAttributes(eIni, eEnd-eIni-1, commentStyle, true);					
 			} else {
 				
 				// cursor line (check for error in this line)
 				if (getParserThread() != null) {
 					
 					// identify the current line of ePar
 					int curLine = getParLineNumber(ePar);
 
 					if (getParserThread().getErrorLine() == curLine) { // has an error?
 						sd.setCharacterAttributes(eIni, eEnd-eIni, errorStyle, true);
 						// paint previous line with no error
 						Element p = getPreviousLine(ePar);
						sd.setCharacterAttributes(p.getStartOffset(), p.getEndOffset()-p.getStartOffset(), noErrorStyle, true);								
 					} else {
 						sd.setCharacterAttributes(eIni, eEnd-eIni, noErrorStyle, true);								
 					}
 				}
 
 				changeAttributesBasedOnTokens(sd, eIni, eEnd, sPar);
 			}
 			
 			offset = eEnd;
 		} catch (Exception ex) {
 			ex.printStackTrace();
 		}
 		return offset;
 	}
 
 	// will be overriden by MAS2JSyntax
 	void changeAttributesBasedOnTokens(StyledDocument sd, int eIni, int eEnd, String sPar) {
 		try {
 			tm.ReInit(new SimpleCharStream(new StringReader(sPar)));
 			
 			Token lastToken = null;
 			Token t = tm.getNextToken();
 			while (t.kind != as2j.EOF) {
 				Style s = context.tokenStyles[t.kind];
 				// set plan label style
 				if (lastToken != null && lastToken.kind == as2j.TK_LABEL_AT) {
 					s = planLabelStyle;
 				// set internal action
 				} else if (t.kind == as2j.ATOM && t.image.startsWith(".")) {
 					s = internalActionStyle;
 				// special annots
 				} else if (t.kind == as2j.ATOM && (t.image.equals(Plan.TBreakPoint.getFunctor()) || t.image.equals(Plan.TAtomic.getFunctor()))) {
 					s = specialAnnot;
 				}
 				sd.setCharacterAttributes(eIni+t.beginColumn-1, t.endColumn-t.beginColumn+1, s, false);
 
 				// set currenttoken
 				// TODO: show all var ocoorences
 				/*
 				if (t.kind == as2j.VAR && editor.getCaretPosition() >= t.beginColumn+eIni && editor.getCaretPosition() <= t.endColumn+eIni) {
 					varToken = t;
 					System.out.println("Var="+varToken);
 					paintOnlyCurVar = true;
 					repainAll();
 					paintOnlyCurVar = false;
 					//return;
 				}
 				*/
 
 				lastToken = t;
 				t = tm.getNextToken();
 			}
 
 			// verify the end of line comments
 			if (lastToken != null && lastToken.endColumn+eIni+1 < eEnd) {
 				sPar = sd.getText(lastToken.endColumn+eIni, eEnd-(lastToken.endColumn+eIni));
 				if (sPar.trim().startsWith("//")) {
 					sd.setCharacterAttributes(lastToken.endColumn+eIni, eEnd-(lastToken.endColumn+eIni), commentStyle, true);					
 				}
 			}
 		} catch (TokenMgrError e) {
 		} catch (javax.swing.text.BadLocationException ex) {
 		}
 	}
 
 	/*
 	public void caretUpdate(CaretEvent e) {
         //Get the location in the text
         refresh(e.getDot());
 		//paintLine();
         //int mark = e.getMark();
     }
     */
 
 	protected int getParLineNumber(Element ePar) {
 		Element parent = ePar.getParentElement();
 		for (int curLine=0; curLine<parent.getElementCount(); curLine++) {
 			if (parent.getElement(curLine) == ePar) {
 				return curLine+1;
 			}
 		}
 		return -1;
 	}
 	protected Element getPreviousLine(Element ePar) {
 		Element parent = ePar.getParentElement();
 		Element prev = null;
 		for (int curLine=0; curLine<parent.getElementCount(); curLine++) {
 			if (parent.getElement(curLine) == ePar) {
 				return prev;
 			}
 			prev = parent.getElement(curLine);
 		}
 		return prev;
 	}
 	
 	
 	class ASDocListener implements DocumentListener {
 		int lastChange;
 		public void changedUpdate(DocumentEvent arg0) {
 			//updateSyntax(arg0);
 		}
 		public void insertUpdate(DocumentEvent arg0) {
 			updateSyntax(arg0);
 		}
 		public void removeUpdate(DocumentEvent arg0) {
 			updateSyntax(arg0);
 		}
 		void updateSyntax(DocumentEvent e) {
 			lastChange = e.getOffset(); 
 			//refresh(lastChange); // cause deadlock!
 		}
 	}
 
 	public class ASStyles extends StyleContext { 
 		Style[] tokenStyles;
 		/**
 		 * Constructs a set of styles to represent java lexical tokens. By default
 		 * there are no colors or fonts specified.
 		 */
 		public ASStyles() {
 			super();
 			Style root = getStyle(DEFAULT_STYLE);
 			StyleConstants.setLeftIndent(root, ASEditorPane.NUMBERS_WIDTH);
 
 			tokenStyles = new Style[as2j.tokenImage.length];
 			for (int i = 0; i < tokenStyles.length; i++) {
 				Style s = addStyle(null,root); // all styles are children of root (!)
 				tokenStyles[i] = s; // store the style ref
 			}
 			
 			SimpleAttributeSet style;
 			
 			// atoms
 			style = new SimpleAttributeSet();
 			//StyleConstants.setFontFamily(style, "Monospaced");
 			//StyleConstants.setFontSize(style, 12);
 			//StyleConstants.setBackground(style, Color.white);
 			StyleConstants.setForeground(style, Color.darkGray);
 			//StyleConstants.setBold(style, false);
 			//StyleConstants.setItalic(style, false);
 			tokenStyles[as2j.ATOM].addAttributes(style);
 			
 			// strings
 			style = new SimpleAttributeSet();
 			StyleConstants.setForeground(style, Color.gray);
 			tokenStyles[as2j.STRING].addAttributes(style);
 
 			// vars
 			style = new SimpleAttributeSet();
 			float chsb[] = Color.RGBtoHSB(120,20,120,null);
 			StyleConstants.setForeground(style, Color.getHSBColor(chsb[0],chsb[1],chsb[2]));
 			StyleConstants.setBold(style, true);
 			tokenStyles[as2j.VAR].addAttributes(style);
 
 			// true false
 			style = new SimpleAttributeSet();
 			StyleConstants.setForeground(style, Color.black);
 			StyleConstants.setItalic(style, true);
 			StyleConstants.setBold(style, true);
 			tokenStyles[as2j.TK_TRUE].addAttributes(style);
 			// false
 			tokenStyles[as2j.TK_FALSE].addAttributes(style);
 
 			// @ TODO: add a token id for it in as2j.jcc
 			style = new SimpleAttributeSet();
 			StyleConstants.setForeground(style, Color.orange);
 			tokenStyles[as2j.TK_LABEL_AT].addAttributes(style);
 			
 			// . : <- ;
 			style = new SimpleAttributeSet();
 			StyleConstants.setForeground(style, Color.black);
 			StyleConstants.setBold(style, true);
 			tokenStyles[21].addAttributes(style);
 			tokenStyles[23].addAttributes(style);
 			tokenStyles[24].addAttributes(style);
 			tokenStyles[39].addAttributes(style);
 
 			// + -
 			style = new SimpleAttributeSet();
 			StyleConstants.setForeground(style, Color.red);
 			StyleConstants.setBold(style, true);
 			tokenStyles[25].addAttributes(style);
 			tokenStyles[26].addAttributes(style);
 
 			// not ~ &
 			style = new SimpleAttributeSet();
 			StyleConstants.setForeground(style, Color.green);
 			StyleConstants.setBold(style, true);
 			tokenStyles[as2j.TK_NOT].addAttributes(style);
 			tokenStyles[as2j.TK_NEG].addAttributes(style);
 			tokenStyles[29].addAttributes(style);
 
 			// ! ?
 			style = new SimpleAttributeSet();
 			StyleConstants.setForeground(style, Color.blue);
 			StyleConstants.setBold(style, true);
 			tokenStyles[27].addAttributes(style);
 			tokenStyles[28].addAttributes(style);
 
 		}
 
 		public Font setFont(String font, int size) {
 			Style root = getStyle(DEFAULT_STYLE);
 			StyleConstants.setFontFamily(root, font);
 			StyleConstants.setFontSize(root, size);
 			return getFont(root);
 			//tokenColors = null;
 			//tokenFonts = null;
 		}
 	}	
 }	
