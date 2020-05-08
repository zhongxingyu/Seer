 import org.newdawn.slick.GameContainer;
 import org.newdawn.slick.Graphics;
 import org.newdawn.slick.Image;
 import org.newdawn.slick.SlickException;
 import org.newdawn.slick.gui.GUIContext;
 import org.newdawn.slick.state.BasicGameState;
 import org.newdawn.slick.state.StateBasedGame;
 
 /**
  * A class that implements the state in which the main menu is first rendered
  */
 public class MenuState extends BasicGameState {
     /** Main Menu */
     private Menu mainMenu;
     /** Starting x-position of menu */
     private int startingX;
     /** Starting y-position of menu */
     private int startingY;
     /** State Id of menu */
     private int stateID;
     /** Space between menu items */
     private int spaceBetweenItems;
     /** Location of unselected play button graphic */
     private final String playUnselected = "res/menu/PlayButton.png";
     /** Location of selected play button graphic */
     private final String playSelected = "res/menu/PlayButtonSelected.png";
     /** Location of unselected instruction button graphic */
     private final String instructionsUnselected = "res/menu/InstructionsButton.png";
     /** Location of selected instruction button graphic */
     private final String instructionsSelected = "res/menu/InstructionsButtonSelected.png";
     /** Location of selected quit button graphic */
     private final String quitSelected = "res/menu/QuitButton.png";
     /** Location of unselected play button graphic */
     private final String quitUnselected = "res/menu/QuitButtonSelected.png";
     /** Location of background graphic */
     private final String bgPath = "res/menu/MenuBG.png";
     /** Background graphic */
     private Image bg;
     /** Unselected play button graphic */
     private Image playUs;
     /** Selected play button graphic */
     private Image playS;
     /** Unselected instruction button graphic */
     private Image instructionsUs;
     /** Selected instruction button graphic */
     private Image instructionsS;
     /** Unselected quit button graphic */
     private Image quitUs;
     /** Selected quit button graphic */
     private Image quitS;
     /** GUI context of menu state */
     private GUIContext guiContext;
     public SoundManager soundManager;
 
 
     /**
      * Initializes Menu state
      * @param stateID
      * @param startingX
      * @param startingY
      * @param spaceBetweenItems
      */
     public MenuState(int stateID, int startingX, int startingY, int spaceBetweenItems) {
         super();
         this.stateID = stateID;
         this.startingX = startingX;
         this.startingY = startingY;
         this.spaceBetweenItems = spaceBetweenItems;
         this.guiContext = guiContext;
 
     }
 
     /**
      * Tries to load images on Menu
      * @param gc
      * @param sbg
      */
     private void initMenu(GameContainer gc, StateBasedGame sbg) {
         mainMenu = new Menu(startingX, startingY, spaceBetweenItems);
 
         // create images for each button
         try {
             playUs = new Image(playUnselected);
             playS = new Image(playSelected);
             instructionsUs = new Image(instructionsUnselected);
             instructionsS = new Image(instructionsSelected);
             quitUs = new Image(quitUnselected);
             quitS = new Image(quitSelected);
         } catch (SlickException ex) {
             ex.printStackTrace();
             return;
         }
         AnimatedButton play = new AnimatedButton(gc, sbg, playUs, playS, startingX, startingY, 1);  //should 1 be PLAY_STATE_ID?
 
         play.add(new ButtonAction() {
             public void perform() {
                 /*currentState = STATES.PLAY;
                 sbg.enterState(GauchoRunner.PLAY_STATE_ID);*/
             }
         }
         );
 
         AnimatedButton instructions = new AnimatedButton(gc, sbg, instructionsUs, instructionsS, startingX, startingY + spaceBetweenItems, 2);
 
         instructions.add(new ButtonAction() {
             public void perform() {
 
             }
         }
 
         );
 
         AnimatedButton quit = new AnimatedButton(gc, sbg, quitUs, quitS, startingX, startingY + 2 * spaceBetweenItems, 3);
 
         quit.add(new ButtonAction() {
             public void perform() {
                 //gc.exit();
             }
         }
         );
 
         mainMenu.addItem(play);
         mainMenu.addItem(instructions);
         mainMenu.addItem(quit);
     }
 
     /**
      * Gets state ID of menu
      * @return
      */
     public int getID() {
         return this.stateID;
     }
 
     /**
      * Initializes Menu background and buttons
      * @param gc
      * @param sbg
      * @throws SlickException
      */
     public void init(GameContainer gc, StateBasedGame sbg) throws SlickException {
         try {
 
             bg = new Image(bgPath);
             initMenu(gc, sbg);
            //soundManager = new SoundManager();
            //soundManager.play();
         } catch (SlickException ex) {
             ex.printStackTrace();
             return;
         }
     }
 
     /**
      * Draws Menu background and buttons
      * @param gc
      * @param sbg
      * @param g
      */
     public void render(GameContainer gc, StateBasedGame sbg, Graphics g) {
         g.drawImage(bg, 0, 0);
         mainMenu.render(gc, g);
     }
 
     /**
      * Updates the menu state
      * @param gc
      * @param sbg
      * @param id
      */
     public void update(GameContainer gc, StateBasedGame sbg, int id) {
        /* switch(currentState)
         {
             case PLAY:
 
                 break;
             case INSTRUCTIONS:
                 break;
         } */
     }
 }
