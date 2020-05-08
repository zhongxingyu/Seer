 package ms.server.ui.dialogs;
 
 import java.awt.Rectangle;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.KeyEvent;
 
 import javax.swing.ImageIcon;
 import javax.swing.JButton;
 import javax.swing.JComponent;
 import javax.swing.JDialog;
 import javax.swing.JFrame;
 import javax.swing.JRootPane;
 import javax.swing.KeyStroke;
 
 import ms.server.ui.MainViewServer;
 
 abstract class AbstractDialog extends JDialog {
 
 	/**
 	 * 
 	 */
 	private static final long serialVersionUID = 1L;
 
 	public AbstractDialog() {
 		initDialog();
 		
 		addESCListener();
 	}
 
 	private void initDialog() {
 		setLayout(null);
 		setResizable(false);
 		setModal(true);
 		setIconImage(new ImageIcon(getClass().getResource(MainViewServer.UIIMAGELOCATION + "icon.png")).getImage());
 		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
 	}
 
 	/**
 	 * esc = close dialog
 	 */
 	protected void addESCListener() {
 		ActionListener cancelListener = new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				close();
 			}
 		};
 		JRootPane rootPane = getRootPane();
 		rootPane.registerKeyboardAction(cancelListener, KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_IN_FOCUSED_WINDOW);
 	}
 	
 	protected void close() {
 		setVisible(false);
 		dispose();
 	}
 	
 	abstract ActionListener getButtonActionListener();
 
 	abstract int[] getButtonMnemonic();
 
 	abstract Rectangle[] getButtonBounds();
 
 	abstract String[] getButtonText();
 
 	
 	/**
 	 * add buttons
 	 */
 	protected void addButtons() {
 		final String[] buttonText = getButtonText();
 		final Rectangle[] bounds = getButtonBounds();
 		final int[] mnemonic = getButtonMnemonic();
 		for (int i = 0; i < buttonText.length; i++) {
 			JButton button = new JButton();
 			button.setBounds(bounds[i]);
 			button.setText(buttonText[i]);
 			button.setMnemonic(mnemonic[i]);
 			button.addActionListener(getButtonActionListener());
 			add(button);
 		}
 	}
 }
