 
 package com.lofibucket.yotris.ui.gui.components;
 
 import com.lofibucket.yotris.ui.gui.components.GameArea;
 import com.lofibucket.yotris.logic.GameState;
 import com.lofibucket.yotris.ui.CommandContainer;
 import com.lofibucket.yotris.ui.gui.View;
 import com.lofibucket.yotris.ui.gui.menu.NewGameActionListener;
 import com.lofibucket.yotris.ui.gui.menu.PauseActionListener;
 import com.lofibucket.yotris.ui.gui.menu.QuitActionListener;
 import com.lofibucket.yotris.ui.gui.menu.ShowSettingsActionListener;
 import com.lofibucket.yotris.util.Settings;
 import java.awt.BorderLayout;
 import java.awt.Container;
 import java.awt.Dimension;
 import java.awt.event.ActionEvent;
 import java.awt.event.KeyEvent;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JMenu;
 import javax.swing.JMenuBar;
 import javax.swing.JMenuItem;
 import javax.swing.KeyStroke;
 import javax.swing.WindowConstants;
 
 
 public class MainWindow extends JFrame implements View {
 	private JMenuBar menubar;
 	private JMenu gameMenu;
 	private JMenu editMenu;
 	private GameArea area;
 	private CommandContainer commandlist;
 	private SettingsWindow settingsWindow;
 	private Settings settings;
 
 	public MainWindow(Settings settings, CommandContainer commandlist) {
 		super("yotris " + settings.getVersion().toString());
 
 		this.settings = settings;
 		this.commandlist = commandlist;
 
 		settingsWindow = new SettingsWindow();
 
		setPreferredSize(new Dimension(400, 550));
 		setResizable(false);
 		
 		setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
 
 		createComponents(getContentPane());
 		createMenu(this);
 	}
 
 	private void createComponents(Container container) {
 		//container.addKeyListener(keylistener);
 		area = new GameArea(null, settings.getTheme());
 		container.add(area, BorderLayout.CENTER);
 		
 		JLabel teksti = new JLabel("Score:");
         container.add(teksti, BorderLayout.LINE_END);
 	}
 
 	private void createMenu(JFrame frame) {
 		menubar = new JMenuBar();
 		gameMenu = new JMenu("Game");
 		editMenu = new JMenu("Edit");
 
 		menubar.add(gameMenu);
 		menubar.add(editMenu);
 
 		JMenuItem startItem = new JMenuItem("New game");
 		JMenuItem pauseItem = new JMenuItem("Toggle pause");
 		JMenuItem quitItem = new JMenuItem("Quit");
 
 		startItem.getAccessibleContext().setAccessibleDescription(
 				"Start a new game");
 		startItem.setMnemonic(KeyEvent.VK_N);
 		quitItem.setMnemonic(KeyEvent.VK_Q);
 
 		startItem.setAccelerator(KeyStroke.getKeyStroke(
 			KeyEvent.VK_N, ActionEvent.ALT_MASK));
 		pauseItem.setAccelerator(KeyStroke.getKeyStroke(
 			KeyEvent.VK_P, ActionEvent.ALT_MASK));
 		quitItem.setAccelerator(KeyStroke.getKeyStroke(
 			KeyEvent.VK_Q, ActionEvent.ALT_MASK));
 
 		gameMenu.add(startItem);
 		gameMenu.add(pauseItem);
 		gameMenu.add(quitItem);
 
 		JMenuItem settingsItem = new JMenuItem("Settings");
 		editMenu.add(settingsItem);
 
 		startItem.addActionListener(new NewGameActionListener(commandlist));
 		pauseItem.addActionListener(new PauseActionListener(commandlist));
 		quitItem.addActionListener(new QuitActionListener(commandlist));
 		settingsItem.addActionListener(new ShowSettingsActionListener(commandlist, settingsWindow));
 
 		frame.setJMenuBar(menubar);
 	}
 
 	@Override
 	public void updateState(GameState state) {
 		area.setRenderGrid(state.getRenderGrid());
 		area.repaint();
 
 	}
 
 }
