 package npc;
 import gameCharacter.AutomatedCharacter;
 import utils.Location;
 import app.RPGame;
 import dialogue.AbstractDialogue;
 
 public class NPC extends AutomatedCharacter{
 
 	/**
 	 * Computer-generated serial ID number
 	 */
 	private static final long serialVersionUID = -5360689062786017503L;
 
 	/**
 	 * constructs a non-player character based on the information at the configuration URL given
 	 * @param game
 	 * @param loc
 	 * @param configURL
 	 */
 	public NPC(RPGame game, Location loc, String configURL) {
 		super(game, loc, configURL);
 	}
 	
 	private void constructActions(String json) {
 //		Gson gson = new Gson();
 //		JsonUtil.JSONNpcActions actions = gson.fromJson(json,
 //				JsonUtil.JSONNpcActions.class);
 //		if (actions.talking == null)
 //			this.dialogue = new NullDialogue();
 //		this.getActions().add("talking",
 //				new Talking(new Talk(this.getActions(), actions.talking)));
 	}
 	
 	public void setDialogue (AbstractDialogue dialogue){
 		this.dialogue = dialogue;
 	}
 	
 	public String getTalk(){
 		return dialogue.getCurrentLine();
 	}
 
 }
