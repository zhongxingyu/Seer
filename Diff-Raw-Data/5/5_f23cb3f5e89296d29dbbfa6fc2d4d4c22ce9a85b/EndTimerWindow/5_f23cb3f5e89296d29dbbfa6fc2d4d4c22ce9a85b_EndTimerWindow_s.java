 package max.utility.tomato.gui;
 
 import java.awt.Color;
 import java.awt.Font;
 import java.awt.Insets;
 import java.awt.Toolkit;
 import java.awt.event.KeyAdapter;
 import java.awt.event.KeyEvent;
 import java.awt.event.MouseAdapter;
 import java.awt.event.MouseEvent;
 import java.awt.event.WindowAdapter;
 import java.awt.event.WindowEvent;
 
 import javax.swing.GroupLayout;
 import javax.swing.JButton;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JScrollPane;
 import javax.swing.JTextArea;
 import javax.swing.LayoutStyle;
 import javax.swing.UIManager;
 import javax.swing.WindowConstants;
 
 import max.utility.tomato.DaoRegister;
 import max.utility.tomato.dao.HibernateBasicDaoImpl;
 import max.utility.tomato.domain.Tomato;
 import max.utility.tomato.domain.TomatoReview;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 public class EndTimerWindow extends JFrame {
 
 	public static final Logger logger = LoggerFactory.getLogger(EndTimerWindow.class);
 	/**
 	 * 
 	 */
 	private static final long serialVersionUID = 8849013985135065049L;
 
 	private final HibernateBasicDaoImpl basicDao;
 
 	// Variables declaration - do not modify
 	private JButton btn_reviewCompleted;
 	private JScrollPane jScrollPane1;
 	private JScrollPane jScrollPane3;
 	private JScrollPane jScrollPane4;
 	private JLabel lblFocusOn;
 	private JLabel lblNote;
 	private JLabel lblDone;
 	private JLabel lblTitle;
 	private JTextArea taFocusOn;
 	private JTextArea taNote;
 	private JTextArea taDone;
 	private Tomato tomato;
 
 	// End of variables declaration
 
 	public EndTimerWindow(Tomato tomato) {
 		setTitle("End Timer");
 		this.tomato = tomato;
 		basicDao = (HibernateBasicDaoImpl) DaoRegister.get(HibernateBasicDaoImpl.class);
 		initComponents();
 		setVisible(true);
 		logger.debug(tomato.toString());
 	}
 
 	private void btn_reviewCompletedKeyPressed(KeyEvent evt) {
 		logger.debug(evt.getKeyCode() + ", " + evt.getKeyChar());
 		if (KeyEvent.VK_ENTER == evt.getKeyCode()) {
 			saveAndCloseWindow();
 		}
 	}
 
 	private void btn_reviewCompletedMouseClicked(MouseEvent evt) {
 		saveAndCloseWindow();
 	}
 
 	private void initComponents() {
 
 		lblFocusOn = new JLabel();
 		taFocusOn = new JTextArea();
 		jScrollPane1 = new JScrollPane();
 
 		lblDone = new JLabel();
 		taDone = new JTextArea();
 		jScrollPane4 = new JScrollPane();
 
 		lblNote = new JLabel();
 		taNote = new JTextArea();
 		jScrollPane3 = new JScrollPane();
 
 		btn_reviewCompleted = new JButton();
 
 		setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
 		setBackground(new Color(0, 0, 0));

 		lblTitle.setFont(new Font("Comic Sans MS", 0, 14)); // NOI18N
 		lblTitle.setForeground(new Color(255, 153, 0));
 		lblTitle.setText("rivedi quanto fatto");
 
 		lblFocusOn.setBackground(new Color(0, 0, 0));
 		lblFocusOn.setForeground(new Color(0, 204, 204));
 		lblFocusOn.setLabelFor(taFocusOn);
 		lblFocusOn.setText("volevo fare ...");
 
 		taFocusOn.setEditable(false);
 		taFocusOn.setBackground(new Color(0, 0, 0));
 		taFocusOn.setColumns(20);
 		taFocusOn.setFont(new Font("Andalus", 0, 13)); // NOI18N
 		taFocusOn.setForeground(new Color(0, 255, 255));
 		taFocusOn.setRows(10);
 		taFocusOn.setTabSize(2);
 		// ta_focusOn.setFocusable(false);
 		taFocusOn.setText(tomato.getFocusOn());
 		taFocusOn.setToolTipText("breve revisione di quanto fatto ..");
 		taFocusOn.setMargin(new Insets(0, 2, 2, 2));
 		jScrollPane1.setViewportView(taFocusOn);
 
 		lblDone.setForeground(new Color(0, 0, 255));
 		lblDone.setLabelFor(taFocusOn);
 		lblDone.setText("ho fatto");
 
 		taDone.setColumns(20);
 		taDone.setFont(new Font("Andalus", 0, 13)); // NOI18N
 		taDone.setForeground(new Color(0, 0, 255));
 		taDone.setRows(10);
 		taDone.setTabSize(2);
 		taDone.setText("salva le ultime modifiche.\ncommit -m \"fine timer\"\n");
 		taDone.setToolTipText("breve revisione di quanto fatto ..");
 		// ta_reallyDone.setFocusCycleRoot(true);
 		taDone.setMargin(new Insets(0, 2, 2, 2));
 		taDone.requestFocusInWindow();
 		taDone.setNextFocusableComponent(taNote);
 		jScrollPane4.setViewportView(taDone);
 
 		lblNote.setForeground(new Color(204, 0, 0));
 		lblNote.setLabelFor(taNote);
		lblNote.setText("difficolt�, impedimenti, problemi, ...");
 
 		jScrollPane3.setBackground(new Color(204, 0, 0));
 
 		taNote.setColumns(20);
 		taNote.setFont(new Font("Andalus", 0, 13)); // NOI18N
 		taNote.setForeground(new Color(204, 0, 0));
 		taNote.setRows(10);
 		taNote.setTabSize(2);
 		taNote.setText("finestre di start e stop (versione base) ok\nformattazione del codice ok\neseguire il codice da riga di comando ok\navvio e stop del timer ok\n");
 		taNote.setToolTipText("breve revisione di quanto fatto ..");
 		// ta_problemsRaised.setFocusCycleRoot(true);
 		taNote.setMargin(new Insets(0, 2, 2, 2));
 		taNote.setNextFocusableComponent(btn_reviewCompleted);
 		jScrollPane3.setViewportView(taNote);
 
 		btn_reviewCompleted.setText("ok");
 		// btn_reviewCompleted.setFocusCycleRoot(true);
 		btn_reviewCompleted.setNextFocusableComponent(taFocusOn);
 		btn_reviewCompleted.addMouseListener(new MouseAdapter() {
 			@Override
 			public void mouseClicked(MouseEvent evt) {
 				btn_reviewCompletedMouseClicked(evt);
 			}
 		});
 		btn_reviewCompleted.addKeyListener(new KeyAdapter() {
 			@Override
 			public void keyPressed(KeyEvent evt) {
 				btn_reviewCompletedKeyPressed(evt);
 			}
 		});
 
 		addWindowListener(new WindowAdapter() {
 			@Override
 			public void windowClosing(WindowEvent evt) {
 				logger.debug("start closing windows..");
 				// setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 				// http://stackoverflow.com/questions/1234912/how-to-programmatically-close-a-jframe
 				// System.exit(0);
 			}
 
 		});
 		GroupLayout layout = new GroupLayout(getContentPane());
 		getContentPane().setLayout(layout);
 		layout.setHorizontalGroup(layout
 				.createParallelGroup(GroupLayout.Alignment.LEADING)
 				.addGroup(
 						layout.createSequentialGroup()
 								.addGroup(
 										layout.createParallelGroup(GroupLayout.Alignment.LEADING)
 												.addGroup(
 														GroupLayout.Alignment.TRAILING,
 														layout.createSequentialGroup()
 																.addGap(0, 0, Short.MAX_VALUE)
 																.addGroup(
 																		layout.createParallelGroup(GroupLayout.Alignment.LEADING)
 																				.addComponent(jScrollPane4,
 																						GroupLayout.Alignment.TRAILING,
 																						GroupLayout.PREFERRED_SIZE, 500,
 																						GroupLayout.PREFERRED_SIZE)
 																				.addComponent(jScrollPane3,
 																						GroupLayout.Alignment.TRAILING,
 																						GroupLayout.PREFERRED_SIZE, 500,
 																						GroupLayout.PREFERRED_SIZE)))
 												.addGroup(
 														layout.createSequentialGroup()
 																.addGroup(
 																		layout.createParallelGroup(GroupLayout.Alignment.LEADING)
 																				.addGroup(
 																						layout.createSequentialGroup()
 																								.addGap(20, 20, 20)
 																								.addComponent(lblDone))
 																				.addGroup(
 																						layout.createSequentialGroup()
 																								.addGap(20, 20, 20)
 																								.addComponent(lblNote))
 																				.addGroup(
 																						layout.createSequentialGroup()
 																								.addGap(19, 19, 19)
 																								.addGroup(
 																										layout.createParallelGroup(
 																												GroupLayout.Alignment.LEADING)
 																												.addComponent(
 																														jScrollPane1,
 																														GroupLayout.PREFERRED_SIZE,
 																														500,
 																														GroupLayout.PREFERRED_SIZE)
 																												.addGroup(
 																														layout.createSequentialGroup()
 																																.addComponent(
 																																		lblFocusOn,
 																																		GroupLayout.PREFERRED_SIZE,
 																																		79,
 																																		GroupLayout.PREFERRED_SIZE)
 																																.addGap(92,
 																																		92,
 																																		92)
 																																.addComponent(
 																																		lblTitle)))))
 																.addGap(0, 0, Short.MAX_VALUE))).addContainerGap())
 				.addGroup(
 						layout.createSequentialGroup().addGap(237, 237, 237).addComponent(btn_reviewCompleted)
 								.addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)));
 		layout.setVerticalGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING).addGroup(
 				layout.createSequentialGroup()
 						.addContainerGap()
 						.addGroup(
 								layout.createParallelGroup(GroupLayout.Alignment.LEADING)
 										.addGroup(layout.createSequentialGroup().addComponent(lblTitle).addGap(15, 15, 15))
 										.addGroup(
 												GroupLayout.Alignment.TRAILING,
 												layout.createSequentialGroup().addComponent(lblFocusOn)
 														.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)))
 						.addComponent(jScrollPane1, GroupLayout.PREFERRED_SIZE, 111, GroupLayout.PREFERRED_SIZE).addGap(19, 19, 19)
 						.addComponent(lblDone).addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
 						.addComponent(jScrollPane4, GroupLayout.PREFERRED_SIZE, 111, GroupLayout.PREFERRED_SIZE).addGap(33, 33, 33)
 						.addComponent(lblNote).addGap(1, 1, 1)
 						.addComponent(jScrollPane3, GroupLayout.PREFERRED_SIZE, 111, GroupLayout.PREFERRED_SIZE).addGap(18, 18, 18)
 						.addComponent(btn_reviewCompleted).addContainerGap(27, Short.MAX_VALUE)));
 
 		pack();
 	}
 
 	public JFrame openWindow() {
 		try {
 			for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
 				if ("Nimbus".equals(info.getName())) {
 					UIManager.setLookAndFeel(info.getClassName());
 					break;
 				}
 			}
 		} catch (Exception ex) {
 			logger.error(null, ex);
 		}
 		return this;
 	}
 
 	private void saveAndCloseWindow() {
 		basicDao.save(new TomatoReview(tomato, taDone.getText(), taNote.getText()));
 		WindowEvent wev = new WindowEvent(this, WindowEvent.WINDOW_CLOSING);
 		Toolkit.getDefaultToolkit().getSystemEventQueue().postEvent(wev);
 	}
 
 }
