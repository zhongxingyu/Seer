 /*****************************************************************************
  * Schaltwerk - A free and extensible digital simulator
  * Copyright (c) 2013 Christian Wichmann
  * 
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  * 
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  * 
  * You should have received a copy of the GNU General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>. 
  *****************************************************************************/
 package de.ichmann.java.schaltwerk.gui;
 
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.KeyEvent;
 import java.awt.event.WindowEvent;
 import java.awt.event.WindowListener;
 import java.beans.PropertyVetoException;
 
 import javax.swing.AbstractAction;
 import javax.swing.JButton;
 import javax.swing.JDesktopPane;
 import javax.swing.JFrame;
 import javax.swing.JMenu;
 import javax.swing.JMenuBar;
 import javax.swing.JMenuItem;
 import javax.swing.JToolBar;
 import javax.swing.KeyStroke;
 import javax.swing.SwingUtilities;
 import javax.swing.UIManager;
 import javax.swing.UIManager.LookAndFeelInfo;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import de.ichmann.java.schaltwerk.blocks.AND;
 import de.ichmann.java.schaltwerk.blocks.Block;
 import de.ichmann.java.schaltwerk.blocks.BlockFactory;
 import de.ichmann.java.schaltwerk.blocks.Blocks;
 import de.ichmann.java.schaltwerk.blocks.NAND;
 import de.ichmann.java.schaltwerk.blocks.NOR;
 import de.ichmann.java.schaltwerk.blocks.NOT;
 import de.ichmann.java.schaltwerk.blocks.OR;
 
 /**
  * Main frame for Schaltwerk displaying menu bar, status bar and circuit panels
  * etc.
  * 
  * @author Christian Wichmann
  * 
  */
 public class SchaltwerkFrame extends JFrame {
 
 	private static final long serialVersionUID = -9210487604950854484L;
 
 	private static final Logger LOG = LoggerFactory
 			.getLogger(SchaltwerkFrame.class);
 
 	private JDesktopPane desktop;
 
 	private CircuitPanel mainCircuitFrame;
 
 	/**
 	 * Initialize main frame of Schaltwerk.
 	 */
 	public SchaltwerkFrame() {
 
 		setLookAndFeel();
 
 		initialize();
 
 		addListeners();
 
 		addKeyBindings();
 	}
 
 	/**
 	 * Sets look and feel for all swing components to "Nimbus".
 	 */
 	private void setLookAndFeel() {
 
 		// set look and feel to new (since Java SE 6 Update 10 release standard
 		try {
 			for (LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
 				if ("Nimbus".equals(info.getName())) {
 					UIManager.setLookAndFeel(info.getClassName());
 					break;
 				}
 			}
 		} catch (Exception e1) {
 
 		}
 	}
 
 	private void initialize() {
 
 		final int width = 800;
 		final int height = 600;
 
 		// setIconImage(new ImageIcon().getImage());
 		setSize(width, height);
 		setLocationRelativeTo(null);
 		setName("Schaltwerk");
 		setTitle("Schaltwerk");
 		setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
 		setDefaultLookAndFeelDecorated(true);
 
 		desktop = new JDesktopPane();
 		// Make dragging a little faster but perhaps uglier.
 		// desktop.setDragMode(JDesktopPane.OUTLINE_DRAG_MODE);
 		getContentPane().add(desktop);
 		createInternalFrame();
 
 		setJMenuBar(createMenuBar());
 	}
 
 	/**
 	 * Add listeners for handling window events.
 	 */
 	private void addListeners() {
 
 		addWindowListener(new WindowListener() {
 			@Override
 			public void windowOpened(WindowEvent e) {
 			}
 
 			@Override
 			public void windowIconified(WindowEvent e) {
 			}
 
 			@Override
 			public void windowDeiconified(WindowEvent e) {
 			}
 
 			@Override
 			public void windowDeactivated(WindowEvent e) {
 			}
 
 			@Override
 			public void windowClosing(WindowEvent e) {
 				handleQuit();
 			}
 
 			@Override
 			public void windowClosed(WindowEvent e) {
 			}
 
 			@Override
 			public void windowActivated(WindowEvent e) {
 			}
 		});
 	}
 
 	/**
 	 * Adds key bindings for main frame and all its children.
 	 */
 	private void addKeyBindings() {
 
 	}
 
 	/*
 	 * ===== Methods providing ui components =====
 	 */
 
 	private JMenuBar createMenuBar() {
 
 		JMenuBar menuBar = new JMenuBar();
 
 		// ===== file menu =====
 		JMenu fileMenu = new JMenu("File");
 		fileMenu.setMnemonic(KeyEvent.VK_F);
 		menuBar.add(fileMenu);
 
 		// Set up the first menu item.
 		JMenuItem menuItem = new JMenuItem("New");
 		menuItem.setMnemonic(KeyEvent.VK_N);
 		menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N,
 				ActionEvent.ALT_MASK));
 		menuItem.setActionCommand("new");
 		// TODO use named Actions to use them for menu and icon... (setAction())
 		menuItem.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				createInternalFrame();
 			}
 		});
 		fileMenu.add(menuItem);
 		fileMenu.addSeparator();
 
 		// Set up the second menu item.
 		menuItem = new JMenuItem("Quit");
 		menuItem.setMnemonic(KeyEvent.VK_Q);
 		menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Q,
 				ActionEvent.ALT_MASK));
 		menuItem.setActionCommand("quit");
 		menuItem.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				handleQuit();
 			}
 		});
 		fileMenu.add(menuItem);
 
 		// ===== file menu =====
 		JMenu blockMenu = new JMenu("Blocks");
 		blockMenu.setMnemonic(KeyEvent.VK_B);
 		menuBar.add(blockMenu);
 
 		menuItem = new JMenuItem("Add new Block");
 		menuItem.setMnemonic(KeyEvent.VK_B);
 		menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_B,
 				ActionEvent.ALT_MASK));
 		menuItem.setAction(new AddNewBlock("Add New Block"));
 		blockMenu.add(menuItem);
 
 		// ===== help menu =====
 		JMenu helpMenu = new JMenu("Help");
 		helpMenu.setMnemonic(KeyEvent.VK_H);
 		menuBar.add(helpMenu);
 		menuItem = new JMenuItem("About");
 		menuItem.setMnemonic(KeyEvent.VK_A);
 		menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_A,
 				ActionEvent.ALT_MASK));
 		helpMenu.add(menuItem);
 
 		return menuBar;
 	}
 
 	private JToolBar getToolBar() {
 
 		JToolBar toolBar = new JToolBar();
 
 		JButton testButton = new JButton("Test");
 		testButton.setSize(100, 100);
 		testButton.addActionListener(new ActionListener() {
 
 			@Override
 			public void actionPerformed(ActionEvent arg0) {
 
 			}
 		});
 		toolBar.add(testButton);
 		return toolBar;
 	}
 
 	private JToolBar getStatusBar() {
 
 		JToolBar statusBar = new JToolBar();
 		statusBar.add(new JMenuItem("Status: 42"));
 		return statusBar;
 	}
 
 	/**
 	 * Create a new internal frame.
 	 */
 	private void createInternalFrame() {
 
 		mainCircuitFrame = new CircuitPanel("Beispielschaltung.circuit");
 		mainCircuitFrame.setVisible(true);
 		desktop.add(mainCircuitFrame);
 		try {
 			mainCircuitFrame.setSelected(true);
 			mainCircuitFrame.setMaximum(true);
 		} catch (PropertyVetoException e) {
 			LOG.warn("Error encountered while selecting internal frame.");
 		}
 	}
 
 	/*
 	 * ===== Methods handling state of program =====
 	 */
 
 	/**
 	 * Quit the application.
 	 */
 	private void handleQuit() {
 
 		System.exit(0);
 	}
 
 	private class AddNewBlock extends AbstractAction {
 
 		private static final long serialVersionUID = -94858533824980815L;
 
 		public AddNewBlock(String name) {
 			super(name);
 		}
 
 		@Override
 		public void actionPerformed(ActionEvent e) {
 			AddBlockDialog d = new AddBlockDialog();
 			d.setVisible(true);
 			Blocks blockType = d.getChosenBlock();
 			Block block = null;
 			switch (blockType) {
 			case AND:
 				block = new AND(3);
 				break;
 			case NAND:
 				block = new NAND(3);
 				break;
 			case NOR:
 				block = new NOR(3);
 				break;
 			case NOT:
 				block = new NOT();
 				break;
 			case OR:
 				block = new OR(3);
 				break;
 			case RS_FLIPFLOP:
 				block = BlockFactory.getInstance().getRSFlipFLop(false);
 				break;
 			default:
 				assert false : blockType;
 				break;
 			}
 			((CircuitPanel) desktop.getSelectedFrame())
 					.addBlockToCircuit(block);
			((CircuitPanel) desktop.getSelectedFrame()).repaint();
 		}
 	}
 
 	public static void main(String[] args) {
 
 		SwingUtilities.invokeLater(new Runnable() {
 
 			@Override
 			public void run() {
 				new SchaltwerkFrame().setVisible(true);
 			}
 		});
 	}
 }
