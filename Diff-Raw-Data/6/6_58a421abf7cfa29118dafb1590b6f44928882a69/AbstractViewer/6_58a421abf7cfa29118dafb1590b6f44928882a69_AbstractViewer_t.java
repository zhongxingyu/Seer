 package views;
 
 import java.awt.Dimension;
 import java.awt.event.ComponentEvent;
 import java.awt.event.ComponentListener;
 import java.awt.event.WindowEvent;
 import java.awt.event.WindowListener;
 
 import javax.swing.JDialog;
 
 import components.MySearchField;
 
 public abstract class AbstractViewer extends JDialog {
 
 	/**
 	 * 
 	 */
 	private static final long serialVersionUID = -3133936580146906508L;
 	protected MySearchField searchfield;
 	private boolean windowClosed = false;
 	public AbstractViewer() {
 		MasterView.setWindowOpen(this);
 //		this.setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
 		this.addComponentListener(new ComponentListener() {
 
 			@Override
 			public void componentShown(ComponentEvent e) {
 				MasterView.setSearchfield(searchfield);
 			}
 
 			@Override
 			public void componentResized(ComponentEvent e) {
 				// System.out.println(getSize());
 			}
 
 			@Override
 			public void componentMoved(ComponentEvent e) {
 				// TODO Auto-generated method stub
 
 			}
 
 			@Override
 			public void componentHidden(ComponentEvent e) {
 				// TODO Auto-generated method stub
 
 			}
 		});
 		this.addWindowListener(new WindowListener() {
 
 			@Override
 			public void windowClosing(WindowEvent e) {
				if (!windowClosed){
					MasterView.setWindowClose();
					windowClosed = true;
				}
 
 			}
 
 			@Override
 			public void windowClosed(WindowEvent e) {
 				if (!windowClosed){
 					MasterView.setWindowClose();
 					windowClosed = true;
 				}
 
 			}
 
 			@Override
 			public void windowActivated(WindowEvent e) {
 
 			}
 
 			@Override
 			public void windowOpened(WindowEvent e) {
 				init_last();
 			}
 
 			@Override
 			public void windowIconified(WindowEvent e) {
 				// TODO Auto-generated method stub
 
 			}
 
 			@Override
 			public void windowDeiconified(WindowEvent e) {
 				// TODO Auto-generated method stub
 
 			}
 
 			@Override
 			public void windowDeactivated(WindowEvent e) {
 
 			}
 		});
 
 	}
 
 	public void init_last() {
 		this.setLocationRelativeTo(MasterView.getInstance().getFrame());
 
 	}
 
 	protected void createWindow() {
 		this.setMinimumSize(new Dimension(900, 500));
 		this.setLocationRelativeTo(MasterView.getFrontWindow());
 		this.setVisible(true);
 	}
 
 }
