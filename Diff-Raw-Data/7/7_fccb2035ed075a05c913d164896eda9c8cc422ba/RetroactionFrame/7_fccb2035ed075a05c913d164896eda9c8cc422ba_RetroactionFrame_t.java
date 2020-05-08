 package br.ufpr.c3sl.view.retroaction;
 
 import java.awt.BorderLayout;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.WindowEvent;
 import java.awt.event.WindowListener;
 
 import javax.swing.JFrame;
 import javax.swing.JMenu;
 import javax.swing.JMenuBar;
 import javax.swing.JMenuItem;
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 
 import br.ufpr.c3sl.dao.RetroactionDAO;
 import br.ufpr.c3sl.daoFactory.DAOFactory;
 import br.ufpr.c3sl.deepClone.ObjectByteArray;
 import br.ufpr.c3sl.exception.UserException;
 import br.ufpr.c3sl.model.Mistake;
 import br.ufpr.c3sl.model.Retroaction;
 import br.ufpr.c3sl.util.Util;
 import br.ufpr.c3sl.view.principal.JpCarrie;
 import br.ufpr.c3sl.virtualkeyboard.main.VirtualKeyBoardMain;
 
 public class RetroactionFrame extends JFrame{
 
 	private static final long serialVersionUID = -4715230985143190020L;
 	private JPanel mainPanel;
 	private JPanel mistakePanel;
 
 	private Mistake mistake;
 
 	public RetroactionFrame(Mistake mistake){
 		super("Retroação: " + mistake.getMistakeInfo().getTitle() + " : " + mistake.getCreatedAtTime());
 		this.setName("RetroactionFrame");
 		this.mistake = mistake;
 		this.mistakePanel = (JPanel) ObjectByteArray.getObject(mistake.getObject());
 		this.mainPanel = new JPanel();
 
 		Util.updateStaticFields(this.mistakePanel);
 
 		this.mistakePanel.setPreferredSize(this.mistakePanel.getSize());
 		this.mistakePanel.setName("Erro na Retroação do " + this.mistakePanel.getName());
 		 
 		mainPanel.setLayout(new BorderLayout());
 		setContentPane(mainPanel);
 		initComponents();
 		
 		
 		this.addWindowListener(new WindowListener() {
 
 			@Override
 			public void windowOpened(WindowEvent e) {
 				//System.out.println("Opened");
 			}
 
 			@Override
 			public void windowIconified(WindowEvent e) { //System.out.println("Iconified");
 				JpCarrie.getInstance().setPanelToSave(null);
 			}
 
 			@Override
 			public void windowDeiconified(WindowEvent e) {//System.out.println("Deiconified");
 			}
 
 			@Override
 			public void windowDeactivated(final WindowEvent e) {//System.out.println("windowDeactivated ");
 				if (e.getOppositeWindow() != null){
 					if (e.getOppositeWindow() instanceof VirtualKeyBoardMain){
 						VirtualKeyBoardMain vk = (VirtualKeyBoardMain) e.getOppositeWindow();
 					
 						if (vk.getOwner() instanceof RetroactionFrame 
 								&& vk.getOwner().hashCode() == RetroactionFrame.this.hashCode()){
 							return;
 						}
 					}
 				}
 				
 				JpCarrie.getInstance().setPanelToSave(null);
 			}
 
 			
 			@Override
 			public void windowClosing(WindowEvent e) { //System.out.println("windowClosing");
 				JpCarrie.getInstance().setPanelToSave(null);
 			}
 
 			@Override
 			public void windowClosed(WindowEvent e) { //System.out.println("windowClosed");
 				JpCarrie.getInstance().setPanelToSave(null);
 			}
 
 			@Override
 			public void windowActivated(WindowEvent e) { //System.out.println("Activated"); 
 				JpCarrie.getInstance().setPanelToSave(mistakePanel);
 			}
 		});
		//saveRetroaction();
 	}
 
 	public void initComponents(){
 		setupMenu();
 		mistakePanel.setPreferredSize(mistakePanel.getPreferredSize());
 		mainPanel.add(mistakePanel, BorderLayout.CENTER);
 	}
 
 	private void setupMenu(){
 		JMenuBar jmenubar = new JMenuBar();
 
 		JMenu menu = new JMenu("Aplicação"); 
 		jmenubar.add(menu);
 
 		JMenuItem itemShowErDescription = new JMenuItem("Descrição do Erro");
 		itemShowErDescription.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent arg0) {
 
 				JOptionPane.showMessageDialog(RetroactionFrame.this,
 						"Retroação: " + mistake.getMistakeInfo().getTitle() + " : " + mistake.getCreatedAtTime()+"\n\n"+
 						mistake.getMistakeInfo().getTitle()+"\n\n"
 						+ mistake.getMistakeInfo().getDescription() + "\n\n",
 						mistake.getMistakeInfo().getTitle() + " : "+ mistake.getCreatedAtTime(),
 						JOptionPane.INFORMATION_MESSAGE);
 
 			}
 		});
 		menu.add(itemShowErDescription);
 
 		JMenuItem itemResetState = new JMenuItem("Voltar ao estado inicial deste erro");
 
 		itemResetState.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent arg0) {
 				resetState();
 			}
 		});
 
 		menu.add(itemResetState);
 
 		JMenuItem itemExit = new JMenuItem("Fechar");
 
 		itemExit.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent arg0) {
 				RetroactionFrame.this.dispose();
 			}
 		});
 
 		menu.add(itemExit);
 
 		mainPanel.add(jmenubar, BorderLayout.NORTH);
 	}
 
 	private void resetState() {
 		mainPanel.remove(this.mistakePanel);
 		this.mistakePanel = (JPanel) ObjectByteArray.getObject(mistake.getObject());
 		this.mistakePanel.setName("Erro na Retroação do " + this.mistakePanel.getName());
 		Util.updateStaticFields(this.mistakePanel);
 		mainPanel.add(this.mistakePanel);
 		mainPanel.revalidate();
 	}
 
 	public void setVisible(boolean arg){
 		this.resetState();
 		super.setVisible(arg);
 	}
 
 	/**
 	 * Create the GUI and show it.  For thread safety,
 	 * this method should be invoked from the
 	 * event-dispatching thread.
 	 */
 	public static JFrame createAndShowGUI(Mistake mistake) {
 		//Create and set up the window.
		RetroactionFrame frame = new RetroactionFrame(mistake);
 		frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
 
 		//Display the window.
 		frame.pack();
 		frame.setLocationRelativeTo(JpCarrie.getInstance());
 		frame.setVisible(true);
		frame.saveRetroaction();
 		return frame;
 	}
 	
 	private void saveRetroaction(){
 		DAOFactory dao = DAOFactory.getDAOFactory(DAOFactory.DATABASE_CHOOSE);
 		RetroactionDAO retroactionDao = dao.getRetroactionDAO();
 		
 		Retroaction retroaction = new Retroaction();
 		retroaction.setMistake(mistake);
 		//retroaction.sUser(Session.getCurrentUser());
 		
 		try {
 			retroactionDao.insert(retroaction);
 		} catch (UserException e1) {
 			e1.printStackTrace();
 		}
 	}
 }
