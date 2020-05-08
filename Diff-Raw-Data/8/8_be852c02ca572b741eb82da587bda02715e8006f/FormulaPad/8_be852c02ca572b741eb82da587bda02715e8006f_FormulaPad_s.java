 package org.eclipse.iee.sample.formula.pad;
 
 import java.util.Collection;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import org.eclipse.iee.editor.core.pad.MouseEventManager;
 import org.eclipse.iee.editor.core.pad.Pad;
 import org.eclipse.iee.editor.core.utils.console.ConsoleMessageEvent;
 import org.eclipse.iee.editor.core.utils.console.ConsoleMessager;
 import org.eclipse.iee.editor.core.utils.console.IConsoleMessageListener;
 import org.eclipse.iee.sample.formula.FormulaPadManager;
 import org.eclipse.iee.sample.formula.bindings.TextViewerSupport;
 import org.eclipse.iee.sample.formula.pad.hover.HoverShell;
 import org.eclipse.iee.sample.formula.storage.FileStorage;
 import org.eclipse.jface.text.Document;
 import org.eclipse.jface.text.ITextListener;
 import org.eclipse.jface.text.TextEvent;
 import org.eclipse.jface.text.TextViewer;
 import org.eclipse.jface.text.TextViewerUndoManager;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.custom.SashForm;
 import org.eclipse.swt.events.FocusEvent;
 import org.eclipse.swt.events.FocusListener;
 import org.eclipse.swt.events.KeyAdapter;
 import org.eclipse.swt.events.KeyEvent;
 import org.eclipse.swt.events.MouseEvent;
 import org.eclipse.swt.events.MouseListener;
 import org.eclipse.swt.graphics.Color;
 import org.eclipse.swt.graphics.Image;
 import org.eclipse.swt.graphics.Point;
 import org.eclipse.swt.layout.FillLayout;
 import org.eclipse.swt.layout.GridData;
 import org.eclipse.swt.layout.GridLayout;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Label;
 
 import com.thoughtworks.xstream.annotations.XStreamOmitField;
 
 public class FormulaPad extends Pad {
 
 	@XStreamOmitField
 	private Composite fParent;
 	@XStreamOmitField
 	private Composite fInputView;
 	@XStreamOmitField
 	private Composite fResultView;
 
 	@XStreamOmitField
 	private Label fFormulaImageLabel;
 	@XStreamOmitField
 	private Label fLastResultImageLabel;
 
 	@XStreamOmitField
 	private TextViewer fViewer;
 	@XStreamOmitField
 	private TextViewerSupport fViewerSupport;
 	@XStreamOmitField
 	private Document fDocument;
 
 	@XStreamOmitField
 	private HoverShell fHoverShell;
 
 	private boolean fIsInputValid;
 
 	private String fOriginalExpression = "";
 	private String fTranslatingExpression = "";
 	private String fLastValidText = "";
 
 	private boolean fTextChanged;
 
 	public String getOriginalExpression() {
 		return fOriginalExpression;
 	}
 
 	public void setOriginalExression(String expression) {
 		fOriginalExpression = expression;
 	}
 
 	public String getTranslatingExpression() {
 		return fTranslatingExpression;
 	}
 
 	public void setTranslatingExression(String expression) {
 		fTranslatingExpression = expression;
 	}
 
 	private final Color INPUT_VALID_COLOR = new Color(null, 255, 255, 255);
 	private final Color INPUT_INVALID_COLOR = new Color(null, 128, 255, 255);
 
 	private IConsoleMessageListener fConsoleMessageListener = new IConsoleMessageListener() {
 		@Override
 		public void messageReceived(ConsoleMessageEvent e) {
 			System.out.println("Message received:" + e.getMessage());
 			updateLastResult(e.getMessage());
 		}
 
 		@Override
 		public String getRequesterID() {
 			return getContainerID();
 		}
 	};
 
 	public FormulaPad() {
 	}
 
 	public FormulaPad(String containerID) {
 		super();
 	}
 
 	public void toggleInputText() {
 		// OFF
 		fResultView.setVisible(false);
 
 		// ON
 		fDocument.set(fOriginalExpression);
 		fInputView.setVisible(true);
 
 		fParent.pack();
 
 		fViewer.getControl().forceFocus();
 	}
 
 	public void toggleFormulaImage() {
 		// OFF
 		fInputView.setVisible(false);
 
 		// ON
 		fResultView.setVisible(true);
 
 		fParent.pack();
 	}
 
 	public void setInputIsValid() {
 		fIsInputValid = true;
 		fViewer.getControl().setBackground(INPUT_VALID_COLOR);
 	}
 
 	public void setInputIsInvalid() {
 		fIsInputValid = false;
 		fViewer.getControl().setBackground(INPUT_INVALID_COLOR);
 	}
 
 	public void validateInput() {
 		String text = fDocument.get();
 		fOriginalExpression = text;
 
 		if (Translator.isTextValid(text) && FormulaRenderer.isTextValid(text)) {
 			setInputIsValid();
 			fLastValidText = text;
 		} else {
 			setInputIsInvalid();
 		}
 	}
 
 	public void processInput() {
 		if (fIsInputValid) {
 			if (!fDocument.get().equals(fTranslatingExpression)) {
 				/* Remove result images from following pads */
 				Collection<Pad> following = FormulaPadManager
 						.getFollowingPads(this);
 
 				for (Pad pad : following) {
 					((FormulaPad) pad).updateLastResult("");
 				}
 			}
 			fTranslatingExpression = fDocument.get();
 		}
 
 		/* Set formula image */
 		Image image = FormulaRenderer.getFormulaImage(fTranslatingExpression);
 		fFormulaImageLabel.setImage(image);
 
 		/* Generate code */
 		String generated = Translator.translateElement(fTranslatingExpression);
 
 		/* Add result output */
 		if (fTranslatingExpression.charAt(fTranslatingExpression.length() - 1) == '=')
 			generated += generateOutputCode(fTranslatingExpression);
 		getContainer().setTextContent(generated);
 	}
 
 	public String generateOutputCode(String expresion) {
 		Pattern p = Pattern.compile("\\s*\\[?\\w+\\]?\\s*=.+");
 		Matcher m = p.matcher(expresion);
 		if (m.matches()) {
 			String variable = expresion.substring(0, expresion.indexOf('='));
 			variable = variable.trim();
 			if (variable.charAt(0) != '[') {
 				return "System.out.println(\"" + getContainerID() + "\" + "
 						+ variable + ");";
 			} else {
 				variable = variable.substring(1, variable.length() - 1);
 				String output = "";
 
 				// TODO: extend Matrix class or change i, j and matrix
 
 				output += "int i=0, j=0; String matrix = \"{\";";
 
 				output += "for(i = 0; i < " + variable
 						+ ".getRowDimension(); i++){" + "matrix += \"{\";"
 						+ "for(j = 0; j < " + variable
 						+ ".getColumnDimension(); j++)";
 				output += "{";
 				output += "matrix += " + variable + ".get(i,j);";
 				output += "if (j !=" + variable + ".getColumnDimension() - 1)";
 				output += "matrix += \",\";";
 				output += "else matrix += \"}\";";
 				output += "}";
 				output += "if (i !=" + variable + ".getRowDimension() - 1)";
 				output += "matrix += \",\";";
 				output += "}";
 
 				output += "matrix += \"}\";";
 
 				output += "System.out.print(\"" + getContainerID() + "\" + "
 						+ "matrix);";
 
 				return output;
 
 			}
 		} else {
 			return "";
 		}
 	}
 
 	public void updateLastResult(String result) {
 		if (result == "") {
 			fLastResultImageLabel.setImage(null);
 			fParent.pack();
 			return;
 		}
 
 		Image image = FormulaRenderer.getFormulaImage(result);
 		fLastResultImageLabel.setImage(image);
 		fParent.pack();
 	}
 
 	public void setListeners() {
 		
 		ConsoleMessager.getInstance().addConsoleMessageListener(
 				fConsoleMessageListener);
 
 		fFormulaImageLabel.addMouseListener(new MouseListener() {
 			@Override
 			public void mouseDoubleClick(MouseEvent e) {
 				moveCaretToCurrentPad();
 				toggleInputText();
 			}
 
 			@Override
 			public void mouseDown(MouseEvent e) {
 				moveCaretToCurrentPad();
 			}
 
 			@Override
 			public void mouseUp(MouseEvent e) {
 			}
 		});
 
 		fLastResultImageLabel.addMouseListener(new MouseListener() {
 			@Override
 			public void mouseDoubleClick(MouseEvent e) {
 				moveCaretToCurrentPad();
 				toggleInputText();
 			}
 
 			@Override
 			public void mouseDown(MouseEvent e) {
 				moveCaretToCurrentPad();
 			}
 
 			@Override
 			public void mouseUp(MouseEvent e) {
 			}
 		});
 		
 		fViewer.getControl().addFocusListener(new FocusListener() {
 
 			@Override
 			public void focusLost(FocusEvent e) {
 				if (fTranslatingExpression != "")
 					toggleFormulaImage();
 				if (fHoverShell != null)
 					fHoverShell.dispose();
 			}
 
 			@Override
 			public void focusGained(FocusEvent e) {
 			}
 		});
 
 		fViewer.addTextListener(new ITextListener() {
 
 			@Override
 			public void textChanged(TextEvent event) {
 				if (fTextChanged) {
 					if (fDocument.get() != "") {
 						fTextChanged = true;
 
 						validateInput();
 						Image image = FormulaRenderer.getFormulaImage(fDocument
 								.get());
 						if (image == null)
 							image = FormulaRenderer
 									.getFormulaImage(fLastValidText);
 						if (fHoverShell != null)
 							fHoverShell.dispose();
 						fHoverShell = new HoverShell(fParent, image);
 
 						/* Resize fInputText */
 						Point size = fViewer.getControl().computeSize(
 								SWT.DEFAULT, SWT.DEFAULT, false);
 						fViewer.getControl().setSize(size);
 						fParent.pack();
 					}
 				}
 				else
 					fTextChanged = true;
 			}
 		});
 
 		fViewer.getControl().addKeyListener(new KeyAdapter() {
 			public void keyPressed(KeyEvent e) {
 				switch (e.keyCode) {
 				case SWT.CR:
 					e.doit = false;
 					processInput();
 					moveCaretToCurrentPad();
 
 					if (fTranslatingExpression != "")
 						toggleFormulaImage();
 
 					if (fHoverShell != null)
 						fHoverShell.dispose();
 
 					break;
 
 				case SWT.ESC:
 					moveCaretToCurrentPad();
 
 					if (fTranslatingExpression != "")
 						toggleFormulaImage();
 
 					if (fHoverShell != null)
 						fHoverShell.dispose();
 
 					break;
 				}
 			}
 		});
 
 	}
 
 	@Override
 	public void createPartControl(final Composite parent) {
 		fParent = parent;
 		
 		FillLayout layout = new FillLayout(SWT.HORIZONTAL);
 		parent.setLayout(layout);
 
 		SashForm sashForm = new SashForm(parent, SWT.FILL);
 		sashForm.setLayout(new FillLayout(SWT.HORIZONTAL));
 
 		/* Input View */
 
 		fInputView = new Composite(sashForm, SWT.NONE);
 		fInputView.setBackground(new Color(null, 255, 255, 255));
 		fInputView.setLayout(new GridLayout(1, true));
 
 		fViewer = new TextViewer(fInputView, SWT.SINGLE);
 		fViewer.getControl().setSize(50, 100);
 		fDocument = new Document();
 		if (fTranslatingExpression.isEmpty())
 			fDocument.set(fOriginalExpression);
 		else
 			fDocument.set(fTranslatingExpression);
 		fViewer.setDocument(fDocument);
 		fTextChanged = false;
 
 		TextViewerUndoManager defaultUndoManager = new TextViewerUndoManager(25);
 		fViewer.setUndoManager(defaultUndoManager);
 		defaultUndoManager.connect(fViewer);
 
 		fViewerSupport = new TextViewerSupport(fViewer);
 
 		/* Result View */
 
 		fResultView = new Composite(sashForm, SWT.NONE);
 		fResultView.setBackground(new Color(null, 255, 255, 255));
 		fResultView.setLayout(new GridLayout(2, false));
 		fResultView.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
 
 		fFormulaImageLabel = new Label(fResultView, SWT.NONE);
 		GridData formulaImageGridData = new GridData(SWT.FILL, SWT.FILL, true,
 				true);
 		fFormulaImageLabel.setLayoutData(formulaImageGridData);
 
 		fLastResultImageLabel = new Label(fResultView, SWT.NONE);
 		GridData lastResultImageGridData = new GridData(SWT.LEFT, SWT.FILL,
 				true, true);
 		fLastResultImageLabel.setLayoutData(lastResultImageGridData);
 
 		setListeners();
 
 		if (fTranslatingExpression != "" && fDocument.get() != "") {
 			validateInput();
 			processInput();
 			toggleFormulaImage();
 		} else {
 			toggleInputText();
 		}
 
 	}
 
 	@Override
 	public void activate() {
 		toggleInputText();
 	}
 
 	@Override
 	public Pad copy() {
 		FormulaPad newPad = new FormulaPad();
 		newPad.fTranslatingExpression = this.fTranslatingExpression;
 		newPad.fOriginalExpression = this.fOriginalExpression;
 		newPad.fIsInputValid = this.fIsInputValid;
 		return newPad;
 	}
 
 	// Save&Load operations, use it for serialization
 
 	public void save() {
 		System.out.println("Saving...");
 		FileStorage.getInstance().saveToFile(this);
 	}
 
 	@Override
 	public void unsave() {
 		System.out.println("Unsaving...");
 		FileStorage.getInstance().removeFile(getContainerID());
 	}
 
 	@Override
 	public void onContainerAttached() {
 	}
 
 	@Override
 	public String getType() {
 		return "Formula";
 	}
 	
 	@Override
 	public void addMouseListeners(Composite control) {
 	}
 }
