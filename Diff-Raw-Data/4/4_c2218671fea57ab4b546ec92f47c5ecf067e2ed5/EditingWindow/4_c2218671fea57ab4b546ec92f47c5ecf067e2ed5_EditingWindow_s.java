 package frontend;
 
 import java.awt.Toolkit;
 import java.awt.event.FocusEvent;
 import java.awt.event.FocusListener;
 import java.awt.event.KeyEvent;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.ObjectInputStream;
 import java.io.ObjectOutputStream;
 import java.io.OutputStreamWriter;
 import java.io.Writer;
 import java.util.Scanner;
 
 import javax.swing.JComponent;
 import javax.swing.JFileChooser;
 import javax.swing.KeyStroke;
 
 import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
 import org.fife.ui.rsyntaxtextarea.SyntaxConstants;
 import org.fife.ui.rtextarea.RTextScrollPane;
 
 import edithistory.UndoController;
 
 /**
  * An editing view controller. Accepts a document and manages its editing and
  * undo views as well as saving and loading.
  * 
  * @author samuelainsworth
  * 
  */
 public class EditingWindow {
 
 	private final Bruno parentApp;
 	private final DocumentModel doc;
 	private RSyntaxTextArea textArea;
 	private RTextScrollPane scrollPane;
 	private UndoController undoController;
 
 	public EditingWindow(final Bruno parentApp, final DocumentModel doc)
 			throws IOException, ClassNotFoundException {
 		this.parentApp = parentApp;
 		this.doc = doc;
 
 		// Read contents of file
 		StringBuilder contents = new StringBuilder();
 		if (doc.getFile() != null) {
 			Scanner scanner = null;
 			try {
 				scanner = new Scanner(doc.getFile());
 				while (scanner.hasNextLine()) {
 					contents.append(scanner.nextLine()
 							+ System.getProperty("line.separator"));
 				}
 			} finally {
 				scanner.close();
 			}
 		}
 
 		textArea = new RSyntaxTextArea();
 		textArea.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_JAVA);
 		textArea.setCodeFoldingEnabled(true);
 		textArea.setAntiAliasingEnabled(true);
 		scrollPane = new RTextScrollPane(textArea);
 		scrollPane.setFoldIndicatorEnabled(true);
 		scrollPane.setLineNumbersEnabled(true);
 
 		// Set text in text area
 		textArea.setText(contents.toString());
 
 		// Stay at the top of the document
 		textArea.setCaretPosition(0);
 
 		// Setup undo tree
 		if (doc.getMetadataFile() != null && doc.getMetadataFile().exists()) {
 			// Read from metadata file if it exists
 			ObjectInputStream metadataStream = new ObjectInputStream(
 					new FileInputStream(doc.getMetadataFile()));
 			undoController = (UndoController) metadataStream.readObject();
 			metadataStream.close();
 			undoController.setTextArea(textArea);
 		} else {
 			// Otherwise, start with a blank slate
 			undoController = new UndoController(textArea);
 		}
 		textArea.getDocument().addUndoableEditListener(undoController);
 
 		textArea.getInputMap().put(
 				KeyStroke.getKeyStroke(KeyEvent.VK_Z, Toolkit
 						.getDefaultToolkit().getMenuShortcutKeyMask()),
 				undoController.getUndoAction());
 		/*
 		 * textArea.getInputMap().put( KeyStroke.getKeyStroke(KeyEvent.VK_Z,
 		 * Toolkit .getDefaultToolkit().getMenuShortcutKeyMask() +
 		 * Event.SHIFT_MASK), undoController.getRedoAction());
 		 */
 
 		textArea.addFocusListener(new FocusListener() {
 
 			@Override
 			public void focusLost(FocusEvent e) {
 				try {
					save();
 				} catch (IOException e1) {
 					// TODO Auto-generated catch block
 					e1.printStackTrace();
 				}
 			}
 
 			@Override
 			public void focusGained(FocusEvent e) {
 				// Update the title bar
 				String filename = "Untitled";
 				if (doc.getFile() != null) {
 					filename = doc.getFile().getName();
 				}
 				parentApp.setTitle(filename + " - Bruno");
 			}
 		});
 	}
 
 	/**
 	 * Save the document.
 	 * 
 	 * @throws IOException
 	 */
 	public void save(boolean showEvenIfNewAndEmpty) throws IOException {
 		if (doc.getFile() == null) {
 			// If the file is unsaved and empty, ignore
 			if (textArea.getText().isEmpty() && !showEvenIfNewAndEmpty) {
 				return;
 			}
 
 			final JFileChooser fc = new JFileChooser();
 			fc.setFileFilter(new BrunoFileFilter());
 			if (fc.showSaveDialog(null) == JFileChooser.APPROVE_OPTION) {
 				File file = fc.getSelectedFile();
 				doc.setFile(file);
 			} else {
 				// They chose cancel, don't do anything else
 				return;
 			}
 		}
 
 		Writer writer = new OutputStreamWriter(new FileOutputStream(
 				doc.getFile()));
 		writer.write(textArea.getText());
 		writer.close();
 
 		// File where will we store edit history, etc.
 		ObjectOutputStream metadataWriter = new ObjectOutputStream(
 				new FileOutputStream(doc.getMetadataFile()));
 
 		// Save metadata
 		metadataWriter.writeObject(undoController);
 		metadataWriter.close();
 	}
 
 	public void save() throws IOException {
 		save(false);
 	}
 
 	public boolean requestFocusInWindow() {
 		return textArea.requestFocusInWindow();
 	}
 
 	public DocumentModel getDoc() {
 		return doc;
 	}
 
 	public RSyntaxTextArea getTextArea() {
 		return textArea;
 	}
 
 	public JComponent getView() {
 		return scrollPane;
 	}
 
 	public UndoController getUndoController() {
 		return undoController;
 	}
 
 	public void setSyntaxStyle(String syntaxStyle) {
 		getTextArea().setSyntaxEditingStyle(syntaxStyle);
 		getUndoController().setSyntaxStyle(syntaxStyle);
 	}
 
 }
