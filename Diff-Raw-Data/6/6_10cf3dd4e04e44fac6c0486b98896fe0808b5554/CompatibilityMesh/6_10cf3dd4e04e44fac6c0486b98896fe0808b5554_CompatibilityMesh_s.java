 package graphics.compatibility;
 
 import static org.lwjgl.opengl.GL11.GL_FLOAT;
 import static org.lwjgl.opengl.GL11.GL_INT;
 import static org.lwjgl.opengl.GL11.GL_TEXTURE_2D;
 import static org.lwjgl.opengl.GL11.GL_TRIANGLES;
 import static org.lwjgl.opengl.GL11.GL_UNSIGNED_INT;
 import static org.lwjgl.opengl.GL11.glBindTexture;
 import static org.lwjgl.opengl.GL11.glDeleteTextures;
 import static org.lwjgl.opengl.GL11.glDrawElements;
 import static org.lwjgl.opengl.GL13.GL_TEXTURE0;
 import static org.lwjgl.opengl.GL13.glActiveTexture;
 import static org.lwjgl.opengl.GL15.GL_ARRAY_BUFFER;
 import static org.lwjgl.opengl.GL15.GL_ELEMENT_ARRAY_BUFFER;
 import static org.lwjgl.opengl.GL15.GL_STATIC_DRAW;
 import static org.lwjgl.opengl.GL15.glBindBuffer;
 import static org.lwjgl.opengl.GL15.glBufferData;
 import static org.lwjgl.opengl.GL15.glDeleteBuffers;
 import static org.lwjgl.opengl.GL15.glGenBuffers;
 import static org.lwjgl.opengl.GL20.glEnableVertexAttribArray;
 import static org.lwjgl.opengl.GL20.glVertexAttribPointer;
 import static org.lwjgl.opengl.GL30.glVertexAttribIPointer;
 import static org.lwjgl.opengl.GL30.glBindVertexArray;
 import static org.lwjgl.opengl.GL30.glDeleteVertexArrays;
 import static org.lwjgl.opengl.GL30.glGenVertexArrays;
 import graphics.GLOperations;
 import graphics.Shader;
 
 import java.nio.FloatBuffer;
 import java.nio.IntBuffer;
 import java.util.HashMap;
 
 import loader.ColladaLoader;
 
 public class CompatibilityMesh{
 
 	private int vao;
 	
 	private int elements;
 	private int elementCount;
 	
 	
 	private int positionVbo;
 	private int normalVbo;
 	private int texCoordVbo;
 	
 	private int textureId;
 	
 	private int positionAttribute;
 	private int normalAttribute;
 	private int texCoordAttribute;
 	
 	private boolean skinned = false;
 	
 	private int jointWeightVbo;
 	private int jointIndexVbo;
 	
 	private int jointWeightAttribute;
 	private int jointIndexAttribute;
 	
 	
 	public CompatibilityMesh(String filename, Shader shader, HashMap<String, Object> modelData)
 	{
 		if(shader.getAttributes().get("jointWeights") != null && modelData.get("skeleton") != null){
 			skinned = true;
 		}
 		
 		this.positionAttribute = shader.getAttributes().get("position");
 		this.normalAttribute = shader.getAttributes().get("normal");
 		this.texCoordAttribute = shader.getAttributes().get("texCoord");
 
 		if(skinned){			
 			this.jointWeightAttribute = shader.getAttributes().get("jointWeights");
 			this.jointIndexAttribute = shader.getAttributes().get("jointIndices");
 		}
 		
 		textureId = GLOperations.loadTexture("temp/debug.png");
 		
 		vao = glGenVertexArrays();
 		positionVbo = glGenBuffers();
 		normalVbo = glGenBuffers();
 		texCoordVbo = glGenBuffers();
 
 		if(skinned){
 			jointWeightVbo = glGenBuffers();
 			jointIndexVbo = glGenBuffers();
 		}
 		
 		elements = glGenBuffers();
 		
 		glBindVertexArray(vao);
 		
 		
 		//modelData = ColladaLoader.load(filename);
 		
 		
 		FloatBuffer vertexBuff = GLOperations.generateFloatBuffer((float[])modelData.get("positions"));
 		FloatBuffer normalBuff = GLOperations.generateFloatBuffer((float[])modelData.get("normals"));
 		FloatBuffer texCoordBuff = GLOperations.generateFloatBuffer((float[])modelData.get("texCoords"));
 		
 		
 		
 		//get elements array
 		float[] fElems = (float[])modelData.get("elements");
 		int[] elems = new int[fElems.length];
 		
 		for(int i = 0; i < fElems.length; i++){
 			elems[i] = (int)fElems[i];
 		}
 		
 		this.elementCount = elems.length;
 		IntBuffer elementBuff = GLOperations.generateIntBuffer(elems);
 		
 		
 		//bind and buffer data
 		glBindBuffer(GL_ARRAY_BUFFER, positionVbo);
         glBufferData(GL_ARRAY_BUFFER, vertexBuff , GL_STATIC_DRAW);
         
 		glBindBuffer(GL_ARRAY_BUFFER, normalVbo);
         glBufferData(GL_ARRAY_BUFFER, normalBuff , GL_STATIC_DRAW);
         
         glBindBuffer(GL_ARRAY_BUFFER, texCoordVbo);
         glBufferData(GL_ARRAY_BUFFER, texCoordBuff, GL_STATIC_DRAW);
        
         
       //TODO: This shouldn't happen when done, the other checks should guarantee it
        if(skinned && modelData.containsKey("jointWeights") && modelData.containsKey("jointIndices")){
         	FloatBuffer jointWeightBuf = GLOperations.generateFloatBuffer((float[])modelData.get("jointWeights"));
         	IntBuffer jointIndexBuf = GLOperations.generateIntBuffer((int[])modelData.get("jointIndices"));
         	
         	
         	glBindBuffer(GL_ARRAY_BUFFER, jointWeightVbo);
         	glBufferData(GL_ARRAY_BUFFER, jointWeightBuf, GL_STATIC_DRAW);
         	
         	
         	glBindBuffer(GL_ARRAY_BUFFER, jointIndexVbo);
         	glBufferData(GL_ARRAY_BUFFER, jointIndexBuf, GL_STATIC_DRAW);
         }
         
         
         glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, elements);
         glBufferData(GL_ELEMENT_ARRAY_BUFFER, elementBuff, GL_STATIC_DRAW);
 		
 		
         //enable vertex attrib ptr
 		glBindBuffer(GL_ARRAY_BUFFER, positionVbo);
         glVertexAttribPointer( positionAttribute, 3, GL_FLOAT, false, 0, 0);
         glEnableVertexAttribArray(positionAttribute);
 		
         glBindBuffer(GL_ARRAY_BUFFER, normalVbo);
         glVertexAttribPointer( normalAttribute, 3, GL_FLOAT, false, 0, 0);
         glEnableVertexAttribArray(normalAttribute);
 
         glBindBuffer(GL_ARRAY_BUFFER, texCoordVbo);
         glVertexAttribPointer( texCoordAttribute, 2, GL_FLOAT, false, 0, 0);
         glEnableVertexAttribArray(texCoordAttribute);
 
         if(skinned){
         	glBindBuffer(GL_ARRAY_BUFFER, jointWeightVbo);
         	glVertexAttribPointer( jointWeightAttribute, 3, GL_FLOAT, false, 0, 0);
         	glEnableVertexAttribArray(jointWeightAttribute);
         	
         	glBindBuffer(GL_ARRAY_BUFFER, jointIndexVbo);
         	glVertexAttribIPointer( jointIndexAttribute, 3, GL_INT, 0, 0);
         	glEnableVertexAttribArray(jointIndexVbo);
         }
         
         //glunbind buffer
 	}
 	
 	public boolean skinned(){
 		return skinned;
 	}
 	
 	public void draw() {
 	
 		glActiveTexture(GL_TEXTURE0);
         glBindTexture(GL_TEXTURE_2D, 1);
 		//bind
 		glBindVertexArray(vao);
 
         glDrawElements(GL_TRIANGLES, elementCount, GL_UNSIGNED_INT, 0);
 
         glBindVertexArray(0);
 	}
 	
 	public void delete() {
 		
 		glDeleteBuffers(this.elements);
 		glDeleteBuffers(this.positionVbo);
 		glDeleteBuffers(this.normalVbo);
 		glDeleteBuffers(this.texCoordVbo);
 		if(skinned){
 			glDeleteBuffers(this.jointWeightVbo);
 		}
 		
 		
 		glDeleteTextures(this.textureId);
 		
 		
 		glDeleteVertexArrays(this.vao);
 		
 		glBindVertexArray(0);
 		
 	}
 
 }
