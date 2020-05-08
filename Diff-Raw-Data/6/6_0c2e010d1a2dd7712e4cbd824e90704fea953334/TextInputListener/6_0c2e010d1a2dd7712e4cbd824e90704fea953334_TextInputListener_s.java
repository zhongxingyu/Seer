 package org.opendarts.ui.x01.utils;
 
 import org.eclipse.jface.fieldassist.ControlDecoration;
 import org.eclipse.jface.fieldassist.FieldDecoration;
 import org.eclipse.jface.fieldassist.FieldDecorationRegistry;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.events.FocusEvent;
 import org.eclipse.swt.events.FocusListener;
 import org.eclipse.swt.events.KeyEvent;
 import org.eclipse.swt.events.KeyListener;
 import org.eclipse.swt.events.SelectionEvent;
 import org.eclipse.swt.events.SelectionListener;
 import org.eclipse.swt.widgets.Shell;
 import org.eclipse.swt.widgets.Text;
 import org.opendarts.core.model.dart.InvalidDartThrowException;
 import org.opendarts.core.model.dart.impl.ThreeDartsThrow;
 import org.opendarts.core.model.player.IPlayer;
 import org.opendarts.core.service.game.IGameService;
 import org.opendarts.core.x01.model.GameX01;
 import org.opendarts.core.x01.model.WinningX01DartsThrow;
 import org.opendarts.ui.x01.utils.shortcut.X01Shortcuts;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 /**
  * Handle input text.
  */
 public class TextInputListener implements FocusListener, SelectionListener,
 		KeyListener {
 
 	/** The logger. */
 	private static final Logger LOG = LoggerFactory
 			.getLogger(TextInputListener.class);
 
 	/** The game. */
 	private final GameX01 game;
 
 	/** The player. */
 	private final IPlayer player;
 
 	/** The game service. */
 	private final IGameService gameService;
 
 	/** The dart throw util. */
 	private final DartThrowUtil dartThrowUtil;
 
 	/** The input text. */
 	private final Text inputText;
 
 	/** The decoration. */
 	private final ControlDecoration decoration;
 
 	/** The shortcuts. */
 	private final X01Shortcuts shortcuts;
 
 	/** The shell. */
 	private final Shell shell;
 
 	/**
 	 * Instantiates a new text input listener.
 	 *
 	 * @param parentShell the parent shell
 	 * @param game the game
 	 * @param player the player
 	 * @param dec 
 	 */
 	public TextInputListener(Shell parentShell, Text inputText, GameX01 game,
 			IPlayer player, ControlDecoration dec) {
 		super();
 		this.shell = parentShell;
 		this.game = game;
 		this.player = player;
 		this.inputText = inputText;
 		this.decoration = dec;
 		this.dartThrowUtil = new DartThrowUtil(parentShell, game, player);
 		this.gameService = game.getParentSet().getGameService();
 
 		this.shortcuts = X01Shortcuts.getX01Shortcuts();
 	}
 
 	/* (non-Javadoc)
 	 * @see org.eclipse.swt.events.FocusListener#focusGained(org.eclipse.swt.events.FocusEvent)
 	 */
 	@Override
 	public void focusGained(FocusEvent e) {
 		// Nothing to do
 	}
 
 	/* (non-Javadoc)
 	 * @see org.eclipse.swt.events.FocusListener#focusLost(org.eclipse.swt.events.FocusEvent)
 	 */
 	@Override
 	public void focusLost(FocusEvent e) {
 		this.handleNewValue(this.inputText.getText());
 	}
 
 	/* (non-Javadoc)
 	 * @see org.eclipse.swt.events.SelectionListener#widgetDefaultSelected(org.eclipse.swt.events.SelectionEvent)
 	 */
 	@Override
 	public void widgetDefaultSelected(SelectionEvent e) {
 		// Nothing to do
 	}
 
 	/* (non-Javadoc)
 	 * @see org.eclipse.swt.events.SelectionListener#widgetSelected(org.eclipse.swt.events.SelectionEvent)
 	 */
 	@Override
 	public void widgetSelected(SelectionEvent e) {
 		this.handleNewValue(this.inputText.getText());
 	}
 
 	/* (non-Javadoc)
 	 * @see org.eclipse.swt.events.KeyListener#keyPressed(org.eclipse.swt.events.KeyEvent)
 	 */
 	@Override
 	public void keyPressed(KeyEvent e) {
 		this.shortcuts.handleKeyEvent(e, this.shell, this.inputText, this.game,
 				this.player, this.decoration);
 		switch (e.keyCode) {
			case 9: // Tab
			case 32: // Space
			case 13: // Enter
 			case SWT.KEYPAD_CR:
 				this.handleNewValue(this.inputText.getText());
 				break;
 			default:
 				break;
 		}
 	}
 
 	/* (non-Javadoc)
 	 * @see org.eclipse.swt.events.KeyListener#keyReleased(org.eclipse.swt.events.KeyEvent)
 	 */
 	@Override
 	public void keyReleased(KeyEvent e) {
 		// Nothing to do
 	}
 
 	/**
 	 * Handle new value.
 	 *
 	 * @param value the value
 	 */
 	private void handleNewValue(String value) {
 		this.decoration.hide();
 		if (!"".equals(value)) {
 			Integer leftScore = this.game.getScore(this.player);
 			ThreeDartsThrow dartThrow;
 			try {
 				dartThrow = this.dartThrowUtil.getDartThrow(value, leftScore);
 				if (dartThrow == null) {
 					this.inputText.setFocus();
 					this.inputText.selectAll();
 				} else if (dartThrow instanceof WinningX01DartsThrow) {
 					this.gameService.addWinningPlayerThrow(this.game,
 							this.player, dartThrow);
 				} else {
 					this.gameService.addPlayerThrow(this.game, this.player,
 							dartThrow);
 				}
 			} catch (NumberFormatException e) {
 				LOG.warn("Invalid format: " + e);
 				String msg = "Not a number!";
 				this.applyError(msg);
 			} catch (InvalidDartThrowException e) {
 				String msg = e.getMessage();
 				this.applyError(msg);
 			}
 		} else {
 			this.inputText.setFocus();
 		}
 	}
 
 	/**
 	 * Apply error.
 	 *
 	 * @param msg the message
 	 */
 	private void applyError(String msg) {
 		// Decoration
 		FieldDecoration errorFieldIndicator = FieldDecorationRegistry
 				.getDefault().getFieldDecoration(
 						FieldDecorationRegistry.DEC_ERROR);
 		this.decoration.setImage(errorFieldIndicator.getImage());
 		this.decoration.setDescriptionText(msg);
 		this.decoration.setImage(errorFieldIndicator.getImage());
 		this.decoration.show();
 
 		// handler input text
 		this.inputText.setFocus();
 		this.inputText.selectAll();
 	}
 }
