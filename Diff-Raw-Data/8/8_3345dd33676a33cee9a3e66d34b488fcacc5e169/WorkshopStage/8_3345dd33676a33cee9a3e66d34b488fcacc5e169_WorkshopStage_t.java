 package com.icbat.game.tradesong.stages;
 
 import com.badlogic.gdx.audio.Sound;
 import com.badlogic.gdx.graphics.Texture;
 import com.badlogic.gdx.graphics.g2d.BitmapFont;
 import com.badlogic.gdx.scenes.scene2d.Actor;
 import com.badlogic.gdx.scenes.scene2d.Group;
 import com.badlogic.gdx.scenes.scene2d.InputEvent;
 import com.badlogic.gdx.scenes.scene2d.Touchable;
 import com.badlogic.gdx.scenes.scene2d.ui.Image;
 import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
 import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
 import com.badlogic.gdx.utils.Timer;
 import com.icbat.game.tradesong.*;
 import com.icbat.game.tradesong.utils.ItemFrame;
 import com.icbat.game.tradesong.utils.SoundAssets;
 import com.icbat.game.tradesong.utils.TextureAssets;
 
 import java.util.ArrayList;
 import java.util.Set;
 
 public class WorkshopStage extends AbstractStage {
 
     private Workshop workshop;
     private TextButton header;
     private Group frames = new Group();
     private Group ingredients = new Group();
     private Group productGroup = new Group();
 
     private Timer craftTimer = new Timer();
 
     private static final int SPACER = 10;
     private Image resultFrame;
     private Sound craftSound = Tradesong.getSound(SoundAssets.GATHER_CLINK);
 
     public WorkshopStage() {
         this(new Workshop("Blacksmith"));
     }
 
     public WorkshopStage(Workshop workshop) {
         super();
         setWorkshop(workshop); // Handles the standard setup
     }
 
     @Override
     public void layout() {
 
         setWorkshop(this.workshop);
     }
 
     /** Called when the workshop changes, including at startup. */
     public void setWorkshop(Workshop newWorkshop) {
         this.clear();
         frames.clear();
         workshop = newWorkshop;
 
 
 
         if (header != null)
             header.remove();
         addWorkshopTitle();
         addWorkshopChangers();
         addIngredientFrames();
         addArrowAndResultFrame();
         this.addActor(ingredients);
 
         addProduct();
         this.addActor(productGroup);
 
 
     }
 
     private void addWorkshopTitle() {
         TextButton.TextButtonStyle style = new TextButton.TextButtonStyle();
         style.font = new BitmapFont();
 
         header = new TextButton(workshop.getType(), style);
         header.setVisible(true);
         header.setTouchable(Touchable.disabled);
         layOutVertically(header);
 
         this.addActor(header);
 
     }
 
     private void addWorkshopChangers() {
         Image blacksmithButton = new Image(Tradesong.getTexture(TextureAssets.ICON_HAMMER));
         Image tinkerButton = new Image(Tradesong.getTexture(TextureAssets.ICON_WRENCH));
         Image scribeButton = new Image(Tradesong.getTexture(TextureAssets.ICON_BOOK));
 
         int left = (int) header.getX();
 
         left -= blacksmithButton.getWidth() + SPACER;
         blacksmithButton.setPosition(left, header.getY());
         left -= tinkerButton.getWidth() + SPACER ;
         tinkerButton.setPosition(left, header.getY());
         left -= scribeButton.getWidth() + SPACER;
         scribeButton.setPosition(left, header.getY());
 
         blacksmithButton.setTouchable(Touchable.enabled);
         tinkerButton.setTouchable(Touchable.enabled);
         scribeButton.setTouchable(Touchable.enabled);
 
         blacksmithButton.addListener(new ChangeWorkshopClickListener("Blacksmith"));
         tinkerButton.addListener(new ChangeWorkshopClickListener("Tinker"));
         scribeButton.addListener(new ChangeWorkshopClickListener("Scribe"));
 
 
         this.addActor(blacksmithButton);
         this.addActor(tinkerButton);
         this.addActor(scribeButton);
 
     }
 
     private void addIngredientFrames() {
         frames.clearChildren();
 
         for (int i = 0; i < workshop.getNumberOfSlots(); ++i) {
             Image frame = new ItemFrame();
             layOutVertically(frame);
             frame.setName(""+(Integer)i);
             frames.addActor(frame);
         }
 
         this.addActor(frames);
     }
 
     private void addArrowAndResultFrame() {
         Texture arrowTexture = Tradesong.getTexture(TextureAssets.WORKSHOP_ARROW);
         Image arrowImage = new Image( arrowTexture );
         layOutVertically(arrowImage);
         this.addActor(arrowImage);
 
         resultFrame = new ItemFrame();
         layOutVertically(resultFrame);
         this.addActor(resultFrame);
     }
 
     public boolean addIngredient(Item item) {
         // Check to see if there's space to add more
         Integer size = ingredients.getChildren().size;
         if (size >= workshop.getNumberOfSlots()) {
             return false;
         }
         else {
 
             Actor frame = frames.findActor(size.toString());
             item.setBounds(frame.getX(), frame.getY(), item.getWidth(), item.getHeight());
 
             // Add the listener to remove it
             item.addListener(new BackToInventoryClickListener(item, false));
 
             // Add the item
             ingredients.addActor(item);
 
             return true;
         }
 
     }
 
     public void addProduct() {
         productGroup.clearChildren();
 
         Set<Recipe> allRecipes = Tradesong.gameState.getAllKnownRecipes();
         for (Recipe recipe : allRecipes) {
             if (recipe.getWorkshop().equals(this.workshop.getType())) {
 
                 if (recipe.check(ingredients.getChildren())) {
                     Item potentialProduct = recipe.getOutput();
                     potentialProduct.setPosition(resultFrame.getX(), resultFrame.getY());
                     potentialProduct.setTouchable(Touchable.enabled);
                     potentialProduct.addListener(new BackToInventoryClickListener(potentialProduct, true));
                     productGroup.addActor(potentialProduct);
                 }
             }
         }
 
 
     }
 
     /** Called to remove all the ingredients and the result (if any)
      *
      * @param returnToInventory should these ingredients be added back to the inventory?*/
     public void clearIngredients(boolean returnToInventory) {
 
 
         ArrayList<Actor> ingredientsSnapshot = new ArrayList<Actor>();
 
         for (Actor actor : ingredients.getChildren()) {
             ingredientsSnapshot.add(actor);
         }
 
 
         for (Actor ingredient : ingredientsSnapshot) {
             if (returnToInventory) {
                 Tradesong.gameState.getInventory().add(new Item(ingredient));
             }
 
             ingredient.remove();
             productGroup.clearChildren();
         }
 
     }
 
     /** Sets the bounds of the param to the next spot in a vertically descending pattern
      * @param   actor   actor on which to calculate bounds */
     private void layOutVertically(Actor actor) {
         actor.setBounds(this.getWidth() - actor.getWidth() - SPACER, findLowestY() - SPACER, actor.getWidth(), actor.getHeight());
     }
 
     /** Helper function to find the next valid Y pos to put an actor in the layout */
     private int findLowestY() {
         float lowestFound = this.getHeight() - 20;
         float check;
         for (Actor actor : this.getActors()) {
            if (!actor.getClass().equals(Group.class)) {
                 check = actor.getY() - actor.getHeight();
                 if (check < lowestFound)
                     lowestFound = check;
             }
 
         }
 
         for (Actor actor : frames.getChildren()) {
             check = actor.getY() - actor.getHeight();
             if (check < lowestFound)
                 lowestFound = check;
         }
 
         return (int) lowestFound;
 
     }
 
 
     class BackToInventoryClickListener extends ClickListener {
 
         private Item owner;
 
         private boolean isResult;
 
         BackToInventoryClickListener(Item owner, boolean isResult) {
 
             this.owner = owner;
             this.isResult = isResult;
         }
 
         @Override
         public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
 
             if (isResult) {
                 // Can we even add this?
                 if (Tradesong.gameState.getInventory().canAdd(owner)) {
 
                     craftSound.stop();
                     craftSound.play();
                     long id = craftSound.play();
                     craftSound.setVolume(id , GameStateManager.getSFXVolume());
 
                     craftTimer.stop();
                     craftTimer.clear();
                     craftTimer.scheduleTask(new Timer.Task() {
 
                         @Override
                         public void run() {
                             if (Tradesong.gameState.getInventory().add(new Item(owner))) {
                                 clearIngredients(false);
                                 craftSound.stop();
                             }
                         }
                     }, Tradesong.gameState.getParameterByName(Tradesong.getParamDelayCraft()).getCurrentValue());
                     craftTimer.start();
                 }
 
 
             } else if (Tradesong.gameState.getInventory().add(new Item(owner))) {
                 owner.remove();
                 addProduct();
 
             }
 
 
             return super.touchDown(event, x, y, pointer, button);
         }
     }
 
     class ChangeWorkshopClickListener extends ClickListener {
         private String targetName;
 
         ChangeWorkshopClickListener(String targetWorkshopName) {
             targetName = targetWorkshopName;
         }
 
         @Override
         public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
             setWorkshop(new Workshop(targetName));
 
             return super.touchDown(event, x, y, pointer, button);
         }
     }
 
 }
