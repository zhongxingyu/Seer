 package game.core.swing;
 
 import game.common.IPlayerDescription;
 import game.config.IGameConfiguration;
 import game.gameclient.IClientGameCreator;
 import game.gameclient.IGameCreatorChangeListener;
 
 import java.awt.BorderLayout;
 import java.awt.GridLayout;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.util.Set;
 
 import javax.swing.JButton;
 import javax.swing.JCheckBox;
 import javax.swing.JComponent;
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 import javax.swing.border.EmptyBorder;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import com.google.common.eventbus.Subscribe;
 import common.config.swing.ConfigurationPanel;
 import common.swing.IDisposableFrame;
 
 /**
  * Panel used to create a game.
  * 
  * @author benobiwan
  * 
  */
 public final class GameCreationPanel extends JPanel implements
 		IGameCreatorChangeListener, ActionListener
 {
 	/**
 	 * serialVersionUID for Serialization.
 	 */
 	private static final long serialVersionUID = -8995089599592601506L;
 
 	/**
 	 * Logger object.
 	 */
 	private static final Logger LOGGER = LoggerFactory
 			.getLogger(GameCreationPanel.class);
 
 	/**
 	 * Action command for the create AI action.
 	 */
 	private static final String ADD_AI_ACTION_COMMAND = "add";
 
 	/**
 	 * Action command for the leave game action.
 	 */
 	private static final String LEAVE_GAME_ACTION_COMMAND = "leave";
 
 	/**
 	 * Action command for the start game action.
 	 */
 	private static final String START_GAME_ACTION_COMMAND = "start";
 
 	/**
 	 * The {@link ConfigurationPanel} used to display the configuration of the
 	 * game.
 	 */
 	private final ConfigurationPanel _confPanel;
 
 	/**
 	 * The {@link PlayerListPanel} used to display the list of players in the
 	 * game.
 	 */
 	private final PlayerListPanel _playerListPanel;
 
 	/**
 	 * The {@link IClientGameCreator} of this game.
 	 */
 	protected final IClientGameCreator<?, ?, ?, ?, ?, ?> _gameCreator;
 
 	/**
 	 * Boolean showing whether the local client is the creator of this game or
 	 * not.
 	 */
 	protected final boolean _bCreator;
 
 	/**
 	 * Frame containing this panel.
 	 */
 	private final IDisposableFrame _parentFrame;
 
 	/**
 	 * Check box for the ready status.
 	 */
 	private final JCheckBox _readyCheckBox;
 
 	/**
 	 * Creates a new GameCreationPanel.
 	 * 
 	 * @param parentFrame
 	 *            the frame containing this panel.
 	 * @param gameCreator
 	 *            the {IClientGameCreator} to display.
 	 */
 	public GameCreationPanel(final IDisposableFrame parentFrame,
 			final IClientGameCreator<?, ?, ?, ?, ?, ?> gameCreator)
 	{
 		super(new BorderLayout());
 		_parentFrame = parentFrame;
 		_gameCreator = gameCreator;
 		_confPanel = new ConfigurationPanel(_gameCreator.getConfiguration(),
 				false);
 		_playerListPanel = new PlayerListPanel();
 		_bCreator = _gameCreator.isCreator();
 		String strLeave;
 
 		JComponent compStart;
 		if (_bCreator)
 		{
 			strLeave = "Cancel Game";
 			_readyCheckBox = null;
 			final JButton buttonCreateGame = new JButton("Start Game");
 			buttonCreateGame.setActionCommand(START_GAME_ACTION_COMMAND);
 			buttonCreateGame.addActionListener(this);
 			compStart = buttonCreateGame;
 		}
 		else
 		{
 			strLeave = "Leave Game";
 			_readyCheckBox = new JCheckBox("Ready");
 			// TODO add control for the ready/not ready status
 			compStart = _readyCheckBox;
 		}
 
 		final JButton buttonAddAI = new JButton("Add AI");
 		buttonAddAI.setActionCommand(ADD_AI_ACTION_COMMAND);
 		buttonAddAI.addActionListener(this);
 
 		final JButton buttonLeaveGame = new JButton(strLeave);
 		buttonLeaveGame.setActionCommand(LEAVE_GAME_ACTION_COMMAND);
 		buttonLeaveGame.addActionListener(this);
 		final JPanel buttonPane = new JPanel(new GridLayout(1, 0, 10, 10));
 		buttonPane.add(buttonAddAI);
 		buttonPane.add(buttonLeaveGame);
 		buttonPane.add(compStart);
 		buttonPane.setBorder(new EmptyBorder(5, 15, 5, 15));
 		add(_confPanel, BorderLayout.CENTER);
 		add(_playerListPanel, BorderLayout.LINE_START);
 		add(buttonPane, BorderLayout.PAGE_END);
 		_gameCreator.registerGameCreatorChangeListener(this);
 	}
 
 	@Override
 	@Subscribe
 	public void setConfiguration(final IGameConfiguration<?> gameConfiguration)
 	{
 		_confPanel.setConfiguration(gameConfiguration);
 		changeReadyStatus(false);
 	}
 
 	@Override
 	@Subscribe
 	public void setClientSidePlayerList(final Set<IPlayerDescription> playerList)
 	{
 		_playerListPanel.updatePlayerList(playerList);
 		changeReadyStatus(false);
 	}
 
 	/**
 	 * Asks whether the player want to exit the creation of this game. Cancels
 	 * it if the player is creator.
 	 */
 	public void askQuitGameCreation()
 	{
 		String strQuestion;
 		if (_bCreator)
 		{
 			strQuestion = "Do you really want to destroy this game?";
 		}
 		else
 		{
 			strQuestion = "Do you really want to leave this game?";
 		}
 		final int iAnswer = JOptionPane.showConfirmDialog(this, strQuestion,
 				"Exit", JOptionPane.YES_NO_OPTION);
 		switch (iAnswer)
 		{
 		case JOptionPane.YES_OPTION:
 			_gameCreator.leaveGameCreation();
 			_parentFrame.dispose();
 			break;
 		case JOptionPane.NO_OPTION:
 		case JOptionPane.CLOSED_OPTION:
 		default:
 			break;
 		}
 	}
 
 	/**
 	 * Change the ready status of the player.
 	 * 
 	 * @param bReadyStatus
 	 *            the new ready status.
 	 */
 	private void changeReadyStatus(final boolean bReadyStatus)
 	{
 		if (_readyCheckBox != null)
 		{
 			if (bReadyStatus != _readyCheckBox.isSelected())
 			{
 				_readyCheckBox.setSelected(bReadyStatus);
 			}
 		}
 	}
 
 	@Override
 	public void actionPerformed(final ActionEvent e)
 	{
 		switch (e.getActionCommand())
 		{
 		case ADD_AI_ACTION_COMMAND:
 			// TODO ask for AI name
 			_gameCreator.addAI("AI");
 			break;
 		case LEAVE_GAME_ACTION_COMMAND:
 			askQuitGameCreation();
 			break;
 		case START_GAME_ACTION_COMMAND:
 			if (_bCreator)
 			{
 				if (_gameCreator.isGameReady())
 				{
 					_parentFrame.dispose();
					if (_gameCreator.tryLaunchGame())
 					{
 						LOGGER.error("Couldn't load game.");
 					}
 				}
 				else
 				{
					LOGGER.info("Game isn't ready");
 				}
 			}
 			else
 			{
 				_gameCreator.updateReadyStatus(true);
 			}
 			break;
 		}
 	}
 }
