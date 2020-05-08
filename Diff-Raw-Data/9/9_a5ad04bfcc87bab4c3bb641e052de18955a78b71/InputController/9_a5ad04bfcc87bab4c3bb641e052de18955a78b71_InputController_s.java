 package projectrts.controller;
 
 import java.util.List;
 
 import projectrts.global.constants.Constants;
 import projectrts.global.utils.Utils;
 import projectrts.model.IGame;
 import projectrts.model.constants.P;
 import projectrts.model.entities.EntityManager;
 import projectrts.model.entities.IAbility;
 import projectrts.model.entities.IBuildStructureAbility;
 import projectrts.model.entities.IEntity;
 import projectrts.model.entities.IPlayerControlledEntity;
 import projectrts.model.entities.ITargetAbility;
 import projectrts.model.entities.PlayerControlledEntity;
 import projectrts.model.entities.abilities.GatherResourceAbility;
 import projectrts.model.entities.misc.Resource;
 import projectrts.model.pathfinding.World;
 import projectrts.model.utils.Position;
 import projectrts.view.GameView;
 
 import com.jme3.app.SimpleApplication;
 import com.jme3.input.KeyInput;
 import com.jme3.input.MouseInput;
 import com.jme3.input.controls.ActionListener;
 import com.jme3.input.controls.AnalogListener;
 import com.jme3.input.controls.KeyTrigger;
 import com.jme3.input.controls.MouseAxisTrigger;
 import com.jme3.input.controls.MouseButtonTrigger;
 import com.jme3.math.Vector2f;
 import com.jme3.math.Vector3f;
 
 /**
  * A class for handling all input.
  * @author Markus Ekstrm Modifed by Jakob Svensson
  *
  */
 public class InputController {
 
 	// Before the mouse is moved it has the position (0, 0), causing the camera to move in that direction.
 	// mouseActivated suppresses the camera until set to true (which is done when the mouse is first moved).
 	private boolean mouseActivated = false; 
 	private SimpleApplication app;
 	private IGame game; // The model
 	private GameView view; 
 	private boolean choosingPosition=false;
 	private IAbility currentAbility;
 	private PlayerControlledEntity selectedEntity;
 	private float buildingSize;
 	private boolean choosingTarget;
 
 	private InputGUIController guiControl;
 	
 
 	
 	public InputController(SimpleApplication app, IGame game, GameView view) {
 		this.app = app;
 		this.game = game;
 		this.view = view;
 		initializeKeys();
 	}
 	
 	/**
 	 * Updates the input, should be hooked into the main update loop
 	 * @param tpf Time-per-frame
 	 */
     public void update(float tpf) {
     	
     	
         if(app.getStateManager().getState(InGameState.class).isEnabled()){
           // do the following while game is RUNNING // modify scene graph...
         	updateCamera(tpf);
         	if(choosingPosition){
         		Position pos = Utils.INSTANCE.convertWorldToModel(
         				app.getCamera().getWorldCoordinates(app.getInputManager().getCursorPosition(), 0));
         		view.drawNodes(World.getInstance().getNodesAt(pos,buildingSize));
         	}
         } else {
           // do the following while game is PAUSED, e.g. play an idle animation.
           //...        
         }
       }
     
     /**
      * Updates the camera.
      * 
      * If the mouse cursor is close enough to one of the edges, it moves the camera a certain amount
      * in that direction. The amount is decided by the getCameraSpeed method in the Constants class. 
      * @param tpf
      */
     private void updateCamera(float tpf) {
 
     	if(mouseActivated) {
 	    	Vector3f loc = app.getCamera().getLocation();
 	    	Vector2f mLoc = app.getInputManager().getCursorPosition();
 	    	float margin = Constants.INSTANCE.getCameraMoveMargin();
 	    	if(mLoc.x >= app.getCamera().getWidth() - margin && loc.x <= P.INSTANCE.getWorldWidth() * Constants.INSTANCE.getModelToWorld()) {
 	    		app.getCamera().setLocation(loc.add(tpf * Constants.INSTANCE.getCameraSpeed(), 0, 0));
 	    	}
 	    	if(mLoc.x <= margin && loc.x >= 0) {
 	    		app.getCamera().setLocation(loc.add(tpf * -Constants.INSTANCE.getCameraSpeed(), 0, 0));
 	    	}
 	    	if(mLoc.y <= margin && loc.y >= -P.INSTANCE.getWorldHeight() * Constants.INSTANCE.getModelToWorld()) {
 	    		app.getCamera().setLocation(loc.add(0, tpf * -Constants.INSTANCE.getCameraSpeed(), 0));
 	    	}
 	    	if(mLoc.y >= app.getCamera().getHeight() - margin && loc.y <= 0) {
 	    		app.getCamera().setLocation(loc.add(0, tpf * Constants.INSTANCE.getCameraSpeed(), 0));
 	    	}
     	}
     }
 	
 	/** 
 	 * Initializes all keybinds.
 	 * 
 	 * Maps named actions to inputs and assigns listeners. 
 	 */
     private void initializeKeys() {
     	this.app.getInputManager().setCursorVisible(true);
     	this.app.getInputManager().clearMappings();
     	
     	// You can map one or several inputs to one named action
     	this.app.getInputManager().addMapping("cameraRightKey",  new KeyTrigger(KeyInput.KEY_RIGHT));
     	this.app.getInputManager().addMapping("cameraLeftKey",   new KeyTrigger(KeyInput.KEY_LEFT));
     	this.app.getInputManager().addMapping("cameraUpKey",  new KeyTrigger(KeyInput.KEY_UP));
     	this.app.getInputManager().addMapping("cameraDownKey", new KeyTrigger(KeyInput.KEY_DOWN));
     	
     	this.app.getInputManager().addMapping("cameraRightMouse",  new MouseAxisTrigger(MouseInput.AXIS_X, true) );
     	this.app.getInputManager().addMapping("cameraLeftMouse",   new MouseAxisTrigger(MouseInput.AXIS_X, false) );
     	this.app.getInputManager().addMapping("cameraUpMouse",  new MouseAxisTrigger(MouseInput.AXIS_Y, true) );
     	this.app.getInputManager().addMapping("cameraDownMouse", new MouseAxisTrigger(MouseInput.AXIS_Y, false) );
     	
     	// Map left and right mouse buttons
     	this.app.getInputManager().addMapping("mouseLeftButton", new MouseButtonTrigger(MouseInput.BUTTON_LEFT));
     	this.app.getInputManager().addMapping("mouseRightButton", new MouseButtonTrigger(MouseInput.BUTTON_RIGHT));
     	
     	// Add the names to the action/analog listener.	
     	this.app.getInputManager().addListener(analogListener, new String[]{"cameraRightMouse", "cameraLeftMouse", "cameraUpMouse", "cameraDownMouse","cameraRightKey", "cameraLeftKey", "cameraUpKey", "cameraDownKey"});
     	this.app.getInputManager().addListener(actionListener, new String[]{ 
     																		"mouseLeftButton", "mouseRightButton"});
     	//Debug control mapping
     	this.app.getInputManager().addMapping("exit",  new KeyTrigger(KeyInput.KEY_ESCAPE));
     	this.app.getInputManager().addMapping("checkMouseLoc", new KeyTrigger(KeyInput.KEY_M));
     	
     	//Add debug controls to action/analog listener
     	this.app.getInputManager().addListener(actionListener, new String[]{"exit", "checkMouseLoc"});
     }
     
     /**
      * An analog listener, use if the input is analog - i.e. it can take on several
      * values, not just "on" and "off". 
      * 
      */
     private AnalogListener analogListener = new AnalogListener() {
 	    public void onAnalog(String name, float value, float tpf) {
 	    	
 	    	// Make sure we are in the correct state and that it is enabled.
 	    	if (app.getStateManager().getState(InGameState.class).isEnabled()) {
 	    		Vector3f loc = app.getCamera().getLocation();
 	    		
 	            if (name.equals("cameraRightKey") && loc.x <= P.INSTANCE.getWorldWidth() * Constants.INSTANCE.getModelToWorld()) {
 	            	app.getCamera().setLocation(loc.add(new Vector3f(value*Constants.INSTANCE.getCameraSpeed(), 0, 0)));
 	            }
 	            if (name.equals("cameraLeftKey") && loc.x >= 0) {
 	            	app.getCamera().setLocation(loc.add(new Vector3f(-value*Constants.INSTANCE.getCameraSpeed(), 0, 0)));
 	            }
 	            if (name.equals("cameraUpKey") && loc.y <= 0) {
 	            	app.getCamera().setLocation(loc.add(new Vector3f(0, value*Constants.INSTANCE.getCameraSpeed(), 0)));
 	            }
 	            if (name.equals("cameraDownKey") && loc.y >= -P.INSTANCE.getWorldHeight() * Constants.INSTANCE.getModelToWorld()) {
 	            	app.getCamera().setLocation(loc.add(new Vector3f(0, -value*Constants.INSTANCE.getCameraSpeed(), 0)));
 	            }
 	            
 	            
 		    	// Bypass the fact that the cursor position is (0, 0) before it is moved,
 		    	// which causes the camera to move towards that location would this not be in place.
 		    	if ((!mouseActivated && name.equals("cameraRightMouse") ||
 					    	name.equals("cameraLeftMouse") ||
 					    	name.equals("cameraUpMouse") ||
 					    	name.equals("cameraDownMouse"))) {
 			    	mouseActivated = true;
 			    	app.getInputManager().deleteMapping("cameraRightMouse");
 			    	app.getInputManager().deleteMapping("cameraLeftMouse");
 			    	app.getInputManager().deleteMapping("cameraUpMouse");
 			    	app.getInputManager().deleteMapping("cameraDownMouse");
 		    	}
 		    	
           	} else {
 	        	  System.out.println("Press P to unpause.");
           	}
     	}
     };
       
     /**
      * A digital listener, use if the input is digital - i.e. it can only
      * be either "on" or "off".
      */
     private ActionListener actionListener = new ActionListener() {
     	public void onAction(String name, boolean keyPressed, float tpf) {
     		if (name.equals("exit") && keyPressed) {
 	            app.stop();
 	    	}
     		
 	    	if(app.getStateManager().getState(InGameState.class).isEnabled()) {
 	    		if (name.equals("mouseLeftButton") && keyPressed) {
 	    			handleLeftClick();
 	    		}
 	    		if (name.equals("mouseRightButton") && keyPressed) {
 	    			handleRightClick();
 	    		}
 	    		
 	    		//Debugging
 	    		if (name.equals("checkMouseLoc") && keyPressed) {
 	    			System.out.println("mLoc = " + app.getInputManager().getCursorPosition().toString());
 	    			System.out.println("wLoc = " + app.getCamera().getWorldCoordinates(app.getInputManager().getCursorPosition(), 0));
 	    		}
 	    	} else {
 	    		
 	    	}
 	    	
 
 	    }
     	
     	private void handleLeftClick(){
     		Position pos = Utils.INSTANCE.convertWorldToModel(
     				app.getCamera().getWorldCoordinates(app.getInputManager().getCursorPosition(), 0));
     		if(choosingPosition){    			
     			if(!World.getInstance().isAnyNodeOccupied(
     					World.getInstance().getNodesAt(pos, buildingSize))){
     				
 	    			selectedEntity.doAbility(currentAbility.getName(), 
 	    					World.getInstance().getNodeAt(pos).getPosition());
 	    			choosingPosition=false;
 	    			view.clearNodes();
     			}
     		}else if(choosingTarget){
     			if(currentAbility instanceof GatherResourceAbility){
     				if(EntityManager.getInstance().getNonPlayerControlledEntity(pos) instanceof Resource){
     					selectedEntity.doAbility(currentAbility.getName(), pos);
     					choosingTarget=false;
     				}else{
     					//TODO Jakob: Notify gui that target is invalid
     				}
     			}else{
     				selectedEntity.doAbility(currentAbility.getName(), pos);
     				choosingTarget=false;
     			}
     		}else{
 	    		game.getEntityManager().select(pos, game.getPlayer());
 				view.drawSelected(game.getEntityManager().getSelectedEntities());
 				guiControl.updateAbilities(game.getEntityManager().getSelectedEntities());
     		}
     	}
     	
     	private void handleRightClick(){
     		Position click = Utils.INSTANCE.convertWorldToModel(app.getCamera().getWorldCoordinates(
     				app.getInputManager().getCursorPosition(), 0));
     		IEntity e = getEntityAtPosition(click);
     		if(choosingPosition||choosingTarget){
     			choosingPosition=false;
     			view.clearNodes();
     	    	choosingTarget=false;
     		}else{
 	    		if(e!=null){
 	    			if(e.getName().equals("Resource")){
 	    				game.getEntityManager().useAbilitySelected("GatherResource", click);
 	    				
 	    			}else if(e instanceof PlayerControlledEntity){
 	    				PlayerControlledEntity pce = (PlayerControlledEntity) e;
 	    				if(!pce.getOwner().equals(game.getPlayer())){
 	    					game.getEntityManager().useAbilitySelected("Attack", pce.getPosition());
 	    				}else{
 	    					game.getEntityManager().useAbilitySelected("Move",Utils.INSTANCE.convertWorldToModel(
 	    	    					app.getCamera().getWorldCoordinates(app.getInputManager().getCursorPosition(), 0)));
 	    				}
 	    			}else{
 	    				game.getEntityManager().useAbilitySelected("Move",Utils.INSTANCE.convertWorldToModel(
 		    					app.getCamera().getWorldCoordinates(app.getInputManager().getCursorPosition(), 0)));
 	    			}
 	    			
 	    		}
 	    		else{
 	    			game.getEntityManager().useAbilitySelected("Move",Utils.INSTANCE.convertWorldToModel(
 	    					app.getCamera().getWorldCoordinates(app.getInputManager().getCursorPosition(), 0)));
 	    		}
     		}
     	}
     	
     	private boolean isWithin(double p, double low, double high){
     		return (p>=low && p<=high);
     	}
     	
     	private IEntity getEntityAtPosition(Position pos){
     		List<IEntity> entities = EntityManager.getInstance().getAllEntities();
     		for(IEntity entity: entities){
 				float unitSize = entity.getSize();
 				Position unitPos = entity.getPosition();
 				
 				//If the point is within the area of the unit
 				if(isWithin(pos.getX(), unitPos.getX()-unitSize/2, unitPos.getX()+unitSize/2)
 						&& isWithin(pos.getY(), unitPos.getY()-unitSize/2, unitPos.getY() + unitSize/2)){
 					
 					return entity;
 					
 				}
 			
     		}
     		return null;
     	}
     };
     
     /**
      * Sets the GUI Control
      * @param guiControl
      */
     public void setGUIControl(InputGUIController guiControl){
     	this.guiControl = guiControl;
     }
     
     
     /**
      * Selects an ability
      * @param ability the ability to become selected
      */
     public void selectAbility(IAbility ability, IPlayerControlledEntity e){
     	PlayerControlledEntity pce = (PlayerControlledEntity)e;
     	currentAbility=ability;
     	selectedEntity= pce;
     	choosingPosition=false;
     	choosingTarget=false;
     	if(currentAbility instanceof IBuildStructureAbility){
     		choosingPosition=true;
     		IBuildStructureAbility ab = (IBuildStructureAbility)ability;
     		buildingSize = ab.getSizeOfBuilding();
     	}else if(currentAbility instanceof ITargetAbility){
     		choosingTarget=true;
     	}else{
     		pce.doAbility(currentAbility.getName(), pce.getPosition());
     	}
     	System.out.println(ability.getName());
     }
     
     public void drawNodesAroundCursor(){
     	
     }
     
     
 }
