 package tetrix.utilities;
 
 import java.io.BufferedReader;
 import java.io.FileReader;
 import java.io.IOException;
 
 import org.lwjgl.opengl.GL11;
 import org.lwjgl.opengl.GL20;
 
 public class ShaderUtils {
 	public static int loadShader(String filename, int type) {
 		StringBuilder shaderSource = new StringBuilder();
 		int shaderID = 0;
 		
 		try {
 			BufferedReader reader = new BufferedReader(new FileReader(filename));
 			String line;
 			while ((line = reader.readLine()) != null) {
 				shaderSource.append(line).append("\n");
 			}
 			reader.close();
 		} catch (IOException e) {
 			System.err.println("Could not read file.");
 			e.printStackTrace();
 			System.exit(-1);
 		}
 		
 		shaderID = GL20.glCreateShader(type);
 		GL20.glShaderSource(shaderID, shaderSource);
 		GL20.glCompileShader(shaderID);
 		
 		if (GL20.glGetShaderi(shaderID, GL20.GL_COMPILE_STATUS) == GL11.GL_FALSE) {
			System.err.println("Could not compile shader.");
 			System.exit(-1);
 		}
 		
 		GLUtils.exitOnGLError("loadShader");
 		
 		return shaderID;
 	}	
 	
 }
