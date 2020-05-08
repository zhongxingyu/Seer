 package team.GunsPlus.Block;
 
 
 import org.getspout.spoutapi.block.design.GenericBlockDesign;
 import org.getspout.spoutapi.block.design.Quad;
 import org.getspout.spoutapi.block.design.SubTexture;
 import org.getspout.spoutapi.block.design.Texture;
 import team.GunsPlus.GunsPlus;
 
 
 public class TripodDesign extends GenericBlockDesign {
 
 	
 	public TripodDesign(GunsPlus plugin, String texURL){
 			Texture tex = new Texture(plugin, texURL, 256, 16, 16);
			texture = tex;
			setBoundingBox(0, 0, 0, 1, 0.9f, 1);
 			setRenderPass(0);
 			setQuadNumber(36);
			setTexture(plugin, texture.getTexture());
 			SubTexture  face1Tex = texture.getSubTexture(0);
 			SubTexture  face2Tex = texture.getSubTexture(1);
 			SubTexture  face3Tex = texture.getSubTexture(2);
 			SubTexture  surTopTex = texture.getSubTexture(3);
 			SubTexture  surBotTex = texture.getSubTexture(4);
 			SubTexture  surSideTex = texture.getSubTexture(5);
 			
 			//struts
 			
 			Quad strut1face1 = new Quad(0, face1Tex);
 			strut1face1.addVertex(0, 0.5f, 1.0f, 0.5f);
 			strut1face1.addVertex(1, 0.5f, 0.9f, 0.5f);
 			strut1face1.addVertex(2, 0.1f, 0, 0);
 			strut1face1.addVertex(3, 0.0f, 0.1f, 0.0f);
 			
 			Quad strut1face2 = new Quad(1, face2Tex);
 			strut1face2.addVertex(0, 0.5f, 1.0f, 0.5f);
 			strut1face2.addVertex(1, 0.5f, 0.9f, 0.5f);
 			strut1face2.addVertex(2, 0, 0, 0.1f);
 			strut1face2.addVertex(3, 0.0f, 0.1f, 0.0f);
 			
 			Quad strut1face3 = new Quad(2, face3Tex);
 			strut1face3.addVertex(0, 0.5f, 0.9f, 0.5f);
 			strut1face3.addVertex(1, 0.5f, 0.9f, 0.5f);
 			strut1face3.addVertex(2, 0, 0, 0.1f);
 			strut1face3.addVertex(3, 0.1f, 0, 0);
 			
 			Quad strut2face1 = new Quad(3, face1Tex);
 			strut2face1.addVertex(0, 0.5f, 1.0f, 0.5f);
 			strut2face1.addVertex(1, 0.5f, 0.9f, 0.5f);
 			strut2face1.addVertex(2, 1, 0, 0.1f);
 			strut2face1.addVertex(3, 1.0f, 0.1f, 0.0f);
 			
 			Quad strut2face2 = new Quad(4, face2Tex);
 			strut2face2.addVertex(0, 0.5f, 1.0f, 0.5f);
 			strut2face2.addVertex(1, 0.5f, 0.9f, 0.5f);
 			strut2face2.addVertex(2, 0.9f, 0, 0);
 			strut2face2.addVertex(3, 1.0f, 0.1f, 0.0f);
 			
 			Quad strut2face3 = new Quad(5, face3Tex);
 			strut2face3.addVertex(0, 0.5f, 0.9f, 0.5f);
 			strut2face3.addVertex(1, 0.5f, 0.9f, 0.5f);
 			strut2face3.addVertex(2, 0.9f, 0, 0);
 			strut2face3.addVertex(3, 1.0f, 0, 0.1f);
 			
 			Quad strut3face1 = new Quad(6, face1Tex);
 			strut3face1.addVertex(0, 0.5f, 1.0f, 0.5f);
 			strut3face1.addVertex(1, 0.5f, 0.9f, 0.5f);
 			strut3face1.addVertex(2, 0.9f, 0, 1);
 			strut3face1.addVertex(3, 1.0f, 0.1f, 1.0f);
 			
 			Quad strut3face2 = new Quad(7, face2Tex);
 			strut3face2.addVertex(0, 0.5f, 1.0f, 0.5f);
 			strut3face2.addVertex(1, 0.5f, 0.9f, 0.5f);
 			strut3face2.addVertex(2, 1, 0, 0.9f);
 			strut3face2.addVertex(3, 1.0f, 0.1f, 1.0f);
 			
 			Quad strut3face3 = new Quad(8, face3Tex);
 			strut3face3.addVertex(0, 0.5f, 1.0f, 0.5f);
 			strut3face3.addVertex(1, 0.5f, 0.9f, 0.5f);
 			strut3face3.addVertex(2, 1, 0, 0.9f);
 			strut3face3.addVertex(3, 0.9f, 0, 1);
 			
 			Quad strut4face1 = new Quad(9, face1Tex);
 			strut4face1.addVertex(0, 0.5f, 1.0f, 0.5f);
 			strut4face1.addVertex(1, 0.5f, 0.9f, 0.5f);
 			strut4face1.addVertex(2, 0, 0, 0.9f);
 			strut4face1.addVertex(3, 0.0f, 0.1f, 1.0f);
 			
 			Quad strut4face2 = new Quad(10, face2Tex);
 			strut4face2.addVertex(0, 0.5f, 1.0f, 0.5f);
 			strut4face2.addVertex(1, 0.5f, 0.9f, 0.5f);
 			strut4face2.addVertex(2, 0.1f, 0, 1);
 			strut4face2.addVertex(3, 0.0f, 0.1f, 1.0f);
 			
 			Quad strut4face3 = new Quad(11, face3Tex);
 			strut4face3.addVertex(0, 0.5f, 1.0f, 0.5f);
 			strut4face3.addVertex(1, 0.5f, 0.9f, 0.5f);
 			strut4face3.addVertex(2, 0.1f, 0, 1);
 			strut4face3.addVertex(3, 0.0f, 0.0f, 0.9f);
 			
 			//the same again but vice-versa due to a render bug :/
 			
 			Quad _strut1face1 = new Quad(12, face1Tex);
 			_strut1face1.addVertex(0, 0.0f, 0.1f, 0.0f);
 			_strut1face1.addVertex(1, 0.1f, 0, 0);
 			_strut1face1.addVertex(2, 0.5f, 0.9f, 0.5f);
 			_strut1face1.addVertex(3, 0.5f, 1.0f, 0.5f);
 			
 			Quad _strut1face2 = new Quad(13, face2Tex);
 			_strut1face2.addVertex(0, 0.0f, 0.1f, 0.0f);
 			_strut1face2.addVertex(1, 0, 0, 0.1f);
 			_strut1face2.addVertex(2, 0.5f, 0.9f, 0.5f);
 			_strut1face2.addVertex(3, 0.5f, 1.0f, 0.5f);
 			
 			Quad _strut1face3 = new Quad(14, face3Tex);
 			_strut1face3.addVertex(0, 0.1f, 0, 0);
 			_strut1face3.addVertex(1, 0, 0, 0.1f);
 			_strut1face3.addVertex(2, 0.5f, 0.9f, 0.5f);
 			_strut1face3.addVertex(3, 0.5f, 0.9f, 0.5f);
 			
 			Quad _strut2face1 = new Quad(15, face1Tex);	
 			_strut2face1.addVertex(0, 1.0f, 0.1f, 0.0f);
 			_strut2face1.addVertex(1, 1, 0, 0.1f);
 			_strut2face1.addVertex(2, 0.5f, 0.9f, 0.5f);
 			_strut2face1.addVertex(3, 0.5f, 1.0f, 0.5f);
 			
 			Quad _strut2face2 = new Quad(16, face2Tex);
 			_strut2face2.addVertex(0, 1.0f, 0.1f, 0.0f);
 			_strut2face2.addVertex(1, 0.9f, 0, 0);
 			_strut2face2.addVertex(2, 0.5f, 0.9f, 0.5f);
 			_strut2face2.addVertex(3, 0.5f, 1.0f, 0.5f);
 			
 			Quad _strut2face3 = new Quad(17, face3Tex);
 			_strut2face3.addVertex(0, 1.0f, 0, 0.1f);
 			_strut2face3.addVertex(1, 0.9f, 0, 0);
 			_strut2face3.addVertex(2, 0.5f, 0.9f, 0.5f);
 			_strut2face3.addVertex(3, 0.5f, 0.9f, 0.5f);
 			
 			Quad _strut3face1 = new Quad(18, face1Tex);
 			_strut3face1.addVertex(0, 1.0f, 0.1f, 1.0f);
 			_strut3face1.addVertex(1, 0.9f, 0, 1);
 			_strut3face1.addVertex(2, 0.5f, 0.9f, 0.5f);
 			_strut3face1.addVertex(3, 0.5f, 1.0f, 0.5f);
 			
 			Quad _strut3face2 = new Quad(19, face2Tex);
 			_strut3face2.addVertex(0, 1.0f, 0.1f, 1.0f);
 			_strut3face2.addVertex(1, 1, 0, 0.9f);
 			_strut3face2.addVertex(2, 0.5f, 0.9f, 0.5f);
 			_strut3face2.addVertex(3, 0.5f, 1.0f, 0.5f);
 			
 			Quad _strut3face3 = new Quad(20, face3Tex);
 			_strut3face3.addVertex(0, 0.9f, 0, 1);
 			_strut3face3.addVertex(1, 1, 0, 0.9f);
 			_strut3face3.addVertex(2, 0.5f, 0.9f, 0.5f);
 			_strut3face3.addVertex(3, 0.5f, 1.0f, 0.5f);
 			
 			Quad _strut4face1 = new Quad(21, face1Tex);
 			_strut4face1.addVertex(0, 0.0f, 0.1f, 1.0f);
 			_strut4face1.addVertex(1, 0, 0, 0.9f);
 			_strut4face1.addVertex(2, 0.5f, 0.9f, 0.5f);
 			_strut4face1.addVertex(3, 0.5f, 1.0f, 0.5f);
 			
 			Quad _strut4face2 = new Quad(22, face2Tex);
 			_strut4face2.addVertex(0, 0.0f, 0.1f, 1.0f);
 			_strut4face2.addVertex(1, 0.1f, 0, 1);
 			_strut4face2.addVertex(2, 0.5f, 0.9f, 0.5f);
 			_strut4face2.addVertex(3, 0.5f, 1.0f, 0.5f);
 			
 			Quad _strut4face3 = new Quad(23, face3Tex);
 			_strut4face3.addVertex(0, 0.0f, 0.0f, 0.9f);
 			_strut4face3.addVertex(1, 0.1f, 0, 1);
 			_strut4face3.addVertex(2, 0.5f, 0.9f, 0.5f);
 			_strut4face3.addVertex(3, 0.5f, 1.0f, 0.5f);
 			
 			//surface
 			
 			Quad surfaceTop = new Quad(24, surTopTex);
 			surfaceTop.addVertex(0, 0.7f, 1, 0.7f);
 			surfaceTop.addVertex(1, 0.7f, 1, 0.3f);
 			surfaceTop.addVertex(2, 0.3f, 1, 0.3f);
 			surfaceTop.addVertex(3, 0.3f, 1, 0.7f);
 			
 			Quad _surfaceTop = new Quad(25, surTopTex);
 			_surfaceTop.addVertex(0, 0.3f, 1, 0.7f);
 			_surfaceTop.addVertex(1, 0.3f, 1, 0.3f);
 			_surfaceTop.addVertex(2, 0.7f, 1, 0.3f);
 			_surfaceTop.addVertex(3, 0.7f, 1, 0.7f);
 			
 			Quad surfaceBottom = new Quad(26, surBotTex);
 			surfaceBottom.addVertex(0, 0.7f, 0.9f, 0.7f);
 			surfaceBottom.addVertex(1, 0.7f, 0.9f, 0.3f);
 			surfaceBottom.addVertex(2, 0.3f, 0.9f, 0.3f);
 			surfaceBottom.addVertex(3, 0.3f, 0.9f, 0.7f);
 			
 			Quad _surfaceBottom = new Quad(27, surBotTex);
 			_surfaceBottom.addVertex(0, 0.3f, 0.9f, 0.7f);
 			_surfaceBottom.addVertex(1, 0.3f, 0.9f, 0.3f);
 			_surfaceBottom.addVertex(2, 0.7f, 0.9f, 0.3f);
 			_surfaceBottom.addVertex(3, 0.7f, 0.9f, 0.7f);
 			
 			Quad surfaceFace1 = new Quad(28, surSideTex);
 			surfaceFace1.addVertex(0, 0.7f, 1, 0.3f);
 			surfaceFace1.addVertex(1, 0.7f, 0.9f, 0.3f);
 			surfaceFace1.addVertex(2, 0.3f, 0.9f, 0.3f);
 			surfaceFace1.addVertex(3, 0.3f, 1, 0.3f);
 			
 			Quad _surfaceFace1 = new Quad(29, surSideTex);
 			_surfaceFace1.addVertex(0, 0.3f, 1, 0.3f);
 			_surfaceFace1.addVertex(1, 0.3f, 0.9f, 0.3f);
 			_surfaceFace1.addVertex(2, 0.7f, 0.9f, 0.3f);
 			_surfaceFace1.addVertex(3, 0.7f, 1, 0.3f);
 			
 			Quad surfaceFace3 = new Quad(30, surSideTex);
 			surfaceFace3.addVertex(0, 0.3f, 1, 0.7f);
 			surfaceFace3.addVertex(1, 0.3f, 0.9f, 0.7f);
 			surfaceFace3.addVertex(2, 0.7f, 0.9f, 0.7f);
 			surfaceFace3.addVertex(3, 0.7f, 1, 0.7f);
 			
 			Quad _surfaceFace3 = new Quad(31, surSideTex);
 			_surfaceFace3.addVertex(0, 0.7f, 1, 0.7f);
 			_surfaceFace3.addVertex(1, 0.7f, 0.9f, 0.7f);
 			_surfaceFace3.addVertex(2, 0.3f, 0.9f, 0.7f);
 			_surfaceFace3.addVertex(3, 0.3f, 1, 0.7f);
 			
 			Quad surfaceFace2 = new Quad(32, surSideTex);
 			surfaceFace2.addVertex(0, 0.7f, 1, 0.7f);
 			surfaceFace2.addVertex(1, 0.7f, 0.9f, 0.7f);
 			surfaceFace2.addVertex(2, 0.7f, 0.9f, 0.3f);
 			surfaceFace2.addVertex(3, 0.7f, 1, 0.3f);
 			
 			Quad _surfaceFace2 = new Quad(33, surSideTex);
 			_surfaceFace2.addVertex(0, 0.7f, 1, 0.3f);
 			_surfaceFace2.addVertex(1, 0.7f, 0.9f, 0.3f);
 			_surfaceFace2.addVertex(2, 0.7f, 0.9f, 0.7f);
 			_surfaceFace2.addVertex(3, 0.7f, 1, 0.7f);
 			
 			Quad surfaceFace4 = new Quad(34, surSideTex);
 			surfaceFace4.addVertex(0, 0.3f, 1, 0.3f);
 			surfaceFace4.addVertex(1, 0.3f, 0.9f, 0.3f);
 			surfaceFace4.addVertex(2, 0.3f, 0.9f, 0.7f);
 			surfaceFace4.addVertex(3, 0.3f, 1, 0.7f);
 			
 			Quad _surfaceFace4 = new Quad(35, surSideTex);
 			_surfaceFace4.addVertex(0, 0.3f, 1, 0.7f);
 			_surfaceFace4.addVertex(1, 0.3f, 0.9f, 0.7f);
 			_surfaceFace4.addVertex(2, 0.3f, 0.9f, 0.3f);
 			_surfaceFace4.addVertex(3, 0.3f, 1, 0.3f);
 			
 			
 			setQuad(strut1face1)
 			.setQuad(strut2face1)
 			.setQuad(strut3face1)
 			.setQuad(strut4face1)
 			.setQuad(_strut1face1)
 			.setQuad(_strut2face1)
 			.setQuad(_strut3face1)
 			.setQuad(_strut4face1)
 			.setQuad(strut1face2)
 			.setQuad(strut2face2)
 			.setQuad(strut3face2)
 			.setQuad(strut4face2)
 			.setQuad(_strut1face2)
 			.setQuad(_strut2face2)
 			.setQuad(_strut3face2)
 			.setQuad(_strut4face2)
 			.setQuad(strut1face3)
 			.setQuad(strut2face3)
 			.setQuad(strut3face3)
 			.setQuad(strut4face3)
 			.setQuad(_strut1face3)
 			.setQuad(_strut2face3)
 			.setQuad(_strut3face3)
 			.setQuad(_strut4face3)
 			.setQuad(surfaceTop)
 			.setQuad(_surfaceTop)
 			.setQuad(surfaceBottom)
 			.setQuad(_surfaceBottom)
 			.setQuad(surfaceFace1)
 			.setQuad(_surfaceFace1)
 			.setQuad(surfaceFace2)
 			.setQuad(_surfaceFace2)
 			.setQuad(surfaceFace3)
 			.setQuad(_surfaceFace3)
 			.setQuad(surfaceFace4)
 			.setQuad(_surfaceFace4);
 	}
 	
 }
