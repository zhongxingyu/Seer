 package edu.bu.cs673.AwesomeAlphabet.view;
 
 import java.awt.BorderLayout;
 import java.awt.CardLayout;
 import java.awt.Color;
 import java.awt.Container;
 import java.util.Hashtable;
 
 import javax.swing.JFrame;
 import javax.swing.JPanel;
 
 import org.apache.log4j.Logger;
 
 
 
 public class MainWindow implements IPageObserver {
 
 	protected static final int AA_JFRAME_SIZE_HEIGHT	= 800;
 	protected static final int AA_JFRAME_SIZE_WIDTH	= 600;
 	static Logger log = Logger.getLogger(MainWindow.class);
 	
 	private JFrame m_frame;
 	private JPanel m_curView;
 	private CardLayout m_cl;
 	private Hashtable<String, PageView> m_pageHash;
 	
 	public MainWindow() {
 		m_frame = new JFrame("Awesome Alphabet");
 		m_frame.setSize(AA_JFRAME_SIZE_HEIGHT, AA_JFRAME_SIZE_WIDTH);
 		m_frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 		
 		Container content = m_frame.getContentPane();
 	    content.setBackground(Color.white);
 	    content.setLayout(new BorderLayout()); 
 
 	    m_cl = new CardLayout();
 	    m_curView = new JPanel(m_cl);
 	    content.add(m_curView, BorderLayout.CENTER);
 	    
 	    m_pageHash = new Hashtable<String, PageView>();
 	}
 	
 	public void registerPage(PageView page)
 	{
		log.info("Registered view: " + page.getPageName());
 		m_curView.add(page.getPageName(), page.getPagePanel());
 		m_pageHash.put(page.getPageName(),  page);
 	}
 	
 	public void Show()
 	{
 		m_frame.setVisible(true);
 	}
 
 	@Override
 	public boolean GoToPage(String sPageName)
 	{
 		PageView pv = m_pageHash.get(sPageName);
 		log.info("Go to Page " + pv.getPageName());
 		pv.activated();
 		m_cl.show(m_curView, sPageName);
 		return true;
 	}
 	
 	public JFrame getJFrame()
 	{
 		return m_frame;
 	}
 	
 }
