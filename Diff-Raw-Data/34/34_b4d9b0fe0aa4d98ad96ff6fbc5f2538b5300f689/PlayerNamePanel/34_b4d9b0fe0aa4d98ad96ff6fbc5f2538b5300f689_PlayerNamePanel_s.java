 package com.christophdietze.jack.client.view;
 
 import com.christophdietze.jack.client.event.GameUpdatedEvent;
 import com.christophdietze.jack.client.event.GameUpdatedEventHandler;
 import com.christophdietze.jack.client.event.MatchEndedEvent;
 import com.christophdietze.jack.client.event.MatchEndedEventHandler;
 import com.christophdietze.jack.client.event.MatchStartedEvent;
 import com.christophdietze.jack.client.event.MatchStartedEventHandler;
 import com.christophdietze.jack.client.presenter.GameModeManager;
 import com.christophdietze.jack.client.presenter.MatchInfo;
 import com.christophdietze.jack.client.presenter.MatchMode;
 import com.christophdietze.jack.client.resources.MyClientBundle;
 import com.christophdietze.jack.client.util.GlobalEventBus;
 import com.christophdietze.jack.common.board.Game;
 import com.google.gwt.core.client.GWT;
 import com.google.gwt.resources.client.CssResource;
 import com.google.gwt.uibinder.client.UiBinder;
 import com.google.gwt.uibinder.client.UiField;
 import com.google.gwt.user.client.ui.Composite;
 import com.google.gwt.user.client.ui.Image;
 import com.google.gwt.user.client.ui.Label;
 import com.google.gwt.user.client.ui.Widget;
 import com.google.inject.Inject;
 
 public class PlayerNamePanel extends Composite {
 
	private static final String WHITE_PLAYER_DEFAULT_NAME = "White player";
 	private static final String BLACK_PLAYER_DEFAULT_NAME = "Black player";
 
 	private static MyClientBundle myClientBundle = MyClientBundle.INSTANCE;
 
 	private static MyUiBinder uiBinder = GWT.create(MyUiBinder.class);
 
 	interface MyUiBinder extends UiBinder<Widget, PlayerNamePanel> {
 	}
 
 	interface MyStyle extends CssResource {
		String playerToMove();
 	}
 
 	private GlobalEventBus eventBus;
 
 	private GameModeManager gameModeManager;
 
 	private Game game;
 
 	@UiField
 	MyStyle style;
 	@UiField
 	Label upperPlayerLabel;
 	@UiField
 	Label lowerPlayerLabel;
 	@UiField
 	Image upperPlayerIcon;
 	@UiField
 	Image lowerPlayerIcon;
 
 	@Inject
 	public PlayerNamePanel(GlobalEventBus eventBus, GameModeManager gameModeManager, Game game) {
 		this.eventBus = eventBus;
 		this.gameModeManager = gameModeManager;
 		this.game = game;
 		initWidget(uiBinder.createAndBindUi(this));
 		upperPlayerLabel.setText(BLACK_PLAYER_DEFAULT_NAME);
 		lowerPlayerLabel.setText(WHITE_PLAYER_DEFAULT_NAME);
 		upperPlayerIcon.setResource(myClientBundle.blackPlayerIcon());
 		lowerPlayerIcon.setResource(myClientBundle.whitePlayerIcon());
 		initListeners();
 		updatePlayerNameHighlighting();
 	}
 
 	private void initListeners() {
 		eventBus.addHandler(MatchStartedEvent.TYPE, new MatchStartedEventHandler() {
 			@Override
 			public void onMatchStarted(MatchStartedEvent event) {
 				MatchMode matchMode = (MatchMode) gameModeManager.getCurrentMode();
 				MatchInfo matchInfo = matchMode.getMatchInfo();
 				if (matchInfo.isPlayerWhite() == game.isWhiteAtBottom()) {
 					upperPlayerLabel.setText("Guest" + matchInfo.getBlackPlayerId());
 					lowerPlayerLabel.setText("You");
 				} else {
 					upperPlayerLabel.setText("You");
 					lowerPlayerLabel.setText("Guest" + matchInfo.getWhitePlayerId());
 				}
 				upperPlayerIcon.setResource(game.isWhiteAtBottom() ? myClientBundle.blackPlayerIcon() : myClientBundle
 						.whitePlayerIcon());
 				lowerPlayerIcon.setResource(game.isWhiteAtBottom() ? myClientBundle.whitePlayerIcon() : myClientBundle
 						.blackPlayerIcon());
 			}
 		});
 		eventBus.addHandler(MatchEndedEvent.TYPE, new MatchEndedEventHandler() {
 			@Override
 			public void onMatchEnded(MatchEndedEvent event) {
 				if (game.isWhiteAtBottom()) {
 					upperPlayerLabel.setText(BLACK_PLAYER_DEFAULT_NAME);
 					lowerPlayerLabel.setText(WHITE_PLAYER_DEFAULT_NAME);
 				} else {
 					upperPlayerLabel.setText(WHITE_PLAYER_DEFAULT_NAME);
 					lowerPlayerLabel.setText(BLACK_PLAYER_DEFAULT_NAME);
 				}
 			}
 		});
 		eventBus.addHandler(GameUpdatedEvent.TYPE, new GameUpdatedEventHandler() {
 			@Override
 			public void onUpdate(GameUpdatedEvent event) {
 				updatePlayerNameHighlighting();
 			}
 		});
 	}
 
 	private void updatePlayerNameHighlighting() {
 		if (game.getPosition().isWhiteToMove() == game.isWhiteAtBottom()) {
			upperPlayerLabel.removeStyleName(style.playerToMove());
			lowerPlayerLabel.addStyleName(style.playerToMove());
 		} else {
			upperPlayerLabel.addStyleName(style.playerToMove());
			lowerPlayerLabel.removeStyleName(style.playerToMove());
 		}
 	}
 }
