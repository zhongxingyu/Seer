 package cs4620.shape;
 
 import java.util.HashMap;
 import java.util.Map;
 
 import javax.media.opengl.GL2;
 
 public class Sphere extends TriangleMesh {
 
 	public Sphere(GL2 gl) {
 		super(gl);
 	}
 
 
 	@Override
 	public void buildMesh(GL2 gl, float tolerance)
 	{
 		// TODO (Scene P2): Implement mesh generation for Sphere. Your code should
 		// fill arrays of vertex positions/normals and vertex indices for triangles/lines
 		// and put this information in the GL buffers using the
 		//   set*()
 		// methods from TriangleMesh.
 		float numSectors = (float)(2*Math.PI) / tolerance;
 		int rowLength = (int)(Math.ceil(numSectors));
 		int numPoints = ((rowLength+1) * (rowLength+1));
 		
 		float[] vertexCoords = new float[numPoints*3];
 		float[] normalCoords = new float[numPoints*3];
 		float[] textureCoords = new float[numPoints*2];
 		int[] triangleVerts = new int[numPoints*3*2];
 		int[] wireframeVerts = new int[numPoints*2*2];
 		
 		float x = 0;
 		float y = 0;
 		float z = 0;
 		
 		float phi = 0;
 		float theta = 0;
 		
 		for (float v = 0; v <= rowLength; v++) {
 			for (float u = 0; u <= rowLength; u++) {
 				int rowPos = (int)(((v*rowLength) + u)*3);
 				
 				phi = (float)((v/rowLength) * Math.PI);
 				theta = (float)(2 * (u/rowLength) * Math.PI);
 				
 				x = (float)(Math.sin(phi) * Math.cos(theta));
 				y = (float)(Math.sin(phi) * Math.sin(theta));
 				z = (float)Math.cos(phi);
 				
 				vertexCoords[rowPos] = x;
 				vertexCoords[rowPos+1] = y;
 				vertexCoords[rowPos+2] = z;
 				
 				normalCoords[rowPos] = x;
 				normalCoords[rowPos+1] = y;
 				normalCoords[rowPos+2] = z;
 				
 				int texPos = (int)(((v*rowLength) + u)*2);
 
 				textureCoords[texPos] = u/rowLength;
 				textureCoords[texPos+1] = v/rowLength;
			
 			}
 		}
 		
 		for (int v = 0; v < rowLength; v++) {
 			for (int u = 0; u < rowLength; u++) {
 				int rowPos = (((v*rowLength) + u)*3*2);
 				
 				triangleVerts[rowPos] = v*rowLength + u;
 				triangleVerts[rowPos+1] = (v+1)*rowLength + u;
 				triangleVerts[rowPos+2] = triangleVerts[rowPos] + 1;
 				
 				triangleVerts[rowPos+3] = triangleVerts[rowPos+2];
 				triangleVerts[rowPos+4] = triangleVerts[rowPos+1];
 				triangleVerts[rowPos+5] = triangleVerts[rowPos+1] + 1;
 				
 				int vertPos = (((v*rowLength) + u)*2*2);
 				
 				if (u == rowLength-1) {
 					wireframeVerts[vertPos] = triangleVerts[rowPos];
 					wireframeVerts[vertPos+1] = triangleVerts[(v*rowLength)*3*2];
 					
 					wireframeVerts[vertPos+2] = triangleVerts[rowPos];
 					wireframeVerts[vertPos+3] = triangleVerts[rowPos+1];
 				}
 				else {
 					wireframeVerts[vertPos] = triangleVerts[rowPos];
 					wireframeVerts[vertPos+1] = triangleVerts[rowPos+1];
 					
 					wireframeVerts[vertPos+2] = triangleVerts[rowPos];
 					wireframeVerts[vertPos+3] = triangleVerts[rowPos+2];
 				}
 				
 			}
 		}
 		
 		setVertices(gl, vertexCoords);
 		setNormals(gl, normalCoords);
 		setTriangleIndices(gl, triangleVerts);
 		setWireframeIndices(gl, wireframeVerts);
 		setTexCoords(gl, textureCoords);
 		
 		// TODO (Shaders 2 P2): Generate texture coordinates for the sphere also.
 	}
 
 	@Override
 	public Object getYamlObjectRepresentation()
 	{
 		Map<Object,Object> result = new HashMap<Object, Object>();
 		result.put("type", "Sphere");
 		return result;
 	}
 
 
 }
