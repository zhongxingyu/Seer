 package assignment4.generatedMeshes;
 
 import javax.vecmath.Point3f;
 import javax.vecmath.Vector3f;
 
 import meshes.WireframeMesh;
 
 public class Cylinder2 {
 	
 	public WireframeMesh result;
 	Vector3f dir1, dir2;
 	float l;
 	float r;
 	
 	float pointDensity;
 	
 	Point3f pos1;
 	
 	public Cylinder2(float r, float l){
 		result = new WireframeMesh();
 		
 		dir1 = new Vector3f(1,0,0);
 		pos1 = new Point3f(-l/2,0,0);
 		
 		dir2 = new Vector3f(0,-1,0);
 		
 		pointDensity = 20;
 		
 		this.r=r;
 		this.l =l;
 		
 		setUp();
 	}
 	
 	
 	public void setUp(){
 		int perCircle = (int) Math.ceil(r*2* Math.PI *pointDensity);	
 		int nCircles = (int) Math.ceil(l *pointDensity);
 		
 		
 		
 		
 		Vector3f dirOrth = new Vector3f();
 		dirOrth.cross(dir1, dir2);
 		dirOrth.normalize();
 		dirOrth.scale(r);
 		
 		perCircle = (perCircle/4)*4;
 		
 		Point3f[][] points = new Point3f[nCircles][perCircle];
 		for(int i = 0; i < nCircles; i++){
 			float length = l * i /nCircles;
 			float delta = r / (perCircle/4);
 			for(int j = 0; j < perCircle/4; j++){
 
 				points[i][j] = new Point3f(pos1);
 				
 				points[i][j].x +=dir1.x*length;
 				points[i][j].y +=dir1.y*length + r;
 				points[i][j].z +=dir1.z*length - r + 2*j*delta;
 				
 				
 				result.vertices.add(points[i][j]);
 			}
 			for(int j = 0; j < perCircle/4; j++){
 
 				points[i][j] = new Point3f(pos1);
 				
 				points[i][j].x +=dir1.x*length;
 				points[i][j].y +=dir1.y*length + r - 2 * j * delta;
 				points[i][j].z +=dir1.z*length + r ;
 				
 				
 				result.vertices.add(points[i][j]);
 			}
 			for(int j = 0; j < perCircle/4; j++){
 
 				points[i][j] = new Point3f(pos1);
 				
 				points[i][j].x +=dir1.x*length;
 				points[i][j].y +=dir1.y*length - r;
 				points[i][j].z +=dir1.z*length + r - 2*j*delta;
 				
 				
 				result.vertices.add(points[i][j]);
 			}
 			for(int j = 0; j < perCircle/4; j++){
 
 				points[i][j] = new Point3f(pos1);
 				
 				points[i][j].x +=dir1.x*length;
 				points[i][j].y +=dir1.y*length - r + 2*j*delta;
 				points[i][j].z +=dir1.z*length -r;
 				
 				
 				result.vertices.add(points[i][j]);
 			}
 		}
 		
 		
 	
 		
 		//cylinder1
 		for(int i = 0; i < nCircles-1; i++){
 			for(int j = 0; j < perCircle; j++){
 				int[] fc = {i*perCircle + j, i*perCircle + (j+1)%perCircle, (i+1)*perCircle + (j+1)%perCircle};
 				int[] fc2 = {i*perCircle + j, (i+1)*perCircle + (j+1)%perCircle, (i+1)*perCircle + j};
 				
 				result.faces.add(fc);
 				result.faces.add(fc2);
 				
 			}
 		}
 		
 	}
 }
