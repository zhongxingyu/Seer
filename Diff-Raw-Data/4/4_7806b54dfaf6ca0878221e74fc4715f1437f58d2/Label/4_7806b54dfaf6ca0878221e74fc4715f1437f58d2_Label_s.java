 package cdg.swi.game.menu;
 
 import static org.lwjgl.opengl.GL15.GL_ARRAY_BUFFER;
 import static org.lwjgl.opengl.GL15.GL_STATIC_DRAW;
 import static org.lwjgl.opengl.GL15.glBindBuffer;
 import static org.lwjgl.opengl.GL30.glBindVertexArray;
 
 import java.nio.ByteBuffer;
 import java.nio.FloatBuffer;
 import java.nio.IntBuffer;
 
 import org.lwjgl.BufferUtils;
 import org.lwjgl.opengl.GL11;
 import org.lwjgl.opengl.GL13;
 import org.lwjgl.opengl.GL15;
 import org.lwjgl.opengl.GL20;
 import org.lwjgl.opengl.GL30;
 
 import cdg.swi.game.util.FontFinals;
 import cdg.swi.game.util.StaticManager;
 import cdg.swi.game.util.VertexData;
 
 public class Label extends Component {
 
	private VertexData[] textPoints;
 	
 	public Label(float x, float y, String text) 
 	{
 		super(x, y);
 		this.setText(text);
 		this.setupGL();
 	}
 	
 	public void setAlpha(float alpha)
 	{
 		
 	}	
 	
 	private void setupGL()
 	{
 		this.setupGLText();		
 		
 	}
 		
 	@Override
 	public void drawUI() 
 	{
 		this.drawGLText();
 	}
 
 }
