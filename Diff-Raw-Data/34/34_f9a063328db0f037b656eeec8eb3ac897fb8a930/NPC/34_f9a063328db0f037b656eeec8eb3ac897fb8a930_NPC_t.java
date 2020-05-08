 import static org.lwjgl.opengl.GL11.*;
 import java.io.File;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.List;
 import org.lwjgl.opengl.GL11;
 import java.io.Serializable;
 
 public class NPC implements Serializable {
 	private static final long serialVersionUID = -8405971951484157840L;
 
 	private static int tex;
 	public static int npcID = 0;
 	private static String npcImg = new File("./img/spike-ball.png").getAbsolutePath();
 
 	public static List<NPC> NPCs = Collections.synchronizedList(new ArrayList<NPC>(16));
 	public static int listPosition = 0; // client's position in the list init to zero
 	public int id, x, y;
 	public String name;
 	public boolean selected = false;
 	public boolean alliance; // which side is NPC on 
 	
 	NPC (int id, String name, int x, int y, boolean alliance) {
 		this.id = id;
 		this.name = name;
 		this.alliance = alliance;
 		this.x = x;
 		this.y = y;
 	}
 	
 	boolean inBounds(int mouseX, int mouseY) {
 		if (mouseX > x && mouseX < x + 50 && mouseY > y && mouseY < y + 50)
 			return true;
 		else
 			return false;
 	}
 	
 	public static void loadTexture() {
 		tex = TextureLoader.setupTextures(npcImg);
 	}
 	
 	public static void deleteTexture() {
 		// dont forget to delete the texture after use
 		GL11.glDeleteTextures(tex);
 	}
 	
 	void drawNPC() {
 		// draw player name
 		SimpleText.drawString(name, x, y+55);
 		
 		// can't add texture to player without this
 		GL11.glEnable(GL_TEXTURE_2D);
 		// we can color the texture if we want
 		//glColor3f(1.0f, 0.2f, 0.2f);
 		
 		GL11.glBindTexture(GL11.GL_TEXTURE_2D, tex);
		// draw a monster (a quad) 100 x 100
 		glBegin(GL_QUADS);
 			glTexCoord2f(0.0f, 0.0f);
 			glVertex2f(x, y);
 			glTexCoord2f(1.0f, 0.0f);
			glVertex2f(x+100, y);
 			glTexCoord2f(1.0f, 1.0f);
			glVertex2f(x+100, y+100);
 			glTexCoord2f(0.0f, 1.0f);
			glVertex2f(x, y+100);
 		glEnd();
 	}
 	public static void setId(String npcId) {
 		NPC.npcID = Integer.parseInt(npcId);
 	}
 	public static int getId() {
 		return NPC.npcID;
 	}
 }
