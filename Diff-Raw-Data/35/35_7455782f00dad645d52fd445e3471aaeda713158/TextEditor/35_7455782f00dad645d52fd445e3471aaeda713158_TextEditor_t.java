 package modules;
 
 import ide.Configuration;
 import ide.ConflictException;
 import ide.MsgRcvr;
 import ide.OutBox;
 import ide.RussianPost;
 
 import java.awt.AWTEvent;
 import java.awt.BorderLayout;
 import java.awt.Component;
 import java.awt.event.KeyEvent;
 import java.awt.event.KeyListener;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileReader;
 import java.io.IOException;
 import java.io.PrintStream;
 import java.util.Enumeration;
 import java.util.Hashtable;
 import java.util.Iterator;
 import java.util.Set;
 
 import javax.swing.ActionMap;
 import javax.swing.ImageIcon;
 import javax.swing.InputMap;
 import javax.swing.JComponent;
 import javax.swing.JFileChooser;
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 import javax.swing.JPopupMenu;
 import javax.swing.JScrollPane;
 import javax.swing.JTabbedPane;
 import javax.swing.JTextPane;
 import javax.swing.KeyStroke;
 import javax.swing.UIManager;
 import javax.swing.event.CaretEvent;
 import javax.swing.event.CaretListener;
 import javax.swing.event.DocumentEvent;
 import javax.swing.event.DocumentListener;
 import javax.swing.text.BadLocationException;
import javax.swing.text.Caret;
 import javax.swing.text.Document;
 import javax.swing.text.PlainDocument;
 import javax.swing.text.StyledDocument;
 import javax.swing.text.StyledEditorKit;
 
 import mainframe.GraphicsModule;
 import mainframe.MainFrame;
 
 public class TextEditor extends GraphicsModule{
 	private final int OPEN;
 	private final int SAVE;
 	private final int OPEN_MENU;
 	private final int SAVE_MENU;
 	private Hashtable<JPanel, TextFile> documents;
 	private JFileChooser fc;
 	private TabChangeListener tabchangelistener;
 	private InputMap im = null;
 	private ActionMap am = null;
 	
 	private class KeyBindingsManager implements MsgRcvr{
 
 		public KeyBindingsManager() {
 			TextEditor.this.russianpost.addIncPostBox("SetKeys", this);
 		}
 		
 		@Override
 		public void handle(Object msg) {
 			if(msg instanceof KeyBindingsInitializer && msg != null){
 				((KeyBindingsInitializer)msg).init(im, am);
 			}
 		}
 		
 	}
 	
 	private class PopupMenuManager implements MsgRcvr, CaretListener{
 		private JTextPane tp = null;
 		private PopupMenu pm = null;
 		
 		public PopupMenuManager() {
 			TextEditor.this.russianpost.addIncPostBox("ShowPopupMenu", this);
 		}
 
 		@Override
 		public void handle(Object msg) {
 			if(msg instanceof PopupMenu && msg!=null){
 				pm = (PopupMenu) msg;
 				tp = documents.get(getActiveTab()).getTextPane();
 				pm.show(tp, tp.getCaret().getMagicCaretPosition().x, tp.getCaret().getMagicCaretPosition().y+tp.getFontMetrics(tp.getFont()).getHeight());
 				tp.addCaretListener(this);
 			}
 		}
 
 		@Override
 		public void caretUpdate(CaretEvent e) {
 			pm.hide();
 			tp.removeCaretListener(this);
 			tp.grabFocus();
 		}
 	}
 	
 	private class SetTab implements MsgRcvr{
 
 		public SetTab() {
 			TextEditor.this.russianpost.addIncPostBox("SetDocument", this);
 		}
 		
 		@Override
 		public void handle(Object msg) {
 			TextFile tf = (TextFile) msg;
 			System.out.println(msg+" "+tf);
 			if(tf != null)TextEditor.this.mainframe.setActiveTab(tf.getPanel(), MainFrame.TOP);
 		}
 		
 	}
 	
 	private class OpenFile implements MsgRcvr{
 
 		public OpenFile() {
 			TextEditor.this.russianpost.addIncPostBox("OpenFile", this);
 		}
 		
 		@Override
 		public void handle(Object msg) {
 			if(msg instanceof File){
 				TextEditor.this.open((File)msg);
 			}
 		}
 		
 	}
 	
 	public class TextFile implements DocumentListener{
 		private File f;
 		private JTextPane tp;
 		private boolean changed;
 		private JPanel p;
 		
 		public TextFile(File f, JTextPane tp, JPanel p) {
 			this.changed = false;
 			this.tp = tp;
 			this.f = f;
 			this.p = p;
 			try {
 				FileInputStream is = new FileInputStream(f);
 				byte[] str = new byte[is.available()];
 				is.read(str);
 				is.close();
 				if(str.length > 0) 
 					tp.getDocument().insertString(0, new String(str), null);
 			} catch (BadLocationException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			} catch (FileNotFoundException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			} catch (IOException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 			tp.getDocument().addDocumentListener(this);
 		}
 		
 		public boolean isChanged() {
 			return changed;
 		}
 		
 		private JPanel getPanel(){
 			return p;
 		}
 		
 		private void save(){
 			try {
 				PrintStream ps = new PrintStream(f);
 				String str = tp.getDocument().getText(0, tp.getDocument().getLength());
 				ps.print(str);
 				ps.close();
 				changed = false;
 				p.setName(f.getName());
 				p.getParent().repaint();
 				tp.getDocument().addDocumentListener(this);
 			} catch (BadLocationException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			} catch (FileNotFoundException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 		}
 		
 		public Document getDocument() {
 			return tp.getDocument();
 		}
 		
		public int getCurrentPosition(){
			return tp.getCaretPosition();
		}
		
 		public File getFile() {
 			return f;
 		}
 		
 		@Override
 		public String toString() {
 			return f.getName();
 		}
 
 		@Override
 		public void changedUpdate(DocumentEvent e) {
 			// here will font changes - so leave it blank
 		}
 
 		@Override
 		public void insertUpdate(DocumentEvent e) {
 			changed = true;
 			p.setName("*"+f.getName());
 			p.getParent().repaint();
 			tp.getDocument().removeDocumentListener(this);
 		}
 
 		@Override
 		public void removeUpdate(DocumentEvent e) {
 			changed = true;
 			p.setName("*"+f.getName());
 			p.getParent().repaint();
 			tp.getDocument().removeDocumentListener(this);
 		}
 		
 		private JTextPane getTextPane(){
 			return tp;
 		}
 	}
 	
 	private class TabChangeListener implements MsgRcvr{
 		private OutBox outChange;
 		private OutBox outOpen;
 		private OutBox outClose;
 		
 		public TabChangeListener() {
 			outChange = TextEditor.this.russianpost.addOutPostBox("ActiveDocument");
 			outOpen = TextEditor.this.russianpost.addOutPostBox("OpenDocument");
 			outClose = TextEditor.this.russianpost.addOutPostBox("CloseDocument");
 			TextEditor.this.russianpost.subscribe("topChange", this);
 		}
 		
 		@Override
 		public void handle(Object msg) {
			if(msg != null){
				TextFile tf = TextEditor.this.documents.get(msg);
				if(tf != null)
					outChange.putAndSend(tf);
			}
 		}
 		
 		public void open(TextFile doc){
 			outOpen.putAndSend(doc);
 		}
 		
 		public void close(TextFile doc){
 			outClose.putAndSend(doc);
 		}
 		
 	}
 
 	public TextEditor(MainFrame mf, RussianPost rp, Configuration conf) {
 		super(mf, rp, conf);
 		int zo = -1, zs = -1;
 		try {
 			zo = mf.addMenu("File/Open", this, KeyStroke.getKeyStroke("ctrl O"));
 			zs = mf.addMenu("File/Save", this, KeyStroke.getKeyStroke("ctrl S"));
 		} catch (ConflictException e) {
 			e.printStackTrace();
 		}
 		OPEN_MENU = zo;
 		SAVE_MENU = zs;
 		OPEN = mf.addButton(new ImageIcon("document-open.png"),this);
 		SAVE = mf.addButton(new ImageIcon("document-save.png"),this);
 		documents = new Hashtable<JPanel, TextFile>();
 		fc = new JFileChooser();
 		tabchangelistener = new TabChangeListener();
 		new SetTab();
 		new OpenFile();
 		new PopupMenuManager();
 		JTextPane tp = new JTextPane();
 		im = tp.getInputMap();
 		am = tp.getActionMap();
 		new KeyBindingsManager();
 	}
 
 	private JPanel getActiveTab(){
 		return mainframe.getActiveTab(MainFrame.TOP);
 	}
 	
 	@Override
 	public boolean onTabClose(Component where) {
 		JPanel p = (JPanel) where;
 		TextFile tf = documents.get(p);
 		if(tf.isChanged()){
 			int res = JOptionPane.showConfirmDialog(mainframe.getFrame(), "Do you want to save changes?", "Question", JOptionPane.YES_NO_CANCEL_OPTION);
 			if(res == JOptionPane.YES_OPTION){
 				tf.save();
 				tabchangelistener.close(documents.get(p));
 				documents.remove(p);
 				return false;
 			}
 			if(res == JOptionPane.NO_OPTION){
 				tabchangelistener.close(documents.get(p));
 				documents.remove(p);
 				return false;
 			}
 			return true;
 		}else{
 			tabchangelistener.close(documents.get(p));
 			documents.remove(p);
 			return false;
 		}
 	}
 	
 	@Override
 	public void menuClick(int index) {
 		if(index == OPEN_MENU){
 			buttonClick(OPEN);
 		}
 		if(index == SAVE_MENU){
 			buttonClick(SAVE);
 		}
 	}
 	
 	@Override
 	public void buttonClick(int index) {
 		/*if(index == NEW){
 			if(mainframe == null){
 				System.out.println("FUCK");
 				return;
 			}
 			JPanel p = mainframe.addGraphicsModule(this, MainFrame.TOP, "Untitled"+(count++));
 			
 		}*/
 		if(index == OPEN){
 			fc.setDialogTitle("Open file");
 			fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
 			int res = fc.showOpenDialog(mainframe.getFrame());
 			if (res == JFileChooser.APPROVE_OPTION){
 				File f = fc.getSelectedFile();
 				open(f);
 			}
 			
 		}
 		if(index == SAVE){
 			save();
 		}
 	}
 	
 	private void open(File f){
 		JPanel p = mainframe.addGraphicsModule(this, MainFrame.TOP, f.getName());
 		p.setLayout(new BorderLayout(0,0)); 
 		JTextPane tp = new JTextPane();
 		tp.setInputMap(JComponent.WHEN_FOCUSED, im);
 		tp.setActionMap(am);
 		TextFile tf = new TextFile(f, tp, p);
 		tp.setCaretPosition(0);
 		//tp.setSize(p.getSize());
 		JScrollPane sp = new JScrollPane(tp);
 		//sp.setSize(p.getSize());
 		p.add(sp,BorderLayout.CENTER);
 		documents.put(p, tf);
		tabchangelistener.handle(p);
 		tabchangelistener.open(tf);
 	}
 	
 	private void save(){
 		JPanel p = getActiveTab();
 		if(p == null) return;
 		TextFile tf = documents.get(p);
 		tf.save();
 	}
 	
 }
 
