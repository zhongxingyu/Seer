 package npc;
 
 import state.MovingState;
 
 import com.google.gson.JsonElement;
 import com.google.gson.JsonObject;
 
 import gameCharacter.CharacterDecorator;
 import gameCharacter.GameCharacter;
 import dialogue.SimpleDialogue.SimpleDialogueObject;
 import dialogue.SimpleDialogue;
 
 import ai.AbstractMovementAI;
 import ai.SquareMovementAI;
 
 public class NPCTest1 extends NPC {
 
 	private boolean hasTalked;
 
 	/**
 	 * Computer-generated serial ID number
 	 */
 	@SuppressWarnings("unused")
 	private static final long serialVersionUID = 4483591744499315422L;
 
 	public NPCTest1(GameCharacter character, JsonElement jsonMovement) {
 		super(character);
 		
 		name = "NPCTest1";
 		
 		AbstractMovementAI movement = AbstractMovementAI.getAbstractMovementAI(this.character.getGame(), this.getCharacter(), jsonMovement);
 		
 		//SquareMovementAI sq = new SquareMovementAI(this.character.getGame(), this.getCharacter(), 300);
 		this.setCurrentState(new MovingState(movement,null));
 		dialogue = new SimpleDialogue("rsc/savedmaps/npc1.txt");
 	}
 
 	/**
 	 * 
 	 * @return
 	 */
 	@Override
 	public String getTalk(SimpleDialogueObject choice) {
 		dialogue.goToNextLine(new SimpleDialogue.SimpleDialogueObject());
 		return dialogue.getCurrentLine();
 	}
 
 	public static class NPCTest1Factory extends NPCFactory {
 
 		public NPCTest1Factory() {
 		};
 
 		@Override
 		public boolean isThisType(String npcName) {
 			return npcName.equals("NPCTest1");
 		}
 
 
 		public NPC constructNPC(GameCharacter gameChar, JsonElement jsonMovement) {
 			return new NPCTest1(gameChar, jsonMovement);
 		}
 
 	}
 
 	@Override
 	public JsonObject getJsonAttributes() {
 		// TODO Auto-generated method stub
 		return new JsonObject();
 	}
 
 
 }
