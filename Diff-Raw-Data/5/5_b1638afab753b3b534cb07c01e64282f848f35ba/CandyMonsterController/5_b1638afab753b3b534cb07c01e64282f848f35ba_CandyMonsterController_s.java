 package controller;
 
 import org.newdawn.slick.Color;
 import org.newdawn.slick.SlickException;
 
 import model.CandyMonster;
 import model.Item;
 import view.CandyMonsterView;
 
 public class CandyMonsterController {
 	private CandyMonsterView candyMonsterView;
 	private CandyMonster candyMonster;
 	private InGameController inGameController;
 	boolean isSoundPlayed = false;
 	
 	public CandyMonsterController(InGameController inGameController, int candyNumber, int index) throws SlickException{
 		this.inGameController = inGameController;
 		this.candyMonster = new CandyMonster(this.inGameController.getBlockMapController().getCandyMonsterMap().getBlockList().get(index).getPosX(), 
 											this.inGameController.getBlockMapController().getCandyMonsterMap().getBlockList().get(index).getPosY(), 
 											candyNumber); //x, y, candyNumber
 		this.candyMonsterView = new CandyMonsterView(this.candyMonster);
 	}
 	
 	public CandyMonster getCandyMonster(){
 		return this.candyMonster;
 	}
 	
 	public CandyMonsterView getCandyMonsterView(){
 		return this.candyMonsterView;
 	}
 	/*Checks if the item is dropped correctly by checking if: the item is picked up, the candynumbers are the same
 	 * and if the item contains/intersects with the character*/
 	public boolean isDroppedOnMonster(Item item){
 		if(!item.isPickedUp() && item.CANDY_NUMBER == candyMonster.CANDY_NUMBER && 
 				(candyMonsterView.getShape().contains(inGameController.getItemControllers().
						get(inGameController.getBlockMapController().getBlockMapView().getCandyMonsterNbrMap().
 								indexOf(candyMonster.CANDY_NUMBER)).getItemView().getShape())) ||
 				 candyMonsterView.getShape().intersects(inGameController.getItemControllers().
						 get(inGameController.getBlockMapController().getBlockMapView().getCandyMonsterNbrMap().
 								 indexOf(candyMonster.CANDY_NUMBER)).getItemView().getShape())){
 			//kolla isDelivered
 			item.setDelivered(true);
 			this.inGameController.getInGame().increaseItemsDelivered();
 			return true;
 		}
 		return false;
 	}
 	
 	public boolean isSoundPlayed() {
 		return isSoundPlayed;
 	}
 }
