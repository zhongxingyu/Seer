 package rsmg.controller;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.newdawn.slick.GameContainer;
 import org.newdawn.slick.Graphics;
 import org.newdawn.slick.Image;
 import org.newdawn.slick.Input;
 import org.newdawn.slick.SlickException;
 import org.newdawn.slick.UnicodeFont;
 import org.newdawn.slick.font.effects.ColorEffect;
 import org.newdawn.slick.state.StateBasedGame;
 import org.newdawn.slick.state.transition.BlobbyTransition;
 
 import rsmg.io.CharacterProgress;
 
 /**
  * The upgrades menu state. This is the view where the player can spend his
  * upgrade points on upgrades.
  * 
  * @author Daniel Jonsson
  * 
  */
 class UpgradesState extends State {
 
 	/**
 	 * The upgrade screen's images.
 	 */
 	private Image background;
 	private Image title;
 	private Image upgradeLocked;
 	private Image upgradeLockedSelected;
 	private Image upgradeUnlocked;
 	private Image upgradeUnlockedSelected;
 	private Image upgradeUnavailable;
 
 	/**
 	 * Font to draw text on the screen.
 	 */
 	private UnicodeFont font;
 	
 	/**
 	 * Information about the upgrade buttons.
 	 */
 	private List<UpgradeButton> upgradeButtons;
 	
 	/**
 	 * Keeps track of which of the buttons that is currently selected.
 	 */
 	private int selected;
 	
 	/**
 	 * How much everything should be multiplied with.
 	 */
 	private float scale;
 	
 	/**
 	 * How far the view should be drawn from the top of the screen. If the screen hasn't
 	 * the aspect radio 16:9, there will be black borders over and under the view.
 	 */
 	private float topOffset;
 	
 	UpgradesState(int stateID) {
 		super(stateID);
 	}
 
 	/**
 	 * Initialize the upgrade menu.
 	 */
 	@SuppressWarnings("unchecked")
 	@Override
 	public void init(GameContainer gc, StateBasedGame sbg)
 			throws SlickException {
 
 		scale = (float)gc.getWidth() / 1920;
 		
 		topOffset = (gc.getHeight() - 1080 * scale) / 2;
 		
 		// Folder path to the sprites.
 		String folderPath = "res/sprites/upgradesScreen/";
 		
 		// Create the bg image and scale it to fit the window's width
 		background = newImage(folderPath+"bg.jpg", scale);
 		
 		// Create the title image with the same scale as the background image
 		title = newImage(folderPath+"title.png", scale);
 		
 		// Load the font which is used to write text to the screen
 		font = new UnicodeFont(new java.awt.Font("Verdana", java.awt.Font.PLAIN, 15));
 		font.getEffects().add(new ColorEffect(java.awt.Color.WHITE));
 		font.addAsciiGlyphs();
 		font.loadGlyphs();
 		
 		// Init upgrade button images
 		upgradeLocked = newImage(folderPath+"upgradeLocked.png", scale);
 		upgradeLockedSelected = newImage(folderPath+"upgradeLockedSelected.png", scale);
 		upgradeUnlocked = newImage(folderPath+"upgradeUnlocked.png", scale);
 		upgradeUnlockedSelected = newImage(folderPath+"upgradeUnlockedSelected.png", scale);
 		upgradeUnavailable = newImage(folderPath+"upgradeUnavailable.png", scale);
 		
 		// Init upgrade button information
 		upgradeButtons = new ArrayList<UpgradeButton>();
 		upgradeButtons.add(new UpgradeButton(CharacterProgress.INC_RUNNING_SPEED, "Run Faster", 306, 400, scale));
 		upgradeButtons.add(new UpgradeButton(CharacterProgress.INC_SHOTGUN_SPREAD, "Shotgun Spread", 1313, 400, scale));
 		upgradeButtons.add(new UpgradeButton(CharacterProgress.DASH, "Dash", 100, 700, scale));
 		upgradeButtons.add(new UpgradeButton(CharacterProgress.DOUBLE_JUMP, "Double Jump", 500, 700, scale));
 		upgradeButtons.add(new UpgradeButton(CharacterProgress.RAPID_FIRE, "Rapid Fire", 1107, 700, scale));
 		upgradeButtons.add(new UpgradeButton(CharacterProgress.INC_RPG_AOE, "Bigger RPG\nAoE", 1507, 700, scale));
 		
 		// Go through all upgrade buttons and see which should be available, unlocked and so on.
 		checkUpgradeButtons();
 		
 		// Set which button is initially selected
 		selected = 0;
 		upgradeButtons.get(selected).toggleSelected();
 	}
 	
 	private static Image newImage(final String path, final float scale) throws SlickException {
 		return new Image(path).getScaledCopy(scale);
 	}
 	
 	@Override
	public void enter(GameContainer gc, StateBasedGame sbg)
 			throws SlickException {
		super.enter(gc, sbg);
		gc.getInput().clearKeyPressedRecord();
 		checkUpgradeButtons();
		
 	}
 	
 	/**
 	 * This method goes through relevant upgrade buttons and check which ones
 	 * should be unlocked and available.
 	 */
 	private void checkUpgradeButtons() {
 		if (CharacterProgress.isIncRunningSpeedUnlocked()) {
 			upgradeButtons.get(0).setUnlocked(true);
 			upgradeButtons.get(2).setUnlocked(CharacterProgress.isDashUnlocked());
 			upgradeButtons.get(3).setUnlocked(CharacterProgress.isDoubleJumpUnlocked());
 		} else {
 			upgradeButtons.get(0).setUnlocked(false);
 			upgradeButtons.get(2).setUnlocked(false);
 			upgradeButtons.get(3).setUnlocked(false);
 			upgradeButtons.get(2).setAvailable(false);
 			upgradeButtons.get(3).setAvailable(false);
 		}
 		
 		if (CharacterProgress.isIncShotgunSpreadUnlocked()) {
 			upgradeButtons.get(1).setUnlocked(true);
 			upgradeButtons.get(4).setUnlocked(CharacterProgress.isRapidFireUnlocked());
 			upgradeButtons.get(5).setUnlocked(CharacterProgress.isIncRPGAoEUnlocked());
 		} else {
 			upgradeButtons.get(1).setUnlocked(false);
 			upgradeButtons.get(4).setUnlocked(false);
 			upgradeButtons.get(5).setUnlocked(false);
 			upgradeButtons.get(4).setAvailable(false);
 			upgradeButtons.get(5).setAvailable(false);
 		}
 	}
 
 	/**
 	 * Handle inputs from the keyboard.
 	 */
 	@Override
 	public void update(GameContainer gc, StateBasedGame sbg, int delta)
 			throws SlickException {
 		handleInputs(gc.getInput(), gc, sbg);
 	}
 
 	/**
 	 * Draw main menu's images.
 	 */
 	@Override
 	public void render(GameContainer gc, StateBasedGame sbg, Graphics g)
 			throws SlickException {
 		
 		drawBackground();
 		drawTitle(gc.getWidth());
 		drawTexts();
 		drawUpgradeButtons();
 	}
 	
 	/**
 	 * Draws the background to the screen
 	 */
 	private void drawBackground() {
 		background.draw(0, topOffset);
 	}
 	
 	/**
 	 * Draws the title to the screen
 	 * @param windowWidth width of the window/screen
 	 */
 	private void drawTitle(int windowWidth) {
 		title.draw((windowWidth - title.getWidth()) / 2, 80*scale+topOffset);
 	}
 	
 	/**
 	 * Draw the text to the screen
 	 */
 	private void drawTexts() {
 		// titles for the upgrade categories
 		font.drawString(300*scale, 300*scale, "Movement Upgrades");
 		font.drawString(1300*scale, 300*scale, "Combat Upgrades");
 		
 		// number of available upgrade points
 		font.drawString(100*scale, 100*scale, "Upgrade Points: " + CharacterProgress.getUpgradePoints());
 	}
 	
 	/**
 	 * Draws the upgrade buttons
 	 */
 	private void drawUpgradeButtons() {
 		for (UpgradeButton button : upgradeButtons) {
 			if (button.isAvailable()) {
 				
 				if (button.isSelected() && button.isUnlocked())
 					upgradeUnlockedSelected.draw(button.getX()-46f*scale, button.getY()-46f*scale);
 				
 				else if (button.isSelected() && !button.isUnlocked())
 					upgradeLockedSelected.draw(button.getX()-46f*scale, button.getY()-46f*scale);
 				
 				else if (!button.isSelected() && button.isUnlocked())
 					upgradeUnlocked.draw(button.getX(), button.getY());
 				
 				else if (!button.isSelected() && !button.isUnlocked())
 					upgradeLocked.draw(button.getX(), button.getY());
 				
 			} else {
 				upgradeUnavailable.draw(button.getX(), button.getY());
 			}
 			font.drawString(button.getX()+20*scale, button.getY()+20*scale, button.getText());
 		}
 	}
 	
 	/**
 	 * Reset the selected button to the first button before state is changed.
 	 */
 	@Override
 	public void leave(GameContainer gc, StateBasedGame game)
 			throws SlickException {
 		upgradeButtons.get(selected).toggleSelected();
 		selected = 0;
 		upgradeButtons.get(selected).toggleSelected();
 	}
 
 	/**
 	 * Handle inputs from the user to navigate in the upgrade menu
 	 * @param input
 	 * @param gc
 	 * @param sbg
 	 * @throws SlickException
 	 */
 	private void handleInputs(Input input, GameContainer gc, StateBasedGame sbg) throws SlickException {
 		if (input.isKeyPressed(Input.KEY_UP))
 			navigateUpInGrid();
 		else if (input.isKeyPressed(Input.KEY_DOWN))
 			navigateDownInGrid();
 		else if (input.isKeyPressed(Input.KEY_LEFT))
 			navigateLeftInGrid();
 		else if (input.isKeyPressed(Input.KEY_RIGHT))
 			navigateRightInGrid();
 		else if (input.isKeyPressed(Input.KEY_ENTER))
 			selectUpgrade();
 		else if (input.isKeyPressed(Input.KEY_ESCAPE))
 			sbg.enterState(Controller.LEVEL_SELECTION_STATE, null, new BlobbyTransition());
 		else if (input.isKeyPressed(Input.KEY_0)) {
 			CharacterProgress.setUpgradePoints(CharacterProgress.getUpgradePoints()+1);
 			CharacterProgress.saveFile();
 		}
 	}
 	
 	/**
 	 * Navigate up in the menu.
 	 */
 	private void navigateUpInGrid() {
 		if (selected > 1) {
 			upgradeButtons.get(selected).toggleSelected();
 			if (selected < 4)
 				selected = 0;
 			else
 				selected = 1;
 			upgradeButtons.get(selected).toggleSelected();
 		}
 	}
 	
 	/**
 	 * Navigate down in the menu.
 	 */
 	private void navigateDownInGrid() {
 		if (selected < 2) {
 			upgradeButtons.get(selected).toggleSelected();
 			if (selected < 1 && upgradeButtons.get(0).isUnlocked())
 				selected = 2;
 			else if (upgradeButtons.get(1).isUnlocked())
 				selected = 4;
 			upgradeButtons.get(selected).toggleSelected();
 		}
 	}
 	
 	/**
 	 * Navigate left in the menu.
 	 */
 	private void navigateLeftInGrid() {
 		if (selected == 1 || selected == 3 || selected == 5 || (selected == 4 && upgradeButtons.get(3).isAvailable())) {
 			upgradeButtons.get(selected).toggleSelected();
 			selected -= 1;
 			upgradeButtons.get(selected).toggleSelected();
 		}
 	}
 
 	/**
 	 * Navigate right in the menu.
 	 */
 	private void navigateRightInGrid() {
 		if (selected == 0 || selected == 2 || selected == 4 || (selected == 3 && upgradeButtons.get(4).isAvailable()) ) {
 			upgradeButtons.get(selected).toggleSelected();
 			selected += 1;
 			upgradeButtons.get(selected).toggleSelected();
 		}
 	}
 	
 	/**
 	 * Select an upgrade.
 	 */
 	private void selectUpgrade() {
 		if (!upgradeButtons.get(selected).isUnlocked() && CharacterProgress.getUpgradePoints() > 0) {
 			CharacterProgress.setUpgrade(upgradeButtons.get(selected).getUpgrade(), true);
 			CharacterProgress.setUpgradePoints(CharacterProgress.getUpgradePoints()-1);
 			CharacterProgress.saveFile();
 			upgradeButtons.get(selected).setUnlocked(true);
 			if (selected == 0) {
 				upgradeButtons.get(2).setAvailable(true);
 				upgradeButtons.get(3).setAvailable(true);
 			} else if (selected == 1) {
 				upgradeButtons.get(4).setAvailable(true);
 				upgradeButtons.get(5).setAvailable(true);
 			}
 		}
 	}
 	
 	/**
 	 * A class containing data about a upgrade button.
 	 * 
 	 * @author Daniel Jonsson
 	 * 
 	 */
 	private class UpgradeButton {
 		
 		private String upgrade;
 		private String buttonText;
 		private float x;
 		private float y;
 		private boolean selected;
 		private boolean unlocked;
 		private boolean available;
 
 		/**
 		 * Creates an upgrade button.
 		 * 
 		 * @param upgrade
 		 *            What upgrade it should be linked to.
 		 * @param buttonText
 		 *            Text on the button.
 		 * @param x
 		 *            Button's X coordinate.
 		 * @param y
 		 *            Button's Y coordinate.
 		 * @param scale
 		 *            How much the button should be scaled.
 		 * @throws SlickException
 		 */
 		public UpgradeButton(String upgrade, String buttonText, int x, int y,
 				float scale) throws SlickException {
 			
 			this.upgrade = upgrade;
 			
 			this.buttonText = buttonText;
 			
 			this.x = x * scale;
 			this.y = y * scale;
 			
 			selected = false;
 			unlocked = false;
 			available = true;
 		}
 		
 		/**
 		 * Toggle if this button should be marked as selected.
 		 */
 		public void toggleSelected() {
 			selected = !selected;
 		}
 		
 		/**
 		 * If this upgrade should be marked as unlocked.
 		 * @param unlocked True if it should be unlocked, otherwise false.
 		 */
 		public void setUnlocked(boolean unlocked) {
 			this.unlocked = unlocked;
 		}
 		
 		/**
 		 * Set if this button is to be available.
 		 * @param available If available.
 		 */
 		public void setAvailable(boolean available) {
 			this.available = available;
 		}
 		
 		/**
 		 * Tells if the button is selected or not.
 		 * @return true If selected, otherwise false.
 		 */
 		public boolean isSelected() {
 			return selected;
 		}
 		
 		/**
 		 * Tells if the button is unlocked.
 		 * @return true If unlocked, otherwise false.
 		 */
 		public boolean isUnlocked() {
 			return unlocked;
 		}
 		
 		/**
 		 * Tells if the button is available.
 		 * @return true If available, otherwise false.
 		 */
 		public boolean isAvailable() {
 			return available;
 		}
 		
 		/**
 		 * Get the upgrade.
 		 * @return Upgrade as String.
 		 */
 		public String getUpgrade() {
 			return upgrade;
 		}
 		
 		/**
 		 * Retrieve the x position for this button.
 		 * @return X position as float.
 		 */
 		public float getX() {
 			return x;
 		}
 		
 		/**
 		 * Retrieve the Y position for this button.
 		 * @return Y position as float.
 		 */
 		public float getY() {
 			return y;
 		}
 		
 		/**
 		 * Get the text for this button.
 		 * @return Button text.
 		 */
 		public String getText() {
 			return buttonText;
 		}
 	}
 }
