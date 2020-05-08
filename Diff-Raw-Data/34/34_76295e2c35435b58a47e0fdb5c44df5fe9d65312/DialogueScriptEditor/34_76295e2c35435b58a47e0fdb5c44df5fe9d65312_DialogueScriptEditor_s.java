 package com.vgdc.merge.ui.dialogue.editor;
 
 import java.awt.BorderLayout;
 import java.awt.Color;
 import java.awt.Component;
 import java.awt.Dimension;
 import java.awt.FileDialog;
 import java.awt.Font;
 import java.awt.Toolkit;
 import java.awt.event.ActionEvent;
 import java.awt.event.KeyEvent;
 import java.awt.event.MouseAdapter;
 import java.awt.event.MouseEvent;
 import java.awt.event.WindowAdapter;
 import java.awt.event.WindowEvent;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.HashMap;
 
 import javax.swing.AbstractAction;
 import javax.swing.Action;
 import javax.swing.BoxLayout;
 import javax.swing.DefaultListModel;
 import javax.swing.JButton;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JList;
 import javax.swing.JMenu;
 import javax.swing.JMenuBar;
 import javax.swing.JMenuItem;
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 import javax.swing.JPopupMenu;
 import javax.swing.JScrollPane;
 import javax.swing.JSeparator;
 import javax.swing.JSplitPane;
 import javax.swing.KeyStroke;
 import javax.swing.ListCellRenderer;
 import javax.swing.SwingUtilities;
 import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
 
 import com.badlogic.gdx.utils.Json;
 import com.vgdc.merge.ui.dialogue.DialogueScript;
 
 public class DialogueScriptEditor extends JFrame {
 	private static final long serialVersionUID = 1L;
 	public static final Font DEFAULT_FONT = new Font("Consolas", Font.BOLD, 12);
 
 	public static void main(String[] args) {
 		SwingUtilities.invokeLater(new Runnable() {
 			public void run() {
 				new DialogueScriptEditor().createGUI();
 			}
 		});
 	}
 
 	private JList<DialogueCard> list;
 	private CardEditorPanel textEditor;
 	private DefaultListModel<DialogueCard> myListModel = createDefaultListModel();
 	private HashMap<ActionName, Action> actionMap;
 	private JLabel feedback;
 	private boolean isSaved = true;
 
 	/*
 	 * JList Access
 	 */
 
 	public void update() {
 		list.updateUI();
 		isSaved = false;
 	}
 
 	/*
 	 * Editor Access
 	 */
 
 	public void launchTextEditor(final DialogueCard item) {
 		SwingUtilities.invokeLater(new Runnable() {
 			public void run() {
 				new CardEditorFrame(DialogueScriptEditor.this, item);
 			}
 		});
 	}
 
 	public void updateTextEditor() {
 		textEditor.loadCard(list.getSelectedValue());
 	}
 
 	public void loadCardIntoEditor(DialogueCard item) {
 		textEditor.loadCard(item);
 		textEditor.setFocus();
 	}
 
 	/*
 	 * File Writing
 	 */
 
 	private String filePath;
 	private String lastDir;
 	private String lastFile;
 	public static final String SCRIPT_FILE_EXTENSION = ".json";
 
 	public void selectFileLocation(int mode) {
 		FileDialog fileDialog = new FileDialog(new JFrame(), "Save DialogueScript As");
 		if (filePath != null) {
 			fileDialog.setDirectory(lastDir);
 			fileDialog.setFile(lastFile);
 		}
 		fileDialog.setMode(mode);
 		fileDialog.setVisible(true);
 		if (fileDialog.getFile() == null) {
 			return;
 		}
 		else {
 			filePath = fileDialog.getDirectory() + fileDialog.getFile();
 			lastDir = fileDialog.getDirectory();
 			lastFile = fileDialog.getFile();
 			if (mode == FileDialog.SAVE) {
 				if (filePath.length() >= SCRIPT_FILE_EXTENSION.length()) {
 					if (!filePath.substring(filePath.length() - SCRIPT_FILE_EXTENSION.length()).equalsIgnoreCase(
 							SCRIPT_FILE_EXTENSION)) {
 						filePath += SCRIPT_FILE_EXTENSION;
 					}
 				}
 				else {
 					filePath += SCRIPT_FILE_EXTENSION;
 				}
 			}
 		}
 	}
 
 	public boolean saveToFile() {
 		// check savePath chosen
 		if (filePath == null) {
 			output("Save failed: no save path specified.");
 			return false;
 		}
 
 		// create DialogueScript object
 		DialogueScript script = null;
 		try {
 			script = buildScriptObject();
 		} catch (Exception e) {
 			output("Save failed: error building DialogueScript object.");
 			e.printStackTrace();
 			return false;
 		}
 
 		// create output JSON
 		String fileOutput = null;
 		try {
 			Json json = new Json();
 			fileOutput = json.prettyPrint(json.toJson(script));
 		} catch (Exception e) {
 			output("Save failed: error generating output DialogueScript file.");
 			e.printStackTrace();
 			return false;
 		}
 
 		// write to file
 		try {
 			FileWriter writer = new FileWriter(new File(filePath));
 			writer.write(fileOutput);
 			writer.close();
 			output("Saved successfully: " + filePath);
 		} catch (IOException e) {
 			output("Save failed: error writing to file: " + filePath);
 			e.printStackTrace();
 			return false;
 		}
 
 		isSaved = true;
 		return true;
 	}
 
 	public boolean checkAndPromptSave() {
 		if (!isSaved) {
 			int option = JOptionPane.showConfirmDialog(this, "You have unsaved changes. Save changes?",
 					"Dialogue DialogueScript Editor: Save changes", JOptionPane.YES_NO_CANCEL_OPTION);
 			if (option == JOptionPane.YES_OPTION) {
 				if (filePath == null) {
 					selectFileLocation(FileDialog.SAVE);
 				}
 				return saveToFile();
 			}
 			return false;
 		}
 		return true;
 		// else if(option == JOptionPane.CANCEL_OPTION) { don't
 		// close }
 	}
 
 	public void loadFromFile() {
 		checkAndPromptSave();
 		// prompt save
 		selectFileLocation(FileDialog.LOAD);
 
 		// check filePath chosen
 		if (filePath == null) {
 			output("Load failed: no file path specified.");
 			return;
 		}
 
 		// create DialogueScript object
 		DialogueScript script = null;
 		System.out.println(filePath);
 		FileInputStream inputStream = null;
 		try {
 			inputStream = new FileInputStream(filePath);
 			try {
 				script = loadScriptFromJson(inputStream);
 				inputStream.close();
 			} catch (Exception e) {
 				e.printStackTrace();
 				output("Load failed: error loading file: file must be DialogueScript format.");
 			}
 		} catch (FileNotFoundException e) {
 			e.printStackTrace();
 			output("Load failed: file not found.");
 			return;
 		}
 
 		// create cards
 		createCards(script);
 	}
 
 	public void createCards(DialogueScript script) {
 		// clear current cards
 		DefaultListModel<DialogueCard> listModel = (DefaultListModel<DialogueCard>) list.getModel();
 		listModel.removeAllElements();
 
 		for (DialogueScript.Page page : script.pages) {
 			if (page.speaker != null) {
 				DialogueCard nameCard = new DialogueCard(CardType.NAME);
 				nameCard.setText(page.speaker);
 				listModel.addElement(nameCard);
 			}
 
 			if (page.emotions != null) {
 				for (DialogueScript.Emotion emotion : page.emotions) {
 					DialogueCard imageCard = new DialogueCard(CardType.IMAGE);
 					imageCard.setText(emotion.image);
 					listModel.addElement(imageCard);
 
 					if (emotion.lines != null) {
 						for (String line : emotion.lines) {
 							DialogueCard dialogueCard = new DialogueCard(CardType.DIALOGUE);
 							dialogueCard.setText(line);
 							listModel.addElement(dialogueCard);
 						}
 					}
 				}
 			}
 
 		}
 		if (script.event != null) {
 			DialogueCard eventCard = new DialogueCard(CardType.EVENT);
 			eventCard.setText(script.event);
 			listModel.addElement(eventCard);
 		}
 	}
 
 	public DialogueScript buildScriptObject() throws Exception {
 		DialogueScript script = new DialogueScript();
 		ArrayList<DialogueScript.Page> pages = new ArrayList<DialogueScript.Page>();
 		ArrayList<DialogueScript.Emotion> emotions = new ArrayList<DialogueScript.Emotion>();
 		ArrayList<String> lines = new ArrayList<String>();
 		DialogueScript.Page page = null;
 		DialogueScript.Emotion emotion = null;
 		DefaultListModel<DialogueCard> listModel = (DefaultListModel<DialogueCard>) list.getModel();
 		for (int i = 0; i < listModel.size(); i++) {
 			DialogueCard card = listModel.getElementAt(i);
 			if (card.getType() == CardType.NAME) {
 				if (page != null && emotions.size() > 0) {
 					page.emotions = emotions.toArray(new DialogueScript.Emotion[1]);
 				}
 				page = new DialogueScript.Page();
 				page.speaker = card.getText();
 				pages.add(page);
 
 				emotions = new ArrayList<DialogueScript.Emotion>();
 			}
 			else if (card.getType() == CardType.IMAGE) {
 				if (emotion != null && lines.size() > 0) {
 					emotion.lines = lines.toArray(new String[1]);
 				}
 				emotion = new DialogueScript.Emotion();
 				emotion.image = card.getText();
 				emotions.add(emotion);
 
 				lines = new ArrayList<String>();
 			}
 			else if (card.getType() == CardType.EVENT) {
 				script.event = card.getText();
 			}
 			else if (card.getType() == CardType.DIALOGUE) {
 				lines.add(card.getText());
 			}
 		}
 		if (lines.size() > 0) {
 			emotion.lines = lines.toArray(new String[1]);
 		}
 		if (emotions.size() > 0) {
 			page.emotions = emotions.toArray(new DialogueScript.Emotion[1]);
 		}
 		if (pages.size() > 0) {
 			script.pages = pages.toArray(new DialogueScript.Page[1]);
 		}
 
 		return script;
 	}
 
 	public DialogueScript loadScriptFromJson(FileInputStream fileInputStream) throws Exception {
 		Json json = new Json();
 		DialogueScript script = json.fromJson(DialogueScript.class, fileInputStream);
 		return script;
 	}
 
 	/*
 	 * Text feedback
 	 */
 
 	public void output(String message) {
 		feedback.setText(message);
 	}
 
 	/*
 	 * GUI Initialization
 	 */
 
 	public void createGUI() {
 		try {
 			UIManager.setLookAndFeel("com.sun.java.swing.plaf.nimbus.NimbusLookAndFeel");
		} catch (ClassNotFoundException | InstantiationException | IllegalAccessException
				| UnsupportedLookAndFeelException e1) {
 			// fail silently
 		}
 		this.setSize(800, 600);
 		this.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
 
 		initializeActionMap();
 
 		list = new JList<DialogueCard>(myListModel);
 		{
 			list.setCellRenderer(new ListCellRenderer<DialogueCard>() {
 				@Override
 				public Component getListCellRendererComponent(JList<? extends DialogueCard> list, DialogueCard value,
 						int index, boolean isSelected, boolean cellHasFocus) {
 					if (value == textEditor.getActiveCard() && isSelected) {
 						value.setSelectionColor(Color.green);
 					}
 					else if (value == textEditor.getActiveCard()) {
 						value.setSelectionColor(Color.blue);
 					}
 					else if (isSelected) {
 						value.setSelectionColor(Color.red);
 					}
 					else {
 						value.setSelectionColor(Color.black);
 					}
 
 					return value;
 				}
 			});
 
 			list.addMouseListener(new ListReorderListener(this, list));
 			list.addMouseMotionListener(new ListReorderListener(this, list));
 		}
 
 		textEditor = new CardEditorPanel(this);
 
 		this.setLayout(new BorderLayout());
 		JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
 		splitPane.setResizeWeight(0.75f);
 		JScrollPane scrollPane = new JScrollPane(list, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
 				JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
 		splitPane.add(scrollPane);
 		splitPane.add(textEditor);
 		// add(new JSeparator(JSeparator.VERTICAL));
 		add(splitPane, BorderLayout.CENTER);
 		add(createButtonPanel(), BorderLayout.EAST);
 
 		feedback = new JLabel("Welcome.");
 		feedback.setFont(DEFAULT_FONT);
 		add(feedback, BorderLayout.SOUTH);
 
 		this.addWindowListener(new WindowAdapter() {
 			@Override
 			public void windowClosing(WindowEvent e) {
 				int option = JOptionPane.showConfirmDialog(DialogueScriptEditor.this, "Close the editor?",
 						"Dialogue Editor: Close", JOptionPane.YES_NO_CANCEL_OPTION);
 				if (option == JOptionPane.YES_OPTION) {
 					DialogueScriptEditor.this.dispose();
 					System.exit(0);
 				}
 			}
 		});
 
 		this.setJMenuBar(createMenuBar());
 		createListPopupMenu();
 
 		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
 		int x = (screenSize.width / 2) - (getWidth() / 2);
 		int y = (screenSize.height / 2) - (getHeight() / 2);
 		setLocation(x, y);
 		this.setVisible(true);
 	}
 
 	private JMenuBar createMenuBar() {
 		JMenuBar menuBar = new JMenuBar();
 		JMenu menu = null;
 		JMenuItem menuItem = null;
 
 		menu = new JMenu("File");
 		{
 			menuItem = new JMenuItem();
 			menuItem.setAction(actionMap.get(ActionName.SAVE));
 			menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, ActionEvent.CTRL_MASK));
 			menu.add(menuItem);
 
 			menuItem = new JMenuItem();
 			menuItem.setAction(actionMap.get(ActionName.SAVE_AS));
 			menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, ActionEvent.CTRL_MASK | ActionEvent.ALT_MASK));
 			menu.add(menuItem);
 
 			menuItem = new JMenuItem();
 			menuItem.setAction(actionMap.get(ActionName.OPEN));
 			menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, ActionEvent.CTRL_MASK));
 			menu.add(menuItem);
 
 			menuBar.add(menu);
 		}
 
 		menu = new JMenu("Edit");
 		{
 			menuItem = new JMenuItem();
 			menuItem.setAction(actionMap.get(ActionName.ADD_CARD));
 			menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N, ActionEvent.CTRL_MASK));
 			menu.add(menuItem);
 
 			menuItem = new JMenuItem();
 			menuItem.setAction(actionMap.get(ActionName.QUICK_ADD_CARD));
 			menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N, ActionEvent.CTRL_MASK
 					| ActionEvent.SHIFT_MASK));
 			menu.add(menuItem);
 
 			menuItem = new JMenuItem();
 			menuItem.setAction(actionMap.get(ActionName.REMOVE_CARD));
 			menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_D, ActionEvent.CTRL_MASK));
 			menu.add(menuItem);
 
 			menu.addSeparator();
 
 			menuItem = new JMenuItem();
 			menuItem.setAction(actionMap.get(ActionName.EDIT));
 			menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_E, ActionEvent.CTRL_MASK));
 			menu.add(menuItem);
 
 			menuItem = new JMenuItem();
 			menuItem.setAction(actionMap.get(ActionName.EDIT_IN_WINDOW));
 			menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_E, ActionEvent.CTRL_MASK
 					| ActionEvent.SHIFT_MASK));
 			menu.add(menuItem);
 
 			menu.addSeparator();
 
 			menuItem = new JMenuItem();
 			menuItem.setAction(actionMap.get(ActionName.MOVE_UP));
 			menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_UP, ActionEvent.ALT_MASK));
 			menu.add(menuItem);
 
 			menuItem = new JMenuItem();
 			menuItem.setAction(actionMap.get(ActionName.MOVE_DOWN));
 			menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, ActionEvent.ALT_MASK));
 			menu.add(menuItem);
 
 			menuBar.add(menu);
 		}
 
 		return menuBar;
 	}
 
 	private JPanel createButtonPanel() {
 		JPanel buttonPanel = new JPanel();
 		{
 			buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.Y_AXIS));
 
 			// --------------------Add/Delete Cards-------------------------
 			buttonPanel.add(createButton(ActionName.ADD_CARD));
 
 			buttonPanel.add(createButton(ActionName.QUICK_ADD_CARD));
 
 			buttonPanel.add(createButton(ActionName.REMOVE_CARD));
 
 			buttonPanel.add(createSeparator());
 
 			// --------------------Editing-------------------------
 			buttonPanel.add(createButton(ActionName.EDIT));
 
 			buttonPanel.add(createButton(ActionName.EDIT_IN_WINDOW));
 
 			buttonPanel.add(createSeparator());
 
 			// --------------------Reordering-------------------------
 			buttonPanel.add(createButton(ActionName.MOVE_UP));
 
 			buttonPanel.add(createButton(ActionName.MOVE_DOWN));
 
 			buttonPanel.add(createSeparator());
 
 			// -------------------Save/Load---------------------------
 			buttonPanel.add(createButton(ActionName.SAVE));
 
 			buttonPanel.add(createButton(ActionName.SAVE_AS));
 
 			buttonPanel.add(createSeparator());
 
 			buttonPanel.add(createButton(ActionName.OPEN));
 
 			buttonPanel.add(createSeparator());
 
 			return buttonPanel;
 		}
 	}
 
 	public JButton createButton(ActionName actionName) {
 		JButton button = new JButton();
 		button.setFont(DialogueScriptEditor.DEFAULT_FONT);
 		button.setAction(actionMap.get(actionName));
 		button.setMaximumSize(new Dimension(Integer.MAX_VALUE, button.getMaximumSize().height));
 
 		return button;
 	}
 
 	private JSeparator createSeparator() {
 		JSeparator separater = new JSeparator(JSeparator.HORIZONTAL);
 		Dimension prefDim = separater.getPreferredSize();
 		prefDim.width = separater.getMaximumSize().width;
 		separater.setMaximumSize(prefDim);
 		return separater;
 	}
 
 	private DefaultListModel<DialogueCard> createDefaultListModel() {
 		DialogueCard[] listElements = new DialogueCard[] { new DialogueCard(CardType.NAME),
 				new DialogueCard(CardType.IMAGE), new DialogueCard(CardType.DIALOGUE),
 				new DialogueCard(CardType.DIALOGUE) };
 		DefaultListModel<DialogueCard> listModel = new DefaultListModel<DialogueCard>();
 		for (DialogueCard element : listElements) {
 			listModel.addElement(element);
 		}
 		return listModel;
 	}
 
 	/*
 	 * List Popup Menu
 	 */
 
 	private void createListPopupMenu() {
 		JMenuItem menuItem = null;
 
 		JPopupMenu listMenu = new JPopupMenu();
 		for (CardType type : CardType.values()) {
 			menuItem = new JMenuItem("Add " + type.name());
 			menuItem.setAction(createQuickAddAction(type));
 			listMenu.add(menuItem);
 		}
 
 		list.addMouseListener(new ListPopupListener(listMenu));
 	}
 
 	private AbstractAction createQuickAddAction(final CardType type) {
 		return new AbstractAction("Add " + type.name()) {
 			private static final long serialVersionUID = 1L;
 
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				DefaultListModel<DialogueCard> listModel = (DefaultListModel<DialogueCard>) list.getModel();
 				DialogueCard newCard = new DialogueCard(type);
 				lastAddedCardType = type;
 				listModel.addElement(newCard);
 				list.setSelectedValue(newCard, true);
 				updateTextEditor();
 				output("New " + type.name() + " card added.");
 				repaint();
 			}
 		};
 	}
 
 	private class ListPopupListener extends MouseAdapter {
 		JPopupMenu popup;
 
 		ListPopupListener(JPopupMenu popupMenu) {
 			popup = popupMenu;
 		}
 
 		public void mousePressed(MouseEvent e) {
 			maybeShowPopup(e);
 		}
 
 		public void mouseReleased(MouseEvent e) {
 			maybeShowPopup(e);
 		}
 
 		private void maybeShowPopup(MouseEvent e) {
 			if (e.isPopupTrigger()) {
 				popup.show(e.getComponent(), e.getX(), e.getY());
 			}
 		}
 	}
 
 	/*
 	 * UI Actions
 	 */
 
 	public enum ActionName {
 		ADD_CARD, QUICK_ADD_CARD, REMOVE_CARD, EDIT, MOVE_DOWN, MOVE_UP, EDIT_IN_WINDOW, SAVE_AS, SAVE, OPEN;
 	}
 
 	private CardType lastAddedCardType = CardType.DIALOGUE;
 
 	public void initializeActionMap() {
 		actionMap = new HashMap<ActionName, Action>();
 
 		actionMap.put(ActionName.ADD_CARD, new AbstractAction("Add Card") {
 			private static final long serialVersionUID = 1L;
 
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				CardType option = (CardType) JOptionPane.showInputDialog(DialogueScriptEditor.this,
 						"Card type to add:", "Dialogue Editor: Add Card", 1, null, CardType.values(), lastAddedCardType);
 				// selection wasn't cancelled
 				if (option != null) {
 					lastAddedCardType = option;
 					DefaultListModel<DialogueCard> listModel = (DefaultListModel<DialogueCard>) list.getModel();
 					DialogueCard newCard = new DialogueCard(option);
 					if (list.getSelectedIndex() != -1) {
 						listModel.insertElementAt(newCard, list.getSelectedIndex() + 1);
 					}
 					else {
 						listModel.addElement(newCard);
 					}
 					list.setSelectedValue(newCard, true);
 					updateTextEditor();
 					output("New " + option.name() + " card added.");
 					isSaved = false;
 					repaint();
 				}
 			}
 		});
 
 		actionMap.put(ActionName.QUICK_ADD_CARD, new AbstractAction("Quick Add Card") {
 			private static final long serialVersionUID = 1L;
 
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				DefaultListModel<DialogueCard> listModel = (DefaultListModel<DialogueCard>) list.getModel();
 				DialogueCard newCard = new DialogueCard(lastAddedCardType);
 				if (list.getSelectedIndex() != -1) {
 					listModel.insertElementAt(newCard, list.getSelectedIndex() + 1);
 				}
 				else {
 					listModel.addElement(newCard);
 				}
 				list.setSelectedValue(newCard, true);
 				updateTextEditor();
 				output("New " + lastAddedCardType.name() + " card added.");
 				isSaved = false;
 				repaint();
 			}
 		});
 
 		actionMap.put(ActionName.REMOVE_CARD, new AbstractAction("Remove Card") {
 			private static final long serialVersionUID = 1L;
 
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				if (list.getSelectedValue() != null) {
 					if (list.getSelectedValuesList().size() > 1) {
 						int option = JOptionPane.showConfirmDialog(DialogueScriptEditor.this,
 								"You are about to delete multiple cards. Do you really want to delete these cards?",
 								"Dialogue Editor: Delete Card", JOptionPane.YES_NO_CANCEL_OPTION);
 						if (option == JOptionPane.YES_OPTION) {
 							DefaultListModel<DialogueCard> listModel = (DefaultListModel<DialogueCard>) list.getModel();
 							for (DialogueCard card : list.getSelectedValuesList()) {
 								card.remove();
 								listModel.removeElement(card);
 							}
 							output("Cards removed.");
 						}
 					}
 					else {
 						int option = JOptionPane.showConfirmDialog(DialogueScriptEditor.this,
 								"Do you really want to delete this card?", "Dialogue Editor: Delete Card",
 								JOptionPane.YES_NO_CANCEL_OPTION);
 						if (option == JOptionPane.YES_OPTION) {
 							DefaultListModel<DialogueCard> listModel = (DefaultListModel<DialogueCard>) list.getModel();
 							DialogueCard card = listModel.getElementAt(list.getSelectedIndex());
 							card.remove();
 							listModel.removeElementAt(list.getSelectedIndex());
 							output(card.getType().name() + " card removed.");
 						}
 					}
 					updateTextEditor();
 					isSaved = false;
 					repaint();
 				}
 			}
 		});
 
 		actionMap.put(ActionName.EDIT, new AbstractAction("Edit Card") {
 			private static final long serialVersionUID = 1L;
 
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				if (list.getSelectedValue() != null) {
 					updateTextEditor();
 					output("Editing: " + list.getSelectedValue().getType().name() + " card.");
 					repaint();
 				}
 			}
 		});
 
 		actionMap.put(ActionName.EDIT_IN_WINDOW, new AbstractAction("Edit In Window") {
 			private static final long serialVersionUID = 1L;
 
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				if (list.getSelectedValue() != null) {
 					launchTextEditor(list.getSelectedValue());
 					output("Editor Window Launched.");
 					repaint();
 				}
 			}
 		});
 
 		// --------------------Reordering-------------------------
 		actionMap.put(ActionName.MOVE_UP, new AbstractAction("Move Up") {
 			private static final long serialVersionUID = 1L;
 
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				int index = list.getSelectedIndex();
 				if (index > 0) {
 					DefaultListModel<DialogueCard> listModel = (DefaultListModel<DialogueCard>) list.getModel();
 					DialogueCard card = listModel.elementAt(index);
 					listModel.removeElementAt(index);
 					listModel.insertElementAt(card, index - 1);
 					// keep the current card selected for multiple moves
 					list.setSelectedIndex(index - 1);
 					repaint();
 					isSaved = false;
 					output(card.getType().name() + " moved up.");
 				}
 			}
 		});
 
 		actionMap.put(ActionName.MOVE_DOWN, new AbstractAction("Move Down") {
 			private static final long serialVersionUID = 1L;
 
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				int index = list.getSelectedIndex();
 				if (index >= 0) {
 					DefaultListModel<DialogueCard> listModel = (DefaultListModel<DialogueCard>) list.getModel();
 					DialogueCard card = listModel.elementAt(index);
 					if (index < listModel.getSize() - 1) {
 						listModel.removeElementAt(index);
 						listModel.insertElementAt(card, index + 1);
 						// keep the current card selected for multiple
 						// moves
 						list.setSelectedIndex(index + 1);
 						repaint();
 						isSaved = false;
 						output(card.getType().name() + " moved down.");
 					}
 				}
 			}
 		});
 
 		actionMap.put(ActionName.SAVE, new AbstractAction("Save") {
 			private static final long serialVersionUID = 1L;
 
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				if (filePath == null) {
 					selectFileLocation(FileDialog.SAVE);
 				}
 				saveToFile();
 			}
 		});
 
 		actionMap.put(ActionName.SAVE_AS, new AbstractAction("Save As") {
 			private static final long serialVersionUID = 1L;
 
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				selectFileLocation(FileDialog.SAVE);
 				saveToFile();
 			}
 		});
 
 		actionMap.put(ActionName.OPEN, new AbstractAction("Open") {
 			private static final long serialVersionUID = 1L;
 
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				loadFromFile();
 			}
 		});
 	}
 }
