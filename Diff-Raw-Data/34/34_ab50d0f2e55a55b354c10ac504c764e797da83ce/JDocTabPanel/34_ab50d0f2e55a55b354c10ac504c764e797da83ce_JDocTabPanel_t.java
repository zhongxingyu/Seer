 package edu.cs319.client.customcomponents;
 
 import java.awt.BorderLayout;
 import java.awt.Dimension;
 import java.awt.Font;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.KeyListener;
 import java.awt.event.MouseAdapter;
 import java.awt.event.MouseEvent;
 import java.awt.event.MouseListener;
 import java.io.File;
 import java.io.IOException;
 
 import javax.imageio.ImageIO;
 import javax.swing.ImageIcon;
 import javax.swing.JButton;
 import javax.swing.JDialog;
 import javax.swing.JEditorPane;
 import javax.swing.JLabel;
 import javax.swing.JList;
 import javax.swing.JMenuItem;
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 import javax.swing.JPopupMenu;
 import javax.swing.JScrollPane;
 import javax.swing.JSplitPane;
 import javax.swing.JTextArea;
 import javax.swing.Timer;
 import javax.swing.border.EmptyBorder;
 import javax.swing.event.ListSelectionEvent;
 import javax.swing.event.ListSelectionListener;
 import javax.swing.text.PlainDocument;
 
 import edu.cs319.dataobjects.DocumentInfo;
 import edu.cs319.dataobjects.DocumentSubSection;
 import edu.cs319.dataobjects.SectionizedDocument;
 import edu.cs319.dataobjects.impl.DocumentSubSectionImpl;
 import edu.cs319.dataobjects.impl.SectionizedDocumentImpl;
 import edu.cs319.util.Util;
 
 /**
  * 
  * @author Amelia Gee
  * @author Wayne Wrowweeclifffe
  * @author Justin Nelson
  * 
  */
 public class JDocTabPanel extends JPanel {
 
 	// Number of milliseconds between automatic updates
 	private final static int UPDATE_NUM_MS = 500;
 
 	private JPanel sectionPanel;
 	private JSplitPane wholePane;
 	private JSplitPane workspace;
 	private DocumentDisplayPane documentPane;
 	private JEditorPane workPane;
 	private JButton sectionUpButton;
 	private JButton sectionDownButton;
 	private JButton aquireLock;
 	private JButton updateSection;
 	private JButton addSubSection;
 	private JButton unlockSubSection;
 
 	private SubSectionList doc;
 	final private DocumentInfo info;
 
 	public JDocTabPanel(DocumentInfo info) {
 		this.info = info;
 		setName(info.getDocumentName());
 		doc = new SubSectionList(new SectionizedDocumentImpl(info.getDocumentName()));
 		Font docFont = new Font("Courier New", Font.PLAIN, 11);
 		documentPane = new DocumentDisplayPane();
 		documentPane.setEditable(false);
 		documentPane.setFont(docFont);
 		documentPane.setLineWrap(false);
 		documentPane.setTabSize(4);
 
 		workPane = new JEditorPane();
 		workPane.setFont(docFont);
 		workPane.addMouseListener(new RightClickListener());
 		PlainDocument doc2 = (PlainDocument) workPane.getDocument();
 		doc2.putProperty(PlainDocument.tabSizeAttribute, 4);
 		try {
 			sectionUpButton = new JButton(new ImageIcon(ImageIO.read(new File(
 					"images/green_up_arrow_small.png"))));
 			sectionDownButton = new JButton(new ImageIcon(ImageIO.read(new File(
 					"images/green_down_arrow_small.png"))));
 		} catch (IOException e) {
 			e.printStackTrace();
 			sectionUpButton = new JButton("^");
 			sectionDownButton = new JButton("V");
 		}
 		aquireLock = new JButton("Aquire Lock");
 		updateSection = new JButton("Update");
 		addSubSection = new JButton("New SubSection");
 		unlockSubSection = new JButton("Unlock");
 		setUpAppearance();
 		setUpListeners();
 
 		Timer timer = new Timer(UPDATE_NUM_MS, new UpdateSubSectionListener());
 		timer.start();
 	}
 
 	private void setUpAppearance() {
 		setLayout(new BorderLayout());
 		setBorder(new EmptyBorder(10, 10, 10, 10));
 
 		JPanel buttonPanel = new JPanel(new BorderLayout(5, 5));
 		buttonPanel.add(sectionUpButton, BorderLayout.NORTH);
 		buttonPanel.add(sectionDownButton, BorderLayout.SOUTH);
 		sectionPanel = new JPanel(new BorderLayout(10, 10));
 		sectionPanel.add(doc, BorderLayout.CENTER);
 		sectionPanel.add(buttonPanel, BorderLayout.SOUTH);
 
 		JPanel bottomPane = new JPanel(new BorderLayout());
 		JPanel north = new JPanel();
 		north.add(aquireLock);
 		north.add(updateSection);
 		north.add(unlockSubSection);
 		north.add(addSubSection);
 		bottomPane.add(north, BorderLayout.NORTH);
 
 		documentPane.setMinimumSize(new Dimension(0, 0));
 		workPane.setMinimumSize(new Dimension(0, 0));
 		JScrollPane workScroll = new JScrollPane(workPane);
 		JScrollPane docScroll = new JScrollPane(documentPane);
 		bottomPane.add(workScroll, BorderLayout.CENTER);
 		workspace = new JSplitPane(JSplitPane.VERTICAL_SPLIT, docScroll, bottomPane);
 		wholePane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, sectionPanel, workspace);
 
 		workspace.setDividerLocation(250);
 		workspace.setOneTouchExpandable(true);
 		wholePane.setDividerLocation(150);
 		wholePane.setOneTouchExpandable(true);
 
 		add(wholePane, BorderLayout.CENTER);
 	}
 
 	private void newSubSection(String name) {
 		info.getServer().newSubSection(info.getUserName(), info.getRoomName(), doc.getName(), name,
 				doc.getSubSectionCount());
 	}
 
 	private void setUpListeners() {
 		doc.addMouseListener(new RightClickListener());
 		sectionUpButton.addActionListener(new UpButtonListener());
 		sectionDownButton.addActionListener(new DownButtonListener());
 		aquireLock.addActionListener(new AquireLockListener());
 		updateSection.addActionListener(new UpdateSubSectionListener());
 		doc.addListSelectionListener(new SelectedSubSectionListener());
 		addSubSection.addActionListener(new NewSubSectionListener());
 		unlockSubSection.addActionListener(new ReleaseLockListener());
 	}
 
 	public JList getList() {
 		return doc;
 	}
 
 	public void updateDocumentView() {
 		documentPane.updateDocument(doc);
 	}
 
 	public SectionizedDocument getSectionizedDocument() {
 		return doc;
 	}
 
 	private void updateSubSection(DocumentSubSection ds, String newText) {
 		if (ds == null) {
 			if (Util.DEBUG) {
 				System.out.println("JDocTabedPanel.updateSubSection  the document was null...wtf");
 			}
 			return;
 		}
 		DocumentSubSection temp = new DocumentSubSectionImpl(ds.getName());
 		temp.setLocked(ds.isLocked(), ds.lockedByUser());
 		temp.setText(info.getUserName(), newText);
 		info.getServer().subSectionUpdated(info.getUserName(), info.getRoomName(),
 				info.getDocumentName(), ds.getName(), temp);
 	}
 
 	private DocumentSubSection getCurrentSubSection() {
 		DocumentSubSection sel = (DocumentSubSection) doc.getSelectedValue();
 		if (sel == null) {
 			if (doc.getModel().getSize() == 0)
 				return null;
 			return (DocumentSubSection) doc.getModel().getElementAt(0);
 		}
 		return sel;
 	}
 
 	private class UpButtonListener implements ActionListener {
 		public void actionPerformed(ActionEvent e) {
 			if (doc.getSelectedIndex() > 0) {
 				DocumentSubSection moveUp = (DocumentSubSection) doc.getSelectedValue();
 				DocumentSubSection moveDown = (DocumentSubSection) doc.getModel().getElementAt(
 						doc.getSelectedIndex() - 1);
 				info.getServer().subSectionFlopped(info.getUserName(), info.getRoomName(),
 						info.getDocumentName(), moveUp.getName(), moveDown.getName());
 				doc.setSelectedValue(moveUp, true);
 			}
 		}
 	}
 
 	private class DownButtonListener implements ActionListener {
 		public void actionPerformed(ActionEvent e) {
 			if (doc.getSelectedIndex() != -1
 					&& doc.getSelectedIndex() < doc.getModel().getSize() - 1) {
 				DocumentSubSection moveDown = (DocumentSubSection) doc.getSelectedValue();
 				DocumentSubSection moveUp = (DocumentSubSection) doc.getModel().getElementAt(
 						doc.getSelectedIndex() + 1);
 				info.getServer().subSectionFlopped(info.getUserName(), info.getRoomName(),
 						info.getDocumentName(), moveUp.getName(), moveDown.getName());
 				doc.setSelectedValue(moveDown, true);
 			}
 		}
 	}
 
 	private class AquireLockListener implements ActionListener {
 		public void actionPerformed(ActionEvent e) {
 			info.getServer().subSectionLocked(info.getUserName(), info.getRoomName(),
 					info.getDocumentName(), getCurrentSubSection().getName());
 		}
 	}
 
 	private class UpdateSubSectionListener implements ActionListener {
 		public void actionPerformed(ActionEvent e) {
			if (!workPane.getText().trim().equals(""))
				updateSubSection(getCurrentSubSection(), workPane.getText());
 		}
 	}
 
 	private class SelectedSubSectionListener implements ListSelectionListener {
 		public void valueChanged(ListSelectionEvent e) {
 			if (e.getValueIsAdjusting() == false) {
 				DocumentSubSection ds = getCurrentSubSection();
 				updateWorkPane(ds);
 			}
 		}
 	}
 
 	public void updateWorkPane(String secName) {
 		updateWorkPane(doc.getSection(secName));
 	}
 
 	public void updateWorkPane(DocumentSubSection ds) {
 		int carrotPos = workPane.getCaretPosition();
 		if (ds != null) {
 			workPane.setEditable(info.getUserName().equals(ds.lockedByUser()));
 			workPane.setText(ds.getText());
 			int length = workPane.getText().length();
 			workPane.setCaretPosition(carrotPos <= length ? carrotPos : length);
 		} else {
 			workPane.setText("");
 			workPane.setEditable(false);
 		}
 	}
 
 	private class NewSubSectionListener implements ActionListener {
 		public void actionPerformed(ActionEvent e) {
 			String name = JOptionPane.showInputDialog(JDocTabPanel.this,
					"Enter a name for the new SubSection");
 			if (name == null)
 				return;
 			newSubSection(name);
 		}
 	}
 
 	private class ReleaseLockListener implements ActionListener {
 		public void actionPerformed(ActionEvent e) {
 			info.getServer().subSectionUnLocked(info.getUserName(), info.getRoomName(),
 					info.getDocumentName(), getCurrentSubSection().getName());
 		}
 	}
 
 	private class SplitSubSectionListener implements ActionListener {
 		@Override
 		public void actionPerformed(ActionEvent e) {
 			String name1 = JOptionPane
 					.showInputDialog(JDocTabPanel.this, "Name of the first part:");
 			if (name1 == null)
 				return;
 			String name2 = JOptionPane.showInputDialog(JDocTabPanel.this,
 					"Name of the second part:");
 			if (name2 == null)
 				return;
 			DocumentSubSection sec = getCurrentSubSection();
 			int idx = SplitChooser.showSplitChooserDialog(sec);
 			if (idx == -1)
 				return;
 			info.getServer().subSectionSplit(info.getUserName(), info.getRoomName(), doc.getName(),
 					sec.getName(), name1, name2, idx);
 		}
 	}
 
 	private class MergeSubSectionListener implements ActionListener {
 		@Override
 		public void actionPerformed(ActionEvent arg0) {
 			DocumentSubSection cur = getCurrentSubSection();
 			int idx = doc.getSubSectionIndex(cur.getName());
 			int count = doc.getSubSectionCount();
 			if (idx == 0 && count > 1) {
 				DocumentSubSection second = doc.getSectionAt(idx + 1);
 				String name = JOptionPane.showInputDialog(JDocTabPanel.this,
 						"Name of merged section:");
 				if (name == null)
 					return;
 				info.getServer().subSectionCombined(info.getUserName(), info.getRoomName(),
 						doc.getName(), cur.getName(), second.getName(), name);
 			} else if (idx == count - 1 && count > 1) {
 				DocumentSubSection first = doc.getSectionAt(idx - 1);
 				String name = JOptionPane.showInputDialog(JDocTabPanel.this,
 						"Name of merged section:");
 				if (name == null)
 					return;
 				info.getServer().subSectionCombined(info.getUserName(), info.getRoomName(),
 						doc.getName(), first.getName(), cur.getName(), name);
 			} else {
 				String[] values = { "Above", "Below" };
 				String aboveOrBelow = (String) JOptionPane
 						.showInputDialog(
 								JDocTabPanel.this,
 								"Would you like to merge the selected section \nwith the section above or below?",
 								"Merge SubSections", JOptionPane.QUESTION_MESSAGE, null, values,
 								values[0]);
 				String name = JOptionPane.showInputDialog(JDocTabPanel.this,
 						"Name of merged section:");
 				if (name == null)
 					return;
 				if (aboveOrBelow.equals("Above")) {
 					DocumentSubSection second = doc.getSectionAt(idx + 1);
 					info.getServer().subSectionCombined(info.getUserName(), info.getRoomName(),
 							doc.getName(), cur.getName(), second.getName(), name);
 				} else if (aboveOrBelow.equals("Below")) {
 					DocumentSubSection first = doc.getSectionAt(idx - 1);
 					info.getServer().subSectionCombined(info.getUserName(), info.getRoomName(),
 							doc.getName(), first.getName(), cur.getName(), name);
 				}
 			}
 		}
 	}
 
 	@SuppressWarnings("serial")
 	private static class SplitChooser extends JDialog {
 		private JTextArea theText;
 		private JButton ok;
 		private JLabel instr;
 		private int retIndex = -1;
 
 		private SplitChooser(DocumentSubSection sec) {
 			super();
 			setModal(true);
 			instr = new JLabel("Place your cursor where you would like to split the subsection");
 			theText = new JTextArea();
 			theText.setText(sec.getText());
 			// theText.setEditable(false);
 			Font f = new Font("Courier New", Font.PLAIN, 11);
 			theText.setFont(f);
 			KeyListener[] list = theText.getKeyListeners();
 			for (KeyListener k : list) {
 				theText.removeKeyListener(k);
 			}
 			theText.setTabSize(4);
 			ok = new JButton("Ok");
 			JScrollPane scroll = new JScrollPane(theText);
 			add(scroll, BorderLayout.CENTER);
 			add(instr, BorderLayout.NORTH);
 			add(ok, BorderLayout.SOUTH);
 			ok.addActionListener(okAction);
 			setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
 			pack();
 		}
 
 		public int getChosenIndex() {
 			return retIndex;
 		}
 
 		public static int showSplitChooserDialog(DocumentSubSection sec) {
 			SplitChooser ch = new SplitChooser(sec);
 			ch.setVisible(true);
 			if (Util.DEBUG) {
 				System.out.println("Index chosen: " + ch.getChosenIndex());
 			}
 			return ch.getChosenIndex();
 		}
 
 		private ActionListener okAction = new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				retIndex = theText.getCaretPosition();
 				SplitChooser.this.dispose();
 			}
 		};
 	}
 
 	private class RightClickListener extends MouseAdapter {
 		@Override
 		public void mousePressed(MouseEvent e) {
 			doPop(e);
 		}
 
 		private void doPop(MouseEvent e) {
 			if (e.getButton() != MouseEvent.BUTTON3)
 				return;
 			if (Util.DEBUG) {
 				System.out.println("click event");
 			}
 			if (e.isPopupTrigger()) {
 				JPopupMenu menu;
 				if (e.getSource() == workPane) {
 					menu = new WorkingViewRightClickMenu();
 				} else {
 					menu = new SectionRightClickMenu(getCurrentSubSection());
 				}
 				menu.show(e.getComponent(), e.getX(), e.getY());
 			}
 		}
 
 		@Override
 		public void mouseReleased(MouseEvent e) {
 			doPop(e);
 		}
 	}
 
 	public class WorkingViewRightClickMenu extends JPopupMenu {
 
 		private JMenuItem splitSubSectionItem;
 		private long bornondate;
 
 		public WorkingViewRightClickMenu() {
 			bornondate = System.currentTimeMillis();
 			splitSubSectionItem = new JMenuItem("Split SubSection At Carrot");
 			splitSubSectionItem.addActionListener(new SplitAtCarrotListener());
 			add(splitSubSectionItem);
 			addMouseListener(mouseOutListener);
 		}
 
 		private MouseListener mouseOutListener = new MouseAdapter() {
 			@Override
 			public void mouseClicked(MouseEvent e) {
 				setVisible(false);
 			}
 
 			@Override
 			public void mouseExited(MouseEvent e) {
 				long thetime = System.currentTimeMillis();
 				if (thetime - bornondate > 500)
 					setVisible(false);
 				else
 					;
 			}
 		};
 	}
 
 	private class SplitAtCarrotListener implements ActionListener {
 		@Override
 		public void actionPerformed(ActionEvent e) {
 			int i = workPane.getCaretPosition();
 			if (i == -1)
 				return;
 			String name1 = JOptionPane
 					.showInputDialog(JDocTabPanel.this, "Name of the first part:");
 			if (name1 == null)
 				return;
 			String name2 = JOptionPane.showInputDialog(JDocTabPanel.this,
 					"Name of the second part:");
 			if (name2 == null)
 				return;
 			DocumentSubSection sec = getCurrentSubSection();
 			info.getServer().subSectionSplit(info.getUserName(), info.getRoomName(), doc.getName(),
 					sec.getName(), name1, name2, i);
 		}
 
 	}
 
 	public class SectionRightClickMenu extends JPopupMenu {
 
 		private JMenuItem aquireLockItem;
 		private JMenuItem releaseLockItem;
 		private JMenuItem splitSectionItem;
 		private JMenuItem mergeSectionItem;
 		private JMenuItem newSubSectionItem;
 
 		private DocumentSubSection sec;
 		private long bornondate;
 
 		public SectionRightClickMenu(DocumentSubSection section) {
 			super();
 			bornondate = System.currentTimeMillis();
 			sec = section;
 			aquireLockItem = new JMenuItem("Aquire Lock");
 			aquireLockItem.addActionListener(new AquireLockListener());
 			add(aquireLockItem);
 			releaseLockItem = new JMenuItem("Release Lock");
 			releaseLockItem.addActionListener(new ReleaseLockListener());
 			add(releaseLockItem);
 			splitSectionItem = new JMenuItem("Split SubSection");
 			splitSectionItem.addActionListener(new SplitSubSectionListener());
 			add(splitSectionItem);
 			mergeSectionItem = new JMenuItem("Merge Subsection");
 			mergeSectionItem.addActionListener(new MergeSubSectionListener());
 			add(mergeSectionItem);
 			newSubSectionItem = new JMenuItem("Add New SubSection");
 			newSubSectionItem.addActionListener(new NewSubSectionListener());
 			add(newSubSectionItem);
 		}
 
 	}
 }
