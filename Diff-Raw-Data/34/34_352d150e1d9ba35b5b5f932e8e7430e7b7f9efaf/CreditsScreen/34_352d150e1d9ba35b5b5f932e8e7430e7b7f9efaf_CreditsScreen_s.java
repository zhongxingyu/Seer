 package cdg.swi.game.menu;
 
 import static org.lwjgl.opengl.GL15.GL_ARRAY_BUFFER;
 import static org.lwjgl.opengl.GL15.GL_STATIC_DRAW;
 import static org.lwjgl.opengl.GL15.glBindBuffer;
 import static org.lwjgl.opengl.GL30.glBindVertexArray;
 
 import java.io.BufferedReader;
 import java.io.DataInputStream;
 import java.io.FileInputStream;
 import java.io.InputStreamReader;
 import java.nio.FloatBuffer;
 import java.nio.IntBuffer;
 import java.util.Calendar;
 
 import org.lwjgl.BufferUtils;
 import org.lwjgl.Sys;
 import org.lwjgl.opengl.GL11;
 import org.lwjgl.opengl.GL15;
 import org.lwjgl.opengl.GL20;
 import org.lwjgl.opengl.GL30;
 
 import cdg.swi.game.util.FontFinals;
 import cdg.swi.game.util.Matrix4x4;
 import cdg.swi.game.util.StaticManager;
 import cdg.swi.game.util.Vertex2;
 import cdg.swi.game.util.Vertex4;
 import cdg.swi.game.util.VertexData;
 import cdg.swi.game.util.interfaces.IGameControl;
 
 public class CreditsScreen {
 
 	private String credits = "".toUpperCase();
 	
 	protected Matrix4x4 textScalingMatrix = new Matrix4x4(1.67f, 0.0f, 0.0f, 0.0f,
 														  0.0f, 1.67f, 0.0f, 0.0f,
 														  0.0f, 0.0f, 1.67f, 0.0f,
 														  0.0f, 0.0f, 0.0f, 1.0f);
 	
 	protected Matrix4x4 textTranslationMatrix = new Matrix4x4(0.5f, 0.0f, 0.0f, 0.0f,
 															  0.0f, 0.5f, 0.0f, 0.0f,
 															  0.0f, 0.0f, 0.5f, 0.0f,
 															  0.0f, 0.0f, 0.0f, 1.0f);
 	protected int drawVAO = -1;
 	protected int drawVBO = -1;
 	protected int drawIndiciesVBO = -1;
 	private float[] textColor = new float[]{0.0196078431f,0.81176470434f,0.67058823402f};
 	protected float alpha = 0.8f;
 	private Vertex2 max;
 	private float time = 400;
 	private int state = 0;
 	private boolean sw = false;
 	private IGameControl c;
 	
 	public CreditsScreen(IGameControl c) 
 	{
 		this.c = c;
 		try
 		{
 
 		    FileInputStream fstream = new FileInputStream("res\\txt\\intro.txt");
 			DataInputStream in = new DataInputStream(fstream);
 		    BufferedReader br = new BufferedReader(new InputStreamReader(in));
 			String strLine;
 			while ((strLine = br.readLine()) != null)   
 			{
 				credits+=strLine.toUpperCase()+"\n";
 			}
 			in.close();
 		}
 		catch (Exception e)
 	    {//Catch exception if any
 			  System.err.println("Error: " + e.getMessage());
 		}
		System.out.println(credits);
 		this.setupGL(0);
 	}
 	
 	private void setupGL(int state)
 	{
 		
 		if(this.credits != "")
 		{
 			if(this.drawVAO == -1)
 				this.drawVAO = GL30.glGenVertexArrays(); //generate id for VAO
 			if(this.drawVBO == -1)
 				this.drawVBO = GL15.glGenBuffers(); //generate id for VBO
 			
 			
 			VertexData[] textPoints = new VertexData[this.credits.length()*4];
 			float xoff = 0.0f;
 			float yoff = 0.0f;
 			float xoffmax = 0.0f;
 			IntBuffer indiciesBuffer = BufferUtils.createIntBuffer(this.credits.length()*3*2);
 			
 			glBindVertexArray(this.drawVAO); //bind our VAO
 			glBindBuffer(GL_ARRAY_BUFFER, this.drawVBO); //bind our VBO		
 			
 			for(int i = 0; i < this.credits.length(); i++)
 			{
 				char c = this.credits.charAt(i);
 				if(c == '\n')
 				{
 					xoff = 0.0f;
 					yoff += FontFinals.getHeight('A');
 				}
 				textPoints[i*4+0] = new VertexData(new float[]{xoff,-yoff,0.0f,1.0f}, 
 											 new float[]{this.textColor[0],this.textColor[1],this.textColor[2],this.alpha},
 											 new float[]{FontFinals.getX(c),FontFinals.getY(c)});
 				
 				textPoints[i*4+1] = new VertexData(new float[]{xoff,-yoff-FontFinals.getHeight(c),0.0f,1.0f}, 
 											 new float[]{this.textColor[0],this.textColor[1],this.textColor[2],this.alpha},
 						 					 new float[]{FontFinals.getX(c),FontFinals.getY(c)+FontFinals.getHeight(c)});
 				
 				textPoints[i*4+2] = new VertexData(new float[]{xoff+FontFinals.getWidth(c),-yoff-FontFinals.getHeight(c),0.0f,1.0f}, 
 											 new float[]{this.textColor[0],this.textColor[1],this.textColor[2],this.alpha},
 						 					 new float[]{FontFinals.getX(c)+FontFinals.getWidth(c),FontFinals.getY(c)+FontFinals.getHeight(c)});
 				
 				textPoints[i*4+3] = new VertexData(new float[]{xoff+FontFinals.getWidth(c),-yoff,0.0f,1.0f}, 
 											 new float[]{this.textColor[0],this.textColor[1],this.textColor[2],this.alpha},
 						 					 new float[]{FontFinals.getX(c)+FontFinals.getWidth(c),FontFinals.getY(c)});	
 				xoff += FontFinals.getWidth(c);
 				
 				if(xoff > xoffmax)
 					xoffmax = xoff;
 				
 				indiciesBuffer.put(new int[]{(int) (0+i*4),
 						 (int) (1+i*4), 
 						 (int) (2+i*4), 
 						 (int) (2+i*4), 
 						 (int) (3+i*4), 
 						 (int) (0+i*4)}); //put in indicies
 				
 			}
 			indiciesBuffer.flip();
 			
 			//buffer to store data
 			FloatBuffer f = BufferUtils.createFloatBuffer(textPoints.length * VertexData.ELEMENT_COUNT);
 			for (int i = 0; i < textPoints.length; i++) 
 			{
 				//add position, color and texture floats to the buffer
 				f.put(textPoints[i].getElements());
 			}
 			f.flip();
 			
 			//upload our data
 			GL15.glBufferData(GL_ARRAY_BUFFER, f, GL_STATIC_DRAW);
 			
 			// Put the position coordinates in attribute list 0
 			GL20.glVertexAttribPointer(0, VertexData.POSITION_ELEMENT_COUNT, GL11.GL_FLOAT, 
 					false, VertexData.STRIDE, VertexData.POSITION_BYTE_OFFSET);
 			
 			// Put the color components in attribute list 1
 			GL20.glVertexAttribPointer(1, VertexData.COLOR_ELEMENT_COUNT, GL11.GL_FLOAT, 
 					false, VertexData.STRIDE, VertexData.COLOR_BYTE_OFFSET);
 			
 			// Put the texture coordinates in attribute list 2
 			GL20.glVertexAttribPointer(2, VertexData.TEXTURE_ELEMENT_COUNT, GL11.GL_FLOAT, 
 					false, VertexData.STRIDE, VertexData.TEXTURE_BYTE_OFFSET);
 				
 			//unbind buffer
 			GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);
 			
 			//buffer for storing indicies
 			
 			if(this.drawIndiciesVBO == -1)
 				this.drawIndiciesVBO = GL15.glGenBuffers(); //generate buffer id for indicies
 			
 			GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, this.drawIndiciesVBO); //bind indicies buffer
 			
 			//upload indicies
 			GL15.glBufferData(GL15.GL_ELEMENT_ARRAY_BUFFER, indiciesBuffer, GL15.GL_STATIC_DRAW);
 			
 			GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, 0); //unbind buffer		
 			GL30.glBindVertexArray(0); //unbind VAO
 			
 			this.max = new Vertex2(xoffmax,yoff+FontFinals.getHeight('#'));
 			
 		}
 	}
 	
 	public void draw()
 	{
 		double delta = this.c.getDelta();
 		GL11.glClearColor(0.0f, 
 		  		  0.0f,
 		  		  0.0f, 
 		  		  1.0f);
 		if(sw)
 		{
 			sw = false;
 			if(state == 1)
 			{
 				try
 				{
 					credits = "";
 				    FileInputStream fstream = new FileInputStream("res\\txt\\logo.txt");
 					DataInputStream in = new DataInputStream(fstream);
 				    BufferedReader br = new BufferedReader(new InputStreamReader(in));
 					String strLine;
 					while ((strLine = br.readLine()) != null)   
 					{
 						credits+=strLine.toUpperCase()+"\n";
 					}
 					in.close();
 				}
 				catch (Exception e)
 			    {//Catch exception if any
 					  System.err.println("Error: " + e.getMessage());
 				}
 				textColor = new float[]{0.9215686257f,0.90588235122f,0.00784313724f};
 				this.setupGL(state);
 				time = 1000;
 			}
 			else
 			{
 				try
 				{
 					credits = "";
 				    FileInputStream fstream = new FileInputStream("res\\txt\\credits.txt");
 					DataInputStream in = new DataInputStream(fstream);
 				    BufferedReader br = new BufferedReader(new InputStreamReader(in));
 					String strLine;
 					while ((strLine = br.readLine()) != null)   
 					{
 						credits+=strLine.toUpperCase()+"\n";
 					}
 					in.close();
 				}
 				catch (Exception e)
 			    {//Catch exception if any
 					  System.err.println("Error: " + e.getMessage());
 				}
 				textColor = new float[]{0.9215686257f,0.90588235122f,0.00784313724f};	  
 				this.setupGL(state);
 				time = 1000;
 			}
 		}
 		if(this.drawVAO != -1)
 		{
 			if(state == 0)
 			{
 				time -= 1000.0/7000.0*delta;
 				GL11.glEnable(GL11.GL_BLEND);
 				GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
 				
 				//GL20.glUseProgram(StaticManager.CREDITS_PROGRAM_ID);
 				GL20.glUseProgram(StaticManager.CREDITS_PROGRAM_ID);
 				GL30.glBindVertexArray(this.drawVAO);
 				
 				textScalingMatrix = new Matrix4x4(1.4f, 0.0f, 0.0f, 0.0f,
 						  						  0.0f, 1.4f, 0.0f, 0.0f,
 						  						  0.0f, 0.0f, 1.4f, 0.0f,
 						  						  0.0f, 0.0f, 0.0f, 1.0f);
 				
 				FloatBuffer mat = BufferUtils.createFloatBuffer(16);
 				mat.put(this.textScalingMatrix.toArray());
 				mat.flip();
 				
				FloatBuffer trmat = BufferUtils.createFloatBuffer(16);
				trmat.put(this.textTranslationMatrix.toArray());
				trmat.flip();
 				
 				GL20.glUniform1i(GL20.glGetUniformLocation(StaticManager.CREDITS_PROGRAM_ID, "texture_font"), 0);
 				GL20.glUniformMatrix4(GL20.glGetUniformLocation(StaticManager.CREDITS_PROGRAM_ID, "font_scaling_matrix"), false, mat);
 				GL20.glUniform2f(GL20.glGetUniformLocation(StaticManager.CREDITS_PROGRAM_ID,"position"), (textScalingMatrix.multiply(max).getX()/2)*-1, textScalingMatrix.multiply(max).getY()/2);		
				GL20.glUniformMatrix4(GL20.glGetUniformLocation(StaticManager.CREDITS_PROGRAM_ID, "translation_matrix"), false, trmat);
 				GL20.glUniform1i(GL20.glGetUniformLocation(StaticManager.CREDITS_PROGRAM_ID, "state"), state);
 				
 				
 				GL20.glEnableVertexAttribArray(0);
 				GL20.glEnableVertexAttribArray(1);
 				GL20.glEnableVertexAttribArray(2);
 				
 				// Bind to the index VBO that has all the information about the order of the vertices
 				GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, this.drawIndiciesVBO);
 				
 				
 				// Draw the vertices
 				GL11.glDrawElements(GL11.GL_TRIANGLES, this.credits.length()*3*2, GL11.GL_UNSIGNED_INT, 0);
 				
 				if(time <= 0)
 				{
 					sw = true;
 					state++;
 				}
 				
 			}
 			else if(state == 1)
 			{
 				time -= 1000.0/8000.0*delta;
 				GL11.glEnable(GL11.GL_BLEND);
 				GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
 				
 				//GL20.glUseProgram(StaticManager.CREDITS_PROGRAM_ID);
 				GL20.glUseProgram(StaticManager.CREDITS_PROGRAM_ID);
 				GL30.glBindVertexArray(this.drawVAO);
 				
 				textScalingMatrix = new Matrix4x4(0.00167f*this.time, 0.0f, 0.0f, 0.0f,
 						  						  0.0f, 0.00167f*this.time, 0.0f, 0.0f,
 						  						  0.0f, 0.0f, 0.00167f*this.time, 0.0f,
 						  						  0.0f, 0.0f, 0.0f, 1.0f);
 				
 				FloatBuffer mat = BufferUtils.createFloatBuffer(16);
 				mat.put(this.textScalingMatrix.toArray());
 				mat.flip();
 				
				FloatBuffer trmat = BufferUtils.createFloatBuffer(16);
				trmat.put(this.textTranslationMatrix.toArray());
				trmat.flip();
 				
 				GL20.glUniform1i(GL20.glGetUniformLocation(StaticManager.CREDITS_PROGRAM_ID, "texture_font"), 0);
 				GL20.glUniformMatrix4(GL20.glGetUniformLocation(StaticManager.CREDITS_PROGRAM_ID, "font_scaling_matrix"), false, mat);
 				GL20.glUniform2f(GL20.glGetUniformLocation(StaticManager.CREDITS_PROGRAM_ID,"position"), (textScalingMatrix.multiply(max).getX()/2)*-1, textScalingMatrix.multiply(max).getY()/2);		
				GL20.glUniformMatrix4(GL20.glGetUniformLocation(StaticManager.CREDITS_PROGRAM_ID, "translation_matrix"), false, trmat);
 				GL20.glUniform1i(GL20.glGetUniformLocation(StaticManager.CREDITS_PROGRAM_ID, "state"), state);
 				
 				
 				GL20.glEnableVertexAttribArray(0);
 				GL20.glEnableVertexAttribArray(1);
 				GL20.glEnableVertexAttribArray(2);
 				
 				// Bind to the index VBO that has all the information about the order of the vertices
 				GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, this.drawIndiciesVBO);
 				
 				
 				// Draw the vertices
 				GL11.glDrawElements(GL11.GL_TRIANGLES, this.credits.length()*3*2, GL11.GL_UNSIGNED_INT, 0);
 			
 				if(time <= 0)
 				{
 					sw = true;
 					state++;
 				}
 			// Put everything back to default (deselect)
 			}			
 			else if(state == 2)
 			{
 				time -= 1000.0/50000.0*delta;
 				GL11.glEnable(GL11.GL_BLEND);
 				GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
 				
 				//GL20.glUseProgram(StaticManager.CREDITS_PROGRAM_ID);
 				GL20.glUseProgram(StaticManager.CREDITS_PROGRAM_ID);
 				GL30.glBindVertexArray(this.drawVAO);
 				
 				textScalingMatrix = new Matrix4x4(0.8f, 0.0f, 0.0f, 0.0f,
 						  						  0.0f, 0.8f, 0.0f, 0.0f,
 						  						  0.0f, 0.0f, 0.8f, 0.0f,
 						  						  0.0f, 0.0f, 0.0f, 1.0f);
 				
 				FloatBuffer mat = BufferUtils.createFloatBuffer(16);
 				mat.put(this.textScalingMatrix.toArray());
 				mat.flip();
 				
				FloatBuffer trmat = BufferUtils.createFloatBuffer(16);
				trmat.put(this.textTranslationMatrix.toArray());
				trmat.flip();
 				
 				GL20.glUniform1i(GL20.glGetUniformLocation(StaticManager.CREDITS_PROGRAM_ID, "texture_font"), 0);
 				GL20.glUniformMatrix4(GL20.glGetUniformLocation(StaticManager.CREDITS_PROGRAM_ID, "font_scaling_matrix"), false, mat);
 				GL20.glUniform2f(GL20.glGetUniformLocation(StaticManager.CREDITS_PROGRAM_ID,"position"), -1.0f, 2.0f);		
				GL20.glUniformMatrix4(GL20.glGetUniformLocation(StaticManager.CREDITS_PROGRAM_ID, "translation_matrix"), false, trmat);
 				GL20.glUniform1i(GL20.glGetUniformLocation(StaticManager.CREDITS_PROGRAM_ID, "state"), state);
 				GL20.glUniform1f(GL20.glGetUniformLocation(StaticManager.CREDITS_PROGRAM_ID, "time"), time);
 				GL20.glUniform1f(GL20.glGetUniformLocation(StaticManager.CREDITS_PROGRAM_ID, "maxy"), textScalingMatrix.multiply(max).getY());
 				
 				
 				GL20.glEnableVertexAttribArray(0);
 				GL20.glEnableVertexAttribArray(1);
 				GL20.glEnableVertexAttribArray(2);
 				
 				// Bind to the index VBO that has all the information about the order of the vertices
 				GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, this.drawIndiciesVBO);
 				
 				
 				// Draw the vertices
 				GL11.glDrawElements(GL11.GL_TRIANGLES, this.credits.length()*3*2, GL11.GL_UNSIGNED_INT, 0);
 			
				if(time <= -230)
 				{
 					c.showMainMenu();
 				}
 			// Put everything back to default (deselect)
 			}
 			GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, 0);
 			GL20.glDisableVertexAttribArray(0);
 			GL20.glDisableVertexAttribArray(1);
 			GL20.glDisableVertexAttribArray(2);
 			GL30.glBindVertexArray(0);
 			GL20.glUseProgram(0);
 		
 		}
 	}
 	
 
 }
