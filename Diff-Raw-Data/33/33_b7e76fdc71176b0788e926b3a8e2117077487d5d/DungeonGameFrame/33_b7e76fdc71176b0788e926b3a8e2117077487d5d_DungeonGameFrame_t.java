 package front;
 
 import static professorShipSrc.ImageUtils.loadImage;
 
 import java.awt.BorderLayout;
 import java.awt.Color;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.KeyAdapter;
 import java.awt.event.KeyEvent;
 import java.io.File;
 import java.io.IOException;
 
 import javax.swing.JFileChooser;
 import javax.swing.JOptionPane;
 
 import parser.BoardParserFromFile;
 import parser.CorruptedFileException;
 import saveLoadImplementation.LoadGameFromFile;
 import saveLoadImplementation.SaveGameOnFile;
 import saveLoadImplementation.SavingCorruptedException;
 import back.BoardObtainer;
 import back.DungeonGameImp;
 import back.DungeonGameListener;
 import back.LoadGame;
 import back.Monster;
 import back.MoveTypes;
 import back.Point;
 import back.Putable;
 
 /**
  * @author tmehdi Class that extends GameFrame. It's used for the frame of the
  *         game.
  */
 public class DungeonGameFrame extends GameFrame {
 
 	private static final long serialVersionUID = 1L;
 	private DataPanel dataPanel;
 	private DungeonPanel dungeonPanel;
 
 	public DungeonGameFrame() {
 		super("Dungeon game");
 		setIcon();
 		addKeyListener();
 	}
 
 	/**
 	 * DungeonGameFrame menu. It have 6 options: New game, Restart,Save game,
 	 * Save game as..., Load game and Exit
 	 * 
 	 * @see front.GameFrame#createDefaultJMenuActionListeners()
 	 **/
 	@Override
 	public void createDefaultJMenuActionListeners() {
 
 		setNewGameItemAction(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				try {
 					if (game != null) {
 						dataPanel.setVisible(false);
 						dungeonPanel.setVisible(false);
 						remove(dataPanel);
 						remove(dungeonPanel);
 						repaint();
 						game = null;
 					}
 					File file = null;
 					LevelSelector levelSelector = new LevelSelectorImp(
 							DungeonGameFrame.this);
 					file = levelSelector.getLevelSelected();
 					if (file != null) {
 						BoardObtainer boardObtainer = new BoardParserFromFile(
 								file);
 						game = new DungeonGameImp(boardObtainer,
 								new DungeonGameListenerImp());
 						setSize();
 						drawDungeonPanel();
 						drawDataPanel();
 						dataPanel.refresh(game, dungeonPanel);
 						dungeonPanel.updateUI();
 					}
 				} catch (Exception e1) {
 					JOptionPane.showMessageDialog(null,
 							"Level file is corrupt", "Error",
 							JOptionPane.ERROR_MESSAGE);
 				}
 			}
 
 		});
 
 		setRestartGameItemAction(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				try {
 					if (game == null) {
 						JOptionPane.showMessageDialog(null,
 								"You are not playing a level.");
 					} else {
 						game.restart();
 						dataPanel.setVisible(false);
 						dungeonPanel.setVisible(false);
 						remove(dataPanel);
 						remove(dungeonPanel);
 						drawDungeonPanel();
 						drawDataPanel();
 						dataPanel.refresh(game, dungeonPanel);
 						dungeonPanel.updateUI();
 					}
 				} catch (CorruptedFileException e1) {
 					JOptionPane.showMessageDialog(null, "The file is corrupt",
 							"Error", JOptionPane.ERROR_MESSAGE);
 				}
 			}
 		});
 
 		setSaveGameItemAction(new ActionListener() {
 
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				if (game != null) {
 					File directory = new File("." + File.separator
 							+ "savedGames");
 					if (!directory.exists()) {
 						directory.mkdir();
 					}
 					try {
 						new SaveGameOnFile(game);
 					} catch (SavingCorruptedException e1) {
 						JOptionPane.showMessageDialog(null,
 								"Files saving error occours. Try again later.",
 								"Error", JOptionPane.ERROR_MESSAGE);
 					}
 				}
 			}
 		});
 
 		setSaveGameAsItemAction(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				if (game != null) {
 					File directory = new File("." + File.separator
 							+ "savedGames");
 					if (!directory.exists()) {
 						directory.mkdir();
 					}
 					File file;
 					JFileChooser fc = new JFileChooser();
 					fc.setCurrentDirectory(new File("." + File.separator
 							+ "savedGames"));
 					fc.showOpenDialog(DungeonGameFrame.this);
 					file = fc.getSelectedFile();
 					if (file == null) {
 						JOptionPane.showMessageDialog(null,
 								"You didn't select any file.");
 					} else {
 						try {
 							new SaveGameOnFile(game, file);
 						} catch (SavingCorruptedException e1) {
 							JOptionPane
 									.showMessageDialog(
 											null,
 											"Files saving error occours. Try again later.",
 											"Error", JOptionPane.ERROR_MESSAGE);
 						}
 					}
 				}
 			}
 		});
 
 		setLoadGameItemAction(new ActionListener() {
 
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				if (game != null) {
 					dataPanel.setVisible(false);
 					dungeonPanel.setVisible(false);
 					remove(dataPanel);
 					remove(dungeonPanel);
 					repaint();
 					game = null;
 				}
 				File file;
 				JFileChooser fc = new JFileChooser();
 				fc.setCurrentDirectory(new File("." + File.separator
 						+ "savedGames"));
 				fc.showOpenDialog(DungeonGameFrame.this);
 				file = fc.getSelectedFile();
 				if (file == null) {
 					JOptionPane.showMessageDialog(null,
 							"You didn't select any file.");
 				} else {
 					try {
 						LoadGame<DungeonGameImp> loadGame = new LoadGameFromFile<DungeonGameImp>(
 								file);
 						game = loadGame.getGame(DungeonGameImp.class,
 								new DungeonGameListenerImp());
 						setSize();
 						drawDungeonPanel();
 						drawDataPanel();
 						dataPanel.updateUI();
 						dungeonPanel.updateUI();
 					} catch (CorruptedFileException e2) {
 						JOptionPane
 								.showMessageDialog(
 										null,
 										"Files loading error occours. Try again later.",
 										"Error", JOptionPane.ERROR_MESSAGE);
 					}
 				}
 			}
 		});
 
 		setExitGameItemAction(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				try {
 					DungeonGameFrame.this.setVisible(false);
 					DungeonGameFrame.this.dispose();
 				} catch (Throwable e1) {
 					JOptionPane.showMessageDialog(null, "Exit fault", "Error",
 							JOptionPane.ERROR_MESSAGE);
 				}
 			}
 		});
 
 	}
 
 	private void setSize() {
 		setSize((game.getBoardDimension().y + 2) * DungeonPanel.CELL_SIZE,
 				(game.getBoardDimension().x) * DungeonPanel.CELL_SIZE );
 	}
 
 	/**
 	 * Method to make appear the data panel.
 	 */
 	private void drawDataPanel() {
 		dataPanel = new DataPanel(game.getPlayer(), Color.GRAY);
 		add(dataPanel, BorderLayout.EAST);
 	}
 
 	/**
 	 * Method to make appear the dungeon panel.
 	 */
 	private void drawDungeonPanel() {
 		dungeonPanel = new DungeonPanel(game, dataPanel,
 				new DungeonPanelListenerImp());
 		add(dungeonPanel, BorderLayout.CENTER);
 	}
 
 	/**
 	 * Getter of the dungeon panel.
 	 * 
 	 * @return DungeonPanel
 	 */
 	public DungeonPanel getDungeonPanel() {
 		return dungeonPanel;
 	}
 
 	/**
 	 * Getter of the data panel.
 	 * 
 	 * @return DataPanel
 	 */
 	public DataPanel getDataPanel() {
 		return dataPanel;
 	}
 
 	/**
 	 * Listener of the move keys, up down left right.
 	 * 
 	 * @see front.GameFrame#addKeyListener()
 	 **/
 	@Override
 	public void addKeyListener() {
 
 		addKeyListener(new KeyAdapter() {
 
 			@Override
 			public void keyPressed(final KeyEvent e) {
 				switch (e.getKeyCode()) {
 				case KeyEvent.VK_LEFT:
 					game.receiveMoveStroke(MoveTypes.LEFT);
 
 					break;
 				case KeyEvent.VK_UP:
 					game.receiveMoveStroke(MoveTypes.UP);
 
 					break;
 				case KeyEvent.VK_RIGHT:
 					game.receiveMoveStroke(MoveTypes.RIGHT);
 
 					break;
 				case KeyEvent.VK_DOWN:
 					game.receiveMoveStroke(MoveTypes.DOWN);
 
 					break;
 				}
 			}
 		});
 	}
 
 	/**
 	 * @author tmehdi Inner class for the listener of this game implementation.
 	 */
 	private class DungeonGameListenerImp implements DungeonGameListener {
 
 		@Override
 		public void executeWhenBonusGrabed(Point p) {
 			dungeonPanel.drawGrabedBonus(p);
 		}
 
 		@Override
 		public void executeWhenCharacterDie(Point p) {
 			dungeonPanel.drawDiedCharacter(p);
 		}
 
 		@Override
 		public void executeWhenGameLoosed() {
 			JOptionPane.showMessageDialog(DungeonGameFrame.this,
 					"You loose the level.");
 			DungeonGameFrame.this.remove(DungeonGameFrame.this
 					.getDungeonPanel());
 			DungeonGameFrame.this.remove(DungeonGameFrame.this.getDataPanel());
 			repaint();
 		}
 
 		@Override
 		public void executeWhenGameWinned() {
 			JOptionPane.showMessageDialog(DungeonGameFrame.this, "WINNER!"
 					+ '\n' + "You win the level with "
 					+ game.getPlayer().getSteps() + " steps.");
 			DungeonGameFrame.this.remove(DungeonGameFrame.this
 					.getDungeonPanel());
 			DungeonGameFrame.this.remove(DungeonGameFrame.this.getDataPanel());
 			repaint();
 		}
 
 		@Override
 		public void executeWhenPlayerMoves(MoveTypes moveType) {
 			dungeonPanel.drawPlayerMove(game, moveType);
 			dataPanel.refresh(game, dungeonPanel);
 			dataPanel.updateUI();
 			dungeonPanel.drawDiscoveredCell(game, moveType);
 		}
 
 		@Override
 		public String playerNameRequest() {
 			String name = null;
 			while (name == null || name.isEmpty()) {
 				name = JOptionPane.showInputDialog("Player name");
 			}
 			return name;
 		}
 
 		@Override
 		public void executeWhenFight() {
 			dataPanel.refresh(game, dungeonPanel);
 			dataPanel.updateUI();
 		}
 
 		@Override
 		public void executeWhenLevelUp() {
 			dungeonPanel.drawLevelUp(game);
 		}
 	}
 
 	/**
 	 * Add the hero image as frame icon.
 	 */
 	private void setIcon() {
 		try {
			setIconImage(loadImage("resources/images/hero.png"));
 		} catch (IOException e) {
 			JOptionPane.showMessageDialog(null, "Unexpected Error", "Error",
 					JOptionPane.ERROR_MESSAGE);
 		}
 	}
 
 	/**
 	 * @author tomas Implementation of DungeonPaneListener used for the actions
 	 *         performed on dungeonPanel with the mouse.
 	 */
 	private class DungeonPanelListenerImp implements DungeonPanelListener {
 
 		@Override
 		public void onMouseMoved(int row, int column) {
 
 			Monster monster = dungeonPanel.getMonsterUnderMouse();
 			if (monster != null) {
 				dataPanel.removeCharacter(monster);
 				dungeonPanel.setMonsterUnderMouse(null);
 			}
 			Putable putable = game.getBoard()[row + 1][column + 1];
 			if (putable instanceof Monster && putable.isVisible()) {
 				dungeonPanel.setMonsterUnderMouse((Monster) putable);
 				dataPanel.addCharacter(dungeonPanel.getMonsterUnderMouse());
 			}
 			dataPanel.refresh(game, dungeonPanel);
 			dataPanel.updateUI();
 
 		}
 
 	}
 }
