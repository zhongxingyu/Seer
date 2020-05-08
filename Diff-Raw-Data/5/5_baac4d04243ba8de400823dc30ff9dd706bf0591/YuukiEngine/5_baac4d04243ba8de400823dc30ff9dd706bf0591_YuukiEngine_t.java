 package yuuki;
 
 import java.util.ArrayList;
 
 import javax.swing.JOptionPane;
 
 import yuuki.action.Action;
 import yuuki.battle.Battle;
 import yuuki.buff.Buff;
 import yuuki.entity.Character;
 import yuuki.entity.EntityFactory;
 import yuuki.entity.NonPlayerCharacter;
 import yuuki.entity.PlayerCharacter;
 import yuuki.sound.SoundEngine;
 import yuuki.ui.GraphicalInterface;
 import yuuki.ui.Interactable;
 import yuuki.ui.UiExecutor;
 
 /**
  * The game engine for the Yuuki JRPG project. This class may be executed
  * directly to run Yuuki.
  */
 public class YuukiEngine implements Runnable, UiExecutor {
 	
 	/**
 	 * Handles the execution of a battle in its own thread.
 	 */
 	private class BattleRunner implements Runnable {
 		private Battle battle;
 		private boolean display;
 		public BattleRunner(Battle battle, boolean display) {
 			this.battle = battle;
 			this.display = display;
 		}
 		@Override
 		public void run() {
 			runBattle(battle, display);
 			requestBattleEnd();
 		}
 	}
 	
 	/**
 	 * Program execution hook. Creates a new instance of YuukiEngine and then
 	 * runs it.
 	 *
 	 * @param args Command line arguments. Not used.
 	 */
 	public static void main(String[] args) {
 		YuukiEngine gameEngine = new YuukiEngine();
 		gameEngine.run();
 	}
 	
 	/**
 	 * The options for the game.
 	 */
 	private GameOptions options;
 	
 	/**
 	 * Creates all entities.
 	 */
 	private EntityFactory entityMaker;
 	
 	/**
 	 * The current battle.
 	 */
 	private Battle mainBattle;
 	
 	/**
 	 * The player character.
 	 */
 	private PlayerCharacter player;;
 	
 	/**
 	 * The user interface.
 	 */
 	private Interactable ui;
 	
 	/**
 	 * The sound engine.
 	 */
 	private SoundEngine soundEngine;	
 	/**
 	 * Creates a new YuukiEngine with a Swing-based GUI.
 	 */
 	public YuukiEngine() {
 		options = new GameOptions();
 		ui = new GraphicalInterface(this, options);
 		entityMaker = new EntityFactory();
 		soundEngine = new SoundEngine();
 		applyOptions();
 	}
 	
 	/**
 	 * @inheritDoc
 	 */
 	@Override
 	public void requestBattle(boolean display) {
 		NonPlayerCharacter slime = entityMaker.createNpc("slime", 2);
 		Character[][] fighters = {{player}, {slime}};
 		Battle battle = new Battle(fighters);
 		if (display) {
 			mainBattle = battle;
 			ui.switchToBattleScreen(fighters);
 		}
 	}
 	
 	/**
 	 * @inheritDoc
 	 */
 	@Override
 	public void requestBattleStart() {
 		spawnBattleThread(mainBattle, true);
 	}
 	
 	/**
 	 * @inheritDoc
 	 */
 	@Override
 	public void requestCharacterCreation(String name, int level) {
 		player = entityMaker.createPlayer(name, level, ui);
 		ui.switchToOverworldScreen();
 	}
 	
 	/**
 	 * @inheritDoc
 	 */
 	@Override
 	public void requestLoadGame() {
		ui.display(null, "Loading hasn't yet been implemented");
 	}
 	
 	/**
 	 * @inheritDoc
 	 */
 	@Override
 	public void requestSaveGame() {
		ui.display(null, "Saving hasn't yet been implemented");
 	}
 	
 	/**
 	 * @inheritDoc
 	 */
 	@Override
 	public void requestNewGame() {
 		ui.switchToCharacterCreationScreen();
 	}
 	
 	/**
 	 * @inheritDoc
 	 */
 	public void requestOptionsSubmission() {
 		// TODO: do not depend on Interactable to set options
 		ui.switchToLastScreen();
 	}
 	
 	/**
 	 * @inheritDoc
 	 */
 	@Override
 	public void requestOptionsScreen() {
 		ui.switchToOptionsScreen();
 	}
 	
 	/**
 	 * @inheritDoc
 	 */
 	@Override
 	public void requestQuit() {
 		int quit = JOptionPane.showConfirmDialog(null,
 				"Are you sure you want to quit?", "Quit Confirmation",
 				JOptionPane.YES_NO_OPTION);
 		if (quit == JOptionPane.YES_OPTION) {
 			ui.destroy();
 			System.exit(0);
 		}
 	}
 	
 	/**
 	 * @inheritDoc
 	 */
 	public void requestBattleEnd() {
 		Character winner = mainBattle.getFighters(0).get(0);
 		ui.getChoice(winner.getName() + " won", new String[]{"Continue"});
 		ui.switchToOverworldScreen();
 		ui.display(null, "Your health has been restored.");
 		player.restoreHP();
 		player.restoreMP();
 	}
 	
 	/**
 	 * Initializes the engine. The UI is started and the intro screen is shown.
 	 */
 	@Override
 	public void run() {
 		ui.initialize();
 		ui.switchToIntroScreen();
 		soundEngine.playMusic("BGM_MAIN_MENU");
 	}
 	
 	/**
 	 * Outputs the results of a battle's action application phase to the user
 	 * interface.
 	 *
 	 * @param battle The battle to output the state of.
 	 */
 	private void outputActionApplication(Battle battle) {
 		Action a = battle.getLastAction();
 		if (a.wasSuccessful()) {
 			if (a.getCostStat() != null) {
 				outputActionCost(a);
 			}
 			ui.showActionUse(a);
 			if (a.getEffectStat() != null) {
 				outputActionEffects(a);
 			}
 			if (a.getOriginBuff() != null) {
 				ui.showBuffActivation(a.getOriginBuff());
 			}
 			if (a.getTargetBuff() != null) {
 				ui.showBuffActivation(a.getTargetBuff());
 			}
 		} else {
 			ui.showActionFailure(a);
 		}
 	}
 	
 	/**
 	 * Outputs the results of an action cost to the user interface.
 	 *
 	 * @param action The Action to output.
 	 */
 	private void outputActionCost(Action a) {
 		ui.showDamage(a.getOrigin(), a.getCostStat(), (int)a.getCost());
 		ui.showStatUpdate(a.getOrigin());
 	}
 	
 	/**
 	 * Outputs the effects of an action to the user interface.
 	 *
 	 * @param a The Action to output.
 	 */
 	private void outputActionEffects(Action a) {
 		int[] effects = a.getActualEffects();
 		ArrayList<Character> targets = a.getTargets();
 		for (int i = 0; i < effects.length; i++) {
 			Character t = targets.get(i);
 			int damage = effects[i];
 			ui.showDamage(t, a.getEffectStat(), damage);
 			ui.showStatUpdate(t);
 		}
 	}
 	
 	/**
 	 * Outputs the results of a battle's action get phase to the user
 	 * interface.
 	 *
 	 * @param battle The battle to output the state of.
 	 */
 	private void outputActionGet(Battle battle) {
 		Action a = battle.getLastAction();
 		ui.showActionPreperation(a);
 	}
 	
 	/**
 	 * Outputs the results of a battle's buff application phase to the user
 	 * interface.
 	 *
 	 * @param battle The battle to output the state of.
 	 */
 	private void outputBuffApplication(Battle battle) {
 		Character currentFighter = battle.getCurrentFighter();
 		ArrayList<Buff> buffs = currentFighter.getBuffs();
 		for (Buff b: buffs) {
 			ui.showBuffApplication(b);
 			ui.showStatUpdate(currentFighter);
 		}
 	}
 	
 	/**
 	 * Outputs the results of a battle's death check phase to the user
 	 * interface.
 	 *
 	 * @param battle The battle to output the state of.
 	 */
 	private void outputDeathCheck(Battle battle) {
 		ArrayList<Character> removed = battle.getRemovedFighters();
 		for (Character c: removed) {
 			ui.showCharacterRemoval(c);
 		}
 	}
 	
 	/**
 	 * Outputs the results of a battle's loot calculation phase to the user
 	 * interface.
 	 *
 	 * @param battle The battle to output the state of.
 	 */
 	private void outputLoot(Battle battle) {}
 	
 	/**
 	 * Outputs the results of a battle's team death check phase to the user
 	 * interface.
 	 *
 	 * @param battle The battle to output the state of.
 	 */
 	private void outputTeamDeathCheck(Battle battle) {}
 	
 	/**
 	 * Outputs the results of a battle's turn start phase to the user
 	 * interface.
 	 *
 	 * @param battle The battle to output the state of.
 	 */
 	private void outputTurnStart(Battle battle) {
 		Character c = battle.getCurrentFighter();
 		int recoveredMana = battle.getRegeneratedMana();
 		ui.display(c, "It looks like I'm up next.");
 		ArrayList<Buff> expiredBuffs = c.getExpiredBuffs();
 		for (Buff expired: expiredBuffs) {
 			ui.showBuffDeactivation(expired);
 		}
 		if (recoveredMana != 0) {
 			ui.showRecovery(c, c.getMPStat(), recoveredMana);
 		}
 		ui.showStatUpdate(c);
 	}
 	
 	/**
 	 * Outputs the results of a battle's victory check phase to the user
 	 * interface.
 	 *
 	 * @param battle The battle to output the state of.
 	 */
 	private void outputVictory(Battle battle) {}
 	
 	/**
 	 * Runs a battle to completion.
 	 *
 	 * @param battle The battle to run
 	 * @param display Whether the battle should be displayed.
 	 */
 	private void runBattle(Battle battle, boolean display) {
 		while (battle.advance()) {
 			if (display) {
 				switch (battle.getLastState()) {
 					case STARTING_TURN:
 						outputTurnStart(battle);
 						break;
 						
 					case GETTING_ACTION:
 						outputActionGet(battle);
 						break;
 						
 					case APPLYING_ACTION:
 						outputActionApplication(battle);
 						break;
 						
 					case APPLYING_BUFFS:
 						outputBuffApplication(battle);
 						break;
 						
 					case CHECKING_DEATH:
 						outputDeathCheck(battle);
 						break;
 						
 					case ENDING_TURN:
 						outputTeamDeathCheck(battle);
 						break;
 						
 					case CHECKING_VICTORY:
 						// not sure we care about this state
 						break;
 						
 					case LOOTING:
 						outputLoot(battle);
 						break;
 						
 					default:
 						break;
 				}
 				if (battle.getState() == Battle.State.ENDING) {
 					outputVictory(battle);
 				}
 			}
 		}
 	}
 	
 	/**
 	 * Spawns a thread that runs a battle to completion.
 	 * 
 	 * @param battle The battle to run through.
 	 * @param display Whether the battle should be displayed on the GUI.
 	 */
 	private void spawnBattleThread(Battle battle, boolean display) {
 		BattleRunner r = new BattleRunner(battle, display);
 		Thread t = new Thread(r, "MainBattle");
 		t.start();
 	}
 	
 	/**
 	 * Applies each of the options in the game options object to obtain their
 	 * respective effects.
 	 */
 	private void applyOptions() {
 		soundEngine.setEffectVolume(options.sfxVolume);
 		soundEngine.setMusicVolume(options.bgmVolume);
 	}
 	
 	/**
 	 * @inheritDoc
 	 */
 	@Override
 	public void requestVolumeUpdate() {
 		applyOptions();
 	}
 	
 	/**
 	 * @inheritDoc
 	 */
 	@Override
 	public void requestSoundEffect(String soundIndex) {
 		soundEngine.playEffect(soundIndex);
 	}
 	
 	/**
 	 * @inheritDoc
 	 */
 	@Override
 	public void requestCloseGame() {
 		ui.switchToIntroScreen();
 	}
 	
 }
