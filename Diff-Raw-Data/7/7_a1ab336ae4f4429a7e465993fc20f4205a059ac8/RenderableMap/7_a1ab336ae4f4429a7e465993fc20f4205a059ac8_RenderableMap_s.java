 package org.pa.boundless.render.map;
 
 import static java.lang.System.arraycopy;
 
 import java.nio.ByteBuffer;
 import java.util.ArrayList;
 
 import org.apache.commons.lang3.ArrayUtils;
 import org.pa.boundless.bsp.BspUtil;
 import org.pa.boundless.bsp.raw.BspFile;
 import org.pa.boundless.bsp.raw.Face;
 import org.pa.boundless.bsp.raw.Leaf;
 import org.pa.boundless.bsp.raw.Lightmap;
 import org.pa.boundless.bsp.raw.Vertex;
 
 import com.jme3.asset.AssetManager;
 import com.jme3.material.Material;
 import com.jme3.math.ColorRGBA;
 import com.jme3.scene.Geometry;
 import com.jme3.scene.Mesh;
 import com.jme3.scene.Node;
 import com.jme3.scene.VertexBuffer.Type;
 import com.jme3.texture.Image;
 import com.jme3.texture.Image.Format;
 import com.jme3.texture.Texture;
 import com.jme3.texture.Texture.WrapMode;
 import com.jme3.texture.Texture2D;
 import com.jme3.util.BufferUtils;
 
 public class RenderableMap {
 
 	private final Node rootNode;
 
 	public RenderableMap(AssetManager assetManager, BspFile bspFile) {
 		System.out.println(bspFile.toString());
 		// load textures
 		ArrayList<Texture> textures = new ArrayList<>();
 		for (org.pa.boundless.bsp.raw.Texture texture : bspFile.textures) {
 			String name = BspUtil.charsToString(texture.name);
 			Texture tex =assetManager.loadTexture(name + ".tga");
 			tex.setWrap(WrapMode.Repeat);
 			textures.add(tex);
 		}
 
 		// load lightmaps
 		ArrayList<Texture> lightmaps = new ArrayList<>();
 		for (Lightmap lightmap : bspFile.lightmaps) {
 			byte[] bytes = new byte[128 * 128 * 3];
 			int i = 0;
 			for (byte[][] row : lightmap.map) {
 				for (byte[] col : row) {
 					for (byte colorcomp : col) {
 						bytes[i++] = colorcomp;
 					}
 				}
 			}
 
 			Image img =
 					new Image(Format.RGB8, 128, 128, ByteBuffer.wrap(bytes));
 			lightmaps.add(new Texture2D(img));
 		}
 
 		rootNode = new Node("mapNode");
 
 		// load brushes
 		for (int iL = 0; iL < bspFile.leafs.length; iL++) {
 			Leaf leaf = bspFile.leafs[iL];
 			Node brush = new Node("brush" + iL);
 			for (int iF = leaf.leafface, iFMax = iF + leaf.n_leaffaces; iF < iFMax; iF++) {
 				// load vertices
 				Face face = bspFile.faces[bspFile.leaffaces[iF]];
 				int nV = face.n_vertexes;
 				float[] positionBuf = new float[3 * nV];
 				float[] texBuf = new float[2 * nV];
 				float[] lmBuf = new float[2 * nV];
 
 				System.out.println("FACE " + iF);
 
 				for (int iV = 0; iV < nV; iV++) {
 					Vertex v = bspFile.vertices[face.vertex + iV];
 					System.out.println(" Vertey " + iV);
 					System.out.println("  pos: "
 							+ ArrayUtils.toString(v.position));
 					System.out.println("  tex: "
 							+ ArrayUtils.toString(v.texcoord[0]));
 					System.out.println("  lm : "
 							+ ArrayUtils.toString(v.texcoord[1]));
 					arraycopy(v.position, 0, positionBuf, iV * 3, 3);
 					arraycopy(v.texcoord[0], 0, texBuf, iV * 2, 2);
					texBuf[iV * 2] /= 8;
					texBuf[iV * 2] += 0.5f;
					texBuf[iV * 2 + 1] /= -8;
					texBuf[iV * 2 + 1] += 0.5f;

 					arraycopy(v.texcoord[1], 0, lmBuf, iV * 2, 2);
 				}
 
 				// fix position buf: exchange x and y
 				for (int pi = 0; pi < positionBuf.length; pi += 3) {
 					float tmp = positionBuf[pi + 1];
 					positionBuf[pi + 1] = positionBuf[pi + 2];
 					positionBuf[pi + 2] = tmp;
 				}
 
 				// load triangulation
 				int[] indices = new int[face.n_meshverts];
 				arraycopy(bspFile.meshverts, face.meshvert, indices, 0,
 						indices.length);
 
 				// create mesh
 				Mesh faceMesh = new Mesh();
 				faceMesh.setBuffer(Type.Position, 3,
 						BufferUtils.createFloatBuffer(positionBuf));
 				faceMesh.setBuffer(Type.TexCoord, 2,
 						BufferUtils.createFloatBuffer(texBuf));
 				faceMesh.setBuffer(Type.TexCoord2, 2,
 						BufferUtils.createFloatBuffer(lmBuf));
 				faceMesh.setBuffer(Type.Index, 3,
 						BufferUtils.createIntBuffer(indices));
 				faceMesh.updateBound();
 
 				// create geometry: mest + texture + lightmap
 				Material mat =
 						new Material(assetManager,
 								"Common/MatDefs/Misc/Unshaded.j3md");
 				mat.setTexture("ColorMap", textures.get(face.texture));
 				mat.setTexture("LightMap", textures.get(face.lm_index));
 				mat.setColor("Color", ColorRGBA.Gray);
 				mat.setBoolean("SeparateTexCoord", true);
 
 				Geometry geom = new Geometry("face" + iF, faceMesh);
 				geom.setMaterial(mat);
 
 				brush.attachChild(geom);
 			}
 
 			rootNode.attachChild(brush);
 		}
 
 	}
 
 	public Node getRootNode() {
 		return rootNode;
 	}
 
 }
