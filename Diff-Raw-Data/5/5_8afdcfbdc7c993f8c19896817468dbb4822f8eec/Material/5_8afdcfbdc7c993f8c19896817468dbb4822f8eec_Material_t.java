 package grige;
 
 import java.io.File;
 import java.io.IOException;
 
 import javax.media.opengl.GL;
 
 import com.jogamp.opengl.util.texture.Texture;
 import com.jogamp.opengl.util.texture.TextureData;
 import com.jogamp.opengl.util.texture.TextureIO;
 
 public class Material
 {
 	private Texture diffuseMap;
 	private Texture normalMap;
 	private Texture selfIlluminationMap;
 	
 	private Material(Texture diffuse)
 	{
 		if(diffuse == null)
 			throw new IllegalArgumentException("Invalid Diffuse Texture, cannot be null");
 		
 		diffuseMap = diffuse;
 		normalMap = null;
 		selfIlluminationMap = null;
 	}
 	
 	public int getWidth()
 	{
		return diffuseMap.getWidth();
 	}
 	
 	public int getHeight()
 	{
		return diffuseMap.getHeight();
 	}
 	
 	public Texture getDiffuseMap()
 	{
 		return diffuseMap;
 	}
 	public Texture getNormalMap()
 	{
 		return normalMap;
 	}
 	public Texture getSelfIlluminationMap()
 	{
 		return selfIlluminationMap;
 	}
 	
 	
 	public static Material load(String filepath)
 	{
 		File sourceFile = new File(filepath);
 		
 		System.out.println("Loading texture "+sourceFile.getAbsolutePath());
 		if(!sourceFile.exists())
 		{
 			System.out.println("Error, attempt to load non-existant file: "+filepath);
 			return null;
 		}
 		
 		try{
 			GL gl = GameBase.instance.getGL();
 			
 			TextureData data = TextureIO.newTextureData(gl.getGLProfile(), sourceFile, false, TextureIO.PNG);
 			Texture result = new Texture(gl, data);
 			
 			return new Material(result);
 			
 		}catch(IOException ioe)
 		{
 			ioe.printStackTrace();
 			System.exit(1);
 		}
 		
 		return null;
 	}
 	
 }
