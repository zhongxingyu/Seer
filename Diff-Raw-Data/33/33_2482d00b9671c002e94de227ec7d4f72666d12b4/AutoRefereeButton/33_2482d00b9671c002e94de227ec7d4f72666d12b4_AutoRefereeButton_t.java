 package net.minecraft.src;
 
 import net.minecraft.src.FontRenderer;
 import net.minecraft.src.GuiButton;
 import net.minecraft.src.Minecraft;
 
 import org.lwjgl.opengl.GL11;
 
 public class AutoRefereeButton extends GuiButton {
 	
 	public String[] message;
 	private String type;
 	
 	private float scale = 1.0F;
 	
 	private AutoRefereePlayer player;
 	private AutoRefereeObjective obj;
 	private AutoRefereeTeam team;
 
	public AutoRefereeButton(int xPos, int yPos, float scale, String type){
		super(1, xPos, yPos, 0, 0, "");
		
		this.type = type;
 		
 		if("TP".equalsIgnoreCase(type))
			scale /= AutoReferee.AUTOREFEREE_ICON_SIZE/20.0F;
 		else if("OBJ".equalsIgnoreCase(type) || "VM".equalsIgnoreCase(type) || "SPAWN".equalsIgnoreCase(type))
 			scale *= 16.0F/AutoReferee.AUTOREFEREE_ICON_SIZE;
         
         this.width = (int)(AutoReferee.AUTOREFEREE_ICON_SIZE*scale);
         this.height = (int)(AutoReferee.AUTOREFEREE_ICON_SIZE*scale);
 		this.scale = scale;
 		
 	}
 	
 	public AutoRefereeButton(float xPos, float yPos, float scale, String type){
		this((int) xPos, (int) yPos, scale, type);
 	}
 	
 	public AutoRefereeButton(float xPos, float yPos, float scale, AutoRefereePlayer player, String type){
		this(xPos, yPos, scale,type);
 		this.player = player;
 		if("TP".equalsIgnoreCase(type)){
 			message = new String[]{"tp","player", player.getName(),"player"};
 		}else if("TPBed".equalsIgnoreCase(type)){
 			message = new String[]{"tp","player", player.getName(),"spawn"};
 		}else if("TPDeath".equalsIgnoreCase(type)){
 			message = new String[]{"tp","player", player.getName(),"death"};
 		}else if("VI".equalsIgnoreCase(type)){
 			message = new String[]{"inventory","player", player.getName()};
 		}else if("VIDeath".equalsIgnoreCase(type)){
 			message = new String[]{"inventory","player", player.getName(),"prev"};
 		}
 	}
 	
 	public AutoRefereeButton(float xPos, float yPos, float scale, AutoRefereeObjective obj, String type){
 		this(xPos, yPos, scale, type);
 		this.obj = obj;
 		if("OBJ".equalsIgnoreCase(type)){
 			message = new String[]{"tp","team", obj.getTeam().getName(), "obj", obj.toString()};
 		}
 	}
 	
 	public AutoRefereeButton(float xPos, float yPos, float scale, AutoRefereeTeam team, String type){
 		this(xPos, yPos, scale, type);
 		this.team = team;
 		if("VM".equalsIgnoreCase(type)){
 			message = new String[]{"tp","team", team.getName(), "vm"};
 		}else if("SPAWN".equalsIgnoreCase(type)){
 			message = new String[]{"tp","team", team.getName(), "spawn"};
 		}
 	}
 	
 	public void drawButton(Minecraft mc, int mouseX, int mouseY)
     {
     	float xTop = xPosition + width/2 - AutoReferee.AUTOREFEREE_ICON_SIZE*scale/2;
     	float yTop = yPosition + height/2 - AutoReferee.AUTOREFEREE_ICON_SIZE*scale/2;
         if("TP".equalsIgnoreCase(type))
        	AutoReferee.get().renderSkinHead(player, xTop, yTop, scale*AutoReferee.AUTOREFEREE_ICON_SIZE/20.0F);
         else if("TPBed".equalsIgnoreCase(type))
        	AutoReferee.get().renderItem(355, 0, (int)(xTop), (int)(yTop), scale*AutoReferee.AUTOREFEREE_ICON_SIZE/16.0F);
         else if("TPDeath".equalsIgnoreCase(type))
         	AutoReferee.get().renderAutoRefereeIcon(AutoReferee.AUTOREFEREE_SKULL_ICON, 0, xTop, yTop, scale);
         else if("VI".equalsIgnoreCase(type))
        	AutoReferee.get().renderItem(54, 0, (int)(xTop), (int)(yTop), scale*AutoReferee.AUTOREFEREE_ICON_SIZE/16.0F);
         else if("VIDeath".equalsIgnoreCase(type))
        	AutoReferee.get().renderItem(130, 0, (int)(xTop), (int)(yTop), scale*AutoReferee.AUTOREFEREE_ICON_SIZE/16.0F);
         else if("OBJ".equalsIgnoreCase(type))
         	AutoReferee.get().renderItem(obj.getId(), obj.getData(), (int) xTop, (int) yTop, scale);
         else if("SPAWN".equalsIgnoreCase(type))
         	AutoReferee.get().renderAutoRefereeIcon(AutoReferee.AUTOREFEREE_ARROW_ICON, 0, xTop, yTop, scale);
         else if("VM".equalsIgnoreCase(type))
         	AutoReferee.get().renderAutoRefereeIcon(AutoReferee.AUTOREFEREE_WINNERS_ICON, 0, xTop, yTop, scale);
     }
 }
