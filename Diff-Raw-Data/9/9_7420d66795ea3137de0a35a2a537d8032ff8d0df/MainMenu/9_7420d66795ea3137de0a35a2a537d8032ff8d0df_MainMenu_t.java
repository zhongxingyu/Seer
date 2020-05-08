 package FRC_Score_Sys;
 
 import java.awt.BorderLayout;
 import java.awt.GridLayout;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.MouseEvent;
 import java.awt.event.MouseListener;
 import java.awt.event.WindowAdapter;
 import java.awt.event.WindowEvent;
 import java.util.List;
 
 import javax.swing.JButton;
 import javax.swing.JFrame;
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 import javax.swing.JTree;
 import javax.swing.ScrollPaneConstants;
 import javax.swing.tree.DefaultMutableTreeNode;
 import javax.swing.tree.DefaultTreeModel;
 
 public class MainMenu extends JFrame {
 
 	public SubSysCommHandler CommHandle;
 
 	InputWindow inputw;
 
 	private static final long serialVersionUID = 1;
 
 	JTree MatchList;
 
 	public MainMenu(SubSysCommHandler CH) {
 		CommHandle = CH;
 		setResizable(false);
 		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 		addWindowListener(new WindowAdapter() {
 			@Override
 			public void windowClosing(WindowEvent e) {
 				System.out
 						.println("Main Window is closing. Let's tell the Comm Handler to close everything out.");
 				CommHandle.RequestAppQuit();
 			}
 		});
 		setTitle("2013 FRC Scoring Application");
 		this.setSize(1000, 500);
 
 		JPanel menu_panel = new JPanel();
 		menu_panel.setLayout(new GridLayout(0, 1, 0, 0));
 		getContentPane().add(menu_panel, BorderLayout.WEST);
 
 		// // - CREATE MENU BUTTONS
 		// Reload Button
 		JButton btnReloadMatches = new JButton("Reload Matches");
 		btnReloadMatches.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent arg0) {
 				LoadMatchList();
 			}
 		});
 		// About Button
 		JButton btnAbout = new JButton("About App");
 		btnAbout.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent arg0) {
 				AboutWindow about = new AboutWindow();
 				about.setLocationRelativeTo(null);
 				about.setVisible(true);
 			}
 		});
 		// Import Button
 		JButton btnImportMatches = new JButton("Import Match List");
 		btnImportMatches.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent arg0) {
 				MainMenu.this.TriggerImportFile();
 				LoadMatchList();
 			}
 		});
 		// Import Button
 		JButton btnQuit = new JButton("Quit");
 		btnQuit.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent arg0) {
 				pullThePlug();
 			}
 		});
 		// /// - ADD MENU BUTTONS TO PANEL
 		menu_panel.add(btnImportMatches);
 		menu_panel.add(btnReloadMatches);
 		menu_panel.add(btnAbout);
 
 		MatchList = new JTree();
 		MatchList.setToggleClickCount(1);
 		MatchList.addMouseListener(new MouseListener() {
 			@Override
 			public void mouseClicked(MouseEvent e) {
 				if (e.getClickCount() == 2) {
 					try {
 						DefaultMutableTreeNode SelectedMatch = (DefaultMutableTreeNode) MatchList
 								.getLastSelectedPathComponent();
 						String leaf = String.valueOf(SelectedMatch);
 						if (SelectedMatch.isLeaf() && (leaf != "Matches")) {
 							System.out
 									.println("Rcvd double click in match list on leaf '"
 											+ leaf
 											+ "'. Triggering edit function!");
 							MainMenu.this.EditMatch(leaf);
 						} else {
 							System.out
 									.println("Rcvd double click in match list, but it was not a leaf. Ignored.");
 						}
 					} catch (NullPointerException err) {
 						System.out
 								.println("Rcvd double click in match list, but caught a Null Error. Was something selected? Ignored.");
						System.out.println(err.getClass().getName() + ": "
								+ err.getMessage());
 					}
 				}
 			}
 
 			@Override
 			public void mouseEntered(MouseEvent e) {
 			}
 
 			@Override
 			public void mouseExited(MouseEvent e) {
 			}
 
 			@Override
 			public void mousePressed(MouseEvent e) {
 			}
 
 			@Override
 			public void mouseReleased(MouseEvent e) {
 			}
 		});
 		LoadMatchList();
 
 		JScrollPane MatchScroller = new JScrollPane(MatchList,
 				ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
 				ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
 
 		getContentPane().add(MatchScroller, BorderLayout.CENTER);
 	}
 
 	private void EditMatch(String matchNumber) {
 		if (inputw == null) {
 			inputw = new InputWindow(this, matchNumber);
 			inputw.pack();
 			inputw.setLocationRelativeTo(null);
 			inputw.setVisible(true);
 		} else {
 			System.out
 					.println("Ignoring Edit Request - Edit already underway!");
 		}
 	}
 
 	private void LoadMatchList() {
 		MatchList.setModel(new DefaultTreeModel(new DefaultMutableTreeNode(
 				"Matches") {
 			private static final long serialVersionUID = 1;
 
 			{
 				DefaultMutableTreeNode node;
 				node = new DefaultMutableTreeNode("Qualifications");
 				List<String> QualMatches = CommHandle.SqlTalk
 						.FetchMatchList("QQ");
 				if (QualMatches.size() > 0) {
 					for (String item : QualMatches) {
 						node.add(new DefaultMutableTreeNode(item));
 					}
 					this.add(node);
 				} else {
 
 				}
 
 			}
 		}));
 	}
 
 	public void pullThePlug() {
 		WindowEvent wev = new WindowEvent(this, WindowEvent.WINDOW_CLOSING);
 		dispatchEvent(wev);
 	}
 
 	// handle como from child windows.
 	public void RecvChildWindowMsg(Object child, String Msg, Object Datagram) {
 		if (child instanceof InputWindow) {
 			switch (Msg) {
 			case "score_save":
 				// TODO: Recast the Datagram to a score class and write to DB
 				break;
 			case "im_closing":
 				System.out
 						.println("InputWindow said it's closing. DIE WINDOW DIE!");
 				inputw = null;
 				break;
 			default:
 				System.out
 						.println("InputWindow said something we didn't understand? German Perhaps?");
 				break;
 			}
 		} else {
 			System.out.println("No child recognized? Hmm...");
 		}
 	}
 
 	public void TriggerImportFile() {
 		MatchReader rdr = new MatchReader(this);
 		rdr.DoFileLoad();
 	}
 
 }
