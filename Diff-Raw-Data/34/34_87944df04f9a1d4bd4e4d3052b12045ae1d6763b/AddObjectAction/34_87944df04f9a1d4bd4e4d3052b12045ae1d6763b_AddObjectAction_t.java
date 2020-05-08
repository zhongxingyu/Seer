 package scene1;
 
 import java.awt.event.ActionEvent;
 import java.util.ArrayList;
 
 import javax.swing.AbstractAction;
 
 public class AddObjectAction extends AbstractAction{
 	public AddObjectAction(SceneComponent scene,int actionType){
 		this.scene = scene;
 		this.actionType=actionType;
 		opposites = new ArrayList<RemoveObjectAction>();
		others = new ArrayList<AddObjectAction>();
 	}
 	
 	public void actionPerformed(ActionEvent e){
 		
 		if(actionType==ADD_HOUSE){
 			scene.add(new HouseShape(20,20,SceneComponent.DEFAULT_WIDTH));
 		}
 		else if(actionType==ADD_CAR){
 			scene.add(new CarShape(20,20,SceneComponent.DEFAULT_WIDTH));
 		}
 		
 		if(scene.getNumber()==SceneComponent.MAX_OBJECTS){
			setEnabled(false);
			setOtherAdders(false);
 			setOpposites(true);
 		}
 		else if(scene.getNumber()>0){
 			setOpposites(true);
 		}
 	}
 	/**
 	 * I used an arrrayList here so as not to violate the 
 	 * POLS between my RemoveObjectAction class and my
 	 * AddObjectAction class -- because in this scenario you
 	 * only need 1 opposite really
 	 * @param opposite
 	 */
 	public void addOpposite(RemoveObjectAction opposite){
 		opposites.add(opposite);
 	}
 	
	public void addOtherAdder(AddObjectAction other){
		others.add(other);
	}
	
	public void setOtherAdders(boolean b){
		for(AddObjectAction a : others){
			a.setEnabled(b);
		}
	}
	
 	public void setOpposites(boolean b){
 		for (RemoveObjectAction a: opposites){
 			a.setEnabled(b);
 		}
 	}
 	
	
	
	private ArrayList<AddObjectAction> others;
 	private ArrayList<RemoveObjectAction> opposites;
 	private final SceneComponent scene;
 	private final int actionType;
 	public static final int ADD_HOUSE = 0;
 	public static final int ADD_CAR = 1;
 	
 }
 
