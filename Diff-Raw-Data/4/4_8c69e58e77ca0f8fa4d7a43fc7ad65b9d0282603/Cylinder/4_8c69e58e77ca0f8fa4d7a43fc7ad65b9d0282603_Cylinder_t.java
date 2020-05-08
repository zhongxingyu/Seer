 package cs4620.shape;
 
 import java.util.HashMap;
 import java.util.Map;
 
 import javax.media.opengl.GL2;
 
 public class Cylinder extends TriangleMesh
 {
 	public Cylinder(GL2 gl)
 	{
 		super(gl);
 	}
 	
 	@Override
 	public void buildMesh(GL2 gl, float tolerance)
 	{
 		// TODO (Scene P2): Implement mesh generation for Cylinder. Your code should
 		// fill arrays of vertex positions/normals and vertex indices for triangles/lines
 		// and put this information in the GL buffers using the
 		//   set*()
 		// methods from TriangleMesh.
 		
 		float numSectors = (float)(2*Math.PI) / tolerance;
 		int rowLength = (int)(Math.ceil(numSectors));
 		int numPoints = (rowLength+1)*2 + 2;
 		
 		float[] vertexCoords = new float[numPoints*3*2];
 		float[] normalCoords = new float[numPoints*3*2];
 		int[] triangleVerts = new int[(numPoints*3 + numPoints*3 + numPoints*3)*2];
 		int[] wireframeVerts = new int[(numPoints*2 + numPoints)*2];
 		
 		float x = 0;
 		float y = 0;
 		float z = 0;
 		
 		int rowPos = 0;
 		
 		//Center point for the top face
 		vertexCoords[(numPoints*3)-6] = 0;
 		vertexCoords[(numPoints*3)-5] = 1;
 		vertexCoords[(numPoints*3)-4] = 0;
 		
 		//Center point for the bottom face
 		vertexCoords[(numPoints*3)-3] = 0;
 		vertexCoords[(numPoints*3)-2] = -1;
 		vertexCoords[(numPoints*3)-1] = 0;
 		
 		//Normal for the top face center point
 		normalCoords[(numPoints*3)-6] = 0;
 		normalCoords[(numPoints*3)-5] = 1;
 		normalCoords[(numPoints*3)-4] = 0;
 		
 		//Normal for the bottom face center point
 		normalCoords[(numPoints*3)-3] = 0;
 		normalCoords[(numPoints*3)-2] = -1;
 		normalCoords[(numPoints*3)-1] = 0;
 		
 		//Populate the points and normals for the top and bottom faces
 		for (float u = 0; u <= rowLength; u++) {
 			//Top face points
 			rowPos = (int)(u*3);
 			
 			x = (float)(Math.cos((u/rowLength) * 2 * Math.PI));
 			y = (float)1;
 			z = (float)(Math.sin((u/rowLength) * 2 *  Math.PI));
 			
 			vertexCoords[rowPos] = x;
 			vertexCoords[rowPos+1] = y;
 			vertexCoords[rowPos+2] = z;
 			
 			vertexCoords[rowPos+((numPoints+1)*3)] = x;
 			vertexCoords[rowPos+((numPoints+1)*3)+1] = y;
 			vertexCoords[rowPos+((numPoints+1)*3)+2] = z;
 			
 			//Top face normals
 			normalCoords[rowPos] = x;
 			normalCoords[rowPos+1] = 0;
 			normalCoords[rowPos+2] = z;
 			
 			normalCoords[rowPos+((numPoints+1)*3)] = 0;
 			normalCoords[rowPos+1+((numPoints+1)*3)] = 1;
 			normalCoords[rowPos+2+((numPoints+1)*3)] = 0;
 			
 			//Bottom face points
 			rowPos = (int)((u + rowLength + 1)*3);
 
 			y = (float)(-1);
 			
 			vertexCoords[rowPos] = x;
 			vertexCoords[rowPos+1] = y;
 			vertexCoords[rowPos+2] = z;
 			
 			vertexCoords[rowPos+((numPoints+1)*3)] = x;
 			vertexCoords[rowPos+((numPoints+1)*3)+1] = y;
 			vertexCoords[rowPos+((numPoints+1)*3)+2] = z;
 			
 			//Bottom face normals
 			normalCoords[rowPos] = x;
 			normalCoords[rowPos+1] = 0;
 			normalCoords[rowPos+2] = z;
 			
 			normalCoords[rowPos+((numPoints+1)*3)] = 0;
 			normalCoords[rowPos+1+((numPoints+1)*3)] = -1;
 			normalCoords[rowPos+2+((numPoints+1)*3)] = 0;
 		}
 	
 		//Populate the triangles and lines
 		for (int u = 0; u < rowLength; u++) {
 			//Top face triangles
 			rowPos = (int)(u*3);
 			 
 			triangleVerts[rowPos] = (2*(rowLength+1));
 			triangleVerts[rowPos+1] = u + 1 + numPoints+1;
 			triangleVerts[rowPos+2] = u +numPoints+1 ;
 			
 			//Bottom face triangles
 			rowPos = (u + rowLength+1)*3;
 			
 			triangleVerts[rowPos] = (2*(rowLength+1))+1;
			triangleVerts[rowPos+1] = u + rowLength+1 +  numPoints+1;
			triangleVerts[rowPos+2] = u + rowLength+2 +  numPoints+1;
 			
 			//Top face lines
 			int vertPos = (u*4);
 			
 			wireframeVerts[vertPos] = (2*(rowLength+1));
 			wireframeVerts[vertPos+1] = u;
 				
 			wireframeVerts[vertPos+2] = u;
 			wireframeVerts[vertPos+3] = u+1;
 			
 			//Bottom face lines
 			vertPos = (u + rowLength+1)*4;
 			
 			wireframeVerts[vertPos] = (2*(rowLength+1))+1;
 			wireframeVerts[vertPos+1] = u + rowLength + 1;
 				
 			wireframeVerts[vertPos+2] = u + rowLength + 1;
 			wireframeVerts[vertPos+3] = u + rowLength + 2;
 			
 			//Vertical lines
 			int linePos = (u + 2*(rowLength+1))*4;
 			
 			wireframeVerts[linePos] = u;
 			wireframeVerts[linePos+1] = u + rowLength + 1;
 			
 			
 			//Vertical triangles
 			int trianglePos = (2*(rowLength+1)*3 + u*6);
 			
 			triangleVerts[trianglePos] = u;
 			triangleVerts[trianglePos+1] = u + 1;
 			triangleVerts[trianglePos+2] = u + rowLength + 1;
 					
 			triangleVerts[trianglePos+3] = u + 1;
 			triangleVerts[trianglePos+4] = u + rowLength + 2;
 			triangleVerts[trianglePos+5] = u + rowLength + 1;
 			
 		}
 
 		setVertices(gl, vertexCoords);
 		setNormals(gl, normalCoords);
 		setTriangleIndices(gl, triangleVerts);
 		setWireframeIndices(gl, wireframeVerts);
 		
 	}
 
 	@Override
 	public Object getYamlObjectRepresentation()
 	{
 		Map<Object,Object> result = new HashMap<Object, Object>();
 		result.put("type", "Cylinder");
 		return result;
 	}
 }
