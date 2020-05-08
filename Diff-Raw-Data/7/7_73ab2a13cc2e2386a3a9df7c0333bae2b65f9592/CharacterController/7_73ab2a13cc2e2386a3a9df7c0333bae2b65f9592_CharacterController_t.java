 package controller;
 
 
 import model.Character;
 import model.Item;
 
 import org.newdawn.slick.Color;
 import org.newdawn.slick.GameContainer;
 import org.newdawn.slick.Input;
 import org.newdawn.slick.KeyListener;
 import view.CharacterView;
 import view.WorldView;
 
 
 public class CharacterController {
 	private InGameController inGameController;
 	private Character character;
 	private CharacterView characterView;
 
 	/*Default key values*/
 	private int keyRight = Input.KEY_RIGHT;
 	private int keyLeft = Input.KEY_LEFT;
 	private int keyDown = Input.KEY_DOWN;
 	private int keyUp = Input.KEY_UP;
 	
 	public CharacterController(InGameController inGameController) {
 		this.inGameController = inGameController;
 		if (this.inGameController.getBlockMapController().getBlockMapView().getStartingPos() == null) {
 			this.character = new Character(400, 100);
 		} else {
 			this.character = new Character(this.inGameController.getBlockMapController().getBlockMapView().getStartingPos().getPosX(),
 					this.inGameController.getBlockMapController().getBlockMapView().getStartingPos().getPosY());
 		}
 		
 		this.characterView = new CharacterView(character, inGameController.getWorldController().getWorldView());
 	}
 	
 	public Character getCharacter() {
 		return character;
 	}
 
 	public CharacterView getCharacterView() {
 		return characterView;
 	}
 
 
 	/*Getters for keypresses*/
 	public int getKeyRight() {
 		return this.keyRight;
 	}
 	
 	public int getKeyLeft() {
 		return this.keyLeft;
 	}
 	
 	public int getKeyUp() {
 		return this.keyUp;
 	}
 	
 	public int getKeyDown() {
 		return this.keyDown;
 	}
 	
 	/*Setters for keypresses*/
 	public void setKeyRight(int keyRight) {
 		this.keyRight = keyRight;
 	}
 	
 	public void setKeyLeft(int keyLeft) {
 		this.keyLeft = keyLeft;
 	}
 	
 	public void setKeyUp(int keyUp) {
 		this.keyUp = keyUp;
 	}
 	
 	public void setKeyDown(int keyDown) {
 		this.keyDown = keyDown;
 	}
 	
 	/*Check the key pressed matches the right key*/
 
 	public boolean isControllerRight(int key) {
 		return this.keyRight == key;
 	}
 	
 	public boolean isControllerLeft(int key) {
 		return this.keyLeft == key;
 	}
 	
 	public boolean isControllerUp(int key) {
 		return this.keyUp == key;
 	}
 	
 	public boolean isControllerDown(int key) {
 		return this.keyUp == key;
 	}
 		
 	//check which key is pressed
 	public void keyPressedUpdate(GameContainer gc) {
 		Input input = gc.getInput();		
 		
 		if(input.isKeyDown(Input.KEY_RIGHT)) {
 			characterView.setColor(Color.blue);
 			inGameController.getWorldController().moveBodyRight();
 		}
 		if(input.isKeyDown(Input.KEY_LEFT)) {
 			characterView.setColor(Color.green);
 			inGameController.getWorldController().moveBodyLeft();
 		}
 		if(input.isKeyDown(Input.KEY_UP)) {
 			tryToJumpCharacter();
 		}
 	}
 	
	/**
	 * If on ground, then jump.
	 */
 	public void tryToJumpCharacter() {
		if(characterView.getCharacterBody().getLinearVelocity().y <= 0.0001 && characterView.getCharacterBody().getLinearVelocity().y >= -0.0001){
 			characterView.setColor(Color.red);
 			inGameController.getWorldController().jumpBody();
 		}
 	}
 	/**
 	 * checks if the character is close enough to an item to pick it up.
 	 * @return the item that the character is standing at.
 	 */
 	public Item findItemToPickUp() {
 		for (int i = 0; i < inGameController.getWorldController().getItemViewList().size(); i++) {
 			if ((characterView.getSlickShape().intersects(inGameController.getWorldController().getItemViewList().get(i).getShape()) ||
 					characterView.getSlickShape().contains(inGameController.getWorldController().getItemViewList().get(i).getShape()))
 					&& !inGameController.getWorldController().getItemViewList().get(i).getItem().isDelivered) {
 				return inGameController.getWorldController().getItemViewList().get(i).getItem();
 			}
 		}
 		return null;
 	}
 	/**
 	 * 
 	 * @return true if the a character is holding an item.
 	 */
 	public boolean isHoldingItem() {
 		for (int i = 0; i < inGameController.getWorldController().getItemViewList().size(); i++) {
 			if (inGameController.getWorldController().getItemViewList().get(i).getItem().isPickedUp()) {
 				return true;
 			}
 			 
 		} 
 		return false; //måste vara här annars gnälls det.
 	}
 }
