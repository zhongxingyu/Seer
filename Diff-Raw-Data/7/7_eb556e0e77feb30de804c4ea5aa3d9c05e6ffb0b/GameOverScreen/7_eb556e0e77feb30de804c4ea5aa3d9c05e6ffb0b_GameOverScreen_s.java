 package com.punchline.NinjaSpacePirate.screens;
 
 import com.punchline.NinjaSpacePirate.gameplay.GameScore;
 import com.punchline.NinjaSpacePirate.gameplay.Stats;
 import com.punchline.NinjaSpacePirate.gameplay.stats.IntStat;
 import com.punchline.NinjaSpacePirate.screens.MenuButton.ButtonCallback;
 import com.punchline.javalib.Game;
 import com.punchline.javalib.utils.Display;
 import com.punchline.javalib.utils.SoundManager;
 
 public class GameOverScreen extends MenuScreen {
 
 	private static final float TIME_PER_NUMBER = 0.005f;
 	private static final float STAT_DELAY = 0.5f;
 	
 	private static final int PRE_COUNT = -1;
 	private static final int METERS = 0;
 	private static final int COINS = 1;
 	private static final int POTIONS = 2;
 	private static final int KILLS = 3;
 	private static final int SCORE_PARTS = 4;
 	
 	private float elapsedTime = 0f;
 	private int currentCount = 0;
 	private int totalCount = 0;
 	
 	private float delay;
 	
 	private int currentStat = PRE_COUNT;
 	
 	private GameScore score;
 	
 	private boolean skipCurrentStat;
 	
 	private MenuButton[] scoreParts;
 	
 	private MenuButton scoreCount;
 	private MenuButton gratzButton;
 	
 	private MenuButton retryButton;
 	private MenuButton backButton;
 	
 	public GameOverScreen(Game game, GameScore score) {
 		super(game, "Game Over");
 		
 		this.score = score;
 		
 		titleY = 7 * Display.getPreferredHeight() / 8;
 		buttonY = 2 * Display.getPreferredHeight() / 3;
 		buttonScale = 0.6f;
 		
 		scoreParts = new MenuButton[SCORE_PARTS];
 		scoreParts[METERS] = new MenuButton(font, "");
 		scoreParts[COINS] = new MenuButton(font, "");;
 		scoreParts[POTIONS] = new MenuButton(font, "");
 		scoreParts[KILLS] = new MenuButton(font, "");
 		scoreCount = new MenuButton(font, "");
 		gratzButton = new MenuButton(font, "");
 		retryButton = new MenuButton(font, "");
 		backButton = new MenuButton(font, "");
 		
 		retryButton.onTrigger = new ButtonCallback() {
 
 			@Override
 			public void invoke(Game game) {
 				game.getScreenManager().closeActiveScreen();
 				game.getScreenManager().addScreen(new GameplayScreen(game));
 			}
 			
 		};
 		
 		backButton.onTrigger = new ButtonCallback() {
 
 			@Override
 			public void invoke(Game game) {
 				game.getScreenManager().closeActiveScreen();
 			}
 			
 		};
 		
 		for (MenuButton button : scoreParts) {
 			buttons.add(button);
 		}
 		
 		buttons.add(scoreCount);
 		buttons.add(gratzButton);
 		buttons.add(new MenuButton(font, ""));
 		buttons.add(retryButton);
 		buttons.add(backButton);
 	}
 
 	@Override
 	public void render(float delta) {
 		super.render(delta);
 		
 		if (delay == 0f) {
 			elapsedTime += delta;
 		}
 		
		int currentMax;
 		String partString = "";
 		int amount = 0;
 		int mod = 0;
 		
 		switch (currentStat) {
 		
 		case PRE_COUNT:
 			currentMax = 0;
 			break;
 		
 		case METERS:
 			currentMax = score.meters;
 			partString = "METERS RUN";
 			amount = score.meters;
 			mod = GameScore.POINTS_PER_METER;
 			break;
 			
 		case COINS:
 			currentMax = score.coins;
 			partString = "COINS COLLECTED";
 			amount = score.coins;
 			mod = GameScore.POINTS_PER_COIN;
 			break;
 		
 		case POTIONS:
 			currentMax = score.potions;
 			partString = "POTIONS DRUNK";
 			amount = score.potions;
 			mod = GameScore.POINTS_PER_POTION;
 			break;
 			
 		case KILLS:
 			currentMax = score.kills;
 			partString = "KILLS";
 			amount = score.kills;
 			mod = GameScore.POINTS_PER_KILL;
 			break;
 			
 		default:
 			Stats.loadStats();
 			IntStat high = (IntStat) Stats.getRecord("High Score");
 			if (score.totalPoints() > high.getValue()) {
 				high.setValue(score.totalPoints());
 				Stats.saveStats();
 				gratzButton.setText("(High Score)");
 			}
 			
 			retryButton.setText("Retry");
 			backButton.setText("Back");
 			return; //do no more processing
 			
 		}
 		
 		currentMax *= mod;
 		
 		if (skipCurrentStat) {
 			 totalCount += currentMax - currentCount;
 			 currentCount = currentMax;
 			 skipCurrentStat = false;
 		}
 		
 		while (elapsedTime >= TIME_PER_NUMBER && currentCount < currentMax) {
 			currentCount++;
 			totalCount++;
 			
 			SoundManager.playSound("Button_Hover");
 			
 			elapsedTime -= TIME_PER_NUMBER;
 		}
 		
 		if (currentStat >= 0) {
 			scoreParts[currentStat].setText(partString + ": " + amount + " x " + mod);
 		}
 		
 		if (currentCount >= currentMax && delay == 0f) {
 			delay = STAT_DELAY;
 		}
 		
 		if (delay > 0) {
 			delay -= delta;
 			
 			if (delay <= 0 && currentCount == currentMax) {
 				//start the next count
 				currentStat++;
 				currentCount = 0;
 				SoundManager.playSound("Button_Press");
 				delay = 0f;
 			}
 		}
 		
 		scoreCount.setText("Points: " + totalCount);
 	}
 
 	@Override
 	public boolean touchDown(int screenX, int screenY, int pointer, int button) {
 		super.touchDown(screenX, screenY, pointer, button);
 		
 		//start the next count
 		if (currentStat < 4) {
 			currentStat++;
 			currentCount = 0;
 			SoundManager.playSound("Button_Press");
 			delay = 0f;
 		}
 		
 		return true;
 	}
 	
 	
 
 }
