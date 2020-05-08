 package graphics.compatibility;
 
 
 import static org.lwjgl.opengl.GL11.GL_COLOR_BUFFER_BIT;
 import static org.lwjgl.opengl.GL11.GL_DEPTH_BUFFER_BIT;
 import static org.lwjgl.opengl.GL11.GL_DEPTH_TEST;
 import static org.lwjgl.opengl.GL11.glClear;
 import static org.lwjgl.opengl.GL11.glEnable;
 import graphics.Camera;
 import graphics.Light;
 import graphics.Model;
 import graphics.RenderMaster;
 import graphics.Shader;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.lwjgl.opengl.Display;
 
 public class CompatibilityRenderMaster implements RenderMaster{
 	
 	CompatibilityModelFactory modelFactory;
 	
 	List<CompatibilityModel> models;
 	List<CompatibilitySkinnedModel> skinnedModels;
 	Camera camera;
 	Shader staticShader;
 	Shader skinnedShader;
 	
 	public CompatibilityRenderMaster()
 	{
         glEnable(GL_DEPTH_TEST);
 		
 		String fragmentShader = "temp/fragShader.txt";
 		String vertexShader = "temp/vertShader.txt";
 		String skinnedVertexShader = "temp/skinVertShader.txt";
		String skinnedFragShader = "temp/skinFragShader.txt";
		
 		
 		staticShader = new CompatibilityShader(fragmentShader, vertexShader);
		skinnedShader = new CompatibilityShader(skinnedFragShader, skinnedVertexShader);
 		
 		modelFactory = new CompatibilityModelFactory(staticShader, skinnedShader);
 		models = new ArrayList<CompatibilityModel>();
 		skinnedModels = new ArrayList<CompatibilitySkinnedModel>();
 		
 		
         
 		
		camera = new Camera(skinnedShader);
 		
 	}
 
 	
 	public void render() {
 		long time = System.currentTimeMillis();
 		
 		glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
 		camera.setActive();
 		
 //		staticShader.use();
 //		for(CompatibilityModel mesh : models)
 //		{
 //			mesh.draw(time);
 //		}
 //        
 		skinnedShader.use();
 		for(CompatibilitySkinnedModel mesh : skinnedModels)
 		{
 			mesh.draw(time);
 		}
 		
         Display.update();
     }
 
 	public Camera getCamera() {
 		return this.camera;
 	}
 	
 	@Override
 	public Light addLight() {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	public Light addMasterLight() {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	public void loadMeshes(String[] filenames) {
 		for(String file : filenames){
 			modelFactory.loadModel(file);
 		}
 	}
 
 	public void unloadMeshes(String[] filenames) {
 		for(String file : filenames){
 			modelFactory.unloadModel(file);
 		}
 	}
 
 	public Model addModel(String filename) {
 		CompatibilityModel m = modelFactory.getModel(filename);
 		if(m.getClass().equals(CompatibilitySkinnedModel.class)){
 			skinnedModels.add((CompatibilitySkinnedModel)m);
 		}
 		else
 		{
 			models.add(m);	
 		}
 		
 		return (Model)m;
 	}
 
 }
